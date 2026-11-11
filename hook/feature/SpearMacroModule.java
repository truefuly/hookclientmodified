package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.mixin.MinecraftClientAccessor;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import java.util.Locale;
import net.minecraft.class_1799;
import net.minecraft.class_304;
import net.minecraft.class_7923;

/**
 * Silent Client's Spear Strike: pressing the hotbar key of a spear jabs with it instantly and returns to the
 * item you were holding. Runs at the start of the client tick, before vanilla consumes the hotbar key press.
 */
public final class SpearMacroModule extends Module {
    private final NumberSetting returnDelay = (NumberSetting) new NumberSetting(EncryptedString.of("Return Delay"), 0.0, 10.0, 1.0, 1.0).setDescription(EncryptedString.of("Ticks before your previous item comes back (0 = same tick)"));
    private final BooleanSetting moddedSpears = new BooleanSetting(EncryptedString.of("Modded Spears"), true).setDescription(EncryptedString.of("Also react to spears added by other mods"));

    private int returnSlot = -1;
    private int ticksUntilReturn;

    public SpearMacroModule() {
        super(EncryptedString.of("Spear Macro"), EncryptedString.of("Press a spear's hotbar key to jab instantly, then swap back"), -1, Category.COMBAT);
        this.addSettings(this.returnDelay, this.moddedSpears);
    }

    @Override
    public void onDisable() {
        this.restore();
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.returnSlot >= 0) {
            if (this.ticksUntilReturn > 0) --this.ticksUntilReturn;
            if (this.ticksUntilReturn <= 0) this.restore();
        }
        if (this.returnSlot >= 0 || this.mc.field_1724 == null || this.mc.field_1761 == null || this.mc.field_1755 != null
                || this.mc.field_1724.method_7325() || !this.mc.field_1724.method_5805()) {
            return;
        }
        int selected = this.mc.field_1724.method_31548().method_67532();
        class_304[] keys = this.mc.field_1690.field_1852;
        for (int slot = 0; slot < 9; ++slot) {
            if (slot == selected || !this.isSpear(this.mc.field_1724.method_31548().method_5438(slot))) continue;
            if (!keys[slot].method_1436()) continue;
            while (keys[slot].method_1436()) {
                // collapse repeated presses into one jab
            }
            this.returnSlot = selected;
            InventoryUtil.swap(slot);
            ((MinecraftClientAccessor) this.mc).invokeDoAttack();
            this.ticksUntilReturn = this.returnDelay.getIntValue();
            if (this.ticksUntilReturn == 0) this.restore();
            return;
        }
    }

    private void restore() {
        if (this.mc.field_1724 != null && this.returnSlot >= 0 && this.returnSlot < 9) {
            InventoryUtil.swap(this.returnSlot);
        }
        this.returnSlot = -1;
        this.ticksUntilReturn = 0;
    }

    private boolean isSpear(class_1799 stack) {
        if (stack.method_7960()) return false;
        net.minecraft.class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
        String path = id.method_12832().toLowerCase(Locale.ROOT);
        boolean spear = path.equals("spear") || path.endsWith("_spear");
        return spear && (this.moddedSpears.getValue() || "minecraft".equals(id.method_12836()));
    }
}
