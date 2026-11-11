/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10789
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_6367
 *  org.lwjgl.BufferUtils
 */
package hook.feature;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_6367;
import org.lwjgl.BufferUtils;

@Environment(value=EnvType.CLIENT)
final class HandAuraFramebuffer {
    private static final RenderPipeline CONSTANT_0063 = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"hook", (String)"pipeline/aura_spread")).withVertexShader("core/screenquad").withFragmentShader(class_2960.method_60655((String)"hook", (String)"core/aura_spread")).withSampler("InSampler").withUniform("AuraSpread", class_10789.field_60031).withoutBlend().withDepthWrite(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).build();
    private static final int CONSTANT_0128 = 16;
    private static final float CONSTANT_0441 = 6.0f;
    private final String name;
    private final Supplier<String> passLabel;
    private class_6367 across;
    private class_6367 down;
    private GpuBuffer acrossConfig;
    private GpuBuffer downConfig;
    private GpuBufferSlice acrossSlice;
    private GpuBufferSlice downSlice;
    private ByteBuffer configData;

    HandAuraFramebuffer(String var1) {
        this.name = var1;
        this.passLabel = () -> var1 + " spread";
    }

    static boolean compiled(GpuDevice var0) {
        return var0.precompilePipeline(CONSTANT_0063).isValid();
    }

    GpuTextureView spread(CommandEncoder var1, GpuTextureView var2, int var3, int var4, float var5) {
        int var6 = Math.max(1, (var3 + 1) / 2);
        int var7 = Math.max(1, (var4 + 1) / 2);
        if (this.across == null) {
            this.allocate(var6, var7);
        } else if (this.across.field_1482 != var6 || this.across.field_1481 != var7) {
            this.across.method_1234(var6, var7);
            this.down.method_1234(var6, var7);
        }
        float var8 = Math.max(0.75f, var5 / 6.0f);
        this.run(var1, var2, this.across, this.acrossSlice, var8 / (float)var3, 0.0f);
        this.run(var1, this.across.method_71639(), this.down, this.downSlice, 0.0f, var8 / (float)var4);
        return this.down.method_71639();
    }

    private void run(CommandEncoder var1, GpuTextureView var2, class_6367 var3, GpuBufferSlice var4, float var5, float var6) {
        this.configData.putFloat(0, var5);
        this.configData.putFloat(4, var6);
        var1.writeToBuffer(var4, this.configData);
        try (RenderPass var7 = var1.createRenderPass(this.passLabel, var3.method_71639(), OptionalInt.empty());){
            var7.setPipeline(CONSTANT_0063);
            var7.setUniform("AuraSpread", var4);
            var7.bindTexture("InSampler", var2, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
            var7.draw(0, 3);
        }
    }

    private void allocate(int var1, int var2) {
        GpuDevice var3 = RenderSystem.getDevice();
        this.across = new class_6367(this.name + " across", var1, var2, false);
        this.down = new class_6367(this.name + " down", var1, var2, false);
        this.acrossConfig = var3.createBuffer(() -> this.name + " across config", 136, 16L);
        this.downConfig = var3.createBuffer(() -> this.name + " down config", 136, 16L);
        this.acrossSlice = this.acrossConfig.slice();
        this.downSlice = this.downConfig.slice();
        this.configData = BufferUtils.createByteBuffer((int)16);
    }

    void release() {
        if (this.across != null) {
            this.across.method_1238();
            this.across = null;
        }
        if (this.down != null) {
            this.down.method_1238();
            this.down = null;
        }
        if (this.acrossConfig != null) {
            this.acrossConfig.close();
            this.acrossConfig = null;
        }
        if (this.downConfig != null) {
            this.downConfig.close();
            this.downConfig = null;
        }
        this.acrossSlice = null;
        this.downSlice = null;
        this.configData = null;
    }
}

