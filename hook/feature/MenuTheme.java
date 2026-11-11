package hook.feature;

import hook.module.modules.client.Hook;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.class_1041;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

/**
 * HookVisuals look for every menu shown before you join a world: animated themed background, the
 * HK logo on the title screen, and rounded "glass" versions of the vanilla widget sprites
 * (buttons, sliders, text fields, checkboxes, scrollbars, list panels).
 *
 * It works on the sprite level, so buttons added by other mods (Mod Menu, ...) get the same style
 * as long as they use the vanilla widget sprites; mods with their own textures keep them.
 * All colours come from the selected client theme (UiPalette), including its fade on change.
 */
public final class MenuTheme {
    private static final Map<Long, float[]> HOVER = new HashMap<>();
    private static final float[][] STARS = new float[70][5];
    private static long lastFrame;
    private static long lastPrune;
    private static float dt = 0.016f;
    private static float time;

    static {
        Random random = new Random(1337);
        for (float[] star : STARS) {
            star[0] = random.nextFloat();
            star[1] = random.nextFloat();
            star[2] = 0.6f + random.nextFloat() * 1.6f;
            star[3] = random.nextFloat() * 100.0f;
            star[4] = 0.003f + random.nextFloat() * 0.008f;
        }
    }

    private MenuTheme() {
    }

    /** True while the themed menus should replace vanilla visuals (no world loaded, setting on). */
    public static boolean active() {
        try {
            class_310 mc = class_310.method_1551();
            return mc != null && mc.field_1687 == null && Hook.customMenus.getValue();
        } catch (Throwable t) {
            return false;
        }
    }

    private static float scale() {
        class_1041 window = class_310.method_1551().method_22683();
        return Math.max(1.0f, window.method_4495());
    }

    private static void tick() {
        long now = System.nanoTime();
        if (lastFrame != now) {
            dt = lastFrame == 0L ? 0.016f : Math.min(0.1f, (now - lastFrame) / 1.0E9f);
            lastFrame = now;
            time += dt;
        }
        if (now - lastPrune > 5_000_000_000L) {
            lastPrune = now;
            HOVER.entrySet().removeIf(e -> now - (long) e.getValue()[1] > 2_000_000_000L);
        }
    }

    /** Runs the drawing with the GUI matrix scaled to real pixels, like the client's own UI. */
    private static void draw(class_332 ctx, java.util.function.BiConsumer<MinecraftUiRenderer, Float> painter) {
        float s = scale();
        ctx.method_51448().pushMatrix();
        ctx.method_51448().scale(1.0f / s, 1.0f / s);
        try {
            painter.accept(new MinecraftUiRenderer(ctx, s), s);
        } finally {
            ctx.method_51448().popMatrix();
        }
    }

    // ------------------------------------------------------------------ background

    public static void background(class_332 ctx) {
        tick();
        int w = ctx.method_51421();
        int h = ctx.method_51443();
        draw(ctx, (ui, s) -> {
            float sw = w * s;
            float sh = h * s;
            float big = Math.max(sw, sh);
            float light = UiPalette.lightAmount();
            float calm = UiPalette.selected() == UiPalette.Hook ? 0.45f : 1.0f;
            ui.rect(0, 0, sw, sh, UiRenderer.argb(1.0f, 7, 8, 11));
            ui.gradient(0, 0, sw, sh, UiPalette.bgTop(0.95f), UiPalette.bgBottom(1.0f));
            ui.glow(sw * (0.25f + 0.14f * (float) Math.sin(time * 0.11)), sh * (0.28f + 0.12f * (float) Math.cos(time * 0.09)), big * 0.7f, UiPalette.light(0.10f * light * calm));
            ui.glow(sw * (0.78f + 0.12f * (float) Math.cos(time * 0.08 + 1.7)), sh * (0.7f + 0.14f * (float) Math.sin(time * 0.1 + 0.6)), big * 0.62f, UiPalette.accent(0.09f * calm));
            ui.glow(sw * (0.5f + 0.25f * (float) Math.sin(time * 0.06 + 3.1)), sh * (0.45f + 0.2f * (float) Math.sin(time * 0.075 + 2.2)), big * 0.45f, UiRenderer.white(0.03f));
            for (float[] star : STARS) {
                star[1] -= star[4] * dt;
                if (star[1] < -0.02f) {
                    star[1] = 1.02f;
                }
                float twinkle = 0.5f + 0.5f * (float) Math.sin(time * 0.9f + star[3]);
                float x = star[0] * sw + (float) Math.sin(time * 0.3f + star[3]) * 6.0f * s;
                float y = star[1] * sh;
                ui.glow(x, y, star[2] * 5.0f * s, UiPalette.light(0.05f * twinkle));
                ui.circle(x, y, Math.max(1.0f, star[2] * s * 0.6f), UiEasing.mix(UiRenderer.white(0.35f * twinkle), UiPalette.light(0.5f * twinkle), 0.4f));
            }
            // soft vignette
            ui.gradient(0, 0, sw, sh * 0.25f, UiRenderer.argb(0.35f, 0, 0, 0), UiRenderer.argb(0.0f, 0, 0, 0));
            ui.gradient(0, sh * 0.7f, sw, sh * 0.3f, UiRenderer.argb(0.0f, 0, 0, 0), UiRenderer.argb(0.45f, 0, 0, 0));
        });
    }

