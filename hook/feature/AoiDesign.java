/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.ArgbColors;
import hook.feature.BoneMotionProfile;
import hook.feature.CharacterBone;
import hook.feature.CharacterDefinition;
import hook.feature.CharacterPartBuilder;
import hook.feature.CharacterRig;
import hook.feature.ColorField2D;
import hook.feature.PixelCanvas;
import hook.feature.Sdf2D;
import hook.feature.Sdf3D;
import hook.feature.SdfShapes;
import hook.feature.SurfaceDecal;
import hook.feature.SurfacePaint;
import hook.feature.TaperedTubeSdf3D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class AoiDesign
extends CharacterDefinition {
    private static final float TAU = (float)Math.PI * 2;
    private static final float NECK = 24.4f;
    private static final float HEAD_K = 1.16f;
    private static final float HEAD_DY = -0.25f;
    private static final float FACE_X = 2.25f;
    private static final float FACE_TOP = 28.6f;
    private static final float FACE_LOW = 26.08f;
    private static final float FACE_PX = 80.0f;
    private static final float YOKE = 21.75f;
    private static final float BEND = 13.0f;
    private static final int SKIN = -9274;
    private static final int SKIN_SHADE = -1331558;
    private static final int SKIN_LINE = -6657438;
    private static final int HAIR = -14140794;
    private static final int HAIR_DEEP = -15063975;
    private static final int HAIR_TOP = -12756032;
    private static final int HAIR_BLUE = -12948262;
    private static final int HAIR_TIP = -13246209;
    private static final int HAIR_INNER = -13646610;
    private static final int HAIR_SHINE = -7363350;
    private static final int HAIR_LINE = -15327158;
    private static final int CYAN = -13703425;
    private static final int NAVY = -14932394;
    private static final int JACKET = -12959662;
    private static final int JACKET_DARK = -14210250;
    private static final int JACKET_WHITE = -1446155;
    private static final int JACKET_SHADE = -3945254;
    private static final int LINE = -15065558;
    private static final int TOP = -525828;
    private static final int TOP_SHADE = -2498584;
    private static final int SKIRT = -12892555;
    private static final int SKIRT_DARK = -14472632;
    private static final int BLACK = -15263457;
    private static final int SILVER = -3550757;
    private static final int SOCK = -14012606;
    private static final int SOLE = -920586;
    private static final float SKIRT_ZC = -0.05f;
    private static final float SKIRT_TOP = 17.3f;
    private static final float SKIRT_HEM = 11.25f;
    private static final int PLEATS = 18;
    private static final float[][] MAIN_LOCKS;
    private static final float[][] ACCENT_LOCKS;

    public AoiDesign() {
        super("aoi", "Aoi");
    }

    @Override
    protected void define(CharacterRig var1) {
        var1.scale(1.0f).outline(-15065558, 0.13f).occlusion(0.35f);
        PixelCanvas var2 = AoiDesign.stripe();
        Sdf3D var3 = AoiDesign.headShape();
        List<float[]> var4 = AoiDesign.fringeLocks(var3, MAIN_LOCKS, 0.2f, 0.17f);
        List<float[]> var5 = AoiDesign.fringeLocks(var3, ACCENT_LOCKS, 0.2f, 0.17f);
        var1.part(CharacterBone.HEAD).pivot(0.0f, 24.4f, 0.0f).shape(AoiDesign.grow(var3.color(-9274))).detail(1.1f).resolution(0.85f).outline(-6657438, 0.09f).flatLight(1.0f).occlusion(0.2f).decal(AoiDesign.face(var4));
        AoiDesign.hair(var1, var4, var5);
        AoiDesign.ponytail(var1);
        Sdf3D var6 = AoiDesign.body(var1, var2);
        AoiDesign.skirt(var1, var2);
        AoiDesign.arms(var1, var2);
        AoiDesign.legs(var1);
        AoiDesign.gear(var1, var6);
    }

    private static Sdf3D grow(Sdf3D var0) {
        return var0.scale(1.16f, 0.0f, 24.4f, 0.0f).move(0.0f, -0.25f, 0.0f);
    }

    private static float hy(float var0) {
        return 24.4f + (var0 - 24.4f) * 1.16f + -0.25f;
    }

    private static Sdf3D headShape() {
        Sdf3D var0 = SdfShapes.ellipsoid(0.0f, 28.52f, -0.15f, 2.28f, 2.24f, 2.32f);
        Sdf3D var1 = SdfShapes.ellipsoid(0.0f, 27.4f, 0.7f, 1.95f, 1.1f, 1.45f);
        Sdf3D var2 = SdfShapes.cone(0.0f, 27.25f, 0.42f, 1.68f, 0.0f, 26.15f, 1.18f, 0.55f);
        float[] var10000 = new float[]{-2.6f, 31.5f, -2.6f, 29.0f, -2.24f, 27.6f, -2.11f, 27.2f, -1.9f, 26.8f, -1.62f, 26.44f, -1.28f, 26.12f, -0.92f, 25.91f, -0.54f, 25.79f, -0.25f, 25.75f, 0.0f, 25.74f, 0.25f, 25.75f, 0.54f, 25.79f, 0.92f, 25.91f, 1.28f, 26.12f, 1.62f, 26.44f, 1.9f, 26.8f, 2.11f, 27.2f, 2.24f, 27.6f, 2.6f, 29.0f, 2.6f, 31.5f};
        Sdf3D var3 = SdfShapes.extrude(Sdf2D.polygon(var10000), 4.0f, 0.0f);
        float var6 = 0.9f;
        Sdf3D[] var10001 = new Sdf3D[]{var0, var1, var2};
        Sdf3D var4 = SdfShapes.blend(var6, var10001);
        Sdf3D var5 = var4.intersect(0.3f, var3);
        return SdfShapes.field((var2x, var3x, var4x) -> {
            float var5x = var3x > 28.8f ? var4.distance(var2x, var3x, var4x) : var5.distance(var2x, var3x, var4x);
            return Math.max(var5x, Math.max(-1.0f - var4x, var3x - 30.1f));
        }, var4.minX(), var4.minY(), -1.1f, var4.maxX(), 30.2f, var4.maxZ());
    }

    private static List<float[]> fringeLocks(Sdf3D var0, float[][] var1, float var2, float var3) {
        ArrayList<float[]> var4 = new ArrayList<float[]>();
        float[][] var5 = var1;
        int var6 = var1.length;
        for (int var7 = 0; var7 < var6; ++var7) {
            float[] var8 = var5[var7];
            int var9 = (var8.length - 1) / 2;
            float[] var10 = new float[var9 * 4];
            for (int var11 = 0; var11 < var9; ++var11) {
                float var12 = var8[1 + var11 * 2];
                float var13 = var8[2 + var11 * 2];
                float var14 = 1.0f - var12 * var12 / 6.76f - (var13 - 28.62f) * (var13 - 28.62f) / 7.08f;
                float var15 = Math.max(AoiDesign.frontZ(var0, var12, var13, 5.0f), -0.5f + 2.64f * (float)Math.sqrt(Math.max(0.0f, var14)));
                float var17 = var13 - 28.3f;
                float var18 = var15 + 0.4f;
                float var19 = (float)Math.sqrt(var12 * var12 + var17 * var17 + var18 * var18);
                float var20 = (float)var11 / ((float)var9 - 1.0f);
                float var21 = (var11 == 0 ? 0.1f : (var11 == var9 - 1 ? var3 : var2)) + 0.22f * AoiDesign.smooth(1.3f, 2.2f, Math.abs(var12));
                var10[var11 * 4] = var12 + var12 / var19 * var21;
                var10[1 + var11 * 4] = var13 + var17 / var19 * var21;
                var10[2 + var11 * 4] = var15 + var18 / var19 * var21;
                var10[3 + var11 * 4] = var8[0] * (float)Math.pow(1.0f - var20, 1.2f) + 0.05f * var20;
            }
            var4.add(var10);
        }
        return var4;
    }

    private static void hair(CharacterRig var0, List<float[]> var1, List<float[]> var2) {
        ArrayList<Sdf3D> var3 = new ArrayList<Sdf3D>();
        var3.add(SdfShapes.subtract(0.35f, SdfShapes.ellipsoid(0.0f, 28.62f, -0.2f, 2.6f, 2.66f, 2.64f), SdfShapes.ellipsoid(0.0f, 26.75f, 2.75f, 2.45f, 2.85f, 2.72f)).intersect(SdfShapes.above(26.0f)));
        float[] var10000 = new float[]{-155.0f, -118.0f, -80.0f, 80.0f, 118.0f, 155.0f};
        for (float var7 : var10000) {
            var3.add(AoiDesign.flowLock(var7));
        }
        var0.extra("hair", CharacterBone.HEAD).shape(AoiDesign.grow(SdfShapes.union(var3.toArray(new Sdf3D[0])).paint(AoiDesign.capPaint()))).detail(0.8f).resolution(0.95f).outline(-15327158, 0.1f).flatLight(0.5f).occlusion(0.4f);
        CharacterPartBuilder var10 = var0.extra("fringe", CharacterBone.HEAD).shape(AoiDesign.grow(AoiDesign.fringe(var1, var2))).detail(0.4f).resolution(0.55f).outline(-15327158, 0.09f).flatLight(0.5f).occlusion(0.2f);
        AoiDesign.shine(var10, var1);
        BoneMotionProfile var11 = BoneMotionProfile.hair().idle(1.5f, 0.0f, 1.5f, 0.35f).walk(3.0f, 0.0f, 3.0f, 2.0f).drag(8.0f).hang(0.7f).turn(10.0f, 0.0f).spring(60.0f, 7.0f).bend(1.3f);
        SurfacePaint var12 = SurfacePaint.field((var0x, var1x, var2x) -> ArgbColors.mix(ArgbColors.mix(-14140794, -12948262, AoiDesign.smooth(28.2f, 25.8f, var1x)), -13246209, AoiDesign.smooth(26.2f, 24.4f, var1x)));
        SurfacePaint var13 = SurfacePaint.field((var0x, var1x, var2x) -> ArgbColors.mix(-13646610, -13246209, AoiDesign.smooth(27.0f, 25.0f, var1x)));
        Sdf3D[] var14 = new Sdf3D[2];
        int var10002 = 0;
        float[] var10003 = new float[]{-1.55f, 29.95f, 1.0f, 0.4f, -2.32f, 28.5f, 1.4f, 0.55f, -2.55f, 27.1f, 1.3f, 0.52f, -2.48f, 25.9f, 1.15f, 0.42f, -2.3f, 24.9f, 1.2f, 0.28f, -2.1f, 24.3f, 1.34f, 0.04f};
        var14[var10002] = SdfShapes.tube(var10003).flattenFrom(2.0f, 0.0f, 27.5f, -0.5f).paint(var12);
        var10002 = 1;
        var10003 = new float[]{-1.7f, 29.7f, 0.7f, 0.36f, -2.28f, 28.2f, 1.05f, 0.44f, -2.36f, 26.8f, 0.95f, 0.42f, -2.22f, 25.6f, 0.9f, 0.3f, -2.05f, 24.55f, 1.0f, 0.04f};
        var14[var10002] = SdfShapes.tube(var10003).flattenFrom(2.0f, 0.0f, 27.5f, -0.5f).paint(var13);
        Sdf3D var8 = SdfShapes.union(var14);
        CharacterPartBuilder var9 = var0.extra("lock_r", CharacterBone.HEAD).pivot(-2.32f, AoiDesign.hy(29.2f), 1.16f).shape(AoiDesign.grow(var8)).motion(var11).detail(0.7f).resolution(0.7f).outline(-15327158, 0.1f).flatLight(0.5f).occlusion(0.35f);
        var0.extra("lock_l", CharacterBone.HEAD).mirrorOf(var9);
    }

    private static Sdf3D flowLock(float var0) {
        float var1 = 0.0f;
        float var2 = 28.62f;
        float var3 = -0.2f;
        float var4 = 2.6f;
        float var5 = 2.66f;
        float var6 = 2.64f;
        float var7 = (float)Math.toRadians(var0);
        float var8 = Math.abs(var0);
        float var9 = 29.1f - 1.7f * AoiDesign.smooth(20.0f, 90.0f, var8) - 1.2f * AoiDesign.smooth(95.0f, 160.0f, var8);
        float var10 = (float)Math.asin(Math.max(-0.95f, Math.min(0.95f, (var9 - var2) / var5)));
        float var11 = (float)(Math.sin(var7) * Math.cos(var10));
        float var12 = (float)Math.sin(var10);
        float var13 = (float)(Math.cos(var7) * Math.cos(var10));
        float var14 = 0.0f;
        float var15 = 30.25f - var2;
        float var16 = -1.95f - var3;
        float var17 = (float)Math.sqrt(var14 * var14 + var15 * var15 + var16 * var16);
        var14 /= var17;
        var15 /= var17;
        var16 /= var17;
        float[] var10000 = new float[]{0.0f, 0.35f, 0.7f, 1.0f};
        float[] var18 = var10000;
        var10000 = new float[]{0.5f, 0.56f, 0.5f, 0.32f};
        float[] var19 = var10000;
        float[] var20 = new float[16];
        for (int var21 = 0; var21 < 4; ++var21) {
            float var22 = var11 + (var14 - var11) * var18[var21];
            float var23 = var12 + (var15 - var12) * var18[var21];
            float var24 = var13 + (var16 - var13) * var18[var21];
            float var25 = (float)Math.sqrt(var22 * var22 + var23 * var23 + var24 * var24);
            float var26 = 1.0f / (float)Math.sqrt((var22 /= var25) * var22 / (var4 * var4) + (var23 /= var25) * var23 / (var5 * var5) + (var24 /= var25) * var24 / (var6 * var6));
            float var27 = var21 == 3 ? -0.1f : 0.04f;
            var20[var21 * 4] = var1 + var22 * (var26 + var27);
            var20[var21 * 4 + 1] = var2 + var23 * (var26 + var27);
            var20[var21 * 4 + 2] = var3 + var24 * (var26 + var27);
            var20[var21 * 4 + 3] = var19[var21];
        }
        return SdfShapes.tube(var20).flattenFrom(2.4f, var1, var2, var3);
    }

    private static SurfacePaint capPaint() {
        return SurfacePaint.field((var0, var1, var2) -> ArgbColors.mix(ArgbColors.mix(-15063975, -14140794, AoiDesign.smooth(26.2f, 28.4f, var1)), -12756032, AoiDesign.smooth(29.4f, 31.2f, var1) * 0.8f));
    }

    private static Sdf3D fringe(List<float[]> var0, List<float[]> var1) {
        Sdf3D[] var2 = new Sdf3D[var0.size() + var1.size()];
        for (int var3 = 0; var3 < var2.length; ++var3) {
            boolean var4 = var3 >= var0.size();
            float[] var5 = var4 ? var1.get(var3 - var0.size()) : var0.get(var3);
            int var6 = var5.length / 4;
            float[] var7 = new float[var6 * 2];
            for (int var8 = 0; var8 < var6; ++var8) {
                var7[var8 * 2] = var5[var8 * 4];
                var7[var8 * 2 + 1] = var5[var8 * 4 + 1];
            }
            float var9 = var4 ? 1.12f : ((var3 & 1) == 0 ? 0.93f : 1.04f);
            var2[var3] = SdfShapes.tube(var5).flattenFrom(2.6f, 0.0f, 28.4f, -0.3f).intersect(SdfShapes.extrude(Sdf2D.path(var5[3] * 2.6f, 0.02f, var7), 4.0f, 0.0f)).paint(SurfacePaint.field((var1x, var2x, var3x) -> ArgbColors.shade(ArgbColors.mix(ArgbColors.mix(-14140794, -12756032, AoiDesign.smooth(29.6f, 30.6f, var2x) * 0.5f), -12948262, AoiDesign.smooth(28.9f, 27.4f, var2x) * 0.85f), var9)));
        }
        return SdfShapes.union(var2);
    }

    private static void shine(CharacterPartBuilder var0, List<float[]> var1) {
        for (int var2 = 0; var2 < var1.size(); ++var2) {
            float[] var3 = var1.get(var2);
            int var4 = var3.length / 4;
            for (int var5 = 1; var5 < var4; ++var5) {
                float var6 = var3[var5 * 4 - 3];
                float var7 = var3[var5 * 4 + 1];
                if (var6 < 29.15f || var7 > 29.15f) continue;
                float var8 = (var6 - 29.15f) / (var6 - var7);
                float var9 = var3[var5 * 4 - 4] + (var3[var5 * 4] - var3[var5 * 4 - 4]) * var8;
                float var10 = var3[var5 * 4 - 2] + (var3[var5 * 4 + 2] - var3[var5 * 4 - 2]) * var8;
                float var11 = var3[var5 * 4 - 1] + (var3[var5 * 4 + 3] - var3[var5 * 4 - 1]) * var8;
                float var12 = (float)Math.atan2(var9, var10 + 0.2f);
                if (Math.abs(var12) > 0.96f) continue;
                float var13 = var11 * 1.1f / (float)Math.sqrt(var9 * var9 + (var10 + 0.2f) * (var10 + 0.2f));
                float var14 = 7.0f + (float)Math.toDegrees(var13) * 0.25f;
                float var15 = 64.0f / (var13 * 1.5f);
                float[] var16 = new float[6];
                float[] var17 = new float[6];
                for (int var18 = 0; var18 < 3; ++var18) {
                    float var19 = var13 * 0.6f * ((float)var18 * 0.5f - 1.0f);
                    float var20 = var13 * 0.6f * (float)var18 * 0.5f;
                    var16[var18 * 2] = 32.0f + var19 * var15;
                    var16[var18 * 2 + 1] = 13.0f + 25.0f * ((float)Math.cos(var12) - (float)Math.cos(var12 + var19));
                    var17[var18 * 2] = 32.0f + var20 * var15;
                    var17[var18 * 2 + 1] = 13.0f + 25.0f * ((float)Math.cos(var12) - (float)Math.cos(var12 + var20));
                }
                PixelCanvas var21 = new PixelCanvas(64, 26);
                var21.fill(Sdf2D.path(var14 * 0.2f, var14, var16).union(Sdf2D.path(var14, var14 * 0.2f, var17)), ArgbColors.withAlpha(-7363350, 0.5f), 5.0f);
                float var22 = AoiDesign.hy(29.1f) + 0.081f - 0.29f * (1.0f - (float)Math.cos(var12)) + (float)((var2 & 1) * 2 - 1) * 0.03f;
                var0.decal(SurfaceDecal.wrap(var21, 0.0f, var22, -0.232f, 0.0f, 1.0f, 0.0f, (float)Math.sin(var12), 0.0f, (float)Math.cos(var12), (float)Math.toDegrees(var13 * 1.5f), 0.30159998f).radius(1.8f, 4.2f).flatLight(0.8f));
            }
        }
    }

    private static void ponytail(CharacterRig var0) {
        float[][] var28;
        float[][] var10000 = new float[11][];
        int var10002 = 0;
        float[] var10003 = new float[]{30.4f, -2.0f};
        var10000[var10002] = var10003;
        var10002 = 1;
        var10003 = new float[]{31.1f, -2.5f};
        var10000[var10002] = var10003;
        var10002 = 2;
        var10003 = new float[]{31.35f, -3.25f};
        var10000[var10002] = var10003;
        var10002 = 3;
        var10003 = new float[]{30.95f, -3.95f};
        var10000[var10002] = var10003;
        var10002 = 4;
        var10003 = new float[]{29.7f, -4.3f};
        var10000[var10002] = var10003;
        var10002 = 5;
        var10003 = new float[]{27.6f, -4.38f};
        var10000[var10002] = var10003;
        var10002 = 6;
        var10003 = new float[]{25.1f, -4.22f};
        var10000[var10002] = var10003;
        var10002 = 7;
        var10003 = new float[]{22.5f, -4.0f};
        var10000[var10002] = var10003;
        var10002 = 8;
        var10003 = new float[]{19.9f, -3.8f};
        var10000[var10002] = var10003;
        var10002 = 9;
        var10003 = new float[]{17.5f, -3.68f};
        var10000[var10002] = var10003;
        var10002 = 10;
        var10003 = new float[]{15.3f, -3.74f};
        var10000[var10002] = var10003;
        float[][] var1 = var10000;
        int var2 = var1.length;
        float[] var41 = new float[]{0.0f, 0.55f, 1.0f, 1.18f, 1.22f, 1.18f, 1.08f, 0.96f, 0.86f, 0.98f, 1.35f};
        float[] var3 = var41;
        float[] var42 = new float[]{0.45f, 0.88f, 1.05f, 1.1f, 1.1f, 1.05f, 0.98f, 0.86f, 0.7f, 0.52f, 0.34f};
        float[] var4 = var42;
        float[] var5 = new float[var2];
        float[] var6 = new float[var2];
        for (int var7 = 0; var7 < var2; ++var7) {
            int var8 = Math.max(0, var7 - 1);
            int var9 = Math.min(var2 - 1, var7 + 1);
            float var10 = var1[var9][0] - var1[var8][0];
            float var11 = var1[var9][1] - var1[var8][1];
            float var12 = (float)Math.sqrt(var10 * var10 + var11 * var11);
            var5[var7] = var11 / var12;
            var6[var7] = -var10 / var12;
        }
        var10000 = new float[8][];
        var10002 = 0;
        var10003 = new float[]{-1.02f, 0.25f, 10.1f, 0.68f, 0.0f, -1.0f};
        var10000[var10002] = var10003;
        var10002 = 1;
        var10003 = new float[]{-0.5f, -0.3f, 10.25f, 0.72f, 1.1f, -1.0f};
        var10000[var10002] = var10003;
        var10002 = 2;
        var10003 = new float[]{0.02f, 0.3f, 10.8f, 0.74f, 2.3f, 0.0f};
        var10000[var10002] = var10003;
        var10002 = 3;
        var10003 = new float[]{0.54f, -0.28f, 9.7f, 0.72f, 0.6f, 1.0f};
        var10000[var10002] = var10003;
        var10002 = 4;
        var10003 = new float[]{1.03f, 0.22f, 9.55f, 0.66f, 1.7f, 1.0f};
        var10000[var10002] = var10003;
        var10002 = 5;
        var10003 = new float[]{-0.72f, 0.62f, 9.4f, 0.62f, 2.9f, -1.0f};
        var10000[var10002] = var10003;
        var10002 = 6;
        var10003 = new float[]{0.74f, 0.6f, 9.5f, 0.62f, 0.3f, 1.0f};
        var10000[var10002] = var10003;
        var10002 = 7;
        var10003 = new float[]{-0.05f, -0.62f, 10.6f, 0.64f, 2.0f, 0.0f};
        var10000[var10002] = var10003;
        float[][] var22 = var10000;
        ArrayList<Sdf3D> var23 = new ArrayList<Sdf3D>();
        float[][] var24 = var22;
        int var26 = var22.length;
        for (int var29 = 0; var29 < var26; ++var29) {
            float[] var32 = var24[var29];
            int var13 = Math.min((int)Math.floor(var32[2]), var2 - 1);
            float var14 = var32[2] - (float)var13;
            float[] var15 = new float[(var13 + 2) * 4];
            for (int var16 = 0; var16 <= var13; ++var16) {
                float var17 = var3[var16];
                float var18 = AoiDesign.smooth(6.5f, 8.6f, var16);
                float var19 = 0.14f * (float)Math.sin((float)var16 * 0.9f + var32[4]) * AoiDesign.smooth(1.0f, 4.0f, var16) * (1.0f - var18);
                float var20 = var32[0] * var17 + var19;
                float var21 = var32[1] * var17 * 0.75f * (1.0f - 0.6f * var18);
                var15[var16 * 4] = var20 + (var32[5] * 1.15f * var17 + var32[0] * 0.2f * var17 - var20) * var18;
                var15[var16 * 4 + 1] = var1[var16][0] + var5[var16] * var21;
                var15[var16 * 4 + 2] = var1[var16][1] + var6[var16] * var21;
                var15[var16 * 4 + 3] = var32[3] * 1.08f * var4[var16];
            }
            int var38 = (var13 + 1) * 4;
            int var39 = Math.min(var13 + 1, var2 - 1);
            float var40 = Math.max(var14, 0.45f);
            var15[var38] = var15[var13 * 4] * 1.12f + var32[5] * 0.2f;
            var15[var38 + 1] = var1[var13][0] + (var1[var39][0] - var1[var13][0]) * var40 - 1.1f;
            var15[var38 + 2] = var15[var13 * 4 + 2] + (var1[var39][1] - var1[var13][1]) * var40 - 0.1f;
            var15[var38 + 3] = 0.04f;
            AoiDesign.split(var23, var15, 6, 1.6f, true);
        }
        float[] var25 = new float[36];
        for (int var27 = 0; var27 < 9; ++var27) {
            var25[var27 * 4 + 1] = var1[var27][0];
            var25[var27 * 4 + 2] = var1[var27][1];
            var25[var27 * 4 + 3] = 0.64f * var4[var27];
        }
        AoiDesign.split(var23, var25, 5, 1.4f, false);
        var10000 = new float[3][];
        var10002 = 0;
        var10003 = new float[]{0.05f, 30.55f, -2.15f, 0.1f, 31.45f, -2.6f, 0.15f, 32.05f, -3.35f, 0.1f, 31.85f, -4.15f};
        var10000[var10002] = var10003;
        var10002 = 1;
        var10003 = new float[]{0.3f, 30.5f, -2.1f, 0.72f, 31.25f, -2.7f, 0.98f, 31.72f, -3.45f, 1.08f, 31.42f, -4.05f};
        var10000[var10002] = var10003;
        var10002 = 2;
        var10003 = new float[]{-0.35f, 30.5f, -2.1f, -0.76f, 31.2f, -2.75f, -1.02f, 31.62f, -3.4f, -1.12f, 31.32f, -4.0f};
        var10000[var10002] = var10003;
        float[][] var30 = var28 = var10000;
        int var33 = var28.length;
        for (int var35 = 0; var35 < var33; ++var35) {
            float[] var37 = var30[var35];
            float[] var10001 = new float[]{var37[0], var37[1], var37[2], 0.36f, var37[3], var37[4], var37[5], 0.44f, var37[6], var37[7], var37[8], 0.3f, var37[9], var37[10], var37[11], 0.04f};
            var23.add(SdfShapes.tube(var10001).flattenFrom(1.8f, 0.0f, 26.0f, 0.5f));
        }
        SurfacePaint var31 = SurfacePaint.field((var0x, var1x, var2x) -> {
            int var3x = ArgbColors.mix(-14140794, -12948262, AoiDesign.smooth(27.5f, 21.5f, var1x));
            var3x = ArgbColors.mix(var3x, -13246209, AoiDesign.smooth(21.5f, 16.0f, var1x));
            float var4x = AoiDesign.smooth(30.9f, 31.5f, var1x) * AoiDesign.smooth(-2.4f, -3.2f, var2x);
            return ArgbColors.mix(var3x, -7363350, var4x * 0.5f);
        });
        Sdf3D var34 = SdfShapes.union(var23.toArray(new Sdf3D[0])).paint(var31);
        Sdf3D var36 = AoiDesign.ring(0.0f, 30.38f, -2.05f, 0.0f, 0.55f, -0.83f, 0.52f, 0.38f).roughen(0.05f, 0.3f).color(-13703425);
        CharacterPartBuilder var45 = var0.extra("ponytail", CharacterBone.HEAD).pivot(0.0f, AoiDesign.hy(30.35f), -2.3779998f);
        Sdf3D[] var46 = new Sdf3D[]{var34, var36};
        var45.shape(AoiDesign.grow(SdfShapes.union(var46))).detail(1.0f).resolution(1.3f).outline(-15327158, 0.12f).flatLight(0.4f).occlusion(0.4f).motion(BoneMotionProfile.hair().idle(3.0f, 0.0f, 2.5f, 0.3f).walk(7.0f, 0.0f, 5.0f, 2.0f).drag(22.0f).hang(0.3f).follow(CharacterBone.BODY, 0.6f).turn(18.0f, 4.0f).spring(36.0f, 4.2f).bend(1.5f).limit(70.0f));
    }

    private static void split(List<Sdf3D> var0, float[] var1, int var2, float var3, boolean var4) {
        int var5 = var1.length / 4;
        if (var5 < var2 + 3) {
            var0.add(SdfShapes.tube(var1).flattenFrom(var3, 0.0f, 26.0f, 0.5f));
        } else {
            var0.add(SdfShapes.tube(Arrays.copyOfRange(var1, 0, (var2 + 2) * 4)).flattenFrom(var3, 0.0f, 26.0f, 0.5f));
            Sdf3D var6 = SdfShapes.tube(Arrays.copyOfRange(var1, (var2 - 1) * 4, var5 * 4)).flattenFrom(var3, 0.0f, 26.0f, 0.5f);
            if (var4) {
                int var7 = (var5 - 3) * 4;
                int var8 = (var5 - 2) * 4;
                int var9 = (var5 - 1) * 4;
                float var10000 = 2.4f;
                float var10001 = 0.02f;
                float[] var10002 = new float[]{var1[var7], var1[var7 + 1], var1[var8], var1[var8 + 1], var1[var9], var1[var9 + 1]};
                Sdf2D var10 = Sdf2D.path(var10000, var10001, var10002);
                Sdf3D[] var11 = new Sdf3D[]{SdfShapes.above(var1[var8 + 1]), SdfShapes.extrude(var10, 8.0f, 0.0f)};
                var6 = ((Sdf3D)var6).intersect(SdfShapes.union(var11));
            }
            var0.add(var6);
        }
    }

    private static Sdf3D body(CharacterRig var0, PixelCanvas var1) {
        SurfacePaint var2 = SurfacePaint.field((var0x, var1x, var2x) -> ArgbColors.mix(-9274, -1331558, AoiDesign.smooth(24.9f, 25.5f, var1x))).flatLight(1.0f).occlusion(0.1f);
        Sdf3D var3 = SdfShapes.cone(0.0f, 23.2f, -0.22f, 0.66f, 0.0f, 26.3f, 0.0f, 0.58f).paint(var2);
        float var10000 = 1.0f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.ellipsoid(0.0f, 21.15f, 0.0f, 2.25f, 2.05f, 1.45f), SdfShapes.ellipsoid(0.0f, 18.7f, 0.0f, 1.78f, 1.75f, 1.22f), SdfShapes.ellipsoid(0.0f, 16.6f, -0.05f, 2.0f, 1.5f, 1.4f), SdfShapes.capsule(-2.2f, 22.3f, -0.18f, 0.0f, 22.62f, -0.18f, 0.88f), SdfShapes.capsule(0.0f, 22.62f, -0.18f, 2.2f, 22.3f, -0.18f, 0.88f)};
        Sdf3D var24 = SdfShapes.blend(var10000, var10001);
        float var29 = 0.6f;
        Sdf3D[] var10002 = new Sdf3D[]{SdfShapes.ellipsoid(0.82f, 20.85f, 0.55f, 0.88f, 0.82f, 0.85f).mirrorX()};
        Sdf3D var4 = var24.blend(var29, var10002);
        var4.paint(SurfacePaint.vertical(-2498584, 17.2f, -525828, 19.4f).flatLight(0.4f));
        Sdf3D var5 = SdfShapes.cone(0.0f, 23.0f, -0.2f, 0.9f, 0.0f, 25.3f, -0.12f, 0.74f).intersect(0.06f, SdfShapes.below(25.1f)).color(-15263457);
        Sdf3D var6 = SdfShapes.ellipsoid(0.0f, 14.95f, -0.05f, 1.95f, 1.2f, 1.35f).color(-15263457);
        var10000 = 1.1f;
        var10001 = new Sdf3D[]{SdfShapes.ellipsoid(0.0f, 21.0f, -0.1f, 2.7f, 2.4f, 1.98f), SdfShapes.capsule(-2.62f, 22.25f, -0.22f, 0.0f, 22.6f, -0.22f, 1.04f), SdfShapes.capsule(0.0f, 22.6f, -0.22f, 2.62f, 22.25f, -0.22f, 1.04f), AoiDesign.oval(-0.1f, 18.3f, 20.3f, 2.45f, 1.97f, 2.55f, 1.97f)};
        Sdf3D var7 = SdfShapes.blend(var10000, var10001).paint(SurfacePaint.field((var0x, var1x, var2x) -> ArgbColors.mix(-12959662, -1446155, AoiDesign.smooth(21.72f, 21.78f, var1x))).flatLight(0.3f).occlusion(0.18f));
        Sdf3D var8 = AoiDesign.oval(-0.1f, 17.42f, 18.45f, 2.45f, 2.03f, 2.47f, 2.02f).color(-14210250);
        Sdf3D var9 = SdfShapes.torus(0.0f, 23.52f, -0.28f, 1.28f, 0.38f).rotate(16.0f, 0.0f, 0.0f, 0.0f, 23.52f, -0.28f).color(-14210250);
        Sdf3D[] var26 = new Sdf3D[]{var7, var8, var9};
        Sdf3D var10 = SdfShapes.union(var26);
        Sdf3D var11 = SdfShapes.subtract(0.06f, var10, AoiDesign.opening(0.0f));
        float var12 = AoiDesign.frontZ(var10, -1.85f, 18.95f, 6.0f);
        float var13 = 16.95f;
        float var14 = AoiDesign.flare(AoiDesign.skirtT(var13));
        float var15 = 1.85f + 1.6f * var14 + 0.22f;
        float var16 = 1.5f + 1.4f * var14 + 0.22f;
        Sdf3D var17 = AoiDesign.ovalRing(-0.05f, var13, 0.42f, var15, var16, 0.26f).color(-15263457);
        float var18 = -0.05f + var16 * (float)Math.sqrt(1.0f - 0.3025f / (var15 * var15)) + 0.26f;
        Sdf3D var19 = SdfShapes.box(0.55f, var13, var18 + 0.05f, 0.46f, 0.36f, 0.14f, 0.1f).color(-13749702);
        CharacterPartBuilder var27 = var0.part(CharacterBone.BODY).pivot(0.0f, 24.4f, 0.0f);
        var10001 = new Sdf3D[]{var3, var5, var4, var6, var11, var17, var19};
        CharacterPartBuilder var20 = var27.shape(SdfShapes.union(var10001)).detail(1.0f).outline(-15065558, 0.13f).occlusion(0.3f);
        PixelCanvas var21 = AoiDesign.backStripe();
        PixelCanvas var22 = AoiDesign.band();
        var20.decal(AoiDesign.topGraphic()).decal(SurfaceDecal.back(var21, 2.02f, 19.6f, -2.0f, 0.6f, 4.0f).depth(1.4f, 1.4f)).decal(SurfaceDecal.back(var21, -2.02f, 19.6f, -2.0f, 0.6f, 4.0f).depth(1.4f, 1.4f)).decal(SurfaceDecal.wrap(var1, 0.0f, 18.12f, -0.1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 360.0f, 0.3f).radius(1.95f, 3.2f)).decal(SurfaceDecal.front(AoiDesign.pocket(), -1.85f, 18.95f, var12, 0.95f, 0.36f).depth(0.5f, 0.5f).maxAngle(70.0f)).decal(SurfaceDecal.front(AoiDesign.pocket().mirrored(), 1.85f, 18.95f, var12, 0.95f, 0.36f).depth(0.5f, 0.5f).maxAngle(70.0f)).decal(SurfaceDecal.front(var22, 0.0f, 17.62f, 1.9f, 2.0f, 0.28f).depth(1.2f, 0.4f).maxAngle(70.0f)).decal(SurfaceDecal.wrap(var22, 0.0f, 24.8f, -0.14f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 240.0f, 0.26f).radius(0.4f, 1.05f)).decal(SurfaceDecal.front(AoiDesign.buckle(), 0.55f, var13, var18 + 0.19f, 0.92f, 0.72f).depth(0.4f, 0.3f).maxAngle(60.0f));
        Sdf3D[] var28 = new Sdf3D[2];
        int var32 = 0;
        float[] var10003 = new float[]{0.98f, 16.7f, var18 + 0.02f, 0.28f, 1.02f, 15.8f, var18 + 0.26f, 0.28f, 1.05f, 14.9f, var18 + 0.45f, 0.27f, 1.07f, 14.3f, var18 + 0.55f, 0.26f};
        var28[var32] = SdfShapes.tube(var10003).flatten(2.2f, 0.0f, 0.0f, 1.0f).color(-15263457);
        var28[1] = SdfShapes.box(1.07f, 14.15f, var18 + 0.56f, 0.28f, 0.2f, 0.13f, 0.08f).color(-3550757);
        Sdf3D var23 = SdfShapes.union(var28);
        var0.extra("strap", CharacterBone.BODY).pivot(0.98f, 16.7f, var18).shape(var23).resolution(0.7f).detail(0.3f).noOutline().motion(BoneMotionProfile.cloth().walk(8.0f, 0.0f, 5.0f, 2.0f).drag(6.0f).spring(40.0f, 5.0f).bend(1.2f));
        return var10;
    }

    private static float openingWidth(float var0) {
        return 0.85f + 0.65f * (24.9f - var0) / 10.1f;
    }

    private static Sdf3D opening(float var0) {
        float[] var10000 = new float[]{-0.85f - var0, 24.9f, 0.85f + var0, 24.9f, 1.5f + var0, 14.8f, -1.5f - var0, 14.8f};
        return SdfShapes.extrude(Sdf2D.polygon(var10000), 2.6f, 0.0f).move(0.0f, 0.0f, 3.15f);
    }

    private static Sdf3D piping(Sdf3D var0, float var1, float var2, float var3) {
        float[] var4 = new float[24];
        for (int var5 = 0; var5 < 8; ++var5) {
            float var6 = var2 + (var3 - var2) * (float)var5 / 7.0f;
            float var7 = AoiDesign.openingWidth(var6);
            var4[var5 * 3] = var1 * (var7 + 0.09f);
            var4[var5 * 3 + 1] = var6;
            var4[var5 * 3 + 2] = AoiDesign.frontZ(var0, var1 * (var7 + 0.1f), var6, 6.0f) - 0.06f;
        }
        return SdfShapes.strand(0.13f, 0.13f, var4);
    }

    private static void skirt(CharacterRig var0, PixelCanvas var1) {
        SurfacePaint var2 = SurfacePaint.field((var0x, var1x, var2x) -> {
            float var3x = (float)Math.atan2(var0x, var2x - -0.05f);
            float var4x = AoiDesign.pleat(var3x * 18.0f / ((float)Math.PI * 2));
            return ArgbColors.mix(-14472632, -12892555, 0.4f + 0.6f * (0.5f + 0.5f * var4x));
        }).flatLight(0.15f);
        SurfaceDecal var3 = SurfaceDecal.wrap(var1, 0.0f, 11.77f, -0.05f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 360.0f, 0.27f).radius(2.4f, 4.6f);
        float var4 = (float)Math.toRadians(30.0);
        float var5 = (float)Math.toRadians(14.0);
        String[] var10000 = new String[]{"skirt_f", "skirt_s", "skirt_b"};
        String[] var6 = var10000;
        float[] var16 = new float[]{-30.0f, -90.0f, -150.0f};
        float[] var7 = var16;
        float[] var17 = new float[]{0.5f, 0.42f, 0.5f};
        float[] var8 = var17;
        float[] var18 = new float[]{16.0f, 13.0f, 15.0f};
        float[] var9 = var18;
        for (int var10 = 0; var10 < 3; ++var10) {
            float var11 = (float)Math.toRadians(var7[var10]);
            float var12 = 1.8f * (float)Math.sin(var11);
            float var13 = -0.05f + 1.45f * (float)Math.cos(var11);
            BoneMotionProfile var14 = BoneMotionProfile.skirt(CharacterBone.RIGHT_LEG).follow(CharacterBone.RIGHT_LEG, var8[var10], var9[var10]).drag(var10 == 0 ? 0.0f : 3.0f);
            CharacterPartBuilder var15 = var0.extra(var6[var10] + "_r", CharacterBone.BODY).pivot(var12, 16.9f, var13).shape(AoiDesign.skirtPanel(var11, var4, var5).paint(var2)).motion(var14).detail(0.32f).resolution(1.0f).occlusion(0.3f).noOutline().decal(var3);
            var0.extra(var6[var10] + "_l", CharacterBone.BODY).mirrorOf(var15);
        }
    }

    private static float skirtT(float var0) {
        return AoiDesign.clamp01((17.3f - var0) / 6.049999f);
    }

    private static float flare(float var0) {
        return var0 * (1.25f - 0.25f * var0);
    }

    private static float skirtRadius(float var0, float var1, float var2, float var3, float var4) {
        float var7;
        float var5 = AoiDesign.skirtT(var4);
        float var6 = AoiDesign.flare(var5);
        float var8 = 1.5f + 1.4f * var6;
        float var9 = (float)Math.sqrt(var8 * var8 * var0 * var0 + (var7 = 1.85f + 1.6f * var6) * var7 * var1 * var1);
        float var10 = var9 < 1.0E-5f ? var7 : var7 * var8 * var2 / var9;
        return var10 + (0.05f + 0.24f * var5) * AoiDesign.pleat(var3 * 18.0f / ((float)Math.PI * 2));
    }

    private static Sdf3D skirtPanel(float var0, float var1, float var2) {
        float var3 = 0.0f;
        float var4 = 0.0f;
        float var5 = -0.05f;
        float var6 = -0.05f;
        for (int var7 = 0; var7 <= 16; ++var7) {
            float var8 = var0 - var1 - var2 + (var1 + var2) * 2.0f * (float)var7 / 16.0f;
            float[] var10000 = new float[]{1.4f, 4.05f};
            for (float var12 : var10000) {
                var3 = Math.min(var3, var12 * (float)Math.sin(var8));
                var4 = Math.max(var4, var12 * (float)Math.sin(var8));
                var5 = Math.min(var5, -0.05f + var12 * (float)Math.cos(var8));
                var6 = Math.max(var6, -0.05f + var12 * (float)Math.cos(var8));
            }
        }
        return SdfShapes.field((var3x, var4x, var5x) -> {
            float var6x = var5x - -0.05f;
            float var7x = (float)Math.atan2(var3x, var6x);
            float var8x = var7x - var0;
            if ((double)var8x > Math.PI) {
                var8x -= (float)Math.PI * 2;
            }
            if ((double)var8x < -Math.PI) {
                var8x += (float)Math.PI * 2;
            }
            float var9x = Math.abs(var8x) - var1;
            float var10x = (float)Math.sqrt(var3x * var3x + var6x * var6x);
            float var11x = AoiDesign.skirtRadius(var3x, var6x, var10x, var7x, var4x) - 0.32f * AoiDesign.smooth(0.0f, 0.05f, var9x);
            float var12x = Math.abs(var10x - var11x) - 0.42f;
            float var13 = Math.max(var4x - 17.3f, 11.25f - var4x);
            float var14 = Math.max(-1.2f, Math.min(1.2f, var9x - var2));
            float var15 = var10x * var14 * (1.0f - var14 * var14 / 6.0f);
            return Math.max(var12x, Math.max(var13, var15)) * 0.8f;
        }, var3 - 0.2f, 11.05f, var5 - 0.2f, var4 + 0.2f, 17.5f, var6 + 0.2f);
    }

    private static void arms(CharacterRig var0, PixelCanvas var1) {
        float var2 = -3.68f;
        float var3 = 18.4f;
        float var4 = -0.1f;
        float var5 = var2 + 3.3f;
        float var6 = var3 - 21.8f;
        float var7 = var4 + 0.12f;
        float var8 = (float)Math.sqrt(var5 * var5 + var6 * var6 + var7 * var7);
        float var10000 = 0.45f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.sphere(-3.22f, 22.2f, -0.15f, 1.1f), SdfShapes.cone(-3.3f, 21.8f, -0.12f, 1.07f, var2, var3, var4, 0.96f)};
        Sdf3D var9 = SdfShapes.blend(var10000, var10001);
        Sdf3D var10 = var9.intersect(SdfShapes.plane(var2, var3, var4, var5 /= var8, var6 /= var8, var7 /= var8).color(-14210250)).paint(SurfacePaint.field((var0x, var1x, var2x) -> ArgbColors.mix(-1446155, -3945254, AoiDesign.smooth(-3.2f, -2.6f, var0x) * 0.7f)).flatLight(0.3f));
        float var11 = var2 - var5 * 0.05f;
        float var12 = var3 - var6 * 0.05f;
        float var13 = var4 - var7 * 0.05f;
        Sdf3D var14 = AoiDesign.ring(var11, var12, var13, var5, var6, var7, 0.86f, 0.34f).color(-14210250);
        SurfacePaint var15 = SurfacePaint.of(-9274).flatLight(1.0f).occlusion(0.25f);
        Sdf3D var16 = SdfShapes.cone(var2, var3 - 0.1f, var4 + 0.05f, 0.55f, -3.92f, 14.66f, 0.15f, 0.39f).paint(var15).rotate(-13.0f, 0.0f, 0.0f, var2, var3, var4);
        float var17 = -1.5f;
        float var18 = (float)Math.cos(Math.toRadians(var17));
        float var19 = (float)Math.sin(Math.toRadians(var17));
        float var20 = -3.3f + (var11 + 3.3f) * var18 - (var12 - 22.4f) * var19;
        float var21 = 22.4f + (var11 + 3.3f) * var19 + (var12 - 22.4f) * var18;
        float var22 = var5 * var18 - var6 * var19;
        float var23 = var5 * var19 + var6 * var18;
        float[] var24 = AoiDesign.bent(-4.15f, 12.75f, 0.45f, var2, var3, var4);
        float var25 = -3.3f + (var24[0] + 3.3f) * var18 - (var24[1] - 22.4f) * var19;
        float var26 = 22.4f + (var24[0] + 3.3f) * var19 + (var24[1] - 22.4f) * var18;
        CharacterPartBuilder var34 = var0.part(CharacterBone.RIGHT_ARM).pivot(-3.3f, 22.4f, -0.1f).grip(var25, var26, var24[2]);
        var10001 = new Sdf3D[]{var10, var14, var16};
        CharacterPartBuilder var27 = var34.shape(SdfShapes.union(var10001).rotate(0.0f, 0.0f, var17, -3.3f, 22.4f, -0.1f)).outline(-12959918, 0.12f).occlusion(0.3f).swing(0.8f).joint(-3.78f, 18.41f, -0.1f, 0.55f);
        CharacterPartBuilder var28 = var0.part(CharacterBone.LEFT_ARM).mirrorOf(var27);
        var27.decal(SurfaceDecal.wrap(var1, var20, var21, var13, -var22, -var23, -var7, -1.0f, 0.0f, 0.3f, 250.0f, 0.4f).radius(0.95f, 1.6f));
        var28.decal(SurfaceDecal.wrap(var1, -var20, var21, var13, var22, -var23, -var7, 1.0f, 0.0f, 0.3f, 250.0f, 0.4f).radius(0.95f, 1.6f));
        var28.decal(SurfaceDecal.left(AoiDesign.sleevePatch(), 4.45f, 20.3f, -0.1f, 1.05f, 1.05f).depth(0.8f, 0.8f).maxAngle(65.0f));
        var27.decal(SurfaceDecal.right(AoiDesign.sleeveText(), -4.5f, 20.2f, -0.1f, 0.62f, 1.75f).depth(0.8f, 0.8f).maxAngle(65.0f));
        CharacterPartBuilder var29 = var0.extra("hand_r", CharacterBone.RIGHT_ARM).shape(AoiDesign.hand().rotate(-13.0f, 0.0f, 0.0f, var2, var3, var4).rotate(0.0f, 0.0f, var17, -3.3f, 22.4f, -0.1f)).resolution(0.45f).detail(0.5f).outline(-6657438, 0.1f).flatLight(1.0f).occlusion(0.25f);
        var0.extra("hand_l", CharacterBone.LEFT_ARM).mirrorOf(var29);
        CharacterPartBuilder var30 = var0.extra("wrist_r", CharacterBone.RIGHT_ARM).shape(SdfShapes.ellipsoid(-3.97f, 14.12f, 0.16f, 0.31f, 0.5f, 0.37f).color(-9274).rotate(-13.0f, 0.0f, 0.0f, var2, var3, var4).rotate(0.0f, 0.0f, var17, -3.3f, 22.4f, -0.1f)).resolution(0.6f).detail(1.4f).outline(-6657438, 0.1f).flatLight(1.0f).occlusion(0.25f);
        var0.extra("wrist_l", CharacterBone.LEFT_ARM).mirrorOf(var30);
    }

    private static float[] bent(float var0, float var1, float var2, float var3, float var4, float var5) {
        double var6 = Math.toRadians(-13.0);
        float var8 = (float)Math.cos(var6);
        float var9 = (float)Math.sin(var6);
        float var10 = var1 - var4;
        float var11 = var2 - var5;
        float[] var10000 = new float[]{var0, var4 + var10 * var8 - var11 * var9, var5 + var10 * var9 + var11 * var8};
        return var10000;
    }

    private static Sdf3D hand() {
        Sdf3D var0 = SdfShapes.ellipsoid(-4.0f, 13.2f, 0.14f, 0.3f, 0.68f, 0.54f);
        float var10000 = 0.2f;
        float var10001 = 0.15f;
        float[] var10002 = new float[]{-3.86f, 13.35f, 0.6f, -3.72f, 12.9f, 0.92f, -3.62f, 12.5f, 0.98f};
        TaperedTubeSdf3D var1 = SdfShapes.strand(var10000, var10001, var10002);
        Sdf3D[] var3 = new Sdf3D[4];
        int var5 = 0;
        float var10003 = 0.17f;
        float var10004 = 0.15f;
        float[] var10005 = new float[]{-3.98f, 12.72f, 0.56f, -3.92f, 12.1f, 0.72f, -3.8f, 11.66f, 0.8f};
        var3[var5] = SdfShapes.strand(var10003, var10004, var10005);
        int var6 = 1;
        var10003 = 0.17f;
        var10004 = 0.15f;
        var10005 = new float[]{-4.0f, 12.74f, 0.24f, -3.88f, 12.12f, 0.28f, -3.66f, 11.8f, 0.3f};
        var3[var6] = SdfShapes.strand(var10003, var10004, var10005);
        int var7 = 2;
        var10003 = 0.165f;
        var10004 = 0.145f;
        var10005 = new float[]{-4.0f, 12.72f, -0.08f, -3.88f, 12.18f, -0.08f, -3.66f, 11.88f, -0.06f};
        var3[var7] = SdfShapes.strand(var10003, var10004, var10005);
        int var8 = 3;
        var10003 = 0.15f;
        var10004 = 0.13f;
        var10005 = new float[]{-3.99f, 12.66f, -0.36f, -3.88f, 12.3f, -0.36f, -3.7f, 12.06f, -0.33f};
        var3[var8] = SdfShapes.strand(var10003, var10004, var10005);
        Sdf3D var2 = SdfShapes.union(var3);
        Sdf3D[] var4 = new Sdf3D[2];
        int var9 = 0;
        var10003 = 0.12f;
        Sdf3D[] var17 = new Sdf3D[]{var0, var1, var2};
        var4[var9] = SdfShapes.blend(var10003, var17).color(-9274);
        var4[1] = SdfShapes.cylinder(-3.95f, 14.22f, 0.18f, -3.93f, 14.9f, 0.15f, 0.5f, 0.1f).color(-13703425);
        return SdfShapes.union(var4);
    }

    private static void legs(CharacterRig var0) {
        Sdf3D var1 = SdfShapes.cone(-1.24f, 14.1f, 0.0f, 1.3f, -1.1f, 8.3f, 0.1f, 0.84f);
        Sdf3D var2 = SdfShapes.sphere(-1.07f, 7.68f, 0.18f, 0.75f);
        Sdf3D var3 = SdfShapes.sphere(-1.06f, 7.8f, 0.5f, 0.52f);
        Sdf3D var4 = SdfShapes.cone(-1.07f, 7.3f, 0.06f, 0.78f, -1.12f, 2.6f, -0.1f, 0.5f);
        Sdf3D var5 = SdfShapes.ellipsoid(-1.12f, 5.8f, -0.2f, 0.72f, 1.5f, 0.74f);
        Sdf3D var6 = SdfShapes.sphere(-1.1f, 14.4f, -0.05f, 1.32f);
        SurfacePaint var7 = SurfacePaint.field((var0x, var1x, var2x) -> ArgbColors.mix(ArgbColors.mix(-14012606, -9274, AoiDesign.smooth(6.7f, 6.95f, var1x)), -15263457, AoiDesign.smooth(12.2f, 12.4f, var1x)));
        Sdf3D[] var10000 = new Sdf3D[2];
        int var10002 = 0;
        float var10003 = 0.5f;
        Sdf3D[] var10004 = new Sdf3D[]{var1, var2, var4, var5};
        Sdf3D var27 = SdfShapes.blend(var10003, var10004);
        float var29 = 0.3f;
        Sdf3D[] var10005 = new Sdf3D[]{var3};
        var10000[var10002] = var27.blend(var29, var10005);
        var10000[1] = var6;
        Sdf3D var8 = SdfShapes.union(var10000).paint(var7);
        var10000 = new Sdf3D[2];
        var10002 = 0;
        var10003 = 0.5f;
        var10004 = new Sdf3D[]{var4, var5};
        var10000[var10002] = SdfShapes.blend(var10003, var10004).inflate(0.1f).intersect(SdfShapes.below(6.95f));
        var10000[1] = SdfShapes.torus(-1.075f, 6.9f, 0.0f, 0.86f, 0.15f);
        Sdf3D var9 = SdfShapes.union(var10000).color(-14012606);
        CharacterPartBuilder var10 = var0.part(CharacterBone.RIGHT_LEG).pivot(-1.2f, 14.3f, 0.0f).shape(var8).outline(-11914188, 0.12f).flatLight(0.7f).occlusion(0.3f).swing(0.55f, 30.0f).joint(-1.07f, 7.68f, 0.18f, 0.75f);
        var0.part(CharacterBone.LEFT_LEG).mirrorOf(var10);
        CharacterPartBuilder var11 = var0.extra("sock_r", CharacterBone.RIGHT_LEG).shape(var9).resolution(0.7f).detail(0.45f).outline(-15460836, 0.12f);
        CharacterPartBuilder var12 = var0.extra("sock_l", CharacterBone.LEFT_LEG).mirrorOf(var11);
        PixelCanvas var13 = AoiDesign.socks();
        var11.decal(SurfaceDecal.wrap(var13, -1.1f, 6.42f, -0.05f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 360.0f, 0.3f).radius(0.3f, 1.4f));
        var12.decal(SurfaceDecal.wrap(var13, 1.1f, 6.42f, -0.05f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 360.0f, 0.3f).radius(0.3f, 1.4f));
        float var14 = -1.14f;
        float var15 = -7.0f;
        CharacterPartBuilder var16 = var0.extra("shoe_r", CharacterBone.RIGHT_LEG).shape(AoiDesign.shoe(var14).rotate(0.0f, var15, 0.0f, var14, 1.0f, 0.3f)).resolution(0.8f).detail(0.55f).outline(-12959664, 0.12f);
        CharacterPartBuilder var17 = var0.extra("shoe_l", CharacterBone.LEFT_LEG).mirrorOf(var16);
        PixelCanvas var18 = AoiDesign.laces();
        double var19 = Math.toRadians(var15);
        float var21 = (float)Math.cos(var19);
        float var22 = (float)Math.sin(var19);
        float var23 = var14 + 0.65f * var22;
        float var24 = 0.3f + 0.65f * var21;
        var16.decal(SurfaceDecal.project(var18, var23, 2.1f, var24, 0.87f * var22, 0.5f, 0.87f * var21, -0.5f * var22, 0.87f, -0.5f * var21, 0.8f, 1.28f).depth(0.7f, 0.7f).maxAngle(75.0f));
        var17.decal(SurfaceDecal.project(var18, -var23, 2.1f, var24, -0.87f * var22, 0.5f, 0.87f * var21, 0.5f * var22, 0.87f, -0.5f * var21, 0.8f, 1.28f).depth(0.7f, 0.7f).maxAngle(75.0f));
    }

    private static Sdf3D shoe(float var0) {
        float var10000 = 0.35f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.box(var0, 0.42f, 0.4f, 0.88f, 0.42f, 1.45f, 0.36f), SdfShapes.ellipsoid(var0, 0.44f, 1.62f, 0.88f, 0.44f, 0.95f), SdfShapes.ellipsoid(var0, 0.44f, -0.85f, 0.84f, 0.44f, 0.7f)};
        Sdf3D var1 = SdfShapes.blend(var10000, var10001);
        Sdf3D var2 = SdfShapes.ellipsoid(var0, 1.12f, 1.35f, 0.76f, 0.58f, 1.1f);
        Sdf3D var3 = SdfShapes.capsule(var0, 1.3f, 0.85f, var0, 2.05f, 0.1f, 0.7f);
        Sdf3D var4 = SdfShapes.ellipsoid(var0, 1.45f, -0.62f, 0.8f, 0.82f, 0.82f);
        Sdf3D[] var9 = new Sdf3D[]{SdfShapes.cone(var0, 1.8f, -0.3f, 0.84f, var0, 3.5f, -0.17f, 0.74f).intersect(SdfShapes.below(3.2f)), SdfShapes.torus(var0, 3.18f, -0.19f, 0.66f, 0.15f)};
        Sdf3D var5 = SdfShapes.union(var9);
        Sdf3D var6 = SdfShapes.ellipsoid(var0, 3.05f, 0.42f, 0.46f, 0.72f, 0.3f);
        var10000 = 0.4f;
        var10001 = new Sdf3D[]{var2, var3, var4, var5};
        Sdf3D var11 = SdfShapes.blend(var10000, var10001);
        float var14 = 0.15f;
        Sdf3D[] var10002 = new Sdf3D[]{var6};
        Sdf3D var7 = var11.blend(var14, var10002);
        SurfacePaint var8 = SurfacePaint.field((var0x, var1x, var2x) -> var1x < 0.9f ? ArgbColors.mix(-14012872, -920586, AoiDesign.smooth(0.05f, 0.24f, var1x)) : ArgbColors.mix(-591621, -14210250, AoiDesign.smooth(2.8f, 3.05f, var1x)));
        Sdf3D[] var12 = new Sdf3D[]{var1, var7};
        return SdfShapes.union(var12).paint(var8);
    }

    private static void gear(CharacterRig var0, Sdf3D var1) {
        ArrayList<Sdf3D> var2 = new ArrayList<Sdf3D>();
        PixelCanvas var3 = AoiDesign.phoneRing();
        CharacterPartBuilder var4 = var0.extra("gear", CharacterBone.BODY);
        for (int var5 = -1; var5 <= 1; var5 += 2) {
            float var6 = (float)var5 * 0.35f;
            float var7 = 0.45f;
            float var8 = 0.82f;
            float var9 = (float)Math.sqrt(var6 * var6 + var7 * var7 + var8 * var8);
            var7 /= var9;
            float var6Normalized = var6 /= var9;
            float var8Normalized = var8 /= var9;
            float var10 = (float)var5 * 1.55f;
            float var11 = 23.5f;
            float var12 = 1.12f;
            float var14 = var7;
            Sdf3D var19 = SdfShapes.cylinder(var10 - var6 * 0.1f, var11 - var7 * 0.1f, var12 - var8 * 0.1f, var10 + var6 * 0.16f, var11 + var7 * 0.16f, var12 + var8 * 0.16f, 0.86f, 0.12f).paint(SurfacePaint.field((var6x, var7x, var8x) -> {
                float var9x = (var6x - var10) * var6Normalized + (var7x - var11) * var14 + (var8x - var12) * var8Normalized;
                return ArgbColors.mix(-14210250, -723206, AoiDesign.smooth(0.03f, 0.1f, var9x));
            }));
            Sdf3D var20 = SdfShapes.cylinder(var10 - var6 * 0.36f, var11 - var7 * 0.36f, var12 - var8 * 0.36f, var10 - var6 * 0.08f, var11 - var7 * 0.08f, var12 - var8 * 0.08f, 0.78f, 0.1f).color(-14210250);
            Sdf3D var21 = SdfShapes.capsule(var10 - var6 * 0.1f - (float)var5 * 0.1f, var11 + 0.8f, var12 - var8 * 0.1f - 0.2f, (float)var5 * 1.3f, 24.4f, 0.5f, 0.2f).color(-14210250);
            Sdf3D[] var10001 = new Sdf3D[]{var19, var20, var21};
            var2.add(SdfShapes.union(var10001));
            var4.decal(SurfaceDecal.project(var3, var10 + var6 * 0.17f, var11 + var7 * 0.17f, var12 + var8 * 0.17f, var6, var7, var8, 0.0f, 1.0f, 0.0f, 1.3f, 1.3f).depth(0.4f, 0.4f).maxAngle(70.0f).glow());
        }
        float[] var31 = new float[]{1.3f, 24.4f, 0.5f, 0.24f, 1.22f, 24.85f, -0.35f, 0.26f, 0.72f, 25.02f, -1.2f, 0.26f, 0.0f, 25.08f, -1.42f, 0.26f, -0.72f, 25.02f, -1.2f, 0.26f, -1.22f, 24.85f, -0.35f, 0.26f, -1.3f, 24.4f, 0.5f, 0.24f};
        var2.add(SdfShapes.tube(var31).flattenFrom(1.8f, 0.0f, 24.9f, -0.2f).color(-14210250));
        Sdf3D[] var32 = new Sdf3D[]{AoiDesign.piping(var1, 1.0f, 23.3f, 17.5f), AoiDesign.piping(var1, -1.0f, 23.3f, 17.5f)};
        var2.add(SdfShapes.union(var32).color(-13703425));
        var4.shape(SdfShapes.union(var2.toArray(new Sdf3D[0]))).resolution(0.7f).detail(0.7f).outline(-15065820, 0.1f).flatLight(0.7f).occlusion(0.15f);
        float[] var10000 = new float[]{1.75f, 22.65f, 1.85f, 21.8f, 1.92f, 20.9f, 1.95f, 20.15f};
        float[] var22 = var10000;
        float[] var24 = new float[12];
        for (int var26 = 0; var26 < 4; ++var26) {
            var24[var26 * 3] = var22[var26 * 2];
            var24[var26 * 3 + 1] = var22[var26 * 2 + 1];
            var24[var26 * 3 + 2] = var26 == 0 ? 1.3f : AoiDesign.frontZ(var1, var22[var26 * 2], var22[var26 * 2 + 1], 6.0f) + 0.16f;
        }
        float var27 = var24[11];
        Sdf3D[] var30 = new Sdf3D[]{SdfShapes.strand(0.13f, 0.13f, var24).color(-7563613), SdfShapes.capsule(1.95f, 20.2f, var27, 1.96f, 19.65f, var27 + 0.02f, 0.19f).color(-12959662), SdfShapes.capsule(1.96f, 19.6f, var27 + 0.02f, 1.96f, 19.3f, var27 + 0.03f, 0.12f).color(-3550757)};
        Sdf3D var29 = SdfShapes.union(var30);
        var0.extra("cable", CharacterBone.BODY).pivot(1.75f, 22.65f, 1.3f).shape(var29).resolution(0.5f).detail(0.25f).noOutline().motion(BoneMotionProfile.cloth().idle(1.0f, 0.0f, 1.5f, 0.35f).walk(4.0f, 0.0f, 5.0f, 2.0f).drag(-2.0f).hang(0.4f).spring(45.0f, 5.0f).bend(1.1f));
    }

    private static float fu(float var0) {
        return (var0 + 2.25f) * 80.0f;
    }

    private static float fv(float var0) {
        return (28.6f - var0) * 80.0f;
    }

    private static SurfaceDecal face(List<float[]> var0) {
        PixelCanvas var1 = new PixelCanvas(Math.round(360.0f), Math.round(201.60004f));
        for (float[] var3 : var0) {
            Sdf2D var4 = null;
            int var5 = 0;
            while (var5 + 7 < var3.length) {
                if (!(var3[var5 + 1] > 29.2f) || !(var3[var5 + 5] > 29.2f)) {
                    Sdf2D var6 = Sdf2D.taper(AoiDesign.fu(var3[var5]), AoiDesign.fv(var3[var5 + 1] - 0.22f), var3[var5 + 3] * 80.0f * 1.7f, AoiDesign.fu(var3[var5 + 4]), AoiDesign.fv(var3[var5 + 5] - 0.22f), Math.max(2.0f, var3[var5 + 7] * 80.0f * 1.5f));
                    var4 = var4 == null ? var6 : var4.union(var6);
                }
                var5 += 4;
            }
            if (var4 == null) continue;
            var1.fill(var4, 2028380826, 3.0f);
        }
        for (int var7 = -1; var7 <= 1; var7 += 2) {
            AoiDesign.eye(var1, var7);
        }
        var1.fill(Sdf2D.taper(AoiDesign.fu(0.06f), AoiDesign.fv(26.8f), 4.0f, AoiDesign.fu(-0.02f), AoiDesign.fv(26.64f), 2.0f), -1714123650, 0.0f);
        var1.fill(Sdf2D.curve(AoiDesign.fu(-0.22f), AoiDesign.fv(26.2f), AoiDesign.fu(0.0f), AoiDesign.fv(26.1f), AoiDesign.fu(0.2f), AoiDesign.fv(26.23f), 3.4f, 2.2f), -7455931, 0.0f);
        for (int var8 = -1; var8 <= 1; var8 += 2) {
            var1.fill(Sdf2D.ellipse(AoiDesign.fu((float)var8 * 1.25f), AoiDesign.fv(26.42f), 32.0f, 11.0f), 1442807955, 10.0f);
        }
        float var9 = 27.34f;
        return SurfaceDecal.front(var1, 0.0f, AoiDesign.hy(var9), 3.132f, 5.22f, 2.9232004f).depth(3.0159998f, 0.8f).maxAngle(72.0f).flatLight(1.0f).occlusion(0.3f);
    }

    private static void eye(PixelCanvas var0, int var1) {
        float var2 = AoiDesign.fu((float)var1 * 0.98f);
        float var3 = AoiDesign.fv(27.2f);
        float var4 = var2 - (float)var1 * 2.0f;
        float var5 = var3 + 4.0f;
        float var6 = var2 - (float)var1 * 46.0f;
        float var7 = var2 + (float)var1 * 47.0f;
        float var8 = var3 - 40.0f;
        float var9 = var3 - 50.0f;
        float var10 = var2 - (float)var1 * 2.0f;
        float var11 = var3 - 49.0f;
        float[] var10000 = new float[]{var6 - (float)var1 * 30.0f, var8 + 2.0f, var6, var8, var10, var11, var7, var9, var7 + (float)var1 * 30.0f, var9, var7 + (float)var1 * 30.0f, var3 + 90.0f, var6 - (float)var1 * 30.0f, var3 + 90.0f};
        Sdf2D var12 = Sdf2D.polygon(var10000);
        Sdf2D var13 = Sdf2D.ellipse(var2, var3 - 2.0f, 47.0f, 53.0f).intersect(var12);
        float var10003 = var3 - 50.0f;
        float var10005 = var3 + 50.0f;
        int[] var10006 = new int[]{-2498062, -1, -1};
        var0.fill(var13, ColorField2D.linear(var2, var10003, var2, var10005, var10006));
        Sdf2D var14 = Sdf2D.ellipse(var4, var5, 32.0f, 43.0f);
        Sdf2D var15 = var14.intersect(var13);
        var10003 = var5 - 43.0f;
        var10005 = var5 + 43.0f;
        var10006 = new int[]{-16050107, -15449954, -14711060, -10687489};
        var0.fill(var15, ColorField2D.linear(var4, var10003, var4, var10005, var10006));
        var0.fill(Sdf2D.ellipse(var4, var5 + 25.0f, 22.0f, 14.0f).intersect(var15), -1871645953, 6.0f);
        var0.fill(Sdf2D.ellipse(var4, var5 + 3.0f, 12.0f, 19.0f), -16315098, 0.0f);
        var0.fill(var14.outline(4.5f).intersect(var13), -536210893, 0.0f);
        var0.fill(Sdf2D.ellipse(var2, var3 - 44.0f, 62.0f, 20.0f).intersect(var13), 1714438256, 8.0f);
        var0.fill(Sdf2D.box(var4 - 12.0f, var5 - 15.0f, 6.5f, 10.5f, 3.5f).rotate(-14.0f, var4 - 12.0f, var5 - 15.0f), -1, 0.0f);
        var0.fill(Sdf2D.box(var4 - 2.0f, var5 - 20.0f, 2.2f, 5.0f, 1.5f).rotate(-14.0f, var4 - 2.0f, var5 - 20.0f), -855638017, 0.0f);
        var0.fill(Sdf2D.circle(var4 + 13.0f, var5 + 21.0f, 4.2f), -570425345, 0.0f);
        int var16 = -15724002;
        float var10001 = 5.0f;
        float var10002 = 9.0f;
        float[] var18 = new float[]{var6, var8 + 1.0f, var10, var11 - 1.0f, var7, var9};
        var0.fill(Sdf2D.path(var10001, var10002, var18), var16, 0.0f);
        var0.fill(Sdf2D.taper(var7 - (float)var1 * 6.0f, var9 + 1.0f, 7.5f, var7 + (float)var1 * 12.0f, var9 - 7.0f, 1.5f), var16, 0.0f);
        var0.fill(Sdf2D.curve(var2 + (float)var1 * 6.0f, var3 + 51.0f, var2 + (float)var1 * 32.0f, var3 + 46.0f, var7 - (float)var1 * 3.0f, var3 + 22.0f, 1.2f, 3.0f), -1072030672, 0.0f);
        var0.fill(Sdf2D.curve(var2 - (float)var1 * 40.0f, var3 - 72.0f, var2 + (float)var1 * 2.0f, var3 - 80.0f, var2 + (float)var1 * 42.0f, var3 - 72.0f, 3.6f, 2.0f), -14011280, 0.0f);
    }

    private static PixelCanvas stripe() {
        PixelCanvas var0 = new PixelCanvas(64, 10);
        var0.fill(Sdf2D.box(32.0f, 3.3f, 40.0f, 2.8f, 0.0f), -13703425);
        var0.fill(Sdf2D.box(32.0f, 8.2f, 40.0f, 0.8f, 0.0f), -1);
        return var0;
    }

    private static PixelCanvas band() {
        PixelCanvas var0 = new PixelCanvas(64, 8);
        var0.fill(Sdf2D.box(32.0f, 4.0f, 40.0f, 3.0f, 0.0f), -13703425);
        return var0;
    }

    private static PixelCanvas socks() {
        PixelCanvas var0 = new PixelCanvas(64, 12);
        var0.fill(Sdf2D.box(32.0f, 2.5f, 40.0f, 2.0f, 0.0f), -13703425);
        var0.fill(Sdf2D.box(32.0f, 9.5f, 40.0f, 2.0f, 0.0f), -13703425);
        return var0;
    }

    private static PixelCanvas phoneRing() {
        PixelCanvas var0 = new PixelCanvas(96, 96);
        var0.fill(Sdf2D.circle(48.0f, 48.0f, 33.0f).outline(5.0f), -13703425);
        return var0;
    }

    private static SurfaceDecal topGraphic() {
        PixelCanvas var0 = new PixelCanvas(152, 120);
        var0.fill(Sdf2D.circle(76.0f, 60.0f, 42.0f).outline(10.0f), -13703425);
        float[] var10000 = new float[]{16.0f, 30.0f, 50.0f, 72.0f, 94.0f, 72.0f, 50.0f, 30.0f, 16.0f};
        float[] var1 = var10000;
        for (int var2 = 0; var2 < var1.length; ++var2) {
            var0.fill(Sdf2D.box(76.0f + (float)(var2 - 4) * 15.0f, 60.0f, 4.6f, var1[var2] * 0.5f, 3.0f), -14932394);
        }
        return SurfaceDecal.front(var0, 0.0f, 20.3f, 1.9f, 1.7f, 1.34f).depth(0.75f, 0.45f).maxAngle(62.0f);
    }

    private static PixelCanvas backStripe() {
        PixelCanvas var0 = new PixelCanvas(12, 80);
        var0.fill(Sdf2D.box(6.0f, 40.0f, 3.1f, 39.0f, 0.9f), -13703425);
        var0.fill(Sdf2D.box(0.9f, 40.0f, 0.7f, 39.0f, 0.35f), -1512206);
        var0.fill(Sdf2D.box(11.1f, 40.0f, 0.7f, 39.0f, 0.35f), -1512206);
        return var0;
    }

    private static PixelCanvas buckle() {
        PixelCanvas var0 = new PixelCanvas(64, 48);
        Sdf2D var10001 = Sdf2D.box(32.0f, 24.0f, 31.0f, 23.0f, 7.0f);
        float var10002 = 0.0f;
        float var10003 = 2.0f;
        float var10004 = 0.0f;
        float var10005 = 46.0f;
        int[] var10006 = new int[]{-1643535, -7563613};
        var0.fill(var10001, ColorField2D.linear(var10002, var10003, var10004, var10005, var10006));
        var0.fill(Sdf2D.box(32.0f, 24.0f, 21.0f, 12.0f, 4.0f), -14276045);
        var0.fill(Sdf2D.box(32.0f, 24.0f, 14.0f, 3.5f, 1.5f), -13703425);
        var0.fill(Sdf2D.box(8.5f, 24.0f, 2.0f, 9.0f, 1.0f), -14276045);
        var0.fill(Sdf2D.box(55.5f, 24.0f, 2.0f, 9.0f, 1.0f), -14276045);
        return var0;
    }

    private static PixelCanvas pocket() {
        PixelCanvas var0 = new PixelCanvas(64, 24);
        var0.fill(Sdf2D.box(32.0f, 12.0f, 28.0f, 3.4f, 3.0f), -15460837);
        var0.fill(Sdf2D.box(32.0f, 12.0f, 25.0f, 0.8f, 0.4f), -13703425);
        var0.fill(Sdf2D.box(54.0f, 15.5f, 2.2f, 5.0f, 1.2f), -3550757);
        return var0;
    }

    private static PixelCanvas sleeveText() {
        float var0 = 0.43f;
        PixelCanvas var1 = new PixelCanvas(41, 117);
        Sdf2D[] var10000 = new Sdf2D[]{Sdf2D.line(26.0f, 86.0f, 48.0f, 14.0f, 13.0f), Sdf2D.line(48.0f, 14.0f, 70.0f, 86.0f, 13.0f), Sdf2D.line(36.0f, 62.0f, 60.0f, 62.0f, 10.0f)};
        Sdf2D var2 = Sdf2D.union(var10000);
        Sdf2D var3 = Sdf2D.ellipse(48.0f, 136.0f, 22.0f, 34.0f).outline(13.0f);
        var10000 = new Sdf2D[]{Sdf2D.line(48.0f, 190.0f, 48.0f, 258.0f, 13.0f), Sdf2D.line(32.0f, 190.0f, 64.0f, 190.0f, 9.0f), Sdf2D.line(32.0f, 258.0f, 64.0f, 258.0f, 9.0f)};
        Sdf2D var4 = Sdf2D.union(var10000);
        var10000 = new Sdf2D[]{var2, var3, var4};
        Sdf2D var5 = Sdf2D.union(var10000);
        AoiDesign.ink(var1, var0, var5.move(3.0f, 3.0f), -13703425, 0.0f);
        AoiDesign.ink(var1, var0, var5, -14932394, 0.0f);
        return var1;
    }

    private static PixelCanvas sleevePatch() {
        float var0 = 0.625f;
        PixelCanvas var1 = new PixelCanvas(80, 80);
        AoiDesign.ink(var1, var0, Sdf2D.circle(64.0f, 64.0f, 60.0f), -14210250, 0.0f);
        AoiDesign.ink(var1, var0, Sdf2D.circle(64.0f, 64.0f, 52.0f).outline(6.0f), -13703425, 0.0f);
        AoiDesign.ink(var1, var0, Sdf2D.arc(64.0f, 70.0f, 30.0f, 180.0f, 360.0f, 8.0f), -591621, 0.0f);
        AoiDesign.ink(var1, var0, Sdf2D.box(36.0f, 78.0f, 8.0f, 14.0f, 5.0f), -591621, 0.0f);
        AoiDesign.ink(var1, var0, Sdf2D.box(92.0f, 78.0f, 8.0f, 14.0f, 5.0f), -591621, 0.0f);
        return var1;
    }

    private static PixelCanvas laces() {
        float var0 = 0.5f;
        PixelCanvas var1 = new PixelCanvas(48, 80);
        for (int var2 = 0; var2 < 4; ++var2) {
            float var3 = 30.0f + (float)var2 * 32.0f;
            AoiDesign.ink(var1, var0, Sdf2D.line(20.0f, var3, 76.0f, var3 + 16.0f, 9.0f), -13703425, 0.0f);
            AoiDesign.ink(var1, var0, Sdf2D.line(76.0f, var3, 20.0f, var3 + 16.0f, 9.0f), -13703425, 0.0f);
            AoiDesign.ink(var1, var0, Sdf2D.circle(14.0f, var3, 5.0f), -3550757, 0.0f);
            AoiDesign.ink(var1, var0, Sdf2D.circle(82.0f, var3, 5.0f), -3550757, 0.0f);
        }
        return var1;
    }

    private static void ink(PixelCanvas var0, float var1, Sdf2D var2, int var3, float var4) {
        var0.fill(var2.scale(var1, 0.0f, 0.0f), var3, var4 * var1);
    }

    private static float frontZ(Sdf3D var0, float var1, float var2, float var3) {
        float var6;
        float var4 = var3;
        for (int var5 = 0; var5 < 400 && var4 > -8.0f; var4 -= Math.max(0.005f, var6 * 0.7f), ++var5) {
            var6 = var0.distance(var1, var2, var4);
            if (!(var6 < 0.003f)) continue;
            return var4;
        }
        return var4;
    }

    private static Sdf3D ring(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
        float var8 = (float)Math.sqrt(var3 * var3 + var4 * var4 + var5 * var5);
        float var9 = var3 / var8;
        float var10 = var4 / var8;
        float var11 = var5 / var8;
        Sdf3D var12 = SdfShapes.torus(var0, var1, var2, var6, var7);
        if (var9 * var9 + var11 * var11 < 1.0E-8f) {
            return var12;
        }
        float var13 = (float)Math.toDegrees(Math.acos(Math.max(-1.0f, Math.min(1.0f, var10))));
        return var12.rotateAxis(var11, 0.0f, -var9, var13, var0, var1, var2);
    }

    private static Sdf3D oval(float var0, float var1, float var2, float var3, float var4, float var5, float var6) {
        float var7 = Math.max(var3, var5);
        float var8 = Math.max(var4, var6);
        return SdfShapes.field((var7x, var8x, var9) -> {
            float var10 = AoiDesign.clamp01((var8x - var1) / (var2 - var1));
            float var11 = AoiDesign.ellipse(var7x, var9 - var0, var3 + (var5 - var3) * var10, var4 + (var6 - var4) * var10);
            return Math.max(var11 * 0.95f, Math.max(var1 - var8x, var8x - var2));
        }, -var7, var1, var0 - var8, var7, var2, var0 + var8);
    }

    private static Sdf3D ovalRing(float var0, float var1, float var2, float var3, float var4, float var5) {
        float var6 = Math.min(var5, var2) * 0.5f;
        return SdfShapes.field((var7, var8, var9) -> {
            float var10 = Math.abs(AoiDesign.ellipse(var7, var9 - var0, var3, var4)) - var5 + var6;
            float var11 = Math.abs(var8 - var1) - var2 + var6;
            float var12 = Math.max(var10, 0.0f);
            float var13 = Math.max(var11, 0.0f);
            return ((float)Math.sqrt(var12 * var12 + var13 * var13) + Math.min(Math.max(var10, var11), 0.0f) - var6) * 0.95f;
        }, -var3 - var5, var1 - var2, var0 - var4 - var5, var3 + var5, var1 + var2, var0 + var4 + var5);
    }

    private static float ellipse(float var0, float var1, float var2, float var3) {
        float var4 = (float)Math.sqrt(var0 * var0 / (var2 * var2) + var1 * var1 / (var3 * var3));
        float var5 = (float)Math.sqrt(var0 * var0 / (var2 * var2 * var2 * var2) + var1 * var1 / (var3 * var3 * var3 * var3));
        return var5 < 1.0E-6f ? -Math.min(var2, var3) : var4 * (var4 - 1.0f) / var5;
    }

    private static float pleat(float var0) {
        float var1 = AoiDesign.tri(var0);
        return var1 * (1.5f - 0.5f * var1 * var1);
    }

    private static float tri(float var0) {
        float var1 = var0 - (float)Math.floor(var0);
        return 4.0f * Math.min(var1, 1.0f - var1) - 1.0f;
    }

    private static float clamp01(float var0) {
        return var0 < 0.0f ? 0.0f : Math.min(var0, 1.0f);
    }

    private static float smooth(float var0, float var1, float var2) {
        float var3 = AoiDesign.clamp01((var2 - var0) / (var1 - var0));
        return var3 * var3 * (3.0f - 2.0f * var3);
    }

    static {
        float[][] var5 = new float[5][];
        float[] fArray = new float[11];
        float[] var15 = fArray;
        fArray[0] = 0.95f;
        var15[1] = -1.55f;
        var15[2] = 29.85f;
        var15[3] = -2.0f;
        var15[4] = 29.5f;
        var15[5] = -2.3f;
        var15[6] = 28.45f;
        var15[7] = -2.24f;
        var15[8] = 27.65f;
        var15[9] = -2.0f;
        var15[10] = 27.42f;
        var5[0] = var15;
        float[] fArray2 = new float[11];
        float[] var17 = fArray2;
        fArray2[0] = 0.95f;
        var17[1] = -1.15f;
        var17[2] = 29.95f;
        var17[3] = -1.38f;
        var17[4] = 29.5f;
        var17[5] = -1.6f;
        var17[6] = 28.6f;
        var17[7] = -1.55f;
        var17[8] = 28.12f;
        var17[9] = -1.3f;
        var17[10] = 27.93f;
        var5[1] = var17;
        float[] fArray3 = new float[11];
        float[] var19 = fArray3;
        fArray3[0] = 0.85f;
        var19[1] = -0.64f;
        var19[2] = 29.95f;
        var19[3] = -0.45f;
        var19[4] = 29.4f;
        var19[5] = -0.08f;
        var19[6] = 28.25f;
        var19[7] = 0.16f;
        var19[8] = 27.4f;
        var19[9] = 0.04f;
        var19[10] = 27.02f;
        var5[2] = var19;
        float[] fArray4 = new float[11];
        float[] var5x = fArray4;
        fArray4[0] = 1.0f;
        var5x[1] = -0.08f;
        var5x[2] = 30.0f;
        var5x[3] = 0.35f;
        var5x[4] = 29.6f;
        var5x[5] = 0.9f;
        var5x[6] = 28.7f;
        var5x[7] = 1.2f;
        var5x[8] = 28.22f;
        var5x[9] = 0.98f;
        var5x[10] = 28.0f;
        var5[3] = var5x;
        float[] fArray5 = new float[11];
        float[] var23 = fArray5;
        fArray5[0] = 1.05f;
        var23[1] = 0.56f;
        var23[2] = 30.0f;
        var23[3] = 1.15f;
        var23[4] = 29.7f;
        var23[5] = 1.8f;
        var23[6] = 28.75f;
        var23[7] = 2.08f;
        var23[8] = 27.8f;
        var23[9] = 1.85f;
        var23[10] = 27.45f;
        var5[4] = var23;
        MAIN_LOCKS = var5;
        float[][] var7 = new float[3][];
        float[] fArray6 = new float[9];
        float[] var25 = fArray6;
        fArray6[0] = 0.55f;
        var25[1] = -0.95f;
        var25[2] = 29.85f;
        var25[3] = -1.0f;
        var25[4] = 29.3f;
        var25[5] = -0.9f;
        var25[6] = 28.5f;
        var25[7] = -0.76f;
        var25[8] = 28.3f;
        var7[0] = var25;
        float[] fArray7 = new float[9];
        float[] var27 = fArray7;
        fArray7[0] = 0.55f;
        var27[1] = -0.22f;
        var27[2] = 29.85f;
        var27[3] = 0.14f;
        var27[4] = 29.35f;
        var27[5] = 0.52f;
        var27[6] = 28.72f;
        var27[7] = 0.44f;
        var27[8] = 28.55f;
        var7[1] = var27;
        float[] fArray8 = new float[9];
        float[] var29 = fArray8;
        fArray8[0] = 0.5f;
        var29[1] = 0.5f;
        var29[2] = 29.85f;
        var29[3] = 1.02f;
        var29[4] = 29.4f;
        var29[5] = 1.5f;
        var29[6] = 28.5f;
        var29[7] = 1.44f;
        var29[8] = 28.2f;
        var7[2] = var29;
        ACCENT_LOCKS = var7;
    }
}

