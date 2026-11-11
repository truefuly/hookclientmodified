/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.minecraft.class_1923
 *  net.minecraft.class_1944
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_2382
 *  net.minecraft.class_2680
 *  net.minecraft.class_2769
 *  net.minecraft.class_2818
 *  net.minecraft.class_2826
 *  net.minecraft.class_4184
 *  net.minecraft.class_4481
 *  net.minecraft.class_4587
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.ChunkDataEvent;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.Setting;
import hook.utils.BlockUtil;
import hook.utils.EncryptedString;
import hook.utils.RenderUtils;
import hook.utils.RenderVisibility;
import hook.utils.chunks.ChunkPlateRenderer;
import hook.utils.chunks.ChunkRegionBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_4184;
import net.minecraft.class_4481;
import net.minecraft.class_4587;

public final class SusChunkFinderModule
extends Module {
    private static final int MIN_SCAN_Y = -64;
    private static final int MAX_SCAN_Y = 64;
    private static final double PLATE_Y = 63.0;
    private static final double PLATE_THICKNESS = 0.05;
    private static final int MAX_CLUSTER_MARKERS = 400;
    private static final long SCAN_BUDGET_NANOS = 4000000L;
    private static final int SCAN_CHUNKS_PER_TICK = 24;
    private static final int MAX_TICKS_BETWEEN_COMMITS = 60;
    private final NumberSetting sensitivity;
    private final NumberSetting simulationDistance;
    private final NumberSetting alpha;
    private final BooleanSetting markGeodeChunks;
    private final BooleanSetting renderClusters;
    private final Map<Long, ChunkCluster> geodes;
    private final Queue<Long> scanQueue;
    private final Set<Long> queued;
    private final Set<Long> scanned;
    private final List<RenderUtils.ColoredBox> renderBoxes;
    private final List<RenderUtils.ColoredLine> renderLines;
    private Set<Long> cachedCandidates;
    private final LongOpenHashSet candidateKeys;
    private final ChunkPlateRenderer candidatePlates;
    private static final class_2350[] DIRECTIONS = class_2350.values();
    private boolean candidatesDirty;
    private int cachedSimulationRadius;
    private int cachedSensitivity;
    private Object trackedWorld;
    private int ticks;
    private int ticksSinceCommit;
    private static final int[][] NEIGHBORS;

    public SusChunkFinderModule() {
        super(EncryptedString.of("Sus Chunk Finder"), EncryptedString.of("Finds suspicious chunks from amethyst and full-beehive generation signals"), -1, Category.DONUT);
        ChunkPlateRenderer var46;
        LongOpenHashSet var45;
        BooleanSetting var37;
        BooleanSetting var36;
        NumberSetting var35;
        NumberSetting var34;
        NumberSetting var32;
        int var10000 = 208259676;
        EncryptedString var58 = EncryptedString.of("Sensitivity");
        NumberSetting var31 = new NumberSetting(var58, 1.0, 20.0, 12.0, 1.0);
        EncryptedString var54 = EncryptedString.of("Geode light-signature threshold (maximum 20)");
        this.sensitivity = var32 = var31.getValue(var54);
        EncryptedString var60 = EncryptedString.of("Simulation Distance");
        NumberSetting var33 = new NumberSetting(var60, 2.0, 16.0, 4.0, 1.0);
        EncryptedString var56 = EncryptedString.of("Chunk radius used to triangulate the possible player origin");
        this.simulationDistance = var34 = var33.getValue(var56);
        EncryptedString var62 = EncryptedString.of("Alpha");
        this.alpha = var35 = new NumberSetting(var62, 0.0, 255.0, 140.0, 1.0);
        EncryptedString var64 = EncryptedString.of("Mark Geode Chunks");
        this.markGeodeChunks = var36 = new BooleanSetting(var64, false);
        EncryptedString var66 = EncryptedString.of("Render Clusters");
        this.renderClusters = var37 = new BooleanSetting(var66, false);
        HashMap<Long, ChunkCluster> var38 = new HashMap<Long, ChunkCluster>();
        this.geodes = var38;
        ArrayDeque<Long> var39 = new ArrayDeque<Long>();
        this.scanQueue = var39;
        HashSet<Long> var40 = new HashSet<Long>();
        this.queued = var40;
        HashSet<Long> var41 = new HashSet<Long>();
        this.scanned = var41;
        ArrayList<RenderUtils.ColoredBox> var42 = new ArrayList<RenderUtils.ColoredBox>();
        this.renderBoxes = var42;
        ArrayList<RenderUtils.ColoredLine> var43 = new ArrayList<RenderUtils.ColoredLine>();
        this.renderLines = var43;
        Set var44 = Set.of();
        this.cachedCandidates = var44;
        this.candidateKeys = var45 = new LongOpenHashSet();
        this.candidatePlates = var46 = new ChunkPlateRenderer();
        this.candidatesDirty = true;
        this.cachedSimulationRadius = -1;
        this.cachedSensitivity = -1;
        Setting[] var51 = new Setting[5];
        NumberSetting var81 = this.sensitivity;
        var51[0] = var81;
        NumberSetting var83 = this.simulationDistance;
        var51[1] = var83;
        NumberSetting var85 = this.alpha;
        var51[2] = var85;
        BooleanSetting var87 = this.markGeodeChunks;
        var51[3] = var87;
        BooleanSetting var89 = this.renderClusters;
        var51[4] = var89;
        this.addSettings(var51);
    }

    @Override
    public void onEnable() {
        this.reset();
        this.trackedWorld = this.mc.field_1687;
        this.enqueueLoadedChunks();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    private void reset() {
        this.geodes.clear();
        this.scanQueue.clear();
        this.queued.clear();
        this.scanned.clear();
        this.renderBoxes.clear();
        this.renderLines.clear();
        this.cachedCandidates = Set.of();
        this.candidateKeys.clear();
        this.candidatesDirty = true;
        this.cachedSimulationRadius = -1;
        this.cachedSensitivity = -1;
        this.trackedWorld = null;
        this.ticks = 0;
        this.ticksSinceCommit = 0;
    }

    @EventListener
    public void onChunkData(ChunkDataEvent var1) {
        this.enqueue(var1.packet.method_11523(), var1.packet.method_11524());
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            if (this.trackedWorld != this.mc.field_1687) {
                this.reset();
                this.trackedWorld = this.mc.field_1687;
            }
            ++this.ticks;
            ++this.ticksSinceCommit;
            int var2 = this.sensitivity.getIntValue();
            if (var2 != this.cachedSensitivity) {
                this.cachedSensitivity = var2;
                this.geodes.clear();
                this.scanQueue.clear();
                this.queued.clear();
                this.scanned.clear();
                this.candidatesDirty = true;
                this.enqueueLoadedChunks();
            }
            if (this.ticks % 100 == 1) {
                this.enqueueLoadedChunks();
            }
            if (this.ticks % 240 == 0) {
                this.scanned.clear();
                this.enqueueLoadedChunks();
            }
            this.scanQueuedChunks();
            this.pruneUnloaded();
            this.getCandidateChunks();
        } else {
            this.reset();
        }
    }

    private void scanQueuedChunks() {
        long var1 = System.nanoTime() + 4000000L;
        for (int var3 = 0; var3 < 24 && !this.scanQueue.isEmpty(); ++var3) {
            this.scanNextChunk();
            if (var3 > 0 && System.nanoTime() >= var1) break;
        }
    }

    private void enqueueLoadedChunks() {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            class_1923 var1 = this.mc.field_1724.method_31476();
            List<class_2818> var2 = BlockUtil.getLoadedChunksList();
            var2.sort(Comparator.comparingInt(var1xx -> {
                class_1923 var2x = ((class_2818)var1xx).method_12004();
                return Math.max(Math.abs(var2x.field_9181 - var1.field_9181), Math.abs(var2x.field_9180 - var1.field_9180));
            }));
            Iterator<class_2818> var3 = var2.iterator();
            while (var3.hasNext()) {
                class_1923 var5 = var3.next().method_12004();
                this.enqueue(var5.field_9181, var5.field_9180);
            }
        }
    }

    private void enqueue(int var1, int var2) {
        long var3 = class_1923.method_8331((int)var1, (int)var2);
        if (!this.scanned.contains(var3) && this.queued.add(var3)) {
            this.scanQueue.offer(var3);
        }
    }

    private void scanNextChunk() {
        Long var1 = this.scanQueue.poll();
        if (var1 != null) {
            this.queued.remove(var1);
            this.scanned.add(var1);
            int var2 = class_1923.method_8325((long)var1);
            int var3 = class_1923.method_8332((long)var1);
            if (!this.mc.field_1687.method_8393(var2, var3)) {
                this.removeGeode(var1);
                return;
            }
            class_2818 var4 = this.mc.field_1687.method_8497(var2, var3);
            class_2826[] var5 = var4.method_12006();
            int var6 = var4.method_32891();
            int var7 = var4.method_12004().method_8326();
            int var8 = var4.method_12004().method_8328();
            LongOpenHashSet var9 = new LongOpenHashSet();
            class_2338.class_2339 var10 = new class_2338.class_2339();
            ArrayList<class_2338> var11 = new ArrayList<class_2338>();
            int var12 = 0;
            for (int var13 = 0; var13 < var5.length; ++var13) {
                class_2826 var28 = var5[var13];
                if (var28 == null || !var28.method_12265().method_19526(var0 -> SusChunkFinderModule.isAmethystSignal(var0.method_26204()) || var0.method_27852(class_2246.field_20422) || var0.method_27852(class_2246.field_20421))) continue;
                int var15 = var6 + var13 << 4;
                for (int var16 = 0; var16 < 16; ++var16) {
                    for (int var17 = 0; var17 < 16; ++var17) {
                        for (int var18 = 0; var18 < 16; ++var18) {
                            class_2680 var19 = var28.method_12254(var18, var16, var17);
                            if (SusChunkFinderModule.isAmethystSignal(var19.method_26204())) {
                                class_2338 var21 = new class_2338(var7 + var18, var15 + var16, var8 + var17);
                                if (var11.size() < 400) {
                                    var11.add(var21);
                                }
                                for (class_2350 var25 : DIRECTIONS) {
                                    class_2338.class_2339 var26 = var10.method_10103(var21.method_10263() + var25.method_10148(), var21.method_10264() + var25.method_10164(), var21.method_10260() + var25.method_10165());
                                    if (!this.mc.field_1687.method_8320((class_2338)var26).method_27852(class_2246.field_10340)) continue;
                                    var9.add(var26.method_10063());
                                }
                                continue;
                            }
                            if (!var19.method_27852(class_2246.field_20422) && !var19.method_27852(class_2246.field_20421) || (Integer)var19.method_11654((class_2769)class_4481.field_20420) < 5) continue;
                            ++var12;
                        }
                    }
                }
            }
            int var27 = Math.max(1, this.sensitivity.getIntValue());
            int var27x = Math.max(var9.size(), var12 >= 2 ? var27 : 0);
            if (var27x >= var27) {
                this.setGeode(var1, new ChunkCluster(new class_1923(var2, var3), var27x, List.copyOf(var11)));
                return;
            }
            this.removeGeode(var1);
        }
    }

    private void setGeode(long var1, ChunkCluster var3) {
        ChunkCluster var4 = this.geodes.put(var1, var3);
        if (!var3.equals(var4)) {
            this.candidatesDirty = true;
        }
    }

    private void removeGeode(long var1) {
        if (this.geodes.remove(var1) != null) {
            this.candidatesDirty = true;
        }
    }

    private List<class_2338> findBestLightSphere(List<class_2338> var1, int var2) {
        if (var1.size() < var2) {
            return List.of();
        }
        boolean[] var3 = new boolean[var1.size()];
        List<class_2338> var4 = List.of();
        for (int var5 = 0; var5 < var1.size(); ++var5) {
            if (var3[var5]) continue;
            ArrayList<class_2338> var6 = new ArrayList<class_2338>();
            ArrayDeque<Integer> var7 = new ArrayDeque<Integer>();
            var7.add(var5);
            var3[var5] = true;
            while (!var7.isEmpty()) {
                int var8 = (Integer)var7.remove();
                class_2338 var9 = var1.get(var8);
                var6.add(var9);
                for (int var10 = 0; var10 < var1.size(); ++var10) {
                    if (var3[var10] || !(var9.method_10262((class_2382)var1.get(var10)) <= 121.0)) continue;
                    var3[var10] = true;
                    var7.add(var10);
                }
            }
            if (var6.size() < var2 || var6.size() <= ((Collection)var4).size() || !this.matchesGeodeSphere(var6)) continue;
            var4 = var6;
        }
        return var4;
    }

    private boolean matchesGeodeSphere(List<class_2338> var1) {
        double d;
        int var2 = var1.size();
        double var3 = 0.0;
        double var5 = 0.0;
        double var7 = 0.0;
        for (class_2338 var10 : var1) {
            var3 += (double)var10.method_10263();
            var5 += (double)var10.method_10264();
            var7 += (double)var10.method_10260();
        }
        var3 /= (double)var2;
        var5 /= (double)var2;
        var7 /= (double)var2;
        double[][] var56 = new double[4][4];
        double[] var57 = new double[4];
        for (class_2338 var59 : var1) {
            double var13 = (double)var59.method_10263() - var3;
            double var15 = (double)var59.method_10264() - var5;
            double var17 = (double)var59.method_10260() - var7;
            double var19 = var13 * var13 + var15 * var15 + var17 * var17;
            double[] var21 = new double[]{var13, var15, var17, 1.0};
            for (int var22 = 0; var22 < 4; ++var22) {
                for (int var23 = 0; var23 < 4; ++var23) {
                    var56[var22][var23] = var56[var22][var23] + var21[var22] * var21[var23];
                }
                int n = var22;
                var57[n] = var57[n] + var21[var22] * var19;
            }
        }
        double[] var581 = SusChunkFinderModule.solve4x4(var56, var57);
        if (var581 == null) {
            return false;
        }
        double var59 = var581[0] / 2.0;
        double var14 = var581[1] / 2.0;
        double var16 = var581[2] / 2.0;
        double var18 = var581[3] + var59 * var59 + var14 * var14 + var16 * var16;
        if (d <= 0.0) {
            return false;
        }
        double var20 = Math.sqrt(var18);
        if (!(var20 < 3.0) && !(var20 > 7.0)) {
            double var60 = var3 + var59;
            double var24 = var5 + var14;
            double var26 = var7 + var16;
            double var28 = 0.0;
            double var30 = 0.0;
            double var32 = 0.0;
            double var34 = 0.0;
            HashSet<Integer> var36 = new HashSet<Integer>();
            int var37 = Integer.MAX_VALUE;
            int var38 = Integer.MAX_VALUE;
            int var39 = Integer.MAX_VALUE;
            int var40 = Integer.MIN_VALUE;
            int var41 = Integer.MIN_VALUE;
            int var42 = Integer.MIN_VALUE;
            for (class_2338 var44 : var1) {
                double d2;
                double var45 = (double)var44.method_10263() - var60;
                double var47 = (double)var44.method_10264() - var24;
                double var49 = (double)var44.method_10260() - var26;
                double var51 = Math.sqrt(var45 * var45 + var47 * var47 + var49 * var49);
                if (d2 < 1.0E-4) {
                    var51 = 1.0;
                }
                var28 += Math.abs(var51 - var20);
                var30 += var45 / var51;
                var32 += var47 / var51;
                var34 += var49 / var51;
                var36.add((var45 >= 0.0 ? 1 : 0) | (var47 >= 0.0 ? 2 : 0) | (var49 >= 0.0 ? 4 : 0));
                var37 = Math.min(var37, var44.method_10263());
                var40 = Math.max(var40, var44.method_10263());
                var38 = Math.min(var38, var44.method_10264());
                var41 = Math.max(var41, var44.method_10264());
                var39 = Math.min(var39, var44.method_10260());
                var42 = Math.max(var42, var44.method_10260());
            }
            if (var28 / (double)var2 > 1.35 || var36.size() < 3) {
                return false;
            }
            if (Math.sqrt(var30 * var30 + var32 * var32 + var34 * var34) / (double)var2 > 0.72) {
                return false;
            }
            class_2338 var62 = class_2338.method_49637((double)var60, (double)var24, (double)var26);
            if (this.mc.field_1687.method_8314(class_1944.field_9282, var62) > 3) {
                return false;
            }
            int var46 = var40 - var37 + 1;
            int var63 = var41 - var38 + 1;
            int var48 = var42 - var39 + 1;
            return (var46 >= 3 || var48 >= 3) && var46 + var48 >= 6 && var63 >= 2;
        }
        return false;
    }

    private static double[] solve4x4(double[][] var0, double[] var1) {
        double[][] var2 = new double[4][5];
        for (int var3 = 0; var3 < 4; ++var3) {
            System.arraycopy(var0[var3], 0, var2[var3], 0, 4);
            var2[var3][4] = var1[var3];
        }
        for (int var10 = 0; var10 < 4; ++var10) {
            int var4 = var10;
            for (int var5 = var10 + 1; var5 < 4; ++var5) {
                if (!(Math.abs(var2[var5][var10]) > Math.abs(var2[var4][var10]))) continue;
                var4 = var5;
            }
            double[] var13 = var2[var10];
            var2[var10] = var2[var4];
            var2[var4] = var13;
            if (Math.abs(var2[var10][var10]) < 1.0E-9) {
                return null;
            }
            for (int var6 = var10 + 1; var6 < 4; ++var6) {
                double var7 = var2[var6][var10] / var2[var10][var10];
                for (int var9 = var10; var9 <= 4; ++var9) {
                    var2[var6][var9] = var2[var6][var9] - var7 * var2[var10][var9];
                }
            }
        }
        double[] var11 = new double[4];
        for (int var12 = 3; var12 >= 0; --var12) {
            double var14 = 0.0;
            for (int var15 = var12 + 1; var15 < 4; ++var15) {
                var14 += var2[var12][var15] * var11[var15];
            }
            var11[var12] = (var2[var12][4] - var14) / var2[var12][var12];
        }
        return var11;
    }

    private static long cellKey(int var0, int var1) {
        return class_1923.method_8331((int)(var0 >> 3), (int)(var1 >> 3));
    }

    private static boolean isGeodeSignature(class_2248 var0) {
        return var0 == class_2246.field_27159 || var0 == class_2246.field_27160 || var0 == class_2246.field_27114 || var0 == class_2246.field_29032 || SusChunkFinderModule.isAmethystGrowth(var0);
    }

    private static boolean isAmethystSignal(class_2248 var0) {
        return var0 == class_2246.field_27159 || var0 == class_2246.field_27160 || SusChunkFinderModule.isAmethystGrowth(var0);
    }

    private static boolean isAmethystGrowth(class_2248 var0) {
        return var0 == class_2246.field_27164 || var0 == class_2246.field_27163 || var0 == class_2246.field_27162 || var0 == class_2246.field_27161;
    }

    private void pruneUnloaded() {
        if (this.mc.field_1687 != null && this.ticks % 20 == 0 && this.geodes.entrySet().removeIf(var1x -> {
            int var3;
            int var2 = class_1923.method_8325((long)((Long)var1x.getKey()));
            return !this.mc.field_1687.method_8393(var2, var3 = class_1923.method_8332((long)((Long)var1x.getKey())));
        })) {
            this.candidatesDirty = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventListener
    public void onRender3D(Render3DEvent var1) {
        if (this.mc.field_1687 != null && !this.geodes.isEmpty()) {
            this.getCandidateChunks();
            int var2 = this.alpha.getIntValue();
            if (var2 != 0) {
                class_4184 var6;
                Color var3 = new Color(255, 0, 0, var2);
                this.candidatePlates.draw(var1, (LongSet)this.candidateKeys, var3, null, 1.0f, 63.0, 0.05);
                boolean var4 = this.markGeodeChunks.getValue();
                boolean var5 = this.renderClusters.getValue();
                if ((var4 || var5) && (var6 = RenderUtils.getCamera()) != null && var6.method_19332()) {
                    Color var7 = new Color(180, 100, 255, var2 / 3);
                    Color var8 = new Color(195, 125, 255, var2);
                    this.renderBoxes.clear();
                    this.renderLines.clear();
                    for (ChunkCluster var10 : this.geodes.values()) {
                        double var11 = var10.pos.method_8326();
                        double var13 = var10.pos.method_8328();
                        if (var4 && RenderVisibility.box(var11, 63.0, var13, var11 + 16.0, 63.05, var13 + 16.0)) {
                            this.renderBoxes.add(new RenderUtils.ColoredBox(var11, 63.0, var13, var11 + 16.0, 63.05, var13 + 16.0, var7));
                            RenderUtils.addBoxOutline(this.renderLines, var11, 63.0, var13, var11 + 16.0, 63.05, var13 + 16.0, var8, 1.5f);
                        }
                        if (!var5) continue;
                        for (class_2338 var16 : var10.clusterPositions) {
                            if (!RenderVisibility.box(var16.method_10263(), var16.method_10264(), var16.method_10260(), var16.method_10263() + 1, var16.method_10264() + 1, var16.method_10260() + 1)) continue;
                            this.renderBoxes.add(new RenderUtils.ColoredBox(var16.method_10263(), var16.method_10264(), var16.method_10260(), var16.method_10263() + 1, var16.method_10264() + 1, var16.method_10260() + 1, var7));
                        }
                    }
                    class_4587 var201 = var1.matrixStack;
                    var1.matrixStack.method_22903();
                    try {
                        RenderUtils.applyCameraTransform(var201, var6);
                        RenderUtils.renderFilledBoxes(var201, this.renderBoxes);
                        RenderUtils.renderLines(var201, this.renderLines);
                    }
                    finally {
                        var201.method_22909();
                    }
                    return;
                }
            }
        }
    }

    private Set<Long> getCandidateChunks() {
        boolean var2;
        int var1 = this.simulationDistance.getIntValue();
        boolean bl = var2 = this.scanQueue.isEmpty() || this.ticksSinceCommit >= 60;
        if (var1 != this.cachedSimulationRadius || this.candidatesDirty && var2) {
            this.cachedCandidates = this.calculateCandidateChunks(var1);
            this.candidateKeys.clear();
            this.candidateKeys.addAll(this.cachedCandidates);
            this.cachedSimulationRadius = var1;
            this.candidatesDirty = false;
            this.ticksSinceCommit = 0;
        }
        return this.cachedCandidates;
    }

    private Set<Long> calculateCandidateChunks(int var1) {
        ArrayList<ChunkCluster> var2 = new ArrayList<ChunkCluster>(this.geodes.values());
        var2.sort(Comparator.comparingInt(var0 -> var0.pos.field_9181).thenComparingInt(var0 -> var0.pos.field_9180));
        long[] var3 = new long[var2.size()];
        for (int var4 = 0; var4 < var2.size(); ++var4) {
            var3[var4] = var2.get((int)var4).pos.method_8324();
        }
        return ChunkRegionBuilder.build(var3, var1);
    }

    static {
        int[][] var6 = new int[6][];
        int[] nArray = new int[3];
        int[] var12 = nArray;
        nArray[0] = 1;
        var12[1] = 0;
        var12[2] = 0;
        var6[0] = var12;
        int[] nArray2 = new int[3];
        int[] var14 = nArray2;
        nArray2[0] = -1;
        var14[1] = 0;
        var14[2] = 0;
        var6[1] = var14;
        int[] nArray3 = new int[3];
        int[] var16 = nArray3;
        nArray3[0] = 0;
        var16[1] = 1;
        var16[2] = 0;
        var6[2] = var16;
        int[] nArray4 = new int[3];
        int[] var18 = nArray4;
        nArray4[0] = 0;
        var18[1] = -1;
        var18[2] = 0;
        var6[3] = var18;
        int[] nArray5 = new int[3];
        int[] var6x = nArray5;
        nArray5[0] = 0;
        var6x[1] = 0;
        var6x[2] = 1;
        var6[4] = var6x;
        int[] nArray6 = new int[3];
        int[] var22 = nArray6;
        nArray6[0] = 0;
        var22[1] = 0;
        var22[2] = -1;
        var6[5] = var22;
        NEIGHBORS = var6;
    }

    private record ChunkCluster(class_1923 pos, int hits, List<class_2338> clusterPositions) {
    }
}

