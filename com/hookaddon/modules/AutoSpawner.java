/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1299
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 */
package com.hookaddon.modules;

import com.hookaddon.HookAddon;
import com.hookaddon.nav.Navigator;
import com.hookaddon.persist.HookStore;
import com.hookaddon.spawner.RtpFallback;
import com.hookaddon.spawner.SpawnerSearcher;
import com.hookaddon.spawner.SpawnerTarget;
import com.hookaddon.task.Capability;
import com.hookaddon.task.ConflictPolicy;
import com.hookaddon.task.MiningController;
import com.hookaddon.task.TaskManager;
import com.hookaddon.util.HookStatus;
import com.hookaddon.util.SafetyChecks;
import hook.compat.meteorclient.events.game.GameLeftEvent;
import hook.compat.meteorclient.events.game.ReceiveMessageEvent;
import hook.compat.meteorclient.events.render.Render3DEvent;
import hook.compat.meteorclient.events.world.TickEvent;
import hook.compat.meteorclient.renderer.ShapeMode;
import hook.compat.meteorclient.settings.BoolSetting;
import hook.compat.meteorclient.settings.ColorSetting;
import hook.compat.meteorclient.settings.DoubleSetting;
import hook.compat.meteorclient.settings.EntityTypeListSetting;
import hook.compat.meteorclient.settings.EnumSetting;
import hook.compat.meteorclient.settings.IntSetting;
import hook.compat.meteorclient.settings.KeybindSetting;
import hook.compat.meteorclient.settings.Setting;
import hook.compat.meteorclient.settings.SettingGroup;
import hook.compat.meteorclient.settings.StringListSetting;
import hook.compat.meteorclient.settings.StringSetting;
import hook.compat.meteorclient.systems.modules.Module;
import hook.compat.meteorclient.utils.misc.Keybind;
import hook.compat.meteorclient.utils.render.color.Color;
import hook.compat.meteorclient.utils.render.color.SettingColor;
import hook.compat.orbit.EventHandler;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1299;
import net.minecraft.class_2246;
import net.minecraft.class_2338;

