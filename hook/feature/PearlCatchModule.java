/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1657
 *  net.minecraft.class_1802
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.PreItemUseEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public final class PearlCatchModule
extends Module {
    private final NumberSetting range;
    private final NumberSetting prediction;
    private final NumberSetting minPitch;
    private final BooleanSetting switchBack;
    private final NumberSetting switchDelay;
    private State state = State.IDLE;
    private class_243 spawnPos;
    private class_243 initialVelocity;
    private int windSlot = -1;
    private int previousSlot = -1;
    private int currentSwitchDelay;

    public PearlCatchModule() {
        super(EncryptedString.of("Pearl Catch"), EncryptedString.of("Redirects thrown pearls with a wind charge."), -1, Category.COMBAT);
        this.range = new NumberSetting(EncryptedString.of("Catch Range"), 1.0, 8.0, 3.1, 0.1);
        this.prediction = new NumberSetting(EncryptedString.of("Prediction"), 1.0, 10.0, 5.0, 1.0);
        this.minPitch = new NumberSetting(EncryptedString.of("Min Pitch"), -90.0, 0.0, -25.0, 1.0);
        this.switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true);
        this.switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 2.0, 1.0);
        this.addSettings(this.range, this.prediction, this.minPitch, this.switchBack, this.switchDelay);
    }

    @Override
    public void onEnable() {
        this.resetState();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.resetState();
        super.onDisable();
    }

    @EventListener
    public void onPreItemUse(PreItemUseEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1755 != null) {
            return;
        }
        if (this.mc.field_1724.method_6047().method_7909() != class_1802.field_8634) {
            return;
        }
        if (this.mc.field_1724.method_36455() > this.minPitch.getFloatValue()) {
            return;
        }
        this.spawnPos = this.mc.field_1724.method_33571();
        this.initialVelocity = this.mc.field_1724.method_5828(1.0f).method_1021(1.5).method_1019(this.mc.field_1724.method_18798());
        this.state = State.ROTATE;
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1755 != null) {
            return;
        }
        switch (this.state.ordinal()) {
            case 1: {
                this.handleRotate();
                break;
            }
            case 2: {
                this.handleSwap();
                break;
            }
            case 3: {
                this.handleFire();
                break;
            }
            case 4: {
                this.handleRestore();
                break;
            }
        }
    }

    private void handleRotate() {
        class_243 catchPos = this.findCatchPosition();
        if (catchPos == null) {
            this.resetState();
            return;
        }
        int slot = this.findWindChargeSlot();
        if (slot == -1) {
            this.resetState();
            return;
        }
        this.windSlot = slot;
        this.previousSlot = this.mc.field_1724.method_31548().method_67532();
        this.rotateToward(catchPos);
        this.state = State.SWAP;
    }

    private void handleSwap() {
        InventoryUtil.swap(this.windSlot);
        this.state = State.FIRE;
    }

    private void handleFire() {
        this.mc.field_1761.method_2919((class_1657)this.mc.field_1724, class_1268.field_5808);
        this.currentSwitchDelay = 0;
        this.state = State.RESTORE;
    }

    private void handleRestore() {
        if (!this.switchBack.getValue()) {
            this.resetState();
            return;
        }
        if (this.currentSwitchDelay < this.switchDelay.getIntValue()) {
            ++this.currentSwitchDelay;
            return;
        }
        InventoryUtil.swap(this.previousSlot);
        this.resetState();
    }

    private class_243 findCatchPosition() {
        if (this.spawnPos == null || this.initialVelocity == null) {
            return null;
        }
        int maxTicks = this.prediction.getIntValue();
        for (int ticks = 1; ticks <= maxTicks; ++ticks) {
            class_243 predicted = this.simulatePearl(this.spawnPos, this.initialVelocity, ticks);
            if (!(this.mc.field_1724.method_33571().method_1022(predicted) <= this.range.getValue())) continue;
            return predicted;
        }
        return null;
    }

    private class_243 simulatePearl(class_243 position, class_243 velocity, int ticks) {
        class_243 pos = position;
        class_243 vel = velocity;
        for (int i = 0; i < ticks; ++i) {
            pos = pos.method_1019(vel);
            vel = vel.method_1031(0.0, -0.03, 0.0).method_1021(0.99);
        }
        return pos;
    }

    private int findWindChargeSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            if (this.mc.field_1724.method_31548().method_5438(slot).method_7909() != class_1802.field_49098) continue;
            return slot;
        }
        return -1;
    }

    private void rotateToward(class_243 target) {
        class_243 eye = this.mc.field_1724.method_33571();
        class_243 biased = target.method_1031(0.0, 0.05, 0.0);
        class_243 direction = biased.method_1020(eye).method_1029();
        float yaw = (float)Math.toDegrees(Math.atan2(-direction.field_1352, direction.field_1350));
        float pitch = (float)Math.toDegrees(-Math.asin(direction.field_1351));
        this.mc.field_1724.method_36456(yaw);
        this.mc.field_1724.method_36457(class_3532.method_15363((float)pitch, (float)-90.0f, (float)90.0f));
    }

    private void resetState() {
        this.state = State.IDLE;
        this.spawnPos = null;
        this.initialVelocity = null;
        this.windSlot = -1;
        this.previousSlot = -1;
        this.currentSwitchDelay = 0;
    }

    private static enum State {
        IDLE,
        ROTATE,
        SWAP,
        FIRE,
        RESTORE;

    }
}

