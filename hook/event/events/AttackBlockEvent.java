/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_2338;
import net.minecraft.class_2350;

public class AttackBlockEvent
extends CancellableEvent {
    public class_2338 pos;
    public class_2350 direction;

    public AttackBlockEvent(class_2338 pos, class_2350 direction) {
        this.pos = pos;
        this.direction = direction;
    }
}

