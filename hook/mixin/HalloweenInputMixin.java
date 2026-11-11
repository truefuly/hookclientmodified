/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10185
 *  net.minecraft.class_241
 *  net.minecraft.class_743
 *  net.minecraft.class_744
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.halloween.CinematicCamera;
import hook.mixin.InputAccessor;
import net.minecraft.class_10185;
import net.minecraft.class_241;
import net.minecraft.class_743;
import net.minecraft.class_744;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_743.class})
public abstract class HalloweenInputMixin {
    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void hook$freeze(CallbackInfo var1) {
        if (CinematicCamera.inputLocked()) {
            ((class_744)this).field_54155 = class_10185.field_54098;
            ((InputAccessor)((Object)this)).hook$setMovementVector(class_241.field_1340);
        }
    }
}

