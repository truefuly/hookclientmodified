/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.Sdf3D;
import hook.feature.SdfShapes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class TaperedTubeSdf3D
extends Sdf3D {
    private static final int STRIDE = 20;
    private final int count;
    private final float[] seg;
    private float ratio = 1.0f;
    private int flatMode;
    private float fx;
    private float fy;
    private float fz;
    private float minHalf;

    private TaperedTubeSdf3D(float[] var1, int var2) {
        this.seg = var1;
        this.count = var2;
        float var3 = 100000.0f;
        float var4 = 100000.0f;
        float var5 = 100000.0f;
        float var6 = -100000.0f;
        float var7 = -100000.0f;
        float var8 = -100000.0f;
        for (int var9 = 0; var9 < var2; ++var9) {
            int var10 = var9 * 20;
            var3 = Math.min(var3, var1[var10 + 14]);
            var4 = Math.min(var4, var1[var10 + 15]);
            var5 = Math.min(var5, var1[var10 + 16]);
            var6 = Math.max(var6, var1[var10 + 17]);
            var7 = Math.max(var7, var1[var10 + 18]);
            var8 = Math.max(var8, var1[var10 + 19]);
        }
        this.bounds(var3, var4, var5, var6, var7, var8);
    }

    static TaperedTubeSdf3D tapered(float var0, float var1, float[] var2) {
        if (var2.length >= 6 && var2.length % 3 == 0) {
            int var3 = var2.length / 3;
            float[] var4 = new float[var3];
            for (int var5 = 1; var5 < var3; ++var5) {
                float var6 = var2[var5 * 3] - var2[var5 * 3 - 3];
                float var7 = var2[var5 * 3 + 1] - var2[var5 * 3 - 2];
                float var8 = var2[var5 * 3 + 2] - var2[var5 * 3 - 1];
                var4[var5] = var4[var5 - 1] + (float)Math.sqrt(var6 * var6 + var7 * var7 + var8 * var8);
            }
            float var9 = Math.max(1.0E-6f, var4[var3 - 1]);
            float[] var10 = new float[var3 * 4];
            for (int var11 = 0; var11 < var3; ++var11) {
                var10[var11 * 4] = var2[var11 * 3];
                var10[var11 * 4 + 1] = var2[var11 * 3 + 1];
                var10[var11 * 4 + 2] = var2[var11 * 3 + 2];
                var10[var11 * 4 + 3] = var0 + (var1 - var0) * var4[var11] / var9;
            }
            return TaperedTubeSdf3D.of(var10, false);
        }
        throw new IllegalArgumentException("strand needs at least two points given as x,y,z triples");
    }

    static TaperedTubeSdf3D of(float[] var0, boolean var1) {
        if (var0.length >= 8 && var0.length % 4 == 0) {
            int var2 = var0.length / 4;
            if (var1) {
                float[] var18 = new float[20];
                TaperedTubeSdf3D.fill(var18, 0, var0[0], var0[1], var0[2], var0[3], var0[4], var0[5], var0[6], var0[7], var0[8], var0[9], var0[10], var0[11]);
                return new TaperedTubeSdf3D(var18, 1);
            }
            if (var2 == 2) {
                float[] var17 = new float[20];
                TaperedTubeSdf3D.fill(var17, 0, var0[0], var0[1], var0[2], var0[3], (var0[0] + var0[4]) * 0.5f, (var0[1] + var0[5]) * 0.5f, (var0[2] + var0[6]) * 0.5f, (var0[3] + var0[7]) * 0.5f, var0[4], var0[5], var0[6], var0[7]);
                return new TaperedTubeSdf3D(var17, 1);
            }
            int var3 = var2 - 2;
            float[] var4 = new float[var3 * 20];
            for (int var5 = 1; var5 <= var3; ++var5) {
                float var16;
                float var15;
                float var14;
                float var13;
                float var12;
                float var11;
                float var10;
                float var9;
                int var6 = (var5 - 1) * 4;
                int var7 = var5 * 4;
                int var8 = (var5 + 1) * 4;
                if (var5 == 1) {
                    var9 = var0[0];
                    var10 = var0[1];
                    var11 = var0[2];
                    var12 = var0[3];
                } else {
                    var9 = (var0[var6] + var0[var7]) * 0.5f;
                    var10 = (var0[var6 + 1] + var0[var7 + 1]) * 0.5f;
                    var11 = (var0[var6 + 2] + var0[var7 + 2]) * 0.5f;
                    var12 = (var0[var6 + 3] + var0[var7 + 3]) * 0.5f;
                }
                if (var5 == var3) {
                    var13 = var0[var8];
                    var14 = var0[var8 + 1];
                    var15 = var0[var8 + 2];
                    var16 = var0[var8 + 3];
                } else {
                    var13 = (var0[var7] + var0[var8]) * 0.5f;
                    var14 = (var0[var7 + 1] + var0[var8 + 1]) * 0.5f;
                    var15 = (var0[var7 + 2] + var0[var8 + 2]) * 0.5f;
                    var16 = (var0[var7 + 3] + var0[var8 + 3]) * 0.5f;
                }
                TaperedTubeSdf3D.fill(var4, (var5 - 1) * 20, var9, var10, var11, var12, var0[var7], var0[var7 + 1], var0[var7 + 2], var0[var7 + 3], var13, var14, var15, var16);
            }
            return new TaperedTubeSdf3D(var4, var3);
        }
        throw new IllegalArgumentException("tube needs at least two points given as x,y,z,radius quadruples");
    }

    private static void fill(float[] var0, int var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13) {
        var0[var1] = var2;
        var0[var1 + 1] = var3;
        var0[var1 + 2] = var4;
        var0[var1 + 3] = var6 - var2;
        var0[var1 + 4] = var7 - var3;
        var0[var1 + 5] = var8 - var4;
        var0[var1 + 6] = var2 - 2.0f * var6 + var10;
        var0[var1 + 7] = var3 - 2.0f * var7 + var11;
        var0[var1 + 8] = var4 - 2.0f * var8 + var12;
        var0[var1 + 9] = var5;
        var0[var1 + 10] = var9;
        var0[var1 + 11] = var13;
        float var14 = var0[var1 + 6] * var0[var1 + 6] + var0[var1 + 7] * var0[var1 + 7] + var0[var1 + 8] * var0[var1 + 8];
        float var15 = var10 - var2;
        float var16 = var11 - var3;
        float var17 = var12 - var4;
        float var18 = var15 * var15 + var16 * var16 + var17 * var17;
        boolean var19 = var14 < 1.0E-5f * Math.max(var18, 1.0E-6f);
        float f = var0[var1 + 12] = var19 ? -1.0f : 1.0f / var14;
        var0[var1 + 13] = var19 ? (var18 > 1.0E-12f ? 1.0f / var18 : 0.0f) : 0.0f;
        float var20 = Math.max(var5, Math.max(var9, var13));
        var0[var1 + 14] = Math.min(var2, Math.min(var6, var10)) - var20;
        var0[var1 + 15] = Math.min(var3, Math.min(var7, var11)) - var20;
        var0[var1 + 16] = Math.min(var4, Math.min(var8, var12)) - var20;
        var0[var1 + 17] = Math.max(var2, Math.max(var6, var10)) + var20;
        var0[var1 + 18] = Math.max(var3, Math.max(var7, var11)) + var20;
        var0[var1 + 19] = Math.max(var4, Math.max(var8, var12)) + var20;
    }

    TaperedTubeSdf3D thickened(float var1) {
        if (var1 <= this.minHalf) {
            return this;
        }
        TaperedTubeSdf3D var2 = new TaperedTubeSdf3D(this.seg, this.count);
        var2.ratio = this.ratio;
        var2.flatMode = this.flatMode;
        var2.fx = this.fx;
        var2.fy = this.fy;
        var2.fz = this.fz;
        var2.minHalf = var1;
        var2.paint = this.paint;
        return var2;
    }

    public TaperedTubeSdf3D flatten(float var1, float var2, float var3, float var4) {
        float var5 = (float)Math.sqrt(var2 * var2 + var3 * var3 + var4 * var4);
        if (!(var5 < 1.0E-6f) && !(var1 <= 1.0f)) {
            this.ratio = var1;
            this.flatMode = 1;
            this.fx = var2 / var5;
            this.fy = var3 / var5;
            this.fz = var4 / var5;
            return this;
        }
        this.flatMode = 0;
        return this;
    }

    public TaperedTubeSdf3D flattenFrom(float var1, float var2, float var3, float var4) {
        if (var1 <= 1.0f) {
            this.flatMode = 0;
            return this;
        }
        this.ratio = var1;
        this.flatMode = 2;
        this.fx = var2;
        this.fy = var3;
        this.fz = var4;
        return this;
    }

    @Override
    public float distance(float var1, float var2, float var3) {
        return this.distance(var1, var2, var3, Float.MAX_VALUE);
    }

    @Override
    float distance(float var1, float var2, float var3, float var4) {
        float[] var5 = this.seg;
        int var6 = 0;
        float var7 = Float.MAX_VALUE;
        for (int var8 = 0; var8 < this.count; ++var8) {
            float var9 = TaperedTubeSdf3D.gap(var5, var8 * 20, var1, var2, var3);
            if (!(var9 < var7)) continue;
            var7 = var9;
            var6 = var8;
        }
        if (var7 >= var4) {
            return var7;
        }
        float var11 = this.segment(var5, var6 * 20, var1, var2, var3);
        for (int var12 = 0; var12 < this.count; ++var12) {
            float var10;
            if (var12 == var6 || TaperedTubeSdf3D.gap(var5, var12 * 20, var1, var2, var3) >= Math.min(var11, var4) || !((var10 = this.segment(var5, var12 * 20, var1, var2, var3)) < var11)) continue;
            var11 = var10;
        }
        return var11;
    }

    private static float gap(float[] var0, int var1, float var2, float var3, float var4) {
        float var6;
        float var5;
        var5 = var2 < var0[var1 + 14] ? var0[var1 + 14] - var2 : (var2 > var0[var1 + 17] ? var2 - var0[var1 + 17] : 0.0f);
        var6 = var3 < var0[var1 + 15] ? var0[var1 + 15] - var3 : (var3 > var0[var1 + 18] ? var3 - var0[var1 + 18] : 0.0f);
        float var7 = var4 < var0[var1 + 16] ? var0[var1 + 16] - var4 : (var4 > var0[var1 + 19] ? var4 - var0[var1 + 19] : 0.0f);
        return (float)Math.sqrt(var5 * var5 + var6 * var6 + var7 * var7);
    }

    private float segment(float[] var1, int var2, float var3, float var4, float var5) {
        float var63;
        float var61;
        float var59;
        float var16;
        float var6 = var1[var2];
        float var7 = var1[var2 + 1];
        float var8 = var1[var2 + 2];
        float var9 = var1[var2 + 3];
        float var10 = var1[var2 + 4];
        float var11 = var1[var2 + 5];
        float var12 = var1[var2 + 6];
        float var13 = var1[var2 + 7];
        float var14 = var1[var2 + 8];
        float var15 = var1[var2 + 12];
        if (var15 < 0.0f) {
            float var17 = 2.0f * var9 + var12;
            float var18 = 2.0f * var10 + var13;
            float var19 = 2.0f * var11 + var14;
            var16 = SdfShapes.clamp01(((var3 - var6) * var17 + (var4 - var7) * var18 + (var5 - var8) * var19) * var1[var2 + 13]);
        } else {
            float var20 = var15 * (var9 * var12 + var10 * var13 + var11 * var14);
            float var40 = var6 - var3;
            float var42 = var7 - var4;
            float var44 = var8 - var5;
            float var21 = var15 * (2.0f * (var9 * var9 + var10 * var10 + var11 * var11) + var40 * var12 + var42 * var13 + var44 * var14) / 3.0f;
            float var22 = var15 * (var40 * var9 + var42 * var10 + var44 * var11);
            float var25 = var20 * (2.0f * var20 * var20 - 3.0f * var21) + var22;
            float var23 = var21 - var20 * var20;
            float var24 = var23 * var23 * var23;
            float var26 = var25 * var25 + 4.0f * var24;
            if (var26 >= 0.0f) {
                var26 = (float)Math.sqrt(var26);
                var16 = SdfShapes.clamp01((float)(Math.cbrt((var26 - var25) * 0.5f) + Math.cbrt((-var26 - var25) * 0.5f)) - var20);
            } else {
                float var27 = (float)Math.sqrt(-var23);
                float var28 = var25 / (var23 * var27 * 2.0f);
                var28 = var28 < -1.0f ? -1.0f : Math.min(var28, 1.0f);
                float var29 = (float)Math.acos(var28) / 3.0f;
                float var30 = (float)Math.cos(var29);
                float var31 = (float)Math.sin(var29) * 1.7320508f;
                float var32 = SdfShapes.clamp01((var30 + var30) * var27 - var20);
                float var33 = SdfShapes.clamp01((-var31 - var30) * var27 - var20);
                float var34 = var40 + (2.0f * var9 + var12 * var32) * var32;
                float var35 = var42 + (2.0f * var10 + var13 * var32) * var32;
                float var36 = var44 + (2.0f * var11 + var14 * var32) * var32;
                float var37 = var40 + (2.0f * var9 + var12 * var33) * var33;
                float var38 = var42 + (2.0f * var10 + var13 * var33) * var33;
                float var39 = var44 + (2.0f * var11 + var14 * var33) * var33;
                var16 = var34 * var34 + var35 * var35 + var36 * var36 <= var37 * var37 + var38 * var38 + var39 * var39 ? var32 : var33;
            }
        }
        float var41 = var6 + (2.0f * var9 + var12 * var16) * var16;
        float var43 = var7 + (2.0f * var10 + var13 * var16) * var16;
        float var45 = var8 + (2.0f * var11 + var14 * var16) * var16;
        float var46 = var3 - var41;
        float var47 = var4 - var43;
        float var48 = var5 - var45;
        float var49 = 1.0f - var16;
        float var50 = var49 * var49 * var1[var2 + 9] + 2.0f * var49 * var16 * var1[var2 + 10] + var16 * var16 * var1[var2 + 11];
        float var52 = var46 * var46 + var47 * var47 + var48 * var48;
        if (this.flatMode == 0) {
            return (float)Math.sqrt(var52) - Math.max(var50, this.minHalf);
        }
        var50 = Math.max(var50, this.minHalf * this.ratio);
        float var54 = 2.0f * (var9 + var12 * var16);
        float var55 = 2.0f * (var10 + var13 * var16);
        float var57 = 2.0f * (var11 + var14 * var16);
        float var58 = (float)Math.sqrt(var54 * var54 + var55 * var55 + var57 * var57);
        if (var58 > 1.0E-6f) {
            var54 /= var58;
            var55 /= var58;
            var57 /= var58;
        }
        if (this.flatMode == 1) {
            var59 = this.fx;
            var61 = this.fy;
            var63 = this.fz;
        } else {
            var59 = var41 - this.fx;
            var61 = var43 - this.fy;
            var63 = var45 - this.fz;
        }
        float var65 = var59 * var54 + var61 * var55 + var63 * var57;
        float var66 = (float)Math.sqrt((var59 -= var65 * var54) * var59 + (var61 -= var65 * var55) * var61 + (var63 -= var65 * var57) * var63);
        if (var66 < 1.0E-6f) {
            return (float)Math.sqrt(var52) - var50;
        }
        float var67 = (var46 * var59 + var47 * var61 + var48 * var63) / var66;
        float var68 = Math.max(0.0f, var52 - var67 * var67);
        return ((float)Math.sqrt(var68 + this.ratio * this.ratio * var67 * var67) - var50) / this.ratio;
    }
}

