/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10017
 *  net.minecraft.class_10042
 *  net.minecraft.class_11659
 *  net.minecraft.class_1921
 *  net.minecraft.class_3887
 *  net.minecraft.class_4587
 *  net.minecraft.class_922
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import hook.feature.Character3dModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_11659;
import net.minecraft.class_1921;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_922.class})
public abstract class CharacterLivingRendererMixin {
    @Inject(method={"getRenderLayer"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$hideModelUnderCharacter(class_10042 var1, boolean var2, boolean var3, boolean var4, CallbackInfoReturnable<class_1921> var5) {
        if (Character3dModule.hidesModel((class_10017)var1)) {
            var5.setReturnValue(null);
        }
    }

    @WrapOperation(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/EntityRenderState;FF)V")})
    private void hook$hideLayersUnderCharacter(class_3887<?, ?> var1, class_4587 var2, class_11659 var3, int var4, class_10017 var5, float var6, float var7, Operation<Void> var8) {
        if (!Character3dModule.hidesLayer(var1, var5)) {
            var8.call(new Object[]{var1, var2, var3, var4, var5, Float.valueOf(var6), Float.valueOf(var7)});
        }
    }
}

