package hook.feature;

import hook.module.Category;
import hook.utils.EncryptedString;
import java.util.Set;

/** Highlights deepslate-family blocks with unnatural rotation, under the Glazed tab. */
public final class GlazedRotatedDeepslateEspModule extends BlockEspModule {
    public GlazedRotatedDeepslateEspModule() {
        super(EncryptedString.of("Glazed Rotated Deepslate ESP"),
            EncryptedString.of("Highlights deepslate and stone blocks with an unusual axis or facing."),
            Set.of("@deepslate"), true, Category.GLAZED);
    }
}
