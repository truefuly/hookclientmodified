/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 */
package hook.compat.meteorclient.commands;

import net.minecraft.class_2561;
import net.minecraft.class_310;

final class CommandChat {
    private CommandChat() {
    }

    static void send(class_2561 message) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1705 == null) {
            return;
        }
        if (mc.method_18854()) {
            mc.field_1705.method_1743().method_1812(message);
        } else {
            mc.execute(() -> mc.field_1705.method_1743().method_1812(message));
        }
    }
}

