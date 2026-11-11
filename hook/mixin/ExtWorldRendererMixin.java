/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  net.minecraft.class_12180$class_12181
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  net.minecraft.class_4604
 *  net.minecraft.class_5636
 *  net.minecraft.class_758$class_4596
 *  net.minecraft.class_761
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import hook.mixin.HookGameRendererAccessor;
import hook.module.ext.ExtBridge;
import hook.utils.ext.ShaderOverlay;
import hook.utils.ext.TracerEngine;
import net.minecraft.class_12180;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4604;
import net.minecraft.class_5636;
import net.minecraft.class_758;
import net.minecraft.class_761;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_761.class})
public abstract class ExtWorldRendererMixin {
    @ModifyArg(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/render/Frustum;DDDF)V"), index=0)
    private class_4604 hook$extRender3D(class_4604 var1, @Local(argsOnly=true, ordinal=0) Matrix4f var2, @Local(argsOnly=true, ordinal=1) Matrix4f var3) {
        float var4 = class_310.method_1551().method_61966().method_60637(false);
        TracerEngine.beginFrame((Matrix4fc)var2, (Matrix4fc)var3);
        try (class_12180.class_12181 var5 = ShaderOverlay.capture();){
            ExtBridge.render3D(var4, var1);
        }
        return var1;
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void hook$extOverlayAfterShaderPack(CallbackInfo var1, @Local(argsOnly=true) GpuBufferSlice var2, @Local(argsOnly=true) class_4184 var3, @Local(argsOnly=true, ordinal=0) Matrix4f var4, @Local(argsOnly=true, ordinal=1) Matrix4f var5) {
        if (ShaderOverlay.pending()) {
            ShaderOverlay.render(var3, var4, var5, var2, ExtWorldRendererMixin.hook$onTopFog(var2, var3));
        }
    }

    @Unique
    private static GpuBufferSlice hook$onTopFog(GpuBufferSlice var0, class_4184 var1) {
        return var1.method_19334() == class_5636.field_27888 ? var0 : ((HookGameRendererAccessor)class_310.method_1551().field_1773).hook$getFogRenderer().method_71109(class_758.class_4596.field_60101);
    }
}

