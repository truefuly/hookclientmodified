/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.settings;

import hook.compat.meteorclient.settings.Setting;
import hook.compat.meteorclient.settings.SettingGroups;

public final class SettingGroup {
    private final SettingGroups owner;
    private final String name;

    SettingGroup(SettingGroups owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public <T> Setting<T> add(Setting<T> setting) {
        this.owner.register(setting);
        return setting;
    }
}

