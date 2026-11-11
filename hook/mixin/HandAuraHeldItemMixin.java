/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_11684
 *  net.minecraft.class_310
 *  net.minecraft.class_759
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package hook.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import hook.feature.HandAuraModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11684;
import net.minecraft.class_310;
import net.minecraft.class_759;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_759.class})
public abstract class HandAuraHeldItemMixin {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapOperation(method={"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/command/RenderDispatcher;render()V")})
    private void hook$handAura(class_11684 var1, Operation<Void> var2) {
        if (HandAuraModule.active != null && HandAuraModule.beginCapture()) {
            boolean var3 = false;
            try {
                var2.call(new Object[]{var1});
                class_310.method_1551().method_22940().method_23000().method_22993();
                var3 = true;
            }
            finally {
                HandAuraModule.endCapture(var3);
            }
        } else {
            var2.call(new Object[]{var1});
        }
    }
}

