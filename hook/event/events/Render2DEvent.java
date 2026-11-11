/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_332;

public class Render2DEvent
extends CancellableEvent {
    public class_332 context;
    public float tickDelta;

    public Render2DEvent(class_332 context, float tickDelta) {
        this.context = context;
        this.tickDelta = tickDelta;
    }
}

