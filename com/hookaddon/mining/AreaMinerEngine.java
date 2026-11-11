/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 */
package com.hookaddon.mining;

import com.hookaddon.mining.AreaGeometry;
import com.hookaddon.mining.MiningPattern;
import com.hookaddon.mining.PickaxeWidth;
import com.hookaddon.nav.Navigator;
import com.hookaddon.task.MiningController;
import com.hookaddon.task.TaskManager;
import com.hookaddon.util.HookStatus;
import com.hookaddon.util.InvGuards;
import com.hookaddon.util.SafetyChecks;
import hook.compat.meteorclient.systems.modules.Module;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_310;

public final class AreaMinerEngine {
    private static final class_310 mc = class_310.method_1551();
    private final String owner;
    private final Module reporter;
    private int[] order;
    private class_2338 origin;
    private int width;
    private int height;
    private int depth;
    private MiningPattern pattern;
    private Phase phase = Phase.IDLE;
    private int cursor;
    private int attempted;
    private int completed;
    private int skipped;
    private int failed;
    private final Deque<Integer> failures = new ArrayDeque<Integer>();
    private int retryPass;
    private static final byte OUT_NONE = 0;
    private static final byte OUT_DONE = 1;
    private static final byte OUT_SKIPPED = 2;
    private static final byte OUT_FAILED = 3;
    private byte[] outcomes = new byte[0];
    private final BitSet attemptedPositions = new BitSet();
    private final Deque<class_2338> pendingSwing = new ArrayDeque<class_2338>();
    private final BitSet swingFailed = new BitSet();
    private int delayTicks;
    private String message = "idle";
    private long startedAtMs;
    private long completedAtStart;
    private boolean inventoryPaused;
    private int maxRetries = 1;

    public AreaMinerEngine(String owner, Module reporter) {
        this.owner = owner;
        this.reporter = reporter;
    }

    public Phase phase() {
        return this.phase;
    }

    public int[] order() {
        return this.order;
    }

