/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1044
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package hook.feature;

import hook.feature.BakedCharacterData;
import hook.feature.Character3dModule;
import hook.feature.CharacterAtlasTexture;
import hook.feature.CharacterDefinition;
import hook.feature.CharacterDetailLevel;
import hook.feature.CharacterMeshBaker;
import hook.feature.WornCharacterModel;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(value=EnvType.CLIENT)
public final class CharacterModelCache {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"HookVisuals");
    private static final int KEEP = 4;
    private static final ForkJoinPool POOL = new ForkJoinPool(Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() - 2)), var0 -> {
        ForkJoinWorkerThread var1 = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(var0);
        var1.setName("Hook Character Builder " + var1.getPoolIndex());
        var1.setDaemon(true);
        var1.setPriority(4);
        return var1;
    }, null, false);
    private static final Map<String, CacheEntry> ENTRIES = new LinkedHashMap<String, CacheEntry>(8, 0.75f, true);
    private static boolean listening;
    private static CacheEntry shown;

    private CharacterModelCache() {
    }

    static void listen() {
        listening = true;
    }

    public static void onResourceReload() {
        if (listening) {
            CharacterModelCache.releaseTextures();
        }
    }

    static void prepare(CharacterDefinition var0, CharacterDetailLevel var1) {
        String var2 = var0.id() + "/" + var1.name();
        if (!ENTRIES.containsKey(var2)) {
            CacheEntry var3 = new CacheEntry(var0, var1);
            ENTRIES.put(var2, var3);
            CharacterModelCache.trim();
            CompletableFuture.runAsync(() -> CharacterModelCache.build(var3), POOL);
        }
    }

    static WornCharacterModel ready(CharacterDefinition var0, CharacterDetailLevel var1) {
        String var2 = var0.id() + "/" + var1.name();
        CacheEntry var3 = ENTRIES.get(var2);
        if (var3 == null) {
            CacheEntry var5 = new CacheEntry(var0, var1);
            ENTRIES.put(var2, var5);
            CharacterModelCache.trim();
            CompletableFuture.runAsync(() -> CharacterModelCache.build(var5), POOL);
            return CharacterModelCache.previous();
        }
        if (var3.worn != null) {
            shown = var3;
            return var3.worn;
        }
        BakedCharacterData var4 = var3.model;
        if (var3.failed) {
            return null;
        }
        if (var4 == null) {
            return CharacterModelCache.previous();
        }
        class_310.method_1551().method_1531().method_4616(var3.texture, (class_1044)new CharacterAtlasTexture(var3.texture.toString(), var4.atlasWidth, var4.atlasHeight, var4.atlasMips));
        var3.worn = new WornCharacterModel(var4, var3.texture);
        shown = var3;
        return var3.worn;
    }

    private static WornCharacterModel previous() {
        CacheEntry var0 = shown;
        return var0 != null && ENTRIES.containsValue(var0) ? var0.worn : null;
    }

    private static void build(CacheEntry var0) {
        try {
            BakedCharacterData var1 = CharacterMeshBaker.build(var0.design, var0.detail);
            Logger var3 = LOGGER;
            String var4 = "[Hook] 3D Character '{}' ({}) built: {} triangles in {} ms";
            Object[] var5 = new Object[]{var0.design.name(), var0.detail.label, var1.triangles(), var1.buildMillis};
            var3.info(var4, var5);
            var0.model = var1;
        }
        catch (Throwable var2) {
            var0.failed = true;
            Logger var10000 = LOGGER;
            String var10001 = "[Hook] 3D Character '{}' ({}) failed to build";
            Object[] var10002 = new Object[]{var0.design.name(), var0.detail.label, var2};
            var10000.error(var10001, var10002);
            Character3dModule.failed(var0.design, var2);
        }
    }

    private static void trim() {
        Iterator<CacheEntry> var0 = ENTRIES.values().iterator();
        while (ENTRIES.size() > 4 && var0.hasNext()) {
            CacheEntry var1 = var0.next();
            CharacterModelCache.release(var1);
            var0.remove();
        }
    }

    static void releaseTextures() {
        for (CacheEntry var1 : ENTRIES.values()) {
            CharacterModelCache.release(var1);
        }
    }

    static void clear() {
        CharacterModelCache.releaseTextures();
        ENTRIES.clear();
        shown = null;
    }

    private static void release(CacheEntry var0) {
        if (var0.worn != null) {
            var0.worn = null;
            class_310.method_1551().method_1531().method_4615(var0.texture);
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class CacheEntry {
        final CharacterDefinition design;
        final CharacterDetailLevel detail;
        final class_2960 texture;
        volatile BakedCharacterData model;
        volatile boolean failed;
        WornCharacterModel worn;

        CacheEntry(CharacterDefinition var1, CharacterDetailLevel var2) {
            this.design = var1;
            this.detail = var2;
            this.texture = class_2960.method_60655((String)"hook", (String)("character/" + var1.id() + "_" + var2.name().toLowerCase(Locale.ROOT)));
        }
    }
}

