/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.FontManager;
import hook.feature.UiTextureData;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

final class HalloweenIconAtlas {
    static final int GHOST_FRAMES = 8;
    private static final Map<String, UiTextureData> CACHE = new HashMap<String, UiTextureData>();

    private HalloweenIconAtlas() {
    }

    static int quant(float var0) {
        return Math.max(8, Math.min(512, Math.round(var0 / 8.0f) * 8));
    }

    private static UiTextureData cached(String var0, Supplier<UiTextureData> var1) {
        UiTextureData var2 = CACHE.get(var0);
        if (var2 == null) {
            var2 = var1.get();
            CACHE.put(var0, var2);
        }
        return var2;
    }

    private static Graphics2D g(BufferedImage var0) {
        Graphics2D var1 = var0.createGraphics();
        var1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        var1.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        var1.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        var1.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        return var1;
    }

    private static UiTextureData finish(String var0, BufferedImage var1, int var2, int var3, int var4, boolean var5) {
        int[] var6 = var1.getRGB(0, 0, var1.getWidth(), var1.getHeight(), null, 0, var1.getWidth());
        int var7 = var1.getWidth();
        int var8 = var7 / var2;
        float[] var9 = new float[var2 * var3];
        float[] var10 = new float[var2 * var3];
        float[] var11 = new float[var2 * var3];
        float[] var12 = new float[var2 * var3];
        for (int var13 = 0; var13 < var3; ++var13) {
            for (int var14 = 0; var14 < var2; ++var14) {
                float var15 = 0.0f;
                float var16 = 0.0f;
                float var17 = 0.0f;
                float var18 = 0.0f;
                for (int var19 = 0; var19 < var8; ++var19) {
                    for (int var20 = 0; var20 < var8; ++var20) {
                        int var21 = var6[(var13 * var8 + var19) * var7 + var14 * var8 + var20];
                        float var22 = (float)(var21 >>> 24) / 255.0f;
                        var15 += var22;
                        var16 += (float)(var21 >> 16 & 0xFF) * var22;
                        var17 += (float)(var21 >> 8 & 0xFF) * var22;
                        var18 += (float)(var21 & 0xFF) * var22;
                    }
                }
                int var30 = var13 * var2 + var14;
                var9[var30] = var15 / (float)(var8 * var8);
                var10[var30] = var16 / (float)(var8 * var8);
                var11[var30] = var17 / (float)(var8 * var8);
                var12[var30] = var18 / (float)(var8 * var8);
            }
        }
        for (int var23 = 0; var23 < var4; ++var23) {
            var9 = HalloweenIconAtlas.box(var9, var2, var3);
            var10 = HalloweenIconAtlas.box(var10, var2, var3);
            var11 = HalloweenIconAtlas.box(var11, var2, var3);
            var12 = HalloweenIconAtlas.box(var12, var2, var3);
        }
        int[] var24 = new int[var2 * var3];
        for (int var25 = 0; var25 < var24.length; ++var25) {
            float var26 = Math.min(1.0f, var9[var25]);
            if (var5) {
                var24[var25] = Math.round(var26 * 255.0f) << 24 | 0xFFFFFF;
                continue;
            }
            if (!(var26 > 1.0E-4f)) continue;
            int var27 = Math.min(255, Math.round(var10[var25] / var9[var25]));
            int var28 = Math.min(255, Math.round(var11[var25] / var9[var25]));
            int var29 = Math.min(255, Math.round(var12[var25] / var9[var25]));
            var24[var25] = Math.round(var26 * 255.0f) << 24 | var27 << 16 | var28 << 8 | var29;
        }
        return new UiTextureData(var0, var2, var3, var24);
    }

    private static float[] box(float[] var0, int var1, int var2) {
        float[] var3 = new float[var0.length];
        for (int var4 = 0; var4 < var2; ++var4) {
            for (int var5 = 0; var5 < var1; ++var5) {
                float var6 = 0.0f;
                int var7 = 0;
                for (int var8 = -1; var8 <= 1; ++var8) {
                    for (int var9 = -1; var9 <= 1; ++var9) {
                        int var10 = var5 + var9;
                        int var11 = var4 + var8;
                        if (var10 < 0 || var11 < 0 || var10 >= var1 || var11 >= var2) continue;
                        var6 += var0[var11 * var1 + var10];
                        ++var7;
                    }
                }
                var3[var4 * var1 + var5] = var6 / (float)var7;
            }
        }
        return var3;
    }

