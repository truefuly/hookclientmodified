/*
 * Decompiled with CFR 0.152.
 */
package hook.itemfx;

import java.nio.ByteBuffer;
import java.util.Random;

final class Drips {
    static final int SLOTS = 10;
    static final int ENTRIES = 48;
    private static final int GROW = 0;
    private static final int PINCH = 1;
    private static final int RETRACT = 2;
    private static final Random RANDOM = new Random();
    private static float[] anchors = new float[0];
    private static final boolean[] alive = new boolean[10];
    private static final int[] stage = new int[10];
    private static final float[] ax = new float[10];
    private static final float[] ay = new float[10];
    private static final float[] az = new float[10];
    private static final float[] age = new float[10];
    private static final float[] growTime = new float[10];
    private static final float[] maxLen = new float[10];
    private static final float[] maxR = new float[10];
    private static final float[] len = new float[10];
    private static final float[] rad = new float[10];
    private static final float[] neck = new float[10];
    private static final boolean[] falling = new boolean[10];
    private static final float[] fy = new float[10];
    private static final float[] fv = new float[10];
    private static final float[] fr = new float[10];
    private static final float[] fage = new float[10];
    private static float nextSpawn = 0.3f;

    private Drips() {
    }

    static void setAnchors(float[] var0) {
        anchors = var0;
    }

    static void reset() {
        for (int var0 = 0; var0 < 10; ++var0) {
            Drips.alive[var0] = false;
            Drips.falling[var0] = false;
        }
    }

    static void update(float var0) {
        if (!(var0 <= 0.0f) && anchors.length >= 3) {
            if ((nextSpawn -= (var0 = Math.min(var0, 0.1f))) <= 0.0f) {
                nextSpawn = 0.35f + RANDOM.nextFloat() * 0.9f;
                Drips.spawn();
            }
            block4: for (int var1 = 0; var1 < 10; ++var1) {
                if (falling[var1]) {
                    Drips.fage[var1] = fage[var1] + var0;
                    Drips.fv[var1] = fv[var1] + 1.9f * var0;
                    Drips.fy[var1] = fy[var1] - fv[var1] * var0;
                    if (fage[var1] > 0.75f || fy[var1] < -0.62f) {
                        Drips.falling[var1] = false;
                    }
                }
                if (!alive[var1]) continue;
                Drips.age[var1] = age[var1] + var0;
                switch (stage[var1]) {
                    case 0: {
                        float var6 = Math.min(1.0f, age[var1] / growTime[var1]);
                        float var7 = 1.0f - (1.0f - var6) * (1.0f - var6);
                        Drips.len[var1] = 0.006f + (maxLen[var1] - 0.006f) * var7;
                        Drips.rad[var1] = 0.005f + (maxR[var1] - 0.005f) * (float)Math.sqrt(var6);
                        Drips.neck[var1] = rad[var1] * (0.75f - 0.3f * var6);
                        if (!(var6 >= 1.0f)) continue block4;
                        Drips.stage[var1] = 1;
                        Drips.age[var1] = 0.0f;
                        continue block4;
                    }
                    case 1: {
                        float var5 = Math.min(1.0f, age[var1] / 0.28f);
                        Drips.len[var1] = maxLen[var1] * (1.0f + 0.18f * var5);
                        Drips.neck[var1] = maxR[var1] * 0.45f * (1.0f - var5);
                        if (!(var5 >= 1.0f)) continue block4;
                        Drips.falling[var1] = true;
                        Drips.fage[var1] = 0.0f;
                        Drips.fv[var1] = 0.05f;
                        Drips.fr[var1] = rad[var1] * 0.95f;
                        Drips.fy[var1] = ay[var1] - len[var1] + rad[var1];
                        Drips.stage[var1] = 2;
                        Drips.age[var1] = 0.0f;
                        continue block4;
                    }
                    default: {
                        float var2 = Math.min(1.0f, age[var1] / 0.45f);
                        float var3 = var2 * var2 * (3.0f - 2.0f * var2);
                        Drips.len[var1] = Math.max(0.004f, maxLen[var1] * 0.55f * (1.0f - var3));
                        Drips.rad[var1] = Math.max(0.003f, maxR[var1] * 0.55f * (1.0f - var3));
                        Drips.neck[var1] = rad[var1] * 0.7f;
                        if (!(var2 >= 1.0f)) continue block4;
                        Drips.alive[var1] = false;
                    }
                }
            }
        }
    }

    private static void spawn() {
        int var0 = -1;
        for (int var1 = 0; var1 < 10; ++var1) {
            if (alive[var1] || falling[var1]) continue;
            var0 = var1;
            break;
        }
        if (var0 >= 0) {
            int var3 = anchors.length / 3;
            int var2 = RANDOM.nextInt(var3);
            Drips.alive[var0] = true;
            Drips.stage[var0] = 0;
            Drips.age[var0] = 0.0f;
            Drips.ax[var0] = anchors[var2 * 3];
            Drips.ay[var0] = anchors[var2 * 3 + 1] + 0.003f;
            Drips.az[var0] = anchors[var2 * 3 + 2];
            Drips.growTime[var0] = 1.3f + RANDOM.nextFloat() * 1.6f;
            Drips.maxLen[var0] = 0.032f + RANDOM.nextFloat() * 0.045f;
            Drips.maxR[var0] = 0.0095f + RANDOM.nextFloat() * 0.0065f;
            Drips.len[var0] = 0.006f;
            Drips.rad[var0] = 0.005f;
            Drips.neck[var0] = 0.004f;
        }
    }

    static int write(ByteBuffer var0, int var1) {
        int var2 = 0;
        for (int var3 = 0; var3 < 10 && var2 < 24; ++var3) {
            if (alive[var3]) {
                int var4 = var1 + 16 + var2 * 32;
                Drips.put4(var0, var4, ax[var3], ay[var3], az[var3], 0.0f);
                Drips.put4(var0, var4 + 16, len[var3], rad[var3], neck[var3], 0.017f);
                ++var2;
            }
            if (!falling[var3] || var2 >= 24) continue;
            int var9 = var1 + 16 + var2 * 32;
            float var5 = Math.min(0.7f, fv[var3] * 0.35f);
            float var6 = 1.0f - Math.max(0.0f, (fage[var3] - 0.5f) / 0.25f);
            float var7 = fr[var3] * Math.max(0.0f, var6);
            float var8 = fy[var3] + var7 * (1.0f + var5);
            Drips.put4(var0, var9, ax[var3], var8, az[var3], 1.0f);
            Drips.put4(var0, var9 + 16, var5, var7, 0.0f, 0.0f);
            ++var2;
        }
        Drips.put4(var0, var1, var2, 0.0f, 0.0f, 0.0f);
        return var2;
    }

    private static void put4(ByteBuffer var0, int var1, float var2, float var3, float var4, float var5) {
        var0.putFloat(var1, var2);
        var0.putFloat(var1 + 4, var3);
        var0.putFloat(var1 + 8, var4);
        var0.putFloat(var1 + 12, var5);
    }
}

