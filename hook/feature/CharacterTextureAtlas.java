/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.GlbDocument;
import hook.feature.UiTextureAtlas;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class CharacterTextureAtlas {
    private static final int SIZE = 2048;
    private static final int PAD = 4;
    private static final int MAX_SIDE = 8192;
    final int width;
    final int height;
    private final int[] pixels;
    private final Map<Integer, float[]> regions = new HashMap<Integer, float[]>();
    private final Map<Integer, int[]> tiles = new HashMap<Integer, int[]>();

    private CharacterTextureAtlas(int var1, int var2) {
        this.width = var1;
        this.height = var2;
        this.pixels = new int[var1 * var2];
    }

    static CharacterTextureAtlas pack(GlbDocument var0, Map<Integer, float[]> var1, UiTextureAtlas var2) throws IOException {
        LinkedHashMap<Integer, UiTextureAtlas.TexturePixels> var3 = new LinkedHashMap<Integer, UiTextureAtlas.TexturePixels>();
        HashMap<Integer, float[]> var4 = new HashMap<Integer, float[]>();
        HashMap<Integer, int[]> var5 = new HashMap<Integer, int[]>();
        for (Map.Entry<Integer, float[]> var7 : var1.entrySet()) {
            float[] var8 = var7.getValue();
            int var9 = (int)Math.floor((double)(var8[0] + 0.001f));
            int var10 = (int)Math.floor((double)(var8[1] + 0.001f));
            int var11 = Math.min(8, Math.max(1, (int)Math.ceil((double)(var8[2] - 0.001f)) - var9));
            int var12 = Math.min(8, Math.max(1, (int)Math.ceil((double)(var8[3] - 0.001f)) - var10));
            UiTextureAtlas.TexturePixels var13 = CharacterTextureAtlas.tile(CharacterTextureAtlas.baseColor(var0, var7.getKey(), var2), var11, var12);
            Integer var10001 = var7.getKey();
            int[] var10002 = new int[]{var9, var10, var11, var12};
            var5.put(var10001, var10002);
            float var16_21 = (var8[0] - (float)var9) / (float)var11;
            float var17_22 = (var8[1] - (float)var10) / (float)var12;
            float var18_23 = (var8[2] - (float)var9) / (float)var11;
            float var19_24 = (var8[3] - (float)var10) / (float)var12;
            int var18 = CharacterTextureAtlas.clamp((int)Math.floor((double)(var16_21 * (float)var13.width())) - 2, 0, var13.width() - 1);
            int var19 = CharacterTextureAtlas.clamp((int)Math.floor((double)(var17_22 * (float)var13.height())) - 2, 0, var13.height() - 1);
            int var20 = CharacterTextureAtlas.clamp((int)Math.ceil((double)(var18_23 * (float)var13.width())) + 2, var18 + 1, var13.width());
            int var21 = CharacterTextureAtlas.clamp((int)Math.ceil((double)(var19_24 * (float)var13.height())) + 2, var19 + 1, var13.height());
            var3.put(var7.getKey(), CharacterTextureAtlas.crop(var13, var18, var19, var20, var21));
            var10001 = var7.getKey();
            float[] var28 = new float[]{(float)var18 / (float)var13.width(), (float)var19 / (float)var13.height(), (float)var20 / (float)var13.width(), (float)var21 / (float)var13.height()};
            var4.put(var10001, var28);
        }
        double var22 = 0.0;
        for (Object var25Object : var3.values()) {
            UiTextureAtlas.TexturePixels var25 = (UiTextureAtlas.TexturePixels)var25Object;
            var22 += (double)(var25.width() + 8) * (double)(var25.height() + 8);
        }
        double var24 = Math.min(1.0, Math.sqrt(3858759.68 / Math.max(1.0, var22)));
        while (true) {
            CharacterTextureAtlas var26;
            if ((var26 = CharacterTextureAtlas.tryPack(var3, var4, var24)) != null) {
                var26.tiles.putAll(var5);
                return var26;
            }
            var24 *= 0.93;
        }
    }

    private static UiTextureAtlas.TexturePixels baseColor(GlbDocument var0, int var1, UiTextureAtlas var2) throws IOException {
        byte[] var3 = var0.baseColorImage(var1);
        UiTextureAtlas.TexturePixels var4 = var3 == null ? new UiTextureAtlas.TexturePixels(4, 4, CharacterTextureAtlas.filled(16, -1)) : var2.decode(var3);
        float[] var5 = var0.baseColorFactor(var1);
        if (var5[0] == 1.0f && var5[1] == 1.0f && var5[2] == 1.0f && var5[3] == 1.0f) {
            return var4;
        }
        for (int var6 = 0; var6 < 3; ++var6) {
            var5[var6] = (float)Math.pow(var5[var6], 0.45454545454545453);
        }
        int[] var13 = new int[var4.argb().length];
        for (int var7 = 0; var7 < var13.length; ++var7) {
            int var8 = var4.argb()[var7];
            int var9 = Math.round((float)(var8 >>> 24) * var5[3]);
            int var10 = Math.round((float)(var8 >> 16 & 0xFF) * var5[0]);
            int var11 = Math.round((float)(var8 >> 8 & 0xFF) * var5[1]);
            int var12 = Math.round((float)(var8 & 0xFF) * var5[2]);
            var13[var7] = var9 << 24 | var10 << 16 | var11 << 8 | var12;
        }
        return new UiTextureAtlas.TexturePixels(var4.width(), var4.height(), var13);
    }

    private static int[] filled(int var0, int var1) {
        int[] var2 = new int[var0];
        Arrays.fill(var2, var1);
        return var2;
    }

    private static UiTextureAtlas.TexturePixels tile(UiTextureAtlas.TexturePixels var0, int var1, int var2) {
        if (var1 == 1 && var2 == 1) {
            return var0;
        }
        if (var0.width() * var1 > 8192 || var0.height() * var2 > 8192) {
            var0 = CharacterTextureAtlas.resize(var0, Math.max(1, Math.min(var0.width(), 8192 / var1)), Math.max(1, Math.min(var0.height(), 8192 / var2)));
        }
        int var3 = var0.width() * var1;
        int var4 = var0.height() * var2;
        int[] var5 = new int[var3 * var4];
        for (int var6 = 0; var6 < var4; ++var6) {
            for (int var7 = 0; var7 < var3; ++var7) {
                var5[var6 * var3 + var7] = var0.argb()[var6 % var0.height() * var0.width() + var7 % var0.width()];
            }
        }
        return new UiTextureAtlas.TexturePixels(var3, var4, var5);
    }

    private static UiTextureAtlas.TexturePixels crop(UiTextureAtlas.TexturePixels var0, int var1, int var2, int var3, int var4) {
        int var5 = var3 - var1;
        int var6 = var4 - var2;
        int[] var7 = new int[var5 * var6];
        for (int var8 = 0; var8 < var6; ++var8) {
            System.arraycopy(var0.argb(), (var2 + var8) * var0.width() + var1, var7, var8 * var5, var5);
        }
        return new UiTextureAtlas.TexturePixels(var5, var6, var7);
    }

    private static CharacterTextureAtlas tryPack(Map<Integer, UiTextureAtlas.TexturePixels> var0, Map<Integer, float[]> var1, double var2) {
        ArrayList<Integer> var4 = new ArrayList<Integer>(var0.keySet());
        var4.sort((var1x, var2x) -> Integer.compare(((UiTextureAtlas.TexturePixels)var0.get(var2x)).height(), ((UiTextureAtlas.TexturePixels)var0.get(var1x)).height()));
        int var5 = 0;
        int var6 = 0;
        int var7 = 0;
        int var8 = 0;
        HashMap<Integer, int[]> var9 = new HashMap<Integer, int[]>();
        for (Integer var20Object : var4) {
            int var20 = var20Object;
            UiTextureAtlas.TexturePixels var21 = var0.get(var20);
            int var22 = Math.max(1, (int)Math.round((double)var21.width() * var2));
            int var23 = Math.max(1, (int)Math.round((double)var21.height() * var2));
            if (var5 + var22 + 8 > 2048) {
                var5 = 0;
                var6 += var7;
                var7 = 0;
            }
            if (var22 + 8 > 2048 || var6 + var23 + 8 > 2048) {
                return null;
            }
            Integer var25 = var20;
            int[] var26 = new int[]{var5 + 4, var6 + 4, var22, var23};
            var9.put(var25, var26);
            var7 = Math.max(var7, var23 + 8);
            var8 = Math.max(var8, var5 += var22 + 8);
        }
        CharacterTextureAtlas var19 = new CharacterTextureAtlas(CharacterTextureAtlas.pow2(var8), CharacterTextureAtlas.pow2(var6 + var7));
        for (Integer var12Object : var4) {
            int var12 = var12Object;
            int[] var13 = (int[])var9.get(var12);
            UiTextureAtlas.TexturePixels var14 = CharacterTextureAtlas.resize(var0.get(var12), var13[2], var13[3]);
            for (int var15 = -4; var15 < var13[3] + 4; ++var15) {
                for (int var16 = -4; var16 < var13[2] + 4; ++var16) {
                    int var17 = CharacterTextureAtlas.clamp(var16, 0, var13[2] - 1);
                    int var18 = CharacterTextureAtlas.clamp(var15, 0, var13[3] - 1);
                    var19.pixels[(var13[1] + var15) * var19.width + var13[0] + var16] = var14.argb()[var18 * var13[2] + var17];
                }
            }
            float[] var24 = var1.get(var12);
            Map<Integer, float[]> var10000 = var19.regions;
            Integer var10001 = var12;
            float[] var10002 = new float[]{var24[0], var24[1], var24[2], var24[3], var13[0], var13[1], var13[2], var13[3]};
            var10000.put(var10001, var10002);
        }
        return var19;
    }

    float[] map(int var1, float var2, float var3) {
        float[] var4 = this.regions.get(var1);
        int[] var5 = this.tiles.get(var1);
        float var6 = (var2 - (float)var5[0]) / (float)var5[2];
        float var7 = (var3 - (float)var5[1]) / (float)var5[3];
        float var8 = (var6 - var4[0]) / (var4[2] - var4[0]);
        float var9 = (var7 - var4[1]) / (var4[3] - var4[1]);
        float[] var10000 = new float[]{(var4[4] + var8 * var4[6]) / (float)this.width, (var4[5] + var9 * var4[7]) / (float)this.height};
        return var10000;
    }

    byte[] packed() throws IOException {
        byte[] var1 = new byte[this.pixels.length * 4];
        for (int var2 = 0; var2 < this.pixels.length; ++var2) {
            int var3 = var2 % this.width == 0 ? 0 : this.pixels[var2 - 1];
            for (int var4 = 0; var4 < 4; ++var4) {
                var1[var2 * 4 + var4] = (byte)((this.pixels[var2] >>> 24 - var4 * 8) - (var3 >>> 24 - var4 * 8));
            }
        }
        ByteArrayOutputStream var8 = new ByteArrayOutputStream();
        try (DeflaterOutputStream var9 = new DeflaterOutputStream((OutputStream)var8, new Deflater(9));){
            var9.write(var1);
        }
        return var8.toByteArray();
    }

    private static UiTextureAtlas.TexturePixels resize(UiTextureAtlas.TexturePixels var0, int var1, int var2) {
        UiTextureAtlas.TexturePixels var3 = var0;
        while (var3.width() / 2 >= var1 && var3.height() / 2 >= var2) {
            var3 = CharacterTextureAtlas.half(var3);
        }
        return var3.width() == var1 && var3.height() == var2 ? var3 : CharacterTextureAtlas.bilinear(var3, var1, var2);
    }

    private static UiTextureAtlas.TexturePixels half(UiTextureAtlas.TexturePixels var0) {
        int var1 = var0.width() / 2;
        int var2 = var0.height() / 2;
        int[] var3 = new int[var1 * var2];
        for (int var4 = 0; var4 < var2; ++var4) {
            for (int var5 = 0; var5 < var1; ++var5) {
                int[] var6 = var0.argb();
                int var7 = var4 * 2 * var0.width() + var5 * 2;
                var3[var4 * var1 + var5] = CharacterTextureAtlas.average(var6[var7], var6[var7 + 1], var6[var7 + var0.width()], var6[var7 + var0.width() + 1]);
            }
        }
        return new UiTextureAtlas.TexturePixels(var1, var2, var3);
    }

    private static int average(int var0, int var1, int var2, int var3) {
        int var4 = 0;
        for (int var5 = 0; var5 < 32; var5 += 8) {
            int var6 = (var0 >>> var5 & 0xFF) + (var1 >>> var5 & 0xFF) + (var2 >>> var5 & 0xFF) + (var3 >>> var5 & 0xFF);
            var4 |= (var6 + 2) / 4 << var5;
        }
        return var4;
    }

    private static UiTextureAtlas.TexturePixels bilinear(UiTextureAtlas.TexturePixels var0, int var1, int var2) {
        int[] var3 = new int[var1 * var2];
        int var4 = var0.width();
        int var5 = var0.height();
        for (int var6 = 0; var6 < var2; ++var6) {
            float var7 = Math.max(0.0f, ((float)var6 + 0.5f) * (float)var5 / (float)var2 - 0.5f);
            int var8 = Math.min(var5 - 1, (int)var7);
            int var9 = Math.min(var5 - 1, var8 + 1);
            float var10 = var7 - (float)var8;
            for (int var11 = 0; var11 < var1; ++var11) {
                float var12 = Math.max(0.0f, ((float)var11 + 0.5f) * (float)var4 / (float)var1 - 0.5f);
                int var13 = Math.min(var4 - 1, (int)var12);
                int var14 = Math.min(var4 - 1, var13 + 1);
                float var15 = var12 - (float)var13;
                int[] var16 = var0.argb();
                int var17 = 0;
                for (int var18 = 0; var18 < 32; var18 += 8) {
                    float var19 = (float)(var16[var8 * var4 + var13] >>> var18 & 0xFF) * (1.0f - var15) + (float)(var16[var8 * var4 + var14] >>> var18 & 0xFF) * var15;
                    float var20 = (float)(var16[var9 * var4 + var13] >>> var18 & 0xFF) * (1.0f - var15) + (float)(var16[var9 * var4 + var14] >>> var18 & 0xFF) * var15;
                    var17 |= Math.round(var19 * (1.0f - var10) + var20 * var10) << var18;
                }
                var3[var6 * var1 + var11] = var17;
            }
        }
        return new UiTextureAtlas.TexturePixels(var1, var2, var3);
    }

    private static int clamp(int var0, int var1, int var2) {
        return Math.max(var1, Math.min(var2, var0));
    }

    private static int pow2(int var0) {
        int var1;
        for (var1 = 16; var1 < var0; var1 <<= 1) {
        }
        return var1;
    }
}

