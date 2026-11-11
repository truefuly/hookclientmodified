/*
 * Decompiled with CFR 0.152.
 */
package hook.itemfx.mesh;

public final class Sd {
    private Sd() {
    }

    public static float len(float var0, float var1) {
        return (float)Math.sqrt(var0 * var0 + var1 * var1);
    }

    public static float len(float var0, float var1, float var2) {
        return (float)Math.sqrt(var0 * var0 + var1 * var1 + var2 * var2);
    }

    public static float clamp(float var0, float var1, float var2) {
        return var0 < var1 ? var1 : (var0 > var2 ? var2 : var0);
    }

    public static float mix(float var0, float var1, float var2) {
        return var0 + (var1 - var0) * var2;
    }

    public static float smoothstep(float var0, float var1, float var2) {
        float var3 = Sd.clamp((var2 - var0) / (var1 - var0), 0.0f, 1.0f);
        return var3 * var3 * (3.0f - 2.0f * var3);
    }

    public static float smin(float var0, float var1, float var2) {
        if (var2 <= 0.0f) {
            return Math.min(var0, var1);
        }
        float var3 = Math.max(var2 - Math.abs(var0 - var1), 0.0f) / var2;
        return Math.min(var0, var1) - var3 * var3 * var2 * 0.25f;
    }

    public static float smax(float var0, float var1, float var2) {
        return -Sd.smin(-var0, -var1, var2);
    }

    public static float ssub(float var0, float var1, float var2) {
        return Sd.smax(var0, -var1, var2);
    }

    public static float sphere(float var0, float var1, float var2, float var3) {
        return Sd.len(var0, var1, var2) - var3;
    }

    public static float ellipsoid(float var0, float var1, float var2, float var3, float var4, float var5) {
        float var6 = Sd.len(var0 / var3, var1 / var4, var2 / var5);
        float var7 = Sd.len(var0 / (var3 * var3), var1 / (var4 * var4), var2 / (var5 * var5));
        return var7 < 1.0E-6f ? -Math.min(var3, Math.min(var4, var5)) : var6 * (var6 - 1.0f) / var7;
    }

    public static float box(float var0, float var1, float var2, float var3, float var4, float var5, float var6) {
        float var7 = Math.abs(var0) - var3 + var6;
        float var8 = Math.abs(var1) - var4 + var6;
        float var9 = Math.abs(var2) - var5 + var6;
        float var10 = Sd.len(Math.max(var7, 0.0f), Math.max(var8, 0.0f), Math.max(var9, 0.0f));
        return var10 + Math.min(Math.max(var7, Math.max(var8, var9)), 0.0f) - var6;
    }

    public static float capsule(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
        float var10 = var0 - var3;
        float var11 = var1 - var4;
        float var12 = var2 - var5;
        float var13 = var6 - var3;
        float var14 = var7 - var4;
        float var15 = var8 - var5;
        float var16 = Sd.clamp((var10 * var13 + var11 * var14 + var12 * var15) / (var13 * var13 + var14 * var14 + var15 * var15), 0.0f, 1.0f);
        return Sd.len(var10 - var13 * var16, var11 - var14 * var16, var12 - var15 * var16) - var9;
    }

    public static float roundCone(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10) {
        float var11 = var6 - var3;
        float var12 = var7 - var4;
        float var13 = var8 - var5;
        float var14 = var11 * var11 + var12 * var12 + var13 * var13;
        float var15 = var9 - var10;
        float var16 = var14 - var15 * var15;
        float var17 = 1.0f / var14;
        float var18 = var0 - var3;
        float var19 = var1 - var4;
        float var20 = var2 - var5;
        float var21 = var18 * var11 + var19 * var12 + var20 * var13;
        float var22 = var21 - var14;
        float var23 = var18 * var14 - var11 * var21;
        float var24 = var19 * var14 - var12 * var21;
        float var25 = var20 * var14 - var13 * var21;
        float var26 = var23 * var23 + var24 * var24 + var25 * var25;
        float var27 = var21 * var21 * var14;
        float var28 = var22 * var22 * var14;
        float var29 = Math.signum(var15) * var15 * var15 * var26;
        if (Math.signum(var22) * var16 * var28 > var29) {
            return (float)Math.sqrt(var26 + var28) * var17 - var10;
        }
        return Math.signum(var21) * var16 * var27 < var29 ? (float)Math.sqrt(var26 + var27) * var17 - var9 : ((float)Math.sqrt(var26 * var16 * var17) + var21 * var15) * var17 - var9;
    }

