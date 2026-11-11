/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  net.minecraft.class_10799
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package hook.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.class_10799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_10799.class})
public interface RenderPipelinesAccessor {
    @Invoker(value="register")
    public static RenderPipeline hook$register(RenderPipeline var0) {
        throw new AssertionError();
    }

    @Accessor(value="TRANSFORMS_AND_PROJECTION_SNIPPET", remap=true)
    public static RenderPipeline.Snippet hook$getTransformsSnippet() {
        throw new AssertionError();
    }

    @Accessor(value="RENDERTYPE_OUTLINE_SNIPPET", remap=true)
    public static RenderPipeline.Snippet hook$getOutlineSnippet() {
        throw new AssertionError();
    }
}

