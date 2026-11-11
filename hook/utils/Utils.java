/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  org.joml.Vector3d
 */
package hook.utils;

import hook.feature.UiPalette;
import java.awt.Color;
import java.io.File;
import java.net.URISyntaxException;
import net.minecraft.class_243;
import org.joml.Vector3d;

public final class Utils {
    public static Color getMainColor(int var0, int var1) {
        return UiPalette.accentColor(var0);
    }

    public static File getCurrentJarPath() throws URISyntaxException {
        return new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
    }

    public static void overwriteFile(String var0, File var1) {
    }

    public static void copyVector(Vector3d var0, class_243 var1) {
        var0.x = var1.field_1352;
        var0.y = var1.field_1351;
        var0.z = var1.field_1350;
    }
}

