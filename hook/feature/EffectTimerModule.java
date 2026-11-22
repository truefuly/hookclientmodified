package hook.feature;

import hook.event.EventListener;
import hook.event.events.MouseButtonEvent;
import hook.event.events.MouseScrolledEvent;
import hook.event.events.Render2DEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.utils.EncryptedString;

/**
 * Clean glass HUD card listing your active potion effects with icon, level and time left (plus a draining bar in
 * the effect's color; the last 10 seconds blink). It is a normal HUD element: open chat to move it (snaps to the
 * center lines, keeps its place on window resize) and scroll on it to resize it.
 */
public final class EffectTimerModule extends Module {
    private static EffectTimerModule instance;

    private final BooleanSetting icons = new BooleanSetting(EncryptedString.of("Icons"), true).setDescription(EncryptedString.of("Effect icons in front of the names"));
    private final BooleanSetting bars = new BooleanSetting(EncryptedString.of("Time Bars"), true).setDescription(EncryptedString.of("Thin bar under every effect that drains with its time"));
    private final BooleanSetting ambient = new BooleanSetting(EncryptedString.of("Beacon Effects"), true).setDescription(EncryptedString.of("Also list effects from beacons and conduits"));
    private final BooleanSetting hideVanilla = new BooleanSetting(EncryptedString.of("Hide Vanilla Effects"), true).setDescription(EncryptedString.of("Hides Minecraft's own effect icons in the top right corner"));

    public EffectTimerModule() {
        super(EncryptedString.of("Effect Timer"), EncryptedString.of("Clean HUD list of your potion effects with their timers; move and resize it in the HUD editor (open chat)"), -1, Category.RENDER);
        this.addSettings(this.icons, this.bars, this.ambient, this.hideVanilla);
        instance = this;
    }

    static boolean active() {
        EffectTimerModule m = instance;
        return m != null && m.isEnabled();
    }

    static boolean icons() {
        return instance == null || instance.icons.getValue();
    }

    static boolean bars() {
        return instance == null || instance.bars.getValue();
    }

    static boolean ambient() {
        return instance == null || instance.ambient.getValue();
    }

    /** Whether Minecraft's own effect overlay should be hidden because the Effect Timer replaces it. */
    public static boolean hidesVanilla() {
        EffectTimerModule m = instance;
        return m != null && m.isEnabled() && m.hideVanilla.getValue();
    }

    private static HudModule hud() {
        HudModule h = HudModule.instance;
        return h != null && !h.isEnabled() ? h : null; // only stand in while the HUD module itself is off
    }

    @EventListener
    public void onRender2D(Render2DEvent event) {
        HudModule h = hud();
        if (h != null) h.renderEffectsOnly(event);
    }

    @EventListener
    public void onMouseButton(MouseButtonEvent event) {
        HudModule h = hud();
        if (h != null) h.onMouseButton(event);
    }

    @EventListener
    public void onMouseScrolled(MouseScrolledEvent event) {
        HudModule h = hud();
        if (h != null) h.onMouseScrolled(event);
    }
}
