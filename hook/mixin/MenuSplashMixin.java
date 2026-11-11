package hook.mixin;

import hook.feature.MenuTheme;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_8519;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The yellow splash text doesn't fit the themed title screen. */
@Mixin(class_8519.class)
public abstract class MenuSplashMixin {
    @Inject(method = "method_51453(Lnet/minecraft/class_332;ILnet/minecraft/class_327;F)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$splash(class_332 ctx, int screenWidth, class_327 font, float alpha, CallbackInfo ci) {
        if (MenuTheme.active()) ci.cancel();
    }
}
