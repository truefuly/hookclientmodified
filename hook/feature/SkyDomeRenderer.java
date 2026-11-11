/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_287
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_5819
 *  net.minecraft.class_9848
 */
package hook.feature;

import hook.feature.AuroraMeshRenderer;
import hook.feature.GpuQuadMesh;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_287;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_5819;
import net.minecraft.class_9848;

@Environment(value=EnvType.CLIENT)
final class SkyDomeRenderer {
    private static final float RADIUS = 100.0f;
    private static final float[] RINGS = new float[]{-30.0f, -12.0f, -4.0f, 0.0f, 4.0f, 9.0f, 16.0f, 26.0f, 40.0f, 58.0f, 76.0f, 90.0f};
    private static final int SEGMENTS = 48;
    private static final int HALO_RINGS = 4;
    private static final int HALO_SEGMENTS = 32;
    private static final int METEOR_SEGMENTS = 10;
    private static final float[] KEYS = new float[]{-0.35f, -0.08f, 0.06f, 0.3f};
    private static final int GOLDEN_ZENITH = 5210048;
    private static final int GOLDEN_HORIZON = 16032864;
    private static final int GOLDEN_GLOW = 16747069;
    private static final int DAY_ZENITH = 3108822;
    private static final int DAY_HORIZON = 11850996;
    private static final int DAY_GLOW = 16774362;
    private static final int BLUE_HOUR_ZENITH = 1319497;
    private final GpuQuadMesh dome = new GpuQuadMesh("Hook sky dome", GpuQuadMesh.PAINT, (RINGS.length - 1) * 48);
    private final GpuQuadMesh endDome = new GpuQuadMesh("Hook end sky", GpuQuadMesh.PAINT, (RINGS.length - 1) * 48);
    private final GpuQuadMesh halo = new GpuQuadMesh("Hook sun and moon glow", GpuQuadMesh.GLOW, 256);
    private final GpuQuadMesh meteorMesh = new GpuQuadMesh("Hook shooting stars", GpuQuadMesh.GLOW, 12);
    private final class_5819 random = class_5819.method_43047();
    private int zenith;
    private int horizon;
    private int glow;
    private float glowStrength;
    private float sunX;
    private float sunY;
    private float sunAngle;
    private float moonAngle;
    private boolean ready;
    private long meteorStart;
    private long meteorEnd;
    private long nextMeteor;
    private float mx;
    private float my;
    private float mz;
    private float tx;
    private float ty;
    private float tz;

    SkyDomeRenderer() {
    }

    void update(float var1, float var2, float var3, float var4, float var5, int var6, int var7, int var8) {
        int var14;
        this.sunAngle = var1;
        this.moonAngle = var2;
        this.sunX = -class_3532.method_15374((double)var1);
        this.sunY = class_3532.method_15362((double)var1);
        int var9 = class_9848.method_61319((float)0.3f, (int)1319497, (int)var6);
        int[] var10000 = new int[]{var6, var9, 5210048, 3108822};
        int[] var10 = var10000;
        var10000 = new int[]{var7, SkyDomeRenderer.scale(var8, 0.75f), 16032864, 11850996};
        int[] var11 = var10000;
        var10000 = new int[]{var7, var8, 16747069, 16774362};
        int[] var12 = var10000;
        float[] var18 = new float[]{0.0f, 0.55f, 1.0f, 0.25f};
        float[] var13 = var18;
        for (var14 = 0; var14 < KEYS.length - 2 && this.sunY > KEYS[var14 + 1]; ++var14) {
        }
        float var15 = AuroraMeshRenderer.smoothstep((this.sunY - KEYS[var14]) / (KEYS[var14 + 1] - KEYS[var14]));
        this.zenith = class_9848.method_61319((float)var15, (int)var10[var14], (int)var10[var14 + 1]);
        this.horizon = class_9848.method_61319((float)var15, (int)var11[var14], (int)var11[var14 + 1]);
        this.glow = class_9848.method_61319((float)var15, (int)var12[var14], (int)var12[var14 + 1]);
        this.glowStrength = class_3532.method_16439((float)var15, (float)var13[var14], (float)var13[var14 + 1]) * (1.0f - var3);
        this.zenith = SkyDomeRenderer.weather(this.zenith, var3, var4, var5);
        this.horizon = SkyDomeRenderer.weather(this.horizon, var3, var4, var5);
        this.ready = true;
    }

