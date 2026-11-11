/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  org.joml.Matrix3x2fStack
 */
package hook.utils;

import hook.Hook;
import hook.feature.FontManager;
import hook.feature.MinecraftUiRenderer;
import hook.utils.RenderUtils;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

public final class TextRenderer {
    private static final float SIZE = 17.0f;
    private static final int WEIGHT = 1;

    private TextRenderer() {
    }

    private static float guiScale() {
        try {
            return Hook.mc.method_22683().method_4495();
        }
        catch (Throwable var1) {
            return 1.0f;
        }
    }

    public static void drawString(CharSequence var0, class_332 var1, int var2, int var3, int var4) {
        RenderUtils.setContext(var1);
        if (FontManager.available()) {
            new MinecraftUiRenderer(var1, TextRenderer.guiScale()).text(var0.toString(), var2, var3, 17.0f, 1, var4);
        } else {
            TextRenderer.drawLargeString(var0, var1, var2, var3, var4);
        }
    }

    public static int getWidth(CharSequence var0) {
        String var1 = var0.toString();
        return FontManager.available() ? Math.round(FontManager.width(var1, 17.0f, 1)) : Hook.mc.field_1772.method_1727(var1) * 2;
    }

    public static void drawCenteredString(CharSequence var0, class_332 var1, int var2, int var3, int var4) {
        if (FontManager.available()) {
            TextRenderer.drawString(var0, var1, var2 - TextRenderer.getWidth(var0) / 2, var3, var4);
        } else {
            TextRenderer.drawCenteredMinecraftText(var0, var1, var2, var3, var4);
        }
    }

    public static void drawLargeString(CharSequence var0, class_332 var1, int var2, int var3, int var4) {
        Matrix3x2fStack var5 = var1.method_51448();
        var5.pushMatrix();
        var5.scale(2.0f, 2.0f);
        var1.method_51433(Hook.mc.field_1772, var0.toString(), var2 / 2, var3 / 2, var4, false);
        var5.popMatrix();
    }

    public static void drawCenteredMinecraftText(CharSequence var0, class_332 var1, int var2, int var3, int var4) {
        Matrix3x2fStack var5 = var1.method_51448();
        var5.pushMatrix();
        var5.scale(2.0f, 2.0f);
        var1.method_51433(Hook.mc.field_1772, var0.toString(), var2 / 2 - Hook.mc.field_1772.method_1727(var0.toString()) / 2, var3 / 2, var4, false);
        var5.popMatrix();
    }
}

