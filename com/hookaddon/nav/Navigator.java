/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_638
 */
package com.hookaddon.nav;

import com.hookaddon.task.Capability;
import com.hookaddon.task.TaskManager;
import hook.compat.meteorclient.pathing.IPathManager;
import hook.compat.meteorclient.pathing.PathManagers;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_638;

public final class Navigator {
    private static final class_310 mc = class_310.method_1551();
    private static final Navigator INSTANCE = new Navigator();
    private String owner;
    private class_2338 target;
    private Options options = Options.DEFAULT;
    private boolean usingPathManager;
    private boolean targetDirty;
    private class_243 lastProgressPos;
    private int ticksSinceProgress;
    private int recoveries;
    private int recoveryTicks;
    private int recoveryDirection = 1;

    private Navigator() {
    }

    public static Navigator get() {
        return INSTANCE;
    }

    public synchronized String owner() {
        return this.owner;
    }

    public synchronized class_2338 target() {
        return this.target;
    }

    public synchronized boolean isNavigating() {
        return this.owner != null;
    }

    public synchronized State tick(String requestingOwner, class_2338 requestedTarget, Options requestedOptions) {
        class_638 world = Navigator.mc.field_1687;
        if (Navigator.mc.field_1724 == null || world == null) {
            this.stop(requestingOwner);
            return State.FAILED;
        }
        if (!TaskManager.get().owns(requestingOwner, Capability.MOVE)) {
            if (this.owner != null) {
                this.stopInternal();
            }
            TaskManager.debug(requestingOwner + ": navigation refused, movement capability not held");
            return State.FAILED;
        }
        if (this.owner == null || !this.owner.equals(requestingOwner)) {
            this.stopInternal();
            this.owner = requestingOwner;
            this.lastProgressPos = null;
            this.ticksSinceProgress = 0;
            this.recoveries = 0;
            this.recoveryTicks = 0;
        }
        if (this.target == null || !this.target.equals((Object)requestedTarget)) {
            this.target = requestedTarget;
            this.targetDirty = true;
            this.ticksSinceProgress = 0;
            this.recoveries = 0;
            this.recoveryTicks = 0;
        }
        if (requestedOptions != null) {
            this.options = requestedOptions;
        }
        if (Navigator.mc.field_1724.method_29504() || Navigator.mc.field_1724.method_6032() <= 0.0f) {
            this.stopInternal();
            return State.FAILED;
        }
        if (this.isArrived()) {
            this.releaseKeys();
            this.stopPathManager();
            this.lastProgressPos = null;
            return State.ARRIVED;
        }
        boolean progressed = this.trackProgress();
        if (Navigator.pathManagerAvailable()) {
            if (!this.usingPathManager) {
                this.releaseKeys();
                this.usingPathManager = true;
                TaskManager.debug(this.owner + ": using path manager '" + PathManagers.get().getName() + "'");
            }
            if (this.targetDirty || !PathManagers.get().isPathing()) {
                PathManagers.get().moveTo(this.target);
                this.targetDirty = false;
            }
        } else {
            if (this.usingPathManager) {
                this.stopPathManager();
            }
            this.walkTowardTarget();
        }
        if (!progressed) {
            ++this.ticksSinceProgress;
            if (this.ticksSinceProgress >= this.options.stuckTicks() && !this.startRecovery()) {
                TaskManager.debug(this.owner + ": navigation failed after " + this.recoveries + " recovery attempts");
                this.stopInternal();
                return State.FAILED;
            }
        }
        if (this.recoveryTicks > 0) {
            --this.recoveryTicks;
        }
        return State.MOVING;
    }

    public synchronized void stop(String releasingOwner) {
        if (this.owner == null || !this.owner.equals(releasingOwner)) {
            return;
        }
        this.stopInternal();
    }

    public synchronized void stopAny() {
        if (this.owner != null) {
            this.stopInternal();
        }
    }

    private void stopInternal() {
        this.releaseKeys();
        this.stopPathManager();
        this.owner = null;
        this.target = null;
        this.targetDirty = false;
        this.lastProgressPos = null;
        this.ticksSinceProgress = 0;
        this.recoveries = 0;
        this.recoveryTicks = 0;
    }

    private boolean isArrived() {
        double distance;
        double dz;
        double dy;
        if (this.target == null || Navigator.mc.field_1724 == null) {
            return true;
        }
        double dx = Navigator.mc.field_1724.method_23317() - ((double)this.target.method_10263() + 0.5);
        return dx * dx + (dy = Navigator.mc.field_1724.method_23318() - ((double)this.target.method_10264() + 0.5)) * dy + (dz = Navigator.mc.field_1724.method_23321() - ((double)this.target.method_10260() + 0.5)) * dz <= (distance = this.options.arriveDistance()) * distance;
    }

    private boolean trackProgress() {
        boolean progressed;
        class_243 position = new class_243(Navigator.mc.field_1724.method_23317(), Navigator.mc.field_1724.method_23318(), Navigator.mc.field_1724.method_23321());
        boolean bl = progressed = this.lastProgressPos == null || position.method_1025(this.lastProgressPos) > 0.0025;
        if (progressed) {
            this.lastProgressPos = position;
            this.ticksSinceProgress = 0;
        }
        return progressed;
    }

