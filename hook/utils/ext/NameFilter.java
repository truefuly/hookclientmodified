/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.utils.ext;

import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class NameFilter {
    private static volatile UnaryOperator<String> filter = UnaryOperator.identity();

    private NameFilter() {
    }

    public static String apply(String var0) {
        if (var0 == null) {
            return null;
        }
        String var1 = (String)filter.apply(var0);
        return var1 != null ? var1 : var0;
    }

    public static void set(UnaryOperator<String> var0) {
        filter = var0 != null ? var0 : UnaryOperator.identity();
    }
}