    public static float torus(float var0, float var1, float var2, float var3, float var4) {
        return Sd.len(Sd.len(var0, var2) - var3, var1) - var4;
    }

    public static float octahedron(float var0, float var1, float var2, float var3) {
        return (Math.abs(var0) + Math.abs(var1) + Math.abs(var2) - var3) * 0.57735026f;
    }

    public static float segment(float var0, float var1, float var2, float var3, float var4, float var5) {
        float var6 = var0 - var2;
        float var7 = var1 - var3;
        float var8 = var4 - var2;
        float var9 = var5 - var3;
        float var10 = Sd.clamp((var6 * var8 + var7 * var9) / (var8 * var8 + var9 * var9), 0.0f, 1.0f);
        return Sd.len(var6 - var8 * var10, var7 - var9 * var10);
    }

    public static float polygon(float var0, float var1, float[] var2) {
        int var3 = var2.length / 2;
        float var4 = (var0 - var2[0]) * (var0 - var2[0]) + (var1 - var2[1]) * (var1 - var2[1]);
        float var5 = 1.0f;
        int var6 = var3 - 1;
        int var7 = 0;
        while (var7 < var3) {
            boolean var21;
            float var8 = var2[var7 * 2];
            float var9 = var2[var7 * 2 + 1];
            float var10 = var2[var6 * 2];
            float var11 = var2[var6 * 2 + 1];
            float var12 = var10 - var8;
            float var13 = var11 - var9;
            float var14 = var0 - var8;
            float var15 = var1 - var9;
            float var16 = Sd.clamp((var14 * var12 + var15 * var13) / (var12 * var12 + var13 * var13), 0.0f, 1.0f);
            float var17 = var14 - var12 * var16;
            float var18 = var15 - var13 * var16;
            var4 = Math.min(var4, var17 * var17 + var18 * var18);
            boolean var19 = var1 >= var9;
            boolean var20 = var1 < var11;
            boolean bl = var21 = var12 * var15 > var13 * var14;
            if (var19 && var20 && var21 || !var19 && !var20 && !var21) {
                var5 = -var5;
            }
            var6 = var7++;
        }
        return var5 * (float)Math.sqrt(var4);
    }

    private static float hash(int var0, int var1, int var2) {
        int var3 = var0 * 374761393 + var1 * 668265263 + var2 * 1274126177;
        var3 = (var3 ^ var3 >>> 13) * 1274126177;
        return (float)((var3 ^ var3 >>> 16) & 0xFFFFFF) / 1.6777215E7f;
    }

    public static float noise(float var0, float var1, float var2) {
        int var3 = (int)Math.floor(var0);
        int var4 = (int)Math.floor(var1);
        int var5 = (int)Math.floor(var2);
        float var6 = var0 - (float)var3;
        float var7 = var1 - (float)var4;
        float var8 = var2 - (float)var5;
        var6 = var6 * var6 * (3.0f - 2.0f * var6);
        var7 = var7 * var7 * (3.0f - 2.0f * var7);
        var8 = var8 * var8 * (3.0f - 2.0f * var8);
        float var9 = Sd.mix(Sd.hash(var3, var4, var5), Sd.hash(var3 + 1, var4, var5), var6);
        float var10 = Sd.mix(Sd.hash(var3, var4 + 1, var5), Sd.hash(var3 + 1, var4 + 1, var5), var6);
        float var11 = Sd.mix(Sd.hash(var3, var4, var5 + 1), Sd.hash(var3 + 1, var4, var5 + 1), var6);
        float var12 = Sd.mix(Sd.hash(var3, var4 + 1, var5 + 1), Sd.hash(var3 + 1, var4 + 1, var5 + 1), var6);
        return Sd.mix(Sd.mix(var9, var10, var7), Sd.mix(var11, var12, var7), var8);
    }

    public static float fbm(float var0, float var1, float var2, int var3) {
        float var4 = 0.0f;
        float var5 = 0.5f;
        float var6 = 0.0f;
        for (int var7 = 0; var7 < var3; ++var7) {
            var4 += Sd.noise(var0, var1, var2) * var5;
            var6 += var5;
            var0 = var0 * 2.03f + 17.1f;
            var1 = var1 * 2.03f + 3.7f;
            var2 = var2 * 2.03f + 9.3f;
            var5 *= 0.5f;
        }
        return var4 / var6;
    }
}

