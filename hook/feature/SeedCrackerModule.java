package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ButtonSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_2680;
import net.minecraft.class_2806;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_4543;
import net.minecraft.class_5250;
import net.minecraft.class_638;
import net.minecraft.class_642;

/**
 * Seed Cracker (like SeedcrackerX, built in). Walk or fly around: desert pyramids, jungle temples and swamp huts are
 * found on their own in every loaded chunk, the End pillars when you are in the End. Other structures can be typed in
 * with {@code .seed add <type> <x> <z>}. With enough of them the lower 48 bits of the seed are cracked on all cores,
 * the top 16 bits come from the hashed seed the server sends at join. Data is kept per world, so it adds up over visits.
 */
public final class SeedCrackerModule extends Module {
    private final BooleanSetting autoCrack = new BooleanSetting(EncryptedString.of("Auto Crack"), true);
    private final BooleanSetting notify = new BooleanSetting(EncryptedString.of("Chat Messages"), true);
    private final BooleanSetting slime = new BooleanSetting(EncryptedString.of("Use For Slime Chunks"), true);
    private final NumberSetting threads = new NumberSetting(EncryptedString.of("Threads"), 1.0, 32.0, Math.max(1, Runtime.getRuntime().availableProcessors() - 1), 1.0);
    private final ButtonSetting crackNow = new ButtonSetting(EncryptedString.of("Crack Now"), () -> instance().startCrack(true));
    private final ButtonSetting copy = new ButtonSetting(EncryptedString.of("Copy Seed"), () -> instance().copySeed());
    private final ButtonSetting status = new ButtonSetting(EncryptedString.of("Show Status"), () -> instance().printStatus());
    private final ButtonSetting clear = new ButtonSetting(EncryptedString.of("Clear Data"), () -> instance().clearData());

    private static SeedCrackerModule INSTANCE;

    // per world
    private String worldKey;
    private long hashed;
    private final List<SeedCracker.Obs> obs = new ArrayList<>();
    private final int[] pillars = new int[10];
    private Long seed;
    private long[] structureSeeds;

    // scanning
    private final Set<Long> scanned = new HashSet<>();
    private final java.util.ArrayDeque<Long> queue = new java.util.ArrayDeque<>();
    private String dim = "overworld";
    private int ticks, pillarCheckAt;

    // cracking
    private volatile SeedCracker.Result running;
    private AtomicBoolean cancel;
    private int dataVersion, triedVersion = -1;
    private String lastMissing;

    public SeedCrackerModule() {
        super(EncryptedString.of("Seed Cracker"), EncryptedString.of("Cracks the world seed from temples, huts and End pillars around you (like SeedcrackerX). Type .seed in chat for help."), -1, Category.MISC);
        INSTANCE = this;
        this.autoCrack.setDescription(EncryptedString.of("Starts cracking by itself as soon as enough structures are known."));
        this.notify.setDescription(EncryptedString.of("Chat message for every structure found and for the result."));
        this.slime.setDescription(EncryptedString.of("Puts the cracked seed into Slime Chunks when its seed field is empty."));
        this.threads.setDescription(EncryptedString.of("CPU threads for cracking. Fewer = smoother game while it runs."));
        this.crackNow.setDescription(EncryptedString.of("Try now with what is known (may need a while)."));
        this.copy.setDescription(EncryptedString.of("Copies the seed to the clipboard."));
        this.status.setDescription(EncryptedString.of("Prints what is known so far into chat."));
        this.clear.setDescription(EncryptedString.of("Forgets everything collected in this world."));
        this.addSettings(this.autoCrack, this.notify, this.slime, this.threads, this.crackNow, this.copy, this.status, this.clear);
        this.moreSettings(this.threads, this.status, this.clear);
        Arrays.fill(this.pillars, -1);
    }

    static SeedCrackerModule instance() {
        return INSTANCE;
    }

    /** The cracked seed of the world you are in, or null. */
    public static Long crackedSeed() {
        return INSTANCE == null ? null : INSTANCE.seed;
    }

    private int announceAt = -1;

    @Override
    public void onEnable() {
        super.onEnable();
        this.announceAt = this.ticks + 60;
    }

