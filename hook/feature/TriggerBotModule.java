package hook.feature;

import hook.event.EventListener;
import hook.event.events.AttackEvent;
import hook.event.events.TickEvent;
import hook.manager.EventManager;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_239;
import net.minecraft.class_3966;
import net.minecraft.class_1268;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_1792;
import net.minecraft.class_3489;

public final class TriggerBotModule extends Module {
    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 1.0, 6.0, 4.25, 0.05).getValue(EncryptedString.of("Maximum distance to the target in blocks"));
    private final BooleanSetting playersOnly = new BooleanSetting(EncryptedString.of("Players Only"), true).setDescription(EncryptedString.of("Ignores mobs and animals"));
    private final BooleanSetting weaponsOnly = new BooleanSetting(EncryptedString.of("Weapons Only"), false).setDescription(EncryptedString.of("Only attacks while holding a sword, axe, mace or spear"));
    private final BooleanSetting swordOnly = new BooleanSetting(EncryptedString.of("Sword Only"), false).setDescription(EncryptedString.of("Only attacks while holding a sword"));
    private final BooleanSetting verticalAim = new BooleanSetting(EncryptedString.of("Vertical Aim"), false).setDescription(EncryptedString.of("Adjusts your pitch towards the target before hitting"));
    private final NumberSetting verticalAimSpeed = new NumberSetting(EncryptedString.of("Vertical Aim Speed"), 1.0, 90.0, 12.0, 1.0).getValue(EncryptedString.of("Degrees per tick the pitch may change"));
    private final BooleanSetting requireCooldown = new BooleanSetting(EncryptedString.of("Require Attack Cooldown"), true).setDescription(EncryptedString.of("Waits for a fully charged hit"));
    private final BooleanSetting attackWhileUsing = new BooleanSetting(EncryptedString.of("Attack While Using Items"), false).setDescription(EncryptedString.of("Also attacks while eating, blocking or drawing a bow"));

    public TriggerBotModule() {
        super(EncryptedString.of("Trigger Bot"), EncryptedString.of("Hits the target under your crosshair automatically"), -1, Category.COMBAT);
        this.addSettings(this.range, this.playersOnly, this.weaponsOnly, this.swordOnly, this.verticalAim, this.verticalAimSpeed, this.requireCooldown, this.attackWhileUsing);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1755 != null) return;
        if (!this.attackWhileUsing.getValue() && this.mc.field_1724.method_6115()) return;
        if (this.requireCooldown.getValue() && this.mc.field_1724.method_7261(0.5f) < 1.0f) return;
        class_1792 heldItem = this.mc.field_1724.method_6047().method_7909();
        if (this.swordOnly.getValue() && !heldItem.method_40131().method_40220(class_3489.field_42611)) return;
        if (!this.swordOnly.getValue() && this.weaponsOnly.getValue() && !heldItem.method_40131().method_40220(class_3489.field_48305)) return;
        if (!(this.mc.field_1765 instanceof class_3966 hit)) return;

        class_1297 target = hit.method_17782();
        if (!(target instanceof class_1309 living) || target == this.mc.field_1724 || !target.method_5805()) return;
        if (this.playersOnly.getValue() && !(target instanceof class_1657)) return;
        if (target instanceof class_1657 player && player.method_68878()) return;
        if (this.mc.field_1724.method_5739(target) > this.range.getValue()) return;
        if (!this.mc.field_1724.method_6057(target)) return;
        if (this.verticalAim.getValue()) this.aimVerticallyAt((class_1309)target);

        AttackEvent attackEvent = new AttackEvent();
        EventManager.b(attackEvent);
        if (attackEvent.isCancelled()) return;
        this.mc.field_1761.method_2918(this.mc.field_1724, target);
        this.mc.field_1724.method_6104(class_1268.field_5808);
    }

    private void aimVerticallyAt(class_1309 target) {
        class_243 eye = this.mc.field_1724.method_33571();
        class_243 targetEye = target.method_33571();
        double dx = targetEye.field_1352 - eye.field_1352;
        double dy = targetEye.field_1351 - eye.field_1351;
        double dz = targetEye.field_1350 - eye.field_1350;
        float desiredPitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
        float pitchDelta = class_3532.method_15393(desiredPitch - this.mc.field_1724.method_36455());
        float step = this.verticalAimSpeed.getFloatValue();
        float pitch = this.mc.field_1724.method_36455() + class_3532.method_15363(pitchDelta, -step, step);
        this.mc.field_1724.method_36457(class_3532.method_15363(pitch, -90.0f, 90.0f));
    }
}
