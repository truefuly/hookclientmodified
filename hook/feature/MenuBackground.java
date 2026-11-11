package hook.feature;

import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

/**
 * Animated pixel-art Minecraft night landscape behind the menus (replaces the vanilla panorama).
 * Layers are drawn at their native 640x360 art resolution and scaled up with nearest filtering,
 * so every art pixel stays a crisp square. Animated: mint aurora, shooting stars, drifting clouds,
 * parallax sway, castle windows, waterfall, lake, nether portal, campfire, lantern, falling cherry
 * petals and fireflies. Art and positions come from tools/MenuArtGen.java.
 */
public final class MenuBackground {
    private static final int ART_W = 640, ART_H = 360;
    private static final class_2960 SKY = tex("sky"), AURORA = tex("aurora"), CLOUDS = tex("clouds"), FAR = tex("far"), MID = tex("mid"), NEAR = tex("near");
    private static final int AURORA_W = 1280, AURORA_H = 180, CLOUDS_W = 1280, CLOUDS_H = 110, FAR_W = 720, MID_W = 760, NEAR_W = 800;

    // positions printed by MenuArtGen (mid.png / near.png coordinates)
    private static final int[][] CASTLE_WINDOWS = {{325, 214}, {376, 214}, {351, 200}, {351, 210}, {336, 230}, {364, 230}};
    private static final int FALL_X = 150, FALL_W = 12, FALL_TOP = 189, FALL_BOTTOM = 270;
    private static final int LAKE_X0 = 176, LAKE_X1 = 440, LAKE_Y = 270, SHORE_Y = 306;
    private static final int CHERRY_X = 466, CHERRY_Y = 222, CHERRY_W = 46, CHERRY_H = 34;
    private static final int PORTAL_X = 564, PORTAL_Y = 238, PORTAL_W = 16, PORTAL_H = 24;
    private static final int FIRE_X = 650, FIRE_Y = 264;
    private static final int LANTERN_X = 696, LANTERN_Y = 252;

    private static final long START = System.nanoTime();
    private static final float[][] FLIES = new float[22][4];
    private static final float[][] PETALS = new float[14][3];

    static {
        java.util.Random random = new java.util.Random(7);
        for (float[] fly : FLIES) {
            fly[0] = 20 + random.nextFloat() * (NEAR_W - 40);
            fly[1] = 250 + random.nextFloat() * 70;
            fly[2] = random.nextFloat() * 100;
            fly[3] = 0.4f + random.nextFloat() * 0.8f;
        }
        for (float[] petal : PETALS) {
            petal[0] = random.nextFloat();
            petal[1] = random.nextFloat() * 100;
            petal[2] = 0.6f + random.nextFloat() * 0.8f;
        }
    }

    private MenuBackground() {
    }

    private static class_2960 tex(String name) {
        return class_2960.method_60655("hook", "textures/gui/menu/" + name + ".png");
    }

    public static void render(class_332 ctx) {
        int w = ctx.method_51421();
        int h = ctx.method_51443();
        float t = (System.nanoTime() - START) / 1.0E9f;
        float p = Math.max(w / (float) ART_W, h / (float) ART_H);
        float sway = (float) Math.sin(t * 0.05);

        ctx.method_51448().pushMatrix();
        ctx.method_51448().translate((w - ART_W * p) / 2.0f, (h - ART_H * p) / 2.0f);
        ctx.method_51448().scale(p, p);
        try {
            layer(ctx, SKY, 0, 0, ART_W, ART_H, -1);
            shootingStar(ctx, t);
            float pulse = 0.72f + 0.28f * (float) Math.sin(t * 0.45);
            float aurora = (t * 2.5f) % AURORA_W;
            int auroraColor = argb(pulse, 0xFFFFFF);
            layer(ctx, AURORA, -aurora, 0, AURORA_W, AURORA_H, auroraColor);
            layer(ctx, AURORA, AURORA_W - aurora, 0, AURORA_W, AURORA_H, auroraColor);
            float clouds = (t * 4.0f) % CLOUDS_W;
            layer(ctx, CLOUDS, -clouds, 26, CLOUDS_W, CLOUDS_H, -1);
            layer(ctx, CLOUDS, CLOUDS_W - clouds, 26, CLOUDS_W, CLOUDS_H, -1);
            layer(ctx, FAR, -(FAR_W - ART_W) / 2.0f + sway * 6, 0, FAR_W, ART_H, -1);

            float midX = -(MID_W - ART_W) / 2.0f + sway * 11;
            layer(ctx, MID, midX, 0, MID_W, ART_H, -1);
            push(ctx, midX);
            castleWindows(ctx, t);
            ctx.method_51448().popMatrix();

            float nearX = -(NEAR_W - ART_W) / 2.0f + sway * 18;
            layer(ctx, NEAR, nearX, 0, NEAR_W, ART_H, -1);
            push(ctx, nearX);
            waterfall(ctx, t);
            lake(ctx, t);
            portal(ctx, t);
            campfire(ctx, t);
            glow(ctx, LANTERN_X + 1, LANTERN_Y, 0xFFD27A, flicker(t, 4.2f), 3);
            petals(ctx, t);
            fireflies(ctx, t);
            ctx.method_51448().popMatrix();
        } finally {
            ctx.method_51448().popMatrix();
        }
    }

