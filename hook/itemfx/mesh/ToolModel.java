/*
 * Decompiled with CFR 0.152.
 */
package hook.itemfx.mesh;

import hook.itemfx.mesh.Mat;
import hook.itemfx.mesh.MeshData;
import hook.itemfx.mesh.Sd;
import hook.itemfx.mesh.SurfaceNets;

public final class ToolModel {
    public static final int SWORD = 0;
    public static final int PICKAXE = 1;
    public static final int AXE = 2;
    public static final int SHOVEL = 3;
    public static final int HOE = 4;
    public static final int TYPES = 5;
    public static final int WOOD = 0;
    public static final int STONE = 1;
    public static final int COPPER = 2;
    public static final int IRON = 3;
    public static final int GOLD = 4;
    public static final int DIAMOND = 5;
    public static final int NETHERITE = 6;
    public static final int TIERS = 7;
    public static final int R_TOOL_HEAD = 6;
    private static final float[] AXE_BIT = new float[]{-0.03f, 0.975f, -0.17f, 0.992f, -0.3f, 1.062f, -0.338f, 1.03f, -0.362f, 0.96f, -0.37f, 0.88f, -0.36f, 0.8f, -0.33f, 0.735f, -0.3f, 0.7f, -0.17f, 0.795f, -0.03f, 0.83f};
    private static final float[] SPADE = new float[]{-0.15f, 0.825f, -0.156f, 1.04f, -0.122f, 1.118f, -0.055f, 1.16f, 0.0f, 1.168f, 0.055f, 1.16f, 0.122f, 1.118f, 0.156f, 1.04f, 0.15f, 0.825f, 0.09f, 0.785f, 0.0f, 0.77f, -0.09f, 0.785f};
    private static final float[] HOE_BLADE = new float[]{-0.196f, 1.0f, -0.33f, 1.004f, -0.346f, 0.8f, -0.318f, 0.758f, -0.222f, 0.812f};
    private static final int ARC = 30;
    private final int type;
    private final int tier;
    private final float[] arc = new float[93];

    private ToolModel(int var1, int var2) {
        this.type = var1;
        this.tier = var2;
        for (int var3 = 0; var3 <= 30; ++var3) {
            float var4 = -0.465f + 0.93f * (float)var3 / 30.0f;
            float var5 = Math.abs(var4);
            this.arc[var3 * 3] = var4;
            this.arc[var3 * 3 + 1] = 0.938f - 0.5f * var4 * var4 - 0.35f * var4 * var4 * var4 * var4;
            float var6 = (float)Math.pow(Math.max(0.0f, 1.0f - (float)Math.pow(var5 / 0.47f, 1.7f)), 0.62f);
            this.arc[var3 * 3 + 2] = 0.05f * var6 + 0.0035f;
        }
    }

    public static MeshData build(int var0, int var1) {
        float var3;
        ToolModel var2 = new ToolModel(var0, var1);
        float f = var0 == 0 ? 1.24f : (var3 = var0 == 3 ? 1.19f : 1.09f);
        float var4 = var0 == 1 ? 0.5f : (var0 == 0 ? 0.27f : (var0 == 3 ? 0.19f : 0.4f));
        MeshData var5 = SurfaceNets.build(var2::field, var2::paint, -var4, -0.04f, -0.075f, var4, var3, 0.075f, 0.006666667f, 0.05f);
        float var6 = 0.70710677f;
        float var7 = var0 == 0 ? 1.235f : var3 - 0.02f;
        float var8 = -var7 * var6 * 0.5f;
        for (int var9 = 0; var9 < var5.vertexCount; ++var9) {
            float var10 = var5.pos[var9 * 3];
            float var11 = var5.pos[var9 * 3 + 1];
            var5.pos[var9 * 3] = var6 * var10 + var6 * var11 + var8 - 0.012f;
            var5.pos[var9 * 3 + 1] = -var6 * var10 + var6 * var11 + var8 - 0.012f;
            float var12 = var5.nrm[var9 * 3];
            float var13 = var5.nrm[var9 * 3 + 1];
            var5.nrm[var9 * 3] = var6 * var12 + var6 * var13;
            var5.nrm[var9 * 3 + 1] = -var6 * var12 + var6 * var13;
        }
        float var14 = 0.0f;
        for (int var15 = 0; var15 < var5.vertexCount; ++var15) {
            var14 = Math.max(var14, Math.max(Math.abs(var5.pos[var15 * 3]), Math.abs(var5.pos[var15 * 3 + 1])));
        }
        if (var14 > 0.475f) {
            float var16 = 0.475f / var14;
            for (int var17 = 0; var17 < var5.vertexCount * 3; ++var17) {
                var5.pos[var17] = var5.pos[var17] * var16;
            }
        }
        return var5;
    }

