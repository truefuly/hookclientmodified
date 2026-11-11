/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_6606
 */
package hook.feature;

import hook.feature.ChunkLightSnapshot;
import hook.feature.SkyLightPattern;
import java.util.BitSet;
import java.util.List;
import net.minecraft.class_6606;

public final class LightPayloadClassifier {
    private static final int NIBBLES_PER_SECTION = 2048;

    private LightPayloadClassifier() {
    }

    public static SkyLightPattern classifyPayload(byte[] data) {
        if (data == null || data.length != 2048) {
            return SkyLightPattern.UNKNOWN;
        }
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
            return SkyLightPattern.NORMAL;
        }
        return allZero ? SkyLightPattern.ZEROED_PRESENT : SkyLightPattern.UNIFORM_MAX;
    }

    public static void apply(class_6606 data, ChunkLightSnapshot snapshot, long stamp) {
        LightPayloadClassifier.applyTrack(data.method_38608(), data.method_38609(), data.method_38610(), snapshot.block, snapshot.blockUpdated, stamp);
        LightPayloadClassifier.applyTrack(data.method_38601(), data.method_38604(), data.method_38606(), snapshot.sky, snapshot.skyUpdated, stamp);
    }

    private static void applyTrack(BitSet initialized, BitSet uninitialized, List<byte[]> payloads, SkyLightPattern[] output, long[] updated, long stamp) {
        int payloadIndex = 0;
        for (int section = 0; section < output.length; ++section) {
            if (initialized.get(section)) {
                byte[] payload = payloadIndex < payloads.size() ? payloads.get(payloadIndex) : null;
                ++payloadIndex;
                output[section] = LightPayloadClassifier.classifyPayload(payload);
                updated[section] = stamp;
                continue;
            }
            if (!uninitialized.get(section)) continue;
            output[section] = SkyLightPattern.EMPTY_MASK;
            updated[section] = stamp;
        }
    }
}

