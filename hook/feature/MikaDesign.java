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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class MikaDesign
extends CharacterDefinition {
    private static final int SKIN = -9532;
    private static final int SKIN_SHADE = -1331558;
    private static final int SKIN_LINE = -5740966;
    private static final int HAIR = -681403;
    private static final int HAIR_DARK = -3647457;
    private static final int HAIR_TIP = -15750;
    private static final int HAIR_LINE = -6142180;
    private static final int EAR_TIP = -4697060;
    private static final int EAR_PINK = -22088;
    private static final int TUFT = -5162;
    private static final int HOODIE = -4200487;
    private static final int HOODIE_SHADE = -7092297;
    private static final int RIB = -5710645;
    private static final int CUFF_IN = -9461870;
    private static final int HOODIE_LINE = -10774650;
    private static final int LINING = -744532;
    private static final int SKIRT = -12892555;
    private static final int SKIRT_DARK = -14275250;
    private static final int SKIRT_LINE = -14999232;
    private static final int TIGHTS = -12965330;
    private static final int TIGHTS_SHEEN = -9808288;
    private static final int TIGHTS_LINE = -15068908;
    private static final int SHOE = -542005;
    private static final int SOLE = -264207;
    private static final int MINT = -5839921;
    private static final int FISH = -8398652;
    private static final int FISH_LINE = -12811148;
    private static final int SHOE_LINE = -6659470;
    private static final int GOLD = -867761;
    private static final int TAIL_TIP = -3618;
    private static final int LASH = -13887470;
    private static final int BROW = -4691398;
    private static final float HX = 0.0f;
    private static final float HY = 27.95f;
    private static final float HZ = -0.15f;
    private static final float RX = 2.3f;
    private static final float RY = 2.3f;
    private static final float RZ = 2.35f;
    private static final float DROP = 0.4f;
    private static final float HS = 1.12f;
    private static final float NY = 24.9f;
    private static final float NZ = -0.3f;
    private static final float PPU = 55.652176f;
    private static final float TOP = 29.0f;
    private static final int PLEATS = 16;
    private static final float[][] FRINGE;
    private static final float[][] CROWN;

    public MikaDesign() {
        super("mika", "Mika");
    }

    @Override
    protected void define(CharacterRig var1) {
        var1.scale(1.0f).outline(-12967400, 0.12f).occlusion(0.3f);
        MikaDesign.body(var1);
        MikaDesign.head(var1);
        MikaDesign.hair(var1);
        MikaDesign.ears(var1);
        MikaDesign.arms(var1);
        MikaDesign.legs(var1);
        MikaDesign.tail(var1);
    }

    public static Sdf3D toHead(Sdf3D var0) {
        return var0.move(0.0f, -0.4f, 0.0f).scale(1.12f, 0.0f, 24.9f, -0.3f);
    }

    public static float wx(float var0) {
        return var0 * 1.12f;
    }

    public static float wy(float var0) {
        return 24.9f + (var0 - 0.4f - 24.9f) * 1.12f;
    }

    public static float wz(float var0) {
        return -0.3f + (var0 - -0.3f) * 1.12f;
    }

    public static float u(float var0) {
        return 128.0f + var0 * 55.652176f;
    }

    public static float v(float var0) {
        return (29.0f - var0) * 55.652176f;
    }

    public static float capZ(float var0, float var1, float var2) {
        float var3 = var0 / (2.72f + var2);
        float var4 = (var1 - 28.25f) / (2.8f + var2);
        return -0.28f + (2.85f + var2) * (float)Math.sqrt(Math.max(0.02f, 1.0f - var3 * var3 - var4 * var4));
    }

    public static float[] onCap(float var0, float var1, float var2, float var3) {
        float var4 = (float)Math.sqrt(var0 * var0 + var1 * var1 + var2 * var2);
        float var5 = (var0 /= var4) / (2.72f + var3);
        float var6 = (var1 /= var4) / (2.8f + var3);
        float var7 = (var2 /= var4) / (2.85f + var3);
        float var8 = 1.0f / (float)Math.sqrt(var5 * var5 + var6 * var6 + var7 * var7);
        float[] var10000 = new float[]{var0 * var8, 28.25f + var1 * var8, -0.28f + var2 * var8};
        return var10000;
    }

    public static Sdf3D crownClump(float[] var0, float var1) {
        float var2 = 0.15f;
        float var3 = 0.93f;
        float var4 = -0.35f;
        float[] var5 = new float[16];
        float[] var10000 = new float[]{0.0f, 0.38f, 0.72f, 1.0f};
        float[] var6 = var10000;
        var10000 = new float[]{0.2f, 0.85f * var1, 0.8f * var1, 0.3f};
        float[] var7 = var10000;
        var10000 = new float[]{-0.12f, -0.07f, -0.07f, -0.14f};
        float[] var8 = var10000;
        for (int var9 = 0; var9 < 4; ++var9) {
            float[] var10 = MikaDesign.onCap(var2 + (var0[0] - var2) * var6[var9], var3 + (var0[1] - var3) * var6[var9], var4 + (var0[2] - var4) * var6[var9], var8[var9]);
            var5[var9 * 4] = var10[0];
            var5[var9 * 4 + 1] = var10[1];
            var5[var9 * 4 + 2] = var10[2];
            var5[var9 * 4 + 3] = var7[var9];
        }
        return SdfShapes.tube(var5).flattenFrom(3.2f, 0.0f, 28.25f, -0.28f);
    }

    public static float surf(float var0, float var1, float var2) {
        float var3 = var0 / (2.3f + var2);
        float var4 = (var1 - 27.95f) / (2.3f + var2);
        return -0.15f + (2.35f + var2) * (float)Math.sqrt(Math.max(0.02f, 1.0f - var3 * var3 - var4 * var4));
    }

    public static void head(CharacterRig var0) {
        Sdf3D var1 = SdfShapes.ellipsoid(0.0f, 27.95f, -0.15f, 2.3f, 2.3f, 2.35f);
        Sdf3D var2 = SdfShapes.ellipsoid(0.0f, 26.72f, 0.35f, 1.93f, 1.3f, 1.7f);
        Sdf3D var3 = SdfShapes.cone(0.0f, 26.2f, 0.45f, 1.2f, 0.0f, 25.25f, 0.98f, 0.3f);
        Sdf3D[] var10000 = new Sdf3D[]{SdfShapes.box(0.0f, 29.0f, -1.8f, 3.0f, 2.3f, 1.5f), SdfShapes.box(0.0f, 30.3f, 0.5f, 3.0f, 1.3f, 3.0f)};
        Sdf3D var4 = SdfShapes.union(var10000);
        float var7 = 0.7f;
        Sdf3D[] var10001 = new Sdf3D[]{var1, var2, var3};
        Sdf3D var5 = SdfShapes.subtract(SdfShapes.blend(var7, var10001), var4).color(-9532);
        var0.part(CharacterBone.HEAD).pivot(0.0f, 24.9f, -0.3f).shape(MikaDesign.toHead(var5)).detail(1.5f).flatLight(1.0f).occlusion(0.2f).outline(-5740966, 0.1f).decal(MikaDesign.face());
        SurfacePaint var6 = SurfacePaint.vertical(-9532, 23.7f, -1331558, 24.3f).flatLight(1.0f).occlusion(0.25f);
        var0.extra("neck", CharacterBone.BODY).shape(SdfShapes.capsule(0.0f, 23.1f, -0.28f, 0.0f, 25.2f, -0.2f, 0.62f).paint(var6)).outline(-5740966, 0.08f);
    }

    public static void hair(CharacterRig var0) {
        SurfacePaint var1 = SurfacePaint.field(MikaDesign::hairColor);
        Sdf3D var2 = SdfShapes.ellipsoid(0.0f, 28.25f, -0.28f, 2.72f, 2.8f, 2.85f).intersect(SdfShapes.plane(0.0f, 27.4f, 1.2f, 0.0f, -0.6f, 1.0f)).intersect(SdfShapes.above(26.4f));
        Sdf3D[] var3 = new Sdf3D[CROWN.length];
        for (int var4 = 0; var4 < CROWN.length; ++var4) {
            var3[var4] = MikaDesign.crownClump(CROWN[var4], var4 % 2 == 0 ? 1.0f : 0.9f);
        }
        float var10000 = 0.3f;
        Sdf3D[] var10001 = new Sdf3D[]{var2, SdfShapes.union(var3)};
        Sdf3D var16 = SdfShapes.blend(var10000, var10001);
        CharacterPartBuilder var22 = var0.extra("hair", CharacterBone.HEAD);
        var10001 = new Sdf3D[]{var16, MikaDesign.backLock(180.0f, 24.95f, 1.05f, 0.25f)};
        CharacterPartBuilder var5 = var22.shape(MikaDesign.toHead(SdfShapes.union(var10001).paint(var1))).detail(0.9f).resolution(0.95f).flatLight(0.5f).occlusion(0.3f).outline(-6142180, 0.09f);
        float[][] var23 = new float[6][];
        int var10002 = 0;
        float[] var10003 = new float[]{-54.0f, 25.95f, 0.9f, 0.3f};
        var23[var10002] = var10003;
        var10002 = 1;
        var10003 = new float[]{-75.0f, 25.45f, 1.0f, -0.3f};
        var23[var10002] = var10003;
        var10002 = 2;
        var10003 = new float[]{-97.0f, 25.8f, 1.05f, 0.25f};
        var23[var10002] = var10003;
        var10002 = 3;
        var10003 = new float[]{-119.0f, 25.3f, 1.05f, -0.2f};
        var23[var10002] = var10003;
        var10002 = 4;
        var10003 = new float[]{-140.0f, 25.5f, 1.05f, 0.3f};
        var23[var10002] = var10003;
        var10002 = 5;
        var10003 = new float[]{-161.0f, 25.1f, 1.0f, -0.3f};
        var23[var10002] = var10003;
        float[][] var6 = var23;
        ArrayList<Sdf3D> var7 = new ArrayList<Sdf3D>();
        float[][] var8 = var6;
        int var9 = var6.length;
        for (int var10 = 0; var10 < var9; ++var10) {
            float[] var11 = var8[var10];
            var7.add(MikaDesign.backLock(var11[0], var11[1], var11[2], var11[3]));
        }
        var7.add(MikaDesign.topLock(-84.0f, 26.25f));
        var7.add(MikaDesign.topLock(-128.0f, 26.5f));
        CharacterPartBuilder var17 = var0.extra("hair_r", CharacterBone.HEAD).pivot(0.0f, MikaDesign.wy(29.8f), MikaDesign.wz(-0.4f)).shape(MikaDesign.toHead(SdfShapes.union(var7.toArray(new Sdf3D[0])).paint(var1))).detail(1.0f).resolution(0.85f).flatLight(0.5f).occlusion(0.35f).outline(-6142180, 0.09f).motion(BoneMotionProfile.hair().idle(1.2f, 0.0f, 1.0f, 0.35f).walk(2.5f, 0.0f, 2.0f, 2.0f).drag(6.0f).hang(0.2f).turn(5.0f, 0.0f).spring(60.0f, 8.0f).bend(1.6f));
        var0.extra("hair_l", CharacterBone.HEAD).mirrorOf(var17);
        Sdf3D[] var18 = new Sdf3D[FRINGE.length];
        for (int var19 = 0; var19 < FRINGE.length; ++var19) {
            var18[var19] = MikaDesign.fringeLock(FRINGE[var19]);
        }
        Sdf3D var20 = SdfShapes.ellipsoid(0.0f, 28.25f, -0.28f, 2.56f, 2.64f, 2.69f).intersect(SdfShapes.above(29.3f));
        Sdf3D var21 = SdfShapes.subtract(SdfShapes.union(var18), var20);
        CharacterPartBuilder var12 = var0.extra("fringe", CharacterBone.HEAD).shape(MikaDesign.toHead(var21.paint(SurfacePaint.vertical(-15750, 26.9f, -681403, 29.2f)))).detail(1.3f).resolution(0.5f).flatLight(0.5f).occlusion(0.25f).outline(-6142180, 0.08f);
        MikaDesign.shine(var5, var12);
        MikaDesign.clip(var0);
        float[] var24 = new float[]{-1.95f, 29.5f, 0.85f, 0.42f, -2.36f, 28.4f, 1.05f, 0.56f, -2.42f, 27.1f, 1.08f, 0.6f, -2.3f, 25.9f, 1.02f, 0.5f, -2.26f, 25.0f, 0.98f, 0.34f, -2.5f, 24.45f, 0.95f, 0.05f};
        TaperedTubeSdf3D var13 = SdfShapes.tube(var24).flatten(2.3f, -0.55f, 0.0f, 0.83f);
        CharacterPartBuilder var14 = var0.extra("lock_r", CharacterBone.HEAD).pivot(MikaDesign.wx(-1.95f), MikaDesign.wy(29.4f), MikaDesign.wz(0.85f)).shape(MikaDesign.toHead(MikaDesign.pointed(var13, -2.5f, 24.45f, 0.95f, 0.4f, 1.0f, 0.3f).paint(var1))).detail(0.8f).resolution(0.55f).flatLight(0.5f).occlusion(0.35f).outline(-6142180, 0.09f).motion(BoneMotionProfile.hair().drag(10.0f).hang(0.35f));
        var0.extra("lock_l", CharacterBone.HEAD).mirrorOf(var14);
        float[] var25 = new float[]{0.05f, 30.8f, 0.1f, 0.24f, 0.03f, 31.45f, 0.2f, 0.26f, 0.3f, 32.05f, 0.32f, 0.22f, 0.8f, 32.38f, 0.42f, 0.17f, 1.34f, 32.35f, 0.5f, 0.11f, 1.72f, 32.02f, 0.55f, 0.05f};
        TaperedTubeSdf3D var15 = SdfShapes.tube(var25).flatten(1.6f, 0.0f, 0.0f, 1.0f);
        var0.extra("ahoge", CharacterBone.HEAD).pivot(MikaDesign.wx(0.05f), MikaDesign.wy(30.85f), MikaDesign.wz(0.1f)).shape(MikaDesign.toHead(var15.paint(SurfacePaint.vertical(-681403, 31.2f, -15750, 32.1f)))).resolution(0.5f).detail(0.9f).flatLight(0.5f).occlusion(0.3f).outline(-6142180, 0.08f).motion(BoneMotionProfile.hair().idle(4.0f, 0.0f, 6.0f, 0.5f).walk(8.0f, 0.0f, 8.0f, 2.0f).drag(6.0f).hang(0.3f).spring(70.0f, 5.0f).bend(1.2f));
    }

    public static int hairColor(float var0, float var1, float var2) {
        int var3 = ArgbColors.mix(-15750, -681403, MikaDesign.smooth(25.0f, 27.6f, var1));
        float var4 = (float)Math.sqrt(var0 * var0 + (var2 + 0.25f) * (var2 + 0.25f));
        float var5 = (1.0f - MikaDesign.smooth(1.9f, 2.55f, var4)) * (1.0f - MikaDesign.smooth(26.4f, 27.6f, var1));
        return ArgbColors.mix(var3, -3647457, var5 * 0.75f);
    }

    public static Sdf3D fringeLock(float[] var0) {
        float var1 = var0[0] < 0.7f ? 0.52f : (var0[9] > 0.0f ? 0.4f : 0.28f);
        float var10000 = var0[0];
        float var10001 = 0.05f;
        float[] var10002 = new float[]{var0[1], var0[2], MikaDesign.capZ(var0[1], var0[2], -0.3f), var0[3], var0[4], MikaDesign.capZ(var0[3], var0[4], 0.1f), var0[5], var0[6], MikaDesign.surf(var0[5], var0[6], var1), var0[7], var0[8], MikaDesign.surf(var0[7], var0[8], 0.12f)};
        TaperedTubeSdf3D var2 = SdfShapes.strand(var10000, var10001, var10002).flattenFrom(2.8f, 0.0f, 27.95f, -0.15f);
        float var3 = var0[5] - var0[7];
        float var4 = var0[6] - var0[8];
        float var5 = (float)Math.sqrt(var3 * var3 + var4 * var4);
        float var6 = 0.9f * (1.0f + var0[9]);
        float var7 = 0.9f * (1.0f - var0[9]);
        float var8 = 0.34f;
        float[] var12 = new float[]{var0[7], var0[8], var0[7] + (var3 /= var5) * var6 - (var4 /= var5) * var8, var0[8] + var4 * var6 + var3 * var8, var0[7] + var3 * 8.0f - var4 * 5.0f, var0[8] + var4 * 8.0f + var3 * 5.0f, var0[7] + var3 * 8.0f + var4 * 5.0f, var0[8] + var4 * 8.0f - var3 * 5.0f, var0[7] + var3 * var7 + var4 * var8, var0[8] + var4 * var7 - var3 * var8};
        Sdf2D var9 = Sdf2D.polygon(var12);
        return var2.intersect(SdfShapes.extrude(var9, 6.0f, 0.0f));
    }

    public static void clip(CharacterRig var0) {
        float var10000 = 0.08f;
        Sdf2D[] var10001 = new Sdf2D[2];
        var10001[0] = Sdf2D.ellipse(-0.1f, 0.0f, 0.5f, 0.32f);
        int var10003 = 1;
        float[] var10004 = new float[]{0.3f, 0.0f, 0.86f, 0.36f, 0.66f, 0.0f, 0.86f, -0.36f};
        var10001[var10003] = Sdf2D.polygon(var10004);
        Sdf2D var1 = Sdf2D.blend(var10000, var10001);
        Sdf3D var2 = SdfShapes.extrude(var1, 0.15f, 0.11f).scale(1.3f).paint(SurfacePaint.gradient(-3869978, 0.0f, 0.35f, 0.0f, -8398652, 0.0f, -0.35f, 0.0f));
        float var3 = 1.62f;
        float var4 = 28.7f;
        float var5 = 0.75f;
        float var6 = MikaDesign.surf(var3, var4, var5);
        float var7 = 2.3f + var5;
        float var8 = 2.3f + var5;
        float var9 = 2.35f + var5;
        float var10 = var3 / (var7 * var7);
        float var11 = (var4 - 27.95f) / (var8 * var8);
        float var12 = (var6 - -0.15f) / (var9 * var9);
        float var13 = (float)Math.sqrt(var10 * var10 + var11 * var11 + var12 * var12);
        float var14 = (float)Math.toDegrees(Math.atan2(var10, var12));
        float var15 = (float)(-Math.toDegrees(Math.asin(var11 / var13)));
        Sdf3D var16 = var2.rotate(0.0f, 0.0f, 35.0f).rotate(var15, var14, 0.0f).move(var3, var4, var6);
        Vec3 var17 = new Vec3(-0.5f, 0.08f, 0.2f).rotate(0.0f, 0.0f, 35.0f).rotate(var15, var14, 0.0f).add(var3, var4, var6);
        Vec3 var18 = new Vec3(0.0f, 0.0f, 1.0f).rotate(0.0f, 0.0f, 35.0f).rotate(var15, var14, 0.0f);
        PixelCanvas var19 = new PixelCanvas(32, 32);
        var19.fill(Sdf2D.circle(16.0f, 16.0f, 7.0f), -14796236);
        var19.fill(Sdf2D.circle(13.5f, 13.5f, 2.2f), -855638017);
        SurfaceDecal var20 = SurfaceDecal.project(var19, MikaDesign.wx(var17.x()), MikaDesign.wy(var17.y()), MikaDesign.wz(var17.z()), var18.x(), var18.y(), var18.z(), 0.0f, 1.0f, 0.0f, 0.33600003f, 0.33600003f).depth(0.25f, 0.25f);
        var0.extra("clip", CharacterBone.HEAD).shape(MikaDesign.toHead(var16)).resolution(0.4f).detail(0.5f).flatLight(0.4f).outline(-12811148, 0.1f).decal(var20);
    }

    public static void shine(CharacterPartBuilder var0, CharacterPartBuilder var1) {
        float[][] var3;
        PixelCanvas var2 = new PixelCanvas(96, 24);
        Sdf2D[] var10001 = new Sdf2D[]{Sdf2D.taper(3.0f, 12.0f, 1.0f, 48.0f, 12.0f, 17.0f), Sdf2D.taper(48.0f, 12.0f, 17.0f, 93.0f, 12.0f, 1.0f)};
        var2.fill(Sdf2D.union(var10001), -419442022, 1.5f);
        float[][] var10000 = new float[5][];
        int var10002 = 0;
        float[] var10003 = new float[]{-52.0f, 0.72f, 0.12f, 6.0f};
        var10000[var10002] = var10003;
        var10002 = 1;
        var10003 = new float[]{-24.0f, 0.9f, -0.06f, -4.0f};
        var10000[var10002] = var10003;
        var10002 = 2;
        var10003 = new float[]{4.0f, 0.66f, 0.08f, 5.0f};
        var10000[var10002] = var10003;
        var10002 = 3;
        var10003 = new float[]{27.0f, 0.82f, -0.08f, -6.0f};
        var10000[var10002] = var10003;
        var10002 = 4;
        var10003 = new float[]{57.0f, 0.7f, 0.1f, 3.0f};
        var10000[var10002] = var10003;
        float[][] var4 = var3 = var10000;
        int var5 = var3.length;
        for (int var6 = 0; var6 < var5; ++var6) {
            float[] var7 = var4[var6];
            double var8 = Math.toRadians(var7[0]);
            float var10 = (float)Math.sin(var8);
            float var11 = (float)Math.cos(var8);
            float[] var12 = MikaDesign.onCap(var10, 0.36f + 0.3f * (1.0f - var11) + var7[2], var11, 0.28f);
            float var13 = var12[0];
            float var14 = var12[1] - 28.25f;
            float var15 = var12[2] + 0.28f;
            Vec3 var16 = new Vec3(0.0f, 1.0f, 0.0f).rotate(0.0f, 0.0f, var7[3]);
            SurfaceDecal var17 = SurfaceDecal.project(var2, MikaDesign.wx(var12[0]), MikaDesign.wy(var12[1]), MikaDesign.wz(var12[2]), var13, var14 * 0.6f, var15, var16.x(), var16.y(), var16.z(), var7[1] * 1.12f, 0.2464f).depth(0.896f, 0.896f).maxAngle(80.0f).flatLight(0.8f).occlusion(0.0f);
            var0.decal(var17);
            var1.decal(var17);
        }
    }

    public static Sdf3D pointed(TaperedTubeSdf3D var0, float var1, float var2, float var3, float var4, float var5, float var6) {
        float var7 = -0.25f;
        float var8 = (float)Math.toDegrees(Math.atan2(var1, var3 - var7));
        float var9 = var5 * (1.0f + var6);
        float var10 = var5 * (1.0f - var6);
        float[] var10000 = new float[]{-5.0f, var2 + 8.0f, 5.0f, var2 + 8.0f, var4, var2 + var9, 0.0f, var2, -var4, var2 + var10};
        Sdf2D var11 = Sdf2D.polygon(var10000);
        return var0.intersect(SdfShapes.extrude(var11, 5.0f, 0.0f).rotate(0.0f, var8, 0.0f, 0.0f, 0.0f, var7));
    }

    public static Sdf3D backLock(float var0, float var1, float var2, float var3) {
        double var4 = Math.toRadians(var0);
        float var6 = (float)Math.sin(var4);
        float var7 = (float)Math.cos(var4);
        float var8 = Math.max(0.0f, Math.min(1.0f, (Math.abs(var0) - 100.0f) / 70.0f));
        float[] var10000 = new float[]{30.1f, 29.1f, 27.9f, 26.9f, (26.9f + var1) * 0.5f - 0.1f, var1};
        float[] var9 = var10000;
        var10000 = new float[]{2.04f, 2.59f, 2.7f, 2.52f, 2.42f, 2.32f};
        float[] var10 = var10000;
        var10000 = new float[]{2.14f, 2.72f, 2.83f, 2.62f, 2.48f, 2.35f};
        float[] var11 = var10000;
        var10000 = new float[]{-0.34f, -0.06f, 0.04f, 0.04f, 0.02f, -0.06f - 0.24f * var8};
        float[] var12 = var10000;
        var10000 = new float[]{0.7f, 1.05f, 1.05f, 0.95f, 0.64f, 0.06f};
        float[] var13 = var10000;
        float[] var14 = new float[24];
        for (int var15 = 0; var15 < 6; ++var15) {
            var14[var15 * 4] = var6 * (var10[var15] + var12[var15]);
            var14[var15 * 4 + 1] = var9[var15];
            var14[var15 * 4 + 2] = -0.28f + var7 * (var11[var15] + var12[var15]);
            var14[var15 * 4 + 3] = var13[var15] * (var15 == 5 ? 1.0f : var2);
        }
        return MikaDesign.pointed(SdfShapes.tube(var14).flattenFrom(2.1f, 0.0f, 27.95f, -0.15f), var14[20], var1, var14[22], 0.62f, 1.25f, var3);
    }

    public static Sdf3D topLock(float var0, float var1) {
        double var2 = Math.toRadians(var0);
        float var4 = (float)Math.sin(var2);
        float var5 = (float)Math.cos(var2);
        float[] var10000 = new float[]{2.2f, 2.8f, 2.98f, 3.0f};
        float[] var6 = var10000;
        var10000 = new float[]{29.4f, 28.2f, 27.2f, var1};
        float[] var7 = var10000;
        var10000 = new float[]{0.55f, 0.75f, 0.55f, 0.08f};
        float[] var8 = var10000;
        float[] var9 = new float[16];
        for (int var10 = 0; var10 < 4; ++var10) {
            var9[var10 * 4] = var4 * var6[var10];
            var9[var10 * 4 + 1] = var7[var10];
            var9[var10 * 4 + 2] = -0.25f + var5 * var6[var10] * 1.05f;
            var9[var10 * 4 + 3] = var8[var10];
        }
        return MikaDesign.pointed(SdfShapes.tube(var9).flattenFrom(2.2f, 0.0f, 27.95f, -0.15f), var9[12], var1, var9[14], 0.6f, 1.15f, 0.2f);
    }

    public static void ears(CharacterRig var0) {
        float[] var10000 = new float[]{-1.1f, 0.0f, 1.1f, 0.0f, 0.08f, 2.55f};
        Sdf3D var1 = SdfShapes.extrude(Sdf2D.polygon(var10000), 0.64f, 0.32f);
        var10000 = new float[]{0.6f, -0.3f, -0.62f, -0.3f, -0.15f, 2.7f, 0.2f, 2.7f};
        Sdf3D var2 = SdfShapes.extrude(Sdf2D.polygon(var10000), 1.3f, 0.2f).rotate(0.0f, 90.0f, 0.0f);
        Sdf3D var3 = SdfShapes.intersect(var1, var2).paint(SurfacePaint.vertical(-681403, 1.45f, -4697060, 2.25f));
        var10000 = new float[]{-0.66f, 0.3f, 0.68f, 0.3f, 0.07f, 1.95f};
        Sdf3D var4 = SdfShapes.extrude(Sdf2D.polygon(var10000), 0.5f, 0.14f).move(0.0f, 0.0f, 0.62f).paint(SurfacePaint.of(-22088).flatLight(0.7f));
        Sdf3D[] var10 = new Sdf3D[2];
        int var10002 = 0;
        float var10003 = 0.14f;
        float var10004 = 0.05f;
        float[] var10005 = new float[]{-0.34f, 0.28f, 0.25f, -0.46f, 0.6f, 0.44f, -0.38f, 0.9f, 0.5f};
        var10[var10002] = SdfShapes.strand(var10003, var10004, var10005);
        var10002 = 1;
        var10003 = 0.14f;
        var10004 = 0.05f;
        var10005 = new float[]{0.3f, 0.28f, 0.25f, 0.46f, 0.58f, 0.44f, 0.56f, 0.86f, 0.48f};
        var10[var10002] = SdfShapes.strand(var10003, var10004, var10005);
        Sdf3D var5 = SdfShapes.union(var10).paint(SurfacePaint.of(-5162).flatLight(0.7f));
        Sdf3D[] var11 = new Sdf3D[]{SdfShapes.subtract(0.1f, var3, var4), var5};
        Sdf3D var6 = SdfShapes.union(var11).scale(1.18f).rotate(-6.0f, -18.0f, 24.0f).move(-1.6f, 29.75f, -0.15f);
        CharacterPartBuilder var7 = var0.extra("ear_r", CharacterBone.HEAD).pivot(MikaDesign.wx(-1.6f), MikaDesign.wy(29.9f), MikaDesign.wz(-0.15f)).shape(MikaDesign.toHead(var6)).resolution(0.58f).detail(0.9f).flatLight(0.4f).outline(-6142180, 0.09f).motion(MikaDesign.earMotion(0.0f, -1.0f));
        var0.extra("ear_l", CharacterBone.HEAD).mirrorOf(var7).motion(MikaDesign.earMotion(140.0f, 1.0f));
    }

    public static BoneMotionProfile earMotion(float var0, float var1) {
        return BoneMotionProfile.none().idle(2.5f, 0.0f, 5.0f * var1, 0.55f).idlePhase(var0).walk(3.0f, 0.0f, 3.0f * var1, 2.0f).spring(90.0f, 7.0f);
    }

    public static void body(CharacterRig var0) {
        Sdf3D[] var10000 = new Sdf3D[]{SdfShapes.cone(0.0f, 22.6f, -0.2f, 1.02f, -1.95f, 22.2f, -0.2f, 0.9f), SdfShapes.cone(0.0f, 22.6f, -0.2f, 1.02f, 1.95f, 22.2f, -0.2f, 0.9f)};
        Sdf3D var1 = SdfShapes.union(var10000);
        Sdf3D var2 = SdfShapes.ellipsoid(0.0f, 20.5f, -0.1f, 2.3f, 2.4f, 1.72f);
        Sdf3D var3 = SdfShapes.ellipsoid(0.0f, 17.8f, -0.02f, 2.25f, 2.9f, 1.7f);
        Sdf3D var4 = SdfShapes.ellipsoid(0.0f, 15.25f, -0.05f, 2.45f, 1.25f, 1.86f);
        float var14 = 1.0f;
        Sdf3D[] var10001 = new Sdf3D[]{var1, var2, var3, var4};
        Sdf3D var5 = SdfShapes.blend(var14, var10001).paint(SurfacePaint.field(MikaDesign::hoodie));
        Sdf3D var15 = SdfShapes.ellipsoid(0.0f, 16.3f, -0.02f, 2.6f, 2.9f, 2.02f);
        float[] var20 = new float[]{-1.6f, 17.3f, 1.6f, 17.3f, 2.15f, 14.75f, -2.15f, 14.75f};
        Sdf3D var6 = SdfShapes.intersect(var15, SdfShapes.extrude(Sdf2D.polygon(var20), 2.0f, 0.0f).move(0.0f, 0.0f, 2.0f)).color(-4200487);
        Sdf3D var7 = SdfShapes.extrude(Sdf2D.ellipse(0.0f, 0.0f, 2.42f, 1.8f), 0.4f, 0.3f).rotate(90.0f, 0.0f, 0.0f).move(0.0f, 14.2f, -0.05f).color(-5710645);
        CharacterPartBuilder var16 = var0.part(CharacterBone.BODY).pivot(0.0f, 23.4f, -0.1f);
        var10001 = new Sdf3D[2];
        int var10003 = 0;
        float var10004 = 0.25f;
        Sdf3D[] var10005 = new Sdf3D[]{var5, var6};
        var10001[var10003] = SdfShapes.blend(var10004, var10005);
        var10001[1] = var7;
        var16.shape(SdfShapes.union(var10001)).resolution(1.15f).detail(1.0f).outline(-10774650, 0.12f).occlusion(0.3f).flatLight(0.2f).decal(MikaDesign.pocketDecal()).decal(MikaDesign.pawDecal()).decal(MikaDesign.hemRib());
        var0.extra("collar", CharacterBone.BODY).shape(MikaDesign.rim().paint(SurfacePaint.field(MikaDesign::rimColor))).resolution(0.7f).detail(0.8f).occlusion(0.3f).flatLight(0.2f).outline(-10774650, 0.1f);
        float var17 = 0.8f;
        var10001 = new Sdf3D[]{SdfShapes.ellipsoid(0.0f, 21.7f, -1.85f, 2.2f, 2.2f, 0.85f), SdfShapes.cone(0.0f, 20.6f, -2.3f, 0.8f, 0.0f, 19.1f, -2.2f, 0.28f)};
        Sdf3D var8 = SdfShapes.blend(var17, var10001);
        Sdf3D var9 = SdfShapes.ellipsoid(0.0f, 0.0f, 0.0f, 1.55f, 0.35f, 0.85f).rotate(-26.6f, 0.0f, 0.0f).move(0.0f, 22.95f, -1.95f);
        Sdf3D var10 = SdfShapes.subtract(0.15f, var8.intersect(SdfShapes.plane(0.0f, 22.7f, -2.2f, 0.0f, 1.0f, -0.5f)), var9).paint(SurfacePaint.field(MikaDesign::hoodColor));
        var0.extra("hood", CharacterBone.BODY).shape(var10).resolution(0.85f).detail(0.6f).occlusion(0.3f).flatLight(0.2f).outline(-10774650, 0.12f);
        var0.extra("skirt", CharacterBone.BODY).pivot(0.0f, 14.6f, 0.0f).shape(MikaDesign.skirt()).resolution(0.9f).detail(0.8f).occlusion(0.35f).outline(-14999232, 0.12f).decal(MikaDesign.skirtStripe()).motion(BoneMotionProfile.none().walk(0.0f, 8.0f, 0.0f, 1.0f).walkPhase(-90.0f).drag(2.0f).spring(80.0f, 10.0f));
        float[] var18 = new float[]{-0.32f, 23.5f, 1.45f, 0.13f, -0.38f, 22.6f, 1.75f, 0.13f, -0.44f, 21.3f, 1.95f, 0.13f, -0.48f, 20.1f, 2.0f, 0.12f};
        Sdf3D var11 = SdfShapes.tube(var18).color(-264984);
        Sdf3D var12 = SdfShapes.sphere(-0.48f, 19.6f, 2.05f, 0.45f).paint(SurfacePaint.radial(-3920, -0.62f, 19.8f, 2.35f, 0.6f, -867761));
        CharacterPartBuilder var19 = var0.extra("cord_r", CharacterBone.BODY).pivot(-0.32f, 23.5f, 1.45f);
        var10001 = new Sdf3D[]{var11, var12};
        CharacterPartBuilder var13 = var19.shape(SdfShapes.union(var10001)).resolution(0.5f).detail(0.5f).outline(-10774650, 0.08f).decal(MikaDesign.bellDecal(-0.48f)).motion(BoneMotionProfile.none().rest(-3.0f, 0.0f, 0.0f).idle(1.5f, 0.0f, 2.0f, 0.5f).walk(5.0f, 0.0f, 6.0f, 2.0f).drag(-4.0f).hang(0.9f).spring(60.0f, 6.0f).bend(0.8f));
        var0.extra("cord_l", CharacterBone.BODY).mirrorOf(var13);
    }

    public static Sdf3D rim() {
        float[] var10000 = new float[]{0.02f, 23.64f, 1.44f, 0.36f, -0.5f, 23.84f, 1.3f, 0.41f, -1.12f, 24.08f, 0.8f, 0.43f, -1.42f, 24.2f, -0.18f, 0.43f, -1.1f, 24.3f, -1.14f, 0.43f, 0.1f, 24.34f, -1.68f, 0.43f};
        return SdfShapes.tube(var10000).flattenFrom(1.25f, 0.0f, 24.1f, -0.25f).mirrorX();
    }

    public static int hoodie(float var0, float var1, float var2) {
        float var3 = (float)Math.sqrt(var0 * var0 + (var2 + 0.2f) * (var2 + 0.2f));
        float var4 = MikaDesign.smooth(22.4f, 23.3f, var1) * (1.0f - MikaDesign.smooth(1.7f, 2.3f, var3)) * 0.6f;
        float var5 = (1.0f - MikaDesign.smooth(14.6f, 15.3f, var1)) * 0.55f;
        return ArgbColors.mix(-4200487, -7092297, Math.max(var4, var5));
    }

    public static int rimColor(float var0, float var1, float var2) {
        float var3 = (float)Math.sqrt(var0 * var0 + (var2 + 0.2f) * (var2 + 0.2f));
        float var4 = MikaDesign.smooth(0.28f, 0.36f, var1 - 24.0f + 0.22f * var2);
        return ArgbColors.mix(-4200487, -744532, Math.max(var4, 1.0f - MikaDesign.smooth(1.12f, 1.38f, var3)));
    }

    public static int hoodColor(float var0, float var1, float var2) {
        float var3 = var0 / 2.2f;
        float var4 = (var1 - 21.7f) / 2.2f;
        float var5 = (var2 + 1.85f) / 0.85f;
        float var6 = (var1 - 22.7f - 0.5f * (var2 + 2.2f)) * 0.894f;
        return var6 > -0.5f && var3 * var3 + var4 * var4 + var5 * var5 < 0.8f ? -744532 : -4200487;
    }

    public static Sdf3D skirt() {
        return SdfShapes.field(MikaDesign::skirtDistance, -3.3f, 11.8f, -2.8f, 3.3f, 14.4f, 2.8f).paint(SurfacePaint.field(MikaDesign::skirtColor));
    }

    public static float flare(float var0) {
        float var1 = Math.max(0.0f, Math.min(1.0f, (14.0f - var0) / 2.1f));
        return var1 * (2.0f - var1);
    }

    public static float pleat(float var0, float var1, float var2) {
        float var3 = 2.2f + 0.75f * var2;
        float var4 = 1.66f + 0.84f * var2;
        double var5 = Math.atan2(var0 / var3, var1 / var4) * 16.0 / (Math.PI * 2);
        var5 -= Math.floor(var5);
        return (float)Math.abs(2.0 * var5 - 1.0);
    }

    public static float skirtDistance(float var0, float var1, float var2) {
        float var3 = MikaDesign.flare(var1);
        float var4 = 2.2f + 0.75f * var3;
        float var5 = 1.66f + 0.84f * var3;
        float var6 = var0 / var4;
        float var7 = var2 / var5;
        float var8 = (float)Math.sqrt(var6 * var6 + var7 * var7);
        float var9 = (var8 - 1.0f) * Math.min(var4, var5) * 0.72f - 0.2f * var3 * (MikaDesign.pleat(var0, var2, var3) - 0.5f);
        return Math.max(var9, Math.max(var1 - 14.3f, 11.9f - var1));
    }

    public static int skirtColor(float var0, float var1, float var2) {
        float var3 = MikaDesign.flare(var1);
        return ArgbColors.mix(-14275250, -12892555, 0.45f + 0.55f * (1.0f - var3 + var3 * MikaDesign.pleat(var0, var2, var3)));
    }

    public static SurfaceDecal skirtStripe() {
        PixelCanvas var0 = new PixelCanvas(512, 24);
        var0.fill(Sdf2D.box(256.0f, 12.0f, 256.0f, 7.0f, 0.0f), -5839921);
        return SurfaceDecal.wrap(var0, 0.0f, 12.35f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 360.0f, 0.16f).radius(2.2f, 3.2f);
    }

    public static void arms(CharacterRig var0) {
        Sdf3D var1 = SdfShapes.sphere(-2.72f, 21.95f, -0.15f, 0.95f);
        Sdf3D var2 = SdfShapes.cone(-2.85f, 21.6f, -0.15f, 0.9f, -3.35f, 18.4f, -0.2f, 0.88f);
        Sdf3D var3 = SdfShapes.sphere(-3.38f, 18.3f, -0.18f, 0.9f);
        Sdf3D var4 = SdfShapes.cone(-3.38f, 18.2f, -0.18f, 0.9f, -3.72f, 15.0f, 0.45f, 1.12f);
        float var10000 = 0.4f;
        Sdf3D[] var10001 = new Sdf3D[]{var1, var2, var3, var4};
        Sdf3D var5 = SdfShapes.blend(var10000, var10001).paint(SurfacePaint.field(MikaDesign::sleeve));
        Sdf3D var6 = SdfShapes.cylinder(-3.74f, 15.05f, 0.47f, -3.8f, 13.4f, 0.69f, 0.9f, 0.28f).paint(SurfacePaint.field(MikaDesign::cuff));
        var10000 = 0.15f;
        var10001 = new Sdf3D[]{var5, var6};
        Sdf3D var7 = SdfShapes.blend(var10000, var10001);
        CharacterPartBuilder var8 = var0.part(CharacterBone.RIGHT_ARM).pivot(-2.72f, 21.95f, -0.15f).grip(-3.8f, 12.9f, 0.9f).shape(var7).detail(0.9f).resolution(1.05f).outline(-10774650, 0.12f).occlusion(0.3f).flatLight(0.2f).swing(0.65f, 40.0f).joint(-3.38f, 18.3f, -0.18f, 0.7f).decal(MikaDesign.cuffRib());
        var0.part(CharacterBone.LEFT_ARM).mirrorOf(var8);
        CharacterPartBuilder var9 = var0.extra("hand_r", CharacterBone.RIGHT_ARM).shape(MikaDesign.hand()).resolution(0.4f).detail(2.6f).flatLight(1.0f).occlusion(0.25f).outline(-5740966, 0.08f);
        var0.extra("hand_l", CharacterBone.LEFT_ARM).mirrorOf(var9);
    }

    public static int sleeve(float var0, float var1, float var2) {
        float var3 = Math.max(0.0f, Math.min(1.0f, (21.9f - var1) / 6.9f));
        float var4 = MikaDesign.smooth(0.25f, 0.85f, var0 + 2.8f + 0.94f * var3) * 0.5f;
        float var5 = (1.0f - MikaDesign.smooth(0.2f, 0.55f, Math.abs(var1 - 18.25f))) * MikaDesign.smooth(0.2f, 0.7f, var2) * 0.45f;
        return ArgbColors.mix(-4200487, -7092297, Math.max(var4, var5));
    }

    public static int cuff(float var0, float var1, float var2) {
        float var3 = -0.06f;
        float var4 = -1.65f;
        float var5 = 0.22f;
        float var6 = (float)Math.sqrt(var3 * var3 + var4 * var4 + var5 * var5);
        float var7 = var0 + 3.8f;
        float var8 = var1 - 13.4f;
        float var9 = var2 - 0.69f;
        float var10 = -(var7 * var3 + var8 * var4 + var9 * var5) / var6;
        float var11 = var7 + var10 * var3 / var6;
        float var12 = var8 + var10 * var4 / var6;
        float var13 = var9 + var10 * var5 / var6;
        float var14 = (float)Math.sqrt(var11 * var11 + var12 * var12 + var13 * var13);
        return ArgbColors.mix(-5710645, -9461870, (1.0f - MikaDesign.smooth(0.04f, 0.12f, var10)) * (1.0f - MikaDesign.smooth(0.55f, 0.68f, var14)));
    }

    public static Sdf3D hand() {
        float[][] var10000 = new float[3][];
        int var10002 = 0;
        float[] var10003 = new float[]{-3.47f, 0.86f, 13.06f};
        var10000[var10002] = var10003;
        var10002 = 1;
        var10003 = new float[]{-3.79f, 0.95f, 12.97f};
        var10000[var10002] = var10003;
        var10002 = 2;
        var10003 = new float[]{-4.1f, 0.86f, 13.04f};
        var10000[var10002] = var10003;
        float[][] var0 = var10000;
        Sdf3D[] var1 = new Sdf3D[var0.length];
        for (int var2 = 0; var2 < var0.length; ++var2) {
            var1[var2] = SdfShapes.capsule(var0[var2][0], 13.5f, var0[var2][1] - 0.12f, var0[var2][0], var0[var2][2], var0[var2][1], 0.16f);
        }
        Sdf3D var3 = SdfShapes.capsule(-3.36f, 13.48f, 0.64f, -3.3f, 13.2f, 0.76f, 0.15f);
        Sdf3D[] var4 = new Sdf3D[]{SdfShapes.union(var1), var3};
        return SdfShapes.union(var4).paint(SurfacePaint.of(-9532));
    }

    public static void legs(CharacterRig var0) {
        Sdf3D var1 = SdfShapes.sphere(-1.12f, 13.2f, -0.05f, 1.0f);
        Sdf3D var2 = SdfShapes.cone(-1.18f, 12.8f, 0.0f, 1.13f, -1.1f, 7.9f, 0.1f, 0.88f);
        Sdf3D var3 = SdfShapes.sphere(-1.08f, 7.45f, 0.12f, 0.82f);
        Sdf3D var4 = SdfShapes.sphere(-1.07f, 7.5f, 0.48f, 0.5f);
        float[] var10000 = new float[]{-1.08f, 7.2f, 0.1f, 0.84f, -1.1f, 5.6f, -0.15f, 0.93f, -1.13f, 3.6f, -0.02f, 0.64f, -1.15f, 2.2f, 0.0f, 0.52f};
        TaperedTubeSdf3D var5 = SdfShapes.tube(var10000);
        float var9 = 0.45f;
        Sdf3D[] var10001 = new Sdf3D[]{var1, var2, var3, var4, var5};
        Sdf3D var6 = SdfShapes.blend(var9, var10001).paint(SurfacePaint.field(MikaDesign::tights));
        CharacterPartBuilder var7 = var0.part(CharacterBone.RIGHT_LEG).pivot(-1.2f, 13.7f, 0.0f).shape(var6).detail(0.85f).resolution(1.1f).outline(-15068908, 0.12f).occlusion(0.35f).swing(0.55f, 30.0f).joint(-1.08f, 7.45f, 0.12f, 0.8f);
        var0.part(CharacterBone.LEFT_LEG).mirrorOf(var7);
        CharacterPartBuilder var8 = var0.extra("shoe_r", CharacterBone.RIGHT_LEG).shape(MikaDesign.sneaker().rotate(0.0f, -7.0f, 0.0f, -1.2f, 0.0f, 0.2f)).resolution(0.75f).detail(1.0f).outline(-6659470, 0.12f).decal(MikaDesign.laces());
        var0.extra("shoe_l", CharacterBone.LEFT_LEG).mirrorOf(var8);
    }

    public static int tights(float var0, float var1, float var2) {
        double var3 = Math.atan2(-(var0 + 1.1f), var2 - 0.02f) - 0.35;
        float var5 = MikaDesign.smooth(0.6f, 0.95f, (float)Math.cos(var3)) * MikaDesign.smooth(2.4f, 4.5f, var1) * 0.5f;
        float var6 = (float)Math.sqrt((var1 - 7.5f) * (var1 - 7.5f) + (var0 + 1.07f) * (var0 + 1.07f) * 1.4f);
        float var7 = (1.0f - MikaDesign.smooth(0.3f, 0.8f, var6)) * MikaDesign.smooth(0.2f, 0.7f, var2) * 0.55f;
        return ArgbColors.mix(-12965330, -9808288, Math.max(var5, var7));
    }

    public static Sdf3D sneaker() {
        float var10000 = 0.25f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.box(-1.2f, 0.3f, 0.5f, 0.9f, 0.3f, 1.84f, 0.26f), SdfShapes.ellipsoid(-1.2f, 0.36f, 2.05f, 0.82f, 0.34f, 0.5f)};
        Sdf3D var0 = SdfShapes.blend(var10000, var10001).intersect(SdfShapes.above(0.0f)).paint(SurfacePaint.of(-264207).band(-1011800, 0.0f, 0.46f, 0.0f, 0.0f, 1.0f, 0.0f, 0.07f, 0.05f));
        Sdf3D var1 = SdfShapes.ellipsoid(-1.2f, 0.9f, 1.15f, 0.74f, 0.55f, 1.12f);
        Sdf3D var2 = SdfShapes.ellipsoid(-1.2f, 1.28f, -0.48f, 0.75f, 0.95f, 0.84f);
        var10000 = 0.45f;
        var10001 = new Sdf3D[]{var1, var2};
        Sdf3D var3 = SdfShapes.blend(var10000, var10001).color(-542005);
        Sdf3D var4 = SdfShapes.ellipsoid(-1.2f, 0.72f, 1.86f, 0.68f, 0.36f, 0.5f).color(-264207);
        Sdf3D var5 = SdfShapes.torus(-1.2f, 2.1f, -0.4f, 0.6f, 0.26f).color(-5839921);
        Sdf3D var6 = SdfShapes.box(-1.2f, 1.75f, 0.35f, 0.42f, 0.42f, 0.3f, 0.18f).rotate(-25.0f, 0.0f, 0.0f, -1.2f, 1.75f, 0.35f).color(-207907);
        Sdf3D var7 = SdfShapes.capsule(-1.2f, 1.8f, -1.2f, -1.2f, 2.45f, -1.02f, 0.17f).color(-5839921);
        Sdf3D[] var9 = new Sdf3D[5];
        var9[0] = var0;
        int var10002 = 1;
        float var10003 = 0.12f;
        Sdf3D[] var10004 = new Sdf3D[]{var3, var4};
        var9[var10002] = SdfShapes.blend(var10003, var10004);
        var9[2] = var5;
        var9[3] = var6;
        var9[4] = var7;
        return SdfShapes.union(var9);
    }

    public static void tail(CharacterRig var0) {
        float[] var10000 = new float[]{0.0f, 13.95f, -1.45f, 0.58f, 0.15f, 13.35f, -2.7f, 0.76f, 0.6f, 13.2f, -4.1f, 0.92f, 1.6f, 14.0f, -5.15f, 1.06f, 2.6f, 15.9f, -5.5f, 1.15f, 3.2f, 18.1f, -5.15f, 1.15f, 3.35f, 19.9f, -4.35f, 1.04f, 3.0f, 21.0f, -3.45f, 0.72f, 2.55f, 21.45f, -2.85f, 0.26f};
        float[] var1 = var10000;
        CurvePath var2 = new CurvePath(var1);
        Sdf3D[] var10 = new Sdf3D[3];
        int var10002 = 0;
        float var10003 = 0.24f;
        float var10004 = 0.04f;
        float[] var10005 = new float[]{2.68f, 21.32f, -3.02f, 2.36f, 21.62f, -2.68f, 1.96f, 21.62f, -2.62f};
        var10[var10002] = SdfShapes.strand(var10003, var10004, var10005);
        var10002 = 1;
        var10003 = 0.24f;
        var10004 = 0.04f;
        var10005 = new float[]{2.68f, 21.32f, -3.02f, 2.46f, 21.6f, -2.62f, 2.42f, 21.62f, -2.2f};
        var10[var10002] = SdfShapes.strand(var10003, var10004, var10005);
        var10002 = 2;
        var10003 = 0.24f;
        var10004 = 0.04f;
        var10005 = new float[]{2.68f, 21.32f, -3.02f, 2.44f, 21.7f, -2.72f, 2.4f, 22.12f, -2.62f};
        var10[var10002] = SdfShapes.strand(var10003, var10004, var10005);
        Sdf3D var3 = SdfShapes.union(var10);
        CharacterPartBuilder var11 = var0.extra("tail", CharacterBone.BODY).pivot(0.0f, 13.95f, -1.45f);
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.tube(var1), var3};
        CharacterPartBuilder var4 = var11.shape(SdfShapes.union(var10001).paint(SurfacePaint.field(var2::tailColor))).detail(1.0f).resolution(1.1f).flatLight(0.3f).outline(-6142180, 0.1f).motion(BoneMotionProfile.tail().idle(3.0f, 14.0f, 4.0f, 0.4f).walk(4.0f, 12.0f, 3.0f, 1.0f).spring(30.0f, 4.0f).bend(1.4f));
        PixelCanvas var5 = new PixelCanvas(128, 96);
        Sdf2D[] var13 = new Sdf2D[]{Sdf2D.line(10.0f, 80.0f, 64.0f, 26.0f, 24.0f), Sdf2D.line(64.0f, 26.0f, 118.0f, 80.0f, 24.0f)};
        var5.fill(Sdf2D.union(var13), -1999082968, 6.0f);
        var10000 = new float[]{0.3f, 0.43f, 0.56f};
        for (float var9 : var10000) {
            var4.decal(var2.chevron(var5, var9));
        }
    }

    public static float smooth(float var0, float var1, float var2) {
        float var3 = Math.max(0.0f, Math.min(1.0f, (var2 - var0) / (var1 - var0)));
        return var3 * var3 * (3.0f - 2.0f * var3);
    }

    public static SurfaceDecal face() {
        PixelCanvas var0 = new PixelCanvas(256, 200);
        float var1 = 13.356522f;
        Sdf2D[] var2 = new Sdf2D[FRINGE.length];
        for (int var3 = 0; var3 < FRINGE.length; ++var3) {
            float[] var4 = FRINGE[var3];
            float var5 = var4[7] + (var4[5] - var4[7]) * 0.12f;
            float var6 = var4[8] + (var4[6] - var4[8]) * 0.12f;
            float var10002 = var4[0] * 1.7f * 55.652176f;
            float var10003 = 9.0f;
            float[] var10004 = new float[]{MikaDesign.u(var4[3]), MikaDesign.v(var4[4]) + var1, MikaDesign.u(var4[5]), MikaDesign.v(var4[6]) + var1, MikaDesign.u(var5), MikaDesign.v(var6) + var1};
            var2[var3] = Sdf2D.path(var10002, var10003, var10004);
        }
        var0.fill(Sdf2D.union(var2), -1713793398, 3.0f);
        for (int var7 = -1; var7 <= 1; var7 += 2) {
            MikaDesign.eye(var0, 128.0f + (float)var7 * 53.0f, 114.0f, var7);
        }
        var0.fill(Sdf2D.curve(125.5f, 146.5f, 128.5f, 150.5f, 131.5f, 149.0f, 2.2f, 1.4f), -1713795968);
        float var10001 = 3.0f;
        float var8 = 3.0f;
        float[] var9 = new float[]{117.0f, 177.0f, 122.5f, 182.5f, 128.0f, 178.5f, 133.5f, 182.5f, 139.0f, 177.0f};
        var0.fill(Sdf2D.path(var10001, var8, var9), -7455954);
        return SurfaceDecal.front(var0, 0.0f, MikaDesign.wy(27.203125f), MikaDesign.wz(2.1f), 5.152f, 4.0249996f).depth(1.4f, 1.4f).maxAngle(72.0f).flatLight(1.0f).occlusion(0.3f);
    }

    public static void eye(PixelCanvas var0, float var1, float var2, int var3) {
        Sdf2D var4 = Sdf2D.ellipse(var1, var2 + 10.0f, 31.0f, 40.0f).rotate((float)var3 * 12.0f, var1, var2 + 10.0f);
        Sdf2D var5 = Sdf2D.ellipse(var1, var2 + 2.0f, 26.0f, 31.0f).intersect(var4);
        float var10003 = var2 - 28.0f;
        float var10005 = var2 + 4.0f;
        int[] var10006 = new int[]{-2571046, -776};
        var0.fill(var5, ColorField2D.linear(var1, var10003, var1, var10005, var10006));
        Sdf2D var6 = Sdf2D.ellipse(var1 + (float)var3, var2 + 5.0f, 21.0f, 27.0f);
        Sdf2D var10001 = var6.intersect(var5);
        var10003 = var2 - 22.0f;
        var10005 = var2 + 32.0f;
        var10006 = new int[]{-11920890, -5748206, -1535446, -11414};
        var0.fill(var10001, ColorField2D.linear(var1, var10003, var1, var10005, var10006));
        var0.fill(Sdf2D.ellipse(var1 + (float)var3, var2 + 6.0f, 9.0f, 21.0f).intersect(var5), -1711287446, 4.0f);
        var0.fill(var6.outline(2.5f).intersect(var5), -1338371580);
        float var7 = var1 + (float)var3;
        float var8 = var2 + 6.0f;
        var0.fill(Sdf2D.circle(var7 - 42.2f, var8, 46.7f).intersect(Sdf2D.circle(var7 + 42.2f, var8, 46.7f)), -14414846);
        var0.fill(Sdf2D.ellipse(var1 - 8.0f, var2 - 8.0f, 7.5f, 9.0f).rotate(-20.0f, var1 - 8.0f, var2 - 8.0f), -1);
        var0.fill(Sdf2D.circle(var1 + 9.0f, var2 + 17.0f, 3.5f), -419430401);
        var0.fill(Sdf2D.curve(var1 - (float)var3 * 28.0f, var2 - 8.0f, var1 - (float)var3 * 2.0f, var2 - 42.0f, var1 + (float)var3 * 30.0f, var2 - 24.0f, 4.0f, 9.5f), -13887470);
        var0.fill(Sdf2D.taper(var1 + (float)var3 * 24.0f, var2 - 24.0f, 7.0f, var1 + (float)var3 * 40.0f, var2 - 36.0f, 1.5f), -13887470);
        var0.fill(Sdf2D.curve(var1 + (float)var3 * 8.0f, var2 + 34.0f, var1 + (float)var3 * 21.0f, var2 + 31.0f, var1 + (float)var3 * 26.0f, var2 + 17.0f, 1.2f, 3.0f), -866506210);
        var0.fill(Sdf2D.curve(var1 - (float)var3 * 17.0f, var2 - 38.0f, var1 + (float)var3 * 2.0f, var2 - 48.0f, var1 + (float)var3 * 24.0f, var2 - 38.0f, 1.5f, 2.2f), 1721786442);
        var0.fill(Sdf2D.curve(var1 - (float)var3 * 16.0f, var2 - 50.0f, var1 + (float)var3 * 2.0f, var2 - 58.0f, var1 + (float)var3 * 22.0f, var2 - 53.0f, 3.0f, 2.0f), -4691398);
        float var9 = var1 + (float)var3 * 10.0f;
        float var10 = var2 + 42.0f;
        var0.fill(Sdf2D.ellipse(var9, var10, 20.0f, 9.0f), 1627357068, 9.0f);
        for (int var11 = 0; var11 < 3; ++var11) {
            var0.fill(Sdf2D.line(var9 - 8.0f + (float)var11 * 7.0f, var10 - 4.0f, var9 - 12.0f + (float)var11 * 7.0f, var10 + 4.0f, 1.6f), 1894277230);
        }
    }

    public static SurfaceDecal pocketDecal() {
        PixelCanvas var0 = new PixelCanvas(240, 144);
        int var1 = -1722777742;
        for (int var2 = 0; var2 < 18; ++var2) {
            var0.fill(Sdf2D.box(43.5f + (float)var2 * 9.0f, 16.5f, 2.6f, 0.75f, 0.7f), var1);
        }
        for (int var5 = 0; var5 < 23; ++var5) {
            var0.fill(Sdf2D.box(21.0f + (float)var5 * 9.0f, 129.0f, 2.6f, 0.75f, 0.7f), var1);
        }
        for (int var6 = -1; var6 <= 1; var6 += 2) {
            float var3 = 120.0f + (float)var6 * 78.0f;
            float var4 = 120.0f + (float)var6 * 102.75f;
            var0.fill(Sdf2D.taper(var3, 13.5f, 6.0f, var4, 132.0f, 3.75f), -1872719998);
            var0.fill(Sdf2D.taper(var3 + (float)var6 * 2.25f, 13.5f, 2.25f, var4 + (float)var6 * 2.25f, 132.0f, 1.5f), -1605733274);
            var0.fill(Sdf2D.taper(var3 - (float)var6 * 4.5f, 16.5f, 1.9f, var4 - (float)var6 * 4.5f, 129.0f, 1.5f), 0x60FFFFFF);
        }
        return SurfaceDecal.front(var0, 0.0f, 16.03f, 2.3f, 5.0f, 3.0f).depth(1.2f, 1.2f).maxAngle(75.0f);
    }

    public static SurfaceDecal pawDecal() {
        PixelCanvas var0 = new PixelCanvas(128, 128);
        int var1 = -877910;
        float var10000 = 8.0f;
        Sdf2D[] var10001 = new Sdf2D[]{Sdf2D.ellipse(64.0f, 90.0f, 27.0f, 19.0f), Sdf2D.circle(48.0f, 78.0f, 15.0f), Sdf2D.circle(80.0f, 78.0f, 15.0f)};
        Sdf2D var2 = Sdf2D.blend(var10000, var10001);
        var0.fill(var2, var1);
        var0.fill(Sdf2D.ellipse(28.0f, 52.0f, 11.0f, 14.0f).rotate(-25.0f, 28.0f, 52.0f), var1);
        var0.fill(Sdf2D.ellipse(51.0f, 33.0f, 11.0f, 14.5f).rotate(-8.0f, 51.0f, 33.0f), var1);
        var0.fill(Sdf2D.ellipse(77.0f, 33.0f, 11.0f, 14.5f).rotate(8.0f, 77.0f, 33.0f), var1);
        var0.fill(Sdf2D.ellipse(100.0f, 52.0f, 11.0f, 14.0f).rotate(25.0f, 100.0f, 52.0f), var1);
        return SurfaceDecal.front(var0, 1.2f, 20.6f, 1.6f, 1.5f, 1.5f).depth(1.5f, 1.5f).maxAngle(70.0f);
    }

    public static PixelCanvas ribs(int var0, int var1) {
        PixelCanvas var2 = new PixelCanvas(var1, 16);
        float var3 = (float)var1 / (float)var0;
        for (int var4 = 0; var4 < var0; ++var4) {
            float var5 = (float)var4 * var3 + var3 * 0.5f;
            var2.fill(Sdf2D.box(var5, 8.0f, var3 * 0.18f, 8.0f, 0.0f), 1078491756);
        }
        return var2;
    }

    public static SurfaceDecal hemRib() {
        return SurfaceDecal.wrap(MikaDesign.ribs(64, 506), 0.0f, 14.2f, -0.05f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 360.0f, 0.62f).radius(1.2f, 3.3f);
    }

    public static SurfaceDecal cuffRib() {
        return SurfaceDecal.wrap(MikaDesign.ribs(24, 256), -3.77f, 14.3f, 0.57f, 0.06f, 1.65f, -0.22f, 0.0f, 0.0f, 1.0f, 360.0f, 1.2f).radius(0.6f, 1.4f);
    }

    public static SurfaceDecal bellDecal(float var0) {
        PixelCanvas var1 = new PixelCanvas(64, 64);
        var1.fill(Sdf2D.box(32.0f, 30.0f, 30.0f, 2.0f, 1.0f), -5737444);
        var1.fill(Sdf2D.circle(32.0f, 40.0f, 4.5f), -11915254);
        var1.fill(Sdf2D.line(32.0f, 40.0f, 32.0f, 60.0f, 3.5f), -11915254);
        var1.fill(Sdf2D.ellipse(24.0f, 20.0f, 5.0f, 3.5f), -1426063361);
        return SurfaceDecal.front(var1, var0, 19.6f, 2.5f, 1.0f, 1.0f).depth(0.6f, 0.6f).maxAngle(70.0f);
    }

    public static SurfaceDecal laces() {
        PixelCanvas var0 = new PixelCanvas(96, 128);
        int var1 = -7710102;
        for (int var2 = 0; var2 < 4; ++var2) {
            float var3 = 30.0f + (float)var2 * 22.0f;
            var0.fill(Sdf2D.line(24.0f, var3, 72.0f, var3 + 14.0f, 6.0f), -1);
            var0.fill(Sdf2D.line(72.0f, var3, 24.0f, var3 + 14.0f, 6.0f), -1);
            var0.fill(Sdf2D.circle(20.0f, var3, 3.5f), var1);
            var0.fill(Sdf2D.circle(76.0f, var3, 3.5f), var1);
        }
        var0.fill(Sdf2D.ellipse(36.0f, 14.0f, 11.0f, 6.0f).outline(4.0f), -1);
        var0.fill(Sdf2D.ellipse(60.0f, 14.0f, 11.0f, 6.0f).outline(4.0f), -1);
        var0.fill(Sdf2D.circle(48.0f, 16.0f, 4.0f), -1);
        Vec3 var6 = new Vec3(-1.2f, 0.0f, 0.2f);
        Vec3 var7 = new Vec3(-1.2f, 1.62f, 0.72f).rotate(0.0f, -7.0f, 0.0f, var6);
        Vec3 var4 = new Vec3(0.0f, 0.8f, 0.6f).rotate(0.0f, -7.0f, 0.0f);
        Vec3 var5 = new Vec3(0.0f, 0.6f, -0.8f).rotate(0.0f, -7.0f, 0.0f);
        return SurfaceDecal.project(var0, var7.x(), var7.y(), var7.z(), var4.x(), var4.y(), var4.z(), var5.x(), var5.y(), var5.z(), 0.95f, 1.25f).depth(0.6f, 0.6f).maxAngle(65.0f);
    }

    static {
        float[][] var5 = new float[8][];
        float[] fArray = new float[10];
        float[] var21 = fArray;
        fArray[0] = 1.0f;
        var21[1] = -1.0f;
        var21[2] = 30.15f;
        var21[3] = -1.5f;
        var21[4] = 29.6f;
        var21[5] = -2.0f;
        var21[6] = 28.3f;
        var21[7] = -1.8f;
        var21[8] = 27.0f;
        var21[9] = 0.3f;
        var5[0] = var21;
        float[] fArray2 = new float[10];
        float[] var23 = fArray2;
        fArray2[0] = 0.95f;
        var23[1] = -0.42f;
        var23[2] = 30.25f;
        var23[3] = -0.75f;
        var23[4] = 29.75f;
        var23[5] = -1.05f;
        var23[6] = 28.6f;
        var23[7] = -0.85f;
        var23[8] = 27.9f;
        var23[9] = -0.35f;
        var5[1] = var23;
        float[] fArray3 = new float[10];
        float[] var25 = fArray3;
        fArray3[0] = 1.05f;
        var25[1] = -0.28f;
        var25[2] = 30.3f;
        var25[3] = 0.0f;
        var25[4] = 29.8f;
        var25[5] = 0.5f;
        var25[6] = 28.4f;
        var25[7] = 0.42f;
        var25[8] = 27.2f;
        var25[9] = 0.4f;
        var5[2] = var25;
        float[] fArray4 = new float[10];
        float[] var5x = fArray4;
        fArray4[0] = 0.95f;
        var5x[1] = 0.55f;
        var5x[2] = 30.25f;
        var5x[3] = 0.9f;
        var5x[4] = 29.75f;
        var5x[5] = 1.25f;
        var5x[6] = 28.6f;
        var5x[7] = 1.02f;
        var5x[8] = 27.65f;
        var5x[9] = -0.3f;
        var5[3] = var5x;
        float[] fArray5 = new float[10];
        float[] var29 = fArray5;
        fArray5[0] = 1.0f;
        var29[1] = 1.1f;
        var29[2] = 30.15f;
        var29[3] = 1.6f;
        var29[4] = 29.6f;
        var29[5] = 2.05f;
        var29[6] = 28.3f;
        var29[7] = 1.85f;
        var29[8] = 26.75f;
        var29[9] = 0.35f;
        var5[4] = var29;
        float[] fArray6 = new float[10];
        float[] var31 = fArray6;
        fArray6[0] = 0.65f;
        var31[1] = -0.75f;
        var31[2] = 30.2f;
        var31[3] = -1.2f;
        var31[4] = 29.6f;
        var31[5] = -1.5f;
        var31[6] = 28.3f;
        var31[7] = -1.3f;
        var31[8] = 27.45f;
        var31[9] = 0.3f;
        var5[5] = var31;
        float[] fArray7 = new float[10];
        float[] var33 = fArray7;
        fArray7[0] = 0.5f;
        var33[1] = 0.28f;
        var33[2] = 30.25f;
        var33[3] = 0.6f;
        var33[4] = 29.7f;
        var33[5] = 0.88f;
        var33[6] = 28.5f;
        var33[7] = 0.75f;
        var33[8] = 27.5f;
        var33[9] = -0.3f;
        var5[6] = var33;
        float[] fArray8 = new float[10];
        float[] var35 = fArray8;
        fArray8[0] = 0.5f;
        var35[1] = -0.45f;
        var35[2] = 30.25f;
        var35[3] = -0.4f;
        var35[4] = 29.7f;
        var35[5] = -0.3f;
        var35[6] = 28.3f;
        var35[7] = -0.18f;
        var35[8] = 27.6f;
        var35[9] = 0.3f;
        var5[7] = var35;
        FRINGE = var5;
        float[][] var7x = new float[6][];
        float[] fArray9 = new float[3];
        float[] var37 = fArray9;
        fArray9[0] = -0.413f;
        var37[1] = 0.766f;
        var37[2] = 0.492f;
        var7x[0] = var37;
        float[] fArray10 = new float[3];
        float[] var39 = fArray10;
        fArray10[0] = -0.992f;
        var39[1] = 0.087f;
        var39[2] = -0.087f;
        var7x[1] = var39;
        float[] fArray11 = new float[3];
        float[] var41 = fArray11;
        fArray11[0] = -0.539f;
        var41[1] = -0.342f;
        var41[2] = -0.77f;
        var7x[2] = var41;
        float[] fArray12 = new float[3];
        float[] var43 = fArray12;
        fArray12[0] = 0.464f;
        var43[1] = -0.375f;
        var43[2] = -0.803f;
        var7x[3] = var43;
        float[] fArray13 = new float[3];
        float[] var45 = fArray13;
        fArray13[0] = 0.975f;
        var45[1] = 0.139f;
        var45[2] = -0.172f;
        var7x[4] = var45;
        float[] fArray14 = new float[3];
        float[] var47 = fArray14;
        fArray14[0] = 0.353f;
        var47[1] = 0.788f;
        var47[2] = 0.504f;
        var7x[5] = var47;
        CROWN = var7x;
    }

    @Environment(value=EnvType.CLIENT)
    private static final class CurvePath {
        private final float[] xs;
        private final float[] ys;
        private final float[] zs;
        private final float[] len;
        private final int n;

        CurvePath(float[] var1) {
            int var2 = var1.length / 4 - 2;
            int var3 = 12;
            this.n = var2 * var3 + 1;
            this.xs = new float[this.n];
            this.ys = new float[this.n];
            this.zs = new float[this.n];
            this.len = new float[this.n];
            int var4 = 0;
            for (int var5 = 1; var5 <= var2; ++var5) {
                int var6 = (var5 - 1) * 4;
                int var7 = var5 * 4;
                int var8 = (var5 + 1) * 4;
                float var9 = var5 == 1 ? var1[0] : (var1[var6] + var1[var7]) * 0.5f;
                float var10 = var5 == 1 ? var1[1] : (var1[var6 + 1] + var1[var7 + 1]) * 0.5f;
                float var11 = var5 == 1 ? var1[2] : (var1[var6 + 2] + var1[var7 + 2]) * 0.5f;
                float var12 = var5 == var2 ? var1[var8] : (var1[var7] + var1[var8]) * 0.5f;
                float var13 = var5 == var2 ? var1[var8 + 1] : (var1[var7 + 1] + var1[var8 + 1]) * 0.5f;
                float var14 = var5 == var2 ? var1[var8 + 2] : (var1[var7 + 2] + var1[var8 + 2]) * 0.5f;
                for (int var15 = 0; var15 < var3 || var5 == var2 && var15 == var3; ++var15) {
                    float var16 = (float)var15 / (float)var3;
                    float var17 = 1.0f - var16;
                    this.xs[var4] = var17 * var17 * var9 + 2.0f * var17 * var16 * var1[var7] + var16 * var16 * var12;
                    this.ys[var4] = var17 * var17 * var10 + 2.0f * var17 * var16 * var1[var7 + 1] + var16 * var16 * var13;
                    this.zs[var4] = var17 * var17 * var11 + 2.0f * var17 * var16 * var1[var7 + 2] + var16 * var16 * var14;
                    if (var4 > 0) {
                        float var18 = this.xs[var4] - this.xs[var4 - 1];
                        float var19 = this.ys[var4] - this.ys[var4 - 1];
                        float var20 = this.zs[var4] - this.zs[var4 - 1];
                        this.len[var4] = this.len[var4 - 1] + (float)Math.sqrt(var18 * var18 + var19 * var19 + var20 * var20);
                    }
                    ++var4;
                }
            }
        }

        float param(float var1, float var2, float var3) {
            int var4 = 0;
            float var5 = Float.MAX_VALUE;
            for (int var6 = 0; var6 < this.n; ++var6) {
                float var7 = var1 - this.xs[var6];
                float var8 = var2 - this.ys[var6];
                float var9 = var3 - this.zs[var6];
                float var10 = var7 * var7 + var8 * var8 + var9 * var9;
                if (!(var10 < var5)) continue;
                var5 = var10;
                var4 = var6;
            }
            return this.len[var4] / this.len[this.n - 1];
        }

        int tailColor(float var1, float var2, float var3) {
            float var4 = this.param(var1, var2, var3);
            return ArgbColors.mix(ArgbColors.mix(-3647457, -681403, MikaDesign.smooth(0.04f, 0.22f, var4)), -3618, MikaDesign.smooth(0.72f, 0.8f, var4));
        }

        SurfaceDecal chevron(PixelCanvas var1, float var2) {
            int var4;
            float var3 = var2 * this.len[this.n - 1];
            for (var4 = 1; var4 < this.n - 2 && this.len[var4] < var3; ++var4) {
            }
            float var5 = this.xs[var4 + 1] - this.xs[var4 - 1];
            float var6 = this.ys[var4 + 1] - this.ys[var4 - 1];
            float var7 = this.zs[var4 + 1] - this.zs[var4 - 1];
            return SurfaceDecal.wrap(var1, this.xs[var4], this.ys[var4], this.zs[var4], var5, var6, var7, this.xs[var4] - 1.8f, this.ys[var4] - 17.6f, this.zs[var4] + 3.3f, 140.0f, 0.9f).radius(0.3f, 1.6f);
        }
    }
}

