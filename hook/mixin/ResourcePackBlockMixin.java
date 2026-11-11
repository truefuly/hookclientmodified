package hook.mixin;

import hook.feature.ResourceBlockerModule;
import java.util.UUID;
import net.minecraft.class_2720;
import net.minecraft.class_2856;
import net.minecraft.class_8673;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Resource Blocker: answers like a client that loaded the pack, without downloading it. */
@Mixin(class_8673.class)
public abstract class ResourcePackBlockMixin {
    @Inject(method = "method_52784(Lnet/minecraft/class_2720;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$blockPack(class_2720 packet, CallbackInfo ci) {
        if (!ResourceBlockerModule.active()) return;
        class_8673 handler = (class_8673) (Object) this;
        UUID id = packet.comp_2158();
        handler.method_52787(new class_2856(id, class_2856.class_2857.field_13016));
        handler.method_52787(new class_2856(id, class_2856.class_2857.field_47704));
        handler.method_52787(new class_2856(id, class_2856.class_2857.field_13017));
        ResourceBlockerModule.blocked();
        ci.cancel();
    }
}
