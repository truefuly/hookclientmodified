/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1923
 *  net.minecraft.class_310
 *  net.minecraft.class_638
 *  net.minecraft.class_6606
 */
package hook.utils;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1923;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_6606;

public final class ChunkLightTracker {
    private static final Map<Long, Snapshot> SNAPSHOTS = new HashMap<Long, Snapshot>();
    private static final Set<Long> FLAGGED = new HashSet<Long>();
    private static volatile Set<Long> flaggedSnapshot = Set.of();
    private static class_638 world;

    private ChunkLightTracker() {
    }

    public static synchronized void ingest(int chunkX, int chunkZ, class_6606 data) {
        class_310 mc = class_310.method_1551();
        class_638 currentWorld = mc.field_1687;
        if (currentWorld != null && data != null) {
            if (world != currentWorld) {
                SNAPSHOTS.clear();
                FLAGGED.clear();
                flaggedSnapshot = Set.of();
                world = currentWorld;
            }
            int sectionCount = currentWorld.method_32890();
            int bottomSectionY = currentWorld.method_32891();
            long key = class_1923.method_8331((int)chunkX, (int)chunkZ);
            Snapshot snapshot = SNAPSHOTS.get(key);
            if (snapshot == null || snapshot.sky.length != sectionCount + 2 || snapshot.bottomSectionY != bottomSectionY) {
                snapshot = new Snapshot(sectionCount, bottomSectionY);
                SNAPSHOTS.put(key, snapshot);
            }
            ChunkLightTracker.applyTrack(data.method_38601(), data.method_38604(), data.method_38606(), snapshot.sky);
            for (int i = 1; i < snapshot.sky.length - 1; ++i) {
                int sectionBottom = (bottomSectionY + i - 1) * 16;
                int sectionTop = sectionBottom + 15;
                if (sectionTop < -64 || sectionBottom > 184 || sectionTop >= 62 || snapshot.sky[i] != LightState.ZEROED_PRESENT) continue;
                if (!FLAGGED.add(key)) break;
                flaggedSnapshot = Set.copyOf(FLAGGED);
                break;
            }
        }
    }

    public static synchronized void unload(int chunkX, int chunkZ) {
        long key = class_1923.method_8331((int)chunkX, (int)chunkZ);
        SNAPSHOTS.remove(key);
        if (FLAGGED.remove(key)) {
            flaggedSnapshot = Set.copyOf(FLAGGED);
        }
    }

    public static Set<Long> getFlaggedChunks() {
        return flaggedSnapshot;
    }

    private static void applyTrack(BitSet initialized, BitSet uninitialized, List<byte[]> payloads, LightState[] output) {
        int payloadIndex = 0;
        for (int i = 0; i < output.length; ++i) {
            if (initialized.get(i)) {
                byte[] payload = payloadIndex < payloads.size() ? payloads.get(payloadIndex) : null;
                ++payloadIndex;
                output[i] = ChunkLightTracker.classify(payload);
                continue;
            }
            if (!uninitialized.get(i)) continue;
            output[i] = LightState.EMPTY_MASK;
        }
    }

    private static LightState classify(byte[] data) {
        if (data != null && data.length == 2048) {
            boolean allZero = true;
            boolean allFull = true;
            for (byte value : data) {
                if (value != 0) {
                    allZero = false;
                }
                if (value != -1) {
                    allFull = false;
                }
                if (allZero || allFull) continue;
                return LightState.NORMAL;
            }
            if (allZero) {
                return LightState.ZEROED_PRESENT;
            }
            return allFull ? LightState.UNIFORM_MAX : LightState.NORMAL;
        }
        return LightState.UNKNOWN;
    }

    private static final class Snapshot {
        final int bottomSectionY;
        final LightState[] sky;

        Snapshot(int sectionCount, int bottomSectionY) {
            this.bottomSectionY = bottomSectionY;
            this.sky = new LightState[sectionCount + 2];
            Arrays.fill((Object[])this.sky, (Object)LightState.UNKNOWN);
        }
    }

    private static enum LightState {
        NORMAL,
        EMPTY_MASK,
        ZEROED_PRESENT,
        UNIFORM_MAX,
        UNKNOWN;

    }
}