    static UiTextureData ghost(int var0, int var1) {
        int var2 = HalloweenIconAtlas.quant(var0);
        return HalloweenIconAtlas.cached("hw_ghost_" + var2 + "_" + var1, () -> {
            int var3 = Math.round((float)var2 * 1.25f);
            int var4 = 2;
            BufferedImage var5 = new BufferedImage(var2 * var4, var3 * var4, 2);
            Graphics2D var6 = HalloweenIconAtlas.g(var5);
            var6.scale((double)(var2 * var4) / 100.0, (double)(var3 * var4) / 125.0);
            double var7 = (double)var1 * Math.PI * 2.0 / 8.0;
            Path2D.Double var9 = new Path2D.Double();
            var9.moveTo(18.0, 58.0);
            var9.curveTo(18.0, 22.0, 36.0, 8.0, 50.0, 8.0);
            var9.curveTo(64.0, 8.0, 82.0, 22.0, 82.0, 58.0);
            var9.curveTo(83.0, 76.0, 86.0 + 3.0 * Math.sin(var7), 92.0, 88.0 + 2.0 * Math.sin(var7 + 0.5), 104.0);
            int var10 = 4;
            double var11 = 12.0 + 2.0 * Math.sin(var7 + 1.7);
            double var13 = 88.0 + 2.0 * Math.sin(var7 + 0.5);
            for (int var15 = var10; var15 > 0; --var15) {
                double var16 = var11 + (var13 - var11) * (double)var15 / (double)var10;
                double var18 = var11 + (var13 - var11) * (double)(var15 - 1) / (double)var10;
                double var20 = (var16 + var18) / 2.0;
                double var22 = 9.0 + 5.0 * Math.sin(var7 * 1.0 + (double)var15 * 1.9);
                double var24 = 104.0 + 4.0 * Math.sin(var7 + (double)var15 * 1.3);
                double var26 = 104.0 + 4.0 * Math.sin(var7 + (double)(var15 - 1) * 1.3);
                var9.curveTo(var16 - 3.0, var24 + var22, var20 + 3.0, (var24 + var26) / 2.0 + var22 + 6.0, var18, var26);
            }
            var9.curveTo(14.0 + 2.0 * Math.sin(var7 + 2.1), 92.0, 17.0, 76.0, 18.0, 58.0);
            var9.closePath();
            var6.setPaint(new GradientPaint(0.0f, 10.0f, new Color(255, 255, 255, 250), 0.0f, 122.0f, new Color(255, 255, 255, 40)));
            var6.fill(var9);
            var6.setColor(new Color(24, 10, 34, 235));
            var6.fill(new Ellipse2D.Double(33.0, 36.0, 11.0, 15.0));
            var6.fill(new Ellipse2D.Double(56.0, 36.0, 11.0, 15.0));
            var6.fill(new Ellipse2D.Double(43.5, 57.0, 13.0, 10.0 + 3.0 * Math.sin(var7 * 2.0)));
            var6.dispose();
            return HalloweenIconAtlas.finish("hw_ghost_" + var2 + "_" + var1, var5, var2, var3, 1, false);
        });
    }

