/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 */
package com.hookaddon.modules;

import com.hookaddon.HookAddon;
import com.hookaddon.finder.BlockFinder;
import com.hookaddon.mining.AreaGeometry;
import com.hookaddon.mining.AreaMinerEngine;
import com.hookaddon.mining.BorePlanner;
import com.hookaddon.mining.MiningMode;
import com.hookaddon.mining.MiningPattern;
import com.hookaddon.mining.PickaxeWidth;
import com.hookaddon.nav.Navigator;
import com.hookaddon.task.Capability;
import com.hookaddon.task.ConflictPolicy;
import com.hookaddon.task.MiningController;
import com.hookaddon.task.TaskManager;
import com.hookaddon.util.HookStatus;
import com.hookaddon.util.InvGuards;
import hook.compat.meteorclient.events.game.GameLeftEvent;
import hook.compat.meteorclient.events.render.Render3DEvent;
import hook.compat.meteorclient.events.world.TickEvent;
import hook.compat.meteorclient.renderer.ShapeMode;
import hook.compat.meteorclient.settings.BlockListSetting;
import hook.compat.meteorclient.settings.BlockPosSetting;
import hook.compat.meteorclient.settings.BoolSetting;
import hook.compat.meteorclient.settings.ColorSetting;
import hook.compat.meteorclient.settings.DoubleSetting;
import hook.compat.meteorclient.settings.EnumSetting;
import hook.compat.meteorclient.settings.IntSetting;
import hook.compat.meteorclient.settings.KeybindSetting;
import hook.compat.meteorclient.settings.Setting;
import hook.compat.meteorclient.settings.SettingGroup;
import hook.compat.meteorclient.systems.modules.Module;
import hook.compat.meteorclient.utils.misc.Keybind;
import hook.compat.meteorclient.utils.render.color.Color;
import hook.compat.meteorclient.utils.render.color.SettingColor;
import hook.compat.orbit.EventHandler;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;

