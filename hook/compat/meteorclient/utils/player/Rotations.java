/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 */
package hook.compat.meteorclient.utils.player;

import net.minecraft.class_243;
import net.minecraft.class_310;

public final class Rotations {
    private static final class_310 mc = class_310.method_1551();

    private Rotations() {
    }

    public static double getYaw(class_243 target) {
        if (Rotations.mc.field_1724 == null || target == null) {
            return 0.0;
        }
        class_243 eyes = Rotations.mc.field_1724.method_33571();
        return Math.toDegrees(Math.atan2(target.field_1350 - eyes.field_1350, target.field_1352 - eyes.field_1352)) - 90.0;
    }

    public static double getPitch(class_243 target) {
        if (Rotations.mc.field_1724 == null || target == null) {
            return 0.0;
        }
        class_243 eyes = Rotations.mc.field_1724.method_33571();
        double dx = target.field_1352 - eyes.field_1352;
        double dy = target.field_1351 - eyes.field_1351;
        double dz = target.field_1350 - eyes.field_1350;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        return -Math.toDegrees(Math.atan2(dy, horizontal));
    }

    public static void rotate(double yaw, double pitch) {
        if (Rotations.mc.field_1724 == null) {
            return;
        }
        Rotations.mc.field_1724.method_36456((float)yaw);
        Rotations.mc.field_1724.method_36457((float)Math.max(-90.0, Math.min(90.0, pitch)));
    }
}

