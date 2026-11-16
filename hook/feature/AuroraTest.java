package hook.feature;

import net.minecraft.class_310;
import net.minecraft.class_437;

/** Developer self-test (only with -Dhook.auroratest=<key>): key screen, wrong key, real key, panel, reopen. */
public final class AuroraTest {
    public static final String KEY = System.getProperty("hook.auroratest");
    public static final boolean ENABLED = KEY != null;
    private static int clock = -1;

    private AuroraTest() {
    }

    public static void tick() {
        class_310 mc = class_310.method_1551();
        if (mc.method_22683() == null) return;
        clock++;
        AuroraScreen screen = mc.field_1755 instanceof AuroraScreen a ? a : null;
        switch (clock) {
            case 200 -> mc.method_1507((class_437) new AuroraScreen());
            case 240 -> shot(mc, "key_empty");
            case 250 -> { if (screen != null) { screen.testType("HV-AAAAA-BBBBB-CCCCC-DDDDD-EEEEE"); screen.testClick("#unlock"); } }
            case 330 -> shot(mc, "key_wrong");
            case 340 -> { if (screen != null) { screen.testType(KEY.toLowerCase()); screen.testClick("#unlock"); } }
            case 460 -> shot(mc, "panel");
            case 470 -> { if (screen != null) screen.testSelectFirst(); }
            case 520 -> shot(mc, "chat");
            case 530 -> mc.method_1507(null);
            case 560 -> mc.method_1507((class_437) new AuroraScreen());
            case 640 -> shot(mc, "reopen");
            case 650 -> {
                System.out.println("AURORATEST STATUS " + AuroraClient.INSTANCE.status + " accounts=" + AuroraClient.INSTANCE.accounts.size() + " servers=" + AuroraClient.INSTANCE.servers.size() + " heads=" + AuroraClient.INSTANCE.heads.size());
                System.out.println("AURORATEST DONE");
                mc.method_1592();
            }
            default -> {}
        }
    }

    private static void shot(class_310 mc, String name) {
        net.minecraft.class_318.method_22690(mc.field_1697, "aurora_" + name + ".png", mc.method_1522(), 1, msg -> {});
        System.out.println("AURORATEST SHOT " + name + " status=" + AuroraClient.INSTANCE.status + " msg=" + AuroraClient.INSTANCE.message);
    }
}
