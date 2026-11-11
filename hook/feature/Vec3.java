/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.RotationMatrix3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public record Vec3(float x, float y, float z) {
    public static final Vec3 ZERO = new Vec3(0.0f, 0.0f, 0.0f);
    public static final Vec3 UP = new Vec3(0.0f, 1.0f, 0.0f);
    public static final Vec3 FORWARD = new Vec3(0.0f, 0.0f, 1.0f);
    public static final Vec3 LEFT = new Vec3(1.0f, 0.0f, 0.0f);

    public static Vec3 of(float var0, float var1, float var2) {
        return new Vec3(var0, var1, var2);
    }

    public Vec3 add(Vec3 var1) {
        return new Vec3(this.x + var1.x, this.y + var1.y, this.z + var1.z);
    }

    public Vec3 add(float var1, float var2, float var3) {
        return new Vec3(this.x + var1, this.y + var2, this.z + var3);
    }

    public Vec3 sub(Vec3 var1) {
        return new Vec3(this.x - var1.x, this.y - var1.y, this.z - var1.z);
    }

    public Vec3 mul(float var1) {
        return new Vec3(this.x * var1, this.y * var1, this.z * var1);
    }

    public float dot(Vec3 var1) {
        return this.x * var1.x + this.y * var1.y + this.z * var1.z;
    }

    public Vec3 cross(Vec3 var1) {
        return new Vec3(this.y * var1.z - this.z * var1.y, this.z * var1.x - this.x * var1.z, this.x * var1.y - this.y * var1.x);
    }

    public float length() {
        return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public float distance(Vec3 var1) {
        return this.sub(var1).length();
    }

    public Vec3 normalize() {
        float var1 = this.length();
        return var1 < 1.0E-8f ? ZERO : this.mul(1.0f / var1);
    }

    public Vec3 lerp(Vec3 var1, float var2) {
        return new Vec3(this.x + (var1.x - this.x) * var2, this.y + (var1.y - this.y) * var2, this.z + (var1.z - this.z) * var2);
    }

    public Vec3 mirrorX() {
        return new Vec3(-this.x, this.y, this.z);
    }

    public Vec3 rotate(float var1, float var2, float var3) {
        return RotationMatrix3.euler(var1, var2, var3).apply(this);
    }

    public Vec3 rotate(float var1, float var2, float var3, Vec3 var4) {
        return RotationMatrix3.euler(var1, var2, var3).apply(this.sub(var4)).add(var4);
    }
}

