/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2IntMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  net.minecraft.class_1923
 */
package hook.utils.chunks;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import net.minecraft.class_1923;

public final class ChunkPeakFinder {
    private static final long MAX_GRID_CELLS = 0x100000L;
    private int[] grid = new int[0];
    private int[] horizontal = new int[0];
    private int[] maxima = new int[0];
    private int[] deque = new int[0];

    public LongOpenHashSet find(Long2IntOpenHashMap samples, int radius) {
        int z;
        int x;
        LongOpenHashSet peaks = new LongOpenHashSet();
        if (samples.isEmpty()) {
            return peaks;
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        LongIterator positions = samples.keySet().iterator();
        while (positions.hasNext()) {
            long position = positions.nextLong();
            int x2 = class_1923.method_8325((long)position);
            int z2 = class_1923.method_8332((long)position);
            minX = Math.min(minX, x2);
            maxX = Math.max(maxX, x2);
            minZ = Math.min(minZ, z2);
            maxZ = Math.max(maxZ, z2);
        }
        long width = (long)maxX - (long)minX + 1L;
        long height = (long)maxZ - (long)minZ + 1L;
        if (width > 0x100000L || height > 0x100000L || width * height > 0x100000L) {
            return this.findSparse(samples, radius, peaks);
        }
        int columns = (int)width;
        int rows = (int)height;
        int cells = columns * rows;
        if (this.grid.length < cells) {
            this.grid = new int[cells];
            this.horizontal = new int[cells];
            this.maxima = new int[cells];
        } else {
            Arrays.fill(this.grid, 0, cells, 0);
        }
        if (this.deque.length < Math.max(columns, rows)) {
            this.deque = new int[Math.max(columns, rows)];
        }
        for (Long2IntMap.Entry entry : samples.long2IntEntrySet()) {
            x = class_1923.method_8325((long)entry.getLongKey()) - minX;
            z = class_1923.method_8332((long)entry.getLongKey()) - minZ;
            this.grid[z * columns + x] = entry.getIntValue();
        }
        for (int z3 = 0; z3 < rows; ++z3) {
            this.window(this.grid, this.horizontal, z3 * columns, 1, columns, radius);
        }
        for (int x3 = 0; x3 < columns; ++x3) {
            this.window(this.horizontal, this.maxima, x3, columns, rows, radius);
        }
        for (Long2IntMap.Entry entry : samples.long2IntEntrySet()) {
            x = class_1923.method_8325((long)entry.getLongKey()) - minX;
            z = class_1923.method_8332((long)entry.getLongKey()) - minZ;
            if (entry.getIntValue() < this.maxima[z * columns + x]) continue;
            peaks.add(entry.getLongKey());
        }
        return peaks;
    }

    private LongOpenHashSet findSparse(Long2IntOpenHashMap samples, int radius, LongOpenHashSet peaks) {
        for (Long2IntMap.Entry entry : samples.long2IntEntrySet()) {
            int x = class_1923.method_8325((long)entry.getLongKey());
            int z = class_1923.method_8332((long)entry.getLongKey());
            boolean peak = true;
            block1: for (int dz = -radius; dz <= radius; ++dz) {
                for (int dx = -radius; dx <= radius; ++dx) {
                    if (dz == 0 && dx == 0 || samples.get(class_1923.method_8331((int)(x + dz), (int)(z + dx))) <= entry.getIntValue()) continue;
                    peak = false;
                    break block1;
                }
            }
            if (!peak) continue;
            peaks.add(entry.getLongKey());
        }
        return peaks;
    }

    private void window(int[] input, int[] output, int start, int stride, int length, int radius) {
        int head = 0;
        int tail = 0;
        int next = 0;
        for (int center = 0; center < length; ++center) {
            int end = Math.min(length - 1, center + radius);
            while (next <= end) {
                int value = input[start + next * stride];
                while (tail > head && input[start + this.deque[tail - 1] * stride] <= value) {
                    --tail;
                }
                this.deque[tail++] = next++;
            }
            while (tail > head && this.deque[head] < center - radius) {
                ++head;
            }
            output[start + center * stride] = input[start + this.deque[head] * stride];
        }
    }
}

