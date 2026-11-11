/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.AddressMode
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.textures.TextureFormat
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5595
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  com.mojang.blaze3d.vertex.VertexFormatElement
 *  net.minecraft.class_1011
 *  net.minecraft.class_10789
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.lwjgl.BufferUtils
 */
package hook.halloween.witch;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import hook.halloween.HalloweenShaders;
import hook.halloween.witch.WitchAsset;
import hook.halloween.witch.WitchRig;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_10789;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;

public final class WitchRenderer {
    private static final int MAX_JOINTS = 80;
    private static final int HEADER = 272;
    private static final int UBO_SIZE = 5392;
    private static final VertexFormat FORMAT = class_290.field_1580;
    public static final RenderPipeline COLOR = WitchRenderer.base().withLocation(class_2960.method_60655((String)"hook", (String)"pipeline/hw_witch")).withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(true).build();
    public static final RenderPipeline DEPTH = WitchRenderer.base().withLocation(class_2960.method_60655((String)"hook", (String)"pipeline/hw_witch_depth")).withColorWrite(false).withDepthWrite(true).build();
    private static GpuBuffer witchVbo;
    private static GpuBuffer witchIbo;
    private static GpuBuffer broomVbo;
    private static GpuBuffer broomIbo;
    private static int witchIndices;
    private static int broomIndices;
    private static GpuTexture witchTex;
    private static GpuTexture emissiveTex;
    private static GpuTexture broomTex;
    private static GpuTexture blackTex;
    private static GpuTextureView witchView;
    private static GpuTextureView emissiveView;
    private static GpuTextureView broomView;
    private static GpuTextureView blackView;
    private static GpuBuffer witchUbo;
    private static GpuBuffer broomUbo;
    private static ByteBuffer scratch;
    private static boolean failed;

    private WitchRenderer() {
    }

    private static RenderPipeline.Builder base() {
        return RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withVertexShader(HalloweenShaders.WITCH).withFragmentShader(HalloweenShaders.WITCH).withUniform("Projection", class_10789.field_60031).withUniform("Fog", class_10789.field_60031).withUniform("WitchData", class_10789.field_60031).withSampler("Sampler0").withSampler("Sampler1").withSampler("Sampler2").withCull(false).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withVertexFormat(FORMAT, VertexFormat.class_5596.field_27379);
    }

    public static boolean uploaded() {
        return witchVbo != null;
    }

    public static boolean ensure(WitchAsset var0) {
        if (failed) {
            return false;
        }
        if (witchVbo != null) {
            return true;
        }
        try {
            GpuDevice var1 = RenderSystem.getDevice();
            witchVbo = WitchRenderer.vertices(var1, "Hook witch", var0.witch);
            witchIbo = WitchRenderer.indices(var1, "Hook witch indices", var0.witch);
            witchIndices = var0.witch.indices.length;
            broomVbo = WitchRenderer.vertices(var1, "Hook broom", var0.broom);
            broomIbo = WitchRenderer.indices(var1, "Hook broom indices", var0.broom);
            broomIndices = var0.broom.indices.length;
            witchTex = WitchRenderer.texture(var1, "/assets/hook/witch/witch.png");
            emissiveTex = WitchRenderer.texture(var1, "/assets/hook/witch/witch_emissive.png");
            broomTex = WitchRenderer.texture(var1, "/assets/hook/witch/broom.png");
            blackTex = var1.createTexture(() -> "Hook black", 5, TextureFormat.RGBA8, 1, 1, 1, 1);
            try (class_1011 var2 = new class_1011(1, 1, false);){
                var2.method_61941(0, 0, -16777216);
                var1.createCommandEncoder().writeToTexture(blackTex, var2);
            }
            witchView = var1.createTextureView(witchTex);
            emissiveView = var1.createTextureView(emissiveTex);
            broomView = var1.createTextureView(broomTex);
            blackView = var1.createTextureView(blackTex);
            witchUbo = var1.createBuffer(() -> "Hook witch joints", 136, 5392L);
            broomUbo = var1.createBuffer(() -> "Hook broom joints", 136, 5392L);
            scratch = BufferUtils.createByteBuffer((int)5392);
            return true;
        }
        catch (Throwable var7) {
            System.err.println("Hook: witch upload failed: " + String.valueOf(var7));
            failed = true;
            WitchRenderer.release();
            return false;
        }
    }

