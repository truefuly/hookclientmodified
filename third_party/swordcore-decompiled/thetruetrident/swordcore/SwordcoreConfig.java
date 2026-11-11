/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.loader.api.FabricLoader
 */
package thetruetrident.swordcore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(value=EnvType.CLIENT)
public class SwordcoreConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("swordcore.json");
    public int configVersion = 37;
    public Preset preset = Preset.ADAPTABLE;
    public boolean enabled = true;
    public boolean chatLogs = true;
    public boolean visualEffects = true;
    public boolean visualTinting = false;
    public boolean soundEffects = true;
    public boolean shieldDisable = true;
    public ShieldDisableMode shieldDisableMode = ShieldDisableMode.NORMAL;
    public boolean movementSyncing = true;
    public JumpFollowingMode jumpFollowingMode = JumpFollowingMode.ON;
    @Deprecated
    public boolean jumpFollowing = false;
    public boolean jumpFollowingRespectsResist = true;
    @Deprecated
    public boolean jumpFollowingNeutralBased = true;
    public boolean airGate = true;
    public boolean preventBackDuringAggressiveJump = true;
    @Deprecated
    public boolean onlyPressWWhenSafe = true;
    public NeutralMode neutralMode = NeutralMode.ADAPTABLE;
    public boolean sTapping = true;
    public int sTapLength = 2;
    public boolean movementResisting = true;
    public boolean movementResistEscape = false;
    public boolean sprintHitFollowThrough = true;
    public int sprintHitJumpDelay = 2;
    public float movementResistingRange = 3.6f;
    public float movementSyncInnerRange = 2.0f;
    public boolean autoWTapAutosprint = true;
    public boolean itemGate = true;
    public MovementEqualizerMode movementEqualizer = MovementEqualizerMode.ADAPTABLE_EXPERIMENTAL;
    public float movementEqualizerRange = 1.0f;
    public boolean comboCounter = false;
    public boolean smartSword = true;
    public boolean freeHitting = false;
    public int addedPing = 0;
    public boolean jumpResetting = true;
    public float targetRange = 8.0f;

    public static SwordcoreConfig load() {
        SwordcoreConfig swordcoreConfig;
        block10: {
            if (Files.notExists(CONFIG_PATH, new LinkOption[0])) {
                SwordcoreConfig config = new SwordcoreConfig();
                config.save();
                return config;
            }
            BufferedReader reader = Files.newBufferedReader(CONFIG_PATH);
            try {
                SwordcoreConfig config = (SwordcoreConfig)GSON.fromJson((Reader)reader, SwordcoreConfig.class);
                if (config == null) {
                    config = new SwordcoreConfig();
                }
                config.ensureValid();
                swordcoreConfig = config;
                if (reader == null) break block10;
            }
            catch (Throwable throwable) {
                try {
                    if (reader != null) {
                        try {
                            ((Reader)reader).close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException | RuntimeException ignored) {
                    return new SwordcoreConfig();
                }
            }
            ((Reader)reader).close();
        }
        return swordcoreConfig;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent(), new FileAttribute[0]);
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH, new OpenOption[0]);){
                GSON.toJson((Object)this, (Appendable)writer);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private void ensureValid() {
        if (this.configVersion < 2 && this.movementResistingRange == 4.1f) {
            this.movementResistingRange = 3.4f;
        }
        if (this.configVersion < 3) {
            if (this.movementResistingRange == 3.4f) {
                this.movementResistingRange = 3.6f;
            }
            if (this.movementSyncInnerRange == 3.2f) {
                this.movementSyncInnerRange = 3.3f;
            }
        }
        if (this.configVersion < 4 && this.movementSyncInnerRange == 3.3f) {
            this.movementSyncInnerRange = 3.0f;
        }
        if (this.configVersion < 5 && this.neutralMode == NeutralMode.PASSIVE && this.onlyPressWWhenSafe) {
            this.neutralMode = NeutralMode.AGGRESSIVE;
        }
        if (this.configVersion < 9) {
            this.freeHitting = false;
        }
        if (this.configVersion < 16 && this.movementSyncInnerRange == 3.0f) {
            this.movementSyncInnerRange = 2.0f;
        }
        if (this.configVersion < 17 && this.neutralMode == NeutralMode.AGGRESSIVE) {
            this.neutralMode = NeutralMode.ADAPTABLE;
        }
        if (this.configVersion < 24) {
            JumpFollowingMode jumpFollowingMode = this.jumpFollowing ? (this.jumpFollowingNeutralBased ? JumpFollowingMode.NEUTRAL_BASED : JumpFollowingMode.ON) : (this.jumpFollowingMode = JumpFollowingMode.OFF);
        }
        if (this.configVersion < 32 && this.preset == Preset.ADAPTABLE && this.movementEqualizer == MovementEqualizerMode.EQUALIZER) {
            this.movementEqualizer = MovementEqualizerMode.ADAPTABLE_EXPERIMENTAL;
        }
        if (this.configVersion < 33) {
            if (this.preset != Preset.CUSTOM) {
                this.sTapping = true;
            }
            if (this.movementResistingRange == 3.5f) {
                this.movementResistingRange = 3.6f;
            }
        }
        if (this.configVersion < 34 && (this.preset == Preset.ADAPTABLE || this.preset == Preset.LIGHT) && this.jumpFollowingMode == JumpFollowingMode.OFF) {
            this.jumpFollowingMode = JumpFollowingMode.ON;
        }
        this.configVersion = 37;
        if (this.preset == null) {
            this.preset = Preset.ADAPTABLE;
        }
        if (this.shieldDisableMode == null) {
            this.shieldDisableMode = ShieldDisableMode.NORMAL;
        }
        if (this.jumpFollowingMode == null) {
            this.jumpFollowingMode = JumpFollowingMode.ON;
        }
        if (this.neutralMode == null) {
            this.neutralMode = NeutralMode.ADAPTABLE;
        }
        if (this.movementEqualizer == null) {
            this.movementEqualizer = MovementEqualizerMode.EQUALIZER;
        }
        this.movementResistingRange = Math.max(0.0f, this.movementResistingRange);
        this.movementSyncInnerRange = Math.max(0.0f, this.movementSyncInnerRange);
        this.movementEqualizerRange = Math.max(0.0f, this.movementEqualizerRange);
        this.sprintHitJumpDelay = Math.max(0, this.sprintHitJumpDelay);
        this.sTapLength = Math.max(0, this.sTapLength);
        this.addedPing = Math.max(0, this.addedPing);
        this.targetRange = Math.max(1.0f, this.targetRange);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Preset {
        ADAPTABLE,
        LIGHT,
        PASSIVE,
        AGGRESSIVE,
        PASSIVE_PLUS_PLUS,
        CUSTOM;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum ShieldDisableMode {
        NORMAL,
        STUN,
        STUN_SNEAK;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum JumpFollowingMode {
        OFF,
        ON,
        NEUTRAL_BASED;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum NeutralMode {
        PASSIVE,
        AGGRESSIVE,
        ADAPTABLE,
        ADAPTABLE_INVERSE;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum MovementEqualizerMode {
        OFF,
        JUMP,
        EQUALIZER_JUMP,
        ADAPTABLE_EXPERIMENTAL,
        EQUALIZER;

    }
}

