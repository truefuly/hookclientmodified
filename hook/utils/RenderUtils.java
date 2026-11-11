/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_12247
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1921
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_7833
 *  net.minecraft.class_9801
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package hook.utils;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import hook.mixin.RenderLayerAccessor;
import hook.mixin.RenderPipelinesAccessor;
import hook.utils.RenderVisibility;
import java.awt.Color;
import java.util.List;
import net.minecraft.class_10789;
import net.minecraft.class_12247;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_9801;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class RenderUtils {
    private static class_332 activeContext;
    private static boolean usingUnscaledProjection;
    private static long lastGuiFrameNanos;
    private static class_243 screenCenter;
    private static class_243 worldRenderOrigin;
    private static final Matrix4f inverseProjectionScratch;
    private static final Matrix4f inverseViewScratch;
    private static final Vector4f centerScratch;

    private RenderUtils() {
    }

    public static void setContext(class_332 var0) {
        activeContext = var0;
    }

    public static class_243 getCameraPos() {
        return RenderUtils.getCamera().method_71156();
    }

    public static class_4184 getCamera() {
        return class_310.method_1551().field_1773.method_19418();
    }

    public static void updateScreenCenter(Matrix4f var0, Matrix4f var1) {
        try {
            Matrix4f var2 = inverseProjectionScratch.set((Matrix4fc)var0).invert();
            Matrix4f var3 = inverseViewScratch.set((Matrix4fc)var1).invert();
            Vector4f var4 = centerScratch.set(0.0f, 0.0f, 0.0f, 1.0f).mul((Matrix4fc)var2).mul((Matrix4fc)var3);
            var4.div(var4.w);
            class_243 var5 = RenderUtils.getCamera().method_71156();
            screenCenter = new class_243(var5.field_1352 + (double)var4.x, var5.field_1351 + (double)var4.y, var5.field_1350 + (double)var4.z);
            RenderVisibility.begin(var0, var1, var5.field_1352, var5.field_1351, var5.field_1350);
        }
        catch (RuntimeException var6) {
            screenCenter = null;
            RenderVisibility.disable();
        }
    }

    public static class_243 getTracerStart(class_4184 var0) {
        if (screenCenter != null) {
            return screenCenter;
        }
        Vector3f var1 = new Vector3f(0.0f, 0.0f, -1.0f).rotate((Quaternionfc)var0.method_23767());
        return var0.method_71156().method_1031((double)var1.x, (double)var1.y, (double)var1.z);
    }

    public static double deltaTime() {
        int var0 = class_310.method_1551().method_47599();
        return var0 > 0 ? 1.0 / (double)var0 : 1.0;
    }

    public static float beginGuiFrame() {
        long var0 = System.nanoTime();
        if (lastGuiFrameNanos == 0L) {
            lastGuiFrameNanos = var0;
            return 1.0f;
        }
        float var2 = (float)(var0 - lastGuiFrameNanos) / 1.6666667E7f;
        lastGuiFrameNanos = var0;
        return class_3532.method_15363((float)var2, (float)0.1f, (float)3.0f);
    }

    public static float fast(float var0, float var1, float var2) {
        float var3 = class_3532.method_15363((float)((float)(RenderUtils.deltaTime() * (double)var2)), (float)0.0f, (float)1.0f);
        return (1.0f - var3) * var0 + var3 * var1;
    }

    public static class_243 getPlayerLookVec(class_1657 var0) {
        float var1 = class_3532.method_15362((double)(var0.method_36454() * ((float)Math.PI / 180) - (float)Math.PI));
        float var2 = class_3532.method_15374((double)(var0.method_36454() * ((float)Math.PI / 180) - (float)Math.PI));
        float var3 = class_3532.method_15362((double)(var0.method_36455() * ((float)Math.PI / 180)));
        return new class_243((double)(var2 * var3), (double)class_3532.method_15374((double)(var0.method_36455() * ((float)Math.PI / 180))), (double)(var1 * var3)).method_1029();
    }

    public static void unscaledProjection() {
        if (activeContext != null && !usingUnscaledProjection) {
            float var0 = class_310.method_1551().method_22683().method_4495();
            if (var0 <= 0.0f) {
                var0 = 1.0f;
            }
            activeContext.method_51448().pushMatrix();
            activeContext.method_51448().scale(1.0f / var0, 1.0f / var0);
            usingUnscaledProjection = true;
        }
    }

    public static void scaledProjection() {
        if (activeContext != null && usingUnscaledProjection) {
            activeContext.method_51448().popMatrix();
            usingUnscaledProjection = false;
        }
    }

    public static void renderRoundedQuad(Matrix3x2fStack var0, Color var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, double var16, double var18) {
        if (activeContext != null) {
            int var20 = (int)Math.floor(Math.min(var4, var8));
            int var21 = (int)Math.ceil(Math.max(var4, var8));
            double var22 = Math.min(var2, var6);
            double var24 = Math.max(var2, var6);
            double var26 = var21 - var20;
            double var28 = Math.min((var24 - var22) / 2.0, var26 / 2.0);
            double var30 = Math.min(Math.max(0.0, var10), var28);
            double var32 = Math.min(Math.max(0.0, var12), var28);
            double var34 = Math.min(Math.max(0.0, var14), var28);
            double var36 = Math.min(Math.max(0.0, var16), var28);
            int var38 = var20;
            int var39 = Integer.MIN_VALUE;
            int var40 = Integer.MIN_VALUE;
            for (int var41 = var20; var41 < var21; ++var41) {
                double var42 = (double)var41 + 0.5;
                double var44 = RenderUtils.cornerInset(var42 - (double)var20, var26, var30, var36);
                double var46 = RenderUtils.cornerInset(var42 - (double)var20, var26, var32, var34);
                int var48 = (int)Math.ceil(var22 + var44);
                int var49 = (int)Math.floor(var24 - var46);
                if (var49 <= var48) continue;
                if (var39 == Integer.MIN_VALUE) {
                    var38 = var41;
                    var39 = var48;
                    var40 = var49;
                    continue;
                }
                if (var48 == var39 && var49 == var40) continue;
                activeContext.method_25294(var39, var38, var40, var41, var1.getRGB());
                var38 = var41;
                var39 = var48;
                var40 = var49;
            }
            if (var39 != Integer.MIN_VALUE) {
                activeContext.method_25294(var39, var38, var40, var21, var1.getRGB());
            }
        }
    }

    public static void renderRoundedQuad(Matrix3x2fStack var0, Color var1, double var2, double var4, double var6, double var8, double var10, double var12) {
        RenderUtils.renderRoundedQuad(var0, var1, var2, var4, var6, var8, var10, var10, var10, var10, var12);
    }

    public static void renderCircle(Matrix3x2fStack var0, Color var1, double var2, double var4, double var6, int var8) {
        if (activeContext != null && !(var6 <= 0.0)) {
            int var9 = (int)Math.floor(var4 - var6);
            int var10 = (int)Math.ceil(var4 + var6);
            int var11 = var9;
            int var12 = Integer.MIN_VALUE;
            int var13 = Integer.MIN_VALUE;
            for (int var14 = var9; var14 < var10; ++var14) {
                double var15 = (double)var14 + 0.5 - var4;
                double var17 = Math.sqrt(Math.max(0.0, var6 * var6 - var15 * var15));
                int var19 = (int)Math.ceil(var2 - var17);
                int var20 = (int)Math.floor(var2 + var17);
                if (var20 <= var19) continue;
                if (var12 == Integer.MIN_VALUE) {
                    var11 = var14;
                    var12 = var19;
                    var13 = var20;
                    continue;
                }
                if (var19 == var12 && var20 == var13) continue;
                activeContext.method_25294(var12, var11, var13, var14, var1.getRGB());
                var11 = var14;
                var12 = var19;
                var13 = var20;
            }
            if (var12 != Integer.MIN_VALUE) {
                activeContext.method_25294(var12, var11, var13, var10, var1.getRGB());
            }
        }
    }

    private static double cornerInset(double var0, double var2, double var4, double var6) {
        if (var4 > 0.0 && var0 < var4) {
            double var10 = var4 - var0;
            return var4 - Math.sqrt(Math.max(0.0, var4 * var4 - var10 * var10));
        }
        if (var6 > 0.0 && var0 > var2 - var6) {
            double var8 = var0 - (var2 - var6);
            return var6 - Math.sqrt(Math.max(0.0, var6 * var6 - var8 * var8));
        }
        return 0.0;
    }

    public static void renderRoundedOutline(class_332 var0, Color var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, double var16, double var18, double var20) {
        RenderUtils.setContext(var0);
        if (!(var18 <= 0.0 || var6 <= var2 || var8 <= var4)) {
            int var22 = (int)Math.floor(var4);
            int var23 = (int)Math.ceil(var8);
            double var24 = var8 - var4;
            double var26 = Math.min((var6 - var2) / 2.0, var24 / 2.0);
            double var28 = Math.min(Math.max(0.0, var10), var26);
            double var30 = Math.min(Math.max(0.0, var12), var26);
            double var32 = Math.min(Math.max(0.0, var14), var26);
            double var34 = Math.min(Math.max(0.0, var16), var26);
            double var36 = var2 + var18;
            double var38 = var4 + var18;
            double var40 = var6 - var18;
            double var42 = var8 - var18;
            double var44 = Math.max(0.0, var42 - var38);
            for (int var46 = var22; var46 < var23; ++var46) {
                double var47 = (double)var46 + 0.5;
                int var49 = (int)Math.ceil(var2 + RenderUtils.cornerInset(var47 - var4, var24, var28, var34));
                int var50 = (int)Math.floor(var6 - RenderUtils.cornerInset(var47 - var4, var24, var30, var32));
                if (var50 <= var49) continue;
                if (!(var47 <= var38 || var47 >= var42 || var40 <= var36)) {
                    int var51 = (int)Math.ceil(var36 + RenderUtils.cornerInset(var47 - var38, var44, Math.max(0.0, var28 - var18), Math.max(0.0, var34 - var18)));
                    int var52 = (int)Math.floor(var40 - RenderUtils.cornerInset(var47 - var38, var44, Math.max(0.0, var30 - var18), Math.max(0.0, var32 - var18)));
                    if (var51 > var49) {
                        var0.method_25294(var49, var46, var51, var46 + 1, var1.getRGB());
                    }
                    if (var50 <= var52) continue;
                    var0.method_25294(var52, var46, var50, var46 + 1, var1.getRGB());
                    continue;
                }
                var0.method_25294(var49, var46, var50, var46 + 1, var1.getRGB());
            }
        }
    }

    public static void setScissorRegion(int var0, int var1, int var2, int var3) {
    }

    public static class_4587 matrixFrom(double var0, double var2, double var4) {
        class_4587 var6 = new class_4587();
        class_4184 var7 = RenderUtils.getCamera();
        var6.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(var7.method_19329()));
        var6.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(var7.method_19330() + 180.0f));
        class_243 var8 = var7.method_71156();
        var6.method_22904(var0 - var8.field_1352, var2 - var8.field_1351, var4 - var8.field_1350);
        return var6;
    }

    public static void renderQuad(class_4587 var0, float var1, float var2, float var3, float var4, int var5) {
    }

    public static void renderFilledBox(class_4587 var0, float var1, float var2, float var3, float var4, float var5, float var6, Color var7) {
        RenderUtils.renderFilledBoxes(var0, List.of(new ColoredBox(var1, var2, var3, var4, var5, var6, var7)));
    }

    public static void renderFilledBoxes(class_4587 var0, Iterable<ColoredBox> var1) {
        if (var0 != null && var1 != null) {
            class_9801 var14;
            class_1921 var2 = EspRenderLayers.FILLED;
            class_287 var3 = null;
            Matrix4f var4 = var0.method_23760().method_23761();
            for (ColoredBox var6 : var1) {
                if (var6 == null || var6.color() == null || var6.color().getAlpha() <= 0 || !RenderVisibility.box(var6.x1(), var6.y1(), var6.z1(), var6.x2(), var6.y2(), var6.z2())) continue;
                if (var3 == null) {
                    var3 = class_289.method_1348().method_60827(var2.method_23033(), var2.method_23031());
                }
                int var7 = var6.color().getRGB();
                float var8 = (float)(var6.x1() - RenderUtils.worldRenderOrigin.field_1352);
                float var9 = (float)(var6.y1() - RenderUtils.worldRenderOrigin.field_1351);
                float var10 = (float)(var6.z1() - RenderUtils.worldRenderOrigin.field_1350);
                float var11 = (float)(var6.x2() - RenderUtils.worldRenderOrigin.field_1352);
                float var12 = (float)(var6.y2() - RenderUtils.worldRenderOrigin.field_1351);
                float var13 = (float)(var6.z2() - RenderUtils.worldRenderOrigin.field_1350);
                RenderUtils.quad(var3, var4, var7, var8, var9, var10, var11, var9, var10, var11, var12, var10, var8, var12, var10);
                RenderUtils.quad(var3, var4, var7, var8, var9, var13, var8, var12, var13, var11, var12, var13, var11, var9, var13);
                RenderUtils.quad(var3, var4, var7, var8, var9, var10, var8, var12, var10, var8, var12, var13, var8, var9, var13);
                RenderUtils.quad(var3, var4, var7, var11, var9, var10, var11, var9, var13, var11, var12, var13, var11, var12, var10);
                RenderUtils.quad(var3, var4, var7, var8, var9, var10, var8, var9, var13, var11, var9, var13, var11, var9, var10);
                RenderUtils.quad(var3, var4, var7, var8, var12, var10, var11, var12, var10, var11, var12, var13, var8, var12, var13);
            }
            class_9801 class_98012 = var14 = var3 == null ? null : var3.method_60794();
            if (var14 != null) {
                var2.method_60895(var14);
            }
        }
    }

    public static void renderLine(class_4587 var0, Color var1, class_243 var2, class_243 var3) {
        RenderUtils.renderLines(var0, List.of(new ColoredLine(var2, var3, var1)));
    }

    public static void applyCameraTransform(class_4587 var0, class_4184 var1) {
        class_243 var2;
        worldRenderOrigin = var2 = var1.method_71156();
        var0.method_22907((Quaternionfc)new Quaternionf((Quaternionfc)var1.method_23767()).conjugate());
    }

    public static void addBoxOutline(List<ColoredLine> var0, double var1, double var3, double var5, double var7, double var9, double var11, Color var13, float var14) {
        if (var0 != null && var13 != null && var13.getAlpha() != 0 && RenderVisibility.box(var1, var3, var5, var7, var9, var11)) {
            class_243 var15 = new class_243(var1, var3, var5);
            class_243 var16 = new class_243(var7, var3, var5);
            class_243 var17 = new class_243(var1, var9, var5);
            class_243 var18 = new class_243(var7, var9, var5);
            class_243 var19 = new class_243(var1, var3, var11);
            class_243 var20 = new class_243(var7, var3, var11);
            class_243 var21 = new class_243(var1, var9, var11);
            class_243 var22 = new class_243(var7, var9, var11);
            var0.add(new ColoredLine(var15, var16, var13, var14));
            var0.add(new ColoredLine(var16, var20, var13, var14));
            var0.add(new ColoredLine(var20, var19, var13, var14));
            var0.add(new ColoredLine(var19, var15, var13, var14));
            var0.add(new ColoredLine(var17, var18, var13, var14));
            var0.add(new ColoredLine(var18, var22, var13, var14));
            var0.add(new ColoredLine(var22, var21, var13, var14));
            var0.add(new ColoredLine(var21, var17, var13, var14));
            var0.add(new ColoredLine(var15, var17, var13, var14));
            var0.add(new ColoredLine(var16, var18, var13, var14));
            var0.add(new ColoredLine(var20, var22, var13, var14));
            var0.add(new ColoredLine(var19, var21, var13, var14));
        }
    }

    public static void renderLines(class_4587 var0, Iterable<ColoredLine> var1) {
        if (var0 != null && var1 != null) {
            class_9801 var27;
            class_1921 var2 = EspRenderLayers.LINES;
            class_287 var3 = null;
            class_4587.class_4665 var4 = var0.method_23760();
            for (ColoredLine var6 : var1) {
                double var13;
                double var19;
                float var23;
                double var11;
                double var17;
                float var22;
                double var9;
                double var15;
                float var21;
                float var24;
                if (var6 == null || var6.color() == null || var6.color().getAlpha() <= 0 || var6.start() == null || var6.end() == null) continue;
                class_243 var7 = var6.start();
                class_243 var8 = var6.end();
                if (!RenderVisibility.box(var7.field_1352, var7.field_1351, var7.field_1350, var8.field_1352, var8.field_1351, var8.field_1350) || (var24 = class_3532.method_15355((float)((var21 = (float)((var15 = var8.field_1352 - RenderUtils.worldRenderOrigin.field_1352) - (var9 = var7.field_1352 - RenderUtils.worldRenderOrigin.field_1352))) * var21 + (var22 = (float)((var17 = var8.field_1351 - RenderUtils.worldRenderOrigin.field_1351) - (var11 = var7.field_1351 - RenderUtils.worldRenderOrigin.field_1351))) * var22 + (var23 = (float)((var19 = var8.field_1350 - RenderUtils.worldRenderOrigin.field_1350) - (var13 = var7.field_1350 - RenderUtils.worldRenderOrigin.field_1350))) * var23))) <= 1.0E-4f) continue;
                var21 /= var24;
                var22 /= var24;
                var23 /= var24;
                if (var3 == null) {
                    var3 = class_289.method_1348().method_60827(var2.method_23033(), var2.method_23031());
                }
                int var25 = var6.color().getRGB();
                float var26 = class_3532.method_15363((float)var6.width(), (float)1.0f, (float)10.0f);
                var3.method_56824(var4, (float)var9, (float)var11, (float)var13).method_39415(var25).method_60831(var4, var21, var22, var23).method_75298(var26);
                var3.method_56824(var4, (float)var15, (float)var17, (float)var19).method_39415(var25).method_60831(var4, var21, var22, var23).method_75298(var26);
            }
            class_9801 class_98012 = var27 = var3 == null ? null : var3.method_60794();
            if (var27 != null) {
                var2.method_60895(var27);
            }
        }
    }

    private static void quad(class_287 var0, Matrix4f var1, int var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14) {
        var0.method_22918((Matrix4fc)var1, var3, var4, var5).method_39415(var2);
        var0.method_22918((Matrix4fc)var1, var6, var7, var8).method_39415(var2);
        var0.method_22918((Matrix4fc)var1, var9, var10, var11).method_39415(var2);
        var0.method_22918((Matrix4fc)var1, var12, var13, var14).method_39415(var2);
    }

    public static void drawItem(class_332 var0, class_1799 var1, int var2, int var3, float var4, int var5) {
        if (!var1.method_7960()) {
            RenderUtils.setContext(var0);
            Matrix3x2fStack var6 = var0.method_51448();
            var6.pushMatrix();
            var6.translate((float)var2, (float)var3);
            float var7 = var4 / 16.0f;
            var6.scale(var7, var7);
            var0.method_51427(var1, 0, 0);
            var6.popMatrix();
        }
    }

    static {
        worldRenderOrigin = class_243.field_1353;
        inverseProjectionScratch = new Matrix4f();
        inverseViewScratch = new Matrix4f();
        centerScratch = new Vector4f();
    }

    public record ColoredBox(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
    }

    private static final class EspRenderLayers {
        private static final class_1921 FILLED = EspRenderLayers.createFilled();
        private static final class_1921 LINES = EspRenderLayers.createLines();

        private EspRenderLayers() {
        }

        private static class_1921 createFilled() {
            RenderPipeline var0 = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"hook", (String)"pipeline/esp_filled")).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withVertexShader("core/position_color").withFragmentShader("core/position_color").withBlend(BlendFunction.TRANSLUCENT).withCull(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_27382).build();
            var0 = RenderPipelinesAccessor.hook$register(var0);
            return RenderLayerAccessor.hook$create("hook_esp_filled", class_12247.method_75927((RenderPipeline)var0).method_75937().method_75929(1536).method_75938());
        }

        private static class_1921 createLines() {
            RenderPipeline var0 = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"hook", (String)"pipeline/esp_lines")).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withUniform("Globals", class_10789.field_60031).withVertexShader("core/rendertype_lines").withFragmentShader("core/rendertype_lines").withBlend(BlendFunction.TRANSLUCENT).withCull(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withVertexFormat(class_290.field_63455, VertexFormat.class_5596.field_27377).build();
            var0 = RenderPipelinesAccessor.hook$register(var0);
            return RenderLayerAccessor.hook$create("hook_esp_lines", class_12247.method_75927((RenderPipeline)var0).method_75937().method_75929(256).method_75938());
        }
    }

    public record ColoredLine(class_243 start, class_243 end, Color color, float width) {
        public ColoredLine(class_243 var1, class_243 var2, Color var3) {
            this(var1, var2, var3, 2.0f);
        }
    }
}

