package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ChoiceSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import java.util.List;

public final class MovementSyncModule extends Module {
    public final NumberSetting innerRange = new NumberSetting(EncryptedString.of("Inner Range"), 0.0, 5.0, 2.0, 0.1);
    public final BooleanSetting airGate = new BooleanSetting(EncryptedString.of("Air Gate"), true);
    public final BooleanSetting itemGate = new BooleanSetting(EncryptedString.of("Item Gate"), true);
    public final ChoiceSetting neutralMode = new ChoiceSetting(EncryptedString.of("Neutral Mode"), "Adaptable", List.of("Passive", "Aggressive", "Adaptable", "Adaptable Inverse"));
    public final ChoiceSetting equalizer = new ChoiceSetting(EncryptedString.of("Movement Equalizer"), "Adaptable Experimental", List.of("Off", "Jump", "Equalizer Jump", "Adaptable Experimental", "Equalizer"));
    public final NumberSetting equalizerRange = new NumberSetting(EncryptedString.of("Equalizer Range"), 0.0, 4.0, 1.0, 0.1);

    public MovementSyncModule() {
        super(EncryptedString.of("Movement Sync"), EncryptedString.of("Passt die Bewegung beim Nahkampf an das Ziel an."), -1, Category.COMBAT);
        this.innerRange.setDescription(EncryptedString.of("Abstand für die innere Movement-Sync-Zone."));
        this.airGate.setDescription(EncryptedString.of("Begrenzt das Folgen auf passende Luft- und Bodensituationen."));
        this.itemGate.setDescription(EncryptedString.of("Beachtet die Item-Bedingungen aus der Vorlage."));
        this.neutralMode.setDescription(EncryptedString.of("Legt fest, wie die Bewegung bei neutralem Zielverhalten reagiert."));
        this.equalizer.setDescription(EncryptedString.of("Wählt die Bewegungsangleichung aus der Vorlage."));
        this.equalizerRange.setDescription(EncryptedString.of("Zusätzlicher Bereich für die Bewegungsangleichung."));
        this.addSettings(this.innerRange, this.airGate, this.itemGate, this.neutralMode, this.equalizer, this.equalizerRange);
    }
}
