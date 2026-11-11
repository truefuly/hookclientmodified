package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.utils.EncryptedString;

public final class TrueJumpResetModule extends Module {
    public TrueJumpResetModule() {
        super(EncryptedString.of("True Jump Reset"), EncryptedString.of("Löst den Jump-Reset aus, wenn Schaden durch einen Spieler erkannt wird."), -1, Category.COMBAT);
    }
}
