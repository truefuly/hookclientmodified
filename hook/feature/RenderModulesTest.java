package hook.feature;

import net.minecraft.class_310;

/** Developer self-test (only with -Dhook.rtest): screenshots of Chunk Borders and Clean Hitboxes in a test world. */
public final class RenderModulesTest {
    public static final boolean ENABLED = System.getProperty("hook.rtest") != null;
    private static int clock = -1, lastAge = -1;

    private RenderModulesTest() {
    }

    public static void tick() {
        class_310 mc = class_310.method_1551();
        var p = mc.field_1724;
        if (p == null || mc.method_1576() == null) return;
        if (mc.field_1755 instanceof net.minecraft.class_433 && mc.field_1755.method_25421()) mc.method_1507(null);
        if (p.field_6012 == lastAge) return;
        lastAge = p.field_6012;
        clock++;
        var mm = hook.Hook.INSTANCE.getModuleManager();
        switch (clock) {
            case 60 -> {
                cmd(mc, "gamemode creative");
                cmd(mc, "time set day");
                cmd(mc, "weather clear");
                cmd(mc, "kill @e[type=!player]");
                cmd(mc, "fill -12 150 -12 12 150 12 grass_block");
                cmd(mc, "fill -12 151 -12 12 160 12 air");
                cmd(mc, "setblock 2 151 6 stone");
                cmd(mc, "setblock 2 152 6 stone");
                cmd(mc, "summon zombie 0.5 151 6.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
                cmd(mc, "summon cow 3.5 151 5.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
                cmd(mc, "summon armor_stand -2.5 151 5.5");
                cmd(mc, "summon skeleton 3.5 151 9.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
                cmd(mc, "summon item -1.5 151 3.5 {Item:{id:\"minecraft:diamond\",count:1}}");
                cmd(mc, "tp @s 0.5 151 0.5 0 15");
            }
            case 100 -> {
                mm.getModuleByClass(CleanHitboxesModule.class).setEnabled(true);
                mm.getModuleByClass(ChunkBordersModule.class).setEnabled(true);
            }
            case 140 -> shot(mc, "both");
            case 160 -> mm.getModuleByClass(ChunkBordersModule.class).setEnabled(false);
            case 200 -> shot(mc, "hitboxes");
            case 210 -> {
                mm.getModuleByClass(CleanHitboxesModule.class).setEnabled(false);
                mm.getModuleByClass(ChunkBordersModule.class).setEnabled(true);
                cmd(mc, "tp @s 0.5 160 0.5 30 45");
            }
            case 250 -> shot(mc, "chunks");
            case 260 -> {
                mm.getModuleByClass(ChunkBordersModule.class).setEnabled(false);
                System.out.println("RTEST DONE");
                mc.method_1592();
            }
            default -> {}
        }
    }

    private static void shot(class_310 mc, String name) {
        net.minecraft.class_318.method_22690(mc.field_1697, "r_" + name + ".png", mc.method_1522(), 1, msg -> {});
        System.out.println("RTEST SHOT " + name);
    }

    private static void cmd(class_310 mc, String c) {
        if (mc.method_1562() != null) mc.method_1562().method_45730(c);
    }
}
