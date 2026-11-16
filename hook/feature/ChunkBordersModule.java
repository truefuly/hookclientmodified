package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.RgbSetting;
import hook.utils.EncryptedString;
import hook.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_4587;

/**
 * Just the chunk borders, nothing else: the walls of the chunk you stand in (corner posts and a ring at your height)
 * and the corner posts of the chunks around you. Cleaner than F3+G.
 */
public final class ChunkBordersModule extends Module {
    private final RgbSetting color = new RgbSetting(EncryptedString.of("Color"), 0xFFFFD24A);
    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range (chunks)"), 0.0, 4.0, 1.0, 1.0);
    private final NumberSetting height = new NumberSetting(EncryptedString.of("Height"), 8.0, 96.0, 32.0, 4.0);
    private final BooleanSetting rings = new BooleanSetting(EncryptedString.of("Height Rings"), false);
    private final NumberSetting width = new NumberSetting(EncryptedString.of("Line Width"), 1.0, 4.0, 1.5, 0.5);
    private final BooleanSetting throughWalls = new BooleanSetting(EncryptedString.of("Through Walls"), true);

    public ChunkBordersModule() {
        super(EncryptedString.of("Chunk Borders"), EncryptedString.of("Shows only the chunk borders around you - clean lines, no debug clutter."), -1, Category.RENDER);
        this.color.setDescription(EncryptedString.of("Colour of the borders."));
        this.range.setDescription(EncryptedString.of("How many chunks around you get their corners shown. 0 = only your chunk."));
        this.height.setDescription(EncryptedString.of("How far the lines go up and down from you (blocks)."));
        this.rings.setDescription(EncryptedString.of("Extra lines around your chunk every 16 blocks of height."));
        this.width.setDescription(EncryptedString.of("Thickness of the lines."));
        this.throughWalls.setDescription(EncryptedString.of("On: always visible. Off: hidden behind blocks like normal world lines."));
        this.addSettings(this.color, this.range, this.height, this.rings, this.width, this.throughWalls);
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) return;
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null || !camera.method_19332()) return;
        class_243 eye = camera.method_71156();
        int cx = (int) Math.floor(eye.field_1352) >> 4, cz = (int) Math.floor(eye.field_1350) >> 4;
        double feet = this.mc.field_1724.method_30950(camera.method_55437()).field_1351;
        int minY = this.mc.field_1687.method_31607(), maxY = minY + this.mc.field_1687.method_31605();
        double h = this.height.getValue();
        double y0 = Math.max(minY, feet - h), y1 = Math.min(maxY, feet + h);
        if (y1 <= y0) return;
        int argb = this.color.getArgb();
        Color main = new Color((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, 230);
        float w = this.width.getFloatValue();
        int r = this.range.getIntValue();
        List<RenderUtils.ColoredLine> lines = new ArrayList<>();

        // corner posts of the chunks around you (fainter the further away)
        for (int x = cx - r; x <= cx + r + 1; x++) {
            for (int z = cz - r; z <= cz + r + 1; z++) {
                boolean own = (x == cx || x == cx + 1) && (z == cz || z == cz + 1);
                if (own) continue;
                int dist = Math.max(Math.max(cx - x, x - cx - 1), Math.max(cz - z, z - cz - 1));
                int a = (int) (170 / (1.0 + dist * 0.6));
                lines.add(new RenderUtils.ColoredLine(new class_243(x * 16, y0, z * 16), new class_243(x * 16, y1, z * 16), withAlpha(main, a), Math.max(1.0f, w - 0.5f)));
            }
        }
        // your chunk: corner posts, a ring at your feet and optional rings every 16 blocks
        double x0 = cx * 16, z0 = cz * 16, x1 = x0 + 16, z1 = z0 + 16;
        for (double[] c : new double[][]{{x0, z0}, {x1, z0}, {x1, z1}, {x0, z1}})
            lines.add(new RenderUtils.ColoredLine(new class_243(c[0], y0, c[1]), new class_243(c[0], y1, c[1]), main, w));
        ring(lines, x0, z0, x1, z1, Math.floor(feet) + 0.01, main, w);
        if (this.rings.getValue()) {
            Color faint = withAlpha(main, 110);
            for (double y = Math.ceil(y0 / 16.0) * 16; y <= y1; y += 16) ring(lines, x0, z0, x1, z1, y, faint, Math.max(1.0f, w - 0.5f));
        }
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        RenderUtils.applyCameraTransform(matrices, camera);
        RenderUtils.renderLines(matrices, lines, this.throughWalls.getValue());
        matrices.method_22909();
    }

    private static void ring(List<RenderUtils.ColoredLine> lines, double x0, double z0, double x1, double z1, double y, Color c, float w) {
        lines.add(new RenderUtils.ColoredLine(new class_243(x0, y, z0), new class_243(x1, y, z0), c, w));
        lines.add(new RenderUtils.ColoredLine(new class_243(x1, y, z0), new class_243(x1, y, z1), c, w));
        lines.add(new RenderUtils.ColoredLine(new class_243(x1, y, z1), new class_243(x0, y, z1), c, w));
        lines.add(new RenderUtils.ColoredLine(new class_243(x0, y, z1), new class_243(x0, y, z0), c, w));
    }

    private static Color withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }
}