    private static int weather(int var0, float var1, float var2, float var3) {
        int var4 = SkyDomeRenderer.scale(SkyDomeRenderer.luma(var0), 0.85f);
        var0 = class_9848.method_61319((float)(var1 * 0.65f), (int)var0, (int)var4);
        var0 = SkyDomeRenderer.scale(var0, 1.0f - 0.35f * var1 - 0.25f * var2);
        return var3 > 0.0f ? class_9848.method_61319((float)(var3 * 0.45f), (int)var0, (int)15265023) : var0;
    }

    boolean ready() {
        return this.ready;
    }

    int horizonToward(float var1, float var2) {
        float var3 = class_3532.method_15355((float)(var1 * var1 + var2 * var2));
        return var3 < 1.0E-4f ? this.horizon : this.colourAt(var1 / var3, 0.02f, var2 / var3);
    }

    float golden() {
        return 1.0f - Math.min(1.0f, Math.abs(this.sunY - 0.06f) / 0.22f);
    }

    float nightness() {
        return AuroraMeshRenderer.smoothstep((0.05f - this.sunY) / 0.4f);
    }

    int twilightGlow() {
        return this.glow;
    }

    void renderDome(class_4587 var1) {
        class_287 var2 = this.dome.begin();
        this.domeQuads(var2, false);
        this.dome.upload(var2);
        this.dome.draw(var1, 1.0f);
    }

    void renderEndDome(class_4587 var1) {
        if (!this.endDome.isBuilt()) {
            class_287 var2 = this.endDome.begin();
            this.domeQuads(var2, true);
            this.endDome.upload(var2);
        }
        this.endDome.draw(var1, 1.0f);
    }

    private void domeQuads(class_287 var1, boolean var2) {
        for (int var3 = 0; var3 < RINGS.length - 1; ++var3) {
            for (int var4 = 0; var4 < 48; ++var4) {
                this.corner(var1, RINGS[var3], var4, var2);
                this.corner(var1, RINGS[var3], var4 + 1, var2);
                this.corner(var1, RINGS[var3 + 1], var4 + 1, var2);
                this.corner(var1, RINGS[var3 + 1], var4, var2);
            }
        }
    }

    private void corner(class_287 var1, float var2, int var3, boolean var4) {
        float var5 = var2 * ((float)Math.PI / 180);
        float var6 = (float)var3 / 48.0f * ((float)Math.PI * 2);
        float var7 = class_3532.method_15362((double)var5) * class_3532.method_15362((double)var6);
        float var8 = class_3532.method_15374((double)var5);
        float var9 = class_3532.method_15362((double)var5) * class_3532.method_15374((double)var6);
        int var10 = var4 ? SkyDomeRenderer.endColourAt(var8) : this.colourAt(var7, var8, var9);
        GpuQuadMesh.vertex(var1, var7 * 100.0f, var8 * 100.0f, var9 * 100.0f, 0xFF000000 | var10);
    }

    private int colourAt(float var1, float var2, float var3) {
        int var4 = class_9848.method_61319((float)((float)Math.sqrt(Math.max(var2, 0.0f))), (int)this.horizon, (int)this.zenith);
        if (var2 < 0.0f) {
            var4 = SkyDomeRenderer.scale(var4, 1.0f - 0.35f * AuroraMeshRenderer.smoothstep(-var2 / 0.4f));
        }
        if (this.glowStrength > 0.001f) {
            float var5 = Math.max(0.0f, var1 * this.sunX + var2 * this.sunY);
            float var6 = Math.max(0.0f, this.sunX < 0.0f ? -var1 : var1);
            float var7 = (float)(Math.pow(var5, 8.0) * 0.9 + Math.pow(var5, 2.0) * 0.25);
            float var8 = (float)(Math.exp(-Math.abs(var2) * 9.0f) * Math.pow(var6, 1.5));
            float var9 = class_3532.method_15363((float)(this.glowStrength * (var7 + var8 * 0.6f)), (float)0.0f, (float)1.0f);
            var4 = class_9848.method_61319((float)var9, (int)var4, (int)this.glow);
        }
        return var4 & 0xFFFFFF;
    }

