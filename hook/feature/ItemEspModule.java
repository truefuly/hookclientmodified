package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.ModeSetting;
import hook.module.setting.SettingVisibility;
import hook.module.setting.StorageBlocksSetting;
import hook.utils.EncryptedString;
import hook.utils.EspStyle;
import hook.utils.RenderUtils;
import java.awt.Color;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1792;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7923;

/** Highlights dropped items through walls: all of them, or only the ones you pick. */
public final class ItemEspModule extends Module {
    public enum Filter { All, Selected }

    public enum Look { Frame, Box }

    private final ModeSetting<Look> look = new ModeSetting<Look>(EncryptedString.of("Look"), Look.Frame, Look.class).setDescription(EncryptedString.of("Frame: a clean rounded frame with a stack size badge. Box: 3D boxes with the style settings below"));
    private final float[] screen = new float[2];

    private final ModeSetting<Filter> filter = new ModeSetting<Filter>(EncryptedString.of("Items"), Filter.All, Filter.class).setDescription(EncryptedString.of("All dropped items or only the ones ticked in the list"));
    private final StorageBlocksSetting list = createList();
    private final EspStyle style = new EspStyle(EspStyle.Shape.Both, 50, 0xFFFFD24D);

    public ItemEspModule() {
        super(EncryptedString.of("Item ESP"), EncryptedString.of("Renders dropped items through walls"), -1, Category.RENDER);
        this.list.setDescription(EncryptedString.of("Items shown in Selected mode"));
        SettingVisibility.set(this.list, () -> this.filter.getValue() == Filter.Selected);
        this.addSettings(this.look, this.filter, this.list);
        this.addSettings(this.style.settings());
        hook.module.setting.SettingVisibility.advanced(() -> this.look.getValue() == Look.Box, this.style.shape, this.style.fillOpacity, this.style.outlineOpacity,
                this.style.outlineWidth, this.style.glow, this.style.glowStrength, this.style.pulse);
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) return;
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null || !camera.method_19332() || !this.style.begin(camera)) return;
        float tickProgress = camera.method_55437();
        int index = 0;
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (!this.wants(entity)) continue;
            class_243 pos = entity.method_30950(tickProgress);
            double half = 0.2;
            if (this.look.getValue() == Look.Frame) {
                this.style.tracer(pos.field_1352, pos.field_1351 + 0.2, pos.field_1350, new Color(0xFFD24D), index++);
                continue;
            }
            this.style.box(pos.field_1352 - half, pos.field_1351, pos.field_1350 - half,
                    pos.field_1352 + half, pos.field_1351 + 0.4, pos.field_1350 + half, new Color(0xFFD24D), index++);
        }
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        RenderUtils.applyCameraTransform(matrices, camera);
        this.style.render(matrices);
        matrices.method_22909();
    }

    private boolean wants(class_1297 entity) {
        if (!(entity instanceof class_1542 item) || item.method_31481() || NoRenderModule.hides(item)) return false;
        if (this.filter.getValue() == Filter.Selected) {
            class_2960 id = class_7923.field_41178.method_10221(item.method_6983().method_7909());
            StorageBlocksSetting.Entry e = id == null ? null : this.list.get(id.toString());
            return e != null && e.enabled();
        }
        return true;
    }

    private record Shown(class_1542 item, double dist, float x, float y, float w, float h) {
    }

    /** Frame look: a rounded frame around each item on screen, with a badge for the stack size. */
    @EventListener
    public void onRender2D(hook.event.events.Render2DEvent event) {
        if (this.look.getValue() != Look.Frame || this.mc.field_1687 == null || this.mc.field_1724 == null || this.mc.field_1690.field_1842) return;
        net.minecraft.class_1041 window = this.mc.method_22683();
        int ww = window.method_4489(), wh = window.method_4506();
        java.util.List<Shown> list = new java.util.ArrayList<>();
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (!this.wants(entity)) continue;
            class_243 p = entity.method_30950(event.tickDelta);
            double dist = hook.utils.RenderVisibility.cameraDistance(p.field_1352, p.field_1351 + 0.2, p.field_1350);
            if (!this.style.inRangeDist(dist)) continue;
            float x1 = Float.MAX_VALUE, y1 = Float.MAX_VALUE, x2 = -Float.MAX_VALUE, y2 = -Float.MAX_VALUE;
            boolean ok = true;
            for (int i = 0; i < 8 && ok; i++) {
                double cx = p.field_1352 + ((i & 1) == 0 ? -0.2 : 0.2), cy = p.field_1351 + ((i & 2) == 0 ? 0.0 : 0.45), cz = p.field_1350 + ((i & 4) == 0 ? -0.2 : 0.2);
                if (!hook.utils.RenderVisibility.project(cx, cy, cz, ww, wh, this.screen)) { ok = false; break; }
                x1 = Math.min(x1, this.screen[0]); y1 = Math.min(y1, this.screen[1]);
                x2 = Math.max(x2, this.screen[0]); y2 = Math.max(y2, this.screen[1]);
            }
            if (!ok || x2 < 0 || y2 < 0 || x1 > ww || y1 > wh) continue;
            list.add(new Shown((class_1542) entity, dist, x1, y1, x2 - x1, y2 - y1));
        }
        if (list.isEmpty()) return;
        list.sort((a, b) -> Double.compare(b.dist(), a.dist()));
        net.minecraft.class_332 ctx = event.context;
        float gui = Math.max(1.0f, (float) window.method_4495());
        ctx.method_51448().pushMatrix();
        ctx.method_51448().scale(1.0f / gui, 1.0f / gui);
        try {
            MinecraftUiRenderer ui = new MinecraftUiRenderer(ctx, gui);
            int index = 0;
            for (Shown s : list) this.drawFrame(ui, gui, s, index++);
        } finally {
            ctx.method_51448().popMatrix();
        }
    }

    private void drawFrame(UiRenderer ui, float u, Shown s, int index) {
        class_243 p = s.item().method_30950(1.0f);
        Color c = this.style.colorFor(new Color(0xFFD24D), index, p.field_1352, p.field_1351, p.field_1350);
        int rgb = c.getRGB() & 0xFFFFFF;
        float min = 9.0f * u, pad = 2.0f * u;
        float w = Math.max(min, s.w() + pad * 2), h = Math.max(min, s.h() + pad * 2);
        float x = s.x() + s.w() / 2 - w / 2, y = s.y() + s.h() / 2 - h / 2, r = Math.min(4.0f * u, Math.min(w, h) / 3);
        ui.pushAlpha((float) this.style.fadeAt(s.dist()));
        ui.shadow(x, y, w, h, r, 4.0f * u, 0x55000000 | rgb);
        ui.round(x, y, w, h, r, 0x1F000000 | rgb);
        ui.outline(x - u, y - u, w + 2 * u, h + 2 * u, r + u, Math.max(1.0f, 2.0f * u), 0x80000000);
        ui.outline(x, y, w, h, r, Math.max(1.0f, 1.2f * u), 0xFF000000 | rgb);
        int count = s.item().method_6983().method_7947();
        if (count > 1) {
            String t = String.valueOf(count);
            float fs = 6.0f * u, bh = 8.5f * u, bw = Math.max(bh, ui.textWidth(t, fs, 1) + 4.5f * u);
            float bx = x + w - bw / 2, by = y - bh / 2;
            ui.round(bx, by, bw, bh, bh / 2, 0xFF000000 | rgb);
            ui.textCentered(t, bx + bw / 2, by + bh / 2 - fs / 2, fs, 1, 0xFF101216);
        }
        ui.popAlpha();
    }

    private static StorageBlocksSetting createList() {
        StorageBlocksSetting list = new StorageBlocksSetting(EncryptedString.of("Item List")).pickerOnly();
        for (class_1792 item : class_7923.field_41178) {
            class_2960 id = class_7923.field_41178.method_10221(item);
            if (id == null || id.method_12832().equals("air")) continue;
            list.add(id.toString(), item.method_63680().getString(), item, false, Color.WHITE);
        }
        return list;
    }
}
