/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.utils.misc;

public class Keybind {
    public static final int NONE = -1;
    private int key;

    public Keybind() {
        this(-1);
    }

    public Keybind(int key) {
        this.key = key;
    }

    public static Keybind none() {
        return new Keybind(-1);
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isSet() {
        return this.key != -1;
    }

    public String toStr() {
        return this.isSet() ? String.valueOf(this.key) : "None";
    }
}

