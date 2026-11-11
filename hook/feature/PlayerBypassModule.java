/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1923
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_5250
 */
package hook.feature;

import hook.Hook;
import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.feature.AutoRtpModule;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.Setting;
import hook.utils.ChunkLightTracker;
import hook.utils.EncryptedString;
import hook.utils.FlagAlarm;
import hook.utils.RenderUtils;
import hook.utils.RenderVisibility;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1923;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_5250;

public final class PlayerBypassModule
extends Module {
    private static final int RENDER_DISTANCE_CHUNKS = 16;
    private final NumberSetting plateSize = new NumberSetting(EncryptedString.of("Plate Size"), 8.0, 64.0, 15.0, 1.0);
    private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 10.0, 255.0, 180.0, 1.0);
    private final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), false);
    private final BooleanSetting kickOnFlag = new BooleanSetting(EncryptedString.of("Kick On Flag"), false).setDescription(EncryptedString.of("Disconnects once, then automatically turns itself off"));
    private final BooleanSetting flagSound = new BooleanSetting(EncryptedString.of("Flag Sound"), true).setDescription(EncryptedString.of("Plays an alarm that continues on the disconnect and menu screens"));
    private final Set<Long> handledFlags = new HashSet<Long>();
    private final List<RenderUtils.ColoredBox> renderBoxes = new ArrayList<RenderUtils.ColoredBox>();
    private final List<RenderUtils.ColoredLine> renderLines = new ArrayList<RenderUtils.ColoredLine>();
    private Object trackedWorld;

    public PlayerBypassModule() {
        super(EncryptedString.of("Player Bypass"), EncryptedString.of("Highlights suspicious light-signal chunks with a yellow through-wall plate"), -1, Category.DONUT);
        Setting[] var10001 = new Setting[]{this.plateSize, this.alpha, this.tracers, this.kickOnFlag, this.flagSound};
        this.addSettings(var10001);
    }

    @Override
    public void onEnable() {
        this.handledFlags.clear();
        this.trackedWorld = this.mc.field_1687;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.handledFlags.clear();
        this.trackedWorld = null;
        this.renderBoxes.clear();
        this.renderLines.clear();
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            if (this.trackedWorld != this.mc.field_1687) {
                this.handledFlags.clear();
                this.trackedWorld = this.mc.field_1687;
            }
            int var2 = this.mc.field_1724.method_24515().method_10263() >> 4;
            int var3 = this.mc.field_1724.method_24515().method_10260() >> 4;
            for (long var5 : ChunkLightTracker.getFlaggedChunks()) {
                int var7 = class_1923.method_8325((long)var5);
                int var8 = class_1923.method_8332((long)var5);
                if (Math.abs(var7 - var2) > 16 || Math.abs(var8 - var3) > 16 || !this.handledFlags.add(var5)) continue;
                this.onFlag(var7, var8);
            }
        }
    }

    private void onFlag(int var1, int var2) {
        int var3 = (var1 << 4) + 8;
        int var4 = (var2 << 4) + 8;
        if (this.flagSound.getValue()) {
            FlagAlarm.trigger(this.mc);
        }
        this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7e[Player Bypass] \u00a7fFlagged chunk at X: " + var3 + " Z: " + var4)), false);
        AutoRtpModule var5 = (AutoRtpModule)Hook.INSTANCE.getModuleManager().getModuleByClass(AutoRtpModule.class);
        if (var5 != null) {
            var5.onBypassFlag();
        }
        if (this.kickOnFlag.getValue() && this.mc.method_1562() != null) {
            this.kickOnFlag.setValue(false);
            class_5250 var6 = class_2561.method_43470((String)("Kicked - Player Bypass flagged X: " + var3 + " Z: " + var4 + "\nKick On Flag was automatically turned OFF."));
            this.mc.method_1562().method_48296().method_10747((class_2561)var6);
        }
    }

    @EventListener
    public void onRender3D(Render3DEvent var1) {
        class_4184 var2;
        if (this.mc.field_1687 != null && this.mc.field_1724 != null && (var2 = RenderUtils.getCamera()) != null && var2.method_19332()) {
            int var3 = this.mc.field_1724.method_24515().method_10263() >> 4;
            int var4 = this.mc.field_1724.method_24515().method_10260() >> 4;
            long var5 = System.currentTimeMillis();
            double var7 = 0.72 + Math.sin((double)var5 / 260.0) * 0.18;
            int var9 = Math.max(1, Math.min(255, (int)((double)this.alpha.getIntValue() * 0.42 * var7)));
            Color var10 = new Color(255, 210, 25, var9);
            Color var11 = new Color(255, 225, 55, this.alpha.getIntValue());
            this.renderBoxes.clear();
            this.renderLines.clear();
            class_243 var12 = RenderUtils.getTracerStart(var2);
            double var13 = this.plateSize.getValue() * 0.5;
            for (long var16 : ChunkLightTracker.getFlaggedChunks()) {
                int var18 = class_1923.method_8325((long)var16);
                int var19 = class_1923.method_8332((long)var16);
                if (Math.abs(var18 - var3) > 16 || Math.abs(var19 - var4) > 16) continue;
                double var20 = (double)(var18 << 4) + 8.0;
                double var22 = (double)(var19 << 4) + 8.0;
                double var24 = 64.25 + Math.sin((double)(var5 + var16) / 420.0) * 0.12;
                double var26 = var24 + 0.16;
                if (!this.tracers.getValue() && !RenderVisibility.box(var20 - var13, var24, var22 - var13, var20 + var13, var26, var22 + var13)) continue;
                this.renderBoxes.add(new RenderUtils.ColoredBox(var20 - var13, var24, var22 - var13, var20 + var13, var26, var22 + var13, var10));
                RenderUtils.addBoxOutline(this.renderLines, var20 - var13, var24, var22 - var13, var20 + var13, var26, var22 + var13, var11, 2.5f);
                if (!this.tracers.getValue()) continue;
                this.renderLines.add(new RenderUtils.ColoredLine(var12, new class_243(var20, var26, var22), var11, 2.0f));
            }
            if (!this.renderBoxes.isEmpty()) {
                class_4587 var28 = var1.matrixStack;
                var28.method_22903();
                RenderUtils.applyCameraTransform(var28, var2);
                RenderUtils.renderFilledBoxes(var28, this.renderBoxes);
                RenderUtils.renderLines(var28, this.renderLines);
                var28.method_22909();
            }
        }
    }
}

