/*
 * Decompiled with CFR 0.152.
 */
package hook.module.setting;

import hook.module.setting.Setting;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;

public final class SettingVisibility {
    private static final Map<Setting, BooleanSupplier> RULES = new WeakHashMap<Setting, BooleanSupplier>();

    private SettingVisibility() {
    }

    public static void set(Setting var0, BooleanSupplier var1) {
        if (var0 != null) {
            RULES.put(var0, var1);
        }
    }

    public static boolean visible(Setting var0) {
        BooleanSupplier var1 = RULES.get(var0);
        try {
            return var1 == null || var1.getAsBoolean();
        }
        catch (Throwable var3) {
            return true;
        }
    }
}

