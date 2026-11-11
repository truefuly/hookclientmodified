/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_287
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_9848
 */
package hook.feature;

import hook.feature.GpuQuadMesh;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_287;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_9848;

@Environment(value=EnvType.CLIENT)
final class AuroraMeshRenderer {
    private static final float RADIUS = 100.0f;
    private static final int COLUMNS = 64;
    private static final int ROWS = 6;
    private static final float[][] CURTAINS;
    private final GpuQuadMesh mesh = new GpuQuadMesh("Hook aurora", GpuQuadMesh.GLOW, CURTAINS.length * 64 * 6);
    private final float[] x = new float[455];
    private final float[] y = new float[this.x.length];
    private final float[] z = new float[this.x.length];
    private final int[] color = new int[this.x.length];

    AuroraMeshRenderer() {
    }

    void render(class_4587 var1, float var2, float var3, int var4, int var5) {
        class_287 var6 = this.mesh.begin();
        for (int var7 = 0; var7 < CURTAINS.length; ++var7) {
            this.curtain(var6, CURTAINS[var7], var7, var3, var4, var5);
        }
        this.mesh.upload(var6);
        this.mesh.draw(var1, var2);
    }

    private void curtain(class_287 var1, float[] var2, int var3, float var4, int var5, int var6) {
        float var7 = var2[0];
        float var8 = var2[1];
        float var9 = var2[2];
        float var10 = var2[3];
        for (int var11 = 0; var11 <= 64; ++var11) {
            float var12 = (float)var11 / 64.0f;
            float var13 = var7 + (var12 - 0.5f) * var9 + 3.0f * AuroraMeshRenderer.wave(var12 * 2.1f + var4 * 0.05f + (float)var3);
            float var14 = var8 + 5.0f * class_3532.method_15374((double)((float)Math.PI * var12)) + 3.0f * AuroraMeshRenderer.wave(var12 * 3.7f - var4 * 0.07f + (float)var3 * 2.0f);
            float var15 = AuroraMeshRenderer.smoothstep(var12 / 0.25f) * AuroraMeshRenderer.smoothstep((1.0f - var12) / 0.25f);
            float var16 = var15 * (0.55f + 0.45f * AuroraMeshRenderer.wave01(var12 * 5.3f + var4 * 0.11f + (float)var3 * 1.3f));
            float var17 = 0.72f + 0.28f * AuroraMeshRenderer.wave01(var12 * 23.0f + var4 * 0.35f + (float)var3);
            float var18 = var10 * (0.8f + 0.35f * AuroraMeshRenderer.wave01(var12 * 4.1f + var4 * 0.06f + (float)var3 * 3.0f));
            float var19 = class_3532.method_15362((double)(var13 * ((float)Math.PI / 180)));
            float var20 = class_3532.method_15374((double)(var13 * ((float)Math.PI / 180)));
            for (int var21 = 0; var21 <= 6; ++var21) {
                float var22 = (float)var21 / 6.0f;
                float var23 = (var14 + var22 * var18) * ((float)Math.PI / 180);
                float var24 = class_3532.method_15362((double)var23);
                int var25 = var11 * 7 + var21;
                this.x[var25] = 100.0f * var24 * var20;
                this.y[var25] = 100.0f * class_3532.method_15374((double)var23);
                this.z[var25] = -100.0f * var24 * var19;
                float var26 = var22 < 0.12f ? AuroraMeshRenderer.smoothstep(var22 / 0.12f) : (float)Math.pow(1.0f - (var22 - 0.12f) / 0.88f, 1.5);
                float var27 = class_3532.method_15363((float)(var16 * var17 * var26 * 0.8f), (float)0.0f, (float)1.0f);
                int var28 = class_9848.method_61319((float)((float)Math.pow(var22, 0.7)), (int)var5, (int)var6) & 0xFFFFFF;
                this.color[var25] = Math.round(var27 * 255.0f) << 24 | var28;
            }
        }
        for (int var29 = 0; var29 < 64; ++var29) {
            for (int var30 = 0; var30 < 6; ++var30) {
                int var31 = var29 * 7 + var30;
                int var32 = (var29 + 1) * 7 + var30;
                this.vertex(var1, var31);
                this.vertex(var1, var32);
                this.vertex(var1, var32 + 1);
                this.vertex(var1, var31 + 1);
            }
        }
    }

    private void vertex(class_287 var1, int var2) {
        GpuQuadMesh.vertex(var1, this.x[var2], this.y[var2], this.z[var2], this.color[var2]);
    }

    static float wave(float var0) {
        return 0.5f * class_3532.method_15374((double)var0) + 0.3f * class_3532.method_15374((double)(var0 * 2.13f + 1.3f)) + 0.2f * class_3532.method_15374((double)(var0 * 4.37f + 2.9f));
    }

    static float wave01(float var0) {
        return 0.5f + 0.5f * AuroraMeshRenderer.wave(var0);
    }

    static float smoothstep(float var0) {
        var0 = class_3532.method_15363((float)var0, (float)0.0f, (float)1.0f);
        return var0 * var0 * (3.0f - 2.0f * var0);
    }

    void close() {
        this.mesh.close();
    }

    static {
        float[][] var10000 = new float[3][];
        int var10002 = 0;
        float[] var10003 = new float[]{-40.0f, 22.0f, 78.0f, 18.0f};
        var10000[var10002] = var10003;
        var10002 = 1;
        var10003 = new float[]{6.0f, 28.0f, 100.0f, 23.0f};
        var10000[var10002] = var10003;
        var10002 = 2;
        var10003 = new float[]{54.0f, 20.0f, 70.0f, 16.0f};
        var10000[var10002] = var10003;
        CURTAINS = var10000;
    }
}

