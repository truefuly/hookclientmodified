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
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 *  org.lwjgl.BufferUtils
 */
package hook.halloween;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import hook.halloween.HalloweenAtmosphere;
import hook.halloween.HalloweenShaders;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

public final class HalloweenSky {
    private static final int UBO_SIZE = 144;
    public static final RenderPipeline PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"hook", (String)"pipeline/hw_sky")).withVertexShader(HalloweenShaders.SKY).withFragmentShader(HalloweenShaders.SKY).withUniform("SkyData", class_10789.field_60031).withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withCull(false).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).build();
    private static final Matrix4f VIEW = new Matrix4f();
    private static final Matrix4f PROJ = new Matrix4f();
    private static boolean haveMatrices;
    private static GpuBuffer ubo;
    private static ByteBuffer data;

    private HalloweenSky() {
    }

    public static void capture(Matrix4f var0, Matrix4f var1) {
        VIEW.set((Matrix4fc)var0);
        PROJ.set((Matrix4fc)var1);
        haveMatrices = true;
    }

    public static Matrix4f projection() {
        return PROJ;
    }

    public static Matrix4f view() {
        return VIEW;
    }

    public static boolean hasMatrices() {
        return haveMatrices;
    }

    public static void draw() {
        float var0 = HalloweenAtmosphere.strength() * HalloweenAtmosphere.nightness();
        class_310 var1 = class_310.method_1551();
        if (!(var0 <= 0.004f) && haveMatrices && var1.field_1687 != null && HalloweenShaders.ready(PIPELINE)) {
            try {
                if (ubo == null) {
                    ubo = RenderSystem.getDevice().createBuffer(() -> "Hook Halloween sky", 136, 144L);
                    data = BufferUtils.createByteBuffer((int)144);
                }
                Matrix4f var2 = new Matrix4f((Matrix4fc)PROJ).mul((Matrix4fc)VIEW).invert();
                Vector4f var3 = HalloweenAtmosphere.moonDir();
                float var4 = 1.0f - var1.field_1687.method_8430(1.0f);
                float var5 = ((float)(var1.field_1687.method_75260() % 240000L) + var1.method_61966().method_60637(false)) / 20.0f;
                data.clear();
                var2.get(0, data);
                HalloweenSky.put4(data, 64, var3.x, var3.y, var3.z, var3.w * var4);
                HalloweenSky.put4(data, 80, var0 * 0.94f, var5, 0.0f, 0.0f);
                HalloweenSky.put4(data, 96, 0.03f, 0.015f, 0.05f, 1.0f);
                HalloweenSky.put4(data, 112, 0.135f, 0.066f, 0.165f, 1.0f);
                HalloweenSky.put4(data, 128, 0.32f, 0.11f, 0.035f, 0.55f);
                data.position(0);
                data.limit(144);
                CommandEncoder var6 = RenderSystem.getDevice().createCommandEncoder();
                var6.writeToBuffer(ubo.slice(), data);
                GpuTextureView var7 = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : var1.method_1522().method_71639();
                try (RenderPass var8 = var6.createRenderPass(() -> "Hook Halloween sky", var7, OptionalInt.empty());){
                    var8.setPipeline(PIPELINE);
                    var8.setUniform("SkyData", ubo);
                    var8.draw(0, 3);
                }
            }
            catch (Throwable var13) {
                System.err.println("Hook: Halloween sky skipped: " + String.valueOf(var13));
            }
        }
    }

    static void put4(ByteBuffer var0, int var1, float var2, float var3, float var4, float var5) {
        var0.putFloat(var1, var2);
        var0.putFloat(var1 + 4, var3);
        var0.putFloat(var1 + 8, var4);
        var0.putFloat(var1 + 12, var5);
    }

    public static void release() {
        if (ubo != null) {
            ubo.close();
            ubo = null;
        }
        data = null;
    }
}

