/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1792
 */
package hook.module.setting;

import hook.module.setting.Setting;
import net.minecraft.class_1792;

public class ItemSetting
extends Setting {
    private final class_1792 defaultValue;
    private class_1792 value;

    public ItemSetting(CharSequence name, class_1792 value) {
        super(name);
        this.value = value;
        this.defaultValue = value;
    }

    public class_1792 getItem() {
        return this.value;
    }

    public void setItem(class_1792 a) {
        this.value = a;
    }

    public class_1792 getDefaultValue() {
        return this.defaultValue;
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }
}

