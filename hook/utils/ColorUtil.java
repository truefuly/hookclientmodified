/*
 * Decompiled with CFR 0.152.
 */
package hook.utils;

import hook.utils.MathUtil;
import java.awt.Color;

public final class ColorUtil {
    public static Color a(int n, int a) {
        Color hsbColor = Color.getHSBColor((float)((System.currentTimeMillis() * 3L + (long)(n * 175)) % 7200L) / 7200.0f, 0.6f, 1.0f);
        return new Color(hsbColor.getRed(), hsbColor.getGreen(), hsbColor.getBlue(), a);
    }

    public static Color rainbow(double speed, int offset, int alpha) {
        double scaledTime = (double)System.currentTimeMillis() * Math.max(0.1, speed) * 1.5;
        float hue = (float)((scaledTime + (double)offset * 175.0) % 7200.0) / 7200.0f;
        Color hsbColor = Color.getHSBColor(hue, 0.6f, 1.0f);
        return new Color(hsbColor.getRed(), hsbColor.getGreen(), hsbColor.getBlue(), alpha);
    }

    public static Color alphaStep_Hookded_From_Prestige_Client_NumberOne(Color color, int n, int n2) {
        float[] hsbvals = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbvals);
        hsbvals[2] = 0.25f + 0.75f * Math.abs(((float)(System.currentTimeMillis() % 2000L) / 1000.0f + (float)n / (float)n2 * 2.0f) % 2.0f - 1.0f) % 2.0f;
        int hsBtoRGB = Color.HSBtoRGB(hsbvals[0], hsbvals[1], hsbvals[2]);
        return new Color(hsBtoRGB >> 16 & 0xFF, hsBtoRGB >> 8 & 0xFF, hsBtoRGB & 0xFF, color.getAlpha());
    }

    public static Color a(float n, Color color, Color color2) {
        return new Color((int)MathUtil.approachValue(n, color2.getRed(), color.getRed()), (int)MathUtil.approachValue(n, color2.getGreen(), color.getGreen()), (int)MathUtil.approachValue(n, color2.getBlue(), color.getBlue()));
    }

    public static Color a(float n, int n2, Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)MathUtil.approachValue(n, color.getAlpha(), n2));
    }

    public static Color a(Color color, Color color2, float n) {
        return new Color(ColorUtil.a(Math.round((float)color.getRed() + n * (float)(color2.getRed() - color.getRed())), 0, 255), ColorUtil.a(Math.round((float)color.getGreen() + n * (float)(color2.getGreen() - color.getGreen())), 0, 255), ColorUtil.a(Math.round((float)color.getBlue() + n * (float)(color2.getBlue() - color.getBlue())), 0, 255), ColorUtil.a(Math.round((float)color.getAlpha() + n * (float)(color2.getAlpha() - color.getAlpha())), 0, 255));
    }

    private static int a(int b, int a, int a2) {
        return Math.max(a, Math.min(a2, b));
    }
}

