/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class RotationMatrix3 {
    public static final RotationMatrix3 IDENTITY = new RotationMatrix3(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f);
    public final float m00;
    public final float m01;
    public final float m02;
    public final float m10;
    public final float m11;
    public final float m12;
    public final float m20;
    public final float m21;
    public final float m22;

    public RotationMatrix3(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
    }

    public static RotationMatrix3 euler(float xDegrees, float yDegrees, float zDegrees) {
        return RotationMatrix3.radians((float)Math.toRadians(xDegrees), (float)Math.toRadians(yDegrees), (float)Math.toRadians(zDegrees));
    }

    public static RotationMatrix3 radians(float xRadians, float yRadians, float zRadians) {
        float cosX = (float)Math.cos(xRadians);
        float sinX = (float)Math.sin(xRadians);
        float cosY = (float)Math.cos(yRadians);
        float sinY = (float)Math.sin(yRadians);
        float cosZ = (float)Math.cos(zRadians);
        float sinZ = (float)Math.sin(zRadians);
        return new RotationMatrix3(cosZ * cosY, cosZ * sinY * sinX - sinZ * cosX, cosZ * sinY * cosX + sinZ * sinX, sinZ * cosY, sinZ * sinY * sinX + cosZ * cosX, sinZ * sinY * cosX - cosZ * sinX, sinY, cosY * sinX, cosY * cosX);
    }

    public static RotationMatrix3 about(float axisX, float axisY, float axisZ, float degrees) {
        float length = (float)Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
        if (length < 1.0E-8f) {
            return IDENTITY;
        }
        float x = axisX / length;
        float y = axisY / length;
        float z = axisZ / length;
        float radians = (float)Math.toRadians(degrees);
        float cosine = (float)Math.cos(radians);
        float sine = (float)Math.sin(radians);
        float complement = 1.0f - cosine;
        return new RotationMatrix3(complement * x * x + cosine, complement * x * y - sine * z, complement * x * z + sine * y, complement * x * y + sine * z, complement * y * y + cosine, complement * y * z - sine * x, complement * x * z - sine * y, complement * y * z + sine * x, complement * z * z + cosine);
    }

    public RotationMatrix3 mul(RotationMatrix3 other) {
        return new RotationMatrix3(this.m00 * other.m00 + this.m01 * other.m10 + this.m02 * other.m20, this.m00 * other.m01 + this.m01 * other.m11 + this.m02 * other.m21, this.m00 * other.m02 + this.m01 * other.m12 + this.m02 * other.m22, this.m10 * other.m00 + this.m11 * other.m10 + this.m12 * other.m20, this.m10 * other.m01 + this.m11 * other.m11 + this.m12 * other.m21, this.m10 * other.m02 + this.m11 * other.m12 + this.m12 * other.m22, this.m20 * other.m00 + this.m21 * other.m10 + this.m22 * other.m20, this.m20 * other.m01 + this.m21 * other.m11 + this.m22 * other.m21, this.m20 * other.m02 + this.m21 * other.m12 + this.m22 * other.m22);
    }

    public RotationMatrix3 transpose() {
        return new RotationMatrix3(this.m00, this.m10, this.m20, this.m01, this.m11, this.m21, this.m02, this.m12, this.m22);
    }

    public Vec3 apply(Vec3 vector) {
        return new Vec3(this.x(vector.x(), vector.y(), vector.z()), this.y(vector.x(), vector.y(), vector.z()), this.z(vector.x(), vector.y(), vector.z()));
    }

    public float x(float x, float y, float z) {
        return this.m00 * x + this.m01 * y + this.m02 * z;
    }

    public float y(float x, float y, float z) {
        return this.m10 * x + this.m11 * y + this.m12 * z;
    }

    public float z(float x, float y, float z) {
        return this.m20 * x + this.m21 * y + this.m22 * z;
    }
}