    static UiTextureData pumpkin(int var0) {
        int var1 = HalloweenIconAtlas.quant(var0);
        return HalloweenIconAtlas.cached("hw_pumpkin_" + var1, () -> {
            int var1x = 2;
            BufferedImage var2 = new BufferedImage(var1 * var1x, var1 * var1x, 2);
            Graphics2D var3 = HalloweenIconAtlas.g(var2);
            var3.scale((double)(var1 * var1x) / 100.0, (double)(var1 * var1x) / 100.0);
            double[][] var4 = new double[][]{{18.0, 30.0, 40.0, 62.0}, {60.0, 30.0, 22.0, 62.0}, {30.0, 26.0, 26.0, 66.0}, {44.0, 26.0, 26.0, 66.0}, {36.0, 25.0, 28.0, 68.0}};
            Color[] var5 = new Color[]{new Color(196, 82, 20), new Color(196, 82, 20), new Color(222, 104, 30), new Color(222, 104, 30), new Color(238, 124, 40)};
            for (int var6 = 0; var6 < var4.length; ++var6) {
                double[] var7 = var4[var6];
                Ellipse2D.Double var8 = new Ellipse2D.Double(var7[0], var7[1], var7[2], var7[3]);
                var3.setPaint(new RadialGradientPaint(new Point2D.Double(var7[0] + var7[2] * 0.42, var7[1] + var7[3] * 0.35), (float)(var7[3] * 0.75), new float[]{0.0f, 1.0f}, new Color[]{var5[var6].brighter(), var5[var6].darker().darker()}));
                var3.fill(var8);
                var3.setColor(new Color(90, 30, 6, 120));
                var3.setStroke(new BasicStroke(1.2f));
                var3.draw(var8);
            }
            Path2D.Double var10 = new Path2D.Double();
            var10.moveTo(46.0, 30.0);
            var10.curveTo(45.0, 22.0, 47.0, 14.0, 54.0, 10.0);
            var10.lineTo(57.0, 13.0);
            var10.curveTo(52.0, 17.0, 52.0, 23.0, 54.0, 30.0);
            var10.closePath();
            var3.setPaint(new GradientPaint(46.0f, 10.0f, new Color(96, 110, 52), 54.0f, 30.0f, new Color(48, 52, 22)));
            var3.fill(var10);
            Color var11 = new Color(255, 226, 120);
            Color var12 = new Color(255, 140, 30);
            GeneralPath var9 = new GeneralPath();
            var9.moveTo(32.0f, 52.0f);
            var9.lineTo(42.0f, 44.0f);
            var9.lineTo(44.0f, 56.0f);
            var9.closePath();
            var9.moveTo(68.0f, 52.0f);
            var9.lineTo(58.0f, 44.0f);
            var9.lineTo(56.0f, 56.0f);
            var9.closePath();
            var9.moveTo(46.0f, 60.0f);
            var9.lineTo(50.0f, 55.0f);
            var9.lineTo(54.0f, 60.0f);
            var9.closePath();
            var9.moveTo(30.0f, 66.0f);
            var9.curveTo(40.0f, 80.0f, 60.0f, 80.0f, 70.0f, 66.0f);
            var9.lineTo(64.0f, 70.0f);
            var9.lineTo(60.0f, 66.0f);
            var9.lineTo(55.0f, 72.0f);
            var9.lineTo(50.0f, 67.0f);
            var9.lineTo(45.0f, 72.0f);
            var9.lineTo(40.0f, 66.0f);
            var9.lineTo(36.0f, 70.0f);
            var9.closePath();
            var3.setPaint(new RadialGradientPaint(new Point2D.Double(50.0, 62.0), 26.0f, new float[]{0.0f, 1.0f}, new Color[]{var11, var12}));
            var3.fill(var9);
            var3.dispose();
            return HalloweenIconAtlas.finish("hw_pumpkin_" + var1, var2, var1, var1, 0, false);
        });
    }

    static UiTextureData web(int var0) {
        return HalloweenIconAtlas.web(var0, false);
    }

    static UiTextureData web(int var0, boolean var1) {
        int var2 = HalloweenIconAtlas.quant(var0);
        return HalloweenIconAtlas.cached("hw_web_" + var2 + (var1 ? "m" : ""), () -> {
            int var2x = 2;
            BufferedImage var3 = new BufferedImage(var2 * var2x, var2 * var2x, 2);
            Graphics2D var4 = HalloweenIconAtlas.g(var3);
            if (var1) {
                var4.translate(var2 * var2x, 0);
                var4.scale(-1.0, 1.0);
            }
            double var5 = (double)(var2 * var2x) / 100.0;
            var4.setColor(Color.WHITE);
            float var7 = (float)Math.max(1.0, (double)(var2 * var2x) / 140.0);
            var4.setStroke(new BasicStroke(var7, 1, 1));
            int var8 = 7;
            double[] var9 = new double[var8];
            for (int var10 = 0; var10 < var8; ++var10) {
                var9[var10] = Math.toRadians(4.0 + (double)var10 * (82.0 / (double)(var8 - 1)));
                var4.draw(new Line2D.Double(0.0, 0.0, Math.cos(var9[var10]) * 104.0 * var5, Math.sin(var9[var10]) * 104.0 * var5));
            }
            for (int var34 = 1; var34 <= 6; ++var34) {
                double var11 = (double)var34 * 15.5 * var5;
                int var13 = 0;
                while (var13 + 1 < var8) {
                    double var14 = var11 * (1.0 + 0.04 * Math.sin((double)var34 * 3.1 + (double)var13));
                    double var16 = var11 * (1.0 + 0.04 * Math.sin((double)var34 * 3.1 + (double)var13 + 1.0));
                    double var18 = Math.cos(var9[var13]) * var14;
                    double var20 = Math.sin(var9[var13]) * var14;
                    double var22 = Math.cos(var9[var13 + 1]) * var16;
                    double var24 = Math.sin(var9[var13 + 1]) * var16;
                    double var26 = (var18 + var22) / 2.0;
                    double var28 = (var20 + var24) / 2.0;
                    double var30 = Math.hypot(var26, var28);
                    double var32 = 0.86;
                    var4.draw(new QuadCurve2D.Double(var18, var20, var26 / var30 * var30 * var32, var28 / var30 * var30 * var32, var22, var24));
                    ++var13;
                }
            }
            var4.dispose();
            return HalloweenIconAtlas.finish("hw_web_" + var2 + (var1 ? "m" : ""), var3, var2, var2, 0, true);
        });
    }

