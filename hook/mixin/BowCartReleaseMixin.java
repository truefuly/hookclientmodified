package hook.mixin;

import hook.feature.BowCartModule;
import net.minecraft.class_1657;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bow Cart: plan right before the bow is released, place right after the release packet. */
@Mixin(value = class_636.class, remap = false)
public abstract class BowCartReleaseMixin {
    @Inject(method = "method_2897(Lnet/minecraft/class_1657;)V", at = @At("HEAD"), remap = false, require = 0)
    private void hook$beforeRelease(class_1657 player, CallbackInfo ci) {
        BowCartModule.beforeRelease();
    }

    @Inject(method = "method_2897(Lnet/minecraft/class_1657;)V", at = @At("RETURN"), remap = false, require = 0)
    private void hook$afterRelease(class_1657 player, CallbackInfo ci) {
        BowCartModule.afterRelease();
    }
}
