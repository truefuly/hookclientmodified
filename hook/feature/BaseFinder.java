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
import net.minecraft.class_1297;
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
 * in it change), block by block, and sorted into kinds. The modules only read the results, so all of them cost one scan.
 *
 * <p>Natural generation is filtered out:
 * <ul>
 *   <li>Every "player looking" block that vanilla also generates somewhere (villages, trial chambers, ancient cities,
 *   mansions, bastions, end cities, igloos, trail ruins, temples, strongholds, mineshafts... - taken from the game's own
 *   structure files) is a <i>weak</i> find. Weak finds only count outside of structures.</li>
 *   <li>Structures are recognised by blocks only they contain (bell, tuff bricks, reinforced deepslate, gilded blackstone,
 *   purpur, infested stone, cobwebs, spawners, prismarine bricks...) and by the mobs that only spawn in them (illagers,
 *   brutes, shulkers, guardians, blazes...). Each one makes a zone of a few chunks in which weak finds are ignored.</li>
 *   <li>Blocks worldgen never places (enchanting tables, anvils, beacons, shulkers, observers, most concrete...) always
 *   count, even inside a structure - so a base built into a village is still found.</li>
 * </ul>
 */
public final class BaseFinder {
    private BaseFinder() {
    }

    // ------------------------------------------------------------------ kinds
    static final int BASE = 0, BASE_W = 1, STORAGE = 2, STORAGE_W = 3, LIGHT = 4, LIGHT_W = 5, PORTAL = 6, SIGN = 7,
            KELP_GROWN = 8, KELP = 9, AMETHYST_FULL = 10, AMETHYST_BUD = 11, BERRY_PICKED = 12, BERRY = 13, TUNNEL = 14,
            LIT_CAVE = 15, NATURAL_LIGHT = 16, STRUCT = 17, KINDS = 18;

    /** Blocks that look player-made. Whether one is "weak" depends on {@link #IN_STRUCTURES}. */
    private static final Set<String> PLAYER_BLOCKS = Set.of(
        "crafting_table", "furnace", "blast_furnace", "smoker", "enchanting_table", "anvil", "chipped_anvil", "damaged_anvil",
        "brewing_stand", "beacon", "grindstone", "smithing_table", "stonecutter", "loom", "cartography_table", "fletching_table",
        "lectern", "composter", "respawn_anchor", "lodestone", "conduit", "jukebox", "note_block", "piston", "sticky_piston",
        "observer", "dropper", "dispenser", "hopper", "comparator", "repeater", "redstone_lamp", "daylight_detector", "target",
        "lever", "tripwire_hook", "redstone_block", "slime_block", "honey_block", "tnt", "ender_chest", "netherite_block",
        "diamond_block", "emerald_block", "gold_block", "iron_block", "lapis_block", "copper_block", "coal_block", "rail",
        "powered_rail", "detector_rail", "activator_rail", "scaffolding", "crafter", "glass", "tinted_glass", "glass_pane",
        "flower_pot", "chiseled_bookshelf", "decorated_pot", "beehive", "cauldron", "water_cauldron", "lava_cauldron",
        "bell", "bookshelf", "redstone_wire", "hay_block", "shulker_box", "candle", "iron_door", "barrel", "chest",
        "trapped_chest", "bamboo_block", "dried_kelp_block");
    private static final String[] PLAYER_SUFFIX = {"_bed", "_concrete", "_concrete_powder", "_shulker_box", "_stained_glass",
        "_stained_glass_pane", "_glazed_terracotta", "_carpet", "_banner", "_candle"};
    /**
     * Player-looking blocks that vanilla structures also generate (read from the 1.21.11 structure files, plus the
     * structures that are built in code: temples, strongholds, mineshafts, fortresses, monuments, dungeons, swamp huts,
     * buried treasure, end ships).
     */
    private static final Set<String> IN_STRUCTURES = new HashSet<>(List.of((
        "barrel bell black_bed black_carpet black_glazed_terracotta black_stained_glass black_wall_banner blast_furnace blue_bed "
        + "blue_carpet bookshelf brewing_stand brown_bed brown_carpet brown_stained_glass brown_wall_banner campfire candle "
        + "cartography_table cauldron chest coal_block comparator composter copper_block crafting_table cyan_bed cyan_carpet "
        + "cyan_glazed_terracotta damaged_anvil decorated_pot diamond_block dispenser end_rod ender_chest fletching_table flower_pot "
        + "furnace glass glass_pane glowstone gold_block gray_bed gray_carpet gray_wall_banner green_bed green_carpet grindstone "
        + "hay_block hopper lantern lapis_block lectern lever light_blue_bed light_blue_carpet light_blue_glazed_terracotta "
        + "light_gray_bed light_gray_carpet light_gray_glazed_terracotta light_gray_stained_glass light_gray_wall_banner lime_bed "
        + "lime_carpet lime_glazed_terracotta loom magenta_bed magenta_carpet magenta_stained_glass magenta_wall_banner note_block "
        + "oak_wall_sign orange_bed orange_carpet orange_glazed_terracotta orange_stained_glass_pane pink_bed pink_carpet "
        + "purple_bed purple_carpet purple_glazed_terracotta rail red_bed red_candle red_carpet red_concrete red_glazed_terracotta "
        + "redstone_block redstone_lamp redstone_torch redstone_wall_torch redstone_wire repeater sea_lantern smithing_table smoker "
        + "soul_lantern spruce_wall_sign sticky_piston stonecutter target tnt torch trapped_chest tripwire_hook wall_torch "
        + "waxed_copper_bulb waxed_oxidized_copper_bulb white_bed white_candle white_carpet white_concrete white_glazed_terracotta "
        + "white_stained_glass white_stained_glass_pane white_wall_banner yellow_bed yellow_carpet yellow_glazed_terracotta "
        + "yellow_stained_glass_pane "
        // built in code
        + "iron_door soul_campfire water_cauldron").split(" ")));

