package hook.feature;

import hook.event.EventListener;
import hook.event.events.MouseButtonEvent;
import hook.event.events.MouseScrolledEvent;
import hook.event.events.Render2DEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.utils.EncryptedString;

/**
 * Armor HUD in the style of Uku's Armor HUD: your armor pieces in slots right next to the hotbar, each with a
 * durability bar and the durability left (green to red, blinks when almost broken). It is a normal HUD element:
 * open chat to move it, scroll on it to resize it, right click it in the editor to hide it.
 */
public final class ArmorHudModule extends Module {
    public enum Layout { Horizontal, Vertical }

    public enum Durability { Percent, Number, BarOnly, Off }

    private static ArmorHudModule instance;

    private final ModeSetting<Layout> layout = new ModeSetting<Layout>(EncryptedString.of("Layout"), Layout.Horizontal, Layout.class).setDescription(EncryptedString.of("Pieces in a row (next to the hotbar) or stacked on top of each other"));
    private final ModeSetting<Durability> durability = new ModeSetting<Durability>(EncryptedString.of("Durability"), Durability.Percent, Durability.class).setDescription(EncryptedString.of("Percent or points left, only the bar, or nothing"));
    private final BooleanSetting hands = new BooleanSetting(EncryptedString.of("Held Items"), false).setDescription(EncryptedString.of("Also shows the item in your main hand and offhand"));
    private final BooleanSetting empty = new BooleanSetting(EncryptedString.of("Empty Slots"), true).setDescription(EncryptedString.of("Keeps an empty slot where a piece is missing"));
    private final BooleanSetting warn = new BooleanSetting(EncryptedString.of("Low Durability Warning"), true).setDescription(EncryptedString.of("The slot blinks red below 10% durability"));

    public ArmorHudModule() {
        super(EncryptedString.of("Armor HUD"), EncryptedString.of("Your armor with its durability next to the hotbar; move and resize it in the HUD editor (open chat)"), -1, Category.RENDER);
        this.addSettings(this.layout, this.durability, this.hands, this.empty, this.warn);
        instance = this;
    }

    static boolean active() {
        ArmorHudModule m = instance;
        return m != null && m.isEnabled();
    }

    static boolean vertical() {
        return instance != null && instance.layout.getValue() == Layout.Vertical;
    }

    static int durabilityMode() {
        return instance == null ? 0 : instance.durability.getValue().ordinal();
    }

    static boolean hands() {
        return instance != null && instance.hands.getValue();
    }

    static boolean emptySlots() {
        return instance == null || instance.empty.getValue();
    }

    static boolean warn() {
        return instance == null || instance.warn.getValue();
    }

    /** Test harness only: switch layout / held items. */
    static void testSet(boolean vertical, boolean held) {
        if (instance == null) return;
        instance.layout.setMode(vertical ? Layout.Vertical : Layout.Horizontal);
        instance.hands.setValue(held);
    }

    private static HudModule hud() {
        HudModule h = HudModule.instance;
        return h != null && !h.isEnabled() ? h : null; // only stand in while the HUD module itself is off
    }

    @EventListener
    public void onRender2D(Render2DEvent event) {
        HudModule h = hud();
        if (h != null) h.renderEffectsOnly(event); // draws once per frame even if the Effect Timer also asks
    }

    @EventListener
    public void onMouseButton(MouseButtonEvent event) {
        HudModule h = hud();
        if (h != null && !EffectTimerModule.active()) h.onMouseButton(event);
    }

    @EventListener
    public void onMouseScrolled(MouseScrolledEvent event) {
        HudModule h = hud();
        if (h != null && !EffectTimerModule.active()) h.onMouseScrolled(event);
    }
}