    static UiTextureData spider(int var0) {
        int var1 = HalloweenIconAtlas.quant(var0);
        return HalloweenIconAtlas.cached("hw_spider_" + var1, () -> {
            int var1x = 2;
            BufferedImage var2 = new BufferedImage(var1 * var1x, var1 * var1x, 2);
            Graphics2D var3 = HalloweenIconAtlas.g(var2);
            var3.scale((double)(var1 * var1x) / 100.0, (double)(var1 * var1x) / 100.0);
            var3.setColor(Color.WHITE);
            var3.setStroke(new BasicStroke(4.0f, 1, 1));
            for (int var4 = -1; var4 <= 1; var4 = (int)((byte)(var4 + 2))) {
                for (int var5 = 0; var5 < 4; ++var5) {
                    double var6 = 46 + var5 * 5;
                    double var8 = 50 + var4 * 8;
                    double var10 = 50 + var4 * (30 + var5 * 4);
                    double var12 = var6 - 16.0 + (double)(var5 * 14);
                    Path2D.Double var14 = new Path2D.Double();
                    var14.moveTo(var8, var6);
                    var14.quadTo(50 + var4 * (22 + var5 * 2), var6 - 20.0 + (double)(var5 * 4), var10, var12);
                    var3.draw(var14);
                }
            }
            var3.fill(new Ellipse2D.Double(38.0, 44.0, 24.0, 30.0));
            var3.fill(new Ellipse2D.Double(42.0, 32.0, 16.0, 16.0));
            var3.dispose();
            return HalloweenIconAtlas.finish("hw_spider_" + var1, var2, var1, var1, 0, true);
        });
    }

    static UiTextureData bat(int var0, int var1) {
        int var2 = HalloweenIconAtlas.quant(var0);
        return HalloweenIconAtlas.cached("hw_bat_" + var2 + "_" + var1, () -> {
            int var3 = var2 / 2;
            int var4 = 2;
            BufferedImage var5 = new BufferedImage(var2 * var4, var3 * var4, 2);
            Graphics2D var6 = HalloweenIconAtlas.g(var5);
            var6.scale((double)(var2 * var4) / 100.0, (double)(var3 * var4) / 50.0);
            var6.setColor(Color.WHITE);
            double var7 = var1 == 0 ? -14.0 : (var1 == 1 ? 0.0 : 10.0);
            Path2D.Double var9 = new Path2D.Double();
            var9.moveTo(50.0, 22.0);
            var9.curveTo(40.0, 14.0 + var7 * 0.3, 22.0, 10.0 + var7, 2.0, 14.0 + var7);
            var9.curveTo(10.0, 20.0 + var7 * 0.6, 12.0, 26.0 + var7 * 0.3, 10.0, 32.0 + var7 * 0.2);
            var9.curveTo(18.0, 27.0, 22.0, 32.0, 26.0, 36.0 + var7 * 0.1);
            var9.curveTo(32.0, 30.0, 36.0, 34.0, 40.0, 38.0);
            var9.curveTo(44.0, 32.0, 46.0, 30.0, 50.0, 34.0);
            var9.curveTo(54.0, 30.0, 56.0, 32.0, 60.0, 38.0);
            var9.curveTo(64.0, 34.0, 68.0, 30.0, 74.0, 36.0 + var7 * 0.1);
            var9.curveTo(78.0, 32.0, 82.0, 27.0, 90.0, 32.0 + var7 * 0.2);
            var9.curveTo(88.0, 26.0 + var7 * 0.3, 90.0, 20.0 + var7 * 0.6, 98.0, 14.0 + var7);
            var9.curveTo(78.0, 10.0 + var7, 60.0, 14.0 + var7 * 0.3, 50.0, 22.0);
            var9.closePath();
            var6.fill(var9);
            var6.fill(new Ellipse2D.Double(45.0, 18.0, 10.0, 18.0));
            Path2D.Double var10 = new Path2D.Double();
            var10.moveTo(46.0, 21.0);
            var10.lineTo(47.0, 13.0);
            var10.lineTo(49.5, 19.0);
            var10.lineTo(50.5, 19.0);
            var10.lineTo(53.0, 13.0);
            var10.lineTo(54.0, 21.0);
            var10.closePath();
            var6.fill(var10);
            var6.dispose();
            return HalloweenIconAtlas.finish("hw_bat_" + var2 + "_" + var1, var5, var2, var3, 0, true);
        });
    }

