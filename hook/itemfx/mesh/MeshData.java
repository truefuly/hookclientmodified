/*
 * Decompiled with CFR 0.152.
 */
package hook.itemfx.mesh;

public final class MeshData {
    public final int vertexCount;
    public final float[] pos;
    public final float[] nrm;
    public final float[] uv;
    public final int[] rgba;
    public final short[] mat;
    public final short[] region;
    public final short[] p1;
    public final short[] p2;
    public final int[] indices;

    public MeshData(int var1, int var2) {
        this.vertexCount = var1;
        this.pos = new float[var1 * 3];
        this.nrm = new float[var1 * 3];
        this.uv = new float[var1 * 2];
        this.rgba = new int[var1];
        this.mat = new short[var1];
        this.region = new short[var1];
        this.p1 = new short[var1];
        this.p2 = new short[var1];
        this.indices = new int[var2 * 3];
    }

    public int triangleCount() {
        return this.indices.length / 3;
    }

    public static MeshData join(MeshData ... var0) {
        int var1 = 0;
        int var2 = 0;
        for (MeshData var6 : var0) {
            var1 += var6.vertexCount;
            var2 += var6.triangleCount();
        }
        MeshData var11 = new MeshData(var1, var2);
        int var12 = 0;
        int var13 = 0;
        for (MeshData var9 : var0) {
            System.arraycopy(var9.pos, 0, var11.pos, var12 * 3, var9.vertexCount * 3);
            System.arraycopy(var9.nrm, 0, var11.nrm, var12 * 3, var9.vertexCount * 3);
            System.arraycopy(var9.uv, 0, var11.uv, var12 * 2, var9.vertexCount * 2);
            System.arraycopy(var9.rgba, 0, var11.rgba, var12, var9.vertexCount);
            System.arraycopy(var9.mat, 0, var11.mat, var12, var9.vertexCount);
            System.arraycopy(var9.region, 0, var11.region, var12, var9.vertexCount);
            System.arraycopy(var9.p1, 0, var11.p1, var12, var9.vertexCount);
            System.arraycopy(var9.p2, 0, var11.p2, var12, var9.vertexCount);
            for (int var10 = 0; var10 < var9.indices.length; ++var10) {
                var11.indices[var13 + var10] = var9.indices[var10] + var12;
            }
            var12 += var9.vertexCount;
            var13 += var9.indices.length;
        }
        return var11;
    }
}

