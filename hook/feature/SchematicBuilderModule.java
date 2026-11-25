package hook.feature;

import hook.event.EventListener;
import hook.event.events.KeyEvent;
import hook.event.events.Render2DEvent;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BindSetting;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ButtonSetting;
import hook.module.setting.ItemSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1733;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_746;

/**
 * Builds the Litematica placement you have loaded: finds what is missing or wrong, walks there on a safe path
 * (bridging / pillaring with your scaffold block, breaking soft obstacles, never into lava), places every block with
 * the right facing while really looking at it, fetches materials from chests you marked and shows the progress with
 * an estimated finish time on the HUD.
 */
public final class SchematicBuilderModule extends Module {
    private final ItemSetting scaffold = new ItemSetting(EncryptedString.of("Scaffold Block"), class_1802.field_20412).onlyBlocks();
    private final NumberSetting speed = new NumberSetting(EncryptedString.of("Blocks / Second"), 1.0, 10.0, 4.0, 0.5);
    private final BooleanSetting human = new BooleanSetting(EncryptedString.of("Human Movement"), true);
    /** Only actions a vanilla player could do (Grim & co): real crosshair clicks, mouse-step rotations, no fake turns. */
    private final BooleanSetting antiCheat = new BooleanSetting(EncryptedString.of("Anticheat Safe"), true);
    private final BooleanSetting breakWrong = new BooleanSetting(EncryptedString.of("Fix Wrong Blocks"), true);
    private final BooleanSetting clearArea = new BooleanSetting(EncryptedString.of("Clear Area"), false);
    private final BooleanSetting breakObstacles = new BooleanSetting(EncryptedString.of("Break Obstacles"), true);
    private final BooleanSetting useScaffold = new BooleanSetting(EncryptedString.of("Bridge & Pillar"), true);
    private final BooleanSetting cleanup = new BooleanSetting(EncryptedString.of("Remove Scaffolding"), true);
    private final NumberSetting maxFall = new NumberSetting(EncryptedString.of("Max Fall"), 2.0, 10.0, 3.0, 1.0);
    private final BooleanSetting useStorage = new BooleanSetting(EncryptedString.of("Use Storage"), true);
    private final BindSetting markKey = new BindSetting(EncryptedString.of("Mark Storage Key"), 74, false); // J
    private final NumberSetting pauseHealth = new NumberSetting(EncryptedString.of("Pause Below Health"), 0.0, 20.0, 8.0, 1.0);
    private final BooleanSetting render = new BooleanSetting(EncryptedString.of("Show Plan"), true);
    private final BooleanSetting autoEat = new BooleanSetting(EncryptedString.of("Auto Eat"), true);
    private final NumberSetting eatBelow = new NumberSetting(EncryptedString.of("Eat Below Hunger"), 1.0, 19.0, 14.0, 1.0);
    private final BooleanSetting saveTools = new BooleanSetting(EncryptedString.of("Save Tools"), true);
    private final BooleanSetting autoMend = new BooleanSetting(EncryptedString.of("Auto Mend"), true);
    private final BooleanSetting placeWater = new BooleanSetting(EncryptedString.of("Place Water"), true);
    private final BooleanSetting finishStates = new BooleanSetting(EncryptedString.of("Finish States"), true);
    private final BooleanSetting layersOnly = new BooleanSetting(EncryptedString.of("Only Visible Layers"), true);
    private final BooleanSetting defend = new BooleanSetting(EncryptedString.of("Defend Yourself"), true);
    private final BooleanSetting lightUp = new BooleanSetting(EncryptedString.of("Light Up Area"), false);
    private final BooleanSetting redstoneLast = new BooleanSetting(EncryptedString.of("Redstone Last"), true);

    static SchematicBuilderModule instance;
    private final Random rnd = new Random();

    // ------------------------------------------------------------------ schematic + progress
    private Object placement;
    private String signature = "";
    private int[] box;
    private int totalBlocks;
    private String schematicName = "";
    private final Map<Long, int[]> chunkStats = new HashMap<>(); // chunk -> {total, done, unbuildable}
    /** Chunks where Litematica's layer range hides part of the schematic (progress counts only what is shown). */
    private final Set<Long> limitedChunks = new HashSet<>();
    private String layerKey = "";
    private final Map<Long, Long> chunkScannedAt = new HashMap<>();
    private final Map<Long, List<Long>> chunkTasks = new HashMap<>();
    private final Map<Long, Task> tasks = new HashMap<>();
    private final Set<Long> dirtyChunks = new HashSet<>();
    private final Map<Long, Long> failedUntil = new HashMap<>();
    private final Map<Long, Integer> failCount = new HashMap<>();
    private final Set<Long> scaffoldPlaced = new HashSet<>();
    private final Set<Long> helperPos = new HashSet<>();
    /** Blocks given up on after many failed tries (counted like unbuildable blocks). */
    private final Set<Long> skipped = new HashSet<>();
    /** Blocks this bot placed: if one ends up wrong it is always fixed, even with "Fix Wrong Blocks" off. */
    private final Set<Long> placedByMe = new HashSet<>();
    /** Placed blocks waiting for the server to confirm them (pos -> tick placed). */
    private final Map<Long, Long> verify = new HashMap<>();
    private final Map<Long, class_2680> verifyWant = new HashMap<>();
    private int wrongPlaced, stuckRetries, forceSneakTicks;
    private long lastClickTick = -1000;
    private boolean pauseLogged;
    private boolean escape;
    /** The plan was told in chat for this schematic. */
    private boolean planAnnounced;
    /** A screen the player had open while the bot works (pause menu, chat...): brought back after a chest trip. */
    private net.minecraft.class_437 bgScreen;
    private int workSpots;
    /** Spots we walked to that gave nothing: not again for a while. */
    private final Map<Long, Long> badSpots = new HashMap<>();
    private long lastSpot = Long.MIN_VALUE;
    private int lastSpotPlaced, placedTotal, pillarBuilt;
    /** Last time a block was placed or broken (watchdog against hanging around). */
    private long lastProgressMs;
    private boolean eating;
    private int eatTicks, lastMendCheck, replans;
    /** Head rotation at the start of this tick: the frames in between are drawn smoothly from it. */
    private float renderYaw = Float.NaN, renderPitch;
    private long renderTick = -1;
    private final ArrayDeque<long[]> doneLog = new ArrayDeque<>(); // {time ms, blocks}
    private int lastDone = -1;

    /** kind: SbFix.PLACE / ADJUST (click it) / GROW (click it again with the item) / CONVERT (tool) / WATERLOG. */
    private record Task(class_2338 pos, class_2680 want, boolean place, int kind) {
        Task(class_2338 pos, class_2680 want, boolean place) {
            this(pos, want, place, SbFix.PLACE);
        }

        boolean interact() {
            return this.kind == SbFix.ADJUST || this.kind == SbFix.CONVERT || this.kind == SbFix.WATERLOG;
        }
    }

    // ------------------------------------------------------------------ state machine
    private enum Mode { IDLE, PLACE, BREAK, WALK, OPEN_CHEST, TRANSFER, ESCAPE, INTERACT, REFILL, SHULKER }

    private Mode mode = Mode.IDLE;
    private String status = "Starting";
    private Task task;
    private class_2338 breakPos;
    private List<SbPath.Step> path;
    private int pathIdx;
    private Runnable arrived;
    private class_2338 walkGoalHint;
    private int waitTicks;
    private int modeTicks;
    private double bestDist;
    private int noProgress;
    private int stuckLevel;
    private final Map<Long, Long> avoid = new HashMap<>();
    private long tickNo;
    private long breakingTick = Long.MIN_VALUE;
    private int placedSincePause;
    private int lastAge = Integer.MIN_VALUE;
    private int freezeTicks;
    private int aimMiss;
    private int nextPauseAt = 40;
    private boolean announcedDone;

    // ------------------------------------------------------------------ storage
    private final Set<Long> storage = new HashSet<>();
    private final Map<Long, Map<class_1792, Integer>> storageSeen = new HashMap<>();
    private final Map<class_1792, Long> missingUntil = new HashMap<>();
    private String storageKey = "";
    private class_2338 chestPos;
    private Map<class_1792, Integer> wanted = new HashMap<>();
    private final Set<Long> visitedThisTrip = new HashSet<>();

    public SchematicBuilderModule() {
        super(EncryptedString.of("Schematic Builder"), EncryptedString.of("Builds the Litematica schematic you have loaded: walks safely, bridges and pillars with your scaffold block, places every block facing the right way, takes materials from chests you marked and shows the progress with the time left."), -1, Category.MISC);
        this.scaffold.setDescription(EncryptedString.of("Block used to bridge gaps and build up (removed again when done)."));
        this.speed.setDescription(EncryptedString.of("How fast blocks are placed. Lower looks more human."));
        this.human.setDescription(EncryptedString.of("Smooth head turns, varied timing and short breaks like a real player."));
        this.breakWrong.setDescription(EncryptedString.of("Breaks blocks that are wrong (or face the wrong way) and places the right one."));
        this.clearArea.setDescription(EncryptedString.of("Also removes blocks inside the schematic where it wants air."));
        this.breakObstacles.setDescription(EncryptedString.of("May break soft blocks that are in the way (never chests, never next to liquids)."));
        this.useScaffold.setDescription(EncryptedString.of("Places the scaffold block to cross gaps and to reach high places."));
        this.cleanup.setDescription(EncryptedString.of("Breaks the scaffold blocks it placed once the schematic is finished."));
        this.maxFall.setDescription(EncryptedString.of("Highest drop it will jump down (deeper only into water)."));
        this.useStorage.setDescription(EncryptedString.of("Takes missing materials from marked chests and drops off junk there."));
        this.markKey.setDescription(EncryptedString.of("Look at a chest, barrel or shulker and press this key to mark / unmark it as storage."));
        this.pauseHealth.setDescription(EncryptedString.of("Stops building when your health drops below this (0 = never)."));
        this.render.setDescription(EncryptedString.of("Highlights the next block, the path and your storage chests."));
        ButtonSetting clear = new ButtonSetting(EncryptedString.of("Unmark All Storage"), () -> {
            this.storage.clear();
            this.storageSeen.clear();
            this.saveStorage();
            this.say("All storage chests unmarked.");
        });
        this.autoEat.setDescription(EncryptedString.of("Stops for a moment and eats when hungry (or hurt), then goes on building."));
        this.eatBelow.setDescription(EncryptedString.of("Hunger level (out of 20) at which it eats."));
        this.saveTools.setDescription(EncryptedString.of("Never uses a tool that is about to break."));
        this.autoMend.setDescription(EncryptedString.of("When XP orbs are around, puts a damaged Mending item in the offhand so it gets repaired."));
        this.placeWater.setDescription(EncryptedString.of("Places the water sources of the schematic (farms...) with water buckets - always last."));
        this.finishStates.setDescription(EncryptedString.of("Clicks repeaters, comparators, note blocks and trapdoors to the right setting, makes double slabs / more candles, tills farmland, strips logs and waterlogs blocks like the schematic."));
        this.layersOnly.setDescription(EncryptedString.of("Only builds the layers Litematica shows (its layer range): build a big schematic layer by layer."));
        this.lightUp.setDescription(EncryptedString.of("Places torches on dark ground around the build (block light 0, where mobs spawn at night) - like you light up around your base. Needs torches."));
        this.defend.setDescription(EncryptedString.of("Hits hostile mobs that come close (with your best sword or axe) instead of letting them kill you."));
        this.redstoneLast.setDescription(EncryptedString.of("Places observers, pistons, redstone and power sources after the blocks around them, so half-built machines don't fire."));
        ButtonSetting mats = new ButtonSetting(EncryptedString.of("Show Missing Materials"), this::listMaterials);
        this.addSettings(this.scaffold, this.speed, this.human, this.antiCheat, this.breakWrong, this.clearArea, this.breakObstacles, this.useScaffold,
                this.cleanup, this.maxFall, this.useStorage, this.markKey, clear, mats, this.pauseHealth, this.autoEat, this.eatBelow, this.saveTools,
                this.autoMend, this.placeWater, this.finishStates, this.redstoneLast, this.layersOnly, this.defend, this.lightUp, this.render);
        instance = this;
    }

    /** The progress bar is a HUD element: draw it even when the HUD module itself is turned off. */
    @EventListener
    public void onRender2D(Render2DEvent event) {
        HudModule h = HudModule.instance;
        if (h != null && !h.isEnabled() && progress() != null) h.renderEffectsOnly(event);
    }

    // ================================================================== self-test hooks

    void testMarkStorage(class_2338 p) {
        this.storage.add(p.method_10063());
    }

    String testStatus() {
        StringBuilder r = new StringBuilder();
        if (this.tasks.size() <= 10) for (Task t : this.tasks.values()) r.append(' ').append(t.place ? "" : "x").append(t.pos.method_23854().replace(" ", "")).append(this.failedUntil.containsKey(t.pos.method_10063()) ? "F" : "");
        return this.mode + " / " + this.status + " tasks=" + this.tasks.size() + " scaffold=" + this.scaffoldPlaced.size() + " cut=" + this.walksCut + " pillarBuilt=" + this.pillarBuilt + " saved=" + this.savedBlocks + r;
    }

    // ================================================================== HUD

    /** Progress for the HUD card (null while the module is off). */
    record Progress(String name, int done, int total, double percent, String eta, String state, boolean problem) {
    }

    static Progress progress() {
        SchematicBuilderModule m = instance;
        if (m == null || !m.isEnabled()) return null;
        return m.makeProgress();
    }

    private Progress makeProgress() {
        if (this.placement == null) return new Progress("No schematic", 0, 0, 0, "", this.status, true);
        int done = 0, unb = 0;
        for (int[] s : this.chunkStats.values()) { done += s[1]; unb += s[2]; }
        int total = Math.max(1, (this.totalBlocks > 0 && this.limitedChunks.isEmpty() ? this.totalBlocks : this.scannedTotal()) - unb);
        done = Math.min(done, total);
        double pct = done * 100.0 / total;
        String eta = "";
        if (done >= total) eta = "done";
        else {
            double rate = this.rate();
            if (rate > 0.01) {
                long sec = Math.round((total - done) / rate);
                java.time.LocalTime at = java.time.LocalTime.now().plusSeconds(sec);
                eta = "~" + duration(sec) + " left · done at " + String.format(java.util.Locale.ROOT, "%02d:%02d", at.getHour(), at.getMinute());
            } else eta = "measuring speed...";
        }
        boolean problem = this.status.startsWith("Missing") || this.status.startsWith("Stuck") || this.status.startsWith("Paused") || this.status.startsWith("No ");
        return new Progress(this.schematicName, done, total, pct, eta, this.status, problem);
    }

    private int scannedTotal() {
        int t = 0;
        for (int[] s : this.chunkStats.values()) t += s[0];
        return t;
    }

    static String duration(long sec) {
        if (sec >= 3600) return (sec / 3600) + "h " + ((sec % 3600) / 60) + "m";
        if (sec >= 60) return (sec / 60) + "m " + (sec % 60) + "s";
        return sec + "s";
    }

    /** Blocks finished per second over the last few minutes. */
    private double rate() {
        if (this.doneLog.size() < 2) return 0;
        long[] a = this.doneLog.peekFirst(), b = this.doneLog.peekLast();
        double secs = (b[0] - a[0]) / 1000.0;
        if (secs < 8) return 0;
        return Math.max(0, (b[1] - a[1]) / secs);
    }

    // ================================================================== lifecycle