    static UiTextureData moon(int var0) {
        int var1 = HalloweenIconAtlas.quant(var0);
        return HalloweenIconAtlas.cached("hw_moon_" + var1, () -> {
            int var1x = 2;
            BufferedImage var2 = new BufferedImage(var1 * var1x, var1 * var1x, 2);
            Graphics2D var3 = HalloweenIconAtlas.g(var2);
            var3.scale((double)(var1 * var1x) / 100.0, (double)(var1 * var1x) / 100.0);
            var3.setPaint(new RadialGradientPaint(new Point2D.Double(42.0, 40.0), 60.0f, new float[]{0.0f, 1.0f}, new Color[]{new Color(255, 246, 226), new Color(214, 196, 176)}));
            var3.fill(new Ellipse2D.Double(2.0, 2.0, 96.0, 96.0));
            var3.setColor(new Color(150, 132, 120, 70));
            var3.fill(new Ellipse2D.Double(24.0, 22.0, 26.0, 20.0));
            var3.fill(new Ellipse2D.Double(54.0, 46.0, 22.0, 26.0));
            var3.fill(new Ellipse2D.Double(34.0, 60.0, 14.0, 12.0));
            var3.fill(new Ellipse2D.Double(62.0, 24.0, 10.0, 9.0));
            var3.dispose();
            return HalloweenIconAtlas.finish("hw_moon_" + var1, var2, var1, var1, 1, false);
        });
    }

    static UiTextureData mist(int var0, int var1) {
        int var2 = HalloweenIconAtlas.quant(var0);
        int var3 = HalloweenIconAtlas.quant(var1);
        return HalloweenIconAtlas.cached("hw_mist_" + var2 + "x" + var3, () -> {
            float[] var2x = new float[var2 * var3];
            int var3x = 6;
            for (int var4 = 0; var4 < var3; ++var4) {
                for (int var5 = 0; var5 < var2; ++var5) {
                    float var6 = (float)var5 / (float)var2;
                    float var7 = (float)var4 / (float)var3;
                    float var8 = 0.0f;
                    float var9 = 0.55f;
                    float var10 = 1.0f;
                    for (int var11 = 0; var11 < 4; ++var11) {
                        var8 += var9 * HalloweenIconAtlas.periodicNoise(var6 * (float)var3x * var10, var7 * 3.0f * var10 + (float)var11 * 7.3f, (int)((float)var3x * var10));
                        var9 *= 0.5f;
                        var10 *= 2.0f;
                    }
                    float var12 = (float)Math.sin(Math.PI * (double)var7);
                    var12 = var12 * var12 * (0.4f + 0.6f * var7);
                    var2x[var4 * var2 + var5] = Math.max(0.0f, Math.min(1.0f, (var8 * 0.9f + 0.35f) * var12));
                }
            }
            return UiTextureData.fromCoverage("hw_mist_" + var2 + "x" + var3, var2, var3, var2x);
        });
    }

    private static float hash(int var0, int var1) {
        int var2 = var0 * 374761393 + var1 * 668265263;
        var2 = (var2 ^ var2 >>> 13) * 1274126177;
        return (float)((var2 ^ var2 >>> 16) & 0xFFFF) / 65535.0f;
    }

    private static float periodicNoise(float var0, float var1, int var2) {
        int var3 = (int)Math.floor(var0);
        int var4 = (int)Math.floor(var1);
        float var5 = var0 - (float)var3;
        float var6 = var1 - (float)var4;
        float var7 = var5 * var5 * (3.0f - 2.0f * var5);
        float var8 = var6 * var6 * (3.0f - 2.0f * var6);
        int var9 = Math.max(1, var2);
        float var10 = HalloweenIconAtlas.hash(Math.floorMod(var3, var9), var4);
        float var11 = HalloweenIconAtlas.hash(Math.floorMod(var3 + 1, var9), var4);
        float var12 = HalloweenIconAtlas.hash(Math.floorMod(var3, var9), var4 + 1);
        float var13 = HalloweenIconAtlas.hash(Math.floorMod(var3 + 1, var9), var4 + 1);
        return (var10 + (var11 - var10) * var7 + (var12 - var10) * var8 + (var10 - var11 - var12 + var13) * var7 * var8) * 2.0f - 1.0f;
    }