    // ------------------------------------------------------------------ title logo

    public static void logo(class_332 ctx, int screenWidth, float alpha) {
        tick();
        draw(ctx, (ui, s) -> {
            ui.pushAlpha(alpha);
            float mark = 50.0f * s;
            float word = 30.0f * s;
            float track = 2.2f * s;              // letter spacing of the wordmark
            float hookW = spaced(ui, "HOOK", word, 1, track);
            float visW = spaced(ui, "VISUALS", word, 1, track);
            float space = 7.0f * s;
            float wordW = hookW + space + visW;
            float gap = 14.0f * s;
            float total = mark + gap + wordW;
            float cx = screenWidth * s / 2.0f;
            float x = cx - total / 2.0f;
            float y = 20.0f * s;
            float pulse = 0.5f + 0.5f * (float) Math.sin(time * 1.4f);

            // ambient light behind the whole logo
            ui.glow(cx, y + mark / 2.0f, total * 0.8f, UiPalette.light(0.07f * UiPalette.lightAmount()));
            ui.glow(x + mark / 2.0f, y + mark / 2.0f, mark * 1.6f, UiPalette.accent(0.16f + 0.06f * pulse));

            // emblem: theme gradient tile, dark inset, "HK", accent ring and a moving shine
            float r = mark * 0.28f;
            ui.shadow(x, y + 2.0f * s, mark, mark, r, 12.0f * s, UiRenderer.argb(0.45f, 0, 0, 0));
            ui.roundGradient(x, y, mark, mark, r, UiPalette.logoTop(1.0f), UiPalette.logoBottom(1.0f));
            float in = 3.2f * s;
            ui.roundGradient(x + in, y + in, mark - 2 * in, mark - 2 * in, r - in * 0.6f, UiRenderer.argb(0.92f, 18, 20, 26), UiRenderer.argb(0.96f, 8, 9, 12));
            ui.pushClip(x + in, y + in, mark - 2 * in, mark - 2 * in);
            float sweep = ((time * 0.35f) % 1.6f) - 0.3f;
            ui.glow(x + mark * sweep, y + mark * 0.3f, mark * 0.55f, UiRenderer.white(0.07f));
            ui.popClip();
            float hk = mark * 0.42f;
            float hkY = y + (mark - hk) / 2.0f - hk * 0.06f;
            ui.glow(x + mark / 2.0f, y + mark / 2.0f, mark * 0.45f, UiPalette.accent(0.18f));
            ui.textCentered("HK", x + mark / 2.0f, hkY, hk, 1, UiEasing.mix(UiRenderer.white(0.97f), UiPalette.light(1.0f), 0.25f));
            ui.round(x + mark * 0.3f, y + mark - in - 6.0f * s, mark * 0.4f, Math.max(1.0f, 1.6f * s), 1.0f * s, UiPalette.accent(0.95f));
            ui.outline(x, y, mark, mark, r, Math.max(1.0f, 1.1f * s), UiEasing.mix(UiRenderer.white(0.35f), UiPalette.accent(0.9f), 0.5f + 0.3f * pulse));

            // wordmark: HOOK in white, VISUALS in the theme colour, with soft shadow
            float tx = x + mark + gap;
            float ty = y + (mark - word) / 2.0f - 5.0f * s;
            spacedText(ui, "HOOK", tx + 1.5f * s, ty + 2.0f * s, word, 1, track, UiRenderer.argb(0.5f, 0, 0, 0));
            spacedText(ui, "VISUALS", tx + hookW + space + 1.5f * s, ty + 2.0f * s, word, 1, track, UiRenderer.argb(0.5f, 0, 0, 0));
            spacedText(ui, "HOOK", tx, ty, word, 1, track, UiRenderer.white(0.97f));
            ui.glow(tx + hookW + space + visW / 2.0f, ty + word / 2.0f, visW * 0.6f, UiPalette.accent(0.08f));
            spacedText(ui, "VISUALS", tx + hookW + space, ty, word, 1, track, UiEasing.mix(UiPalette.accent(1.0f), UiRenderer.white(1.0f), 0.18f));

            // accent line fading out to both ends, then the subtitle
            float ly = ty + word + 6.0f * s;
            float lh = Math.max(1.0f, 1.2f * s);
            int steps = 24;
            for (int i = 0; i < steps; i++) {
                float t = (i + 0.5f) / steps;
                float fade = 1.0f - Math.abs(t - 0.5f) * 2.0f;
                ui.rect(tx + wordW * i / steps, ly, wordW / steps + 0.5f, lh, UiPalette.accent(0.75f * fade * fade));
            }
            spacedText(ui, "MINECRAFT 1.21.11  ·  CLIENT", tx, ly + 5.0f * s, 8.5f * s, 0, 1.6f * s, UiEasing.mix(UiRenderer.white(0.5f), UiPalette.accent(0.7f), 0.35f));
            ui.popAlpha();
        });
    }

