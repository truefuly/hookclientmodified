/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1713
 *  net.minecraft.class_1723
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_437
 *  net.minecraft.class_490
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.event.events.TotemPopEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.FakeInvScreen;
import java.util.Random;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1723;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_437;
import net.minecraft.class_490;

public final class AutoInvTotemModule
extends Module {
    private static final int OFFHAND_BUTTON = 40;
    private final ModeSetting<TotemMode> equipMode = new ModeSetting<TotemMode>(EncryptedString.of("Equip Mode"), TotemMode.Always, TotemMode.class).setDescription(EncryptedString.of("Always keeps totems equipped, or only refills after your totem pops"));
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 20.0, 2.0, 1.0).getValue(EncryptedString.of("Ticks to wait before each inventory click"));
    private final NumberSetting randomDelay = new NumberSetting(EncryptedString.of("Random Delay"), 0.0, 10.0, 2.0, 1.0).getValue(EncryptedString.of("Adds up to this many extra ticks to each click so the timing varies"));
    private final BooleanSetting hotbar = new BooleanSetting(EncryptedString.of("Hotbar"), true).setDescription(EncryptedString.of("Also keeps a totem in your Totem Slot"));
    private final NumberSetting totemSlot = new NumberSetting(EncryptedString.of("Totem Slot"), 1.0, 9.0, 1.0, 1.0).getValue(EncryptedString.of("Your preferred totem slot"));
    private final BooleanSetting autoSwitch = new BooleanSetting(EncryptedString.of("Auto Switch"), false).setDescription(EncryptedString.of("Selects the totem slot after the inventory closes"));
    private final BooleanSetting forceTotem = new BooleanSetting(EncryptedString.of("Force Totem"), false).setDescription(EncryptedString.of("Puts the totem in the slot, regardless if its space is taken up by something else"));
    private final BooleanSetting autoOpen = new BooleanSetting(EncryptedString.of("Auto Open"), false).setDescription(EncryptedString.of("Automatically opens and closes the inventory for you"));
    private final NumberSetting stayOpenDuration = new NumberSetting(EncryptedString.of("Stay Open For"), 0.0, 20.0, 0.0, 1.0);
    private final Random random = new Random();
    private int clickCooldown = -1;
    private int stayOpenCounter = -1;
    private boolean pendingPop;
    private boolean openedByModule;
    private boolean switchAfterClose;

    public AutoInvTotemModule() {
        super(EncryptedString.of("Auto Inv Totem"), EncryptedString.of("Automatically equips a totem in your offhand and main hand if empty"), -1, Category.COMBAT);
        this.addSettings(this.equipMode, this.delay, this.randomDelay, this.hotbar, this.totemSlot, this.autoSwitch, this.forceTotem, this.autoOpen, this.stayOpenDuration);
    }

    @Override
    public void onEnable() {
        this.clickCooldown = -1;
        this.stayOpenCounter = -1;
        this.pendingPop = false;
        this.openedByModule = false;
        this.switchAfterClose = false;
        super.onEnable();
    }

    @EventListener
    public void onTotemPop(TotemPopEvent var1) {
        this.pendingPop = true;
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1724 != null && this.mc.field_1761 != null) {
            boolean var4;
            boolean var2 = this.mc.field_1755 instanceof class_490;
            if (!var2) {
                this.clickCooldown = -1;
                this.stayOpenCounter = -1;
                this.openedByModule = false;
                if (this.mc.field_1755 == null && this.switchAfterClose) {
                    this.switchAfterClose = false;
                    this.selectTotemSlot();
                }
            }
            boolean var3 = this.hasWork();
            if (this.pendingPop && !var3) {
                this.pendingPop = false;
            }
            boolean bl = var4 = this.equipMode.isMode(TotemMode.Always) || this.pendingPop;
            if (!var2) {
                if (var4 && var3 && this.autoOpen.getValue() && this.mc.field_1755 == null) {
                    this.mc.method_1507((class_437)new FakeInvScreen((class_1657)this.mc.field_1724));
                    this.openedByModule = true;
                    this.scheduleClick(true);
                }
            } else {
                if (this.clickCooldown < 0) {
                    this.scheduleClick(true);
                }
                if (var4 && var3) {
                    this.stayOpenCounter = -1;
                    if (this.clickCooldown > 0) {
                        --this.clickCooldown;
                    } else if (!this.mc.field_1724.method_5624() && !this.mc.field_1724.method_6115() && this.clickOnce()) {
                        this.switchAfterClose = this.autoSwitch.getValue();
                        this.scheduleClick(false);
                    }
                } else if (this.openedByModule && this.mc.field_1755 instanceof FakeInvScreen) {
                    if (this.stayOpenCounter < 0) {
                        this.stayOpenCounter = this.stayOpenDuration.getIntValue();
                    }
                    if (this.stayOpenCounter > 0) {
                        --this.stayOpenCounter;
                        return;
                    }
                    this.openedByModule = false;
                    this.mc.field_1755.method_25419();
                }
            }
        }
    }

    private void scheduleClick(boolean var1) {
        int var2 = this.randomDelay.getIntValue() > 0 ? this.random.nextInt(this.randomDelay.getIntValue() + 1) : 0;
        int var3 = this.delay.getIntValue() + var2;
        this.clickCooldown = var1 ? Math.max(1, var3) : var3;
    }

    private boolean clickOnce() {
        int var4;
        int var3;
        int var1 = ((class_1723)((class_490)this.mc.field_1755).method_17577()).field_7763;
        int var2 = this.totemSlot.getIntValue() - 1;
        if (this.needsOffhand() && (var3 = this.findTotem(this.hotbar.getValue() ? var2 : -1, true)) != -1) {
            this.mc.field_1761.method_2906(var1, AutoInvTotemModule.toHandlerSlot(var3), 40, class_1713.field_7791, (class_1657)this.mc.field_1724);
            return true;
        }
        if (this.needsHotbar() && (var4 = this.findTotem(var2, false)) != -1) {
            this.mc.field_1761.method_2906(var1, AutoInvTotemModule.toHandlerSlot(var4), var2, class_1713.field_7791, (class_1657)this.mc.field_1724);
            return true;
        }
        return false;
    }

    private boolean hasWork() {
        int var1 = this.totemSlot.getIntValue() - 1;
        return this.needsOffhand() && this.findTotem(this.hotbar.getValue() ? var1 : -1, true) != -1 || this.needsHotbar() && this.findTotem(var1, false) != -1;
    }

    private boolean needsOffhand() {
        return this.mc.field_1724.method_6079().method_7909() != class_1802.field_8288;
    }

    private boolean needsHotbar() {
        if (!this.hotbar.getValue()) {
            return false;
        }
        class_1799 var1 = this.mc.field_1724.method_31548().method_5438(this.totemSlot.getIntValue() - 1);
        return var1.method_7909() != class_1802.field_8288 && (var1.method_7960() || this.forceTotem.getValue());
    }

    private int findTotem(int var1, boolean var2) {
        class_1661 var3 = this.mc.field_1724.method_31548();
        for (int var4 = 9; var4 < 36; ++var4) {
            if (var3.method_5438(var4).method_7909() != class_1802.field_8288) continue;
            return var4;
        }
        for (int var5 = 0; var5 < 9; ++var5) {
            if (var5 == var1 || var3.method_5438(var5).method_7909() != class_1802.field_8288) continue;
            return var5;
        }
        return var2 && var1 >= 0 && var3.method_5438(var1).method_7909() == class_1802.field_8288 ? var1 : -1;
    }

    private static int toHandlerSlot(int var0) {
        return var0 < 9 ? 36 + var0 : var0;
    }

    private void selectTotemSlot() {
        int var1 = this.totemSlot.getIntValue() - 1;
        class_1661 var2 = this.mc.field_1724.method_31548();
        if (var2.method_5438(var1).method_7909() == class_1802.field_8288) {
            var2.method_61496(var1);
        }
    }

    public static enum TotemMode {
        Always,
        OnPop;

    }
}

