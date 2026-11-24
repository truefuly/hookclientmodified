package hook.feature;

import hook.utils.InventoryUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1701;
import net.minecraft.class_243;
import net.minecraft.class_310;

/**
 * Developer self-test (only with -Dhook.carttest): runs Xbow Cart and Bow Cart in a singleplayer world from many
 * distances / directions and reads the integrated server to see whether the TNT minecart really blew up instantly.
 */
final class CartTest {
    static final boolean ENABLED = System.getProperty("hook.carttest") != null;

    private static final String ONLY = System.getProperty("hook.carttest.only");
    private record Trial(String kind, int dist, float yaw, int height) {}

    private static final List<Trial> TRIALS = new ArrayList<>();
    static {
        float[] yaws = {0, 45, 90, 180, -30, -135};
        for (String k : new String[]{"xbow", "bowflame", "bownorm", "bowup", "xbowmob", "bowmob"})
            for (int d : new int[]{2, 3, 4})
                for (float y : yaws)
                    for (int h : k.equals("bowflame") ? new int[]{0, 1, 3} : k.equals("xbow") ? new int[]{0, 1} : new int[]{0})
                        if ((k.equals("xbow") || k.equals("bowflame") || k.endsWith("mob") || d == 3) && (!k.endsWith("mob") || h == 0) && (!k.equals("bowup") || y == 0) && (ONLY == null || java.util.Arrays.asList(ONLY.split(",")).contains(k))) TRIALS.add(new Trial(k, d, y, h));
    }

    private static int lastAge = -1, clock, trial = -1, phase;
    private static int ok, total;
    static volatile String lastFail;
    static volatile int explosions;

    // server observation
    private static volatile int cartSeenAt = -1, cartGoneAt = -1;
    private static volatile boolean cartNow;
    static volatile String planInfo, fireResult, stepLog;
    static volatile net.minecraft.class_2338 planFire;
    static volatile net.minecraft.class_2338 planRail;
    private static volatile boolean railGone, railWas, railNow;
    private static volatile boolean arrowSeen, arrowBurning, fireSeen, railSeen;

    static void tick() {
        class_310 mc = class_310.method_1551();
        var p = mc.field_1724;
        if (p == null || mc.method_1576() == null) return;
        if (p.field_6012 == lastAge || phase == 99) return;
        lastAge = p.field_6012;
        clock++;
        if ("slime".equals(ONLY)) { slime(mc, p); return; }
        if ("creative".equals(ONLY)) { creative(mc, p); return; }
        if ("basefinder".equals(ONLY)) { basefinder(mc, p); return; }
        if ("fog".equals(ONLY)) { fog(mc, p); return; }
        if ("hud".equals(ONLY)) { hud(mc, p); return; }
        if ("newchunks".equals(ONLY)) { newChunks(mc, p); return; }
        if ("builder".equals(ONLY)) { builder(mc, p); return; }
        if ("seedcracker".equals(ONLY)) { seedcracker(mc, p); return; }
        if (clock == 100) {
            cmd("gamemode creative");
            cmd("effect give @s resistance infinite 255 true");
            cmd("time set day");
            cmd("gamerule doFireTick false");
            cmd("gamerule tnt_explodes " + !Boolean.getBoolean("hook.carttest.notnt")); // the world remembers it
            phase = 0;
            return;
        }
        if (clock < 140) return;
        if (phase == 0) {
            trial++;
            if (trial >= TRIALS.size()) {
                System.out.println("CARTTEST DONE ok=" + ok + "/" + total);
                mc.method_1592();
                phase = 99;
                return;
            }
            Trial t = TRIALS.get(trial);
            cmd("kill @e[type=!player]");
            cmd("fill -10 151 -10 10 158 10 air");
            cmd("fill -10 150 -10 10 150 10 obsidian");
            cmd("item replace entity @s hotbar.0 with crossbow[charged_projectiles=[{id:\"minecraft:arrow\",count:1}]]");
            cmd("item replace entity @s hotbar.1 with rail 64");
            cmd("item replace entity @s hotbar.2 with tnt_minecart 1");
            cmd(t.kind.equals("bow") ? "item replace entity @s hotbar.3 with flint_and_steel" : t.kind.startsWith("xbow") ? "item replace entity @s hotbar.3 with flint_and_steel" : "item replace entity @s hotbar.3 with air");
            cmd(t.kind.equals("bownorm") ? "item replace entity @s hotbar.4 with bow" : "item replace entity @s hotbar.4 with bow[enchantments={flame:1}]");
            cmd("item replace entity @s inventory.0 with arrow 64");
            cmd("item replace entity @s hotbar.5 with cobblestone 64");
            double r = Math.toRadians(t.yaw);
            int tx = (int) Math.floor(0.5 - Math.sin(r) * t.dist), tz = (int) Math.floor(0.5 + Math.cos(r) * t.dist);
            if (t.height >= 1) cmd("setblock " + tx + " 151 " + tz + " obsidian");
            // an enemy standing right on the spot (rails and carts can go under / next to players in vanilla)
            if (t.kind.endsWith("mob")) cmd("summon zombie " + (tx + 0.5) + " 151 " + (tz + 0.5) + " {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            cmd("tp @s 0.5 151 0.5 " + t.yaw + " 0");
            phase = 1;
            clock = 140;
            return;
        }
        Trial t = TRIALS.get(trial);
        if (phase == 1 && clock >= 140 + 25) {
            double r = Math.toRadians(t.yaw);
            int tx = (int) Math.floor(0.5 - Math.sin(r) * t.dist), tz = (int) Math.floor(0.5 + Math.cos(r) * t.dist);
            // bow at a raised spot: aim where the cart will stand (that's where you'd aim to hit it)
            // h=3: raised block too, but aimed exactly at its top centre
            double up = t.kind.startsWith("bow") && t.height == 1 ? 0.45 : 0.0;
            class_243 target = new class_243(tx + 0.5, 151 + Math.min(t.height, 1) + up, tz + 0.5);
            float[] a = CartPlanner.aimAt(p.method_33571(), target);
            p.method_36456(a[0]);
            p.method_36457(t.kind.equals("bowup") ? -60.0f : a[1]);
            lastFail = null;
            planInfo = null;
            stepLog = null;
            planRail = null;
            explosions = 0;
            planFire = null;
            fireResult = null;
            mc.method_1576().execute(CartTest::resetObserve);
            if (t.kind.startsWith("xbow")) {
                InventoryUtil.swap(0);
                phase = 2; // let the crosshair update for a tick
            } else {
                InventoryUtil.swap(4);
                mc.field_1690.field_1904.method_23481(true);
                mc.field_1761.method_2919((class_1657) p, class_1268.field_5808);
                phase = 3;
            }
            clock = 200;
            return;
        }
        if (phase == 2 || phase == 3) mc.method_1576().execute(CartTest::observe);
        if (phase == 2 && clock >= 202) {
            XbowCartModule m = XbowCartModule.instance;
            boolean handled = m != null && m.tryStart();
            if (!handled) lastFail = "not handled (crosshair " + mc.field_1765 + ")";
            phase = 4;
            clock = 300;
            return;
        }
        if (phase == 3) {
            // keep aiming while drawing; release after 22 ticks (full power)
            if (clock >= 200 + 22) {
                mc.field_1690.field_1904.method_23481(false); // vanilla releases the bow next input pass
                phase = 4;
                clock = 300;
            }
            return;
        }
        if (phase == 4) {
            mc.method_1576().execute(CartTest::observe);
            if (clock >= 300 + (t.kind.startsWith("xbow") ? 90 : 30)) {
                String res;
                if (explosions > 0 && (cartSeenAt < 0 || cartGoneAt >= 0 && cartGoneAt - cartSeenAt <= 6)) res = "BOOM";
                else if (shotAt >= 0 && cartGoneAt >= 0 && cartGoneAt - shotAt <= 6) res = "BOOM(shot+" + (cartGoneAt - shotAt) + "t, seq " + (shotAt - startAt) + "t)";
                else if (cartSeenAt < 0) res = "NOCART";
                else if (cartGoneAt >= 0 && cartGoneAt - cartSeenAt <= 6) res = "BOOM(" + (cartGoneAt - cartSeenAt) + "t)";
                else if (cartGoneAt >= 0) res = "LATE(" + (cartGoneAt - cartSeenAt) + "t)";
                else res = "MISS";
                total++;
                boolean expectNothing = t.kind.equals("bownorm") || t.kind.equals("bowup");
                if (expectNothing) res = (cartSeenAt < 0 && explosions == 0 && planInfo == null ? "NOTHING(ok)" : "PLACED(bad) " + res);
                if (res.startsWith("BOOM") || res.startsWith("NOTHING")) ok++;
                System.out.println("CARTTEST " + t.kind + " d=" + t.dist + " yaw=" + t.yaw + " h=" + t.height + " -> " + res + " steps[" + stepLog + "]" + (lastFail != null ? " fail=" + lastFail : "") + " arrow=" + arrowSeen + " burning=" + arrowBurning + " fire=" + fireSeen + " plan=" + planInfo + " fireUse=" + fireResult + " railGone=" + railGone);
                phase = 0;
                clock = 140;
            }
        }
    }

    private static volatile int shotAt = -1, startAt = -1;

    /** Xbow Cart fired the crossbow (humanized sequence): explosion is measured from here. */
    static void shot() {
        var server = class_310.method_1551().method_1576();
        if (server != null) server.execute(() -> shotAt = server.method_3780());
    }

    private static void resetObserve() {
        shotAt = -1;
        startAt = class_310.method_1551().method_1576().method_3780();
        cartSeenAt = -1;
        cartGoneAt = -1;
        cartNow = false;
        arrowSeen = arrowBurning = fireSeen = railSeen = false;
        railGone = railWas = false;
    }

    private static void observe() {
        var server = class_310.method_1551().method_1576();
        if (server == null) return;
        boolean any = false;
        var lvl = server.method_30002();
        for (class_1297 e : lvl.method_27909()) {
            if (e instanceof class_1701) any = true;
            if (e instanceof net.minecraft.class_1665) { arrowSeen = true; if (e.method_5809()) arrowBurning = true; }
        }
        var pr = planRail;
        if (pr != null) {
            boolean isRail = lvl.method_8320(pr).method_26204() instanceof net.minecraft.class_2241;
            railNow = isRail;
            if (isRail) railWas = true;
            else if (railWas) railGone = true;
        }
        var pf = planFire;
        if (pf != null && lvl.method_8320(pf).method_26204() instanceof net.minecraft.class_4770) fireSeen = true;
        int now = server.method_3780();
        if (any && cartSeenAt < 0) cartSeenAt = now;
        if (!any && cartSeenAt >= 0 && cartGoneAt < 0) cartGoneAt = now;
        cartNow = any;
    }

    /** -Dhook.carttest.only=slime: Slime Chunks module from above, plus a check of the formula against the seed. */
    private static void slime(class_310 mc, net.minecraft.class_746 p) {
        if (clock == 100) {
            cmd("gamemode spectator");
            cmd("time set day");
            cmd("tp @s 8 152 40 0 35");
            hook.module.Module m = hook.Hook.INSTANCE.getModuleManager().getModuleByClass(SlimeChunkModule.class);
            if (m != null && !m.isEnabled()) m.setEnabled(true);
        }
        if (clock == 140) {
            long seed = mc.method_1576().method_30002().method_8412();
            StringBuilder sb = new StringBuilder();
            int n = 0;
            for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++) if (SlimeChunkModule.isSlimeChunk(seed, x, z)) { n++; if (sb.length() < 300) sb.append(x).append(',').append(z).append(' '); }
            System.out.println("CARTTEST SLIME seed=" + seed + " count=" + n + "/169 chunks: " + sb);
            System.out.println("CARTTEST SLIME ref seed0 (0,0)=" + SlimeChunkModule.isSlimeChunk(0L, 0, 0) + " seed12345 list=" + refList(12345L));
        }
        if (clock == 170) net.minecraft.class_318.method_22690(mc.field_1697, "slime_top.png", mc.method_1522(), 1, msg -> {});
        if (clock == 190) { System.out.println("CARTTEST DONE ok=1/1"); mc.method_1592(); phase = 99; }
    }

