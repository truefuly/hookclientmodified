/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1937
 *  net.minecraft.class_4184
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package hook.mixin;

import hook.Hook;
import hook.feature.FreecamModule;
import hook.feature.FreelookModule;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={class_4184.class})
public class CameraMixin {
    @Shadow
    private boolean field_18719;
    @Unique
    private float tickDelta;

    @Unique
    private static FreecamModule hook$freecam() {
        try {
            return Hook.INSTANCE != null && Hook.INSTANCE.MODULE_MANAGER != null ? (FreecamModule)Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(FreecamModule.class) : null;
        }
        catch (Throwable var1) {
            return null;
        }
    }

    @Unique
    private static FreelookModule hook$freelook() {
        try {
            return Hook.INSTANCE != null && Hook.INSTANCE.MODULE_MANAGER != null ? (FreelookModule)Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(FreelookModule.class) : null;
        }
        catch (Throwable var1) {
            return null;
        }
    }

    @Inject(method={"update"}, at={@At(value="HEAD")})
    private void onUpdateHead(class_1937 var1, class_1297 var2, boolean var3, boolean var4, float var5, CallbackInfo var6) {
        this.tickDelta = var5;
        FreecamModule var7 = CameraMixin.hook$freecam();
        if (var7 != null && var7.isEnabled()) {
            var7.updateCameraMovement();
        }
    }

    @Inject(method={"update"}, at={@At(value="TAIL")})
    private void keepPlayerVisible(class_1937 var1, class_1297 var2, boolean var3, boolean var4, float var5, CallbackInfo var6) {
        FreecamModule var7 = CameraMixin.hook$freecam();
        if (var7 != null && var7.isEnabled()) {
            this.field_18719 = true;
        }
    }

    @ModifyArgs(method={"update"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void update(Args var1) {
        FreecamModule var2 = CameraMixin.hook$freecam();
        if (var2 != null && var2.isEnabled()) {
            var1.set(0, (Object)var2.getInterpolatedX(this.tickDelta));
            var1.set(1, (Object)var2.getInterpolatedY(this.tickDelta));
            var1.set(2, (Object)var2.getInterpolatedZ(this.tickDelta));
        }
    }

    @ModifyArgs(method={"update"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void onUpdateSetRotationArgs(Args var1) {
        FreecamModule var2 = CameraMixin.hook$freecam();
        if (var2 != null && var2.isEnabled()) {
            var1.set(0, (Object)Float.valueOf((float)var2.getInterpolatedYaw(this.tickDelta)));
            var1.set(1, (Object)Float.valueOf((float)var2.getInterpolatedPitch(this.tickDelta)));
        } else {
            FreelookModule var3 = CameraMixin.hook$freelook();
            if (var3 != null && var3.isEnabled()) {
                var1.set(0, (Object)Float.valueOf(var3.getCameraYaw()));
                var1.set(1, (Object)Float.valueOf(var3.getCameraPitch()));
            }
        }
    }

    @ModifyArg(method={"update"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/Camera;clipToSpace(F)F"), index=0)
    private float useFreelookDistance(float var1) {
        FreelookModule var2 = CameraMixin.hook$freelook();
        return var2 != null && var2.isEnabled() ? var2.getDistance() : var1;
    }

    @Inject(method={"clipToSpace"}, at={@At(value="HEAD")}, cancellable=true)
    private void allowFreelookThroughWalls(float var1, CallbackInfoReturnable<Float> var2) {
        FreelookModule var3 = CameraMixin.hook$freelook();
        if (var3 != null && var3.isEnabled() && var3.isThroughWalls()) {
            var2.setReturnValue((Object)Float.valueOf(var1));
        }
    }
}

