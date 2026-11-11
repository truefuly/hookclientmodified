/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.fzzyhmstrs.fzzy_config.api.ConfigApiJava
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_1041
 *  net.minecraft.class_1282
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1743
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1839
 *  net.minecraft.class_238
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_2394
 *  net.minecraft.class_2396
 *  net.minecraft.class_243
 *  net.minecraft.class_2558
 *  net.minecraft.class_2558$class_10608
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_3419
 *  net.minecraft.class_3532
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_3966
 *  net.minecraft.class_437
 *  net.minecraft.class_5250
 */
package thetruetrident.swordcore;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1041;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1839;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_243;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3532;
import net.minecraft.class_3675;
import net.minecraft.class_3966;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import thetruetrident.swordcore.MovementResistParticle;
import thetruetrident.swordcore.MovementSyncInnerParticle;
import thetruetrident.swordcore.SwordcoreConfig;
import thetruetrident.swordcore.SwordcoreConfigScreen;
import thetruetrident.swordcore.SwordcoreParticles;
import thetruetrident.swordcore.SwordcorePresetApplier;
import thetruetrident.swordcore.SwordcoreVariant;
import thetruetrident.swordcore.mixin.client.AttackInvoker;

@Environment(value=EnvType.CLIENT)
public class SwordcoreClient
implements ClientModInitializer {
    public static SwordcoreConfig swordConfig;
    public static boolean smartSwordActive;
    public static boolean swordcoreOwnsAttack;
    public static boolean macecoreSwapAssistRequested;
    public static boolean premiumFeaturesEnabled;
    private static final double MOVEMENT_RESIST_EDGE_MARGIN = 0.2;
    private static final long VISUAL_TINT_DURATION_NANOS = 650000000L;
    private static final String LOG_PREFIX = "\ue81b ";
    private static final int NON_PREMIUM_SMARTSWORD_HIT_LIMIT = 5;
    private static final int MACECORE_TARGET_IMPORT_TICKS = 5;
    private static class_304 openConfigKey;
    private static class_304 smartSwordKey;
    private static class_304 cycleJumpFollowingKey;
    private static class_304 cycleNeutralModeKey;
    private static class_304 toggleSTappingKey;
    private static class_304 toggleFreeHittingKey;
    private static class_304 toggleSwordcoreKey;
    private static class_304 increaseResistRangeKey;
    private static class_304 decreaseResistRangeKey;
    private static class_1657 targetPlayer;
    private static class_1657 pendingStunTarget;
    private static int lastAttackTick;
    private static int lastManualAttackIntentTick;
    private static int lastSwapTick;
    private static int pendingStunHitTick;
    private static int pendingSwordRestoreTick;
    private static int pendingSwordRestoreSlot;
    private static int sTapUntilTick;
    private static int comboCount;
    private static int lastComboHitTick;
    private static int sneakUntilTick;
    private static boolean previousAttackDown;
    private static float previousHealth;
    private static boolean forcedForward;
    private static boolean forcedBack;
    private static boolean forcedLeft;
    private static boolean forcedRight;
    private static boolean forcedSneak;
    private static boolean forcedJump;
    private static int queuedJumpResetUntilTick;
    private static boolean forcedJumpFollowing;
    private static boolean forcedSprint;
    private static boolean resistedForward;
    private static boolean resistedBack;
    private static boolean resistedLeft;
    private static boolean resistedRight;
    private static boolean movementResistFeedbackActive;
    private static boolean movementSyncInnerFeedbackActive;
    private static boolean adaptableEqualizerJumping;
    private static int adaptableEqualizerJumpDamageCount;
    private static int adaptableEqualizerNoJumpDamageCount;
    private static int adaptableEqualizerJumpUntilTick;
    private static int adaptableNeutralHabitScore;
    private static int lastAdaptableNeutralBehavior;
    private static boolean aggressiveMovementJumpActive;
    private static int aggressiveMovementJumpUntilTick;
    private static int sprintHitFollowUntilTick;
    private static int sprintHitFollowJumpTick;
    private static long visualTintStartNanos;
    private static int visualTintColor;
    private static boolean airGateFeedbackActive;
    private static boolean movementEqualizerFeedbackActive;
    private static int targetCooldownUntilTick;
    private static float lastTargetSwingProgress;
    private static boolean lastTargetHandSwinging;
    private static boolean lastTargetEatingOrDrinking;
    private static class_1657 lastCooldownTarget;
    private static class_1657 lastMovementSyncTarget;
    private static double lastMovementSyncTargetX;
    private static double lastMovementSyncTargetZ;
    private static boolean previousSmartSwordEnabled;
    private static int nonPremiumSmartSwordHits;
    private static boolean msgSent;
    private static Field macecoreTargetEntityField;
    private static Method macecoreCanRouteSwapMethod;
    private static boolean swordcoreSendingAttack;
    private static boolean attackInputPhase;
    private static class_1297 dispatchedAttackTarget;

    public void onInitializeClient() {
        swordConfig = SwordcoreConfig.load();
        SwordcoreClient.enforcePremiumFeatureLocks();
        SwordcorePresetApplier.refreshPresetFromCurrent();
        swordConfig.save();
        ParticleFactoryRegistry.getInstance().register((class_2396)SwordcoreParticles.MOVEMENT_RESIST, MovementResistParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register((class_2396)SwordcoreParticles.MOVEMENT_SYNC_INNER, MovementSyncInnerParticle.Factory::new);
        ConfigApiJava.registerScreenProvider((String)"swordcore", (id, scope) -> new SwordcoreConfigScreen(class_310.method_1551().field_1755));
        openConfigKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.swordcore.open_config", class_3675.class_307.field_1668, 74, class_304.class_11900.field_62556));
        smartSwordKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.swordcore.smart_sword", class_3675.class_307.field_1668, 82, class_304.class_11900.field_62556));
        cycleJumpFollowingKey = SwordcoreClient.registerMiscKey("key.swordcore.cycle_jump_following", -1);
        cycleNeutralModeKey = SwordcoreClient.registerMiscKey("key.swordcore.cycle_neutral_mode", -1);
        toggleSTappingKey = SwordcoreClient.registerMiscKey("key.swordcore.toggle_s_tapping", -1);
        toggleFreeHittingKey = SwordcoreClient.registerMiscKey("key.swordcore.toggle_free_hitting", -1);
        toggleSwordcoreKey = SwordcoreClient.registerMiscKey("key.swordcore.toggle_swordcore", -1);
        increaseResistRangeKey = SwordcoreClient.registerMiscKey("key.swordcore.increase_resist_range", 61);
        decreaseResistRangeKey = SwordcoreClient.registerMiscKey("key.swordcore.decrease_resist_range", 45);
        ClientTickEvents.START_CLIENT_TICK.register(SwordcoreClient::tick);
    }

    private static class_304 registerMiscKey(String translationKey, int defaultKey) {
        return KeyBindingHelper.registerKeyBinding((class_304)new class_304(translationKey, class_3675.class_307.field_1668, defaultKey, class_304.class_11900.field_62556));
    }

    private static void tick(class_310 client) {
        if (openConfigKey.method_1436()) {
            client.method_1507((class_437)new SwordcoreConfigScreen(client.field_1755));
        }
        SwordcoreClient.handleConfigKeybinds(client);
        SwordcoreClient.enforcePremiumFeatureLocks();
        if (client.field_1687 == null || client.field_1724 == null) {
            SwordcoreClient.resetTransientState();
            return;
        }
        SwordcoreClient.sendNonPremiumMessage(client);
        if (!SwordcoreClient.swordConfig.enabled) {
            SwordcoreClient.releaseForcedKeys(client);
            SwordcoreClient.resetTransientState();
            return;
        }
        if (client.field_1755 != null || client.method_1493()) {
            SwordcoreClient.releaseForcedKeys(client);
        }
    }

    public static void processAttackInput(class_310 client) {
        if (!SwordcoreClient.isEnabled() || client.field_1724 == null || client.field_1687 == null || client.field_1755 != null || client.method_1493()) {
            return;
        }
        attackInputPhase = true;
        try {
            int tick = client.field_1724.field_6012;
            SwordcoreClient.releaseJumpFollowing(client);
            SwordcoreClient.validateTarget(client);
            SwordcoreClient.updateTargetCooldown(client, tick);
            SwordcoreClient.handleJumpReset(client);
            SwordcoreClient.handleScheduledActions(client, tick);
            SwordcoreClient.handleAutosprint(client);
            SwordcoreClient.handleCombat(client, tick);
            SwordcoreClient.handleMovement(client, tick);
            if (!client.field_1690.field_1894.method_1434()) {
                SwordcoreClient.releaseSprint(client);
            }
            SwordcoreClient.handleSneakWindow(client, tick);
        }
        finally {
            attackInputPhase = false;
        }
    }

    public static void onAttackDispatched(class_1297 target) {
        if (swordcoreSendingAttack) {
            dispatchedAttackTarget = target;
        }
    }

    public static boolean shouldSuppressDuplicateClick(class_310 client) {
        return !swordcoreSendingAttack && client.field_1724 != null && client.field_1724.field_6012 == lastAttackTick;
    }

    public static boolean isEnabled() {
        return swordConfig != null && SwordcoreClient.swordConfig.enabled;
    }

    public static int getAddedPingMillis() {
        if (swordConfig == null || !SwordcoreClient.swordConfig.enabled) {
            return 0;
        }
        return Math.max(0, SwordcoreClient.swordConfig.addedPing);
    }

    public static boolean isAirAttack(class_310 client) {
        if (SwordcoreClient.isSpearLikeHeld(client) || SwordcoreClient.isMaceHeld(client)) {
            return false;
        }
        return client.field_1692 == null && (client.field_1765 == null || client.field_1765.method_17783() == class_239.class_240.field_1333);
    }

    private static boolean isMaceHeld(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1799 stack = client.field_1724.method_6047();
        return stack != null && !stack.method_7960() && String.valueOf(stack.method_7909()).contains("mace");
    }

    private static boolean isSpearLikeHeld(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1799 stack = client.field_1724.method_6047();
        if (stack == null || stack.method_7960()) {
            return false;
        }
        String itemName = String.valueOf(stack.method_7909());
        return itemName.contains("spear") || itemName.contains("trident");
    }

    private static void enforcePremiumFeatureLocks() {
        if (swordConfig != null && !premiumFeaturesEnabled) {
            SwordcoreClient.swordConfig.visualEffects = true;
            SwordcoreClient.swordConfig.soundEffects = true;
        }
    }

    private static void sendNonPremiumMessage(class_310 client) {
        if (premiumFeaturesEnabled || msgSent || client.field_1724 == null) {
            return;
        }
        client.field_1724.method_7353(SwordcoreClient.linkUrls("\ue81b \u00a7cYou are using the free version of \ue81b\u00a77. This version is limited in terms of sword features and notably limits SmartSword, visual effect toggles, sound effect toggles, and more. Get it on thetruetrident.com/swordcore or get \u00a7r swordcore premium \u00a7r\u00a77for just $3. Those who get it from my patreon also get access to in-dev & advanced features entirely free. Get it on my Patreon at https://www.patreon.com/c/thetruetrident."), false);
        msgSent = true;
    }

    private static void handleConfigKeybinds(class_310 client) {
        while (cycleJumpFollowingKey.method_1436()) {
            SwordcoreClient.swordConfig.jumpFollowingMode = (SwordcoreConfig.JumpFollowingMode)SwordcoreClient.nextValue((Enum)SwordcoreClient.swordConfig.jumpFollowingMode, (Enum[])SwordcoreConfig.JumpFollowingMode.values());
            SwordcoreClient.saveAndReportKeybind(client, "\u00a71(KEYBIND)\u00a77 Jump Following set to \u00a7f" + SwordcoreClient.formatEnum(SwordcoreClient.swordConfig.jumpFollowingMode) + "\u00a77.");
        }
        while (cycleNeutralModeKey.method_1436()) {
            SwordcoreClient.swordConfig.neutralMode = (SwordcoreConfig.NeutralMode)SwordcoreClient.nextValue((Enum)SwordcoreClient.swordConfig.neutralMode, (Enum[])SwordcoreConfig.NeutralMode.values());
            SwordcoreClient.resetAdaptableNeutral();
            SwordcoreClient.saveAndReportKeybind(client, "\u00a71(KEYBIND)\u00a77 Neutral Mode set to \u00a7f" + SwordcoreClient.formatEnum(SwordcoreClient.swordConfig.neutralMode) + "\u00a77.");
        }
        while (toggleSTappingKey.method_1436()) {
            boolean bl = SwordcoreClient.swordConfig.sTapping = !SwordcoreClient.swordConfig.sTapping;
            if (!SwordcoreClient.swordConfig.sTapping) {
                sTapUntilTick = -1;
            }
            SwordcoreClient.saveAndReportKeybind(client, "\u00a71(KEYBIND)\u00a77 S Tapping set to \u00a7f" + SwordcoreClient.onOff(SwordcoreClient.swordConfig.sTapping) + "\u00a77.");
        }
        while (toggleFreeHittingKey.method_1436()) {
            SwordcoreClient.swordConfig.freeHitting = !SwordcoreClient.swordConfig.freeHitting;
            SwordcoreClient.saveAndReportKeybind(client, "\u00a71(KEYBIND)\u00a77 Free Hitting set to \u00a7f" + SwordcoreClient.onOff(SwordcoreClient.swordConfig.freeHitting) + "\u00a77.");
        }
        while (toggleSwordcoreKey.method_1436()) {
            boolean bl = SwordcoreClient.swordConfig.enabled = !SwordcoreClient.swordConfig.enabled;
            if (!SwordcoreClient.swordConfig.enabled) {
                SwordcoreClient.releaseForcedKeys(client);
                SwordcoreClient.resetTransientState();
            }
            SwordcoreClient.saveAndReportKeybind(client, "\u00a71(KEYBIND)\u00a77 Swordcore set to \u00a7f" + SwordcoreClient.onOff(SwordcoreClient.swordConfig.enabled) + "\u00a77.");
        }
        while (increaseResistRangeKey.method_1436()) {
            if (!SwordcoreClient.isControlDown(client)) continue;
            SwordcoreClient.swordConfig.movementResistingRange = (float)Math.round((SwordcoreClient.swordConfig.movementResistingRange + 0.1f) * 10.0f) / 10.0f;
            SwordcoreClient.saveAndReportKeybind(client, "\u00a71(KEYBIND)\u00a77 Movement Resist range increased to \u00a7f" + String.format("%.1f", Float.valueOf(SwordcoreClient.swordConfig.movementResistingRange)) + "\u00a77.");
        }
        while (decreaseResistRangeKey.method_1436()) {
            if (!SwordcoreClient.isControlDown(client)) continue;
            SwordcoreClient.swordConfig.movementResistingRange = Math.max(0.0f, (float)Math.round((SwordcoreClient.swordConfig.movementResistingRange - 0.1f) * 10.0f) / 10.0f);
            SwordcoreClient.saveAndReportKeybind(client, "\u00a71(KEYBIND)\u00a77 Movement Resist range decreased to \u00a7f" + String.format("%.1f", Float.valueOf(SwordcoreClient.swordConfig.movementResistingRange)) + "\u00a77.");
        }
    }

    private static <T extends Enum<T>> T nextValue(T current, T[] values) {
        return values[(current.ordinal() + 1) % values.length];
    }

    private static String formatEnum(Enum<?> value) {
        return value.name().toLowerCase().replace('_', ' ');
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    private static boolean isControlDown(class_310 client) {
        return class_3675.method_15987((class_1041)client.method_22683(), (int)341) || class_3675.method_15987((class_1041)client.method_22683(), (int)345);
    }

    private static void saveAndReportKeybind(class_310 client, String message) {
        SwordcorePresetApplier.refreshPresetFromCurrent();
        swordConfig.save();
        SwordcoreClient.sendKeybindLog(message, (class_1657)client.field_1724);
        SwordcoreClient.playKeybindSound(client);
    }

    private static void sendKeybindLog(String text, class_1657 player) {
        if (SwordcoreClient.swordConfig.chatLogs && player != null) {
            player.method_7353((class_2561)class_2561.method_43470((String)(LOG_PREFIX + text + " \u00a78Chat logs can be disabled in swordcore premium..")), false);
        }
    }

    private static void playKeybindSound(class_310 client) {
        if (SwordcoreClient.swordConfig.soundEffects && client.field_1724 != null) {
            client.field_1724.method_5783(class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"entity.baby_nautilus.swim")), 1.0f, 2.0f);
        }
    }

    private static void handleCombat(class_310 client, int tick) {
        boolean attackDown = client.field_1690.field_1886.method_1434();
        boolean attackPressed = attackDown && !previousAttackDown;
        previousAttackDown = attackDown;
        boolean playerBusy = SwordcoreClient.isPlayerUsingShieldOrEating(client);
        if (SwordcoreClient.swordConfig.smartSword && !previousSmartSwordEnabled) {
            nonPremiumSmartSwordHits = 0;
        }
        previousSmartSwordEnabled = SwordcoreClient.swordConfig.smartSword;
        smartSwordActive = SwordcoreClient.swordConfig.smartSword && smartSwordKey.method_1434() && !playerBusy;
        boolean sprintHitIntent = SwordcoreClient.hasSprintHitIntent(client);
        if (attackPressed) {
            lastManualAttackIntentTick = tick;
            SwordcoreClient.setTargetFromAttack(client);
        }
        SwordcoreClient.syncMacecoreTargetFromBackend(client, tick);
        SwordcoreClient.syncSmartSwordTargetFromLook(client);
        class_1657 lookedPlayer = SwordcoreClient.getLookedPlayer(client);
        boolean bl = swordcoreOwnsAttack = !macecoreSwapAssistRequested && !playerBusy && lookedPlayer != null && lookedPlayer == targetPlayer && (smartSwordActive || SwordcoreClient.canShieldDisable(client, lookedPlayer) || !SwordcoreClient.swordConfig.freeHitting && SwordcoreClient.isAirAttack(client));
        if (attackPressed && lookedPlayer != null && lookedPlayer == targetPlayer && SwordcoreClient.isPlayerAttackReady(client)) {
            SwordcoreClient.startSTap(lookedPlayer, tick);
            SwordcoreClient.registerComboHit(client, lookedPlayer, tick);
            SwordcoreClient.queueSprintHitFollowThrough(client, lookedPlayer, tick, sprintHitIntent);
        }
        if (!SwordcoreClient.swordConfig.freeHitting && SwordcoreClient.isAirAttack(client)) {
            client.field_1690.field_1886.method_23481(false);
            return;
        }
        if (targetPlayer == null) {
            return;
        }
        if (!playerBusy && attackPressed && SwordcoreClient.canShieldDisable(client, lookedPlayer)) {
            SwordcoreClient.performShieldDisable(client, lookedPlayer, tick);
            return;
        }
        if (smartSwordActive && lookedPlayer != null && SwordcoreClient.canSmartSwordHit(client, lookedPlayer) && SwordcoreClient.isSmartSwordSwapItem(client.field_1724.method_6047())) {
            if (SwordcoreClient.tryMacecoreSwapAssist(client, lookedPlayer, tick)) {
                SwordcoreClient.registerSmartSwordHit(client);
                SwordcoreClient.queueSprintHitFollowThrough(client, lookedPlayer, tick, sprintHitIntent);
                return;
            }
            if (!SwordcoreClient.isSword(client.field_1724.method_6047())) {
                return;
            }
            if (SwordcoreClient.canShieldDisable(client, lookedPlayer)) {
                if (SwordcoreClient.performShieldDisable(client, lookedPlayer, tick)) {
                    SwordcoreClient.registerSmartSwordHit(client);
                }
                return;
            }
            if (SwordcoreClient.attackTarget(client, lookedPlayer, tick, null)) {
                SwordcoreClient.registerSmartSwordHit(client);
                SwordcoreClient.queueSprintHitFollowThrough(client, lookedPlayer, tick, sprintHitIntent);
            }
            return;
        }
    }

    private static void syncSmartSwordTargetFromLook(class_310 client) {
        if (!smartSwordActive) {
            return;
        }
        class_1657 lookedPlayer = SwordcoreClient.getLookedPlayer(client);
        if (lookedPlayer != null && lookedPlayer != targetPlayer) {
            SwordcoreClient.setTarget(lookedPlayer, client);
        }
    }

    private static boolean canShieldDisable(class_310 client, class_1657 target) {
        return SwordcoreClient.swordConfig.shieldDisable && target != null && !SwordcoreClient.isPlayerUsingShieldOrEating(client) && SwordcoreClient.isShieldBlocking(target) && SwordcoreClient.isSword(client.field_1724.method_6047()) && !SwordcoreClient.shouldStandDownForMacecoreShieldSwap(client, target);
    }

    private static boolean shouldStandDownForMacecoreShieldSwap(class_310 client, class_1657 target) {
        if (!SwordcoreClient.isMacecoreLoaded() || client.field_1724 == null || target == null || !SwordcoreClient.isShieldBlocking(target)) {
            return false;
        }
        if (SwordcoreClient.hasHotbarItem((class_1657)client.field_1724, "mace")) {
            return true;
        }
        return client.field_1724.method_18798().field_1351 < -0.4 && (SwordcoreClient.hasHotbarItem((class_1657)client.field_1724, "spear") || SwordcoreClient.hasHotbarItem((class_1657)client.field_1724, "trident"));
    }

    public static boolean isMacecoreSwapAssistRequested() {
        return macecoreSwapAssistRequested;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean tryMacecoreSwapAssist(class_310 client, class_1657 target, int tick) {
        if (!SwordcoreClient.isMacecoreLoaded() || client.field_1724 == null || target == null || !SwordcoreClient.hasHotbarItem((class_1657)client.field_1724, "mace")) {
            return false;
        }
        class_1799 held = client.field_1724.method_6047();
        if (!SwordcoreClient.isSword(held) && held.method_7909() != class_1802.field_8547 && !String.valueOf(held.method_7909()).contains("spear")) {
            return false;
        }
        try {
            if (macecoreCanRouteSwapMethod == null) {
                macecoreCanRouteSwapMethod = Class.forName("thetruetrident.macecore.MacecoreClient").getMethod("canRouteAttackThroughSwapIgnoringSwordcore", class_310.class);
            }
            if (!Boolean.TRUE.equals(macecoreCanRouteSwapMethod.invoke(null, client))) {
                boolean bl = false;
                return bl;
            }
            macecoreSwapAssistRequested = true;
            boolean bl = SwordcoreClient.sendValidatedAttack(client, target, tick, null);
            return bl;
        }
        catch (ReflectiveOperationException | RuntimeException ignored) {
            boolean bl = false;
            return bl;
        }
        finally {
            macecoreSwapAssistRequested = false;
        }
    }

    private static boolean performShieldDisable(class_310 client, class_1657 target, int tick) {
        int swordSlot = client.field_1724.method_31548().method_67532();
        if (SwordcoreClient.swapSlot((class_1657)client.field_1724, "_axe", tick)) {
            if (SwordcoreClient.swordConfig.shieldDisableMode == SwordcoreConfig.ShieldDisableMode.STUN_SNEAK) {
                sneakUntilTick = tick + 2;
                SwordcoreClient.setSneak(client, true);
            }
            SwordcoreClient.sendValidatedAttack(client, target, tick, "Shield disable axe hit.");
            SwordcoreClient.scheduleSwordRestore(swordSlot, tick);
            if (SwordcoreClient.swordConfig.shieldDisableMode != SwordcoreConfig.ShieldDisableMode.NORMAL) {
                pendingStunTarget = target;
                pendingStunHitTick = tick + 1;
            }
            return true;
        }
        return false;
    }

    private static void handleScheduledActions(class_310 client, int tick) {
        if (pendingStunHitTick >= 0 && tick >= pendingStunHitTick) {
            if (pendingStunTarget != null && pendingStunTarget.method_5805()) {
                SwordcoreClient.attackTarget(client, pendingStunTarget, tick, "Stun follow-up hit.");
            }
            pendingStunTarget = null;
            pendingStunHitTick = -1;
        }
        if (pendingSwordRestoreTick >= 0 && tick >= pendingSwordRestoreTick) {
            SwordcoreClient.restoreSwordSlot((class_1657)client.field_1724, tick);
            pendingSwordRestoreTick = -1;
            pendingSwordRestoreSlot = -1;
        }
    }

    private static void handleMovement(class_310 client, int tick) {
        SwordcoreClient.releaseMovementKeys(client);
        SwordcoreClient.releaseResistKeys(client);
        if (SwordcoreClient.holdSprintHitFollowThrough(client, tick)) {
            movementResistFeedbackActive = false;
            return;
        }
        if (SwordcoreClient.isSTapping(tick)) {
            SwordcoreClient.setResistedForward(client, true);
            SwordcoreClient.setBack(client, true);
            movementResistFeedbackActive = false;
            return;
        }
        if (!SwordcoreClient.canUseMovementFeatures(client)) {
            movementSyncInnerFeedbackActive = false;
            airGateFeedbackActive = false;
            movementEqualizerFeedbackActive = false;
            movementResistFeedbackActive = false;
            return;
        }
        if (targetPlayer == null) {
            movementSyncInnerFeedbackActive = false;
            airGateFeedbackActive = false;
            movementEqualizerFeedbackActive = false;
            return;
        }
        if (SwordcoreClient.holdAggressiveJumpMovement(client)) {
            movementResistFeedbackActive = false;
            return;
        }
        boolean resisting = SwordcoreClient.swordConfig.movementResisting && SwordcoreClient.shouldResist(client, tick);
        SwordcoreClient.updateMovementSyncInnerFeedback(client, tick);
        SwordcoreClient.spawnMovementResistBubbleParticles(client, tick);
        SwordcoreClient.spawnMovementSyncInnerParticles(client, tick);
        if (resisting) {
            if (SwordcoreClient.swordConfig.movementSyncing && SwordcoreClient.syncMovement(client, tick, true)) {
                movementResistFeedbackActive = false;
                return;
            }
            SwordcoreClient.resistMovement(client);
            return;
        }
        if (SwordcoreClient.isInsideResistBubble(client) && !SwordcoreClient.isTargetOnCooldown(client, tick)) {
            movementResistFeedbackActive = false;
            if (SwordcoreClient.swordConfig.movementResistEscape) {
                SwordcoreClient.escapeResistBubble(client);
            }
            return;
        }
        movementResistFeedbackActive = false;
        if (SwordcoreClient.swordConfig.movementSyncing && !SwordcoreClient.shouldResist(client, tick)) {
            SwordcoreClient.syncMovement(client, tick);
        }
    }

    private static void updateMovementSyncInnerFeedback(class_310 client, int tick) {
        if (!(SwordcoreClient.canUseMovementFeatures(client) && SwordcoreClient.swordConfig.movementSyncing && targetPlayer != null && client.field_1687 != null && SwordcoreClient.isTargetOnCooldown(client, tick))) {
            movementSyncInnerFeedbackActive = false;
            return;
        }
        double visibleRange = (double)Math.max(SwordcoreClient.swordConfig.movementResistingRange, SwordcoreClient.swordConfig.movementSyncInnerRange) + 0.8;
        double eyeDistance = targetPlayer.method_33571().method_1022(client.field_1724.method_33571());
        if (eyeDistance > visibleRange) {
            movementSyncInnerFeedbackActive = false;
            return;
        }
        if (!movementSyncInnerFeedbackActive) {
            SwordcoreClient.playSound(client, class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"entity.creaking.activate")), 0.5f, 2.0f);
            movementSyncInnerFeedbackActive = true;
        }
    }

    private static boolean syncMovement(class_310 client, int tick) {
        return SwordcoreClient.syncMovement(client, tick, false);
    }

    private static boolean syncMovement(class_310 client, int tick, boolean allowAggressiveResistOverride) {
        if (!SwordcoreClient.canUseMovementFeatures(client)) {
            return false;
        }
        if (SwordcoreClient.isTargetOnCooldown(client, tick)) {
            SwordcoreClient.syncTowardInnerBubble(client);
            return false;
        }
        class_243 horizontalMovement = SwordcoreClient.getObservedTargetHorizontalMovement();
        if (horizontalMovement.method_1027() < 0.0025) {
            airGateFeedbackActive = false;
            movementEqualizerFeedbackActive = false;
            return false;
        }
        class_243 toPlayer = client.field_1724.method_33571().method_1020(targetPlayer.method_33571()).method_18805(1.0, 0.0, 1.0);
        if (toPlayer.method_1027() < 1.0E-4) {
            return false;
        }
        class_243 normal = toPlayer.method_1029();
        double radialMovement = horizontalMovement.method_1026(normal);
        SwordcoreClient.updateAdaptableNeutralHabit(radialMovement);
        if (radialMovement > 0.05 && SwordcoreClient.isWithinBackSyncRange(client) && !SwordcoreClient.shouldBlockBackDuringAggressiveJump(client)) {
            if (SwordcoreClient.handleMovementEqualizer(client)) {
                return true;
            }
            SwordcoreClient.setBack(client, true);
            return true;
        }
        if (SwordcoreClient.isNeutralModeAggressive() && radialMovement < -0.05) {
            if (!SwordcoreClient.canMovementSyncPressForward(client)) {
                return false;
            }
            if (!SwordcoreClient.canAggressiveJumpOverrideResist(client, allowAggressiveResistOverride) && SwordcoreClient.wouldSyncPushIntoResistBubble(client, SwordcoreClient.forwardVector(client.field_1724.method_36454())) && (!SwordcoreClient.canUseJumpFollowing() || SwordcoreClient.swordConfig.jumpFollowingRespectsResist)) {
                return false;
            }
            SwordcoreClient.setForward(client, true);
            SwordcoreClient.handleJumpFollowing(client, true, allowAggressiveResistOverride);
            return true;
        }
        airGateFeedbackActive = false;
        movementEqualizerFeedbackActive = false;
        return false;
    }

    private static boolean canAggressiveJumpOverrideResist(class_310 client, boolean allowAggressiveResistOverride) {
        return allowAggressiveResistOverride && SwordcoreClient.swordConfig.preventBackDuringAggressiveJump && SwordcoreClient.canUseJumpFollowing() && client.field_1724.method_24828();
    }

    private static boolean shouldBlockBackDuringAggressiveJump(class_310 client) {
        return SwordcoreClient.isAggressiveMovementJumpWindow(client);
    }

    private static boolean holdAggressiveJumpMovement(class_310 client) {
        if (!SwordcoreClient.isAggressiveMovementJumpWindow(client)) {
            return false;
        }
        if (forcedBack) {
            SwordcoreClient.setBack(client, false);
        }
        SwordcoreClient.setForward(client, true);
        client.field_1690.field_1867.method_23481(true);
        forcedSprint = true;
        return true;
    }

    private static boolean holdSprintHitFollowThrough(class_310 client, int tick) {
        if (!(SwordcoreClient.canUseMovementFeatures(client) && SwordcoreClient.swordConfig.sprintHitFollowThrough && SwordcoreClient.swordConfig.movementSyncing && SwordcoreClient.canUseJumpFollowing() && targetPlayer != null && tick <= sprintHitFollowUntilTick && !SwordcoreClient.isInsideMovementSyncInner(client))) {
            SwordcoreClient.clearSprintHitFollowThrough();
            return false;
        }
        if (forcedBack) {
            SwordcoreClient.setBack(client, false);
        }
        SwordcoreClient.setForward(client, true);
        client.field_1690.field_1867.method_23481(true);
        forcedSprint = true;
        if (tick >= sprintHitFollowJumpTick && client.field_1724.method_24828()) {
            client.field_1690.field_1903.method_23481(true);
            forcedJumpFollowing = true;
            aggressiveMovementJumpActive = true;
            aggressiveMovementJumpUntilTick = Math.max(aggressiveMovementJumpUntilTick, tick + 4);
        }
        return true;
    }

    private static boolean isAggressiveMovementJumpWindow(class_310 client) {
        if (!SwordcoreClient.swordConfig.preventBackDuringAggressiveJump || !aggressiveMovementJumpActive || client.field_1724 == null) {
            return false;
        }
        if (client.field_1724.field_6012 > aggressiveMovementJumpUntilTick) {
            aggressiveMovementJumpActive = false;
            aggressiveMovementJumpUntilTick = -1;
            return false;
        }
        return true;
    }

    private static void updateAdaptableNeutralHabit(double radialMovement) {
        int behavior;
        if (!SwordcoreClient.isAdaptableNeutralMode()) {
            lastAdaptableNeutralBehavior = 0;
            return;
        }
        boolean wasAggressive = SwordcoreClient.isAdaptableNeutralAggressive();
        if (radialMovement > 0.05) {
            adaptableNeutralHabitScore = Math.min(3, adaptableNeutralHabitScore + 1);
        } else if (radialMovement < -0.05) {
            adaptableNeutralHabitScore = Math.max(-3, adaptableNeutralHabitScore - 1);
        }
        boolean isAggressive = SwordcoreClient.isAdaptableNeutralAggressive();
        int n = behavior = isAggressive ? 2 : 1;
        if (lastAdaptableNeutralBehavior != 0 && lastAdaptableNeutralBehavior != behavior) {
            if (isAggressive) {
                SwordcoreClient.playSound(class_310.method_1551(), class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"entity.illusioner.prepare_mirror")), 1.0f, 2.0f);
                SwordcoreClient.startVisualTint(true);
                SwordcoreClient.sendLog("\u00a71(MOVEMENT SYNC)\u00a77 Neutral adapter switched to \u00a74\u00a7lAGGRESSIVE behavior.", (class_1657)class_310.method_1551().field_1724);
            } else if (wasAggressive) {
                SwordcoreClient.playSound(class_310.method_1551(), class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"entity.illusioner.prepare_mirror")), 1.0f, 1.0f);
                SwordcoreClient.startVisualTint(false);
                SwordcoreClient.sendLog("\u00a71(MOVEMENT SYNC)\u00a77 Neutral adapter switched to \u00a7b\u00a7lPASSIVE behavior.", (class_1657)class_310.method_1551().field_1724);
            }
        }
        lastAdaptableNeutralBehavior = behavior;
    }

    private static void startVisualTint(boolean aggressive) {
        if (!SwordcoreClient.isEnabled() || !SwordcoreClient.swordConfig.visualTinting) {
            return;
        }
        visualTintColor = aggressive ? -57312 : -14656001;
        visualTintStartNanos = System.nanoTime();
    }

    public static void renderVisualTint(class_332 context) {
        if (!SwordcoreClient.isEnabled() || swordConfig == null || !SwordcoreClient.swordConfig.visualTinting || visualTintStartNanos == 0L) {
            return;
        }
        long elapsed = System.nanoTime() - visualTintStartNanos;
        if (elapsed >= 650000000L) {
            visualTintStartNanos = 0L;
            return;
        }
        float progress = (float)elapsed / 6.5E8f;
        float alphaProgress = (1.0f - progress) * (1.0f - progress);
        int alpha = class_3532.method_15340((int)((int)(110.0f * alphaProgress)), (int)0, (int)110);
        int color = alpha << 24 | visualTintColor & 0xFFFFFF;
        class_310 client = class_310.method_1551();
        context.method_25294(0, 0, client.method_22683().method_4486(), client.method_22683().method_4502(), color);
    }

    private static boolean isNeutralModeAggressive() {
        return SwordcoreClient.swordConfig.neutralMode == SwordcoreConfig.NeutralMode.AGGRESSIVE || SwordcoreClient.isAdaptableNeutralMode() && SwordcoreClient.isAdaptableNeutralAggressive();
    }

    private static boolean isNeutralModePassive() {
        return SwordcoreClient.swordConfig.neutralMode == SwordcoreConfig.NeutralMode.PASSIVE || SwordcoreClient.isAdaptableNeutralMode() && !SwordcoreClient.isAdaptableNeutralAggressive();
    }

    private static boolean canUseJumpFollowing() {
        if (SwordcoreClient.isCrystalcoreCrystalFlicking()) {
            return false;
        }
        return SwordcoreClient.swordConfig.jumpFollowingMode == SwordcoreConfig.JumpFollowingMode.ON || SwordcoreClient.swordConfig.jumpFollowingMode == SwordcoreConfig.JumpFollowingMode.NEUTRAL_BASED && SwordcoreClient.isNeutralModeAggressive();
    }

    private static boolean isCrystalcoreCrystalFlicking() {
        try {
            Class<?> updatedFlicking = Class.forName("thetruetrident.crystalcore.updatedCrystalFlicking");
            return updatedFlicking.getField("crystalFlickCheck").getBoolean(null);
        }
        catch (LinkageError | ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static boolean isAdaptableNeutralMode() {
        return SwordcoreClient.swordConfig.neutralMode == SwordcoreConfig.NeutralMode.ADAPTABLE || SwordcoreClient.swordConfig.neutralMode == SwordcoreConfig.NeutralMode.ADAPTABLE_INVERSE;
    }

    private static boolean isAdaptableNeutralAggressive() {
        boolean defaultAggressive;
        boolean bl = defaultAggressive = adaptableNeutralHabitScore <= -2;
        return SwordcoreClient.swordConfig.neutralMode == SwordcoreConfig.NeutralMode.ADAPTABLE_INVERSE ? !defaultAggressive : defaultAggressive;
    }

    private static boolean isWithinBackSyncRange(class_310 client) {
        double eyeDistance = targetPlayer.method_33571().method_1022(client.field_1724.method_33571());
        return eyeDistance <= (double)SwordcoreClient.swordConfig.movementResistingRange + 2.0;
    }

    private static boolean handleMovementEqualizer(class_310 client) {
        if (!(SwordcoreClient.canUseMovementFeatures(client) && SwordcoreClient.swordConfig.movementEqualizer != SwordcoreConfig.MovementEqualizerMode.OFF && targetPlayer != null && targetPlayer.method_5624() && SwordcoreClient.isAtOrJustOutsideResistRange(client))) {
            movementEqualizerFeedbackActive = false;
            return false;
        }
        if (SwordcoreClient.shouldEqualizerBlockBack() && !targetPlayer.method_24828()) {
            SwordcoreClient.playMovementSyncGateFeedback(client, "Movement Equalizer activated.", true);
            SwordcoreClient.setBack(client, false);
            SwordcoreClient.setForward(client, true);
            SwordcoreClient.playSound(client, class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"block.conduit.deactivate")), 1.0f, 0.0f);
            SwordcoreClient.sendLog("\u00a71(MOVEMENT SYNC)\u00a77 Neutralizing aerial aggressive target.", (class_1657)client.field_1724);
            return true;
        }
        boolean equalizerJump = SwordcoreClient.shouldEqualizerJump();
        if (equalizerJump) {
            if (!targetPlayer.method_24828()) {
                SwordcoreClient.playMovementSyncGateFeedback(client, "Movement Equalizer activated.", true);
                SwordcoreClient.setBack(client, false);
                SwordcoreClient.setForward(client, true);
                SwordcoreClient.playSound(client, class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"block.conduit.deactivate")), 1.0f, 0.0f);
                SwordcoreClient.sendLog("\u00a71(MOVEMENT SYNC)\u00a77 Neutralizing aerial aggressive target.", (class_1657)client.field_1724);
                return true;
            }
            if (client.field_1724.method_24828()) {
                SwordcoreClient.playMovementSyncGateFeedback(client, "Movement Equalizer activated, jumping to prepare for a counter-hit.", true);
                client.field_1690.field_1903.method_23481(true);
                forcedJumpFollowing = true;
                if (SwordcoreClient.swordConfig.movementEqualizer == SwordcoreConfig.MovementEqualizerMode.ADAPTABLE_EXPERIMENTAL) {
                    adaptableEqualizerJumpUntilTick = client.field_1724.field_6012 + 8;
                }
            }
        } else {
            SwordcoreClient.playMovementSyncGateFeedback(client, "Movement Equalizer activated.", true);
        }
        return SwordcoreClient.shouldEqualizerBlockBack();
    }

    private static boolean shouldEqualizerJump() {
        return SwordcoreClient.swordConfig.movementEqualizer == SwordcoreConfig.MovementEqualizerMode.JUMP || SwordcoreClient.swordConfig.movementEqualizer == SwordcoreConfig.MovementEqualizerMode.EQUALIZER_JUMP || SwordcoreClient.swordConfig.movementEqualizer == SwordcoreConfig.MovementEqualizerMode.ADAPTABLE_EXPERIMENTAL && adaptableEqualizerJumping;
    }

    private static boolean shouldEqualizerBlockBack() {
        return SwordcoreClient.swordConfig.movementEqualizer == SwordcoreConfig.MovementEqualizerMode.EQUALIZER || SwordcoreClient.swordConfig.movementEqualizer == SwordcoreConfig.MovementEqualizerMode.EQUALIZER_JUMP || SwordcoreClient.swordConfig.movementEqualizer == SwordcoreConfig.MovementEqualizerMode.ADAPTABLE_EXPERIMENTAL;
    }

    private static boolean isAtOrJustOutsideResistRange(class_310 client) {
        double eyeDistance = targetPlayer.method_33571().method_1022(client.field_1724.method_33571());
        return eyeDistance >= (double)SwordcoreClient.swordConfig.movementResistingRange - 0.2 && eyeDistance <= (double)(SwordcoreClient.swordConfig.movementResistingRange + SwordcoreClient.swordConfig.movementEqualizerRange);
    }

    private static class_243 getObservedTargetHorizontalMovement() {
        if (targetPlayer != lastMovementSyncTarget) {
            lastMovementSyncTarget = targetPlayer;
            lastMovementSyncTargetX = targetPlayer.method_23317();
            lastMovementSyncTargetZ = targetPlayer.method_23321();
            return class_243.field_1353;
        }
        class_243 movement = new class_243(targetPlayer.method_23317() - lastMovementSyncTargetX, 0.0, targetPlayer.method_23321() - lastMovementSyncTargetZ);
        lastMovementSyncTargetX = targetPlayer.method_23317();
        lastMovementSyncTargetZ = targetPlayer.method_23321();
        return movement;
    }

    private static void syncTowardInnerBubble(class_310 client) {
        if (!SwordcoreClient.canUseMovementFeatures(client)) {
            return;
        }
        if (!SwordcoreClient.canMovementSyncPressForward(client)) {
            return;
        }
        if (!client.field_1724.method_24828() && !SwordcoreClient.isNeutralModeAggressive()) {
            return;
        }
        double eyeDistance = targetPlayer.method_33571().method_1022(client.field_1724.method_33571());
        if (eyeDistance <= (double)SwordcoreClient.swordConfig.movementSyncInnerRange) {
            return;
        }
        SwordcoreClient.setForward(client, true);
    }

    private static boolean canMovementSyncPressForward(class_310 client) {
        if (!SwordcoreClient.swordConfig.airGate || client.field_1724.method_24828()) {
            airGateFeedbackActive = false;
            return true;
        }
        if (!SwordcoreClient.isWithinAirGateRange(client)) {
            airGateFeedbackActive = false;
            return true;
        }
        SwordcoreClient.playMovementSyncGateFeedback(client, "Air Gate stopped forward sync while airborne.", false);
        return false;
    }

    private static boolean isWithinAirGateRange(class_310 client) {
        if (targetPlayer == null) {
            return false;
        }
        double eyeDistance = targetPlayer.method_33571().method_1022(client.field_1724.method_33571());
        return eyeDistance <= (double)SwordcoreClient.swordConfig.movementResistingRange + 0.5;
    }

    private static void handleJumpFollowing(class_310 client, boolean aggressiveMovementJump, boolean overrideResist) {
        if (!SwordcoreClient.canUseJumpFollowing() || targetPlayer == null || !client.field_1724.method_24828()) {
            return;
        }
        if (!overrideResist && SwordcoreClient.swordConfig.jumpFollowingRespectsResist && targetPlayer.method_33571().method_1022(client.field_1724.method_33571()) <= (double)SwordcoreClient.swordConfig.movementResistingRange) {
            return;
        }
        client.field_1690.field_1867.method_23481(true);
        client.field_1690.field_1903.method_23481(true);
        SwordcoreClient.setBack(client, false);
        SwordcoreClient.setForward(client, true);
        forcedSprint = true;
        forcedJumpFollowing = true;
        aggressiveMovementJumpActive = aggressiveMovementJump;
        aggressiveMovementJumpUntilTick = aggressiveMovementJump ? client.field_1724.field_6012 + 8 : -1;
    }

    private static boolean shouldResist(class_310 client, int tick) {
        if (!SwordcoreClient.canUseMovementFeatures(client)) {
            return false;
        }
        if (client.field_1690.field_1832.method_1434()) {
            return false;
        }
        double eyeDistance = targetPlayer.method_33571().method_1022(client.field_1724.method_33571());
        if (!SwordcoreClient.isAtResistBoundary(eyeDistance)) {
            return false;
        }
        if (SwordcoreClient.isTargetOnCooldown(client, tick)) {
            return false;
        }
        class_243 intended = SwordcoreClient.intendedMovement(client);
        if (intended.method_1027() < 1.0E-4) {
            return false;
        }
        class_243 toTarget = targetPlayer.method_33571().method_1020(client.field_1724.method_33571()).method_18805(1.0, 0.0, 1.0);
        if (toTarget.method_1027() < 1.0E-4) {
            return false;
        }
        return intended.method_1029().method_1026(toTarget.method_1029()) > 0.15;
    }

    private static boolean wouldSyncPushIntoResistBubble(class_310 client, class_243 syncDirection) {
        if (!SwordcoreClient.canUseMovementFeatures(client) || !SwordcoreClient.swordConfig.movementResisting || targetPlayer == null || client.field_1690.field_1832.method_1434() || SwordcoreClient.isTargetOnCooldown(client, client.field_1724.field_6012)) {
            return false;
        }
        class_243 toTarget = targetPlayer.method_33571().method_1020(client.field_1724.method_33571()).method_18805(1.0, 0.0, 1.0);
        if (toTarget.method_1027() < 1.0E-4) {
            return false;
        }
        double eyeDistance = targetPlayer.method_33571().method_1022(client.field_1724.method_33571());
        return SwordcoreClient.isAtResistBoundary(eyeDistance) && syncDirection.method_1029().method_1026(toTarget.method_1029()) > 0.15;
    }

    private static boolean isAtResistBoundary(double eyeDistance) {
        return eyeDistance >= (double)SwordcoreClient.swordConfig.movementResistingRange - 0.2 && eyeDistance <= (double)SwordcoreClient.swordConfig.movementResistingRange + 0.2;
    }

    private static boolean isInsideResistBubble(class_310 client) {
        return SwordcoreClient.swordConfig.movementResisting && SwordcoreClient.canUseMovementFeatures(client) && targetPlayer != null && targetPlayer.method_33571().method_1022(client.field_1724.method_33571()) < (double)SwordcoreClient.swordConfig.movementResistingRange - 0.2;
    }

    private static void spawnMovementResistBubbleParticles(class_310 client, int tick) {
        if (!SwordcoreClient.canUseMovementFeatures(client) || !SwordcoreClient.swordConfig.visualEffects || !SwordcoreClient.swordConfig.movementResisting || targetPlayer == null || client.field_1687 == null || tick % 4 != 0 || SwordcoreClient.isTargetOnCooldown(client, tick)) {
            return;
        }
        double eyeDistance = targetPlayer.method_33571().method_1022(client.field_1724.method_33571());
        if (eyeDistance > (double)SwordcoreClient.swordConfig.movementResistingRange + 0.8) {
            return;
        }
        class_243 center = targetPlayer.method_33571();
        int points = 36;
        for (int i = 0; i < points; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)points;
            double x = center.field_1352 + Math.cos(angle) * (double)SwordcoreClient.swordConfig.movementResistingRange;
            double z = center.field_1350 + Math.sin(angle) * (double)SwordcoreClient.swordConfig.movementResistingRange;
            client.field_1687.method_8466((class_2394)SwordcoreParticles.MOVEMENT_RESIST, true, true, x, center.field_1351, z, 0.0, 0.0, 0.0);
        }
    }

    private static void spawnMovementSyncInnerParticles(class_310 client, int tick) {
        if (!(SwordcoreClient.canUseMovementFeatures(client) && SwordcoreClient.swordConfig.visualEffects && SwordcoreClient.swordConfig.movementSyncing && targetPlayer != null && client.field_1687 != null && tick % 4 == 0 && SwordcoreClient.isTargetOnCooldown(client, tick))) {
            return;
        }
        double visibleRange = (double)Math.max(SwordcoreClient.swordConfig.movementResistingRange, SwordcoreClient.swordConfig.movementSyncInnerRange) + 0.8;
        double eyeDistance = targetPlayer.method_33571().method_1022(client.field_1724.method_33571());
        if (eyeDistance > visibleRange) {
            return;
        }
        class_243 center = targetPlayer.method_33571();
        int points = 36;
        for (int i = 0; i < points; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)points;
            double x = center.field_1352 + Math.cos(angle) * (double)SwordcoreClient.swordConfig.movementSyncInnerRange;
            double z = center.field_1350 + Math.sin(angle) * (double)SwordcoreClient.swordConfig.movementSyncInnerRange;
            client.field_1687.method_8466((class_2394)SwordcoreParticles.MOVEMENT_SYNC_INNER, true, true, x, center.field_1351, z, 0.0, 0.0, 0.0);
        }
    }

    private static void resistMovement(class_310 client) {
        class_243 toTarget = targetPlayer.method_33571().method_1020(client.field_1724.method_33571()).method_18805(1.0, 0.0, 1.0).method_1029();
        class_243 forward = SwordcoreClient.forwardVector(client.field_1724.method_36454());
        class_243 right = SwordcoreClient.rightVector(client.field_1724.method_36454());
        if (client.field_1690.field_1894.method_1434() && forward.method_1026(toTarget) > 0.05) {
            SwordcoreClient.playMovementResistFeedback(client);
            SwordcoreClient.setResistedForward(client, true);
        }
        if (client.field_1690.field_1881.method_1434() && forward.method_1021(-1.0).method_1026(toTarget) > 0.05) {
            SwordcoreClient.setResistedBack(client, true);
        }
        if (client.field_1690.field_1849.method_1434() && right.method_1026(toTarget) > 0.05) {
            SwordcoreClient.setResistedRight(client, true);
        }
        if (client.field_1690.field_1913.method_1434() && right.method_1021(-1.0).method_1026(toTarget) > 0.05) {
            SwordcoreClient.setResistedLeft(client, true);
        }
    }

    private static void escapeResistBubble(class_310 client) {
        class_243 awayFromTarget = client.field_1724.method_33571().method_1020(targetPlayer.method_33571()).method_18805(1.0, 0.0, 1.0);
        if (awayFromTarget.method_1027() < 1.0E-4) {
            return;
        }
        awayFromTarget = awayFromTarget.method_1029();
        class_243 forward = SwordcoreClient.forwardVector(client.field_1724.method_36454());
        class_243 right = SwordcoreClient.rightVector(client.field_1724.method_36454());
        double forwardScore = forward.method_1026(awayFromTarget);
        double backScore = forward.method_1021(-1.0).method_1026(awayFromTarget);
        double rightScore = right.method_1026(awayFromTarget);
        double leftScore = right.method_1021(-1.0).method_1026(awayFromTarget);
        double best = Math.max(Math.max(forwardScore, backScore), Math.max(rightScore, leftScore));
        if (best == forwardScore) {
            SwordcoreClient.setForward(client, true);
        } else if (best == backScore) {
            SwordcoreClient.setBack(client, true);
        } else if (best == rightScore) {
            SwordcoreClient.setRight(client, true);
        } else {
            SwordcoreClient.setLeft(client, true);
        }
    }

    private static class_243 intendedMovement(class_310 client) {
        class_243 movement = class_243.field_1353;
        class_243 forward = SwordcoreClient.forwardVector(client.field_1724.method_36454());
        class_243 right = SwordcoreClient.rightVector(client.field_1724.method_36454());
        if (client.field_1690.field_1894.method_1434()) {
            movement = movement.method_1019(forward);
        }
        if (client.field_1690.field_1881.method_1434()) {
            movement = movement.method_1020(forward);
        }
        if (client.field_1690.field_1849.method_1434()) {
            movement = movement.method_1019(right);
        }
        if (client.field_1690.field_1913.method_1434()) {
            movement = movement.method_1020(right);
        }
        return movement.method_18805(1.0, 0.0, 1.0);
    }

    private static void handleAutosprint(class_310 client) {
        if (!SwordcoreClient.swordConfig.autoWTapAutosprint) {
            SwordcoreClient.releaseSprint(client);
            return;
        }
        if (client.field_1690.field_1894.method_1434() && !client.field_1724.method_5715()) {
            client.field_1690.field_1867.method_23481(true);
            forcedSprint = true;
        } else {
            SwordcoreClient.releaseSprint(client);
        }
    }

    private static void handleJumpReset(class_310 client) {
        boolean tookDamage;
        float health = client.field_1724.method_6032();
        boolean bl = tookDamage = previousHealth >= 0.0f && health < previousHealth;
        if (tookDamage) {
            SwordcoreClient.resetComboCounter();
            SwordcoreClient.updateAdaptableEqualizerDamage(client.field_1724.field_6012);
            if (SwordcoreClient.swordConfig.jumpResetting && SwordcoreClient.wasRecentlyDamagedByPlayerOpponent(client)) {
                queuedJumpResetUntilTick = client.field_1724.field_6012 + 2;
            }
        }
        if (queuedJumpResetUntilTick >= 0 && SwordcoreClient.swordConfig.jumpResetting && client.field_1724.field_6012 <= queuedJumpResetUntilTick && client.field_1724.method_24828()) {
            client.field_1690.field_1903.method_23481(true);
            forcedJump = true;
            queuedJumpResetUntilTick = -1;
        } else if (forcedJump) {
            client.field_1690.field_1903.method_23481(false);
            forcedJump = false;
        }
        if (!(queuedJumpResetUntilTick < 0 || SwordcoreClient.swordConfig.jumpResetting && client.field_1724.field_6012 <= queuedJumpResetUntilTick)) {
            queuedJumpResetUntilTick = -1;
        }
        previousHealth = health;
    }

    private static boolean wasRecentlyDamagedByPlayerOpponent(class_310 client) {
        class_1657 attackerPlayer;
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1282 source = client.field_1724.method_6081();
        if (source == null) {
            return false;
        }
        class_1297 attacker = source.method_5529();
        if (!(attacker instanceof class_1657) || (attackerPlayer = (class_1657)attacker) == client.field_1724 || !attackerPlayer.method_5805()) {
            return false;
        }
        return client.field_1687 != null && client.field_1687.method_18456().contains(attackerPlayer);
    }

    private static void updateAdaptableEqualizerDamage(int tick) {
        if (SwordcoreClient.swordConfig.movementEqualizer != SwordcoreConfig.MovementEqualizerMode.ADAPTABLE_EXPERIMENTAL || targetPlayer == null) {
            return;
        }
        if (adaptableEqualizerJumping) {
            if (tick > adaptableEqualizerJumpUntilTick) {
                return;
            }
            if (++adaptableEqualizerJumpDamageCount >= 2) {
                adaptableEqualizerJumping = false;
                adaptableEqualizerJumpDamageCount = 0;
                adaptableEqualizerNoJumpDamageCount = 0;
                adaptableEqualizerJumpUntilTick = -1;
            }
            return;
        }
        if (++adaptableEqualizerNoJumpDamageCount >= 3) {
            adaptableEqualizerJumping = true;
            adaptableEqualizerJumpDamageCount = 0;
            adaptableEqualizerNoJumpDamageCount = 0;
        }
    }

    private static boolean isLikelyTargetHit(class_310 client) {
        return targetPlayer != null && client.field_1724 != null && targetPlayer.method_33571().method_1022(client.field_1724.method_33571()) <= (double)Math.max(SwordcoreClient.swordConfig.targetRange, SwordcoreClient.swordConfig.movementResistingRange + 1.5f);
    }

    private static void handleSneakWindow(class_310 client, int tick) {
        if (forcedSneak && tick > sneakUntilTick) {
            SwordcoreClient.setSneak(client, false);
            sneakUntilTick = -1;
        }
    }

    private static void updateTargetCooldown(class_310 client, int tick) {
        if (targetPlayer == null) {
            targetCooldownUntilTick = -1;
            lastTargetSwingProgress = 0.0f;
            lastTargetHandSwinging = false;
            lastTargetEatingOrDrinking = false;
            lastCooldownTarget = null;
            return;
        }
        if (targetPlayer != lastCooldownTarget) {
            lastCooldownTarget = targetPlayer;
            lastTargetSwingProgress = SwordcoreClient.targetPlayer.field_6251;
            lastTargetHandSwinging = SwordcoreClient.targetPlayer.field_6252;
            lastTargetEatingOrDrinking = SwordcoreClient.isEatingOrDrinking(targetPlayer);
            targetCooldownUntilTick = -1;
            return;
        }
        float swing = SwordcoreClient.targetPlayer.field_6251;
        boolean handSwingStarted = SwordcoreClient.targetPlayer.field_6252 && !lastTargetHandSwinging;
        boolean swingProgressStarted = swing > 0.0f && lastTargetSwingProgress <= 0.0f;
        boolean eatingOrDrinking = SwordcoreClient.isEatingOrDrinking(targetPlayer);
        if (handSwingStarted || swingProgressStarted) {
            targetCooldownUntilTick = tick + SwordcoreClient.estimateCooldownTicks(targetPlayer.method_6047());
            SwordcoreClient.playSound(client, class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"block.bell.use")), 0.1f, 2.0f);
            SwordcoreClient.sendLog("\u00a71(MOVEMENT SYNC)\u00a77 Target missed a hit and entered cooldown. Inner forcefield reduced.", (class_1657)client.field_1724);
        } else if (eatingOrDrinking && !lastTargetEatingOrDrinking) {
            targetCooldownUntilTick = Math.max(targetCooldownUntilTick, tick + 20);
        }
        lastTargetSwingProgress = swing;
        lastTargetHandSwinging = SwordcoreClient.targetPlayer.field_6252;
        lastTargetEatingOrDrinking = eatingOrDrinking;
    }

    private static boolean isTargetOnCooldown(class_310 client, int tick) {
        if (targetPlayer == null) {
            return false;
        }
        if (SwordcoreClient.isEatingOrDrinking(targetPlayer)) {
            return true;
        }
        if (tick < targetCooldownUntilTick) {
            return true;
        }
        return targetPlayer.method_7261(0.0f) < 0.95f;
    }

    private static int estimateCooldownTicks(class_1799 stack) {
        if (SwordcoreClient.isSword(stack)) {
            return 13;
        }
        if (stack.method_7909() instanceof class_1743) {
            return 20;
        }
        return 10;
    }

    private static void setTargetFromAttack(class_310 client) {
        class_1657 attackedPlayer = SwordcoreClient.getLookedPlayer(client);
        if (attackedPlayer == null) {
            return;
        }
        SwordcoreClient.setTarget(attackedPlayer, client);
    }

    private static void syncMacecoreTargetFromBackend(class_310 client, int tick) {
        if (!SwordcoreClient.isMacecoreLoaded() || client == null || client.field_1724 == null || client.field_1687 == null) {
            return;
        }
        if (tick - lastManualAttackIntentTick > 5) {
            return;
        }
        try {
            class_1657 macecoreTarget;
            Object backendTarget;
            if (macecoreTargetEntityField == null) {
                Class<?> macecoreClientClass = Class.forName("thetruetrident.macecore.MacecoreClient");
                macecoreTargetEntityField = macecoreClientClass.getField("targetEntity");
            }
            if ((backendTarget = macecoreTargetEntityField.get(null)) instanceof class_1657 && (macecoreTarget = (class_1657)backendTarget) != client.field_1724 && macecoreTarget.method_5805() && client.field_1687.method_18456().contains(macecoreTarget)) {
                SwordcoreClient.setTarget(macecoreTarget, client);
            }
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            // empty catch block
        }
    }

    private static void setTarget(class_1657 player, class_310 client) {
        if (player != null && targetPlayer != player) {
            targetPlayer = player;
            targetCooldownUntilTick = -1;
            lastCooldownTarget = null;
            SwordcoreClient.resetComboCounter();
            SwordcoreClient.resetAdaptableEqualizer();
            SwordcoreClient.resetAdaptableNeutral();
            SwordcoreClient.playMasterSound(client, class_3417.field_38830, 0.1f, 0.0f);
        }
    }

    private static void validateTarget(class_310 client) {
        if (targetPlayer == null) {
            return;
        }
        if (!targetPlayer.method_5805() || targetPlayer.method_7325() || client.field_1687 == null || !client.field_1687.method_18456().contains(targetPlayer)) {
            targetPlayer = null;
            SwordcoreClient.resetComboCounter();
            SwordcoreClient.clearTargetMovementState(client);
        }
    }

    private static void clearTargetMovementState(class_310 client) {
        SwordcoreClient.releaseMovementKeys(client);
        SwordcoreClient.releaseResistKeys(client);
        sTapUntilTick = -1;
        movementResistFeedbackActive = false;
        movementSyncInnerFeedbackActive = false;
        lastMovementSyncTarget = null;
        SwordcoreClient.resetAdaptableEqualizer();
        SwordcoreClient.resetAdaptableNeutral();
        aggressiveMovementJumpActive = false;
        aggressiveMovementJumpUntilTick = -1;
        SwordcoreClient.clearSprintHitFollowThrough();
        airGateFeedbackActive = false;
        movementEqualizerFeedbackActive = false;
    }

    private static void resetAdaptableEqualizer() {
        adaptableEqualizerJumping = true;
        adaptableEqualizerJumpDamageCount = 0;
        adaptableEqualizerNoJumpDamageCount = 0;
        adaptableEqualizerJumpUntilTick = -1;
    }

    private static void resetAdaptableNeutral() {
        adaptableNeutralHabitScore = 0;
        lastAdaptableNeutralBehavior = 0;
    }

    private static class_1657 getLookedPlayer(class_310 client) {
        class_1657 lookedPlayer;
        class_1297 looked = client.field_1692;
        if (looked instanceof class_1657 && (lookedPlayer = (class_1657)looked) != client.field_1724 && lookedPlayer.method_5805()) {
            return lookedPlayer;
        }
        return null;
    }

    private static boolean attackTarget(class_310 client, class_1657 target, int tick, String log) {
        if (client.field_1761 == null || tick == lastAttackTick || target == null || !target.method_5805()) {
            return false;
        }
        return SwordcoreClient.attackTargetNow(client, target, tick, log);
    }

    private static boolean attackTargetNow(class_310 client, class_1657 target, int tick, String log) {
        if (client.field_1761 == null || tick == lastAttackTick || target == null || !target.method_5805()) {
            return false;
        }
        return SwordcoreClient.sendValidatedAttack(client, target, tick, log);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean sendValidatedAttack(class_310 client, class_1657 target, int tick, String log) {
        if (!attackInputPhase || client.field_1724 == null || client.field_1761 == null || client.field_1755 != null || client.field_1724.method_6115() || tick == lastAttackTick || !(client instanceof AttackInvoker)) {
            return false;
        }
        AttackInvoker accessor = (AttackInvoker)client;
        dispatchedAttackTarget = null;
        lastAttackTick = tick;
        swordcoreSendingAttack = true;
        try {
            accessor.swordcore$callDoAttack();
        }
        finally {
            swordcoreSendingAttack = false;
        }
        class_1297 class_12972 = dispatchedAttackTarget;
        if (!(class_12972 instanceof class_1657)) {
            return false;
        }
        class_1657 clickedTarget = (class_1657)class_12972;
        SwordcoreClient.startSTap(clickedTarget, tick);
        SwordcoreClient.registerComboHit(client, clickedTarget, tick);
        if (log != null) {
            SwordcoreClient.sendLog(log, (class_1657)client.field_1724);
        }
        return true;
    }

    private static boolean isRememberedTargetReachValidated(class_310 client, class_1297 target) {
        double closestZ;
        double dz;
        double closestY;
        double dy;
        if (client == null || client.field_1724 == null || client.field_1687 == null || target == null || !target.method_5805()) {
            return false;
        }
        double reach = client.field_1724.method_55755() + 0.15;
        class_243 eye = client.field_1724.method_33571();
        class_238 box = target.method_5829();
        double closestX = Math.max(box.field_1323, Math.min(eye.field_1352, box.field_1320));
        double dx = eye.field_1352 - closestX;
        return dx * dx + (dy = eye.field_1351 - (closestY = Math.max(box.field_1322, Math.min(eye.field_1351, box.field_1325)))) * dy + (dz = eye.field_1350 - (closestZ = Math.max(box.field_1321, Math.min(eye.field_1350, box.field_1324)))) * dz <= reach * reach;
    }

    private static boolean isFreshReachValidatedTarget(class_310 client, class_1297 requiredTarget) {
        if (client == null || client.field_1724 == null || client.field_1687 == null || requiredTarget == null) {
            return false;
        }
        class_239 class_2392 = client.field_1765;
        if (!(class_2392 instanceof class_3966)) {
            return false;
        }
        class_3966 hitResult = (class_3966)class_2392;
        class_1297 target = hitResult.method_17782();
        if (target != requiredTarget || !target.method_5805()) {
            return false;
        }
        double reach = client.field_1724.method_55755();
        double reachWithTolerance = reach + 0.05;
        return client.field_1724.method_33571().method_1025(hitResult.method_17784()) <= reachWithTolerance * reachWithTolerance;
    }

    private static void registerComboHit(class_310 client, class_1657 target, int tick) {
        if (swordConfig == null || !SwordcoreClient.swordConfig.comboCounter || client.field_1724 == null || target == null || target != targetPlayer || tick == lastComboHitTick) {
            return;
        }
        lastComboHitTick = tick;
        float pitch = 1.0f + (float)(++comboCount - 1) * 0.1f;
        SwordcoreClient.playSound(client, class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"block.note_block.bit")), 1.0f, pitch);
        client.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\ue81b Combo Counter: " + comboCount)), true);
    }

    private static void resetComboCounter() {
        comboCount = 0;
        lastComboHitTick = -100;
    }

    private static void startSTap(class_1657 target, int tick) {
        if (swordConfig == null || !SwordcoreClient.swordConfig.sTapping || SwordcoreClient.swordConfig.sTapLength <= 0 || target == null || target != targetPlayer) {
            return;
        }
        sTapUntilTick = tick + SwordcoreClient.swordConfig.sTapLength;
    }

    private static void queueSprintHitFollowThrough(class_310 client, class_1657 target, int tick, boolean sprintHitIntent) {
        if (!(SwordcoreClient.swordConfig.sprintHitFollowThrough && SwordcoreClient.swordConfig.movementSyncing && SwordcoreClient.canUseJumpFollowing() && target != null && target == targetPlayer)) {
            return;
        }
        if (!sprintHitIntent || !SwordcoreClient.isSword(client.field_1724.method_6047())) {
            return;
        }
        if (SwordcoreClient.isInsideMovementSyncInner(client)) {
            return;
        }
        sprintHitFollowJumpTick = tick + SwordcoreClient.swordConfig.sprintHitJumpDelay;
        sprintHitFollowUntilTick = tick + 8;
    }

    private static boolean isInsideMovementSyncInner(class_310 client) {
        return client.field_1724 != null && targetPlayer != null && targetPlayer.method_33571().method_1022(client.field_1724.method_33571()) <= (double)SwordcoreClient.swordConfig.movementSyncInnerRange;
    }

    private static void clearSprintHitFollowThrough() {
        sprintHitFollowUntilTick = -1;
        sprintHitFollowJumpTick = -1;
    }

    private static boolean hasSprintHitIntent(class_310 client) {
        if (client.field_1724 == null) {
            return false;
        }
        return client.field_1724.method_5624() || client.field_1690.field_1867.method_1434() || SwordcoreClient.swordConfig.autoWTapAutosprint && client.field_1690.field_1894.method_1434() && !client.field_1724.method_5715();
    }

    private static boolean isSTapping(int tick) {
        return tick < sTapUntilTick;
    }

    private static boolean canHit(class_310 client, class_1657 target) {
        return SwordcoreClient.isPlayerAttackReady(client) && target != null && client.field_1724.method_5739((class_1297)target) <= SwordcoreClient.swordConfig.targetRange;
    }

    private static boolean canSmartSwordHit(class_310 client, class_1657 target) {
        if (!SwordcoreClient.canHit(client, target)) {
            return false;
        }
        if (!premiumFeaturesEnabled && nonPremiumSmartSwordHits >= 5) {
            SwordcoreClient.disableSmartSwordForNonPremium(client);
            return false;
        }
        return !SwordcoreClient.isFallingForPotentialCrit(client) || SwordcoreClient.isCritTimingReady(client);
    }

    private static void registerSmartSwordHit(class_310 client) {
        if (premiumFeaturesEnabled) {
            return;
        }
        if (++nonPremiumSmartSwordHits >= 5) {
            SwordcoreClient.disableSmartSwordForNonPremium(client);
        }
    }

    private static void disableSmartSwordForNonPremium(class_310 client) {
        if (!SwordcoreClient.swordConfig.smartSword) {
            return;
        }
        SwordcoreClient.swordConfig.smartSword = false;
        smartSwordActive = false;
        previousSmartSwordEnabled = false;
        swordConfig.save();
        if (client.field_1724 != null) {
            client.field_1724.method_7353(SwordcoreClient.linkUrls("You can not use smartsword for more than 5 hits, as you are using swordcore non-premium. You can get the full version on https://thetruetrident.com/swordcore"), false);
        }
    }

    private static boolean isPlayerAttackReady(class_310 client) {
        float cooldown = client.field_1724.method_7261(0.0f);
        return cooldown >= 1.0f || client.field_1724.method_18798().field_1351 < -0.1 && cooldown >= 0.9f;
    }

    private static boolean isFallingForPotentialCrit(class_310 client) {
        return !client.field_1724.method_24828() && client.field_1724.method_18798().field_1351 < -0.02;
    }

    private static boolean isCritTimingReady(class_310 client) {
        return client.field_1724.field_6017 > 0.0 && !client.field_1724.method_24828() && !client.field_1724.method_6101() && !client.field_1724.method_5799() && !client.field_1724.method_5765() && !client.field_1724.method_5624();
    }

    public static boolean swapSlot(class_1657 player, String itemName, int tick) {
        if (tick == lastSwapTick) {
            return false;
        }
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack = player.method_31548().method_5438(slot);
            String path = stack.method_7909().toString();
            if (stack.method_7960() || !path.contains(itemName)) continue;
            player.method_31548().method_61496(slot);
            lastSwapTick = tick;
            return true;
        }
        return false;
    }

    private static void scheduleSwordRestore(int swordSlot, int tick) {
        pendingSwordRestoreSlot = swordSlot;
        pendingSwordRestoreTick = tick + ThreadLocalRandom.current().nextInt(1, 3);
    }

    private static boolean restoreSwordSlot(class_1657 player, int tick) {
        if (tick == lastSwapTick) {
            return false;
        }
        if (pendingSwordRestoreSlot >= 0 && pendingSwordRestoreSlot < 9 && SwordcoreClient.isSword(player.method_31548().method_5438(pendingSwordRestoreSlot))) {
            player.method_31548().method_61496(pendingSwordRestoreSlot);
            lastSwapTick = tick;
            return true;
        }
        return SwordcoreClient.swapSlot(player, "_sword", tick);
    }

    public static void sendLog(String text, class_1657 player) {
        if (SwordcoreClient.isEnabled() && SwordcoreClient.swordConfig.chatLogs && player != null) {
            player.method_7353((class_2561)class_2561.method_43470((String)(LOG_PREFIX + text + " \u00a78Chat logs can be disabled in swordcore premium..")), false);
        }
    }

    private static class_2561 linkUrls(String text) {
        class_5250 message = class_2561.method_43473();
        int index = 0;
        while (true) {
            UrlMatch match;
            int urlIndex;
            int n = urlIndex = (match = SwordcoreClient.nextUrl(text, index)) == null ? -1 : match.index();
            if (urlIndex < 0) {
                message.method_10852((class_2561)class_2561.method_43470((String)text.substring(index)));
                return message;
            }
            message.method_10852((class_2561)class_2561.method_43470((String)text.substring(index, urlIndex)));
            message.method_10852((class_2561)class_2561.method_43470((String)match.label()).method_27694(style -> style.method_10958((class_2558)new class_2558.class_10608(URI.create(match.url()))).method_30938(Boolean.valueOf(true))));
            index = urlIndex + match.matchedText().length();
        }
    }

    private static UrlMatch nextUrl(String text, int fromIndex) {
        UrlMatch swordcoreHttps = SwordcoreClient.urlMatch(text, fromIndex, "https://thetruetrident.com/swordcore", "thetruetrident.com/swordcore", "https://thetruetrident.com/swordcore");
        UrlMatch swordcorePlain = SwordcoreClient.urlMatch(text, fromIndex, "thetruetrident.com/swordcore", "thetruetrident.com/swordcore", "https://thetruetrident.com/swordcore");
        UrlMatch patreon = SwordcoreClient.urlMatch(text, fromIndex, "https://www.patreon.com/c/thetruetrident", "https://www.patreon.com/c/thetruetrident", "https://www.patreon.com/c/thetruetrident");
        UrlMatch best = SwordcoreClient.earlier(swordcoreHttps, swordcorePlain);
        return SwordcoreClient.earlier(best, patreon);
    }

    private static UrlMatch urlMatch(String text, int fromIndex, String matchedText, String label, String url) {
        int index = text.indexOf(matchedText, fromIndex);
        return index < 0 ? null : new UrlMatch(index, matchedText, label, url);
    }

    private static UrlMatch earlier(UrlMatch first, UrlMatch second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.index() <= second.index() ? first : second;
    }

    private static void playMovementResistFeedback(class_310 client) {
        if (movementResistFeedbackActive) {
            return;
        }
        SwordcoreClient.playSound(client, class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"weather.end_flash")), 1.0f, 2.0f);
        SwordcoreClient.sendLog("\u00a71(MOVEMENT RESIST)\u00a77 Stopped moving towards target since they arent in a cooldown. Sneak to override and move closer.", (class_1657)client.field_1724);
        movementResistFeedbackActive = true;
    }

    private static void playMovementSyncGateFeedback(class_310 client, String message, boolean equalizer) {
        if (equalizer) {
            if (movementEqualizerFeedbackActive) {
                return;
            }
            movementEqualizerFeedbackActive = true;
        } else {
            if (airGateFeedbackActive) {
                return;
            }
            airGateFeedbackActive = true;
        }
        if (equalizer) {
            SwordcoreClient.playSound(client, class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"entity.wolf_big.death")), 1.0f, 0.0f);
        } else {
            SwordcoreClient.playSound(client, class_3414.method_47908((class_2960)class_2960.method_60655((String)"minecraft", (String)"entity.bee.sting")), 1.0f, 2.0f);
        }
        SwordcoreClient.sendLog("\u00a71(MOVEMENT SYNC)\u00a77 " + message, (class_1657)client.field_1724);
    }

    private static void playSound(class_310 client, class_3414 sound, float volume, float pitch) {
        if (SwordcoreClient.isEnabled() && SwordcoreClient.swordConfig.soundEffects && client.field_1724 != null) {
            client.field_1724.method_5783(sound, volume, pitch);
        }
    }

    private static void playMasterSound(class_310 client, class_3414 sound, float volume, float pitch) {
        if (SwordcoreClient.isEnabled() && SwordcoreClient.swordConfig.soundEffects && client.field_1687 != null) {
            client.field_1687.method_67392(sound, class_3419.field_15250, volume, pitch);
        }
    }

    private static boolean isSword(class_1799 stack) {
        return String.valueOf(stack.method_7909()).contains("_sword");
    }

    private static boolean canUseMovementFeatures(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        if (SwordcoreClient.isActuallyAirborne(client)) {
            return false;
        }
        return !SwordcoreClient.swordConfig.itemGate || SwordcoreClient.isMovementGateItem(client.field_1724.method_6047());
    }

    private static boolean isActuallyAirborne(class_310 client) {
        return !client.field_1724.method_24828() && client.field_1724.field_6017 > 0.0;
    }

    private static boolean isMovementGateItem(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        String itemName = String.valueOf(stack.method_7909());
        return itemName.contains("_sword") || stack.method_7909() instanceof class_1743 || itemName.contains("spear");
    }

    private static boolean isSmartSwordSwapItem(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        String itemName = String.valueOf(stack.method_7909());
        return itemName.contains("_sword") || stack.method_7909() == class_1802.field_8547 || itemName.contains("spear");
    }

    private static boolean isMacecoreLoaded() {
        return FabricLoader.getInstance().isModLoaded("mace-utils") || FabricLoader.getInstance().isModLoaded("macecore");
    }

    private static boolean hasHotbarItem(class_1657 player, String itemName) {
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack = player.method_31548().method_5438(slot);
            if (stack.method_7960() || !String.valueOf(stack.method_7909()).contains(itemName)) continue;
            return true;
        }
        return false;
    }

    private static boolean isShieldBlocking(class_1657 player) {
        return player.method_6039() && player.method_6030().method_31574(class_1802.field_8255);
    }

    private static boolean isPlayerUsingShieldOrEating(class_310 client) {
        if (client.field_1724 == null || !client.field_1724.method_6115()) {
            return false;
        }
        class_1799 activeStack = client.field_1724.method_6030();
        class_1839 useAction = activeStack.method_7976();
        return activeStack.method_31574(class_1802.field_8255) || useAction == class_1839.field_8949 || useAction == class_1839.field_8950 || useAction == class_1839.field_8946;
    }

    private static boolean isEatingOrDrinking(class_1657 player) {
        if (player == null || !player.method_6115()) {
            return false;
        }
        class_1839 useAction = player.method_6030().method_7976();
        return useAction == class_1839.field_8950 || useAction == class_1839.field_8946;
    }

    private static class_243 forwardVector(float yaw) {
        double yawRad = Math.toRadians(yaw);
        return new class_243(-Math.sin(yawRad), 0.0, Math.cos(yawRad));
    }

    private static class_243 rightVector(float yaw) {
        double yawRad = Math.toRadians(yaw);
        return new class_243(Math.cos(yawRad), 0.0, Math.sin(yawRad));
    }

    private static void setForward(class_310 client, boolean down) {
        client.field_1690.field_1894.method_23481(down);
        forcedForward = down;
    }

    private static void setBack(class_310 client, boolean down) {
        client.field_1690.field_1881.method_23481(down);
        forcedBack = down;
    }

    private static void setLeft(class_310 client, boolean down) {
        client.field_1690.field_1913.method_23481(down);
        forcedLeft = down;
    }

    private static void setRight(class_310 client, boolean down) {
        client.field_1690.field_1849.method_23481(down);
        forcedRight = down;
    }

    private static void setSneak(class_310 client, boolean down) {
        client.field_1690.field_1832.method_23481(down);
        forcedSneak = down;
    }

    private static void releaseMovementKeys(class_310 client) {
        if (forcedForward) {
            client.field_1690.field_1894.method_23481(false);
            forcedForward = false;
        }
        if (forcedBack) {
            client.field_1690.field_1881.method_23481(false);
            forcedBack = false;
        }
        if (forcedLeft) {
            client.field_1690.field_1913.method_23481(false);
            forcedLeft = false;
        }
        if (forcedRight) {
            client.field_1690.field_1849.method_23481(false);
            forcedRight = false;
        }
    }

    private static void setResistedForward(class_310 client, boolean down) {
        client.field_1690.field_1894.method_23481(!down && SwordcoreClient.isPhysicallyDown(client, client.field_1690.field_1894));
        resistedForward = down;
    }

    private static void setResistedBack(class_310 client, boolean down) {
        client.field_1690.field_1881.method_23481(!down && SwordcoreClient.isPhysicallyDown(client, client.field_1690.field_1881));
        resistedBack = down;
    }

    private static void setResistedLeft(class_310 client, boolean down) {
        client.field_1690.field_1913.method_23481(!down && SwordcoreClient.isPhysicallyDown(client, client.field_1690.field_1913));
        resistedLeft = down;
    }

    private static void setResistedRight(class_310 client, boolean down) {
        client.field_1690.field_1849.method_23481(!down && SwordcoreClient.isPhysicallyDown(client, client.field_1690.field_1849));
        resistedRight = down;
    }

    private static void releaseResistKeys(class_310 client) {
        if (resistedForward) {
            SwordcoreClient.setResistedForward(client, false);
        }
        if (resistedBack) {
            SwordcoreClient.setResistedBack(client, false);
        }
        if (resistedLeft) {
            SwordcoreClient.setResistedLeft(client, false);
        }
        if (resistedRight) {
            SwordcoreClient.setResistedRight(client, false);
        }
    }

    private static boolean isPhysicallyDown(class_310 client, class_304 keyBinding) {
        class_3675.class_306 boundKey = class_3675.method_15981((String)keyBinding.method_1428());
        return class_3675.method_15987((class_1041)client.method_22683(), (int)boundKey.method_1444());
    }

    private static void releaseForcedKeys(class_310 client) {
        SwordcoreClient.releaseMovementKeys(client);
        SwordcoreClient.releaseResistKeys(client);
        if (forcedSneak) {
            client.field_1690.field_1832.method_23481(false);
            forcedSneak = false;
        }
        if (forcedJump) {
            client.field_1690.field_1903.method_23481(false);
            forcedJump = false;
        }
        SwordcoreClient.releaseJumpFollowing(client);
        SwordcoreClient.releaseSprint(client);
    }

    private static void releaseJumpFollowing(class_310 client) {
        if (forcedJumpFollowing) {
            client.field_1690.field_1903.method_23481(false);
            forcedJumpFollowing = false;
        }
    }

    private static void releaseSprint(class_310 client) {
        if (forcedSprint) {
            client.field_1690.field_1867.method_23481(SwordcoreClient.isPhysicallyDown(client, client.field_1690.field_1867));
            forcedSprint = false;
        }
    }

    private static void resetTransientState() {
        lastAttackTick = -100;
        attackInputPhase = false;
        swordcoreSendingAttack = false;
        targetPlayer = null;
        pendingStunTarget = null;
        pendingStunHitTick = -1;
        pendingSwordRestoreTick = -1;
        pendingSwordRestoreSlot = -1;
        sTapUntilTick = -1;
        SwordcoreClient.resetComboCounter();
        sneakUntilTick = -1;
        previousAttackDown = false;
        previousHealth = -1.0f;
        queuedJumpResetUntilTick = -1;
        smartSwordActive = false;
        swordcoreOwnsAttack = false;
        targetCooldownUntilTick = -1;
        lastTargetSwingProgress = 0.0f;
        lastTargetHandSwinging = false;
        lastCooldownTarget = null;
        lastMovementSyncTarget = null;
        movementSyncInnerFeedbackActive = false;
        SwordcoreClient.resetAdaptableEqualizer();
        SwordcoreClient.resetAdaptableNeutral();
        aggressiveMovementJumpActive = false;
        aggressiveMovementJumpUntilTick = -1;
        SwordcoreClient.clearSprintHitFollowThrough();
        airGateFeedbackActive = false;
        movementEqualizerFeedbackActive = false;
        forcedSprint = false;
        forcedJumpFollowing = false;
        resistedForward = false;
        resistedBack = false;
        resistedLeft = false;
        resistedRight = false;
    }

    static {
        premiumFeaturesEnabled = SwordcoreVariant.isPremium();
        lastAttackTick = -100;
        lastManualAttackIntentTick = -100;
        lastSwapTick = -100;
        pendingStunHitTick = -1;
        pendingSwordRestoreTick = -1;
        pendingSwordRestoreSlot = -1;
        sTapUntilTick = -1;
        lastComboHitTick = -100;
        sneakUntilTick = -1;
        previousHealth = -1.0f;
        queuedJumpResetUntilTick = -1;
        adaptableEqualizerJumping = true;
        adaptableEqualizerJumpUntilTick = -1;
        aggressiveMovementJumpUntilTick = -1;
        sprintHitFollowUntilTick = -1;
        sprintHitFollowJumpTick = -1;
        targetCooldownUntilTick = -1;
    }

    @Environment(value=EnvType.CLIENT)
    private record UrlMatch(int index, String matchedText, String label, String url) {
    }
}

