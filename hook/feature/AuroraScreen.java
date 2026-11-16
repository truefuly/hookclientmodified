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
 * AuroraAFK inside Minecraft: the same layout as the AuroraAFK app (server profiles on the left, the accounts of the
 * selected profile in the middle, chat on the right), drawn in the HookVisuals glass style and theme colour.
 * The first time it asks for a key; the key is checked by the AuroraAFK VPS and then stored encrypted on this PC.
 */
public final class AuroraScreen extends class_437 {
    private static final int GREEN = UiRenderer.argb(1f, 92, 214, 140), AMBER = UiRenderer.argb(1f, 240, 184, 76), RED = UiRenderer.argb(1f, 240, 96, 96);

    private final AuroraClient api = AuroraClient.INSTANCE;
    private final long openedAt = System.nanoTime();
    private long closingAt = -1L, last;
    private float dt = 0.016f;
    private final List<float[]> hits = new ArrayList<>();
    private final List<String> hitKeys = new ArrayList<>();
    private final Map<String, float[]> anims = new HashMap<>();

    private String keyInput = "";
    private String chatInput = "";
    private boolean chatFocus;
    private String serverId, accountId;
    private float listScroll, listScrollTarget, chatScroll, chatScrollTarget;
    private float listMax, chatMax;
    private float[] listArea = new float[4], chatArea = new float[4];

    public AuroraScreen() {
        super((class_2561) class_2561.method_43473());
    }

    @Override
    protected void method_25426() {
        cue(UiAudio.Cue.GUI_OPEN);
        this.api.open();
    }

    private static void cue(UiAudio.Cue c) {
        if (QuickUtilitiesModule.instance != null) QuickUtilitiesModule.instance.cue(c);
        else UiAudio.play(c);
    }

    private float scale() {
        return Math.max(1.0f, class_310.method_1551().method_22683().method_4495());
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

    private void hit(String key, float x, float y, float w, float h) {
        this.hits.add(new float[]{x, y, w, h});
        this.hitKeys.add(key);
    }

    private static boolean inside(double x, double y, float rx, float ry, float rw, float rh) {
        return x >= rx && x < rx + rw && y >= ry && y < ry + rh;
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
        this.api.tick();
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
            float u = UiTheme.unit(sw, sh);
            UiTheme.backdrop(ui, sw, sh, vis);
            float scl = 0.94f + 0.06f * vis;
            ui.pushAlpha(vis);
            ui.pushTransform(sw / 2.0f * (1 - scl), sh / 2.0f * (1 - scl) + 14 * u * (1 - vis), scl);
            if (this.api.status == AuroraClient.Status.READY) this.drawPanel(ui, sw, sh, u, mx, my);
            else this.drawKey(ui, sw, sh, u, mx, my);
            this.drawToast(ui, sw, sh, u);
            ui.popTransform();
            ui.popAlpha();
        } finally {
            ctx.method_51448().popMatrix();
        }
    }

    private void closeButton(UiRenderer ui, float px, float py, float pw, float u, double mx, double my) {
        float cb = 32 * u, cx = px + pw - 22 * u - cb, cy = py + (78 * u - cb) / 2.0f;
        UiTheme.iconButton(ui, UiIcon.CLOSE, cx, cy, cb, this.anim("close", inside(mx, my, cx, cy, cb, cb) ? 1 : 0, 16), u);
        this.hit("#close", cx, cy, cb, cb);
    }

    /** A text button drawn like the module cards; accent = the main action. */
    private void button(UiRenderer ui, String id, String label, float x, float y, float w, float h, boolean accent, boolean enabled, float u, double mx, double my) {
        boolean hot = enabled && inside(mx, my, x, y, w, h);
        float hv = this.anim("b" + id, hot ? 1 : 0, 14);
        UiTheme.card(ui, x, y, w, h, 12 * u, hv, accent && enabled ? 1f : 0f, u);
        if (accent && enabled) ui.round(x, y, w, h, 12 * u, UiPalette.accent(0.12f + 0.1f * hv));
        int col = !enabled ? UiTheme.FAINT : accent ? UiEasing.mix(UiPalette.accent(1f), UiTheme.TEXT, 0.25f + 0.3f * hv) : UiTheme.TEXT;
        float ts = 13 * u;
        ui.textCentered(ui.ellipsize(label, w - 16 * u, ts, 1), x + w / 2, y + (h - ts) / 2 - u, ts, 1, col);
        if (enabled) this.hit(id, x, y, w, h);
    }

