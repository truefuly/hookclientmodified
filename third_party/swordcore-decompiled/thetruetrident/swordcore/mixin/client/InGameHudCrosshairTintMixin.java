/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package thetruetrident.swordcore.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thetruetrident.swordcore.SwordcoreClient;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_329.class})
public class InGameHudCrosshairTintMixin {
    @Shadow
    @Final
    private static class_2960 field_45304;

    @Inject(method={"method_1753"}, at={@At(value="RETURN")})
    private void swordcore$renderVisualTint(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        SwordcoreClient.renderVisualTint(context);
    }

    @Inject(method={"method_1736"}, at={@At(value="RETURN")})
    private void swordcore$tintCrosshairBlue(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        class_310 client = class_310.method_1551();
        if (SwordcoreClient.isEnabled() && SwordcoreClient.swordConfig.visualEffects && client.field_1692 != null) {
            int x = (client.method_22683().method_4486() - 15) / 2;
            int y = (client.method_22683().method_4502() - 15) / 2;
            context.method_52707(class_10799.field_56890, field_45304, x, y, 15, 15, -3375360);
        }
    }
}

