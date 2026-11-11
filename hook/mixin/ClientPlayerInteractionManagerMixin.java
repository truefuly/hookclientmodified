/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_636
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import hook.event.events.AttackBlockEvent;
import hook.manager.EventManager;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_636.class})
public class ClientPlayerInteractionManagerMixin {
    @Inject(method={"attackBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAttackBlock(class_2338 pos, class_2350 dir, CallbackInfoReturnable<Boolean> cir) {
        AttackBlockEvent event = new AttackBlockEvent(pos, dir);
        EventManager.b(event);
        if (event.isCancelled()) {
            cir.setReturnValue((Object)false);
        }
    }
}

