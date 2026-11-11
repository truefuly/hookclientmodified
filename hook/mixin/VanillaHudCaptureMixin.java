package hook.mixin;

import hook.feature.VanillaHud;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Measures what the boss bar / scoreboard actually draw (their background fills). */
@Mixin(class_332.class)
public abstract class VanillaHudCaptureMixin {
    @Inject(method = "method_25294(IIIII)V", at = @At("HEAD"), remap = false)
    private void hook$captureFill(int x1, int y1, int x2, int y2, int color, CallbackInfo ci) {
        VanillaHud.capture(x1, y1, x2, y2);
    }

    // sprite regions: (pipeline, id, textureWidth, textureHeight, u, v, x, y, width, height [, color])
    @Inject(method = "method_70846(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIIIIIII)V", at = @At("HEAD"), remap = false)
    private void hook$captureRegion(com.mojang.blaze3d.pipeline.RenderPipeline pipeline, net.minecraft.class_2960 id, int tw, int th, int u, int v, int x, int y, int w, int h, CallbackInfo ci) {
        VanillaHud.capture(x, y, x + w, y + h);
    }

    @Inject(method = "method_52708(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIIIIIIII)V", at = @At("HEAD"), remap = false)
    private void hook$captureRegionColor(com.mojang.blaze3d.pipeline.RenderPipeline pipeline, net.minecraft.class_2960 id, int tw, int th, int u, int v, int x, int y, int w, int h, int color, CallbackInfo ci) {
        VanillaHud.capture(x, y, x + w, y + h);
    }
}
