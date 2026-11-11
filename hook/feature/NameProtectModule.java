/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.Hook;
import hook.module.Category;
import hook.module.Module;
import hook.module.ModuleManager;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;

public class NameProtectModule
extends Module {
    private final StringSetting fakeName = new StringSetting("Fake Name", "Player");

    public NameProtectModule() {
        super(EncryptedString.of("Name Protect"), EncryptedString.of("Replaces your name with given one."), -1, Category.MISC);
        this.addSettings(this.fakeName);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public String getFakeName() {
        String value = this.fakeName.getValue();
        return value != null && !value.isBlank() ? value.trim() : "Player";
    }

    public static String displayName(String original) {
        if (original == null || original.isEmpty()) {
            return "";
        }
        Hook client = Hook.INSTANCE;
        if (client == null) {
            return original;
        }
        ModuleManager modules = client.getModuleManager();
        if (modules == null) {
            return original;
        }
        NameProtectModule nameProtect = (NameProtectModule)modules.getModuleByClass(NameProtectModule.class);
        return nameProtect != null && nameProtect.isEnabled() ? nameProtect.getFakeName() : original;
    }
}

