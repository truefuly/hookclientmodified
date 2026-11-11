/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;

public final class SwingSpeedModule
extends Module {
    public static SwingSpeedModule INSTANCE;
    private final NumberSetting level = new NumberSetting(EncryptedString.of("Swing Speed"), 1.0, 10.0, 5.0, 1.0);

    public SwingSpeedModule() {
        super(EncryptedString.of("Swing Speed"), EncryptedString.of("Adjusts how quickly the held item swing animation plays"), -1, Category.MISC);
        this.level.getValue(EncryptedString.of("1 = very fast, 10 = very slow"));
        this.addSettings(this.level);
        INSTANCE = this;
    }

    public int getLevel() {
        return this.level.getIntValue();
    }

    public static float adjust(float progress) {
        SwingSpeedModule module = INSTANCE;
        if (module == null || !module.isEnabled()) {
            return progress;
        }
        int level = Math.max(1, Math.min(10, module.getLevel()));
        float exponent = 0.25f + (float)(level - 1) * 0.30555555f;
        float clampedProgress = Math.min(1.0f, Math.max(0.0f, progress));
        return (float)Math.pow(clampedProgress, exponent);
    }

    public static int adjustDuration(int duration) {
        SwingSpeedModule module = INSTANCE;
        if (module == null || !module.isEnabled()) {
            return duration;
        }
        int level = Math.max(1, Math.min(10, module.getLevel()));
        float exponent = 0.25f + (float)(level - 1) * 0.30555555f;
        return Math.max(1, Math.round((float)duration * exponent));
    }
}

