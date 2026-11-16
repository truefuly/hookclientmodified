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
        new Slot("Calculator", "Quick maths", UiIcon.GRID, () -> class_310.method_1551().method_1507((class_437) new CalculatorScreen())),
        new Slot("Empty", "More soon", UiIcon.PLUS, null),
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

    private float d(float v) {
        int h = class_310.method_1551().method_22683().method_4506();
        return v * Math.max(0.75f, h / 1080.0f);
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
        double mx = mouseX * s, my = mouseY * s;
        float cx = sw / 2.0f, cy = sh / 2.0f;
        float outer = this.d(250), inner = this.d(98);
        int n = SLOTS.length;

        // which slot the mouse points at (direction from the centre, small dead zone in the middle)
        double dx = mx - cx, dy = my - cy;
        int hov = -1;
        if (Math.hypot(dx, dy) > this.d(40)) {
            double ang = Math.toDegrees(Math.atan2(dx, -dy)); // 0 = up, clockwise
            ang = (ang + 360.0 + 180.0 / n) % 360.0;
            hov = (int) (ang / (360.0 / n));
            float target = (float) Math.toDegrees(Math.atan2(dx, -dy));
            if (Float.isNaN(this.pointer)) this.pointer = target;
            float diff = ((target - this.pointer + 540f) % 360f) - 180f;
            this.pointer += diff * (1.0f - (float) Math.exp(-22f * this.dt));
        }
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
            ui.rect(0, 0, sw, sh, UiRenderer.argb(0.30f * vis, 0, 0, 0));
            // grows out from behind: small and faded -> full size
            float scl = 0.78f + 0.22f * vis;
            ui.pushAlpha(vis);
            ui.pushTransform(cx * (1 - scl), cy * (1 - scl), scl);

            // wheel body
            ui.glow(cx, cy, outer + this.d(30), UiPalette.accent(0.10f));
            ui.circle(cx, cy, outer, UiEasing.mix(UiPalette.glass(0.92f), UiRenderer.argb(0.92f, 14, 15, 19), 0.55f));
            ui.circle(cx, cy, outer - this.d(2), UiRenderer.argb(0.0f, 0, 0, 0));
            ui.circle(cx, cy, inner + this.d(6), UiRenderer.white(0.05f));

            float slotDist = (outer + inner) / 2.0f;
            float slotR = this.d(52);
            for (int i = 0; i < n; i++) {
                double a = Math.toRadians(i * 360.0 / n);
                float sx = cx + (float) (Math.sin(a) * slotDist), sy = cy - (float) (Math.cos(a) * slotDist);
                boolean hot = i == this.hovered;
                this.hoverAnim[i] += ((hot ? 1f : 0f) - this.hoverAnim[i]) * (1.0f - (float) Math.exp(-18f * this.dt));
                float h = this.hoverAnim[i];
                Slot slot = SLOTS[i];
                boolean empty = slot.action() == null;
                float r = slotR * (1.0f + 0.10f * h);
                if (h > 0.02f) ui.glow(sx, sy, r + this.d(18), UiPalette.accent(0.35f * h));
                ui.circle(sx, sy, r, UiEasing.mix(UiRenderer.white(empty ? 0.03f : 0.06f), UiPalette.accent(0.85f), h));
                float is = this.d(26) * (1.0f + 0.08f * h);
                int iconCol = empty ? UiRenderer.white(0.25f + 0.3f * h) : UiEasing.mix(UiRenderer.white(0.85f), UiRenderer.argb(0.95f, 14, 15, 19), h);
                ui.icon(slot.icon(), sx - is / 2, sy - is / 2, is, iconCol);
            }

            // highlighted arc along the rim toward the hovered slot (dots make a smooth arc)
            if (this.hovered >= 0 && !Float.isNaN(this.pointer)) {
                float span = 360f / n;
                float rimR = outer - this.d(7);
                for (float t = -span / 2 + 1; t <= span / 2 - 1; t += 1.2f) {
                    double a = Math.toRadians(this.pointer + t);
                    float fade = 1.0f - Math.abs(t) / (span / 2);
                    ui.circle(cx + (float) (Math.sin(a) * rimR), cy - (float) (Math.cos(a) * rimR), this.d(3.2f), UiPalette.accent(0.35f + 0.6f * fade));
                }
            }

            // centre: name of what you point at
            ui.circle(cx, cy, inner, UiRenderer.argb(0.85f, 10, 11, 14));
            Slot cur = this.hovered >= 0 ? SLOTS[this.hovered] : null;
            String title = cur == null ? "Quick Utilities" : cur.name();
            String hint = cur == null ? "Point at a utility" : cur.hint();
            ui.textCentered(title, cx, cy - this.d(16), this.d(17), 1, cur != null && cur.action() != null ? UiPalette.accent(1.0f) : UiRenderer.white(0.95f));
            ui.textCentered(hint, cx, cy + this.d(8), this.d(11), 0, UiRenderer.white(0.5f));

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
