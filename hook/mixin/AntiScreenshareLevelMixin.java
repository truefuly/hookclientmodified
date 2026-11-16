package hook.mixin;

import hook.feature.AntiScreenshareModule;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Anti Screenshare HUD-only clean pass: the clean world image of the normal frame is already in the window, so the
 * world is not drawn again - only the hand, vanilla outlines/post effects and the HUD on top.
 */
@Mixin(class_761.class)
public abstract class AntiScreenshareLevelMixin {
    @Inject(method = "method_22710", at = @At("HEAD"), cancellable = true)
    private void hook$asSkipWorld(CallbackInfo ci) {
        if (AntiScreenshareModule.GUI_ONLY) ci.cancel();
    }
}
