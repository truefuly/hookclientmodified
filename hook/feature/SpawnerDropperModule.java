/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1657
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
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
import hook.module.setting.Setting;
import hook.utils.EncryptedString;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3965;

public final class SpawnerDropperModule
extends Module {
    private static final int CHECK_DELAY = 3;
    private final BooleanSetting notifications = new BooleanSetting(EncryptedString.of("Notifications"), true);
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 1.0, 20.0, 5.0, 1.0);
    private final BooleanSetting boneOnly = new BooleanSetting(EncryptedString.of("Bone Only"), true);
    private final NumberSetting autoReopenInterval = new NumberSetting(EncryptedString.of("Auto Reopen Interval"), 1.0, 600.0, 10.0, 1.0);
    private int tickCounter;
    private int currentStep;
    private int checkDelayCounter;
    private int reopenTimer;
    private class_2338 spawnerPos;
    private boolean waitingForInterval;

    public SpawnerDropperModule() {
        super(EncryptedString.of("Spawner Dropper"), EncryptedString.of("Drops all items from nearby spawners"), -1, Category.MISC);
        Setting[] var10001 = new Setting[]{this.notifications, this.delay, this.boneOnly, this.autoReopenInterval};
        this.addSettings(var10001);
    }

    @Override
    public void onEnable() {
        this.reset();
        this.message("Spawner Dropper activated - looking for a nearby spawner.");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.reset();
        if (this.mc.field_1724 != null && this.mc.field_1724.field_7512 instanceof class_1707) {
            this.mc.field_1724.method_7346();
        }
        super.onDisable();
    }

    private void reset() {
        this.tickCounter = 0;
        this.currentStep = 0;
        this.checkDelayCounter = 0;
        this.reopenTimer = 0;
        this.spawnerPos = null;
        this.waitingForInterval = false;
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.field_1761 != null) {
            if (this.spawnerPos == null || !this.mc.field_1687.method_8320(this.spawnerPos).method_27852(class_2246.field_10260)) {
                this.spawnerPos = this.findNearbySpawner();
                if (this.spawnerPos != null) {
                    this.message("Spawner detected nearby.");
                }
            }
            ++this.reopenTimer;
            int var2 = this.autoReopenInterval.getIntValue() * 60 * 20;
            if (this.waitingForInterval) {
                if (this.reopenTimer >= var2) {
                    this.openSpawner();
                }
            } else {
                class_1703 class_17032 = this.mc.field_1724.field_7512;
                if (class_17032 instanceof class_1707) {
                    class_1707 var3 = (class_1707)class_17032;
                    if (var3.field_7761.size() > 53) {
                        if (this.boneOnly.getValue() && this.hasArrows(var3)) {
                            this.message("Arrows detected - waiting for the next interval.");
                            this.mc.field_1724.method_7346();
                            this.waitingForInterval = true;
                            this.reopenTimer = 0;
                        } else if (this.currentStep != 2 && this.currentStep != 5) {
                            if (++this.tickCounter >= this.delay.getIntValue()) {
                                this.tickCounter = 0;
                                switch (this.currentStep) {
                                    case 0: {
                                        this.click(var3, 50);
                                        this.currentStep = 1;
                                        break;
                                    }
                                    case 1: {
                                        this.click(var3, 53);
                                        this.currentStep = 2;
                                        this.checkDelayCounter = 0;
                                    }
                                    default: {
                                        break;
                                    }
                                    case 3: {
                                        this.click(var3, 50);
                                        this.currentStep = 4;
                                        break;
                                    }
                                    case 4: {
                                        this.click(var3, 53);
                                        this.currentStep = 5;
                                        this.checkDelayCounter = 0;
                                        break;
                                    }
                                }
                            }
                        } else if (++this.checkDelayCounter >= 3) {
                            if (var3.method_7611(0).method_7677().method_7960()) {
                                this.message("Spawner emptied - waiting for the next interval.");
                                this.mc.field_1724.method_7346();
                                this.waitingForInterval = true;
                                this.reopenTimer = 0;
                            } else {
                                this.currentStep = this.currentStep == 2 ? 3 : 0;
                                this.checkDelayCounter = 0;
                            }
                        }
                    }
                } else if (this.spawnerPos != null && (this.reopenTimer >= var2 || this.reopenTimer == 20)) {
                    this.openSpawner();
                }
            }
        }
    }

    private boolean hasArrows(class_1707 var1) {
        for (int var2 = 0; var2 <= 44 && var2 < var1.field_7761.size(); ++var2) {
            if (!var1.method_7611(var2).method_7677().method_31574(class_1802.field_8107)) continue;
            return true;
        }
        return false;
    }

    private class_2338 findNearbySpawner() {
        class_2338 var1 = this.mc.field_1724.method_24515();
        class_2338.class_2339 var2 = new class_2338.class_2339();
        for (int var3 = -6; var3 <= 6; ++var3) {
            for (int var4 = -6; var4 <= 6; ++var4) {
                for (int var5 = -6; var5 <= 6; ++var5) {
                    var2.method_10103(var1.method_10263() + var3, var1.method_10264() + var4, var1.method_10260() + var5);
                    if (!this.mc.field_1687.method_8320((class_2338)var2).method_27852(class_2246.field_10260)) continue;
                    return var2.method_10062();
                }
            }
        }
        return null;
    }

    private void openSpawner() {
        if (this.spawnerPos != null) {
            class_3965 var1 = new class_3965(class_243.method_24953((class_2382)this.spawnerPos), class_2350.field_11036, this.spawnerPos, false);
            this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5808, var1);
            this.currentStep = 0;
            this.checkDelayCounter = 0;
            this.tickCounter = 0;
            this.waitingForInterval = false;
            this.reopenTimer = 0;
        }
    }

    private void click(class_1707 var1, int var2) {
        this.mc.field_1761.method_2906(var1.field_7763, var2, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
    }

    private void message(String var1) {
        if (this.notifications.getValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7b[Spawner Dropper] \u00a7f" + var1)), false);
        }
    }
}

