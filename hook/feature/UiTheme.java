package hook.feature;

/**
 * The client menu's look as reusable pieces (same values as {@link HookUiController}): tinted backdrop with the
 * theme glow, glass panels, the header with its icon tile, cards and small icon buttons. Used by the Quick Utilities
 * wheel and its utilities so they look exactly like the rest of the client.
 */
final class UiTheme {
    static final int TEXT = UiRenderer.argb(1.0f, 244, 245, 247);
    static final int SUB = UiRenderer.white(0.56f);
    static final int FAINT = UiRenderer.white(0.34f);

    private UiTheme() {
    }

    /** Same unit as the client menu: 1.0 at 1080p, smaller on small windows. */
    static float unit(int sw, int sh) {
        return Math.max(0.45f, Math.min(sh / 1080.0f, Math.min((sw - 40) / 1000.0f, (sh - 40) / 700.0f)));
    }

    static float time() {
        return (System.nanoTime() % 3_600_000_000_000L) / 1.0e9f;
    }

    /** The menu's background: dark tint, theme gradient and slowly drifting theme lights. */
    static void backdrop(UiRenderer ui, int sw, int sh, float vis) {
        float t = time();
        ui.pushAlpha(vis);
        float pad = sh * 0.04f;
        ui.rect(-pad, -pad, sw + 2 * pad, sh + 2 * pad, UiRenderer.argb(0.38f, 5, 6, 8));
        ui.gradient(-pad, -pad, sw + 2 * pad, sh + 2 * pad, UiPalette.bgTop(0.2f), UiPalette.bgBottom(0.5f));
        float big = Math.max(sw, sh);
        float light = UiPalette.lightAmount();
        float mt = UiPalette.matte();
        if (mt > 0.001f) ui.rect(-pad, -pad, sw + 2 * pad, sh + 2 * pad, UiRenderer.argb(mt, 11, 11, 12));
        float k = (UiPalette.selected() == UiPalette.Hook ? 0.35f : 1.0f) * (1.0f - mt);
        ui.glow(sw * (0.28f + 0.16f * (float) Math.sin(t * 0.031)), sh * (0.3f + 0.14f * (float) Math.cos(t * 0.026)), big * 0.62f, UiPalette.light(0.05f * light * k));
        ui.glow(sw * (0.74f + 0.14f * (float) Math.cos(t * 0.022 + 1.7)), sh * (0.66f + 0.16f * (float) Math.sin(t * 0.028 + 0.6)), big * 0.58f,
            UiEasing.mix(UiRenderer.argb(0.06f * k, 190, 195, 205), UiPalette.light(0.06f * light * k), 0.75f));
        ui.glow(sw * (0.5f + 0.3f * (float) Math.sin(t * 0.017 + 3.1)), sh * (0.5f + 0.24f * (float) Math.sin(t * 0.021 + 2.2)), big * 0.4f, UiRenderer.white(0.025f * k));
        ui.popAlpha();
    }

    /** A glass panel exactly like the menu's panels. */
    static void glass(UiRenderer ui, float x, float y, float w, float h, float r, float u, float seed) {
        float t = time();
        ui.shadow(x, y, w, h, r, 30 * u, UiRenderer.argb(0.42f, 0, 0, 0));
        ui.round(x, y, w, h, r, UiPalette.glass(0.6f));
        ui.roundGradient(x, y, w, Math.min(h, Math.max(h * 0.5f, Math.min(h, 150 * u))), r, UiRenderer.white(0.05f), UiRenderer.white(0.0f));
        float gr = Math.min(w, h) * 0.5f;
        float gx = x + gr + (w - 2 * gr) * (0.5f + 0.5f * (float) Math.sin(t * 0.09f + seed));
        float gy = y + gr + (h - 2 * gr) * (0.35f + 0.35f * (float) Math.cos(t * 0.07f + seed * 1.3f));
        ui.glow(gx, gy, gr, UiEasing.mix(UiRenderer.white(0.04f), UiPalette.light(0.05f * UiPalette.lightAmount()), 0.7f));
        ui.outline(x, y, w, h, r, Math.max(1.0f, u * 0.9f), UiRenderer.white(0.085f));
        ui.rect(x + r, y + Math.max(1.0f, u), w - 2 * r, Math.max(1.0f, u * 0.8f), UiRenderer.white(0.05f));
    }

