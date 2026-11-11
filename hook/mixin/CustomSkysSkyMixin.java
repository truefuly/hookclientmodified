/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12076
 *  net.minecraft.class_12131
 *  net.minecraft.class_2874$class_12326
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_638
 *  net.minecraft.class_9975
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.feature.CustomSkysModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12076;
import net.minecraft.class_12131;
import net.minecraft.class_2874;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_9975;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_9975.class})
public abstract class CustomSkysSkyMixin {
    @Inject(method={"updateRenderState"}, at={@At(value="TAIL")})
    private void hook$readSky(class_638 var1, float var2, class_4184 var3, class_12076 var4, CallbackInfo var5) {
        CustomSkysModule var6 = CustomSkysModule.get();
        if (var6 != null && var4.field_64464 == class_2874.class_12326.field_64386) {
            var6.extract(var1, var2, var4);
        }
    }

    @Inject(method={"renderTopSky"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$dome(int var1, CallbackInfo var2) {
        CustomSkysModule var3 = CustomSkysModule.get();
        if (var3 != null && var3.skyDisc()) {
            var2.cancel();
        }
    }

    @Inject(method={"renderGlowingSky"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$noSunriseFan(class_4587 var1, float var2, int var3, CallbackInfo var4) {
        CustomSkysModule var5 = CustomSkysModule.get();
        if (var5 != null && var5.replacesSunrise()) {
            var4.cancel();
        }
    }

    @Inject(method={"renderStars"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$stars(float var1, class_4587 var2, CallbackInfo var3) {
        CustomSkysModule var4 = CustomSkysModule.get();
        if (var4 != null && var4.stars(var2)) {
            var3.cancel();
        }
    }

    @Inject(method={"renderCelestialBodies"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/SkyRendering;renderMoon(Lnet/minecraft/world/MoonPhase;FLnet/minecraft/client/util/math/MatrixStack;)V")})
    private void hook$beforeMoon(class_4587 var1, float var2, float var3, float var4, class_12131 var5, float var6, float var7, CallbackInfo var8) {
        CustomSkysModule var9 = CustomSkysModule.get();
        if (var9 != null) {
            var9.beforeMoon(var1);
        }
    }

    @Inject(method={"renderCelestialBodies"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/SkyRendering;renderMoon(Lnet/minecraft/world/MoonPhase;FLnet/minecraft/client/util/math/MatrixStack;)V", shift=At.Shift.AFTER)})
    private void hook$afterMoon(class_4587 var1, float var2, float var3, float var4, class_12131 var5, float var6, float var7, CallbackInfo var8) {
        CustomSkysModule var9 = CustomSkysModule.get();
        if (var9 != null) {
            var9.afterMoon(var1);
        }
    }

    @Inject(method={"renderCelestialBodies"}, at={@At(value="TAIL")})
    private void hook$afterCelestials(class_4587 var1, float var2, float var3, float var4, class_12131 var5, float var6, float var7, CallbackInfo var8) {
        CustomSkysModule var9 = CustomSkysModule.get();
        if (var9 != null) {
            var9.afterCelestials();
        }
    }

    @Inject(method={"renderEndSky"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$endSky(CallbackInfo var1) {
        CustomSkysModule var2 = CustomSkysModule.get();
        if (var2 != null && var2.endSky()) {
            var1.cancel();
        }
    }
}

