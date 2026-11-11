/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1792
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2680
 *  net.minecraft.class_2741
 *  net.minecraft.class_2769
 *  net.minecraft.class_310
 */
package com.hookaddon.schematic;

import com.hookaddon.nav.Navigator;
import com.hookaddon.schematic.PlacementPlanner;
import com.hookaddon.task.MiningController;
import com.hookaddon.task.RetryTracker;
import com.hookaddon.task.TaskManager;
import com.hookaddon.util.HookStatus;
import com.hookaddon.util.InvGuards;
import hook.compat.meteorclient.systems.modules.Module;
import hook.compat.meteorclient.utils.player.FindItemResult;
import hook.compat.meteorclient.utils.player.InvUtils;
import hook.compat.meteorclient.utils.world.BlockUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1792;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2769;
import net.minecraft.class_310;

public final class PlacementEngine {
    private static final class_310 mc = class_310.method_1551();
    private final String owner;
    private final Module reporter;
    private PlacementPlanner.Plan plan;
    private Phase phase = Phase.IDLE;
    private final Deque<Work> queue = new ArrayDeque<Work>();
    private int verifyIndex;
    private int verified;
    private int skipped;
    private int failed;
    private int placed;
    private int wrongBlocks;
    private int missingBlocks;
    private Work pending;
    private int confirmTicksLeft;
    private int attemptsOnCurrent;
    private int placeCooldown;
    private int supportAttempts;
    private class_2338 lastWorkPos;
    private long buildStartedAt;
    private int placedAtBuildStart;
    private String lastMessage = "idle";
    private final RetryTracker materialTracker = new RetryTracker();

    public PlacementEngine(String owner, Module reporter) {
        this.owner = owner;
        this.reporter = reporter;
    }

    public PlacementPlanner.Plan plan() {
        return this.plan;
    }

    public Phase phase() {
        return this.phase;
    }

    public class_2338 currentTarget() {
        if (this.pending != null) {
            return this.pending.pos();
        }
        Work head = this.queue.peek();
        return head == null ? null : head.pos();
    }

    public List<class_2338> pendingPositions() {
        ArrayList<class_2338> positions = new ArrayList<class_2338>(Math.min(this.queue.size(), 512));
        int index = 0;
        for (Work work : this.queue) {
            if (index++ >= 512) break;
            positions.add(work.pos());
        }
        if (this.pending != null) {
            positions.add(this.pending.pos());
        }
        return positions;
    }

    public void setPlan(PlacementPlanner.Plan newPlan) {
        this.plan = newPlan;
        this.phase = Phase.VERIFYING;
        this.queue.clear();
        this.verifyIndex = 0;
        this.verified = 0;
        this.skipped = 0;
        this.failed = 0;
        this.placed = 0;
        this.wrongBlocks = 0;
        this.missingBlocks = 0;
        this.pending = null;
        this.confirmTicksLeft = 0;
        this.attemptsOnCurrent = 0;
        this.placeCooldown = 0;
        this.lastWorkPos = null;
        this.buildStartedAt = 0L;
        this.placedAtBuildStart = 0;
        this.lastMessage = "verifying " + newPlan.targets().size() + " positions";
    }

    public void pause() {
        if (this.phase == Phase.BUILDING || this.phase == Phase.VERIFYING) {
            this.phase = Phase.PAUSED;
            this.lastMessage = "paused";
            Navigator.get().stop(this.owner);
        }
    }

    public void resume() {
        if (this.phase == Phase.PAUSED) {
            this.phase = this.verifyIndex < this.totalTargets() ? Phase.VERIFYING : Phase.BUILDING;
            this.lastMessage = "resumed";
        }
    }

    public void cancel() {
        this.phase = Phase.IDLE;
        this.queue.clear();
        this.pending = null;
        this.attemptsOnCurrent = 0;
        this.supportAttempts = 0;
        this.lastWorkPos = null;
        Navigator.get().stop(this.owner);
        MiningController.get().reset(this.owner);
        this.lastMessage = "cancelled";
    }

    public void restart() {
        if (this.plan != null) {
            this.setPlan(this.plan);
        }
    }

    public Status status() {
        return new Status(this.phase, this.totalTargets(), this.verified, this.remaining(), this.skipped, this.failed, this.wrongBlocks, this.missingBlocks, this.etaSeconds(), this.lastMessage);
    }

    private int remaining() {
        return this.queue.size() + (this.pending != null ? 1 : 0);
    }