    // ---- key screen ---------------------------------------------------------------------------------

    private void drawKey(UiRenderer ui, int sw, int sh, float u, double mx, double my) {
        boolean checking = this.api.status == AuroraClient.Status.CHECKING;
        float pw = 500 * u, pad = 24 * u;
        String info = "Paste the access key you got. The AuroraAFK server checks it, then it is saved encrypted on this PC so you only enter it once.";
        List<String> infoLines = wrap(ui, info, pw - 2 * pad, 12.5f * u, 0);
        String msg = this.api.message;
        List<String> msgLines = msg.isEmpty() ? List.of() : wrap(ui, msg, pw - 2 * pad - 22 * u, 12 * u, 0);
        float ph = 78 * u + 20 * u + infoLines.size() * 18 * u + 16 * u + 54 * u + 14 * u + (msgLines.isEmpty() ? 0 : msgLines.size() * 17 * u + 10 * u) + 46 * u + pad;
        float px = (sw - pw) / 2, py = (sh - ph) / 2;
        UiTheme.glass(ui, px, py, pw, ph, 22 * u, u, 5.1f);
        UiTheme.header(ui, px, py, pw, UiIcon.HEAD, "AuroraAFK", checking ? "Checking your key…" : "Unlock with your access key", u);
        this.closeButton(ui, px, py, pw, u, mx, my);

        float y = py + 78 * u + 20 * u;
        for (String line : infoLines) { ui.text(line, px + pad, y, 12.5f * u, 0, UiTheme.SUB); y += 18 * u; }
        y += 16 * u;

        // key field
        float fx = px + pad, fw = pw - 2 * pad, fh = 54 * u;
        boolean hasText = !this.keyInput.isEmpty();
        UiTheme.card(ui, fx, y, fw, fh, 14 * u, 0f, 1f, u);
        UiTheme.accentDot(ui, fx + 20 * u, y + fh / 2, u, checking ? 1f : 0.6f);
        ui.icon(UiIcon.KEY, fx + fw - 34 * u, y + (fh - 18 * u) / 2, 18 * u, UiTheme.FAINT);
        float ts = 17 * u;
        String shown = hasText ? this.keyInput : "HV-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX";
        shown = ui.ellipsize(shown, fw - 90 * u, ts, 1);
        float tw = ui.text(shown, fx + 38 * u, y + (fh - ts) / 2 - u, ts, 1, hasText ? UiTheme.TEXT : UiTheme.FAINT);
        if (!checking && (System.currentTimeMillis() / 530) % 2 == 0) {
            float cx = hasText ? fx + 38 * u + ui.textWidth(shown, ts, 1) + 2 * u : fx + 38 * u;
            ui.rect(cx, y + fh / 2 - 10 * u, Math.max(1f, 1.6f * u), 20 * u, UiPalette.accent(0.9f));
        }
        y += fh + 14 * u;

        if (!msgLines.isEmpty()) {
            int col = this.api.messageError ? RED : UiTheme.SUB;
            ui.circle(px + pad + 4 * u, y + 7 * u, 3 * u, this.api.messageError ? RED : UiPalette.accent(0.9f));
            for (String line : msgLines) { ui.text(line, px + pad + 16 * u, y, 12 * u, 0, col); y += 17 * u; }
            y += 10 * u;
        }

        float bw = (pw - 2 * pad - 12 * u) / 2;
        this.button(ui, "#paste", "Paste", px + pad, y, bw, 40 * u, false, !checking, u, mx, my);
        this.button(ui, "#unlock", checking ? "Checking…" : "Unlock", px + pad + bw + 12 * u, y, bw, 40 * u, true, !checking && hasText, u, mx, my);
    }

    // ---- main panel ---------------------------------------------------------------------------------

