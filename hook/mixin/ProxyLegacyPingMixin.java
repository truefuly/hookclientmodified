package hook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When a server list ping fails, vanilla retries with the old 1.6 ping on its own direct socket - that would skip
 * the proxy and show your real IP. With a proxy on, that retry is skipped.
 */
@Mixin(net.minecraft.class_644.class)
public class ProxyLegacyPingMixin {
    @Inject(method = "method_3001(Ljava/net/InetSocketAddress;Lnet/minecraft/class_639;Lnet/minecraft/class_642;Lnet/minecraft/class_12239;)V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void hook$noDirectLegacyPing(CallbackInfo ci) {
        if (hook.feature.ProxyManager.active() != null) ci.cancel();
    }
}
