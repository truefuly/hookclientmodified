/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1747
 *  net.minecraft.class_1792
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.module.setting.ItemSetting;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_2246;
import net.minecraft.class_2248;

public class BlockSetting
extends ItemSetting
implements Setting<class_2248> {
    private Consumer<class_2248> onChanged;

    public BlockSetting(String name, class_2248 defaultValue) {
        super(name, defaultValue == null ? class_2246.field_10124.method_8389() : defaultValue.method_8389());
    }

    @Override
    public class_2248 get() {
        class_1792 item = this.getItem();
        if (item instanceof class_1747) {
            class_1747 blockItem = (class_1747)item;
            return blockItem.method_7711();
        }
        return class_2246.field_10124;
    }

    @Override
    public void set(class_2248 value) {
        if (value != null) {
            this.setItem(value.method_8389());
        }
    }

    @Override
    public void setItem(class_1792 item) {
        super.setItem(item);
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
        private class_2248 defaultValue = class_2246.field_10124;
        private Consumer<class_2248> onChanged;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(class_2248 defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder visible(Supplier<Boolean> visible) {
            return this;
        }

        public Builder onChanged(Consumer<class_2248> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public BlockSetting build() {
            BlockSetting setting = new BlockSetting(this.name, this.defaultValue);
            setting.onChanged = this.onChanged;
            if (!this.description.isEmpty()) {
                setting.setDescription(this.description);
            }
            return setting;
        }
    }
}

