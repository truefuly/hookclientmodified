/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2672
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_2672;

public class ChunkDataEvent
extends CancellableEvent {
    public class_2672 packet;

    public ChunkDataEvent(class_2672 packet) {
        this.packet = packet;
    }
}

