/*
 * Decompiled with CFR 0.152.
 */
package hook.module.setting;

import hook.module.setting.Setting;

public class NumberSetting
extends Setting {
    private final double min;
    private final double max;
    private double value;
    private final double format;
    private final double defaultValue;

    public NumberSetting(CharSequence charSequence, double min, double max, double defaultValue, double format) {
        super(charSequence);
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.format = format;
        this.defaultValue = defaultValue;
    }

    /** When set, this value no longer has its own slider: it follows a module's one "Speed" slider (1 = slow, 10 = fastest). */
    private NumberSetting speed;
    private double fast;
    private double slow;

    /** A module's one simple speed slider: 1 = slow and calm, 10 = as fast as possible. */
    public static NumberSetting speed(double defaultSpeed) {
        return new NumberSetting(hook.utils.EncryptedString.of("Speed"), 1.0, 10.0, defaultSpeed, 1.0)
                .getValue(hook.utils.EncryptedString.of("How fast it works: 1 = slow and calm, 10 = as fast as possible"));
    }

    /** Lets this delay follow {@code speed}: {@code fast} at speed 10, {@code slow} at speed 1. */
    public NumberSetting follow(NumberSetting speed, double fast, double slow) {
        this.speed = speed;
        this.fast = fast;
        this.slow = slow;
        return this;
    }

    public double getValue() {
        if (this.speed != null) {
            double t = (10.0 - this.speed.getValue()) / 9.0;
            double v = this.fast + (this.slow - this.fast) * t;
            double n = 1.0 / this.format;
            return (double)Math.round(v * n) / n;
        }
        return this.value;
    }

    public double getDefaultValue() {
        return this.defaultValue;
    }

    public double getFormat() {
        return this.format;
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    public int getIntValue() {
        return (int)this.getValue();
    }

    public float getFloatValue() {
        return (float)this.getValue();
    }

    public long getLongValue() {
        return (long)this.getValue();
    }

    public void getValue(double b) {
        double n = 1.0 / this.format;
        this.value = (double)Math.round(Math.max(this.min, Math.min(this.max, b)) * n) / n;
    }

    public NumberSetting getValue(CharSequence charSequence) {
        super.setDescription(charSequence);
        return this;
    }
}

