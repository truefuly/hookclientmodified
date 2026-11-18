package hook.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

/**
 * Proxies (like Meteor's): a list of saved SOCKS proxies, add / edit / delete / import, and a switch per proxy.
 * The one that is on carries every server connection, so you turn it on here and then join a server as usual.
 * Opened from the "Proxies" button at the top right of the multiplayer screen.
 */
public final class ProxyScreen extends class_437 {
    private static final int GREEN = UiRenderer.argb(1f, 92, 214, 140), RED = UiRenderer.argb(1f, 240, 96, 96);
    private static final String[] FIELDS = {"Name", "Address", "Port", "Username", "Password"};

    private final class_437 parent;
    private final long openedAt = System.nanoTime();
    private long closingAt = -1, last;
    private float dt = 0.016f;
    private final List<float[]> hits = new ArrayList<>();
    private final List<String> hitKeys = new ArrayList<>();
    private final Map<String, float[]> anims = new HashMap<>();
    private final UiScrollbar bar = new UiScrollbar();
    private float[] listArea = new float[4], clip;

    // editor (null = list view)
    private ProxyManager.Proxy editing;
    private boolean isNew;
    private final String[] values = new String[5];
    private ProxyManager.Type editType = ProxyManager.Type.SOCKS5;
    private int focus;
    private String toast = "";
    private long toastAt;
    private boolean toastBad;

    public ProxyScreen(class_437 parent) {
        super((class_2561) class_2561.method_43473());
        this.parent = parent;
    }

    private float scale() {
        return Math.max(1.0f, class_310.method_1551().method_22683().method_4495());
    }

    private float anim(String key, float target, float speed) {
        float[] a = this.anims.computeIfAbsent(key, k -> new float[]{target});
        a[0] += (target - a[0]) * (1.0f - (float) Math.exp(-speed * this.dt));
        return a[0];
    }

    private void hit(String key, float x, float y, float w, float h) {
        if (this.clip != null) {
            float nx = Math.max(x, this.clip[0]), ny = Math.max(y, this.clip[1]);
            float nw = Math.min(x + w, this.clip[0] + this.clip[2]) - nx, nh = Math.min(y + h, this.clip[1] + this.clip[3]) - ny;
            if (nw <= 0 || nh <= 0) return;
            x = nx; y = ny; w = nw; h = nh;
        }
        this.hits.add(new float[]{x, y, w, h});
        this.hitKeys.add(key);
    }

    private static boolean inside(double x, double y, float rx, float ry, float rw, float rh) {
        return x >= rx && x < rx + rw && y >= ry && y < ry + rh;
    }

    private float visibility() {
        long now = System.nanoTime();
        if (this.closingAt >= 0) {
            float t = UiEasing.clamp((now - this.closingAt) / 1.0e6f / 110f, 0, 1);
            return 1.0f - t * t;
        }
        return UiEasing.out(UiEasing.clamp((now - this.openedAt) / 1.0e6f / 170f, 0, 1));
    }

    private void say(String text, boolean bad) {
        this.toast = text;
        this.toastBad = bad;
        this.toastAt = System.currentTimeMillis();
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        this.dt = this.last == 0L ? 0.016f : Math.min(0.1f, (now - this.last) / 1.0e9f);
        this.last = now;
        float vis = this.visibility();
        if (this.closingAt >= 0 && vis <= 0.001f) {
            class_310.method_1551().method_1507(this.parent);
            return;
        }
        this.hits.clear();
        this.hitKeys.clear();
        if (class_310.method_1551().field_1687 == null) MenuBackground.render(ctx);
        float s = this.scale();
        int sw = class_310.method_1551().method_22683().method_4489();
        int sh = class_310.method_1551().method_22683().method_4506();
        double mx = mouseX * s, my = mouseY * s;
        ctx.method_51448().pushMatrix();
        ctx.method_51448().scale(1.0f / s, 1.0f / s);
        try {
            MinecraftUiRenderer ui = new MinecraftUiRenderer(ctx, s);
            float u = UiTheme.unit(sw, sh);
            UiTheme.backdrop(ui, sw, sh, vis);
            float scl = 0.94f + 0.06f * vis;
            ui.pushAlpha(vis);
            ui.pushTransform(sw / 2.0f * (1 - scl), sh / 2.0f * (1 - scl) + 14 * u * (1 - vis), scl);
            this.drawList(ui, sw, sh, u, mx, my);
            if (this.editing != null) this.drawEditor(ui, sw, sh, u, mx, my);
            this.drawToast(ui, sw, sh, u);
            ui.popTransform();
            ui.popAlpha();
        } finally {
            ctx.method_51448().popMatrix();
        }
    }

