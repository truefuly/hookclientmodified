/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package thetruetrident.swordcore;

import java.lang.reflect.Method;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class SpearcoreCompatBridge {
    private static boolean lookedUp;
    private static Method activeMethod;
    private static Method bypassMethod;

    private SpearcoreCompatBridge() {
    }

    public static boolean isLungeSwapClickActive() {
        if (SpearcoreCompatBridge.isSpearcoreAttackCall()) {
            return true;
        }
        if (!lookedUp) {
            lookedUp = true;
            try {
                Class<?> compat = Class.forName("thetruetrident.spearcore.SpearcoreCompat");
                activeMethod = compat.getMethod("isLungeSwapClickActive", new Class[0]);
                bypassMethod = compat.getMethod("shouldBypassSwordcoreFreeHitting", new Class[0]);
            }
            catch (ReflectiveOperationException ignored) {
                activeMethod = null;
                bypassMethod = null;
            }
        }
        if (activeMethod == null && bypassMethod == null) {
            return false;
        }
        try {
            return activeMethod != null && Boolean.TRUE.equals(activeMethod.invoke(null, new Object[0])) || bypassMethod != null && Boolean.TRUE.equals(bypassMethod.invoke(null, new Object[0]));
        }
        catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static boolean isSpearcoreAttackCall() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();
            if (!className.startsWith("thetruetrident.spearcore.")) continue;
            return true;
        }
        return false;
    }
}

