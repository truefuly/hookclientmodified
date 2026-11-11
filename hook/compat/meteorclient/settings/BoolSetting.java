/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.module.setting.BooleanSetting;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoolSetting
extends BooleanSetting
implements Setting<Boolean> {
    private Consumer<Boolean> onChanged;

    public BoolSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Boolean get() {
        return this.getValue();
    }

    @Override
    public void set(Boolean value) {
        if (value != null) {
            this.setValue(value);
        }
    }

    @Override
    public void setValue(boolean value) {
        super.setValue(value);
        this.fire();
    }

    private void fire() {
        if (this.onChanged != null) {
            this.onChanged.accept(this.getValue());
        }
    }

    public static class Builder {
        private String name = "";
        private String description = "";
        private boolean defaultValue;
        private Consumer<Boolean> onChanged;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(boolean defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder visible(Supplier<Boolean> visible) {
            return this;
        }

        public Builder onChanged(Consumer<Boolean> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public BoolSetting build() {
            BoolSetting setting = new BoolSetting(this.name, this.defaultValue);
            setting.onChanged = this.onChanged;
            if (!this.description.isEmpty()) {
                setting.setDescription(this.description);
            }
            return setting;
        }
    }
}

