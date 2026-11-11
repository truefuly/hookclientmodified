/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3944
 *  net.minecraft.class_437
 *  net.minecraft.class_490
 */
package hook.module.modules.client;

import hook.event.EventListener;
import hook.event.events.PacketReceiveEvent;
import hook.event.events.TickEvent;
import hook.feature.HookClickGuiScreen;
import hook.feature.UiPalette;
import hook.halloween.Halloween;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_3944;
import net.minecraft.class_437;
import net.minecraft.class_490;

public final class Hook
extends Module {
    public static final ModeSetting<UiPalette> theme = new ModeSetting<UiPalette>(EncryptedString.of("Theme"), UiPalette.Hook, UiPalette.class).setDescription(EncryptedString.of("Accent, gradient and lighting colors of the client"));
    public static final BooleanSetting uiSounds = new BooleanSetting(EncryptedString.of("UI Sounds"), true).setDescription(EncryptedString.of("Soft sounds for clicks, toggles and menus"));
    public static final NumberSetting uiVolume = new NumberSetting(EncryptedString.of("UI Volume"), 0.0, 100.0, 70.0, 1.0).getValue(EncryptedString.of("Volume of the interface sounds in percent"));
    public static final BooleanSetting halloweenMusic = new BooleanSetting(EncryptedString.of("Halloween Music"), true).setDescription(EncryptedString.of("Halloween song in the menu (Halloween theme)"));
    public static final NumberSetting halloweenVolume = new NumberSetting(EncryptedString.of("Halloween Music Volume"), 0.0, 100.0, 35.0, 1.0).getValue(EncryptedString.of("How loud the Halloween menu music is"));
    public static final BooleanSetting beatShake = new BooleanSetting(EncryptedString.of("Beat Shake"), true).setDescription(EncryptedString.of("The backdrop moves gently with the beat"));
    public static final NumberSetting shakeStrength = new NumberSetting(EncryptedString.of("Beat Shake Strength"), 0.0, 100.0, 50.0, 5.0).getValue(EncryptedString.of("How strongly the background reacts to the beat"));
    public static final NumberSetting guiSize = new NumberSetting(EncryptedString.of("GUI Size"), 70.0, 140.0, 100.0, 5.0).getValue(EncryptedString.of("Scale of the HookVisuals menu in percent"));
    private final BooleanSetting preventClose = new BooleanSetting(EncryptedString.of("Prevent Close"), true).setDescription(EncryptedString.of("For servers with freeze plugins that don't let you open the GUI"));
    public static final ModeSetting<hook.feature.LobbyTheme> lobbyTheme = new ModeSetting<hook.feature.LobbyTheme>(EncryptedString.of("Lobby Theme"), hook.feature.LobbyTheme.OverworldCastle, hook.feature.LobbyTheme.class).setDescription(EncryptedString.of("Background and buttons of the menus; Standard keeps vanilla"));
    public boolean shouldPreventClose;

    public Hook() {
        super(EncryptedString.of("HookVisuals"), EncryptedString.of("Settings for the client"), 344, Category.CLIENT);
        this.addSettings(theme, uiSounds, uiVolume, halloweenMusic, halloweenVolume, beatShake, shakeStrength, guiSize, this.preventClose, lobbyTheme);
    }

    @Override
    public void onEnable() {
        if (Halloween.guiLocked()) {
            this.setEnabled(false);
            Halloween.lockedNotice();
        } else {
            if (!(this.mc.field_1755 instanceof HookClickGuiScreen)) {
                hook.Hook.INSTANCE.screen = this.mc.field_1755;
            }
            if (hook.Hook.INSTANCE.GUI != null) {
                hook.Hook.INSTANCE.GUI.prepareOpen();
                this.mc.method_29970((class_437)hook.Hook.INSTANCE.GUI);
            } else if (this.mc.field_1724 != null && this.mc.field_1755 instanceof class_490) {
                this.shouldPreventClose = true;
            }
            super.onEnable();
        }
    }

    @Override
    public void onDisable() {
        if (this.mc.field_1755 instanceof HookClickGuiScreen) {
            hook.Hook.INSTANCE.GUI.requestClose();
        } else if (this.mc.field_1755 instanceof class_490) {
            this.shouldPreventClose = false;
        }
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (hook.Hook.INSTANCE.GUI != null) {
            hook.Hook.INSTANCE.GUI.syncAmbience();
        }
    }

    @EventListener
    public void onPacketReceive(PacketReceiveEvent var1) {
        if (this.shouldPreventClose && var1.packet instanceof class_3944 && this.preventClose.getValue()) {
            var1.cancel();
        }
    }

    /** Rebuilt as a real enum. The constant names match the original ("Normal", ...) so saved settings still load. */
    public enum AnimationMode {
        Normal,
        Positive,
        Off;

        public static final AnimationMode NORMAL = Normal;
        public static final AnimationMode POSITIVE = Positive;
        public static final AnimationMode OFF = Off;
    }
}

