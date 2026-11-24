package hook.feature;

import hook.module.Category;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.utils.EncryptedString;
import java.awt.Color;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_243;

/** Other players through walls: team / health / distance colours, health bars, bots left out. */
public final class PlayerEspModule extends EntityEspBase {
    public enum Colors { Team, Health, Distance, Armor }

    private final ModeSetting<Colors> colors = new ModeSetting<Colors>(EncryptedString.of("Default Colors"), Colors.Health, Colors.class)
            .setDescription(EncryptedString.of("Colours in the Default colour mode. Team: the scoreboard team colour (red without a team). Health: green to red. Distance: red close, green far. Armor: grey no armor, blue full armor"));
    private final BooleanSetting bots = new BooleanSetting(EncryptedString.of("Ignore Bots"), true)
            .setDescription(EncryptedString.of("Leaves out fake players that are not in the tab list (server NPCs, anticheat bots)"));
    private final BooleanSetting lowHealth = new BooleanSetting(EncryptedString.of("Low Health Pulse"), true)
            .setDescription(EncryptedString.of("Players under 6 hearts flash red - easy kills stand out"));

    public PlayerEspModule() {
        super("Player ESP", "Shows other players through walls with health bars, team colours and tracers", Category.RENDER, 40, 0xFFFF5A6E, true);
        this.addSettings(this.colors, this.bots, this.lowHealth, this.healthBar, this.invisible, this.expand);
        this.addSettings(this.style.settings());
    }

    @Override
    protected boolean wants(class_1297 e) {
        if (!(e instanceof class_1657 p)) return false;
        if (this.bots.getValue() && this.mc.method_1562() != null && this.mc.method_1562().method_2871(p.method_5667()) == null) return false;
        return true;
    }

    @Override
    protected Color color(class_1297 e, class_243 pos) {
        if (this.lowHealth.getValue() && e instanceof class_1657 p && p.method_6032() + p.method_6067() < 12.0f
                && (System.currentTimeMillis() / 250L) % 2L == 0L) return new Color(0xFF2A2A);
        switch ((Colors) this.colors.getValue()) {
            case Team: {
                int c = e.method_22861();
                return c == 0xFFFFFF || c == 0 ? new Color(0xFF5A6E) : new Color(c & 0xFFFFFF);
            }
            case Distance:
                return this.distance(pos);
            case Armor: {
                int armor = e instanceof class_1657 p ? p.method_6096() : 0;
                float t = Math.min(1.0f, armor / 20.0f);
                return new Color((int) (170 - 110 * t), (int) (170 - 40 * t), (int) (170 + 85 * t));
            }
            default:
                return health(e);
        }
    }
}
