/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext.setting;

import hook.module.ext.setting.Setting;
import hook.utils.EncryptedString;

public final class BooleanSetting
extends Setting {
    private final hook.module.setting.BooleanSetting backing;

    public BooleanSetting(String var1, boolean var2) {
        super(var1);
        this.backing = new hook.module.setting.BooleanSetting(EncryptedString.of(var1), var2);
    }

    public boolean get() {
        return this.backing.getValue();
    }

    public void set(boolean var1) {
        if (this.backing.getValue() != var1) {
            this.backing.setValue(var1);
            this.notifyChanged();
        }
    }

    @Override
    public hook.module.setting.Setting hook() {
        return this.backing;
    }

    @Override
    protected Object snapshot() {
        return this.backing.getValue();
    }
}

