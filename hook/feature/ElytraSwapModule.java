/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1304
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1792
 *  net.minecraft.class_1802
 *  net.minecraft.class_9334
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BindSetting;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import hook.utils.KeyUtils;
import java.util.function.Predicate;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_9334;

public final class ElytraSwapModule
extends Module {
    private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), 71, false);
    private final NumberSetting swapDelay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 20.0, 0.0, 1.0);
    private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true);
    private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 0.0, 1.0);
    private final BooleanSetting moveToSlot = new BooleanSetting(EncryptedString.of("Move to slot"), true).setDescription("If elytra is not in hotbar it will move it from inventory to preferred slot");
    private final NumberSetting elytraSlot = new NumberSetting(EncryptedString.of("Elytra Slot"), 1.0, 9.0, 9.0, 1.0).getValue(EncryptedString.of("Your preferred elytra slot"));
    private boolean isSwapping;
    private boolean isSwinging;
    private boolean isItemSwapped;
    private int swapCounter;
    private int switchCounter;
    private int activationCooldown;
    private int originalSlot;

    public ElytraSwapModule() {
        super(EncryptedString.of("Elytra Swap"), EncryptedString.of("Seamlessly swap between an Elytra and a Chestplate with a configurable keybinding"), -1, Category.COMBAT);
        this.addSettings(this.activateKey, this.swapDelay, this.switchBack, this.switchDelay, this.moveToSlot, this.elytraSlot);
    }

    @Override
    public void onEnable() {
        this.resetState();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        Predicate<class_1792> targetItem;
        if (this.mc.field_1755 != null || this.mc.field_1724 == null) {
            return;
        }
        if (this.activationCooldown > 0) {
            --this.activationCooldown;
        } else if (KeyUtils.isKeyPressed(this.activateKey.getValue())) {
            this.isSwapping = true;
            this.activationCooldown = 4;
        }
        if (!this.isSwapping) {
            return;
        }
        if (this.originalSlot == -1) {
            this.originalSlot = this.mc.field_1724.method_31548().method_67532();
        }
        if (this.swapCounter < this.swapDelay.getIntValue()) {
            ++this.swapCounter;
            return;
        }
        boolean wearingElytra = this.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833);
        Predicate<class_1792> predicate = targetItem = wearingElytra ? item -> item != class_1802.field_8833 && item.method_57347().method_57832(class_9334.field_54196) : item -> item.equals(class_1802.field_8833);
        if (!this.isItemSwapped) {
            if (!InventoryUtil.swapItem(targetItem)) {
                if (!this.moveToSlot.getValue()) {
                    this.resetState();
                    return;
                }
                this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 9, this.elytraSlot.getIntValue() - 1, class_1713.field_7791, (class_1657)this.mc.field_1724);
                this.swapCounter = 0;
                return;
            }
            this.isItemSwapped = true;
        }
        if (!this.isSwinging) {
            this.mc.field_1761.method_2919((class_1657)this.mc.field_1724, class_1268.field_5808);
            this.mc.field_1724.method_6104(class_1268.field_5808);
            this.isSwinging = true;
        }
        if (this.switchBack.getValue()) {
            this.handleSwitchBack();
        } else {
            this.resetState();
        }
    }

    private void handleSwitchBack() {
        if (this.switchCounter < this.switchDelay.getIntValue()) {
            ++this.switchCounter;
        } else {
            InventoryUtil.swap(this.originalSlot);
            this.resetState();
        }
    }

    private void resetState() {
        this.originalSlot = -1;
        this.switchCounter = 0;
        this.swapCounter = 0;
        this.isSwapping = false;
        this.isSwinging = false;
        this.isItemSwapped = false;
    }
}

