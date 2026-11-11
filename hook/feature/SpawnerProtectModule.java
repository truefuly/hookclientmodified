/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1893
 *  net.minecraft.class_1923
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2382
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2586
 *  net.minecraft.class_2611
 *  net.minecraft.class_2636
 *  net.minecraft.class_2818
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_3489
 *  net.minecraft.class_3532
 *  net.minecraft.class_3965
 *  net.minecraft.class_742
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.StringSetting;
import hook.utils.EnchantmentUtil;
import hook.utils.EncryptedString;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1893;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2611;
import net.minecraft.class_2636;
import net.minecraft.class_2818;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_742;

public final class SpawnerProtectModule
extends Module {
    private static final double REACH = 4.3;
    private static final int TARGET_TIMEOUT_TICKS = 400;
    private static final int STUCK_CHECK_TICKS = 60;
    private static final int NO_TARGET_GRACE_TICKS = 30;
    private static final int OFFHAND_SLOT = 40;
    private final NumberSetting detectRange = new NumberSetting(EncryptedString.of("Detect Range"), 8.0, 128.0, 48.0, 1.0).getValue(EncryptedString.of("Starts protecting when another player comes within this many blocks"));
    private final NumberSetting emergencyDistance = new NumberSetting(EncryptedString.of("Emergency Distance"), 0.0, 32.0, 5.0, 1.0).getValue(EncryptedString.of("Logs out if a player gets this close (0 = off)"));
    private final NumberSetting mineRadius = new NumberSetting(EncryptedString.of("Mine Radius"), 4.0, 32.0, 12.0, 1.0).getValue(EncryptedString.of("Spawners within this many blocks of where you stood get collected"));
    private final BooleanSetting useEnderChest = new BooleanSetting(EncryptedString.of("Use Ender Chest"), true).setDescription(EncryptedString.of("Stores the spawners in the ender chest placed next to them"));
    private final NumberSetting stashStacks = new NumberSetting(EncryptedString.of("Stash After Stacks"), 1.0, 27.0, 4.0, 1.0).getValue(EncryptedString.of("Stores spawners in the ender chest once you carry this many stacks"));
    private final StringSetting whitelist = new StringSetting(EncryptedString.of("Whitelist"), "").setDescription(EncryptedString.of("Player names that won't trigger it, separated by commas"));
    private final BooleanSetting chatAlerts = new BooleanSetting(EncryptedString.of("Chat Alerts"), true);
    private final Random random = new Random();
    private final Set<class_2338> skipped = new HashSet<class_2338>();
    private ProtectionState state = ProtectionState.IDLE;
    private class_2338 origin;
    private class_2338 target;
    private class_2338 chestPos;
    private int targetTicks;
    private int stateTicks;
    private int actionDelay;
    private int noTargetTicks;
    private int nextOpenAttempt;
    private class_243 stuckCheckPos;
    private int stuckTicks;
    private boolean chestFull;
    private boolean stashFailed;
    private boolean warnedNoPickaxe;
    private boolean warnedNoChest;
    private int aimTicks;
    private int stashCooldown;
    private int stashFailures;
    private class_2338 jitterFor;
    private double jitterA;
    private double jitterB;
    private boolean aimActive;
    private boolean aimOvershoot;
    private long aimStart;
    private long aimDuration;
    private float aimStartYaw;
    private float aimStartPitch;
    private float aimEndYaw;
    private float aimEndPitch;
    private float aimFinalYaw;
    private float aimFinalPitch;
    private float aimCurve;
    private float tremorYaw;
    private float tremorPitch;
    private float tremorTargetYaw;
    private float tremorTargetPitch;
    private long nextTremorChange;
    private long lastAimUpdate;
    private int lastMineAge = Integer.MIN_VALUE;

    public SpawnerProtectModule() {
        super(EncryptedString.of("Spawner Protect"), EncryptedString.of("Mines and stashes your spawners when a player shows up, and logs out if they get too close"), -1, Category.MACROS);
        this.addSettings(this.detectRange, this.emergencyDistance, this.mineRadius, this.useEnderChest, this.stashStacks, this.whitelist, this.chatAlerts);
    }

    @Override
    public void onEnable() {
        this.reset();
        if (this.mc.field_1724 != null && this.findSilkPickaxe() == -1) {
            this.warnNoPickaxe();
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    private void reset() {
        this.releaseKeys();
        if (this.mc.field_1761 != null) {
            this.mc.field_1761.method_2925();
        }
        this.state = ProtectionState.IDLE;
        this.skipped.clear();
        this.origin = null;
        this.target = null;
        this.chestPos = null;
        this.targetTicks = 0;
        this.stateTicks = 0;
        this.actionDelay = 0;
        this.noTargetTicks = 0;
        this.nextOpenAttempt = 0;
        this.stuckCheckPos = null;
        this.stuckTicks = 0;
        this.chestFull = false;
        this.stashFailed = false;
        this.warnedNoPickaxe = false;
        this.warnedNoChest = false;
        this.aimTicks = 0;
        this.stashCooldown = 0;
        this.stashFailures = 0;
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.field_1761 != null) {
            double var3;
            class_742 var2 = this.nearestThreat();
            double d = var3 = var2 == null ? Double.MAX_VALUE : Math.sqrt(this.mc.field_1724.method_5858((class_1297)var2));
            if (var2 != null && this.emergencyDistance.getValue() > 0.0 && var3 <= this.emergencyDistance.getValue()) {
                this.emergencyLogout(var2, var3);
            } else {
                boolean var5 = var2 != null && var3 <= this.detectRange.getValue();
                switch (this.state.ordinal()) {
                    case 0: {
                        if (!var5) {
                            this.warnedNoPickaxe = false;
                        } else if (this.findSilkPickaxe() == -1) {
                            if (!this.warnedNoPickaxe) {
                                this.warnNoPickaxe();
                            }
                        } else {
                            this.start(var2);
                        }
                        return;
                    }
                    case 5: {
                        this.releaseKeys();
                        if (!var5) {
                            this.state = ProtectionState.IDLE;
                            this.warnedNoPickaxe = false;
                        }
                        return;
                    }
                }
                ++this.stateTicks;
                if (this.actionDelay > 0) {
                    --this.actionDelay;
                } else {
                    switch (this.state.ordinal()) {
                        case 1: {
                            this.tickMining();
                            break;
                        }
                        case 2: {
                            this.tickStashWalk();
                            break;
                        }
                        case 3: {
                            this.tickStashOpen();
                            break;
                        }
                        case 4: {
                            this.tickStashDeposit();
                        }
                    }
                }
            }
        }
    }

    private void start(class_742 var1) {
        this.origin = this.mc.field_1724.method_24515();
        this.skipped.clear();
        this.chestFull = false;
        this.stashFailed = false;
        this.stashFailures = 0;
        this.stashCooldown = 0;
        this.warnedNoChest = false;
        this.target = null;
        this.setState(ProtectionState.MINING);
        this.alert("\u00a7e" + var1.method_5477().getString() + " \u00a7fdetected - mining your spawners");
        if (this.useEnderChest.getValue() && this.findPlacedChest() == null) {
            this.warnNoChest();
        }
    }

    private void tickMining() {
        this.mc.field_1690.field_1832.method_23481(true);
        if (this.stashCooldown > 0) {
            --this.stashCooldown;
        }
        if (this.shouldStash(false)) {
            this.beginStash();
        } else {
            if (this.target == null || !this.isSpawner(this.target)) {
                this.target = this.nextTarget();
                this.targetTicks = 0;
                this.aimTicks = 0;
                this.stuckCheckPos = null;
                this.stuckTicks = 0;
                if (this.target == null) {
                    this.stopWalking();
                    if (++this.noTargetTicks < 30) {
                        return;
                    }
                    if (this.shouldStash(true)) {
                        this.beginStash();
                        return;
                    }
                    if (this.stashCooldown > 0 && this.canStash() && this.countItem(class_1802.field_8849) > 0) {
                        return;
                    }
                    this.finish();
                    return;
                }
            }
            this.noTargetTicks = 0;
            int var1 = this.findSilkPickaxe();
            if (var1 == -1) {
                this.warnNoPickaxe();
                this.finish();
            } else {
                this.mc.field_1724.method_31548().method_61496(var1);
                if (++this.targetTicks > 400) {
                    this.skipTarget("took too long");
                } else {
                    class_243 var2 = class_243.method_24953((class_2382)this.target);
                    if (this.mc.field_1724.method_33571().method_1025(var2) <= 18.49) {
                        this.stopWalking();
                        boolean var3 = this.lookAt(this.faceCenter(this.target));
                        class_3965 var4 = this.crosshairBlock();
                        if (var4 != null && !var4.method_17777().equals((Object)this.target) && this.isCollectable(var4.method_17777())) {
                            this.target = var4.method_17777().method_10062();
                        }
                        if (var4 != null && var4.method_17777().equals((Object)this.target)) {
                            this.aimTicks = 0;
                            this.mineBlock(this.target, var4.method_17780());
                        } else if (var3 && ++this.aimTicks > 40) {
                            this.skipTarget("blocked from view");
                        }
                    } else if (!this.walkTowards(var2)) {
                        this.skipTarget("can't walk there");
                    }
                }
            }
        }
    }

    private boolean isCollectable(class_2338 var1) {
        double var2 = this.mineRadius.getValue();
        return this.isSpawner(var1) && !this.skipped.contains(var1) && var1.method_10262((class_2382)this.origin) <= var2 * var2;
    }

    private class_243 faceCenter(class_2338 pos) {
        if (!pos.equals((Object)this.jitterFor)) {
            this.jitterFor = pos;
            this.jitterA = (this.random.nextDouble() - 0.5) * 0.5;
            this.jitterB = (this.random.nextDouble() - 0.5) * 0.5;
        }
        class_243 eye = this.mc.field_1724.method_33571();
        class_243 center = class_243.method_24953((class_2382)pos);
        class_2350 facing = class_2350.method_10147((float)((float)(eye.field_1352 - center.field_1352)), (float)((float)(eye.field_1351 - center.field_1351)), (float)((float)(eye.field_1350 - center.field_1350)));
        class_243 point = center.method_1031((double)facing.method_10148() * 0.45, (double)facing.method_10164() * 0.45, (double)facing.method_10165() * 0.45);
        if (facing.method_10148() != 0) {
            return point.method_1031(0.0, this.jitterA, this.jitterB);
        }
        return facing.method_10164() != 0 ? point.method_1031(this.jitterA, 0.0, this.jitterB) : point.method_1031(this.jitterA, this.jitterB, 0.0);
    }

    private class_3965 crosshairBlock() {
        class_3965 var1;
        class_239 class_2392 = this.mc.field_1765;
        if (class_2392 instanceof class_3965 && (var1 = (class_3965)class_2392).method_17783() == class_239.class_240.field_1332) {
            return var1;
        }
        return null;
    }

    private class_2338 nextTarget() {
        double var1 = this.mineRadius.getValue() * this.mineRadius.getValue();
        int var3 = (int)Math.ceil(this.mineRadius.getValue() / 16.0) + 1;
        class_1923 var4 = new class_1923(this.origin);
        class_2338 var5 = null;
        double var6 = Double.MAX_VALUE;
        for (int var8 = var4.field_9181 - var3; var8 <= var4.field_9181 + var3; ++var8) {
            for (int var9 = var4.field_9180 - var3; var9 <= var4.field_9180 + var3; ++var9) {
                if (!this.mc.field_1687.method_8393(var8, var9)) continue;
                class_2818 var10 = this.mc.field_1687.method_8497(var8, var9);
                for (class_2586 var12 : var10.method_12214().values()) {
                    double var14;
                    class_2338 var13;
                    if (!(var12 instanceof class_2636) || this.skipped.contains(var13 = var12.method_11016()) || !(var13.method_10262((class_2382)this.origin) <= var1) || !this.isSpawner(var13) || !((var14 = this.mc.field_1724.method_33571().method_1025(class_243.method_24953((class_2382)var13))) < var6)) continue;
                    var6 = var14;
                    var5 = var13.method_10062();
                }
            }
        }
        return var5;
    }

    private boolean isSpawner(class_2338 var1) {
        return this.mc.field_1687.method_8320(var1).method_27852(class_2246.field_10260);
    }

    private void skipTarget(String var1) {
        this.alert("\u00a7cSkipped spawner at " + this.target.method_23854() + " (" + var1 + ")");
        this.skipped.add(this.target);
        this.target = null;
        this.mc.field_1761.method_2925();
    }

    private void finish() {
        String var3;
        this.releaseKeys();
        this.mc.field_1761.method_2925();
        String var1 = this.skipped.isEmpty() ? "All spawners mined" : "Done - " + this.skipped.size() + " spawner(s) could not be reached";
        int var2 = this.countItem(class_1802.field_8849);
        String string = var3 = var2 > 0 ? "[Spawner Protect] " + var1 + " - logged out holding " + var2 + " spawners" : "[Spawner Protect] " + var1 + " and stored in the ender chest - logged out";
        if (this.isEnabled()) {
            this.toggle();
        }
        if (this.mc.method_1562() != null) {
            this.mc.method_1562().method_48296().method_10747((class_2561)class_2561.method_43470((String)var3));
        }
    }

    private void mineBlock(class_2338 var1, class_2350 var2) {
        if (this.mc.field_1724.field_6012 != this.lastMineAge) {
            this.lastMineAge = this.mc.field_1724.field_6012;
            if (this.mc.field_1761.method_2902(var1, var2)) {
                this.mc.field_1724.method_6104(class_1268.field_5808);
            }
        }
    }

    private boolean walkTowards(class_243 var1) {
        this.lookAt(var1);
        this.mc.field_1690.field_1894.method_23481(true);
        this.mc.field_1690.field_1903.method_23481(this.mc.field_1724.field_5976 && this.mc.field_1724.method_24828());
        if (this.stuckCheckPos == null) {
            this.stuckCheckPos = this.mc.field_1724.method_73189();
            this.stuckTicks = 0;
        } else if (++this.stuckTicks >= 60) {
            boolean var2 = this.mc.field_1724.method_73189().method_1025(this.stuckCheckPos) > 0.09;
            this.stuckCheckPos = this.mc.field_1724.method_73189();
            this.stuckTicks = 0;
            if (!var2) {
                this.stopWalking();
                return false;
            }
        }
        return true;
    }

    private void stopWalking() {
        this.mc.field_1690.field_1894.method_23481(false);
        this.mc.field_1690.field_1903.method_23481(false);
    }

    private void releaseKeys() {
        if (this.mc.field_1690 != null) {
            this.mc.field_1690.field_1894.method_23481(false);
            this.mc.field_1690.field_1903.method_23481(false);
            this.mc.field_1690.field_1832.method_23481(false);
        }
    }

    @EventListener
    public void onRender3D(Render3DEvent var1) {
        if (this.mc.field_1724 != null && this.state != ProtectionState.IDLE && this.state != ProtectionState.DONE) {
            this.updateAim(System.nanoTime());
        }
    }

    private boolean lookAt(class_243 var1) {
        float[] var4;
        float var6;
        long var2 = System.nanoTime();
        float var5 = this.aimActive ? this.aimFinalYaw : this.mc.field_1724.method_36454();
        float var7 = SpawnerProtectModule.angleBetween(var5, var6 = this.aimActive ? this.aimFinalPitch : this.mc.field_1724.method_36455(), (var4 = this.rotationTo(var1))[0], var4[1]);
        if (var7 > 6.0f) {
            this.startAim(var4[0], var4[1], var2 + (long)(60 + this.random.nextInt(100)) * 1000000L, true);
        } else if (var7 > 0.4f) {
            if (this.aimActive) {
                this.aimFinalYaw = this.aimStartYaw + class_3532.method_15393((float)(var4[0] - this.aimStartYaw));
                this.aimFinalPitch = var4[1];
                if (!this.aimOvershoot) {
                    this.aimEndYaw = this.aimFinalYaw;
                    this.aimEndPitch = this.aimFinalPitch;
                }
            } else {
                this.startAim(var4[0], var4[1], var2, false);
            }
        }
        this.updateAim(var2);
        return !this.aimActive && SpawnerProtectModule.angleBetween(this.mc.field_1724.method_36454(), this.mc.field_1724.method_36455(), var4[0], var4[1]) < 3.0f;
    }

    private void startAim(float var1, float var2, long var3, boolean var5) {
        float var6 = this.mc.field_1724.method_36454();
        float var7 = this.mc.field_1724.method_36455();
        float var8 = class_3532.method_15393((float)(var1 - var6));
        float var9 = var2 - var7;
        float var10 = (float)Math.sqrt(var8 * var8 + var9 * var9);
        this.aimFinalYaw = var6 + var8;
        this.aimFinalPitch = var2;
        this.aimOvershoot = var5 && var10 > 20.0f && this.random.nextFloat() < 0.35f;
        float var11 = this.aimOvershoot ? 1.04f + this.random.nextFloat() * 0.06f : 1.0f;
        this.aimStartYaw = var6;
        this.aimStartPitch = var7;
        this.aimEndYaw = var6 + var8 * var11;
        this.aimEndPitch = class_3532.method_15363((float)(var7 + var9 * var11), (float)-90.0f, (float)90.0f);
        double var12 = (90.0 + 110.0 * (Math.log(1.0 + (double)var10 / 4.0) / Math.log(2.0))) * (0.85 + this.random.nextDouble() * 0.35);
        this.aimDuration = (long)(var12 * 1000000.0);
        this.aimCurve = (this.random.nextFloat() - 0.5f) * 0.24f;
        this.aimStart = var3;
        this.aimActive = true;
    }

    private void updateAim(long var1) {
        if (this.mc.field_1724 != null && this.mc.field_1755 == null) {
            double var3 = this.lastAimUpdate == 0L ? 0.0 : Math.min(0.1, (double)(var1 - this.lastAimUpdate) / 1.0E9);
            this.lastAimUpdate = var1;
            if (this.aimActive) {
                if (var1 < this.aimStart) {
                    return;
                }
                if (var1 - this.aimStart > this.aimDuration + 500000000L) {
                    this.aimActive = false;
                    return;
                }
                double var5 = Math.min(1.0, (double)(var1 - this.aimStart) / (double)this.aimDuration);
                double var7 = var5 * var5 * var5 * (10.0 - 15.0 * var5 + 6.0 * var5 * var5);
                float var9 = this.aimEndYaw - this.aimStartYaw;
                float var10 = this.aimEndPitch - this.aimStartPitch;
                double var11 = Math.sqrt(var9 * var9 + var10 * var10);
                double var13 = Math.sin(Math.PI * var7) * (double)this.aimCurve * var11;
                double var15 = var11 > 0.001 ? (double)(-var10) / var11 : 0.0;
                double var17 = var11 > 0.001 ? (double)var9 / var11 : 0.0;
                this.moveLook((float)((double)this.aimStartYaw + (double)var9 * var7 + var15 * var13), (float)((double)this.aimStartPitch + (double)var10 * var7 + var17 * var13));
                if (var5 >= 1.0) {
                    if (this.aimOvershoot) {
                        this.aimOvershoot = false;
                        this.aimStartYaw = this.mc.field_1724.method_36454();
                        this.aimStartPitch = this.mc.field_1724.method_36455();
                        this.aimEndYaw = this.aimFinalYaw;
                        this.aimEndPitch = this.aimFinalPitch;
                        float var19 = SpawnerProtectModule.angleBetween(this.aimStartYaw, this.aimStartPitch, this.aimEndYaw, this.aimEndPitch);
                        this.aimDuration = (long)((60.0 + (double)this.random.nextInt(60) + (double)var19 * 4.0) * 1000000.0);
                        this.aimCurve = 0.0f;
                        this.aimStart = var1 + (long)(20 + this.random.nextInt(40)) * 1000000L;
                    } else {
                        this.aimActive = false;
                    }
                }
            } else if (var3 > 0.0) {
                if (var1 >= this.nextTremorChange) {
                    this.tremorTargetYaw = (this.random.nextFloat() - 0.5f) * 0.6f;
                    this.tremorTargetPitch = (this.random.nextFloat() - 0.5f) * 0.4f;
                    this.nextTremorChange = var1 + (long)(180 + this.random.nextInt(260)) * 1000000L;
                }
                float var20 = (float)(1.0 - Math.exp(-var3 / 0.25));
                float var6 = (this.tremorTargetYaw - this.tremorYaw) * var20;
                float var21 = (this.tremorTargetPitch - this.tremorPitch) * var20;
                this.tremorYaw += var6;
                this.tremorPitch += var21;
                this.moveLook(this.mc.field_1724.method_36454() + var6, this.mc.field_1724.method_36455() + var21);
            }
        } else {
            this.lastAimUpdate = var1;
        }
    }

    private void moveLook(float var1, float var2) {
        double var3 = var1 - this.mc.field_1724.method_36454();
        double var5 = var2 - this.mc.field_1724.method_36455();
        if (Math.abs(var3) > 1.0E-4 || Math.abs(var5) > 1.0E-4) {
            this.mc.field_1724.method_5872(var3 / 0.15, var5 / 0.15);
        }
    }

    private float[] rotationTo(class_243 var1) {
        class_243 var2 = this.mc.field_1724.method_33571();
        double var3 = var1.field_1352 - var2.field_1352;
        double var5 = var1.field_1351 - var2.field_1351;
        double var7 = var1.field_1350 - var2.field_1350;
        float var9 = (float)Math.toDegrees(Math.atan2(var7, var3)) - 90.0f;
        float var10 = (float)(-Math.toDegrees(Math.atan2(var5, Math.sqrt(var3 * var3 + var7 * var7))));
        float[] var10000 = new float[]{var9, class_3532.method_15363((float)var10, (float)-90.0f, (float)90.0f)};
        return var10000;
    }

    private static float angleBetween(float var0, float var1, float var2, float var3) {
        float var4 = class_3532.method_15393((float)(var2 - var0));
        float var5 = var3 - var1;
        return (float)Math.sqrt(var4 * var4 + var5 * var5);
    }

    private boolean canStash() {
        return this.useEnderChest.getValue() && !this.chestFull && !this.stashFailed && this.findPlacedChest() != null;
    }

    private boolean shouldStash(boolean var1) {
        if (this.useEnderChest.getValue() && !this.chestFull && !this.stashFailed && this.stashCooldown <= 0) {
            boolean var3;
            int var2 = this.countItem(class_1802.field_8849);
            if (var2 == 0) {
                return false;
            }
            boolean bl = var3 = var1 || var2 >= this.stashStacks.getIntValue() * 64 || this.mc.field_1724.method_31548().method_7376() == -1;
            if (!var3) {
                return false;
            }
            if (this.findPlacedChest() == null) {
                if (!this.warnedNoChest) {
                    this.warnNoChest();
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private class_2338 findPlacedChest() {
        double var1 = Math.max(8.0, this.mineRadius.getValue());
        double var3 = var1 * var1;
        int var5 = (int)Math.ceil(var1 / 16.0) + 1;
        class_2338 var6 = this.origin != null ? this.origin : this.mc.field_1724.method_24515();
        class_1923 var7 = new class_1923(var6);
        class_2338 var8 = null;
        double var9 = Double.MAX_VALUE;
        for (int var11 = var7.field_9181 - var5; var11 <= var7.field_9181 + var5; ++var11) {
            for (int var12 = var7.field_9180 - var5; var12 <= var7.field_9180 + var5; ++var12) {
                if (!this.mc.field_1687.method_8393(var11, var12)) continue;
                for (class_2586 var14 : this.mc.field_1687.method_8497(var11, var12).method_12214().values()) {
                    double var16;
                    class_2338 var15;
                    if (!(var14 instanceof class_2611) || !((var15 = var14.method_11016()).method_10262((class_2382)var6) <= var3) || !this.mc.field_1687.method_8320(var15).method_27852(class_2246.field_10443) || !((var16 = this.mc.field_1724.method_33571().method_1025(class_243.method_24953((class_2382)var15))) < var9)) continue;
                    var9 = var16;
                    var8 = var15.method_10062();
                }
            }
        }
        return var8;
    }

    private void beginStash() {
        this.stopWalking();
        this.mc.field_1761.method_2925();
        this.chestPos = this.findPlacedChest();
        this.stuckCheckPos = null;
        this.stuckTicks = 0;
        this.setState(ProtectionState.STASH_WALK);
    }

    private void stashFailed(String var1) {
        if (this.mc.field_1724.field_7512 instanceof class_1707) {
            this.mc.field_1724.method_7346();
        }
        if (++this.stashFailures >= 3) {
            this.alert("\u00a7cEnder chest stash failed (" + var1 + ") - keeping spawners in your inventory");
            this.stashFailed = true;
        } else {
            this.alert("\u00a7cEnder chest stash failed (" + var1 + ") - trying again in a few seconds");
            this.stashCooldown = 100;
        }
        this.chestPos = null;
        this.stopWalking();
        this.setState(ProtectionState.MINING);
    }

    private void tickStashWalk() {
        this.mc.field_1690.field_1832.method_23481(true);
        if (this.chestPos != null && this.mc.field_1687.method_8320(this.chestPos).method_27852(class_2246.field_10443)) {
            class_243 var1 = class_243.method_24953((class_2382)this.chestPos);
            if (this.mc.field_1724.method_33571().method_1025(var1) <= 18.49) {
                this.stopWalking();
                this.setState(ProtectionState.STASH_OPEN);
            } else if (this.stateTicks > 400 || !this.walkTowards(var1)) {
                this.stashFailed("could not walk to the ender chest");
            }
        } else {
            this.stashFailed("the ender chest is gone");
        }
    }

    private void tickStashOpen() {
        this.mc.field_1690.field_1832.method_23481(false);
        if (this.mc.field_1724.field_7512 instanceof class_1707) {
            this.setState(ProtectionState.STASH_DEPOSIT);
            this.actionDelay = 3;
        } else if (this.chestPos == null || !this.mc.field_1687.method_8320(this.chestPos).method_27852(class_2246.field_10443)) {
            this.stashFailed("the ender chest is gone");
        } else if (!this.mc.field_1724.method_5715()) {
            if (this.stateTicks > 60) {
                this.stashFailed("could not open the ender chest");
            } else {
                boolean var1 = this.lookAt(class_243.method_24953((class_2382)this.chestPos));
                class_3965 var2 = this.crosshairBlock();
                if (var1 && var2 != null && var2.method_17777().equals((Object)this.chestPos) && this.stateTicks >= this.nextOpenAttempt) {
                    this.mc.field_1761.method_2896(this.mc.field_1724, class_1268.field_5808, var2);
                    this.mc.field_1724.method_6104(class_1268.field_5808);
                    this.nextOpenAttempt = this.stateTicks + 20;
                }
            }
        }
    }

    private void tickStashDeposit() {
        class_1703 class_17032 = this.mc.field_1724.field_7512;
        if (class_17032 instanceof class_1707) {
            int var5;
            class_1707 var1 = (class_1707)class_17032;
            for (int var3 = var5 = var1.method_17388() * 9; var3 < var1.field_7761.size(); ++var3) {
                class_1799 var4 = var1.method_7611(var3).method_7677();
                if (!var4.method_31574(class_1802.field_8849)) continue;
                if (this.hasRoomFor(var1, var5, var4)) {
                    this.mc.field_1761.method_2906(var1.field_7763, var3, 0, class_1713.field_7794, (class_1657)this.mc.field_1724);
                    this.actionDelay = 2 + this.random.nextInt(2);
                    return;
                }
                this.chestFull = true;
                this.alert("\u00a7eEnder chest is full, keeping the rest in your inventory");
                break;
            }
            this.mc.field_1724.method_7346();
            this.alert("\u00a7aStored spawners in the ender chest");
            this.chestPos = null;
            this.setState(ProtectionState.MINING);
            this.actionDelay = 2;
        } else {
            this.setState(ProtectionState.MINING);
        }
    }

    private boolean hasRoomFor(class_1707 var1, int var2, class_1799 var3) {
        for (int var4 = 0; var4 < var2; ++var4) {
            class_1799 var5 = var1.method_7611(var4).method_7677();
            if (!var5.method_7960() && (!class_1799.method_31577((class_1799)var5, (class_1799)var3) || var5.method_7947() >= var5.method_7914())) continue;
            return true;
        }
        return false;
    }

    private class_742 nearestThreat() {
        HashSet<String> var1 = new HashSet<String>();
        for (String var5 : this.whitelist.getValue().split(",")) {
            if (var5.isBlank()) continue;
            var1.add(var5.trim().toLowerCase(Locale.ROOT));
        }
        class_742 var9 = null;
        double var10 = Double.MAX_VALUE;
        for (class_742 var6 : this.mc.field_1687.method_18456()) {
            double var7;
            if (var6 == this.mc.field_1724 || var6.method_7325() || !SpawnerProtectModule.isRealPlayer(var6.method_5667()) || var1.contains(var6.method_5477().getString().toLowerCase(Locale.ROOT)) || !((var7 = this.mc.field_1724.method_5858((class_1297)var6)) < var10)) continue;
            var10 = var7;
            var9 = var6;
        }
        return var9;
    }

    private static boolean isRealPlayer(UUID var0) {
        return var0.version() == 4;
    }

    private void emergencyLogout(class_742 var1, double var2) {
        String var4 = var1.method_5477().getString();
        this.reset();
        if (this.mc.method_1562() != null) {
            this.mc.method_1562().method_48296().method_10747((class_2561)class_2561.method_43470((String)("[Spawner Protect] Logged out - " + var4 + " came within " + Math.round(var2) + " blocks")));
        }
    }

    private int findSilkPickaxe() {
        class_1661 var1 = this.mc.field_1724.method_31548();
        for (int var2 = 0; var2 < 9; ++var2) {
            class_1799 var3 = var1.method_5438(var2);
            if (!var3.method_31573(class_3489.field_42614) || !EnchantmentUtil.hasEnchantment(var3, class_1893.field_9099)) continue;
            return var2;
        }
        return -1;
    }

    private int countItem(class_1792 var1) {
        int var2 = 0;
        class_1661 var3 = this.mc.field_1724.method_31548();
        for (int var4 = 0; var4 < 36; ++var4) {
            class_1799 var5 = var3.method_5438(var4);
            if (!var5.method_31574(var1)) continue;
            var2 += var5.method_7947();
        }
        return var2;
    }

    private void warnNoPickaxe() {
        this.warnedNoPickaxe = true;
        this.mc.field_1724.method_5783((class_3414)class_3417.field_14624.comp_349(), 1.0f, 0.5f);
        this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)"\u00a7c[Spawner Protect] \u00a7fNot holding a silk touch pickaxe"), false);
        this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)"\u00a7cNot holding a silk touch pickaxe"), true);
    }

    private void warnNoChest() {
        this.warnedNoChest = true;
        this.mc.field_1724.method_5783((class_3414)class_3417.field_14624.comp_349(), 1.0f, 0.5f);
        this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)"\u00a7c[Spawner Protect] \u00a7fNo ender chest placed nearby - will mine everything and log out when done"), false);
        this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)"\u00a7cNo ender chest placed nearby"), true);
    }

    private void alert(String var1) {
        if (this.chatAlerts.getValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a76[Spawner Protect] \u00a7f" + var1)), false);
        }
    }

    private void setState(ProtectionState var1) {
        this.state = var1;
        this.stateTicks = 0;
        this.nextOpenAttempt = 0;
    }

    private static enum ProtectionState {
        IDLE,
        MINING,
        STASH_WALK,
        STASH_OPEN,
        STASH_DEPOSIT,
        DONE;

    }
}

