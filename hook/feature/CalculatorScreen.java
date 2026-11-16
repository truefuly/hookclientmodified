package hook.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

/** Calculator utility in the client's glass style. Click the buttons or type; Enter = result, Esc closes. */
public final class CalculatorScreen extends class_437 {
    private static final String[][] KEYS = {
        {"C", "(", ")", "/"},
        {"7", "8", "9", "*"},
        {"4", "5", "6", "-"},
        {"1", "2", "3", "+"},
        {"%", "0", ".", "="},
    };
    private String expr = "";
    private String result = "";
    private final long openedAt = System.nanoTime();
    private long closingAt = -1L;
    private final List<float[]> hits = new ArrayList<>();
    private final List<String> hitKeys = new ArrayList<>();
    private final java.util.Map<String, float[]> anims = new java.util.HashMap<>();
    private long last;
    private float dt = 0.016f;
    private String flash;
    private long flashAt;

    public CalculatorScreen() {
        super((class_2561) class_2561.method_43473());
    }

    @Override
    protected void method_25426() {
        cue(UiAudio.Cue.GUI_OPEN);
    }

    private static void cue(UiAudio.Cue c) {
        if (QuickUtilitiesModule.instance != null) QuickUtilitiesModule.instance.cue(c);
        else UiAudio.play(c);
    }

    private float scale() {
        return Math.max(1.0f, class_310.method_1551().method_22683().method_4495());
    }

    private float d(float v) {
        int h = class_310.method_1551().method_22683().method_4506();
        return v * Math.max(0.75f, h / 1080.0f);
    }

    private float anim(String key, float target, float speed) {
        float[] a = this.anims.computeIfAbsent(key, k -> new float[]{target});
        a[0] += (target - a[0]) * (1.0f - (float) Math.exp(-speed * this.dt));
        return a[0];
    }

    private float visibility() {
        long now = System.nanoTime();
        if (this.closingAt >= 0) {
            float t = UiEasing.clamp((now - this.closingAt) / 1.0e6f / 110f, 0, 1);
            return 1.0f - t * t;
        }
        return UiEasing.out(UiEasing.clamp((now - this.openedAt) / 1.0e6f / 170f, 0, 1));
    }

    private void press(String k) {
        this.flash = k;
        this.flashAt = System.nanoTime();
        cue(UiAudio.Cue.TICK);
        switch (k) {
            case "C" -> { this.expr = ""; this.result = ""; }
            case "=" -> {
                String r = evaluate(this.expr);
                if (r != null) { this.result = this.expr + " ="; this.expr = r; }
                else this.result = "Error";
            }
            case "<" -> { if (!this.expr.isEmpty()) this.expr = this.expr.substring(0, this.expr.length() - 1); }
            default -> { if (this.expr.length() < 48) this.expr += k; }
        }
    }

    // ------------------------------------------------------------------ maths (recursive descent, no eval)

