/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.compat.meteorclient.settings.SettingGroup;
import hook.module.Module;
import java.util.ArrayList;
import java.util.List;

public final class SettingGroups {
    private final Module owner;
    private final List<Setting<?>> all = new ArrayList();

    public SettingGroups(Module owner) {
        this.owner = owner;
    }

    public SettingGroup getDefaultGroup() {
        return new SettingGroup(this, "general");
    }

    public SettingGroup createGroup(String name) {
        return new SettingGroup(this, name);
    }

    void register(Setting<?> setting) {
        this.all.add(setting);
        if (setting instanceof hook.module.setting.Setting) {
            hook.module.setting.Setting base = (hook.module.setting.Setting)((Object)setting);
            this.owner.addSetting(base);
        }
    }

    public <T> Setting<T> get(String name, Class<T> type) {
        for (Setting<?> setting : this.all) {
            Object value;
            hook.module.setting.Setting base;
            if (!(setting instanceof hook.module.setting.Setting) || !name.contentEquals((base = (hook.module.setting.Setting)((Object)setting)).getName()) || (value = setting.get()) == null || !type.isInstance(value)) continue;
            return (Setting<T>)setting;
        }
        return null;
    }
}

