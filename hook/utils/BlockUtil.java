/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1923
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2769
 *  net.minecraft.class_2818
 *  net.minecraft.class_3965
 *  net.minecraft.class_4969
 */
package hook.utils;

import hook.Hook;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2769;
import net.minecraft.class_2818;
import net.minecraft.class_3965;
import net.minecraft.class_4969;

public final class BlockUtil {
    public static Stream<class_2818> getLoadedChunks() {
        return BlockUtil.getLoadedChunksList().stream();
    }

    public static List<class_2818> getLoadedChunksList() {
        int radius = Math.max(2, Hook.mc.field_1690.method_38521()) + 3;
        class_1923 center = Hook.mc.field_1724.method_31476();
        ArrayList<class_2818> loaded = new ArrayList<class_2818>((radius * 2 + 1) * (radius * 2 + 1));
        for (int x = center.field_9181 - radius; x <= center.field_9181 + radius; ++x) {
            for (int z = center.field_9180 - radius; z <= center.field_9180 + radius; ++z) {
                class_2818 chunk;
                if (!Hook.mc.field_1687.method_8393(x, z) || (chunk = Hook.mc.field_1687.method_8497(x, z)) == null) continue;
                loaded.add(chunk);
            }
        }
        return loaded;
    }

    public static boolean isBlockAtPosition(class_2338 blockPos, class_2248 block) {
        return Hook.mc.field_1687.method_8320(blockPos).method_26204() == block;
    }

    public static boolean isRespawnAnchorCharged(class_2338 blockPos) {
        return BlockUtil.isBlockAtPosition(blockPos, class_2246.field_23152) && (Integer)Hook.mc.field_1687.method_8320(blockPos).method_11654((class_2769)class_4969.field_23153) != 0;
    }

    public static boolean isRespawnAnchorUncharged(class_2338 blockPos) {
        return BlockUtil.isBlockAtPosition(blockPos, class_2246.field_23152) && (Integer)Hook.mc.field_1687.method_8320(blockPos).method_11654((class_2769)class_4969.field_23153) == 0;
    }

    public static void interactWithBlock(class_3965 blockHitResult, boolean shouldSwingHand) {
        class_1269 result = Hook.mc.field_1761.method_2896(Hook.mc.field_1724, class_1268.field_5808, blockHitResult);
        if (result.method_23665() && shouldSwingHand) {
            Hook.mc.field_1724.method_6104(class_1268.field_5808);
        }
    }
}

