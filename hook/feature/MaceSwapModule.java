package hook.feature;

import hook.event.EventListener;
import hook.event.events.AttackEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EnchantmentUtil;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1893;
import net.minecraft.class_2561;
import net.minecraft.class_746;
import net.minecraft.class_9362;

/**
 * Mace Swap with the Silent Client "Stun Slam": one left click while falling far enough for a smash swaps
 * to the preferred mace and hits. With Stun Slam on, a shielding target first gets the axe (shield disable),
 * then the mace follows up. Ground hits stay vanilla unless "Only Smash Hits" is off.
 */
public final class MaceSwapModule extends Module {
    public enum Preferred { Density, Breach, WindBurst }

    private final ModeSetting<Preferred> preferred = new ModeSetting<>(EncryptedString.of("Preferred Mace"), Preferred.Density, Preferred.class);
    private final BooleanSetting onlySmash = new BooleanSetting(EncryptedString.of("Only Smash Hits"), true).setDescription(EncryptedString.of("Only swap while falling fast enough for a mace smash"));
    private final BooleanSetting onlyPlayers = new BooleanSetting(EncryptedString.of("Only Players"), false);
    private final BooleanSetting stunSlam = new BooleanSetting(EncryptedString.of("Stun Slam"), true).setDescription(EncryptedString.of("Shielding target: axe hit to disable the shield, then the mace slam"));
    private final NumberSetting followUp = (NumberSetting) new NumberSetting(EncryptedString.of("Stun Slam Follow-up"), 0.0, 6.0, 0.0, 1.0).setDescription(EncryptedString.of("Ticks between the axe hit and the mace slam"));
    private final NumberSetting holdTime = (NumberSetting) new NumberSetting(EncryptedString.of("Mace Hold Time"), 1.0, 10.0, 3.0, 1.0).setDescription(EncryptedString.of("Ticks the mace stays selected after the hit"));
    private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true);
    private final BooleanSetting messages = new BooleanSetting(EncryptedString.of("Status Messages"), true).setDescription(EncryptedString.of("Shows what happened in the action bar"));

    private static boolean maceFollowUp;
    private class_1297 slamTarget;
    private int slamTicks = -1;
    private int slamOriginal = -1;
    private int returnSlot = -1;
    private int returnTicks;

    public MaceSwapModule() {
        super(EncryptedString.of("Mace Swap"), EncryptedString.of("Swaps to your mace for smash hits, with optional Stun Slam against shields"), -1, Category.COMBAT);
        this.addSettings(this.preferred, this.onlySmash, this.onlyPlayers, this.stunSlam, this.followUp, this.holdTime, this.switchBack, this.messages);
    }

    /** True while the mace hit of a stun slam is being sent (Shield Breaker must not swap to the axe then). */
    public static boolean isMaceFollowUp() {
        return maceFollowUp;
    }

    @Override
    public void onEnable() {
        this.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (this.slamTarget != null && this.mc.field_1724 != null && this.switchBack.getValue() && this.slamOriginal >= 0) {
            InventoryUtil.swap(this.slamOriginal);
        }
        this.reset();
        super.onDisable();
    }

    private void reset() {
        this.slamTarget = null;
        this.slamTicks = -1;
        this.slamOriginal = -1;
        this.returnSlot = -1;
        this.returnTicks = 0;
    }

    @EventListener
    public void onAttack(AttackEvent event) {
        class_746 player = this.mc.field_1724;
        if (player == null || this.mc.field_1761 == null || this.mc.field_1755 != null) {
            return;
        }
        if (this.slamTarget != null) {
            event.cancel();
            return;
        }
        class_1297 aimed = this.mc.field_1692;
        if (!(aimed instanceof class_1309 target) || aimed == player || !target.method_5805()) {
            return;
        }
        if (this.onlyPlayers.getValue() && !(target instanceof class_1657)) {
            return;
        }
        boolean smash = class_9362.method_58659(player);
        if (this.onlySmash.getValue() && !smash) {
            return;
        }
        int mace = this.findMace();
        if (mace < 0) {
            if (smash) this.message("No mace in hotbar", class_124.field_1061);
            return;
        }
        int selected = player.method_31548().method_67532();
        int original = this.returnSlot >= 0 ? this.returnSlot : selected;
        this.returnSlot = -1;

        int axe = this.stunSlam.getValue() && ShieldBreakerModule.isShielding(target) ? ShieldBreakerModule.findAxeSlot() : -1;
        if (axe >= 0) {
            event.cancel();
            InventoryUtil.swap(axe);
            this.attack(target);
            if (this.followUp.getIntValue() == 0) {
                InventoryUtil.swap(mace);
                this.maceAttack(target);
                this.message("Shield break + " + this.maceName(mace) + " slam", class_124.field_1075);
                this.queueReturn(original);
            } else {
                this.slamTarget = target;
                this.slamTicks = this.followUp.getIntValue();
                this.slamOriginal = original;
            }
            return;
        }
        if (mace != selected) {
            // vanilla's attack right after this syncs the new slot and hits with the mace
            InventoryUtil.swap(mace);
        }
        if (smash) this.message(this.maceName(mace) + " slam", class_124.field_1075);
        this.queueReturn(original);
    }

    @EventListener
    public void onTick(TickEvent event) {
        class_746 player = this.mc.field_1724;
        if (player == null) {
            this.reset();
            return;
        }
        if (this.returnSlot >= 0) {
            if (this.returnTicks > 0) {
                --this.returnTicks;
            } else {
                if (player.method_31548().method_67532() != this.returnSlot) InventoryUtil.swap(this.returnSlot);
                this.returnSlot = -1;
            }
        }
        if (this.slamTarget == null) {
            return;
        }
        if (this.slamTicks > 0) {
            --this.slamTicks;
            return;
        }
        class_1297 target = this.slamTarget;
        int original = this.slamOriginal;
        this.slamTarget = null;
        int mace = this.findMace();
        if (target.method_31481() || !target.method_5805() || player.method_5739(target) > 6.0f || mace < 0 || this.mc.field_1761 == null) {
            if (this.switchBack.getValue() && original >= 0) InventoryUtil.swap(original);
            return;
        }
        InventoryUtil.swap(mace);
        this.maceAttack(target);
        this.message("Shield break + " + this.maceName(mace) + " slam", class_124.field_1075);
        this.queueReturn(original);
    }

    private void attack(class_1297 target) {
        this.mc.field_1761.method_2918(this.mc.field_1724, target);
        this.mc.field_1724.method_6104(class_1268.field_5808);
    }

    private void maceAttack(class_1297 target) {
        maceFollowUp = true;
        try {
            this.attack(target);
        } finally {
            maceFollowUp = false;
        }
    }

    private void queueReturn(int slot) {
        if (this.switchBack.getValue() && slot >= 0 && slot < 9) {
            this.returnSlot = slot;
            this.returnTicks = this.holdTime.getIntValue();
        }
    }

    private int findMace() {
        int fallback = -1;
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(slot);
            if (stack.method_7960() || stack.method_7909() != class_1802.field_49814) continue;
            if (fallback < 0) fallback = slot;
            if (this.hasPreferred(stack)) return slot;
        }
        return fallback;
    }

    private boolean hasPreferred(class_1799 stack) {
        return switch ((Preferred) this.preferred.getValue()) {
            case Density -> EnchantmentUtil.hasEnchantment(stack, class_1893.field_50157);
            case Breach -> EnchantmentUtil.hasEnchantment(stack, class_1893.field_50158);
            case WindBurst -> EnchantmentUtil.hasEnchantment(stack, class_1893.field_50159);
        };
    }

    private String maceName(int slot) {
        class_1799 stack = this.mc.field_1724.method_31548().method_5438(slot);
        if (!this.hasPreferred(stack)) return "Mace";
        return this.preferred.getValue() == Preferred.WindBurst ? "Wind Burst" : this.preferred.getValue().name();
    }

    private void message(String text, class_124 color) {
        if (this.messages.getValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_7353(class_2561.method_43470(text).method_27692(color), true);
        }
    }
}
