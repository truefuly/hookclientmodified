import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Generates the lobby themes (pixel-art menu backgrounds). For every theme it writes the image
 * layers, a preview.png (shown in the client's Lobby Themes picker) and theme.txt, which tells
 * MenuBackground.java how to stack, move and animate the layers.
 *
 * Run: java tools/MenuArtGen.java assets/hook/textures/gui/menu
 *
 * theme.txt lines:
 *   layer <file> <w> <h> <sway> <scrollSpeed> <y> <pulse>   (drawn in order; sway = parallax amplitude in art px)
 *   fx <type> <layer|-> <x> <y> <w> <h> <rrggbb> <n>          (animated effect, coordinates in that layer's pixels)
 */
public final class MenuArtGen {
    static final int W = 640, H = 360;
    static Random RNG;

    public static void main(String[] args) throws Exception {
        File root = new File(args.length > 0 ? args[0] : ".");
        root.mkdirs();
        File[] old = root.listFiles();
        if (old != null) for (File f : old) if (f.isFile()) f.delete();
        theme(root, "castle", MenuArtGen::castleTheme);
        theme(root, "caves", MenuArtGen::cavesTheme);
        theme(root, "nether", MenuArtGen::netherTheme);
        theme(root, "end", MenuArtGen::endTheme);
        theme(root, "halloween", MenuArtGen::halloweenTheme);
        theme(root, "winter", MenuArtGen::winterTheme);
        theme(root, "hook", MenuArtGen::hookTheme);
        System.out.println("lobby themes written to " + root.getAbsolutePath());
    }

    // ------------------------------------------------------------ scene description

    static final class Scene {
        final List<String> lines = new ArrayList<>();
        final List<Object[]> layers = new ArrayList<>(); // name, image, sway, scroll, y

        void layer(String name, BufferedImage img, float sway, float scroll, int y, boolean pulse) {
            layers.add(new Object[]{name, img, sway, scroll, y});
            lines.add(String.format(java.util.Locale.ROOT, "layer %s.png %d %d %.1f %.1f %d %d", name, img.getWidth(), img.getHeight(), sway, scroll, y, pulse ? 1 : 0));
        }

        void fx(String type, String layer, int x, int y, int w, int h, int rgb, int n) {
            lines.add(String.format("fx %s %s %d %d %d %d %06x %d", type, layer, x, y, w, h, rgb & 0xFFFFFF, n));
        }
    }

    interface Builder {
        void build(Scene s) throws Exception;
    }

    static void theme(File root, String id, Builder builder) throws Exception {
        RNG = new Random(id.hashCode() * 31L + 20261111L);
        Scene s = new Scene();
        builder.build(s);
        File dir = new File(root, id);
        dir.mkdirs();
        File[] old = dir.listFiles();
        if (old != null) for (File f : old) f.delete();
        BufferedImage preview = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        for (Object[] l : s.layers) {
            BufferedImage img = (BufferedImage) l[1];
            ImageIO.write(img, "png", new File(dir, l[0] + ".png"));
            int ox = (W - img.getWidth()) / 2;
            if ((Float) l[3] != 0f) ox = 0;
            blit(preview, img, ox, (Integer) l[4]);
        }
        ImageIO.write(scaleDown(preview, 320, 180), "png", new File(dir, "preview.png"));
        try (PrintWriter out = new PrintWriter(new File(dir, "theme.txt"), "UTF-8")) {
            for (String line : s.lines) out.println(line);
        }
        System.out.println(id + ": " + s.layers.size() + " layers, " + (s.lines.size() - s.layers.size()) + " effects");
    }

    static void blit(BufferedImage dst, BufferedImage src, int ox, int oy) {
        for (int x = 0; x < src.getWidth(); x++) for (int y = 0; y < src.getHeight(); y++) {
            int dx = x + ox, dy = y + oy;
            if (dx < 0 || dy < 0 || dx >= dst.getWidth() || dy >= dst.getHeight()) continue;
            int c = src.getRGB(x, y);
            int a = c >>> 24;
            if (a == 0) continue;
            if (a == 255) { dst.setRGB(dx, dy, c); continue; }
            dst.setRGB(dx, dy, mix(dst.getRGB(dx, dy), c | 0xFF000000, a / 255.0));
        }
    }

    static BufferedImage scaleDown(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int fx = src.getWidth() / w, fy = src.getHeight() / h;
        for (int x = 0; x < w; x++) for (int y = 0; y < h; y++) out.setRGB(x, y, src.getRGB(x * fx, y * fy) | 0xFF000000);
        return out;
    }

    // ------------------------------------------------------------ pixel helpers

    static int rgb(int r, int g, int b) {
        return 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    static int argb(int a, int c) {
        return (clamp(a) << 24) | (c & 0xFFFFFF);
    }

    static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    static int hex(int c) {
        return 0xFF000000 | c;
    }

    static int shade(int c, int d) {
        return (c & 0xFF000000) | (rgb(((c >> 16) & 0xFF) + d, ((c >> 8) & 0xFF) + d, (c & 0xFF) + d) & 0xFFFFFF);
    }

    static int mix(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) Math.round(((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int g = (int) Math.round(((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = (int) Math.round((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return rgb(r, g, bl);
    }

    static BufferedImage img(int w, int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    static void set(BufferedImage img, int x, int y, int c) {
        if (x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight()) img.setRGB(x, y, c);
    }

    static int get(BufferedImage img, int x, int y) {
        return x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight() ? img.getRGB(x, y) : 0;
    }

    static void rect(BufferedImage img, int x, int y, int w, int h, int c) {
        for (int i = 0; i < w; i++) for (int j = 0; j < h; j++) set(img, x + i, y + j, c);
    }

    static final int[][] BAYER = {{0, 8, 2, 10}, {12, 4, 14, 6}, {3, 11, 1, 9}, {15, 7, 13, 5}};

    static boolean dither(int x, int y, double amount) {
        return (BAYER[y & 3][x & 3] + 0.5) / 16.0 < amount;
    }

    static double hash(int i, long seed) {
        long h = i * 374761393L + seed * 668265263L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        return ((h ^ (h >>> 16)) & 0xFFFF) / 65535.0;
    }

    static double hash2(int x, int y, long seed) {
        return hash(x * 73856093 ^ y * 19349663, seed);
    }

    static double noise(double x, long seed) {
        int i = (int) Math.floor(x);
        double f = x - i, t = f * f * (3 - 2 * f);
        return hash(i, seed) + (hash(i + 1, seed) - hash(i, seed)) * t;
    }

    static double fbm(double x, long seed) {
        return noise(x, seed) * 0.55 + noise(x * 2.1, seed + 7) * 0.27 + noise(x * 4.3, seed + 13) * 0.13 + noise(x * 8.7, seed + 19) * 0.05;
    }

    /** Vertical dithered gradient through the given colour stops down to y = horizon. */
    static BufferedImage gradient(int[] stops, double horizon) {
        BufferedImage img = img(W, H);
        for (int y = 0; y < H; y++) {
            double t = Math.min(1.0, y / horizon) * (stops.length - 1);
            int band = (int) Math.floor(t);
            double frac = t - band;
            for (int x = 0; x < W; x++) {
                int c = band >= stops.length - 1 ? stops[stops.length - 1] : dither(x, y, frac) ? stops[band + 1] : stops[band];
                img.setRGB(x, y, c);
            }
        }
        return img;
    }

    static void stars(BufferedImage img, int count, int maxY, int tint) {
        for (int i = 0; i < count; i++) {
            int x = RNG.nextInt(img.getWidth()), y = RNG.nextInt(maxY);
            int c = mix(hex(0xffffff), hex(tint), RNG.nextDouble() * 0.6);
            set(img, x, y, c);
            if (RNG.nextInt(12) == 0) {
                int d = shade(c, -110);
                set(img, x - 1, y, d); set(img, x + 1, y, d); set(img, x, y - 1, d); set(img, x, y + 1, d);
            }
        }
    }

    static void moon(BufferedImage img, int mx, int my, int mr, int body, int dark, int halo) {
        for (int r = 4; r >= 1; r--) {
            int rr = mr + r * 7;
            for (int x = mx - rr; x <= mx + rr; x++) for (int y = my - rr; y <= my + rr; y++) {
                if (Math.hypot(x - mx, y - my) <= rr && dither(x, y, 0.1 + 0.07 * (4 - r))) set(img, x, y, mix(get(img, x, y), halo, 0.3));
            }
        }
        for (int x = mx - mr; x <= mx + mr; x++) for (int y = my - mr; y <= my + mr; y++) {
            double d = Math.hypot(x - mx, y - my);
            if (d > mr) continue;
            int c = body;
            if (x - mx + (y - my) * 0.4 > mr * 0.45) c = dark;
            if (d > mr - 1.2) c = shade(dark, -12);
            set(img, x, y, c);
        }
        int[][] craters = {{-6, -5, 3}, {5, 3, 4}, {-2, 8, 2}, {8, -7, 2}, {-9, 4, 2}};
        for (int[] cr : craters) for (int x = -cr[2]; x <= cr[2]; x++) for (int y = -cr[2]; y <= cr[2]; y++) {
            if (x * x + y * y <= cr[2] * cr[2] && Math.hypot(cr[0] + x, cr[1] + y) < mr - 1) set(img, mx + cr[0] + x, my + cr[1] + y, shade(dark, -6));
        }
    }

    /** Mountain range silhouette with lit faces and optional snow caps. */
    static void range(BufferedImage img, long seed, int base, int amp, int body, int light, int snow, int step) {
        int w = img.getWidth();
        for (int x = 0; x < w; x += step) {
            double n = fbm(x / 80.0, seed);
            int top = base - (int) Math.round(n * amp * 1.7) + amp / 2;
            top -= top % step;
            double slope = fbm((x + 4) / 80.0, seed) - n;
            for (int dx = 0; dx < step && x + dx < w; dx++) for (int y = Math.max(0, top); y < H; y++) {
                int depth = y - top;
                int c = body;
                if (slope > 0.004 && depth < 34) c = mix(light, body, depth / 34.0);
                if (snow != 0 && top < base - amp * 0.25 && depth < 5 + (int) (hash(x, seed) * 5)) c = slope > 0.004 ? snow : shade(snow, -45);
                if (hash2(x + dx, y, seed) > 0.93) c = shade(c, -8);
                img.setRGB(x + dx, y, c);
            }
        }
    }

    static int[] terrain(int w, long seed, int base, int amp, int step, double scale) {
        int[] top = new int[w];
        for (int x = 0; x < w; x += step) {
            int t = base - (int) Math.round(fbm(x / scale, seed) * amp);
            t -= t % step;
            for (int dx = 0; dx < step && x + dx < w; dx++) top[x + dx] = t;
        }
        return top;
    }

    /** Fills below a height line with a base colour that darkens towards the bottom. */
    static void fillGround(BufferedImage img, int[] top, int surface, int body, int bottom, double fadeStart, long seed) {
        for (int x = 0; x < img.getWidth(); x++) for (int y = top[x]; y < H; y++) {
            int depth = y - top[x];
            int c = depth < 2 ? surface : body;
            if (depth >= 2 && hash2(x / 2, y / 2, seed) > 0.75) c = shade(c, -9);
            if (depth >= 2 && hash2(x, y, seed + 1) > 0.95) c = shade(c, 12);
            c = mix(c, bottom, Math.max(0, (y - fadeStart) / (H - fadeStart)) * 0.9);
            img.setRGB(x, y, c);
        }
    }

    static void spruce(BufferedImage img, int x, int ground, int leaf, int h, int snow) {
        rect(img, x, ground - 3, 2, 3, hex(0x241910));
        for (int j = 0; j < h; j++) {
            int y = ground - 3 - j;
            int half = Math.max(0, (h - j) / 3 + ((j % 4 == 0) ? 1 : 0));
            for (int i = -half; i <= half + 1; i++) {
                int c = i > 0 ? shade(leaf, -8) : shade(leaf, 8);
                if (snow != 0 && j % 4 == 0 && i > -half + 1) c = snow;
                set(img, x + i, y, c);
            }
        }
    }

    static void oak(BufferedImage img, int x, int ground, int leaf, int trunk) {
        int h = 6 + RNG.nextInt(4);
        rect(img, x, ground - h, 2, h, trunk);
        int lx = x - 5, ly = ground - h - 7;
        for (int i = 0; i < 12; i++) for (int j = 0; j < 9; j++) {
            if ((i < 2 || i > 9) && (j < 2 || j > 6) && RNG.nextInt(3) > 0) continue;
            int c = j < 2 ? shade(leaf, 14) : j > 6 ? shade(leaf, -12) : leaf;
            if (RNG.nextInt(6) == 0) c = shade(c, RNG.nextBoolean() ? 12 : -12);
            set(img, lx + i, ly + j, c);
        }
    }

    static void bigSpruce(BufferedImage img, int x, int ground, int leaf, int snow) {
        rect(img, x, ground - 8, 3, 8, hex(0x241910));
        int h = 66;
        for (int j = 0; j < h; j++) {
            int y = ground - 8 - j;
            int half = (h - j) / 3 + ((j % 6 < 2) ? 2 : 0);
            for (int i = -half; i <= half + 2; i++) {
                int c = i > 1 ? shade(leaf, -10) : shade(leaf, 10);
                if (hash2(i + x, j, 31) > 0.86) c = shade(c, 12);
                if (snow != 0 && j % 6 == 2 && i < half) c = snow;
                set(img, x + i, y, c);
            }
        }
    }

    static void grassBlades(BufferedImage img, int dark, int darker) {
        for (int x = 0; x < img.getWidth(); x++) {
            int blade = (int) (hash(x, 123) * 18 + Math.sin(x / 9.0) * 4) + 4;
            for (int j = 0; j < blade; j++) set(img, x, H - 1 - j, mix(dark, darker, j / (double) blade));
        }
    }

    /** Tileable blocky clouds. */
    static BufferedImage clouds(int count, int body, int top, int shadow) {
        int w = 1280, h = 110;
        BufferedImage img = img(w, h);
        for (int i = 0; i < count; i++) {
            int cx = RNG.nextInt(w), cy = 10 + RNG.nextInt(h - 34);
            int blocks = 3 + RNG.nextInt(5);
            for (int b = 0; b < blocks; b++) {
                int bw = 14 + RNG.nextInt(24), bh = 4 + RNG.nextInt(5);
                int bx = cx + b * 10 - blocks * 5, by = cy + RNG.nextInt(5) - (b % 2) * 3;
                for (int x = 0; x < bw; x++) for (int y = 0; y < bh; y++) {
                    int c = y == bh - 1 ? shadow : y == 0 ? top : body;
                    if (by + y >= 0 && by + y < h) img.setRGB(Math.floorMod(bx + x, w), by + y, c);
                }
            }
        }
        return img;
    }

    /** Tileable aurora curtain. */
    static BufferedImage aurora(int lowColor, int midColor, int highColor) {
        int w = 1280, h = 180;
        BufferedImage img = img(w, h);
        for (int x = 0; x < w; x++) {
            double phase = x / (double) w * Math.PI * 2;
            double center = 78 + 24 * Math.sin(phase * 3) + 12 * Math.sin(phase * 7 + 1.3);
            double strength = 0.4 + 0.6 * Math.pow(Math.sin(phase * 5 + 0.7) * 0.5 + 0.5, 1.4);
            double streak = 0.55 + 0.45 * hash(x / 2, 501);
            int length = (int) (40 + 28 * Math.sin(phase * 4 + 2));
            for (int y = 0; y < h; y++) {
                double dy = y - center;
                if (dy < -length || dy > 16) continue;
                double fall = dy > 0 ? 1 - dy / 16.0 : 1 - (-dy) / length;
                double a = strength * streak * Math.pow(Math.max(0, fall), 1.3);
                if (a <= 0.03 || !dither(x, y, a * 1.3)) continue;
                int c = dy > -6 ? hex(lowColor) : mix(hex(midColor), hex(highColor), Math.min(1, -dy / length * 1.3));
                img.setRGB(x, y, argb((int) (a * 200), c));
            }
        }
        return img;
    }

    static void portalFrame(BufferedImage img, Scene s, String layer, int x, int ground, int inner) {
        int obs = hex(0x150d22), obsLight = hex(0x2a1c40);
        int w = 24, h = 32, y = ground - h;
        for (int i = 0; i < w; i++) for (int j = 0; j < h; j++) {
            boolean frame = i < 4 || i >= w - 4 || j < 4 || j >= h - 4;
            set(img, x + i, y + j, frame ? ((i % 4 == 0 || j % 4 == 0) ? obsLight : obs) : hex(inner));
        }
        s.fx("portal", layer, x + 4, y + 4, w - 8, h - 8, 0xC889FF, 0);
    }

    static void campfire(BufferedImage img, Scene s, String layer, int x, int ground, int flame) {
        rect(img, x - 6, ground - 3, 13, 2, hex(0x5a3a20));
        rect(img, x - 4, ground - 4, 9, 1, hex(0x6e4a28));
        rect(img, x - 2, ground - 2, 5, 2, hex(0x2a1a10));
        s.fx("fire", layer, x, ground - 4, 5, 8, flame, 0);
    }

    static void lantern(BufferedImage img, Scene s, String layer, int x, int ground) {
        rect(img, x, ground - 22, 2, 22, hex(0x45301f));
        rect(img, x - 4, ground - 22, 10, 2, hex(0x45301f));
        rect(img, x - 6, ground - 18, 5, 5, hex(0x3a3a40));
        rect(img, x - 5, ground - 17, 3, 3, hex(0xffd27a));
        s.fx("glow", layer, x - 4, ground - 16, 1, 1, 0xFFD27A, 3);
    }

    static void torch(BufferedImage img, Scene s, String layer, int x, int ground) {
        rect(img, x, ground - 6, 1, 6, hex(0x6b4a2b));
        set(img, x, ground - 7, hex(0xffd36a));
        set(img, x, ground - 8, hex(0xfff2b8));
        s.fx("glow", layer, x, ground - 7, 1, 1, 0xFFB347, 3);
    }

    // ================================================================= THEMES

    // ------------------------------------------------------------ overworld castle (night, aurora)

    static void castleTheme(Scene s) {
        BufferedImage sky = gradient(new int[]{hex(0x05070f), hex(0x080c1c), hex(0x0d1430), hex(0x131d42), hex(0x1a2752), hex(0x22335f), hex(0x2a4068), hex(0x2f4f6e), hex(0x3a6170)}, 250);
        for (int i = 0; i < 1100; i++) {
            double u = RNG.nextDouble();
            int x = (int) (u * W), y = (int) (30 + u * 110 + RNG.nextGaussian() * 16);
            if (y >= 0 && y < 200) set(sky, x, y, mix(get(sky, x, y), hex(0x8fa4d0), 0.3 + RNG.nextDouble() * 0.3));
        }
        stars(sky, 280, 200, 0xa8c0ff);
        moon(sky, 486, 62, 18, hex(0xf2f3e8), hex(0xd8dacb), hex(0xcfe8ff));
        s.layer("sky", sky, 0, 0, 0, false);
        s.fx("shooting", "-", 0, 0, 640, 120, 0xF2F6FF, 11);
        s.layer("aurora", aurora(0x6cf5c0, 0x4fd1a5, 0x4a7cff), 0, 2.5f, 0, true);
        s.layer("clouds", clouds(14, 0xA0303a5c, 0xB0647898, 0x8c1c2440), 0, 4f, 26, false);

        BufferedImage far = img(720, H);
        range(far, 101, 196, 58, hex(0x26304f), hex(0x3d4f78), hex(0xd8e6f2), 4);
        range(far, 202, 220, 42, hex(0x1d2541), hex(0x2f3d63), hex(0xb8c8de), 4);
        range(far, 303, 240, 24, hex(0x161c33), hex(0x222c4a), 0, 3);
        s.layer("far", far, 6, 0, 0, false);

        BufferedImage mid = img(760, H);
        int[] mt = terrain(760, 404, 262, 24, 3, 60);
        for (int x = 300; x < 400; x++) mt[x] -= (int) (16 * Math.sin((x - 300) / 100.0 * Math.PI)) / 3 * 3;
        fillGround(mid, mt, hex(0x2f5a3c), hex(0x1f3a2c), hex(0x101628), 250, 9);
        for (int x = 4; x < 756; x += 5 + RNG.nextInt(6)) {
            if (x > 312 && x < 390) continue;
            if (RNG.nextInt(4) > 0) spruce(mid, x, mt[x], hex(0x173026), 14 + RNG.nextInt(12), 0);
            else oak(mid, x, mt[x], hex(0x1f3c2b), hex(0x33251a));
        }
        castle(mid, s, "mid", 324, mt[350], hex(0x434a5f), hex(0x2f3445), hex(0x59627a), hex(0x2c3f4a), 0x4fd1a5);
        s.layer("mid", mid, 11, 0, 0, false);

        BufferedImage near = img(800, H);
        int lake0 = 176, lake1 = 440, lakeY = 270, shoreY = 306;
        int[] top = terrain(800, 808, 272, 8, 2, 45);
        for (int x = 0; x < 150; x++) top[x] = 190 + (int) Math.round((fbm(x / 30.0, 31) - 0.5) * 10) / 2 * 2;
        for (int x = 150; x < lake0; x++) top[x] = 190 + (x - 150) * 3;
        for (int x = lake0; x < lake1; x++) top[x] = lakeY;
        lakeBand(near, lake0, lake1, lakeY, shoreY, hex(0x2e5d74), hex(0x10223a), 380);
        meadow(near, top, lake0, lake1, shoreY, hex(0x4b8a3c), hex(0x2f5a2c), hex(0x25472a), hex(0x0a120d), true);
        for (int x = 150; x < 176; x++) for (int y = top[x]; y < lakeY + 2; y++) set(near, x, y, (y / 3 + x / 4) % 3 == 0 ? hex(0x4f5560) : hex(0x3f444f));
        waterfall(near, s, 150, 12, top[150] - 1, lakeY, hex(0x4f7fc8), hex(0xbcd6ff), 0xE6F0FF);
        s.fx("shimmer", "near", lake0 + 6, lakeY + 3, lake1 - lake0 - 12, shoreY - lakeY - 6, 0xBFE4F0, 14);
        cherry(near, s, 488, top[488]);
        portalFrame(near, s, "near", 560, top[572], 0x6a24c9);
        campfire(near, s, "near", 650, top[650], 0xFF9A3C);
        lantern(near, s, "near", 700, top[700]);
        bigSpruce(near, 742, top[742], hex(0x143022), 0);
        bigSpruce(near, 112, top[112], hex(0x143022), 0);
        oak(near, 96, top[96], hex(0x2a5a28), hex(0x45301f));
        grassBlades(near, hex(0x0c1a12), hex(0x050906));
        s.fx("flies", "near", 20, 250, 760, 70, 0xE8FFF0, 22);
        s.layer("near", near, 18, 0, 0, false);
    }

    static void lakeBand(BufferedImage img, int x0, int x1, int y0, int y1, int top, int bottom, int reflectX) {
        for (int x = x0 - 6; x < x1 + 6; x++) for (int y = y0; y < y1 + 4; y++) {
            double d = (y - y0) / (double) (y1 - y0);
            int c = mix(top, bottom, d);
            if (Math.abs(x - reflectX) < 7 - d * 4 && ((y + x) & 1) == 0) c = mix(c, hex(0xe8f2ff), 0.5);
            if (((x / 6 + y) % 7 == 0) && hash2(x, y, 4) > 0.7) c = shade(c, 16);
            set(img, x, y, c);
        }
    }

    static void meadow(BufferedImage img, int[] top, int lake0, int lake1, int shoreY, int grassTop, int grass, int meadow, int dark, boolean flowers) {
        for (int x = 0; x < img.getWidth(); x++) {
            boolean water = x >= lake0 && x < lake1;
            int start = water ? shoreY : top[x];
            for (int y = start; y < H; y++) {
                int depth = y - start;
                int c = depth < 1 ? grassTop : depth < 3 ? grass : meadow;
                if (depth >= 3 && hash2(x / 2, y / 2, 3) > 0.72) c = shade(c, -10);
                if (depth >= 3 && hash2(x, y, 5) > 0.95) c = shade(c, 14);
                c = mix(c, dark, Math.min(0.85, Math.max(0, (y - 290) / 70.0) * 0.95));
                img.setRGB(x, y, c);
            }
            if (!water) {
                if (hash(x, 77) > 0.6) set(img, x, top[x] - 1, grassTop);
                if (hash(x, 78) > 0.86) { set(img, x, top[x] - 1, grassTop); set(img, x, top[x] - 2, shade(grassTop, 12)); }
                if (flowers && hash(x, 91) > 0.95) {
                    int flower = new int[]{hex(0xe8403a), hex(0xf2d33a), hex(0x8fb7ff), hex(0xf28ac0)}[(int) (hash(x, 5) * 4) & 3];
                    set(img, x, top[x] - 2, hex(0x3f8a2c));
                    set(img, x, top[x] - 3, flower);
                }
            } else if (hash(x, 99) > 0.55) {
                int hgt = 2 + (int) (hash(x, 98) * 5);
                for (int j = 0; j < hgt; j++) set(img, x, shoreY - 1 - j, hex(0x2d5a2a));
            }
        }
    }

    static void waterfall(BufferedImage img, Scene s, int x, int w, int top, int bottom, int dark, int light, int streak) {
        for (int i = x; i < x + w; i++) for (int y = top; y < bottom + 3; y++) set(img, i, y, mix(dark, light, ((i * 3 + y) % 5 == 0) ? 0.6 : 0.15));
        s.fx("fall", "near", x, top, w, bottom - top, streak, 9);
    }

    static void cherry(BufferedImage img, Scene s, int x, int ground) {
        int trunk = hex(0x3e2624), petal = hex(0xd88ab6), petalLight = hex(0xf0b4d2), petalDark = hex(0xa25c88);
        rect(img, x, ground - 22, 3, 22, trunk);
        rect(img, x + 3, ground - 17, 6, 2, trunk);
        rect(img, x - 6, ground - 14, 6, 2, trunk);
        int[][] blobs = {{x - 16, ground - 40, 22, 14}, {x - 2, ground - 46, 24, 16}, {x - 22, ground - 28, 16, 10}, {x + 8, ground - 30, 18, 10}};
        for (int[] b : blobs) for (int i = 0; i < b[2]; i++) for (int j = 0; j < b[3]; j++) {
            if ((i < 2 || i > b[2] - 3) && (j < 2 || j > b[3] - 3)) continue;
            int c = j < 3 ? petalLight : j > b[3] - 4 ? petalDark : petal;
            if (hash2(i + b[0], j + b[1], 61) > 0.8) c = shade(c, hash2(i, j, 3) > 0.5 ? 14 : -18);
            set(img, b[0] + i, b[1] + j, c);
        }
        s.fx("drop", "near", x - 22, ground - 30, 46, 50, 0xF0B4D2, 14);
    }

    static void castle(BufferedImage img, Scene s, String layer, int x, int ground, int stone, int dark, int light, int roof, int banner) {
        for (int i = 0; i < 56; i++) for (int j = 0; j < 22; j++) set(img, x + i, ground - 22 + j, ((j % 4 == 0) || ((i + (j / 4) * 2) % 6 == 0)) ? dark : stone);
        for (int i = 0; i < 56; i += 4) rect(img, x + i, ground - 25, 2, 3, stone);
        tower(img, x - 4, ground, 12, 40, stone, dark, light, roof);
        tower(img, x + 48, ground, 12, 40, stone, dark, light, roof);
        tower(img, x + 20, ground, 16, 54, stone, dark, light, roof);
        rect(img, x + 25, ground - 12, 6, 12, hex(0x14121a));
        int[][] win = {{x + 1, ground - 30}, {x + 52, ground - 30}, {x + 27, ground - 44}, {x + 27, ground - 34}, {x + 12, ground - 14}, {x + 40, ground - 14}};
        for (int[] wv : win) {
            rect(img, wv[0], wv[1], 3, 4, hex(0x14121a));
            rect(img, wv[0], wv[1] + 1, 3, 3, hex(0xffc76a));
            s.fx("window", layer, wv[0], wv[1] + 1, 3, 3, 0xFFC76A, 2);
        }
        rect(img, x + 28, ground - 74, 1, 12, hex(0x8a8a8a));
        rect(img, x + 29, ground - 74, 7, 4, hex(banner));
        rect(img, x + 29, ground - 70, 5, 1, shade(hex(banner), -30));
    }

    static void tower(BufferedImage img, int x, int ground, int w, int h, int stone, int dark, int light, int roof) {
        for (int i = 0; i < w; i++) for (int j = 0; j < h; j++) set(img, x + i, ground - h + j, (j % 4 == 0 || (i + (j / 4) * 2) % 5 == 0) ? dark : (i < 2 ? light : stone));
        for (int k = 0; k < w / 2 + 2; k++) rect(img, x - 1 + k, ground - h - 1 - k, w + 2 - 2 * k, 1, k % 2 == 0 ? roof : shade(roof, 14));
    }

    // ------------------------------------------------------------ overworld caves

    static void cavesTheme(Scene s) {
        // back wall of a huge cavern: deepslate with darkness towards the top
        BufferedImage back = img(W, H);
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) {
            int c = ((x / 4 + y / 4) % 2 == 0) ? hex(0x2c3242) : hex(0x272c3b);
            if (hash2(x / 2, y / 2, 5) > 0.82) c = shade(c, -8);
            if (hash2(x, y, 6) > 0.97) c = shade(c, 10);
            // warm light from the lava fall on the left, cool light from the crystals on the right
            double lava = Math.max(0, 1 - Math.hypot(x - 70, y - 230) / 230.0);
            double cool = Math.max(0, 1 - Math.hypot(x - 560, y - 290) / 200.0);
            c = mix(c, hex(0x9a4a22), lava * 0.55);
            c = mix(c, hex(0x5a3a9a), cool * 0.35);
            c = mix(c, hex(0x07070b), Math.max(0, 1 - y / 200.0) * 0.6);
            back.setRGB(x, y, c);
        }
        // glow lichen and distant ore specks on the wall
        for (int i = 0; i < 160; i++) {
            int x = RNG.nextInt(W), y = 60 + RNG.nextInt(200);
            int c = RNG.nextInt(5) == 0 ? hex(0x4ff0e8) : hex(0x8fae6a);
            set(back, x, y, mix(get(back, x, y), c, 0.55));
        }
        s.layer("sky", back, 0, 0, 0, false);
        s.fx("dust", "-", 0, 0, 640, 360, 0xC8C2B0, 26);

        // stalactites hanging from the ceiling + far stalagmites
        BufferedImage far = img(720, H);
        for (int x = 0; x < 720; x += 4) {
            int len = (int) (20 + fbm(x / 23.0, 71) * 90);
            for (int dx = 0; dx < 4; dx++) for (int y = 0; y < len - Math.abs(dx - 2) * 3; y++) set(far, x + dx, y, mix(hex(0x1a1b22), hex(0x2c2d36), y / (double) len));
            int g = 230 - (int) (fbm(x / 31.0, 72) * 70);
            g -= g % 4;
            for (int dx = 0; dx < 4; dx++) for (int y = g; y < H; y++) set(far, x + dx, y, mix(hex(0x30313b), hex(0x14151b), (y - g) / 120.0));
        }
        s.layer("far", far, 5, 0, 0, false);

        // mid: lava fall on the left, mineshaft supports, ore veins
        BufferedImage mid = img(760, H);
        int[] mt = terrain(760, 404, 286, 30, 4, 50);
        fillGround(mid, mt, hex(0x4a4b55), hex(0x3a3b44), hex(0x0d0d12), 290, 12);
        for (int x = 0; x < 760; x += 4) for (int y = mt[x] + 4; y < H; y += 4) {
            double o = hash2(x / 4, y / 4, 81);
            if (o > 0.985) rect(mid, x, y, 2, 2, hex(0x4ff0e8));       // diamond
            else if (o > 0.97) rect(mid, x, y, 2, 2, hex(0xff3b3b));   // redstone
            else if (o > 0.955) rect(mid, x, y, 2, 2, hex(0xe8c24a));  // gold
        }
        int lavaX = 120;
        for (int x = lavaX; x < lavaX + 14; x++) for (int y = 0; y < mt[x + 5]; y++) set(mid, x, y, ((x + y) % 3 == 0) ? hex(0xffb13d) : hex(0xf06a1c));
        for (int x = lavaX - 30; x < lavaX + 44; x++) for (int y = mt[x] - 2; y < mt[x] + 6; y++) set(mid, x, y, ((x + y) % 4 == 0) ? hex(0xffc14a) : hex(0xf0601c));
        s.fx("fall", "mid", lavaX, 0, 14, mt[lavaX + 5], 0xFFE08A, 12);
        s.fx("glow", "mid", lavaX + 7, mt[lavaX + 5] - 4, 1, 1, 0xFF7A20, 8);
        s.fx("lava", "mid", lavaX - 30, mt[lavaX] - 2, 74, 6, 0xFFD27A, 6);
        for (int k = 0; k < 3; k++) {
            int sx = 330 + k * 46;
            rect(mid, sx, mt[sx] - 34, 3, 34, hex(0x6b4a2b));
            rect(mid, sx + 30, mt[sx + 30] - 34, 3, 34, hex(0x6b4a2b));
            rect(mid, sx, mt[sx] - 37, 33, 3, hex(0x7d5a36));
        }
        for (int x = 320; x < 480; x++) {
            set(mid, x, mt[x] - 1, (x % 4 == 0) ? hex(0x6b4a2b) : hex(0x8a8a92));
        }
        s.layer("mid", mid, 10, 0, 0, false);

        // near: amethyst geode, glow berries, torches, underground lake
        BufferedImage near = img(800, H);
        int[] top = terrain(800, 909, 300, 18, 2, 40);
        int pool0 = 300, pool1 = 470;
        for (int x = pool0; x < pool1; x++) top[x] = 316;
        for (int x = 0; x < 800; x++) {
            boolean pool = x >= pool0 && x < pool1;
            for (int y = top[x]; y < H; y++) {
                int c;
                if (pool && y < 344) c = mix(hex(0x1d4a5a), hex(0x0a1a24), (y - 316) / 28.0);
                else {
                    c = ((x / 3 + y / 3) % 2 == 0) ? hex(0x3a3b44) : hex(0x34353e);
                    if (y - top[x] < 2) c = hex(0x5b5c66);
                    if (hash2(x / 2, y / 2, 8) > 0.8) c = shade(c, -8);
                    c = mix(c, hex(0x050508), Math.min(0.85, Math.max(0, (y - 300) / 60.0)));
                }
                near.setRGB(x, y, c);
            }
        }
        s.fx("shimmer", "near", pool0 + 4, 318, pool1 - pool0 - 8, 22, 0x7FD8E8, 10);
        geode(near, s, 600, top[600]);
        geode(near, s, 70, top[70]);
        for (int k = 0; k < 6; k++) {
            int vx = 180 + k * 28 + RNG.nextInt(10);
            int len = 40 + RNG.nextInt(60);
            for (int y = 0; y < len; y++) set(near, vx, y, (y % 5 == 0) ? hex(0x3f6a2c) : hex(0x2d5222));
            for (int b = 8; b < len; b += 9 + RNG.nextInt(6)) {
                rect(near, vx - 1, b, 3, 2, hex(0xffa63a));
                s.fx("glow", "near", vx, b, 1, 1, 0xFFB050, 2);
            }
        }
        torch(near, s, "near", 250, top[250]);
        torch(near, s, "near", 520, top[520]);
        torch(near, s, "near", 710, top[710]);
        s.fx("drip", "near", 160, 0, 520, 316, 0x9FC8FF, 6);
        s.layer("near", near, 16, 0, 0, false);
    }

    static void geode(BufferedImage img, Scene s, int cx, int ground) {
        int[][] crystals = {{-14, 22}, {-8, 30}, {-2, 38}, {4, 28}, {10, 34}, {16, 20}};
        for (int[] c : crystals) {
            int x = cx + c[0], h = c[1];
            for (int j = 0; j < h; j++) {
                int half = Math.max(0, 3 - j / Math.max(1, h / 4));
                for (int i = -half; i <= half; i++) set(img, x + i, ground - 1 - j, i < 0 ? hex(0xb88cff) : i == 0 ? hex(0xe2c8ff) : hex(0x7a4fd1));
            }
        }
        s.fx("glow", "near", cx, ground - 18, 1, 1, 0xA070FF, 6);
        s.fx("sparkle", "near", cx - 18, ground - 40, 36, 38, 0xF0E0FF, 8);
    }

    // ------------------------------------------------------------ nether

    static void netherTheme(Scene s) {
        BufferedImage sky = gradient(new int[]{hex(0x120406), hex(0x1f0708), hex(0x2e0a0a), hex(0x43100c), hex(0x5c180e), hex(0x7a2410), hex(0x96361a)}, 300);
        s.layer("sky", sky, 0, 0, 0, false);
        s.fx("ash", "-", 0, 0, 640, 360, 0x6a5a58, 40);

        BufferedImage far = img(720, H);
        range(far, 51, 210, 70, hex(0x3a0f12), hex(0x5a1a1a), 0, 4);
        range(far, 52, 236, 46, hex(0x2a0a0d), hex(0x46131a), 0, 4);
        s.layer("far", far, 6, 0, 0, false);

        // mid: nether fortress bridge + basalt pillars
        BufferedImage mid = img(760, H);
        int[] mt = terrain(760, 61, 270, 26, 3, 50);
        fillGround(mid, mt, hex(0x6a2121), hex(0x4f1818), hex(0x160606), 260, 21);
        int brick = hex(0x2c1316), brickLight = hex(0x3d1b1f);
        for (int x = 160; x < 600; x++) {
            int deckY = 216;
            for (int y = deckY; y < deckY + 6; y++) set(mid, x, y, (x % 4 == 0 || y == deckY) ? brickLight : brick);
            if (x % 6 < 2) set(mid, x, deckY - 1, brick);
        }
        for (int k = 0; k < 6; k++) {
            int px = 180 + k * 80;
            for (int x = px; x < px + 10; x++) for (int y = 222; y < mt[x]; y++) set(mid, x, y, (y % 4 == 0 || x == px) ? brickLight : brick);
        }
        rect(mid, 360, 186, 40, 30, brick);
        for (int i = 0; i < 40; i += 5) rect(mid, 360 + i, 183, 3, 3, brick);
        rect(mid, 372, 196, 4, 6, hex(0xff7a2a));
        rect(mid, 384, 196, 4, 6, hex(0xff7a2a));
        s.fx("window", "mid", 372, 196, 4, 6, 0xFF7A2A, 2);
        s.fx("window", "mid", 384, 196, 4, 6, 0xFF7A2A, 2);
        for (int k = 0; k < 9; k++) {
            int bx = 20 + k * 82 + RNG.nextInt(20);
            int h = 40 + RNG.nextInt(60);
            for (int x = bx; x < bx + 8; x++) for (int y = mt[bx] - h; y < mt[bx]; y++) set(mid, x, y, (x - bx < 2) ? hex(0x4a4650) : hex(0x35323b));
        }
        s.layer("mid", mid, 11, 0, 0, false);

        // near: lava lake, crimson + warped fungi, soul fire, portal
        BufferedImage near = img(800, H);
        int[] top = terrain(800, 71, 280, 10, 2, 40);
        int lava0 = 230, lava1 = 520, lavaY = 288;
        for (int x = lava0; x < lava1; x++) top[x] = lavaY;
        for (int x = 0; x < 800; x++) {
            boolean lava = x >= lava0 && x < lava1;
            for (int y = top[x]; y < H; y++) {
                int c;
                if (lava && y < 330) {
                    c = ((x / 3 + y) % 5 == 0) ? hex(0xffc14a) : mix(hex(0xff7a1c), hex(0xc8400c), (y - lavaY) / 42.0);
                } else {
                    int depth = y - top[x];
                    c = depth < 2 ? (x % 7 < 3 ? hex(0x8e2a2a) : hex(0x2a7a72)) : ((x / 3 + y / 3) % 2 == 0 ? hex(0x5a1c1c) : hex(0x521818));
                    if (hash2(x / 2, y / 2, 9) > 0.8) c = shade(c, -10);
                    c = mix(c, hex(0x0c0303), Math.min(0.85, Math.max(0, (y - 296) / 64.0)));
                }
                near.setRGB(x, y, c);
            }
        }
        s.fx("lava", "near", lava0, lavaY, lava1 - lava0, 40, 0xFFD27A, 12);
        s.fx("glow", "near", (lava0 + lava1) / 2, lavaY, 1, 1, 0xFF6A1C, 9);
        fungus(near, 110, top[110], hex(0x9a1f2a), hex(0xff6a3a));
        fungus(near, 660, top[660], hex(0x167a70), hex(0x4fd1a5));
        fungus(near, 740, top[740], hex(0x167a70), hex(0x4fd1a5));
        fungus(near, 40, top[40], hex(0x9a1f2a), hex(0xff6a3a));
        portalFrame(near, s, "near", 570, top[580], 0x6a24c9);
        campfire(near, s, "near", 190, top[190], 0x7FE8FF);
        s.fx("rise", "near", lava0, lavaY - 60, lava1 - lava0, 60, 0xFFB13D, 18);
        grassBlades(near, hex(0x2a0a0a), hex(0x0c0303));
        s.layer("near", near, 18, 0, 0, false);
    }

    static void fungus(BufferedImage img, int x, int ground, int cap, int light) {
        rect(img, x, ground - 26, 3, 26, hex(0x5a2a3a));
        for (int j = 0; j < 14; j++) {
            int half = 12 - j / 2;
            for (int i = -half; i <= half + 2; i++) set(img, x + i, ground - 26 - j, hash2(i + x, j, 4) > 0.85 ? hex(light) : (j < 3 ? shade(hex(cap), -20) : hex(cap)));
        }
        for (int k = 0; k < 4; k++) set(img, x - 8 + k * 6, ground - 25, hex(light));
    }

    // ------------------------------------------------------------ the end

    static void endTheme(Scene s) {
        BufferedImage sky = gradient(new int[]{hex(0x050309), hex(0x0b0614), hex(0x140a22), hex(0x1d0f2e), hex(0x26143a), hex(0x2e1a44)}, 300);
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) if (hash2(x / 3, y / 3, 11) > 0.97) set(sky, x, y, mix(get(sky, x, y), hex(0x6a4a8a), 0.4));
        stars(sky, 220, 300, 0xd0a8ff);
        s.layer("sky", sky, 0, 0, 0, false);
        s.fx("rise", "-", 0, 120, 640, 240, 0xC98BFF, 30);

        // far: floating end islands
        BufferedImage far = img(720, H);
        int[][] islands = {{90, 150, 70}, {300, 110, 50}, {520, 160, 90}, {660, 120, 40}};
        for (int[] is : islands) island(far, is[0], is[1], is[2], hex(0xc9c48a), hex(0x8f8a5a));
        s.layer("far", far, 6, 0, 0, false);

        // mid: end city towers with lit windows
        BufferedImage mid = img(760, H);
        int[] mt = terrain(760, 33, 270, 16, 3, 60);
        fillGround(mid, mt, hex(0xdcd69a), hex(0xbab478), hex(0x2a2440), 240, 3);
        endCity(mid, s, 420, mt[440]);
        endCity(mid, s, 160, mt[180]);
        s.layer("mid", mid, 11, 0, 0, false);

        // near: end stone, chorus plants, obsidian pillars with crystals and a dragon egg
        BufferedImage near = img(800, H);
        int[] top = terrain(800, 44, 290, 12, 2, 40);
        fillGround(near, top, hex(0xe6e0a4), hex(0xcfc98c), hex(0x0c0a14), 300, 7);
        for (int k = 0; k < 2; k++) {
            int px = k == 0 ? 120 : 640;
            int h = 120 + k * 20;
            for (int x = px; x < px + 22; x++) for (int y = top[px + 10] - h; y < top[px + 10]; y++) set(near, x, y, (x - px < 3) ? hex(0x2e2148) : ((x + y) % 7 == 0 ? hex(0x241a38) : hex(0x150d22)));
            int cy = top[px + 10] - h - 10;
            rect(near, px + 6, cy, 10, 10, hex(0xf0a8ff));
            rect(near, px + 8, cy + 2, 6, 6, hex(0xffffff));
            s.fx("crystal", "near", px + 11, cy + 5, 10, 10, 0xF0A8FF, 0);
            s.fx("beam", "near", px + 11, 0, 2, cy, 0xD48CFF, 0);
        }
        for (int k = 0; k < 7; k++) chorus(near, 220 + k * 60 + RNG.nextInt(20), top[220 + k * 60]);
        rect(near, 392, top[400] - 4, 16, 4, hex(0x1f1f24));
        for (int j = 0; j < 12; j++) {
            int half = j < 3 ? 3 + j : j < 8 ? 6 : 6 - (j - 8);
            for (int i = -half; i <= half; i++) set(near, 400 + i, top[400] - 5 - j, (i + j) % 5 == 0 ? hex(0x4a1a6a) : hex(0x12081c));
        }
        s.fx("sparkle", "near", 388, top[400] - 24, 24, 20, 0xC070FF, 6);
        grassBlades(near, hex(0x2a2638), hex(0x0a0810));
        s.layer("near", near, 18, 0, 0, false);
    }

    static void island(BufferedImage img, int cx, int cy, int w) {
        island(img, cx, cy, w, hex(0xc9c48a), hex(0x8f8a5a));
    }

    static void island(BufferedImage img, int cx, int cy, int w, int top, int under) {
        for (int x = cx - w / 2; x < cx + w / 2; x++) {
            double u = (x - cx) / (w / 2.0);
            int depth = (int) ((1 - u * u) * w * 0.45);
            for (int y = cy; y < cy + depth; y++) set(img, x, y, y < cy + 2 ? top : mix(under, hex(0x2a2238), (y - cy) / (double) Math.max(1, depth)));
        }
    }

    static void endCity(BufferedImage img, Scene s, int x, int ground) {
        int purpur = hex(0xa97aa8), dark = hex(0x7a527a), light = hex(0xc9a0c8);
        int[][] parts = {{0, 30, 24}, {4, 60, 16}, {-6, 82, 36}, {2, 104, 20}};
        int base = ground;
        for (int[] p : parts) {
            for (int i = 0; i < p[2]; i++) for (int j = 0; j < 22; j++) set(img, x + p[0] + i, base - p[1] + j, (i < 2) ? light : ((i + j) % 6 == 0 ? dark : purpur));
            rect(img, x + p[0] + p[2] / 2 - 2, base - p[1] + 8, 4, 5, hex(0xffe48a));
            s.fx("window", "mid", x + p[0] + p[2] / 2 - 2, base - p[1] + 8, 4, 5, 0xFFE48A, 2);
        }
        for (int j = 0; j < 30; j++) set(img, x + 12, base - 104 - j, dark);
    }

    static void chorus(BufferedImage img, int x, int ground) {
        int stem = hex(0x7a5a8a), flower = hex(0xe2c8f0);
        int h = 18 + RNG.nextInt(20);
        for (int j = 0; j < h; j++) rect(img, x, ground - j, 2, 1, stem);
        for (int b = 6; b < h; b += 7) {
            int dir = RNG.nextBoolean() ? 1 : -1;
            rect(img, dir > 0 ? x + 2 : x - 5, ground - b, 5, 2, stem);
            rect(img, dir > 0 ? x + 6 : x - 6, ground - b - 2, 3, 3, flower);
        }
        rect(img, x - 1, ground - h - 2, 4, 3, flower);
    }

    // ------------------------------------------------------------ halloween edition

    static void halloweenTheme(Scene s) {
        BufferedImage sky = gradient(new int[]{hex(0x0b0614), hex(0x1a0b24), hex(0x2c1030), hex(0x481838), hex(0x6e2234), hex(0x9a3a28), hex(0xc8642a)}, 270);
        stars(sky, 150, 160, 0xffd2a8);
        moon(sky, 200, 70, 26, hex(0xffb347), hex(0xe88a2a), hex(0xff9a3c));
        s.layer("sky", sky, 0, 0, 0, false);
        s.fx("bats", "-", 0, 30, 640, 140, 0x0a0608, 7);
        s.layer("clouds", clouds(12, 0x9a2a1830, 0xB0603048, 0x80140a18), 0, 5f, 30, false);

        BufferedImage far = img(720, H);
        range(far, 81, 230, 40, hex(0x1e0f22), hex(0x2e1834), 0, 4);
        s.layer("far", far, 6, 0, 0, false);

        BufferedImage mid = img(760, H);
        int[] mt = terrain(760, 82, 268, 22, 3, 55);
        fillGround(mid, mt, hex(0x2a2a1c), hex(0x1c1c14), hex(0x0c0808), 250, 5);
        for (int x = 6; x < 756; x += 14 + RNG.nextInt(16)) deadTree(mid, x, mt[x], hex(0x140c10), 26 + RNG.nextInt(20));
        hauntedHouse(mid, s, 360, mt[380]);
        s.layer("mid", mid, 11, 0, 0, false);
        s.fx("fog", "-", 0, 250, 640, 60, 0x9a8aa8, 0);

        BufferedImage near = img(800, H);
        int[] top = terrain(800, 83, 284, 10, 2, 40);
        fillGround(near, top, hex(0x3a3a24), hex(0x24241a), hex(0x060404), 296, 6);
        for (int k = 0; k < 6; k++) grave(near, 120 + k * 52 + RNG.nextInt(14), top[120 + k * 52]);
        int[] pumpkins = {60, 470, 540, 690, 740};
        for (int px : pumpkins) pumpkin(near, s, px, top[px]);
        deadTree(near, 20, top[20], hex(0x0c0608), 90);
        deadTree(near, 780, top[780], hex(0x0c0608), 80);
        lantern(near, s, "near", 610, top[610]);
        grassBlades(near, hex(0x14140c), hex(0x040302));
        s.fx("flies", "near", 20, 250, 760, 70, 0xFFB347, 14);
        s.layer("near", near, 18, 0, 0, false);
    }

    static void deadTree(BufferedImage img, int x, int ground, int c, int h) {
        rect(img, x, ground - h, 3, h, c);
        for (int b = h / 3; b < h; b += 8 + RNG.nextInt(6)) {
            int dir = RNG.nextBoolean() ? 1 : -1, len = 6 + RNG.nextInt(10);
            for (int i = 0; i < len; i++) set(img, x + 1 + dir * i, ground - b - i / 2, c);
        }
    }

    static void hauntedHouse(BufferedImage img, Scene s, int x, int ground) {
        int wall = hex(0x2a2030), dark = hex(0x1a1420), roof = hex(0x120c16);
        rect(img, x, ground - 40, 50, 40, wall);
        for (int j = 0; j < 40; j += 4) rect(img, x, ground - 40 + j, 50, 1, dark);
        for (int k = 0; k < 18; k++) rect(img, x - 4 + k, ground - 41 - k, 58 - 2 * k, 1, roof);
        rect(img, x + 34, ground - 70, 10, 30, wall);
        for (int k = 0; k < 8; k++) rect(img, x + 33 + k / 2, ground - 71 - k, 12 - k, 1, roof);
        int[][] win = {{x + 6, ground - 32}, {x + 22, ground - 32}, {x + 37, ground - 60}, {x + 6, ground - 16}};
        for (int[] wv : win) {
            rect(img, wv[0], wv[1], 6, 6, hex(0xffb347));
            s.fx("window", "mid", wv[0], wv[1], 6, 6, 0xFF9A2A, 3);
        }
        rect(img, x + 22, ground - 14, 8, 14, hex(0x0a0608));
    }

    static void grave(BufferedImage img, int x, int ground) {
        int stone = hex(0x5a5866), dark = hex(0x3c3a46);
        rect(img, x, ground - 14, 10, 14, stone);
        rect(img, x + 1, ground - 16, 8, 2, stone);
        rect(img, x + 2, ground - 17, 6, 1, stone);
        rect(img, x + 4, ground - 12, 2, 6, dark);
        rect(img, x + 2, ground - 10, 6, 2, dark);
    }

    static void pumpkin(BufferedImage img, Scene s, int x, int ground) {
        int body = hex(0xe07a1c), rib = hex(0xb85a10);
        for (int i = 0; i < 12; i++) for (int j = 0; j < 9; j++) {
            if ((i == 0 || i == 11) && (j == 0 || j == 8)) continue;
            set(img, x + i, ground - 9 + j, i % 4 == 0 ? rib : body);
        }
        rect(img, x + 5, ground - 11, 2, 2, hex(0x3a6a1a));
        rect(img, x + 2, ground - 7, 2, 2, hex(0xffe08a));
        rect(img, x + 8, ground - 7, 2, 2, hex(0xffe08a));
        rect(img, x + 3, ground - 4, 6, 1, hex(0xffe08a));
        s.fx("glow", "near", x + 6, ground - 5, 1, 1, 0xFF9A2A, 3);
    }

    // ------------------------------------------------------------ winter edition

    static void winterTheme(Scene s) {
        BufferedImage sky = gradient(new int[]{hex(0x0a1020), hex(0x121c34), hex(0x1c2c4c), hex(0x2a4064), hex(0x3c5678), hex(0x5a7290), hex(0x8aa0b8)}, 260);
        stars(sky, 220, 190, 0xd8e8ff);
        moon(sky, 120, 64, 16, hex(0xf4f8ff), hex(0xd8e2ee), hex(0xe8f4ff));
        s.layer("sky", sky, 0, 0, 0, false);
        s.layer("aurora", aurora(0x9af0ff, 0x6ac8ff, 0xb08cff), 0, 2f, 0, true);
        s.fx("snow", "-", 0, 0, 640, 360, 0xF4F8FF, 70);

        BufferedImage far = img(720, H);
        range(far, 91, 200, 60, hex(0x8ea4c0), hex(0xc8d8ea), hex(0xffffff), 4);
        range(far, 92, 226, 40, hex(0x6c84a4), hex(0x9cb4d0), hex(0xf0f6ff), 4);
        s.layer("far", far, 6, 0, 0, false);

        BufferedImage mid = img(760, H);
        int[] mt = terrain(760, 93, 266, 20, 3, 55);
        fillGround(mid, mt, hex(0xf0f4fa), hex(0xd4dce8), hex(0x5a6a84), 250, 5);
        for (int x = 4; x < 756; x += 6 + RNG.nextInt(7)) {
            if (x > 300 && x < 460) continue;
            spruce(mid, x, mt[x], hex(0x1c3a34), 14 + RNG.nextInt(12), hex(0xeef4fa));
        }
        for (int k = 0; k < 3; k++) cabinSnow(mid, s, 310 + k * 52, mt[320 + k * 52]);
        s.layer("mid", mid, 11, 0, 0, false);

        BufferedImage near = img(800, H);
        int[] top = terrain(800, 94, 284, 10, 2, 40);
        int ice0 = 240, ice1 = 470;
        for (int x = ice0; x < ice1; x++) top[x] = 290;
        for (int x = 0; x < 800; x++) for (int y = top[x]; y < H; y++) {
            boolean ice = x >= ice0 && x < ice1 && y < 318;
            int c = ice ? (((x + y * 3) % 23 == 0) ? hex(0xe8f6ff) : mix(hex(0x9cc8e8), hex(0x6a98c0), (y - 290) / 28.0))
                    : (y - top[x] < 2 ? hex(0xffffff) : hex(0xe4eaf2));
            if (!ice && hash2(x / 2, y / 2, 3) > 0.8) c = shade(c, -10);
            c = mix(c, hex(0x2a3448), Math.min(0.8, Math.max(0, (y - 300) / 70.0)));
            near.setRGB(x, y, c);
        }
        s.fx("shimmer", "near", ice0 + 4, 292, ice1 - ice0 - 8, 22, 0xFFFFFF, 10);
        bigSpruce(near, 60, top[60], hex(0x143022), hex(0xf4f8ff));
        bigSpruce(near, 750, top[750], hex(0x143022), hex(0xf4f8ff));
        igloo(near, 560, top[580]);
        snowman(near, 160, top[160]);
        campfire(near, s, "near", 660, top[660], 0xFF9A3C);
        lantern(near, s, "near", 500, top[500]);
        grassBlades(near, hex(0xb8c4d4), hex(0x5a6a80));
        s.layer("near", near, 18, 0, 0, false);
    }

    static void cabinSnow(BufferedImage img, Scene s, int x, int ground) {
        rect(img, x, ground - 16, 30, 16, hex(0x7d5f37));
        for (int j = 0; j < 16; j += 4) rect(img, x, ground - 16 + j, 30, 1, hex(0x5e4127));
        for (int k = 0; k < 10; k++) rect(img, x - 3 + k * 2, ground - 17 - k * 2, 36 - k * 4, 2, k % 2 == 0 ? hex(0xf4f8ff) : hex(0xd4dce8));
        rect(img, x + 6, ground - 10, 5, 4, hex(0xffcf73));
        rect(img, x + 19, ground - 10, 5, 4, hex(0xffcf73));
        s.fx("window", "mid", x + 6, ground - 10, 5, 4, 0xFFCF73, 2);
        s.fx("window", "mid", x + 19, ground - 10, 5, 4, 0xFFCF73, 2);
        rect(img, x + 22, ground - 30, 4, 8, hex(0x6d6d74));
        s.fx("smoke", "mid", x + 24, ground - 30, 1, 1, 0xC8D0DC, 4);
    }

    static void igloo(BufferedImage img, int cx, int ground) {
        for (int x = -22; x <= 22; x++) {
            int h = (int) Math.sqrt(22 * 22 - x * x);
            for (int y = 0; y < h; y++) set(img, cx + x, ground - y, ((y / 4 + (x + 22) / 6) % 2 == 0) ? hex(0xf4f8ff) : hex(0xdce6f2));
        }
        for (int x = -5; x <= 5; x++) for (int y = 0; y < 9 - Math.abs(x) / 2; y++) set(img, cx + x, ground - y, hex(0x2a3a52));
    }

    static void snowman(BufferedImage img, int x, int ground) {
        int[][] balls = {{0, 0, 9}, {0, 15, 7}, {0, 26, 5}};
        for (int[] b : balls) for (int i = -b[2]; i <= b[2]; i++) for (int j = -b[2]; j <= b[2]; j++) if (i * i + j * j <= b[2] * b[2]) set(img, x + i, ground - b[2] - b[1] + j, i > b[2] / 2 ? hex(0xd4dce8) : hex(0xffffff));
        rect(img, x - 3, ground - 34, 2, 2, hex(0x1a1a1a));
        rect(img, x + 2, ground - 34, 2, 2, hex(0x1a1a1a));
        rect(img, x, ground - 32, 4, 1, hex(0xff8a2a));
        rect(img, x - 5, ground - 41, 10, 2, hex(0x2a2a30));
        rect(img, x - 3, ground - 46, 6, 5, hex(0x2a2a30));
    }

    // ------------------------------------------------------------ HookVisuals special

    static void hookTheme(Scene s) {
        BufferedImage sky = gradient(new int[]{hex(0x020406), hex(0x05090c), hex(0x081014), hex(0x0b1a1c), hex(0x0f2626), hex(0x14342e), hex(0x1a4436)}, 280);
        stars(sky, 240, 230, 0xb8ffe6);
        s.layer("sky", sky, 0, 0, 0, false);
        s.layer("aurora", aurora(0x6cf5c0, 0x4fd1a5, 0x2a8f75), 0, 3f, 0, true);

        // far: floating islands with mint crystals
        BufferedImage far = img(720, H);
        int[][] islands = {{80, 170, 60}, {230, 120, 46}, {500, 140, 70}, {660, 180, 50}};
        for (int[] is : islands) {
            island(far, is[0], is[1], is[2], hex(0x3a6a4c), hex(0x26302e));
            for (int k = -1; k <= 1; k++) rect(far, is[0] + k * 10, is[1] - 6, 2, 6, hex(0x6cf5c0));
        }
        s.layer("far", far, 6, 0, 0, false);

        // mid: the big HK logo built from blocks on a platform, mint beacon beams
        BufferedImage mid = img(760, H);
        int[] mt = terrain(760, 97, 276, 10, 3, 60);
        fillGround(mid, mt, hex(0x1c2e28), hex(0x121c1a), hex(0x040606), 260, 5);
        hkBlocks(mid, s, 590, 172); // right of the menu buttons, below the Minecraft logo
        int[] beams = {120, 250, 510, 640};
        for (int bx : beams) {
            rect(mid, bx - 4, mt[bx] - 6, 9, 6, hex(0x2a3a3a));
            rect(mid, bx - 2, mt[bx] - 8, 5, 2, hex(0x6cf5c0));
            s.fx("beam", "mid", bx, 0, 2, mt[bx] - 8, 0x4FD1A5, 0);
        }
        s.layer("mid", mid, 11, 0, 0, false);

        // near: neon grid floor fading into the distance
        BufferedImage near = img(800, H);
        int horizon = 292;
        for (int x = 0; x < 800; x++) for (int y = horizon; y < H; y++) {
            double d = (y - horizon) / (double) (H - horizon);
            int c = mix(hex(0x0a1412), hex(0x020404), d);
            int rowStep = (int) (3 + d * 14);
            boolean line = ((y - horizon) % Math.max(2, rowStep) == 0);
            double vx = (x - 400) / (0.25 + d * 1.6);
            if (line || Math.abs(vx % 40) < 1.2) c = mix(c, hex(0x4fd1a5), 0.35 + 0.35 * (1 - d));
            near.setRGB(x, y, c);
        }
        for (int x = 0; x < 800; x++) set(near, x, horizon, hex(0x6cf5c0));
        s.fx("rise", "near", 0, 200, 800, 100, 0x6CF5C0, 26);
        s.fx("scan", "-", 0, 0, 640, 360, 0x4FD1A5, 0);
        s.layer("near", near, 14, 0, 0, false);
    }

    /** The HookVisuals HK logo (italic H + K sharing a stem) built from 4px blocks. */
    static void hkBlocks(BufferedImage img, Scene s, int cx, int cy) {
        String[] rows = {
                "##...##.##...##",
                "##...##.##..##.",
                "##...##.##.##..",
                "#######.####...",
                "##...##.##.##..",
                "##...##.##..##.",
                "##...##.##...##"};
        int block = 6;
        int w = rows[0].length() * block, h = rows.length * block;
        int x0 = cx - w / 2, y0 = cy - h / 2;
        for (int r = 0; r < rows.length; r++) for (int c = 0; c < rows[r].length(); c++) {
            if (rows[r].charAt(c) != '#') continue;
            int bx = x0 + c * block + (rows.length - 1 - r) * block / 3, by = y0 + r * block; // italic like the logo
            for (int i = 0; i < block; i++) for (int j = 0; j < block; j++) {
                int col = (i == 0 || j == 0) ? hex(0xb8ffe6) : (i == block - 1 || j == block - 1) ? hex(0x2a8f75) : hex(0x6cf5c0);
                set(img, bx + i, by + j, col);
            }
        }
        s.fx("glow", "mid", cx, cy, 1, 1, 0x4FD1A5, 9);
        s.fx("sparkle", "mid", x0 - 6, y0 - 6, w + 12, h + 12, 0xE8FFF6, 10);
    }
}
