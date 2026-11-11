/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_4050
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import hook.Hook;
import hook.event.events.TargetMarginEvent;
import hook.event.events.TargetPoseEvent;
import hook.feature.FreecamModule;
import hook.feature.FreelookModule;
import hook.manager.EventManager;
import net.minecraft.class_1297;
import net.minecraft.class_4050;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_1297.class})
public class EntityMixin {
    @Inject(method={"getTargetingMargin"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendMovementPackets(CallbackInfoReturnable<Float> var1) {
        if (EventManager.hasActiveListener(TargetMarginEvent.class)) {
            EventManager.b(new TargetMarginEvent((class_1297)class_1297.class.cast(this), var1));
        }
    }

    @Inject(method={"getPose"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetPose(CallbackInfoReturnable<class_4050> var1) {
        if (EventManager.hasActiveListener(TargetPoseEvent.class)) {
            EventManager.b(new TargetPoseEvent((class_1297)class_1297.class.cast(this), var1));
        }
    }

    @Inject(method={"changeLookDirection"}, at={@At(value="HEAD")}, cancellable=true)
    private void updateChangeLookDirection(double var1, double var3, CallbackInfo var5) {
        if (class_1297.class.cast(this) == Hook.mc.field_1724) {
            FreecamModule var6 = (FreecamModule)Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(FreecamModule.class);
            if (var6.isEnabled()) {
                var6.updateRotation(var1 * 0.15, var3 * 0.15);
                var5.cancel();
                return;
            }
            FreelookModule var7 = (FreelookModule)Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(FreelookModule.class);
            if (var7 != null && var7.isEnabled() && var7.keepPlayerHeadStill()) {
                var7.consumeMouseDelta(var1, var3);
                var5.cancel();
            }
        }
    }
}

