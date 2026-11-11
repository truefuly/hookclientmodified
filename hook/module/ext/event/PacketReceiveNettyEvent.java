/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2596
 */
package hook.module.ext.event;

import hook.module.ext.event.CancellableEvent;
import hook.module.ext.event.EventType;
import net.minecraft.class_2596;

public final class PacketReceiveNettyEvent
extends CancellableEvent {
    public static final EventType<PacketReceiveNettyEvent> TYPE = new EventType<PacketReceiveNettyEvent>(PacketReceiveNettyEvent.class);
    public class_2596<?> packet;

    public PacketReceiveNettyEvent(class_2596<?> var1) {
        this.packet = var1;
    }
}

