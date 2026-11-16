package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BindSetting;
import hook.module.setting.BooleanSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;

/**
 * Quick Utilities: hold the wheel key and a radial menu opens around the centre of the screen. Move the mouse toward a
 * slot and let go of the key (or click) to open that utility. Utilities live in {@link QuickWheelScreen#SLOTS}.
 */
public final class QuickUtilitiesModule extends Module {
    static QuickUtilitiesModule instance;
    final BindSetting wheelKey = new BindSetting(EncryptedString.of("Wheel Key"), 66, false); // B
    final BooleanSetting sounds = new BooleanSetting(EncryptedString.of("Sounds"), true);
    final BooleanSetting blur = new BooleanSetting(EncryptedString.of("Blur"), true);
    final BooleanSetting releaseToSelect = new BooleanSetting(EncryptedString.of("Release To Select"), true);
    private boolean wasDown;

    public QuickUtilitiesModule() {
        super(EncryptedString.of("Quick Utilities"), EncryptedString.of("Hold the wheel key to open a radial menu with quick utilities (Calculator, more later)."), -1, Category.MISC);
        this.wheelKey.setDescription(EncryptedString.of("Hold this key to open the wheel"));
        this.sounds.setDescription(EncryptedString.of("Plays the client's UI sounds on open, hover and select"));
        this.blur.setDescription(EncryptedString.of("Blurs the game behind the wheel"));
        this.releaseToSelect.setDescription(EncryptedString.of("Letting go of the key opens the hovered utility (like an emote wheel). Off: click to select"));
        this.addSettings(this.wheelKey, this.releaseToSelect, this.blur, this.sounds);
        instance = this;
    }

    static boolean keyDown() {
        QuickUtilitiesModule m = instance;
        if (m == null) return false;
        int key = m.wheelKey.getValue();
        if (key <= 0) return false;
        try {
            return class_3675.method_15987(class_310.method_1551().method_22683(), key);
        } catch (Throwable t) {
            return false;
        }
    }

    void cue(UiAudio.Cue cue) {
        if (this.sounds.getValue()) UiAudio.play(cue);
    }

    @EventListener
    public void onTick(TickEvent event) {
        class_310 mc = class_310.method_1551();
        boolean down = keyDown();
        // open on the press edge, only while playing (no other screen / chat open)
        if (down && !this.wasDown && mc.field_1724 != null && mc.field_1755 == null) {
            mc.method_1507((class_437) new QuickWheelScreen());
        }
        this.wasDown = down;
    }
}