    private static final Set<String> STORAGE_BLOCKS = Set.of("chest", "trapped_chest", "barrel", "ender_chest", "shulker_box",
        "hopper", "dropper", "dispenser", "furnace", "blast_furnace", "smoker", "brewing_stand", "crafter", "chiseled_bookshelf", "decorated_pot");
    /** Light a player places. */
    private static final Set<String> PLAYER_LIGHT = Set.of("torch", "wall_torch", "soul_torch", "soul_wall_torch", "redstone_torch",
        "redstone_wall_torch", "copper_torch", "copper_wall_torch", "lantern", "soul_lantern", "jack_o_lantern", "glowstone",
        "sea_lantern", "redstone_lamp", "end_rod", "beacon", "conduit", "ochre_froglight", "verdant_froglight", "pearlescent_froglight",
        "campfire", "soul_campfire", "candle", "copper_bulb", "exposed_copper_bulb", "weathered_copper_bulb", "oxidized_copper_bulb",
        "waxed_copper_bulb", "waxed_exposed_copper_bulb", "waxed_weathered_copper_bulb", "waxed_oxidized_copper_bulb", "copper_lantern",
        "exposed_copper_lantern", "weathered_copper_lantern", "oxidized_copper_lantern");
    /** Natural light that is only lit in some states (checked by the actual light emission). */
    private static final Set<String> NATURAL_LIGHT_EXTRA = Set.of("redstone_ore", "deepslate_redstone_ore", "cave_vines",
        "cave_vines_plant", "sea_pickle");

