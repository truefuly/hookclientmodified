package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_2561;

/**
 * Reconnects you to the last server after a kick or lost connection. The disconnect screen shows a countdown and a
 * "Cancel" button; leaving a server yourself (Disconnect in the pause menu) never triggers it.
 */
public final class AutoReconnectModule extends Module {
    private static AutoReconnectModule instance;
    private static boolean registered;

    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 1.0, 60.0, 5.0, 1.0).getValue(EncryptedString.of("Seconds to wait before reconnecting"));
    private final NumberSetting attempts = new NumberSetting(EncryptedString.of("Max Attempts"), 0.0, 50.0, 0.0, 1.0).getValue(EncryptedString.of("Gives up after this many tries in a row (0 = keep trying forever)"));

    /** The server you were last playing on (kept after the disconnect). */
    private static net.minecraft.class_642 lastServer;
    private static int tries;
    private static boolean cancelled;
    private static long reconnectAt;
    private static net.minecraft.class_437 countdownScreen;

    public AutoReconnectModule() {
        super(EncryptedString.of("Auto Reconnect"), EncryptedString.of("Reconnects you to the server automatically when you get kicked or lose connection"), -1, Category.AUTOMATION);
        this.addSettings(this.delay, this.attempts);
        instance = this;
        register();
    }

    @EventListener
    public void onTick(TickEvent event) {
        net.minecraft.class_642 cur = this.mc.method_1558();
        if (cur != null && !this.mc.method_1542() && this.mc.field_1724 != null) {
            lastServer = cur;
            tries = 0; // back in game: the next disconnect starts a fresh series
            cancelled = false;
        }
    }

    /** Test harness only. */
    static void testServer(String name, String address) {
        lastServer = new net.minecraft.class_642(name, address, net.minecraft.class_642.class_8678.field_45611);
        tries = 0;
        cancelled = false;
    }

    static int testTries() {
        return tries;
    }

    static void testCancel() {
        cancelled = true;
    }

    private static boolean active() {
        return instance != null && instance.isEnabled();
    }

    private static void register() {
        if (registered) return;
        registered = true;
        try {
            net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
                if (!(screen instanceof net.minecraft.class_419) || !active() || lastServer == null || cancelled) return;
                int max = instance.attempts.getIntValue();
                if (max > 0 && tries >= max) return;
                if (countdownScreen != screen) {
                    countdownScreen = screen;
                    reconnectAt = System.currentTimeMillis() + instance.delay.getIntValue() * 1000L;
                }
                net.minecraft.class_4185 cancel = net.minecraft.class_4185.method_46430(class_2561.method_43470("Cancel Auto Reconnect"), btn -> {
                    cancelled = true;
                    btn.field_22764 = false;
                }).method_46434(w / 2 - 100, h - 28, 200, 20).method_46431();
                net.fabricmc.fabric.api.client.screen.v1.Screens.getButtons(screen).add(cancel);
                net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterRender(screen).register((s, ctx, mx, my, delta) -> {
                    if (cancelled || !active() || countdownScreen != s) return;
                    long left = Math.max(0L, reconnectAt - System.currentTimeMillis());
                    String n = lastServer.field_3752 == null || lastServer.field_3752.isBlank() ? lastServer.field_3761 : lastServer.field_3752;
                    String msg = "Reconnecting to " + n + " in " + (left + 999L) / 1000L + "s" + (tries > 0 ? " (try " + (tries + 1) + ")" : "");
                    ctx.method_25300(client.field_1772, msg, w / 2, h - 44, 0xFFB8F0C8);
                });
                net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterTick(screen).register(s -> {
                    if (cancelled || !active() || countdownScreen != s || System.currentTimeMillis() < reconnectAt || client.field_1687 != null) return; // only once the old world is fully gone
                    countdownScreen = null;
                    tries++;
                    net.minecraft.class_642 info = lastServer;
                    client.execute(() -> net.minecraft.class_412.method_36877(new net.minecraft.class_500(new net.minecraft.class_442()), client, net.minecraft.class_639.method_2950(info.field_3761), info, false, null));
                });
            });
        } catch (Throwable t) {
            System.err.println("Hook: Auto Reconnect unavailable (Fabric API screen events missing): " + t);
        }
    }
}
