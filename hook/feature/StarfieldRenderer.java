/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_287
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_5819
 *  net.minecraft.class_9848
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package hook.feature;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import hook.feature.AuroraMeshRenderer;
import hook.feature.GpuQuadMesh;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_287;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_5819;
import net.minecraft.class_9848;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
final class StarfieldRenderer {
    private static final float RADIUS = 100.0f;
    private static final int STARS = 2600;
    private static final float GLOW_ABOVE = 0.8f;
    private static final long TWINKLE_NANOS = 50000000L;
    private static final int BAND_COLUMNS = 180;
    private static final int BAND_ROWS = 12;
    private static final float BAND_HALF_WIDTH = 17.0f;
    private static final int NEBULA_COLUMNS = 72;
    private static final int NEBULA_ROWS = 36;
    private static final Vector3f BAND_NORMAL = new Vector3f(0.42f, 0.5f, 0.76f).normalize();
    private final GpuQuadMesh starMesh;
    private final GpuQuadMesh bandMesh;
    private final GpuQuadMesh nebulaMesh;
    private final float[] dir;
    private final float[] size;
    private final float[] light;
    private final float[] roll;
    private final float[] phase;
    private final float[] speed;
    private final int[] rgb;
    private boolean placed;
    private long lastTwinkle;
    private float builtBrightness;
    private int bandTint;

    StarfieldRenderer() {
        GpuQuadMesh var18;
        GpuQuadMesh var17;
        GpuQuadMesh var2;
        String var3 = "Hook stars";
        RenderPipeline var4 = GpuQuadMesh.GLOW;
        this.starMesh = var2 = new GpuQuadMesh(var3, var4, 5200);
        String var34 = "Hook milky way";
        RenderPipeline var36 = GpuQuadMesh.GLOW;
        this.bandMesh = var17 = new GpuQuadMesh(var34, var36, 2160);
        String var35 = "Hook end nebulae";
        RenderPipeline var37 = GpuQuadMesh.GLOW;
        this.nebulaMesh = var18 = new GpuQuadMesh(var35, var37, 2592);
        float[] var20 = new float[7800];
        this.dir = var20;
        float[] var22 = new float[2600];
        this.size = var22;
        float[] var24 = new float[2600];
        this.light = var24;
        float[] var26 = new float[2600];
        this.roll = var26;
        float[] var28 = new float[2600];
        this.phase = var28;
        float[] var30 = new float[2600];
        this.speed = var30;
        int[] var32 = new int[2600];
        this.rgb = var32;
        this.builtBrightness = -1.0f;
    }

    void renderStars(class_4587 var1, float var2, float var3) {
        long var4 = System.nanoTime();
        if (var4 - this.lastTwinkle > 50000000L || var3 != this.builtBrightness) {
            this.lastTwinkle = var4;
            this.builtBrightness = var3;
            this.buildStars((float)(var4 % 3600000000000L) / 1.0E9f, var3);
        }
        this.starMesh.draw(var1, var2);
    }

    void renderBand(class_4587 var1, float var2, int var3) {
        if (!this.bandMesh.isBuilt() || var3 != this.bandTint) {
            this.bandTint = var3;
            this.buildBand(var3);
        }
        this.bandMesh.draw(var1, var2);
    }

    void renderNebulae(class_4587 var1, float var2) {
        if (!this.nebulaMesh.isBuilt()) {
            this.buildNebulae();
        }
        this.nebulaMesh.draw(var1, var2);
    }

    private void place() {
        class_5819 var1 = class_5819.method_43049((long)10842L);
        Vector3f var2 = new Vector3f();
        Vector3f var3 = new Vector3f();
        StarfieldRenderer.bandAxes(var2, var3);
        for (int var4 = 0; var4 < 2600; ++var4) {
            float var7;
            float var6;
            float var5;
            if (var1.method_43057() < 0.3f) {
                float var8 = var1.method_43057() * ((float)Math.PI * 2);
                float var9 = (float)var1.method_43059() * 9.0f * ((float)Math.PI / 180);
                float var10 = class_3532.method_15362((double)var9);
                float var11 = class_3532.method_15374((double)var9);
                var5 = var10 * (class_3532.method_15362((double)var8) * var2.x + class_3532.method_15374((double)var8) * var3.x) + var11 * StarfieldRenderer.BAND_NORMAL.x;
                var6 = var10 * (class_3532.method_15362((double)var8) * var2.y + class_3532.method_15374((double)var8) * var3.y) + var11 * StarfieldRenderer.BAND_NORMAL.y;
                var7 = var10 * (class_3532.method_15362((double)var8) * var2.z + class_3532.method_15374((double)var8) * var3.z) + var11 * StarfieldRenderer.BAND_NORMAL.z;
            } else {
                var6 = var1.method_43057() * 2.0f - 1.0f;
                float var12 = var1.method_43057() * ((float)Math.PI * 2);
                float var14 = class_3532.method_15355((float)(1.0f - var6 * var6));
                var5 = var14 * class_3532.method_15362((double)var12);
                var7 = var14 * class_3532.method_15374((double)var12);
            }
            this.dir[var4 * 3] = var5;
            this.dir[var4 * 3 + 1] = var6;
            this.dir[var4 * 3 + 2] = var7;
            float var13 = (float)Math.pow(var1.method_43057(), 6.0);
            this.size[var4] = 0.1f + 0.5f * var13;
            this.light[var4] = class_3532.method_15363((float)(0.3f + 0.7f * var13 + 0.25f * var1.method_43057()), (float)0.0f, (float)1.0f);
            this.roll[var4] = var1.method_43057() * ((float)Math.PI * 2);
            this.phase[var4] = var1.method_43057() * ((float)Math.PI * 2);
            this.speed[var4] = 1.5f + 3.5f * var1.method_43057();
            this.rgb[var4] = StarfieldRenderer.starColour(var1.method_43057());
        }
        this.placed = true;
    }

