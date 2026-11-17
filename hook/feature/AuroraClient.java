package hook.feature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Connection to the AuroraAFK VPS for the AuroraAFK panel.
 *
 * <ul>
 * <li>Only talks HTTPS to the AuroraAFK VPS and only trusts the AuroraAFK private CA below (pinned, public part only),
 * so nobody in between can read or change anything.</li>
 * <li>The key is checked by the VPS. It is sent once per login, after that a short-lived session token is used.</li>
 * <li>The key is stored encrypted on this PC: Windows DPAPI (bound to this Windows user) or, elsewhere, AES-GCM.</li>
 * <li>Nothing here is secret: without a key that the VPS accepts, the panel can not see or do anything.</li>
 * </ul>
 */
final class AuroraClient {
    static final AuroraClient INSTANCE = new AuroraClient();
    private static final String BASE = "https://5.175.174.77:8446";
    private static final Pattern KEY = Pattern.compile("^HV(-[A-Z2-7]{5}){5}$");
    private static final String CA = """
        -----BEGIN CERTIFICATE-----
        MIIEHzCCAoegAwIBAgIUSywi9rQLZsyrydqFh39ewLNJyugwDQYJKoZIhvcNAQEL
        BQAwHzEdMBsGA1UEAwwUQXVyb3JhQUZLIFByaXZhdGUgQ0EwHhcNMjYxMDI1MTYz
        MDIzWhcNMzYxMDIyMTYzMDIzWjAfMR0wGwYDVQQDDBRBdXJvcmFBRksgUHJpdmF0
        ZSBDQTCCAaIwDQYJKoZIhvcNAQEBBQADggGPADCCAYoCggGBANHb/i/HI4ukrvaL
        Z6D+ttDAfeARJK4me3eIXxm3b9/ENBHPKRb8mdFjaKfUos27wr3qROEq3WnQ1mfT
        pC/eSU5UDfq0ciEgp0UhbHmvOt4vKwBx4bJrQSUbJNKRgetfXD+xgQ5o712l2E85
        SGdx0ja1yhFhJuwiAbod/sEdrTkZyVIXn8i89DeFBHFb+vCXIRifXBSp/Rh2L8fy
        5bcZZuMa2QdQdyKM+BaJQjQKL9JgIjAJBY3hPl7A6g1h0G3PGeTdXuWLzDGz2Fgr
        YhG1SZp1/uZsSufIwvJSrxtbvQ2ppl7nqx9+soTKmmhUam5CNu3qWhMmMFRbG8nN
        53qNixIzFqrm5IoS5FFI3VWEIYNQXdRMNiY5lH5eAnv+QHvZukH/Mimshn4F/Gqq
        8bcVzhSzZAOCO/b1PdDp+ZBrXhmjrlK18FyIIIwSxi3fxUJtJq8XIZTfL5bYUZC7
        mFtTBO/3OYdsYmIxi1FAM/Cje2f7C2ZrHXZYiaV0wlo/rnBd6QIDAQABo1MwUTAd
        BgNVHQ4EFgQUV/8km7dFBFaLS0l7KRRHKKWML2cwHwYDVR0jBBgwFoAUV/8km7dF
        BFaLS0l7KRRHKKWML2cwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOC
        AYEAk1HMRCO6ugTVt589owAh76doJ5HacT86+NzI0A3wUqIjqAHn2bmywd5CYDat
        bL4cORpo1W2EU542ECrL+zn8CQyEwnCSPWtf3cVvhuNL06GcuU2V1PcPZkGFjMB1
        m1JYB5TSzdDPFkBR2hYR2GC/0+eA2AL+veTXceAw3d1CIlzF572iZ9vic805nkwj
        MCL2wpSxcSdrlmQHmkDsHQuW/Lh3uw0P9kuZbzz3v2nBiIa/HXheWQF5iXvL6heW
        TarbUkM7lPqIygK/33J77hbvI2L1tfk/JFY/ZW+duN3ON789N8rUPEsZRrjRGqT9
        lzPJ5CdZ++uHrCFlJR3K4EjDaS5jilr/1uE7IP5DvLVLrmvNRfMbGYDGthSNnlid
        gS/XxOv3mgCz4cw7DZhaSoY/191pQq7v5TmjPtvMDVNOmEB5mTbefwc7EefaRXnT
        2uUVjlKS+nD0jHTrktUz3yIDrKrUe2vt8i7p+Qoi+5giMqRfhrKVfszyHNzC3ZJR
        eLJG
        -----END CERTIFICATE-----
        """;

