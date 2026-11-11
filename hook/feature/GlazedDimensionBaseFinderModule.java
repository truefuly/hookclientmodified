package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.BlockUtil;
import hook.utils.EncryptedString;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_1923;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2609;
import net.minecraft.class_2611;
import net.minecraft.class_2627;
import net.minecraft.class_2636;
import net.minecraft.class_2818;
import net.minecraft.class_3719;

/** Shared Hook port of Glazed's End and Nether RTP stash scanning modules. */
public abstract class GlazedDimensionBaseFinderModule extends Module {
    private final String dimension;
    private final String command;
    private final BooleanSetting notifications = new BooleanSetting(EncryptedString.of("Notifications"), true);
    private final NumberSetting minimumStorage = new NumberSetting(EncryptedString.of("Minimum Storage Count"), 1.0, 100.0, 4.0, 1.0);
    private final BooleanSetting spawnerIsCritical = new BooleanSetting(EncryptedString.of("Single Spawner Is Base"), true);
    private final NumberSetting rtpInterval = new NumberSetting(EncryptedString.of("RTP Interval Seconds"), 5.0, 60.0, 10.0, 1.0);
    private final Set<String> scannedChunks = new HashSet<>();
    private Object scannedWorld;
    private long lastRtp;
    private int scanDelay;

    protected GlazedDimensionBaseFinderModule(String name, String dimension, String command) {
        super(EncryptedString.of(name), EncryptedString.of("RTPs to " + dimension + " and scans newly loaded chunks for stash storage."), -1, Category.GLAZED);
        this.dimension = dimension;
        this.command = command;
        this.addSettings(this.notifications, this.minimumStorage, this.spawnerIsCritical, this.rtpInterval);
    }

    @Override
    public void onEnable() {
        this.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.method_1562() == null) return;
        if (this.scannedWorld != this.mc.field_1687) {
            this.scannedWorld = this.mc.field_1687;
            this.scannedChunks.clear();
        }
        long now = System.currentTimeMillis();
        if (now - this.lastRtp >= this.rtpInterval.getValue() * 1000.0) {
            this.mc.method_1562().method_45730(this.command);
            this.lastRtp = now;
        }
        if (++this.scanDelay < 20) return;
        this.scanDelay = 0;
        String currentDimension = this.mc.field_1687.method_27983().method_29177().toString();
        if (!currentDimension.equals(this.dimension)) return;
        for (class_2818 chunk : BlockUtil.getLoadedChunksList()) {
            class_1923 pos = chunk.method_12004();
            String key = this.dimension + ':' + pos.field_9181 + ':' + pos.field_9180;
            if (!this.scannedChunks.add(key)) continue;
            int[] counts = this.countStorage(chunk);
            int storageCount = counts[0];
            int spawnerCount = counts[1];
            if ((this.spawnerIsCritical.getValue() && spawnerCount > 0) || storageCount >= this.minimumStorage.getIntValue()) {
                this.notifyFound(pos, storageCount, spawnerCount);
            }
        }
    }

    private int[] countStorage(class_2818 chunk) {
        int storage = 0;
        int spawners = 0;
        for (Object value : chunk.method_12214().values()) {
            if (!(value instanceof class_2586 blockEntity)) continue;
            if (blockEntity instanceof class_2636) {
                spawners++;
            } else if (blockEntity instanceof class_2595 || blockEntity instanceof class_3719
                || blockEntity instanceof class_2627 || blockEntity instanceof class_2611
                || blockEntity instanceof class_2609) {
                storage++;
            }
        }
        return new int[]{storage, spawners};
    }

    private void notifyFound(class_1923 pos, int storage, int spawners) {
        if (!this.notifications.getValue() || this.mc.field_1724 == null) return;
        int x = pos.field_9181 * 16 + 8;
        int z = pos.field_9180 * 16 + 8;
        this.mc.field_1724.method_7353(net.minecraft.class_2561.method_43470(
            "[Glazed] " + this.dimension + " stash at X " + x + ", Z " + z
                + " (storage " + storage + ", spawners " + spawners + ")"), false);
    }

    private void reset() {
        this.scannedChunks.clear();
        this.scannedWorld = null;
        this.lastRtp = 0L;
        this.scanDelay = 0;
    }
}
