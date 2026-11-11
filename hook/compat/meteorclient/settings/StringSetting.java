/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringSetting
extends hook.module.setting.StringSetting
implements Setting<String> {
    private Consumer<String> onChanged;

    public StringSetting(String name, String defaultValue) {
        super(name, defaultValue == null ? "" : defaultValue);
    }

    @Override
    public String get() {
        String value = this.getValue();
        return value == null ? "" : value;
    }

    @Override
    public void set(String value) {
        if (value != null) {
            this.setValue(value);
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
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
        private String defaultValue = "";
        private Consumer<String> onChanged;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder placeholder(String placeholder) {
            return this;
        }

        public Builder visible(Supplier<Boolean> visible) {
            return this;
        }

        public Builder onChanged(Consumer<String> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public StringSetting build() {
            StringSetting setting = new StringSetting(this.name, this.defaultValue);
            setting.onChanged = this.onChanged;
            if (!this.description.isEmpty()) {
                setting.setDescription(this.description);
            }
            return setting;
        }
    }
}

