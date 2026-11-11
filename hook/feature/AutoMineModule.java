/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_2338
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_3965
 */
package hook.feature;

import hook.Hook;
import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.feature.AutoEatModule;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_3965;

public final class AutoMineModule
extends Module {
    private final BooleanSetting lockView = new BooleanSetting(EncryptedString.of("Lock View"), true).setDescription(EncryptedString.of("Keeps your camera at the yaw and pitch below while mining"));
    private final NumberSetting pitch = new NumberSetting(EncryptedString.of("Pitch"), -90.0, 90.0, 0.0, 0.5).getValue(EncryptedString.of("Up/down angle to lock to (-90 up, 90 down)"));
    private final NumberSetting yaw = new NumberSetting(EncryptedString.of("Yaw"), -180.0, 180.0, 0.0, 0.5).getValue(EncryptedString.of("Left/right angle to lock to"));

    public AutoMineModule() {
        super(EncryptedString.of("Auto Mine"), EncryptedString.of("Mines the block you are looking at automatically"), -1, Category.MISC);
        this.addSettings(this.lockView, this.pitch, this.yaw);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1755 != null || this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        AutoEatModule autoEat = (AutoEatModule)Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(AutoEatModule.class);
        if (autoEat.isEnabled() && autoEat.shouldEat()) {
            return;
        }
        this.processMiningAction();
        if (this.lockView.getValue()) {
            float currentYaw = this.mc.field_1724.method_36454();
            float currentPitch = this.mc.field_1724.method_36455();
            float targetYaw = this.yaw.getFloatValue();
            float targetPitch = this.pitch.getFloatValue();
            if (currentYaw - targetYaw != 0.0f || currentPitch - targetPitch != 0.0f) {
                this.mc.field_1724.method_36456(targetYaw);
                this.mc.field_1724.method_36457(targetPitch);
            }
        }
    }

    private void processMiningAction() {
        if (this.mc.field_1724.method_6115()) {
            return;
        }
        class_239 target = this.mc.field_1765;
        if (target != null && target.method_17783() == class_239.class_240.field_1332) {
            class_3965 blockTarget = (class_3965)target;
            class_2338 position = blockTarget.method_17777();
            if (!this.mc.field_1687.method_8320(position).method_26215() && this.mc.field_1761.method_2902(position, blockTarget.method_17780())) {
                this.mc.field_1724.method_6104(class_1268.field_5808);
            }
            return;
        }
        this.mc.field_1761.method_2925();
    }
}

