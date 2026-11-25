package hook.mixin;

import hook.feature.PlayerEspModule;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Player ESP "Glow" mode: tells the game (and culling mods like EntityCulling, which ask the same question) that
 * these players glow, so they are still rendered behind walls and their outline shows through.
 */
@Mixin(value = class_310.class, remap = false)
public abstract class PlayerEspOutlineMixin {
    @Inject(method = "method_27022(Lnet/minecraft/class_1297;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$playerEspOutline(class_1297 entity, CallbackInfoReturnable<Boolean> cir) {
        if (PlayerEspModule.glowColor(entity) != 0) cir.setReturnValue(true);
    }
}