    @Override
    public void onEnable() {
        this.resetAll();
        this.storageKey = this.mc.field_1687 == null ? "" : this.worldKey();
        this.loadStorage();
        if (!SbLitematica.available()) {
            this.say("Litematica is not installed - load a schematic with Litematica first.");
            this.status = "No Litematica";
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (this.eating) this.stopEat();
        this.releaseKeys();
        if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB wrongPlaced=" + this.wrongPlaced + " workSpots=" + this.workSpots);
        if (this.mc.field_1761 != null) this.mc.field_1761.method_2925();
        super.onDisable();
    }

    private void resetAll() {
        this.lightPos.clear();
        this.torchesPlaced = 0;
        this.shulkerPos = null;
        this.shulkerStage = 0;
        this.shulkerCheckAt = 0;
        this.buildItems.clear();
        this.placement = null;
        this.badSpots.clear();
        this.lastSpot = Long.MIN_VALUE;
        this.lastProgressMs = 0;
        this.planAnnounced = false;
        this.workSpots = 0;
        this.placedByMe.clear();
        this.verify.clear();
        this.verifyWant.clear();
        this.wrongPlaced = 0;
        SbAcTest.reset();
        this.signature = "";
        this.chunkStats.clear();
        this.limitedChunks.clear();
        this.chunkScannedAt.clear();
        this.chunkTasks.clear();
        this.tasks.clear();
        this.dirtyChunks.clear();
        this.failedUntil.clear();
        this.failCount.clear();
        this.skipped.clear();
        this.doneLog.clear();
        this.lastDone = -1;
        this.announcedDone = false;
        this.scaffoldWarned = false;
        this.scaffoldWaits = 0;
        this.missingUntil.clear();
        this.toIdle();
    }

    public static boolean ownsBreaking() {
        SchematicBuilderModule m = instance;
        return m != null && m.isEnabled() && m.breakingTick >= m.tickNo - 1;
    }

    private void say(String msg) {
        if (this.mc.field_1724 != null) this.mc.field_1724.method_7353(class_2561.method_43470("§b[Builder] §f" + msg), false);
    }

    // ================================================================== tick

    @EventListener
    public void onTick(TickEvent event) {
        class_746 pl = this.mc.field_1724;
        class_638 w = this.mc.field_1687;
        if (pl == null || w == null || this.mc.field_1761 == null) return;
        // the tick event fires twice per game tick (client tick + player tick): act exactly once, at the start of
        // the tick like real input does, so every click reaches the server before that tick's movement packet
        if (pl.field_6012 == this.lastAge) return;
        this.lastAge = pl.field_6012;
        this.tickNo++;
        // the game drops to 30 fps after a minute without mouse / keyboard input and to 10 fps after ten ("AFK"
        // inactivity limit) - and you don't touch anything while the bot builds, so it looked like 5 fps. The bot's
        // own moving counts as input.
        try { this.mc.method_61964().method_61939(); } catch (Throwable ignored) { }
        this.renderYaw = pl.method_36454();
        this.renderPitch = pl.method_36455();
        this.renderTick = this.tickNo;
        if (this.freezeTicks > 0) this.freezeTicks--;
        if (this.forceSneakTicks > 0) this.forceSneakTicks--;
        SbPlacer.forced = !this.antiCheat.getValue();
        SbAcTest.mouseStep = this.mouseStep();
        this.checkPlaced(w);
        String key = this.worldKey();
        if (!key.equals(this.storageKey)) { this.storageKey = key; this.loadStorage(); }

        if (!this.updatePlacement(pl)) {
            this.releaseKeys();
            return;
        }
        this.scan(w, pl);
        this.logProgress();
        this.announcePlan(w, pl);

        boolean ownScreen = this.mode == Mode.TRANSFER || this.mode == Mode.OPEN_CHEST;
        net.minecraft.class_437 scr = this.mc.field_1755;
        if ((scr instanceof net.minecraft.class_465 || scr instanceof net.minecraft.class_7743) && !ownScreen && this.tickNo - this.lastClickTick <= 20) {
            // a click opened a block's menu or a sign editor (shouldn't happen for menus, we sneak): close it again
            if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB closed a menu our click opened: " + scr.getClass().getSimpleName());
            if (scr instanceof net.minecraft.class_465) pl.method_7346();
            else scr.method_25419();
            this.restoreScreen();
        }
        // the pause menu, chat, options, the click gui, an unfocused window... don't stop the build (on a server the
        // game keeps running behind them). Only real inventories / containers and the death screen pause it.
        scr = this.mc.field_1755;
        boolean blocking = scr instanceof net.minecraft.class_465 || scr instanceof net.minecraft.class_418;
        if (!(scr instanceof net.minecraft.class_465)) this.bgScreen = scr != null && !blocking ? scr : null;
        if (scr != null && blocking && !ownScreen) {
            if (!this.pauseLogged && System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB paused by screen " + this.mc.field_1755.getClass().getName());
            this.pauseLogged = true;
            this.releaseKeys();
            this.status = "Paused (menu open)";
            return;
        }
        // eat when hungry (also when hurt: health only comes back with a full enough hunger bar)
        if (this.tickEat(pl)) return;
        // safety first
        if (this.pauseHealth.getValue() > 0 && pl.method_6032() < this.pauseHealth.getValue() && this.mode != Mode.ESCAPE) {
            this.releaseKeys();
            this.status = "Paused (low health)";
            return;
        }
        if ((pl.method_5771() || pl.method_5809() || SbWorld.nearLava(w, feet(pl))) && this.mode != Mode.ESCAPE) this.startEscape(w, pl);
        if (this.tickDefend(w, pl)) return;

        if (this.waitTicks > 0) {
            this.waitTicks--;
            this.keys(false, false, false, false);
            return;
        }
        this.watchdog(w, pl);
        this.modeTicks++;
        switch (this.mode) {
            case IDLE -> this.decide(w, pl);
            case PLACE -> this.tickPlace(w, pl);
            case BREAK -> this.tickBreak(w, pl);
            case WALK -> this.tickWalk(w, pl);
            case OPEN_CHEST -> this.tickOpenChest(w, pl);
            case TRANSFER -> this.tickTransfer(w, pl);
            case ESCAPE -> this.tickEscape(w, pl);
            case INTERACT -> this.tickInteract(w, pl);
            case REFILL -> this.tickRefill(w, pl);
            case SHULKER -> this.tickShulker(w, pl);
        }
    }

    private static class_2338 feet(class_746 pl) {
        return class_2338.method_49637(pl.method_23317(), pl.method_23318() + 0.01, pl.method_23321());
    }

    private void toIdle() {
        this.mode = Mode.IDLE;
        this.modeTicks = 0;
        this.task = null;
        this.breakPos = null;
        this.path = null;
        this.arrived = null;
        this.releaseKeys();
    }

    // ================================================================== schematic tracking

    private boolean updatePlacement(class_746 pl) {
        Object p = SbLitematica.placement(pl.method_23317(), pl.method_23318(), pl.method_23321());
        if (p == null) {
            if (this.placement != null) this.resetAll();
            this.status = SbLitematica.available() ? "No schematic loaded in Litematica" : "No Litematica";
            return false;
        }
        String sig = SbLitematica.signature(p);
        if (p != this.placement || !sig.equals(this.signature)) {
            boolean first = this.placement == null;
            this.resetAll();
            this.placement = p;
            this.signature = sig;
            this.box = SbLitematica.box(p);
            this.totalBlocks = SbLitematica.totalBlocks(p);
            this.schematicName = SbLitematica.name(p);
            if (this.box == null) return false;
            this.say((first ? "Building " : "Schematic changed - now building ") + "§e" + this.schematicName + "§f ("
                    + (this.box[3] - this.box[0] + 1) + "x" + (this.box[4] - this.box[1] + 1) + "x" + (this.box[5] - this.box[2] + 1) + ")");
        }
        if (this.tickNo % 20 == 0) {
            String lk = this.layersOnly.getValue() ? SbLitematica.layerKey() : "";
            if (!lk.equals(this.layerKey)) {
                if (!this.layerKey.isEmpty() || !lk.isEmpty()) { this.chunkScannedAt.clear(); this.announcedDone = false; }
                this.layerKey = lk;
            }
        }
        return this.box != null;
    }

    /** Rescans one chunk per tick: what is missing / wrong there, and how much is finished. */
    private void scan(class_638 w, class_746 pl) {
        class_1937 sw = SbLitematica.world();
        if (sw == null) return;
        long now = System.currentTimeMillis();
        int pcx = (int) Math.floor(pl.method_23317()) >> 4, pcz = (int) Math.floor(pl.method_23321()) >> 4;
        long pick = Long.MIN_VALUE;
        double pickScore = Double.MAX_VALUE;
        for (int cx = this.box[0] >> 4; cx <= this.box[3] >> 4; cx++) for (int cz = this.box[2] >> 4; cz <= this.box[5] >> 4; cz++) {
            if (!w.method_2935().method_12123(cx, cz) || !SbLitematica.loaded(sw, cx, cz)) continue;
            long k = BaseFinder.key(cx, cz);
            Long at = this.chunkScannedAt.get(k);
            double dist = Math.abs(cx - pcx) + Math.abs(cz - pcz);
            double age = at == null ? 1.0E9 : now - at;
            boolean dirty = this.dirtyChunks.contains(k);
            double need = at == null ? 0 : dirty && age > 700 ? 1 : age > (dist <= 2 ? 4000 : 20000) ? 2 : -1;
            if (need < 0) continue;
            double score = need * 1000 + dist;
            if (score < pickScore) { pickScore = score; pick = k; }
        }
        if (pick != Long.MIN_VALUE) this.scanChunk(w, sw, net.minecraft.class_1923.method_8325(pick), net.minecraft.class_1923.method_8332(pick), pick, now);
    }

    private void scanChunk(class_638 w, class_1937 sw, int cx, int cz, long key, long now) {
        int x0 = Math.max(this.box[0], cx << 4), x1 = Math.min(this.box[3], (cx << 4) + 15);
        int z0 = Math.max(this.box[2], cz << 4), z1 = Math.min(this.box[5], (cz << 4) + 15);
        int y0 = Math.max(this.box[1], w.method_31607()), y1 = Math.min(this.box[4], w.method_31600());
        List<Long> old = this.chunkTasks.remove(key);
        if (old != null) for (Long p : old) this.tasks.remove(p);
        List<Long> list = new ArrayList<>();
        int total = 0, done = 0, unb = 0;
        boolean limited = false;
        Object layers = this.layersOnly.getValue() ? SbLitematica.layerRange() : null;
        boolean fix = this.finishStates.getValue();
        class_2338.class_2339 m = new class_2338.class_2339();
        for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) for (int y = y0; y <= y1; y++) {
            m.method_10103(x, y, z);
            class_2680 want = sw.method_8320(m);
            if (layers != null && !want.method_26215() && !SbLitematica.inLayers(layers, x, y, z)) { limited = true; continue; }
            class_2680 have = w.method_8320(m);
            if (want.method_26215()) {
                if (this.clearArea.getValue() && !have.method_26215() && !have.method_45474() && SbWorld.breakable(w, m)) {
                    class_2338 p = m.method_10062();
                    this.tasks.put(p.method_10063(), new Task(p, want, false));
                    list.add(p.method_10063());
                }
                continue;
            }
            total++;
            if (!SbWorld.matches(want, have)) {
                // remember what the build still needs, so a full inventory never drops it into a chest
                this.buildItems.add(itemFor(want));
                class_2680 base = SbFix.convertBase(want);
                if (base != null) this.buildItems.add(itemFor(base));
            }
            if (want.method_26204() == class_2246.field_10382) {
                // water: sources get placed with a bucket (farms), flowing water comes by itself
                boolean src = want.toString().contains("level=0");
                boolean haveSrc = have.method_26204() == class_2246.field_10382 && have.toString().contains("level=0");
                if (!src || haveSrc) { done++; continue; }
                if (!this.placeWater.getValue() || this.skipped.contains(m.method_10063())) { unb++; continue; }
                class_2338 p = m.method_10062();
                if (have.method_26215() || have.method_45474()) {
                    this.tasks.put(p.method_10063(), new Task(p, want, true));
                    list.add(p.method_10063());
                } else if ((this.breakWrong.getValue() || this.placedByMe.contains(p.method_10063())) && SbWorld.breakable(w, p)) {
                    this.tasks.put(p.method_10063(), new Task(p, want, false));
                    list.add(p.method_10063());
                }
                continue;
            }
            if (SbWorld.matches(want, have)) {
                // waterlogged in the schematic: pour water into it (last, like water)
                if (fix && SbFix.waterlogged(want) && !SbFix.waterlogged(have) && SbFix.get(have, "waterlogged") != null && this.placeWater.getValue()
                        && !this.skipped.contains(m.method_10063())) {
                    class_2338 p = m.method_10062();
                    this.tasks.put(p.method_10063(), new Task(p, want, true, SbFix.WATERLOG));
                    list.add(p.method_10063());
                    continue;
                }
                done++;
                continue;
            }
            if (fix && !this.skipped.contains(m.method_10063()) && !SbWorld.secondaryPart(want)) {
                // the right block is there but needs a click (repeater delay...), another item on it (double slab,
                // candles...) or a tool (farmland, path, stripped log): no need to break it
                int kind = SbFix.grows(want, have) ? SbFix.GROW : SbFix.clicks(want, have) > 0 ? SbFix.ADJUST
                        : SbFix.convertBase(want) != null && SbFix.convertsFrom(want, have) ? SbFix.CONVERT : -1;
                if (kind >= 0) {
                    class_2338 p = m.method_10062();
                    this.tasks.put(p.method_10063(), new Task(p, want, true, kind));
                    list.add(p.method_10063());
                    continue;
                }
                class_2680 base = SbFix.convertBase(want);
                if (base != null && (have.method_26215() || have.method_45474())
                        && (SbFix.needsAirAbove(want) || this.count(want.method_26204().method_8389()) == 0 && this.count(base.method_26204().method_8389()) > 0)) {
                    // farmland / path / stripped log: place the dirt or the log first, then use the tool on it
                    class_2338 p = m.method_10062();
                    this.tasks.put(p.method_10063(), new Task(p, base, true));
                    list.add(p.method_10063());
                    continue;
                }
            }
            if (SbFix.waterPlant(want) && !(isWater(have) && have.toString().contains("level=0")) && (have.method_26215() || have.method_45474())
                    && this.placeWater.getValue() && !this.skipped.contains(m.method_10063())) {
                // kelp / seagrass: a water source first (bucket), then the plant into it
                class_2338 p = m.method_10062();
                this.tasks.put(p.method_10063(), new Task(p, class_2246.field_10382.method_9564(), true));
                list.add(p.method_10063());
                continue;
            }
            class_1792 item = itemFor(want);
            boolean noItem = !(item instanceof class_1747) || !want.method_26227().method_15769() && want.method_26204() instanceof net.minecraft.class_2404;
            if (SbWorld.secondaryPart(want)) {
                if (have.method_26204() == want.method_26204()) done++;
                else if (noItem) unb++;
                continue;
            }
            if (noItem || this.skipped.contains(m.method_10063())) { unb++; continue; }
            class_2338 p = m.method_10062();
            if (have.method_26215() || have.method_45474()) {
                this.tasks.put(p.method_10063(), new Task(p, want, true));
                list.add(p.method_10063());
            } else if ((this.breakWrong.getValue() || this.placedByMe.contains(p.method_10063())) && SbWorld.breakable(w, p) || this.scaffoldPlaced.contains(p.method_10063())) {
                this.tasks.put(p.method_10063(), new Task(p, want, false));
                list.add(p.method_10063());
            }
        }
        this.chunkStats.put(key, new int[]{total, done, unb});
        if (limited) this.limitedChunks.add(key);
        else this.limitedChunks.remove(key);
        this.chunkTasks.put(key, list);
        this.chunkScannedAt.put(key, now);
        this.dirtyChunks.remove(key);
    }

    private void logProgress() {
        int done = 0;
        for (int[] s : this.chunkStats.values()) done += s[1];
        long now = System.currentTimeMillis();
        if (done != this.lastDone) {
            this.lastDone = done;
            this.doneLog.addLast(new long[]{now, done});
        }
        while (this.doneLog.size() > 2 && now - this.doneLog.peekFirst()[0] > 180_000L) this.doneLog.pollFirst();
    }

    private void touched(class_2338 p) {
        this.dirtyChunks.add(BaseFinder.key(p.method_10263() >> 4, p.method_10260() >> 4));
    }

    // ================================================================== decisions

    private double reach() {
        return Math.min(4.5, this.mc.field_1724.method_55754()) - 0.25;
    }

    private class_1747 scaffoldItem() {
        return this.scaffold.getItem() instanceof class_1747 b ? b : (class_1747) class_1802.field_20412;
    }

    private void decide(class_638 w, class_746 pl) {
        long now = System.currentTimeMillis();
        this.failedUntil.values().removeIf(t -> t < now);
        this.avoid.values().removeIf(t -> t < now);
        this.missingUntil.values().removeIf(t -> t < now);

        // human: a short break every now and then
        if (this.human.getValue() && this.placedSincePause >= this.nextPauseAt) {
            this.placedSincePause = 0;
            this.nextPauseAt = 25 + this.rnd.nextInt(50);
            this.waitTicks = 10 + this.rnd.nextInt(40);
            return;
        }

        if (this.tryMend(w, pl)) return;
        if (this.tryUnseal(w, pl)) return;
        // water to place but only empty buckets: fill them at a water source first
        if (this.tryRefill(w, pl)) return;
        // material we need is in a shulker box we carry: set it down, take it out, pick the box up again
        if (this.tryShulker(w, pl)) return;
        // dark ground around the build: a torch, so no mobs spawn next to us
        if (this.tryLight(w, pl)) return;
        // restock when the scaffold block runs low or the nearby work needs something we don't carry
        if (this.useStorage.getValue() && !this.storage.isEmpty() && this.wantsRestock(pl)) {
            if (this.startRestock(w, pl)) return;
        }

        List<Task> cands = this.candidates(pl, 70);
        if (cands.isEmpty()) {
            // everything left needs material we don't have
            for (Task t : this.tasks.values()) {
                if (!t.place || this.failedUntil.containsKey(t.pos.method_10063())) continue;
                class_1792 it = this.requiredItem(t);
                if (it != null && this.count(it) == 0) {
                    if (this.useStorage.getValue() && !this.storage.isEmpty() && this.startRestock(w, pl)) return;
                    this.missing(it);
                    this.status = "Missing " + it.method_63680().getString();
                    return;
                }
            }
            this.nothingToDo(w, pl);
            return;
        }
        class_243 eye = new class_243(pl.method_23317(), pl.method_23318() + 1.27, pl.method_23321());
        int sims = 0;
        for (Task t : cands) {
            if (!t.place) {
                if (eye.method_1025(class_243.method_24953(t.pos)) <= this.reach() * this.reach() && this.visibleFace(w, pl, t.pos) != null) {
                    this.startBreak(t.pos, t);
                    return;
                }
                continue;
            }
            if (t.interact()) {
                if (eye.method_1025(class_243.method_24953(t.pos)) <= this.reach() * this.reach() && this.interactFace(w, pl, t, eye) != null) {
                    this.startInteract(t);
                    return;
                }
                continue;
            }
            class_1792 item = itemFor(t.want);
            if (this.missingUntil.containsKey(item) || this.count(item) == 0) continue;
            if (eye.method_1025(class_243.method_24953(t.pos)) > (this.reach() + 0.8) * (this.reach() + 0.8)) continue;
            if (sims++ > 30) break;
            if (isWater(t.want) ? this.waterClick(w, pl, t.pos, eye) != null : this.solution(t, (class_1747) item, eye, this.reach()) != null) {
                this.task = t;
                this.mode = Mode.PLACE;
                this.modeTicks = 0;
                this.status = "Building";
                return;
            }
        }
        // nothing reachable from here: walk to the best task
        int tries = 0;
        Task floating = null;
        for (Task t : cands) {
            if (t.place && t.kind == SbFix.PLACE && !this.supported(w, t.pos)) { // nothing to build it against yet: comes later
                if (floating == null && this.count(itemFor(t.want)) > 0 && !this.missingUntil.containsKey(itemFor(t.want))) floating = t;
                continue;
            }
            if (tries++ >= 5) break;
            class_1792 item = t.place ? this.requiredItem(t) : null;
            if (item != null) {
                if (this.missingUntil.containsKey(item)) continue;
                if (this.count(item) == 0) {
                    if (this.useStorage.getValue() && !this.storage.isEmpty() && this.startRestock(w, pl)) return;
                    this.missing(item);
                    continue;
                }
            }
            // tried before and still impossible: put a helper block next to it (top slabs, upside-down stairs...)
            boolean plain = t.place && t.kind == SbFix.PLACE;
            if (plain && this.failCount.getOrDefault(t.pos.method_10063(), 0) > 2 && this.tryHelper(w, pl, t)) return;
            if (plain && this.failCount.getOrDefault(t.pos.method_10063(), 0) == 0 && this.walkToWorkSpot(w, pl, t)) return;
            if (this.walkNear(w, pl, t.pos, t)) return;
            if (plain && this.tryHelper(w, pl, t)) return;
            this.fail(t.pos, 20_000L, "nowalk");
        }
        // only blocks with nothing around them are left (a ridge of slabs over an empty attic): "later" never
        // comes for those - give the first one a helper block to sit on, walking there first if needed
        if (tries == 0 && floating != null && !this.anySupportedLeft(w)) {
            dbg("only floating blocks left - helper for " + floating.pos.method_23854());
            if (this.tryHelper(w, pl, floating)) return;
            if (this.walkNear(w, pl, floating.pos, floating)) { this.status = "Walking (block needs a helper)"; return; }
            this.fail(floating.pos, 20_000L, "nohelper");
            return;
        }
        // everything left waits for something that is on cool-down: retry that instead of standing around
        if (tries == 0) { this.nothingToDo(w, pl); return; }
        this.status = "Waiting (nothing reachable)";
    }

    /** Pending tasks, lowest layer first, then closest - but don't run across the whole build for one block. */
    private List<Task> candidates(class_746 pl, int max) {
        List<Task> all = new ArrayList<>();
        double px = pl.method_23317(), py = pl.method_23318(), pz = pl.method_23321();
        int minY = Integer.MAX_VALUE;
        Map<class_1792, Boolean> carry = new HashMap<>();
        class_638 cw = this.mc.field_1687;
        for (Task t : this.tasks.values()) {
            if (this.failedUntil.containsKey(t.pos.method_10063())) continue;
            // water only once the blocks around it stand, or it floods the build
            if (t.place && (isWater(t.want) || t.kind == SbFix.WATERLOG) && this.waterTooEarly(t)) continue;
            // never close the last open side of a block that still has to be placed (it could never be reached)
            if (t.place && t.kind == SbFix.PLACE && this.sealsPending(t.pos)) continue;
            // not its turn yet: what it stands on / hangs on isn't there (crops before farmland, torches before
            // their wall, sand with nothing below it would fall)
            if (t.place && t.kind == SbFix.PLACE && !isWater(t.want) && !this.placeableNow(cw, t)) continue;
            // a hoe / shovel needs air above the block
            if (t.kind == SbFix.CONVERT && SbFix.needsAirAbove(t.want) && !this.airAboveOrClearable(cw, t.pos)) continue;
            // only work we can do right now (material in the inventory); the rest waits for a restock
            class_1792 need = t.place ? this.requiredItem(t) : null;
            if (need != null && !carry.computeIfAbsent(need, it -> this.count(it) > 0)) continue;
            all.add(t);
            if (t.place) minY = Math.min(minY, t.pos.method_10264());
        }
        final int floorY = minY;
        final float yaw = pl.method_36454();
        Map<Task, Double> sc = new HashMap<>();
        class_638 w = this.mc.field_1687;
        class_1792 held = pl.method_6047().method_7909();
        for (Task t : all) {
            // the same block as in the hand first: fewer item switches (faster and less bot-like)
            double swap = t.place && t.kind != SbFix.ADJUST && this.requiredItem(t) != held ? 1.2 : 0.0;
            sc.put(t, this.taskScore(t, px, py, pz, floorY, yaw) + this.planScore(w, t) + swap);
        }
        all.sort((a, b) -> Double.compare(sc.get(a), sc.get(b)));
        return all.size() > max ? all.subList(0, max) : all;
    }

    private double taskScore(Task t, double px, double py, double pz, int floorY, float yaw) {
        double dx = t.pos.method_10263() + 0.5 - px, dz = t.pos.method_10260() + 0.5 - pz, dy = t.pos.method_10264() - py;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        double layer = Math.max(0, t.pos.method_10264() - floorY);
        // wrong blocks first in the area, layers bottom-up, nearby first; things above head are fine within reach
        // and what is roughly in front of us before what needs a big head turn (faster and smoother)
        float to = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        double turn = horiz < 1.0 ? 0.0 : Math.abs(class_3532.method_15393(to - yaw)) / 60.0;
        return (t.place ? 0 : -3) + layer * 2.5 + horiz + Math.abs(dy) * 0.5 + turn;
    }

    /**
     * The build plan on top of "bottom-up, nearby first":
     * <ul>
     * <li>inside out: the middle of a layer comes before its edge, so the bot ends every layer on the outside and
     * doesn't wall itself in;</li>
     * <li>things that hang on other blocks (torches, buttons, levers, rails...) after the solid blocks of their layer,
     * so their wall / floor is there and they don't get in the way;</li>
     * <li>blocks that others need as support first.</li>
     * </ul>
     */
    private double planScore(class_638 w, Task t) {
        if (!t.place || this.box == null || w == null) return 0;
        class_2338 p = t.pos;
        int edge = Math.min(Math.min(p.method_10263() - this.box[0], this.box[3] - p.method_10263()), Math.min(p.method_10260() - this.box[2], this.box[5] - p.method_10260()));
        double s = -Math.min(edge, 8) * 0.7;
        if (isWater(t.want) || t.kind == SbFix.WATERLOG) return 30.0; // water flows: only when everything around it stands
        if (t.interact()) return s - 1.0; // the block is there already: just a click
        if (t.want.method_26220(w, p).method_1110()) s += 3.0;
        if (this.redstoneLast.getValue()) {
            // redstone after the blocks around it (an observer placed early fires at every block placed next to it,
            // a piston beside a power source pushes) - but not when something above still needs it as support
            double late = SbFix.redstoneLate(t.want);
            if (late > 0 && !this.tasks.containsKey(p.method_10084().method_10063())) s += late;
        }
        // something pending sits on / hangs on this one: do this first
        for (class_2350 d : class_2350.values()) {
            Task o = this.tasks.get(p.method_10093(d).method_10063());
            if (o != null && o.place && o.want.method_26220(w, o.pos).method_1110()) { s -= 1.0; break; }
        }
        return s;
    }

    /**
     * Where to stand: around the task, the free spot from which the most pending blocks of this layer (and the one
     * above) are in reach, not standing where the build still needs a block. Then walk there.
     */
    private boolean walkToWorkSpot(class_638 w, class_746 pl, Task t) {
        if (!(itemFor(t.want) instanceof class_1747 bi)) return false;
        long nowMs = System.currentTimeMillis();
        this.badSpots.values().removeIf(x -> x < nowMs);
        // the last spot we walked to gave nothing: don't pick it again for a minute
        if (this.lastSpot != Long.MIN_VALUE && this.placedTotal == this.lastSpotPlaced) this.badSpots.put(this.lastSpot, nowMs + 60_000L);
        this.lastSpot = Long.MIN_VALUE;
        double reach = this.reach() - 0.3;
        class_1937 sw = SbLitematica.world();
        List<class_2338> near = new ArrayList<>();
        for (Task o : this.tasks.values()) {
            if (!o.place || this.failedUntil.containsKey(o.pos.method_10063())) continue;
            class_2338 q = o.pos;
            if (Math.abs(q.method_10263() - t.pos.method_10263()) > 9 || Math.abs(q.method_10260() - t.pos.method_10260()) > 9) continue;
            if (q.method_10264() < t.pos.method_10264() - 1 || q.method_10264() > t.pos.method_10264() + 1) continue;
            near.add(q);
        }
        class_2338 me = feet(pl);
        List<Object[]> spots = new ArrayList<>();
        class_243 c = class_243.method_24953(t.pos);
        for (int dx = -5; dx <= 5; dx++) for (int dz = -5; dz <= 5; dz++) for (int dy = -3; dy <= 2; dy++) {
            class_2338 p = t.pos.method_10069(dx, dy, dz);
            if (p.equals(t.pos) || p.method_10084().equals(t.pos)) continue;
            double ex = p.method_10263() + 0.5 - c.field_1352, ey = p.method_10264() + 1.27 - c.field_1351, ez = p.method_10260() + 0.5 - c.field_1350;
            double d2 = ex * ex + ey * ey + ez * ez;
            if (d2 > reach * reach || d2 < 0.3) continue;
            if (this.badSpots.containsKey(p.method_10063())) continue;
            // up in the air: fine if we can pillar up to it from a floor below (high walls, towers, roofs)
            int lift = SbWorld.standable(w, p) ? 0 : this.pillarLift(w, sw, p);
            if (lift < 0) continue;
            double s = -lift * 0.9;
            for (class_2338 q : near) {
                if (q.equals(p) || q.equals(p.method_10084())) { s -= 4; continue; } // we'd stand in the way
                double qx = q.method_10263() + 0.5 - (p.method_10263() + 0.5), qy = q.method_10264() + 0.5 - (p.method_10264() + 1.27), qz = q.method_10260() + 0.5 - (p.method_10260() + 0.5);
                if (qx * qx + qy * qy + qz * qz <= reach * reach) s += 1;
            }
            // standing on a spot the build still needs: that block can't be placed while we are there
            if (sw != null && this.inBox(p)) {
                class_2680 a = SbLitematica.target(sw, p), b = SbLitematica.target(sw, p.method_10084());
                if (!a.method_26215() && !SbWorld.matches(a, w.method_8320(p)) || !b.method_26215() && !SbWorld.matches(b, w.method_8320(p.method_10084()))) s -= 4;
            }
            s -= Math.sqrt(p.method_10262(me)) * 0.15;
            spots.add(new Object[]{p, s});
        }
        spots.sort((a, b) -> Double.compare((double) b[1], (double) a[1]));
        int sims = 0;
        for (Object[] o : spots) {
            if (sims++ >= 8) break;
            class_2338 p = (class_2338) o[0];
            class_243 eye = new class_243(p.method_10263() + 0.5, p.method_10264() + 1.27, p.method_10260() + 0.5);
            if (this.solution(t, bi, eye, reach) == null) continue;
            if (p.equals(me)) return false; // already here: the normal walk centres us
            long pk = p.method_10063();
            if (this.walk(w, pl, q -> q.method_10063() == pk, p, () -> {}, 9000)) {
                this.workSpots++;
                this.lastSpot = pk;
                this.lastSpotPlaced = this.placedTotal;
                dbg("work spot " + p.method_23854() + " covers " + String.format(java.util.Locale.ROOT, "%.1f", (double) o[1]) + " for " + t.pos.method_23854());
                return true;
            }
        }
        return false;
    }

    /**
     * How many blocks we would pillar up from a floor straight below to stand at {@code p}, or -1 when that isn't
     * possible (no scaffold, something in the way, the column is part of the build, more than 6 high).
     */
    private int pillarLift(class_638 w, class_1937 sw, class_2338 p) {
        if (!this.useScaffold.getValue() || this.scaffoldItem() == null) return -1;
        if (!SbWorld.passable(w, p) || !SbWorld.passable(w, p.method_10084())) return -1;
        int have = this.count(this.scaffoldItem());
        for (int k = 1; k <= 6 && k <= have; k++) {
            class_2338 q = p.method_10087(k);
            if (!SbWorld.passable(w, q)) return -1;
            if (sw != null && this.inBox(q) && !SbLitematica.target(sw, q).method_26215() && SbLitematica.target(sw, q).method_26204() != this.scaffoldItem().method_7711()
                    && this.pillarItem(sw, q) == null) return -1;
            if (this.besideCactus(sw, q)) return -1;
            if (SbWorld.standable(w, q)) return k;
        }
        return -1;
    }

    /** Once everything is scanned: tell the plan and what material is missing before starting. */
    private void announcePlan(class_638 w, class_746 pl) {
        if (this.planAnnounced || this.box == null) return;
        class_1937 sw = SbLitematica.world();
        if (sw == null) return;
        for (int cx = this.box[0] >> 4; cx <= this.box[3] >> 4; cx++) for (int cz = this.box[2] >> 4; cz <= this.box[5] >> 4; cz++) {
            if (!w.method_2935().method_12123(cx, cz) || !SbLitematica.loaded(sw, cx, cz)) continue;
            if (!this.chunkScannedAt.containsKey(BaseFinder.key(cx, cz))) return;
        }
        this.planAnnounced = true;
        Map<class_1792, Integer> need = new LinkedHashMap<>();
        Set<Integer> layers = new HashSet<>();
        int place = 0, fix = 0;
        for (Task t : this.tasks.values()) {
            if (!t.place || t.interact()) { fix++; continue; }
            place++;
            layers.add(t.pos.method_10264());
            need.merge(itemFor(t.want), SbFix.firstSteps(t.want).length > 1 ? 2 : 1, Integer::sum);
        }
        if (place == 0 && fix == 0) return;
        Map<class_1792, Integer> stored = new HashMap<>();
        for (Map<class_1792, Integer> m : this.storageSeen.values()) for (var e : m.entrySet()) stored.merge(e.getKey(), e.getValue(), Integer::sum);
        StringBuilder miss = new StringBuilder();
        int missKinds = 0;
        for (var e : need.entrySet()) {
            int have = this.count(e.getKey()) + stored.getOrDefault(e.getKey(), 0);
            if (have >= e.getValue()) continue;
            if (missKinds++ < 4) miss.append(miss.length() == 0 ? "" : ", ").append(e.getValue() - have).append("x ").append(e.getKey().method_63680().getString());
        }
        if (missKinds > 4) miss.append(" +").append(missKinds - 4).append(" more");
        this.say("Plan: §e" + place + "§f blocks in §e" + layers.size() + "§f layers" + (fix > 0 ? ", §e" + fix + "§f to fix" : "")
                + " - bottom up, inside out, decorations last, from the spots that reach the most blocks.");
        if (missKinds > 0) this.say("§eNot in inventory" + (this.storage.isEmpty() ? "" : " or known storage") + ": §f" + miss);
        if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB plan place=" + place + " fix=" + fix + " layers=" + layers.size() + " missing=" + miss);
    }

    private int openSides(class_2338 n, class_2338 except) {
        class_638 w = this.mc.field_1687;
        int open = 0;
        for (class_2350 d : class_2350.values()) {
            class_2338 m = n.method_10093(d);
            if (m.equals(except)) continue;
            class_2680 s = w.method_8320(m);
            if (s.method_26215() || s.method_45474()) open++;
        }
        return open;
    }

    private boolean sealsPending(class_2338 p) {
        for (class_2350 d : class_2350.values()) {
            Task o = this.tasks.get(p.method_10093(d).method_10063());
            // also when it failed for now (it stood in the hole...): walled in, it could never be placed later
            if (o != null && o.place && this.failCount.getOrDefault(o.pos.method_10063(), 0) < 6 && this.openSides(o.pos, p) == 0) return true;
        }
        return false;
    }

    /** A block that has to be placed is walled in on all sides: open it up (that block gets placed again later). */
    private boolean tryUnseal(class_638 w, class_746 pl) {
        class_2338 p = this.unseal;
        if (p == null) return false;
        if (this.unsealTries++ > 3) { this.unseal = null; return false; }
        class_2680 st = w.method_8320(p);
        if (st.method_26215() || st.method_45474() || !SbWorld.breakable(w, p) || SbWorld.container(st)) { this.unseal = null; return false; }
        class_1937 sw = SbLitematica.world();
        class_2680 want = sw == null ? st : SbLitematica.target(sw, p);
        Task t = new Task(p, want, !want.method_26215());
        if (pl.method_33571().method_1025(class_243.method_24953(p)) <= this.reach() * this.reach() && this.visibleFace(w, pl, p) != null) {
            dbg("unseal: breaking " + p.method_23854());
            this.unseal = null;
            this.startBreak(p, t);
            return true;
        }
        return this.walkNear(w, pl, p, null);
    }

    private class_2338 unseal;
    private int unsealTries;

    private boolean waterTooEarly(Task w) {
        for (Task o : this.tasks.values()) {
            // sugar cane, kelp, lily pads wait for this water themselves
            if (!o.place || isWater(o.want) || SbFix.needsWater(o.want)) continue;
            if (Math.abs(o.pos.method_10263() - w.pos.method_10263()) <= 4 && Math.abs(o.pos.method_10260() - w.pos.method_10260()) <= 4
                    && o.pos.method_10264() <= w.pos.method_10264() + 1 && o.pos.method_10264() >= w.pos.method_10264() - 3) return true;
        }
        return false;
    }

    static boolean isWater(class_2680 s) {
        return s.method_26204() == class_2246.field_10382;
    }

    /** The item that builds this state (a water bucket for water). */
    static class_1792 itemFor(class_2680 s) {
        return isWater(s) ? class_1802.field_8705 : SbFix.stem(s.method_26204()).method_8389();
    }

    /**
     * Where to click with a water bucket so the water lands at {@code target}: a visible face of a solid neighbour
     * that doesn't take the water itself (waterloggable) and doesn't open a menu.
     */
    private class_3965 waterClick(class_638 w, class_746 pl, class_2338 target, class_243 eye) {
        double reach = this.reach();
        double cx = target.method_10263() + 0.5, cy = target.method_10264() + 0.5, cz = target.method_10260() + 0.5;
        class_3965 best = null;
        double bestD = Double.MAX_VALUE;
        for (class_2350 dir : class_2350.values()) {
            class_2338 nb = target.method_10093(dir);
            class_2680 ns = w.method_8320(nb);
            if (ns.method_26215() || ns.method_45474() || ns.method_26218(w, nb).method_1110()) continue;
            if (ns.toString().contains("waterlogged=") || SbWorld.usable(w, nb)) continue;
            class_2350 side = dir.method_10153();
            double fx = cx + dir.method_10148() * 0.5, fy = cy + dir.method_10164() * 0.5, fz = cz + dir.method_10165() * 0.5;
            double[] g = {0.0, -0.22, 0.22};
            for (double u : g) for (double v : g) {
                double ox, oy, oz;
                if (dir.method_10164() != 0) { ox = u; oy = 0; oz = v; }
                else if (dir.method_10148() != 0) { ox = 0; oy = v; oz = u; }
                else { ox = u; oy = v; oz = 0; }
                class_243 point = new class_243(fx + ox, fy + oy, fz + oz);
                if ((eye.field_1352 - point.field_1352) * side.method_10148() + (eye.field_1351 - point.field_1351) * side.method_10164() + (eye.field_1350 - point.field_1350) * side.method_10165() <= 0.02) continue;
                double d = eye.method_1025(point);
                if (d > reach * reach || d >= bestD) continue;
                class_3965 r = SbPlacer.ray(w, pl, eye, point);
                if (r == null || !r.method_17777().equals(nb) || r.method_17780() != side) continue;
                best = r;
                bestD = d;
            }
        }
        return best;
    }

    /** Places a water source like a player: look at the face next to it and right-click with the bucket. */
    private void tickWater(class_638 w, class_746 pl, Task t) {
        class_1792 b = class_1802.field_8705;
        if (!this.hold(pl, b)) {
            if (this.count(b) == 0) { this.missing(b); this.toIdle(); }
            return;
        }
        this.keys(false, false, false, true); // sneaking: the clicked block is never used
        if (!pl.method_5715() && this.modeTicks < 6) return;
        class_3965 h = this.waterClick(w, pl, t.pos, pl.method_33571());
        if (h == null) {
            if (this.modeTicks > 3) { this.fail(t.pos, 8_000L, "nowater"); this.toIdle(); }
            return;
        }
        float[] rot = SbPlacer.lookAt(pl.method_33571(), h.method_17784());
        if (!this.rotateTo(pl, rot[0], rot[1], 0.4f) || !settled(pl) || this.modeTicks < this.placeDelay()) return;
        class_3965 hit = this.crosshair(pl);
        if (hit == null || !hit.method_17777().equals(h.method_17777()) || hit.method_17780() != h.method_17780()) {
            this.rotateTo(pl, rot[0], rot[1], 0.2f);
            if (++this.aimMiss > 20) { this.aimMiss = 0; this.fail(t.pos, 8_000L, "wateraim"); this.toIdle(); }
            return;
        }
        // like vanilla: the click on the block first (does nothing while sneaking), then the bucket itself
        if (this.dupRotRisk(pl)) return;
        var res = this.mc.field_1761.method_2896(pl, class_1268.field_5808, hit);
        if (!res.method_23665()) res = this.mc.field_1761.method_2919(pl, class_1268.field_5808);
        this.clickedBlock(); // Grim counts every block click as a place
        pl.method_6104(class_1268.field_5808);
        this.lastClickTick = this.tickNo;
        this.aimMiss = 0;
        this.placedTotal++;
        this.lastProgressMs = System.currentTimeMillis();
        this.placedByMe.add(t.pos.method_10063());
        this.touched(t.pos);
        dbg("water at " + t.pos.method_23854() + " " + res);
        this.waitTicks = 4;
        this.modeTicks = 0;
    }

    // ------------------------------------------------------------------ survival: eating, tools, mending

    private static final Set<String> BAD_FOOD = Set.of("rotten_flesh", "spider_eye", "poisonous_potato", "pufferfish", "chorus_fruit", "suspicious_stew");

    /** Best food in the inventory (most nutrition, nothing that poisons or teleports). */
    private class_1792 foodItem(class_746 pl) {
        class_1792 best = null;
        int bestN = 0;
        for (int i = 0; i < 36; i++) {
            class_1799 st = pl.method_31548().method_5438(i);
            if (st.method_7960()) continue;
            net.minecraft.class_4174 f = st.method_58694(net.minecraft.class_9334.field_50075);
            if (f == null || BAD_FOOD.contains(net.minecraft.class_7923.field_41178.method_10221(st.method_7909()).method_12832())) continue;
            int n = f.comp_2491() + (i < 9 ? 1 : 0);
            if (n > bestN) { bestN = n; best = st.method_7909(); }
        }
        return best;
    }

    private boolean tickEat(class_746 pl) {
        int food = pl.method_7344().method_7586();
        if (!this.autoEat.getValue()) { if (this.eating) this.stopEat(); return false; }
        if (!this.eating) {
            boolean hungry = food <= this.eatBelow.getIntValue() || pl.method_6032() < 14.0f && food < 18;
            if (!hungry || food >= 20) return false;
            if (this.mode == Mode.OPEN_CHEST || this.mode == Mode.TRANSFER || this.mode == Mode.ESCAPE || this.mode == Mode.WALK && !pl.method_24828()) return false;
            if (this.foodItem(pl) == null) return false;
            this.toIdle();
            this.eating = true;
            this.eatTicks = 0;
            dbg("eat: hunger " + food);
            if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB eat start food=" + food);
        }
        boolean using = pl.method_6115();
        class_1792 f = this.foodItem(pl);
        if (!using && (f == null || food >= Math.min(20, this.eatBelow.getIntValue() + 6) || this.eatTicks > 600)) {
            this.stopEat();
            if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB eat done food=" + food);
            return false;
        }
        this.eatTicks++;
        this.status = "Eating";
        this.keys(false, false, false, false);
        if (using) { this.mc.field_1690.field_1904.method_23481(true); return true; }
        if (!this.hold(pl, f)) return true;
        // eat looking up a bit, not at a block: a right-click on a block would be sent first (like holding the key)
        if (this.crosshair(pl) != null && this.eatTicks < 40) {
            this.mc.field_1690.field_1904.method_23481(false);
            this.rotateTo(pl, pl.method_36454(), -45.0f, 4.0f);
            return true;
        }
        this.mc.field_1690.field_1904.method_23481(true);
        if (this.eatTicks % 8 == 2 && this.mc.field_1761.method_2919(pl, class_1268.field_5808).method_23665()) this.lastClickTick = this.tickNo;
        return true;
    }

    private void stopEat() {
        this.eating = false;
        try { this.mc.field_1690.field_1904.method_23481(false); } catch (Throwable ignored) { }
        this.lastProgressMs = System.currentTimeMillis();
    }

    private boolean nearlyBroken(class_1799 s) {
        return this.saveTools.getValue() && !s.method_7960() && s.method_7963() && s.method_7936() - s.method_7919() <= 6;
    }

    private static boolean mendable(class_1799 s) {
        return !s.method_7960() && s.method_7963() && s.method_7919() > 0 && s.method_58657().toString().contains("mending");
    }

    /** XP orbs around: a damaged Mending item from the bag goes to the (empty or repaired) offhand. */
    private boolean tryMend(class_638 w, class_746 pl) {
        if (!this.autoMend.getValue() || this.tickNo - this.lastMendCheck < 20) return false;
        this.lastMendCheck = (int) this.tickNo;
        if (w.method_8390(net.minecraft.class_1303.class, pl.method_5829().method_1014(8.0), e -> true).isEmpty()) return false;
        class_1799 off = pl.method_6079();
        if (mendable(off)) return false;
        if (!off.method_7960() && !(off.method_7963() && off.method_7919() == 0 && off.method_58657().toString().contains("mending"))) return false;
        if (pl.field_7512 != pl.field_7498) return false;
        class_1661 inv = pl.method_31548();
        for (int i = 9; i < 36; i++) {
            if (!mendable(inv.method_5438(i))) continue;
            if (!this.readyForInventory(pl)) return true;
            this.mc.field_1761.method_2906(pl.field_7498.field_7763, i, 40, class_1713.field_7791, (class_1657) pl);
            this.mc.method_1562().method_52787(new net.minecraft.class_2815(pl.field_7498.field_7763));
            dbg("mend: " + inv.method_5438(i) + " <-> offhand");
            this.waitTicks = 3;
            return true;
        }
        return false;
    }

    /**
     * Nothing placed or broken for half a minute although there is work: something is wrong with the current idea
     * (a spot that never works, built in, a desynced world). Drop it and make a new plan from scratch.
     */
    private void watchdog(class_638 w, class_746 pl) {
        long now = System.currentTimeMillis();
        boolean waiting = this.tasks.isEmpty() || this.eating || this.mode == Mode.OPEN_CHEST || this.mode == Mode.TRANSFER
                || this.status.startsWith("Missing") || this.status.startsWith("Restock") || this.status.startsWith("Paused") || this.status.startsWith("Finished")
                || this.status.startsWith("Walking to unbuilt") || this.status.startsWith("Removing");
        if (this.lastProgressMs == 0 || waiting) { this.lastProgressMs = now; return; }
        if (now - this.lastProgressMs < 30_000L) return;
        this.lastProgressMs = now;
        this.replans++;
        class_2338 f = feet(pl);
        dbg("watchdog: no progress for 30s at " + f.method_23854() + " (" + this.status + ") - new plan");
        if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB replan " + this.replans + " at " + f.method_23854() + " " + this.status);
        if (this.task != null) this.fail(this.task.pos, 20_000L, "watchdog");
        if (this.lastSpot != Long.MIN_VALUE) this.badSpots.put(this.lastSpot, now + 90_000L);
        this.badSpots.put(f.method_10063(), now + 90_000L);
        this.lastSpot = Long.MIN_VALUE;
        // look at everything again (the world may not be what we think) and allow digging out
        this.chunkScannedAt.clear();
        this.escape = true;
        this.toIdle();
        this.say("§eNo progress for a while - making a new plan.");
    }

    /** After the bot closed a screen it opened, bring back what the player had open (pause menu, chat...). */
    private void restoreScreen() {
        net.minecraft.class_437 s = this.bgScreen;
        dbg("restore screen " + (s == null ? "-" : s.getClass().getSimpleName()) + " now " + (this.mc.field_1755 == null ? "-" : this.mc.field_1755.getClass().getSimpleName()));
        if (s != null && this.mc.field_1755 == null) this.mc.method_1507(s);
    }

    private void nothingToDo(class_638 w, class_746 pl) {
        // parts of the schematic that were never loaded: go there
        class_1937 sw = SbLitematica.world();
        int pcx = (int) Math.floor(pl.method_23317()) >> 4, pcz = (int) Math.floor(pl.method_23321()) >> 4;
        int bx = Integer.MIN_VALUE, bz = 0;
        double bd = Double.MAX_VALUE;
        for (int cx = this.box[0] >> 4; cx <= this.box[3] >> 4; cx++) for (int cz = this.box[2] >> 4; cz <= this.box[5] >> 4; cz++) {
            long k = BaseFinder.key(cx, cz);
            if (this.chunkScannedAt.containsKey(k)) continue;
            double d = Math.hypot(cx - pcx, cz - pcz);
            if (d < bd) { bd = d; bx = cx; bz = cz; }
        }
        if (bx != Integer.MIN_VALUE) {
            this.status = "Walking to unbuilt part";
            class_2338 target = new class_2338((bx << 4) + 8, (int) pl.method_23318(), (bz << 4) + 8);
            final int tx = bx, tz = bz;
            if (this.walk(w, pl, p -> Math.abs((p.method_10263() >> 4) - tx) <= 1 && Math.abs((p.method_10260() >> 4) - tz) <= 1, target, null, 30000)) return;
        }
        // remove our scaffolding (only once nothing else is left to build)
        if (this.cleanup.getValue() && this.tasks.isEmpty()) {
            this.scaffoldPlaced.removeIf(l -> w.method_8320(class_2338.method_10092(l)).method_26204() != this.scaffoldItem().method_7711());
            // top down: the lower blocks are the way up to the higher ones. Nearest-first took the pillar away
            // under a high block, which then needed a new pillar, which was taken away again...
            // The highest one left goes first (one in reach if there are several that high).
            class_2338 best = null, bestNear = null;
            double d2 = Double.MAX_VALUE, dn = Double.MAX_VALUE, r2 = this.reach() * this.reach();
            for (Long l : this.scaffoldPlaced) {
                if (this.failedUntil.containsKey(l)) continue;
                class_2338 p = class_2338.method_10092(l);
                if (this.inBox(p) && !SbLitematica.target(sw, p).method_26215()) continue; // part of the build now
                double d = pl.method_33571().method_1025(class_243.method_24953(p));
                if (d <= r2 && (bestNear == null || p.method_10264() > bestNear.method_10264() || p.method_10264() == bestNear.method_10264() && d < dn)
                        && this.visibleFace(w, pl, p) != null) { dn = d; bestNear = p; }
                if (best == null || p.method_10264() > best.method_10264() || p.method_10264() == best.method_10264() && d < d2) { d2 = d; best = p; }
            }
            // a lower one in reach only once nothing higher is left (it may be the way up there)
            if (bestNear != null && bestNear.method_10264() >= best.method_10264()) { best = bestNear; d2 = dn; }
            if (best == null) {
                // some scaffolding is left that failed a moment ago: not finished yet, try again from elsewhere
                int left = 0;
                class_2338 any = null;
                for (Long l : this.scaffoldPlaced) {
                    class_2338 p = class_2338.method_10092(l);
                    if (this.inBox(p) && !SbLitematica.target(sw, p).method_26215()) continue;
                    if (this.failCount.getOrDefault(l, 0) >= 6) continue; // given up on it
                    left++;
                    any = p;
                }
                if (left > 0) {
                    this.status = "Removing scaffolding";
                    this.waitTicks = 20;
                    if (++this.scaffoldWaits % 5 == 0 && this.newViewpoint(w, pl)) return;
                    return;
                }
                if (!this.scaffoldPlaced.isEmpty() && !this.scaffoldWarned) {
                    this.scaffoldWarned = true;
                    StringBuilder at = new StringBuilder();
                    int n = 0;
                    for (Long l : this.scaffoldPlaced) {
                        class_2338 p = class_2338.method_10092(l);
                        if (this.inBox(p) && !SbLitematica.target(sw, p).method_26215()) continue;
                        if (n++ < 3) at.append(at.length() == 0 ? "" : ", ").append(p.method_10263()).append(' ').append(p.method_10264()).append(' ').append(p.method_10260());
                    }
                    if (n > 0) this.say("§e" + n + " scaffold block(s) could not be removed: " + at + (n > 3 ? "..." : ""));
                }
            }
            if (best != null) {
                this.status = "Removing scaffolding";
                Task t = new Task(best, net.minecraft.class_2246.field_10124.method_9564(), false);
                if (d2 <= this.reach() * this.reach() && this.visibleFace(w, pl, best) != null) { this.startBreak(best, t); return; }
                if (this.walkNear(w, pl, best, t)) return;
                this.fail(best, 30_000L, "scafwalk");
                return;
            }
        }
        if (this.tasks.isEmpty() && !this.chunkStats.isEmpty()) {
            // blocks it had to skip: say so instead of a plain "finished"
            int skippedLeft = 0;
            for (Long l : this.skipped) {
                class_2338 p = class_2338.method_10092(l);
                if (!SbWorld.matches(SbLitematica.target(sw, p), w.method_8320(p))) skippedLeft++;
            }
            this.status = skippedLeft > 0 ? "Finished (" + skippedLeft + " skipped)" : "Finished";
            if (!this.announcedDone) {
                this.announcedDone = true;
                this.say(skippedLeft > 0 ? "§e" + this.schematicName + " is finished except " + skippedLeft + " skipped block(s) - place them yourself."
                        : "§a" + this.schematicName + " is finished!");
            }
        } else if (this.tasks.isEmpty()) {
            this.status = "Scanning";
        } else {
            // everything left is on cool-down: retry the one that is closest instead of standing around
            this.status = "Retrying";
            Task best = null;
            double rd = Double.MAX_VALUE;
            for (Task t : this.tasks.values()) {
                if (t.place && this.count(itemFor(t.want)) == 0) continue;
                double d = pl.method_33571().method_1025(class_243.method_24953(t.pos));
                if (d < rd) { rd = d; best = t; }
            }
            if (best != null) {
                // everything is on cool-down: give all of them another go (the world around them changed since)
                for (Task t : this.tasks.values()) this.failedUntil.remove(t.pos.method_10063());
            } else this.status = "Waiting (blocks not placeable yet)";
            // still nothing after a while: we may have built ourselves in - allow breaking finished blocks to get
            // out (they get placed again later)
            if (++this.stuckRetries >= 4 && !this.escape) { this.escape = true; dbg("stuck: may break finished blocks to get out"); }
            // the same spot keeps giving nothing (on top of our own scaffolding, in a corner...): look at it from
            // somewhere else - a new spot a few blocks away, then plan again from there
            if (this.stuckRetries % 6 == 3 && best != null && this.newViewpoint(w, pl)) return;
            if (this.stuckRetries >= 40) {
                // nothing worked for a minute: what is left can't be reached or placed (it hangs on skipped
                // blocks...). Give it up so the build finishes instead of standing here forever.
                // blocks we simply have no material for are not impossible: they stay and wait for a restock
                int n = 0, waiting = 0;
                class_1792 lacking = null;
                for (Task t : new ArrayList<>(this.tasks.values())) {
                    class_1792 need = t.place ? this.requiredItem(t) : null;
                    if (need != null && this.count(need) == 0) { waiting++; lacking = need; continue; }
                    this.skipped.add(t.pos.method_10063());
                    this.tasks.remove(t.pos.method_10063());
                    this.touched(t.pos);
                    n++;
                }
                this.stuckRetries = 0;
                this.escape = false;
                if (n > 0) this.say("§eGave up on " + n + " block(s) that can't be reached or placed from anywhere - place them yourself.");
                if (waiting > 0 && lacking != null) {
                    this.missingUntil.remove(lacking);
                    this.missing(lacking);
                }
                if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB gave up on " + n + " (" + waiting + " wait for material)");
            }
            this.waitTicks = 30;
        }
    }

    /** Walks to a random free spot a few blocks away (not one that already failed) to see the work from there. */
    private boolean newViewpoint(class_638 w, class_746 pl) {
        class_2338 f = feet(pl);
        this.badSpots.put(f.method_10063(), System.currentTimeMillis() + 60_000L);
        List<class_2338> spots = new ArrayList<>();
        for (int dx = -6; dx <= 6; dx++) for (int dz = -6; dz <= 6; dz++) for (int dy = -6; dy <= 2; dy++) {
            if (dx * dx + dz * dz < 5) continue;
            class_2338 p = f.method_10069(dx, dy, dz);
            if (this.badSpots.containsKey(p.method_10063()) || !SbWorld.standable(w, p)) continue;
            spots.add(p);
        }
        java.util.Collections.shuffle(spots, this.rnd);
        // lower spots first: off the scaffolding, back on the ground
        spots.sort((a, b) -> Integer.compare(a.method_10264(), b.method_10264()));
        int tries = 0;
        for (class_2338 p : spots) {
            if (tries++ >= 6) break;
            long pk = p.method_10063();
            if (this.walk(w, pl, q -> q.method_10063() == pk, p, () -> {}, 6000)) {
                this.status = "Walking to another spot";
                dbg("new viewpoint " + p.method_23854() + " from " + f.method_23854());
                return true;
            }
        }
        return false;
    }

    private static void dbg(String m) {
        if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB dbg " + m);
    }

    private String pfeet() {
        return this.mc.field_1724 == null ? "?" : this.mc.field_1724.method_24515().method_23854();
    }

    private boolean inBox(class_2338 p) {
        return p.method_10263() >= this.box[0] && p.method_10263() <= this.box[3] && p.method_10264() >= this.box[1] && p.method_10264() <= this.box[4]
                && p.method_10260() >= this.box[2] && p.method_10260() <= this.box[5];
    }

    private void fail(class_2338 p, long ms, String why) {
        if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB fail " + p.method_23854() + " " + why + " from " + pfeet());
        long k = p.method_10063();
        int n = this.failCount.merge(k, 1, Integer::sum);
        this.failedUntil.put(k, System.currentTimeMillis() + ms * Math.min(4, n));
        Task t = this.tasks.get(k);
        // walled in on every side: open the side above (or any breakable one) so it can be placed
        if (t != null && t.place && n >= 2 && this.unseal == null && this.openSides(p, null) == 0) {
            for (class_2350 d : new class_2350[]{class_2350.field_11036, class_2350.field_11043, class_2350.field_11035, class_2350.field_11039, class_2350.field_11034}) {
                class_2338 q = p.method_10093(d);
                class_2680 qs = this.mc.field_1687.method_8320(q);
                if (SbWorld.breakable(this.mc.field_1687, q) && !SbWorld.container(qs)) { this.unseal = q; this.unsealTries = 0; dbg("sealed " + p.method_23854() + " -> open " + q.method_23854()); break; }
            }
        }
        if (t == null || !t.place || this.helperPos.contains(k)) return;
        boolean vertical = false;
        for (var prop : t.want.method_28501()) {
            Object v = t.want.method_11654(prop);
            if (v == class_2350.field_11036 || v == class_2350.field_11033) vertical = true;
        }
        // no spot anywhere gives that facing with a real look (observer facing up on the ground...): give up sooner
        int limit = vertical && this.antiCheat.getValue() && "nowalk".equals(why) ? 3 : 8;
        if (n >= limit) {
            // given up: don't let one impossible block hold up the whole build
            this.skipped.add(k);
            this.tasks.remove(k);
            this.touched(p);
            String name = t.want.method_26204().method_9518().getString();
            this.say("§eSkipped " + name + " at " + p.method_10263() + " " + p.method_10264() + " " + p.method_10260()
                    + (vertical && this.antiCheat.getValue() ? " - it can only be placed with a fake head rotation; turn off Anticheat Safe for it or place it yourself."
                    : " - could not place it after many tries, place it yourself."));
            if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB skipped " + p.method_23854() + " " + name);
        }
    }

    private void missing(class_1792 item) {
        if (this.missingUntil.containsKey(item)) return;
        this.missingUntil.put(item, System.currentTimeMillis() + 45_000L);
        this.status = "Missing " + item.method_63680().getString();
        this.say("§eMissing " + item.method_63680().getString() + (this.storage.isEmpty() ? " - mark a storage chest with " + hook.utils.KeyUtils.getKey(this.markKey.getValue()) : " - not in your inventory or storage"));
    }

    // ================================================================== placing

    private void tickPlace(class_638 w, class_746 pl) {
        Task t = this.task;
        if (t == null) { this.toIdle(); return; }
        class_2680 have = w.method_8320(t.pos);
        if (isWater(t.want) ? isWater(have) && have.toString().contains("level=0") : SbWorld.matches(t.want, have)) { this.finishedBlock(t); return; }
        if (!have.method_26215() && !have.method_45474()) {
            // the first half is there (a single slab of a double one, a repeater that still needs its delay...):
            // go on with it right away instead of waiting for the next scan
            Task next = !this.finishStates.getValue() || isWater(t.want) ? null : SbFix.grows(t.want, have) ? new Task(t.pos, t.want, true, SbFix.GROW)
                    : SbFix.clicks(t.want, have) > 0 ? new Task(t.pos, t.want, true, SbFix.ADJUST) : null;
            if (next == null || t.kind == SbFix.GROW && next.kind == SbFix.GROW) {
                if (next == null) { this.toIdle(); return; }
            } else {
                this.tasks.put(t.pos.method_10063(), next);
                this.task = next;
                t = next;
                if (next.interact()) { this.startInteract(next); return; }
                this.modeTicks = 0;
                return;
            }
        }
        if (isWater(t.want)) { this.tickWater(w, pl, t); return; }
        class_1792 item = itemFor(t.want);
        if (!this.hold(pl, item)) {
            if (this.count(item) == 0) { this.missing(item); this.toIdle(); }
            return;
        }
        if (t.kind == SbFix.GROW) {
            // another candle / pickle / egg onto the block: only standing up (sneaking places it beside instead)
            this.keys(false, false, false, false);
            if (pl.method_5715()) { this.growReady = this.tickNo + 3; return; }
            if (this.tickNo < this.growReady) return; // the server has to see us standing first (eye height)
            if (SbAcTest.ON) {
                SbAcTest.intendedPos = t.pos.method_10063();
                var sh = have.method_26218(w, t.pos);
                SbAcTest.intendedBox = sh.method_1110() ? new class_238(t.pos) : sh.method_1107().method_996(t.pos);
            }
        } else {
            this.keys(false, false, false, true); // sneak while placing: no opening chests / doors by accident
            if (!pl.method_5715() && this.modeTicks < 6) return;
        }
        SbPlacer.Solution s = this.solution(t, (class_1747) item, pl.method_33571(), this.reach());
        if (s == null) {
            if (this.modeTicks > 3) { this.fail(t.pos, 8_000L, "nosolution"); this.toIdle(); }
            return;
        }
        class_2680 stepWant = this.solWant;
        int r;
        if (s.score() >= 400.0) {
            // a state that needs the head turned away from the clicked spot (Anticheat Safe is off)
            if (!this.rotateTo(pl, s.yaw(), s.pitch(), 0.4f) || this.modeTicks < this.placeDelay()) return;
            if (!pl.method_5715() && SbWorld.usable(w, s.hit().method_17777())) { this.forceSneakTicks = 10; return; }
            if (this.dupRotRisk(pl)) return;
            r = this.mc.field_1761.method_2896(pl, class_1268.field_5808, s.hit()).method_23665() ? 1 : -1;
            this.clickedBlock(); // Grim counts every block click as a place
            this.lastClickTick = this.tickNo;
            if (r == 1) pl.method_6104(class_1268.field_5808);
        } else r = this.aimAndPlace(w, pl, t.pos, t.kind == SbFix.GROW ? null : stepWant, (class_1747) item, s.yaw(), s.pitch(), this.modeTicks >= this.placeDelay());
        if (r == 0) return;
        if (r < 0) {
            if (++this.aimMiss > 6) { this.aimMiss = 0; this.fail(t.pos, 8_000L, "aim"); this.toIdle(); }
            return;
        }
        this.aimMiss = 0;
        {
            this.placedTotal++;
            this.lastProgressMs = System.currentTimeMillis();
            this.placedSincePause++;
            this.stuckRetries = 0;
            this.escape = false;
            this.placedByMe.add(t.pos.method_10063());
            if (this.lightPos.contains(t.pos.method_10063())) this.torchesPlaced++;
            else if (!this.helperPos.contains(t.pos.method_10063()) && t.kind == SbFix.PLACE) { this.verify.put(t.pos.method_10063(), this.tickNo); this.verifyWant.put(t.pos.method_10063(), stepWant); }
            if (this.helperPos.remove(t.pos.method_10063())) this.scaffoldPlaced.add(t.pos.method_10063());
            this.touched(t.pos);
            this.waitTicks = 1;
            this.modeTicks = 0;
        }
    }

    /**
     * Once the server had time to confirm a placed block: is it really the wanted state (facing, half...)? If not it
     * gets broken and placed again (scanChunk sees placedByMe).
     */
    private void checkPlaced(class_638 w) {
        if (this.verify.isEmpty()) return;
        var it = this.verify.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            if (this.tickNo - e.getValue() < 15) continue;
            it.remove();
            class_2338 p = class_2338.method_10092(e.getKey());
            class_2680 want = this.verifyWant.remove(e.getKey());
            class_2680 have = w.method_8320(p);
            if (want == null || SbWorld.matchesShape(want, have)) continue;
            this.wrongPlaced++;
            this.touched(p);
            if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB wrongstate " + p.method_23854() + " have " + have + " want " + want);
        }
    }

