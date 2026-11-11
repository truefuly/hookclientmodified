/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_309
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.event.events.KeyEvent;
import hook.manager.EventManager;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_309.class})
public class KeyboardMixin {
    @Inject(method={"onKey"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPress(long window, int action, class_11908 input, CallbackInfo ci) {
        int key = input.comp_4795();
        if (key != -1) {
            KeyEvent event = new KeyEvent(key, window, action);
            EventManager.b(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }
}

