package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;

public final class STappingModule extends Module {
    public final NumberSetting duration = new NumberSetting(EncryptedString.of("Tap Length (ticks)"), 0.0, 5.0, 2.0, 1.0);

    public STappingModule() {
        super(EncryptedString.of("S Tapping"), EncryptedString.of("Tippt nach einem gültigen Treffer kurz S, um die Bewegung zurückzusetzen."), -1, Category.COMBAT);
        this.duration.setDescription(EncryptedString.of("Dauer des S-Taps in Ticks. 0 deaktiviert den Tap."));
        this.addSetting(this.duration);
    }
}
