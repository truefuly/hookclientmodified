package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import java.util.Locale;
import java.util.function.Predicate;
import net.minecraft.class_124;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_465;
import net.minecraft.class_7923;

/** Shared parts of the GUI-clicking macros: timing settings and safe container helpers. (Ported from SilentClient.) */
public abstract class ContainerMacroModule extends Module {
    protected final NumberSetting actionDelay = new NumberSetting(EncryptedString.of("Click Delay"), 1.0, 20.0, 3.0, 1.0).getValue(EncryptedString.of("Ticks to wait after a menu opens before clicking"));
    protected final NumberSetting cycleDelay = new NumberSetting(EncryptedString.of("Cycle Delay"), 1.0, 40.0, 8.0, 1.0).getValue(EncryptedString.of("Ticks to pause between two rounds"));
    protected final NumberSetting timeout = new NumberSetting(EncryptedString.of("Timeout"), 20.0, 200.0, 80.0, 10.0).getValue(EncryptedString.of("Restarts the round if the server doesn't answer (ticks)"));
    protected final BooleanSetting autoClose = new BooleanSetting(EncryptedString.of("Close Menus"), true).setDescription(EncryptedString.of("Closes the menu after each round"));
    protected int timer;

    protected ContainerMacroModule(String name, String description) {
        super(EncryptedString.of(name), EncryptedString.of(description), -1, Category.MACROS);
        this.addSettings(this.actionDelay, this.cycleDelay, this.timeout, this.autoClose);
    }

    protected abstract String tag();

    protected abstract void restart();

    protected abstract void step(class_310 client);

    @Override
    public void onEnable() {
        this.timer = 0;
        this.restart();
        this.actionBar("Started - press the keybind again to stop", class_124.field_1060);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.actionBar("Stopped", class_124.field_1061);
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            this.setEnabled(false);
            return;
        }
        if (this.mc.field_1755 instanceof HookClickGuiScreen) return; // paused while the client menu is open
        try {
            this.timer++;
            this.step(this.mc);
        } catch (RuntimeException e) {
            this.restart();
        }
    }

    protected void actionBar(String text, class_124 color) {
        if (this.mc.field_1724 != null) {
            this.mc.field_1705.method_1758(class_2561.method_43470("[" + this.tag() + "] ").method_27692(color)
                    .method_10852(class_2561.method_43470(text).method_27692(class_124.field_1080)), false);
        }
    }

    protected void retryAfterTimeout() {
        if (this.timer > this.timeout.getIntValue()) {
            this.restart();
        }
    }

    protected static class_1703 menu(class_310 client) {
        return client.field_1755 instanceof class_465<?> screen ? screen.method_17577() : null;
    }

    protected static String title(class_310 client) {
        return client.field_1755 == null ? "" : client.field_1755.method_25440().getString().toLowerCase(Locale.ROOT);
    }

    protected static String itemPath(class_1799 stack) {
        return stack.method_7960() ? "" : class_7923.field_41178.method_10221(stack.method_7909()).method_12832();
    }

    protected static String itemName(class_1799 stack) {
        return stack.method_7960() ? "" : stack.method_7964().getString().toLowerCase(Locale.ROOT);
    }

    /** Menu index of the first slot whose item matches, or -1. */
    protected static int findSlot(class_1703 menu, Predicate<class_1799> matcher) {
        for (class_1735 slot : menu.field_7761) {
            class_1799 stack = slot.method_7677();
            if (!stack.method_7960() && matcher.test(stack)) return slot.field_7874;
        }
        return -1;
    }

    protected static boolean isConfirmItem(class_1799 stack) {
        String path = itemPath(stack);
        String name = itemName(stack);
        return ((path.contains("lime") || path.contains("light_green")) && path.contains("stained_glass"))
                || name.contains("confirm") || name.contains("bestätigen") || name.contains("bestaetigen");
    }

    protected static void click(class_310 client, class_1703 menu, int slotIndex) {
        if (client.field_1761 != null && client.field_1724 != null) {
            client.field_1761.method_2906(menu.field_7763, slotIndex, 0, class_1713.field_7790, client.field_1724);
        }
    }

    protected static void closeScreen(class_310 client) {
        if (client.field_1724 != null) client.field_1724.method_7346();
        if (client.field_1755 != null) client.method_1507(null);
    }
}
