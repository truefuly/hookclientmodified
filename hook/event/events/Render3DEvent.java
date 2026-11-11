/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 *  org.joml.Matrix4f
 */
package hook.event.events;

import hook.event.CancellableEvent;
import net.minecraft.class_4587;
import org.joml.Matrix4f;

public class Render3DEvent
extends CancellableEvent {
    public class_4587 matrixStack;
    public Matrix4f matrix4f;
    public float tickDelta;

    public Render3DEvent(class_4587 matrixStack, Matrix4f matrix4f, float tickDelta) {
        this.matrixStack = matrixStack;
        this.matrix4f = matrix4f;
        this.tickDelta = tickDelta;
    }
}

