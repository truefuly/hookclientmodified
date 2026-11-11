/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.halloween.Halloween;
import hook.module.Category;
import hook.module.Module;
import hook.module.NewFeature;
import hook.module.setting.ButtonSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;

public final class HalloweenModule
extends Module
implements NewFeature {
    public static final NumberSetting darkness = new NumberSetting(EncryptedString.of("Halloween Darkness"), 0.0, 100.0, 60.0, 1.0).getValue(EncryptedString.of("How dark the Halloween night gets (fullbright is overridden)"));
    public static final NumberSetting fogDensity = new NumberSetting(EncryptedString.of("Fog Density"), 0.0, 100.0, 55.0, 1.0).getValue(EncryptedString.of("How thick the Halloween fog and ground mist are"));
    public static final NumberSetting eventVolume = new NumberSetting(EncryptedString.of("Event Music Volume"), 0.0, 100.0, 60.0, 1.0).getValue(EncryptedString.of("Volume of the Halloween event song"));
    private final ButtonSetting replay = new ButtonSetting(EncryptedString.of("Replay Intro"), Halloween::replayIntro);

    public HalloweenModule() {
        super(EncryptedString.of("Halloween"), EncryptedString.of("A HookVisuals Halloween night: darkness, fog, a haunted sky and a visitor on a broom"), -1, Category.VISUALS);
        this.replay.setDescription(EncryptedString.of("Plays the Halloween welcome event again"));
        this.addSettings(darkness, fogDensity, eventVolume, this.replay);
    }

    @Override
    public void onEnable() {
        Halloween.onModuleEnabled();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Halloween.onModuleDisabled();
        super.onDisable();
    }
}