    private void drawPanel(UiRenderer ui, int sw, int sh, float u, double mx, double my) {
        List<AuroraClient.Server> servers = this.api.servers;
        List<AuroraClient.Account> accounts = this.api.accounts;
        Map<String, AuroraClient.Session> sessions = this.api.sessions;
        if ((this.serverId == null || servers.stream().noneMatch(v -> v.id().equals(this.serverId))) && !servers.isEmpty()) {
            // like the app: open the profile with the most accounts first
            this.serverId = servers.stream().max((a, b) -> Long.compare(count(accounts, a.id()), count(accounts, b.id()))).get().id();
            this.accountId = null;
        }
        List<AuroraClient.Account> shown = new ArrayList<>();
        for (AuroraClient.Account a : accounts) if (a.servers().contains(this.serverId)) shown.add(a);
        if (this.accountId != null && shown.stream().noneMatch(a -> a.id().equals(this.accountId))) this.accountId = null;
        int online = 0, total = 0;
        for (AuroraClient.Account a : accounts) for (String sv : a.servers()) { total++; if ("online".equals(st(sessions.get(a.id() + "@" + sv)))) online++; }

        float pw = Math.min(sw - 48 * u, 1280 * u), ph = Math.min(sh - 48 * u, 780 * u);
        float px = (sw - pw) / 2, py = (sh - ph) / 2;
        UiTheme.glass(ui, px, py, pw, ph, 22 * u, u, 2.4f);
        String sub = (this.api.keyName.isEmpty() ? "" : this.api.keyName + "  ·  ") + online + " of " + total + " online" + (this.api.reachable && this.api.live ? "  ·  live" : "  ·  reconnecting…");
        UiTheme.header(ui, px, py, pw, UiIcon.HEAD, "AuroraAFK", sub, u);
        this.closeButton(ui, px, py, pw, u, mx, my);
        float kb = 32 * u, kx = px + pw - 22 * u - 32 * u - 10 * u - kb, ky = py + (78 * u - kb) / 2;
        UiTheme.iconButton(ui, UiIcon.KEY, kx, ky, kb, this.anim("keybtn", inside(mx, my, kx, ky, kb, kb) ? 1 : 0, 16), u);
        this.hit("#changekey", kx, ky, kb, kb);
        if (inside(mx, my, kx, ky, kb, kb)) ui.textRight("Change key", kx - 10 * u, ky + (kb - 12 * u) / 2, 12 * u, 0, UiTheme.SUB);

        float top = py + 78 * u + 18 * u, bottom = py + ph - 20 * u, bodyH = bottom - top;
        float sideW = Math.max(190 * u, Math.min(250 * u, pw * 0.2f));
        float chatW = Math.max(280 * u, Math.min(400 * u, pw * 0.31f));
        float sx = px + 20 * u, mxL = sx + sideW + 16 * u, cx = px + pw - 20 * u - chatW, mainW = cx - 16 * u - mxL;

        // -- server profiles
        ui.text("SERVER PROFILES", sx + 4 * u, top, 11 * u, 1, UiTheme.FAINT);
        float y = top + 22 * u;
        if (servers.isEmpty()) ui.text(ui.ellipsize("No profiles on this key", sideW, 12 * u, 0), sx + 4 * u, y, 12 * u, 0, UiTheme.SUB);
        for (AuroraClient.Server v : servers) {
            float h = 60 * u;
            if (y + h > bottom) break;
            boolean sel = v.id().equals(this.serverId), hot = inside(mx, my, sx, y, sideW, h);
            UiTheme.card(ui, sx, y, sideW, h, 14 * u, this.anim("s" + v.id(), hot ? 1 : 0, 14), this.anim("ss" + v.id(), sel ? 1 : 0, 12), u);
            if (sel) ui.round(sx + 6 * u, y + 16 * u, 3 * u, h - 32 * u, 1.5f * u, UiPalette.accent(0.95f));
            long on = 0, all = 0;
            for (AuroraClient.Account a : accounts) if (a.servers().contains(v.id())) { all++; if ("online".equals(st(sessions.get(a.id() + "@" + v.id())))) on++; }
            String cnt = on + "/" + all;
            float cw = ui.textWidth(cnt, 11 * u, 1) + 14 * u;
            ui.round(sx + sideW - 12 * u - cw, y + 12 * u, cw, 20 * u, 10 * u, on > 0 ? UiEasing.mix(UiRenderer.white(0.05f), UiPalette.accent(0.18f), 0.7f) : UiRenderer.white(0.05f));
            ui.textCentered(cnt, sx + sideW - 12 * u - cw / 2, y + 16 * u, 11 * u, 1, on > 0 ? UiPalette.accent(1f) : UiTheme.SUB);
            ui.text(ui.ellipsize(v.name(), sideW - cw - 38 * u, 14 * u, 1), sx + 18 * u, y + 12 * u, 14 * u, 1, UiTheme.TEXT);
            ui.text(ui.ellipsize(v.address(), sideW - 30 * u, 11 * u, 0), sx + 18 * u, y + 34 * u, 11 * u, 0, UiTheme.SUB);
            this.hit("s:" + v.id(), sx, y, sideW, h);
            y += h + 8 * u;
        }

        // -- accounts of the profile
        AuroraClient.Server cur = servers.stream().filter(v -> v.id().equals(this.serverId)).findFirst().orElse(null);
        long curOn = shown.stream().filter(a -> "online".equals(st(sessions.get(a.id() + "@" + this.serverId)))).count();
        ui.text(ui.ellipsize(cur == null ? "AuroraAFK" : cur.name(), mainW * 0.5f, 20 * u, 1), mxL + 2 * u, top - 2 * u, 20 * u, 1, UiTheme.TEXT);
        ui.text(shown.size() + (shown.size() == 1 ? " account" : " accounts") + "  ·  " + curOn + " online", mxL + 2 * u, top + 24 * u, 12 * u, 0, UiTheme.SUB);
        if (this.api.canControl && cur != null) {
            float bw = 128 * u, bh = 34 * u;
            this.button(ui, "#stopall", "Disconnect all", mxL + mainW - bw, top, bw, bh, false, curOn > 0, u, mx, my);
            this.button(ui, "#startall", "Connect all", mxL + mainW - 2 * bw - 10 * u, top, bw, bh, true, curOn < shown.size(), u, mx, my);
        }
        float ly = top + 52 * u, lh = bottom - ly;
        this.listArea = new float[]{mxL, ly, mainW, lh};
        float rowH = 56 * u, gap = 6 * u;
        this.listMax = Math.max(0, shown.size() * (rowH + gap) - gap - lh);
        this.listScrollTarget = UiEasing.clamp(this.listScrollTarget, 0, this.listMax);
        this.listScroll += (this.listScrollTarget - this.listScroll) * (1 - (float) Math.exp(-18 * this.dt));
        ui.pushClip(mxL - 4 * u, ly, mainW + 8 * u, lh);
        if (shown.isEmpty()) {
            ui.textCentered(cur == null ? "Your key has no server profiles yet." : "No accounts in this profile.", mxL + mainW / 2, ly + lh / 2 - 8 * u, 13 * u, 0, UiTheme.SUB);
        }
        float ry = ly - this.listScroll;
        for (AuroraClient.Account a : shown) {
            if (ry + rowH < ly) { ry += rowH + gap; continue; }
            if (ry > bottom) break;
            this.drawRow(ui, a, sessions.get(a.id() + "@" + this.serverId), mxL, ry, mainW, rowH, u, mx, my, inside(mx, my, mxL, ly, mainW, lh));
            ry += rowH + gap;
        }
        ui.popClip();
        if (this.listMax > 0) {
            float tr = lh * lh / (lh + this.listMax), tY = ly + (lh - tr) * (this.listScroll / this.listMax);
            ui.round(mxL + mainW + 5 * u, tY, 3 * u, tr, 1.5f * u, UiRenderer.white(0.14f));
        }

        // -- chat
        this.drawChat(ui, shown, cx, top, chatW, bodyH, u, mx, my);
    }

