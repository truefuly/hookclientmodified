/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1792
 */
package hook.module.setting;

import hook.module.setting.Setting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_1792;

public final class StorageBlocksSetting
extends Setting {
    private final List<Entry> entries = new ArrayList<Entry>();
    private boolean colorOnly;
    private boolean pickerOnly;

    public StorageBlocksSetting(CharSequence var1) {
        super(var1);
    }

    public StorageBlocksSetting colorOnly() {
        this.colorOnly = true;
        return this;
    }

    public boolean isColorOnly() {
        return this.colorOnly && !this.entries.isEmpty();
    }

    public StorageBlocksSetting pickerOnly() {
        this.pickerOnly = true;
        return this;
    }

    public boolean isPickerOnly() {
        return this.pickerOnly;
    }

    public StorageBlocksSetting add(String var1, String var2, class_1792 var3, boolean var4, Color var5) {
        this.entries.add(new Entry(var1, var2, var3, var4, var5));
        return this;
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(this.entries);
    }

    public Entry get(String var1) {
        for (Entry var3 : this.entries) {
            if (!var3.id.equals(var1)) continue;
            return var3;
        }
        return null;
    }

    public int selectedCount() {
        int var1 = 0;
        for (Entry var3 : this.entries) {
            if (!var3.enabled) continue;
            ++var1;
        }
        return var1;
    }

    public void clear() {
        for (Entry var2 : this.entries) {
            var2.enabled = false;
        }
    }

    public void reset() {
        for (Entry var2 : this.entries) {
            var2.reset();
        }
    }

    public static final class Entry {
        private final String id;
        private final String displayName;
        private final class_1792 item;
        private final boolean defaultEnabled;
        private final Color defaultColor;
        private boolean enabled;
        private Color color;

        private Entry(String var1, String var2, class_1792 var3, boolean var4, Color var5) {
            this.id = var1;
            this.displayName = var2;
            this.item = var3;
            this.defaultEnabled = var4;
            this.defaultColor = var5;
            this.enabled = var4;
            this.color = var5;
        }

        public String id() {
            return this.id;
        }

        public String displayName() {
            return this.displayName;
        }

        public class_1792 item() {
            return this.item;
        }

        public boolean enabled() {
            return this.enabled;
        }

        public Color color() {
            return this.color;
        }

        public void setEnabled(boolean var1) {
            this.enabled = var1;
        }

        public void toggle() {
            this.enabled = !this.enabled;
        }

        public void setColor(Color var1) {
            this.color = new Color(var1.getRed(), var1.getGreen(), var1.getBlue());
        }

        private void reset() {
            this.enabled = this.defaultEnabled;
            this.color = this.defaultColor;
        }
    }
}

