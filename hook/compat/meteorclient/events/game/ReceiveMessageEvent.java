/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 */
package hook.compat.meteorclient.events.game;

import net.minecraft.class_2561;

public class ReceiveMessageEvent {
    public class_2561 message;

    public ReceiveMessageEvent(class_2561 message) {
        this.message = message;
    }

    public class_2561 getMessage() {
        return this.message;
    }
}