    /**
     * Blocks only structures generate, with the zone radius (in chunks) they mark as natural. Some need a minimum count
     * per chunk (see {@link #ZONE_MIN}).
     */
    private static final Map<String, Integer> ZONE = new HashMap<>();
    private static final Map<String, Integer> ZONE_MIN = new HashMap<>();
    /** Mobs that only spawn with structures, and the zone they mark. */
    private static final Map<String, Integer> ENTITY_ZONE_TYPES = Map.of("villager", 2, "vindicator", 4, "evoker", 4,
        "piglin_brute", 3, "shulker", 3, "elder_guardian", 4, "guardian", 3, "blaze", 2, "wither_skeleton", 2);
    static {
        // village (the bell is in the middle; villages reach ~5 chunks out)
        zone(5, "bell");
        // trial chambers
        zone(4, "trial_spawner", "vault");
        zone(3, "tuff_bricks", "chiseled_tuff", "chiseled_tuff_bricks", "polished_tuff", "waxed_copper_grate", "waxed_oxidized_copper_grate",
            "waxed_copper_bulb", "waxed_oxidized_copper_bulb", "waxed_chiseled_copper", "waxed_oxidized_chiseled_copper");
        // ancient city / deep dark
        zone(5, "reinforced_deepslate");
        zone(3, "sculk_shrieker", "sculk_catalyst", "deepslate_tiles", "cracked_deepslate_tiles", "chiseled_deepslate", "cracked_deepslate_bricks");
        zone(2, 6, "sculk", "sculk_vein");
        // stronghold
        zone(4, "end_portal_frame", "infested_stone_bricks", "infested_mossy_stone_bricks", "infested_cracked_stone_bricks", "infested_chiseled_stone_bricks");
        zone(3, 3, "mossy_stone_bricks", "cracked_stone_bricks");
        // mineshaft, dungeons, spawner rooms
        zone(3, 2, "cobweb");
        zone(2, "spawner", "mossy_cobblestone");
        // temples, outposts, wells, trail / ocean ruins
        zone(3, "suspicious_sand", "suspicious_gravel", "mud_bricks", "packed_mud", "mud_brick_slab", "mud_brick_stairs",
            "mossy_cobblestone_wall", "mossy_cobblestone_stairs", "mossy_cobblestone_slab", "chiseled_sandstone", "chiseled_stone_bricks");
        zone(4, "prismarine_bricks", "dark_prismarine");
        zone(2, 8, "prismarine");
        zone(1, "potted_red_mushroom"); // swamp hut
        zone(1, 20, "snow_block"); // igloo
        zone(2, "crying_obsidian"); // ruined portal
        zone(4, "infested_cobblestone"); // woodland mansion
        // nether
        zone(4, "gilded_blackstone");
        zone(3, "polished_blackstone_bricks", "cracked_polished_blackstone_bricks", "chiseled_polished_blackstone");
        zone(3, "nether_bricks", "nether_brick_fence", "nether_brick_stairs", "nether_wart");
        // end cities
        zone(4, "purpur_block", "purpur_pillar", "dragon_wall_head");
        zone(3, "end_stone_bricks", "purpur_stairs", "purpur_slab");
    }

    private static void zone(int radius, String... ids) {
        for (String s : ids) ZONE.put(s, radius);
    }

    private static void zone(int radius, int min, String... ids) {
        for (String s : ids) { ZONE.put(s, radius); ZONE_MIN.put(s, min); }
    }

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

    static boolean playerLooking(String id) {
        if (PLAYER_BLOCKS.contains(id)) return true;
        if (id.equals("moss_carpet") || id.equals("pale_moss_carpet")) return false;
        for (String suf : PLAYER_SUFFIX) if (id.endsWith(suf)) return true;
        return id.startsWith("potted_");
    }

    /** True for a block vanilla never generates in any structure. */
    static boolean strong(String id) {
        // structure processors randomise these (trial chambers: copper weathering, bed/candle colours)
        if (id.startsWith("waxed_") || id.endsWith("candle") || id.endsWith("_bed") || id.startsWith("potted_")) return false;
        return !IN_STRUCTURES.contains(id);
    }

    /** Blocks worldgen never places outside structures (player bases): a chunk containing one was visited. */
    static boolean playerBuilt(class_2248 b) {
        return (kindOf(b) & (1 << BASE | 1 << STORAGE | 1 << LIGHT)) != 0;
    }

