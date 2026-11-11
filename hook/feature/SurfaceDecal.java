/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.PixelCanvas;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class SurfaceDecal {
    final PixelCanvas canvas;
    final boolean wrap;
    final float cx;
    final float cy;
    final float cz;
    final float dx;
    final float dy;
    final float dz;
    final float ux;
    final float uy;
    final float uz;
    final float rx;
    final float ry;
    final float rz;
    final float width;
    final float height;
    float near = -3.4028235E38f;
    float far = Float.MAX_VALUE;
    float cosLimit = (float)Math.cos(Math.toRadians(78.0));
    float offset = 0.035f;
    boolean glow;
    float flat = Float.NaN;
    float occlusion = 1.0f;

    private SurfaceDecal(PixelCanvas canvas, boolean wrap, float centerX, float centerY, float centerZ, float directionX, float directionY, float directionZ, float upX, float upY, float upZ, float width, float height) {
        if (canvas == null) {
            throw new IllegalArgumentException("decal needs a canvas");
        }
        if (width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("decal size must be positive");
        }
        this.canvas = canvas;
        this.wrap = wrap;
        this.cx = centerX;
        this.cy = centerY;
        this.cz = centerZ;
        float directionLength = (float)Math.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);
        if (directionLength < 1.0E-6f) {
            throw new IllegalArgumentException("decal direction must not be zero");
        }
        this.dx = directionX / directionLength;
        this.dy = directionY / directionLength;
        this.dz = directionZ / directionLength;
        float alongDirection = upX * this.dx + upY * this.dy + upZ * this.dz;
        float projectedX = upX - alongDirection * this.dx;
        float projectedY = upY - alongDirection * this.dy;
        float projectedZ = upZ - alongDirection * this.dz;
        float upLength = (float)Math.sqrt(projectedX * projectedX + projectedY * projectedY + projectedZ * projectedZ);
        if (upLength < 1.0E-6f) {
            throw new IllegalArgumentException("decal up/front direction must not be parallel to its axis");
        }
        this.ux = projectedX / upLength;
        this.uy = projectedY / upLength;
        this.uz = projectedZ / upLength;
        if (wrap) {
            this.rx = this.dy * this.uz - this.dz * this.uy;
            this.ry = this.dz * this.ux - this.dx * this.uz;
            this.rz = this.dx * this.uy - this.dy * this.ux;
        } else {
            this.rx = this.uy * this.dz - this.uz * this.dy;
            this.ry = this.uz * this.dx - this.ux * this.dz;
            this.rz = this.ux * this.dy - this.uy * this.dx;
        }
        this.width = width;
        this.height = height;
    }

    public static SurfaceDecal front(PixelCanvas canvas, float x, float y, float z, float width, float height) {
        return new SurfaceDecal(canvas, false, x, y, z, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, width, height);
    }

    public static SurfaceDecal back(PixelCanvas canvas, float x, float y, float z, float width, float height) {
        return new SurfaceDecal(canvas, false, x, y, z, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, width, height);
    }

    public static SurfaceDecal left(PixelCanvas canvas, float x, float y, float z, float width, float height) {
        return new SurfaceDecal(canvas, false, x, y, z, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, width, height);
    }

    public static SurfaceDecal right(PixelCanvas canvas, float x, float y, float z, float width, float height) {
        return new SurfaceDecal(canvas, false, x, y, z, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, width, height);
    }

    public static SurfaceDecal top(PixelCanvas canvas, float x, float y, float z, float width, float height) {
        return new SurfaceDecal(canvas, false, x, y, z, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, width, height);
    }

    public static SurfaceDecal project(PixelCanvas canvas, float x, float y, float z, float directionX, float directionY, float directionZ, float upX, float upY, float upZ, float width, float height) {
        return new SurfaceDecal(canvas, false, x, y, z, directionX, directionY, directionZ, upX, upY, upZ, width, height);
    }

    public static SurfaceDecal wrap(PixelCanvas canvas, float x, float y, float z, float directionX, float directionY, float directionZ, float upX, float upY, float upZ, float width, float height) {
        return new SurfaceDecal(canvas, true, x, y, z, directionX, directionY, directionZ, upX, upY, upZ, Math.min(360.0f, width), height);
    }

    public SurfaceDecal depth(float near, float far) {
        this.near = Math.abs(near);
        this.far = Math.abs(far);
        return this;
    }

    public SurfaceDecal radius(float near, float far) {
        this.near = near;
        this.far = far;
        return this;
    }

    public SurfaceDecal maxAngle(float angleDegrees) {
        this.cosLimit = (float)Math.cos(Math.toRadians(Math.max(1.0f, Math.min(89.5f, angleDegrees))));
        return this;
    }

    public SurfaceDecal offset(float value) {
        this.offset = value;
        return this;
    }

    public SurfaceDecal glow() {
        this.glow = true;
        return this;
    }

    public SurfaceDecal flatLight(float value) {
        this.flat = Math.max(0.0f, Math.min(1.0f, value));
        return this;
    }

    public SurfaceDecal occlusion(float value) {
        this.occlusion = Math.max(0.0f, Math.min(1.0f, value));
        return this;
    }

    SurfaceDecal mirrored() {
        SurfaceDecal copy = new SurfaceDecal(this.canvas.mirrored(), this.wrap, this.cx, this.cy, this.cz, this.dx, this.dy, this.dz, this.ux, this.uy, this.uz, this.width, this.height);
        copy.near = this.near;
        copy.far = this.far;
        copy.cosLimit = this.cosLimit;
        copy.offset = this.offset;
        copy.glow = this.glow;
        copy.flat = this.flat;
        copy.occlusion = this.occlusion;
        return copy;
    }
}