    private static int endColourAt(float var0) {
        return class_9848.method_61319((float)((float)Math.sqrt(Math.max(var0, 0.0f))), (int)1839152, (int)459789) & 0xFFFFFF;
    }

    void renderHalos(class_4587 var1, float var2, float var3) {
        class_287 var4 = this.halo.begin();
        float var5 = 1.0f - AuroraMeshRenderer.smoothstep(this.sunY / 0.35f);
        float var6 = AuroraMeshRenderer.smoothstep((this.sunY + 0.12f) / 0.12f) * var2 * (0.45f + 0.35f * var5);
        int var7 = class_9848.method_61319((float)var5, (int)16773584, (int)16742963);
        SkyDomeRenderer.haloQuads(var4, this.sunAngle, 28.0f * (1.0f + 0.6f * var5), var7, var6);
        SkyDomeRenderer.haloQuads(var4, this.moonAngle, 34.0f, 11520255, 0.32f * var3 * var2);
        this.halo.upload(var4);
        this.halo.draw(var1, 1.0f);
    }

    private static void haloQuads(class_287 var0, float var1, float var2, int var3, float var4) {
        if (!(var4 <= 0.002f)) {
            float[] var10000 = new float[]{0.0f, var2 * 0.16f, var2 * 0.38f, var2 * 0.66f, var2};
            float[] var5 = var10000;
            var10000 = new float[]{1.0f, 0.55f, 0.22f, 0.07f, 0.0f};
            float[] var6 = var10000;
            float var7 = class_3532.method_15362((double)var1);
            float var8 = class_3532.method_15374((double)var1);
            for (int var9 = 0; var9 < 4; ++var9) {
                for (int var10 = 0; var10 < 32; ++var10) {
                    SkyDomeRenderer.haloVertex(var0, var5[var9], var10, var7, var8, var3, var6[var9] * var4);
                    SkyDomeRenderer.haloVertex(var0, var5[var9], var10 + 1, var7, var8, var3, var6[var9] * var4);
                    SkyDomeRenderer.haloVertex(var0, var5[var9 + 1], var10 + 1, var7, var8, var3, var6[var9 + 1] * var4);
                    SkyDomeRenderer.haloVertex(var0, var5[var9 + 1], var10, var7, var8, var3, var6[var9 + 1] * var4);
                }
            }
        }
    }

    private static void haloVertex(class_287 var0, float var1, int var2, float var3, float var4, int var5, float var6) {
        float var7 = (float)var2 / 32.0f * ((float)Math.PI * 2);
        float var8 = class_3532.method_15362((double)var7) * var1;
        float var9 = 100.0f;
        float var10 = class_3532.method_15374((double)var7) * var1;
        float var11 = var9 * var3 - var10 * var4;
        float var12 = var9 * var4 + var10 * var3;
        GpuQuadMesh.vertex(var0, -var12, var11, var8, Math.round(class_3532.method_15363((float)var6, (float)0.0f, (float)1.0f) * 255.0f) << 24 | var5 & 0xFFFFFF);
    }

    void renderMeteors(class_4587 var1, float var2, float var3) {
        long var4 = System.nanoTime();
        if (var4 >= this.meteorEnd) {
            if (this.nextMeteor == 0L) {
                this.nextMeteor = var4 + SkyDomeRenderer.seconds(4.0f + 10.0f * this.random.method_43057());
            }
            if (var4 < this.nextMeteor || var2 < 0.6f || var3 < 0.8f) {
                return;
            }
            this.launch(var4);
        }
        float var6 = (float)(var4 - this.meteorStart) / (float)(this.meteorEnd - this.meteorStart);
        float var7 = class_3532.method_15374((double)((float)Math.PI * class_3532.method_15363((float)var6, (float)0.0f, (float)1.0f))) * var2 * var3;
        class_287 var8 = this.meteorMesh.begin();
        float var9 = var6 * 0.55f;
        float var10 = 0.16f;
        float[] var11 = null;
        for (int var12 = 0; var12 <= 10; ++var12) {
            float var13 = var9 - var10 * (float)var12 / 10.0f;
            float[] var10000 = new float[]{(this.mx + this.tx * var13) * 100.0f, (this.my + this.ty * var13) * 100.0f, (this.mz + this.tz * var13) * 100.0f};
            float[] var14 = var10000;
            if (var11 != null) {
                SkyDomeRenderer.streak(var8, var11, var14, var7 * (1.0f - ((float)var12 - 1.0f) / 10.0f), var7 * (1.0f - (float)var12 / 10.0f));
            }
            var11 = var14;
        }
        this.meteorMesh.upload(var8);
        this.meteorMesh.draw(var1, 1.0f);
    }

