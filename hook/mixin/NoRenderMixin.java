package hook.mixin;

import hook.feature.NoRenderModule;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = class_898.class, remap = false)
public abstract class NoRenderMixin {
    @Inject(method = "method_3950(Lnet/minecraft/class_1297;Lnet/minecraft/class_4604;DDD)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$noRender(net.minecraft.class_1297 entity, net.minecraft.class_4604 frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (NoRenderModule.hides(entity)) cir.setReturnValue(false);
    }
}