    private float handleTop() {
        return switch (this.type) {
            case 0 -> 0.27f;
            case 3 -> 0.8f;
            default -> 0.952f;
        };
    }

    private float haft(float var1, float var2, float var3) {
        float var4 = this.handleTop();
        if (this.type == 0) {
            return Sd.capsule(var1, var2, var3, 0.0f, 0.06f, 0.0f, 0.0f, var4, 0.0f, 0.03f);
        }
        float var5 = 0.029f + 0.003f * (float)Math.sin(Math.PI * (double)Sd.clamp(var2 / var4, 0.0f, 1.0f));
        return Sd.capsule(var1, var2, var3, 0.0f, 0.02f, 0.0f, 0.0f, var4, 0.0f, var5);
    }

    private float grip(float var1, float var2, float var3) {
        return this.type == 0 ? Sd.capsule(var1, var2, var3, 0.0f, 0.065f, 0.0f, 0.0f, 0.262f, 0.0f, 0.035f) : Sd.capsule(var1, var2, var3, 0.0f, 0.07f, 0.0f, 0.0f, 0.39f, 0.0f, 0.0355f);
    }

    private float fittings(float var1, float var2, float var3) {
        if (this.type == 0) {
            float var9 = Sd.ellipsoid(var1, var2 - 0.034f, var3, 0.05f, 0.044f, 0.04f);
            var9 = Sd.smin(var9, Sd.roundCone(var1, var2, var3, 0.0f, 0.01f, 0.0f, 0.0f, -0.022f, 0.0f, 0.016f, 0.004f), 0.008f);
            float var11 = Math.abs(var1);
            float var6 = Sd.box(var1, var2 - 0.3f, var3, 0.05f, 0.028f, 0.034f, 0.014f);
            var6 = Sd.smin(var6, Sd.roundCone(var11, var2, var3, 0.03f, 0.298f, 0.0f, 0.195f, 0.318f, 0.0f, 0.024f, 0.017f), 0.012f);
            var6 = Sd.smin(var6, Sd.roundCone(var11, var2, var3, 0.195f, 0.318f, 0.0f, 0.232f, 0.362f, 0.0f, 0.017f, 0.008f), 0.008f);
            return Math.min(var9, var6);
        }
        float var4 = Sd.ellipsoid(var1, var2 - 0.018f, var3, 0.04f, 0.032f, 0.04f);
        var4 = Math.min(var4, Sd.torus(var1, var2 - 0.045f, var3, 0.032f, 0.0075f));
        float var5 = this.handleTop() - (this.type == 3 ? 0.03f : 0.115f);
        var4 = Math.min(var4, Sd.torus(var1, var2 - var5, var3, 0.033f, 0.0085f));
        return Math.min(var4, Sd.torus(var1, var2 - 0.4f, var3, 0.037f, 0.006f));
    }

    private float headShape(float var1, float var2, float var3) {
        return switch (this.type) {
            case 1 -> this.pick(var1, var2, var3);
            case 2 -> ToolModel.axe(var1, var2, var3);
            case 3 -> ToolModel.shovel(var1, var2, var3);
            case 4 -> ToolModel.hoe(var1, var2, var3);
            default -> ToolModel.blade(var1, var2, var3);
        };
    }

    private float head(float var1, float var2, float var3) {
        float var4 = this.headShape(var1, var2, var3);
        if (this.tier == 1 && var4 < 0.02f) {
            var4 += 0.0075f * (Sd.fbm(var1 * 22.0f, var2 * 22.0f, var3 * 22.0f, 3) - 0.5f);
        }
        return var4;
    }

    private float pick(float var1, float var2, float var3) {
        if (var2 < 0.7f) {
            return var2 < 0.6f ? 0.2f : 0.7f - var2 + 0.05f;
        }
        float var4 = Sd.box(var1, var2 - 0.936f, var3, 0.05f, 0.074f, 0.046f, 0.02f);
        for (int var5 = 0; var5 < 30; ++var5) {
            float[] var6 = this.arc;
            float var7 = Sd.roundCone(var1, var2, var3 / 0.74f, var6[var5 * 3], var6[var5 * 3 + 1], 0.0f, var6[var5 * 3 + 3], var6[var5 * 3 + 4], 0.0f, var6[var5 * 3 + 2], var6[var5 * 3 + 5]) * 0.74f;
            var4 = Sd.smin(var4, var7, 0.018f);
        }
        return var4;
    }

