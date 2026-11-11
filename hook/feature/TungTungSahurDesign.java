/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.BoneMotionProfile;
import hook.feature.CharacterBone;
import hook.feature.CharacterDefinition;
import hook.feature.CharacterPartBuilder;
import hook.feature.CharacterRig;
import hook.feature.ColorField2D;
import hook.feature.PerlinNoise;
import hook.feature.PixelCanvas;
import hook.feature.Sdf2D;
import hook.feature.Sdf3D;
import hook.feature.SdfShapes;
import hook.feature.SurfaceDecal;
import hook.feature.SurfaceMaterialSample;
import hook.feature.SurfacePaint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class TungTungSahurDesign
extends CharacterDefinition {
    public static final float R = 5.0f;
    private static final float BOTTOM = 12.0f;
    private static final float TOP = 34.0f;
    private static final float RIM = 0.85f;
    private static final float FIST_X = -7.0f;
    private static final float FIST_Y = 11.6f;
    private static final float FIST_Z = 0.72f;
    private static final float[] KNUCKLE_DIR = TungTungSahurDesign.unit(-0.16f, 0.86f, 0.48f);
    private static final int WOOD = -1063542;
    private static final int WOOD_DARK = -4158378;
    private static final int WOOD_WORN = -5868993;
    private static final int FISSURE = -9551070;
    private static final int KNOT = -11653867;
    private static final int CUT = -1389672;
    private static final int LIMB = -3567528;
    private static final int LIMB_DARK = -7053254;
    private static final int INK = -15003638;
    private static final int LINE = -12967406;

    public TungTungSahurDesign() {
        super("tung_tung_sahur", "Tung Tung Tung Sahur");
    }

    @Override
    protected void define(CharacterRig var1) {
        var1.scale(1.0f).outline(-12967406, 0.16f).occlusion(0.3f).firstPersonArms(false);
        var1.part(CharacterBone.HEAD).pivot(0.0f, 12.5f, 0.0f);
        CharacterPartBuilder var2 = var1.extra("upright", CharacterBone.HEAD).motion(BoneMotionProfile.none().hang(1.0f).limit(100.0f));
        var1.extra("log", var2).shape(TungTungSahurDesign.log()).detail(1.5f).resolution(1.2f).flatLight(0.35f).neck(0.0f, 22.5f, 0.0f, 0.0f, 1.0f, 0.0f, 4.0f).look(0.6f, 0.0f, 40.0f).motion(BoneMotionProfile.none().walk(0.0f, 4.0f, -6.0f, 1.0f).drag(6.0f)).decal(TungTungSahurDesign.eyes()).decal(TungTungSahurDesign.mouth()).decal(TungTungSahurDesign.rings());
        CharacterPartBuilder var3 = var1.part(CharacterBone.RIGHT_ARM).pivot(-5.3f, 22.8f, 0.2f).grip(-7.0f, 11.6f, 0.72f).shape(TungTungSahurDesign.arm()).joint(-6.57f, 17.1f, 0.14f, 0.6f);
        var1.part(CharacterBone.LEFT_ARM).mirrorOf(var3);
        CharacterPartBuilder var4 = var1.extra("hand_r", CharacterBone.RIGHT_ARM).shape(TungTungSahurDesign.hand()).resolution(0.6f).detail(0.8f);
        var1.extra("hand_l", CharacterBone.LEFT_ARM).mirrorOf(var4);
        CharacterPartBuilder var5 = var1.part(CharacterBone.RIGHT_LEG).pivot(-2.2f, 12.8f, 0.0f).shape(TungTungSahurDesign.leg()).swing(0.6f, 35.0f).joint(-2.3f, 6.75f, 0.16f, 0.7f);
        var1.part(CharacterBone.LEFT_LEG).mirrorOf(var5);
        CharacterPartBuilder var6 = var1.extra("foot_r", CharacterBone.RIGHT_LEG).shape(TungTungSahurDesign.foot()).resolution(0.7f).detail(0.8f);
        var1.extra("foot_l", CharacterBone.LEFT_LEG).mirrorOf(var6);
    }

    private static Sdf3D log() {
        Sdf3D var0 = SdfShapes.field(TungTungSahurDesign::trunk, -5.5f, 11.8f, -5.5f, 5.5f, 34.2f, 5.5f);
        PaintWrapper var1 = new PaintWrapper(SurfacePaint.wood(-1063542, -4158378, 0.3f, 0.0f, -0.2f, 0.0f, 1.0f, 0.0f, 0.8f).noise(-5868993, 3.5f, 0.08f).grain(0.35f, 0.04f));
        float var10000 = 0.35f;
        Sdf3D[] var10001 = new Sdf3D[]{var0.paint(var1), TungTungSahurDesign.knot(38.0f, 17.2f, 0.8f), TungTungSahurDesign.knot(165.0f, 27.5f, 0.95f), TungTungSahurDesign.knot(-120.0f, 19.5f, 0.7f), TungTungSahurDesign.knot(-58.0f, 14.2f, 0.55f), TungTungSahurDesign.stub(128.0f, 25.2f), TungTungSahurDesign.eye(1.0f, var1), TungTungSahurDesign.eye(-1.0f, var1)};
        Sdf3D var2 = SdfShapes.blend(var10000, var10001);
        Sdf3D var3 = SdfShapes.ellipsoid(0.0f, 25.35f, TungTungSahurDesign.radius(0.0f, 25.35f, 1.0f) + 0.12f, 2.7f, 1.05f, 0.55f);
        var10000 = 0.12f;
        Sdf3D var5 = SdfShapes.subtract(0.35f, var2, var3);
        Sdf3D[] var10002 = new Sdf3D[]{TungTungSahurDesign.hole(165.0f, 27.5f, 0.32f), TungTungSahurDesign.hole(-120.0f, 19.5f, 0.24f)};
        return SdfShapes.subtract(var10000, var5, SdfShapes.union(var10002));
    }

    private static Sdf3D stub(float var0, float var1) {
        float var2 = (float)Math.sin(Math.toRadians(var0));
        float var3 = (float)Math.cos(Math.toRadians(var0));
        float var4 = (float)Math.cos(Math.toRadians(32.0));
        float var5 = (float)Math.sin(Math.toRadians(32.0));
        float var6 = var2 * var4;
        float var8 = var3 * var4;
        float var9 = var2 * 3.9f;
        float var10 = var3 * 3.9f;
        float var11 = var9 + var6 * 3.05f;
        float var12 = var1 + var5 * 3.05f;
        float var13 = var10 + var8 * 3.05f;
        Sdf3D var14 = SdfShapes.cone(var9, var1, var10, 0.98f, var11 + var6 * 0.4f, var12 + var5 * 0.4f, var13 + var8 * 0.4f, 0.64f).paint(SurfacePaint.wood(-1063542, -4158378, var9, var1, var10, var6, var5, var8, 0.3f));
        SurfacePaint var15 = SurfacePaint.radial(-6264264, var11, var12, var13, 0.75f, -1389672);
        return SdfShapes.subtract(var14, SdfShapes.plane(var11, var12, var13, -var6, -var5, -var8).paint(var15));
    }

    private static Sdf3D eye(float var0, SurfacePaint var1) {
        float var2 = var0 * 2.14f;
        float var3 = 28.72f;
        float var4 = var2 / 5.0f;
        float var5 = (float)Math.sqrt(1.0f - var4 * var4);
        float var6 = TungTungSahurDesign.radius(var4, var3, var5) - 0.5f;
        float var7 = (float)Math.toDegrees(Math.asin(var4));
        return SdfShapes.ellipsoid(var4 * var6, var3, var5 * var6, 1.85f, 1.8f, 0.85f).rotate(0.0f, var7, 0.0f, var4 * var6, var3, var5 * var6).paint(var1);
    }

    private static Sdf3D knot(float var0, float var1, float var2) {
        float var3 = (float)Math.sin(Math.toRadians(var0));
        float var4 = (float)Math.cos(Math.toRadians(var0));
        float var5 = TungTungSahurDesign.radius(var3, var1, var4) + 0.28f;
        float var6 = var5 - var2;
        return SdfShapes.ellipsoid(var3 * var6, var1, var4 * var6, var2, var2 * 1.35f, var2).paint(SurfacePaint.radial(-11653867, var3 * var5, var1, var4 * var5, var2 * 1.05f, -4158378));
    }

    private static Sdf3D hole(float var0, float var1, float var2) {
        float var3 = (float)Math.sin(Math.toRadians(var0));
        float var4 = (float)Math.cos(Math.toRadians(var0));
        float var5 = TungTungSahurDesign.radius(var3, var1, var4) + 0.28f;
        return SdfShapes.ellipsoid(var3 * var5, var1, var4 * var5, var2, var2 * 1.3f, var2).color(-11653867);
    }

    private static float radius(float var0, float var1, float var2) {
        float var3 = 0.17f * PerlinNoise.noise(var0 * 1.1f + 2.3f, var1 * 0.1f, var2 * 1.1f - 1.3f);
        return 5.0f + var3 - 0.17f * TungTungSahurDesign.fissure(var0, var1, var2);
    }

    private static float fissure(float var0, float var1, float var2) {
        float var3 = Math.abs(PerlinNoise.noise(var0 * 3.0f - 5.1f, var1 * 0.08f, var2 * 3.0f + 0.7f));
        float var4 = TungTungSahurDesign.smooth(0.3f, 0.62f, var2) * TungTungSahurDesign.smooth(21.8f, 23.6f, var1);
        return Math.max(0.0f, 1.0f - var3 / 0.28f) * (1.0f - 0.88f * var4);
    }

    private static float trunk(float var0, float var1, float var2) {
        float var3 = (float)Math.sqrt(var0 * var0 + var2 * var2);
        float var4 = var3 > 1.0E-4f ? var0 / var3 : 0.0f;
        float var5 = var3 > 1.0E-4f ? var2 / var3 : 1.0f;
        float var6 = var3 - TungTungSahurDesign.radius(var4, var1, var5) + 0.85f;
        float var7 = Math.abs(var1 - 23.0f) - 11.0f + 0.85f;
        float var8 = Math.max(var6, 0.0f);
        float var9 = Math.max(var7, 0.0f);
        return (float)Math.sqrt(var8 * var8 + var9 * var9) + Math.min(Math.max(var6, var7), 0.0f) - 0.85f;
    }

    private static Sdf3D arm() {
        SurfacePaint var0 = TungTungSahurDesign.stick(-5.4f, 22.6f, 0.2f, -7.0f, 11.6f, 0.72f);
        float var10000 = 0.2f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.sphere(-5.3f, 22.8f, 0.2f, 0.7f), SdfShapes.capsule(-5.3f, 22.8f, 0.2f, -3.9f, 22.8f, 0.2f, 0.55f)};
        Sdf3D var1 = SdfShapes.blend(var10000, var10001);
        Sdf3D var2 = SdfShapes.cone(-5.4f, 22.5f, 0.2f, 0.64f, -6.55f, 17.3f, 0.12f, 0.54f);
        Sdf3D var3 = SdfShapes.sphere(-6.57f, 17.1f, 0.14f, 0.6f);
        Sdf3D var4 = SdfShapes.cone(-6.6f, 16.85f, 0.14f, 0.55f, -6.93f, 12.6f, 0.5f, 0.5f);
        var10000 = 0.3f;
        var10001 = new Sdf3D[]{var1, var2, var3, var4};
        return SdfShapes.blend(var10000, var10001).paint(var0);
    }

    private static Sdf3D hand() {
        float var0 = KNUCKLE_DIR[0];
        float var1 = KNUCKLE_DIR[1];
        float var2 = KNUCKLE_DIR[2];
        Sdf3D var3 = SdfShapes.ellipsoid(-6.92f, 11.650001f, 0.70000005f, 0.86f, 1.05f, 0.9f);
        Sdf3D var4 = SdfShapes.cone(-6.93f, 12.75f, 0.5f, 0.5f, -6.95f, 12.150001f, 0.72f, 0.64f);
        Sdf3D[] var5 = new Sdf3D[4];
        for (int var6 = 0; var6 < 4; ++var6) {
            float var7 = -0.66f + (float)var6 * 0.44f;
            float var8 = -7.0f + var0 * var7 - 0.28f;
            float var9 = 11.6f + var1 * var7;
            float var10 = 0.72f + var2 * var7 + 0.72f;
            var5[var6] = SdfShapes.capsule(var8 - 0.35f, var9, var10 - 0.3f, var8 + 0.3f, var9, var10 + 0.1f, 0.26f + 0.02f * (float)(3 - var6));
        }
        Sdf3D var11 = SdfShapes.capsule(-6.38f, 12.1f, 0.92f, -6.82f, 12.320001f, 1.6700001f, 0.27f);
        float var10000 = 0.18f;
        Sdf3D[] var10001 = new Sdf3D[3];
        int var10003 = 0;
        float var10004 = 0.3f;
        Sdf3D[] var10005 = new Sdf3D[]{var3, var4};
        var10001[var10003] = SdfShapes.blend(var10004, var10005);
        var10001[1] = SdfShapes.union(var5);
        var10001[2] = var11;
        return SdfShapes.blend(var10000, var10001).paint(TungTungSahurDesign.stick(-6.9f, 12.8f, 0.5f, -7.0f, 10.6f, 0.72f));
    }

    private static Sdf3D leg() {
        SurfacePaint var0 = TungTungSahurDesign.stick(-2.2f, 12.8f, 0.0f, -2.35f, 1.4f, 0.0f);
        Sdf3D var1 = SdfShapes.sphere(-2.2f, 12.6f, 0.0f, 0.8f);
        Sdf3D var2 = SdfShapes.cone(-2.2f, 12.4f, 0.0f, 0.74f, -2.3f, 7.0f, 0.12f, 0.62f);
        Sdf3D var3 = SdfShapes.sphere(-2.3f, 6.75f, 0.16f, 0.68f);
        Sdf3D var4 = SdfShapes.cone(-2.3f, 6.5f, 0.12f, 0.62f, -2.36f, 1.9f, 0.0f, 0.55f);
        float var10000 = 0.3f;
        Sdf3D[] var10001 = new Sdf3D[]{var1, var2, var3, var4};
        return SdfShapes.blend(var10000, var10001).paint(var0);
    }

    private static Sdf3D foot() {
        float var10000 = 0.5f;
        Sdf3D[] var10001 = new Sdf3D[]{SdfShapes.ellipsoid(-2.42f, 0.55f, 1.0f, 1.2f, 0.55f, 1.9f), SdfShapes.ellipsoid(-2.36f, 0.95f, -0.1f, 0.8f, 0.85f, 1.0f), SdfShapes.sphere(-2.36f, 1.75f, 0.0f, 0.55f)};
        Sdf3D var0 = SdfShapes.blend(var10000, var10001);
        Sdf3D[] var2 = new Sdf3D[]{SdfShapes.sphere(-1.7f, 0.38f, 2.55f, 0.4f), SdfShapes.sphere(-2.2f, 0.35f, 2.8f, 0.36f), SdfShapes.sphere(-2.68f, 0.34f, 2.76f, 0.33f), SdfShapes.sphere(-3.12f, 0.33f, 2.52f, 0.31f)};
        Sdf3D var1 = SdfShapes.union(var2);
        var10000 = 0.22f;
        var10001 = new Sdf3D[]{var0, var1};
        return SdfShapes.intersect(SdfShapes.blend(var10000, var10001), SdfShapes.above(0.0f)).paint(TungTungSahurDesign.stick(-2.4f, 0.6f, -1.0f, -2.4f, 0.6f, 3.0f));
    }

    private static SurfacePaint stick(float var0, float var1, float var2, float var3, float var4, float var5) {
        return SurfacePaint.wood(-3567528, -7053254, var0, var1, var2, var3 - var0, var4 - var1, var5 - var2, 0.35f).grain(0.3f, 0.05f);
    }

    private static SurfaceDecal eyes() {
        PixelCanvas var0 = new PixelCanvas(336, 208);
        for (int var1 = -1; var1 <= 1; var1 += 2) {
            float var2 = 168.0f + (float)var1 * 85.5f;
            float var3 = 122.0f;
            var0.fill(Sdf2D.circle(var2, var3, 75.0f), 808458248, 13.0f);
            var0.fill(Sdf2D.circle(var2, var3, 66.0f), -15003638);
            Sdf2D var10001 = Sdf2D.circle(var2, var3, 60.75f);
            float var10002 = var2 - 15.0f;
            float var10003 = var3 - 18.0f;
            float var10004 = 78.0f;
            int[] var10005 = new int[]{-1, -132105, -1054240};
            var0.fill(var10001, ColorField2D.radial(var10002, var10003, var10004, var10005));
            float var4 = var2 - (float)var1 * 1.5f;
            float var5 = var3 + 1.0f;
            var0.fill(Sdf2D.circle(var4, var5, 12.0f), -15857658);
            var0.fill(Sdf2D.circle(var4 - 3.9f, var5 - 4.4f, 3.6f), -285212673);
        }
        Sdf2D var6 = Sdf2D.curve(15.0f, 48.0f, 74.0f, 18.0f, 147.0f, 71.0f, 21.0f, 34.5f);
        var0.fill(var6, -15003638);
        var0.fill(var6.mirrorX(168.0f).rotate(6.0f, 321.0f, 48.0f).move(0.0f, -7.0f), -15003638);
        return SurfaceDecal.front(var0, 0.0f, 29.175f, 5.0f, 8.4f, 5.2f).depth(3.6f, 1.5f).maxAngle(72.0f).flatLight(1.0f).occlusion(0.0f);
    }

    private static SurfaceDecal mouth() {
        PixelCanvas var0 = new PixelCanvas(272, 104);
        Sdf2D var1 = Sdf2D.ellipse(136.0f, 19.0f, 124.5f, 78.0f).subtract(Sdf2D.circle(136.0f, -482.0f, 507.0f));
        var0.fill(var1.inflate(6.0f), -15003638);
        float var10002 = 136.0f;
        float var10003 = 10.0f;
        float var10004 = 136.0f;
        float var10005 = 95.5f;
        int[] var10006 = new int[]{-15071227, -12970996};
        var0.fill(var1, ColorField2D.linear(var10002, var10003, var10004, var10005, var10006));
        var0.fill(Sdf2D.ellipse(136.0f, 98.5f, 75.0f, 39.0f).intersect(var1), -3646898);
        return SurfaceDecal.front(var0, 0.0f, 24.675f, 5.0f, 6.8f, 2.6f).depth(3.6f, 1.5f).maxAngle(72.0f);
    }

    private static SurfaceDecal rings() {
        PixelCanvas var0 = new PixelCanvas(168, 168);
        Sdf2D var10001 = Sdf2D.circle(84.0f, 84.0f, 72.0f);
        float var10002 = 81.0f;
        float var10003 = 86.0f;
        float var10004 = 72.0f;
        int[] var10005 = new int[]{-3565470, -1917818, -1521774, -2709138};
        var0.fill(var10001, ColorField2D.radial(var10002, var10003, var10004, var10005), 4.0f);
        Sdf2D var1 = Sdf2D.field((var0x, var1x) -> {
            float var2 = var0x - 81.0f;
            float var3 = var1x - 86.0f;
            float var4 = (float)Math.sqrt(var2 * var2 + var3 * var3) + 0.001f;
            float var5 = var4 + 2.5f * PerlinNoise.noise(var2 / var4 * 1.3f, var3 / var4 * 1.3f, var4 * 0.028f);
            float var6 = var5 / 8.25f;
            float var7 = var6 - (float)Math.round(var6);
            return Math.max(Math.max(Math.abs(var7) * 8.25f - 0.82f, var4 - 67.5f), 6.7f - var4);
        }, 12.0f, 17.0f, 150.0f, 155.0f);
        var0.fill(var1, -1602927328);
        var0.fill(Sdf2D.circle(81.0f, 86.0f, 3.6f), -9813984);
        var0.fill(Sdf2D.taper(87.0f, 81.0f, 3.0f, 133.0f, 52.0f, 1.0f), -1069538288);
        var0.fill(Sdf2D.taper(77.0f, 91.0f, 2.2f, 47.0f, 126.0f, 1.0f), -1337973744);
        return SurfaceDecal.top(var0, 0.0f, 34.0f, 0.0f, 9.678f, 9.678f).depth(0.6f, 0.6f).maxAngle(35.0f);
    }

    private static float smooth(float var0, float var1, float var2) {
        float var3 = Math.max(0.0f, Math.min(1.0f, (var2 - var0) / (var1 - var0)));
        return var3 * var3 * (3.0f - 2.0f * var3);
    }

    private static float[] unit(float var0, float var1, float var2) {
        float var3 = (float)Math.sqrt(var0 * var0 + var1 * var1 + var2 * var2);
        float[] var10000 = new float[]{var0 / var3, var1 / var3, var2 / var3};
        return var10000;
    }

    @Environment(value=EnvType.CLIENT)
    private static final class PaintWrapper
    extends SurfacePaint {
        private final SurfacePaint base;

        PaintWrapper(SurfacePaint var1) {
            this.base = var1;
        }

        @Override
        public void apply(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            this.base.apply(var1, var2, var3, var4);
            float var5 = (float)Math.sqrt(var1 * var1 + var3 * var3);
            if (!(var5 < 0.5f)) {
                float var6 = TungTungSahurDesign.smooth(3.95f, 4.7f, var5) * TungTungSahurDesign.smooth(12.0f, 12.6f, var2) * (1.0f - TungTungSahurDesign.smooth(33.4f, 34.0f, var2));
                float var7 = TungTungSahurDesign.fissure(var1 / var5, var2, var3 / var5) * var6;
                if (var7 > 0.0f) {
                    var4.toward(-9551070, Math.min(1.0f, var7 * 0.62f));
                }
            }
        }
    }
}

