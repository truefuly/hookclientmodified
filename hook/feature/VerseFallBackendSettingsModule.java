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
        super(EncryptedString.of("Musik"), EncryptedString.of("Einstellungen für die Musikwiedergabe."), -1, Category.CLIENT);
        this.backend.setDescription(EncryptedString.of("Wähle die Medienquelle für Windows, Linux oder den Browser."));
        this.openMusic.setDescription(EncryptedString.of("Öffnet den Musikdienst im Browser für Titel- und Textanzeige."));
        this.addSettings(this.backend, this.openMusic);
    }
}
