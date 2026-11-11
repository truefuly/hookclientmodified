/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_3965
 */
package hook.compat.meteorclient.utils.world;

import hook.compat.meteorclient.utils.player.FindItemResult;
import hook.compat.meteorclient.utils.player.Rotations;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3965;

public final class BlockUtils {
    private static final class_310 mc = class_310.method_1551();
    private static class_2338 breakingPos;

    private BlockUtils() {
    }

    public static boolean canBreak(class_2338 pos, class_2680 state) {
        if (BlockUtils.mc.field_1687 == null || pos == null || state == null || state.method_26215()) {
            return false;
        }
        return state.method_26214((class_1922)BlockUtils.mc.field_1687, pos) >= 0.0f;
    }

    public static boolean breakBlock(class_2338 pos, boolean swing) {
        if (BlockUtils.mc.field_1724 == null || BlockUtils.mc.field_1761 == null || BlockUtils.mc.field_1687 == null || pos == null) {
            return false;
        }
        class_2680 state = BlockUtils.mc.field_1687.method_8320(pos);
        if (!BlockUtils.canBreak(pos, state)) {
            return false;
        }
        class_2350 side = BlockUtils.facing(pos);
        boolean continuing = BlockUtils.mc.field_1761.method_2923() && pos.equals((Object)breakingPos);
        boolean working = continuing ? BlockUtils.mc.field_1761.method_2902(pos, side) : BlockUtils.mc.field_1761.method_2910(pos, side);
        breakingPos = pos;
        if (swing) {
            BlockUtils.mc.field_1724.method_6104(class_1268.field_5808);
        }
        return working || continuing;
    }

    public static class_2350 getPlaceSide(class_2338 pos) {
        if (BlockUtils.mc.field_1687 == null || pos == null) {
            return null;
        }
        for (class_2350 direction : class_2350.values()) {
            if (BlockUtils.mc.field_1687.method_8320(pos.method_10093(direction)).method_45474()) continue;
            return direction;
        }
        return null;
    }

    public static boolean canPlace(class_2338 pos) {
        if (BlockUtils.mc.field_1687 == null || pos == null) {
            return false;
        }
        return BlockUtils.mc.field_1687.method_8320(pos).method_45474();
    }

    public static boolean place(class_2338 pos, FindItemResult item, boolean rotate, int priority, boolean swing, boolean checkSpace, boolean altInteract) {
        class_3965 hitResult;
        class_1269 result;
        class_2350 side;
        if (BlockUtils.mc.field_1724 == null || BlockUtils.mc.field_1761 == null || BlockUtils.mc.field_1687 == null || pos == null) {
            return false;
        }
        if (item == null || !item.found() || !item.isHotbar()) {
            return false;
        }
        if (BlockUtils.mc.field_1724.method_31548().method_67532() != item.slot()) {
            BlockUtils.mc.field_1724.method_31548().method_61496(item.slot());
        }
        if ((side = BlockUtils.getPlaceSide(pos)) == null) {
            return false;
        }
        class_2338 support = pos.method_10093(side);
        class_2350 face = side.method_10153();
        class_243 hit = new class_243((double)support.method_10263() + 0.5 + (double)face.method_10148() * 0.5, (double)support.method_10264() + 0.5 + (double)face.method_10164() * 0.5, (double)support.method_10260() + 0.5 + (double)face.method_10165() * 0.5);
        if (rotate) {
            Rotations.rotate(Rotations.getYaw(hit), Rotations.getPitch(hit));
        }
        if ((result = BlockUtils.mc.field_1761.method_2896(BlockUtils.mc.field_1724, class_1268.field_5808, hitResult = new class_3965(hit, face, support, false))).method_23665() && swing) {
            BlockUtils.mc.field_1724.method_6104(class_1268.field_5808);
        }
        return result.method_23665();
    }

    private static class_2350 facing(class_2338 pos) {
        class_243 delta = class_243.method_24953((class_2382)pos).method_1020(BlockUtils.mc.field_1724.method_33571());
        double ax = Math.abs(delta.field_1352);
        double ay = Math.abs(delta.field_1351);
        double az = Math.abs(delta.field_1350);
        if (ax >= ay && ax >= az) {
            return delta.field_1352 > 0.0 ? class_2350.field_11034 : class_2350.field_11039;
        }
        if (ay >= az) {
            return delta.field_1351 > 0.0 ? class_2350.field_11036 : class_2350.field_11033;
        }
        return delta.field_1350 > 0.0 ? class_2350.field_11035 : class_2350.field_11043;
    }
}

