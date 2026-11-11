/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_1041;

public class ResolutionChangedEvent
extends CancellableEvent {
    public class_1041 window;

    public ResolutionChangedEvent(class_1041 window) {
        this.window = window;
    }
}

