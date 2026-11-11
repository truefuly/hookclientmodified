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
        super(EncryptedString.of("Movement Sync"), EncryptedString.of("Matches your movement to your target during melee fights."), -1, Category.COMBAT);
        this.innerRange.setDescription(EncryptedString.of("Distance of the inner movement sync zone."));
        this.airGate.setDescription(EncryptedString.of("Only follows when you and the target are both in the air or both on the ground."));
        this.itemGate.setDescription(EncryptedString.of("Only syncs while holding a weapon."));
        this.neutralMode.setDescription(EncryptedString.of("How to move when the target is neither approaching nor retreating."));
        this.equalizer.setDescription(EncryptedString.of("How your movement is equalized with the target."));
        this.equalizerRange.setDescription(EncryptedString.of("Extra range for movement equalizing."));
        this.addSettings(this.innerRange, this.airGate, this.itemGate, this.neutralMode, this.equalizer, this.equalizerRange);
    }
}
