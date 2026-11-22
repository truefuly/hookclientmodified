package hook.mixin;

import hook.feature.VanillaHud;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5481;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Measures what the wrapped vanilla HUD parts actually draw (fills, sprites, text). */
@Mixin(class_332.class)
public abstract class VanillaHudCaptureMixin {
    @Inject(method = "method_25294(IIIII)V", at = @At("HEAD"), remap = false)
    private void hook$captureFill(int x1, int y1, int x2, int y2, int color, CallbackInfo ci) {
        VanillaHud.capture((class_332) (Object) this, x1, y1, x2, y2);
    }

    // sprite regions: (pipeline, id, textureWidth, textureHeight, u, v, x, y, width, height [, color])
    @Inject(method = "method_70846(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIIIIIII)V", at = @At("HEAD"), remap = false)
    private void hook$captureRegion(com.mojang.blaze3d.pipeline.RenderPipeline pipeline, net.minecraft.class_2960 id, int tw, int th, int u, int v, int x, int y, int w, int h, CallbackInfo ci) {
        VanillaHud.capture((class_332) (Object) this, x, y, x + w, y + h);
    }

    @Inject(method = "method_52708(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIIIIIIII)V", at = @At("HEAD"), remap = false)
    private void hook$captureRegionColor(com.mojang.blaze3d.pipeline.RenderPipeline pipeline, net.minecraft.class_2960 id, int tw, int th, int u, int v, int x, int y, int w, int h, int color, CallbackInfo ci) {
        VanillaHud.capture((class_332) (Object) this, x, y, x + w, y + h);
    }

    // all text ends up here (title, action bar, item name, XP level, effect timers...)
    @Inject(method = "method_51430(Lnet/minecraft/class_327;Lnet/minecraft/class_5481;IIIZ)V", at = @At("HEAD"), remap = false)
    private void hook$captureText(class_327 font, class_5481 text, int x, int y, int color, boolean shadow, CallbackInfo ci) {
        if (text == null || (color >>> 24) == 0) return;
        VanillaHud.capture((class_332) (Object) this, x, y, x + font.method_30880(text), y + 9);
    }
}
