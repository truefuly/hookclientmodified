/*
 * Decompiled with CFR 0.152.
 */
package hook.compat.meteorclient.systems.modules;

import hook.Hook;
import hook.compat.meteorclient.systems.modules.Category;
import hook.compat.meteorclient.systems.modules.Module;
import java.util.Optional;

public final class Modules {
    private static Modules instance;

    private Modules() {
    }

    public static Modules get() {
        if (instance == null) {
            instance = new Modules();
        }
        return instance;
    }

    public void add(Module module) {
        Hook.INSTANCE.getModuleManager().a(module);
    }

    public <T extends Module> T get(Class<T> type) {
        return (T)((Module)Hook.INSTANCE.getModuleManager().getModuleByClass(type));
    }

    public <T extends Module> Optional<T> getOptional(Class<T> type) {
        return Optional.ofNullable((Module)Hook.INSTANCE.getModuleManager().getModuleByClass(type));
    }

    public static void registerCategory(Category category) {
    }
}

