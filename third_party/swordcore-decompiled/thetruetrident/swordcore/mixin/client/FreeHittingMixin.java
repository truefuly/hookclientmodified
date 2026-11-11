/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package thetruetrident.swordcore.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thetruetrident.swordcore.SpearcoreCompatBridge;
import thetruetrident.swordcore.SwordcoreClient;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_310.class})
public class FreeHittingMixin {
    @Inject(method={"method_1536"}, at={@At(value="HEAD")}, cancellable=true)
    private void swordcore$cancelAirAttack(CallbackInfoReturnable<Boolean> cir) {
        class_310 client = class_310.method_1551();
        if (SwordcoreClient.shouldSuppressDuplicateClick(client)) {
            cir.setReturnValue((Object)false);
            return;
        }
        if (SwordcoreClient.isEnabled() && !SwordcoreClient.swordConfig.freeHitting && SwordcoreClient.isAirAttack(client) && !SpearcoreCompatBridge.isLungeSwapClickActive()) {
            cir.setReturnValue((Object)false);
        }
    }
}

