/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class VanishSignalClassifier {
    private VanishSignalClassifier() {
    }

    public static boolean suspiciousSound(String var0) {
        String var1 = VanishSignalClassifier.path(var0);
        return var1.contains("chest.open") || var1.contains("chest.close") || var1.contains("chest.locked") || var1.contains("barrel.open") || var1.contains("barrel.close") || var1.contains("shulker_box.open") || var1.contains("shulker_box.close") || var1.contains("door.open") || var1.contains("door.close") || var1.contains("fence_gate.open") || var1.contains("fence_gate.close") || var1.contains("button.click") || var1.contains("lever.click");
    }

    public static boolean suspiciousParticle(String var0) {
        String var1 = VanishSignalClassifier.path(var0);
        return var1.contains("crit") || var1.contains("enchanted_hit") || var1.contains("damage_indicator") || var1.contains("smoke") || var1.equals("block");
    }

    public static String path(String var0) {
        if (var0 == null) {
            return "unknown";
        }
        int var1 = var0.indexOf(58);
        return (var1 >= 0 && var1 + 1 < var0.length() ? var0.substring(var1 + 1) : var0).toLowerCase(Locale.ROOT);
    }
}

