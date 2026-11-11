/*
 * Decompiled with CFR 0.152.
 */
package hook.itemfx.mesh;

import hook.itemfx.mesh.Mat;
import hook.itemfx.mesh.MeshData;
import hook.itemfx.mesh.Sd;
import hook.itemfx.mesh.SurfaceNets;
import hook.itemfx.mesh.TotemShape;

public final class SpookyModel {
    private static final TotemShape SHAPE = new TotemShape(0.042f, 0.0f, 0.0f, 1.0f);
    private static final float FACE_Z = 0.112f;
    private static final float CARVE_DEPTH = 0.046f;
    private static final float[] EYE_L = new float[]{-0.1f, 0.32f, -0.021f, 0.3f, -0.025f, 0.276f, -0.092f, 0.284f};
    private static final float[] EYE_R = new float[]{0.1f, 0.32f, 0.092f, 0.284f, 0.025f, 0.276f, 0.021f, 0.3f};
    private static final float[] MOUTH = new float[]{-0.09f, 0.214f, -0.06f, 0.192f, -0.035f, 0.187f, -0.025f, 0.204f, -0.015f, 0.187f, 0.015f, 0.187f, 0.025f, 0.204f, 0.035f, 0.187f, 0.06f, 0.192f, 0.09f, 0.214f, 0.066f, 0.229f, 0.056f, 0.229f, 0.048f, 0.207f, 0.04f, 0.229f, -0.04f, 0.229f, -0.048f, 0.207f, -0.056f, 0.229f, -0.066f, 0.229f};
    private static final float GEM_Z = 0.112f;

    private SpookyModel() {
    }

    public static MeshData build() {
        return SurfaceNets.build(SpookyModel::field, SpookyModel::paint, -0.47f, -0.51f, -0.18f, 0.47f, 0.46f, 0.18f, 0.008333334f, 0.07f);
    }

    static float carve(float var0, float var1, float var2) {
        if (!(var2 < 0.046f || var1 < 0.16f || var1 > 0.36f)) {
            float var3 = Math.min(Math.min(Sd.polygon(var0, var1, EYE_L), Sd.polygon(var0, var1, EYE_R)), Sd.polygon(var0, var1, MOUTH));
            return Math.max(var3, 0.066f - var2);
        }
        return 1.0f;
    }

    static float features(float var0, float var1, float var2) {
        if (!(var2 < 0.07f || var1 < 0.22f || var1 > 0.37f)) {
            float var3 = Math.abs(var0);
            float var4 = Sd.roundCone(var3, var1, var2, 0.104f, 0.338f, 0.108f, 0.02f, 0.316f, 0.112f, 0.0115f, 0.0125f);
            float var5 = Sd.box(var0, var1 - 0.258f, var2 - 0.112f, 0.013f, 0.026f, 0.012f, 0.008f);
            return Math.min(var4, var5);
        }
        return 1.0f;
    }

    static float plainHead(float var0, float var1, float var2) {
        return Sd.smin(SHAPE.head(var0, var1, var2), SpookyModel.features(var0, var1, var2), 0.008f);
    }

    static float head(float var0, float var1, float var2) {
        return Sd.ssub(SpookyModel.plainHead(var0, var1, var2), SpookyModel.carve(var0, var1, var2), 0.004f);
    }

    static float gem(float var0, float var1, float var2) {
        return !(Math.abs(var0) > 0.08f) && !(Math.abs(var1 - -0.125f) > 0.08f) ? Sd.octahedron(var0, (var1 - -0.125f) / 1.25f, var2 - 0.112f, 0.036f) : 1.0f;
    }

    static float bodyNoHead(float var0, float var1, float var2) {
        float var3 = SHAPE.torso(var0, var1, var2);
        var3 = Sd.smin(var3, SHAPE.arms(var0, var1, var2), 0.03f);
        var3 = Sd.smin(var3, SHAPE.sleeves(var0, var1, var2), 0.035f);
        return Sd.smin(var3, SHAPE.skirt(var0, var1, var2), 0.03f);
    }

    public static float field(float var0, float var1, float var2) {
        float var3 = Sd.smin(SpookyModel.head(var0, var1, var2), SpookyModel.bodyNoHead(var0, var1, var2), 0.04f);
        var3 = Sd.smin(var3, SHAPE.belt(var0, var1, var2), 0.008f);
        return Math.min(var3, SpookyModel.gem(var0, var1, var2));
    }

    public static void paint(float var0, float var1, float var2, float var3, float var4, float var5, SurfaceNets.Surface var6) {
        float var7 = Math.abs(SpookyModel.gem(var0, var1, var2));
        float var8 = Math.abs(SHAPE.belt(var0, var1, var2));
        float var9 = Math.abs(SpookyModel.head(var0, var1, var2));
        float var10 = Math.abs(SpookyModel.bodyNoHead(var0, var1, var2));
        var6.u = (float)(Math.atan2(var2, var0) / (Math.PI * 2) + 0.5);
        var6.v = var1;
        if (var7 <= var8 && var7 < 0.006f) {
            var6.mat = 4;
            var6.rgb = Mat.rgb(0.62f, 0.16f, 1.0f);
            var6.p1 = 1.0f;
        } else if (var8 < var10 && var8 < var9 && var8 < 0.006f) {
            var6.mat = 1;
            var6.rgb = Mat.rgb(0.36f, 0.13f, 0.48f);
            var6.p1 = 0.3f;
        } else {
            boolean var11 = var9 <= var10 && var1 > 0.12f;
            float var12 = Math.abs(var0);
            var6.mat = 5;
            float var13 = var11 ? Sd.smoothstep(0.06f, 0.1f, var2) : 0.0f;
            var6.rgb = Mat.rgb(0.062f + 0.045f * var13, 0.052f + 0.034f * var13, 0.06f + 0.036f * var13);
            var6.p2 = var11 ? Sd.clamp(-SpookyModel.plainHead(var0, var1, var2) / 0.046f, 0.0f, 1.0f) : 0.0f;
            float var14 = (float)Math.atan2(var2, var0);
            float var15 = var1 < -0.36f ? 1.0f - Sd.smoothstep(0.0f, 0.032f, var1 - SHAPE.hemY(var14)) : 0.0f;
            float var16 = 0.0f;
            if (!var11 && var12 > 0.11f && var12 < 0.35f && var1 < 0.05f && var1 > -0.11f && Math.abs(SHAPE.sleeves(var0, var1, var2)) < 0.012f) {
                var16 = (1.0f - Sd.smoothstep(0.0f, 0.022f, Math.abs(SHAPE.sleeveOutline(var12, var1)))) * Sd.smoothstep(0.05f, 0.025f, var1);
            }
            var6.p1 = Math.max(var15, var16);
            var6.region = var11 ? 4 : (var1 < -0.3f ? 3 : (var12 > 0.11f && var1 < 0.075f ? (var0 < 0.0f ? 1 : 2) : 0));
        }
    }
}

