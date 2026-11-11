/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext;

import hook.module.Module;
import hook.module.ext.ExtBridge;
import hook.module.ext.ModuleCategory;
import hook.module.ext.Stage;
import hook.module.ext.event.EventType;
import hook.module.ext.setting.BooleanSetting;
import hook.module.ext.setting.Setting;
import hook.utils.EncryptedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ClientModule
extends Module {
    private final String id;
    private final List<Setting> extSettings = new ArrayList<Setting>();
    private BooleanSetting notifications;

    protected ClientModule(String var1, String var2, ModuleCategory var3) {
        this(var1.toLowerCase(Locale.ROOT).replace(' ', '-'), var1, var2, var3);
    }

    protected ClientModule(String var1, String var2, String var3, ModuleCategory var4) {
        super(EncryptedString.of(var2), EncryptedString.of(var3), -1, var4.hook());
        this.id = var1;
        ExtBridge.track(this);
    }

    protected final <S extends Setting> S addSetting(S var1) {
        return this.addSetting(var1.id(), var1);
    }

    protected final <S extends Setting> S addSetting(String var1, S var2) {
        var2.bindId(var1);
        this.extSettings.add(var2);
        super.addSetting(var2.hook());
        return var2;
    }

    public final List<Setting> extSettings() {
        return Collections.unmodifiableList(this.extSettings);
    }

    protected final BooleanSetting usesNotifications() {
        if (this.notifications == null) {
            this.notifications = this.addSetting("notifications", new BooleanSetting("Notifications", true));
        }
        return this.notifications;
    }

    public final boolean notificationsOn() {
        return this.notifications == null || this.notifications.get();
    }

    public final boolean toggleMessageOn() {
        return false;
    }

    public final String id() {
        return this.id;
    }

    public final String name() {
        return this.getName().toString();
    }

    public final String description() {
        return this.getDescription() == null ? "" : this.getDescription().toString();
    }

    public boolean isAction() {
        return false;
    }

    public boolean persistEnabled() {
        return true;
    }

    protected final void requestDisable() {
        ExtBridge.later(() -> this.toggle(false));
    }

    protected final <E> void on(EventType<E> var1, Consumer<E> var2) {
        ExtBridge.subscribe(this, var1.type(), Stage.PRE, var2, this.isEnabled());
    }

    protected final <E> void on(EventType<E> var1, Stage var2, Consumer<E> var3) {
        ExtBridge.subscribe(this, var1.type(), var2, var3, this.isEnabled());
    }

    @Override
    public void toggle() {
        super.toggle();
        if (!this.isEnabled()) {
            ExtBridge.dropOwned(this);
        }
    }

    @Override
    public void toggle(boolean var1) {
        super.toggle(var1);
        if (!this.isEnabled()) {
            ExtBridge.dropOwned(this);
        }
    }
}

