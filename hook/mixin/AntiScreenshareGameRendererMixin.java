package hook.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import hook.feature.AntiScreenshareModule;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Anti Screenshare: no auto screenshot in the HUD-only clean pass (the world is not drawn there). */
@Mixin(class_757.class)
public abstract class AntiScreenshareGameRendererMixin {
    @WrapOperation(method = "method_3192", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_757;method_37473()V"))
    private void hook$asShot(class_757 self, Operation<Void> original) {
        if (AntiScreenshareModule.GUI_ONLY) return;
        original.call(self);
    }
}
