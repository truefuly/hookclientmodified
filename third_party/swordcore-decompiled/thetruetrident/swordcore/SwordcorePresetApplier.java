/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package thetruetrident.swordcore;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import thetruetrident.swordcore.SwordcoreClient;
import thetruetrident.swordcore.SwordcoreConfig;

@Environment(value=EnvType.CLIENT)
public final class SwordcorePresetApplier {
    private SwordcorePresetApplier() {
    }

    public static void applyPreset(SwordcoreConfig.Preset preset) {
        SwordcoreConfig config = SwordcoreClient.swordConfig;
        if (config == null) {
            return;
        }
        SwordcorePresetApplier.applyBase(config);
        switch (preset) {
            case PASSIVE: {
                SwordcorePresetApplier.applyPassive(config);
                break;
            }
            case AGGRESSIVE: {
                SwordcorePresetApplier.applyAggressive(config);
                break;
            }
            case PASSIVE_PLUS_PLUS: {
                SwordcorePresetApplier.applyPassivePlusPlus(config);
                break;
            }
            case LIGHT: {
                SwordcorePresetApplier.applyLight(config);
                break;
            }
        }
        config.preset = preset == SwordcoreConfig.Preset.CUSTOM ? SwordcoreConfig.Preset.ADAPTABLE : preset;
        config.save();
    }

    public static void refreshPresetFromCurrent() {
        SwordcoreConfig config = SwordcoreClient.swordConfig;
        if (config == null) {
            return;
        }
        config.preset = SwordcorePresetApplier.detectPreset(config);
    }

    private static SwordcoreConfig.Preset detectPreset(SwordcoreConfig config) {
        for (SwordcoreConfig.Preset preset : SwordcoreConfig.Preset.values()) {
            if (preset == SwordcoreConfig.Preset.CUSTOM || !SwordcorePresetApplier.matchesPreset(config, preset)) continue;
            return preset;
        }
        return SwordcoreConfig.Preset.CUSTOM;
    }

    private static boolean matchesPreset(SwordcoreConfig config, SwordcoreConfig.Preset preset) {
        SwordcoreConfig expected = new SwordcoreConfig();
        expected.preset = preset;
        switch (preset) {
            case PASSIVE: {
                SwordcorePresetApplier.applyPassive(expected);
                break;
            }
            case AGGRESSIVE: {
                SwordcorePresetApplier.applyAggressive(expected);
                break;
            }
            case PASSIVE_PLUS_PLUS: {
                SwordcorePresetApplier.applyPassivePlusPlus(expected);
                break;
            }
            case LIGHT: {
                SwordcorePresetApplier.applyLight(expected);
                break;
            }
        }
        return config.enabled == expected.enabled && config.chatLogs == expected.chatLogs && config.visualEffects == expected.visualEffects && config.visualTinting == expected.visualTinting && config.soundEffects == expected.soundEffects && config.shieldDisable == expected.shieldDisable && config.shieldDisableMode == expected.shieldDisableMode && config.movementSyncing == expected.movementSyncing && config.jumpFollowingMode == expected.jumpFollowingMode && config.jumpFollowingRespectsResist == expected.jumpFollowingRespectsResist && config.airGate == expected.airGate && config.preventBackDuringAggressiveJump == expected.preventBackDuringAggressiveJump && config.neutralMode == expected.neutralMode && config.sTapping == expected.sTapping && config.sTapLength == expected.sTapLength && config.movementResisting == expected.movementResisting && config.movementResistEscape == expected.movementResistEscape && config.sprintHitFollowThrough == expected.sprintHitFollowThrough && config.sprintHitJumpDelay == expected.sprintHitJumpDelay && SwordcorePresetApplier.same(config.movementResistingRange, expected.movementResistingRange) && SwordcorePresetApplier.same(config.movementSyncInnerRange, expected.movementSyncInnerRange) && config.autoWTapAutosprint == expected.autoWTapAutosprint && config.itemGate == expected.itemGate && config.movementEqualizer == expected.movementEqualizer && SwordcorePresetApplier.same(config.movementEqualizerRange, expected.movementEqualizerRange) && config.comboCounter == expected.comboCounter && config.smartSword == expected.smartSword && config.freeHitting == expected.freeHitting && config.addedPing == expected.addedPing && config.jumpResetting == expected.jumpResetting && SwordcorePresetApplier.same(config.targetRange, expected.targetRange);
    }

    private static void applyBase(SwordcoreConfig config) {
        SwordcoreConfig defaults = new SwordcoreConfig();
        config.enabled = defaults.enabled;
        config.chatLogs = defaults.chatLogs;
        config.visualEffects = defaults.visualEffects;
        config.visualTinting = defaults.visualTinting;
        config.soundEffects = defaults.soundEffects;
        config.shieldDisable = defaults.shieldDisable;
        config.shieldDisableMode = defaults.shieldDisableMode;
        config.movementSyncing = defaults.movementSyncing;
        config.jumpFollowingMode = defaults.jumpFollowingMode;
        config.jumpFollowingRespectsResist = defaults.jumpFollowingRespectsResist;
        config.airGate = defaults.airGate;
        config.preventBackDuringAggressiveJump = defaults.preventBackDuringAggressiveJump;
        config.neutralMode = defaults.neutralMode;
        config.sTapping = defaults.sTapping;
        config.sTapLength = defaults.sTapLength;
        config.movementResisting = defaults.movementResisting;
        config.movementResistEscape = defaults.movementResistEscape;
        config.sprintHitFollowThrough = defaults.sprintHitFollowThrough;
        config.sprintHitJumpDelay = defaults.sprintHitJumpDelay;
        config.movementResistingRange = defaults.movementResistingRange;
        config.movementSyncInnerRange = defaults.movementSyncInnerRange;
        config.autoWTapAutosprint = defaults.autoWTapAutosprint;
        config.itemGate = defaults.itemGate;
        config.movementEqualizer = defaults.movementEqualizer;
        config.movementEqualizerRange = defaults.movementEqualizerRange;
        config.comboCounter = defaults.comboCounter;
        config.smartSword = defaults.smartSword;
        config.freeHitting = defaults.freeHitting;
        config.addedPing = defaults.addedPing;
        config.jumpResetting = defaults.jumpResetting;
        config.targetRange = defaults.targetRange;
    }

    private static void applyPassive(SwordcoreConfig config) {
        config.neutralMode = SwordcoreConfig.NeutralMode.PASSIVE;
        config.movementEqualizer = SwordcoreConfig.MovementEqualizerMode.EQUALIZER;
        config.movementSyncInnerRange = 3.0f;
        config.jumpFollowingMode = SwordcoreConfig.JumpFollowingMode.OFF;
    }

    private static void applyAggressive(SwordcoreConfig config) {
        config.neutralMode = SwordcoreConfig.NeutralMode.AGGRESSIVE;
        config.jumpFollowingMode = SwordcoreConfig.JumpFollowingMode.ON;
        config.airGate = false;
        config.movementEqualizer = SwordcoreConfig.MovementEqualizerMode.EQUALIZER_JUMP;
    }

    private static void applyPassivePlusPlus(SwordcoreConfig config) {
        SwordcorePresetApplier.applyPassive(config);
        config.movementResistingRange = 6.0f;
        config.movementEqualizer = SwordcoreConfig.MovementEqualizerMode.EQUALIZER;
    }

    private static void applyLight(SwordcoreConfig config) {
        config.movementSyncing = false;
        config.movementResisting = false;
    }

    private static boolean same(float left, float right) {
        return Math.abs(left - right) < 1.0E-4f;
    }
}

