/*
 * Decompiled with CFR 0.152.
 */
package hook.event.events;

import hook.event.CancellableEvent;

public class MouseScrolledEvent
extends CancellableEvent {
    public double amount;

    public MouseScrolledEvent(double amount) {
        this.amount = amount;
    }
}

