package hook.mixin;

import hook.feature.MenuTheme;
import net.minecraft.class_332;
import net.minecraft.class_350;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Glass panel and accent lines for scrolling lists (servers, worlds, options) in menus. */
@Mixin(class_350.class)
public abstract class MenuListMixin {
    @Inject(method = "method_57715(Lnet/minecraft/class_332;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$listBackground(class_332 ctx, CallbackInfo ci) {
        if (MenuTheme.active()) {
            class_350<?> self = (class_350<?>) (Object) this;
            MenuTheme.listPanel(ctx, self.method_46426(), self.method_46427(), self.method_25368(), self.method_25364());
            ci.cancel();
        }
    }

    @Inject(method = "method_57713(Lnet/minecraft/class_332;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void hook$separators(class_332 ctx, CallbackInfo ci) {
        if (MenuTheme.active()) {
            class_350<?> self = (class_350<?>) (Object) this;
            MenuTheme.separators(ctx, self.method_46426(), self.method_46427(), self.method_55443(), self.method_25368());
            ci.cancel();
        }
    }
}
