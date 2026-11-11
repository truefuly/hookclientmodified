/*
 * Decompiled with CFR 0.152.
 */
package hook.module.setting;

import hook.module.setting.Setting;

public final class ButtonSetting
extends Setting {
    private final Runnable action;

    public ButtonSetting(CharSequence var1, Runnable var2) {
        super(var1);
        this.action = var2;
    }

    public void run() {
        try {
            this.action.run();
        }
        catch (Throwable var2) {
            System.err.println("Hook: action failed: " + String.valueOf(var2));
        }
    }
}