    @Override
    public void onDisable() {
        if (this.cancel != null) this.cancel.set(true);
        super.onDisable();
    }

    // ------------------------------------------------------------------ tick

    @EventListener
    public void onTick(TickEvent event) {
        class_310 mc = this.mc;
        class_638 w = mc.field_1687;
        if (w == null || mc.field_1724 == null) return;
        this.ticks++;
        try { this.dim = w.method_27983().method_29177().method_12832(); } catch (Throwable t) { this.dim = "overworld"; }
        long h = readHashed(w);
        String key = serverKey() + (h != 0 ? "_" + Long.toHexString(h) : "");
        if (!key.equals(this.worldKey)) {
            if (this.cancel != null) this.cancel.set(true);
            this.running = null;
            this.worldKey = key;
            this.hashed = h;
            this.load();
            this.scanned.clear();
            this.queue.clear();
            this.pending.clear();
            this.announceAt = this.ticks + 60;
        }
        if (this.announceAt >= 0 && this.ticks >= this.announceAt) {
            this.announceAt = -1;
            if (this.notify.getValue() && this.seed == null && this.running == null) this.say("watching for structures: " + this.obs.size() + " known - " + this.needText());
        }
        if (this.seed == null) {
            if (this.dim.equals("overworld")) this.scanChunks(w);
            else if (this.dim.equals("the_end") && this.ticks >= this.pillarCheckAt) { this.pillarCheckAt = this.ticks + 40; this.scanPillars(w); }
        }
        this.followRun();
        if (this.seed == null && this.running == null && this.autoCrack.getValue() && this.triedVersion != this.dataVersion) this.startCrack(false);
    }

    private static long readHashed(class_638 w) {
        try {
            class_4543 bm = w.method_22385();
            for (Field f : class_4543.class.getDeclaredFields()) {
                if (f.getType() == long.class && !Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    return f.getLong(bm);
                }
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private String serverKey() {
        try {
            class_642 sd = this.mc.method_1558();
            if (sd != null && sd.field_3761 != null) return sd.field_3761.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
        } catch (Throwable ignored) {
        }
        return "singleplayer";
    }

    // ------------------------------------------------------------------ structure detection

    private void scanChunks(class_638 w) {
        var p = this.mc.field_1724;
        int pcx = (int) Math.floor(p.method_23317()) >> 4, pcz = (int) Math.floor(p.method_23321()) >> 4;
        int r = Math.max(2, this.mc.field_1690.method_38521()) + 1;
        if (this.ticks % 10 == 0 || this.queue.isEmpty()) {
            this.queue.clear();
            for (int ring = 0; ring <= r; ring++) {
                for (int dx = -ring; dx <= ring; dx++) {
                    for (int dz = -ring; dz <= ring; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                        long k = class_1923.method_8331(pcx + dx, pcz + dz);
                        if (!this.scanned.contains(k)) this.queue.add(k);
                    }
                }
            }
            if (this.scanned.size() > 200_000) this.scanned.clear();
        }
        long budget = System.nanoTime() + 2_000_000L;
        while (!this.queue.isEmpty() && System.nanoTime() < budget) {
            long k = this.queue.poll();
            int cx = class_1923.method_8325(k), cz = class_1923.method_8332(k);
            Object c = w.method_2935().method_2857(cx, cz, class_2806.field_12803, false);
            if (!(c instanceof class_2818 chunk)) continue;
            this.scanned.add(k);
            String found = detect(chunk, cx, cz);
            if (found != null) this.addObs(new SeedCracker.Obs(SeedCracker.TYPES.get(found), cx, cz, 0, true), true);
            this.collectAnchors(chunk);
        }
        this.workPending(w, budget + 1_500_000L);
    }

    // ------------------------------------------------------------------ template structures (shipwrecks, igloos, ruined portals) + monuments

    /** A block that may belong to a structure, checked once enough around it is loaded. kind: index in StructureMatcher.KINDS, -1 = monument. */
    private record Anchor(int kind, int x, int y, int z, int[] tries) {
    }

    private final java.util.ArrayDeque<Anchor> pending = new java.util.ArrayDeque<>();

    private void collectAnchors(class_2818 chunk) {
        class_2826[] sections = chunk.method_12006();
        int minSec = chunk.method_32891();
        int bx = chunk.method_12004().field_9181 << 4, bz = chunk.method_12004().field_9180 << 4;
        int chests = 0, furnaces = 0, obsidian = 0;
        boolean prism = false;
        for (int si = 0; si < sections.length; si++) {
            class_2826 sec = sections[si];
            if (sec == null || sec.method_38292()) continue;
            boolean any = sec.method_19523(st -> {
                String id = BaseFinder.id(((class_2680) st).method_26204());
                return id.equals("chest") || id.equals("furnace") || id.equals("obsidian") || id.equals("crying_obsidian") || id.equals("prismarine_bricks");
            });
            if (!any) continue;
            int by = (minSec + si) << 4;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        String id = BaseFinder.id(sec.method_12254(x, y, z).method_26204());
                        switch (id) {
                            case "chest" -> { if (chests++ < 4) this.addAnchor(0, bx + x, by + y, bz + z); }
                            case "furnace" -> { if (furnaces++ < 2) this.addAnchor(1, bx + x, by + y, bz + z); }
                            case "obsidian", "crying_obsidian" -> { if (obsidian++ % 5 == 0 && obsidian < 16) this.addAnchor(2, bx + x, by + y, bz + z); }
                            case "prismarine_bricks" -> { if (!prism) { prism = true; this.addAnchor(-1, bx + x, by + y, bz + z); } }
                            default -> { }
                        }
                    }
                }
            }
        }
    }

