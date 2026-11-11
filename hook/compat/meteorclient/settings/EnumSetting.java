/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.module.setting.ModeSetting;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumSetting<T extends Enum<T>>
extends ModeSetting<T>
implements Setting<T> {
    private Consumer<T> onChanged;

    public EnumSetting(String name, T defaultValue, Class<T> type) {
        super(name, defaultValue, type);
    }

    @Override
    public T get() {
        return (T)this.getValue();
    }

    @Override
    public void set(T value) {
        if (value != null) {
            this.setMode((Enum<T>)value);
        }
    }

    @Override
    public void setMode(Enum<T> value) {
        super.setMode(value);
        this.fire();
    }

    @Override
    public void setModeIndex(int index) {
        super.setModeIndex(index);
        this.fire();
    }

    @Override
    public void cycleUp() {
        super.cycleUp();
        this.fire();
    }

    @Override
    public void cycleDown() {
        super.cycleDown();
        this.fire();
    }

    private void fire() {
        if (this.onChanged != null) {
            this.onChanged.accept(this.get());
        }
    }

    public static class Builder<T extends Enum<T>> {
        private String name = "";
        private String description = "";
        private T defaultValue;
        private Consumer<T> onChanged;

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> description(String description) {
            this.description = description;
            return this;
        }

        public Builder<T> defaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<T> visible(Supplier<Boolean> visible) {
            return this;
        }

        public Builder<T> onChanged(Consumer<T> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public EnumSetting<T> build() {
            if (this.defaultValue == null) {
                throw new IllegalStateException("EnumSetting '" + this.name + "' built without a default value");
            }
            Class type = ((Enum)this.defaultValue).getDeclaringClass();
            EnumSetting<T> setting = new EnumSetting<T>(this.name, this.defaultValue, type);
            setting.onChanged = this.onChanged;
            if (!this.description.isEmpty()) {
                setting.setDescription(this.description);
            }
            return setting;
        }
    }
}

