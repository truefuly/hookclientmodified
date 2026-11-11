/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_2338
 *  net.minecraft.class_243
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 */
package com.hookaddon.task;

import com.hookaddon.task.Capability;
import com.hookaddon.task.TaskManager;
import com.hookaddon.util.InvGuards;
import com.hookaddon.util.SafetyChecks;
import hook.compat.meteorclient.utils.player.Rotations;
import hook.compat.meteorclient.utils.world.BlockUtils;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;

public final class MiningController {
    private static final class_310 mc = class_310.method_1551();
    private String owner;
    private class_2338 target;
    private Options options = Options.DEFAULT;
    private int ticksOnTarget;
    private int attempts;
    private static final MiningController INSTANCE = new MiningController();

    private MiningController() {
    }

    public static MiningController get() {
        return INSTANCE;
    }

    public synchronized String owner() {
        return this.owner;
    }

    public synchronized class_2338 target() {
        return this.target;
    }

    public synchronized boolean isWorking() {
        return this.owner != null;
    }

    public synchronized Result tick(String requestingOwner, class_2338 requestedTarget, Options requestedOptions) {
        class_2680 state;
        if (MiningController.mc.field_1724 == null || MiningController.mc.field_1687 == null || MiningController.mc.field_1761 == null) {
            this.reset(requestingOwner);
            return Result.FAILED;
        }
        if (!TaskManager.get().owns(requestingOwner, Capability.MINE)) {
            if (this.owner != null) {
                this.reset(requestingOwner);
            }
            TaskManager.debug(requestingOwner + ": mining refused, mining capability not held");
            return Result.REJECTED;
        }
        if (this.owner == null || !this.owner.equals(requestingOwner)) {
            this.owner = requestingOwner;
            this.target = null;
            this.ticksOnTarget = 0;
            this.attempts = 0;
        }
        if (requestedOptions != null) {
            this.options = requestedOptions;
        }
        if (this.target == null || !this.target.equals((Object)requestedTarget)) {
            this.target = requestedTarget;
            this.ticksOnTarget = 0;
        }
        if ((state = MiningController.mc.field_1687.method_8320(this.target)).method_26215()) {
            this.owner = null;
            this.target = null;
            this.ticksOnTarget = 0;
            return Result.DONE;
        }
        if (!BlockUtils.canBreak(this.target, state)) {
            this.forgetTarget();
            return Result.FAILED;
        }
        if (SafetyChecks.unsafeToMine(this.target, this.options.avoidLava(), this.options.avoidFluids(), this.options.avoidFalling())) {
            this.forgetTarget();
            return Result.UNSAFE;
        }
        if (!MiningController.inReach(this.target)) {
            return Result.OUT_OF_REACH;
        }
        if (this.options.autoTool()) {
            InvGuards.selectBestTool(state, this.options.minToolDurability());
        }
        if (this.options.rotate()) {
            Rotations.rotate(Rotations.getYaw(this.target.method_46558()), Rotations.getPitch(this.target.method_46558()));
        }
        if (BlockUtils.breakBlock(this.target, this.options.swing())) {
            ++this.ticksOnTarget;
        }
        if (this.ticksOnTarget > this.options.timeoutTicks()) {
            ++this.attempts;
            TaskManager.debug(this.owner + ": mining timeout on " + this.target.method_23854());
            if (this.attempts >= Math.max(1, this.options.maxAttempts())) {
                this.forgetTarget();
                return Result.FAILED;
            }
            this.ticksOnTarget = 0;
        }
        return Result.WORKING;
    }

    public synchronized void reset(String requestingOwner) {
        if (this.owner != null && !this.owner.equals(requestingOwner)) {
            return;
        }
        this.owner = null;
        this.target = null;
        this.ticksOnTarget = 0;
        this.attempts = 0;
    }

    public synchronized void forgetTarget() {
        this.target = null;
        this.ticksOnTarget = 0;
        this.attempts = 0;
        this.owner = null;
    }

    public static boolean inReach(class_2338 pos) {
        if (MiningController.mc.field_1724 == null || MiningController.mc.field_1687 == null) {
            return false;
        }
        return MiningController.inReach(pos, Options.DEFAULT.reach());
    }

    public static boolean inReach(class_2338 pos, double reach) {
        if (MiningController.mc.field_1724 == null || MiningController.mc.field_1687 == null) {
            return false;
        }
        class_243 center = pos.method_46558();
        if (MiningController.mc.field_1724.method_33571().method_1025(center) > reach * reach) {
            return false;
        }
        class_3959 context = new class_3959(MiningController.mc.field_1724.method_33571(), center, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)MiningController.mc.field_1724);
        class_3965 result = MiningController.mc.field_1687.method_17742(context);
        return result != null && result.method_17777().equals((Object)pos);
    }

    public static String describe(Result result) {
        return result.name().toLowerCase().replace('_', ' ');
    }

    public record Options(double reach, boolean rotate, boolean swing, boolean autoTool, int minToolDurability, boolean avoidLava, boolean avoidFluids, boolean avoidFalling, int timeoutTicks, int maxAttempts) {
        public static final Options DEFAULT = new Options(4.5, true, true, true, 10, true, false, true, 200, 3);
    }

    public static enum Result {
        REJECTED,
        WORKING,
        DONE,
        OUT_OF_REACH,
        UNSAFE,
        FAILED;

    }
}

