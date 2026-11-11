/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2676
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_2676;

public final class LightUpdateEvent
extends CancellableEvent {
    public final class_2676 packet;

    public LightUpdateEvent(class_2676 var1) {
        this.packet = var1;
    }
}

