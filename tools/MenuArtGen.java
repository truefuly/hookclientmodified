import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Generates the pixel-art layers of the menu background: a Minecraft night landscape under a mint
 * aurora (the HookVisuals accent colour) with a big moon, snowy mountains, a castle on a hill,
 * a cliff with a waterfall into a lake, a cherry tree, a nether portal, a campfire and a lantern,
 * seen across a meadow. Run: java tools/MenuArtGen.java assets/hook/textures/gui/menu
 * Positions of everything that is animated in game are printed and mirrored in MenuBackground.java.
 */
public final class MenuArtGen {
    static final int W = 640, H = 360;
    static final Random RNG = new Random(20261111L);

    public static void main(String[] args) throws Exception {
        File out = new File(args.length > 0 ? args[0] : ".");
        out.mkdirs();
        File[] old = out.listFiles();
        if (old != null) for (File f : old) f.delete();
        ImageIO.write(sky(), "png", new File(out, "sky.png"));
        ImageIO.write(aurora(), "png", new File(out, "aurora.png"));
        ImageIO.write(mountains(), "png", new File(out, "far.png"));
        ImageIO.write(mid(), "png", new File(out, "mid.png"));
        ImageIO.write(near(), "png", new File(out, "near.png"));
        ImageIO.write(clouds(), "png", new File(out, "clouds.png"));
        System.out.println("menu art written to " + out.getAbsolutePath());
    }

    // ------------------------------------------------------------ helpers

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

    // ------------------------------------------------------------ sky + moon

    static BufferedImage sky() {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        int[] stops = {hex(0x05070f), hex(0x080c1c), hex(0x0d1430), hex(0x131d42), hex(0x1a2752), hex(0x22335f), hex(0x2a4068), hex(0x2f4f6e), hex(0x3a6170)};
        double horizon = 250.0;
        for (int y = 0; y < H; y++) {
            double t = Math.min(1.0, y / horizon) * (stops.length - 1);
            int band = (int) Math.floor(t);
            double frac = t - band;
            for (int x = 0; x < W; x++) {
                int c = band >= stops.length - 1 ? stops[stops.length - 1] : dither(x, y, frac) ? stops[band + 1] : stops[band];
                img.setRGB(x, y, c);
            }
        }
        // milky way band
        for (int i = 0; i < 1100; i++) {
            double u = RNG.nextDouble();
            int x = (int) (u * W), y = (int) (30 + u * 110 + RNG.nextGaussian() * 16);
            if (y < 0 || y > 200) continue;
            set(img, x, y, mix(get(img, x, y), hex(0x8fa4d0), 0.3 + RNG.nextDouble() * 0.3));
        }
        for (int i = 0; i < 280; i++) {
            int x = RNG.nextInt(W), y = RNG.nextInt(200);
            int c = mix(hex(0xffffff), hex(0xa8c0ff), RNG.nextDouble() * 0.6);
            set(img, x, y, c);
            if (RNG.nextInt(12) == 0) {
                int d = shade(c, -110);
                set(img, x - 1, y, d); set(img, x + 1, y, d); set(img, x, y - 1, d); set(img, x, y + 1, d);
            }
        }
        // big pixel moon with craters and a stepped halo
        int mx = 486, my = 62, mr = 18;
        for (int r = 4; r >= 1; r--) {
            int rr = mr + r * 7;
            for (int x = mx - rr; x <= mx + rr; x++) for (int y = my - rr; y <= my + rr; y++) {
                if (Math.hypot(x - mx, y - my) <= rr && dither(x, y, 0.1 + 0.07 * (4 - r))) set(img, x, y, mix(get(img, x, y), hex(0xcfe8ff), 0.3));
            }
        }
        for (int x = mx - mr; x <= mx + mr; x++) for (int y = my - mr; y <= my + mr; y++) {
            double d = Math.hypot(x - mx, y - my);
            if (d > mr) continue;
            int c = hex(0xf2f3e8);
            if (x - mx + (y - my) * 0.4 > mr * 0.45) c = hex(0xd8dacb);
            if (d > mr - 1.2) c = hex(0xc6c9b9);
            set(img, x, y, c);
        }
        int[][] craters = {{-6, -5, 3}, {5, 3, 4}, {-2, 8, 2}, {8, -7, 2}, {-9, 4, 2}};
        for (int[] cr : craters) for (int x = -cr[2]; x <= cr[2]; x++) for (int y = -cr[2]; y <= cr[2]; y++) {
            if (x * x + y * y <= cr[2] * cr[2]) set(img, mx + cr[0] + x, my + cr[1] + y, hex(0xc2c5b4));
        }
        return img;
    }

