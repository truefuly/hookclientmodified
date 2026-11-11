/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 */
package hook;

import hook.feature.HookClickGuiScreen;
import hook.manager.EventManager;
import hook.manager.SettingsManager;
import hook.module.ModuleManager;
import java.io.File;
import net.minecraft.class_310;
import net.minecraft.class_437;

public final class Hook {
    public SettingsManager settingsManager;
    public ModuleManager MODULE_MANAGER;
    public EventManager EVENT_BUS;
    public static class_310 mc;
    public String version;
    public static Hook INSTANCE;
    public boolean shouldPreventClose;
    public HookClickGuiScreen GUI;
    public class_437 screen;
    public long modified;
    public File jar;

    public Hook() {
        try {
            INSTANCE = this;
            this.version = " b1.3";
            this.screen = null;
            this.EVENT_BUS = new EventManager();
            this.MODULE_MANAGER = new ModuleManager();
            this.GUI = new HookClickGuiScreen();
            this.settingsManager = new SettingsManager();
            this.getSettingsManager().loadProfile();
            this.jar = new File(Hook.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            this.modified = this.jar.lastModified();
            this.shouldPreventClose = false;
            mc = class_310.method_1551();
        }
        catch (Throwable var2) {
            var2.printStackTrace(System.err);
        }
    }

    public SettingsManager getSettingsManager() {
        return this.settingsManager;
    }

    public ModuleManager getModuleManager() {
        return this.MODULE_MANAGER;
    }

    public EventManager getEventBus() {
        return this.EVENT_BUS;
    }

    public void resetModifiedDate() {
        this.jar.setLastModified(this.modified);
    }
}