    private void addAnchor(int kind, int x, int y, int z) {
        // already known structure of this kind close by: nothing new to learn
        String type = kind < 0 ? "monument" : StructureMatcher.KINDS[kind].type();
        int near = kind < 0 ? 96 : 48;
        for (SeedCracker.Obs o : this.obs) {
            if (o.type.id().equals(type) && Math.abs(o.cx * 16 + 8 - x) < near && Math.abs(o.cz * 16 + 8 - z) < near) return;
        }
        if (kind < 0) for (Anchor a : this.pending) if (a.kind < 0 && Math.abs(a.x - x) < near && Math.abs(a.z - z) < near) return;
        if (this.pending.size() < 400) this.pending.add(new Anchor(kind, x, y, z, new int[]{0, 0}));
    }

    private void workPending(class_638 w, long until) {
        int n = this.pending.size();
        for (int i = 0; i < n && System.nanoTime() < until; i++) {
            Anchor a = this.pending.poll();
            if (a.tries[1] > this.ticks) { this.pending.add(a); continue; }
            boolean retry;
            if (a.kind < 0) retry = this.checkMonument(w, a);
            else {
                StructureMatcher.Kind k = StructureMatcher.KINDS[a.kind];
                boolean known = false;
                for (SeedCracker.Obs o : this.obs) if (o.type.id().equals(k.type()) && Math.abs(o.cx * 16 + 8 - a.x) < 48 && Math.abs(o.cz * 16 + 8 - a.z) < 48) known = true;
                if (known) continue;
                StructureMatcher.Hit h = StructureMatcher.match(k, worldView(w), a.x, a.y, a.z);
                retry = h != null && h.retry;
                if (h != null && !h.retry) {
                    System.out.println("Hook SeedCracker: " + k.type() + " " + h.template + " " + h.matched + "/" + h.total + " -> chunk " + h.cx + " " + h.cz);
                    this.addObs(new SeedCracker.Obs(SeedCracker.TYPES.get(k.type()), h.cx, h.cz, 0, true), true);
                }
            }
            if (retry && ++a.tries[0] < 30) {
                a.tries[1] = this.ticks + 40;
                this.pending.add(a);
            }
        }
    }

    private static StructureMatcher.World worldView(class_638 w) {
        return new StructureMatcher.World() {
            long key = Long.MIN_VALUE;
            class_2818 chunk;
            final class_2338.class_2339 pos = new class_2338.class_2339();

            @Override
            public String block(int x, int y, int z) {
                long k = class_1923.method_8331(x >> 4, z >> 4);
                if (k != this.key) {
                    this.key = k;
                    Object c = w.method_2935().method_2857(x >> 4, z >> 4, class_2806.field_12803, false);
                    this.chunk = c instanceof class_2818 ch ? ch : null;
                }
                if (this.chunk == null) return null;
                return BaseFinder.id(this.chunk.method_8320(this.pos.method_10103(x, y, z)).method_26204());
            }
        };
    }

