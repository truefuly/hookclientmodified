package hook.mixin;

import io.netty.channel.ChannelPipeline;
import net.minecraft.class_2535;
import net.minecraft.class_2598;
import net.minecraft.class_8762;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Puts the enabled HookVisuals proxy in front of every outgoing game connection (Connection.configureSerialization). */
@Mixin(class_2535.class)
public class ProxyPipelineMixin {
    @Inject(method = "method_48311(Lio/netty/channel/ChannelPipeline;Lnet/minecraft/class_2598;ZLnet/minecraft/class_8762;)V", at = @At("RETURN"), remap = false, require = 0)
    private static void hook$proxy(ChannelPipeline pipeline, class_2598 flow, boolean local, class_8762 monitor, CallbackInfo ci) {
        if (local || flow != class_2598.field_11942) return; // only our own client -> server connections
        try {
            hook.feature.ProxyManager.install(pipeline);
        } catch (Throwable t) {
            System.err.println("Hook: proxy setup failed: " + t);
        }
    }
}
