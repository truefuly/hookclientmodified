/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

public final class UiTextureData {
    public final String key;
    public final int w;
    public final int h;
    public final int[] argb;
    public Object handle;

    public UiTextureData(String var1, int var2, int var3, int[] var4) {
        this.key = var1;
        this.w = var2;
        this.h = var3;
        this.argb = var4;
    }

    static UiTextureData fromCoverage(String var0, int var1, int var2, float[] var3) {
        int[] var4 = new int[var1 * var2];
        for (int var5 = 0; var5 < var4.length; ++var5) {
            int var6 = Math.round(Math.max(0.0f, Math.min(1.0f, var3[var5])) * 255.0f);
            var4[var5] = var6 << 24 | 0xFFFFFF;
        }
        return new UiTextureData(var0, var1, var2, var4);
    }
}