    static String evaluate(String s) {
        try {
            Parser p = new Parser(s.replace(" ", ""));
            double v = p.expression();
            if (p.pos != p.src.length() || Double.isNaN(v) || Double.isInfinite(v)) return null;
            if (v == Math.rint(v) && Math.abs(v) < 1e15) return Long.toString((long) v);
            String out = String.format(Locale.ROOT, "%.10f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
            return out;
        } catch (Throwable t) {
            return null;
        }
    }

    private static final class Parser {
        final String src;
        int pos;

        Parser(String src) {
            this.src = src;
        }

        char peek() {
            return this.pos < this.src.length() ? this.src.charAt(this.pos) : '\0';
        }

        double expression() {
            double v = this.term();
            while (true) {
                char c = this.peek();
                if (c == '+') { this.pos++; v += this.term(); }
                else if (c == '-') { this.pos++; v -= this.term(); }
                else return v;
            }
        }

        double term() {
            double v = this.factor();
            while (true) {
                char c = this.peek();
                if (c == '*' || c == 'x') { this.pos++; v *= this.factor(); }
                else if (c == '/') { this.pos++; v /= this.factor(); }
                else if (c == '(') { v *= this.factor(); } // 2(3) = 6
                else return v;
            }
        }

        double factor() {
            char c = this.peek();
            double v;
            if (c == '-') { this.pos++; return -this.factor(); }
            if (c == '+') { this.pos++; return this.factor(); }
            if (c == '(') {
                this.pos++;
                v = this.expression();
                if (this.peek() == ')') this.pos++;
            } else {
                int start = this.pos;
                while (Character.isDigit(this.peek()) || this.peek() == '.') this.pos++;
                if (start == this.pos) throw new IllegalArgumentException();
                v = Double.parseDouble(this.src.substring(start, this.pos));
            }
            if (this.peek() == '^') { this.pos++; v = Math.pow(v, this.factor()); }
            while (this.peek() == '%') { this.pos++; v /= 100.0; }
            return v;
        }
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        this.dt = this.last == 0L ? 0.016f : Math.min(0.1f, (now - this.last) / 1.0e9f);
        this.last = now;
        float vis = this.visibility();
        if (this.closingAt >= 0 && vis <= 0.001f) {
            class_310.method_1551().method_1507(null);
            return;
        }
        this.hits.clear();
        this.hitKeys.clear();
        QuickUtilitiesModule m = QuickUtilitiesModule.instance;
        if (m == null || m.blur.getValue()) {
            try { ctx.method_71278(); } catch (Throwable ignored) {}
        }
        float s = this.scale();
        int sw = class_310.method_1551().method_22683().method_4489();
        int sh = class_310.method_1551().method_22683().method_4506();
        double mx = mouseX * s, my = mouseY * s;

        ctx.method_51448().pushMatrix();
        ctx.method_51448().scale(1.0f / s, 1.0f / s);
        try {
            MinecraftUiRenderer ui = new MinecraftUiRenderer(ctx, s);
            ui.rect(0, 0, sw, sh, UiRenderer.argb(0.30f * vis, 0, 0, 0));
            float pw = this.d(380), gap = this.d(10), bh = this.d(58);
            float ph = this.d(180) + KEYS.length * (bh + gap) + this.d(14);
            float px = (sw - pw) / 2.0f, py = (sh - ph) / 2.0f;
            float scl = 0.82f + 0.18f * vis;
            ui.pushAlpha(vis);
            ui.pushTransform(sw / 2.0f * (1 - scl), sh / 2.0f * (1 - scl), scl);

            ui.shadow(px, py, pw, ph, this.d(22), this.d(30), UiRenderer.argb(0.45f, 0, 0, 0));
            ui.round(px, py, pw, ph, this.d(22), UiEasing.mix(UiPalette.glass(0.97f), UiRenderer.argb(0.97f, 14, 15, 19), 0.5f));
            ui.outline(px, py, pw, ph, this.d(22), Math.max(1.0f, this.d(1)), UiRenderer.white(0.08f));
            ui.text("Calculator", px + this.d(24), py + this.d(20), this.d(18), 1, UiRenderer.white(0.96f));
            // close button
            float cxb = px + pw - this.d(44), cyb = py + this.d(16);
            boolean chot = inside(mx, my, cxb, cyb, this.d(28), this.d(28));
            ui.round(cxb, cyb, this.d(28), this.d(28), this.d(9), UiRenderer.white(0.04f + 0.08f * this.anim("close", chot ? 1 : 0, 16)));
            ui.icon(UiIcon.CLOSE, cxb + this.d(8), cyb + this.d(8), this.d(12), UiRenderer.white(0.7f));
            this.hits.add(new float[]{cxb, cyb, this.d(28), this.d(28)});
            this.hitKeys.add("#close");

            // display
            float dx = px + this.d(20), dy = py + this.d(60), dw = pw - this.d(40), dh = this.d(100);
            ui.round(dx, dy, dw, dh, this.d(16), UiRenderer.argb(0.9f, 8, 9, 12));
            ui.outline(dx, dy, dw, dh, this.d(16), Math.max(1.0f, this.d(1)), UiPalette.accent(0.35f));
            ui.textRight(ui.ellipsize(this.result, dw - this.d(30), this.d(13), 0), dx + dw - this.d(16), dy + this.d(16), this.d(13), 0, this.result.equals("Error") ? UiRenderer.argb(0.9f, 240, 90, 90) : UiRenderer.white(0.45f));
            boolean caret = (System.currentTimeMillis() / 500L) % 2L == 0L;
            String shown = this.expr.isEmpty() ? "0" : this.expr;
            float big = this.d(34);
            while (big > this.d(16) && ui.textWidth(shown, big, 1) > dw - this.d(32)) big -= this.d(1);
            ui.textRight(shown + (caret ? "" : ""), dx + dw - this.d(16), dy + dh - big - this.d(16), big, 1, UiRenderer.white(0.96f));

            // keys
            float kw = (pw - this.d(40) - gap * 3) / 4.0f;
            float ky = dy + dh + this.d(20);
            for (int r = 0; r < KEYS.length; r++) {
                for (int c = 0; c < 4; c++) {
                    String k = KEYS[r][c];
                    float kx = px + this.d(20) + c * (kw + gap), y = ky + r * (bh + gap);
                    boolean op = "/*-+=".contains(k) && k.length() == 1;
                    boolean hot = inside(mx, my, kx, y, kw, bh);
                    float h = this.anim("k" + k, hot ? 1 : 0, 16);
                    float f = k.equals(this.flash) ? Math.max(0f, 1f - (now - this.flashAt) / 1.0e6f / 180f) : 0f;
                    int base = k.equals("=") ? UiPalette.accent(0.85f) : op ? UiPalette.accent(0.18f + 0.12f * h) : UiRenderer.white(0.05f + 0.06f * h);
                    ui.round(kx, y, kw, bh, this.d(14), UiEasing.mix(base, UiPalette.accent(0.9f), f * 0.7f));
                    ui.outline(kx, y, kw, bh, this.d(14), Math.max(1.0f, this.d(1)), UiRenderer.white(0.05f + 0.07f * h));
                    String label = k.equals("*") ? "×" : k.equals("/") ? "÷" : k;
                    int col = k.equals("=") ? UiRenderer.argb(0.95f, 14, 15, 19) : op ? UiPalette.accent(1.0f) : UiRenderer.white(0.92f);
                    ui.textCentered(label, kx + kw / 2.0f, y + (bh - this.d(20)) / 2.0f - this.d(1), this.d(20), 1, col);
                    this.hits.add(new float[]{kx, y, kw, bh});
                    this.hitKeys.add(k);
                }
            }
            ui.popTransform();
            ui.popAlpha();
        } finally {
            ctx.method_51448().popMatrix();
        }
    }

    private static boolean inside(double x, double y, float rx, float ry, float rw, float rh) {
        return x >= rx && x < rx + rw && y >= ry && y < ry + rh;
    }

    private void close() {
        if (this.closingAt >= 0) return;
        this.closingAt = System.nanoTime();
        cue(UiAudio.Cue.GUI_CLOSE);
    }

    @Override
    public void method_25420(class_332 ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        if (click.method_74245() != 0 || this.closingAt >= 0) return true;
        double s = this.scale();
        double x = click.comp_4798() * s, y = click.comp_4799() * s;
        for (int i = this.hits.size() - 1; i >= 0; i--) {
            float[] r = this.hits.get(i);
            if (inside(x, y, r[0], r[1], r[2], r[3])) {
                String k = this.hitKeys.get(i);
                if (k.equals("#close")) this.close();
                else this.press(k);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean method_25404(class_11908 key) {
        int code = key.comp_4795();
        if (code == 256) { this.close(); return true; }                  // Esc
        if (code == 257 || code == 335) { this.press("="); return true; } // Enter
        if (code == 259) { this.press("<"); return true; }                // Backspace
        if (code == 261) { this.press("C"); return true; }                // Delete
        return true;
    }

    @Override
    public boolean method_25400(class_11905 input) {
        if (!input.method_74227()) return true;
        char c = (char) input.comp_4793();
        if ("0123456789+-*/().%^".indexOf(c) >= 0) this.press(String.valueOf(c));
        else if (c == ',') this.press(".");
        else if (c == 'x' || c == 'X') this.press("*");
        else if (c == '=') this.press("=");
        return true;
    }

    @Override
    public void method_25419() {
        this.close();
    }

    @Override
    public boolean method_25421() {
        return false;
    }
}
