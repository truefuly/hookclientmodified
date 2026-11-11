/*
 * Decompiled with CFR 0.152.
 */
package hook.itemfx.mesh;

import hook.itemfx.mesh.Mat;
import hook.itemfx.mesh.MeshData;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;

public final class SpriteModels {
    public static final int SPOOKY = 1;
    public static final int CHROME = 2;
    public static final int GHOST = 3;
    private static int[] atlas;

    private SpriteModels() {
    }

    static synchronized int[] atlas() {
        if (atlas == null) {
            try (InputStream var0 = SpriteModels.class.getResourceAsStream("/assets/hook/itemfx/atlas.png");){
                BufferedImage var1 = ImageIO.read(var0);
                atlas = var1.getRGB(0, 0, 128, 128, null, 0, 128);
            }
            catch (Exception var5) {
                throw new IllegalStateException("item fx atlas missing", var5);
            }
        }
        return atlas;
    }

    public static Model build(int var0) {
        int[] var1 = SpriteModels.atlas();
        Builder var2 = new Builder();
        float[] var3 = new float[]{};
        if (var0 == 1) {
            float var22 = 0.025f;
            float var24 = -0.4f;
            float var6 = 0.315f;
            var2.sprite(var1, Mat.SPOOKY_CELL, false, false, var24, var6, var22, -0.025f, 0.025f, 20, 0, 0.0f, 0.0f, 0.0f);
            float var7 = var22 * 0.82f;
            float var8 = var6 - 2.0f * var22;
            float var9 = var8 - 3.0f * var22 + 14.5f * var7;
            float var10 = -8.0f * var7;
            var2.sprite(var1, Mat.HAT_CELL, false, false, var10, var9, var7, -0.031f, 0.031f, 23, 11, (float)Math.toRadians(-8.0), 0.0f, var8 - 2.0f * var22);
            float var11 = var22 * 0.62f;
            float var12 = var24 + 8.0f * var22;
            float var13 = var6 - 16.0f * var22;
            var2.sprite(var1, Mat.WEB_CELL, true, true, var12 - 16.0f * var11, var13 + 16.0f * var11, var11, -0.004f, 0.004f, 23, 0, 0.0f, 0.0f, 0.0f);
            float var14 = var22 * 0.75f;
            float var15 = var24 + 26.5f * var22;
            float var16 = var6 - 21.0f * var22;
            float var17 = 0.12f;
            var2.thread(var1, var15, var16, var16 - var17, var14 * 0.45f);
            var2.sprite(var1, Mat.SPIDER_CELL, false, false, var15 - 3.5f * var14, var16 - var17 + 0.5f * var14, var14, -0.012f, 0.012f, 23, 12, 0.0f, 0.0f, 0.0f);
            float var18 = var22 * 0.8f;
            var2.sprite(var1, Mat.BAT_A_CELL, false, false, -6.0f * var18, 3.5f * var18, var18, -0.01f, 0.01f, 23, 14, 0.0f, 0.0f, 0.0f);
            var2.sprite(var1, Mat.BAT_A_CELL, false, false, -6.0f * var18, 3.5f * var18, var18, -0.01f, 0.01f, 23, 15, 0.0f, 0.0f, 0.0f);
            int var19 = var2.indexCount();
            var2.halo(Mat.SPOOKY_CELL, var24, var6, var22, 0.028f, -0.028f, 6);
            return new Model(var2.build(), var19, var3);
        }
        if (var0 == 2) {
            float var21 = 0.03125f;
            var2.sprite(var1, Mat.CHROME_CELL, false, false, -0.5f, 0.5f, var21, -0.03125f, 0.03125f, 21, 0, 0.0f, 0.0f, 0.0f);
            int var23 = var2.indexCount();
            var2.halo(Mat.CHROME_CELL, -0.5f, 0.5f, var21, 0.034f, -0.034f, 5);
            var3 = SpriteModels.bottomEdges(var1, Mat.CHROME_CELL, -0.5f, 0.5f, var21);
            return new Model(var2.build(), var23, var3);
        }
        float var4 = 0.03125f;
        var2.sprite(var1, Mat.PICK_CELL, false, false, -0.5f, 0.5f, var4, -0.075f, 0.075f, 22, 0, 0.0f, 0.0f, 0.0f);
        int var5 = var2.indexCount();
        var2.halo(Mat.PICK_CELL, -0.5f, 0.5f, var4, 0.078f, -0.078f, 7);
        return new Model(var2.build(), var5, var3);
    }

