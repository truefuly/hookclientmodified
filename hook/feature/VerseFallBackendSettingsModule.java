package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.ChoiceSetting;
import hook.module.setting.ButtonSetting;
import hook.utils.BrowserWindow;
import hook.utils.EncryptedString;

import java.util.List;

/** Client category settings for the music media backend. */
public final class VerseFallBackendSettingsModule extends Module {
    public final ChoiceSetting backend = new ChoiceSetting(EncryptedString.of("Media Backend"), "Windows", List.of("Windows", "Linux", "Browser"));
    private final ButtonSetting openMusic = new ButtonSetting(EncryptedString.of("Open Music"), () -> BrowserWindow.open("https://music.youtube.com"));

    public VerseFallBackendSettingsModule() {
        super(EncryptedString.of("Music"), EncryptedString.of("Music playback settings."), -1, Category.CLIENT);
        this.backend.setDescription(EncryptedString.of("Media source: Windows, Linux or browser."));
        this.openMusic.setDescription(EncryptedString.of("Opens the music service in your browser for track and lyrics info."));
        this.addSettings(this.backend, this.openMusic);
    }
}
