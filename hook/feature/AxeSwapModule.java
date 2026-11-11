/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1743
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1893
 *  net.minecraft.class_3483
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.AttackEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EnchantmentUtil;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1893;
import net.minecraft.class_3483;

public final class AxeSwapModule
extends Module {
    private final BooleanSetting shieldBreak = new BooleanSetting(EncryptedString.of("Shield Break"), true);
    private final BooleanSetting maceSwap = new BooleanSetting(EncryptedString.of("Mace Swap"), true);
    private final BooleanSetting useSmite = new BooleanSetting(EncryptedString.of("Smite"), true);
    private final BooleanSetting useDensity = new BooleanSetting(EncryptedString.of("Density"), true);
    private final BooleanSetting useBreach = new BooleanSetting(EncryptedString.of("Breach"), true);
    private final BooleanSetting onlyPlayers = new BooleanSetting(EncryptedString.of("Only Players"), false);
    private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true);
    private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 1.0, 1.0);
    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 1.0, 6.0, 3.0, 0.1);
    private boolean isSwitching;
    private int previousSlot;
    private int currentSwitchDelay;

    public AxeSwapModule() {
        super(EncryptedString.of("Axe Swap"), EncryptedString.of("Swaps to an axe for shields and a mace for hits."), -1, Category.COMBAT);
        this.addSettings(this.shieldBreak, this.maceSwap, this.useSmite, this.useDensity, this.useBreach, this.onlyPlayers, this.switchBack, this.switchDelay, this.range);
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
    public void onAttack(AttackEvent event) {
        int maceSlot;
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
        class_1799 held = this.mc.field_1724.method_6047();
        if (this.shieldBreak.getValue() && this.isBlockingWithShield(target)) {
            int axeSlot;
            if (!(held.method_7909() instanceof class_1743) && (axeSlot = this.findAxeSlot()) != -1) {
                this.beginSwitch(axeSlot);
            }
            return;
        }
        if (this.maceSwap.getValue() && held.method_7909() instanceof class_1743 && (maceSlot = this.findMaceSlot(target)) != -1) {
            this.beginSwitch(maceSlot);
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1755 != null || !this.isSwitching) {
            return;
        }
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

    private boolean isBlockingWithShield(class_1309 target) {
        return target.method_6039() && target.method_6030().method_31574(class_1802.field_8255);
    }

    private int findAxeSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            if (!(this.mc.field_1724.method_31548().method_5438(slot).method_7909() instanceof class_1743)) continue;
            return slot;
        }
        return -1;
    }

    private int findMaceSlot(class_1309 target) {
        int bestSlot = -1;
        int bestScore = -1;
        boolean falling = this.mc.field_1724.method_18798().field_1351 < -0.45;
        boolean undead = target.method_5864().method_20210(class_3483.field_46232);
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(slot);
            if (stack.method_7960() || stack.method_7909() != class_1802.field_49814) continue;
            int score = 0;
            if (this.useSmite.getValue() && undead && EnchantmentUtil.hasEnchantment(stack, class_1893.field_9123)) {
                score += 100;
            }
            if (this.useDensity.getValue() && falling && EnchantmentUtil.hasEnchantment(stack, class_1893.field_50157)) {
                score += 50;
            }
            if (this.useBreach.getValue() && EnchantmentUtil.hasEnchantment(stack, class_1893.field_50158)) {
                score += 25;
            }
            if (score <= bestScore) continue;
            bestScore = score;
            bestSlot = slot;
        }
        return bestSlot;
    }

    private void beginSwitch(int slot) {
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

    private void resetState() {
        this.previousSlot = -1;
        this.currentSwitchDelay = 0;
        this.isSwitching = false;
    }
}

