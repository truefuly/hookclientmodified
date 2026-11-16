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
    static volatile String planInfo, fireResult;
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
        if (clock == 100) {
            cmd("gamemode creative");
            cmd("effect give @s resistance infinite 255 true");
            cmd("time set day");
            cmd("gamerule doFireTick false");
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
                System.out.println("CARTTEST " + t.kind + " d=" + t.dist + " yaw=" + t.yaw + " h=" + t.height + " -> " + res + (lastFail != null ? " fail=" + lastFail : "") + " arrow=" + arrowSeen + " burning=" + arrowBurning + " fire=" + fireSeen + " plan=" + planInfo + " fireUse=" + fireResult + " railGone=" + railGone);
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

    private static void cmd(String c) {
        class_310 mc = class_310.method_1551();
        if (mc.method_1562() != null) mc.method_1562().method_45730(c);
    }
}
