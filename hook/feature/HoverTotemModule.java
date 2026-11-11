/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1723
 *  net.minecraft.class_1735
 *  net.minecraft.class_1802
 *  net.minecraft.class_437
 *  net.minecraft.class_490
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.mixin.HandledScreenMixin;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1723;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_437;
import net.minecraft.class_490;

public final class HoverTotemModule
extends Module {
    private final NumberSetting tickDelay = new NumberSetting(EncryptedString.of("Tick Delay"), 0.0, 20.0, 0.0, 1.0).getValue(EncryptedString.of("Ticks to wait between operations"));
    private final BooleanSetting hotbarTotem = new BooleanSetting(EncryptedString.of("Hotbar Totem"), true).setDescription(EncryptedString.of("Also places a totem in your preferred hotbar slot"));
    private final NumberSetting hotbarSlot = new NumberSetting(EncryptedString.of("Hotbar Slot"), 1.0, 9.0, 1.0, 1.0).getValue(EncryptedString.of("Your preferred hotbar slot for totem (1-9)"));
    private final BooleanSetting autoSwitchToTotem = new BooleanSetting(EncryptedString.of("Auto Switch To Totem"), false).setDescription(EncryptedString.of("Automatically switches to totem slot when inventory is opened"));
    private int remainingDelay;

    public HoverTotemModule() {
        super(EncryptedString.of("Hover Totem"), EncryptedString.of("Equips a totem in offhand and optionally hotbar when hovering over one in inventory"), -1, Category.COMBAT);
        this.addSettings(this.tickDelay, this.hotbarTotem, this.hotbarSlot, this.autoSwitchToTotem);
    }

    @Override
    public void onEnable() {
        this.resetDelay();
        super.onEnable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null) {
            return;
        }
        class_437 screen = this.mc.field_1755;
        if (!(screen instanceof class_490)) {
            this.resetDelay();
            return;
        }
        class_490 inventoryScreen = (class_490)screen;
        class_1735 focused = ((HandledScreenMixin)screen).getFocusedSlot();
        if (focused == null || focused.method_34266() > 35) {
            return;
        }
        if (this.autoSwitchToTotem.getValue()) {
            this.mc.field_1724.method_31548().method_61496(this.hotbarSlot.getIntValue() - 1);
        }
        if (focused.method_7677().method_7909() != class_1802.field_8288) {
            return;
        }
        if (this.remainingDelay > 0) {
            --this.remainingDelay;
            return;
        }
        int hoveredSlot = focused.method_34266();
        int syncId = ((class_1723)inventoryScreen.method_17577()).field_7763;
        if (!this.mc.field_1724.method_6079().method_31574(class_1802.field_8288)) {
            this.equipOffhandTotem(syncId, hoveredSlot);
        } else if (this.hotbarTotem.getValue()) {
            int selectedHotbarSlot = this.hotbarSlot.getIntValue() - 1;
            if (!this.mc.field_1724.method_31548().method_5438(selectedHotbarSlot).method_31574(class_1802.field_8288)) {
                this.equipHotbarTotem(syncId, hoveredSlot, selectedHotbarSlot);
            }
        }
    }

    private void equipOffhandTotem(int syncId, int hoveredSlot) {
        this.mc.field_1761.method_2906(syncId, hoveredSlot, 40, class_1713.field_7791, (class_1657)this.mc.field_1724);
        this.resetDelay();
    }

    private void equipHotbarTotem(int syncId, int hoveredSlot, int hotbarSlot) {
        this.mc.field_1761.method_2906(syncId, hoveredSlot, hotbarSlot, class_1713.field_7791, (class_1657)this.mc.field_1724);
        this.resetDelay();
    }

    private void resetDelay() {
        this.remainingDelay = this.tickDelay.getIntValue();
    }
}

