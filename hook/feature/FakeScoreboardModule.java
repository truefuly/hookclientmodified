/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_266
 *  net.minecraft.class_268
 *  net.minecraft.class_269
 *  net.minecraft.class_274
 *  net.minecraft.class_274$class_275
 *  net.minecraft.class_5250
 *  net.minecraft.class_8646
 *  net.minecraft.class_9014
 *  net.minecraft.class_9015
 *  net.minecraft.class_9020
 *  net.minecraft.class_9022
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_274;
import net.minecraft.class_5250;
import net.minecraft.class_8646;
import net.minecraft.class_9014;
import net.minecraft.class_9015;
import net.minecraft.class_9020;
import net.minecraft.class_9022;

public final class FakeScoreboardModule
extends Module {
    private static final String OBJECTIVE = "hook_fake_scoreboard";
    private static final String TEAM_PREFIX = "hook_sb_";
    private final StringSetting title = new StringSetting(EncryptedString.of("Title"), "HookVisuals");
    private final BooleanSetting showMoney = new BooleanSetting(EncryptedString.of("Money"), true);
    private final StringSetting money = new StringSetting(EncryptedString.of("Money Amount"), "1M");
    private final BooleanSetting showShards = new BooleanSetting(EncryptedString.of("Shards"), true);
    private final StringSetting shards = new StringSetting(EncryptedString.of("Shards Amount"), "0");
    private final BooleanSetting showKills = new BooleanSetting(EncryptedString.of("Kills"), true);
    private final StringSetting kills = new StringSetting(EncryptedString.of("Kills Amount"), "0");
    private final BooleanSetting showDeaths = new BooleanSetting(EncryptedString.of("Deaths"), true);
    private final StringSetting deaths = new StringSetting(EncryptedString.of("Deaths Amount"), "0");
    private final BooleanSetting showPlaytime = new BooleanSetting(EncryptedString.of("Playtime"), true);
    private final StringSetting playtime = new StringSetting(EncryptedString.of("Playtime Amount"), "0h");
    private final List<String> teamNames = new ArrayList<String>();
    private class_266 ours;
    private class_266 serverObjective;
    private Object trackedWorld;
    private long lastRefresh;

    public FakeScoreboardModule() {
        super(EncryptedString.of("Fake Scoreboard"), EncryptedString.of("Replaces the sidebar with your own configurable scoreboard"), -1, Category.DONUT);
        this.addSettings(this.title, this.showMoney, this.money, this.showShards, this.shards, this.showKills, this.kills, this.showDeaths, this.deaths, this.showPlaytime, this.playtime);
    }

    @Override
    public void onEnable() {
        this.lastRefresh = 0L;
        this.trackedWorld = this.mc.field_1687;
        this.rebuild();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.teardown();
        this.trackedWorld = null;
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            long var2;
            if (this.trackedWorld != this.mc.field_1687) {
                this.ours = null;
                this.serverObjective = null;
                this.teamNames.clear();
                this.trackedWorld = this.mc.field_1687;
                this.lastRefresh = 0L;
            }
            if ((var2 = System.currentTimeMillis()) - this.lastRefresh >= 1000L) {
                this.lastRefresh = var2;
                this.rebuild();
            }
        }
    }

    private void rebuild() {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            class_269 var1 = this.mc.field_1687.method_8428();
            class_266 var2 = var1.method_1189(class_8646.field_45157);
            if (var2 != null && !OBJECTIVE.equals(var2.method_1113())) {
                this.serverObjective = var2;
            }
            this.dropTeams(var1);
            class_266 var3 = var1.method_1170(OBJECTIVE);
            if (var3 != null) {
                var1.method_1194(var3);
            }
            this.ours = var1.method_1168(OBJECTIVE, class_274.field_1468, (class_2561)class_2561.method_43470((String)this.title.getValue()), class_274.class_275.field_1472, false, (class_9022)class_9020.field_47557);
            var1.method_1158(class_8646.field_45157, this.ours);
            List<class_5250> var4 = this.buildLines();
            for (int var5 = 0; var5 < var4.size(); ++var5) {
                String var6 = TEAM_PREFIX + var5;
                this.teamNames.add(var6);
                class_268 var7 = var1.method_1153(var6);
                if (var7 != null) {
                    var1.method_1191(var7);
                }
                class_268 var8 = var1.method_1171(var6);
                var8.method_1138((class_2561)var4.get(var5));
                String var9 = "\u00a7" + Integer.toHexString(var5);
                class_9015 var10 = class_9015.method_55422((String)var9);
                var1.method_1155(var10, this.ours);
                class_9014 var11 = var1.method_1180(var10, this.ours);
                var11.method_55410(var4.size() - var5);
                var1.method_1172(var9, var8);
            }
        }
    }

    private List<class_5250> buildLines() {
        ArrayList<class_5250> var1 = new ArrayList<class_5250>();
        if (this.showMoney.getValue()) {
            var1.add(this.line("$ ", class_124.field_1060, this.money.getValue()));
        }
        if (this.showShards.getValue()) {
            var1.add(this.line("\u2726 ", class_124.field_1076, this.shards.getValue()));
        }
        if (this.showKills.getValue()) {
            var1.add(this.line("\u2694 ", class_124.field_1061, this.kills.getValue()));
        }
        if (this.showDeaths.getValue()) {
            var1.add(this.line("\u2620 ", class_124.field_1065, this.deaths.getValue()));
        }
        if (this.showPlaytime.getValue()) {
            var1.add(this.line("\u25f7 ", class_124.field_1054, this.playtime.getValue()));
        }
        if (var1.isEmpty()) {
            var1.add(class_2561.method_43470((String)" "));
        }
        return var1;
    }

    private class_5250 line(String var1, class_124 var2, String var3) {
        return class_2561.method_43470((String)var1).method_27692(var2).method_10852((class_2561)class_2561.method_43470((String)var3).method_27692(class_124.field_1068));
    }

    public void deductMoney(double var1) {
        String var3 = this.money.getValue().trim().replace(",", "").replace("$", "").toUpperCase(Locale.ROOT);
        double var4 = 1.0;
        if (var3.endsWith("K")) {
            var4 = 1000.0;
            var3 = var3.substring(0, var3.length() - 1);
        } else if (var3.endsWith("M")) {
            var4 = 1000000.0;
            var3 = var3.substring(0, var3.length() - 1);
        } else if (var3.endsWith("B")) {
            var4 = 1.0E9;
            var3 = var3.substring(0, var3.length() - 1);
        } else if (var3.endsWith("T")) {
            var4 = 1.0E12;
            var3 = var3.substring(0, var3.length() - 1);
        }
        try {
            double var6 = Math.max(0.0, Double.parseDouble(var3) * var4 - var1);
            if (var6 >= 1.0E12) {
                this.money.setValue(FakeScoreboardModule.compact(var6 / 1.0E12) + "T");
            } else if (var6 >= 1.0E9) {
                this.money.setValue(FakeScoreboardModule.compact(var6 / 1.0E9) + "B");
            } else if (var6 >= 1000000.0) {
                this.money.setValue(FakeScoreboardModule.compact(var6 / 1000000.0) + "M");
            } else if (var6 >= 1000.0) {
                this.money.setValue(FakeScoreboardModule.compact(var6 / 1000.0) + "K");
            } else {
                this.money.setValue(FakeScoreboardModule.compact(var6));
            }
            this.lastRefresh = 0L;
            if (this.isEnabled()) {
                this.rebuild();
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
    }

    private static String compact(double var0) {
        if (Math.rint(var0) == var0) {
            return Long.toString((long)var0);
        }
        Locale var10000 = Locale.ROOT;
        String var10001 = "%.2f";
        Object[] var10002 = new Object[]{var0};
        return String.format(var10000, var10001, var10002).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private void teardown() {
        if (this.mc.field_1687 == null) {
            this.ours = null;
            this.teamNames.clear();
        } else {
            class_269 var1 = this.mc.field_1687.method_8428();
            this.dropTeams(var1);
            class_266 var2 = var1.method_1170(OBJECTIVE);
            if (var2 != null) {
                var1.method_1194(var2);
            }
            class_266 var3 = null;
            if (this.serverObjective != null && var1.method_1170(this.serverObjective.method_1113()) != null) {
                var3 = this.serverObjective;
            }
            var1.method_1158(class_8646.field_45157, var3);
            this.ours = null;
            this.serverObjective = null;
        }
    }

    private void dropTeams(class_269 var1) {
        for (String var3 : this.teamNames) {
            class_268 var4 = var1.method_1153(var3);
            if (var4 == null) continue;
            var1.method_1191(var4);
        }
        this.teamNames.clear();
    }
}

