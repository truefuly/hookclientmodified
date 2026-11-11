/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.module.setting.NumberSetting;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DoubleSetting
extends NumberSetting
implements Setting<Double> {
    private Consumer<Double> onChanged;

    public DoubleSetting(String name, double min, double max, double defaultValue) {
        super(name, min, max, defaultValue, 0.01);
    }

    @Override
    public Double get() {
        return this.getValue();
    }

    @Override
    public void set(Double value) {
        if (value != null) {
            this.getValue(value);
        }
    }

    @Override
    public void getValue(double value) {
        super.getValue(value);
        this.fire();
    }

    private void fire() {
        if (this.onChanged != null) {
            this.onChanged.accept(this.get());
        }
    }

    public static class Builder {
        private String name = "";
        private String description = "";
        private double defaultValue;
        private double min = Double.NaN;
        private double max = Double.NaN;
        private Consumer<Double> onChanged;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(double defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder range(double min, double max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public Builder sliderRange(double min, double max) {
            if (Double.isNaN(this.min)) {
                this.min = min;
                this.max = max;
            }
            return this;
        }

        public Builder visible(Supplier<Boolean> visible) {
            return this;
        }

        public Builder onChanged(Consumer<Double> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public DoubleSetting build() {
            double lo = Double.isNaN(this.min) ? Math.min(0.0, this.defaultValue) : this.min;
            double hi = Double.isNaN(this.max) ? Math.max(1000.0, this.defaultValue) : this.max;
            DoubleSetting setting = new DoubleSetting(this.name, lo, hi, this.defaultValue);
            setting.onChanged = this.onChanged;
            if (!this.description.isEmpty()) {
                setting.setDescription(this.description);
            }
            return setting;
        }
    }
}

