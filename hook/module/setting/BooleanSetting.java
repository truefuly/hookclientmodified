/*
 * Decompiled with CFR 0.152.
 */
package hook.module.setting;

import hook.module.setting.Setting;

public class BooleanSetting
extends Setting {
    private boolean value;
    private final boolean defaultValue;

    public BooleanSetting(CharSequence charSequence, boolean value) {
        super(charSequence);
        this.value = value;
        this.defaultValue = value;
    }

    public void toggle() {
        this.setValue(!this.value);
    }

    public void setValue(boolean a) {
        this.value = a;
    }

    public boolean getDefaultValue() {
        return this.defaultValue;
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public BooleanSetting setDescription(CharSequence charSequence) {
        super.setDescription(charSequence);
        return this;
    }
}

