/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2561
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BindSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;
import hook.utils.KeyUtils;
import hook.utils.embed.DiscordWebhook;
import hook.utils.embed.TelegramNotifier;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_2338;
import net.minecraft.class_2561;

public final class CordSnapperModule
extends Module {
    private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), -1, false);
    private final ModeSetting<Delivery> delivery = new ModeSetting<Delivery>(EncryptedString.of("Delivery"), Delivery.Discord, Delivery.class);
    private final StringSetting webhookUrl = new StringSetting(EncryptedString.of("Discord Webhook"), "");
    private final StringSetting telegramToken = new StringSetting(EncryptedString.of("Telegram Bot Token"), "");
    private final StringSetting telegramChatId = new StringSetting(EncryptedString.of("Telegram Chat ID"), "");
    private boolean wasPressed;

    public CordSnapperModule() {
        super(EncryptedString.of("Cord Snapper"), EncryptedString.of("Sends your current coordinates to Discord or Telegram"), -1, Category.MISC);
        this.addSettings(this.activateKey, this.delivery, this.webhookUrl, this.telegramToken, this.telegramChatId);
    }

    @Override
    public void onEnable() {
        this.wasPressed = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.wasPressed = false;
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.activateKey.getValue() < 0) {
            return;
        }
        boolean pressed = KeyUtils.isKeyPressed(this.activateKey.getValue());
        if (pressed && !this.wasPressed) {
            this.sendCoordinates();
        }
        this.wasPressed = pressed;
    }

    private void sendCoordinates() {
        class_2338 position = this.mc.field_1724.method_24515();
        String dimension = this.mc.field_1687.method_27983().method_29177().toString();
        String message = "HookVisuals coordinates\nDimension: " + dimension + "\nX: " + position.method_10263() + " Y: " + position.method_10264() + " Z: " + position.method_10260();
        Delivery destination = this.delivery.isMode(Delivery.Telegram) ? Delivery.Telegram : Delivery.Discord;
        String discordUrl = this.webhookUrl.getValue();
        String botToken = this.telegramToken.getValue();
        String chatId = this.telegramChatId.getValue();
        CompletableFuture.runAsync(() -> {
            try {
                if (destination == Delivery.Telegram) {
                    TelegramNotifier.send(botToken, chatId, message);
                } else {
                    DiscordWebhook webhook = new DiscordWebhook(discordUrl);
                    webhook.a(message);
                    webhook.execute();
                }
                this.showStatus("\u00a7aCord Snapper: coordinates sent to " + String.valueOf((Object)destination) + ".");
            }
            catch (Throwable error) {
                System.err.println("Hook Cord Snapper: " + error.getMessage());
                this.showStatus("\u00a7cCord Snapper failed: " + CordSnapperModule.safeMessage(error));
            }
        });
    }

    private void showStatus(String message) {
        this.mc.execute(() -> {
            if (this.mc.field_1724 != null) {
                this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)message), false);
            }
        });
    }

    private static String safeMessage(Throwable error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return error.getClass().getSimpleName();
        }
        return message.length() > 160 ? message.substring(0, 160) + "\u2026" : message;
    }

    private static enum Delivery {
        Discord,
        Telegram;

    }
}

