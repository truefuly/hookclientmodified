/*
 * Decompiled with CFR 0.152.
 */
package hook.module.setting;

import hook.module.setting.Setting;
import java.util.Arrays;
import java.util.List;

public class ModeSetting<T extends Enum<T>>
extends Setting {
    public int index;
    private final List<T> possibleValues;
    private final int originalValue;

    public ModeSetting(CharSequence charSequence, T defaultValue, Class<T> type) {
        super(charSequence);
        this.possibleValues = Arrays.asList(type.getEnumConstants());
        this.originalValue = this.index = this.possibleValues.indexOf(defaultValue);
    }

    public Enum<T> getValue() {
        return (Enum)this.possibleValues.get(this.index);
    }

    public void setMode(Enum<T> enum1) {
        this.index = this.possibleValues.indexOf(enum1);
    }

    public void setModeIndex(int a) {
        this.index = a;
    }

    public int getModeIndex() {
        return this.index;
    }

    public int getOriginalValue() {
        return this.originalValue;
    }

    public void cycleUp() {
        this.index = this.index < this.possibleValues.size() - 1 ? ++this.index : 0;
    }

    public void cycleDown() {
        this.index = this.index > 0 ? --this.index : this.possibleValues.size() - 1;
    }

    public boolean isMode(Enum<T> enum1) {
        return this.index == this.possibleValues.indexOf(enum1);
    }

    public List<T> getPossibleValues() {
        return this.possibleValues;
    }

    @Override
    public ModeSetting<T> setDescription(CharSequence charSequence) {
        super.setDescription(charSequence);
        return this;
    }
}

