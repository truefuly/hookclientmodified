/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonParseException
 *  net.fabricmc.loader.api.FabricLoader
 */
package com.hookaddon.persist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.hookaddon.HookAddon;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

public final class HookStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private HookStore() {
    }

    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("hook-addon");
    }

    public static Path schematicsDir() {
        return HookStore.configDir().resolve("schematics");
    }

    private static Path buildStateFile() {
        return HookStore.configDir().resolve("build-state.json");
    }

    private static Path presetsFile() {
        return HookStore.configDir().resolve("mining-presets.json");
    }

    private static Path spawnerFile() {
        return HookStore.configDir().resolve("spawner-state.json");
    }

    public static void ensureDirectories() {
        try {
            Files.createDirectories(HookStore.schematicsDir(), new FileAttribute[0]);
        }
        catch (IOException e) {
            HookAddon.LOG.warn("Could not create the Hook Addon config folder: {}", (Object)e.getMessage());
        }
    }

    public static BuildState loadBuildState() {
        return HookStore.read(HookStore.buildStateFile(), BuildState.class, BuildState::new);
    }

    public static void saveBuildState(BuildState state) {
        HookStore.write(HookStore.buildStateFile(), state);
    }

    public static void clearBuildState() {
        HookStore.delete(HookStore.buildStateFile());
    }

    public static List<MiningPreset> loadPresets() {
        MiningPresetFile file = HookStore.read(HookStore.presetsFile(), MiningPresetFile.class, MiningPresetFile::new);
        return file.presets == null ? new ArrayList<MiningPreset>() : new ArrayList<MiningPreset>(file.presets);
    }

    public static void savePresets(List<MiningPreset> presets) {
        MiningPresetFile file = new MiningPresetFile();
        file.presets = presets;
        HookStore.write(HookStore.presetsFile(), file);
    }

    public static SpawnerState loadSpawnerState() {
        return HookStore.read(HookStore.spawnerFile(), SpawnerState.class, SpawnerState::new);
    }

    public static void saveSpawnerState(SpawnerState state) {
        if (state.visited != null && state.visited.size() > 40000) {
            state.visited = new ArrayList<Long>(state.visited.subList(state.visited.size() - 40000, state.visited.size()));
        }
        HookStore.write(HookStore.spawnerFile(), state);
    }

    private static <T> T read(Path file, Class<T> type, Supplier<T> defaults) {
        if (!Files.isRegularFile(file, new LinkOption[0])) {
            return defaults.get();
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            Object value = GSON.fromJson(json, type);
            return (T)(value == null ? defaults.get() : value);
        }
        catch (JsonParseException | IOException e) {
            HookAddon.LOG.warn("Could not read {}, starting from defaults: {}", (Object)file.getFileName(), (Object)e.getMessage());
            HookStore.quarantine(file);
            return defaults.get();
        }
    }

    private static void write(Path file, Object value) {
        try {
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            Path temp = file.resolveSibling(String.valueOf(file.getFileName()) + ".tmp");
            Files.writeString(temp, (CharSequence)GSON.toJson(value), StandardCharsets.UTF_8, new OpenOption[0]);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            HookAddon.LOG.warn("Could not write {}: {}", (Object)file.getFileName(), (Object)e.getMessage());
        }
    }

    private static void delete(Path file) {
        try {
            Files.deleteIfExists(file);
        }
        catch (IOException e) {
            HookAddon.LOG.warn("Could not delete {}: {}", (Object)file.getFileName(), (Object)e.getMessage());
        }
    }

    private static void quarantine(Path file) {
        try {
            Path target = file.resolveSibling(String.valueOf(file.getFileName()) + ".corrupt");
            Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
            HookAddon.LOG.warn("Kept the unreadable file as {}", (Object)target.getFileName());
        }
        catch (IOException e) {
            HookAddon.LOG.warn("Could not move the unreadable file aside: {}", (Object)e.getMessage());
        }
    }

    public static final class BuildState {
        public String schematicFile;
        public String blueprint;
        public String blueprintMaterial;
        public String anchor;
        public int originX;
        public int originY;
        public int originZ;
        public String mirror;
        public String rotation;
        public long savedAt;
        public String dimension;
    }

    private static final class MiningPresetFile {
        List<MiningPreset> presets = new ArrayList<MiningPreset>();

        private MiningPresetFile() {
        }
    }

    public static final class SpawnerState {
        public String dimension;
        public List<Long> visited = new ArrayList<Long>();
        public int rtpAttempts;
        public long lastRtpAt;
        public String lastStatus;
    }

    public static final class MiningPreset {
        public String name;
        public int originX;
        public int originY;
        public int originZ;
        public int width;
        public int height;
        public int depth;
        public String pattern;
        public List<String> mineBlocks = new ArrayList<String>();
        public List<String> ignoreBlocks = new ArrayList<String>();
        public long savedAt;
    }
}