    private void drawRow(UiRenderer ui, AuroraClient.Account a, AuroraClient.Session s, float x, float y, float w, float h, float u, double mx, double my, boolean inList) {
        String status = st(s);
        boolean sel = a.id().equals(this.accountId), hot = inList && inside(mx, my, x, y, w, h);
        UiTheme.card(ui, x, y, w, h, 14 * u, this.anim("r" + a.id(), hot ? 1 : 0, 14), this.anim("rs" + a.id(), sel ? 1 : 0, 12), u);
        float hs = 32 * u, hx = x + 14 * u, hy = y + (h - hs) / 2;
        UiTextureData head = this.api.heads.get(a.id());
        ui.round(hx - u, hy - u, hs + 2 * u, hs + 2 * u, 7 * u, UiRenderer.white(0.06f));
        if (head != null) ui.texture(head, hx, hy, hs, hs, "online".equals(status) ? 0xFFFFFFFF : UiRenderer.argb(1f, 150, 150, 156));
        else ui.icon(UiIcon.USER, hx + 6 * u, hy + 6 * u, hs - 12 * u, UiTheme.FAINT);
        int col = colorOf(status);
        ui.circle(hx + hs - u, hy + hs - u, 4.5f * u, UiPalette.glass(1f));
        ui.circle(hx + hs - u, hy + hs - u, 3.2f * u, col);

        float bw = this.api.canControl ? 112 * u : 0, tx = hx + hs + 14 * u, textW = w - (tx - x) - bw - 26 * u;
        float nameW = ui.textWidth(a.name(), 14 * u, 1);
        ui.text(ui.ellipsize(a.name(), textW, 14 * u, 1), tx, y + 11 * u, 14 * u, 1, UiTheme.TEXT);
        if (a.pinned() && nameW + 26 * u < textW) ui.circle(tx + nameW + 10 * u, y + 18 * u, 2.4f * u, UiPalette.accent(0.8f));
        String line = label(status);
        if (s != null && "online".equals(status) && s.health() >= 0) line += "  ·  Health " + Math.round(s.health()) + "  ·  Food " + Math.round(s.food()) + (s.pos().isEmpty() ? "" : "  ·  " + s.pos());
        else if (s != null && !s.reason().isEmpty() && !"online".equals(status)) line += "  ·  " + s.reason();
        float lw = ui.textWidth(label(status), 11.5f * u, 1);
        ui.text(label(status), tx, y + 32 * u, 11.5f * u, 1, col);
        String rest = line.substring(label(status).length());
        if (!rest.isEmpty()) ui.text(ui.ellipsize(rest, textW - lw, 11.5f * u, 0), tx + lw, y + 32 * u, 11.5f * u, 0, UiTheme.SUB);

        if (this.api.canControl) {
            boolean up = !"offline".equals(status) && !"error".equals(status) && !status.isEmpty();
            boolean wait = this.api.pending.containsKey((up ? "stop" : "start") + a.id() + this.serverId);
            float bx = x + w - 14 * u - bw, by = y + (h - 32 * u) / 2;
            this.button(ui, (up ? "stop:" : "start:") + a.id(), wait ? "…" : up ? "Disconnect" : "Connect", bx, by, bw, 32 * u, !up, !wait, u, mx, my);
        }
        this.hit("a:" + a.id(), x, y, w, h);
    }

