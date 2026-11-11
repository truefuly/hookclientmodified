/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1937
 *  net.minecraft.class_638
 *  net.minecraft.class_7134
 */
package hook.utils.ext;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1937;
import net.minecraft.class_638;
import net.minecraft.class_7134;

@Environment(value=EnvType.CLIENT)
public final class LevelUtil {
    private LevelUtil() {
    }

    public static boolean isOverworld(class_638 var0) {
        return var0.method_27983() == class_1937.field_25179 || var0.method_40134().method_40225(class_7134.field_37666);
    }

    public static boolean isLoaded(class_638 var0, int var1, int var2) {
        return var0.method_2935().method_12123(var1, var2);
    }
}

