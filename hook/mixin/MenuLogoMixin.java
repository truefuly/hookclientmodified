package hook.mixin;

import hook.feature.MenuTheme;
import net.minecraft.class_332;
import net.minecraft.class_8020;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** HookVisuals logo instead of the Minecraft logo on the title screen. */
@Mixin(class_8020.class)
public abstract class MenuLogoMixin {
    @Inject(method = "method_48210(Lnet/minecraft/class_332;IFI)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$logo(class_332 ctx, int screenWidth, float alpha, int y, CallbackInfo ci) {
        if (MenuTheme.active()) {
            MenuTheme.logo(ctx, screenWidth, alpha);
            ci.cancel();
        }
    }
}
