package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.ChoiceSetting;
import hook.utils.EncryptedString;
import java.util.List;

public final class ShieldDisableModule extends Module {
    public final ChoiceSetting mode = new ChoiceSetting(EncryptedString.of("Disable Mode"), "Normal", List.of("Normal", "Stun", "Stun Sneak"));

    public ShieldDisableModule() {
        super(EncryptedString.of("Shield Disable"), EncryptedString.of("Wechselt bei einem blockenden Schild zum Beil und führt den Schild-Disable-Angriff aus."), -1, Category.COMBAT);
        this.mode.setDescription(EncryptedString.of("Wähle Normal, Stun oder Stun Sneak."));
        this.addSetting(this.mode);
    }
}
