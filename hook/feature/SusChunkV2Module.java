/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2IntMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1923
 *  net.minecraft.class_2818
 *  net.minecraft.class_2826
 *  net.minecraft.class_310
 *  net.minecraft.class_638
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.ChunkDataEvent;
import hook.event.events.LightUpdateEvent;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.module.NewFeature;
import hook.module.ext.ClientModule;
import hook.module.ext.ModuleCategory;
import hook.module.ext.setting.BooleanSetting;
import hook.module.ext.setting.NumberSetting;
import hook.module.ext.setting.Setting;
import hook.utils.chunks.ChunkLightSampler;
import hook.utils.chunks.ChunkPeakFinder;
import hook.utils.chunks.ChunkPlateRenderer;
import hook.utils.ext.LevelUtil;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.awt.Color;
import java.util.function.LongConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1923;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_638;

@Environment(value=EnvType.CLIENT)
public final class SusChunkV2Module
extends ClientModule
implements NewFeature {
    private static final long SCAN_BUDGET_NANOS = 3000000L;
    private static final int MIN_SCANNED_NEIGHBOURS = 3;
    private static final int NO_PACKET_SENSITIVITY = 1;
    private static final int VISIBLE_REFRESH_TICKS = 20;
    private static final int PACKET_MIN_Y = -16;
    private static final int PACKET_MAX_Y = 30;
    private static final int GLOW_CELLS_PER_POINT = 12;
    private final NumberSetting spreadRadius;
    private final NumberSetting sensitivity;
    private final BooleanSetting outline;
    private final NumberSetting alpha;
    private final ChunkLightSampler glow;
    private final ChunkPlateRenderer plates;
    private final Long2IntOpenHashMap heat;
    private final Long2IntOpenHashMap vetoes;
    private final Long2IntOpenHashMap chunkScore;
    private final Long2BooleanOpenHashMap chunkUngrown;
    private final Long2BooleanOpenHashMap chunkHasPacket;
    private final LongOpenHashSet scannedChunks;
    private final LongArrayFIFOQueue queue;
    private final LongOpenHashSet queued;
    private Long2IntOpenHashMap visible;
    private boolean visibleDirty;
    private final ChunkPeakFinder peakFinder;
    private int visibleAge;
    private class_638 lastLevel;
    private int appliedRadius;
    private int ticks;
    private final Long2IntOpenHashMap settling;

    public SusChunkV2Module() {
        super("sus-chunk-v2", "Sus Chunk V2", "Detects suspicious underground activity from geode light patterns.", ModuleCategory.DONUT);
        Long2IntOpenHashMap var56;
        ChunkPeakFinder var55;
        boolean var54;
        Long2IntOpenHashMap var53;
        LongOpenHashSet var52;
        LongArrayFIFOQueue var51;
        LongOpenHashSet var50;
        Long2BooleanOpenHashMap var49;
        Long2BooleanOpenHashMap var48;
        Long2IntOpenHashMap var47;
        Long2IntOpenHashMap var46;
        Long2IntOpenHashMap var45;
        ChunkPlateRenderer var44;
        ChunkLightSampler var43;
        NumberSetting var41;
        NumberSetting var42;
        BooleanSetting var38;
        BooleanSetting var39;
        NumberSetting var35;
        NumberSetting var36;
        NumberSetting var32;
        NumberSetting var33;
        String var59 = "spread-radius";
        String var6 = "Simulation Distance";
        float var7 = 5.0f;
        float var8 = 2.0f;
        float var9 = 16.0f;
        NumberSetting var63 = new NumberSetting(var6, var7, var8, var9);
        float var70 = 1.0f;
        NumberSetting var64 = var63.step(var70);
        this.spreadRadius = var33 = (var32 = this.addSetting(var59, var64));
        String var60 = "packet-sensitivity";
        String var73 = "Sensitivity";
        float var76 = 6.0f;
        float var79 = 1.0f;
        float var81 = 20.0f;
        NumberSetting var65 = new NumberSetting(var73, var76, var79, var81);
        float var71 = 1.0f;
        NumberSetting var66 = var65.step(var71);
        this.sensitivity = var36 = (var35 = this.addSetting(var60, var66));
        String var61 = "outline";
        String var74 = "Outline";
        boolean var77 = true;
        BooleanSetting var67 = new BooleanSetting(var74, var77);
        this.outline = var39 = (var38 = this.addSetting(var61, var67));
        String var62 = "alpha";
        String var75 = "Alpha";
        float var78 = 100.0f;
        float var80 = 0.0f;
        float var82 = 255.0f;
        NumberSetting var68 = new NumberSetting(var75, var78, var80, var82);
        float var72 = 1.0f;
        NumberSetting var69 = var68.step(var72);
        this.alpha = var42 = (var41 = this.addSetting(var62, var69));
        this.glow = var43 = new ChunkLightSampler();
        this.plates = var44 = new ChunkPlateRenderer();
        this.heat = var45 = new Long2IntOpenHashMap();
        this.vetoes = var46 = new Long2IntOpenHashMap();
        this.chunkScore = var47 = new Long2IntOpenHashMap();
        this.chunkUngrown = var48 = new Long2BooleanOpenHashMap();
        this.chunkHasPacket = var49 = new Long2BooleanOpenHashMap();
        this.scannedChunks = var50 = new LongOpenHashSet();
        this.queue = var51 = new LongArrayFIFOQueue();
        this.queued = var52 = new LongOpenHashSet();
        this.visible = var53 = new Long2IntOpenHashMap();
        this.visibleDirty = var54 = true;
        this.peakFinder = var55 = new ChunkPeakFinder();
        this.settling = var56 = new Long2IntOpenHashMap();
        NumberSetting var29 = this.sensitivity;
        Runnable var58 = () -> {
            this.visibleDirty = true;
        };
        Setting var30 = var29.onChange(var58);
    }

    @Override
    public void onEnable() {
        this.clearAll();
        this.lastLevel = class_310.method_1551().field_1687;
        this.queueLoadedChunks();
    }

    @Override
    public void onDisable() {
        this.clearAll();
        this.lastLevel = null;
    }

    private void clearAll() {
        this.heat.clear();
        this.vetoes.clear();
        this.chunkScore.clear();
        this.chunkUngrown.clear();
        this.chunkHasPacket.clear();
        this.scannedChunks.clear();
        this.queue.clear();
        this.queued.clear();
        this.settling.clear();
        this.ticks = 0;
        this.visible = new Long2IntOpenHashMap();
        this.visibleDirty = true;
        this.visibleAge = 0;
        this.appliedRadius = this.spreadRadius.getInt();
    }

    private void syncState(class_638 var1) {
        if (var1 != this.lastLevel) {
            this.clearAll();
            this.lastLevel = var1;
            this.queueLoadedChunks();
        }
    }

    private void queueLoadedChunks() {
        class_310 var1 = class_310.method_1551();
        if (var1.field_1687 != null && var1.field_1724 != null) {
            int var2 = (Integer)var1.field_1690.method_42503().method_41753() + 1;
            class_1923 var3 = var1.field_1724.method_31476();
            for (int var4 = var3.field_9181 - var2; var4 <= var3.field_9181 + var2; ++var4) {
                for (int var5 = var3.field_9180 - var2; var5 <= var3.field_9180 + var2; ++var5) {
                    if (!LevelUtil.isLoaded(var1.field_1687, var4, var5)) continue;
                    this.enqueue(class_1923.method_8331((int)var4, (int)var5));
                }
            }
        }
    }

    private void enqueue(long var1) {
        if (this.queued.add(var1)) {
            this.queue.enqueue(var1);
        }
    }

    @EventListener
    public void onChunkData(ChunkDataEvent var1) {
        this.syncState(class_310.method_1551().field_1687);
        this.enqueue(class_1923.method_8331((int)var1.packet.method_11523(), (int)var1.packet.method_11524()));
        this.settleAround(var1.packet.method_11523(), var1.packet.method_11524());
    }

    @EventListener
    public void onLightUpdate(LightUpdateEvent var1) {
        this.syncState(class_310.method_1551().field_1687);
        this.settleAround(var1.packet.method_11558(), var1.packet.method_11554());
    }

    private void settleAround(int var1, int var2) {
        for (int var3 = -1; var3 <= 1; ++var3) {
            for (int var4 = -1; var4 <= 1; ++var4) {
                long var5 = class_1923.method_8331((int)(var1 + var3), (int)(var2 + var4));
                this.settling.put(var5, Math.min(this.settling.getOrDefault(var5, this.ticks + 2), this.ticks + 2));
            }
        }
    }

    private void drainSettling() {
        ObjectIterator var1 = this.settling.long2IntEntrySet().fastIterator();
        while (var1.hasNext()) {
            Long2IntMap.Entry var2 = (Long2IntMap.Entry)var1.next();
            if (var2.getIntValue() > this.ticks) continue;
            this.enqueue(var2.getLongKey());
            var1.remove();
        }
    }

    @EventListener
    public void onTick(TickEvent var1) {
        class_310 var2 = class_310.method_1551();
        class_638 var3 = var2.field_1687;
        this.syncState(var3);
        if (var3 != null) {
            ++this.ticks;
            this.drainSettling();
            if (this.spreadRadius.getInt() != this.appliedRadius) {
                this.clearAll();
                this.queueLoadedChunks();
            }
            long var4 = System.nanoTime() + 3000000L;
            while (!this.queue.isEmpty() && System.nanoTime() < var4) {
                int var9;
                long var6 = this.queue.dequeueLong();
                this.queued.remove(var6);
                int var8 = class_1923.method_8325((long)var6);
                if (!LevelUtil.isLoaded(var3, var8, var9 = class_1923.method_8332((long)var6))) continue;
                this.updateChunk(var6, var3.method_8497(var8, var9), var3);
                if (!this.scannedChunks.add(var6)) continue;
                this.visibleDirty = true;
            }
            if (this.visibleDirty || ++this.visibleAge >= 20) {
                this.visible = this.computeVisible(var3);
                this.visibleDirty = false;
                this.visibleAge = 0;
            }
        }
    }

    private void updateChunk(long var1, class_2818 var3, class_638 var4) {
        int var5 = this.spreadRadius.getInt();
        int var6 = class_1923.method_8325((long)var1);
        int var7 = class_1923.method_8332((long)var1);
        int var8 = this.chunkScore.getOrDefault(var1, 0);
        boolean var9 = this.chunkUngrown.getOrDefault(var1, false);
        boolean var10 = SusChunkV2Module.hasBlocksInRange(var3, -16, 30);
        ChunkEvidence var11 = this.scoreChunk(var3, var4);
        if (!this.chunkHasPacket.containsKey(var1) || this.chunkHasPacket.get(var1) != var10) {
            this.visibleDirty = true;
        }
        this.chunkHasPacket.put(var1, var10);
        if (var8 != var11.selfHeat() || var9 != var11.hasUngrown() || !this.chunkScore.containsKey(var1)) {
            if (var8 > 0) {
                SusChunkV2Module.forArea(var6, var7, var5, var2 -> SusChunkV2Module.subtract(this.heat, var2, var8));
            }
            if (var9) {
                SusChunkV2Module.forArea(var6, var7, var5, var1x -> SusChunkV2Module.subtract(this.vetoes, var1x, 1));
            }
            this.chunkScore.put(var1, var11.selfHeat());
            this.chunkUngrown.put(var1, var11.hasUngrown());
            if (var11.hasUngrown()) {
                SusChunkV2Module.forArea(var6, var7, var5, var1x -> this.vetoes.addTo(var1x, 1));
            }
            if (var11.selfHeat() > 0) {
                SusChunkV2Module.forArea(var6, var7, var5, var2 -> this.heat.addTo(var2, var11.selfHeat()));
            }
            this.visibleDirty = true;
        }
    }

    private static void forArea(int var0, int var1, int var2, LongConsumer var3) {
        for (int var4 = -var2; var4 <= var2; ++var4) {
            for (int var5 = -var2; var5 <= var2; ++var5) {
                var3.accept(class_1923.method_8331((int)(var0 + var4), (int)(var1 + var5)));
            }
        }
    }

    private static void subtract(Long2IntOpenHashMap var0, long var1, int var3) {
        int var4 = var0.get(var1) - var3;
        if (var4 > 0) {
            var0.put(var1, var4);
        } else {
            var0.remove(var1);
        }
    }

    private static boolean hasBlocksInRange(class_2818 var0, int var1, int var2) {
        class_2826[] var3 = var0.method_12006();
        int var4 = var0.method_32891();
        for (int var5 = 0; var5 < var3.length; ++var5) {
            class_2826 var6 = var3[var5];
            if (var6 == null || var6.method_38292()) continue;
            int var7 = var4 + var5 << 4;
            if (var7 >= var1 && var7 + 15 <= var2) {
                return true;
            }
            if (var7 + 15 < var1 || var7 > var2) continue;
            for (int var8 = 0; var8 < 16; ++var8) {
                int var9 = var7 + var8;
                if (var9 < var1 || var9 > var2) continue;
                for (int var10 = 0; var10 < 16; ++var10) {
                    for (int var11 = 0; var11 < 16; ++var11) {
                        if (var6.method_12254(var10, var8, var11).method_26215()) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ChunkEvidence scoreChunk(class_2818 var1, class_638 var2) {
        return new ChunkEvidence(this.glow.scan(var2, var1, null) / 12, false);
    }

    private Long2IntOpenHashMap computeVisible(class_638 var1) {
        Long2IntOpenHashMap var2 = new Long2IntOpenHashMap();
        for (Long2IntMap.Entry var4 : this.heat.long2IntEntrySet()) {
            int var8;
            int var7;
            long var5 = var4.getLongKey();
            if (this.vetoes.containsKey(var5) || !LevelUtil.isLoaded(var1, var7 = class_1923.method_8325((long)var5), var8 = class_1923.method_8332((long)var5)) || this.scannedNeighbours(var7, var8) < 3) continue;
            var2.put(var5, var4.getIntValue());
        }
        int var14 = this.sensitivity.getInt();
        int var15 = this.appliedRadius;
        Long2IntOpenHashMap var6 = new Long2IntOpenHashMap();
        LongOpenHashSet var16 = this.peakFinder.find(var2, var15);
        for (Long2IntMap.Entry var9 : var2.long2IntEntrySet()) {
            int var13;
            long var10 = var9.getLongKey();
            int var12 = var9.getIntValue();
            if (var12 < (var13 = this.chunkHasPacket.getOrDefault(var10, true) ? var14 : 1) || !var16.contains(var10)) continue;
            var6.put(var10, var12);
        }
        return var6;
    }

    private int scannedNeighbours(int var1, int var2) {
        int var3 = 0;
        for (int var4 = -1; var4 <= 1; ++var4) {
            for (int var5 = -1; var5 <= 1; ++var5) {
                if (var4 == 0 && var5 == 0 || !this.scannedChunks.contains(class_1923.method_8331((int)(var1 + var4), (int)(var2 + var5)))) continue;
                ++var3;
            }
        }
        return var3;
    }

    @EventListener
    public void onRender3D(Render3DEvent var1) {
        class_638 var2 = class_310.method_1551().field_1687;
        this.syncState(var2);
        if (var2 != null) {
            int var3 = this.alpha.getInt();
            Color var4 = this.outline.get() ? new Color(255, 242, 75, Math.min(220, var3 + 45)) : null;
            this.plates.draw(var1, this.visible.keySet(), new Color(255, 235, 0, var3), var4, 1.0f);
        }
    }

    @Environment(value=EnvType.CLIENT)
    private record ChunkEvidence(int selfHeat, boolean hasUngrown) {
    }
}

