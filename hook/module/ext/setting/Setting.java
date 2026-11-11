/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext.setting;

import hook.module.setting.SettingVisibility;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public abstract class Setting {
    private final String name;
    private String id;
    private final List<Runnable> listeners = new ArrayList<Runnable>();
    private BooleanSupplier visible;
    private Object lastSeen;

    protected Setting(String var1) {
        this.name = var1;
        this.id = var1;
    }

    public final String id() {
        return this.id;
    }

    public final void bindId(String var1) {
        this.id = var1;
    }

    public final String name() {
        return this.name;
    }

    public final Setting onChange(Runnable var1) {
        this.listeners.add(var1);
        return this;
    }

    public final Setting visibleWhen(BooleanSupplier var1) {
        this.visible = var1;
        SettingVisibility.set(this.hook(), var1);
        return this;
    }

    public final boolean isVisible() {
        try {
            return this.visible == null || this.visible.getAsBoolean();
        }
        catch (Throwable var2) {
            return true;
        }
    }

    public abstract hook.module.setting.Setting hook();

    protected abstract Object snapshot();

    public final void notifyChanged() {
        this.lastSeen = this.snapshot();
        this.fire();
    }

    public final void poll() {
        Object var1 = this.snapshot();
        if (this.lastSeen == null) {
            this.lastSeen = var1;
        } else if (!Objects.equals(var1, this.lastSeen)) {
            this.lastSeen = var1;
            this.fire();
        }
    }

    private void fire() {
        for (int var1 = 0; var1 < this.listeners.size(); ++var1) {
            try {
                this.listeners.get(var1).run();
                continue;
            }
            catch (Throwable var3) {
                System.err.println("Hook: setting listener failed for " + this.name + ": " + String.valueOf(var3));
            }
        }
    }
}

