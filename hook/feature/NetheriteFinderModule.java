/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1542
 *  net.minecraft.class_1802
 *  net.minecraft.class_1923
 *  net.minecraft.class_2246
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2818
 *  net.minecraft.class_2826
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.BlockUtil;
import hook.utils.EncryptedString;
import hook.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_4184;
import net.minecraft.class_4587;

public final class NetheriteFinderModule
extends Module {
    private static final int RESCAN_TICKS = 20;
    private final NumberSetting maxShown = new NumberSetting(EncryptedString.of("Max Shown"), 1.0, 1000.0, 100.0, 1.0).getValue(EncryptedString.of("Maximum hits shown, nearest exact blocks first"));
    private final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), false);
    private volatile List<BoundingBox> hits = List.of();
    private final List<RenderUtils.ColoredBox> renderBoxes = new ArrayList<RenderUtils.ColoredBox>();
    private final List<RenderUtils.ColoredLine> renderLines = new ArrayList<RenderUtils.ColoredLine>();
    private final AtomicBoolean scanning = new AtomicBoolean();
    private ExecutorService worker;
    private int timer;

    public NetheriteFinderModule() {
        super(EncryptedString.of("Netherite Finder"), EncryptedString.of("Finds ancient debris even with anti-xray"), -1, Category.DONUT);
        this.addSettings(this.maxShown, this.tracers);
    }

    @Override
    public void onEnable() {
        this.hits = List.of();
        this.renderBoxes.clear();
        this.renderLines.clear();
        this.timer = 0;
        this.scanning.set(false);
        this.worker = Executors.newSingleThreadExecutor(var0 -> {
            Thread var1 = new Thread(var0, "Hook-NetheriteFinder");
            var1.setDaemon(true);
            var1.setPriority(1);
            return var1;
        });
        super.onEnable();
    }

    @Override
    public void onDisable() {
        ExecutorService var1 = this.worker;
        this.worker = null;
        if (var1 != null) {
            var1.shutdownNow();
        }
        this.hits = List.of();
        this.scanning.set(false);
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null || ++this.timer < 20) {
            return;
        }
        this.timer = 0;
        ExecutorService activeWorker = this.worker;
        if (activeWorker == null || activeWorker.isShutdown() || !this.scanning.compareAndSet(false, true)) {
            return;
        }
        double playerX = this.mc.field_1724.method_23317();
        double playerY = this.mc.field_1724.method_23318();
        double playerZ = this.mc.field_1724.method_23321();
        List<class_2818> loaded = BlockUtil.getLoadedChunksList();
        int cap = this.maxShown.getIntValue();
        activeWorker.submit(() -> {
            try {
                List<BoundingBox> found = NetheriteFinderModule.scan(loaded, cap, playerX, playerY, playerZ);
                if (this.isEnabled() && this.worker == activeWorker) {
                    this.hits = found;
                }
            }
            catch (Throwable throwable) {
            }
            finally {
                this.scanning.set(false);
            }
        });
    }

    private static List<BoundingBox> scan(List<class_2818> loaded, int cap, double playerX, double playerY, double playerZ) {
        ArrayList<BoundingBox> hits = new ArrayList<BoundingBox>();
        for (class_2818 chunk : loaded) {
            class_2826[] sections = chunk.method_12006();
            int bottomSection = chunk.method_32891();
            class_1923 chunkPos = chunk.method_12004();
            for (int sectionIndex = 0; sectionIndex < sections.length; ++sectionIndex) {
                int z;
                class_2826 section = sections[sectionIndex];
                if (section == null || !section.method_12265().method_19526(state -> state.method_27852(class_2246.field_22109))) continue;
                int sectionY = bottomSection + sectionIndex << 4;
                int priorHits = hits.size();
                for (int y = 0; y < 16; ++y) {
                    for (z = 0; z < 16; ++z) {
                        for (int x = 0; x < 16; ++x) {
                            if (!section.method_12254(x, y, z).method_27852(class_2246.field_22109)) continue;
                            int worldX = chunkPos.method_8326() + x;
                            int worldY = sectionY + y;
                            int worldZ = chunkPos.method_8328() + z;
                            hits.add(new BoundingBox(worldX, worldY, worldZ, worldX + 1, worldY + 1, worldZ + 1, true));
                        }
                    }
                }
                if (hits.size() != priorHits) continue;
                int xx = chunkPos.method_8326();
                z = chunkPos.method_8328();
                hits.add(new BoundingBox(xx, sectionY, z, xx + 16, sectionY + 16, z + 16, false));
            }
        }
        hits.sort(Comparator.comparingInt(hit -> hit.exact() ? 0 : 1).thenComparingDouble(hit -> {
            double dx = (hit.x1() + hit.x2()) * 0.5 - playerX;
            double dy = (hit.y1() + hit.y2()) * 0.5 - playerY;
            double dz = (hit.z1() + hit.z2()) * 0.5 - playerZ;
            return dx * dx + dy * dy + dz * dz;
        }));
        if (hits.size() > cap) {
            hits = new ArrayList(hits.subList(0, cap));
        }
        return List.copyOf(hits);
    }

    @EventListener
    public void onRender3D(Render3DEvent var1) {
        class_4184 var2;
        if (this.mc.field_1687 != null && (var2 = RenderUtils.getCamera()) != null && var2.method_19332()) {
            class_4587 var3 = var1.matrixStack;
            var3.method_22903();
            RenderUtils.applyCameraTransform(var3, var2);
            long var4 = System.currentTimeMillis() % 2000L;
            double var6 = var4 < 1000L ? (double)var4 / 1000.0 : (double)(2000L - var4) / 1000.0;
            int var8 = (int)(255.0 - 155.0 * var6);
            Color var9 = new Color(190, 90, 255, var8);
            Color var10 = new Color(190, 90, 255, 255);
            this.renderBoxes.clear();
            this.renderLines.clear();
            class_243 var11 = RenderUtils.getTracerStart(var2);
            for (BoundingBox var13 : this.hits) {
                if (var13.exact()) {
                    this.renderBoxes.add(new RenderUtils.ColoredBox(var13.x1(), var13.y1(), var13.z1(), var13.x2(), var13.y2(), var13.z2(), var9));
                }
                RenderUtils.addBoxOutline(this.renderLines, var13.x1(), var13.y1(), var13.z1(), var13.x2(), var13.y2(), var13.z2(), var10, 1.5f);
                if (!var13.exact() && !this.tracers.getValue()) continue;
                this.renderLines.add(new RenderUtils.ColoredLine(var11, var13.center(), var10, 1.5f));
            }
            for (class_1297 var17 : this.mc.field_1687.method_18112()) {
                class_1542 var14;
                if (!(var17 instanceof class_1542) || !(var14 = (class_1542)var17).method_6983().method_31574(class_1802.field_22019)) continue;
                class_238 var15 = var14.method_5829();
                this.renderBoxes.add(new RenderUtils.ColoredBox(var15.field_1323, var15.field_1322, var15.field_1321, var15.field_1320, var15.field_1325, var15.field_1324, var9));
                RenderUtils.addBoxOutline(this.renderLines, var15.field_1323, var15.field_1322, var15.field_1321, var15.field_1320, var15.field_1325, var15.field_1324, var10, 1.5f);
                this.renderLines.add(new RenderUtils.ColoredLine(var11, new class_243(var14.method_23317(), var14.method_23318(), var14.method_23321()), var10, 1.5f));
            }
            RenderUtils.renderFilledBoxes(var3, this.renderBoxes);
            RenderUtils.renderLines(var3, this.renderLines);
            var3.method_22909();
        }
    }

    private record BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2, boolean exact) {
        private class_243 center() {
            return new class_243((this.x1 + this.x2) * 0.5, (this.y1 + this.y2) * 0.5, (this.z1 + this.z2) * 0.5);
        }
    }
}

