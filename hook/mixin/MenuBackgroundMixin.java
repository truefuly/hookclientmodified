package hook.mixin;

import hook.feature.MenuTheme;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Themed animated background instead of the panorama, without the vanilla blur and darkening. */
@Mixin(class_437.class)
public abstract class MenuBackgroundMixin {
    @Inject(method = "method_57728(Lnet/minecraft/class_332;F)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$panorama(class_332 ctx, float delta, CallbackInfo ci) {
        if (MenuTheme.active()) {
            MenuTheme.background(ctx);
            ci.cancel();
        }
    }

    @Inject(method = "method_57734(Lnet/minecraft/class_332;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$blur(class_332 ctx, CallbackInfo ci) {
        if (MenuTheme.active()) ci.cancel();
    }

    @Inject(method = "method_57735(Lnet/minecraft/class_332;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$darkening(class_332 ctx, CallbackInfo ci) {
        if (MenuTheme.active()) ci.cancel();
    }
}
