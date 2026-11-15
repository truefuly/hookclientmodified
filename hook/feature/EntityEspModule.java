package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.StorageBlocksSetting;
import hook.utils.EncryptedString;
import hook.utils.EspStyle;
import hook.utils.RenderUtils;
import java.awt.Color;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1569;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_1826;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7923;

/** Shows any selected entities (players, every mob, ...) through walls with a configurable box, glow and tracers. */
public final class EntityEspModule extends Module {
    public enum DefaultColor { Type, Theme, Health, Distance }

    private final StorageBlocksSetting entities = createList();
    private final EspStyle style = new EspStyle(EspStyle.Shape.Both, 45, 0xFFFF5A6E);
    private final ModeSetting<DefaultColor> defaultColor = new ModeSetting<DefaultColor>(EncryptedString.of("Default Colors"), DefaultColor.Type, DefaultColor.class).setDescription(EncryptedString.of("Colours in Default colour mode. Type: players red, hostile mobs orange, others green. Theme: client accent. Health: green to red. Distance: red close, green far"));
    private final NumberSetting expand = new NumberSetting(EncryptedString.of("Expand"), 0.0, 0.5, 0.05, 0.01).getValue(EncryptedString.of("Makes the box a bit bigger than the entity"));
    private final BooleanSetting invisible = new BooleanSetting(EncryptedString.of("Show Invisible"), true).setDescription(EncryptedString.of("Also shows invisible entities"));

    public EntityEspModule() {
        super(EncryptedString.of("Entity ESP"), EncryptedString.of("Renders the selected players and mobs through walls"), -1, Category.RENDER);
        this.entities.setDescription(EncryptedString.of("Entities that get highlighted"));
        this.addSettings(this.entities);
        this.addSettings(this.style.settings());
        this.addSettings(this.defaultColor, this.expand, this.invisible);
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) return;
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null || !camera.method_19332() || !this.style.begin(camera)) return;
        float tickProgress = camera.method_55437();
        double grow = this.expand.getValue();
        int index = 0;
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (entity == this.mc.field_1724 || entity == this.mc.method_1560() || entity.method_31481()) continue;
            if (entity instanceof class_1309 living && !living.method_5805()) continue;
            if (!this.invisible.getValue() && entity.method_5767()) continue;
            if (!this.selected(entity)) continue;
            class_243 pos = entity.method_30950(tickProgress);
            double half = entity.method_17681() * 0.5 + grow;
            this.style.box(pos.field_1352 - half, pos.field_1351 - grow, pos.field_1350 - half,
                    pos.field_1352 + half, pos.field_1351 + entity.method_17682() + grow, pos.field_1350 + half,
                    this.colorFor(entity, pos), index++);
        }
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        RenderUtils.applyCameraTransform(matrices, camera);
        this.style.render(matrices);
        matrices.method_22909();
    }

    private boolean selected(class_1297 entity) {
        class_2960 id = class_7923.field_41177.method_10221(entity.method_5864());
        StorageBlocksSetting.Entry e = id == null ? null : this.entities.get(id.toString());
        return e != null && e.enabled();
    }

    private Color colorFor(class_1297 entity, class_243 pos) {
        switch ((DefaultColor) this.defaultColor.getValue()) {
            case Health: {
                if (entity instanceof class_1309 living) {
                    float max = Math.max(1.0f, living.method_6063());
                    float t = Math.max(0.0f, Math.min(1.0f, (living.method_6032() + living.method_6067()) / max));
                    return Color.getHSBColor(t * 0.33f, 0.75f, 1.0f);
                }
                return Color.WHITE;
            }
            case Distance: {
                double d = Math.sqrt(this.mc.field_1724.method_5707(pos));
                float t = (float) Math.max(0.0, Math.min(1.0, d / 48.0));
                return Color.getHSBColor(t * 0.33f, 0.75f, 1.0f);
            }
            case Theme: {
                Color c = UiPalette.accentColor(255);
                return new Color(c.getRed(), c.getGreen(), c.getBlue());
            }
            default: {
                if (entity instanceof class_1657) return new Color(0xFF5A6E);
                if (entity instanceof class_1569) return new Color(0xFF9A3C);
                return new Color(0x6EE38B);
            }
        }
    }

    private static StorageBlocksSetting createList() {
        StorageBlocksSetting list = new StorageBlocksSetting(EncryptedString.of("Entities")).pickerOnly();
        for (class_1299<?> type : class_7923.field_41177) {
            class_2960 id = class_7923.field_41177.method_10221(type);
            if (id == null) continue;
            class_1826 egg = class_1826.method_8019(type);
            boolean player = id.method_12832().equals("player");
            class_1792 icon = egg != null ? (class_1792) egg : player ? class_1802.field_8575 : class_1802.field_8803;
            list.addEntity(id.toString(), type.method_5897().getString(), icon, type, player, Color.WHITE);
        }
        return list;
    }
}
