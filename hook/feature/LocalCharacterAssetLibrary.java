/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.loader.api.FabricLoader
 *  org.lwjgl.stb.STBImage
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package hook.feature;

import hook.feature.BakedCharacterAsset;
import hook.feature.Character3dModule;
import hook.feature.GltfCharacterBaker;
import hook.feature.UiTextureAtlas;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(value=EnvType.CLIENT)
final class LocalCharacterAssetLibrary {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"HookVisuals");
    private static final long MAX_FILE = 0x10000000L;
    private static final int MAX_TEXTURE = 8192;

    private LocalCharacterAssetLibrary() {
    }

    static Path folder() {
        return FabricLoader.getInstance().getConfigDir().resolve("hook").resolve("characters");
    }

    private static Path cache() {
        return LocalCharacterAssetLibrary.folder().resolve(".baked");
    }

    static List<BakedCharacterAsset> scan() {
        ArrayList<Path> var0 = new ArrayList<Path>();
        try (DirectoryStream<Path> var1 = Files.newDirectoryStream(LocalCharacterAssetLibrary.folder(), "*.{glb,GLB}");){
            for (Path var3Object : var1) {
                Path path = var3Object;
                if (!Files.isRegularFile(path, new LinkOption[0])) continue;
                var0.add(path);
            }
        }
        catch (IOException var11) {
            return List.of();
        }
        var0.sort(null);
        ArrayList<BakedCharacterAsset> var12 = new ArrayList<BakedCharacterAsset>();
        HashSet<String> var13 = new HashSet<String>();
        for (Object e : var0) {
            Path var4 = (Path)e;
            String var5 = var4.getFileName().toString();
            var5 = var5.substring(0, var5.length() - 4);
            try {
                String var6 = var5 + Files.size(var4) + Files.getLastModifiedTime(var4, new LinkOption[0]).toMillis() + "3";
                String var7 = "custom_" + LocalCharacterAssetLibrary.slug(var5) + "_" + Integer.toHexString(var6.hashCode());
                var13.add(var7);
                var12.add(new BakedCharacterAsset(var7, var5, () -> LocalCharacterAssetLibrary.open(var4, var7)));
            }
            catch (IOException var9) {
                LOGGER.warn("[Hook] Skipping custom character {}: {}", (Object)var4, (Object)var9.toString());
            }
        }
        LocalCharacterAssetLibrary.prune(var13);
        return var12;
    }

    private static String slug(String var0) {
        String var1 = var0.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return var1.isEmpty() ? "model" : (var1.length() > 32 ? var1.substring(0, 32) : var1);
    }

    private static void prune(Set<String> var0) {
        try (DirectoryStream<Path> var1 = Files.newDirectoryStream(LocalCharacterAssetLibrary.cache(), "*.anc");){
            for (Path var3Object : var1) {
                Path var3 = var3Object;
                String var4 = var3.getFileName().toString().replace(".anc", "");
                if (var0.contains(var4)) continue;
                Files.deleteIfExists(var3);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static synchronized InputStream open(Path var0, String var1) throws IOException {
        Path var2 = LocalCharacterAssetLibrary.cache().resolve(var1 + ".anc");
        if (Files.exists(var2, new LinkOption[0])) {
            return Files.newInputStream(var2, new OpenOption[0]);
        }
        if (Files.size(var0) > 0x10000000L) {
            throw new IOException("the file is larger than 256 MB");
        }
        String var3 = var0.getFileName().toString();
        Character3dModule.baking(var3);
        long var4 = System.nanoTime();
        GltfCharacterBaker.BakedCharacterModel var6 = GltfCharacterBaker.bake(Files.readAllBytes(var0), new GltfCharacterBaker.CharacterProfile(), LocalCharacterAssetLibrary::decode);
        Logger var10000 = LOGGER;
        String var10001 = "[Hook] Baked custom character {} in {} ms, moving {}";
        Object[] var10002 = new Object[]{var3, (System.nanoTime() - var4) / 1000000L, var6.limbs()};
        var10000.info(var10001, var10002);
        if (var6.limbs().size() < 3) {
            Character3dModule.notice(var3, var6.limbs());
        }
        Files.createDirectories(LocalCharacterAssetLibrary.cache(), new FileAttribute[0]);
        Path var7 = LocalCharacterAssetLibrary.cache().resolve(var1 + ".tmp");
        Files.write(var7, var6.data(), new OpenOption[0]);
        CopyOption[] var8 = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
        Files.move(var7, var2, var8);
        return new ByteArrayInputStream(var6.data());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static UiTextureAtlas.TexturePixels decode(byte[] var0) throws IOException {
        UiTextureAtlas.TexturePixels var2;
        ByteBuffer var1 = MemoryUtil.memAlloc((int)var0.length);
        try (MemoryStack var3 = MemoryStack.stackPush();){
            var1.put(var0).flip();
            IntBuffer var4 = var3.mallocInt(1);
            IntBuffer var5 = var3.mallocInt(1);
            IntBuffer var6 = var3.mallocInt(1);
            ByteBuffer var7 = STBImage.stbi_load_from_memory((ByteBuffer)var1, (IntBuffer)var4, (IntBuffer)var5, (IntBuffer)var6, (int)4);
            if (var7 == null) {
                throw new IOException("a texture can't be read: " + STBImage.stbi_failure_reason());
            }
            try {
                int var8 = var4.get(0);
                int var9 = var5.get(0);
                if (var8 > 8192 || var9 > 8192) {
                    throw new IOException("a texture is over 8192 px");
                }
                int[] var10 = new int[var8 * var9];
                for (int var11 = 0; var11 < var10.length; ++var11) {
                    int var12 = var7.get(var11 * 4) & 0xFF;
                    int var13 = var7.get(var11 * 4 + 1) & 0xFF;
                    int var14 = var7.get(var11 * 4 + 2) & 0xFF;
                    int var15 = var7.get(var11 * 4 + 3) & 0xFF;
                    var10[var11] = var15 << 24 | var12 << 16 | var13 << 8 | var14;
                }
                var2 = new UiTextureAtlas.TexturePixels(var8, var9, var10);
            }
            finally {
                STBImage.stbi_image_free((ByteBuffer)var7);
            }
        }
        finally {
            MemoryUtil.memFree((Buffer)var1);
        }
        return var2;
    }
}