    // ================================================================== finishing states, tools, defence

    /** The block state the last {@link #solution} plans to place (a single slab for a double one...). */
    private class_2680 solWant;

    /**
     * How to place this task from {@code eye}: a block that is finished by clicking it again (double slab) starts as
     * a single one, more candles / layers are clicked onto the block that is there.
     */
    private SbPlacer.Solution solution(Task t, class_1747 item, class_243 eye, double reach) {
        if (t.kind == SbFix.GROW) {
            this.solWant = t.want;
            return SbPlacer.findGrow(this.mc, t.pos, t.want, item, eye, reach);
        }
        for (class_2680 step : SbFix.firstSteps(t.want)) {
            SbPlacer.Solution s = SbPlacer.find(this.mc, t.pos, step, item, eye, reach);
            if (s != null) {
                this.solWant = step;
                return s;
            }
        }
        return null;
    }

    /** The item this task uses up or needs in the hand (null = nothing special). */
    private class_1792 requiredItem(Task t) {
        return switch (t.kind) {
            case SbFix.ADJUST -> null;
            case SbFix.WATERLOG -> class_1802.field_8705;
            case SbFix.CONVERT -> {
                class_1792 tool = this.toolItem(SbFix.convertTool(t.want));
                yield tool != null ? tool : itemById("iron" + SbFix.convertTool(t.want));
            }
            default -> itemFor(t.want);
        };
    }

