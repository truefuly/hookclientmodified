package hook.feature;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1429;
import net.minecraft.class_310;

/**
 * Developer self-test (only with -Dhook.breedtest): spawns animals around the player - in view, behind a wall, out of
 * range, a baby - turns Auto Breed on and reads the integrated server to see exactly which ones got fed.
 */
public final class AutoBreedTest {
    public static final boolean ENABLED = System.getProperty("hook.breedtest") != null;

    private record Spot(String type, double x, double z, boolean baby, boolean expect, String why) {}

    private static final List<Spot> SPOTS = List.of(
            new Spot("cow", 2.5, 0.5, false, true, "visible east"),
            new Spot("cow", -1.5, 0.5, false, true, "visible west"),
            new Spot("sheep", 0.5, 2.6, false, true, "visible south"),
            new Spot("pig", 2.3, 2.3, false, true, "visible diagonal (carrot)"),
            new Spot("chicken", -1.8, 2.2, false, true, "visible chicken (seeds)"),
            new Spot("cow", 0.5, -3.0, false, false, "behind stone wall"),
            new Spot("cow", 6.5, 0.5, false, false, "out of range"),
            new Spot("cow", -1.5, -1.5, true, false, "baby (age filter Adult)"));

    private static int clock, phase;
    private static int lastAge = -1;

    public static void tick() {
        class_310 mc = class_310.method_1551();
        var p = mc.field_1724;
        if (p == null || mc.method_1576() == null || phase == 99) return;
        if (p.field_6012 == lastAge) return;
        lastAge = p.field_6012;
        clock++;
        if (clock == 100) {
            cmd("gamemode survival");
            cmd("effect give @s resistance infinite 255 true");
            cmd("time set day");
            cmd("kill @e[type=!player]");
            cmd("fill -10 151 -10 10 158 10 air");
            cmd("fill -10 150 -10 10 150 10 grass_block");
            cmd("fill -3 151 -2 3 153 -2 stone");
            cmd("tp @s 0.5 151 0.5 0 0");
            cmd("item replace entity @s hotbar.0 with diamond_sword");
            cmd("item replace entity @s hotbar.1 with wheat 64");
            cmd("item replace entity @s hotbar.2 with carrot 64");
            cmd("item replace entity @s hotbar.3 with wheat_seeds 64");
            cmd("item replace entity @s hotbar.4 with air");
            for (Spot s : SPOTS)
                cmd("summon " + s.type + " " + s.x + " 151 " + s.z + " {NoAI:1b" + (s.baby ? ",Age:-24000" : "") + "}");
            hook.utils.InventoryUtil.swap(0);
            return;
        }
        if (clock == 130) {
            if (AutoBreedModule.instance != null && !AutoBreedModule.instance.isEnabled()) AutoBreedModule.instance.toggle(true);
            phase = 1;
            return;
        }
        if (phase == 1 && clock == 170) {
            mc.method_1576().execute(AutoBreedTest::check);
            phase = 2;
            return;
        }
        if (phase == 2 && clock == 185) {
            System.out.println("BREEDTEST slotAfter=" + p.method_31548().method_67532() + " (expect 0)");
            phase = 99;
            mc.method_1592();
        }
    }

    private static void check() {
        var level = class_310.method_1551().method_1576().method_30002();
        List<String> lines = new ArrayList<>();
        int ok = 0;
        for (Spot s : SPOTS) {
            class_1429 best = null;
            double bd = 0.6;
            for (class_1297 e : level.method_27909()) {
                if (!(e instanceof class_1429 a)) continue;
                double d = Math.hypot(e.method_23317() - s.x, e.method_23321() - s.z);
                if (d < bd) { bd = d; best = a; }
            }
            boolean love = best != null && best.method_6479();
            boolean pass = best != null && love == s.expect;
            if (pass) ok++;
            lines.add("BREEDTEST " + s.why + " -> " + (best == null ? "MISSING" : love ? "in love" : "not fed") + (pass ? " OK" : " FAIL"));
        }
        lines.forEach(System.out::println);
        System.out.println("BREEDTEST DONE ok=" + ok + "/" + SPOTS.size());
    }

    private static void cmd(String c) {
        class_310 mc = class_310.method_1551();
        if (mc.method_1562() != null) mc.method_1562().method_45730(c);
    }
}
