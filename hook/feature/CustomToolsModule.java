/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.itemfx.ItemFx;
import hook.module.Category;
import hook.module.Module;
import hook.module.NewFeature;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;

public final class CustomToolsModule
extends Module
implements NewFeature {
    public static final ModeSetting<ToolDesign> style = new ModeSetting<ToolDesign>(EncryptedString.of("Style"), ToolDesign.Ghost, ToolDesign.class).setDescription(EncryptedString.of("Ghost: see-through haunted diamond / netherite pickaxe. 3D: sculpted tools (all tools)"));
    public static final BooleanSetting animations = new BooleanSetting(EncryptedString.of("Animations"), true).setDescription(EncryptedString.of("A slow sheen across metal heads and living glows"));
    public static final BooleanSetting effects = new BooleanSetting(EncryptedString.of("Effects"), true).setDescription(EncryptedString.of("Flying skulls, ghost wisps, sparkles and embers"));
    public static final NumberSetting strength = new NumberSetting(EncryptedString.of("Effect Strength"), 0.0, 100.0, 70.0, 1.0).getValue(EncryptedString.of("How bright the glows and particles are"));
    private static CustomToolsModule instance;

    public CustomToolsModule() {
        super(EncryptedString.of("Custom Tools"), EncryptedString.of("Ghost pickaxes with flying skulls, or sculpted 3D tools"), -1, Category.VISUALS);
        this.addSettings(style, animations, effects, strength);
        instance = this;
    }

    public static boolean active() {
        return instance != null && instance.isEnabled();
    }

    public static int styleIndex() {
        return style.getModeIndex();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ItemFx.moduleOff();
    }

    public static enum ToolDesign {
        Ghost,
        Forged_3D,
        Spectral_3D;

    }
}