    public class_2338 origin() {
        return this.origin;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public int depth() {
        return this.depth;
    }

    private int positionCount() {
        return this.order == null ? 0 : this.order.length / 3;
    }

    public class_2338 currentTarget() {
        if (this.cursor >= this.positionCount()) {
            return null;
        }
        int index = this.cursor * 3;
        return this.origin.method_10069(this.order[index], this.order[index + 1], this.order[index + 2]);
    }

    public List<class_2338> pendingPositions() {
        ArrayList<class_2338> positions = new ArrayList<class_2338>();
        if (this.order == null) {
            return positions;
        }
        for (int i = this.cursor; i < this.positionCount() && positions.size() < 512; ++i) {
            int index = i * 3;
            positions.add(this.origin.method_10069(this.order[index], this.order[index + 1], this.order[index + 2]));
        }
        return positions;
    }

    public Status status() {
        return new Status(this.phase, this.positionCount(), this.attempted, this.completed, this.skipped, this.failed, this.retryPass, this.etaSeconds(), this.message);
    }

    public void prepare(class_2338 areaOrigin, int areaWidth, int areaHeight, int areaDepth, MiningPattern miningPattern, int stripWidth, int stripGap) {
        this.origin = areaOrigin;
        this.width = areaWidth;
        this.height = areaHeight;
        this.depth = areaDepth;
        this.pattern = miningPattern;
        this.order = AreaGeometry.order(areaWidth, areaHeight, areaDepth, miningPattern, stripWidth, stripGap);
        this.resetPlan();
    }

    public void prepareFromPositions(class_2338 areaOrigin, int areaWidth, int areaHeight, int areaDepth, List<class_2338> positions) {
        this.origin = areaOrigin;
        this.width = areaWidth;
        this.height = areaHeight;
        this.depth = areaDepth;
        this.pattern = null;
        this.order = new int[positions.size() * 3];
        for (int i = 0; i < positions.size(); ++i) {
            class_2338 pos = positions.get(i);
            int index = i * 3;
            this.order[index] = pos.method_10263() - areaOrigin.method_10263();
            this.order[index + 1] = pos.method_10264() - areaOrigin.method_10264();
            this.order[index + 2] = pos.method_10260() - areaOrigin.method_10260();
        }
        this.resetPlan();
    }

    private void resetPlan() {
        this.cursor = 0;
        this.attempted = 0;
        this.completed = 0;
        this.skipped = 0;
        this.failed = 0;
        this.failures.clear();
        this.outcomes = new byte[this.positionCount()];
        this.attemptedPositions.clear();
        this.pendingSwing.clear();
        this.swingFailed.clear();
        this.retryPass = 0;
        this.delayTicks = 0;
        this.inventoryPaused = false;
        this.startedAtMs = 0L;
        this.completedAtStart = 0L;
        this.phase = Phase.IDLE;
        this.message = "ready: " + this.positionCount() + " candidate blocks";
    }

    public void start() {
        if (this.positionCount() == 0) {
            return;
        }
        this.phase = Phase.RUNNING;
        this.startedAtMs = System.currentTimeMillis();
        this.completedAtStart = this.completed;
        this.message = "mining";
        Navigator.get().stop(this.owner);
    }

    public void pause(String reason) {
        if (this.phase == Phase.RUNNING) {
            this.phase = Phase.PAUSED;
            this.message = "paused: " + reason;
            Navigator.get().stop(this.owner);
            MiningController.get().reset(this.owner);
        }
    }

    public void resume() {
        if (this.phase == Phase.PAUSED) {
            this.phase = Phase.RUNNING;
            this.inventoryPaused = false;
            this.message = "mining";
        }
    }

    public void reset() {
        this.phase = Phase.IDLE;
        this.cursor = 0;
        this.attempted = 0;
        this.completed = 0;
        this.skipped = 0;
        this.failed = 0;
        this.failures.clear();
        Arrays.fill(this.outcomes, (byte)0);
        this.attemptedPositions.clear();
        this.pendingSwing.clear();
        this.swingFailed.clear();
        this.retryPass = 0;
        this.delayTicks = 0;
        this.inventoryPaused = false;
        Navigator.get().stop(this.owner);
        MiningController.get().reset(this.owner);
        this.message = "reset";
    }

    public Status tick(Options options, Predicate<class_2248> shouldMine, Predicate<class_2248> shouldIgnore) {
        class_2338 pos;
        if (this.phase != Phase.RUNNING || this.order == null) {
            return this.status();
        }
        if (AreaMinerEngine.mc.field_1724 == null || AreaMinerEngine.mc.field_1687 == null) {
            return this.status();
        }
        if (this.delayTicks > 0) {
            --this.delayTicks;
            return this.status();
        }
        if (options.stopWhenFull() && InvGuards.inventoryFull()) {
            if (!this.inventoryPaused) {
                this.inventoryPaused = true;
                this.pause("your inventory is full");
                if (this.reporter != null) {
                    HookStatus.warn(this.reporter, "Inventory is full, pausing the area miner.", new Object[0]);
                }
            }
            return this.status();
        }
        if (!this.pendingSwing.isEmpty()) {
            this.tickSwingNeighbor(options, shouldMine, shouldIgnore);
            return this.status();
        }
        while (this.cursor < this.positionCount() && (pos = this.currentTarget()) != null) {
            class_2680 state = AreaMinerEngine.mc.field_1687.method_8320(pos);
            class_2248 block = state.method_26204();
            if (state.method_26215()) {
                this.markDone(this.cursor);
                ++this.cursor;
                this.delayTicks = 0;
                continue;
            }
            if (shouldIgnore.test(block) || !shouldMine.test(block)) {
                this.markSkipped(this.cursor);
                ++this.cursor;
                continue;
            }
            if (SafetyChecks.unsafeToMine(pos, options.avoidLava(), options.avoidFluids(), options.avoidFalling())) {
                this.markFailed(this.cursor);
                this.failures.add(this.cursor);
                ++this.cursor;
                if (this.reporter == null || this.failed > 3) continue;
                HookStatus.warn(this.reporter, "Skipped %s at %s for safety reasons.", block.method_9518().getString(), AreaMinerEngine.format(pos));
                continue;
            }
            if (!MiningController.inReach(pos, options.reach())) {
                if (!options.autoNavigate()) {
                    this.message = "out of reach: " + AreaMinerEngine.format(pos);
                    return this.status();
                }
                Navigator.State nav = Navigator.get().tick(this.owner, pos, new Navigator.Options(Math.max(1.5, options.reach() - 0.5), true, true, 80, 3));
                this.message = "walking to " + AreaMinerEngine.format(pos);
                if (nav == Navigator.State.FAILED) {
                    this.markFailed(this.cursor);
                    this.failures.add(this.cursor);
                    ++this.cursor;
                    Navigator.get().stop(this.owner);
                    if (this.reporter != null) {
                        HookStatus.warn(this.reporter, "Cannot reach %s, skipping it.", AreaMinerEngine.format(pos));
                    }
                }
                return this.status();
            }
            Navigator.get().stop(this.owner);
            this.mineOne(pos, state, options, shouldMine, shouldIgnore);
            return this.status();
        }
        this.finishRun();
        return this.status();
    }

    private void mineOne(class_2338 pos, class_2680 state, Options options, Predicate<class_2248> shouldMine, Predicate<class_2248> shouldIgnore) {
        this.markAttempted(pos);
        MiningController.Result result = MiningController.get().tick(this.owner, pos, AreaMinerEngine.miningControllerOptions(options));
        switch (result) {
            case WORKING: {
                this.message = "mining " + AreaMinerEngine.format(pos);
                break;
            }
            case DONE: {
                this.markDone(this.cursor);
                ++this.cursor;
                this.delayTicks = Math.max(0, options.delayBetweenBlocks());
                this.enqueueSwing(pos, options, shouldMine, shouldIgnore);
                this.message = "mined " + AreaMinerEngine.format(pos);
                break;
            }
            case OUT_OF_REACH: {
                this.message = "reaching " + AreaMinerEngine.format(pos);
                break;
            }
            case UNSAFE: 
            case FAILED: 
            case REJECTED: {
                this.markFailed(this.cursor);
                this.failures.add(this.cursor);
                ++this.cursor;
                this.delayTicks = Math.max(0, options.delayBetweenBlocks());
                if (this.reporter != null && this.failed <= 3) {
                    HookStatus.warn(this.reporter, "Could not mine %s (%s).", AreaMinerEngine.format(pos), MiningController.describe(result));
                }
                if (result != MiningController.Result.REJECTED) break;
                this.pause("another module owns mining");
            }
        }
    }

    private static MiningController.Options miningControllerOptions(Options options) {
        return new MiningController.Options(options.reach(), options.rotate(), options.swing(), options.autoTool(), options.minToolDurability(), options.avoidLava(), options.avoidFluids(), options.avoidFalling(), 200, 2);
    }

    private void enqueueSwing(class_2338 center, Options options, Predicate<class_2248> shouldMine, Predicate<class_2248> shouldIgnore) {
        if (options.pickaxe() != PickaxeWidth.THREE) {
            return;
        }
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                class_2248 block;
                class_2680 state;
                class_2338 neighbour;
                int index;
                if (dx == 0 && dz == 0 || (index = this.volumeIndex(neighbour = center.method_10069(dx, 0, dz))) < 0 || this.swingFailed.get(index) || (state = AreaMinerEngine.mc.field_1687.method_8320(neighbour)).method_26215() || shouldIgnore.test(block = state.method_26204()) || !shouldMine.test(block) || SafetyChecks.unsafeToMine(neighbour, options.avoidLava(), options.avoidFluids(), options.avoidFalling())) continue;
                this.pendingSwing.addLast(neighbour);
            }
        }
    }

    private void tickSwingNeighbor(Options options, Predicate<class_2248> shouldMine, Predicate<class_2248> shouldIgnore) {
        class_2338 pos = this.pendingSwing.peekFirst();
        class_2680 state = AreaMinerEngine.mc.field_1687.method_8320(pos);
        class_2248 block = state.method_26204();
        if (state.method_26215() || shouldIgnore.test(block) || !shouldMine.test(block)) {
            this.pendingSwing.pollFirst();
            return;
        }
        if (SafetyChecks.unsafeToMine(pos, options.avoidLava(), options.avoidFluids(), options.avoidFalling())) {
            int index = this.volumeIndex(pos);
            if (index >= 0) {
                this.swingFailed.set(index);
            }
            this.pendingSwing.pollFirst();
            return;
        }
        if (!MiningController.inReach(pos, options.reach())) {
            if (!options.autoNavigate()) {
                this.pendingSwing.pollFirst();
                return;
            }
            Navigator.State nav = Navigator.get().tick(this.owner, pos, new Navigator.Options(Math.max(1.5, options.reach() - 0.5), true, true, 80, 3));
            this.message = "walking to " + AreaMinerEngine.format(pos);
            if (nav == Navigator.State.FAILED) {
                this.pendingSwing.pollFirst();
                Navigator.get().stop(this.owner);
            }
            return;
        }
        Navigator.get().stop(this.owner);
        this.markAttempted(pos);
        MiningController.Result result = MiningController.get().tick(this.owner, pos, AreaMinerEngine.miningControllerOptions(options));
        switch (result) {
            case WORKING: {
                this.message = "mining " + AreaMinerEngine.format(pos);
                break;
            }
            case DONE: {
                this.pendingSwing.pollFirst();
                this.delayTicks = Math.max(0, options.delayBetweenBlocks());
                this.message = "mined " + AreaMinerEngine.format(pos);
                break;
            }
            case OUT_OF_REACH: {
                this.pendingSwing.pollFirst();
                break;
            }
            case UNSAFE: 
            case FAILED: 
            case REJECTED: {
                int index = this.volumeIndex(pos);
                if (index >= 0) {
                    this.swingFailed.set(index);
                }
                this.pendingSwing.pollFirst();
                this.delayTicks = Math.max(0, options.delayBetweenBlocks());
                if (result != MiningController.Result.REJECTED) break;
                this.pause("another module owns mining");
            }
        }
    }

    private int volumeIndex(class_2338 pos) {
        int x = pos.method_10263() - this.origin.method_10263();
        int y = pos.method_10264() - this.origin.method_10264();
        int z = pos.method_10260() - this.origin.method_10260();
        if (x < 0 || x >= this.width || y < 0 || y >= this.height || z < 0 || z >= this.depth) {
            return -1;
        }
        return (y * this.depth + z) * this.width + x;
    }

    private void markAttempted(class_2338 pos) {
        int index = this.volumeIndex(pos);
        if (index < 0 || this.attemptedPositions.get(index)) {
            return;
        }
        this.attemptedPositions.set(index);
        ++this.attempted;
    }

    private void finishRun() {
        if (!this.failures.isEmpty() && this.retryPass < this.maxRetries) {
            ++this.retryPass;
            Integer first = this.failures.poll();
            this.failures.clear();
            this.cursor = first == null ? this.positionCount() : Math.max(0, first);
            this.message = "retry pass " + this.retryPass;
            if (this.reporter != null) {
                HookStatus.info(this.reporter, "Starting retry pass %d for blocks that failed before.", this.retryPass);
            }
            return;
        }
        this.phase = Phase.COMPLETE;
        Navigator.get().stop(this.owner);
        MiningController.get().reset(this.owner);
        String summary = "Area mining finished: " + this.completed + " mined, " + this.skipped + " skipped, " + this.failed + " failed out of " + this.positionCount();
        this.message = summary;
        if (this.reporter != null) {
            HookStatus.progressNow(this.reporter, "area-miner", "%s", summary);
        }
    }

    private void markDone(int index) {
        byte previous = this.outcomes[index];
        if (previous == 1) {
            return;
        }
        if (previous == 3) {
            --this.failed;
        } else if (previous == 2) {
            --this.skipped;
        }
        this.outcomes[index] = 1;
        ++this.completed;
    }

    private void markSkipped(int index) {
        byte previous = this.outcomes[index];
        if (previous == 2) {
            return;
        }
        if (previous == 1) {
            --this.completed;
        } else if (previous == 3) {
            --this.failed;
        }
        this.outcomes[index] = 2;
        ++this.skipped;
    }

    private void markFailed(int index) {
        byte previous = this.outcomes[index];
        if (previous == 3) {
            return;
        }
        if (previous == 1) {
            --this.completed;
        } else if (previous == 2) {
            --this.skipped;
        }
        this.outcomes[index] = 3;
        ++this.failed;
    }

    public void setMaxRetries(int retries) {
        this.maxRetries = Math.max(0, retries);
    }

    private double etaSeconds() {
        if (this.phase != Phase.RUNNING || this.startedAtMs == 0L) {
            return -1.0;
        }
        long elapsed = System.currentTimeMillis() - this.startedAtMs;
        int doneNow = this.completed;
        int delta = doneNow - (int)this.completedAtStart;
        if (elapsed < 5000L || delta <= 0) {
            return -1.0;
        }
        double perSecond = (double)delta / ((double)elapsed / 1000.0);
        if (perSecond <= 0.001) {
            return -1.0;
        }
        int remaining = this.positionCount() - this.cursor;
        return (double)remaining / perSecond;
    }

    private static String format(class_2338 pos) {
        return "(" + pos.method_10263() + ", " + pos.method_10264() + ", " + pos.method_10260() + ")";
    }

    public void shutdown() {
        Navigator.get().stop(this.owner);
        MiningController.get().reset(this.owner);
        TaskManager.get().release(this.owner);
    }

    public String describe() {
        if (this.order == null) {
            return "no area";
        }
        return this.completed + "/" + this.positionCount() + " mined, " + this.skipped + " skipped, " + this.failed + " failed";
    }

    public boolean isInventoryPaused() {
        return this.inventoryPaused;
    }

    public static enum Phase {
        IDLE,
        RUNNING,
        PAUSED,
        COMPLETE;

    }

    public record Status(Phase phase, int total, int attempted, int completed, int skipped, int failed, int retryPass, double etaSeconds, String message) {
    }

    public record Options(double reach, boolean autoNavigate, boolean rotate, boolean swing, boolean autoTool, int minToolDurability, int delayBetweenBlocks, int maxRetries, boolean stopWhenFull, boolean avoidLava, boolean avoidFluids, boolean avoidFalling, PickaxeWidth pickaxe) {
    }
}

