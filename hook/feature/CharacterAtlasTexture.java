/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.TextureFormat
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1011
 *  net.minecraft.class_1044
 */
package hook.feature;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1044;

@Environment(value=EnvType.CLIENT)
final class CharacterAtlasTexture
extends class_1044 {
    CharacterAtlasTexture(String name, int width, int height, int[][] mipPixels) {
        GpuDevice device = RenderSystem.getDevice();
        this.field_56974 = device.createTexture(() -> name, 5, TextureFormat.RGBA8, width, height, 1, mipPixels.length);
        this.field_60597 = device.createTextureView(this.field_56974);
        CommandEncoder encoder = device.createCommandEncoder();
        int mipWidth = width;
        int mipHeight = height;
        for (int level = 0; level < mipPixels.length; ++level) {
            int[] pixels = mipPixels[level];
            try (class_1011 image = new class_1011(mipWidth, mipHeight, false);){
                for (int y = 0; y < mipHeight; ++y) {
                    for (int x = 0; x < mipWidth; ++x) {
                        image.method_61941(x, y, pixels[y * mipWidth + x]);
                    }
                }
                encoder.writeToTexture(this.field_56974, image, level, 0, 0, 0, mipWidth, mipHeight, 0, 0);
            }
            mipWidth = Math.max(1, mipWidth / 2);
            mipHeight = Math.max(1, mipHeight / 2);
        }
        this.field_63613 = RenderSystem.getSamplerCache().method_76520(FilterMode.LINEAR, true);
    }
}

