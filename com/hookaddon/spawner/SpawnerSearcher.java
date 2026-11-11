/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1299
 *  net.minecraft.class_1937
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2382
 *  net.minecraft.class_2586
 *  net.minecraft.class_2636
 *  net.minecraft.class_2818
 *  net.minecraft.class_310
 *  net.minecraft.class_638
 */
package com.hookaddon.spawner;

import com.hookaddon.spawner.SpawnerTarget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_2586;
import net.minecraft.class_2636;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import net.minecraft.class_638;

public final class SpawnerSearcher {
    private static final class_310 mc = class_310.method_1551();
    private static final long SETTLE_MS = 4000L;
    private final Set<Long> visited = new HashSet<Long>();
    private List<long[]> chunks;
    private int chunkIndex;
    private List<long[]> pending = new ArrayList<long[]>();
    private class_2338 center;
    private int radius;
    private int minY;
    private int maxY;
    private boolean running;
    private long settleUntilMs;
    private int scannedThisRun;
    private SpawnerTarget lastCandidate;
    private SpawnerTarget confirmedTarget;

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

    public SpawnerTarget lastCandidate() {
        return this.lastCandidate;
    }

    public void start(Options options, class_2338 start, boolean resetVisited) {
        if (resetVisited) {
            this.visited.clear();
        }
        this.center = new class_2338((class_2382)start);
        this.radius = Math.max(4, options.radius());
        this.minY = start.method_10264() - options.height();
        this.maxY = start.method_10264() + options.height();
        this.chunks = this.buildChunkOrder();
        this.chunkIndex = 0;
        this.pending = new ArrayList<long[]>();
        this.settleUntilMs = System.currentTimeMillis() + 4000L;
        this.running = true;
        this.scannedThisRun = 0;
        this.lastCandidate = null;
    }

    public void stop() {
        this.running = false;
        this.chunks = null;
    }

    public Result tick(Options options) {
        if (!this.running || this.chunks == null || SpawnerSearcher.mc.field_1687 == null || SpawnerSearcher.mc.field_1724 == null) {
            this.running = false;
            return new Result(Outcome.NO_TARGET, this.lastCandidate, 0);
        }
        int budget = Math.max(1, options.budgetPerTick());
        while (budget > 0 && this.chunkIndex < this.chunks.size()) {
            long[] chunk = this.chunks.get(this.chunkIndex);
            int chunkX = (int)chunk[0];
            int chunkZ = (int)chunk[1];
            this.advanceChunk();
            if (!SpawnerSearcher.mc.field_1687.method_8393(chunkX, chunkZ)) {
                this.pending.add(chunk);
                this.advanceChunk();
                continue;
            }
            this.scanChunk(options, chunkX, chunkZ);
            --budget;
            ++this.scannedThisRun;
            SpawnerTarget found = this.confirmedTarget;
            if (found == null) continue;
            this.confirmedTarget = null;
            this.lastCandidate = null;
            this.running = false;
            this.trimVisited(options);
            return new Result(Outcome.CONFIRMED, found, this.scannedThisRun);
        }
        if (this.chunkIndex < this.chunks.size()) {
            return new Result(Outcome.SCANNING, this.lastCandidate, this.scannedThisRun);
        }
        if (!this.pending.isEmpty() && System.currentTimeMillis() < this.settleUntilMs) {
            this.chunks = this.pending;
            this.pending = new ArrayList<long[]>();
            this.chunkIndex = 0;
            return new Result(Outcome.SCANNING, this.lastCandidate, this.scannedThisRun);
        }
        this.running = false;
        return new Result(Outcome.NO_TARGET, this.lastCandidate, this.scannedThisRun);
    }

    private void scanChunk(Options options, int chunkX, int chunkZ) {
        class_2818 chunk = SpawnerSearcher.mc.field_1687.method_8497(chunkX, chunkZ);
        if (chunk == null) {
            return;
        }
        for (class_2586 entity : chunk.method_12214().values()) {
            class_2338 pos;
            if (!(entity instanceof class_2636) || (pos = entity.method_11016()).method_10264() < this.minY || pos.method_10264() > this.maxY || this.outsideRadius(pos.method_10263(), pos.method_10260())) continue;
            long key = SpawnerSearcher.pack(pos.method_10263(), pos.method_10264(), pos.method_10260());
            if (options.skipVisited() && this.visited.contains(key)) continue;
            this.visited.add(key);
            SpawnerTarget target = this.inspect(pos, options);
            if (target == null) continue;
            if (target.confirmed()) {
                this.confirmedTarget = target;
                return;
            }
            this.lastCandidate = target;
        }
        this.trimVisited(options);
    }

    public Result check(class_2338 pos, Options options) {
        SpawnerTarget target = this.inspect(pos, options);
        if (target == null) {
            return new Result(Outcome.SCANNING, null, 0);
        }
        return new Result(target.confirmed() ? Outcome.CONFIRMED : Outcome.CANDIDATE, target, 0);
    }

    private SpawnerTarget inspect(class_2338 pos, Options options) {
        class_638 world = SpawnerSearcher.mc.field_1687;
        if (world == null || !world.method_8320(pos).method_27852(class_2246.field_10260)) {
            return null;
        }
        class_2586 class_25862 = world.method_8321(pos);
        if (!(class_25862 instanceof class_2636)) {
            return new SpawnerTarget(pos, null, false);
        }
        class_2636 spawner = (class_2636)class_25862;
        class_1299 type = null;
        try {
            class_1297 rendered = spawner.method_11390().method_8283((class_1937)world, pos);
            if (rendered != null) {
                type = rendered.method_5864();
            }
        }
        catch (RuntimeException e) {
            type = null;
        }
        if (type == null) {
            return new SpawnerTarget(pos, null, false);
        }
        if (options.types().isEmpty()) {
            return new SpawnerTarget(pos, type, true);
        }
        if (options.types().contains(type)) {
            return new SpawnerTarget(pos, type, true);
        }
        return options.onlySelected() ? null : new SpawnerTarget(pos, type, false);
    }

    private boolean outsideRadius(int x, int z) {
        int dz;
        int dx = x - this.center.method_10263();
        return (long)dx * (long)dx + (long)(dz = z - this.center.method_10260()) * (long)dz > (long)this.radius * (long)this.radius;
    }

    private void advanceChunk() {
        ++this.chunkIndex;
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

    public record Options(int radius, int height, int budgetPerTick, Set<class_1299<?>> types, boolean onlySelected, boolean skipVisited, int maxVisited) {
    }

    public record Result(Outcome outcome, SpawnerTarget target, int scanned) {
    }

    public static enum Outcome {
        SCANNING,
        CONFIRMED,
        CANDIDATE,
        NO_TARGET;

    }
}

