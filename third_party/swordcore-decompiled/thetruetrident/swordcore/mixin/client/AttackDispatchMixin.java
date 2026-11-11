/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_636
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package thetruetrident.swordcore.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thetruetrident.swordcore.SwordcoreClient;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_636.class})
public class AttackDispatchMixin {
    @Inject(method={"method_2918"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_634;method_52787(Lnet/minecraft/class_2596;)V", shift=At.Shift.AFTER)})
    private void swordcore$attackSent(class_1657 player, class_1297 target, CallbackInfo ci) {
        SwordcoreClient.onAttackDispatched(target);
    }
}

