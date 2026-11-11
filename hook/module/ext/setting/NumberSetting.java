/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext.setting;

import hook.module.ext.setting.Setting;
import hook.utils.EncryptedString;
import java.math.BigDecimal;
import java.util.Locale;

public final class NumberSetting
extends Setting {
    private final float initial;
    private final float min;
    private final float max;
    private float step;
    private hook.module.setting.NumberSetting backing;

    public NumberSetting(String var1, float var2, float var3, float var4) {
        super(var1);
        this.initial = var2;
        this.min = var3;
        this.max = var4;
    }

    public NumberSetting step(float var1) {
        if (var1 >= 0.0f && !Float.isInfinite(var1)) {
            this.step = var1;
        }
        return this;
    }

    public float step() {
        return this.step;
    }

    @Override
    public hook.module.setting.NumberSetting hook() {
        if (this.backing == null) {
            double var1 = this.step > 0.0f ? (double)this.step : 0.01;
            this.backing = new hook.module.setting.NumberSetting(EncryptedString.of(this.name()), this.min, this.max, this.initial, var1);
        }
        return this.backing;
    }

    public float get() {
        return (float)this.hook().getValue();
    }

    public int getInt() {
        return Math.round(this.get());
    }

    public void set(float var1) {
        float var2 = this.snap(var1);
        if (var2 != this.get()) {
            this.hook().getValue(var2);
            this.notifyChanged();
        }
    }

    public float snap(float var1) {
        float var2 = Math.max(this.min, Math.min(this.max, var1));
        return this.step > 0.0f ? this.min + (float)Math.round((var2 - this.min) / this.step) * this.step : var2;
    }

    public int decimals() {
        return this.step > 0.0f ? Math.max(0, new BigDecimal(Float.toString(this.step)).stripTrailingZeros().scale()) : 2;
    }

    public String formatted() {
        return String.format(Locale.ROOT, "%." + this.decimals() + "f", Float.valueOf(this.get()));
    }

    public float min() {
        return this.min;
    }

    public float max() {
        return this.max;
    }

    @Override
    protected Object snapshot() {
        return this.hook().getValue();
    }
}

