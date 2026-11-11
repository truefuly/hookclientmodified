package hook.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import hook.feature.MenuTheme;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Swaps the vanilla widget sprites (buttons, sliders, fields, ...) for the themed shapes in menus. */
@Mixin(class_332.class)
public abstract class MenuSpriteMixin {
    @Inject(method = "method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$sprite(RenderPipeline pipeline, class_2960 id, int x, int y, int w, int h, CallbackInfo ci) {
        hook.feature.VanillaHud.capture(x, y, x + w, y + h);
        if (MenuTheme.active() && MenuTheme.sprite((class_332) (Object) this, id, x, y, w, h, 1.0f)) ci.cancel();
    }

    @Inject(method = "method_71501(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIIIF)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$spriteAlpha(RenderPipeline pipeline, class_2960 id, int x, int y, int w, int h, float alpha, CallbackInfo ci) {
        hook.feature.VanillaHud.capture(x, y, x + w, y + h);
        if (MenuTheme.active() && MenuTheme.sprite((class_332) (Object) this, id, x, y, w, h, alpha)) ci.cancel();
    }

    @Inject(method = "method_52707(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIIII)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$spriteColor(RenderPipeline pipeline, class_2960 id, int x, int y, int w, int h, int color, CallbackInfo ci) {
        hook.feature.VanillaHud.capture(x, y, x + w, y + h);
        if (MenuTheme.active() && MenuTheme.sprite((class_332) (Object) this, id, x, y, w, h, (color >>> 24) / 255.0f)) ci.cancel();
    }
}