    private static GpuBuffer vertices(GpuDevice var0, String var1, WitchAsset.Mesh var2) {
        int var3 = FORMAT.getVertexSize();
        int var4 = FORMAT.getOffset(VertexFormatElement.POSITION);
        int var5 = FORMAT.getOffset(VertexFormatElement.COLOR);
        int var6 = FORMAT.getOffset(VertexFormatElement.UV0);
        int var7 = FORMAT.getOffset(VertexFormatElement.UV1);
        int var8 = FORMAT.getOffset(VertexFormatElement.UV2);
        int var9 = FORMAT.getOffset(VertexFormatElement.NORMAL);
        ByteBuffer var10 = BufferUtils.createByteBuffer((int)(var3 * var2.vertexCount)).order(ByteOrder.nativeOrder());
        for (int var11 = 0; var11 < var2.vertexCount; ++var11) {
            int var12 = var11 * var3;
            var10.putFloat(var12 + var4, var2.pos[var11 * 3]);
            var10.putFloat(var12 + var4 + 4, var2.pos[var11 * 3 + 1]);
            var10.putFloat(var12 + var4 + 8, var2.pos[var11 * 3 + 2]);
            for (int var13 = 0; var13 < 4; ++var13) {
                var10.put(var12 + var5 + var13, var2.joints[var11 * 4 + var13]);
            }
            var10.putFloat(var12 + var6, var2.uv[var11 * 2]);
            var10.putFloat(var12 + var6 + 4, var2.uv[var11 * 2 + 1]);
            var10.putShort(var12 + var7, var2.weights[var11 * 4]);
            var10.putShort(var12 + var7 + 2, var2.weights[var11 * 4 + 1]);
            var10.putShort(var12 + var8, var2.weights[var11 * 4 + 2]);
            var10.putShort(var12 + var8 + 2, var2.weights[var11 * 4 + 3]);
            var10.put(var12 + var9, var2.normal[var11 * 3]);
            var10.put(var12 + var9 + 1, var2.normal[var11 * 3 + 1]);
            var10.put(var12 + var9 + 2, var2.normal[var11 * 3 + 2]);
        }
        var10.position(0);
        var10.limit(var3 * var2.vertexCount);
        return var0.createBuffer(() -> var1, 32, var10);
    }

    private static GpuBuffer indices(GpuDevice var0, String var1, WitchAsset.Mesh var2) {
        ByteBuffer var3 = BufferUtils.createByteBuffer((int)(var2.indices.length * 4)).order(ByteOrder.nativeOrder());
        for (int var7 : var2.indices) {
            var3.putInt(var7);
        }
        var3.flip();
        return var0.createBuffer(() -> var1, 64, var3);
    }

    private static GpuTexture texture(GpuDevice var0, String var1) throws Exception {
        int var6;
        BufferedImage var2;
        try (InputStream var3 = WitchRenderer.class.getResourceAsStream(var1);){
            if (var3 == null) {
                throw new IllegalStateException("missing " + var1);
            }
            var2 = ImageIO.read(var3);
        }
        int var33 = var2.getWidth();
        int var4 = var2.getHeight();
        int[] var5 = var2.getRGB(0, 0, var33, var4, null, 0, var33);
        for (var6 = 1; var33 >> var6 >= 1 && var4 >> var6 >= 1 && var6 < 12; ++var6) {
        }
        GpuTexture var7 = var0.createTexture(() -> "Hook " + var1, 5, TextureFormat.RGBA8, var33, var4, 1, var6);
        CommandEncoder var8 = var0.createCommandEncoder();
        int var9 = var33;
        int var10 = var4;
        int[] var11 = var5;
        for (int var12 = 0; var12 < var6; ++var12) {
            try (class_1011 var13 = new class_1011(var9, var10, false);){
                for (int var14 = 0; var14 < var10; ++var14) {
                    for (int var15 = 0; var15 < var9; ++var15) {
                        var13.method_61941(var15, var14, var11[var14 * var9 + var15]);
                    }
                }
                var8.writeToTexture(var7, var13, var12, 0, 0, 0, var9, var10, 0, 0);
            }
            if (var12 + 1 >= var6) continue;
            int var34 = Math.max(1, var9 / 2);
            int var35 = Math.max(1, var10 / 2);
            int[] var36 = new int[var34 * var35];
            for (int var16 = 0; var16 < var35; ++var16) {
                for (int var17 = 0; var17 < var34; ++var17) {
                    long var18 = 0L;
                    long var20 = 0L;
                    long var22 = 0L;
                    long var24 = 0L;
                    for (int var26 = 0; var26 < 2; ++var26) {
                        for (int var27 = 0; var27 < 2; ++var27) {
                            int var28 = var11[Math.min(var10 - 1, var16 * 2 + var26) * var9 + Math.min(var9 - 1, var17 * 2 + var27)];
                            var18 += (long)(var28 >>> 24);
                            var20 += (long)(var28 >> 16 & 0xFF);
                            var22 += (long)(var28 >> 8 & 0xFF);
                            var24 += (long)(var28 & 0xFF);
                        }
                    }
                    var36[var16 * var34 + var17] = (int)(var18 / 4L) << 24 | (int)(var20 / 4L) << 16 | (int)(var22 / 4L) << 8 | (int)(var24 / 4L);
                }
            }
            var11 = var36;
            var9 = var34;
            var10 = var35;
        }
        return var7;
    }