    public Map<class_2248, Integer> missingMaterials(Options options) {
        if (this.plan == null) {
            return Map.of();
        }
        HashMap<class_2248, Integer> available = new HashMap<class_2248, Integer>();
        for (Map.Entry<class_2248, Integer> entry : this.plan.required().entrySet()) {
            class_1792 item = class_1792.method_7867((class_2248)entry.getKey());
            if (item == null) continue;
            available.put(entry.getKey(), InvGuards.count(stack -> stack.method_31574(item)));
        }
        return PlacementPlanner.missingMaterials(this.plan, available);
    }

    public Status tick(Options options) {
        if (PlacementEngine.mc.field_1724 == null || PlacementEngine.mc.field_1687 == null) {
            this.lastMessage = "no world";
            return this.status();
        }
        if (this.phase == Phase.IDLE || this.phase == Phase.PAUSED || this.phase == Phase.COMPLETE || this.phase == Phase.FAILED) {
            return this.status();
        }
        if (this.placeCooldown > 0) {
            --this.placeCooldown;
        }
        if (this.phase == Phase.VERIFYING) {
            this.verifyStep(options);
        }
        if (this.phase == Phase.BUILDING) {
            this.buildStep(options);
        }
        return this.status();
    }

    private int totalTargets() {
        return this.plan == null ? 0 : this.plan.targets().size();
    }

    private void verifyStep(Options options) {
        int budget = Math.max(1, options.verifyBudgetPerTick());
        List<PlacementPlanner.Target> targets = this.plan.targets();
        while (budget-- > 0 && this.verifyIndex < targets.size()) {
            PlacementPlanner.Target target;
            class_2680 world;
            if (PlacementEngine.matches(world = PlacementEngine.mc.field_1687.method_8320((target = targets.get(this.verifyIndex++)).pos()), target.expected())) {
                ++this.verified;
                continue;
            }
            if (world.method_26215()) {
                ++this.missingBlocks;
            } else {
                ++this.wrongBlocks;
            }
            this.queue.add(new Work(target.pos(), target.expected()));
        }
        if (this.verifyIndex >= targets.size()) {
            this.phase = this.queue.isEmpty() ? Phase.COMPLETE : Phase.BUILDING;
            this.buildStartedAt = System.currentTimeMillis();
            this.placedAtBuildStart = this.placed;
            if (this.phase == Phase.COMPLETE) {
                this.lastMessage = "already complete, " + this.verified + " blocks verified";
                if (this.reporter != null) {
                    HookStatus.progressNow(this.reporter, "schematic", "Schematic %s is already complete (%d blocks verified)", this.plan.targets().size(), this.verified);
                }
            } else {
                this.lastMessage = "verifying finished: " + this.queue.size() + " blocks to place";
                if (this.reporter != null) {
                    HookStatus.progressNow(this.reporter, "schematic", "Verified %d blocks: %d correct, %d missing, %d incorrect. Placing %d blocks.", targets.size(), this.verified, this.missingBlocks, this.wrongBlocks, this.queue.size());
                }
            }
        }
    }

    private void buildStep(Options options) {
        class_2680 world;
        if (this.queue.isEmpty() && this.pending == null) {
            this.phase = Phase.COMPLETE;
            Navigator.get().stop(this.owner);
            MiningController.get().reset(this.owner);
            this.lastMessage = "complete";
            if (this.reporter != null) {
                HookStatus.progressNow(this.reporter, "schematic", "Build complete: %d blocks verified, %d skipped, %d failed", this.verified, this.skipped, this.failed);
            }
            return;
        }
        if (this.pending != null && this.confirmPlacement(options)) {
            return;
        }
        Work work = this.queue.peek();
        if (work == null) {
            return;
        }
        if (this.lastWorkPos == null || !this.lastWorkPos.equals((Object)work.pos())) {
            this.lastWorkPos = work.pos();
            this.attemptsOnCurrent = 0;
            this.supportAttempts = 0;
        }
        if (PlacementEngine.matches(world = PlacementEngine.mc.field_1687.method_8320(work.pos()), work.expected())) {
            this.queue.poll();
            ++this.verified;
            ++this.placed;
            this.attemptsOnCurrent = 0;
            this.materialTracker.reset();
            return;
        }
        if (!world.method_26215()) {
            this.breakWrongBlock(work, world, options);
            return;
        }
        this.placeBlock(work, options);
    }

