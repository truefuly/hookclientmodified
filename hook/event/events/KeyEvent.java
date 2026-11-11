/*
 * Decompiled with CFR 0.152.
 */
package hook.event.events;

import hook.event.CancellableEvent;

public class KeyEvent
extends CancellableEvent {
    public int key;
    public int mode;
    public long window;

    public KeyEvent(int key, long window, int mode) {
        this.key = key;
        this.window = window;
        this.mode = mode;
    }
}

