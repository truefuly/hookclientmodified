/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.systems.RenderSystem$class_5590
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.textures.TextureFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1011
 *  net.minecraft.class_10789
 *  net.minecraft.class_1944
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_243
 *  net.minecraft.class_276
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2902$class_2903
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  net.minecraft.class_638
 *  net.minecraft.class_9801
 *  org.joml.FrustumIntersection
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.lwjgl.BufferUtils
 */
package hook.halloween;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import hook.halloween.HalloweenAtmosphere;
import hook.halloween.HalloweenShaders;
import hook.halloween.HalloweenSky;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.class_1011;
import net.minecraft.class_10789;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2902;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_9801;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;

public final class HalloweenMist {
    private static final int CELL = 7;
    private static final int RANGE = 6;
    private static final float FAR = 40.0f;
    private static final int UBO_SIZE = 96;
    public static final RenderPipeline PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"hook", (String)"pipeline/hw_mist")).withVertexShader(HalloweenShaders.MIST).withFragmentShader(HalloweenShaders.MIST).withUniform("Projection", class_10789.field_60031).withUniform("Fog", class_10789.field_60031).withUniform("MistData", class_10789.field_60031).withSampler("Sampler0").withSampler("DepthSampler").withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withCull(false).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).build();
    private static GpuTexture puff;
    private static GpuTextureView puffView;
    private static GpuBuffer ubo;
    private static ByteBuffer data;
    private static float fade;
    private static long lastNanos;

    private HalloweenMist() {
    }

    private static float hash(int var0, int var1, int var2) {
        int var3 = var0 * 73856093 ^ var1 * 19349663 ^ var2 * 83492791;
        var3 = (var3 ^ var3 >>> 13) * 1274126177;
        return (float)((var3 ^ var3 >>> 16) & 0xFFFF) / 65535.0f;
    }

    public static void draw(class_4184 var0) {
        class_310 var1 = class_310.method_1551();
        class_638 var2 = var1.field_1687;
        long var3 = System.nanoTime();
        float var5 = lastNanos == 0L ? 0.016f : Math.min(0.1f, (float)(var3 - lastNanos) / 1.0E9f);
        lastNanos = var3;
        float var6 = HalloweenAtmosphere.fogAmount() * HalloweenAtmosphere.outdoor() * HalloweenAtmosphere.nightness();
        if (!((fade += (var6 - fade) * (1.0f - (float)Math.exp(-var5 / 0.9f))) < 0.004f) && var2 != null && var0 != null && HalloweenSky.hasMatrices() && HalloweenShaders.ready(PIPELINE)) {
            try {
                HalloweenMist.ensure();
                class_243 var7 = var0.method_71156();
                Matrix4f var8 = HalloweenSky.view();
                Matrix4f var9 = new Matrix4f((Matrix4fc)HalloweenSky.projection()).mul((Matrix4fc)var8);
                FrustumIntersection var10 = new FrustumIntersection((Matrix4fc)var9);
                float var11 = var8.m00();
                float var12 = var8.m10();
                float var13 = var8.m20();
                float var14 = var8.m01();
                float var15 = var8.m11();
                float var16 = var8.m21();
                float var17 = ((float)(var2.method_75260() % 240000L) + var1.method_61966().method_60637(false)) / 20.0f;
                float var18 = 1.0f - 0.55f * HalloweenAtmosphere.darkness();
                int var19 = Math.round(150.0f * var18);
                int var20 = Math.round(142.0f * var18);
                int var21 = Math.round(176.0f * var18);
                int var22 = Math.floorDiv((int)Math.floor(var7.field_1352), 7);
                int var23 = Math.floorDiv((int)Math.floor(var7.field_1350), 7);
                class_287 var24 = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
                int var25 = 0;
                class_2338.class_2339 var26 = new class_2338.class_2339();
                for (int var27 = -6; var27 <= 6; ++var27) {
                    for (int var28 = -6; var28 <= 6; ++var28) {
                        int var29 = var22 + var27;
                        int var30 = var23 + var28;
                        for (int var31 = 0; var31 < 2; ++var31) {
                            int var41;
                            double var42;
                            double var44;
                            int var40;
                            float var32 = HalloweenMist.hash(var29, var30, var31);
                            if (var32 < 0.18f) continue;
                            float var33 = (var32 * 7.0f + var17 * (0.035f + 0.02f * var32)) % 1.0f;
                            float var34 = (float)Math.sin(Math.PI * (double)var33);
                            double var35 = (double)((float)(var29 * 7) + HalloweenMist.hash(var29, var30, 9 + var31) * 7.0f) + (double)(var33 * 7.0f) * 0.9;
                            double var37 = (double)((float)(var30 * 7) + HalloweenMist.hash(var29, var30, 17 + var31) * 7.0f) + (double)(var33 * 7.0f) * 0.35;
                            int var39 = (int)Math.floor(var35);
                            if (!var2.method_8393(var39 >> 4, (var40 = (int)Math.floor(var37)) >> 4) || (var44 = (var42 = (double)(var41 = var2.method_8624(class_2902.class_2903.field_13197, var39, var40)) + 0.55 + (double)var31 * 0.9 + (double)HalloweenMist.hash(var29, var30, 31 + var31) * 0.8) - var7.field_1351) > 22.0 || var44 < -26.0) continue;
                            float var46 = 3.2f + var32 * 3.4f + (float)var31 * 1.2f;
                            float var47 = (float)(var35 - var7.field_1352);
                            float var48 = (float)var44;
                            float var49 = (float)(var37 - var7.field_1350);
                            float var50 = (float)Math.sqrt(var47 * var47 + var49 * var49);
                            float var51 = 1.0f - HalloweenMist.smooth((var50 - 28.0f) / 12.0f);
                            if (var51 <= 0.0f || !var10.testSphere(var47, var48, var49, var46 * 1.2f)) continue;
                            var26.method_10103(var39, var41, var40);
                            float var52 = (float)var2.method_8314(class_1944.field_9282, (class_2338)var26) / 15.0f;
                            float var53 = (0.1f + 0.1f * var32) * fade * var34 * var51 * (1.0f - 0.65f * var52);
                            if (var53 < 0.003f) continue;
                            int var54 = Math.min(255, Math.round(var53 * 255.0f));
                            float var55 = var31 == 0 ? 0.0f : 1.0f;
                            float var56 = 1.0f - var55;
                            var24.method_22912(var47 - var11 * var46 - var14 * var46, var48 - var12 * var46 - var15 * var46, var49 - var13 * var46 - var16 * var46).method_22913(var55, 1.0f).method_1336(var19, var20, var21, var54);
                            var24.method_22912(var47 + var11 * var46 - var14 * var46, var48 + var12 * var46 - var15 * var46, var49 + var13 * var46 - var16 * var46).method_22913(var56, 1.0f).method_1336(var19, var20, var21, var54);
                            var24.method_22912(var47 + var11 * var46 + var14 * var46, var48 + var12 * var46 + var15 * var46, var49 + var13 * var46 + var16 * var46).method_22913(var56, 0.0f).method_1336(var19, var20, var21, var54);
                            var24.method_22912(var47 - var11 * var46 + var14 * var46, var48 - var12 * var46 + var15 * var46, var49 - var13 * var46 + var16 * var46).method_22913(var55, 0.0f).method_1336(var19, var20, var21, var54);
                            ++var25;
                        }
                    }
                }
                class_9801 var62 = var24.method_60794();
                if (var62 == null || var25 == 0) {
                    if (var62 != null) {
                        var62.close();
                    }
                    return;
                }
                class_9801 var63 = var62;
                try {
                    class_310 var64 = class_310.method_1551();
                    class_276 var65 = var64.method_1522();
                    Matrix4f var66 = HalloweenSky.projection();
                    float var67 = var66.m32() / (var66.m22() - 1.0f);
                    float var68 = var66.m32() / (var66.m22() + 1.0f);
                    data.clear();
                    var8.get(0, data);
                    HalloweenSky.put4(data, 64, var67, var68, 1.6f, 0.0f);
                    HalloweenSky.put4(data, 80, var65.field_1482, var65.field_1481, 0.0f, 0.0f);
                    data.position(0);
                    data.limit(96);
                    CommandEncoder var69 = RenderSystem.getDevice().createCommandEncoder();
                    var69.writeToBuffer(ubo.slice(), data);
                    GpuBuffer var70 = PIPELINE.getVertexFormat().uploadImmediateVertexBuffer(var62.method_60818());
                    RenderSystem.class_5590 var36 = RenderSystem.getSequentialBuffer((VertexFormat.class_5596)VertexFormat.class_5596.field_27382);
                    GpuBuffer var71 = var36.method_68274(var62.method_60822().comp_751());
                    GpuTextureView var38 = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : var65.method_71639();
                    try (RenderPass var72 = var69.createRenderPass(() -> "Hook Halloween mist", var38, OptionalInt.empty());){
                        var72.setPipeline(PIPELINE);
                        RenderSystem.bindDefaultUniforms((RenderPass)var72);
                        var72.setUniform("MistData", ubo);
                        var72.bindTexture("Sampler0", puffView, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
                        var72.bindTexture("DepthSampler", var65.method_71640(), RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
                        var72.setVertexBuffer(0, var70);
                        var72.setIndexBuffer(var71, var36.method_31924());
                        var72.drawIndexed(0, 0, var62.method_60822().comp_751(), 1);
                    }
                }
                catch (Throwable var60) {
                    if (var62 != null) {
                        try {
                            var63.close();
                        }
                        catch (Throwable var57) {
                            var60.addSuppressed(var57);
                        }
                    }
                    throw var60;
                }
                if (var62 != null) {
                    var62.close();
                }
            }
            catch (Throwable var61) {
                System.err.println("Hook: Halloween mist skipped: " + String.valueOf(var61));
            }
        }
    }

    private static float smooth(float var0) {
        var0 = Math.max(0.0f, Math.min(1.0f, var0));
        return var0 * var0 * (3.0f - 2.0f * var0);
    }

    private static void ensure() {
        if (puff == null) {
            GpuDevice var0 = RenderSystem.getDevice();
            int var1 = 96;
            puff = var0.createTexture(() -> "Hook mist puff", 5, TextureFormat.RGBA8, var1, var1, 1, 1);
            try (class_1011 var2 = new class_1011(var1, var1, false);){
                for (int var3 = 0; var3 < var1; ++var3) {
                    for (int var4 = 0; var4 < var1; ++var4) {
                        float var5 = ((float)var4 + 0.5f) / (float)var1 * 2.0f - 1.0f;
                        float var6 = ((float)var3 + 0.5f) / (float)var1 * 2.0f - 1.0f;
                        float var7 = (float)Math.sqrt(var5 * var5 + var6 * var6);
                        float var8 = Math.max(0.0f, 1.0f - var7);
                        var8 = var8 * var8 * (3.0f - 2.0f * var8);
                        float var9 = 0.0f;
                        float var10 = 0.5f;
                        float var11 = 2.5f;
                        for (int var12 = 0; var12 < 4; ++var12) {
                            var9 += var10 * HalloweenMist.valueNoise(var5 * var11 + (float)var12 * 3.1f, var6 * var11 - (float)var12 * 1.7f);
                            var10 *= 0.5f;
                            var11 *= 2.1f;
                        }
                        float var17 = Math.max(0.0f, Math.min(1.0f, var8 * (0.55f + 0.75f * var9)));
                        int var13 = Math.round(var17 * 255.0f);
                        var2.method_61941(var4, var3, var13 << 24 | var13 << 16 | var13 << 8 | var13);
                    }
                }
                var0.createCommandEncoder().writeToTexture(puff, var2);
            }
            puffView = var0.createTextureView(puff);
            ubo = var0.createBuffer(() -> "Hook Halloween mist", 136, 96L);
            data = BufferUtils.createByteBuffer((int)96);
        }
    }

    private static float valueNoise(float var0, float var1) {
        int var2 = (int)Math.floor(var0);
        int var3 = (int)Math.floor(var1);
        float var4 = var0 - (float)var2;
        float var5 = var1 - (float)var3;
        var4 = var4 * var4 * (3.0f - 2.0f * var4);
        var5 = var5 * var5 * (3.0f - 2.0f * var5);
        float var6 = HalloweenMist.hash(var2, var3, 5);
        float var7 = HalloweenMist.hash(var2 + 1, var3, 5);
        float var8 = HalloweenMist.hash(var2, var3 + 1, 5);
        float var9 = HalloweenMist.hash(var2 + 1, var3 + 1, 5);
        return var6 + (var7 - var6) * var4 + (var8 - var6) * var5 + (var6 - var7 - var8 + var9) * var4 * var5;
    }

    public static void release() {
        for (AutoCloseable var3 : new AutoCloseable[]{puffView, puff, ubo}) {
            if (var3 == null) continue;
            try {
                var3.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        puffView = null;
        puff = null;
        ubo = null;
        data = null;
        fade = 0.0f;
    }
}

