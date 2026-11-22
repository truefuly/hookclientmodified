package hook.feature;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

/**
 * Lets the HUD editor move, resize and hide every vanilla HUD part (hotbar, health/food, XP bar, item name,
 * action bar, title, effects, boss bar, scoreboard). Rendering of a part is wrapped in a matrix transform; its real
 * size is measured every frame from the fills, sprites and text Minecraft draws for it (through the current matrix,
 * so scaled text like titles is measured right), so the editor frame always matches. Parts that are not on screen
 * right now (no title, no action bar message...) get a preview frame in the editor so they can still be placed.
 */
public final class VanillaHud {
    public enum Part {
        HOTBAR("Hotbar"), STATUS("Health & Food"), XP("XP Bar"), HELD_ITEM("Item Name"), ACTIONBAR("Action Bar"),
        TITLE("Title"), EFFECTS("Effects"), BOSSBAR("Bossbar"), SCOREBOARD("Scoreboard");

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
        boolean preview;                    // bounds are a placeholder (part not drawn right now)
        long measuredAt;
        // accumulation over one frame (the XP bar is drawn in several calls)
        float aMinX, aMinY, aMaxX, aMaxY;
        boolean aAny;
        int aFrame = -1;
    }

    private static final Map<Part, State> STATES = new EnumMap<>(Part.class);
    private static Part capturing;
    private static class_332 capCtx;
    private static final Matrix3x2f INV = new Matrix3x2f();
    private static final Matrix3x2f TMP = new Matrix3x2f();
    private static final Vector2f P = new Vector2f();
    private static int frame;

    static {
        for (Part part : Part.values()) STATES.put(part, new State());
    }

    private VanillaHud() {
    }

    static State state(Part part) {
        return STATES.get(part);
    }

    /** Called once per HUD frame. */
    public static void newFrame() {
        frame++;
    }

    /** @return false if the part is hidden and vanilla must not draw it. */
    public static boolean begin(Part part, class_332 ctx) {
        State s = STATES.get(part);
        if (!s.visible) return false;
        if (capturing != null) return true; // never nest
        ctx.method_51448().pushMatrix();
        float px = s.measured ? s.minX : 0.0f, py = s.measured ? s.minY : 0.0f;
        ctx.method_51448().translate(s.dx + px, s.dy + py);
        ctx.method_51448().scale(s.scale, s.scale);
        ctx.method_51448().translate(-px, -py);
        INV.set(ctx.method_51448()).invert();
        capturing = part;
        capCtx = ctx;
        if (s.aFrame != frame) {
            s.aFrame = frame;
            s.aAny = false;
        }
        return true;
    }

    public static void end(Part part, class_332 ctx) {
        if (capturing != part) return;
        capturing = null;
        capCtx = null;
        ctx.method_51448().popMatrix();
        State s = STATES.get(part);
        if (s.aAny) {
            s.minX = s.aMinX;
            s.minY = part == Part.BOSSBAR ? s.aMinY - 10 : s.aMinY; // the boss name is drawn above the bar
            s.maxX = s.aMaxX;
            s.maxY = s.aMaxY;
            s.measured = true;
            s.preview = false;
            s.measuredAt = System.currentTimeMillis();
        }
    }

    /** Called for every fill / GUI sprite / text drawn (coordinates in the current matrix). */
    public static void capture(class_332 ctx, float x1, float y1, float x2, float y2) {
        if (capturing == null || ctx != capCtx) return;
        TMP.set(INV).mul(ctx.method_51448());
        float a = Float.MAX_VALUE, b = Float.MAX_VALUE, c = -Float.MAX_VALUE, d = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            TMP.transformPosition((i & 1) == 0 ? x1 : x2, (i & 2) == 0 ? y1 : y2, P);
            a = Math.min(a, P.x); b = Math.min(b, P.y); c = Math.max(c, P.x); d = Math.max(d, P.y);
        }
        State s = STATES.get(capturing);
        if (!s.aAny) {
            s.aMinX = a; s.aMinY = b; s.aMaxX = c; s.aMaxY = d;
            s.aAny = true;
        } else {
            s.aMinX = Math.min(s.aMinX, a); s.aMinY = Math.min(s.aMinY, b);
            s.aMaxX = Math.max(s.aMaxX, c); s.aMaxY = Math.max(s.aMaxY, d);
        }
    }

    static boolean editorOpen() {
        try {
            return HudLayoutEditor.editing && class_310.method_1551().field_1755 instanceof class_408;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Where a part sits by default (GUI units) - used as preview while it is not drawn. */
    private static float[] placeholder(Part part) {
        class_310 mc = class_310.method_1551();
        float w = mc.method_22683().method_4486(), h = mc.method_22683().method_4502();
        return switch (part) {
            case HOTBAR -> new float[]{w / 2 - 91, h - 22, 182, 22};
            case STATUS -> new float[]{w / 2 - 91, h - 49, 182, 19};
            case XP -> new float[]{w / 2 - 91, h - 30, 182, 8};
            case HELD_ITEM -> new float[]{w / 2 - 45, h - 61, 90, 13};
            case ACTIONBAR -> new float[]{w / 2 - 70, h - 74, 140, 13};
            case TITLE -> new float[]{w / 2 - 110, h / 2 - 42, 220, 52};
            case EFFECTS -> new float[]{w - 77, 1, 76, 26};
            case BOSSBAR -> new float[]{w / 2 - 91, 2, 182, 17};
            case SCOREBOARD -> new float[]{w - 82, h / 2 - 45, 81, 90};
        };
    }

    /** On-screen rectangle (GUI units) of a part, or null if it is not drawn (outside the editor). */
    static float[] rect(Part part) {
        State s = STATES.get(part);
        boolean fresh = s.measured && !s.preview && (!s.visible || System.currentTimeMillis() - s.measuredAt <= 1500);
        if (!fresh) {
            if (!editorOpen()) return null;
            float[] p = placeholder(part);
            s.minX = p[0]; s.minY = p[1]; s.maxX = p[0] + p[2]; s.maxY = p[1] + p[3];
            s.measured = true;
            s.preview = true;
        }
        return new float[]{s.minX + s.dx, s.minY + s.dy, (s.maxX - s.minX) * s.scale, (s.maxY - s.minY) * s.scale};
    }

    static boolean preview(Part part) {
        return STATES.get(part).preview;
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

    static void setScale(Part part, float scale) {
        STATES.get(part).scale = Math.max(0.4f, Math.min(3.0f, Math.round(scale * 100.0f) / 100.0f));
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