    private static final Map<String, class_1792> ITEMS = new HashMap<>();

    static class_1792 itemById(String id) {
        synchronized (ITEMS) {
            if (ITEMS.isEmpty()) for (class_1792 it : net.minecraft.class_7923.field_41178) ITEMS.put(net.minecraft.class_7923.field_41178.method_10221(it).method_12832(), it);
            return ITEMS.getOrDefault(id, class_1802.field_8162);
        }
    }

    private static String itemId(class_1792 it) {
        try {
            return net.minecraft.class_7923.field_41178.method_10221(it).method_12832();
        } catch (Throwable t) {
            return "?";
        }
    }

    /** A tool in the inventory whose id ends with {@code suffix} ("_hoe"...), the best material first, never one about to break. */
    private class_1792 toolItem(String suffix) {
        class_1661 inv = this.mc.field_1724.method_31548();
        class_1792 best = null;
        int bestRank = -1;
        String[] order = {"wooden", "golden", "stone", "copper", "iron", "diamond", "netherite"};
        for (int i = 0; i < 36; i++) {
            class_1799 s = inv.method_5438(i);
            if (s.method_7960() || this.nearlyBroken(s)) continue;
            String id = itemId(s.method_7909());
            if (!id.endsWith(suffix)) continue;
            int rank = 0;
            for (int k = 0; k < order.length; k++) if (id.startsWith(order[k])) rank = k + 1;
            if (rank > bestRank) { bestRank = rank; best = s.method_7909(); }
        }
        return best;
    }

    /** It can stand there now: what it sits on / hangs on is there, and sand / gravel / concrete powder won't fall. */
    private boolean placeableNow(class_638 w, Task t) {
        if (w == null) return true;
        try {
            if (!t.want.method_26184(w, t.pos)) return false;
            if (SbFix.falls(t.want)) {
                class_2680 below = w.method_8320(t.pos.method_10074());
                if (below.method_26215() || below.method_45474() || !below.method_26227().method_15769()) return false;
            }
        } catch (Throwable ignored) {
        }
        return true;
    }

    /** Air above, or only grass / flowers there that can simply be broken. */
    private boolean airAboveOrClearable(class_638 w, class_2338 p) {
        class_2680 up = w.method_8320(p.method_10084());
        return up.method_26215() || up.method_45474() && up.method_26227().method_15769() && SbWorld.breakable(w, p.method_10084());
    }

    /** Where to click for an interact task (null = nothing to click from {@code eye}). */
    private class_3965 interactFace(class_638 w, class_746 pl, Task t, class_243 eye) {
        return this.facePoint(w, pl, t.pos, eye, t.kind != SbFix.ADJUST);
    }

    private long growReady;
    private int interactClicks;
    private long interactReady;

    private void startInteract(Task t) {
        this.task = t;
        this.mode = Mode.INTERACT;
        this.modeTicks = 0;
        this.interactClicks = 0;
        this.status = switch (t.kind) {
            case SbFix.CONVERT -> "Using a tool";
            case SbFix.WATERLOG -> "Waterlogging";
            default -> "Setting " + t.want.method_26204().method_9518().getString();
        };
    }

    /**
     * Right-clicks a block that is already there: a repeater to its delay, a comparator to subtract, a note block to
     * its note, a trapdoor open; a hoe on dirt for farmland, an axe on a log, a water bucket into a slab. Every click
     * with the real crosshair on the block, one at a time, waiting for the server to answer before the next.
     */
    private void tickInteract(class_638 w, class_746 pl) {
        Task t = this.task;
        if (t == null) { this.toIdle(); return; }
        class_2680 have = w.method_8320(t.pos);
        boolean done = t.kind == SbFix.WATERLOG ? SbFix.waterlogged(have) || !SbWorld.matches(t.want, have) : SbWorld.matches(t.want, have);
        if (done) { this.finishedBlock(t); return; }
        if (t.kind == SbFix.ADJUST && SbFix.clicks(t.want, have) <= 0 || t.kind == SbFix.CONVERT && !SbFix.convertsFrom(t.want, have)) {
            this.touched(t.pos);
            this.toIdle();
            return;
        }
        if (this.modeTicks > 300 || this.interactClicks > 30) { this.fail(t.pos, 15_000L, "interact"); this.toIdle(); return; }
        // grass or a flower on top: a hoe / shovel needs air there
        if (t.kind == SbFix.CONVERT && SbFix.needsAirAbove(t.want) && !w.method_8320(t.pos.method_10084()).method_26215()) {
            class_2338 up = t.pos.method_10084();
            if (this.airAboveOrClearable(w, t.pos) && this.visibleFace(w, pl, up) != null) { this.startBreak(up, null); return; }
            this.fail(t.pos, 15_000L, "notair");
            this.toIdle();
            return;
        }
        class_1792 need = this.requiredItem(t);
        if (need != null) {
            if (this.count(need) == 0) { this.missing(need); this.toIdle(); return; }
            if (!this.hold(pl, need)) return;
        }
        // a click without sneaking uses the block (repeater, trapdoor...); tools and buckets work while sneaking too
        boolean sneak = t.kind != SbFix.ADJUST;
        this.keys(false, false, false, sneak);
        if (!sneak && pl.method_5715()) return;
        if (sneak && !pl.method_5715() && this.modeTicks < 6) return;
        if (this.tickNo < this.interactReady) return;
        class_3965 face = this.interactFace(w, pl, t, pl.method_33571());
        if (face == null) { this.fail(t.pos, 8_000L, "interactface"); this.toIdle(); return; }
        float[] rot = SbPlacer.lookAt(pl.method_33571(), face.method_17784());
        if (!this.rotateTo(pl, rot[0], rot[1], 0.6f) || !settled(pl) || this.modeTicks < this.placeDelay()) return;
        class_3965 hit = this.crosshair(pl);
        if (hit == null || !hit.method_17777().equals(t.pos) || t.kind != SbFix.ADJUST && hit.method_17780() == class_2350.field_11033) {
            this.rotateTo(pl, rot[0], rot[1], 0.2f);
            if (++this.aimMiss > 20) { this.aimMiss = 0; this.fail(t.pos, 8_000L, "interactaim"); this.toIdle(); }
            return;
        }
        if (SbAcTest.ON) {
            SbAcTest.intendedPos = t.pos.method_10063();
            var sh = have.method_26218(w, t.pos);
            SbAcTest.intendedBox = sh.method_1110() ? new class_238(t.pos) : sh.method_1107().method_996(t.pos);
        }
        if (this.dupRotRisk(pl)) return;
        var res = this.mc.field_1761.method_2896(pl, class_1268.field_5808, hit);
        if (t.kind == SbFix.WATERLOG && !res.method_23665()) res = this.mc.field_1761.method_2919(pl, class_1268.field_5808);
        this.clickedBlock(); // Grim counts every block click as a place
        pl.method_6104(class_1268.field_5808);
        this.lastClickTick = this.tickNo;
        this.aimMiss = 0;
        this.interactClicks++;
        this.placedTotal++;
        this.lastProgressMs = System.currentTimeMillis();
        this.touched(t.pos);
        dbg("interact " + t.kind + " at " + t.pos.method_23854() + " " + res + " have " + have);
        // the server decides some of these (note blocks, tools): wait for its answer before clicking again
        this.interactReady = this.tickNo + 7 + (this.human.getValue() ? this.rnd.nextInt(4) : 0);
        this.modeTicks = 0;
    }

    private boolean defending;

    /** A hostile mob close by: turn to it and hit it when the attack is charged (like a player would). */
    private boolean tickDefend(class_638 w, class_746 pl) {
        if (!this.defend.getValue() || this.mode == Mode.OPEN_CHEST || this.mode == Mode.TRANSFER || this.mode == Mode.ESCAPE || this.eating) return this.defending = false;
        if (this.mode == Mode.WALK && !pl.method_24828()) return false; // mid-jump / pillar: finish that first
        net.minecraft.class_1297 best = null;
        double bd = 3.2 * 3.2;
        for (net.minecraft.class_1309 e : w.method_8390(net.minecraft.class_1309.class, pl.method_5829().method_1014(4.5), e -> e instanceof net.minecraft.class_1569 && e.method_5805())) {
            String id = BaseFinder.entityId(e);
            // neutral until you hit them: leave them alone
            if (id.equals("enderman") || id.equals("zombified_piglin") || id.equals("piglin") || id.equals("piglin_brute")) continue;
            double d = pl.method_33571().method_1025(e.method_5829().method_1005());
            if (d < bd && pl.method_6057(e)) { bd = d; best = e; }
        }
        if (best == null) {
            if (this.defending) { this.defending = false; this.toIdle(); }
            return false;
        }
        if (!this.defending) {
            this.defending = true;
            this.mc.field_1761.method_2925();
            this.toIdle();
            dbg("defend against " + BaseFinder.entityId(best));
        }
        this.status = "Defending";
        this.keys(false, false, false, false);
        class_1792 weapon = this.toolItem("_sword");
        if (weapon == null) weapon = this.toolItem("_axe");
        if (weapon != null) this.hold(pl, weapon);
        class_243 aim = best.method_5829().method_1005();
        float[] rot = SbPlacer.lookAt(pl.method_33571(), aim);
        this.rotateTo(pl, rot[0], rot[1], 3.0f);
        if (pl.method_7261(0.5f) >= 0.92f && this.mc.field_1765 instanceof net.minecraft.class_3966 eh && eh.method_17782() == best) {
            this.mc.field_1761.method_2918(pl, best);
            pl.method_6104(class_1268.field_5808);
            this.lastProgressMs = System.currentTimeMillis();
        }
        return true;
    }

