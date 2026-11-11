/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.halloween.CinematicOverlay;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_329.class})
public abstract class HalloweenHudMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$cinematicHud(class_332 var1, class_9779 var2, CallbackInfo var3) {
        if (CinematicOverlay.hideHud()) {
            CinematicOverlay.render(var1);
            var3.cancel();
        }
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void hook$cinematicOverlay(class_332 var1, class_9779 var2, CallbackInfo var3) {
        CinematicOverlay.render(var1);
    }
}

