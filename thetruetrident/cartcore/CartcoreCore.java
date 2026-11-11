/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1764
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2338
 *  net.minecraft.class_239
 *  net.minecraft.class_243
 *  net.minecraft.class_2487
 *  net.minecraft.class_2558
 *  net.minecraft.class_2558$class_10608
 *  net.minecraft.class_2561
 *  net.minecraft.class_2680
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_5250
 *  net.minecraft.class_746
 *  net.minecraft.class_7923
 */
package thetruetrident.cartcore;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import thetruetrident.cartcore.B;
import thetruetrident.cartcore.CartcoreClient;
import thetruetrident.cartcore.cartConfig;

@Environment(value=EnvType.CLIENT)
public class CartcoreCore {
    public static cartConfig cartConfig = thetruetrident.cartcore.cartConfig.load();
    public static Integer targetPitch = null;
    public static float pitchSpeed = 5.0f;
    public static int lastPitchAssistTick = -1000;
    public static int cartActionLockUntilTick = -1000;

    public static class_2338 getTargetedBlockPos() {
        class_243 class_2432;
        class_243 class_2433;
        class_310 class_3102 = class_310.method_1551();
        class_746 class_7462 = class_3102.field_1724;
        if (class_7462 == null) {
            return null;
        }
        double d = class_7462.method_55754();
        class_243 class_2434 = class_7462.method_5836(1.0f);
        class_3965 class_39652 = class_3102.field_1687.method_17742(new class_3959(class_2434, class_2433 = class_2434.method_1019((class_2432 = class_7462.method_5828(1.0f)).method_1021(d)), class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)class_7462));
        if (class_39652 == null) {
            return null;
        }
        class_2680 class_26802 = class_3102.field_1687.method_8320(class_39652.method_17777());
        class_2960 class_29602 = class_7923.field_41175.method_10221(class_26802.method_26204());
        class_2487 class_24872 = new class_2487();
        class_24872.method_10582("BlockID", class_29602.toString());
        class_26802.method_11656().forEach((class_27692, comparable) -> class_24872.method_10582(class_27692.method_11899(), comparable.toString()));
        return class_39652.method_17777();
    }

    public static boolean isBlockInFrontOfFeet(class_1657 class_16572) {
        class_2338 class_23382 = class_16572.method_24515().method_10093(class_16572.method_5735());
        return !class_16572.method_73183().method_8320(class_23382).method_26215();
    }

    public static boolean checkIfCrossbowLoadedInHotbar(class_1657 class_16572) {
        if (class_16572 == null) {
            return false;
        }
        for (int i = 0; i <= 8; ++i) {
            class_1799 class_17992 = class_16572.method_31548().method_5438(i);
            if (!CartcoreCore.A(class_17992)) continue;
            return true;
        }
        return false;
    }

    public static boolean swapChargedCrossbowSlot(class_1657 class_16572) {
        if (class_16572 == null) {
            return false;
        }
        for (int i = 0; i <= 8; ++i) {
            class_1799 class_17992 = class_16572.method_31548().method_5438(i);
            if (!CartcoreCore.A(class_17992)) continue;
            class_16572.method_31548().method_61496(i);
            return true;
        }
        return false;
    }

    private static boolean A(class_1799 class_17992) {
        return class_17992 != null && class_17992.method_7909() instanceof class_1764 && class_1764.method_7781((class_1799)class_17992);
    }

    public static boolean checkIfInHotbar(class_1657 class_16572, String string) {
        int n = 0;
        for (class_1799 class_17992 : class_16572.method_31548()) {
            if (String.valueOf(class_17992.method_7909()).contains(string)) {
                return true;
            }
            if (++n <= 8) continue;
            break;
        }
        return class_310.method_1551().field_1724.method_6079().method_7909() == class_1802.field_8069 && "tnt_minecart".equals(string);
    }

    public static boolean checkIfInInventory(class_1657 class_16572, String string) {
        if (class_16572 == null) {
            return false;
        }
        int n = 0;
        for (class_1799 class_17992 : class_16572.method_31548()) {
            if (String.valueOf(class_17992.method_7909()).contains(string) && n > 8) {
                return true;
            }
            if (++n <= 35) continue;
            break;
        }
        return false;
    }

    public static boolean isCartAimItemHeld(class_1657 class_16572) {
        if (class_16572 == null) {
            return false;
        }
        return CartcoreCore.B(class_16572.method_6047()) || CartcoreCore.B(class_16572.method_6079());
    }

    public static boolean isCartAimActive(class_1657 class_16572) {
        if (class_16572 == null) {
            return false;
        }
        return CartcoreCore.isCartAimItemHeld(class_16572) || targetPitch != null || class_16572.field_6012 <= cartActionLockUntilTick;
    }

    public static float getPhysicalPitch(class_1657 class_16572) {
        if (class_16572 instanceof B) {
            B b = (B)class_16572;
            return b.getPhysicalPitch();
        }
        return class_16572.method_36455();
    }

    private static boolean B(class_1799 class_17992) {
        if (class_17992 == null || class_17992.method_7960()) {
            return false;
        }
        String string = String.valueOf(class_17992.method_7909());
        return class_17992.method_7909() == class_1802.field_8102 || class_17992.method_7909() == class_1802.field_8399 || class_17992.method_7909() == class_1802.field_8069 || class_17992.method_7909() == class_1802.field_8187 || class_17992.method_7909() == class_1802.field_8884 || string.contains("rail") || string.contains("slab");
    }

    public static boolean swapSlot(class_1657 class_16572, String string) {
        int n = 0;
        for (class_1799 class_17992 : class_16572.method_31548()) {
            if (String.valueOf(class_17992.method_7909()).contains(string)) {
                try {
                    class_310.method_1551().field_1724.method_31548().method_61496(n);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                return true;
            }
            if (++n <= 8) continue;
            break;
        }
        return false;
    }

    public static int getSlotInstance(class_1657 class_16572, int n) {
        int n2 = 0;
        for (class_1799 class_17992 : class_16572.method_31548()) {
            if (n <= n2 && class_17992.method_7909() != class_1802.field_8069) {
                return n2;
            }
            if (++n2 <= 9) continue;
            break;
        }
        return -1;
    }

    public static void playSoundSlow(class_3414 class_34142, class_310 class_3102, float f) {
        if (CartcoreCore.cartConfig.soundEffects) {
            class_3102.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)class_34142, (float)0.0f, (float)f));
        }
    }

    public static void playSoundRealPitch(class_3414 class_34142, class_310 class_3102, float f) {
        if (CartcoreCore.cartConfig.soundEffects) {
            class_3102.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)class_34142, (float)f, (float)1.0f));
        }
    }

    public static boolean checkIfPlaceAbove(class_310 class_3102) {
        class_239 class_2392 = class_3102.field_1765;
        if (class_2392 instanceof class_3965) {
            class_3965 class_39652 = (class_3965)class_2392;
            net.minecraft.class_2338 above = class_39652.method_17777().method_10093(class_39652.method_17780());
            if (class_3102.field_1724 != null && above.method_10264() <= class_3102.field_1724.method_31478()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCartAimLocked(class_1657 class_16572) {
        return class_16572 != null && class_16572.field_6012 <= cartActionLockUntilTick;
    }

    public static void releaseCartAim(class_310 class_3102) {
        class_746 class_7462;
        if (class_3102 != null && (class_7462 = class_3102.field_1724) instanceof B) {
            B b = (B)class_7462;
            float f = b.getLastPitch();
            if (f != 0.0f) {
                class_3102.field_1724.method_36457(f);
            }
            b.setLastPitch(0.0f);
        }
        targetPitch = null;
        pitchSpeed = 5.0f;
        cartActionLockUntilTick = -1000;
    }

    public static void holdCartAim(class_310 class_3102, int n) {
        if (class_3102 == null || class_3102.field_1724 == null) {
            return;
        }
        lastPitchAssistTick = class_3102.field_1724.field_6012;
        cartActionLockUntilTick = Math.max(cartActionLockUntilTick, class_3102.field_1724.field_6012 + n);
    }
}

