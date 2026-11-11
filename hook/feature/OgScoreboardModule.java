/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_266
 *  net.minecraft.class_268
 *  net.minecraft.class_269
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_5250
 *  net.minecraft.class_5348
 *  net.minecraft.class_640
 *  net.minecraft.class_8646
 *  net.minecraft.class_9011
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render2DEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.Setting;
import hook.utils.EncryptedString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_5348;
import net.minecraft.class_640;
import net.minecraft.class_8646;
import net.minecraft.class_9011;

public final class OgScoreboardModule
extends Module {
    private static OgScoreboardModule instance;
    private final BooleanSetting hideVanilla;
    private final NumberSetting scale;
    private final NumberSetting x;
    private final NumberSetting y;
    private int lastKnownPing;
    private long lastPingUpdateMs;

    public OgScoreboardModule() {
        super(EncryptedString.of("OG Scoreboard"), EncryptedString.of("Recreates the old Donut SMP sidebar from live scoreboard data"), -1, Category.DONUT);
        NumberSetting var22;
        NumberSetting var21;
        NumberSetting var20;
        BooleanSetting var19;
        EncryptedString var27 = EncryptedString.of("Hide Vanilla");
        this.hideVanilla = var19 = new BooleanSetting(var27, true);
        EncryptedString var29 = EncryptedString.of("Scale");
        this.scale = var20 = new NumberSetting(var29, 0.5, 3.0, 1.0, 0.1);
        EncryptedString var31 = EncryptedString.of("X");
        this.x = var21 = new NumberSetting(var31, -1.0, 3000.0, -1.0, 1.0);
        EncryptedString var33 = EncryptedString.of("Y");
        this.y = var22 = new NumberSetting(var33, -1.0, 3000.0, -1.0, 1.0);
        Setting[] var24 = new Setting[4];
        BooleanSetting var46 = this.hideVanilla;
        var24[0] = var46;
        NumberSetting var48 = this.scale;
        var24[1] = var48;
        NumberSetting var50 = this.x;
        var24[2] = var50;
        NumberSetting var52 = this.y;
        var24[3] = var52;
        this.addSettings(var24);
        instance = this;
    }

    public static boolean shouldHideVanillaSidebar() {
        return instance != null && instance.isEnabled() && OgScoreboardModule.instance.hideVanilla.getValue();
    }

    @EventListener
    private void onRender2D(Render2DEvent var1) {
        this.renderScoreboard(var1.context);
    }

    private void renderScoreboard(class_332 var1) {
        class_310 var2 = class_310.method_1551();
        if (var2.field_1724 != null && var2.field_1687 != null && var2.field_1772 != null) {
            ScoreboardData var3 = this.readScoreboard(var2);
            ArrayList<class_5250> var4 = new ArrayList<class_5250>(8);
            var4.add(class_2561.method_43473());
            var4.add(OgScoreboardModule.statRow("$", "Money", OgScoreboardModule.safe(var3.money), 65280, true));
            var4.add(OgScoreboardModule.statRow("\u2605", "Shards", OgScoreboardModule.safe(var3.shards), 10813692, false));
            var4.add(OgScoreboardModule.statRow("\u2694", "Kills", OgScoreboardModule.safe(var3.kills), 0xFF0000, false));
            var4.add(OgScoreboardModule.statRow("\u2620", "Deaths", OgScoreboardModule.safe(var3.deaths), 16546163, false));
            var4.add(OgScoreboardModule.statRow("\u231a", "Playtime", OgScoreboardModule.safe(var3.playtime), 16766464, false));
            var4.add(class_2561.method_43473());
            var4.add(OgScoreboardModule.footerRow(OgScoreboardModule.safe(var3.ping)));
            class_5250 var5 = OgScoreboardModule.donutTitle();
            int var6 = var2.field_1772.method_27525((class_5348)var5);
            for (Object e : var4) {
                class_2561 var8 = (class_2561)e;
                var6 = Math.max(var6, var2.field_1772.method_27525((class_5348)var8));
            }
            int var20 = 3;
            int n = 9;
            int var9 = var6 + var20 * 2;
            int var10 = n + 2;
            int var11 = var10 + var4.size() * n + 2;
            float var12 = Math.max(0.5f, Math.min(3.0f, this.scale.getFloatValue()));
            int var13 = Math.round((float)var9 * var12);
            int var14 = Math.round((float)var11 * var12);
            int var15 = this.x.getValue() < 0.0 ? var1.method_51421() - var13 - 2 : this.x.getIntValue();
            int var16 = this.y.getValue() < 0.0 ? (var1.method_51443() - var14) / 2 : this.y.getIntValue();
            var1.method_51448().pushMatrix();
            var1.method_51448().translate((float)var15, (float)var16);
            var1.method_51448().scale(var12, var12);
            var1.method_25294(0, 0, var9, var10, Integer.MIN_VALUE);
            var1.method_25294(0, var10, var9, var11, 0x60000000);
            var1.method_51439(var2.field_1772, (class_2561)var5, (var9 - var2.field_1772.method_27525((class_5348)var5)) / 2, 2, -1, false);
            int var17 = var10 + 1;
            for (Object e : var4) {
                class_2561 var19 = (class_2561)e;
                var1.method_51439(var2.field_1772, var19, var20, var17, -1, false);
                var17 += n;
            }
            var1.method_51448().popMatrix();
        }
    }

    private ScoreboardData readScoreboard(class_310 var1) {
        ScoreboardData var2 = new ScoreboardData();
        try {
            class_269 var3 = var1.field_1687.method_8428();
            class_266 var4 = var3.method_1189(class_8646.field_45157);
            if (var4 == null) {
                var2.ping = String.valueOf(this.currentPing(var1));
                return var2;
            }
            Collection var5 = var3.method_1184(var4);
            ArrayList<class_9011> var6 = new ArrayList<class_9011>(var5);
            var6.sort(Comparator.comparingInt(class_9011::comp_2128).reversed());
            boolean[] var7 = new boolean[5];
            int var8 = 0;
            for (Object e : var6) {
                class_9011 var10 = (class_9011)e;
                String var11 = this.plain(var3, var10);
                if (var11.isEmpty()) continue;
                String var12 = var11.toLowerCase(Locale.ROOT);
                if (!var7[0]) {
                    String[] var10002 = new String[]{"$", "money", "balance", "coins", "bal"};
                    if (OgScoreboardModule.matchesAny(var11, var12, var10002)) {
                        var2.money = OgScoreboardModule.extractValue(var11);
                        var7[0] = true;
                        ++var8;
                        continue;
                    }
                }
                if (!var7[1]) {
                    String[] var14 = new String[]{"*", "shard", "nirmata", "gem"};
                    if (OgScoreboardModule.matchesAny(var11, var12, var14)) {
                        var2.shards = OgScoreboardModule.extractValue(var11);
                        var7[1] = true;
                        ++var8;
                        continue;
                    }
                }
                if (!var7[2]) {
                    String[] var15 = new String[]{"kill"};
                    if (OgScoreboardModule.matchesAny(var11, var12, var15)) {
                        var2.kills = OgScoreboardModule.extractValue(var11);
                        var7[2] = true;
                        ++var8;
                        continue;
                    }
                }
                if (!var7[3]) {
                    String[] var16 = new String[]{"death"};
                    if (OgScoreboardModule.matchesAny(var11, var12, var16)) {
                        var2.deaths = OgScoreboardModule.extractValue(var11);
                        var7[3] = true;
                        ++var8;
                        continue;
                    }
                }
                if (var7[4]) continue;
                String[] var17 = new String[]{"playtime", "play time", "time played", "uptime"};
                if (!OgScoreboardModule.matchesAny(var11, var12, var17)) continue;
                var2.playtime = OgScoreboardModule.extractPlaytime(var11);
                var7[4] = true;
                ++var8;
            }
            if (var8 < 3) {
                this.positionFallback(var3, var6, var2, var7);
            }
            var2.ping = String.valueOf(this.currentPing(var1));
        }
        catch (Throwable var13) {
            var2.ping = String.valueOf(this.currentPing(var1));
        }
        return var2;
    }

    private void positionFallback(class_269 var1, List<class_9011> var2, ScoreboardData var3, boolean[] var4) {
        ArrayList<String> var5 = new ArrayList<String>();
        for (class_9011 var7 : var2) {
            String var8 = this.plain(var1, var7);
            if (var8.isEmpty() || OgScoreboardModule.looksLikeSeparator(var8)) continue;
            var5.add(var8);
        }
        int var9 = !var5.isEmpty() && !OgScoreboardModule.hasDigit((String)var5.getFirst()) ? 1 : 0;
        String[] var10 = new String[5];
        for (int var11 = 0; var11 + var9 < var5.size() && var11 < var10.length; ++var11) {
            var10[var11] = (String)var5.get(var11 + var9);
        }
        if (!var4[0] && var10[0] != null) {
            var3.money = OgScoreboardModule.extractValue(var10[0]);
        }
        if (!var4[1] && var10[1] != null) {
            var3.shards = OgScoreboardModule.extractValue(var10[1]);
        }
        if (!var4[2] && var10[2] != null) {
            var3.kills = OgScoreboardModule.extractValue(var10[2]);
        }
        if (!var4[3] && var10[3] != null) {
            var3.deaths = OgScoreboardModule.extractValue(var10[3]);
        }
        if (!var4[4] && var10[4] != null) {
            var3.playtime = OgScoreboardModule.extractPlaytime(var10[4]);
        }
    }

    private String plain(class_269 var1, class_9011 var2) {
        class_2561 var3 = var2.method_55387();
        if (var3 == null) {
            var3 = class_2561.method_43470((String)(var2.comp_2127() == null ? "" : var2.comp_2127()));
        }
        class_2561 var4 = var3;
        try {
            class_268 var5 = var1.method_1164(var2.comp_2127());
            if (var5 != null) {
                var4 = var5.method_1198(var3);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return OgScoreboardModule.sanitise(var4.getString());
    }

    private int currentPing(class_310 var1) {
        int var2 = this.readPing(var1);
        long var3 = System.currentTimeMillis();
        if (var2 > 0) {
            this.lastKnownPing = var2;
            this.lastPingUpdateMs = var3;
            return var2;
        }
        return this.lastKnownPing > 0 && var3 - this.lastPingUpdateMs < 60000L ? this.lastKnownPing : 0;
    }

    private int readPing(class_310 var1) {
        if (var1.field_1724 != null && var1.method_1562() != null) {
            try {
                UUID var2 = var1.field_1724.method_5667();
                String var3 = var1.field_1724.method_5477().getString();
                class_640 var4 = var1.method_1562().method_2871(var2);
                if (var4 != null && var4.method_2959() > 0) {
                    return var4.method_2959();
                }
                for (class_640 var6 : var1.method_1562().method_2880()) {
                    if (var6 == null || var6.method_2966() == null) continue;
                    if (var2.equals(var6.method_2966().id()) && var6.method_2959() > 0) {
                        return var6.method_2959();
                    }
                    if (!var3.equals(var6.method_2966().name()) || var6.method_2959() <= 0) continue;
                    return var6.method_2959();
                }
                for (class_640 var9 : var1.method_1562().method_2880()) {
                    if (var9 == null || var9.method_2959() <= 0) continue;
                    return var9.method_2959();
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return 0;
        }
        return 0;
    }

    private static class_5250 statRow(String var0, String var1, String var2, int var3, boolean var4) {
        return class_2561.method_43473().method_10852((class_2561)class_2561.method_43470((String)var0).method_10862(class_2583.field_24360.method_36139(var3).method_10982(Boolean.valueOf(var4)))).method_10852((class_2561)class_2561.method_43470((String)(" " + var1 + " ")).method_27692(class_124.field_1080)).method_10852((class_2561)class_2561.method_43470((String)var2).method_27694(var1x -> var1x.method_36139(var3)));
    }

    private static class_5250 footerRow(String var0) {
        return class_2561.method_43473().method_10852((class_2561)class_2561.method_43470((String)"EU Central ").method_27694(var0x -> var0x.method_36139(0xAAAAAA))).method_10852((class_2561)class_2561.method_43470((String)("(" + var0 + "ms)")).method_27694(var0x -> var0x.method_36139(42751)));
    }

    private static class_5250 donutTitle() {
        String var0 = "Donut SMP";
        int[] var10000 = new int[]{32252, 35068, 37628, 40444, 43260, 46076, 48892, 51708, 54524};
        int[] var1 = var10000;
        class_5250 var2 = class_2561.method_43473();
        for (int var3 = 0; var3 < var0.length(); ++var3) {
            int var4 = var1[Math.min(var3, var1.length - 1)];
            var2.method_10852((class_2561)class_2561.method_43470((String)String.valueOf(var0.charAt(var3))).method_27694(var1x -> var1x.method_36139(var4).method_10982(Boolean.valueOf(true))));
        }
        return var2;
    }

    private static boolean matchesAny(String var0, String var1, String ... var2) {
        String[] var3 = var2;
        int var4 = var2.length;
        for (int var5 = 0; var5 < var4; ++var5) {
            String var6 = var3[var5];
            if (var6 == null || var6.isEmpty() || !var0.contains(var6) && (!OgScoreboardModule.isLetterWord(var6) || !var1.contains(var6.toLowerCase(Locale.ROOT)))) continue;
            return true;
        }
        return false;
    }

    private static boolean isLetterWord(String var0) {
        for (int var1 = 0; var1 < var0.length(); ++var1) {
            if (!Character.isLetter(var0.charAt(var1))) continue;
            return true;
        }
        return false;
    }

    private static boolean looksLikeSeparator(String var0) {
        String var1 = OgScoreboardModule.sanitise(var0);
        return var1.isEmpty() || var1.chars().allMatch(var0x -> var0x == 32 || var0x == 45 || var0x == 95 || var0x == 46 || var0x == 42);
    }

    private static boolean hasDigit(String var0) {
        for (int var1 = 0; var1 < var0.length(); ++var1) {
            if (!Character.isDigit(var0.charAt(var1))) continue;
            return true;
        }
        return false;
    }

    private static String extractValue(String var0) {
        String var1 = OgScoreboardModule.sanitise(var0);
        if (var1.isEmpty()) {
            return "0";
        }
        String[] var2 = var1.split("\\s+");
        for (int var3 = var2.length - 1; var3 >= 0; --var3) {
            if (!OgScoreboardModule.hasDigit(var2[var3])) continue;
            return var2[var3];
        }
        for (int var4 = var2.length - 1; var4 >= 0; --var4) {
            if (var2[var4].isEmpty()) continue;
            return var2[var4];
        }
        return "0";
    }

    private static String extractPlaytime(String var0) {
        String var5;
        String var1 = OgScoreboardModule.sanitise(var0);
        if (var1.isEmpty()) {
            return "0";
        }
        String[] var2 = var1.split("\\s+");
        ArrayList<String> var3 = new ArrayList<String>();
        for (int var4 = var2.length - 1; var4 >= 0 && ((var5 = var2[var4]).matches("\\d+[dhmsy]") || var5.matches("\\d+")); --var4) {
            var3.addFirst(var5);
        }
        return !var3.isEmpty() ? String.join((CharSequence)" ", var3) : OgScoreboardModule.extractValue(var1);
    }

    private static String sanitise(String var0) {
        if (var0 == null) {
            return "";
        }
        StringBuilder var1 = new StringBuilder(var0.length());
        for (int var2 = 0; var2 < var0.length(); ++var2) {
            char var3 = var0.charAt(var2);
            if (var3 == '\u00a7') {
                ++var2;
                continue;
            }
            int var4 = Character.getType(var3);
            if (var4 != 15 && var4 != 16 && var4 != 19 && var3 != '\u200b' && var3 != '\u200c' && var3 != '\u200d' && var3 != '\ufeff') {
                var1.append(var3);
                continue;
            }
            var1.append(' ');
        }
        return var1.toString().trim();
    }

    private static String safe(String var0) {
        return var0 != null && !var0.isEmpty() ? var0 : "0";
    }

    private static final class ScoreboardData {
        private String money = "0";
        private String shards = "0";
        private String kills = "0";
        private String deaths = "0";
        private String playtime = "0";
        private String ping = "0";

        private ScoreboardData() {
        }
    }
}