    /** Chat list: what the rest of the build needs and how much of it is missing (inventory + known storage). */
    private void listMaterials() {
        if (this.mc.field_1724 == null) return;
        if (this.tasks.isEmpty()) { this.say(this.placement == null ? "No schematic loaded." : "Nothing left to place (in the scanned part)."); return; }
        Map<class_1792, Integer> need = new LinkedHashMap<>();
        for (Task t : this.tasks.values()) {
            if (!t.place || t.kind == SbFix.ADJUST) continue;
            class_1792 it = this.requiredItem(t);
            if (it == null || t.kind == SbFix.CONVERT || t.kind == SbFix.WATERLOG) continue;
            need.merge(it, t.kind == SbFix.PLACE && SbFix.firstSteps(t.want).length > 1 ? 2 : 1, Integer::sum);
        }
        Map<class_1792, Integer> stored = new HashMap<>();
        for (Map<class_1792, Integer> m : this.storageSeen.values()) for (var e : m.entrySet()) stored.merge(e.getKey(), e.getValue(), Integer::sum);
        List<Map.Entry<class_1792, Integer>> rows = new ArrayList<>(need.entrySet());
        rows.sort((a, b) -> b.getValue() - a.getValue());
        int missingKinds = 0;
        StringBuilder sb = new StringBuilder();
        for (var e : rows) {
            int have = this.count(e.getKey()) + stored.getOrDefault(e.getKey(), 0);
            int miss = Math.max(0, e.getValue() - have);
            if (miss == 0) continue;
            if (missingKinds++ < 14) {
                String stacks = miss >= 64 ? " (" + (miss / 64) + " stacks" + (miss % 64 > 0 ? " + " + miss % 64 : "") + ")" : "";
                sb.append("\n §7- §f").append(miss).append("x ").append(e.getKey().method_63680().getString()).append(stacks);
            }
        }
        boolean tool = false;
        for (Task t : this.tasks.values()) if (t.kind == SbFix.CONVERT && this.toolItem(SbFix.convertTool(t.want)) == null) tool = true;
        if (missingKinds == 0) this.say("§aYou have everything for the rest of the build" + (this.storage.isEmpty() ? "." : " (inventory + storage)."));
        else this.say("§eStill missing " + missingKinds + " kind(s) of blocks:" + sb + (missingKinds > 14 ? "\n §7+" + (missingKinds - 14) + " more" : ""));
        if (tool) this.say("§eThe build needs a tool you don't carry (hoe for farmland, shovel for paths or axe for stripped logs).");
    }


    // ================================================================== water bucket refill

    private class_2338 refillAt;
    private final Map<Long, Long> badSources = new HashMap<>();
    private long refillCheckAt;

    private static boolean waterSource(class_2680 s) {
        return isWater(s) && s.toString().contains("level=0");
    }

    /**
     * A source we may take: outside the build (a river, a lake, your own pool), or inside it only where it comes back
     * by itself (an infinite source: two sources beside it and something holding it below).
     */
    private boolean takeableSource(class_638 w, class_2338 p) {
        if (!waterSource(w.method_8320(p))) return false;
        boolean inside = this.inBox(p);
        if (!inside) return true;
        int n = 0;
        for (class_2350 d : new class_2350[]{class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039})
            if (waterSource(w.method_8320(p.method_10093(d)))) n++;
        class_2680 below = w.method_8320(p.method_10074());
        return n >= 2 && (waterSource(below) || !below.method_26215() && !below.method_45474());
    }

    private boolean needsWaterBucket() {
        for (Task t : this.tasks.values()) {
            if (!t.place || this.skipped.contains(t.pos.method_10063())) continue;
            if (isWater(t.want) || t.kind == SbFix.WATERLOG) return true;
        }
        return false;
    }

    /** Only empty buckets but water to place: walk to the nearest water we may take and fill one. */
    private boolean tryRefill(class_638 w, class_746 pl) {
        if (!this.placeWater.getValue()) return false;
        class_1792 empty = itemById("bucket");
        if (this.count(class_1802.field_8705) > 0 || this.count(empty) == 0) return false;
        long now = System.currentTimeMillis();
        if (now < this.refillCheckAt || !this.needsWaterBucket()) return false;
        this.badSources.values().removeIf(t -> t < now);
        class_2338 f = feet(pl);
        class_2338 best = null;
        double bd = Double.MAX_VALUE;
        for (int dx = -24; dx <= 24; dx++) for (int dz = -24; dz <= 24; dz++) for (int dy = -6; dy <= 4; dy++) {
            class_2338 p = f.method_10069(dx, dy, dz);
            if (this.badSources.containsKey(p.method_10063()) || !this.takeableSource(w, p)) continue;
            // only the top of the water can be scooped
            if (!w.method_8320(p.method_10084()).method_26215()) continue;
            double d = dx * dx + dz * dz + dy * dy * 2.0;
            if (d < bd) { bd = d; best = p; }
        }
        if (best == null) {
            this.refillCheckAt = now + 15_000L;
            if (this.count(class_1802.field_8705) == 0) this.missing(class_1802.field_8705);
            return false;
        }
        this.refillAt = best;
        this.status = "Filling a bucket";
        class_243 surf = new class_243(best.method_10263() + 0.5, best.method_10264() + 0.85, best.method_10260() + 0.5);
        if (pl.method_33571().method_1025(surf) <= this.reach() * this.reach()) {
            this.mode = Mode.REFILL;
            this.modeTicks = 0;
            return true;
        }
        double reach = this.reach() - 0.5;
        final class_2338 src = best;
        boolean ok = this.walk(w, pl, q -> {
            if (q.equals(src) || q.method_10084().equals(src)) return false;
            double ex = q.method_10263() + 0.5 - surf.field_1352, ey = q.method_10264() + 1.5 - surf.field_1351, ez = q.method_10260() + 0.5 - surf.field_1350;
            return ex * ex + ey * ey + ez * ez <= reach * reach && ey > 0.3;
        }, best, () -> { this.mode = Mode.REFILL; this.modeTicks = 0; }, 9000);
        if (!ok) { this.badSources.put(best.method_10063(), now + 60_000L); this.refillAt = null; }
        else this.status = "Walking to water";
        return ok;
    }

    /** Looks at the water surface and right-clicks with the empty bucket (like vanilla: block click, then the item). */
    private void tickRefill(class_638 w, class_746 pl) {
        class_2338 p = this.refillAt;
        if (p == null || this.count(class_1802.field_8705) > 0) { this.refillAt = null; this.toIdle(); return; }
        if (!waterSource(w.method_8320(p)) || this.modeTicks > 120) {
            this.badSources.put(p.method_10063(), System.currentTimeMillis() + 60_000L);
            this.refillAt = null;
            this.toIdle();
            return;
        }
        class_1792 empty = itemById("bucket");
        if (!this.hold(pl, empty)) { if (this.count(empty) == 0) this.toIdle(); return; }
        this.keys(false, false, false, true);
        if (!pl.method_5715() && this.modeTicks < 6) return;
        class_243 surf = new class_243(p.method_10263() + 0.5, p.method_10264() + 0.85, p.method_10260() + 0.5);
        float[] rot = SbPlacer.lookAt(pl.method_33571(), surf);
        if (!this.rotateTo(pl, rot[0], rot[1], 0.8f) || !settled(pl) || this.modeTicks < this.placeDelay()) return;
        // what the bucket really scoops: the first source along the look
        var fr = pl.method_5745(pl.method_55754(), 1.0f, true);
        if (!(fr instanceof class_3965 fh) || fr.method_17783() != net.minecraft.class_239.class_240.field_1332 || !fh.method_17777().equals(p)) {
            this.rotateTo(pl, rot[0], rot[1], 0.2f);
            if (++this.aimMiss > 25) { this.aimMiss = 0; this.badSources.put(p.method_10063(), System.currentTimeMillis() + 60_000L); this.refillAt = null; this.toIdle(); }
            return;
        }
        class_3965 hit = this.crosshair(pl);
        if (hit != null && this.dupRotRisk(pl)) return;
        if (hit != null) this.mc.field_1761.method_2896(pl, class_1268.field_5808, hit);
        var res = this.mc.field_1761.method_2919(pl, class_1268.field_5808);
        this.clickedBlock(); // Grim counts every block click as a place
        pl.method_6104(class_1268.field_5808);
        this.lastClickTick = this.tickNo;
        this.aimMiss = 0;
        this.lastProgressMs = System.currentTimeMillis();
        dbg("refill bucket at " + p.method_23854() + " " + res);
        this.waitTicks = 5;
        this.modeTicks = 0;
    }

    // ================================================================== shulker box restock

    /** Where our shulker box stands; stage 1 = place it, 2 = open it, 3 = break it, 4 = pick it up. */
    private class_2338 shulkerPos;
    private int shulkerStage, shulkerSlot, shulkerOpens, shulkerRoomTries;
    private long shulkerCheckAt, shulkerStageAt;

    private static boolean isShulker(class_1799 st) {
        return st.method_7909() instanceof class_1747 bi && bi.method_7711() instanceof net.minecraft.class_2480;
    }

    /** What a shulker box item holds. */
    private static Map<class_1792, Integer> shulkerContents(class_1799 st) {
        Map<class_1792, Integer> m = new HashMap<>();
        try {
            net.minecraft.class_9288 c = st.method_58694(net.minecraft.class_9334.field_49622);
            if (c != null) for (class_1799 s : c.method_59714()) m.merge(s.method_7909(), s.method_7947(), Integer::sum);
        } catch (Throwable ignored) {
        }
        return m;
    }

