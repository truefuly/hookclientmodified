/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2346
 *  net.minecraft.class_2350
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_3611
 *  net.minecraft.class_3612
 *  net.minecraft.class_638
 */
package com.hookaddon.util;

import net.minecraft.class_2338;
import net.minecraft.class_2346;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3611;
import net.minecraft.class_3612;
import net.minecraft.class_638;

public final class SafetyChecks {
    private static final class_310 mc = class_310.method_1551();

    private SafetyChecks() {
    }

    public static boolean nearLava(class_2338 pos) {
        class_638 world = SafetyChecks.mc.field_1687;
        if (world == null) {
            return false;
        }
        for (class_2350 direction : class_2350.values()) {
            class_2680 state = world.method_8320(pos.method_10093(direction));
            if (!state.method_26227().method_39360((class_3611)class_3612.field_15908) && !state.method_26227().method_39360((class_3611)class_3612.field_15907)) continue;
            return true;
        }
        return false;
    }

    public static boolean nearFluid(class_2338 pos) {
        class_638 world = SafetyChecks.mc.field_1687;
        if (world == null) {
            return false;
        }
        for (class_2350 direction : class_2350.values()) {
            class_2680 state = world.method_8320(pos.method_10093(direction));
            if (state.method_26227().method_15769()) continue;
            return true;
        }
        return false;
    }

    public static boolean wouldReleaseFallingBlock(class_2338 pos) {
        class_638 world = SafetyChecks.mc.field_1687;
        if (world == null) {
            return false;
        }
        return world.method_8320(pos.method_10084()).method_26204() instanceof class_2346;
    }

    public static boolean playerInDanger() {
        if (SafetyChecks.mc.field_1724 == null) {
            return false;
        }
        if (SafetyChecks.mc.field_1724.method_5771()) {
            return true;
        }
        return SafetyChecks.nearLava(SafetyChecks.mc.field_1724.method_24515());
    }

    public static boolean unsafeToMine(class_2338 pos, boolean avoidLava, boolean avoidFluids, boolean avoidFalling) {
        if (avoidLava && SafetyChecks.nearLava(pos)) {
            return true;
        }
        if (avoidFluids && SafetyChecks.nearFluid(pos)) {
            return true;
        }
        return avoidFalling && SafetyChecks.wouldReleaseFallingBlock(pos);
    }

    public static String describe(class_2338 pos) {
        if (SafetyChecks.nearLava(pos)) {
            return "lava";
        }
        if (SafetyChecks.wouldReleaseFallingBlock(pos)) {
            return "falling block above";
        }
        if (SafetyChecks.nearFluid(pos)) {
            return "fluid";
        }
        return null;
    }
}

