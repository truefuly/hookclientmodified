package hook.feature;

import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

/**
 * The quick utility wheel (emote-wheel style radial menu, drawn like the HookVisuals click GUI: glass panels, header tile, sidebar-style rows).
 * Hold the key: the wheel grows out from behind, the mouse direction from the centre picks a slot, letting go opens
 * it. It fades back quickly on close.
 */
public final class QuickWheelScreen extends class_437 {
    /** One slot on the wheel. action == null = empty slot. */
    record Slot(String name, String hint, UiIcon icon, Runnable action) {}

    static final Slot[] SLOTS = {
        new Slot("Calculator", "Quick maths", UiIcon.CALCULATOR, () -> class_310.method_1551().method_1507((class_437) new CalculatorScreen())),
        new Slot("Empty", "More soon", UiIcon.PLUS, null),
        new Slot("Empty", "More soon", UiIcon.PLUS, null),
        new Slot("Empty", "More soon", UiIcon.PLUS, null),
        new Slot("AuroraAFK", "AFK accounts", UiIcon.HEAD, () -> class_310.method_1551().method_1507((class_437) new AuroraScreen())),
        new Slot("Empty", "More soon", UiIcon.PLUS, null),
        new Slot("Empty", "More soon", UiIcon.PLUS, null),
        new Slot("Empty", "More soon", UiIcon.PLUS, null),
    };

    private static final float OPEN_MS = 170f, CLOSE_MS = 110f;
    private final long openedAt = System.nanoTime();
    private long closingAt = -1L;
    private Runnable after;
    private int hovered = -1;
    private final float[] hoverAnim = new float[SLOTS.length];
    private float pointer = Float.NaN;
    /** Test only: pretend the mouse points at this slot. */
    static int testHover = -1;
    private long last;
    private float dt = 0.016f;

    public QuickWheelScreen() {
        super((class_2561) class_2561.method_43473());
    }

    @Override
    protected void method_25426() {
        cue(UiAudio.Cue.GUI_OPEN);
    }

    private static void cue(UiAudio.Cue c) {
        if (QuickUtilitiesModule.instance != null) QuickUtilitiesModule.instance.cue(c);
    }

    private float scale() {
        return Math.max(1.0f, class_310.method_1551().method_22683().method_4495());
    }

    /** 0..1 how open the wheel is (eased), 0 once fully closed. */
    private float visibility() {
        long now = System.nanoTime();
        if (this.closingAt >= 0) {
            float t = (now - this.closingAt) / 1.0e6f / CLOSE_MS;
            return 1.0f - UiEasing.clamp(t, 0, 1) * UiEasing.clamp(t, 0, 1); // fades back fast
        }
        float t = UiEasing.clamp((now - this.openedAt) / 1.0e6f / OPEN_MS, 0, 1);
        return UiEasing.out(t);
    }

    private void close(Runnable then) {
        if (this.closingAt >= 0) return;
        this.closingAt = System.nanoTime();
        this.after = then;
        cue(then != null ? UiAudio.Cue.CONFIRM : UiAudio.Cue.GUI_CLOSE);
    }

    private void select() {
        Slot s = this.hovered >= 0 ? SLOTS[this.hovered] : null;
        this.close(s != null ? s.action() : null);
    }

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        this.dt = this.last == 0L ? 0.016f : Math.min(0.1f, (now - this.last) / 1.0e9f);
        this.last = now;

        QuickUtilitiesModule m = QuickUtilitiesModule.instance;
        // emote-wheel behaviour: letting go of the key picks what you point at
        if (this.closingAt < 0 && !QuickUtilitiesModule.keyDown() && (m == null || m.releaseToSelect.getValue())) this.select();

        float vis = this.visibility();
        if (this.closingAt >= 0 && vis <= 0.001f) {
            Runnable r = this.after;
            this.after = null;
            class_310.method_1551().method_1507(null);
            if (r != null) r.run();
            return;
        }

        float s = this.scale();
        int sw = class_310.method_1551().method_22683().method_4489();
        int sh = class_310.method_1551().method_22683().method_4506();
        float u = UiTheme.unit(sw, sh);
        double mx = mouseX * s, my = mouseY * s;
        float cx = sw / 2.0f, cy = sh / 2.0f;
        int n = SLOTS.length;