    private static int starColour(float var0) {
        if (var0 < 0.18f) {
            return class_9848.method_61319((float)(var0 / 0.18f), (int)16757870, (int)16774116);
        }
        return var0 < 0.75f ? 0xFFF8F0 : class_9848.method_61319((float)((var0 - 0.75f) / 0.25f), (int)0xFFF3F6, (int)11125503);
    }

    private void buildStars(float var1, float var2) {
        if (!this.placed) {
            this.place();
        }
        class_287 var3 = this.starMesh.begin();
        for (int var4 = 0; var4 < 2600; ++var4) {
            float var5 = 0.62f + 0.38f * class_3532.method_15374((double)(var1 * this.speed[var4] + this.phase[var4]));
            float var6 = class_3532.method_15363((float)(this.light[var4] * var5 * var2), (float)0.0f, (float)1.0f);
            this.star(var3, var4, this.size[var4], var6);
            if (!(this.light[var4] > 0.8f)) continue;
            this.star(var3, var4, this.size[var4] * 4.0f, var6 * 0.12f);
        }
        this.starMesh.upload(var3);
    }

    private void star(class_287 var1, int var2, float var3, float var4) {
        float var10;
        float var9;
        float var8;
        float var5 = this.dir[var2 * 3];
        float var6 = this.dir[var2 * 3 + 1];
        float var7 = this.dir[var2 * 3 + 2];
        if (Math.abs(var6) < 0.9f) {
            var8 = var7;
            var9 = 0.0f;
            var10 = -var5;
        } else {
            var8 = 0.0f;
            var9 = -var7;
            var10 = var6;
        }
        float var11 = class_3532.method_15355((float)(var8 * var8 + var9 * var9 + var10 * var10));
        float var12 = var6 * (var10 /= var11) - var7 * (var9 /= var11);
        float var13 = var7 * (var8 /= var11) - var5 * var10;
        float var14 = var5 * var9 - var6 * var8;
        float var15 = class_3532.method_15362((double)this.roll[var2]) * var3;
        float var16 = class_3532.method_15374((double)this.roll[var2]) * var3;
        float var17 = var8 * var15 + var12 * var16;
        float var18 = var9 * var15 + var13 * var16;
        float var19 = var10 * var15 + var14 * var16;
        float var20 = var12 * var15 - var8 * var16;
        float var21 = var13 * var15 - var9 * var16;
        float var22 = var14 * var15 - var10 * var16;
        float var23 = var5 * 100.0f;
        float var24 = var6 * 100.0f;
        float var25 = var7 * 100.0f;
        int var26 = Math.round(var4 * 255.0f) << 24 | this.rgb[var2];
        GpuQuadMesh.vertex(var1, var23 - var17 - var20, var24 - var18 - var21, var25 - var19 - var22, var26);
        GpuQuadMesh.vertex(var1, var23 + var17 - var20, var24 + var18 - var21, var25 + var19 - var22, var26);
        GpuQuadMesh.vertex(var1, var23 + var17 + var20, var24 + var18 + var21, var25 + var19 + var22, var26);
        GpuQuadMesh.vertex(var1, var23 - var17 + var20, var24 - var18 + var21, var25 - var19 + var22, var26);
    }

    private static void bandAxes(Vector3f var0, Vector3f var1) {
        new Vector3f(0.0f, 1.0f, 0.0f).cross((Vector3fc)BAND_NORMAL, var0).normalize();
        new Vector3f((Vector3fc)BAND_NORMAL).cross((Vector3fc)var0, var1).normalize();
    }