    private void button(UiRenderer ui, String id, String label, float x, float y, float w, float h, boolean accent, boolean enabled, float u, double mx, double my) {
        boolean hot = enabled && this.editingOk(id) && inside(mx, my, x, y, w, h);
        float hv = this.anim("b" + id, hot ? 1 : 0, 14);
        UiTheme.card(ui, x, y, w, h, 12 * u, hv, accent && enabled ? 1f : 0f, u);
        if (accent && enabled) ui.round(x, y, w, h, 12 * u, UiPalette.accent(0.12f + 0.1f * hv));
        int col = !enabled ? UiTheme.FAINT : accent ? UiEasing.mix(UiPalette.accent(1f), UiTheme.TEXT, 0.25f + 0.3f * hv) : UiTheme.TEXT;
        float ts = 13 * u;
        ui.textCentered(ui.ellipsize(label, w - 16 * u, ts, 1), x + w / 2, y + (h - ts) / 2 - u, ts, 1, col);
        if (enabled) this.hit(id, x, y, w, h);
    }

    /** While the editor is open only its own controls react. */
    private boolean editingOk(String id) {
        return this.editing == null || id.startsWith("#e-");
    }

    private float bw(UiRenderer ui, String label, float u) {
        return ui.textWidth(label, 13 * u, 1) + 34 * u;
    }

