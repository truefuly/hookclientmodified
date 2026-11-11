/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_852
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.event.events.ChunkMarkClosedEvent;
import hook.manager.EventManager;
import net.minecraft.class_2338;
import net.minecraft.class_852;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_852.class})
public abstract class ChunkOcclusionDataBuilderMixin {
    @Inject(method={"markClosed"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMarkClosed(class_2338 var1, CallbackInfo var2) {
        if (EventManager.hasActiveListener(ChunkMarkClosedEvent.class)) {
            ChunkMarkClosedEvent var3 = new ChunkMarkClosedEvent();
            EventManager.b(var3);
            if (var3.isCancelled()) {
                var2.cancel();
            }
        }
    }
}