    /** -Dhook.carttest.only=creative: fly in creative straight into an obsidian wall with Fly Through on. */
    private static void creative(class_310 mc, net.minecraft.class_746 p) {
        if (clock == 100) {
            cmd("gamemode creative");
            cmd("time set day");
            cmd("fill -3 150 3 3 156 9 obsidian");
            cmd("tp @s 0.5 152 0.5 0 0");
            hook.module.Module m = hook.Hook.INSTANCE.getModuleManager().getModuleByClass(CreativeModule.class);
            if (m != null && !m.isEnabled()) m.setEnabled(true);
        }
        if (clock == 120) { p.method_31549().field_7479 = true; p.method_7355(); }
        if (clock >= 125 && clock < 175) {
            p.method_31549().field_7479 = true;
            p.method_18800(0.0, 0.0, 0.25); // fly south into the wall (z 3..9)
        }
        if (clock == 150) net.minecraft.class_318.method_22690(mc.field_1697, "creative_inside.png", mc.method_1522(), 1, msg -> {});
        if (clock == 150 || clock == 180 || clock == 200) {
            var sp = mc.method_1576().method_3760().method_14602(p.method_5667());
            System.out.println("CARTTEST CREATIVE t=" + clock + " client z=" + String.format("%.2f", p.method_23321()) + " server z=" + (sp == null ? "?" : String.format("%.2f", sp.method_23321())) + " noPhysics=" + p.field_5960 + " flySpeed=" + p.method_31549().method_7252());
        }
        if (clock == 210) { System.out.println("CARTTEST DONE ok=1/1"); mc.method_1592(); phase = 99; }
    }

    /**
     * -Dhook.carttest.only=basefinder: a fake base, real vanilla structures (village, trial chambers, igloo, temples,
     * swamp hut, outpost, shipwreck, ruined portal, mineshaft), grown kelp and a grown geode; then what got flagged.
     */
    private static final String[][] STRUCTS = {{"village_plains", "-110", "20"}, {"trial_chambers", "20", "-120"}, {"igloo", "130", "20"},
        {"desert_pyramid", "130", "130"}, {"swamp_hut", "-110", "130"}, {"pillager_outpost", "20", "150"}, {"jungle_pyramid", "-110", "-110"},
        {"shipwreck", "130", "-110"}, {"ruined_portal", "150", "-20"}, {"mineshaft", "-150", "-20"}, {"village_desert", "-20", "-170"}};