        // which slot the mouse points at (direction from the centre, small dead zone in the middle)
        double dx = mx - cx, dy = my - cy;
        int hov = -1;
        if (Math.hypot(dx, dy) > 40 * u) {
            double ang = Math.toDegrees(Math.atan2(dx, -dy)); // 0 = up, clockwise
            ang = (ang + 360.0 + 180.0 / n) % 360.0;
            hov = (int) (ang / (360.0 / n));
            float target = (float) Math.toDegrees(Math.atan2(dx, -dy));
            if (Float.isNaN(this.pointer)) this.pointer = target;
            float diff = ((target - this.pointer + 540f) % 360f) - 180f;
            this.pointer += diff * (1.0f - (float) Math.exp(-22f * this.dt));
        }
        if (testHover >= 0) { hov = testHover; this.pointer = testHover * 360f / n; }
        if (hov != this.hovered && this.closingAt < 0) {
            if (hov >= 0) cue(UiAudio.Cue.TICK);
            this.hovered = hov;
        }

        if (m == null || m.blur.getValue()) {
            try { ctx.method_71278(); } catch (Throwable ignored) {}
        }

        ctx.method_51448().pushMatrix();
        ctx.method_51448().scale(1.0f / s, 1.0f / s);
        try {
            MinecraftUiRenderer ui = new MinecraftUiRenderer(ctx, s);
            UiTheme.backdrop(ui, sw, sh, vis);
            // grows out from behind: small and faded -> full size (same feel as the client menu opening)
            float scl = 0.82f + 0.18f * vis;
            ui.pushAlpha(vis);
            ui.pushTransform(cx * (1 - scl), cy * (1 - scl), scl);

            // Classic HookVisuals look: a header panel in the middle and the utilities as sidebar-style tiles around it,
            // each one its own glass panel like the click GUI (selected = accent stripe, lighter tile, accent icon).
            float rx = 300 * u, ry = 205 * u, tw = 172 * u, th = 54 * u;
            float pw = 284 * u, ph = 136 * u;

            // connector dots from the centre panel toward the hovered tile
            if (this.hovered >= 0 && !Float.isNaN(this.pointer)) {
                double a = Math.toRadians(this.pointer);
                float h = this.hoverAnim[this.hovered];
                for (int k = 0; k < 7; k++) {
                    float f = 0.30f + k * 0.075f;
                    float px = cx + (float) (Math.sin(a) * rx * f), py = cy - (float) (Math.cos(a) * ry * f);
                    if (Math.abs(px - cx) < pw / 2 + 6 * u && Math.abs(py - cy) < ph / 2 + 6 * u) continue;
                    ui.circle(px, py, (1.4f + 0.25f * k) * u, UiPalette.accent((0.18f + 0.07f * k) * h));
                }
            }

            for (int i = 0; i < n; i++) {
                double a = Math.toRadians(i * 360.0 / n);
                float sx = cx + (float) (Math.sin(a) * rx), sy = cy - (float) (Math.cos(a) * ry);
                boolean hot = i == this.hovered;
                this.hoverAnim[i] += ((hot ? 1f : 0f) - this.hoverAnim[i]) * (1.0f - (float) Math.exp(-13f * this.dt));
                float h = this.hoverAnim[i];
                Slot slot = SLOTS[i];
                boolean empty = slot.action() == null;
                float grow = 1.0f + 0.04f * h;
                float w = tw * grow, hh = th * grow, x = sx - w / 2, y = sy - hh / 2;
                ui.pushAlpha(empty ? 0.62f + 0.2f * h : 1f);
                UiTheme.glass(ui, x, y, w, hh, 16 * u, u, i * 1.7f);
                if (h > 0.01f) {
                    ui.round(x + 6 * u, y + 6 * u, w - 12 * u, hh - 12 * u, 11 * u, UiEasing.mix(UiRenderer.white(0.05f * h), UiPalette.accent(0.10f * h), empty ? 0f : 0.6f));
                    ui.round(x + 6 * u, y + 15 * u, 3 * u, hh - 30 * u, 1.5f * u, empty ? UiRenderer.white(0.3f * h) : UiPalette.accent(0.95f * h));
                    if (!empty) ui.glow(sx, sy, w * 0.6f, UiPalette.accent(0.07f * h));
                }
                float box = 34 * u, bx = x + 16 * u, by = y + (hh - box) / 2;
                ui.round(bx, by, box, box, 11 * u, UiEasing.mix(UiRenderer.white(0.07f), UiPalette.accent(0.16f), empty ? 0f : h));
                ui.outline(bx, by, box, box, 11 * u, Math.max(1.0f, u * 0.9f), UiRenderer.white(0.09f));
                float is = 17 * u;
                int iconCol = empty ? UiRenderer.white(0.4f + 0.2f * h) : UiEasing.mix(UiTheme.TEXT, UiPalette.accent(1.0f), h);
                ui.icon(slot.icon(), bx + (box - is) / 2, by + (box - is) / 2, is, iconCol);
                float lx = bx + box + 12 * u, lw = x + w - 14 * u - lx;
                ui.text(ui.ellipsize(empty ? "Empty slot" : slot.name(), lw, 13.5f * u, 1), lx, y + hh / 2 - 15 * u, 13.5f * u, 1, empty ? UiTheme.SUB : UiTheme.TEXT);
                ui.text(ui.ellipsize(slot.hint(), lw, 11 * u, 0), lx, y + hh / 2 + 3 * u, 11 * u, 0, empty ? UiTheme.FAINT : UiTheme.SUB);
                ui.popAlpha();
            }

            // centre: a panel with the click GUI header (icon tile, title, subtitle) for what you point at
            float px = cx - pw / 2, py = cy - ph / 2;
            UiTheme.glass(ui, px, py, pw, ph, 20 * u, u, 4.2f);
            Slot cur = this.hovered >= 0 ? SLOTS[this.hovered] : null;
            boolean usable = cur != null && cur.action() != null;
            float tile = 44 * u, tx = px + 20 * u, ty = py + 20 * u;
            float hv = this.hovered >= 0 ? this.hoverAnim[this.hovered] : 0f;
            ui.round(tx, ty, tile, tile, 14 * u, usable ? UiEasing.mix(UiRenderer.white(0.07f), UiPalette.accent(0.18f), hv) : UiRenderer.white(0.07f));
            ui.outline(tx, ty, tile, tile, 14 * u, Math.max(1.0f, u * 0.9f), UiRenderer.white(0.09f));
            float his = 21 * u;
            ui.icon(cur == null ? UiIcon.BOLT : cur.icon(), tx + (tile - his) / 2, ty + (tile - his) / 2, his, usable ? UiEasing.mix(UiTheme.TEXT, UiPalette.accent(1f), hv) : UiTheme.TEXT);
            String title = cur == null ? "Quick Use" : usable ? cur.name() : "Empty slot";
            String hint = cur == null ? "HookVisuals" : usable ? cur.hint() : "Nothing here yet";
            ui.text(ui.ellipsize(title, pw - tile - 56 * u, 20 * u, 1), tx + tile + 15 * u, ty + u, 20 * u, 1, UiTheme.TEXT);
            ui.text(ui.ellipsize(hint, pw - tile - 56 * u, 12 * u, 0), tx + tile + 16 * u, ty + 26 * u, 12 * u, 0, UiTheme.SUB);
            ui.rect(px + 18 * u, py + 82 * u, pw - 36 * u, Math.max(1.0f, u), UiRenderer.white(0.055f));
            boolean release = m == null || m.releaseToSelect.getValue();
            String foot = usable ? (release ? "Release to open" : "Click to open") : "Point at a utility";
            UiTheme.accentDot(ui, px + 26 * u, py + 108 * u, u, usable ? 1f : 0.35f);
            ui.text(foot, px + 38 * u, py + 101 * u, 12 * u, 1, usable ? UiTheme.TEXT : UiTheme.SUB);
            ui.textRight("Esc", px + pw - 22 * u, py + 101 * u, 12 * u, 0, UiTheme.FAINT);

            ui.popTransform();
            ui.popAlpha();
        } finally {
            ctx.method_51448().popMatrix();
        }
    }

    @Override
    public void method_25420(class_332 ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        if (this.closingAt < 0) this.select();
        return true;
    }

    @Override
    public boolean method_25404(class_11908 key) {
        if (key.comp_4795() == 256) { this.close(null); return true; } // Esc
        return true;
    }

    @Override
    public boolean method_25400(class_11905 input) {
        return true;
    }

    @Override
    public void method_25419() {
        this.close(null);
    }

    @Override
    public boolean method_25421() {
        return false;
    }
}
