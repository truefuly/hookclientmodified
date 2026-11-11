/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1299
 *  net.minecraft.class_2338
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2586
 *  net.minecraft.class_2636
 *  net.minecraft.class_2818
 *  net.minecraft.class_2960
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_638
 *  net.minecraft.class_7923
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.mixin.MobSpawnerLogicAccessor;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.BlockUtil;
import hook.utils.EncryptedString;
import hook.utils.RenderUtils;
import hook.utils.RenderVisibility;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1299;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2636;
import net.minecraft.class_2818;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_7923;

public final class SpawnerNotifyModule
extends Module {
    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 8.0, 512.0, 256.0, 8.0);
    private final BooleanSetting pillar = new BooleanSetting(EncryptedString.of("3x3 Pillar"), true);
    private final NumberSetting pillarAlpha = new NumberSetting(EncryptedString.of("Pillar Alpha"), 0.0, 255.0, 80.0, 1.0);
    private final BooleanSetting spawnerBox = new BooleanSetting(EncryptedString.of("Spawner Box"), true);
    private final NumberSetting boxAlpha = new NumberSetting(EncryptedString.of("Box Alpha"), 0.0, 255.0, 150.0, 1.0);
    private final BooleanSetting chatAlerts = new BooleanSetting(EncryptedString.of("Chat Alerts"), true);
    private final BooleanSetting soundAlerts = new BooleanSetting(EncryptedString.of("Sound Alerts"), true);
    private final Map<class_2338, String> trackedSpawners = new HashMap<class_2338, String>();
    private final Set<Long> alertedSpawners = new HashSet<Long>();
    private final List<RenderUtils.ColoredBox> renderBoxes = new ArrayList<RenderUtils.ColoredBox>();
    private class_638 trackedWorld;
    private int scanDelay;

    public SpawnerNotifyModule() {
        super(EncryptedString.of("Spawner Notify"), EncryptedString.of("Highlights nearby spawners and alerts when one is found"), -1, Category.RENDER);
        this.addSettings(this.range, this.pillar, this.pillarAlpha, this.spawnerBox, this.boxAlpha, this.chatAlerts, this.soundAlerts);
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

    private void reset() {
        this.trackedSpawners.clear();
        this.alertedSpawners.clear();
        this.renderBoxes.clear();
        this.trackedWorld = null;
        this.scanDelay = 0;
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            int var10000;
            if (this.trackedWorld != this.mc.field_1687) {
                this.trackedWorld = this.mc.field_1687;
                this.trackedSpawners.clear();
                this.alertedSpawners.clear();
                this.scanDelay = 0;
            }
            if ((var10000 = this.scanDelay++) <= 0 || this.scanDelay >= 40) {
                this.scanDelay = 0;
                this.scanLoadedSpawners();
            }
        } else {
            this.reset();
        }
    }

    private void scanLoadedSpawners() {
        double var1 = this.range.getValue() * this.range.getValue();
        HashMap<class_2338, String> var3 = new HashMap<class_2338, String>();
        HashSet<Long> var4 = new HashSet<Long>();
        for (class_2818 var6 : BlockUtil.getLoadedChunksList()) {
            for (class_2338 var8 : var6.method_12021()) {
                class_2586 var9 = this.mc.field_1687.method_8321(var8);
                if (!(var9 instanceof class_2636)) continue;
                class_2636 var10 = (class_2636)var9;
                if (var9.method_11015() || var9.method_10997() != this.mc.field_1687 || this.mc.field_1724.method_5707(class_243.method_24953((class_2382)var8)) > var1) continue;
                class_2338 var11 = var8.method_10062();
                String var12 = this.readSpawnerType(var10);
                var3.put(var11, var12);
                var4.add(var11.method_10063());
                if (!this.alertedSpawners.add(var11.method_10063())) continue;
                this.alert(var12, var11);
            }
        }
        this.alertedSpawners.removeIf(var1x -> !var4.contains(var1x));
        this.trackedSpawners.clear();
        this.trackedSpawners.putAll(var3);
    }

    private String readSpawnerType(class_2636 var1) {
        try {
            class_1299 var4;
            String var2 = ((MobSpawnerLogicAccessor)var1.method_11390()).getSpawnEntry().method_38093().method_10558("id").orElse("");
            class_2960 var3 = class_2960.method_12829((String)var2);
            if (var3 != null && (var4 = (class_1299)class_7923.field_41177.method_63535(var3)) != null) {
                return var4.method_5897().getString();
            }
            if (!var2.isBlank()) {
                int var7 = var2.indexOf(58);
                String var5 = var7 >= 0 ? var2.substring(var7 + 1) : var2;
                return Character.toUpperCase(var5.charAt(0)) + var5.substring(1).replace('_', ' ');
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
        return "Unknown";
    }

    private void alert(String var1, class_2338 var2) {
        if (this.soundAlerts.getValue()) {
            this.mc.field_1724.method_5783((class_3414)class_3417.field_14793.comp_349(), 1.0f, 0.8f);
        }
        if (this.chatAlerts.getValue()) {
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7c[Spawner Notify] \u00a7f" + var1 + " Spawner at X: " + var2.method_10263() + " Y: " + var2.method_10264() + " Z: " + var2.method_10260())), false);
        }
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null || this.trackedSpawners.isEmpty()) {
            return;
        }
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null || !camera.method_19332()) {
            return;
        }
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        RenderUtils.applyCameraTransform(matrices, camera);
        this.renderBoxes.clear();
        double bottomY = this.mc.field_1687.method_31607();
        double topY = (double)this.mc.field_1687.method_31600() + 1.0;
        Color pillarColor = new Color(255, 0, 0, this.pillarAlpha.getIntValue());
        Color boxColor = new Color(255, 0, 0, this.boxAlpha.getIntValue());
        for (class_2338 pos : this.trackedSpawners.keySet()) {
            double x = pos.method_10263();
            double y = pos.method_10264();
            double z = pos.method_10260();
            if (this.pillar.getValue() && this.pillarAlpha.getIntValue() > 0 && RenderVisibility.box(x - 1.0, bottomY, z - 1.0, x + 2.0, topY, z + 2.0)) {
                this.renderBoxes.add(new RenderUtils.ColoredBox(x - 1.0, bottomY, z - 1.0, x + 2.0, topY, z + 2.0, pillarColor));
            }
            if (!this.spawnerBox.getValue() || this.boxAlpha.getIntValue() <= 0 || !RenderVisibility.box(x, y, z, x + 1.0, y + 1.0, z + 1.0)) continue;
            this.renderBoxes.add(new RenderUtils.ColoredBox(x, y, z, x + 1.0, y + 1.0, z + 1.0, boxColor));
        }
        RenderUtils.renderFilledBoxes(matrices, this.renderBoxes);
        matrices.method_22909();
    }
}

