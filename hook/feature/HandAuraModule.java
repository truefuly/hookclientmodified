/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_310
 *  net.minecraft.class_9848
 */
package hook.feature;

import com.mojang.blaze3d.systems.RenderSystem;
import hook.feature.HandAuraColors;
import hook.feature.HandAuraHelper;
import hook.module.ext.ClientModule;
import hook.module.ext.ModuleCategory;
import hook.module.ext.setting.BooleanSetting;
import hook.module.ext.setting.ColorSetting;
import hook.module.ext.setting.ModeSetting;
import hook.module.ext.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_9848;

@Environment(value=EnvType.CLIENT)
public final class HandAuraModule
extends ClientModule {
    public static volatile HandAuraModule active;
    private static final String CONSTANT_0002 = "Theme";
    private static final String CONSTANT_0462 = "Manual";
    private static final int CONSTANT_0594 = -1;
    private static final float CONSTANT_0336 = 0.55f;
    private static final long CONSTANT_0480 = 3200000000L;
    private static final long CONSTANT_0291 = 4400000000L;
    private final ModeSetting colorMode;
    private final ColorSetting glowColor;
    private final NumberSetting aura;
    private final NumberSetting reach;
    private final NumberSetting rim;
    private final NumberSetting sheen;
    private final ColorSetting sheenColor;
    private final BooleanSetting breathe;

    public HandAuraModule() {
        super("hand-aura", "Hand Aura", "Adds a glowing outline to your hand and held item.", ModuleCategory.VISUALS);
        String var10002 = "color";
        String var10005 = "Color";
        String var10006 = CONSTANT_0002;
        String[] var10007 = new String[]{CONSTANT_0002, CONSTANT_0462};
        this.colorMode = this.addSetting(var10002, new ModeSetting(var10005, var10006, var10007));
        this.glowColor = this.addSetting("glow-color", new ColorSetting("Glow Color", -13338378));
        this.aura = this.addSetting("aura", new NumberSetting("Aura", 65.0f, 0.0f, 100.0f).step(5.0f));
        this.reach = this.addSetting("reach", new NumberSetting("Reach", 8.0f, 2.0f, 20.0f).step(1.0f));
        this.rim = this.addSetting("rim", new NumberSetting("Rim", 40.0f, 0.0f, 100.0f).step(5.0f));
        this.sheen = this.addSetting("sheen", new NumberSetting("Sheen", 25.0f, 0.0f, 100.0f).step(5.0f));
        this.sheenColor = this.addSetting("sheen-color", new ColorSetting("Sheen Color", -3153665));
        this.breathe = this.addSetting("breathe", new BooleanSetting("Breathe", true));
        this.glowColor.visibleWhen(this::manual);
        this.reach.visibleWhen(() -> this.aura.get() > 0.0f);
        this.sheenColor.visibleWhen(() -> this.sheen.get() > 0.0f && this.manual());
    }

    @Override
    public void onEnable() {
        active = this;
    }

    @Override
    public void onDisable() {
        active = null;
        if (RenderSystem.isOnRenderThread()) {
            HandAuraHelper.release();
        } else {
            class_310.method_1551().execute(HandAuraModule::releaseIfOff);
        }
    }

    private static void releaseIfOff() {
        if (active == null) {
            HandAuraHelper.release();
        }
    }

    public static boolean beginCapture() {
        HandAuraModule var0 = active;
        return var0 != null && var0.shows() && HandAuraHelper.begin();
    }

    public static void endCapture(boolean var0) {
        if (HandAuraHelper.end() && var0) {
            HandAuraModule var1 = active;
            float var2 = Math.max(1.0f, (float)class_310.method_1551().method_1522().field_1481 / 1080.0f);
            if (var1 == null) {
                HandAuraHelper.compose(-1, 0.0f, 2.0f, 0.0f, 2.0f * var2, -1, 0.0f, 0.0f);
            } else {
                long var3 = System.nanoTime();
                float var5 = (float)((double)(var3 % 4400000000L) / 4.4E9);
                int var6 = var1.glowArgb();
                HandAuraHelper.compose(var6, var1.auraStrength(var3), var1.reach.get() * var2, var1.rim.get() / 100.0f, 2.0f * var2, var1.sheenArgb(var6), var1.sheen.get() / 100.0f, var5);
            }
        }
    }

    private boolean manual() {
        return this.colorMode.is(CONSTANT_0462);
    }

    private int glowArgb() {
        return this.manual() ? this.glowColor.argb() : HandAuraColors.argb();
    }

    private int sheenArgb(int var1) {
        return this.manual() ? this.sheenColor.argb() : class_9848.method_61319((float)0.55f, (int)var1, (int)-1);
    }

    private boolean shows() {
        return this.aura.get() > 0.0f || this.rim.get() > 0.0f;
    }

    private float auraStrength(long var1) {
        float var3 = this.aura.get() / 100.0f;
        if (!this.breathe.get()) {
            return var3;
        }
        double var4 = (double)(var1 % 3200000000L) / 3.2E9;
        float var6 = (float)Math.sin(var4 * Math.PI);
        return var3 * (0.6f + 0.4f * var6 * var6);
    }
}

