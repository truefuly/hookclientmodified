package hook.feature;

import hook.event.events.PostItemUseEvent;
import hook.event.events.PreItemUseEvent;
import hook.utils.InventoryUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1684;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_9236;

/**
 * Developer self-test (only with -Dhook.pearltest): throws pearls through the real Pearl Catch flow in a
 * singleplayer world and reads the integrated server to see whether the pearl really landed on the wind charge.
 */
final class PearlCatchTest {
    static final boolean ENABLED = System.getProperty("hook.pearltest") != null;
    static final boolean TRACE = System.getProperty("hook.pearltest.trace") != null;
    /** Trace mode: throw the charge straight where you look, no aiming, to measure the raw physics. */
    static boolean forceStraight() { return TRACE; }

    private record Trial(int delay, float pitch, float yaw) {}

    private static final List<Trial> TRIALS = new ArrayList<>();
    static {
        float[] pitches = TRACE ? new float[]{0} : System.getProperty("hook.pearltest.quick") != null ? new float[]{-45} : new float[]{-70, -45, -20, 0, 20};
        int[] delays = TRACE ? new int[]{0, 2} : System.getProperty("hook.pearltest.quick") != null ? new int[]{3, 6} : new int[]{0, 1, 2, 3, 4, 5, 6, 8, 10, 15};
        for (int d : delays) for (float p : pitches) TRIALS.add(new Trial(d, p, 0));
    }

    private static int lastAge = -1, clock, trial = -1, phase;
    private static int caught, total;

    // server side observation (server thread only)
    private static final Map<Integer, class_243> pearls = new HashMap<>();
    private static final Map<Integer, class_243> winds = new HashMap<>();
    private static int windsSeen;
    private static volatile String result;
    private static class_243 lastPlayer;

    static void tick(PearlCatchModule m) {
        class_310 mc = class_310.method_1551();
        var p = mc.field_1724;
        if (p == null || mc.method_1576() == null) return;
        if (p.field_6012 == lastAge) return; // TickEvent fires twice per tick
        lastAge = p.field_6012;
        clock++;
        if (clock == 100) {
            cmd("gamemode creative");
            cmd("kill @e[type=!player]");
            cmd("tp @s 0.5 150 0.5 0 0");
            cmd("fill -3 149 -3 3 149 3 glass");
            cmd("item replace entity @s hotbar.0 with ender_pearl 16");
            cmd("item replace entity @s hotbar.1 with wind_charge 64");
            cmd("effect give @s resistance infinite 255 true");
            phase = 0;
            return;
        }
        if (clock < 140) return;
        if (phase == 0) {
            trial++;
            if (trial >= TRIALS.size()) {
                System.out.println("PEARLTEST DONE caught=" + caught + "/" + total);
                mc.method_1592();
                phase = 99;
                return;
            }
            Trial t = TRIALS.get(trial);
            cmd("kill @e[type=!player]");
            cmd("tp @s 0.5 150 0.5 " + t.yaw + " " + t.pitch);
            if (p.method_31549().field_7479) cmd("gamemode creative");
            phase = 1;
            clock = 140;
            return;
        }
        if (phase == 1 && clock >= 140 + 40) {
            Trial t = TRIALS.get(trial);
            m.testConfigure(t.delay);
            p.method_36456(t.yaw);
            p.method_36457(t.pitch);
            InventoryUtil.swap(0);
            mc.method_1576().execute(PearlCatchTest::resetObserve);
            PreItemUseEvent pre = new PreItemUseEvent(0);
            m.onPreItemUse(pre);
            if (!pre.isCancelled()) mc.field_1761.method_2919((class_1657) p, class_1268.field_5808);
            m.onPostItemUse(new PostItemUseEvent(0));
            phase = 2;
            clock = 200;
            return;
        }
        if (phase == 2) {
            mc.method_1576().execute(PearlCatchTest::observe);
            String r = result;
            if (r != null || clock >= 200 + (TRACE ? 15 : 100)) {
                Trial t = TRIALS.get(trial);
                if (r == null) r = "TIMEOUT winds=" + windsSeen;
                total++;
                if (r.startsWith("CATCH")) caught++;
                System.out.println("PEARLTEST delay=" + t.delay + " pitch=" + t.pitch + " -> " + r);
                phase = 0;
                clock = 140;
            }
        }
    }

    private static void resetObserve() {
        pearls.clear();
        winds.clear();
        windsSeen = 0;
        result = null;
        lastPlayer = null;
    }

    private static void observe() {
        if (result != null) return;
        class_310 mc = class_310.method_1551();
        var server = mc.method_1576();
        if (server == null || mc.field_1724 == null) return;
        var sp = server.method_3760().method_14602(mc.field_1724.method_5667());
        if (sp == null) return;
        var level = server.method_30002();
        Map<Integer, class_243> nowPearls = new HashMap<>();
        Map<Integer, class_243> nowWinds = new HashMap<>();
        for (class_1297 e : level.method_27909()) {
            if (e instanceof class_1684) nowPearls.put(e.method_5628(), new class_243(e.method_23317(), e.method_23318(), e.method_23321()));
            if (e instanceof class_9236) nowWinds.put(e.method_5628(), new class_243(e.method_23317(), e.method_23318(), e.method_23321()));
        }
        for (Integer id : nowWinds.keySet()) if (!winds.containsKey(id)) windsSeen++;
        if (TRACE) {
            StringBuilder sb = new StringBuilder("PTRACE t=" + server.method_3780());
            for (var e : level.method_27909()) {
                if (e instanceof class_1684 || e instanceof class_9236) {
                    var v = e.method_18798();
                    sb.append(String.format(" %s(%.4f %.4f %.4f | %.4f %.4f %.4f)", e instanceof class_1684 ? "P" : "W", e.method_23317(), e.method_23318(), e.method_23321(), v.field_1352, v.field_1351, v.field_1350));
                }
            }
            System.out.println(sb);
        }
        class_243 playerNow = new class_243(sp.method_23317(), sp.method_23318(), sp.method_23321());
        for (Map.Entry<Integer, class_243> e : pearls.entrySet()) {
            if (nowPearls.containsKey(e.getKey())) continue;
            // pearl gone: where did it put the player, and was a wind charge there?
            double best = Double.MAX_VALUE;
            for (class_243 w : winds.values()) best = Math.min(best, w.method_1022(playerNow));
            for (class_243 w : nowWinds.values()) best = Math.min(best, w.method_1022(playerNow));
            boolean onGlass = playerNow.field_1351 < 150.2 && Math.abs(playerNow.field_1352 - 0.5) < 4 && Math.abs(playerNow.field_1350 - 0.5) < 4;
            String tag = windsSeen == 0 ? "NOWIND" : best < 2.0 && !onGlass ? "CATCH" : "MISS";
            result = String.format("%s windDist=%.2f landed=(%.1f %.1f %.1f) winds=%d", tag, best, playerNow.field_1352, playerNow.field_1351, playerNow.field_1350, windsSeen);
            return;
        }
        pearls.clear();
        pearls.putAll(nowPearls);
        winds.clear();
        winds.putAll(nowWinds);
        lastPlayer = playerNow;
    }

    private static void cmd(String c) {
        class_310 mc = class_310.method_1551();
        if (mc.method_1562() != null) mc.method_1562().method_45730(c);
    }
}
