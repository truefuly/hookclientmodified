/*
 * Decompiled with CFR 0.152.
 */
package hook.module.setting;

import hook.module.setting.Setting;

public class StringSetting
extends Setting {
    public String value;

    public StringSetting(CharSequence charSequence, String a) {
        super(charSequence);
        this.value = a;
    }

    public void setValue(String a) {
        this.value = a;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public StringSetting setDescription(CharSequence charSequence) {
        super.setDescription(charSequence);
        return this;
    }
}