    private void buildBand(int var1) {
        Vector3f var2 = new Vector3f();
        Vector3f var3 = new Vector3f();
        StarfieldRenderer.bandAxes(var2, var3);
        float[][] var4 = new float[181][13];
        float[][] var5 = new float[181][13];
        float[][] var6 = new float[181][13];
        int[][] var7 = new int[181][13];
        for (int var8 = 0; var8 <= 180; ++var8) {
            float var9 = (float)var8 / 180.0f * ((float)Math.PI * 2);
            float var10 = class_3532.method_15362((double)var9) * var2.x + class_3532.method_15374((double)var9) * var3.x;
            float var11 = class_3532.method_15362((double)var9) * var2.y + class_3532.method_15374((double)var9) * var3.y;
            float var12 = class_3532.method_15362((double)var9) * var2.z + class_3532.method_15374((double)var9) * var3.z;
            float var13 = 0.55f + 0.45f * StarfieldRenderer.fbm(var10 * 1.7f, var11 * 1.7f, var12 * 1.7f);
            float var14 = 3.0f * (StarfieldRenderer.fbm(var10 * 2.3f + 5.0f, var11 * 2.3f, var12 * 2.3f) - 0.5f);
            float var15 = class_3532.method_15363((float)(StarfieldRenderer.fbm(var10 * 1.4f, var11 * 1.4f + 9.0f, var12 * 1.4f) * 1.6f - 0.35f), (float)0.0f, (float)1.0f);
            for (int var16 = 0; var16 <= 12; ++var16) {
                float var17 = ((float)var16 / 12.0f * 2.0f - 1.0f) * 17.0f;
                float var18 = var17 * ((float)Math.PI / 180);
                float var19 = class_3532.method_15362((double)var18);
                float var20 = class_3532.method_15374((double)var18);
                float var21 = var19 * var10 + var20 * StarfieldRenderer.BAND_NORMAL.x;
                float var22 = var19 * var11 + var20 * StarfieldRenderer.BAND_NORMAL.y;
                float var23 = var19 * var12 + var20 * StarfieldRenderer.BAND_NORMAL.z;
                var4[var8][var16] = var21 * 100.0f;
                var5[var8][var16] = var22 * 100.0f;
                var6[var8][var16] = var23 * 100.0f;
                float var24 = var17 / (17.0f * var13);
                float var25 = (float)Math.exp(-var24 * var24 * 2.2f);
                float var26 = 0.35f + 0.65f * StarfieldRenderer.fbm(var21 * 6.0f, var22 * 6.0f, var23 * 6.0f);
                float var27 = 1.0f - 0.75f * var15 * (float)Math.exp(-StarfieldRenderer.sq((var17 - var14) / 2.6f));
                float var28 = class_3532.method_15363((float)(var25 * var26 * var27), (float)0.0f, (float)1.0f);
                int var29 = class_9848.method_61319((float)var25, (int)7306440, (int)var1) & 0xFFFFFF;
                var7[var8][var16] = Math.round(var28 * 0.42f * 255.0f) << 24 | var29;
            }
        }
        class_287 var30 = this.bandMesh.begin();
        for (int var31 = 0; var31 < 180; ++var31) {
            for (int var32 = 0; var32 < 12; ++var32) {
                GpuQuadMesh.vertex(var30, var4[var31][var32], var5[var31][var32], var6[var31][var32], var7[var31][var32]);
                GpuQuadMesh.vertex(var30, var4[var31 + 1][var32], var5[var31 + 1][var32], var6[var31 + 1][var32], var7[var31 + 1][var32]);
                GpuQuadMesh.vertex(var30, var4[var31 + 1][var32 + 1], var5[var31 + 1][var32 + 1], var6[var31 + 1][var32 + 1], var7[var31 + 1][var32 + 1]);
                GpuQuadMesh.vertex(var30, var4[var31][var32 + 1], var5[var31][var32 + 1], var6[var31][var32 + 1], var7[var31][var32 + 1]);
            }
        }
        this.bandMesh.upload(var30);
    }

