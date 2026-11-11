/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1713
 *  net.minecraft.class_1799
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 */
package hook.compat.meteorclient.utils.player;

import hook.compat.meteorclient.utils.player.FindItemResult;
import java.util.function.Predicate;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_2680;
import net.minecraft.class_310;

public final class InvUtils {
    private static final class_310 mc = class_310.method_1551();

    private InvUtils() {
    }

    public static FindItemResult find(Predicate<class_1799> filter) {
        return InvUtils.scan(0, 36, filter);
    }

    public static FindItemResult findInHotbar(Predicate<class_1799> filter) {
        return InvUtils.scan(0, 9, filter);
    }

    public static FindItemResult findFastestTool(class_2680 state) {
        if (InvUtils.mc.field_1724 == null || state == null) {
            return new FindItemResult(-1, 0);
        }
        class_1661 inventory = InvUtils.mc.field_1724.method_31548();
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        boolean bestSuitable = false;
        for (int slot = 0; slot < 36; ++slot) {
            boolean better;
            class_1799 stack = inventory.method_5438(slot);
            if (stack.method_7960()) continue;
            float speed = stack.method_7924(state);
            boolean suitable = stack.method_7951(state);
            if (speed <= 1.0f && !suitable) continue;
            boolean bl = better = speed > bestSpeed || speed == bestSpeed && suitable && !bestSuitable;
            if (!better) continue;
            bestSpeed = speed;
            bestSuitable = suitable;
            bestSlot = slot;
        }
        if (bestSlot < 0) {
            return new FindItemResult(-1, 0);
        }
        return new FindItemResult(bestSlot, inventory.method_5438(bestSlot).method_7947());
    }

    public static boolean swap(int slot, boolean swapBack) {
        if (InvUtils.mc.field_1724 == null || slot < 0 || slot > 8) {
            return false;
        }
        InvUtils.mc.field_1724.method_31548().method_61496(slot);
        return true;
    }

    public static MoveItem move() {
        return new MoveItem();
    }

    private static FindItemResult scan(int from, int to, Predicate<class_1799> filter) {
        if (InvUtils.mc.field_1724 == null) {
            return new FindItemResult(-1, 0);
        }
        class_1661 inventory = InvUtils.mc.field_1724.method_31548();
        for (int slot = from; slot < to; ++slot) {
            class_1799 stack = inventory.method_5438(slot);
            if (stack.method_7960() || !filter.test(stack)) continue;
            return new FindItemResult(slot, stack.method_7947());
        }
        return new FindItemResult(-1, 0);
    }

    public static final class MoveItem {
        private int from = -1;

        private MoveItem() {
        }

        public MoveItem from(int slot) {
            this.from = slot;
            return this;
        }

        public MoveItem from(FindItemResult result) {
            this.from = result == null ? -1 : result.slot();
            return this;
        }

        public void toHotbar(int hotbarSlot) {
            if (InvUtils.mc.field_1724 == null || InvUtils.mc.field_1761 == null) {
                return;
            }
            if (this.from < 0 || this.from >= 36) {
                return;
            }
            if (hotbarSlot < 0 || hotbarSlot > 8 || this.from == hotbarSlot) {
                return;
            }
            InvUtils.mc.field_1761.method_2906(InvUtils.mc.field_1724.field_7512.field_7763, this.from, hotbarSlot, class_1713.field_7791, (class_1657)InvUtils.mc.field_1724);
        }
    }
}

