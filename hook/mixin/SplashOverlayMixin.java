package hook.mixin;

import hook.feature.ThemedSplash;
import net.minecraft.class_156;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4011;
import net.minecraft.class_425;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the drawing of the resource-loading overlay (game start, resource pack changes, F3+T) with the
 * themed HookVisuals screen. Only rendering is replaced: finishing the reload stays in vanilla's tick(), and the
 * fade timing below mirrors vanilla's render() so the overlay closes exactly as before.
 */
@Mixin(class_425.class)
public abstract class SplashOverlayMixin {
    @Shadow(remap = false) @Final private class_310 field_18217;
    @Shadow(remap = false) @Final private class_4011 field_17767;
    @Shadow(remap = false) @Final private boolean field_18219;
    @Shadow(remap = false) private float field_17770;
    @Shadow(remap = false) private long field_17771;
    @Shadow(remap = false) private long field_18220;

    @Inject(method = "method_25394(Lnet/minecraft/class_332;IIF)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$render(class_332 ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!ThemedSplash.active()) return;
        class_310 client = this.field_18217;
        long now = class_156.method_658();
        if (this.field_18219 && this.field_18220 == -1L) this.field_18220 = now;
        float fadeOut = this.field_17771 > -1L ? (now - this.field_17771) / 1000.0f : -1.0f;
        float fadeIn = this.field_18220 > -1L ? (now - this.field_18220) / 500.0f : -1.0f;
        // on a server / in a world: keep the world (and any open screen) visible behind a light tint
        boolean seeThrough = this.field_18219 && client.field_1687 != null;
        float alpha;
        if (seeThrough && fadeOut < 1.0f) {
            if (client.field_1755 != null) client.field_1755.method_47413(ctx, mouseX, mouseY, delta);
            else client.field_1705.method_72738();
            alpha = Math.max(0.0f, Math.min(1.0f, fadeIn));
            ctx.method_71048();
        } else if (fadeOut >= 1.0f) {
            if (client.field_1755 != null) client.field_1755.method_47413(ctx, 0, 0, delta);
            else client.field_1705.method_72738();
            alpha = 1.0f - Math.max(0.0f, Math.min(1.0f, fadeOut - 1.0f));
            ctx.method_71048();
        } else if (this.field_18219) {
            if (client.field_1755 != null && fadeIn < 1.0f) client.field_1755.method_47413(ctx, mouseX, mouseY, delta);
            else client.field_1705.method_72738();
            alpha = Math.max(0.15f, Math.min(1.0f, fadeIn));
            ctx.method_71048();
        } else {
            alpha = 1.0f;
        }
        this.field_17770 = Math.max(0.0f, Math.min(1.0f, this.field_17770 * 0.95f + this.field_17767.method_18229() * 0.050000012f));
        float barAlpha = fadeOut < 1.0f ? 1.0f - Math.max(0.0f, Math.min(1.0f, fadeOut)) : 0.0f;
        if (!ThemedSplash.render(ctx, alpha, this.field_17770, barAlpha, seeThrough)) {
            return; // drawing failed: let vanilla draw this frame (and all later ones)
        }
        if (fadeOut >= 2.0f) client.method_18502(null);
        ci.cancel();
    }
}
