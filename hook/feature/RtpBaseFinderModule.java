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
 *  net.minecraft.class_1893
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
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
 *  net.minecraft.class_634
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
import hook.module.setting.ModeSetting;
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
import java.util.Random;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1893;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
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
import net.minecraft.class_634;

public final class RtpBaseFinderModule
extends Module {
    public final ModeSetting<Region> mode = new ModeSetting<Region>(EncryptedString.of("Mode"), Region.RANDOM, Region.class);
    private final BooleanSetting spawn = new BooleanSetting(EncryptedString.of("Spawners"), true);
    private final NumberSetting minStorage = new NumberSetting(EncryptedString.of("Minimum Storage"), 1.0, 500.0, 100.0, 1.0);
    private final BooleanSetting autoTotemBuy = new BooleanSetting(EncryptedString.of("Auto Totem Buy"), true);
    private final NumberSetting totemSlot = new NumberSetting(EncryptedString.of("Totem Slot"), 1.0, 9.0, 8.0, 1.0);
    private final BooleanSetting autoMend = new BooleanSetting(EncryptedString.of("Auto Mend"), true).setDescription(EncryptedString.of("Automatically repairs pickaxe."));
    private final NumberSetting xpBottleSlot = new NumberSetting(EncryptedString.of("XP Bottle Slot"), 1.0, 9.0, 9.0, 1.0);
    private final BooleanSetting discordNotification = new BooleanSetting(EncryptedString.of("Discord Notification"), false);
    private final StringSetting webhook = new StringSetting(EncryptedString.of("Webhook"), "");
    private final BooleanSetting totemCheck = new BooleanSetting(EncryptedString.of("Totem Check"), true);
    private final NumberSetting totemCheckTime = new NumberSetting(EncryptedString.of("Totem Check Time"), 1.0, 120.0, 20.0, 1.0);
    private final NumberSetting digToY = new NumberSetting(EncryptedString.of("Dig To Y"), -59.0, 30.0, -20.0, 1.0);
    private class_243 currentPosition;
    private class_243 previousPosition;
    private double idleTime;
    private double totemCheckCounter;
    private boolean isDigging;
    private boolean shouldDig;
    private boolean isRepairing;
    private boolean isBuyingTotem;
    private int selectedSlot;
    private int rtpCooldown;
    private int actionDelay;
    private int totemBuyCounter;
    private int spawnerCounter;

    public RtpBaseFinderModule() {
        super(EncryptedString.of("Rtp Base Finder"), EncryptedString.of("Automatically searches for bases on DonutSMP"), -1, Category.DONUT);
        this.addSettings(this.mode, this.spawn, this.minStorage, this.autoTotemBuy, this.totemSlot, this.autoMend, this.xpBottleSlot, this.discordNotification, this.webhook, this.totemCheck, this.totemCheckTime, this.digToY);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 != null) {
            if (this.actionDelay > 0) {
                --this.actionDelay;
            } else {
                this.scanForEntities();
                if (this.autoTotemBuy.getValue()) {
                    int slot = this.totemSlot.getIntValue() - 1;
                    if (!this.mc.field_1724.method_31548().method_5438(slot).method_31574(class_1802.field_8288)) {
                        if (this.totemBuyCounter < 30 && !this.isBuyingTotem) {
                            ++this.totemBuyCounter;
                            return;
                        }
                        this.totemBuyCounter = 0;
                        this.isBuyingTotem = true;
                        if (this.mc.field_1724.method_31548().method_67532() != slot) {
                            InventoryUtil.swap(slot);
                        }
                        class_1703 var9 = this.mc.field_1724.field_7512;
                        if (this.mc.field_1724.field_7512 instanceof class_1707 && ((class_1707)var9).method_17388() == 3) {
                            if (var9.method_7611(11).method_7677().method_31574(class_1802.field_20399)) {
                                this.mc.field_1761.method_2906(var9.field_7763, 13, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                                this.actionDelay = 10;
                                return;
                            }
                            if (var9.method_7611(16).method_7677().method_31574(class_1802.field_8287)) {
                                this.mc.field_1761.method_2906(var9.field_7763, 13, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                                this.actionDelay = 10;
                                return;
                            }
                            this.dropHeldStack();
                            if (var9.method_7611(23).method_7677().method_31574(class_1802.field_8581)) {
                                this.mc.field_1761.method_2906(var9.field_7763, 23, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                                this.actionDelay = 10;
                                return;
                            }
                        }
                        this.openShop();
                        return;
                    }
                    if (this.isBuyingTotem) {
                        if (this.mc.field_1755 != null) {
                            this.mc.field_1724.method_7346();
                            this.actionDelay = 20;
                        }
                        this.isBuyingTotem = false;
                        this.totemBuyCounter = 0;
                    }
                }
                if (this.isRepairing) {
                    int slotx = this.xpBottleSlot.getIntValue() - 1;
                    class_1799 stack = this.mc.field_1724.method_31548().method_5438(slotx);
                    if (this.mc.field_1724.method_31548().method_67532() != slotx) {
                        InventoryUtil.swap(slotx);
                    }
                    if (!stack.method_31574(class_1802.field_8287)) {
                        class_1703 handler = this.mc.field_1724.field_7512;
                        if (!(this.mc.field_1724.field_7512 instanceof class_1707) || ((class_1707)handler).method_17388() != 3) {
                            this.openShop();
                        } else if (handler.method_7611(11).method_7677().method_31574(class_1802.field_20399)) {
                            this.mc.field_1761.method_2906(handler.field_7763, 13, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                            this.actionDelay = 10;
                        } else if (handler.method_7611(16).method_7677().method_31574(class_1802.field_8287)) {
                            this.mc.field_1761.method_2906(handler.field_7763, 16, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                            this.actionDelay = 10;
                        } else if (handler.method_7611(17).method_7677().method_31574(class_1802.field_8581)) {
                            this.mc.field_1761.method_2906(handler.field_7763, 17, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                            this.actionDelay = 10;
                        } else {
                            this.dropHeldStack();
                            if (handler.method_7611(23).method_7677().method_31574(class_1802.field_8581)) {
                                this.mc.field_1761.method_2906(handler.field_7763, 23, 0, class_1713.field_7790, (class_1657)this.mc.field_1724);
                                this.actionDelay = 10;
                            } else {
                                this.openShop();
                            }
                        }
                    } else if (this.mc.field_1755 != null) {
                        this.mc.field_1724.method_7346();
                        this.actionDelay = 20;
                    } else if (!EnchantmentUtil.hasEnchantment(this.mc.field_1724.method_6079(), class_1893.field_9101)) {
                        this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 36 + this.selectedSlot, 40, class_1713.field_7791, (class_1657)this.mc.field_1724);
                        this.actionDelay = 20;
                    } else if (this.mc.field_1724.method_6079().method_7919() > 0) {
                        if (this.mc.field_1761.method_2919((class_1657)this.mc.field_1724, class_1268.field_5808).method_23665()) {
                            this.mc.field_1724.method_6104(class_1268.field_5808);
                        }
                        this.actionDelay = 1;
                    } else {
                        this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 36 + this.selectedSlot, 40, class_1713.field_7791, (class_1657)this.mc.field_1724);
                        this.isRepairing = false;
                    }
                } else {
                    if (this.shouldDig) {
                        this.handleAutoEat();
                    }
                    if (this.totemCheck.getValue()) {
                        boolean hasTotem = this.mc.field_1724.method_6079().method_7909().equals(class_1802.field_8288);
                        Module autoTotem = Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(AutoTotemModule.class);
                        this.totemCheckCounter = !(hasTotem || autoTotem.isEnabled() && ((AutoTotemModule)autoTotem).findItemSlot(class_1802.field_8288) != -1) ? (this.totemCheckCounter += 1.0) : 0.0;
                        if (this.totemCheckCounter > this.totemCheckTime.getValue()) {
                            this.notifyTotemExplosion("Your totem exploded", (int)this.mc.field_1724.method_23317(), (int)this.mc.field_1724.method_23318(), (int)this.mc.field_1724.method_23321());
                            return;
                        }
                    }
                    if (this.rtpCooldown > 0) {
                        --this.rtpCooldown;
                        if (this.rtpCooldown <= 0) {
                            class_1799 var6;
                            if (this.previousPosition != null && this.previousPosition.method_1022(this.mc.field_1724.method_73189()) < 100.0) {
                                this.sendRtpCommand();
                                return;
                            }
                            this.mc.field_1724.method_36457(89.9f);
                            if (this.autoMend.getValue() && EnchantmentUtil.hasEnchantment(var6 = this.mc.field_1724.method_6047(), class_1893.field_9101) && var6.method_7936() - var6.method_7919() < 100) {
                                this.isRepairing = true;
                                this.selectedSlot = this.mc.field_1724.method_31548().method_67532();
                            }
                            this.shouldDig = true;
                        }
                    } else {
                        if (this.currentPosition != null && this.currentPosition.method_1022(this.mc.field_1724.method_73189()) < 2.0) {
                            this.idleTime += 1.0;
                        } else {
                            this.currentPosition = this.mc.field_1724.method_73189();
                            this.idleTime = 0.0;
                        }
                        if (this.idleTime > 20.0 && this.isDigging) {
                            this.sendRtpCommand();
                            this.isDigging = false;
                        } else if (this.idleTime > 200.0) {
                            this.sendRtpCommand();
                            this.idleTime = 0.0;
                        } else if (this.mc.field_1724.method_23318() < (double)this.digToY.getIntValue() && !this.isDigging) {
                            this.isDigging = true;
                            this.shouldDig = false;
                        }
                    }
                }
            }
        }
    }

    private void openShop() {
        this.mc.method_1562().method_45730("shop");
        this.actionDelay = 10;
    }

    private void dropHeldStack() {
        this.mc.field_1724.field_3944.method_52787((class_2596)new class_2846(class_2846.class_2847.field_12970, class_2338.field_10980, class_2350.field_11033));
    }

    private void sendRtpCommand() {
        this.shouldDig = false;
        class_634 var1 = this.mc.method_1562();
        Region var2 = this.mode.getValue() == Region.RANDOM ? this.getRandomMode() : (Region)this.mode.getValue();
        var1.method_45730("rtp " + this.getModeName(var2));
        this.rtpCooldown = 150;
        this.idleTime = 0.0;
        this.previousPosition = this.mc.field_1724.method_73189();
    }

    private void disconnectWithMessage(class_2561 var1) {
        class_5250 var2 = class_2561.method_43470((String)"[RTPBaseFinder] ");
        var2.method_10852(var1);
        this.toggle();
        this.mc.field_1724.field_3944.method_52781(new class_2661((class_2561)var2));
    }

    private Region getRandomMode() {
        Region[] var10000 = new Region[]{Region.EUCENTRAL, Region.EUWEST, Region.EAST, Region.WEST, Region.ASIA, Region.OCEANIA};
        Region[] var1 = var10000;
        return var1[new Random().nextInt(var1.length)];
    }

    private String getModeName(Region mode) {
        return switch (mode.ordinal()) {
            case 0 -> "eu central";
            case 1 -> "eu west";
            default -> mode.name();
        };
    }

    private void handleAutoEat() {
        Module var1 = Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(AutoEatModule.class);
        if (!var1.isEnabled()) {
            this.handleBlockBreaking(true);
        } else if (!((AutoEatModule)var1).shouldEat()) {
            this.handleBlockBreaking(true);
        }
    }

    private void handleBlockBreaking(boolean var1) {
        if (this.mc.field_1724.method_36455() != 89.9f) {
            this.mc.field_1724.method_36457(89.9f);
        }
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

    private void scanForEntities() {
        int var1 = 0;
        int var2 = 0;
        class_2338 var3 = null;
        Iterator var4 = BlockUtil.getLoadedChunks().iterator();
        while (var4.hasNext()) {
            for (Object var6 : ((class_2818)var4.next()).method_12021()) {
                String var8;
                class_2586 var7 = this.mc.field_1687.method_8321((class_2338)var6);
                if (this.spawn.getValue() && var7 instanceof class_2636 && !(var8 = ((MobSpawnerLogicAccessor)((class_2636)var7).method_11390()).getSpawnEntry().method_38093().method_10558("id").orElse("")).equals("minecraft:cave_spider") && !var8.equals("minecraft:spider")) {
                    ++var2;
                    var3 = (class_2338)var6;
                }
                if (!(var7 instanceof class_2595) && !(var7 instanceof class_2611) && !(var7 instanceof class_2627) && !(var7 instanceof class_3866) && !(var7 instanceof class_3719) && !(var7 instanceof class_2605)) continue;
                ++var1;
            }
        }
        this.spawnerCounter = var2 > 0 ? ++this.spawnerCounter : 0;
        if (this.spawnerCounter > 10) {
            this.notifyBaseOrSpawner("YOU FOUND SPAWNER", var3.method_10263(), var3.method_10264(), var3.method_10260(), false);
            this.spawnerCounter = 0;
        }
        if (var1 > this.minStorage.getIntValue()) {
            this.notifyBaseOrSpawner("YOU FOUND BASE", (int)this.mc.field_1724.method_23317(), (int)this.mc.field_1724.method_23318(), (int)this.mc.field_1724.method_23321(), true);
        }
    }

    private void notifyBaseOrSpawner(String var1, int var2, int var3, int var4, boolean var5) {
        String var6 = var5 ? "Base" : "Spawner";
        if (this.discordNotification.getValue()) {
            DiscordWebhook var7 = new DiscordWebhook(this.webhook.value);
            DiscordWebhook.EmbedObject var8 = new DiscordWebhook.EmbedObject();
            var8.setTitle(var6);
            var8.setThumbnail("https://render.crafty.gg/3d/bust/" + String.valueOf(class_310.method_1551().method_1548().method_44717()) + "?format=webp");
            var8.setDescription(var6 + " Found - " + class_310.method_1551().method_1548().method_1676());
            var8.setColor(Color.GRAY);
            var8.setFooter(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), null);
            var8.addField(var6 + "Found at", "x: " + var2 + " y: " + var3 + " z: " + var4, true);
            var7.addEmbed(var8);
            try {
                var7.execute();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        this.toggle();
        this.disconnectWithMessage(class_2561.method_30163((String)var1));
    }

    private void notifyTotemExplosion(String var1, int var2, int var3, int var4) {
        if (this.discordNotification.getValue()) {
            DiscordWebhook var5 = new DiscordWebhook(this.webhook.value);
            DiscordWebhook.EmbedObject var6 = new DiscordWebhook.EmbedObject();
            var6.setTitle("Totem Exploded");
            var6.setThumbnail("https://render.crafty.gg/3d/bust/" + String.valueOf(class_310.method_1551().method_1548().method_44717()) + "?format=webp");
            var6.setDescription("Your Totem Exploded - " + class_310.method_1551().method_1548().method_1676());
            var6.setColor(Color.RED);
            var6.setFooter(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), null);
            var6.addField("Location", "x: " + var2 + " y: " + var3 + " z: " + var4, true);
            var5.addEmbed(var6);
            try {
                var5.execute();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        this.disconnectWithMessage(class_2561.method_30163((String)var1));
    }

    public boolean isRepairingActive() {
        return this.isRepairing;
    }

    static enum Region {
        EUCENTRAL("eucentral", 0),
        EUWEST("euwest", 1),
        EAST("east", 2),
        WEST("west", 3),
        ASIA("asia", 4),
        OCEANIA("oceania", 5),
        RANDOM("random", 6);


        private Region(String name, int ordinal) {
        }
    }
}

