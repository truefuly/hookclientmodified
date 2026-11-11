/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11910
 *  net.minecraft.class_312
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.event.events.MouseButtonEvent;
import hook.event.events.MouseScrolledEvent;
import hook.manager.EventManager;
import net.minecraft.class_11910;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_312.class})
public abstract class MouseMixin {
    @Inject(method={"onMouseButton"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseButton(long window, class_11910 input, int action, CallbackInfo ci) {
        int button = input.comp_4801();
        if (button != -1) {
            MouseButtonEvent event = new MouseButtonEvent(button, window, action);
            EventManager.b(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method={"onMouseScroll"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseScrolledEvent event = new MouseScrolledEvent(vertical);
        EventManager.b(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}

