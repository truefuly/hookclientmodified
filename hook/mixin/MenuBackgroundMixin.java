package hook.mixin;

import hook.feature.MenuBackground;
import hook.feature.MenuTheme;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Pixel-art Minecraft landscape instead of the vanilla panorama (blur and darkening stay vanilla). */
@Mixin(class_437.class)
public abstract class MenuBackgroundMixin {
    @Inject(method = "method_57728(Lnet/minecraft/class_332;F)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$panorama(class_332 ctx, float delta, CallbackInfo ci) {
        if (MenuTheme.active()) {
            MenuBackground.render(ctx);
            ci.cancel();
        }
    }
}
