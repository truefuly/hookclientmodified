package hook.mixin;

import hook.feature.CreativeModule;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Creative "Fly Through": Player.tick sets noPhysics = isSpectator(); right after that, turn it on while noclipping. */
@Mixin(value = class_1657.class, remap = false)
public abstract class CreativeNoclipMixin {
    @Inject(method = "method_5773()V", at = @At(value = "FIELD", target = "Lnet/minecraft/class_1657;field_5960:Z", opcode = 181, shift = At.Shift.AFTER, ordinal = 0), remap = false, require = 0)
    private void hook$flyThrough(CallbackInfo ci) {
        class_1657 self = (class_1657) (Object) this;
        if (CreativeModule.noclip(self)) ((class_1297) self).field_5960 = true;
    }
}
