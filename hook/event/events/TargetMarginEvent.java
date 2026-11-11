/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class TargetMarginEvent
extends CancellableEvent {
    public class_1297 entity;
    public CallbackInfoReturnable<Float> cir;

    public TargetMarginEvent(class_1297 entity, CallbackInfoReturnable<Float> cir) {
        this.entity = entity;
        this.cir = cir;
    }
}

