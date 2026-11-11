package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.utils.EncryptedString;

public final class TrueJumpResetModule extends Module {
    public TrueJumpResetModule() {
        super(EncryptedString.of("True Jump Reset"), EncryptedString.of("Jump-resets automatically when a player hits you."), -1, Category.COMBAT);
    }
}