    private static int kindOf(class_2248 b) {
        Integer k = KIND.get(b);
        if (k != null) return k;
        String id = id(b);
        int bits = 0;
        boolean strong = strong(id);
        boolean storage = STORAGE_BLOCKS.contains(id) || id.endsWith("_shulker_box");
        boolean light = PLAYER_LIGHT.contains(id) || id.endsWith("_candle");
        if (playerLooking(id) && !storage && !light) bits |= 1 << (strong ? BASE : BASE_W);
        if (storage) bits |= 1 << (strong ? STORAGE : STORAGE_W);
        if (light) bits |= 1 << (strong ? LIGHT : LIGHT_W);
        else {
            int emit = 0;
            try { emit = b.method_9564().method_26213(); } catch (Throwable ignored) { }
            if (emit > 0 || NATURAL_LIGHT_EXTRA.contains(id)) bits |= 1 << NATURAL_LIGHT;
        }
        if (id.equals("nether_portal") || id.equals("end_portal")) bits |= 1 << PORTAL;
        if (id.endsWith("_sign")) bits |= 1 << SIGN;
        if (id.equals("kelp")) bits |= 1 << KELP;
        if (id.equals("amethyst_cluster")) bits |= 1 << AMETHYST_FULL;
        if (id.endsWith("_amethyst_bud")) bits |= 1 << AMETHYST_BUD;
        if (id.equals("sweet_berry_bush")) bits |= 1 << BERRY;
        if (ZONE.containsKey(id)) bits |= 1 << STRUCT;
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
        /** Zone radius this chunk's own structure blocks mark (-1 = none) and which block did it. */
        int zone = -1;
        String zoneBlock;
        /** Kelp heads that cannot grow any more (blocked) and never grew: worldgen leftovers. */
        int kelpStuck;
        /** Too much natural light to list (lava lakes): treat every lit spot nearby as natural. */
        boolean naturalFlood;
        // caches (re-done when any zone changes)
        int zoneStamp = -1;
        String inZone;
        int scoreStamp = -1;
        int score;
        int distinct;
        boolean settled;
        Boolean litVerdict;

        ChunkData(int cx, int cz) {
            this.cx = cx;
            this.cz = cz;
            for (int i = 0; i < KINDS; i++) this.hits[i] = new ArrayList<>();
        }

        int count(int kind) {
            return this.hits[kind].size();
        }

        int centerX() {
            return (this.cx << 4) + 8;
        }

        int centerZ() {
            return (this.cz << 4) + 8;
        }
    }

    private static final Map<Long, ChunkData> DATA = new HashMap<>();
    private static final ArrayDeque<Long> QUEUE = new ArrayDeque<>();
    private static final Set<Long> QUEUED = new HashSet<>();
    private static final Set<Long> DIRTY = new HashSet<>();
    /** Structure mobs seen: chunk -> radius / name. Kept until the world changes. */
    private static final Map<Long, Integer> MOB_ZONE = new HashMap<>();
    private static final Map<Long, String> MOB_ZONE_NAME = new HashMap<>();
    private static class_638 world;
    private static String dim = "overworld";
    private static int lastTick = Integer.MIN_VALUE;
    private static boolean wantAir;
    /** Bumped whenever a structure zone appears or disappears. */
    private static int zoneVersion;
    private static final int MAX_ZONE = 5;

    static long key(int cx, int cz) {
        return class_1923.method_8331(cx, cz);
    }

    static ChunkData get(int cx, int cz) {
        return DATA.get(key(cx, cz));
    }

    static Iterable<ChunkData> all() {
        return DATA.values();
    }

    static String dimension() {
        return dim;
    }

    static boolean overworld() {
        return dim.equals("overworld");
    }

    /**
     * Name of the structure block/mob that makes this chunk part of a natural structure, or null. Also counts the End's
     * main island (exit portal, pillars, gateways, crystals).
     */
    static String naturalZone(int cx, int cz) {
        ChunkData cd = get(cx, cz);
        if (cd != null && cd.zoneStamp == zoneVersion) return cd.inZone;
        String found = null;
        if (dim.equals("the_end") && Math.abs(cx) <= 12 && Math.abs(cz) <= 12) found = "end main island";
        for (int dx = -MAX_ZONE; dx <= MAX_ZONE && found == null; dx++) {
            for (int dz = -MAX_ZONE; dz <= MAX_ZONE; dz++) {
                int ring = Math.max(Math.abs(dx), Math.abs(dz));
                long k = key(cx + dx, cz + dz);
                ChunkData n = DATA.get(k);
                if (n != null && n.zone >= ring) { found = n.zoneBlock; break; }
                Integer mob = MOB_ZONE.get(k);
                if (mob != null && mob >= ring) { found = MOB_ZONE_NAME.get(k); break; }
            }
        }
        if (cd != null) { cd.zoneStamp = zoneVersion; cd.inZone = found; }
        return found;
    }

