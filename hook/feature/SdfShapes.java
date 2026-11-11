/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.PerlinNoise;
import hook.feature.RotationMatrix3;
import hook.feature.Sdf2D;
import hook.feature.Sdf3D;
import hook.feature.SurfaceMaterialSample;
import hook.feature.TaperedTubeSdf3D;
import java.util.Arrays;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class SdfShapes {
    private static final ThreadLocal<FloatArrayPool> SCRATCH = ThreadLocal.withInitial(FloatArrayPool::new);

    private SdfShapes() {
    }

    public static Sdf3D sphere(float var0, float var1, float var2, float var3) {
        return new SphereSdf3D(var0, var1, var2, var3);
    }

    public static Sdf3D ellipsoid(float var0, float var1, float var2, float var3, float var4, float var5) {
        return new EllipsoidSdf3D(var0, var1, var2, var3, var4, var5);
    }

    public static Sdf3D box(float var0, float var1, float var2, float var3, float var4, float var5) {
        return new RoundedBoxSdf3D(var0, var1, var2, var3, var4, var5, 0.0f);
    }

    public static Sdf3D box(float var0, float var1, float var2, float var3, float var4, float var5, float var6) {
        return new RoundedBoxSdf3D(var0, var1, var2, var3, var4, var5, var6);
    }

    public static Sdf3D capsule(float var0, float var1, float var2, float var3, float var4, float var5, float var6) {
        return new CapsuleSdf3D(var0, var1, var2, var6, var3, var4, var5, var6);
    }

    public static Sdf3D cone(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
        return new CapsuleSdf3D(var0, var1, var2, var3, var4, var5, var6, var7);
    }

    public static Sdf3D cylinder(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
        return new ConeSdf3D(var0, var1, var2, var3, var4, var5, var6, var7);
    }

    public static Sdf3D torus(float var0, float var1, float var2, float var3, float var4) {
        return new TorusSdf3D(var0, var1, var2, var3, var4);
    }

    public static Sdf3D plane(float var0, float var1, float var2, float var3, float var4, float var5) {
        return new PlaneSdf3D(var0, var1, var2, var3, var4, var5);
    }

    public static Sdf3D below(float var0) {
        return new PlaneSdf3D(0.0f, var0, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    public static Sdf3D above(float var0) {
        return new PlaneSdf3D(0.0f, var0, 0.0f, 0.0f, -1.0f, 0.0f);
    }

    public static TaperedTubeSdf3D bezier(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10) {
        float[] var10000 = new float[]{var0, var1, var2, var9, var3, var4, var5, (var9 + var10) * 0.5f, var6, var7, var8, var10};
        return TaperedTubeSdf3D.of(var10000, true);
    }

    public static TaperedTubeSdf3D strand(float var0, float var1, float ... var2) {
        return TaperedTubeSdf3D.tapered(var0, var1, var2);
    }

    public static TaperedTubeSdf3D tube(float ... var0) {
        return TaperedTubeSdf3D.of(var0, false);
    }

    public static Sdf3D extrude(Sdf2D var0, float var1, float var2) {
        return new ExtrudedSdf3D(var0, var1, var2);
    }

    public static Sdf3D revolve(Sdf2D var0) {
        return new RevolvedSdf3D(var0);
    }

    public static Sdf3D field(DistanceField3D var0, float var1, float var2, float var3, float var4, float var5, float var6) {
        return new FunctionSdf3D(var0, var1, var2, var3, var4, var5, var6);
    }

    public static Sdf3D union(Sdf3D ... var0) {
        return var0.length == 1 ? var0[0] : new UnionSdf3D(0.0f, var0);
    }

    public static Sdf3D blend(float var0, Sdf3D ... var1) {
        return var1.length == 1 ? var1[0] : new UnionSdf3D(var0, var1);
    }

    public static Sdf3D subtract(Sdf3D var0, Sdf3D var1) {
        return new DifferenceSdf3D(0.0f, var0, var1);
    }

    public static Sdf3D subtract(float var0, Sdf3D var1, Sdf3D var2) {
        return new DifferenceSdf3D(var0, var1, var2);
    }

    public static Sdf3D intersect(Sdf3D var0, Sdf3D var1) {
        return new IntersectionSdf3D(0.0f, var0, var1);
    }

    public static Sdf3D intersect(float var0, Sdf3D var1, Sdf3D var2) {
        return new IntersectionSdf3D(var0, var1, var2);
    }

    static float clamp01(float var0) {
        return var0 < 0.0f ? 0.0f : Math.min(var0, 1.0f);
    }

    static float length(float var0, float var1, float var2) {
        return (float)Math.sqrt(var0 * var0 + var1 * var1 + var2 * var2);
    }

    static float boxLowerBound(Sdf3D var0, float var1, float var2, float var3) {
        float var4 = (var0.minX + var0.maxX) * 0.5f;
        float var5 = (var0.minY + var0.maxY) * 0.5f;
        float var6 = (var0.minZ + var0.maxZ) * 0.5f;
        float var7 = Math.abs(var1 - var4) - (var0.maxX - var0.minX) * 0.5f;
        float var8 = Math.abs(var2 - var5) - (var0.maxY - var0.minY) * 0.5f;
        float var9 = Math.abs(var3 - var6) - (var0.maxZ - var0.minZ) * 0.5f;
        float var10 = Math.max(var7, 0.0f);
        float var11 = Math.max(var8, 0.0f);
        float var12 = Math.max(var9, 0.0f);
        return (float)Math.sqrt(var10 * var10 + var11 * var11 + var12 * var12) + Math.min(Math.max(var7, Math.max(var8, var9)), 0.0f);
    }

    @Environment(value=EnvType.CLIENT)
    static final class SphereSdf3D
    extends Sdf3D {
        private final float cx;
        private final float cy;
        private final float cz;
        private final float r;

        SphereSdf3D(float var1, float var2, float var3, float var4) {
            this.cx = var1;
            this.cy = var2;
            this.cz = var3;
            this.r = var4;
            this.bounds(var1 - var4, var2 - var4, var3 - var4, var1 + var4, var2 + var4, var3 + var4);
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            return SdfShapes.length(var1 - this.cx, var2 - this.cy, var3 - this.cz) - this.r;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class EllipsoidSdf3D
    extends Sdf3D {
        private final float cxField;
        private final float cyField;
        private final float czField;
        private final float ixField;
        private final float iyField;
        private final float izField;
        private final float iixField;
        private final float iiyField;
        private final float iizField;
        private final float rminField;

        EllipsoidSdf3D(float var1, float var2, float var3, float var4, float var5, float var6) {
            this.cxField = var1;
            this.cyField = var2;
            this.czField = var3;
            this.ixField = 1.0f / var4;
            this.iyField = 1.0f / var5;
            this.izField = 1.0f / var6;
            this.iixField = this.ixField * this.ixField;
            this.iiyField = this.iyField * this.iyField;
            this.iizField = this.izField * this.izField;
            this.rminField = Math.min(var4, Math.min(var5, var6));
            this.bounds(var1 - var4, var2 - var5, var3 - var6, var1 + var4, var2 + var5, var3 + var6);
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            float var4 = var1 - this.cxField;
            float var5 = var2 - this.cyField;
            float var6 = var3 - this.czField;
            float var7 = SdfShapes.length(var4 * this.ixField, var5 * this.iyField, var6 * this.izField);
            float var8 = SdfShapes.length(var4 * this.iixField, var5 * this.iiyField, var6 * this.iizField);
            return var8 < 1.0E-9f ? -this.rminField : var7 * (var7 - 1.0f) / var8;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class RoundedBoxSdf3D
    extends Sdf3D {
        private final float cxField;
        private final float cyField;
        private final float czField;
        private final float hxField;
        private final float hyField;
        private final float hzField;
        private final float r;

        RoundedBoxSdf3D(float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
            this.cxField = var1;
            this.cyField = var2;
            this.czField = var3;
            this.r = Math.max(0.0f, Math.min(var7, Math.min(var4, Math.min(var5, var6))));
            this.hxField = var4 - this.r;
            this.hyField = var5 - this.r;
            this.hzField = var6 - this.r;
            this.bounds(var1 - var4, var2 - var5, var3 - var6, var1 + var4, var2 + var5, var3 + var6);
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            float var4 = Math.abs(var1 - this.cxField) - this.hxField;
            float var5 = Math.abs(var2 - this.cyField) - this.hyField;
            float var6 = Math.abs(var3 - this.czField) - this.hzField;
            float var7 = Math.max(var4, 0.0f);
            float var8 = Math.max(var5, 0.0f);
            float var9 = Math.max(var6, 0.0f);
            return SdfShapes.length(var7, var8, var9) + Math.min(Math.max(var4, Math.max(var5, var6)), 0.0f) - this.r;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class CapsuleSdf3D
    extends Sdf3D {
        private final float axField;
        private final float ayField;
        private final float azField;
        private final float baxField;
        private final float bayField;
        private final float bazField;
        private final float raField;
        private final float rbField;
        private final float l2Field;
        private final float rrField;
        private final float a2Field;
        private final float il2Field;
        private final boolean sphereField;
        private final float sxField;
        private final float syField;
        private final float szField;
        private final float srField;

        CapsuleSdf3D(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
            this.axField = var1;
            this.ayField = var2;
            this.azField = var3;
            this.raField = var4;
            this.rbField = var8;
            this.baxField = var5 - var1;
            this.bayField = var6 - var2;
            this.bazField = var7 - var3;
            this.l2Field = this.baxField * this.baxField + this.bayField * this.bayField + this.bazField * this.bazField;
            this.rrField = var4 - var8;
            this.a2Field = this.l2Field - this.rrField * this.rrField;
            this.il2Field = this.l2Field > 1.0E-12f ? 1.0f / this.l2Field : 0.0f;
            this.sphereField = this.a2Field <= 1.0E-6f;
            boolean var9 = var4 >= var8;
            this.sxField = var9 ? var1 : var5;
            this.syField = var9 ? var2 : var6;
            this.szField = var9 ? var3 : var7;
            this.srField = Math.max(var4, var8);
            this.bounds(Math.min(var1 - var4, var5 - var8), Math.min(var2 - var4, var6 - var8), Math.min(var3 - var4, var7 - var8), Math.max(var1 + var4, var5 + var8), Math.max(var2 + var4, var6 + var8), Math.max(var3 + var4, var7 + var8));
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            if (this.sphereField) {
                return SdfShapes.length(var1 - this.sxField, var2 - this.syField, var3 - this.szField) - this.srField;
            }
            float var4 = var1 - this.axField;
            float var5 = var2 - this.ayField;
            float var6 = var3 - this.azField;
            float var7 = var4 * this.baxField + var5 * this.bayField + var6 * this.bazField;
            float var8 = var7 - this.l2Field;
            float var9 = var4 * this.l2Field - this.baxField * var7;
            float var10 = var5 * this.l2Field - this.bayField * var7;
            float var11 = var6 * this.l2Field - this.bazField * var7;
            float var12 = var9 * var9 + var10 * var10 + var11 * var11;
            float var13 = var7 * var7 * this.l2Field;
            float var14 = var8 * var8 * this.l2Field;
            float var15 = Math.signum(this.rrField) * this.rrField * this.rrField * var12;
            if (Math.signum(var8) * this.a2Field * var14 > var15) {
                return (float)Math.sqrt(var12 + var14) * this.il2Field - this.rbField;
            }
            return Math.signum(var7) * this.a2Field * var13 < var15 ? (float)Math.sqrt(var12 + var13) * this.il2Field - this.raField : ((float)Math.sqrt(var12 * this.a2Field * this.il2Field) + var7 * this.rrField) * this.il2Field - this.raField;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class ConeSdf3D
    extends Sdf3D {
        private final float axField;
        private final float ayField;
        private final float azField;
        private final float baxField;
        private final float bayField;
        private final float bazField;
        private final float babaField;
        private final float r;
        private final float roundField;

        ConeSdf3D(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
            float var9 = var4 - var1;
            float var10 = var5 - var2;
            float var11 = var6 - var3;
            float var12 = SdfShapes.length(var9, var10, var11);
            float var13 = Math.max(0.0f, Math.min(var8, Math.min(var7, var12 * 0.5f)));
            float var14 = var12 > 1.0E-6f ? var9 / var12 : 0.0f;
            float var15 = var12 > 1.0E-6f ? var10 / var12 : 1.0f;
            float var16 = var12 > 1.0E-6f ? var11 / var12 : 0.0f;
            this.axField = var1 + var14 * var13;
            this.ayField = var2 + var15 * var13;
            this.azField = var3 + var16 * var13;
            this.baxField = var4 - var14 * var13 - this.axField;
            this.bayField = var5 - var15 * var13 - this.ayField;
            this.bazField = var6 - var16 * var13 - this.azField;
            this.babaField = Math.max(1.0E-8f, this.baxField * this.baxField + this.bayField * this.bayField + this.bazField * this.bazField);
            this.r = var7 - var13;
            this.roundField = var13;
            float var17 = var7 * (float)Math.sqrt(Math.max(0.0f, 1.0f - var14 * var14));
            float var18 = var7 * (float)Math.sqrt(Math.max(0.0f, 1.0f - var15 * var15));
            float var19 = var7 * (float)Math.sqrt(Math.max(0.0f, 1.0f - var16 * var16));
            this.bounds(Math.min(var1, var4) - var17, Math.min(var2, var5) - var18, Math.min(var3, var6) - var19, Math.max(var1, var4) + var17, Math.max(var2, var5) + var18, Math.max(var3, var6) + var19);
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            float var4 = var1 - this.axField;
            float var5 = var2 - this.ayField;
            float var6 = var3 - this.azField;
            float var7 = var4 * this.baxField + var5 * this.bayField + var6 * this.bazField;
            float var8 = SdfShapes.length(var4 * this.babaField - this.baxField * var7, var5 * this.babaField - this.bayField * var7, var6 * this.babaField - this.bazField * var7) - this.r * this.babaField;
            float var9 = Math.abs(var7 - this.babaField * 0.5f) - this.babaField * 0.5f;
            float var10 = var8 * var8;
            float var11 = var9 * var9 * this.babaField;
            float var12 = Math.max(var8, var9) < 0.0f ? -Math.min(var10, var11) : (var8 > 0.0f ? var10 : 0.0f) + (var9 > 0.0f ? var11 : 0.0f);
            return Math.signum(var12) * (float)Math.sqrt(Math.abs(var12)) / this.babaField - this.roundField;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class TorusSdf3D
    extends Sdf3D {
        private final float cxField;
        private final float cyField;
        private final float czField;
        private final float majorField;
        private final float minorField;

        TorusSdf3D(float var1, float var2, float var3, float var4, float var5) {
            this.cxField = var1;
            this.cyField = var2;
            this.czField = var3;
            this.majorField = var4;
            this.minorField = var5;
            float var6 = var4 + var5;
            this.bounds(var1 - var6, var2 - var5, var3 - var6, var1 + var6, var2 + var5, var3 + var6);
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            float var4 = var1 - this.cxField;
            float var5 = var3 - this.czField;
            float var6 = (float)Math.sqrt(var4 * var4 + var5 * var5) - this.majorField;
            float var7 = var2 - this.cyField;
            return (float)Math.sqrt(var6 * var6 + var7 * var7) - this.minorField;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class PlaneSdf3D
    extends Sdf3D {
        private final float pxField;
        private final float pyField;
        private final float pzField;
        private final float nxField;
        private final float nyField;
        private final float nzField;

        PlaneSdf3D(float var1, float var2, float var3, float var4, float var5, float var6) {
            float var7 = SdfShapes.length(var4, var5, var6);
            this.pxField = var1;
            this.pyField = var2;
            this.pzField = var3;
            this.nxField = var7 < 1.0E-8f ? 0.0f : var4 / var7;
            this.nyField = var7 < 1.0E-8f ? 1.0f : var5 / var7;
            this.nzField = var7 < 1.0E-8f ? 0.0f : var6 / var7;
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            return (var1 - this.pxField) * this.nxField + (var2 - this.pyField) * this.nyField + (var3 - this.pzField) * this.nzField;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class ExtrudedSdf3D
    extends Sdf3D {
        private final Sdf2D profileField;
        private final float hField;
        private final float r;

        ExtrudedSdf3D(Sdf2D var1, float var2, float var3) {
            this.profileField = var1;
            this.r = Math.max(0.0f, Math.min(var3, var2));
            this.hField = var2;
            this.bounds(var1.minX, var1.minY, -var2, var1.maxX, var1.maxY, var2);
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            float var4 = this.profileField.distance(var1, var2) + this.r;
            float var5 = Math.abs(var3) - this.hField + this.r;
            float var6 = Math.max(var4, 0.0f);
            float var7 = Math.max(var5, 0.0f);
            return Math.min(Math.max(var4, var5), 0.0f) + (float)Math.sqrt(var6 * var6 + var7 * var7) - this.r;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class RevolvedSdf3D
    extends Sdf3D {
        private final Sdf2D profileField;

        RevolvedSdf3D(Sdf2D var1) {
            this.profileField = var1;
            float var2 = Math.max(Math.abs(var1.minX), Math.abs(var1.maxX));
            this.bounds(-var2, var1.minY, -var2, var2, var1.maxY, var2);
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            return this.profileField.distance((float)Math.sqrt(var1 * var1 + var3 * var3), var2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class FunctionSdf3D
    extends Sdf3D {
        private final DistanceField3D fieldField;

        FunctionSdf3D(DistanceField3D var1, float var2, float var3, float var4, float var5, float var6, float var7) {
            this.fieldField = var1;
            this.bounds(var2, var3, var4, var5, var6, var7);
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            return this.fieldField.distance(var1, var2, var3);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface DistanceField3D {
        public float distance(float var1, float var2, float var3);
    }

    @Environment(value=EnvType.CLIENT)
    static final class UnionSdf3D
    extends Sdf3D {
        private final Sdf3D[] kidsField;
        private final float kField;

        UnionSdf3D(float var1, Sdf3D[] var2) {
            if (var2.length == 0) {
                throw new IllegalArgumentException("union needs at least one shape");
            }
            this.kField = Math.max(0.0f, var1);
            this.kidsField = (Sdf3D[])var2.clone();
            boolean var3 = false;
            float var4 = 100000.0f;
            float var5 = 100000.0f;
            float var6 = 100000.0f;
            float var7 = -100000.0f;
            float var8 = -100000.0f;
            float var9 = -100000.0f;
            for (Sdf3D var13 : this.kidsField) {
                if (!var13.bounded()) {
                    var3 = true;
                    continue;
                }
                var4 = Math.min(var4, var13.minX);
                var5 = Math.min(var5, var13.minY);
                var6 = Math.min(var6, var13.minZ);
                var7 = Math.max(var7, var13.maxX);
                var8 = Math.max(var8, var13.maxY);
                var9 = Math.max(var9, var13.maxZ);
            }
            if (!var3) {
                float var14 = this.kField * 0.3f;
                this.bounds(var4 - var14, var5 - var14, var6 - var14, var7 + var14, var8 + var14, var9 + var14);
            }
        }

        @Override
        Sdf3D map(UnaryOperator<Sdf3D> var1) {
            Sdf3D[] var2 = new Sdf3D[this.kidsField.length];
            boolean var3 = false;
            for (int var4 = 0; var4 < this.kidsField.length; ++var4) {
                var2[var4] = this.kidsField[var4].map(var1);
                var3 |= var2[var4] != this.kidsField[var4];
            }
            return (Sdf3D)var1.apply(var3 ? new UnionSdf3D(this.kField, var2).inherit(this) : this);
        }

        @Override
        Sdf3D prune(float var1, float var2, float var3, float var4, float var5, float var6) {
            float var7 = (var1 + var4) * 0.5f;
            float var8 = (var2 + var5) * 0.5f;
            float var9 = (var3 + var6) * 0.5f;
            float var10 = SdfShapes.length(var4 - var1, var5 - var2, var6 - var3) * 0.5f * 1.2f;
            float var11 = Float.MAX_VALUE;
            for (Sdf3D var15 : this.kidsField) {
                if (var15.boxGap(var1, var2, var3, var4, var5, var6) >= var11 + this.kField) continue;
                var11 = Math.min(var11, var15.distance(var7, var8, var9) + var10);
            }
            Sdf3D[] var12 = new Sdf3D[this.kidsField.length];
            int var13 = 0;
            boolean var22 = false;
            for (Sdf3D var18 : this.kidsField) {
                if (var18.boxGap(var1, var2, var3, var4, var5, var6) >= var11 + this.kField) {
                    var22 = true;
                    continue;
                }
                Sdf3D var19 = var18.prune(var1, var2, var3, var4, var5, var6);
                var22 |= var19 != var18;
                var12[var13++] = var19;
            }
            if (var22 && var13 != 0) {
                return var13 == 1 ? var12[0] : new UnionSdf3D(this.kField, Arrays.copyOf(var12, var13));
            }
            return this;
        }

        private int nearest(float var1, float var2, float var3, float[] var4) {
            int var5 = 0;
            float var6 = Float.MAX_VALUE;
            for (int var7 = 0; var7 < this.kidsField.length; ++var7) {
                float var8 = SdfShapes.boxLowerBound(this.kidsField[var7], var1, var2, var3);
                if (var4 != null) {
                    var4[var7] = var8;
                }
                if (!(var8 < var6)) continue;
                var6 = var8;
                var5 = var7;
            }
            return var5;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public float distance(float var1, float var2, float var3) {
            float var7;
            Sdf3D[] var4 = this.kidsField;
            if (var4.length <= 4) {
                return this.distance(var1, var2, var3, null);
            }
            FloatArrayPool var5 = SCRATCH.get();
            float[] var6 = var5.take(var4.length);
            boolean var10 = false;
            try {
                var10 = true;
                var7 = this.distance(var1, var2, var3, var6);
                var10 = false;
            }
            finally {
                if (var10) {
                    --var5.depth;
                }
            }
            --var5.depth;
            return var7;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        float distance(float var1, float var2, float var3, float var4) {
            float var8;
            Sdf3D[] var5 = this.kidsField;
            if (var5.length <= 4) {
                return this.evaluate(var1, var2, var3, null, var4);
            }
            FloatArrayPool var6 = SCRATCH.get();
            float[] var7 = var6.take(var5.length);
            boolean var11 = false;
            try {
                var11 = true;
                var8 = this.evaluate(var1, var2, var3, var7, var4);
                var11 = false;
            }
            finally {
                if (var11) {
                    --var6.depth;
                }
            }
            --var6.depth;
            return var8;
        }

        private float distance(float var1, float var2, float var3, float[] var4) {
            return this.evaluate(var1, var2, var3, var4, Float.MAX_VALUE);
        }

        private float evaluate(float var1, float var2, float var3, float[] var4, float var5) {
            float var8;
            Sdf3D[] var6 = this.kidsField;
            int var7 = this.nearest(var1, var2, var3, var4);
            float f = var8 = var4 != null ? var4[var7] : SdfShapes.boxLowerBound(var6[var7], var1, var2, var3);
            if (var8 >= var5) {
                return var8;
            }
            float var9 = this.kField;
            float var10 = var6[var7].distance(var1, var2, var3, var5 == Float.MAX_VALUE ? Float.MAX_VALUE : var5 + var9);
            for (int var11 = 0; var11 < var6.length; ++var11) {
                float var12;
                if (var11 == var7) continue;
                float f2 = var12 = var4 != null ? var4[var11] : SdfShapes.boxLowerBound(var6[var11], var1, var2, var3);
                if (var12 >= var10 + var9) continue;
                float var13 = var6[var11].distance(var1, var2, var3, var10 + var9);
                if (var9 > 0.0f) {
                    float var14 = SdfShapes.clamp01(0.5f + 0.5f * (var13 - var10) / var9);
                    var10 = var13 + (var10 - var13) * var14 - var9 * var14 * (1.0f - var14);
                    continue;
                }
                if (!(var13 < var10)) continue;
                var10 = var13;
            }
            return var10;
        }

        @Override
        float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            Sdf3D[] var5 = this.kidsField;
            int var6 = this.nearest(var1, var2, var3, null);
            float var7 = var5[var6].sample(var1, var2, var3, var4);
            SurfaceMaterialSample var8 = null;
            float var9 = this.kField;
            for (int var10 = 0; var10 < var5.length; ++var10) {
                Sdf3D var11;
                if (var10 == var6 || SdfShapes.boxLowerBound(var11 = var5[var10], var1, var2, var3) >= var7 + var9) continue;
                if (var8 == null) {
                    var8 = new SurfaceMaterialSample();
                }
                float var12 = var11.sample(var1, var2, var3, var8);
                if (var9 > 0.0f) {
                    float var13 = SdfShapes.clamp01(0.5f + 0.5f * (var12 - var7) / var9);
                    var7 = var12 + (var7 - var12) * var13 - var9 * var13 * (1.0f - var13);
                    var4.mix(var8, 1.0f - var13);
                    continue;
                }
                if (!(var12 < var7)) continue;
                var7 = var12;
                var4.set(var8);
            }
            return var7;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class DifferenceSdf3D
    extends Sdf3D {
        private final Sdf3D baseField;
        private final Sdf3D cutterField;
        private final float kField;

        DifferenceSdf3D(float var1, Sdf3D var2, Sdf3D var3) {
            this.kField = Math.max(0.0f, var1);
            this.baseField = var2;
            this.cutterField = var3;
            this.boundsOf(var2, this.kField * 0.3f);
        }

        @Override
        Sdf3D prune(float var1, float var2, float var3, float var4, float var5, float var6) {
            Sdf3D var7 = this.baseField.prune(var1, var2, var3, var4, var5, var6);
            Sdf3D var8 = this.cutterField.prune(var1, var2, var3, var4, var5, var6);
            return var7 == this.baseField && var8 == this.cutterField ? this : new DifferenceSdf3D(this.kField, var7, var8);
        }

        @Override
        Sdf3D map(UnaryOperator<Sdf3D> var1) {
            Sdf3D var2 = this.baseField.map(var1);
            Sdf3D var3 = this.cutterField.map(var1);
            return (Sdf3D)var1.apply(var2 == this.baseField && var3 == this.cutterField ? this : new DifferenceSdf3D(this.kField, var2, var3).inherit(this));
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            float var4 = this.baseField.distance(var1, var2, var3);
            if (SdfShapes.boxLowerBound(this.cutterField, var1, var2, var3) >= this.kField - var4) {
                return var4;
            }
            float var5 = this.cutterField.distance(var1, var2, var3);
            if (this.kField <= 0.0f) {
                return Math.max(var4, -var5);
            }
            float var6 = SdfShapes.clamp01(0.5f - 0.5f * (var4 + var5) / this.kField);
            return var4 + (-var5 - var4) * var6 + this.kField * var6 * (1.0f - var6);
        }

        @Override
        float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            float var5 = this.baseField.sample(var1, var2, var3, var4);
            if (SdfShapes.boxLowerBound(this.cutterField, var1, var2, var3) >= this.kField - var5) {
                return var5;
            }
            if (this.cutterField.paint == null) {
                float var9 = this.cutterField.distance(var1, var2, var3);
                if (this.kField <= 0.0f) {
                    return Math.max(var5, -var9);
                }
                float var10 = SdfShapes.clamp01(0.5f - 0.5f * (var5 + var9) / this.kField);
                return var5 + (-var9 - var5) * var10 + this.kField * var10 * (1.0f - var10);
            }
            SurfaceMaterialSample var6 = new SurfaceMaterialSample();
            float var7 = this.cutterField.sample(var1, var2, var3, var6);
            if (this.kField <= 0.0f) {
                if (-var7 > var5) {
                    var4.set(var6);
                    return -var7;
                }
                return var5;
            }
            float var8 = SdfShapes.clamp01(0.5f - 0.5f * (var5 + var7) / this.kField);
            var4.mix(var6, var8);
            return var5 + (-var7 - var5) * var8 + this.kField * var8 * (1.0f - var8);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class IntersectionSdf3D
    extends Sdf3D {
        private final Sdf3D aField;
        private final Sdf3D b;
        private final float kField;

        IntersectionSdf3D(float var1, Sdf3D var2, Sdf3D var3) {
            this.kField = Math.max(0.0f, var1);
            this.aField = var2;
            this.b = var3;
            if (var2.bounded() && var3.bounded()) {
                this.bounds(Math.max(var2.minX, var3.minX), Math.max(var2.minY, var3.minY), Math.max(var2.minZ, var3.minZ), Math.min(var2.maxX, var3.maxX), Math.min(var2.maxY, var3.maxY), Math.min(var2.maxZ, var3.maxZ));
            } else if (var2.bounded()) {
                this.boundsOf(var2, 0.0f);
            } else if (var3.bounded()) {
                this.boundsOf(var3, 0.0f);
            }
        }

        @Override
        Sdf3D prune(float var1, float var2, float var3, float var4, float var5, float var6) {
            Sdf3D var7 = this.aField.prune(var1, var2, var3, var4, var5, var6);
            Sdf3D var8 = this.b.prune(var1, var2, var3, var4, var5, var6);
            return var7 == this.aField && var8 == this.b ? this : new IntersectionSdf3D(this.kField, var7, var8);
        }

        @Override
        Sdf3D map(UnaryOperator<Sdf3D> var1) {
            Sdf3D var2 = this.aField.map(var1);
            Sdf3D var3 = this.b.map(var1);
            return (Sdf3D)var1.apply(var2 == this.aField && var3 == this.b ? this : new IntersectionSdf3D(this.kField, var2, var3).inherit(this));
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            float var4 = this.aField.distance(var1, var2, var3);
            float var5 = this.b.distance(var1, var2, var3);
            if (this.kField <= 0.0f) {
                return Math.max(var4, var5);
            }
            float var6 = SdfShapes.clamp01(0.5f - 0.5f * (var5 - var4) / this.kField);
            return var5 + (var4 - var5) * var6 + this.kField * var6 * (1.0f - var6);
        }

        @Override
        float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            float var5 = this.aField.sample(var1, var2, var3, var4);
            if (this.b.paint == null) {
                float var9 = this.b.distance(var1, var2, var3);
                if (this.kField <= 0.0f) {
                    return Math.max(var5, var9);
                }
                float var10 = SdfShapes.clamp01(0.5f - 0.5f * (var9 - var5) / this.kField);
                return var9 + (var5 - var9) * var10 + this.kField * var10 * (1.0f - var10);
            }
            SurfaceMaterialSample var6 = new SurfaceMaterialSample();
            float var7 = this.b.sample(var1, var2, var3, var6);
            if (this.kField <= 0.0f) {
                if (var7 > var5) {
                    var4.set(var6);
                    return var7;
                }
                return var5;
            }
            float var8 = SdfShapes.clamp01(0.5f - 0.5f * (var7 - var5) / this.kField);
            var4.mix(var6, 1.0f - var8);
            return var7 + (var5 - var7) * var8 + this.kField * var8 * (1.0f - var8);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class TwistedSdf3D
    extends Sdf3D {
        private final Sdf3D childField;
        private final float kField;
        private final float cxField;
        private final float czField;

        TwistedSdf3D(Sdf3D var1, float var2, float var3, float var4) {
            this.childField = var1;
            this.kField = (float)Math.toRadians(var2);
            this.cxField = var3;
            this.czField = var4;
            if (var1.bounded()) {
                float var5 = 0.0f;
                for (int var6 = 0; var6 < 4; ++var6) {
                    float var7 = ((var6 & 1) == 0 ? var1.minX : var1.maxX) - var3;
                    float var8 = ((var6 & 2) == 0 ? var1.minZ : var1.maxZ) - var4;
                    var5 = Math.max(var5, (float)Math.sqrt(var7 * var7 + var8 * var8));
                }
                this.bounds(var3 - var5, var1.minY, var4 - var5, var3 + var5, var1.maxY, var4 + var5);
            }
        }

        @Override
        Sdf3D map(UnaryOperator<Sdf3D> var1) {
            Sdf3D var2 = this.childField.map(var1);
            return (Sdf3D)var1.apply(var2 == this.childField ? this : new TwistedSdf3D(var2, (float)Math.toDegrees(this.kField), this.cxField, this.czField).inherit(this));
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            float var4 = -this.kField * var2;
            float var5 = (float)Math.cos(var4);
            float var6 = (float)Math.sin(var4);
            float var7 = var1 - this.cxField;
            float var8 = var3 - this.czField;
            return this.childField.distance(var5 * var7 + var6 * var8 + this.cxField, var2, -var6 * var7 + var5 * var8 + this.czField);
        }

        @Override
        float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            float var5 = -this.kField * var2;
            float var6 = (float)Math.cos(var5);
            float var7 = (float)Math.sin(var5);
            float var8 = var1 - this.cxField;
            float var9 = var3 - this.czField;
            return this.childField.sample(var6 * var8 + var7 * var9 + this.cxField, var2, -var7 * var8 + var6 * var9 + this.czField, var4);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class TranslatedSdf3D
    extends Sdf3D {
        private final Sdf3D childField;
        private final float dxField;
        private final float dyField;
        private final float dzField;

        TranslatedSdf3D(Sdf3D var1, float var2, float var3, float var4) {
            this.childField = var1;
            this.dxField = var2;
            this.dyField = var3;
            this.dzField = var4;
            if (var1.bounded()) {
                this.bounds(var1.minX + var2, var1.minY + var3, var1.minZ + var4, var1.maxX + var2, var1.maxY + var3, var1.maxZ + var4);
            }
        }

        @Override
        Sdf3D prune(float var1, float var2, float var3, float var4, float var5, float var6) {
            Sdf3D var7 = this.childField.prune(var1 - this.dxField, var2 - this.dyField, var3 - this.dzField, var4 - this.dxField, var5 - this.dyField, var6 - this.dzField);
            return var7 == this.childField ? this : new TranslatedSdf3D(var7, this.dxField, this.dyField, this.dzField);
        }

        @Override
        Sdf3D map(UnaryOperator<Sdf3D> var1) {
            Sdf3D var2 = this.childField.map(var1);
            return (Sdf3D)var1.apply(var2 == this.childField ? this : new TranslatedSdf3D(var2, this.dxField, this.dyField, this.dzField).inherit(this));
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            return this.childField.distance(var1 - this.dxField, var2 - this.dyField, var3 - this.dzField);
        }

        @Override
        float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            return this.childField.sample(var1 - this.dxField, var2 - this.dyField, var3 - this.dzField, var4);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class ShellSdf3D
    extends Sdf3D {
        private final Sdf3D childField;
        private final float halfField;

        ShellSdf3D(Sdf3D var1, float var2) {
            this.childField = var1;
            this.halfField = Math.abs(var2) * 0.5f;
            this.boundsOf(var1, this.halfField);
        }

        @Override
        Sdf3D prune(float var1, float var2, float var3, float var4, float var5, float var6) {
            Sdf3D var7 = this.childField.prune(var1, var2, var3, var4, var5, var6);
            return var7 == this.childField ? this : new ShellSdf3D(var7, this.halfField * 2.0f);
        }

        @Override
        Sdf3D map(UnaryOperator<Sdf3D> var1) {
            Sdf3D var2 = this.childField.map(var1);
            return (Sdf3D)var1.apply(var2 == this.childField ? this : new ShellSdf3D(var2, this.halfField * 2.0f).inherit(this));
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            return Math.abs(this.childField.distance(var1, var2, var3)) - this.halfField;
        }

        @Override
        float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            return Math.abs(this.childField.sample(var1, var2, var3, var4)) - this.halfField;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class FloatArrayPool {
        final float[][] pool = new float[64][];
        int depth;

        FloatArrayPool() {
        }

        float[] take(int var1) {
            int var10000;
            int var2;
            if ((var2 = (var10000 = this.depth++)) >= this.pool.length) {
                return new float[var1];
            }
            float[] var3 = this.pool[var2];
            if (var3 == null || var3.length < var1) {
                var3 = new float[Math.max(var1, 16)];
                this.pool[var2] = var3;
            }
            return var3;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class ScaledSdf3D
    extends Sdf3D {
        private final Sdf3D childField;
        private final float kField;
        private final float ikField;
        private final float cxField;
        private final float cyField;
        private final float czField;

        ScaledSdf3D(Sdf3D var1, float var2, float var3, float var4, float var5) {
            this.childField = var1;
            this.kField = var2;
            this.ikField = 1.0f / var2;
            this.cxField = var3;
            this.cyField = var4;
            this.czField = var5;
            if (var1.bounded()) {
                this.bounds((var1.minX - var3) * var2 + var3, (var1.minY - var4) * var2 + var4, (var1.minZ - var5) * var2 + var5, (var1.maxX - var3) * var2 + var3, (var1.maxY - var4) * var2 + var4, (var1.maxZ - var5) * var2 + var5);
            }
        }

        @Override
        Sdf3D prune(float var1, float var2, float var3, float var4, float var5, float var6) {
            float var7 = (var1 - this.cxField) * this.ikField + this.cxField;
            float var8 = (var4 - this.cxField) * this.ikField + this.cxField;
            float var9 = (var2 - this.cyField) * this.ikField + this.cyField;
            float var10 = (var5 - this.cyField) * this.ikField + this.cyField;
            float var11 = (var3 - this.czField) * this.ikField + this.czField;
            float var12 = (var6 - this.czField) * this.ikField + this.czField;
            Sdf3D var13 = this.childField.prune(Math.min(var7, var8), Math.min(var9, var10), Math.min(var11, var12), Math.max(var7, var8), Math.max(var9, var10), Math.max(var11, var12));
            return var13 == this.childField ? this : new ScaledSdf3D(var13, this.kField, this.cxField, this.cyField, this.czField);
        }

        @Override
        Sdf3D map(UnaryOperator<Sdf3D> var1) {
            Sdf3D var2 = this.childField.map(var1);
            return (Sdf3D)var1.apply(var2 == this.childField ? this : new ScaledSdf3D(var2, this.kField, this.cxField, this.cyField, this.czField).inherit(this));
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            return this.childField.distance((var1 - this.cxField) * this.ikField + this.cxField, (var2 - this.cyField) * this.ikField + this.cyField, (var3 - this.czField) * this.ikField + this.czField) * this.kField;
        }

        @Override
        float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            return this.childField.sample((var1 - this.cxField) * this.ikField + this.cxField, (var2 - this.cyField) * this.ikField + this.cyField, (var3 - this.czField) * this.ikField + this.czField, var4) * this.kField;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class ExpandedSdf3D
    extends Sdf3D {
        private final Sdf3D childField;
        private final float r;

        ExpandedSdf3D(Sdf3D var1, float var2) {
            this.childField = var1;
            this.r = var2;
            this.boundsOf(var1, Math.max(0.0f, var2));
        }

        @Override
        Sdf3D prune(float var1, float var2, float var3, float var4, float var5, float var6) {
            Sdf3D var7 = this.childField.prune(var1, var2, var3, var4, var5, var6);
            return var7 == this.childField ? this : new ExpandedSdf3D(var7, this.r);
        }

        @Override
        Sdf3D map(UnaryOperator<Sdf3D> var1) {
            Sdf3D var2 = this.childField.map(var1);
            return (Sdf3D)var1.apply(var2 == this.childField ? this : new ExpandedSdf3D(var2, this.r).inherit(this));
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            return this.childField.distance(var1, var2, var3) - this.r;
        }

        @Override
        float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            return this.childField.sample(var1, var2, var3, var4) - this.r;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class RoughenedSdf3D
    extends Sdf3D {
        private final Sdf3D childField;
        private final float ampField;
        private final float freqField;

        RoughenedSdf3D(Sdf3D var1, float var2, float var3) {
            this.childField = var1;
            this.ampField = var2;
            this.freqField = 1.0f / Math.max(0.01f, var3);
            this.boundsOf(var1, Math.abs(var2));
        }

        @Override
        Sdf3D prune(float var1, float var2, float var3, float var4, float var5, float var6) {
            Sdf3D var7 = this.childField.prune(var1, var2, var3, var4, var5, var6);
            return var7 == this.childField ? this : new RoughenedSdf3D(var7, this.ampField, 1.0f / this.freqField);
        }

        @Override
        Sdf3D map(UnaryOperator<Sdf3D> var1) {
            Sdf3D var2 = this.childField.map(var1);
            return (Sdf3D)var1.apply(var2 == this.childField ? this : new RoughenedSdf3D(var2, this.ampField, 1.0f / this.freqField).inherit(this));
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            float var4 = this.childField.distance(var1, var2, var3);
            return var4 > this.ampField * 1.5f + 0.5f ? var4 : var4 + this.ampField * PerlinNoise.noise(var1 * this.freqField, var2 * this.freqField, var3 * this.freqField);
        }

        @Override
        float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            return this.childField.sample(var1, var2, var3, var4) + this.ampField * PerlinNoise.noise(var1 * this.freqField, var2 * this.freqField, var3 * this.freqField);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class RotatedSdf3D
    extends Sdf3D {
        private final Sdf3D childField;
        private final RotationMatrix3 invField;
        private final float cxField;
        private final float cyField;
        private final float czField;

        RotatedSdf3D(Sdf3D var1, RotationMatrix3 var2, float var3, float var4, float var5) {
            this.childField = var1;
            this.invField = var2.transpose();
            this.cxField = var3;
            this.cyField = var4;
            this.czField = var5;
            if (var1.bounded()) {
                float var6 = 100000.0f;
                float var7 = 100000.0f;
                float var8 = 100000.0f;
                float var9 = -100000.0f;
                float var10 = -100000.0f;
                float var11 = -100000.0f;
                for (int var12 = 0; var12 < 8; ++var12) {
                    float var13 = ((var12 & 1) == 0 ? var1.minX : var1.maxX) - var3;
                    float var14 = ((var12 & 2) == 0 ? var1.minY : var1.maxY) - var4;
                    float var15 = ((var12 & 4) == 0 ? var1.minZ : var1.maxZ) - var5;
                    float var16 = var2.x(var13, var14, var15) + var3;
                    float var17 = var2.y(var13, var14, var15) + var4;
                    float var18 = var2.z(var13, var14, var15) + var5;
                    var6 = Math.min(var6, var16);
                    var7 = Math.min(var7, var17);
                    var8 = Math.min(var8, var18);
                    var9 = Math.max(var9, var16);
                    var10 = Math.max(var10, var17);
                    var11 = Math.max(var11, var18);
                }
                this.bounds(var6, var7, var8, var9, var10, var11);
            }
        }

        @Override
        Sdf3D prune(float var1, float var2, float var3, float var4, float var5, float var6) {
            float var7 = 100000.0f;
            float var8 = 100000.0f;
            float var9 = 100000.0f;
            float var10 = -100000.0f;
            float var11 = -100000.0f;
            float var12 = -100000.0f;
            for (int var13 = 0; var13 < 8; ++var13) {
                float var14 = ((var13 & 1) == 0 ? var1 : var4) - this.cxField;
                float var15 = ((var13 & 2) == 0 ? var2 : var5) - this.cyField;
                float var16 = ((var13 & 4) == 0 ? var3 : var6) - this.czField;
                float var17 = this.invField.x(var14, var15, var16) + this.cxField;
                float var18 = this.invField.y(var14, var15, var16) + this.cyField;
                float var19 = this.invField.z(var14, var15, var16) + this.czField;
                var7 = Math.min(var7, var17);
                var8 = Math.min(var8, var18);
                var9 = Math.min(var9, var19);
                var10 = Math.max(var10, var17);
                var11 = Math.max(var11, var18);
                var12 = Math.max(var12, var19);
            }
            Sdf3D var20 = this.childField.prune(var7, var8, var9, var10, var11, var12);
            return var20 == this.childField ? this : new RotatedSdf3D(var20, this.invField.transpose(), this.cxField, this.cyField, this.czField);
        }

        @Override
        Sdf3D map(UnaryOperator<Sdf3D> var1) {
            Sdf3D var2 = this.childField.map(var1);
            return (Sdf3D)var1.apply(var2 == this.childField ? this : new RotatedSdf3D(var2, this.invField.transpose(), this.cxField, this.cyField, this.czField).inherit(this));
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            float var4 = var1 - this.cxField;
            float var5 = var2 - this.cyField;
            float var6 = var3 - this.czField;
            return this.childField.distance(this.invField.x(var4, var5, var6) + this.cxField, this.invField.y(var4, var5, var6) + this.cyField, this.invField.z(var4, var5, var6) + this.czField);
        }

        @Override
        float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            float var5 = var1 - this.cxField;
            float var6 = var2 - this.cyField;
            float var7 = var3 - this.czField;
            return this.childField.sample(this.invField.x(var5, var6, var7) + this.cxField, this.invField.y(var5, var6, var7) + this.cyField, this.invField.z(var5, var6, var7) + this.czField, var4);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class MirroredSdf3D
    extends Sdf3D {
        private final Sdf3D childField;
        private final boolean keepField;

        MirroredSdf3D(Sdf3D var1, boolean var2) {
            this.childField = var1;
            this.keepField = var2;
            if (var1.bounded()) {
                if (var2) {
                    this.bounds(Math.min(var1.minX, -var1.maxX), var1.minY, var1.minZ, Math.max(var1.maxX, -var1.minX), var1.maxY, var1.maxZ);
                } else {
                    this.bounds(-var1.maxX, var1.minY, var1.minZ, -var1.minX, var1.maxY, var1.maxZ);
                }
            }
        }

        @Override
        Sdf3D prune(float var1, float var2, float var3, float var4, float var5, float var6) {
            Sdf3D var7 = this.keepField ? this.childField.prune(Math.min(var1, -var4), var2, var3, Math.max(var4, -var1), var5, var6) : this.childField.prune(-var4, var2, var3, -var1, var5, var6);
            return var7 == this.childField ? this : new MirroredSdf3D(var7, this.keepField);
        }

        @Override
        Sdf3D map(UnaryOperator<Sdf3D> var1) {
            Sdf3D var2 = this.childField.map(var1);
            return (Sdf3D)var1.apply(var2 == this.childField ? this : new MirroredSdf3D(var2, this.keepField).inherit(this));
        }

        @Override
        public float distance(float var1, float var2, float var3) {
            if (!this.keepField) {
                return this.childField.distance(-var1, var2, var3);
            }
            float var4 = this.childField.distance(var1, var2, var3);
            return SdfShapes.boxLowerBound(this.childField, -var1, var2, var3) >= var4 ? var4 : Math.min(var4, this.childField.distance(-var1, var2, var3));
        }

        @Override
        float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
            if (!this.keepField) {
                return this.childField.sample(-var1, var2, var3, var4);
            }
            float var5 = this.childField.distance(var1, var2, var3);
            float var6 = SdfShapes.boxLowerBound(this.childField, -var1, var2, var3) >= var5 ? Float.MAX_VALUE : this.childField.distance(-var1, var2, var3);
            return var6 < var5 ? this.childField.sample(-var1, var2, var3, var4) : this.childField.sample(var1, var2, var3, var4);
        }
    }
}

