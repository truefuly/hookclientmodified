/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1657
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1893
 *  net.minecraft.class_1922
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2382
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2586
 *  net.minecraft.class_2595
 *  net.minecraft.class_2596
 *  net.minecraft.class_2605
 *  net.minecraft.class_2611
 *  net.minecraft.class_2627
 *  net.minecraft.class_2636
 *  net.minecraft.class_2661
 *  net.minecraft.class_2818
 *  net.minecraft.class_2846
 *  net.minecraft.class_2846$class_2847
 *  net.minecraft.class_310
 *  net.minecraft.class_3719
 *  net.minecraft.class_3866
 *  net.minecraft.class_3965
 *  net.minecraft.class_5250
 */
package hook.feature;

import hook.Hook;
import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.feature.AutoEatModule;
import hook.feature.AutoTotemModule;
import hook.mixin.MobSpawnerLogicAccessor;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.StringSetting;
import hook.utils.BlockUtil;
import hook.utils.EnchantmentUtil;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import hook.utils.embed.DiscordWebhook;
import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1893;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2596;
import net.minecraft.class_2605;
import net.minecraft.class_2611;
import net.minecraft.class_2627;
import net.minecraft.class_2636;
import net.minecraft.class_2661;
import net.minecraft.class_2818;
import net.minecraft.class_2846;
import net.minecraft.class_310;
import net.minecraft.class_3719;
import net.minecraft.class_3866;
import net.minecraft.class_3965;
import net.minecraft.class_5250;

