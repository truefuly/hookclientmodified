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
import hook.feature.Vec3;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class LunaDesign
extends CharacterDefinition {
    private static final int SKIN = -6958;
    private static final int SKIN_SHADE = -1331558;
    private static final int SKIN_LINE = -5737368;
    private static final int HAIR = -1912065;
    private static final int HAIR_MID = -4940554;
    private static final int HAIR_TIP = -7706904;
    private static final int HAIR_ROOT = -6519080;
    private static final int HAIR_LIGHT = -1647873;
    private static final int HAIR_LINE = -10794346;
    private static final int NAVY = -14603682;
    private static final int NAVY_DARK = -15460295;
    private static final int NAVY_LINE = -15921370;
    private static final int BODICE = -13945223;
    private static final int TIER = -12693598;
    private static final int LINING = -7571752;
    private static final int RIBBON = -4544530;
    private static final int PETTICOAT = -4611086;
    private static final int LACE = -1036;
    private static final int GOLD = -1326758;
    private static final int GOLD_DARK = -5011921;
    private static final int GOLD_PALE = -2868;
    private static final int GOLD_LINE = -8760296;
    private static final int TIGHTS = -12959880;
    private static final int TIGHTS_SHEEN = -10656082;
    private static final int BOOT = -7710146;
    private static final int BOOT_DARK = -9550799;
    private static final int BOOT_CUFF = -6395064;
    private static final int LEATHER = -8762574;
    private static final int LEATHER_DARK = -11653344;
    private static final int MOON = -4688;
    private static final float HEAD_K = 1.05f;
    private static final float NECK_Y = 24.7f;
    private static final float NECK_Z = -0.1f;
    private static final float AXIS_Z = -0.3f;
    private static final float HAT_TILT_X = -8.0f;
    private static final float HAT_TILT_Z = 4.0f;
    private static final Vec3 HAT_CENTRE = new Vec3(0.0f, 29.15f, -0.25f);
    private static final Sdf2D BRIM = Sdf2D.taper(2.9f, 29.25f, 0.82f, 5.72f, 29.0f, 0.5f);
    private static final float BRIM_WAVE = 0.3f;
    private static final float RIM_R = 5.86f;
    private static final Vec3 ELBOW = new Vec3(-3.72f, 18.4f, -0.05f);
    private static final float ELBOW_PITCH = -12.0f;
    private static final float ELBOW_ROLL = 3.0f;
    private static final float FACE_PPU = 41.48148f;
    private static final float FACE_LEFT = -2.3142858f;
    private static final float FACE_TOP = 28.810715f;
    private static final float[] CAPE_Y;
    private static final float[] CAPE_RX;
    private static final float[] CAPE_RZ;
    private static final float[] CAPE_CZ;
    private static final float[] CAPE_WRAP;
    private static final int CAPE_STEPS = 96;
    private static final float[] CAPE_LUT;
    private static final float[] CURTAIN_Y;
    private static final float[] CURTAIN_RX;
    private static final float[] CURTAIN_RZ;
    private static final float[] CURTAIN_CZ;

    public LunaDesign() {
        super("luna", "Luna");
    }

    @Override
    protected void define(CharacterRig var1) {
        var1.scale(1.0f).outline(-15396047, 0.15f).occlusion(0.35f);
        PixelCanvas var16 = new PixelCanvas(128, 12);
        LunaDesign.paintBand(var16);
        Sdf3D var18 = LunaDesign.faceShape();
        Sdf3D var20 = SdfShapes.ellipsoid(0.0f, 27.55f, -0.35f, 2.82f, 2.94f, 2.88f);
        Sdf3D var36 = SdfShapes.plane(0.0f, 24.6f, -2.7f, 0.0f, -1.0f, 0.86f);
        Sdf3D var21 = var20.intersect(var36);
        List<CurvedShape> var23 = LunaDesign.bangs(var18);
        LunaDesign.head(var1, var18, var23);
        LunaDesign.hair(var1, var21, var23);
        LunaDesign.hat(var1);
        LunaDesign.body(var1);
        LunaDesign.cape(var1);
        LunaDesign.skirt(var1, var16);
        LunaDesign.arms(var1, var16);
        LunaDesign.legs(var1);
    }

    public static Sdf3D faceShape() {
        Sdf3D var0 = SdfShapes.ellipsoid(0.0f, 27.5f, -0.3f, 2.38f, 2.5f, 2.42f);
        Sdf3D var1 = SdfShapes.ellipsoid(0.0f, 26.3f, 0.3f, 1.84f, 1.32f, 1.84f);
        Sdf3D var2 = SdfShapes.cone(0.0f, 25.75f, 0.4f, 1.28f, 0.0f, 24.74f, 1.02f, 0.34f);
        float var10000 = 1.0f;
        Sdf3D[] var10001 = new Sdf3D[]{var0, var1, var2};
        return SdfShapes.blend(var10000, var10001);
    }

    public static void head(CharacterRig var0, Sdf3D var1, List<CurvedShape> var2) {
        var0.part(CharacterBone.HEAD).pivot(0.0f, 24.7f, -0.1f).shape(LunaDesign.hs(var1.paint(SurfacePaint.of(-6958)))).resolution(0.9f).detail(1.5f).flatLight(1.0f).occlusion(0.2f).outline(-5737368, 0.08f).decal(LunaDesign.faceDecal(var2));
    }

    public static Sdf3D hs(Sdf3D var0) {
        return var0.scale(1.05f, 0.0f, 24.7f, -0.1f);
    }

    public static Vec3 hp(float var0, float var1, float var2) {
        return new Vec3(var0 * 1.05f, 24.7f + (var1 - 24.7f) * 1.05f, -0.1f + (var2 - -0.1f) * 1.05f);
    }

    public static CharacterPartBuilder at(CharacterPartBuilder var0, Vec3 var1) {
        return var0.pivot(var1.x(), var1.y(), var1.z());
    }

    public static List<CurvedShape> bangs(Sdf3D var0) {
        float[][] var10000 = new float[7][];
        int var10002 = 0;
        float[] var10003 = new float[]{-50.0f, 26.5f, 0.75f, 0.3f, 0.0f};
        var10000[var10002] = var10003;
        var10002 = 1;
        var10003 = new float[]{-24.0f, 27.6f, 0.72f, 0.25f, 0.0f};
        var10000[var10002] = var10003;
        var10002 = 2;
        var10003 = new float[]{4.0f, 26.9f, 0.8f, -0.2f, 0.0f};
        var10000[var10002] = var10003;
        var10002 = 3;
        var10003 = new float[]{27.0f, 27.8f, 0.7f, -0.25f, 0.0f};
        var10000[var10002] = var10003;
        var10002 = 4;
        var10003 = new float[]{52.0f, 26.62f, 0.75f, -0.3f, 0.0f};
        var10000[var10002] = var10003;
        var10002 = 5;
        var10003 = new float[]{-37.0f, 27.2f, 0.36f, 0.2f, 1.0f};
        var10000[var10002] = var10003;
        var10002 = 6;
        var10003 = new float[]{40.0f, 27.02f, 0.36f, -0.2f, 1.0f};
        var10000[var10002] = var10003;
        float[][] var1 = var10000;
        ArrayList<CurvedShape> var2 = new ArrayList<CurvedShape>();
        float[][] var3 = var1;
        int var4 = var1.length;
        for (int var5 = 0; var5 < var4; ++var5) {
            float[] var6 = var3[var5];
            float[] var7 = new float[10];
            float[] var8 = new float[10];
            for (int var9 = 0; var9 < 10; ++var9) {
                var7[var9] = var6[1] - 0.3f + (29.6f - var6[1] + 0.3f) * (float)var9 / 9.0f;
                var8[var9] = LunaDesign.surfaceR(var0, var6[0], var7[var9]);
            }
            var2.add(var6[4] > 0.0f ? new CurvedShape(var6[0], 29.4f, var6[1], var6[2], var6[3], 0.7f, 0.75f, 0.1f, 0.3f, 0.2f, var7, var8) : new CurvedShape(var6[0], 29.4f, var6[1], var6[2], var6[3], 0.5f, 0.45f, 0.05f, 0.4f, 0.22f, var7, var8));
        }
        return var2;
    }

    public static float interp(float[] var0, float[] var1, float var2) {
        int var3 = var0.length;
        if (var2 <= var0[0]) {
            return var1[0];
        }
        if (var2 >= var0[var3 - 1]) {
            return var1[var3 - 1];
        }
        int var4 = 0;
        while (var2 > var0[var4 + 1]) {
            ++var4;
        }
        return var1[var4] + (var1[var4 + 1] - var1[var4]) * (var2 - var0[var4]) / (var0[var4 + 1] - var0[var4]);
    }

    public static float surfaceR(Sdf3D var0, float var1, float var2) {
        float var9;
        double var3 = Math.toRadians(var1);
        float var5 = (float)Math.sin(var3);
        float var6 = (float)Math.cos(var3);
        float var7 = 4.5f;
        for (int var8 = 0; var8 < 80 && !(Math.abs(var9 = var0.distance(var5 * var7, var2, -0.3f + var6 * var7)) < 0.001f); ++var8) {
            var7 -= var9;
        }
        return var7;
    }

    public static void hair(CharacterRig var0, Sdf3D var1, List<CurvedShape> var2) {
        ArrayList<Sdf3D> var3 = new ArrayList<Sdf3D>();
        var3.add(var1);
        for (int var4 = -1; var4 <= 1; var4 += 2) {
            float var10001 = 0.74f;
            float var10002 = 0.24f;
            float[] var10003 = new float[]{(float)var4 * 2.35f, 29.2f, -0.9f, (float)var4 * 3.04f, 27.6f, -0.72f, (float)var4 * 3.02f, 26.0f, -0.5f, (float)var4 * 2.78f, 24.5f, -0.35f};
            var3.add(SdfShapes.strand(var10001, var10002, var10003).flattenFrom(1.5f, 0.0f, 26.8f, -0.6f));
        }
        SurfacePaint var21 = SurfacePaint.field((var0x, var1x, var2x) -> LunaDesign.shine(ArgbColors.mix(-4940554, -1912065, LunaDesign.clamp01((var1x - 24.6f) / 3.6f)), var0x, var1x, var2x, 28.1f, 0.26f, 7, 0.3f));
        var0.extra("hair", CharacterBone.HEAD).shape(LunaDesign.hs(SdfShapes.union(var3.toArray(new Sdf3D[0])).paint(var21))).resolution(1.3f).detail(0.6f).flatLight(0.4f).noOutline();
        ArrayList<Sdf3D> var5 = new ArrayList<Sdf3D>();
        for (CurvedShape var7 : var2) {
            var5.add(var7.shape());
        }
        SurfacePaint var22 = SurfacePaint.field((var0x, var1x, var2x) -> LunaDesign.shine(ArgbColors.mix(-1647873, -6519080, LunaDesign.clamp01((var1x - 27.1f) / 1.9f)), var0x, var1x, var2x, 28.45f, 0.14f, 11, 0.9f));
        var0.extra("bangs", CharacterBone.HEAD).shape(LunaDesign.hs(SdfShapes.union(var5.toArray(new Sdf3D[0])).paint(var22))).resolution(0.45f).detail(1.3f).flatLight(0.7f).occlusion(0.15f).outline(-10794346, 0.08f);
        float var33 = -72.0f;
        float var34 = 29.4f;
        float var10004 = 24.3f;
        float var10005 = 0.62f;
        float var10006 = 0.9f;
        float var10007 = 0.0f;
        float var10008 = 0.0f;
        float var10009 = 0.0f;
        float var10010 = 0.45f;
        float var10011 = 0.3f;
        float[] var10012 = new float[]{24.0f, 25.0f, 26.2f, 27.6f, 28.8f, 29.6f};
        float[] var10013 = new float[]{2.26f, 2.54f, 2.72f, 2.7f, 2.42f, 2.0f};
        CurvedShape var23 = new CurvedShape(var33, var34, var10004, var10005, var10006, var10007, var10008, var10009, var10010, var10011, var10012, var10013);
        SurfacePaint var8 = SurfacePaint.field((var0x, var1x, var2x) -> var1x > 27.2f ? ArgbColors.mix(-1647873, -6519080, LunaDesign.clamp01((var1x - 27.2f) / 1.8f)) : ArgbColors.mix(-4940554, -1647873, LunaDesign.clamp01((var1x - 24.3f) / 2.9f)));
        var0.extra("locks", CharacterBone.HEAD).shape(LunaDesign.hs(var23.shape().mirrorX().paint(var8))).resolution(0.45f).detail(0.7f).flatLight(0.5f).occlusion(0.3f).outline(-10794346, 0.08f);
        ArrayList<Sdf3D> var9 = new ArrayList<Sdf3D>();
        ArrayList<Sdf3D> var10 = new ArrayList<Sdf3D>();
        for (int var11 = 0; var11 < 5; ++var11) {
            var9.add(LunaDesign.backLock(118.0f + 31.0f * (float)var11, -0.22f, 18.2f + (float)(var11 % 2) * 0.35f, 1.25f, 0.5f, 2.6f, (float)var11 * 1.7f));
        }
        for (int var24 = 0; var24 < 7; ++var24) {
            float var12 = (float)var24 / 6.0f;
            float var13 = 16.9f + 1.4f * (float)Math.pow(Math.abs(var12 - 0.5f) * 2.0f, 1.5) + (float)(var24 % 3) * 0.3f;
            var10.add(LunaDesign.backLock(116.0f + 128.0f * var12, var24 % 2 == 0 ? 0.14f : 0.06f, var13, 0.66f, 0.12f, 1.7f, (float)var24 * 2.3f));
        }
        Sdf3D var25 = SdfShapes.union(var9.toArray(new Sdf3D[0])).paint(SurfacePaint.vertical(-9152312, 18.0f, -5993238, 26.0f));
        Sdf3D var26 = SdfShapes.union(var10.toArray(new Sdf3D[0])).paint(SurfacePaint.field((var0x, var1x, var2x) -> LunaDesign.shine(LunaDesign.backHair(var1x), var0x, var1x, var2x, 27.25f, 0.3f, 6, 1.1f)));
        CharacterPartBuilder var10000 = LunaDesign.at(var0.extra("hair_back", CharacterBone.HEAD), LunaDesign.hp(0.0f, 28.2f, -1.6f));
        Sdf3D[] var32 = new Sdf3D[]{var25, var26};
        var10000.shape(LunaDesign.hs(SdfShapes.union(var32))).motion(BoneMotionProfile.hair().hang(0.8f).drag(18.0f)).resolution(1.15f).detail(0.85f).flatLight(0.35f).outline(-10794346, 0.1f);
        float[] var30 = new float[]{-2.3f, 29.0f, -0.2f, 0.52f, -2.8f, 27.4f, -0.05f, 0.58f, -2.95f, 25.6f, 0.25f, 0.56f, -2.8f, 23.8f, 1.0f, 0.5f, -2.66f, 22.2f, 1.7f, 0.46f, -2.62f, 20.8f, 2.02f, 0.42f};
        TaperedTubeSdf3D var27 = SdfShapes.tube(var30).flattenFrom(1.45f, 0.0f, 22.0f, -0.5f);
        int var14 = 20;
        float[] var15 = new float[var14 * 4];
        for (int var16 = 0; var16 < var14; ++var16) {
            float var17 = (float)var16 / ((float)var14 - 1.0f);
            double var18 = Math.PI + (double)var17 * Math.PI * 5.0;
            float var20 = 0.8f - 0.22f * var17;
            var15[var16 * 4] = -1.82f + var20 * (float)Math.cos(var18);
            var15[var16 * 4 + 1] = 20.8f - 3.3f * var17;
            var15[var16 * 4 + 2] = 2.02f + var20 * (float)Math.sin(var18);
            var15[var16 * 4 + 3] = 0.42f - 0.18f * var17;
        }
        Sdf3D[] var31 = new Sdf3D[]{var27, SdfShapes.tube(var15)};
        Sdf3D var28 = SdfShapes.union(var31).paint(SurfacePaint.vertical(-7706904, 17.4f, -1912065, 25.0f));
        CharacterPartBuilder var29 = LunaDesign.at(var0.extra("curl_r", CharacterBone.HEAD), LunaDesign.hp(-2.35f, 28.7f, -0.15f)).shape(LunaDesign.hs(var28)).resolution(0.9f).motion(BoneMotionProfile.hair().drag(4.0f).walk(3.0f, 0.0f, 3.5f, 2.0f).hang(0.7f).turn(10.0f, 0.0f).bend(1.4f)).detail(0.9f).flatLight(0.4f).outline(-10794346, 0.1f);
        var0.extra("curl_l", CharacterBone.HEAD).mirrorOf(var29);
    }

    public static int backHair(float var0) {
        if (var0 > 26.2f) {
            return ArgbColors.mix(-1912065, -6519080, LunaDesign.clamp01((var0 - 26.2f) / 1.9f));
        }
        return var0 > 22.0f ? ArgbColors.mix(-4940554, -1912065, LunaDesign.clamp01((var0 - 22.0f) / 4.2f)) : ArgbColors.mix(-7706904, -4940554, LunaDesign.clamp01((var0 - 17.2f) / 4.8f));
    }

    public static int shine(int var0, float var1, float var2, float var3, float var4, float var5, int var6, float var7) {
        float var8 = (float)Math.atan2(var1, var3 - -0.3f);
        float var9 = Math.abs(var2 - var4 + 0.25f * (1.0f - (float)Math.cos(var8)));
        if (var9 > var5) {
            return var0;
        }
        float var10 = (float)Math.sin(var8 * (float)var6 + var7) + 0.2f;
        if (var10 <= 0.0f) {
            return var0;
        }
        float var11 = (1.0f - var9 / var5) * Math.min(1.0f, var10 * 3.0f) * 0.8f;
        return ArgbColors.mix(var0, ArgbColors.mix(var0, -1, 0.5f), Math.min(1.0f, var11 * 1.6f));
    }

    public static Sdf3D backLock(float var0, float var1, float var2, float var3, float var4, float var5, float var6) {
        double var7 = Math.toRadians(var0);
        float var9 = (float)Math.sin(var7);
        float var10 = (float)Math.cos(var7);
        float[] var10000 = new float[]{29.3f, 27.6f, 25.3f, 23.0f, 20.8f, 18.9f, var2};
        float[] var11 = var10000;
        float[] var12 = new float[var11.length * 3];
        for (int var13 = 0; var13 < var11.length; ++var13) {
            float var14 = var11[var13];
            float var15 = LunaDesign.spline(CURTAIN_Y, CURTAIN_RX, var14) + var1;
            float var16 = LunaDesign.spline(CURTAIN_Y, CURTAIN_RZ, var14) + var1;
            float var17 = LunaDesign.spline(CURTAIN_Y, CURTAIN_CZ, var14);
            float var18 = 0.32f * LunaDesign.clamp01((26.5f - var14) / 5.0f) * (float)Math.sin(var14 * 0.95f + var6);
            float var19 = var13 == var11.length - 1 ? 0.16f : 0.0f;
            var12[var13 * 3] = (var15 + var19) * var9 + var18 * var10;
            var12[var13 * 3 + 1] = var14;
            var12[var13 * 3 + 2] = var17 + (var16 + var19) * var10 - var18 * var9;
        }
        return SdfShapes.strand(var3, var4, var12).flatten(var5, var9, 0.0f, var10);
    }

    public static void hat(CharacterRig var0) {
        SurfacePaint var1 = SurfacePaint.vertical(-15460295, 28.6f, -14603682, 31.0f);
        Sdf3D var2 = SdfShapes.field(LunaDesign::brimDistance, -6.1f, 28.1f, -6.35f, 6.1f, 30.1f, 5.85f).paint(var1);
        var0.extra("brim", CharacterBone.HEAD).shape(LunaDesign.worn(var2)).resolution(1.2f).detail(0.65f).outline(-15921370, 0.12f);
        Sdf3D var3 = SdfShapes.field(LunaDesign::rimDistance, -6.2f, 28.35f, -6.45f, 6.2f, 29.65f, 5.95f).paint(SurfacePaint.of(-1326758).grain(0.6f, 0.08f));
        Sdf3D var4 = SdfShapes.cone(0.0f, 28.7f, -0.25f, 3.43f, 0.0f, 31.2f, -0.3f, 3.08f).intersect(SdfShapes.box(0.0f, 29.95f, -0.27f, 3.6f, 0.5f, 3.6f)).subtract(SdfShapes.cone(0.0f, 28.7f, -0.25f, 2.75f, 0.0f, 31.2f, -0.3f, 2.4f)).paint(SurfacePaint.vertical(-6650664, 29.45f, -4544530, 30.3f));
        CharacterPartBuilder var10000 = var0.extra("hat_trim", CharacterBone.HEAD);
        Sdf3D[] var10001 = new Sdf3D[]{var3, var4};
        var10000.shape(LunaDesign.worn(SdfShapes.union(var10001))).resolution(0.95f).detail(0.35f).noOutline();
        Sdf3D var5 = SdfShapes.cone(0.0f, 28.7f, -0.25f, 3.2f, 0.0f, 31.2f, -0.3f, 2.85f).intersect(SdfShapes.box(0.0f, 29.6f, -0.27f, 3.5f, 0.75f, 3.5f));
        var0.extra("hat", CharacterBone.HEAD).shape(LunaDesign.worn(var5.paint(var1))).resolution(1.2f).detail(0.3f).outline(-15921370, 0.15f);
        var0.extra("hat_pin", CharacterBone.HEAD).shape(LunaDesign.worn(LunaDesign.pin())).resolution(0.4f).detail(0.9f).outline(-8760296, 0.06f);
        float[] var12 = new float[]{0.0f, 30.2f, -0.29f, 2.8f, 0.0f, 32.2f, -0.42f, 2.24f, 0.0f, 34.1f, -0.74f, 1.56f, 0.04f, 35.3f, -1.42f, 1.02f, 0.14f, 36.0f, -2.45f, 0.64f, 0.3f, 36.15f, -3.65f, 0.36f};
        Sdf3D var6 = SdfShapes.tube(var12).intersect(SdfShapes.above(30.15f)).subtract(0.25f, SdfShapes.ellipsoid(0.0f, 34.45f, -2.3f, 0.9f, 0.35f, 0.45f)).paint(SurfacePaint.vertical(-14603682, 30.0f, -14011792, 37.0f));
        Vec3 var7 = LunaDesign.hatPoint(0.0f, 30.4f, -0.3f);
        CharacterPartBuilder var8 = LunaDesign.at(var0.extra("hat_tip", var0.part(CharacterBone.HEAD)), LunaDesign.hp(var7.x(), var7.y(), var7.z())).shape(LunaDesign.worn(var6)).resolution(1.25f).motion(new BoneMotionProfile().idle(2.0f, 0.0f, 2.0f, 0.3f).walk(6.0f, 0.0f, 4.0f, 2.0f).drag(7.0f).hang(0.25f).turn(10.0f, 0.0f).spring(38.0f, 4.5f).bend(1.7f).limit(35.0f)).detail(0.75f).outline(-15921370, 0.15f);
        Vec3 var9 = LunaDesign.hatPoint(0.28f, 35.9f, -3.5f);
        float var13 = 0.08f;
        float var15 = 0.08f;
        float[] var10002 = new float[]{var9.x(), var9.y(), var9.z(), var9.x(), var9.y() - 0.8f, var9.z()};
        Sdf3D var10 = SdfShapes.strand(var13, var15, var10002).color(-5011921);
        Sdf3D var11 = SdfShapes.extrude(Sdf2D.star(0.0f, 0.0f, 0.62f, 0.27f, 5).rotate(180.0f, 0.0f, 0.0f), 0.14f, 0.05f).rotate(0.0f, 90.0f, 0.0f).move(var9.x(), var9.y() - 1.3f, var9.z()).paint(SurfacePaint.of(-9872).glow());
        var10000 = LunaDesign.at(var0.extra("hat_charm", var8), LunaDesign.hp(var9.x(), var9.y(), var9.z()));
        var10001 = new Sdf3D[]{var10, var11};
        var10000.shape(LunaDesign.hs(SdfShapes.union(var10001))).motion(BoneMotionProfile.hair().hang(0.9f).spring(60.0f, 6.0f)).resolution(0.45f).detail(1.2f).outline(-8760296, 0.05f);
    }

    public static float brimDistance(float var0, float var1, float var2) {
        float var3 = var2 + 0.25f;
        float var4 = (float)Math.sqrt(var0 * var0 + var3 * var3);
        return BRIM.distance(var4, var1 - LunaDesign.wave(var0, var3, var4, LunaDesign.clamp01((var4 - 2.9f) / 2.9f)));
    }

    public static float rimDistance(float var0, float var1, float var2) {
        float var3 = var2 + 0.25f;
        float var4 = (float)Math.sqrt(var0 * var0 + var3 * var3);
        float var5 = var4 - 5.86f;
        float var6 = var1 - 29.0f - LunaDesign.wave(var0, var3, var4, 1.0f);
        return (float)Math.sqrt(var5 * var5 + var6 * var6) - 0.31f;
    }

    public static float wave(float var0, float var1, float var2, float var3) {
        return var2 < 0.001f ? 0.0f : 0.3f * var3 * var3 * (var0 * var0 - var1 * var1) / (var2 * var2);
    }

    public static Sdf3D pin() {
        Sdf2D var0 = Sdf2D.circle(0.0f, 0.0f, 0.68f).subtract(Sdf2D.circle(0.34f, 0.18f, 0.52f));
        Sdf2D var1 = Sdf2D.star(0.46f, 0.14f, 0.38f, 0.17f, 5).rotate(180.0f, 0.46f, 0.14f);
        Sdf3D[] var10000 = new Sdf3D[]{SdfShapes.extrude(var0, 0.14f, 0.06f).paint(SurfacePaint.radial(-4688, -0.3f, -0.2f, 0.0f, 0.8f, -1326758).glow()), SdfShapes.extrude(var1, 0.12f, 0.05f).move(0.0f, 0.0f, 0.04f).paint(SurfacePaint.of(-9872).glow())};
        Sdf3D var2 = SdfShapes.union(var10000);
        double var3 = Math.toRadians(38.0);
        float var5 = 3.38f;
        return var2.rotate(-8.0f, 38.0f, 0.0f).move(var5 * (float)Math.sin(var3), 29.95f, -0.25f + var5 * (float)Math.cos(var3));
    }

    public static Sdf3D worn(Sdf3D var0) {
        return LunaDesign.hs(var0.rotate(-8.0f, 0.0f, 4.0f, HAT_CENTRE.x(), HAT_CENTRE.y(), HAT_CENTRE.z()));
    }

    public static Vec3 hatPoint(float var0, float var1, float var2) {
        return new Vec3(var0, var1, var2).rotate(-8.0f, 0.0f, 4.0f, HAT_CENTRE);
    }

    public static void body(CharacterRig var0) {
        Sdf3D var1 = SdfShapes.ellipsoid(0.0f, 21.35f, 0.05f, 2.2f, 1.75f, 1.4f);
        Sdf3D var2 = SdfShapes.capsule(0.0f, 22.95f, -0.15f, -2.45f, 22.4f, -0.15f, 0.72f).mirrorX();
        Sdf3D var3 = SdfShapes.ellipsoid(0.0f, 19.0f, 0.0f, 1.62f, 1.5f, 1.15f);
        Sdf3D var4 = SdfShapes.ellipsoid(0.0f, 16.8f, -0.05f, 2.2f, 1.75f, 1.5f);
        float var10000 = 1.1f;
        Sdf3D[] var10001 = new Sdf3D[]{var1, var2, var3, var4};
        Sdf3D var5 = SdfShapes.blend(var10000, var10001);
        Sdf3D var6 = var5.paint(SurfacePaint.vertical(-14933168, 16.5f, -13945223, 22.5f));
        Sdf3D var7 = SdfShapes.capsule(0.0f, 22.9f, -0.15f, 0.0f, 25.1f, -0.05f, 0.6f).paint(SurfacePaint.vertical(-6958, 23.7f, -1331558, 24.2f).flatLight(1.0f).occlusion(0.25f));
        Sdf3D var8 = SdfShapes.cone(0.0f, 22.5f, -0.12f, 1.02f, 0.0f, 24.3f, -0.1f, 0.78f).intersect(SdfShapes.box(0.0f, 23.45f, -0.1f, 1.3f, 0.63f, 1.3f));
        Sdf3D var9 = var5.inflate(0.22f).intersect(SdfShapes.box(0.0f, 18.45f, 0.0f, 3.0f, 0.6f, 3.0f)).paint(SurfacePaint.vertical(-11653344, 17.9f, -8762574, 18.9f));
        CharacterPartBuilder var17 = var0.part(CharacterBone.BODY).pivot(0.0f, 24.5f, 0.0f);
        var10001 = new Sdf3D[]{var6, var7, var9};
        var17.shape(SdfShapes.union(var10001)).resolution(1.2f).detail(0.9f).occlusion(0.3f).outline(-15921370, 0.14f).decal(LunaDesign.bodiceTrim());
        var0.extra("collar", CharacterBone.BODY).shape(var8.paint(SurfacePaint.vertical(-15460295, 22.9f, -13945223, 24.05f))).resolution(0.6f).detail(0.35f).occlusion(0.3f).outline(-15921370, 0.08f);
        ArrayList<Sdf3D> var10 = new ArrayList<Sdf3D>();
        float var11 = LunaDesign.surfaceZ(var5, 0.0f, 23.1f, 4.0f);
        float var12 = Math.max(var11, LunaDesign.surfaceZ(var8, 0.0f, 23.1f, 4.0f));
        var10.add(SdfShapes.ellipsoid(0.0f, 23.08f, var12 + 0.12f, 0.3f, 0.26f, 0.2f).color(-4544530));
        var10.add(SdfShapes.ellipsoid(0.6f, 23.14f, var12 + 0.02f, 0.5f, 0.3f, 0.18f).rotate(0.0f, -18.0f, -14.0f, 0.6f, 23.14f, var12).mirrorX().color(-4544530));
        for (int var13 = -1; var13 <= 1; var13 += 2) {
            float var14 = LunaDesign.surfaceZ(var5, (float)var13 * 0.35f, 22.3f, 4.0f);
            float var15 = LunaDesign.surfaceZ(var5, (float)var13 * 0.55f, 21.6f, 4.0f);
            float var19 = 0.22f;
            float var10002 = 0.16f;
            float[] var10003 = new float[]{(float)var13 * 0.12f, 22.95f, var12 + 0.08f, (float)var13 * 0.35f, 22.3f, var14 + 0.14f, (float)var13 * 0.55f, 21.6f, var15 + 0.12f};
            var10.add(SdfShapes.strand(var19, var10002, var10003).flatten(1.7f, 0.0f, 0.0f, 1.0f).color(-4544530));
        }
        var10.add(SdfShapes.sphere(0.0f, 23.08f, var12 + 0.3f, 0.17f).paint(SurfacePaint.of(-4678401).glow()));
        float var16 = LunaDesign.surfaceZ(var9, 0.0f, 18.45f, 4.0f);
        var10.add(SdfShapes.extrude(Sdf2D.star(0.0f, 0.0f, 0.56f, 0.25f, 5).rotate(180.0f, 0.0f, 0.0f), 0.17f, 0.05f).move(0.0f, 18.45f, var16 + 0.08f).color(-1326758));
        var0.extra("trim", CharacterBone.BODY).shape(SdfShapes.union(var10.toArray(new Sdf3D[0]))).resolution(0.5f).detail(0.55f).noOutline();
    }

    public static void cape(CharacterRig var0) {
        Sdf3D var1 = SdfShapes.field(LunaDesign::capeDistance, -6.0f, 6.3f, -6.2f, 6.0f, 25.4f, 1.3f).paint(SurfacePaint.field(LunaDesign::capeColor));
        var0.extra("cape", CharacterBone.BODY).pivot(0.0f, 23.0f, -1.0f).shape(var1).resolution(1.35f).motion(BoneMotionProfile.cloth().drag(12.0f).hang(0.6f)).detail(0.45f).occlusion(0.3f).noOutline().decal(LunaDesign.capeStars()).decal(LunaDesign.capeHem());
    }

    public static float capeAt(int var0, float var1) {
        float var2 = (var1 - CAPE_Y[0]) / (CAPE_Y[CAPE_Y.length - 1] - CAPE_Y[0]) * 96.0f;
        var2 = var2 < 0.0f ? 0.0f : Math.min(var2, 96.0f);
        int var3 = Math.min((int)var2, 95);
        float var4 = CAPE_LUT[var3 * 4 + var0];
        return var4 + (CAPE_LUT[var3 * 4 + 4 + var0] - var4) * (var2 - (float)var3);
    }

    public static float[] capeTable() {
        float[] var0 = new float[388];
        for (int var1 = 0; var1 <= 96; ++var1) {
            float var2 = CAPE_Y[0] + (CAPE_Y[CAPE_Y.length - 1] - CAPE_Y[0]) * (float)var1 / 96.0f;
            var0[var1 * 4] = LunaDesign.spline(CAPE_Y, CAPE_RX, var2);
            var0[var1 * 4 + 1] = LunaDesign.spline(CAPE_Y, CAPE_RZ, var2);
            var0[var1 * 4 + 2] = LunaDesign.spline(CAPE_Y, CAPE_CZ, var2);
            var0[var1 * 4 + 3] = LunaDesign.spline(CAPE_Y, CAPE_WRAP, var2);
        }
        return var0;
    }

    public static float capeDistance(float var0, float var1, float var2) {
        float var9;
        float var3 = LunaDesign.capeAt(0, var1);
        float var4 = LunaDesign.capeAt(1, var1);
        float var5 = LunaDesign.capeAt(2, var1);
        float var6 = Math.abs(LunaDesign.ellipse(var0, var2 - var5, var3, var4)) - 0.43f;
        if (var6 < 0.6f) {
            float var7 = (float)Math.toDegrees(Math.atan2(Math.abs(var0) / var3, -(var2 - var5) / var4));
            var6 = Math.max(var6, (var7 - LunaDesign.capeAt(3, var1)) * 0.0174533f * (var3 + var4) * 0.5f);
        }
        var6 = Math.max(var6, Math.max(var1 - 23.4f, 7.0f - var1));
        if (var1 < 22.5f) {
            return var6;
        }
        float var13 = LunaDesign.clamp01((var1 - 22.9f) / 2.3f);
        float var8 = 1.45f + 0.35f * var13;
        float var10 = Math.abs(LunaDesign.ellipse(var0, var2 + 0.3f, var8, var9 = 1.3f + 0.3f * var13)) - 0.38f;
        if (var10 < 0.6f) {
            float var11 = (float)Math.toDegrees(Math.atan2(Math.abs(var0) / var8, -(var2 + 0.3f) / var9));
            var10 = Math.max(var10, (var11 - 118.0f) * 0.0174533f * 1.5f);
        }
        var10 = Math.max(var10, Math.max(var1 - 25.2f, 22.8f - var1));
        return Math.min(var6, var10);
    }

    public static int capeColor(float var0, float var1, float var2) {
        float var6;
        float var7;
        float var3 = LunaDesign.capeAt(0, var1);
        float var4 = LunaDesign.capeAt(1, var1);
        float var5 = LunaDesign.capeAt(2, var1);
        if (var1 > 22.9f && Math.abs(var7 = LunaDesign.ellipse(var0, var2 + 0.3f, 1.45f + 0.35f * (var6 = LunaDesign.clamp01((var1 - 22.9f) / 2.3f)), 1.3f + 0.3f * var6)) < Math.abs(LunaDesign.ellipse(var0, var2 - var5, var3, var4))) {
            return var7 > 0.0f ? -14603682 : -7571752;
        }
        float var8 = LunaDesign.ellipse(var0, var2 - var5, var3, var4);
        return var8 < 0.0f ? -7571752 : ArgbColors.mix(-15460295, -14603682, LunaDesign.clamp01((var1 - 7.0f) / 14.0f));
    }

    public static SurfaceDecal capeStars() {
        PixelCanvas var0 = new PixelCanvas(176, 176);
        long var1 = 7L;
        for (int var3 = 0; var3 < 36; ++var3) {
            long var4 = var1 * 6364136223846793005L + 1442695040888963407L;
            float var6 = 7.0f + (float)((var4 >>> 33) % 162L);
            long var7 = var4 * 6364136223846793005L + 1442695040888963407L;
            float var9 = 8.0f + (float)((var7 >>> 33) % 160L);
            var1 = var7 * 6364136223846793005L + 1442695040888963407L;
            float var10 = 2.8f + (float)((var1 >>> 33) % 3L);
            boolean var11 = (var1 >>> 40) % 3L == 0L;
            Sdf2D var12 = var11 ? Sdf2D.star(var6, var9, var10 * 1.3f, var10 * 0.3f, 4) : Sdf2D.star(var6, var9, var10, var10 * 0.46f, 5);
            var0.fill(var12, var11 ? -598638 : -1326758);
        }
        var0.fill(Sdf2D.circle(88.0f, 92.0f, 19.0f).subtract(Sdf2D.circle(97.0f, 86.0f, 16.0f)), -1326758);
        var0.fill(Sdf2D.star(97.0f, 88.0f, 7.5f, 3.2f, 5), -598638);
        return SurfaceDecal.back(var0, 0.0f, 12.5f, -3.0f, 11.0f, 11.0f).depth(4.0f, 4.0f).maxAngle(62.0f);
    }

    public static SurfaceDecal capeHem() {
        PixelCanvas var0 = new PixelCanvas(192, 12);
        LunaDesign.paintBand(var0);
        return SurfaceDecal.wrap(var0, 0.0f, 7.27f, -0.8f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 230.0f, 0.48f).offset(0.03f);
    }

    public static void paintBand(PixelCanvas var0) {
        float var1 = var0.height();
        float var2 = var1 * 2.0f;
        var0.fillAll((var2x, var3) -> {
            float var4 = var3 / var1;
            if (var4 < 0.1f || var4 > 0.9f) {
                return 0;
            }
            if (!(var4 < 0.24f) && !(var4 > 0.76f)) {
                float var5 = ((float)Math.floor(var2x / var2) + 0.5f) * var2;
                return Math.abs(var2x - var5) + Math.abs(var3 - var1 * 0.5f) < var1 * 0.22f ? -2868 : -1326758;
            }
            return -5011921;
        });
    }

    public static PixelCanvas lace(int var0, int var1) {
        PixelCanvas var2 = new PixelCanvas(var0, 16);
        float var3 = (float)var0 / (float)var1;
        var2.fill(Sdf2D.box((float)var0 * 0.5f, 3.0f, (float)var0 * 0.5f, 3.0f, 0.0f), -1036);
        for (int var4 = 0; var4 < var1; ++var4) {
            float var5 = ((float)var4 + 0.5f) * var3;
            var2.fill(Sdf2D.circle(var5, 6.5f, var3 * 0.52f), -1036);
            var2.erase(Sdf2D.circle(var5, 6.8f, var3 * 0.16f));
        }
        return var2;
    }

    public static void skirt(CharacterRig var0, PixelCanvas var1) {
        PixelCanvas var2 = LunaDesign.lace(448, 34);
        for (int var3 = -1; var3 <= 1; var3 += 2) {
            CharacterBone var4 = var3 < 0 ? CharacterBone.RIGHT_LEG : CharacterBone.LEFT_LEG;
            Sdf3D[] var10000 = new Sdf3D[]{LunaDesign.tier(var3, 18.5f, 12.4f, 1.75f, 3.45f, 2.5f, 11.0f, 0.24f, 0.78f, 6.0f, 16.0f).shape(-13945223, -15460295), LunaDesign.tier(var3, 13.4f, 9.1f, 3.1f, 3.95f, 1.3f, 14.0f, 0.28f, 0.74f, 14.0f, 22.0f).shape(-12693598, -14603682), LunaDesign.tier(var3, 9.55f, 8.6f, 3.7f, 4.05f, 1.0f, 20.0f, 0.16f, 0.74f, 20.0f, 24.0f).shape(-4611086, -7571752)};
            Sdf3D var5 = SdfShapes.union(var10000);
            var0.extra(var3 < 0 ? "skirt_r" : "skirt_l", CharacterBone.BODY).pivot(0.0f, 18.2f, 0.0f).shape(var5).resolution(1.15f).motion(BoneMotionProfile.skirt(var4).follow(var4, 0.3f, 12.0f)).detail(0.4f).occlusion(0.3f).noOutline().decal(LunaDesign.hemBand(var1, 12.58f, (float)var3 * 76.0f, 140.0f, 0.36f)).decal(LunaDesign.hemBand(var1, 9.28f, (float)var3 * 79.0f, 134.0f, 0.36f)).decal(LunaDesign.hemBand(var2, 8.8f, (float)var3 * 80.0f, 132.0f, 0.34f));
        }
        FoldedShape var6 = new FoldedShape(18.2f, 8.7f, 1.6f, 2.9f, 1.6f, 16.0f, 0.12f, 0.66f, -42.0f, -42.0f, 42.0f, 42.0f);
        var0.extra("underskirt", CharacterBone.BODY).pivot(0.0f, 18.2f, 0.2f).shape(var6.shape(-4611086, -7571752)).resolution(1.1f).motion(new BoneMotionProfile().drag(-8.0f).walk(-5.0f, 0.0f, 0.0f, 2.0f).walkPhase(90.0f).spring(120.0f, 14.0f).bend(1.0f)).detail(0.4f).occlusion(0.3f).noOutline().decal(LunaDesign.underskirtStars());
    }

    public static FoldedShape tier(int var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10) {
        return var0 > 0 ? new FoldedShape(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, 188.0f, 188.0f) : new FoldedShape(var1, var2, var3, var4, var5, var6, var7, var8, -188.0f, -188.0f, -var9, -var10);
    }

    public static SurfaceDecal hemBand(PixelCanvas var0, float var1, float var2, float var3, float var4) {
        double var5 = Math.toRadians(var2);
        return SurfaceDecal.wrap(var0, 0.0f, var1, 0.0f, 0.0f, 1.0f, 0.0f, (float)Math.sin(var5), 0.0f, (float)Math.cos(var5), var3, var4).offset(0.03f);
    }

    public static SurfaceDecal underskirtStars() {
        PixelCanvas var0 = new PixelCanvas(192, 288);
        long var1 = 3L;
        for (int var3 = 0; var3 < 20; ++var3) {
            long var4 = var1 * 6364136223846793005L + 1442695040888963407L;
            float var6 = 12.0f + (float)((var4 >>> 33) % 168L);
            var1 = var4 * 6364136223846793005L + 1442695040888963407L;
            float var7 = 88.0f + (float)((var1 >>> 33) % 170L);
            var0.fill(Sdf2D.star(var6, var7, 5.4f, 1.7f, 4), -1326758);
        }
        for (int var8 = 0; var8 < 18; ++var8) {
            float var9 = ((float)var8 + 0.5f) * 192.0f / 18.0f;
            var0.fill(Sdf2D.circle(var9, 278.0f, 6.2f), -1036);
            var0.erase(Sdf2D.circle(var9, 278.5f, 1.9f));
        }
        var0.fill(Sdf2D.box(96.0f, 272.0f, 96.0f, 4.0f, 0.0f), -1036);
        return SurfaceDecal.front(var0, 0.0f, 13.5f, 3.0f, 6.4f, 9.6f).depth(3.5f, 3.0f).maxAngle(66.0f);
    }

    public static SurfaceDecal bodiceTrim() {
        PixelCanvas var0 = new PixelCanvas(64, 96);
        for (int var1 = -1; var1 <= 1; var1 += 2) {
            float var10001 = 2.0f;
            float var10002 = 2.0f;
            float[] var10003 = new float[]{32.0f + (float)var1 * 10.5f, 2.0f, 32.0f + (float)var1 * 9.0f, 48.0f, 32.0f + (float)var1 * 9.8f, 94.0f};
            var0.fill(Sdf2D.path(var10001, var10002, var10003), -1326758);
        }
        for (int var3 = 0; var3 < 4; ++var3) {
            float var2 = 13.0f + (float)var3 * 21.0f;
            var0.fill(Sdf2D.circle(32.0f, var2, 5.0f), -5011921);
            var0.fill(Sdf2D.circle(32.0f, var2, 4.0f), -1326758);
            var0.fill(Sdf2D.circle(30.8f, var2 - 1.3f, 1.5f), -2868);
        }
        return SurfaceDecal.front(var0, 0.0f, 20.6f, 3.0f, 2.0f, 3.0f).depth(2.5f, 2.0f).maxAngle(70.0f);
    }

    private static Sdf3D fore(Sdf3D var0, int var1) {
        float var11 = (float)var1 * 3.0f;
        float var13 = var1;
        float var15 = ELBOW.x();
        float var14 = var13 * var15;
        float var17 = ELBOW.y();
        float var18 = ELBOW.z();
        return var0.rotate(-12.0f, 0.0f, var11, var14, var17, var18);
    }

    private static Vec3 fore(float var0, float var1, float var2) {
        Vec3 var3 = new Vec3(var0, var1, var2);
        Vec3 var11 = ELBOW;
        return var3.rotate(-12.0f, 0.0f, 3.0f, var11);
    }

    public static void arms(CharacterRig var0, PixelCanvas var1) {
        SurfacePaint var2 = SurfacePaint.of(-6958).flatLight(1.0f).occlusion(0.25f);
        Sdf3D var3 = SdfShapes.ellipsoid(-3.5f, 22.0f, -0.1f, 0.92f, 1.0f, 0.9f);
        Sdf3D var4 = SdfShapes.cone(-3.55f, 21.5f, -0.1f, 0.66f, -3.72f, 18.45f, -0.05f, 0.55f);
        Sdf3D var5 = SdfShapes.sphere(-3.72f, 18.4f, -0.05f, 0.55f);
        Sdf3D var6 = LunaDesign.fore(SdfShapes.cone(-3.72f, 18.3f, -0.05f, 0.54f, -3.84f, 15.6f, 0.12f, 0.5f), 1);
        float var10000 = 0.35f;
        Sdf3D[] var10001 = new Sdf3D[]{var3, var4, var5, var6};
        Sdf3D var7 = SdfShapes.blend(var10000, var10001).paint(SurfacePaint.vertical(-14867372, 15.0f, -13945223, 22.0f));
        Sdf3D var8 = LunaDesign.fore(SdfShapes.cone(-3.84f, 16.1f, 0.12f, 0.52f, -3.92f, 14.3f, 0.2f, 1.02f).intersect(SdfShapes.above(14.3f)), 1).color(-13945223);
        Sdf3D var9 = LunaDesign.fore(SdfShapes.cone(-3.84f, 15.7f, 0.12f, 0.2f, -3.93f, 14.0f, 0.2f, 0.5f), 1).color(-7571752);
        Sdf3D[] var22 = new Sdf3D[]{var7, SdfShapes.subtract(var8, var9)};
        Sdf3D var10 = SdfShapes.union(var22);
        Vec3 var11 = LunaDesign.fore(-4.0f, 13.3f, 0.42f);
        Vec3 var12 = LunaDesign.fore(-3.91f, 14.52f, 0.2f);
        Vec3 var13 = new Vec3(-0.04f, 1.0f, 0.05f).rotate(-12.0f, 0.0f, 3.0f);
        Vec3 var14 = new Vec3(0.0f, 0.0f, 1.0f).rotate(-12.0f, 0.0f, 3.0f);
        CharacterPartBuilder var15 = var0.part(CharacterBone.RIGHT_ARM).pivot(-3.5f, 22.3f, -0.1f).grip(var11.x(), var11.y(), var11.z()).shape(var10).resolution(0.95f).detail(0.75f).occlusion(0.3f).outline(-15921370, 0.13f).swing(0.8f).joint(ELBOW.x(), ELBOW.y(), ELBOW.z(), 0.55f).decal(SurfaceDecal.wrap(var1, var12.x(), var12.y(), var12.z(), var13.x(), var13.y(), var13.z(), var14.x(), var14.y(), var14.z(), 360.0f, 0.36f).offset(0.03f));
        var0.part(CharacterBone.LEFT_ARM).mirrorOf(var15);
        ArrayList<Sdf3D> var16 = new ArrayList<Sdf3D>();
        var16.add(SdfShapes.cone(3.92f, 14.6f, 0.22f, 0.33f, 3.97f, 13.8f, 0.28f, 0.33f));
        var16.add(SdfShapes.ellipsoid(4.0f, 13.25f, 0.32f, 0.27f, 0.6f, 0.52f));
        float[] var23 = new float[]{-24.0f, -19.0f, -14.0f, -8.0f};
        float[] var17 = var23;
        for (int var18 = 0; var18 < 4; ++var18) {
            float var19 = 0.02f + (float)var18 * 0.21f;
            float var20 = var18 != 0 && var18 != 3 ? 0.8f : 0.62f;
            var16.add(SdfShapes.capsule(4.02f, 12.8f, var19, 3.94f, 12.8f - var20, var19 + 0.1f, 0.12f).rotate(0.0f, 0.0f, var17[var18], 4.02f, 12.85f, var19));
        }
        var16.add(SdfShapes.capsule(3.86f, 13.35f, 0.76f, 3.74f, 12.75f, 0.96f, 0.14f));
        CharacterPartBuilder var21 = var0.extra("hand_l", CharacterBone.LEFT_ARM).shape(LunaDesign.fore(SdfShapes.blend(0.18f, var16.toArray(new Sdf3D[0])).paint(var2), -1)).resolution(0.5f).detail(0.6f).outline(-5737368, 0.08f);
        var0.extra("hand_r", CharacterBone.RIGHT_ARM).mirrorOf(var21);
    }

    public static void legs(CharacterRig var0) {
        Sdf3D var1 = SdfShapes.cone(-1.3f, 15.3f, 0.0f, 1.18f, -1.1f, 8.6f, 0.08f, 0.82f);
        Sdf3D var2 = SdfShapes.sphere(-1.08f, 8.25f, 0.14f, 0.78f);
        Sdf3D var3 = SdfShapes.sphere(-1.08f, 8.3f, 0.45f, 0.55f);
        Sdf3D var4 = SdfShapes.cone(-1.08f, 8.0f, 0.1f, 0.8f, -1.14f, 2.1f, -0.1f, 0.5f);
        Sdf3D var5 = SdfShapes.ellipsoid(-1.12f, 5.9f, -0.3f, 0.85f, 1.5f, 0.9f);
        float var10000 = 0.45f;
        Sdf3D[] var10001 = new Sdf3D[]{var1, var2, var3, var4, var5};
        Sdf3D var6 = SdfShapes.blend(var10000, var10001).paint(SurfacePaint.field(LunaDesign::tights));
        Sdf3D var7 = SdfShapes.cone(-1.12f, 4.9f, -0.08f, 0.8f, -1.1f, 1.5f, -0.1f, 0.62f).intersect(SdfShapes.below(5.0f));
        Sdf3D var8 = SdfShapes.ellipsoid(-1.1f, 0.48f, 1.0f, 0.6f, 0.48f, 0.82f);
        Sdf3D var9 = SdfShapes.ellipsoid(-1.1f, 1.0f, 0.25f, 0.64f, 0.62f, 0.9f);
        var10000 = 0.35f;
        var10001 = new Sdf3D[]{var7, var8, var9};
        Sdf3D var10 = SdfShapes.blend(var10000, var10001).paint(SurfacePaint.vertical(-9550799, 0.5f, -7710146, 4.6f));
        Sdf3D var11 = SdfShapes.box(-1.1f, 0.42f, -0.5f, 0.4f, 0.42f, 0.36f, 0.14f).color(-12704232);
        Sdf3D var12 = SdfShapes.box(-1.1f, 0.12f, 0.97f, 0.64f, 0.12f, 0.8f, 0.1f).color(-12966888);
        Sdf3D var13 = SdfShapes.torus(-1.1f, 1.75f, -0.05f, 0.62f, 0.26f).color(-11653344);
        Sdf3D var14 = SdfShapes.ellipsoid(-1.72f, 1.75f, 0.57f, 0.24f, 0.3f, 0.14f).rotate(0.0f, -45.0f, 0.0f, -1.72f, 1.75f, 0.57f).color(-1326758);
        Sdf3D[] var21 = new Sdf3D[]{var10, var11, var12, var13, var14};
        Sdf3D var15 = SdfShapes.union(var21).rotate(0.0f, -7.0f, 0.0f, -1.12f, 0.0f, -0.1f);
        Sdf3D[] var22 = new Sdf3D[]{var6, var15};
        Sdf3D var16 = SdfShapes.union(var22).intersect(SdfShapes.above(0.0f));
        CharacterPartBuilder var17 = var0.part(CharacterBone.RIGHT_LEG).pivot(-1.3f, 15.3f, 0.0f).shape(var16).resolution(1.2f).detail(0.75f).occlusion(0.3f).outline(-14411226, 0.13f).swing(0.55f, 30.0f).joint(-1.08f, 8.25f, 0.14f, 0.8f);
        var0.part(CharacterBone.LEFT_LEG).mirrorOf(var17);
        Sdf3D var18 = SdfShapes.cylinder(-1.12f, 4.45f, -0.08f, -1.12f, 5.18f, -0.08f, 0.94f, 0.18f).paint(SurfacePaint.vertical(-7710146, 4.5f, -6395064, 5.1f));
        CharacterPartBuilder var19 = var0.extra("cuff_r", CharacterBone.RIGHT_LEG).shape(var18).resolution(0.7f).detail(0.9f).occlusion(0.3f).outline(-12966888, 0.08f);
        var0.extra("cuff_l", CharacterBone.LEFT_LEG).mirrorOf(var19);
    }

    public static int tights(float var0, float var1, float var2) {
        float var3 = -1.1f - 0.2f * LunaDesign.clamp01((var1 - 8.0f) / 7.0f);
        float var4 = 0.05f;
        float var5 = (float)Math.atan2(var0 - var3, var2 - var4);
        float var6 = (float)Math.exp(-(var5 + 0.35f) * (var5 + 0.35f) / 0.16f);
        int var7 = ArgbColors.mix(ArgbColors.shade(-12959880, 0.82f), -12959880, LunaDesign.clamp01((var1 - 3.0f) / 6.0f));
        return ArgbColors.mix(var7, -10656082, 0.55f * var6);
    }

    public static SurfaceDecal faceDecal(List<CurvedShape> var0) {
        PixelCanvas var1 = new PixelCanvas(192, 158);
        Sdf2D var2 = null;
        for (CurvedShape var4 : var0) {
            Sdf2D var5 = var4.shadow(0.28f).inflate(3.7333333f);
            var2 = var2 == null ? var5 : var2.union(var5);
        }
        var1.fill(var2, ArgbColors.withAlpha(-1199194, 0.55f), 4.0f);
        var1.draw(LunaDesign.features(), 12, 44);
        Vec3 var6 = LunaDesign.hp(0.0f, 26.90625f, 3.0f);
        return SurfaceDecal.front(var1, var6.x(), var6.y(), var6.z(), 4.8599997f, 3.9993749f).depth(3.0f, 3.0f).maxAngle(74.0f).occlusion(0.3f).flatLight(1.0f);
    }

    public static PixelCanvas features() {
        float var0 = 0.5833333f;
        PixelCanvas var1 = new PixelCanvas(168, 114);
        PixelCanvas var2 = new PixelCanvas(64, 80);
        LunaDesign.eye(new CanvasTransform(var2, var0, 60.0f, 96.0f), 122.0f, 181.0f, -1);
        var1.draw(var2, 7, 0);
        var1.draw(var2.mirrored(), 97, 0);
        CanvasTransform var3 = new CanvasTransform(var1, var0, 48.0f, 96.0f);
        for (int var4 = -1; var4 <= 1; var4 += 2) {
            float var5 = 192.0f + (float)var4 * 70.0f;
            float var6 = 181.0f;
            var3.fill(Sdf2D.star(var5 - 10.0f, var6 - 13.0f, 14.0f, 3.8f, 4), -1);
            var3.fill(Sdf2D.circle(var5 - 10.0f, var6 - 13.0f, 4.8f), -1);
            var3.fill(Sdf2D.star(var5 + 12.0f, var6 + 17.0f, 7.0f, 2.1f, 4), -285212673);
            var3.fill(Sdf2D.circle(var5 + 10.0f, var6 - 2.0f, 2.3f), -1191182337);
            var3.fill(Sdf2D.ellipse(192.0f + (float)var4 * 96.0f, 226.0f, 27.0f, 12.0f), 1895793815, 12.0f);
        }
        var3.fill(Sdf2D.curve(191.0f, 232.0f, 195.0f, 238.0f, 190.0f, 242.0f, 2.4f, 1.6f), -2135459206);
        var3.fill(Sdf2D.curve(180.0f, 275.0f, 192.0f, 284.0f, 204.0f, 275.0f, 3.2f, 3.2f), -7060390);
        var3.fill(Sdf2D.curve(188.0f, 285.0f, 192.0f, 287.0f, 196.0f, 285.0f, 1.8f, 1.8f), 1438670960);
        return var1;
    }

    public static void eye(CanvasTransform var0, float var1, float var2, int var3) {
        Sdf2D var4 = Sdf2D.ellipse(var1, var2 + 2.0f, 37.0f, 46.0f);
        float var10003 = var2 - 44.0f;
        float var10005 = var2 - 8.0f;
        int[] var10006 = new int[]{-3160852, -1};
        var0.fill(var4, ColorField2D.linear(var1, var10003, var1, var10005, var10006));
        float var10001 = var2 - 36.0f;
        var10003 = var2 + 44.0f;
        int[] var10004 = new int[]{-14282676, -11194192, -6656782, -2044673};
        ColorField2D var5 = ColorField2D.linear(var1, var10001, var1, var10003, var10004);
        var0.fill(Sdf2D.ellipse(var1, var2 + 4.0f, 29.0f, 41.0f).intersect(var4), (var3x, var4x) -> {
            float var5x = (var3x - var1) / 32.0f;
            float var6x = (var4x - var2 + 28.0f) / 19.0f;
            float var7x = (var3x - var1) / 18.0f;
            float var8x = (var4x - var2 - 27.0f) / 10.0f;
            float var9x = (var3x - var1) / 29.0f;
            float var10x = (var4x - var2 - 4.0f) / 41.0f;
            float var11x = LunaDesign.clamp01((1.0f - (float)Math.sqrt(var5x * var5x + var6x * var6x)) * 8.0f) * 0.47f;
            float var12x = LunaDesign.clamp01((1.0f - (float)Math.sqrt(var7x * var7x + var8x * var8x)) * 5.0f) * 0.5f;
            float var13x = LunaDesign.clamp01(((float)Math.sqrt(var9x * var9x + var10x * var10x) - 0.88f) / 0.06f) * 0.82f;
            int var14x = ArgbColors.mix(ArgbColors.mix(var5.argb(var3x, var4x), -14676416, var11x), -726785, var12x);
            return ArgbColors.mix(var14x, -14479041, var13x);
        });
        var0.fill(Sdf2D.ellipse(var1, var2 + 5.0f, 12.0f, 17.0f), -15136210);
        var10001 = 6.0f;
        float var10002 = 12.0f;
        float[] var12 = new float[]{var1 - (float)var3 * 37.0f, var2 - 6.0f, var1 - (float)var3 * 24.0f, var2 - 36.0f, var1 + (float)var3 * 2.0f, var2 - 48.0f, var1 + (float)var3 * 25.0f, var2 - 42.0f, var1 + (float)var3 * 39.0f, var2 - 26.0f, var1 + (float)var3 * 50.0f, var2 - 32.0f};
        var0.fill(Sdf2D.path(var10001, var10002, var12), -14938064);
        var0.fill(Sdf2D.curve(var1 + (float)var3 * 40.0f, var2 - 28.0f, var1 + (float)var3 * 52.0f, var2 - 40.0f, var1 + (float)var3 * 58.0f, var2 - 50.0f, 5.0f, 1.0f), -14938064);
        var0.fill(Sdf2D.curve(var1 + (float)var3 * 30.0f, var2 - 38.0f, var1 + (float)var3 * 38.0f, var2 - 50.0f, var1 + (float)var3 * 40.0f, var2 - 58.0f, 3.5f, 1.0f), -14938064);
        var0.fill(Sdf2D.curve(var1 + (float)var3 * 6.0f, var2 + 48.0f, var1 + (float)var3 * 26.0f, var2 + 44.0f, var1 + (float)var3 * 35.0f, var2 + 28.0f, 1.5f, 4.0f), -1069931696);
        var10001 = 2.0f;
        var10002 = 2.0f;
        float[] var13 = new float[]{var1 - (float)var3 * 18.0f, var2 - 55.0f, var1 + (float)var3 * 4.0f, var2 - 60.0f, var1 + (float)var3 * 27.0f, var2 - 53.0f};
        var0.fill(Sdf2D.path(var10001, var10002, var13), 1883909461);
        var10001 = 5.5f;
        var10002 = 2.0f;
        float[] var14 = new float[]{var1 - (float)var3 * 28.0f, var2 - 70.0f, var1 + (float)var3 * 2.0f, var2 - 82.0f, var1 + (float)var3 * 36.0f, var2 - 76.0f};
        var0.fill(Sdf2D.path(var10001, var10002, var14), -7703368);
    }

    public static float ellipse(float var0, float var1, float var2, float var3) {
        float var4 = var0 / var2;
        float var5 = var1 / var3;
        float var6 = (float)Math.sqrt(var4 * var4 + var5 * var5);
        float var7 = (float)Math.sqrt(var4 * var4 / (var2 * var2) + var5 * var5 / (var3 * var3));
        return var7 < 1.0E-6f ? -Math.min(var2, var3) : var6 * (var6 - 1.0f) / var7;
    }

    public static float spline(float[] var0, float[] var1, float var2) {
        int var4;
        int var3 = var0.length;
        if (var2 <= var0[0]) {
            return var1[0];
        }
        if (var2 >= var0[var3 - 1]) {
            return var1[var3 - 1];
        }
        for (var4 = 0; var4 < var3 - 2 && var2 > var0[var4 + 1]; ++var4) {
        }
        float var5 = (var2 - var0[var4]) / (var0[var4 + 1] - var0[var4]);
        float var6 = var0[var4 + 1] - var0[var4];
        float var7 = var4 == 0 ? (var1[1] - var1[0]) / (var0[1] - var0[0]) : (var1[var4 + 1] - var1[var4 - 1]) / (var0[var4 + 1] - var0[var4 - 1]);
        float var8 = var4 + 2 >= var3 ? (var1[var3 - 1] - var1[var3 - 2]) / (var0[var3 - 1] - var0[var3 - 2]) : (var1[var4 + 2] - var1[var4]) / (var0[var4 + 2] - var0[var4]);
        float var9 = var5 * var5;
        float var10 = var9 * var5;
        return (2.0f * var10 - 3.0f * var9 + 1.0f) * var1[var4] + (var10 - 2.0f * var9 + var5) * var6 * var7 + (-2.0f * var10 + 3.0f * var9) * var1[var4 + 1] + (var10 - var9) * var6 * var8;
    }

    public static float surfaceZ(Sdf3D var0, float var1, float var2, float var3) {
        float var6;
        float var4 = var3;
        for (int var5 = 0; var5 < 80 && !(Math.abs(var6 = var0.distance(var1, var2, var4)) < 0.001f); ++var5) {
            var4 -= var6;
        }
        return var4;
    }

    public static float clamp01(float var0) {
        return var0 < 0.0f ? 0.0f : Math.min(var0, 1.0f);
    }

    static {
        float[] fArray = new float[6];
        float[] var9 = fArray;
        fArray[0] = 7.0f;
        var9[1] = 10.0f;
        var9[2] = 13.5f;
        var9[3] = 17.0f;
        var9[4] = 20.5f;
        var9[5] = 23.4f;
        CAPE_Y = var9;
        float[] fArray2 = new float[6];
        float[] var11 = fArray2;
        fArray2[0] = 5.15f;
        var11[1] = 4.85f;
        var11[2] = 4.15f;
        var11[3] = 3.45f;
        var11[4] = 3.2f;
        var11[5] = 3.15f;
        CAPE_RX = var11;
        float[] fArray3 = new float[6];
        float[] var13 = fArray3;
        fArray3[0] = 4.45f;
        var13[1] = 4.2f;
        var13[2] = 3.5f;
        var13[3] = 2.3f;
        var13[4] = 1.62f;
        var13[5] = 1.3f;
        CAPE_RZ = var13;
        float[] fArray4 = new float[6];
        float[] var15 = fArray4;
        fArray4[0] = -0.8f;
        var15[1] = -0.85f;
        var15[2] = -0.85f;
        var15[3] = -0.6f;
        var15[4] = -0.45f;
        var15[5] = -0.35f;
        CAPE_CZ = var15;
        float[] fArray5 = new float[6];
        float[] var17 = fArray5;
        fArray5[0] = 104.0f;
        var17[1] = 102.0f;
        var17[2] = 100.0f;
        var17[3] = 105.0f;
        var17[4] = 118.0f;
        var17[5] = 132.0f;
        CAPE_WRAP = var17;
        CAPE_LUT = LunaDesign.capeTable();
        float[] fArray6 = new float[6];
        float[] var20 = fArray6;
        fArray6[0] = 17.0f;
        var20[1] = 20.0f;
        var20[2] = 22.5f;
        var20[3] = 24.5f;
        var20[4] = 27.6f;
        var20[5] = 29.3f;
        CURTAIN_Y = var20;
        float[] fArray7 = new float[6];
        float[] var22 = fArray7;
        fArray7[0] = 3.2f;
        var22[1] = 3.05f;
        var22[2] = 2.95f;
        var22[3] = 2.95f;
        var22[4] = 3.12f;
        var22[5] = 2.35f;
        CURTAIN_RX = var22;
        float[] fArray8 = new float[6];
        float[] var24 = fArray8;
        fArray8[0] = 3.0f;
        var24[1] = 2.65f;
        var24[2] = 2.25f;
        var24[3] = 2.55f;
        var24[4] = 3.05f;
        var24[5] = 2.35f;
        CURTAIN_RZ = var24;
        float[] fArray9 = new float[6];
        float[] var26 = fArray9;
        fArray9[0] = -0.95f;
        var26[1] = -0.95f;
        var26[2] = -0.9f;
        var26[3] = -0.65f;
        var26[4] = -0.3f;
        var26[5] = -0.3f;
        CURTAIN_CZ = var26;
    }

    @Environment(value=EnvType.CLIENT)
    private static final class CurvedShape {
        private static final float CONSTANT_0102 = 2.6f;
        private final float rad;
        private final float top;
        private final float tip;
        private final float width;
        private final float hook;
        private final float g0;
        private final float lift;
        private final float g1;
        private final float t0;
        private final float t1;
        private final float[] ys;
        private final float[] rs;
        private final float[] poly;
        private final Sdf2D outline;

        CurvedShape(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float[] var11, float[] var12) {
            this.rad = (float)Math.toRadians(var1);
            this.top = var2;
            this.tip = var3;
            this.width = var4;
            this.hook = var5;
            this.g0 = var6;
            this.lift = var7;
            this.g1 = var8;
            this.t0 = var9;
            this.t1 = var10;
            this.ys = var11;
            this.rs = var12;
            int var13 = 7;
            float var14 = var5 > 0.0f ? -1.0f : 1.0f;
            this.poly = new float[(2 * var13 + 3) * 2];
            int var15 = 0;
            this.poly[var15++] = var14 * var4;
            this.poly[var15++] = var2 + 0.5f;
            for (int var16 = 0; var16 <= var13; ++var16) {
                float var17 = 1.0f - (float)Math.pow(1.0f - (float)var16 / (float)var13, 1.4f);
                this.poly[var15++] = var5 * var17 * var17 + var14 * var4 * (float)Math.pow(1.0f - var17, 0.8f);
                this.poly[var15++] = var2 + (var3 - var2) * var17;
            }
            for (int var23 = var13 - 1; var23 >= 0; --var23) {
                float var24 = 1.0f - (float)Math.pow(1.0f - (float)var23 / (float)var13, 1.4f);
                this.poly[var15++] = var5 * var24 * var24 - var14 * var4 * (float)Math.pow(1.0f - var24, 1.25);
                this.poly[var15++] = var2 + (var3 - var2) * var24;
            }
            this.poly[var15++] = -var14 * var4;
            this.poly[var15] = var2 + 0.5f;
            this.outline = Sdf2D.polygon(this.poly);
        }

        private float gap(float var1) {
            float var2 = 1.0f - var1;
            return this.g0 * var2 * var2 + 2.0f * this.lift * var1 * var2 + this.g1 * var1 * var1;
        }

        float distance(float var1, float var2, float var3) {
            float var8;
            float var4 = var3 - -0.3f;
            float var5 = (float)Math.sqrt(var1 * var1 + var4 * var4);
            float var6 = LunaDesign.clamp01((this.top - var2) / (this.top - this.tip));
            float var7 = LunaDesign.interp(this.ys, this.rs, var2) + this.gap(var6);
            float var9 = Math.max(var7 - var5, var5 - var7 - (var8 = this.t0 + (this.t1 - this.t0) * var6));
            if (var9 > 0.8f) {
                return var9;
            }
            float var10 = 2.6f * ((float)Math.atan2(var1, var4) - this.rad);
            float var11 = this.outline.distance(var10, var2);
            if (var11 > 0.8f) {
                return var11;
            }
            float var12 = Math.min(1.0f, Math.abs(var10 - this.hook * var6 * var6) / (this.width * (1.0f - var6) + 0.05f));
            var9 = Math.max(var7 - var5, var5 - var7 - var8 * (1.0f - 0.35f * var12 * var12));
            float var13 = LunaDesign.clamp01(0.5f - 0.5f * (var9 - var11) / 0.07f);
            return var9 + (var11 - var9) * var13 + 0.07f * var13 * (1.0f - var13);
        }

        Sdf3D shape() {
            float var1 = Float.MAX_VALUE;
            float var2 = -3.4028235E38f;
            float var3 = Float.MAX_VALUE;
            float var4 = 0.0f;
            for (int var5 = 0; var5 < this.poly.length; var5 += 2) {
                var1 = Math.min(var1, this.poly[var5]);
                var2 = Math.max(var2, this.poly[var5]);
            }
            for (float var8 : this.rs) {
                var3 = Math.min(var3, var8);
                var4 = Math.max(var4, var8);
            }
            var3 = Math.max(0.2f, var3 + Math.min(this.g0, this.g1) - 0.15f);
            var4 += Math.max(this.g0, Math.max(this.lift, this.g1)) + this.t0 + 0.15f;
            float var19 = Float.MAX_VALUE;
            float var20 = -3.4028235E38f;
            float var21 = Float.MAX_VALUE;
            float var22 = -3.4028235E38f;
            for (int var9 = 0; var9 <= 12; ++var9) {
                double var10 = this.rad + (var1 + (var2 - var1) * (float)var9 / 12.0f) / 2.6f;
                float[] var10000 = new float[]{var3, var4};
                for (float var15 : var10000) {
                    var19 = Math.min(var19, var15 * (float)Math.sin(var10));
                    var20 = Math.max(var20, var15 * (float)Math.sin(var10));
                    var21 = Math.min(var21, -0.3f + var15 * (float)Math.cos(var10));
                    var22 = Math.max(var22, -0.3f + var15 * (float)Math.cos(var10));
                }
            }
            return SdfShapes.field(this::distance, var19 - 0.1f, this.tip - 0.15f, var21 - 0.1f, var20 + 0.1f, this.top + 0.6f, var22 + 0.1f);
        }

        Sdf2D shadow(float var1) {
            float[] var2 = (float[])this.poly.clone();
            for (int var3 = 0; var3 < var2.length; var3 += 2) {
                float var4 = var2[var3 + 1];
                float var5 = LunaDesign.interp(this.ys, this.rs, var4) + this.gap(LunaDesign.clamp01((this.top - var4) / (this.top - this.tip))) + this.t0 * 0.5f;
                var2[var3] = (var5 * (float)Math.sin(this.rad + var2[var3] / 2.6f) - -2.3142858f) * 41.48148f;
                var2[var3 + 1] = (28.810715f - var4 + var1) * 41.48148f;
            }
            return Sdf2D.polygon(var2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class FoldedShape {
        private static final int CONSTANT_0251 = 48;
        private final float yTop;
        private final float yBot;
        private final float rTop;
        private final float rBot;
        private final float folds;
        private final float amp;
        private final float half;
        private final float a0Top;
        private final float a0Bot;
        private final float a1Top;
        private final float a1Bot;
        private final float[] shapeField = new float[98];

        FoldedShape(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12) {
            this.yTop = var1;
            this.yBot = var2;
            this.rTop = var3;
            this.rBot = var4;
            this.folds = var6;
            this.amp = var7;
            this.half = var8 * 0.5f;
            this.a0Top = var9;
            this.a0Bot = var10;
            this.a1Top = var11;
            this.a1Bot = var12;
            for (int var13 = 0; var13 <= 48; ++var13) {
                float var14 = Math.min((float)var13 / 48.0f, 0.999f);
                this.shapeField[var13 * 2] = 1.0f - (float)Math.pow(1.0f - var14, var5);
                this.shapeField[var13 * 2 + 1] = var5 * (float)Math.pow(1.0f - var14, var5 - 1.0f);
            }
        }

        private float u(float var1) {
            return LunaDesign.clamp01((this.yTop - var1) / (this.yTop - this.yBot));
        }

        private float table(int var1, float var2) {
            float var3 = var2 * 48.0f;
            int var4 = Math.min((int)var3, 47);
            float var5 = this.shapeField[var4 * 2 + var1];
            return var5 + (this.shapeField[var4 * 2 + 2 + var1] - var5) * (var3 - (float)var4);
        }

        float radius(float var1, float var2, float var3) {
            float var4 = this.u(var2);
            return this.rTop + (this.rBot - this.rTop) * this.table(0, var4) + this.amp * var4 * (float)Math.cos((double)this.folds * Math.atan2(var1, var3));
        }

        float distance(float var1, float var2, float var3) {
            double var21;
            float var7;
            float var4 = this.u(var2);
            float var5 = this.yTop - this.yBot;
            float var6 = (float)Math.sqrt(var1 * var1 + var3 * var3);
            float var8 = Math.abs(var6 - (var7 = this.rTop + (this.rBot - this.rTop) * this.table(0, var4))) - this.half - this.amp;
            if (var8 > 0.6f) {
                return Math.max(var8 * 0.7f, Math.max(var2 - this.yTop, this.yBot - var2));
            }
            double var9 = Math.atan2(var1, var3);
            float var11 = (float)Math.cos((double)this.folds * var9);
            float var12 = (float)Math.sin((double)this.folds * var9);
            float var13 = var7 + this.amp * var4 * var11;
            float var14 = ((this.rBot - this.rTop) * this.table(1, var4) + this.amp * var11) / var5;
            float var15 = this.amp * var4 * this.folds * var12 / Math.max(var6, 0.5f);
            float var16 = (Math.abs(var6 - var13) - this.half) / (float)Math.sqrt(1.0f + var14 * var14 + var15 * var15);
            var16 = Math.max(var16, Math.max(var2 - this.yTop, this.yBot - var2));
            double var17 = Math.toRadians(this.a0Top + (this.a0Bot - this.a0Top) * var4);
            double var19 = Math.toRadians(this.a1Top + (this.a1Bot - this.a1Top) * var4);
            for (var21 = var9 - (var17 + var19) * 0.5; var21 > Math.PI; var21 -= Math.PI * 2) {
            }
            while (var21 < -Math.PI) {
                var21 += Math.PI * 2;
            }
            return Math.max(var16, (float)(Math.abs(var21) - (var19 - var17) * 0.5) * var6);
        }

        Sdf3D shape(int var1, int var2) {
            float var3 = Math.max(this.rTop, this.rBot) + this.amp + this.half + 0.2f;
            float var4 = Math.min(this.a0Top, this.a0Bot);
            float var5 = Math.max(this.a1Top, this.a1Bot);
            float var6 = 0.0f;
            float var7 = 0.0f;
            float var8 = 0.0f;
            float var9 = 0.0f;
            for (int var10 = 0; var10 <= 64; ++var10) {
                double var11 = Math.toRadians(var4 + (var5 - var4) * (float)var10 / 64.0f);
                var6 = Math.min(var6, (float)Math.sin(var11) * var3);
                var7 = Math.max(var7, (float)Math.sin(var11) * var3);
                var8 = Math.min(var8, (float)Math.cos(var11) * var3);
                var9 = Math.max(var9, (float)Math.cos(var11) * var3);
            }
            Sdf3D var13 = SdfShapes.field(this::distance, var6 - 0.3f, this.yBot - 0.2f, var8 - 0.3f, var7 + 0.3f, this.yTop + 0.2f, var9 + 0.3f);
            return var13.paint(SurfacePaint.field((var3x, var4x, var5x) -> {
                float var6x = (float)Math.sqrt(var3x * var3x + var5x * var5x);
                return var6x < this.radius(var3x, var4x, var5x) ? var2 : ArgbColors.mix(ArgbColors.shade(var1, 0.8f), var1, LunaDesign.clamp01((var4x - this.yBot) / (this.yTop - this.yBot) * 1.6f));
            }));
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class CanvasTransform {
        private final PixelCanvas canvas;
        private final float k;
        private final float ox;
        private final float oy;

        CanvasTransform(PixelCanvas var1, float var2, float var3, float var4) {
            this.canvas = var1;
            this.k = var2;
            this.ox = var3;
            this.oy = var4;
        }

        private Sdf2D map(Sdf2D var1) {
            float var2 = this.k / (this.k - 1.0f);
            return var1.scale(this.k, this.ox * var2, this.oy * var2);
        }

        void fill(Sdf2D var1, int var2) {
            this.canvas.fill(this.map(var1), var2);
        }

        void fill(Sdf2D var1, int var2, float var3) {
            this.canvas.fill(this.map(var1), var2, var3 * this.k);
        }

        void fill(Sdf2D var1, ColorField2D var2) {
            this.canvas.fill(this.map(var1), (var2x, var3) -> var2.argb(var2x / this.k + this.ox, var3 / this.k + this.oy));
        }
    }
}

