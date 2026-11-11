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

public final class CustomTotemModule
extends Module
implements NewFeature {
    public static final ModeSetting<TotemDesign> design = new ModeSetting<TotemDesign>(EncryptedString.of("Design"), TotemDesign.Spooky, TotemDesign.class).setDescription(EncryptedString.of("Minecraft-style Spooky or Chrome Liquid, or the sculpted 3D versions"));
    public static final BooleanSetting animations = new BooleanSetting(EncryptedString.of("Animations"), true).setDescription(EncryptedString.of("Floating, swaying cloth, flowing chrome and drips"));
    public static final BooleanSetting particles = new BooleanSetting(EncryptedString.of("Particles"), true).setDescription(EncryptedString.of("Orbiting glow, embers, runes, smoke and glints around the totem"));
    public static final NumberSetting glow = new NumberSetting(EncryptedString.of("Glow Strength"), 0.0, 100.0, 75.0, 1.0).getValue(EncryptedString.of("How bright the glowing parts and their bloom are"));
    public static final BooleanSetting popEffects = new BooleanSetting(EncryptedString.of("Pop Effects"), true).setDescription(EncryptedString.of("The design's own burst when a totem pops (replaces the green particles)"));
    private static CustomTotemModule instance;

    public CustomTotemModule() {
        super(EncryptedString.of("Custom Totem"), EncryptedString.of("Halloween totem designs with animated glow shaders (Minecraft style or 3D)"), -1, Category.VISUALS);
        this.addSettings(design, animations, particles, glow, popEffects);
        instance = this;
    }

    public static boolean active() {
        return instance != null && instance.isEnabled();
    }

    public static int designIndex() {
        return design.getModeIndex();
    }

    public static boolean chromeActive() {
        return CustomTotemModule.active() && (CustomTotemModule.designIndex() == 1 || CustomTotemModule.designIndex() == 3) && animations.getValue();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ItemFx.moduleOff();
    }

    public static enum TotemDesign {
        Spooky,
        Chrome_Liquid,
        Spooky_3D,
        Chrome_Liquid_3D;

    }
}