public final class TunnelBaseFinderModule
extends Module {
    private final NumberSetting minimumStorage = new NumberSetting(EncryptedString.of("Minimum Storage"), 1.0, 500.0, 100.0, 1.0);
    private final BooleanSetting spawners = new BooleanSetting(EncryptedString.of("Spawners"), true);
    private final BooleanSetting autoTotemBuy = new BooleanSetting(EncryptedString.of("Auto Totem Buy"), true);
    private final NumberSetting totemSlot = new NumberSetting(EncryptedString.of("Totem Slot"), 1.0, 9.0, 8.0, 1.0);
    private final BooleanSetting autoMend = new BooleanSetting(EncryptedString.of("Auto Mend"), true).setDescription(EncryptedString.of("Automatically repairs pickaxe."));
    private final NumberSetting xpBottleSlot = new NumberSetting(EncryptedString.of("XP Bottle Slot"), 1.0, 9.0, 9.0, 1.0);
    private final BooleanSetting discordNotification = new BooleanSetting(EncryptedString.of("Discord Notification"), false);
    private final StringSetting webhook = new StringSetting(EncryptedString.of("Webhook"), "");
    private final BooleanSetting totemCheck = new BooleanSetting(EncryptedString.of("Totem Check"), true);
    private final NumberSetting totemCheckTime = new NumberSetting(EncryptedString.of("Totem Check Time"), 1.0, 120.0, 20.0, 1.0);
    private TunnelDirection currentDirection;
    private int blocksMined;
    private int spawnerCount;
    private int idleTicks;
    private class_243 lastPosition;
    private boolean isDigging;
    private boolean shouldDig;
    private int totemCheckCounter;
    private int totemBuyCounter;
    private double actionDelay;
    private int scanCooldown;

    public TunnelBaseFinderModule() {
        super(EncryptedString.of("Tunnel Base Finder"), EncryptedString.of("Finds bases digging tunnel"), -1, Category.DONUT);
        this.addSettings(this.minimumStorage, this.spawners, this.autoTotemBuy, this.totemSlot, this.autoMend, this.xpBottleSlot, this.discordNotification, this.webhook, this.totemCheck, this.totemCheckTime);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.currentDirection = this.mc.field_1724 == null ? null : this.getInitialDirection();
        this.blocksMined = 0;
        this.idleTicks = 0;
        this.spawnerCount = 0;
        this.lastPosition = null;
        this.isDigging = false;
        this.shouldDig = false;
        this.totemCheckCounter = 0;
        this.totemBuyCounter = 0;
        this.actionDelay = 0.0;
        this.scanCooldown = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.currentDirection = null;
        this.isDigging = false;
        this.shouldDig = false;
        this.mc.field_1690.field_1913.method_23481(false);
        this.mc.field_1690.field_1849.method_23481(false);
        this.mc.field_1690.field_1894.method_23481(false);
        if (this.mc.field_1761 != null) {
            this.mc.field_1761.method_2925();
        }
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.field_1761 != null) {
            Module var2;
            if (this.currentDirection == null) {
                this.currentDirection = this.getInitialDirection();
            }
            if (!(this.currentDirection == null || (var2 = Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(AutoEatModule.class)).isEnabled() && ((AutoEatModule)var2).shouldEat())) {
                int var3 = (this.calculateDirection(this.currentDirection) + 90 * this.idleTicks) % 360;
                if (this.mc.field_1724.method_36454() != (float)var3) {
                    this.mc.field_1724.method_36456((float)var3);
                }
                if (this.mc.field_1724.method_36455() != 2.0f) {
                    this.mc.field_1724.method_36457(2.0f);
                }
                this.updateDirection(this.getInitialDirection());
                if (this.blocksMined > 0) {
                    this.mc.field_1690.field_1894.method_23481(false);
                    --this.blocksMined;
                } else {
                    if (++this.scanCooldown >= 20) {
                        this.scanCooldown = 0;
                        this.notifyFound();
                        if (!this.isEnabled()) {
                            return;
                        }
                    }
                    if (this.autoTotemBuy.getValue()) {
                        int var4 = this.totemSlot.getIntValue() - 1;
                        if (!this.mc.field_1724.method_31548().method_5438(var4).method_31574(class_1802.field_8288)) {
                            if (this.totemBuyCounter < 30 && !this.shouldDig) {
                                ++this.totemBuyCounter;
                                return;
                            }
                            this.totemBuyCounter = 0;
                            this.shouldDig = true;
                            if (this.mc.field_1724.method_31548().method_67532() != var4) {
                                InventoryUtil.swap(var4);
                            }
                            class_1703 var18 = this.mc.field_1724.field_7512;
                            if (this.mc.field_1724.field_7512 instanceof class_1707 && ((class_1707)var18).method_17388() == 3) {
                                if (var18.method_7611(11).method_7677().method_31574(class_1802.field_20399)) {
                                    this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 13, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                                    this.blocksMined = 10;
                                    return;
                                }
                                if (var18.method_7611(16).method_7677().method_31574(class_1802.field_8287)) {
                                    this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 13, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                                    this.blocksMined = 10;
                                    return;
                                }
                                this.mc.field_1724.field_3944.method_52787((class_2596)new class_2846(class_2846.class_2847.field_12970, class_2338.field_10980, class_2350.field_11033));
                                if (var18.method_7611(23).method_7677().method_31574(class_1802.field_8581)) {
                                    this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 23, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                                    this.blocksMined = 10;
                                    return;
                                }
                                this.mc.method_1562().method_45730("shop");
                                this.blocksMined = 10;
                                return;
                            }
                            this.mc.method_1562().method_45730("shop");
                            this.blocksMined = 10;
                            return;
                        }
                        if (this.shouldDig) {
                            if (this.mc.field_1755 != null) {
                                this.mc.field_1724.method_7346();
                                this.blocksMined = 20;
                            }
                            this.shouldDig = false;
                            this.totemBuyCounter = 0;
                        }
                    }
                    if (this.isDigging) {
                        int var12 = this.xpBottleSlot.getIntValue() - 1;
                        class_1799 var5 = this.mc.field_1724.method_31548().method_5438(var12);
                        if (this.mc.field_1724.method_31548().method_67532() != var12) {
                            InventoryUtil.swap(var12);
                        }
                        if (!var5.method_31574(class_1802.field_8287)) {
                            class_1703 var6 = this.mc.field_1724.field_7512;
                            if (!(this.mc.field_1724.field_7512 instanceof class_1707) || ((class_1707)var6).method_17388() != 3) {
                                this.mc.method_1562().method_45730("shop");
                                this.blocksMined = 10;
                                return;
                            }
                            if (var6.method_7611(11).method_7677().method_31574(class_1802.field_20399)) {
                                this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 13, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                                this.blocksMined = 10;
                                return;
                            }
                            if (var6.method_7611(16).method_7677().method_31574(class_1802.field_8287)) {
                                this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 16, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                                this.blocksMined = 10;
                                return;
                            }
                            if (var6.method_7611(17).method_7677().method_31574(class_1802.field_8581)) {
                                this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 17, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                                this.blocksMined = 10;
                                return;
                            }
                            this.mc.field_1724.field_3944.method_52787((class_2596)new class_2846(class_2846.class_2847.field_12970, class_2338.field_10980, class_2350.field_11033));
                            if (var6.method_7611(23).method_7677().method_31574(class_1802.field_8581)) {
                                this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 23, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                                this.blocksMined = 10;
                                return;
                            }
                            this.mc.method_1562().method_45730("shop");
                            this.blocksMined = 10;
                        } else {
                            if (this.mc.field_1755 != null) {
                                this.mc.field_1724.method_7346();
                                this.blocksMined = 20;
                                return;
                            }
                            if (!EnchantmentUtil.hasEnchantment(this.mc.field_1724.method_6079(), class_1893.field_9101)) {
                                this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 36 + this.totemCheckCounter, 40, class_1713.field_7791, (class_1657)this.mc.field_1724);
                                this.blocksMined = 20;
                                return;
                            }
                            if (this.mc.field_1724.method_6079().method_7919() > 0) {
                                class_1269 var19 = this.mc.field_1761.method_2919((class_1657)this.mc.field_1724, class_1268.field_5808);
                                if (var19.method_23665()) {
                                    this.mc.field_1724.method_6104(class_1268.field_5808);
                                }
                                this.blocksMined = 1;
                                return;
                            }
                            this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 36 + this.totemCheckCounter, 40, class_1713.field_7791, (class_1657)this.mc.field_1724);
                            this.isDigging = false;
                        }
                    } else {
                        class_2338 var20;
                        class_1799 var13;
                        if (this.autoMend.getValue() && EnchantmentUtil.hasEnchantment(var13 = this.mc.field_1724.method_6047(), class_1893.field_9101) && var13.method_7936() - var13.method_7919() < 100) {
                            this.isDigging = true;
                            this.totemCheckCounter = this.mc.field_1724.method_31548().method_67532();
                        }
                        if (this.totemCheck.getValue()) {
                            boolean var14 = this.mc.field_1724.method_6079().method_7909().equals(class_1802.field_8288);
                            Module var16 = Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(AutoTotemModule.class);
                            this.actionDelay = var14 ? 0.0 : (var16.isEnabled() && ((AutoTotemModule)var16).findItemSlot(class_1802.field_8288) != -1 ? 0.0 : (this.actionDelay += 1.0));
                            if (this.actionDelay > this.totemCheckTime.getValue()) {
                                this.notifyTotemExploded("Your totem exploded", (int)this.mc.field_1724.method_23317(), (int)this.mc.field_1724.method_23318(), (int)this.mc.field_1724.method_23321());
                                return;
                            }
                        }
                        boolean var15 = false;
                        class_239 var17 = this.mc.field_1765;
                        if (this.mc.field_1765 instanceof class_3965 && !BlockUtil.isBlockAtPosition(var20 = ((class_3965)var17).method_17777(), class_2246.field_10124)) {
                            var15 = this.isBlockPositionValid(var20, this.mc.field_1724.method_5735());
                        }
                        if (var15) {
                            this.handleBlockBreaking(true);
                        }
                        boolean var21 = this.isBlockInDirection(this.mc.field_1724.method_5735(), 3);
                        boolean var7 = false;
                        class_239 var8 = this.mc.field_1765;
                        if (this.mc.field_1765 instanceof class_3965) {
                            boolean bl = var7 = this.mc.field_1724.method_5836(1.0f).method_1022(class_243.method_24953((class_2382)((class_3965)var8).method_17777())) > 3.0;
                        }
                        if (!(var15 || var7 && var21)) {
                            ++this.idleTicks;
                            this.lastPosition = this.mc.field_1724.method_73189();
                            this.blocksMined = 5;
                            return;
                        }
                        this.mc.field_1690.field_1894.method_23481(var21 && var7);
                        if (this.idleTicks > 0 && this.lastPosition != null && this.mc.field_1724.method_73189().method_1022(this.lastPosition) > 1.0) {
                            this.lastPosition = this.mc.field_1724.method_73189();
                            class_2350 var9 = this.mc.field_1724.method_5735().method_10160();
                            class_2338 var10 = this.mc.field_1724.method_24515().method_10084().method_10093(var9);
                            for (int var11 = 0; var11 < 5; ++var11) {
                                if (this.mc.field_1687.method_8320(var10 = var10.method_10093(var9)).method_26204().equals(class_2246.field_10124)) continue;
                                if (this.isBlockPositionValid(var10, var9) && this.isBlockPositionValid(var10.method_10093(var9), var9)) {
                                    --this.idleTicks;
                                    this.blocksMined = 5;
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private int calculateDirection(TunnelDirection var1) {
        if (var1 == TunnelDirection.NORTH) {
            return 180;
        }
        if (var1 == TunnelDirection.SOUTH) {
            return 0;
        }
        if (var1 == TunnelDirection.EAST) {
            return 270;
        }
        return var1 == TunnelDirection.WEST ? 90 : Math.round(this.mc.field_1724.method_36454());
    }

    private boolean isBlockInDirection(class_2350 var1, int var2) {
        class_2338 var3 = this.mc.field_1724.method_24515().method_10074();
        class_2338 var4 = this.mc.field_1724.method_24515();
        for (int var5 = 0; var5 < var2; ++var5) {
            class_2338 var6 = var3.method_10079(var1, var5);
            class_2338 var7 = var4.method_10079(var1, var5);
            if (this.mc.field_1687.method_8320(var6).method_26215() || !this.isBlockPositionSafe(var6)) {
                return false;
            }
            if (this.mc.field_1687.method_8320(var7).method_26215()) continue;
            return false;
        }
        return true;
    }

    private boolean isBlockPositionValid(class_2338 var1, class_2350 var2) {
        if (var1 != null && var2 != null && this.mc.field_1724 != null && this.mc.field_1687 != null) {
            class_2338 var3 = this.mc.field_1724.method_24515();
            int var4 = var1.method_10263() - var3.method_10263();
            int var5 = var1.method_10260() - var3.method_10260();
            int var6 = var4 * var2.method_10148() + var5 * var2.method_10165();
            int var7 = var4 * var2.method_10165() - var5 * var2.method_10148();
            int var8 = var1.method_10264() - var3.method_10264();
            if (var6 >= 1 && var6 <= 4 && var7 == 0 && var8 >= 0 && var8 <= 1) {
                if (this.mc.field_1724.method_5836(1.0f).method_1025(class_243.method_24953((class_2382)var1)) > 25.0) {
                    return false;
                }
                if (!this.mc.field_1687.method_8320(var1).method_26215() && !(this.mc.field_1687.method_8320(var1).method_26214((class_1922)this.mc.field_1687, var1) < 0.0f)) {
                    class_2350 var9 = var2.method_10170();
                    class_2338[] var10000 = new class_2338[]{var1, var1.method_10084(), var1.method_10093(var2), var1.method_10093(var9), var1.method_10093(var9.method_10153())};
                    for (class_2338 var13 : var10000) {
                        if (this.isBlockPositionSafe(var13)) continue;
                        return false;
                    }
                    return this.mc.field_1687.method_8320(var1.method_10084()).method_26204() != class_2246.field_10255;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    private boolean isBlockPositionSafe(class_2338 var1) {
        return this.isBlockValid(this.mc.field_1687.method_8320(var1).method_26204());
    }

    private boolean isBlockValid(class_2248 var1) {
        return var1 != class_2246.field_10164 && var1 != class_2246.field_10382;
    }

    private void updateDirection(TunnelDirection var1) {
        double var2 = this.mc.field_1724.method_23317();
        double var4 = this.mc.field_1724.method_23321();
        double var6 = Math.floor(var4);
        double var8 = Math.floor(var2) + 0.5 - var2;
        double var10 = var6 + 0.5 - var4;
        this.mc.field_1690.field_1913.method_23481(false);
        this.mc.field_1690.field_1849.method_23481(false);
        boolean var12 = false;
        boolean var13 = false;
        if (var1 == TunnelDirection.SOUTH) {
            if (var8 > 0.1) {
                var13 = true;
            } else if (var8 < -0.1) {
                var12 = true;
            }
        }
        if (var1 == TunnelDirection.NORTH) {
            if (var8 > 0.1) {
                var12 = true;
            } else if (var8 < -0.1) {
                var13 = true;
            }
        }
        if (var1 == TunnelDirection.WEST) {
            if (var10 > 0.1) {
                var13 = true;
            } else if (var10 < -0.1) {
                var12 = true;
            }
        }
        if (var1 == TunnelDirection.EAST) {
            if (var10 > 0.1) {
                var12 = true;
            } else if (var10 < -0.1) {
                var13 = true;
            }
        }
        if (var12) {
            this.mc.field_1690.field_1849.method_23481(true);
        }
        if (var13) {
            this.mc.field_1690.field_1913.method_23481(true);
        }
    }

    private void handleBlockBreaking(boolean var1) {
        if (!this.mc.field_1724.method_6115()) {
            if (var1 && this.mc.field_1765 != null && this.mc.field_1765.method_17783() == class_239.class_240.field_1332) {
                class_2350 var4;
                class_3965 var2 = (class_3965)this.mc.field_1765;
                class_2338 var3 = ((class_3965)this.mc.field_1765).method_17777();
                if (!this.mc.field_1687.method_8320(var3).method_26215() && this.mc.field_1761.method_2902(var3, var4 = var2.method_17780())) {
                    this.mc.field_1724.method_6104(class_1268.field_5808);
                }
            } else {
                this.mc.field_1761.method_2925();
            }
        }
    }

    private TunnelDirection getInitialDirection() {
        float var1 = this.mc.field_1724.method_36454() % 360.0f;
        if (var1 < 0.0f) {
            var1 += 360.0f;
        }
        if (var1 >= 45.0f && var1 < 135.0f) {
            return TunnelDirection.WEST;
        }
        if (var1 >= 135.0f && var1 < 225.0f) {
            return TunnelDirection.NORTH;
        }
        return var1 >= 225.0f && var1 < 315.0f ? TunnelDirection.EAST : TunnelDirection.SOUTH;
    }

    private void notifyFound() {
        int var1 = 0;
        int var2 = 0;
        class_2338 var3 = null;
        Iterator var4 = BlockUtil.getLoadedChunks().iterator();
        while (var4.hasNext()) {
            for (Object var6 : ((class_2818)var4.next()).method_12021()) {
                String var8;
                class_2586 var7 = this.mc.field_1687.method_8321((class_2338)var6);
                if (this.spawners.getValue() && var7 instanceof class_2636 && !(var8 = ((MobSpawnerLogicAccessor)((class_2636)var7).method_11390()).getSpawnEntry().method_38093().method_10558("id").orElse("")).equals("minecraft:cave_spider") && !var8.equals("minecraft:spider")) {
                    ++var2;
                    var3 = (class_2338)var6;
                }
                if (!(var7 instanceof class_2595) && !(var7 instanceof class_2611)) {
                    if (var7 instanceof class_2627) {
                        ++var1;
                        continue;
                    }
                    if (!(var7 instanceof class_3866) && !(var7 instanceof class_3719) && !(var7 instanceof class_2605)) continue;
                }
                ++var1;
            }
        }
        this.spawnerCount = var2 > 0 ? ++this.spawnerCount : 0;
        if (this.spawnerCount > 10) {
            this.notifyFound("YOU FOUND SPAWNER", var3.method_10263(), var3.method_10264(), var3.method_10260(), false);
            this.spawnerCount = 0;
        }
        if (var1 > this.minimumStorage.getIntValue()) {
            this.notifyFound("YOU FOUND BASE", (int)this.mc.field_1724.method_23317(), (int)this.mc.field_1724.method_23318(), (int)this.mc.field_1724.method_23321(), true);
        }
    }

    private void notifyTotemExploded(String message, int x, int y, int z) {
        if (this.discordNotification.getValue()) {
            DiscordWebhook webhookMessage = new DiscordWebhook(this.webhook.value);
            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
            embed.setTitle("Totem Exploded");
            embed.setThumbnail(String.valueOf(class_310.method_1551().method_1548().method_44717()));
            embed.setDescription("Your Totem Exploded - " + class_310.method_1551().method_1548().method_1676());
            embed.setColor(Color.RED);
            embed.setFooter(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), null);
            embed.addField("Location", "x: " + x + " y: " + y + " z: " + z, true);
            webhookMessage.addEmbed(embed);
            TunnelBaseFinderModule.sendWebhookAsync(webhookMessage);
        }
        this.disconnectWithMessage(class_2561.method_30163((String)message));
    }

    private void notifyFound(String message, int x, int y, int z, boolean base) {
        String type;
        String string = type = base ? "Base" : "Spawner";
        if (this.discordNotification.getValue()) {
            DiscordWebhook webhookMessage = new DiscordWebhook(this.webhook.value);
            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
            embed.setTitle(type);
            embed.setThumbnail(String.valueOf(class_310.method_1551().method_1548().method_44717()));
            embed.setDescription(type + " Found - " + class_310.method_1551().method_1548().method_1676());
            embed.setColor(Color.GRAY);
            embed.setFooter(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), null);
            embed.addField(type + "Found at", "x: " + x + " y: " + y + " z: " + z, true);
            webhookMessage.addEmbed(embed);
            TunnelBaseFinderModule.sendWebhookAsync(webhookMessage);
        }
        this.disconnectWithMessage(class_2561.method_30163((String)message));
    }

    private void disconnectWithMessage(class_2561 message) {
        class_5250 notice = class_2561.method_43470((String)"[TunnelBaseFinder] ");
        notice.method_10852(message);
        if (this.isEnabled()) {
            this.toggle();
        }
        if (this.mc.field_1724 != null && this.mc.field_1724.field_3944 != null) {
            this.mc.field_1724.field_3944.method_52781(new class_2661((class_2561)notice));
        }
    }

    private static void sendWebhookAsync(DiscordWebhook webhookMessage) {
        CompletableFuture.runAsync(() -> {
            try {
                webhookMessage.execute();
            }
            catch (Exception error) {
                System.err.println("Tunnel Base Finder notification failed: " + error.getMessage());
            }
        });
    }

    public boolean isDigging() {
        return this.isDigging;
    }

    static enum TunnelDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST;

    }
}

