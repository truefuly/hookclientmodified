/*
 * Decompiled with CFR 0.152.
 */
package hook.itemfx.mesh;

import hook.itemfx.mesh.Mat;
import hook.itemfx.mesh.MeshData;
import hook.itemfx.mesh.Sd;
import hook.itemfx.mesh.SurfaceNets;
import hook.itemfx.mesh.TotemShape;
import java.util.Arrays;
import java.util.HashSet;

public final class ChromeModel {
    private static final TotemShape SHAPE = new TotemShape(0.08f, 0.045f, 0.0f, 0.0f);
    private static final float FACE_Z = 0.112f;
    private static final float EYE_X = 0.052f;
    private static final float EYE_Y = 0.305f;

    private ChromeModel() {
    }

    public static MeshData build() {
        return SurfaceNets.build(ChromeModel::field, ChromeModel::paint, -0.47f, -0.49f, -0.18f, 0.47f, 0.47f, 0.18f, 0.008333334f, 0.07f);
    }

    static float face(float var0, float var1, float var2) {
        if (!(var2 < 0.05f || var1 < 0.17f || var1 > 0.38f)) {
            float var3 = Sd.box(var0, var1 - 0.346f, var2 - 0.112f + 0.004f, 0.1f, 0.012f, 0.014f, 0.011f);
            float var4 = Sd.box(var0, var1 - 0.262f, var2 - 0.112f, 0.015f, 0.032f, 0.014f, 0.011f);
            return Math.min(var3, var4);
        }
        return 1.0f;
    }

    static float hollows(float var0, float var1, float var2) {
        if (!(var2 < 0.05f || var1 < 0.17f || var1 > 0.38f)) {
            float var3 = Sd.ellipsoid(Math.abs(var0) - 0.052f, var1 - 0.305f, var2 - 0.112f - 0.004f, 0.036f, 0.026f, 0.03f);
            float var4 = Sd.box(var0, var1 - 0.205f, var2 - 0.112f - 0.004f, 0.06f, 0.009f, 0.022f, 0.008f);
            return Math.min(var3, var4);
        }
        return 1.0f;
    }

    static float eyes(float var0, float var1, float var2) {
        return Sd.ellipsoid(Math.abs(var0) - 0.052f, var1 - 0.305f, var2 - 0.094f, 0.026f, 0.018f, 0.014f);
    }

    static float body(float var0, float var1, float var2) {
        float var3 = SHAPE.body(var0, var1, var2);
        var3 = Sd.smin(var3, SHAPE.belt(var0, var1, var2), 0.02f);
        var3 = Sd.smin(var3, ChromeModel.face(var0, var1, var2), 0.014f);
        if ((var3 = Sd.ssub(var3, ChromeModel.hollows(var0, var1, var2), 0.012f)) < 0.05f) {
            float var4 = ChromeModel.faceMask(var0, var1, var2);
            var3 -= 0.02f * (1.0f - 0.85f * var4) * (Sd.fbm(var0 * 5.5f + 3.1f, var1 * 4.0f, var2 * 5.5f, 2) - 0.5f);
        }
        return var3;
    }

    public static float field(float var0, float var1, float var2) {
        return Math.min(ChromeModel.body(var0, var1, var2), ChromeModel.eyes(var0, var1, var2));
    }

    public static float faceMask(float var0, float var1, float var2) {
        return Sd.smoothstep(0.05f, 0.09f, var2) * (1.0f - Sd.smoothstep(0.112f, 0.14f, Math.abs(var0))) * Sd.smoothstep(0.165f, 0.19f, var1) * (1.0f - Sd.smoothstep(0.392f, 0.42f, var1));
    }

    public static void paint(float var0, float var1, float var2, float var3, float var4, float var5, SurfaceNets.Surface var6) {
        float var7 = Math.abs(ChromeModel.eyes(var0, var1, var2));
        float var8 = Math.abs(ChromeModel.body(var0, var1, var2));
        var6.u = (float)(Math.atan2(var2, var0) / (Math.PI * 2) + 0.5);
        var6.v = var1;
        if (var7 < var8 && var7 < 0.008f) {
            var6.mat = 4;
            var6.rgb = Mat.rgb(0.1f, 0.95f, 0.42f);
            var6.p1 = 0.8f;
        } else {
            var6.mat = 2;
            var6.rgb = Mat.rgb(0.92f, 0.93f, 0.95f);
            var6.p1 = 1.0f - 0.8f * ChromeModel.faceMask(var0, var1, var2);
            var6.region = 5;
        }
    }

    public static float[] dripAnchors(MeshData var0) {
        float var1 = 0.035f;
        HashSet<Long> var2 = new HashSet<Long>();
        float[] var3 = new float[288];
        int var4 = 0;
        for (int var5 = 0; var5 < var0.vertexCount && var4 < 96; ++var5) {
            long var10;
            float var6 = var0.nrm[var5 * 3 + 1];
            if (var6 > -0.8f || var0.p1[var5] < 900 || var0.mat[var5] != 2) continue;
            float var7 = var0.pos[var5 * 3];
            float var8 = var0.pos[var5 * 3 + 1];
            float var9 = var0.pos[var5 * 3 + 2];
            if (var9 < -0.07f || !var2.add(var10 = (long)Math.floor(var7 / var1) * 1000003L + (long)Math.floor(var8 / var1) * 1009L + (long)Math.floor(var9 / var1))) continue;
            var3[var4 * 3] = var7;
            var3[var4 * 3 + 1] = var8;
            var3[var4 * 3 + 2] = var9;
            ++var4;
        }
        return Arrays.copyOf(var3, var4 * 3);
    }
}

