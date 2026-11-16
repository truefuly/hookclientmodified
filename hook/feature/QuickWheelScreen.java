package hook.feature;

import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

/**
 * The quick utility wheel (emote-wheel style radial menu, drawn in the client's own glass style and theme colour).
 * Hold the key: the wheel grows out from behind, the mouse direction from the centre picks a slot, letting go opens
 * it. It fades back quickly on close.
 */
public final class QuickWheelScreen extends class_437 {
    /** One slot on the wheel. action == null = empty slot. */
    record Slot(String name, String hint, UiIcon icon, Runnable action) {}

    static final Slot[] SLOTS = {
        new Slot("Calculator", "Quick maths", UiIcon.CALCULATOR, () -> class_310.method_1551().method_1507((class_437) new CalculatorScreen())),
        new Slot("AuroraAFK", "AFK accounts", UiIcon.HEAD, () -> class_310.method_1551().method_1507((class_437) new AuroraScreen())),
        new Slot("Empty", "More soon", UiIcon.PLUS, null),
        new Slot("Empty", "More soon", UiIcon.PLUS, null),
        new Slot("Empty", "More soon", UiIcon.PLUS, null),
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
        float outer = 250 * u, inner = 96 * u;
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

            // wheel body: the menu's glass, as a disc
            UiTheme.glassDisc(ui, cx, cy, outer, u);

            float slotDist = (outer + inner) / 2.0f + 6 * u;
            float slotR = 50 * u;
            for (int i = 0; i < n; i++) {
                double a = Math.toRadians(i * 360.0 / n);
                float sx = cx + (float) (Math.sin(a) * slotDist), sy = cy - (float) (Math.cos(a) * slotDist);
                boolean hot = i == this.hovered;
                this.hoverAnim[i] += ((hot ? 1f : 0f) - this.hoverAnim[i]) * (1.0f - (float) Math.exp(-13f * this.dt));
                float h = this.hoverAnim[i];
                Slot slot = SLOTS[i];
                boolean empty = slot.action() == null;
                float r = slotR * (1.0f + 0.06f * h);
                // each slot is a round module card; the hovered one lights up like an enabled module
                UiTheme.card(ui, sx - r, sy - r, r * 2, r * 2, r, h, empty ? 0f : h, u);
                if (!empty && h > 0.01f) ui.glow(sx, sy, r + 16 * u, UiPalette.accent(0.16f * h));
                float is = 24 * u * (1.0f + 0.06f * h);
                int iconCol = empty ? UiRenderer.white(0.18f + 0.14f * h) : UiEasing.mix(UiRenderer.white(0.86f), UiPalette.accent(1.0f), h);
                ui.icon(slot.icon(), sx - is / 2, sy - is / 2, is, iconCol);
            }

            // thin accent arc along the rim toward the hovered slot
            if (this.hovered >= 0 && !Float.isNaN(this.pointer)) {
                float span = 360f / n;
                float rimR = outer - 6 * u;
                for (float t = -span / 2 + 2; t <= span / 2 - 2; t += 1.0f) {
                    double a = Math.toRadians(this.pointer + t);
                    float fade = 1.0f - Math.abs(t) / (span / 2);
                    ui.circle(cx + (float) (Math.sin(a) * rimR), cy - (float) (Math.cos(a) * rimR), 1.6f * u + u * fade, UiPalette.accent(0.25f + 0.65f * fade * fade));
                }
            }

            // centre: an inner card with the name of what you point at
            ui.round(cx - inner, cy - inner, inner * 2, inner * 2, inner, UiRenderer.argb(0.55f, 8, 9, 12));
            ui.outline(cx - inner, cy - inner, inner * 2, inner * 2, inner, Math.max(1.0f, u * 0.9f), UiRenderer.white(0.08f));
            Slot cur = this.hovered >= 0 ? SLOTS[this.hovered] : null;
            boolean usable = cur != null && cur.action() != null;
            String title = cur == null ? "Quick Utilities" : cur.name();
            String hint = cur == null ? "Point at a utility" : cur.hint();
            float ts = (cur == null ? 17 : 19) * u;
            ui.textCentered(ui.ellipsize(title, inner * 1.7f, ts, 1), cx, cy - 15 * u, ts, 1, UiTheme.TEXT);
            ui.textCentered(ui.ellipsize(hint, inner * 1.7f, 11.5f * u, 0), cx, cy + 9 * u, 11.5f * u, 0, UiTheme.SUB);
            if (usable) UiTheme.accentDot(ui, cx, cy + 34 * u, u, this.hoverAnim[this.hovered]);

            ui.popTransform();
            // small key hint under the wheel
            boolean release = m == null || m.releaseToSelect.getValue();
            ui.textCentered(release ? "Release to open  ·  Esc to close" : "Click to open  ·  Esc to close", cx, cy + outer * scl + 22 * u, 11.5f * u, 0, UiTheme.FAINT);
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
