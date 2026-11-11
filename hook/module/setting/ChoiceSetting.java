/*
 * Decompiled with CFR 0.152.
 */
package hook.module.setting;

import hook.module.setting.Setting;
import java.util.List;

public final class ChoiceSetting
extends Setting {
    private List<String> modes;
    private int index;
    private final String defaultMode;

    public ChoiceSetting(CharSequence var1, String var2, List<String> var3) {
        super(var1);
        this.modes = List.copyOf(var3);
        this.defaultMode = var2;
        this.index = Math.max(0, this.modes.indexOf(var2));
    }

    public String getMode() {
        return this.modes.isEmpty() ? "" : this.modes.get(Math.min(this.index, this.modes.size() - 1));
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int var1) {
        if (!this.modes.isEmpty()) {
            this.index = Math.floorMod(var1, this.modes.size());
        }
    }

    public boolean setMode(String var1) {
        int var2 = this.modes.indexOf(var1);
        if (var2 >= 0) {
            this.index = var2;
            return true;
        }
        return false;
    }

    public List<String> getModes() {
        return this.modes;
    }

    public void setModes(List<String> var1) {
        if (!var1.isEmpty() && !var1.equals(this.modes)) {
            String var2 = this.getMode();
            this.modes = List.copyOf(var1);
            int var3 = this.modes.indexOf(var2);
            this.index = Math.max(0, var3);
        }
    }

    public void cycleUp() {
        this.setIndex(this.index + 1);
    }

    public void cycleDown() {
        this.setIndex(this.index - 1);
    }

    public String getDefaultMode() {
        return this.defaultMode;
    }
}

