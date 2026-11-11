/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.systems.hud;

import hook.compat.meteorclient.systems.hud.HudElement;
import hook.compat.meteorclient.systems.hud.HudGroup;
import java.util.function.Supplier;

public class HudElementInfo<T extends HudElement> {
    public final HudGroup group;
    public final String name;
    public final String description;
    private final Supplier<T> factory;

    public HudElementInfo(HudGroup group, String name, String description, Supplier<T> factory) {
        this.group = group;
        this.name = name;
        this.description = description;
        this.factory = factory;
    }

    public T create() {
        return (T)((HudElement)this.factory.get());
    }
}

