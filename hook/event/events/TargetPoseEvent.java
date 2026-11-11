/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_4050
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_1297;
import net.minecraft.class_4050;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class TargetPoseEvent
extends CancellableEvent {
    public class_1297 entity;
    public CallbackInfoReturnable<class_4050> cir;

    public TargetPoseEvent(class_1297 entity, CallbackInfoReturnable<class_4050> cir) {
        this.entity = entity;
        this.cir = cir;
    }
}

