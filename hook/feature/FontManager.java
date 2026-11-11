/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.UiTextureData;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class FontManager {
    public static final int MEDIUM = 0;
    public static final int BOLD = 1;
    private static final String[] FILES = new String[]{"/assets/hook/fonts/Montserrat-Medium.ttf", "/assets/hook/fonts/Montserrat-Bold.ttf"};
    static final String CHARS;
    private static final Font[] BASE;
    private static final boolean[] FAILED;
    private static final Map<Integer, GlyphAtlas> ATLASES;
    private static final int MAX_PX = 160;

    private FontManager() {
    }

    public static boolean available() {
        return FontManager.base(0) != null;
    }

    static Font base(int var0) {
        if (BASE[var0] == null && !FAILED[var0]) {
            try (InputStream var1 = FontManager.class.getResourceAsStream(FILES[var0]);){
                if (var1 == null) {
                    throw new IllegalStateException("missing " + FILES[var0]);
                }
                FontManager.BASE[var0] = Font.createFont(0, var1);
            }
            catch (Throwable var6) {
                FontManager.FAILED[var0] = true;
                System.err.println("Hook: font unavailable, falling back: " + String.valueOf(var6));
            }
        }
        return BASE[var0];
    }

    static GlyphAtlas atlas(int var0, int var1) {
        int var2 = Math.max(4, Math.min(160, var1));
        return ATLASES.computeIfAbsent(var0 << 16 | var2, var2x -> FontManager.build(var0, var2));
    }

    public static float width(String var0, float var1, int var2) {
        GlyphAtlas var3 = FontManager.atlas(var2, Math.max(4, Math.round(var1)));
        float var4 = var1 / (float)var3.px;
        float var5 = 0.0f;
        for (int var6 = 0; var6 < var0.length(); ++var6) {
            var5 += var3.advance(var0.charAt(var6));
        }
        return var5 * var4;
    }

    private static GlyphAtlas build(int var0, int var1) {
        Font var2 = FontManager.base(var0);
        if (var2 == null) {
            return null;
        }
        Font var3 = var2.deriveFont((float)var1);
        FontRenderContext var4 = new FontRenderContext(null, true, true);
        int var5 = CHARS.length();
        BufferedImage[] var6 = new BufferedImage[var5];
        float[] var7 = new float[var5];
        int[] var8 = new int[var5];
        int[] var9 = new int[var5];
        for (int var10 = 0; var10 < var5; ++var10) {
            GlyphVector var11 = var3.createGlyphVector(var4, new char[]{CHARS.charAt(var10)});
            var7[var10] = var11.getGlyphMetrics(0).getAdvanceX();
            Rectangle var12 = var11.getPixelBounds(var4, 0.0f, 0.0f);
            if (var12.width <= 0 || var12.height <= 0) continue;
            BufferedImage var13 = new BufferedImage(var12.width + 2, var12.height + 2, 2);
            Graphics2D var14 = var13.createGraphics();
            var14.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            var14.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            var14.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            var14.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            var14.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            var14.setColor(Color.WHITE);
            var14.drawGlyphVector(var11, -var12.x + 1, -var12.y + 1);
            var14.dispose();
            var6[var10] = var13;
            var8[var10] = var12.x - 1;
            var9[var10] = var12.y - 1;
        }
        int var24 = var1 > 60 ? 2048 : 1024;
        int var25 = 1;
        int var26 = 1;
        int var27 = 0;
        int[] var28 = new int[var5];
        int[] var15 = new int[var5];
        for (int var16 = 0; var16 < var5; ++var16) {
            if (var6[var16] == null) continue;
            if (var25 + var6[var16].getWidth() + 1 > var24) {
                var25 = 1;
                var26 += var27 + 1;
                var27 = 0;
            }
            var28[var16] = var25;
            var15[var16] = var26;
            var25 += var6[var16].getWidth() + 1;
            var27 = Math.max(var27, var6[var16].getHeight());
        }
        int var29 = var26 + var27 + 1;
        int[] var17 = new int[var24 * var29];
        for (int var18 = 0; var18 < var5; ++var18) {
            if (var6[var18] == null) continue;
            BufferedImage var19 = var6[var18];
            for (int var20 = 0; var20 < var19.getHeight(); ++var20) {
                for (int var21 = 0; var21 < var19.getWidth(); ++var21) {
                    int var22 = var19.getRGB(var21, var20) >>> 24;
                    int var23 = (int)Math.round(Math.pow((double)var22 / 255.0, 0.82) * 255.0);
                    var17[(var15[var18] + var20) * var24 + var28[var18] + var21] = var23 << 24 | 0xFFFFFF;
                }
            }
        }
        UiTextureData var30 = new UiTextureData("font_" + var0 + "_" + var1, var24, var29, var17);
        float var31 = var3.getLineMetrics("Hd", var4).getAscent();
        return new GlyphAtlas(var1, var30, var6, var7, var8, var9, var28, var15, var31);
    }

    static {
        BASE = new Font[2];
        FAILED = new boolean[2];
        ATLASES = new HashMap<Integer, GlyphAtlas>();
        StringBuilder var0 = new StringBuilder();
        for (char var1 = ' '; var1 <= '~'; var1 = (char)(var1 + '\u0001')) {
            var0.append(var1);
        }
        var0.append("\u2022\u2026\u00b7\u2014\u2013\u00b0\u00d7\u2190\u2192\u2713\u00a7\u00e9\u00e8\u00e0\u00e7\u00f1\u00fc\u00f6\u00e4\u00df");
        CHARS = var0.toString();
    }

    static final class GlyphAtlas {
        final int px;
        final UiTextureData tex;
        final int[] w;
        final int[] h;
        final float[] adv;
        final int[] ox;
        final int[] oy;
        final int[] u;
        final int[] v;
        final float ascent;
        private final float fallbackAdvance;

        GlyphAtlas(int var1, UiTextureData var2, BufferedImage[] var3, float[] var4, int[] var5, int[] var6, int[] var7, int[] var8, float var9) {
            this.px = var1;
            this.tex = var2;
            this.w = new int[var3.length];
            this.h = new int[var3.length];
            for (int var10 = 0; var10 < var3.length; ++var10) {
                if (var3[var10] == null) continue;
                this.w[var10] = var3[var10].getWidth();
                this.h[var10] = var3[var10].getHeight();
            }
            this.adv = var4;
            this.ox = var5;
            this.oy = var6;
            this.u = var7;
            this.v = var8;
            this.ascent = var9;
            this.fallbackAdvance = var4[CHARS.indexOf(63)];
        }

        int index(char var1) {
            return var1 >= ' ' && var1 <= '~' ? var1 - 32 : CHARS.indexOf(var1);
        }

        float advance(char var1) {
            int var2 = this.index(var1);
            return var2 < 0 ? this.fallbackAdvance : this.adv[var2];
        }
    }
}

