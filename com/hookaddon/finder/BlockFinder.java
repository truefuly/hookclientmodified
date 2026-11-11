/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2382
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_638
 */
package com.hookaddon.finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_638;

public final class BlockFinder {
    private static final class_310 mc = class_310.method_1551();
    private static final long SETTLE_MS = 4000L;
    private final Set<Long> visited = new HashSet<Long>();
    private final List<class_2338> matches = new ArrayList<class_2338>();
    private List<long[]> chunks;
    private int chunkIndex;
    private int localX;
    private int localY;
    private int localZ;
    private List<long[]> pending = new ArrayList<long[]>();
    private class_2338 center;
    private int radius;
    private int minY;
    private int maxY;
    private int sizeY;
    private boolean running;
    private long settleUntilMs;
    private int scannedThisRun;

    public List<class_2338> matches() {
        return this.matches;
    }

    public Set<Long> visited() {
        return this.visited;
    }

    public void loadVisited(Collection<Long> positions) {
        this.visited.clear();
        if (positions != null) {
            this.visited.addAll(positions);
        }
    }

    public void clearVisited() {
        this.visited.clear();
    }

    public boolean isRunning() {
        return this.running;
    }

    public int scanned() {
        return this.scannedThisRun;
    }

    public void start(Options options, class_2338 start, boolean resetVisited) {
        if (resetVisited) {
            this.visited.clear();
        }
        this.matches.clear();
        this.center = new class_2338((class_2382)start);
        this.radius = Math.max(4, options.radius());
        this.sizeY = options.height() * 2 + 1;
        this.minY = start.method_10264() - options.height();
        this.maxY = start.method_10264() + options.height();
        this.chunks = this.buildChunkOrder();
        this.chunkIndex = 0;
        this.localZ = 0;
        this.localY = 0;
        this.localX = 0;
        this.pending = new ArrayList<long[]>();
        this.settleUntilMs = System.currentTimeMillis() + 4000L;
        this.running = true;
        this.scannedThisRun = 0;
    }

    public void stop() {
        this.running = false;
        this.chunks = null;
    }

    public Result tick(Options options, Predicate<class_2248> shouldMine, Predicate<class_2248> shouldIgnore) {
        if (!this.running || this.chunks == null || BlockFinder.mc.field_1687 == null || BlockFinder.mc.field_1724 == null) {
            this.running = false;
            return this.result(this.matches.isEmpty() ? Outcome.NO_TARGET : Outcome.TARGET);
        }
        int budget = Math.max(1, options.budgetPerTick());
        while (budget > 0) {
            int chunkZ;
            if (this.matches.size() >= options.maxTargets()) {
                return this.finish(options);
            }
            if (this.passComplete()) break;
            long[] chunk = this.chunks.get(this.chunkIndex);
            int chunkX = (int)chunk[0];
            if (!BlockFinder.mc.field_1687.method_8393(chunkX, chunkZ = (int)chunk[1])) {
                this.pending.add(chunk);
                this.advanceChunk();
                continue;
            }
            int used = this.scanChunk(options, chunkX, chunkZ, budget, shouldMine, shouldIgnore);
            this.scannedThisRun += used;
            budget -= used;
        }
        if (!this.passComplete()) {
            return new Result(Outcome.SCANNING, this.firstMatch(), this.scannedThisRun);
        }
        if (!this.pending.isEmpty() && System.currentTimeMillis() < this.settleUntilMs) {
            this.chunks = this.pending;
            this.pending = new ArrayList<long[]>();
            this.chunkIndex = 0;
            this.localZ = 0;
            this.localY = 0;
            this.localX = 0;
            return new Result(Outcome.SCANNING, this.firstMatch(), this.scannedThisRun);
        }
        return this.finish(options);
    }

    private boolean passComplete() {
        return this.chunkIndex >= this.chunks.size();
    }