    private static float axe(float var0, float var1, float var2) {
        if (var1 < 0.66f) {
            return 0.7f - var1;
        }
        float var3 = Sd.box(var0, var1 - 0.905f, var2, 0.042f, 0.086f, 0.046f, 0.016f);
        float var4 = Sd.box(var0 - 0.066f, var1 - 0.905f, var2, 0.032f, 0.058f, 0.041f, 0.016f);
        float var5 = Sd.smoothstep(-0.03f, -0.365f, var0);
        float var6 = Sd.mix(0.043f, 0.0035f, (float)Math.pow(var5, 0.85f));
        float var7 = Math.max(Sd.polygon(var0, var1, AXE_BIT), Math.abs(var2) - var6);
        return Sd.smin(Sd.smin(var3, var4, 0.012f), var7, 0.018f);
    }

    private static float shovel(float var0, float var1, float var2) {
        if (var1 < 0.62f) {
            return 0.66f - var1;
        }
        float var3 = Sd.roundCone(var0, var1, var2 / 0.8f, 0.0f, 0.68f, 0.0f, 0.0f, 0.8f, 0.0f, 0.034f, 0.05f) * 0.8f;
        float var4 = 0.065f * (var0 / 0.156f) * (var0 / 0.156f) - 0.025f + 0.03f * Sd.smoothstep(1.0f, 1.17f, var1);
        float var5 = Math.max(Sd.polygon(var0, var1, SPADE), (Math.abs(var2 - var4) - 0.0115f) * 0.62f);
        return Sd.smin(var3, var5, 0.03f);
    }

    private static float hoe(float var0, float var1, float var2) {
        if (var1 < 0.7f) {
            return 0.74f - var1;
        }
        float var3 = Sd.box(var0, var1 - 0.935f, var2, 0.04f, 0.062f, 0.045f, 0.015f);
        float var4 = Sd.roundCone(var0, var1, var2, -0.03f, 0.958f, 0.0f, -0.215f, 0.975f, 0.0f, 0.03f, 0.021f);
        float var5 = Math.max(Sd.polygon(var0, var1, HOE_BLADE), Math.abs(var2) - Sd.mix(0.016f, 0.004f, Sd.smoothstep(0.93f, 0.76f, var1)));
        return Sd.smin(Sd.smin(var3, var4, 0.014f), var5, 0.016f);
    }

    private static float bladeWidth(float var0) {
        float var1 = Sd.mix(0.08f, 0.06f, Sd.clamp((var0 - 0.33f) / 0.7f, 0.0f, 1.0f));
        float var2 = Sd.smoothstep(1.225f, 1.02f, var0);
        return var1 * (float)Math.sqrt(var2);
    }

    private static float blade(float var0, float var1, float var2) {
        if (var1 < 0.28f) {
            return 0.31f - var1;
        }
        float var3 = ToolModel.bladeWidth(var1);
        float var4 = Math.abs(var0);
        float var5 = Math.max(var4 - var3, var1 - 1.228f);
        float var6 = 0.025f * Sd.clamp((1.24f - var1) / 0.25f, 0.25f, 1.0f);
        float var7 = var6 * (float)Math.pow(Sd.clamp(1.0f - var4 / Math.max(var3, 1.0E-4f), 0.0f, 1.0f), 0.85f) + 0.0016f;
        float var8 = Math.max(Math.max(var5, Math.abs(var2) - var7), 0.32f - var1);
        float var9 = Math.max(Math.max(var4 - 0.0115f, var6 - 0.0072f - Math.abs(var2)), Math.max(0.36f - var1, var1 - 0.9f));
        return Sd.ssub(var8, var9, 0.004f);
    }

    private float field(float var1, float var2, float var3) {
        float var4 = Sd.smin(this.haft(var1, var2, var3), this.grip(var1, var2, var3), 0.006f);
        var4 = Sd.smin(var4, this.fittings(var1, var2, var3), 0.006f);
        return Sd.smin(var4, this.head(var1, var2, var3), 0.008f);
    }

