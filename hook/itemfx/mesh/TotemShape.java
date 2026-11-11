/*
 * Decompiled with CFR 0.152.
 */
package hook.itemfx.mesh;

import hook.itemfx.mesh.Sd;

public final class TotemShape {
    public static final float HEAD_Y = 0.292f;
    public static final float BELT_Y = -0.125f;
    public static final float HEM_Y = -0.455f;
    private static final float[] SLEEVE = new float[]{0.105f, 0.088f, 0.345f, 0.098f, 0.335f, 0.035f, 0.25f, 0.0f, 0.125f, -0.05f};
    private static final float[] SLEEVE_TORN = new float[]{0.105f, 0.088f, 0.345f, 0.098f, 0.335f, 0.032f, 0.312f, 0.004f, 0.291f, 0.026f, 0.262f, -0.024f, 0.236f, 0.006f, 0.206f, -0.05f, 0.181f, -0.014f, 0.152f, -0.074f, 0.128f, -0.04f, 0.105f, -0.056f};
    public final float round;
    public final float blend;
    public final float melt;
    public final float tatter;

    public TotemShape(float var1, float var2, float var3, float var4) {
        this.round = var1;
        this.blend = var2;
        this.melt = var3;
        this.tatter = var4;
    }

    public float head(float var1, float var2, float var3) {
        return Sd.box(var1, var2 - 0.292f, var3, 0.135f, 0.145f, 0.112f, this.round);
    }

    public float torso(float var1, float var2, float var3) {
        float var4 = 1.0f + 0.16f * Sd.clamp((0.14f - var2) / 0.27f, 0.0f, 1.0f);
        return Sd.box(var1 / var4, var2 - 0.005f, var3, 0.098f, 0.135f, 0.082f, Math.min(0.06f, this.round * 0.8f + 0.01f));
    }

    public float arms(float var1, float var2, float var3) {
        float var4 = Math.abs(var1);
        if (var2 < -0.14f) {
            return 1.0f;
        }
        float var5 = Sd.roundCone(var4, var2, var3, 0.085f, 0.09f, 0.0f, 0.355f, 0.106f, 0.0f, 0.058f, 0.043f);
        float var6 = Sd.ellipsoid(var4 - 0.392f, var2 - 0.112f, var3, 0.046f, 0.05f, 0.042f);
        return Sd.smin(var5, var6, 0.03f);
    }

    public float sleeves(float var1, float var2, float var3) {
        float var4 = Math.abs(var1);
        if (!(var2 < -0.14f) && !(var4 < 0.08f)) {
            float var5 = Math.max(Sd.polygon(var4, var2, this.tatter > 0.0f ? SLEEVE_TORN : SLEEVE), Math.abs(var3) - 0.03f);
            return Sd.smax(var5, -var2 - 0.1f, 0.01f) - 0.004f;
        }
        return 1.0f;
    }

    public float sleeveOutline(float var1, float var2) {
        return Sd.polygon(var1, var2, this.tatter > 0.0f ? SLEEVE_TORN : SLEEVE);
    }

    public float hemY(float var1) {
        if (this.tatter <= 0.0f) {
            return -0.455f;
        }
        double var2 = 0.5 + 0.5 * Math.cos((double)(var1 * 9.0f) + (double)0.8f * Math.sin(var1 * 4.0f));
        return -0.44300002f - this.tatter * 0.045f * (float)(var2 * var2 * var2);
    }

    public float skirt(float var1, float var2, float var3) {
        if (var2 > -0.05f) {
            return 0.2f;
        }
        float var4 = Sd.clamp((-0.12f - var2) / 0.34f, 0.0f, 1.0f);
        float var5 = (float)Math.atan2(var3, var1);
        float var6 = var4 * var4 * 0.4f + var4 * 0.6f;
        float var7 = Sd.mix(0.125f, 0.205f, var6) * (1.0f + 0.035f * (float)Math.cos(var5 * 10.0f) * var4);
        float var8 = Sd.mix(0.095f, 0.15f, var6);
        float var9 = (Sd.len(var1 / var7, var3 / var8) - 1.0f) * Math.min(var7, var8);
        var9 = Sd.smax(var9, this.hemY(var5) - var2, 0.02f);
        return Sd.smax(var9, var2 + 0.08f, 0.02f);
    }

    public float belt(float var1, float var2, float var3) {
        return Sd.torus(var1, var2 - -0.125f, var3 / 0.8f, 0.122f, 0.021f) * 0.8f;
    }

    public float body(float var1, float var2, float var3) {
        float var4 = this.blend;
        float var5 = this.head(var1, var2, var3);
        var5 = Sd.smin(var5, this.torso(var1, var2, var3), 0.04f + var4);
        var5 = Sd.smin(var5, this.arms(var1, var2, var3), 0.03f + var4);
        var5 = Sd.smin(var5, this.sleeves(var1, var2, var3), 0.035f + var4);
        var5 = Sd.smin(var5, this.skirt(var1, var2, var3), 0.03f + var4);
        if (this.melt > 0.0f && var5 < 0.05f) {
            var5 -= this.melt * (Sd.fbm(var1 * 6.0f + 3.1f, var2 * 6.0f, var3 * 6.0f, 2) - 0.5f);
        }
        return var5;
    }
}

