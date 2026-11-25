package hook.feature;

import hook.module.Category;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.RgbSetting;
import hook.module.setting.SettingVisibility;
import hook.utils.EncryptedString;
import hook.utils.EspStyle;
import java.awt.Color;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_243;

/**
 * Other players through walls. Only five settings: how they are shown (Minecraft glow, box, outline or corners),
 * which colour, a custom colour, health bars and tracers. Everything else uses fixed good defaults
 * (bots left out, invisible players shown, low-health players flash red, far players a bit fainter).
 */
public final class PlayerEspModule extends EntityEspBase {
    public enum Mode { Glow, Box, Outline, Corners }

    public enum Colors { Health, Team, Distance, Custom }

    private static PlayerEspModule instance;
    /** Self-test only: fake players (not in the tab list) count as real players. */
    static boolean testBots;

    private final ModeSetting<Mode> mode = new ModeSetting<Mode>(EncryptedString.of("Mode"), Mode.Glow, Mode.class)
            .setDescription(EncryptedString.of("Glow: the Minecraft glowing outline, through walls. Box / Outline / Corners: drawn boxes"));
    private final ModeSetting<Colors> colors = new ModeSetting<Colors>(EncryptedString.of("Color"), Colors.Health, Colors.class)
            .setDescription(EncryptedString.of("Health: green to red. Team: scoreboard team colour. Distance: red close, green far. Custom: your own colour"));
    private final RgbSetting custom = new RgbSetting(EncryptedString.of("Custom Color"), 0xFFFF5A6E);
    private final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), false)
            .setDescription(EncryptedString.of("A line from your crosshair to every player"));

    public PlayerEspModule() {
        super("Player ESP", "Shows other players through walls: Minecraft glow or boxes, health bars and tracers", Category.RENDER, 40, 0xFFFF5A6E, true);
        instance = this;
        this.expand.getValue(0.05);
        SettingVisibility.set(this.custom, () -> this.colors.getValue() == Colors.Custom);
        this.addSettings(this.mode, this.colors, this.custom, this.healthBar, this.tracers);
    }

    void testMode(Mode m) {
        this.mode.setMode(m);
    }

    @Override
    protected void beforeFrame() {
        EspStyle s = this.style;
        Mode m = (Mode) this.mode.getValue();
        s.shape.setMode(switch (m) {
            case Box -> EspStyle.Shape.Both;
            case Corners -> EspStyle.Shape.Corners;
            default -> EspStyle.Shape.Outline;
        });
        boolean glow = m == Mode.Glow;
        s.fillOpacity.getValue(40.0);
        s.outlineOpacity.getValue(glow ? 0.0 : 230.0);
        s.outlineWidth.getValue(2.0);
        s.glow.setValue(!glow);
        s.glowStrength.getValue(45.0);
        s.pulse.setValue(false);
        s.distanceFade.setValue(true);
        s.maxDistance.getValue(0.0);
        s.colorMode.setMode(EspStyle.ColorMode.Default);
        s.tracers.setValue(this.tracers.getValue());
        s.tracerWidth.getValue(1.5);
        s.tracerOpacity.getValue(200.0);
    }

    @Override
    protected boolean drawBoxes() {
        return this.mode.getValue() != Mode.Glow;
    }

    /** For the glow mixin: the outline colour (RGB) for this entity, or 0 to leave vanilla alone. */
    public static int glowColor(class_1297 e) {
        PlayerEspModule m = instance;
        if (m == null || !m.isEnabled() || m.mode.getValue() != Mode.Glow || m.mc.field_1724 == null) return 0;
        if (!(e instanceof class_1657) || !m.shown(e)) return 0;
        int rgb = m.color(e, e.method_30950(1.0f)).getRGB() & 0xFFFFFF;
        return rgb == 0 ? 0x010101 : rgb;
    }

    @Override
    protected boolean wants(class_1297 e) {
        if (!(e instanceof class_1657 p)) return false;
        // bots / server NPCs are not in the tab list
        if (testBots || this.mc.method_1562() == null) return true;
        return this.mc.method_1562().method_2871(p.method_5667()) != null;
    }

    @Override
    protected Color color(class_1297 e, class_243 pos) {
        if (e instanceof class_1657 p && p.method_6032() + p.method_6067() < 12.0f && (System.currentTimeMillis() / 250L) % 2L == 0L) return new Color(0xFF2A2A);
        switch ((Colors) this.colors.getValue()) {
            case Team: {
                int c = e.method_22861();
                return c == 0xFFFFFF || c == 0 ? new Color(0xFF5A6E) : new Color(c & 0xFFFFFF);
            }
            case Distance:
                return this.distance(pos);
            case Custom:
                return new Color(this.custom.getArgb() & 0xFFFFFF);
            default:
                return health(e);
        }
    }
}
