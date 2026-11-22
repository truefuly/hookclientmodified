package hook.feature;

import hook.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_7923;

/**
 * Shared chunk scanner for the Base Finder modules. Every loaded chunk around you is read once (and again when blocks
 * in it change), block by block, and sorted into kinds (player blocks, storage, light, portals, signs, growth, tunnels,
 * lit caves). The modules only read the results, so ten modules cost one scan. Work is spread over ticks with a
 * time budget, so it never stutters.
 */
public final class BaseFinder {
    private BaseFinder() {
    }

    // ------------------------------------------------------------------ kinds
    static final int BASE = 0, STORAGE = 1, LIGHT = 2, PORTAL = 3, SIGN = 4, KELP_TOP = 5, KELP = 6, AMETHYST_FULL = 7,
            AMETHYST_BUD = 8, BERRY_RIPE = 9, BERRY = 10, TUNNEL = 11, LIT_CAVE = 12, NATURAL_LIGHT = 13, KINDS = 14;

    /** Blocks that worldgen basically never places on its own (or only inside structures) - a player put them there. */
    private static final Set<String> PLAYER_BLOCKS = Set.of(
        "crafting_table", "furnace", "blast_furnace", "smoker", "enchanting_table", "anvil", "chipped_anvil", "damaged_anvil",
        "brewing_stand", "beacon", "grindstone", "smithing_table", "stonecutter", "loom", "cartography_table", "fletching_table",
        "lectern", "composter", "respawn_anchor", "lodestone", "conduit", "jukebox", "note_block", "piston", "sticky_piston",
        "observer", "dropper", "dispenser", "hopper", "comparator", "repeater", "redstone_lamp", "daylight_detector", "target",
        "lever", "tripwire_hook", "redstone_block", "slime_block", "honey_block", "tnt", "end_crystal", "ender_chest",
        "obsidian_bed", "netherite_block", "diamond_block", "emerald_block", "gold_block", "iron_block", "lapis_block",
        "copper_block", "rail", "powered_rail", "detector_rail", "activator_rail", "scaffolding", "crafter", "trial_spawner_never",
        "glass", "tinted_glass", "glass_pane", "sea_lantern_placed", "white_concrete", "black_concrete", "shulker_box",
        "item_frame", "flower_pot", "armor_stand", "chiseled_bookshelf", "decorated_pot", "beehive", "barrel_never");
    private static final String[] PLAYER_SUFFIX = {"_bed", "_concrete", "_concrete_powder", "_shulker_box", "_stained_glass",
        "_stained_glass_pane", "_glazed_terracotta", "_carpet", "_banner", "_wall_banner", "_candle", "_pressure_plate_never"};
    private static final Set<String> STORAGE_BLOCKS = Set.of("chest", "trapped_chest", "barrel", "ender_chest", "shulker_box",
        "hopper", "dropper", "dispenser", "furnace", "blast_furnace", "smoker", "brewing_stand", "crafter", "chiseled_bookshelf", "decorated_pot");
    /** Light a player places. (Natural ones - lava, glow lichen, magma, shroomlight in the nether... - are not here.) */
    private static final Set<String> PLAYER_LIGHT = Set.of("torch", "wall_torch", "soul_torch", "soul_wall_torch", "redstone_torch",
        "redstone_wall_torch", "copper_torch", "copper_wall_torch", "lantern", "soul_lantern", "jack_o_lantern", "glowstone",
        "sea_lantern", "redstone_lamp", "end_rod", "beacon", "conduit", "ochre_froglight", "verdant_froglight", "pearlescent_froglight",
        "campfire", "soul_campfire", "candle", "light", "copper_bulb", "exposed_copper_bulb", "weathered_copper_bulb",
        "oxidized_copper_bulb", "waxed_copper_bulb", "copper_lantern");
    private static final Set<String> NATURAL_LIGHT_BLOCKS = Set.of("lava", "glow_lichen", "magma_block", "shroomlight",
        "cave_vines", "cave_vines_plant", "sculk_sensor", "sculk_catalyst", "amethyst_cluster", "large_amethyst_bud",
        "medium_amethyst_bud", "small_amethyst_bud", "brown_mushroom", "fire", "soul_fire", "nether_portal", "end_portal",
        "end_gateway", "crying_obsidian", "respawn_anchor", "dragon_egg", "spawner", "trial_spawner", "vault", "light_block");
    private static final Set<String> PORTAL_BLOCKS = Set.of("nether_portal", "end_portal", "end_portal_frame", "end_gateway");

