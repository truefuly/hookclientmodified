/*
 * Decompiled with CFR 0.152.
 */
package hook.event;

public interface Event {
    default public boolean isCancelled() {
        return false;
    }
}

