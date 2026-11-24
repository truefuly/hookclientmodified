package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.EspStyle;
import hook.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_4587;

/**
 * Shared part of Player ESP and Mob ESP: walks the loaded entities, lets the module pick and colour them, draws them
 * with the usual ESP style (box / corners / glow / tracers) and an optional health bar beside each one.
 */
abstract class EntityEspBase extends Module {
    protected final EspStyle style;
    protected final NumberSetting expand = new NumberSetting(EncryptedString.of("Expand"), 0.0, 0.5, 0.05, 0.01).getValue(EncryptedString.of("Makes the box a bit bigger than the entity"));
    protected final BooleanSetting invisible = new BooleanSetting(EncryptedString.of("Show Invisible"), true).setDescription(EncryptedString.of("Also shows invisible ones"));
    protected final BooleanSetting healthBar;

    private final List<RenderUtils.ColoredBox> bars = new ArrayList<>();

    protected EntityEspBase(String name, String description, Category category, int defaultFill, int defaultColor, boolean bar) {
        super(EncryptedString.of(name), EncryptedString.of(description), -1, category);
        this.style = new EspStyle(EspStyle.Shape.Both, defaultFill, defaultColor);
        this.healthBar = new BooleanSetting(EncryptedString.of("Health Bar"), bar).setDescription(EncryptedString.of("A bar beside each one: green full health, red almost dead (absorption counts)"));
    }

    /** Whether this entity gets drawn (the player, the camera and dead / removed ones are filtered already). */
    protected abstract boolean wants(class_1297 entity);

    /** The colour in the style's Default colour mode. */
    protected abstract Color color(class_1297 entity, class_243 pos);

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) return;
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null || !camera.method_19332() || !this.style.begin(camera)) return;
        float tick = camera.method_55437();
        double grow = this.expand.getValue();
        class_243 cam = camera.method_71156();
        this.bars.clear();
        int index = 0;
        for (class_1297 e : this.mc.field_1687.method_18112()) {
            if (e == this.mc.field_1724 || e == this.mc.method_1560() || e.method_31481() || NoRenderModule.hides(e)) continue;
            if (e instanceof class_1309 living && !living.method_5805()) continue;
            if (!this.invisible.getValue() && e.method_5767()) continue;
            if (!this.wants(e)) continue;
            class_243 pos = e.method_30950(tick);
            if (!this.style.inRange(pos.field_1352, pos.field_1351, pos.field_1350)) continue;
            double half = e.method_17681() * 0.5 + grow;
            double h = e.method_17682();
            this.style.box(pos.field_1352 - half, pos.field_1351 - grow, pos.field_1350 - half,
                    pos.field_1352 + half, pos.field_1351 + h + grow, pos.field_1350 + half, this.color(e, pos), index++);
            if (this.healthBar.getValue() && e instanceof class_1309 living) this.bar(living, pos, half, h + grow * 2, cam);
        }
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        RenderUtils.applyCameraTransform(matrices, camera);
        this.style.render(matrices);
        if (!this.bars.isEmpty()) RenderUtils.renderFilledBoxes(matrices, this.bars);
        matrices.method_22909();
    }

    /** A thin upright bar on the right side of the box as seen from the camera. */
    private void bar(class_1309 e, class_243 pos, double half, double height, class_243 cam) {
        double dx = pos.field_1352 - cam.field_1352, dz = pos.field_1350 - cam.field_1350;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.5) return;
        // right of the line of sight
        double rx = -dz / len, rz = dx / len;
        double off = half + 0.18, w = 0.045;
        double cx = pos.field_1352 + rx * off, cz = pos.field_1350 + rz * off;
        float max = Math.max(1.0f, e.method_6063());
        float t = Math.max(0.0f, Math.min(1.0f, (e.method_6032() + e.method_6067()) / max));
        double y0 = pos.field_1351;
        this.bars.add(new RenderUtils.ColoredBox(cx - w, y0, cz - w, cx + w, y0 + height, cz + w, new Color(10, 10, 14, 150)));
        Color c = Color.getHSBColor(t * 0.33f, 0.8f, 1.0f);
        this.bars.add(new RenderUtils.ColoredBox(cx - w * 0.7, y0, cz - w * 0.7, cx + w * 0.7, y0 + height * t, cz + w * 0.7, new Color(c.getRed(), c.getGreen(), c.getBlue(), 220)));
    }

    /** Green (full) to red (almost dead). */
    protected static Color health(class_1297 e) {
        if (!(e instanceof class_1309 living)) return Color.WHITE;
        float max = Math.max(1.0f, living.method_6063());
        float t = Math.max(0.0f, Math.min(1.0f, (living.method_6032() + living.method_6067()) / max));
        return Color.getHSBColor(t * 0.33f, 0.75f, 1.0f);
    }

    /** Red close, green far (48 blocks). */
    protected Color distance(class_243 pos) {
        double d = Math.sqrt(this.mc.field_1724.method_5707(pos));
        float t = (float) Math.max(0.0, Math.min(1.0, d / 48.0));
        return Color.getHSBColor(t * 0.33f, 0.75f, 1.0f);
    }
}
