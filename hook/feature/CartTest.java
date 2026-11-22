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

    private static String refList(long seed) {
        StringBuilder sb = new StringBuilder();
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) if (SlimeChunkModule.isSlimeChunk(seed, x, z)) sb.append(x).append(',').append(z).append(' ');
        return sb.toString();
    }

    private static void cmd(String c) {
        class_310 mc = class_310.method_1551();
        if (mc.method_1562() != null) mc.method_1562().method_45730(c);
    }
}
