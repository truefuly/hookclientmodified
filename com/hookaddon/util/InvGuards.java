/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1661
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_2248
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 */
package com.hookaddon.util;

import hook.compat.meteorclient.utils.player.FindItemResult;
import hook.compat.meteorclient.utils.player.InvUtils;
import java.util.function.Predicate;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2248;
import net.minecraft.class_2680;
import net.minecraft.class_310;

public final class InvGuards {
    private static final class_310 mc = class_310.method_1551();

    private InvGuards() {
    }

    public static int emptySlots() {
        if (InvGuards.mc.field_1724 == null) {
            return 0;
        }
        class_1661 inventory = InvGuards.mc.field_1724.method_31548();
        int empty = 0;
        for (int i = 0; i < 36; ++i) {
            if (!inventory.method_5438(i).method_7960()) continue;
            ++empty;
        }
        return empty;
    }

    public static boolean inventoryFull() {
        return InvGuards.emptySlots() == 0;
    }

    public static int count(Predicate<class_1799> filter) {
        if (InvGuards.mc.field_1724 == null) {
            return 0;
        }
        class_1661 inventory = InvGuards.mc.field_1724.method_31548();
        int total = 0;
        for (int i = 0; i < 36; ++i) {
            class_1799 stack = inventory.method_5438(i);
            if (stack.method_7960() || !filter.test(stack)) continue;
            total += stack.method_7947();
        }
        return total;
    }

    public static int durabilityLeft(class_1799 stack) {
        if (stack == null || stack.method_7960() || !stack.method_7963()) {
            return Integer.MAX_VALUE;
        }
        return stack.method_7936() - stack.method_7919();
    }

    public static boolean durableEnough(class_1799 stack, int minDurability) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        return InvGuards.durabilityLeft(stack) > Math.max(0, minDurability);
    }

    public static FindItemResult bestTool(class_2680 state, int minDurability) {
        FindItemResult result = InvUtils.findFastestTool(state);
        if (!result.found() || !result.isHotbar()) {
            return result;
        }
        if (!InvGuards.durableEnough(InvGuards.mc.field_1724.method_31548().method_5438(result.slot()), minDurability)) {
            return new FindItemResult(-1, 0);
        }
        return result;
    }

    public static boolean selectBestTool(class_2680 state, int minDurability) {
        if (InvGuards.mc.field_1724 == null || InvGuards.mc.field_1761 == null) {
            return false;
        }
        FindItemResult best = InvGuards.bestTool(state, minDurability);
        if (!best.found() || !best.isHotbar()) {
            return InvGuards.durableEnough(InvGuards.mc.field_1724.method_6047(), minDurability);
        }
        class_1799 current = InvGuards.mc.field_1724.method_31548().method_5438(InvGuards.mc.field_1724.method_31548().method_67532());
        if (InvUtils.findFastestTool(state).found() && !current.method_7960() && current.method_7909() == InvGuards.mc.field_1724.method_31548().method_5438(best.slot()).method_7909() && best.slot() == InvGuards.mc.field_1724.method_31548().method_67532()) {
            return true;
        }
        return InvUtils.swap(best.slot(), false);
    }

    public static boolean hasBlockItem(class_2248 block) {
        return InvGuards.findBlockItem(block).found();
    }

    public static FindItemResult findBlockItem(class_2248 block) {
        class_1792 item = class_1792.method_7867((class_2248)block);
        if (item == null) {
            return new FindItemResult(-1, 0);
        }
        return InvUtils.find(stack -> stack.method_31574(item));
    }

    public static int countForBlock(class_2248 block) {
        class_1792 item = class_1792.method_7867((class_2248)block);
        if (item == null) {
            return 0;
        }
        return InvGuards.count(stack -> stack.method_31574(item));
    }
}

