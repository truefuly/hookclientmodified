/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package hook.halloween.witch;

import hook.utils.BytecodeException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.joml.Matrix4f;

public final class WitchAsset {
    public final String[] jointNames;
    public final int[] parent;
    public final Matrix4f[] bind;
    public final Mesh witch;
    public final Mesh broom;
    private final Map<String, Integer> byName = new HashMap<String, Integer>();

    private WitchAsset(String[] var1, int[] var2, Matrix4f[] var3, Mesh var4, Mesh var5) {
        this.jointNames = var1;
        this.parent = var2;
        this.bind = var3;
        this.witch = var4;
        this.broom = var5;
        for (int var6 = 0; var6 < var1.length; ++var6) {
            this.byName.put(var1[var6], var6);
        }
    }

    public int joint(String var1) {
        Integer var2 = this.byName.get(var1);
        return var2 == null ? -1 : var2;
    }

    public static WitchAsset load(InputStream var0) throws IOException {
        WitchAsset var13;
        try (DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(var0), 65536));){
            if (var1.readInt() != 1264012593) {
                throw BytecodeException.unchecked(new IOException("not a witch model"));
            }
            int var2 = var1.readShort();
            String[] var3 = new String[var2];
            int[] var4 = new int[var2];
            Matrix4f[] var5 = new Matrix4f[var2];
            for (int var6 = 0; var6 < var2; ++var6) {
                var3[var6] = var1.readUTF();
                var4[var6] = var1.readShort();
                float[] var7 = new float[16];
                for (int var8 = 0; var8 < 16; ++var8) {
                    var7[var8] = var1.readFloat();
                }
                var5[var6] = new Matrix4f().set(var7);
            }
            Mesh var11 = WitchAsset.readMesh(var1);
            Mesh var12 = WitchAsset.readMesh(var1);
            var13 = new WitchAsset(var3, var4, var5, var11, var12);
        }
        return var13;
    }

    private static Mesh readMesh(DataInputStream var0) throws IOException {
        int var1 = var0.readInt();
        float[] var2 = new float[var1 * 3];
        byte[] var3 = new byte[var1 * 3];
        float[] var4 = new float[var1 * 2];
        byte[] var5 = new byte[var1 * 4];
        short[] var6 = new short[var1 * 4];
        for (int var7 = 0; var7 < var1; ++var7) {
            var2[var7 * 3] = var0.readFloat();
            var2[var7 * 3 + 1] = var0.readFloat();
            var2[var7 * 3 + 2] = var0.readFloat();
            var3[var7 * 3] = var0.readByte();
            var3[var7 * 3 + 1] = var0.readByte();
            var3[var7 * 3 + 2] = var0.readByte();
            var4[var7 * 2] = var0.readFloat();
            var4[var7 * 2 + 1] = var0.readFloat();
            for (int var8 = 0; var8 < 4; ++var8) {
                var5[var7 * 4 + var8] = var0.readByte();
            }
            for (int var11 = 0; var11 < 4; ++var11) {
                var6[var7 * 4 + var11] = var0.readShort();
            }
        }
        int var10 = var0.readInt();
        Mesh var12 = new Mesh(var1, var10);
        System.arraycopy(var2, 0, var12.pos, 0, var2.length);
        System.arraycopy(var3, 0, var12.normal, 0, var3.length);
        System.arraycopy(var4, 0, var12.uv, 0, var4.length);
        System.arraycopy(var5, 0, var12.joints, 0, var5.length);
        System.arraycopy(var6, 0, var12.weights, 0, var6.length);
        for (int var9 = 0; var9 < var10; ++var9) {
            var12.indices[var9] = var0.readInt();
        }
        return var12;
    }

    public static final class Mesh {
        public final int vertexCount;
        public final float[] pos;
        public final byte[] normal;
        public final float[] uv;
        public final byte[] joints;
        public final short[] weights;
        public final int[] indices;

        Mesh(int var1, int var2) {
            this.vertexCount = var1;
            this.pos = new float[var1 * 3];
            this.normal = new byte[var1 * 3];
            this.uv = new float[var1 * 2];
            this.joints = new byte[var1 * 4];
            this.weights = new short[var1 * 4];
            this.indices = new int[var2];
        }
    }
}

