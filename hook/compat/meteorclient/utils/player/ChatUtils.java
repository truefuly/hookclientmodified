/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package hook.compat.meteorclient.utils.player;

import net.minecraft.class_310;

public final class ChatUtils {
    private static final class_310 mc = class_310.method_1551();

    private ChatUtils() {
    }

    public static void sendPlayerMsg(String message) {
        if (ChatUtils.mc.field_1724 == null || mc.method_1562() == null || message == null) {
            return;
        }
        mc.method_1562().method_45729(message);
    }
}