    private static void basefinder(class_310 mc, net.minecraft.class_746 p) {
        if (clock == 100) {
            cmd("gamemode creative");
            cmd("time set day");
            cmd("kill @e[type=!player]");
            for (String[] st : STRUCTS) cmd("place structure minecraft:" + st[0] + " " + st[1] + " 70 " + st[2]);
            // kelp: 6 grown (age 25) in one chunk, 6 fresh (age 21) in another
            cmd("fill -40 40 -40 -1 61 -25 stone");
            cmd("fill -39 41 -39 -2 61 -26 water");
            for (int i = 0; i < 6; i++) {
                cmd("fill " + (-38 + i) + " 41 -30 " + (-38 + i) + " 54 -30 kelp_plant");
                cmd("setblock " + (-38 + i) + " 55 -30 kelp[age=25]");
                cmd("fill " + (-14 + i) + " 41 -30 " + (-14 + i) + " 54 -30 kelp_plant");
                cmd("setblock " + (-14 + i) + " 55 -30 kelp[age=21]");
            }
            // amethyst: grown geode (8 clusters, 1 bud) and a fresh one (2 clusters, 6 buds)
            cmd("fill 58 28 -42 100 28 -38 amethyst_block");
            for (int i = 0; i < 8; i++) cmd("setblock " + (58 + i) + " 29 -40 amethyst_cluster[facing=up]");
            cmd("setblock 66 29 -40 small_amethyst_bud[facing=up]");
            for (int i = 0; i < 8; i++) cmd("setblock " + (90 + i) + " 29 -40 " + (i < 2 ? "amethyst_cluster" : "medium_amethyst_bud") + "[facing=up]");
            cmd("fill 30 0 30 45 40 45 stone");
            cmd("fill 31 20 38 44 21 38 air"); // straight 1x2 tunnel, 14 long
            cmd("fill 34 25 34 38 27 36 air");
            cmd("setblock 36 25 35 torch");
            cmd("setblock 35 25 35 torch");
            cmd("setblock 37 25 35 torch");
            cmd("fill 40 151 40 46 151 46 oak_planks");
            cmd("setblock 40 152 40 crafting_table");
            cmd("setblock 41 152 40 furnace");
            cmd("setblock 42 152 40 red_bed[part=foot,facing=north]");
            cmd("setblock 42 152 39 red_bed[part=head,facing=north]");
            cmd("setblock 43 152 40 anvil");
            cmd("setblock 44 152 40 enchanting_table");
            for (int i = 0; i < 6; i++) cmd("setblock " + (40 + i) + " 152 42 chest");
            cmd("setblock 40 152 44 shulker_box");
            cmd("setblock 41 152 44 barrel");
            cmd("setblock 45 152 45 oak_sign{front_text:{messages:['\"Base of\"','\"Tester\"','\"\"','\"\"']}}");
            cmd("fill 50 152 40 50 156 43 obsidian");
            cmd("fill 50 153 41 50 155 42 nether_portal[axis=z]");
            for (int i = 0; i < 12; i++) cmd("summon cow " + (60 + (i % 4)) + " 152 " + (40 + i / 4));
            cmd("summon item_frame 45 153 46 {Facing:2}");
            cmd("summon wolf 44 152 44 {Owner:[I;0,0,0,1]}");
            cmd("tp @s 20 160 20 -45 30");
            for (Class<?> c : new Class<?>[]{BaseFinderModules.BaseBlocks.class, BaseFinderModules.Storage.class, BaseFinderModules.NewChunks.class,
                    BaseFinderModules.KelpEsp.class, BaseFinderModules.AmethystEsp.class, BaseFinderModules.Growth.class, BaseFinderModules.Light.class, BaseFinderModules.Portals.class, BaseFinderModules.Signs.class,
                    BaseFinderModules.EntityClues.class, BaseFinderModules.Tunnels.class, NoFogModule.class}) {
                @SuppressWarnings("unchecked") hook.module.Module m = hook.Hook.INSTANCE.getModuleManager().getModuleByClass((Class) c);
                if (m != null && !m.isEnabled()) m.setEnabled(true);
            }
        }
        if (clock == 460) {
            hook.module.ModuleManager mm = hook.Hook.INSTANCE.getModuleManager();
            BaseFinderModules.BaseBlocks base = (BaseFinderModules.BaseBlocks) mm.getModuleByClass(BaseFinderModules.BaseBlocks.class);
            BaseFinderModules.KelpEsp kelp = (BaseFinderModules.KelpEsp) mm.getModuleByClass(BaseFinderModules.KelpEsp.class);
            BaseFinderModules.AmethystEsp ame = (BaseFinderModules.AmethystEsp) mm.getModuleByClass(BaseFinderModules.AmethystEsp.class);
            BaseFinderModules.Tunnels tun = (BaseFinderModules.Tunnels) mm.getModuleByClass(BaseFinderModules.Tunnels.class);
            int chunks = 0, settled = 0;
            StringBuilder bases = new StringBuilder(), kelps = new StringBuilder(), ames = new StringBuilder(), tunnels = new StringBuilder(), stor = new StringBuilder(), lights = new StringBuilder(), signs = new StringBuilder();
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                chunks++;
                if (BaseFinder.settled(cd)) settled++;
                if (base.isBase(cd)) {
                    java.util.TreeMap<String, Integer> types = new java.util.TreeMap<>();
                    for (int k : new int[]{BaseFinder.BASE, BaseFinder.STORAGE, BaseFinder.LIGHT})
                        for (net.minecraft.class_2338 q : cd.hits[k]) types.merge(BaseFinder.id(mc.field_1687.method_8320(q).method_26204()), 1, Integer::sum);
                    bases.append(cd.cx).append(',').append(cd.cz).append("(score ").append(cd.score).append(' ').append(types).append(" zone=").append(BaseFinder.naturalZone(cd.cx, cd.cz)).append(") ");
                }
                if (kelp.grown(cd)) kelps.append(cd.cx).append(',').append(cd.cz).append(' ');
                if (ame.grown(cd)) ames.append(cd.cx).append(',').append(cd.cz).append(' ');
                if (tun.ok(cd)) tunnels.append(cd.cx).append(',').append(cd.cz).append(' ');
                int sc = BaseFinder.finds(cd, BaseFinder.STORAGE, BaseFinder.STORAGE_W).size();
                if (sc > 0) stor.append(cd.cx).append(',').append(cd.cz).append('=').append(sc).append(' ');
                int lc = BaseFinder.finds(cd, BaseFinder.LIGHT, BaseFinder.LIGHT_W).size();
                if (lc > 0) lights.append(cd.cx).append(',').append(cd.cz).append('=').append(lc).append(' ');
                if (cd.count(BaseFinder.SIGN) > 0 && BaseFinder.settled(cd) && !BaseFinder.natural(cd)) signs.append(cd.cx).append(',').append(cd.cz).append(' ');
            }
            System.out.println("CARTTEST BASEFINDER chunks=" + chunks + " settled=" + settled);
            System.out.println("CARTTEST BASEFINDER bases: " + bases + " (expect 2,2)");
            System.out.println("CARTTEST BASEFINDER storage: " + stor);
            System.out.println("CARTTEST BASEFINDER light: " + lights);
            System.out.println("CARTTEST BASEFINDER signs: " + signs);
            System.out.println("CARTTEST BASEFINDER kelp: " + kelps + " (expect -3,-2)");
            System.out.println("CARTTEST BASEFINDER amethyst: " + ames + " (expect 3,-3)");
            System.out.println("CARTTEST BASEFINDER tunnels: " + tunnels + " (expect 2,2)");
            for (String[] st : STRUCTS) {
                int cx = Integer.parseInt(st[1]) >> 4, cz = Integer.parseInt(st[2]) >> 4;
                int raw = 0;
                for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
                    BaseFinder.ChunkData n = BaseFinder.get(cx + dx, cz + dz);
                    if (n != null) raw += n.count(BaseFinder.BASE_W) + n.count(BaseFinder.STORAGE_W) + n.count(BaseFinder.LIGHT_W) + n.count(BaseFinder.BASE) + n.count(BaseFinder.STORAGE);
                }
                System.out.println("CARTTEST BASEFINDER struct " + st[0] + " zone=" + BaseFinder.naturalZone(cx, cz) + " scanned=" + (BaseFinder.get(cx, cz) != null) + " playerLookingBlocksAround=" + raw);
            }
        }
        if (clock == 465) net.minecraft.class_318.method_22690(mc.field_1697, "basefinder.png", mc.method_1522(), 1, msg -> {});
        if (clock == 480) { System.out.println("CARTTEST DONE ok=1/1"); mc.method_1592(); phase = 99; }
    }

    /** -Dhook.carttest.only=fog: under water and in lava, No Fog off/on with the user's "Custom 0%/10%" config. */
    private static void fog(class_310 mc, net.minecraft.class_746 p) {
        NoFogModule nf = (NoFogModule) hook.Hook.INSTANCE.getModuleManager().getModuleByClass(NoFogModule.class);
        if (clock == 100) {
            cmd("gamemode creative");
            cmd("time set noon");
            cmd("weather clear");
            cmd("effect give @s fire_resistance infinite 0 true");
            cmd("effect give @s water_breathing infinite 0 true");
            for (int b = 0; b < 2; b++) {
                int z = b * 30;
                cmd("fill 0 99 " + (z - 7) + " 81 113 " + (z + 7) + " glass hollow");
                cmd("fill 1 100 " + (z - 6) + " 80 112 " + (z + 6) + " " + (b == 0 ? "water" : "lava"));
                String[] col = {"red", "orange", "yellow", "lime", "cyan", "blue", "purple"};
                for (int i = 0; i < 7; i++) cmd("fill " + (10 + i * 10) + " 100 " + (z + 2 - (i % 2) * 6) + " " + (10 + i * 10) + " 110 " + (z + 4 - (i % 2) * 6) + " " + col[i] + "_concrete");
            }
            nf.testConfig(1, 0.0, 10.0);
            if (nf.isEnabled()) nf.setEnabled(false);
            cmd("tp @s 2 105 0 -90 0"); cmd("clear @s");
        }
        if (clock == 140 || clock == 225) mc.field_1705.method_1743().method_1808(false);
        if (clock == 150) shot(mc, "fog_water_off");
        if (clock == 155) nf.setEnabled(true);
        if (clock == 175) shot(mc, "fog_water_on");
        if (clock == 180) { nf.setEnabled(false); cmd("tp @s 2 105 30 -90 0"); }
        if (clock == 230) shot(mc, "fog_lava_off");
        if (clock == 235) nf.setEnabled(true);
        if (clock == 255) shot(mc, "fog_lava_on");
        if (clock == 260) System.out.println("CARTTEST FOG medium=" + mc.field_1773.method_19418().method_19334() + " enabled=" + nf.isEnabled());
        if (clock == 270) { System.out.println("CARTTEST DONE ok=1/1"); mc.method_1592(); phase = 99; }
    }

    /** -Dhook.carttest.only=hud: HUD editor with grid, every vanilla part framed, window resize keeps anchors. */
    private static void hud(class_310 mc, net.minecraft.class_746 p) {
        if (clock == 100) {
            cmd("gamemode survival");
            cmd("time set noon");
            cmd("weather clear");
            cmd("tp @s 0 200 0");
            cmd("effect clear @s");
            cmd("effect give @s speed 95 1");
            cmd("effect give @s strength 8 0");
            cmd("effect give @s night_vision infinite 0");
            cmd("effect give @s slow_falling infinite 0");
            hook.module.Module hud = hook.Hook.INSTANCE.getModuleManager().getModuleByClass(HudModule.class);
            if (hud != null && !hud.isEnabled()) hud.setEnabled(true);
            hook.module.Module et = hook.Hook.INSTANCE.getModuleManager().getModuleByClass(EffectTimerModule.class);
            if (et != null && !et.isEnabled()) et.setEnabled(true);
            com.spotifyclient.ModSettings.enableHudLyrics = true;
            com.spotifyclient.ModSettings.hudLyricsStyle = 0;
        }
        if (clock == 115) mc.field_1705.method_1743().method_1808(false);
        if (clock == 118) shot(mc, "hud_play_effects");
        if (clock == 125) mc.method_1507(new net.minecraft.class_408("", false));
        if (clock == 135) {
            float[] bc = HudModule.instance.editor().editorButtonCenter();
            System.out.println("CARTTEST CLICK " + (bc == null ? "-1 -1" : (int) bc[0] + " " + (int) bc[1]) + " editing=" + HudLayoutEditor.editing);
            shot(mc, "hud_chat_button");
        }
        if (clock == 165) {
            System.out.println("CARTTEST EDITING " + HudLayoutEditor.editing);
            // window-pixel centers for the script to scroll on (real mouse wheel messages to the test window)
            float[] mb = HudModule.instance.editor().boundsOf(HudLayoutEditor.HudSection.MUSIC_BAR);
            float[] et = HudModule.instance.editor().boundsOf(HudLayoutEditor.HudSection.EFFECT_TIMER);
            System.out.println("CARTTEST SCROLLAT " + (mb == null ? "-1 -1" : (int) (mb[0] + mb[2] / 2) + " " + (int) (mb[1] + mb[3] / 2))
                    + " " + (et == null ? "-1 -1" : (int) (et[0] + et[2] / 2) + " " + (int) (et[1] + et[3] / 2)));
            report(mc, "before");
            shot(mc, "hud_editor_before");
        }
        if (clock == 250) {
            System.out.println("CARTTEST SCALES wheel music=" + HudModule.instance.editor().scaleOf(HudLayoutEditor.HudSection.MUSIC_BAR)
                    + " effects=" + HudModule.instance.editor().scaleOf(HudLayoutEditor.HudSection.EFFECT_TIMER));
            float[] eh = HudModule.instance.editor().handleOf(HudLayoutEditor.HudSection.EFFECT_TIMER);
            float[] sh = HudModule.instance.editor().handleOf(VanillaHud.Part.SCOREBOARD);
            System.out.println("CARTTEST DRAG " + (eh == null ? "-1 -1" : (int) eh[0] + " " + (int) eh[1]) + " " + (sh == null ? "-1 -1" : (int) sh[0] + " " + (int) sh[1]));
        }
        if (clock == 330) {
            System.out.println("CARTTEST SCALES drag effects=" + HudModule.instance.editor().scaleOf(HudLayoutEditor.HudSection.EFFECT_TIMER)
                    + " scoreboard=" + VanillaHud.state(VanillaHud.Part.SCOREBOARD).scale);
            report(mc, "big");
            shot(mc, "hud_editor_big");
        }
        if (clock == 335) org.lwjgl.glfw.GLFW.glfwSetWindowSize(mc.method_22683().method_4490(), 1000, 620);
        if (clock == 370) { report(mc, "small"); shot(mc, "hud_editor_small"); }
        if (clock == 380) mc.method_1507(null);
        if (clock == 395) { shot(mc, "hud_play_small"); System.out.println("CARTTEST EDITING after close " + HudLayoutEditor.editing); }
        if (clock == 410) { System.out.println("CARTTEST DONE ok=1/1"); mc.method_1592(); phase = 99; }
    }

    private static net.minecraft.class_2338 sbOrigin, sbChest;
    private static Object sbSchematic;
    private static int sbLastPct = -1;

    /** -Dhook.carttest.only=builder: Schematic Builder builds a Litematica schematic in survival, part of it from a chest. */
    private static void builder(class_310 mc, net.minecraft.class_746 p) {
        SchematicBuilderModule sb = (SchematicBuilderModule) hook.Hook.INSTANCE.getModuleManager().getModuleByClass(SchematicBuilderModule.class);
        int bx = 3000, bz = 3000;
        if (clock == 100) {
            cmd("gamemode survival");
            cmd("time set noon");
            cmd("weather clear");
            cmd("gamerule doDaylightCycle false");
            cmd("clear @s");
            cmd("effect give @s resistance infinite 4 true");
            cmd("effect give @s saturation infinite 0 true");
            cmd("tp @s " + bx + " -60 " + bz + " 0 20");
        }
        if (clock == 140) {
            // a clean flat test area (old test runs may have left blocks)
            // vanilla /fill is limited to 32768 blocks: clear layer by layer
            for (int y = -60; y <= -40; y++) cmd("fill " + (bx - 8) + " " + y + " " + (bz - 8) + " " + (bx + 24) + " " + y + " " + (bz + 24) + " air");
            cmd("fill " + (bx - 6) + " -61 " + (bz - 6) + " " + (bx + 40) + " -61 " + (bz + 40) + " grass_block");
            cmd("kill @e[type=item]");
        }
        if (clock == 150) {
            // a small test build: floor, log pillars + a log beam, stairs in 4 directions, top / bottom slabs,
            // an observer facing up, a piston facing west and a 9 high tower (needs climbing)
            int ox = bx + 4, oy = -60, oz = bz + 4;
            cmd("fill " + ox + " " + oy + " " + oz + " " + (ox + 6) + " " + oy + " " + (oz + 6) + " stone_bricks");
            for (int[] c : new int[][]{{0, 0}, {6, 0}, {0, 6}, {6, 6}}) cmd("fill " + (ox + c[0]) + " " + (oy + 1) + " " + (oz + c[1]) + " " + (ox + c[0]) + " " + (oy + 4) + " " + (oz + c[1]) + " oak_log[axis=y]");
            cmd("fill " + (ox + 1) + " " + (oy + 4) + " " + oz + " " + (ox + 5) + " " + (oy + 4) + " " + oz + " oak_log[axis=x]");
            cmd("setblock " + (ox + 2) + " " + (oy + 1) + " " + (oz + 2) + " oak_stairs[facing=north]");
            cmd("setblock " + (ox + 4) + " " + (oy + 1) + " " + (oz + 2) + " oak_stairs[facing=east]");
            cmd("setblock " + (ox + 2) + " " + (oy + 1) + " " + (oz + 4) + " oak_stairs[facing=south]");
            cmd("setblock " + (ox + 4) + " " + (oy + 1) + " " + (oz + 4) + " oak_stairs[facing=west]");
            cmd("setblock " + (ox + 3) + " " + (oy + 1) + " " + (oz + 1) + " smooth_stone_slab[type=top]");
            cmd("setblock " + (ox + 3) + " " + (oy + 1) + " " + (oz + 5) + " smooth_stone_slab[type=bottom]");
            cmd("setblock " + (ox + 1) + " " + (oy + 1) + " " + (oz + 3) + " observer[facing=up]");
            cmd("setblock " + (ox + 5) + " " + (oy + 1) + " " + (oz + 3) + " piston[facing=west]");
            cmd("fill " + (ox + 3) + " " + (oy + 1) + " " + (oz + 3) + " " + (ox + 3) + " " + (oy + 9) + " " + (oz + 3) + " bricks");
            // blocks whose direction depends on where you stand and what you click
            String[][] more = {
                    {"1", "1", "1", "stone_bricks"},
                    {"2", "1", "1", "hopper[facing=west]"},
                    {"3", "2", "2", "oak_stairs[facing=south,half=top]"},
                    {"2", "3", "3", "wall_torch[facing=west]"},
                    {"3", "5", "2", "stone_button[face=wall,facing=north]"},
                    {"1", "2", "6", "oak_trapdoor[facing=east,half=top]"},
                    {"4", "1", "5", "furnace[facing=east]"},
                    {"6", "3", "1", "dispenser[facing=down]"},
                    {"6", "1", "3", "lever[face=wall,facing=east]"},
                    {"6", "1", "5", "oak_log[axis=z]"},
                    // a little water basin (farms): the source has to be placed with a bucket, last
                    {"4", "1", "1", "stone_bricks"},
                    {"6", "1", "1", "stone_bricks"},
                    {"5", "1", "0", "stone_bricks"},
                    {"5", "1", "2", "sand"},
                    // farm plants: kelp in the basin (water first, then the kelp), sugar cane beside it, a cactus
                    {"5", "1", "1", "kelp"},
                    {"5", "2", "2", "sugar_cane"},
                    {"5", "3", "2", "sugar_cane"},
                    {"4", "1", "6", "sand"},
                    {"4", "2", "6", "cactus"},
                    // rotated by the look direction (16 steps) and a sign that opens its editor
                    {"3", "1", "4", "oak_sign[rotation=6]"},
                    {"1", "1", "2", "skeleton_skull[rotation=10]"},
                    {"5", "1", "4", "white_banner[rotation=12]"},
                    // states that need a click, a second item or a tool
                    {"1", "1", "4", "smooth_stone_slab[type=double]"},
                    {"2", "1", "5", "repeater[facing=north,delay=3]"},
                    {"4", "1", "3", "comparator[facing=west,mode=subtract]"},
                    {"1", "1", "5", "note_block[note=5]"},
                    {"0", "1", "2", "candle[candles=3]"},
                    {"0", "1", "4", "farmland"},
                    {"0", "2", "4", "wheat"},
                    {"6", "1", "2", "stripped_oak_log[axis=x]"},
                    {"2", "1", "6", "oak_trapdoor[facing=south,half=bottom,open=true]"},
            };
            for (String[] b : more) cmd("setblock " + (ox + Integer.parseInt(b[0])) + " " + (oy + Integer.parseInt(b[1])) + " " + (oz + Integer.parseInt(b[2])) + " " + b[3]);
        }
        if (clock == 170) {
            try {
                sbOrigin = new net.minecraft.class_2338(bx + 4, -60, bz + 4);
                Class<?> as = Class.forName("fi.dy.masa.litematica.selection.AreaSelection");
                Object area = as.getConstructor().newInstance();
                Class<?> boxC = Class.forName("fi.dy.masa.litematica.selection.Box");
                Object box = boxC.getConstructor(net.minecraft.class_2338.class, net.minecraft.class_2338.class, String.class).newInstance(sbOrigin, sbOrigin.method_10069(6, 9, 6), "main");
                as.getMethod("addSubRegionBox", boxC, boolean.class).invoke(area, box, false);
                as.getMethod("setName", String.class).invoke(area, "sbtest");
                as.getMethod("setExplicitOrigin", net.minecraft.class_2338.class).invoke(area, sbOrigin);
                Class<?> info = Class.forName("fi.dy.masa.litematica.schematic.LitematicaSchematic$SchematicSaveInfo");
                Object si = info.getConstructor(boolean.class, boolean.class).newInstance(false, false);
                Class<?> isc = Class.forName("fi.dy.masa.malilib.interfaces.IStringConsumer");
                Object sink = java.lang.reflect.Proxy.newProxyInstance(isc.getClassLoader(), new Class<?>[]{isc}, (o, m, a) -> null);
                Class<?> ls = Class.forName("fi.dy.masa.litematica.schematic.LitematicaSchematic");
                sbSchematic = ls.getMethod("createFromWorld", net.minecraft.class_1937.class, as, info, String.class, isc).invoke(null, mc.field_1687, area, si, "test", sink);
                System.out.println("CARTTEST SB schematic captured " + sbSchematic);
            } catch (Throwable t) {
                System.out.println("CARTTEST SB capture failed " + t);
            }
        }
        if (clock == 180) {
            cmd("fill " + (bx + 4) + " -60 " + (bz + 4) + " " + (bx + 10) + " -50 " + (bz + 10) + " air");
            cmd("kill @e[type=item]");
        }
        if (clock == 200 && sbSchematic != null) {
            try {
                Class<?> plc = Class.forName("fi.dy.masa.litematica.schematic.placement.SchematicPlacement");
                Object pl = plc.getMethod("createFor", sbSchematic.getClass(), net.minecraft.class_2338.class, String.class, boolean.class, boolean.class).invoke(null, sbSchematic, sbOrigin, "sbtest", true, true);
                Object mgr = Class.forName("fi.dy.masa.litematica.data.DataManager").getMethod("getSchematicPlacementManager").invoke(null);
                mgr.getClass().getMethod("addSchematicPlacement", plc, boolean.class).invoke(mgr, pl, false);
                mgr.getClass().getMethod("setSelectedSchematicPlacement", plc).invoke(mgr, pl);
                System.out.println("CARTTEST SB placement loaded at " + sbOrigin.method_23854() + " box=" + java.util.Arrays.toString(SbLitematica.box(pl)) + " total=" + SbLitematica.totalBlocks(pl));
            } catch (Throwable t) {
                System.out.println("CARTTEST SB load failed " + t);
            }
        }
        if (clock == 260) {
            // materials: everything in the inventory except the most used block, which waits in a marked chest
            Object pl = SbLitematica.placement(p.method_23317(), p.method_23318(), p.method_23321());
            int[] b = pl == null ? null : SbLitematica.box(pl);
            net.minecraft.class_1937 sw = SbLitematica.world();
            java.util.Map<net.minecraft.class_1792, Integer> need = new java.util.LinkedHashMap<>();
            if (b != null && sw != null) for (int x = b[0]; x <= b[3]; x++) for (int y = b[1]; y <= b[4]; y++) for (int z = b[2]; z <= b[5]; z++) {
                net.minecraft.class_2680 st = sw.method_8320(new net.minecraft.class_2338(x, y, z));
                if (st.method_26215() || SbWorld.secondaryPart(st)) continue;
                String bid = SbFix.id(st.method_26204());
                if (bid.equals("farmland")) { need.merge(SchematicBuilderModule.itemById("dirt"), 1, Integer::sum); continue; }
                if (bid.startsWith("stripped_")) { need.merge(SchematicBuilderModule.itemById(bid.substring(9)), 1, Integer::sum); continue; }
                net.minecraft.class_1792 it = st.method_26204().method_8389();
                if (it instanceof net.minecraft.class_1747) need.merge(it, 1, Integer::sum);
            }
            net.minecraft.class_1792 most = null;
            for (var e : need.entrySet()) if (most == null || e.getValue() > need.get(most)) most = e.getKey();
            StringBuilder sb2 = new StringBuilder();
            for (var e : need.entrySet()) {
                String id = net.minecraft.class_7923.field_41178.method_10221(e.getKey()).toString();
                sb2.append(id).append('=').append(e.getValue()).append(' ');
                if (e.getKey() != most) cmd("give @s " + id + " " + (e.getValue() + 2));
            }
            cmd("give @s cobblestone 10");
            // only an empty bucket: it has to fill it at the little pool next to the build
            cmd("give @s bucket 1");
            cmd("setblock " + (bx - 2) + " -61 " + (bz + 6) + " water");
            cmd("setblock " + (bx - 2) + " -61 " + (bz + 7) + " water");
            cmd("give @s bread 8");
            cmd("give @s diamond_pickaxe");
            cmd("give @s diamond_shovel");
            cmd("give @s diamond_axe");
            cmd("give @s diamond_hoe");
            cmd("give @s diamond_sword");
            sbChest = new net.minecraft.class_2338(bx - 3, -60, bz);
            cmd("setblock " + sbChest.method_10263() + " -60 " + sbChest.method_10260() + " chest");
            if (most != null) {
                String id = net.minecraft.class_7923.field_41178.method_10221(most).toString();
                int left = need.get(most) + 2, slot = 0;
                while (left > 0 && slot < 20) { int n = Math.min(64, left); cmd("item replace block " + sbChest.method_10263() + " -60 " + sbChest.method_10260() + " container." + slot++ + " with " + id + " " + n); left -= n; }
            }
            cmd("item replace block " + sbChest.method_10263() + " -60 " + sbChest.method_10260() + " container.25 with cobblestone 64");
            cmd("item replace block " + sbChest.method_10263() + " -60 " + sbChest.method_10260() + " container.26 with cobblestone 64");
            System.out.println("CARTTEST SB materials " + sb2 + "| in chest: " + (most == null ? "-" : net.minecraft.class_7923.field_41178.method_10221(most)));
        }
        if (clock == 300 && sb != null && !sb.isEnabled()) sb.setEnabled(true);
        if (clock == 302 && sb != null) sb.testMarkStorage(sbChest);
        // the pause menu (made non-pausing like on a server) open for the first minute (chest trip included): it must keep building
        if (clock == 340) {
            sbMenuDone = SchematicBuilderModule.progress() == null ? 0 : SchematicBuilderModule.progress().done();
            mc.method_1507(new net.minecraft.class_433(true) {
                @Override
                public boolean method_25421() { return false; }

                @Override
                public void method_25432() {
                    super.method_25432();
                    System.out.println("CARTTEST SB menu removed by:");
                    for (StackTraceElement e : Thread.currentThread().getStackTrace()) System.out.println("CARTTEST SB   at " + e);
                }
            });
            System.out.println("CARTTEST SB menu opened done=" + sbMenuDone);
        }
        if (clock > 340 && clock < 300 + 20 * 60 && clock % 20 == 0) {
            String sn = mc.field_1755 == null ? "none" : mc.field_1755 instanceof net.minecraft.class_433 ? "pause" : mc.field_1755.getClass().getSimpleName();
            if (clock % 100 == 0) System.out.println("CARTTEST SB menu screen=" + sn);
        }
        // get hungry mid-build: it has to stop, eat and go on
        if (clock == 300 + 20 * 70) { cmd("difficulty easy"); cmd("effect clear @s saturation"); cmd("effect give @s hunger 8 250 true"); }
        if (clock == 300 + 20 * 110) { cmd("effect give @s saturation infinite 0 true"); cmd("difficulty peaceful"); }
        // a zombie walks up mid-build: it has to fight it off
        if (clock == 300 + 20 * 130) { cmd("difficulty easy"); cmd("summon zombie ~3 ~ ~ {PersistenceRequired:1b}"); System.out.println("CARTTEST SB zombie summoned"); }
        if (clock == 300 + 20 * 150) {
            int z = mc.field_1687.method_8390(net.minecraft.class_1309.class, p.method_5829().method_1014(30), e -> BaseFinder.entityId(e).equals("zombie") && e.method_5805()).size();
            System.out.println("CARTTEST SB zombies alive after 20s: " + z);
            cmd("difficulty peaceful");
        }
        if (clock == 300 + 20 * 60) {
            int d = SchematicBuilderModule.progress() == null ? 0 : SchematicBuilderModule.progress().done();
            System.out.println("CARTTEST SB menu closed placedWhileOpen=" + (d - sbMenuDone) + " screen=" + (mc.field_1755 == null ? "none" : mc.field_1755.getClass().getSimpleName()));
            mc.method_1507(null);
        }
        if (clock > 300 && sb != null && clock % 100 == 0) {
            SchematicBuilderModule.Progress pr = SchematicBuilderModule.progress();
            System.out.println("CARTTEST SB t=" + (clock - 300) / 20 + "s " + (pr == null ? "-" : String.format(java.util.Locale.ROOT, "%d/%d %.1f%% eta=[%s] state=[%s]", pr.done(), pr.total(), pr.percent(), pr.eta(), pr.state()))
                    + " | " + sb.testStatus() + " pos=" + p.method_24515().method_23854() + " hp=" + p.method_6032() + " food=" + p.method_7344().method_7586());
            int pct = pr == null ? 0 : (int) pr.percent();
            if (clock == 700 || clock == 1500 || clock == 2600 || pct >= 100 && sbLastPct < 100) shot(mc, "sb_" + (clock - 300) / 20);
            sbLastPct = pct;
            if (pr != null && pr.state().equals("Finished") || clock > 300 + 20 * 480) {
                shot(mc, "sb_end");
                // every block of the schematic: is it really what was wanted (states included)?
                Object plc = SbLitematica.placement(p.method_23317(), p.method_23318(), p.method_23321());
                int[] bb = plc == null ? null : SbLitematica.box(plc);
                net.minecraft.class_1937 sw = SbLitematica.world();
                int bad = 0, all = 0;
                if (bb != null && sw != null) for (int x = bb[0]; x <= bb[3]; x++) for (int y = bb[1]; y <= bb[4]; y++) for (int z = bb[2]; z <= bb[5]; z++) {
                    net.minecraft.class_2338 q = new net.minecraft.class_2338(x, y, z);
                    net.minecraft.class_2680 want = sw.method_8320(q), have = mc.field_1687.method_8320(q);
                    if (want.method_26215()) continue;
                    all++;
                    if (SbWorld.matches(want, have) || want.method_26204() == net.minecraft.class_2246.field_10382 && have.method_26204() == want.method_26204()) continue;
                    bad++;
                    System.out.println("CARTTEST SB check BAD " + q.method_23854() + " want " + want + " have " + have);
                }
                System.out.println("CARTTEST SB check " + (all - bad) + "/" + all + " exact");
                SbAcTest.report();
                System.out.println("CARTTEST DONE ok=" + (pr != null && pr.state().equals("Finished") ? 1 : 0) + "/1");
                sb.setEnabled(false);
                mc.method_1592();
                phase = 99;
            }
        }
    }

    private static int ncX;
    private static int sbMenuDone;

    /** -Dhook.carttest.only=newchunks: fly into never-generated land (should be red), then back home (green). */
    private static void newChunks(class_310 mc, net.minecraft.class_746 p) {
        BaseFinderModules.NewChunks nc = (BaseFinderModules.NewChunks) hook.Hook.INSTANCE.getModuleManager().getModuleByClass(BaseFinderModules.NewChunks.class);
        if (clock == 100) {
            cmd("gamemode spectator");
            cmd("time set noon");
            cmd("weather clear");
            if (nc != null && !nc.isEnabled()) nc.setEnabled(true);
            ncX = 200_000 + (int) (System.currentTimeMillis() % 500_000L);
            cmd("tp @s " + ncX + " -25 0 0 35");
        }
        // the test world is superflat (no natural liquid): pour water in 8 chunks of the just-generated land
        if (clock == 200) for (int i = -1; i <= 1; i++) for (int j = -1; j <= 1; j++) if (i != 0 || j != 0)
            cmd("setblock " + (ncX + i * 64 + 8) + " -60 " + (j * 64 + 8) + " water");
        if (clock == 700) { ncReport(mc, nc, "fresh"); shot(mc, "nc_fresh"); }
        if (clock == 710) cmd("tp @s 0 -25 0 0 35");
        if (clock == 1300) { ncReport(mc, nc, "home"); shot(mc, "nc_home"); }
        if (clock == 1310) cmd("tp @s " + ncX + " -25 0 0 35");
        if (clock == 1900) { ncReport(mc, nc, "revisit"); shot(mc, "nc_revisit"); }
        if (clock == 1920) { System.out.println("CARTTEST DONE ok=1/1"); mc.method_1592(); phase = 99; }
    }

    private static void ncReport(class_310 mc, BaseFinderModules.NewChunks nc, String tag) {
        int pcx = (int) Math.floor(mc.field_1724.method_23317()) >> 4, pcz = (int) Math.floor(mc.field_1724.method_23321()) >> 4;
        int n = 0, o = 0, u = 0;
        for (int x = -8; x <= 8; x++) for (int z = -8; z <= 8; z++) {
            Boolean b = nc.stateOf(pcx + x, pcz + z);
            if (b == null) u++; else if (b) n++; else o++;
        }
        System.out.println("CARTTEST NC " + tag + " near(17x17): new=" + n + " old=" + o + " unknown=" + u + " | total new=" + nc.count(true) + " old=" + nc.count(false));
    }

    private static void report(class_310 mc, String tag) {
        StringBuilder sb = new StringBuilder("CARTTEST HUD " + tag + " gui=" + mc.method_22683().method_4486() + "x" + mc.method_22683().method_4502());
        for (VanillaHud.Part part : VanillaHud.Part.values()) {
            float[] r = VanillaHud.rect(part);
            sb.append(" | ").append(part.name()).append(r == null ? "=null" : String.format(java.util.Locale.ROOT, "=%.0f,%.0f %.0fx%.0f%s", r[0], r[1], r[2], r[3], VanillaHud.preview(part) ? " preview" : ""));
        }
        com.spotifyclient.hud.Draggable bar = com.spotifyclient.hud.DragSystem.get().byId("lyricsbar");
        if (bar != null) sb.append(String.format(java.util.Locale.ROOT, " | lyricsbar center=%.1f screenCenter=%.1f w=%.1f",
                bar.drag().targetX() + bar.width() / 2.0f, com.spotifyclient.hud.HudSpace.width() / 2.0f, bar.width()));
        System.out.println(sb);
    }

    private static void shot(class_310 mc, String name) {
        System.out.println("CARTTEST FOG shot " + name + " medium=" + mc.field_1773.method_19418().method_19334());
        net.minecraft.class_318.method_22690(mc.field_1697, name + ".png", mc.method_1522(), 1, msg -> {});
    }

    private static String refList(long seed) {
        StringBuilder sb = new StringBuilder();
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) if (SlimeChunkModule.isSlimeChunk(seed, x, z)) sb.append(x).append(',').append(z).append(' ');
        return sb.toString();
    }

    // ------------------------------------------------------------------ seed cracker
    private static final java.util.List<int[]> SC_FOUND = new java.util.concurrent.CopyOnWriteArrayList<>(); // type index, chunk x, chunk z
    private static volatile boolean scLocated;
    private static int scVisit, scNext, scEndAt = -1, scAddedAt = -1, scPhase2 = -1;
    private static final String[] SC_SETS = {"desert_pyramids", "jungle_temples", "swamp_huts", "villages", "pillager_outposts", "ocean_monuments", "shipwrecks", "igloos", "ruined_portals"};
    private static final String[] SC_IDS = {"desert_pyramid", "jungle_temple", "swamp_hut", "village", "outpost", "monument", "shipwreck", "igloo", "ruined_portal"};
    private static final String[] SC_PLACE = {"desert_pyramid", "jungle_pyramid", "swamp_hut", "", "", "monument", "shipwreck", "igloo", "ruined_portal"};
    private static final int[] SC_VISIT = {0, 1, 2, 6, 7, 8};
    private static int scAuto;
    private static volatile String scDirect;

    /**
     * -Dhook.carttest.only=seedcracker: the test world is flat, so the chunks where this seed puts temples / huts /
     * villages... are taken from the game's own placement code, three temples are built there with /place structure and
     * must be found by the client on its own, the End pillars are read, then the rest is added and the crack must give the
     * server's own seed. A second crack with temples only (no pillars) runs straight after.
     */
    private static void seedcracker(class_310 mc, net.minecraft.class_746 p) {
        var server = mc.method_1576();
        if (clock == 100) {
            cmd("gamemode spectator");
            cmd("time set day");
            hook.module.Module m = hook.Hook.INSTANCE.getModuleManager().getModuleByClass(SeedCrackerModule.class);
            if (m != null && !m.isEnabled()) m.setEnabled(true);
            server.execute(() -> {
                var level = server.method_30002();
                long seed = level.method_8412();
                net.minecraft.class_2378<net.minecraft.class_7059> sets = level.method_30349().method_30530(net.minecraft.class_7924.field_41248);
                for (int ti = 0; ti < SC_SETS.length; ti++) {
                    var pl = (net.minecraft.class_6872) sets.method_63535(net.minecraft.class_2960.method_60656(SC_SETS[ti])).comp_511();
                    int sp = pl.method_41632();
                    for (int rx = -1; rx <= 1; rx++) {
                        for (int rz = -1; rz <= 1; rz++) {
                            var c = pl.method_40169(seed, rx * sp, rz * sp);
                            if (ti == 4 && !pl.method_56575(c.field_9181, c.field_9180, seed)) continue;
                            SC_FOUND.add(new int[]{ti, c.field_9181, c.field_9180});
                        }
                    }
                }
                System.out.println("CARTTEST SEED placement chunks " + SC_FOUND.size());
                scLocated = true;
            });
        }
        if (clock == 110) {
            long seed = server.method_30002().method_8412();
            long h = SeedCracker.hashSeed(SeedCracker.sha(), seed);
            SeedCracker.debugTruth = seed;
            StructureMatcher.debug = true;
            System.out.println("CARTTEST SEED real seed=" + seed + " myHash=" + h + " fast=" + SeedCracker.hashFast(seed, new int[64]) + " guava=" + com.google.common.hash.Hashing.sha256().hashLong(seed).asLong() + " module " + SeedCrackerModule.testState());
        }
        // build one of each temple kind at its real chunk and let the client find it
        if (scLocated && scVisit < SC_VISIT.length && clock >= scNext && clock > 120) {
            int[] f = null;
            for (int[] g : SC_FOUND) if (g[0] == SC_VISIT[scVisit]) { f = g; break; }
            int x = f[1] * 16 + 8, z = f[2] * 16 + 8;
            cmd("tp @s " + x + " 0 " + z);
            System.out.println("CARTTEST SEED visiting " + SC_IDS[f[0]] + " chunk " + f[1] + " " + f[2]);
            final int[] ff = f;
            scNext = clock + 260;
            scVisit++;
            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException ignored) { }
                mc.execute(() -> { cmd("place structure minecraft:" + SC_PLACE[ff[0]] + " " + x + " -60 " + z); SeedCrackerModule.testRescan(); });
                try { Thread.sleep(5000); } catch (InterruptedException ignored) { }
                mc.execute(SeedCrackerModule::testRescan);
            }).start();
        }
        if (scVisit == SC_VISIT.length && clock >= scNext && scEndAt < 0) {
            System.out.println("CARTTEST SEED after visits: auto found " + SeedCrackerModule.testObs());
            int good = 0;
            for (int vi : SC_VISIT) {
                int[] f = null;
                for (int[] g : SC_FOUND) if (g[0] == vi) { f = g; break; }
                boolean hit = false, wrong = false;
                for (SeedCracker.Obs o : SeedCrackerModule.testObs()) {
                    if (!o.type.id().equals(SC_IDS[vi])) continue;
                    if (o.cx == f[1] && o.cz == f[2]) hit = true;
                    // anything else of this kind must be a real placement chunk too (stale temples of older tests are dropped below)
                    else if (vi >= 5 && SC_FOUND.stream().noneMatch(g -> g[0] == vi && g[1] == o.cx && g[2] == o.cz)) {
                        // the basefinder test placed one of each by hand at fixed spots: finding those exactly is right too
                        boolean other = false;
                        for (String[] st : STRUCTS) if (st[0].equals(SC_IDS[vi]) && (Integer.parseInt(st[1]) >> 4) == o.cx && (Integer.parseInt(st[2]) >> 4) == o.cz) other = true;
                        if (!other) wrong = true;
                        else System.out.println("CARTTEST SEED AUTO " + SC_IDS[vi] + " also found the basefinder one exactly at chunk " + o.cx + " " + o.cz);
                    }
                }
                if (hit && !wrong) good++;
                System.out.println("CARTTEST SEED AUTO " + SC_IDS[vi] + " " + (hit ? "OK" : "MISS") + (wrong ? " +WRONG" : "") + " at chunk " + f[1] + " " + f[2]);
            }
            scAuto = good;
            // the flat test world also holds temples an older test built anywhere: those fit no seed, drop them
            SeedCrackerModule.testRetain(o -> SC_FOUND.stream().anyMatch(f -> SC_IDS[f[0]].equals(o.type.id()) && f[1] == o.cx && f[2] == o.cz));
            System.out.println("CARTTEST SEED kept " + SeedCrackerModule.testObs());
            cmd("execute in minecraft:the_end run tp @s 0 120 0");
            scEndAt = clock;
        }
        if (scEndAt > 0 && clock == scEndAt + 300) {
            System.out.println("CARTTEST SEED in the end: " + SeedCrackerModule.testState());
            int[] last = null;
            for (int[] g : SC_FOUND) if (g[0] == 2) { last = g; break; }
            cmd("execute in minecraft:overworld run tp @s " + (last[1] * 16 + 8) + " 0 " + (last[2] * 16 + 8));
        }
        if (scEndAt > 0 && clock == scEndAt + 360) {
            // the rest by hand, like ".seed add" would: two of each temple kind, two villages a chunk off (radius 1), an outpost, a monument
            int[] per = new int[SC_SETS.length];
            for (int[] f : SC_FOUND) {
                int lim = f[0] <= 3 ? 2 : f[0] <= 5 ? 1 : 0;
                if (per[f[0]]++ >= lim) continue;
                int off = f[0] == 3 ? 1 : 0;
                SeedCrackerModule.testAdd(new SeedCracker.Obs(SeedCracker.TYPES.get(SC_IDS[f[0]]), f[1] + off, f[2] - off, f[0] >= 3 ? 1 : 0, false));
            }
            SeedCrackerModule.testRetain(o -> SC_FOUND.stream().anyMatch(f -> SC_IDS[f[0]].equals(o.type.id()) && Math.abs(f[1] - o.cx) <= 1 && Math.abs(f[2] - o.cz) <= 1));
            scAddedAt = clock;
            System.out.println("CARTTEST SEED all added: " + SeedCrackerModule.testState());
        }
        if (scAddedAt > 0 && scPhase2 < 0 && (clock - scAddedAt) % 100 == 0) {
            Long got = SeedCrackerModule.crackedSeed();
            long real = server.method_30002().method_8412();
            System.out.println("CARTTEST SEED t+" + (clock - scAddedAt) / 20 + "s " + SeedCrackerModule.testState());
            if (got != null || clock - scAddedAt > 20 * 240) {
                System.out.println("CARTTEST SEED RESULT1 " + (got != null && got == real ? "OK" : "FAIL") + " cracked=" + got + " real=" + real);
                scPhase2 = clock;
                // second: five temples only, no pillars (the low-20-bit path), straight on the math
                java.util.List<SeedCracker.Obs> five = new java.util.ArrayList<>();
                int[] per2 = new int[3];
                for (int[] f : SC_FOUND) if (f[0] <= 2 && five.size() < 8 && per2[f[0]]++ < 3) five.add(new SeedCracker.Obs(SeedCracker.TYPES.get(SC_IDS[f[0]]), f[1], f[2], 0, false));
                long h = SeedCracker.hashSeed(SeedCracker.sha(), real);
                new Thread(() -> {
                    long t0 = System.currentTimeMillis();
                    SeedCracker.Result r = new SeedCracker.Result();
                    SeedCracker.run(five, null, h, Math.max(1, Runtime.getRuntime().availableProcessors() - 1), r, new java.util.concurrent.atomic.AtomicBoolean(), s -> System.out.println("CARTTEST SEED direct: " + s));
                    scDirect = (r.seed != null && r.seed == real ? "OK" : "FAIL") + " seed=" + r.seed + " err=" + r.error + " in " + (System.currentTimeMillis() - t0) + " ms, ready said " + SeedCracker.ready(five, null, h);
                }).start();
            }
        }
        if (scPhase2 > 0 && (scDirect != null || clock - scPhase2 > 20 * 240)) {
            Long got = SeedCrackerModule.crackedSeed();
            long real = server.method_30002().method_8412();
            boolean ok1 = got != null && got == real, ok2 = scDirect != null && scDirect.startsWith("OK");
            System.out.println("CARTTEST SEED RESULT2 " + scDirect);
            System.out.println("CARTTEST DONE ok=" + ((ok1 ? 1 : 0) + (ok2 ? 1 : 0) + (scAuto == SC_VISIT.length ? 1 : 0)) + "/3 auto=" + scAuto + "/" + SC_VISIT.length);
            mc.method_1592();
            phase = 99;
        }
    }

    private static void cmd(String c) {
        class_310 mc = class_310.method_1551();
        if (mc.method_1562() != null) mc.method_1562().method_45730(c);
    }
}
