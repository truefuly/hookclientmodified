/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2246
 *  net.minecraft.class_239
 *  net.minecraft.class_3965
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BindSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.Setting;
import hook.utils.BlockUtil;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import hook.utils.KeyUtils;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_239;
import net.minecraft.class_3965;

public final class DoubleAnchorModule
extends Module {
    private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), 71, false).setDescription(EncryptedString.of("Key that starts double anchoring"));
    private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 0.0, 1.0);
    private final NumberSetting totemSlot = new NumberSetting(EncryptedString.of("Totem Slot"), 1.0, 9.0, 1.0, 1.0);
    private int delayCounter = 0;
    private int step = 0;
    private boolean isAnchoring = false;

    public DoubleAnchorModule() {
        super(EncryptedString.of("Double Anchor"), EncryptedString.of("Places and detonates two respawn anchors in a row"), -1, Category.COMBAT);
        Setting[] var10001 = new Setting[]{this.switchDelay, this.totemSlot, this.activateKey};
        this.addSettings(var10001);
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
    public void onTick(TickEvent var1) {
        if (this.mc.field_1755 == null && this.mc.field_1724 != null && this.hasRequiredItems() && (this.isAnchoring || this.checkActivationKey())) {
            class_239 var2 = this.mc.field_1765;
            if (!(this.mc.field_1765 instanceof class_3965) || BlockUtil.isBlockAtPosition(((class_3965)var2).method_17777(), class_2246.field_10124)) {
                this.isAnchoring = false;
                this.resetState();
            } else if (this.delayCounter < this.switchDelay.getIntValue()) {
                ++this.delayCounter;
            } else {
                if (this.step == 0) {
                    InventoryUtil.swap(class_1802.field_23141);
                } else if (this.step == 1) {
                    BlockUtil.interactWithBlock((class_3965)var2, true);
                } else if (this.step == 2) {
                    InventoryUtil.swap(class_1802.field_8801);
                } else if (this.step == 3) {
                    BlockUtil.interactWithBlock((class_3965)var2, true);
                } else if (this.step == 4) {
                    InventoryUtil.swap(class_1802.field_23141);
                } else if (this.step == 5) {
                    BlockUtil.interactWithBlock((class_3965)var2, true);
                    BlockUtil.interactWithBlock((class_3965)var2, true);
                } else if (this.step == 6) {
                    InventoryUtil.swap(class_1802.field_8801);
                } else if (this.step == 7) {
                    BlockUtil.interactWithBlock((class_3965)var2, true);
                } else if (this.step == 8) {
                    InventoryUtil.swap(this.totemSlot.getIntValue() - 1);
                } else if (this.step == 9) {
                    BlockUtil.interactWithBlock((class_3965)var2, true);
                } else if (this.step == 10) {
                    this.isAnchoring = false;
                    this.step = 0;
                    this.resetState();
                    return;
                }
                ++this.step;
            }
        }
    }

    private boolean hasRequiredItems() {
        boolean var1 = false;
        boolean var2 = false;
        for (int var3 = 0; var3 < 9; ++var3) {
            class_1799 var4 = this.mc.field_1724.method_31548().method_5438(var3);
            if (var4.method_7909().equals(class_1802.field_23141)) {
                var1 = true;
            }
            if (!var4.method_7909().equals(class_1802.field_8801)) continue;
            var2 = true;
        }
        return var1 && var2;
    }

    private boolean checkActivationKey() {
        int var1 = this.activateKey.getValue();
        if (var1 != -1 && KeyUtils.isKeyPressed(var1)) {
            this.isAnchoring = true;
            return true;
        }
        this.resetState();
        return false;
    }

    private void resetState() {
        this.delayCounter = 0;
    }

    public boolean isAnchoringActive() {
        return this.isAnchoring;
    }
}

