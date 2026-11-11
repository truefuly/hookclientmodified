/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.module.setting.StringSetting;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_2338;

public class BlockPosSetting
extends StringSetting
implements Setting<class_2338> {
    private final class_2338 defaultValue;
    private Consumer<class_2338> onChanged;

    public BlockPosSetting(String name, class_2338 defaultValue) {
        super(name, BlockPosSetting.format(defaultValue));
        this.defaultValue = defaultValue == null ? class_2338.field_10980 : defaultValue;
    }

    @Override
    public class_2338 get() {
        String value = this.getValue();
        if (value == null) {
            return this.defaultValue;
        }
        String[] parts = value.split(",");
        if (parts.length != 3) {
            return this.defaultValue;
        }
        try {
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            int z = Integer.parseInt(parts[2].trim());
            return new class_2338(x, y, z);
        }
        catch (NumberFormatException e) {
            return this.defaultValue;
        }
    }

    @Override
    public void set(class_2338 value) {
        if (value != null) {
            this.setValue(BlockPosSetting.format(value));
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

    private static String format(class_2338 pos) {
        return pos.method_10263() + ", " + pos.method_10264() + ", " + pos.method_10260();
    }

    public static class Builder {
        private String name = "";
        private String description = "";
        private class_2338 defaultValue = class_2338.field_10980;
        private Consumer<class_2338> onChanged;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(class_2338 defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder visible(Supplier<Boolean> visible) {
            return this;
        }

        public Builder onChanged(Consumer<class_2338> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public BlockPosSetting build() {
            BlockPosSetting setting = new BlockPosSetting(this.name, this.defaultValue);
            setting.onChanged = this.onChanged;
            if (!this.description.isEmpty()) {
                setting.setDescription(this.description);
            }
            return setting;
        }
    }
}

