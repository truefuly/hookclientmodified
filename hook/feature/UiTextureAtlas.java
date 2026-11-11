/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface UiTextureAtlas {
    public TexturePixels decode(byte[] var1) throws IOException;

    @Environment(value=EnvType.CLIENT)
    public record TexturePixels(int width, int height, int[] argb) {
    }
}

