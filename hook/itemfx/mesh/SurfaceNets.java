/*
 * Decompiled with CFR 0.152.
 */
package hook.itemfx.mesh;

import hook.itemfx.mesh.MeshData;
import hook.itemfx.mesh.Sd;
import java.util.Arrays;

public final class SurfaceNets {
    private static final int[] EDGE_A = new int[]{0, 2, 4, 6, 0, 1, 4, 5, 0, 1, 2, 3};
    private static final int[] EDGE_B = new int[]{1, 3, 5, 7, 2, 3, 6, 7, 4, 5, 6, 7};

    private SurfaceNets() {
    }

    public static MeshData build(Field var0, Painter var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
        int var10 = (int)Math.ceil((var5 - var2) / var8) + 1;
        int var11 = (int)Math.ceil((var6 - var3) / var8) + 1;
        int var12 = (int)Math.ceil((var7 - var4) / var8) + 1;
        float[] var13 = new float[var10 * var11 * var12];
        for (int var14 = 0; var14 < var12; ++var14) {
            float var15 = var4 + (float)var14 * var8;
            for (int var16 = 0; var16 < var11; ++var16) {
                float var17 = var3 + (float)var16 * var8;
                int var18 = var10 * (var16 + var11 * var14);
                for (int var19 = 0; var19 < var10; ++var19) {
                    var13[var18 + var19] = var0.at(var2 + (float)var19 * var8, var17, var15);
                }
            }
        }
        int var45 = var10 - 1;
        int var46 = var11 - 1;
        int var47 = var12 - 1;
        int[] var48 = new int[var45 * var46 * var47];
        Arrays.fill(var48, -1);
        FloatList var49 = new FloatList(16384);
        float[] var50 = new float[8];
        for (int var20 = 0; var20 < var47; ++var20) {
            for (int var21 = 0; var21 < var46; ++var21) {
                for (int var22 = 0; var22 < var45; ++var22) {
                    int var23 = 0;
                    for (int var24 = 0; var24 < 8; ++var24) {
                        var50[var24] = var13[var22 + (var24 & 1) + var10 * (var21 + (var24 >> 1 & 1) + var11 * (var20 + (var24 >> 2 & 1)))];
                        if (!(var50[var24] < 0.0f)) continue;
                        var23 |= 1 << var24;
                    }
                    if (var23 == 0 || var23 == 255) continue;
                    float var64 = 0.0f;
                    float var25 = 0.0f;
                    float var26 = 0.0f;
                    int var27 = 0;
                    for (int var28 = 0; var28 < 12; ++var28) {
                        int var29 = EDGE_A[var28];
                        int var30 = EDGE_B[var28];
                        if ((var23 >> var29 & 1) == (var23 >> var30 & 1)) continue;
                        float var31 = var50[var29] / (var50[var29] - var50[var30]);
                        float var32 = var29 & 1;
                        float var33 = var29 >> 1 & 1;
                        float var34 = var29 >> 2 & 1;
                        float var35 = var30 & 1;
                        float var36 = var30 >> 1 & 1;
                        float var37 = var30 >> 2 & 1;
                        var64 += var32 + (var35 - var32) * var31;
                        var25 += var33 + (var36 - var33) * var31;
                        var26 += var34 + (var37 - var34) * var31;
                        ++var27;
                    }
                    float var80 = var2 + ((float)var22 + var64 / (float)var27) * var8;
                    float var83 = var3 + ((float)var21 + var25 / (float)var27) * var8;
                    float var86 = var4 + ((float)var20 + var26 / (float)var27) * var8;
                    float var88 = var2 + ((float)var22 - 0.5f) * var8;
                    float var91 = var3 + ((float)var21 - 0.5f) * var8;
                    float var94 = var4 + ((float)var20 - 0.5f) * var8;
                    float var97 = var88 + 2.0f * var8;
                    float var99 = var91 + 2.0f * var8;
                    float var101 = var94 + 2.0f * var8;
                    for (int var103 = 0; var103 < 3; ++var103) {
                        float var42;
                        float var41;
                        float var38 = var0.at(var80, var83, var86);
                        float var39 = var8 * 0.25f;
                        float var40 = var0.at(var80 + var39, var83, var86) - var0.at(var80 - var39, var83, var86);
                        float var43 = var40 * var40 + (var41 = var0.at(var80, var83 + var39, var86) - var0.at(var80, var83 - var39, var86)) * var41 + (var42 = var0.at(var80, var83, var86 + var39) - var0.at(var80, var83, var86 - var39)) * var42;
                        if (var43 < 1.0E-12f) break;
                        float var44 = var38 * 2.0f * var39 / var43;
                        var80 = Sd.clamp(var80 - var40 * var44, var88, var97);
                        var83 = Sd.clamp(var83 - var41 * var44, var91, var99);
                        var86 = Sd.clamp(var86 - var42 * var44, var94, var101);
                    }
                    var48[var22 + var45 * (var21 + var46 * var20)] = var49.size() / 3;
                    var49.add(var80);
                    var49.add(var83);
                    var49.add(var86);
                }
            }
        }
        IntList var51 = new IntList(32768);
        for (int var52 = 1; var52 < var12 - 1; ++var52) {
            for (int var56 = 1; var56 < var11 - 1; ++var56) {
                for (int var60 = 0; var60 < var10 - 1; ++var60) {
                    float var71;
                    float var65 = var13[var60 + var10 * (var56 + var11 * var52)];
                    if (var65 < 0.0f == (var71 = var13[var60 + 1 + var10 * (var56 + var11 * var52)]) < 0.0f) continue;
                    SurfaceNets.quad(var51, var65 < 0.0f, var48, var45, var46, var60, var56 - 1, var52 - 1, var60, var56, var52 - 1, var60, var56, var52, var60, var56 - 1, var52);
                }
            }
        }
        for (int var53 = 1; var53 < var12 - 1; ++var53) {
            for (int var57 = 0; var57 < var11 - 1; ++var57) {
                for (int var61 = 1; var61 < var10 - 1; ++var61) {
                    float var72;
                    float var66 = var13[var61 + var10 * (var57 + var11 * var53)];
                    if (var66 < 0.0f == (var72 = var13[var61 + var10 * (var57 + 1 + var11 * var53)]) < 0.0f) continue;
                    SurfaceNets.quad(var51, var66 < 0.0f, var48, var45, var46, var61 - 1, var57, var53 - 1, var61 - 1, var57, var53, var61, var57, var53, var61, var57, var53 - 1);
                }
            }
        }
        for (int var54 = 0; var54 < var12 - 1; ++var54) {
            for (int var58 = 1; var58 < var11 - 1; ++var58) {
                for (int var62 = 1; var62 < var10 - 1; ++var62) {
                    float var73;
                    float var67 = var13[var62 + var10 * (var58 + var11 * var54)];
                    if (var67 < 0.0f == (var73 = var13[var62 + var10 * (var58 + var11 * (var54 + 1))]) < 0.0f) continue;
                    SurfaceNets.quad(var51, var67 < 0.0f, var48, var45, var46, var62 - 1, var58 - 1, var54, var62, var58 - 1, var54, var62, var58, var54, var62 - 1, var58, var54);
                }
            }
        }
        int var55 = var49.size() / 3;
        int var59 = var51.size() / 4 * 2;
        MeshData var63 = new MeshData(var55, var59);
        System.arraycopy(var49.data, 0, var63.pos, 0, var55 * 3);
        int var68 = 0;
        for (int var74 = 0; var74 < var51.size(); var74 = (int)((byte)(var74 + 4))) {
            int var76 = var51.data[var74];
            int var78 = var51.data[var74 + 1];
            int var81 = var51.data[var74 + 2];
            int var84 = var51.data[var74 + 3];
            if (SurfaceNets.dist2(var63.pos, var76, var81) <= SurfaceNets.dist2(var63.pos, var78, var84)) {
                var68 = SurfaceNets.tri(var63.indices, var68, var76, var78, var81);
                var68 = SurfaceNets.tri(var63.indices, var68, var76, var81, var84);
                continue;
            }
            var68 = SurfaceNets.tri(var63.indices, var68, var76, var78, var84);
            var68 = SurfaceNets.tri(var63.indices, var68, var78, var81, var84);
        }
        Surface var75 = new Surface();
        float var77 = var8 * 0.35f;
        for (int var79 = 0; var79 < var55; ++var79) {
            float var95;
            float var92;
            float var82 = var63.pos[var79 * 3];
            float var85 = var63.pos[var79 * 3 + 1];
            float var87 = var63.pos[var79 * 3 + 2];
            float var89 = var0.at(var82 + var77, var85, var87) - var0.at(var82 - var77, var85, var87);
            float var98 = (float)Math.sqrt(var89 * var89 + (var92 = var0.at(var82, var85 + var77, var87) - var0.at(var82, var85 - var77, var87)) * var92 + (var95 = var0.at(var82, var85, var87 + var77) - var0.at(var82, var85, var87 - var77)) * var95);
            if (var98 < 1.0E-9f) {
                var89 = 0.0f;
                var92 = 1.0f;
                var95 = 0.0f;
            } else {
                var89 /= var98;
                var92 /= var98;
                var95 /= var98;
            }
            var63.nrm[var79 * 3] = var89;
            var63.nrm[var79 * 3 + 1] = var92;
            var63.nrm[var79 * 3 + 2] = var95;
            float var100 = 0.0f;
            float var102 = 0.0f;
            for (int var104 = 1; var104 <= 5; ++var104) {
                float var107 = var9 * (float)var104 / 5.0f;
                float var108 = var0.at(var82 + var89 * var107, var85 + var92 * var107, var87 + var95 * var107);
                float var109 = 1.0f / (float)var104;
                var100 += var109 * Sd.clamp((var107 - var108) / var107, 0.0f, 1.0f);
                var102 += var109;
            }
            float var105 = 1.0f - var100 / var102;
            var75.reset();
            var1.paint(var82, var85, var87, var89, var92, var95, var75);
            var105 = Sd.clamp(var105 * var75.ao, 0.0f, 1.0f);
            var63.rgba[var79] = (Math.round(var105 * 255.0f) & 0xFF) << 24 | var75.rgb & 0xFFFFFF;
            var63.mat[var79] = (short)var75.mat;
            var63.region[var79] = (short)var75.region;
            var63.p1[var79] = (short)Math.round(Sd.clamp(var75.p1, 0.0f, 1.0f) * 1000.0f);
            var63.p2[var79] = (short)Math.round(Sd.clamp(var75.p2, 0.0f, 1.0f) * 1000.0f);
            var63.uv[var79 * 2] = var75.u;
            var63.uv[var79 * 2 + 1] = var75.v;
        }
        return var63;
    }