    private boolean confirmPlacement(Options options) {
        class_2680 world = PlacementEngine.mc.field_1687.method_8320(this.pending.pos());
        if (PlacementEngine.matches(world, this.pending.expected())) {
            ++this.verified;
            ++this.placed;
            this.pending = null;
            this.attemptsOnCurrent = 0;
            this.materialTracker.reset();
            return false;
        }
        if (this.confirmTicksLeft-- > 0) {
            return true;
        }
        ++this.attemptsOnCurrent;
        Work timedOut = this.pending;
        this.pending = null;
        if (this.attemptsOnCurrent >= Math.max(1, options.maxAttempts())) {
            ++this.failed;
            this.attemptsOnCurrent = 0;
            this.report("warn", "Could not place %s at %s after %d attempts, skipping", PlacementEngine.name(timedOut.expected()), timedOut.pos().method_23854(), options.maxAttempts());
        } else {
            this.queue.addFirst(timedOut);
            this.report("info", "Placement at %s not confirmed, retrying (%d/%d)", timedOut.pos().method_23854(), this.attemptsOnCurrent, options.maxAttempts());
        }
        return false;
    }

    private void breakWrongBlock(Work work, class_2680 world, Options options) {
        Navigator.get().stop(this.owner);
        MiningController.Options miningOptions = new MiningController.Options(options.reach(), options.rotate(), options.swing(), options.autoTool(), options.minToolDurability(), options.avoidLava(), options.avoidFluids(), options.avoidFalling(), 200, options.maxAttempts());
        MiningController.Result result = MiningController.get().tick(this.owner, work.pos(), miningOptions);
        switch (result) {
            case DONE: {
                this.attemptsOnCurrent = 0;
                break;
            }
            case WORKING: {
                this.lastMessage = "breaking " + PlacementEngine.name(work.expected());
                break;
            }
            case OUT_OF_REACH: {
                this.navigateTo(work.pos(), options, "breaking");
                break;
            }
            case UNSAFE: {
                this.queue.poll();
                ++this.failed;
                this.report("warn", "Refusing to break %s at %s for safety reasons, skipping", PlacementEngine.name(PlacementEngine.mc.field_1687.method_8320(work.pos())), work.pos().method_23854());
                break;
            }
            case FAILED: {
                this.queue.poll();
                ++this.failed;
                this.attemptsOnCurrent = 0;
                this.report("warn", "Could not break %s at %s, skipping", PlacementEngine.name(PlacementEngine.mc.field_1687.method_8320(work.pos())), work.pos().method_23854());
                break;
            }
            case REJECTED: {
                this.lastMessage = "mining owned by another module";
            }
        }
    }

    private void placeBlock(Work work, Options options) {
        class_2680 expected = work.expected();
        class_2248 block = expected.method_26204();
        class_1792 item = class_1792.method_7867((class_2248)block);
        if (item == null) {
            this.queue.poll();
            ++this.failed;
            this.report("error", "No item exists for block %s, skipping", PlacementEngine.name(expected));
            return;
        }
        FindItemResult hotbar = InvUtils.findInHotbar(stack -> stack.method_31574(item));
        if (!hotbar.found()) {
            FindItemResult anywhere = InvUtils.find(stack -> stack.method_31574(item));
            if (!anywhere.found()) {
                this.handleMissingMaterial(work, block, options);
                return;
            }
            if (!options.autoMoveToHotbar()) {
                this.lastMessage = "waiting for " + PlacementEngine.name(expected) + " in the hotbar";
                return;
            }
            int emptyHotbar = PlacementEngine.findEmptyHotbarSlot();
            if (emptyHotbar < 0) {
                this.lastMessage = "free a hotbar slot for " + PlacementEngine.name(expected);
                return;
            }
            InvUtils.move().from(anywhere.slot()).toHotbar(emptyHotbar);
            return;
        }
        if (!MiningController.inReach(work.pos(), options.reach())) {
            this.navigateTo(work.pos(), options, "placing");
            return;
        }
        Navigator.get().stop(this.owner);
        this.lastMessage = "placing " + PlacementEngine.name(expected);
        if (BlockUtils.getPlaceSide(work.pos()) == null) {
            if (!options.placeSupportBlocks()) {
                this.queue.poll();
                ++this.failed;
                this.report("warn", "No surface to place against at %s, skipping", work.pos().method_23854());
                return;
            }
            switch (this.placeSupport(work, options).ordinal()) {
                case 0: {
                    break;
                }
                case 1: {
                    break;
                }
                case 2: {
                    this.queue.poll();
                    ++this.failed;
                    this.supportAttempts = 0;
                    this.report("warn", "Could not build support for %s at %s, skipping", PlacementEngine.name(expected), work.pos().method_23854());
                }
            }
            return;
        }
        if (this.placeCooldown > 0) {
            return;
        }
        boolean placedOk = BlockUtils.place(work.pos(), hotbar, options.rotate(), 50, options.swing(), true, true);
        if (!placedOk) {
            ++this.attemptsOnCurrent;
            if (this.attemptsOnCurrent >= Math.max(1, options.maxAttempts())) {
                this.queue.poll();
                ++this.failed;
                this.attemptsOnCurrent = 0;
                this.report("warn", "Placement at %s was rejected, skipping", work.pos().method_23854());
            }
            return;
        }
        this.queue.poll();
        this.pending = work;
        this.confirmTicksLeft = Math.max(5, options.verifyTimeoutTicks());
        this.placeCooldown = Math.max(0, options.placeDelayTicks());
        this.supportAttempts = 0;
    }

