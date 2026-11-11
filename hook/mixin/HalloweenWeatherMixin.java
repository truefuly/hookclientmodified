/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_12077
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_4597
 *  net.minecraft.class_9976
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.halloween.HalloweenMist;
import hook.halloween.HalloweenParticles;
import net.minecraft.class_12077;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4597;
import net.minecraft.class_9976;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_9976.class})
public abstract class HalloweenWeatherMixin {
    @Inject(method={"renderPrecipitation"}, at={@At(value="TAIL")})
    private void hook$mist(class_4597 var1, class_243 var2, class_12077 var3, CallbackInfo var4) {
        HalloweenMist.draw(class_310.method_1551().field_1773.method_19418());
        HalloweenParticles.render(class_310.method_1551().field_1773.method_19418());
    }
}

