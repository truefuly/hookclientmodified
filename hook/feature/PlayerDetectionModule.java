/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_2561
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;
import hook.utils.embed.DiscordWebhook;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.class_1657;
import net.minecraft.class_2561;

public final class PlayerDetectionModule
extends Module {
    private final BooleanSetting notifications = new BooleanSetting(EncryptedString.of("Chat Alerts"), true);
    private final StringSetting whitelist = new StringSetting(EncryptedString.of("Whitelist"), "FreeCamera").setDescription(EncryptedString.of("Comma-separated player names to ignore"));
    private final BooleanSetting disconnect = new BooleanSetting(EncryptedString.of("Disconnect"), true);
    private final BooleanSetting disableOnDetect = new BooleanSetting(EncryptedString.of("Disable On Detect"), true);
    private final BooleanSetting stopBaritone = new BooleanSetting(EncryptedString.of("Stop Baritone"), true);
    private final BooleanSetting webhook = new BooleanSetting(EncryptedString.of("Webhook"), false);
    private final StringSetting webhookUrl = new StringSetting(EncryptedString.of("Discord Webhook"), "");
    private final BooleanSetting panicPay = new BooleanSetting(EncryptedString.of("Panic Pay"), false);
    private final StringSetting panicPayTarget = new StringSetting(EncryptedString.of("Panic Pay Target"), "");
    private final StringSetting panicPayAmount = new StringSetting(EncryptedString.of("Panic Pay Amount"), "");
    private final Set<String> detectedPlayers = new HashSet<String>();
    private int scanDelay;

    public PlayerDetectionModule() {
        super(EncryptedString.of("Player Detection"), EncryptedString.of("Alerts and optionally disconnects when another player enters view"), -1, Category.MISC);
        this.addSettings(this.notifications, this.whitelist, this.disconnect, this.disableOnDetect, this.stopBaritone, this.webhook, this.webhookUrl, this.panicPay, this.panicPayTarget, this.panicPayAmount);
    }

    @Override
    public void onEnable() {
        this.detectedPlayers.clear();
        this.scanDelay = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.detectedPlayers.clear();
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
            int var10000;
            if ((var10000 = this.scanDelay++) <= 0 || this.scanDelay >= 10) {
                this.scanDelay = 0;
                Set<String> var2 = this.parseWhitelist();
                HashSet<String> var3 = new HashSet<String>();
                for (class_1657 var5 : this.mc.field_1687.method_18456()) {
                    String var6;
                    if (var5 == this.mc.field_1724 || var5.method_31481() || var2.contains((var6 = var5.method_5477().getString()).toLowerCase(Locale.ROOT))) continue;
                    var3.add(var6);
                }
                HashSet<String> var7 = new HashSet<String>(var3);
                var7.removeAll(this.detectedPlayers);
                this.detectedPlayers.clear();
                this.detectedPlayers.addAll(var3);
                if (!var7.isEmpty()) {
                    this.handleDetection(var7);
                }
            }
        } else {
            this.detectedPlayers.clear();
        }
    }

    private Set<String> parseWhitelist() {
        HashSet<String> var1 = new HashSet<String>();
        var1.add("freecamera");
        Arrays.stream(this.whitelist.getValue().split("[,;\\s]+")).map(var0 -> var0.trim().toLowerCase(Locale.ROOT)).filter(var0 -> !var0.isBlank()).forEach(var1::add);
        return var1;
    }

    private void handleDetection(Set<String> var1) {
        String var2 = String.join((CharSequence)", ", var1);
        if (this.notifications.getValue()) {
            this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)("\u00a7c[Player Detection] \u00a7fPlayer(s) detected: \u00a7e" + var2)), false);
        }
        if (this.stopBaritone.getValue() && this.mc.method_1562() != null) {
            this.mc.method_1562().method_45729("#stop");
        }
        if (this.panicPay.getValue() && this.mc.method_1562() != null && !this.panicPayTarget.getValue().isBlank() && !this.panicPayAmount.getValue().isBlank()) {
            this.mc.method_1562().method_45730("pay " + this.panicPayTarget.getValue().trim() + " " + this.panicPayAmount.getValue().trim());
        }
        if (this.webhook.getValue()) {
            this.sendWebhook(var2);
        }
        if (this.disableOnDetect.getValue()) {
            this.toggle(false);
        }
        if (this.disconnect.getValue()) {
            CompletableFuture.delayedExecutor(500L, TimeUnit.MILLISECONDS).execute(() -> this.mc.execute(() -> {
                if (this.mc.method_1562() != null) {
                    this.mc.method_1562().method_48296().method_10747((class_2561)class_2561.method_43470((String)("Player(s) detected: " + var2)));
                }
            }));
        }
    }

    private void sendWebhook(String names) {
        String url = this.webhookUrl.getValue();
        if (url.isBlank()) {
            if (this.notifications.getValue()) {
                this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)"\u00a7c[Player Detection] Webhook URL is empty."), false);
            }
            return;
        }
        String server = this.mc.method_1558() == null ? "Single Player" : this.mc.method_1558().field_3761;
        CompletableFuture.runAsync(() -> {
            try {
                DiscordWebhook message = new DiscordWebhook(url);
                message.addEmbed(new DiscordWebhook.EmbedObject().setTitle("Player Detection Alert").setDescription("Player(s) detected: " + names).setColor(new Color(231, 76, 60)).addField("Server", server, true));
                message.execute();
            }
            catch (Throwable failure) {
                System.err.println("Hook Player Detection webhook failed: " + failure.getMessage());
            }
        });
    }
}