    private void handleMissingMaterial(Work work, class_2248 block, Options options) {
        if (options.skipMissingMaterials()) {
            this.queue.poll();
            ++this.skipped;
            this.materialTracker.failure(System.currentTimeMillis());
            if (this.materialTracker.attempts() == 1 || this.materialTracker.attempts() % 16 == 0) {
                this.report("warn", "Missing materials for %s, skipping (total skipped: %d)", PlacementEngine.name(block.method_9564()), this.skipped);
            }
            return;
        }
        this.lastMessage = "waiting for " + PlacementEngine.name(block.method_9564());
        if (this.materialTracker.attempts() == 0 || this.materialTracker.attempts() % 32 == 0) {
            this.report("warn", "Missing materials for %s, waiting. Enable skip-missing to continue without them.", PlacementEngine.name(block.method_9564()));
        }
        this.materialTracker.failure(System.currentTimeMillis());
    }

    private SupportOutcome placeSupport(Work work, Options options) {
        class_2338 target = work.pos();
        class_2248 supportBlock = options.supportBlock();
        if (supportBlock == null || supportBlock == class_2246.field_10124) {
            return SupportOutcome.FAILED;
        }
        class_1792 supportItem = class_1792.method_7867((class_2248)supportBlock);
        if (supportItem == null) {
            return SupportOutcome.FAILED;
        }
        FindItemResult hotbar = InvUtils.findInHotbar(stack -> stack.method_31574(supportItem));
        if (!hotbar.found()) {
            FindItemResult anywhere = InvUtils.find(stack -> stack.method_31574(supportItem));
            if (!anywhere.found()) {
                if (options.skipMissingMaterials()) {
                    this.supportAttempts = 0;
                    return SupportOutcome.FAILED;
                }
                this.lastMessage = "waiting for " + supportBlock.method_9518().getString() + " to build support";
                return SupportOutcome.WAITING;
            }
            if (options.autoMoveToHotbar()) {
                int emptyHotbar = PlacementEngine.findEmptyHotbarSlot();
                if (emptyHotbar >= 0) {
                    InvUtils.move().from(anywhere.slot()).toHotbar(emptyHotbar);
                } else {
                    this.lastMessage = "free a hotbar slot for " + supportBlock.method_9518().getString();
                }
            } else {
                this.lastMessage = "waiting for " + supportBlock.method_9518().getString() + " to build support";
            }
            return SupportOutcome.WAITING;
        }
        if (!MiningController.inReach(target, options.reach())) {
            this.navigateTo(target, options, "support");
            return SupportOutcome.PLACED;
        }
        if (++this.supportAttempts > 8) {
            this.supportAttempts = 0;
            return SupportOutcome.FAILED;
        }
        for (class_2350 direction : class_2350.values()) {
            class_2338 neighbour = target.method_10093(direction);
            if (!PlacementEngine.mc.field_1687.method_8320(neighbour).method_45474() || !BlockUtils.canPlace(neighbour) || BlockUtils.getPlaceSide(neighbour) == null || !BlockUtils.place(neighbour, hotbar, options.rotate(), 40, options.swing(), true, true)) continue;
            this.lastMessage = "placing support block";
            return SupportOutcome.PLACED;
        }
        return SupportOutcome.FAILED;
    }

