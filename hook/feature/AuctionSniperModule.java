/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.class_1657
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1792$class_9635
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1836
 *  net.minecraft.class_2561
 *  net.minecraft.class_746
 */
package hook.feature;

import com.google.gson.JsonObject;
import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ItemSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1836;
import net.minecraft.class_2561;
import net.minecraft.class_746;

public final class AuctionSniperModule
extends Module {
    private final ItemSetting snipingItem = new ItemSetting(EncryptedString.of("Sniping Item"), class_1802.field_8162);
    private final StringSetting price = new StringSetting(EncryptedString.of("Price"), "1k");
    private final ModeSetting<SnipingMode> mode = new ModeSetting<SnipingMode>(EncryptedString.of("Mode"), SnipingMode.MANUAL, SnipingMode.class).setDescription(EncryptedString.of("Manual is faster but api doesnt require auction gui opened all the time"));
    private final StringSetting apiKey = new StringSetting(EncryptedString.of("Api Key"), "").setDescription(EncryptedString.of("You can get it by typing /api in chat"));
    private final NumberSetting refreshDelay = new NumberSetting(EncryptedString.of("Refresh Delay"), 0.0, 100.0, 2.0, 1.0);
    private final NumberSetting buyDelay = new NumberSetting(EncryptedString.of("Buy Delay"), 0.0, 100.0, 2.0, 1.0);
    private final NumberSetting apiRefreshRate = new NumberSetting(EncryptedString.of("API Refresh Rate"), 10.0, 5000.0, 250.0, 10.0).getValue(EncryptedString.of("How often to query the API (in milliseconds)"));
    private final BooleanSetting showApiNotifications = new BooleanSetting(EncryptedString.of("Show API Notifications"), true).setDescription(EncryptedString.of("Show chat notifications for API actions"));
    private int delayCounter;
    private boolean isProcessing;
    private long lastApiCallTimestamp;
    private final Map<String, Double> snipingItems = new HashMap<String, Double>();
    private boolean isApiQueryInProgress;
    private boolean isAuctionSniping;
    private int auctionPageCounter = -1;
    private String currentSellerName = "";

    public AuctionSniperModule() {
        super(EncryptedString.of("Auction Sniper"), EncryptedString.of("Snipes items on auction house for cheap"), -1, Category.MISC);
        this.addSettings(this.snipingItem, this.price, this.mode, this.apiKey, this.refreshDelay, this.buyDelay, this.apiRefreshRate, this.showApiNotifications);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        double var1 = this.parsePrice(this.price.getValue());
        if (var1 == -1.0) {
            if (this.mc.field_1724 != null) {
                class_746 var3 = this.mc.field_1724;
                var3.method_7353(class_2561.method_30163((String)"Invalid Price"), true);
            }
            this.toggle();
        } else {
            if (this.snipingItem.getItem() != class_1802.field_8162) {
                Map<String, Double> var4 = this.snipingItems;
                var4.put(this.snipingItem.getItem().toString(), var1);
            }
            this.lastApiCallTimestamp = 0L;
            this.isApiQueryInProgress = false;
            this.isAuctionSniping = false;
            this.currentSellerName = "";
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.isAuctionSniping = false;
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1724 != null) {
            if (this.delayCounter > 0) {
                --this.delayCounter;
            } else if (this.mode.isMode(SnipingMode.API)) {
                this.handleApiMode();
            } else if (this.mode.isMode(SnipingMode.MANUAL)) {
                class_1703 var2 = this.mc.field_1724.field_7512;
                if (!(this.mc.field_1724.field_7512 instanceof class_1707)) {
                    String[] var3 = this.snipingItem.getItem().method_7876().split("\\.");
                    String var4 = var3[var3.length - 1];
                    String var5 = Arrays.stream(var4.replace("_", " ").split(" ")).map(var0 -> var0.substring(0, 1).toUpperCase() + var0.substring(1)).collect(Collectors.joining(" "));
                    this.mc.method_1562().method_45730("ah " + var5);
                    this.delayCounter = 20;
                    return;
                }
                if (((class_1707)var2).method_17388() == 6) {
                    this.processSixRowAuction((class_1707)var2);
                } else if (((class_1707)var2).method_17388() == 3) {
                    this.processThreeRowAuction((class_1707)var2);
                }
            }
        }
    }

    private void handleApiMode() {
        if (!this.isAuctionSniping) {
            long var1;
            long var3;
            if (this.mc.field_1724.field_7512 instanceof class_1707 && this.mc.field_1755.method_25440().getString().contains("Page")) {
                this.mc.field_1724.method_7346();
                this.delayCounter = 20;
            } else if (!this.isApiQueryInProgress && (var3 = (var1 = System.currentTimeMillis()) - this.lastApiCallTimestamp) > (long)this.apiRefreshRate.getIntValue()) {
                this.lastApiCallTimestamp = var1;
                if (this.apiKey.getValue().isEmpty()) {
                    if (this.showApiNotifications.getValue()) {
                        class_746 var5 = this.mc.field_1724;
                        var5.method_7353(class_2561.method_30163((String)"\u00a7cAPI key is not set. Set it using /api in-game."), false);
                    }
                    return;
                }
                this.isApiQueryInProgress = true;
                this.queryApi().thenAccept(this::processApiResponse);
            }
        } else {
            class_1703 var6 = this.mc.field_1724.field_7512;
            if (!(this.mc.field_1724.field_7512 instanceof class_1707)) {
                if (this.auctionPageCounter != -1) {
                    if (this.auctionPageCounter <= 40) {
                        ++this.auctionPageCounter;
                    } else {
                        this.isAuctionSniping = false;
                        this.currentSellerName = "";
                    }
                } else {
                    this.mc.method_1562().method_45730("ah " + this.currentSellerName);
                    this.auctionPageCounter = 0;
                }
            } else {
                this.auctionPageCounter = -1;
                if (((class_1707)var6).method_17388() == 6) {
                    this.processSixRowAuction((class_1707)var6);
                } else if (((class_1707)var6).method_17388() == 3) {
                    this.processThreeRowAuction((class_1707)var6);
                }
            }
        }
    }

    private CompletableFuture<List<?>> queryApi() {
        return CompletableFuture.supplyAsync(() -> Collections.emptyList());
    }

    private void processApiResponse(List var1) {
        for (Object var3 : var1) {
            try {
                String var4 = ((JsonObject)var3).getAsJsonObject("item").get("id").getAsString();
                long var15 = ((JsonObject)var3).get("price").getAsLong();
                String var7 = ((JsonObject)var3).getAsJsonObject("seller").get("name").getAsString();
                for (Map.Entry<String, Double> var9 : this.snipingItems.entrySet()) {
                    String var10 = var9.getKey();
                    double var11 = var9.getValue();
                    if (!var4.contains(var10) || !((double)var15 <= var11)) continue;
                    if (this.showApiNotifications.getValue() && this.mc.field_1724 != null) {
                        class_746 var13 = this.mc.field_1724;
                        var13.method_7353(class_2561.method_30163((String)("\u00a7aFound " + var4 + " for " + this.formatPrice(var15) + " \u00a7r(threshold: " + this.formatPrice(var11) + ") \u00a7afrom seller: " + var7)), false);
                    }
                    this.isAuctionSniping = true;
                    this.currentSellerName = var7;
                    return;
                }
            }
            catch (Exception var14) {
                if (!this.showApiNotifications.getValue() || this.mc.field_1724 == null) continue;
                class_746 var5 = this.mc.field_1724;
                var5.method_7353(class_2561.method_30163((String)("\u00a7cError processing auction: " + var14.getMessage())), false);
            }
        }
    }

    private void processSixRowAuction(class_1707 var1) {
        class_1799 var2 = var1.method_7611(47).method_7677();
        if (var2.method_31574(class_1802.field_8162)) {
            this.delayCounter = 2;
        } else {
            for (Object var4 : var2.method_7950(class_1792.class_9635.field_51353, (class_1657)this.mc.field_1724, (class_1836)class_1836.field_41070)) {
                String var5 = var4.toString();
                if (!var5.contains("Recently Listed") || !((class_2561)var4).method_10866().toString().contains("white") && !var5.contains("white")) continue;
                this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 47, 1, class_1713.field_7794, (class_1657)this.mc.field_1724);
                this.delayCounter = 5;
                return;
            }
            for (int var6 = 0; var6 < 44; ++var6) {
                class_1799 var7 = var1.method_7611(var6).method_7677();
                if (!var7.method_31574(this.snipingItem.getItem()) || !this.isValidAuctionItem(var7)) continue;
                if (this.isProcessing) {
                    this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, var6, 1, class_1713.field_7794, (class_1657)this.mc.field_1724);
                    this.isProcessing = false;
                    return;
                }
                this.isProcessing = true;
                this.delayCounter = this.buyDelay.getIntValue();
                return;
            }
            if (this.isAuctionSniping) {
                this.isAuctionSniping = false;
                this.currentSellerName = "";
                this.mc.field_1724.method_7346();
            } else {
                this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 49, 1, class_1713.field_7794, (class_1657)this.mc.field_1724);
                this.delayCounter = this.refreshDelay.getIntValue();
            }
        }
    }

    private void processThreeRowAuction(class_1707 var1) {
        if (this.isValidAuctionItem(var1.method_7611(13).method_7677())) {
            this.mc.field_1761.method_2906(this.mc.field_1724.field_7512.field_7763, 15, 1, class_1713.field_7794, (class_1657)this.mc.field_1724);
            this.delayCounter = 20;
        }
        if (this.isAuctionSniping) {
            this.isAuctionSniping = false;
            this.currentSellerName = "";
        }
    }

    private double parseTooltipPrice(List var1) {
        if (var1 != null && !var1.isEmpty()) {
            Iterator var2 = var1.iterator();
            while (var2.hasNext()) {
                String var3 = ((class_2561)var2.next()).getString();
                if (!var3.matches("(?i).*price\\s*:\\s*\\$.*")) continue;
                String var4 = var3.replaceAll("[,$]", "");
                Matcher var5 = Pattern.compile("([\\d]+(?:\\.[\\d]+)?)\\s*([KMB])?", 2).matcher(var4);
                if (!var5.find()) continue;
                String var6 = var5.group(1);
                String var7 = var5.group(2) != null ? var5.group(2).toUpperCase() : "";
                return this.parsePrice(var6 + var7);
            }
            return -1.0;
        }
        return -1.0;
    }

    private boolean isValidAuctionItem(class_1799 var1) {
        List var2 = var1.method_7950(class_1792.class_9635.field_51353, (class_1657)this.mc.field_1724, (class_1836)class_1836.field_41070);
        double var3 = this.parseTooltipPrice(var2) / (double)var1.method_7947();
        double var5 = this.parsePrice(this.price.getValue());
        if (var5 == -1.0) {
            if (this.mc.field_1724 != null) {
                class_746 var10 = this.mc.field_1724;
                var10.method_7353(class_2561.method_30163((String)"Invalid Price"), true);
            }
            this.toggle();
            return false;
        }
        if (var3 != -1.0) {
            return var3 <= var5;
        }
        if (this.mc.field_1724 != null) {
            class_746 var7 = this.mc.field_1724;
            var7.method_7353(class_2561.method_30163((String)"Invalid Auction Item Price"), true);
            for (int var8 = 0; var8 < var2.size() - 1; ++var8) {
                PrintStream var9 = System.out;
                var9.println(var8 + ". " + ((class_2561)var2.get(var8)).getString());
            }
        }
        this.toggle();
        return false;
    }

    private double parsePrice(String var1) {
        if (var1 == null) {
            return -1.0;
        }
        if (var1.isEmpty()) {
            return -1.0;
        }
        String var2 = var1.trim().toUpperCase();
        double var3 = 1.0;
        if (var2.endsWith("B")) {
            var3 = 1.0E9;
            var2 = var2.substring(0, var2.length() - 1);
        } else if (var2.endsWith("M")) {
            var3 = 1000000.0;
            var2 = var2.substring(0, var2.length() - 1);
        } else if (var2.endsWith("K")) {
            var3 = 1000.0;
            var2 = var2.substring(0, var2.length() - 1);
        }
        try {
            return Double.parseDouble(var2) * var3;
        }
        catch (NumberFormatException var6) {
            return -1.0;
        }
    }

    private String formatPrice(double var1) {
        if (var1 >= 1.0E9) {
            Object[] var9 = new Object[]{var1 / 1.0E9};
            Object[] var6 = var9;
            return String.format("%.2fB", var6);
        }
        if (var1 >= 1000000.0) {
            Object[] var8 = new Object[]{var1 / 1000000.0};
            Object[] var5 = var8;
            return String.format("%.2fM", var5);
        }
        if (var1 >= 1000.0) {
            Object[] var7 = new Object[]{var1 / 1000.0};
            Object[] var4 = var7;
            return String.format("%.2fK", var4);
        }
        Object[] var10000 = new Object[]{var1};
        Object[] var3 = var10000;
        return String.format("%.2f", var3);
    }

    public static enum SnipingMode {
        API("API", 0),
        MANUAL("MANUAL", 1);


        private SnipingMode(String name, int ordinal) {
        }
    }
}