    private static final Map<class_2248, Integer> KIND = new IdentityHashMap<>();
    private static final Map<class_2248, String> IDS = new IdentityHashMap<>();

    static String id(class_2248 b) {
        String s = IDS.get(b);
        if (s == null) {
            try { s = class_7923.field_41175.method_10221(b).method_12832(); } catch (Throwable t) { s = "?"; }
            IDS.put(b, s);
        }
        return s;
    }

    private static int kindOf(class_2248 b) {
        Integer k = KIND.get(b);
        if (k != null) return k;
        String id = id(b);
        int bits = 0;
        if (PLAYER_BLOCKS.contains(id)) bits |= 1 << BASE;
        for (String suf : PLAYER_SUFFIX) if (id.endsWith(suf)) bits |= 1 << BASE;
        if (id.endsWith("_shulker_box")) bits |= 1 << STORAGE;
        if (STORAGE_BLOCKS.contains(id)) bits |= 1 << STORAGE;
        if (PLAYER_LIGHT.contains(id) || id.endsWith("_candle")) bits |= 1 << LIGHT;
        if (NATURAL_LIGHT_BLOCKS.contains(id)) bits |= 1 << NATURAL_LIGHT;
        if (PORTAL_BLOCKS.contains(id)) bits |= 1 << PORTAL;
        if (id.endsWith("_sign") || id.endsWith("_hanging_sign")) bits |= 1 << SIGN;
        if (id.equals("kelp") || id.equals("kelp_plant")) bits |= 1 << KELP;
        if (id.equals("amethyst_cluster")) bits |= 1 << AMETHYST_FULL;
        if (id.endsWith("_amethyst_bud")) bits |= 1 << AMETHYST_BUD;
        if (id.equals("sweet_berry_bush")) bits |= 1 << BERRY;
        KIND.put(b, bits);
        return bits;
    }

    static int intProp(class_2680 st, String name) {
        for (class_2769<?> p : st.method_28501()) {
            if (p.method_11899().equals(name)) {
                Object v = st.method_11654(p);
                return v instanceof Integer i ? i : -1;
            }
        }
        return -1;
    }

    static boolean boolProp(class_2680 st, String name) {
        for (class_2769<?> p : st.method_28501()) {
            if (p.method_11899().equals(name)) return Boolean.TRUE.equals(st.method_11654(p));
        }
        return false;
    }

    // ------------------------------------------------------------------ results
    static final class ChunkData {
        final int cx, cz;
        @SuppressWarnings("unchecked")
        final List<class_2338>[] hits = new List[KINDS];
        long scannedAt;

        ChunkData(int cx, int cz) {
            this.cx = cx;
            this.cz = cz;
            for (int i = 0; i < KINDS; i++) this.hits[i] = new ArrayList<>();
        }

        int count(int kind) {
            return this.hits[kind].size();
        }
    }

    private static final Map<Long, ChunkData> DATA = new HashMap<>();
    private static final ArrayDeque<Long> QUEUE = new ArrayDeque<>();
    private static final Set<Long> QUEUED = new HashSet<>();
    private static final Set<Long> DIRTY = new HashSet<>();
    private static class_638 world;
    private static int lastTick = Integer.MIN_VALUE;
    private static boolean wantAir;

    static long key(int cx, int cz) {
        return class_1923.method_8331(cx, cz);
    }

    static ChunkData get(int cx, int cz) {
        return DATA.get(key(cx, cz));
    }

