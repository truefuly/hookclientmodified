/*
 * Decompiled with CFR 0.152.
 */
package hook.utils;

public final class MathUtil {
    public static double roundToNearest(double value, double step) {
        return step * (double)Math.round(value / step);
    }

    public static double smoothStep(double factor, double start, double end) {
        double max = Math.max(0.0, Math.min(1.0, factor));
        return start + (end - start) * max * max * (3.0 - 2.0 * max);
    }

    public static double approachValue(float speed, double current, double target) {
        double ceil = Math.ceil(Math.abs(target - current) * (double)speed);
        return current < target ? Math.min(current + (double)((int)ceil), target) : Math.max(current - (double)((int)ceil), target);
    }

    public static double linearInterpolate(double factor, double start, double end) {
        return start + (end - start) * factor;
    }

    public static double exponentialInterpolate(double start, double end, double base, double exponent) {
        return MathUtil.linearInterpolate(1.0f - (float)Math.pow(base, exponent), start, end);
    }

    public static double clampValue(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}

