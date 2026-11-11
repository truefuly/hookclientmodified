/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2IntMap
 *  it.unimi.dsi.fastutil.longs.Long2IntMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2IntMaps
 *  it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  it.unimi.dsi.fastutil.longs.LongCollection
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1923
 *  net.minecraft.class_1937
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2561
 *  net.minecraft.class_2680
 *  net.minecraft.class_2818
 *  net.minecraft.class_2826
 *  net.minecraft.class_310
 *  net.minecraft.class_634
 *  net.minecraft.class_638
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.ChunkDataEvent;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.feature.ChunkFlagToast;
import hook.module.NewFeature;
import hook.module.ext.ClientModule;
import hook.module.ext.EventHandler;
import hook.module.ext.ModuleCategory;
import hook.module.ext.event.BlockUpdateEvent;
import hook.module.ext.setting.BooleanSetting;
import hook.module.ext.setting.ColorSetting;
import hook.module.ext.setting.ModeSetting;
import hook.module.ext.setting.NumberSetting;
import hook.utils.chunks.ChunkPlateRenderer;
import hook.utils.ext.LevelUtil;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_638;

@Environment(value=EnvType.CLIENT)
public final class ChunkFinderBetterModule
extends ClientModule
implements NewFeature {
    private static final int PROBE_CHUNKS = 48;
    private static final byte OTHER = 0;
    private static final byte STONE = 1;
    private static final byte BLOB = 2;
    private static final byte AIR = 3;
    private static final byte MISSING = 4;
    private static final int[] SIDE_X;
    private static final int[] SIDE_Z;
    private static final int[] CORNER_X;
    private static final int[] CORNER_Z;
    private static final int MIN_Y = 0;
    private static final int MAX_Y = 32;
    private static final int CHUNK_COLOR = 0x60FFFFFF;
    private static final int OLD_CHUNK_COLOR = 1614011733;
    private final ModeSetting detection;
    private final NumberSetting spreadRadius;
    private final NumberSetting alpha;
    private final ColorSetting chunkColor;
    private final BooleanSetting sound;
    private final ChunkPlateRenderer plates;
    private static final ChunkCandidate UNDECIDED;
    private final Long2ObjectOpenHashMap<ChunkCandidate> flagged;
    private final LongArrayFIFOQueue queue;
    private final LongOpenHashSet queued;
    private final List<ChunkCandidate> fresh;
    private final Long2IntOpenHashMap heat;
    private int appliedRadius;
    private LongOpenHashSet shown;
    private boolean shownDirty;
    private int shownAge;
    private class_638 lastLevel;
    private boolean scanning;
    private HeightBand appliedConfig;
    private WeakReference<class_634> probeServer;
    private boolean probing;
    private final LongOpenHashSet probed;
    private final class_2818[] around;
    private final int[] groupX;
    private final int[] groupZ;
    private int groupSize;
    private boolean guessed;
    private class_2248 wall;
    private class_2248 lastBlock;
    private final int[] stackTop;
    private final int[] stackShape;
    private final int[] stackAreas;
    private final boolean[] stackGuessed;
    private final class_2248[] stackWall;
    private boolean sawBlob;
    private int scanX;
    private int scanZ;
    private int keepHeight;
    private long[] keptStacks;
    private int keptStackCount;
    private int tallest;
    private long bottom;
    private boolean unsure;

    public ChunkFinderBetterModule() {
        super("chunk-finder-better", "Chunk Finder Better", "Scans suspicious underground shafts with Calm or Aggressive detection.", ModuleCategory.DONUT);
        LongOpenHashSet var65;
        LongOpenHashSet var62;
        Long2IntOpenHashMap var61;
        LongOpenHashSet var59;
        LongArrayFIFOQueue var58;
        Long2ObjectOpenHashMap var57;
        ChunkPlateRenderer var56;
        BooleanSetting var55;
        ColorSetting var52;
        NumberSetting var49;
        NumberSetting var46;
        ModeSetting var43;
        int var10000 = 93709009;
        String var86 = "detection";
        String var6 = "Detection";
        String var7 = "Calm";
        String var10 = "Calm";
        String var116 = "Aggressive";
        String[] var110 = new String[]{var10, var116};
        ModeSetting var92 = new ModeSetting(var6, var7, var110);
        this.detection = var43 = this.addSetting(var86, var92);
        String var87 = "spread-radius";
        String var102 = "Spread Radius";
        NumberSetting var94 = new NumberSetting(var102, 1.0f, 0.0f, 16.0f).step(1.0f);
        this.spreadRadius = var46 = this.addSetting(var87, var94);
        String var88 = "alpha";
        String var103 = "Alpha";
        NumberSetting var96 = new NumberSetting(var103, 96.0f, 0.0f, 255.0f).step(1.0f);
        this.alpha = var49 = this.addSetting(var88, var96);
        String var89 = "chunk-color";
        String var104 = "Chunk Color";
        ColorSetting var97 = new ColorSetting(var104, 0x60FFFFFF);
        this.chunkColor = var52 = this.addSetting(var89, var97);
        String var90 = "sound";
        String var105 = "Sound";
        BooleanSetting var98 = new BooleanSetting(var105, true);
        this.sound = var55 = this.addSetting(var90, var98);
        this.plates = var56 = new ChunkPlateRenderer();
        this.flagged = var57 = new Long2ObjectOpenHashMap();
        this.queue = var58 = new LongArrayFIFOQueue();
        this.queued = var59 = new LongOpenHashSet();
        ArrayList<ChunkCandidate> var60 = new ArrayList<ChunkCandidate>();
        this.fresh = var60;
        this.heat = var61 = new Long2IntOpenHashMap();
        this.shown = var62 = new LongOpenHashSet();
        this.shownDirty = true;
        WeakReference<net.minecraft.class_634> var64 = new WeakReference<>(null);
        this.probeServer = var64;
        this.probed = var65 = new LongOpenHashSet();
        class_2818[] var67 = new class_2818[9];
        this.around = var67;
        int[] var69 = new int[4];
        this.groupX = var69;
        int[] var71 = new int[4];
        this.groupZ = var71;
        int[] var73 = new int[256];
        this.stackTop = var73;
        int[] var75 = new int[256];
        this.stackShape = var75;
        int[] var77 = new int[256];
        this.stackAreas = var77;
        boolean[] var79 = new boolean[256];
        this.stackGuessed = var79;
        class_2248[] var81 = new class_2248[256];
        this.stackWall = var81;
        long[] var83 = new long[16];
        this.keptStacks = var83;
        BooleanSetting var37 = this.usesNotifications();
        BooleanSetting var39 = this.sound;
        BooleanSupplier var85 = var37::get;
        var39.visibleWhen(var85);
    }

    @Override
    public void onEnable() {
        this.startLevel(class_310.method_1551().field_1687);
    }

    @Override
    public void onDisable() {
        this.startLevel(null);
    }

    private void startLevel(class_638 var1) {
        this.flagged.clear();
        this.heat.clear();
        this.appliedRadius = this.spreadRadius.getInt();
        this.queue.clear();
        this.queued.clear();
        this.fresh.clear();
        this.shown = new LongOpenHashSet();
        this.shownDirty = true;
        this.shownAge = 0;
        this.lastLevel = var1;
        boolean bl = this.scanning = var1 != null && var1.method_27983() != class_1937.field_25180 && var1.method_27983() != class_1937.field_25181;
        if (this.scanning) {
            class_634 var2 = class_310.method_1551().method_1562();
            if (var2 != this.probeServer.get()) {
                this.probeServer = new WeakReference<class_634>(var2);
                this.probing = true;
                this.probed.clear();
            }
            this.appliedConfig = this.currentConfig(var1);
            this.queueLoadedChunks(var1);
        }
    }

    private HeightBand currentConfig(class_638 var1) {
        return new HeightBand(Math.max(0, var1.method_31607()), Math.min(32, var1.method_31600()), this.detection.is("Aggressive") ? 3 : 2);
    }

    private void queueLoadedChunks(class_638 var1) {
        class_310 var2 = class_310.method_1551();
        if (var2.field_1724 != null) {
            int var3 = (Integer)var2.field_1690.method_42503().method_41753() + 1;
            class_1923 var4 = var2.field_1724.method_31476();
            for (int var5 = var4.field_9181 - var3; var5 <= var4.field_9181 + var3; ++var5) {
                for (int var6 = var4.field_9180 - var3; var6 <= var4.field_9180 + var3; ++var6) {
                    if (!LevelUtil.isLoaded(var1, var5, var6)) continue;
                    this.enqueue(class_1923.method_8331((int)var5, (int)var6));
                }
            }
        }
    }

    private void enqueue(long var1) {
        if (this.scanning && this.queued.add(var1)) {
            this.queue.enqueue(var1);
        }
    }

    @EventListener
    public void onChunkData(ChunkDataEvent var1) {
        this.shownDirty = true;
        int var2 = var1.packet.method_11523();
        int var3 = var1.packet.method_11524();
        for (int var4 = -1; var4 <= 1; ++var4) {
            for (int var5 = -1; var5 <= 1; ++var5) {
                this.enqueue(class_1923.method_8331((int)(var2 + var4), (int)(var3 + var5)));
            }
        }
    }

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent var1) {
        int var2;
        if (this.scanning && var1.pos != null && (var2 = var1.pos.method_10264()) >= this.appliedConfig.minY() - 1 && var2 <= this.appliedConfig.maxY() + 1 && (ChunkFinderBetterModule.matters(var1.oldState) || ChunkFinderBetterModule.matters(var1.newState))) {
            int var3 = var1.pos.method_10263();
            int var4 = var1.pos.method_10260();
            for (int var5 = var3 - 2 >> 4; var5 <= var3 + 1 >> 4; ++var5) {
                for (int var6 = var4 - 2 >> 4; var6 <= var4 + 2 >> 4; ++var6) {
                    this.enqueue(class_1923.method_8331((int)var5, (int)var6));
                }
            }
        }
    }

    @EventListener
    public void onTick(TickEvent var1) {
        class_638 var2 = class_310.method_1551().field_1687;
        if (var2 != this.lastLevel) {
            this.startLevel(var2);
        }
        if (this.scanning) {
            int var4;
            HeightBand var3 = this.currentConfig(var2);
            if (!var3.equals(this.appliedConfig)) {
                this.appliedConfig = var3;
                this.queueLoadedChunks(var2);
            }
            if ((var4 = this.spreadRadius.getInt()) != this.appliedRadius) {
                this.appliedRadius = var4;
                this.shownDirty = true;
                this.heat.clear();
                ObjectIterator var5 = this.flagged.long2ObjectEntrySet().fastIterator();
                while (var5.hasNext()) {
                    Long2ObjectMap.Entry var6 = (Long2ObjectMap.Entry)var5.next();
                    this.spread(var6.getLongKey(), (ChunkCandidate)var6.getValue(), 1);
                }
            }
            long var13 = System.nanoTime() + 3000000L;
            while (!this.queue.isEmpty() && System.nanoTime() < var13) {
                int var10;
                long var7 = this.queue.dequeueLong();
                this.queued.remove(var7);
                int var9 = class_1923.method_8325((long)var7);
                if (!LevelUtil.isLoaded(var2, var9, var10 = class_1923.method_8332((long)var7))) continue;
                ChunkCandidate var11 = this.scan(var2, var9, var10, var3);
                if (this.sawBlob) {
                    this.probe(var7);
                }
                if (var11 == UNDECIDED) continue;
                ChunkCandidate var12 = (ChunkCandidate)this.flagged.get(var7);
                if (!(var11 == null ? var12 != null : var12 == null || !var12.sameAs(var11))) continue;
                this.refile(var7, var12, var11);
                if (var12 != null) continue;
                this.fresh.add(var11);
            }
            this.announce(this.fresh);
            this.fresh.clear();
            if (this.shownDirty || ++this.shownAge >= 20) {
                this.shown = this.computeShown(var2);
                this.shownDirty = false;
                this.shownAge = 0;
            }
        }
    }

    private void refile(long var1, ChunkCandidate var3, ChunkCandidate var4) {
        this.shownDirty = true;
        if (var3 != null) {
            this.spread(var1, var3, -1);
        }
        if (var4 == null) {
            this.flagged.remove(var1);
        } else {
            this.flagged.put(var1, var4);
            this.spread(var1, var4, 1);
        }
    }

    private void spread(long var1, ChunkCandidate var3, int var4) {
        int var5 = var4 * var3.stacks().length;
        int var6 = this.appliedRadius;
        int var7 = class_1923.method_8325((long)var1);
        int var8 = class_1923.method_8332((long)var1);
        for (int var9 = var7 - var6; var9 <= var7 + var6; ++var9) {
            for (int var10 = var8 - var6; var10 <= var8 + var6; ++var10) {
                long var11 = class_1923.method_8331((int)var9, (int)var10);
                if (this.heat.addTo(var11, var5) + var5 != 0) continue;
                this.heat.remove(var11);
            }
        }
    }

    private LongOpenHashSet computeShown(class_638 var1) {
        Long2IntOpenHashMap var2 = new Long2IntOpenHashMap();
        for (Long2IntMap.Entry var4 : Long2IntMaps.fastIterable((Long2IntMap)this.heat)) {
            long var5 = var4.getLongKey();
            if (!LevelUtil.isLoaded(var1, class_1923.method_8325((long)var5), class_1923.method_8332((long)var5))) continue;
            var2.put(var5, var4.getIntValue());
        }
        return this.appliedRadius == 0 ? new LongOpenHashSet((LongCollection)var2.keySet()) : ChunkFinderBetterModule.hottestPerPatch(var2);
    }

    private static LongOpenHashSet hottestPerPatch(Long2IntOpenHashMap var0) {
        LongOpenHashSet var1 = new LongOpenHashSet();
        LongOpenHashSet var2 = new LongOpenHashSet();
        LongArrayList var3 = new LongArrayList();
        LongArrayFIFOQueue var4 = new LongArrayFIFOQueue();
        LongIterator var5 = var0.keySet().iterator();
        while (var5.hasNext()) {
            long var6 = var5.next();
            if (!var2.add(var6)) continue;
            var3.clear();
            var4.enqueue(var6);
            int var8 = 0;
            while (!var4.isEmpty()) {
                long var9 = var4.dequeueLong();
                var3.add(var9);
                var8 = Math.max(var8, var0.get(var9));
                int var11 = class_1923.method_8325((long)var9);
                int var12 = class_1923.method_8332((long)var9);
                for (int var13 = 0; var13 < 4; ++var13) {
                    long var14 = class_1923.method_8331((int)(var11 + SIDE_X[var13]), (int)(var12 + SIDE_Z[var13]));
                    if (!var0.containsKey(var14) || !var2.add(var14)) continue;
                    var4.enqueue(var14);
                }
            }
            for (int var16 = 0; var16 < var3.size(); ++var16) {
                if (var0.get(var3.getLong(var16)) != var8) continue;
                var1.add(var3.getLong(var16));
            }
        }
        return var1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private ChunkCandidate scan(class_638 var1, int var2, int var3, HeightBand var4) {
        ChunkCandidate var27;
        for (int var5 = -1; var5 <= 1; ++var5) {
            for (int var6 = -1; var6 <= 1; ++var6) {
                this.around[(var5 + 1) * 3 + var6 + 1] = var1.method_2935().method_21730(var2 + var5, var3 + var6);
            }
        }
        this.sawBlob = false;
        this.scanX = var2 << 4;
        this.scanZ = var3 << 4;
        this.keepHeight = var4.stackHeight();
        this.keptStackCount = 0;
        this.tallest = 0;
        this.bottom = 0L;
        this.unsure = false;
        try {
            class_2818 var251 = this.around[4];
            class_2826[] var24 = var251.method_12006();
            int var7 = var251.method_32891();
            Arrays.fill(this.stackTop, Integer.MIN_VALUE);
            boolean var8 = true;
            for (int var9 = 0; var9 < var24.length; ++var9) {
                int var11;
                class_2826 var10 = var24[var9];
                if (var10 == null || var10.method_38292() || (var11 = var7 + var9 << 4) + 15 < var4.minY() || var11 > var4.maxY()) continue;
                int var12x = Math.max(var4.minY() - var11, 0);
                int var13 = Math.min(var4.maxY() - var11, 15);
                if (var8) {
                    boolean bl = var8 = !ChunkFinderBetterModule.hasBlocks(var10, var12x, var13);
                }
                if (!var10.method_19523(var0 -> var0.method_27852(class_2246.field_10340)) || !var10.method_19523(var0 -> ChunkFinderBetterModule.isBlob(var0.method_26204()))) continue;
                for (int var14 = var12x; var14 <= var13; ++var14) {
                    for (int var15 = 0; var15 < 16; ++var15) {
                        boolean var16 = false;
                        for (int var17 = 0; var17 < 16; ++var17) {
                            class_2248 var18 = var10.method_12254(var17, var14, var15).method_26204();
                            boolean var19 = var18 == class_2246.field_10340;
                            if (var19) {
                                if (!var16 && this.isArea(var17, var11 + var14, var15)) {
                                    this.stack(var17, var11 + var14, var15);
                                }
                            } else if (this.probing && ChunkFinderBetterModule.isBlob(var18)) {
                                this.sawBlob = true;
                                if (this.enclosed(var17, var11 + var14, var15)) {
                                    this.probing = false;
                                    this.probed.clear();
                                }
                            }
                            var16 = var19;
                        }
                    }
                }
            }
            for (int var25x = 0; var25x < 256; ++var25x) {
                this.keep(var25x);
            }
            if (this.tallest >= var4.stackHeight()) {
                ChunkCandidate chunkCandidate = new ChunkCandidate(this.bottom, this.tallest, Arrays.copyOf(this.keptStacks, this.keptStackCount));
                return chunkCandidate;
            }
            var27 = !this.unsure && !var8 ? null : UNDECIDED;
        }
        finally {
            Arrays.fill(this.around, null);
        }
        return var27;
    }

    private void stack(int var1, int var2, int var3) {
        int var4 = var3 << 4 | var1;
        int var5 = this.shape(var1, var3);
        boolean var6 = this.stackTop[var4] == var2 - 1 && this.stackShape[var4] == var5 && this.stackWall[var4] == this.wall;
        if (!var6) {
            this.keep(var4);
        }
        this.stackTop[var4] = var2;
        this.stackShape[var4] = var5;
        this.stackWall[var4] = this.wall;
        this.stackAreas[var4] = var6 ? this.stackAreas[var4] + 1 : 1;
        this.stackGuessed[var4] = this.guessed || var6 && this.stackGuessed[var4];
    }

    private void keep(int var1) {
        int var4;
        int var2 = this.stackAreas[var1];
        int var3 = this.stackTop[var1];
        if (var3 != Integer.MIN_VALUE && var2 >= this.keepHeight && this.goesOn(var1 & 0xF, var3 + 1, var1 >> 4, var4 = this.stackShape[var1]) && this.goesOn(var1 & 0xF, var3 - var2, var1 >> 4, var4)) {
            if (this.stackGuessed[var1]) {
                this.unsure = true;
                return;
            }
            int var5 = this.scanX + (var1 & 0xF);
            int var6 = this.scanZ + (var1 >> 4);
            long var7 = class_2338.method_10064((int)var5, (int)(var3 - var2 + 1), (int)var6);
            if (var2 > this.tallest || var2 == this.tallest && ChunkFinderBetterModule.before(var7, this.bottom)) {
                this.tallest = var2;
                this.bottom = var7;
            }
            if (this.keptStackCount == this.keptStacks.length) {
                this.keptStacks = Arrays.copyOf(this.keptStacks, this.keptStacks.length << 1);
            }
            this.keptStacks[this.keptStackCount++] = var7;
        }
    }

    private boolean goesOn(int var1, int var2, int var3, int var4) {
        for (int var5 = 0; var5 < 6; ++var5) {
            if ((var4 >> var5 & 1) == 0) continue;
            this.kind(var1 + var5 / 3, var2, var3 + var5 % 3 - 1);
            if (ChunkFinderBetterModule.carriesStone(this.lastBlock)) continue;
            return false;
        }
        return true;
    }

    private static boolean before(long var0, long var2) {
        if (class_2338.method_10071((long)var0) != class_2338.method_10071((long)var2)) {
            return class_2338.method_10071((long)var0) < class_2338.method_10071((long)var2);
        }
        return class_2338.method_10083((long)var0) != class_2338.method_10083((long)var2) ? class_2338.method_10083((long)var0) < class_2338.method_10083((long)var2) : class_2338.method_10061((long)var0) < class_2338.method_10061((long)var2);
    }

    private int shape(int var1, int var2) {
        int var3 = 0;
        for (int var4 = 0; var4 < this.groupSize; ++var4) {
            var3 |= 1 << (this.groupX[var4] - var1) * 3 + this.groupZ[var4] - var2 + 1;
        }
        return var3;
    }

    private boolean isArea(int var1, int var2, int var3) {
        this.guessed = false;
        this.wall = null;
        if (this.seen(var1 - 1, var2, var3) == 2 && this.sameWall() && this.seen(var1, var2, var3 - 1) == 2 && this.sameWall()) {
            this.groupX[0] = var1;
            this.groupZ[0] = var3;
            int var4 = 1;
            int var5 = var1;
            int var6 = var3;
            int var7 = var3;
            for (int var8 = 0; var8 < var4; ++var8) {
                for (int var9 = 0; var9 < 4; ++var9) {
                    int var10 = this.groupX[var8] + SIDE_X[var9];
                    int var11 = this.groupZ[var8] + SIDE_Z[var9];
                    if (this.inGroup(var10, var11, var4)) continue;
                    byte var12 = this.seen(var10, var2, var11);
                    if (var12 == 2) {
                        if (this.sameWall()) continue;
                        return false;
                    }
                    if (var12 != 1) {
                        return false;
                    }
                    if (var10 < var1 || var10 == var1 && var11 < var3) {
                        return false;
                    }
                    var5 = Math.max(var5, var10);
                    var6 = Math.min(var6, var11);
                    var7 = Math.max(var7, var11);
                    if (var5 - var1 > 1 || var7 - var6 > 1) {
                        return false;
                    }
                    this.groupX[var4] = var10;
                    this.groupZ[var4] = var11;
                    ++var4;
                }
            }
            for (int var13 = 0; var13 < var4; ++var13) {
                for (int var15 = 0; var15 < 4; ++var15) {
                    int var16 = this.groupX[var13] + CORNER_X[var15];
                    int var17 = this.groupZ[var13] + CORNER_Z[var15];
                    if (this.inGroup(var16, var17, var4) || this.seen(var16, var2, var17) == 2 && this.sameWall()) continue;
                    return false;
                }
            }
            for (int var14 = 0; var14 < var4; ++var14) {
                if (this.kind(this.groupX[var14], var2 + 1, this.groupZ[var14]) != 3 && this.kind(this.groupX[var14], var2 - 1, this.groupZ[var14]) != 3) continue;
                return false;
            }
            this.groupSize = var4;
            return true;
        }
        return false;
    }

    private boolean inGroup(int var1, int var2, int var3) {
        for (int var4 = 0; var4 < var3; ++var4) {
            if (this.groupX[var4] != var1 || this.groupZ[var4] != var2) continue;
            return true;
        }
        return false;
    }

    private byte seen(int var1, int var2, int var3) {
        byte var4 = this.kind(var1, var2, var3);
        if (var4 != 4) {
            return var4;
        }
        this.guessed = true;
        return 2;
    }

    private boolean sameWall() {
        if (this.lastBlock == null) {
            return true;
        }
        if (this.wall == null) {
            this.wall = this.lastBlock;
        }
        return this.lastBlock == this.wall;
    }

    private byte kind(int var1, int var2, int var3) {
        class_2818 var4 = this.around[((var1 >> 4) + 1) * 3 + (var3 >> 4) + 1];
        this.lastBlock = null;
        if (var4 == null) {
            return 4;
        }
        int var5 = var4.method_31602(var2);
        if (var5 >= 0 && var5 < var4.method_32890()) {
            class_2680 var6 = var4.method_38259(var5).method_12254(var1 & 0xF, var2 & 0xF, var3 & 0xF);
            this.lastBlock = var6.method_26204();
            return ChunkFinderBetterModule.kindOf(var6);
        }
        return 0;
    }

    private boolean enclosed(int var1, int var2, int var3) {
        return this.full(var1 + 1, var2, var3) && this.full(var1 - 1, var2, var3) && this.full(var1, var2, var3 + 1) && this.full(var1, var2, var3 - 1) && this.full(var1, var2 + 1, var3) && this.full(var1, var2 - 1, var3);
    }

    private boolean full(int var1, int var2, int var3) {
        int var5;
        class_2818 var4 = this.around[((var1 >> 4) + 1) * 3 + (var3 >> 4) + 1];
        return var4 == null ? false : (var5 = var4.method_31602(var2)) >= 0 && var5 < var4.method_32890() && var4.method_38259(var5).method_12254(var1 & 0xF, var2 & 0xF, var3 & 0xF).method_26216();
    }

    private static boolean hasBlocks(class_2826 var0, int var1, int var2) {
        if (!var0.method_19523(var0x -> !var0x.method_26215())) {
            return false;
        }
        for (int var3 = var1; var3 <= var2; ++var3) {
            for (int var4 = 0; var4 < 16; ++var4) {
                for (int var5 = 0; var5 < 16; ++var5) {
                    if (var0.method_12254(var5, var3, var4).method_26215()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private static byte kindOf(class_2680 var0) {
        class_2248 var1 = var0.method_26204();
        if (var1 == class_2246.field_10340) {
            return 1;
        }
        return ChunkFinderBetterModule.isBlob(var1) ? (byte)2 : (byte)(var0.method_26215() ? 3 : 0);
    }

    private static boolean isBlob(class_2248 var0) {
        return var0 == class_2246.field_10508 || var0 == class_2246.field_10115 || var0 == class_2246.field_10474 || var0 == class_2246.field_10255;
    }

    private static boolean carriesStone(class_2248 var0) {
        return var0 == class_2246.field_10340 || var0 == class_2246.field_28888;
    }

    private static boolean matters(class_2680 var0) {
        return var0 != null && (ChunkFinderBetterModule.kindOf(var0) != 0 || ChunkFinderBetterModule.carriesStone(var0.method_26204()));
    }

    private void probe(long var1) {
        if (this.probing && this.probed.add(var1) && this.probed.size() >= 48) {
            this.probing = false;
            this.probed.clear();
            if (class_310.method_1551().field_1687 != null && class_310.method_1551().field_1724 != null) {
                class_310.method_1551().field_1724.method_7353((class_2561)class_2561.method_43470((String)"Hook: This server hides ores with anti-xray; patched shafts may not be visible."), false);
            }
        }
    }

    private void announce(List<ChunkCandidate> var1) {
        if (this.notificationsOn() && !var1.isEmpty()) {
            ChunkCandidate var2 = var1.get(0);
            ChunkFlagToast.show(class_2338.method_10061((long)var2.pos()), class_2338.method_10083((long)var2.pos()), this.sound.get());
        }
    }

    @EventListener
    public void onRender3D(Render3DEvent var1) {
        if (class_310.method_1551().field_1687 != null) {
            int var2 = this.alpha.getInt();
            int var3 = this.chunkColor.argb() & 0xFFFFFF;
            this.plates.draw(var1, (LongSet)this.shown, new Color(var2 << 24 | var3, true), new Color(Math.min(220, var2 + 45) << 24 | var3, true), 1.5f);
        }
    }

    static {
        int[] nArray = new int[4];
        int[] var5 = nArray;
        nArray[0] = 1;
        var5[1] = -1;
        var5[2] = 0;
        var5[3] = 0;
        SIDE_X = var5;
        int[] nArray2 = new int[4];
        int[] var7 = nArray2;
        nArray2[0] = 0;
        var7[1] = 0;
        var7[2] = 1;
        var7[3] = -1;
        SIDE_Z = var7;
        int[] nArray3 = new int[4];
        int[] var9 = nArray3;
        nArray3[0] = 1;
        var9[1] = 1;
        var9[2] = -1;
        var9[3] = -1;
        CORNER_X = var9;
        int[] nArray4 = new int[4];
        int[] var11 = nArray4;
        nArray4[0] = 1;
        var11[1] = -1;
        var11[2] = 1;
        var11[3] = -1;
        CORNER_Z = var11;
        long[] var45 = new long[]{};
        UNDECIDED = new ChunkCandidate(0L, 0, var45);
    }

    @Environment(value=EnvType.CLIENT)
    private record HeightBand(int minY, int maxY, int stackHeight) {
    }

    @Environment(value=EnvType.CLIENT)
    private record ChunkCandidate(long pos, int height, long[] stacks) {
        boolean sameAs(ChunkCandidate var1) {
            return this.pos == var1.pos && this.height == var1.height && Arrays.equals(this.stacks, var1.stacks);
        }
    }
}