    private void drawList(UiRenderer ui, int sw, int sh, float u, double mx, double my) {
        List<ProxyManager.Proxy> list = ProxyManager.all();
        ProxyManager.Proxy active = ProxyManager.active();
        float pw = Math.min(sw - 48 * u, 860 * u), ph = Math.min(sh - 48 * u, 600 * u);
        float px = (sw - pw) / 2, py = (sh - ph) / 2;
        UiTheme.glass(ui, px, py, pw, ph, 22 * u, u, 3.3f);
        String sub = active == null ? "Direct connection  ·  no proxy on" : "On  ·  " + active.type.name() + "  ·  " + active.address();
        UiTheme.header(ui, px, py, pw, UiIcon.GLOBE, "Proxies", sub, u);
        float cb = 32 * u, cx = px + pw - 22 * u - cb, cy = py + (78 * u - cb) / 2.0f;
        UiTheme.iconButton(ui, UiIcon.CLOSE, cx, cy, cb, this.anim("close", this.editing == null && inside(mx, my, cx, cy, cb, cb) ? 1 : 0, 16), u);
        if (this.editing == null) this.hit("#close", cx, cy, cb, cb);

        float pad = 22 * u, x = px + pad, w = pw - 2 * pad, y = py + 78 * u + 16 * u, th = 36 * u;
        float b1 = this.bw(ui, "Add proxy", u), b2 = this.bw(ui, "Import from clipboard", u);
        this.button(ui, "#add", "Add proxy", x, y, b1, th, true, true, u, mx, my);
        this.button(ui, "#import", "Import from clipboard", x + b1 + 8 * u, y, b2, th, false, true, u, mx, my);
        if (active != null) {
            float b3 = this.bw(ui, "Turn off proxy", u);
            this.button(ui, "#off", "Turn off proxy", x + w - b3, y, b3, th, false, true, u, mx, my);
        }
        y += th + 14 * u;

        float footH = 34 * u, lh = py + ph - pad - footH - y;
        this.listArea = new float[]{x - 4 * u, y, w + 8 * u, lh};
        float rowH = 60 * u, gap = 8 * u;
        this.bar.update(lh, list.size() * (rowH + gap) - gap, this.dt);
        ui.pushClip(x - 4 * u, y, w + 8 * u, lh);
        this.clip = this.listArea;
        if (list.isEmpty()) {
            ui.textCentered("No proxies yet", x + w / 2, y + lh / 2 - 22 * u, 15 * u, 1, UiTheme.SUB);
            ui.textCentered("Add one, or copy a list (host:port or host:port:user:pass per line) and import it.", x + w / 2, y + lh / 2 + 2 * u, 12 * u, 0, UiTheme.FAINT);
        }
        boolean inList = this.editing == null && inside(mx, my, x, y, w, lh);
        float rw = w - 14 * u;
        for (int i = 0; i < list.size(); i++) {
            ProxyManager.Proxy p = list.get(i);
            float ry = y + i * (rowH + gap) - this.bar.pos;
            if (ry + rowH < y || ry > y + lh) continue;
            boolean hot = inList && inside(mx, my, x, ry, rw, rowH);
            UiTheme.card(ui, x, ry, rw, rowH, 14 * u, this.anim("r" + i, hot ? 1 : 0, 14), p.enabled ? 1f : 0f, u);
            if (p.enabled) ui.round(x + 6 * u, ry + 16 * u, 3 * u, rowH - 32 * u, 1.5f * u, UiPalette.accent(0.95f));
            // switch
            float tw = 40 * u, tgh = 22 * u, tx = x + 18 * u, ty = ry + (rowH - tgh) / 2;
            float t = this.anim("t" + i, p.enabled ? 1 : 0, 14);
            ui.round(tx, ty, tw, tgh, tgh / 2, UiEasing.mix(UiRenderer.white(0.12f), UiPalette.accent(0.85f), t));
            float k = tgh - 6 * u;
            ui.circle(tx + 3 * u + k / 2 + (tw - 6 * u - k) * t, ty + tgh / 2, k / 2, UiRenderer.white(0.95f));
            this.hit("tg:" + i, tx - 6 * u, ry, tw + 12 * u, rowH);
            float lx = tx + tw + 16 * u, iconW = 2 * (32 * u) + 8 * u, lw = rw - (lx - x) - iconW - 20 * u;
            ui.text(ui.ellipsize(p.label(), lw, 14 * u, 1), lx, ry + 12 * u, 14 * u, 1, UiTheme.TEXT);
            String info = p.type.name() + "  ·  " + p.address() + (p.user.isEmpty() ? "" : "  ·  " + p.user) + (p.enabled ? "  ·  in use" : "");
            ui.text(ui.ellipsize(info, lw, 11.5f * u, 0), lx, ry + 33 * u, 11.5f * u, 0, p.enabled ? GREEN : UiTheme.SUB);
            float ib = 32 * u, ix = x + rw - 14 * u - ib, iy = ry + (rowH - ib) / 2;
            UiTheme.iconButton(ui, UiIcon.TRASH, ix, iy, ib, this.anim("del" + i, inList && inside(mx, my, ix, iy, ib, ib) ? 1 : 0, 16), u);
            this.hit("del:" + i, ix, iy, ib, ib);
            UiTheme.iconButton(ui, UiIcon.EDIT, ix - ib - 8 * u, iy, ib, this.anim("ed" + i, inList && inside(mx, my, ix - ib - 8 * u, iy, ib, ib) ? 1 : 0, 16), u);
            this.hit("edit:" + i, ix - ib - 8 * u, iy, ib, ib);
        }
        this.clip = null;
        ui.popClip();
        this.bar.draw(ui, x + w - 3 * u, y, lh, u, mx, my, this.dt);

        float fy = py + ph - pad - footH;
        String hint = active == null ? "Turn a proxy on, then join a server: the connection goes through it." : "Join any server now and it goes through " + active.label() + ".";
        UiTheme.accentDot(ui, x + 6 * u, fy + footH / 2, u, active == null ? 0.35f : 1f);
        ui.text(ui.ellipsize(hint, w - 160 * u, 12 * u, 0), x + 18 * u, fy + (footH - 12 * u) / 2 - u, 12 * u, 0, UiTheme.SUB);
        float db = this.bw(ui, "Join a server", u);
        this.button(ui, "#done", "Join a server", x + w - db, fy, db, footH, active != null, true, u, mx, my);
    }

