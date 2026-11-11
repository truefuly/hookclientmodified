/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.utils.render.color;

import hook.compat.meteorclient.utils.render.color.Color;

public class SettingColor
extends Color {
    public SettingColor(int r, int g, int b) {
        super(r, g, b);
    }

    public SettingColor(int r, int g, int b, int a) {
        super(r, g, b, a);
    }

    public SettingColor(int argb) {
        super(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >> 24 & 0xFF);
    }
}

