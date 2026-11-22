package hook.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import hook.feature.VerseFallHudAnchors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Draws a VerseFall HUD element (music bar, now playing, armor...) at the size chosen in the HUD editor. */
@Mixin(targets = "com.spotifyclient.hud.Draggable", remap = false)
public abstract class VerseFallHudScaleMixin {
    @Shadow(remap = false)
    public abstract String id();

    @Shadow(remap = false)
    protected abstract float x();

    @Shadow(remap = false)
    protected abstract float y();

    @WrapMethod(method = "renderWithTilt(Lcom/spotifyclient/compat/GuiGraphics;)V", remap = false)
    private void hook$scaled(com.spotifyclient.compat.GuiGraphics c, Operation<Void> original) {
        float s = VerseFallHudAnchors.scaleOf(this.id());
        if (Math.abs(s - 1.0f) < 0.001f) {
            original.call(c);
            return;
        }
        float px = this.x(), py = this.y();
        c.pose().method_22903();
        c.pose().method_46416(px, py, 0.0f);
        c.pose().method_22905(s, s, 1.0f);
        c.pose().method_46416(-px, -py, 0.0f);
        try {
            original.call(c);
        } finally {
            c.pose().method_22909();
        }
    }
}
