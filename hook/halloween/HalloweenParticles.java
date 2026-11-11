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
 *  net.minecraft.class_243
 *  net.minecraft.class_276
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
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
import hook.halloween.HalloweenShaders;
import hook.halloween.HalloweenSky;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import java.util.Random;
import net.minecraft.class_1011;
import net.minecraft.class_10789;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_9801;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;

public final class HalloweenParticles {
    public static final int SMOKE = 0;
    public static final int GLOW = 1;
    public static final int SPARK = 2;
    public static final int EMBER = 3;
    public static final int DUST = 4;
    private static final int MAX = 1400;
    private static final int UBO_SIZE = 96;
    private static final RenderPipeline SMOKE_PIPE = HalloweenParticles.pipeline("pipeline/hw_particle_smoke", BlendFunction.TRANSLUCENT);
    private static final RenderPipeline GLOW_PIPE = HalloweenParticles.pipeline("pipeline/hw_particle_glow", BlendFunction.LIGHTNING);
    private static final double[] px = new double[1400];
    private static final double[] py = new double[1400];
    private static final double[] pz = new double[1400];
    private static final float[] vx = new float[1400];
    private static final float[] vy = new float[1400];
    private static final float[] vz = new float[1400];
    private static final float[] age = new float[1400];
    private static final float[] life = new float[1400];
    private static final float[] size = new float[1400];
    private static final float[] grow = new float[1400];
    private static final float[] rot = new float[1400];
    private static final float[] spin = new float[1400];
    private static final float[] seed = new float[1400];
    private static final float[] cr = new float[1400];
    private static final float[] cg = new float[1400];
    private static final float[] cb = new float[1400];
    private static final float[] ca = new float[1400];
    private static final int[] kind = new int[1400];
    private static int count;
    private static float clock;
    private static final Random RANDOM;
    private static GpuTexture smokeTex;
    private static GpuTextureView smokeView;
    private static GpuTexture glowTex;
    private static GpuTextureView glowView;
    private static GpuBuffer ubo;
    private static ByteBuffer data;

    private HalloweenParticles() {
    }

