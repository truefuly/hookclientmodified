/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext.setting;

import hook.module.ext.setting.Setting;
import hook.module.setting.ButtonSetting;
import hook.utils.EncryptedString;

public final class ActionSetting
extends Setting {
    private final ButtonSetting backing;

    public ActionSetting(String var1, Runnable var2) {
        super(var1);
        this.backing = new ButtonSetting(EncryptedString.of(var1), var2);
    }

    public void run() {
        this.backing.run();
    }

    @Override
    public hook.module.setting.Setting hook() {
        return this.backing;
    }

    @Override
    protected Object snapshot() {
        return Boolean.TRUE;
    }
}

