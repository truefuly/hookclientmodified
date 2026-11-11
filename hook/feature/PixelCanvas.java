/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.ArgbColors;
import hook.feature.ColorField2D;
import hook.feature.Sdf2D;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class PixelCanvas {
    private final int width;
    private final int height;
    final int[] pixels;

    public PixelCanvas(int var1, int var2) {
        if (var1 < 1 || var2 < 1 || var1 > 2048 || var2 > 2048) {
            throw new IllegalArgumentException("canvas size must be between 1 and 2048 pixels");
        }
        this.width = var1;
        this.height = var2;
        this.pixels = new int[var1 * var2];
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public int get(int var1, int var2) {
        return var1 >= 0 && var2 >= 0 && var1 < this.width && var2 < this.height ? this.pixels[var2 * this.width + var1] : 0;
    }

    public int[] pixels() {
        return (int[])this.pixels.clone();
    }

    public PixelCanvas clear(int var1) {
        Arrays.fill(this.pixels, var1);
        return this;
    }

    public PixelCanvas fill(Sdf2D var1, int var2) {
        return this.fill(var1, ColorField2D.solid(var2), 0.0f);
    }

    public PixelCanvas fill(Sdf2D var1, int var2, float var3) {
        return this.fill(var1, ColorField2D.solid(var2), var3);
    }

    public PixelCanvas fill(Sdf2D var1, ColorField2D var2) {
        return this.fill(var1, var2, 0.0f);
    }

    public PixelCanvas fill(Sdf2D var1, ColorField2D var2, float var3) {
        float var4 = Math.max(1.0f, var3);
        int var5 = Math.max(0, (int)Math.floor(var1.minX - var4 - 1.0f));
        int var6 = Math.max(0, (int)Math.floor(var1.minY - var4 - 1.0f));
        int var7 = Math.min(this.width - 1, (int)Math.ceil(var1.maxX + var4 + 1.0f));
        int var8 = Math.min(this.height - 1, (int)Math.ceil(var1.maxY + var4 + 1.0f));
        for (int var9 = var6; var9 <= var8; ++var9) {
            float var10 = (float)var9 + 0.5f;
            for (int var11 = var5; var11 <= var7; ++var11) {
                float var12 = (float)var11 + 0.5f;
                float var13 = 0.5f - var1.distance(var12, var10) / var4;
                if (var13 <= 0.0f) continue;
                if (var13 > 1.0f) {
                    var13 = 1.0f;
                }
                this.blend(var9 * this.width + var11, var2.argb(var12, var10), var13);
            }
        }
        return this;
    }

    public PixelCanvas fillAll(ColorField2D var1) {
        for (int var2 = 0; var2 < this.height; ++var2) {
            for (int var3 = 0; var3 < this.width; ++var3) {
                this.blend(var2 * this.width + var3, var1.argb((float)var3 + 0.5f, (float)var2 + 0.5f), 1.0f);
            }
        }
        return this;
    }

    public PixelCanvas erase(Sdf2D var1) {
        return this.erase(var1, 0.0f);
    }

    public PixelCanvas erase(Sdf2D var1, float var2) {
        float var3 = Math.max(1.0f, var2);
        int var4 = Math.max(0, (int)Math.floor(var1.minX - var3 - 1.0f));
        int var5 = Math.max(0, (int)Math.floor(var1.minY - var3 - 1.0f));
        int var6 = Math.min(this.width - 1, (int)Math.ceil(var1.maxX + var3 + 1.0f));
        int var7 = Math.min(this.height - 1, (int)Math.ceil(var1.maxY + var3 + 1.0f));
        for (int var8 = var5; var8 <= var7; ++var8) {
            for (int var9 = var4; var9 <= var6; ++var9) {
                float var10 = 0.5f - var1.distance((float)var9 + 0.5f, (float)var8 + 0.5f) / var3;
                if (var10 <= 0.0f) continue;
                int var11 = var8 * this.width + var9;
                int var12 = Math.round((float)(this.pixels[var11] >>> 24) * (1.0f - Math.min(1.0f, var10)));
                this.pixels[var11] = var12 << 24 | this.pixels[var11] & 0xFFFFFF;
            }
        }
        return this;
    }

    public PixelCanvas draw(PixelCanvas var1, int var2, int var3) {
        for (int var4 = 0; var4 < var1.height; ++var4) {
            int var5 = var4 + var3;
            if (var5 < 0 || var5 >= this.height) continue;
            for (int var6 = 0; var6 < var1.width; ++var6) {
                int var7 = var6 + var2;
                if (var7 < 0 || var7 >= this.width) continue;
                this.blend(var5 * this.width + var7, var1.pixels[var4 * var1.width + var6], 1.0f);
            }
        }
        return this;
    }

    public PixelCanvas mirrored() {
        PixelCanvas var1 = new PixelCanvas(this.width, this.height);
        for (int var2 = 0; var2 < this.height; ++var2) {
            for (int var3 = 0; var3 < this.width; ++var3) {
                var1.pixels[var2 * this.width + var3] = this.pixels[var2 * this.width + this.width - 1 - var3];
            }
        }
        return var1;
    }

    public PixelCanvas copy() {
        PixelCanvas var1 = new PixelCanvas(this.width, this.height);
        System.arraycopy(this.pixels, 0, var1.pixels, 0, this.pixels.length);
        return var1;
    }

    private void blend(int var1, int var2, float var3) {
        float var4 = (float)(var2 >>> 24) / 255.0f * var3;
        if (!(var4 <= 0.0f)) {
            int var5 = this.pixels[var1];
            float var6 = (float)(var5 >>> 24) / 255.0f;
            float var7 = var4 + var6 * (1.0f - var4);
            if (var7 <= 0.0f) {
                this.pixels[var1] = 0;
            } else {
                float var8 = var6 * (1.0f - var4) / var7;
                float var9 = var4 / var7;
                int var10 = Math.round((float)(var2 >> 16 & 0xFF) * var9 + (float)(var5 >> 16 & 0xFF) * var8);
                int var11 = Math.round((float)(var2 >> 8 & 0xFF) * var9 + (float)(var5 >> 8 & 0xFF) * var8);
                int var12 = Math.round((float)(var2 & 0xFF) * var9 + (float)(var5 & 0xFF) * var8);
                int var13 = Math.round(var7 * 255.0f);
                this.pixels[var1] = ArgbColors.clamp(var13) << 24 | ArgbColors.clamp(var10) << 16 | ArgbColors.clamp(var11) << 8 | ArgbColors.clamp(var12);
            }
        }
    }
}

