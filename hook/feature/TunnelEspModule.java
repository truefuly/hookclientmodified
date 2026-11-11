package hook.feature;

import hook.utils.EncryptedString;
import java.util.Set;

/** Highlights stone and deepslate tunnel materials in loaded chunks. */
public final class TunnelEspModule extends BlockEspModule {
    public TunnelEspModule() {
        super(EncryptedString.of("Tunnel ESP"), EncryptedString.of("Highlights stone, deepslate and cobbled deepslate in loaded chunks."),
            Set.of("minecraft:stone", "minecraft:deepslate", "minecraft:cobbled_deepslate"), false);
    }
}
