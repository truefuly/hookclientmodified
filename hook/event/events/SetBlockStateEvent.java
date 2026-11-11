/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_2338;
import net.minecraft.class_2680;

public class SetBlockStateEvent
extends CancellableEvent {
    public class_2338 pos;
    public class_2680 newState;
    public class_2680 oldState;

    public SetBlockStateEvent(class_2338 pos, class_2680 newState, class_2680 oldState) {
        this.pos = pos;
        this.newState = oldState;
        this.oldState = newState;
    }
}

