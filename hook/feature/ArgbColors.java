/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class ArgbColors {
    public static final int WHITE = -1;
    public static final int BLACK = -16777216;
    public static final int CLEAR = 0;

    private ArgbColors() {
    }

    public static int rgb(int red, int green, int blue) {
        return 0xFF000000 | ArgbColors.clamp(red) << 16 | ArgbColors.clamp(green) << 8 | ArgbColors.clamp(blue);
    }

    public static int argb(int alpha, int red, int green, int blue) {
        return ArgbColors.clamp(alpha) << 24 | ArgbColors.clamp(red) << 16 | ArgbColors.clamp(green) << 8 | ArgbColors.clamp(blue);
    }

    public static int hex(String value) {
        String digits = value.startsWith("#") ? value.substring(1) : value;
        long parsed = Long.parseLong(digits, 16);
        return digits.length() <= 6 ? (int)(0xFF000000L | parsed) : (int)parsed;
    }

    public static int hsv(float hue, float saturation, float value) {
        float blue;
        float green;
        float red;
        float sector = (hue % 360.0f + 360.0f) % 360.0f / 60.0f;
        float chroma = value * saturation;
        float secondary = chroma * (1.0f - Math.abs(sector % 2.0f - 1.0f));
        if (sector < 1.0f) {
            red = chroma;
            green = secondary;
            blue = 0.0f;
        } else if (sector < 2.0f) {
            red = secondary;
            green = chroma;
            blue = 0.0f;
        } else if (sector < 3.0f) {
            red = 0.0f;
            green = chroma;
            blue = secondary;
        } else if (sector < 4.0f) {
            red = 0.0f;
            green = secondary;
            blue = chroma;
        } else if (sector < 5.0f) {
            red = secondary;
            green = 0.0f;
            blue = chroma;
        } else {
            red = chroma;
            green = 0.0f;
            blue = secondary;
        }
        float minimum = value - chroma;
        return ArgbColors.rgb(Math.round((red + minimum) * 255.0f), Math.round((green + minimum) * 255.0f), Math.round((blue + minimum) * 255.0f));
    }

    public static int mix(int first, int second, float weight) {
        float inverse = 1.0f - weight;
        return ArgbColors.argb(Math.round((float)ArgbColors.alpha(first) * inverse + (float)ArgbColors.alpha(second) * weight), Math.round((float)ArgbColors.red(first) * inverse + (float)ArgbColors.red(second) * weight), Math.round((float)ArgbColors.green(first) * inverse + (float)ArgbColors.green(second) * weight), Math.round((float)ArgbColors.blue(first) * inverse + (float)ArgbColors.blue(second) * weight));
    }

    public static int shade(int color, float factor) {
        if (factor <= 1.0f) {
            return ArgbColors.argb(ArgbColors.alpha(color), Math.round((float)ArgbColors.red(color) * factor), Math.round((float)ArgbColors.green(color) * factor), Math.round((float)ArgbColors.blue(color) * factor));
        }
        float brighten = Math.min(1.0f, factor - 1.0f);
        return ArgbColors.argb(ArgbColors.alpha(color), Math.round((float)ArgbColors.red(color) + (float)(255 - ArgbColors.red(color)) * brighten), Math.round((float)ArgbColors.green(color) + (float)(255 - ArgbColors.green(color)) * brighten), Math.round((float)ArgbColors.blue(color) + (float)(255 - ArgbColors.blue(color)) * brighten));
    }

    public static int withAlpha(int color, float alpha) {
        return ArgbColors.clamp(Math.round(alpha * 255.0f)) << 24 | color & 0xFFFFFF;
    }

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int red(int color) {
        return color >> 16 & 0xFF;
    }

    public static int green(int color) {
        return color >> 8 & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    static int clamp(int value) {
        return value < 0 ? 0 : Math.min(value, 255);
    }
}

