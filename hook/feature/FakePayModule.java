/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_3417
 *  net.minecraft.class_5250
 *  net.minecraft.class_7472
 *  net.minecraft.class_9449
 */
package hook.feature;

import hook.Hook;
import hook.event.EventListener;
import hook.event.events.PacketSendEvent;
import hook.feature.FakeScoreboardModule;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.Setting;
import hook.utils.EncryptedString;
import java.util.Locale;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_3417;
import net.minecraft.class_5250;
import net.minecraft.class_7472;
import net.minecraft.class_9449;

public final class FakePayModule
extends Module {
    private final BooleanSetting sound = new BooleanSetting(EncryptedString.of("Sound"), true);
    private final BooleanSetting actionBar = new BooleanSetting(EncryptedString.of("Action Bar"), true);
    private final BooleanSetting removeFromScoreboard = new BooleanSetting(EncryptedString.of("Remove From Fake Scoreboard"), false).setDescription(EncryptedString.of("Subtracts every fake payment from Fake Scoreboard's money amount"));

    public FakePayModule() {
        super(EncryptedString.of("Fake Pay"), EncryptedString.of("Fakes /pay instead of sending it"), -1, Category.MISC);
        Setting[] var10001 = new Setting[]{this.sound, this.actionBar, this.removeFromScoreboard};
        this.addSettings(var10001);
    }

    @EventListener
    public void onPacketSend(PacketSendEvent var1) {
        String var2 = null;
        class_2596<?> class_25962 = var1.packet;
        if (class_25962 instanceof class_7472) {
            class_7472 var3 = (class_7472)class_25962;
            var2 = var3.comp_808();
        } else {
            class_25962 = var1.packet;
            if (class_25962 instanceof class_9449) {
                class_9449 var4 = (class_9449)class_25962;
                var2 = var4.comp_2532();
            }
        }
        if (var2 != null && this.handlePay(var2)) {
            var1.cancel();
        }
    }

    private boolean handlePay(String var1) {
        if (this.mc.field_1724 == null) {
            return false;
        }
        String[] var2 = var1.trim().split("\\s+");
        if (var2.length >= 3 && "pay".equals(var2[0].toLowerCase(Locale.ROOT))) {
            Double var3 = FakePayModule.parseMoney(var2[2]);
            if (var3 != null && Double.isFinite(var3) && !(var3 <= 0.0)) {
                FakeScoreboardModule var5;
                class_5250 var4 = class_2561.method_43470((String)"You paid ").method_27692(class_124.field_1080).method_10852((class_2561)class_2561.method_43470((String)var2[1]).method_27692(class_124.field_1075)).method_10852((class_2561)class_2561.method_43470((String)(" $" + FakePayModule.formatMoney(var3))).method_27692(class_124.field_1060)).method_10852((class_2561)class_2561.method_43470((String)".").method_27692(class_124.field_1080));
                this.mc.field_1724.method_7353((class_2561)var4, false);
                if (this.actionBar.getValue()) {
                    this.mc.field_1724.method_7353((class_2561)var4.method_27661(), true);
                }
                if (this.sound.getValue()) {
                    this.mc.field_1724.method_5783(class_3417.field_15224, 1.0f, 1.0f);
                }
                if (this.removeFromScoreboard.getValue() && (var5 = (FakeScoreboardModule)Hook.INSTANCE.getModuleManager().getModuleByClass(FakeScoreboardModule.class)) != null) {
                    var5.deductMoney(var3);
                }
                return true;
            }
            return false;
        }
        return false;
    }

    private static Double parseMoney(String var0) {
        if (var0 == null) {
            return null;
        }
        String var1 = var0.trim().replace(",", "").replace("$", "").toUpperCase(Locale.ROOT);
        double var2 = 1.0;
        if (var1.endsWith("K")) {
            var2 = 1000.0;
            var1 = var1.substring(0, var1.length() - 1);
        } else if (var1.endsWith("M")) {
            var2 = 1000000.0;
            var1 = var1.substring(0, var1.length() - 1);
        } else if (var1.endsWith("B")) {
            var2 = 1.0E9;
            var1 = var1.substring(0, var1.length() - 1);
        } else if (var1.endsWith("T")) {
            var2 = 1.0E12;
            var1 = var1.substring(0, var1.length() - 1);
        }
        try {
            return Double.parseDouble(var1) * var2;
        }
        catch (NumberFormatException var5) {
            return null;
        }
    }

    private static String formatMoney(double var0) {
        if (var0 >= 1.0E12) {
            return FakePayModule.trim(var0 / 1.0E12) + "T";
        }
        if (var0 >= 1.0E9) {
            return FakePayModule.trim(var0 / 1.0E9) + "B";
        }
        if (var0 >= 1000000.0) {
            return FakePayModule.trim(var0 / 1000000.0) + "M";
        }
        return var0 >= 1000.0 ? FakePayModule.trim(var0 / 1000.0) + "K" : FakePayModule.trim(var0);
    }

    private static String trim(double var0) {
        if (Math.rint(var0) == var0) {
            return Long.toString((long)var0);
        }
        Locale var10000 = Locale.ROOT;
        String var10001 = "%.2f";
        Object[] var10002 = new Object[]{var0};
        return String.format(var10000, var10001, var10002).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}

