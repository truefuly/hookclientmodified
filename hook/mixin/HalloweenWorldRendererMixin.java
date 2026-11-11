/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  net.minecraft.class_11658
 *  net.minecraft.class_11659
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_761
 *  net.minecraft.class_9779
 *  net.minecraft.class_9922
 *  org.joml.Matrix4f
 *  org.joml.Vector4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import hook.halloween.HalloweenSky;
import hook.halloween.WitchScene;
import net.minecraft.class_11658;
import net.minecraft.class_11659;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_761.class})
public abstract class HalloweenWorldRendererMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void hook$captureMatrices(class_9922 var1, class_9779 var2, boolean var3, class_4184 var4, Matrix4f var5, Matrix4f var6, Matrix4f var7, GpuBufferSlice var8, Vector4f var9, boolean var10, CallbackInfo var11) {
        HalloweenSky.capture(var5, var6);
    }

    @Inject(method={"pushEntityRenders"}, at={@At(value="TAIL")})
    private void hook$witch(class_4587 var1, class_11658 var2, class_11659 var3, CallbackInfo var4) {
        if (WitchScene.running()) {
            WitchScene.render(class_310.method_1551().field_1773.method_19418().method_71156());
        }
    }
}

