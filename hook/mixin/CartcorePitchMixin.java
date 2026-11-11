/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_638
 *  net.minecraft.class_742
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thetruetrident.cartcore.B;
import thetruetrident.cartcore.CartcoreCore;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_746.class}, priority=700)
public abstract class CartcorePitchMixin
extends class_742
implements B {
    @Shadow
    @Final
    protected class_310 field_3937;
    @Unique
    private float cartcore$lastPitch;

    public CartcorePitchMixin(class_638 class_6382, GameProfile gameProfile) {
        super(class_6382, gameProfile);
    }

    @Inject(method={"method_5695"}, at={@At(value="RETURN")}, cancellable=true)
    private void modifyPitch(float f, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        float f2;
        if (CartcoreCore.targetPitch != null && this.field_6012 > CartcoreCore.cartActionLockUntilTick) {
            if (this.cartcore$lastPitch != 0.0f) {
                this.method_36457(this.cartcore$lastPitch);
            }
            CartcoreCore.targetPitch = null;
            CartcoreCore.pitchSpeed = 5.0f;
            this.cartcore$lastPitch = 0.0f;
            return;
        }
        if (CartcoreCore.targetPitch == null) {
            CartcoreCore.pitchSpeed = 5.0f;
            this.cartcore$lastPitch = 0.0f;
            return;
        }
        CartcoreCore.lastPitchAssistTick = this.field_6012;
        float f3 = ((Float)callbackInfoReturnable.getReturnValue()).floatValue();
        float f4 = CartcoreCore.pitchSpeed / 20.0f;
        float f5 = this.cartcore$lastPitch == 0.0f ? f3 : this.cartcore$lastPitch;
        this.cartcore$lastPitch = f2 = this.cartcore$approach(f5, CartcoreCore.targetPitch.floatValue(), f4);
        callbackInfoReturnable.setReturnValue(Float.valueOf(f2));
    }

    @Unique
    private float cartcore$approach(float f, float f2, float f3) {
        float f4 = class_3532.method_15393((float)(f2 - f));
        if (Math.abs(f4) <= f3) {
            return f2;
        }
        return f + class_3532.method_15363((float)f4, (float)(-f3), (float)f3);
    }

    @Override
    public void setLastPitch(float f) {
        this.cartcore$lastPitch = f;
    }

    @Override
    public float getLastPitch() {
        return this.cartcore$lastPitch;
    }

    @Override
    public float getPhysicalPitch() {
        return super.method_36455();
    }
}

