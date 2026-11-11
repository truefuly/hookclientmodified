/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.MeshData;
import hook.feature.Sdf3D;
import java.util.Arrays;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class SdfMeshBaker {
    private static final int BLOCK = 4;
    private static final int MAX_POINTS = 6000000;
    private static final int[] EDGE_A = new int[]{0, 2, 4, 6, 0, 1, 4, 5, 0, 1, 2, 3};
    private static final int[] EDGE_B = new int[]{1, 3, 5, 7, 2, 3, 6, 7, 4, 5, 6, 7};
    private final Sdf3D shape;
    private final float ox;
    private final float oy;
    private final float oz;
    private final float h;
    private final int nx;
    private final int ny;
    private final int nz;
    private final int bx;
    private final int by;
    private final int bz;
    private final float[] field;
    private final boolean[] active;

    private SdfMeshBaker(Sdf3D var1, float var2, float var3, float var4, int var5, int var6, int var7, float var8) {
        this.shape = var1;
        this.ox = var2;
        this.oy = var3;
        this.oz = var4;
        this.nx = var5;
        this.ny = var6;
        this.nz = var7;
        this.h = var8;
        this.bx = (var5 + 4 - 1) / 4;
        this.by = (var6 + 4 - 1) / 4;
        this.bz = (var7 + 4 - 1) / 4;
        this.field = new float[var5 * var6 * var7];
        Arrays.fill(this.field, Float.intBitsToFloat(2143289344));
        this.active = new boolean[this.bx * this.by * this.bz];
    }

    static float fitStep(float var0, float var1, float var2, float var3) {
        float var8 = var3;
        long var31;
        long var26;
        long var12;
        long var14;
        long var45;
        while (((var45 = (var14 = (var12 = (long)(var0 / var8 + 4.0f)) * (var26 = (long)(var1 / var8 + 4.0f))) * (var31 = (long)(var2 / var8 + 4.0f)) - 6000000L) == 0L ? 0 : (var45 < 0L ? -1 : 1)) > 0) {
            var8 *= 1.12f;
        }
        return var8;
    }

    static MeshData mesh(Sdf3D var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
        float var24 = var7 * 3.0f;
        float var26 = var1 - var24;
        float var28 = var2 - var24;
        float var30 = var3 - var24;
        float var32 = var4 + var24;
        float var34 = var5 + var24;
        float var36 = var6 + var24;
        float var38 = var32 - var26;
        float var79 = var34 - var28;
        float var90 = var36 - var30;
        float var39 = SdfMeshBaker.fitStep(var38, var79, var90, var7);
        int var48 = (int)Math.ceil((var32 - var26) / var39) + 1;
        int var57 = (int)Math.ceil((var34 - var28) / var39) + 1;
        int var66 = (int)Math.ceil((var36 - var30) / var39) + 1;
        SdfMeshBaker var67 = new SdfMeshBaker(var0, var26, var28, var30, var48, var57, var66, var39);
        var67.evaluate();
        return var67.extract();
    }

    private void evaluate() {
        float var1 = 4.0f * this.h * 0.8660254f * 1.6f + this.h * 1.5f;
        float var2 = this.h * 2.5f;
        IntStream.range(0, this.bz).parallel().forEach(var3 -> {
            for (int var4 = 0; var4 < this.by; ++var4) {
                for (int var5 = 0; var5 < this.bx; ++var5) {
                    float var6 = this.ox + ((float)(var5 * 4) + 2.0f) * this.h;
                    float var7 = this.oy + ((float)(var4 * 4) + 2.0f) * this.h;
                    float var8 = this.oz + ((float)(var3 * 4) + 2.0f) * this.h;
                    float var9 = this.shape.distance(var6, var7, var8);
                    if (var9 < -var1 || var9 > var1) continue;
                    this.active[(var3 * this.by + var4) * this.bx + var5] = true;
                    Sdf3D var10 = this.shape.prune(this.ox + (float)(var5 * 4) * this.h - var2, this.oy + (float)(var4 * 4) * this.h - var2, this.oz + (float)(var3 * 4) * this.h - var2, this.ox + (float)((var5 + 1) * 4) * this.h + var2, this.oy + (float)((var4 + 1) * 4) * this.h + var2, this.oz + (float)((var3 + 1) * 4) * this.h + var2);
                    this.fill(var10, var5 * 4, var4 * 4, var3 * 4);
                }
            }
        });
    }

    private void fill(Sdf3D var1, int var2, int var3, int var4) {
        int var5 = Math.min(var2 + 4, this.nx);
        int var6 = Math.min(var3 + 4, this.ny);
        int var7 = Math.min(var4 + 4, this.nz);
        for (int var8 = var4; var8 < var7; ++var8) {
            for (int var9 = var3; var9 < var6; ++var9) {
                for (int var10 = var2; var10 < var5; ++var10) {
                    float var11 = var1.distance(this.ox + (float)var10 * this.h, this.oy + (float)var9 * this.h, this.oz + (float)var8 * this.h);
                    this.field[(var8 * this.ny + var9) * this.nx + var10] = var11 != var11 ? 1.0f : var11;
                }
            }
        }
    }

    private float value(int var1, int var2, int var3) {
        int var4 = (var3 * this.ny + var2) * this.nx + var1;
        float var5 = this.field[var4];
        if (var5 != var5) {
            var5 = this.shape.distance(this.ox + (float)var1 * this.h, this.oy + (float)var2 * this.h, this.oz + (float)var3 * this.h);
            if (var5 != var5) {
                var5 = 1.0f;
            }
            this.field[var4] = var5;
        }
        return var5;
    }

    private MeshData extract() {
        MeshAccumulator var1 = new MeshAccumulator();
        for (int var2 = 0; var2 < this.bz; ++var2) {
            for (int var3 = 0; var3 < this.by; ++var3) {
                for (int var4 = 0; var4 < this.bx; ++var4) {
                    if (!this.active[(var2 * this.by + var3) * this.bx + var4]) continue;
                    var1.block(var4 * 4, var3 * 4, var2 * 4);
                }
            }
        }
        return new MeshData(Arrays.copyOf(var1.pos, var1.verts * 3), var1.verts, Arrays.copyOf(var1.tris, var1.triCount * 3), var1.triCount);
    }

    @Environment(value=EnvType.CLIENT)
    private final class MeshAccumulator {
        private final int[] cells;
        private final float[] corner = new float[8];
        private float[] pos = new float[12288];
        private int verts;
        private int[] tris = new int[24576];
        private int triCount;

        MeshAccumulator() {
            this.cells = new int[SdfMeshBaker.this.nx * SdfMeshBaker.this.ny * SdfMeshBaker.this.nz];
            Arrays.fill(this.cells, -1);
        }

        void block(int startX, int startY, int startZ) {
            int endX = Math.min(startX + 4, SdfMeshBaker.this.nx);
            int endY = Math.min(startY + 4, SdfMeshBaker.this.ny);
            int endZ = Math.min(startZ + 4, SdfMeshBaker.this.nz);
            for (int z = startZ; z < endZ; ++z) {
                for (int y = startY; y < endY; ++y) {
                    for (int x = startX; x < endX; ++x) {
                        int d;
                        int c;
                        int b;
                        int a;
                        boolean nextInside;
                        boolean inside;
                        boolean bl = inside = SdfMeshBaker.this.value(x, y, z) < 0.0f;
                        if (x + 1 < SdfMeshBaker.this.nx && y >= 1 && z >= 1 && y < SdfMeshBaker.this.ny - 1 && z < SdfMeshBaker.this.nz - 1) {
                            boolean bl2 = nextInside = SdfMeshBaker.this.value(x + 1, y, z) < 0.0f;
                            if (nextInside != inside) {
                                a = this.cell(x, y - 1, z - 1);
                                b = this.cell(x, y, z - 1);
                                c = this.cell(x, y, z);
                                d = this.cell(x, y - 1, z);
                                this.quad(a, b, c, d, inside);
                            }
                        }
                        if (y + 1 < SdfMeshBaker.this.ny && x >= 1 && z >= 1 && x < SdfMeshBaker.this.nx - 1 && z < SdfMeshBaker.this.nz - 1) {
                            boolean bl3 = nextInside = SdfMeshBaker.this.value(x, y + 1, z) < 0.0f;
                            if (nextInside != inside) {
                                a = this.cell(x - 1, y, z - 1);
                                b = this.cell(x - 1, y, z);
                                c = this.cell(x, y, z);
                                d = this.cell(x, y, z - 1);
                                this.quad(a, b, c, d, inside);
                            }
                        }
                        if (z + 1 >= SdfMeshBaker.this.nz || x < 1 || y < 1 || x >= SdfMeshBaker.this.nx - 1 || y >= SdfMeshBaker.this.ny - 1) continue;
                        boolean bl4 = nextInside = SdfMeshBaker.this.value(x, y, z + 1) < 0.0f;
                        if (nextInside == inside) continue;
                        a = this.cell(x - 1, y - 1, z);
                        b = this.cell(x, y - 1, z);
                        c = this.cell(x, y, z);
                        d = this.cell(x - 1, y, z);
                        this.quad(a, b, c, d, inside);
                    }
                }
            }
        }

        private int cell(int var1, int var2, int var3) {
            float var26;
            int var4 = (var3 * SdfMeshBaker.this.ny + var2) * SdfMeshBaker.this.nx + var1;
            int var5 = this.cells[var4];
            if (var5 >= 0) {
                return var5;
            }
            float[] var6 = this.corner;
            for (int var7 = 0; var7 < 8; ++var7) {
                var6[var7] = SdfMeshBaker.this.value(var1 + (var7 & 1), var2 + (var7 >> 1 & 1), var3 + (var7 >> 2 & 1));
            }
            float var23 = 0.0f;
            float var8 = 0.0f;
            float var9 = 0.0f;
            int var10 = 0;
            for (int var11 = 0; var11 < 12; ++var11) {
                int var13;
                float var15;
                int var12 = EDGE_A[var11];
                float var14 = var6[var12];
                if (var14 < 0.0f == (var15 = var6[var13 = EDGE_B[var11]]) < 0.0f) continue;
                float var16 = var14 / (var14 - var15);
                float var17 = var12 & 1;
                float var18 = var12 >> 1 & 1;
                float var19 = var12 >> 2 & 1;
                float var20 = var13 & 1;
                float var21 = var13 >> 1 & 1;
                float var22 = var13 >> 2 & 1;
                var23 += var17 + (var20 - var17) * var16;
                var8 += var18 + (var21 - var18) * var16;
                var9 += var19 + (var22 - var19) * var16;
                ++var10;
            }
            float var24 = var10 > 0 ? var23 / (float)var10 : 0.5f;
            float var25 = var10 > 0 ? var8 / (float)var10 : 0.5f;
            float f = var26 = var10 > 0 ? var9 / (float)var10 : 0.5f;
            if (this.verts * 3 + 3 > this.pos.length) {
                this.pos = Arrays.copyOf(this.pos, this.pos.length * 2);
            }
            this.pos[this.verts * 3] = SdfMeshBaker.this.ox + ((float)var1 + var24) * SdfMeshBaker.this.h;
            this.pos[this.verts * 3 + 1] = SdfMeshBaker.this.oy + ((float)var2 + var25) * SdfMeshBaker.this.h;
            this.pos[this.verts * 3 + 2] = SdfMeshBaker.this.oz + ((float)var3 + var26) * SdfMeshBaker.this.h;
            this.cells[var4] = this.verts;
            int var10000 = this.verts++;
            return var10000;
        }

        private void quad(int var1, int var2, int var3, int var4, boolean var5) {
            float var7;
            float var8;
            if (!var5) {
                int var6 = var2;
                var2 = var4;
                var4 = var6;
            }
            if ((var8 = this.dist2(var1, var3)) <= (var7 = this.dist2(var2, var4))) {
                this.tri(var1, var2, var3);
                this.tri(var1, var3, var4);
            } else {
                this.tri(var1, var2, var4);
                this.tri(var2, var3, var4);
            }
        }

        private float dist2(int var1, int var2) {
            float var3 = this.pos[var1 * 3] - this.pos[var2 * 3];
            float var4 = this.pos[var1 * 3 + 1] - this.pos[var2 * 3 + 1];
            float var5 = this.pos[var1 * 3 + 2] - this.pos[var2 * 3 + 2];
            return var3 * var3 + var4 * var4 + var5 * var5;
        }

        private void tri(int var1, int var2, int var3) {
            if (var1 != var2 && var2 != var3 && var1 != var3) {
                if (this.triCount * 3 + 3 > this.tris.length) {
                    this.tris = Arrays.copyOf(this.tris, this.tris.length * 2);
                }
                this.tris[this.triCount * 3] = var1;
                this.tris[this.triCount * 3 + 1] = var2;
                this.tris[this.triCount * 3 + 2] = var3;
                ++this.triCount;
            }
        }
    }
}

