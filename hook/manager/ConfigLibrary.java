/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package hook.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hook.utils.BytecodeException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class ConfigLibrary {
    public static final int LIMIT = 5;
    private static final int MAX_BYTES = 0x100000;
    private static final int MAX_CODE = 131072;
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private final Supplier<JsonObject> capture;
    private final Consumer<JsonObject> apply;
    private final Consumer<JsonObject> validate;
    private final Runnable reset;
    private List<Profile> profiles = new ArrayList<Profile>();
    private String selected;
    private boolean loaded;

    public ConfigLibrary(Path var1, Supplier<JsonObject> var2, Consumer<JsonObject> var3, Consumer<JsonObject> var4, Runnable var5) {
        this.file = var1;
        this.capture = var2;
        this.apply = var3;
        this.validate = var4;
        this.reset = var5;
    }

    public synchronized List<Profile> profiles() {
        this.load();
        return List.copyOf(this.profiles);
    }

    public synchronized String selected() {
        this.load();
        return this.selected;
    }

    public synchronized void select(String var1) {
        this.load();
        this.find(var1);
        if (!Objects.equals(this.selected, var1)) {
            this.commit(this.profiles, var1);
        }
    }

    public synchronized String create(String var1) {
        this.load();
        this.capacity();
        var1 = ConfigLibrary.cleanName(var1);
        this.unique(var1, null);
        JsonObject var2 = this.capture.get().deepCopy();
        this.validate.accept(var2);
        ConfigLibrary.size(var2);
        Profile var3 = new Profile(UUID.randomUUID().toString(), var1, System.currentTimeMillis(), var2);
        ArrayList<Profile> var4 = new ArrayList<Profile>(this.profiles);
        var4.add(var3);
        this.commit(var4, var3.id());
        return var3.id();
    }

    public synchronized void saveCurrent(String var1) {
        this.load();
        Profile var2 = this.find(var1);
        JsonObject var3 = this.capture.get().deepCopy();
        this.validate.accept(var3);
        ConfigLibrary.size(var3);
        this.replace(new Profile(var2.id(), var2.name(), System.currentTimeMillis(), var3));
    }

    public synchronized void rename(String var1, String var2) {
        this.load();
        Profile var3 = this.find(var1);
        var2 = ConfigLibrary.cleanName(var2);
        this.unique(var2, var1);
        this.replace(new Profile(var1, var2, var3.updated(), var3.data()));
    }

    public synchronized void delete(String var1) {
        this.load();
        this.find(var1);
        ArrayList<Profile> var2 = new ArrayList<Profile>(this.profiles);
        var2.removeIf(var1x -> ((Profile)var1x).id().equals(var1));
        String var3 = Objects.equals(this.selected, var1) ? (var2.isEmpty() ? null : var2.get(0).id()) : this.selected;
        this.commit(var2, var3);
    }

    public synchronized void loadConfig(String var1) {
        this.load();
        Profile var2 = this.find(var1);
        this.validate.accept(var2.data());
        this.apply.accept(var2.data().deepCopy());
        this.commit(this.profiles, var1);
    }

    public void resetCurrent() {
        this.reset.run();
    }

    public synchronized String copyCode(String var1) {
        this.load();
        Profile var2 = this.find(var1);
        JsonObject var3 = new JsonObject();
        var3.addProperty("version", (Number)1);
        var3.addProperty("name", var2.name());
        var3.add("settings", (JsonElement)var2.data());
        try {
            byte[] var4 = var3.toString().getBytes(StandardCharsets.UTF_8);
            if (var4.length > 0x100000) {
                throw new IllegalArgumentException("Config is too large to share.");
            }
            ByteArrayOutputStream var5 = new ByteArrayOutputStream();
            try (GZIPOutputStream var6 = new GZIPOutputStream(var5);){
                var6.write(var4);
            }
            byte[] var12 = var5.toByteArray();
            String var7 = "KRSP1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(var12) + "." + ConfigLibrary.digest(var12);
            if (var7.length() > 131072) {
                throw new IllegalArgumentException("Config is too large to share.");
            }
            return var7;
        }
        catch (IOException var11) {
            throw new IllegalStateException("Couldn't create the config code.", var11);
        }
    }

    public synchronized String pasteCode(String var1) {
        this.load();
        this.capacity();
        JsonObject var2 = ConfigLibrary.decode(var1);
        String var3 = ConfigLibrary.cleanName(var2.get("name").getAsString());
        String var4 = var3;
        int var5 = 2;
        while (this.taken((String)var3, null)) {
            String var6 = " (" + var5 + ")";
            var3 = var4.substring(0, Math.min(var4.length(), 32 - var6.length())) + var6;
            ++var5;
        }
        JsonObject var8 = var2.getAsJsonObject("settings");
        this.validate.accept(var8);
        Profile var9 = new Profile(UUID.randomUUID().toString(), (String)var3, System.currentTimeMillis(), var8.deepCopy());
        ArrayList<Profile> var7 = new ArrayList<Profile>(this.profiles);
        var7.add(var9);
        this.commit(var7, var9.id());
        return var9.id();
    }

    private static JsonObject decode(String var0) {
        try {
            if (var0 != null && var0.length() <= 131072) {
                String var1 = var0.strip();
                if (!var1.startsWith("KRSP1:")) {
                    throw new IllegalArgumentException();
                }
                int var2 = var1.lastIndexOf(46);
                if (var2 >= 6 && var1.length() - var2 == 17) {
                    byte[] var4;
                    byte[] var3 = Base64.getUrlDecoder().decode(var1.substring(6, var2));
                    if (!MessageDigest.isEqual(ConfigLibrary.digest(var3).getBytes(StandardCharsets.US_ASCII), var1.substring(var2 + 1).getBytes(StandardCharsets.US_ASCII))) {
                        throw new IllegalArgumentException();
                    }
                    try (GZIPInputStream var5 = new GZIPInputStream(new ByteArrayInputStream(var3));){
                        var4 = var5.readNBytes(0x100001);
                        if (var4.length > 0x100000 || var5.read() != -1) {
                            throw new IllegalArgumentException();
                        }
                    }
                    JsonObject var11 = ConfigLibrary.parse(new String(var4, StandardCharsets.UTF_8));
                    if (var11.get("version").getAsInt() == 1 && var11.get("name").getAsJsonPrimitive().isString() && var11.get("settings").isJsonObject()) {
                        return var11;
                    }
                    throw new IllegalArgumentException();
                }
                throw new IllegalArgumentException();
            }
            throw new IllegalArgumentException();
        }
        catch (Exception var10) {
            throw new IllegalArgumentException("Invalid config code. Copy the complete code and try again.");
        }
    }

    private static String digest(byte[] var0) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(var0), 0, 8);
        }
        catch (Exception var2) {
            throw new IllegalStateException(var2);
        }
    }

    private static JsonObject parse(String var0) {
        int var1 = 0;
        boolean var2 = false;
        boolean var3 = false;
        for (int var4 = 0; var4 < var0.length(); ++var4) {
            char var5 = var0.charAt(var4);
            if (var2) {
                if (var3) {
                    var3 = false;
                    continue;
                }
                if (var5 == '\\') {
                    var3 = true;
                    continue;
                }
                if (var5 != '\"') continue;
                var2 = false;
                continue;
            }
            if (var5 == '\"') {
                var2 = true;
                continue;
            }
            if (var5 == '{' || var5 == '[') {
                if (++var1 <= 32) continue;
                throw new IllegalArgumentException("Config data is nested too deeply.");
            }
            if (var5 != '}' && var5 != ']') continue;
            --var1;
        }
        return JsonParser.parseString((String)var0).getAsJsonObject();
    }

    public static String cleanName(String var0) {
        if (var0 == null) {
            throw new IllegalArgumentException("Enter a config name.");
        }
        String var1 = var0.strip().replaceAll("\\s+", " ");
        if (!var1.isEmpty() && var1.length() <= 32 && !var1.chars().anyMatch(Character::isISOControl)) {
            return var1;
        }
        throw new IllegalArgumentException("Use a name between 1 and 32 characters.");
    }

    private void capacity() {
        if (this.profiles.size() >= 5) {
            throw new IllegalArgumentException("You already have five configs. Delete one first.");
        }
    }

    private static void size(JsonObject var0) {
        if (var0.toString().getBytes(StandardCharsets.UTF_8).length > 0x100000) {
            throw new IllegalArgumentException("This config is too large to save.");
        }
    }

    private boolean taken(String var1, String var2) {
        return this.profiles.stream().anyMatch(var2x -> !Objects.equals(var2x.id(), var2) && var2x.name().equalsIgnoreCase(var1));
    }

    private void unique(String var1, String var2) {
        if (this.taken(var1, var2)) {
            throw new IllegalArgumentException("A config with that name already exists.");
        }
    }

    private Profile find(String var1) {
        return this.profiles.stream().filter(var1x -> var1x.id().equals(var1)).findFirst().orElseThrow(() -> new IllegalArgumentException("Select a config first."));
    }

    private void replace(Profile var1) {
        ArrayList<Profile> var2 = new ArrayList<Profile>(this.profiles);
        for (int var3 = 0; var3 < var2.size(); ++var3) {
            if (!var2.get(var3).id().equals(var1.id())) continue;
            var2.set(var3, var1);
        }
        this.commit(var2, this.selected);
    }

    private void load() {
        if (!this.loaded) {
            if (Files.isRegularFile(this.file, new LinkOption[0])) {
                try {
                    if (Files.size(this.file) > 0x1400000L) {
                        throw BytecodeException.unchecked(new IOException("Oversized config library"));
                    }
                    JsonObject var1 = ConfigLibrary.parse(Files.readString(this.file));
                    if (var1.get("version").getAsInt() != 1) {
                        throw BytecodeException.unchecked(new IOException("Unsupported config version"));
                    }
                    JsonArray var2 = var1.getAsJsonArray("configs");
                    if (var2.size() > 5) {
                        throw BytecodeException.unchecked(new IOException("Too many configs"));
                    }
                    ArrayList<Profile> var3 = new ArrayList<Profile>();
                    HashSet<String> var4 = new HashSet<String>();
                    HashSet<String> var5 = new HashSet<String>();
                    for (JsonElement var7 : var2) {
                        JsonObject var8 = var7.getAsJsonObject();
                        String var9 = var8.get("id").getAsString();
                        UUID.fromString(var9);
                        String var10 = ConfigLibrary.cleanName(var8.get("name").getAsString());
                        if (!var4.add(var9) || !var5.add(var10.toLowerCase(Locale.ROOT))) {
                            throw BytecodeException.unchecked(new IOException("Duplicate config"));
                        }
                        JsonObject var11 = var8.getAsJsonObject("data");
                        ConfigLibrary.size(var11);
                        var3.add(new Profile(var9, var10, var8.get("updated").getAsLong(), var11.deepCopy()));
                    }
                    String var13 = var1.has("selected") && !var1.get("selected").isJsonNull() ? var1.get("selected").getAsString() : null;
                    this.profiles = var3;
                    this.selected = var4.contains(var13) ? var13 : (var3.isEmpty() ? null : var3.get(0).id());
                }
                catch (Exception var12) {
                    throw new IllegalStateException("Couldn't read saved configs. Your file was kept safe.", var12);
                }
            }
            this.loaded = true;
        }
    }

    private void commit(List<Profile> var1, String var2) {
        JsonObject var3 = new JsonObject();
        var3.addProperty("version", (Number)1);
        var3.addProperty("selected", var2);
        JsonArray var4 = new JsonArray();
        for (Profile var6 : var1) {
            JsonObject var7 = new JsonObject();
            var7.addProperty("id", var6.id());
            var7.addProperty("name", var6.name());
            var7.addProperty("updated", (Number)var6.updated());
            var7.add("data", (JsonElement)var6.data());
            var4.add((JsonElement)var7);
        }
        var3.add("configs", (JsonElement)var4);
        try {
            ConfigLibrary.writeAtomic(this.file, JSON.toJson((JsonElement)var3));
        }
        catch (IOException var8) {
            throw new IllegalStateException("Couldn't save configs. Check the config folder.", var8);
        }
        this.profiles = new ArrayList<Profile>(var1);
        this.selected = var2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void writeAtomic(Path var0, String var1) throws IOException {
        Files.createDirectories(var0.getParent(), new FileAttribute[0]);
        Path var2 = Files.createTempFile(var0.getParent(), var0.getFileName().toString(), ".tmp", new FileAttribute[0]);
        try {
            Files.writeString(var2, (CharSequence)var1, StandardCharsets.UTF_8, new OpenOption[0]);
            try {
                Files.move(var2, var0, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException var7) {
                Files.move(var2, var0, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally {
            Files.deleteIfExists(var2);
        }
    }

    public record Profile(String id, String name, long updated, JsonObject data) {
        public int enabledCount() {
            int var1 = 0;
            for (Map.Entry var3 : this.data.entrySet()) {
                JsonElement var4;
                if (!((JsonElement)var3.getValue()).isJsonObject() || (var4 = ((JsonElement)var3.getValue()).getAsJsonObject().get("enabled")) == null || !var4.isJsonPrimitive() || !var4.getAsJsonPrimitive().isBoolean() || !var4.getAsBoolean()) continue;
                ++var1;
            }
            return var1;
        }
    }
}

