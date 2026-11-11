/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.halloween.CinematicCamera;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_1297.class})
public abstract class HalloweenLookMixin {
    @Inject(method={"changeLookDirection"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$lockLook(double var1, double var3, CallbackInfo var5) {
        if (CinematicCamera.inputLocked() && this == class_310.method_1551().field_1724) {
            var5.cancel();
        }
    }
}

