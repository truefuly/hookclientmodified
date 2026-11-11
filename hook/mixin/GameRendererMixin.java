/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_757
 *  net.minecraft.class_9779
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import hook.Hook;
import hook.event.events.Render3DEvent;
import hook.feature.FreecamModule;
import hook.manager.EventManager;
import hook.utils.RenderUtils;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_757.class})
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private class_4184 field_18765;

    @Shadow
    protected abstract float method_3196(class_4184 var1, float var2, boolean var3);

    @Shadow
    public abstract Matrix4f method_22973(float var1);

    @Unique
    private static FreecamModule hook$freecam() {
        try {
            return Hook.INSTANCE != null && Hook.INSTANCE.getModuleManager() != null ? (FreecamModule)Hook.INSTANCE.getModuleManager().getModuleByClass(FreecamModule.class) : null;
        }
        catch (Throwable var1) {
            return null;
        }
    }

    @Inject(method={"renderWorld"}, at={@At(value="INVOKE_STRING", target="Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args={"ldc=hand"})})
    private void onWorldRender(class_9779 var1, CallbackInfo var2, @Local(ordinal=0) Matrix4f var3, @Local(ordinal=1) Matrix4f var4) {
        RenderUtils.updateScreenCenter(var3, var4);
        EventManager.b(new Render3DEvent(new class_4587(), new Matrix4f((Matrix4fc)var3), var1.method_60637(true)));
    }

    @Inject(method={"shouldRenderBlockOutline"}, at={@At(value="HEAD")}, cancellable=true)
    private void onShouldRenderBlockOutline(CallbackInfoReturnable<Boolean> var1) {
        FreecamModule var2 = GameRendererMixin.hook$freecam();
        if (var2 != null && var2.isEnabled()) {
            var1.setReturnValue((Object)false);
        }
    }
}