    private static void push(class_332 ctx, float x) {
        ctx.method_51448().pushMatrix();
        ctx.method_51448().translate(x, 0);
    }

    private static void layer(class_332 ctx, class_2960 id, float x, float y, int texW, int texH, int color) {
        ctx.method_51448().pushMatrix();
        ctx.method_51448().translate(x, y);
        if (color == -1) {
            ctx.method_25290(class_10799.field_56883, id, 0, 0, 0.0f, 0.0f, texW, texH, texW, texH);
        } else {
            ctx.method_25293(class_10799.field_56883, id, 0, 0, 0.0f, 0.0f, texW, texH, texW, texH, texW, texH, color);
        }
        ctx.method_51448().popMatrix();
    }

    private static void fill(class_332 ctx, int x, int y, int w, int h, int color) {
        ctx.method_25294(x, y, x + w, y + h, color);
    }

    private static float flicker(float t, float seed) {
        return 0.75f + 0.15f * (float) Math.sin(t * 9.1 + seed) + 0.1f * (float) Math.sin(t * 23.7 + seed * 2.3);
    }

    private static void glow(class_332 ctx, int cx, int cy, int rgb, float strength, int rings) {
        for (int r = rings; r >= 1; r--) {
            int size = r * 3;
            fill(ctx, cx - size, cy - size, size * 2 + 1, size * 2 + 1, argb(0.05f * strength * (rings - r + 1), rgb));
        }
    }

    // ------------------------------------------------------------------ sky

    private static void shootingStar(class_332 ctx, float t) {
        float cycle = 11.0f;
        float local = t % cycle;
        if (local > 1.2f) return;
        int n = (int) (t / cycle);
        float sx = 60 + (n * 211 % 420), sy = 20 + (n * 97 % 70);
        float k = local / 1.2f;
        float x = sx + k * 90, y = sy + k * 38;
        for (int i = 0; i < 12; i++) {
            float a = (1.0f - i / 12.0f) * (1.0f - k);
            fill(ctx, Math.round(x - i * 2), Math.round(y - i * 0.85f), 1, 1, argb(a, 0xF2F6FF));
        }
    }

    private static void castleWindows(class_332 ctx, float t) {
        for (int i = 0; i < CASTLE_WINDOWS.length; i++) {
            int[] wv = CASTLE_WINDOWS[i];
            float f = flicker(t, i * 1.7f);
            fill(ctx, wv[0], wv[1], 3, 3, argb(0.35f * f, 0xFFE7A0));
            glow(ctx, wv[0] + 1, wv[1] + 1, 0xFFC76A, f * 0.6f, 2);
        }
    }

    // ------------------------------------------------------------------ water

    private static void waterfall(class_332 ctx, float t) {
        int len = FALL_BOTTOM - FALL_TOP;
        for (int i = 0; i < 9; i++) {
            int x = FALL_X + 1 + (i * 5) % (FALL_W - 2);
            float y = FALL_TOP + ((t * (38 + i * 4) + i * 23) % len);
            fill(ctx, x, Math.round(y), 1, 4 + i % 3, argb(0.55f, 0xE6F0FF));
        }
        // splash and mist where it meets the lake
        for (int i = 0; i < 8; i++) {
            float phase = (t * 1.6f + i * 0.37f) % 1.0f;
            int x = FALL_X - 3 + Math.round((i * 7 % 18) + (float) Math.sin(t * 3 + i) * 1.5f);
            int y = Math.round(FALL_BOTTOM + 1 - phase * 7);
            fill(ctx, x, y, 1, 1, argb(0.6f * (1 - phase), 0xF4F8FF));
        }
        fill(ctx, FALL_X - 4, FALL_BOTTOM - 2, FALL_W + 8, 4, argb(0.16f + 0.05f * (float) Math.sin(t * 2), 0xDDE8FF));
    }

    private static void lake(class_332 ctx, float t) {
        for (int i = 0; i < 14; i++) {
            float speed = 3.0f + (i % 4) * 1.5f;
            float span = LAKE_X1 - LAKE_X0 - 12;
            float x = LAKE_X0 + 6 + ((i * 41.0f + t * speed) % span);
            int y = LAKE_Y + 3 + (i * 7) % (SHORE_Y - LAKE_Y - 6);
            int len = 3 + (i % 3) * 2;
            float alpha = 0.18f + 0.16f * (float) Math.sin(t * 1.2 + i);
            fill(ctx, Math.round(x), y, len, 1, argb(alpha, 0xBFE4F0));
        }
    }