    private int scanChunk(Options options, int chunkX, int chunkZ, int budget, Predicate<class_2248> shouldMine, Predicate<class_2248> shouldIgnore) {
        int processed;
        class_638 world = BlockFinder.mc.field_1687;
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        for (processed = 0; processed < budget && this.localY < this.sizeY; ++processed) {
            class_2248 block;
            class_2680 state;
            int x = baseX + this.localX;
            int y = this.minY + this.localY;
            int z = baseZ + this.localZ;
            this.step();
            if (this.outsideRadius(x, z)) continue;
            long key = BlockFinder.pack(x, y, z);
            if (options.skipVisited() && this.visited.contains(key) || (state = world.method_8320(new class_2338(x, y, z))).method_26215() || shouldIgnore.test(block = state.method_26204()) || !shouldMine.test(block)) continue;
            this.visited.add(key);
            this.matches.add(new class_2338(x, y, z));
            if (this.matches.size() < options.maxTargets()) continue;
            break;
        }
        if (this.localY >= this.sizeY) {
            this.advanceChunk();
        }
        return processed;
    }

    private Result finish(Options options) {
        this.running = false;
        this.matches.sort(Comparator.comparingDouble(p -> {
            long dx = p.method_10263() - this.center.method_10263();
            long dy = p.method_10264() - this.center.method_10264();
            long dz = p.method_10260() - this.center.method_10260();
            return dx * dx + dy * dy + dz * dz;
        }));
        this.trimVisited(options);
        return this.result(this.matches.isEmpty() ? Outcome.NO_TARGET : Outcome.TARGET);
    }

    private Result result(Outcome outcome) {
        return new Result(outcome, this.firstMatch(), this.scannedThisRun);
    }

    private class_2338 firstMatch() {
        return this.matches.isEmpty() ? null : this.matches.get(0);
    }

    private boolean outsideRadius(int x, int z) {
        int dz;
        int dx = x - this.center.method_10263();
        return (long)dx * (long)dx + (long)(dz = z - this.center.method_10260()) * (long)dz > (long)this.radius * (long)this.radius;
    }

    private void step() {
        ++this.localX;
        if (this.localX >= 16) {
            this.localX = 0;
            ++this.localZ;
            if (this.localZ >= 16) {
                this.localZ = 0;
                ++this.localY;
            }
        }
    }

    private void advanceChunk() {
        ++this.chunkIndex;
        this.localZ = 0;
        this.localY = 0;
        this.localX = 0;
    }

    private void trimVisited(Options options) {
        if (this.visited.size() > options.maxVisited()) {
            this.visited.clear();
        }
    }

    private List<long[]> buildChunkOrder() {
        int chunkRadius = (this.radius + 15) / 16;
        ArrayList<long[]> list = new ArrayList<long[]>((chunkRadius * 2 + 1) * (chunkRadius * 2 + 1));
        int centerX = this.center.method_10263() >> 4;
        int centerZ = this.center.method_10260() >> 4;
        for (int cx = -chunkRadius; cx <= chunkRadius; ++cx) {
            for (int cz = -chunkRadius; cz <= chunkRadius; ++cz) {
                list.add(new long[]{centerX + cx, centerZ + cz});
            }
        }
        list.sort(Comparator.comparingLong(chunk -> {
            int dx = (int)chunk[0] - centerX;
            int dz = (int)chunk[1] - centerZ;
            return (long)dx * (long)dx + (long)dz * (long)dz;
        }));
        return list;
    }

    public static long pack(int x, int y, int z) {
        return (long)(x & 0x3FFFFFF) << 38 | (long)(z & 0x3FFFFFF) << 12 | (long)(y & 0xFFF);
    }

    public record Options(int radius, int height, int budgetPerTick, int maxTargets, boolean skipVisited, int maxVisited) {
    }

    public static enum Outcome {
        SCANNING,
        TARGET,
        NO_TARGET;

    }

    public record Result(Outcome outcome, class_2338 matched, int scanned) {
    }
}