    /**
     * Ocean monument: its building is always 58 x 58 blocks from (chunk corner - 29) at y 39..61, whatever way it faces.
     * When the prismarine around spans exactly that, the start chunk is exact. Returns true = try again later.
     */
    private boolean checkMonument(class_638 w, Anchor a) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        boolean missing = false;
        class_2338.class_2339 pos = new class_2338.class_2339();
        for (int cx = (a.x >> 4) - 4; cx <= (a.x >> 4) + 4; cx++) {
            for (int cz = (a.z >> 4) - 4; cz <= (a.z >> 4) + 4; cz++) {
                Object c = w.method_2935().method_2857(cx, cz, class_2806.field_12803, false);
                if (!(c instanceof class_2818 chunk)) { missing = true; continue; }
                class_2826[] sections = chunk.method_12006();
                int minSec = chunk.method_32891();
                for (int si = 0; si < sections.length; si++) {
                    int by = (minSec + si) << 4;
                    if (by + 15 < 39 || by > 61) continue;
                    class_2826 sec = sections[si];
                    if (sec == null || sec.method_38292()) continue;
                    if (!sec.method_19523(st -> isPrismarine(BaseFinder.id(((class_2680) st).method_26204())))) continue;
                    for (int y = Math.max(0, 39 - by); y < 16 && by + y <= 61; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                if (!isPrismarine(BaseFinder.id(sec.method_12254(x, y, z).method_26204()))) continue;
                                int wx = (cx << 4) + x, wz = (cz << 4) + z;
                                if (wx < minX) minX = wx;
                                if (wx > maxX) maxX = wx;
                                if (wz < minZ) minZ = wz;
                                if (wz > maxZ) maxZ = wz;
                            }
                        }
                    }
                }
            }
        }
        if (StructureMatcher.debug) System.out.println("Hook SeedCracker: monument check box " + minX + ".." + maxX + " " + minZ + ".." + maxZ + " missing=" + missing);
        if (minX == Integer.MAX_VALUE) return false;
        if (maxX - minX == 57 && maxZ - minZ == 57 && ((minX + 29) & 15) == 0 && ((minZ + 29) & 15) == 0) {
            System.out.println("Hook SeedCracker: monument box " + minX + " " + minZ + " -> chunk " + ((minX + 29) >> 4) + " " + ((minZ + 29) >> 4));
            this.addObs(new SeedCracker.Obs(SeedCracker.TYPES.get("monument"), (minX + 29) >> 4, (minZ + 29) >> 4, 0, true), true);
            return false;
        }
        return missing || maxX - minX < 57 || maxZ - minZ < 57;
    }

    private static boolean isPrismarine(String id) {
        return id.equals("prismarine") || id.equals("prismarine_bricks") || id.equals("dark_prismarine") || id.equals("sea_lantern");
    }

    /** Which structure started in this chunk (by its blocks), or null. */
    static String detect(class_2818 chunk, int cx, int cz) {
        class_2826[] sections = chunk.method_12006();
        int minSec = chunk.method_32891();
        int dispensers = 0, hooks = 0, mossy = 0, chiseled = 0, pots = 0, cauldrons = 0, tables = 0, spruce = 0;
        List<int[]> blue = new ArrayList<>();
        for (int si = 0; si < sections.length; si++) {
            class_2826 sec = sections[si];
            if (sec == null || sec.method_38292()) continue;
            int by = (minSec + si) << 4;
            boolean any = sec.method_19523(st -> {
                String id = BaseFinder.id(((class_2680) st).method_26204());
                return id.equals("blue_terracotta") || id.equals("dispenser") || id.equals("potted_red_mushroom") || id.equals("tripwire_hook");
            });
            if (!any) continue;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        String id = BaseFinder.id(sec.method_12254(x, y, z).method_26204());
                        switch (id) {
                            case "blue_terracotta" -> { if (x == 10 && z == 10) blue.add(new int[]{by + y}); }
                            case "dispenser" -> dispensers++;
                            case "tripwire_hook" -> hooks++;
                            case "potted_red_mushroom" -> pots++;
                            default -> { }
                        }
                    }
                }
            }
        }
        class_638 w = class_310.method_1551().field_1687;
        int bx = cx << 4, bz = cz << 4;
        for (int[] b : blue) {
            int y = b[0], orange = 0;
            for (int[] d : new int[][]{{2, 0}, {-2, 0}, {0, 2}, {0, -2}}) {
                if (BaseFinder.id(w.method_8320(new class_2338(bx + 10 + d[0], y, bz + 10 + d[1])).method_26204()).equals("orange_terracotta")) orange++;
            }
            if (orange >= 3) return "desert_pyramid";
        }
        if (dispensers >= 2 || hooks >= 1 || pots >= 1) {
            // count the surroundings once, only for chunks that may hold a temple / hut
            for (int si = 0; si < sections.length; si++) {
                class_2826 sec = sections[si];
                if (sec == null || sec.method_38292()) continue;
                int by = (minSec + si) << 4;
                    for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            switch (BaseFinder.id(sec.method_12254(x, y, z).method_26204())) {
                                case "mossy_cobblestone" -> mossy++;
                                case "chiseled_stone_bricks" -> chiseled++;
                                case "cauldron", "water_cauldron" -> cauldrons++;
                                case "crafting_table" -> tables++;
                                case "spruce_planks" -> spruce++;
                                default -> { }
                            }
                        }
                    }
                }
            }
            if (dispensers >= 2 && hooks >= 1 && mossy >= 8 && chiseled >= 1) return "jungle_temple";
            if (pots >= 1 && cauldrons >= 1 && tables >= 1 && spruce >= 20) return "swamp_hut";
        }
        return null;
    }

    private void scanPillars(class_638 w) {
        boolean changed = false;
        for (int i = 0; i < 10; i++) {
            if (this.pillars[i] >= 0) continue;
            int x = (int) Math.floor(42.0 * Math.cos(2.0 * (-Math.PI + (Math.PI / 10) * i)));
            int z = (int) Math.floor(42.0 * Math.sin(2.0 * (-Math.PI + (Math.PI / 10) * i)));
            if (!(w.method_2935().method_2857(x >> 4, z >> 4, class_2806.field_12803, false) instanceof class_2818)) continue;
            if (!(w.method_2935().method_2857((x + 6) >> 4, z >> 4, class_2806.field_12803, false) instanceof class_2818)) continue;
            int top = -1;
            for (int y = 130; y >= 60; y--) {
                if (BaseFinder.id(w.method_8320(new class_2338(x, y, z)).method_26204()).equals("obsidian")) { top = y; break; }
            }
            if (top < 0) continue;
            int h = top + 1;
            if ((h - 76) % 3 != 0 || h < 76 || h > 103) continue;
            int l = (h - 76) / 3, radius = 0;
            while (radius < 7 && BaseFinder.id(w.method_8320(new class_2338(x + radius + 1, 70, z)).method_26204()).equals("obsidian")) radius++;
            if (radius != 2 + l / 3) continue; // mined / changed pillar
            this.pillars[i] = h;
            changed = true;
        }
        if (changed) {
            int n = 0;
            for (int p : this.pillars) if (p >= 0) n++;
            this.dataVersion++;
            this.save();
            if (this.notify.getValue()) this.say("End pillars read: " + n + "/10 - " + this.needText());
        }
    }

    private void addObs(SeedCracker.Obs o, boolean tell) {
        for (SeedCracker.Obs e : this.obs) if (e.type == o.type && e.cx == o.cx && e.cz == o.cz) return;
        this.obs.add(o);
        this.dataVersion++;
        this.save();
        if (tell && this.notify.getValue()) {
            this.say("§a" + o.type.name() + "§f found at " + (o.cx * 16) + " " + (o.cz * 16) + " §7(" + this.obs.size() + (this.obs.size() == 1 ? " structure" : " structures") + ") - " + this.needText());
        }
    }

    /** "~3 more structures needed (or ~1 + the End pillars)" / "enough - cracking". */
    private String needText() {
        int[] ps = this.pillarSeedsOrNull();
        int k = SeedCracker.needMore(this.obs, ps, this.hashed);
        if (k == 0) return "§aenough - " + (this.autoCrack.getValue() ? "cracking now" : "press Crack Now");
        String s = "§e~" + k + " more structure" + (k == 1 ? "" : "s") + " needed";
        if (ps == null) {
            int e = SeedCracker.needMoreWithEnd(this.obs, null, this.hashed);
            if (e < k) s += e == 0 ? "§7 (or just read the End pillars)" : "§7 (or ~" + e + " + reading the End pillars)";
        }
        return s;
    }

    private int[] pillarSeedsOrNull() {
        return SeedCracker.pillarSeeds(this.pillars);
    }

    // ------------------------------------------------------------------ cracking

    void startCrack(boolean manual) {
        if (this.mc.field_1687 == null) return;
        if (this.running != null) { if (manual) this.say("already cracking - " + this.progressText()); return; }
        if (this.seed != null) { if (manual) this.printResult(); return; }
        this.triedVersion = this.dataVersion;
        int[] ps = this.pillarSeedsOrNull();
        String miss = SeedCracker.ready(this.obs, ps, this.hashed);
        if (miss != null && !manual) {
            this.lastMissing = miss;
            return;
        }
        if (this.obs.isEmpty() && ps == null) { this.say("nothing collected yet - walk around (temples, huts, igloos, shipwrecks, ruined portals, monuments) or visit the End"); return; }
        SeedCracker.Result res = new SeedCracker.Result();
        AtomicBoolean c = new AtomicBoolean();
        this.cancel = c;
        this.running = res;
        List<SeedCracker.Obs> copyObs = new ArrayList<>(this.obs);
        long h = this.hashed;
        int th = this.threads.getIntValue();
        if (this.notify.getValue()) this.say("cracking with " + copyObs.size() + " structures" + (ps != null ? " + End pillars" : "") + " on " + th + " threads...");
        Thread t = new Thread(() -> SeedCracker.run(copyObs, ps, h, th, res, c, s -> System.out.println("Hook SeedCracker: " + s)), "Hook SeedCracker");
        t.setDaemon(true);
        t.start();
    }

    private void followRun() {
        SeedCracker.Result r = this.running;
        if (r == null) return;
        if (!r.done) {
            if (this.ticks % 20 == 0 && this.mc.field_1724 != null && this.isEnabled()) this.mc.field_1724.method_7353(class_2561.method_43470("§b[Seed] §f" + this.progressText()), true);
            return;
        }
        this.running = null;
        if (r.badObs != null) {
            this.obs.removeIf(o -> o.toString().equals(r.badObs.toString()));
            this.dataVersion++;
            this.triedVersion = this.dataVersion;
            this.save();
            this.say("§e" + r.badObs.type.name() + " at " + r.badObs.cx * 16 + " " + r.badObs.cz * 16 + " does not fit the others - left out");
        }
        if (r.seed != null) {
            this.seed = r.seed;
            this.structureSeeds = r.structureSeeds;
            this.save();
            this.printResult();
            this.offerSlime();
        } else {
            if (r.structureSeeds != null && r.structureSeeds.length == 1) {
                this.structureSeeds = r.structureSeeds;
                this.save();
                this.offerSlime();
            }
            if (!"cancelled".equals(r.error)) this.say("§c" + r.error + (r.structureSeeds != null && r.structureSeeds.length == 1 ? " §7(structure seed " + r.structureSeeds[0] + ")" : ""));
        }
    }

    private String progressText() {
        SeedCracker.Result r = this.running;
        if (r == null) return "idle";
        return "cracking: " + r.stage + " " + Math.round(r.progress * 100) + "%";
    }

    private void offerSlime() {
        if (!this.slime.getValue()) return;
        long s = this.seed != null ? this.seed : this.structureSeeds != null && this.structureSeeds.length == 1 ? this.structureSeeds[0] : Long.MIN_VALUE;
        if (s == Long.MIN_VALUE) return;
        Module m = hook.Hook.INSTANCE.getModuleManager().getModuleByClass(SlimeChunkModule.class);
        if (m instanceof SlimeChunkModule sm) sm.offerSeed(s);
    }

    private void printResult() {
        if (this.seed == null) { this.printStatus(); return; }
        String s = Long.toString(this.seed);
        class_5250 msg = class_2561.method_43470("§b[Seed] §fSeed found: ");
        msg.method_10852(class_2561.method_43470("§a§n" + s).method_27694(st -> st.method_10958(new class_2558.class_10606(s)).method_10949(new class_2568.class_10613(class_2561.method_43470("Click to copy")))));
        if (this.mc.field_1724 != null) this.mc.field_1724.method_7353(msg, false);
    }

    private void copySeed() {
        if (this.seed == null) { this.say("no seed yet - " + (this.running != null ? this.progressText() : "keep exploring")); return; }
        this.mc.field_1774.method_1455(Long.toString(this.seed));
        this.say("seed copied");
    }

    private void printStatus() {
        if (this.seed != null) { this.printResult(); return; }
        int n = 0;
        for (int p : this.pillars) if (p >= 0) n++;
        String miss = SeedCracker.ready(this.obs, this.pillarSeedsOrNull(), this.hashed);
        this.say(this.obs.size() + " structures, " + n + "/10 End pillars, hashed seed " + (this.hashed != 0 ? "known" : "§cmissing") + "§f - "
            + (this.running != null ? this.progressText() : this.needText()) + (miss != null ? " §7(" + miss + ")" : ""));
        StringBuilder sb = new StringBuilder();
        for (SeedCracker.Obs o : this.obs) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(o.type.id()).append(' ').append(o.cx * 16).append(' ').append(o.cz * 16);
        }
        if (sb.length() > 0) this.say("§7" + sb);
    }

    private void clearData() {
        if (this.cancel != null) this.cancel.set(true);
        this.running = null;
        this.obs.clear();
        Arrays.fill(this.pillars, -1);
        this.seed = null;
        this.structureSeeds = null;
        this.scanned.clear();
        this.dataVersion++;
        this.save();
        this.say("data of this world cleared");
    }

    private void say(String s) {
        if (this.mc.field_1724 != null) this.mc.field_1724.method_7353(class_2561.method_43470("§b[Seed] §f" + s), false);
    }

    // ------------------------------------------------------------------ chat commands

    /** Called for every chat line you send. True = it was a .seed command and must not go to the server. */
    public static boolean onChat(String msg) {
        if (msg == null || !(msg.equals(".seed") || msg.startsWith(".seed "))) return false;
        SeedCrackerModule m = INSTANCE;
        if (m == null) return true;
        class_310.method_1551().execute(() -> m.command(msg.substring(5).trim()));
        return true;
    }

    private void command(String a) {
        String[] p = a.isEmpty() ? new String[0] : a.split("\\s+");
        String c = p.length == 0 ? "help" : p[0].toLowerCase(Locale.ROOT);
        switch (c) {
            case "status", "info" -> this.printStatus();
            case "crack" -> this.startCrack(true);
            case "copy" -> this.copySeed();
            case "clear" -> this.clearData();
            case "stop" -> { if (this.cancel != null) this.cancel.set(true); this.say("stopped"); }
            case "types" -> this.say(String.join(", ", SeedCracker.TYPES.keySet()));
            case "remove" -> {
                if (this.obs.isEmpty()) { this.say("nothing to remove"); return; }
                SeedCracker.Obs o = this.obs.remove(this.obs.size() - 1);
                this.dataVersion++;
                this.save();
                this.say("removed " + o.type.name() + " at " + o.cx * 16 + " " + o.cz * 16);
            }
            case "add" -> {
                if (p.length < 4) { this.say("usage: .seed add <type> <x> <z> [radius in chunks] - x z = block coordinates inside the structure. Types: .seed types"); return; }
                SeedCracker.Type t = SeedCracker.TYPES.get(p[1].toLowerCase(Locale.ROOT));
                if (t == null) { this.say("unknown type - " + String.join(", ", SeedCracker.TYPES.keySet())); return; }
                try {
                    int x = (int) Math.floor(Double.parseDouble(p[2])), z = (int) Math.floor(Double.parseDouble(p[3]));
                    int tol = p.length > 4 ? Integer.parseInt(p[4]) : t.tolerance();
                    this.addObs(new SeedCracker.Obs(t, x >> 4, z >> 4, Math.max(0, Math.min(4, tol)), false), true);
                } catch (NumberFormatException e) {
                    this.say("x / z must be numbers");
                }
            }
            case "pillar" -> {
                // .seed pillar <index 0-9> <height>  (for when you know them from somewhere else)
                try {
                    this.pillars[Integer.parseInt(p[1])] = Integer.parseInt(p[2]);
                    this.dataVersion++;
                    this.save();
                    this.say("pillar set");
                } catch (Exception e) {
                    this.say("usage: .seed pillar <0-9> <height>");
                }
            }
            default -> {
                this.say(".seed status | crack | copy | stop | clear | types | remove (last) | add <type> <x> <z> [radius]");
                this.say("§7Temples, swamp huts, igloos, shipwrecks, ruined portals, ocean monuments and End pillars are found by themselves. Add villages, outposts, buried treasure (exact chest chunk) etc. by hand.");
            }
        }
    }

    // ------------------------------------------------------------------ storage

    private Path file() {
        return this.mc.field_1697.toPath().resolve("config").resolve("hook-seedcracker").resolve(this.worldKey + ".txt");
    }

    private void load() {
        this.obs.clear();
        Arrays.fill(this.pillars, -1);
        this.seed = null;
        this.structureSeeds = null;
        this.dataVersion++;
        try {
            Path f = this.file();
            if (!Files.exists(f)) return;
            for (String line : Files.readAllLines(f, StandardCharsets.UTF_8)) {
                String[] p = line.trim().split("\\s+");
                if (p.length < 2) continue;
                switch (p[0]) {
                    case "seed" -> this.seed = Long.parseLong(p[1]);
                    case "structure" -> this.structureSeeds = new long[]{Long.parseLong(p[1])};
                    case "pillar" -> this.pillars[Integer.parseInt(p[1])] = Integer.parseInt(p[2]);
                    case "obs" -> {
                        SeedCracker.Type t = SeedCracker.TYPES.get(p[1]);
                        if (t != null) this.obs.add(new SeedCracker.Obs(t, Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4]), p.length > 5 && p[5].equals("auto")));
                    }
                    default -> { }
                }
            }
            if (this.seed != null) { this.offerSlime(); if (this.notify.getValue()) this.printResult(); }
        } catch (Throwable t) {
            System.out.println("Hook SeedCracker: could not load " + t);
        }
    }

    private void save() {
        if (this.worldKey == null) return;
        try {
            List<String> lines = new ArrayList<>();
            if (this.seed != null) lines.add("seed " + this.seed);
            if (this.structureSeeds != null && this.structureSeeds.length == 1) lines.add("structure " + this.structureSeeds[0]);
            for (int i = 0; i < 10; i++) if (this.pillars[i] >= 0) lines.add("pillar " + i + " " + this.pillars[i]);
            for (SeedCracker.Obs o : this.obs) lines.add("obs " + o);
            Path f = this.file();
            Files.createDirectories(f.getParent());
            Files.write(f, lines, StandardCharsets.UTF_8);
        } catch (Throwable t) {
            System.out.println("Hook SeedCracker: could not save " + t);
        }
    }

    // ------------------------------------------------------------------ self-test access

    static List<SeedCracker.Obs> testObs() {
        return INSTANCE.obs;
    }

    static void testAdd(SeedCracker.Obs o) {
        INSTANCE.addObs(o, false);
    }

    static void testRetain(java.util.function.Predicate<SeedCracker.Obs> keep) {
        INSTANCE.obs.removeIf(o -> !keep.test(o));
        INSTANCE.dataVersion++;
        INSTANCE.save();
    }

    static void testRescan() {
        INSTANCE.scanned.clear();
        INSTANCE.queue.clear();
    }

    static String testState() {
        SeedCrackerModule m = INSTANCE;
        return "obs=" + m.obs.size() + " hashed=" + m.hashed + " seed=" + m.seed + " running=" + (m.running == null ? "-" : m.progressText()) + " ready=" + SeedCracker.ready(m.obs, m.pillarSeedsOrNull(), m.hashed);
    }
}
