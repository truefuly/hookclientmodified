/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_743
 *  net.minecraft.class_744
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.Hook;
import hook.feature.FreecamModule;
import net.minecraft.class_743;
import net.minecraft.class_744;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_743.class}, remap=false)
public abstract class FreecamInputMixin {
    @Inject(method={"method_3129"}, at={@At(value="TAIL")}, remap=false)
    private void hook$continueBodyInput(CallbackInfo var1) {
        FreecamModule var2;
        if (Hook.INSTANCE != null && Hook.INSTANCE.getModuleManager() != null && (var2 = (FreecamModule)Hook.INSTANCE.getModuleManager().getModuleByClass(FreecamModule.class)) != null && var2.isEnabled()) {
            var2.applyContinuedPlayerInput((class_744)this);
        }
    }
}