    static float[] bottomEdges(int[] var0, int[] var1, float var2, float var3, float var4) {
        float[] var5 = new float[var1[2] * var1[3] * 3];
        int var6 = 0;
        for (int var7 = 0; var7 < var1[3]; ++var7) {
            for (int var8 = 0; var8 < var1[2]; ++var8) {
                if (!SpriteModels.opaque(var0, var1, var8, var7) || SpriteModels.opaque(var0, var1, var8, var7 + 1)) continue;
                var5[var6++] = var2 + ((float)var8 + 0.5f) * var4;
                var5[var6++] = var3 - ((float)var7 + 1.0f) * var4;
                var5[var6++] = 0.0f;
            }
        }
        return Arrays.copyOf(var5, var6);
    }

    static boolean opaque(int[] var0, int[] var1, int var2, int var3) {
        return var2 >= 0 && var3 >= 0 && var2 < var1[2] && var3 < var1[3] ? var0[(var1[1] + var3) * 128 + var1[0] + var2] >>> 24 > 0 : false;
    }

    static final class Builder {
        private float[] pos = new float[4096];
        private float[] nrm = new float[4096];
        private float[] uv = new float[4096];
        private short[] mat = new short[2048];
        private short[] region = new short[2048];
        private short[] p1 = new short[2048];
        private short[] p2 = new short[2048];
        private int[] idx = new int[4096];
        private int verts;
        private int indices;

        Builder() {
        }

        int indexCount() {
            return this.indices;
        }

        int vert(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, int var10, int var11, int var12) {
            if (this.verts * 3 + 3 > this.pos.length) {
                this.pos = Arrays.copyOf(this.pos, this.pos.length * 2);
                this.nrm = Arrays.copyOf(this.nrm, this.nrm.length * 2);
                this.uv = Arrays.copyOf(this.uv, this.uv.length * 2);
                this.mat = Arrays.copyOf(this.mat, this.mat.length * 2);
                this.region = Arrays.copyOf(this.region, this.region.length * 2);
                this.p1 = Arrays.copyOf(this.p1, this.p1.length * 2);
                this.p2 = Arrays.copyOf(this.p2, this.p2.length * 2);
            }
            int var13 = this.verts++;
            this.pos[var13 * 3] = var1;
            this.pos[var13 * 3 + 1] = var2;
            this.pos[var13 * 3 + 2] = var3;
            this.nrm[var13 * 3] = var4;
            this.nrm[var13 * 3 + 1] = var5;
            this.nrm[var13 * 3 + 2] = var6;
            this.uv[var13 * 2] = var7;
            this.uv[var13 * 2 + 1] = var8;
            this.mat[var13] = (short)var9;
            this.region[var13] = (short)var10;
            this.p1[var13] = (short)var11;
            this.p2[var13] = (short)var12;
            return var13;
        }

        void quad(int var1, int var2, int var3, int var4) {
            if (this.indices + 6 > this.idx.length) {
                this.idx = Arrays.copyOf(this.idx, this.idx.length * 2);
            }
            this.idx[this.indices++] = var1;
            this.idx[this.indices++] = var2;
            this.idx[this.indices++] = var3;
            this.idx[this.indices++] = var1;
            this.idx[this.indices++] = var3;
            this.idx[this.indices++] = var4;
        }

