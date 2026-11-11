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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class MikuDesign
extends CharacterDefinition {
    private static final int SKIN = -8241;
    private static final int SKIN_SHADE = -1198942;
    private static final int SKIN_LINE = -5740452;
    private static final int HAIR_ROOT = -14705514;
    private static final int HAIR = -12793152;
    private static final int HAIR_TIP = -7936291;
    private static final int HAIR_SHADE = -15368325;
    private static final int HAIR_LINE = -15903662;
    private static final int SHINE = -857866245;
    private static final int SHIRT = -2499360;
    private static final int SHIRT_SHADE = -5525063;
    private static final int SHIRT_LINE = -10788500;
    private static final int BLACK = -13618373;
    private static final int BLACK_SHADE = -14934492;
    private static final int BLACK_IN = -15592681;
    private static final int BLACK_LINE = -16119026;
    private static final int TEAL = -13645638;
    private static final int TEAL_DARK = -14971002;
    private static final int MAGENTA = -1882484;
    private static final int TATTOO = -2608065;
    private static final float SKIRT_TOP = 17.9f;
    private static final float SKIRT_HEM = 13.3f;
    private static final int PLEATS = 18;
    private static final float HS = 1.12f;
    private static final float DROP = 0.4f;
    private static final float HX = 0.0f;
    private static final float HY = 28.9f;
    private static final float HZ = -0.3f;
    private static final float PPU = 56.88889f;
    private static final float FACE_TOP = 29.95f;
    private static final float[][] LOCKS;

    public MikuDesign() {
        super("miku", "Miku");
    }

    @Override
    protected void define(CharacterRig var1) {
        var1.scale(1.0f).outline(-14011336, 0.12f).occlusion(0.3f).flatLight(0.55f);
        float[][] var2 = MikuDesign.lockPaths();
        SurfacePaint var3 = MikuDesign.hairPaint();
        MikuDesign.head(var1, var2);
        MikuDesign.hair(var1, var3);
        MikuDesign.fringe(var1, var2);
        MikuDesign.twinTails(var1);
        MikuDesign.headset(var1);
        MikuDesign.body(var1);
        MikuDesign.skirt(var1);
        MikuDesign.arms(var1);
        MikuDesign.legs(var1);
    }

    private static Sdf3D up(Sdf3D var0) {
        return var0.scale(1.12f, 0.0f, 24.6f, -0.2f).move(0.0f, -0.4f, 0.0f);
    }

    private static float tx(float var0) {
        return var0 * 1.12f;
    }

    private static float ty(float var0) {
        return 24.6f + (var0 - 24.6f) * 1.12f - 0.4f;
    }

    private static float tz(float var0) {
        return -0.2f + (var0 + 0.2f) * 1.12f;
    }

    private static Sdf3D headBase() {
        float var10000 = 0.85f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.ellipsoid(0.0f, 28.72f, -0.15f, 2.28f, 2.4f, 2.35f), SdfShapes.ellipsoid(0.0f, 27.75f, 0.55f, 2.05f, 1.3f, 1.75f), SdfShapes.cone(0.0f, 27.3f, 0.62f, 1.35f, 0.0f, 26.05f, 1.2f, 0.3f)};
        return SdfShapes.blend(var10000, var10001);
    }

    private static void head(CharacterRig var0, float[][] var1) {
        Sdf3D var2 = SdfShapes.capsule(0.0f, 23.3f, -0.45f, 0.0f, 25.9f, -0.3f, 0.58f);
        float var3 = MikuDesign.ty(25.82f);
        float var4 = MikuDesign.tz(1.2f);
        SurfacePaint var5 = SurfacePaint.field((var2x, var3x, var4x) -> {
            float var5x = var3 + Math.max(0.0f, var4 - var4x) * 0.62f;
            float var6x = MikuDesign.smooth(var5x + 0.05f, var5x - 0.2f, var3x) * (0.6f + 0.4f * MikuDesign.smooth(23.4f, 24.8f, var3x));
            return ArgbColors.mix(-8241, -1198942, var6x);
        });
        Sdf3D var6 = MikuDesign.up(MikuDesign.headBase().intersect(SdfShapes.below(30.2f)));
        CharacterPartBuilder var10000 = var0.part(CharacterBone.HEAD).pivot(0.0f, 24.6f, -0.2f);
        float var10001 = 0.45f;
        Sdf3D[] var10002 = new Sdf3D[]{var6, var2};
        var10000.shape(SdfShapes.blend(var10001, var10002).paint(var5)).detail(2.0f).flatLight(1.0f).occlusion(0.22f).outline(-5740452, 0.1f).decal(MikuDesign.face(var1));
    }

    private static float[][] lockPaths() {
        Sdf3D var0 = MikuDesign.headBase();
        float[][] var1 = new float[LOCKS.length][];
        for (int var2 = 0; var2 < LOCKS.length; ++var2) {
            float[] var3 = LOCKS[var2];
            float[] var4 = MikuDesign.onHead(36.0f, var3[2], -0.15f);
            float[] var5 = MikuDesign.onHead(58.0f, var3[2], 0.05f);
            float var6 = var5[0] + (var3[0] - var5[0]) * 0.75f;
            float var7 = var3[1] + 0.68f;
            float var8 = var3[0] - Math.signum(var3[0]) * var3[4];
            float[] var10002 = new float[]{var4[0], var4[1], var4[2], var5[0], var5[1], var5[2], var6, var7, MikuDesign.frontZ(var0, var6, var7) + var3[5], var8, var3[1], MikuDesign.frontZ(var0, var8, var3[1]) + 0.22f, var3[3]};
            var1[var2] = var10002;
        }
        return var1;
    }

    private static float frontZ(Sdf3D var0, float var1, float var2) {
        float var3;
        for (var3 = 3.5f; var3 > -1.0f && var0.distance(var1, var2, var3) > 0.0f; var3 -= 0.02f) {
        }
        return var3;
    }

    private static void fringe(CharacterRig var0, float[][] var1) {
        Sdf3D[] var2 = new Sdf3D[var1.length];
        Sdf3D[] var3 = new Sdf3D[var1.length];
        for (int var4 = 0; var4 < var1.length; ++var4) {
            float[] var5 = var1[var4];
            float var6 = var5[12];
            float[] var10000 = new float[]{var5[0], var5[1], var5[2], 0.9f * var6, var5[3], var5[4], var5[5], 1.15f * var6, var5[6], var5[7], var5[8], 0.95f * var6, var5[9], var5[10], var5[11], 0.12f};
            TaperedTubeSdf3D var7 = SdfShapes.tube(var10000).flattenFrom(3.0f, 0.0f, 28.7f, -0.15f);
            var3[var4] = SdfShapes.extrude(Sdf2D.polygon(MikuDesign.blade(var5)), 5.0f, 0.0f);
            var2[var4] = SdfShapes.intersect(var7, var3[var4]);
        }
        Sdf3D var8 = SdfShapes.intersect(SdfShapes.blend(0.15f, var2), SdfShapes.union(var3));
        Sdf3D var9 = MikuDesign.headBase();
        SurfacePaint var10 = SurfacePaint.field((var1x, var2x, var3x) -> {
            int var4x = ArgbColors.mix(ArgbColors.mix(-14705514, -12793152, 0.45f), -12793152, MikuDesign.smooth(30.9f, 29.7f, var2x));
            var4x = ArgbColors.mix(var4x, -7936291, 0.45f * MikuDesign.smooth(29.5f, 28.5f, var2x));
            return ArgbColors.mix(var4x, -15368325, 0.6f * (1.0f - MikuDesign.smooth(0.12f, 0.42f, var9.distance(var1x, var2x, var3x))));
        });
        var0.extra("fringe", CharacterBone.HEAD).shape(MikuDesign.up(var8.paint(var10))).resolution(0.45f).detail(1.05f).flatLight(0.7f).occlusion(0.1f).outline(-15903662, 0.09f).decal(MikuDesign.shine());
    }

    private static float[] blade(float[] var0) {
        float var1 = var0[12];
        boolean var2 = var0[9] < 0.0f;
        float var3 = var0[3];
        float var4 = 31.6f;
        float var5 = var0[6];
        float var6 = var0[7];
        float var7 = var0[9];
        float var8 = var0[10];
        int var9 = 12;
        float[] var10 = new float[(2 * var9 + 1) * 2];
        for (int var11 = 0; var11 <= var9; ++var11) {
            float var12 = (float)var11 / (float)var9;
            float var13 = 1.0f - var12;
            float var14 = var13 * var13 * var3 + 2.0f * var13 * var12 * var5 + var12 * var12 * var7;
            float var15 = var13 * var13 * var4 + 2.0f * var13 * var12 * var6 + var12 * var12 * var8;
            float var16 = var13 * (var5 - var3) + var12 * (var7 - var5);
            float var17 = var13 * (var6 - var4) + var12 * (var8 - var6);
            float var18 = (float)Math.sqrt(var16 * var16 + var17 * var17);
            float var19 = -var17 / var18;
            float var20 = var16 / var18;
            if (var19 > 0.0f != var2) {
                var19 = -var19;
                var20 = -var20;
            }
            float var21 = var1 * (1.0f - MikuDesign.smooth(0.6f, 1.0f, var12));
            float var22 = var1 * (1.0f - (float)Math.pow(MikuDesign.smooth(0.3f, 1.0f, var12), 0.75));
            var10[var11 * 2] = var14 + var19 * var21;
            var10[var11 * 2 + 1] = var15 + var20 * var21;
            if (var11 >= var9) continue;
            var10[(2 * var9 - var11) * 2] = var14 - var19 * var22;
            var10[(2 * var9 - var11) * 2 + 1] = var15 - var20 * var22;
        }
        return var10;
    }

    private static SurfaceDecal shine() {
        float[][] var1;
        PixelCanvas var0 = new PixelCanvas(280, 22);
        float[][] var10000 = new float[5][];
        int var10002 = 0;
        float[] var10003 = new float[]{-68.0f, -54.0f};
        var10000[var10002] = var10003;
        var10002 = 1;
        var10003 = new float[]{-45.0f, -25.0f};
        var10000[var10002] = var10003;
        var10002 = 2;
        var10003 = new float[]{-13.0f, 9.0f};
        var10000[var10002] = var10003;
        var10002 = 3;
        var10003 = new float[]{20.0f, 38.0f};
        var10000[var10002] = var10003;
        var10002 = 4;
        var10003 = new float[]{48.0f, 64.0f};
        var10000[var10002] = var10003;
        float[][] var2 = var1 = var10000;
        int var3 = var1.length;
        for (int var4 = 0; var4 < var3; ++var4) {
            float[] var5 = var2[var4];
            float var6 = var5[0];
            float var7 = var5[1];
            float var8 = (var6 + var7) * 0.5f;
            float var9 = 6.5f - Math.abs(var8) * 0.025f;
            var0.fill(Sdf2D.curve(140.0f + var6 * 2.0f, MikuDesign.ringY(var6), 140.0f + var6 + var8, MikuDesign.ringY((var6 + var8) * 0.5f), 140.0f + var8 * 2.0f, MikuDesign.ringY(var8), 1.0f, var9), -857866245);
            var0.fill(Sdf2D.curve(140.0f + var8 * 2.0f, MikuDesign.ringY(var8), 140.0f + var8 + var7, MikuDesign.ringY((var8 + var7) * 0.5f), 140.0f + var7 * 2.0f, MikuDesign.ringY(var7), var9, 1.0f), -857866245);
        }
        return SurfaceDecal.wrap(var0, 0.0f, MikuDesign.ty(30.16f), MikuDesign.tz(-0.3f), 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 140.0f, 0.6272f).radius(1.2f, 3.8f).offset(0.03f);
    }

    private static float ringY(float var0) {
        float var1 = 30.4f - 0.36f * (1.0f - (float)Math.cos(Math.toRadians(var0)));
        return (30.44f - var1) * 40.0f;
    }

    private static void hair(CharacterRig var0, SurfacePaint var1) {
        ArrayList<Sdf3D> var2 = new ArrayList<Sdf3D>();
        var2.add(SdfShapes.ellipsoid(0.0f, 28.9f, -0.3f, 2.58f, 2.62f, 2.7f));
        float[] var10000 = new float[]{64.0f, 88.0f, 272.0f, 296.0f};
        for (float var6 : var10000) {
            var2.add(MikuDesign.capLock(var6, 5.0f, 98.0f, 0.8f, 0.62f));
        }
        for (float var11 = 130.0f; var11 <= 230.0f; var11 += 25.0f) {
            var2.add(MikuDesign.capLock(var11, 20.0f, 118.0f, 0.85f, 0.5f));
        }
        Sdf3D var12 = SdfShapes.box(0.0f, 26.3f, 2.3f, 2.95f, 3.3f, 2.8f, 0.9f);
        float var16 = 0.3f;
        Sdf3D[] var10001 = new Sdf3D[2];
        int var10003 = 0;
        float var10004 = 0.62f;
        float var10005 = 0.44f;
        float[] var10006 = new float[]{-2.3f, 29.6f, -0.05f, -2.48f, 28.1f, -0.12f, -2.45f, 26.5f, -0.22f};
        var10001[var10003] = SdfShapes.strand(var10004, var10005, var10006).flatten(1.6f, 1.0f, 0.0f, 0.0f);
        var10003 = 1;
        var10004 = 0.68f;
        var10005 = 0.44f;
        var10006 = new float[]{-2.25f, 29.5f, -0.95f, -2.46f, 27.9f, -1.05f, -2.45f, 26.3f, -1.12f};
        var10001[var10003] = SdfShapes.strand(var10004, var10005, var10006).flatten(1.6f, 1.0f, 0.0f, 0.0f);
        Sdf3D var13 = SdfShapes.blend(var16, var10001).mirrorX();
        float var17 = 0.9f;
        float var22 = 0.3f;
        float[] var10002 = new float[]{0.0f, 28.0f, -2.35f, 0.0f, 26.6f, -2.2f, 0.0f, 25.6f, -1.95f};
        TaperedTubeSdf3D var14 = SdfShapes.strand(var17, var22, var10002).flatten(2.2f, 0.0f, 0.0f, 1.0f);
        float var18 = 0.25f;
        var10001 = new Sdf3D[]{SdfShapes.subtract(SdfShapes.blend(0.3f, var2.toArray(new Sdf3D[0])), var12).paint(var1), var13.paint(var1.occlusion(0.35f)), var14.paint(var1)};
        Sdf3D var15 = SdfShapes.blend(var18, var10001);
        var0.extra("hair", CharacterBone.HEAD).shape(MikuDesign.up(var15)).detail(1.1f).flatLight(0.55f).occlusion(0.22f).outline(-15903662, 0.09f).decal(MikuDesign.shine());
        var10000 = new float[]{-1.95f, 30.4f, 0.92f, 0.55f, -2.3f, 29.3f, 1.12f, 0.6f, -2.42f, 27.8f, 1.0f, 0.55f, -2.4f, 25.9f, 0.8f, 0.46f, -2.3f, 24.0f, 0.78f, 0.36f, -2.15f, 22.6f, 0.82f, 0.05f};
        TaperedTubeSdf3D var7 = SdfShapes.tube(var10000).flatten(1.8f, 1.0f, 0.0f, 0.3f);
        var10000 = new float[]{-2.2f, 30.2f, 0.1f, 0.58f, -2.52f, 28.9f, 0.24f, 0.62f, -2.58f, 27.3f, 0.32f, 0.55f, -2.45f, 25.6f, 0.5f, 0.42f, -2.36f, 23.8f, 0.52f, 0.25f, -2.3f, 23.0f, 0.55f, 0.04f};
        TaperedTubeSdf3D var8 = SdfShapes.tube(var10000).flatten(1.8f, 1.0f, 0.0f, 0.15f);
        BoneMotionProfile var9 = new BoneMotionProfile().idle(1.5f, 0.0f, 0.6f, 0.35f).walk(3.0f, 0.0f, 1.0f, 2.0f).drag(8.0f).hang(0.6f).spring(60.0f, 8.0f).bend(1.4f);
        CharacterPartBuilder var21 = var0.extra("sidelock_r", CharacterBone.HEAD).pivot(MikuDesign.tx(-2.3f), MikuDesign.ty(30.0f), MikuDesign.tz(0.6f));
        float var24 = 0.25f;
        Sdf3D[] var25 = new Sdf3D[]{var7, var8};
        CharacterPartBuilder var10 = var21.shape(MikuDesign.up(SdfShapes.blend(var24, var25).paint(var1))).motion(var9).resolution(0.7f).detail(0.6f).flatLight(0.5f).occlusion(0.35f).outline(-15903662, 0.09f);
        var0.extra("sidelock_l", CharacterBone.HEAD).mirrorOf(var10);
    }

    private static void twinTails(CharacterRig var0) {
        float var1 = MikuDesign.tx(-2.62f);
        float var2 = MikuDesign.ty(30.2f);
        float var3 = MikuDesign.tz(-1.35f);
        float[][] var10000 = new float[3][];
        int var10002 = 0;
        float[] var10003 = new float[]{0.0f, 0.0f, 1.0f, -0.9f, -0.5f, 1.35f, -1.5f, -2.6f, 1.5f, -1.75f, -6.5f, 1.45f, -1.75f, -11.5f, 1.3f, -1.6f, -16.5f, 1.05f, -1.45f, -20.5f, 0.7f, -1.35f, -23.4f, 0.06f};
        var10000[var10002] = var10003;
        var10002 = 1;
        var10003 = new float[]{0.0f, 0.1f, 0.8f, -1.0f, -0.4f, 1.0f, -1.9f, -2.8f, 1.1f, -2.2f, -7.0f, 1.0f, -2.25f, -12.0f, 0.9f, -2.05f, -17.0f, 0.7f, -1.85f, -20.8f, 0.45f, -1.95f, -22.8f, 0.04f};
        var10000[var10002] = var10003;
        var10002 = 2;
        var10003 = new float[]{0.0f, 0.1f, 0.8f, -0.7f, -0.6f, 1.0f, -1.0f, -2.9f, 1.05f, -1.15f, -7.2f, 1.0f, -1.1f, -12.2f, 0.9f, -0.95f, -16.8f, 0.7f, -0.9f, -20.2f, 0.45f, -0.75f, -22.2f, 0.04f};
        var10000[var10002] = var10003;
        float[][] var4 = var10000;
        float[] var17 = new float[]{0.0f, -0.25f, 0.2f};
        float[] var5 = var17;
        Sdf3D[] var6 = new Sdf3D[var4.length];
        for (int var7 = 0; var7 < var4.length; ++var7) {
            float[] var8 = var4[var7];
            float[] var9 = new float[var8.length / 3 * 4];
            for (int var10 = 0; var10 < var8.length / 3; ++var10) {
                float var11 = (float)var10 / ((float)var8.length / 3.0f - 1.0f);
                var9[var10 * 4] = var1 + var8[var10 * 3];
                var9[var10 * 4 + 1] = var2 + var8[var10 * 3 + 1] * 1.12f;
                var9[var10 * 4 + 2] = var3 - 1.15f * (float)Math.sin((double)Math.min(1.0f, var11 * 3.0f) * Math.PI * 0.5) + var5[var7] * var11;
                var9[var10 * 4 + 3] = var8[var10 * 3 + 2];
            }
            var6[var7] = SdfShapes.tube(var9).flatten(1.7f, 0.25f, 0.0f, 1.0f);
        }
        float var12 = var2 - 26.208f;
        SurfacePaint var13 = SurfacePaint.field((var3x, var4x, var5x) -> {
            float var6x = MikuDesign.clamp01((var2 - var4x) / (var2 - var12));
            int var7x = ArgbColors.mix(-14705514, -12793152, MikuDesign.smooth(0.0f, 0.18f, var6x));
            var7x = ArgbColors.mix(var7x, -7936291, 0.6f * MikuDesign.smooth(0.55f, 1.0f, var6x));
            float var8x = MikuDesign.smooth(0.4f, -0.6f, var3x - var1 + 1.6f) * 0.35f;
            return ArgbColors.mix(var7x, -15368325, var8x);
        });
        Sdf3D[] var18 = new Sdf3D[]{SdfShapes.box(var1 - 0.3f, var2, var3, 0.34f, 1.0f, 0.75f, 0.14f).color(-13618373), SdfShapes.box(var1 - 0.62f, var2, var3 + 0.38f, 0.06f, 0.86f, 0.09f, 0.03f).color(-1882484), SdfShapes.box(var1 - 0.62f, var2, var3 - 0.38f, 0.06f, 0.86f, 0.09f, 0.03f).color(-1882484)};
        Sdf3D var14 = SdfShapes.union(var18).rotate(0.0f, 0.0f, -12.0f, var1, var2, var3);
        BoneMotionProfile var15 = BoneMotionProfile.hair().hang(0.25f).follow(CharacterBone.BODY, 0.8f).drag(12.0f).turn(6.0f, 0.0f).spring(40.0f, 6.0f).bend(1.6f).limit(35.0f);
        CharacterPartBuilder var19 = var0.extra("tail_r", CharacterBone.HEAD).pivot(var1, var2, var3);
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.blend(0.35f, var6).paint(var13), var14};
        CharacterPartBuilder var16 = var19.shape(SdfShapes.union(var10001)).motion(var15).resolution(1.0f).detail(0.9f).flatLight(0.5f).occlusion(0.3f).outline(-15903662, 0.1f);
        var0.extra("tail_l", CharacterBone.HEAD).mirrorOf(var16);
    }

    private static void headset(CharacterRig var0) {
        Sdf3D[] var10000 = new Sdf3D[]{SdfShapes.cylinder(-2.45f, 28.0f, 0.05f, -2.85f, 28.0f, 0.05f, 0.62f, 0.12f).color(-13618373), SdfShapes.cylinder(-2.8f, 28.0f, 0.05f, -2.95f, 28.0f, 0.05f, 0.38f, 0.06f).color(-13645638)};
        Sdf3D var1 = SdfShapes.union(var10000);
        var10000 = new Sdf3D[2];
        int var10002 = 0;
        float[] var10003 = new float[]{-2.7f, 27.7f, 0.45f, 0.08f, -2.45f, 26.8f, 1.3f, 0.08f, -1.7f, 26.25f, 2.05f, 0.08f, -0.95f, 26.15f, 2.25f, 0.08f};
        var10000[var10002] = SdfShapes.tube(var10003).color(-13618373);
        var10000[1] = SdfShapes.ellipsoid(-0.85f, 26.15f, 2.28f, 0.22f, 0.16f, 0.16f).color(-14934492);
        Sdf3D var2 = SdfShapes.union(var10000);
        CharacterPartBuilder var4 = var0.extra("headset", CharacterBone.HEAD);
        Sdf3D[] var10001 = new Sdf3D[]{var1, var1.mirrored(), var2};
        var4.shape(MikuDesign.up(SdfShapes.union(var10001))).resolution(0.4f).detail(0.6f).flatLight(0.4f).outline(-16119026, 0.08f);
    }

    private static Sdf3D capLock(float var0, float var1, float var2, float var3, float var4) {
        float[] var5 = new float[15];
        for (int var6 = 0; var6 < 5; ++var6) {
            float[] var7 = MikuDesign.onHead(var1 + (var2 - var1) * (float)var6 / 4.0f, var0, 0.02f);
            System.arraycopy(var7, 0, var5, var6 * 3, 3);
        }
        return SdfShapes.strand(var3, var4, var5).flattenFrom(2.8f, 0.0f, 28.9f, -0.3f);
    }

    private static float[] onHead(float var0, float var1, float var2) {
        double var3 = Math.toRadians(var0);
        double var5 = Math.toRadians(var1);
        float var7 = (float)Math.cos(var3);
        float var8 = (float)Math.sin(var3);
        float var9 = (float)Math.cos(var5);
        float var10 = (float)Math.sin(var5);
        float var11 = var10 * var8;
        float var12 = 0.97f * var7 + 0.24f * var9 * var8;
        float var13 = -0.24f * var7 + 0.97f * var9 * var8;
        float var14 = 1.0f / (float)Math.sqrt(var11 * var11 / 7.3984003f + var12 * var12 / 7.3984003f + var13 * var13 / 8.065599f) + var2;
        float[] var10000 = new float[]{0.0f + var11 * var14, 28.9f + var12 * var14, -0.3f + var13 * var14};
        return var10000;
    }

    private static SurfacePaint hairPaint() {
        return SurfacePaint.field((var0, var1, var2) -> {
            float var3 = var1 - 31.2f;
            float var4 = var2 + 0.7f;
            float var5 = (float)Math.sqrt(var0 * var0 + var3 * var3 + var4 * var4);
            int var6 = ArgbColors.mix(-14705514, -12793152, MikuDesign.smooth(0.8f, 2.9f, var5));
            return ArgbColors.mix(var6, -7936291, MikuDesign.smooth(5.5f, 17.5f, var5) * 0.9f);
        });
    }

    private static void body(CharacterRig var0) {
        Sdf3D[] var10000 = new Sdf3D[]{SdfShapes.capsule(0.0f, 23.95f, -0.15f, -2.45f, 23.3f, -0.15f, 0.9f), SdfShapes.capsule(0.0f, 23.95f, -0.15f, 2.45f, 23.3f, -0.15f, 0.9f)};
        Sdf3D var1 = SdfShapes.union(var10000);
        Sdf3D var2 = SdfShapes.ellipsoid(0.0f, 21.7f, 0.05f, 2.45f, 2.1f, 1.65f);
        Sdf3D var3 = SdfShapes.ellipsoid(0.84f, 21.6f, 0.6f, 0.9f, 0.8f, 0.95f).mirrorX();
        Sdf3D var4 = SdfShapes.ellipsoid(0.0f, 19.6f, 0.0f, 2.0f, 1.7f, 1.45f);
        Sdf3D var5 = SdfShapes.ellipsoid(0.0f, 17.8f, 0.0f, 2.15f, 1.6f, 1.5f);
        float var18 = 1.0f;
        Sdf3D[] var10001 = new Sdf3D[4];
        var10001[0] = var1;
        int var10003 = 1;
        float var10004 = 0.8f;
        Sdf3D[] var10005 = new Sdf3D[]{var2, var3};
        var10001[var10003] = SdfShapes.blend(var10004, var10005);
        var10001[2] = var4;
        var10001[3] = var5;
        Sdf3D var6 = SdfShapes.blend(var18, var10001).intersect(SdfShapes.above(17.2f));
        SurfacePaint var7 = SurfacePaint.field((var0x, var1x, var2x) -> {
            float var3x = MikuDesign.smooth(0.5f, 1.3f, var2x) * MikuDesign.smooth(19.9f, 20.5f, var1x) * (1.0f - MikuDesign.smooth(20.6f, 21.1f, var1x));
            float var4x = MikuDesign.smooth(1.6f, 2.3f, Math.abs(var0x)) * 0.35f;
            return ArgbColors.mix(-2499360, -5525063, Math.max(var3x * 0.55f, var4x));
        });
        var0.part(CharacterBone.BODY).shape(var6.paint(var7)).outline(-10788500, 0.1f).detail(0.85f).occlusion(0.2f);
        float[] var19 = new float[]{0.05f, 24.5f, 1.15f, 24.55f, 1.35f, 23.7f, 0.6f, 23.15f, 0.08f, 23.55f};
        Sdf2D var8 = Sdf2D.polygon(var19);
        Sdf3D var9 = SdfShapes.extrude(var8, 0.14f, 0.07f).rotate(-25.0f, -20.0f, 0.0f, 0.5f, 23.9f, 0.0f).move(0.0f, 0.0f, MikuDesign.frontZ(var6, 0.6f, 23.8f) + 0.02f);
        var10000 = new Sdf3D[]{var9, var9.mirrored()};
        Sdf3D var10 = SdfShapes.union(var10000).color(-2499360);
        var0.extra("collar", CharacterBone.BODY).shape(var10).resolution(0.35f).detail(0.8f).flatLight(0.3f).outline(-13645638, 0.1f);
        float[] var21 = new float[]{23.35f, 22.4f, 21.3f, 20.2f, 19.3f};
        float[] var11 = var21;
        float[] var22 = new float[]{0.26f, 0.32f, 0.38f, 0.42f, 0.44f};
        float[] var12 = var22;
        float[] var13 = new float[var11.length * 4];
        for (int var14 = 0; var14 < var11.length; ++var14) {
            var13[var14 * 4 + 1] = var11[var14];
            var13[var14 * 4 + 2] = MikuDesign.frontZ(var6, 0.0f, var11[var14]) + 0.1f;
            var13[var14 * 4 + 3] = var12[var14];
        }
        float[] var23 = new float[]{-1.0f, 24.0f, 1.0f, 24.0f, 1.0f, 19.7f, 0.0f, 19.1f, -1.0f, 19.7f};
        Sdf2D var17 = Sdf2D.polygon(var23);
        Sdf3D var15 = SdfShapes.intersect(SdfShapes.tube(var13).flatten(3.5f, 0.0f, 0.0f, 1.0f), SdfShapes.extrude(var17, 4.0f, 0.0f));
        Sdf3D var16 = SdfShapes.ellipsoid(0.0f, 23.45f, MikuDesign.frontZ(var6, 0.0f, 23.45f) + 0.12f, 0.36f, 0.3f, 0.22f);
        CharacterPartBuilder var24 = var0.extra("tie", CharacterBone.BODY).pivot(0.0f, 23.45f, 1.6f);
        var10001 = new Sdf3D[]{var15, var16};
        var24.shape(SdfShapes.union(var10001).paint(SurfacePaint.vertical(-14971002, 19.3f, -13645638, 22.5f))).motion(BoneMotionProfile.none().walk(3.0f, 0.0f, 2.0f, 2.0f).drag(4.0f).spring(80.0f, 8.0f)).resolution(0.4f).detail(0.5f).flatLight(0.4f).outline(-15835560, 0.08f);
    }

    private static void skirt(CharacterRig var0) {
        Sdf3D var1 = SdfShapes.field(MikuDesign::skirtDistance, -3.4f, 13.1f, -3.0f, 3.4f, 18.1f, 3.0f).paint(SurfacePaint.field(MikuDesign::skirtColor));
        var0.extra("skirt", CharacterBone.BODY).pivot(0.0f, 17.9f, 0.0f).shape(var1).resolution(0.9f).detail(0.8f).occlusion(0.35f).outline(-16119026, 0.12f).decal(MikuDesign.stripe(13.650001f, 0.26f)).motion(BoneMotionProfile.none().walk(0.0f, 8.0f, 0.0f, 1.0f).walkPhase(-90.0f).drag(2.0f).spring(80.0f, 10.0f));
    }

    private static float flare(float var0) {
        float var1 = MikuDesign.clamp01((17.6f - var0) / 4.299999f);
        return var1 * (2.0f - var1) * 0.8f + var1 * 0.2f;
    }

    private static float pleat(float var0, float var1, float var2) {
        float var3 = 2.25f + 0.95f * var2;
        float var4 = 1.62f + 0.95f * var2;
        double var5 = Math.atan2(var0 / var3, var1 / var4) * 18.0 / (Math.PI * 2);
        var5 -= Math.floor(var5);
        return (float)Math.abs(2.0 * var5 - 1.0);
    }

    private static float skirtDistance(float var0, float var1, float var2) {
        float var3 = MikuDesign.flare(var1);
        float var4 = 2.25f + 0.95f * var3;
        float var5 = 1.62f + 0.95f * var3;
        float var6 = var0 / var4;
        float var7 = var2 / var5;
        float var8 = (float)Math.sqrt(var6 * var6 + var7 * var7);
        float var9 = (var8 - 1.0f) * Math.min(var4, var5) * 0.72f - 0.2f * var3 * (MikuDesign.pleat(var0, var2, var3) - 0.5f);
        return Math.max(var9, Math.max(var1 - 17.9f, 13.3f - var1));
    }

    private static int skirtColor(float var0, float var1, float var2) {
        float var3 = MikuDesign.flare(var1);
        return ArgbColors.mix(-14934492, -13618373, 0.35f + 0.65f * (1.0f - var3 + var3 * MikuDesign.pleat(var0, var2, var3)));
    }

    private static SurfaceDecal stripe(float var0, float var1) {
        PixelCanvas var2 = new PixelCanvas(512, 24);
        var2.fill(Sdf2D.box(256.0f, 12.0f, 256.0f, 8.0f, 0.0f), -13645638);
        return SurfaceDecal.wrap(var2, 0.0f, var0, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 360.0f, var1).radius(1.8f, 3.4f);
    }

    private static void arms(CharacterRig var0) {
        float var1 = -3.5f;
        float var2 = 23.1f;
        float var3 = -0.12f;
        float var4 = -3.95f;
        float var5 = 19.5f;
        float var6 = -0.3f;
        float var7 = -4.2f;
        float var8 = 16.0f;
        float var9 = 0.4f;
        float var10000 = 0.35f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.sphere(var1 + 0.1f, var2, var3, 0.66f), SdfShapes.cone(var1, var2, var3, 0.62f, var4, var5, var6, 0.52f), SdfShapes.cone(var4, var5, var6, 0.52f, var7, var8, var9, 0.44f)};
        Sdf3D var10 = SdfShapes.blend(var10000, var10001).paint(SurfacePaint.field((var0x, var1x, var2x) -> {
            float var3x = MikuDesign.smooth(23.4f, 21.8f, var1x);
            return ArgbColors.mix(-1198942, -8241, 0.4f + 0.6f * var3x);
        }));
        CharacterPartBuilder var11 = var0.part(CharacterBone.RIGHT_ARM).pivot(-3.5f, 23.25f, -0.1f).grip(-4.32f, 15.05f, 0.72f).shape(var10).resolution(0.7f).detail(0.8f).flatLight(0.8f).outline(-5740452, 0.1f).swing(0.55f).joint(var4, var5, var6, 0.55f);
        var0.part(CharacterBone.LEFT_ARM).mirrorOf(var11).decal(MikuDesign.tattoo(-var1, var2, var3, -var4, var5, var6));
        float var12 = (float)Math.sqrt((var7 - var4) * (var7 - var4) + (var8 - var5) * (var8 - var5) + (var9 - var6) * (var9 - var6));
        float var13 = (var7 - var4) / var12;
        float var14 = (var8 - var5) / var12;
        float var15 = (var9 - var6) / var12;
        float var16 = var4 + 0.35f * (var7 - var4);
        float var17 = var5 + 0.35f * (var8 - var5) + 0.9f;
        float var18 = var6 + 0.35f * (var9 - var6);
        float var19 = var7 + 1.1f * var13;
        float var20 = var8 + 1.1f * var14;
        float var21 = var9 + 1.1f * var15;
        Sdf3D var22 = SdfShapes.cone(var16, var17 + 0.3f, var18, 0.62f, var19, var20, var21, 1.15f);
        Sdf3D var23 = SdfShapes.sphere(var19 + 0.25f * var13, var20 + 0.25f * var14, var21 + 0.25f * var15, 0.95f);
        float var25 = var17 + 0.3f;
        float var27 = (float)Math.sqrt((var19 - var16) * (var19 - var16) + (var20 - var25) * (var20 - var25) + (var21 - var18) * (var21 - var18));
        Sdf3D var28 = SdfShapes.subtract(0.1f, var22, var23).paint(SurfacePaint.field((var7x, var8x, var9x) -> {
            float var10x = ((var7x - var16) * (var19 - var16) + (var8x - var25) * (var20 - var25) + (var9x - var18) * (var21 - var18)) / (var27 * var27);
            return ArgbColors.mix(-13618373, -14934492, MikuDesign.smooth(0.2f, 1.0f, var10x) * 0.5f);
        }));
        PixelCanvas var29 = new PixelCanvas(256, 16);
        var29.fill(Sdf2D.box(128.0f, 8.0f, 128.0f, 6.0f, 0.0f), -13645638);
        float var30 = var19 - var16;
        float var31 = var20 - var25;
        float var32 = var21 - var18;
        SurfaceDecal var33 = SurfaceDecal.wrap(var29, var16 + var30 * 0.93f, var25 + var31 * 0.93f, var18 + var32 * 0.93f, var30, var31, var32, 0.0f, 0.0f, 1.0f, 360.0f, var27 * 0.1f).radius(0.7f, 1.4f);
        SurfaceDecal var34 = SurfaceDecal.wrap(var29, var16 + var30 * 0.05f, var25 + var31 * 0.05f, var18 + var32 * 0.05f, var30, var31, var32, 0.0f, 0.0f, 1.0f, 360.0f, var27 * 0.08f).radius(0.4f, 1.0f);
        CharacterPartBuilder var35 = var0.extra("sleeve_r", CharacterBone.RIGHT_ARM).shape(var28).resolution(1.1f).detail(0.8f).flatLight(0.3f).occlusion(0.4f).outline(-16119026, 0.1f).decal(var33).decal(var34);
        var0.extra("sleeve_l", CharacterBone.LEFT_ARM).mirrorOf(var35);
        MikuDesign.hands(var0, var7, var8, var9);
    }

    private static SurfaceDecal tattoo(float var0, float var1, float var2, float var3, float var4, float var5) {
        PixelCanvas var6 = new PixelCanvas(64, 40);
        var6.fill(Sdf2D.ellipse(44.0f, 20.0f, 9.0f, 14.0f).outline(4.5f), -2608065);
        var6.fill(Sdf2D.line(20.0f, 7.0f, 20.0f, 34.0f, 4.5f), -2608065);
        var6.fill(Sdf2D.line(20.0f, 7.0f, 14.0f, 13.0f, 4.0f), -2608065);
        float var7 = 0.22f;
        float var8 = var0 + (var3 - var0) * var7;
        float var9 = var1 + (var4 - var1) * var7;
        float var10 = var2 + (var5 - var2) * var7;
        return SurfaceDecal.left(var6, var8 + 0.55f, var9, var10 + 0.05f, 1.0f, 0.62f).depth(0.6f, 0.6f).maxAngle(70.0f).flatLight(0.8f);
    }

    private static void hands(CharacterRig var0, float var1, float var2, float var3) {
        float var4 = 4.3f;
        float var5 = 15.25f;
        float var6 = 0.6f;
        ArrayList<Sdf3D> var7 = new ArrayList<Sdf3D>();
        var7.add(SdfShapes.ellipsoid(var4, var5, var6, 0.33f, 0.6f, 0.52f));
        var7.add(SdfShapes.capsule(var4 - 0.02f, var5 + 0.45f, var6 - 0.12f, -var1, var2 + 0.1f, var3, 0.32f));
        float var10001 = 0.15f;
        float var10002 = 0.13f;
        float[] var10003 = new float[]{var4 + 0.02f, var5 - 0.4f, var6 + 0.36f, var4 - 0.02f, var5 - 0.86f, var6 + 0.44f, var4 - 0.2f, var5 - 1.1f, var6 + 0.4f};
        var7.add(SdfShapes.strand(var10001, var10002, var10003));
        var10001 = 0.15f;
        var10002 = 0.13f;
        var10003 = new float[]{var4 + 0.03f, var5 - 0.44f, var6 + 0.12f, var4, var5 - 0.98f, var6 + 0.14f, var4 - 0.22f, var5 - 1.26f, var6 + 0.11f};
        var7.add(SdfShapes.strand(var10001, var10002, var10003));
        var10001 = 0.14f;
        var10002 = 0.12f;
        var10003 = new float[]{var4 + 0.03f, var5 - 0.42f, var6 - 0.12f, var4, var5 - 0.9f, var6 - 0.13f, var4 - 0.2f, var5 - 1.16f, var6 - 0.14f};
        var7.add(SdfShapes.strand(var10001, var10002, var10003));
        var10001 = 0.13f;
        var10002 = 0.11f;
        var10003 = new float[]{var4 + 0.02f, var5 - 0.36f, var6 - 0.34f, var4, var5 - 0.74f, var6 - 0.36f, var4 - 0.16f, var5 - 0.95f, var6 - 0.35f};
        var7.add(SdfShapes.strand(var10001, var10002, var10003));
        var10001 = 0.17f;
        var10002 = 0.14f;
        var10003 = new float[]{var4 - 0.18f, var5 + 0.1f, var6 + 0.42f, var4 - 0.3f, var5 - 0.3f, var6 + 0.58f, var4 - 0.32f, var5 - 0.54f, var6 + 0.5f};
        var7.add(SdfShapes.strand(var10001, var10002, var10003));
        CharacterPartBuilder var8 = var0.extra("hand_l", CharacterBone.LEFT_ARM).shape(SdfShapes.blend(0.12f, var7.toArray(new Sdf3D[0])).color(-8241)).resolution(0.45f).detail(0.35f).flatLight(1.0f).occlusion(0.25f).outline(-5740452, 0.1f);
        var0.extra("hand_r", CharacterBone.RIGHT_ARM).mirrorOf(var8);
    }

    private static void legs(CharacterRig var0) {
        float var1 = -1.1f;
        Sdf3D var2 = SdfShapes.sphere(var1, 14.6f, -0.05f, 1.02f);
        Sdf3D var3 = SdfShapes.cone(var1 - 0.05f, 14.3f, 0.0f, 1.02f, var1, 8.4f, 0.08f, 0.76f);
        Sdf3D var4 = SdfShapes.sphere(var1, 7.9f, 0.1f, 0.72f);
        float[] var10000 = new float[]{var1, 7.6f, 0.08f, 0.72f, var1 - 0.02f, 5.8f, -0.12f, 0.8f, var1 - 0.04f, 3.6f, -0.02f, 0.56f, var1 - 0.05f, 2.0f, 0.0f, 0.48f};
        TaperedTubeSdf3D var5 = SdfShapes.tube(var10000);
        float var13 = 0.45f;
        Sdf3D[] var10001 = new Sdf3D[]{var2, var3, var4, var5};
        Sdf3D var6 = SdfShapes.blend(var13, var10001);
        Sdf3D var7 = var6.intersect(SdfShapes.above(10.6f)).paint(SurfacePaint.vertical(-1198942, 10.8f, -8241, 12.5f));
        Sdf3D var8 = SdfShapes.cylinder(var1, 11.05f, 0.03f, var1, 10.35f, 0.05f, 0.9f, 0.12f).color(-13645638);
        float var14 = 0.3f;
        var10001 = new Sdf3D[]{var6.intersect(SdfShapes.below(10.4f)).inflate(0.09f), MikuDesign.shoe(var1)};
        Sdf3D var9 = SdfShapes.blend(var14, var10001).paint(SurfacePaint.field((var0x, var1x, var2x) -> ArgbColors.mix(-13618373, -14934492, MikuDesign.smooth(-0.2f, -0.8f, var2x) * 0.6f)));
        CharacterPartBuilder var10 = var0.part(CharacterBone.RIGHT_LEG).pivot(var1, 14.2f, 0.0f).shape(var7).detail(0.6f).resolution(1.0f).flatLight(0.8f).outline(-5740452, 0.1f).occlusion(0.3f).swing(0.55f, 30.0f).joint(var1, 7.9f, 0.1f, 0.8f);
        var0.part(CharacterBone.LEFT_LEG).mirrorOf(var10);
        CharacterPartBuilder var11 = var0.extra("boot_r", CharacterBone.RIGHT_LEG).shape(var9).detail(0.8f).resolution(1.0f).outline(-16119026, 0.11f).occlusion(0.3f);
        var0.extra("boot_l", CharacterBone.LEFT_LEG).mirrorOf(var11);
        CharacterPartBuilder var15 = var0.extra("boot_trim_r", CharacterBone.RIGHT_LEG);
        var10001 = new Sdf3D[]{var8, MikuDesign.sole(var1)};
        CharacterPartBuilder var12 = var15.shape(SdfShapes.union(var10001)).detail(0.4f).resolution(0.6f).flatLight(0.3f).outline(-15835560, 0.09f);
        var0.extra("boot_trim_l", CharacterBone.LEFT_LEG).mirrorOf(var12);
    }

    private static Sdf3D shoe(float var0) {
        float var10000 = 0.4f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.ellipsoid(var0, 1.1f, 0.95f, 0.62f, 0.6f, 1.15f), SdfShapes.ellipsoid(var0, 1.4f, -0.35f, 0.62f, 0.9f, 0.72f), SdfShapes.cone(var0, 2.3f, -0.05f, 0.5f, var0, 1.2f, 0.0f, 0.6f)};
        return SdfShapes.blend(var10000, var10001);
    }

    private static Sdf3D sole(float var0) {
        Sdf3D var1 = SdfShapes.box(var0, 0.62f, 0.85f, 0.64f, 0.14f, 1.3f, 0.12f);
        Sdf3D var2 = SdfShapes.box(var0, 0.3f, -0.55f, 0.46f, 0.3f, 0.4f, 0.1f);
        float var10000 = 0.12f;
        Sdf3D[] var10001 = new Sdf3D[]{var1, var2};
        return SdfShapes.blend(var10000, var10001).intersect(SdfShapes.above(0.0f)).paint(SurfacePaint.vertical(-14971002, 0.0f, -13645638, 0.7f));
    }

    private static float px(float var0) {
        return 128.0f + var0 * 56.88889f;
    }

    private static float py(float var0) {
        return (29.95f - var0) * 56.88889f;
    }

    private static SurfaceDecal face(float[][] var0) {
        PixelCanvas var1 = new PixelCanvas(88, 136);
        float var2 = 40.0f;
        float var3 = 70.0f;
        Sdf2D var4 = Sdf2D.ellipse(var2, var3, 27.5f, 33.0f);
        float var10003 = var3 - 33.0f;
        float var10005 = var3 + 10.0f;
        int[] var10006 = new int[]{-1904912, -1};
        var1.fill(var4, ColorField2D.linear(var2, var10003, var2, var10005, var10006));
        var1.fill(Sdf2D.ellipse(var2, var3 - 34.0f, 34.0f, 22.0f).intersect(var4), -1715153190, 2.0f);
        Sdf2D var5 = Sdf2D.ellipse(var2, var3 + 3.5f, 20.5f, 27.5f);
        Sdf2D var6 = var5.intersect(var4);
        var10003 = var3 - 24.0f;
        var10005 = var3 + 30.0f;
        var10006 = new int[]{-16106432, -14504024, -6491416};
        var1.fill(var6, ColorField2D.linear(var2, var10003, var2, var10005, var10006));
        var1.fill(var5.outline(2.6f).intersect(var4), -1341506496);
        var1.fill(Sdf2D.ellipse(var2, var3 + 1.5f, 9.0f, 12.5f), -16439768);
        var1.fill(Sdf2D.ellipse(var2, var3 + 17.0f, 12.0f, 6.5f).intersect(var6), -2133655560, 3.0f);
        var1.fill(Sdf2D.curve(var2 + 26.5f, var3 - 13.0f, var2 + 2.0f, var3 - 46.0f, var2 - 30.0f, var3 - 6.0f, 4.2f, 8.4f), -14800336);
        var1.fill(Sdf2D.taper(var2 - 25.0f, var3 - 9.0f, 6.0f, var2 - 35.0f, var3 - 6.0f, 1.4f), -14800336);
        var1.fill(Sdf2D.curve(var2 - 5.0f, var3 + 32.0f, var2 - 20.0f, var3 + 31.0f, var2 - 26.5f, var3 + 16.0f, 1.6f, 2.8f), -868591010);
        var1.fill(Sdf2D.curve(var2 + 15.0f, var3 - 38.0f, var2, var3 - 48.0f, var2 - 23.0f, var3 - 33.0f, 1.5f, 1.5f), 2005965984);
        var1.fill(Sdf2D.curve(var2 + 20.0f, var3 - 48.0f, var2 + 2.0f, var3 - 57.0f, var2 - 22.0f, var3 - 51.0f, 4.0f, 2.6f), -13989243);
        var1.fill(Sdf2D.ellipse(var2 - 4.0f, var3 + 46.0f, 21.0f, 9.0f), 1442810524, 10.0f);
        PixelCanvas var7 = new PixelCanvas(256, 220);
        int var8 = Math.round(MikuDesign.py(28.0f)) - 70;
        ArrayList<Sdf2D> var9 = new ArrayList<Sdf2D>();
        float[][] var10 = var0;
        int var11 = var0.length;
        for (int var12 = 0; var12 < var11; ++var12) {
            float[] var13 = var10[var12];
            float[] var14 = MikuDesign.blade(var13);
            for (int var15 = 0; var15 < var14.length; var15 += 2) {
                var14[var15] = MikuDesign.px(var14[var15]);
                var14[var15 + 1] = MikuDesign.py(var14[var15 + 1]) + 14.0f;
            }
            var9.add(Sdf2D.polygon(var14));
        }
        var9.add(Sdf2D.box(128.0f, MikuDesign.py(29.75f), 130.0f, 14.222222f, 0.0f));
        var7.fill(Sdf2D.union(var9.toArray(new Sdf2D[0])), -1931434602, 3.0f);
        var7.draw(var1, 28, var8);
        var7.draw(var1.mirrored(), 140, var8);
        for (int var16 = -1; var16 <= 1; var16 += 2) {
            float var17 = 128.0f + (float)var16 * 60.0f;
            var7.fill(Sdf2D.ellipse(var17 - 7.5f, (float)var8 + 60.0f, 7.0f, 8.8f), -1);
            var7.fill(Sdf2D.circle(var17 + 8.0f, (float)var8 + 83.0f, 3.0f), -570425345);
            var7.fill(Sdf2D.circle(var17 - 12.0f, (float)var8 + 76.0f, 1.6f), -1426063361);
        }
        var7.fill(Sdf2D.line(130.0f, (float)var8 + 119.0f, 127.0f, (float)var8 + 124.5f, 2.4f), -1713795968);
        float var10001 = 3.0f;
        float var10002 = 3.0f;
        float[] var19 = new float[]{118.0f, (float)var8 + 156.0f, 123.0f, (float)var8 + 161.0f, 128.0f, (float)var8 + 157.5f, 133.0f, (float)var8 + 161.0f, 138.0f, (float)var8 + 156.0f};
        var7.fill(Sdf2D.path(var10001, var10002, var19), -6668728);
        return SurfaceDecal.front(var7, 0.0f, MikuDesign.ty(28.016407f), MikuDesign.tz(2.1f), 5.04f, 4.33125f).depth(2.0f, 1.8f).maxAngle(72.0f).flatLight(1.0f).occlusion(0.3f);
    }

    private static float smooth(float var0, float var1, float var2) {
        float var3 = MikuDesign.clamp01((var2 - var0) / (var1 - var0));
        return var3 * var3 * (3.0f - 2.0f * var3);
    }

    private static float clamp01(float var0) {
        return var0 < 0.0f ? 0.0f : Math.min(var0, 1.0f);
    }

    static {
        float[][] var5 = new float[9][];
        float[] fArray = new float[6];
        float[] var14 = fArray;
        fArray[0] = -0.1f;
        var14[1] = 28.3f;
        var14[2] = -2.0f;
        var14[3] = 0.95f;
        var14[4] = 0.12f;
        var14[5] = 0.34f;
        var5[0] = var14;
        float[] fArray2 = new float[6];
        float[] var16 = fArray2;
        fArray2[0] = -1.0f;
        var16[1] = 28.75f;
        var16[2] = -25.0f;
        var16[3] = 0.95f;
        var16[4] = 0.28f;
        var16[5] = 0.3f;
        var5[1] = var16;
        float[] fArray3 = new float[6];
        float[] var18 = fArray3;
        fArray3[0] = -1.92f;
        var18[1] = 28.4f;
        var18[2] = -47.0f;
        var18[3] = 0.92f;
        var18[4] = 0.26f;
        var18[5] = 0.3f;
        var5[2] = var18;
        float[] fArray4 = new float[6];
        float[] var5x = fArray4;
        fArray4[0] = 0.9f;
        var5x[1] = 28.85f;
        var5x[2] = 24.0f;
        var5x[3] = 0.95f;
        var5x[4] = 0.28f;
        var5x[5] = 0.3f;
        var5[3] = var5x;
        float[] fArray5 = new float[6];
        float[] var22 = fArray5;
        fArray5[0] = 1.9f;
        var22[1] = 28.5f;
        var22[2] = 46.0f;
        var22[3] = 0.92f;
        var22[4] = 0.25f;
        var22[5] = 0.3f;
        var5[4] = var22;
        float[] fArray6 = new float[6];
        float[] var24 = fArray6;
        fArray6[0] = -0.55f;
        var24[1] = 28.5f;
        var24[2] = -13.0f;
        var24[3] = 0.45f;
        var24[4] = 0.16f;
        var24[5] = 0.44f;
        var5[5] = var24;
        float[] fArray7 = new float[6];
        float[] var26 = fArray7;
        fArray7[0] = 0.46f;
        var26[1] = 28.62f;
        var26[2] = 11.0f;
        var26[3] = 0.45f;
        var26[4] = 0.18f;
        var26[5] = 0.44f;
        var5[6] = var26;
        float[] fArray8 = new float[6];
        float[] var28 = fArray8;
        fArray8[0] = -1.46f;
        var28[1] = 28.65f;
        var28[2] = -36.0f;
        var28[3] = 0.42f;
        var28[4] = 0.2f;
        var28[5] = 0.42f;
        var5[7] = var28;
        float[] fArray9 = new float[6];
        float[] var30 = fArray9;
        fArray9[0] = 1.42f;
        var30[1] = 28.7f;
        var30[2] = 35.0f;
        var30[3] = 0.42f;
        var30[4] = 0.2f;
        var30[5] = 0.42f;
        var5[8] = var30;
        LOCKS = var5;
    }
}

