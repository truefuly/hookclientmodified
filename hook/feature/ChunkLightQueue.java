/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_6606
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package hook.feature;

import hook.feature.ChunkLightSnapshot;
import hook.feature.LightPayloadClassifier;
import hook.feature.SkyLightPattern;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.class_6606;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChunkLightQueue {
    private static final Logger LOG = LoggerFactory.getLogger((String)"Hook ChunkFinder");
    private static final int DETECTION_CUTOFF_Y = 1;
    private static final int MAX_DRAIN_PER_TICK = 4096;
    private static final int MAX_SNAPSHOTS = 8192;
    private final Queue<PendingLight> queue = new ConcurrentLinkedQueue<PendingLight>();
    private final Map<Long, ChunkLightSnapshot> snapshots = new LinkedHashMap<Long, ChunkLightSnapshot>(256, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, ChunkLightSnapshot> eldest) {
            return this.size() > 8192;
        }
    };
    private final Set<Long> alertedChunks = new HashSet<Long>();
    private final Set<AlertedSection> alertedSections = new HashSet<AlertedSection>();
    private final Listener listener;
    private long stamp;

    public ChunkLightQueue(Listener listener) {
        this.listener = listener;
    }

    public void offer(int var1, int var2, class_6606 var3) {
        if (var3 != null) {
            this.queue.offer(new PendingLight(var1, var2, var3));
        }
    }

    public void clear() {
        this.queue.clear();
        this.snapshots.clear();
        this.alertedChunks.clear();
        this.alertedSections.clear();
    }

    public void drain(int var1, int var2) {
        long var3 = System.nanoTime() + 3000000L;
        for (int var5 = 0; var5 < 4096; ++var5) {
            if (var5 > 0 && (var5 & 7) == 0 && System.nanoTime() >= var3) {
                return;
            }
            PendingLight var6 = this.queue.poll();
            if (var6 == null) {
                return;
            }
            this.ingest(var6, var1, var2);
        }
    }

    private void ingest(PendingLight var1, int var2, int var3) {
        long var4 = ChunkLightQueue.chunkKey(var1.chunkX(), var1.chunkZ());
        ChunkLightSnapshot var6 = this.snapshots.get(var4);
        if (var6 == null || var6.sectionCount != var2 || var6.bottomSectionY != var3) {
            var6 = new ChunkLightSnapshot(var2, var3);
            this.snapshots.put(var4, var6);
        }
        long var7 = ++this.stamp;
        try {
            LightPayloadClassifier.apply(var1.data(), var6, var7);
        }
        catch (Throwable var10) {
            LOG.error("[Chunk Finder] Light classifier crashed on chunk " + var1.chunkX() + "," + var1.chunkZ(), var10);
            return;
        }
        this.maybeAlert(var1.chunkX(), var1.chunkZ(), var4, var6, var7);
    }

    private void maybeAlert(int var1, int var2, long var3, ChunkLightSnapshot var5, long var6) {
        int var9;
        for (int var8 = 1; var8 <= var5.sectionCount && (var9 = (var5.bottomSectionY + var8 - 1) * 16) < 1; ++var8) {
            if (var5.skyUpdated[var8] == var6) {
                this.inspect(Track.SKY, var1, var2, var3, var9, var5.sky[var8]);
            }
            if (var5.blockUpdated[var8] != var6) continue;
            this.inspect(Track.BLOCK, var1, var2, var3, var9, var5.block[var8]);
        }
    }

    private void inspect(Track var1, int var2, int var3, long var4, int var6, SkyLightPattern var7) {
        if ((var7 == SkyLightPattern.ZEROED_PRESENT || var7 == SkyLightPattern.UNIFORM_MAX) && this.alertedSections.add(new AlertedSection(var4, var6, var1)) && this.alertedChunks.add(var4)) {
            this.listener.onChunkFound(var2, var3, var6);
        }
    }

    private static long chunkKey(int var0, int var1) {
        return (long)var0 & 0xFFFFFFFFL | ((long)var1 & 0xFFFFFFFFL) << 32;
    }

    public static interface Listener {
        public void onChunkFound(int var1, int var2, int var3);
    }

    private record PendingLight(int chunkX, int chunkZ, class_6606 data) {
    }

    private static enum Track {
        SKY,
        BLOCK;

    }

    private record AlertedSection(long chunkKey, int sectionY, Track track) {
    }
}

