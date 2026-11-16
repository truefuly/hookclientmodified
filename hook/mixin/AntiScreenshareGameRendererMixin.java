package hook.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import hook.feature.AntiScreenshareModule;
import net.minecraft.class_279;
import net.minecraft.class_310;
import net.minecraft.class_757;
import net.minecraft.class_758;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Anti Screenshare: saves the world image of the normal frame (before the HUD), and in the HUD-only clean pass skips
 * drawing the world again - the saved image is used instead.
 */
@Mixin(class_757.class)
public abstract class AntiScreenshareGameRendererMixin {
    @WrapOperation(method = "method_3192", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_757;method_3188(Lnet/minecraft/class_9779;)V"))
    private void hook$asLevel(class_757 self, class_9779 delta, Operation<Void> original) {
        if (AntiScreenshareModule.GUI_ONLY) return;
        original.call(self, delta);
    }

    @WrapOperation(method = "method_3192", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_757;method_37473()V"))
    private void hook$asShot(class_757 self, Operation<Void> original) {
        if (AntiScreenshareModule.GUI_ONLY) return;
        original.call(self);
    }

    @WrapOperation(method = "method_3192", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_761;method_3254()V"))
    private void hook$asOutline(class_761 self, Operation<Void> original) {
        if (AntiScreenshareModule.GUI_ONLY) return;
        original.call(self);
    }

    @WrapOperation(method = "method_3192", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_279;method_1258(Lnet/minecraft/class_276;Lnet/minecraft/class_9922;)V"))
    private void hook$asPost(class_279 self, net.minecraft.class_276 target, net.minecraft.class_9922 pool, Operation<Void> original) {
        if (AntiScreenshareModule.GUI_ONLY) return;
        original.call(self, target, pool);
    }

    @WrapOperation(method = "method_3192", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_758;method_71108()V"))
    private void hook$asWorldDone(class_758 self, Operation<Void> original) {
        if (AntiScreenshareModule.FULL_PASS) {
            try {
                AntiScreenshareModule.afterWorld(class_310.method_1551().method_1522());
            } catch (Throwable ignored) {
            }
        }
        original.call(self);
    }
}
