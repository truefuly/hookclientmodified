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
        long t0 = System.nanoTime();
        try {
            this.hook$asFrame(renderer, delta, renderLevel, original);
        } finally {
            if (hook.feature.AntiScreenshareTest.ENABLED) hook.feature.AntiScreenshareTest.frameTime(System.nanoTime() - t0);
        }
    }

    private void hook$asFrame(class_757 renderer, net.minecraft.class_9779 delta, boolean renderLevel, Operation<Void> original) {
        boolean on = AntiScreenshareModule.active();
        AntiScreenshareModule.CLIENT_DREW = false;
        AntiScreenshareModule.WORLD_SNAP = false;
        AntiScreenshareModule.WORLD_DIRTY = false;
        class_310 mc0 = (class_310) (Object) this;
        if (on) {
            try { AntiScreenshareModule.beginFrame(mc0.method_1522()); } catch (Throwable ignored) {}
        }
        AntiScreenshareModule.FULL_PASS = on;
        try {
            original.call(renderer, delta, renderLevel);
        } finally {
            AntiScreenshareModule.FULL_PASS = false;
            AntiScreenshareModule.popLayer();
        }
        if (hook.feature.AntiScreenshareTest.ENABLED) hook.feature.AntiScreenshareTest.frame();
        if (!on || !AntiScreenshareModule.active()) {
            AntiScreenshareModule.idle();
            return;
        }
        class_310 mc = (class_310) (Object) this;
        class_437 screen = mc.field_1755;
        boolean hideScreen = screen != null && isClientScreen(screen);
        boolean alreadyClean = !AntiScreenshareModule.CLIENT_DREW && !hideScreen;
        class_276 target = mc.method_1522();
        try {
            var color = target.method_30277();
            if (!(color instanceof net.minecraft.class_10868 gl)) return;
            AntiScreenshareModule.afterFullFrame(gl.method_68427(), target.field_1482, target.field_1481);
        } catch (Throwable t) {
            return;
        }
        if (!AntiScreenshareModule.active()) return;
        int w = target.field_1482, h = target.field_1481;
        // nothing of the client was drawn this frame: the normal frame already is the clean one - no second pass
        if (alreadyClean) {
            if (hook.feature.AntiScreenshareTest.ENABLED) hook.feature.AntiScreenshareTest.freeFrame();
            try {
                if (AntiScreenshareModule.CLEAN_DUE) AntiScreenshareModule.storeClean(target.method_30277(), w, h);
            } catch (Throwable ignored) {
            }
            return;
        }
        // between capture frames the last clean frame is reused - recorders only need ~30 FPS, you keep yours
        if (!AntiScreenshareModule.CLEAN_DUE) {
            try {
                AntiScreenshareModule.restoreClean(target.method_30277(), w, h);
                return;
            } catch (Throwable ignored) {
                // fall through and draw a fresh clean frame
            }
        }
        // second pass: clean frame for the real window. The clean world image was saved before any client 3D drawing
        // (that went into its own layer), so normally only the hand and the HUD are drawn again (almost free). Only
        // when a visual changed the world itself (x-ray like mixins) the whole world is rendered again.
        boolean guiOnly = false;
        if (AntiScreenshareModule.WORLD_SNAP && !AntiScreenshareModule.WORLD_DIRTY) {
            try { guiOnly = AntiScreenshareModule.restoreWorld(target.method_30277(), w, h); } catch (Throwable ignored) {}
        }
        if (hook.feature.AntiScreenshareTest.ENABLED) hook.feature.AntiScreenshareTest.cleanPass(guiOnly);
        AntiScreenshareModule.CLEAN_PASS = true;
        try {
            if (hideScreen) mc.field_1755 = null;
            if (guiOnly) {
                AntiScreenshareModule.GUI_ONLY = true;
            } else {
                com.mojang.blaze3d.systems.RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(target.method_30277(), 0, target.method_30278(), 1.0);
            }
            original.call(renderer, delta, renderLevel);
        } finally {
            if (hideScreen) mc.field_1755 = screen;
            AntiScreenshareModule.CLEAN_PASS = false;
            AntiScreenshareModule.GUI_ONLY = false;
        }
        try {
            AntiScreenshareModule.storeClean(target.method_30277(), w, h);
        } catch (Throwable ignored) {
        }
    }

    private static boolean isClientScreen(class_437 screen) {
        String n = screen.getClass().getName();
        return !n.startsWith("net.minecraft.");
    }
}
