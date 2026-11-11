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
import hook.feature.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class TralaleroDesign
extends CharacterDefinition {
    private static final float YJ = 17.9f;
    private static final float PIVOT_Z = -1.5f;
    private static final float SHIFT = 0.8f;
    private static final float LEG_X = 2.9f;
    private static final float HIP = 12.0f;
    private static final float LEG_Z = 3.4f;
    private static final float KNEE = 6.4f;
    private static final float MID_HIP = 14.2f;
    private static final float MID_Z = -7.0f;
    private static final float SHOE_SCALE = 1.15f;
    private static final float[] BODY_Z;
    private static final float[] BODY_Y;
    private static final float[] BODY_RY;
    private static final float[] BODY_RX;
    private static final float[] LINE_Z;
    private static final float[] LINE_Y;
    private static final float MOUTH_DEG = 152.0f;
    private static final int MOUTH_W = 284;
    private static final int MOUTH_H = 120;
    private static final float MOUTH_PX = 25.0f;
    private static final float MOUTH_Z = 9.45f;
    private static final float CORNER = 74.0f;
    private static final float LIP_FRONT = 11.0f;
    private static final float LIP_BACK = 7.4f;
    private static final int BACK_TOP = -12621165;
    private static final int BACK = -10515274;
    private static final int BELLY = -920330;
    private static final int FIN_TIP = -13346176;
    private static final int SKIN = -10055494;
    private static final int SKIN_LIGHT = -8674358;
    private static final int SKIN_DARK = -11634525;
    private static final int SHOE = -13933608;
    private static final int SHOE_DARK = -14989656;
    private static final int SHOE_LIGHT = -9593609;
    private static final int SOLE = -657935;
    private static final int SOLE_GREY = -4604218;
    private static final int LINE = -15194566;
    private static final int MOUTH = -12972264;
    private static final int GUM = -10872282;
    private static final int TEETH = -263436;

    public TralaleroDesign() {
        super("tralalero", "Tralalero Tralala");
    }

    @Override
    protected void define(CharacterRig var1) {
        var1.scale(1.0f).outline(-15194566, 0.16f).occlusion(0.5f).firstPersonArms(false);
        var1.part(CharacterBone.BODY).pivot(0.0f, 15.9f, 0.0f);
        Sdf3D var2 = SdfShapes.field(TralaleroDesign::loft, -5.8f, 11.7f, -14.6f, 5.8f, 24.099998f, 14.2f);
        Sdf3D var3 = SdfShapes.field((var0, var1x, var2x) -> TralaleroDesign.bellyLine(var2x) - var1x, -6.0f, 10.9f, -14.0f, 6.0f, 24.9f, 15.0f);
        float var10000 = 1.1f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.intersect(0.5f, var2, var3).paint(SurfacePaint.field(TralaleroDesign::skin)), TralaleroDesign.dorsal()};
        Sdf3D var4 = SdfShapes.blend(var10000, var10001).move(0.0f, 0.0f, 0.8f);
        PixelCanvas var5 = TralaleroDesign.eyeCanvas();
        PixelCanvas var6 = TralaleroDesign.gillCanvas();
        PixelCanvas var7 = TralaleroDesign.laceCanvas();
        PixelCanvas var8 = TralaleroDesign.stripeCanvas();
        CharacterPartBuilder var9 = var1.extra("shark", CharacterBone.BODY).pivot(0.0f, 17.9f, -1.5f).shape(var4).resolution(1.2f).detail(0.78f).outline(0.1f).neck(0.0f, 17.9f, 6.8f, 0.0f, 0.0f, 1.0f, 3.0f).look(0.45f, 0.45f, 28.0f).motion(BoneMotionProfile.none().idle(0.7f, 0.8f, 0.0f, 0.28f).walk(2.0f, 0.0f, 4.0f, 1.0f).follow(CharacterBone.HEAD, 0.25f).spring(70.0f, 10.0f).limit(12.0f)).decal(TralaleroDesign.eye(var5, 1.0f)).decal(TralaleroDesign.eye(var5.mirrored(), -1.0f)).decal(TralaleroDesign.gills(var6, 1.0f)).decal(TralaleroDesign.gills(var6.mirrored(), -1.0f));
        Sdf3D var10 = SdfShapes.field((var0, var1x, var2x) -> var1x - TralaleroDesign.bellyLine(var2x) - 0.4f, -6.0f, 10.9f, -14.0f, 6.0f, 24.9f, 15.0f);
        Sdf3D var11 = SdfShapes.intersect(var2.inflate(-0.15f), var10).paint(SurfacePaint.of(-920330).flatLight(0.4f).occlusion(0.35f)).move(0.0f, 0.0f, 0.8f);
        var1.extra("belly", var9).shape(var11).resolution(1.0f).detail(1.0f).decal(TralaleroDesign.mouth());
        var1.extra("tail", var9).pivot(0.0f, 19.199999f, -9.9f).shape(TralaleroDesign.caudal()).resolution(1.0f).motion(BoneMotionProfile.tail().idle(1.0f, 9.0f, 0.0f, 0.45f).walk(2.0f, 14.0f, 0.0f, 1.0f).drag(5.0f).hang(0.3f).turn(0.0f, 16.0f).spring(40.0f, 5.5f).bend(1.2f));
        CharacterPartBuilder var12 = var1.part(CharacterBone.RIGHT_ARM).pivot(-4.3f, 15.4f, 4.0f).grip(-6.8f, 13.4f, 2.7f).shape(TralaleroDesign.pectoral()).resolution(0.9f).detail(0.8f).swing(0.35f);
        var1.part(CharacterBone.LEFT_ARM).mirrorOf(var12);
        CharacterPartBuilder var13 = var1.part(CharacterBone.RIGHT_LEG).pivot(-2.9f, 12.0f, 3.4f).shape(TralaleroDesign.leg(-2.9f, 12.0f, 3.4f, -1.0f)).resolution(1.0f).detail(1.4f).swing(0.5f, 35.0f).joint(-2.8000002f, 6.5f, 3.5500002f, 0.8f);
        var1.part(CharacterBone.LEFT_LEG).mirrorOf(var13);
        CharacterPartBuilder var14 = var1.extra("shoe_r", CharacterBone.RIGHT_LEG).shape(TralaleroDesign.shoe(-2.9f, 3.4f)).resolution(1.0f).detail(1.6f).decal(TralaleroDesign.laces(var7, -2.9f, 3.4f)).decal(TralaleroDesign.stripe(var8, -2.9f, 3.4f, -1.0f));
        var1.extra("shoe_l", CharacterBone.LEFT_LEG).mirrorOf(var14);
        CharacterPartBuilder var15 = var1.extra("leg_mid", CharacterBone.BODY).pivot(0.0f, 14.2f, -7.0f).shape(TralaleroDesign.leg(0.0f, 14.2f, -7.0f, 1.0f)).resolution(1.0f).detail(1.4f).motion(BoneMotionProfile.none().follow(CharacterBone.LEFT_LEG, 0.5f).walk(22.0f, 0.0f, 0.0f, 1.0f).hang(0.5f).limit(45.0f));
        var1.extra("shoe_mid", var15).shape(TralaleroDesign.shoe(0.0f, -7.0f)).resolution(1.0f).detail(1.6f).decal(TralaleroDesign.laces(var7, 0.0f, -7.0f)).decal(TralaleroDesign.stripe(var8, 0.0f, -7.0f, -1.0f)).decal(TralaleroDesign.stripe(var8.mirrored(), 0.0f, -7.0f, 1.0f));
    }

    private static float smooth(float var0, float var1, float var2) {
        float var3 = (var2 - var0) / (var1 - var0);
        var3 = var3 < 0.0f ? 0.0f : Math.min(var3, 1.0f);
        return var3 * var3 * (3.0f - 2.0f * var3);
    }

    private static int segment(float[] var0, float var1) {
        int var2;
        for (var2 = 0; var2 < var0.length - 2 && var1 > var0[var2 + 1]; ++var2) {
        }
        return var2;
    }

    private static float spline(float[] var0, float[] var1, int var2, float var3, boolean var4) {
        int var5 = var0.length;
        float var6 = var0[var2 + 1] - var0[var2];
        float var7 = Math.max(0.0f, Math.min(1.0f, (var3 - var0[var2]) / var6));
        float var8 = var1[Math.max(0, var2 - 1)];
        float var9 = var1[var2];
        float var10 = var1[var2 + 1];
        float var11 = var1[Math.min(var5 - 1, var2 + 2)];
        float var12 = 2.0f * var8 - 5.0f * var9 + 4.0f * var10 - var11;
        float var13 = 3.0f * var9 - var8 - 3.0f * var10 + var11;
        return var4 ? 0.5f * (var10 - var8 + 2.0f * var12 * var7 + 3.0f * var13 * var7 * var7) / var6 : 0.5f * (2.0f * var9 + (var10 - var8) * var7 + var12 * var7 * var7 + var13 * var7 * var7 * var7);
    }

    private static float bellyLine(float var0) {
        return TralaleroDesign.spline(LINE_Z, LINE_Y, TralaleroDesign.segment(LINE_Z, var0), var0, false);
    }

    private static float loft(float var0, float var1, float var2) {
        int var3 = BODY_Z.length;
        if (var2 > BODY_Z[var3 - 1]) {
            float var18 = var1 - 17.9f - BODY_Y[var3 - 1];
            float var20 = var2 - BODY_Z[var3 - 1];
            return (float)Math.sqrt(var0 * var0 + var18 * var18 + var20 * var20) - BODY_RY[var3 - 1];
        }
        if (var2 < BODY_Z[0]) {
            float var17 = var1 - 17.9f - BODY_Y[0];
            float var19 = var2 - BODY_Z[0];
            return (float)Math.sqrt(var0 * var0 + var17 * var17 + var19 * var19) - BODY_RX[0];
        }
        int var4 = TralaleroDesign.segment(BODY_Z, var2);
        float var5 = 17.9f + TralaleroDesign.spline(BODY_Z, BODY_Y, var4, var2, false);
        float var6 = TralaleroDesign.spline(BODY_Z, BODY_RY, var4, var2, false);
        float var7 = TralaleroDesign.spline(BODY_Z, BODY_RX, var4, var2, false);
        float var8 = TralaleroDesign.spline(BODY_Z, BODY_RY, var4, var2, true);
        float var9 = TralaleroDesign.spline(BODY_Z, BODY_RX, var4, var2, true);
        float var10 = TralaleroDesign.spline(BODY_Z, BODY_Y, var4, var2, true);
        float var11 = Math.max(Math.abs(var9), Math.abs(var8)) + Math.abs(var10);
        float var12 = var0 / var7;
        float var13 = (var1 - var5) / var6;
        float var14 = (float)Math.sqrt(var12 * var12 + var13 * var13);
        float var15 = (float)Math.sqrt(var12 * var12 / (var7 * var7) + var13 * var13 / (var6 * var6));
        float var16 = var15 < 1.0E-6f ? -Math.min(var7, var6) : var14 * (var14 - 1.0f) / var15;
        return var16 / (float)Math.sqrt(1.0f + var11 * var11);
    }

    private static int skin(float var0, float var1, float var2) {
        return ArgbColors.mix(-10515274, -12621165, TralaleroDesign.smooth(TralaleroDesign.bellyLine(var2) + 0.5f, 23.4f, var1));
    }

    private static Sdf2D outline(float var0, float ... var1) {
        float[] var2 = (float[])var1.clone();
        for (int var3 = 1; var3 < var2.length; var3 += 2) {
            int n = var3;
            var2[n] = var2[n] + var0;
        }
        return Sdf2D.polygon(var2);
    }

    private static Sdf3D blade(Sdf2D var0, float var1, float var2, float var3, float var4) {
        Sdf3D var5 = SdfShapes.extrude(var0, var3, var3);
        float var6 = (float)Math.sqrt(1.0f + var4 * var4);
        return SdfShapes.field((var6x, var7, var8) -> {
            float var9 = (float)Math.sqrt((var6x - var1) * (var6x - var1) + (var7 - var2) * (var7 - var2));
            return Math.max(var5.distance(var6x, var7, var8), (Math.abs(var8) - var3 + var4 * var9) / var6);
        }, var0.minX(), var0.minY(), -var3, var0.maxX(), var0.maxY(), var3);
    }

    private static Sdf3D side(Sdf3D var0) {
        return TralaleroDesign.orient(var0, 0.0f, 0.0f, 0.0f, new Vec3(0.0f, 0.0f, -1.0f), Vec3.UP);
    }

    private static Sdf3D dorsal() {
        float var10000 = 17.9f;
        float[] var10001 = new float[]{-2.6f, 4.2f, -1.9f, 6.2f, -1.0f, 7.9f, 0.1f, 9.4f, 1.4f, 10.6f, 2.6f, 11.5f, 3.7f, 12.1f, 3.2f, 11.2f, 2.75f, 10.0f, 2.45f, 8.7f, 2.4f, 7.4f, 2.6f, 6.2f, 3.1f, 5.0f, 3.6f, 4.2f};
        Sdf2D var0 = TralaleroDesign.outline(var10000, var10001);
        var10000 = 17.9f;
        var10001 = new float[]{8.3f, 2.2f, 8.9f, 3.6f, 9.7f, 4.7f, 10.6f, 5.5f, 10.2f, 4.5f, 10.1f, 3.4f, 10.4f, 2.2f};
        Sdf2D var1 = TralaleroDesign.outline(var10000, var10001);
        Sdf3D[] var4 = new Sdf3D[]{TralaleroDesign.side(TralaleroDesign.blade(var0.inflate(0.1f), 0.4f, 22.5f, 1.5f, 0.125f)), TralaleroDesign.side(TralaleroDesign.blade(var1, 9.4f, 20.699999f, 0.72f, 0.14f))};
        Sdf3D var2 = SdfShapes.union(var4);
        return var2.paint(SurfacePaint.vertical(-12621165, 22.9f, -13346176, 29.4f));
    }

    private static Sdf3D caudal() {
        float var10000 = 17.9f;
        float[] var10001 = new float[]{12.0f, 1.4f, 11.7f, 2.2f, 12.4f, 3.8f, 13.2f, 5.4f, 14.0f, 7.0f, 14.8f, 8.3f, 15.4f, 9.1f, 15.0f, 7.9f, 14.6f, 6.3f, 14.3f, 4.6f, 14.1f, 3.0f, 14.2f, 1.6f, 14.4f, 0.2f, 14.7f, -1.3f, 14.95f, -2.7f, 15.2f, -3.9f, 14.5f, -3.2f, 13.6f, -2.2f, 12.8f, -1.0f, 11.9f, 0.4f};
        Sdf2D var0 = TralaleroDesign.outline(var10000, var10001);
        Sdf3D var1 = TralaleroDesign.side(TralaleroDesign.blade(var0.inflate(0.08f), 12.0f, 19.3f, 0.82f, 0.062f));
        Sdf3D var2 = SdfShapes.cone(0.0f, 19.1f, -10.3f, 1.5f, 0.0f, 19.3f, -12.5f, 1.0f);
        Sdf3D var3 = SdfShapes.ellipsoid(0.0f, 19.3f, -12.2f, 1.3f, 0.55f, 1.4f);
        var10000 = 0.7f;
        Sdf3D[] var5 = new Sdf3D[]{var2, var3, var1};
        return SdfShapes.blend(var10000, var5).paint(SurfacePaint.radial(-10515274, 0.0f, 19.3f, -12.2f, 5.5f, -13346176)).move(0.0f, 0.0f, 0.8f);
    }

    private static Sdf3D pectoral() {
        float[] var10000 = new float[]{-1.2f, 2.0f, 0.5f, 2.1f, 2.0f, 1.7f, 3.5f, 1.0f, 5.0f, 0.1f, 6.3f, -0.9f, 7.0f, -1.6f, 6.0f, -1.5f, 4.6f, -1.35f, 3.2f, -1.3f, 1.8f, -1.5f, 0.4f, -1.9f, -1.2f, -2.2f};
        Sdf2D var0 = Sdf2D.polygon(var10000);
        Sdf3D var1 = TralaleroDesign.orient(TralaleroDesign.blade(var0, 0.0f, 0.0f, 0.62f, 0.05f), -4.3f, 15.4f, 3.2f, new Vec3(-0.8f, -0.5f, -0.33f), new Vec3(0.25f, 0.05f, 1.0f));
        return var1.paint(SurfacePaint.radial(-10515274, -4.3f, 15.4f, 3.2f, 6.0f, -13346176)).move(0.0f, 0.0f, 0.8f);
    }

    private static SurfacePaint legPaint(float var0) {
        return SurfacePaint.field((var1, var2, var3) -> {
            float var4 = TralaleroDesign.smooth(var0 + 0.3f, var0 + 1.2f, var3);
            float var5 = TralaleroDesign.smooth(1.3f, 0.3f, Math.abs(var2 - 6.4f)) * TralaleroDesign.smooth(var0 - 0.2f, var0 - 0.8f, var3);
            return ArgbColors.mix(ArgbColors.mix(-10055494, -8674358, var4), -11634525, var5);
        });
    }

    private static Sdf3D leg(float var0, float var1, float var2, float var3) {
        float var4 = 6.4f;
        float var5 = var1 - 6.4f;
        Sdf3D var6 = SdfShapes.capsule(var0, var1, var2, var0, var1 + 2.8f, var2, 1.85f);
        Sdf3D var7 = SdfShapes.cone(var0, var1 + 0.3f, var2, 2.0f, var0 - var3 * 0.15f, var4 + 0.9f, var2 + 0.15f, 1.12f);
        Sdf3D var8 = SdfShapes.ellipsoid(var0 + var3 * 0.05f, var4 + 0.56f * var5, var2 + 0.85f, 1.18f, 0.42f * var5, 1.08f);
        Sdf3D var9 = SdfShapes.ellipsoid(var0 + var3 * 0.7f, var4 + 0.55f * var5, var2, 0.85f, 0.45f * var5, 1.05f);
        Sdf3D var10 = SdfShapes.ellipsoid(var0 - var3 * 0.6f, var4 + 1.35f, var2 + 0.55f, 0.8f, 1.0f, 0.78f);
        Sdf3D var11 = SdfShapes.ellipsoid(var0, var4 + 0.55f * var5, var2 - 0.7f, 1.2f, 0.45f * var5, 0.95f);
        Sdf3D var12 = SdfShapes.sphere(var0 - var3 * 0.1f, var4 + 0.1f, var2 + 0.15f, 0.9f);
        Sdf3D var13 = SdfShapes.ellipsoid(var0 - var3 * 0.05f, var4 + 0.25f, var2 + 0.95f, 0.55f, 0.62f, 0.4f);
        Sdf3D var14 = SdfShapes.cone(var0 - var3 * 0.1f, var4 - 0.3f, var2 + 0.25f, 0.86f, var0, 2.4f, var2 + 0.05f, 0.56f);
        Sdf3D var15 = SdfShapes.ellipsoid(var0 - var3 * 0.36f, var4 - 1.55f, var2 - 0.62f, 0.84f, 1.55f, 0.98f);
        Sdf3D var16 = SdfShapes.ellipsoid(var0 + var3 * 0.36f, var4 - 1.25f, var2 - 0.55f, 0.76f, 1.35f, 0.88f);
        Sdf3D var17 = SdfShapes.cone(var0, var4 - 2.7f, var2 - 0.55f, 0.5f, var0, 2.2f, var2 - 0.5f, 0.42f);
        Sdf3D var18 = SdfShapes.sphere(var0, 2.3f, var2, 0.58f);
        Sdf3D[] var10000 = new Sdf3D[2];
        var10000[0] = var6;
        int var10002 = 1;
        float var10003 = 0.3f;
        Sdf3D[] var10004 = new Sdf3D[]{var7, var8, var9, var10, var11, var12, var13, var14, var15, var16, var17, var18};
        var10000[var10002] = SdfShapes.blend(var10003, var10004);
        Sdf3D var19 = SdfShapes.union(var10000).paint(TralaleroDesign.legPaint(var2));
        Sdf3D var20 = TralaleroDesign.flat(TralaleroDesign.foot(0.0f), var0, 0.6f, var2, 0.6f, 0.52f).paint(SurfacePaint.vertical(-4604218, 0.2f, -657935, 0.3f));
        var10000 = new Sdf3D[]{var19, var20.scale(1.15f, var0, 0.0f, var2)};
        return SdfShapes.union(var10000);
    }

    private static Sdf3D orient(Sdf3D var0, float var1, float var2, float var3, Vec3 var4, Vec3 var5) {
        Vec3 var6 = var4.normalize();
        Vec3 var7 = var6.cross(var5).normalize();
        Vec3 var8 = var7.cross(var6);
        float var9 = (float)Math.acos(Math.max(-1.0f, Math.min(1.0f, (var6.x() + var8.y() + var7.z() - 1.0f) * 0.5f)));
        Sdf3D var10 = var9 < 1.0E-4f ? var0 : var0.rotateAxis(var8.z() - var7.y(), var7.x() - var6.z(), var6.y() - var8.x(), (float)Math.toDegrees(var9), 0.0f, 0.0f, 0.0f);
        return var10.move(var1, var2, var3);
    }

    private static Sdf3D flat(Sdf2D var0, float var1, float var2, float var3, float var4, float var5) {
        return TralaleroDesign.orient(SdfShapes.extrude(var0, var4, var5), var1, var2, var3, Vec3.LEFT, Vec3.FORWARD);
    }

    private static Sdf2D foot(float var0) {
        Sdf2D var1 = Sdf2D.circle(0.0f, -1.05f, 1.0f);
        Sdf2D var2 = Sdf2D.circle(0.0f, 1.4f, 1.22f);
        Sdf2D var3 = Sdf2D.circle(0.0f, 2.35f, 1.05f);
        float var10000 = 2.2f;
        Sdf2D[] var10001 = new Sdf2D[]{var1, var2, var3};
        Sdf2D var4 = Sdf2D.blend(var10000, var10001);
        return var0 > 0.0f ? var4.inflate(-var0) : var4;
    }

    private static Sdf3D shoe(float var0, float var1) {
        float var10000 = 0.8f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.cone(var0, 1.55f, var1 - 0.85f, 1.3f, var0, 1.25f, var1 + 1.1f, 1.3f), SdfShapes.ellipsoid(var0, 0.95f, var1 + 1.95f, 1.22f, 0.92f, 1.5f)};
        Sdf3D var2 = SdfShapes.blend(var10000, var10001);
        SurfacePaint var3 = SurfacePaint.field((var1x, var2x, var3x) -> ArgbColors.mix(-13933608, -14989656, TralaleroDesign.smooth(var1 - 0.6f, var1 - 1.3f, var3x)));
        Sdf3D var4 = SdfShapes.intersect(0.15f, var2, TralaleroDesign.flat(TralaleroDesign.foot(0.1f), var0, 2.0f, var1, 1.3f, 0.25f)).paint(var3);
        Sdf3D var5 = SdfShapes.torus(var0, 2.95f, var1 - 0.5f, 0.78f, 0.34f).rotate(-14.0f, 0.0f, 0.0f, var0, 2.95f, var1 - 0.5f).color(-14989656);
        Sdf3D var6 = SdfShapes.ellipsoid(var0, 2.9f, var1 + 0.35f, 0.52f, 0.4f, 0.7f).rotate(20.0f, 0.0f, 0.0f, var0, 2.9f, var1 + 0.35f).color(-9593609);
        Sdf3D var7 = SdfShapes.ellipsoid(var0, 3.15f, var1 - 1.25f, 0.26f, 0.45f, 0.2f).color(-14989656);
        var10000 = 0.2f;
        var10001 = new Sdf3D[]{var4, var5, var6, var7};
        return SdfShapes.blend(var10000, var10001).scale(1.15f, var0, 0.0f, var1);
    }

    private static PixelCanvas laceCanvas() {
        PixelCanvas var0 = new PixelCanvas(60, 76);
        for (int var1 = 0; var1 < 4; ++var1) {
            float var2 = 6.5f + (float)var1 * 20.25f;
            float var3 = 22.5f - (float)var1 * 1.9f;
            var0.fill(Sdf2D.box(30.0f, var2, var3, 4.1f, 4.1f), -1);
            var0.fill(Sdf2D.circle(30.0f - var3 - 2.3f, var2, 2.4f), -14274500);
            var0.fill(Sdf2D.circle(30.0f + var3 + 2.3f, var2, 2.4f), -14274500);
        }
        return var0;
    }

    private static PixelCanvas stripeCanvas() {
        PixelCanvas var0 = new PixelCanvas(160, 30);
        var0.fill(Sdf2D.line(20.0f, 9.0f, 140.0f, 21.0f, 13.0f), -1);
        return var0;
    }

    private static SurfaceDecal stripe(PixelCanvas var0, float var1, float var2, float var3) {
        float var4 = 1.15f;
        return SurfaceDecal.project(var0, var1 + var3 * 1.3f * var4, 1.55f * var4, var2 + 0.35f * var4, var3, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 3.6f * var4, 0.68f * var4).depth(0.9f, 0.9f).maxAngle(70.0f).flatLight(0.6f).occlusion(0.4f);
    }

    private static SurfaceDecal laces(PixelCanvas var0, float var1, float var2) {
        float var3 = 1.15f;
        return SurfaceDecal.project(var0, var1, 2.7f * var3, var2 + 1.3f * var3, 0.0f, 0.79f, 0.61f, 0.0f, 1.0f, 0.0f, 1.25f * var3, 1.58f * var3).depth(0.9f, 0.9f).flatLight(0.6f).occlusion(0.4f);
    }

    private static PixelCanvas eyeCanvas() {
        PixelCanvas var0 = new PixelCanvas(96, 96);
        float var1 = 48.0f;
        float var2 = 50.0f;
        Sdf2D var3 = Sdf2D.ellipse(var1, var2, 21.0f, 20.0f);
        var0.fill(var3.inflate(4.0f), 1428832344, 3.0f);
        float var10002 = var1 - 4.5f;
        float var10003 = var2 - 6.0f;
        float var10004 = 26.0f;
        int[] var10005 = new int[]{-14011322, -16316147};
        var0.fill(var3, ColorField2D.radial(var10002, var10003, var10004, var10005));
        var0.fill(Sdf2D.circle(var1 - 7.5f, var2 - 7.0f, 5.6f), -1);
        var0.fill(Sdf2D.circle(var1 + 7.5f, var2 + 8.0f, 2.4f), -1325400065);
        var0.fill(Sdf2D.arc(var1, var2 + 2.0f, 22.0f, 200.0f, 340.0f, 4.0f), -2144713630, 2.0f);
        return var0;
    }

    private static SurfaceDecal eye(PixelCanvas var0, float var1) {
        return SurfaceDecal.project(var0, var1 * 3.3f, 20.1f, 10.2f, var1 * 0.84f, 0.36f, 0.4f, 0.0f, 1.0f, 0.0f, 2.2f, 2.2f).depth(1.2f, 1.2f).maxAngle(75.0f).flatLight(0.7f);
    }

    private static PixelCanvas gillCanvas() {
        PixelCanvas var0 = new PixelCanvas(120, 120);
        for (int var1 = 0; var1 < 5; ++var1) {
            float var2 = 16.0f + (float)var1 * 22.0f;
            float var3 = 42.0f - (float)var1 * 3.0f;
            var0.fill(Sdf2D.curve(var2, 58.0f - var3, var2 + 7.0f, 58.0f, var2, 58.0f + var3, 3.4f, 3.4f), -802799780);
        }
        return var0;
    }

    private static SurfaceDecal gills(PixelCanvas var0, float var1) {
        return SurfaceDecal.project(var0, var1 * 5.1f, 18.5f, 6.0f, var1, 0.05f, 0.2f, 0.0f, 1.0f, 0.0f, 2.8f, 2.8f).depth(1.5f, 1.5f);
    }

    private static float mouthX(float var0) {
        return 142.0f + var0 * 284.0f / 152.0f;
    }

    private static float arch(float var0) {
        float var1 = Math.min(1.0f, Math.abs(var0) / 74.0f);
        float var2 = 11.0f - 3.6f * (float)Math.pow(var1, 1.8);
        return 60.0f - (var2 - 9.45f) * 25.0f;
    }

    private static float gape(float var0) {
        float var1 = Math.min(1.0f, Math.abs(var0) / 74.0f);
        return 15.000001f * (1.0f - var1 * var1);
    }

    private static SurfaceDecal mouth() {
        PixelCanvas var0 = new PixelCanvas(284, 120);
        float var1 = 0.53521127f;
        float[] var2 = new float[284];
        float[] var3 = new float[284];
        float[] var4 = new float[284];
        for (int var5 = 0; var5 < 284; ++var5) {
            float var6 = ((float)var5 + 0.5f - 142.0f) * var1;
            float var7 = (TralaleroDesign.arch(var6 + 0.5f) - TralaleroDesign.arch(var6 - 0.5f)) * var1;
            var2[var5] = TralaleroDesign.arch(var6);
            var3[var5] = Math.abs(var6) > 75.0f ? -1.0f : TralaleroDesign.gape(var6);
            var4[var5] = 1.0f / (float)Math.sqrt(1.0f + var7 * var7);
        }
        Sdf2D var15 = Sdf2D.field((var3x, var4x) -> {
            int var5x = Math.max(0, Math.min(283, (int)var3x));
            return var3[var5x] < 0.0f ? 4.0f : (Math.abs(var4x - var2[var5x]) - var3[var5x]) * var4[var5x];
        }, TralaleroDesign.mouthX(-76.0f), 3.0f, TralaleroDesign.mouthX(76.0f), 117.0f);
        float var10002 = 0.0f;
        float var10003 = 0.0f;
        float var10004 = 0.0f;
        float var10005 = 120.0f;
        int[] var10006 = new int[]{-10872282, -12972264};
        var0.fill(var15, ColorField2D.linear(var10002, var10003, var10004, var10005, var10006));
        float var16 = 0.75f;
        float var17 = 2.5f;
        for (int var8 = -4; var8 <= 4; ++var8) {
            float var9 = (float)var8 * 15.0f;
            float var10 = TralaleroDesign.gape(var9);
            if (var10 < var17) continue;
            float[] var10000 = new float[]{TralaleroDesign.mouthX(var9 - 6.0f), TralaleroDesign.arch(var9 - 6.0f) - TralaleroDesign.gape(var9 - 6.0f) - var16, TralaleroDesign.mouthX(var9 + 6.0f), TralaleroDesign.arch(var9 + 6.0f) - TralaleroDesign.gape(var9 + 6.0f) - var16, TralaleroDesign.mouthX(var9), TralaleroDesign.arch(var9) + var10 * 0.5f};
            Sdf2D var11 = Sdf2D.polygon(var10000);
            var0.fill(var11.intersect(var15), -263436);
            float var12 = var9 + 7.5f;
            float var13 = TralaleroDesign.gape(var12);
            if (var13 < var17 || var8 == 4) continue;
            var10000 = new float[]{TralaleroDesign.mouthX(var12 - 5.0f), TralaleroDesign.arch(var12 - 5.0f) + TralaleroDesign.gape(var12 - 5.0f) + var16, TralaleroDesign.mouthX(var12 + 5.0f), TralaleroDesign.arch(var12 + 5.0f) + TralaleroDesign.gape(var12 + 5.0f) + var16, TralaleroDesign.mouthX(var12), TralaleroDesign.arch(var12) - var13 * 0.35f};
            Sdf2D var14 = Sdf2D.polygon(var10000);
            var0.fill(var14.intersect(var15), -1185056);
        }
        var0.fill(var15.outline(2.5f), -15194566);
        return SurfaceDecal.wrap(var0, 0.0f, 18.35f, 10.25f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f, 152.0f, 4.8f).maxAngle(80.0f).flatLight(1.0f).occlusion(0.3f);
    }

    static {
        float[] fArray = new float[13];
        float[] var3 = fArray;
        fArray[0] = -12.4f;
        var3[1] = -10.7f;
        var3[2] = -8.4f;
        var3[3] = -5.7f;
        var3[4] = -2.9f;
        var3[5] = 0.0f;
        var3[6] = 2.9f;
        var3[7] = 5.8f;
        var3[8] = 8.3f;
        var3[9] = 10.3f;
        var3[10] = 11.8f;
        var3[11] = 12.8f;
        var3[12] = 13.4f;
        BODY_Z = var3;
        float[] fArray2 = new float[13];
        float[] var5 = fArray2;
        fArray2[0] = 1.4f;
        var5[1] = 1.15f;
        var5[2] = 0.8f;
        var5[3] = 0.4f;
        var5[4] = 0.1f;
        var5[5] = 0.0f;
        var5[6] = 0.05f;
        var5[7] = 0.25f;
        var5[8] = 0.5f;
        var5[9] = 0.75f;
        var5[10] = 0.95f;
        var5[11] = 1.05f;
        var5[12] = 1.1f;
        BODY_Y = var5;
        float[] fArray3 = new float[13];
        float[] var7 = fArray3;
        fArray3[0] = 1.0f;
        var7[1] = 2.1f;
        var7[2] = 3.3f;
        var7[3] = 4.6f;
        var7[4] = 5.5f;
        var7[5] = 5.85f;
        var7[6] = 5.75f;
        var7[7] = 5.2f;
        var7[8] = 4.35f;
        var7[9] = 3.35f;
        var7[10] = 2.3f;
        var7[11] = 1.35f;
        var7[12] = 0.45f;
        BODY_RY = var7;
        float[] fArray4 = new float[13];
        float[] var9 = fArray4;
        fArray4[0] = 0.9f;
        var9[1] = 1.8f;
        var9[2] = 2.9f;
        var9[3] = 4.2f;
        var9[4] = 5.1f;
        var9[5] = 5.45f;
        var9[6] = 5.35f;
        var9[7] = 4.8f;
        var9[8] = 3.95f;
        var9[9] = 3.0f;
        var9[10] = 2.1f;
        var9[11] = 1.25f;
        var9[12] = 0.45f;
        BODY_RX = var9;
        float[] fArray5 = new float[12];
        float[] var11 = fArray5;
        fArray5[0] = -14.5f;
        var11[1] = -11.0f;
        var11[2] = -8.0f;
        var11[3] = -5.0f;
        var11[4] = -2.0f;
        var11[5] = 1.0f;
        var11[6] = 3.5f;
        var11[7] = 5.5f;
        var11[8] = 7.0f;
        var11[9] = 9.0f;
        var11[10] = 11.0f;
        var11[11] = 14.5f;
        LINE_Z = var11;
        float[] fArray6 = new float[12];
        float[] var13 = fArray6;
        fArray6[0] = 19.699999f;
        var13[1] = 19.1f;
        var13[2] = 18.1f;
        var13[3] = 17.1f;
        var13[4] = 16.4f;
        var13[5] = 16.3f;
        var13[6] = 16.9f;
        var13[7] = 17.65f;
        var13[8] = 17.4f;
        var13[9] = 17.6f;
        var13[10] = 18.1f;
        var13[11] = 18.4f;
        LINE_Y = var13;
    }
}

