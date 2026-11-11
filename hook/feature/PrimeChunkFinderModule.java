/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.longs.Long2LongMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongListIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_156
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1923
 *  net.minecraft.class_1935
 *  net.minecraft.class_1944
 *  net.minecraft.class_2231
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2269
 *  net.minecraft.class_2309
 *  net.minecraft.class_2312
 *  net.minecraft.class_2315
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_2377
 *  net.minecraft.class_2401
 *  net.minecraft.class_2426
 *  net.minecraft.class_2428
 *  net.minecraft.class_2436
 *  net.minecraft.class_2453
 *  net.minecraft.class_2457
 *  net.minecraft.class_2459
 *  net.minecraft.class_2537
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2665
 *  net.minecraft.class_2667
 *  net.minecraft.class_2671
 *  net.minecraft.class_2672
 *  net.minecraft.class_2676
 *  net.minecraft.class_2678
 *  net.minecraft.class_2680
 *  net.minecraft.class_2804
 *  net.minecraft.class_2806
 *  net.minecraft.class_2818
 *  net.minecraft.class_2826
 *  net.minecraft.class_310
 *  net.minecraft.class_3562
 *  net.minecraft.class_368
 *  net.minecraft.class_374
 *  net.minecraft.class_4076
 *  net.minecraft.class_4850
 *  net.minecraft.class_5803
 *  net.minecraft.class_631
 *  net.minecraft.class_638
 *  net.minecraft.class_6606
 *  net.minecraft.class_8886
 *  net.minecraft.class_9848
 */
package hook.feature;

import hook.feature.StackedNotificationToast;
import hook.module.ext.ClientModule;
import hook.module.ext.EventHandler;
import hook.module.ext.ModuleCategory;
import hook.module.ext.Stage;
import hook.module.ext.event.BlockUpdateEvent;
import hook.module.ext.event.PacketReceiveNettyEvent;
import hook.module.ext.event.Render3DEvent;
import hook.module.ext.event.TickEvent;
import hook.module.ext.setting.BooleanSetting;
import hook.module.ext.setting.ColorSetting;
import hook.utils.ext.ChunkAreas;
import hook.utils.ext.ChunkOverlay;
import hook.utils.ext.LevelUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_1935;
import net.minecraft.class_1944;
import net.minecraft.class_2231;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2269;
import net.minecraft.class_2309;
import net.minecraft.class_2312;
import net.minecraft.class_2315;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2377;
import net.minecraft.class_2401;
import net.minecraft.class_2426;
import net.minecraft.class_2428;
import net.minecraft.class_2436;
import net.minecraft.class_2453;
import net.minecraft.class_2457;
import net.minecraft.class_2459;
import net.minecraft.class_2537;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2665;
import net.minecraft.class_2667;
import net.minecraft.class_2671;
import net.minecraft.class_2672;
import net.minecraft.class_2676;
import net.minecraft.class_2678;
import net.minecraft.class_2680;
import net.minecraft.class_2804;
import net.minecraft.class_2806;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_3562;
import net.minecraft.class_368;
import net.minecraft.class_374;
import net.minecraft.class_4076;
import net.minecraft.class_4850;
import net.minecraft.class_5803;
import net.minecraft.class_631;
import net.minecraft.class_638;
import net.minecraft.class_6606;
import net.minecraft.class_8886;
import net.minecraft.class_9848;

