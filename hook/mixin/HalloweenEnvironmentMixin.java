/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_12197
 *  net.minecraft.class_12202
 *  net.minecraft.class_12206
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import hook.halloween.HalloweenAtmosphere;
import net.minecraft.class_12197;
import net.minecraft.class_12202;
import net.minecraft.class_12206;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_12202.class})
public abstract class HalloweenEnvironmentMixin {
    @Inject(method={"get"}, at={@At(value="RETURN")}, cancellable=true)
    private void hook$halloweenSky(class_12197<?> var1, float var2, CallbackInfoReturnable<Object> var3) {
        if (!(HalloweenAtmosphere.strength() <= 0.0f)) {
            Object var4 = var3.getReturnValue();
            if (var1 == class_12206.field_63746 && var4 instanceof Integer) {
                Integer var5 = (Integer)var4;
                var3.setReturnValue((Object)HalloweenAtmosphere.skyColor(var5));
            } else if (var1 == class_12206.field_64339 && var4 instanceof Integer) {
                Integer var6 = (Integer)var4;
                var3.setReturnValue((Object)HalloweenAtmosphere.sunset(var6));
            } else if (var1 == class_12206.field_64126 && var4 instanceof Integer) {
                Integer var7 = (Integer)var4;
                var3.setReturnValue((Object)HalloweenAtmosphere.clouds(var7));
            } else if (var1 == class_12206.field_64344 && var4 instanceof Float) {
                Float var8 = (Float)var4;
                var3.setReturnValue((Object)Float.valueOf(HalloweenAtmosphere.stars(var8.floatValue())));
            }
        }
    }
}

