/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.UiTextureData;
import java.util.HashMap;
import java.util.Map;

final class UiProceduralTextures {
    private static final int SS = 6;
    private static final Map<String, UiTextureData> CACHE = new HashMap<String, UiTextureData>();

    private UiProceduralTextures() {
    }

    static UiTextureData circle(int var0) {
        return CACHE.computeIfAbsent("c" + var0, var1 -> {
            int var2 = var0 * 2;
            float[] var3 = new float[var2 * var2];
            for (int var4 = 0; var4 < var2; ++var4) {
                for (int var5 = 0; var5 < var2; ++var5) {
                    var3[var4 * var2 + var5] = UiProceduralTextures.discCoverage(var5, var4, var0, var0, var0);
                }
            }
            return UiTextureData.fromCoverage(var1, var2, var2, var3);
        });
    }

    static UiTextureData ring(int var0, float var1) {
        return CACHE.computeIfAbsent("r" + var0 + "_" + var1, var2 -> {
            int var3 = var0 * 2;
            float[] var4 = new float[var3 * var3];
            for (int var5 = 0; var5 < var3; ++var5) {
                for (int var6 = 0; var6 < var3; ++var6) {
                    var4[var5 * var3 + var6] = Math.max(0.0f, UiProceduralTextures.discCoverage(var6, var5, var0, var0, var0) - UiProceduralTextures.discCoverage(var6, var5, var0, var0, (float)var0 - var1));
                }
            }
            return UiTextureData.fromCoverage(var2, var3, var3, var4);
        });
    }

    static UiTextureData shadow(int var0, int var1) {
        return CACHE.computeIfAbsent("s" + var0 + "_" + var1, var2 -> {
            int var3 = var0 + var1;
            int var4 = var3 * 2;
            float[] var5 = new float[var4 * var4];
            for (int var6 = 0; var6 < var4; ++var6) {
                for (int var7 = 0; var7 < var4; ++var7) {
                    float var14;
                    double var8 = (double)var7 + 0.5 - (double)var3;
                    double var10 = (double)var6 + 0.5 - (double)var3;
                    double var12 = Math.sqrt(var8 * var8 + var10 * var10) - (double)var0;
                    if (var12 < -1.0) {
                        var14 = 0.0f;
                    } else {
                        double var15 = Math.pow(Math.max(0.0, 1.0 - Math.max(0.0, var12) / (double)var1), 2.6);
                        var14 = (float)(var15 * Math.min(1.0, var12 + 1.0));
                    }
                    var5[var6 * var4 + var7] = var14;
                }
            }
            return UiTextureData.fromCoverage(var2, var4, var4, var5);
        });
    }

    static UiTextureData radial() {
        return CACHE.computeIfAbsent("radial", var0 -> {
            int var1 = 128;
            float[] var2 = new float[var1 * var1];
            for (int var3 = 0; var3 < var1; ++var3) {
                for (int var4 = 0; var4 < var1; ++var4) {
                    double var5 = ((double)var4 + 0.5 - 64.0) / 64.0;
                    double var7 = ((double)var3 + 0.5 - 64.0) / 64.0;
                    double var9 = Math.sqrt(var5 * var5 + var7 * var7);
                    double var11 = Math.exp(-var9 * var9 * 4.2);
                    double var13 = Math.min(1.0, Math.max(0.0, (1.0 - var9) / 0.25));
                    var2[var3 * var1 + var4] = (float)(var11 * var13 * var13);
                }
            }
            return UiTextureData.fromCoverage(var0, var1, var1, var2);
        });
    }

    private static float discCoverage(int var0, int var1, double var2, double var4, double var6) {
        if (var6 <= 0.0) {
            return 0.0f;
        }
        double var8 = var6 * var6;
        int var10 = 0;
        for (int var11 = 0; var11 < 6; ++var11) {
            for (int var12 = 0; var12 < 6; ++var12) {
                double var13 = (double)var0 + ((double)var12 + 0.5) / 6.0 - var2;
                double var15 = (double)var1 + ((double)var11 + 0.5) / 6.0 - var4;
                if (!(var13 * var13 + var15 * var15 <= var8)) continue;
                ++var10;
            }
        }
        return (float)var10 / 36.0f;
    }
}

