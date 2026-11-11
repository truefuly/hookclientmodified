/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.module.modules.client.Hook;
import java.awt.Color;

public enum UiPalette {
    Black(15527409, 0xFFFFFF, 1.0f, 1316121, 1579293, 131587, 0xFFFFFF, 9343126),
    White(0xFFFFFF, 0xFFFFFF, 1.9f, 0x33353C, 3816516, 657932, 0xFFFFFF, 14277860),
    Purple(12166143, 10585343, 1.45f, 1578026, 2825546, 328459, 0xFFFFFF, 10255359),
    Ocean(8571135, 5941503, 1.4f, 1120806, 1388623, 132620, 0xFFFFFF, 6140671),
    Rose(16753606, 16742572, 1.35f, 2233372, 4528175, 656134, 0xFFFFFF, 16745137),
    Mint(9302732, 5234092, 1.3f, 0x111F1B, 1391923, 133640, 0xFFFFFF, 5956276),
    Amber(16763786, 16754002, 1.3f, 0x211A11, 4599834, 656898, 0xFFFFFF, 16756318),
    Crimson(0xFF8A8A, 0xFF5A5A, 1.3f, 2233108, 4659483, 656131, 0xFFFFFF, 0xFF6464),
    Halloween(15964750, 9725680, 1.25f, 1708568, 2757680, 656389, 16761470, 10706687),
    Hook(5231013, 5231013, 0.6f, 1316379, 1777188, 723983, 0xFFFFFF, 10134450);

    private static final int SIZE = 23;
    private static final long BLEND_NS = 700000000L;
    private static UiPalette target;
    private static float[] from;
    private static long changedAt;
    private static float[] cached;
    private static long cachedAt;
    final int accent;
    final int light;
    final float lightAmount;
    final int glass;
    final int bgTop;
    final int bgBottom;
    final int logoTop;
    final int logoBottom;

    private UiPalette(int nullxx, int nullxxx, float nullxxxx, int nullxxxxx, int nullxxxxxx, int nullxxxxxxx, int nullxxxxxxxx, int nullxxxxxxxxx) {
        this.accent = nullxx;
        this.light = nullxxx;
        this.lightAmount = nullxxxx;
        this.glass = nullxxxxx;
        this.bgTop = nullxxxxxx;
        this.bgBottom = nullxxxxxxx;
        this.logoTop = nullxxxxxxxx;
        this.logoBottom = nullxxxxxxxxx;
    }

    private float[] vector() {
        float[] var1 = new float[23];
        UiPalette.put(var1, 0, this.accent);
        UiPalette.put(var1, 3, this.light);
        UiPalette.put(var1, 6, this.glass);
        UiPalette.put(var1, 9, this.bgTop);
        UiPalette.put(var1, 12, this.bgBottom);
        UiPalette.put(var1, 15, this.logoTop);
        UiPalette.put(var1, 18, this.logoBottom);
        var1[21] = this.lightAmount;
        var1[22] = this == Halloween ? 1.0f : 0.0f;
        return var1;
    }

    private static void put(float[] var0, int var1, int var2) {
        var0[var1] = var2 >> 16 & 0xFF;
        var0[var1 + 1] = var2 >> 8 & 0xFF;
        var0[var1 + 2] = var2 & 0xFF;
    }

    public static UiPalette selected() {
        try {
            UiPalette var1;
            Enum<UiPalette> enum_ = hook.module.modules.client.Hook.theme.getValue();
            return enum_ instanceof UiPalette ? (var1 = (UiPalette)enum_) : Black;
        }
        catch (Throwable var2) {
            return Black;
        }
    }

    private static float[] now() {
        long var0 = System.nanoTime();
        if (var0 == cachedAt && cached != null) {
            return cached;
        }
        UiPalette var2 = UiPalette.selected();
        if (target == null) {
            target = var2;
            from = var2.vector();
            changedAt = var0 - 700000000L;
        } else if (var2 != target) {
            from = cached != null ? (float[])cached.clone() : target.vector();
            target = var2;
            changedAt = var0;
        }
        float var3 = Math.min(1.0f, (float)(var0 - changedAt) / 7.0E8f);
        float var4 = var3 * var3 * (3.0f - 2.0f * var3);
        float[] var5 = target.vector();
        float[] var6 = new float[23];
        for (int var7 = 0; var7 < 23; ++var7) {
            var6[var7] = from[var7] + (var5[var7] - from[var7]) * var4;
        }
        cached = var6;
        cachedAt = var0;
        return var6;
    }

    private static int rgb(float[] var0, int var1, float var2) {
        int var3 = Math.round(Math.max(0.0f, Math.min(1.0f, var2)) * 255.0f);
        return var3 << 24 | Math.round(var0[var1]) << 16 | Math.round(var0[var1 + 1]) << 8 | Math.round(var0[var1 + 2]);
    }

    public static int accent(float var0) {
        return UiPalette.rgb(UiPalette.now(), 0, var0);
    }

    public static int light(float var0) {
        return UiPalette.rgb(UiPalette.now(), 3, var0);
    }

    public static int glass(float var0) {
        return UiPalette.rgb(UiPalette.now(), 6, var0);
    }

    public static int bgTop(float var0) {
        return UiPalette.rgb(UiPalette.now(), 9, var0);
    }

    public static int bgBottom(float var0) {
        return UiPalette.rgb(UiPalette.now(), 12, var0);
    }

    public static int logoTop(float var0) {
        return UiPalette.rgb(UiPalette.now(), 15, var0);
    }

    public static int logoBottom(float var0) {
        return UiPalette.rgb(UiPalette.now(), 18, var0);
    }

    public static float halloween() {
        return UiPalette.now()[22];
    }

    public static float lightAmount() {
        return UiPalette.now()[21];
    }

    public static Color accentColor(int var0) {
        float[] var1 = UiPalette.now();
        return new Color(Math.round(var1[0]), Math.round(var1[1]), Math.round(var1[2]), Math.max(0, Math.min(255, var0)));
    }

    public int previewAccent() {
        return 0xFF000000 | this.accent;
    }

    public int previewLight() {
        return 0xFF000000 | this.light;
    }

    public int previewTop() {
        return 0xFF000000 | this.bgTop;
    }

    public int previewLogo() {
        return 0xFF000000 | this.logoBottom;
    }

    static {
        cachedAt = -1L;
    }
}

