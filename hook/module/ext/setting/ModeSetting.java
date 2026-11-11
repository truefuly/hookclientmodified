/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext.setting;

import hook.module.ext.setting.Setting;
import hook.module.setting.ChoiceSetting;
import hook.utils.EncryptedString;
import java.util.List;

public final class ModeSetting
extends Setting {
    private final ChoiceSetting backing;

    public ModeSetting(String var1, String var2, String ... var3) {
        super(var1);
        this.backing = new ChoiceSetting(EncryptedString.of(var1), var2, List.of(var3));
    }

    public String get() {
        return this.backing.getMode();
    }

    public boolean is(String var1) {
        return this.get().equals(var1);
    }

    public void set(String var1) {
        String var2 = this.get();
        if (this.backing.setMode(var1) && !var2.equals(var1)) {
            this.notifyChanged();
        }
    }

    public String value() {
        return this.get();
    }

    public List<String> modes() {
        return this.backing.getModes();
    }

    public void setModes(List<String> var1) {
        String var2 = this.get();
        this.backing.setModes(var1);
        if (!var2.equals(this.get())) {
            this.notifyChanged();
        }
    }

    public void next() {
        this.backing.cycleUp();
        this.notifyChanged();
    }

    @Override
    public hook.module.setting.Setting hook() {
        return this.backing;
    }

    @Override
    protected Object snapshot() {
        return this.get();
    }
}

