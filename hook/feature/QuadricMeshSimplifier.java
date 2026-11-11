/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.GltfTransforms;
import hook.feature.PosedGltfMesh;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class QuadricMeshSimplifier {
    private final float[] pos;
    private final int[] tri;
    private final boolean[] dead;
    private final int[] corner;
    private final List<List<Integer>> around = new ArrayList<List<Integer>>();
    private final boolean[] locked;
    private final double[] quad;
    private final int[] version;
    private int live;

    QuadricMeshSimplifier(PosedGltfMesh var1, List<Integer> var2, boolean var3) {
        HashMap<Long, Integer> var4 = new HashMap<Long, Integer>();
        HashMap<CallSite, Integer> var5 = new HashMap<CallSite, Integer>();
        ArrayList<Integer> var6 = new ArrayList<Integer>();
        this.tri = new int[var2.size() * 3];
        for (int var7 = 0; var7 < var2.size(); ++var7) {
            int var8 = var2.get(var7);
            boolean var9 = var8 < 0;
            int var10 = var9 ? ~var8 : var8;
            for (int var11 = 0; var11 < 3; ++var11) {
                int var12 = var1.tris[var10 * 3 + (var9 ? 2 - var11 : var11)];
                int var13 = var9 ? ~var12 : var12;
                Integer var14 = (Integer)var4.get(var13);
                if (var14 == null && var3) {
                    String var15 = Float.floatToIntBits(var1.pos[var12 * 3]) + "," + Float.floatToIntBits(var1.pos[var12 * 3 + 1]) + "," + Float.floatToIntBits(var1.pos[var12 * 3 + 2]) + "," + Float.floatToIntBits(var1.uv[var12 * 2]) + "," + Float.floatToIntBits(var1.uv[var12 * 2 + 1]) + "," + var1.material[var12] + (var9 ? "b" : "");
                    var14 = (Integer)var5.get(var15);
                    if (var14 == null) {
                        var5.put((CallSite)((Object)var15), var6.size());
                    }
                    if (var14 != null) {
                        var4.put(Long.valueOf(var13), var14);
                    }
                }
                if (var14 == null) {
                    var14 = var6.size();
                    var4.put(Long.valueOf(var13), var14);
                    var6.add(var13);
                }
                this.tri[var7 * 3 + var11] = var14;
            }
        }
        int var28 = var6.size();
        this.corner = new int[var28];
        this.pos = new float[var28 * 3];
        for (int var29 = 0; var29 < var28; ++var29) {
            this.corner[var29] = (Integer)var6.get(var29);
            int var33 = this.corner[var29] < 0 ? ~this.corner[var29] : this.corner[var29];
            System.arraycopy(var1.pos, var33 * 3, this.pos, var29 * 3, 3);
        }
        this.dead = new boolean[var2.size()];
        this.live = var2.size();
        for (int var30 = 0; var30 < var28; ++var30) {
            this.around.add(new ArrayList());
        }
        for (int var31 = 0; var31 < var2.size(); ++var31) {
            int var34 = this.tri[var31 * 3];
            int var38 = this.tri[var31 * 3 + 1];
            int var42 = this.tri[var31 * 3 + 2];
            if (var34 != var38 && var38 != var42 && var34 != var42) {
                for (int var44 = 0; var44 < 3; ++var44) {
                    this.around.get(this.tri[var31 * 3 + var44]).add(var31);
                }
                continue;
            }
            this.dead[var31] = true;
            --this.live;
        }
        this.locked = new boolean[var28];
        HashMap<Long, Integer> var32 = new HashMap<Long, Integer>();
        for (int var35 = 0; var35 < var2.size(); ++var35) {
            if (this.dead[var35]) continue;
            for (int var39 = 0; var39 < 3; ++var39) {
                var32.merge(QuadricMeshSimplifier.key(this.tri[var35 * 3 + var39], this.tri[var35 * 3 + (var39 + 1) % 3]), 1, Integer::sum);
            }
        }
        for (Map.Entry var40 : var32.entrySet()) {
            if ((Integer)var40.getValue() == 2) continue;
            this.locked[(int)(((Long)var40.getKey()).longValue() >> 32)] = true;
            this.locked[(int)(((Long)var40.getKey()).longValue() & 0xFFFFFFFFL)] = true;
        }
        this.quad = new double[var28 * 10];
        for (int var37 = 0; var37 < var2.size(); ++var37) {
            int var45;
            int var43;
            int var41;
            float[] var46;
            double var47;
            if (this.dead[var37] || (var47 = Math.sqrt((var46 = GltfTransforms.faceNormal(this.pos, var41 = this.tri[var37 * 3], var43 = this.tri[var37 * 3 + 1], var45 = this.tri[var37 * 3 + 2]))[0] * var46[0] + var46[1] * var46[1] + var46[2] * var46[2])) < 1.0E-12) continue;
            double var16 = (double)var46[0] / var47;
            double var18 = (double)var46[1] / var47;
            double var20 = (double)var46[2] / var47;
            double var22 = -(var16 * (double)this.pos[var41 * 3] + var18 * (double)this.pos[var41 * 3 + 1] + var20 * (double)this.pos[var41 * 3 + 2]);
            int[] var10000 = new int[]{var41, var43, var45};
            for (int var27 : var10000) {
                this.addPlane(var27, var16, var18, var20, var22, var47 * 0.5);
            }
        }
        this.version = new int[var28];
    }

    private static long key(int var0, int var1) {
        return (long)Math.min(var0, var1) << 32 | (long)Math.max(var0, var1);
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

    private double error(int var1, double var2, double var4, double var6) {
        double[] var8 = this.quad;
        int var9 = var1 * 10;
        return var8[var9] * var2 * var2 + 2.0 * var8[var9 + 1] * var2 * var4 + 2.0 * var8[var9 + 2] * var2 * var6 + 2.0 * var8[var9 + 3] * var2 + var8[var9 + 4] * var4 * var4 + 2.0 * var8[var9 + 5] * var4 * var6 + 2.0 * var8[var9 + 6] * var4 + var8[var9 + 7] * var6 * var6 + 2.0 * var8[var9 + 8] * var6 + var8[var9 + 9];
    }

    int[][] reduce(int[] var1) {
        PriorityQueue<CollapseCandidate> var2 = new PriorityQueue<CollapseCandidate>((var0, var1x) -> Double.compare(var0.cost(), var1x.cost()));
        for (int var3 = 0; var3 < this.locked.length; ++var3) {
            this.push(var2, var3);
        }
        int[][] var14 = new int[var1.length][];
        Integer[] var4 = new Integer[var1.length];
        for (int var5 = 0; var5 < var4.length; ++var5) {
            var4[var5] = var5;
        }
        Arrays.sort(var4, (var1x, var2x) -> Integer.compare(var1[var2x], var1[var1x]));
        Integer[] var15 = var4;
        int var6 = var4.length;
        for (int var7 = 0; var7 < var6; ++var7) {
            int var8 = var15[var7];
            while (this.live > var1[var8] && !var2.isEmpty()) {
                CollapseCandidate var9 = var2.poll();
                if (this.version[var9.from()] != var9.vf() || this.version[var9.to()] != var9.vt() || this.locked[var9.from()] || !this.collapse(var9.from(), var9.to())) continue;
                HashSet<Integer> var10 = new HashSet<Integer>();
                var10.add(var9.to());
                for (int var12 : this.around.get(var9.to())) {
                    if (this.dead[var12]) continue;
                    for (int var13 = 0; var13 < 3; ++var13) {
                        var10.add(this.tri[var12 * 3 + var13]);
                    }
                }
                for (Integer var17Object : var10) {
                    int var17 = var17Object;
                    this.push(var2, var17);
                }
            }
            var14[var8] = this.snapshot();
        }
        return var14;
    }

    private void push(PriorityQueue<CollapseCandidate> var1, int var2) {
        if (!this.locked[var2]) {
            for (int var4 : this.neighbours(var2)) {
                double var5 = this.pos[var4 * 3] - this.pos[var2 * 3];
                double var7 = this.pos[var4 * 3 + 1] - this.pos[var2 * 3 + 1];
                double var9 = this.pos[var4 * 3 + 2] - this.pos[var2 * 3 + 2];
                double var11 = this.error(var2, this.pos[var4 * 3], this.pos[var4 * 3 + 1], this.pos[var4 * 3 + 2]) + 1.0E-4 * (var5 * var5 + var7 * var7 + var9 * var9);
                var1.add(new CollapseCandidate(var11, var2, var4, this.version[var2], this.version[var4]));
            }
        }
    }

    private List<Integer> neighbours(int var1) {
        ArrayList<Integer> var2 = new ArrayList<Integer>();
        for (int var4 : this.around.get(var1)) {
            if (this.dead[var4]) continue;
            for (int var5 = 0; var5 < 3; ++var5) {
                int var6 = this.tri[var4 * 3 + var5];
                if (var6 == var1 || var2.contains(var6)) continue;
                var2.add(var6);
            }
        }
        return var2;
    }

    private boolean collapse(int var1, int var2) {
        List<Integer> var3 = this.neighbours(var1);
        List<Integer> var4 = this.neighbours(var2);
        if (!var3.contains(var2)) {
            return false;
        }
        int var5 = 0;
        int var6 = 0;
        for (int var8 : this.around.get(var1)) {
            if (this.dead[var8] || !this.has(var8, var2)) continue;
            ++var5;
        }
        for (Integer var20Object : var3) {
            int var20 = var20Object;
            if (!var4.contains(var20)) continue;
            ++var6;
        }
        if (var6 != var5) {
            return false;
        }
        for (int var21 : this.around.get(var1)) {
            if (this.dead[var21] || this.has(var21, var2)) continue;
            float[] var9 = GltfTransforms.faceNormal(this.pos, this.tri[var21 * 3], this.tri[var21 * 3 + 1], this.tri[var21 * 3 + 2]);
            int[] var10000 = new int[]{this.tri[var21 * 3], this.tri[var21 * 3 + 1], this.tri[var21 * 3 + 2]};
            int[] var10 = var10000;
            for (int var11 = 0; var11 < 3; ++var11) {
                if (var10[var11] != var1) continue;
                var10[var11] = var2;
            }
            float[] var24 = GltfTransforms.faceNormal(this.pos, var10[0], var10[1], var10[2]);
            double var12 = Math.sqrt(var9[0] * var9[0] + var9[1] * var9[1] + var9[2] * var9[2]);
            double var14 = Math.sqrt(var24[0] * var24[0] + var24[1] * var24[1] + var24[2] * var24[2]);
            if (var14 < 1.0E-12 || var12 < 1.0E-12) {
                return false;
            }
            if (!((double)(var9[0] * var24[0] + var9[1] * var24[1] + var9[2] * var24[2]) / (var14 * var12) < 0.5)) continue;
            return false;
        }
        for (int var22 : this.around.get(var1)) {
            if (this.dead[var22]) continue;
            if (this.has(var22, var2)) {
                this.dead[var22] = true;
                --this.live;
                continue;
            }
            for (int var23 = 0; var23 < 3; ++var23) {
                if (this.tri[var22 * 3 + var23] != var1) continue;
                this.tri[var22 * 3 + var23] = var2;
            }
            this.around.get(var2).add(var22);
        }
        this.around.get(var1).clear();
        for (int var19 = 0; var19 < 10; ++var19) {
            this.quad[var2 * 10 + var19] = this.quad[var2 * 10 + var19] + this.quad[var1 * 10 + var19];
        }
        this.version[var1] = this.version[var1] + 1;
        this.version[var2] = this.version[var2] + 1;
        this.locked[var1] = true;
        return true;
    }

    private boolean has(int var1, int var2) {
        return this.tri[var1 * 3] == var2 || this.tri[var1 * 3 + 1] == var2 || this.tri[var1 * 3 + 2] == var2;
    }

    private int[] snapshot() {
        int[] var1 = new int[this.live * 3];
        int var2 = 0;
        for (int var3 = 0; var3 < this.dead.length; ++var3) {
            if (this.dead[var3]) continue;
            for (int var4 = 0; var4 < 3; ++var4) {
                var1[var2++] = this.corner[this.tri[var3 * 3 + var4]];
            }
        }
        return Arrays.copyOf(var1, var2);
    }

    @Environment(value=EnvType.CLIENT)
    private record CollapseCandidate(double cost, int from, int to, int vf, int vt) {
    }
}

