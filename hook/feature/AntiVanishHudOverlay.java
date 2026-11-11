/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package hook.feature;

import hook.feature.AntiVanishModule;
import hook.feature.MinecraftUiRenderer;
import hook.feature.PlayerWatchHud;
import hook.feature.UiPalette;
import hook.feature.UiRenderer;
import java.util.List;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class AntiVanishHudOverlay {
    private static final int WARN = -22016;
    private static float shown;
    private static long last;

    private AntiVanishHudOverlay() {
    }

    public static void render(class_332 var0) {
        try {
            PlayerWatchHud.render(var0);
            AntiVanishHudOverlay.drawVanish(var0);
        }
        catch (Throwable var2) {
            System.err.println("Hook: HUD element failed: " + String.valueOf(var2));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void drawVanish(class_332 var0) {
        List<AntiVanishModule.VanishHudEntry> var1 = AntiVanishModule.hudEntries();
        long var2 = System.nanoTime();
        float var4 = last == 0L ? 0.016f : Math.min(0.1f, (float)(var2 - last) / 1.0E9f);
        last = var2;
        if (!((shown += ((var1.isEmpty() ? 0.0f : 1.0f) - shown) * (1.0f - (float)Math.exp(-10.0f * var4))) < 0.01f) && !var1.isEmpty()) {
            class_310 var5 = class_310.method_1551();
            class_1041 var6 = var5.method_22683();
            float var7 = var6.method_4495();
            float var8 = Math.max(0.6f, (float)var6.method_4506() / 1080.0f);
            var0.method_51448().pushMatrix();
            var0.method_51448().scale(1.0f / var7, 1.0f / var7);
            try {
                MinecraftUiRenderer var9 = new MinecraftUiRenderer(var0, var7);
                float var10 = 13.0f * var8;
                float var11 = 11.0f * var8;
                float var12 = 12.0f * var8;
                float var13 = 24.0f * var8;
                float var14 = 0.0f;
                float var15 = 0.0f;
                for (AntiVanishModule.VanishHudEntry var17 : var1) {
                    var14 = Math.max(var14, var9.textWidth(AntiVanishModule.hudTag(var17), var11, 1));
                    var15 = Math.max(var15, var9.textWidth(AntiVanishModule.hudValue(var17), var10, 0));
                }
                float var28 = var12 * 2.0f + var14 + 10.0f * var8 + var15;
                float var29 = var13 * (float)var1.size() + 8.0f * var8;
                float var18 = 12.0f * var8 - (1.0f - shown) * 14.0f * var8;
                float var19 = (float)var6.method_4506() * 0.3f;
                var9.pushAlpha(shown);
                var9.shadow(var18, var19, var28, var29, 12.0f * var8, 16.0f * var8, UiRenderer.argb(0.35f, 0, 0, 0));
                var9.round(var18, var19, var28, var29, 12.0f * var8, UiPalette.glass(0.78f));
                var9.outline(var18, var19, var28, var29, 12.0f * var8, Math.max(1.0f, var8 * 0.9f), UiRenderer.white(0.09f));
                var9.round(var18 + 4.0f * var8, var19 + var29 * 0.25f, 3.0f * var8, var29 * 0.5f, 1.5f * var8, UiPalette.accent(0.95f));
                for (int var20 = 0; var20 < var1.size(); ++var20) {
                    AntiVanishModule.VanishHudEntry var21 = var1.get(var20);
                    String var22 = AntiVanishModule.hudTag(var21);
                    boolean var23 = "ALERT".equals(var22) || "VANISH".equals(var22);
                    float var24 = var19 + 4.0f * var8 + (float)var20 * var13;
                    var9.text(var22, var18 + var12, var24 + (var13 - var11) / 2.0f, var11, 1, var23 ? -22016 : UiRenderer.white(0.55f));
                    var9.text(AntiVanishModule.hudValue(var21), var18 + var12 + var14 + 10.0f * var8, var24 + (var13 - var10) / 2.0f - var8, var10, 0, UiRenderer.white(0.95f));
                }
                var9.popAlpha();
            }
            finally {
                var0.method_51448().popMatrix();
            }
        }
    }
}

