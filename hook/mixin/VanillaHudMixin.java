package hook.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import hook.feature.HudModule;
import hook.feature.VanillaHud;
import net.minecraft.class_11223;
import net.minecraft.class_327;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Wraps every vanilla HUD part so the HUD editor can move, resize and hide it. @WrapMethod sits outside other mods'
 * HEAD-cancels (VerseFall's custom hotbar / effects), so the matrix push / pop always stays balanced.
 */
@Mixin(class_329.class)
public abstract class VanillaHudMixin {
    private static void hook$part(VanillaHud.Part part, class_332 ctx, Runnable draw) {
        if (!VanillaHud.begin(part, ctx)) return;
        try {
            draw.run();
        } finally {
            VanillaHud.end(part, ctx);
        }
    }

    @WrapMethod(method = "method_1753(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", remap = false)
    private void hook$frame(class_332 ctx, class_9779 tick, Operation<Void> original) {
        VanillaHud.newFrame();
        original.call(ctx, tick);
    }

    @WrapMethod(method = "method_70837(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", remap = false)
    private void hook$bossBar(class_332 ctx, class_9779 tick, Operation<Void> original) {
        hook$part(VanillaHud.Part.BOSSBAR, ctx, () -> original.call(ctx, tick));
    }

    @WrapMethod(method = "method_55803(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", remap = false)
    private void hook$scoreboard(class_332 ctx, class_9779 tick, Operation<Void> original) {
        hook$part(VanillaHud.Part.SCOREBOARD, ctx, () -> original.call(ctx, tick));
    }

    @WrapMethod(method = "method_1759(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", remap = false)
    private void hook$hotbar(class_332 ctx, class_9779 tick, Operation<Void> original) {
        hook$part(VanillaHud.Part.HOTBAR, ctx, () -> original.call(ctx, tick));
    }

    @WrapMethod(method = "method_1760(Lnet/minecraft/class_332;)V", remap = false)
    private void hook$statusBars(class_332 ctx, Operation<Void> original) {
        hook$part(VanillaHud.Part.STATUS, ctx, () -> original.call(ctx));
    }

    @WrapMethod(method = "method_1749(Lnet/minecraft/class_332;)V", remap = false)
    private void hook$heldItem(class_332 ctx, Operation<Void> original) {
        hook$part(VanillaHud.Part.HELD_ITEM, ctx, () -> original.call(ctx));
    }

    @WrapMethod(method = "method_55800(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", remap = false)
    private void hook$actionBar(class_332 ctx, class_9779 tick, Operation<Void> original) {
        hook$part(VanillaHud.Part.ACTIONBAR, ctx, () -> original.call(ctx, tick));
    }

    @WrapMethod(method = "method_55801(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", remap = false)
    private void hook$title(class_332 ctx, class_9779 tick, Operation<Void> original) {
        hook$part(VanillaHud.Part.TITLE, ctx, () -> original.call(ctx, tick));
    }

    @WrapMethod(method = "method_1765(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", remap = false)
    private void hook$effects(class_332 ctx, class_9779 tick, Operation<Void> original) {
        if (HudModule.HIDE_POTION_HUD.getValue() || hook.feature.EffectTimerModule.hidesVanilla()) return;
        hook$part(VanillaHud.Part.EFFECTS, ctx, () -> original.call(ctx, tick));
    }

    // the XP / locator / jump bar is drawn in three calls inside renderMainHud
    @WrapOperation(method = "method_55805(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_11223;method_70865(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"), remap = false)
    private void hook$bar(class_11223 bar, class_332 ctx, class_9779 tick, Operation<Void> original) {
        hook$part(VanillaHud.Part.XP, ctx, () -> original.call(bar, ctx, tick));
    }

    @WrapOperation(method = "method_55805(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_11223;method_70866(Lnet/minecraft/class_332;Lnet/minecraft/class_327;I)V"), remap = false)
    private void hook$level(class_332 ctx, class_327 font, int level, Operation<Void> original) {
        hook$part(VanillaHud.Part.XP, ctx, () -> original.call(ctx, font, level));
    }

    @WrapOperation(method = "method_55805(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_11223;method_70868(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"), remap = false)
    private void hook$barAddons(class_11223 bar, class_332 ctx, class_9779 tick, Operation<Void> original) {
        hook$part(VanillaHud.Part.XP, ctx, () -> original.call(bar, ctx, tick));
    }
}
