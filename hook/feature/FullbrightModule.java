/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;

public final class FullbrightModule
extends Module {
    public static final NumberSetting strength = new NumberSetting(EncryptedString.of("Strength"), 0.0, 100.0, 100.0, 1.0).getValue(EncryptedString.of("How much of the vanilla lightmap darkness is removed (100 = fully lit)"));
    private static FullbrightModule instance;

    public FullbrightModule() {
        super(EncryptedString.of("Fullbright"), EncryptedString.of("Renders every block at full brightness, day or night, without touching your gamma setting"), -1, Category.VISUALS);
        this.addSettings(strength);
        instance = this;
    }

    public static boolean active() {
        return instance != null && instance.isEnabled();
    }

    public static float strength() {
        if (!FullbrightModule.active()) {
            return 0.0f;
        }
        double value = strength.getValue();
        return (float)Math.max(0.0, Math.min(100.0, value)) / 100.0f;
    }
}