    private void drawChat(UiRenderer ui, List<AuroraClient.Account> shown, float x, float y, float w, float h, float u, double mx, double my) {
        UiTheme.card(ui, x, y, w, h, 16 * u, 0f, 0f, u);
        AuroraClient.Account target = shown.stream().filter(a -> a.id().equals(this.accountId)).findFirst().orElse(null);
        ui.text("Chat", x + 18 * u, y + 14 * u, 16 * u, 1, UiTheme.TEXT);
        String who = target == null ? "All accounts  ·  click an account to chat as it" : "as " + target.name();
        ui.text(ui.ellipsize(who, w - 36 * u, 11.5f * u, 0), x + 18 * u, y + 36 * u, 11.5f * u, 0, UiTheme.SUB);
        if (target != null) {
            float cb = 24 * u, bx = x + w - 14 * u - cb, by = y + 14 * u;
            UiTheme.iconButton(ui, UiIcon.CLOSE, bx, by, cb, this.anim("chatall", inside(mx, my, bx, by, cb, cb) ? 1 : 0, 16), u);
            this.hit("#chatall", bx, by, cb, cb);
        }
        ui.rect(x + 14 * u, y + 58 * u, w - 28 * u, Math.max(1f, u), UiRenderer.white(0.055f));

        Map<String, String> names = new HashMap<>();
        for (AuroraClient.Account a : this.api.accounts) names.put(a.id(), a.name());
        List<AuroraClient.Log> logs;
        synchronized (this.api.logs) {
            logs = new ArrayList<>();
            for (AuroraClient.Log l : this.api.logs) {
                if (this.serverId == null || !this.serverId.equals(l.serverId())) continue;
                if (target != null && !target.id().equals(l.accountId())) continue;
                logs.add(l);
            }
        }
        float inH = 40 * u, ly = y + 66 * u, lh = h - 66 * u - inH - 26 * u, ts = 12 * u, lineH = 17 * u, tw = w - 36 * u;
        this.chatArea = new float[]{x, ly, w, lh};
        // build wrapped lines (newest at the bottom)
        List<Object[]> lines = new ArrayList<>();
        for (AuroraClient.Log l : logs) {
            String prefix = target == null && names.containsKey(l.accountId()) ? names.get(l.accountId()) + "  " : "";
            List<String> parts = wrap(ui, prefix + l.message(), tw, ts, 0);
            int col = "error".equals(l.level()) ? RED : "warning".equals(l.level()) ? AMBER : "chat".equals(l.level()) ? UiTheme.TEXT : UiTheme.SUB;
            for (int i = 0; i < parts.size(); i++) lines.add(new Object[]{parts.get(i), col, i == 0 ? prefix : ""});
        }
        float content = lines.size() * lineH;
        this.chatMax = Math.max(0, content - lh);
        this.chatScrollTarget = UiEasing.clamp(this.chatScrollTarget, 0, this.chatMax);
        this.chatScroll += (this.chatScrollTarget - this.chatScroll) * (1 - (float) Math.exp(-18 * this.dt));
        ui.pushClip(x, ly, w, lh);
        if (lines.isEmpty()) ui.textCentered("No messages yet", x + w / 2, ly + lh / 2 - 8 * u, 12 * u, 0, UiTheme.FAINT);
        float base = ly + lh - content + this.chatScroll; // chatScroll = how far up from the newest
        for (int i = 0; i < lines.size(); i++) {
            float ty = base + i * lineH;
            if (ty + lineH < ly || ty > ly + lh) continue;
            Object[] ln = lines.get(i);
            String text = (String) ln[0], prefix = (String) ln[2];
            if (!prefix.isEmpty() && text.startsWith(prefix)) {
                float pw = ui.text(prefix, x + 18 * u, ty, ts, 1, UiPalette.accent(0.95f));
                ui.text(text.substring(prefix.length()), x + 18 * u + ui.textWidth(prefix, ts, 1), ty, ts, 0, (int) ln[1]);
            } else ui.text(text, x + 18 * u, ty, ts, 0, (int) ln[1]);
        }
        ui.popClip();

        // input
        float ix = x + 14 * u, iy = y + h - 14 * u - inH, iw = w - 28 * u;
        boolean can = this.api.canChat && target != null;
        boolean hot = can && inside(mx, my, ix, iy, iw, inH);
        UiTheme.card(ui, ix, iy, iw, inH, 12 * u, this.anim("chatin", hot || this.chatFocus ? 1 : 0, 14), this.chatFocus ? 1f : 0f, u);
        String ph = !this.api.canChat ? "Your key can not chat" : target == null ? "Select an account to chat" : "Message or /command as " + target.name();
        float its = 12.5f * u;
        if (this.chatInput.isEmpty()) ui.text(ui.ellipsize(ph, iw - 28 * u, its, 0), ix + 14 * u, iy + (inH - its) / 2 - u, its, 0, UiTheme.FAINT);
        else {
            String shownText = this.chatInput;
            while (shownText.length() > 1 && ui.textWidth(shownText, its, 0) > iw - 30 * u) shownText = shownText.substring(1);
            ui.text(shownText, ix + 14 * u, iy + (inH - its) / 2 - u, its, 0, UiTheme.TEXT);
        }
        if (this.chatFocus && can && (System.currentTimeMillis() / 530) % 2 == 0) {
            String shownText = this.chatInput;
            while (shownText.length() > 1 && ui.textWidth(shownText, its, 0) > iw - 30 * u) shownText = shownText.substring(1);
            float cxp = ix + 14 * u + (this.chatInput.isEmpty() ? 0 : ui.textWidth(shownText, its, 0) + u);
            ui.rect(cxp, iy + inH / 2 - 8 * u, Math.max(1f, 1.4f * u), 16 * u, UiPalette.accent(0.9f));
        }
        if (can) this.hit("#chatin", ix, iy, iw, inH);
    }

