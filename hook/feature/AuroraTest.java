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
        if (KEY.equals("wheel")) { wheel(mc); return; }
        switch (clock) {
            case 200 -> mc.method_1507((class_437) new AuroraScreen());
            case 240 -> shot(mc, "key_empty");
            case 250 -> { if (screen != null) { screen.testType("HV-AAAAA-BBBBB-CCCCC-DDDDD-EEEEE"); screen.testClick("#unlock"); } }
            case 330 -> shot(mc, "key_wrong");
            case 340 -> { if (screen != null) { screen.testType(KEY.toLowerCase()); screen.testClick("#unlock"); } }
            case 460 -> shot(mc, "panel");
            case 470 -> { if (screen != null) { screen.testClick("tab:chat"); screen.testClick("p:"); } }
            case 520 -> shot(mc, "chat");
            case 530 -> { if (screen != null) { screen.testClick("tab:actions"); screen.testSelectFirst(); } }
            case 580 -> shot(mc, "actions");
            case 590 -> { if (screen != null) screen.testClick("sec:shops"); }
            case 620 -> shot(mc, "shops");
            case 630 -> { if (screen != null) { screen.testClick("#payout"); screen.testPayoutName("Notch"); screen.testClick("#pay-check"); } }
            case 700 -> shot(mc, "payout");
            case 710 -> { if (screen != null) screen.testClick("#pay-cancel"); }
            case 720 -> mc.method_1507(null);
            case 750 -> mc.method_1507((class_437) new AuroraScreen());
            case 830 -> shot(mc, "reopen");
            case 840 -> {
                System.out.println("AURORATEST STATUS " + AuroraClient.INSTANCE.status + " accounts=" + AuroraClient.INSTANCE.accounts.size() + " servers=" + AuroraClient.INSTANCE.servers.size() + " active=" + AuroraClient.INSTANCE.activeServers.size() + " rewards=" + AuroraClient.INSTANCE.rewards.size() + " heads=" + AuroraClient.INSTANCE.heads.size());
                System.out.println("AURORATEST DONE");
                mc.method_1592();
            }
            default -> {}
        }
    }

    /** -Dhook.auroratest=wheel: screenshots of the click GUI and the quick wheel only (no key needed). */
    private static void wheel(class_310 mc) {
        switch (clock) {
            case 200 -> mc.method_1507(new HookClickGuiScreen());
            case 240 -> shot(mc, "clickgui");
            case 250 -> {
                if (QuickUtilitiesModule.instance != null) QuickUtilitiesModule.instance.releaseToSelect.setValue(false);
                QuickWheelScreen.testHover = -1;
                mc.method_1507(new QuickWheelScreen());
            }
            case 280 -> shot(mc, "wheel_idle");
            case 290 -> QuickWheelScreen.testHover = 4;
            case 320 -> shot(mc, "wheel_aurora");
            case 330 -> QuickWheelScreen.testHover = 0;
            case 360 -> shot(mc, "wheel_calc");
            case 370 -> { QuickWheelScreen.testHover = -1; System.out.println("AURORATEST DONE"); mc.method_1592(); }
            default -> {}
        }
    }

    private static void shot(class_310 mc, String name) {
        net.minecraft.class_318.method_22690(mc.field_1697, "aurora_" + name + ".png", mc.method_1522(), 1, msg -> {});
        System.out.println("AURORATEST SHOT " + name + " status=" + AuroraClient.INSTANCE.status + " msg=" + AuroraClient.INSTANCE.message);
    }
}
