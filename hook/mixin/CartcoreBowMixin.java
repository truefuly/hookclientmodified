/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1753
 *  net.minecraft.class_1799
 *  net.minecraft.class_1937
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1753;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thetruetrident.cartcore.CartcoreClient;
import thetruetrident.cartcore.CartcoreCore;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_1753.class})
public abstract class CartcoreBowMixin {
    @Inject(method={"method_7840"}, at={@At(value="HEAD")})
    private void onShoot(class_1799 class_17992, class_1937 class_19372, class_1309 class_13092, int n, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (!class_19372.method_8608()) {
            return;
        }
        if (!CartcoreCore.cartConfig.instaCarting) {
            return;
        }
        class_310 class_3102 = class_310.method_1551();
        if (!CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "rail") || !CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "tnt_minecart")) {
            return;
        }
        if (class_13092 instanceof class_1657) {
            class_1657 class_16572 = (class_1657)class_13092;
            int n2 = ((class_1753)(Object)this).method_7881(class_17992, class_13092) - n;
            float f = class_1753.method_7722((int)n2);
            if (f >= 0.1f) {
                CartcoreClient.shotBow = true;
            }
        }
    }
}

