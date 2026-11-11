/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_757
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package hook.mixin;

import hook.feature.HookClickGuiScreen;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={class_757.class})
public abstract class GuiBlurMixin {
    @ModifyArg(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gl/GlobalSettings;set(IIDJLnet/minecraft/client/render/RenderTickCounter;ILnet/minecraft/client/render/Camera;Z)V"), index=5)
    private int hook$fadeMenuBlur(int var1) {
        return HookClickGuiScreen.blurRadius(var1);
    }
}

