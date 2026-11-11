package hook.mixin;

import hook.feature.HitGlowModule;
import hook.feature.NametagsModule;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hit Glow outline color and hiding vanilla name tags for entities that get a Nametags tag. */
@Mixin(class_897.class)
public abstract class HitGlowMixins {
    @Inject(method = "method_62354(Lnet/minecraft/class_1297;Lnet/minecraft/class_10017;F)V", at = @At("TAIL"), remap = false)
    private void hook$renderState(class_1297 entity, class_10017 state, float tickDelta, CallbackInfo ci) {
        int color = HitGlowModule.outlineColor(entity);
        if (color != 0) {
            state.field_61821 = color;
        }
        if (NametagsModule.targets(entity)) {
            state.field_53337 = null;
        }
    }
}
