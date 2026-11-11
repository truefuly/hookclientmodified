package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;

public final class MovementResistModule extends Module {
    public final NumberSetting range = new NumberSetting(EncryptedString.of("Resist Range"), 0.0, 8.0, 3.6, 0.1);
    public final BooleanSetting escape = new BooleanSetting(EncryptedString.of("Escape"), false);

    public MovementResistModule() {
        super(EncryptedString.of("Movement Resist"), EncryptedString.of("Wendet die einstellbare Resist-Logik im Nahkampf an."), -1, Category.COMBAT);
        this.range.setDescription(EncryptedString.of("Reichweite für Movement Resist."));
        this.escape.setDescription(EncryptedString.of("Erlaubt die Escape-Variante der Resist-Logik."));
        this.addSettings(this.range, this.escape);
    }
}
