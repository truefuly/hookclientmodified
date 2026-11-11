/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.FontManager;
import hook.feature.UiTextureData;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

final class HalloweenFonts {
    private static Font akira;
    private static boolean failed;
    private static final Map<String, GlyphLayers> LINES;
    private static final Map<String, GlyphMask> GLYPHS;

    private HalloweenFonts() {
    }

    static Font font(float var0) {
        if (akira == null && !failed) {
            try (InputStream var1 = HalloweenFonts.class.getResourceAsStream("/assets/hook/fonts/AkiraExpanded.otf");){
                akira = var1 == null ? null : Font.createFont(0, var1);
            }
            catch (Throwable var6) {
                akira = null;
            }
            if (akira == null) {
                failed = true;
            }
        }
        Font var7 = akira != null ? akira : FontManager.base(1);
        return var7 != null ? var7.deriveFont(var0) : new Font("SansSerif", 1, Math.round(var0));
    }

    private static Shape outline(String var0, Font var1, float var2) {
        FontRenderContext var3 = new FontRenderContext(new AffineTransform(), true, true);
        GlyphVector var4 = var1.createGlyphVector(var3, var0);
        if (var2 != 0.0f) {
            for (int var5 = 0; var5 < var4.getNumGlyphs(); ++var5) {
                Point2D var6 = var4.getGlyphPosition(var5);
                var4.setGlyphPosition(var5, new Point2D.Double(var6.getX() + (double)((float)var5 * var2), var6.getY()));
            }
        }
        return var4.getOutline();
    }

    private static Graphics2D g(BufferedImage var0) {
        Graphics2D var1 = var0.createGraphics();
        var1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        var1.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        var1.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return var1;
    }

    static GlyphLayers title(String var0, int var1, float var2) {
        String var3 = var0 + "@" + var1 + "/" + var2;
        GlyphLayers var4 = LINES.get(var3);
        if (var4 != null) {
            return var4;
        }
        Font var5 = HalloweenFonts.font(var1);
        Shape var6 = HalloweenFonts.outline(var0, var5, (float)var1 * var2);
        Rectangle2D var7 = var6.getBounds2D();
        int var8 = Math.round((float)var1 * 0.6f);
        int var9 = (int)Math.ceil(var7.getWidth()) + var8 * 2;
        int var10 = (int)Math.ceil(var7.getHeight()) + var8 * 2;
        AffineTransform var11 = AffineTransform.getTranslateInstance((double)var8 - var7.getX(), (double)var8 - var7.getY());
        Shape var12 = var11.createTransformedShape(var6);
        float var13 = var8;
        float var14 = (float)var8 + (float)var7.getHeight();
        BufferedImage var15 = new BufferedImage(var9, var10, 2);
        Graphics2D var16 = HalloweenFonts.g(var15);
        var16.setPaint(new LinearGradientPaint(0.0f, var13, 0.0f, var14, new float[]{0.0f, 0.42f, 0.78f, 1.0f}, new Color[]{new Color(255, 241, 214), new Color(255, 196, 112), new Color(244, 120, 38), new Color(196, 70, 22)}));
        var16.fill(var12);
        var16.setClip(var12);
        var16.setPaint(new LinearGradientPaint(0.0f, var13, 0.0f, var13 + (var14 - var13) * 0.22f, new float[]{0.0f, 1.0f}, new Color[]{new Color(255, 255, 255, 90), new Color(255, 255, 255, 0)}));
        var16.fillRect(0, 0, var9, var10);
        var16.dispose();
        BufferedImage var17 = new BufferedImage(var9, var10, 2);
        var16 = HalloweenFonts.g(var17);
        var16.setColor(Color.WHITE);
        var16.setStroke(new BasicStroke(Math.max(2.0f, (float)var1 * 0.075f), 1, 1));
        var16.draw(var12);
        var16.fill(var12);
        var16.dispose();
        GlyphLayers var18 = new GlyphLayers();
        var18.fill = HalloweenFonts.colour("hw_title_fill_" + var3.hashCode(), var15);
        var18.stroke = HalloweenFonts.mask("hw_title_stroke_" + var3.hashCode(), var17);
        var18.glow = HalloweenFonts.glow("hw_title_glow_" + var3.hashCode(), var17, Math.max(3, Math.round((float)var1 * 0.22f)), 1.7f);
        var18.pad = var8;
        var18.w = var9;
        var18.h = var10;
        LINES.put(var3, var18);
        return var18;
    }

