/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class MeshBufferBuilder {
    public final float[] pos;
    public final float[] nrm;
    public final float[] uv;
    public final int[] color;
    public final byte[] glow;
    public final float[] flat;
    public final int[] quads;
    public final int triangles;
    public float[] weight;
    public float[] joint;

    MeshBufferBuilder(float[] var1, float[] var2, float[] var3, int[] var4, byte[] var5, float[] var6, int[] var7, int var8) {
        this.pos = var1;
        this.nrm = var2;
        this.uv = var3;
        this.color = var4;
        this.glow = var5;
        this.flat = var6;
        this.quads = var7;
        this.triangles = var8;
    }

    public int vertexCount() {
        return this.pos.length / 3;
    }

    public int quadCount() {
        return this.quads.length / 4;
    }

    public boolean glows() {
        return this.glow != null;
    }

    public boolean flatLit() {
        return this.flat != null;
    }

    @Environment(value=EnvType.CLIENT)
    static final class MeshArrays {
        float[] pos = new float[768];
        float[] nrm = new float[768];
        float[] uv = new float[512];
        int[] color = new int[256];
        byte[] glow = new byte[256];
        float[] flat = new float[256];
        boolean anyGlow;
        boolean anyFlat;
        int verts;
        int[] quads = new int[1024];
        int quadCount;
        int triangles;

        MeshArrays() {
        }

        int vertex(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, boolean var10) {
            return this.vertex(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, 0.0f);
        }

        int vertex(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, boolean var10, float var11) {
            int var10000;
            if (this.verts == this.color.length) {
                int var12 = this.verts * 2;
                this.pos = Arrays.copyOf(this.pos, var12 * 3);
                this.nrm = Arrays.copyOf(this.nrm, var12 * 3);
                this.uv = Arrays.copyOf(this.uv, var12 * 2);
                this.color = Arrays.copyOf(this.color, var12);
                this.glow = Arrays.copyOf(this.glow, var12);
                this.flat = Arrays.copyOf(this.flat, var12);
            }
            int var13 = var10000 = this.verts++;
            this.pos[var13 * 3] = var1;
            this.pos[var13 * 3 + 1] = var2;
            this.pos[var13 * 3 + 2] = var3;
            this.nrm[var13 * 3] = var4;
            this.nrm[var13 * 3 + 1] = var5;
            this.nrm[var13 * 3 + 2] = var6;
            this.uv[var13 * 2] = var7;
            this.uv[var13 * 2 + 1] = var8;
            this.color[var13] = var9;
            this.glow[var13] = (byte)(var10 ? 1 : 0);
            this.flat[var13] = var11;
            this.anyGlow |= var10;
            this.anyFlat |= var11 > 0.0f;
            return var13;
        }

        void quad(int var1, int var2, int var3, int var4, int var5) {
            if (this.quadCount * 4 + 4 > this.quads.length) {
                this.quads = Arrays.copyOf(this.quads, this.quads.length * 2);
            }
            this.quads[this.quadCount * 4] = var1;
            this.quads[this.quadCount * 4 + 1] = var2;
            this.quads[this.quadCount * 4 + 2] = var3;
            this.quads[this.quadCount * 4 + 3] = var4;
            ++this.quadCount;
            this.triangles += var5;
        }

        MeshBufferBuilder build() {
            return this.verts != 0 && this.quadCount != 0 ? new MeshBufferBuilder(Arrays.copyOf(this.pos, this.verts * 3), Arrays.copyOf(this.nrm, this.verts * 3), Arrays.copyOf(this.uv, this.verts * 2), Arrays.copyOf(this.color, this.verts), this.anyGlow ? Arrays.copyOf(this.glow, this.verts) : null, this.anyFlat ? Arrays.copyOf(this.flat, this.verts) : null, Arrays.copyOf(this.quads, this.quadCount * 4), this.triangles) : null;
        }
    }
}

