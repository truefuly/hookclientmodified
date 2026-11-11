package hook.feature;

import hook.module.Category;
import hook.utils.EncryptedString;
import java.util.Set;

/** Glazed-tab preset for finding amethyst geode blocks in loaded chunks. */
public final class GlazedAmethystEspModule extends BlockEspModule {
    public GlazedAmethystEspModule() {
        super(EncryptedString.of("Glazed Amethyst ESP"),
            EncryptedString.of("Highlights amethyst blocks and budding amethyst in loaded chunks."),
            Set.of("minecraft:amethyst_block", "minecraft:budding_amethyst", "minecraft:amethyst_cluster",
                "minecraft:large_amethyst_bud", "minecraft:medium_amethyst_bud", "minecraft:small_amethyst_bud"),
            false, Category.GLAZED);
    }
}
