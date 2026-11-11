/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package hook.halloween;

import hook.feature.MinecraftUiRenderer;
import hook.feature.UiIcon;
import hook.feature.UiRenderer;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class CinematicOverlay {
    private static boolean barsWanted;
    private static float bars;
    private static boolean hudWanted;
    private static boolean hudShown;
    private static float dip;
    private static float vignette;
    private static float notice;
    private static String noticeText;

    private CinematicOverlay() {
    }

    public static void cinematic(boolean var0) {
        barsWanted = var0;
        if (hudWanted == var0) {
            boolean bl = hudWanted = !var0;
            if (dip < 0.0f) {
                dip = 0.0f;
            }
        }
    }

    public static void reset() {
        barsWanted = false;
        bars = 0.0f;
        hudWanted = true;
        hudShown = true;
        dip = -1.0f;
        vignette = 0.0f;
    }

    public static void notice(String var0) {
        noticeText = var0;
        notice = 2.2f;
    }

    public static boolean hideHud() {
        return !hudShown;
    }

    public static boolean visible() {
        return bars > 0.001f || dip >= 0.0f || notice > 0.0f || vignette > 0.001f;
    }

    public static void frame(float var0) {
        float var1 = barsWanted ? 1.0f : 0.0f;
        bars = bars < var1 ? Math.min(var1, bars + var0 / 0.85f) : Math.max(var1, bars - var0 / 0.75f);
        float f = vignette = vignette < var1 ? Math.min(var1, vignette + var0 / 1.4f) : Math.max(var1, vignette - var0 / 0.9f);
        if (dip >= 0.0f) {
            if ((dip += var0 / 0.7f) >= 0.5f && hudShown != hudWanted) {
                hudShown = hudWanted;
            }
            if (dip >= 1.0f) {
                dip = -1.0f;
                hudShown = hudWanted;
            }
        }
        if (notice > 0.0f) {
            notice -= var0;
        }
    }

    private static float smooth(float var0) {
        var0 = Math.max(0.0f, Math.min(1.0f, var0));
        return var0 * var0 * var0 * (var0 * (var0 * 6.0f - 15.0f) + 10.0f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 var0) {
        if (CinematicOverlay.visible()) {
            class_310 var1 = class_310.method_1551();
            class_1041 var2 = var1.method_22683();
            int var3 = var2.method_4489();
            int var4 = var2.method_4506();
            float var5 = var2.method_4495();
            var0.method_51448().pushMatrix();
            var0.method_51448().scale(1.0f / var5, 1.0f / var5);
            try {
                float var20;
                MinecraftUiRenderer var6 = new MinecraftUiRenderer(var0, var5);
                float var7 = CinematicOverlay.smooth(vignette);
                if (var7 > 0.001f) {
                    float var8 = Math.max(var3, var4);
                    var6.glow(0.0f, 0.0f, var8 * 0.55f, UiRenderer.argb(0.42f * var7, 0, 0, 0));
                    var6.glow(var3, 0.0f, var8 * 0.55f, UiRenderer.argb(0.42f * var7, 0, 0, 0));
                    var6.glow(0.0f, var4, var8 * 0.55f, UiRenderer.argb(0.42f * var7, 0, 0, 0));
                    var6.glow(var3, var4, var8 * 0.55f, UiRenderer.argb(0.42f * var7, 0, 0, 0));
                }
                if ((var20 = CinematicOverlay.smooth(bars)) > 0.001f) {
                    float var9 = (float)var4 * 0.105f * var20;
                    float var10 = (float)var4 * 0.02f;
                    var6.rect(0.0f, 0.0f, var3, var9, UiRenderer.argb(0.97f, 3, 2, 5));
                    var6.gradient(0.0f, var9, var3, var10, UiRenderer.argb(0.55f * var20, 3, 2, 5), UiRenderer.argb(0.0f, 3, 2, 5));
                    var6.rect(0.0f, (float)var4 - var9, var3, var9, UiRenderer.argb(0.97f, 3, 2, 5));
                    var6.gradient(0.0f, (float)var4 - var9 - var10, var3, var10, UiRenderer.argb(0.0f, 3, 2, 5), UiRenderer.argb(0.55f * var20, 3, 2, 5));
                }
                if (dip >= 0.0f) {
                    float var21 = (float)Math.sin(Math.PI * (double)Math.min(1.0f, dip));
                    var6.rect(0.0f, 0.0f, var3, var4, UiRenderer.argb(0.72f * var21 * var21, 2, 1, 4));
                }
                if (notice > 0.0f) {
                    float var22 = Math.min(1.0f, notice / 0.4f) * Math.min(1.0f, (2.2f - notice) / 0.25f);
                    float var23 = (float)var4 / 1080.0f;
                    float var11 = 15.0f * var23;
                    float var12 = var6.textWidth(noticeText, var11, 0);
                    float var13 = var12 + 64.0f * var23;
                    float var14 = 40.0f * var23;
                    float var15 = ((float)var3 - var13) / 2.0f;
                    float var16 = (float)var4 * 0.13f + (1.0f - var22) * 8.0f * var23;
                    var6.pushAlpha(var22);
                    var6.shadow(var15, var16, var13, var14, var14 / 2.0f, 16.0f * var23, UiRenderer.argb(0.45f, 0, 0, 0));
                    var6.round(var15, var16, var13, var14, var14 / 2.0f, UiRenderer.argb(0.86f, 24, 16, 28));
                    var6.outline(var15, var16, var13, var14, var14 / 2.0f, Math.max(1.0f, var23), UiRenderer.argb(0.5f, 255, 150, 70));
                    var6.icon(UiIcon.MONITOR, var15 + 18.0f * var23, var16 + (var14 - 18.0f * var23) / 2.0f, 18.0f * var23, UiRenderer.argb(0.9f, 255, 170, 90));
                    var6.text(noticeText, var15 + 46.0f * var23, var16 + (var14 - var11) / 2.0f - var23, var11, 0, UiRenderer.argb(1.0f, 240, 234, 232));
                    var6.popAlpha();
                }
            }
            finally {
                var0.method_51448().popMatrix();
            }
        }
    }

    static {
        hudWanted = true;
        hudShown = true;
        dip = -1.0f;
        noticeText = "";
    }
}

