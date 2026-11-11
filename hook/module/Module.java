/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package hook.module;

import hook.Hook;
import hook.manager.EventManager;
import hook.module.Category;
import hook.module.setting.Setting;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_310;

public abstract class Module
implements Serializable {
    private final List<Setting> settings = new ArrayList<Setting>();
    protected final EventManager EVENT_BUS = Hook.INSTANCE.getEventBus();
    protected class_310 mc = class_310.method_1551();
    private CharSequence name;
    private CharSequence description;
    private boolean enabled;
    private int keybind;
    private Category category;
    private final boolean i = false;

    public Module(CharSequence name, CharSequence description, int keybind, Category category) {
        this.name = name;
        this.description = description;
        this.enabled = false;
        this.keybind = keybind;
        this.category = category;
    }

    public void toggle() {
        if (!this.enabled) {
            // empty if block
        }
        boolean bl = this.enabled = !this.enabled;
        if (this.enabled) {
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

    public CharSequence getName() {
        return this.name;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public CharSequence getDescription() {
        return this.description;
    }

    public int getKeybind() {
        return this.keybind;
    }

    public Category getCategory() {
        return this.category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public void setDescription(CharSequence description) {
        this.description = description;
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    public List<Setting> getSettings() {
        return this.settings;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void addSetting(Setting setting) {
        this.settings.add(setting);
    }

    public void addSettings(Setting ... a) {
        this.settings.addAll(Arrays.asList(a));
    }

    public void toggle(boolean enabled) {
        if (enabled) {
            // empty if block
        }
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                this.onEnable();
            } else {
                this.onDisable();
            }
        }
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            // empty if block
        }
        this.enabled = enabled;
    }
}