    private void drawEditor(UiRenderer ui, int sw, int sh, float u, double mx, double my) {
        this.hit("#e-block", 0, 0, sw, sh);
        ui.rect(0, 0, sw, sh, UiRenderer.argb(0.45f, 0, 0, 0));
        float pw = 520 * u, pad = 22 * u, fh = 40 * u, rowGap = 10 * u;
        float ph = 78 * u + pad + 22 * u + 34 * u + rowGap + FIELDS.length * (18 * u + fh + rowGap) + 8 * u + 40 * u + pad;
        ph = Math.min(ph, sh - 24 * u);
        float px = (sw - pw) / 2, py = (sh - ph) / 2;
        ui.round(px, py, pw, ph, 20 * u, UiPalette.glass(1f));
        UiTheme.glass(ui, px, py, pw, ph, 20 * u, u, 4f);
        UiTheme.header(ui, px, py, pw, UiIcon.GLOBE, this.isNew ? "Add proxy" : "Edit proxy", "SOCKS proxy for joining servers", u);
        float x = px + pad, w = pw - 2 * pad, y = py + 78 * u + pad;
        ui.text("Type", x, y, 12 * u, 1, UiTheme.SUB);
        y += 18 * u;
        float half = (w - 8 * u) / 2;
        for (int i = 0; i < 2; i++) {
            ProxyManager.Type t = ProxyManager.Type.values()[i];
            boolean sel = this.editType == t;
            float bx = x + i * (half + 8 * u);
            float hv = this.anim("ty" + i, inside(mx, my, bx, y, half, 34 * u) ? 1 : 0, 14), sv = this.anim("tys" + i, sel ? 1 : 0, 12);
            UiTheme.card(ui, bx, y, half, 34 * u, 11 * u, hv, sv, u);
            if (sv > 0.01f) ui.round(bx, y, half, 34 * u, 11 * u, UiPalette.accent(0.12f * sv));
            ui.textCentered(t.name(), bx + half / 2, y + (34 * u - 13 * u) / 2 - u, 13 * u, 1, UiEasing.mix(UiTheme.SUB, UiPalette.accent(1f), sv));
            this.hit("#e-type:" + i, bx, y, half, 34 * u);
        }
        y += 34 * u + rowGap;
        for (int i = 0; i < FIELDS.length; i++) {
            ui.text(FIELDS[i] + (i == 0 || i >= 3 ? "  (optional)" : ""), x, y, 12 * u, 1, UiTheme.SUB);
            y += 18 * u;
            boolean f = this.focus == i;
            UiTheme.card(ui, x, y, w, fh, 12 * u, this.anim("f" + i, inside(mx, my, x, y, w, fh) ? 1 : 0, 14), f ? 1f : 0f, u);
            String v = this.values[i];
            String shown = i == 4 ? "•".repeat(v.length()) : v;
            String ph2 = switch (i) { case 0 -> "My proxy"; case 1 -> "proxy.example.com or 1.2.3.4"; case 2 -> "1080"; case 3 -> "user"; default -> "password"; };
            float ts = 14 * u;
            String txt = shown.isEmpty() ? ph2 : shown;
            String vis = txt;
            while (vis.length() > 1 && ui.textWidth(vis, ts, 0) > w - 32 * u) vis = vis.substring(1); // keep the end (cursor) visible
            ui.text(vis, x + 14 * u, y + (fh - ts) / 2 - u, ts, 0, shown.isEmpty() ? UiTheme.FAINT : UiTheme.TEXT);
            if (f && (System.currentTimeMillis() / 530) % 2 == 0) ui.rect(x + 14 * u + (shown.isEmpty() ? 0 : ui.textWidth(vis, ts, 0) + u), y + fh / 2 - 8 * u, Math.max(1f, 1.4f * u), 16 * u, UiPalette.accent(0.9f));
            this.hit("#e-field:" + i, x, y, w, fh);
            y += fh + rowGap;
        }
        y += 8 * u;
        float b = (w - 8 * u) / 2;
        this.button(ui, "#e-cancel", "Cancel", x, y, b, 40 * u, false, true, u, mx, my);
        this.button(ui, "#e-save", "Save", x + b + 8 * u, y, b, 40 * u, true, !this.values[1].isBlank() && this.port() > 0, u, mx, my);
    }

