/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4604
 */
package hook.module.ext.event;

import hook.module.ext.event.EventType;
import net.minecraft.class_4604;

public final class Render3DEvent {
    public static final EventType<Render3DEvent> TYPE = new EventType<Render3DEvent>(Render3DEvent.class);
    public float partialTick;
    public class_4604 frustum;

    public Render3DEvent(float var1, class_4604 var2) {
        this.partialTick = var1;
        this.frustum = var2;
    }
}

