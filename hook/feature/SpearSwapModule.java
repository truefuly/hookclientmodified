/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1893
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.AttackEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.BindSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EnchantmentUtil;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import hook.utils.KeyUtils;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1893;

public final class SpearSwapModule
extends Module {
    private final BooleanSetting onlyLunge = new BooleanSetting(EncryptedString.of("Only Lunge"), true);
    private final BooleanSetting onlyPlayers = new BooleanSetting(EncryptedString.of("Only Players"), false);
    private final BindSetting manualSwapKey = new BindSetting(EncryptedString.of("Manual Swap Key"), -1, false).setDescription(EncryptedString.of("Press once to swap to your hotbar spear."));
    private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true);
    private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 2.0, 1.0);
    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 1.0, 6.0, 3.2, 0.1);
    private boolean isSwitching;
    private int previousSlot;
    private int currentSwitchDelay;
    private boolean manualKeyWasDown;

    public SpearSwapModule() {
        super(EncryptedString.of("Spear Swap"), EncryptedString.of("Swaps to a lunge spear when attacking."), -1, Category.COMBAT);
        this.addSettings(this.onlyLunge, this.onlyPlayers, this.manualSwapKey, this.switchBack, this.switchDelay, this.range);
    }

    @Override
    public void onEnable() {
        this.resetState();
        this.manualKeyWasDown = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.resetState();
        this.manualKeyWasDown = false;
        super.onDisable();
    }

    @EventListener
    public void onAttack(AttackEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1755 != null) {
            return;
        }
        class_1297 aimed = this.mc.field_1692;
        if (!(aimed instanceof class_1309)) {
            return;
        }
        class_1309 target = (class_1309)aimed;
        if (!target.method_5805() || target == this.mc.field_1724) {
            return;
        }
        if (this.onlyPlayers.getValue() && !(target instanceof class_1657)) {
            return;
        }
        if ((double)this.mc.field_1724.method_5739((class_1297)target) > this.range.getValue()) {
            return;
        }
        this.swapToSpear();
    }

    private void swapToSpear() {
        if (this.mc.field_1724 == null || this.isSwitching) return;
        int slot = this.findSpearSlot();
        if (slot == -1) {
            return;
        }
        int selected = this.mc.field_1724.method_31548().method_67532();
        if (slot == selected) {
            return;
        }
        if (!this.isSwitching) {
            this.previousSlot = selected;
        }
        InventoryUtil.swap(slot);
        this.isSwitching = true;
        this.currentSwitchDelay = 0;
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1755 != null) {
            return;
        }
        int key = this.manualSwapKey.getValue();
        boolean keyDown = key >= 0 && KeyUtils.isKeyPressed(key);
        boolean keyPressed = keyDown && !this.manualKeyWasDown;
        this.manualKeyWasDown = keyDown;
        if (keyPressed && !this.isSwitching) {
            this.swapToSpear();
            return;
        }
        if (!this.isSwitching) return;
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

    private int findSpearSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(slot);
            if (stack.method_7960() || !this.isSpear(stack.method_7909()) || this.onlyLunge.getValue() && !EnchantmentUtil.hasEnchantment(stack, class_1893.field_63420)) continue;
            return slot;
        }
        return -1;
    }

    private boolean isSpear(class_1792 item) {
        if (item == class_1802.field_63384 || item == class_1802.field_63385 || item == class_1802.field_63386 || item == class_1802.field_63387 || item == class_1802.field_63388 || item == class_1802.field_63389 || item == class_1802.field_63390) {
            return true;
        }
        return String.valueOf(item).toLowerCase().contains("spear");
    }

    private void resetState() {
        this.previousSlot = -1;
        this.currentSwitchDelay = 0;
        this.isSwitching = false;
    }
}

