/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_757
 *  net.minecraft.class_7833
 *  net.minecraft.class_9779
 *  org.joml.Matrix4f
 *  org.joml.Quaternionfc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import hook.feature.HalloweenGhostOverlay;
import hook.halloween.CinematicCamera;
import hook.halloween.CinematicOverlay;
import hook.halloween.Halloween;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.minecraft.class_7833;
import net.minecraft.class_9779;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_757.class})
public abstract class HalloweenGameRendererMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void hook$halloweenFrame(class_9779 var1, boolean var2, CallbackInfo var3) {
        try {
            Halloween.frame();
        }
        catch (Throwable var5) {
            System.err.println("Hook: Halloween frame failed: " + String.valueOf(var5));
        }
    }

    @Inject(method={"getFov"}, at={@At(value="RETURN")}, cancellable=true)
    private void hook$cinematicZoom(class_4184 var1, float var2, boolean var3, CallbackInfoReturnable<Float> var4) {
        if (var3 && CinematicCamera.active()) {
            var4.setReturnValue(Float.valueOf(CinematicCamera.fov(var4.getReturnValueF())));
        }
    }

    @Inject(method={"tiltViewWhenHurt"}, at={@At(value="HEAD")})
    private void hook$beatShake(class_4587 var1, float var2, CallbackInfo var3) {
        float var4 = HalloweenGhostOverlay.worldRoll();
        float var5 = HalloweenGhostOverlay.worldPitch();
        if (var4 != 0.0f || var5 != 0.0f) {
            var1.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(var4));
            var1.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(var5));
        }
    }

    @Inject(method={"renderHand"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$hideHand(float var1, boolean var2, Matrix4f var3, CallbackInfo var4) {
        if (CinematicOverlay.hideHud()) {
            var4.cancel();
        }
    }
}

