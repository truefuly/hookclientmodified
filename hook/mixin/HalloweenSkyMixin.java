/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_12076
 *  net.minecraft.class_4184
 *  net.minecraft.class_638
 *  net.minecraft.class_9975
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.halloween.HalloweenAtmosphere;
import hook.halloween.HalloweenSky;
import net.minecraft.class_12076;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_9975;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_9975.class})
public abstract class HalloweenSkyMixin {
    @Inject(method={"renderTopSky"}, at={@At(value="TAIL")})
    private void hook$halloweenSky(int var1, CallbackInfo var2) {
        HalloweenSky.draw();
    }

    @Inject(method={"updateRenderState"}, at={@At(value="TAIL")})
    private void hook$moon(class_638 var1, float var2, class_4184 var3, class_12076 var4, CallbackInfo var5) {
        HalloweenAtmosphere.moonAngle(var4.field_64465);
    }
}