    private void drawToast(UiRenderer ui, int sw, int sh, float u) {
        long age = System.currentTimeMillis() - this.toastAt;
        if (this.toast.isEmpty() || age > 3200) return;
        float a = Math.min(1, Math.min(age / 150f, (3200 - age) / 300f));
        float ts = 12.5f * u, w = ui.textWidth(this.toast, ts, 1) + 40 * u, h = 36 * u, x = (sw - w) / 2, y = sh - h - 30 * u;
        ui.pushAlpha(a);
        ui.round(x, y, w, h, h / 2, UiPalette.glass(0.96f));
        ui.circle(x + 18 * u, y + h / 2, 3.5f * u, this.toastBad ? RED : UiPalette.accent(1f));
        ui.text(this.toast, x + 28 * u, y + (h - ts) / 2 - u, ts, 1, UiTheme.TEXT);
        ui.popAlpha();
    }

    // ------------------------------------------------------------------ editing

    private int port() {
        try { int p = Integer.parseInt(this.values[2].trim()); return p > 0 && p <= 65535 ? p : -1; } catch (Exception e) { return -1; }
    }

    private void openEditor(ProxyManager.Proxy p, boolean fresh) {
        this.editing = p;
        this.isNew = fresh;
        this.values[0] = p.name;
        this.values[1] = p.host;
        this.values[2] = fresh ? "" : String.valueOf(p.port);
        this.values[3] = p.user;
        this.values[4] = p.password;
        this.editType = p.type;
        this.focus = fresh ? 1 : 0;
    }

    private void save() {
        if (this.editing == null) return;
        // "host:port" typed into the address field: split it
        String host = this.values[1].trim();
        int c = host.lastIndexOf(':');
        if (c > 0 && host.indexOf(':') == c && this.values[2].isBlank()) { this.values[2] = host.substring(c + 1); host = host.substring(0, c); }
        if (host.isEmpty() || this.port() < 0) { this.say("Enter an address and a port (1-65535).", true); return; }
        ProxyManager.Proxy p = this.editing;
        p.name = this.values[0].trim();
        p.host = host;
        p.port = this.port();
        p.user = this.values[3].trim();
        p.password = this.values[4];
        p.type = this.editType;
        if (this.isNew) ProxyManager.all().add(p);
        ProxyManager.save();
        this.say(this.isNew ? "Proxy added" : "Proxy saved", false);
        this.editing = null;
    }

    private void close() {
        if (this.closingAt >= 0) return;
        this.closingAt = System.nanoTime();
        UiAudio.play(UiAudio.Cue.GUI_CLOSE);
    }

    // ------------------------------------------------------------------ input

    @Override
    public void method_25420(class_332 ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        if (click.method_74245() != 0 || this.closingAt >= 0) return true;
        double s = this.scale(), x = click.comp_4798() * s, y = click.comp_4799() * s;
        if (this.editing == null && this.bar.press(x, y)) return true;
        for (int i = this.hits.size() - 1; i >= 0; i--) {
            float[] r = this.hits.get(i);
            if (!inside(x, y, r[0], r[1], r[2], r[3])) continue;
            String k = this.hitKeys.get(i);
            if (!this.editingOk(k)) return true;
            this.click(k);
            return true;
        }
        return true;
    }

    private void click(String k) {
        List<ProxyManager.Proxy> list = ProxyManager.all();
        switch (k) {
            case "#close", "#done" -> this.close();
            case "#add" -> this.openEditor(new ProxyManager.Proxy(), true);
            case "#import" -> {
                String clip = class_310.method_1551().field_1774.method_1460();
                int n = ProxyManager.importText(clip);
                this.say(n == 0 ? "Nothing to import: copy lines like host:port or host:port:user:pass first." : "Imported " + n + (n == 1 ? " proxy" : " proxies"), n == 0);
            }
            case "#off" -> {
                for (ProxyManager.Proxy p : list) p.enabled = false;
                ProxyManager.save();
                this.say("Proxy off: direct connection", false);
            }
            case "#e-block" -> {}
            case "#e-cancel" -> this.editing = null;
            case "#e-save" -> this.save();
            default -> {
                if (k.startsWith("#e-type:")) this.editType = ProxyManager.Type.values()[Integer.parseInt(k.substring(8))];
                else if (k.startsWith("#e-field:")) this.focus = Integer.parseInt(k.substring(9));
                else {
                    int c = k.indexOf(':');
                    int i = Integer.parseInt(k.substring(c + 1));
                    if (i < 0 || i >= list.size()) return;
                    ProxyManager.Proxy p = list.get(i);
                    switch (k.substring(0, c)) {
                        case "tg" -> {
                            ProxyManager.setEnabled(p, !p.enabled);
                            this.say(p.enabled ? "Using " + p.label() + " for joining servers" : "Proxy off: direct connection", false);
                        }
                        case "edit" -> this.openEditor(p, false);
                        case "del" -> {
                            list.remove(i);
                            ProxyManager.save();
                            this.say("Removed " + p.label(), false);
                        }
                        default -> {}
                    }
                }
            }
        }
        UiAudio.play(UiAudio.Cue.TICK);
    }

