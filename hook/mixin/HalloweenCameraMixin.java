/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4184
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package hook.mixin;

import hook.halloween.CinematicCamera;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={class_4184.class})
public abstract class HalloweenCameraMixin {
    @ModifyArgs(method={"update"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal=0))
    private void hook$cinematicRiding(Args var1) {
        HalloweenCameraMixin.hook$apply(var1);
    }

    @ModifyArgs(method={"update"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal=1))
    private void hook$cinematic(Args var1) {
        HalloweenCameraMixin.hook$apply(var1);
    }

    @Unique
    private static void hook$apply(Args var0) {
        if (CinematicCamera.active()) {
            float var1 = ((Float)var0.get(0)).floatValue();
            float var2 = ((Float)var0.get(1)).floatValue();
            var0.set(0, (Object)Float.valueOf(CinematicCamera.yaw(var1)));
            var0.set(1, (Object)Float.valueOf(CinematicCamera.pitch(var2)));
        }
    }
}

