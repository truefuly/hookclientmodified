/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.class_640
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
import net.minecraft.class_640;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_640.class})
public abstract class PlayerListEntrySkinMixin {
    @Inject(method={"method_52810"}, at={@At(value="RETURN")}, cancellable=true, remap=false)
    private void hook$replaceSkinTextures(CallbackInfoReturnable<class_8685> var1) {
        SkinProtectModule var2;
        if (Hook.INSTANCE != null && Hook.INSTANCE.MODULE_MANAGER != null && (var2 = (SkinProtectModule)Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(SkinProtectModule.class)) != null && var2.isEnabled()) {
            GameProfile var3 = ((class_640)this).method_2966();
            var1.setReturnValue((Object)var2.replacementFor(var3.id(), var3.name(), (class_8685)var1.getReturnValue()));
        }
    }
}

