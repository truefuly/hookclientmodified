/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.StorageBlocksSetting;
import hook.utils.EncryptedString;
import java.awt.Color;
import java.util.Locale;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public final class AutoSellModule
extends Module {
    private static final int SELL_MENU_ROWS = 5;
    private final ModeSetting<SellingMode> mode = new ModeSetting<SellingMode>(EncryptedString.of("Sell Mode"), SellingMode.Whitelist, SellingMode.class).setDescription(EncryptedString.of("Whether to whitelist or blacklist the selected items"));
    private final StorageBlocksSetting items = AutoSellModule.createItemList();
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 20.0, 1.0, 1.0).getValue(EncryptedString.of("Delay in ticks between actions"));
    private final BooleanSetting notifications = new BooleanSetting(EncryptedString.of("Notifications"), true).setDescription(EncryptedString.of("Show chat feedback"));
    private int delayCounter;
    private boolean needsReopen;

    public AutoSellModule() {
        super(EncryptedString.of("Auto Sell"), EncryptedString.of("Automatically sells items"), -1, Category.MISC);
        this.addSettings(this.mode, this.items, this.delay, this.notifications);
    }

    private static StorageBlocksSetting createItemList() {
        StorageBlocksSetting var0 = new StorageBlocksSetting(EncryptedString.of("Items")).pickerOnly();
        Color var1 = new Color(75, 125, 185);
        for (class_1792 var3 : class_7923.field_41178) {
            if (var3 == class_1802.field_8162) continue;
            class_2960 var4 = class_7923.field_41178.method_10221(var3);
            var0.add(var4.toString(), AutoSellModule.prettyName(var4.method_12832()), var3, var3 == class_1802.field_17498, var1);
        }
        return var0;
    }

    private static String prettyName(String var0) {
        StringBuilder var1 = new StringBuilder();
        for (String var5 : var0.split("_")) {
            if (var5.isEmpty()) continue;
            if (!var1.isEmpty()) {
                var1.append(' ');
            }
            var1.append(var5.substring(0, 1).toUpperCase(Locale.ROOT)).append(var5.substring(1));
        }
        return var1.toString();
    }

    @Override
    public void onEnable() {
        this.delayCounter = 20;
        this.needsReopen = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.needsReopen = false;
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1724 != null && this.mc.field_1761 != null && this.mc.method_1562() != null) {
            if (this.delayCounter > 0) {
                --this.delayCounter;
            } else if (this.needsReopen) {
                this.openSell();
                this.needsReopen = false;
                this.delayCounter = 20;
            } else {
                class_1707 var2 = this.container();
                if (var2 == null) {
                    this.openSell();
                    this.delayCounter = 20;
                } else if (this.firstEmptyUsableSlot(var2) < 0) {
                    this.mc.field_1724.method_7346();
                    this.needsReopen = this.hasMatchingItems(var2);
                    if (!this.needsReopen) {
                        this.info("All items sold.");
                        this.toggle();
                    }
                    this.delayCounter = this.delay.getIntValue();
                } else {
                    int var3;
                    for (int var4 = var3 = var2.method_17388() * 9; var4 < var2.field_7761.size(); ++var4) {
                        class_1799 var5 = var2.method_7611(var4).method_7677();
                        if (var5.method_7960() || !this.shouldSellItem(var5.method_7909())) continue;
                        this.mc.field_1761.method_2906(var2.field_7763, var4, 0, class_1713.field_7794, (class_1657)this.mc.field_1724);
                        this.delayCounter = this.delay.getIntValue();
                        return;
                    }
                    this.mc.field_1724.method_7346();
                    this.info("All items sold.");
                    this.toggle();
                }
            }
        }
    }

    private void openSell() {
        this.mc.method_1562().method_45730("sell");
    }

    private class_1707 container() {
        class_1703 class_17032 = this.mc.field_1724.field_7512;
        if (class_17032 instanceof class_1707) {
            boolean var3;
            class_1707 var1 = (class_1707)class_17032;
            boolean bl = var3 = this.mc.field_1755 != null && this.mc.field_1755.method_25440().getString().toLowerCase(Locale.ROOT).contains("sell");
            if (var3 || var1.method_17388() == 5) {
                return var1;
            }
        }
        return null;
    }

    private int usableSlots(class_1707 var1) {
        return Math.max(9, (var1.method_17388() - 1) * 9);
    }

    private int firstEmptyUsableSlot(class_1707 var1) {
        int var2 = Math.min(this.usableSlots(var1), var1.field_7761.size());
        for (int var3 = 0; var3 < var2; ++var3) {
            if (!var1.method_7611(var3).method_7677().method_7960()) continue;
            return var3;
        }
        return -1;
    }

    private boolean hasMatchingItems(class_1707 var1) {
        int var2;
        for (int var3 = var2 = var1.method_17388() * 9; var3 < var1.field_7761.size(); ++var3) {
            class_1799 var4 = var1.method_7611(var3).method_7677();
            if (var4.method_7960() || !this.shouldSellItem(var4.method_7909())) continue;
            return true;
        }
        return false;
    }

    private boolean shouldSellItem(class_1792 var1) {
        boolean var3;
        StorageBlocksSetting.Entry var2 = this.items.get(class_7923.field_41178.method_10221(var1).toString());
        boolean bl = var3 = var2 != null && var2.enabled();
        return this.mode.isMode(SellingMode.Whitelist) ? var3 : !var3;
    }

    private void info(String var1) {
        if (this.notifications.getValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7a[Auto Sell] \u00a7f" + var1)), false);
        }
    }

    public static enum SellingMode {
        Whitelist,
        Blacklist;

    }
}

