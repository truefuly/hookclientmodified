/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 */
package hook.module.ext.event;

import hook.module.ext.event.EventType;
import net.minecraft.class_2338;
import net.minecraft.class_2680;

public final class BlockUpdateEvent {
    public static final EventType<BlockUpdateEvent> TYPE = new EventType<BlockUpdateEvent>(BlockUpdateEvent.class);
    public class_2338 pos;
    public class_2680 oldState;
    public class_2680 newState;

    public BlockUpdateEvent(class_2338 var1, class_2680 var2, class_2680 var3) {
        this.pos = var1;
        this.oldState = var2;
        this.newState = var3;
    }
}