    private void navigateTo(class_2338 pos, Options options, String activity) {
        if (!options.autoNavigate()) {
            this.lastMessage = "out of reach while " + activity;
            return;
        }
        Navigator.Options navOptions = new Navigator.Options(Math.max(1.5, options.reach() - 0.5), true, true, 60, 3);
        Navigator.State state = Navigator.get().tick(this.owner, pos, navOptions);
        switch (state) {
            default: {
                throw new MatchException(null, null);
            }
            case MOVING: {
                Object object = "walking to " + pos.method_23854();
                break;
            }
            case ARRIVED: {
                Object object = "in position at " + pos.method_23854();
                break;
            }
            case FAILED: {
                Object object = "cannot reach " + pos.method_23854();
                break;
            }
            case IDLE: {
                Object object = this.lastMessage = "idle";
            }
        }
        if (state == Navigator.State.FAILED) {
            this.queue.poll();
            ++this.failed;
            this.report("warn", "Cannot reach %s, skipping", pos.method_23854());
        }
    }

    private static int findEmptyHotbarSlot() {
        if (PlacementEngine.mc.field_1724 == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            if (!PlacementEngine.mc.field_1724.method_31548().method_5438(i).method_7960()) continue;
            return i;
        }
        return -1;
    }

    public static boolean matches(class_2680 world, class_2680 expected) {
        if (world == expected) {
            return true;
        }
        if (world.method_26204() != expected.method_26204()) {
            return false;
        }
        if (world.method_28498((class_2769)class_2741.field_12508) && expected.method_28498((class_2769)class_2741.field_12508)) {
            return world.method_11657((class_2769)class_2741.field_12508, (Comparable)Boolean.valueOf(false)) == expected.method_11657((class_2769)class_2741.field_12508, (Comparable)Boolean.valueOf(false));
        }
        return false;
    }

    private double etaSeconds() {
        if (this.phase != Phase.BUILDING || this.buildStartedAt == 0L) {
            return -1.0;
        }
        long elapsedMs = System.currentTimeMillis() - this.buildStartedAt;
        int placedNow = this.placed;
        int delta = placedNow - this.placedAtBuildStart;
        if (elapsedMs < 4000L || delta <= 0) {
            return -1.0;
        }
        double blocksPerSecond = (double)delta / ((double)elapsedMs / 1000.0);
        if (blocksPerSecond <= 0.001) {
            return -1.0;
        }
        return (double)this.remaining() / blocksPerSecond;
    }

    private static String name(class_2680 state) {
        return state.method_26204().method_9518().getString();
    }

    private static String name(class_2248 block) {
        return block.method_9518().getString();
    }

    private void report(String level, String message, Object ... args) {
        this.lastMessage = message.formatted(args);
        if (this.reporter == null) {
            return;
        }
        switch (level) {
            case "error": {
                HookStatus.error(this.reporter, message, args);
                break;
            }
            case "warn": {
                HookStatus.warn(this.reporter, message, args);
                break;
            }
            default: {
                HookStatus.info(this.reporter, message, args);
            }
        }
    }

    public boolean needsMovement() {
        return this.phase == Phase.BUILDING || this.phase == Phase.VERIFYING;
    }

    public String describe() {
        return String.valueOf((Object)this.phase) + " " + this.verified + "/" + this.totalTargets() + " verified, " + this.queue.size() + " queued, " + this.skipped + " skipped, " + this.failed + " failed";
    }

    public void shutdown() {
        Navigator.get().stop(this.owner);
        MiningController.get().reset(this.owner);
        this.pending = null;
        this.queue.clear();
        TaskManager.get().release(this.owner);
    }

    public static enum Phase {
        IDLE,
        VERIFYING,
        BUILDING,
        PAUSED,
        COMPLETE,
        FAILED;

    }

    private record Work(class_2338 pos, class_2680 expected) {
    }

    public record Status(Phase phase, int total, int verified, int pending, int skipped, int failed, int wrongBlocks, int missingBlocks, double etaSeconds, String message) {
    }

    public record Options(double reach, boolean autoNavigate, boolean rotate, boolean swing, int placeDelayTicks, int verifyTimeoutTicks, int maxAttempts, int verifyBudgetPerTick, boolean skipMissingMaterials, boolean placeSupportBlocks, class_2248 supportBlock, boolean autoMoveToHotbar, boolean autoTool, int minToolDurability, boolean avoidLava, boolean avoidFluids, boolean avoidFalling) {
    }

    private static enum SupportOutcome {
        PLACED,
        WAITING,
        FAILED;

    }
}

