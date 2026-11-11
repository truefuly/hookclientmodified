/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 */
package hook.compat.meteorclient.events.render;

import hook.compat.meteorclient.renderer.ShapeMode;
import hook.compat.meteorclient.utils.render.color.Color;
import hook.utils.RenderUtils;
import java.util.ArrayList;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_310;
import net.minecraft.class_4587;

public class Render3DEvent {
    private static final class_310 mc = class_310.method_1551();
    public final Renderer renderer = new Renderer();

    public static final class Renderer {
        public void box(class_2338 pos, Color color1, Color color2, ShapeMode shapeMode, double lineWidth) {
            this.box(new class_238(pos), color1, color2, shapeMode, lineWidth);
        }

        public void box(class_238 box, Color color1, Color color2, ShapeMode shapeMode, double lineWidth) {
            if (mc == null || Render3DEvent.mc.field_1687 == null || color1 == null) {
                return;
            }
            class_4587 matrices = new class_4587();
            RenderUtils.applyCameraTransform(matrices, RenderUtils.getCamera());
            if (shapeMode == ShapeMode.Both || shapeMode == ShapeMode.Sides) {
                RenderUtils.renderFilledBox(matrices, (float)box.field_1323, (float)box.field_1322, (float)box.field_1321, (float)box.field_1320, (float)box.field_1325, (float)box.field_1324, color1.toAwt());
            }
            if (shapeMode == ShapeMode.Both || shapeMode == ShapeMode.Lines) {
                Color lineColor = color2 != null ? color2 : color1;
                float width = lineWidth <= 0.0 ? 1.5f : (float)lineWidth;
                ArrayList<RenderUtils.ColoredLine> lines = new ArrayList<RenderUtils.ColoredLine>();
                RenderUtils.addBoxOutline(lines, box.field_1323, box.field_1322, box.field_1321, box.field_1320, box.field_1325, box.field_1324, lineColor.toAwt(), width);
                RenderUtils.renderLines(matrices, lines);
            }
        }
    }
}