    private static void quad(IntList var0, boolean var1, int[] var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15, int var16) {
        int var17 = var2[var5 + var3 * (var6 + var4 * var7)];
        int var18 = var2[var8 + var3 * (var9 + var4 * var10)];
        int var19 = var2[var11 + var3 * (var12 + var4 * var13)];
        int var20 = var2[var14 + var3 * (var15 + var4 * var16)];
        if (var17 >= 0 && var18 >= 0 && var19 >= 0 && var20 >= 0) {
            if (var1) {
                var0.add(var17);
                var0.add(var18);
                var0.add(var19);
                var0.add(var20);
            } else {
                var0.add(var20);
                var0.add(var19);
                var0.add(var18);
                var0.add(var17);
            }
        }
    }

    private static int tri(int[] var0, int var1, int var2, int var3, int var4) {
        var0[var1] = var2;
        var0[var1 + 1] = var3;
        var0[var1 + 2] = var4;
        return var1 + 3;
    }

    private static float dist2(float[] var0, int var1, int var2) {
        float var3 = var0[var1 * 3] - var0[var2 * 3];
        float var4 = var0[var1 * 3 + 1] - var0[var2 * 3 + 1];
        float var5 = var0[var1 * 3 + 2] - var0[var2 * 3 + 2];
        return var3 * var3 + var4 * var4 + var5 * var5;
    }

