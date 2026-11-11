/*
 * Decompiled with CFR 0.152.
 */
package hook.utils.embed;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class TelegramNotifier {
    private TelegramNotifier() {
    }

    public static void send(String tokenOrUrl, String chatId, String text) throws IOException {
    }

    private static String normalizeToken(String tokenOrUrl) {
        if (tokenOrUrl == null) {
            return "";
        }
        String value = tokenOrUrl.trim();
        int bot = value.indexOf("/bot");
        if (bot >= 0) {
            int start = bot + 4;
            int end = value.indexOf(47, start);
            return end >= 0 ? value.substring(start, end) : value.substring(start);
        }
        return value.startsWith("bot") ? value.substring(3) : value;
    }

    private static String read(InputStream input) throws IOException {
        String var2;
        if (input == null) {
            return "";
        }
        InputStream var1 = input;
        try {
            var2 = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (Throwable var5) {
            if (input != null) {
                try {
                    var1.close();
                }
                catch (Throwable var4) {
                    var5.addSuppressed(var4);
                }
            }
            throw var5;
        }
        if (input != null) {
            input.close();
        }
        return var2;
    }
}

