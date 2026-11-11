/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.PerlinNoise;
import hook.feature.SurfaceMaterialSample;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public abstract class SurfacePaint {
    static final SurfacePaint DEFAULT = SurfacePaint.of(-4934476);

    public abstract void apply(float var1, float var2, float var3, SurfaceMaterialSample var4);

    public static SurfacePaint of(int var0) {
        return new SolidPaint(var0);
    }

    public static SurfacePaint gradient(int var0, float var1, float var2, float var3, int var4, float var5, float var6, float var7) {
        return new GradientPaint(var0, var1, var2, var3, var4, var5, var6, var7);
    }

    public static SurfacePaint vertical(int var0, float var1, int var2, float var3) {
        return new GradientPaint(var0, 0.0f, var1, 0.0f, var2, 0.0f, var3, 0.0f);
    }

    public static SurfacePaint radial(int var0, float var1, float var2, float var3, float var4, int var5) {
        return new RadialPaint(var0, var1, var2, var3, var4, var5);
    }

    public static SurfacePaint wood(int var0, int var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
        return new WoodGrainPaint(var0, var1, var2, var3, var4, var5, var6, var7, var8);
    }

    public static SurfacePaint field(ColorField3D var0) {
        return new FunctionPaint(var0);
    }

    public SurfacePaint noise(int var1, float var2, float var3) {
        return new NoisePaint(this, var1, var2, var3);
    }

    public SurfacePaint grain(float var1, float var2) {
        return new GrainPaint(this, var1, var2);
    }

    public SurfacePaint band(int var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
        return new ColorBandPaint(this, var1, var2, var3, var4, var5, var6, var7, var8, var9);
    }

    public SurfacePaint glow() {
        return this.glow(1.0f);
    }

    public SurfacePaint glow(float var1) {
        return new EmissivePaint(this, var1);
    }

    public SurfacePaint flatLight(float var1) {
        return new LightingPaint(this, Math.max(0.0f, Math.min(1.0f, var1)), Float.intBitsToFloat(2143289344));
    }

    public SurfacePaint occlusion(float var1) {
        return new LightingPaint(this, Float.intBitsToFloat(2143289344), Math.max(0.0f, Math.min(1.0f, var1)));
    }

    static float smooth(float var0, float var1, float var2) {
        float var3 = (var2 - var0) / (var1 - var0);
        var3 = var3 < 0.0f ? 0.0f : Math.min(var3, 1.0f);
        return var3 * var3 * (3.0f - 2.0f * var3);
    }

    @Environment(value=EnvType.CLIENT)
    private static final class SolidPaint
    extends SurfacePaint {
        private final int argb;

        SolidPaint(int var1) {
            this.argb = var1;
        }

        @Override
        public void apply(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            var4.set(this.argb);
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class GradientPaint
    extends SurfacePaint {
        private final int from;
        private final int to;
        private final float ax;
        private final float ay;
        private final float az;
        private final float dx;
        private final float dy;
        private final float dz;
        private final float inv;

        GradientPaint(int var1, float var2, float var3, float var4, int var5, float var6, float var7, float var8) {
            this.from = var1;
            this.to = var5;
            this.ax = var2;
            this.ay = var3;
            this.az = var4;
            this.dx = var6 - var2;
            this.dy = var7 - var3;
            this.dz = var8 - var4;
            float var9 = this.dx * this.dx + this.dy * this.dy + this.dz * this.dz;
            this.inv = var9 < 1.0E-8f ? 0.0f : 1.0f / var9;
        }

        @Override
        public void apply(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            float var5 = ((var1 - this.ax) * this.dx + (var2 - this.ay) * this.dy + (var3 - this.az) * this.dz) * this.inv;
            var5 = var5 < 0.0f ? 0.0f : Math.min(var5, 1.0f);
            var4.set(this.from);
            var4.toward(this.to, var5);
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class RadialPaint
    extends SurfacePaint {
        private final int inner;
        private final int outer;
        private final float cx;
        private final float cy;
        private final float cz;
        private final float radius;

        RadialPaint(int var1, float var2, float var3, float var4, float var5, int var6) {
            this.inner = var1;
            this.outer = var6;
            this.cx = var2;
            this.cy = var3;
            this.cz = var4;
            this.radius = Math.max(1.0E-4f, var5);
        }

        @Override
        public void apply(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            float var5 = var1 - this.cx;
            float var6 = var2 - this.cy;
            float var7 = var3 - this.cz;
            float var8 = (float)Math.sqrt(var5 * var5 + var6 * var6 + var7 * var7) / this.radius;
            var4.set(this.inner);
            var4.toward(this.outer, var8 < 0.0f ? 0.0f : Math.min(var8, 1.0f));
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class WoodGrainPaint
    extends SurfacePaint {
        private final int light;
        private final int dark;
        private final float ax;
        private final float ay;
        private final float az;
        private final float dx;
        private final float dy;
        private final float dz;
        private final float spacing;

        WoodGrainPaint(int var1, int var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
            this.light = var1;
            this.dark = var2;
            this.ax = var3;
            this.ay = var4;
            this.az = var5;
            float var10 = (float)Math.sqrt(var6 * var6 + var7 * var7 + var8 * var8);
            this.dx = var10 < 1.0E-6f ? 0.0f : var6 / var10;
            this.dy = var10 < 1.0E-6f ? 1.0f : var7 / var10;
            this.dz = var10 < 1.0E-6f ? 0.0f : var8 / var10;
            this.spacing = Math.max(0.05f, var9);
        }

        @Override
        public void apply(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            float var5 = var1 - this.ax;
            float var6 = var2 - this.ay;
            float var7 = var3 - this.az;
            float var8 = var5 * this.dx + var6 * this.dy + var7 * this.dz;
            float var9 = var5 - this.dx * var8;
            float var10 = var6 - this.dy * var8;
            float var11 = var7 - this.dz * var8;
            float var12 = (float)Math.sqrt(var9 * var9 + var10 * var10 + var11 * var11);
            float var13 = 1.0f / this.spacing;
            float var14 = PerlinNoise.fbm(var9 * 0.3f + var8 * 0.021f, var10 * 0.3f + var8 * 0.017f, var11 * 0.3f + var8 * 0.013f, 2);
            float var15 = PerlinNoise.fbm(var9 * var13 * 1.6f + var14, var10 * var13 * 1.6f + var8 * 0.045f, var11 * var13 * 1.6f - var14, 3);
            float var16 = var12 * var13 + var14 * 2.2f;
            float var17 = var16 - (float)Math.floor(var16);
            float var18 = WoodGrainPaint.smooth(0.7f, 0.85f, var17) * (1.0f - WoodGrainPaint.smooth(0.88f, 1.0f, var17));
            float var19 = 0.18f + var15 * 0.9f + var18 * 0.22f;
            var4.set(this.light);
            var4.toward(this.dark, var19 < 0.0f ? 0.0f : Math.min(var19, 1.0f));
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class FunctionPaint
    extends SurfacePaint {
        private final ColorField3D field;

        FunctionPaint(ColorField3D var1) {
            this.field = var1;
        }

        @Override
        public void apply(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            var4.set(this.field.argb(var1, var2, var3));
        }
    }

    @FunctionalInterface
    public static interface ColorField3D {
        public int argb(float var1, float var2, float var3);
    }

    @Environment(value=EnvType.CLIENT)
    private static final class NoisePaint
    extends SurfacePaint {
        private final SurfacePaint base;
        private final int other;
        private final float freq;
        private final float amount;

        NoisePaint(SurfacePaint var1, int var2, float var3, float var4) {
            this.base = var1;
            this.other = var2;
            this.freq = 1.0f / Math.max(0.01f, var3);
            this.amount = var4;
        }

        @Override
        public void apply(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            this.base.apply(var1, var2, var3, var4);
            float var5 = PerlinNoise.fbm(var1 * this.freq, var2 * this.freq, var3 * this.freq, 3) * 1.6f + 0.5f;
            float var6 = (var5 < 0.0f ? 0.0f : Math.min(var5, 1.0f)) * this.amount;
            float var7 = var4.glow;
            var4.toward(this.other, var6);
            var4.glow = var7;
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class GrainPaint
    extends SurfacePaint {
        private final SurfacePaint base;
        private final float freq;
        private final float amount;

        GrainPaint(SurfacePaint var1, float var2, float var3) {
            this.base = var1;
            this.freq = 1.0f / Math.max(0.01f, var2);
            this.amount = var3;
        }

        @Override
        public void apply(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            this.base.apply(var1, var2, var3, var4);
            float var5 = 1.0f + PerlinNoise.fbm(var1 * this.freq, var2 * this.freq, var3 * this.freq, 2) * 2.0f * this.amount;
            var4.r *= var5;
            var4.g *= var5;
            var4.b *= var5;
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class ColorBandPaint
    extends SurfacePaint {
        private final SurfacePaint base;
        private final int argb;
        private final float px;
        private final float py;
        private final float pz;
        private final float nx;
        private final float ny;
        private final float nz;
        private final float half;
        private final float soft;

        ColorBandPaint(SurfacePaint var1, int var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10) {
            this.base = var1;
            this.argb = var2;
            this.px = var3;
            this.py = var4;
            this.pz = var5;
            float var11 = (float)Math.sqrt(var6 * var6 + var7 * var7 + var8 * var8);
            this.nx = var11 < 1.0E-6f ? 0.0f : var6 / var11;
            this.ny = var11 < 1.0E-6f ? 1.0f : var7 / var11;
            this.nz = var11 < 1.0E-6f ? 0.0f : var8 / var11;
            this.half = var9;
            this.soft = Math.max(0.001f, var10);
        }

        @Override
        public void apply(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            this.base.apply(var1, var2, var3, var4);
            float var5 = Math.abs((var1 - this.px) * this.nx + (var2 - this.py) * this.ny + (var3 - this.pz) * this.nz);
            float var6 = 1.0f - ColorBandPaint.smooth(this.half, this.half + this.soft, var5);
            if (var6 > 0.0f) {
                float var7 = var4.glow;
                var4.toward(this.argb, var6 * ((float)(this.argb >>> 24) / 255.0f));
                var4.glow = var7;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class EmissivePaint
    extends SurfacePaint {
        private final SurfacePaint base;
        private final float amount;

        EmissivePaint(SurfacePaint var1, float var2) {
            this.base = var1;
            this.amount = var2;
        }

        @Override
        public void apply(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            this.base.apply(var1, var2, var3, var4);
            var4.glow = this.amount;
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class LightingPaint
    extends SurfacePaint {
        private final SurfacePaint base;
        private final float flat;
        private final float occlusion;

        LightingPaint(SurfacePaint var1, float var2, float var3) {
            this.base = var1;
            this.flat = var2;
            this.occlusion = var3;
        }

        @Override
        public void apply(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            this.base.apply(var1, var2, var3, var4);
            if (this.flat == this.flat) {
                var4.flat = this.flat;
                var4.flatWeight = 1.0f;
            }
            if (this.occlusion == this.occlusion) {
                var4.occlusion = this.occlusion;
                var4.occlusionWeight = 1.0f;
            }
        }
    }
}

