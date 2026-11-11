/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.module.setting.StringSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringListSetting
extends StringSetting
implements Setting<List<String>> {
    private Consumer<List<String>> onChanged;

    public StringListSetting(String name, String ... defaultValues) {
        super(name, StringListSetting.join(defaultValues));
    }

    @Override
    public List<String> get() {
        String value = this.getValue();
        ArrayList<String> result = new ArrayList<String>();
        if (value == null || value.isEmpty()) {
            return result;
        }
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            result.add(trimmed);
        }
        return result;
    }

    @Override
    public void set(List<String> value) {
        if (value != null) {
            this.setValue(StringListSetting.join(value.toArray(new String[0])));
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        this.fire();
    }

    private void fire() {
        if (this.onChanged != null) {
            this.onChanged.accept((List<String>)this.get());
        }
    }

    private static String join(String[] values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.isEmpty()) continue;
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(value);
        }
        return builder.toString();
    }

    public static class Builder {
        private String name = "";
        private String description = "";
        private String[] defaultValues = new String[0];
        private Consumer<List<String>> onChanged;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(String ... defaultValues) {
            this.defaultValues = defaultValues;
            return this;
        }

        public Builder visible(Supplier<Boolean> visible) {
            return this;
        }

        public Builder onChanged(Consumer<List<String>> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public StringListSetting build() {
            StringListSetting setting = new StringListSetting(this.name, this.defaultValues);
            setting.onChanged = this.onChanged;
            if (!this.description.isEmpty()) {
                setting.setDescription(this.description);
            }
            return setting;
        }
    }
}