    private float edge(float var1, float var2, float var3) {
        return switch (this.type) {
            case 1 -> {
                float var4 = 1.0f;
                for (int var5 = 0; var5 <= 30; ++var5) {
                    var4 = Math.min(var4, Sd.len(var1 - this.arc[var5 * 3], var2 - this.arc[var5 * 3 + 1]) - this.arc[var5 * 3 + 2]);
                }
                float var6 = Sd.smoothstep(0.25f, 0.45f, Math.abs(var1));
                yield var6 * (1.0f - Sd.smoothstep(-0.004f, 0.02f, -var4));
            }
            case 2 -> Sd.smoothstep(-0.3f, -0.36f, var1);
            case 3 -> Sd.smoothstep(1.1f, 1.165f, var2) + (1.0f - Sd.smoothstep(0.0f, 0.02f, -Sd.polygon(var1, var2, SPADE))) * Sd.smoothstep(1.0f, 1.12f, var2);
            case 4 -> Sd.smoothstep(0.8f, 0.765f, var2) * Sd.smoothstep(-0.22f, -0.3f, var1);
            default -> 1.0f - Sd.smoothstep(0.004f, 0.02f, ToolModel.bladeWidth(var2) - Math.abs(var1));
        };
    }

    private void headMaterial(SurfaceNets.Surface var1) {
        switch (this.tier) {
            case 0: {
                var1.mat = 9;
                var1.rgb = Mat.rgb(0.7f, 0.52f, 0.31f);
                var1.p1 = 0.6f;
                break;
            }
            case 1: {
                var1.mat = 11;
                var1.rgb = Mat.rgb(0.5f, 0.5f, 0.52f);
                var1.p1 = 0.8f;
                break;
            }
            case 2: {
                var1.mat = 14;
                var1.rgb = Mat.rgb(0.95f, 0.58f, 0.42f);
                var1.p1 = 0.3f;
                break;
            }
            case 3: {
                var1.mat = 14;
                var1.rgb = Mat.rgb(0.76f, 0.77f, 0.8f);
                var1.p1 = 0.18f;
                break;
            }
            case 4: {
                var1.mat = 14;
                var1.rgb = Mat.rgb(1.0f, 0.78f, 0.34f);
                var1.p1 = 0.12f;
                break;
            }
            case 5: {
                var1.mat = 12;
                var1.rgb = Mat.rgb(0.32f, 0.94f, 0.88f);
                var1.p1 = 0.05f;
                break;
            }
            default: {
                var1.mat = 14;
                var1.rgb = Mat.rgb(0.25f, 0.215f, 0.24f);
                var1.p1 = 0.26f;
            }
        }
    }

    private void fittingMaterial(SurfaceNets.Surface var1) {
        var1.mat = 1;
        switch (this.tier) {
            case 0: 
            case 1: {
                var1.rgb = Mat.rgb(0.42f, 0.42f, 0.45f);
                var1.p1 = 0.45f;
                break;
            }
            case 2: {
                var1.rgb = Mat.rgb(0.93f, 0.6f, 0.44f);
                var1.p1 = 0.35f;
                break;
            }
            case 3: {
                var1.rgb = Mat.rgb(0.62f, 0.63f, 0.66f);
                var1.p1 = 0.25f;
                break;
            }
            case 4: 
            case 5: {
                var1.rgb = Mat.rgb(1.0f, 0.77f, 0.36f);
                var1.p1 = 0.18f;
                break;
            }
            default: {
                var1.rgb = Mat.rgb(0.34f, 0.29f, 0.31f);
                var1.p1 = 0.3f;
            }
        }
    }

    private void paint(float var1, float var2, float var3, float var4, float var5, float var6, SurfaceNets.Surface var7) {
        float var8 = Math.abs(this.haft(var1, var2, var3));
        float var9 = Math.abs(this.grip(var1, var2, var3));
        float var10 = Math.abs(this.fittings(var1, var2, var3));
        float var11 = Math.abs(this.head(var1, var2, var3));
        float var12 = Math.min(Math.min(var8, var9), Math.min(var10, var11));
        var7.u = (float)(Math.atan2(var3, var1) / (Math.PI * 2) + 0.5);
        var7.v = var2;
        if (var12 == var11) {
            this.headMaterial(var7);
            var7.p2 = Sd.clamp(this.edge(var1, var2, var3), 0.0f, 1.0f);
            var7.region = 6;
            var7.u = var1 * 4.0f;
        } else if (var12 == var10) {
            this.fittingMaterial(var7);
        } else if (var12 == var9) {
            var7.mat = 10;
            var7.rgb = this.tier == 6 ? Mat.rgb(0.1f, 0.07f, 0.08f) : Mat.rgb(0.26f, 0.14f, 0.075f);
            var7.p1 = 0.75f;
        } else {
            var7.mat = 9;
            var7.rgb = this.tier == 6 ? Mat.rgb(0.14f, 0.09f, 0.075f) : Mat.rgb(0.4f, 0.245f, 0.13f);
            var7.p1 = 0.55f;
        }
    }
}

