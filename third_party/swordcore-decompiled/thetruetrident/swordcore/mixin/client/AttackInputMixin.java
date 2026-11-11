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
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package thetruetrident.swordcore.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thetruetrident.swordcore.SwordcoreClient;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_310.class}, priority=1100)
public class AttackInputMixin {
    @Inject(method={"method_1508"}, at={@At(value="HEAD")})
    private void swordcore$processAttackInput(CallbackInfo ci) {
        SwordcoreClient.processAttackInput((class_310)this);
    }
}

