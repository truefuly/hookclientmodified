/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.schematic;

public final class BitPacking {
    private BitPacking() {
    }

    public static int bitsForPalette(int paletteSize) {
        if (paletteSize <= 1) {
            return 2;
        }
        return Math.max(2, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
    }

    public static int[] unpack(long[] data, int bits, int count) {
        if (bits < 1 || bits > 32) {
            throw new IllegalArgumentException("invalid bit width: " + bits);
        }
        if (count < 0) {
            throw new IllegalArgumentException("invalid count: " + count);
        }
        int[] values = new int[count];
        if (count == 0) {
            return values;
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("missing block state data");
        }
        long mask = (1L << bits) - 1L;
        for (int i = 0; i < count; ++i) {
            long startOffset = (long)i * (long)bits;
            int startIndex = (int)(startOffset >> 6);
            int endIndex = (int)(((long)i + 1L) * (long)bits - 1L >> 6);
            int startBit = (int)(startOffset & 0x3FL);
            if (startIndex >= data.length) {
                throw new IllegalArgumentException("block state data too short: need index " + startIndex + " of " + data.length);
            }
            if (startIndex == endIndex) {
                values[i] = (int)(data[startIndex] >>> startBit & mask);
                continue;
            }
            if (endIndex >= data.length) {
                throw new IllegalArgumentException("block state data too short: need index " + endIndex + " of " + data.length);
            }
            int endOffset = 64 - startBit;
            values[i] = (int)((data[startIndex] >>> startBit | data[endIndex] << endOffset) & mask);
        }
        return values;
    }

    public static int[] readVarInts(byte[] data, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("invalid count: " + count);
        }
        int[] values = new int[count];
        if (count == 0) {
            return values;
        }
        if (data == null) {
            throw new IllegalArgumentException("missing block data");
        }
        int index = 0;
        int pos = 0;
        while (index < count) {
            int value;
            block6: {
                value = 0;
                int shift = 0;
                do {
                    if (pos >= data.length) {
                        throw new IllegalArgumentException("block data truncated: expected " + count + " entries, decoded " + index);
                    }
                    byte next = data[pos++];
                    value |= (next & 0x7F) << shift;
                    if ((next & 0x80) == 0) break block6;
                } while ((shift += 7) <= 35);
                throw new IllegalArgumentException("malformed varint in block data");
            }
            values[index++] = value;
        }
        return values;
    }

    public static long[] pack(int[] values, int bits) {
        if (bits < 1 || bits > 32) {
            throw new IllegalArgumentException("invalid bit width: " + bits);
        }
        if (values.length == 0) {
            return new long[0];
        }
        long mask = (1L << bits) - 1L;
        long[] data = new long[(int)Math.ceil((double)values.length * (double)bits / 64.0)];
        for (int i = 0; i < values.length; ++i) {
            long startOffset = (long)i * (long)bits;
            int startIndex = (int)(startOffset >> 6);
            int endIndex = (int)(((long)i + 1L) * (long)bits - 1L >> 6);
            int startBit = (int)(startOffset & 0x3FL);
            long value = (long)values[i] & mask;
            data[startIndex] = data[startIndex] & (mask << startBit ^ 0xFFFFFFFFFFFFFFFFL) | value << startBit;
            if (startIndex == endIndex) continue;
            int endOffset = 64 - startBit;
            data[endIndex] = data[endIndex] >>> bits - endOffset << bits - endOffset | value >>> endOffset;
        }
        return data;
    }
}

