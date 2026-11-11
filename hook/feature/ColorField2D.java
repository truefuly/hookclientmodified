/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.ArgbColors;
import hook.feature.PerlinNoise;
import hook.feature.SurfacePaint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface ColorField2D {
    public int argb(float var1, float var2);

    public static ColorField2D solid(int var0) {
        return (var1, var2) -> var0;
    }

    public static ColorField2D linear(float var0, float var1, float var2, float var3, int ... var4) {
        if (var4.length == 0) {
            throw new IllegalArgumentException("gradient needs at least one colour");
        }
        float var5 = var2 - var0;
        float var6 = var3 - var1;
        float var7 = var5 * var5 + var6 * var6;
        float var8 = var7 < 1.0E-8f ? 0.0f : 1.0f / var7;
        return (var6x, var7x) -> ColorField2D.stop(var4, ((var6x - var0) * var5 + (var7x - var1) * var6) * var8);
    }

    public static ColorField2D radial(float var0, float var1, float var2, int ... var3) {
        if (var3.length == 0) {
            throw new IllegalArgumentException("gradient needs at least one colour");
        }
        float var4 = 1.0f / Math.max(1.0E-4f, var2);
        return (var4x, var5) -> {
            float var6 = var4x - var0;
            float var7 = var5 - var1;
            return ColorField2D.stop(var3, (float)Math.sqrt(var6 * var6 + var7 * var7) * var4);
        };
    }

    public static ColorField2D elliptic(float var0, float var1, float var2, float var3, int ... var4) {
        if (var4.length == 0) {
            throw new IllegalArgumentException("gradient needs at least one colour");
        }
        float var5 = 1.0f / Math.max(1.0E-4f, var2);
        float var6 = 1.0f / Math.max(1.0E-4f, var3);
        return (var5x, var6x) -> {
            float var7 = (var5x - var0) * var5;
            float var8 = (var6x - var1) * var6;
            return ColorField2D.stop(var4, (float)Math.sqrt(var7 * var7 + var8 * var8));
        };
    }

    public static ColorField2D noise(int var0, int var1, float var2, float var3) {
        float var4 = 1.0f / Math.max(0.001f, var2);
        return (var4x, var5) -> {
            float var6 = PerlinNoise.fbm(var4x * var4, var5 * var4, 4) * var3 + 0.5f;
            return ArgbColors.mix(var0, var1, var6 < 0.0f ? 0.0f : Math.min(var6, 1.0f));
        };
    }

    public static ColorField2D grain(int var0, int var1, float var2, float var3, float var4) {
        double var5 = Math.toRadians(var2);
        float var7 = (float)Math.cos(var5);
        float var8 = (float)Math.sin(var5);
        float var9 = 1.0f / Math.max(0.1f, var3);
        return (var6, var7x) -> {
            float var14;
            float var9x = -var6 * var8 + var7x * var7;
            float var8x = var6 * var7 + var7x * var8;
            float var10 = PerlinNoise.fbm(var8x * 0.02f, var9x * 0.05f, 3) * var4;
            float var11 = (var9x + var10) * var9;
            float var12 = var11 - (float)Math.floor(var11);
            float var13 = SurfacePaint.smooth(0.6f, 0.8f, var12) * (1.0f - SurfacePaint.smooth(0.85f, 1.0f, var12));
            float var15 = var13 * 0.8f + (var14 = PerlinNoise.noise(var8x * 0.015f, var9x * var9 * 1.7f) * 0.5f + 0.5f) * 0.3f;
            return ArgbColors.mix(var0, var1, var15 < 0.0f ? 0.0f : Math.min(var15, 1.0f));
        };
    }

    private static int stop(int[] var0, float var1) {
        if (var0.length == 1) {
            return var0[0];
        }
        var1 = var1 < 0.0f ? 0.0f : Math.min(var1, 1.0f);
        float var2 = var1 * (float)(var0.length - 1);
        int var3 = Math.min((int)var2, var0.length - 2);
        return ArgbColors.mix(var0[var3], var0[var3 + 1], var2 - (float)var3);
    }
}