    public static interface Field {
        public float at(float var1, float var2, float var3);
    }

    static final class FloatList {
        float[] data;
        int size;

        FloatList(int var1) {
            this.data = new float[var1];
        }

        void add(float var1) {
            if (this.size == this.data.length) {
                this.data = Arrays.copyOf(this.data, this.size * 2);
            }
            this.data[this.size++] = var1;
        }

        int size() {
            return this.size;
        }
    }

    static final class IntList {
        int[] data;
        int size;

        IntList(int var1) {
            this.data = new int[var1];
        }

        void add(int var1) {
            if (this.size == this.data.length) {
                this.data = Arrays.copyOf(this.data, this.size * 2);
            }
            this.data[this.size++] = var1;
        }

        int size() {
            return this.size;
        }
    }

    public static final class Surface {
        public int rgb = 0xFFFFFF;
        public int mat;
        public int region;
        public float p1;
        public float p2;
        public float u;
        public float v;
        public float ao = 1.0f;

        void reset() {
            this.rgb = 0xFFFFFF;
            this.mat = 0;
            this.region = 0;
            this.p1 = 0.0f;
            this.p2 = 0.0f;
            this.u = 0.0f;
            this.v = 0.0f;
            this.ao = 1.0f;
        }
    }

    public static interface Painter {
        public void paint(float var1, float var2, float var3, float var4, float var5, float var6, Surface var7);
    }
}

