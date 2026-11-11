/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.module.ext.ExtBridge;
import hook.module.ext.Stage;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_310.class})
public abstract class ExtTickMixin {
    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void hook$extTickStart(CallbackInfo var1) {
        ExtBridge.tick(Stage.PRE);
    }

    @Inject(method={"tick"}, at={@At(value="RETURN")})
    private void hook$extTickEnd(CallbackInfo var1) {
        ExtBridge.tick(Stage.POST);
    }
}

