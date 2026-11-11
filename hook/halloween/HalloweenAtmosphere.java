/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1937
 *  net.minecraft.class_1944
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2374
 *  net.minecraft.class_243
 *  net.minecraft.class_2874$class_12326
 *  net.minecraft.class_2902$class_2903
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_638
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package hook.halloween;

import hook.feature.HalloweenModule;
import net.minecraft.class_1937;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_243;
import net.minecraft.class_2874;
import net.minecraft.class_2902;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class HalloweenAtmosphere {
    private static final double NIGHT = 15200.0;
    private static final int NONE = 0;
    private static final int TO_NIGHT = 1;
    private static final int HOLD = 2;
    private static final int TO_REAL = 3;
    private static boolean wanted;
    private static float raw;
    private static int mode;
    private static double from;
    private static double delta;
    private static double progress;
    private static double duration;
    private static double vis;
    private static float outdoor;
    private static float under;
    private static boolean underValid;
    private static float lit;
    private static final class_2338.class_2339 PROBE;
    private static final int[] PROBES;
    private static final int[] COLUMNS;
    private static float moonAngle;
    private static boolean moonValid;
    private static class_1937 world;

    private HalloweenAtmosphere() {
    }

    public static void setWanted(boolean var0) {
        wanted = var0;
        if (!(var0 || mode != 1 && mode != 2)) {
            HalloweenAtmosphere.beginToReal();
        }
    }

    public static float strength() {
        float var0 = Math.max(0.0f, Math.min(1.0f, raw));
        return var0 * var0 * (3.0f - 2.0f * var0);
    }

    public static float darkness() {
        return HalloweenAtmosphere.strength() * (float)(HalloweenModule.darkness.getValue() / 100.0) * (0.35f + 0.65f * HalloweenAtmosphere.nightness());
    }

    public static float nightness() {
        double var0;
        var0 = mode != 0 ? HalloweenAtmosphere.mod(vis) : (world != null ? HalloweenAtmosphere.mod(world.method_8401().method_217()) : 18000.0);
        if (var0 >= 12000.0 && var0 < 13800.0) {
            return (float)HalloweenAtmosphere.smooth((var0 - 12000.0) / 1800.0);
        }
        if (var0 >= 13800.0 && var0 < 22200.0) {
            return 1.0f;
        }
        return var0 >= 22200.0 ? (float)HalloweenAtmosphere.smooth((24000.0 - var0) / 1800.0) : 0.0f;
    }

    private static double mod(double var0) {
        double var2 = var0 % 24000.0;
        return var2 < 0.0 ? var2 + 24000.0 : var2;
    }

    private static double smooth(double var0) {
        var0 = Math.max(0.0, Math.min(1.0, var0));
        return var0 * var0 * var0 * (var0 * (var0 * 6.0 - 15.0) + 10.0);
    }

    private static boolean skyWorld(class_638 var0) {
        try {
            return var0.method_8597().comp_5220() == class_2874.class_12326.field_64386 && var0.method_8597().comp_642();
        }
        catch (Throwable var2) {
            return false;
        }
    }

    public static void frame(float var0, class_310 var1) {
        class_638 var2 = var1.field_1687;
        if (var2 != world) {
            world = var2;
            mode = 0;
            raw = 0.0f;
            outdoor = 1.0f;
            under = 0.0f;
            underValid = false;
            lit = 0.0f;
            moonValid = false;
        }
        if (var2 != null) {
            float var3 = wanted ? 1.0f : 0.0f;
            float var4 = var0 / 3.2f;
            raw = raw < var3 ? Math.min(var3, raw + var4) : Math.max(var3, raw - var4);
            boolean var5 = HalloweenAtmosphere.skyWorld(var2);
            double var6 = var2.method_28104().method_217();
            if (wanted && var5 && (mode == 0 || mode == 3)) {
                HalloweenAtmosphere.beginToNight(var6);
            }
            if (!var5 && mode != 0) {
                mode = 0;
            }
            switch (mode) {
                case 1: {
                    vis = from + delta * HalloweenAtmosphere.smooth(progress += (double)var0 / duration);
                    if (!(progress >= 1.0)) break;
                    mode = 2;
                    vis = from + delta;
                    break;
                }
                case 3: {
                    double var8 = HalloweenAtmosphere.mod(var6 - HalloweenAtmosphere.mod(from));
                    if (var8 > 21000.0) {
                        var8 -= 24000.0;
                    }
                    vis = from + var8 * HalloweenAtmosphere.smooth(progress += (double)var0 / duration);
                    if (!(progress >= 1.0)) break;
                    mode = 0;
                }
            }
            class_4184 var12 = var1.field_1773.method_19418();
            if (var12 != null && var12.method_19332()) {
                float var10;
                class_243 var9 = var12.method_71156();
                float f = var10 = var5 ? HalloweenAtmosphere.measureUnder(var2, var9) : 0.0f;
                if (!underValid) {
                    under = var10;
                    underValid = true;
                } else {
                    under += (var10 - under) * (1.0f - (float)Math.exp(-var0 / 1.4f));
                }
                outdoor = 1.0f - under;
                int var11 = var2.method_8314(class_1944.field_9282, class_2338.method_49638((class_2374)var9));
                lit += ((float)var11 / 15.0f - lit) * (1.0f - (float)Math.exp(-var0 / 0.9f));
            }
        }
    }

    private static float measureUnder(class_638 var0, class_243 var1) {
        int var2 = class_3532.method_15357((double)var1.field_1352);
        int var3 = class_3532.method_15357((double)var1.field_1351);
        int var4 = class_3532.method_15357((double)var1.field_1350);
        float var5 = 0.0f;
        float var6 = 0.0f;
        for (int var7 = 0; var7 < PROBES.length; var7 = (int)((byte)(var7 + 3))) {
            PROBE.method_10103(var2 + PROBES[var7], var3 + PROBES[var7 + 1], var4 + PROBES[var7 + 2]);
            int var8 = var0.method_8314(class_1944.field_9284, (class_2338)PROBE);
            var5 += (float)var8;
            var6 = Math.max(var6, (float)var8);
        }
        float var11 = (0.6f * var5 / (float)(PROBES.length / 3) + 0.4f * var6) / 15.0f;
        float var12 = 1.0f - (float)HalloweenAtmosphere.smooth((var11 - 0.3f) / 0.56f);
        float var9 = 0.0f;
        for (int var10 = 0; var10 < COLUMNS.length; var10 = (int)((byte)(var10 + 2))) {
            var9 += (float)var0.method_8624(class_2902.class_2903.field_13203, var2 + COLUMNS[var10], var4 + COLUMNS[var10 + 1]);
        }
        float var14 = Math.max(0.0f, Math.min(1.0f, ((var9 /= (float)(COLUMNS.length / 2)) - (float)var1.field_1351 - 1.0f) / 12.0f));
        return var12 * (0.6f + 0.4f * var14);
    }

    private static void beginToNight(double var0) {
        double var6;
        double var2 = mode == 3 ? vis : var0;
        double var4 = HalloweenAtmosphere.mod(var2);
        if (var4 >= 12500.0) {
            var6 = 15200.0 - var4;
            if (var6 < -11000.0) {
                var6 += 24000.0;
            }
        } else {
            var6 = HalloweenAtmosphere.mod(15200.0 - var4);
        }
        double var8 = 0.0;
        if (var4 < 11000.0) {
            long var10 = (long)Math.floor((var2 + var6) / 24000.0);
            var8 = (double)(-Math.floorMod(var10, 8L)) * 24000.0;
        }
        from = var2 + var8;
        delta = var6;
        progress = 0.0;
        duration = Math.max(2.6, Math.min(8.5, 2.6 + Math.abs(var6) / 13000.0 * 6.0));
        vis = from;
        mode = 1;
    }

    private static void beginToReal() {
        if (world == null) {
            mode = 0;
        } else {
            double var0 = world.method_8401().method_217();
            double var2 = HalloweenAtmosphere.mod(var0 - HalloweenAtmosphere.mod(vis));
            from = vis;
            progress = 0.0;
            duration = Math.max(2.6, Math.min(7.0, 2.6 + Math.abs(var2 > 21000.0 ? var2 - 24000.0 : var2) / 13000.0 * 5.0));
            mode = 3;
        }
    }

    public static long timeOfDay(class_1937 var0, long var1) {
        return mode != 0 && var0 == world ? (long)Math.floor(vis) : var1;
    }

    public static float lightmap(int var0, float var1) {
        float var2 = HalloweenAtmosphere.strength();
        if (var2 <= 0.0f) {
            return var1;
        }
        float var3 = HalloweenAtmosphere.darkness();
        float var4 = under;
        return switch (var0) {
            case 1 -> var1 * (1.0f - 0.66f * var3);
            case 2 -> var1 * (1.0f - 0.3f * var3 * (1.0f - 0.6f * var4));
            case 3 -> var1 * (1.0f - var2) + var2 * HalloweenAtmosphere.ambientFloor(var4);
            default -> var1;
            case 5 -> var1 + (1.0f - var1) * 0.4f * var3 * (1.0f - var4);
            case 6 -> var1 * (1.0f - var2);
        };
    }

    private static float ambientFloor(float var0) {
        float var1 = (float)(HalloweenModule.darkness.getValue() / 100.0);
        float var2 = 0.066f;
        float var3 = 0.19f - 0.045f * var1;
        float var4 = var2 + (var3 - var2) * var0;
        return Math.max(0.0f, ((var4 - 0.03f) / 0.96f - 0.03f) / 0.97f);
    }

    public static Vector3f skyLight(Vector3f var0) {
        float var1 = HalloweenAtmosphere.strength();
        if (var1 > 0.0f) {
            var0.mul(1.0f + -0.19999999f * var1, 1.0f + -0.24000001f * var1, 1.0f + 0.059999943f * var1);
        }
        return var0;
    }

    public static float fogAmount() {
        float var0 = HalloweenAtmosphere.strength();
        if (var0 <= 0.0f) {
            return 0.0f;
        }
        float var1 = (float)(HalloweenModule.fogDensity.getValue() / 100.0);
        return var0 * (float)Math.pow(var1, 0.8) * (0.18f + 0.82f * outdoor) * (0.4f + 0.6f * HalloweenAtmosphere.nightness());
    }

    private static float nearEnd() {
        float var0 = (float)(HalloweenModule.fogDensity.getValue() / 100.0);
        return (150.0f + -126.0f * var0) * (1.0f + 0.6f * lit);
    }

    public static void fog(float[] var0, Vector4f var1) {
        float var2 = HalloweenAtmosphere.fogAmount();
        if (!(var2 <= 0.0f)) {
            float var3 = HalloweenAtmosphere.nearEnd();
            var0[0] = HalloweenAtmosphere.lerp(var0[0], Math.min(var0[0], var3 * 0.03f), var2);
            var0[1] = HalloweenAtmosphere.lerp(var0[1], Math.min(var0[1], var3), var2);
            var0[4] = HalloweenAtmosphere.lerp(var0[4], Math.min(var0[4], var3 * 1.45f), var2);
            var0[5] = HalloweenAtmosphere.lerp(var0[5], Math.min(var0[5], var3 * 2.2f), var2);
            float var4 = var2 * 0.9f;
            var1.x = HalloweenAtmosphere.lerp(var1.x, 0.08f, var4);
            var1.y = HalloweenAtmosphere.lerp(var1.y, 0.066f, var4);
            var1.z = HalloweenAtmosphere.lerp(var1.z, 0.108f, var4);
        }
    }

    private static float lerp(float var0, float var1, float var2) {
        return var0 + (var1 - var0) * var2;
    }

    public static int skyColor(int var0) {
        return HalloweenAtmosphere.mixRgb(var0, 1510692, HalloweenAtmosphere.strength() * 0.78f * HalloweenAtmosphere.nightness());
    }

    public static int sunset(int var0) {
        return var0 & 0xFF000000 | HalloweenAtmosphere.mixRgb(var0, 16735778, HalloweenAtmosphere.strength() * 0.8f) & 0xFFFFFF;
    }

    public static int clouds(int var0) {
        float var1 = HalloweenAtmosphere.strength() * (0.3f + 0.7f * HalloweenAtmosphere.nightness());
        int var2 = Math.round((float)(var0 >>> 24) * (1.0f - 0.6f * var1));
        return var2 << 24 | HalloweenAtmosphere.mixRgb(var0, 2761270, var1 * 0.85f) & 0xFFFFFF;
    }

    public static float stars(float var0) {
        return var0 * (1.0f + 0.35f * HalloweenAtmosphere.strength());
    }

    private static int mixRgb(int var0, int var1, float var2) {
        var2 = Math.max(0.0f, Math.min(1.0f, var2));
        int var3 = Math.round((float)(var0 >> 16 & 0xFF) + (float)((var1 >> 16 & 0xFF) - (var0 >> 16 & 0xFF)) * var2);
        int var4 = Math.round((float)(var0 >> 8 & 0xFF) + (float)((var1 >> 8 & 0xFF) - (var0 >> 8 & 0xFF)) * var2);
        int var5 = Math.round((float)(var0 & 0xFF) + (float)((var1 & 0xFF) - (var0 & 0xFF)) * var2);
        return var0 & 0xFF000000 | var3 << 16 | var4 << 8 | var5;
    }

    public static void moonAngle(float var0) {
        moonAngle = var0;
        moonValid = true;
    }

    public static Vector4f moonDir() {
        if (!moonValid) {
            return new Vector4f(0.0f, 1.0f, 0.0f, 0.0f);
        }
        float var0 = -((float)Math.sin(moonAngle));
        float var1 = (float)Math.cos(moonAngle);
        return new Vector4f(var0, var1, 0.0f, Math.max(0.0f, Math.min(1.0f, (var1 + 0.08f) / 0.25f)));
    }

    public static float outdoor() {
        return outdoor;
    }

    public static float under() {
        return under;
    }

    public static float lit() {
        return lit;
    }

    public static boolean overridingTime() {
        return mode != 0;
    }

    static {
        mode = 0;
        duration = 1.0;
        outdoor = 1.0f;
        PROBE = new class_2338.class_2339();
        PROBES = new int[]{0, 0, 0, 0, 2, 0, 0, 6, 0, 3, 1, 0, -3, 1, 0, 0, 1, 3, 0, 1, -3};
        COLUMNS = new int[]{0, 0, 4, 0, -4, 0, 0, 4, 0, -4};
    }
}