        void sprite(int[] var1, int[] var2, boolean var3, boolean var4, float var5, float var6, float var7, float var8, float var9, int var10, int var11, float var12, float var13, float var14) {
            int var15 = var2[2];
            int var16 = var2[3];
            float var17 = (float)Math.cos(var12);
            float var18 = (float)Math.sin(var12);
            float var19 = 0.0078125f;
            float var20 = (float)var2[0] * var19;
            float var21 = (float)(var2[0] + var15) * var19;
            float var22 = (float)var2[1] * var19;
            float var23 = (float)(var2[1] + var16) * var19;
            float var24 = var3 ? var21 : var20;
            float var25 = var3 ? var20 : var21;
            float var26 = var4 ? var23 : var22;
            float var27 = var4 ? var22 : var23;
            float var29 = var5 + (float)var15 * var7;
            float var31 = var6 - (float)var16 * var7;
            int var32 = this.vRot(var5, var6, var9, 0.0f, 0.0f, 1.0f, var24, var26, var10, var11, var17, var18, var13, var14);
            int var33 = this.vRot(var29, var6, var9, 0.0f, 0.0f, 1.0f, var25, var26, var10, var11, var17, var18, var13, var14);
            int var34 = this.vRot(var29, var31, var9, 0.0f, 0.0f, 1.0f, var25, var27, var10, var11, var17, var18, var13, var14);
            int var35 = this.vRot(var5, var31, var9, 0.0f, 0.0f, 1.0f, var24, var27, var10, var11, var17, var18, var13, var14);
            this.quad(var35, var34, var33, var32);
            int var36 = this.vRot(var5, var6, var8, 0.0f, 0.0f, -1.0f, var24, var26, var10, var11, var17, var18, var13, var14);
            int var37 = this.vRot(var29, var6, var8, 0.0f, 0.0f, -1.0f, var25, var26, var10, var11, var17, var18, var13, var14);
            int var38 = this.vRot(var29, var31, var8, 0.0f, 0.0f, -1.0f, var25, var27, var10, var11, var17, var18, var13, var14);
            int var39 = this.vRot(var5, var31, var8, 0.0f, 0.0f, -1.0f, var24, var27, var10, var11, var17, var18, var13, var14);
            this.quad(var36, var37, var38, var39);
            for (int var40 = 0; var40 < var16; ++var40) {
                for (int var41 = 0; var41 < var15; ++var41) {
                    int var43;
                    int var42 = var3 ? var15 - 1 - var41 : var41;
                    int n = var43 = var4 ? var16 - 1 - var40 : var40;
                    if (!SpriteModels.opaque(var1, var2, var42, var43)) continue;
                    float var44 = ((float)(var2[0] + var42) + 0.5f) * var19;
                    float var45 = ((float)(var2[1] + var43) + 0.5f) * var19;
                    float var46 = var5 + (float)var41 * var7;
                    float var47 = var46 + var7;
                    float var48 = var6 - (float)var40 * var7;
                    float var49 = var48 - var7;
                    if (!this.occ(var1, var2, var3, var4, var41 - 1, var40)) {
                        this.side(var46, var49, var46, var48, var8, var9, -1.0f, 0.0f, var44, var45, var10, var11, var17, var18, var13, var14);
                    }
                    if (!this.occ(var1, var2, var3, var4, var41 + 1, var40)) {
                        this.side(var47, var48, var47, var49, var8, var9, 1.0f, 0.0f, var44, var45, var10, var11, var17, var18, var13, var14);
                    }
                    if (!this.occ(var1, var2, var3, var4, var41, var40 - 1)) {
                        this.side(var46, var48, var47, var48, var8, var9, 0.0f, 1.0f, var44, var45, var10, var11, var17, var18, var13, var14);
                    }
                    if (this.occ(var1, var2, var3, var4, var41, var40 + 1)) continue;
                    this.side(var47, var49, var46, var49, var8, var9, 0.0f, -1.0f, var44, var45, var10, var11, var17, var18, var13, var14);
                }
            }
        }

        private boolean occ(int[] var1, int[] var2, boolean var3, boolean var4, int var5, int var6) {
            int var7 = var2[2];
            int var8 = var2[3];
            return var5 >= 0 && var6 >= 0 && var5 < var7 && var6 < var8 ? SpriteModels.opaque(var1, var2, var3 ? var7 - 1 - var5 : var5, var4 ? var8 - 1 - var6 : var6) : false;
        }

        private void side(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, int var11, int var12, float var13, float var14, float var15, float var16) {
            int var17 = this.vRot(var1, var2, var5, var7, var8, 0.0f, var9, var10, var11, var12, var13, var14, var15, var16);
            int var18 = this.vRot(var3, var4, var5, var7, var8, 0.0f, var9, var10, var11, var12, var13, var14, var15, var16);
            int var19 = this.vRot(var3, var4, var6, var7, var8, 0.0f, var9, var10, var11, var12, var13, var14, var15, var16);
            int var20 = this.vRot(var1, var2, var6, var7, var8, 0.0f, var9, var10, var11, var12, var13, var14, var15, var16);
            this.quad(var17, var18, var19, var20);
        }

