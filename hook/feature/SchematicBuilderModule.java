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

    static SchematicBuilderModule instance;
    private final Random rnd = new Random();

    // ------------------------------------------------------------------ schematic + progress
    private Object placement;
    private String signature = "";
    private int[] box;
    private int totalBlocks;
    private String schematicName = "";
    private final Map<Long, int[]> chunkStats = new HashMap<>(); // chunk -> {total, done, unbuildable}
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
    private final ArrayDeque<long[]> doneLog = new ArrayDeque<>(); // {time ms, blocks}
    private int lastDone = -1;

    private record Task(class_2338 pos, class_2680 want, boolean place) {
    }

    // ------------------------------------------------------------------ state machine
    private enum Mode { IDLE, PLACE, BREAK, WALK, OPEN_CHEST, TRANSFER, ESCAPE }

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
        this.addSettings(this.scaffold, this.speed, this.human, this.antiCheat, this.breakWrong, this.clearArea, this.breakObstacles, this.useScaffold,
                this.cleanup, this.maxFall, this.useStorage, this.markKey, clear, this.pauseHealth, this.render);
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
        return this.mode + " / " + this.status + " tasks=" + this.tasks.size() + " scaffold=" + this.scaffoldPlaced.size() + r;
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
        int total = Math.max(1, (this.totalBlocks > 0 ? this.totalBlocks : this.scannedTotal()) - unb);
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
        this.releaseKeys();
        if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB wrongPlaced=" + this.wrongPlaced);
        if (this.mc.field_1761 != null) this.mc.field_1761.method_2925();
        super.onDisable();
    }

    private void resetAll() {
        this.placement = null;
        this.placedByMe.clear();
        this.verify.clear();
        this.verifyWant.clear();
        this.wrongPlaced = 0;
        SbAcTest.reset();
        this.signature = "";
        this.chunkStats.clear();
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

        // a screen the builder did not open (inventory, chat, menu): wait
        boolean ownScreen = this.mode == Mode.TRANSFER || this.mode == Mode.OPEN_CHEST;
        if (this.mc.field_1755 instanceof net.minecraft.class_465 && !ownScreen && this.tickNo - this.lastClickTick <= 20) {
            // a click opened a block's menu (shouldn't happen, we sneak): close it again right away
            if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB closed a menu our click opened: " + this.mc.field_1755.getClass().getSimpleName());
            pl.method_7346();
        }
        if (this.mc.field_1755 != null && !ownScreen) {
            if (!this.pauseLogged && System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB paused by screen " + this.mc.field_1755.getClass().getName());
            this.pauseLogged = true;
            this.releaseKeys();
            this.status = "Paused (menu open)";
            return;
        }
        // safety first
        if (this.pauseHealth.getValue() > 0 && pl.method_6032() < this.pauseHealth.getValue() && this.mode != Mode.ESCAPE) {
            this.releaseKeys();
            this.status = "Paused (low health)";
            return;
        }
        if ((pl.method_5771() || pl.method_5809() || SbWorld.nearLava(w, feet(pl))) && this.mode != Mode.ESCAPE) this.startEscape(w, pl);

        if (this.waitTicks > 0) {
            this.waitTicks--;
            this.keys(false, false, false, false);
            return;
        }
        this.modeTicks++;
        switch (this.mode) {
            case IDLE -> this.decide(w, pl);
            case PLACE -> this.tickPlace(w, pl);
            case BREAK -> this.tickBreak(w, pl);
            case WALK -> this.tickWalk(w, pl);
            case OPEN_CHEST -> this.tickOpenChest(w, pl);
            case TRANSFER -> this.tickTransfer(w, pl);
            case ESCAPE -> this.tickEscape(w, pl);
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
        class_2338.class_2339 m = new class_2338.class_2339();
        for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) for (int y = y0; y <= y1; y++) {
            m.method_10103(x, y, z);
            class_2680 want = sw.method_8320(m);
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
            if (SbWorld.matches(want, have)) { done++; continue; }
            class_1792 item = want.method_26204().method_8389();
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

        // restock when the scaffold block runs low or the nearby work needs something we don't carry
        if (this.useStorage.getValue() && !this.storage.isEmpty() && this.wantsRestock(pl)) {
            if (this.startRestock(w, pl)) return;
        }

        List<Task> cands = this.candidates(pl, 70);
        if (cands.isEmpty()) {
            // everything left needs material we don't have
            for (Task t : this.tasks.values()) {
                if (!t.place || this.failedUntil.containsKey(t.pos.method_10063())) continue;
                class_1792 it = t.want.method_26204().method_8389();
                if (this.count(it) == 0) {
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
            class_1792 item = t.want.method_26204().method_8389();
            if (this.missingUntil.containsKey(item) || this.count(item) == 0) continue;
            if (eye.method_1025(class_243.method_24953(t.pos)) > (this.reach() + 0.8) * (this.reach() + 0.8)) continue;
            if (sims++ > 30) break;
            if (SbPlacer.find(this.mc, t.pos, t.want, (class_1747) item, eye, this.reach()) != null) {
                this.task = t;
                this.mode = Mode.PLACE;
                this.modeTicks = 0;
                this.status = "Building";
                return;
            }
        }
        // nothing reachable from here: walk to the best task
        int tries = 0;
        for (Task t : cands) {
            if (t.place && !this.supported(w, t.pos)) continue; // nothing to build it against yet: comes later
            if (tries++ >= 5) break;
            if (t.place) {
                class_1792 item = t.want.method_26204().method_8389();
                if (this.missingUntil.containsKey(item)) continue;
                if (this.count(item) == 0) {
                    if (this.useStorage.getValue() && !this.storage.isEmpty() && this.startRestock(w, pl)) return;
                    this.missing(item);
                    continue;
                }
            }
            // tried before and still impossible: put a helper block next to it (top slabs, upside-down stairs...)
            if (t.place && this.failCount.getOrDefault(t.pos.method_10063(), 0) > 2 && this.tryHelper(w, pl, t)) return;
            if (this.walkNear(w, pl, t.pos, t)) return;
            if (t.place && this.tryHelper(w, pl, t)) return;
            this.fail(t.pos, 20_000L, "nowalk");
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
        for (Task t : this.tasks.values()) {
            if (this.failedUntil.containsKey(t.pos.method_10063())) continue;
            // only work we can do right now (material in the inventory); the rest waits for a restock
            if (t.place && !carry.computeIfAbsent(t.want.method_26204().method_8389(), it -> this.count(it) > 0)) continue;
            all.add(t);
            if (t.place) minY = Math.min(minY, t.pos.method_10264());
        }
        final int floorY = minY;
        final float yaw = pl.method_36454();
        Map<Task, Double> sc = new HashMap<>();
        for (Task t : all) sc.put(t, this.taskScore(t, px, py, pz, floorY, yaw));
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
            class_2338 best = null;
            double d2 = Double.MAX_VALUE;
            for (Long l : this.scaffoldPlaced) {
                if (this.failedUntil.containsKey(l)) continue;
                class_2338 p = class_2338.method_10092(l);
                if (this.inBox(p) && !SbLitematica.target(sw, p).method_26215()) continue; // part of the build now
                double d = pl.method_33571().method_1025(class_243.method_24953(p));
                if (d < d2) { d2 = d; best = p; }
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
            this.status = "Finished";
            if (!this.announcedDone) {
                this.announcedDone = true;
                this.say("§a" + this.schematicName + " is finished!");
            }
        } else if (this.tasks.isEmpty()) {
            this.status = "Scanning";
        } else {
            // everything left is on cool-down: retry the one that is closest instead of standing around
            this.status = "Retrying";
            Task best = null;
            double rd = Double.MAX_VALUE;
            for (Task t : this.tasks.values()) {
                if (t.place && this.count(t.want.method_26204().method_8389()) == 0) continue;
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
            if (this.stuckRetries >= 40) {
                // nothing worked for a minute: what is left can't be reached or placed (it hangs on skipped
                // blocks...). Give it up so the build finishes instead of standing here forever.
                int n = 0;
                for (Task t : new ArrayList<>(this.tasks.values())) {
                    this.skipped.add(t.pos.method_10063());
                    this.tasks.remove(t.pos.method_10063());
                    this.touched(t.pos);
                    n++;
                }
                this.stuckRetries = 0;
                this.escape = false;
                this.say("§eGave up on " + n + " block(s) that can't be reached or placed from anywhere - place them yourself.");
                if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB gave up on " + n);
            }
            this.waitTicks = 30;
        }
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
        if (SbWorld.matches(t.want, have)) { this.finishedBlock(t); return; }
        if (!have.method_26215() && !have.method_45474()) { this.toIdle(); return; }
        class_1792 item = t.want.method_26204().method_8389();
        if (!this.hold(pl, item)) {
            if (this.count(item) == 0) { this.missing(item); this.toIdle(); }
            return;
        }
        this.keys(false, false, false, true); // sneak while placing: no opening chests / doors by accident
        if (!pl.method_5715() && this.modeTicks < 6) return;
        SbPlacer.Solution s = SbPlacer.find(this.mc, t.pos, t.want, (class_1747) item, pl.method_33571(), this.reach());
        if (s == null) {
            if (this.modeTicks > 3) { this.fail(t.pos, 8_000L, "nosolution"); this.toIdle(); }
            return;
        }
        int r;
        if (s.score() >= 400.0) {
            // a state that needs the head turned away from the clicked spot (Anticheat Safe is off)
            if (!this.rotateTo(pl, s.yaw(), s.pitch(), 0.4f) || this.modeTicks < this.placeDelay()) return;
            if (!pl.method_5715() && SbWorld.usable(w, s.hit().method_17777())) { this.forceSneakTicks = 10; return; }
            r = this.mc.field_1761.method_2896(pl, class_1268.field_5808, s.hit()).method_23665() ? 1 : -1;
            this.lastClickTick = this.tickNo;
            if (r == 1) pl.method_6104(class_1268.field_5808);
        } else r = this.aimAndPlace(w, pl, t.pos, t.want, (class_1747) item, s.yaw(), s.pitch(), this.modeTicks >= this.placeDelay());
        if (r == 0) return;
        if (r < 0) {
            if (++this.aimMiss > 6) { this.aimMiss = 0; this.fail(t.pos, 8_000L, "aim"); this.toIdle(); }
            return;
        }
        this.aimMiss = 0;
        {
            this.placedSincePause++;
            this.stuckRetries = 0;
            this.escape = false;
            this.placedByMe.add(t.pos.method_10063());
            if (!this.helperPos.contains(t.pos.method_10063())) { this.verify.put(t.pos.method_10063(), this.tickNo); this.verifyWant.put(t.pos.method_10063(), t.want); }
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
            if (want == null || SbWorld.matches(want, have)) continue;
            this.wrongPlaced++;
            this.touched(p);
            if (System.getProperty("hook.carttest") != null) System.out.println("CARTTEST SB wrongstate " + p.method_23854() + " have " + have + " want " + want);
        }
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

    /** Self-test only: what an anticheat would think of every packet. */
    @EventListener
    public void onPacketSend(hook.event.events.PacketSendEvent e) {
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
        if (st == null || want != null && !SbWorld.matches(want, st)) {
            dbg("aim: target " + target.method_23854() + " crosshair " + hit.method_17777().method_23854() + " " + hit.method_17780() + " @" + hit.method_17784()
                    + " gives " + st + " want " + want + " rot " + pl.method_36454() + "/" + pl.method_36455() + " aim " + yaw + "/" + pitch + " eye " + pl.method_33571());
            this.aimPrecise = true; // the next try aims at the exact planned spot
            this.aimTY = Float.NaN;
            return -1;
        }
        var res = this.mc.field_1761.method_2896(pl, class_1268.field_5808, hit);
        if (!res.method_23665()) {
            dbg("aim: click refused " + res + " target " + target.method_23854() + " crosshair " + hit.method_17777().method_23854() + " " + hit.method_17780()
                    + " holding " + pl.method_6047() + " feet " + pfeet());
            return -1;
        }
        pl.method_6104(class_1268.field_5808);
        this.lastClickTick = this.tickNo;
        return 1;
    }

    /**
     * Mines a block like holding the attack button: look at it first, then only ever hit what the crosshair is on.
     * False while still turning.
     */
    private boolean aimAndMine(class_638 w, class_746 pl, class_2338 p, class_3965 face) {
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
        if (v.field_1352 * v.field_1352 + v.field_1350 * v.field_1350 > 1.0E-4 || pl.method_5624() || pl.method_5715() || !pl.method_24828()) {
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
        class_1792 item = t.want.method_26204().method_8389();
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
                    if (st != null && SbWorld.matches(t.want, st)) { ok = true; break; }
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

    private void finishedBlock(Task t) {
        this.stuckRetries = 0;
        this.escape = false;
        this.tasks.remove(t.pos.method_10063());
        this.failCount.remove(t.pos.method_10063());
        this.touched(t.pos);
        this.toIdle();
    }

    // ================================================================== breaking

    private void startBreak(class_2338 p, Task t) {
        this.task = t;
        this.breakPos = p;
        this.mode = Mode.BREAK;
        this.modeTicks = 0;
        this.status = t != null && t.place ? "Fixing wrong block" : "Breaking";
    }

    /** A face of the block the player can really see from here (null = hidden). */
    private class_3965 visibleFace(class_638 w, class_746 pl, class_2338 p) {
        return this.visibleFace(w, pl, p, pl.method_33571());
    }

    private class_3965 visibleFace(class_638 w, class_746 pl, class_2338 p, class_243 eye) {
        double[][] pts = {{0.5, 0.5, 0.5}, {0.5, 0.9, 0.5}, {0.5, 0.1, 0.5}, {0.1, 0.5, 0.5}, {0.9, 0.5, 0.5}, {0.5, 0.5, 0.1}, {0.5, 0.5, 0.9}};
        for (double[] o : pts) {
            class_243 to = new class_243(p.method_10263() + o[0], p.method_10264() + o[1], p.method_10260() + o[2]);
            if (eye.method_1025(to) > this.reach() * this.reach()) continue;
            class_3965 r = SbPlacer.ray(w, pl, eye, to);
            if (r != null && r.method_17777().equals(p)) return r;
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
        this.aimAndMine(w, pl, p, face);
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
                return want.method_26215() || !SbWorld.matches(want, this.mc.field_1687.method_8320(p));
            }
            return true;
        };
        r.mayScaffold = p -> sw == null || !this.inBox(p) || SbLitematica.target(sw, p).method_26215()
                || SbLitematica.target(sw, p).method_26204() == this.scaffoldItem().method_7711();
        return r;
    }

    /** Walk to a spot from which the task's block is within reach. */
    private boolean walkNear(class_638 w, class_746 pl, class_2338 target, Task t) {
        double reach = this.reach() - 0.3;
        class_243 c = class_243.method_24953(target);
        long tk = target.method_10063();
        class_1792 item = t != null && t.place ? t.want.method_26204().method_8389() : null;
        return this.walk(w, pl, p -> {
            if (p.method_10063() == tk || p.method_10084().method_10063() == tk) return false;
            double ex = p.method_10263() + 0.5 - c.field_1352, ey = p.method_10264() + 1.27 - c.field_1351, ez = p.method_10260() + 0.5 - c.field_1350;
            double d2 = ex * ex + ey * ey + ez * ez;
            if (d2 > reach * reach || d2 < 0.3) return false;
            // only spots from which the block can really be placed / hit (sneaking eye height)
            class_243 eye = new class_243(p.method_10263() + 0.5, p.method_10264() + 1.27, p.method_10260() + 0.5);
            if (item instanceof class_1747 bi) return SbPlacer.find(this.mc, target, t.want, bi, eye, reach) != null;
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
        // obstacles first
        for (class_2338 b : s.breaks()) {
            if (!SbWorld.passable(w, b)) {
                if (!SbWorld.breakable(w, b)) { this.replan(w, pl, s.pos()); return; }
                this.keys(false, false, false, false);
                class_3965 face = this.visibleFace(w, pl, b);
                if (face == null) { this.replan(w, pl, s.pos()); return; }
                this.holdTool(pl, w.method_8320(b), b);
                this.aimAndMine(w, pl, b, face);
                this.noProgress = 0;
                return;
            }
        }
        this.mc.field_1761.method_2925();
        double tx = s.pos().method_10263() + 0.5, tz = s.pos().method_10260() + 0.5;
        double dx = tx - pl.method_23317(), dz = tz - pl.method_23321();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        double dy = pl.method_23318() - s.pos().method_10264();
        boolean ground = pl.method_24828() || pl.method_5799();

        if (s.move() == SbPath.Move.PILLAR) {
            class_2338 under = s.pos().method_10074();
            if (SbWorld.floor(w, under) && Math.abs(dy) < 0.6 && ground) { this.nextStep(); return; }
            if (!this.hold(pl, this.scaffoldItem())) { if (this.count(this.scaffoldItem()) == 0) { this.missing(this.scaffoldItem()); this.toIdle(); } return; }
            boolean centered = horiz < 0.25;
            float yaw = centered ? pl.method_36454() : (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            boolean looking = this.rotateTo(pl, yaw, 89.0f, 1.0f);
            if (!centered) { this.keys(true, false, false, true); this.progress(horiz, w, pl, s); return; }
            this.keys(false, ground && looking && settled(pl), false, false);
            if (pl.method_23318() > under.method_10264() + 1.05 && w.method_8320(under).method_45474()) {
                if (this.aimAndPlace(w, pl, under, null, this.scaffoldItem(), yaw, 89.0f, true) == 1) this.scaffoldPlaced.add(under.method_10063());
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
            class_1792 it = t.want.method_26204().method_8389();
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
        if (this.modeTicks % 20 == 5 || this.modeTicks < 5) {
            this.modeTicks = 6;
            this.mc.field_1761.method_2896(pl, class_1268.field_5808, hit);
            pl.method_6104(class_1268.field_5808);
        }
    }

    private void tickTransfer(class_638 w, class_746 pl) {
        class_1703 h = pl.field_7512;
        if (!(h instanceof class_1707 || h instanceof class_1733)) { this.toIdle(); return; }
        int size = h instanceof class_1707 g ? g.method_17388() * 9 : 27;
        // remember what is in there
        Map<class_1792, Integer> seen = new HashMap<>();
        for (int i = 0; i < size; i++) {
            class_1799 st = h.method_7611(i).method_7677();
            if (!st.method_7960()) seen.merge(st.method_7909(), st.method_7947(), Integer::sum);
        }
        this.storageSeen.put(this.chestPos.method_10063(), seen);
        int free = this.freeSlots(pl);
        // take what we need
        for (int i = 0; i < size; i++) {
            class_1799 st = h.method_7611(i).method_7677();
            if (st.method_7960()) continue;
            Integer n = this.wanted.get(st.method_7909());
            if (n == null || n <= 0 || free <= 0) continue;
            this.mc.field_1761.method_2906(h.field_7763, i, 0, class_1713.field_7794, (class_1657) pl);
            this.wanted.put(st.method_7909(), n - st.method_7947());
            this.waitTicks = this.human.getValue() ? 2 + this.rnd.nextInt(3) : 1;
            return;
        }
        // drop off junk when the inventory is nearly full
        if (free < 4) {
            Set<class_1792> useful = new HashSet<>();
            for (Task t : this.tasks.values()) if (t.place) useful.add(t.want.method_26204().method_8389());
            useful.add(this.scaffoldItem());
            for (int i = size; i < size + 36 && i < h.field_7761.size(); i++) {
                class_1799 st = h.method_7611(i).method_7677();
                if (st.method_7960() || useful.contains(st.method_7909()) || isTool(st) || st.method_7909() == class_1802.field_8288) continue;
                this.mc.field_1761.method_2906(h.field_7763, i, 0, class_1713.field_7794, (class_1657) pl);
                this.waitTicks = this.human.getValue() ? 2 + this.rnd.nextInt(3) : 1;
                return;
            }
        }
        // done here
        pl.method_7346();
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

    private int freeSlots(class_746 pl) {
        int n = 0;
        for (int i = 0; i < 36; i++) if (pl.method_31548().method_5438(i).method_7960()) n++;
        return n;
    }

    static boolean isTool(class_1799 s) {
        if (s.method_7960()) return false;
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
            if (s.method_7960()) continue;
            if (st.method_29291() && !s.method_7951(st)) continue;
            float sp = s.method_7924(st);
            if (sp > bestSpeed + 0.01f || sp > bestSpeed - 0.01f && best >= 9 && i < 9) { bestSpeed = sp; best = i; }
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
            this.aimSpeed = 0.2f + this.rnd.nextFloat() * 0.3f;
            this.aimCap = 18.0f + this.rnd.nextFloat() * 16.0f;
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
            if (this.aimHesitate > 0) { this.aimHesitate--; return false; }
            if (dist > 12.0f && this.rnd.nextFloat() < 0.05f) { this.aimHesitate = this.rnd.nextInt(2); return false; } // a short hitch
            // fast when far, slow when close (ease out), never two ticks alike
            float f = this.aimSpeed * (0.8f + this.rnd.nextFloat() * 0.4f);
            float step = Math.min(dist, Math.max(1.2f + this.rnd.nextFloat() * 1.2f, dist * f));
            step = Math.min(step, this.aimCap);
            float k = step / dist;
            my = dy * k;
            mp = dp * k * (dist < 5.0f ? 1.0f : this.aimLag); // the wrist moves sideways first, up / down follows
            if (dist > 6.0f) mp += Math.abs(dy * k) * this.aimCurve; // an arc, not a ruler-straight line
            if (dist > 2.0f) { my += (this.rnd.nextInt(5) - 2) * g; mp += (this.rnd.nextInt(3) - 1) * g; } // tremor
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
        pl.method_36456(cy + my);
        pl.method_36457(Math.max(-90.0f, Math.min(90.0f, cp + mp)));
        float ry = class_3532.method_15393(gy - pl.method_36454()), rp = gp - pl.method_36455();
        return !this.aimOvershoot && Math.sqrt(ry * ry + rp * rp) < done;
    }

    private float aimTY = Float.NaN, aimTP, aimOffY, aimOffP, aimSpeed, aimCap, aimLag, aimCurve, aimOSY, aimOSP;
    private boolean aimOvershoot, aimPrecise;
    private int aimHesitate, reactWait = -1;
    private final float[] walkPhase = {(float) (Math.random() * 6.3), (float) (Math.random() * 6.3), (float) (Math.random() * 6.3)};
    private float walkPitch = 14.0f;

    // ================================================================== render

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (!this.render.getValue() || this.mc.field_1687 == null) return;
        BaseFinder.Draw d = new BaseFinder.Draw();
        Color accent = new Color(90, 200, 255, 230);
        if (this.task != null && this.mode == Mode.PLACE) d.block(this.task.pos, accent, 40);
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
