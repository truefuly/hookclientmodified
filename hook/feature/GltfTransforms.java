/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hook.feature.GlbDocument;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class GltfTransforms {
    private GltfTransforms() {
    }

    static float[] identity() {
        float[] var0 = new float[16];
        var0[15] = 1.0f;
        var0[10] = 1.0f;
        var0[5] = 1.0f;
        var0[0] = 1.0f;
        return var0;
    }

    static float[] mul(float[] var0, float[] var1) {
        float[] var2 = new float[16];
        for (int var3 = 0; var3 < 4; ++var3) {
            for (int var4 = 0; var4 < 4; ++var4) {
                float var5 = 0.0f;
                for (int var6 = 0; var6 < 4; ++var6) {
                    var5 += var0[var6 * 4 + var4] * var1[var3 * 4 + var6];
                }
                var2[var3 * 4 + var4] = var5;
            }
        }
        return var2;
    }

    static float[] local(JsonObject var0) {
        JsonArray var1 = GlbDocument.array(var0, "matrix");
        if (var1.size() != 16) {
            float[] var9 = GltfTransforms.vec(var0, "translation", 0.0f, 3);
            float[] var10 = GltfTransforms.vec(var0, "scale", 1.0f, 3);
            float[] var10000 = var0.has("rotation") ? GltfTransforms.vec(var0, "rotation", 0.0f, 4) : new float[]{0.0f, 0.0f, 0.0f, 1.0f};
            float[] var4 = var10000;
            float var5 = var4[0];
            float var6 = var4[1];
            float var7 = var4[2];
            float var8 = var4[3];
            var10000 = new float[]{(1.0f - 2.0f * (var6 * var6 + var7 * var7)) * var10[0], 2.0f * (var5 * var6 + var7 * var8) * var10[0], 2.0f * (var5 * var7 - var6 * var8) * var10[0], 0.0f, 2.0f * (var5 * var6 - var7 * var8) * var10[1], (1.0f - 2.0f * (var5 * var5 + var7 * var7)) * var10[1], 2.0f * (var6 * var7 + var5 * var8) * var10[1], 0.0f, 2.0f * (var5 * var7 + var6 * var8) * var10[2], 2.0f * (var6 * var7 - var5 * var8) * var10[2], (1.0f - 2.0f * (var5 * var5 + var6 * var6)) * var10[2], 0.0f, var9[0], var9[1], var9[2], 1.0f};
            return var10000;
        }
        float[] var2 = new float[16];
        for (int var3 = 0; var3 < 16; ++var3) {
            var2[var3] = var1.get(var3).getAsFloat();
        }
        return var2;
    }

    private static float[] vec(JsonObject var0, String var1, float var2, int var3) {
        float[] var4 = new float[var3];
        JsonArray var5 = GlbDocument.array(var0, var1);
        for (int var6 = 0; var6 < var3; ++var6) {
            var4[var6] = var6 < var5.size() ? var5.get(var6).getAsFloat() : var2;
        }
        return var4;
    }

    static float[] rollAbout(float var0, float[] var1) {
        double var2 = Math.toRadians(var0);
        float var4 = (float)Math.cos(var2);
        float var5 = (float)Math.sin(var2);
        float[] var6 = GltfTransforms.identity();
        var6[0] = var4;
        var6[1] = var5;
        var6[4] = -var5;
        var6[5] = var4;
        var6[12] = var1[0] - var4 * var1[0] + var5 * var1[1];
        var6[13] = var1[1] - var5 * var1[0] - var4 * var1[1];
        var6[14] = 0.0f;
        return var6;
    }

    static float[] point(float[] var0, float var1, float var2, float var3) {
        float[] var10000 = new float[]{var0[0] * var1 + var0[4] * var2 + var0[8] * var3 + var0[12], var0[1] * var1 + var0[5] * var2 + var0[9] * var3 + var0[13], var0[2] * var1 + var0[6] * var2 + var0[10] * var3 + var0[14]};
        return var10000;
    }

    static float[] dir(float[] var0, float var1, float var2, float var3) {
        float[] var10000 = new float[]{var0[0] * var1 + var0[4] * var2 + var0[8] * var3, var0[1] * var1 + var0[5] * var2 + var0[9] * var3, var0[2] * var1 + var0[6] * var2 + var0[10] * var3};
        return var10000;
    }

    static void normalise(float[] var0) {
        float var1 = (float)Math.sqrt(var0[0] * var0[0] + var0[1] * var0[1] + var0[2] * var0[2]);
        if (var1 < 1.0E-12f) {
            var0[1] = 1.0f;
        } else {
            var0[0] = var0[0] / var1;
            var0[1] = var0[1] / var1;
            var0[2] = var0[2] / var1;
        }
    }

    static float[] faceNormal(float[] var0, int var1, int var2, int var3) {
        float var4 = var0[var2 * 3] - var0[var1 * 3];
        float var5 = var0[var2 * 3 + 1] - var0[var1 * 3 + 1];
        float var6 = var0[var2 * 3 + 2] - var0[var1 * 3 + 2];
        float var7 = var0[var3 * 3] - var0[var1 * 3];
        float var8 = var0[var3 * 3 + 1] - var0[var1 * 3 + 1];
        float var9 = var0[var3 * 3 + 2] - var0[var1 * 3 + 2];
        float[] var10000 = new float[]{var5 * var9 - var6 * var8, var6 * var7 - var4 * var9, var4 * var8 - var5 * var7};
        return var10000;
    }

    static float area(float[] var0, int var1, int var2, int var3) {
        float[] var4 = GltfTransforms.faceNormal(var0, var1, var2, var3);
        return (float)Math.sqrt(var4[0] * var4[0] + var4[1] * var4[1] + var4[2] * var4[2]) * 0.5f;
    }
}

