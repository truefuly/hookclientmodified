package hook.feature;

import net.minecraft.class_310;
import net.minecraft.class_437;

/** Developer self-test (only with -Dhook.auroratest=<key>): key screen, wrong key, real key, panel, reopen. */
public final class AuroraTest {
    public static final String KEY = System.getProperty("hook.auroratest");
    public static final boolean ENABLED = KEY != null;
    private static int clock = -1;
    private static String savedClip;

    private AuroraTest() {
    }

    public static void tick() {
        class_310 mc = class_310.method_1551();
        if (mc.method_22683() == null) return;
        clock++;
        AuroraScreen screen = mc.field_1755 instanceof AuroraScreen a ? a : null;
        if (KEY.equals("wheel")) { wheel(mc); return; }
        if (KEY.equals("themes")) { themes(mc); return; }
        if (KEY.equals("calc")) { calc(mc); return; }
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
            case 800 -> { if (screen != null) screen.testClick("tab:connect"); }
            case 830 -> shot(mc, "reopen");
            case 835 -> System.out.println("AURORATEST LIVE v=" + AuroraClient.INSTANCE.liveVersion() + " listMax=" + (screen == null ? -1 : screen.testListMax()));
            case 840 -> { if (screen != null) System.out.println("AURORATEST DRAG grabbed=" + screen.testDragList() + " pos=" + screen.testListPos() + " max=" + screen.testListMax()); }
            case 870 -> shot(mc, "scrolled");
            case 880 -> { if (screen != null) screen.testClick("flt:online"); }
            case 910 -> shot(mc, "filter");
            case 920 -> { if (screen != null) { screen.testClick("flt:all"); screen.testClick("#allprof"); } }
            case 950 -> shot(mc, "allprof");
            case 960 -> { if (screen != null) screen.testClick("#allprof"); }
            case 1100 -> {
                System.out.println("AURORATEST LIVE v=" + AuroraClient.INSTANCE.liveVersion());
                System.out.println("AURORATEST STATUS " + AuroraClient.INSTANCE.status + " accounts=" + AuroraClient.INSTANCE.accounts.size() + " servers=" + AuroraClient.INSTANCE.servers.size() + " active=" + AuroraClient.INSTANCE.activeServers.size() + " rewards=" + AuroraClient.INSTANCE.rewards.size() + " heads=" + AuroraClient.INSTANCE.heads.size());
                System.out.println("AURORATEST DONE");
                mc.method_1592();
            }
            default -> {}
        }
    }

    /** -Dhook.auroratest=themes: every lobby theme on the title + multiplayer screen, then the proxy screen and a ping through a local SOCKS5 test proxy. */
    private static void themes(class_310 mc) {
        int n = LobbyTheme.values().length; // 1..n-1 (Standard is vanilla)
        int base = 200, per = 90;
        if (clock >= base && clock < base + (n - 1) * per) {
            int i = 1 + (clock - base) / per, k = (clock - base) % per;
            LobbyTheme t = LobbyTheme.values()[i];
            if (k == 0) { hook.module.modules.client.Hook.lobbyTheme.setModeIndex(i); mc.method_1507(new net.minecraft.class_442()); }
            if (k == 40) shot(mc, "t_" + t.folder + "_title");
            if (k == 45) mc.method_1507(new net.minecraft.class_500(new net.minecraft.class_442()));
            if (k == 85) shot(mc, "t_" + t.folder + "_mp");
            return;
        }
        int t0 = base + (n - 1) * per;
        if (clock == t0) {
            hook.module.modules.client.Hook.lobbyTheme.setModeIndex(LobbyTheme.HookVisualsDark.ordinal());
            int added = ProxyManager.importText("127.0.0.1:1085:testuser:testpass");
            ProxyManager.Proxy p = ProxyManager.all().stream().filter(x -> x.port == 1085).findFirst().orElse(null);
            if (p != null) { p.name = "Local test proxy"; ProxyManager.setEnabled(p, true); }
            System.out.println("AURORATEST PROXY added=" + added + " active=" + (ProxyManager.active() == null ? "-" : ProxyManager.active().address()));
            mc.method_1507(new ProxyScreen(new net.minecraft.class_442()));
        }
        if (clock == t0 + 40) shot(mc, "proxy_list");
        if (clock == t0 + 45 && mc.field_1755 instanceof ProxyScreen ps) ps.testEdit(0);
        if (clock == t0 + 75) shot(mc, "proxy_edit");
        if (clock == t0 + 80) mc.method_1507(new net.minecraft.class_500(new net.minecraft.class_442())); // pings 127.0.0.1:25599 through the proxy
        if (clock == t0 + 200) shot(mc, "proxy_mp");
        if (clock == t0 + 210) {
            ProxyManager.all().removeIf(x -> x.port == 1085);
            ProxyManager.save();
            System.out.println("AURORATEST PROXY cleaned active=" + (ProxyManager.active() == null ? "-" : "?"));
            System.out.println("AURORATEST DONE");
            mc.method_1592();
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
            case 290 -> QuickWheelScreen.testHover = 4; // MinecraftAFK
            case 320 -> shot(mc, "wheel_aurora");
            case 330 -> QuickWheelScreen.testHover = 0;
            case 360 -> shot(mc, "wheel_calc");
            case 370 -> { QuickWheelScreen.testHover = -1; System.out.println("AURORATEST DONE"); mc.method_1592(); }
            default -> {}
        }
    }

    /** -Dhook.auroratest=calc: calculator with grouped numbers, divide key and the copy button. */
    private static void calc(class_310 mc) {
        CalculatorScreen c = mc.field_1755 instanceof CalculatorScreen cs ? cs : null;
        switch (clock) {
            case 200 -> mc.method_1507(new CalculatorScreen());
            case 230 -> { if (c != null) for (String k : "1234567.5*12/4".split("")) c.press(k); }
            case 250 -> shot(mc, "calc_expr");
            case 260 -> { if (c != null) c.press("="); }
            case 280 -> shot(mc, "calc_result");
            case 290 -> { savedClip = mc.field_1774.method_1460(); if (c != null) c.testCopy(); }
            case 300 -> {
                shot(mc, "calc_copied");
                System.out.println("AURORATEST CLIP " + mc.field_1774.method_1460() + " pretty=" + CalculatorScreen.pretty("-1234567.5*-12/4+1000"));
                mc.field_1774.method_1455(savedClip == null ? "" : savedClip); // give the user's clipboard back
            }
            case 330 -> { System.out.println("AURORATEST DONE"); mc.method_1592(); }
            default -> {}
        }
    }

    private static void shot(class_310 mc, String name) {
        net.minecraft.class_318.method_22690(mc.field_1697, "aurora_" + name + ".png", mc.method_1522(), 1, msg -> {});
        System.out.println("AURORATEST SHOT " + name + " status=" + AuroraClient.INSTANCE.status + " msg=" + AuroraClient.INSTANCE.message);
    }
}
