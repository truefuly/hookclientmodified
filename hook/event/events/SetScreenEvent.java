/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_437
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_437;

public class SetScreenEvent
extends CancellableEvent {
    public class_437 screen;

    public SetScreenEvent(class_437 screen) {
        this.screen = screen;
    }
}

