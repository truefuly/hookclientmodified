/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1304
 *  net.minecraft.class_1657
 *  net.minecraft.class_1802
 *  net.minecraft.class_304
 *  net.minecraft.class_3675$class_306
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.PreItemUseEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_304;
import net.minecraft.class_3675;

public final class ElytraGlideModule
extends Module {
    private boolean isFlyingTriggered;
    private boolean isFireworkUsed;
    private boolean isJumpKeyPressed;

    public ElytraGlideModule() {
        super(EncryptedString.of("Elytra Glide"), EncryptedString.of("Starts flying when attempting to use a firework"), -1, Category.MISC);
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
    public void onTick(TickEvent event) {
        if (this.mc.field_1755 != null) {
            return;
        }
        if (this.isJumpKeyPressed) {
            this.isJumpKeyPressed = false;
            class_304.method_1416((class_3675.class_306)this.mc.field_1690.field_1903.method_1429(), (boolean)false);
            return;
        }
        if (!this.isFlyingTriggered) {
            return;
        }
        if (!this.isFireworkUsed) {
            class_304.method_1416((class_3675.class_306)this.mc.field_1690.field_1903.method_1429(), (boolean)true);
            this.isJumpKeyPressed = true;
            if (!this.mc.field_1724.method_24828()) {
                this.isFireworkUsed = true;
            }
            return;
        }
        int selectedSlot = this.mc.field_1724.method_31548().method_67532();
        if (!this.mc.field_1724.method_6047().method_31574(class_1802.field_8639) && !InventoryUtil.swap(class_1802.field_8639)) {
            return;
        }
        this.mc.field_1761.method_2919((class_1657)this.mc.field_1724, this.mc.field_1724.method_6058());
        this.mc.field_1724.method_31548().method_61496(selectedSlot);
        this.isFireworkUsed = false;
        this.isFlyingTriggered = false;
    }

    @EventListener
    public void onPreItemUse(PreItemUseEvent event) {
        if (this.mc.field_1724.method_6047().method_31574(class_1802.field_8639) && this.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833) && this.mc.field_1724.method_6128()) {
            this.isFlyingTriggered = true;
        }
    }
}

