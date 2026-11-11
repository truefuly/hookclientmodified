/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class PerlinNoise {
    private static final int[] PERM = new int[512];

    private PerlinNoise() {
    }

    public static float noise(float var0, float var1, float var2) {
        int var3 = PerlinNoise.floor(var0);
        int var4 = PerlinNoise.floor(var1);
        int var5 = PerlinNoise.floor(var2);
        float var6 = var0 - (float)var3;
        float var7 = var1 - (float)var4;
        float var8 = var2 - (float)var5;
        int var9 = var3 & 0xFF;
        int var10 = var4 & 0xFF;
        int var11 = var5 & 0xFF;
        float var12 = PerlinNoise.fade(var6);
        float var13 = PerlinNoise.fade(var7);
        float var14 = PerlinNoise.fade(var8);
        int var15 = PERM[var9] + var10;
        int var16 = PERM[var15] + var11;
        int var17 = PERM[var15 + 1] + var11;
        int var18 = PERM[var9 + 1] + var10;
        int var19 = PERM[var18] + var11;
        int var20 = PERM[var18 + 1] + var11;
        float var21 = PerlinNoise.lerp(PerlinNoise.grad(PERM[var16], var6, var7, var8), PerlinNoise.grad(PERM[var19], var6 - 1.0f, var7, var8), var12);
        float var22 = PerlinNoise.lerp(PerlinNoise.grad(PERM[var17], var6, var7 - 1.0f, var8), PerlinNoise.grad(PERM[var20], var6 - 1.0f, var7 - 1.0f, var8), var12);
        float var23 = PerlinNoise.lerp(var21, var22, var13);
        float var24 = PerlinNoise.lerp(PerlinNoise.grad(PERM[var16 + 1], var6, var7, var8 - 1.0f), PerlinNoise.grad(PERM[var19 + 1], var6 - 1.0f, var7, var8 - 1.0f), var12);
        float var25 = PerlinNoise.lerp(PerlinNoise.grad(PERM[var17 + 1], var6, var7 - 1.0f, var8 - 1.0f), PerlinNoise.grad(PERM[var20 + 1], var6 - 1.0f, var7 - 1.0f, var8 - 1.0f), var12);
        return PerlinNoise.lerp(var23, PerlinNoise.lerp(var24, var25, var13), var14);
    }

    public static float noise(float var0, float var1) {
        return PerlinNoise.noise(var0, var1, 0.37f);
    }

    public static float fbm(float var0, float var1, float var2, int var3) {
        float var4 = 0.0f;
        float var5 = 0.5f;
        float var6 = 0.0f;
        for (int var7 = 0; var7 < var3; ++var7) {
            var4 += var5 * PerlinNoise.noise(var0, var1, var2);
            var6 += var5;
            var0 = var0 * 2.03f + 17.1f;
            var1 = var1 * 2.03f - 4.7f;
            var2 = var2 * 2.03f + 9.3f;
            var5 *= 0.5f;
        }
        return var4 / var6;
    }

    public static float fbm(float var0, float var1, int var2) {
        return PerlinNoise.fbm(var0, var1, 0.37f, var2);
    }

    public static float hash(int var0, int var1, int var2) {
        int var3 = var0 * 0x1F1F1F1F ^ var1 * 1540483477 ^ var2 * 668265263;
        var3 ^= var3 >>> 13;
        var3 *= -2048144789;
        var3 ^= var3 >>> 16;
        return (float)(var3 & 0xFFFF) / 65535.0f;
    }

    private static int floor(float var0) {
        int var1 = (int)var0;
        return var0 < (float)var1 ? var1 - 1 : var1;
    }

    private static float fade(float var0) {
        return var0 * var0 * var0 * (var0 * (var0 * 6.0f - 15.0f) + 10.0f);
    }

    private static float lerp(float var0, float var1, float var2) {
        return var0 + (var1 - var0) * var2;
    }

    private static float grad(int var0, float var1, float var2, float var3) {
        float var5;
        int var4 = var0 & 0xF;
        float f = var5 = var4 < 8 ? var1 : var2;
        float var6 = var4 < 4 ? var2 : (var4 != 12 && var4 != 14 ? var3 : var1);
        return ((var4 & 1) == 0 ? var5 : -var5) + ((var4 & 2) == 0 ? var6 : -var6);
    }

    static {
        int[] var0 = new int[256];
        int var1 = 0;
        while (var1 < 256) {
            var0[var1] = var1++;
        }
        long var2 = 25214903917L;
        for (int var4 = 255; var4 > 0; --var4) {
            var2 = var2 * 6364136223846793005L + 1442695040888963407L;
            int var5 = (int)((var2 >>> 33) % (long)(var4 + 1));
            int var6 = var0[var4];
            var0[var4] = var0[var5];
            var0[var5] = var6;
        }
        for (int var7 = 0; var7 < 512; ++var7) {
            PerlinNoise.PERM[var7] = var0[var7 & 0xFF];
        }
    }
}