    /** Round glass disc (for the wheel), same layers as {@link #glass}. */
    static void glassDisc(UiRenderer ui, float cx, float cy, float rad, float u) {
        float t = time();
        ui.glow(cx, cy + 10 * u, rad + 34 * u, UiRenderer.argb(0.38f, 0, 0, 0));
        ui.round(cx - rad, cy - rad, rad * 2, rad * 2, rad, UiPalette.glass(0.6f));
        ui.roundGradient(cx - rad, cy - rad, rad * 2, rad, rad, UiRenderer.white(0.05f), UiRenderer.white(0.0f));
        ui.glow(cx + rad * 0.35f * (float) Math.sin(t * 0.09f), cy - rad * 0.3f * (float) Math.cos(t * 0.07f), rad * 0.7f,
            UiEasing.mix(UiRenderer.white(0.04f), UiPalette.light(0.05f * UiPalette.lightAmount()), 0.7f));
        ui.outline(cx - rad, cy - rad, rad * 2, rad * 2, rad, Math.max(1.0f, u * 0.9f), UiRenderer.white(0.085f));
    }

    /** Panel header: icon tile, title, subtitle and the thin divider below (78 units high). */
    static void header(UiRenderer ui, float x, float y, float w, UiIcon icon, String title, String sub, float u) {
        float tile = 44 * u, tx = x + 22 * u, ty = y + 18 * u;
        ui.round(tx, ty, tile, tile, 14 * u, UiRenderer.white(0.07f));
        ui.outline(tx, ty, tile, tile, 14 * u, Math.max(1.0f, u * 0.9f), UiRenderer.white(0.09f));
        float is = 21 * u;
        ui.icon(icon, tx + (tile - is) / 2, ty + (tile - is) / 2, is, TEXT);
        ui.text(title, tx + tile + 16 * u, ty + u, 21 * u, 1, TEXT);
        ui.text(ui.ellipsize(sub, w - 150 * u, 12 * u, 0), tx + tile + 17 * u, ty + 27 * u, 12 * u, 0, SUB);
        ui.rect(x + 20 * u, y + 78 * u, w - 40 * u, Math.max(1.0f, u), UiRenderer.white(0.055f));
    }

    /** A module-card style tile; hover 0..1, on 0..1 (accent tint like an enabled module). */
    static void card(UiRenderer ui, float x, float y, float w, float h, float r, float hover, float on, float u) {
        if (hover > 0.01f) ui.shadow(x, y, w, h, r, 16 * u, UiRenderer.white(0.035f * hover));
        ui.round(x, y, w, h, r, UiRenderer.white(0.035f + 0.03f * hover));
        if (on > 0.01f) ui.round(x, y, w, h, r, UiEasing.mix(UiRenderer.white(0.035f * on), UiPalette.accent(0.05f * on), 0.6f));
        ui.outline(x, y, w, h, r, Math.max(1.0f, u * 0.9f),
            UiEasing.mix(UiRenderer.white(0.06f + 0.08f * hover + 0.06f * on), UiPalette.accent(0.06f + 0.08f * hover + 0.1f * on), 0.5f * on));
    }

    /** Small square icon button like the gear on module cards. */
    static void iconButton(UiRenderer ui, UiIcon icon, float x, float y, float size, float hover, float u) {
        ui.round(x, y, size, size, 11 * u, UiRenderer.white(0.035f + 0.07f * hover));
        float is = size * 0.53f;
        ui.icon(icon, x + (size - is) / 2, y + (size - is) / 2, is, UiRenderer.white(0.42f + 0.5f * hover));
    }

    /** The pulsing accent dot used next to active things. */
    static void accentDot(UiRenderer ui, float x, float y, float u, float amount) {
        float p = 0.55f + 0.45f * (float) Math.sin(time() * 2.2f);
        ui.glow(x, y, 9 * u, UiPalette.accent(0.22f * p * amount));
        ui.circle(x, y, 3 * u, UiPalette.accent(0.9f * amount));
    }
}
