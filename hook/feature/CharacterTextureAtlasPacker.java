/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.ArgbColors;
import hook.feature.PixelCanvas;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class CharacterTextureAtlasPacker {
    static final int WHITE = 16;
    private static final int PAD = 3;
    private static final int MAX = 2048;
    private static final int LEVELS = 5;
    final int width;
    final int height;
    final int[][] mips;
    private final Map<PixelCanvas, int[]> rects;

    private CharacterTextureAtlasPacker(int var1, int var2, int[][] var3, Map<PixelCanvas, int[]> var4) {
        this.width = var1;
        this.height = var2;
        this.mips = var3;
        this.rects = var4;
    }

    float whiteU() {
        return 8.0f / (float)this.width;
    }

    float whiteV() {
        return 8.0f / (float)this.height;
    }

    float u(PixelCanvas var1, float var2) {
        int[] var3 = this.rects.get(var1);
        return ((float)var3[0] + var2 * (float)var3[2]) / (float)this.width;
    }

    float v(PixelCanvas var1, float var2) {
        int[] var3 = this.rects.get(var1);
        return ((float)var3[1] + var2 * (float)var3[3]) / (float)this.height;
    }

    static CharacterTextureAtlasPacker pack(List<PixelCanvas> var0) {
        ArrayList<PixelCanvas> var1 = new ArrayList<PixelCanvas>();
        for (PixelCanvas var3 : var0) {
            if (var1.contains(var3)) continue;
            var1.add(var3);
        }
        var1.sort((var0x, var1x) -> Integer.compare(var1x.height(), var0x.height()));
        long var14 = 484L;
        int var4 = 22;
        for (Object e : var1) {
            PixelCanvas var6 = (PixelCanvas)e;
            var14 += (long)(var6.width() + 6) * (long)(var6.height() + 6);
            var4 = Math.max(var4, var6.width() + 6);
        }
        int var15 = CharacterTextureAtlasPacker.pow2(Math.max(var4, (int)Math.ceil(Math.sqrt(var14))));
        while (true) {
            IdentityHashMap<PixelCanvas, int[]> identityHashMap = new IdentityHashMap<PixelCanvas, int[]>();
            int var7 = 22;
            int var8 = 0;
            int var9 = 22;
            for (Object e : var1) {
                PixelCanvas var11 = (PixelCanvas)e;
                int var12 = var11.width() + 6;
                int var13 = var11.height() + 6;
                if (var7 + var12 > var15) {
                    var7 = 0;
                    var8 += var9;
                    var9 = 0;
                }
                int[] var10002 = new int[]{var7 + 3, var8 + 3, var11.width(), var11.height()};
                identityHashMap.put(var11, var10002);
                var7 += var12;
                var9 = Math.max(var9, var13);
            }
            int var17 = CharacterTextureAtlasPacker.pow2(Math.max(22, var8 + var9));
            if (var17 <= 2048 || var15 >= 2048) {
                if (var17 > 2048) {
                    throw new IllegalStateException("decals do not fit into a 2048x2048 atlas");
                }
                return CharacterTextureAtlasPacker.render(var15, var17, var1, identityHashMap);
            }
            var15 *= 2;
        }
    }

    private static CharacterTextureAtlasPacker render(int var0, int var1, List<PixelCanvas> var2, Map<PixelCanvas, int[]> var3) {
        int[] var4 = new int[var0 * var1];
        for (int var5 = 0; var5 < 19; ++var5) {
            for (int var6 = 0; var6 < 19; ++var6) {
                var4[var5 * var0 + var6] = -1;
            }
        }
        for (PixelCanvas var14 : var2) {
            int[] var7 = var3.get(var14);
            int[] var8 = CharacterTextureAtlasPacker.dilate(var14);
            for (int var9 = -3; var9 < var14.height() + 3; ++var9) {
                int var10 = Math.max(0, Math.min(var14.height() - 1, var9));
                for (int var11 = -3; var11 < var14.width() + 3; ++var11) {
                    int var12 = Math.max(0, Math.min(var14.width() - 1, var11));
                    var4[(var7[1] + var9) * var0 + var7[0] + var11] = var8[var10 * var14.width() + var12];
                }
            }
        }
        return new CharacterTextureAtlasPacker(var0, var1, CharacterTextureAtlasPacker.mips(var4, var0, var1), var3);
    }

    static int[][] mips(int[] var0, int var1, int var2) {
        int var3 = Math.min(5, 1 + (31 - Integer.numberOfLeadingZeros(Math.min(var1, var2))));
        int[][] var4 = new int[var3][];
        var4[0] = var0;
        int var5 = var1;
        int var6 = var2;
        for (int var7 = 1; var7 < var3; ++var7) {
            var4[var7] = CharacterTextureAtlasPacker.down(var4[var7 - 1], var5, var6);
            var5 = Math.max(1, var5 / 2);
            var6 = Math.max(1, var6 / 2);
        }
        return var4;
    }

    private static int[] dilate(PixelCanvas var0) {
        int var1 = var0.width();
        int var2 = var0.height();
        int[] var3 = Arrays.copyOf(var0.pixels, var0.pixels.length);
        boolean[] var4 = new boolean[var3.length];
        for (int var5 = 0; var5 < var3.length; ++var5) {
            var4[var5] = var3[var5] >>> 24 != 0;
        }
        for (int var19 = 0; var19 < 4; ++var19) {
            boolean[] var6 = (boolean[])var4.clone();
            for (int var7 = 0; var7 < var2; ++var7) {
                for (int var8 = 0; var8 < var1; ++var8) {
                    int var9 = var7 * var1 + var8;
                    if (var4[var9]) continue;
                    int var10 = 0;
                    int var11 = 0;
                    int var12 = 0;
                    int var13 = 0;
                    for (int var14 = -1; var14 <= 1; ++var14) {
                        for (int var15 = -1; var15 <= 1; ++var15) {
                            int var18;
                            int var16 = var8 + var15;
                            int var17 = var7 + var14;
                            if (var16 < 0 || var17 < 0 || var16 >= var1 || var17 >= var2 || !var4[var18 = var17 * var1 + var16]) continue;
                            var10 += var3[var18] >> 16 & 0xFF;
                            var11 += var3[var18] >> 8 & 0xFF;
                            var12 += var3[var18] & 0xFF;
                            ++var13;
                        }
                    }
                    if (var13 == 0) continue;
                    var3[var9] = var10 / var13 << 16 | var11 / var13 << 8 | var12 / var13;
                    var6[var9] = true;
                }
            }
            var4 = var6;
        }
        return var3;
    }

    private static int[] down(int[] var0, int var1, int var2) {
        int var3 = Math.max(1, var1 / 2);
        int var4 = Math.max(1, var2 / 2);
        int[] var5 = new int[var3 * var4];
        for (int var6 = 0; var6 < var4; ++var6) {
            for (int var7 = 0; var7 < var3; ++var7) {
                int var23;
                int var22;
                int var21;
                float var8 = 0.0f;
                float var9 = 0.0f;
                float var10 = 0.0f;
                float var11 = 0.0f;
                float var12 = 0.0f;
                float var13 = 0.0f;
                float var14 = 0.0f;
                for (int var15 = 0; var15 < 4; ++var15) {
                    int var16 = Math.min(var1 - 1, var7 * 2 + (var15 & 1));
                    int var17 = Math.min(var2 - 1, var6 * 2 + (var15 >> 1));
                    int var18 = var0[var17 * var1 + var16];
                    float var19 = (float)(var18 >>> 24) / 255.0f;
                    var8 += var19;
                    var9 += (float)(var18 >> 16 & 0xFF) * var19;
                    var10 += (float)(var18 >> 8 & 0xFF) * var19;
                    var11 += (float)(var18 & 0xFF) * var19;
                    var12 += (float)(var18 >> 16 & 0xFF);
                    var13 += (float)(var18 >> 8 & 0xFF);
                    var14 += (float)(var18 & 0xFF);
                }
                int var20 = Math.round(var8 / 4.0f * 255.0f);
                if (var8 > 1.0E-4f) {
                    var21 = Math.round(var9 / var8);
                    var22 = Math.round(var10 / var8);
                    var23 = Math.round(var11 / var8);
                } else {
                    var21 = Math.round(var12 / 4.0f);
                    var22 = Math.round(var13 / 4.0f);
                    var23 = Math.round(var14 / 4.0f);
                }
                var5[var6 * var3 + var7] = ArgbColors.clamp(var20) << 24 | ArgbColors.clamp(var21) << 16 | ArgbColors.clamp(var22) << 8 | ArgbColors.clamp(var23);
            }
        }
        return var5;
    }

    private static int pow2(int var0) {
        int var1;
        for (var1 = 16; var1 < var0; var1 <<= 1) {
        }
        return var1;
    }
}

