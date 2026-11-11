/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2596
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_2596;

public class PacketSendEvent
extends CancellableEvent {
    public class_2596<?> packet;

    public PacketSendEvent(class_2596<?> packet) {
        this.packet = packet;
    }
}

