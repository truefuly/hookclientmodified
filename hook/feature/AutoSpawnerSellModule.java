/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1792
 *  net.minecraft.class_1792$class_9635
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1836
 *  net.minecraft.class_1937
 *  net.minecraft.class_2561
 *  net.minecraft.class_304
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_3675$class_307
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1836;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_3675;

public final class AutoSpawnerSellModule
extends Module {
    private final NumberSetting dropDelay = new NumberSetting(EncryptedString.of("Drop Delay"), 0.0, 120.0, 30.0, 1.0).getValue(EncryptedString.of("How often it should start dropping bones in minutes"));
    private final NumberSetting pageAmount = new NumberSetting(EncryptedString.of("Page Amount"), 1.0, 10.0, 2.0, 1.0).getValue(EncryptedString.of("How many pages should it drop before selling"));
    private final NumberSetting pageSwitchDelay = new NumberSetting(EncryptedString.of("Page Switch Delay"), 0.0, 720.0, 4.0, 1.0).getValue(EncryptedString.of("How often it should switch pages in seconds"));
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("delay"), 0.0, 20.0, 1.0, 1.0).getValue(EncryptedString.of("What should be delay in ticks"));
    private int delayCounter;
    private int pageCounter;
    private boolean isProcessing;
    private boolean isSelling;
    private boolean isPageSwitching;

    public AutoSpawnerSellModule() {
        super(EncryptedString.of("Auto Spawner Sell"), EncryptedString.of("Automatically drops bones from spawner and sells them"), -1, Category.MISC);
        this.addSettings(this.dropDelay, this.pageAmount, this.pageSwitchDelay, this.delay);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.delayCounter = 20;
        this.isProcessing = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.delayCounter > 0) {
            --this.delayCounter;
        } else if (this.mc.field_1724 != null) {
            if (this.pageCounter >= this.pageAmount.getIntValue()) {
                this.isSelling = true;
                this.pageCounter = 0;
                this.delayCounter = 40;
            } else if (!this.isSelling) {
                class_1703 var7 = this.mc.field_1724.field_7512;
                if (!(this.mc.field_1724.field_7512 instanceof class_1707)) {
                    class_304.method_1420((class_3675.class_306)class_3675.class_307.field_1672.method_1447(1));
                    this.delayCounter = 20;
                    return;
                }
                if (var7.method_7611(15).method_7677().method_31574(class_1802.field_8581)) {
                    this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 15, 1, class_1713.field_7794, (class_1657)this.mc.field_1724);
                    this.delayCounter = 10;
                    return;
                }
                if (this.mc.field_1724.field_7512.method_7611(13).method_7677().method_31574(class_1802.field_8398)) {
                    this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 11, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                    this.delayCounter = 20;
                    return;
                }
                if (!this.mc.field_1724.field_7512.method_7611(53).method_7677().method_31574(class_1802.field_8695)) {
                    this.mc.field_1724.method_7346();
                    this.delayCounter = 20;
                    return;
                }
                if (this.mc.field_1724.field_7512.method_7611(48).method_7677().method_31574(class_1802.field_8107)) {
                    this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 48, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                    this.delayCounter = 20;
                    return;
                }
                boolean var9 = true;
                for (int var12 = 0; var12 < 45; ++var12) {
                    if (this.mc.field_1724.field_7512.method_7611(var12).method_7677().method_31574(class_1802.field_8606)) continue;
                    var9 = false;
                    break;
                }
                if (var9) {
                    this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 52, 1, class_1713.field_7795, (class_1657)this.mc.field_1724);
                    this.isProcessing = true;
                    this.delayCounter = this.pageSwitchDelay.getIntValue() * 20;
                    ++this.pageCounter;
                } else if (this.isProcessing) {
                    this.isProcessing = false;
                    this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 50, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                    this.delayCounter = 20;
                } else {
                    this.isProcessing = false;
                    if (this.pageCounter != 0) {
                        this.pageCounter = 0;
                        this.isSelling = true;
                        this.delayCounter = 40;
                        return;
                    }
                    this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 45, 1, class_1713.field_7795, (class_1657)this.mc.field_1724);
                    this.delayCounter = 1200 * this.dropDelay.getIntValue();
                }
            } else {
                class_1703 var2 = this.mc.field_1724.field_7512;
                if (!(this.mc.field_1724.field_7512 instanceof class_1707)) {
                    this.mc.method_1562().method_45730("order " + this.getOrderCommand());
                    this.delayCounter = 20;
                    return;
                }
                if (((class_1707)var2).method_17388() != 6) {
                    if (((class_1707)var2).method_17388() == 4) {
                        int var8 = InventoryUtil.getSlot(class_1802.field_8162);
                        if (var8 <= 0) {
                            this.mc.field_1724.method_7346();
                            this.delayCounter = 10;
                            return;
                        }
                        if (this.isPageSwitching && var8 == 36) {
                            this.isPageSwitching = false;
                            this.mc.field_1724.method_7346();
                            return;
                        }
                        class_1792 var11 = this.getInventoryItem();
                        while (true) {
                            int var13 = 36;
                            class_1792 var14 = ((class_1799)this.mc.field_1724.field_7512.method_7602().get(36)).method_7909();
                            if (var14 == class_1802.field_8162 || var14 != var11) continue;
                            this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 36, 1, class_1713.field_7794, (class_1657)this.mc.field_1724);
                            this.delayCounter = this.delay.getIntValue();
                            if (this.delay.getIntValue() != 0) break;
                        }
                    } else if (((class_1707)var2).method_17388() == 3) {
                        this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 15, 1, class_1713.field_7794, (class_1657)this.mc.field_1724);
                        this.isPageSwitching = true;
                        this.delayCounter = 10;
                    }
                } else {
                    class_1799 var3 = var2.method_7611(47).method_7677();
                    if (var3.method_31574(class_1802.field_8162)) {
                        this.delayCounter = 2;
                        this.mc.field_1724.method_7346();
                        return;
                    }
                    for (Object var5 : var3.method_7950(class_1792.class_9635.method_59528((class_1937)this.mc.field_1687), (class_1657)this.mc.field_1724, (class_1836)class_1836.field_41070)) {
                        String var6 = var5.toString();
                        if (!var6.contains("Most Money Per Item") || !((class_2561)var5).method_10866().toString().contains("white") && !var6.contains("white")) continue;
                        this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 47, 1, class_1713.field_7794, (class_1657)this.mc.field_1724);
                        this.delayCounter = 5;
                        return;
                    }
                    for (int var10 = 0; var10 < 44; ++var10) {
                        if (!var2.method_7611(var10).method_7677().method_31574(this.getInventoryItem())) continue;
                        this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, var10, 1, class_1713.field_7794, (class_1657)this.mc.field_1724);
                        this.delayCounter = 10;
                        return;
                    }
                    this.delayCounter = 40;
                    this.mc.field_1724.method_7346();
                }
            }
        }
    }

    private class_1792 getInventoryItem() {
        for (int var1 = 0; var1 < 35; ++var1) {
            class_1799 var2 = this.mc.field_1724.method_31548().method_5438(var1);
            if (var2.method_31574(class_1802.field_8162)) continue;
            return var2.method_7909();
        }
        return class_1802.field_8162;
    }

    private String getOrderCommand() {
        class_1792 var1 = this.getInventoryItem();
        return var1.equals(class_1802.field_8606) ? "Bones" : var1.method_63680().getString();
    }
}

