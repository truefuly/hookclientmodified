package hook.mixin;

import hook.feature.NoParticlesModule;
import net.minecraft.class_1297;
import net.minecraft.class_2394;
import net.minecraft.class_702;
import net.minecraft.class_703;
import net.minecraft.class_727;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** No Particles: stops hidden particle types before they are created. */
@Mixin(class_702.class)
public abstract class NoParticlesMixin {
    /** True while a hidden particle is being added: it still gets created (callers like fireworks use it), just never added. */
    private static boolean hook$hiding;

    @Inject(method = "method_3056(Lnet/minecraft/class_2394;DDDDDD)Lnet/minecraft/class_703;", at = @At("HEAD"), remap = false)
    private void hook$noParticle(class_2394 effect, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<class_703> cir) {
        hook$hiding = NoParticlesModule.hides(effect);
    }

    @Inject(method = "method_3056(Lnet/minecraft/class_2394;DDDDDD)Lnet/minecraft/class_703;", at = @At("RETURN"), remap = false)
    private void hook$noParticleEnd(class_2394 effect, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<class_703> cir) {
        hook$hiding = false;
    }

    @Inject(method = "method_3058(Lnet/minecraft/class_703;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$noBlockCrumbs(class_703 particle, CallbackInfo ci) {
        if (hook$hiding || particle instanceof class_727 && NoParticlesModule.hidesBlockCrumbs()) ci.cancel();
    }

    @Inject(method = "method_3051(Lnet/minecraft/class_1297;Lnet/minecraft/class_2394;I)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$noEmitter(class_1297 entity, class_2394 effect, int ticks, CallbackInfo ci) {
        if (NoParticlesModule.hides(effect)) ci.cancel();
    }

    @Inject(method = "method_3061(Lnet/minecraft/class_1297;Lnet/minecraft/class_2394;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$noEmitterShort(class_1297 entity, class_2394 effect, CallbackInfo ci) {
        if (NoParticlesModule.hides(effect)) ci.cancel();
    }
}
