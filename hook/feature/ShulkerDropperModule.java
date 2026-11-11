/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1802
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.Setting;
import hook.utils.EncryptedString;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1802;

public final class ShulkerDropperModule
extends Module {
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 20.0, 1.0, 1.0);
    private final NumberSetting clicksPerBuy = new NumberSetting(EncryptedString.of("Clicks Per Buy"), 1.0, 32.0, 8.0, 1.0);
    private final NumberSetting reopenDelay = new NumberSetting(EncryptedString.of("Reopen Delay"), 1.0, 60.0, 20.0, 1.0);
    private final BooleanSetting dropBought = new BooleanSetting(EncryptedString.of("Drop Bought"), true);
    private int delayCounter;

    public ShulkerDropperModule() {
        super(EncryptedString.of("Shulker Dropper"), EncryptedString.of("Automatically buys and drops shulker boxes from /shop"), -1, Category.MISC);
        Setting[] var10001 = new Setting[]{this.delay, this.clicksPerBuy, this.reopenDelay, this.dropBought};
        this.addSettings(var10001);
    }

    @Override
    public void onEnable() {
        this.delayCounter = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.delayCounter = 0;
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent var1) {
        int var10000;
        if (this.mc.field_1724 != null && this.mc.field_1761 != null && this.mc.method_1562() != null && (var10000 = this.delayCounter--) <= 0) {
            class_1703 class_17032 = this.mc.field_1724.field_7512;
            if (!(class_17032 instanceof class_1707)) {
                this.mc.method_1562().method_45730("shop");
                this.delayCounter = this.reopenDelay.getIntValue();
            } else {
                class_1707 var2 = (class_1707)class_17032;
                int var6 = var2.method_17388() * 9;
                int var4 = -1;
                for (int var5 = 0; var5 < var6; ++var5) {
                    if (!var2.method_7611(var5).method_7677().method_31574(class_1802.field_8545)) continue;
                    var4 = var5;
                    break;
                }
                if (var4 < 0) {
                    this.delayCounter = Math.max(1, this.delay.getIntValue());
                } else {
                    for (int var7 = 0; var7 < this.clicksPerBuy.getIntValue(); ++var7) {
                        this.mc.field_1761.method_2906(var2.field_7763, var4, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                    }
                    if (this.dropBought.getValue()) {
                        for (int var8 = var6; var8 < var2.field_7761.size(); ++var8) {
                            if (!var2.method_7611(var8).method_7677().method_31574(class_1802.field_8545)) continue;
                            this.mc.field_1761.method_2906(var2.field_7763, var8, 1, class_1713.field_7795, (class_1657)this.mc.field_1724);
                        }
                    }
                    this.delayCounter = Math.max(1, this.delay.getIntValue());
                }
            }
        }
    }
}

