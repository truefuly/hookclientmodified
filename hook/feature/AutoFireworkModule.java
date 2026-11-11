/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1304
 *  net.minecraft.class_1657
 *  net.minecraft.class_1802
 *  net.minecraft.class_9323
 *  net.minecraft.class_9334
 */
package hook.feature;

import hook.Hook;
import hook.event.EventListener;
import hook.event.events.PostItemUseEvent;
import hook.event.events.TickEvent;
import hook.feature.ElytraGlideModule;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BindSetting;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import hook.utils.KeyUtils;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_9323;
import net.minecraft.class_9334;

public final class AutoFireworkModule
extends Module {
    private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), -1, false);
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 20.0, 0.0, 1.0);
    private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true);
    private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 0.0, 1.0).getValue(EncryptedString.of("Delay after using firework before switching back."));
    private boolean isFireworkActive;
    private boolean hasUsedFirework;
    private int useDelayCounter;
    private int previousSelectedSlot;
    private int switchDelayCounter;
    private int cooldownCounter;

    public AutoFireworkModule() {
        super(EncryptedString.of("Auto Firework"), EncryptedString.of("Switches to a firework and uses it when you press a bind."), -1, Category.MISC);
        this.addSettings(this.activateKey, this.delay, this.switchBack, this.switchDelay);
    }

    @Override
    public void onEnable() {
        this.resetState();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        class_9323 components;
        ElytraGlideModule glide;
        if (this.mc.field_1755 != null) {
            return;
        }
        if (this.cooldownCounter > 0) {
            --this.cooldownCounter;
            return;
        }
        if (this.mc.field_1724 != null && KeyUtils.isKeyPressed(this.activateKey.getValue()) && ((glide = (ElytraGlideModule)Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(ElytraGlideModule.class)).isEnabled() || this.mc.field_1724.method_6128()) && this.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833) && !this.mc.field_1724.method_31548().method_7391().method_31574(class_1802.field_8639) && !(components = this.mc.field_1724.method_6047().method_7909().method_57347()).method_57832(class_9334.field_50075) && !components.method_57832(class_9334.field_54196)) {
            this.isFireworkActive = true;
        }
        if (!this.isFireworkActive) {
            return;
        }
        if (this.previousSelectedSlot == -1) {
            this.previousSelectedSlot = this.mc.field_1724.method_31548().method_67532();
        }
        if (!InventoryUtil.swap(class_1802.field_8639)) {
            this.resetState();
            return;
        }
        if (this.useDelayCounter < this.delay.getIntValue()) {
            ++this.useDelayCounter;
            return;
        }
        if (!this.hasUsedFirework) {
            this.mc.field_1761.method_2919((class_1657)this.mc.field_1724, class_1268.field_5808);
            this.hasUsedFirework = true;
        }
        if (this.switchBack.getValue()) {
            this.handleSwitchBack();
        } else {
            this.resetState();
        }
    }

    private void handleSwitchBack() {
        if (this.switchDelayCounter < this.switchDelay.getIntValue()) {
            ++this.switchDelayCounter;
        } else {
            InventoryUtil.swap(this.previousSelectedSlot);
            this.resetState();
        }
    }

    private void resetState() {
        this.previousSelectedSlot = -1;
        this.useDelayCounter = 0;
        this.switchDelayCounter = 0;
        this.cooldownCounter = 4;
        this.isFireworkActive = false;
        this.hasUsedFirework = false;
    }

    @EventListener
    public void onPostItemUse(PostItemUseEvent event) {
        if (this.mc.field_1724.method_6047().method_31574(class_1802.field_8639)) {
            this.hasUsedFirework = true;
        }
        if (this.cooldownCounter > 0) {
            event.cancel();
        }
    }
}