    private boolean startRecovery() {
        if (this.recoveries >= this.options.maxRecoveries()) {
            return false;
        }
        ++this.recoveries;
        this.recoveryTicks = 20;
        this.recoveryDirection = -this.recoveryDirection;
        this.ticksSinceProgress = 0;
        TaskManager.debug(this.owner + ": stuck, recovery attempt " + this.recoveries + "/" + this.options.maxRecoveries());
        return true;
    }

    private static boolean pathManagerAvailable() {
        IPathManager manager = PathManagers.get();
        return manager != null && !"none".equals(manager.getName());
    }

    private void stopPathManager() {
        if (!this.usingPathManager) {
            return;
        }
        this.usingPathManager = false;
        if (Navigator.mc.field_1687 != null) {
            PathManagers.get().stop();
        }
    }

    private void walkTowardTarget() {
        boolean horizontalDistanceLeft;
        if (this.target == null || Navigator.mc.field_1724 == null) {
            return;
        }
        double dx = (double)this.target.method_10263() + 0.5 - Navigator.mc.field_1724.method_23317();
        double dz = (double)this.target.method_10260() + 0.5 - Navigator.mc.field_1724.method_23321();
        double dy = (double)this.target.method_10264() - Navigator.mc.field_1724.method_23318();
        float desiredYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
        if (this.recoveryTicks > 0) {
            desiredYaw += (float)(90 * this.recoveryDirection);
        }
        float delta = Navigator.wrapDegrees(desiredYaw - Navigator.mc.field_1724.method_36454());
        float turn = Math.max(-30.0f, Math.min(30.0f, delta));
        Navigator.mc.field_1724.method_36456(Navigator.mc.field_1724.method_36454() + turn);
        boolean bl = horizontalDistanceLeft = Math.hypot(dx, dz) > 1.0;
        if (horizontalDistanceLeft) {
            Navigator.mc.field_1690.field_1894.method_23481(true);
        } else {
            Navigator.mc.field_1690.field_1894.method_23481(false);
        }
        boolean water = Navigator.mc.field_1724.method_5799();
        boolean blockedAhead = this.isBlockedAhead();
        boolean dropAhead = this.isDropAhead();
        boolean jump = false;
        if (this.options.allowJump()) {
            if (water && dy > 0.5) {
                jump = true;
            } else if (blockedAhead && this.canJumpOver()) {
                jump = true;
            } else if (this.recoveryTicks > 0 && Navigator.mc.field_1724.method_24828()) {
                jump = true;
            }
        }
        Navigator.mc.field_1690.field_1903.method_23481(jump);
        boolean sneak = this.options.edgeGuard() && dropAhead && dy > -1.5 && !water && Navigator.mc.field_1724.method_24828() && horizontalDistanceLeft;
        Navigator.mc.field_1690.field_1832.method_23481(sneak);
    }

    private boolean isBlockedAhead() {
        class_638 world = Navigator.mc.field_1687;
        if (world == null) {
            return false;
        }
        return !world.method_8320(this.aheadBlock()).method_45474();
    }

    private boolean canJumpOver() {
        class_638 world = Navigator.mc.field_1687;
        if (world == null) {
            return false;
        }
        class_2338 front = this.aheadBlock();
        return world.method_8320(front.method_10084()).method_45474();
    }

    private boolean isDropAhead() {
        class_638 world = Navigator.mc.field_1687;
        if (world == null) {
            return false;
        }
        class_2338 front = this.aheadBlock();
        class_2338 below = front.method_10074();
        return world.method_8320(below).method_26215() && world.method_8320(below.method_10074()).method_26215();
    }

    private class_2338 aheadBlock() {
        float radians = (float)Math.toRadians(Navigator.mc.field_1724.method_36454());
        int ox = (int)Math.round(-Math.sin(radians));
        int oz = (int)Math.round(Math.cos(radians));
        return Navigator.mc.field_1724.method_24515().method_10069(ox, 0, oz);
    }

    private void releaseKeys() {
        if (Navigator.mc.field_1690 == null) {
            return;
        }
        Navigator.mc.field_1690.field_1894.method_23481(false);
        Navigator.mc.field_1690.field_1903.method_23481(false);
        Navigator.mc.field_1690.field_1832.method_23481(false);
        Navigator.mc.field_1690.field_1881.method_23481(false);
        Navigator.mc.field_1690.field_1913.method_23481(false);
        Navigator.mc.field_1690.field_1849.method_23481(false);
    }

    private static float wrapDegrees(float degrees) {
        float value = degrees % 360.0f;
        if (value >= 180.0f) {
            value -= 360.0f;
        }
        if (value < -180.0f) {
            value += 360.0f;
        }
        return value;
    }

    public record Options(double arriveDistance, boolean allowJump, boolean edgeGuard, int stuckTicks, int maxRecoveries) {
        public static final Options DEFAULT = new Options(3.0, true, true, 60, 3);
    }

    public static enum State {
        IDLE,
        MOVING,
        ARRIVED,
        FAILED;

    }
}