    private void launch(long var1) {
        float var3 = (30.0f + 40.0f * this.random.method_43057()) * ((float)Math.PI / 180);
        float var4 = this.random.method_43057() * ((float)Math.PI * 2);
        this.mx = class_3532.method_15362((double)var3) * class_3532.method_15362((double)var4);
        this.my = class_3532.method_15374((double)var3);
        this.mz = class_3532.method_15362((double)var3) * class_3532.method_15374((double)var4);
        float var5 = -class_3532.method_15374((double)var4);
        float var6 = class_3532.method_15362((double)var4);
        float var7 = -0.6f - 0.4f * this.random.method_43057();
        float var8 = var5 * this.mx + var7 * this.my + var6 * this.mz;
        this.tx = var5 - var8 * this.mx;
        this.ty = var7 - var8 * this.my;
        this.tz = var6 - var8 * this.mz;
        float var9 = class_3532.method_15355((float)(this.tx * this.tx + this.ty * this.ty + this.tz * this.tz));
        this.tx /= var9;
        this.ty /= var9;
        this.tz /= var9;
        this.meteorStart = var1;
        this.meteorEnd = var1 + SkyDomeRenderer.seconds(0.7f + 0.5f * this.random.method_43057());
        this.nextMeteor = this.meteorEnd + SkyDomeRenderer.seconds(6.0f + 18.0f * this.random.method_43057());
    }

    private static void streak(class_287 var0, float[] var1, float[] var2, float var3, float var4) {
        float var7 = var2[2] - var1[2];
        float var6 = var2[1] - var1[1];
        float var8 = var1[1] * var7 - var1[2] * var6;
        float var5 = var2[0] - var1[0];
        float var9 = var1[2] * var5 - var1[0] * var7;
        float var10 = var1[0] * var6 - var1[1] * var5;
        float var11 = class_3532.method_15355((float)(var8 * var8 + var9 * var9 + var10 * var10));
        if (!(var11 < 1.0E-5f)) {
            float var12 = 0.22f / var11;
            int var13 = Math.round(class_3532.method_15363((float)var3, (float)0.0f, (float)1.0f) * 255.0f) << 24 | 0xEAF2FF;
            int var14 = Math.round(class_3532.method_15363((float)var4, (float)0.0f, (float)1.0f) * 255.0f) << 24 | 0xEAF2FF;
            GpuQuadMesh.vertex(var0, var1[0] - (var8 *= var12), var1[1] - (var9 *= var12), var1[2] - (var10 *= var12), var13);
            GpuQuadMesh.vertex(var0, var1[0] + var8, var1[1] + var9, var1[2] + var10, var13);
            GpuQuadMesh.vertex(var0, var2[0] + var8, var2[1] + var9, var2[2] + var10, var14);
            GpuQuadMesh.vertex(var0, var2[0] - var8, var2[1] - var9, var2[2] - var10, var14);
        }
    }

    private static long seconds(float var0) {
        return (long)(var0 * 1.0E9f);
    }

    static int scale(int var0, float var1) {
        int var2 = class_3532.method_15340((int)Math.round((float)(var0 >> 16 & 0xFF) * var1), (int)0, (int)255);
        int var3 = class_3532.method_15340((int)Math.round((float)(var0 >> 8 & 0xFF) * var1), (int)0, (int)255);
        int var4 = class_3532.method_15340((int)Math.round((float)(var0 & 0xFF) * var1), (int)0, (int)255);
        return var0 & 0xFF000000 | var2 << 16 | var3 << 8 | var4;
    }

    static int luma(int var0) {
        int var1 = Math.round(0.2126f * (float)(var0 >> 16 & 0xFF) + 0.7152f * (float)(var0 >> 8 & 0xFF) + 0.0722f * (float)(var0 & 0xFF));
        return var0 & 0xFF000000 | var1 << 16 | var1 << 8 | var1;
    }

    void close() {
        this.dome.close();
        this.endDome.close();
        this.halo.close();
        this.meteorMesh.close();
        this.ready = false;
    }
}

