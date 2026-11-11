/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package hook.compat.meteorclient.systems.hud;

import hook.compat.meteorclient.systems.hud.HudElement;
import hook.compat.meteorclient.systems.hud.HudElementInfo;
import hook.compat.meteorclient.systems.hud.HudRenderer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_332;

public class Hud {
    private static final Hud INSTANCE = new Hud();
    private final List<HudElementInfo<?>> infos = new ArrayList();
    private final Map<HudElementInfo<?>, HudElement> elements = new IdentityHashMap();

    public static Hud get() {
        return INSTANCE;
    }

    public void register(HudElementInfo<?> info) {
        if (!this.infos.contains(info)) {
            this.infos.add(info);
        }
    }

    public void render(class_332 context) {
        if (this.infos.isEmpty()) {
            return;
        }
        HudRenderer renderer = new HudRenderer();
        renderer.begin(context);
        double y = 4.0;
        for (HudElementInfo<?> info : this.infos) {
            HudElement element = this.elements.get(info);
            if (element == null) {
                element = info.create();
                this.elements.put(info, element);
            }
            element.x = 4.0;
            element.y = y;
            element.render(renderer);
            y += Math.max(element.height, 0.0) + 2.0;
        }
    }
}