    enum Status { NEED_KEY, CHECKING, READY }

    record Server(String id, String name, String address) {}

    record Account(String id, String name, List<String> servers, boolean pinned) {}

    record Session(String status, String reason, double health, double food, String pos, boolean afk, String money, String gems, String coins, String rewardXp, String busy) {}

    record Log(String id, String accountId, String serverId, String level, String message, long at) {}

    // ---- state (written on the worker thread, read by the screen) --------------------------------------
    volatile Status status = Status.NEED_KEY;
    volatile String message = "";
    volatile boolean messageError;
    volatile String keyName = "";
    volatile boolean canControl, canChat, live, reachable = true;
    volatile List<Server> servers = List.of();
    volatile List<Account> accounts = List.of();
    volatile Map<String, Session> sessions = Map.of();
    /** Actions state per account@server (AutoSell on, daily status, ...). */
    volatile Map<String, JsonObject> rewards = Map.of();
    volatile Map<String, String> shops = Map.of();
    volatile java.util.Set<String> activeServers = java.util.Set.of();
    volatile long clockOffset;
    /** How often the open panel asks the VPS for fresh data. */
    static final long REFRESH_MS = 7000;
    final Map<String, UiTextureData> heads = Collections.synchronizedMap(new HashMap<>());
    final ArrayDeque<Log> logs = new ArrayDeque<>();
    volatile long logVersion;
    volatile String toast = "";
    volatile long toastAt;
    final Map<String, Long> pending = Collections.synchronizedMap(new HashMap<>());

    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "HookVisuals AuroraAFK");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean busy = new AtomicBoolean();
    private HttpClient http;
    private String session, lastLogId, device;
    private long nextPoll, nextFull, retryAt;
    private boolean started;

    private AuroraClient() {
    }

    // ---- life cycle -----------------------------------------------------------------------------------

    /** Called when the panel opens: use the saved key, or ask for one. */
    void open() {
        if (this.status == Status.READY && this.session != null) { this.nextPoll = 0; return; }
        if (this.started && this.status == Status.CHECKING) return;
        this.started = true;
        this.submit(() -> {
            Saved saved = Saved.load();
            this.device = saved.deviceId();
            if (saved.key == null) { this.status = Status.NEED_KEY; return; }
            this.login(saved.key, false);
        });
    }

    /** Key typed in by the user. */
    void submitKey(String raw) {
        String key = normalize(raw);
        if (!KEY.matcher(key).matches()) { this.say("That is not a valid key. It looks like HV-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX.", true); return; }
        this.status = Status.CHECKING;
        this.say("Checking key with the AuroraAFK server…", false);
        this.submit(() -> {
            if (this.device == null) this.device = Saved.load().deviceId();
            this.login(key, true);
        });
    }

    /** Forget the key on this PC and go back to the key screen. */
    void changeKey() {
        String token = this.session;
        this.session = null;
        this.status = Status.NEED_KEY;
        this.say("Paste a new key.", false);
        this.submit(() -> {
            if (token != null) { try { this.send("POST", "/v1/logout", "{}", token); } catch (Exception ignored) {} }
            Saved s = Saved.load();
            s.key = null;
            s.save();
            this.clearData();
        });
    }

    /** Called every frame while the panel is open: keeps the data fresh (about once per second). */
    void tick() {
        long now = System.currentTimeMillis();
        if (this.status == Status.CHECKING && this.retryAt > 0 && now >= this.retryAt && !this.busy.get()) {
            this.retryAt = 0;
            this.submit(() -> {
                Saved saved = Saved.load();
                if (saved.key == null) this.status = Status.NEED_KEY;
                else this.login(saved.key, false);
            });
            return;
        }
        if (this.status != Status.READY || now < this.nextPoll || this.busy.get()) return;
        this.nextPoll = now + REFRESH_MS;
        this.submit(this::poll);
    }

    void action(String type, String accountId, String serverId, String text) {
        this.action(type, accountId, serverId, text, null);
    }

    void action(String type, String accountId, String serverId, String text, Boolean enabled) {
        if (this.status != Status.READY) return;
        this.pending.put(type + accountId + serverId, System.currentTimeMillis());
        this.worker.execute(() -> {
            try {
                JsonObject b = new JsonObject();
                b.addProperty("type", type);
                b.addProperty("accountId", accountId);
                b.addProperty("serverId", serverId);
                if (text != null) b.addProperty("message", text);
                if (enabled != null) b.addProperty("enabled", enabled);
                Reply r = this.send("POST", "/v1/action", b.toString(), this.session);
                if (r.code == 401) { this.relogin(); r = this.send("POST", "/v1/action", b.toString(), this.session); }
                if (r.code != 200) this.toast(r.error("Action failed."));
                else { this.nextPoll = System.currentTimeMillis() + 1200; if (!type.equals("chat")) this.toastOk(okText(type, enabled)); }
            } catch (Exception e) {
                this.toast("Can't reach the AuroraAFK server.");
            } finally {
                this.pending.remove(type + accountId + serverId);
            }
        });
    }

    /** One payout offer from the VPS: what every online account would send (99% of its balance). */
    record PayoutQuote(String batch, String serverId, String recipient, long total, List<String[]> rows, int skipped, long at) {}
    volatile PayoutQuote payoutQuote;
    volatile String payoutError = "";

    /** Sends one chat message (or /command) from every online account of the server at once. */
    void chatAll(String serverId, String text) {
        JsonObject b = new JsonObject();
        b.addProperty("message", text);
        this.bulk("chat", serverId, b, r -> {
            int ok = r.json.get("sent").getAsInt(), failed = r.json.get("failed").getAsInt();
            if (failed == 0) this.toastOk("Sent from " + ok + (ok == 1 ? " account" : " accounts"));
            else this.toast("Sent from " + ok + ", " + failed + " failed");
        });
    }

    /** Claims eggs on every online account whose eggs are ready. */
    void claimEggsAll(String serverId) {
        this.bulk("claimEggs", serverId, new JsonObject(), r -> {
            int n = r.json.get("queued").getAsInt();
            if (n == 0) this.toast("No account has eggs ready right now");
            else this.toastOk("Claiming eggs on " + n + (n == 1 ? " account" : " accounts"));
        });
    }

    /** Step 1 of the payout: asks the VPS how much each online account would send to the player. */
    void payoutQuote(String serverId, String recipient) {
        this.payoutQuote = null;
        this.payoutError = "";
        JsonObject b = new JsonObject();
        b.addProperty("recipient", recipient);
        this.bulk("payoutQuote", serverId, b, r -> {
            List<String[]> rows = new ArrayList<>();
            Map<String, String> names = new HashMap<>();
            for (Account a : this.accounts) names.put(a.id, a.name);
            for (JsonElement e : arr(r.json, "quotes")) {
                JsonObject q = e.getAsJsonObject();
                rows.add(new String[]{names.getOrDefault(str(q, "accountId"), "?"), str(q, "display"), String.valueOf(q.get("amount").getAsLong()), str(q, "accountId")});
            }
            this.payoutQuote = new PayoutQuote(str(r.json, "batch"), serverId, str(r.json, "recipient"), r.json.get("total").getAsLong(), List.copyOf(rows), arr(r.json, "skipped").size(), System.currentTimeMillis());
            if (rows.isEmpty()) this.payoutError = "No online account has money to pay out.";
        }, err -> this.payoutError = err);
    }

    /** Step 2: sends the payout the player just checked (only once, only within 100 seconds). */
    void payoutSend() {
        PayoutQuote q = this.payoutQuote;
        if (q == null) return;
        this.payoutQuote = null;
        JsonObject b = new JsonObject();
        b.addProperty("batch", q.batch());
        this.bulk("payoutSend", q.serverId(), b, r -> this.toastOk("Paying $" + String.format(java.util.Locale.US, "%,d", r.json.get("total").getAsLong()) + " to " + str(r.json, "recipient") + " from " + r.json.get("sending").getAsInt() + " accounts · check chat"));
    }

    private void bulk(String type, String serverId, JsonObject body, java.util.function.Consumer<Reply> ok) {
        this.bulk(type, serverId, body, ok, this::toast);
    }

    private void bulk(String type, String serverId, JsonObject body, java.util.function.Consumer<Reply> ok, java.util.function.Consumer<String> fail) {
        if (this.status != Status.READY) return;
        String mark = type + "All" + serverId;
        if (this.pending.containsKey(mark)) return;
        this.pending.put(mark, System.currentTimeMillis());
        body.addProperty("type", type);
        body.addProperty("serverId", serverId);
        this.worker.execute(() -> {
            try {
                Reply r = this.send("POST", "/v1/bulk", body.toString(), this.session);
                if (r.code == 401) { this.relogin(); r = this.send("POST", "/v1/bulk", body.toString(), this.session); }
                if (r.code != 200 || r.json == null) { fail.accept(r.error("Action failed.")); return; }
                this.nextPoll = System.currentTimeMillis() + 1200;
                ok.accept(r);
            } catch (Exception e) {
                fail.accept("Can't reach the AuroraAFK server.");
            } finally {
                this.pending.remove(mark);
            }
        });
    }

    // ---- worker side ----------------------------------------------------------------------------------

    private void submit(Runnable job) {
        if (!this.busy.compareAndSet(false, true)) return;
        this.worker.execute(() -> {
            try { job.run(); } catch (Throwable t) { this.say("Something went wrong: " + t.getClass().getSimpleName(), true); } finally { this.busy.set(false); }
        });
    }

    private void login(String key, boolean typed) {
        try {
            JsonObject b = new JsonObject();
            b.addProperty("key", key);
            b.addProperty("device", this.device);
            Reply r = this.send("POST", "/v1/login", b.toString(), null);
            if (r.code == 200 && r.json != null) {
                JsonObject j = r.json;
                this.session = j.get("session").getAsString();
                this.keyName = str(j, "name");
                this.canControl = j.has("control") && j.get("control").getAsBoolean();
                this.canChat = j.has("chat") && j.get("chat").getAsBoolean();
                Saved s = Saved.load();
                if (!key.equals(s.key)) { s.key = key; s.save(); }
                if (typed) this.clearData();
                this.reachable = true;
                this.retryAt = 0;
                this.nextFull = 0;
                this.nextPoll = 0;
                this.say("", false);
                this.status = Status.READY;
                this.poll();
                return;
            }
            String reason = r.error("invalid");
            if (r.code == 429) {
                this.status = Status.NEED_KEY;
                this.say("Too many wrong keys from your connection. Wait 15 minutes and try again.", true);
                return;
            }
            if (r.code == 401 || r.code == 400) {
                Saved s = Saved.load();
                s.key = null;
                s.save();
                this.session = null;
                this.clearData();
                this.status = Status.NEED_KEY;
                this.say(switch (reason) {
                    case "revoked" -> "This key was turned off. Paste a new key.";
                    case "expired" -> "This key has expired. Paste a new key.";
                    case "device" -> "This key is locked to another PC. Ask for a reset or a new key.";
                    default -> typed ? "The AuroraAFK server does not know this key." : "Your saved key no longer works. Paste a new key.";
                }, true);
                return;
            }
            this.unreachable(typed, "The AuroraAFK server answered " + r.code + ".");
        } catch (Exception e) {
            this.unreachable(typed, "Can't reach the AuroraAFK server.");
        }
    }

    private void unreachable(boolean typed, String text) {
        this.reachable = false;
        if (typed) { this.status = Status.NEED_KEY; this.say(text + " Check your internet and try again.", true); }
        else { this.status = Status.CHECKING; this.say(text + " Retrying…", true); this.retryAt = System.currentTimeMillis() + 4000; }
    }

    private void relogin() {
        Saved s = Saved.load();
        if (s.key != null) { this.login(s.key, false); return; }
        this.session = null;
        this.clearData();
        this.status = Status.NEED_KEY;
    }

    private void poll() {
        if (this.session == null) return;
        try {
            boolean full = System.currentTimeMillis() >= this.nextFull;
            String q = "/v1/state?" + (full ? "full=1" : "full=0") + (this.lastLogId != null ? "&since=" + this.lastLogId : "");
            Reply r = this.send("GET", q, null, this.session);
            if (r.code == 401) { this.relogin(); return; } // session ran out or key was changed on the VPS: the login decides
            if (r.code != 200 || r.json == null) { this.reachable = r.code != 503; this.live = false; return; }
            this.reachable = true;
            this.accept(r.json, full);
            if (full) this.nextFull = System.currentTimeMillis() + 60_000;
        } catch (Exception e) {
            this.reachable = false;
            this.live = false;
            this.nextPoll = System.currentTimeMillis() + 3000;
        }
    }

    private void accept(JsonObject j, boolean full) {
        this.live = j.has("live") && j.get("live").getAsBoolean();
        this.canControl = j.has("control") && j.get("control").getAsBoolean();
        this.canChat = j.has("chat") && j.get("chat").getAsBoolean();
        if (j.has("name")) this.keyName = str(j, "name");
        List<Server> sv = new ArrayList<>();
        for (JsonElement e : arr(j, "servers")) {
            JsonObject o = e.getAsJsonObject();
            sv.add(new Server(str(o, "id"), str(o, "name"), str(o, "address")));
        }
        List<Account> ac = new ArrayList<>();
        boolean missingHead = false;
        for (JsonElement e : arr(j, "accounts")) {
            JsonObject o = e.getAsJsonObject();
            List<String> on = new ArrayList<>();
            for (JsonElement s : arr(o, "servers")) on.add(s.getAsString());
            String id = str(o, "id");
            ac.add(new Account(id, str(o, "name"), on, o.has("pinned") && o.get("pinned").getAsBoolean()));
            if (o.has("skin") && !o.get("skin").isJsonNull()) {
                UiTextureData head = headFromSkin(id, o.get("skin").getAsString());
                if (head != null) this.heads.put(id, head);
            } else if (!this.heads.containsKey(id)) missingHead = true;
        }
        ac.sort((a, b) -> a.pinned != b.pinned ? (a.pinned ? -1 : 1) : a.name.compareToIgnoreCase(b.name));
        Map<String, Session> ss = new HashMap<>();
        for (JsonElement e : arr(j, "sessions")) {
            JsonObject o = e.getAsJsonObject();
            String pos = "";
            if (o.has("position") && o.get("position").isJsonObject()) {
                JsonObject p = o.getAsJsonObject("position");
                pos = p.get("x").getAsInt() + " " + p.get("y").getAsInt() + " " + p.get("z").getAsInt();
            }
            ss.put(str(o, "accountId") + "@" + str(o, "serverId"), new Session(str(o, "status"), str(o, "reason"), num(o, "health"), num(o, "food"), pos,
                o.has("afk") && o.get("afk").getAsBoolean(), str(o, "money"), str(o, "gems"), str(o, "coins"), str(o, "rewardXp"), str(o, "busy")));
        }
        synchronized (this.logs) {
            if (j.has("reset") && j.get("reset").getAsBoolean()) this.logs.clear();
            for (JsonElement e : arr(j, "logs")) {
                JsonObject o = e.getAsJsonObject();
                Log l = new Log(str(o, "id"), str(o, "accountId"), str(o, "serverId"), str(o, "level"), strip(str(o, "message")), o.has("at") ? o.get("at").getAsLong() : 0L);
                this.logs.addLast(l);
                this.lastLogId = l.id;
            }
            while (this.logs.size() > 600) this.logs.removeFirst();
            this.logVersion++;
        }
        Map<String, JsonObject> rw = new HashMap<>();
        for (JsonElement e : arr(j, "rewards")) { JsonObject o = e.getAsJsonObject(); rw.put(str(o, "accountId") + "@" + str(o, "serverId"), o); }
        Map<String, String> sh = new HashMap<>();
        if (j.has("shops") && j.get("shops").isJsonObject()) for (var en : j.getAsJsonObject("shops").entrySet()) sh.put(en.getKey(), en.getValue().getAsString());
        java.util.Set<String> act = new java.util.HashSet<>();
        for (JsonElement e : arr(j, "activeServers")) act.add(e.getAsString());
        if (j.has("serverNow")) this.clockOffset = j.get("serverNow").getAsLong() - System.currentTimeMillis();
        this.rewards = Map.copyOf(rw);
        this.shops = Map.copyOf(sh);
        this.activeServers = java.util.Set.copyOf(act);
        this.servers = List.copyOf(sv);
        this.accounts = List.copyOf(ac);
        this.sessions = Map.copyOf(ss);
        if (missingHead && !full) this.nextFull = Math.min(this.nextFull, System.currentTimeMillis() + 3000);
    }

    private void clearData() {
        this.servers = List.of();
        this.accounts = List.of();
        this.sessions = Map.of();
        this.rewards = Map.of();
        this.activeServers = java.util.Set.of();
        this.heads.clear();
        synchronized (this.logs) { this.logs.clear(); this.logVersion++; }
        this.lastLogId = null;
    }

    private void say(String text, boolean error) {
        this.message = text;
        this.messageError = error;
    }

    volatile boolean toastGood;

    private void toastOk(String text) {
        this.toast = text;
        this.toastGood = true;
        this.toastAt = System.currentTimeMillis();
    }

    private static String okText(String type, Boolean enabled) {
        return switch (type) {
            case "start" -> "Connecting…";
            case "stop" -> "Disconnected.";
            case "claimDaily" -> "Daily reward claim started.";
            case "claimEggs" -> "Egg claim started.";
            case "claimRewards" -> "Reward claim started.";
            case "buySpawners", "buyXpSpawner" -> "Buying started.";
            case "stopXpSpawner" -> "Stopped.";
            case "setHome" -> "Home 1 set.";
            case "tpHome" -> "Teleporting to home 1.";
            case "deleteHome" -> "Home 1 deleted.";
            default -> enabled == null ? "Done." : enabled ? "Turned on." : "Turned off.";
        };
    }

    private void toast(String text) {
        this.toastGood = false;
        this.toast = text;
        this.toastAt = System.currentTimeMillis();
    }

    // ---- http -----------------------------------------------------------------------------------------

    private record Reply(int code, JsonObject json) {
        String error(String fallback) {
            return this.json != null && this.json.has("error") ? this.json.get("error").getAsString() : fallback;
        }
    }

    private Reply send(String method, String path, String body, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + path)).timeout(Duration.ofSeconds(12)).header("Accept", "application/json");
        if (token != null) b.header("Authorization", "Session " + token);
        if (body != null) b.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        else b.method(method, HttpRequest.BodyPublishers.noBody());
        HttpResponse<String> res = this.http().send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject json = null;
        try {
            JsonElement e = JsonParser.parseString(res.body());
            if (e.isJsonObject()) json = e.getAsJsonObject();
        } catch (Exception ignored) {
        }
        return new Reply(res.statusCode(), json);
    }

    private synchronized HttpClient http() throws Exception {
        if (this.http == null) {
            KeyStore trust = KeyStore.getInstance(KeyStore.getDefaultType());
            trust.load(null, null);
            trust.setCertificateEntry("auroraafk-ca", CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(CA.getBytes(StandardCharsets.US_ASCII))));
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trust);
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(null, tmf.getTrustManagers(), new SecureRandom());
            this.http = HttpClient.newBuilder().sslContext(ssl).connectTimeout(Duration.ofSeconds(6)).followRedirects(HttpClient.Redirect.NEVER).build();
        }
        return this.http;
    }

    // ---- helpers --------------------------------------------------------------------------------------

    static String normalize(String raw) {
        String s = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (s.startsWith("HV")) s = s.substring(2);
        if (s.length() != 25) return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        StringBuilder out = new StringBuilder("HV");
        for (int i = 0; i < 25; i += 5) out.append('-').append(s, i, i + 5);
        return out.toString();
    }

    private static String strip(String s) {
        return s.replaceAll("§.", "");
    }

    private static String str(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e == null || e.isJsonNull() ? "" : e.getAsString();
    }

    private static double num(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e == null || e.isJsonNull() ? -1 : e.getAsDouble();
    }

    private static JsonArray arr(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e != null && e.isJsonArray() ? e.getAsJsonArray() : new JsonArray();
    }

    /** Face + hat layer of a skin, scaled up with hard pixels. */
    static UiTextureData headFromSkin(String id, String dataUrl) {
        try {
            int comma = dataUrl.indexOf(',');
            if (!dataUrl.startsWith("data:image/png;base64,") || comma < 0) return null;
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(dataUrl.substring(comma + 1))));
            if (img == null || img.getWidth() < 64 || img.getHeight() < 32) return null;
            int scale = img.getWidth() / 64, n = 8, up = 8, size = n * up;
            int[] px = new int[size * size];
            for (int y = 0; y < n; y++) {
                for (int x = 0; x < n; x++) {
                    int face = img.getRGB((8 + x) * scale, (8 + y) * scale) | 0xFF000000;
                    int hat = img.getRGB((40 + x) * scale, (8 + y) * scale);
                    int a = hat >>> 24;
                    int c = a == 0 ? face : blend(face, hat, a / 255f);
                    for (int yy = 0; yy < up; yy++) for (int xx = 0; xx < up; xx++) px[(y * up + yy) * size + x * up + xx] = c;
                }
            }
            return new UiTextureData("aurora_head_" + id + "_" + Integer.toHexString(dataUrl.hashCode()), size, size, px);
        } catch (Throwable t) {
            return null;
        }
    }

    private static int blend(int base, int top, float a) {
        int r = Math.round(((base >> 16) & 255) * (1 - a) + ((top >> 16) & 255) * a);
        int g = Math.round(((base >> 8) & 255) * (1 - a) + ((top >> 8) & 255) * a);
        int b = Math.round((base & 255) * (1 - a) + (top & 255) * a);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    // ---- encrypted storage on this PC -----------------------------------------------------------------

    /** The saved key and this PC's device secret, encrypted at rest. */
    static final class Saved {
        private static final byte[] MAGIC = "HVA1".getBytes(StandardCharsets.US_ASCII);
        private static final byte[] ENTROPY = "HookVisuals AuroraAFK key v1".getBytes(StandardCharsets.US_ASCII);
        String key;
        String deviceSecret;

        static Path file() {
            return FabricLoader.getInstance().getConfigDir().resolve("hook").resolve("auroraafk.bin");
        }

        String deviceId() {
            try {
                if (this.deviceSecret == null) {
                    byte[] r = new byte[32];
                    new SecureRandom().nextBytes(r);
                    this.deviceSecret = HexFormat.of().formatHex(r);
                    this.save();
                }
                return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(("hookvisuals device|" + this.deviceSecret).getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        static synchronized Saved load() {
            Saved s = new Saved();
            try {
                Path f = file();
                if (!Files.exists(f)) return s;
                byte[] raw = Files.readAllBytes(f);
                if (raw.length < 6 || !java.util.Arrays.equals(java.util.Arrays.copyOf(raw, 4), MAGIC)) return s;
                byte[] body = java.util.Arrays.copyOfRange(raw, 5, raw.length);
                byte[] plain = raw[4] == 1 ? dpapi(body, false) : aes(body, false);
                JsonObject j = JsonParser.parseString(new String(plain, StandardCharsets.UTF_8)).getAsJsonObject();
                s.key = j.has("key") && !j.get("key").isJsonNull() ? j.get("key").getAsString() : null;
                s.deviceSecret = j.has("device") ? j.get("device").getAsString() : null;
                if (s.key != null && !KEY.matcher(s.key).matches()) s.key = null;
            } catch (Throwable ignored) {
                // unreadable (other PC / other Windows user / damaged): start fresh
            }
            return s;
        }

        synchronized void save() {
            try {
                JsonObject j = new JsonObject();
                if (this.key != null) j.addProperty("key", this.key);
                if (this.deviceSecret != null) j.addProperty("device", this.deviceSecret);
                byte[] plain = j.toString().getBytes(StandardCharsets.UTF_8);
                byte mode;
                byte[] body;
                try {
                    body = dpapi(plain, true);
                    mode = 1;
                } catch (Throwable notWindows) {
                    body = aes(plain, true);
                    mode = 2;
                }
                java.util.Arrays.fill(plain, (byte) 0);
                Path f = file();
                Files.createDirectories(f.getParent());
                Path tmp = f.resolveSibling(f.getFileName() + ".tmp");
                byte[] out = new byte[5 + body.length];
                System.arraycopy(MAGIC, 0, out, 0, 4);
                out[4] = mode;
                System.arraycopy(body, 0, out, 5, body.length);
                Files.write(tmp, out);
                Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Throwable ignored) {
            }
        }

        private static byte[] dpapi(byte[] data, boolean protect) {
            if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) throw new UnsupportedOperationException();
            int flags = 1; // CRYPTPROTECT_UI_FORBIDDEN
            return protect
                ? com.sun.jna.platform.win32.Crypt32Util.cryptProtectData(data, ENTROPY, flags, "HookVisuals AuroraAFK", null)
                : com.sun.jna.platform.win32.Crypt32Util.cryptUnprotectData(data, ENTROPY, flags, null);
        }

        private static byte[] aes(byte[] data, boolean encrypt) throws Exception {
            String host = "";
            try { host = java.net.InetAddress.getLocalHost().getHostName(); } catch (Exception ignored) {}
            byte[] k = MessageDigest.getInstance("SHA-256").digest(("HookVisuals AuroraAFK|" + System.getProperty("user.name") + "|" + System.getProperty("user.home") + "|" + System.getProperty("os.name") + "|" + host).getBytes(StandardCharsets.UTF_8));
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            if (encrypt) {
                byte[] iv = new byte[12];
                new SecureRandom().nextBytes(iv);
                c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(k, "AES"), new GCMParameterSpec(128, iv));
                c.updateAAD(MAGIC);
                byte[] ct = c.doFinal(data);
                byte[] out = new byte[12 + ct.length];
                System.arraycopy(iv, 0, out, 0, 12);
                System.arraycopy(ct, 0, out, 12, ct.length);
                return out;
            }
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(k, "AES"), new GCMParameterSpec(128, data, 0, 12));
            c.updateAAD(MAGIC);
            return c.doFinal(data, 12, data.length - 12);
        }
    }
}
