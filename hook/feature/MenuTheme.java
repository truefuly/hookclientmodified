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
 * Custom widgets for every menu shown before you join a world: dark glass versions of the vanilla
 * widget sprites (buttons, sliders, text fields, checkboxes, scrollbars) in neutral colours.
 *
 * It works on the sprite level, so buttons added by other mods (Mod Menu, ...) get the same style
 * as long as they use the vanilla widget sprites; mods with their own textures keep them.
 * Everything else (background, logo, lists) stays vanilla.
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

    /** Custom widgets are on for every lobby theme except Standard - in the lobby and in-game menus (pause, options...). */
    public static boolean active() {
        try {
            return LobbyTheme.selected() != LobbyTheme.Standard;
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
                case SCROLLER -> ui.round(px + pw * 0.2f, py, pw * 0.6f, ph, pw * 0.3f, UiRenderer.white(0.3f + 0.25f * hover));
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
        // dark glass with a thin light edge on top; brighter edge and underline on hover
        ui.round(x, y, w, h, r, UiEasing.mix(UiRenderer.argb(0.55f, 6, 7, 10), UiEasing.mix(UiRenderer.argb(0.62f, 10, 11, 15), UiRenderer.white(0.14f), 0.5f), hover));
        ui.rect(x + r, y + line, w - 2.0f * r, line, UiRenderer.white(0.07f + 0.05f * hover));
        if (hover > 0.01f) {
            ui.round(x + w * 0.2f, y + h - line * 2.0f, w * 0.6f, line * 2.0f, line, UiRenderer.white(0.55f * hover));
        }
        ui.outline(x, y, w, h, r, line, UiEasing.mix(UiRenderer.white(0.11f), UiRenderer.white(0.42f), hover));
    }

    private static void track(UiRenderer ui, float s, float x, float y, float w, float h, float hover) {
        float r = Math.min(h / 2.0f, 4.0f * s);
        ui.round(x, y, w, h, r, UiEasing.mix(UiRenderer.argb(0.6f, 12, 13, 16), UiRenderer.white(0.08f), 0.5f));
        ui.outline(x, y, w, h, r, Math.max(1.0f, s * 0.6f), UiEasing.mix(UiRenderer.white(0.12f), UiRenderer.white(0.35f), hover));
    }

    private static void handle(UiRenderer ui, float s, float x, float y, float w, float h, float hover) {
        float inset = 1.5f * s;
        float hw = Math.max(w - 2 * inset, 3.0f * s);
        float hx = x + (w - hw) / 2.0f;
        float hy = y + inset;
        float hh = h - 2 * inset;
        ui.glow(hx + hw / 2.0f, y + h / 2.0f, h * 0.9f, UiRenderer.white(0.04f + 0.05f * hover));
        ui.round(hx, hy, hw, hh, Math.min(hw / 2.0f, 2.5f * s), UiRenderer.argb(1.0f, 205 + Math.round(40 * hover), 208 + Math.round(40 * hover), 214 + Math.round(40 * hover)));
    }

    private static void field(UiRenderer ui, float s, float x, float y, float w, float h, float hover) {
        float r = Math.min(h / 2.0f, 4.0f * s);
        ui.round(x, y, w, h, r, UiEasing.mix(UiRenderer.argb(0.85f, 12, 13, 16), UiRenderer.argb(0.85f, 8, 9, 12), 0.4f));
        ui.outline(x, y, w, h, r, Math.max(1.0f, s * 0.6f), UiEasing.mix(UiRenderer.white(0.14f), UiRenderer.white(0.6f), hover));
    }

    private static void checkbox(UiRenderer ui, float s, float x, float y, float w, float h, float hover, boolean selected) {
        float r = Math.min(w, h) * 0.3f;
        ui.round(x, y, w, h, r, selected ? UiRenderer.white(0.85f) : UiEasing.mix(UiRenderer.argb(0.7f, 12, 13, 16), UiRenderer.white(0.12f), 0.5f));
        ui.outline(x, y, w, h, r, Math.max(1.0f, s * 0.6f), UiEasing.mix(UiRenderer.white(0.18f), UiRenderer.white(0.9f), Math.max(hover, selected ? 1.0f : 0.0f)));
        if (selected) {
            ui.circle(x + w / 2.0f, y + h / 2.0f, Math.min(w, h) * 0.18f, UiRenderer.argb(0.95f, 20, 21, 25));
        }
    }

}