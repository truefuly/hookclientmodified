/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.GltfCharacterBaker;
import hook.feature.PosedGltfMesh;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class HeightSliceLimbPartitioner {
    private static final int BANDS = 160;
    private static final int BRIDGE = 5;
    private static final int NO_TRUNK = -2;
    private final PosedGltfMesh mesh;
    private final float height;
    private final float step;
    private final float gap;
    private final List<float[]>[] spans;
    private final float[][][] intervals;
    private final int[][] labels;

    private HeightSliceLimbPartitioner(PosedGltfMesh var1) {
        this.mesh = var1;
        this.height = var1.bounds()[4];
        this.step = this.height / 160.0f;
        this.gap = this.height * 0.004f;
        this.spans = new List[160];
        for (int var2 = 0; var2 < 160; ++var2) {
            this.spans[var2] = new ArrayList<float[]>();
        }
        this.intervals = new float[160][][];
        this.labels = new int[160][];
    }

    static GltfCharacterBaker.PartProfile cut(PosedGltfMesh var0) {
        HeightSliceLimbPartitioner var1 = new HeightSliceLimbPartitioner(var0);
        if (!(var1.height > 0.0f)) {
            return null;
        }
        var1.slice();
        return var1.find();
    }

    private void slice() {
        float[] positions = this.mesh.pos;
        for (int triangle = 0; triangle < this.mesh.triangles; ++triangle) {
            int firstVertex = this.mesh.tris[triangle * 3];
            int secondVertex = this.mesh.tris[triangle * 3 + 1];
            int thirdVertex = this.mesh.tris[triangle * 3 + 2];
            float firstY = positions[firstVertex * 3 + 1];
            float secondY = positions[secondVertex * 3 + 1];
            float thirdY = positions[thirdVertex * 3 + 1];
            float lowestY = Math.min(firstY, Math.min(secondY, thirdY));
            float highestY = Math.max(firstY, Math.max(secondY, thirdY));
            int firstBand = Math.max(0, (int)Math.ceil(lowestY / this.step - 0.5f));
            int lastBand = Math.min(159, (int)Math.floor(highestY / this.step - 0.5f));
            for (int band = firstBand; band <= lastBand; ++band) {
                int[][] edges;
                float level = ((float)band + 0.5f) * this.step;
                float left = Float.MAX_VALUE;
                float right = -3.4028235E38f;
                for (int[] edge : edges = new int[][]{{firstVertex, secondVertex}, {secondVertex, thirdVertex}, {thirdVertex, firstVertex}}) {
                    float side;
                    float startY = positions[edge[0] * 3 + 1];
                    float endY = positions[edge[1] * 3 + 1];
                    if (startY - endY == 0.0f || !((side = (level - startY) * (level - endY)) <= 0.0f)) continue;
                    float startX = positions[edge[0] * 3];
                    float endX = positions[edge[1] * 3];
                    float x = startX + (endX - startX) * (level - startY) / (endY - startY);
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                }
                if (!(left - right <= 0.0f)) continue;
                this.spans[band].add(new float[]{left, right});
            }
        }
        for (int band = 0; band < 160; ++band) {
            List<float[]> segments = this.spans[band];
            segments.sort((a, b) -> Float.compare(a[0], b[0]));
            ArrayList<float[]> merged = new ArrayList<float[]>();
            for (float[] segment : segments) {
                float[] previous;
                float[] fArray = previous = merged.isEmpty() ? null : (float[])merged.get(merged.size() - 1);
                if (previous != null && segment[0] - (previous[1] + this.gap) <= 0.0f) {
                    previous[1] = Math.max(previous[1], segment[1]);
                    continue;
                }
                merged.add(new float[]{segment[0], segment[1]});
            }
            this.intervals[band] = (float[][])merged.toArray(new float[0][]);
            this.labels[band] = new int[this.intervals[band].length];
            this.spans[band] = null;
        }
    }

    private GltfCharacterBaker.PartProfile find() {
        boolean var27;
        int var1 = this.crotch();
        int[] var2 = new int[320];
        float[] var3 = null;
        for (int var4 = 0; var4 < 160; ++var4) {
            int[] var5 = this.innerIntervals(var4, var4 < var1);
            if (var4 >= var1 && var5[0] >= 0) {
                boolean var8;
                float[] var6 = this.intervals[var4][var5[0]];
                boolean var7 = var6[0] <= 0.0f && var6[1] >= 0.0f;
                boolean bl = var8 = var3 != null && var6[0] <= var3[1] && var6[1] >= var3[0];
                if (!var7 && !var8) {
                    int[] var10000 = new int[]{-2, -2};
                    var5 = var10000;
                } else {
                    var3 = var6;
                }
            }
            var2[var4 * 2] = var5[0];
            var2[var4 * 2 + 1] = var5[1];
        }
        HeightSlice[] var46 = new HeightSlice[]{this.arm(var2, var1, 0), this.arm(var2, var1, 1)};
        HeightSlice[] var24 = var46;
        int var25 = Math.max(var24[0] == null ? -1 : var24[0].to(), var24[1] == null ? -1 : var24[1].to());
        int var26 = this.neck(var2, var1, var25 < 0 ? 112 : var25);
        boolean bl = var27 = var1 > 0;
        if (!var27 && var24[0] == null && var24[1] == null && var26 < 0) {
            return null;
        }
        for (int var28 = 0; var28 < 160; ++var28) {
            float[][] var9 = this.intervals[var28];
            for (int var10 = 0; var10 < var9.length; ++var10) {
                int var11 = 1;
                if (var28 < var1 && var27) {
                    if (var10 == var2[var28 * 2]) {
                        var11 = 4;
                    } else if (var10 == var2[var28 * 2 + 1]) {
                        var11 = 5;
                    }
                }
                if (var26 >= 0 && var28 > var26 && var11 == 1) {
                    var11 = 0;
                }
                this.labels[var28][var10] = var11;
            }
        }
        int[] var29 = new int[this.mesh.vertices];
        for (int var30 = 0; var30 < this.mesh.vertices; ++var30) {
            float var33 = this.mesh.pos[var30 * 3];
            float var35 = this.mesh.pos[var30 * 3 + 1];
            int var12 = Math.max(0, Math.min(159, (int)(var35 / this.step)));
            var29[var30] = this.label(var33, var35);
            for (int var13 = 0; var13 < 2; ++var13) {
                HeightSlice var14 = var24[var13];
                if (var14 == null || var12 < var14.from() || var12 > var14.to() || !(var13 == 0 ? var33 < var14.edge()[var12] : var33 > var14.edge()[var12])) continue;
                var29[var30] = var13 == 0 ? 2 : 3;
            }
        }
        if (this.share(var29, 2) + this.share(var29, 3) > 0.4f) {
            for (int var31 = 0; var31 < var29.length; ++var31) {
                if (var29[var31] != 2 && var29[var31] != 3) continue;
                var29[var31] = 1;
            }
            var24 = new HeightSlice[2];
        }
        if (var24[0] != null || var24[1] != null) {
            var29 = this.mesh.separate(var29);
        }
        float[][] var32 = new float[6][];
        float[][] var34 = new float[6][];
        float[][] var36 = new float[6][];
        float[][] var37 = new float[6][];
        int var10001 = 1;
        float[] var10002 = new float[]{0.0f, 24.0f, 0.0f};
        var32[var10001] = var10002;
        float var38 = this.height * 0.02f;
        if (var27) {
            float var39 = (float)var1 * this.step;
            int[] var47 = new int[]{4, 5};
            for (int var18 : var47) {
                var32[var18] = this.mean(var29, var18, var39 - this.height * 0.06f, var39);
                var32[var18][1] = var39;
                var36[var18] = this.mean(var29, var18, var39 * 0.45f, var39 * 0.55f);
                var36[var18][1] = var39 * 0.5f;
                var37[var18] = this.below(var36[var18][1], var38);
            }
        }
        for (int var40 = 0; var40 < 2; ++var40) {
            HeightSlice var42 = var24[var40];
            if (var42 == null) continue;
            int var43 = var40 == 0 ? 2 : 3;
            float var44 = (float)(var42.to() + 1) * this.step;
            float var45 = (float)var42.from() * this.step;
            float[] var19 = this.mean(var29, var43, var44 - this.height * 0.05f, var44);
            float[] var20 = this.mean(var29, var43, var45, var45 + this.height * 0.05f);
            var19[1] = var44;
            var20[1] = var45 + this.height * 0.03f;
            boolean var21 = this.attached(var42, var40, var42.from(), -1) && !this.attached(var42, var40, var42.to(), 1);
            var10002 = var21 ? new float[]{var20[0], var45, var20[2]} : var19;
            var32[var43] = var10002;
            var10002 = var21 ? new float[]{var19[0], var44 - this.height * 0.03f, var19[2]} : var20;
            var34[var43] = var10002;
            this.lower(var29, var43, var32[var43], var34[var43]);
            float[] var22 = new float[3];
            var36[var43] = new float[3];
            for (int var23 = 0; var23 < 3; ++var23) {
                var36[var43][var23] = (var32[var43][var23] + var34[var43][var23]) * 0.5f;
                var22[var23] = var34[var43][var23] - var32[var43][var23];
            }
            var37[var43] = this.beyond(var36[var43], var22, var38);
        }
        if (var26 >= 0) {
            float var41 = (float)(var26 + 1) * this.step;
            var32[0] = this.mean(var29, 0, var41, var41 + this.height * 0.04f);
            var32[0][1] = var41;
        }
        return new GltfCharacterBaker.PartProfile(var29, var32, var34, var36, var37);
    }

    private int crotch() {
        int var2;
        int var1 = -1;
        for (var2 = 0; var2 < 160 && this.intervals[var2].length == 0; ++var2) {
        }
        int var3 = var2 + 19;
        while (var2 < 160) {
            boolean var4 = this.splitAtCentre(var2);
            if (var1 < 0) {
                if (var4) {
                    var1 = var2;
                } else if (var2 > var3) {
                    return 0;
                }
            } else if (!var4) {
                return (float)var2 * this.step >= this.height * 0.1f && (float)var2 * this.step <= this.height * 0.65f ? var2 : 0;
            }
            ++var2;
        }
        return 0;
    }

    private boolean splitAtCentre(int var1) {
        float[][] var2 = this.intervals[var1];
        if (var2.length < 2) {
            return false;
        }
        float[][] var3 = var2;
        int var4 = var2.length;
        for (int var5 = 0; var5 < var4; ++var5) {
            float[] var6 = var3[var5];
            if (!(var6[0] <= 0.0f) || !(var6[1] >= 0.0f)) continue;
            return false;
        }
        return var2[0][1] < 0.0f && var2[var2.length - 1][0] > 0.0f;
    }

    private int[] innerIntervals(int var1, boolean var2) {
        float[][] var3 = this.intervals[var1];
        if (var3.length == 0) {
            int[] var12 = new int[]{-1, -1};
            return var12;
        }
        if (var2) {
            int var8 = -1;
            int var9 = -1;
            for (int var10 = 0; var10 < var3.length; ++var10) {
                if (var3[var10][1] < 0.0f) {
                    var8 = var10;
                    continue;
                }
                if (var9 >= 0) continue;
                var9 = var10;
            }
            int[] var11 = new int[]{var8, var9};
            return var11;
        }
        int var4 = 0;
        float var5 = Float.MAX_VALUE;
        for (int var6 = 0; var6 < var3.length; ++var6) {
            float var7;
            var7 = var3[var6][0] > 0.0f ? var3[var6][0] : (var3[var6][1] < 0.0f ? -var3[var6][1] : 0.0f);
            if (!(var7 < var5)) continue;
            var5 = var7;
            var4 = var6;
        }
        int[] var10000 = new int[]{var4, var4};
        return var10000;
    }

    private HeightSlice arm(int[] var1, int var2, int var3) {
        float[] var4 = new float[160];
        Arrays.fill(var4, Float.intBitsToFloat(2143289344));
        for (int var5 = 0; var5 < 160; ++var5) {
            int var6 = var1[var5 * 2 + var3];
            float[][] var7 = this.intervals[var5];
            if (var6 == -2 && var7.length > 0 && (var3 == 0 ? var7[0][0] < 0.0f : var7[var7.length - 1][1] > 0.0f)) {
                var4[var5] = 0.0f;
            }
            if (var6 < 0) continue;
            if (var3 == 0 && var6 > 0) {
                var4[var5] = (var7[var6 - 1][1] + var7[var6][0]) * 0.5f;
            }
            if (var3 != 1 || var6 >= var7.length - 1) continue;
            var4[var5] = (var7[var6][1] + var7[var6 + 1][0]) * 0.5f;
        }
        int var12 = -1;
        int var13 = -1;
        int var14 = -1;
        int var8 = -1;
        for (int var9 = 0; var9 <= 160; ++var9) {
            boolean var10;
            boolean bl = var10 = var9 < 160 && !Float.isNaN(var4[var9]);
            if (!var10) continue;
            if (var14 >= 0 && var9 - var8 - 1 > 5) {
                if (var8 - var14 > var13 - var12) {
                    var12 = var14;
                    var13 = var8;
                }
                var14 = -1;
            }
            if (var14 < 0) {
                var14 = var9;
            }
            var8 = var9;
        }
        if (var14 >= 0 && var8 - var14 > var13 - var12) {
            var12 = var14;
            var13 = var8;
        }
        if (var12 >= 0 && !((float)(var13 - var12 + 1) < 12.799999f) && var13 > var2) {
            for (int var15 = var12 + 1; var15 < var13; ++var15) {
                if (!Float.isNaN(var4[var15])) continue;
                int var16 = var15 - 1;
                int var11 = var15 + 1;
                while (Float.isNaN(var4[var11])) {
                    ++var11;
                }
                var4[var15] = var3 == 0 ? Math.min(var4[var16], var4[var11]) : Math.max(var4[var16], var4[var11]);
            }
            return new HeightSlice(var12, var13, var4);
        }
        return null;
    }

    private boolean attached(HeightSlice var1, int var2, int var3, int var4) {
        float var5 = var1.edge()[var3];
        boolean var6 = var2 == 0;
        for (int var7 = 1; var7 <= 2; ++var7) {
            int var8 = var3 + var4 * var7;
            if (var8 < 0 || var8 >= 160) {
                return false;
            }
            for (float[] var12 : this.intervals[var8]) {
                if (!(var6 ? var12[0] < var5 : var12[1] > var5)) continue;
                return true;
            }
        }
        return false;
    }

    private void lower(int[] var1, int var2, float[] var3, float[] var4) {
        float var9;
        float var5 = var4[0] - var3[0];
        float var6 = var4[1] - var3[1];
        float var7 = var4[2] - var3[2];
        float var8 = (float)Math.sqrt(var5 * var5 + var6 * var6 + var7 * var7);
        if (!(var8 < 1.0E-4f) && !((double)(var9 = (float)Math.acos(Math.max(-1.0f, Math.min(1.0f, -var6 / var8)))) < Math.toRadians(40.0))) {
            float var10 = var7;
            float var11 = -var5;
            float var12 = (float)Math.sqrt(var7 * var7 + var11 * var11);
            if (var12 < 1.0E-5f) {
                var10 = 0.0f;
                var11 = 1.0f;
                var12 = 1.0f;
            }
            var10 /= var12;
            var11 /= var12;
            float var13 = var9 - (float)Math.toRadians(10.0);
            float var14 = (float)Math.cos(var13);
            float var15 = (float)Math.sin(var13);
            for (int var16 = 0; var16 < var1.length; ++var16) {
                if (var1[var16] != var2) continue;
                HeightSliceLimbPartitioner.rotate(this.mesh.pos, var16 * 3, var3, var10, var11, var14, var15);
                HeightSliceLimbPartitioner.rotate(this.mesh.nrm, var16 * 3, null, var10, var11, var14, var15);
            }
            float[] var19 = (float[])var4.clone();
            HeightSliceLimbPartitioner.rotate(var19, 0, var3, var10, var11, var14, var15);
            System.arraycopy(var19, 0, var4, 0, 3);
        }
    }

    private static void rotate(float[] var0, int var1, float[] var2, float var3, float var4, float var5, float var6) {
        float var7 = var0[var1] - (var2 == null ? 0.0f : var2[0]);
        float var8 = var0[var1 + 1] - (var2 == null ? 0.0f : var2[1]);
        float var9 = var0[var1 + 2] - (var2 == null ? 0.0f : var2[2]);
        float var10 = var3 * var7 + var4 * var9;
        float var11 = -var4 * var8;
        float var12 = var4 * var7 - var3 * var9;
        float var13 = var3 * var8;
        float var14 = var7 * var5 + var11 * var6 + var3 * var10 * (1.0f - var5);
        float var15 = var8 * var5 + var12 * var6;
        float var16 = var9 * var5 + var13 * var6 + var4 * var10 * (1.0f - var5);
        var0[var1] = var14 + (var2 == null ? 0.0f : var2[0]);
        var0[var1 + 1] = var15 + (var2 == null ? 0.0f : var2[1]);
        var0[var1 + 2] = var16 + (var2 == null ? 0.0f : var2[2]);
    }

    private int neck(int[] var1, int var2, int var3) {
        int var4 = 154;
        int var5 = Math.max(var3, var2 + 1);
        int var6 = -1;
        float var7 = Float.MAX_VALUE;
        for (int var8 = var5; var8 < var4; ++var8) {
            float var9 = this.torsoWidth(var1, var8);
            if (!(var9 > 0.0f) || !(var9 < var7)) continue;
            var7 = var9;
            var6 = var8;
        }
        if (var6 < 0) {
            return -1;
        }
        float var12 = 0.0f;
        float var13 = this.torsoWidth(var1, var5);
        for (int var10 = var6 + 1; var10 < 160; ++var10) {
            var12 = Math.max(var12, this.torsoWidth(var1, var10));
        }
        float var14 = (float)(160 - var6) * this.step;
        boolean var11 = var7 < var12 * 0.75f && var7 < var13 * 0.8f;
        return var11 && var14 > this.height * 0.08f && var14 < this.height * 0.45f ? var6 : -1;
    }

    private float torsoWidth(int[] var1, int var2) {
        int var3 = var1[var2 * 2];
        return var3 < 0 ? 0.0f : this.intervals[var2][var3][1] - this.intervals[var2][var3][0];
    }

    private int label(float var1, float var2) {
        int var3 = Math.max(0, Math.min(159, (int)(var2 / this.step)));
        for (int var4 = 0; var4 < 160; ++var4) {
            int[] var10000 = new int[]{var3 - var4, var3 + var4};
            for (int var8 : var10000) {
                if (var8 < 0 || var8 >= 160 || this.intervals[var8].length == 0) continue;
                float[][] var9 = this.intervals[var8];
                int var10 = 0;
                float var11 = Float.MAX_VALUE;
                for (int var12 = 0; var12 < var9.length; ++var12) {
                    float var13;
                    var13 = var1 < var9[var12][0] ? var9[var12][0] - var1 : (var1 > var9[var12][1] ? var1 - var9[var12][1] : 0.0f);
                    if (!(var13 < var11)) continue;
                    var11 = var13;
                    var10 = var12;
                }
                return this.labels[var8][var10];
            }
        }
        return 1;
    }

    private float share(int[] var1, int var2) {
        int var3 = 0;
        int[] var4 = var1;
        int var5 = var1.length;
        for (int var6 = 0; var6 < var5; ++var6) {
            int var7 = var4[var6];
            if (var7 != var2) continue;
            ++var3;
        }
        return (float)var3 / (float)Math.max(1, var1.length);
    }

    private float[] mean(int[] var1, int var2, float var3, float var4) {
        double var5 = 0.0;
        double var7 = 0.0;
        double var9 = 0.0;
        int var11 = 0;
        for (int var12 = 0; var12 < 2 && var11 == 0; ++var12) {
            for (int var13 = 0; var13 < var1.length; ++var13) {
                float var14 = this.mesh.pos[var13 * 3 + 1];
                if (var1[var13] != var2 || var12 == 0 && (var14 < var3 || var14 > var4)) continue;
                var5 += (double)this.mesh.pos[var13 * 3];
                var7 += (double)var14;
                var9 += (double)this.mesh.pos[var13 * 3 + 2];
                ++var11;
            }
        }
        float[] var10000 = var11 == 0 ? new float[3] : new float[]{(float)(var5 / (double)var11), (float)(var7 / (double)var11), (float)(var9 / (double)var11)};
        return var10000;
    }

    private float[] beyond(float[] var1, float[] var2, float var3) {
        float var4 = (float)Math.sqrt(var2[0] * var2[0] + var2[1] * var2[1] + var2[2] * var2[2]);
        float[] var5 = new float[this.mesh.vertices];
        if (var4 < 1.0E-5f) {
            return var5;
        }
        for (int var6 = 0; var6 < var5.length; ++var6) {
            float var7 = ((this.mesh.pos[var6 * 3] - var1[0]) * var2[0] + (this.mesh.pos[var6 * 3 + 1] - var1[1]) * var2[1] + (this.mesh.pos[var6 * 3 + 2] - var1[2]) * var2[2]) / var4;
            float var8 = Math.max(0.0f, Math.min(1.0f, (var7 + var3) / (2.0f * var3)));
            var5[var6] = var8 * var8 * (3.0f - 2.0f * var8);
        }
        return var5;
    }

    private float[] below(float var1, float var2) {
        float[] var3 = new float[this.mesh.vertices];
        for (int var4 = 0; var4 < var3.length; ++var4) {
            float var5 = Math.max(0.0f, Math.min(1.0f, (var1 + var2 - this.mesh.pos[var4 * 3 + 1]) / (2.0f * var2)));
            var3[var4] = var5 * var5 * (3.0f - 2.0f * var5);
        }
        return var3;
    }

    @Environment(value=EnvType.CLIENT)
    private record HeightSlice(int from, int to, float[] edge) {
    }
}

