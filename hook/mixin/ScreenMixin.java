/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  org.jetbrains.annotations.NotNull
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.feature.HookClickGuiScreen;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_437.class})
public class ScreenMixin {
    @Shadow
    @NotNull
    protected class_310 field_22787;

    @Inject(method={"renderBackground"}, at={@At(value="HEAD")}, cancellable=true)
    private void dontRenderBackground(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.field_22787.field_1755 instanceof HookClickGuiScreen) {
            ci.cancel();
        }
    }
}