    @Override
    public boolean method_25403(class_11909 click, double dx, double dy) {
        this.bar.drag(click.comp_4799() * this.scale());
        return true;
    }

    @Override
    public boolean method_25406(class_11909 click) {
        this.bar.release();
        return true;
    }

    @Override
    public boolean method_25401(double x, double y, double h, double v) {
        if (this.editing == null) this.bar.wheel(-(float) v * 70 * UiTheme.unit(class_310.method_1551().method_22683().method_4489(), class_310.method_1551().method_22683().method_4506()));
        return true;
    }

    @Override
    public boolean method_25404(class_11908 key) {
        int code = key.comp_4795();
        boolean ctrl = (key.comp_4797() & 2) != 0, shift = (key.comp_4797() & 1) != 0;
        if (this.editing == null) {
            if (code == 256) this.close();
            return true;
        }
        if (code == 256) { this.editing = null; return true; }
        if (code == 258) { this.focus = (this.focus + (shift ? FIELDS.length - 1 : 1)) % FIELDS.length; return true; }
        if (code == 257 || code == 335) { this.save(); return true; }
        String v = this.values[this.focus];
        if (code == 259 && !v.isEmpty()) this.values[this.focus] = ctrl ? "" : v.substring(0, v.length() - 1);
        else if (code == 86 && ctrl) {
            String clip = class_310.method_1551().field_1774.method_1460();
            if (clip != null) this.type(clip.replaceAll("[\\r\\n\\t]", "").trim());
        }
        return true;
    }

    private void type(String s) {
        String v = this.values[this.focus] + s;
        if (this.focus == 2) v = v.replaceAll("[^0-9]", "");
        if (this.focus == 1) v = v.replace(" ", "");
        this.values[this.focus] = v.length() > 128 ? v.substring(0, 128) : v;
    }

    @Override
    public boolean method_25400(class_11905 input) {
        if (!input.method_74227() || this.editing == null) return true;
        char c = (char) input.comp_4793();
        if (c >= 32 && c != 127) this.type(String.valueOf(c));
        return true;
    }

    @Override
    public void method_25419() {
        if (this.editing != null) this.editing = null;
        else this.close();
    }

    @Override
    public boolean method_25421() {
        return false;
    }

    void testEdit(int i) {
        if (i < ProxyManager.all().size()) this.openEditor(ProxyManager.all().get(i), false);
    }

    // ------------------------------------------------------------------ "Proxies" button on the multiplayer screen

    /** Adds the "Proxies" button at the top right of the multiplayer screen (like Meteor). */
    public static void registerButton() {
        try {
            net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
                if (!(screen instanceof net.minecraft.class_500)) return;
                ProxyManager.Proxy a = ProxyManager.active();
                String label = a == null ? "Proxies" : "Proxy: on";
                net.minecraft.class_4185 b = net.minecraft.class_4185.method_46430(class_2561.method_43470(label), btn -> client.method_1507(new ProxyScreen(screen)))
                        .method_46434(w - 75 - 3, 3, 75, 20).method_46431();
                net.fabricmc.fabric.api.client.screen.v1.Screens.getButtons(screen).add(b);
            });
        } catch (Throwable t) {
            System.err.println("Hook: no proxies button (Fabric API screen events missing): " + t);
        }
    }
}