    static Iterable<ChunkData> all() {
        return DATA.values();
    }

    /** A block changed (packet): rescan that chunk soon. Called from the network thread. */
    static void markDirty(int bx, int bz) {
        synchronized (DIRTY) { DIRTY.add(key(bx >> 4, bz >> 4)); }
    }

    /** Block changes from the server: the chunks they are in get scanned again. */
    static void onPacket(Object pk) {
        if (pk instanceof net.minecraft.class_8038<?> bundle) { for (Object s : bundle.method_48324()) onPacket(s); return; }
        if (pk instanceof net.minecraft.class_2626 u) markDirty(u.method_11309().method_10263(), u.method_11309().method_10260());
        else if (pk instanceof net.minecraft.class_2637 s) {
            long[] first = {Long.MIN_VALUE};
            s.method_30621((pos, st) -> { long k = key(pos.method_10263() >> 4, pos.method_10260() >> 4); if (k != first[0]) { first[0] = k; markDirty(pos.method_10263(), pos.method_10260()); } });
        } else if (pk instanceof net.minecraft.class_2672 c) markDirty(c.method_11523() << 4, c.method_11524() << 4);
    }

    /** Called by every Base Finder module each tick; does the work once per game tick. */
    static void tick(boolean needAir) {
        class_310 mc = class_310.method_1551();
        class_746 p = mc.field_1724;
        if (p == null || mc.field_1687 == null) return;
        if (needAir) wantAir = true;
        if (p.field_6012 == lastTick) return;
        lastTick = p.field_6012;
        if (mc.field_1687 != world) {
            world = mc.field_1687;
            DATA.clear();
            QUEUE.clear();
            QUEUED.clear();
            synchronized (DIRTY) { DIRTY.clear(); }
        }
        int pcx = (int) Math.floor(p.method_23317()) >> 4, pcz = (int) Math.floor(p.method_23321()) >> 4;
        int r = Math.max(2, mc.field_1690.method_38521()) + 1;
        // drop chunks far away
        if (p.field_6012 % 40 == 0) DATA.values().removeIf(d -> Math.abs(d.cx - pcx) > r + 2 || Math.abs(d.cz - pcz) > r + 2);
        // queue unscanned chunks, nearest first (rings)
        if (p.field_6012 % 10 == 0 || QUEUE.isEmpty()) {
            for (int ring = 0; ring <= r; ring++) {
                for (int dx = -ring; dx <= ring; dx++) {
                    for (int dz = -ring; dz <= ring; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                        long k = key(pcx + dx, pcz + dz);
                        if (!DATA.containsKey(k) && QUEUED.add(k)) QUEUE.add(k);
                    }
                }
            }
        }
        synchronized (DIRTY) {
            for (Long k : DIRTY) if (QUEUED.add(k)) QUEUE.addFirst(k);
            DIRTY.clear();
        }
        boolean air = wantAir;
        wantAir = false;
        long budget = System.nanoTime() + 3_000_000L; // 3 ms per tick
        while (!QUEUE.isEmpty() && System.nanoTime() < budget) {
            long k = QUEUE.poll();
            QUEUED.remove(k);
            int cx = class_1923.method_8325(k), cz = class_1923.method_8332(k);
            Object c = mc.field_1687.method_2935().method_2857(cx, cz, net.minecraft.class_2806.field_12803, false);
            if (!(c instanceof class_2818 chunk)) continue;
            DATA.put(k, scan(chunk, cx, cz, air));
        }
    }