        private int vRot(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, int var10, float var11, float var12, float var13, float var14) {
            float var15 = var1 - var13;
            float var16 = var2 - var14;
            return this.vert(var13 + var15 * var11 - var16 * var12, var14 + var15 * var12 + var16 * var11, var3, var4 * var11 - var5 * var12, var4 * var12 + var5 * var11, var6, var7, var8, var9, var10, 0, 0);
        }

        void thread(int[] var1, float var2, float var3, float var4, float var5) {
            float var6 = ((float)Mat.WEB_CELL[0] + 0.5f) / 128.0f;
            float var7 = ((float)Mat.WEB_CELL[1] + 0.5f) / 128.0f;
            int var8 = this.vert(var2 - var5 * 0.5f, var3, 0.0f, 0.0f, 0.0f, 1.0f, var6, var7, 23, 13, 0, 0);
            int var9 = this.vert(var2 + var5 * 0.5f, var3, 0.0f, 0.0f, 0.0f, 1.0f, var6, var7, 23, 13, 0, 0);
            int var10 = this.vert(var2 + var5 * 0.5f, var4, 0.0f, 0.0f, 0.0f, 1.0f, var6, var7, 23, 13, 0, 1);
            int var11 = this.vert(var2 - var5 * 0.5f, var4, 0.0f, 0.0f, 0.0f, 1.0f, var6, var7, 23, 13, 0, 1);
            this.quad(var11, var10, var9, var8);
        }

        void halo(int[] var1, float var2, float var3, float var4, float var5, float var6, int var7) {
            float var8 = 0.0078125f;
            float var9 = (float)(var1[0] - var7) * var8;
            float var10 = (float)(var1[0] + var1[2] + var7) * var8;
            float var11 = (float)(var1[1] - var7) * var8;
            float var12 = (float)(var1[1] + var1[3] + var7) * var8;
            float var13 = var2 - (float)var7 * var4;
            float var14 = var2 + (float)(var1[2] + var7) * var4;
            float var15 = var3 + (float)var7 * var4;
            float var16 = var3 - (float)(var1[3] + var7) * var4;
            for (int var17 = 0; var17 < 2; ++var17) {
                float var18 = var17 == 0 ? var5 : var6;
                float var19 = var17 == 0 ? 1.0f : -1.0f;
                int var20 = this.vert(var13, var15, var18, 0.0f, 0.0f, var19, var9, var11, 24, 0, var1[0], var1[1]);
                int var21 = this.vert(var14, var15, var18, 0.0f, 0.0f, var19, var10, var11, 24, 0, var1[0], var1[1]);
                int var22 = this.vert(var14, var16, var18, 0.0f, 0.0f, var19, var10, var12, 24, 0, var1[0], var1[1]);
                int var23 = this.vert(var13, var16, var18, 0.0f, 0.0f, var19, var9, var12, 24, 0, var1[0], var1[1]);
                if (var17 == 0) {
                    this.quad(var23, var22, var21, var20);
                    continue;
                }
                this.quad(var20, var21, var22, var23);
            }
        }

        MeshData build() {
            MeshData var1 = new MeshData(this.verts, this.indices / 3);
            System.arraycopy(this.pos, 0, var1.pos, 0, this.verts * 3);
            System.arraycopy(this.nrm, 0, var1.nrm, 0, this.verts * 3);
            System.arraycopy(this.uv, 0, var1.uv, 0, this.verts * 2);
            System.arraycopy(this.mat, 0, var1.mat, 0, this.verts);
            System.arraycopy(this.region, 0, var1.region, 0, this.verts);
            System.arraycopy(this.p1, 0, var1.p1, 0, this.verts);
            System.arraycopy(this.p2, 0, var1.p2, 0, this.verts);
            Arrays.fill(var1.rgba, -1);
            System.arraycopy(this.idx, 0, var1.indices, 0, this.indices);
            return var1;
        }
    }

    public static final class Model {
        public final MeshData mesh;
        public final int mainIndices;
        public final float[] anchors;

        Model(MeshData var1, int var2, float[] var3) {
            this.mesh = var1;
            this.mainIndices = var2;
            this.anchors = var3;
        }
    }
}

