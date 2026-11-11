/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

final class UiEasing {
    private UiEasing() {
    }

    static float clamp(float var0, float var1, float var2) {
        return var0 < var1 ? var1 : Math.min(var0, var2);
    }

    static float out(float var0) {
        float var1 = 1.0f - UiEasing.clamp(var0, 0.0f, 1.0f);
        return 1.0f - var1 * var1 * var1;
    }

    static float inOut(float var0) {
        return (var0 = UiEasing.clamp(var0, 0.0f, 1.0f)) < 0.5f ? 4.0f * var0 * var0 * var0 : 1.0f - (float)Math.pow(-2.0f * var0 + 2.0f, 3.0) / 2.0f;
    }

    static float lerp(float var0, float var1, float var2) {
        return var0 + (var1 - var0) * var2;
    }

    static int mix(int var0, int var1, float var2) {
        var2 = UiEasing.clamp(var2, 0.0f, 1.0f);
        int var3 = Math.round(UiEasing.lerp(var0 >>> 24, var1 >>> 24, var2));
        int var4 = Math.round(UiEasing.lerp(var0 >> 16 & 0xFF, var1 >> 16 & 0xFF, var2));
        int var5 = Math.round(UiEasing.lerp(var0 >> 8 & 0xFF, var1 >> 8 & 0xFF, var2));
        int var6 = Math.round(UiEasing.lerp(var0 & 0xFF, var1 & 0xFF, var2));
        return var3 << 24 | var4 << 16 | var5 << 8 | var6;
    }
}

