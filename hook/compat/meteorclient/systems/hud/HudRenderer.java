/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package hook.compat.meteorclient.systems.hud;

import hook.compat.meteorclient.utils.render.color.Color;
import java.util.Objects;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;

public class HudRenderer {
    private class_332 context;

    void begin(class_332 context) {
        this.context = context;
    }

    public void text(String text, double x, double y, Color color, boolean shadow) {
        if (this.context == null || text == null) {
            return;
        }
        class_310 mc = class_310.method_1551();
        this.context.method_51439(mc.field_1772, (class_2561)class_2561.method_43470((String)text), (int)x, (int)y, color.getRGB(), shadow);
    }

    public double textWidth(String text, boolean bold) {
        if (text == null) {
            return 0.0;
        }
        return class_310.method_1551().field_1772.method_1727(text);
    }

    public double textHeight(boolean bold) {
        Objects.requireNonNull(class_310.method_1551().field_1772);
        return 9.0;
    }
}

