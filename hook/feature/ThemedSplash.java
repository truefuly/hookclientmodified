package hook.feature;

import net.minecraft.class_310;
import net.minecraft.class_332;

/**
 * Resource-loading screen in the client's style: the selected lobby theme plays in the background, with the
 * HookVisuals title and a progress bar in the client accent colour on top. In a world (server resource packs,
 * F3+T) the background stays see-through so the game remains visible. The Standard lobby theme keeps the
 * vanilla Mojang screen.
 */
public final class ThemedSplash {
    private static boolean failed;
    private static float shown;

    private ThemedSplash() {
    }

    public static boolean active() {
        if (failed) return false;
        try {
            return LobbyTheme.selected().folder != null;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * @param alpha    opacity of the whole screen (fades in over the old screen and out at the end)
     * @param progress smoothed reload progress 0..1
     * @param barAlpha opacity of the progress bar and texts (fades out once loading is done)
     * @return false if drawing failed (vanilla takes over from then on)
     */
    public static boolean render(class_332 ctx, float alpha, float progress, float barAlpha, boolean seeThrough) {
        try {
            draw(ctx, alpha, progress, barAlpha, seeThrough);
            return true;
        } catch (Throwable t) {
            failed = true;
            System.err.println("Hook: themed loading screen disabled: " + t);
            return false;
        }
    }

    private static void draw(class_332 ctx, float alpha, float progress, float barAlpha, boolean seeThrough) {
        if (alpha <= 0.0f) return;
        if (seeThrough) {
            // in a world: only a light tint, the game stays visible behind the loading bar
            ctx.method_25294(0, 0, ctx.method_51421(), ctx.method_51443(), UiRenderer.argb(0.30f * alpha, 6, 7, 10));
        } else {
            // background: the lobby theme, faded with the overlay
            ctx.method_25294(0, 0, ctx.method_51421(), ctx.method_51443(), UiRenderer.argb(alpha, 9, 10, 14));
            MenuBackground.render(ctx, alpha);
        }

        class_310 mc = class_310.method_1551();
        float s = Math.max(1.0f, (float) mc.method_22683().method_4495());
        int sw = mc.method_22683().method_4489();
        int sh = mc.method_22683().method_4506();
        float u = Math.max(0.75f, sh / 1080.0f);

        ctx.method_51448().pushMatrix();
        ctx.method_51448().scale(1.0f / s, 1.0f / s);
        try {
            MinecraftUiRenderer ui = new MinecraftUiRenderer(ctx, s);
            // soft dark band behind the texts so they read on every theme
            ui.gradient(0, sh * 0.30f, sw, sh * 0.20f, UiRenderer.argb(0.0f, 0, 0, 0), UiRenderer.argb(0.45f * alpha, 6, 7, 10));
            ui.rect(0, sh * 0.50f, sw, sh * 0.16f, UiRenderer.argb(0.45f * alpha, 6, 7, 10));
            ui.gradient(0, sh * 0.66f, sw, sh * 0.20f, UiRenderer.argb(0.45f * alpha, 6, 7, 10), UiRenderer.argb(0.0f, 0, 0, 0));

            float a = alpha * barAlpha;
            if (a <= 0.01f) return;
            ui.pushAlpha(a);
            try {
                float cx = sw / 2.0f;
                float titleSize = 44.0f * u;
                float ty = sh * 0.50f;
                ui.textCentered("HookVisuals", cx, ty, titleSize, 1, UiRenderer.white(0.97f));
                // small accent underline below the title
                float lineW = 64.0f * u;
                ui.round(cx - lineW / 2.0f, ty + titleSize + 10.0f * u, lineW, 3.0f * u, 1.5f * u, UiPalette.accent(0.95f));

                float barW = Math.min(sw * 0.42f, 620.0f * u);
                float barH = 6.0f * u;
                float bx = cx - barW / 2.0f;
                float by = ty + titleSize + 44.0f * u;
                // eased value so the bar glides instead of jumping between reload steps
                shown += (progress - shown) * 0.25f;
                if (progress < shown - 0.5f) shown = progress;
                float p = Math.max(0.0f, Math.min(1.0f, shown));
                ui.round(bx, by, barW, barH, barH / 2.0f, UiRenderer.white(0.12f));
                if (p > 0.0f) {
                    float fw = Math.max(barH, barW * p);
                    ui.shadow(bx, by, fw, barH, barH / 2.0f, 14.0f * u, UiPalette.accent(0.35f));
                    ui.round(bx, by, fw, barH, barH / 2.0f, UiPalette.accent(1.0f));
                }
                String status = p >= 0.999f ? "Ready" : "Loading resources";
                float labelSize = 13.0f * u;
                ui.text(status, bx, by + barH + 12.0f * u, labelSize, 0, UiRenderer.white(0.62f));
                String pct = Math.round(p * 100.0f) + "%";
                ui.text(pct, bx + barW - ui.textWidth(pct, labelSize, 1), by + barH + 12.0f * u, labelSize, 1, UiRenderer.white(0.9f));

                String theme = LobbyTheme.selected().title;
                float small = 11.0f * u;
                ui.text(theme, 24.0f * u, sh - 24.0f * u - small, small, 0, UiRenderer.white(0.45f));
            } finally {
                ui.popAlpha();
            }
        } finally {
            ctx.method_51448().popMatrix();
        }
    }
}
