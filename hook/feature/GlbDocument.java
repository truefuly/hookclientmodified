/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class GlbDocument {
    private static final int GLTF = 1179937895;
    private static final int JSON = 1313821514;
    private static final int BIN = 5130562;
    private static final int MAX_ELEMENTS = 0x2000000;
    final JsonObject json;
    private final ByteBuffer bin;

    private GlbDocument(JsonObject var1, ByteBuffer var2) {
        this.json = var1;
        this.bin = var2;
    }

    static GlbDocument read(byte[] var0) throws IOException {
        ByteBuffer var1 = ByteBuffer.wrap(var0).order(ByteOrder.LITTLE_ENDIAN);
        if (var0.length < 20 || var1.getInt(0) != 1179937895) {
            throw new IOException("not a .glb file");
        }
        if (var1.getInt(4) != 2) {
            throw new IOException("only glTF 2.0 files are supported");
        }
        int var2 = Math.min(var1.getInt(8), var0.length);
        int var3 = 12;
        JsonObject var4 = null;
        ByteBuffer var5 = ByteBuffer.allocate(0);
        while (var3 + 8 <= var2) {
            int var6 = var1.getInt(var3);
            int var7 = var1.getInt(var3 + 4);
            if (var6 < 0 || var6 > var2 - var3 - 8) {
                throw new IOException("the file is cut short");
            }
            if (var7 == 1313821514) {
                var4 = JsonParser.parseString((String)new String(var0, var3 + 8, var6, StandardCharsets.UTF_8)).getAsJsonObject();
            } else if (var7 == 5130562) {
                var5 = ByteBuffer.wrap(var0, var3 + 8, var6).slice().order(ByteOrder.LITTLE_ENDIAN);
            }
            var3 += 8 + var6;
        }
        if (var4 == null) {
            throw new IOException("the file has no glTF data");
        }
        JsonArray var8 = GlbDocument.array(var4, "extensionsRequired");
        if (!var8.isEmpty()) {
            throw new IOException("it needs " + String.valueOf(var8) + ", export it without compression");
        }
        return new GlbDocument(var4, var5);
    }

    static JsonArray array(JsonObject var0, String var1) {
        JsonElement var2 = var0.get(var1);
        return var2 != null && var2.isJsonArray() ? var2.getAsJsonArray() : new JsonArray();
    }

    static JsonObject object(JsonObject var0, String var1) {
        JsonElement var2 = var0.get(var1);
        return var2 != null && var2.isJsonObject() ? var2.getAsJsonObject() : new JsonObject();
    }

    static int integer(JsonObject var0, String var1, int var2) {
        JsonElement var3 = var0.get(var1);
        return var3 != null && var3.isJsonPrimitive() ? var3.getAsInt() : var2;
    }

    private static String string(JsonObject var0, String var1, String var2) {
        JsonElement var3 = var0.get(var1);
        return var3 != null && var3.isJsonPrimitive() ? var3.getAsString() : var2;
    }

    int nodes() {
        return GlbDocument.array(this.json, "nodes").size();
    }

    JsonObject node(int var1) {
        return GlbDocument.array(this.json, "nodes").get(var1).getAsJsonObject();
    }

    String nodeName(int var1) {
        return GlbDocument.string(this.node(var1), "name", "node" + var1);
    }

    private JsonObject material(int var1) {
        return var1 < 0 ? new JsonObject() : GlbDocument.array(this.json, "materials").get(var1).getAsJsonObject();
    }

    String materialName(int var1) {
        return GlbDocument.string(this.material(var1), "name", "material" + var1);
    }

    boolean translucent(int var1) {
        String var2 = GlbDocument.string(this.material(var1), "alphaMode", "OPAQUE");
        return var2.equals("BLEND") || var2.equals("MASK");
    }

    boolean doubleSided(int var1) {
        JsonElement var2 = this.material(var1).get("doubleSided");
        return var2 != null && var2.getAsBoolean();
    }

    private JsonObject baseColorTexture(int var1) {
        return GlbDocument.object(GlbDocument.object(this.material(var1), "pbrMetallicRoughness"), "baseColorTexture");
    }

    int texCoord(int var1) {
        return GlbDocument.integer(this.baseColorTexture(var1), "texCoord", 0);
    }

    float[] baseColorFactor(int var1) {
        JsonArray var2 = GlbDocument.array(GlbDocument.object(this.material(var1), "pbrMetallicRoughness"), "baseColorFactor");
        float[] var10000 = var2.size() != 4 ? new float[]{1.0f, 1.0f, 1.0f, 1.0f} : new float[]{var2.get(0).getAsFloat(), var2.get(1).getAsFloat(), var2.get(2).getAsFloat(), var2.get(3).getAsFloat()};
        return var10000;
    }

    byte[] baseColorImage(int var1) throws IOException {
        JsonObject var2 = this.baseColorTexture(var1);
        if (!var2.has("index")) {
            return null;
        }
        JsonObject var3 = GlbDocument.array(this.json, "textures").get(var2.get("index").getAsInt()).getAsJsonObject();
        int var4 = GlbDocument.integer(var3, "source", -1);
        if (var4 < 0) {
            return null;
        }
        JsonObject var5 = GlbDocument.array(this.json, "images").get(var4).getAsJsonObject();
        if (var5.has("view")) {
            ByteBuffer var8 = this.view(var5.get("view").getAsInt(), 0);
            byte[] var7 = new byte[var8.remaining()];
            var8.get(var7);
            return var7;
        }
        String var6 = GlbDocument.string(var5, "uri", "");
        if (var6.startsWith("data:")) {
            return Base64.getDecoder().decode(var6.substring(var6.indexOf(44) + 1));
        }
        throw new IOException("its textures are separate files, export it as a single .glb");
    }

    float[] floats(int var1) throws IOException {
        JsonObject var2 = this.accessor(var1);
        int var3 = var2.get("count").getAsInt();
        int var4 = GlbDocument.components(var2.get("type").getAsString());
        int var5 = var2.get("componentType").getAsInt();
        JsonElement var6 = var2.get("normalized");
        boolean var7 = var6 != null && var6.getAsBoolean();
        float[] var8 = new float[var3 * var4];
        if (!var2.has("view")) {
            return var8;
        }
        ByteBuffer var9 = this.view(var2.get("view").getAsInt(), GlbDocument.integer(var2, "byteOffset", 0));
        int var10 = this.stride(var2, var4, var5);
        int var11 = GlbDocument.size(var5);
        for (int var12 = 0; var12 < var3; ++var12) {
            for (int var13 = 0; var13 < var4; ++var13) {
                int var14 = var12 * var10 + var13 * var11;
                var8[var12 * var4 + var13] = switch (var5) {
                    case 5120 -> {
                        if (var7) {
                            yield Math.max((float)var9.get(var14) / 127.0f, -1.0f);
                        }
                        yield var9.get(var14);
                    }
                    case 5121 -> (float)(var9.get(var14) & 0xFF) / (var7 ? 255.0f : 1.0f);
                    case 5122 -> {
                        if (var7) {
                            yield Math.max((float)var9.getShort(var14) / 32767.0f, -1.0f);
                        }
                        yield var9.getShort(var14);
                    }
                    case 5123 -> (float)(var9.getShort(var14) & 0xFFFF) / (var7 ? 65535.0f : 1.0f);
                    default -> throw new IOException("unknown component type " + var5);
                    case 5125 -> var9.getInt(var14);
                    case 5126 -> var9.getFloat(var14);
                };
            }
        }
        return var8;
    }

    int[] ints(int var1) throws IOException {
        JsonObject var2 = this.accessor(var1);
        int var3 = var2.get("count").getAsInt();
        int var4 = GlbDocument.components(var2.get("type").getAsString());
        int var5 = var2.get("componentType").getAsInt();
        ByteBuffer var6 = this.view(var2.get("view").getAsInt(), GlbDocument.integer(var2, "byteOffset", 0));
        int var7 = this.stride(var2, var4, var5);
        int var8 = GlbDocument.size(var5);
        int[] var9 = new int[var3 * var4];
        for (int var10 = 0; var10 < var3; ++var10) {
            for (int var11 = 0; var11 < var4; ++var11) {
                int var12 = var10 * var7 + var11 * var8;
                var9[var10 * var4 + var11] = switch (var5) {
                    case 5121 -> var6.get(var12) & 0xFF;
                    default -> throw new IOException("unknown index type " + var5);
                    case 5123 -> var6.getShort(var12) & 0xFFFF;
                    case 5125 -> var6.getInt(var12);
                };
            }
        }
        return var9;
    }

    private JsonObject accessor(int var1) throws IOException {
        JsonObject var2 = GlbDocument.array(this.json, "accessors").get(var1).getAsJsonObject();
        if (var2.has("sparse")) {
            throw new IOException("sparse accessors aren't supported");
        }
        long var3 = (long)var2.get("count").getAsInt() * (long)GlbDocument.components(var2.get("type").getAsString());
        if (var3 >= 0L && var3 <= 0x2000000L) {
            return var2;
        }
        throw new IOException("a mesh is far too large");
    }

    private ByteBuffer view(int var1, int var2) throws IOException {
        JsonObject var3 = GlbDocument.array(this.json, "views").get(var1).getAsJsonObject();
        if (GlbDocument.integer(var3, "buffer", 0) != 0) {
            throw new IOException("its data is split over several files");
        }
        int var4 = GlbDocument.integer(var3, "byteOffset", 0) + var2;
        int var5 = var3.get("byteLength").getAsInt() - var2;
        if (var4 >= 0 && var5 >= 0 && var4 + var5 <= this.bin.capacity()) {
            return this.bin.slice(var4, var5).order(ByteOrder.LITTLE_ENDIAN);
        }
        throw new IOException("the file is cut short");
    }

    private int stride(JsonObject var1, int var2, int var3) {
        JsonObject var4 = GlbDocument.array(this.json, "views").get(var1.get("view").getAsInt()).getAsJsonObject();
        return GlbDocument.integer(var4, "byteStride", var2 * GlbDocument.size(var3));
    }

    private static int size(int var0) {
        return switch (var0) {
            case 5120, 5121 -> 1;
            case 5122, 5123 -> 2;
            default -> 4;
        };
    }

    private static int components(String var0) throws IOException {
        int var2 = -1;
        switch (var0.hashCode()) {
            case -1854860308: {
                if (!var0.equals("SCALAR")) break;
                var2 = 0;
                break;
            }
            case 2359028: {
                if (!var0.equals("MAT4")) break;
                var2 = 4;
                break;
            }
            case 2630462: {
                if (!var0.equals("VEC2")) break;
                var2 = 1;
                break;
            }
            case 2630463: {
                if (!var0.equals("VEC3")) break;
                var2 = 2;
                break;
            }
            case 2630464: {
                if (!var0.equals("VEC4")) break;
                var2 = 3;
            }
        }
        return switch (var2) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 4;
            case 4 -> 16;
            default -> throw new IOException("unknown accessor type " + var0);
        };
    }
}

