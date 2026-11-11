/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1661
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 */
package hook.utils;

import hook.Hook;
import hook.mixin.ClientPlayerInteractionManagerAccessor;
import java.util.function.Predicate;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1792;
import net.minecraft.class_1799;

public final class InventoryUtil {
    public static void swap(int selectedSlot) {
        if (selectedSlot >= 0 && selectedSlot <= 8) {
            Hook.mc.field_1724.method_31548().method_61496(selectedSlot);
            ((ClientPlayerInteractionManagerAccessor)Hook.mc.field_1761).syncSlot();
        }
    }

    public static boolean swapStack(Predicate<class_1799> predicate) {
        class_1661 getInventory = Hook.mc.field_1724.method_31548();
        for (int i = 0; i < 9; ++i) {
            if (!predicate.test(getInventory.method_5438(i))) continue;
            getInventory.method_61496(i);
            return true;
        }
        return false;
    }

    public static boolean swapItem(Predicate<class_1792> predicate) {
        class_1661 getInventory = Hook.mc.field_1724.method_31548();
        for (int i = 0; i < 9; ++i) {
            if (!predicate.test(getInventory.method_5438(i).method_7909())) continue;
            getInventory.method_61496(i);
            return true;
        }
        return false;
    }

    public static boolean swap(class_1792 item) {
        return InventoryUtil.swapItem(item2 -> item2 == item);
    }

    public static int getSlot(class_1792 obj) {
        class_1703 currentScreenHandler = Hook.mc.field_1724.field_7512;
        if (Hook.mc.field_1724.field_7512 instanceof class_1707) {
            int n = 0;
            for (int i = 0; i < ((class_1707)Hook.mc.field_1724.field_7512).method_17388() * 9; ++i) {
                if (!currentScreenHandler.method_7611(i).method_7677().method_7909().equals(obj)) continue;
                ++n;
            }
            return n;
        }
        return 0;
    }
}

