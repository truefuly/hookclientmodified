/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.mining;

import com.hookaddon.mining.MiningPattern;
import java.util.Arrays;

public final class AreaGeometry {
    private AreaGeometry() {
    }

    public static int[] order(int width, int height, int depth, MiningPattern pattern, int stripWidth, int stripGap) {
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw new IllegalArgumentException("area dimensions must be positive");
        }
        int safeStripWidth = Math.max(1, stripWidth);
        int safeGap = Math.max(0, stripGap);
        return switch (pattern) {
            default -> throw new MatchException(null, null);
            case MiningPattern.LAYER_BY_LAYER -> AreaGeometry.fullOrder(width, height, depth, false);
            case MiningPattern.GRID -> AreaGeometry.fullOrder(width, height, depth, true);
            case MiningPattern.STRIP -> AreaGeometry.stripOrder(width, height, depth, safeStripWidth, safeGap, false);
            case MiningPattern.TUNNEL -> AreaGeometry.stripOrder(width, height, depth, 1, safeGap, true);
        };
    }

    public static int count(int width, int height, int depth, MiningPattern pattern, int stripWidth, int stripGap) {
        if (width <= 0 || height <= 0 || depth <= 0) {
            return 0;
        }
        int safeStripWidth = Math.max(1, stripWidth);
        int safeGap = Math.max(0, stripGap);
        return switch (pattern) {
            default -> throw new MatchException(null, null);
            case MiningPattern.LAYER_BY_LAYER, MiningPattern.GRID -> width * height * depth;
            case MiningPattern.STRIP -> AreaGeometry.countStrips(width, safeStripWidth, safeGap) * height * depth;
            case MiningPattern.TUNNEL -> AreaGeometry.countStrips(width, 1, safeGap) * height * depth;
        };
    }

    private static int countStrips(int width, int stripWidth, int stripGap) {
        int period = stripWidth + stripGap;
        int complete = width / period;
        int remainder = width % period;
        return complete * stripWidth + Math.min(remainder, stripWidth);
    }

    private static int[] fullOrder(int width, int height, int depth, boolean threeDimensional) {
        int[] order = new int[width * height * depth * 3];
        int index = 0;
        for (int y = 0; y < height; ++y) {
            boolean reverseRows = threeDimensional && (y & 1) == 1;
            for (int row = 0; row < width; ++row) {
                int x;
                int n = x = reverseRows ? width - 1 - row : row;
                boolean reverseCells = threeDimensional ? (row & 1) == 1 : (row + y & 1) == 1;
                for (int cell = 0; cell < depth; ++cell) {
                    int z = reverseCells ? depth - 1 - cell : cell;
                    order[index++] = x;
                    order[index++] = y;
                    order[index++] = z;
                }
            }
        }
        return order;
    }

    private static int[] stripOrder(int width, int height, int depth, int stripWidth, int stripGap, boolean tunnel) {
        int[] xs = AreaGeometry.stripPositions(width, stripWidth, stripGap);
        int[] order = new int[Math.max(1, xs.length * height * depth) * 3];
        int index = 0;
        if (tunnel) {
            for (int pairStart = 0; pairStart < height; pairStart += 2) {
                int pairEnd = Math.min(height, pairStart + 2);
                for (int xi = 0; xi < xs.length; ++xi) {
                    int x = xs[xi];
                    boolean reverse = (xi & 1) == 1;
                    for (int cell = 0; cell < depth; ++cell) {
                        int z = reverse ? depth - 1 - cell : cell;
                        int y = pairStart;
                        while (y < pairEnd) {
                            order[index++] = x;
                            order[index++] = y++;
                            order[index++] = z;
                        }
                    }
                }
            }
            return Arrays.copyOf(order, index);
        }
        for (int y = 0; y < height; ++y) {
            for (int xi = 0; xi < xs.length; ++xi) {
                int x = xs[xi];
                boolean reverse = (xi + y & 1) == 1;
                for (int cell = 0; cell < depth; ++cell) {
                    int z = reverse ? depth - 1 - cell : cell;
                    order[index++] = x;
                    order[index++] = y;
                    order[index++] = z;
                }
            }
        }
        return Arrays.copyOf(order, index);
    }

    private static int[] stripPositions(int width, int stripWidth, int stripGap) {
        int period = Math.max(1, stripWidth + stripGap);
        int[] positions = new int[width];
        int count = 0;
        for (int x = 0; x < width; ++x) {
            if (x % period >= stripWidth) continue;
            positions[count++] = x;
        }
        return Arrays.copyOf(positions, count);
    }
}

