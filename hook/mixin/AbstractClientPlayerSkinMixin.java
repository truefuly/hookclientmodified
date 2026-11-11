/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.class_742
 *  net.minecraft.class_8685
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import com.mojang.authlib.GameProfile;
import hook.Hook;
import hook.feature.SkinProtectModule;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_742.class})
public abstract class AbstractClientPlayerSkinMixin {
    @Inject(method={"method_52814"}, at={@At(value="RETURN")}, cancellable=true, remap=false)
    private void hook$replacePlayerSkin(CallbackInfoReturnable<class_8685> var1) {
        SkinProtectModule var2;
        if (Hook.INSTANCE != null && Hook.INSTANCE.MODULE_MANAGER != null && (var2 = (SkinProtectModule)Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(SkinProtectModule.class)) != null && var2.isEnabled()) {
            class_742 var3 = ((class_742)(Object)this);
            GameProfile var4 = var3.method_7334();
            var1.setReturnValue(var2.replacementFor(var4.id(), var4.name(), (class_8685)var1.getReturnValue()));
        }
    }
}

