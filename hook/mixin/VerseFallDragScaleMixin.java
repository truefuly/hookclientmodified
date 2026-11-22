package hook.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.spotifyclient.hud.DragController;
import hook.feature.VerseFallHudAnchors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Grabbing, dragging and edge clamping of VerseFall HUD elements use their resized size. */
@Mixin(targets = "com.spotifyclient.hud.DragSystem", remap = false)
public abstract class VerseFallDragScaleMixin {
    @WrapOperation(method = "onClick", at = @At(value = "INVOKE", target = "Lcom/spotifyclient/hud/DragController;tryGrab(FFFF)Z"), remap = false)
    private boolean hook$grab(DragController g, float mx, float my, float w, float h, Operation<Boolean> original) {
        float s = VerseFallHudAnchors.scaleOf(g);
        return original.call(g, mx, my, w * s, h * s);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/spotifyclient/hud/DragController;tick(FFFFZ)V"), remap = false)
    private void hook$tick(DragController g, float mx, float my, float w, float h, boolean direct, Operation<Void> original) {
        float s = VerseFallHudAnchors.scaleOf(g);
        original.call(g, mx, my, w * s, h * s, direct);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/spotifyclient/hud/DragController;clamp(FF)V"), remap = false)
    private void hook$clamp(DragController g, float w, float h, Operation<Void> original) {
        float s = VerseFallHudAnchors.scaleOf(g);
        original.call(g, w * s, h * s);
    }
}
