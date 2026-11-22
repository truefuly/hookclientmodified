package hook.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import hook.feature.NoFogModule;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_7285;
import net.minecraft.class_758;
import net.minecraft.class_9779;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * No Fog. The distances are written into vanilla's FogData itself, right after it is filled and before anything reads
 * it: Sodium's terrain fog copies FogData's fields (not the buffer arguments), so changing only the upload left the
 * chunks fogged. The colour is still tinted on the upload.
 */
@Mixin(value = {class_758.class}, priority = 1100)
public abstract class NoFogMixin {
    @Inject(method = {"method_3211(Lnet/minecraft/class_4184;ILnet/minecraft/class_9779;FLnet/minecraft/class_638;)Lorg/joml/Vector4f;"},
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getDevice()Lcom/mojang/blaze3d/systems/GpuDevice;", remap = false), remap = false)
    private void hook$noFogData(class_4184 camera, int viewDistance, class_9779 tick, float skyDarkness, class_638 world, CallbackInfoReturnable<Vector4f> cir, @Local class_7285 data) {
        float[] fog = {data.field_60582, data.field_60584, data.field_60583, data.field_60585, data.field_60099, data.field_60100};
        NoFogModule.apply(camera, fog, null);
        data.field_60582 = fog[0];
        data.field_60584 = fog[1];
        data.field_60583 = fog[2];
        data.field_60585 = fog[3];
        data.field_60099 = fog[4];
        data.field_60100 = fog[5];
    }

    @ModifyArgs(method = {"method_3211(Lnet/minecraft/class_4184;ILnet/minecraft/class_9779;FLnet/minecraft/class_638;)Lorg/joml/Vector4f;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_758;method_71110(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V", remap = false), remap = false)
    private void hook$noFog(Args args, class_4184 camera, int viewDistance, class_9779 tick, float skyDarkness, class_638 world) {
        // distances already come from the rewritten FogData; only tint the colour here
        Vector4f color = new Vector4f((Vector4fc) args.get(2));
        NoFogModule.tint(camera, color);
        args.set(2, color);
    }
}