    static UiTextureData[] lettering(String var0, int var1, int var2) {
        String var3 = "hw_txt_" + var1 + "_" + var2 + "_" + Integer.toHexString(var0.hashCode());
        UiTextureData var4 = CACHE.get(var3);
        UiTextureData var5 = CACHE.get(var3 + "_g");
        if (var4 == null || var5 == null) {
            Font var6 = FontManager.base(var1);
            Font var7 = var6 == null ? new Font("SansSerif", 1, var2) : var6.deriveFont((float)var2);
            BufferedImage var8 = new BufferedImage(1, 1, 2);
            Graphics2D var9 = HalloweenIconAtlas.g(var8);
            var9.setFont(var7);
            FontMetrics var10 = var9.getFontMetrics();
            int var11 = Math.round((float)var2 * 0.55f);
            int var12 = var10.stringWidth(var0) + var11 * 2;
            int var13 = var10.getAscent() + var10.getDescent() + var11 * 2;
            var9.dispose();
            BufferedImage var14 = new BufferedImage(var12, var13, 2);
            Graphics2D var15 = HalloweenIconAtlas.g(var14);
            var15.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            var15.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            var15.setFont(var7);
            var15.setColor(Color.WHITE);
            var15.drawString(var0, var11, var11 + var10.getAscent());
            var15.dispose();
            int[] var16 = var14.getRGB(0, 0, var12, var13, null, 0, var12);
            float[] var17 = new float[var12 * var13];
            for (int var18 = 0; var18 < var17.length; ++var18) {
                var17[var18] = (float)(var16[var18] >>> 24) / 255.0f;
            }
            var4 = UiTextureData.fromCoverage(var3, var12, var13, var17);
            float[] var21 = (float[])var17.clone();
            int var19 = Math.max(2, Math.round((float)var2 * 0.16f));
            for (int var20 = 0; var20 < 3; ++var20) {
                var21 = HalloweenIconAtlas.blurH(var21, var12, var13, var19);
                var21 = HalloweenIconAtlas.blurV(var21, var12, var13, var19);
            }
            for (int var23 = 0; var23 < var21.length; ++var23) {
                var21[var23] = Math.min(1.0f, var21[var23] * 2.2f);
            }
            var5 = UiTextureData.fromCoverage(var3 + "_g", var12, var13, var21);
            CACHE.put(var3, var4);
            CACHE.put(var3 + "_g", var5);
        }
        return new UiTextureData[]{var4, var5};
    }

    private static float[] blurH(float[] var0, int var1, int var2, int var3) {
        float[] var4 = new float[var0.length];
        for (int var5 = 0; var5 < var2; ++var5) {
            float var6 = 0.0f;
            int var7 = var5 * var1;
            for (int var8 = -var3; var8 <= var3; ++var8) {
                var6 += var0[var7 + Math.max(0, Math.min(var1 - 1, var8))];
            }
            for (int var9 = 0; var9 < var1; ++var9) {
                var4[var7 + var9] = var6 / (float)(2 * var3 + 1);
                var6 += var0[var7 + Math.min(var1 - 1, var9 + var3 + 1)] - var0[var7 + Math.max(0, var9 - var3)];
            }
        }
        return var4;
    }

    private static float[] blurV(float[] var0, int var1, int var2, int var3) {
        float[] var4 = new float[var0.length];
        for (int var5 = 0; var5 < var1; ++var5) {
            float var6 = 0.0f;
            for (int var7 = -var3; var7 <= var3; ++var7) {
                var6 += var0[Math.max(0, Math.min(var2 - 1, var7)) * var1 + var5];
            }
            for (int var8 = 0; var8 < var2; ++var8) {
                var4[var8 * var1 + var5] = var6 / (float)(2 * var3 + 1);
                var6 += var0[Math.min(var2 - 1, var8 + var3 + 1) * var1 + var5] - var0[Math.max(0, var8 - var3) * var1 + var5];
            }
        }
        return var4;
    }
}