public class AutoSpawner
extends Module {
    private final SettingGroup sgSearch;
    private final SettingGroup sgRtp;
    private final SettingGroup sgMovement;
    private final SettingGroup sgSafety;
    private final SettingGroup sgRender;
    private final SettingGroup sgKeys;
    private final Setting<Set<class_1299<?>>> spawnerTypes;
    private final Setting<Boolean> onlySelected;
    private final Setting<Integer> searchRadius;
    private final Setting<Integer> searchHeight;
    private final Setting<Integer> budgetPerTick;
    private final Setting<Integer> searchTimeout;
    private final Setting<Integer> searchAttempts;
    private final Setting<Integer> searchCooldown;
    private final Setting<Boolean> skipVisited;
    private final Setting<NoTargetAction> onNoTarget;
    private final Setting<Boolean> rtpEnabled;
    private final Setting<String> rtpCommand;
    private final Setting<Integer> rtpRetryDelay;
    private final Setting<Integer> rtpMaxAttempts;
    private final Setting<Integer> rtpTimeout;
    private final Setting<Integer> rtpPostDelay;
    private final Setting<Double> rtpMinDistance;
    private final Setting<Boolean> rtpPauseAfterFailures;
    private final Setting<List<String>> rtpFailureKeywords;
    private final Setting<Boolean> navigate;
    private final Setting<Double> reach;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> autoTool;
    private final Setting<Integer> minToolDurability;
    private final Setting<Integer> mineTimeout;
    private final Setting<Integer> maxNavigationRecoveries;
    private final Setting<Integer> maxTargetFailures;
    private final Setting<Boolean> avoidLava;
    private final Setting<Boolean> avoidFalling;
    private final Setting<ConflictPolicy> conflictPolicy;
    private final Setting<Boolean> renderTarget;
    private final Setting<SettingColor> targetColor;
    private final Setting<Keybind> stopKey;
    private final SpawnerSearcher searcher;
    private final RtpFallback rtp;
    private Phase phase;
    private SpawnerTarget target;
    private class_2338 mineTarget;
    private int attempts;
    private int consecutiveTargetFailures;
    private int searchTicks;
    private int candidateTicks;
    private long searchStartedAtMs;
    private long nextSearchAtMs;
    private int postRtpTicks;
    private int saveCounter;
    private String status;
    private boolean claimed;
    private boolean interrupted;

    public AutoSpawner() {
        super(HookAddon.CATEGORY, "auto-spawner", "Finds spawners in loaded chunks, mines them and falls back to a teleport command when the area is exhausted.");
        this.sgSearch = this.settings.getDefaultGroup();
        this.sgRtp = this.settings.createGroup("RTP Fallback");
        this.sgMovement = this.settings.createGroup("Movement & Mining");
        this.sgSafety = this.settings.createGroup("Safety");
        this.sgRender = this.settings.createGroup("Render");
        this.sgKeys = this.settings.createGroup("Keys");
        this.spawnerTypes = this.sgSearch.add(new EntityTypeListSetting.Builder().name("spawner-types").description("Spawner types to search for; uncheck a type to ignore it").defaultValue(class_1299.field_6051, class_1299.field_6137, class_1299.field_6079, class_1299.field_6046, class_1299.field_6084, class_1299.field_6099, class_1299.field_6102, class_1299.field_6069, class_1299.field_6145, class_1299.field_6125, class_1299.field_6091, class_1299.field_6105, class_1299.field_6117, class_1299.field_6090, class_1299.field_6118, class_1299.field_6123, class_1299.field_6098, class_1299.field_6071, class_1299.field_6078, class_1299.field_6107).build());
        this.onlySelected = this.sgSearch.add(new BoolSetting.Builder().name("only-selected-types").description("Ignore spawners whose mob is not in the list above").defaultValue(true).build());
        this.searchRadius = this.sgSearch.add(new IntSetting.Builder().name("search-radius").description("Horizontal search radius in blocks, loaded chunks only").defaultValue(48).sliderRange(8.0, 128.0).build());
        this.searchHeight = this.sgSearch.add(new IntSetting.Builder().name("search-height").description("Blocks to scan above and below your position").defaultValue(24).sliderRange(8.0, 64.0).build());
        this.budgetPerTick = this.sgSearch.add(new IntSetting.Builder().name("budget-per-tick").description("Maximum loaded chunks inspected each tick, higher is faster but laggier").defaultValue(64).sliderRange(1.0, 512.0).build());
        this.searchTimeout = this.sgSearch.add(new IntSetting.Builder().name("search-timeout").description("Seconds before a search is considered exhausted").defaultValue(90).sliderRange(10.0, 600.0).build());
        this.searchAttempts = this.sgSearch.add(new IntSetting.Builder().name("max-search-attempts").description("Searches in one place before the no-target action runs").defaultValue(3).range(1.0, 50.0).build());
        this.searchCooldown = this.sgSearch.add(new IntSetting.Builder().name("search-cooldown").description("Seconds between two searches in the same area").defaultValue(5).sliderRange(0.0, 120.0).build());
        this.skipVisited = this.sgSearch.add(new BoolSetting.Builder().name("skip-visited").description("Do not report spawner positions that were already inspected in this session").defaultValue(true).build());
        this.onNoTarget = this.sgSearch.add(new EnumSetting.Builder().name("on-no-target").description("What to do when the search area is exhausted").defaultValue(NoTargetAction.RTP).build());
        this.rtpEnabled = this.sgRtp.add(new BoolSetting.Builder().name("rtp-enabled").description("Allow the module to run a teleport command when nothing is found").defaultValue(true).visible(() -> this.onNoTarget.get() == NoTargetAction.RTP).build());
        this.rtpCommand = this.sgRtp.add(new StringSetting.Builder().name("rtp-command").description("Command sent to teleport, must match what the server accepts").defaultValue("/rtp").visible(() -> this.onNoTarget.get() == NoTargetAction.RTP && this.rtpEnabled.get() != false).build());
        this.rtpRetryDelay = this.sgRtp.add(new IntSetting.Builder().name("rtp-retry-delay").description("Minimum seconds between two teleport attempts").defaultValue(60).sliderRange(5.0, 600.0).visible(() -> this.onNoTarget.get() == NoTargetAction.RTP && this.rtpEnabled.get() != false).build());
        this.rtpMaxAttempts = this.sgRtp.add(new IntSetting.Builder().name("rtp-max-attempts").description("Consecutive failed attempts before the fallback pauses").defaultValue(3).range(1.0, 20.0).visible(() -> this.onNoTarget.get() == NoTargetAction.RTP && this.rtpEnabled.get() != false).build());
        this.rtpTimeout = this.sgRtp.add(new IntSetting.Builder().name("rtp-timeout").description("Seconds to wait for the position to change after sending the command").defaultValue(30).sliderRange(5.0, 120.0).visible(() -> this.onNoTarget.get() == NoTargetAction.RTP && this.rtpEnabled.get() != false).build());
        this.rtpPostDelay = this.sgRtp.add(new IntSetting.Builder().name("rtp-after-teleport-delay").description("Seconds to wait after a confirmed teleport before searching again").defaultValue(4).sliderRange(0.0, 30.0).visible(() -> this.onNoTarget.get() == NoTargetAction.RTP && this.rtpEnabled.get() != false).build());
        this.rtpMinDistance = this.sgRtp.add(new DoubleSetting.Builder().name("rtp-min-distance").description("Blocks you must move for the teleport to count as confirmed").defaultValue(32.0).range(4.0, 256.0).visible(() -> this.onNoTarget.get() == NoTargetAction.RTP && this.rtpEnabled.get() != false).build());
        this.rtpPauseAfterFailures = this.sgRtp.add(new BoolSetting.Builder().name("rtp-pause-after-failures").description("Stop trying once the attempt limit is reached instead of retrying forever").defaultValue(true).visible(() -> this.onNoTarget.get() == NoTargetAction.RTP && this.rtpEnabled.get() != false).build());
        this.rtpFailureKeywords = this.sgRtp.add(new StringListSetting.Builder().name("rtp-failure-keywords").description("Chat keywords that mark a teleport attempt as denied").defaultValue("cooldown", "permission", "cannot", "can't", "not allowed", "must wait", "denied", "insufficient", "not available").visible(() -> this.onNoTarget.get() == NoTargetAction.RTP && this.rtpEnabled.get() != false).build());
        this.navigate = this.sgMovement.add(new BoolSetting.Builder().name("navigate").description("Walk towards a confirmed spawner").defaultValue(true).build());
        this.reach = this.sgMovement.add(new DoubleSetting.Builder().name("reach").description("Interaction distance in blocks").defaultValue(4.5).range(3.0, 6.0).build());
        this.rotate = this.sgMovement.add(new BoolSetting.Builder().name("rotate").description("Look at the spawner while mining it").defaultValue(true).build());
        this.swing = this.sgMovement.add(new BoolSetting.Builder().name("swing").description("Swing the hand while mining").defaultValue(true).build());
        this.autoTool = this.sgMovement.add(new BoolSetting.Builder().name("auto-tool").description("Use the fastest pickaxe for the spawner").defaultValue(true).build());
        this.minToolDurability = this.sgMovement.add(new IntSetting.Builder().name("min-tool-durability").description("Do not use a tool with fewer durability points left").defaultValue(10).range(0.0, 500.0).visible(this.autoTool::get).build());
        this.mineTimeout = this.sgMovement.add(new IntSetting.Builder().name("mine-timeout").description("Ticks a single spawner may take before the attempt is abandoned").defaultValue(300).range(40.0, 2000.0).build());
        this.maxNavigationRecoveries = this.sgMovement.add(new IntSetting.Builder().name("max-recoveries").description("Stuck recovery attempts before a target is abandoned").defaultValue(3).range(1.0, 10.0).build());
        this.maxTargetFailures = this.sgMovement.add(new IntSetting.Builder().name("max-target-failures").description("Targets that may fail in a row before the search restarts").defaultValue(3).range(1.0, 20.0).build());
        this.avoidLava = this.sgSafety.add(new BoolSetting.Builder().name("avoid-lava").description("Do not mine next to lava").defaultValue(true).build());
        this.avoidFalling = this.sgSafety.add(new BoolSetting.Builder().name("avoid-falling-blocks").description("Do not mine with sand or gravel above").defaultValue(true).build());
        this.conflictPolicy = this.sgSafety.add(new EnumSetting.Builder().name("conflict-policy").description("What to do when another Hook module already owns movement or mining").defaultValue(ConflictPolicy.REJECT).build());
        this.renderTarget = this.sgRender.add(new BoolSetting.Builder().name("render-target").description("Highlight the current spawner target").defaultValue(true).build());
        this.targetColor = this.sgRender.add(new ColorSetting.Builder().name("target-color").description("Colour of the highlighted spawner").defaultValue(new SettingColor(255, 90, 90, 200)).visible(this.renderTarget::get).build());
        this.stopKey = this.sgKeys.add(new KeybindSetting.Builder().name("stop-key").description("Stop searching and cancel any pending teleport").defaultValue(Keybind.none()).action(this::manualStop).build());
        this.searcher = new SpawnerSearcher();
        this.rtp = new RtpFallback();
        this.phase = Phase.SEARCHING;
        this.status = "starting";
    }

    @Override
    public void onActivate() {
        HookStore.ensureDirectories();
        if (!this.ensureClaim()) {
            return;
        }
        HookStore.SpawnerState saved = HookStore.loadSpawnerState();
        if (saved != null && saved.visited != null) {
            this.searcher.loadVisited(saved.visited);
        }
        if (saved != null) {
            this.rtp.restoreCounters(saved.rtpAttempts, saved.lastRtpAt);
        }
        this.attempts = 0;
        this.consecutiveTargetFailures = 0;
        this.target = null;
        this.mineTarget = null;
        this.phase = Phase.SEARCHING;
        this.interrupted = false;
        this.startSearch(false);
        HookStatus.reset("spawner");
        this.info("Searching for spawners in loaded chunks (radius %d, %d types).", this.searchRadius.get(), this.spawnerTypes.get().size());
    }

    @Override
    public void onDeactivate() {
        this.finish();
    }

    private void finish() {
        Navigator.get().stop(this.name);
        MiningController.get().reset(this.name);
        this.searcher.stop();
        this.saveState();
        TaskManager.get().release(this.name);
        this.claimed = false;
        this.rtp.reset();
        HookStatus.reset("spawner");
    }

    private void forceStop() {
        if (this.isActive()) {
            this.disable();
        }
    }

    private boolean ensureClaim() {
        if (this.claimed) {
            return true;
        }
        if (!this.isActive()) {
            return false;
        }
        TaskManager.ClaimResult claim = TaskManager.get().claim(this.name, this.title, EnumSet.of(Capability.MOVE, Capability.MINE), this.conflictPolicy.get(), this::forceStop);
        if (!claim.ok()) {
            this.error(claim.message(), new Object[0]);
            this.disable();
            return false;
        }
        this.claimed = true;
        return true;
    }

    private void resumeAfterReconnect() {
        this.interrupted = false;
        this.target = null;
        this.mineTarget = null;
        this.attempts = 0;
        this.consecutiveTargetFailures = 0;
        this.phase = Phase.SEARCHING;
        this.startSearch(false);
        HookStatus.info(this, "Reconnected, resuming the spawner search.", new Object[0]);
    }

    private void manualStop() {
        if (!this.isActive()) {
            return;
        }
        if (this.phase == Phase.RTP_SENT) {
            this.rtp.stop(this.rtpOptions(), x$0 -> this.info((String)x$0, new Object[0]));
        }
        this.target = null;
        this.mineTarget = null;
        this.phase = Phase.PAUSED;
        Navigator.get().stop(this.name);
        MiningController.get().reset(this.name);
        this.searcher.stop();
        this.info("Stopped. Toggle the module to start a new search.", new Object[0]);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (this.phase == Phase.PAUSED) {
            this.interrupted = false;
            if (this.claimed) {
                Navigator.get().stop(this.name);
                MiningController.get().reset(this.name);
                TaskManager.get().release(this.name);
                this.claimed = false;
            }
            return;
        }
        if (!this.ensureClaim()) {
            return;
        }
        if (this.interrupted) {
            this.resumeAfterReconnect();
        }
        switch (this.phase.ordinal()) {
            case 0: {
                this.tickSearching();
                break;
            }
            case 3: {
                this.tickWaitingForType();
                break;
            }
            case 1: {
                this.tickNavigating();
                break;
            }
            case 2: {
                this.tickMining();
                break;
            }
            case 4: {
                this.tickNoTarget();
                break;
            }
            case 5: {
                this.tickRtp();
                break;
            }
            case 6: {
                this.tickPostRtp();
                break;
            }
        }
        if (++this.saveCounter >= 200) {
            this.saveCounter = 0;
            this.saveState();
        }
    }

    private void tickSearching() {
        ++this.searchTicks;
        long elapsed = (System.currentTimeMillis() - this.searchStartedAtMs) / 1000L;
        if (elapsed >= (long)this.searchTimeout.get().intValue()) {
            this.handleNoTarget("search timeout after " + elapsed + "s");
            return;
        }
        SpawnerSearcher.Result result = this.searcher.tick(this.searchOptions());
        this.status = "scanning, " + this.searcher.scanned() + " chunks scanned";
        switch (result.outcome()) {
            case CONFIRMED: {
                this.target = result.target();
                this.phase = Phase.NAVIGATING;
                this.consecutiveTargetFailures = 0;
                HookStatus.progressNow(this, "spawner", "Found a %s at %s", this.describeType(this.target), AutoSpawner.format(this.target.pos()));
                this.saveState();
                break;
            }
            case CANDIDATE: {
                this.candidateTicks = 0;
                this.phase = Phase.WAITING_FOR_TYPE;
                this.status = "candidate at " + AutoSpawner.format(result.target().pos()) + ", waiting for its type";
                break;
            }
            case NO_TARGET: {
                if (this.searcher.lastCandidate() != null) {
                    this.target = this.searcher.lastCandidate();
                    this.candidateTicks = 0;
                    this.phase = Phase.WAITING_FOR_TYPE;
                    break;
                }
                this.handleNoTarget("area exhausted after " + this.searcher.scanned() + " chunk scans");
                break;
            }
            case SCANNING: {
                if (this.searchTicks % 100 != 0) break;
                HookStatus.progress(this, "spawner-search", "Searching... %d chunk scans, %ds left", this.searcher.scanned(), Math.max(0L, (long)this.searchTimeout.get().intValue() - elapsed));
            }
        }
    }

    private void tickWaitingForType() {
        if (this.target == null) {
            this.phase = Phase.SEARCHING;
            return;
        }
        if (!this.mc.field_1687.method_8320(this.target.pos()).method_27852(class_2246.field_10260)) {
            this.target = null;
            this.phase = Phase.SEARCHING;
            return;
        }
        if (++this.candidateTicks <= 100) {
            SpawnerSearcher.Result check = this.searcher.check(this.target.pos(), this.searchOptions());
            if (check.outcome() == SpawnerSearcher.Outcome.CONFIRMED) {
                this.target = check.target();
                this.phase = Phase.NAVIGATING;
                HookStatus.progressNow(this, "spawner", "Found a %s at %s", this.describeType(this.target), AutoSpawner.format(this.target.pos()));
                return;
            }
            this.status = "waiting for the spawner type (" + this.candidateTicks + " ticks)";
            return;
        }
        if (!this.onlySelected.get().booleanValue() || this.spawnerTypes.get().isEmpty()) {
            this.phase = Phase.NAVIGATING;
            return;
        }
        HookStatus.warn(this, "Spawner at %s did not report its type, skipping it.", AutoSpawner.format(this.target.pos()));
        this.target = null;
        this.candidateTicks = 0;
        this.phase = Phase.SEARCHING;
    }

    private void tickNavigating() {
        if (this.target == null) {
            this.phase = Phase.SEARCHING;
            return;
        }
        if (!this.mc.field_1687.method_8320(this.target.pos()).method_27852(class_2246.field_10260)) {
            HookStatus.info(this, "The spawner at %s disappeared before we got there.", AutoSpawner.format(this.target.pos()));
            this.target = null;
            this.phase = Phase.SEARCHING;
            this.startSearch(false);
            return;
        }
        if (!this.navigate.get().booleanValue()) {
            this.phase = Phase.MINING;
            return;
        }
        double within = Math.max(1.5, this.reach.get() - 0.5);
        Navigator.State state = Navigator.get().tick(this.name, this.target.pos(), new Navigator.Options(within, true, true, 80, this.maxNavigationRecoveries.get()));
        this.status = "walking to " + AutoSpawner.format(this.target.pos());
        switch (state) {
            case ARRIVED: {
                Navigator.get().stop(this.name);
                this.phase = Phase.MINING;
                break;
            }
            case FAILED: {
                if (++this.consecutiveTargetFailures >= this.maxTargetFailures.get()) {
                    this.giveUpTarget("cannot reach it repeatedly");
                    break;
                }
                HookStatus.warn(this, "Could not reach the spawner at %s, trying again.", AutoSpawner.format(this.target.pos()));
                Navigator.get().stop(this.name);
                this.candidateTicks = 0;
                break;
            }
        }
    }

    private void tickMining() {
        if (this.target == null) {
            this.phase = Phase.SEARCHING;
            return;
        }
        this.mineTarget = this.target.pos();
        if (SafetyChecks.nearLava(this.mineTarget) && this.avoidLava.get().booleanValue()) {
            this.giveUpTarget("it is next to lava");
            return;
        }
        MiningController.Options options = new MiningController.Options(this.reach.get(), this.rotate.get(), this.swing.get(), this.autoTool.get(), this.minToolDurability.get(), this.avoidLava.get(), false, this.avoidFalling.get(), this.mineTimeout.get(), 2);
        MiningController.Result result = MiningController.get().tick(this.name, this.mineTarget, options);
        switch (result) {
            case WORKING: {
                this.status = "mining the spawner at " + AutoSpawner.format(this.mineTarget);
                break;
            }
            case DONE: {
                HookStatus.progressNow(this, "spawner", "Mined the %s at %s.", this.describeType(this.target), AutoSpawner.format(this.mineTarget));
                this.target = null;
                this.mineTarget = null;
                this.consecutiveTargetFailures = 0;
                this.attempts = 0;
                this.saveState();
                this.phase = Phase.SEARCHING;
                this.startSearch(false);
                break;
            }
            case OUT_OF_REACH: {
                this.phase = this.navigate.get() != false ? Phase.NAVIGATING : Phase.MINING;
                break;
            }
            case UNSAFE: {
                this.giveUpTarget("it is unsafe to mine");
                break;
            }
            case FAILED: {
                this.giveUpTarget("mining timed out or was rejected");
                break;
            }
            case REJECTED: {
                this.status = "mining owned by another module";
            }
        }
    }

    private void tickNoTarget() {
        long now = System.currentTimeMillis();
        if (now < this.nextSearchAtMs) {
            long seconds = (this.nextSearchAtMs - now) / 1000L + 1L;
            this.status = "cooldown, next search in " + seconds + "s";
            return;
        }
        switch (this.onNoTarget.get().ordinal()) {
            case 0: {
                this.phase = Phase.SEARCHING;
                this.startSearch(true);
                break;
            }
            case 1: {
                this.phase = Phase.SEARCHING;
                this.startSearch(true);
                break;
            }
            case 2: {
                this.disable();
                break;
            }
            case 3: {
                this.phase = Phase.PAUSED;
                this.status = "paused after an exhausted search";
            }
        }
    }

    private void tickRtp() {
        RtpFallback.Event event = this.rtp.tick(this.rtpOptions(), x$0 -> this.info((String)x$0, new Object[0]));
        switch (event) {
            case CONFIRMED: {
                this.postRtpTicks = this.rtpPostDelay.get() * 20;
                this.phase = Phase.POST_RTP;
                this.status = "teleport confirmed, restarting the search";
                this.saveState();
                break;
            }
            case FAILED: 
            case PAUSED: {
                this.phase = event == RtpFallback.Event.PAUSED ? Phase.PAUSED : Phase.NO_TARGET;
                this.nextSearchAtMs = this.rtp.nextAttemptAtMs();
                this.status = event == RtpFallback.Event.PAUSED ? "RTP paused" : "waiting for the RTP cooldown";
                break;
            }
            default: {
                this.status = "waiting for the teleport";
            }
        }
    }

    private void tickPostRtp() {
        if (this.postRtpTicks-- > 0) {
            this.status = "settling after teleport";
            return;
        }
        this.searcher.clearVisited();
        this.attempts = 0;
        this.consecutiveTargetFailures = 0;
        this.target = null;
        this.phase = Phase.SEARCHING;
        this.startSearch(true);
        HookStatus.progressNow(this, "spawner", "New area reached, starting a fresh search.", new Object[0]);
    }

    private void startSearch(boolean resetVisited) {
        this.searcher.start(this.searchOptions(), this.mc.field_1724.method_24515(), resetVisited);
        this.searchStartedAtMs = System.currentTimeMillis();
        this.searchTicks = 0;
        this.candidateTicks = 0;
        this.status = "scanning";
        if (resetVisited) {
            HookStatus.reset("spawner-search");
        }
    }

    private void handleNoTarget(String reason) {
        this.searcher.stop();
        ++this.attempts;
        if (this.attempts < this.searchAttempts.get()) {
            this.nextSearchAtMs = System.currentTimeMillis() + (long)this.searchCooldown.get().intValue() * 1000L;
            this.phase = Phase.NO_TARGET;
            HookStatus.progress(this, "spawner-none", "Nothing found (%s). Attempt %d/%d, next search in %ds.", reason, this.attempts, this.searchAttempts.get(), this.searchCooldown.get());
            return;
        }
        HookStatus.warn(this, "No target after %d attempts: %s.", this.attempts, reason);
        switch (this.onNoTarget.get().ordinal()) {
            case 0: {
                this.beginRtp(reason);
                break;
            }
            case 1: {
                this.attempts = 0;
                this.nextSearchAtMs = System.currentTimeMillis() + (long)this.searchCooldown.get().intValue() * 1000L;
                this.phase = Phase.NO_TARGET;
                HookStatus.info(this, "Restarting the search in %ds.", this.searchCooldown.get());
                break;
            }
            case 2: {
                this.disable();
                break;
            }
            case 3: {
                this.phase = Phase.PAUSED;
                this.status = "paused, no target found";
            }
        }
    }

    private void beginRtp(String reason) {
        if (!this.rtpEnabled.get().booleanValue()) {
            this.phase = Phase.PAUSED;
            HookStatus.warn(this, "No target found (%s) and the RTP fallback is disabled, pausing.", reason);
            return;
        }
        if (this.rtp.request(reason, this.rtpOptions(), x$0 -> this.info((String)x$0, new Object[0]))) {
            this.phase = Phase.RTP_SENT;
            this.status = "waiting for teleport";
            this.saveState();
        } else {
            if (this.rtp.isPaused()) {
                this.phase = Phase.PAUSED;
                return;
            }
            this.phase = Phase.NO_TARGET;
            this.nextSearchAtMs = Math.max(this.nextSearchAtMs, this.rtp.nextAttemptAtMs());
        }
    }

    private void giveUpTarget(String reason) {
        HookStatus.warn(this, "Giving up on the spawner at %s: %s.", AutoSpawner.format(this.target.pos()), reason);
        this.target = null;
        this.mineTarget = null;
        Navigator.get().stop(this.name);
        MiningController.get().reset(this.name);
        if (++this.consecutiveTargetFailures >= this.maxTargetFailures.get()) {
            this.consecutiveTargetFailures = 0;
            this.attempts = this.searchAttempts.get();
            this.handleNoTarget("repeated target failures");
            return;
        }
        this.phase = Phase.SEARCHING;
        this.startSearch(false);
    }

    private void saveState() {
        HookStore.SpawnerState state = new HookStore.SpawnerState();
        state.dimension = this.mc.field_1687 == null ? null : this.mc.field_1687.method_27983().method_29177().toString();
        state.visited = new ArrayList<Long>(this.searcher.visited());
        state.rtpAttempts = this.rtp.consecutiveFailures();
        state.lastRtpAt = this.rtp.lastAttemptAtMs();
        state.lastStatus = this.status;
        HookStore.saveSpawnerState(state);
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (this.phase != Phase.RTP_SENT) {
            return;
        }
        this.rtp.onChatMessage(event.getMessage().getString(), this.rtpOptions(), this.rtpFailureKeys(), x$0 -> this.info((String)x$0, new Object[0]));
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        this.interrupted = true;
        this.finish();
    }

    @EventHandler
    private void onRender3d(Render3DEvent event) {
        if (!this.renderTarget.get().booleanValue() || this.target == null) {
            return;
        }
        event.renderer.box(this.target.pos(), (Color)this.targetColor.get(), (Color)this.targetColor.get(), ShapeMode.Lines, 0.0);
    }

    private SpawnerSearcher.Options searchOptions() {
        return new SpawnerSearcher.Options(this.searchRadius.get(), this.searchHeight.get(), this.budgetPerTick.get(), this.spawnerTypes.get(), this.onlySelected.get(), this.skipVisited.get(), 250000);
    }

    private RtpFallback.Options rtpOptions() {
        return new RtpFallback.Options(this.rtpEnabled.get(), this.rtpCommand.get(), this.rtpRetryDelay.get(), this.rtpMaxAttempts.get(), this.rtpTimeout.get(), this.rtpPostDelay.get(), this.rtpPauseAfterFailures.get(), this.rtpMinDistance.get());
    }

    private List<String> rtpFailureKeys() {
        List<String> value = this.rtpFailureKeywords.get();
        return value == null ? List.of() : value;
    }

    private static String format(class_2338 pos) {
        return "(" + pos.method_10263() + ", " + pos.method_10264() + ", " + pos.method_10260() + ")";
    }

    private String describeType(SpawnerTarget target) {
        if (target == null || target.type() == null) {
            return "spawner";
        }
        return target.type().method_5897().getString() + " spawner";
    }

    @Override
    public String getInfoString() {
        return this.status;
    }

    public String describeFull() {
        StringBuilder builder = new StringBuilder("phase=").append((Object)this.phase).append(", ").append(this.status);
        if (this.target != null) {
            builder.append(", target ").append(AutoSpawner.format(this.target.pos()));
        }
        builder.append(", attempts=").append(this.attempts);
        builder.append(", scanned=").append(this.searcher.scanned());
        builder.append(", rtp=").append((Object)this.rtp.state());
        return builder.toString();
    }

    public RtpFallback rtpFallback() {
        return this.rtp;
    }

    public SpawnerSearcher searcher() {
        return this.searcher;
    }

    public static enum NoTargetAction {
        RTP("Run the RTP fallback"),
        RESTART("Restart the search here"),
        DISABLE("Disable the module"),
        PAUSE("Pause until toggled again");

        public final String title;

        private NoTargetAction(String title) {
            this.title = title;
        }
    }

    private static enum Phase {
        SEARCHING,
        NAVIGATING,
        MINING,
        WAITING_FOR_TYPE,
        NO_TARGET,
        RTP_SENT,
        POST_RTP,
        PAUSED;

    }
}

