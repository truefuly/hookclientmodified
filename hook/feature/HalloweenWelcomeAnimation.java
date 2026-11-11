/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.HalloweenFonts;
import hook.feature.HalloweenGhostOverlay;
import hook.feature.UiEasing;
import hook.feature.UiIcon;
import hook.feature.UiRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class HalloweenWelcomeAnimation {
    public static final float CONTINUE_DELAY = 3.0f;
    private static final float WELCOME_IN = 0.45f;
    private static final float BRAND_IN = 0.9f;
    private static final float LANDED = 2.1f;
    private final Random rnd = new Random();
    private final List<WelcomeParticle> motes = new ArrayList<WelcomeParticle>();
    private final float[] button = new float[4];
    private float buttonShown;
    private float hover;
    private float press;
    private float spawn;
    private float flicker = 1.0f;

    public static float continueAt() {
        return 5.1f;
    }

    public boolean overContinue(double var1, double var3) {
        return this.buttonShown > 0.6f && var1 >= (double)this.button[0] && var3 >= (double)this.button[1] && var1 < (double)(this.button[0] + this.button[2]) && var3 < (double)(this.button[1] + this.button[3]);
    }

    public boolean continueVisible() {
        return this.buttonShown > 0.6f;
    }

    public void pressed() {
        this.press = 1.0f;
    }

    private static float expoOut(float var0) {
        return (var0 = UiEasing.clamp(var0, 0.0f, 1.0f)) >= 1.0f ? 1.0f : 1.0f - (float)Math.pow(2.0, -10.0 * (double)var0);
    }

    private static float smooth(float var0) {
        var0 = UiEasing.clamp(var0, 0.0f, 1.0f);
        return var0 * var0 * (3.0f - 2.0f * var0);
    }

    private static int argb(float var0, int var1) {
        return Math.round(UiEasing.clamp(var0, 0.0f, 1.0f) * 255.0f) << 24 | var1 & 0xFFFFFF;
    }

    public void render(UiRenderer var1, int var2, int var3, float var4, float var5, float var6, double var7, double var9) {
        float var61;
        float var11 = UiEasing.inOut(var6);
        float var12 = 1.0f - var11;
        float var13 = (float)var3 / 1080.0f;
        float var14 = (float)var2 / 2.0f;
        float var15 = (float)var3 * 0.42f;
        float var16 = 0.86f + 0.08f * (float)Math.sin(var4 * 7.1f) * (float)Math.sin(var4 * 2.3f + 1.0f) + 0.06f * (float)Math.sin(var4 * 13.7f);
        this.flicker += (var16 - this.flicker) * (1.0f - (float)Math.exp(-var5 * 12.0f));
        float var17 = HalloweenWelcomeAnimation.smooth((var4 - 0.2f) / 1.4f) * var12;
        var1.glow(var14, var15, (float)var3 * 0.7f, UiRenderer.argb(0.38f * var17, 6, 2, 8));
        int var18 = Math.max(18, Math.round(96.0f * var13));
        HalloweenFonts.GlyphLayers var19 = HalloweenFonts.title("HOOK CLIENT", var18, 0.02f);
        float var20 = HalloweenWelcomeAnimation.expoOut((var4 - 0.9f) / 1.25f);
        float var21 = HalloweenWelcomeAnimation.smooth((var4 - 0.9f) / 0.7f) * var12;
        float var22 = (1.0f + 0.1f * (1.0f - var20)) * (1.0f + 0.04f * var11);
        float var23 = (float)var19.w * var22;
        float var24 = (float)var19.h * var22;
        float var25 = var14 - var23 / 2.0f;
        float var26 = var15 - var24 / 2.0f + 14.0f * var13 * (1.0f - var20) - 8.0f * var13 * var11;
        float var27 = (float)Math.exp(-Math.max(0.0f, var4 - 0.9f - 0.55f) * 2.2f) * HalloweenWelcomeAnimation.smooth((var4 - 0.9f - 0.15f) / 0.45f);
        if (var21 > 0.002f) {
            float var28 = (0.3f + 0.4f * var27) * this.flicker;
            var1.texture(var19.glow, var25, var26 + 6.0f * var13 * var22, var23, var24, HalloweenWelcomeAnimation.argb(var28 * var21, 16742942));
            var1.texture(var19.glow, var25, var26, var23, var24, HalloweenWelcomeAnimation.argb(0.22f * var21 * this.flicker, 16762218));
            var1.texture(var19.stroke, var25, var26 + 5.0f * var13 * var22, var23, var24, HalloweenWelcomeAnimation.argb(0.55f * var21, 656134));
            var1.texture(var19.stroke, var25, var26, var23, var24, HalloweenWelcomeAnimation.argb(0.92f * var21, 1509129));
            int var29 = UiEasing.mix(-1648952, -1, this.flicker * 0.9f + var27 * 0.4f);
            var1.texture(var19.fill, var25, var26, var23, var24, var29 & 0xFFFFFF | Math.round(255.0f * var21) << 24);
            float var30 = (var4 - 2.1f - 0.2f) / 2.2f;
            if (var30 > 0.0f && var30 < 1.0f) {
                float var31 = var25 + var23 * (var30 * 1.25f - 0.12f);
                var1.pushClip(var31 - var23 * 0.05f, var26, var23 * 0.1f, var24);
                var1.texture(var19.stroke, var25, var26, var23, var24, HalloweenWelcomeAnimation.argb(0.3f * var21 * (float)Math.sin(Math.PI * (double)var30), 16774368));
                var1.popClip();
            }
        }
        float var57 = HalloweenWelcomeAnimation.expoOut((var4 - 0.45f) / 1.4f);
        float var58 = HalloweenWelcomeAnimation.smooth((var4 - 0.45f) / 0.8f) * var12;
        int var59 = Math.max(10, Math.round(22.0f * var13));
        float var60 = var26 + (float)var19.pad * var22 - 12.0f * var13;
        float var32 = var60 - (float)var59 * 1.9f - (1.0f - var57) * 10.0f * var13 - var11 * 6.0f * var13;
        if (var58 > 0.002f) {
            String var33 = "WELCOME TO";
            float var34 = (float)var59 * (0.95f - 0.5f * var57);
            float var35 = 0.0f;
            for (int var36 = 0; var36 < var33.length(); ++var36) {
                var35 += HalloweenFonts.glyph((char)var33.charAt((int)var36), (int)var59).advance + (var36 + 1 < var33.length() ? var34 : 0.0f);
            }
            float var66 = var14 - var35 / 2.0f;
            int var37 = 15984338;
            for (int var38 = 0; var38 < var33.length(); ++var38) {
                HalloweenFonts.GlyphMask var39 = HalloweenFonts.glyph(var33.charAt(var38), var59);
                float var40 = HalloweenWelcomeAnimation.smooth((var4 - 0.45f - (float)var38 * 0.035f) / 0.6f) * var12;
                var1.texture(var39.mask, var66 - (float)var39.pad, var32 - (float)var39.pad + 3.0f * var13 * (1.0f - var40), var39.mask.w, var39.mask.h, HalloweenWelcomeAnimation.argb(0.9f * var40 * var58, var37));
                var66 += var39.advance + var34;
            }
            float var69 = HalloweenWelcomeAnimation.smooth((var4 - 0.45f - 0.3f) / 1.0f) * var58;
            float var71 = var32 + (float)var59 * 0.38f;
            float var73 = 22.0f * var13;
            float var41 = 150.0f * var13 * var69;
            var1.pushTransform(var14 - var35 / 2.0f - var73 - var41, var71, 1.0f);
            HalloweenWelcomeAnimation.hRule(var1, var41, Math.max(1.0f, 1.4f * var13), HalloweenWelcomeAnimation.argb(0.65f * var69, var37), false);
            var1.popTransform();
            var1.pushTransform(var14 + var35 / 2.0f + var73, var71, 1.0f);
            HalloweenWelcomeAnimation.hRule(var1, var41, Math.max(1.0f, 1.4f * var13), HalloweenWelcomeAnimation.argb(0.65f * var69, var37), true);
            var1.popTransform();
        }
        if ((var61 = HalloweenWelcomeAnimation.smooth((var4 - 0.9f - 0.6f) / 1.1f) * var12) > 0.002f) {
            float var62 = var26 + var24 - (float)var19.pad * var22 + 22.0f * var13;
            float var64 = 210.0f * var13 * var61;
            var1.pushTransform(var14 - var64 - 16.0f * var13, var62, 1.0f);
            HalloweenWelcomeAnimation.hRule(var1, var64, Math.max(1.0f, 1.4f * var13), HalloweenWelcomeAnimation.argb(0.55f * var61, 15980980), false);
            var1.popTransform();
            var1.pushTransform(var14 + 16.0f * var13, var62, 1.0f);
            HalloweenWelcomeAnimation.hRule(var1, var64, Math.max(1.0f, 1.4f * var13), HalloweenWelcomeAnimation.argb(0.55f * var61, 15980980), true);
            var1.popTransform();
            HalloweenGhostOverlay.lantern(var1, var14 - 12.0f * var13, var62 - 13.0f * var13, 24.0f * var13, var4, var61);
        }
        this.motes(var1, var2, var3, var14, var15, var23, var24, var4, var5, var12 * HalloweenWelcomeAnimation.smooth((var4 - 0.9f) / 1.6f));
        float var63 = HalloweenWelcomeAnimation.expoOut((var4 - HalloweenWelcomeAnimation.continueAt()) / 0.9f);
        this.buttonShown = HalloweenWelcomeAnimation.smooth((var4 - HalloweenWelcomeAnimation.continueAt()) / 0.6f) * var12;
        if (this.buttonShown > 0.002f) {
            float var65 = 236.0f * var13;
            float var67 = 54.0f * var13;
            float var68 = (float)var3 * 0.665f;
            boolean var70 = this.buttonShown > 0.6f && var7 >= (double)(var14 - var65 / 2.0f) && var7 < (double)(var14 + var65 / 2.0f) && var9 >= (double)var68 && var9 < (double)(var68 + var67);
            this.hover += ((var70 ? 1.0f : 0.0f) - this.hover) * (1.0f - (float)Math.exp(-14.0f * var5));
            this.press = Math.max(0.0f, this.press - var5 * 3.0f);
            float var72 = (1.0f - var63) * 20.0f * var13 + this.hover * 2.0f * var13;
            float var74 = (0.95f + 0.05f * var63) * (1.0f - 0.03f * this.press);
            float var75 = var65 * var74;
            float var42 = var67 * var74;
            float var43 = var14 - var75 / 2.0f;
            float var44 = var68 - var72 + (var67 - var42) / 2.0f;
            this.button[0] = var14 - var65 / 2.0f;
            this.button[1] = var68;
            this.button[2] = var65;
            this.button[3] = var67;
            var1.pushAlpha(this.buttonShown);
            float var45 = (var4 - HalloweenWelcomeAnimation.continueAt()) % 2.6f / 2.6f;
            if (var63 > 0.98f) {
                float var46 = var45 * 10.0f * var13;
                var1.outline(var43 - var46, var44 - var46, var75 + var46 * 2.0f, var42 + var46 * 2.0f, var42 / 2.0f + var46, Math.max(1.0f, var13), HalloweenWelcomeAnimation.argb(0.2f * (1.0f - var45), 16756832));
            }
            var1.shadow(var43, var44, var75, var42, var42 / 2.0f, 22.0f * var13, UiRenderer.argb(0.5f, 0, 0, 0));
            var1.glow(var43 + var75 / 2.0f, var44 + var42 / 2.0f, var75 * 0.75f, HalloweenWelcomeAnimation.argb(0.05f + 0.1f * this.hover, 16747066));
            var1.round(var43, var44, var75, var42, var42 / 2.0f, UiRenderer.argb(0.7f, 20, 12, 14));
            var1.roundGradient(var43, var44, var75, var42 * 0.55f, var42 / 2.0f, UiRenderer.white(0.06f + 0.04f * this.hover), UiRenderer.white(0.0f));
            var1.round(var43, var44, var75, var42, var42 / 2.0f, HalloweenWelcomeAnimation.argb(0.1f * this.hover + 0.18f * this.press, 16751176));
            var1.outline(var43, var44, var75, var42, var42 / 2.0f, Math.max(1.0f, 1.2f * var13), UiEasing.mix(HalloweenWelcomeAnimation.argb(0.22f, 15984338), HalloweenWelcomeAnimation.argb(0.8f, 16757862), this.hover));
            int var76 = Math.max(8, Math.round(15.0f * var13));
            String var47 = "CONTINUE";
            float var48 = (float)var76 * 0.28f;
            float var49 = 0.0f;
            for (int var50 = 0; var50 < var47.length(); ++var50) {
                var49 += HalloweenFonts.glyph((char)var47.charAt((int)var50), (int)var76).advance + (var50 + 1 < var47.length() ? var48 : 0.0f);
            }
            float var77 = 16.0f * var13;
            float var51 = 10.0f * var13;
            float var52 = var43 + (var75 - var49 - var51 - var77) / 2.0f;
            float var53 = var44 + (var42 - (float)var76) / 2.0f - (float)var76 * 0.08f;
            int var54 = UiEasing.mix(-792878, -7492, this.hover);
            for (int var55 = 0; var55 < var47.length(); ++var55) {
                HalloweenFonts.GlyphMask var56 = HalloweenFonts.glyph(var47.charAt(var55), var76);
                var1.texture(var56.mask, var52 - (float)var56.pad, var53 - (float)var56.pad, var56.mask.w, var56.mask.h, var54);
                var52 += var56.advance + var48;
            }
            var1.icon(UiIcon.CHEVRON_RIGHT, var52 - var48 + var51 + this.hover * 3.0f * var13, var44 + (var42 - var77) / 2.0f, var77, UiEasing.mix(UiRenderer.white(0.75f), HalloweenWelcomeAnimation.argb(1.0f, 16757862), this.hover));
            var1.popAlpha();
        }
    }

    private static void hRule(UiRenderer var0, float var1, float var2, int var3, boolean var4) {
        int var5 = 12;
        float var6 = var1 / (float)var5;
        for (int var7 = 0; var7 < var5; ++var7) {
            float var8 = ((float)var7 + 0.5f) / (float)var5;
            float var9 = var4 ? 1.0f - var8 : var8;
            int var10 = Math.round((float)(var3 >>> 24) * var9 * var9) << 24 | var3 & 0xFFFFFF;
            var0.rect((float)var7 * var6, 0.0f, var6 + 0.5f, var2, var10);
        }
    }

    private void motes(UiRenderer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10) {
        if (!(var10 <= 0.002f)) {
            this.spawn += var9 * 16.0f;
            while (this.spawn >= 1.0f && this.motes.size() < 56) {
                this.spawn -= 1.0f;
                WelcomeParticle var11 = new WelcomeParticle();
                var11.kind = this.rnd.nextFloat() < 0.72f ? 0 : 1;
                var11.x = var4 + (this.rnd.nextFloat() - 0.5f) * var6 * 1.25f;
                var11.y = var5 + var7 * (0.25f + this.rnd.nextFloat() * 0.9f);
                var11.vx = (this.rnd.nextFloat() - 0.5f) * var3 * 0.01f;
                var11.vy = -var3 * (0.03f + this.rnd.nextFloat() * 0.04f) * (var11.kind == 1 ? 0.35f : 1.0f);
                var11.life = 2.4f + this.rnd.nextFloat() * 2.6f;
                var11.size = var3 * (var11.kind == 0 ? 0.0012f + this.rnd.nextFloat() * 0.0018f : 8.0E-4f + this.rnd.nextFloat() * 0.001f);
                var11.seed = this.rnd.nextFloat() * 50.0f;
                this.motes.add(var11);
            }
            for (int var20 = this.motes.size() - 1; var20 >= 0; --var20) {
                WelcomeParticle var12 = this.motes.get(var20);
                var12.age += var9;
                if (var12.age >= var12.life) {
                    this.motes.remove(var20);
                    continue;
                }
                float var13 = 1.0f - var12.age / var12.life;
                var12.x = var12.x + (var12.vx + (float)Math.sin(var12.age * 2.1f + var12.seed) * var3 * 0.012f * (var12.kind == 0 ? 1.0f : 0.4f)) * var9;
                var12.y += var12.vy * var9 * (0.5f + 0.5f * var13);
                float var14 = var12.age / var12.life;
                float var15 = (float)Math.sin(Math.PI * (double)Math.min(1.0f, var14 * 1.2f)) * var10;
                float var16 = (var12.x - var4) / (var6 * 0.52f);
                float var17 = (var12.y - var5) / (var7 * 0.42f);
                var15 *= UiEasing.clamp((var16 * var16 + var17 * var17 - 0.7f) / 0.6f, 0.12f, 1.0f);
                if (var12.kind == 0) {
                    float var18 = 0.65f + 0.35f * (float)Math.sin(var12.age * 19.0f + var12.seed);
                    int var19 = UiEasing.mix(-8032, -38368, var14);
                    var1.glow(var12.x, var12.y, var12.size * 8.0f, HalloweenWelcomeAnimation.argb(0.16f * var15 * var18, 16738842));
                    var1.glow(var12.x, var12.y, var12.size * 2.0f, HalloweenWelcomeAnimation.argb(0.95f * var15 * var18, var19));
                    continue;
                }
                var1.glow(var12.x, var12.y, var12.size * 3.0f, HalloweenWelcomeAnimation.argb(0.35f * var15, 15984338));
            }
        }
    }

    private static final class WelcomeParticle {
        float x;
        float y;
        float vx;
        float vy;
        float life;
        float age;
        float size;
        float seed;
        int kind;

        private WelcomeParticle() {
        }
    }
}