    static boolean natural(ChunkData cd) {
        return naturalZone(cd.cx, cd.cz) != null;
    }

    static boolean naturalAt(double x, double z) {
        return naturalZone((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4) != null;
    }

    /** Every chunk that could put this one into a structure zone has been scanned (or is not loaded): verdicts are final. */
    static boolean settled(ChunkData cd) {
        if (cd.settled) return true;
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null) return false;
        for (int dx = -MAX_ZONE; dx <= MAX_ZONE; dx++) {
            for (int dz = -MAX_ZONE; dz <= MAX_ZONE; dz++) {
                if (DATA.containsKey(key(cd.cx + dx, cd.cz + dz))) continue;
                if (mc.field_1687.method_2935().method_2857(cd.cx + dx, cd.cz + dz, net.minecraft.class_2806.field_12803, false) != null) return false;
            }
        }
        cd.settled = true;
        return true;
    }

    /** Hits of a strong kind plus its weak twin when the chunk is settled and not part of a structure. */
    static List<class_2338> finds(ChunkData cd, int strongKind, int weakKind) {
        if (cd.hits[weakKind].isEmpty() || !settled(cd) || natural(cd)) return cd.hits[strongKind];
        if (cd.hits[strongKind].isEmpty()) return cd.hits[weakKind];
        List<class_2338> all = new ArrayList<>(cd.hits[strongKind]);
        all.addAll(cd.hits[weakKind]);
        return all;
    }

