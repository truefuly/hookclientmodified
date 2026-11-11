/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_761
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.feature.BlockOverlayModule;
import hook.module.ext.ModuleRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_761.class})
public abstract class BlockOverlayWorldRendererMixin {
    @Inject(method={"renderTargetBlockOutline"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$hideVanillaOutline(CallbackInfo var1) {
        BlockOverlayModule var2 = ModuleRegistry.get(BlockOverlayModule.class);
        if (var2 != null && var2.isEnabled()) {
            var1.cancel();
        }
    }
}

