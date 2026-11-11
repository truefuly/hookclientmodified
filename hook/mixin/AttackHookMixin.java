package hook.mixin;

import hook.feature.HitGlowModule;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tells Hit Glow which entity the local player attacked. */
@Mixin(class_636.class)
public abstract class AttackHookMixin {
    @Inject(method = "method_2918(Lnet/minecraft/class_1657;Lnet/minecraft/class_1297;)V", at = @At("HEAD"), remap = false)
    private void hook$attack(class_1657 player, class_1297 target, CallbackInfo ci) {
        HitGlowModule.onAttack(target);
    }
}
