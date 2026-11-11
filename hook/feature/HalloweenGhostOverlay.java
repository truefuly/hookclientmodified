/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.feature.HalloweenIconAtlas;
import hook.feature.UiEasing;
import hook.feature.UiRenderer;
import hook.feature.UiTextureData;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class HalloweenGhostOverlay {
    private static final int GHOSTS = 7;
    static volatile float worldRoll;
    static volatile float worldPitch;
    private static volatile long shakeAt;
    private final Random rnd = new Random();
    private final List<GhostSprite> ghosts = new ArrayList<GhostSprite>();
    private final List<GhostParticle> bats = new ArrayList<GhostParticle>();
    private float batTimer = 5.0f;
    private float sx;
    private float sy;
    private float svx;
    private float svy;
    private float kick;
    private float kickVel;
    private float[] window;

    public static float worldRoll() {
        return System.nanoTime() - shakeAt < 150000000L ? worldRoll : 0.0f;
    }

    public static float worldPitch() {
        return System.nanoTime() - shakeAt < 150000000L ? worldPitch : 0.0f;
    }

    float[] shake(float var1, float var2, float var3, float var4, float var5) {
        float var6 = var4 / 1080.0f * 7.0f * var3;
        float var7 = (float)(Math.sin((double)var5 * 9.7) * 0.6 + Math.sin((double)var5 * 5.3 + 1.1) * 0.4);
        float var8 = (float)(Math.sin((double)var5 * 8.1 + 2.3) * 0.5 + Math.sin((double)var5 * 4.7 + 0.4) * 0.5);
        float var9 = var7 * var2 * var6 * 0.75f;
        float var10 = (var8 * 0.45f + 0.55f) * var2 * var6;
        float var11 = 260.0f;
        float var12 = 2.0f * (float)Math.sqrt(var11) * 0.9f;
        int var13 = Math.max(1, (int)Math.ceil(var1 / 0.004f));
        float var14 = var1 / (float)var13;
        for (int var15 = 0; var15 < var13; ++var15) {
            this.svx += ((var9 - this.sx) * var11 - this.svx * var12) * var14;
            this.svy += ((var10 - this.sy) * var11 - this.svy * var12) * var14;
            this.sx += this.svx * var14;
            this.sy += this.svy * var14;
            this.kickVel += ((var2 * var3 - this.kick) * var11 - this.kickVel * var12) * var14;
            this.kick += this.kickVel * var14;
        }
        worldRoll = this.sx / Math.max(1.0f, var6) * 0.35f * var3;
        worldPitch = this.kick * 0.28f;
        shakeAt = System.nanoTime();
        return new float[]{this.sx, this.sy};
    }

    void calm() {
        worldRoll = 0.0f;
        worldPitch = 0.0f;
    }

    void background(UiRenderer var1, float var2, float var3, float var4, float var5, float var6, float[] var7) {
        this.window = var7;
        if (!(var6 <= 0.003f)) {
            float var8 = Math.max(var2, var3);
            var1.pushAlpha(var6);
            float var9 = var3 * 0.115f;
            float var10 = var2 * 0.83f + (float)Math.sin((double)var4 * 0.05) * var2 * 0.004f;
            float var11 = var3 * 0.17f;
            var1.glow(var10, var11, var9 * 3.4f, UiRenderer.argb(0.055f, 255, 190, 140));
            var1.glow(var10, var11, var9 * 1.5f, UiRenderer.argb(0.1f, 255, 228, 200));
            var1.texture(HalloweenIconAtlas.moon(Math.round(var9)), var10 - var9 / 2.0f, var11 - var9 / 2.0f, var9, var9, UiRenderer.white(0.21f));
            float var12 = var3 * 0.3f;
            float var13 = var2 * 1.25f;
            UiTextureData var14 = HalloweenIconAtlas.mist(512, 128);
            this.mistLayer(var1, var14, var2, var3, var12, var13, var4 * 9.0f, UiRenderer.argb(0.13f, 196, 180, 224));
            this.mistLayer(var1, var14, var2, var3, var12 * 0.75f, var13 * 0.8f, -var4 * 5.5f + var13 * 0.37f, UiRenderer.argb(0.09f, 220, 160, 140));
            float var15 = var3 * 0.26f;
            float var16 = (float)Math.sin((double)var4 * 0.55) * var3 * 0.0018f;
            var1.texture(HalloweenIconAtlas.web(Math.round(var15)), -2.0f + var16, -2.0f, var15, var15, UiRenderer.white(0.068f));
            var1.texture(HalloweenIconAtlas.web(Math.round(var15), true), var2 - var15 + 2.0f - var16, -2.0f, var15, var15, UiRenderer.white(0.058f));
            this.pumpkin(var1, var2 * 0.045f, var3, var3 * 0.13f, var4, 0.0f);
            this.pumpkin(var1, var2 - var2 * 0.075f, var3, var3 * 0.095f, var4, 2.3f);
            this.pumpkin(var1, var2 - var2 * 0.03f - var3 * 0.11f, var3, var3 * 0.065f, var4, 4.1f);
            this.bats(var1, var2, var3, var5, var4);
            this.ghosts(var1, var2, var3, var5, var4, var8);
            var1.popAlpha();
        }
    }

    private void mistLayer(UiRenderer var1, UiTextureData var2, float var3, float var4, float var5, float var6, float var7, int var8) {
        float var9 = var7 % var6;
        if (var9 > 0.0f) {
            var9 -= var6;
        }
        for (float var10 = var9; var10 < var3; var10 += var6) {
            var1.texture(var2, var10, var4 - var5, var6 + 1.0f, var5, var8);
        }
    }

    private void pumpkin(UiRenderer var1, float var2, float var3, float var4, float var5, float var6) {
        float var7 = 0.62f + 0.22f * (float)Math.sin((double)var5 * 7.3 + (double)var6) + 0.16f * (float)Math.sin((double)var5 * 13.1 + (double)(var6 * 2.0f));
        float var8 = var3 - var4 * 0.92f;
        var1.glow(var2, var8 + var4 * 0.55f, var4 * 1.6f, UiRenderer.argb(0.07f * var7, 255, 140, 40));
        var1.glow(var2, var8 + var4 * 0.62f, var4 * 0.55f, UiRenderer.argb(0.16f * var7, 255, 196, 90));
        var1.texture(HalloweenIconAtlas.pumpkin(Math.round(var4)), var2 - var4 / 2.0f, var8, var4, var4, UiRenderer.white(0.78f));
    }

    private void bats(UiRenderer var1, float var2, float var3, float var4, float var5) {
        this.batTimer -= var4;
        if (this.batTimer <= 0.0f && this.bats.size() < 2) {
            GhostParticle var6 = new GhostParticle();
            var6.dir = this.rnd.nextBoolean() ? 1 : -1;
            var6.x = var6.dir > 0 ? -0.08f : 1.08f;
            var6.y = 0.08f + this.rnd.nextFloat() * 0.22f;
            var6.speed = 0.075f + this.rnd.nextFloat() * 0.05f;
            var6.size = 0.028f + this.rnd.nextFloat() * 0.016f;
            var6.phase = this.rnd.nextFloat() * 10.0f;
            this.bats.add(var6);
            this.batTimer = 7.0f + this.rnd.nextFloat() * 9.0f;
        }
        for (int var14 = this.bats.size() - 1; var14 >= 0; --var14) {
            GhostParticle var7 = this.bats.get(var14);
            var7.x += (float)var7.dir * var7.speed * var4;
            var7.age += var4;
            if (!(var7.x < -0.12f) && !(var7.x > 1.12f)) {
                float var8 = (var7.age * 11.0f + var7.phase) % 3.0f;
                int var9 = (int)var8;
                float var10 = var7.size * var3 * 2.0f;
                float var11 = var10 / 2.0f;
                float var12 = (var7.y + 0.018f * (float)Math.sin(var7.age * 3.1f + var7.phase)) * var3;
                float var13 = Math.min(1.0f, Math.min(var7.x + 0.1f, 1.1f - var7.x) / 0.1f);
                var1.texture(HalloweenIconAtlas.bat(Math.round(var10), var9), var7.x * var2 - var10 / 2.0f, var12, var10, var11, UiRenderer.argb(0.42f * var13, 14, 6, 20));
                continue;
            }
            this.bats.remove(var14);
        }
    }

    private void ghosts(UiRenderer var1, float var2, float var3, float var4, float var5, float var6) {
        if (this.ghosts.isEmpty()) {
            for (int var7 = 0; var7 < 7; ++var7) {
                GhostSprite var8 = new GhostSprite();
                this.respawn(var8);
                var8.age = this.rnd.nextFloat() * var8.life;
                this.ghosts.add(var8);
            }
        }
        int var29 = Math.round(var3 * 0.085f);
        for (GhostSprite var9 : this.ghosts) {
            var9.age += var4;
            if (var9.age >= var9.life) {
                this.respawn(var9);
            }
            float var10 = var9.age + var9.seed;
            float var11 = var9.drift + 0.018f * (float)Math.sin(var10 * 0.31f) + 0.01f * (float)Math.sin(var10 * 0.77f + 1.3f);
            float var12 = -0.006f + 0.012f * (float)Math.sin(var10 * 0.23f + 2.1f) + 0.006f * (float)Math.sin(var10 * 0.61f);
            var9.x += var11 * var9.speed * var4;
            var9.y += var12 * var9.speed * var4;
            float var13 = UiEasing.out(var9.age / 2.6f);
            float var14 = UiEasing.out((var9.life - var9.age) / 2.8f);
            float var15 = Math.min(var13, var14);
            float var16 = (float)Math.sin(var5 * 1.25f + var9.seed) * 0.006f;
            float var17 = 1.0f + 0.035f * (float)Math.sin(var5 * 1.9f + var9.seed * 2.0f);
            float var18 = var9.size * var3 * var17;
            float var20 = var18 * 1.25f;
            float var21 = var9.x * var2;
            float var22 = (var9.y + var16) * var3;
            float var23 = var9.alpha * var15 * this.behind(var21, var22, var18);
            if (var23 <= 0.002f) continue;
            var1.glow(var21, var22, var18 * 1.25f, UiRenderer.argb(0.1f * var23, 200, 170, 255));
            var1.glow(var21, var22 - var18 * 0.1f, var18 * 0.55f, UiRenderer.argb(0.12f * var23, 255, 244, 255));
            float var24 = (var5 * 5.5f + var9.seed * 3.0f) % 8.0f;
            int var25 = (int)var24;
            int var26 = (var25 + 1) % 8;
            float var27 = var24 - (float)var25;
            int var28 = UiEasing.mix(UiRenderer.argb(1.0f, 236, 230, 255), UiRenderer.argb(1.0f, 255, 238, 220), var9.warm);
            var1.texture(HalloweenIconAtlas.ghost(var29, var25), var21 - var18 / 2.0f, var22 - var20 / 2.0f, var18, var20, var28 & 0xFFFFFF | Math.round(255.0f * var23 * (1.0f - var27)) << 24);
            var1.texture(HalloweenIconAtlas.ghost(var29, var26), var21 - var18 / 2.0f, var22 - var20 / 2.0f, var18, var20, var28 & 0xFFFFFF | Math.round(255.0f * var23 * var27) << 24);
        }
    }

    private float behind(float var1, float var2, float var3) {
        if (this.window == null) {
            return 1.0f;
        }
        float[] var4 = this.window;
        float var5 = Math.max(var4[0] - var1, var1 - (var4[0] + var4[2]));
        float var6 = Math.max(var4[1] - var2, var2 - (var4[1] + var4[3]));
        float var7 = Math.max(var5, var6);
        float var8 = UiEasing.clamp((var7 + var3 * 0.6f) / (var3 * 1.2f), 0.0f, 1.0f);
        var8 = var8 * var8 * (3.0f - 2.0f * var8);
        return 0.28f + 0.72f * var8;
    }

    private void respawn(GhostSprite var1) {
        var1.x = 0.06f + this.rnd.nextFloat() * 0.88f;
        var1.y = 0.18f + this.rnd.nextFloat() * 0.66f;
        var1.size = 0.045f + this.rnd.nextFloat() * 0.045f;
        var1.speed = 0.6f + this.rnd.nextFloat() * 0.7f;
        var1.drift = (float)(this.rnd.nextBoolean() ? 1 : -1) * (0.004f + this.rnd.nextFloat() * 0.01f);
        var1.seed = this.rnd.nextFloat() * 100.0f;
        var1.life = 13.0f + this.rnd.nextFloat() * 10.0f;
        var1.alpha = 0.18f + this.rnd.nextFloat() * 0.17f;
        var1.warm = this.rnd.nextFloat() * 0.5f;
        var1.age = 0.0f;
    }

    void panelWeb(UiRenderer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, boolean var9) {
        if (!(var8 <= 0.003f)) {
            float var10 = 70.0f * var6;
            var1.pushClip(var2, var3, var4, var5);
            var1.texture(HalloweenIconAtlas.web(Math.round(var10), true), var2 + var4 - var10 - 1.0f, var3 + 1.0f, var10, var10, UiRenderer.white(0.075f * var8));
            var1.popClip();
            if (var9) {
                float var11 = (26.0f + 9.0f * (float)Math.sin(var7 * 0.7f)) * var6;
                float var12 = var2 + var4 - 30.0f * var6;
                float var13 = var3 + 8.0f * var6;
                var1.rect(var12, var13, Math.max(1.0f, var6 * 0.8f), var11, UiRenderer.white(0.1f * var8));
                float var14 = 13.0f * var6;
                var1.texture(HalloweenIconAtlas.spider(Math.round(var14)), var12 - var14 / 2.0f + 0.5f, var13 + var11 - var14 * 0.32f, var14, var14, UiRenderer.argb(0.55f * var8, 18, 10, 24));
            }
        }
    }

    static void lantern(UiRenderer var0, float var1, float var2, float var3, float var4, float var5) {
        if (!(var5 <= 0.003f)) {
            float var6 = 0.7f + 0.3f * (float)Math.sin(var4 * 8.0f) * (float)Math.sin(var4 * 3.1f + 1.0f);
            var0.glow(var1 + var3 / 2.0f, var2 + var3 * 0.62f, var3 * 1.1f, UiRenderer.argb(0.18f * var5 * var6, 255, 150, 50));
            var0.texture(HalloweenIconAtlas.pumpkin(Math.round(var3)), var1, var2, var3, var3, UiRenderer.white(var5));
        }
    }

    static float newBadge(UiRenderer var0, float var1, float var2, float var3, float var4, float var5) {
        float var6 = var0.textWidth("NEW", var3 * 0.52f, 1);
        float var7 = var6 + var3 * 0.9f;
        float var8 = 0.5f + 0.5f * (float)Math.sin(var4 * 2.6f);
        var0.pushAlpha(var5);
        var0.glow(var1 + var7 / 2.0f, var2 + var3 / 2.0f, var7 * 0.9f, UiRenderer.argb(0.1f + 0.08f * var8, 255, 140, 50));
        var0.roundGradient(var1, var2, var7, var3, var3 / 2.0f, UiRenderer.argb(1.0f, 255, 170, 92), UiRenderer.argb(1.0f, 226, 104, 40));
        var0.outline(var1, var2, var7, var3, var3 / 2.0f, Math.max(1.0f, var3 / 22.0f), UiRenderer.argb(0.35f + 0.25f * var8, 190, 120, 255));
        var0.textCentered("NEW", var1 + var7 / 2.0f, var2 + (var3 - var3 * 0.52f) / 2.0f - var3 * 0.04f, var3 * 0.52f, 1, UiRenderer.argb(1.0f, 30, 14, 10));
        var0.popAlpha();
        return var7;
    }

    private static final class GhostParticle {
        float x;
        float y;
        float speed;
        float size;
        float phase;
        float age;
        int dir;

        private GhostParticle() {
        }
    }

    private static final class GhostSprite {
        float x;
        float y;
        float size;
        float speed;
        float drift;
        float seed;
        float life;
        float age;
        float alpha;
        float warm;

        private GhostSprite() {
        }
    }
}

