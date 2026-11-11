/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1747
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1811
 *  net.minecraft.class_9334
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.PostItemUseEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1811;
import net.minecraft.class_9334;

public class FastPlaceModule
extends Module {
    private final BooleanSetting onlyXP = new BooleanSetting("Only XP", false);
    private final BooleanSetting allowBlocks = new BooleanSetting("Blocks", true);
    private final BooleanSetting allowItems = new BooleanSetting("Items", true);
    private final NumberSetting useDelay = new NumberSetting("Delay", 0.0, 10.0, 0.0, 1.0);

    public FastPlaceModule() {
        super(EncryptedString.of("Fast Place"), EncryptedString.of("Spams use action."), -1, Category.MISC);
        this.addSettings(this.onlyXP, this.allowBlocks, this.allowItems, this.useDelay);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onPostItemUse(PostItemUseEvent event) {
        class_1799 main = this.mc.field_1724.method_6047();
        class_1799 off = this.mc.field_1724.method_6079();
        class_1792 mainItem = main.method_7909();
        class_1792 offItem = this.mc.field_1724.method_6079().method_7909();
        if (!main.method_31574(class_1802.field_8287) && !off.method_31574(class_1802.field_8287) && this.onlyXP.getValue()) {
            return;
        }
        if (!this.onlyXP.getValue()) {
            boolean placingBlock;
            boolean bl = placingBlock = mainItem instanceof class_1747 || offItem instanceof class_1747;
            if (placingBlock ? !this.allowBlocks.getValue() : !this.allowItems.getValue()) {
                return;
            }
        }
        if (main.method_58694(class_9334.field_50075) != null || off.method_58694(class_9334.field_50075) != null) {
            return;
        }
        if (main.method_31574(class_1802.field_23141) || main.method_31574(class_1802.field_8801) || off.method_31574(class_1802.field_23141) || off.method_31574(class_1802.field_8801)) {
            return;
        }
        if (mainItem instanceof class_1811 || offItem instanceof class_1811) {
            return;
        }
        event.cooldown = this.useDelay.getIntValue();
    }
}

