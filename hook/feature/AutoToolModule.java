/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1820
 *  net.minecraft.class_2202
 *  net.minecraft.class_2211
 *  net.minecraft.class_2338
 *  net.minecraft.class_2397
 *  net.minecraft.class_2680
 *  net.minecraft.class_3481
 *  net.minecraft.class_3489
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.AttackBlockEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1820;
import net.minecraft.class_2202;
import net.minecraft.class_2211;
import net.minecraft.class_2338;
import net.minecraft.class_2397;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3489;

public final class AutoToolModule
extends Module {
    private static final int MANUAL_GRACE_MS = 450;
    private static final int MAX_SWITCHES = 10;
    private static final int SWITCH_WINDOW_MS = 4000;
    private final BooleanSetting legit = new BooleanSetting(EncryptedString.of("Legit"), false).setDescription(EncryptedString.of("Adds human-like delay, hold time, variation and switch limits"));
    private final BooleanSetting antiBreak = new BooleanSetting(EncryptedString.of("Anti Break"), true);
    private final NumberSetting antiBreakPercentage = new NumberSetting(EncryptedString.of("Anti Break Percentage"), 1.0, 100.0, 5.0, 1.0);
    private final Deque<Long> switchTimes = new ArrayDeque<Long>();
    private int pendingSlot = -1;
    private class_2338 pendingBlock;
    private long switchAt;
    private long nextSwitchAllowedAt;
    private long lastManualChangeAt;
    private int lastObservedSlot = -1;
    private int lastAutomaticSlot = -1;

    public AutoToolModule() {
        super(EncryptedString.of("Auto Tool"), EncryptedString.of("Switches to the best tool for the block you mine"), -1, Category.MISC);
        this.addSettings(this.legit, this.antiBreak, this.antiBreakPercentage);
    }

    @Override
    public void onEnable() {
        this.resetState();
        if (this.mc.field_1724 != null) {
            this.lastObservedSlot = this.mc.field_1724.method_31548().method_67532();
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.resetState();
        super.onDisable();
    }

    private void resetState() {
        this.pendingSlot = -1;
        this.pendingBlock = null;
        this.switchAt = 0L;
        this.nextSwitchAllowedAt = 0L;
        this.lastManualChangeAt = 0L;
        this.lastObservedSlot = -1;
        this.lastAutomaticSlot = -1;
        this.switchTimes.clear();
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            long var2 = System.currentTimeMillis();
            int var4 = this.mc.field_1724.method_31548().method_67532();
            if (this.lastObservedSlot == -1) {
                this.lastObservedSlot = var4;
            }
            if (var4 != this.lastObservedSlot) {
                if (var4 != this.lastAutomaticSlot) {
                    this.lastManualChangeAt = var2;
                    this.pendingSlot = -1;
                    this.pendingBlock = null;
                }
                this.lastObservedSlot = var4;
                this.lastAutomaticSlot = -1;
            }
            while (!this.switchTimes.isEmpty() && var2 - this.switchTimes.peekFirst() > 4000L) {
                this.switchTimes.removeFirst();
            }
            if (this.legit.getValue() && this.pendingSlot >= 0) {
                if (this.mc.field_1755 == null && this.mc.field_1690.field_1886.method_1434() && this.pendingBlock != null) {
                    if (var2 >= this.switchAt && var2 >= this.nextSwitchAllowedAt && var2 - this.lastManualChangeAt >= 450L) {
                        if (this.switchTimes.size() < 10 && !this.mc.field_1687.method_8320(this.pendingBlock).method_26215()) {
                            this.performSwitch(this.pendingSlot, var2);
                            this.pendingSlot = -1;
                            this.pendingBlock = null;
                        } else {
                            this.pendingSlot = -1;
                            this.pendingBlock = null;
                        }
                    }
                } else {
                    this.pendingSlot = -1;
                    this.pendingBlock = null;
                }
            }
        } else {
            this.pendingSlot = -1;
            this.pendingBlock = null;
        }
    }

    @EventListener
    public void handleAttackBlockEvent(AttackBlockEvent var1) {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            class_2680 var2 = this.mc.field_1687.method_8320(var1.pos);
            class_1799 var3 = this.mc.field_1724.method_6047();
            if (this.isToolBreakingSoon(var3) && AutoToolModule.isToolItemStack(var3)) {
                this.mc.field_1690.field_1886.method_23481(false);
                this.pendingSlot = -1;
                this.pendingBlock = null;
                var1.cancel();
            } else {
                SlotScore var4 = this.findBestChoice(var2, this.legit.getValue());
                if (var4.slot() >= 0) {
                    double var5 = AutoToolModule.calculateToolEfficiency(var3, var2, var1x -> !this.isToolBreakingSoon((class_1799)var1x));
                    if (var4.slot() != this.mc.field_1724.method_31548().method_67532() && this.isWorthSwitching(var5, var4.score())) {
                        if (!this.legit.getValue()) {
                            this.performSwitch(var4.slot(), System.currentTimeMillis());
                        } else {
                            long var7 = System.currentTimeMillis();
                            if (var7 - this.lastManualChangeAt >= 450L && var7 >= this.nextSwitchAllowedAt && !(ThreadLocalRandom.current().nextDouble() < 0.03)) {
                                this.pendingSlot = var4.slot();
                                this.pendingBlock = var1.pos.method_10062();
                                int var9 = ThreadLocalRandom.current().nextInt(30, 76);
                                int var10 = ThreadLocalRandom.current().nextInt(55, 126);
                                this.switchAt = var7 + (long)var9 + (long)var10;
                            }
                        }
                    }
                }
            }
        }
    }

    private SlotScore findBestChoice(class_2680 var1, boolean var2) {
        int[] var10000 = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        int[] var3 = var10000;
        if (var2) {
            ThreadLocalRandom var4 = ThreadLocalRandom.current();
            for (int var5 = var3.length - 1; var5 > 0; --var5) {
                int var6 = var4.nextInt(var5 + 1);
                int var7 = var3[var5];
                var3[var5] = var3[var6];
                var3[var6] = var7;
            }
        }
        int var16 = -1;
        int var17 = -1;
        double var18 = -1.0;
        double var8 = -1.0;
        int[] var10 = var3;
        int var11 = var3.length;
        for (int var12 = 0; var12 < var11; ++var12) {
            int var13 = var10[var12];
            double var14 = AutoToolModule.calculateToolEfficiency(this.mc.field_1724.method_31548().method_5438(var13), var1, var1x -> !this.isToolBreakingSoon((class_1799)var1x));
            if (var14 > var18) {
                var17 = var16;
                var8 = var18;
                var16 = var13;
                var18 = var14;
                continue;
            }
            if (!(var14 > var8)) continue;
            var17 = var13;
            var8 = var14;
        }
        return var2 && var17 >= 0 && var18 > 0.0 && var8 / var18 >= 0.92 && ThreadLocalRandom.current().nextDouble() < 0.12 ? new SlotScore(var17, var8) : new SlotScore(var16, var18);
    }

    private boolean isWorthSwitching(double var1, double var3) {
        if (var3 < 0.0) {
            return false;
        }
        if (var1 < 0.0) {
            return true;
        }
        if (!this.legit.getValue()) {
            return var3 > var1;
        }
        if (var3 <= 0.0) {
            return false;
        }
        if (var1 / var3 >= 0.96) {
            return false;
        }
        return var3 >= var1 * 1.08;
    }

    private void performSwitch(int var1, long var2) {
        if (var1 >= 0 && var1 <= 8 && this.mc.field_1724 != null) {
            InventoryUtil.swap(var1);
            this.lastAutomaticSlot = var1;
            this.lastObservedSlot = var1;
            this.switchTimes.addLast(var2);
            this.nextSwitchAllowedAt = var2 + (long)ThreadLocalRandom.current().nextInt(95, 221);
        }
    }

    public static double calculateToolEfficiency(class_1799 var0, class_2680 var1, Predicate<class_1799> var2) {
        if (var2.test(var0) && AutoToolModule.isToolItemStack(var0)) {
            boolean var3 = var0.method_41409().method_40220(class_3489.field_42611) && (var1.method_26204() instanceof class_2211 || var1.method_26204() instanceof class_2202);
            boolean var4 = var0.method_7909() instanceof class_1820 && var1.method_26204() instanceof class_2397;
            boolean var5 = var0.method_7951(var1) || var3 || var4 || var1.method_26164(class_3481.field_15481);
            return var5 ? (double)(var0.method_7924(var1) * 1000.0f) : -1.0;
        }
        return -1.0;
    }

    public static boolean isToolItemStack(class_1799 var0) {
        return AutoToolModule.isToolItem(var0.method_7909());
    }

    public static boolean isToolItem(class_1792 var0) {
        return var0.method_40131().method_40220(class_3489.field_42612) || var0.method_40131().method_40220(class_3489.field_42613) || var0.method_40131().method_40220(class_3489.field_42614) || var0.method_40131().method_40220(class_3489.field_42615) || var0 instanceof class_1820;
    }

    private boolean isToolBreakingSoon(class_1799 var1) {
        return this.antiBreak.getValue() && var1.method_7963() && var1.method_7936() - var1.method_7919() < var1.method_7936() * this.antiBreakPercentage.getIntValue() / 100;
    }

    private record SlotScore(int slot, double score) {
    }
}

