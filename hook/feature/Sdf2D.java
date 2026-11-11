/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public abstract class Sdf2D {
    static final float FAR = 100000.0f;
    float minX = -100000.0f;
    float minY = -100000.0f;
    float maxX = 100000.0f;
    float maxY = 100000.0f;

    protected Sdf2D() {
    }

    public abstract float distance(float var1, float var2);

    protected final void bounds(float var1, float var2, float var3, float var4) {
        this.minX = Math.min(var1, var3);
        this.minY = Math.min(var2, var4);
        this.maxX = Math.max(var1, var3);
        this.maxY = Math.max(var2, var4);
    }

    public final float minX() {
        return this.minX;
    }

    public final float minY() {
        return this.minY;
    }

    public final float maxX() {
        return this.maxX;
    }

    public final float maxY() {
        return this.maxY;
    }

    public static Sdf2D circle(float var0, float var1, float var2) {
        return new CircleSdf2D(var0, var1, var2);
    }

    public static Sdf2D ellipse(float var0, float var1, float var2, float var3) {
        return new EllipseSdf2D(var0, var1, var2, var3);
    }

    public static Sdf2D box(float var0, float var1, float var2, float var3, float var4) {
        return new RoundedBoxSdf2D(var0, var1, var2, var3, var4);
    }

    public static Sdf2D line(float var0, float var1, float var2, float var3, float var4) {
        return new TaperedLineSdf2D(var0, var1, var4 * 0.5f, var2, var3, var4 * 0.5f);
    }

    public static Sdf2D taper(float var0, float var1, float var2, float var3, float var4, float var5) {
        return new TaperedLineSdf2D(var0, var1, var2 * 0.5f, var3, var4, var5 * 0.5f);
    }

    public static Sdf2D curve(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
        float[] var10002 = new float[]{var0, var1, var2, var3, var4, var5};
        return new PathSdf2D(var10002, var6 * 0.5f, var7 * 0.5f, true);
    }

    public static Sdf2D path(float var0, float var1, float ... var2) {
        return new PathSdf2D(var2, var0 * 0.5f, var1 * 0.5f, false);
    }

    public static Sdf2D polygon(float ... var0) {
        return new PolygonSdf2D(var0);
    }

    public static Sdf2D arc(float var0, float var1, float var2, float var3, float var4, float var5) {
        int var6 = Math.max(3, (int)Math.ceil(Math.abs(var4 - var3) / 20.0f) + 1);
        float[] var7 = new float[(var6 + 1) * 2];
        for (int var8 = 0; var8 <= var6; ++var8) {
            double var9 = Math.toRadians(var3 + (var4 - var3) * (float)var8 / (float)var6);
            var7[var8 * 2] = var0 + var2 * (float)Math.cos(var9);
            var7[var8 * 2 + 1] = var1 + var2 * (float)Math.sin(var9);
        }
        return new PathSdf2D(var7, var5 * 0.5f, var5 * 0.5f, false);
    }

    public static Sdf2D star(float var0, float var1, float var2, float var3, int var4) {
        float[] var5 = new float[var4 * 4];
        for (int var6 = 0; var6 < var4 * 2; ++var6) {
            double var7 = Math.PI * (double)var6 / (double)var4 - 1.5707963267948966;
            float var9 = (var6 & 1) == 0 ? var2 : var3;
            var5[var6 * 2] = var0 + var9 * (float)Math.cos(var7);
            var5[var6 * 2 + 1] = var1 + var9 * (float)Math.sin(var7);
        }
        return new PolygonSdf2D(var5);
    }

    public static Sdf2D heart(float var0, float var1, float var2) {
        return new HeartSdf2D(var0, var1, var2);
    }

    public static Sdf2D field(DistanceField2D var0, float var1, float var2, float var3, float var4) {
        return new FunctionSdf2D(var0, var1, var2, var3, var4);
    }

    public static Sdf2D union(Sdf2D ... var0) {
        return var0.length == 1 ? var0[0] : new UnionSdf2D(0.0f, var0);
    }

    public static Sdf2D blend(float var0, Sdf2D ... var1) {
        return var1.length == 1 ? var1[0] : new UnionSdf2D(var0, var1);
    }

    public Sdf2D union(Sdf2D var1) {
        float var10002 = 0.0f;
        Sdf2D[] var10003 = new Sdf2D[]{this, var1};
        return new UnionSdf2D(var10002, var10003);
    }

    public Sdf2D blend(float var1, Sdf2D var2) {
        Sdf2D[] var10003 = new Sdf2D[]{this, var2};
        return new UnionSdf2D(var1, var10003);
    }

    public Sdf2D subtract(Sdf2D var1) {
        return new BooleanSdf2D(this, var1, 1, 0.0f);
    }

    public Sdf2D subtract(float var1, Sdf2D var2) {
        return new BooleanSdf2D(this, var2, 1, var1);
    }

    public Sdf2D intersect(Sdf2D var1) {
        return new BooleanSdf2D(this, var1, 2, 0.0f);
    }

    public Sdf2D intersect(float var1, Sdf2D var2) {
        return new BooleanSdf2D(this, var2, 2, var1);
    }

    public Sdf2D inflate(float var1) {
        return new ExpandedSdf2D(this, var1, false);
    }

    public Sdf2D outline(float var1) {
        return new ExpandedSdf2D(this, var1 * 0.5f, true);
    }

    public Sdf2D move(float var1, float var2) {
        return new TransformedSdf2D(this, 1.0f, 0.0f, var1, var2, 0.0f, 0.0f, 1.0f);
    }

    public Sdf2D rotate(float var1, float var2, float var3) {
        double var4 = Math.toRadians(var1);
        return new TransformedSdf2D(this, (float)Math.cos(var4), (float)Math.sin(var4), 0.0f, 0.0f, var2, var3, 1.0f);
    }

    public Sdf2D scale(float var1, float var2, float var3) {
        return new TransformedSdf2D(this, 1.0f, 0.0f, 0.0f, 0.0f, var2, var3, var1);
    }

    public Sdf2D mirrorX(float var1) {
        return new MirroredSdf2D(this, var1);
    }

    static float clamp01(float var0) {
        return var0 < 0.0f ? 0.0f : Math.min(var0, 1.0f);
    }

    @Environment(value=EnvType.CLIENT)
    static final class CircleSdf2D
    extends Sdf2D {
        private final float cx;
        private final float cy;
        private final float r;

        CircleSdf2D(float var1, float var2, float var3) {
            this.cx = var1;
            this.cy = var2;
            this.r = var3;
            this.bounds(var1 - var3, var2 - var3, var1 + var3, var2 + var3);
        }

        @Override
        public float distance(float var1, float var2) {
            float var3 = var1 - this.cx;
            float var4 = var2 - this.cy;
            return (float)Math.sqrt(var3 * var3 + var4 * var4) - this.r;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class EllipseSdf2D
    extends Sdf2D {
        private final float cx;
        private final float cy;
        private final float ix;
        private final float iy;
        private final float iix;
        private final float iiy;
        private final float rmin;

        EllipseSdf2D(float var1, float var2, float var3, float var4) {
            this.cx = var1;
            this.cy = var2;
            this.ix = 1.0f / var3;
            this.iy = 1.0f / var4;
            this.iix = this.ix * this.ix;
            this.iiy = this.iy * this.iy;
            this.rmin = Math.min(var3, var4);
            this.bounds(var1 - var3, var2 - var4, var1 + var3, var2 + var4);
        }

        @Override
        public float distance(float var1, float var2) {
            float var3 = var1 - this.cx;
            float var4 = var2 - this.cy;
            float var5 = var3 * this.ix;
            float var6 = var4 * this.iy;
            float var7 = (float)Math.sqrt(var5 * var5 + var6 * var6);
            float var8 = var3 * this.iix;
            float var9 = var4 * this.iiy;
            float var10 = (float)Math.sqrt(var8 * var8 + var9 * var9);
            return var10 < 1.0E-9f ? -this.rmin : var7 * (var7 - 1.0f) / var10;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class RoundedBoxSdf2D
    extends Sdf2D {
        private final float cx;
        private final float cy;
        private final float hx;
        private final float hy;
        private final float r;

        RoundedBoxSdf2D(float var1, float var2, float var3, float var4, float var5) {
            this.cx = var1;
            this.cy = var2;
            this.r = Math.max(0.0f, Math.min(var5, Math.min(var3, var4)));
            this.hx = var3 - this.r;
            this.hy = var4 - this.r;
            this.bounds(var1 - var3, var2 - var4, var1 + var3, var2 + var4);
        }

        @Override
        public float distance(float var1, float var2) {
            float var3 = Math.abs(var1 - this.cx) - this.hx;
            float var4 = Math.abs(var2 - this.cy) - this.hy;
            float var5 = Math.max(var3, 0.0f);
            float var6 = Math.max(var4, 0.0f);
            return (float)Math.sqrt(var5 * var5 + var6 * var6) + Math.min(Math.max(var3, var4), 0.0f) - this.r;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class TaperedLineSdf2D
    extends Sdf2D {
        private final float ax;
        private final float ay;
        private final float bax;
        private final float bay;
        private final float ra;
        private final float rb;
        private final float l2;
        private final float rr;
        private final float a2;
        private final float il2;
        private final boolean circle;
        private final float sx;
        private final float sy;
        private final float sr;

        TaperedLineSdf2D(float var1, float var2, float var3, float var4, float var5, float var6) {
            this.ax = var1;
            this.ay = var2;
            this.ra = var3;
            this.rb = var6;
            this.bax = var4 - var1;
            this.bay = var5 - var2;
            this.l2 = this.bax * this.bax + this.bay * this.bay;
            this.rr = var3 - var6;
            this.a2 = this.l2 - this.rr * this.rr;
            this.il2 = this.l2 > 1.0E-12f ? 1.0f / this.l2 : 0.0f;
            this.circle = this.a2 <= 1.0E-6f;
            boolean var7 = var3 >= var6;
            this.sx = var7 ? var1 : var4;
            this.sy = var7 ? var2 : var5;
            this.sr = Math.max(var3, var6);
            this.bounds(Math.min(var1 - var3, var4 - var6), Math.min(var2 - var3, var5 - var6), Math.max(var1 + var3, var4 + var6), Math.max(var2 + var3, var5 + var6));
        }

        @Override
        public float distance(float var1, float var2) {
            if (this.circle) {
                float var13 = var1 - this.sx;
                float var14 = var2 - this.sy;
                return (float)Math.sqrt(var13 * var13 + var14 * var14) - this.sr;
            }
            float var3 = var1 - this.ax;
            float var4 = var2 - this.ay;
            float var5 = var3 * this.bax + var4 * this.bay;
            float var6 = var5 - this.l2;
            float var7 = var3 * this.l2 - this.bax * var5;
            float var8 = var4 * this.l2 - this.bay * var5;
            float var9 = var7 * var7 + var8 * var8;
            float var10 = var5 * var5 * this.l2;
            float var11 = var6 * var6 * this.l2;
            float var12 = Math.signum(this.rr) * this.rr * this.rr * var9;
            if (Math.signum(var6) * this.a2 * var11 > var12) {
                return (float)Math.sqrt(var9 + var11) * this.il2 - this.rb;
            }
            return Math.signum(var5) * this.a2 * var10 < var12 ? (float)Math.sqrt(var9 + var10) * this.il2 - this.ra : ((float)Math.sqrt(var9 * this.a2 * this.il2) + var5 * this.rr) * this.il2 - this.ra;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class PathSdf2D
    extends Sdf2D {
        private final float[] seg;
        private final int count;

        PathSdf2D(float[] var1, float var2, float var3, boolean var4) {
            float var23;
            float var22;
            float var21;
            float var20;
            if (var1.length >= 4 && var1.length % 2 == 0) {
                int var5 = var1.length / 2;
                float[] var6 = new float[var5];
                for (int var7 = 1; var7 < var5; ++var7) {
                    float var8 = var1[var7 * 2] - var1[var7 * 2 - 2];
                    float var9 = var1[var7 * 2 + 1] - var1[var7 * 2 - 1];
                    var6[var7] = var6[var7 - 1] + (float)Math.sqrt(var8 * var8 + var9 * var9);
                }
                float var16 = Math.max(1.0E-6f, var6[var5 - 1]);
                float[] var17 = new float[var5];
                for (int var18 = 0; var18 < var5; ++var18) {
                    var17[var18] = var2 + (var3 - var2) * var6[var18] / var16;
                }
                if (!var4 && var5 != 2) {
                    this.count = var5 - 2;
                    this.seg = new float[this.count * 12];
                    for (int var19 = 1; var19 <= this.count; ++var19) {
                        float var10 = var19 == 1 ? var1[0] : (var1[var19 * 2 - 2] + var1[var19 * 2]) * 0.5f;
                        float var11 = var19 == 1 ? var1[1] : (var1[var19 * 2 - 1] + var1[var19 * 2 + 1]) * 0.5f;
                        float var12 = var19 == 1 ? var17[0] : (var17[var19 - 1] + var17[var19]) * 0.5f;
                        float var13 = var19 == this.count ? var1[var19 * 2 + 2] : (var1[var19 * 2] + var1[var19 * 2 + 2]) * 0.5f;
                        float var14 = var19 == this.count ? var1[var19 * 2 + 3] : (var1[var19 * 2 + 1] + var1[var19 * 2 + 3]) * 0.5f;
                        float var15 = var19 == this.count ? var17[var19 + 1] : (var17[var19] + var17[var19 + 1]) * 0.5f;
                        PathSdf2D.fill(this.seg, (var19 - 1) * 12, var10, var11, var12, var1[var19 * 2], var1[var19 * 2 + 1], var17[var19], var13, var14, var15);
                    }
                } else {
                    this.count = 1;
                    this.seg = new float[12];
                    if (var5 == 2) {
                        PathSdf2D.fill(this.seg, 0, var1[0], var1[1], var17[0], (var1[0] + var1[2]) * 0.5f, (var1[1] + var1[3]) * 0.5f, (var17[0] + var17[1]) * 0.5f, var1[2], var1[3], var17[1]);
                    } else {
                        PathSdf2D.fill(this.seg, 0, var1[0], var1[1], var2, var1[2], var1[3], (var2 + var3) * 0.5f, var1[4], var1[5], var3);
                    }
                }
                var20 = 100000.0f;
                var21 = 100000.0f;
                var22 = -100000.0f;
                var23 = -100000.0f;
                for (int var24 = 0; var24 < this.count; ++var24) {
                    int var25 = var24 * 12;
                    var20 = Math.min(var20, this.seg[var25 + 8]);
                    var21 = Math.min(var21, this.seg[var25 + 9]);
                    var22 = Math.max(var22, this.seg[var25 + 10]);
                    var23 = Math.max(var23, this.seg[var25 + 11]);
                }
            } else {
                throw new IllegalArgumentException("path needs at least two points given as x,y pairs");
            }
            this.bounds(var20, var21, var22, var23);
        }

        private static void fill(float[] var0, int var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10) {
            var0[var1] = var2;
            var0[var1 + 1] = var3;
            var0[var1 + 2] = var5;
            var0[var1 + 3] = var6;
            var0[var1 + 4] = var8;
            var0[var1 + 5] = var9;
            var0[var1 + 6] = var4;
            var0[var1 + 7] = var10;
            float var11 = Math.max(var4, Math.max(var7, var10));
            var0[var1 + 8] = Math.min(var2, Math.min(var5, var8)) - var11;
            var0[var1 + 9] = Math.min(var3, Math.min(var6, var9)) - var11;
            var0[var1 + 10] = Math.max(var2, Math.max(var5, var8)) + var11;
            var0[var1 + 11] = Math.max(var3, Math.max(var6, var9)) + var11;
        }

        @Override
        public float distance(float var1, float var2) {
            float var3 = Float.MAX_VALUE;
            for (int var4 = 0; var4 < this.count; ++var4) {
                float var8;
                float var7;
                float var6;
                int var5 = var4 * 12;
                var6 = var1 < this.seg[var5 + 8] ? this.seg[var5 + 8] - var1 : (var1 > this.seg[var5 + 10] ? var1 - this.seg[var5 + 10] : 0.0f);
                var7 = var2 < this.seg[var5 + 9] ? this.seg[var5 + 9] - var2 : (var2 > this.seg[var5 + 11] ? var2 - this.seg[var5 + 11] : 0.0f);
                if (var3 > 0.0f && var3 != Float.MAX_VALUE && var6 * var6 + var7 * var7 >= var3 * var3 || !((var8 = this.segment(var5, var1, var2)) < var3)) continue;
                var3 = var8;
            }
            return var3;
        }

        private float segment(int var1, float var2, float var3) {
            float var20;
            float var4 = this.seg[var1];
            float var5 = this.seg[var1 + 1];
            float var6 = this.seg[var1 + 2];
            float var7 = this.seg[var1 + 3];
            float var8 = this.seg[var1 + 4];
            float var9 = this.seg[var1 + 5];
            float var10 = var6 - var4;
            float var11 = var7 - var5;
            float var12 = var4 - 2.0f * var6 + var8;
            float var13 = var5 - 2.0f * var7 + var9;
            float var14 = var12 * var12 + var13 * var13;
            float var15 = var4 - var2;
            float var16 = var5 - var3;
            float var17 = var8 - var4;
            float var18 = var9 - var5;
            float var19 = var17 * var17 + var18 * var18;
            if (var14 < 1.0E-5f * Math.max(var19, 1.0E-6f)) {
                var20 = var19 > 1.0E-12f ? PathSdf2D.clamp01(((var2 - var4) * var17 + (var3 - var5) * var18) / var19) : 0.0f;
            } else {
                float var21 = 1.0f / var14;
                float var22 = var21 * (var10 * var12 + var11 * var13);
                float var23 = var21 * (2.0f * (var10 * var10 + var11 * var11) + var15 * var12 + var16 * var13) / 3.0f;
                float var24 = var21 * (var15 * var10 + var16 * var11);
                float var27 = var22 * (2.0f * var22 * var22 - 3.0f * var23) + var24;
                float var25 = var23 - var22 * var22;
                float var26 = var25 * var25 * var25;
                float var28 = var27 * var27 + 4.0f * var26;
                if (var28 >= 0.0f) {
                    var28 = (float)Math.sqrt(var28);
                    var20 = PathSdf2D.clamp01((float)(Math.cbrt((var28 - var27) * 0.5f) + Math.cbrt((-var28 - var27) * 0.5f)) - var22);
                } else {
                    float var29 = (float)Math.sqrt(-var25);
                    float var30 = var27 / (var25 * var29 * 2.0f);
                    var30 = var30 < -1.0f ? -1.0f : Math.min(var30, 1.0f);
                    float var31 = (float)Math.acos(var30) / 3.0f;
                    float var32 = (float)Math.cos(var31);
                    float var33 = (float)Math.sin(var31) * 1.7320508f;
                    float var34 = PathSdf2D.clamp01((var32 + var32) * var29 - var22);
                    float var35 = PathSdf2D.clamp01((-var33 - var32) * var29 - var22);
                    float var36 = var15 + (2.0f * var10 + var12 * var34) * var34;
                    float var37 = var16 + (2.0f * var11 + var13 * var34) * var34;
                    float var38 = var15 + (2.0f * var10 + var12 * var35) * var35;
                    float var39 = var16 + (2.0f * var11 + var13 * var35) * var35;
                    var20 = var36 * var36 + var37 * var37 <= var38 * var38 + var39 * var39 ? var34 : var35;
                }
            }
            float var40 = var15 + (2.0f * var10 + var12 * var20) * var20;
            float var41 = var16 + (2.0f * var11 + var13 * var20) * var20;
            float var42 = this.seg[var1 + 6] + (this.seg[var1 + 7] - this.seg[var1 + 6]) * var20;
            return (float)Math.sqrt(var40 * var40 + var41 * var41) - var42;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class PolygonSdf2D
    extends Sdf2D {
        private final float[] v;
        private final int n;

        PolygonSdf2D(float[] var1) {
            float var5;
            float var4;
            float var3;
            float var2;
            if (var1.length >= 6 && var1.length % 2 == 0) {
                this.v = (float[])var1.clone();
                this.n = var1.length / 2;
                var2 = 100000.0f;
                var3 = 100000.0f;
                var4 = -100000.0f;
                var5 = -100000.0f;
                for (int var6 = 0; var6 < this.n; ++var6) {
                    var2 = Math.min(var2, this.v[var6 * 2]);
                    var3 = Math.min(var3, this.v[var6 * 2 + 1]);
                    var4 = Math.max(var4, this.v[var6 * 2]);
                    var5 = Math.max(var5, this.v[var6 * 2 + 1]);
                }
            } else {
                throw new IllegalArgumentException("polygon needs at least three points given as x,y pairs");
            }
            this.bounds(var2, var3, var4, var5);
        }

        @Override
        public float distance(float var1, float var2) {
            float var3 = var1 - this.v[0];
            float var4 = var2 - this.v[1];
            float var5 = var3 * var3 + var4 * var4;
            float var6 = 1.0f;
            int var7 = 0;
            int var8 = this.n - 1;
            while (var7 < this.n) {
                boolean var19;
                float var9 = this.v[var8 * 2] - this.v[var7 * 2];
                float var10 = this.v[var8 * 2 + 1] - this.v[var7 * 2 + 1];
                float var11 = var1 - this.v[var7 * 2];
                float var12 = var2 - this.v[var7 * 2 + 1];
                float var13 = var9 * var9 + var10 * var10;
                float var14 = var13 > 1.0E-12f ? PolygonSdf2D.clamp01((var11 * var9 + var12 * var10) / var13) : 0.0f;
                float var15 = var11 - var9 * var14;
                float var16 = var12 - var10 * var14;
                var5 = Math.min(var5, var15 * var15 + var16 * var16);
                boolean var17 = var2 >= this.v[var7 * 2 + 1];
                boolean var18 = var2 < this.v[var8 * 2 + 1];
                boolean bl = var19 = var9 * var12 > var10 * var11;
                if (var17 && var18 && var19 || !var17 && !var18 && !var19) {
                    var6 = -var6;
                }
                var8 = var7++;
            }
            return var6 * (float)Math.sqrt(var5);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class HeartSdf2D
    extends Sdf2D {
        private final float cx;
        private final float cy;
        private final float size;

        HeartSdf2D(float var1, float var2, float var3) {
            this.cx = var1;
            this.cy = var2;
            this.size = var3;
            this.bounds(var1 - var3 * 0.7f, var2 - var3 * 0.35f, var1 + var3 * 0.7f, var2 + var3 * 0.8f);
        }

        @Override
        public float distance(float var1, float var2) {
            float var4;
            float var3 = Math.abs(var1 - this.cx) / this.size;
            if (var3 + (var4 = (this.cy + this.size * 0.75f - var2) / this.size) > 1.0f) {
                float var5 = var3 - 0.25f;
                float var10 = var4 - 0.75f;
                return ((float)Math.sqrt(var5 * var5 + var10 * var10) - 0.35355338f) * this.size;
            }
            float var6 = var4 - 1.0f;
            float var7 = Math.max(var3 + var4, 0.0f) * 0.5f;
            float var8 = var3 - var7;
            float var9 = var4 - var7;
            return (float)Math.sqrt(Math.min(var3 * var3 + var6 * var6, var8 * var8 + var9 * var9)) * Math.signum(var3 - var4) * this.size;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class FunctionSdf2D
    extends Sdf2D {
        private final DistanceField2D field;

        FunctionSdf2D(DistanceField2D var1, float var2, float var3, float var4, float var5) {
            this.field = var1;
            this.bounds(var2, var3, var4, var5);
        }

        @Override
        public float distance(float var1, float var2) {
            return this.field.distance(var1, var2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface DistanceField2D {
        public float distance(float var1, float var2);
    }

    @Environment(value=EnvType.CLIENT)
    static final class UnionSdf2D
    extends Sdf2D {
        private final Sdf2D[] kids;
        private final float k;

        UnionSdf2D(float var1, Sdf2D[] var2) {
            this.kids = (Sdf2D[])var2.clone();
            this.k = Math.max(0.0f, var1);
            float var3 = 100000.0f;
            float var4 = 100000.0f;
            float var5 = -100000.0f;
            float var6 = -100000.0f;
            for (Sdf2D var10 : this.kids) {
                var3 = Math.min(var3, var10.minX);
                var4 = Math.min(var4, var10.minY);
                var5 = Math.max(var5, var10.maxX);
                var6 = Math.max(var6, var10.maxY);
            }
            this.bounds(var3 - this.k, var4 - this.k, var5 + this.k, var6 + this.k);
        }

        @Override
        public float distance(float var1, float var2) {
            float var3 = this.kids[0].distance(var1, var2);
            for (int var4 = 1; var4 < this.kids.length; ++var4) {
                float var5 = this.kids[var4].distance(var1, var2);
                if (this.k > 0.0f) {
                    float var6 = UnionSdf2D.clamp01(0.5f + 0.5f * (var5 - var3) / this.k);
                    var3 = var5 + (var3 - var5) * var6 - this.k * var6 * (1.0f - var6);
                    continue;
                }
                if (!(var5 < var3)) continue;
                var3 = var5;
            }
            return var3;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class BooleanSdf2D
    extends Sdf2D {
        private final Sdf2D a;
        private final Sdf2D b;
        private final int mode;
        private final float k;

        BooleanSdf2D(Sdf2D var1, Sdf2D var2, int var3, float var4) {
            this.a = var1;
            this.b = var2;
            this.mode = var3;
            this.k = Math.max(0.0f, var4);
            if (var3 == 2) {
                this.bounds(Math.max(var1.minX, var2.minX), Math.max(var1.minY, var2.minY), Math.min(var1.maxX, var2.maxX), Math.min(var1.maxY, var2.maxY));
            } else {
                this.bounds(var1.minX, var1.minY, var1.maxX, var1.maxY);
            }
        }

        @Override
        public float distance(float var1, float var2) {
            float var3 = this.a.distance(var1, var2);
            float var4 = this.b.distance(var1, var2);
            if (this.mode == 1) {
                if (this.k <= 0.0f) {
                    return Math.max(var3, -var4);
                }
                float var6 = BooleanSdf2D.clamp01(0.5f - 0.5f * (var3 + var4) / this.k);
                return var3 + (-var4 - var3) * var6 + this.k * var6 * (1.0f - var6);
            }
            if (this.k <= 0.0f) {
                return Math.max(var3, var4);
            }
            float var5 = BooleanSdf2D.clamp01(0.5f - 0.5f * (var4 - var3) / this.k);
            return var4 + (var3 - var4) * var5 + this.k * var5 * (1.0f - var5);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class ExpandedSdf2D
    extends Sdf2D {
        private final Sdf2D child;
        private final float r;
        private final boolean ring;

        ExpandedSdf2D(Sdf2D var1, float var2, boolean var3) {
            this.child = var1;
            this.r = var2;
            this.ring = var3;
            float var4 = Math.abs(var2);
            this.bounds(var1.minX - var4, var1.minY - var4, var1.maxX + var4, var1.maxY + var4);
        }

        @Override
        public float distance(float var1, float var2) {
            float var3 = this.child.distance(var1, var2);
            return this.ring ? Math.abs(var3) - this.r : var3 - this.r;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class TransformedSdf2D
    extends Sdf2D {
        private final Sdf2D child;
        private final float c;
        private final float s;
        private final float dx;
        private final float dy;
        private final float cx;
        private final float cy;
        private final float k;

        TransformedSdf2D(Sdf2D var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
            this.child = var1;
            this.c = var2;
            this.s = var3;
            this.dx = var4;
            this.dy = var5;
            this.cx = var6;
            this.cy = var7;
            this.k = var8;
            float var9 = 100000.0f;
            float var10 = 100000.0f;
            float var11 = -100000.0f;
            float var12 = -100000.0f;
            for (int var13 = 0; var13 < 4; ++var13) {
                float var14 = ((var13 & 1) == 0 ? var1.minX : var1.maxX) - var6;
                float var15 = ((var13 & 2) == 0 ? var1.minY : var1.maxY) - var7;
                float var16 = (var2 * var14 - var3 * var15) * var8 + var6 + var4;
                float var17 = (var3 * var14 + var2 * var15) * var8 + var7 + var5;
                var9 = Math.min(var9, var16);
                var10 = Math.min(var10, var17);
                var11 = Math.max(var11, var16);
                var12 = Math.max(var12, var17);
            }
            this.bounds(var9, var10, var11, var12);
        }

        @Override
        public float distance(float var1, float var2) {
            float var3 = (var1 - this.dx - this.cx) / this.k;
            float var4 = (var2 - this.dy - this.cy) / this.k;
            return this.child.distance(this.c * var3 + this.s * var4 + this.cx, -this.s * var3 + this.c * var4 + this.cy) * this.k;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class MirroredSdf2D
    extends Sdf2D {
        private final Sdf2D child;
        private final float axis;

        MirroredSdf2D(Sdf2D var1, float var2) {
            this.child = var1;
            this.axis = var2;
            this.bounds(2.0f * var2 - var1.maxX, var1.minY, 2.0f * var2 - var1.minX, var1.maxY);
        }

        @Override
        public float distance(float var1, float var2) {
            return this.child.distance(2.0f * this.axis - var1, var2);
        }
    }
}

