package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.module.setting.ChoiceSetting;
import hook.utils.EncryptedString;
import java.util.List;

public final class SmartSwordModule extends Module {
    public final NumberSetting targetRange = new NumberSetting(EncryptedString.of("Target Range"), 1.0, 8.0, 8.0, 0.5);
    public final ChoiceSetting activation = new ChoiceSetting(EncryptedString.of("Activation"), "Hold", List.of("Hold", "Switch"));

    public SmartSwordModule() {
        super(EncryptedString.of("Smart Sword"), EncryptedString.of("Verwende den einstellbaren Smart-Sword-Keybind im Halte- oder Umschaltmodus."), -1, Category.COMBAT);
        this.targetRange.setDescription(EncryptedString.of("Maximale Reichweite für Smart Sword."));
        this.activation.setDescription(EncryptedString.of("Hold aktiviert Smart Sword nur solange der Keybind gehalten wird. Switch schaltet es mit jedem Tastendruck um."));
        this.addSettings(this.targetRange, this.activation);
    }
}
