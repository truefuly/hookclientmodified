/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext.event;

public final class EventType<E> {
    private final Class<E> type;

    public EventType(Class<E> var1) {
        this.type = var1;
    }

    public Class<E> type() {
        return this.type;
    }
}

