package hook.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import hook.feature.AntiScreenshareModule;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Anti Screenshare: after the normal frame is drawn, it is shown in the capture-excluded overlay window, then the
 * frame is drawn a second time with the client's visuals off - that clean frame is what the game window (and so
 * every screen recorder) gets.
 */
@Mixin(class_310.class)
public abstract class AntiScreenshareMixin {
    @WrapOperation(method = "method_1523", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_757;method_3192(Lnet/minecraft/class_9779;Z)V"))
    private void hook$antiScreenshare(class_757 renderer, net.minecraft.class_9779 delta, boolean renderLevel, Operation<Void> original) {
        original.call(renderer, delta, renderLevel);
        if (!AntiScreenshareModule.active()) {
            AntiScreenshareModule.idle();
            return;
        }
        class_310 mc = (class_310) (Object) this;
        class_276 target = mc.method_1522();
        try {
            var color = target.method_30277();
            if (!(color instanceof net.minecraft.class_10868 gl)) return;
            AntiScreenshareModule.afterFullFrame(gl.method_68427(), target.field_1482, target.field_1481);
        } catch (Throwable t) {
            return;
        }
        if (!AntiScreenshareModule.active()) return;
        // second pass: clean frame for the real window
        class_437 screen = mc.field_1755;
        boolean hideScreen = screen != null && isClientScreen(screen);
        AntiScreenshareModule.CLEAN_PASS = true;
        try {
            if (hideScreen) mc.field_1755 = null;
            com.mojang.blaze3d.systems.RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(target.method_30277(), 0, target.method_30278(), 1.0);
            original.call(renderer, delta, renderLevel);
        } finally {
            if (hideScreen) mc.field_1755 = screen;
            AntiScreenshareModule.CLEAN_PASS = false;
        }
    }

    private static boolean isClientScreen(class_437 screen) {
        String n = screen.getClass().getName();
        return !n.startsWith("net.minecraft.");
    }
}