    private static RenderPipeline pipeline(String var0, BlendFunction var1) {
        return RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"hook", (String)var0)).withVertexShader(HalloweenShaders.PARTICLE).withFragmentShader(HalloweenShaders.PARTICLE).withUniform("Projection", class_10789.field_60031).withUniform("Fog", class_10789.field_60031).withUniform("MistData", class_10789.field_60031).withSampler("Sampler0").withSampler("DepthSampler").withBlend(var1).withDepthWrite(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withCull(false).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).build();
    }

    public static Random random() {
        return RANDOM;
    }

    public static void spawn(int var0, double var1, double var3, double var5, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15, float var16) {
        int var17 = count < 1400 ? count++ : RANDOM.nextInt(1400);
        HalloweenParticles.kind[var17] = var0;
        HalloweenParticles.px[var17] = var1;
        HalloweenParticles.py[var17] = var3;
        HalloweenParticles.pz[var17] = var5;
        HalloweenParticles.vx[var17] = var7;
        HalloweenParticles.vy[var17] = var8;
        HalloweenParticles.vz[var17] = var9;
        HalloweenParticles.size[var17] = var10;
        HalloweenParticles.grow[var17] = var11;
        HalloweenParticles.life[var17] = Math.max(0.05f, var12);
        HalloweenParticles.age[var17] = 0.0f;
        HalloweenParticles.rot[var17] = RANDOM.nextFloat() * 6.2832f;
        HalloweenParticles.spin[var17] = (RANDOM.nextFloat() - 0.5f) * (var0 == 0 ? 0.9f : 0.0f);
        HalloweenParticles.seed[var17] = RANDOM.nextFloat() * 100.0f;
        HalloweenParticles.cr[var17] = var13;
        HalloweenParticles.cg[var17] = var14;
        HalloweenParticles.cb[var17] = var15;
        HalloweenParticles.ca[var17] = var16;
    }

    public static int count() {
        return count;
    }

    public static void clear() {
        count = 0;
    }

    public static void update(float var0) {
        if (!(var0 <= 0.0f) && count != 0) {
            float var1 = clock += var0;
            for (int var2 = count - 1; var2 >= 0; --var2) {
                float var5;
                float var4;
                HalloweenParticles.age[var2] = age[var2] + var0;
                if (age[var2] >= life[var2]) {
                    HalloweenParticles.kill(var2);
                    continue;
                }
                int var3 = kind[var2];
                float var6 = switch (var3) {
                    case 0 -> {
                        var4 = 1.5f;
                        var5 = 0.32f;
                        yield 0.55f;
                    }
                    case 1 -> {
                        var4 = 1.8f;
                        var5 = 0.18f;
                        yield 0.45f;
                    }
                    case 2 -> {
                        var4 = 1.1f;
                        var5 = -0.9f;
                        yield 0.25f;
                    }
                    case 3 -> {
                        var4 = 0.9f;
                        var5 = 0.55f;
                        yield 1.1f;
                    }
                    default -> {
                        var4 = 2.6f;
                        var5 = 0.04f;
                        yield 0.35f;
                    }
                };
                float var7 = seed[var2];
                float var8 = (float)px[var2];
                float var9 = (float)py[var2];
                float var10 = (float)pz[var2];
                float var11 = (float)(Math.sin((double)var9 * 1.7 + (double)var1 * 1.3 + (double)var7) + Math.sin((double)var10 * 2.3 - (double)var1 * 0.7 + (double)var7 * 0.5)) * 0.5f;
                float var12 = (float)Math.sin((double)var8 * 1.9 + (double)var1 * 0.9 + (double)var7 * 1.3) * 0.35f;
                float var13 = (float)(Math.cos((double)var8 * 1.5 - (double)var1 * 1.1 + (double)var7) + Math.sin((double)var9 * 2.1 + (double)var1 * 0.6)) * 0.5f;
                HalloweenParticles.vx[var2] = vx[var2] + var11 * var6 * var0;
                HalloweenParticles.vy[var2] = vy[var2] + (var12 * var6 + var5) * var0;
                HalloweenParticles.vz[var2] = vz[var2] + var13 * var6 * var0;
                float var14 = (float)Math.exp(-var4 * var0);
                HalloweenParticles.vx[var2] = vx[var2] * var14;
                HalloweenParticles.vy[var2] = vy[var2] * var14;
                HalloweenParticles.vz[var2] = vz[var2] * var14;
                HalloweenParticles.px[var2] = px[var2] + (double)(vx[var2] * var0);
                HalloweenParticles.py[var2] = py[var2] + (double)(vy[var2] * var0);
                HalloweenParticles.pz[var2] = pz[var2] + (double)(vz[var2] * var0);
                HalloweenParticles.rot[var2] = rot[var2] + spin[var2] * var0;
            }
        }
    }

    private static void kill(int var0) {
        int var1;
        if (var0 != (var1 = --count)) {
            HalloweenParticles.kind[var0] = kind[var1];
            HalloweenParticles.px[var0] = px[var1];
            HalloweenParticles.py[var0] = py[var1];
            HalloweenParticles.pz[var0] = pz[var1];
            HalloweenParticles.vx[var0] = vx[var1];
            HalloweenParticles.vy[var0] = vy[var1];
            HalloweenParticles.vz[var0] = vz[var1];
            HalloweenParticles.age[var0] = age[var1];
            HalloweenParticles.life[var0] = life[var1];
            HalloweenParticles.size[var0] = size[var1];
            HalloweenParticles.grow[var0] = grow[var1];
            HalloweenParticles.rot[var0] = rot[var1];
            HalloweenParticles.spin[var0] = spin[var1];
            HalloweenParticles.seed[var0] = seed[var1];
            HalloweenParticles.cr[var0] = cr[var1];
            HalloweenParticles.cg[var0] = cg[var1];
            HalloweenParticles.cb[var0] = cb[var1];
            HalloweenParticles.ca[var0] = ca[var1];
        }
    }

    public static void render(class_4184 var0) {
        if (count != 0 && var0 != null && HalloweenSky.hasMatrices() && HalloweenShaders.ready(SMOKE_PIPE) && HalloweenShaders.ready(GLOW_PIPE)) {
            try {
                HalloweenParticles.ensure();
                class_243 var1 = var0.method_71156();
                Matrix4f var2 = HalloweenSky.view();
                FrustumIntersection var3 = new FrustumIntersection((Matrix4fc)new Matrix4f((Matrix4fc)HalloweenSky.projection()).mul((Matrix4fc)var2));
                class_310 var4 = class_310.method_1551();
                class_276 var5 = var4.method_1522();
                Matrix4f var6 = HalloweenSky.projection();
                float var7 = var6.m32() / (var6.m22() - 1.0f);
                float var8 = var6.m32() / (var6.m22() + 1.0f);
                data.clear();
                var2.get(0, data);
                HalloweenSky.put4(data, 64, var7, var8, 0.35f, 0.0f);
                HalloweenSky.put4(data, 80, var5.field_1482, var5.field_1481, 0.0f, 0.0f);
                data.position(0);
                data.limit(96);
                CommandEncoder var9 = RenderSystem.getDevice().createCommandEncoder();
                var9.writeToBuffer(ubo.slice(), data);
                HalloweenParticles.draw(true, var1, var2, var3, var5, var9);
                HalloweenParticles.draw(false, var1, var2, var3, var5, var9);
            }
            catch (Throwable var10) {
                System.err.println("Hook: Halloween particles skipped: " + String.valueOf(var10));
            }
        }
    }

    private static void draw(boolean var0, class_243 var1, Matrix4f var2, FrustumIntersection var3, class_276 var4, CommandEncoder var5) {
        float var6 = var2.m00();
        float var7 = var2.m10();
        float var8 = var2.m20();
        float var9 = var2.m01();
        float var10 = var2.m11();
        float var11 = var2.m21();
        float var12 = -var2.m02();
        float var13 = -var2.m12();
        float var14 = -var2.m22();
        class_287 var15 = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        int var16 = 0;
        for (int var17 = 0; var17 < count; ++var17) {
            float var34;
            float var33;
            float var30;
            float var29;
            float var31;
            float var28;
            float var32;
            float var35;
            float var23;
            float var18;
            float var22;
            float var21;
            float var20;
            float var19;
            if (kind[var17] == 0 != var0 || !var3.testSphere(var19 = (float)(px[var17] - var1.field_1352), var20 = (float)(py[var17] - var1.field_1351), var21 = (float)(pz[var17] - var1.field_1350), (var22 = size[var17] * (1.0f + (grow[var17] - 1.0f) * (1.0f - (1.0f - (var18 = age[var17] / life[var17])) * (1.0f - var18)))) * 3.0f + 0.5f)) continue;
            if (kind[var17] == 0) {
                var23 = Math.min(1.0f, var18 / 0.18f) * (1.0f - var18) * (1.0f - var18);
            } else {
                float var24 = kind[var17] != 3 && kind[var17] != 4 ? 1.0f : 0.6f + 0.4f * (float)Math.sin(age[var17] * (kind[var17] == 4 ? 6.0f : 17.0f) + seed[var17]);
                var23 = Math.min(1.0f, var18 / 0.08f) * (float)Math.pow(1.0f - var18, 1.4f) * var24;
            }
            if ((var23 *= ca[var17]) < 0.004f) continue;
            int var55 = Math.min(255, Math.round(cr[var17] * 255.0f));
            int var25 = Math.min(255, Math.round(cg[var17] * 255.0f));
            int var26 = Math.min(255, Math.round(cb[var17] * 255.0f));
            int var27 = Math.min(255, Math.round(var23 * 255.0f));
            if (kind[var17] == 2 && (var35 = (float)Math.sqrt((var32 = (var28 = vx[var17]) - var12 * (var31 = var28 * var12 + (var29 = vy[var17]) * var13 + (var30 = vz[var17]) * var14)) * var32 + (var33 = var29 - var13 * var31) * var33 + (var34 = var30 - var14 * var31) * var34)) > 0.05f) {
                float var65 = var32 / var35;
                float var66 = var33 / var35;
                float var67 = var34 / var35;
                float var68 = var66 * var14 - var67 * var13;
                float var40 = var67 * var12 - var65 * var14;
                float var41 = var65 * var13 - var66 * var12;
                float var42 = Math.min(1.4f, var35 * 0.07f) + var22;
                var15.method_22912(var19 - var65 * var42 - var68 * var22, var20 - var66 * var42 - var40 * var22, var21 - var67 * var42 - var41 * var22).method_22913(0.0f, 1.0f).method_1336(var55, var25, var26, var27);
                var15.method_22912(var19 - var65 * var42 + var68 * var22, var20 - var66 * var42 + var40 * var22, var21 - var67 * var42 + var41 * var22).method_22913(1.0f, 1.0f).method_1336(var55, var25, var26, var27);
                var15.method_22912(var19 + var65 * var22 + var68 * var22, var20 + var66 * var22 + var40 * var22, var21 + var67 * var22 + var41 * var22).method_22913(1.0f, 0.0f).method_1336(var55, var25, var26, var27);
                var15.method_22912(var19 + var65 * var22 - var68 * var22, var20 + var66 * var22 - var40 * var22, var21 + var67 * var22 - var41 * var22).method_22913(0.0f, 0.0f).method_1336(var55, var25, var26, var27);
                ++var16;
                continue;
            }
            float var57 = (float)Math.cos(rot[var17]);
            float var58 = (float)Math.sin(rot[var17]);
            float var59 = (var6 * var57 + var9 * var58) * var22;
            float var60 = (var7 * var57 + var10 * var58) * var22;
            float var61 = (var8 * var57 + var11 * var58) * var22;
            float var62 = (var9 * var57 - var6 * var58) * var22;
            float var63 = (var10 * var57 - var7 * var58) * var22;
            float var64 = (var11 * var57 - var8 * var58) * var22;
            float var36 = 0.0f;
            float var37 = 0.0f;
            float var38 = 1.0f;
            if (var0) {
                int var39 = (int)(seed[var17] * 4.0f) & 3;
                var36 = (float)(var39 & 1) * 0.5f;
                var37 = (float)(var39 >> 1) * 0.5f;
                var38 = 0.5f;
            }
            var15.method_22912(var19 - var59 - var62, var20 - var60 - var63, var21 - var61 - var64).method_22913(var36, var37 + var38).method_1336(var55, var25, var26, var27);
            var15.method_22912(var19 + var59 - var62, var20 + var60 - var63, var21 + var61 - var64).method_22913(var36 + var38, var37 + var38).method_1336(var55, var25, var26, var27);
            var15.method_22912(var19 + var59 + var62, var20 + var60 + var63, var21 + var61 + var64).method_22913(var36 + var38, var37).method_1336(var55, var25, var26, var27);
            var15.method_22912(var19 - var59 + var62, var20 - var60 + var63, var21 - var61 + var64).method_22913(var36, var37).method_1336(var55, var25, var26, var27);
            ++var16;
        }
        try (class_9801 built = var15.method_60794();){
            if (built == null || var16 == 0) {
                return;
            }
            RenderPipeline pipeline = var0 ? SMOKE_PIPE : GLOW_PIPE;
            GpuBuffer vertices = pipeline.getVertexFormat().uploadImmediateVertexBuffer(built.method_60818());
            RenderSystem.class_5590 indices = RenderSystem.getSequentialBuffer((VertexFormat.class_5596)VertexFormat.class_5596.field_27382);
            GpuBuffer indexBuffer = indices.method_68274(built.method_60822().comp_751());
            GpuTextureView colorView = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : var4.method_71639();
            try (RenderPass pass = var5.createRenderPass(() -> "Hook Halloween particles", colorView, OptionalInt.empty());){
                pass.setPipeline(pipeline);
                RenderSystem.bindDefaultUniforms((RenderPass)pass);
                pass.setUniform("MistData", ubo);
                pass.bindTexture("Sampler0", var0 ? smokeView : glowView, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
                pass.bindTexture("DepthSampler", var4.method_71640(), RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
                pass.setVertexBuffer(0, vertices);
                pass.setIndexBuffer(indexBuffer, indices.method_31924());
                pass.drawIndexed(0, 0, built.method_60822().comp_751(), 1);
            }
        }
    }

    private static float hash(int var0, int var1, int var2) {
        int var3 = var0 * 73856093 ^ var1 * 19349663 ^ var2 * 83492791;
        var3 = (var3 ^ var3 >>> 13) * 1274126177;
        return (float)((var3 ^ var3 >>> 16) & 0xFFFF) / 65535.0f;
    }

    private static float noise(float var0, float var1, int var2) {
        int var3 = (int)Math.floor(var0);
        int var4 = (int)Math.floor(var1);
        float var5 = var0 - (float)var3;
        float var6 = var1 - (float)var4;
        var5 = var5 * var5 * (3.0f - 2.0f * var5);
        var6 = var6 * var6 * (3.0f - 2.0f * var6);
        float var7 = HalloweenParticles.hash(var3, var4, var2);
        float var8 = HalloweenParticles.hash(var3 + 1, var4, var2);
        float var9 = HalloweenParticles.hash(var3, var4 + 1, var2);
        float var10 = HalloweenParticles.hash(var3 + 1, var4 + 1, var2);
        return var7 + (var8 - var7) * var5 + (var9 - var7) * var6 + (var7 - var8 - var9 + var10) * var5 * var6;
    }

    private static void ensure() {
        if (smokeTex == null) {
            GpuDevice var0 = RenderSystem.getDevice();
            int var1 = 96;
            int var2 = var1 * 2;
            smokeTex = var0.createTexture(() -> "Hook smoke", 5, TextureFormat.RGBA8, var2, var2, 1, 1);
            try (class_1011 var3 = new class_1011(var2, var2, false);){
                for (int var4 = 0; var4 < 4; ++var4) {
                    int var5 = (var4 & 1) * var1;
                    int var6 = (var4 >> 1) * var1;
                    for (int var7 = 0; var7 < var1; ++var7) {
                        for (int var8 = 0; var8 < var1; ++var8) {
                            float var9 = ((float)var8 + 0.5f) / (float)var1 * 2.0f - 1.0f;
                            float var10 = ((float)var7 + 0.5f) / (float)var1 * 2.0f - 1.0f;
                            float var11 = 0.0f;
                            float var12 = 0.5f;
                            float var13 = 2.2f;
                            for (int var14 = 0; var14 < 4; ++var14) {
                                var11 += var12 * HalloweenParticles.noise(var9 * var13 + (float)var4 * 7.1f, var10 * var13 - (float)var4 * 3.3f, var4 * 11 + var14);
                                var12 *= 0.5f;
                                var13 *= 2.05f;
                            }
                            float var31 = (float)Math.sqrt(var9 * var9 + var10 * var10) / (0.62f + 0.42f * var11);
                            float var15 = Math.max(0.0f, 1.0f - var31);
                            var15 = var15 * var15 * (3.0f - 2.0f * var15);
                            float var16 = Math.max(0.0f, Math.min(1.0f, var15 * (0.45f + 0.9f * var11)));
                            int var17 = Math.round(var16 * 255.0f);
                            var3.method_61941(var5 + var8, var6 + var7, var17 << 24 | var17 << 16 | var17 << 8 | var17);
                        }
                    }
                }
                var0.createCommandEncoder().writeToTexture(smokeTex, var3);
            }
            smokeView = var0.createTextureView(smokeTex);
            int var22 = 64;
            glowTex = var0.createTexture(() -> "Hook glow", 5, TextureFormat.RGBA8, var22, var22, 1, 1);
            try (class_1011 var23 = new class_1011(var22, var22, false);){
                for (int var24 = 0; var24 < var22; ++var24) {
                    for (int var25 = 0; var25 < var22; ++var25) {
                        float var26 = ((float)var25 + 0.5f) / (float)var22 * 2.0f - 1.0f;
                        float var27 = ((float)var24 + 0.5f) / (float)var22 * 2.0f - 1.0f;
                        float var28 = var26 * var26 + var27 * var27;
                        float var29 = (float)(Math.exp((double)(-var28) * 26.0) * 0.85 + Math.exp((double)(-var28) * 5.0) * 0.32) * Math.max(0.0f, 1.0f - (float)Math.sqrt(var28));
                        int var30 = Math.round(Math.min(1.0f, var29) * 255.0f);
                        var23.method_61941(var25, var24, var30 << 24 | var30 << 16 | var30 << 8 | var30);
                    }
                }
                var0.createCommandEncoder().writeToTexture(glowTex, var23);
            }
            glowView = var0.createTextureView(glowTex);
            ubo = var0.createBuffer(() -> "Hook Halloween particles", 136, 96L);
            data = BufferUtils.createByteBuffer((int)96);
        }
    }

    public static void release() {
        count = 0;
        for (AutoCloseable var3 : new AutoCloseable[]{smokeView, smokeTex, glowView, glowTex, ubo}) {
            if (var3 == null) continue;
            try {
                var3.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        smokeView = null;
        smokeTex = null;
        glowView = null;
        glowTex = null;
        ubo = null;
        data = null;
    }

    static {
        RANDOM = new Random();
    }
}

