/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2604
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_2604;

public class EntitySpawnEvent
extends CancellableEvent {
    public class_2604 packet;

    public EntitySpawnEvent(class_2604 packet) {
        this.packet = packet;
    }
}

