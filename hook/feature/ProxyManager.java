package hook.feature;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import net.minecraft.class_310;

/**
 * Saved proxies (like Meteor's Proxies screen): a list you can add to, edit and remove from; the one that is turned
 * on carries every server connection (joining and the server list pings). Stored in config/hook/proxies.json,
 * passwords encrypted for this Windows user (DPAPI).
 */
public final class ProxyManager {
    public enum Type { SOCKS5, SOCKS4 }

    public static final class Proxy {
        String name = "", host = "", user = "", password = "";
        int port = 1080;
        Type type = Type.SOCKS5;
        boolean enabled;

        String address() { return this.host + ":" + this.port; }

        String label() { return this.name.isBlank() ? this.address() : this.name; }
    }

    public static final List<Proxy> PROXIES = new ArrayList<>();
    private static boolean loaded;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ProxyManager() {
    }

    private static Path file() {
        return class_310.method_1551().field_1697.toPath().resolve("config").resolve("hook").resolve("proxies.json");
    }

    public static synchronized List<Proxy> all() {
        if (!loaded) load();
        return PROXIES;
    }

    /** The proxy that is turned on, or null (direct connection). */
    public static synchronized Proxy active() {
        for (Proxy p : all()) if (p.enabled && !p.host.isBlank() && p.port > 0) return p;
        return null;
    }

    /** Only one proxy can be on at a time. */
    public static synchronized void setEnabled(Proxy p, boolean on) {
        for (Proxy o : all()) o.enabled = false;
        p.enabled = on;
        save();
    }

    private static void load() {
        loaded = true;
        PROXIES.clear();
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            JsonElement root = JsonParser.parseString(Files.readString(f, StandardCharsets.UTF_8));
            JsonArray arr = root.isJsonObject() && root.getAsJsonObject().has("proxies") ? root.getAsJsonObject().getAsJsonArray("proxies") : new JsonArray();
            for (JsonElement e : arr) {
                JsonObject o = e.getAsJsonObject();
                Proxy p = new Proxy();
                p.name = str(o, "name");
                p.host = str(o, "host");
                p.port = o.has("port") ? o.get("port").getAsInt() : 1080;
                p.user = str(o, "user");
                p.password = unprotect(str(o, "password"));
                try { p.type = Type.valueOf(str(o, "type")); } catch (Exception ignored) { p.type = Type.SOCKS5; }
                p.enabled = o.has("enabled") && o.get("enabled").getAsBoolean();
                PROXIES.add(p);
            }
        } catch (Exception e) {
            System.err.println("Hook: could not read proxies: " + e);
        }
    }

    public static synchronized void save() {
        try {
            JsonArray arr = new JsonArray();
            for (Proxy p : PROXIES) {
                JsonObject o = new JsonObject();
                o.addProperty("name", p.name);
                o.addProperty("type", p.type.name());
                o.addProperty("host", p.host);
                o.addProperty("port", p.port);
                o.addProperty("user", p.user);
                o.addProperty("password", protect(p.password));
                o.addProperty("enabled", p.enabled);
                arr.add(o);
            }
            JsonObject root = new JsonObject();
            root.add("proxies", arr);
            Path f = file();
            Files.createDirectories(f.getParent());
            Files.writeString(f, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Hook: could not save proxies: " + e);
        }
    }

    /**
     * Adds proxies from text, one per line: host:port, host:port:user:password or socks4://host:port / socks5://...
     * @return how many were added
     */
    public static synchronized int importText(String text) {
        int added = 0;
        if (text == null) return 0;
        for (String raw : text.split("[\\r\\n,;\\s]+")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            Type type = Type.SOCKS5;
            String lower = line.toLowerCase(java.util.Locale.ROOT);
            if (lower.startsWith("socks4://")) { type = Type.SOCKS4; line = line.substring(9); }
            else if (lower.startsWith("socks5://")) line = line.substring(9);
            String user = "", pass = "";
            int at = line.lastIndexOf('@');
            if (at > 0) {
                String cred = line.substring(0, at);
                line = line.substring(at + 1);
                int c = cred.indexOf(':');
                user = c < 0 ? cred : cred.substring(0, c);
                pass = c < 0 ? "" : cred.substring(c + 1);
            }
            String[] p = line.split(":");
            if (p.length < 2) continue;
            int port;
            try { port = Integer.parseInt(p[1]); } catch (NumberFormatException e) { continue; }
            if (port <= 0 || port > 65535 || p[0].isBlank()) continue;
            if (p.length >= 4) { user = p[2]; pass = p[3]; }
            Proxy x = new Proxy();
            x.host = p[0];
            x.port = port;
            x.type = type;
            x.user = user;
            x.password = pass;
            boolean dup = false;
            for (Proxy o : all()) if (o.host.equalsIgnoreCase(x.host) && o.port == x.port && o.user.equals(x.user)) dup = true;
            if (dup) continue;
            PROXIES.add(x);
            added++;
        }
        if (added > 0) save();
        return added;
    }

    /** Called from the connection mixin for every outgoing game connection. */
    public static void install(io.netty.channel.ChannelPipeline pipeline) {
        Proxy p = active();
        if (p == null || pipeline.get("hook_proxy") != null) return;
        if (pipeline.channel() instanceof io.netty.channel.local.LocalChannel) return; // singleplayer
        pipeline.addFirst("hook_proxy", new SocksProxyHandler(p));
    }

    private static String str(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    private static String protect(String s) {
        if (s == null || s.isEmpty()) return "";
        try {
            byte[] enc = com.sun.jna.platform.win32.Crypt32Util.cryptProtectData(s.getBytes(StandardCharsets.UTF_8));
            return "dpapi:" + Base64.getEncoder().encodeToString(enc);
        } catch (Throwable t) {
            return "b64:" + Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String unprotect(String s) {
        try {
            if (s.startsWith("dpapi:")) return new String(com.sun.jna.platform.win32.Crypt32Util.cryptUnprotectData(Base64.getDecoder().decode(s.substring(6))), StandardCharsets.UTF_8);
            if (s.startsWith("b64:")) return new String(Base64.getDecoder().decode(s.substring(4)), StandardCharsets.UTF_8);
        } catch (Throwable ignored) {
        }
        return s;
    }
}