    static GlyphMask glyph(char var0, int var1) {
        String var2 = var0 + "@" + var1;
        GlyphMask var3 = GLYPHS.get(var2);
        if (var3 != null) {
            return var3;
        }
        Font var4 = HalloweenFonts.font(var1);
        FontRenderContext var5 = new FontRenderContext(new AffineTransform(), true, true);
        GlyphVector var6 = var4.createGlyphVector(var5, String.valueOf(var0));
        float var7 = var6.getGlyphMetrics(0).getAdvance();
        int var8 = Math.max(2, Math.round((float)var1 * 0.3f));
        int var9 = Math.max(1, (int)Math.ceil(var7) + var8 * 2);
        int var10 = Math.round((float)var1 * 1.0f) + var8 * 2;
        BufferedImage var11 = new BufferedImage(var9, var10, 2);
        Graphics2D var12 = HalloweenFonts.g(var11);
        var12.setColor(Color.WHITE);
        var12.fill(AffineTransform.getTranslateInstance(var8, (float)var8 + (float)var1 * 0.8f).createTransformedShape(var6.getOutline()));
        var12.dispose();
        GlyphMask var13 = new GlyphMask();
        var13.mask = HalloweenFonts.mask("hw_glyph_" + var0 + "_" + var1, var11);
        var13.advance = var7;
        var13.pad = var8;
        GLYPHS.put(var2, var13);
        return var13;
    }

    private static UiTextureData colour(String var0, BufferedImage var1) {
        int var2 = var1.getWidth();
        int var3 = var1.getHeight();
        return new UiTextureData(var0, var2, var3, var1.getRGB(0, 0, var2, var3, null, 0, var2));
    }

    private static UiTextureData mask(String var0, BufferedImage var1) {
        int var2 = var1.getWidth();
        int var3 = var1.getHeight();
        int[] var4 = var1.getRGB(0, 0, var2, var3, null, 0, var2);
        for (int var5 = 0; var5 < var4.length; ++var5) {
            var4[var5] = var4[var5] & 0xFF000000 | 0xFFFFFF;
        }
        return new UiTextureData(var0, var2, var3, var4);
    }

    private static UiTextureData glow(String var0, BufferedImage var1, int var2, float var3) {
        int var4 = var1.getWidth();
        int var5 = var1.getHeight();
        int[] var6 = var1.getRGB(0, 0, var4, var5, null, 0, var4);
        float[] var7 = new float[var4 * var5];
        for (int var8 = 0; var8 < var7.length; ++var8) {
            var7[var8] = (float)(var6[var8] >>> 24) / 255.0f;
        }
        for (int var10 = 0; var10 < 3; ++var10) {
            var7 = HalloweenFonts.blur(var7, var4, var5, var2, true);
            var7 = HalloweenFonts.blur(var7, var4, var5, var2, false);
        }
        for (int var11 = 0; var11 < var7.length; ++var11) {
            var7[var11] = Math.min(1.0f, var7[var11] * var3);
        }
        return UiTextureData.fromCoverage(var0, var4, var5, var7);
    }

    private static float[] blur(float[] var0, int var1, int var2, int var3, boolean var4) {
        float[] var5 = new float[var0.length];
        int var6 = var4 ? var1 : var2;
        int var7 = var4 ? var2 : var1;
        for (int var8 = 0; var8 < var7; ++var8) {
            float var9 = 0.0f;
            for (int var10 = -var3; var10 <= var3; ++var10) {
                var9 += HalloweenFonts.at(var0, var1, var4, var8, Math.max(0, Math.min(var6 - 1, var10)));
            }
            for (int var12 = 0; var12 < var6; ++var12) {
                int var11 = var4 ? var8 * var1 + var12 : var12 * var1 + var8;
                var5[var11] = var9 / (float)(2 * var3 + 1);
                var9 += HalloweenFonts.at(var0, var1, var4, var8, Math.min(var6 - 1, var12 + var3 + 1)) - HalloweenFonts.at(var0, var1, var4, var8, Math.max(0, var12 - var3));
            }
        }
        return var5;
    }

    private static float at(float[] var0, int var1, boolean var2, int var3, int var4) {
        return var2 ? var0[var3 * var1 + var4] : var0[var4 * var1 + var3];
    }

    static {
        LINES = new HashMap<String, GlyphLayers>();
        GLYPHS = new HashMap<String, GlyphMask>();
    }

    static final class GlyphLayers {
        UiTextureData fill;
        UiTextureData stroke;
        UiTextureData glow;
        int pad;
        int w;
        int h;

        GlyphLayers() {
        }
    }

    static final class GlyphMask {
        UiTextureData mask;
        float advance;
        int pad;

        GlyphMask() {
        }
    }
}

