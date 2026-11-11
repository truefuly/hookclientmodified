/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_1923
 *  net.minecraft.class_2338
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2902$class_2903
 *  net.minecraft.class_2960
 *  net.minecraft.class_3414
 *  net.minecraft.class_368
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_638
 */
package hook.feature;

import hook.Hook;
import hook.event.EventListener;
import hook.event.events.ChunkDataEvent;
import hook.event.events.LightUpdateEvent;
import hook.event.events.Render3DEvent;
import hook.event.events.SetBlockStateEvent;
import hook.event.events.TickEvent;
import hook.feature.AutoRtpModule;
import hook.feature.ChunkFinderToast;
import hook.feature.ChunkLightQueue;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2902;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_368;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_638;

public class ChunkFinderModule
extends Module {
    private static final int MAX_FLAGS = 512;
    private static final long DIG_IGNORE_MS = 4000L;
    private static final double DIG_RANGE_SQ = 64.0;
    private static final class_3414 FLAG_SOUND = class_3414.method_47908((class_2960)class_2960.method_60655((String)"hook", (String)"chunk_flag"));
    private final NumberSetting plateSize;
    private final BooleanSetting outline;
    private final NumberSetting plateThickness;
    private final BooleanSetting advancementNotifications;
    private final BooleanSetting soundPing;
    private final NumberSetting soundVolume;
    private final BooleanSetting kickOnFlag;
    private final ChunkLightQueue lightCache;
    private final Map<Long, FlaggedChunk> flags = new LinkedHashMap<Long, FlaggedChunk>();
    private final Map<Long, Long> recentDigs = new HashMap<Long, Long>();
    private final List<RenderUtils.ColoredBox> renderBoxes = new ArrayList<RenderUtils.ColoredBox>();
    private final List<RenderUtils.ColoredLine> renderLines = new ArrayList<RenderUtils.ColoredLine>();
    private class_638 trackedWorld;

    public ChunkFinderModule() {
        this(EncryptedString.of("Chunk Finder"), Category.DONUT);
    }

    protected ChunkFinderModule(CharSequence name, Category category) {
        super(name, EncryptedString.of("Detects underground base chunks from light updates."), -1, category);
        this.plateSize = new NumberSetting(EncryptedString.of("Plate Size"), 1.0, 64.0, 16.0, 0.5);
        this.outline = new BooleanSetting(EncryptedString.of("Outline"), true);
        this.plateThickness = new NumberSetting(EncryptedString.of("Plate Thickness"), 0.01, 4.0, 0.15, 0.01);
        this.advancementNotifications = new BooleanSetting(EncryptedString.of("Advancement Notifications"), true).setDescription(EncryptedString.of("Shows a Chunk Finder advancement popup for every new flagged chunk"));
        this.soundPing = new BooleanSetting(EncryptedString.of("Sound Ping"), true).setDescription(EncryptedString.of("Plays the original Nexus chunk flag sound"));
        this.soundVolume = new NumberSetting(EncryptedString.of("Sound Volume"), 0.0, 100.0, 100.0, 1.0);
        this.kickOnFlag = new BooleanSetting(EncryptedString.of("Kick On Flag"), false).setDescription(EncryptedString.of("Disconnects on the next flag, then turns this setting and Auto RTP off"));
        this.lightCache = new ChunkLightQueue(this::flagLightChunk);
        this.addSettings(this.plateSize, this.plateThickness, this.outline, this.advancementNotifications, this.soundPing, this.soundVolume, this.kickOnFlag);
    }

    @Override
    public void onEnable() {
        this.reset();
        this.trackedWorld = this.mc.field_1687;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    private void reset() {
        this.lightCache.clear();
        this.flags.clear();
        this.recentDigs.clear();
        this.renderBoxes.clear();
        this.renderLines.clear();
        this.trackedWorld = null;
    }

    private void ensureWorld() {
        if (this.trackedWorld != this.mc.field_1687) {
            this.reset();
            this.trackedWorld = this.mc.field_1687;
        }
    }

    @EventListener
    public void onChunkData(ChunkDataEvent event) {
        this.ensureWorld();
        if (this.mc.field_1687 != null) {
            this.lightCache.offer(event.packet.method_11523(), event.packet.method_11524(), event.packet.method_38599());
        }
    }

    @EventListener
    public void onLightUpdate(LightUpdateEvent event) {
        this.ensureWorld();
        if (this.mc.field_1687 != null) {
            this.lightCache.offer(event.packet.method_11558(), event.packet.method_11554(), event.packet.method_38600());
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        this.ensureWorld();
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            this.lightCache.drain(this.mc.field_1687.method_32890(), this.mc.field_1687.method_32891());
        }
    }

    @EventListener
    public void onBlockChanged(SetBlockStateEvent event) {
        if (this.mc.field_1724 != null && event.pos != null && this.mc.field_1724.method_5707(class_243.method_24953((class_2382)event.pos)) <= 64.0) {
            this.recentDigs.put(class_1923.method_8331((int)(event.pos.method_10263() >> 4), (int)(event.pos.method_10260() >> 4)), System.currentTimeMillis());
        }
    }

    private boolean nearRecentDig(int chunkX, int chunkZ) {
        long now = System.currentTimeMillis();
        this.recentDigs.values().removeIf(timestamp -> now - timestamp > 4000L);
        for (long packedPos : this.recentDigs.keySet()) {
            if (Math.abs(class_1923.method_8325((long)packedPos) - chunkX) > 1 || Math.abs(class_1923.method_8332((long)packedPos) - chunkZ) > 1) continue;
            return true;
        }
        return false;
    }

    private void flagLightChunk(int chunkX, int chunkZ, int lightLevel) {
        if (this.nearRecentDig(chunkX, chunkZ)) {
            return;
        }
        int blockX = (chunkX << 4) + 8;
        int blockZ = (chunkZ << 4) + 8;
        int surfaceY = this.mc.field_1687 != null && this.mc.field_1687.method_8393(chunkX, chunkZ) ? Math.max(21, this.mc.field_1687.method_8624(class_2902.class_2903.field_13202, blockX, blockZ)) : 21;
        this.flagChunk(new class_2338(blockX, surfaceY, blockZ));
    }

    private void flagChunk(class_2338 position) {
        class_1923 chunk = new class_1923(position);
        FlaggedChunk flag = new FlaggedChunk(chunk, Math.max(21, position.method_10264()), position.method_10263(), position.method_10260());
        if (this.flags.putIfAbsent(chunk.method_8324(), flag) != null) {
            return;
        }
        if (this.flags.size() > 512) {
            this.flags.remove(this.flags.keySet().iterator().next());
        }
        if (this.advancementNotifications.getValue()) {
            this.showAdvancement(flag);
        }
        if (this.soundPing.getValue()) {
            float volume = (float)this.soundVolume.getIntValue() / 100.0f;
            this.mc.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)FLAG_SOUND, (float)1.0f, (float)volume));
        }
        if (this.kickOnFlag.getValue() && this.mc.method_1562() != null) {
            this.kickOnFlag.setValue(false);
            AutoRtpModule autoRtp = (AutoRtpModule)Hook.INSTANCE.getModuleManager().getModuleByClass(AutoRtpModule.class);
            if (autoRtp != null) {
                autoRtp.toggle(false);
            }
            Hook.INSTANCE.getSettingsManager().saveNow();
            this.mc.method_1562().method_48296().method_10747((class_2561)class_2561.method_43470((String)("Kicked - Chunk Finder flagged X: " + flag.x() + " Z: " + flag.z() + "\nKick On Flag was automatically turned OFF.")));
        }
    }

    private void showAdvancement(FlaggedChunk flag) {
        this.mc.method_1566().method_1999((class_368)new ChunkFinderToast(flag.x(), flag.z()));
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.flags.isEmpty() || this.mc.field_1724 == null) {
            return;
        }
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null || !camera.method_19332()) {
            return;
        }
        this.renderBoxes.clear();
        this.renderLines.clear();
        double halfSize = this.plateSize.getValue() / 2.0;
        double thickness = this.plateThickness.getValue();
        Color fillColor = new Color(0, 255, 70, 75);
        Color outlineColor = new Color(0, 255, 70, 220);
        class_1923 playerChunk = this.mc.field_1724.method_31476();
        for (FlaggedChunk flag : this.flags.values()) {
            if (Math.abs(flag.chunk().field_9181 - playerChunk.field_9181) > 24 || Math.abs(flag.chunk().field_9180 - playerChunk.field_9180) > 24) continue;
            double centerX = flag.chunk().method_33940();
            double centerZ = flag.chunk().method_33942();
            double topY = (double)flag.y() + 1.002;
            this.renderBoxes.add(new RenderUtils.ColoredBox(centerX - halfSize, topY, centerZ - halfSize, centerX + halfSize, topY + thickness, centerZ + halfSize, fillColor));
            if (!this.outline.getValue()) continue;
            RenderUtils.addBoxOutline(this.renderLines, centerX - halfSize, topY, centerZ - halfSize, centerX + halfSize, topY + thickness, centerZ + halfSize, outlineColor, 1.5f);
        }
        if (!this.renderBoxes.isEmpty()) {
            class_4587 matrices = event.matrixStack;
            matrices.method_22903();
            RenderUtils.applyCameraTransform(matrices, camera);
            RenderUtils.renderFilledBoxes(matrices, this.renderBoxes);
            if (!this.renderLines.isEmpty()) {
                RenderUtils.renderLines(matrices, this.renderLines);
            }
            matrices.method_22909();
        }
    }

    private record FlaggedChunk(class_1923 chunk, int y, int x, int z) {
    }
}

