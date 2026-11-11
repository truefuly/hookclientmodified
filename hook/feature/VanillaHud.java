package hook.feature;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_332;

/**
 * Lets the HUD editor move, resize and hide vanilla HUD parts (boss bar, scoreboard sidebar).
 * Rendering of a part is wrapped in a matrix transform; its real size is measured every frame
 * from the fills and sprites Minecraft draws for it, so the editor frame always matches.
 */
public final class VanillaHud {
    public enum Part {
        BOSSBAR("Bossbar"), SCOREBOARD("Scoreboard");

        public final String label;

        Part(String label) {
            this.label = label;
        }
    }

    static final class State {
        boolean visible = true;
        float scale = 1.0f;
        float dx, dy;                       // offset in GUI units
        float minX, minY, maxX, maxY;       // last measured bounds (untransformed GUI units)
        boolean measured;
        long measuredAt;
    }

    private static final Map<Part, State> STATES = new EnumMap<>(Part.class);
    private static Part capturing;
    private static float cMinX, cMinY, cMaxX, cMaxY;
    private static boolean cAny;

    static {
        for (Part part : Part.values()) STATES.put(part, new State());
    }

    private VanillaHud() {
    }

    static State state(Part part) {
        return STATES.get(part);
    }

    /** @return false if the part is hidden and vanilla must not draw it. */
    public static boolean begin(Part part, class_332 ctx) {
        State s = STATES.get(part);
        if (!s.visible) {
            return false;
        }
        ctx.method_51448().pushMatrix();
        float px = s.measured ? s.minX : 0.0f, py = s.measured ? s.minY : 0.0f;
        ctx.method_51448().translate(s.dx + px, s.dy + py);
        ctx.method_51448().scale(s.scale, s.scale);
        ctx.method_51448().translate(-px, -py);
        capturing = part;
        cAny = false;
        return true;
    }

    public static void end(Part part, class_332 ctx) {
        if (capturing != part) return;
        capturing = null;
        ctx.method_51448().popMatrix();
        State s = STATES.get(part);
        if (cAny) {
            if (part == Part.BOSSBAR) cMinY -= 10; // the boss name is drawn as text above the bar
            s.minX = cMinX; s.minY = cMinY; s.maxX = cMaxX; s.maxY = cMaxY;
            s.measured = true;
            s.measuredAt = System.currentTimeMillis();
        }
    }

    /** Called for every fill / GUI sprite drawn while a part renders (GUI coordinates before our transform). */
    public static void capture(int x1, int y1, int x2, int y2) {
        if (capturing == null) return;
        float a = Math.min(x1, x2), b = Math.min(y1, y2), c = Math.max(x1, x2), d = Math.max(y1, y2);
        if (!cAny) {
            cMinX = a; cMinY = b; cMaxX = c; cMaxY = d;
            cAny = true;
        } else {
            cMinX = Math.min(cMinX, a); cMinY = Math.min(cMinY, b); cMaxX = Math.max(cMaxX, c); cMaxY = Math.max(cMaxY, d);
        }
    }

    /** On-screen rectangle (GUI units) of a part, or null if it was never drawn / not drawn recently. */
    static float[] rect(Part part) {
        State s = STATES.get(part);
        if (!s.measured || (s.visible && System.currentTimeMillis() - s.measuredAt > 1500)) return null;
        return new float[]{s.minX + s.dx, s.minY + s.dy, (s.maxX - s.minX) * s.scale, (s.maxY - s.minY) * s.scale};
    }

    static void move(Part part, float guiX, float guiY) {
        State s = STATES.get(part);
        s.dx = guiX - s.minX;
        s.dy = guiY - s.minY;
    }

    static void resize(Part part, float step) {
        State s = STATES.get(part);
        s.scale = Math.max(0.4f, Math.min(3.0f, Math.round((s.scale + step) * 20.0f) / 20.0f));
    }

    static void toggle(Part part) {
        State s = STATES.get(part);
        s.visible = !s.visible;
    }

    static void reset() {
        for (State s : STATES.values()) {
            s.visible = true;
            s.scale = 1.0f;
            s.dx = 0.0f;
            s.dy = 0.0f;
        }
    }

    static String serialize() {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<Part, State> e : STATES.entrySet()) {
            State s = e.getValue();
            out.append("VANILLA,").append(e.getKey().name()).append(',').append(s.visible).append(',')
                    .append(String.format(Locale.ROOT, "%.2f,%.1f,%.1f", s.scale, s.dx, s.dy)).append('\n');
        }
        return out.toString();
    }

    static boolean parse(String[] p) {
        if (p.length != 6 || !p[0].equals("VANILLA")) return false;
        try {
            State s = STATES.get(Part.valueOf(p[1]));
            s.visible = Boolean.parseBoolean(p[2]);
            s.scale = Float.parseFloat(p[3]);
            s.dx = Float.parseFloat(p[4]);
            s.dy = Float.parseFloat(p[5]);
        } catch (IllegalArgumentException ignored) {
        }
        return true;
    }
}
