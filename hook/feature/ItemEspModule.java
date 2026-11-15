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

    private final ModeSetting<Filter> filter = new ModeSetting<Filter>(EncryptedString.of("Items"), Filter.All, Filter.class).setDescription(EncryptedString.of("All dropped items or only the ones ticked in the list"));
    private final StorageBlocksSetting list = createList();
    private final EspStyle style = new EspStyle(EspStyle.Shape.Both, 50, 0xFFFFD24D);

    public ItemEspModule() {
        super(EncryptedString.of("Item ESP"), EncryptedString.of("Renders dropped items through walls"), -1, Category.RENDER);
        this.list.setDescription(EncryptedString.of("Items shown in Selected mode"));
        SettingVisibility.set(this.list, () -> this.filter.getValue() == Filter.Selected);
        this.addSettings(this.filter, this.list);
        this.addSettings(this.style.settings());
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) return;
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null || !camera.method_19332() || !this.style.begin(camera)) return;
        float tickProgress = camera.method_55437();
        int index = 0;
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (!(entity instanceof class_1542 item) || item.method_31481() || NoRenderModule.hides(item)) continue;
            class_1792 type = item.method_6983().method_7909();
            if (this.filter.getValue() == Filter.Selected) {
                class_2960 id = class_7923.field_41178.method_10221(type);
                StorageBlocksSetting.Entry e = id == null ? null : this.list.get(id.toString());
                if (e == null || !e.enabled()) continue;
            }
            class_243 pos = item.method_30950(tickProgress);
            double half = 0.2;
            this.style.box(pos.field_1352 - half, pos.field_1351, pos.field_1350 - half,
                    pos.field_1352 + half, pos.field_1351 + 0.4, pos.field_1350 + half, new Color(0xFFD24D), index++);
        }
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        RenderUtils.applyCameraTransform(matrices, camera);
        this.style.render(matrices);
        matrices.method_22909();
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
