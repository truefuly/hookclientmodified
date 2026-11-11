package hook.feature;

import hook.event.EventListener;
import hook.event.events.PostItemUseEvent;
import hook.event.events.TickEvent;
import hook.mixin.ItemUseCooldownAccessor;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1811;
import net.minecraft.class_9334;

/**
 * Fast Use: removes the 4 tick delay between right-click uses while you hold the use key
 * (XP bottles, snowballs, eggs, pearls, blocks...). Works like Meteor's Fast Use: the client's item
 * use cooldown is lowered every tick and after every use.
 */
public class FastPlaceModule
extends Module {
    private final BooleanSetting xpOnly = new BooleanSetting(EncryptedString.of("XP Bottles Only"), false).setDescription(EncryptedString.of("Only speeds up throwing experience bottles"));
    private final BooleanSetting blocks = new BooleanSetting(EncryptedString.of("Blocks"), true).setDescription(EncryptedString.of("Also places blocks faster"));
    private final BooleanSetting items = new BooleanSetting(EncryptedString.of("Items"), true).setDescription(EncryptedString.of("Throwables and other usable items (XP bottles, snowballs, eggs...)"));
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 4.0, 0.0, 1.0).getValue(EncryptedString.of("Ticks between uses (vanilla is 4)"));

    public FastPlaceModule() {
        super(EncryptedString.of("Fast Use"), EncryptedString.of("Uses and places items without the vanilla delay, e.g. XP bottles"), -1, Category.MISC);
        this.addSettings(this.xpOnly, this.blocks, this.items, this.delay);
    }

    private boolean applies() {
        if (this.mc.field_1724 == null) return false;
        class_1799 main = this.mc.field_1724.method_6047();
        class_1799 off = this.mc.field_1724.method_6079();
        boolean xp = main.method_31574(class_1802.field_8287) || off.method_31574(class_1802.field_8287);
        if (this.xpOnly.getValue()) return xp;
        if (xp) return true;
        class_1799 held = !main.method_7960() ? main : off;
        if (held.method_7960()) return false;
        // never speed up food/drinks, bows/crossbows or anchors/glowstone (would break charging or eating)
        if (held.method_58694(class_9334.field_50075) != null) return false;
        if (held.method_7909() instanceof class_1811) return false;
        if (held.method_31574(class_1802.field_23141) || held.method_31574(class_1802.field_8801)) return false;
        return held.method_7909() instanceof class_1747 ? this.blocks.getValue() : this.items.getValue();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1690 == null || !this.mc.field_1690.field_1904.method_1434() || !this.applies()) return;
        ItemUseCooldownAccessor client = (ItemUseCooldownAccessor) this.mc;
        if (client.hook$getItemUseCooldown() > this.delay.getIntValue()) {
            client.hook$setItemUseCooldown(this.delay.getIntValue());
        }
    }

    @EventListener
    public void onPostItemUse(PostItemUseEvent event) {
        if (this.applies()) {
            event.cooldown = Math.min(event.cooldown, this.delay.getIntValue());
        }
    }
}
