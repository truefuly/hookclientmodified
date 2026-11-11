/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1923
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2818
 *  net.minecraft.class_2826
 *  net.minecraft.class_2960
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_7923
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.ChunkDataEvent;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.StorageBlocksSetting;
import hook.module.setting.StringSetting;
import hook.utils.BlockUtil;
import hook.utils.EncryptedString;
import hook.utils.RenderUtils;
import hook.utils.RenderVisibility;
import hook.utils.embed.DiscordWebhook;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7923;

public final class BlockNotifierModule
extends Module {
    private static final int MAX_RENDERED = 4096;
    private final StorageBlocksSetting blocks = BlockNotifierModule.createPalette();
    private final ModeSetting<NotificationMode> notificationMode = new ModeSetting<NotificationMode>(EncryptedString.of("Notification Mode"), NotificationMode.BOTH, NotificationMode.class);
    private final BooleanSetting disconnectOnFind = new BooleanSetting(EncryptedString.of("Disconnect On Find"), false);
    private final BooleanSetting notifications = new BooleanSetting(EncryptedString.of("Notifications"), true);
    private final BooleanSetting webhookEnabled = new BooleanSetting(EncryptedString.of("Webhook Enabled"), false);
    private final StringSetting webhookUrl = new StringSetting(EncryptedString.of("Webhook URL"), "");
    private final BooleanSetting selfPing = new BooleanSetting(EncryptedString.of("Self Ping"), false);
    private final StringSetting discordId = new StringSetting(EncryptedString.of("Discord ID"), "");
    private final BooleanSetting showEsp = new BooleanSetting(EncryptedString.of("Show ESP"), true);
    private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 0.0, 255.0, 100.0, 1.0);
    private final BooleanSetting outlines = new BooleanSetting(EncryptedString.of("Outlines"), true);
    private final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), true);
    private final BooleanSetting onlyNew = new BooleanSetting(EncryptedString.of("Only Render New"), false);
    private final NumberSetting renderDistance = new NumberSetting(EncryptedString.of("Render Distance"), 0.0, 500.0, 0.0, 5.0);
    private final Map<class_2248, StorageBlocksSetting.Entry> selected = new HashMap<class_2248, StorageBlocksSetting.Entry>();
    private final Map<Long, List<BlockEntry>> perChunk = new HashMap<Long, List<BlockEntry>>();
    private final Set<class_2338> everFound = new HashSet<class_2338>();
    private final Set<class_2338> newFound = new HashSet<class_2338>();
    private final Queue<Long> scanQueue = new ArrayDeque<Long>();
    private final Set<Long> queued = new HashSet<Long>();
    private final Set<Long> scanned = new HashSet<Long>();
    private final List<RenderUtils.ColoredBox> boxes = new ArrayList<RenderUtils.ColoredBox>();
    private final List<RenderUtils.ColoredLine> lines = new ArrayList<RenderUtils.ColoredLine>();
    private Object trackedWorld;
    private int selectionSignature;
    private int ticks;
    private int totalFound;

    public BlockNotifierModule() {
        super(EncryptedString.of("Block Notifier"), EncryptedString.of("Notifies and highlights selected blocks when chunks load"), -1, Category.RENDER);
        this.addSettings(this.blocks, this.notificationMode, this.disconnectOnFind, this.notifications, this.webhookEnabled, this.webhookUrl, this.selfPing, this.discordId, this.showEsp, this.alpha, this.outlines, this.tracers, this.onlyNew, this.renderDistance);
    }

    private static StorageBlocksSetting createPalette() {
        StorageBlocksSetting palette = new StorageBlocksSetting(EncryptedString.of("Blocks To Find"));
        for (class_2248 block : class_7923.field_41175) {
            if (block == class_2246.field_10124 || block == class_2246.field_10543 || block == class_2246.field_10243) continue;
            class_2960 id = class_7923.field_41175.method_10221((Object)block);
            Color color = Color.getHSBColor((float)(id.toString().hashCode() & 0xFFFF) / 65535.0f, 0.7f, 1.0f);
            palette.add(id.toString(), block.method_9518().getString(), block.method_8389(), false, color);
        }
        return palette;
    }

    @Override
    public void onEnable() {
        this.reset();
        this.trackedWorld = this.mc.field_1687;
        this.refreshSelection();
        this.enqueueLoaded();
        this.chat("Block Notifier activated.");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        int var1 = this.totalFound;
        this.reset();
        if (this.notifications.getValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7b[Block Notifier] \u00a7fDeactivated. Found " + var1 + " blocks this session.")), false);
        }
        super.onDisable();
    }

    private void reset() {
        this.selected.clear();
        this.perChunk.clear();
        this.everFound.clear();
        this.newFound.clear();
        this.scanQueue.clear();
        this.queued.clear();
        this.scanned.clear();
        this.boxes.clear();
        this.lines.clear();
        this.trackedWorld = null;
        this.selectionSignature = 0;
        this.ticks = 0;
        this.totalFound = 0;
    }

    @EventListener
    public void onChunkData(ChunkDataEvent var1) {
        this.enqueue(var1.packet.method_11523(), var1.packet.method_11524());
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            int var2;
            if (this.trackedWorld != this.mc.field_1687) {
                this.reset();
                this.trackedWorld = this.mc.field_1687;
                this.refreshSelection();
                this.enqueueLoaded();
            }
            if ((var2 = this.computeSignature()) != this.selectionSignature) {
                this.perChunk.clear();
                this.everFound.clear();
                this.newFound.clear();
                this.scanQueue.clear();
                this.queued.clear();
                this.scanned.clear();
                this.totalFound = 0;
                this.refreshSelection();
                this.enqueueLoaded();
            }
            if (!this.selected.isEmpty()) {
                ++this.ticks;
                if (this.ticks % 100 == 1) {
                    this.enqueueLoaded();
                }
                if (this.ticks % 400 == 0) {
                    this.scanned.clear();
                }
                this.scanOne();
                if (this.ticks % 20 == 0) {
                    this.perChunk.keySet().removeIf(var1x -> !this.mc.field_1687.method_8393(class_1923.method_8325((long)var1x), class_1923.method_8332((long)var1x)));
                }
            }
        }
    }

    private int computeSignature() {
        int var1 = 1;
        for (StorageBlocksSetting.Entry var3 : this.blocks.entries()) {
            if (!var3.enabled()) continue;
            var1 = 31 * var1 + var3.id().hashCode();
        }
        return var1;
    }

    private void refreshSelection() {
        this.selected.clear();
        for (StorageBlocksSetting.Entry var2 : this.blocks.entries()) {
            if (!var2.enabled()) continue;
            class_7923.field_41175.method_10223(class_2960.method_60654((String)var2.id())).ifPresent(var2x -> this.selected.put((class_2248)var2x.comp_349(), var2));
        }
        this.selectionSignature = this.computeSignature();
    }

    private void enqueueLoaded() {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null && !this.selected.isEmpty()) {
            class_1923 var1 = this.mc.field_1724.method_31476();
            List<class_2818> var2 = BlockUtil.getLoadedChunksList();
            var2.sort(Comparator.comparingInt(var1x -> Math.max(Math.abs(var1x.method_12004().field_9181 - var1.field_9181), Math.abs(var1x.method_12004().field_9180 - var1.field_9180))));
            Iterator<class_2818> iterator = var2.iterator();
            while (iterator.hasNext()) {
                class_2818 var4Object;
                class_2818 var4 = var4Object = iterator.next();
                this.enqueue(var4.method_12004().field_9181, var4.method_12004().field_9180);
            }
        }
    }

    private void enqueue(int var1, int var2) {
        long var3 = class_1923.method_8331((int)var1, (int)var2);
        if (!this.selected.isEmpty() && !this.scanned.contains(var3) && this.queued.add(var3)) {
            this.scanQueue.offer(var3);
        }
    }

    private void scanOne() {
        Long var1 = this.scanQueue.poll();
        if (var1 != null) {
            this.queued.remove(var1);
            this.scanned.add(var1);
            int var2 = class_1923.method_8325((long)var1);
            int var3 = class_1923.method_8332((long)var1);
            if (this.mc.field_1687.method_8393(var2, var3)) {
                class_2818 var4 = this.mc.field_1687.method_8497(var2, var3);
                ArrayList<BlockEntry> var5 = new ArrayList<BlockEntry>();
                HashMap<class_2248, Integer> var6 = new HashMap<class_2248, Integer>();
                class_2826[] var7 = var4.method_12006();
                int var8 = var4.method_32891();
                for (int var9 = 0; var9 < var7.length; ++var9) {
                    class_2826 var10 = var7[var9];
                    if (var10 == null || !var10.method_12265().method_19526(var1x -> this.selected.containsKey(var1x.method_26204()))) continue;
                    int var11 = var8 + var9 << 4;
                    for (int var12 = 0; var12 < 16; ++var12) {
                        for (int var13 = 0; var13 < 16; ++var13) {
                            for (int var14 = 0; var14 < 16; ++var14) {
                                class_2248 var15 = var10.method_12254(var14, var12, var13).method_26204();
                                if (!this.selected.containsKey(var15)) continue;
                                class_2338 var16 = new class_2338(var4.method_12004().method_8326() + var14, var11 + var12, var4.method_12004().method_8328() + var13);
                                var5.add(new BlockEntry(var16, var15));
                                if (!this.everFound.add(var16)) continue;
                                this.newFound.add(var16);
                                ++this.totalFound;
                                var6.merge(var15, 1, Integer::sum);
                            }
                        }
                    }
                }
                if (var5.isEmpty()) {
                    this.perChunk.remove(var1);
                } else {
                    this.perChunk.put(var1, List.copyOf(var5));
                }
                if (!var6.isEmpty()) {
                    this.notifyFound(var4.method_12004(), var6);
                }
            }
        }
    }

    private void notifyFound(class_1923 var1, Map<class_2248, Integer> var2) {
        int var3 = var1.method_33940();
        int var4 = var1.method_33942();
        StringBuilder var5 = new StringBuilder();
        for (Map.Entry<class_2248, Integer> var7 : var2.entrySet()) {
            if (!var5.isEmpty()) {
                var5.append(", ");
            }
            var5.append(var7.getKey().method_9518().getString()).append(" x").append(var7.getValue());
        }
        String var8 = "Found " + String.valueOf(var5) + " at chunk " + var3 + ", " + var4;
        if (this.notificationMode.isMode(NotificationMode.CHAT) || this.notificationMode.isMode(NotificationMode.BOTH)) {
            this.chat(var8);
        }
        if ((this.notificationMode.isMode(NotificationMode.TOAST) || this.notificationMode.isMode(NotificationMode.BOTH)) && this.mc.field_1724 != null) {
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a76[Block Notifier] \u00a7f" + var8)), true);
        }
        if (this.webhookEnabled.getValue() && !this.webhookUrl.getValue().isBlank()) {
            this.sendWebhook(var8, var3, var4);
        }
        if (this.disconnectOnFind.getValue() && this.mc.method_1562() != null) {
            this.toggle(false);
            this.mc.method_1562().method_48296().method_10747((class_2561)class_2561.method_43470((String)("Target block found at " + var3 + ", " + var4)));
        }
    }

    private void sendWebhook(String var1, int var2, int var3) {
        String var4 = this.webhookUrl.getValue().trim();
        String var5 = this.selfPing.getValue() && !this.discordId.getValue().isBlank() ? "<@" + this.discordId.getValue().trim() + ">" : null;
        String var6 = this.mc.method_1558() == null ? "Singleplayer" : this.mc.method_1558().field_3761;
        CompletableFuture.runAsync(() -> {
            try {
                DiscordWebhook var7 = new DiscordWebhook(var4);
                var7.b("Hook Block Notifier");
                if (var5 != null) {
                    var7.a(var5);
                }
                var7.addEmbed(new DiscordWebhook.EmbedObject().setTitle("Block Notifier Alert").setDescription(var1).setColor(new Color(120, 190, 255)).addField("Coordinates", var2 + ", " + var3, true).addField("Server", var6, true));
                var7.execute();
            }
            catch (Exception var8) {
                this.mc.execute(() -> this.chat("Webhook failed: " + var8.getMessage()));
            }
        });
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.mc.field_1724 == null || this.perChunk.isEmpty() || !this.showEsp.getValue() && !this.tracers.getValue()) {
            return;
        }
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null || !camera.method_19332()) {
            return;
        }
        this.boxes.clear();
        this.lines.clear();
        class_243 tracerStart = RenderUtils.getTracerStart(camera);
        double maxDistance = this.renderDistance.getValue();
        int rendered = 0;
        block0: for (List<BlockEntry> chunkEntries : this.perChunk.values()) {
            for (BlockEntry entry : chunkEntries) {
                StorageBlocksSetting.Entry selectedBlock;
                class_2338 pos = entry.pos();
                if (!this.tracers.getValue() && !RenderVisibility.box(pos.method_10263(), pos.method_10264(), pos.method_10260(), pos.method_10263() + 1, pos.method_10264() + 1, pos.method_10260() + 1) || this.onlyNew.getValue() && !this.newFound.contains(pos) || maxDistance > 0.0 && this.mc.field_1724.method_5707(class_243.method_24953((class_2382)pos)) > maxDistance * maxDistance || (selectedBlock = this.selected.get(entry.block())) == null) continue;
                Color baseColor = selectedBlock.color();
                Color color = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), this.alpha.getIntValue());
                if (this.showEsp.getValue()) {
                    double inset = this.outlines.getValue() ? 0.05 : 0.0;
                    this.boxes.add(new RenderUtils.ColoredBox((double)pos.method_10263() + inset, (double)pos.method_10264() + inset, (double)pos.method_10260() + inset, (double)(pos.method_10263() + 1) - inset, (double)(pos.method_10264() + 1) - inset, (double)(pos.method_10260() + 1) - inset, color));
                }
                if (this.tracers.getValue()) {
                    this.lines.add(new RenderUtils.ColoredLine(tracerStart, class_243.method_24953((class_2382)pos), color, 1.5f));
                }
                if (++rendered < 4096) continue;
                continue block0;
            }
        }
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        RenderUtils.applyCameraTransform(matrices, camera);
        RenderUtils.renderFilledBoxes(matrices, this.boxes);
        RenderUtils.renderLines(matrices, this.lines);
        matrices.method_22909();
    }

    private void chat(String message) {
        if (this.notifications.getValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7b[Block Notifier] \u00a7f" + message)), false);
        }
    }

    public int getDetectedBlockCount() {
        return this.totalFound;
    }

    public static enum NotificationMode {
        CHAT,
        TOAST,
        BOTH;

    }

    private record BlockEntry(class_2338 pos, class_2248 block) {
    }
}

