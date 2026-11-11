/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.NewFeature;
import java.util.ArrayList;
import java.util.List;

public final class ModuleRecency {
    private ModuleRecency() {
    }

    public static List<Module> newFirst(List<Module> var0) {
        boolean var1 = false;
        boolean var2 = false;
        for (Module var4 : var0) {
            if (ModuleRecency.fresh(var4)) {
                if (!var1) continue;
                var2 = true;
                break;
            }
            var1 = true;
        }
        if (!var2) {
            return var0;
        }
        ArrayList<Module> var6 = new ArrayList<Module>(var0.size());
        for (Module var5 : var0) {
            if (!ModuleRecency.fresh(var5)) continue;
            var6.add(var5);
        }
        for (Module var9 : var0) {
            if (ModuleRecency.fresh(var9)) continue;
            var6.add(var9);
        }
        return var6;
    }

    private static boolean fresh(Module var0) {
        return var0 instanceof NewFeature && (var0.getCategory() == Category.DONUT || var0.getCategory() == Category.GLAZED);
    }
}

