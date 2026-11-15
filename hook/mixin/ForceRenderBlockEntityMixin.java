package hook.mixin;

import hook.feature.ForceRenderModule;
import net.minecraft.class_824;
import net.minecraft.class_827;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Force Render: chests, signs, banners... render at any distance instead of only within 64 blocks. */
@Mixin(value = class_824.class, remap = false)
public abstract class ForceRenderBlockEntityMixin {
    @Redirect(method = "method_74348", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_827;method_33892(Lnet/minecraft/class_2586;Lnet/minecraft/class_243;)Z"), remap = false, require = 0)
    private boolean hook$forceBlockEntityDistance(class_827<?, ?> renderer, net.minecraft.class_2586 blockEntity, net.minecraft.class_243 camera) {
        if (ForceRenderModule.ignoreDistances()) return true;
        return ((class_827) renderer).method_33892(blockEntity, camera);
    }
}
