/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.BlendFunction
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
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_6367
 *  org.lwjgl.BufferUtils
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package hook.feature;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import hook.feature.HandAuraFramebuffer;
import hook.utils.ext.ShaderOverlay;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_6367;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(value=EnvType.CLIENT)
final class HandAuraHelper {
    private static final Logger CONSTANT_0292 = LoggerFactory.getLogger((String)"HookVisuals");
    private static final RenderPipeline CONSTANT_0368 = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"hook", (String)"pipeline/hand_aura")).withVertexShader("core/screenquad").withFragmentShader(class_2960.method_60655((String)"hook", (String)"core/hand_aura")).withSampler("CaptureSampler").withSampler("HaloSampler").withUniform("HandAuraConfig", class_10789.field_60031).withBlend(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA).withColorWrite(true, false).withDepthWrite(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).build();
    private static final Supplier<String> CONSTANT_0313 = () -> "Hook hand aura";
    private static final Supplier<String> CONSTANT_0274 = () -> "Hook hand aura config";
    private static final int CONSTANT_0459 = 48;
    private static class_6367 hand;
    private static HandAuraFramebuffer cachedValue0023;
    private static GpuBuffer cachedValue0013;
    private static GpuBufferSlice cachedValue0018;
    private static ByteBuffer cachedValue0002;
    private static boolean stateFlag0001;
    private static boolean stateFlag0014;

    private HandAuraHelper() {
    }

    static boolean begin() {
        if (!stateFlag0001 && RenderSystem.outputColorTextureOverride == null && RenderSystem.outputDepthTextureOverride == null) {
            class_310 var0 = class_310.method_1551();
            class_276 var1 = var0.method_1522();
            if (var0.field_1687 != null && !var0.field_1773.method_35765() && var1.method_30277() != null && var1.field_1482 > 0 && var1.field_1481 > 0 && !ShaderOverlay.shaderPackInUse()) {
                GpuDevice var2 = RenderSystem.getDevice();
                if (!HandAuraHelper.shaderReady(var2)) {
                    return false;
                }
                if (hand == null) {
                    HandAuraHelper.allocate(var1.field_1482, var1.field_1481);
                } else if (HandAuraHelper.hand.field_1482 != var1.field_1482 || HandAuraHelper.hand.field_1481 != var1.field_1481) {
                    hand.method_1234(var1.field_1482, var1.field_1481);
                }
                var2.createCommandEncoder().clearColorAndDepthTextures(hand.method_30277(), 0, hand.method_30278(), 1.0);
                RenderSystem.outputColorTextureOverride = hand.method_71639();
                RenderSystem.outputDepthTextureOverride = hand.method_71640();
                stateFlag0001 = true;
                return true;
            }
            return false;
        }
        return false;
    }

    static boolean end() {
        if (!stateFlag0001) {
            return false;
        }
        stateFlag0001 = false;
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
        return true;
    }

    static void compose(int var0, float var1, float var2, float var3, float var4, int var5, float var6, float var7) {
        class_276 var8 = class_310.method_1551().method_1522();
        CommandEncoder var9 = RenderSystem.getDevice().createCommandEncoder();
        GpuTextureView var10 = var1 > 0.0f ? cachedValue0023.spread(var9, hand.method_71639(), HandAuraHelper.hand.field_1482, HandAuraHelper.hand.field_1481, var2) : hand.method_71639();
        HandAuraHelper.putColor(0, var0, var1);
        HandAuraHelper.putColor(16, var5, var6);
        cachedValue0002.putFloat(32, var3);
        cachedValue0002.putFloat(36, var4);
        cachedValue0002.putFloat(40, var7);
        cachedValue0002.putFloat(44, 0.0f);
        var9.writeToBuffer(cachedValue0018, cachedValue0002);
        try (RenderPass var11 = var9.createRenderPass(CONSTANT_0313, var8.method_71639(), OptionalInt.empty());){
            var11.setPipeline(CONSTANT_0368);
            var11.setUniform("HandAuraConfig", cachedValue0018);
            var11.bindTexture("CaptureSampler", hand.method_71639(), RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
            var11.bindTexture("HaloSampler", var10, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
            var11.draw(0, 3);
        }
    }

    private static void putColor(int var0, int var1, float var2) {
        cachedValue0002.putFloat(var0, (float)(var1 >> 16 & 0xFF) / 255.0f);
        cachedValue0002.putFloat(var0 + 4, (float)(var1 >> 8 & 0xFF) / 255.0f);
        cachedValue0002.putFloat(var0 + 8, (float)(var1 & 0xFF) / 255.0f);
        cachedValue0002.putFloat(var0 + 12, var2);
    }

    private static boolean shaderReady(GpuDevice var0) {
        if (var0.precompilePipeline(CONSTANT_0368).isValid() && HandAuraFramebuffer.compiled(var0)) {
            stateFlag0014 = false;
            return true;
        }
        if (!stateFlag0014) {
            stateFlag0014 = true;
            CONSTANT_0292.warn("Hand Aura shaders did not compile; the hand is drawn as usual");
        }
        return false;
    }

    private static void allocate(int var0, int var1) {
        hand = new class_6367("Hook hand aura", var0, var1, true);
        cachedValue0023 = new HandAuraFramebuffer("Hook hand aura");
        cachedValue0013 = RenderSystem.getDevice().createBuffer(CONSTANT_0274, 136, 48L);
        cachedValue0018 = cachedValue0013.slice();
        cachedValue0002 = BufferUtils.createByteBuffer((int)48);
    }

    static void release() {
        if (stateFlag0001) {
            HandAuraHelper.end();
        }
        if (hand != null) {
            hand.method_1238();
            hand = null;
        }
        if (cachedValue0023 != null) {
            cachedValue0023.release();
            cachedValue0023 = null;
        }
        if (cachedValue0013 != null) {
            cachedValue0013.close();
            cachedValue0013 = null;
        }
        cachedValue0018 = null;
        cachedValue0002 = null;
    }
}

