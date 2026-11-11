/*
 * Decompiled with CFR 0.152.
 */
package hook.event.events;

import hook.event.CancellableEvent;

public class PreItemUseEvent
extends CancellableEvent {
    public int cooldown;

    public PreItemUseEvent(int cooldown) {
        this.cooldown = cooldown;
    }
}

