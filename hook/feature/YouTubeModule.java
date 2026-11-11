/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ButtonSetting;
import hook.module.setting.StringSetting;
import hook.utils.BrowserWindow;
import hook.utils.EncryptedString;

public final class YouTubeModule
extends Module {
    private final StringSetting url = new StringSetting(EncryptedString.of("URL"), "https://music.youtube.com").setDescription(EncryptedString.of("Music service link or any other http(s) address"));
    private final BooleanSetting openOnEnable = new BooleanSetting(EncryptedString.of("Open on Enable"), true).setDescription(EncryptedString.of("Opens the window when the module is enabled"));
    private final BooleanSetting closeOnDisable = new BooleanSetting(EncryptedString.of("Close on Disable"), true).setDescription(EncryptedString.of("Closes the window when the module is disabled"));
    private final ButtonSetting openNow = new ButtonSetting(EncryptedString.of("Open Now"), () -> BrowserWindow.open(this.url.getValue()));

    public YouTubeModule() {
        super(EncryptedString.of("Music Browser"), EncryptedString.of("Opens the music service in your browser on Windows or Linux."), 71, Category.VISUALS);
        this.openNow.setDescription(EncryptedString.of("Opens the configured browser URL immediately"));
        this.addSettings(this.url, this.openOnEnable, this.closeOnDisable, this.openNow);
    }

    @Override
    public void onEnable() {
        if (this.openOnEnable.getValue()) {
            BrowserWindow.open(this.url.getValue());
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (this.closeOnDisable.getValue()) {
            BrowserWindow.close();
        }
        super.onDisable();
    }
}

