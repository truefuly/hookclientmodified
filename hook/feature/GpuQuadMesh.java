/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.systems.RenderSystem$class_5590
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_276
 *  net.minecraft.class_287
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_9799
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 */
package hook.feature;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import hook.mixin.RenderPipelinesAccessor;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

@Environment(value=EnvType.CLIENT)
final class GpuQuadMesh {
    static final RenderPipeline GLOW = GpuQuadMesh.pipeline("sky_glow", BlendFunction.LIGHTNING);
    static final RenderPipeline PAINT = GpuQuadMesh.pipeline("sky_paint", null);
    private static final int VERTEX_SIZE = class_290.field_1576.getVertexSize();
    private final String label;
    private final RenderPipeline pipeline;
    private final int maxQuads;
    private class_9799 bytes;
    private GpuBuffer buffer;
    private int indexCount;
    private boolean built;

    GpuQuadMesh(String var1, RenderPipeline var2, int var3) {
        this.label = var1;
        this.pipeline = var2;
        this.maxQuads = var3;
    }

    private static RenderPipeline pipeline(String var0, BlendFunction var1) {
        RenderPipeline.Snippet[] var10000 = new RenderPipeline.Snippet[]{RenderPipelinesAccessor.hook$getTransformsSnippet()};
        RenderPipeline.Builder var2 = RenderPipeline.builder((RenderPipeline.Snippet[])var10000).withLocation(class_2960.method_60655((String)"hook", (String)("pipeline/" + var0))).withVertexShader("core/position_color").withFragmentShader("core/position_color").withDepthWrite(false).withCull(false).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_27382);
        if (var1 == null) {
            return var2.withoutBlend().build();
        }
        return var2.withBlend(var1).build();
    }

    class_287 begin() {
        if (this.bytes == null) {
            this.bytes = new class_9799(this.maxQuads * 4 * VERTEX_SIZE);
        }
        return new class_287(this.bytes, VertexFormat.class_5596.field_27382, class_290.field_1576);
    }

    void upload(class_287 builder) {
        this.built = true;
        class_9801 mesh = builder.method_60794();
        if (mesh == null) {
            this.indexCount = 0;
            return;
        }
        try (class_9801 class_98012 = mesh;){
            if (this.buffer == null) {
                this.buffer = RenderSystem.getDevice().createBuffer(() -> this.label, 40, (long)this.maxQuads * 4L * (long)VERTEX_SIZE);
            }
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer.slice(0L, (long)mesh.method_60818().remaining()), mesh.method_60818());
            this.indexCount = mesh.method_60822().comp_751();
        }
    }

    boolean isBuilt() {
        return this.built;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void draw(class_4587 matrices, float alpha) {
        if (this.indexCount != 0 && !(alpha <= 0.002f)) {
            RenderSystem.class_5590 indices = RenderSystem.getSequentialBuffer((VertexFormat.class_5596)VertexFormat.class_5596.field_27382);
            Matrix4fStack modelView = RenderSystem.getModelViewStack();
            modelView.pushMatrix();
            try {
                modelView.mul((Matrix4fc)matrices.method_23760().method_23761());
                GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)modelView, (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, alpha), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
                class_276 framebuffer = class_310.method_1551().method_1522();
                try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> this.label, framebuffer.method_71639(), OptionalInt.empty(), framebuffer.method_71640(), OptionalDouble.empty());){
                    pass.setPipeline(this.pipeline);
                    RenderSystem.bindDefaultUniforms((RenderPass)pass);
                    pass.setUniform("DynamicTransforms", transforms);
                    pass.setVertexBuffer(0, this.buffer);
                    pass.setIndexBuffer(indices.method_68274(this.indexCount), indices.method_31924());
                    pass.drawIndexed(0, 0, this.indexCount, 1);
                }
            }
            finally {
                modelView.popMatrix();
            }
        }
    }

    void close() {
        if (this.buffer != null) {
            this.buffer.close();
        }
        if (this.bytes != null) {
            this.bytes.close();
        }
        this.buffer = null;
        this.bytes = null;
        this.indexCount = 0;
        this.built = false;
    }

    static void vertex(class_287 var0, float var1, float var2, float var3, int var4) {
        var0.method_22912(var1, var2, var3).method_39415(var4);
    }
}