    private void buildNebulae() {
        float[][] var1 = new float[73][37];
        float[][] var2 = new float[73][37];
        float[][] var3 = new float[73][37];
        int[][] var4 = new int[73][37];
        for (int var5 = 0; var5 <= 72; ++var5) {
            float var6 = (float)var5 / 72.0f * ((float)Math.PI * 2);
            for (int var7 = 0; var7 <= 36; ++var7) {
                float var8 = ((float)var7 / 36.0f - 0.5f) * (float)Math.PI;
                float var9 = class_3532.method_15362((double)var8) * class_3532.method_15362((double)var6);
                float var10 = class_3532.method_15374((double)var8);
                float var11 = class_3532.method_15362((double)var8) * class_3532.method_15374((double)var6);
                var1[var5][var7] = var9 * 100.0f;
                var2[var5][var7] = var10 * 100.0f;
                var3[var5][var7] = var11 * 100.0f;
                float var12 = AuroraMeshRenderer.smoothstep((StarfieldRenderer.fbm(var9 * 2.2f, var10 * 2.2f, var11 * 2.2f) - 0.42f) / 0.35f);
                float var13 = StarfieldRenderer.fbm(var9 * 1.3f + 11.0f, var10 * 1.3f, var11 * 1.3f);
                int var14 = var13 < 0.5f ? class_9848.method_61319((float)(var13 * 2.0f), (int)8011007, (int)16732104) : class_9848.method_61319((float)((var13 - 0.5f) * 2.0f), (int)16732104, (int)3989712);
                var4[var5][var7] = Math.round(var12 * 0.55f * 255.0f) << 24 | var14 & 0xFFFFFF;
            }
        }
        class_287 var15 = this.nebulaMesh.begin();
        for (int var16 = 0; var16 < 72; ++var16) {
            for (int var17 = 0; var17 < 36; ++var17) {
                GpuQuadMesh.vertex(var15, var1[var16][var17], var2[var16][var17], var3[var16][var17], var4[var16][var17]);
                GpuQuadMesh.vertex(var15, var1[var16 + 1][var17], var2[var16 + 1][var17], var3[var16 + 1][var17], var4[var16 + 1][var17]);
                GpuQuadMesh.vertex(var15, var1[var16 + 1][var17 + 1], var2[var16 + 1][var17 + 1], var3[var16 + 1][var17 + 1], var4[var16 + 1][var17 + 1]);
                GpuQuadMesh.vertex(var15, var1[var16][var17 + 1], var2[var16][var17 + 1], var3[var16][var17 + 1], var4[var16][var17 + 1]);
            }
        }
        this.nebulaMesh.upload(var15);
    }

    static float fbm(float var0, float var1, float var2) {
        float var3 = 0.0f;
        float var4 = 0.5f;
        float var5 = 0.0f;
        for (int var6 = 0; var6 < 4; ++var6) {
            var3 += var4 * StarfieldRenderer.noise(var0, var1, var2);
            var5 += var4;
            var0 *= 2.03f;
            var1 *= 2.03f;
            var2 *= 2.03f;
            var4 *= 0.5f;
        }
        return var3 / var5;
    }

    private static float noise(float var0, float var1, float var2) {
        int var3 = class_3532.method_15375((float)var0);
        int var4 = class_3532.method_15375((float)var1);
        int var5 = class_3532.method_15375((float)var2);
        float var6 = var0 - (float)var3;
        float var7 = var1 - (float)var4;
        float var8 = var2 - (float)var5;
        var6 = var6 * var6 * (3.0f - 2.0f * var6);
        var7 = var7 * var7 * (3.0f - 2.0f * var7);
        var8 = var8 * var8 * (3.0f - 2.0f * var8);
        float var9 = class_3532.method_16439((float)var6, (float)StarfieldRenderer.hash(var3, var4, var5), (float)StarfieldRenderer.hash(var3 + 1, var4, var5));
        float var10 = class_3532.method_16439((float)var6, (float)StarfieldRenderer.hash(var3, var4 + 1, var5), (float)StarfieldRenderer.hash(var3 + 1, var4 + 1, var5));
        float var11 = class_3532.method_16439((float)var6, (float)StarfieldRenderer.hash(var3, var4, var5 + 1), (float)StarfieldRenderer.hash(var3 + 1, var4, var5 + 1));
        float var12 = class_3532.method_16439((float)var6, (float)StarfieldRenderer.hash(var3, var4 + 1, var5 + 1), (float)StarfieldRenderer.hash(var3 + 1, var4 + 1, var5 + 1));
        return class_3532.method_16439((float)var8, (float)class_3532.method_16439((float)var7, (float)var9, (float)var10), (float)class_3532.method_16439((float)var7, (float)var11, (float)var12));
    }

    private static float hash(int var0, int var1, int var2) {
        int var3 = var0 * 374761393 + var1 * 668265263 + var2 * 1274126177;
        var3 = (var3 ^ var3 >>> 13) * 1274126177;
        return (float)((var3 ^ var3 >>> 16) & 0xFFFFFF) / 1.6777215E7f;
    }

    private static float sq(float var0) {
        return var0 * var0;
    }

    void close() {
        this.starMesh.close();
        this.bandMesh.close();
        this.nebulaMesh.close();
        this.builtBrightness = -1.0f;
    }
}

