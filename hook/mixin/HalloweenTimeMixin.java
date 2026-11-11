/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1937
 *  net.minecraft.class_638
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import hook.halloween.HalloweenAtmosphere;
import net.minecraft.class_1937;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_1937.class})
public abstract class HalloweenTimeMixin {
    @Inject(method={"getTimeOfDay"}, at={@At(value="RETURN")}, cancellable=true)
    private void hook$halloweenTime(CallbackInfoReturnable<Long> var1) {
        if ((Object)this instanceof class_638 && HalloweenAtmosphere.overridingTime()) {
            var1.setReturnValue(HalloweenAtmosphere.timeOfDay(((class_1937)(Object)this), var1.getReturnValueJ()));
        }
    }
}

