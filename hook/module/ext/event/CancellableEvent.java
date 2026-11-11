/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext.event;

public abstract class CancellableEvent {
    private boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }

    public void setCancelled(boolean var1) {
        this.cancelled = var1;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
}