    private static float spaced(UiRenderer ui, String text, float size, int weight, float tracking) {
        float w = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            w += ui.textWidth(String.valueOf(text.charAt(i)), size, weight) + (i > 0 ? tracking : 0.0f);
        }
        return w;
    }

    private static void spacedText(UiRenderer ui, String text, float x, float y, float size, int weight, float tracking, int color) {
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            ui.text(ch, x, y, size, weight, color);
            x += ui.textWidth(ch, size, weight) + tracking;
        }
    }

    // ------------------------------------------------------------------ widget sprites

    /** @return true when the sprite was drawn by the theme (vanilla drawing must be skipped). */
    public static boolean sprite(class_332 ctx, class_2960 id, int x, int y, int w, int h, float alpha) {
        if (!"minecraft".equals(id.method_12836())) return false;
        String path = id.method_12832();
        if (!path.startsWith("widget/")) return false;
        String name = path.substring(7);
        Kind kind = Kind.of(name);
        if (kind == null || w <= 0 || h <= 0) return false;
        tick();
        boolean hot = name.endsWith("_highlighted");
        boolean disabled = name.endsWith("_disabled");
        boolean selected = name.startsWith("checkbox_selected");
        float hover = animate(x, y, w, h, kind, hot ? 1.0f : 0.0f);
        draw(ctx, (ui, s) -> {
            ui.pushAlpha(alpha);
            float px = x * s, py = y * s, pw = w * s, ph = h * s;
            switch (kind) {
                case BUTTON -> button(ui, s, px, py, pw, ph, hover, disabled);
                case SLIDER -> track(ui, s, px, py, pw, ph, hover);
                case HANDLE -> handle(ui, s, px, py, pw, ph, hover);
                case FIELD -> field(ui, s, px, py, pw, ph, hover);
                case CHECKBOX -> checkbox(ui, s, px, py, pw, ph, hover, selected);
                case SCROLLER -> ui.round(px + pw * 0.2f, py, pw * 0.6f, ph, pw * 0.3f, UiEasing.mix(UiRenderer.white(0.28f), UiPalette.accent(0.85f), 0.35f + 0.5f * hover));
                case SCROLL_TRACK -> ui.round(px + pw * 0.2f, py, pw * 0.6f, ph, pw * 0.3f, UiRenderer.white(0.05f));
            }
            ui.popAlpha();
        });
        return true;
    }

    private enum Kind {
        BUTTON, SLIDER, HANDLE, FIELD, CHECKBOX, SCROLLER, SCROLL_TRACK;

        static Kind of(String name) {
            return switch (name) {
                case "button", "button_highlighted", "button_disabled" -> BUTTON;
                case "slider", "slider_highlighted" -> SLIDER;
                case "slider_handle", "slider_handle_highlighted" -> HANDLE;
                case "text_field", "text_field_highlighted" -> FIELD;
                case "checkbox", "checkbox_highlighted", "checkbox_selected", "checkbox_selected_highlighted" -> CHECKBOX;
                case "scroller" -> SCROLLER;
                case "scroller_background" -> SCROLL_TRACK;
                default -> null;
            };
        }
    }

    private static float animate(int x, int y, int w, int h, Kind kind, float target) {
        long key = ((long) (x & 0xFFFF) << 48) | ((long) (y & 0xFFFF) << 32) | ((long) (w & 0xFFFF) << 16) | ((long) (h & 0xFFF) << 4) | kind.ordinal();
        float[] state = HOVER.computeIfAbsent(key, k -> new float[]{target, 0});
        state[0] += (target - state[0]) * (1.0f - (float) Math.exp(-14.0f * dt));
        state[1] = System.nanoTime();
        return state[0];
    }

    private static void button(UiRenderer ui, float s, float x, float y, float w, float h, float hover, boolean disabled) {
        float r = Math.min(h / 2.0f, 3.0f * s);
        float line = Math.max(1.0f, s * 0.5f);
        if (disabled) {
            ui.round(x, y, w, h, r, UiRenderer.argb(0.28f, 0, 0, 0));
            ui.outline(x, y, w, h, r, line, UiRenderer.white(0.05f));
            return;
        }
        // dark glass, thin light edge on top; the theme accent shows up on hover
        ui.round(x, y, w, h, r, UiEasing.mix(UiRenderer.argb(0.55f, 6, 7, 10), UiEasing.mix(UiRenderer.argb(0.62f, 10, 11, 15), UiPalette.accent(0.30f), 0.45f), hover));
        ui.rect(x + r, y + line, w - 2.0f * r, line, UiRenderer.white(0.07f + 0.05f * hover));
        if (hover > 0.01f) {
            ui.round(x + w * 0.2f, y + h - line * 2.0f, w * 0.6f, line * 2.0f, line, UiPalette.accent(0.9f * hover));
        }
        ui.outline(x, y, w, h, r, line, UiEasing.mix(UiRenderer.white(0.11f), UiPalette.accent(0.75f), hover));
    }

    private static void track(UiRenderer ui, float s, float x, float y, float w, float h, float hover) {
        float r = Math.min(h / 2.0f, 4.0f * s);
        ui.round(x, y, w, h, r, UiEasing.mix(UiPalette.glass(0.6f), UiRenderer.white(0.08f), 0.5f));
        ui.outline(x, y, w, h, r, Math.max(1.0f, s * 0.6f), UiEasing.mix(UiRenderer.white(0.12f), UiPalette.accent(0.6f), hover));
    }

    private static void handle(UiRenderer ui, float s, float x, float y, float w, float h, float hover) {
        float inset = 1.5f * s;
        float hw = Math.max(w - 2 * inset, 3.0f * s);
        float hx = x + (w - hw) / 2.0f;
        float hy = y + inset;
        float hh = h - 2 * inset;
        ui.glow(hx + hw / 2.0f, y + h / 2.0f, h * 0.9f, UiPalette.accent(0.14f + 0.12f * hover));
        ui.round(hx, hy, hw, hh, Math.min(hw / 2.0f, 2.5f * s), UiEasing.mix(UiPalette.accent(0.95f), UiRenderer.white(0.95f), 0.25f + 0.35f * hover));
    }

    private static void field(UiRenderer ui, float s, float x, float y, float w, float h, float hover) {
        float r = Math.min(h / 2.0f, 4.0f * s);
        ui.round(x, y, w, h, r, UiEasing.mix(UiPalette.glass(0.85f), UiRenderer.argb(0.85f, 8, 9, 12), 0.4f));
        ui.outline(x, y, w, h, r, Math.max(1.0f, s * 0.6f), UiEasing.mix(UiRenderer.white(0.14f), UiPalette.accent(0.9f), hover));
    }

    private static void checkbox(UiRenderer ui, float s, float x, float y, float w, float h, float hover, boolean selected) {
        float r = Math.min(w, h) * 0.3f;
        ui.round(x, y, w, h, r, selected ? UiPalette.accent(0.85f) : UiEasing.mix(UiPalette.glass(0.7f), UiRenderer.white(0.12f), 0.5f));
        ui.outline(x, y, w, h, r, Math.max(1.0f, s * 0.6f), UiEasing.mix(UiRenderer.white(0.18f), UiPalette.accent(0.9f), Math.max(hover, selected ? 1.0f : 0.0f)));
        if (selected) {
            ui.circle(x + w / 2.0f, y + h / 2.0f, Math.min(w, h) * 0.18f, UiRenderer.white(0.95f));
        }
    }

    // ------------------------------------------------------------------ lists and separators

    /** Glass panel behind scrolling lists (server list, world list, options lists). */
    public static void listPanel(class_332 ctx, int x, int y, int w, int h) {
        tick();
        draw(ctx, (ui, s) -> {
            float px = x * s + 6 * s, py = y * s + 2 * s, pw = w * s - 12 * s, ph = h * s - 4 * s;
            float r = 6.0f * s;
            ui.round(px, py, pw, ph, r, UiPalette.glass(0.42f));
            ui.outline(px, py, pw, ph, r, Math.max(1.0f, s * 0.6f), UiRenderer.white(0.07f));
        });
    }

    /** Thin accent lines instead of the dirt-style header/footer separators. */
    public static void separators(class_332 ctx, int x, int top, int bottom, int w) {
        tick();
        draw(ctx, (ui, s) -> {
            float line = Math.max(1.0f, s * 0.6f);
            float inset = w * s * 0.08f;
            int color = UiEasing.mix(UiRenderer.white(0.10f), UiPalette.accent(0.45f), 0.6f);
            ui.round(x * s + inset, top * s - line, w * s - 2 * inset, line, line / 2.0f, color);
            ui.round(x * s + inset, bottom * s, w * s - 2 * inset, line, line / 2.0f, color);
        });
    }
}