@Environment(value=EnvType.CLIENT)
public final class PrimeChunkFinderModule
extends ClientModule {
    private static final int SECTION_BYTES = 2048;
    private static final int MAX_FLAGGED_CHUNKS = 256;
    private static final int AMETHYST_MAX_LIGHT = 5;
    private static final int AMETHYST_REACH = 5;
    private static final long REDSTONE_NOTIFY_COOLDOWN_MS = 15000L;
    private static final int ORE_LIGHT = 9;
    private static final long[] NO_ORES = new long[0];
    private static final int LAMP_LIGHT = 14;
    private static final int[] NO_LAMPS = new int[0];
    private static final long GONE_LAMP_MS = 5000L;
    private static final int MAX_TRACKED_CHUNKS = 5000;
    private static final int SNAPSHOT_RADIUS = 32;
    private static final byte[] ZERO_SECTION = new byte[2048];
    private static final double MAX_Y = 0.0;
    private final ColorSetting chunkColor;
    private final BooleanSetting tracers;
    private final BooleanSetting sound;
    private final LinkedHashMap<Long, Map<Integer, byte[]>> chunkLight;
    private final LongOpenHashSet passedChunks;
    private final LongLinkedOpenHashSet flaggedChunks;
    private final LongOpenHashSet shownChunks;
    private final ChunkAreas chunkAreas;
    private final LongOpenHashSet checkedSections;
    private final LongOpenHashSet amethystSections;
    private final Long2ObjectOpenHashMap<long[]> oreSections;
    private final Long2ObjectOpenHashMap<int[]> lampSections;
    private final Long2LongOpenHashMap goneLamps;
    private final class_2338.class_2339 cursor;
    private final Long2LongOpenHashMap redstoneNotified;

    public PrimeChunkFinderModule() {
        super("prime-chunk-finder", "Prime Chunk Finder", "Flags chunks with light and redstone changes below Y 0.", ModuleCategory.BASE_RAID);
        Long2LongOpenHashMap var51;
        class_2338.class_2339 var50;
        Long2LongOpenHashMap var49;
        Long2ObjectOpenHashMap var48;
        Long2ObjectOpenHashMap var47;
        LongOpenHashSet var46;
        LongOpenHashSet var45;
        ChunkAreas var44;
        LongOpenHashSet var43;
        LongLinkedOpenHashSet var42;
        LongOpenHashSet var41;
        BooleanSetting var39;
        BooleanSetting var36;
        ColorSetting var33;
        String var56 = "chunk-color";
        String var6 = "Chunk Color";
        ColorSetting var60 = new ColorSetting(var6, 1342242650);
        this.chunkColor = var33 = this.addSetting(var56, var60);
        String var57 = "tracers";
        String var65 = "Tracers";
        BooleanSetting var61 = new BooleanSetting(var65, true);
        this.tracers = var36 = this.addSetting(var57, var61);
        String var58 = "sound";
        String var66 = "Sound";
        BooleanSetting var62 = new BooleanSetting(var66, true);
        this.sound = var39 = this.addSetting(var58, var62);
        LinkedHashMap var40 = new LinkedHashMap(256, 0.75f, true);
        this.chunkLight = var40;
        this.passedChunks = var41 = new LongOpenHashSet();
        this.flaggedChunks = var42 = new LongLinkedOpenHashSet();
        this.shownChunks = var43 = new LongOpenHashSet();
        this.chunkAreas = var44 = new ChunkAreas();
        this.checkedSections = var45 = new LongOpenHashSet();
        this.amethystSections = var46 = new LongOpenHashSet();
        this.oreSections = var47 = new Long2ObjectOpenHashMap();
        this.lampSections = var48 = new Long2ObjectOpenHashMap();
        this.goneLamps = var49 = new Long2LongOpenHashMap();
        this.cursor = var50 = new class_2338.class_2339();
        this.redstoneNotified = var51 = new Long2LongOpenHashMap();
        BooleanSetting var25 = this.usesNotifications();
        BooleanSetting var27 = this.sound;
        BooleanSupplier var53 = var25::get;
        var27.visibleWhen(var53);
        this.redstoneNotified.defaultReturnValue(-4611686018427387904L);
    }

    @Override
    public boolean persistEnabled() {
        return false;
    }

    @Override
    public void onEnable() {
        class_310 var1 = class_310.method_1551();
        if (var1.field_1724 != null && var1.field_1687 != null && !(var1.field_1724.method_23318() >= 0.0)) {
            this.reset();
            this.snapshotLoadedChunks();
            this.on(PacketReceiveNettyEvent.TYPE, this::onPacket);
        } else {
            PrimeChunkFinderModule.warnDeepslateOnly(var1);
            this.requestDisable();
        }
    }

    @Override
    public void onDisable() {
        this.reset();
    }

    private static void warnDeepslateOnly(class_310 var0) {
        class_374 var1 = var0.method_1566();
        if (var1.method_1997(PrimeChunkToast.class, class_368.field_2208) == null) {
            var1.method_1999((class_368)new PrimeChunkToast());
        }
    }

    private void onPacket(PacketReceiveNettyEvent var1) {
        class_310 var2 = class_310.method_1551();
        if (var1.packet instanceof class_2678) {
            var2.execute(() -> {
                if (this.isEnabled()) {
                    this.reset();
                }
            });
        } else {
            class_2596<?> var10 = var1.packet;
            if (var1.packet instanceof class_2672) {
                class_2672 var3 = (class_2672)var10;
                int var8 = var3.method_11523();
                int var6 = var3.method_11524();
                class_6606 var7 = var3.method_38599();
                var2.execute(() -> {
                    if (this.isEnabled() && var2.field_1687 != null) {
                        this.baselineChunk(var8, var6, var7);
                    }
                });
            } else {
                var10 = var1.packet;
                if (var1.packet instanceof class_2676) {
                    class_2676 var4 = (class_2676)var10;
                    int var10x = var4.method_11558();
                    int var11 = var4.method_11554();
                    class_6606 var12 = var4.method_38600();
                    var2.execute(() -> {
                        if (this.isEnabled() && var2.field_1687 != null) {
                            this.detectChanges(var10x, var11, var12);
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent var1) {
        if (var1.oldState != var1.newState && class_310.method_1551().field_1687 != null) {
            this.rememberGoneLamp(var1.pos, var1.oldState, var1.newState);
            this.detectRedstone(var1.pos, var1.oldState, var1.newState);
        }
    }

    @EventHandler(stage=Stage.POST)
    public void onTick(TickEvent var1) {
        class_310 var2 = class_310.method_1551();
        if (var2.field_1687 == null) {
            this.reset();
        } else {
            if (var2.field_1724 != null && var2.field_1724.method_23318() < 0.0) {
                this.passedChunks.add(var2.field_1724.method_31476().method_8324());
            }
            this.forgetOldLamps();
            this.refreshShown(var2.field_1687);
        }
    }

    private void refreshShown(class_638 var1) {
        this.shownChunks.clear();
        LongListIterator var2 = this.flaggedChunks.iterator();
        while (var2.hasNext()) {
            long var3 = var2.next();
            if (!LevelUtil.isLoaded(var1, class_1923.method_8325((long)var3), class_1923.method_8332((long)var3))) continue;
            this.shownChunks.add(var3);
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent var1) {
        if (!this.shownChunks.isEmpty() && class_310.method_1551().field_1687 != null) {
            int var2 = this.chunkColor.argb();
            ChunkOverlay.draw((LongSet)this.shownChunks, var2, var1.partialTick);
            if (this.tracers.get()) {
                this.chunkAreas.drawTracers((LongSet)this.shownChunks, 63.0, class_9848.method_61334((int)var2));
            }
        }
    }

    private void flagChunk(long var1) {
        this.flaggedChunks.addAndMoveToLast(var1);
        this.shownChunks.add(var1);
        while (this.flaggedChunks.size() > 256) {
            this.flaggedChunks.removeFirstLong();
        }
    }

    private void snapshotLoadedChunks() {
        class_310 var1 = class_310.method_1551();
        if (var1.field_1687 != null && var1.field_1724 != null) {
            int var2 = var1.field_1724.method_31477() >> 4;
            int var3 = var1.field_1724.method_31479() >> 4;
            for (int var4 = var2 - 32; var4 <= var2 + 32; ++var4) {
                for (int var5 = var3 - 32; var5 <= var3 + 32; ++var5) {
                    Map<Integer, byte[]> var6 = this.snapshotChunk(var4, var5);
                    if (var6 == null) continue;
                    this.chunkLight.put(class_1923.method_8331((int)var4, (int)var5), var6);
                }
            }
            this.trimChunkCache();
        }
    }

    private Map<Integer, byte[]> snapshotChunk(int var1, int var2) {
        class_638 var3 = class_310.method_1551().field_1687;
        if (var3 == null) {
            return null;
        }
        class_631 var4 = var3.method_2935();
        if (!var4.method_12123(var1, var2)) {
            return null;
        }
        class_3562 var5 = var4.method_12130().method_15562(class_1944.field_9282);
        Map<Integer, byte[]> var6 = this.createZeroedChunk();
        for (int var7 = this.bottomSection(); var7 <= this.topSection(); ++var7) {
            class_2804 var8 = var5.method_15544(class_4076.method_18676((int)var1, (int)var7, (int)var2));
            if (var8 == null || var8.method_51379(0)) continue;
            var6.put(var7, Arrays.copyOf(var8.method_12137(), 2048));
        }
        return var6;
    }

    private void baselineChunk(int var1, int var2, class_6606 var3) {
        Map<Integer, byte[]> var4 = this.createZeroedChunk();
        this.applyInitializedSections(var3, var4, var1, var2, false, null);
        this.applyUninitializedSections(var3, var4, var1, var2, false, null);
        this.chunkLight.put(class_1923.method_8331((int)var1, (int)var2), var4);
        this.trimChunkCache();
    }

    private void detectChanges(int var1, int var2, class_6606 var3) {
        boolean var7;
        long var4 = class_1923.method_8331((int)var1, (int)var2);
        this.checkedSections.clear();
        this.amethystSections.clear();
        this.oreSections.clear();
        this.lampSections.clear();
        Map<Integer, byte[]> var6 = this.chunkLight.get(var4);
        if (var6 == null) {
            var6 = this.snapshotChunk(var1, var2);
        }
        boolean bl = var7 = var6 != null && !this.isOwnChunk(var4);
        if (var6 == null) {
            var6 = this.createZeroedChunk();
        }
        BlockChange var8 = new BlockChange();
        this.applyInitializedSections(var3, var6, var1, var2, var7, var8);
        this.applyUninitializedSections(var3, var6, var1, var2, var7, var8);
        this.chunkLight.put(var4, var6);
        this.trimChunkCache();
        if (var8.count != 0) {
            this.flagChunk(var4);
            if (this.notificationsOn()) {
                StackedNotificationToast.addOrUpdate(new class_1799((class_1935)class_1802.field_8733), this.name(), (class_2561)class_2561.method_43470((String)(var8.x + ", " + var8.y + ", " + var8.z)), this.sound.get());
            }
        }
    }

    private void rememberGoneLamp(class_2338 var1, class_2680 var2, class_2680 var3) {
        if (PrimeChunkFinderModule.isLamp(var2)) {
            int var4 = var2.method_26213();
            if (!PrimeChunkFinderModule.isLamp(var3) || var3.method_26213() < var4) {
                this.goneLamps.put(var1.method_10063(), class_156.method_658() + 5000L << 4 | (long)var4);
            }
        }
    }

    private void forgetOldLamps() {
        if (!this.goneLamps.isEmpty()) {
            long var1 = class_156.method_658();
            LongIterator var3 = this.goneLamps.values().iterator();
            while (var3.hasNext()) {
                if (var3.nextLong() >>> 4 >= var1) continue;
                var3.remove();
            }
        }
    }

    private void detectRedstone(class_2338 var1, class_2680 var2, class_2680 var3) {
        long var4;
        if (!((double)var1.method_10264() >= 0.0) && (PrimeChunkFinderModule.isRedstone(var3) || PrimeChunkFinderModule.isRedstone(var2)) && !this.isOwnChunk(var4 = class_1923.method_8331((int)(var1.method_10263() >> 4), (int)(var1.method_10260() >> 4)))) {
            this.flagChunk(var4);
            long var6 = class_156.method_658();
            if (this.notificationsOn() && var6 - this.redstoneNotified.get(var4) >= 15000L) {
                this.redstoneNotified.put(var4, var6);
                StackedNotificationToast.addOrUpdate(new class_1799((class_1935)class_1802.field_8725), this.name(), (class_2561)class_2561.method_43470((String)("Redstone " + var1.method_10263() + ", " + var1.method_10264() + ", " + var1.method_10260())), this.sound.get());
            }
        }
    }

    private static boolean isRedstone(class_2680 var0) {
        class_2248 var1 = var0.method_26204();
        return var1 instanceof class_2457 || var1 instanceof class_2312 || var1 instanceof class_2459 || var1 instanceof class_2453 || var1 instanceof class_2426 || var1 instanceof class_2665 || var1 instanceof class_2671 || var1 instanceof class_2667 || var1 instanceof class_2315 || var1 instanceof class_2377 || var1 instanceof class_2428 || var1 instanceof class_2309 || var1 instanceof class_2401 || var1 instanceof class_2269 || var1 instanceof class_2231 || var1 instanceof class_2537 || var1 instanceof class_4850 || var1 instanceof class_8886 || var1 instanceof class_2436;
    }

    private boolean isOwnChunk(long var1) {
        if (this.passedChunks.contains(var1)) {
            return true;
        }
        class_310 var3 = class_310.method_1551();
        return var3.field_1724 != null && var3.field_1724.method_31476().method_8324() == var1;
    }

    private void applyInitializedSections(class_6606 var1, Map<Integer, byte[]> var2, int var3, int var4, boolean var5, BlockChange var6) {
        BitSet var7 = var1.method_38608();
        List var8 = var1.method_38610();
        int var9 = 0;
        int var10 = var7.nextSetBit(0);
        while (var10 >= 0 && var9 < var8.size()) {
            byte[] var11 = (byte[])var8.get(var9++);
            int var12 = this.lightSectionY(var10);
            if (this.isWorldSection(var12) && var11.length == 2048) {
                this.updateSection(var2, var3, var4, var12, Arrays.copyOf(var11, 2048), var5, var6);
            }
            var10 = var7.nextSetBit(var10 + 1);
        }
    }

    private void applyUninitializedSections(class_6606 var1, Map<Integer, byte[]> var2, int var3, int var4, boolean var5, BlockChange var6) {
        BitSet var7 = var1.method_38609();
        int var8 = var7.nextSetBit(0);
        while (var8 >= 0) {
            int var9 = this.lightSectionY(var8);
            if (this.isWorldSection(var9)) {
                this.updateSection(var2, var3, var4, var9, ZERO_SECTION, var5, var6);
            }
            var8 = var7.nextSetBit(var8 + 1);
        }
    }

    private void updateSection(Map<Integer, byte[]> var1, int var2, int var3, int var4, byte[] var5, boolean var6, BlockChange var7) {
        byte[] var8 = var1.get(var4);
        if (var6 && var8 != null && var7 != null) {
            this.compareSection(var8, var5, var2, var3, var4, var7);
        }
        var1.put(var4, var5);
    }

    private void compareSection(byte[] var1, byte[] var2, int var3, int var4, int var5, BlockChange var6) {
        for (int var7 = 0; var7 < 2048; ++var7) {
            int var8 = var1[var7] & 0xFF;
            int var9 = var2[var7] & 0xFF;
            int var10 = var8 ^ var9;
            if ((var10 & 0xF) != 0) {
                this.recordChange(var3, var4, var5, var7 << 1, var8 & 0xF, var9 & 0xF, var6);
            }
            if ((var10 & 0xF0) == 0) continue;
            this.recordChange(var3, var4, var5, (var7 << 1) + 1, var8 >>> 4 & 0xF, var9 >>> 4 & 0xF, var6);
        }
    }

    private void recordChange(int var1, int var2, int var3, int var4, int var5, int var6, BlockChange var7) {
        int var8 = (var1 << 4) + (var4 & 0xF);
        int var9 = (var3 << 4) + (var4 >>> 8 & 0xF);
        int var10 = (var2 << 4) + (var4 >>> 4 & 0xF);
        int var11 = Math.max(var5, var6);
        if (!(this.fromAmethyst(var8, var9, var10, var5, var6) || this.fromRedstoneOre(var8, var9, var10, var11) || this.fromLamp(var8, var9, var10, var11))) {
            var7.add(var8, var9, var10, var5, var6);
        }
    }

    private boolean fromLamp(int var1, int var2, int var3, int var4) {
        if (var4 > 14) {
            return false;
        }
        class_638 var5 = class_310.method_1551().field_1687;
        if (var5 == null) {
            return false;
        }
        int var6 = 14 - var4;
        for (int var7 = var1 - var6 >> 4; var7 <= var1 + var6 >> 4; ++var7) {
            for (int var8 = var2 - var6 >> 4; var8 <= var2 + var6 >> 4; ++var8) {
                for (int var9 = var3 - var6 >> 4; var9 <= var3 + var6 >> 4; ++var9) {
                    for (int var13 : this.lamps(var5, var7, var8, var9)) {
                        int var14 = var13 >>> 4;
                        int var15 = (var7 << 4) + (var14 & 0xF);
                        int var16 = (var8 << 4) + (var14 >>> 8);
                        int var17 = (var9 << 4) + (var14 >>> 4 & 0xF);
                        if (Math.abs(var15 - var1) + Math.abs(var16 - var2) + Math.abs(var17 - var3) > (var13 & 0xF) - var4) continue;
                        return true;
                    }
                }
            }
        }
        long var18 = class_156.method_658();
        for (Long2LongMap.Entry var20 : this.goneLamps.long2LongEntrySet()) {
            long var22;
            int var23;
            long var21 = var20.getLongValue();
            if (var21 >>> 4 < var18 || (var23 = Math.abs(class_2338.method_10061((long)(var22 = var20.getLongKey())) - var1) + Math.abs(class_2338.method_10071((long)var22) - var2) + Math.abs(class_2338.method_10083((long)var22) - var3)) > (int)(var21 & 0xFL) - var4) continue;
            return true;
        }
        return false;
    }

    private int[] lamps(class_638 var1, int var2, int var3, int var4) {
        long var5 = class_4076.method_18685((int)var2, (int)var3, (int)var4);
        int[] var7 = (int[])this.lampSections.get(var5);
        if (var7 != null) {
            return var7;
        }
        int[] var8 = PrimeChunkFinderModule.scanLamps(var1, var2, var3, var4);
        this.lampSections.put(var5, (Object)var8);
        return var8;
    }

    private static int[] scanLamps(class_638 var0, int var1, int var2, int var3) {
        if (var2 >= var0.method_32891() && var2 <= var0.method_31597()) {
            class_2818 var4 = var0.method_2935().method_2857(var1, var3, class_2806.field_12803, false);
            if (var4 == null) {
                return NO_LAMPS;
            }
            class_2826 var5 = var4.method_38259(var4.method_31603(var2));
            if (!var5.method_38292() && var5.method_12265().method_19526(PrimeChunkFinderModule::isLamp)) {
                IntArrayList var6 = new IntArrayList();
                for (int var7 = 0; var7 < 4096; ++var7) {
                    class_2680 var8 = var5.method_12254(var7 & 0xF, var7 >>> 8, var7 >>> 4 & 0xF);
                    if (!PrimeChunkFinderModule.isLamp(var8)) continue;
                    var6.add(var7 << 4 | var8.method_26213());
                }
                return var6.isEmpty() ? NO_LAMPS : var6.toIntArray();
            }
            return NO_LAMPS;
        }
        return NO_LAMPS;
    }

    private static boolean isLamp(class_2680 var0) {
        return var0.method_27852(class_2246.field_10336) || var0.method_27852(class_2246.field_10099) || var0.method_27852(class_2246.field_22092) || var0.method_27852(class_2246.field_22093) || var0.method_27852(class_2246.field_61902) || var0.method_27852(class_2246.field_61903) || class_5803.method_33618((class_2680)var0);
    }

    private boolean fromAmethyst(int var1, int var2, int var3, int var4, int var5) {
        if (Math.max(var4, var5) > 5) {
            return false;
        }
        class_638 var6 = class_310.method_1551().field_1687;
        if (var6 != null && this.amethystSectionNear(var6, var1, var2, var3)) {
            for (int var7 = -5; var7 <= 5; ++var7) {
                int var8 = 5 - Math.abs(var7);
                for (int var9 = -var8; var9 <= var8; ++var9) {
                    int var10 = var8 - Math.abs(var9);
                    for (int var11 = -var10; var11 <= var10; ++var11) {
                        if (!PrimeChunkFinderModule.isAmethyst(var6.method_8320((class_2338)this.cursor.method_10103(var1 + var7, var2 + var9, var3 + var11)))) continue;
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private boolean amethystSectionNear(class_638 var1, int var2, int var3, int var4) {
        for (int var5 = var2 - 5 >> 4; var5 <= var2 + 5 >> 4; ++var5) {
            for (int var6 = var3 - 5 >> 4; var6 <= var3 + 5 >> 4; ++var6) {
                for (int var7 = var4 - 5 >> 4; var7 <= var4 + 5 >> 4; ++var7) {
                    long var8 = class_4076.method_18685((int)var5, (int)var6, (int)var7);
                    if (this.checkedSections.add(var8) && PrimeChunkFinderModule.sectionHasAmethyst(var1, var5, var6, var7)) {
                        this.amethystSections.add(var8);
                    }
                    if (!this.amethystSections.contains(var8)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean sectionHasAmethyst(class_638 var0, int var1, int var2, int var3) {
        if (var2 >= var0.method_32891() && var2 <= var0.method_31597()) {
            class_2818 var4 = var0.method_2935().method_2857(var1, var3, class_2806.field_12803, false);
            if (var4 == null) {
                return false;
            }
            class_2826 var5 = var4.method_38259(var4.method_31603(var2));
            return !var5.method_38292() && var5.method_12265().method_19526(PrimeChunkFinderModule::isAmethyst);
        }
        return false;
    }

    private boolean fromRedstoneOre(int var1, int var2, int var3, int var4) {
        if (var4 > 9) {
            return false;
        }
        class_638 var5 = class_310.method_1551().field_1687;
        if (var5 == null) {
            return false;
        }
        int var6 = 9 - var4;
        for (int var7 = var1 - var6 >> 4; var7 <= var1 + var6 >> 4; ++var7) {
            for (int var8 = var2 - var6 >> 4; var8 <= var2 + var6 >> 4; ++var8) {
                for (int var9 = var3 - var6 >> 4; var9 <= var3 + var6 >> 4; ++var9) {
                    for (long var13 : this.exposedOres(var5, var7, var8, var9)) {
                        int var15 = Math.abs(class_2338.method_10061((long)var13) - var1) + Math.abs(class_2338.method_10071((long)var13) - var2) + Math.abs(class_2338.method_10083((long)var13) - var3);
                        if (var15 > var6) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private long[] exposedOres(class_638 var1, int var2, int var3, int var4) {
        long var5 = class_4076.method_18685((int)var2, (int)var3, (int)var4);
        long[] var7 = (long[])this.oreSections.get(var5);
        if (var7 != null) {
            return var7;
        }
        long[] var8 = this.scanExposedOres(var1, var2, var3, var4);
        this.oreSections.put(var5, (Object)var8);
        return var8;
    }

    private long[] scanExposedOres(class_638 var1, int var2, int var3, int var4) {
        if (var3 >= var1.method_32891() && var3 <= var1.method_31597()) {
            class_2818 var5 = var1.method_2935().method_2857(var2, var4, class_2806.field_12803, false);
            if (var5 == null) {
                return NO_ORES;
            }
            class_2826 var6 = var5.method_38259(var5.method_31603(var3));
            if (!var6.method_38292() && var6.method_12265().method_19526(PrimeChunkFinderModule::isRedstoneOre)) {
                LongArrayList var7 = new LongArrayList();
                for (int var8 = 0; var8 < 16; ++var8) {
                    for (int var9 = 0; var9 < 16; ++var9) {
                        for (int var10 = 0; var10 < 16; ++var10) {
                            int var13;
                            int var12;
                            int var11;
                            if (!PrimeChunkFinderModule.isRedstoneOre(var6.method_12254(var10, var8, var9)) || !this.exposed(var1, var11 = (var2 << 4) + var10, var12 = (var3 << 4) + var8, var13 = (var4 << 4) + var9)) continue;
                            var7.add(class_2338.method_10064((int)var11, (int)var12, (int)var13));
                        }
                    }
                }
                return var7.isEmpty() ? NO_ORES : var7.toLongArray();
            }
            return NO_ORES;
        }
        return NO_ORES;
    }

    private boolean exposed(class_638 var1, int var2, int var3, int var4) {
        for (class_2350 var8 : class_2350.values()) {
            class_2680 var9 = var1.method_8320((class_2338)this.cursor.method_10103(var2 + var8.method_10148(), var3 + var8.method_10164(), var4 + var8.method_10165()));
            if (!var9.method_26215() && var9.method_26225()) continue;
            return true;
        }
        return false;
    }

    private static boolean isRedstoneOre(class_2680 var0) {
        return var0.method_27852(class_2246.field_10080) || var0.method_27852(class_2246.field_29030);
    }

    private static boolean isAmethyst(class_2680 var0) {
        return var0.method_27852(class_2246.field_27161) || var0.method_27852(class_2246.field_27162) || var0.method_27852(class_2246.field_27163) || var0.method_27852(class_2246.field_27164) || var0.method_27852(class_2246.field_27160) || var0.method_27852(class_2246.field_27159);
    }

    private Map<Integer, byte[]> createZeroedChunk() {
        HashMap<Integer, byte[]> var1 = new HashMap<Integer, byte[]>();
        for (int var2 = this.bottomSection(); var2 <= this.topSection(); ++var2) {
            var1.put(var2, ZERO_SECTION);
        }
        return var1;
    }

    private int lightSectionY(int var1) {
        class_638 var2 = class_310.method_1551().field_1687;
        return var2 == null ? Integer.MIN_VALUE : var2.method_2935().method_12130().method_31929() + var1;
    }

    private int bottomSection() {
        return class_310.method_1551().field_1687.method_32891();
    }

    private int topSection() {
        return class_310.method_1551().field_1687.method_31597();
    }

    private boolean isWorldSection(int var1) {
        return var1 >= this.bottomSection() && var1 <= this.topSection();
    }

    private void trimChunkCache() {
        while (this.chunkLight.size() > 5000) {
            Iterator<Long> var1 = this.chunkLight.keySet().iterator();
            var1.next();
            var1.remove();
        }
    }

    private void reset() {
        this.chunkLight.clear();
        this.passedChunks.clear();
        this.flaggedChunks.clear();
        this.shownChunks.clear();
        this.redstoneNotified.clear();
        this.lampSections.clear();
        this.goneLamps.clear();
    }

    @Environment(value=EnvType.CLIENT)
    public static final class PrimeChunkToast
    extends StackedNotificationToast {
        private static final class_2561 TITLE = class_2561.method_43470((String)"Deepslate Level Only");
        private static final class_2561 MESSAGE = class_2561.method_43470((String)"Go below Y 0 first.");

        PrimeChunkToast() {
            super(new class_1799((class_1935)class_1802.field_28866), TITLE, MESSAGE, true);
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class BlockChange {
        int count;
        int x;
        int y;
        int z;
        int oldValue;
        int newValue;

        private BlockChange() {
        }

        void add(int x, int y, int z, int oldValue, int newValue) {
            if (this.count == 0 || Math.abs(newValue - oldValue) > Math.abs(this.newValue - this.oldValue)) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.oldValue = oldValue;
                this.newValue = newValue;
            }
            ++this.count;
        }
    }
}

