/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.RotationMatrix3;
import hook.feature.SdfShapes;
import hook.feature.SurfaceMaterialSample;
import hook.feature.SurfacePaint;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public abstract class Sdf3D {
    static final float FAR = 100000.0f;
    float minX = -100000.0f;
    float minY = -100000.0f;
    float minZ = -100000.0f;
    float maxX = 100000.0f;
    float maxY = 100000.0f;
    float maxZ = 100000.0f;
    SurfacePaint paint;

    protected Sdf3D() {
    }

    public abstract float distance(float var1, float var2, float var3);

    float distance(float var1, float var2, float var3, float var4) {
        return this.distance(var1, var2, var3);
    }

    float shade(float var1, float var2, float var3, SurfaceMaterialSample var4) {
        float var5 = this.distance(var1, var2, var3);
        SurfacePaint.DEFAULT.apply(var1, var2, var3, var4);
        return var5;
    }

    public final float sample(float var1, float var2, float var3, SurfaceMaterialSample var4) {
        if (this.paint != null) {
            float var5 = this.distance(var1, var2, var3);
            this.paint.apply(var1, var2, var3, var4);
            return var5;
        }
        return this.shade(var1, var2, var3, var4);
    }

    protected final void bounds(float var1, float var2, float var3, float var4, float var5, float var6) {
        this.minX = Math.min(var1, var4);
        this.minY = Math.min(var2, var5);
        this.minZ = Math.min(var3, var6);
        this.maxX = Math.max(var1, var4);
        this.maxY = Math.max(var2, var5);
        this.maxZ = Math.max(var3, var6);
    }

    final void boundsOf(Sdf3D var1, float var2) {
        if (!var1.bounded()) {
            this.minZ = -100000.0f;
            this.minY = -100000.0f;
            this.minX = -100000.0f;
            this.maxZ = 100000.0f;
            this.maxY = 100000.0f;
            this.maxX = 100000.0f;
        } else {
            this.bounds(var1.minX - var2, var1.minY - var2, var1.minZ - var2, var1.maxX + var2, var1.maxY + var2, var1.maxZ + var2);
        }
    }

    public final boolean bounded() {
        return this.maxX - this.minX < 100000.0f && this.maxY - this.minY < 100000.0f && this.maxZ - this.minZ < 100000.0f;
    }

    public final float minX() {
        return this.minX;
    }

    public final float minY() {
        return this.minY;
    }

    public final float minZ() {
        return this.minZ;
    }

    public final float maxX() {
        return this.maxX;
    }

    public final float maxY() {
        return this.maxY;
    }

    public final float maxZ() {
        return this.maxZ;
    }

    Sdf3D prune(float var1, float var2, float var3, float var4, float var5, float var6) {
        return this;
    }

    Sdf3D map(UnaryOperator<Sdf3D> var1) {
        return (Sdf3D)var1.apply(this);
    }

    final Sdf3D inherit(Sdf3D var1) {
        if (var1.paint != null && this.paint == null) {
            this.paint = var1.paint;
        }
        return this;
    }

    final float boxGap(float var1, float var2, float var3, float var4, float var5, float var6) {
        float var7 = Math.max(0.0f, Math.max(this.minX - var4, var1 - this.maxX));
        float var8 = Math.max(0.0f, Math.max(this.minY - var5, var2 - this.maxY));
        float var9 = Math.max(0.0f, Math.max(this.minZ - var6, var3 - this.maxZ));
        return (float)Math.sqrt(var7 * var7 + var8 * var8 + var9 * var9);
    }

    final float boundDistance(float var1, float var2, float var3) {
        float var4 = var1 < this.minX ? this.minX - var1 : (var1 > this.maxX ? var1 - this.maxX : 0.0f);
        float var5 = var2 < this.minY ? this.minY - var2 : (var2 > this.maxY ? var2 - this.maxY : 0.0f);
        float var6 = var3 < this.minZ ? this.minZ - var3 : (var3 > this.maxZ ? var3 - this.maxZ : 0.0f);
        return (float)Math.sqrt(var4 * var4 + var5 * var5 + var6 * var6);
    }

    public Sdf3D color(int var1) {
        this.paint = SurfacePaint.of(var1);
        return this;
    }

    public Sdf3D paint(SurfacePaint var1) {
        this.paint = var1;
        return this;
    }

    public Sdf3D move(float var1, float var2, float var3) {
        return new SdfShapes.TranslatedSdf3D(this, var1, var2, var3);
    }

    public Sdf3D rotate(float var1, float var2, float var3) {
        return this.rotate(var1, var2, var3, 0.0f, 0.0f, 0.0f);
    }

    public Sdf3D rotate(float var1, float var2, float var3, float var4, float var5, float var6) {
        return new SdfShapes.RotatedSdf3D(this, RotationMatrix3.euler(var1, var2, var3), var4, var5, var6);
    }

    public Sdf3D rotateAxis(float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
        return new SdfShapes.RotatedSdf3D(this, RotationMatrix3.about(var1, var2, var3, var4), var5, var6, var7);
    }

    public Sdf3D scale(float var1) {
        return this.scale(var1, 0.0f, 0.0f, 0.0f);
    }

    public Sdf3D scale(float var1, float var2, float var3, float var4) {
        return new SdfShapes.ScaledSdf3D(this, var1, var2, var3, var4);
    }

    public Sdf3D mirrorX() {
        return new SdfShapes.MirroredSdf3D(this, true);
    }

    public Sdf3D mirrored() {
        return new SdfShapes.MirroredSdf3D(this, false);
    }

    public Sdf3D inflate(float var1) {
        return new SdfShapes.ExpandedSdf3D(this, var1);
    }

    public Sdf3D shell(float var1) {
        return new SdfShapes.ShellSdf3D(this, var1);
    }

    public Sdf3D roughen(float var1, float var2) {
        return new SdfShapes.RoughenedSdf3D(this, var1, var2);
    }

    public Sdf3D twist(float var1, float var2, float var3) {
        return new SdfShapes.TwistedSdf3D(this, var1, var2, var3);
    }

    public Sdf3D union(Sdf3D ... var1) {
        return SdfShapes.union(this.prepend(var1));
    }

    public Sdf3D blend(float var1, Sdf3D ... var2) {
        return SdfShapes.blend(var1, this.prepend(var2));
    }

    public Sdf3D subtract(Sdf3D var1) {
        return SdfShapes.subtract(this, var1);
    }

    public Sdf3D subtract(float var1, Sdf3D var2) {
        return SdfShapes.subtract(var1, this, var2);
    }

    public Sdf3D intersect(Sdf3D var1) {
        return SdfShapes.intersect(this, var1);
    }

    public Sdf3D intersect(float var1, Sdf3D var2) {
        return SdfShapes.intersect(var1, this, var2);
    }

    private Sdf3D[] prepend(Sdf3D[] var1) {
        Sdf3D[] var2 = new Sdf3D[var1.length + 1];
        var2[0] = this;
        System.arraycopy(var1, 0, var2, 1, var1.length);
        return var2;
    }
}

