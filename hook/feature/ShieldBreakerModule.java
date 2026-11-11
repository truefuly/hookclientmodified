package hook.feature;

import hook.Hook;
import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1802;
import net.minecraft.class_310;

/**
 * Silent Client's Shield Breaker: hitting a blocking target swaps to the hotbar axe right before the attack
 * (so the attack itself syncs the slot), then switches back after a short delay.
 */
public final class ShieldBreakerModule extends Module {
    private static ShieldBreakerModule instance;
    private final NumberSetting returnDelay = (NumberSetting) new NumberSetting(EncryptedString.of("Switch Back Delay"), 0.0, 10.0, 4.0, 1.0).setDescription(EncryptedString.of("Ticks before your old item comes back"));
    private final NumberSetting chance = (NumberSetting) new NumberSetting(EncryptedString.of("Consistency"), 50.0, 100.0, 100.0, 1.0).setDescription(EncryptedString.of("How often (%) a shield hit triggers the swap"));
    private final BooleanSetting playersOnly = new BooleanSetting(EncryptedString.of("Only Players"), true);

    private int originalSlot = -1;
    private int axeSlot = -1;
    private int ticksRemaining = -1;

    public ShieldBreakerModule() {
        super(EncryptedString.of("Shield Breaker"), EncryptedString.of("Swaps to your axe when you hit a shielding target, then back"), -1, Category.COMBAT);
        this.addSettings(this.returnDelay, this.chance, this.playersOnly);
        instance = this;
    }

    /** Called at the start of ClientPlayerInteractionManager.attackEntity. */
    public static void beforeAttack(class_1297 target) {
        ShieldBreakerModule m = instance;
        if (m == null || !m.isEnabled() || MaceSwapModule.isMaceFollowUp()) return;
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || !(target instanceof class_1309 living) || !isShielding(living)) return;
        if (m.playersOnly.getValue() && !(target instanceof class_1657)) return;
        if (m.chance.getIntValue() < 100 && ThreadLocalRandom.current().nextInt(100) >= m.chance.getIntValue()) return;
        int axe = findAxeSlot();
        int current = mc.field_1724.method_31548().method_67532();
        if (axe < 0 || axe == current) return;
        if (m.ticksRemaining < 0) m.originalSlot = current;
        m.axeSlot = axe;
        InventoryUtil.swap(axe);
        m.ticksRemaining = m.returnDelay.getIntValue();
    }

    /** Called at the end of ClientPlayerInteractionManager.attackEntity. */
    public static void afterAttack() {
        ShieldBreakerModule m = instance;
        if (m != null && m.ticksRemaining == 0) m.restore();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.ticksRemaining > 0 && --this.ticksRemaining == 0) this.restore();
    }

    @Override
    public void onDisable() {
        this.restore();
        super.onDisable();
    }

    private void restore() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 != null && this.originalSlot >= 0 && mc.field_1724.method_31548().method_67532() == this.axeSlot) {
            InventoryUtil.swap(this.originalSlot);
        }
        this.originalSlot = this.axeSlot = this.ticksRemaining = -1;
    }

    public static boolean isShielding(class_1309 target) {
        return target.method_6039() || target.method_6115() && target.method_6030().method_31574(class_1802.field_8255);
    }

    public static int findAxeSlot() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) return -1;
        for (int slot = 0; slot < 9; ++slot) {
            if (mc.field_1724.method_31548().method_5438(slot).method_7909() instanceof class_1743) return slot;
        }
        return -1;
    }
}