    private void drawToast(UiRenderer ui, int sw, int sh, float u) {
        long age = System.currentTimeMillis() - this.api.toastAt;
        if (this.api.toast.isEmpty() || age > 3500) return;
        float a = Math.min(1f, Math.min(age / 150f, (3500 - age) / 300f));
        float ts = 13 * u, w = ui.textWidth(this.api.toast, ts, 0) + 40 * u, h = 38 * u, x = (sw - w) / 2, y = sh - 70 * u;
        ui.pushAlpha(a);
        ui.shadow(x, y, w, h, 12 * u, 16 * u, UiRenderer.argb(0.4f, 0, 0, 0));
        ui.round(x, y, w, h, 12 * u, UiPalette.glass(0.95f));
        ui.outline(x, y, w, h, 12 * u, Math.max(1f, u), UiRenderer.white(0.09f));
        ui.circle(x + 16 * u, y + h / 2, 3 * u, RED);
        ui.text(this.api.toast, x + 26 * u, y + (h - ts) / 2 - u, ts, 0, UiTheme.TEXT);
        ui.popAlpha();
    }

    // ---- helpers ------------------------------------------------------------------------------------

    private static long count(List<AuroraClient.Account> accounts, String serverId) {
        return accounts.stream().filter(a -> a.servers().contains(serverId)).count();
    }

