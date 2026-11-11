/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.SkyLightPattern;
import java.util.Arrays;

public final class ChunkLightSnapshot {
    public final int sectionCount;
    public final int bottomSectionY;
    public final SkyLightPattern[] block;
    public final SkyLightPattern[] sky;
    public final long[] blockUpdated;
    public final long[] skyUpdated;

    public ChunkLightSnapshot(int sectionCount, int bottomSectionY) {
        this.sectionCount = sectionCount;
        this.bottomSectionY = bottomSectionY;
        int length = sectionCount + 2;
        this.block = new SkyLightPattern[length];
        this.sky = new SkyLightPattern[length];
        this.blockUpdated = new long[length];
        this.skyUpdated = new long[length];
        Arrays.fill((Object[])this.block, (Object)SkyLightPattern.UNKNOWN);
        Arrays.fill((Object[])this.sky, (Object)SkyLightPattern.UNKNOWN);
    }
}