    private static ChunkData scan(class_2818 chunk, int cx, int cz, boolean air) {
        ChunkData d = new ChunkData(cx, cz);
        d.scannedAt = System.currentTimeMillis();
        class_2826[] sections = chunk.method_12006();
        int minSec = chunk.method_32891();
        int bx = cx << 4, bz = cz << 4;
        for (int si = 0; si < sections.length; si++) {
            class_2826 sec = sections[si];
            if (sec == null || sec.method_38292()) continue;
            int by = (minSec + si) << 4;
            boolean interesting = sec.method_19523(st -> kindOf(((class_2680) st).method_26204()) != 0);
            boolean underground = air && by < 56;
            if (!interesting && !underground) continue;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        class_2680 st = sec.method_12254(x, y, z);
                        int bits = interesting ? kindOf(st.method_26204()) : 0;
                        if (bits != 0) add(d, st, bits, chunk, bx + x, by + y, bz + z);
                        else if (underground && st.method_26215() && (x & 1) == 0 && (z & 1) == 0) caveAir(d, chunk, bx + x, by + y, bz + z);
                    }
                }
            }
        }
        return d;
    }

    private static void add(ChunkData d, class_2680 st, int bits, class_2818 chunk, int x, int y, int z) {
        class_2338 pos = new class_2338(x, y, z);
        for (int k = 0; k < KINDS; k++) {
            if ((bits & (1 << k)) == 0) continue;
            if (k == KELP) {
                // only the top block of a kelp column counts: did it grow all the way up?
                class_2680 above = world.method_8320(pos.method_10084());
                String a = id(above.method_26204());
                if (a.equals("kelp") || a.equals("kelp_plant")) continue;
                d.hits[KELP].add(pos);
                if (!a.equals("water")) d.hits[KELP_TOP].add(pos);
                continue;
            }
            if (k == BERRY) {
                d.hits[BERRY].add(pos);
                if (intProp(st, "age") >= 3) d.hits[BERRY_RIPE].add(pos);
                continue;
            }
            if (k == PORTAL && id(st.method_26204()).equals("end_portal_frame") && !boolProp(st, "eye")) continue;
            if (k == LIGHT && id(st.method_26204()).equals("redstone_lamp") && !boolProp(st, "lit")) continue;
            d.hits[k].add(pos);
        }
    }

    /** Cave air: lit caves (block light, no sky) and straight 1x2 / 2x2 tunnels. */
    private static void caveAir(ChunkData d, class_2818 chunk, int x, int y, int z) {
        class_2338 pos = new class_2338(x, y, z);
        int sky = world.method_8314(class_1944.field_9284, pos);
        if (sky > 0) return;
        int block = world.method_8314(class_1944.field_9282, pos);
        if (block >= 9) d.hits[LIT_CAVE].add(pos);
        // tunnel: floor and roof solid, 2 high, walls on both sides, and the same for 8 blocks in a straight line
        if (isTunnelSlice(pos, true) && runLength(pos, true) >= 8) d.hits[TUNNEL].add(pos);
        else if (isTunnelSlice(pos, false) && runLength(pos, false) >= 8) d.hits[TUNNEL].add(pos);
    }

    private static boolean solid(int x, int y, int z) {
        class_2680 st = world.method_8320(new class_2338(x, y, z));
        return !st.method_26215() && st.method_26227().method_15769() && st.method_26216();
    }

    private static boolean airAt(int x, int y, int z) {
        return world.method_8320(new class_2338(x, y, z)).method_26215();
    }

    /** One slice of a tunnel running along X (alongX) or Z: air 2 high, solid floor/roof, solid walls left and right. */
    private static boolean isTunnelSlice(class_2338 p, boolean alongX) {
        int x = p.method_10263(), y = p.method_10264(), z = p.method_10260();
        if (!airAt(x, y, z) || !airAt(x, y + 1, z)) return false;
        if (!solid(x, y - 1, z) || !solid(x, y + 2, z)) return false;
        if (alongX) return solid(x, y, z - 1) && solid(x, y, z + 1) && solid(x, y + 1, z - 1) && solid(x, y + 1, z + 1);
        return solid(x - 1, y, z) && solid(x + 1, y, z) && solid(x - 1, y + 1, z) && solid(x + 1, y + 1, z);
    }

    private static int runLength(class_2338 p, boolean alongX) {
        int n = 1;
        for (int dir = -1; dir <= 1; dir += 2) {
            for (int i = 1; i < 12; i++) {
                class_2338 q = alongX ? p.method_10069(dir * i, 0, 0) : p.method_10069(0, 0, dir * i);
                if (!isTunnelSlice(q, alongX)) break;
                n++;
            }
        }
        return n;
    }

    // ------------------------------------------------------------------ drawing helpers
    static Color col(int argb, int alpha) {
        return new Color((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, Math.max(0, Math.min(255, alpha)));
    }

    static final class Draw {
        final List<RenderUtils.ColoredBox> boxes = new ArrayList<>();
        final List<RenderUtils.ColoredLine> lines = new ArrayList<>();

        void block(class_2338 p, Color c, int fillAlpha) {
            double x = p.method_10263(), y = p.method_10264(), z = p.method_10260();
            this.box(x + 0.02, y + 0.02, z + 0.02, x + 0.98, y + 0.98, z + 0.98, c, fillAlpha);
        }

        void box(double x1, double y1, double z1, double x2, double y2, double z2, Color c, int fillAlpha) {
            if (fillAlpha > 0) this.boxes.add(new RenderUtils.ColoredBox(x1, y1, z1, x2, y2, z2, new Color(c.getRed(), c.getGreen(), c.getBlue(), fillAlpha)));
            RenderUtils.addBoxOutline(this.lines, x1, y1, z1, x2, y2, z2, c, 1.6f);
        }

        /** A chunk drawn as a square at height y. */
        void chunk(int cx, int cz, double y, Color c, int fillAlpha) {
            double x1 = (cx << 4) + 0.05, z1 = (cz << 4) + 0.05, x2 = (cx << 4) + 15.95, z2 = (cz << 4) + 15.95;
            if (fillAlpha > 0) this.boxes.add(new RenderUtils.ColoredBox(x1, y, z1, x2, y + 0.03, z2, new Color(c.getRed(), c.getGreen(), c.getBlue(), fillAlpha)));
            class_243 a = new class_243(x1, y, z1), b = new class_243(x2, y, z1), e = new class_243(x2, y, z2), f = new class_243(x1, y, z2);
            this.lines.add(new RenderUtils.ColoredLine(a, b, c, 2.0f));
            this.lines.add(new RenderUtils.ColoredLine(b, e, c, 2.0f));
            this.lines.add(new RenderUtils.ColoredLine(e, f, c, 2.0f));
            this.lines.add(new RenderUtils.ColoredLine(f, a, c, 2.0f));
        }

        void tracer(class_243 to, Color c) {
            class_4184 cam = RenderUtils.getCamera();
            if (cam == null) return;
            this.lines.add(new RenderUtils.ColoredLine(RenderUtils.getTracerStart(cam), to, c, 1.2f));
        }

        void render(hook.event.events.Render3DEvent event, boolean throughWalls) {
            if (this.boxes.isEmpty() && this.lines.isEmpty()) return;
            class_4184 camera = RenderUtils.getCamera();
            if (camera == null || !camera.method_19332()) return;
            class_4587 m = event.matrixStack;
            m.method_22903();
            RenderUtils.applyCameraTransform(m, camera);
            RenderUtils.renderFilledBoxes(m, this.boxes);
            RenderUtils.renderLines(m, this.lines, throughWalls);
            m.method_22909();
        }
    }

    static double feetY() {
        class_746 p = class_310.method_1551().field_1724;
        return p == null ? 64 : Math.floor(p.method_23318()) + 0.05;
    }

    static double distSq(class_2338 p) {
        class_746 me = class_310.method_1551().field_1724;
        if (me == null) return 0;
        double dx = p.method_10263() + 0.5 - me.method_23317(), dy = p.method_10264() + 0.5 - me.method_23318(), dz = p.method_10260() + 0.5 - me.method_23321();
        return dx * dx + dy * dy + dz * dz;
    }

    static void chat(String prefix, String msg) {
        class_746 p = class_310.method_1551().field_1724;
        if (p != null) p.method_7353(net.minecraft.class_2561.method_43470("§6[" + prefix + "] §f" + msg), false);
    }
}
