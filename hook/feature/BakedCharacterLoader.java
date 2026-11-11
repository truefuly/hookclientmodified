/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.BakedCharacterAsset;
import hook.feature.BakedCharacterData;
import hook.feature.BakedCharacterPart;
import hook.feature.CharacterBone;
import hook.feature.CharacterDetailLevel;
import hook.feature.CharacterMeshBaker;
import hook.feature.CharacterTextureAtlasPacker;
import hook.feature.MeshBufferBuilder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.zip.InflaterInputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class BakedCharacterLoader {
    public static final int MAGIC = 1095648072;
    public static final int VERSION = 3;
    private static final int SHIPPED_VERSION = 4;

    private BakedCharacterLoader() {
    }

    static BakedCharacterData load(BakedCharacterAsset asset, CharacterDetailLevel detailLevel) {
        long startNanos = System.nanoTime();
        try {
            BakedCharacterData var45;
            try (InputStream stream = asset.open();){
                DataInputStream input = new DataInputStream(new BufferedInputStream(stream));
                int version = input.readInt() == 1095648072 ? input.readInt() : -1;
                if (version != 3 && version != 4) {
                    throw new IllegalStateException(asset.id() + ": not a version 3 baked character");
                }
                int atlasWidth = 0;
                int atlasHeight = 0;
                int[] atlasPixels = null;
                if (version == 4) {
                    atlasWidth = input.readInt();
                    atlasHeight = input.readInt();
                    byte[] rgba = new byte[atlasWidth * atlasHeight << 2];
                    input.readFully(rgba);
                    atlasPixels = BakedCharacterLoader.unfilter(rgba, atlasWidth, atlasHeight);
                }
                float minY = input.readFloat();
                float maxY = input.readFloat();
                float hipY = input.readFloat();
                float scale = input.readFloat();
                boolean firstPersonArms = input.readBoolean();
                boolean flags = input.readBoolean();
                int detailCount = input.readInt();
                int selectedDetail = Math.min(detailLevel.ordinal(), detailCount - 1);
                BakedCharacterPart[] parts = new BakedCharacterPart[input.readInt()];
                for (int partIndex = 0; partIndex < parts.length; ++partIndex) {
                    CharacterBone bone = CharacterBone.values()[input.readByte()];
                    float pivotX = input.readFloat();
                    float pivotY = input.readFloat();
                    float pivotZ = input.readFloat();
                    float gripX = input.readFloat();
                    float gripY = input.readFloat();
                    float gripZ = input.readFloat();
                    float[] bounds = new float[6];
                    for (int coordinate = 0; coordinate < bounds.length; ++coordinate) {
                        bounds[coordinate] = input.readFloat();
                    }
                    float swingScale = input.readFloat();
                    float swingLimit = input.readFloat();
                    boolean jointed = input.readBoolean();
                    float jointX = 0.0f;
                    float jointY = 0.0f;
                    float jointZ = 0.0f;
                    if (jointed) {
                        jointX = input.readFloat() - pivotX;
                        jointY = input.readFloat() - pivotY;
                        jointZ = input.readFloat() - pivotZ;
                    }
                    MeshBufferBuilder baseMesh = null;
                    MeshBufferBuilder outlineMesh = null;
                    for (int level = 0; level < detailCount; ++level) {
                        MeshBufferBuilder base = BakedCharacterLoader.surface(input, bounds, pivotX, pivotY, pivotZ, scale, flags, jointed, level == selectedDetail);
                        MeshBufferBuilder outline = BakedCharacterLoader.surface(input, bounds, pivotX, pivotY, pivotZ, scale, flags, jointed, level == selectedDetail);
                        if (level != selectedDetail) continue;
                        baseMesh = base;
                        outlineMesh = outline;
                    }
                    parts[partIndex] = new BakedCharacterPart(bone.name(), bone, -1, false, pivotX, pivotY, pivotZ, gripX, gripY, gripZ, null, baseMesh, null, outlineMesh, -1, swingScale, swingLimit, jointed, jointX, jointY, jointZ, false, 0.0f, 0.0f, 0.0f);
                }
                if (version == 3) {
                    atlasWidth = input.readInt();
                    atlasHeight = input.readInt();
                    byte[] compressed = new byte[input.readInt()];
                    input.readFully(compressed);
                    atlasPixels = BakedCharacterLoader.pixels(compressed, atlasWidth, atlasHeight);
                }
                long buildMillis = (System.nanoTime() - startNanos) / 1000000L;
                var45 = new BakedCharacterData(asset.id(), asset.name(), detailLevel, 1.0f, parts, 0, atlasWidth, atlasHeight, CharacterTextureAtlasPacker.mips(atlasPixels, atlasWidth, atlasHeight), 0.0f, 0.0f, minY, maxY, hipY, firstPersonArms, buildMillis);
            }
            return var45;
        }
        catch (IOException var41) {
            throw new UncheckedIOException(asset.name() + ": " + var41.getMessage(), var41);
        }
    }

    private static int[] pixels(byte[] var0, int var1, int var2) throws IOException {
        byte[] var3 = new byte[var1 * var2 * 4];
        try (InflaterInputStream var4 = new InflaterInputStream(new ByteArrayInputStream(var0));){
            int var5 = var4.readNBytes(var3, 0, var3.length);
            if (var5 != var3.length) {
                throw new IOException("atlas is truncated");
            }
        }
        return BakedCharacterLoader.unfilter(var3, var1, var2);
    }

    private static int[] unfilter(byte[] var0, int var1, int var2) {
        int[] var3 = new int[var1 * var2];
        for (int var4 = 0; var4 < var3.length; ++var4) {
            int var5 = var4 % var1 == 0 ? 0 : var3[var4 - 1];
            int var6 = 0;
            for (int var7 = 0; var7 < 4; ++var7) {
                int var8 = 24 - var7 * 8;
                var6 |= (var0[var4 * 4 + var7] + (var5 >>> var8) & 0xFF) << var8;
            }
            var3[var4] = var6;
        }
        return var3;
    }

    private static MeshBufferBuilder surface(DataInputStream var0, float[] var1, float var2, float var3, float var4, float var5, boolean var6, boolean var7, boolean var8) throws IOException {
        int var9 = var0.readInt();
        if (!var8) {
            var0.skipNBytes((long)var9 * (13L + (var6 ? 3L : 0L) + (var7 ? 1L : 0L)));
            var0.skipNBytes((long)var0.readInt() * (var9 > 65535 ? 12L : 6L));
            return null;
        }
        float[] var10 = new float[var9 * 3];
        for (int var11 = 0; var11 < var10.length; ++var11) {
            int var12 = var11 % 3;
            var10[var11] = var1[var12] + (float)var0.readChar() / 65535.0f * (var1[var12 + 3] - var1[var12]);
        }
        byte[] var23 = new byte[var9 * 3];
        var0.readFully(var23);
        float[] var24 = new float[var9 * 2];
        for (int var13 = 0; var13 < var24.length; ++var13) {
            var24[var13] = (float)var0.readChar() / 65535.0f;
        }
        int[] var25 = new int[var9];
        for (int var14 = 0; var14 < var9; ++var14) {
            var25[var14] = var6 ? 0xFF000000 | var0.readUnsignedByte() << 16 | var0.readUnsignedByte() << 8 | var0.readUnsignedByte() : -1;
        }
        float[] var26 = null;
        if (var7) {
            var26 = new float[var9];
            for (int var15 = 0; var15 < var9; ++var15) {
                var26[var15] = (float)var0.readUnsignedByte() / 255.0f;
            }
        }
        int var27 = var0.readInt();
        int[] var16 = new int[var27 * 3];
        for (int var17 = 0; var17 < var16.length; ++var17) {
            var16[var17] = var9 > 65535 ? var0.readInt() : (int)var0.readChar();
        }
        if (var9 != 0 && var27 != 0) {
            MeshBufferBuilder.MeshArrays var28 = new MeshBufferBuilder.MeshArrays();
            for (int var18 = 0; var18 < var9; ++var18) {
                float var19 = (float)var23[var18 * 3] / 127.0f;
                float var20 = (float)var23[var18 * 3 + 1] / 127.0f;
                float var21 = (float)var23[var18 * 3 + 2] / 127.0f;
                float var22 = (float)Math.sqrt(var19 * var19 + var20 * var20 + var21 * var21);
                if (var22 > 1.0E-6f) {
                    var19 /= var22;
                    var20 /= var22;
                    var21 /= var22;
                }
                var28.vertex(var10[var18 * 3] - var2, var10[var18 * 3 + 1] - var3, var10[var18 * 3 + 2] - var4, var19, var20, var21, var24[var18 * 2], var24[var18 * 2 + 1], var25[var18], false, var5);
            }
            CharacterMeshBaker.pair(var16, var27, var9, var28);
            MeshBufferBuilder var29 = var28.build();
            if (var29 != null && var26 != null) {
                float[] var30 = var26;
                int var31 = var26.length;
                for (int var32 = 0; var32 < var31; ++var32) {
                    float var33 = var30[var32];
                    if (!(var33 > 0.0f)) continue;
                    var29.joint = var26;
                    break;
                }
            }
            return var29;
        }
        return null;
    }
}

