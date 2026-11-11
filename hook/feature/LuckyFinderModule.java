/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_1132
 *  net.minecraft.class_1923
 *  net.minecraft.class_2818
 *  net.minecraft.class_2902$class_2903
 *  net.minecraft.class_2960
 *  net.minecraft.class_3414
 *  net.minecraft.class_368
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_5218
 *  net.minecraft.class_638
 *  net.minecraft.class_642
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.feature.LuckyFinderToast;
import hook.feature.RetryDeadline;
import hook.module.Category;
import hook.module.Module;
import hook.module.NewFeature;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.BytecodeException;
import hook.utils.EncryptedString;
import hook.utils.RenderUtils;
import hook.utils.RenderVisibility;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1132;
import net.minecraft.class_1923;
import net.minecraft.class_2818;
import net.minecraft.class_2902;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_368;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_5218;
import net.minecraft.class_638;
import net.minecraft.class_642;

public final class LuckyFinderModule
extends Module
implements NewFeature {
    private static final int PLATE_SIZE = 48;
    private static final double PLATE_THICKNESS = 0.15;
    private static final class_3414 FLAG_SOUND = class_3414.method_47908((class_2960)class_2960.method_60655((String)"hook", (String)"lucky_flag"));
    private final BooleanSetting notifications = new BooleanSetting(EncryptedString.of("Notifications"), true);
    private final BooleanSetting sound = new BooleanSetting(EncryptedString.of("Sound"), true);
    private final NumberSetting volume = new NumberSetting(EncryptedString.of("Sound Volume"), 0.0, 100.0, 35.0, 1.0);
    private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 0.0, 255.0, 75.0, 1.0);
    private final RetryDeadline timer = new RetryDeadline();
    private final Map<Long, ChunkMarker> flags = new LinkedHashMap<Long, ChunkMarker>();
    private final ArrayList<RenderUtils.ColoredBox> geometry = new ArrayList();
    private final ArrayList<RenderUtils.ColoredBox> visibleGeometry = new ArrayList();
    private class_638 trackedWorld;
    private String flagWorldKey;
    private boolean geometryDirty;
    private int cachedAlpha = -1;
    private int lastChunkX;
    private int lastChunkZ;
    private int lastViewDistance = -1;

    public LuckyFinderModule() {
        super(EncryptedString.of("Lucky Finder"), EncryptedString.of("Follow your luck."), -1, Category.DONUT);
        this.addSettings(this.notifications, this.sound, this.volume, this.alpha);
    }

    @Override
    public void onEnable() {
        this.reset();
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            this.timer.arm(System.nanoTime(), ThreadLocalRandom.current());
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    private void reset() {
        this.timer.clear();
        this.clearVisuals();
        this.trackedWorld = null;
        this.flagWorldKey = null;
    }

    private void clearVisuals() {
        this.flags.clear();
        this.geometry.clear();
        this.visibleGeometry.clear();
        this.cachedAlpha = -1;
        this.lastViewDistance = -1;
        this.geometryDirty = true;
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            this.trackedWorld = null;
            return;
        }
        if (this.trackedWorld != this.mc.field_1687) {
            String currentWorldKey = this.worldKey();
            if (!currentWorldKey.equals(this.flagWorldKey)) {
                this.clearVisuals();
                this.flagWorldKey = currentWorldKey;
            }
            this.trackedWorld = this.mc.field_1687;
            this.lastViewDistance = -1;
        }
        this.pruneFlags();
        long now = System.nanoTime();
        if (!this.timer.isArmed()) {
            this.timer.arm(now, ThreadLocalRandom.current());
            return;
        }
        if (!this.timer.due(now)) {
            return;
        }
        if (this.tryFlag()) {
            this.timer.arm(now, ThreadLocalRandom.current());
        } else {
            this.timer.retry(now);
        }
    }

    private String worldKey() {
        String location;
        class_642 server = this.mc.method_1558();
        if (server != null) {
            location = "server:" + server.field_3761.toLowerCase(Locale.ROOT);
        } else if (this.mc.method_1576() == null) {
            location = "session:" + System.identityHashCode(this.mc.field_1687);
        } else {
            class_1132 integrated = this.mc.method_1576();
            location = "local:" + String.valueOf(integrated.method_27050(class_5218.field_24188).toAbsolutePath().normalize());
        }
        return location + "|" + String.valueOf(this.mc.field_1687.method_27983().method_29177());
    }

    private void pruneFlags() {
        if (this.flags.isEmpty()) {
            return;
        }
        class_1923 playerChunk = this.mc.field_1724.method_31476();
        if (!this.mc.field_1687.method_8393(playerChunk.field_9181, playerChunk.field_9180)) {
            return;
        }
        int viewDistance = this.mc.field_1690.method_38521();
        if (this.lastChunkX == playerChunk.field_9181 && this.lastChunkZ == playerChunk.field_9180 && this.lastViewDistance == viewDistance) {
            return;
        }
        this.lastChunkX = playerChunk.field_9181;
        this.lastChunkZ = playerChunk.field_9180;
        this.lastViewDistance = viewDistance;
        boolean removed = this.flags.values().removeIf(marker -> Math.abs((long)marker.cx - (long)playerChunk.field_9181) > (long)viewDistance || Math.abs((long)marker.cz - (long)playerChunk.field_9180) > (long)viewDistance);
        if (removed) {
            this.geometryDirty = true;
        }
    }

    private boolean tryFlag() {
        class_1923 playerChunk = this.mc.field_1724.method_31476();
        int radius = Math.max(0, Math.min(8, this.mc.field_1690.method_38521() - 2));
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 32; ++attempt) {
            int surfaceY;
            int chunkX = playerChunk.field_9181 + random.nextInt(-radius, radius + 1);
            int chunkZ = playerChunk.field_9180 + random.nextInt(-radius, radius + 1);
            long key = class_1923.method_8331((int)chunkX, (int)chunkZ);
            if (attempt < 24 && this.flags.containsKey(key) || (surfaceY = this.highestSurface(chunkX, chunkZ)) == Integer.MIN_VALUE) continue;
            this.flags.put(key, new ChunkMarker(chunkX, chunkZ, (double)surfaceY + 1.002));
            this.geometryDirty = true;
            int centerX = (chunkX << 4) + 8;
            int centerZ = (chunkZ << 4) + 8;
            if (this.notifications.getValue()) {
                this.mc.method_1566().method_1999((class_368)new LuckyFinderToast(centerX, centerZ));
            }
            if (this.sound.getValue() && this.volume.getValue() > 0.0) {
                this.mc.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)FLAG_SOUND, (float)1.0f, (float)(this.volume.getFloatValue() / 100.0f)));
            }
            return true;
        }
        return false;
    }

    private int highestSurface(int chunkX, int chunkZ) {
        for (int offsetX = -1; offsetX <= 1; ++offsetX) {
            for (int offsetZ = -1; offsetZ <= 1; ++offsetZ) {
                if (this.mc.field_1687.method_8393(chunkX + offsetX, chunkZ + offsetZ)) continue;
                return Integer.MIN_VALUE;
            }
        }
        int highest = Integer.MIN_VALUE;
        for (int offsetX = -1; offsetX <= 1; ++offsetX) {
            for (int offsetZ = -1; offsetZ <= 1; ++offsetZ) {
                class_2818 chunk = this.mc.field_1687.method_8497(chunkX + offsetX, chunkZ + offsetZ);
                for (int localX = 0; localX < 16; ++localX) {
                    for (int localZ = 0; localZ < 16; ++localZ) {
                        highest = Math.max(highest, chunk.method_12005(class_2902.class_2903.field_13202, localX, localZ));
                    }
                }
            }
        }
        return highest < this.mc.field_1687.method_31607() ? Integer.MIN_VALUE : highest;
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.mc.field_1724 == null || this.trackedWorld != this.mc.field_1687 || this.flags.isEmpty()) {
            return;
        }
        int alpha = this.alpha.getIntValue();
        if (alpha <= 0) {
            return;
        }
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null || !camera.method_19332()) {
            return;
        }
        if (this.geometryDirty || this.cachedAlpha != alpha) {
            this.geometry.clear();
            Color color = new Color(0, 255, 70, alpha);
            for (ChunkMarker marker : this.flags.values()) {
                double centerX = (double)marker.cx * 16.0 + 8.0;
                double centerZ = (double)marker.cz * 16.0 + 8.0;
                this.geometry.add(new RenderUtils.ColoredBox(centerX - 24.0, marker.y, centerZ - 24.0, centerX + 24.0, marker.y + 0.15, centerZ + 24.0, color));
            }
            this.geometryDirty = false;
            this.cachedAlpha = alpha;
        }
        this.visibleGeometry.clear();
        double playerX = this.mc.field_1724.method_23317();
        double playerZ = this.mc.field_1724.method_23321();
        double maximumDistance = (double)this.mc.field_1690.method_38521() * 16.0 + 24.0;
        for (RenderUtils.ColoredBox box : this.geometry) {
            double centerX = (box.x1() + box.x2()) / 2.0;
            double centerZ = (box.z1() + box.z2()) / 2.0;
            if (Math.abs(centerX - playerX) > maximumDistance || Math.abs(centerZ - playerZ) > maximumDistance || !RenderVisibility.box(box.x1(), box.y1(), box.z1(), box.x2(), box.y2(), box.z2())) continue;
            this.visibleGeometry.add(box);
        }
        if (this.visibleGeometry.isEmpty()) {
            return;
        }
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        try {
            RenderUtils.applyCameraTransform(matrices, camera);
            RenderUtils.renderFilledBoxes(matrices, this.visibleGeometry);
        }
        catch (Throwable error) {
            matrices.method_22909();
            throw BytecodeException.unchecked(error);
        }
        matrices.method_22909();
    }

    private record ChunkMarker(int cx, int cz, double y) {
    }
}

