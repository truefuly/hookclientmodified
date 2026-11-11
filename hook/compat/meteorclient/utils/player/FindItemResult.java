/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.utils.player;

public class FindItemResult {
    private final int slot;
    private final int count;

    public FindItemResult(int slot, int count) {
        this.slot = slot;
        this.count = count;
    }

    public boolean found() {
        return this.slot >= 0;
    }

    public boolean empty() {
        return !this.found();
    }

    public int slot() {
        return this.slot;
    }

    public int count() {
        return this.count;
    }

    public boolean isHotbar() {
        return this.slot >= 0 && this.slot < 9;
    }

    public int hotbar() {
        return this.isHotbar() ? this.slot : -1;
    }
}

