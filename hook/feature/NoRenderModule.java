package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.StorageBlocksSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_1826;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

import java.awt.Color;

/** Like Meteor's No Render entity list: the ticked entity types are not drawn by your client at all. */
public final class NoRenderModule extends Module {
    private static NoRenderModule instance;
    private final StorageBlocksSetting entities = createList();

    public NoRenderModule() {
        super(EncryptedString.of("No Render"), EncryptedString.of("Stops your client from rendering the entities you select"), -1, Category.RENDER);
        this.entities.setDescription(EncryptedString.of("Ticked entities are not rendered"));
        this.addSettings(this.entities);
        instance = this;
    }

    /** Whether this entity should be skipped. Never hides you or the camera entity. */
    public static boolean hides(class_1297 entity) {
        NoRenderModule m = instance;
        if (m == null || !m.isEnabled() || entity == null) return false;
        if (entity == m.mc.field_1724 || entity == m.mc.method_1560()) return false;
        class_2960 id = class_7923.field_41177.method_10221(entity.method_5864());
        StorageBlocksSetting.Entry e = id == null ? null : m.entities.get(id.toString());
        return e != null && e.enabled();
    }

    private static StorageBlocksSetting createList() {
        StorageBlocksSetting list = new StorageBlocksSetting(EncryptedString.of("Entities")).pickerOnly();
        for (class_1299<?> type : class_7923.field_41177) {
            class_2960 id = class_7923.field_41177.method_10221(type);
            if (id == null) continue;
            class_1826 egg = class_1826.method_8019(type);
            class_1792 icon = egg != null ? (class_1792) egg : class_1802.field_8803;
            list.add(id.toString(), type.method_5897().getString(), icon, false, Color.WHITE);
        }
        return list;
    }
}
