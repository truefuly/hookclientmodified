package hook.mixin;

import hook.feature.ForceRenderModule;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Force Render: every entity the server sends is drawn, no matter how far (vanilla limits by Entity Distance). */
@Mixin(value = class_1297.class, remap = false)
public abstract class ForceRenderDistanceMixin {
    @Inject(method = "method_5640(D)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$forceEntityDistance(double sqrDistance, CallbackInfoReturnable<Boolean> cir) {
        if (ForceRenderModule.ignoreDistances()) cir.setReturnValue(true);
    }
}
