package hook.feature;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

/**
 * Plays the selected lobby theme behind the menus: pixel-art layers drawn at their native 640x360
 * resolution and scaled up with nearest filtering, plus the animated effects listed in the theme's
 * theme.txt (written by tools/MenuArtGen.java).
 */
public final class MenuBackground {
    private static final int ART_W = 640, ART_H = 360;
    private static final long START = System.nanoTime();
    private static final Map<LobbyTheme, Theme> CACHE = new EnumMap<>(LobbyTheme.class);

    private MenuBackground() {
    }

    private record Layer(String name, class_2960 id, int w, int h, float sway, float scroll, int y, boolean pulse) {
    }

    private record Effect(String type, String layer, int x, int y, int w, int h, int rgb, int n) {
    }

    private record Theme(List<Layer> layers, List<Effect> effects) {
    }

    private static Theme load(LobbyTheme lobby) {
        return CACHE.computeIfAbsent(lobby, key -> {
            List<Layer> layers = new ArrayList<>();
            List<Effect> effects = new ArrayList<>();
            String base = "/assets/hook/textures/gui/menu/" + key.folder + "/";
            try (InputStream in = MenuBackground.class.getResourceAsStream(base + "theme.txt")) {
                if (in != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    for (String line; (line = reader.readLine()) != null; ) {
                        String[] p = line.trim().split("\\s+");
                        if (p[0].equals("layer") && p.length >= 8) {
                            String file = p[1];
                            layers.add(new Layer(file.replace(".png", ""), class_2960.method_60655("hook", "textures/gui/menu/" + key.folder + "/" + file),
                                    Integer.parseInt(p[2]), Integer.parseInt(p[3]), Float.parseFloat(p[4]), Float.parseFloat(p[5]), Integer.parseInt(p[6]), p[7].equals("1")));
                        } else if (p[0].equals("fx") && p.length >= 9) {
                            effects.add(new Effect(p[1], p[2], Integer.parseInt(p[3]), Integer.parseInt(p[4]), Integer.parseInt(p[5]), Integer.parseInt(p[6]),
                                    Integer.parseInt(p[7], 16), Integer.parseInt(p[8])));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Hook: could not load lobby theme " + key.folder + ": " + e);
            }
            return new Theme(layers, effects);
        });
    }

    private static float alpha = 1.0f;

    /** Same as {@link #render(class_332)} but faded: every layer and effect is drawn with the given opacity. */
    public static boolean render(class_332 ctx, float opacity) {
        alpha = Math.max(0.0f, Math.min(1.0f, opacity));
        try {
            return render(ctx);
        } finally {
            alpha = 1.0f;
        }
    }

    private static int faded(int color) {
        if (alpha >= 1.0f) return color;
        return Math.round((color >>> 24) * alpha) << 24 | (color & 0xFFFFFF);
    }

    /** @return false when the theme draws nothing (Standard), so vanilla stays. */
    public static boolean render(class_332 ctx) {
        LobbyTheme lobby = LobbyTheme.selected();
        if (lobby.folder == null) return false;
        Theme theme = load(lobby);
        if (theme.layers.isEmpty()) return false;
        int w = ctx.method_51421();
        int h = ctx.method_51443();
        float t = (System.nanoTime() - START) / 1.0E9f;
        float p = Math.max(w / (float) ART_W, h / (float) ART_H);
        float sway = (float) Math.sin(t * 0.05);
        Map<String, Float> offsets = new HashMap<>();
        offsets.put("-", 0.0f);

        ctx.method_51448().pushMatrix();
        ctx.method_51448().translate((w - ART_W * p) / 2.0f, (h - ART_H * p) / 2.0f);
        ctx.method_51448().scale(p, p);
        try {
            int layerIndex = 0;
            for (Layer layer : theme.layers) {
                int color = layer.pulse ? argb(0.7f + 0.3f * (float) Math.sin(t * 0.45), 0xFFFFFF) : -1;
                if (layer.scroll != 0.0f) {
                    float off = (t * layer.scroll) % layer.w;
                    draw(ctx, layer, -off, layer.y, color);
                    draw(ctx, layer, layer.w - off, layer.y, color);
                    offsets.put(layer.name, 0.0f);
                } else {
                    float x = -(layer.w - ART_W) / 2.0f + sway * layer.sway;
                    draw(ctx, layer, x, layer.y, color);
                    offsets.put(layer.name, x);
                }
                // effects belonging to this layer (and global ones after the sky) are drawn right after it
                boolean last = layerIndex == theme.layers.size() - 1;
                for (Effect fx : theme.effects) {
                    boolean global = fx.layer.equals("-");
                    boolean inSky = fx.type.equals("shooting") || fx.type.equals("bats");
                    if (global ? (inSky ? layerIndex == 0 : last) : fx.layer.equals(layer.name)) {
                        ctx.method_51448().pushMatrix();
                        ctx.method_51448().translate(offsets.getOrDefault(fx.layer, 0.0f), 0);
                        effect(ctx, fx, t);
                        ctx.method_51448().popMatrix();
                    }
                }
                layerIndex++;
            }
        } finally {
            ctx.method_51448().popMatrix();
        }
        return true;
    }

    private static final Map<class_2960, class_2960> DIRECT = new HashMap<>();

    /**
     * The layer as a texture uploaded straight from the mod jar. It does not go through the resource manager, so it
     * also draws while resources reload (loading screen) and resource packs cannot break the menu art.
     */
    private static class_2960 direct(Layer layer) {
        return DIRECT.computeIfAbsent(layer.id, id -> {
            try (InputStream in = MenuBackground.class.getResourceAsStream("/assets/hook/" + id.method_12832())) {
                if (in == null) return id;
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(in);
                int w = img.getWidth(), h = img.getHeight();
                net.minecraft.class_1011 image = new net.minecraft.class_1011(w, h, false);
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        image.method_61941(x, y, img.getRGB(x, y));
                    }
                }
                class_2960 tid = class_2960.method_60655("hook", "menu_direct/" + id.method_12832().replace(".png", ""));
                net.minecraft.class_310.method_1551().method_1531().method_4616(tid, new net.minecraft.class_1043(() -> "Hook menu " + layer.name, image));
                return tid;
            } catch (Throwable t) {
                System.err.println("Hook: could not load menu layer " + id + ": " + t);
                return id;
            }
        });
    }

    private static void draw(class_332 ctx, Layer layer, float x, float y, int color) {
        class_2960 tex = direct(layer);
        ctx.method_51448().pushMatrix();
        ctx.method_51448().translate(x, y);
        if (alpha < 1.0f) color = faded(color);
        if (color == -1) {
            ctx.method_25290(class_10799.field_56883, tex, 0, 0, 0.0f, 0.0f, layer.w, layer.h, layer.w, layer.h);
        } else {
            ctx.method_25293(class_10799.field_56883, tex, 0, 0, 0.0f, 0.0f, layer.w, layer.h, layer.w, layer.h, layer.w, layer.h, color);
        }
        ctx.method_51448().popMatrix();
    }

    // ------------------------------------------------------------------ effects

    private static void effect(class_332 ctx, Effect fx, float t) {
        switch (fx.type) {
            case "glow" -> glow(ctx, fx.x, fx.y, fx.rgb, flicker(t, fx.x * 0.13f + fx.y), Math.max(1, fx.n));
            case "window" -> {
                float f = flicker(t, fx.x * 0.31f + fx.y * 0.7f);
                fill(ctx, fx.x, fx.y, fx.w, fx.h, argb(0.3f * f, 0xFFF0C0));
                glow(ctx, fx.x + fx.w / 2, fx.y + fx.h / 2, fx.rgb, f * 0.6f, Math.max(1, fx.n));
            }
            case "fall" -> fall(ctx, fx, t);
            case "shimmer" -> shimmer(ctx, fx, t);
            case "lava" -> lava(ctx, fx, t);
            case "portal" -> portal(ctx, fx, t);
            case "fire" -> fire(ctx, fx, t);
            case "rise" -> particles(ctx, fx, t, -1, 0.06f);
            case "drop" -> particles(ctx, fx, t, 1, 0.09f);
            case "dust" -> particles(ctx, fx, t, 1, 0.012f);
            case "ash" -> particles(ctx, fx, t, 1, 0.02f);
            case "snow" -> snow(ctx, fx, t);
            case "drip" -> drip(ctx, fx, t);
            case "flies" -> flies(ctx, fx, t);
            case "sparkle" -> sparkle(ctx, fx, t);
            case "beam" -> beam(ctx, fx, t);
            case "crystal" -> {
                float pulse = 0.6f + 0.4f * (float) Math.sin(t * 2.0 + fx.x);
                glow(ctx, fx.x, fx.y, fx.rgb, pulse, 5);
            }
            case "shooting" -> shootingStar(ctx, fx, t);
            case "bats" -> bats(ctx, fx, t);
            case "fog" -> fog(ctx, fx, t);
            case "smoke" -> smoke(ctx, fx, t);
            case "scan" -> scan(ctx, fx, t);
            default -> {
            }
        }
    }

    private static void fill(class_332 ctx, int x, int y, int w, int h, int color) {
        ctx.method_25294(x, y, x + w, y + h, faded(color));
    }

    private static float flicker(float t, float seed) {
        return 0.75f + 0.15f * (float) Math.sin(t * 9.1 + seed) + 0.1f * (float) Math.sin(t * 23.7 + seed * 2.3);
    }

    private static float rand(int i, int salt) {
        long h = i * 374761393L + salt * 668265263L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        return ((h ^ (h >>> 16)) & 0xFFFF) / 65535.0f;
    }

    private static void glow(class_332 ctx, int cx, int cy, int rgb, float strength, int rings) {
        for (int r = rings; r >= 1; r--) {
            int size = r * 3;
            fill(ctx, cx - size, cy - size, size * 2 + 1, size * 2 + 1, argb(0.05f * strength * (rings - r + 1) / Math.max(1.0f, rings / 3.0f), rgb));
        }
    }

    private static void fall(class_332 ctx, Effect fx, float t) {
        int n = Math.max(1, fx.n);
        for (int i = 0; i < n; i++) {
            int x = fx.x + 1 + (i * 5) % Math.max(1, fx.w - 2);
            float y = fx.y + ((t * (38 + i * 4) + i * 23) % Math.max(1, fx.h));
            fill(ctx, x, Math.round(y), 1, 4 + i % 3, argb(0.55f, fx.rgb));
        }
        for (int i = 0; i < 8; i++) {
            float phase = (t * 1.6f + i * 0.37f) % 1.0f;
            int x = fx.x - 3 + Math.round((i * 7 % (fx.w + 6)) + (float) Math.sin(t * 3 + i) * 1.5f);
            int y = Math.round(fx.y + fx.h + 1 - phase * 7);
            fill(ctx, x, y, 1, 1, argb(0.6f * (1 - phase), fx.rgb));
        }
    }

    private static void shimmer(class_332 ctx, Effect fx, float t) {
        for (int i = 0; i < fx.n; i++) {
            float speed = 3.0f + (i % 4) * 1.5f;
            float x = fx.x + ((i * 41.0f + t * speed) % Math.max(1, fx.w));
            int y = fx.y + (i * 7) % Math.max(1, fx.h);
            int len = 3 + (i % 3) * 2;
            fill(ctx, Math.round(x), y, len, 1, argb(0.18f + 0.16f * (float) Math.sin(t * 1.2 + i), fx.rgb));
        }
    }

    private static void lava(class_332 ctx, Effect fx, float t) {
        for (int i = 0; i < fx.n; i++) {
            float phase = (t * 0.7f + rand(i, 3)) % 1.0f;
            int x = fx.x + Math.round(rand(i, 4) * fx.w);
            int y = fx.y + Math.round(rand(i, 5) * Math.min(fx.h, 10));
            int size = phase < 0.5f ? 1 : 2;
            fill(ctx, x, y, size, size, argb(0.8f * (1 - phase), fx.rgb));
        }
        fill(ctx, fx.x, fx.y, fx.w, 1, argb(0.25f + 0.15f * (float) Math.sin(t * 1.7), 0xFFE6A0));
    }

    private static void portal(class_332 ctx, Effect fx, float t) {
        for (int y = 0; y < fx.h; y++) {
            for (int x = 0; x < fx.w; x += 2) {
                float v = (float) Math.sin((x * 0.6 + y * 0.35) + t * 3.0) + (float) Math.sin((x * -0.4 + y * 0.5) - t * 2.2);
                if (v > 1.1f) fill(ctx, fx.x + x + (y & 1), fx.y + y, 1, 1, argb(0.55f, fx.rgb));
                else if (v < -1.2f) fill(ctx, fx.x + x + (y & 1), fx.y + y, 1, 1, argb(0.45f, 0x3B0F7A));
            }
        }
        glow(ctx, fx.x + fx.w / 2, fx.y + fx.h / 2, 0x9D4BFF, 0.7f + 0.3f * (float) Math.sin(t * 2.0), 4);
        for (int i = 0; i < 6; i++) {
            float phase = (t * 0.5f + i * 0.17f) % 1.0f;
            int x = fx.x - 4 + (i * 11) % (fx.w + 8) + Math.round((float) Math.sin(t + i) * 2);
            int y = Math.round(fx.y + fx.h - phase * (fx.h + 8));
            fill(ctx, x, y, 1, 1, argb(0.8f * (1 - phase), 0xD9A6FF));
        }
    }

    private static void fire(class_332 ctx, Effect fx, float t) {
        boolean soul = (fx.rgb & 0xFF) > 0xC0 && ((fx.rgb >> 16) & 0xFF) < 0xA0;
        int hot = soul ? 0xD8FFFF : 0xFFF2B0, mid = soul ? 0x7FE8FF : 0xFFB13D, low = soul ? 0x2A9AC8 : 0xF0621C;
        glow(ctx, fx.x, fx.y - 3, fx.rgb, flicker(t, fx.x), 4);
        for (int i = -2; i <= 2; i++) {
            int height = Math.round(3 + 3 * (0.5f + 0.5f * (float) Math.sin(t * 11 + i * 1.9)) - Math.abs(i));
            for (int j = 0; j < height; j++) {
                int color = j < height / 3 ? hot : j < height * 2 / 3 ? mid : low;
                fill(ctx, fx.x + i, fx.y - 1 - j, 1, 1, argb(0.95f, color));
            }
        }
        for (int i = 0; i < 6; i++) {
            float phase = (t * 0.6f + i * 0.21f) % 1.0f;
            int x = fx.x + Math.round((float) Math.sin(t * 1.5 + i * 2) * 3 + phase * 4);
            int y = Math.round(fx.y - 6 - phase * 34);
            if (i % 3 == 0) fill(ctx, x, y, 1, 1, argb(0.9f * (1 - phase), mid));
            else fill(ctx, x, y, 2, 2, argb(0.22f * (1 - phase), 0xB8BCC8));
        }
    }

    /** Particles moving through a rect: dir -1 = rising, 1 = falling; speed = rect heights per second. */
    private static void particles(class_332 ctx, Effect fx, float t, int dir, float speed) {
        for (int i = 0; i < fx.n; i++) {
            float phase = (t * speed * (0.6f + rand(i, 1)) + rand(i, 2)) % 1.0f;
            float x = fx.x + rand(i, 3) * fx.w + (float) Math.sin(t * (0.8 + rand(i, 4)) + i) * 4;
            float y = dir < 0 ? fx.y + fx.h - phase * fx.h : fx.y + phase * fx.h;
            float a = (float) Math.sin(phase * Math.PI);
            fill(ctx, Math.round(x), Math.round(y), 1, 1, argb(0.8f * a, fx.rgb));
        }
    }

    private static void snow(class_332 ctx, Effect fx, float t) {
        for (int i = 0; i < fx.n; i++) {
            float speed = 0.05f + rand(i, 1) * 0.06f;
            float phase = (t * speed + rand(i, 2)) % 1.0f;
            float x = fx.x + rand(i, 3) * fx.w + (float) Math.sin(t * 0.9 + i) * 6 + phase * 20;
            float y = fx.y + phase * fx.h;
            int size = rand(i, 5) > 0.75f ? 2 : 1;
            fill(ctx, Math.round(x) % (fx.x + fx.w), Math.round(y), size, size, argb(0.85f, fx.rgb));
        }
    }

    private static void drip(class_332 ctx, Effect fx, float t) {
        for (int i = 0; i < fx.n; i++) {
            float cycle = 2.5f + rand(i, 1) * 2.0f;
            float phase = ((t + rand(i, 2) * cycle) % cycle) / cycle;
            int x = fx.x + Math.round(rand(i, 3) * fx.w);
            int y = fx.y + Math.round(phase * phase * fx.h);
            fill(ctx, x, y, 1, 2, argb(0.7f, fx.rgb));
        }
    }

    private static void flies(class_332 ctx, Effect fx, float t) {
        for (int i = 0; i < fx.n; i++) {
            float speed = 0.4f + rand(i, 1) * 0.8f;
            float x = fx.x + rand(i, 2) * fx.w + (float) Math.sin(t * 0.4 * speed + i) * 14;
            float y = fx.y + rand(i, 3) * fx.h + (float) Math.sin(t * 0.7 * speed + i * 1.7) * 6;
            float blink = 0.5f + 0.5f * (float) Math.sin(t * 2.2 * speed + i * 3.1);
            if (blink < 0.15f) continue;
            int ix = Math.round(x), iy = Math.round(y);
            fill(ctx, ix - 1, iy - 1, 3, 3, argb(0.12f * blink, fx.rgb));
            fill(ctx, ix, iy, 1, 1, argb(0.9f * blink, fx.rgb));
        }
    }

    private static void sparkle(class_332 ctx, Effect fx, float t) {
        for (int i = 0; i < fx.n; i++) {
            float phase = (t * (0.5f + rand(i, 1)) + rand(i, 2) * 10) % 2.0f;
            if (phase > 1.0f) continue;
            float a = (float) Math.sin(phase * Math.PI);
            int x = fx.x + Math.round(rand(i + (int) (t / 2), 3) * fx.w);
            int y = fx.y + Math.round(rand(i + (int) (t / 2), 4) * fx.h);
            fill(ctx, x, y, 1, 1, argb(a, fx.rgb));
            if (a > 0.7f) {
                fill(ctx, x - 1, y, 3, 1, argb(a * 0.4f, fx.rgb));
                fill(ctx, x, y - 1, 1, 3, argb(a * 0.4f, fx.rgb));
            }
        }
    }

    private static void beam(class_332 ctx, Effect fx, float t) {
        float pulse = 0.55f + 0.25f * (float) Math.sin(t * 1.5 + fx.x);
        fill(ctx, fx.x - 2, fx.y, fx.w + 4, fx.h, argb(0.08f * pulse, fx.rgb));
        fill(ctx, fx.x, fx.y, fx.w, fx.h, argb(0.45f * pulse, fx.rgb));
        for (int i = 0; i < 4; i++) {
            float phase = (t * 0.4f + i * 0.25f) % 1.0f;
            fill(ctx, fx.x, Math.round(fx.y + fx.h - phase * fx.h), fx.w, 3, argb(0.5f * (1 - phase), 0xFFFFFF));
        }
    }

    private static void shootingStar(class_332 ctx, Effect fx, float t) {
        float cycle = Math.max(4, fx.n);
        float local = t % cycle;
        if (local > 1.2f) return;
        int n = (int) (t / cycle);
        float sx = fx.x + 60 + (n * 211 % Math.max(1, fx.w - 200)), sy = fx.y + 20 + (n * 97 % Math.max(1, fx.h - 40));
        float k = local / 1.2f;
        float x = sx + k * 90, y = sy + k * 38;
        for (int i = 0; i < 12; i++) {
            fill(ctx, Math.round(x - i * 2), Math.round(y - i * 0.85f), 1, 1, argb((1.0f - i / 12.0f) * (1.0f - k), fx.rgb));
        }
    }

    private static void bats(class_332 ctx, Effect fx, float t) {
        for (int i = 0; i < fx.n; i++) {
            float speed = 18 + rand(i, 1) * 20;
            float x = fx.x + ((t * speed + rand(i, 2) * fx.w) % (fx.w + 40)) - 20;
            float y = fx.y + rand(i, 3) * fx.h + (float) Math.sin(t * 2 + i) * 8;
            boolean up = ((int) (t * 8 + i)) % 2 == 0;
            int ix = Math.round(x), iy = Math.round(y);
            int c = argb(0.9f, fx.rgb);
            fill(ctx, ix, iy, 2, 2, c);
            fill(ctx, ix - 3, iy + (up ? -1 : 1), 3, 1, c);
            fill(ctx, ix + 2, iy + (up ? -1 : 1), 3, 1, c);
        }
    }

    private static void fog(class_332 ctx, Effect fx, float t) {
        for (int i = 0; i < 6; i++) {
            float x = fx.x + ((t * (4 + i) + i * 120) % (fx.w + 200)) - 100;
            int y = fx.y + (i * 9) % Math.max(1, fx.h);
            fill(ctx, Math.round(x), y, 120, 6, argb(0.06f, fx.rgb));
            fill(ctx, Math.round(x) + 20, y + 2, 80, 3, argb(0.05f, fx.rgb));
        }
    }

    private static void smoke(class_332 ctx, Effect fx, float t) {
        for (int i = 0; i < Math.max(1, fx.n); i++) {
            float phase = (t * 0.25f + i / (float) Math.max(1, fx.n)) % 1.0f;
            int x = fx.x + Math.round(phase * 10 + (float) Math.sin(t + i) * 2);
            int y = Math.round(fx.y - phase * 30);
            fill(ctx, x, y, 3, 3, argb(0.25f * (1 - phase), fx.rgb));
        }
    }

    private static void scan(class_332 ctx, Effect fx, float t) {
        int y = fx.y + Math.round(((t * 22) % (fx.h + 40)) - 20);
        fill(ctx, fx.x, y, fx.w, 1, argb(0.10f, fx.rgb));
        fill(ctx, fx.x, y + 1, fx.w, 1, argb(0.05f, fx.rgb));
    }

    private static int argb(float alpha, int rgb) {
        return Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f) << 24 | (rgb & 0xFFFFFF);
    }
}