    private boolean tryShulker(class_638 w, class_746 pl) {
        if (this.shulkerPos != null) {
            this.mode = Mode.SHULKER;
            this.modeTicks = 0;
            return true;
        }
        long now = System.currentTimeMillis();
        if (now < this.shulkerCheckAt) return false;
        this.shulkerCheckAt = now + 3000L;
        Map<class_1792, Integer> need = this.needs(pl);
        if (need.isEmpty()) return false;
        class_1661 inv = pl.method_31548();
        int slot = -1;
        for (int i = 0; i < 36 && slot < 0; i++) {
            class_1799 st = inv.method_5438(i);
            if (!isShulker(st)) continue;
            Map<class_1792, Integer> in = shulkerContents(st);
            for (class_1792 it : need.keySet()) if (in.getOrDefault(it, 0) > 0) { slot = i; break; }
        }
        if (slot < 0) return false;
        // a free spot right beside us: air with room above (the lid opens upwards) and a floor below,
        // outside the build if possible (never where the schematic wants a block)
        class_1937 sw = SbLitematica.world();
        class_2338 f = feet(pl);
        class_2338 best = null;
        double bd = Double.MAX_VALUE;
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) for (int dy = -1; dy <= 1; dy++) {
            if (dx == 0 && dz == 0) continue;
            class_2338 q = f.method_10069(dx, dy, dz);
            if (!w.method_8320(q).method_26215() || !w.method_8320(q.method_10084()).method_26215() || !SbWorld.floor(w, q.method_10074())) continue;
            if (pl.method_5829().method_994(new class_238(q)) || this.storage.contains(q.method_10063())) continue;
            boolean inside = this.inBox(q);
            if (inside && (sw == null || !SbLitematica.target(sw, q).method_26215() || !SbLitematica.target(sw, q.method_10084()).method_26215())) continue;
            double d = dx * dx + dz * dz + dy * dy * 2 + (inside ? 20 : 0);
            if (d < bd) { bd = d; best = q; }
        }
        if (best == null) return false;
        dbg("shulker: placing the box from slot " + slot + " at " + best.method_23854() + " for " + need.keySet());
        this.shulkerPos = best;
        this.shulkerSlot = slot;
        this.shulkerOpens = 0;
        this.shulkerStage = 1;
        this.shulkerStageAt = now;
        this.status = "Unpacking a shulker box";
        this.mode = Mode.SHULKER;
        this.modeTicks = 0;
        return true;
    }

    private void shulkerDone(String why) {
        dbg("shulker: " + why);
        this.shulkerPos = null;
        this.shulkerStage = 0;
        this.toIdle();
    }

    private void tickShulker(class_638 w, class_746 pl) {
        class_2338 p = this.shulkerPos;
        if (p == null) { this.toIdle(); return; }
        long now = System.currentTimeMillis();
        boolean standing = w.method_8320(p).method_26204() instanceof net.minecraft.class_2480;
        if (now - this.shulkerStageAt > 60_000L) {
            if (standing) this.say("§eLeft a shulker box at " + p.method_10263() + " " + p.method_10264() + " " + p.method_10260() + " - could not pick it up again.");
            this.shulkerCheckAt = now + 60_000L;
            this.shulkerDone("gave up in stage " + this.shulkerStage);
            return;
        }
        this.status = "Unpacking a shulker box";
        switch (this.shulkerStage) {
            case 1 -> {
                if (standing) { this.shulkerStage = 2; this.shulkerStageAt = now; return; }
                class_1661 inv = pl.method_31548();
                class_1799 st = inv.method_5438(this.shulkerSlot);
                if (!isShulker(st)) { this.shulkerCheckAt = 0; this.shulkerDone("box moved"); return; }
                if (!w.method_8320(p).method_26215()) { this.shulkerDone("spot taken"); return; }
                // the very box that has the items in the hand (not just any box of that colour)
                if (inv.method_67532() != this.shulkerSlot) {
                    if (this.shulkerSlot < 9) { inv.method_61496(this.shulkerSlot); return; }
                    if (this.swapCooldown > 0) { this.swapCooldown--; return; }
                    if (pl.field_7512 != pl.field_7498 || !this.readyForInventory(pl)) return;
                    int hb = this.freeHotbar(inv);
                    this.inventorySwap(pl, this.shulkerSlot, hb);
                    inv.method_61496(hb);
                    this.shulkerSlot = hb;
                    this.swapCooldown = 3;
                    return;
                }
                class_1747 item = (class_1747) st.method_7909();
                class_2680 want = item.method_7711().method_9564();
                this.keys(false, false, false, true);
                SbPlacer.Solution sol = SbPlacer.find(this.mc, p, want, item, pl.method_33571(), this.reach());
                if (sol == null) {
                    if (this.modeTicks > 40) { this.shulkerCheckAt = now + 20_000L; this.shulkerDone("no way to place it"); }
                    return;
                }
                int r = this.aimAndPlace(w, pl, p, want, item, sol.yaw(), sol.pitch(), this.modeTicks >= this.placeDelay());
                if (r == 1) { this.waitTicks = 4; this.modeTicks = 0; }
                else if (r < 0 && this.modeTicks > 60) { this.shulkerCheckAt = now + 20_000L; this.shulkerDone("aim failed"); }
            }
            case 2 -> {
                if (!standing) { this.shulkerStage = 4; this.shulkerStageAt = now; return; }
                // open it and take what we need (the normal chest code)
                this.storageSeen.remove(p.method_10063());
                this.wanted = this.needs(pl);
                this.chestPos = p;
                this.shulkerStage = 3;
                this.mode = Mode.OPEN_CHEST;
                this.modeTicks = 0;
            }
            case 3 -> {
                if (!standing) { this.shulkerStage = 4; this.shulkerStageAt = now; this.modeTicks = 0; return; }
                if (pl.field_7512 != pl.field_7498) { pl.method_7346(); this.restoreScreen(); return; }
                // the menu got closed before we could look inside: open it again
                if (!this.storageSeen.containsKey(p.method_10063()) && this.shulkerOpens++ < 4) { this.shulkerStage = 2; return; }
                // pushed away while the menu was open (or opened it from the edge of reach): walk back to it first,
                // breaking from here would only fail over and over
                class_3965 bf = this.visibleFace(w, pl, p);
                double rr = this.reach() - 0.2;
                if ((bf == null || pl.method_33571().method_1025(bf.method_17784()) > rr * rr) && this.walkNear(w, pl, p, null)) {
                    this.status = "Walking back to the shulker box";
                    dbg("shulker: walking back to the box at " + p.method_23854());
                    return;
                }
                this.storageSeen.remove(p.method_10063());
                this.startBreak(p, null);
                this.status = "Packing the shulker box";
            }
            case 4 -> {
                // walk to the dropped box: it gets picked up on the way
                var drops = w.method_8390(net.minecraft.class_1542.class, new class_238(p).method_1014(4.0), e -> isShulker(e.method_6983()));
                if (drops.isEmpty()) {
                    if (this.modeTicks > 10) { this.shulkerCheckAt = now + 1000L; this.shulkerDone("box packed up again"); }
                    return;
                }
                class_2338 at = drops.get(0).method_24515();
                this.keys(false, false, false, false);
                // standing on it and it still isn't picked up: the inventory is full
                if (this.freeSlots(pl) == 0 && drops.get(0).method_5739(pl) < 1.5f && this.modeTicks > 40) {
                    this.say("§eInventory full - the shulker box lies at " + at.method_10263() + " " + at.method_10264() + " " + at.method_10260() + ", make room and pick it up.");
                    this.shulkerCheckAt = now + 60_000L;
                    this.shulkerDone("inventory full, box left on the ground");
                    return;
                }
                if (this.modeTicks % 30 == 1) {
                    if (!this.walk(w, pl, q -> q.equals(at), at, () -> { this.mode = Mode.SHULKER; this.modeTicks = 2; }, 4000)) dbg("shulker: no way to the box at " + at.method_23854());
                }
            }
            default -> this.shulkerDone("?");
        }
    }

    // ================================================================== torches against mob spawns

    /** Torches we place around the build (not part of the schematic). */
    private final Set<Long> lightPos = new HashSet<>();
    private long lightCheckAt;
    private int torchesPlaced;

    /**
     * Light Up Area: dark ground next to the build (block light 0, where mobs spawn at night) gets a torch, like a
     * player lights up around their base. Only within reach, outside the build, a torch lights about 12 blocks.
     */
    /** Self-test: light up around the build. */
    void testLightUp() {
        this.lightUp.setValue(true);
    }

    private boolean tryLight(class_638 w, class_746 pl) {
        if (!this.lightUp.getValue() || this.tasks.isEmpty()) return false;
        long now = System.currentTimeMillis();
        if (now < this.lightCheckAt) return false;
        this.lightCheckAt = now + 4000L;
        class_1792 torch = class_1802.field_8810;
        if (this.count(torch) == 0 || !(torch instanceof class_1747 ti)) return false;
        if (this.placement == null) return false;
        int[] b = SbLitematica.box(this.placement);
        if (b == null) return false;
        class_2680 want = ti.method_7711().method_9564();
        class_2338 f = feet(pl);
        class_243 eye = pl.method_33571();
        class_2338 best = null;
        double bd = Double.MAX_VALUE;
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) for (int dy = -2; dy <= 1; dy++) {
            class_2338 q = f.method_10069(dx, dy, dz);
            if (this.inBox(q) || this.lightPos.contains(q.method_10063()) || this.storage.contains(q.method_10063())) continue;
            // only the ground around the build (8 blocks), not anywhere we walk
            int x = q.method_10263(), y = q.method_10264(), z = q.method_10260();
            if (x < b[0] - 8 || x > b[3] + 8 || z < b[2] - 8 || z > b[5] + 8 || y < b[1] - 3 || y > b[4]) continue;
            if (!w.method_8320(q).method_26215() || w.method_8314(net.minecraft.class_1944.field_9282, q) > 0) continue;
            if (!SbWorld.floor(w, q.method_10074()) || !want.method_26184(w, q)) continue;
            double d = dx * dx + dz * dz + dy * dy;
            if (d < 1.5 || d > bd) continue;
            if (SbPlacer.find(this.mc, q, want, ti, eye, this.reach()) == null) continue;
            bd = d;
            best = q;
        }
        if (best == null) return false;
        dbg("light: torch at " + best.method_23854());
        this.lightPos.add(best.method_10063());
        this.task = new Task(best, want, true);
        this.mode = Mode.PLACE;
        this.modeTicks = 0;
        this.status = "Placing a torch";
        return true;
    }

    // ================================================================== legit actions (what a player could do)

    /**
     * Mouse step of the player's sensitivity: real mouse input only ever turns the head in multiples of this, and
     * anticheats (Grim's aim checks) look for rotations that are not.
     */
    private float mouseStep() {
        double s = 0.5;
        try { s = ((Number) this.mc.field_1690.method_42495().method_41753()).doubleValue(); } catch (Throwable ignored) { }
        double f = s * 0.6 + 0.2;
        return (float) (f * f * f * 8.0 * 0.15);
    }

    /** The server already has the rotation we are looking with (it didn't change since the last movement packet). */
    private boolean settled(class_746 pl) {
        return pl.method_36454() == this.sentYaw && pl.method_36455() == this.sentPitch;
    }

    private float sentYaw = Float.NaN, sentPitch = Float.NaN;

    /**
     * The game only turns the head 20 times a second; the frames in between are drawn from the rotation of the last
     * tick. Without this every bot turn would jump in steps on screen - now it glides like a mouse.
     */
    @EventListener
    public void onSendMovementSmooth(hook.event.events.SendMovementPacketsEvent e) {
        class_746 pl = this.mc.field_1724;
        if (pl == null || this.renderTick != this.tickNo || Float.isNaN(this.renderYaw)) return;
        pl.field_5982 = this.renderYaw;
        pl.field_6004 = this.renderPitch;
        // this tick's turn as one piece of a smooth curve: it starts as fast as the last piece ended, so the speed
        // never jumps at a tick (a straight line per tick changes speed 20 times a second - that is the stutter)
        float chordY = pl.method_36454() - this.renderYaw, chordP = pl.method_36455() - this.renderPitch;
        this.segM0Y = limitTangent(this.segTanY, chordY);
        this.segM0P = limitTangent(this.segTanP, chordP);
        this.segM1Y = limitTangent(chordY, chordY);
        this.segM1P = limitTangent(chordP, chordP);
        this.segTanY = this.segM1Y;
        this.segTanP = this.segM1P;
        this.segY1 = pl.method_36454();
        this.segP1 = pl.method_36455();
        this.segTick = this.tickNo;
        if (SbAcTest.ON) SbAcTest.simulateTick(this.renderYaw, this.segY1, this.segM0Y, this.segM1Y, this.renderPitch, this.segP1, this.segM0P, this.segM1P);
    }

    private static final boolean LINEAR = Boolean.getBoolean("hook.sb.linear");
    private float segTanY, segTanP, segM0Y, segM0P, segM1Y, segM1P, segY1, segP1;
    private long segTick = -1;

    /** Monotone (Fritsch-Carlson style): no tangent against the move or more than 3x it, so the curve never wobbles. */
    private static float limitTangent(float m, float chord) {
        if (chord == 0.0f || Math.signum(m) != Math.signum(chord)) return 0.0f;
        return Math.abs(m) > Math.abs(chord) * 3.0f ? chord * 3.0f : m;
    }

    private static float hermite(float a, float b, float m0, float m1, float t) {
        float t2 = t * t, t3 = t2 * t;
        return (2 * t3 - 3 * t2 + 1) * a + (t3 - 2 * t2 + t) * m0 + (-2 * t3 + 3 * t2) * b + (t3 - t2) * m1;
    }

    /**
     * Every frame, right before the camera is placed: the drawn view follows the bot's head softly instead of in
     * 20 straight pieces a second (only what is drawn - what the server gets is untouched).
     */
    public static void cameraFrame(float t) {
        SchematicBuilderModule m = instance;
        if (m == null || !m.isEnabled()) return;
        class_746 pl = m.mc.field_1724;
        if (pl == null || LINEAR) { m.camY = Float.NaN; return; }
        if (m.renderTick != m.tickNo || m.segTick != m.tickNo || t >= 0.999f || t < 0.0f) return;
        // where the plain game would draw the head this frame (a straight line per tick, plus any turn of the
        // user's own mouse since the tick) ...
        float ly = m.renderYaw + (m.segY1 - m.renderYaw) * t + (pl.method_36454() - m.segY1);
        float lp = m.renderPitch + (m.segP1 - m.renderPitch) * t + (pl.method_36455() - m.segP1);
        long now = System.nanoTime();
        if (Float.isNaN(m.camY) || Math.abs(ly - m.camY) > 60.0f || Math.abs(lp - m.camP) > 60.0f || now - m.camNs > 200_000_000L) {
            m.camY = ly;
            m.camP = lp;
        } else {
            // ... and the view glides after it like a heavy camera: speed changes at the ticks get rounded off
            float a = (float) (1.0 - Math.exp(-((now - m.camNs) / 1.0e9) / CAMERA_SMOOTH));
            m.camY += (ly - m.camY) * a;
            m.camP += (lp - m.camP) * a;
        }
        m.camNs = now;
        // the game draws lerp(t, last, now): pick "last" so that lands exactly on the smoothed view
        pl.field_5982 = (m.camY - t * pl.method_36454()) / (1.0f - t);
        pl.field_6004 = (m.camP - t * pl.method_36455()) / (1.0f - t);
        if (SbAcTest.ON) SbAcTest.frame(m.camY, m.camP);
    }

    /** Seconds the view takes to catch up (63%) - long enough to hide the 20-per-second steps, short enough to feel direct. */
    private static final double CAMERA_SMOOTH = 0.04;
    private float camY = Float.NaN, camP;
    private long camNs;

    /** Self-test only: what an anticheat would think of every packet. */
    @EventListener
    public void onPacketSend(hook.event.events.PacketSendEvent e) {
        // any block click counts as a place for Grim: remember the turn that went before it
        if (e.packet instanceof net.minecraft.class_2885 && this.gRotated) { this.gLastPlaced = this.gDeltaX; this.gRotated = false; }
        if (SbAcTest.ON) SbAcTest.onSend(e.packet);
    }

    @EventListener
    public void onPacketReceive(hook.event.events.PacketReceiveEvent e) {
        if (SbAcTest.ON) SbAcTest.onReceive(e.packet);
    }

    /** The rotation that goes out with this tick's movement packet. */
    @EventListener
    public void onSendMovement(hook.event.events.SendMovementPacketsEvent e) {
        class_746 pl = this.mc.field_1724;
        if (pl == null) return;
        // what Grim DuplicateRotPlace sees: the yaw turn of the last rotating movement packet
        if (!Float.isNaN(this.sentYaw) && (pl.method_36454() != this.sentYaw || pl.method_36455() != this.sentPitch)) {
            this.gDeltaX = Math.abs(pl.method_36454() - this.sentYaw);
            this.gRotated = true;
        }
        this.sentYaw = pl.method_36454();
        this.sentPitch = pl.method_36455();
    }

    /** What the crosshair really points at right now (exactly what vanilla would click). */
    private class_3965 crosshair(class_746 pl) {
        var r = pl.method_5745(pl.method_55754(), 1.0f, false);
        return r instanceof class_3965 b && r.method_17783() == net.minecraft.class_239.class_240.field_1332 ? b : null;
    }

    /**
     * Turns towards the aim, and once the head is still and the server knows that rotation, right-clicks whatever
     * the crosshair hits - but only if that click really gives the wanted block at the target.
     * 1 = placed, 0 = still aiming / waiting, -1 = the crosshair is on the wrong spot.
     */
    private int aimAndPlace(class_638 w, class_746 pl, class_2338 target, class_2680 want, class_1747 item, float yaw, float pitch, boolean mayClick) {
        boolean there = this.rotateTo(pl, yaw, pitch, 0.4f);
        if (!there || !settled(pl) || !mayClick) { if (!there) this.reactWait = -1; return 0; }
        // a moment to notice it is on target before clicking (varies like a real reaction)
        if (this.human.getValue()) {
            if (this.reactWait < 0) this.reactWait = this.rnd.nextInt(3);
            if (this.reactWait-- > 0) return 0;
        }
        class_3965 hit = this.crosshair(pl);
        if (hit == null) { dbg("aim: crosshair on nothing, target " + target.method_23854()); this.aimPrecise = true; this.aimTY = Float.NaN; return -1; }
        // clicking a chest, furnace, trapdoor, lever... without sneaking uses it instead of placing against it
        if (!pl.method_5715() && SbWorld.usable(w, hit.method_17777())) {
            this.forceSneakTicks = 10;
            this.mc.field_1690.field_1832.method_23481(true);
            return 0;
        }
        class_2680 st = SbPlacer.simulate(pl, w, item, hit, new float[]{pl.method_36454(), pl.method_36455()}, target, w.method_8320(target));
        if (st == null || want != null && !SbWorld.matchesShape(want, st)) {
            dbg("aim: target " + target.method_23854() + " crosshair " + hit.method_17777().method_23854() + " " + hit.method_17780() + " @" + hit.method_17784()
                    + " gives " + st + " want " + want + " rot " + pl.method_36454() + "/" + pl.method_36455() + " aim " + yaw + "/" + pitch + " eye " + pl.method_33571());
            this.aimPrecise = true; // the next try aims at the exact planned spot
            this.aimTY = Float.NaN;
            return -1;
        }
        // the last turn before this place equals the one before the last place: a tiny correction first
        if (this.dupRotRisk(pl)) return 0;
        var res = this.mc.field_1761.method_2896(pl, class_1268.field_5808, hit);
        if (!res.method_23665()) {
            dbg("aim: click refused " + res + " target " + target.method_23854() + " crosshair " + hit.method_17777().method_23854() + " " + hit.method_17780()
                    + " holding " + pl.method_6047() + " feet " + pfeet());
            return -1;
        }
        pl.method_6104(class_1268.field_5808);
        this.lastClickTick = this.tickNo;
        this.lastBeforePlace = this.lastTurnAbs;
        this.afterPlace = true;
        return 1;
    }

    /**
     * Mines a block like holding the attack button: look at it first, then only ever hit what the crosshair is on.
     * False while still turning.
     */
    private boolean aimAndMine(class_638 w, class_746 pl, class_2338 p, class_3965 face) {
        if (this.correctInBuild(w, p) && !this.escape && !p.equals(this.unseal)) {
            if (this.guardLogged.add(p.method_10063())) {
                this.savedBlocks++;
                dbg("refused to break correct " + p.method_23854() + " (" + this.mode + ", " + this.status + ")");
            }
            if (this.mode == Mode.BREAK) this.toIdle();
            return false;
        }
        float[] rot = SbPlacer.lookAt(pl.method_33571(), face.method_17784());
        boolean there = this.rotateTo(pl, rot[0], rot[1], 1.0f);
        class_3965 hit = this.crosshair(pl);
        if (hit == null || !hit.method_17777().equals(p)) {
            if (there && settled(pl)) this.rotateTo(pl, rot[0], rot[1], 0.2f);
            return false;
        }
        this.breakingTick = this.tickNo;
        this.mc.field_1761.method_2902(p, hit.method_17780());
        pl.method_6104(class_1268.field_5808);
        return true;
    }

    /**
     * Inventory clicks only while standing still (not sprinting or sneaking), and closed again right after like
     * opening and closing the inventory screen. False = stopping first.
     */
    private boolean readyForInventory(class_746 pl) {
        var v = pl.method_18798();
        // also the last step: the server judges the movement it was sent, not the speed left after friction
        double mx = pl.method_23317() - pl.field_6014, mz = pl.method_23321() - pl.field_5969;
        if (v.field_1352 * v.field_1352 + v.field_1350 * v.field_1350 > 1.0E-4 || mx * mx + mz * mz > 1.0E-4 || pl.method_5624() || pl.method_5715() || !pl.method_24828()) {
            this.freezeTicks = Math.max(this.freezeTicks, 3);
            this.keys(false, false, false, false);
            return false;
        }
        return true;
    }

    private void inventorySwap(class_746 pl, int slot, int hotbar) {
        this.mc.field_1761.method_2906(pl.field_7498.field_7763, slot, hotbar, class_1713.field_7791, (class_1657) pl);
        this.mc.method_1562().method_52787(new net.minecraft.class_2815(pl.field_7498.field_7763));
    }

    private int placeDelay() {
        double base = 20.0 / Math.max(0.5, this.speed.getValue());
        if (this.human.getValue()) base *= 0.75 + this.rnd.nextDouble() * 0.6;
        return Math.max(1, (int) Math.round(base));
    }

    /** A block next to it exists to click on (otherwise it simply isn't its turn yet). */
    private boolean supported(class_638 w, class_2338 p) {
        for (class_2350 d : class_2350.values()) {
            class_2338 n = p.method_10093(d);
            class_2680 s = w.method_8320(n);
            if (!s.method_26215() && !s.method_45474() && !s.method_26218(w, n).method_1110()) return true;
        }
        return false;
    }

    /**
     * Some states need a block to click on that the schematic doesn't have there (a top slab with nothing beside
     * it...). Find a free neighbour where a scaffold block would make it placeable, and place that first.
     */
    private boolean tryHelper(class_638 w, class_746 pl, Task t) {
        class_1792 sc = this.scaffoldItem();
        if (!(sc instanceof class_1747 sbi) || this.count(sc) == 0 || !this.useScaffold.getValue()) {
            dbg("helper: no scaffold blocks (" + this.count(sc) + ")");
            return false;
        }
        class_1792 item = itemFor(t.want);
        if (!(item instanceof class_1747 bi)) return false;
        SbPath.Rules r = this.rules();
        class_243 eye = pl.method_33571();
        class_2680 cur = w.method_8320(t.pos);
        float[][] rots = {null, {pl.method_36454(), -90f}, {pl.method_36454(), 90f}, {0f, 0f}, {90f, 0f}, {180f, 0f}, {-90f, 0f}};
        for (class_2350 d : class_2350.values()) {
            class_2338 nb = t.pos.method_10093(d);
            if (!w.method_8320(nb).method_45474() || SbWorld.dangerous(w.method_8320(nb)) || !r.mayScaffold.test(nb)) continue;
            if (this.storage.contains(nb.method_10063()) || pl.method_5829().method_994(new class_238(nb))) continue;
            if (this.failedUntil.containsKey(nb.method_10063())) continue;
            class_2350 side = d.method_10153();
            double[] ys = d.method_10164() == 0 ? new double[]{0.0, 0.3, -0.3} : new double[]{0.0};
            boolean ok = false;
            // the click only lands on the target if there really is a block at nb: pretend it is there for a moment
            class_2680 old = w.method_8320(nb);
            w.method_8501(nb, sbi.method_7711().method_9564());
            try {
            for (double yo : ys) {
                class_243 point = class_243.method_24953(t.pos).method_1031(d.method_10148() * 0.5, d.method_10164() * 0.5 + yo, d.method_10165() * 0.5);
                class_3965 hit = new class_3965(point, side, nb, false);
                for (float[] rot : rots) {
                    class_2680 st = SbPlacer.simulate(pl, w, bi, hit, rot == null ? SbPlacer.lookAt(eye, point) : rot, t.pos, cur, true);
                    if (st != null && SbWorld.matchesShape(t.want, st)) { ok = true; break; }
                }
                if (ok) break;
            }
            } finally {
                w.method_8501(nb, old);
            }
            if (!ok) { dbg("helper " + nb.method_23854() + " would not give the wanted state"); continue; }
            Task h = new Task(nb, sbi.method_7711().method_9564(), true);
            this.helperPos.add(nb.method_10063());
            if (SbPlacer.find(this.mc, nb, null, sbi, eye, this.reach()) != null) {
                this.task = h;
                this.mode = Mode.PLACE;
                this.modeTicks = 0;
                this.status = "Placing a helper block";
                return true;
            }
            if (this.walkNear(w, pl, nb, h)) { this.status = "Walking (helper block)"; return true; }
            dbg("helper " + nb.method_23854() + " not reachable");
            this.helperPos.remove(nb.method_10063());
        }
        return false;
    }

    /** Some block that can be built the normal way is still left anywhere (then floating ones just wait for it). */
    private boolean anySupportedLeft(class_638 w) {
        for (Task t : this.tasks.values()) {
            if (!t.place || t.kind != SbFix.PLACE) return true; // breaking / clicking work left
            if (this.skipped.contains(t.pos.method_10063())) continue;
            class_1792 it = itemFor(t.want);
            if (this.count(it) == 0) continue;
            if (this.supported(w, t.pos)) return true;
        }
        return false;
    }

    private class_2338 doorOpened;
    private boolean doorPassed;
    private int doorTicks;

    /**
     * Doors on the way: open a closed one in the next steps (like a player, aimed and clicked without sneaking),
     * and close it again once we went through and are clear of it. True while busy with a door.
     */
    private boolean handleDoors(class_638 w, class_746 pl) {
        class_243 eye = pl.method_33571();
        double r2 = this.reach() * this.reach();
        if (this.doorOpened != null) {
            class_2338 d = this.doorOpened;
            class_2680 ds = w.method_8320(d);
            boolean inIt = pl.method_5829().method_994(new class_238(d)) || pl.method_5829().method_994(new class_238(d.method_10084()));
            if (inIt) this.doorPassed = true;
            double hx = pl.method_23317() - (d.method_10263() + 0.5), hz = pl.method_23321() - (d.method_10260() + 0.5);
            if (!SbWorld.door(ds) || !SbWorld.doorOpen(ds) || eye.method_1025(class_243.method_24953(d)) > r2) this.doorOpened = null;
            else if (this.doorPassed && !inIt && hx * hx + hz * hz > 1.5 * 1.5) {
                this.keys(false, false, false, false);
                if (this.clickDoor(w, pl, d)) { dbg("closed door " + d.method_23854() + " behind us"); this.doorOpened = null; }
                else if (++this.doorTicks > 60) { dbg("could not close door " + d.method_23854()); this.doorOpened = null; this.doorTicks = 0; }
                return true;
            }
        }
        for (int i = this.pathIdx; i < Math.min(this.pathIdx + 2, this.path.size()); i++) {
            class_2338 p = this.path.get(i).pos();
            for (class_2338 q : new class_2338[]{p, p.method_10084()}) {
                class_2680 st = w.method_8320(q);
                if (!SbWorld.door(st) || SbWorld.doorOpen(st)) continue;
                if (eye.method_1025(class_243.method_24953(q)) > r2) return false; // walk closer first
                class_2338 low = SbWorld.door(w.method_8320(q.method_10074())) ? q.method_10074() : q;
                this.keys(false, false, false, false);
                if (this.clickDoor(w, pl, low)) { dbg("opened door " + low.method_23854()); this.doorOpened = low; this.doorPassed = false; }
                else if (++this.doorTicks > 60) {
                    dbg("could not open door " + low.method_23854() + " - new path");
                    this.doorTicks = 0;
                    this.avoid.put(low.method_10063(), System.currentTimeMillis() + 60_000L);
                    this.replan(w, pl, this.path.get(this.path.size() - 1).pos());
                }
                return true;
            }
        }
        return false;
    }

    /** Looks at the door (the panel, wherever it is) and right-clicks it - never while sneaking. */
    private boolean clickDoor(class_638 w, class_746 pl, class_2338 d) {
        // a spot of the door really in sight (the wall beside it hides the middle when standing at an angle),
        // the upper half first: that is at eye height
        class_3965 f = SbWorld.door(w.method_8320(d.method_10084())) ? this.visibleFace(w, pl, d.method_10084()) : null;
        if (f == null) f = this.visibleFace(w, pl, d);
        if (f == null) return false;
        float[] rot = SbPlacer.lookAt(pl.method_33571(), f.method_17784());
        boolean there = this.rotateTo(pl, rot[0], rot[1], 0.6f);
        if (!there || !settled(pl) || pl.method_5715()) return false;
        class_3965 hit = this.crosshair(pl);
        if (hit == null || !(hit.method_17777().equals(d) || hit.method_17777().equals(d.method_10084())) || !SbWorld.door(w.method_8320(hit.method_17777()))) {
            this.rotateTo(pl, rot[0], rot[1], 0.2f);
            return false;
        }
        if (SbAcTest.ON) {
            class_2338 hp = hit.method_17777();
            var hs = w.method_8320(hp).method_26218(w, hp);
            SbAcTest.intendedPos = hp.method_10063();
            SbAcTest.intendedBox = hs.method_1110() ? new class_238(hp) : hs.method_1107().method_996(hp);
        }
        if (this.dupRotRisk(pl)) return false;
        var res = this.mc.field_1761.method_2896(pl, class_1268.field_5808, hit);
        if (!res.method_23665()) return false;
        pl.method_6104(class_1268.field_5808);
        this.clickedBlock();
        this.lastClickTick = this.tickNo;
        this.doorTicks = 0;
        return true;
    }

    private void finishedBlock(Task t) {
        this.stuckRetries = 0;
        this.escape = false;
        this.tasks.remove(t.pos.method_10063());
        this.failCount.remove(t.pos.method_10063());
        this.touched(t.pos);
        this.toIdle();
    }

    // ================================================================== breaking

    private final Set<Long> guardLogged = new HashSet<>();
    private int savedBlocks;

    /** The block there is already exactly what the schematic wants: never mine it (the build would go backwards). */
    private boolean correctInBuild(class_638 w, class_2338 p) {
        class_1937 sw = SbLitematica.world();
        if (sw == null || !this.inBox(p)) return false;
        class_2680 want = SbLitematica.target(sw, p);
        return !want.method_26215() && SbWorld.matches(want, w.method_8320(p));
    }

    private void startBreak(class_2338 p, Task t) {
        if (System.getProperty("hook.carttest") != null) dbg("break " + p.method_23854() + " " + this.mc.field_1687.method_8320(p).method_26204().method_9518().getString() + " (" + this.status + ")");
        this.task = t;
        this.breakPos = p;
        this.mode = Mode.BREAK;
        this.modeTicks = 0;
        this.breakAimMiss = 0;
        this.status = t != null && t.place ? "Fixing wrong block" : "Breaking";
    }

    private int breakAimMiss, scaffoldWaits;
    private boolean scaffoldWarned;

    /** A face of the block the player can really see from here (null = hidden). */
    private class_3965 visibleFace(class_638 w, class_746 pl, class_2338 p) {
        return this.visibleFace(w, pl, p, pl.method_33571());
    }

    private class_3965 visibleFace(class_638 w, class_746 pl, class_2338 p, class_243 eye) {
        return this.facePoint(w, pl, p, eye, false);
    }

    /**
     * A spot on the block (its real outline: thin blocks like repeaters, rails or carpets too) the eye can see and
     * reach. {@code notBottom}: the bottom face doesn't count (hoes / shovels / buckets).
     */
    private class_3965 facePoint(class_638 w, class_746 pl, class_2338 p, class_243 eye, boolean notBottom) {
        class_238 bb;
        try {
            var shape = w.method_8320(p).method_26218(w, p);
            bb = shape.method_1110() ? new class_238(p) : shape.method_1107().method_996(p);
        } catch (Throwable t) {
            bb = new class_238(p);
        }
        double cx = (bb.field_1323 + bb.field_1320) / 2, cy = (bb.field_1322 + bb.field_1325) / 2, cz = (bb.field_1321 + bb.field_1324) / 2;
        double ex = (bb.field_1320 - bb.field_1323) * 0.4, ey = (bb.field_1325 - bb.field_1322) * 0.4, ez = (bb.field_1324 - bb.field_1321) * 0.4;
        double[][] pts = {{cx, cy, cz}, {cx, bb.field_1325 - 0.01, cz}, {cx, bb.field_1322 + 0.01, cz}, {bb.field_1323 + 0.01, cy, cz}, {bb.field_1320 - 0.01, cy, cz},
                {cx, cy, bb.field_1321 + 0.01}, {cx, cy, bb.field_1324 - 0.01}, {cx + ex, bb.field_1325 - 0.01, cz + ez}, {cx - ex, bb.field_1325 - 0.01, cz - ez},
                {cx, cy + ey, bb.field_1321 + 0.01}, {cx, cy + ey, bb.field_1324 - 0.01}, {bb.field_1323 + 0.01, cy + ey, cz}, {bb.field_1320 - 0.01, cy + ey, cz}};
        for (double[] o : pts) {
            class_243 to = new class_243(o[0], o[1], o[2]);
            if (eye.method_1025(to) > this.reach() * this.reach()) continue;
            class_3965 r = SbPlacer.ray(w, pl, eye, to);
            if (r != null && r.method_17777().equals(p) && !(notBottom && r.method_17780() == class_2350.field_11033)) return r;
        }
        return null;
    }

    private void tickBreak(class_638 w, class_746 pl) {
        class_2338 p = this.breakPos;
        class_2680 st = w.method_8320(p);
        if (st.method_26215() || st.method_45474() && st.method_26227().method_15769() == false) {
            this.mc.field_1761.method_2925();
            this.scaffoldPlaced.remove(p.method_10063());
            this.touched(p);
            this.lastProgressMs = System.currentTimeMillis();
            Task t = this.task;
            this.tasks.remove(p.method_10063());
            this.toIdle();
            if (t != null && t.place && !t.want.method_26215()) this.tasks.put(p.method_10063(), new Task(p, t.want, true));
            this.waitTicks = this.human.getValue() ? 2 + this.rnd.nextInt(3) : 1;
            return;
        }
        if (this.modeTicks > 400 || SbWorld.lava(st)) { this.mc.field_1761.method_2925(); this.fail(p, 30_000L, "breaktimeout"); this.toIdle(); return; }
        class_3965 face = this.visibleFace(w, pl, p);
        if (face == null) { this.mc.field_1761.method_2925(); this.fail(p, 10_000L, "breakface"); this.toIdle(); return; }
        this.keys(false, false, false, false);
        this.holdTool(pl, st, p);
        if (this.aimAndMine(w, pl, p, face)) this.breakAimMiss = 0;
        else if (++this.breakAimMiss > 50) {
            // the crosshair never gets onto it from here (only a corner shows): try it from another spot
            this.mc.field_1761.method_2925();
            this.badSpots.put(feet(pl).method_10063(), System.currentTimeMillis() + 60_000L);
            this.fail(p, 4_000L, "breakaim");
            this.toIdle();
        }
    }

    // ================================================================== walking

    private SbPath.Rules rules() {
        SbPath.Rules r = new SbPath.Rules();
        r.maxFall = this.maxFall.getIntValue();
        r.scaffold = this.useScaffold.getValue() && this.count(this.scaffoldItem()) > 0;
        r.breakObstacles = this.breakObstacles.getValue();
        r.avoid = new HashSet<>(this.avoid.keySet());
        class_1937 sw = SbLitematica.world();
        r.mayBreak = p -> {
            if (this.storage.contains(p.method_10063())) return false;
            if (sw != null && this.inBox(p)) {
                if (this.escape) return !SbWorld.container(this.mc.field_1687.method_8320(p));
                class_2680 want = SbLitematica.target(sw, p);
                // never break what is already correct in the build
                return want.method_26215() || !SbWorld.matchesShape(want, this.mc.field_1687.method_8320(p));
            }
            return true;
        };
        r.mayScaffold = p -> (sw == null || !this.inBox(p) || SbLitematica.target(sw, p).method_26215()
                || SbLitematica.target(sw, p).method_26204() == this.scaffoldItem().method_7711()) && !this.besideCactus(sw, p);
        r.mayBuildPillar = p -> this.pillarItem(sw, p) != null;
        return r;
    }

    /**
     * The item to pillar up with at {@code p} when the build wants a plain full block there (stone, planks, bricks,
     * an upright log...) that is still missing and that we carry - placed from above it comes out exactly right.
     */
    private class_1747 pillarItem(class_1937 sw, class_2338 p) {
        if (sw == null || !this.inBox(p)) return null;
        Task t = this.tasks.get(p.method_10063());
        if (t == null || !t.place || t.kind != SbFix.PLACE) return null;
        class_2680 want = t.want;
        if (want != want.method_26204().method_9564() || !want.method_26234(sw, p) || SbWorld.container(want) || SbFix.cactus(want)) return null;
        if (!(itemFor(want) instanceof class_1747 bi) || this.count(bi) == 0) return null;
        if (!this.mc.field_1687.method_8320(p).method_45474()) return null;
        return bi;
    }

    /** A block here would break a cactus beside it (one that stands or one the schematic wants). */
    private boolean besideCactus(class_1937 sw, class_2338 p) {
        for (class_2350 d : new class_2350[]{class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039}) {
            class_2338 n = p.method_10093(d);
            if (SbFix.cactus(this.mc.field_1687.method_8320(n)) || sw != null && this.inBox(n) && SbFix.cactus(SbLitematica.target(sw, n))) return true;
        }
        return false;
    }

    /** Walk to a spot from which the task's block is within reach. */
    private boolean walkNear(class_638 w, class_746 pl, class_2338 target, Task t) {
        double reach = this.reach() - 0.3;
        class_243 c = class_243.method_24953(target);
        long tk = target.method_10063();
        class_1792 item = t != null && t.place ? itemFor(t.want) : null;
        return this.walk(w, pl, p -> {
            if (p.method_10063() == tk || p.method_10084().method_10063() == tk) return false;
            if (this.badSpots.containsKey(p.method_10063())) return false; // gave nothing from there just now
            double ex = p.method_10263() + 0.5 - c.field_1352, ey = p.method_10264() + 1.27 - c.field_1351, ez = p.method_10260() + 0.5 - c.field_1350;
            double d2 = ex * ex + ey * ey + ez * ez;
            if (d2 > reach * reach || d2 < 0.3) return false;
            // only spots from which the block can really be placed / hit (sneaking eye height)
            class_243 eye = new class_243(p.method_10263() + 0.5, p.method_10264() + 1.27, p.method_10260() + 0.5);
            if (t != null && t.place && isWater(t.want)) return this.waterClick(w, pl, target, eye) != null;
            if (t != null && t.interact()) return this.interactFace(w, pl, t, eye) != null;
            if (item instanceof class_1747 bi) return this.solution(t, bi, eye, reach) != null;
            return this.visibleFace(w, pl, target, eye) != null;
        }, target, () -> {}, 9000);
    }

    private boolean walk(class_638 w, class_746 pl, java.util.function.Predicate<class_2338> goal, class_2338 hint, Runnable onArrive, int nodes) {
        SbPath.Rules r = this.rules();
        r.maxNodes = nodes;
        List<SbPath.Step> p = SbPath.find(w, feet(pl), goal, hint, r);
        if (p == null) dbg("no path from " + feet(pl).method_23854() + " towards " + (hint == null ? "?" : hint.method_23854()));
        if (p == null) return false;
        if (p.isEmpty()) {
            // the goal spot is the block we are on: step onto its middle (that is where the plan looked from)
            double cx = feet(pl).method_10263() + 0.5 - pl.method_23317(), cz = feet(pl).method_10260() + 0.5 - pl.method_23321();
            if (cx * cx + cz * cz < 0.04) return false;
            p = new ArrayList<>(List.of(new SbPath.Step(feet(pl), SbPath.Move.WALK, List.of())));
        }
        this.path = p;
        this.pathIdx = 0;
        this.arrived = onArrive;
        this.walkGoalHint = hint;
        this.mode = Mode.WALK;
        this.modeTicks = 0;
        this.bestDist = Double.MAX_VALUE;
        this.noProgress = 0;
        if (!this.status.startsWith("Walking to") && !this.status.startsWith("Restock") && !this.status.startsWith("Removing")) this.status = "Walking";
        return true;
    }

    private void tickWalk(class_638 w, class_746 pl) {
        if (this.path == null || this.pathIdx >= this.path.size()) {
            this.releaseKeys();
            Runnable a = this.arrived;
            this.toIdle();
            this.stuckLevel = 0;
            if (a != null) a.run();
            return;
        }
        SbPath.Step s = this.path.get(this.pathIdx);
        // the block we walk to is already in reach from here: stop and build it (no creeping to the planned spot)
        if (this.reachableEarly(w, pl, s)) {
            this.walksCut++;
            this.toIdle();
            this.stuckLevel = 0;
            return;
        }
        // obstacles first
        for (class_2338 b : s.breaks()) {
            if (!SbWorld.passable(w, b)) {
                if (!SbWorld.breakable(w, b)) { this.replan(w, pl, s.pos()); return; }
                // planned while it was still wrong or missing, built correctly since: go around instead
                if (this.correctInBuild(w, b) && !this.escape) { this.savedBlocks++; dbg("path would break built " + b.method_23854() + " - new path"); this.replan(w, pl, s.pos()); return; }
                this.keys(false, false, false, false);
                class_3965 face = this.visibleFace(w, pl, b);
                if (face == null) { this.replan(w, pl, s.pos()); return; }
                this.holdTool(pl, w.method_8320(b), b);
                this.aimAndMine(w, pl, b, face);
                this.noProgress = 0;
                return;
            }
        }
        if (this.handleDoors(w, pl)) return;
        this.mc.field_1761.method_2925();
        double tx = s.pos().method_10263() + 0.5, tz = s.pos().method_10260() + 0.5;
        double dx = tx - pl.method_23317(), dz = tz - pl.method_23321();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        double dy = pl.method_23318() - s.pos().method_10264();
        boolean ground = pl.method_24828() || pl.method_5799();

        if (s.move() == SbPath.Move.PILLAR) {
            class_2338 under = s.pos().method_10074();
            if (SbWorld.floor(w, under) && Math.abs(dy) < 0.6 && ground) { this.nextStep(); return; }
            // nothing to set the block on (we only stand on the edge of the block next to it, the planned block
            // below never came) or something sits in the spot (a torch): jumping here would go on forever
            class_2680 us = w.method_8320(under);
            boolean blocked = !SbWorld.floor(w, under) && !us.method_45474();
            if (ground && (blocked || us.method_45474() && !SbWorld.floor(w, under.method_10074()))) {
                dbg("pillar at " + under.method_23854() + " impossible (" + (blocked ? SbFix.id(us.method_26204()) + " in the way" : "nothing below") + ") - new path");
                this.avoid.put(s.pos().method_10063(), System.currentTimeMillis() + 60_000L);
                this.avoid.put(under.method_10063(), System.currentTimeMillis() + 60_000L);
                this.replan(w, pl, s.pos());
                return;
            }
            class_1747 buildItem = this.pillarItem(SbLitematica.world(), under);
            class_1747 pillarWith = buildItem != null ? buildItem : this.scaffoldItem();
            if (!this.hold(pl, pillarWith)) { if (this.count(pillarWith) == 0) { this.missing(pillarWith); this.toIdle(); } return; }
            boolean centered = horiz < 0.25;
            // centred: keep the yaw that is already being aimed at - aiming at "wherever the head is now" plus the
            // random aim offset chases itself every tick, the head never comes to rest and the jump passes unclicked
            float yaw = !centered ? (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0)
                    : !Float.isNaN(this.aimTY) && Math.abs(class_3532.method_15393(this.aimTY - pl.method_36454())) < 6.0f ? this.aimTY : pl.method_36454();
            boolean looking = this.rotateTo(pl, yaw, 89.0f, 1.0f);
            if (!centered) { this.keys(true, false, false, true); this.progress(horiz, w, pl, s); return; }
            this.keys(false, ground && looking && settled(pl), false, false);
            if (pl.method_23318() > under.method_10264() + 1.05 && w.method_8320(under).method_45474()) {
                int pr = this.aimAndPlace(w, pl, under, null, pillarWith, yaw, 89.0f, true);
                if (pr != 1 && this.tickNo - this.pillarDbgTick > 4) {
                    this.pillarDbgTick = this.tickNo;
                    class_3965 ch = this.crosshair(pl);
                    dbg("pillar jump no place r=" + pr + " rot " + pl.method_36454() + "/" + pl.method_36455() + " sent " + this.sentYaw + "/" + this.sentPitch
                            + " react " + this.reactWait + " hit " + (ch == null ? "none" : ch.method_17777().method_23854() + " " + ch.method_17780())
                            + " y+" + String.format("%.2f", pl.method_23318() - under.method_10264()) + " vy " + String.format("%.3f", pl.method_18798().field_1351));
                }
                if (pr == 1) {
                    if (buildItem == null) this.scaffoldPlaced.add(under.method_10063());
                    else { this.pillarBuilt++; this.placedTotal++; this.lastProgressMs = System.currentTimeMillis(); }
                }
            }
            this.progress(Math.abs(dy) + horiz, w, pl, s);
            return;
        }
        if (s.move() == SbPath.Move.BRIDGE && !SbWorld.floor(w, s.pos().method_10074())) {
            class_2338 under = s.pos().method_10074();
            if (!this.hold(pl, this.scaffoldItem())) { if (this.count(this.scaffoldItem()) == 0) { this.missing(this.scaffoldItem()); this.toIdle(); } return; }
            SbPlacer.Solution sol = SbPlacer.find(this.mc, under, null, this.scaffoldItem(), pl.method_33571(), this.reach());
            if (sol != null) {
                this.keys(false, false, false, true);
                if (this.aimAndPlace(w, pl, under, null, this.scaffoldItem(), sol.yaw(), sol.pitch(), true) == 1) {
                    this.scaffoldPlaced.add(under.method_10063());
                    this.waitTicks = 2;
                }
                this.noProgress = 0;
                return;
            }
            // creep to the edge (sneaking: can't fall) until the side of the block below is in view
            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            this.rotateTo(pl, yaw, 78.0f);
            this.keys(true, false, false, true);
            this.progress(horiz, w, pl, s);
            return;
        }
        // reached?
        boolean last = this.pathIdx == this.path.size() - 1;
        if (horiz < (last ? 0.2 : 0.6) && Math.abs(dy) < 0.75 && (ground || s.move() == SbPath.Move.DROP && dy < 0.2)) {
            this.nextStep();
            return;
        }
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float diff = Math.abs(class_3532.method_15393(yaw - pl.method_36454()));
        if (this.human.getValue()) {
            // eyes wander a bit while walking: slow, uneven drifts left / right and up / down, a new habit now and then
            if (this.tickNo % 160 == 0) this.walkPitch = 5.0f + this.rnd.nextFloat() * 20.0f;
            float t = this.tickNo;
            float wy = (float) (Math.sin(t * 0.031 + this.walkPhase[0]) * 5.0 + Math.sin(t * 0.087 + this.walkPhase[1]) * 2.0);
            float wp = (float) (Math.sin(t * 0.043 + this.walkPhase[2]) * 6.0);
            this.rotateTo(pl, yaw + wy, Math.max(-10.0f, Math.min(45.0f, this.walkPitch + wp)), 2.5f);
        } else this.rotateTo(pl, yaw, 10.0f);
        boolean jump = s.move() == SbPath.Move.UP && ground && horiz < 1.6 && dy < 0.5 || pl.method_5799() && s.pos().method_10264() >= pl.method_23318() - 0.2;
        boolean sneak = this.edgeDanger(w, s.pos()) && s.move() != SbPath.Move.DROP && s.move() != SbPath.Move.UP
                || last && horiz < 0.7 && ground; // creep onto the exact spot
        boolean sprint = !sneak && this.path.size() - this.pathIdx > 5 && diff < 20 && !pl.method_5799();
        this.keys(diff < 60, jump, sprint, sneak);
        this.progress(horiz + Math.abs(dy), w, pl, s);
    }

    private int walksCut;

    private boolean reachableEarly(class_638 w, class_746 pl, SbPath.Step s) {
        class_2338 hint = this.walkGoalHint;
        if (hint == null || this.arrived == null || (this.tickNo & 3) != 0 || !pl.method_24828()) return false;
        if (s.move() == SbPath.Move.PILLAR || s.move() == SbPath.Move.BRIDGE || !s.breaks().isEmpty()) return false;
        if (!this.status.equals("Walking")) return false; // only walks to a block of the build (not chests, water...)
        Task t = this.tasks.get(hint.method_10063());
        if (t == null || this.failedUntil.containsKey(hint.method_10063())) return false;
        class_243 eye = new class_243(pl.method_23317(), pl.method_23318() + 1.27, pl.method_23321());
        double r = this.reach();
        if (eye.method_1025(class_243.method_24953(hint)) > r * r) return false;
        if (!t.place) return this.visibleFace(w, pl, hint, eye) != null;
        if (t.interact()) return this.interactFace(w, pl, t, eye) != null;
        if (isWater(t.want)) return false;
        class_1792 item = this.requiredItem(t);
        return item instanceof class_1747 bi && this.count(item) > 0 && this.solution(t, bi, eye, r) != null;
    }

    private void nextStep() {
        this.pathIdx++;
        this.bestDist = Double.MAX_VALUE;
        this.noProgress = 0;
    }

    /** A drop deeper than allowed (or lava) right next to the spot: sneak so you can't slip off. */
    private boolean edgeDanger(class_638 w, class_2338 feet) {
        for (class_2350 d : new class_2350[]{class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039}) {
            class_2338 n = feet.method_10093(d);
            if (!SbWorld.passable(w, n) || SbWorld.floor(w, n.method_10074())) continue;
            int depth = 0;
            class_2338 q = n.method_10074();
            while (depth <= this.maxFall.getIntValue() + 1 && SbWorld.passable(w, q) && !SbWorld.water(w.method_8320(q))) { depth++; q = q.method_10074(); }
            if (depth > this.maxFall.getIntValue() || SbWorld.lava(w.method_8320(q))) return true;
        }
        return false;
    }

    /** Stuck detection with escalating ways out. */
    private void progress(double dist, class_638 w, class_746 pl, SbPath.Step s) {
        if (dist < this.bestDist - 0.06) {
            this.bestDist = dist;
            this.noProgress = 0;
            return;
        }
        if (++this.noProgress < 35) return;
        this.noProgress = 0;
        this.bestDist = Double.MAX_VALUE;
        this.stuckLevel++;
        dbg("stuck " + this.stuckLevel + " on " + s.move() + " to " + s.pos().method_23854() + " breaks=" + s.breaks().size() + " at "
                + String.format(java.util.Locale.ROOT, "%.2f %.2f %.2f", pl.method_23317(), pl.method_23318(), pl.method_23321()) + " ground=" + pl.method_24828()
                + " step " + this.pathIdx + "/" + this.path.size() + " status=" + this.status
                + " pose=" + pl.method_18376() + " vy=" + String.format(java.util.Locale.ROOT, "%.3f", pl.method_18798().field_1351)
                + " below=" + SbFix.id(w.method_8320(feet(pl).method_10074()).method_26204()) + " feet=" + SbFix.id(w.method_8320(feet(pl)).method_26204())
                + " head=" + SbFix.id(w.method_8320(feet(pl).method_10084()).method_26204()) + " above=" + SbFix.id(w.method_8320(feet(pl).method_10086(2)).method_26204()));
        switch (this.stuckLevel) {
            case 1 -> { // hop
                this.keys(true, true, false, false);
                this.waitTicks = 0;
            }
            case 2 -> { // back off and plan again
                this.keyBack(8);
                this.replan(w, pl, s.pos());
            }
            case 3, 4 -> { // that spot doesn't work: avoid it
                this.avoid.put(s.pos().method_10063(), System.currentTimeMillis() + 60_000L);
                this.replan(w, pl, s.pos());
            }
            case 5 -> { // climb out of a hole
                if (this.useScaffold.getValue() && this.count(this.scaffoldItem()) > 0 && SbWorld.passable(w, feet(pl).method_10086(2))) {
                    this.path = new ArrayList<>(List.of(new SbPath.Step(feet(pl).method_10084(), SbPath.Move.PILLAR, List.of())));
                    this.pathIdx = 0;
                    this.arrived = null;
                } else this.replan(w, pl, s.pos());
            }
            default -> {
                this.status = "Stuck - trying again";
                if (this.stuckLevel > 8) {
                    this.say("§eStuck at " + s.pos().method_23854() + " - trying another way.");
                    if (this.task != null) this.fail(this.task.pos, 60_000L, "stuck");
                    this.stuckLevel = 0;
                }
                this.toIdle();
                this.waitTicks = 20;
            }
        }
    }

    private int backTicks;

    private void keyBack(int ticks) {
        this.backTicks = ticks;
    }

    private void replan(class_638 w, class_746 pl, class_2338 failedAt) {
        this.mc.field_1761.method_2925();
        Runnable a = this.arrived;
        class_2338 hint = this.walkGoalHint;
        List<SbPath.Step> old = this.path;
        if (old == null || old.isEmpty() || hint == null) { this.toIdle(); return; }
        class_2338 end = old.get(old.size() - 1).pos();
        long ek = end.method_10063();
        if (!this.walk(w, pl, p -> p.method_10063() == ek, hint, a, 6000)) this.toIdle();
    }

    // ================================================================== escape (lava / fire)

    private void startEscape(class_638 w, class_746 pl) {
        this.mc.field_1761.method_2925();
        this.mode = Mode.ESCAPE;
        this.modeTicks = 0;
        this.status = "Escaping danger";
        class_2338 f = feet(pl);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) for (int dy = -1; dy <= 1; dy++)
            if (SbWorld.nearLava(w, f.method_10069(dx, dy, dz))) this.avoid.put(f.method_10069(dx, dy, dz).method_10063(), System.currentTimeMillis() + 120_000L);
    }

    private void tickEscape(class_638 w, class_746 pl) {
        class_2338 f = feet(pl);
        class_2338 best = null;
        double bd = Double.MAX_VALUE;
        for (int dx = -5; dx <= 5; dx++) for (int dz = -5; dz <= 5; dz++) for (int dy = -2; dy <= 2; dy++) {
            class_2338 p = f.method_10069(dx, dy, dz);
            if (!SbWorld.standable(w, p)) continue;
            double d = dx * dx + dz * dz + dy * dy * 2;
            if (d < bd) { bd = d; best = p; }
        }
        boolean safe = !pl.method_5771() && !pl.method_5809() && !SbWorld.nearLava(w, f);
        if (safe && pl.method_24828() || this.modeTicks > 120) { this.toIdle(); return; }
        if (best == null) { this.keys(true, true, false, false); return; }
        double dx = best.method_10263() + 0.5 - pl.method_23317(), dz = best.method_10260() + 0.5 - pl.method_23321();
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        this.rotateTo(pl, yaw, 0.0f);
        this.keys(true, true, true, false);
    }

    // ================================================================== storage

    private String worldKey() {
        String server = this.mc.method_1542() ? "local" : this.mc.method_1558() != null ? this.mc.method_1558().field_3761 : "unknown";
        return server + "|" + (this.mc.field_1687 == null ? "" : this.mc.field_1687.method_27983().method_29177().toString());
    }

    private Path storageFile() {
        return this.mc.field_1697.toPath().resolve("config").resolve("hook-builder-storage.txt");
    }

    private void loadStorage() {
        this.storage.clear();
        this.storageSeen.clear();
        try {
            Path f = this.storageFile();
            if (!Files.exists(f)) return;
            for (String line : Files.readAllLines(f)) {
                int i = line.lastIndexOf('#');
                if (i < 0 || !line.substring(0, i).equals(this.storageKey)) continue;
                this.storage.add(Long.parseLong(line.substring(i + 1).trim()));
            }
        } catch (Throwable ignored) {
        }
    }

    private void saveStorage() {
        try {
            Path f = this.storageFile();
            List<String> keep = new ArrayList<>();
            if (Files.exists(f)) for (String line : Files.readAllLines(f)) {
                int i = line.lastIndexOf('#');
                if (i >= 0 && !line.substring(0, i).equals(this.storageKey)) keep.add(line);
            }
            for (Long l : this.storage) keep.add(this.storageKey + "#" + l);
            Files.createDirectories(f.getParent());
            Files.write(f, keep);
        } catch (Throwable ignored) {
        }
    }

    @EventListener
    public void onKey(KeyEvent e) {
        if (e.mode != 1 || this.mc.field_1755 != null || this.mc.field_1687 == null) return;
        if (this.markKey.getValue() <= 0 || e.key != this.markKey.getValue()) return;
        if (!(this.mc.field_1765 instanceof class_3965 hit) || hit.method_17783() != net.minecraft.class_239.class_240.field_1332) return;
        class_2338 p = hit.method_17777();
        if (!SbWorld.container(this.mc.field_1687.method_8320(p))) {
            this.say("Look at a chest, barrel or shulker box to mark it as storage.");
            return;
        }
        long k = p.method_10063();
        if (this.storage.remove(k)) {
            this.storageSeen.remove(k);
            this.say("Storage unmarked (" + this.storage.size() + " left).");
        } else {
            this.storage.add(k);
            this.say("§aStorage marked§f (" + this.storage.size() + " total). Missing materials are taken from here.");
        }
        this.saveStorage();
    }

    /** Items still needed somewhere in the build (also for blocks whose turn has not come yet: kelp after its water...). */
    private final Set<class_1792> buildItems = new HashSet<>();

    private boolean wantsRestock(class_746 pl) {
        Map<class_1792, Integer> need = this.needs(pl);
        return !need.isEmpty();
    }

    /** Items the work around you needs that you don't carry (only what fits into the inventory). */
    private Map<class_1792, Integer> needs(class_746 pl) {
        Map<class_1792, Integer> want = new LinkedHashMap<>();
        double px = pl.method_23317(), pz = pl.method_23321();
        List<Task> near = new ArrayList<>(this.tasks.values());
        near.sort((a, b) -> Double.compare(sq(a.pos, px, pz), sq(b.pos, px, pz)));
        int types = 0;
        for (Task t : near) {
            if (!t.place) continue;
            class_1792 it = this.requiredItem(t);
            if (!(it instanceof class_1747)) continue;
            if (!want.containsKey(it)) { if (++types > 12) break; }
            want.merge(it, 1, Integer::sum);
        }
        Map<class_1792, Integer> need = new LinkedHashMap<>();
        for (Map.Entry<class_1792, Integer> e : want.entrySet()) {
            int have = this.count(e.getKey());
            if (have == 0 && !this.missingUntil.containsKey(e.getKey())) need.put(e.getKey(), Math.min(e.getValue(), 64 * 4));
        }
        if (this.useScaffold.getValue() && this.count(this.scaffoldItem()) < 12 && !this.missingUntil.containsKey(this.scaffoldItem()))
            need.put(this.scaffoldItem(), 128);
        return need;
    }

    private static double sq(class_2338 p, double x, double z) {
        double dx = p.method_10263() - x, dz = p.method_10260() - z;
        return dx * dx + dz * dz;
    }

    private boolean startRestock(class_638 w, class_746 pl) {
        Map<class_1792, Integer> need = this.needs(pl);
        if (need.isEmpty()) return false;
        // best chest: known to have something we need, else not looked into yet, nearest first
        class_2338 best = null;
        double bestScore = Double.MAX_VALUE;
        for (Long l : this.storage) {
            if (this.visitedThisTrip.contains(l)) continue;
            class_2338 p = class_2338.method_10092(l);
            Map<class_1792, Integer> seen = this.storageSeen.get(l);
            int useful = 0;
            if (seen != null) for (class_1792 it : need.keySet()) useful += seen.getOrDefault(it, 0) > 0 ? 1 : 0;
            if (seen != null && useful == 0) continue;
            double score = Math.sqrt(sq(p, pl.method_23317(), pl.method_23321())) - (seen != null ? useful * 50 : 0);
            if (score < bestScore) { bestScore = score; best = p; }
        }
        if (best == null) {
            this.visitedThisTrip.clear();
            for (class_1792 it : need.keySet()) this.missing(it);
            return false;
        }
        this.wanted = need;
        this.chestPos = best;
        this.status = "Restocking";
        class_2338 chest = best;
        class_243 c = class_243.method_24953(chest);
        if (pl.method_33571().method_1025(c) <= this.reach() * this.reach() && this.visibleFace(w, pl, chest) != null) {
            this.mode = Mode.OPEN_CHEST;
            this.modeTicks = 0;
            return true;
        }
        double reach = this.reach() - 0.5;
        boolean ok = this.walk(w, pl, p -> {
            double ex = p.method_10263() + 0.5 - c.field_1352, ey = p.method_10264() + 1.5 - c.field_1351, ez = p.method_10260() + 0.5 - c.field_1350;
            return ex * ex + ey * ey + ez * ez <= reach * reach && !p.equals(chest) && !p.method_10084().equals(chest);
        }, chest, () -> {
            this.mode = Mode.OPEN_CHEST;
            this.modeTicks = 0;
        }, 12000);
        if (!ok) {
            this.visitedThisTrip.add(chest.method_10063());
            this.say("§eCan't reach storage at " + chest.method_23854());
        }
        return ok;
    }

    private void tickOpenChest(class_638 w, class_746 pl) {
        class_1703 h = pl.field_7512;
        if (h instanceof class_1707 || h instanceof class_1733) {
            this.mode = Mode.TRANSFER;
            this.modeTicks = 0;
            this.waitTicks = 4 + (this.human.getValue() ? this.rnd.nextInt(5) : 0);
            return;
        }
        if (this.modeTicks > 80 || !SbWorld.container(w.method_8320(this.chestPos))) {
            this.visitedThisTrip.add(this.chestPos.method_10063());
            this.toIdle();
            return;
        }
        this.keys(false, false, false, false);
        if (pl.method_5715()) return; // stand up first, sneaking would not open it
        class_3965 face = this.visibleFace(w, pl, this.chestPos);
        if (face == null) { this.visitedThisTrip.add(this.chestPos.method_10063()); this.toIdle(); return; }
        float[] rot = SbPlacer.lookAt(pl.method_33571(), face.method_17784());
        if (!this.rotateTo(pl, rot[0], rot[1], 0.8f) || !settled(pl) || !this.readyForInventory(pl)) return;
        class_3965 hit = this.crosshair(pl);
        if (hit == null || !hit.method_17777().equals(this.chestPos)) { this.rotateTo(pl, rot[0], rot[1], 0.2f); return; }
        if ((this.modeTicks % 20 == 5 || this.modeTicks < 5) && !this.dupRotRisk(pl)) {
            this.modeTicks = 6;
            this.mc.field_1761.method_2896(pl, class_1268.field_5808, hit);
            this.clickedBlock(); // Grim counts every block click as a place
            pl.method_6104(class_1268.field_5808);
        }
    }

    private void tickTransfer(class_638 w, class_746 pl) {
        class_1703 h = pl.field_7512;
        if (!(h instanceof class_1707 || h instanceof class_1733)) { this.restoreScreen(); this.toIdle(); return; }
        int size = h instanceof class_1707 g ? g.method_17388() * 9 : 27;
        // remember what is in there
        Map<class_1792, Integer> seen = new HashMap<>();
        for (int i = 0; i < size; i++) {
            class_1799 st = h.method_7611(i).method_7677();
            if (!st.method_7960()) seen.merge(st.method_7909(), st.method_7947(), Integer::sum);
        }
        this.storageSeen.put(this.chestPos.method_10063(), seen);
        int free = this.freeSlots(pl);
        // unpacking a shulker box: one slot has to stay free, or the box can't be picked up again afterwards
        boolean unpacking = this.shulkerPos != null && this.shulkerPos.equals(this.chestPos);
        int keepFree = unpacking ? 1 : 0;
        // take what we need
        for (int i = 0; i < size; i++) {
            class_1799 st = h.method_7611(i).method_7677();
            if (st.method_7960()) continue;
            Integer n = this.wanted.get(st.method_7909());
            if (n == null || n <= 0 || free <= keepFree) continue;
            this.mc.field_1761.method_2906(h.field_7763, i, 0, class_1713.field_7794, (class_1657) pl);
            this.wanted.put(st.method_7909(), n - st.method_7947());
            this.waitTicks = this.human.getValue() ? 2 + this.rnd.nextInt(3) : 1;
            return;
        }
        // drop off junk when the inventory is nearly full
        if (free < 4) {
            Set<class_1792> useful = new HashSet<>();
            for (Task t : this.tasks.values()) if (t.place && this.requiredItem(t) != null) useful.add(this.requiredItem(t));
            useful.addAll(this.buildItems);
            useful.add(class_1802.field_8705);
            useful.add(itemById("bucket"));
            useful.add(this.scaffoldItem());
            for (int i = size; i < size + 36 && i < h.field_7761.size(); i++) {
                class_1799 st = h.method_7611(i).method_7677();
                if (st.method_7960() || useful.contains(st.method_7909()) || isTool(st) || isShulker(st) || st.method_7909() == class_1802.field_8288) continue;
                this.mc.field_1761.method_2906(h.field_7763, i, 0, class_1713.field_7794, (class_1657) pl);
                this.waitTicks = this.human.getValue() ? 2 + this.rnd.nextInt(3) : 1;
                return;
            }
        }
        // the inventory is full of useful things and the chest still has what we came for: put back stacks we
        // carry more of than the rest of the build needs (else we'd open this chest over and over for nothing)
        if (!unpacking && this.freeSlots(pl) == 0 && this.chestHasWanted(h, size)) {
            Map<class_1792, Integer> left = new HashMap<>();
            for (Task t : this.tasks.values()) {
                class_1792 it = t.place ? this.requiredItem(t) : null;
                if (it != null) left.merge(it, 1, Integer::sum);
            }
            left.merge(this.scaffoldItem(), 16, Integer::sum);
            class_1792 held = pl.method_6047().method_7909();
            for (int i = size; i < size + 36 && i < h.field_7761.size(); i++) {
                class_1799 st = h.method_7611(i).method_7677();
                if (st.method_7960() || isTool(st) || isShulker(st) || st.method_7909() == held || this.wanted.getOrDefault(st.method_7909(), 0) > 0) continue;
                if (this.count(st.method_7909()) - st.method_7947() < left.getOrDefault(st.method_7909(), 0)) continue;
                dbg("restock: inventory full, putting back spare " + st.method_7947() + "x " + st.method_7909());
                this.mc.field_1761.method_2906(h.field_7763, i, 0, class_1713.field_7794, (class_1657) pl);
                this.waitTicks = this.human.getValue() ? 2 + this.rnd.nextInt(3) : 1;
                return;
            }
        }
        // still no free slot for the box: put the smallest stack of what we carry most of into it
        if (unpacking && this.freeSlots(pl) == 0) {
            Map<class_1792, Integer> have = new HashMap<>();
            for (int i = 0; i < 36; i++) {
                class_1799 st = pl.method_31548().method_5438(i);
                if (!st.method_7960() && !isTool(st) && !isShulker(st)) have.merge(st.method_7909(), st.method_7947(), Integer::sum);
            }
            class_1792 most = null;
            for (Map.Entry<class_1792, Integer> e : have.entrySet()) if (most == null || e.getValue() > have.get(most)) most = e.getKey();
            int pick = -1, pickN = Integer.MAX_VALUE;
            class_1792 held = pl.method_6047().method_7909();
            for (int i = size; i < size + 36 && i < h.field_7761.size(); i++) {
                class_1799 st = h.method_7611(i).method_7677();
                if (st.method_7960() || st.method_7909() != most) continue;
                int n = st.method_7947() + (st.method_7909() == held ? 1000 : 0);
                if (n < pickN) { pickN = n; pick = i; }
            }
            if (pick >= 0 && this.shulkerRoomTries++ < 3) {
                dbg("shulker: inventory full, putting " + most + " back so the box fits");
                this.mc.field_1761.method_2906(h.field_7763, pick, 0, class_1713.field_7794, (class_1657) pl);
                this.waitTicks = this.human.getValue() ? 2 + this.rnd.nextInt(3) : 1;
                return;
            }
        }
        this.shulkerRoomTries = 0;
        // done here
        pl.method_7346();
        this.restoreScreen();
        this.visitedThisTrip.add(this.chestPos.method_10063());
        boolean still = false;
        for (Map.Entry<class_1792, Integer> e : this.wanted.entrySet()) if (e.getValue() > 0 && this.count(e.getKey()) == 0) still = true;
        if (!still) this.visitedThisTrip.clear();
        this.toIdle();
        this.waitTicks = 4;
    }

    // ================================================================== inventory

    private int count(class_1792 it) {
        class_1661 inv = this.mc.field_1724.method_31548();
        int n = 0;
        for (int i = 0; i < 36; i++) {
            class_1799 s = inv.method_5438(i);
            if (s.method_7909() == it) n += s.method_7947();
        }
        class_1799 off = this.mc.field_1724.method_6079();
        if (off.method_7909() == it) n += off.method_7947();
        return n;
    }

    private boolean chestHasWanted(class_1703 h, int size) {
        for (int i = 0; i < size; i++) {
            class_1799 st = h.method_7611(i).method_7677();
            if (!st.method_7960() && this.wanted.getOrDefault(st.method_7909(), 0) > 0) return true;
        }
        return false;
    }

    private int freeSlots(class_746 pl) {
        int n = 0;
        for (int i = 0; i < 36; i++) if (pl.method_31548().method_5438(i).method_7960()) n++;
        return n;
    }

    static boolean isTool(class_1799 s) {
        if (s.method_7960()) return false;
        // anything that wears out (hoes, swords, shears, flint and steel...): never junk
        if (s.method_7963()) return true;
        return s.method_7924(class_2246.field_10340.method_9564()) > 1.5f || s.method_7924(class_2246.field_10566.method_9564()) > 1.5f
                || s.method_7924(class_2246.field_10431.method_9564()) > 1.5f;
    }

    private int swapCooldown;

    /** Puts the item in the hand (moving it into the hotbar if needed). True when it is held. */
    private boolean hold(class_746 pl, class_1792 it) {
        class_1661 inv = pl.method_31548();
        if (inv.method_5438(inv.method_67532()).method_7909() == it) return true;
        for (int i = 0; i < 9; i++) if (inv.method_5438(i).method_7909() == it) { inv.method_61496(i); return true; }
        if (this.swapCooldown > 0) { this.swapCooldown--; return false; }
        if (pl.field_7512 != pl.field_7498) return false;
        for (int i = 9; i < 36; i++) {
            if (inv.method_5438(i).method_7909() != it) continue;
            if (!this.readyForInventory(pl)) return false;
            int hb = this.freeHotbar(inv);
            this.inventorySwap(pl, i, hb);
            inv.method_61496(hb);
            this.swapCooldown = this.human.getValue() ? 3 + this.rnd.nextInt(3) : 1;
            return false;
        }
        return false;
    }

    private int hotbarRoll = 8;

    private int freeHotbar(class_1661 inv) {
        for (int i = 8; i >= 0; i--) if (inv.method_5438(i).method_7960()) return i;
        for (int n = 0; n < 9; n++) {
            this.hotbarRoll = this.hotbarRoll <= 1 ? 8 : this.hotbarRoll - 1;
            class_1799 s = inv.method_5438(this.hotbarRoll);
            if (!isTool(s) && s.method_7909() != this.scaffoldItem() && s.method_7909() != class_1802.field_8288) return this.hotbarRoll;
        }
        return 8;
    }

    private void holdTool(class_746 pl, class_2680 st, class_2338 p) {
        class_1661 inv = pl.method_31548();
        int best = -1;
        float bestSpeed = 1.0f;
        for (int i = 0; i < 36; i++) {
            class_1799 s = inv.method_5438(i);
            if (s.method_7960() || this.nearlyBroken(s)) continue;
            if (st.method_29291() && !s.method_7951(st)) continue;
            float sp = s.method_7924(st);
            if (sp > bestSpeed + 0.01f || sp > bestSpeed - 0.01f && best >= 9 && i < 9) { bestSpeed = sp; best = i; }
        }
        if (best < 0 && this.nearlyBroken(inv.method_5438(inv.method_67532()))) {
            // only the almost broken tool would do: rather use the hand than break it
            for (int i = 0; i < 9; i++) if (!isTool(inv.method_5438(i))) { inv.method_61496(i); return; }
            return;
        }
        if (best < 0 || best == inv.method_67532()) return;
        if (best < 9) { inv.method_61496(best); return; }
        if (this.swapCooldown > 0) { this.swapCooldown--; return; }
        if (pl.field_7512 != pl.field_7498) return;
        if (!this.readyForInventory(pl)) return;
        int hb = 0;
        for (int i = 0; i < 9; i++) if (isTool(inv.method_5438(i)) || inv.method_5438(i).method_7960()) { hb = i; break; }
        this.inventorySwap(pl, best, hb);
        inv.method_61496(hb);
        this.swapCooldown = 3;
    }

    // ================================================================== input

    private void keys(boolean forward, boolean jump, boolean sprint, boolean sneak) {
        var o = this.mc.field_1690;
        if (this.forceSneakTicks > 0) sneak = true;
        if (this.freezeTicks > 0) { forward = false; jump = false; sprint = false; sneak = false; }
        if (this.backTicks > 0) {
            this.backTicks--;
            o.field_1894.method_23481(false);
            o.field_1881.method_23481(true);
            o.field_1903.method_23481(false);
            o.field_1867.method_23481(false);
            o.field_1832.method_23481(false);
            return;
        }
        o.field_1894.method_23481(forward);
        o.field_1881.method_23481(false);
        o.field_1903.method_23481(jump);
        o.field_1867.method_23481(sprint);
        o.field_1832.method_23481(sneak);
    }

    private void releaseKeys() {
        try {
            var o = this.mc.field_1690;
            o.field_1894.method_23481(false);
            o.field_1881.method_23481(false);
            o.field_1903.method_23481(false);
            o.field_1867.method_23481(false);
            o.field_1832.method_23481(false);
            o.field_1913.method_23481(false);
            o.field_1849.method_23481(false);
        } catch (Throwable ignored) {
        }
    }

    private boolean rotateTo(class_746 pl, float yaw, float pitch) {
        return this.rotateTo(pl, yaw, pitch, 1.2f);
    }

    /**
     * Turns the head towards a rotation like a mouse would: only in whole mouse steps of the current sensitivity,
     * never more than a fast flick per tick, easing out near the target (human mode). True once within {@code tol}.
     */
    private boolean rotateTo(class_746 pl, float yaw, float pitch, float tol) {
        pitch = Math.max(-90.0f, Math.min(90.0f, pitch));
        float cy = pl.method_36454(), cp = pl.method_36455();
        float g = this.mouseStep();
        boolean human = this.human.getValue();
        // every new thing to look at gets its own "hand": speed, curve, how far off it lands, maybe an overshoot.
        // A target that only drifts (walking) keeps the current style so the head does not twitch.
        if (Float.isNaN(this.aimTY) || Math.abs(class_3532.method_15393(yaw - this.aimTY)) > 6.0f || Math.abs(pitch - this.aimTP) > 6.0f) {
            float far = (float) Math.hypot(class_3532.method_15393(yaw - cy), pitch - cp);
            this.aimSpeed = 0.34f + this.rnd.nextFloat() * 0.22f;
            this.aimCap = 26.0f + this.rnd.nextFloat() * 12.0f;
            this.aimAccel = 0.5f + this.rnd.nextFloat() * 0.3f;
            this.aimLag = 0.6f + this.rnd.nextFloat() * 0.4f;
            this.aimCurve = (this.rnd.nextFloat() - 0.5f) * 0.3f;
            // never exactly the planned spot: somewhere around it (the click is checked with the real crosshair)
            float om = !human || this.aimPrecise ? 0.0f : tol <= 0.6f ? 0.55f : Math.min(1.6f, tol * 1.2f);
            this.aimOffY = (this.rnd.nextFloat() * 2.0f - 1.0f) * om;
            this.aimOffP = (this.rnd.nextFloat() * 2.0f - 1.0f) * om * 0.8f;
            this.aimPrecise = false;
            this.aimOvershoot = human && far > 20.0f && this.rnd.nextFloat() < 0.35f;
            this.aimOSY = class_3532.method_15393(yaw - cy) * (0.03f + this.rnd.nextFloat() * 0.05f);
            this.aimOSP = (pitch - cp) * (0.02f + this.rnd.nextFloat() * 0.04f);
            this.aimHesitate = human && this.rnd.nextFloat() < 0.25f ? this.rnd.nextInt(3) : 0; // reaction time
        }
        this.aimTY = yaw;
        this.aimTP = pitch;
        float gy = yaw + this.aimOffY, gp = Math.max(-90.0f, Math.min(90.0f, pitch + this.aimOffP));
        if (this.aimOvershoot) { gy += this.aimOSY; gp = Math.max(-90.0f, Math.min(90.0f, gp + this.aimOSP)); }
        float dy = class_3532.method_15393(gy - cy), dp = gp - cp;
        float dist = (float) Math.sqrt(dy * dy + dp * dp);
        float done = Math.max(tol, g * 0.75f);
        if (this.aimOvershoot && dist < 1.5f) { this.aimOvershoot = false; return false; } // past it: correct back
        if (!this.aimOvershoot && dist < done) return true;
        float my, mp;
        if (human) {
            if (this.aimHesitate > 0) { this.aimHesitate--; this.aimVel *= 0.3f; return false; }
            // a smooth hand: speeds up over a tick or two, glides, and slows down softly before the target
            // (no instant full-speed jumps, no random jitter from tick to tick)
            float f = this.aimSpeed * (0.92f + this.rnd.nextFloat() * 0.16f);
            float want = Math.min(this.aimCap, Math.max(0.9f + this.rnd.nextFloat() * 0.5f, dist * f));
            float vel = want > this.aimVel ? this.aimVel + (want - this.aimVel) * this.aimAccel : want;
            float step = Math.min(dist, Math.max(0.6f, vel));
            this.aimVel = step;
            float k = step / dist;
            my = dy * k;
            mp = dp * k * (dist < 5.0f ? 1.0f : this.aimLag); // the wrist moves sideways first, up / down follows
            if (dist > 6.0f) mp += Math.abs(dy * k) * this.aimCurve; // an arc, not a ruler-straight line
            if (dist > 3.0f) {
                // hand tremor as a slow wave, not noise
                my += (float) Math.sin(this.tickNo * 0.83 + this.walkPhase[0]) * g * 1.5f;
                mp += (float) Math.sin(this.tickNo * 0.61 + this.walkPhase[1]) * g * 1.0f;
            }
        } else {
            float k = Math.min(dist, 55.0f) / dist;
            my = dy * k;
            mp = dp * k;
            if (this.antiCheat.getValue() && dist > 3.0f) { my += (this.rnd.nextInt(5) - 2) * g; mp += (this.rnd.nextInt(3) - 1) * g; }
        }
        // only whole mouse steps of the player's sensitivity, like real mouse input
        my = Math.round(my / g) * g;
        mp = Math.round(mp / g) * g;
        if (my == 0.0f && mp == 0.0f) { this.aimOvershoot = false; return true; } // closer than one mouse step
        // never the very same first turn after two places in a row (Grim DuplicateRotPlace)
        if (this.afterPlace && Math.abs(my) > 2.0f && Math.abs(Math.abs(my) - this.lastAfterPlace) < 1.0E-3f) my += Math.signum(my) * g;
        if (this.afterPlace) { this.lastAfterPlace = Math.abs(my); this.afterPlace = false; }
        this.lastTurnAbs = Math.abs(my);
        pl.method_36456(cy + my);
        pl.method_36457(Math.max(-90.0f, Math.min(90.0f, cp + mp)));
        float ry = class_3532.method_15393(gy - pl.method_36454()), rp = gp - pl.method_36455();
        return !this.aimOvershoot && Math.sqrt(ry * ry + rp * rp) < done;
    }

    private float aimTY = Float.NaN, aimTP, aimOffY, aimOffP, aimSpeed, aimCap, aimLag, aimCurve, aimOSY, aimOSP;
    private boolean aimOvershoot, aimPrecise, afterPlace;

    /** After any block click: the next turn must not repeat the one after the last click (Grim DuplicateRotPlace). */
    private void clickedBlock() {
        this.lastBeforePlace = this.lastTurnAbs;
        this.afterPlace = true;
    }

    private boolean gRotated;
    private float gDeltaX, gLastPlaced = Float.NaN;

    /**
     * Grim DuplicateRotPlace: the last turn the server got before a block click must not be exactly the one before
     * the previous click. If it would be, turn one mouse step more first and click a tick later.
     */
    private boolean dupRotRisk(class_746 pl) {
        if (!this.gRotated || this.gDeltaX <= 2.0f || Math.abs(this.gDeltaX - this.gLastPlaced) >= 1.0E-3f) return false;
        float g = this.mouseStep();
        pl.method_36456(pl.method_36454() + (this.rnd.nextBoolean() ? g : -g));
        return true;
    }
    private float lastTurnAbs, lastAfterPlace = Float.NaN, lastBeforePlace = Float.NaN, aimAccel = 0.6f, aimVel;
    private int aimHesitate, reactWait = -1;
    private long pillarDbgTick;
    private final float[] walkPhase = {(float) (Math.random() * 6.3), (float) (Math.random() * 6.3), (float) (Math.random() * 6.3)};
    private float walkPitch = 14.0f;

    // ================================================================== render

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (!this.render.getValue() || this.mc.field_1687 == null) return;
        BaseFinder.Draw d = new BaseFinder.Draw();
        Color accent = new Color(90, 200, 255, 230);
        if (this.task != null && this.mode == Mode.PLACE) d.block(this.task.pos, accent, 40);
        if (this.task != null && this.mode == Mode.INTERACT) d.block(this.task.pos, new Color(200, 140, 255, 230), 40);
        if (this.breakPos != null && this.mode == Mode.BREAK) d.block(this.breakPos, new Color(255, 110, 90, 230), 40);
        if (this.path != null) {
            for (int i = Math.max(0, this.pathIdx); i < this.path.size(); i++) {
                class_2338 p = this.path.get(i).pos();
                d.box(p.method_10263() + 0.35, p.method_10264() + 0.02, p.method_10260() + 0.35, p.method_10263() + 0.65, p.method_10264() + 0.08, p.method_10260() + 0.65,
                        new Color(120, 255, 170, 200), 70);
            }
        }
        Color store = new Color(255, 200, 80, 220);
        for (Long l : this.storage) d.block(class_2338.method_10092(l), store, 22);
        d.render(event, true);
    }
}
