package hook.mixin;

import hook.feature.PlayerEspModule;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Player ESP "Glow" mode: gives players the vanilla glowing outline (the same one as the Glowing effect, visible
 * through walls) in the ESP colour, by setting the outline colour of their render state.
 */
@Mixin(value = class_897.class, remap = false)
public abstract class PlayerEspGlowMixin {
    @Inject(method = "method_62354(Lnet/minecraft/class_1297;Lnet/minecraft/class_10017;F)V", at = @At("TAIL"), remap = false)
    private void hook$playerEspGlow(class_1297 entity, class_10017 state, float tickDelta, CallbackInfo ci) {
        int rgb = PlayerEspModule.glowColor(entity);
        if (rgb != 0) state.field_61821 = 0xFF000000 | rgb;
    }
}
