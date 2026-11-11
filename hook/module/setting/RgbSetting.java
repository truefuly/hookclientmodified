/*
 * Decompiled with CFR 0.152.
 */
package hook.module.setting;

import hook.module.setting.Setting;

public class RgbSetting
extends Setting {
    private int argb;
    private final int defaultArgb;

    public RgbSetting(CharSequence var1, int var2) {
        super(var1);
        this.argb = var2;
        this.defaultArgb = var2;
    }

    public int getArgb() {
        return this.argb;
    }

    public void setArgb(int var1) {
        this.argb = var1;
    }

    public int getDefault() {
        return this.defaultArgb;
    }
}

