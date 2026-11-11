package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;

public final class SprintHitFollowModule extends Module {
    public final NumberSetting jumpDelay = new NumberSetting(EncryptedString.of("Jump Delay (ticks)"), 0.0, 10.0, 2.0, 1.0);

    public SprintHitFollowModule() {
        super(EncryptedString.of("Sprint Hit Follow"), EncryptedString.of("Setzt den Sprint-Hit-Follow-Through nach einem Treffer um."), -1, Category.COMBAT);
        this.jumpDelay.setDescription(EncryptedString.of("Verzögerung bis zum Folge-Sprung in Ticks."));
        this.addSetting(this.jumpDelay);
    }
}
