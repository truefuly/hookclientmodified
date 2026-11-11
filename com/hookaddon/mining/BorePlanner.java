/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.mining;

public final class BorePlanner {
    private BorePlanner() {
    }

    public static int[] plan(int width, int height, int depth, int sign, int yOffset) {
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw new IllegalArgumentException("bore dimensions must be positive");
        }
        if (sign == 0) {
            throw new IllegalArgumentException("travel sign must be non-zero");
        }
        int[] order = new int[width * height * depth * 3];
        int index = 0;
        int half = (width - 1) / 2;
        for (int slice = 0; slice < depth; ++slice) {
            int forward = sign * (1 + slice);
            for (int layer = 0; layer < height; ++layer) {
                int up = yOffset + layer;
                boolean reverse = (layer & 1) == 1;
                for (int cell = 0; cell < width; ++cell) {
                    int cross = reverse ? width - 1 - cell - half : cell - half;
                    order[index++] = forward;
                    order[index++] = cross;
                    order[index++] = up;
                }
            }
        }
        return order;
    }

    public static int count(int width, int height, int depth) {
        if (width <= 0 || height <= 0 || depth <= 0) {
            return 0;
        }
        return width * height * depth;
    }
}

