package hook.mixin;

import hook.feature.MenuBackground;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3928;
import net.minecraft.class_412;
import net.minecraft.class_424;
import net.minecraft.class_435;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lobby theme background: replaces the panorama in the lobby menus, and the background of the
 * loading / connecting screens (also while the world is already being created).
 * With the Standard theme MenuBackground draws nothing and vanilla stays.
 */
@Mixin(class_437.class)
public abstract class MenuBackgroundMixin {
    @Inject(method = "method_57728(Lnet/minecraft/class_332;F)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$panorama(class_332 ctx, float delta, CallbackInfo ci) {
        if (MenuBackground.render(ctx)) ci.cancel();
    }

    @Inject(method = "method_25420(Lnet/minecraft/class_332;IIF)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$loadingBackground(class_332 ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Object self = this;
        boolean loading = self instanceof class_3928 || self instanceof class_412 || self instanceof class_435 || self instanceof class_424;
        if (loading && class_310.method_1551().field_1687 != null && MenuBackground.render(ctx)) {
            ci.cancel(); // world exists already: vanilla would draw the in-game darkening instead of a backdrop
        }
    }
}