    private static String st(AuroraClient.Session s) {
        return s == null ? "offline" : s.status() == null || s.status().isEmpty() ? "offline" : s.status();
    }

    private static int colorOf(String status) {
        return switch (status) {
            case "online" -> GREEN;
            case "connecting", "authenticating", "reconnecting", "waiting" -> AMBER;
            case "error", "banned", "kicked" -> RED;
            default -> UiTheme.FAINT;
        };
    }

    private static String label(String status) {
        return switch (status) {
            case "online" -> "Online";
            case "connecting" -> "Connecting";
            case "authenticating" -> "Signing in";
            case "reconnecting" -> "Reconnecting";
            case "waiting" -> "Waiting";
            case "error" -> "Error";
            case "offline", "" -> "Offline";
            default -> Character.toUpperCase(status.charAt(0)) + status.substring(1);
        };
    }

    static List<String> wrap(UiRenderer ui, String text, float max, float size, int weight) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String next = line.length() == 0 ? word : line + " " + word;
            if (ui.textWidth(next, size, weight) <= max) { line.setLength(0); line.append(next); continue; }
            if (line.length() > 0) { out.add(line.toString()); line.setLength(0); }
            // words longer than a line are cut
            String w = word;
            while (ui.textWidth(w, size, weight) > max && w.length() > 1) {
                int cut = w.length() - 1;
                while (cut > 1 && ui.textWidth(w.substring(0, cut), size, weight) > max) cut--;
                out.add(w.substring(0, cut));
                w = w.substring(cut);
            }
            line.append(w);
        }
        if (line.length() > 0 || out.isEmpty()) out.add(line.toString());
        return out;
    }

    // ------------------------------------------------------------------ input

    private void close() {
        if (this.closingAt >= 0) return;
        this.closingAt = System.nanoTime();
        cue(UiAudio.Cue.GUI_CLOSE);
    }

    private void unlock() {
        if (this.keyInput.isBlank() || this.api.status == AuroraClient.Status.CHECKING) return;
        cue(UiAudio.Cue.TICK);
        this.api.submitKey(this.keyInput);
    }

    private void sendChat() {
        String text = this.chatInput.trim();
        if (text.isEmpty() || this.accountId == null || !this.api.canChat) return;
        this.api.action("chat", this.accountId, this.serverId, text);
        this.chatInput = "";
        this.chatScrollTarget = 0;
        cue(UiAudio.Cue.TICK);
    }

    @Override
    public void method_25420(class_332 ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        if (click.method_74245() != 0 || this.closingAt >= 0) return true;
        double s = this.scale();
        double x = click.comp_4798() * s, y = click.comp_4799() * s;
        boolean wasFocus = this.chatFocus;
        this.chatFocus = false;
        for (int i = this.hits.size() - 1; i >= 0; i--) {
            float[] r = this.hits.get(i);
            if (!inside(x, y, r[0], r[1], r[2], r[3])) continue;
            String k = this.hitKeys.get(i);
            if (k.startsWith("a:") && !inside(x, y, this.listArea[0], this.listArea[1], this.listArea[2], this.listArea[3])) continue;
            this.click(k, wasFocus);
            return true;
        }
        return true;
    }

    private void click(String k, boolean wasFocus) {
        switch (k) {
            case "#close" -> this.close();
            case "#paste" -> {
                String clip = class_310.method_1551().field_1774.method_1460();
                if (clip != null) this.keyInput = AuroraClient.normalize(clip.length() > 80 ? clip.substring(0, 80) : clip);
                cue(UiAudio.Cue.TICK);
            }
            case "#unlock" -> this.unlock();
            case "#changekey" -> { this.api.changeKey(); this.keyInput = ""; cue(UiAudio.Cue.TICK); }
            case "#chatin" -> this.chatFocus = true;
            case "#chatall" -> { this.accountId = null; this.chatScrollTarget = 0; cue(UiAudio.Cue.TICK); }
            case "#startall", "#stopall" -> {
                boolean start = k.equals("#startall");
                for (AuroraClient.Account a : this.api.accounts) {
                    if (!a.servers().contains(this.serverId)) continue;
                    String state = st(this.api.sessions.get(a.id() + "@" + this.serverId));
                    boolean up = !"offline".equals(state) && !"error".equals(state);
                    if (start != up) this.api.action(start ? "start" : "stop", a.id(), this.serverId, null);
                }
                cue(UiAudio.Cue.TICK);
            }
            default -> {
                if (k.startsWith("s:")) {
                    String id = k.substring(2);
                    if (!id.equals(this.serverId)) { this.serverId = id; this.accountId = null; this.listScrollTarget = this.listScroll = 0; this.chatScrollTarget = this.chatScroll = 0; }
                    cue(UiAudio.Cue.TICK);
                } else if (k.startsWith("a:")) {
                    String id = k.substring(2);
                    this.accountId = id.equals(this.accountId) ? null : id;
                    this.chatScrollTarget = this.chatScroll = 0;
                    this.chatFocus = this.accountId != null && this.api.canChat;
                    cue(UiAudio.Cue.TICK);
                } else if (k.startsWith("start:") || k.startsWith("stop:")) {
                    int c = k.indexOf(':');
                    this.api.action(k.substring(0, c), k.substring(c + 1), this.serverId, null);
                    this.chatFocus = wasFocus;
                    cue(UiAudio.Cue.TICK);
                }
            }
        }
    }

    @Override
    public boolean method_25401(double x, double y, double h, double v) {
        double s = this.scale();
        double px = x * s, py = y * s;
        float step = 70 * UiTheme.unit(class_310.method_1551().method_22683().method_4489(), class_310.method_1551().method_22683().method_4506());
        if (inside(px, py, this.chatArea[0], this.chatArea[1], this.chatArea[2], this.chatArea[3])) this.chatScrollTarget = UiEasing.clamp(this.chatScrollTarget + (float) v * step, 0, this.chatMax);
        else this.listScrollTarget = UiEasing.clamp(this.listScrollTarget - (float) v * step, 0, this.listMax);
        return true;
    }

    @Override
    public boolean method_25404(class_11908 key) {
        int code = key.comp_4795();
        boolean ctrl = (key.comp_4797() & 2) != 0;
        boolean keyMode = this.api.status != AuroraClient.Status.READY;
        if (code == 256) {
            if (this.chatFocus) this.chatFocus = false;
            else this.close();
            return true;
        }
        if (keyMode) {
            if (code == 257 || code == 335) this.unlock();
            else if (code == 259 && !this.keyInput.isEmpty()) this.keyInput = ctrl ? "" : this.keyInput.substring(0, this.keyInput.length() - 1);
            else if (code == 86 && ctrl) this.click("#paste", false);
            return true;
        }
        if (this.chatFocus) {
            if (code == 257 || code == 335) this.sendChat();
            else if (code == 259 && !this.chatInput.isEmpty()) this.chatInput = ctrl ? "" : this.chatInput.substring(0, this.chatInput.length() - 1);
            else if (code == 86 && ctrl) {
                String clip = class_310.method_1551().field_1774.method_1460();
                if (clip != null) this.chatInput = (this.chatInput + clip.replaceAll("[\\r\\n\\t]", " ")).substring(0, Math.min(256, this.chatInput.length() + clip.length()));
            }
            return true;
        }
        if (code == 257 || code == 335 || code == 84) { if (this.accountId != null && this.api.canChat) this.chatFocus = true; }
        return true;
    }

    @Override
    public boolean method_25400(class_11905 input) {
        if (!input.method_74227()) return true;
        char c = (char) input.comp_4793();
        if (this.api.status != AuroraClient.Status.READY) {
            if (this.api.status == AuroraClient.Status.CHECKING) return true;
            char up = Character.toUpperCase(c);
            if (((up >= 'A' && up <= 'Z') || (up >= '0' && up <= '9') || up == '-') && this.keyInput.length() < 40) this.keyInput += up;
            return true;
        }
        if (this.chatFocus && c >= 32 && c != 127 && this.chatInput.length() < 256) this.chatInput += c;
        return true;
    }

    // test hooks (AuroraTest only)
    void testType(String text) { this.keyInput = AuroraClient.normalize(text); }

    void testClick(String key) { this.click(key, false); }

    void testSelectFirst() {
        for (AuroraClient.Account a : this.api.accounts) if (a.servers().contains(this.serverId)) { this.click("a:" + a.id(), false); return; }
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