    public static void draw(WitchRig var0, Matrix4f var1, Matrix4f var2, Look var3) {
        if (!(var3.alpha <= 0.003f) && witchVbo != null && HalloweenShaders.ready(COLOR)) {
            boolean var4;
            boolean bl = var4 = var3.alpha < 0.995f;
            if (!var4 || HalloweenShaders.ready(DEPTH)) {
                GpuTextureView var8;
                class_310 var5 = class_310.method_1551();
                class_276 var6 = var5.method_1522();
                GpuTextureView var7 = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : var6.method_71639();
                GpuTextureView gpuTextureView = var8 = RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : var6.method_71640();
                if (var7 != null && var8 != null) {
                    Matrix4f var9 = new Matrix4f((Matrix4fc)RenderSystem.getModelViewMatrix());
                    CommandEncoder var10 = RenderSystem.getDevice().createCommandEncoder();
                    WitchRenderer.writeUbo(var9, var1, var3, var3.emissive, var3.rim, var0.skin, var0.count);
                    var10.writeToBuffer(witchUbo.slice(), scratch);
                    Matrix4f[] var11 = new Matrix4f[]{var2};
                    WitchRenderer.writeUbo(var9, var1, var3, 0.0f, var3.rim * 0.6f, var11, 1);
                    var10.writeToBuffer(broomUbo.slice(), scratch);
                    GpuTextureView var12 = var5.field_1773.method_22974().method_71650();
                    try (RenderPass var13 = var10.createRenderPass(() -> "Hook witch", var7, OptionalInt.empty(), var8, OptionalDouble.empty());){
                        if (var4) {
                            var13.setPipeline(DEPTH);
                            WitchRenderer.bind(var13, var12, witchView, witchUbo);
                            var13.setVertexBuffer(0, witchVbo);
                            var13.setIndexBuffer(witchIbo, VertexFormat.class_5595.field_27373);
                            var13.drawIndexed(0, 0, witchIndices, 1);
                            WitchRenderer.bind(var13, var12, broomView, broomUbo);
                            var13.setVertexBuffer(0, broomVbo);
                            var13.setIndexBuffer(broomIbo, VertexFormat.class_5595.field_27373);
                            var13.drawIndexed(0, 0, broomIndices, 1);
                        }
                        var13.setPipeline(COLOR);
                        WitchRenderer.bind(var13, var12, witchView, witchUbo);
                        var13.bindTexture("Sampler1", emissiveView, RenderSystem.getSamplerCache().method_75293(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.LINEAR, true));
                        var13.setVertexBuffer(0, witchVbo);
                        var13.setIndexBuffer(witchIbo, VertexFormat.class_5595.field_27373);
                        var13.drawIndexed(0, 0, witchIndices, 1);
                        WitchRenderer.bind(var13, var12, broomView, broomUbo);
                        var13.setVertexBuffer(0, broomVbo);
                        var13.setIndexBuffer(broomIbo, VertexFormat.class_5595.field_27373);
                        var13.drawIndexed(0, 0, broomIndices, 1);
                    }
                }
            }
        }
    }

