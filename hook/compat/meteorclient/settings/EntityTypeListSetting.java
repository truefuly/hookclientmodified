/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1299
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.module.setting.StringSetting;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_1299;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class EntityTypeListSetting
extends StringSetting
implements Setting<Set<class_1299<?>>> {
    private Consumer<Set<class_1299<?>>> onChanged;

    public EntityTypeListSetting(String name, class_1299<?> ... defaultValues) {
        super(name, EntityTypeListSetting.idsOf(defaultValues));
    }

    @Override
    public Set<class_1299<?>> get() {
        LinkedHashSet result = new LinkedHashSet();
        String value = this.getValue();
        if (value == null || value.isEmpty()) {
            return result;
        }
        for (String part : value.split(",")) {
            class_1299 type;
            class_2960 id = class_2960.method_12829((String)part.trim());
            if (id == null || !class_7923.field_41177.method_10250(id) || (type = (class_1299)class_7923.field_41177.method_63535(id)) == null) continue;
            result.add(type);
        }
        return result;
    }

    @Override
    public void set(Set<class_1299<?>> value) {
        if (value != null) {
            this.setValue(EntityTypeListSetting.idsOf(value.toArray(new class_1299[0])));
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        this.fire();
    }

    private void fire() {
        if (this.onChanged != null) {
            this.onChanged.accept((Set<class_1299<?>>)this.get());
        }
    }

    private static String idsOf(class_1299<?> ... types) {
        StringBuilder builder = new StringBuilder();
        for (class_1299<?> type : types) {
            class_2960 id = class_7923.field_41177.method_10221(type);
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
        private class_1299<?>[] defaultValues = new class_1299[0];
        private Consumer<Set<class_1299<?>>> onChanged;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(class_1299<?> ... defaultValues) {
            this.defaultValues = defaultValues;
            return this;
        }

        public Builder visible(Supplier<Boolean> visible) {
            return this;
        }

        public Builder onChanged(Consumer<Set<class_1299<?>>> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public EntityTypeListSetting build() {
            EntityTypeListSetting setting = new EntityTypeListSetting(this.name, this.defaultValues);
            setting.onChanged = this.onChanged;
            if (!this.description.isEmpty()) {
                setting.setDescription(this.description);
            }
            return setting;
        }
    }
}