    /**
     * Base score: strong blocks count 2, weak ones 1 (only outside structures), plus 1 per different block type - a real
     * base has many kinds of things; one long glass wall or a rail line alone is capped.
     */
    static int baseScore(ChunkData cd) {
        if (cd.scoreStamp == zoneVersion) return cd.score;
        boolean weakOk = settled(cd) && !natural(cd);
        Map<String, Integer> types = new HashMap<>();
        int[] strongKinds = {BASE, STORAGE, LIGHT};
        int[] weakKinds = {BASE_W, STORAGE_W, LIGHT_W};
        for (int i = 0; i < 3; i++) {
            for (class_2338 p : cd.hits[strongKinds[i]]) types.merge(id(world.method_8320(p).method_26204()), i == 2 ? 1 : 2, Integer::sum);
            if (!weakOk) continue;
            for (class_2338 p : cd.hits[weakKinds[i]]) types.merge(id(world.method_8320(p).method_26204()), 1, Integer::sum);
        }
        int s = 0;
        for (int v : types.values()) s += Math.min(v, 8); // one block type repeated counts at most 8
        s += types.size();
        if (weakOk) s += cd.count(SIGN) * 2;
        cd.distinct = types.size();
        cd.score = s;
        cd.scoreStamp = settled(cd) ? zoneVersion : -1;
        return s;
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

    static String entityId(class_1297 e) {
        try { return class_7923.field_41177.method_10221(e.method_5864()).method_12832(); } catch (Throwable t) { return "?"; }
    }

    /** Walled in on all four sides (a trading-hall cell, a boat, a minecart...). */
    static boolean enclosed(class_1297 e) {
        if (world == null) return false;
        class_2338 p = new class_2338((int) Math.floor(e.method_23317()), (int) Math.floor(e.method_23318()), (int) Math.floor(e.method_23321()));
        if (e.method_5765()) return true;
        int walls = 0;
        for (class_2338 n : new class_2338[]{p.method_10095(), p.method_10072(), p.method_10078(), p.method_10067()}) {
            class_2680 st = world.method_8320(n);
            if (!st.method_26215() && st.method_26227().method_15769()) walls++;
        }
        return walls >= 3;
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
            try { dim = world.method_27983().method_29177().method_12832(); } catch (Throwable t) { dim = "overworld"; }
            DATA.clear();
            QUEUE.clear();
            QUEUED.clear();
            MOB_ZONE.clear();
            MOB_ZONE_NAME.clear();
            zoneVersion++;
            synchronized (DIRTY) { DIRTY.clear(); }
        }
        int pcx = (int) Math.floor(p.method_23317()) >> 4, pcz = (int) Math.floor(p.method_23321()) >> 4;
        int r = Math.max(2, mc.field_1690.method_38521()) + 1;
        // drop chunks far away
        if (p.field_6012 % 40 == 0 && DATA.values().removeIf(d -> Math.abs(d.cx - pcx) > r + 2 || Math.abs(d.cz - pcz) > r + 2)) {
            MOB_ZONE.keySet().removeIf(k -> Math.abs(class_1923.method_8325(k) - pcx) > r + 8 || Math.abs(class_1923.method_8332(k) - pcz) > r + 8);
            zoneVersion++;
        }
        // structure mobs
        if (p.field_6012 % 20 == 0) {
            for (class_1297 e : mc.field_1687.method_18112()) {
                String t = entityId(e);
                Integer zr = ENTITY_ZONE_TYPES.get(t);
                if (zr == null) continue;
                if (t.equals("villager") && enclosed(e)) continue; // trading hall villagers are no village
                long k = key((int) Math.floor(e.method_23317()) >> 4, (int) Math.floor(e.method_23321()) >> 4);
                Integer old = MOB_ZONE.get(k);
                if (old == null || old < zr) { MOB_ZONE.put(k, zr); MOB_ZONE_NAME.put(k, t); zoneVersion++; }
            }
        }
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
            ChunkData old = DATA.get(k);
            ChunkData d = scan(chunk, cx, cz, air);
            DATA.put(k, d);
            if (d.zone >= 0 || (old != null && old.zone >= 0)) zoneVersion++;
        }
    }

    private static ChunkData scan(class_2818 chunk, int cx, int cz, boolean air) {
        ChunkData d = new ChunkData(cx, cz);
        d.scannedAt = System.currentTimeMillis();
        class_2826[] sections = chunk.method_12006();
        int minSec = chunk.method_32891();
        int bx = cx << 4, bz = cz << 4;
        boolean nether = dim.equals("the_nether");
        Map<String, Integer> zoneCounts = new HashMap<>();
        for (int si = 0; si < sections.length; si++) {
            class_2826 sec = sections[si];
            if (sec == null || sec.method_38292()) continue;
            int by = (minSec + si) << 4;
            boolean interesting = sec.method_19523(st -> kindOf(((class_2680) st).method_26204()) != 0);
            // caves: overworld below the surface, and the whole nether (highways, roof tunnels)
            boolean underground = air && (nether || (overworld() && by < 56));
            if (!interesting && !underground) continue;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        class_2680 st = sec.method_12254(x, y, z);
                        int bits = interesting ? kindOf(st.method_26204()) : 0;
                        if (bits != 0) add(d, st, bits, bx + x, by + y, bz + z, zoneCounts);
                        else if (underground && ((x + z) & 1) == 0 && st.method_26215()) caveAir(d, bx + x, by + y, bz + z, nether);
                    }
                }
            }
        }
        for (Map.Entry<String, Integer> e : zoneCounts.entrySet()) {
            int r = ZONE.get(e.getKey());
            if (e.getValue() >= ZONE_MIN.getOrDefault(e.getKey(), 1) && r > d.zone) { d.zone = r; d.zoneBlock = e.getKey().replace('_', ' '); }
        }
        if (d.hits[NATURAL_LIGHT].size() > 3000) { d.naturalFlood = true; d.hits[NATURAL_LIGHT].subList(3000, d.hits[NATURAL_LIGHT].size()).clear(); }
        return d;
    }

    private static void add(ChunkData d, class_2680 st, int bits, int x, int y, int z, Map<String, Integer> zoneCounts) {
        class_2338 pos = new class_2338(x, y, z);
        String id = id(st.method_26204());
        if ((bits & (1 << STRUCT)) != 0) zoneCounts.merge(id, 1, Integer::sum);
        for (int k = 0; k < KINDS; k++) {
            if ((bits & (1 << k)) == 0 || k == STRUCT) continue;
            switch (k) {
                case KELP -> {
                    // the kelp head. Worldgen kelp is age 20-23; it only gets older by growing, which needs a player nearby.
                    int age = intProp(st, "age");
                    boolean blocked = !id(world.method_8320(pos.method_10084()).method_26204()).equals("water");
                    d.hits[KELP].add(pos);
                    if (age >= 24) d.hits[KELP_GROWN].add(pos);
                    else if (blocked) d.kelpStuck++;
                }
                case BERRY -> {
                    // worldgen bushes are all fully grown (age 3); a younger one was picked or planted
                    d.hits[BERRY].add(pos);
                    if (intProp(st, "age") < 3) d.hits[BERRY_PICKED].add(pos);
                }
                case STORAGE, STORAGE_W -> {
                    if (naturalContainer(st, id, pos)) continue;
                    d.hits[k].add(pos);
                }
                case LIGHT, LIGHT_W -> {
                    if (id.equals("glowstone") && !overworld()) { d.hits[NATURAL_LIGHT].add(pos); continue; } // nether ceilings
                    if ((id.equals("redstone_lamp") || id.endsWith("candle") || id.endsWith("campfire") || id.endsWith("copper_bulb")) && !boolProp(st, "lit")) {
                        d.hits[k == LIGHT ? BASE : BASE_W].add(pos);
                        continue;
                    }
                    d.hits[k].add(pos);
                }
                case NATURAL_LIGHT -> {
                    if (st.method_26213() > 0) d.hits[NATURAL_LIGHT].add(pos);
                }
                case PORTAL -> {
                    // the End's exit portal is natural
                    if (id.equals("end_portal") && dim.equals("the_end") && Math.abs(x) < 64 && Math.abs(z) < 64) continue;
                    d.hits[PORTAL].add(pos);
                }
                default -> d.hits[k].add(pos);
            }
        }
    }

    /** Shipwreck / ocean ruin chests (underwater) and buried treasure (sealed in sand, nothing above to open it). */
    private static boolean naturalContainer(class_2680 st, String id, class_2338 pos) {
        if (!id.equals("chest") && !id.equals("barrel") && !id.equals("trapped_chest")) return false;
        if (boolProp(st, "waterlogged")) return true;
        String above = id(world.method_8320(pos.method_10084()).method_26204());
        if (id.equals("chest") && (above.equals("sand") || above.equals("sandstone") || above.equals("gravel") || above.equals("red_sand")
                || above.equals("suspicious_sand") || above.equals("suspicious_gravel"))) return true;
        // a container nobody can open: sealed in natural ground on every side (buried loot)
        int sealed = 0;
        for (class_2338 n : new class_2338[]{pos.method_10084(), pos.method_10074(), pos.method_10095(), pos.method_10072(), pos.method_10078(), pos.method_10067()}) {
            String s = id(world.method_8320(n).method_26204());
            if (s.equals("sand") || s.equals("sandstone") || s.equals("gravel") || s.equals("stone") || s.equals("dirt") || s.equals("water")) sealed++;
        }
        return sealed == 6;
    }

    /** Cave air: lit caves (block light, no sky) and straight tunnels. */
    private static void caveAir(ChunkData d, int x, int y, int z, boolean nether) {
        class_2338 pos = new class_2338(x, y, z);
        if (!nether) {
            int sky = world.method_8314(class_1944.field_9284, pos);
            if (sky > 0) return;
            int block = world.method_8314(class_1944.field_9282, pos);
            if (block >= 9) d.hits[LIT_CAVE].add(pos);
        }
        for (int w = 1; w <= 3; w++) {
            for (int h = 2; h <= 3; h++) {
                int need = w == 1 && h == 2 ? 8 : 12;
                if (tunnel(pos, true, w, h, need) || tunnel(pos, false, w, h, need)) { d.hits[TUNNEL].add(pos); return; }
            }
        }
    }

    private static boolean solid(int x, int y, int z) {
        class_2680 st = world.method_8320(new class_2338(x, y, z));
        return !st.method_26215() && st.method_26227().method_15769() && st.method_26216();
    }

    private static boolean airAt(int x, int y, int z) {
        return world.method_8320(new class_2338(x, y, z)).method_26215();
    }

    /**
     * One cross-section of a tunnel w wide and h high whose first column is (x, y, z), running along X (alongX) or Z:
     * all air inside, solid floor, roof and walls - exactly that size, no bigger.
     */
    private static boolean slice(int x, int y, int z, boolean alongX, int w, int h) {
        int sx = alongX ? 0 : 1, sz = alongX ? 1 : 0; // sideways step
        if (!solid(x - sx, y, z - sz) || !solid(x + sx * w, y, z + sz * w)) return false;
        for (int i = 0; i < w; i++) {
            int cx = x + sx * i, cz = z + sz * i;
            if (!solid(cx, y - 1, cz) || !solid(cx, y + h, cz)) return false;
            for (int j = 0; j < h; j++) if (!airAt(cx, y + j, cz)) return false;
        }
        for (int j = 1; j < h; j++) if (!solid(x - sx, y + j, z - sz) || !solid(x + sx * w, y + j, z + sz * w)) return false;
        return true;
    }

    private static boolean tunnel(class_2338 p, boolean alongX, int w, int h, int need) {
        int x = p.method_10263(), y = p.method_10264(), z = p.method_10260();
        if (!slice(x, y, z, alongX, w, h)) return false;
        int n = 1;
        for (int dir = -1; dir <= 1 && n < need; dir += 2) {
            for (int i = 1; i < need && n < need; i++) {
                if (!slice(x + (alongX ? dir * i : 0), y, z + (alongX ? 0 : dir * i), alongX, w, h)) break;
                n++;
            }
        }
        return n >= need;
    }

    /**
     * Is this cave chunk lit by something a player placed? Every lit spot is checked against every natural light source
     * in reach (light falls off by one per block); only spots nothing natural explains count. Cached once the chunk and
     * its neighbours are scanned.
     */
    static boolean litByPlayer(ChunkData cd, int minSpots) {
        if (cd.count(LIT_CAVE) < minSpots) return false;
        if (cd.litVerdict != null) return cd.litVerdict;
        List<ChunkData> around = new ArrayList<>();
        boolean complete = true;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            ChunkData n = get(cd.cx + dx, cd.cz + dz);
            if (n == null) { complete = false; continue; }
            if (n.naturalFlood) { cd.litVerdict = false; return false; }
            around.add(n);
        }
        if (!complete || !settled(cd)) return false;
        if (natural(cd)) { cd.litVerdict = false; return false; }
        int unexplained = 0;
        for (class_2338 p : cd.hits[LIT_CAVE]) {
            int level = world.method_8314(class_1944.field_9282, p);
            boolean explained = false;
            for (ChunkData n : around) {
                for (class_2338 q : n.hits[NATURAL_LIGHT]) {
                    int dist = Math.abs(q.method_10263() - p.method_10263()) + Math.abs(q.method_10264() - p.method_10264()) + Math.abs(q.method_10260() - p.method_10260());
                    if (dist > 15) continue;
                    if (world.method_8320(q).method_26213() - dist >= level) { explained = true; break; }
                }
                if (explained) break;
            }
            if (!explained) unexplained++;
        }
        cd.litVerdict = unexplained >= minSpots;
        return cd.litVerdict;
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

        /**
         * One chunk of a connected area at height y: seamless fill, and an outline only on the sides where the
         * neighbour is not part of the same area (north, east, south, west), so touching chunks read as one shape.
         */
        void region(int cx, int cz, double y, Color c, int fillAlpha, boolean n, boolean e, boolean s, boolean w) {
            double x1 = cx << 4, z1 = cz << 4, x2 = x1 + 16, z2 = z1 + 16;
            if (fillAlpha > 0) this.boxes.add(new RenderUtils.ColoredBox(x1, y, z1, x2, y + 0.01, z2, new Color(c.getRed(), c.getGreen(), c.getBlue(), fillAlpha)));
            double yy = y + 0.015;
            if (!n) this.lines.add(new RenderUtils.ColoredLine(new class_243(x1, yy, z1), new class_243(x2, yy, z1), c, 2.0f));
            if (!e) this.lines.add(new RenderUtils.ColoredLine(new class_243(x2, yy, z1), new class_243(x2, yy, z2), c, 2.0f));
            if (!s) this.lines.add(new RenderUtils.ColoredLine(new class_243(x2, yy, z2), new class_243(x1, yy, z2), c, 2.0f));
            if (!w) this.lines.add(new RenderUtils.ColoredLine(new class_243(x1, yy, z2), new class_243(x1, yy, z1), c, 2.0f));
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
