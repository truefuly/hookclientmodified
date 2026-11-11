/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.MeshData;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class MeshSimplifier {
    private static final double FLIP_LIMIT = 0.25;
    private static final int DEBRIS = 10;
    private final int nv;
    private final double[] pos;
    private final int[] tri;
    private final boolean[] triDead;
    private final int[][] around;
    private final int[] aroundCount;
    private final double[] quad;
    private final int[] version;
    private final boolean[] vertDead;
    private final int[] mark;
    private final int[] mark2;
    private int stamp;
    private int live;
    private boolean relaxed;
    private final double lengthWeight;
    private float[] heapCost = new float[1024];
    private int[] heapA = new int[1024];
    private int[] heapB = new int[1024];
    private int[] heapVa = new int[1024];
    private int[] heapVb = new int[1024];
    private int heapSize;
    private final double[] best = new double[3];

    MeshSimplifier(MeshData var1) {
        this.nv = var1.vertexCount;
        int var2 = var1.triCount;
        this.pos = new double[this.nv * 3];
        for (int var3 = 0; var3 < this.nv * 3; ++var3) {
            this.pos[var3] = var1.pos[var3];
        }
        this.tri = Arrays.copyOf(var1.tris, var2 * 3);
        this.triDead = new boolean[var2];
        this.aroundCount = new int[this.nv];
        for (int var33 = 0; var33 < var2 * 3; ++var33) {
            this.aroundCount[this.tri[var33]] = this.aroundCount[this.tri[var33]] + 1;
        }
        this.around = new int[this.nv][];
        for (int var34 = 0; var34 < this.nv; ++var34) {
            this.around[var34] = new int[Math.max(4, this.aroundCount[var34] + 2)];
        }
        Arrays.fill(this.aroundCount, 0);
        for (int var35 = 0; var35 < var2; ++var35) {
            for (int var4 = 0; var4 < 3; ++var4) {
                int var5 = this.tri[var35 * 3 + var4];
                int[] var10000 = this.around[var5];
                this.aroundCount[var5] = this.aroundCount[var5] + 1;
                var10000[this.aroundCount[var5]] = var35;
            }
        }
        this.quad = new double[this.nv * 10];
        this.version = new int[this.nv];
        this.vertDead = new boolean[this.nv];
        this.mark = new int[this.nv];
        this.mark2 = new int[this.nv];
        this.live = var2;
        double var36 = 0.0;
        for (int var37 = 0; var37 < var2; ++var37) {
            int var7 = this.tri[var37 * 3 + 1];
            int var6 = this.tri[var37 * 3];
            double var11 = this.pos[var7 * 3 + 1] - this.pos[var6 * 3 + 1];
            int var8 = this.tri[var37 * 3 + 2];
            double var19 = this.pos[var8 * 3 + 2] - this.pos[var6 * 3 + 2];
            double var13 = this.pos[var7 * 3 + 2] - this.pos[var6 * 3 + 2];
            double var17 = this.pos[var8 * 3 + 1] - this.pos[var6 * 3 + 1];
            double var21 = var11 * var19 - var13 * var17;
            double var15 = this.pos[var8 * 3] - this.pos[var6 * 3];
            double var9 = this.pos[var7 * 3] - this.pos[var6 * 3];
            double var23 = var13 * var15 - var9 * var19;
            double var25 = var9 * var17 - var11 * var15;
            double var27 = Math.sqrt(var21 * var21 + var23 * var23 + var25 * var25);
            if (var27 < 1.0E-12) continue;
            double var29 = var27 * 0.5;
            var36 += var29;
            double var31 = -((var21 /= var27) * this.pos[var6 * 3] + (var23 /= var27) * this.pos[var6 * 3 + 1] + (var25 /= var27) * this.pos[var6 * 3 + 2]);
            this.addPlane(var6, var21, var23, var25, var31, var29);
            this.addPlane(var7, var21, var23, var25, var31, var29);
            this.addPlane(var8, var21, var23, var25, var31, var29);
        }
        this.lengthWeight = var2 > 0 ? 0.04 * var36 / (double)var2 : 0.0;
        this.dropDebris();
        this.pushAll();
    }

    private void pushAll() {
        this.heapSize = 0;
        for (int var1 = 0; var1 < this.triDead.length; ++var1) {
            if (this.triDead[var1]) continue;
            for (int var2 = 0; var2 < 3; ++var2) {
                int var3 = this.tri[var1 * 3 + var2];
                int var4 = this.tri[var1 * 3 + (var2 + 1) % 3];
                if (var3 >= var4 && this.hasEdgeTriangleReverse(var1, var3, var4)) continue;
                this.push(var3, var4);
            }
        }
    }

    private void dropDebris() {
        int var1 = this.triDead.length;
        int[] var2 = new int[this.nv];
        int var3 = 0;
        while (var3 < this.nv) {
            var2[var3] = var3++;
        }
        for (int var4 = 0; var4 < var1; ++var4) {
            int var5 = MeshSimplifier.find(var2, this.tri[var4 * 3]);
            for (int var6 = 1; var6 < 3; ++var6) {
                int var7 = MeshSimplifier.find(var2, this.tri[var4 * 3 + var6]);
                if (var5 == var7) continue;
                var2[var7] = var5;
            }
        }
        int[] var8 = new int[this.nv];
        for (int var9 = 0; var9 < var1; ++var9) {
            int n = MeshSimplifier.find(var2, this.tri[var9 * 3]);
            var8[n] = var8[n] + 1;
        }
        for (int var10 = 0; var10 < var1; ++var10) {
            if (var8[MeshSimplifier.find(var2, this.tri[var10 * 3])] >= 10) continue;
            this.triDead[var10] = true;
            --this.live;
        }
    }

    private static int find(int[] var0, int var1) {
        while (var0[var1] != var1) {
            var0[var1] = var0[var0[var1]];
            var1 = var0[var1];
        }
        return var1;
    }

    private boolean hasEdgeTriangleReverse(int var1, int var2, int var3) {
        int[] var4 = this.around[var2];
        for (int var5 = 0; var5 < this.aroundCount[var2]; ++var5) {
            int var6 = var4[var5];
            if (var6 == var1) continue;
            for (int var7 = 0; var7 < 3; ++var7) {
                if (this.tri[var6 * 3 + var7] != var3 || this.tri[var6 * 3 + (var7 + 1) % 3] != var2) continue;
                return true;
            }
        }
        return false;
    }

    private void addPlane(int var1, double var2, double var4, double var6, double var8, double var10) {
        int var12 = var1 * 10;
        this.quad[var12] = this.quad[var12] + var10 * var2 * var2;
        this.quad[var12 + 1] = this.quad[var12 + 1] + var10 * var2 * var4;
        this.quad[var12 + 2] = this.quad[var12 + 2] + var10 * var2 * var6;
        this.quad[var12 + 3] = this.quad[var12 + 3] + var10 * var2 * var8;
        this.quad[var12 + 4] = this.quad[var12 + 4] + var10 * var4 * var4;
        this.quad[var12 + 5] = this.quad[var12 + 5] + var10 * var4 * var6;
        this.quad[var12 + 6] = this.quad[var12 + 6] + var10 * var4 * var8;
        this.quad[var12 + 7] = this.quad[var12 + 7] + var10 * var6 * var6;
        this.quad[var12 + 8] = this.quad[var12 + 8] + var10 * var6 * var8;
        this.quad[var12 + 9] = this.quad[var12 + 9] + var10 * var8 * var8;
    }

    int liveTriangles() {
        return this.live;
    }

    void reduce(int var1) {
        this.drain(var1);
        if (this.live > var1 && !this.relaxed) {
            this.relaxed = true;
            this.pushAll();
            this.drain(var1);
        }
    }

    private void drain(int var1) {
        while (this.live > var1 && this.heapSize > 0) {
            int var2 = this.heapA[0];
            int var3 = this.heapB[0];
            int var4 = this.heapVa[0];
            int var5 = this.heapVb[0];
            this.pop();
            if (this.vertDead[var2] || this.vertDead[var3] || this.version[var2] != var4 || this.version[var3] != var5) continue;
            this.cost(var2, var3);
            double var6 = this.best[0];
            double var8 = this.best[1];
            double var10 = this.best[2];
            if (!this.valid(var2, var3, var6, var8, var10)) continue;
            this.collapse(var2, var3, var6, var8, var10);
        }
    }

    MeshData snapshot() {
        int[] var1 = new int[this.nv];
        Arrays.fill(var1, -1);
        int var2 = 0;
        int[] var3 = new int[this.live * 3];
        int var4 = 0;
        for (int var5 = 0; var5 < this.triDead.length; ++var5) {
            if (this.triDead[var5]) continue;
            for (int var6 = 0; var6 < 3; ++var6) {
                int var7 = this.tri[var5 * 3 + var6];
                if (var1[var7] < 0) {
                    var1[var7] = var2++;
                }
                var3[var4 * 3 + var6] = var1[var7];
            }
            ++var4;
        }
        float[] var8 = new float[var2 * 3];
        for (int var9 = 0; var9 < this.nv; ++var9) {
            int var10 = var1[var9];
            if (var10 < 0) continue;
            var8[var10 * 3] = (float)this.pos[var9 * 3];
            var8[var10 * 3 + 1] = (float)this.pos[var9 * 3 + 1];
            var8[var10 * 3 + 2] = (float)this.pos[var9 * 3 + 2];
        }
        return new MeshData(var8, var2, var3, var4);
    }

    private float cost(int var1, int var2) {
        double var58;
        double var64;
        double var56;
        double var62;
        double var52;
        double var54;
        double var60;
        double[] var3 = this.quad;
        int var4 = var1 * 10;
        int var5 = var2 * 10;
        double var6 = var3[var4] + var3[var5];
        double var8 = var3[var4 + 1] + var3[var5 + 1];
        double var10 = var3[var4 + 2] + var3[var5 + 2];
        double var12 = var3[var4 + 3] + var3[var5 + 3];
        double var14 = var3[var4 + 4] + var3[var5 + 4];
        double var16 = var3[var4 + 5] + var3[var5 + 5];
        double var18 = var3[var4 + 6] + var3[var5 + 6];
        double var20 = var3[var4 + 7] + var3[var5 + 7];
        double var22 = var3[var4 + 8] + var3[var5 + 8];
        double var24 = var3[var4 + 9] + var3[var5 + 9];
        double var26 = this.pos[var1 * 3];
        double var28 = this.pos[var1 * 3 + 1];
        double var30 = this.pos[var1 * 3 + 2];
        double var32 = this.pos[var2 * 3];
        double var34 = this.pos[var2 * 3 + 1];
        double var36 = this.pos[var2 * 3 + 2];
        double var38 = var32 - var26;
        double var40 = var34 - var28;
        double var42 = var36 - var30;
        double var44 = var38 * var38 + var40 * var40 + var42 * var42;
        double var46 = var6 * (var14 * var20 - var16 * var16) - var8 * (var8 * var20 - var16 * var10) + var10 * (var8 * var16 - var14 * var10);
        double var48 = Math.abs(var6 * var14 * var20) + 1.0E-30;
        double var50 = Double.MAX_VALUE;
        if (Math.abs(var46) > 1.0E-9 * var48 && (var60 = (var26 + var32) * 0.5 - (var54 = -(var52 = 1.0 / var46) * (var12 * (var14 * var20 - var16 * var16) - var8 * (var18 * var20 - var16 * var22) + var10 * (var18 * var16 - var14 * var22)))) * var60 + (var62 = (var28 + var34) * 0.5 - (var56 = -var52 * (var6 * (var18 * var20 - var22 * var16) - var12 * (var8 * var20 - var16 * var10) + var10 * (var8 * var22 - var18 * var10)))) * var62 + (var64 = (var30 + var36) * 0.5 - (var58 = -var52 * (var6 * (var14 * var22 - var16 * var18) - var8 * (var8 * var22 - var18 * var10) + var12 * (var8 * var16 - var14 * var10)))) * var64 <= var44) {
            var50 = MeshSimplifier.error(var6, var8, var10, var12, var14, var16, var18, var20, var22, var24, var54, var56, var58);
            this.best[0] = var54;
            this.best[1] = var56;
            this.best[2] = var58;
        }
        if (var50 == Double.MAX_VALUE) {
            for (int var66 = 0; var66 < 3; ++var66) {
                double var53 = (double)var66 * 0.5;
                double var55 = var26 + var38 * var53;
                double var57 = var28 + var40 * var53;
                double var59 = var30 + var42 * var53;
                double var61 = MeshSimplifier.error(var6, var8, var10, var12, var14, var16, var18, var20, var22, var24, var55, var57, var59);
                if (!(var61 < var50)) continue;
                var50 = var61;
                this.best[0] = var55;
                this.best[1] = var57;
                this.best[2] = var59;
            }
        }
        return (float)(Math.max(0.0, var50) + this.lengthWeight * var44);
    }

    private static double error(double var0, double var2, double var4, double var6, double var8, double var10, double var12, double var14, double var16, double var18, double var20, double var22, double var24) {
        return var0 * var20 * var20 + 2.0 * var2 * var20 * var22 + 2.0 * var4 * var20 * var24 + 2.0 * var6 * var20 + var8 * var22 * var22 + 2.0 * var10 * var22 * var24 + 2.0 * var12 * var22 + var14 * var24 * var24 + 2.0 * var16 * var24 + var18;
    }

    private boolean valid(int var1, int var2, double var3, double var5, double var7) {
        int var9 = ++this.stamp;
        int var10 = 0;
        int[] var11 = this.around[var1];
        for (int var12 = 0; var12 < this.aroundCount[var1]; ++var12) {
            int var13 = var11[var12];
            if (this.triDead[var13]) continue;
            boolean var14 = false;
            for (int var15 = 0; var15 < 3; ++var15) {
                int var16 = this.tri[var13 * 3 + var15];
                if (var16 == var2) {
                    var14 = true;
                }
                if (var16 == var1) continue;
                this.mark[var16] = var9;
            }
            if (!var14) continue;
            ++var10;
        }
        if (var10 != 0 && var10 <= 2) {
            int var18 = 0;
            int[] var19 = this.around[var2];
            for (int var20 = 0; var20 < this.aroundCount[var2]; ++var20) {
                int var21 = var19[var20];
                if (this.triDead[var21]) continue;
                for (int var22 = 0; var22 < 3; ++var22) {
                    int var17 = this.tri[var21 * 3 + var22];
                    if (var17 == var2 || var17 == var1 || this.mark[var17] != var9 || this.mark2[var17] == var9) continue;
                    this.mark2[var17] = var9;
                    ++var18;
                }
            }
            return var18 != var10 ? false : this.keepsOrientation(var1, var2, var3, var5, var7) && this.keepsOrientation(var2, var1, var3, var5, var7);
        }
        return false;
    }

    private boolean keepsOrientation(int var1, int var2, double var3, double var5, double var7) {
        int[] var9 = this.around[var1];
        for (int var10 = 0; var10 < this.aroundCount[var1]; ++var10) {
            double var67;
            double var65;
            double var63;
            double var69;
            double var61;
            int var11 = var9[var10];
            if (this.triDead[var11]) continue;
            int var12 = this.tri[var11 * 3];
            int var13 = this.tri[var11 * 3 + 1];
            int var14 = this.tri[var11 * 3 + 2];
            if (var12 == var2 || var13 == var2 || var14 == var2) continue;
            double var15 = this.pos[var12 * 3];
            double var17 = this.pos[var12 * 3 + 1];
            double var19 = this.pos[var12 * 3 + 2];
            double var21 = this.pos[var13 * 3];
            double var23 = this.pos[var13 * 3 + 1];
            double var25 = this.pos[var13 * 3 + 2];
            double var27 = this.pos[var14 * 3];
            double var29 = this.pos[var14 * 3 + 1];
            double var31 = this.pos[var14 * 3 + 2];
            double var33 = (var23 - var17) * (var31 - var19) - (var25 - var19) * (var29 - var17);
            double var35 = (var25 - var19) * (var27 - var15) - (var21 - var15) * (var31 - var19);
            double var37 = (var21 - var15) * (var29 - var17) - (var23 - var17) * (var27 - var15);
            if (var12 == var1) {
                var15 = var3;
                var17 = var5;
                var19 = var7;
            } else if (var13 == var1) {
                var21 = var3;
                var23 = var5;
                var25 = var7;
            } else {
                var27 = var3;
                var29 = var5;
                var31 = var7;
            }
            double var39 = var21 - var15;
            double var41 = var23 - var17;
            double var43 = var25 - var19;
            double var45 = var27 - var15;
            double var47 = var29 - var17;
            double var49 = var31 - var19;
            double var51 = var41 * var49 - var43 * var47;
            double var53 = var43 * var45 - var39 * var49;
            double var55 = var39 * var47 - var41 * var45;
            double var57 = Math.sqrt(var33 * var33 + var35 * var35 + var37 * var37);
            double var59 = Math.sqrt(var51 * var51 + var53 * var53 + var55 * var55);
            if (var59 < 1.0E-14) {
                return false;
            }
            double d = var61 = this.relaxed ? 0.0 : 0.25;
            if (var57 > 1.0E-14 && var33 * var51 + var35 * var53 + var37 * var55 < var61 * var57 * var59) {
                return false;
            }
            if (this.relaxed || !(var59 * 3.4641016 < 0.06 * (var69 = var39 * var39 + var41 * var41 + var43 * var43 + var45 * var45 + var47 * var47 + var49 * var49 + (var63 = var27 - var21) * var63 + (var65 = var29 - var23) * var65 + (var67 = var31 - var25) * var67))) continue;
            return false;
        }
        return true;
    }

    private void collapse(int var1, int var2, double var3, double var5, double var7) {
        this.pos[var2 * 3] = var3;
        this.pos[var2 * 3 + 1] = var5;
        this.pos[var2 * 3 + 2] = var7;
        for (int var9 = 0; var9 < 10; ++var9) {
            this.quad[var2 * 10 + var9] = this.quad[var2 * 10 + var9] + this.quad[var1 * 10 + var9];
        }
        int[] var16 = this.around[var1];
        for (int var10 = 0; var10 < this.aroundCount[var1]; ++var10) {
            int var11 = var16[var10];
            if (this.triDead[var11]) continue;
            int var12 = var11 * 3;
            if (this.tri[var12] != var2 && this.tri[var12 + 1] != var2 && this.tri[var12 + 2] != var2) {
                for (int var13 = 0; var13 < 3; ++var13) {
                    if (this.tri[var12 + var13] != var1) continue;
                    this.tri[var12 + var13] = var2;
                }
                this.append(var2, var11);
                continue;
            }
            this.triDead[var11] = true;
            --this.live;
        }
        this.vertDead[var1] = true;
        this.aroundCount[var1] = 0;
        this.version[var1] = this.version[var1] + 1;
        this.version[var2] = this.version[var2] + 1;
        this.compact(var2);
        int var17 = ++this.stamp;
        int[] var18 = this.around[var2];
        for (int var19 = 0; var19 < this.aroundCount[var2]; ++var19) {
            int var20 = var18[var19];
            for (int var14 = 0; var14 < 3; ++var14) {
                int var15 = this.tri[var20 * 3 + var14];
                if (var15 == var2 || this.mark[var15] == var17) continue;
                this.mark[var15] = var17;
                this.push(var2, var15);
            }
        }
    }

    private void append(int var1, int var2) {
        int[] var3 = this.around[var1];
        if (this.aroundCount[var1] == var3.length) {
            var3 = Arrays.copyOf(var3, var3.length * 2);
            this.around[var1] = var3;
        }
        this.aroundCount[var1] = this.aroundCount[var1] + 1;
        var3[this.aroundCount[var1]] = var2;
    }

    private void compact(int var1) {
        int[] var2 = this.around[var1];
        int var3 = 0;
        for (int var4 = 0; var4 < this.aroundCount[var1]; ++var4) {
            if (this.triDead[var2[var4]]) continue;
            var2[var3++] = var2[var4];
        }
        this.aroundCount[var1] = var3;
    }

    private void push(int var1, int var2) {
        int var5;
        int var10000;
        float var3 = this.cost(var1, var2);
        if (this.heapSize == this.heapCost.length) {
            int var4 = this.heapSize * 2;
            this.heapCost = Arrays.copyOf(this.heapCost, var4);
            this.heapA = Arrays.copyOf(this.heapA, var4);
            this.heapB = Arrays.copyOf(this.heapB, var4);
            this.heapVa = Arrays.copyOf(this.heapVa, var4);
            this.heapVb = Arrays.copyOf(this.heapVb, var4);
        }
        int var6 = var10000 = this.heapSize++;
        while (var6 > 0 && !(this.heapCost[var5 = var6 - 1 >> 1] <= var3)) {
            this.move(var5, var6);
            var6 = var5;
        }
        this.heapCost[var6] = var3;
        this.heapA[var6] = var1;
        this.heapB[var6] = var2;
        this.heapVa[var6] = this.version[var1];
        this.heapVb[var6] = this.version[var2];
    }

    private void pop() {
        int var1;
        if ((var1 = --this.heapSize) != 0) {
            int var8;
            float var2 = this.heapCost[var1];
            int var3 = this.heapA[var1];
            int var4 = this.heapB[var1];
            int var5 = this.heapVa[var1];
            int var6 = this.heapVb[var1];
            int var7 = 0;
            while ((var8 = var7 * 2 + 1) < var1) {
                int var10;
                int var9 = var8 + 1;
                int n = var10 = var9 < var1 && this.heapCost[var9] < this.heapCost[var8] ? var9 : var8;
                if (this.heapCost[var10] >= var2) break;
                this.move(var10, var7);
                var7 = var10;
            }
            this.heapCost[var7] = var2;
            this.heapA[var7] = var3;
            this.heapB[var7] = var4;
            this.heapVa[var7] = var5;
            this.heapVb[var7] = var6;
        }
    }

    private void move(int var1, int var2) {
        this.heapCost[var2] = this.heapCost[var1];
        this.heapA[var2] = this.heapA[var1];
        this.heapB[var2] = this.heapB[var1];
        this.heapVa[var2] = this.heapVa[var1];
        this.heapVb[var2] = this.heapVb[var1];
    }
}

