package hook.utils;

import net.minecraft.class_243;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Draws touching blocks as one shape: only the faces that have no neighbour in the set, and only the silhouette
 * edges (outer corners and inner corners), so a vein, a tunnel or a double chest reads as one clean object instead of
 * a pile of boxes with lines inside.
 */
public final class MergedShape {
    public enum Style {
        /** Shaded faces plus the outline. */
        SHAPE,
        /** Only the outline, glowing. */
        EDGES,
        /** Soft shaded faces that fade upward, no lines. */
        SOFT
    }

    private final Set<Long> cells = new HashSet<>();

    public static long pack(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) z & 0x3FFFFFFL) << 12 | (long) y & 0xFFFL;
    }

    private static int ux(long k) { return (int) (k >> 38); }
    private static int uy(long k) { return (int) (k << 52 >> 52); }
    private static int uz(long k) { return (int) (k << 26 >> 38); }

    public void add(int x, int y, int z) {
        this.cells.add(pack(x, y, z));
    }

    public boolean isEmpty() {
        return this.cells.isEmpty();
    }

    public int size() {
        return this.cells.size();
    }

    private boolean has(int x, int y, int z) {
        return this.cells.contains(pack(x, y, z));
    }

    private boolean has(int[] c) {
        return this.has(c[0], c[1], c[2]);
    }

    /**
     * Builds the shape.
     * @param fill    face strength 0-255 (SHAPE: top face, sides a bit less; SOFT: bottom of the sides)
     * @param line    outline strength 0-255
     * @param width   outline width in pixels
     * @param glow    0-1, adds a soft wide line under the outline
     * @param inset   how far the shape sits inside the blocks (0 = exactly on the block faces)
     */
    public void build(Style style, Color color, double fill, double line, float width, double glow, double inset,
                      List<RenderUtils.ColoredQuad> quads, List<RenderUtils.ColoredLine> lines) {
        int rgb = color.getRGB() & 0xFFFFFF;
        Color edge = alpha(color, line);
        Color halo = alpha(color, line * glow * 0.32);
        Color haloOuter = alpha(color, line * glow * 0.14);
        int[] c = new int[3], n = new int[3];
        for (long k : this.cells) {
            c[0] = ux(k); c[1] = uy(k); c[2] = uz(k);
            for (int a = 0; a < 3; a++) {
                int b = (a + 1) % 3, d = (a + 2) % 3;
                // faces
                if (style != Style.EDGES) {
                    for (int s = -1; s <= 1; s += 2) {
                        System.arraycopy(c, 0, n, 0, 3);
                        n[a] += s;
                        if (this.has(n)) continue;
                        double plane = s > 0 ? c[a] + 1 - inset : c[a] + inset;
                        double b0 = this.lo(c, b, inset), b1 = this.hi(c, b, inset), d0 = this.lo(c, d, inset), d1 = this.hi(c, d, inset);
                        double[] p = new double[12];
                        put(p, 0, a, b, d, plane, b0, d0);
                        put(p, 1, a, b, d, plane, b1, d0);
                        put(p, 2, a, b, d, plane, b1, d1);
                        put(p, 3, a, b, d, plane, b0, d1);
                        int[] col = this.faceColors(style, rgb, fill, a, s, p);
                        quads.add(new RenderUtils.ColoredQuad(p, col[0], col[1], col[2], col[3]));
                    }
                }
                // silhouette edges along axis a
                if (style == Style.SOFT || line <= 0.0) continue;
                for (int sb = -1; sb <= 1; sb += 2) for (int sd = -1; sd <= 1; sd += 2) {
                    boolean n1 = this.has(off(c, b, sb, d, 0)), n2 = this.has(off(c, b, 0, d, sd)), n12 = this.has(off(c, b, sb, d, sd));
                    if (!((!n1 && !n2) || (n1 && n2 && !n12))) continue;
                    double bv = sb > 0 ? c[b] + 1 - inset : c[b] + inset, dv = sd > 0 ? c[d] + 1 - inset : c[d] + inset;
                    double[] p0 = new double[3], p1 = new double[3];
                    p0[a] = this.lo(c, a, inset); p1[a] = this.hi(c, a, inset);
                    p0[b] = p1[b] = bv; p0[d] = p1[d] = dv;
                    class_243 v0 = new class_243(p0[0], p0[1], p0[2]), v1 = new class_243(p1[0], p1[1], p1[2]);
                    if (glow > 0.0) {
                        lines.add(new RenderUtils.ColoredLine(v0, v1, haloOuter, width + 6.0f));
                        lines.add(new RenderUtils.ColoredLine(v0, v1, halo, width + 2.5f));
                    }
                    lines.add(new RenderUtils.ColoredLine(v0, v1, edge, width));
                }
            }
        }
    }

    private int[] faceColors(Style style, int rgb, double fill, int axis, int side, double[] p) {
        if (style == Style.SOFT && axis != 1) {
            // sides fade from strong at the bottom to faint at the top of the block
            double y0 = Math.min(Math.min(p[1], p[4]), Math.min(p[7], p[10]));
            double strong = fill * (axis == 0 ? 2.0 : 1.7), faint = fill * 0.7;
            int[] out = new int[4];
            for (int i = 0; i < 4; i++) out[i] = argb(p[i * 3 + 1] <= y0 + 1.0E-4 ? strong : faint, rgb);
            return out;
        }
        double f;
        if (axis == 1) f = side > 0 ? (style == Style.SOFT ? 2.0 : 1.35) : 0.55;
        else f = axis == 0 ? 1.0 : 0.8;
        int c = argb(fill * f, rgb);
        return new int[]{c, c, c, c};
    }

    private double lo(int[] c, int axis, double inset) {
        int[] n = off(c, axis, -1);
        return c[axis] + (this.has(n) ? 0.0 : inset);
    }

    private double hi(int[] c, int axis, double inset) {
        int[] n = off(c, axis, 1);
        return c[axis] + 1 - (this.has(n) ? 0.0 : inset);
    }

    private static int[] off(int[] c, int axis, int s) {
        int[] n = c.clone();
        n[axis] += s;
        return n;
    }

    private static int[] off(int[] c, int a1, int s1, int a2, int s2) {
        int[] n = c.clone();
        n[a1] += s1;
        n[a2] += s2;
        return n;
    }

    private static void put(double[] p, int i, int a, int b, int d, double av, double bv, double dv) {
        p[i * 3 + a] = av;
        p[i * 3 + b] = bv;
        p[i * 3 + d] = dv;
    }

    private static int argb(double alpha, int rgb) {
        int a = (int) Math.max(0, Math.min(255, Math.round(alpha)));
        return a << 24 | rgb;
    }

    private static Color alpha(Color c, double a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) Math.max(0, Math.min(255, Math.round(a))));
    }
}
