/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 */
package hook.compat.meteorclient.pathing;

import net.minecraft.class_2338;

public interface IPathManager {
    public String getName();

    public void moveTo(class_2338 var1);

    public boolean isPathing();

    public void stop();
}

