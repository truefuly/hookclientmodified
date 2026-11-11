/*
 * Decompiled with CFR 0.152.
 */
package hook.module.ext;

import hook.Hook;
import hook.module.Module;

public final class ModuleRegistry {
    private ModuleRegistry() {
    }

    public static <T extends Module> T get(Class<T> var0) {
        try {
            Module var1 = Hook.INSTANCE.getModuleManager().getModuleByClass(var0);
            return (T)(var0.isInstance(var1) ? (Module)var0.cast(var1) : null);
        }
        catch (Throwable var2) {
            return null;
        }
    }
}

