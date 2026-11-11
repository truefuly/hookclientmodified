/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.halloween.CinematicCamera;
import net.minecraft.class_304;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_310.class})
public abstract class HalloweenClientInputMixin {
    @Inject(method={"handleInputEvents"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$swallowInput(CallbackInfo var1) {
        if (CinematicCamera.inputLocked()) {
            class_310 var2 = ((class_310)(Object)this);
            if (var2.field_1690 != null) {
                for (class_304 var6 : var2.field_1690.field_1839) {
                    while (var6.method_1436()) {
                    }
                }
            }
            var1.cancel();
        }
    }
}