public class AutoAreaMiner
extends Module {
    private final SettingGroup sgArea;
    private final SettingGroup sgFilters;
    private final SettingGroup sgMining;
    private final SettingGroup sgSafety;
    private final SettingGroup sgRender;
    private final SettingGroup sgKeys;
    private final Setting<MiningMode> mode;
    private final Setting<Integer> boreWidth;
    private final Setting<Integer> boreHeight;
    private final Setting<Integer> boreDepth;
    private final Setting<Integer> boreYOffset;
    private final Setting<Boolean> boreContinue;
    private final Setting<Integer> findRadius;
    private final Setting<Integer> findHeight;
    private final Setting<Integer> findBudget;
    private final Setting<Integer> findCooldown;
    private final Setting<Boolean> usePlayerPos;
    private final Setting<class_2338> origin;
    private final Setting<Integer> width;
    private final Setting<Integer> height;
    private final Setting<Integer> depth;
    private final Setting<MiningPattern> pattern;
    private final Setting<Integer> stripWidth;
    private final Setting<Integer> stripGap;
    private final Setting<Integer> maxOperationSize;
    private final Setting<Boolean> autoStart;
    private final Setting<ConflictPolicy> conflictPolicy;
    private final Setting<List<class_2248>> ignoreBlocks;
    private final Setting<List<class_2248>> mineBlocks;
    private final Setting<Double> reach;
    private final Setting<Boolean> navigate;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> swing;
    private final Setting<PickaxeWidth> pickaxe;
    private final Setting<Boolean> autoTool;
    private final Setting<Integer> minToolDurability;
    private final Setting<Integer> delayBetweenBlocks;
    private final Setting<Integer> maxRetries;
    private final Setting<Boolean> stopWhenFull;
    private final Setting<Boolean> avoidLava;
    private final Setting<Boolean> avoidFluids;
    private final Setting<Boolean> avoidFalling;
    private final Setting<Boolean> preview;
    private final Setting<SettingColor> boundsColor;
    private final Setting<SettingColor> targetColor;
    private final Setting<Integer> maxPreviewBlocks;
    private final Setting<Keybind> pauseKey;
    private final Setting<Keybind> resetKey;
    private final AreaMinerEngine engine;
    private final BlockFinder finder;
    private long nextFindAtMs;
    private boolean findCooldownScheduled;
    private long nextBoreAtMs;
    private class_2338 lastBoreAnchor;
    private String autoStatus;
    private int emptyScans;
    private boolean claimed;
    private boolean dirty;
    private boolean running;
    private boolean interrupted;

    public AutoAreaMiner() {
        super(HookAddon.CATEGORY, "auto-area-miner", "Mines a rectangular area layer by layer, in strips, tunnels or as a grid, with filters and safety checks.");
        this.sgArea = this.settings.getDefaultGroup();
        this.sgFilters = this.settings.createGroup("Filters");
        this.sgMining = this.settings.createGroup("Mining");
        this.sgSafety = this.settings.createGroup("Safety");
        this.sgRender = this.settings.createGroup("Render");
        this.sgKeys = this.settings.createGroup("Keys");
        this.mode = this.sgArea.add(new EnumSetting.Builder().name("mode").description("What to mine: a fixed area, blocks found around you, or a 3x3 bore forward").defaultValue(MiningMode.AREA).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.boreWidth = this.sgArea.add(new IntSetting.Builder().name("bore-width").description("Face size across the direction you travel").defaultValue(3).range(1.0, 7.0).visible(this::isBore).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.boreHeight = this.sgArea.add(new IntSetting.Builder().name("bore-height").description("Face height along Y").defaultValue(3).range(1.0, 7.0).visible(this::isBore).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.boreDepth = this.sgArea.add(new IntSetting.Builder().name("bore-depth").description("Slices to mine forward per segment").defaultValue(8).range(1.0, 32.0).visible(this::isBore).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.boreYOffset = this.sgArea.add(new IntSetting.Builder().name("bore-y-offset").description("Vertical shift of the bore face relative to your feet").defaultValue(0).range(-8.0, 16.0).visible(this::isBore).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.boreContinue = this.sgArea.add(new BoolSetting.Builder().name("bore-continue").description("Keep boring new segments forward as each one finishes").defaultValue(true).visible(this::isBore).build());
        this.findRadius = this.sgArea.add(new IntSetting.Builder().name("find-radius").description("Horizontal search radius of the finder in blocks, loaded chunks only").defaultValue(32).sliderRange(8.0, 128.0).visible(this::isFinder).build());
        this.findHeight = this.sgArea.add(new IntSetting.Builder().name("find-height").description("Blocks above and below your position the finder looks at").defaultValue(16).sliderRange(4.0, 64.0).visible(this::isFinder).build());
        this.findBudget = this.sgArea.add(new IntSetting.Builder().name("find-budget-per-tick").description("Maximum positions the finder inspects each tick, higher is faster but laggier").defaultValue(4000).sliderRange(500.0, 16000.0).visible(this::isFinder).build());
        this.findCooldown = this.sgArea.add(new IntSetting.Builder().name("find-cooldown").description("Seconds between two finder scans").defaultValue(5).sliderRange(0.0, 120.0).visible(this::isFinder).build());
        this.usePlayerPos = this.sgArea.add(new BoolSetting.Builder().name("use-my-position").description("Use your feet as the area origin, otherwise the origin setting").defaultValue(true).visible(this::isArea).build());
        this.origin = this.sgArea.add(new BlockPosSetting.Builder().name("origin").description("Minimum corner of the area").defaultValue(class_2338.field_10980).visible(() -> this.isArea() && this.usePlayerPos.get() == false).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.width = this.sgArea.add(new IntSetting.Builder().name("width").description("Size along X").defaultValue(8).range(1.0, 64.0).visible(this::isArea).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.height = this.sgArea.add(new IntSetting.Builder().name("height").description("Size along Y").defaultValue(4).range(1.0, 64.0).visible(this::isArea).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.depth = this.sgArea.add(new IntSetting.Builder().name("depth").description("Size along Z").defaultValue(8).range(1.0, 64.0).visible(this::isArea).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.pattern = this.sgArea.add(new EnumSetting.Builder().name("pattern").description("Traversal order and which blocks are touched").defaultValue(MiningPattern.LAYER_BY_LAYER).visible(this::isArea).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.stripWidth = this.sgArea.add(new IntSetting.Builder().name("strip-width").description("Blocks kept per strip").defaultValue(3).range(1.0, 16.0).visible(() -> this.isArea() && this.pattern.get() == MiningPattern.STRIP).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.stripGap = this.sgArea.add(new IntSetting.Builder().name("strip-gap").description("Blocks left between two strips or corridors").defaultValue(2).range(0.0, 16.0).visible(() -> this.isArea() && (this.pattern.get() == MiningPattern.STRIP || this.pattern.get() == MiningPattern.TUNNEL)).onChanged(value -> {
            this.dirty = true;
        }).build());
        this.maxOperationSize = this.sgArea.add(new IntSetting.Builder().name("max-operation-size").description("Refuse to start an area larger than this many blocks").defaultValue(32768).sliderRange(64.0, 262144.0).build());
        this.autoStart = this.sgArea.add(new BoolSetting.Builder().name("auto-start").description("Start mining immediately when the module is enabled").defaultValue(true).build());
        this.conflictPolicy = this.sgArea.add(new EnumSetting.Builder().name("conflict-policy").description("What to do when another Hook module already owns movement or mining").defaultValue(ConflictPolicy.REJECT).build());
        this.ignoreBlocks = this.sgFilters.add(new BlockListSetting.Builder().name("ignore-blocks").description("Blocks that are never mined").defaultValue(class_2246.field_10034, class_2246.field_10380, class_2246.field_16328, class_2246.field_10260, class_2246.field_10443).build());
        this.mineBlocks = this.sgFilters.add(new BlockListSetting.Builder().name("mine-blocks").description("Blocks to mine, empty means every block that is not ignored").defaultValue(new class_2248[0]).build());
        this.reach = this.sgMining.add(new DoubleSetting.Builder().name("reach").description("Interaction distance in blocks").defaultValue(4.5).range(3.0, 6.0).build());
        this.navigate = this.sgMining.add(new BoolSetting.Builder().name("navigate").description("Walk to blocks that are out of reach").defaultValue(true).build());
        this.rotate = this.sgMining.add(new BoolSetting.Builder().name("rotate").description("Look at the block while mining").defaultValue(true).build());
        this.swing = this.sgMining.add(new BoolSetting.Builder().name("swing").description("Swing the hand while mining").defaultValue(true).build());
        this.pickaxe = this.sgMining.add(new EnumSetting.Builder().name("pickaxe").description("1x1 mines only the current block; a 3x3 swing also clears the eight same-layer neighbours inside the area (mined one by one, vanilla cannot break several blocks at once)").defaultValue(PickaxeWidth.ONE).build());
        this.autoTool = this.sgMining.add(new BoolSetting.Builder().name("auto-tool").description("Select the fastest tool for the block").defaultValue(true).build());
        this.minToolDurability = this.sgMining.add(new IntSetting.Builder().name("min-tool-durability").description("Do not use a tool with fewer durability points left").defaultValue(10).range(0.0, 500.0).visible(this.autoTool::get).build());
        this.delayBetweenBlocks = this.sgMining.add(new IntSetting.Builder().name("delay-between-blocks").description("Ticks to wait after each block").defaultValue(1).range(0.0, 40.0).build());
        this.maxRetries = this.sgMining.add(new IntSetting.Builder().name("max-retries").description("Extra passes over blocks that failed").defaultValue(1).range(0.0, 5.0).build());
        this.stopWhenFull = this.sgMining.add(new BoolSetting.Builder().name("stop-when-inventory-full").description("Pause when no empty slot is left").defaultValue(true).build());
        this.avoidLava = this.sgSafety.add(new BoolSetting.Builder().name("avoid-lava").description("Skip blocks touching lava").defaultValue(true).build());
        this.avoidFluids = this.sgSafety.add(new BoolSetting.Builder().name("avoid-fluids").description("Skip blocks touching any fluid").defaultValue(false).build());
        this.avoidFalling = this.sgSafety.add(new BoolSetting.Builder().name("avoid-falling-blocks").description("Skip blocks with sand or gravel above them").defaultValue(true).build());
        this.preview = this.sgRender.add(new BoolSetting.Builder().name("preview").description("Render the area bounds and the remaining blocks").defaultValue(true).build());
        this.boundsColor = this.sgRender.add(new ColorSetting.Builder().name("bounds-color").description("Colour of the area outline").defaultValue(new SettingColor(157, 89, 248, 200)).visible(this.preview::get).build());
        this.targetColor = this.sgRender.add(new ColorSetting.Builder().name("target-color").description("Colour of the current target").defaultValue(new SettingColor(64, 220, 255, 200)).visible(this.preview::get).build());
        this.maxPreviewBlocks = this.sgRender.add(new IntSetting.Builder().name("max-preview-blocks").description("Upper bound on how many pending blocks are drawn").defaultValue(256).sliderRange(16.0, 2048.0).visible(this.preview::get).build());
        this.pauseKey = this.sgKeys.add(new KeybindSetting.Builder().name("pause-key").description("Pause and resume the current run").defaultValue(Keybind.none()).action(this::togglePause).build());
        this.resetKey = this.sgKeys.add(new KeybindSetting.Builder().name("reset-key").description("Throw away the current run and rebuild the plan").defaultValue(Keybind.none()).action(this::resetRun).build());
        this.finder = new BlockFinder();
        this.autoStatus = "idle";
        this.engine = new AreaMinerEngine(this.name, this);
    }

    public AreaMinerEngine engine() {
        return this.engine;
    }

    private boolean isArea() {
        return this.mode.get() == MiningMode.AREA;
    }

    private boolean isFinder() {
        return this.mode.get() == MiningMode.FINDER;
    }

    private boolean isBore() {
        return this.mode.get() == MiningMode.BORE;
    }

    @Override
    public void onActivate() {
        if (!this.ensureClaim()) {
            return;
        }
        this.dirty = false;
        this.interrupted = false;
        this.findCooldownScheduled = false;
        this.nextFindAtMs = 0L;
        this.emptyScans = 0;
        if (!this.prepareArea()) {
            this.disable();
            return;
        }
        if (this.isFinder()) {
            this.finder.clearVisited();
            this.running = this.autoStart.get();
            if (this.running) {
                this.autoStatus = "finder: looking for blocks to mine";
            } else {
                this.autoStatus = "finder: waiting to start";
                this.info("Auto-target ready. Run '.hook area start' or press the module key to begin the scan.", new Object[0]);
            }
            return;
        }
        if (this.isBore()) {
            this.running = this.autoStart.get();
            if (this.running) {
                this.autoStatus = "bore: ready";
                this.planBoreSegment();
            } else {
                this.autoStatus = "bore: waiting to start";
                this.info("Bore ready. Run '.hook area start' or press the module key to begin.", new Object[0]);
            }
            return;
        }
        if (this.autoStart.get().booleanValue()) {
            this.startRun();
        } else {
            this.info("Area ready. Press the module key again or run '.hook area start' to begin.", new Object[0]);
        }
    }

    @Override
    public void onDeactivate() {
        this.engine.shutdown();
        this.finder.stop();
        if (this.claimed) {
            TaskManager.get().release(this.name);
            this.claimed = false;
        }
        this.running = false;
        HookStatus.reset("area-miner");
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

    public boolean prepareArea() {
        int areaDepth;
        int areaHeight;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            this.error("Join a world before preparing an area.", new Object[0]);
            return false;
        }
        if (this.isFinder() || this.isBore()) {
            this.engine.setMaxRetries(this.maxRetries.get());
            this.engine.reset();
            return true;
        }
        class_2338 areaOrigin = this.usePlayerPos.get() != false ? this.mc.field_1724.method_24515() : this.origin.get();
        int areaWidth = this.width.get();
        int candidateCount = AreaGeometry.count(areaWidth, areaHeight = this.height.get().intValue(), areaDepth = this.depth.get().intValue(), this.pattern.get(), this.stripWidth.get(), this.stripGap.get());
        if (candidateCount > this.maxOperationSize.get()) {
            this.error("That area touches %d blocks, above the max-operation-size of %d. Shrink the area or raise the limit.", candidateCount, this.maxOperationSize.get());
            return false;
        }
        this.engine.setMaxRetries(this.maxRetries.get());
        this.engine.prepare(areaOrigin, areaWidth, areaHeight, areaDepth, this.pattern.get(), this.stripWidth.get(), this.stripGap.get());
        this.info("Area ready at (%d, %d, %d): %d x %d x %d, %s, %d candidate blocks.", areaOrigin.method_10263(), areaOrigin.method_10264(), areaOrigin.method_10260(), areaWidth, areaHeight, areaDepth, this.pattern.get().title, candidateCount);
        return true;
    }

    public boolean startRun() {
        if (this.isFinder()) {
            this.running = true;
            this.nextFindAtMs = 0L;
            this.autoStatus = "finder: looking for blocks to mine";
            HookStatus.reset("area-miner");
            this.info("Finder scan requested within %d blocks.", this.findRadius.get());
            return true;
        }
        if (this.isBore()) {
            this.running = true;
            this.nextBoreAtMs = 0L;
            this.autoStatus = "bore: ready";
            HookStatus.reset("area-miner");
            this.planBoreSegment();
            this.info("Bore started: %d x %d face, %d slices forward.", this.boreWidth.get(), this.boreHeight.get(), this.boreDepth.get());
            return true;
        }
        if (this.engine.order() == null && !this.prepareArea()) {
            return false;
        }
        if (this.engine.phase() == AreaMinerEngine.Phase.COMPLETE) {
            this.engine.reset();
        }
        this.engine.setMaxRetries(this.maxRetries.get());
        this.engine.start();
        this.running = true;
        HookStatus.reset("area-miner");
        this.info("Mining started: %s.", this.engine.describe());
        return true;
    }

    public void togglePause() {
        if (!this.isActive()) {
            return;
        }
        if (this.engine.phase() == AreaMinerEngine.Phase.RUNNING) {
            this.engine.pause("paused by keybind");
            this.info("Area mining paused: %s.", this.engine.describe());
        } else if (this.engine.phase() == AreaMinerEngine.Phase.PAUSED) {
            this.engine.resume();
            this.running = true;
            this.info("Area mining resumed.", new Object[0]);
        } else {
            this.startRun();
        }
    }

    public void stopRun() {
        if (!this.isActive()) {
            return;
        }
        if (this.isFinder()) {
            this.running = false;
            this.finder.stop();
            this.autoStatus = "finder: stopped";
        } else if (this.isBore()) {
            this.running = false;
            this.autoStatus = "bore: stopped";
        }
        this.engine.pause("stopped by user");
        this.info("Area mining stopped: %s.", this.engine.describe());
    }

    public void resetRun() {
        if (!this.isActive()) {
            return;
        }
        this.engine.reset();
        if (this.isFinder()) {
            this.finder.clearVisited();
            this.finder.stop();
            this.nextFindAtMs = 0L;
            this.findCooldownScheduled = false;
            this.running = true;
            this.autoStatus = "finder: looking for blocks to mine";
            this.info("Finder state cleared, starting a fresh scan.", new Object[0]);
            return;
        }
        if (this.isBore()) {
            this.running = true;
            this.nextBoreAtMs = 0L;
            this.autoStatus = "bore: ready";
            this.planBoreSegment();
            this.info("Bore plan rebuilt, no progress kept.", new Object[0]);
            return;
        }
        if (this.prepareArea()) {
            this.info("Plan rebuilt, no progress kept.", new Object[0]);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (!this.ensureClaim()) {
            return;
        }
        if (this.interrupted) {
            this.interrupted = false;
            if (this.isFinder()) {
                this.finder.stop();
            }
            HookStatus.info(this, "Reconnected, resuming the area miner: %s.", this.engine.describe());
        }
        if (this.engine.phase() == AreaMinerEngine.Phase.PAUSED && this.engine.isInventoryPaused() && !InvGuards.inventoryFull()) {
            this.engine.resume();
            HookStatus.info(this, "Inventory has space again, resuming the area miner.", new Object[0]);
        }
        if (this.dirty && (this.engine.phase() != AreaMinerEngine.Phase.RUNNING || this.isFinder() || this.isBore())) {
            this.dirty = false;
            this.engine.reset();
            if (this.prepareArea()) {
                this.info("Settings changed, plan rebuilt.", new Object[0]);
            }
            return;
        }
        if (this.isFinder()) {
            this.tickAutoTarget();
        } else if (this.isBore()) {
            this.tickBore();
        }
        this.engine.tick(this.engineOptions(), this::shouldMine, this::shouldIgnore);
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        Navigator.get().stop(this.name);
        MiningController.get().reset(this.name);
        if (this.claimed) {
            TaskManager.get().release(this.name);
            this.claimed = false;
        }
        this.running = false;
        this.interrupted = true;
    }

    private AreaMinerEngine.Options engineOptions() {
        return new AreaMinerEngine.Options(this.reach.get(), this.navigate.get(), this.rotate.get(), this.swing.get(), this.autoTool.get(), this.minToolDurability.get(), this.delayBetweenBlocks.get(), this.maxRetries.get(), this.stopWhenFull.get(), this.avoidLava.get(), this.avoidFluids.get(), this.avoidFalling.get(), this.pickaxe.get());
    }

    private BlockFinder.Options finderOptions() {
        return new BlockFinder.Options(this.findRadius.get(), this.findHeight.get(), this.findBudget.get(), this.maxOperationSize.get(), true, 250000);
    }

    private void tickAutoTarget() {
        boolean idle;
        long now = System.currentTimeMillis();
        if (this.finder.isRunning()) {
            BlockFinder.Result result = this.finder.tick(this.finderOptions(), this::shouldMine, this::shouldIgnore);
            switch (result.outcome()) {
                case SCANNING: {
                    this.autoStatus = "finder: scanning, " + this.finder.scanned() + " positions checked";
                    break;
                }
                case TARGET: {
                    this.finder.stop();
                    this.startBatch(this.finder.matches());
                    break;
                }
                case NO_TARGET: {
                    this.finder.stop();
                    this.autoStatus = "finder: nothing to mine nearby, retrying in " + String.valueOf(this.findCooldown.get()) + "s";
                    this.nextFindAtMs = now + (long)this.findCooldown.get().intValue() * 1000L;
                    ++this.emptyScans;
                    if (this.emptyScans != 1) break;
                    HookStatus.progressNow(this, "area-finder", "Finder checked %d positions and found nothing in the loaded chunks; it keeps retrying every %ds.", this.finder.scanned(), this.findCooldown.get());
                }
            }
            return;
        }
        if (this.engine.phase() == AreaMinerEngine.Phase.COMPLETE) {
            if (!this.findCooldownScheduled) {
                this.findCooldownScheduled = true;
                this.nextFindAtMs = now + (long)this.findCooldown.get().intValue() * 1000L;
                this.autoStatus = "finder: batch finished, rescanning soon";
            }
        } else {
            this.findCooldownScheduled = false;
        }
        boolean bl = idle = this.engine.phase() == AreaMinerEngine.Phase.IDLE || this.engine.phase() == AreaMinerEngine.Phase.COMPLETE;
        if ((this.autoStart.get().booleanValue() || this.running) && idle && now >= this.nextFindAtMs && this.mc.field_1724 != null) {
            this.finder.start(this.finderOptions(), this.mc.field_1724.method_24515(), false);
            this.findCooldownScheduled = false;
            this.autoStatus = "finder: scanning, 0 positions checked";
        }
    }

    private void startBatch(List<class_2338> found) {
        if (found == null || found.isEmpty()) {
            return;
        }
        class_2338 min = found.get(0);
        class_2338 max = found.get(0);
        for (class_2338 pos : found) {
            min = new class_2338(Math.min(min.method_10263(), pos.method_10263()), Math.min(min.method_10264(), pos.method_10264()), Math.min(min.method_10260(), pos.method_10260()));
            max = new class_2338(Math.max(max.method_10263(), pos.method_10263()), Math.max(max.method_10264(), pos.method_10264()), Math.max(max.method_10260(), pos.method_10260()));
        }
        if (found.size() >= this.maxOperationSize.get()) {
            this.warning("The finder hit max-operation-size (%d blocks); mining the %d nearest ones.", this.maxOperationSize.get(), found.size());
        }
        this.engine.setMaxRetries(this.maxRetries.get());
        this.engine.prepareFromPositions(min, max.method_10263() - min.method_10263() + 1, max.method_10264() - min.method_10264() + 1, max.method_10260() - min.method_10260() + 1, found);
        this.engine.start();
        this.running = true;
        this.findCooldownScheduled = false;
        this.emptyScans = 0;
        this.autoStatus = "mining " + found.size() + " found blocks";
        HookStatus.reset("area-miner");
        this.info("Finder located %d blocks to mine, starting the run.", found.size());
    }

    private void tickBore() {
        if (this.engine.phase() != AreaMinerEngine.Phase.COMPLETE) {
            return;
        }
        if (!this.boreContinue.get().booleanValue() || !this.running) {
            return;
        }
        if (this.mc.field_1724 == null) {
            return;
        }
        if (System.currentTimeMillis() < this.nextBoreAtMs) {
            return;
        }
        if (this.lastBoreAnchor != null && this.lastBoreAnchor.equals((Object)this.mc.field_1724.method_24515())) {
            return;
        }
        this.autoStatus = "bore: extending the tunnel";
        this.planBoreSegment();
    }

    private void planBoreSegment() {
        class_2338 feet;
        boolean alongX;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        double radians = Math.toRadians(this.mc.field_1724.method_36454());
        double lookX = -Math.sin(radians);
        double lookZ = Math.cos(radians);
        boolean bl = alongX = Math.abs(lookX) > Math.abs(lookZ);
        int sign = alongX ? (lookX >= 0.0 ? 1 : -1) : (lookZ >= 0.0 ? 1 : -1);
        int[] order = BorePlanner.plan(this.boreWidth.get(), this.boreHeight.get(), this.boreDepth.get(), sign, this.boreYOffset.get());
        this.lastBoreAnchor = feet = this.mc.field_1724.method_24515();
        ArrayList<class_2338> positions = new ArrayList<class_2338>(order.length / 3);
        class_2338 min = null;
        class_2338 max = null;
        for (int i = 0; i < order.length; i += 3) {
            int forward = order[i];
            int cross = order[i + 1];
            int up = order[i + 2];
            int x = feet.method_10263() + (alongX ? forward : cross);
            int z = feet.method_10260() + (alongX ? cross : forward);
            int y = feet.method_10264() + up;
            class_2338 pos = new class_2338(x, y, z);
            positions.add(pos);
            if (min == null) {
                min = pos;
                max = pos;
                continue;
            }
            min = new class_2338(Math.min(min.method_10263(), x), Math.min(min.method_10264(), y), Math.min(min.method_10260(), z));
            max = new class_2338(Math.max(max.method_10263(), x), Math.max(max.method_10264(), y), Math.max(max.method_10260(), z));
        }
        if (min == null || positions.isEmpty()) {
            return;
        }
        this.engine.setMaxRetries(this.maxRetries.get());
        this.engine.prepareFromPositions(min, max.method_10263() - min.method_10263() + 1, max.method_10264() - min.method_10264() + 1, max.method_10260() - min.method_10260() + 1, positions);
        this.engine.start();
        this.running = true;
        this.nextBoreAtMs = System.currentTimeMillis() + 250L;
        this.autoStatus = "bore: mining " + positions.size() + " blocks ahead";
        HookStatus.reset("area-miner");
    }

    private boolean shouldMine(class_2248 block) {
        List<class_2248> allowed = this.mineBlocks.get();
        if (allowed == null || allowed.isEmpty()) {
            return true;
        }
        return allowed.contains(block);
    }

    private boolean shouldIgnore(class_2248 block) {
        List<class_2248> ignored = this.ignoreBlocks.get();
        return ignored != null && ignored.contains(block);
    }

    @EventHandler
    private void onRender3d(Render3DEvent event) {
        if (!this.preview.get().booleanValue() || this.engine.origin() == null || this.engine.order() == null) {
            return;
        }
        if (this.isArea()) {
            class_2338 originPos = this.engine.origin();
            class_238 bounds = new class_238((double)originPos.method_10263(), (double)originPos.method_10264(), (double)originPos.method_10260(), (double)(originPos.method_10263() + this.engine.width()), (double)(originPos.method_10264() + this.engine.height()), (double)(originPos.method_10260() + this.engine.depth()));
            event.renderer.box(bounds, (Color)this.boundsColor.get(), (Color)this.boundsColor.get(), ShapeMode.Lines, 0.0);
        }
        List<class_2338> pending = this.engine.pendingPositions();
        int limit = Math.min(pending.size(), this.maxPreviewBlocks.get());
        for (int i = 0; i < limit; ++i) {
            event.renderer.box(pending.get(i), (Color)this.targetColor.get(), (Color)this.targetColor.get(), ShapeMode.Lines, 0.0);
        }
        class_2338 current = this.engine.currentTarget();
        if (current != null) {
            event.renderer.box(current, Color.WHITE, (Color)this.targetColor.get(), ShapeMode.Both, 0.0);
        }
    }

    @Override
    public String getInfoString() {
        if (this.isFinder() || this.isBore()) {
            return this.engine.phase() == AreaMinerEngine.Phase.RUNNING ? this.engine.describe() : this.autoStatus;
        }
        return this.engine.order() == null ? null : this.engine.describe();
    }

    public String describeFull() {
        if (this.isFinder()) {
            String scan = this.finder.isRunning() ? "scanning, " + this.finder.scanned() + " positions checked" : this.autoStatus;
            return "mode=auto-target, " + scan + (String)(this.engine.order() == null ? "" : ", " + this.engine.describe());
        }
        if (this.isBore()) {
            return "mode=3x3-bore, " + this.autoStatus + (String)(this.engine.order() == null ? "" : ", " + this.engine.describe());
        }
        if (this.engine.order() == null) {
            return "no area prepared";
        }
        AreaMinerEngine.Status status = this.engine.status();
        return "phase=" + String.valueOf((Object)status.phase()) + ", " + this.engine.describe() + ", attempted=" + status.attempted() + ", retryPass=" + status.retryPass() + ", " + status.message();
    }

    public void reportInfo(String message, Object ... args) {
        this.info(message, args);
    }

    public void reportError(String message, Object ... args) {
        this.error(message, args);
    }
}

