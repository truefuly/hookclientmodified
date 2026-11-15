package hook.mixin;

import hook.feature.NoRenderModule;
import net.minecraft.class_1297;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** No Render: hidden dropped items / XP orbs are not ticked client side at all (no physics, no bobbing, no lag). */
@Mixin(value = class_638.class, remap = false)
public abstract class NoRenderTickMixin {
    @Inject(method = "method_18646(Lnet/minecraft/class_1297;)V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void hook$skipHidden(class_1297 entity, CallbackInfo ci) {
        if (NoRenderModule.skipsTick(entity)) ci.cancel();
    }
}
