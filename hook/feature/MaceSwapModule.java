/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1743
 *  net.minecraft.class_1792
 *  net.minecraft.class_1802
 *  net.minecraft.class_1893
 *  net.minecraft.class_3489
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
import net.minecraft.class_1743;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_1893;
import net.minecraft.class_3489;
import net.minecraft.class_1297;
import net.minecraft.class_1657;

public final class MaceSwapModule
extends Module {
    private final BooleanSetting enableWindBurst = new BooleanSetting(EncryptedString.of("Wind Burst"), true);
    private final BooleanSetting enableBreach = new BooleanSetting(EncryptedString.of("Breach"), true);
    private final BooleanSetting onlySword = new BooleanSetting(EncryptedString.of("Only Sword"), false);
    private final BooleanSetting onlyAxe = new BooleanSetting(EncryptedString.of("Only Axe"), false);
    private final BooleanSetting onlyIfPlayerCanBeHit = new BooleanSetting(EncryptedString.of("Only If Player Can Be Hit"), false);
    private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true);
    private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 0.0, 1.0);
    private boolean isSwitching;
    private int previousSlot;
    private int currentSwitchDelay;

    public MaceSwapModule() {
        super(EncryptedString.of("Mace Swap"), EncryptedString.of("Switches to a mace when attacking."), -1, Category.COMBAT);
        this.addSettings(this.enableWindBurst, this.enableBreach, this.onlySword, this.onlyAxe, this.onlyIfPlayerCanBeHit, this.switchBack, this.switchDelay);
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
        if (this.mc.field_1755 != null || this.mc.field_1724 == null || !this.isSwitching) {
            return;
        }
        if (this.switchBack.getValue()) {
            this.performSwitchBack();
        } else {
            this.resetState();
        }
    }

    @EventListener
    public void onAttack(AttackEvent event) {
        boolean useBreach;
        boolean useWindBurst;
        if (this.mc.field_1724 == null || !this.isValidWeapon()) {
            return;
        }
        if (this.onlyIfPlayerCanBeHit.getValue() && !this.canHitPlayer()) {
            return;
        }
        if (this.previousSlot == -1) {
            this.previousSlot = this.mc.field_1724.method_31548().method_67532();
        }
        if ((useWindBurst = this.enableWindBurst.getValue()) != (useBreach = this.enableBreach.getValue())) {
            if (this.enableWindBurst.getValue()) {
                InventoryUtil.swapStack(stack -> EnchantmentUtil.hasEnchantment(stack, class_1893.field_50159));
            }
            if (this.enableBreach.getValue()) {
                InventoryUtil.swapStack(stack -> EnchantmentUtil.hasEnchantment(stack, class_1893.field_50158));
            }
        } else {
            InventoryUtil.swap(class_1802.field_49814);
        }
        this.isSwitching = true;
    }

    private boolean canHitPlayer() {
        class_1297 target = this.mc.field_1692;
        return target instanceof class_1657 player
            && player != this.mc.field_1724
            && player.method_5805()
            && !player.method_68878()
            && this.mc.field_1724.method_5739(player) <= 4.5
            && this.mc.field_1724.method_6057(player);
    }

    private boolean isValidWeapon() {
        class_1792 item = this.mc.field_1724.method_6047().method_7909();
        if (this.onlySword.getValue() && this.onlyAxe.getValue()) {
            return item.method_40131().method_40220(class_3489.field_42611) || item instanceof class_1743;
        }
        if (this.onlySword.getValue() && !item.method_40131().method_40220(class_3489.field_42611)) {
            return false;
        }
        return !this.onlyAxe.getValue() || item instanceof class_1743;
    }

    private void performSwitchBack() {
        if (this.currentSwitchDelay < this.switchDelay.getIntValue()) {
            ++this.currentSwitchDelay;
        } else {
            InventoryUtil.swap(this.previousSlot);
            this.resetState();
        }
    }

    private void resetState() {
        this.previousSlot = -1;
        this.currentSwitchDelay = 0;
        this.isSwitching = false;
    }
}