    /** Tileable aurora curtains in the HookVisuals mint (scrolled and pulsed in game). */
    static BufferedImage aurora() {
        int w = 1280, h = 180;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
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
                int c = dy > -6 ? hex(0x6cf5c0) : mix(hex(0x4fd1a5), hex(0x4a7cff), Math.min(1, -dy / length * 1.3));
                img.setRGB(x, y, argb((int) (a * 200), c));
            }
        }
        return img;
    }

    // ------------------------------------------------------------ mountains

    static BufferedImage mountains() {
        BufferedImage img = new BufferedImage(720, H, BufferedImage.TYPE_INT_ARGB);
        range(img, 101, 196, 58, hex(0x26304f), hex(0x3d4f78), hex(0xd8e6f2), 4, true);
        range(img, 202, 220, 42, hex(0x1d2541), hex(0x2f3d63), hex(0xb8c8de), 4, true);
        range(img, 303, 240, 24, hex(0x161c33), hex(0x222c4a), 0, 3, false);
        return img;
    }

    static void range(BufferedImage img, long seed, int base, int amp, int body, int light, int snow, int step, boolean caps) {
        int w = img.getWidth();
        for (int x = 0; x < w; x += step) {
            double n = fbm(x / 80.0, seed);
            int top = base - (int) Math.round(n * amp * 1.7) + amp / 2;
            top -= top % step;
            double slope = fbm((x + 4) / 80.0, seed) - n;
            for (int dx = 0; dx < step && x + dx < w; dx++) {
                for (int y = Math.max(0, top); y < H; y++) {
                    int depth = y - top;
                    int c = body;
                    if (slope > 0.004 && depth < 34) c = mix(light, body, depth / 34.0);
                    if (caps && top < base - amp * 0.25 && depth < 5 + (int) (hash(x, seed) * 5)) c = slope > 0.004 ? snow : shade(snow, -45);
                    if (hash2(x + dx, y, seed) > 0.93) c = shade(c, -8);
                    img.setRGB(x + dx, y, c);
                }
            }
        }
    }

    // ------------------------------------------------------------ mid: forest hills and a castle

    static BufferedImage mid() {
        int w = 760;
        BufferedImage img = new BufferedImage(w, H, BufferedImage.TYPE_INT_ARGB);
        int[] top = new int[w];
        for (int x = 0; x < w; x += 3) {
            int t = 262 - (int) Math.round(fbm(x / 60.0, 404) * 24);
            if (x > 300 && x < 400) t -= (int) (16 * Math.sin((x - 300) / 100.0 * Math.PI));
            t -= t % 3;
            for (int dx = 0; dx < 3 && x + dx < w; dx++) top[x + dx] = t;
        }
        int grass = hex(0x1f3a2c), grassTop = hex(0x2f5a3c);
        for (int x = 0; x < w; x++) for (int y = top[x]; y < H; y++) {
            int depth = y - top[x];
            int c = depth < 1 ? grassTop : grass;
            if (hash2(x / 2, y / 2, 9) > 0.78) c = shade(c, -10);
            c = mix(c, hex(0x101628), Math.min(0.6, 0.15 + depth / 80.0));
            img.setRGB(x, y, c);
        }
        for (int x = 4; x < w - 4; x += 5 + RNG.nextInt(6)) {
            if (x > 312 && x < 390) continue;
            int kind = RNG.nextInt(5);
            if (kind < 3) spruce(img, x, top[x], hex(0x173026), 14 + RNG.nextInt(12));
            else if (kind == 3) oak(img, x, top[x], hex(0x1f3c2b), hex(0x33251a));
            else birch(img, x, top[x]);
        }
        castle(img, 324 + 60, top[350]);
        return img;
    }

    static void castle(BufferedImage img, int x, int ground) {
        x -= 60; // keep it on the hill top
        int stone = hex(0x434a5f), dark = hex(0x2f3445), light = hex(0x59627a), roof = hex(0x2c3f4a);
        for (int i = 0; i < 56; i++) for (int j = 0; j < 22; j++) {
            int c = ((j % 4 == 0) || ((i + (j / 4) * 2) % 6 == 0)) ? dark : stone;
            set(img, x + i, ground - 22 + j, c);
        }
        for (int i = 0; i < 56; i += 4) rect(img, x + i, ground - 25, 2, 3, stone);
        tower(img, x - 4, ground, 12, 40, stone, dark, light, roof);
        tower(img, x + 48, ground, 12, 40, stone, dark, light, roof);
        tower(img, x + 20, ground, 16, 54, stone, dark, light, roof);
        rect(img, x + 25, ground - 12, 6, 12, hex(0x14121a));
        int[][] win = {{x + 1, ground - 30}, {x + 52, ground - 30}, {x + 27, ground - 44}, {x + 27, ground - 34}, {x + 12, ground - 14}, {x + 40, ground - 14}};
        for (int[] wv : win) {
            rect(img, wv[0], wv[1], 3, 4, hex(0x14121a));
            rect(img, wv[0], wv[1] + 1, 3, 3, hex(0xffc76a));
            System.out.println("castleWindow: " + wv[0] + "," + (wv[1] + 1));
        }
        // banner in the client mint
        rect(img, x + 28, ground - 74, 1, 12, hex(0x8a8a8a));
        rect(img, x + 29, ground - 74, 7, 4, hex(0x4fd1a5));
        rect(img, x + 29, ground - 70, 5, 1, hex(0x3aa883));
    }

    static void tower(BufferedImage img, int x, int ground, int w, int h, int stone, int dark, int light, int roof) {
        for (int i = 0; i < w; i++) for (int j = 0; j < h; j++) {
            int c = (j % 4 == 0 || (i + (j / 4) * 2) % 5 == 0) ? dark : (i < 2 ? light : stone);
            set(img, x + i, ground - h + j, c);
        }
        for (int k = 0; k < w / 2 + 2; k++) rect(img, x - 1 + k, ground - h - 1 - k, w + 2 - 2 * k, 1, k % 2 == 0 ? roof : shade(roof, 14));
    }

    // ------------------------------------------------------------ trees

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

    static void spruce(BufferedImage img, int x, int ground, int leaf, int h) {
        rect(img, x, ground - 3, 2, 3, hex(0x241910));
        for (int j = 0; j < h; j++) {
            int y = ground - 3 - j;
            int half = Math.max(0, (h - j) / 3 + ((j % 4 == 0) ? 1 : 0));
            for (int i = -half; i <= half + 1; i++) set(img, x + i, y, i > 0 ? shade(leaf, -8) : shade(leaf, 8));
        }
    }

    static void birch(BufferedImage img, int x, int ground) {
        int h = 10 + RNG.nextInt(4);
        for (int j = 0; j < h; j++) set(img, x, ground - 1 - j, j % 3 == 0 ? hex(0x2b2b2b) : hex(0xa9a79e));
        int lx = x - 4, ly = ground - h - 6;
        for (int i = 0; i < 9; i++) for (int j = 0; j < 7; j++) {
            if ((i == 0 || i == 8) && (j == 0 || j == 6)) continue;
            int c = hex(0x34502a);
            if (RNG.nextInt(5) == 0) c = shade(c, 12);
            set(img, lx + i, ly + j, c);
        }
    }

    static void cherry(BufferedImage img, int x, int ground) {
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
        System.out.println("cherry: " + (x - 22) + "," + (ground - 46) + ",46,34");
    }

    static void bigSpruce(BufferedImage img, int x, int ground) {
        int leaf = hex(0x143022);
        rect(img, x, ground - 8, 3, 8, hex(0x241910));
        int h = 66;
        for (int j = 0; j < h; j++) {
            int y = ground - 8 - j;
            int half = (h - j) / 3 + ((j % 6 < 2) ? 2 : 0);
            for (int i = -half; i <= half + 2; i++) {
                int c = i > 1 ? shade(leaf, -10) : shade(leaf, 10);
                if (hash2(i + x, j, 31) > 0.86) c = shade(c, 12);
                set(img, x + i, y, c);
            }
        }
    }

    // ------------------------------------------------------------ foreground, seen across a meadow

    static BufferedImage near() {
        int w = 800;
        BufferedImage img = new BufferedImage(w, H, BufferedImage.TYPE_INT_ARGB);
        int lake0 = 176, lake1 = 440, lakeY = 270, shoreY = 306;
        int[] top = new int[w];
        for (int x = 0; x < w; x += 2) {
            int t;
            if (x < 150) t = 190 + (int) Math.round((fbm(x / 30.0, 31) - 0.5) * 10);    // cliff plateau
            else if (x < lake0) t = 190 + (x - 150) * 3;                                  // cliff edge
            else if (x < lake1) t = lakeY;                                                // water surface
            else t = 268 + (int) Math.round((fbm(x / 45.0, 808) - 0.5) * 8);
            t -= t % 2;
            top[x] = t;
            if (x + 1 < w) top[x + 1] = t;
        }
        int grassTop = hex(0x4b8a3c), grass = hex(0x2f5a2c), meadow = hex(0x25472a), dark = hex(0x0a120d);

        // lake: a band of water between the far bank and the meadow in front
        for (int x = lake0 - 6; x < lake1 + 6; x++) for (int y = lakeY; y < shoreY + 4; y++) {
            double d = (y - lakeY) / (double) (shoreY - lakeY);
            int c = mix(hex(0x2e5d74), hex(0x10223a), d);
            if (Math.abs(x - 380) < 7 - d * 4 && ((y + x) & 1) == 0) c = mix(c, hex(0xe8f2ff), 0.5);   // moon reflection
            if (((x / 6 + y) % 7 == 0) && hash2(x, y, 4) > 0.7) c = shade(c, 16);                   // ripples
            set(img, x, y, c);
        }
        // ground: grass surface, then the meadow running towards the viewer and darkening
        for (int x = 0; x < w; x++) {
            boolean water = x >= lake0 && x < lake1;
            int start = water ? shoreY : top[x];
            for (int y = start; y < H; y++) {
                int depth = y - start;
                int c;
                if (!water && x < 150 + 26 && x >= 150 && y < lakeY + 2) {
                    c = (y / 3 + x / 4) % 3 == 0 ? hex(0x4f5560) : hex(0x3f444f);              // cliff rock face
                    if (hash2(x, y, 71) > 0.8) c = hex(0x335c34);                              // moss
                } else {
                    c = depth < 1 ? grassTop : depth < 3 ? grass : meadow;
                    if (depth >= 3 && hash2(x / 2, y / 2, 3) > 0.72) c = shade(c, -10);
                    if (depth >= 3 && hash2(x, y, 5) > 0.95) c = shade(c, 14);
                }
                double fade = Math.max(0, (y - 290) / 70.0);
                c = mix(c, dark, Math.min(0.85, fade * 0.95));
                img.setRGB(x, y, c);
            }
            if (!water) {
                if (hash(x, 77) > 0.6) set(img, x, top[x] - 1, grassTop);
                if (hash(x, 78) > 0.86) { set(img, x, top[x] - 1, grassTop); set(img, x, top[x] - 2, shade(grassTop, 12)); }
                if (hash(x, 91) > 0.95) {
                    int flower = new int[]{hex(0xe8403a), hex(0xf2d33a), hex(0x8fb7ff), hex(0xf28ac0)}[(int) (hash(x, 5) * 4) & 3];
                    set(img, x, top[x] - 2, hex(0x3f8a2c));
                    set(img, x, top[x] - 3, flower);
                }
            }
            // reeds and grass along the near shore
            if (water && hash(x, 99) > 0.55) {
                int hgt = 2 + (int) (hash(x, 98) * 5);
                for (int j = 0; j < hgt; j++) set(img, x, shoreY - 1 - j, j > hgt - 2 && hash(x, 97) > 0.85 ? hex(0x7a5a32) : hex(0x2d5a2a));
            }
        }
        // foreground grass blades as dark silhouettes along the bottom edge
        for (int x = 0; x < w; x++) {
            int blade = (int) (hash(x, 123) * 18 + Math.sin(x / 9.0) * 4) + 4;
            for (int j = 0; j < blade; j++) set(img, x, H - 1 - j, mix(hex(0x0c1a12), hex(0x050906), j / (double) blade));
        }

        // waterfall from the cliff into the lake (animated streaks drawn in game)
        int wfx = 150, wfw = 12, wfTop = top[150] - 1;
        for (int x = wfx; x < wfx + wfw; x++) for (int y = wfTop; y < lakeY + 3; y++) {
            set(img, x, y, mix(hex(0x4f7fc8), hex(0xbcd6ff), ((x * 3 + y) % 5 == 0) ? 0.6 : 0.15));
        }
        System.out.println("waterfall: x=" + wfx + " w=" + wfw + " top=" + wfTop + " bottom=" + lakeY);
        System.out.println("lake: " + lake0 + "-" + lake1 + " y=" + lakeY + " shore=" + shoreY);

        cherry(img, 488, top[488]);
        portal(img, 560, top[572]);
        campfire(img, 650, top[650]);
        lantern(img, 700, top[700]);
        bigSpruce(img, 742, top[742]);
        bigSpruce(img, 112, top[112]);
        oak(img, 96, top[96], hex(0x2a5a28), hex(0x45301f));
        return img;
    }

    static void portal(BufferedImage img, int x, int ground) {
        int obs = hex(0x150d22), obsLight = hex(0x2a1c40);
        int w = 6 * 4, h = 8 * 4;
        int y = ground - h;
        for (int i = 0; i < w; i++) for (int j = 0; j < h; j++) {
            boolean frame = i < 4 || i >= w - 4 || j < 4 || j >= h - 4;
            set(img, x + i, y + j, frame ? ((i % 4 == 0 || j % 4 == 0) ? obsLight : obs) : hex(0x6a24c9));
        }
        System.out.println("portal: " + (x + 4) + "," + (y + 4) + "," + (w - 8) + "," + (h - 8));
    }

    static void campfire(BufferedImage img, int x, int ground) {
        rect(img, x - 6, ground - 3, 13, 2, hex(0x5a3a20));
        rect(img, x - 4, ground - 4, 9, 1, hex(0x6e4a28));
        rect(img, x - 2, ground - 2, 5, 2, hex(0x2a1a10));
        System.out.println("campfire: " + x + "," + (ground - 4));
    }

    static void lantern(BufferedImage img, int x, int ground) {
        rect(img, x, ground - 22, 2, 22, hex(0x45301f));
        rect(img, x - 4, ground - 22, 10, 2, hex(0x45301f));
        rect(img, x - 5, ground - 19, 3, 1, hex(0x3a3a40));
        rect(img, x - 6, ground - 18, 5, 5, hex(0x3a3a40));
        rect(img, x - 5, ground - 17, 3, 3, hex(0xffd27a));
        System.out.println("lantern: " + (x - 4) + "," + (ground - 16));
    }

    // ------------------------------------------------------------ night clouds (tileable)

    static BufferedImage clouds() {
        int w = 1280, h = 110;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int body = 0xA0303a5c, top = 0xB0647898, shadow = 0x8c1c2440;
        for (int i = 0; i < 14; i++) {
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
}