    private static void bind(RenderPass var0, GpuTextureView var1, GpuTextureView var2, GpuBuffer var3) {
        RenderSystem.bindDefaultUniforms((RenderPass)var0);
        var0.setUniform("WitchData", var3);
        var0.bindTexture("Sampler0", var2, RenderSystem.getSamplerCache().method_75293(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.LINEAR, true));
        var0.bindTexture("Sampler1", blackView, RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
        var0.bindTexture("Sampler2", var1, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
    }

    private static void put4(ByteBuffer var0, int var1, float var2, float var3, float var4, float var5) {
        var0.putFloat(var1, var2);
        var0.putFloat(var1 + 4, var3);
        var0.putFloat(var1 + 8, var4);
        var0.putFloat(var1 + 12, var5);
    }

    private static void writeUbo(Matrix4f var0, Matrix4f var1, Look var2, float var3, float var4, Matrix4f[] var5, int var6) {
        ByteBuffer var7 = scratch;
        var7.clear();
        var0.get(0, var7);
        var1.get(64, var7);
        WitchRenderer.put4(var7, 128, var2.alpha, var3, var4, 0.0f);
        WitchRenderer.put4(var7, 144, var2.block, var2.sky, var2.worldLight, 0.0f);
        WitchRenderer.put4(var7, 160, var2.rimColor[0], var2.rimColor[1], var2.rimColor[2], var4);
        WitchRenderer.put4(var7, 176, var2.dissolveAt, var2.dissolveEdge, var2.dissolveMode, var2.dissolveScale);
        WitchRenderer.put4(var7, 192, var2.dissolveColor[0], var2.dissolveColor[1], var2.dissolveColor[2], var2.dissolveColor[3]);
        WitchRenderer.put4(var7, 208, var2.keyDir[0], var2.keyDir[1], var2.keyDir[2], var2.keyDir[3]);
        WitchRenderer.put4(var7, 224, var2.keyColor[0], var2.keyColor[1], var2.keyColor[2], 0.0f);
        WitchRenderer.put4(var7, 240, var2.fillColor[0], var2.fillColor[1], var2.fillColor[2], var2.fillColor[3]);
        WitchRenderer.put4(var7, 256, var2.ambient[0], var2.ambient[1], var2.ambient[2], 0.0f);
        for (int var8 = 0; var8 < 80; ++var8) {
            Matrix4f var9 = var8 < var6 ? var5[var8] : var5[0];
            var9.get(272 + var8 * 64, var7);
        }
        var7.position(0);
        var7.limit(5392);
    }

    public static void release() {
        for (AutoCloseable var3 : new AutoCloseable[]{witchVbo, witchIbo, broomVbo, broomIbo, witchView, emissiveView, broomView, blackView, witchTex, emissiveTex, broomTex, blackTex, witchUbo, broomUbo}) {
            if (var3 == null) continue;
            try {
                var3.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        broomIbo = null;
        broomVbo = null;
        witchIbo = null;
        witchVbo = null;
        blackView = null;
        broomView = null;
        emissiveView = null;
        witchView = null;
        blackTex = null;
        broomTex = null;
        emissiveTex = null;
        witchTex = null;
        broomUbo = null;
        witchUbo = null;
        scratch = null;
    }

    public static final class Look {
        public float alpha = 1.0f;
        public float emissive;
        public int block;
        public int sky;
        public float worldLight = 0.8f;
        public float rim;
        public float dissolveAt;
        public float dissolveEdge = 0.09f;
        public float dissolveMode;
        public float dissolveScale = 9.0f;
        public final float[] dissolveColor = new float[]{1.0f, 0.52f, 0.18f, 2.4f};
        public final float[] keyDir = new float[]{0.2f, 0.8f, 0.3f, 1.0f};
        public final float[] keyColor = new float[]{0.62f, 0.64f, 0.92f};
        public final float[] fillColor = new float[]{1.0f, 0.52f, 0.2f, 0.3f};
        public final float[] ambient = new float[]{0.17f, 0.14f, 0.22f};
        public final float[] rimColor = new float[]{0.6f, 0.55f, 0.95f};
    }
}

