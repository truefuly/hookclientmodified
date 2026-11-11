package hook.feature;

import hook.utils.EncryptedString;
import java.util.Set;

/** Highlights deepslate-family blocks whose axis or horizontal facing is non-default. */
public final class DeepslateRotationEspModule extends BlockEspModule {
    public DeepslateRotationEspModule() {
        super(EncryptedString.of("Deepslate Rotation ESP"), EncryptedString.of("Highlights deepslate and stone blocks with a horizontal axis or facing."),
            Set.of("@deepslate"), true);
    }
}
