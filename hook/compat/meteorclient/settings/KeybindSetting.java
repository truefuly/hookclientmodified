/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.compat.meteorclient.utils.misc.Keybind;
import hook.module.setting.BindSetting;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KeybindSetting
extends BindSetting
implements Setting<Keybind> {
    static final List<KeybindSetting> ALL = new CopyOnWriteArrayList<KeybindSetting>();
    private Runnable action;
    private Consumer<Keybind> onChanged;

    public KeybindSetting(String name, Keybind defaultValue, Runnable action) {
        super(name, defaultValue == null ? -1 : defaultValue.getKey(), false);
        this.action = action;
        ALL.add(this);
    }

    @Override
    public Keybind get() {
        return new Keybind(this.getValue());
    }

    @Override
    public void set(Keybind value) {
        if (value != null) {
            this.setValue(value.getKey());
        }
    }

    @Override
    public void setValue(int key) {
        super.setValue(key);
        this.fire();
    }

    public void runAction() {
        if (this.action != null) {
            this.action.run();
        }
    }

    public static void handleKeyPress(int key) {
        if (key < 0) {
            return;
        }
        for (KeybindSetting setting : ALL) {
            if (setting.getValue() != key || setting.action == null) continue;
            try {
                setting.action.run();
            }
            catch (Throwable t) {
                System.err.println("Hook: keybind action failed");
                t.printStackTrace(System.err);
            }
        }
    }

    private void fire() {
        if (this.onChanged != null) {
            this.onChanged.accept(this.get());
        }
    }

    public static class Builder {
        private String name = "";
        private String description = "";
        private Keybind defaultValue = Keybind.none();
        private Runnable action;
        private Consumer<Keybind> onChanged;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(Keybind defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder action(Runnable action) {
            this.action = action;
            return this;
        }

        public Builder visible(Supplier<Boolean> visible) {
            return this;
        }

        public Builder onChanged(Consumer<Keybind> onChanged) {
            this.onChanged = onChanged;
            return this;
        }

        public KeybindSetting build() {
            KeybindSetting setting = new KeybindSetting(this.name, this.defaultValue, this.action);
            setting.onChanged = this.onChanged;
            if (!this.description.isEmpty()) {
                setting.setDescription(this.description);
            }
            return setting;
        }
    }
}

