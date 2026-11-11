/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext.setting;

import hook.module.ext.setting.Setting;
import hook.module.setting.RgbSetting;
import hook.utils.EncryptedString;

public final class ColorSetting
extends Setting {
    private final RgbSetting backing;

    public ColorSetting(String var1, int var2) {
        super(var1);
        this.backing = new RgbSetting(EncryptedString.of(var1), var2);
    }

    public int argb() {
        return this.backing.getArgb();
    }

    public int rgb() {
        return this.argb() & 0xFFFFFF;
    }

    public int argbWithAlpha(int var1) {
        return Math.max(0, Math.min(255, var1)) << 24 | this.rgb();
    }

    public void setArgb(int var1) {
        if (this.argb() != var1) {
            this.backing.setArgb(var1);
            this.notifyChanged();
        }
    }

    public float[] value() {
        int var1 = this.argb();
        return new float[]{(float)(var1 >> 16 & 0xFF) / 255.0f, (float)(var1 >> 8 & 0xFF) / 255.0f, (float)(var1 & 0xFF) / 255.0f, (float)(var1 >>> 24) / 255.0f};
    }

    @Override
    public hook.module.setting.Setting hook() {
        return this.backing;
    }

    @Override
    protected Object snapshot() {
        return this.argb();
    }
}

