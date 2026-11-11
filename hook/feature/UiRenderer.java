/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.FontManager;
import hook.feature.UiEasing;
import hook.feature.UiIcon;
import hook.feature.UiImageCache;
import hook.feature.UiProceduralTextures;
import hook.feature.UiTextureData;

public abstract class UiRenderer {
    private final float[] alphaStack = new float[32];
    private int alphaDepth;
    protected float alpha = 1.0f;

    protected abstract void fillRaw(int var1, int var2, int var3, int var4, int var5);

    protected abstract void gradientRaw(int var1, int var2, int var3, int var4, int var5, int var6);

    protected abstract void blitRaw(UiTextureData var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

    public abstract void pushTransform(float var1, float var2, float var3);

    public abstract void popTransform();

    public abstract float scale();

    public abstract void pushClip(float var1, float var2, float var3, float var4);

    public abstract void popClip();

    public abstract void head(float var1, float var2, float var3);

    public abstract void item(Object var1, float var2, float var3, float var4);

    public void imageGradient(String var1, float var2, float var3, float var4, int var5, int var6) {
        float var7 = this.scale();
        int var8 = Math.max(4, Math.round(var4 * var7));
        UiTextureData var9 = UiImageCache.get(var1, var8);
        if (var9 != null) {
            this.pushTransform(0.0f, 0.0f, 1.0f / var7);
            int var10 = Math.round(var2 * var7);
            int var11 = Math.round(var3 * var7);
            for (int var12 = 0; var12 < var8; ++var12) {
                int var13 = this.mul(UiEasing.mix(var5, var6, ((float)var12 + 0.5f) / (float)var8));
                if (var13 >>> 24 == 0) continue;
                this.blitRaw(var9, var10, var11 + var12, var8, 1, 0, var12, var8, 1, var13);
            }
            this.popTransform();
        }
    }

    public void image(String var1, float var2, float var3, float var4, int var5) {
        int var6 = this.mul(var5);
        float var7 = this.scale();
        int var8 = Math.max(4, Math.round(var4 * var7));
        UiTextureData var9 = UiImageCache.get(var1, var8);
        if (var9 != null && var6 >>> 24 != 0) {
            this.pushTransform(0.0f, 0.0f, 1.0f / var7);
            this.blitRaw(var9, Math.round(var2 * var7), Math.round(var3 * var7), var8, var8, 0, 0, var8, var8, var6);
            this.popTransform();
        }
    }

    public void texture(UiTextureData var1, float var2, float var3, float var4, float var5, int var6) {
        this.textureRegion(var1, var2, var3, var4, var5, 0, 0, var1 == null ? 0 : var1.w, var1 == null ? 0 : var1.h, var6);
    }

    public void textureRegion(UiTextureData var1, float var2, float var3, float var4, float var5, int var6, int var7, int var8, int var9, int var10) {
        int var11 = this.mul(var10);
        if (var1 != null && var11 >>> 24 != 0 && var4 > 0.0f && var5 > 0.0f) {
            float var12 = this.scale();
            float var13 = Math.max(1.0E-4f, var12);
            int var14 = Math.max(1, Math.round(var4 * var13));
            int var15 = Math.max(1, Math.round(var5 * var13));
            this.pushTransform(var2, var3, 1.0f / var13);
            this.blitRaw(var1, 0, 0, var14, var15, var6, var7, var8, var9, var11);
            this.popTransform();
        }
    }

    public void pushAlpha(float var1) {
        this.alphaStack[this.alphaDepth++] = this.alpha;
        this.alpha *= Math.max(0.0f, Math.min(1.0f, var1));
    }

    public void popAlpha() {
        this.alpha = this.alphaStack[--this.alphaDepth];
    }

    protected int mul(int var1) {
        if (this.alpha >= 0.999f) {
            return var1;
        }
        int var2 = Math.round((float)(var1 >>> 24) * this.alpha);
        return var2 << 24 | var1 & 0xFFFFFF;
    }

    public void rect(float var1, float var2, float var3, float var4, int var5) {
        if (var3 > 0.0f && var4 > 0.0f && var5 >>> 24 != 0) {
            this.fillRaw(Math.round(var1), Math.round(var2), Math.round(var1 + var3), Math.round(var2 + var4), this.mul(var5));
        }
    }

    public void gradient(float var1, float var2, float var3, float var4, int var5, int var6) {
        if (var3 > 0.0f && var4 > 0.0f) {
            this.gradientRaw(Math.round(var1), Math.round(var2), Math.round(var1 + var3), Math.round(var2 + var4), this.mul(var5), this.mul(var6));
        }
    }

    public void round(float var1, float var2, float var3, float var4, float var5, int var6) {
        int var7 = this.mul(var6);
        if (var7 >>> 24 != 0) {
            int var8 = Math.round(var1);
            int var9 = Math.round(var2);
            int var10 = Math.round(var1 + var3) - var8;
            int var11 = Math.round(var2 + var4) - var9;
            if (var10 > 0 && var11 > 0) {
                int var12 = Math.min(Math.round(var5), Math.min(var10, var11) / 2);
                if (var12 <= 0) {
                    this.fillRaw(var8, var9, var8 + var10, var9 + var11, var7);
                } else {
                    UiTextureData var13 = UiProceduralTextures.circle(var12);
                    this.blitRaw(var13, var8, var9, var12, var12, 0, 0, var12, var12, var7);
                    this.blitRaw(var13, var8 + var10 - var12, var9, var12, var12, var12, 0, var12, var12, var7);
                    this.blitRaw(var13, var8, var9 + var11 - var12, var12, var12, 0, var12, var12, var12, var7);
                    this.blitRaw(var13, var8 + var10 - var12, var9 + var11 - var12, var12, var12, var12, var12, var12, var12, var7);
                    this.fillRaw(var8 + var12, var9, var8 + var10 - var12, var9 + var11, var7);
                    this.fillRaw(var8, var9 + var12, var8 + var12, var9 + var11 - var12, var7);
                    this.fillRaw(var8 + var10 - var12, var9 + var12, var8 + var10, var9 + var11 - var12, var7);
                }
            }
        }
    }

    public void roundGradient(float var1, float var2, float var3, float var4, float var5, int var6, int var7) {
        int var8 = this.mul(var6);
        int var9 = this.mul(var7);
        int var10 = Math.round(var1);
        int var11 = Math.round(var2);
        int var12 = Math.round(var1 + var3) - var10;
        int var13 = Math.round(var2 + var4) - var11;
        if (var12 > 0 && var13 > 0) {
            int var14 = Math.min(Math.round(var5), Math.min(var12, var13) / 2);
            if (var14 <= 0) {
                this.gradientRaw(var10, var11, var10 + var12, var11 + var13, var8, var9);
            } else {
                float var15 = (float)var14 / (float)var13;
                int var16 = UiEasing.mix(var8, var9, var15);
                int var17 = UiEasing.mix(var8, var9, 1.0f - var15);
                UiTextureData var18 = UiProceduralTextures.circle(var14);
                this.blitRaw(var18, var10, var11, var14, var14, 0, 0, var14, var14, UiEasing.mix(var8, var9, var15 * 0.5f));
                this.blitRaw(var18, var10 + var12 - var14, var11, var14, var14, var14, 0, var14, var14, UiEasing.mix(var8, var9, var15 * 0.5f));
                this.blitRaw(var18, var10, var11 + var13 - var14, var14, var14, 0, var14, var14, var14, UiEasing.mix(var8, var9, 1.0f - var15 * 0.5f));
                this.blitRaw(var18, var10 + var12 - var14, var11 + var13 - var14, var14, var14, var14, var14, var14, var14, UiEasing.mix(var8, var9, 1.0f - var15 * 0.5f));
                this.gradientRaw(var10 + var14, var11, var10 + var12 - var14, var11 + var13, var8, var9);
                this.gradientRaw(var10, var11 + var14, var10 + var14, var11 + var13 - var14, var16, var17);
                this.gradientRaw(var10 + var12 - var14, var11 + var14, var10 + var12, var11 + var13 - var14, var16, var17);
            }
        }
    }

    public void circle(float var1, float var2, float var3, int var4) {
        int var5 = Math.max(1, Math.round(var3));
        this.round(Math.round(var1) - var5, Math.round(var2) - var5, var5 * 2, var5 * 2, var5, var4);
    }

    public void outline(float var1, float var2, float var3, float var4, float var5, float var6, int var7) {
        int var8 = this.mul(var7);
        if (var8 >>> 24 != 0) {
            int var9 = Math.round(var1);
            int var10 = Math.round(var2);
            int var11 = Math.round(var1 + var3) - var9;
            int var12 = Math.round(var2 + var4) - var10;
            int var13 = Math.max(1, Math.round(var6));
            int var14 = Math.min(Math.round(var5), Math.min(var11, var12) / 2);
            if (var11 > 2 * var13 && var12 > 2 * var13) {
                if (var14 <= var13) {
                    this.fillRaw(var9, var10, var9 + var11, var10 + var13, var8);
                    this.fillRaw(var9, var10 + var12 - var13, var9 + var11, var10 + var12, var8);
                    this.fillRaw(var9, var10 + var13, var9 + var13, var10 + var12 - var13, var8);
                    this.fillRaw(var9 + var11 - var13, var10 + var13, var9 + var11, var10 + var12 - var13, var8);
                } else {
                    UiTextureData var15 = UiProceduralTextures.ring(var14, var13);
                    this.blitRaw(var15, var9, var10, var14, var14, 0, 0, var14, var14, var8);
                    this.blitRaw(var15, var9 + var11 - var14, var10, var14, var14, var14, 0, var14, var14, var8);
                    this.blitRaw(var15, var9, var10 + var12 - var14, var14, var14, 0, var14, var14, var14, var8);
                    this.blitRaw(var15, var9 + var11 - var14, var10 + var12 - var14, var14, var14, var14, var14, var14, var14, var8);
                    this.fillRaw(var9 + var14, var10, var9 + var11 - var14, var10 + var13, var8);
                    this.fillRaw(var9 + var14, var10 + var12 - var13, var9 + var11 - var14, var10 + var12, var8);
                    this.fillRaw(var9, var10 + var14, var9 + var13, var10 + var12 - var14, var8);
                    this.fillRaw(var9 + var11 - var13, var10 + var14, var9 + var11, var10 + var12 - var14, var8);
                }
            }
        }
    }

    public void shadow(float var1, float var2, float var3, float var4, float var5, float var6, int var7) {
        int var8 = this.mul(var7);
        if (var8 >>> 24 != 0) {
            int var9 = Math.round(var1);
            int var10 = Math.round(var2);
            int var11 = Math.round(var1 + var3) - var9;
            int var12 = Math.round(var2 + var4) - var10;
            int var13 = Math.min(Math.round(var5), Math.min(var11, var12) / 2);
            int var14 = Math.max(1, Math.round(var6));
            int var15 = var13 + var14;
            UiTextureData var16 = UiProceduralTextures.shadow(var13, var14);
            int var17 = var9 - var14;
            int var18 = var10 - var14;
            int var19 = var11 + 2 * var14;
            int var20 = var12 + 2 * var14;
            this.blitRaw(var16, var17, var18, var15, var15, 0, 0, var15, var15, var8);
            this.blitRaw(var16, var17 + var19 - var15, var18, var15, var15, var15, 0, var15, var15, var8);
            this.blitRaw(var16, var17, var18 + var20 - var15, var15, var15, 0, var15, var15, var15, var8);
            this.blitRaw(var16, var17 + var19 - var15, var18 + var20 - var15, var15, var15, var15, var15, var15, var15, var8);
            int var21 = var19 - 2 * var15;
            int var22 = var20 - 2 * var15;
            if (var21 > 0) {
                this.blitRaw(var16, var17 + var15, var18, var21, var15, var15, 0, 1, var15, var8);
                this.blitRaw(var16, var17 + var15, var18 + var20 - var15, var21, var15, var15, var15, 1, var15, var8);
            }
            if (var22 > 0) {
                this.blitRaw(var16, var17, var18 + var15, var15, var22, 0, var15, var15, 1, var8);
                this.blitRaw(var16, var17 + var19 - var15, var18 + var15, var15, var22, var15, var15, var15, 1, var8);
            }
        }
    }

    public void glow(float var1, float var2, float var3, int var4) {
        int var5 = this.mul(var4);
        if (var5 >>> 24 != 0 && var3 >= 1.0f) {
            int var6 = Math.round(var3);
            this.blitRaw(UiProceduralTextures.radial(), Math.round(var1) - var6, Math.round(var2) - var6, var6 * 2, var6 * 2, 0, 0, 128, 128, var5);
        }
    }

    public void icon(UiIcon var1, float var2, float var3, float var4, int var5) {
        int var6 = this.mul(var5);
        if (var6 >>> 24 != 0) {
            float var7 = this.scale();
            int var8 = Math.max(6, Math.round(var4 * var7));
            UiTextureData var9 = var1.tex(var8);
            this.pushTransform(0.0f, 0.0f, 1.0f / var7);
            this.blitRaw(var9, Math.round(var2 * var7), Math.round(var3 * var7), var8, var8, 0, 0, var8, var8, var6);
            this.popTransform();
        }
    }

    public float text(String var1, float var2, float var3, float var4, int var5, int var6) {
        if (var1 != null && !var1.isEmpty()) {
            int var7 = this.mul(var6);
            float var8 = this.scale();
            FontManager.GlyphAtlas var9 = FontManager.atlas(var5, Math.round(var4 * var8));
            if (var9 == null) {
                return 0.0f;
            }
            float var10 = var8 * var4 / (float)var9.px;
            this.pushTransform(0.0f, 0.0f, 1.0f / var8);
            float var11 = var2 * var8;
            float var12 = Math.round(var3 * var8 + (float)var9.px * 0.86f);
            for (int var13 = 0; var13 < var1.length(); ++var13) {
                char var14 = var1.charAt(var13);
                int var15 = var9.index(var14);
                if (var15 < 0) {
                    var15 = var9.index('?');
                }
                if (var9.w[var15] > 0 && var7 >>> 24 != 0) {
                    this.blitRaw(var9.tex, Math.round(var11) + var9.ox[var15], Math.round(var12) + var9.oy[var15], var9.w[var15], var9.h[var15], var9.u[var15], var9.v[var15], var9.w[var15], var9.h[var15], var7);
                }
                var11 += var9.adv[var15] * var10;
            }
            this.popTransform();
            return var11 / var8 - var2;
        }
        return 0.0f;
    }

    public float textWidth(String var1, float var2, int var3) {
        return var1 == null ? 0.0f : FontManager.width(var1, var2, var3);
    }

    public void textCentered(String var1, float var2, float var3, float var4, int var5, int var6) {
        this.text(var1, var2 - this.textWidth(var1, var4, var5) / 2.0f, var3, var4, var5, var6);
    }

    public void textRight(String var1, float var2, float var3, float var4, int var5, int var6) {
        this.text(var1, var2 - this.textWidth(var1, var4, var5), var3, var4, var5, var6);
    }

    public String ellipsize(String var1, float var2, float var3, int var4) {
        int var7;
        if (this.textWidth(var1, var3, var4) <= var2) {
            return var1;
        }
        String var5 = "\u2026";
        float var6 = this.textWidth(var5, var3, var4);
        for (var7 = var1.length(); var7 > 0 && this.textWidth(var1.substring(0, var7), var3, var4) + var6 > var2; --var7) {
        }
        return var1.substring(0, var7).trim() + var5;
    }

    public static int argb(float var0, int var1, int var2, int var3) {
        return Math.round(Math.max(0.0f, Math.min(1.0f, var0)) * 255.0f) << 24 | var1 << 16 | var2 << 8 | var3;
    }

    public static int white(float var0) {
        return UiRenderer.argb(var0, 255, 255, 255);
    }
}

