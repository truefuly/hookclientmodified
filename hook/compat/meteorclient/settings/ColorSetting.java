/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.compat.meteorclient.utils.render.color.SettingColor;
import hook.module.setting.RgbSetting;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorSetting
extends RgbSetting
implements Setting<SettingColor> {
    private Consumer<SettingColor> onChanged;

    public ColorSetting(String name, SettingColor defaultValue) {
        super(name, ColorSetting.toArgb(defaultValue));
    }

    @Override
    public SettingColor get() {
        return new SettingColor(this.getArgb());
    }

    @Override
    public void set(SettingColor value) {
        if (value != null) {
            this.setArgb(ColorSetting.toArgb(value));
        }
    }

    @Override
    public void setArgb(int argb) {
        super.setArgb(argb);
        this.fire();
    }

    private void fire() {
        if (this.onChanged != null) {
            this.onChanged.accept(this.get());
        }
    }

    private static int toArgb(SettingColor color) {
        return color.a << 24 | color.r << 16 | color.g << 8 | color.b;
    }

    public static class Builder {
        private String name = "";
        private String description = "";
        private SettingColor defaultValue = new SettingColor(255, 255, 255, 255);
        private Consumer<SettingColor> onChanged;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(SettingColor defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder visible(Supplier<Boolean> visible) {
            return this;
        }

        public Builder onChanged(Consumer<SettingColor> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public ColorSetting build() {
            ColorSetting setting = new ColorSetting(this.name, this.defaultValue);
            setting.onChanged = this.onChanged;
            if (!this.description.isEmpty()) {
                setting.setDescription(this.description);
            }
            return setting;
        }
    }
}

