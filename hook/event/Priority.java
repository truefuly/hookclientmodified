/*
 * Decompiled with CFR 0.152.
 */
package hook.event;

public enum Priority {
    HIGHEST(0),
    HIGH(1),
    NORMAL(2),
    LOW(3),
    LOWEST(4);

    private final int value;

    private Priority(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

