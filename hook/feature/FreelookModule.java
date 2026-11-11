/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3532
 *  net.minecraft.class_5498
 */
package hook.feature;

import hook.Hook;
import hook.event.EventListener;
import hook.event.events.MouseScrolledEvent;
import hook.event.events.TickEvent;
import hook.feature.FreecamModule;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.Setting;
import hook.utils.EncryptedString;
import net.minecraft.class_3532;
import net.minecraft.class_5498;

public final class FreelookModule
extends Module {
    private static final double CONSTANT_0281 = 0.15;
    private final NumberSetting distance = new NumberSetting(EncryptedString.of("Distance"), 1.0, 15.0, 4.0, 0.1);
    private final NumberSetting sensitivity = new NumberSetting(EncryptedString.of("Sensitivity"), 0.1, 3.0, 1.0, 0.1);
    private final NumberSetting zoomSensitivity = new NumberSetting(EncryptedString.of("Zoom Sensitivity"), 0.1, 3.0, 1.0, 0.1);
    private final BooleanSetting throughWalls = new BooleanSetting(EncryptedString.of("Through Walls"), true);
    private final BooleanSetting invertY = new BooleanSetting(EncryptedString.of("Invert Y"), false);
    private final BooleanSetting keepPlayerHeadStill = new BooleanSetting(EncryptedString.of("Keep Player Head Still"), true);
    private class_5498 savedPerspective;
    private boolean savedChunkCulling;
    private float cameraYaw;
    private float cameraPitch;

    public FreelookModule() {
        super(EncryptedString.of("Freelook"), EncryptedString.of("Rotates and zooms the third-person camera without changing player rotation"), -1, Category.VISUALS);
        Setting[] var10001 = new Setting[]{this.distance, this.sensitivity, this.zoomSensitivity, this.throughWalls, this.invertY, this.keepPlayerHeadStill};
        this.addSettings(var10001);
    }

    @Override
    public void onEnable() {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            FreecamModule var1 = (FreecamModule)Hook.INSTANCE.getModuleManager().getModuleByClass(FreecamModule.class);
            if (var1 != null && var1.isEnabled()) {
                var1.toggle(false);
            }
            this.savedPerspective = this.mc.field_1690.method_31044();
            this.savedChunkCulling = this.mc.field_1730;
            this.cameraYaw = this.mc.field_1724.method_36454();
            this.cameraPitch = this.mc.field_1724.method_36455();
            this.mc.field_1690.method_31043(class_5498.field_26665);
            this.mc.field_1730 = false;
            super.onEnable();
        } else {
            this.toggle(false);
        }
    }

    @Override
    public void onDisable() {
        if (this.savedPerspective != null) {
            this.mc.field_1690.method_31043(this.savedPerspective);
        }
        this.mc.field_1730 = this.savedChunkCulling;
        super.onDisable();
    }

    @EventListener
    private void onTick(TickEvent var1) {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            if (this.mc.field_1690.method_31044() != class_5498.field_26665) {
                this.mc.field_1690.method_31043(class_5498.field_26665);
            }
        } else {
            this.toggle(false);
        }
    }

    @EventListener
    private void onMouseScrolled(MouseScrolledEvent event) {
        if (this.mc.field_1755 != null || event.amount == 0.0) return;
        double delta = event.amount * 0.5 * this.zoomSensitivity.getValue();
        this.distance.getValue(class_3532.method_15350(this.distance.getValue() + delta, 1.0, 15.0));
        event.cancel();
    }

    public void consumeMouseDelta(double var1, double var3) {
        double var5 = 0.15 * this.sensitivity.getValue();
        this.cameraYaw = class_3532.method_15393((float)(this.cameraYaw + (float)(var1 * var5)));
        double var7 = var3 * var5 * (this.invertY.getValue() ? -1.0 : 1.0);
        this.cameraPitch = class_3532.method_15363((float)(this.cameraPitch + (float)var7), (float)-90.0f, (float)90.0f);
    }

    public float getCameraYaw() {
        return !this.keepPlayerHeadStill.getValue() && this.mc.field_1724 != null ? this.mc.field_1724.method_36454() : this.cameraYaw;
    }

    public float getCameraPitch() {
        return !this.keepPlayerHeadStill.getValue() && this.mc.field_1724 != null ? this.mc.field_1724.method_36455() : this.cameraPitch;
    }

    public boolean keepPlayerHeadStill() {
        return this.keepPlayerHeadStill.getValue();
    }

    public float getDistance() {
        return this.distance.getFloatValue();
    }

    public boolean isThroughWalls() {
        return this.throughWalls.getValue();
    }
}

