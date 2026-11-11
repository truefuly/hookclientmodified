/*
 * Decompiled with CFR 0.152.
 */
package hook.event.events;

import hook.event.CancellableEvent;

public class PostItemUseEvent
extends CancellableEvent {
    public int cooldown;

    public PostItemUseEvent(int cooldown) {
        this.cooldown = cooldown;
    }
}

