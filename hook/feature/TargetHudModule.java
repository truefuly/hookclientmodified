/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_2596
 *  net.minecraft.class_2824
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_640
 *  net.minecraft.class_7532
 *  net.minecraft.class_8685
 *  org.joml.Matrix3x2fStack
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.PacketSendEvent;
import hook.event.events.Render2DEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.ColorUtil;
import hook.utils.EncryptedString;
import hook.utils.MathUtil;
import hook.utils.RenderUtils;
import hook.utils.TextRenderer;
import java.awt.Color;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2596;
import net.minecraft.class_2824;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_640;
import net.minecraft.class_7532;
import net.minecraft.class_8685;
import org.joml.Matrix3x2fStack;

public final class TargetHudModule
extends Module {
    private final NumberSetting xPosition = new NumberSetting(EncryptedString.of("X"), 0.0, 1920.0, 500.0, 1.0);
    private final NumberSetting yPosition = new NumberSetting(EncryptedString.of("Y"), 0.0, 1080.0, 500.0, 1.0);
    private final BooleanSetting timeoutEnabled = new BooleanSetting(EncryptedString.of("Timeout"), true).setDescription(EncryptedString.of("Target hud will disappear after 10 seconds"));
    private final NumberSetting fadeSpeed = new NumberSetting(EncryptedString.of("Fade Speed"), 5.0, 30.0, 15.0, 1.0).getValue(EncryptedString.of("Speed of animations"));
    private final Color primaryColor = new Color(255, 50, 100);
    private final Color backgroundColor = new Color(0, 0, 0, 175);
    private long lastAttackTime;
    public static float fadeProgress = 1.0f;
    private float currentHealth;
    private AttackPacketDetector hudHandler;

    public TargetHudModule() {
        super(EncryptedString.of("Target HUD"), EncryptedString.of("Displays detailed information about your target with style"), -1, Category.RENDER);
        this.addSettings(this.xPosition, this.yPosition, this.timeoutEnabled, this.fadeSpeed);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onRender2D(Render2DEvent var1) {
        class_332 var2 = var1.context;
        int var3 = this.xPosition.getIntValue();
        int var4 = this.yPosition.getIntValue();
        float var5 = this.fadeSpeed.getFloatValue();
        Color var6 = this.primaryColor;
        Color var7 = this.backgroundColor;
        RenderUtils.unscaledProjection();
        boolean var8 = this.mc.field_1724.method_6052() != null && this.mc.field_1724.method_6052() instanceof class_1657 && this.mc.field_1724.method_6052().method_5805();
        boolean var9 = !this.timeoutEnabled.getValue() || System.currentTimeMillis() - this.lastAttackTime <= 10000L;
        float var10 = var8 && var9 ? 0.0f : 1.0f;
        fadeProgress = RenderUtils.fast(fadeProgress, var10, var5);
        if (fadeProgress < 0.99f && var8) {
            class_1309 var11 = this.mc.field_1724.method_6052();
            class_640 var12 = this.mc.method_1562().method_2871(var11.method_5667());
            Matrix3x2fStack var13 = var2.method_51448();
            var13.pushMatrix();
            float var14 = 1.0f - fadeProgress;
            float var15 = 0.8f + 0.2f * var14;
            var13.translate((float)var3, (float)var4);
            var13.scale(var15, var15);
            var13.translate((float)(-var3), (float)(-var4));
            this.currentHealth = RenderUtils.fast(this.currentHealth, var11.method_6032() + var11.method_6067(), var5 * 0.5f);
            this.drawHud(var2, var3, var4, (class_1657)var11, var12, var14, var6, var7);
            var13.popMatrix();
        }
        RenderUtils.scaledProjection();
    }

    private void drawHud(class_332 var1, int var2, int var3, class_1657 var4, class_640 var5, float var6, Color var7, Color var8) {
        Matrix3x2fStack var9 = var1.method_51448();
        RenderUtils.renderRoundedQuad(var9, new Color(var7.getRed(), var7.getGreen(), var7.getBlue(), (int)(50.0f * var6)), var2 - 5, var3 - 5, var2 + 300 + 5, var3 + 180 + 5, 15.0, 15.0, 15.0, 15.0, 30.0);
        RenderUtils.renderRoundedQuad(var9, new Color(var8.getRed(), var8.getGreen(), var8.getBlue(), (int)((float)var8.getAlpha() * var6)), var2, var3, var2 + 300, var3 + 180, 10.0, 10.0, 10.0, 10.0, 20.0);
        Color var10 = new Color(var7.getRed(), var7.getGreen(), var7.getBlue(), (int)((float)var7.getAlpha() * var6));
        RenderUtils.renderRoundedQuad(var9, var10, var2 + 20, var3, var2 + 300 - 20, var3 + 3, 0.0, 0.0, 0.0, 0.0, 10.0);
        RenderUtils.renderRoundedQuad(var9, var10, var2 + 20, var3 + 180 - 3, var2 + 300 - 20, var3 + 180, 0.0, 0.0, 0.0, 0.0, 10.0);
        if (var5 != null) {
            RenderUtils.renderRoundedQuad(var9, new Color(30, 30, 30, (int)(200.0f * var6)), var2 + 15, var3 + 15, var2 + 85, var3 + 85, 5.0, 5.0, 5.0, 5.0, 10.0);
            class_7532.method_52722((class_332)var1, (class_8685)var5.method_52810(), (int)(var2 + 25), (int)(var3 + 25), (int)50);
            TextRenderer.drawString(var4.method_5477().getString(), var1, var2 + 100, var3 + 25, ColorUtil.a((int)((float)(System.currentTimeMillis() % 1000L) / 1000.0f), 255).getRGB());
            TextRenderer.drawString(MathUtil.roundToNearest(var4.method_5739((class_1297)this.mc.field_1724), 1.0) + " blocks away", var1, var2 + 100, var3 + 45, Color.WHITE.getRGB());
            RenderUtils.renderRoundedQuad(var9, new Color(60, 60, 60, (int)(200.0f * var6)), var2 + 15, var3 + 95, var2 + 300 - 15, var3 + 110, 5.0, 5.0, 5.0, 5.0, 10.0);
            float var11 = this.currentHealth / var4.method_6063();
            float var12 = 270.0f * Math.min(1.0f, var11);
            RenderUtils.renderRoundedQuad(var9, this.healthColor(var11 * (float)((double)0.8f + (double)0.2f * Math.sin((double)System.currentTimeMillis() / 300.0)), var6), var2 + 15, var3 + 95, var2 + 15 + (int)var12, var3 + 110, 5.0, 5.0, 5.0, 5.0, 10.0);
            String var13 = Math.round(this.currentHealth) + "/" + Math.round(var4.method_6063()) + " HP";
            TextRenderer.drawString(var13, var1, var2 + 15 + (int)var12 / 2 - TextRenderer.getWidth(var13) / 2, var3 + 95, Color.WHITE.getRGB());
            int var14 = var3 + 120;
            this.drawStat(var1, var2 + 15, var14, 80, 45, "PING", var5.method_2959() + "ms", this.latencyColor(var5.method_2959(), var6), var10, var6);
            String var15 = var5 != null ? "PLAYER" : "BOT";
            Color var16 = var5 != null ? new Color(100, 255, 100, (int)(255.0f * var6)) : new Color(255, 100, 100, (int)(255.0f * var6));
            this.drawStat(var1, var2 + 100 + 5, var14, 80, 45, "TYPE", var15, var16, var10, var6);
            if (var4.field_6235 > 0) {
                this.drawStat(var1, var2 + 200 + 5, var14, 80, 45, "HURT", "" + var4.field_6235, this.hurtColor(var4.field_6235, var6), var10, var6);
            } else {
                this.drawStat(var1, var2 + 200 + 5, var14, 80, 45, "HURT", "No", new Color(150, 150, 150, (int)(255.0f * var6)), var10, var6);
            }
        } else {
            TextRenderer.drawString("BOT DETECTED", var1, var2 + 150 - TextRenderer.getWidth("BOT DETECTED") / 2, var3 + 90, new Color(255, 50, 50).getRGB());
        }
    }

    private void drawStat(class_332 var1, int var2, int var3, int var4, int var5, String var6, String var7, Color var8, Color var9, float var10) {
        Matrix3x2fStack var11 = var1.method_51448();
        RenderUtils.renderRoundedQuad(var11, var9, var2, var3, var2 + var4, var3 + 3, 3.0, 3.0, 0.0, 0.0, 6.0);
        RenderUtils.renderRoundedQuad(var11, new Color(30, 30, 30, (int)(200.0f * var10)), var2, var3 + 3, var2 + var4, var3 + var5, 0.0, 0.0, 3.0, 3.0, 6.0);
        TextRenderer.drawString(var6, var1, var2 + var4 / 2 - TextRenderer.getWidth(var6) / 2, var3 + 5, new Color(200, 200, 200, (int)(255.0f * var10)).getRGB());
        TextRenderer.drawString(var7, var1, var2 + var4 / 2 - TextRenderer.getWidth(var7) / 2, var3 + var5 - 17, var8.getRGB());
    }

    private Color healthColor(float var1, float var2) {
        Color var3;
        if (var1 > 0.75f) {
            var3 = ColorUtil.a(new Color(100, 255, 100), new Color(255, 255, 100), (1.0f - var1) * 4.0f);
        } else if (var1 > 0.25f) {
            var3 = ColorUtil.a(new Color(255, 255, 100), new Color(255, 100, 100), (0.75f - var1) * 2.0f);
        } else {
            float var4 = var1 < 0.1f ? (float)((double)0.7f + (double)0.3f * Math.sin((double)System.currentTimeMillis() / 200.0)) : 1.0f;
            var3 = new Color((int)(255.0f * var4), (int)(100.0f * var4), (int)(100.0f * var4));
        }
        return new Color(var3.getRed(), var3.getGreen(), var3.getBlue(), (int)((float)var3.getAlpha() * var2));
    }

    private Color latencyColor(int var1, float var2) {
        Color var3 = var1 < 50 ? new Color(100, 255, 100) : (var1 < 100 ? ColorUtil.a(new Color(100, 255, 100), new Color(255, 255, 100), (float)(var1 - 50) / 50.0f) : (var1 < 200 ? ColorUtil.a(new Color(255, 255, 100), new Color(255, 150, 50), (float)(var1 - 100) / 100.0f) : ColorUtil.a(new Color(255, 150, 50), new Color(255, 80, 80), Math.min(1.0f, (float)(var1 - 200) / 300.0f))));
        return new Color(var3.getRed(), var3.getGreen(), var3.getBlue(), (int)((float)var3.getAlpha() * var2));
    }

    private Color hurtColor(int var1, float var2) {
        double var3 = (double)0.7f + (double)0.3f * Math.sin((double)System.currentTimeMillis() / 150.0);
        float var5 = Math.min(1.0f, (float)var1 / 10.0f);
        Color var6 = new Color(255, (int)(50.0f + 100.0f * (1.0f - var5)), (int)(50.0f + 100.0f * (1.0f - var5)));
        return new Color((int)((float)var6.getRed() * (float)var3), (int)((float)var6.getGreen() * (float)var3), (int)((float)var6.getBlue() * (float)var3), (int)(255.0f * var2));
    }

    @EventListener
    public void onPacketSend(PacketSendEvent var1) {
        class_2596<?> class_25962 = var1.packet;
        if (class_25962 instanceof class_2824) {
            class_2824 var2 = (class_2824)class_25962;
            if (this.hudHandler == null) {
                this.hudHandler = new AttackPacketDetector(this);
            }
            if (this.hudHandler.isAttackPacket(var2)) {
                this.lastAttackTime = System.currentTimeMillis();
            }
        }
    }

    public static class AttackPacketDetector {
        public static final class_310 MC = class_310.method_1551();

        AttackPacketDetector(TargetHudModule module) {
        }

        private boolean hasPlayerTarget() {
            return AttackPacketDetector.MC.field_1724 != null && AttackPacketDetector.MC.field_1724.method_6052() instanceof class_1657;
        }

        public boolean isAttackPacket(class_2824 packet) {
            String description;
            try {
                description = packet.toString();
                if (description.contains("ATTACK")) {
                    return true;
                }
            }
            catch (Exception ignored) {
                return this.hasPlayerTarget();
            }
            try {
                if (!this.hasPlayerTarget()) {
                    return false;
                }
                if (description.contains(class_1268.field_5808.toString()) && description.contains("INTERACT_AT")) {
                    return true;
                }
            }
            catch (Exception ignored) {
                return this.hasPlayerTarget();
            }
            try {
                return AttackPacketDetector.MC.field_1724.field_6252 && AttackPacketDetector.MC.field_1724.method_6052() != null;
            }
            catch (Exception ignored) {
                return this.hasPlayerTarget();
            }
        }
    }
}

