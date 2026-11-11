package hook.mixin;

import hook.feature.VanillaHud;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Wraps the boss bar and scoreboard sidebar so the HUD editor can move, resize and hide them. */
@Mixin(class_329.class)
public abstract class VanillaHudMixin {
    @Inject(method = "method_70837(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$bossBarStart(class_332 ctx, class_9779 tick, CallbackInfo ci) {
        if (!VanillaHud.begin(VanillaHud.Part.BOSSBAR, ctx)) ci.cancel();
    }

    @Inject(method = "method_70837(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", at = @At("RETURN"), remap = false)
    private void hook$bossBarEnd(class_332 ctx, class_9779 tick, CallbackInfo ci) {
        VanillaHud.end(VanillaHud.Part.BOSSBAR, ctx);
    }

    @Inject(method = "method_55803(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$scoreboardStart(class_332 ctx, class_9779 tick, CallbackInfo ci) {
        if (!VanillaHud.begin(VanillaHud.Part.SCOREBOARD, ctx)) ci.cancel();
    }

    @Inject(method = "method_55803(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", at = @At("RETURN"), remap = false)
    private void hook$scoreboardEnd(class_332 ctx, class_9779 tick, CallbackInfo ci) {
        VanillaHud.end(VanillaHud.Part.SCOREBOARD, ctx);
    }
}
