/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1657
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_3965
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3965;

public final class SpawnerOrderModule
extends Module {
    private final StringSetting dropDelayMinutes = new StringSetting(EncryptedString.of("Drop Delay Minutes"), "5");
    private final NumberSetting actionDelay = new NumberSetting(EncryptedString.of("Action Delay Ticks"), 1.0, 200.0, 20.0, 1.0);
    private final BooleanSetting notifications = new BooleanSetting(EncryptedString.of("Notifications"), true);
    private final NumberSetting pagesToProcess = new NumberSetting(EncryptedString.of("Spawner Pages"), 1.0, 10.0, 1.0, 1.0);
    private final List<class_2338> spawners = new ArrayList<class_2338>();
    private OrderState state = OrderState.INVENTORY_CHECK;
    private class_2338 target;
    private int actionTimer;
    private int page;
    private boolean secondPageClick;
    private long lastCycle;
    private boolean movingNotice;

    public SpawnerOrderModule() {
        super(EncryptedString.of("Spawner Order"), EncryptedString.of("Drops nearby spawner loot and sells bones and arrows"), -1, Category.DONUT);
        this.addSettings(this.dropDelayMinutes, this.actionDelay, this.notifications, this.pagesToProcess);
    }

    @Override
    public void onEnable() {
        this.state = OrderState.INVENTORY_CHECK;
        this.actionTimer = 0;
        this.page = 0;
        this.secondPageClick = false;
        this.target = null;
        this.spawners.clear();
        this.lastCycle = 0L;
        this.movingNotice = false;
        this.message("Spawner Order activated.");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.spawners.clear();
        this.target = null;
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.field_1761 != null && this.mc.method_1562() != null) {
            int var10000;
            if (this.state == OrderState.WAITING_CYCLE) {
                if (System.currentTimeMillis() - this.lastCycle < (long)this.delayMinutes() * 60000L) {
                    return;
                }
                this.state = OrderState.INVENTORY_CHECK;
            }
            if ((var10000 = this.actionTimer--) <= 0) {
                this.actionTimer = this.actionDelay.getIntValue();
                switch (this.state.ordinal()) {
                    case 0: {
                        this.state = this.hasSellableInventory() ? OrderState.OPENING_SELL : OrderState.SCANNING;
                        break;
                    }
                    case 1: {
                        this.scan();
                        break;
                    }
                    case 2: {
                        this.handleMoving();
                        break;
                    }
                    case 3: {
                        this.openSpawner();
                        break;
                    }
                    case 4: {
                        this.dropPage();
                        break;
                    }
                    case 5: {
                        this.openSell();
                        break;
                    }
                    case 6: {
                        this.depositOrConfirm();
                    }
                }
            }
        }
    }

    private void scan() {
        this.spawners.clear();
        class_2338 var1 = this.mc.field_1724.method_24515();
        class_2338.class_2339 var2 = new class_2338.class_2339();
        for (int var3 = -5; var3 <= 5; ++var3) {
            for (int var4 = -5; var4 <= 5; ++var4) {
                for (int var5 = -5; var5 <= 5; ++var5) {
                    var2.method_10103(var1.method_10263() + var3, var1.method_10264() + var4, var1.method_10260() + var5);
                    if (!this.mc.field_1687.method_8320((class_2338)var2).method_27852(class_2246.field_10260)) continue;
                    this.spawners.add(var2.method_10062());
                }
            }
        }
        this.spawners.sort(Comparator.comparingDouble(var1x -> this.mc.field_1724.method_5707(class_243.method_24953((class_2382)var1x))));
        if (this.spawners.isEmpty()) {
            this.message("No nearby spawner found; waiting for the next cycle.");
            this.finishCycle();
        } else {
            this.target = this.spawners.get(0);
            this.state = OrderState.MOVING;
            this.movingNotice = false;
        }
    }

    private void handleMoving() {
        if (this.target != null && this.mc.field_1687.method_8320(this.target).method_27852(class_2246.field_10260)) {
            if (this.mc.field_1724.method_5707(class_243.method_24953((class_2382)this.target)) <= 20.25) {
                this.state = OrderState.OPENING;
            } else if (!this.movingNotice) {
                this.message("Move within 4 blocks of the detected spawner.");
                this.movingNotice = true;
            }
        } else {
            this.state = OrderState.SCANNING;
        }
    }

    private void openSpawner() {
        class_3965 var1 = new class_3965(class_243.method_24953((class_2382)this.target), class_2350.field_11036, this.target, false);
        this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5808, var1);
        this.page = 0;
        this.secondPageClick = false;
        this.state = OrderState.DROPPING;
    }

    private void dropPage() {
        class_1707 var1;
        block7: {
            block6: {
                class_1703 class_17032 = this.mc.field_1724.field_7512;
                if (!(class_17032 instanceof class_1707)) break block6;
                var1 = (class_1707)class_17032;
                if (var1.field_7761.size() > 53) break block7;
            }
            return;
        }
        if (!this.secondPageClick) {
            this.click(var1, 50, class_1713.field_7790);
            this.secondPageClick = true;
        } else {
            this.click(var1, 53, class_1713.field_7790);
            this.secondPageClick = false;
            if (++this.page >= this.pagesToProcess.getIntValue()) {
                this.mc.field_1724.method_7346();
                this.state = OrderState.OPENING_SELL;
            }
        }
    }

    private void openSell() {
        if (this.mc.field_1724.field_7512 instanceof class_1707) {
            this.mc.field_1724.method_7346();
        }
        this.mc.method_1562().method_45730("sell");
        this.state = OrderState.DEPOSITING;
        this.actionTimer = Math.max(20, this.actionDelay.getIntValue());
    }

    private void depositOrConfirm() {
        class_1703 class_17032 = this.mc.field_1724.field_7512;
        if (class_17032 instanceof class_1707) {
            int var5;
            class_1707 var1 = (class_1707)class_17032;
            for (int var3 = var5 = var1.method_17388() * 9; var3 < var1.field_7761.size(); ++var3) {
                class_1799 var4 = var1.method_7611(var3).method_7677();
                if (!var4.method_31574(class_1802.field_8606) && !var4.method_31574(class_1802.field_8107)) continue;
                this.click(var1, var3, class_1713.field_7794);
                return;
            }
            for (int var6 = 0; var6 < var5; ++var6) {
                class_1799 var7 = var1.method_7611(var6).method_7677();
                if (!var7.method_31574(class_1802.field_8581) && !var7.method_31574(class_1802.field_8656)) continue;
                this.click(var1, var6, class_1713.field_7790);
                this.mc.field_1724.method_7346();
                this.message("Cycle completed. Next cycle in " + this.delayMinutes() + " minutes.");
                this.finishCycle();
                return;
            }
            this.mc.field_1724.method_7346();
            this.finishCycle();
        }
    }

    private boolean hasSellableInventory() {
        for (class_1799 var2 : this.mc.field_1724.method_31548().method_67533()) {
            if (!var2.method_31574(class_1802.field_8606) && !var2.method_31574(class_1802.field_8107)) continue;
            return true;
        }
        return false;
    }

    private int delayMinutes() {
        try {
            return Math.max(1, Integer.parseInt(this.dropDelayMinutes.getValue().trim()));
        }
        catch (NumberFormatException var2) {
            return 5;
        }
    }

    private void finishCycle() {
        this.lastCycle = System.currentTimeMillis();
        this.state = OrderState.WAITING_CYCLE;
        this.target = null;
    }

    private void click(class_1707 var1, int var2, class_1713 var3) {
        this.mc.field_1761.method_2906(var1.field_7763, var2, 0, var3, (class_1657)this.mc.field_1724);
    }

    private void message(String var1) {
        if (this.notifications.getValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7b[Spawner Order] \u00a7f" + var1)), false);
        }
    }

    private static enum OrderState {
        INVENTORY_CHECK,
        SCANNING,
        MOVING,
        OPENING,
        DROPPING,
        OPENING_SELL,
        DEPOSITING,
        WAITING_CYCLE;

    }
}

