package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ChoiceSetting;
import hook.utils.EncryptedString;
import java.util.List;

public final class JumpFollowingModule extends Module {
    public final ChoiceSetting mode = new ChoiceSetting(EncryptedString.of("Following Mode"), "On", List.of("Off", "On", "Neutral Based"));
    public final BooleanSetting respectResist = new BooleanSetting(EncryptedString.of("Respect Resist"), true);
    public final BooleanSetting preventBackDuringJump = new BooleanSetting(EncryptedString.of("Prevent Back During Jump"), true);

    public JumpFollowingModule() {
        super(EncryptedString.of("Jump Following"), EncryptedString.of("Steuert das Sprungverhalten passend zur Bewegung und zum Ziel."), -1, Category.COMBAT);
        this.mode.setDescription(EncryptedString.of("On aktiviert Jump Following; Neutral Based folgt dem Neutral-Modus."));
        this.respectResist.setDescription(EncryptedString.of("Beachtet den Movement-Resist-Abstand."));
        this.preventBackDuringJump.setDescription(EncryptedString.of("Hält die Vorwärtsbewegung während eines aggressiven Folge-Sprungs."));
        this.addSettings(this.mode, this.respectResist, this.preventBackDuringJump);
    }
}
