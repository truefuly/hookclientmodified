/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1531
 *  net.minecraft.class_1657
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1531;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public final class AimAssistModule
extends Module {
    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 1.0, 6.0, 4.2, 0.1);
    private final NumberSetting speed = new NumberSetting(EncryptedString.of("Speed"), 1.0, 90.0, 12.0, 1.0);
    private final NumberSetting fov = new NumberSetting(EncryptedString.of("FOV"), 10.0, 180.0, 70.0, 1.0);
    private final BooleanSetting onlyPlayers = new BooleanSetting(EncryptedString.of("Only Players"), true);
    private final BooleanSetting requireUse = new BooleanSetting(EncryptedString.of("Require Use"), false);
    private final BooleanSetting fallOnly = new BooleanSetting(EncryptedString.of("Fall Only"), false);
    private final BooleanSetting visibleOnly = new BooleanSetting(EncryptedString.of("Visible Only"), true);

    public AimAssistModule() {
        super(EncryptedString.of("Aim Assist"), EncryptedString.of("Smoothly aims at nearby targets."), -1, Category.COMBAT);
        this.addSettings(this.range, this.speed, this.fov, this.onlyPlayers, this.requireUse, this.fallOnly, this.visibleOnly);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1755 != null) {
            return;
        }
        if (this.requireUse.getValue() && !this.mc.field_1690.field_1904.method_1434()) {
            return;
        }
        if (this.fallOnly.getValue() && this.mc.field_1724.method_18798().field_1351 > -0.1) {
            return;
        }
        class_1309 target = this.findTarget();
        if (target != null) {
            this.rotateTo(target);
        }
    }

    private class_1309 findTarget() {
        class_1309 living;
        class_1297 aimed = this.mc.field_1692;
        if (aimed instanceof class_1309 && this.isValid(living = (class_1309)aimed)) {
            return living;
        }
        class_1309 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            double distance;
            class_1309 living2;
            if (!(entity instanceof class_1309) || !this.isValid(living2 = (class_1309)entity) || !((distance = (double)this.mc.field_1724.method_5739((class_1297)living2)) < bestDistance)) continue;
            bestDistance = distance;
            best = living2;
        }
        return best;
    }

    private boolean isValid(class_1309 entity) {
        if (entity == this.mc.field_1724 || !entity.method_5805() || entity instanceof class_1531) {
            return false;
        }
        if (this.onlyPlayers.getValue() && !(entity instanceof class_1657)) {
            return false;
        }
        if (entity instanceof class_1657 && ((class_1657)entity).method_68878()) {
            return false;
        }
        if ((double)this.mc.field_1724.method_5739((class_1297)entity) > this.range.getValue()) {
            return false;
        }
        return !this.visibleOnly.getValue() || this.mc.field_1724.method_6057((class_1297)entity);
    }

    private void rotateTo(class_1309 target) {
        class_243 eye = this.mc.field_1724.method_33571();
        class_243 point = target.method_33571();
        double dx = point.field_1352 - eye.field_1352;
        double dy = point.field_1351 - eye.field_1351;
        double dz = point.field_1350 - eye.field_1350;
        float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
        float yawDelta = class_3532.method_15393((float)(targetYaw - this.mc.field_1724.method_36454()));
        float pitchDelta = class_3532.method_15393((float)(targetPitch - this.mc.field_1724.method_36455()));
        double limit = this.fov.getValue();
        if ((double)Math.abs(yawDelta) > limit || (double)Math.abs(pitchDelta) > limit) {
            return;
        }
        float step = this.speed.getFloatValue();
        float yaw = this.mc.field_1724.method_36454() + class_3532.method_15363((float)yawDelta, (float)(-step), (float)step);
        float pitch = this.mc.field_1724.method_36455() + class_3532.method_15363((float)pitchDelta, (float)(-step), (float)step);
        this.mc.field_1724.method_36456(yaw);
        this.mc.field_1724.method_36457(class_3532.method_15363((float)pitch, (float)-90.0f, (float)90.0f));
    }
}

