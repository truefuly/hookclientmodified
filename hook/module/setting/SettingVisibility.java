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

    /** Hides the given settings until {@code shown} is true, keeping any rule they already have. */
    public static void advanced(BooleanSupplier shown, Setting... settings) {
        for (Setting s : settings) {
            if (s == null) continue;
            BooleanSupplier old = RULES.get(s);
            RULES.put(s, old == null ? shown : () -> shown.getAsBoolean() && old.getAsBoolean());
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

