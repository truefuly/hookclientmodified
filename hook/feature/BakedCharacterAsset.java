/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.CharacterDefinition;
import hook.feature.CharacterRig;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class BakedCharacterAsset
extends CharacterDefinition {
    private final Source source;

    public BakedCharacterAsset(String id, String name, Source source) {
        super(id, name);
        this.source = source;
    }

    public static BakedCharacterAsset builtIn(String id, String name, String resourcePath) {
        return new BakedCharacterAsset(id, name, () -> {
            InputStream stream = BakedCharacterAsset.class.getResourceAsStream(resourcePath);
            if (stream == null) {
                throw new FileNotFoundException(resourcePath);
            }
            return stream;
        });
    }

    InputStream open() throws IOException {
        return this.source.open();
    }

    @Override
    protected void define(CharacterRig rig) {
        throw new UnsupportedOperationException(this.id() + " is baked and has no rig");
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Source {
        public InputStream open() throws IOException;
    }
}

