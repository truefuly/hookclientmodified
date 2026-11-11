/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_5250
 */
package hook.compat.meteorclient.systems.modules;

import hook.compat.meteorclient.settings.SettingGroups;
import hook.compat.meteorclient.systems.modules.Category;
import hook.compat.orbit.EventBus;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5250;

public abstract class Module
extends hook.module.Module {
    public final String name;
    public final String title;
    public SettingGroups settings;

    public Module(Category category, String name, String description) {
        super(Module.formatName(name), description, -1, category == null ? hook.module.Category.AUTOMATION : category.hook());
        this.name = name;
        this.title = Module.formatName(name);
        this.settings = new SettingGroups(this);
    }

    @Override
    public void onEnable() {
        EventBus.get().subscribe(this);
        super.onEnable();
        this.onActivate();
    }

    @Override
    public void onDisable() {
        EventBus.get().unsubscribe(this);
        super.onDisable();
        this.onDeactivate();
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    public boolean isActive() {
        return this.isEnabled();
    }

    public void activate() {
        this.toggle(true);
    }

    public void enable() {
        this.toggle(true);
    }

    public void disable() {
        this.toggle(false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean was = this.isEnabled();
        super.setEnabled(enabled);
        if (enabled == was) {
            return;
        }
        if (enabled) {
            EventBus.get().subscribe(this);
            super.onEnable();
            this.onActivate();
        } else {
            EventBus.get().unsubscribe(this);
            super.onDisable();
            this.onDeactivate();
        }
    }

    public String getInfoString() {
        return null;
    }

    public void info(String format, Object ... args) {
        this.chat("\u00a7f", format, args);
    }

    public void warning(String format, Object ... args) {
        this.chat("\u00a7e", format, args);
    }

    public void error(String format, Object ... args) {
        this.chat("\u00a7c", format, args);
    }

    private void chat(String color, String format, Object ... args) {
        String message;
        try {
            message = args == null || args.length == 0 ? format : String.format(format, args);
        }
        catch (RuntimeException e) {
            message = format;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1705 == null) {
            return;
        }
        class_5250 text = class_2561.method_43470((String)("\u00a77[\u00a7b" + this.title + "\u00a77] " + color + message));
        if (mc.method_18854()) {
            mc.field_1705.method_1743().method_1812((class_2561)text);
        } else {
            mc.execute(() -> Module.lambda$chat$0(mc, (class_2561)text));
        }
    }

    private static String formatName(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(raw.length() + 8);
        for (String word : raw.split("[-_]+")) {
            if (word.isEmpty()) continue;
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }

    private static /* synthetic */ void lambda$chat$0(class_310 mc, class_2561 text) {
        mc.field_1705.method_1743().method_1812(text);
    }
}

