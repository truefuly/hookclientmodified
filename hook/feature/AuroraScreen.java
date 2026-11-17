package hook.feature;

import com.google.gson.JsonObject;
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
 * AuroraAFK inside Minecraft, built like the AuroraAFK app: server profiles on the left (only the ones where accounts
 * are or were connected), and for the selected profile the tabs Connect, Chat and Actions. Drawn in the HookVisuals
 * glass style and theme colour. The first time it asks for a key; the key is checked by the AuroraAFK VPS and then
 * stored encrypted on this PC. While open it refreshes from the VPS every 7 seconds (and right after every action).
 */
public final class AuroraScreen extends class_437 {
    private static final int GREEN = UiRenderer.argb(1f, 92, 214, 140), AMBER = UiRenderer.argb(1f, 240, 184, 76), RED = UiRenderer.argb(1f, 240, 96, 96);
    private static final String[][] TABS = {{"connect", "Connect"}, {"chat", "Chat"}, {"actions", "Actions"}};
    private static final String[][] SECTIONS = {{"rewards", "Rewards"}, {"homes", "Homes"}, {"shops", "Shops"}, {"selling", "Selling"}, {"movement", "Movement"}};

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
    private static String tab = "connect", section = "rewards";
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

    /** On/off switch like the client's module toggles. */
    private void toggle(UiRenderer ui, String id, boolean on, float x, float y, float u, boolean enabled, double mx, double my) {
        float w = 40 * u, h = 22 * u;
        float t = this.anim("t" + id, on ? 1 : 0, 14);
        ui.round(x, y, w, h, h / 2, UiEasing.mix(UiRenderer.white(enabled ? 0.12f : 0.06f), UiPalette.accent(enabled ? 0.85f : 0.35f), t));
        float k = h - 6 * u;
        ui.circle(x + 3 * u + k / 2 + (w - 6 * u - k) * t, y + h / 2, k / 2, UiRenderer.white(enabled ? 0.95f : 0.5f));
        if (enabled) this.hit(id, x - 4 * u, y - 4 * u, w + 8 * u, h + 8 * u);
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

        float fx = px + pad, fw = pw - 2 * pad, fh = 54 * u;
        boolean hasText = !this.keyInput.isEmpty();
        UiTheme.card(ui, fx, y, fw, fh, 14 * u, 0f, 1f, u);
        UiTheme.accentDot(ui, fx + 20 * u, y + fh / 2, u, checking ? 1f : 0.6f);
        ui.icon(UiIcon.KEY, fx + fw - 34 * u, y + (fh - 18 * u) / 2, 18 * u, UiTheme.FAINT);
        float ts = 17 * u;
        String shown = ui.ellipsize(hasText ? this.keyInput : "HV-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX", fw - 90 * u, ts, 1);
        ui.text(shown, fx + 38 * u, y + (fh - ts) / 2 - u, ts, 1, hasText ? UiTheme.TEXT : UiTheme.FAINT);
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

    /** Only profiles where accounts are or were connected, like the user asked; the open one always stays. */
    private List<AuroraClient.Server> visibleServers() {
        List<AuroraClient.Server> out = new ArrayList<>();
        java.util.Set<String> active = this.api.activeServers;
        for (AuroraClient.Server v : this.api.servers) if (active.contains(v.id())) out.add(v);
        return out;
    }

    private void drawPanel(UiRenderer ui, int sw, int sh, float u, double mx, double my) {
        List<AuroraClient.Server> servers = this.visibleServers();
        List<AuroraClient.Account> accounts = this.api.accounts;
        Map<String, AuroraClient.Session> sessions = this.api.sessions;
        if ((this.serverId == null || servers.stream().noneMatch(v -> v.id().equals(this.serverId)))) {
            this.serverId = servers.stream().max((a, b) -> Long.compare(onlineOn(accounts, sessions, a.id()), onlineOn(accounts, sessions, b.id()))).map(AuroraClient.Server::id).orElse(null);
            this.accountId = null;
        }
        List<AuroraClient.Account> shown = new ArrayList<>();
        for (AuroraClient.Account a : accounts) if (a.servers().contains(this.serverId)) shown.add(a);
        if (this.accountId != null && shown.stream().noneMatch(a -> a.id().equals(this.accountId))) this.accountId = null;
        int online = 0, total = 0;
        for (AuroraClient.Server v : servers) for (AuroraClient.Account a : accounts) if (a.servers().contains(v.id())) { total++; if ("online".equals(st(sessions.get(a.id() + "@" + v.id())))) online++; }

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

        float top = py + 78 * u + 18 * u, bottom = py + ph - 20 * u;
        float sideW = Math.max(190 * u, Math.min(250 * u, pw * 0.2f));
        float sx = px + 20 * u, cx = sx + sideW + 18 * u, cw = px + pw - 20 * u - cx;

        // -- server profiles (only where accounts are or were online)
        ui.text("SERVER PROFILES", sx + 4 * u, top, 11 * u, 1, UiTheme.FAINT);
        float y = top + 22 * u;
        if (servers.isEmpty()) {
            for (String line : wrap(ui, "No accounts are connected anywhere right now. Profiles show up here as soon as an account joins.", sideW - 8 * u, 12 * u, 0)) {
                ui.text(line, sx + 4 * u, y, 12 * u, 0, UiTheme.SUB);
                y += 17 * u;
            }
        }
        for (AuroraClient.Server v : servers) {
            float h = 60 * u;
            if (y + h > bottom) break;
            boolean sel = v.id().equals(this.serverId), hot = inside(mx, my, sx, y, sideW, h);
            UiTheme.card(ui, sx, y, sideW, h, 14 * u, this.anim("s" + v.id(), hot ? 1 : 0, 14), this.anim("ss" + v.id(), sel ? 1 : 0, 12), u);
            if (sel) ui.round(sx + 6 * u, y + 16 * u, 3 * u, h - 32 * u, 1.5f * u, UiPalette.accent(0.95f));
            long on = onlineOn(accounts, sessions, v.id()), all = accounts.stream().filter(a -> a.servers().contains(v.id())).count();
            String cnt = on + "/" + all;
            float cwid = ui.textWidth(cnt, 11 * u, 1) + 14 * u;
            ui.round(sx + sideW - 12 * u - cwid, y + 12 * u, cwid, 20 * u, 10 * u, on > 0 ? UiEasing.mix(UiRenderer.white(0.05f), UiPalette.accent(0.18f), 0.7f) : UiRenderer.white(0.05f));
            ui.textCentered(cnt, sx + sideW - 12 * u - cwid / 2, y + 16 * u, 11 * u, 1, on > 0 ? UiPalette.accent(1f) : UiTheme.SUB);
            ui.text(ui.ellipsize(v.name(), sideW - cwid - 38 * u, 14 * u, 1), sx + 18 * u, y + 12 * u, 14 * u, 1, UiTheme.TEXT);
            ui.text(ui.ellipsize(v.address(), sideW - 30 * u, 11 * u, 0), sx + 18 * u, y + 34 * u, 11 * u, 0, UiTheme.SUB);
            this.hit("s:" + v.id(), sx, y, sideW, h);
            y += h + 8 * u;
        }

        AuroraClient.Server cur = servers.stream().filter(v -> v.id().equals(this.serverId)).findFirst().orElse(null);
        if (cur == null) {
            ui.textCentered("Nothing to show yet", cx + cw / 2, top + (bottom - top) / 2 - 10 * u, 15 * u, 1, UiTheme.SUB);
            return;
        }
        long curOn = onlineOn(accounts, sessions, this.serverId);
        ui.text(ui.ellipsize(cur.name(), cw * 0.5f, 20 * u, 1), cx + 2 * u, top - 2 * u, 20 * u, 1, UiTheme.TEXT);
        ui.text(shown.size() + (shown.size() == 1 ? " account" : " accounts") + "  ·  " + curOn + " online", cx + 2 * u, top + 24 * u, 12 * u, 0, UiTheme.SUB);

        // -- tabs like the app
        float ty = top + 50 * u, th = 36 * u, tx = cx;
        for (String[] t : TABS) {
            UiIcon icon = t[0].equals("connect") ? UiIcon.SIGNAL : t[0].equals("chat") ? UiIcon.CHAT : UiIcon.BOLT;
            float tw = 30 * u + 18 * u + ui.textWidth(t[1], 13 * u, 1) + 6 * u;
            boolean sel = tab.equals(t[0]), hot = inside(mx, my, tx, ty, tw, th);
            float hv = this.anim("tab" + t[0], hot ? 1 : 0, 14), sv = this.anim("tabs" + t[0], sel ? 1 : 0, 12);
            UiTheme.card(ui, tx, ty, tw, th, 11 * u, hv, sv, u);
            if (sv > 0.01f) ui.round(tx, ty, tw, th, 11 * u, UiPalette.accent(0.12f * sv));
            int col = UiEasing.mix(UiTheme.SUB, UiPalette.accent(1f), sv);
            ui.icon(icon, tx + 14 * u, ty + (th - 16 * u) / 2, 16 * u, col);
            ui.text(t[1], tx + 38 * u, ty + (th - 13 * u) / 2 - u, 13 * u, 1, UiEasing.mix(UiTheme.SUB, UiTheme.TEXT, Math.max(sv, hv)));
            this.hit("tab:" + t[0], tx, ty, tw, th);
            tx += tw + 8 * u;
        }
        if (tab.equals("connect") && this.api.canControl) {
            float bw = 128 * u, bh = th;
            this.button(ui, "#stopall", "Disconnect all", cx + cw - bw, ty, bw, bh, false, curOn > 0, u, mx, my);
            this.button(ui, "#startall", "Connect all", cx + cw - 2 * bw - 10 * u, ty, bw, bh, true, curOn < shown.size(), u, mx, my);
        }
        float by = ty + th + 14 * u, bh = bottom - by;
        switch (tab) {
            case "chat" -> {
                float lw = Math.min(280 * u, cw * 0.34f);
                this.drawPicker(ui, shown, sessions, cx, by, lw, bh, true, u, mx, my);
                this.drawChat(ui, shown, cx + lw + 14 * u, by, cw - lw - 14 * u, bh, u, mx, my);
            }
            case "actions" -> {
                float lw = Math.min(280 * u, cw * 0.34f);
                if (this.accountId == null && !shown.isEmpty()) this.accountId = shown.stream().filter(a -> "online".equals(st(sessions.get(a.id() + "@" + this.serverId)))).findFirst().orElse(shown.get(0)).id();
                this.drawPicker(ui, shown, sessions, cx, by, lw, bh, false, u, mx, my);
                this.drawActions(ui, shown, sessions, cx + lw + 14 * u, by, cw - lw - 14 * u, bh, u, mx, my);
            }
            default -> this.drawConnect(ui, shown, sessions, cx, by, cw, bh, u, mx, my);
        }
    }

    private void scrollList(float viewH, float content) {
        this.listMax = Math.max(0, content - viewH);
        this.listScrollTarget = UiEasing.clamp(this.listScrollTarget, 0, this.listMax);
        this.listScroll += (this.listScrollTarget - this.listScroll) * (1 - (float) Math.exp(-18 * this.dt));
    }

    private void scrollbar(UiRenderer ui, float x, float y, float h, float u) {
        if (this.listMax <= 0) return;
        float tr = h * h / (h + this.listMax), tY = y + (h - tr) * (this.listScroll / this.listMax);
        ui.round(x, tY, 3 * u, tr, 1.5f * u, UiRenderer.white(0.14f));
    }

    // ---- Connect tab: every account of the profile with connect / disconnect (two columns when wide)
    private void drawConnect(UiRenderer ui, List<AuroraClient.Account> shown, Map<String, AuroraClient.Session> sessions, float x, float y, float w, float h, float u, double mx, double my) {
        int cols = w >= 760 * u ? 2 : 1;
        float gap = 8 * u, rowH = 58 * u, colW = (w - (cols - 1) * 10 * u) / cols;
        int rows = (shown.size() + cols - 1) / cols;
        this.listArea = new float[]{x, y, w, h};
        this.scrollList(h, rows * (rowH + gap) - gap);
        ui.pushClip(x - 4 * u, y, w + 8 * u, h);
        if (shown.isEmpty()) ui.textCentered("No accounts in this profile.", x + w / 2, y + h / 2 - 8 * u, 13 * u, 0, UiTheme.SUB);
        boolean inList = inside(mx, my, x, y, w, h);
        for (int i = 0; i < shown.size(); i++) {
            float rx = x + (i % cols) * (colW + 10 * u), ry = y + (i / cols) * (rowH + gap) - this.listScroll;
            if (ry + rowH < y || ry > y + h) continue;
            AuroraClient.Account a = shown.get(i);
            this.drawRow(ui, a, sessions.get(a.id() + "@" + this.serverId), rx, ry, colW, rowH, u, mx, my, inList);
        }
        ui.popClip();
        this.scrollbar(ui, x + w + 5 * u, y, h, u);
    }

    private void drawRow(UiRenderer ui, AuroraClient.Account a, AuroraClient.Session s, float x, float y, float w, float h, float u, double mx, double my, boolean inList) {
        String status = st(s);
        boolean hot = inList && inside(mx, my, x, y, w, h);
        UiTheme.card(ui, x, y, w, h, 14 * u, this.anim("r" + a.id(), hot ? 1 : 0, 14), 0f, u);
        float hs = 32 * u, hx = x + 14 * u, hy = y + (h - hs) / 2;
        this.head(ui, a, status, hx, hy, hs, u);
        int col = colorOf(status);
        float bw = this.api.canControl ? 108 * u : 0, tx = hx + hs + 14 * u, textW = w - (tx - x) - bw - 26 * u;
        float nameW = ui.textWidth(a.name(), 14 * u, 1);
        ui.text(ui.ellipsize(a.name(), textW, 14 * u, 1), tx, y + 11 * u, 14 * u, 1, UiTheme.TEXT);
        if (a.pinned() && nameW + 26 * u < textW) ui.circle(tx + nameW + 10 * u, y + 18 * u, 2.4f * u, UiPalette.accent(0.8f));
        String lab = label(status), rest = "";
        if (s != null && "online".equals(status) && s.health() >= 0) rest = "  ·  Health " + Math.round(s.health()) + "  ·  Food " + Math.round(s.food()) + (s.money().isEmpty() ? "" : "  ·  " + s.money());
        else if (s != null && !s.reason().isEmpty() && !"online".equals(status)) rest = "  ·  " + s.reason();
        float lw = ui.textWidth(lab, 11.5f * u, 1);
        ui.text(lab, tx, y + 32 * u, 11.5f * u, 1, col);
        if (!rest.isEmpty()) ui.text(ui.ellipsize(rest, textW - lw, 11.5f * u, 0), tx + lw, y + 32 * u, 11.5f * u, 0, UiTheme.SUB);
        if (this.api.canControl) {
            boolean up = isUp(status);
            boolean wait = this.api.pending.containsKey((up ? "stop" : "start") + a.id() + this.serverId);
            this.button(ui, (up ? "stop:" : "start:") + a.id(), wait ? "…" : up ? "Disconnect" : "Connect", x + w - 14 * u - bw, y + (h - 32 * u) / 2, bw, 32 * u, !up, !wait, u, mx, my);
        }
    }

    private void head(UiRenderer ui, AuroraClient.Account a, String status, float x, float y, float s, float u) {
        UiTextureData head = this.api.heads.get(a.id());
        ui.round(x - u, y - u, s + 2 * u, s + 2 * u, 7 * u, UiRenderer.white(0.06f));
        if (head != null) ui.texture(head, x, y, s, s, "online".equals(status) ? 0xFFFFFFFF : UiRenderer.argb(1f, 150, 150, 156));
        else ui.icon(UiIcon.USER, x + s * 0.2f, y + s * 0.2f, s * 0.6f, UiTheme.FAINT);
        ui.circle(x + s - u, y + s - u, Math.max(3.5f * u, s * 0.14f), UiPalette.glass(1f));
        ui.circle(x + s - u, y + s - u, Math.max(2.5f * u, s * 0.1f), colorOf(status));
    }

    // ---- account picker (Chat and Actions tabs)
    private void drawPicker(UiRenderer ui, List<AuroraClient.Account> shown, Map<String, AuroraClient.Session> sessions, float x, float y, float w, float h, boolean allRow, float u, double mx, double my) {
        UiTheme.card(ui, x, y, w, h, 16 * u, 0f, 0f, u);
        float rowH = 46 * u, pad = 8 * u, iy = y + pad, ih = h - 2 * pad;
        int n = shown.size() + (allRow ? 1 : 0);
        this.listArea = new float[]{x, iy, w, ih};
        this.scrollList(ih, n * (rowH + 4 * u));
        ui.pushClip(x, iy, w, ih);
        boolean inList = inside(mx, my, x, iy, w, ih);
        float ry = iy - this.listScroll;
        if (allRow) {
            this.pickerRow(ui, null, "All accounts", "Write as every online account", UiTheme.SUB, x + pad, ry, w - 2 * pad, rowH, u, mx, my, inList, null);
            ry += rowH + 4 * u;
        }
        for (AuroraClient.Account a : shown) {
            if (ry + rowH >= iy && ry <= iy + ih) {
                AuroraClient.Session s = sessions.get(a.id() + "@" + this.serverId);
                String status = st(s);
                String info = label(status) + (s != null && !s.money().isEmpty() && "online".equals(status) ? "  ·  " + s.money() : "");
                this.pickerRow(ui, a, a.name(), info, colorOf(status), x + pad, ry, w - 2 * pad, rowH, u, mx, my, inList, status);
            }
            ry += rowH + 4 * u;
        }
        ui.popClip();
        this.scrollbar(ui, x + w - 6 * u, iy, ih, u);
    }

    private void pickerRow(UiRenderer ui, AuroraClient.Account a, String name, String info, int infoCol, float x, float y, float w, float h, float u, double mx, double my, boolean inList, String status) {
        String id = a == null ? "" : a.id();
        boolean sel = a == null ? this.accountId == null : a.id().equals(this.accountId);
        boolean hot = inList && inside(mx, my, x, y, w, h);
        float hv = this.anim("p" + id, hot ? 1 : 0, 14), sv = this.anim("ps" + id, sel ? 1 : 0, 12);
        if (hv > 0.01f || sv > 0.01f) ui.round(x, y, w, h, 11 * u, UiEasing.mix(UiRenderer.white(0.04f * Math.max(hv, sv)), UiPalette.accent(0.12f), sv));
        if (sv > 0.01f) ui.round(x + 2 * u, y + 12 * u, 3 * u, h - 24 * u, 1.5f * u, UiPalette.accent(0.95f * sv));
        float hs = 28 * u, hx = x + 12 * u, hy = y + (h - hs) / 2;
        if (a != null) this.head(ui, a, status, hx, hy, hs, u);
        else { ui.round(hx, hy, hs, hs, 7 * u, UiRenderer.white(0.06f)); ui.icon(UiIcon.CHAT, hx + 6 * u, hy + 6 * u, hs - 12 * u, UiTheme.SUB); }
        float tx = hx + hs + 12 * u, tw = w - (tx - x) - 8 * u;
        ui.text(ui.ellipsize(name, tw, 13 * u, 1), tx, y + 8 * u, 13 * u, 1, UiTheme.TEXT);
        ui.text(ui.ellipsize(info, tw, 11 * u, 0), tx + 0.5f * u, y + 26 * u, 11 * u, 0, infoCol);
        this.hit("p:" + id, x, y, w, h);
    }

    private List<String> onlineIds(List<AuroraClient.Account> shown) {
        List<String> ids = new ArrayList<>();
        for (AuroraClient.Account a : shown) if ("online".equals(st(this.api.sessions.get(a.id() + "@" + this.serverId)))) ids.add(a.id());
        return ids;
    }

    // ---- Chat tab
    private void drawChat(UiRenderer ui, List<AuroraClient.Account> shown, float x, float y, float w, float h, float u, double mx, double my) {
        UiTheme.card(ui, x, y, w, h, 16 * u, 0f, 0f, u);
        AuroraClient.Account target = shown.stream().filter(a -> a.id().equals(this.accountId)).findFirst().orElse(null);
        ui.text("Chat", x + 18 * u, y + 14 * u, 16 * u, 1, UiTheme.TEXT);
        int onlineCount = this.onlineIds(shown).size();
        String who = target == null ? "All accounts  ·  your message is sent from all " + onlineCount + " online accounts" : "as " + target.name();
        ui.text(ui.ellipsize(who, w - 36 * u, 11.5f * u, 0), x + 18 * u, y + 36 * u, 11.5f * u, 0, UiTheme.SUB);
        ui.rect(x + 14 * u, y + 58 * u, w - 28 * u, Math.max(1f, u), UiRenderer.white(0.055f));

        Map<String, String> names = new HashMap<>();
        for (AuroraClient.Account a : this.api.accounts) names.put(a.id(), a.name());
        List<AuroraClient.Log> logs = new ArrayList<>();
        synchronized (this.api.logs) {
            for (AuroraClient.Log l : this.api.logs) {
                if (this.serverId == null || !this.serverId.equals(l.serverId())) continue;
                if (target != null && !target.id().equals(l.accountId())) continue;
                logs.add(l);
            }
        }
        float inH = 40 * u, ly = y + 66 * u, lh = h - 66 * u - inH - 26 * u, ts = 12 * u, lineH = 17 * u, tw = w - 36 * u;
        this.chatArea = new float[]{x, ly, w, lh};
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
        float base = ly + lh - content + this.chatScroll;
        for (int i = 0; i < lines.size(); i++) {
            float ty = base + i * lineH;
            if (ty + lineH < ly || ty > ly + lh) continue;
            Object[] ln = lines.get(i);
            String text = (String) ln[0], prefix = (String) ln[2];
            if (!prefix.isEmpty() && text.startsWith(prefix)) {
                ui.text(prefix, x + 18 * u, ty, ts, 1, UiPalette.accent(0.95f));
                ui.text(text.substring(prefix.length()), x + 18 * u + ui.textWidth(prefix, ts, 1), ty, ts, 0, (int) ln[1]);
            } else ui.text(text, x + 18 * u, ty, ts, 0, (int) ln[1]);
        }
        ui.popClip();

        float ix = x + 14 * u, iy = y + h - 14 * u - inH, iw = w - 28 * u;
        boolean can = this.api.canChat && (target != null || onlineCount > 0);
        boolean sending = this.api.pending.containsKey("chatAll" + this.serverId);
        boolean hot = can && inside(mx, my, ix, iy, iw, inH);
        UiTheme.card(ui, ix, iy, iw, inH, 12 * u, this.anim("chatin", hot || this.chatFocus ? 1 : 0, 14), this.chatFocus ? 1f : 0f, u);
        String ph = !this.api.canChat ? "Your key can not chat" : sending ? "Sending to all accounts…" : target == null ? (onlineCount == 0 ? "No account is online" : "Message or /command for all " + onlineCount + " online accounts") : "Message or /command as " + target.name();
        float its = 12.5f * u;
        String shownText = this.chatInput;
        while (shownText.length() > 1 && ui.textWidth(shownText, its, 0) > iw - 30 * u) shownText = shownText.substring(1);
        if (this.chatInput.isEmpty()) ui.text(ui.ellipsize(ph, iw - 28 * u, its, 0), ix + 14 * u, iy + (inH - its) / 2 - u, its, 0, UiTheme.FAINT);
        else ui.text(shownText, ix + 14 * u, iy + (inH - its) / 2 - u, its, 0, UiTheme.TEXT);
        if (this.chatFocus && can && (System.currentTimeMillis() / 530) % 2 == 0) {
            float cxp = ix + 14 * u + (this.chatInput.isEmpty() ? 0 : ui.textWidth(shownText, its, 0) + u);
            ui.rect(cxp, iy + inH / 2 - 8 * u, Math.max(1f, 1.4f * u), 16 * u, UiPalette.accent(0.9f));
        }
        if (can) this.hit("#chatin", ix, iy, iw, inH);
    }

    // ---- Actions tab: like the app's Actions page (summary, categories, tiles)
    private void drawActions(UiRenderer ui, List<AuroraClient.Account> shown, Map<String, AuroraClient.Session> sessions, float x, float y, float w, float h, float u, double mx, double my) {
        UiTheme.card(ui, x, y, w, h, 16 * u, 0f, 0f, u);
        AuroraClient.Account a = shown.stream().filter(v -> v.id().equals(this.accountId)).findFirst().orElse(null);
        if (a == null) {
            ui.textCentered("Add accounts to this profile to use actions.", x + w / 2, y + h / 2 - 8 * u, 13 * u, 0, UiTheme.SUB);
            return;
        }
        AuroraClient.Session s = sessions.get(a.id() + "@" + this.serverId);
        String status = st(s);
        boolean online = "online".equals(status);
        JsonObject r = this.api.rewards.getOrDefault(a.id() + "@" + this.serverId, new JsonObject());
        float pad = 18 * u, ix = x + pad, iw = w - 2 * pad;

        // account line
        this.head(ui, a, status, ix, y + pad, 36 * u, u);
        ui.text(ui.ellipsize(a.name(), iw - 60 * u, 16 * u, 1), ix + 50 * u, y + pad + 1 * u, 16 * u, 1, UiTheme.TEXT);
        String line = label(status) + (s != null && s.busy() != null && !s.busy().isEmpty() ? "  ·  busy: " + s.busy() : "");
        ui.text(ui.ellipsize(line, iw - 60 * u, 11.5f * u, 0), ix + 50 * u, y + pad + 22 * u, 11.5f * u, 0, colorOf(status));

        // summary metrics
        float my0 = y + pad + 50 * u, mh = 58 * u, mg = 10 * u, mw = (iw - 3 * mg) / 4;
        String[][] metrics = {{"Balance", s == null ? "" : s.money()}, {"Gems", s == null ? "" : s.gems()}, {"Coins", s == null ? "" : s.coins()}, {"Reward XP", s == null ? "" : s.rewardXp()}};
        for (int i = 0; i < 4; i++) {
            float mx0 = ix + i * (mw + mg);
            UiTheme.card(ui, mx0, my0, mw, mh, 13 * u, 0f, i == 0 ? 1f : 0f, u);
            ui.text(metrics[i][0], mx0 + 14 * u, my0 + 11 * u, 11 * u, 0, UiTheme.SUB);
            String v = metrics[i][1].isEmpty() ? "—" : metrics[i][1];
            float vs = 17 * u;
            ui.text(ui.ellipsize(v, mw - 26 * u, vs, 1), mx0 + 14 * u, my0 + 29 * u, vs, 1, i == 0 && !metrics[i][1].isEmpty() ? UiPalette.accent(1f) : UiTheme.TEXT);
        }

        // categories
        float cy = my0 + mh + 14 * u, ch = 32 * u, cx = ix;
        for (String[] c : SECTIONS) {
            UiIcon icon = switch (c[0]) { case "rewards" -> UiIcon.GIFT; case "homes" -> UiIcon.HOME; case "shops" -> UiIcon.BOX; case "selling" -> UiIcon.RESET; default -> UiIcon.ACTIVITY; };
            float cwid = 26 * u + 14 * u + ui.textWidth(c[1], 12.5f * u, 1) + 4 * u;
            boolean sel = section.equals(c[0]), hot = inside(mx, my, cx, cy, cwid, ch);
            float hv = this.anim("sec" + c[0], hot ? 1 : 0, 14), sv = this.anim("secs" + c[0], sel ? 1 : 0, 12);
            ui.round(cx, cy, cwid, ch, ch / 2, UiEasing.mix(UiRenderer.white(0.035f + 0.04f * hv), UiPalette.accent(0.16f), sv));
            int col = UiEasing.mix(UiTheme.SUB, UiPalette.accent(1f), sv);
            ui.icon(icon, cx + 12 * u, cy + (ch - 14 * u) / 2, 14 * u, col);
            ui.text(c[1], cx + 32 * u, cy + (ch - 12.5f * u) / 2 - u, 12.5f * u, 1, UiEasing.mix(UiTheme.SUB, UiTheme.TEXT, Math.max(sv, hv)));
            this.hit("sec:" + c[0], cx, cy, cwid, ch);
            cx += cwid + 8 * u;
        }

        // tiles
        float ty = cy + ch + 14 * u, th = y + h - pad - ty;
        List<Tile> tiles = this.tiles(a, s, r, online);
        int cols = iw >= 560 * u ? 2 : 1;
        float tg = 10 * u, tw = (iw - (cols - 1) * tg) / cols, tileH = Math.min(150 * u, (th - tg) / 2);
        if (cols == 1) tileH = Math.min(140 * u, th / Math.max(1, tiles.size()) - tg);
        ui.pushClip(ix - 4 * u, ty, iw + 8 * u, th);
        for (int i = 0; i < tiles.size(); i++) {
            float tx0 = ix + (i % cols) * (tw + tg), ty0 = ty + (i / cols) * (tileH + tg);
            this.drawTile(ui, tiles.get(i), a, tx0, ty0, tw, tileH, u, mx, my);
        }
        ui.popClip();
    }

    /** One action card: icon, title, text, status line and its buttons / switch. */
    private record Tile(UiIcon icon, String title, String text, String status, String[][] buttons, String toggleType, boolean toggleOn, boolean toggleEnabled) {}

    private List<Tile> tiles(AuroraClient.Account a, AuroraClient.Session s, JsonObject r, boolean online) {
        boolean ctl = this.api.canControl, chat = this.api.canChat;
        String on = online && ctl ? "1" : "0", homes = online && chat ? "1" : "0";
        List<Tile> t = new ArrayList<>();
        switch (section) {
            case "homes" -> t.add(new Tile(UiIcon.HOME, "Home 1", "Save the current location, go back to it, or delete it.", online ? "Uses /sethome 1 and /home 1" : "Account is offline",
                new String[][]{{"setHome", "Set home", homes}, {"tpHome", "Teleport", homes}, {"deleteHome", "Delete", homes}}, null, false, false));
            case "shops" -> {
                String sp = this.api.shops.getOrDefault(this.serverId, "cow");
                t.add(new Tile(UiIcon.BOX, "AutoSpawner Buy", "Buys " + cap(sp) + " spawners until the shop closes (type is set in the app).", str(r, "spawnerStatus", "Not started"),
                    new String[][]{{"buySpawners", "Buy spawners", on}}, null, false, false));
                t.add(new Tile(UiIcon.BOX, "XP Skeleton Spawner", "Buys and confirms until Reward XP runs out. Stop at any time.", str(r, "xpSpawnerStatus", "Not started"),
                    new String[][]{{"buyXpSpawner", "Buy", on}, {"stopXpSpawner", "Stop", ctl ? "1" : "0"}}, null, false, false));
            }
            case "selling" -> {
                t.add(new Tile(UiIcon.RESET, "AutoSell", "Sells inventory items in batches of three rows and keeps going after reconnecting.", str(r, "autoSellStatus", bool(r, "autoSellEnabled") ? "On" : "Off"),
                    new String[0][], "toggleAutoSell", bool(r, "autoSellEnabled"), ctl));
                t.add(new Tile(UiIcon.RESET, "Bone Sell", "Drops all bones where the account looks and sells everything else.", str(r, "boneSellStatus", bool(r, "boneSellEnabled") ? "On" : "Off"),
                    new String[0][], "toggleBoneSell", bool(r, "boneSellEnabled"), ctl));
            }
            case "movement" -> {
                t.add(new Tile(UiIcon.ACTIVITY, "Force Sneak", "Holds sneak all the time, also after reconnecting.", bool(r, "forceSneakEnabled") ? "Sneaking" : "Off",
                    new String[0][], "toggleForceSneak", bool(r, "forceSneakEnabled"), ctl));
                boolean afk = s != null && s.afk();
                t.add(new Tile(UiIcon.ACTIVITY, "AFK moves", "Small jumps, looks and swings so the server doesn't kick the account for being AFK.", afk ? "On" : "Off",
                    new String[0][], "afk", afk, ctl && online));
            }
            default -> {
                long until = r.has("eggCooldownUntil") && !r.get("eggCooldownUntil").isJsonNull() ? r.get("eggCooldownUntil").getAsLong() : 0;
                long left = until - (System.currentTimeMillis() + this.api.clockOffset);
                String eggs = left > 0 ? "Next eggs in " + fmt(left) : str(r, "eggStatus", "Ready to claim");
                t.add(new Tile(UiIcon.GIFT, "Egg rewards", "Claimed automatically every midnight. Claim by hand when ready.", eggs,
                    new String[][]{{"claimEggs", "Claim eggs", online && ctl && left <= 0 ? "1" : "0"}}, null, false, false));
                t.add(new Tile(UiIcon.GIFT, "Daily reward", "Collect the daily reward. AutoClaim runs at 01:00 Paris time on the VPS.", str(r, "dailyStatus", "Not claimed yet"),
                    new String[][]{{"claimDaily", "Claim daily", on}}, "toggleDailyAuto", bool(r, "dailyAutoEnabled"), ctl));
                t.add(new Tile(UiIcon.GIFT, "Rewards", "Checks the reward menu and claims everything that is ready.", str(r, "status", "Not checked"),
                    new String[][]{{"claimRewards", "Claim rewards", on}}, "toggleRewards", bool(r, "enabled"), ctl));
            }
        }
        return t;
    }

    private void drawTile(UiRenderer ui, Tile t, AuroraClient.Account a, float x, float y, float w, float h, float u, double mx, double my) {
        boolean hot = inside(mx, my, x, y, w, h);
        UiTheme.card(ui, x, y, w, h, 14 * u, this.anim("tile" + t.title(), hot ? 1 : 0, 14), t.toggleType() != null && t.toggleOn() ? 1f : 0f, u);
        float pad = 14 * u, is = 30 * u;
        ui.round(x + pad, y + pad, is, is, 9 * u, UiEasing.mix(UiRenderer.white(0.06f), UiPalette.accent(0.18f), t.toggleOn() ? 1f : 0f));
        ui.icon(t.icon(), x + pad + 7 * u, y + pad + 7 * u, 16 * u, t.toggleOn() ? UiPalette.accent(1f) : UiTheme.TEXT);
        float tx = x + pad + is + 12 * u, tw = w - (tx - x) - pad - (t.toggleType() != null ? 50 * u : 0);
        ui.text(ui.ellipsize(t.title(), tw, 14 * u, 1), tx, y + pad + 1 * u, 14 * u, 1, UiTheme.TEXT);
        ui.text(ui.ellipsize(t.status(), tw, 11 * u, 0), tx, y + pad + 19 * u, 11 * u, 0, t.toggleOn() ? UiPalette.accent(0.95f) : UiTheme.SUB);
        if (t.toggleType() != null) {
            boolean wait = this.api.pending.containsKey(t.toggleType() + a.id() + this.serverId);
            this.toggle(ui, "tg:" + t.toggleType() + ":" + (t.toggleOn() ? "0" : "1"), t.toggleOn(), x + w - pad - 40 * u, y + pad + 4 * u, u, t.toggleEnabled() && !wait, mx, my);
        }
        float dy = y + pad + is + 12 * u;
        float bh = 30 * u, bottomY = y + h - pad - bh;
        for (String line : wrap(ui, t.text(), w - 2 * pad, 11.5f * u, 0)) {
            if (dy + 14 * u > bottomY - 6 * u) break;
            ui.text(line, x + pad, dy, 11.5f * u, 0, UiTheme.FAINT);
            dy += 16 * u;
        }
        float bx = x + pad;
        for (String[] b : t.buttons()) {
            float bw = Math.max(80 * u, ui.textWidth(b[1], 13 * u, 1) + 30 * u);
            boolean wait = this.api.pending.containsKey(b[0] + a.id() + this.serverId);
            this.button(ui, "act:" + b[0], wait ? "…" : b[1], bx, bottomY, bw, bh, b == t.buttons()[0], "1".equals(b[2]) && !wait, u, mx, my);
            bx += bw + 8 * u;
        }
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
        ui.circle(x + 16 * u, y + h / 2, 3 * u, this.api.toastGood ? GREEN : RED);
        ui.text(this.api.toast, x + 26 * u, y + (h - ts) / 2 - u, ts, 0, UiTheme.TEXT);
        ui.popAlpha();
    }

    // ---- helpers ------------------------------------------------------------------------------------

    private static long onlineOn(List<AuroraClient.Account> accounts, Map<String, AuroraClient.Session> sessions, String serverId) {
        return accounts.stream().filter(a -> a.servers().contains(serverId) && "online".equals(st(sessions.get(a.id() + "@" + serverId)))).count();
    }

    private static boolean isUp(String status) {
        return !"offline".equals(status) && !"error".equals(status) && !status.isEmpty();
    }

    private static String st(AuroraClient.Session s) {
        return s == null ? "offline" : s.status() == null || s.status().isEmpty() ? "offline" : s.status();
    }

    private static String str(JsonObject o, String k, String fallback) {
        return o.has(k) && !o.get(k).isJsonNull() && !o.get(k).getAsString().isBlank() ? o.get(k).getAsString() : fallback;
    }

    private static boolean bool(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() && o.get(k).getAsBoolean();
    }

    private static String cap(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String fmt(long ms) {
        long m = Math.max(1, ms / 60000);
        return m >= 60 ? m / 60 + "h " + m % 60 + "m" : m + "m";
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
        if (text.isEmpty() || !this.api.canChat || this.serverId == null) return;
        if (this.accountId == null) {
            if (this.api.pending.containsKey("chatAll" + this.serverId)) return;
            List<AuroraClient.Account> shown = new ArrayList<>();
            for (AuroraClient.Account a : this.api.accounts) if (a.servers().contains(this.serverId)) shown.add(a);
            List<String> ids = this.onlineIds(shown);
            if (ids.isEmpty()) return;
            this.api.chatAll(ids, this.serverId, text);
        } else this.api.action("chat", this.accountId, this.serverId, text);
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
            boolean inScroll = k.startsWith("p:") || k.startsWith("start:") || k.startsWith("stop:");
            if (inScroll && !inside(x, y, this.listArea[0], this.listArea[1], this.listArea[2], this.listArea[3])) continue;
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
            case "#startall", "#stopall" -> {
                boolean start = k.equals("#startall");
                for (AuroraClient.Account a : this.api.accounts) {
                    if (!a.servers().contains(this.serverId)) continue;
                    boolean up = isUp(st(this.api.sessions.get(a.id() + "@" + this.serverId)));
                    if (start != up) this.api.action(start ? "start" : "stop", a.id(), this.serverId, null);
                }
                cue(UiAudio.Cue.TICK);
            }
            default -> {
                if (k.startsWith("s:")) {
                    String id = k.substring(2);
                    if (!id.equals(this.serverId)) { this.serverId = id; this.accountId = null; this.listScrollTarget = this.listScroll = 0; this.chatScrollTarget = this.chatScroll = 0; }
                    cue(UiAudio.Cue.TICK);
                } else if (k.startsWith("tab:")) {
                    String t = k.substring(4);
                    if (!t.equals(tab)) { tab = t; this.listScrollTarget = this.listScroll = 0; if (t.equals("chat")) this.accountId = null; }
                    cue(UiAudio.Cue.TICK);
                } else if (k.startsWith("sec:")) {
                    section = k.substring(4);
                    cue(UiAudio.Cue.TICK);
                } else if (k.startsWith("p:")) {
                    String id = k.substring(2);
                    this.accountId = id.isEmpty() ? null : id;
                    this.chatScrollTarget = this.chatScroll = 0;
                    this.chatFocus = tab.equals("chat") && this.api.canChat;
                    cue(UiAudio.Cue.TICK);
                } else if (k.startsWith("start:") || k.startsWith("stop:")) {
                    int c = k.indexOf(':');
                    this.api.action(k.substring(0, c), k.substring(c + 1), this.serverId, null);
                    cue(UiAudio.Cue.TICK);
                } else if (k.startsWith("act:") && this.accountId != null) {
                    this.api.action(k.substring(4), this.accountId, this.serverId, null);
                    cue(UiAudio.Cue.TICK);
                } else if (k.startsWith("tg:") && this.accountId != null) {
                    String[] p = k.split(":");
                    if (p[1].equals("afk")) this.api.action("afk", this.accountId, this.serverId, null);
                    else this.api.action(p[1], this.accountId, this.serverId, null, p[2].equals("1"));
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
        if (tab.equals("chat") && inside(px, py, this.chatArea[0], this.chatArea[1], this.chatArea[2], this.chatArea[3])) this.chatScrollTarget = UiEasing.clamp(this.chatScrollTarget + (float) v * step, 0, this.chatMax);
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
        if ((code == 257 || code == 335 || code == 84) && tab.equals("chat") && this.api.canChat) this.chatFocus = true;
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
        for (AuroraClient.Account a : this.api.accounts) if (a.servers().contains(this.serverId)) { this.click("p:" + a.id(), false); return; }
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
