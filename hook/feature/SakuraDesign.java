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
public final class SakuraDesign
extends CharacterDefinition {
    private static final int SKIN = -9016;
    private static final int SKIN_SHADE = -1331558;
    private static final int SKIN_LINE = -5740452;
    private static final int HAIR_ROOT = -2592100;
    private static final int HAIR = -677434;
    private static final int HAIR_TIP = -12321;
    private static final int HAIR_SHADE = -3908470;
    private static final int HAIR_LINE = -6864801;
    private static final int SHINE = -855643406;
    private static final int WHITE = -263689;
    private static final int WHITE_SHADE = -2305053;
    private static final int SLEEVE_IN = -2502430;
    private static final int CLOTH_LINE = -7568744;
    private static final int RED = -2674372;
    private static final int RED_DARK = -5955793;
    private static final int RED_IN = -10087904;
    private static final int RED_LINE = -8514270;
    private static final int CORD = -1816774;
    private static final int GOLD = -1459895;
    private static final int BEAD = -2608577;
    private static final int STRAW = -1320012;
    private static final int SOLE = -10600144;
    private static final int PETAL = -8984;
    private static final int PETAL_CORE = -2078596;
    private static final float HAKAMA_TOP = 20.0f;
    private static final float HAKAMA_HEM = 2.5f;
    private static final float HS = 1.12f;
    private static final float DROP = 0.4f;
    private static final float HX = 0.0f;
    private static final float HY = 28.9f;
    private static final float HZ = -0.3f;
    private static final float PPU = 56.88889f;
    private static final float FACE_TOP = 29.95f;
    private static final float[] PLEATS;
    private static final float[][] LOCKS;
    private static final Sdf2D SLEEVE_SIDE;

    public SakuraDesign() {
        super("sakura", "Sakura");
    }

    @Override
    protected void define(CharacterRig var1) {
        var1.scale(1.0f).outline(-11914176, 0.12f).occlusion(0.3f).flatLight(0.55f);
        float[][] var2 = SakuraDesign.lockPaths();
        SurfacePaint var3 = SakuraDesign.hairPaint();
        SakuraDesign.head(var1, var2);
        SakuraDesign.hair(var1, var3);
        SakuraDesign.fringe(var1, var2);
        SakuraDesign.kanzashi(var1);
        SakuraDesign.body(var1);
        SakuraDesign.hakama(var1);
        SakuraDesign.arms(var1);
        SakuraDesign.legs(var1);
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
        Sdf3D var2 = SdfShapes.capsule(0.0f, 23.3f, -0.45f, 0.0f, 25.9f, -0.3f, 0.6f);
        float var3 = SakuraDesign.ty(25.82f);
        float var4 = SakuraDesign.tz(1.2f);
        SurfacePaint var5 = SurfacePaint.field((var2x, var3x, var4x) -> {
            float var5x = var3 + Math.max(0.0f, var4 - var4x) * 0.62f;
            float var6x = SakuraDesign.smooth(var5x + 0.05f, var5x - 0.2f, var3x) * (0.6f + 0.4f * SakuraDesign.smooth(23.4f, 24.8f, var3x));
            return ArgbColors.mix(-9016, -1331558, var6x);
        });
        Sdf3D var6 = SakuraDesign.up(SakuraDesign.headBase().intersect(SdfShapes.below(30.2f)));
        CharacterPartBuilder var10000 = var0.part(CharacterBone.HEAD).pivot(0.0f, 24.6f, -0.2f);
        float var10001 = 0.45f;
        Sdf3D[] var10002 = new Sdf3D[]{var6, var2};
        var10000.shape(SdfShapes.blend(var10001, var10002).paint(var5)).detail(2.0f).flatLight(1.0f).occlusion(0.22f).outline(-5740452, 0.1f).decal(SakuraDesign.face(var1));
    }

    private static float[][] lockPaths() {
        Sdf3D var0 = SakuraDesign.headBase();
        float[][] var1 = new float[LOCKS.length][];
        for (int var2 = 0; var2 < LOCKS.length; ++var2) {
            float[] var3 = LOCKS[var2];
            float[] var4 = SakuraDesign.onHead(36.0f, var3[2], -0.15f);
            float[] var5 = SakuraDesign.onHead(58.0f, var3[2], 0.05f);
            float var6 = var5[0] + (var3[0] - var5[0]) * 0.75f;
            float var7 = var3[1] + 0.68f;
            float var8 = var3[0] - Math.signum(var3[0]) * var3[4];
            float[] var10002 = new float[]{var4[0], var4[1], var4[2], var5[0], var5[1], var5[2], var6, var7, SakuraDesign.frontZ(var0, var6, var7) + var3[5], var8, var3[1], SakuraDesign.frontZ(var0, var8, var3[1]) + 0.22f, var3[3]};
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
            var3[var4] = SdfShapes.extrude(Sdf2D.polygon(SakuraDesign.blade(var5)), 5.0f, 0.0f);
            var2[var4] = SdfShapes.intersect(var7, var3[var4]);
        }
        Sdf3D var8 = SdfShapes.intersect(SdfShapes.blend(0.15f, var2), SdfShapes.union(var3));
        Sdf3D var9 = SakuraDesign.headBase();
        SurfacePaint var10 = SurfacePaint.field((var1x, var2x, var3x) -> {
            int var4x = ArgbColors.mix(ArgbColors.mix(-2592100, -677434, 0.45f), -677434, SakuraDesign.smooth(30.9f, 29.7f, var2x));
            var4x = ArgbColors.mix(var4x, -12321, 0.5f * SakuraDesign.smooth(29.5f, 28.5f, var2x));
            return ArgbColors.mix(var4x, -3908470, 0.6f * (1.0f - SakuraDesign.smooth(0.12f, 0.42f, var9.distance(var1x, var2x, var3x))));
        });
        var0.extra("fringe", CharacterBone.HEAD).shape(SakuraDesign.up(var8.paint(var10))).resolution(0.45f).detail(1.05f).flatLight(0.7f).occlusion(0.1f).outline(-6864801, 0.09f).decal(SakuraDesign.shine());
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
            float var21 = var1 * (1.0f - SakuraDesign.smooth(0.6f, 1.0f, var12));
            float var22 = var1 * (1.0f - (float)Math.pow(SakuraDesign.smooth(0.3f, 1.0f, var12), 0.75));
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
            var0.fill(Sdf2D.curve(140.0f + var6 * 2.0f, SakuraDesign.ringY(var6), 140.0f + var6 + var8, SakuraDesign.ringY((var6 + var8) * 0.5f), 140.0f + var8 * 2.0f, SakuraDesign.ringY(var8), 1.0f, var9), -855643406);
            var0.fill(Sdf2D.curve(140.0f + var8 * 2.0f, SakuraDesign.ringY(var8), 140.0f + var8 + var7, SakuraDesign.ringY((var8 + var7) * 0.5f), 140.0f + var7 * 2.0f, SakuraDesign.ringY(var7), var9, 1.0f), -855643406);
        }
        return SurfaceDecal.wrap(var0, 0.0f, SakuraDesign.ty(30.16f), SakuraDesign.tz(-0.3f), 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 140.0f, 0.6272f).radius(1.2f, 3.8f).offset(0.03f);
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
            var2.add(SakuraDesign.capLock(var6, 5.0f, 98.0f, 0.8f, 0.62f));
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
        float var17 = 0.25f;
        var10001 = new Sdf3D[]{SdfShapes.subtract(SdfShapes.blend(0.3f, var2.toArray(new Sdf3D[0])), var12).paint(var1), var13.paint(var1.occlusion(0.35f))};
        Sdf3D var14 = SdfShapes.blend(var17, var10001);
        var0.extra("hair", CharacterBone.HEAD).shape(SakuraDesign.up(var14)).detail(1.1f).flatLight(0.55f).occlusion(0.22f).outline(-6864801, 0.09f).decal(SakuraDesign.shine());
        var10000 = new float[]{-1.95f, 30.4f, 0.92f, 0.55f, -2.3f, 29.3f, 1.12f, 0.6f, -2.42f, 27.8f, 1.0f, 0.55f, -2.41f, 25.9f, 0.72f, 0.3f, -2.3f, 25.1f, 0.74f, 0.36f, -2.12f, 24.7f, 0.78f, 0.32f};
        TaperedTubeSdf3D var15 = SdfShapes.tube(var10000).flatten(1.8f, 1.0f, 0.0f, 0.3f);
        var10000 = new float[]{-2.2f, 30.2f, 0.1f, 0.58f, -2.52f, 28.9f, 0.24f, 0.62f, -2.58f, 27.3f, 0.32f, 0.55f, -2.41f, 25.9f, 0.56f, 0.3f, -2.28f, 25.0f, 0.54f, 0.36f, -2.2f, 24.35f, 0.56f, 0.32f};
        TaperedTubeSdf3D var7 = SdfShapes.tube(var10000).flatten(1.8f, 1.0f, 0.0f, 0.15f);
        Sdf3D var8 = SdfShapes.cylinder(-2.41f, 25.38f, 0.64f, -2.41f, 25.94f, 0.64f, 0.5f, 0.12f).color(-263689);
        Sdf3D[] var20 = new Sdf3D[2];
        int var10002 = 0;
        float var23 = 0.25f;
        Sdf3D[] var25 = new Sdf3D[]{var15, var7};
        var20[var10002] = SdfShapes.blend(var23, var25).paint(var1);
        var20[1] = var8;
        Sdf3D var9 = SdfShapes.union(var20);
        BoneMotionProfile var10 = new BoneMotionProfile().idle(1.5f, 0.0f, 0.6f, 0.35f).walk(3.0f, 0.0f, 1.0f, 2.0f).drag(8.0f).hang(0.6f).spring(60.0f, 8.0f).bend(1.4f);
        CharacterPartBuilder var11 = var0.extra("sidelock_r", CharacterBone.HEAD).pivot(SakuraDesign.tx(-2.3f), SakuraDesign.ty(30.0f), SakuraDesign.tz(0.6f)).shape(SakuraDesign.up(var9)).motion(var10).resolution(0.7f).detail(0.6f).flatLight(0.5f).occlusion(0.35f).outline(-6864801, 0.09f).decal(SakuraDesign.tieCord());
        var0.extra("sidelock_l", CharacterBone.HEAD).mirrorOf(var11);
        SakuraDesign.backHair(var0, var1);
    }

    private static SurfaceDecal tieCord() {
        PixelCanvas var0 = new PixelCanvas(40, 20);
        var0.fill(Sdf2D.box(20.0f, 10.0f, 20.0f, 2.8f, 0.0f), -1816774);
        var0.fill(Sdf2D.ellipse(20.0f, 10.0f, 4.0f, 5.0f), -1816774);
        return SurfaceDecal.wrap(var0, SakuraDesign.tx(-2.41f), SakuraDesign.ty(25.67f), SakuraDesign.tz(0.63f), 0.0f, 1.0f, 0.0f, -1.0f, 0.0f, 0.45f, 220.0f, 0.56f).radius(0.2f, 0.9f);
    }

    private static void backHair(CharacterRig var0, SurfacePaint var1) {
        ArrayList<TaperedTubeSdf3D> var2 = new ArrayList<TaperedTubeSdf3D>();
        ArrayList<TaperedTubeSdf3D> var3 = new ArrayList<TaperedTubeSdf3D>();
        for (int var4 = 0; var4 < 7; ++var4) {
            float var5 = ((float)var4 - 3.0f) / 3.0f;
            float var6 = 180.0f - var5 * 72.0f;
            float var7 = var4 % 2 == 0 ? 0.1f : 0.0f;
            float[] var8 = SakuraDesign.onHead(26.0f, var6, -0.02f);
            float[] var9 = SakuraDesign.onHead(60.0f, var6, 0.22f + var7);
            float[] var10 = SakuraDesign.onHead(88.0f, var6, 0.34f + var7);
            float[] var11 = SakuraDesign.onHead(114.0f, var6, 0.2f + var7);
            float var10001 = 0.78f;
            float var10002 = 0.72f;
            float[] var10003 = new float[]{var8[0], var8[1], var8[2], var9[0], var9[1], var9[2], var10[0], var10[1], var10[2], var11[0], var11[1], var11[2]};
            var2.add(SdfShapes.strand(var10001, var10002, var10003).flattenFrom(2.4f, 0.0f, 28.9f, -0.3f));
            float[] var12 = SakuraDesign.onHead(104.0f, var6, 0.32f);
            var10001 = 0.8f;
            var10002 = 0.64f;
            var10003 = new float[]{var12[0], var12[1], var12[2], var5 * 2.85f, 25.0f, -2.52f - 0.3f * (1.0f - var5 * var5), var5 * 1.02f, 21.9f, -2.8f};
            var3.add(SdfShapes.strand(var10001, var10002, var10003).flatten(2.0f, 0.0f, 0.0f, 1.0f));
        }
        ArrayList<TaperedTubeSdf3D> var13 = new ArrayList<TaperedTubeSdf3D>();
        float[] var10000 = new float[]{14.1f, 13.72f, 13.58f, 13.78f, 14.16f};
        float[] var14 = var10000;
        for (int var15 = 0; var15 < 5; ++var15) {
            float var17 = ((float)var15 - 2.0f) / 2.0f;
            float var29 = 0.8f;
            float var33 = 0.14f;
            float[] var36 = new float[]{var17 * 0.72f, 22.3f, -2.85f, var17 * 1.02f, 18.9f, -3.08f, var17 * 0.84f, 16.0f, -3.25f, var17 * 0.2f, var14[var15], -3.32f};
            var13.add(SdfShapes.strand(var29, var33, var36).flatten(1.6f, 0.0f, 0.0f, 1.0f));
        }
        Sdf3D[] var24 = new Sdf3D[]{SdfShapes.blend(0.1f, var2.toArray(new Sdf3D[0])), SdfShapes.blend(0.2f, var3.toArray(new Sdf3D[0])), SdfShapes.blend(0.15f, var13.toArray(new Sdf3D[0]))};
        Sdf3D var16 = SdfShapes.union(var24).paint(var1);
        Sdf3D var18 = SdfShapes.box(0.0f, 21.9f, -2.82f, 1.42f, 0.45f, 0.62f, 0.32f).color(-263689);
        Sdf3D var19 = SdfShapes.box(0.0f, 21.9f, -2.82f, 1.5f, 0.2f, 0.7f, 0.17f).color(-1816774);
        Sdf3D var20 = SdfShapes.ellipsoid(-1.4f, 22.2f, -3.5f, 1.2f, 0.66f, 0.38f).paint(SurfacePaint.radial(-2305053, -0.3f, 22.0f, -3.6f, 1.6f, -263689)).rotate(0.0f, 0.0f, -16.0f, -1.4f, 22.2f, -3.5f);
        Sdf3D var21 = SdfShapes.ellipsoid(0.0f, 22.0f, -3.58f, 0.48f, 0.52f, 0.36f).color(-1816774);
        float var25 = 0.44f;
        float var30 = 0.38f;
        float[] var34 = new float[]{-0.3f, 21.6f, -3.5f, -0.8f, 20.5f, -3.45f, -1.2f, 19.4f, -3.4f};
        Sdf3D var22 = SdfShapes.strand(var25, var30, var34).flatten(2.2f, 0.0f, 0.0f, 1.0f).color(-263689);
        Sdf3D[] var26 = new Sdf3D[]{var18, var19, var20, var20.mirrored(), var21, var22, var22.mirrored()};
        Sdf3D var23 = SdfShapes.union(var26);
        CharacterPartBuilder var27 = var0.extra("hair_back", CharacterBone.HEAD).pivot(0.0f, SakuraDesign.ty(29.0f), SakuraDesign.tz(-1.6f));
        Sdf3D[] var31 = new Sdf3D[]{var16, var23};
        var27.shape(SakuraDesign.up(SdfShapes.union(var31))).motion(BoneMotionProfile.hair().hang(0.0f).follow(CharacterBone.BODY, 0.85f).drag(12.0f)).detail(0.58f).flatLight(0.5f).occlusion(0.35f).outline(-6864801, 0.09f).decal(SakuraDesign.tailLines());
    }

    private static SurfaceDecal tailLines() {
        PixelCanvas var0 = new PixelCanvas(48, 108);
        float var1 = 12.0f;
        float var2 = 22.0f;
        int var3 = 1890076012;
        for (int var4 = 0; var4 < 4; ++var4) {
            float var5 = ((float)var4 - 1.5f) / 2.0f;
            float var6 = var5 * 0.72f;
            float var7 = var5 * 1.02f;
            float var8 = var5 * 0.84f;
            float var9 = var5 * 0.2f;
            float var10 = (var7 + var8) * 0.5f;
            float var11 = 14.6f + Math.abs(var5) * 0.4f;
            var0.fill(Sdf2D.curve(24.0f - var1 * var6, (var2 - 21.4f) * var1, 24.0f - var1 * var7, (var2 - 18.9f) * var1, 24.0f - var1 * var10, (var2 - 17.45f) * var1, 0.4f, 1.5f), var3);
            var0.fill(Sdf2D.curve(24.0f - var1 * var10, (var2 - 17.45f) * var1, 24.0f - var1 * var8, (var2 - 16.0f) * var1, 24.0f - var1 * var9, (var2 - var11) * var1, 1.5f, 0.3f), var3);
        }
        return SurfaceDecal.back(var0, 0.0f, SakuraDesign.ty(var2 - 54.0f / var1), SakuraDesign.tz(-3.4f), 4.48f, 10.08f).depth(0.9f, 1.0f).maxAngle(70.0f);
    }

    private static void kanzashi(CharacterRig var0) {
        Sdf3D[] var10000 = new Sdf3D[]{SakuraDesign.blossom(1.6f).rotate(-22.0f, 42.0f, 0.0f).move(2.08f, 30.9f, 1.18f), SakuraDesign.blossom(0.95f).rotate(-12.0f, 70.0f, 0.0f).move(2.66f, 30.02f, 0.12f)};
        Sdf3D var1 = SdfShapes.union(var10000);
        var0.extra("kanzashi", CharacterBone.HEAD).shape(SakuraDesign.up(var1)).resolution(0.42f).detail(0.5f).flatLight(0.4f).outline(-6669732, 0.08f);
        ArrayList<Sdf3D> var2 = new ArrayList<Sdf3D>();
        float[][] var12 = new float[2][];
        int var10002 = 0;
        float[] var10003 = new float[]{2.86f, 29.84f, 1.06f, 2.0f};
        var12[var10002] = var10003;
        var10002 = 1;
        var10003 = new float[]{3.2f, 29.74f, 0.52f, 3.0f};
        var12[var10002] = var10003;
        float[][] var3 = var12;
        for (int var4 = 0; var4 < var3.length; ++var4) {
            float var5 = var3[var4][0];
            float var6 = var3[var4][1];
            float var7 = var3[var4][2];
            int var8 = (int)var3[var4][3];
            float var9 = var6 - 0.55f * (float)var8;
            var2.add(SdfShapes.capsule(var5, var6 + 0.05f, var7, var5 + 0.03f, var9, var7 + 0.02f, 0.07f).color(-1459895));
            for (int var10 = 0; var10 < var8; ++var10) {
                float var11 = var10 == var8 - 1 ? 0.23f : 0.17f;
                var2.add(SdfShapes.sphere(var5 + 0.01f * (float)var10, var6 - 0.55f * (float)(var10 + 1), var7, var11).color((var10 + var4) % 2 == 0 ? -2608577 : -1459895));
            }
        }
        var0.extra("bira", CharacterBone.HEAD).pivot(SakuraDesign.tx(3.08f), SakuraDesign.ty(29.86f), SakuraDesign.tz(0.8f)).shape(SakuraDesign.up(SdfShapes.union(var2.toArray(new Sdf3D[0])))).motion(BoneMotionProfile.hair().hang(1.0f).spring(60.0f, 6.0f)).resolution(0.4f).detail(1.5f).flatLight(0.5f).outline(-8767416, 0.05f);
    }

    private static Sdf3D blossom(float var0) {
        ArrayList<Sdf3D> var1 = new ArrayList<Sdf3D>();
        for (int var2 = 0; var2 < 5; ++var2) {
            float var3 = 90.0f + 72.0f * (float)var2;
            double var4 = Math.toRadians(var3);
            float var6 = (float)Math.cos(var4);
            float var7 = (float)Math.sin(var4);
            float var8 = var6 * 0.47f * var0;
            float var9 = var7 * 0.47f * var0;
            Sdf3D var10 = SdfShapes.ellipsoid(var8, var9, 0.0f, 0.3f * var0, 0.44f * var0, 0.2f).rotate(0.0f, 0.0f, var3 - 90.0f, var8, var9, 0.0f);
            var1.add(SdfShapes.subtract(var10, SdfShapes.sphere(var6 * 0.93f * var0, var7 * 0.93f * var0, 0.0f, 0.12f * var0)));
        }
        Sdf3D var11 = SdfShapes.union(var1.toArray(new Sdf3D[0])).paint(SurfacePaint.radial(-2078596, 0.0f, 0.0f, 0.1f, 0.4f * var0, -8984));
        Sdf3D[] var10000 = new Sdf3D[]{var11, SdfShapes.sphere(0.0f, 0.0f, 0.16f, 0.15f * var0).color(-1459895)};
        return SdfShapes.union(var10000);
    }

    private static Sdf3D capLock(float var0, float var1, float var2, float var3, float var4) {
        float[] var5 = new float[15];
        for (int var6 = 0; var6 < 5; ++var6) {
            float[] var7 = SakuraDesign.onHead(var1 + (var2 - var1) * (float)var6 / 4.0f, var0, 0.02f);
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
            int var6 = ArgbColors.mix(-2592100, -677434, SakuraDesign.smooth(0.8f, 2.9f, var5));
            return ArgbColors.mix(var6, -12321, SakuraDesign.smooth(5.5f, 17.5f, var5) * 0.9f);
        });
    }

    private static void body(CharacterRig var0) {
        Sdf3D[] var10000 = new Sdf3D[]{SdfShapes.capsule(0.0f, 23.95f, -0.15f, -2.55f, 23.3f, -0.15f, 0.95f), SdfShapes.capsule(0.0f, 23.95f, -0.15f, 2.55f, 23.3f, -0.15f, 0.95f)};
        Sdf3D var1 = SdfShapes.union(var10000);
        Sdf3D var2 = SdfShapes.ellipsoid(0.0f, 21.7f, 0.05f, 2.55f, 2.1f, 1.7f);
        Sdf3D var3 = SdfShapes.ellipsoid(0.86f, 21.6f, 0.6f, 0.92f, 0.82f, 0.98f).mirrorX();
        Sdf3D var4 = SdfShapes.ellipsoid(0.0f, 19.6f, 0.0f, 2.15f, 1.7f, 1.5f);
        Sdf3D var5 = SdfShapes.ellipsoid(0.0f, 17.8f, 0.0f, 2.2f, 1.6f, 1.55f);
        float var15 = 1.0f;
        Sdf3D[] var10001 = new Sdf3D[4];
        var10001[0] = var1;
        int var10003 = 1;
        float var10004 = 0.8f;
        Sdf3D[] var10005 = new Sdf3D[]{var2, var3};
        var10001[var10003] = SdfShapes.blend(var10004, var10005);
        var10001[2] = var4;
        var10001[3] = var5;
        Sdf3D var6 = SdfShapes.blend(var15, var10001).intersect(SdfShapes.above(17.2f));
        float var16 = 0.42f;
        float var22 = 0.4f;
        float[] var10002 = new float[]{0.0f, 24.75f, -1.1f, -0.95f, 24.62f, -0.72f, -1.22f, 24.3f, 0.35f, -0.75f, 23.35f, 1.45f, 0.12f, 22.45f, 1.92f};
        TaperedTubeSdf3D var7 = SdfShapes.strand(var16, var22, var10002).flattenFrom(2.2f, 0.0f, 22.5f, -1.2f);
        float var17 = 0.42f;
        float var23 = 0.4f;
        var10002 = new float[]{0.0f, 24.75f, -1.1f, 0.95f, 24.62f, -0.72f, 1.22f, 24.3f, 0.35f, 0.75f, 23.35f, 1.5f, -0.3f, 22.15f, 2.02f, -1.35f, 20.6f, 1.88f};
        TaperedTubeSdf3D var8 = SdfShapes.strand(var17, var23, var10002).flattenFrom(2.2f, 0.0f, 22.5f, -1.2f);
        SurfacePaint var9 = SurfacePaint.field((var0x, var1x, var2x) -> {
            float var3x = SakuraDesign.smooth(0.5f, 1.3f, var2x) * SakuraDesign.smooth(19.9f, 20.5f, var1x) * (1.0f - SakuraDesign.smooth(20.6f, 21.1f, var1x));
            return ArgbColors.mix(-263689, -2305053, var3x * 0.55f);
        });
        CharacterPartBuilder var18 = var0.part(CharacterBone.BODY);
        float var24 = 0.14f;
        Sdf3D[] var28 = new Sdf3D[]{var6, var7, var8};
        var18.shape(SdfShapes.blend(var24, var28).paint(var9)).outline(-7568744, 0.1f).detail(0.85f).occlusion(0.2f).decal(SakuraDesign.collar());
        Sdf3D var10 = SdfShapes.subtract(SdfShapes.ellipsoid(0.0f, 19.2f, 0.0f, 2.82f, 2.6f, 2.32f).intersect(SdfShapes.box(0.0f, 19.2f, 0.0f, 3.2f, 0.3f, 2.7f, 0.12f)), SdfShapes.ellipsoid(0.0f, 19.2f, 0.0f, 2.4f, 2.6f, 1.9f));
        Sdf3D var11 = SdfShapes.ellipsoid(0.0f, 19.22f, 2.36f, 0.36f, 0.36f, 0.24f);
        Sdf3D var12 = SdfShapes.ellipsoid(-1.0f, 19.4f, 2.28f, 1.0f, 0.44f, 0.2f).rotate(0.0f, -8.0f, -12.0f, -1.0f, 19.4f, 2.28f);
        float var19 = 0.27f;
        float var25 = 0.22f;
        var10002 = new float[]{-0.12f, 19.0f, 2.38f, -0.42f, 18.1f, 2.44f, -0.64f, 17.2f, 2.48f};
        TaperedTubeSdf3D var13 = SdfShapes.strand(var19, var25, var10002).flatten(2.2f, 0.0f, 0.0f, 1.0f);
        var10000 = new Sdf3D[]{var11, var12, var12.mirrored(), var13, var13.mirrored()};
        Sdf3D var14 = SdfShapes.union(var10000);
        CharacterPartBuilder var21 = var0.extra("himo", CharacterBone.BODY);
        var10001 = new Sdf3D[]{var10, var14};
        var21.shape(SdfShapes.union(var10001).color(-263689)).resolution(0.6f).detail(0.36f).outline(-7568744, 0.09f);
    }

    private static SurfaceDecal collar() {
        PixelCanvas var0 = new PixelCanvas(92, 78);
        float[] var10000 = new float[]{0.0f, 25.1f};
        float[] var1 = SakuraDesign.uv(var10000);
        var10000 = new float[]{0.0f, 23.2f};
        float[] var2 = SakuraDesign.uv(var10000);
        float[] var10001 = new float[]{-0.95f, 25.1f, -0.84f, 24.49f, -0.41f, 23.59f, 0.02f, 23.16f, 0.4f, 23.59f, 0.84f, 24.49f, 0.95f, 25.1f};
        Sdf2D var7 = Sdf2D.polygon(SakuraDesign.uv(var10001));
        float var10002 = 0.0f;
        float var10003 = var1[1];
        float var10004 = 0.0f;
        float var10005 = var2[1];
        int[] var10006 = new int[]{-9016, -9016, -1331558};
        var0.fill(var7, ColorField2D.linear(var10002, var10003, var10004, var10005, var10006));
        float var8 = 6.6f;
        int var12 = -2674372;
        float[] var14 = new float[]{-0.9f, 25.1f, -0.84f, 24.49f, -0.41f, 23.59f, 0.05f, 23.12f};
        SakuraDesign.segments(var0, var8, var12, var14);
        float var9 = 6.6f;
        int var13 = -2674372;
        float[] var15 = new float[]{0.9f, 25.1f, 0.84f, 24.49f, 0.4f, 23.59f, -0.02f, 23.1f};
        SakuraDesign.segments(var0, var9, var13, var15);
        var10000 = new float[]{0.02f, 23.12f};
        float[] var3 = SakuraDesign.uv(var10000);
        var0.fill(Sdf2D.circle(var3[0], var3[1], 4.2f), -2674372);
        int var4 = 1484418696;
        float var10 = 1.8f;
        float[] var16 = new float[]{-1.65f, 24.25f, -1.6f, 24.11f, -1.13f, 23.16f, -0.6f, 22.6f};
        SakuraDesign.segments(var0, var10, var4, var16);
        float var11 = 1.8f;
        float[] var17 = new float[]{1.65f, 24.25f, 1.6f, 24.11f, 1.09f, 23.12f, 0.55f, 22.45f};
        SakuraDesign.segments(var0, var11, var4, var17);
        return SurfaceDecal.front(var0, 0.0f, 23.8f, 2.0f, 3.8333333f, 3.25f).depth(1.6f, 1.2f).maxAngle(86.0f).flatLight(0.9f).occlusion(0.4f);
    }

    private static float[] uv(float ... var0) {
        float[] var1 = new float[var0.length];
        for (int var2 = 0; var2 < var0.length; var2 += 2) {
            var1[var2] = (var0[var2] + 1.9166666f) * 24.0f;
            var1[var2 + 1] = (25.425f - var0[var2 + 1]) * 24.0f;
        }
        return var1;
    }

    private static void segments(PixelCanvas var0, float var1, int var2, float ... var3) {
        float[] var4 = SakuraDesign.uv(var3);
        int var5 = 0;
        while (var5 + 3 < var4.length) {
            var0.fill(Sdf2D.line(var4[var5], var4[var5 + 1], var4[var5 + 2], var4[var5 + 3], var1), var2);
            var5 += 2;
        }
    }

    private static void hakama(CharacterRig var0) {
        float[] var10000 = new float[]{-0.3f, 20.4f, 0.3f, 20.4f, 0.0f, 16.3f};
        Sdf3D var1 = SdfShapes.extrude(Sdf2D.polygon(var10000), 0.7f, 0.04f).rotate(0.0f, 90.0f, 0.0f).move(-2.95f, 0.0f, 0.0f).color(-263689);
        Sdf3D var2 = SdfShapes.subtract(SdfShapes.field(SakuraDesign::hakamaField, -5.1f, 2.25f, -3.9f, 2.1f, 20.25f, 3.9f).paint(SurfacePaint.field(SakuraDesign::hakamaColor)), var1);
        BoneMotionProfile var3 = new BoneMotionProfile().follow(CharacterBone.RIGHT_LEG, 0.62f, 13.0f).drag(3.0f).hang(0.5f).spring(80.0f, 11.0f).bend(1.6f).limit(24.0f);
        CharacterPartBuilder var4 = var0.extra("hakama_r", CharacterBone.BODY).pivot(-1.0f, 19.6f, 0.0f).shape(var2).motion(var3).resolution(1.1f).detail(0.42f).flatLight(0.35f).occlusion(0.2f).outline(-8514270, 0.12f);
        var4.decal(SakuraDesign.pleatLine(PLEATS[0])).decal(SakuraDesign.pleatLine(PLEATS[1]));
        var0.extra("hakama_l", CharacterBone.BODY).mirrorOf(var4);
    }

    private static SurfaceDecal pleatLine(float var0) {
        PixelCanvas var1 = new PixelCanvas(10, 96);
        Sdf2D var10001 = Sdf2D.taper(5.0f, 1.0f, 1.2f, 5.0f, 95.0f, 6.0f);
        float var10002 = 0.0f;
        float var10003 = 0.0f;
        float var10004 = 0.0f;
        float var10005 = 40.0f;
        int[] var10006 = new int[]{8262946, -1719790302};
        var1.fill(var10001, ColorField2D.linear(var10002, var10003, var10004, var10005, var10006));
        float var2 = (float)Math.sin(var0);
        float var3 = (float)Math.cos(var0);
        return SurfaceDecal.wrap(var1, -1.14f, 8.0f, 0.0f, 1.15f, 17.5f, 0.0f, -var2, 0.0f, var3, 3.6f, 11.0f);
    }

    private static float hakamaField(float var0, float var1, float var2) {
        float var3 = SakuraDesign.clamp01((20.0f - var1) / 17.5f);
        float var4 = (float)Math.pow(var3, 0.8);
        float var5 = -(0.35f + 1.15f * var3);
        float var6 = 2.05f + 1.15f * var4;
        float var7 = 1.9f + 1.5f * var4;
        float var8 = var0 - var5;
        float var9 = SakuraDesign.ellipse(var8, var2, var6, var7);
        float var10 = (float)Math.atan2(-var8, var2);
        float var11 = 0.05f + 0.17f * var3;
        for (float var15 : PLEATS) {
            var9 -= var11 * SakuraDesign.pleat(var10 - var15);
        }
        float var16 = Math.max(var1 - 20.0f, 2.5f - var1);
        return SakuraDesign.smax(var9, var16, 0.2f) * 0.9f;
    }

    private static int hakamaColor(float var0, float var1, float var2) {
        float var3 = SakuraDesign.clamp01((20.0f - var1) / 17.5f);
        float var4 = (float)Math.pow(var3, 0.8);
        float var5 = var0 + 0.35f + 1.15f * var3;
        if (var1 < 2.8f && SakuraDesign.ellipse(var5, var2, 2.05f + 1.15f * var4, 1.9f + 1.5f * var4) < -0.2f) {
            return -10087904;
        }
        float var6 = (float)Math.atan2(-var5, var2);
        float var7 = 0.0f;
        for (float var11 : PLEATS) {
            float var12 = var6 - var11;
            if (!(var12 >= 0.0f) || !(var12 < 0.16f)) continue;
            var7 = Math.max(var7, 1.0f - SakuraDesign.smooth(0.02f, 0.16f, var12));
        }
        int var13 = ArgbColors.mix(-2674372, -5955793, SakuraDesign.smooth(9.0f, 2.5f, var1) * 0.45f);
        return ArgbColors.mix(var13, -5955793, var7 * 0.35f * SakuraDesign.smooth(0.02f, 0.2f, var3));
    }

    private static float pleat(float var0) {
        if (!(var0 < -0.4f) && !(var0 > 0.07f)) {
            return var0 < 0.0f ? SakuraDesign.smooth(-0.4f, 0.0f, var0) : 1.0f - SakuraDesign.smooth(0.0f, 0.07f, var0);
        }
        return 0.0f;
    }

    private static float ellipse(float var0, float var1, float var2, float var3) {
        float var4 = var0 / var2;
        float var5 = var1 / var3;
        float var6 = (float)Math.sqrt(var4 * var4 + var5 * var5);
        float var7 = var0 / (var2 * var2);
        float var8 = var1 / (var3 * var3);
        float var9 = (float)Math.sqrt(var7 * var7 + var8 * var8);
        return var9 < 1.0E-6f ? -Math.min(var2, var3) : var6 * (var6 - 1.0f) / var9;
    }

    private static void arms(CharacterRig var0) {
        float var1 = -3.62f;
        float var2 = 23.0f;
        float var3 = -0.12f;
        float var4 = -4.05f;
        float var5 = 19.45f;
        float var6 = -0.3f;
        float var7 = -4.28f;
        float var8 = 15.95f;
        float var9 = 0.45f;
        float var10 = (float)Math.sqrt((var7 - var4) * (var7 - var4) + (var8 - var5) * (var8 - var5) + (var9 - var6) * (var9 - var6));
        float var11 = (var7 - var4) / var10;
        float var12 = (var8 - var5) / var10;
        float var13 = (var9 - var6) / var10;
        float var10000 = 0.35f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.cone(var1, var2, var3, 0.66f, var4, var5, var6, 0.58f), SdfShapes.cone(var4, var5, var6, 0.58f, var7, var8, var9, 0.66f)};
        Sdf3D var14 = SdfShapes.blend(var10000, var10001);
        Sdf3D var15 = SdfShapes.sphere(var7 + 0.3f * var11, var8 + 0.3f * var12, var9 + 0.3f * var13, 0.46f).color(-2502430);
        CharacterPartBuilder var16 = var0.part(CharacterBone.RIGHT_ARM).pivot(-3.5f, 23.25f, -0.1f).grip(-4.32f, 15.05f, 0.72f).shape(SdfShapes.subtract(0.08f, var14.color(-263689), var15)).resolution(0.7f).detail(0.7f).outline(-7568744, 0.1f).swing(0.55f).joint(var4, var5, var6, 0.6f);
        var0.part(CharacterBone.LEFT_ARM).mirrorOf(var16);
        Sdf3D var17 = SdfShapes.field((var0x, var1x, var2x) -> {
            float var3x = SLEEVE_SIDE.distance(var2x, var1x);
            float var4x = -4.12f - 0.085f * (23.8f - var1x);
            float var5x = 0.3f + 0.16f * SakuraDesign.clamp01((22.0f - var1x) / 8.0f) + 0.26f * SakuraDesign.smooth(0.0f, 1.3f, -var3x) + 0.07f * (float)Math.sin(var2x * 2.3f + 0.6f) * SakuraDesign.smooth(22.0f, 17.0f, var1x);
            return SakuraDesign.smax(var3x, Math.abs(var0x - var4x) - var5x, 0.28f);
        }, -6.1f, 13.2f, -3.9f, -3.3f, 24.7f, 1.6f).paint(SurfacePaint.vertical(-1053966, 13.5f, -263689, 19.0f));
        Sdf3D[] var21 = new Sdf3D[]{SdfShapes.sphere(-5.0f, 14.14f, -3.42f, 0.23f).color(-1816774), SdfShapes.cone(-5.0f, 13.98f, -3.44f, 0.15f, -5.01f, 13.58f, -3.46f, 0.27f).paint(SurfacePaint.vertical(-4705748, 13.3f, -1816774, 13.9f))};
        Sdf3D var18 = SdfShapes.union(var21);
        BoneMotionProfile var19 = BoneMotionProfile.cloth().hang(0.7f).drag(8.0f).spring(40.0f, 5.0f).bend(1.3f).limit(35.0f);
        CharacterPartBuilder var22 = var0.extra("sleeve_r", CharacterBone.RIGHT_ARM);
        var10001 = new Sdf3D[]{var17, var18};
        CharacterPartBuilder var20 = var22.shape(SdfShapes.union(var10001)).motion(var19).resolution(1.0f).detail(0.7f).outline(-7568744, 0.1f).decal(SakuraDesign.sleeveTrim());
        var0.extra("sleeve_l", CharacterBone.LEFT_ARM).mirrorOf(var20);
        SakuraDesign.hands(var0, var7, var8, var9);
    }

    private static void hands(CharacterRig var0, float var1, float var2, float var3) {
        float var4 = 4.3f;
        float var5 = 15.25f;
        float var6 = 0.64f;
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
        CharacterPartBuilder var8 = var0.extra("hand_l", CharacterBone.LEFT_ARM).shape(SdfShapes.blend(0.12f, var7.toArray(new Sdf3D[0])).color(-9016)).resolution(0.45f).detail(0.35f).flatLight(1.0f).occlusion(0.25f).outline(-5740452, 0.1f);
        var0.extra("hand_r", CharacterBone.RIGHT_ARM).mirrorOf(var8);
    }

    private static SurfaceDecal sleeveTrim() {
        PixelCanvas var0 = new PixelCanvas(64, 24);
        float var1 = 16.0f;
        float[] var10000 = new float[]{-0.3f, 14.75f, -1.2f, 14.3f, -2.25f, 14.05f, -2.85f, 14.25f, -3.12f, 14.8f};
        float[] var2 = var10000;
        int var3 = 0;
        while (var3 + 3 < var2.length) {
            var0.fill(Sdf2D.line((var2[var3] + 3.75f) * var1, (15.3f - var2[var3 + 1]) * var1, (var2[var3 + 2] + 3.75f) * var1, (15.3f - var2[var3 + 3]) * var1, 3.0f), -1816774);
            var3 += 2;
        }
        return SurfaceDecal.right(var0, -6.1f, 15.3f - 12.0f / var1, -3.75f + 32.0f / var1, 64.0f / var1, 24.0f / var1).depth(1.3f, 1.2f).maxAngle(48.0f);
    }

    private static void legs(CharacterRig var0) {
        float var1 = -1.3f;
        float var10000 = 0.35f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.sphere(var1, 1.62f, -0.12f, 0.52f), SdfShapes.ellipsoid(var1, 0.98f, -0.45f, 0.56f, 0.54f, 0.62f), SdfShapes.ellipsoid(var1, 0.84f, 0.55f, 0.6f, 0.43f, 1.22f), SdfShapes.ellipsoid(var1 + 0.33f, 0.76f, 1.57f, 0.25f, 0.31f, 0.43f), SdfShapes.ellipsoid(var1 - 0.18f, 0.73f, 1.47f, 0.37f, 0.29f, 0.46f)};
        Sdf3D var2 = SdfShapes.blend(var10000, var10001);
        Sdf3D var3 = SdfShapes.box(var1 + 0.1f, 0.8f, 1.97f, 0.05f, 0.4f, 0.55f, 0.02f).color(-2305053);
        Sdf3D var4 = SdfShapes.cone(var1, 1.7f, -0.1f, 0.5f, var1 + 0.02f, 3.3f, -0.08f, 0.54f);
        var10000 = 0.3f;
        var10001 = new Sdf3D[]{var2, var4};
        Sdf3D var5 = SdfShapes.subtract(SdfShapes.blend(var10000, var10001).color(-263689), var3);
        Sdf3D var6 = SdfShapes.box(var1, 0.22f, 0.52f, 0.74f, 0.22f, 1.64f, 0.2f).paint(SurfacePaint.field((var0x, var1x, var2x) -> var1x > 0.36f ? -1320012 : -10600144));
        Sdf3D[] var11 = new Sdf3D[3];
        int var10002 = 0;
        float var10003 = 0.2f;
        float var10004 = 0.2f;
        float[] var10005 = new float[]{var1 + 0.13f, 0.5f, 1.5f, var1 + 0.13f, 0.95f, 1.3f};
        var11[var10002] = SdfShapes.strand(var10003, var10004, var10005);
        var10002 = 1;
        var10003 = 0.2f;
        var10004 = 0.2f;
        var10005 = new float[]{var1 + 0.13f, 0.95f, 1.3f, var1 - 0.35f, 1.08f, 0.85f, var1 - 0.62f, 0.55f, 0.35f};
        var11[var10002] = SdfShapes.strand(var10003, var10004, var10005);
        var10002 = 2;
        var10003 = 0.2f;
        var10004 = 0.2f;
        var10005 = new float[]{var1 + 0.13f, 0.95f, 1.3f, var1 + 0.35f, 1.08f, 0.85f, var1 + 0.6f, 0.55f, 0.35f};
        var11[var10002] = SdfShapes.strand(var10003, var10004, var10005);
        Sdf3D var7 = SdfShapes.union(var11).color(-1816774);
        Sdf3D[] var12 = new Sdf3D[]{var5, var6, var7};
        Sdf3D var8 = SdfShapes.union(var12).rotate(0.0f, -7.0f, 0.0f, var1, 0.0f, -0.1f);
        CharacterPartBuilder var9 = var0.part(CharacterBone.RIGHT_LEG).pivot(var1, 14.2f, 0.0f).shape(var8).resolution(0.6f).detail(0.5f).flatLight(0.4f).outline(-10860992, 0.1f).swing(0.38f, 18.0f);
        var0.part(CharacterBone.LEFT_LEG).mirrorOf(var9);
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
        int[] var10006 = new int[]{-923920, -1};
        var1.fill(var4, ColorField2D.linear(var2, var10003, var2, var10005, var10006));
        var1.fill(Sdf2D.ellipse(var2, var3 - 34.0f, 34.0f, 22.0f).intersect(var4), -1713846054, 2.0f);
        Sdf2D var5 = Sdf2D.ellipse(var2, var3 + 3.5f, 20.5f, 27.5f);
        Sdf2D var6 = var5.intersect(var4);
        var10003 = var3 - 24.0f;
        var10005 = var3 + 30.0f;
        var10006 = new int[]{-11923926, -2996102, -19508};
        var1.fill(var6, ColorField2D.linear(var2, var10003, var2, var10005, var10006));
        var1.fill(var5.outline(2.6f).intersect(var4), -1337323990);
        var1.fill(Sdf2D.ellipse(var2, var3 + 1.5f, 9.0f, 12.5f), -14023146);
        var1.fill(Sdf2D.ellipse(var2, var3 + 17.0f, 12.0f, 6.5f).intersect(var6), -2130716956, 3.0f);
        var1.fill(Sdf2D.curve(var2 + 26.5f, var3 - 13.0f, var2 + 2.0f, var3 - 46.0f, var2 - 30.0f, var3 - 6.0f, 4.2f, 8.4f), -13757408);
        var1.fill(Sdf2D.taper(var2 - 25.0f, var3 - 9.0f, 6.0f, var2 - 35.0f, var3 - 6.0f, 1.4f), -13757408);
        var1.fill(Sdf2D.curve(var2 - 5.0f, var3 + 32.0f, var2 - 20.0f, var3 + 31.0f, var2 - 26.5f, var3 + 16.0f, 1.6f, 2.8f), -864407480);
        var1.fill(Sdf2D.curve(var2 + 15.0f, var3 - 38.0f, var2, var3 - 48.0f, var2 - 23.0f, var3 - 33.0f, 1.5f, 1.5f), 2009102984);
        var1.fill(Sdf2D.curve(var2 + 20.0f, var3 - 48.0f, var2 + 2.0f, var3 - 57.0f, var2 - 22.0f, var3 - 51.0f, 4.0f, 2.6f), -4892806);
        var1.fill(Sdf2D.ellipse(var2 - 4.0f, var3 + 46.0f, 21.0f, 9.0f), 1728023196, 10.0f);
        PixelCanvas var7 = new PixelCanvas(256, 220);
        int var8 = Math.round(SakuraDesign.py(28.0f)) - 70;
        ArrayList<Sdf2D> var9 = new ArrayList<Sdf2D>();
        float[][] var10 = var0;
        int var11 = var0.length;
        for (int var12 = 0; var12 < var11; ++var12) {
            float[] var13 = var10[var12];
            float[] var14 = SakuraDesign.blade(var13);
            for (int var15 = 0; var15 < var14.length; var15 += 2) {
                var14[var15] = SakuraDesign.px(var14[var15]);
                var14[var15 + 1] = SakuraDesign.py(var14[var15 + 1]) + 14.0f;
            }
            var9.add(Sdf2D.polygon(var14));
        }
        var9.add(Sdf2D.box(128.0f, SakuraDesign.py(29.75f), 130.0f, 14.222222f, 0.0f));
        var7.fill(Sdf2D.union(var9.toArray(new Sdf2D[0])), -1930845035, 3.0f);
        var7.draw(var1, 28, var8);
        var7.draw(var1.mirrored(), 140, var8);
        for (int var16 = -1; var16 <= 1; var16 += 2) {
            float var17 = 128.0f + (float)var16 * 60.0f;
            var7.fill(Sdf2D.ellipse(var17 - 7.5f, (float)var8 + 60.0f, 7.0f, 8.8f), -1);
            var7.fill(Sdf2D.circle(var17 + 8.0f, (float)var8 + 83.0f, 3.0f), -570425345);
            var7.fill(Sdf2D.circle(var17 - 12.0f, (float)var8 + 76.0f, 1.6f), -1426063361);
        }
        var7.fill(Sdf2D.line(130.0f, (float)var8 + 119.0f, 127.0f, (float)var8 + 124.5f, 2.4f), -1713795968);
        var7.fill(Sdf2D.curve(119.0f, (float)var8 + 157.0f, 128.0f, (float)var8 + 163.0f, 137.0f, (float)var8 + 157.0f, 2.6f, 2.6f), -4896678);
        return SurfaceDecal.front(var7, 0.0f, SakuraDesign.ty(28.016407f), SakuraDesign.tz(2.1f), 5.04f, 4.33125f).depth(2.0f, 1.8f).maxAngle(72.0f).flatLight(1.0f).occlusion(0.3f);
    }

    private static float smax(float var0, float var1, float var2) {
        float var3 = SakuraDesign.clamp01(0.5f - 0.5f * (var1 - var0) / var2);
        return var1 + (var0 - var1) * var3 + var2 * var3 * (1.0f - var3);
    }

    private static float smooth(float var0, float var1, float var2) {
        float var3 = SakuraDesign.clamp01((var2 - var0) / (var1 - var0));
        return var3 * var3 * (3.0f - 2.0f * var3);
    }

    private static float clamp01(float var0) {
        return var0 < 0.0f ? 0.0f : Math.min(var0, 1.0f);
    }

    static {
        float[] fArray = new float[3];
        float[] var5 = fArray;
        fArray[0] = 0.21f;
        var5[1] = 0.7f;
        var5[2] = 2.76f;
        PLEATS = var5;
        float[][] var7 = new float[9][];
        float[] fArray2 = new float[6];
        float[] var49 = fArray2;
        fArray2[0] = -0.08f;
        var49[1] = 28.38f;
        var49[2] = -2.0f;
        var49[3] = 0.95f;
        var49[4] = 0.12f;
        var49[5] = 0.34f;
        var7[0] = var49;
        float[] fArray3 = new float[6];
        float[] var51 = fArray3;
        fArray3[0] = -0.98f;
        var51[1] = 28.82f;
        var51[2] = -25.0f;
        var51[3] = 0.95f;
        var51[4] = 0.28f;
        var51[5] = 0.3f;
        var7[1] = var51;
        float[] fArray4 = new float[6];
        float[] var53 = fArray4;
        fArray4[0] = -1.92f;
        var53[1] = 28.5f;
        var53[2] = -47.0f;
        var53[3] = 0.92f;
        var53[4] = 0.26f;
        var53[5] = 0.3f;
        var7[2] = var53;
        float[] fArray5 = new float[6];
        float[] var55 = fArray5;
        fArray5[0] = 0.92f;
        var55[1] = 28.9f;
        var55[2] = 24.0f;
        var55[3] = 0.95f;
        var55[4] = 0.28f;
        var55[5] = 0.3f;
        var7[3] = var55;
        float[] fArray6 = new float[6];
        float[] var7x = fArray6;
        fArray6[0] = 1.9f;
        var7x[1] = 28.62f;
        var7x[2] = 46.0f;
        var7x[3] = 0.92f;
        var7x[4] = 0.25f;
        var7x[5] = 0.3f;
        var7[4] = var7x;
        float[] fArray7 = new float[6];
        float[] var59 = fArray7;
        fArray7[0] = -0.55f;
        var59[1] = 28.58f;
        var59[2] = -13.0f;
        var59[3] = 0.45f;
        var59[4] = 0.16f;
        var59[5] = 0.44f;
        var7[5] = var59;
        float[] fArray8 = new float[6];
        float[] var61 = fArray8;
        fArray8[0] = 0.46f;
        var61[1] = 28.7f;
        var61[2] = 11.0f;
        var61[3] = 0.45f;
        var61[4] = 0.18f;
        var61[5] = 0.44f;
        var7[6] = var61;
        float[] fArray9 = new float[6];
        float[] var63 = fArray9;
        fArray9[0] = -1.46f;
        var63[1] = 28.72f;
        var63[2] = -36.0f;
        var63[3] = 0.42f;
        var63[4] = 0.2f;
        var63[5] = 0.42f;
        var7[7] = var63;
        float[] fArray10 = new float[6];
        float[] var65 = fArray10;
        fArray10[0] = 1.42f;
        var65[1] = 28.8f;
        var65[2] = 35.0f;
        var65[3] = 0.42f;
        var65[4] = 0.2f;
        var65[5] = 0.42f;
        var7[8] = var65;
        LOCKS = var7;
        float[] fArray11 = new float[22];
        float[] var9 = fArray11;
        fArray11[0] = 0.85f;
        var9[1] = 23.95f;
        var9[2] = 0.62f;
        var9[3] = 19.0f;
        var9[4] = 0.12f;
        var9[5] = 15.7f;
        var9[6] = -0.2f;
        var9[7] = 14.7f;
        var9[8] = -1.2f;
        var9[9] = 14.25f;
        var9[10] = -2.25f;
        var9[11] = 14.0f;
        var9[12] = -2.9f;
        var9[13] = 14.2f;
        var9[14] = -3.2f;
        var9[15] = 14.8f;
        var9[16] = -3.25f;
        var9[17] = 16.0f;
        var9[18] = -2.2f;
        var9[19] = 22.4f;
        var9[20] = -1.0f;
        var9[21] = 24.2f;
        SLEEVE_SIDE = Sdf2D.polygon(var9).inflate(0.3f);
    }
}