    // ------------------------------------------------------------------ props

    private static void portal(class_332 ctx, float t) {
        // swirling purple: moving brighter streaks inside the frame
        for (int y = 0; y < PORTAL_H; y++) {
            for (int x = 0; x < PORTAL_W; x += 2) {
                float v = (float) Math.sin((x * 0.6 + y * 0.35) + t * 3.0) + (float) Math.sin((x * -0.4 + y * 0.5) - t * 2.2);
                if (v > 1.1f) fill(ctx, PORTAL_X + x + (y & 1), PORTAL_Y + y, 1, 1, argb(0.55f, 0xC889FF));
                else if (v < -1.2f) fill(ctx, PORTAL_X + x + (y & 1), PORTAL_Y + y, 1, 1, argb(0.45f, 0x3B0F7A));
            }
        }
        glow(ctx, PORTAL_X + PORTAL_W / 2, PORTAL_Y + PORTAL_H / 2, 0x9D4BFF, 0.7f + 0.3f * (float) Math.sin(t * 2.0), 4);
        for (int i = 0; i < 6; i++) {
            float phase = (t * 0.5f + i * 0.17f) % 1.0f;
            int x = PORTAL_X - 4 + (i * 11) % (PORTAL_W + 8) + Math.round((float) Math.sin(t + i) * 2);
            int y = Math.round(PORTAL_Y + PORTAL_H - phase * (PORTAL_H + 8));
            fill(ctx, x, y, 1, 1, argb(0.8f * (1 - phase), 0xD9A6FF));
        }
    }

    private static void campfire(class_332 ctx, float t) {
        glow(ctx, FIRE_X, FIRE_Y - 3, 0xFF9A3C, flicker(t, 0.5f), 4);
        for (int i = -2; i <= 2; i++) {
            int height = Math.round(3 + 3 * (0.5f + 0.5f * (float) Math.sin(t * 11 + i * 1.9)) - Math.abs(i));
            for (int j = 0; j < height; j++) {
                int color = j < height / 3 ? 0xFFF2B0 : j < height * 2 / 3 ? 0xFFB13D : 0xF0621C;
                fill(ctx, FIRE_X + i, FIRE_Y - 1 - j, 1, 1, argb(0.95f, color));
            }
        }
        for (int i = 0; i < 6; i++) {
            float phase = (t * 0.6f + i * 0.21f) % 1.0f;
            int x = FIRE_X + Math.round((float) Math.sin(t * 1.5 + i * 2) * 3 + phase * 4);
            int y = Math.round(FIRE_Y - 6 - phase * 34);
            if (i % 3 == 0) fill(ctx, x, y, 1, 1, argb(0.9f * (1 - phase), 0xFFB13D));        // ember
            else fill(ctx, x, y, 2, 2, argb(0.22f * (1 - phase), 0xB8BCC8));                 // smoke
        }
    }

    private static void petals(class_332 ctx, float t) {
        for (float[] petal : PETALS) {
            float fall = (t * 0.09f * petal[2] + petal[1]) % 1.0f;
            float x = CHERRY_X + petal[0] * CHERRY_W + fall * 26 + (float) Math.sin(t * 1.4 + petal[1]) * 3;
            float y = CHERRY_Y + CHERRY_H * 0.5f + fall * 48;
            fill(ctx, Math.round(x), Math.round(y), 1, 1, argb(0.9f * (1 - fall * 0.6f), fall > 0.5f ? 0xF0B4D2 : 0xD88AB6));
        }
    }

    private static void fireflies(class_332 ctx, float t) {
        for (float[] fly : FLIES) {
            float x = fly[0] + (float) Math.sin(t * 0.4 * fly[3] + fly[2]) * 14;
            float y = fly[1] + (float) Math.sin(t * 0.7 * fly[3] + fly[2] * 1.7) * 6;
            float blink = 0.5f + 0.5f * (float) Math.sin(t * 2.2 * fly[3] + fly[2] * 3.1);
            if (blink < 0.15f) continue;
            int ix = Math.round(x), iy = Math.round(y);
            fill(ctx, ix - 1, iy - 1, 3, 3, argb(0.12f * blink, 0xBFFFD9));
            fill(ctx, ix, iy, 1, 1, argb(0.9f * blink, 0xE8FFF0));
        }
    }

    private static int argb(float alpha, int rgb) {
        return Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f) << 24 | (rgb & 0xFFFFFF);
    }
}
