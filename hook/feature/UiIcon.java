/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.UiTextureData;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public enum UiIcon {
    SWORD,
    GRID,
    BOLT,
    DONUT,
    EYE,
    MONITOR,
    SEARCH,
    GEAR,
    SPEAKER,
    SPEAKER_OFF,
    LAYOUT,
    CHEVRON_LEFT,
    CHEVRON_RIGHT,
    CLOSE,
    GRIP,
    USER,
    BOX,
    KEYBOARD,
    SIGNAL,
    CHECK,
    ACTIVITY,
    GLOBE,
    HEART,
    FOLDER,
    PLUS,
    EDIT,
    COPY,
    TRASH,
    RESET;

    private static final Map<Long, UiTextureData> CACHE;

    UiTextureData tex(int var1) {
        int var2 = Math.max(6, Math.min(256, var1));
        return CACHE.computeIfAbsent((long)this.ordinal() << 32 | (long)var2, var2x -> {
            BufferedImage var3 = new BufferedImage(var2, var2, 2);
            Graphics2D var4 = var3.createGraphics();
            var4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            var4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            var4.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            var4.scale((double)var2 / 24.0, (double)var2 / 24.0);
            var4.setColor(Color.WHITE);
            float var5 = Math.max(1.75f, 31.199999f / (float)var2);
            var4.setStroke(new BasicStroke(var5, 1, 1));
            this.paint(var4);
            var4.dispose();
            int[] var6 = new int[var2 * var2];
            var3.getRGB(0, 0, var2, var2, var6, 0, var2);
            for (int var7 = 0; var7 < var6.length; ++var7) {
                var6[var7] = var6[var7] & 0xFF000000 | 0xFFFFFF;
            }
            return new UiTextureData("icon_" + this.name() + "_" + var2, var2, var2, var6);
        });
    }

    private void paint(Graphics2D var1) {
        switch (this.ordinal()) {
            case 0: {
                var1.draw(UiIcon.poly(14.5, 17.5, 3.0, 6.0, 3.0, 3.0, 6.0, 3.0, 17.5, 14.5));
                UiIcon.line(var1, 13.0, 19.0, 19.0, 13.0);
                UiIcon.line(var1, 16.0, 16.0, 20.0, 20.0);
                UiIcon.line(var1, 19.0, 21.0, 21.0, 19.0);
                break;
            }
            case 1: {
                var1.draw(new RoundRectangle2D.Double(3.0, 3.0, 7.0, 7.0, 2.5, 2.5));
                var1.draw(new RoundRectangle2D.Double(14.0, 3.0, 7.0, 7.0, 2.5, 2.5));
                var1.draw(new RoundRectangle2D.Double(14.0, 14.0, 7.0, 7.0, 2.5, 2.5));
                var1.draw(new RoundRectangle2D.Double(3.0, 14.0, 7.0, 7.0, 2.5, 2.5));
                break;
            }
            case 2: {
                Path2D var4 = UiIcon.poly(13.0, 2.0, 4.0, 13.5, 11.5, 13.5, 10.5, 22.0, 20.0, 10.5, 12.5, 10.5);
                var4.closePath();
                var1.draw(var4);
                break;
            }
            case 3: {
                var1.draw(new Ellipse2D.Double(3.0, 3.0, 18.0, 18.0));
                var1.draw(new Ellipse2D.Double(9.0, 9.0, 6.0, 6.0));
                UiIcon.line(var1, 7.2, 8.0, 8.4, 7.0);
                UiIcon.line(var1, 15.8, 6.6, 16.8, 7.8);
                UiIcon.line(var1, 6.6, 15.4, 7.6, 16.6);
                UiIcon.line(var1, 16.4, 15.8, 17.6, 14.8);
                UiIcon.line(var1, 12.0, 5.2, 12.0, 6.4);
                break;
            }
            case 4: {
                Path2D.Double var5 = new Path2D.Double();
                var5.moveTo(2.0, 12.0);
                var5.curveTo(5.0, 6.0, 8.5, 5.0, 12.0, 5.0);
                var5.curveTo(15.5, 5.0, 19.0, 6.0, 22.0, 12.0);
                var5.curveTo(19.0, 18.0, 15.5, 19.0, 12.0, 19.0);
                var5.curveTo(8.5, 19.0, 5.0, 18.0, 2.0, 12.0);
                var5.closePath();
                var1.draw(var5);
                var1.draw(new Ellipse2D.Double(9.0, 9.0, 6.0, 6.0));
                break;
            }
            case 5: {
                var1.draw(new RoundRectangle2D.Double(2.5, 3.5, 19.0, 13.0, 4.0, 4.0));
                UiIcon.line(var1, 8.0, 20.5, 16.0, 20.5);
                UiIcon.line(var1, 12.0, 16.5, 12.0, 20.5);
                break;
            }
            case 6: {
                var1.draw(new Ellipse2D.Double(3.5, 3.5, 14.0, 14.0));
                UiIcon.line(var1, 20.5, 20.5, 15.6, 15.6);
                break;
            }
            case 7: {
                Path2D.Double var6 = new Path2D.Double();
                int var7 = 8;
                for (int var17 = 0; var17 < var7 * 4; ++var17) {
                    double var19 = Math.PI * 2 * (double)var17 / (double)(var7 * 4) - 1.5707963267948966;
                    double var21 = var17 % 4 != 0 && var17 % 4 != 1 ? 7.4 : 9.6;
                    double var13 = 12.0 + Math.cos(var19) * var21;
                    double var15 = 12.0 + Math.sin(var19) * var21;
                    if (var17 == 0) {
                        var6.moveTo(var13, var15);
                        continue;
                    }
                    var6.lineTo(var13, var15);
                }
                var6.closePath();
                var1.draw(var6);
                var1.draw(new Ellipse2D.Double(9.0, 9.0, 6.0, 6.0));
                break;
            }
            case 8: 
            case 9: {
                Path2D var8 = UiIcon.poly(11.0, 5.0, 6.0, 9.0, 2.5, 9.0, 2.5, 15.0, 6.0, 15.0, 11.0, 19.0);
                var8.closePath();
                var1.draw(var8);
                if (this == SPEAKER) {
                    var1.draw(new Arc2D.Double(10.5, 8.5, 7.0, 7.0, -45.0, 90.0, 0));
                    var1.draw(new Arc2D.Double(9.5, 4.5, 13.0, 15.0, -48.0, 96.0, 0));
                    break;
                }
                UiIcon.line(var1, 22.0, 9.0, 16.0, 15.0);
                UiIcon.line(var1, 16.0, 9.0, 22.0, 15.0);
                break;
            }
            case 10: {
                var1.draw(new RoundRectangle2D.Double(3.0, 3.0, 7.0, 9.0, 2.5, 2.5));
                var1.draw(new RoundRectangle2D.Double(14.0, 3.0, 7.0, 5.0, 2.5, 2.5));
                var1.draw(new RoundRectangle2D.Double(14.0, 12.0, 7.0, 9.0, 2.5, 2.5));
                var1.draw(new RoundRectangle2D.Double(3.0, 16.0, 7.0, 5.0, 2.5, 2.5));
                break;
            }
            case 11: {
                var1.draw(UiIcon.poly(15.0, 18.0, 9.0, 12.0, 15.0, 6.0));
                break;
            }
            case 12: {
                var1.draw(UiIcon.poly(9.0, 18.0, 15.0, 12.0, 9.0, 6.0));
                break;
            }
            case 13: {
                UiIcon.line(var1, 18.0, 6.0, 6.0, 18.0);
                UiIcon.line(var1, 6.0, 6.0, 18.0, 18.0);
                break;
            }
            case 14: {
                for (int var18 = 0; var18 < 2; ++var18) {
                    for (int var20 = 0; var20 < 3; ++var20) {
                        var1.fill(new Ellipse2D.Double(8.0 + (double)var18 * 6.5, 4.6 + (double)var20 * 6.2, 2.4, 2.4));
                    }
                }
                break;
            }
            case 15: {
                var1.draw(new Ellipse2D.Double(8.0, 4.0, 8.0, 8.0));
                Path2D.Double var9 = new Path2D.Double();
                var9.moveTo(5.0, 20.5);
                var9.curveTo(5.0, 16.5, 8.0, 14.5, 12.0, 14.5);
                var9.curveTo(16.0, 14.5, 19.0, 16.5, 19.0, 20.5);
                var1.draw(var9);
                break;
            }
            case 16: {
                Path2D var10 = UiIcon.poly(12.0, 2.8, 20.5, 7.4, 20.5, 16.6, 12.0, 21.2, 3.5, 16.6, 3.5, 7.4);
                var10.closePath();
                var1.draw(var10);
                var1.draw(UiIcon.poly(3.5, 7.4, 12.0, 12.0, 20.5, 7.4));
                UiIcon.line(var1, 12.0, 12.0, 12.0, 21.2);
                break;
            }
            case 17: {
                var1.draw(new RoundRectangle2D.Double(2.5, 6.0, 19.0, 12.0, 4.0, 4.0));
                UiIcon.line(var1, 7.0, 14.5, 17.0, 14.5);
                UiIcon.line(var1, 7.0, 10.0, 7.01, 10.0);
                UiIcon.line(var1, 10.3, 10.0, 10.31, 10.0);
                UiIcon.line(var1, 13.6, 10.0, 13.61, 10.0);
                UiIcon.line(var1, 17.0, 10.0, 17.01, 10.0);
                break;
            }
            case 18: {
                UiIcon.line(var1, 5.0, 19.0, 5.0, 16.0);
                UiIcon.line(var1, 10.0, 19.0, 10.0, 12.5);
                UiIcon.line(var1, 15.0, 19.0, 15.0, 9.0);
                UiIcon.line(var1, 20.0, 19.0, 20.0, 5.0);
                break;
            }
            case 19: {
                var1.draw(UiIcon.poly(5.0, 12.5, 10.0, 17.5, 19.5, 7.0));
                break;
            }
            case 20: {
                var1.draw(UiIcon.poly(2.5, 12.0, 6.5, 12.0, 9.5, 5.0, 14.5, 19.0, 17.5, 12.0, 21.5, 12.0));
                break;
            }
            case 21: {
                var1.draw(new Ellipse2D.Double(3.0, 3.0, 18.0, 18.0));
                var1.draw(new Ellipse2D.Double(8.0, 3.0, 8.0, 18.0));
                UiIcon.line(var1, 3.4, 12.0, 20.6, 12.0);
                break;
            }
            case 22: {
                Path2D.Double var11 = new Path2D.Double();
                var11.moveTo(12.0, 20.0);
                var11.curveTo(5.0, 15.5, 2.5, 12.0, 2.5, 8.6);
                var11.curveTo(2.5, 5.6, 4.8, 3.6, 7.5, 3.6);
                var11.curveTo(9.4, 3.6, 11.0, 4.7, 12.0, 6.4);
                var11.curveTo(13.0, 4.7, 14.6, 3.6, 16.5, 3.6);
                var11.curveTo(19.2, 3.6, 21.5, 5.6, 21.5, 8.6);
                var11.curveTo(21.5, 12.0, 19.0, 15.5, 12.0, 20.0);
                var11.closePath();
                var1.draw(var11);
                break;
            }
            case 23: {
                Path2D var2 = UiIcon.poly(3.0, 7.0, 3.0, 5.0, 9.0, 5.0, 11.0, 7.0, 21.0, 7.0, 21.0, 19.0, 3.0, 19.0);
                var2.closePath();
                var1.draw(var2);
                UiIcon.line(var1, 3.0, 10.0, 21.0, 10.0);
                break;
            }
            case 24: {
                UiIcon.line(var1, 12.0, 5.0, 12.0, 19.0);
                UiIcon.line(var1, 5.0, 12.0, 19.0, 12.0);
                break;
            }
            case 25: {
                Path2D var3 = UiIcon.poly(5.0, 15.0, 15.0, 5.0, 19.0, 9.0, 9.0, 19.0, 4.0, 20.0, 5.0, 15.0);
                var1.draw(var3);
                UiIcon.line(var1, 13.0, 7.0, 17.0, 11.0);
                break;
            }
            case 26: {
                var1.draw(new RoundRectangle2D.Double(8.0, 8.0, 12.0, 13.0, 3.0, 3.0));
                var1.draw(UiIcon.poly(15.0, 4.0, 4.0, 4.0, 4.0, 16.0));
                break;
            }
            case 27: {
                UiIcon.line(var1, 5.0, 7.0, 19.0, 7.0);
                UiIcon.line(var1, 9.0, 4.0, 15.0, 4.0);
                var1.draw(UiIcon.poly(7.0, 7.0, 8.0, 20.0, 16.0, 20.0, 17.0, 7.0));
                UiIcon.line(var1, 10.0, 11.0, 10.0, 16.0);
                UiIcon.line(var1, 14.0, 11.0, 14.0, 16.0);
                break;
            }
            case 28: {
                var1.draw(new Arc2D.Double(5.0, 5.0, 15.0, 15.0, -80.0, 285.0, 0));
                var1.draw(UiIcon.poly(4.0, 4.0, 4.0, 10.0, 10.0, 10.0));
            }
        }
    }

    private static Path2D poly(double ... var0) {
        Path2D.Double var1 = new Path2D.Double();
        var1.moveTo(var0[0], var0[1]);
        for (int var2 = 2; var2 < var0.length; var2 = (int)((byte)(var2 + 2))) {
            var1.lineTo(var0[var2], var0[var2 + 1]);
        }
        return var1;
    }

    private static void line(Graphics2D var0, double var1, double var3, double var5, double var7) {
        var0.draw(new Line2D.Double(var1, var3, var5, var7));
    }

    static {
        CACHE = new HashMap<Long, UiTextureData>();
    }
}

