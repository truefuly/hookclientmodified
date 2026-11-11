/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2248
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.module.setting.StringSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class BlockListSetting
extends StringSetting
implements Setting<List<class_2248>> {
    private Consumer<List<class_2248>> onChanged;

    public BlockListSetting(String name, class_2248 ... defaultValues) {
        super(name, BlockListSetting.idsOf(defaultValues));
    }

    @Override
    public List<class_2248> get() {
        ArrayList<class_2248> result = new ArrayList<class_2248>();
        String value = this.getValue();
        if (value == null || value.isEmpty()) {
            return result;
        }
        for (String part : value.split(",")) {
            class_2248 block;
            class_2960 id = class_2960.method_12829((String)part.trim());
            if (id == null || !class_7923.field_41175.method_10250(id) || (block = (class_2248)class_7923.field_41175.method_63535(id)) == null) continue;
            result.add(block);
        }
        return result;
    }

    @Override
    public void set(List<class_2248> value) {
        if (value != null) {
            this.setValue(BlockListSetting.idsOf(value.toArray(new class_2248[0])));
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        this.fire();
    }

    private void fire() {
        if (this.onChanged != null) {
            this.onChanged.accept((List<class_2248>)this.get());
        }
    }

    private static String idsOf(class_2248 ... blocks) {
        StringBuilder builder = new StringBuilder();
        for (class_2248 block : blocks) {
            class_2960 id = class_7923.field_41175.method_10221(block);
            if (id == null) continue;
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(id);
        }
        return builder.toString();
    }

    public static class Builder {
        private String name = "";
        private String description = "";
        private class_2248[] defaultValues = new class_2248[0];
        private Consumer<List<class_2248>> onChanged;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(class_2248 ... defaultValues) {
            this.defaultValues = defaultValues;
            return this;
        }

        public Builder visible(Supplier<Boolean> visible) {
            return this;
        }

        public Builder onChanged(Consumer<List<class_2248>> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public BlockListSetting build() {
            BlockListSetting setting = new BlockListSetting(this.name, this.defaultValues);
            setting.onChanged = this.onChanged;
            if (!this.description.isEmpty()) {
                setting.setDescription(this.description);
            }
            return setting;
        }
    }
}

