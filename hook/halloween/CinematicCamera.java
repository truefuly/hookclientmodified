/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 */
package hook.halloween;

import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;

public final class CinematicCamera {
    private static boolean active;
    private static boolean inputLocked;
    private static float blend;
    private static boolean releasing;
    private static float yaw;
    private static float pitch;
    private static float yawVel;
    private static float pitchVel;
    private static class_243 target;
    private static float stiffness;
    private static float aimBias;
    private static float fov;
    private static float fovVel;
    private static float fovTarget;

    private CinematicCamera() {
    }

    public static void begin() {
        class_310 var0 = class_310.method_1551();
        class_746 var1 = var0.field_1724;
        if (var1 != null) {
            if (!active) {
                yaw = var1.method_36454();
                pitch = var1.method_36455();
                yawVel = 0.0f;
                pitchVel = 0.0f;
                fov = 1.0f;
                fovVel = 0.0f;
                blend = 0.0f;
            }
            active = true;
            releasing = false;
            inputLocked = true;
            fovTarget = 1.0f;
        }
    }

    public static void follow(class_243 var0, float var1, float var2, float var3) {
        target = var0;
        stiffness = var1;
        fovTarget = var2;
        aimBias = var3;
    }

    public static void release() {
        if (active) {
            releasing = true;
            fovTarget = 1.0f;
        }
    }

    public static void reset() {
        active = false;
        releasing = false;
        inputLocked = false;
        blend = 0.0f;
        fov = 1.0f;
        fovTarget = 1.0f;
        target = null;
    }

    public static boolean active() {
        return active;
    }

    public static boolean inputLocked() {
        return inputLocked;
    }

    private static float wrap(float var0) {
        if ((var0 %= 360.0f) >= 180.0f) {
            var0 -= 360.0f;
        }
        if (var0 < -180.0f) {
            var0 += 360.0f;
        }
        return var0;
    }

    public static void update(float var0) {
        if (active) {
            class_310 var1 = class_310.method_1551();
            class_746 var2 = var1.field_1724;
            if (var2 == null || var1.field_1687 == null) {
                CinematicCamera.reset();
            } else if (!(var0 <= 0.0f)) {
                float var3 = releasing ? 0.0f : 1.0f;
                float var4 = var0 / (releasing ? 1.15f : 0.55f);
                float f = blend = blend < var3 ? Math.min(var3, blend + var4) : Math.max(var3, blend - var4);
                if (releasing && blend <= 0.0f) {
                    active = false;
                    releasing = false;
                    inputLocked = false;
                    fov = 1.0f;
                    target = null;
                } else {
                    if (target != null && !releasing) {
                        class_243 var5 = var2.method_5836(1.0f);
                        class_243 var6 = target.method_1020(var5);
                        double var7 = Math.sqrt(var6.field_1352 * var6.field_1352 + var6.field_1350 * var6.field_1350);
                        float var9 = (float)Math.toDegrees(Math.atan2(-var6.field_1352, var6.field_1350));
                        float var10 = (float)(-Math.toDegrees(Math.atan2(var6.field_1351, Math.max(0.01, var7))));
                        var10 += aimBias;
                        var10 = Math.max(-88.0f, Math.min(88.0f, var10));
                        float var11 = stiffness;
                        float var12 = 2.0f * (float)Math.sqrt(var11);
                        int var13 = Math.max(1, (int)Math.ceil(var0 / 0.008f));
                        float var14 = var0 / (float)var13;
                        for (int var15 = 0; var15 < var13; ++var15) {
                            float var16 = CinematicCamera.wrap(var9 - yaw);
                            yawVel += (var16 * var11 - yawVel * var12) * var14;
                            yaw += yawVel * var14;
                            pitchVel += ((var10 - pitch) * var11 - pitchVel * var12) * var14;
                            pitch += pitchVel * var14;
                        }
                    }
                    float var17 = 3.2f;
                    float var18 = 2.0f * (float)Math.sqrt(var17) * 1.05f;
                    fovVel += ((fovTarget - fov) * var17 - fovVel * var18) * var0;
                    fov += fovVel * var0;
                }
            }
        }
    }

    private static float weight() {
        float var0 = Math.max(0.0f, Math.min(1.0f, blend));
        return var0 * var0 * (3.0f - 2.0f * var0);
    }

    public static float yaw(float var0) {
        return active ? var0 + CinematicCamera.wrap(yaw - var0) * CinematicCamera.weight() : var0;
    }

    public static float pitch(float var0) {
        return active ? var0 + (pitch - var0) * CinematicCamera.weight() : var0;
    }

    public static float fov(float var0) {
        return active ? var0 * (1.0f + (fov - 1.0f) * CinematicCamera.weight()) : var0;
    }

    static {
        stiffness = 8.0f;
        fov = 1.0f;
        fovTarget = 1.0f;
    }
}

