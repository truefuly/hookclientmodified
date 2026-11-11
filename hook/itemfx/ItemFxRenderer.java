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
 *  com.mojang.blaze3d.platform.DestFactor
 *  com.mojang.blaze3d.platform.SourceFactor
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
 *  net.minecraft.class_11219
 *  net.minecraft.class_11659
 *  net.minecraft.class_11659$class_11660
 *  net.minecraft.class_12137
 *  net.minecraft.class_12247
 *  net.minecraft.class_1921
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_9801
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.lwjgl.BufferUtils
 */
package hook.itemfx;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
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
import hook.itemfx.Drips;
import hook.itemfx.ItemFx;
import hook.itemfx.ItemFxShaders;
import hook.itemfx.mesh.ChromeModel;
import hook.itemfx.mesh.MeshData;
import hook.itemfx.mesh.SpookyModel;
import hook.itemfx.mesh.SpriteModels;
import hook.itemfx.mesh.ToolModel;
import hook.mixin.RenderLayerAccessor;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_10789;
import net.minecraft.class_11219;
import net.minecraft.class_11659;
import net.minecraft.class_12137;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_9801;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;

public final class ItemFxRenderer {
    static final int DESIGN_SPOOKY = 1;
    static final int DESIGN_CHROME = 2;
    static final int DESIGN_TOOL = 3;
    static final int DESIGN_SPOOKY_PX = 4;
    static final int DESIGN_CHROME_PX = 5;
    static final int DESIGN_GHOST = 6;
    static final int CTX_WORLD = 0;
    static final int CTX_HAND = 1;
    static final int CTX_GUI = 2;
    static final int CTX_FLOATING = 3;
    private static final int MAX = 128;
    private static final int CHUNK = 32;
    private static final int INSTANCE_SIZE = 272;
    private static final int DRIPS_SIZE = 784;
    private static final VertexFormat FORMAT = class_290.field_1580;
    private static final RenderPipeline MESH_PIPE = ItemFxRenderer.base("pipeline/fx_item", ItemFxShaders.MESH, FORMAT).withUniform("Lighting", class_10789.field_60031).withSampler("Sampler2").withDepthWrite(true).build();
    private static final RenderPipeline DRIP_PIPE = ItemFxRenderer.base("pipeline/fx_drip", ItemFxShaders.DRIP, class_290.field_1592).withUniform("Lighting", class_10789.field_60031).withUniform("ItemFxDrips", class_10789.field_60031).withSampler("Sampler2").withDepthWrite(true).build();
    private static final RenderPipeline GLOW_PIPE = ItemFxRenderer.base("pipeline/fx_glow", ItemFxShaders.GLOW, class_290.field_1592).withBlend(new BlendFunction(SourceFactor.ONE, DestFactor.ONE, SourceFactor.ZERO, DestFactor.ONE)).withDepthWrite(false).build();
    private static final RenderPipeline SMOKE_PIPE = ItemFxRenderer.base("pipeline/fx_smoke", ItemFxShaders.SMOKE, class_290.field_1592).withBlend(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA).withDepthWrite(false).build();
    private static final BlendFunction ADD = new BlendFunction(SourceFactor.ONE, DestFactor.ONE, SourceFactor.ZERO, DestFactor.ONE);
    private static final RenderPipeline SPRITE_PIPE = ItemFxRenderer.base("pipeline/fx_sprite", ItemFxShaders.SPRITE, FORMAT).withUniform("Lighting", class_10789.field_60031).withSampler("Sampler0").withSampler("Sampler2").withDepthWrite(true).build();
    private static final RenderPipeline GHOST_BACK_PIPE = ItemFxRenderer.base("pipeline/fx_ghost_back", ItemFxShaders.GHOST_BACK, FORMAT).withSampler("Sampler0").withSampler("Sampler2").withBlend(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA).withDepthWrite(true).build();
    private static final RenderPipeline GHOST_FRONT_PIPE = ItemFxRenderer.base("pipeline/fx_ghost_front", ItemFxShaders.GHOST_FRONT, FORMAT).withSampler("Sampler0").withSampler("Sampler2").withBlend(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA).withDepthWrite(true).build();
    private static final RenderPipeline HALO_PIPE = ItemFxRenderer.base("pipeline/fx_halo", ItemFxShaders.HALO, FORMAT).withSampler("Sampler0").withBlend(ADD).withDepthWrite(false).build();
    private static final RenderPipeline SKULL_PIPE = ItemFxRenderer.base("pipeline/fx_skull", ItemFxShaders.SKULL, class_290.field_1592).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA).withDepthWrite(true).build();
    private static final RenderPipeline MARKER_PIPE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"hook", (String)"pipeline/fx_marker")).withVertexShader(ItemFxShaders.MESH).withFragmentShader(ItemFxShaders.MESH).withVertexFormat(FORMAT, VertexFormat.class_5596.field_27382).build();
    private static class_1921 layer;
    private static boolean layerFailed;
    private static final Matrix4f[] POSE;
    private static final Matrix3f[] NRM;
    private static final int[] FX;
    private static final int[] CTX;
    private static final int[] LIGHT;
    private static final float[] ACT;
    private static final boolean[] GLINT;
    private static int pending;
    private static final Command[] COMMANDS;
    private static int nextCommand;
    private static final Map<Integer, GpuMeshes> MESHES;
    private static final Map<Integer, CompletableFuture<Built>> BUILDS;
    private static final Map<Integer, float[]> ANCHORS;
    private static final ExecutorService BUILDER;
    private static GpuTexture atlas;
    private static GpuTextureView atlasView;
    private static GpuBuffer quadVbo;
    private static GpuBuffer quadIbo;
    private static GpuBuffer dripVbo;
    private static GpuBuffer dripIbo;
    private static int dripIndices;
    private static GpuBuffer ubo;
    private static GpuBuffer dripUbo;
    private static int stride;
    private static ByteBuffer data;
    private static ByteBuffer dripData;
    private static long dripFrame;
    private static boolean failed;
    private static final Matrix4f TMP;
    private static final Matrix4f ANIM;
    private static final Matrix4f MV;
    private static final Matrix4f NM;
    private static final Matrix3f TMP3;
    private static final Matrix3f ANIM3;

    private ItemFxRenderer() {
    }

    private static RenderPipeline.Builder base(String var0, class_2960 var1, VertexFormat var2) {
        return RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"hook", (String)var0)).withVertexShader(var1).withFragmentShader(var1).withUniform("Projection", class_10789.field_60031).withUniform("Fog", class_10789.field_60031).withUniform("ItemFx", class_10789.field_60031).withCull(false).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withVertexFormat(var2, VertexFormat.class_5596.field_27379);
    }

    static class_1921 layer() {
        if (layer == null && !layerFailed) {
            try {
                layer = RenderLayerAccessor.hook$create("hook_item_fx", class_12247.method_75927((RenderPipeline)MARKER_PIPE).method_75938());
            }
            catch (Throwable var1) {
                layerFailed = true;
                System.err.println("Hook: item fx layer unavailable: " + String.valueOf(var1));
            }
        }
        return layer;
    }

    public static boolean isMarker(Object var0) {
        return var0 != null && var0 == layer;
    }

    static int meshKey(int var0, int var1, int var2) {
        return switch (var0) {
            case 3 -> 100 + var1 * 10 + var2;
            case 4 -> 201;
            case 5 -> 202;
            case 6 -> 203;
            default -> var0;
        };
    }

    private static Built buildModel(int var0) {
        if (var0 == 1) {
            MeshData var4 = SpookyModel.build();
            return new Built(var4, var4.indices.length, null);
        }
        if (var0 == 2) {
            MeshData var3 = ChromeModel.build();
            return new Built(var3, var3.indices.length, ChromeModel.dripAnchors(var3));
        }
        if (var0 > 200) {
            SpriteModels.Model var2 = SpriteModels.build(var0 == 201 ? 1 : (var0 == 202 ? 2 : 3));
            return new Built(var2.mesh, var2.mainIndices, var0 == 202 ? var2.anchors : null);
        }
        MeshData var1 = ToolModel.build((var0 - 100) / 10, (var0 - 100) % 10);
        return new Built(var1, var1.indices.length, null);
    }

    static float[] anchors(int var0) {
        return ANCHORS.get(var0);
    }

    static boolean ready(int var0) {
        if (failed) {
            return false;
        }
        if (MESHES.containsKey(var0)) {
            return true;
        }
        CompletableFuture var1 = BUILDS.computeIfAbsent(var0, var0x -> CompletableFuture.supplyAsync(() -> ItemFxRenderer.buildModel(var0x), BUILDER));
        if (!var1.isDone()) {
            return false;
        }
        try {
            Built var2 = (Built)var1.join();
            MESHES.put(var0, ItemFxRenderer.upload(var2.mesh(), var2.main(), var0));
            if (var2.anchors() != null) {
                ANCHORS.put(var0, var2.anchors());
            }
            BUILDS.remove(var0);
            return true;
        }
        catch (Throwable var3) {
            System.err.println("Hook: item model " + var0 + " failed: " + String.valueOf(var3));
            failed = true;
            return false;
        }
    }

    static boolean prepare(int var0) {
        if (failed || ItemFxRenderer.layer() == null) {
            return false;
        }
        if (!(ItemFxRenderer.ready(var0) && ItemFxShaders.ready(GLOW_PIPE) && ItemFxShaders.ready(SMOKE_PIPE))) {
            return false;
        }
        if (var0 > 200) {
            boolean var1;
            boolean bl = var1 = ItemFxShaders.ready(HALO_PIPE) && ItemFxRenderer.ensureAtlas();
            return var0 == 203 ? var1 && ItemFxShaders.ready(GHOST_BACK_PIPE) && ItemFxShaders.ready(GHOST_FRONT_PIPE) && ItemFxShaders.ready(SKULL_PIPE) : var1 && ItemFxShaders.ready(SPRITE_PIPE) && (var0 != 202 || ItemFxShaders.ready(DRIP_PIPE));
        }
        return ItemFxShaders.ready(MESH_PIPE) && (var0 != 2 || ItemFxShaders.ready(DRIP_PIPE));
    }

    private static boolean ensureAtlas() {
        if (atlasView != null) {
            return true;
        }
        try {
            BufferedImage var1;
            GpuDevice var0 = RenderSystem.getDevice();
            try (InputStream var2 = ItemFxRenderer.class.getResourceAsStream("/assets/hook/itemfx/atlas.png");){
                var1 = ImageIO.read(var2);
            }
            int var12 = var1.getWidth();
            int var3 = var1.getHeight();
            atlas = var0.createTexture(() -> "Hook item atlas", 5, TextureFormat.RGBA8, var12, var3, 1, 1);
            try (class_1011 var4 = new class_1011(var12, var3, false);){
                for (int var5 = 0; var5 < var3; ++var5) {
                    for (int var6 = 0; var6 < var12; ++var6) {
                        var4.method_61941(var6, var5, var1.getRGB(var6, var5));
                    }
                }
                var0.createCommandEncoder().writeToTexture(atlas, var4);
            }
            atlasView = var0.createTextureView(atlas);
            return true;
        }
        catch (Throwable var11) {
            System.err.println("Hook: item atlas failed: " + String.valueOf(var11));
            failed = true;
            return false;
        }
    }

    private static GpuMeshes upload(MeshData var0, int var1, int var2) {
        GpuDevice var3 = RenderSystem.getDevice();
        int var4 = FORMAT.getVertexSize();
        int var5 = FORMAT.getOffset(VertexFormatElement.POSITION);
        int var6 = FORMAT.getOffset(VertexFormatElement.COLOR);
        int var7 = FORMAT.getOffset(VertexFormatElement.UV0);
        int var8 = FORMAT.getOffset(VertexFormatElement.UV1);
        int var9 = FORMAT.getOffset(VertexFormatElement.UV2);
        int var10 = FORMAT.getOffset(VertexFormatElement.NORMAL);
        ByteBuffer var11 = BufferUtils.createByteBuffer((int)(var4 * var0.vertexCount)).order(ByteOrder.nativeOrder());
        for (int var12 = 0; var12 < var0.vertexCount; ++var12) {
            int var13 = var12 * var4;
            var11.putFloat(var13 + var5, var0.pos[var12 * 3]);
            var11.putFloat(var13 + var5 + 4, var0.pos[var12 * 3 + 1]);
            var11.putFloat(var13 + var5 + 8, var0.pos[var12 * 3 + 2]);
            int var14 = var0.rgba[var12];
            var11.put(var13 + var6, (byte)(var14 >> 16));
            var11.put(var13 + var6 + 1, (byte)(var14 >> 8));
            var11.put(var13 + var6 + 2, (byte)var14);
            var11.put(var13 + var6 + 3, (byte)(var14 >>> 24));
            var11.putFloat(var13 + var7, var0.uv[var12 * 2]);
            var11.putFloat(var13 + var7 + 4, var0.uv[var12 * 2 + 1]);
            var11.putShort(var13 + var8, var0.mat[var12]);
            var11.putShort(var13 + var8 + 2, var0.region[var12]);
            var11.putShort(var13 + var9, var0.p1[var12]);
            var11.putShort(var13 + var9 + 2, var0.p2[var12]);
            var11.put(var13 + var10, (byte)Math.round(var0.nrm[var12 * 3] * 127.0f));
            var11.put(var13 + var10 + 1, (byte)Math.round(var0.nrm[var12 * 3 + 1] * 127.0f));
            var11.put(var13 + var10 + 2, (byte)Math.round(var0.nrm[var12 * 3 + 2] * 127.0f));
        }
        var11.position(0);
        var11.limit(var4 * var0.vertexCount);
        GpuBuffer var18 = var3.createBuffer(() -> "Hook item model " + var2, 32, var11);
        ByteBuffer var19 = BufferUtils.createByteBuffer((int)(var0.indices.length * 4)).order(ByteOrder.nativeOrder());
        for (int var17 : var0.indices) {
            var19.putInt(var17);
        }
        var19.flip();
        GpuBuffer var21 = var3.createBuffer(() -> "Hook item model indices " + var2, 64, var19);
        return new GpuMeshes(var18, var21, var0.indices.length, var1);
    }

    private static void ensureStatic() {
        if (ubo == null) {
            GpuDevice var0 = RenderSystem.getDevice();
            int var1 = Math.max(16, var0.getUniformOffsetAlignment());
            stride = (272 + var1 - 1) / var1 * var1;
            ubo = var0.createBuffer(() -> "Hook item fx instances", 136, (long)stride * 32L);
            dripUbo = var0.createBuffer(() -> "Hook item fx drips", 136, 784L);
            data = BufferUtils.createByteBuffer((int)(stride * 32)).order(ByteOrder.nativeOrder());
            dripData = BufferUtils.createByteBuffer((int)784).order(ByteOrder.nativeOrder());
            ByteBuffer var2 = BufferUtils.createByteBuffer((int)48).order(ByteOrder.nativeOrder());
            float[] var3 = new float[]{-1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f};
            for (int var4 = 0; var4 < 4; ++var4) {
                var2.putFloat(var3[var4 * 2]);
                var2.putFloat(var3[var4 * 2 + 1]);
                var2.putFloat(0.0f);
            }
            var2.flip();
            quadVbo = var0.createBuffer(() -> "Hook fx quad", 32, var2);
            ByteBuffer var15 = BufferUtils.createByteBuffer((int)24).order(ByteOrder.nativeOrder());
            for (int var8 : new int[]{0, 1, 2, 0, 2, 3}) {
                var15.putInt(var8);
            }
            var15.flip();
            quadIbo = var0.createBuffer(() -> "Hook fx quad indices", 64, var15);
            int var16 = 16;
            int var17 = 26;
            ByteBuffer var18 = BufferUtils.createByteBuffer((int)((var16 + 1) * (var17 + 1) * 12)).order(ByteOrder.nativeOrder());
            for (int var19 = 0; var19 <= var17; ++var19) {
                for (int var9 = 0; var9 <= var16; ++var9) {
                    var18.putFloat((float)var9 / (float)var16);
                    float var10 = (float)var19 / (float)var17;
                    var18.putFloat(var10);
                    var18.putFloat(0.0f);
                }
            }
            var18.flip();
            dripVbo = var0.createBuffer(() -> "Hook drip", 32, var18);
            ByteBuffer var20 = BufferUtils.createByteBuffer((int)(var16 * var17 * 6 * 4)).order(ByteOrder.nativeOrder());
            for (int var21 = 0; var21 < var17; ++var21) {
                for (int var22 = 0; var22 < var16; ++var22) {
                    int var11 = var21 * (var16 + 1) + var22;
                    int var12 = var11 + 1;
                    int var13 = var11 + var16 + 1;
                    int var14 = var13 + 1;
                    var20.putInt(var11);
                    var20.putInt(var13);
                    var20.putInt(var12);
                    var20.putInt(var12);
                    var20.putInt(var13);
                    var20.putInt(var14);
                }
            }
            var20.flip();
            dripIndices = var16 * var17 * 6;
            dripIbo = var0.createBuffer(() -> "Hook drip indices", 64, var20);
        }
    }

    static void submit(class_11659 var0, class_4587 var1, int var2, int var3, int var4, boolean var5, float var6) {
        Command var7 = COMMANDS[nextCommand];
        nextCommand = (nextCommand + 1) % COMMANDS.length;
        var7.fx = var2;
        var7.ctx = var3;
        var7.light = var4;
        var7.glint = var5;
        var7.act = var6;
        var0.method_73483(var1, layer, (class_11659.class_11660)var7);
    }

    public static void flush(class_9801 var0) {
        try {
            var0.close();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        int var1 = pending;
        pending = 0;
        if (var1 != 0 && !failed) {
            try {
                ItemFxRenderer.draw(var1);
            }
            catch (Throwable var3) {
                failed = true;
                System.err.println("Hook: custom item rendering disabled after an error: " + String.valueOf(var3));
                var3.printStackTrace();
            }
        }
    }

    private static void draw(int var0) {
        GpuTextureView var4;
        GpuTextureView var3;
        class_310 var1 = class_310.method_1551();
        class_276 var2 = var1.method_1522();
        GpuTextureView gpuTextureView = var3 = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : var2.method_71639();
        var4 = RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : (var2.field_1478 ? var2.method_71640() : null);
        if (var3 != null && ItemFxShaders.ready(GLOW_PIPE) && ItemFxShaders.ready(SMOKE_PIPE)) {
            ItemFxRenderer.ensureStatic();
            CommandEncoder var5 = RenderSystem.getDevice().createCommandEncoder();
            MV.set((Matrix4fc)RenderSystem.getModelViewMatrix());
            boolean var6 = ItemFxShaders.ready(DRIP_PIPE);
            long var7 = ItemFx.frameIndex();
            if (var7 != dripFrame) {
                dripFrame = var7;
                dripData.clear();
                Drips.write(dripData, 0);
                dripData.position(0);
                dripData.limit(784);
                var5.writeToBuffer(dripUbo.slice(), dripData);
            }
            int var9 = Math.round(dripData.getFloat(0));
            GpuTextureView var10 = var1.field_1773.method_22974().method_71650();
            class_12137 var11 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
            class_11219 var12 = RenderSystem.getScissorStateForRenderTypeDraws();
            class_12137 var13 = RenderSystem.getSamplerCache().method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, false);
            class_12137 var14 = RenderSystem.getSamplerCache().method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
            for (int var15 = 0; var15 < var0; var15 = (int)((byte)(var15 + 32))) {
                int var16 = Math.min(32, var0 - var15);
                data.clear();
                for (int var17 = 0; var17 < var16; ++var17) {
                    ItemFxRenderer.writeInstance(var15 + var17, var17 * stride);
                }
                data.position(0);
                data.limit(var16 * stride);
                var5.writeToBuffer(ubo.slice(0L, (long)var16 * (long)stride), data);
                try (RenderPass var25 = var5.createRenderPass(() -> "Hook items", var3, OptionalInt.empty(), var4, OptionalDouble.empty());){
                    if (var12.method_72091()) {
                        var25.enableScissor(var12.method_72092(), var12.method_72093(), var12.method_72094(), var12.method_72095());
                    }
                    boolean var18 = false;
                    for (int var19 = 0; var19 < var16; ++var19) {
                        int var20 = FX[var15 + var19];
                        int var21 = ItemFx.design(var20);
                        GpuMeshes var22 = MESHES.get(ItemFxRenderer.meshKey(var21, ItemFx.type(var20), ItemFx.tier(var20)));
                        if (var22 == null || var21 > 3) continue;
                        if (!var18) {
                            var25.setPipeline(MESH_PIPE);
                            RenderSystem.bindDefaultUniforms((RenderPass)var25);
                            var25.bindTexture("Sampler2", var10, var11);
                            var18 = true;
                        }
                        var25.setUniform("ItemFx", ubo.slice((long)var19 * (long)stride, 272L));
                        var25.setVertexBuffer(0, var22.vbo);
                        var25.setIndexBuffer(var22.ibo, VertexFormat.class_5595.field_27373);
                        var25.drawIndexed(0, 0, var22.count, 1);
                    }
                    var18 = false;
                    for (int var29 = 0; var29 < var16; ++var29) {
                        int var33 = FX[var15 + var29];
                        int var37 = ItemFx.design(var33);
                        GpuMeshes var41 = MESHES.get(ItemFxRenderer.meshKey(var37, ItemFx.type(var33), ItemFx.tier(var33)));
                        if (var41 == null || var37 != 4 && var37 != 5) continue;
                        if (!var18) {
                            var25.setPipeline(SPRITE_PIPE);
                            RenderSystem.bindDefaultUniforms((RenderPass)var25);
                            var25.bindTexture("Sampler0", atlasView, var13);
                            var25.bindTexture("Sampler2", var10, var11);
                            var18 = true;
                        }
                        var25.setUniform("ItemFx", ubo.slice((long)var29 * (long)stride, 272L));
                        var25.setVertexBuffer(0, var41.vbo);
                        var25.setIndexBuffer(var41.ibo, VertexFormat.class_5595.field_27373);
                        var25.drawIndexed(0, 0, var41.main, 1);
                    }
                    if (var6) {
                        var18 = false;
                        for (int var30 = 0; var30 < var16; ++var30) {
                            int var38;
                            int var34 = FX[var15 + var30];
                            if (ItemFx.design(var34) != 2 && ItemFx.design(var34) != 5 || (var38 = (ItemFx.animations(var34) ? var9 : 0) + (ACT[var15 + var30] >= 0.0f && ItemFx.popFx(var34) ? 18 : 0)) <= 0) continue;
                            if (!var18) {
                                var25.setPipeline(DRIP_PIPE);
                                RenderSystem.bindDefaultUniforms((RenderPass)var25);
                                var25.bindTexture("Sampler2", var10, var11);
                                var25.setUniform("ItemFxDrips", dripUbo);
                                var25.setVertexBuffer(0, dripVbo);
                                var25.setIndexBuffer(dripIbo, VertexFormat.class_5595.field_27373);
                                var18 = true;
                            }
                            var25.setUniform("ItemFx", ubo.slice((long)var30 * (long)stride, 272L));
                            var25.drawIndexed(0, 0, dripIndices, var38);
                        }
                    }
                    for (int var31 = 0; var31 < var16; ++var31) {
                        GpuMeshes var39;
                        int var35 = FX[var15 + var31];
                        if (ItemFx.design(var35) != 6 || (var39 = MESHES.get(ItemFxRenderer.meshKey(6, 0, 0))) == null) continue;
                        var25.setPipeline(GHOST_BACK_PIPE);
                        RenderSystem.bindDefaultUniforms((RenderPass)var25);
                        var25.bindTexture("Sampler0", atlasView, var13);
                        var25.bindTexture("Sampler2", var10, var11);
                        var25.setUniform("ItemFx", ubo.slice((long)var31 * (long)stride, 272L));
                        var25.setVertexBuffer(0, var39.vbo);
                        var25.setIndexBuffer(var39.ibo, VertexFormat.class_5595.field_27373);
                        var25.drawIndexed(0, 0, var39.main, 1);
                        if (ItemFx.particles(var35)) {
                            var25.setPipeline(SKULL_PIPE);
                            RenderSystem.bindDefaultUniforms((RenderPass)var25);
                            var25.bindTexture("Sampler0", atlasView, var13);
                            var25.setVertexBuffer(0, quadVbo);
                            var25.setIndexBuffer(quadIbo, VertexFormat.class_5595.field_27373);
                            var25.drawIndexed(0, 0, 6, 5);
                        }
                        var25.setPipeline(GHOST_FRONT_PIPE);
                        RenderSystem.bindDefaultUniforms((RenderPass)var25);
                        var25.bindTexture("Sampler0", atlasView, var13);
                        var25.bindTexture("Sampler2", var10, var11);
                        var25.setVertexBuffer(0, var39.vbo);
                        var25.setIndexBuffer(var39.ibo, VertexFormat.class_5595.field_27373);
                        var25.drawIndexed(0, 0, var39.main, 1);
                    }
                    var18 = false;
                    for (int var32 = 0; var32 < var16; ++var32) {
                        GpuMeshes var42;
                        int var36 = FX[var15 + var32];
                        int var40 = ItemFx.design(var36);
                        if (var40 < 4 || (var42 = MESHES.get(ItemFxRenderer.meshKey(var40, ItemFx.type(var36), ItemFx.tier(var36)))) == null || var42.count <= var42.main) continue;
                        if (!var18) {
                            var25.setPipeline(HALO_PIPE);
                            RenderSystem.bindDefaultUniforms((RenderPass)var25);
                            var25.bindTexture("Sampler0", atlasView, var14);
                            var18 = true;
                        }
                        var25.setUniform("ItemFx", ubo.slice((long)var32 * (long)stride, 272L));
                        var25.setVertexBuffer(0, var42.vbo);
                        var25.setIndexBuffer(var42.ibo, VertexFormat.class_5595.field_27373);
                        var25.drawIndexed(0, var42.main, var42.count - var42.main, 1);
                    }
                    ItemFxRenderer.particles(var25, var15, var16, true);
                    ItemFxRenderer.particles(var25, var15, var16, false);
                    continue;
                }
            }
        }
    }

    private static void particles(RenderPass var0, int var1, int var2, boolean var3) {
        boolean var4 = false;
        for (int var5 = 0; var5 < var2; ++var5) {
            int var7;
            int var6 = FX[var1 + var5];
            int n = var7 = var3 ? ItemFx.glowCount(var6, ACT[var1 + var5]) : ItemFx.smokeCount(var6, ACT[var1 + var5]);
            if (var7 <= 0) continue;
            if (!var4) {
                var0.setPipeline(var3 ? GLOW_PIPE : SMOKE_PIPE);
                RenderSystem.bindDefaultUniforms((RenderPass)var0);
                var0.setVertexBuffer(0, quadVbo);
                var0.setIndexBuffer(quadIbo, VertexFormat.class_5595.field_27373);
                var4 = true;
            }
            var0.setUniform("ItemFx", ubo.slice((long)var5 * (long)stride, 272L));
            var0.drawIndexed(0, 0, 6, var7);
        }
    }

    private static void writeInstance(int var0, int var1) {
        int var2 = FX[var0];
        int var3 = ItemFx.design(var2);
        int var4 = CTX[var0];
        float var5 = ACT[var0];
        float var6 = ItemFx.time();
        ItemFx.anim(ANIM, var2, var4, var5, var6);
        TMP.set((Matrix4fc)POSE[var0]).mul((Matrix4fc)ANIM);
        TMP3.set((Matrix3fc)NRM[var0]).mul((Matrix3fc)ANIM.get3x3(ANIM3));
        NM.identity().set((Matrix3fc)TMP3);
        ByteBuffer var7 = data;
        MV.get(var1, var7);
        TMP.get(var1 + 64, var7);
        NM.get(var1 + 128, var7);
        int var8 = LIGHT[var0];
        boolean var9 = var4 == 2 || var4 == 3;
        ItemFxRenderer.put4(var7, var1 + 192, var6, var5, var3, var4);
        ItemFxRenderer.put4(var7, var1 + 208, (var8 & 0xFFFF) >> 4, var8 >>> 16 >> 4, var9 ? 0.0f : 1.0f, GLINT[var0] ? 1.0f : 0.0f);
        float var10 = (float)Math.sqrt(TMP.m00() * TMP.m00() + TMP.m01() * TMP.m01() + TMP.m02() * TMP.m02());
        ItemFxRenderer.put4(var7, var1 + 224, 0.0f, var4 == 2 ? -1.0f : 1.0f, 0.0f, var10);
        ItemFxRenderer.put4(var7, var1 + 240, ItemFx.tier(var2), ItemFx.shaderStyle(var2), ItemFx.type(var2), ItemFx.animations(var2) ? 1.0f : 0.0f);
        ItemFxRenderer.put4(var7, var1 + 256, ItemFx.particles(var2) ? 1.0f : 0.0f, ItemFx.intensity(var2), var3 == 3 ? 0.17f * (float)ItemFx.type(var2) + 0.05f * (float)ItemFx.tier(var2) : 0.37f, ItemFx.tint(var2));
    }

    private static void put4(ByteBuffer var0, int var1, float var2, float var3, float var4, float var5) {
        var0.putFloat(var1, var2);
        var0.putFloat(var1 + 4, var3);
        var0.putFloat(var1 + 8, var4);
        var0.putFloat(var1 + 12, var5);
    }

    public static void releaseModels() {
        for (GpuMeshes var1 : MESHES.values()) {
            try {
                var1.close();
            }
            catch (Throwable throwable) {}
        }
        MESHES.clear();
        pending = 0;
        if (atlasView != null) {
            atlasView.close();
            atlas.close();
            atlasView = null;
            atlas = null;
        }
    }

    static {
        POSE = new Matrix4f[128];
        NRM = new Matrix3f[128];
        FX = new int[128];
        CTX = new int[128];
        LIGHT = new int[128];
        ACT = new float[128];
        GLINT = new boolean[128];
        COMMANDS = new Command[512];
        MESHES = new HashMap<Integer, GpuMeshes>();
        BUILDS = new HashMap<Integer, CompletableFuture<Built>>();
        ANCHORS = new HashMap<Integer, float[]>();
        BUILDER = Executors.newSingleThreadExecutor(var0x -> {
            Thread var1x = new Thread(var0x, "Hook item models");
            var1x.setDaemon(true);
            var1x.setPriority(1);
            return var1x;
        });
        dripFrame = -1L;
        TMP = new Matrix4f();
        ANIM = new Matrix4f();
        MV = new Matrix4f();
        NM = new Matrix4f();
        TMP3 = new Matrix3f();
        ANIM3 = new Matrix3f();
        for (int var0 = 0; var0 < 128; ++var0) {
            ItemFxRenderer.POSE[var0] = new Matrix4f();
            ItemFxRenderer.NRM[var0] = new Matrix3f();
        }
        for (int var1 = 0; var1 < COMMANDS.length; ++var1) {
            ItemFxRenderer.COMMANDS[var1] = new Command();
        }
    }

    private record Built(MeshData mesh, int main, float[] anchors) {
    }

    private record GpuMeshes(GpuBuffer vbo, GpuBuffer ibo, int count, int main) implements AutoCloseable
    {
        @Override
        public void close() {
            this.vbo.close();
            this.ibo.close();
        }
    }

    private static final class Command
    implements class_11659.class_11660 {
        int fx;
        int ctx;
        int light;
        boolean glint;
        float act;

        private Command() {
        }

        public void render(class_4587.class_4665 var1, class_4588 var2) {
            if (pending < 128) {
                int var3 = pending++;
                POSE[var3].set((Matrix4fc)var1.method_23761());
                NRM[var3].set((Matrix3fc)var1.method_23762());
                ItemFxRenderer.FX[var3] = this.fx;
                ItemFxRenderer.CTX[var3] = this.ctx;
                ItemFxRenderer.LIGHT[var3] = this.light;
                ItemFxRenderer.GLINT[var3] = this.glint;
                ItemFxRenderer.ACT[var3] = this.act;
            }
            for (int var4 = 0; var4 < 4; ++var4) {
                var2.method_23919(0.0f, 0.0f, 0.0f, 0, 0.0f, 0.0f, 0, 0, 0.0f, 1.0f, 0.0f);
            }
        }
    }
}

