package hook.feature;

import hook.module.Category;
import hook.utils.EncryptedString;

/** Glazed-tab entry using Hook's light-update based chunk flagging and render controls. */
public final class GlazedChunkFinderModule extends ChunkFinderModule {
    public GlazedChunkFinderModule() {
        super(EncryptedString.of("Glazed Chunk Finder"), Category.GLAZED);
    }
}
