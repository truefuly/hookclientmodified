/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.utils.render.color;

public class Color {
    public int r;
    public int g;
    public int b;
    public int a;
    public static final Color WHITE = new Color(255, 255, 255);

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a) {
        this.r = Color.clamp(r);
        this.g = Color.clamp(g);
        this.b = Color.clamp(b);
        this.a = Color.clamp(a);
    }

    public int getRGB() {
        return this.a << 24 | this.r << 16 | this.g << 8 | this.b;
    }

    public int getAlpha() {
        return this.a;
    }

    public java.awt.Color toAwt() {
        return new java.awt.Color(this.r, this.g, this.b, this.a);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}

