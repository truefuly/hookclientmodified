/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.systems.hud;

import hook.compat.meteorclient.systems.hud.HudElementInfo;
import hook.compat.meteorclient.systems.hud.HudRenderer;

public abstract class HudElement {
    public double x;
    public double y;
    public double width;
    public double height;

    protected HudElement(HudElementInfo<?> info) {
    }

    public abstract void render(HudRenderer var1);

    protected void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }
}

