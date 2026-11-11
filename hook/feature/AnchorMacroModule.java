/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1802
 *  net.minecraft.class_1819
 *  net.minecraft.class_2246
 *  net.minecraft.class_239
 *  net.minecraft.class_3965
 *  net.minecraft.class_9334
 *  org.lwjgl.glfw.GLFW
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.module.setting.Setting;
import hook.utils.BlockUtil;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import hook.utils.KeyUtils;
import net.minecraft.class_1802;
import net.minecraft.class_1819;
import net.minecraft.class_2246;
import net.minecraft.class_239;
import net.minecraft.class_3965;
import net.minecraft.class_9334;
import org.lwjgl.glfw.GLFW;

public final class AnchorMacroModule
extends Module {
    private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 0.0, 1.0);
    private final NumberSetting glowstoneDelay = new NumberSetting(EncryptedString.of("Glowstone Delay"), 0.0, 20.0, 0.0, 1.0);
    private final NumberSetting explodeDelay = new NumberSetting(EncryptedString.of("Explode Delay"), 0.0, 20.0, 0.0, 1.0);
    private final NumberSetting totemSlot = new NumberSetting(EncryptedString.of("Totem Slot"), 1.0, 9.0, 1.0, 1.0);
    private int keybind = 0;
    private int glowstoneDelayCounter = 0;
    private int explodeDelayCounter = 0;

    public AnchorMacroModule() {
        super(EncryptedString.of("Anchor Macro"), EncryptedString.of("Charges and blows up respawn anchors for you"), -1, Category.COMBAT);
        Setting[] var10001 = new Setting[]{this.switchDelay, this.glowstoneDelay, this.explodeDelay, this.totemSlot};
        this.addSettings(var10001);
    }

    @Override
    public void onEnable() {
        this.resetCounters();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent var1) {
        if (this.mc.field_1755 == null && !this.isShieldOrFoodActive() && KeyUtils.isKeyPressed(1)) {
            this.handleAnchorInteraction();
        }
    }

    private boolean isShieldOrFoodActive() {
        boolean var1 = this.mc.field_1724.method_6047().method_7909().method_57347().method_57832(class_9334.field_50075) || this.mc.field_1724.method_6079().method_7909().method_57347().method_57832(class_9334.field_50075);
        boolean var2 = this.mc.field_1724.method_6047().method_7909() instanceof class_1819 || this.mc.field_1724.method_6079().method_7909() instanceof class_1819;
        boolean var3 = GLFW.glfwGetMouseButton((long)this.mc.method_22683().method_4490(), (int)1) == 1;
        return (var1 || var2) && var3;
    }

    private void handleAnchorInteraction() {
        class_3965 var1;
        class_239 class_2392 = this.mc.field_1765;
        if (class_2392 instanceof class_3965 && BlockUtil.isBlockAtPosition((var1 = (class_3965)class_2392).method_17777(), class_2246.field_23152)) {
            this.mc.field_1690.field_1904.method_23481(false);
            if (BlockUtil.isRespawnAnchorUncharged(var1.method_17777())) {
                this.placeGlowstone(var1);
            } else if (BlockUtil.isRespawnAnchorCharged(var1.method_17777())) {
                this.explodeAnchor(var1);
            }
        }
    }

    private void placeGlowstone(class_3965 var1) {
        if (!this.mc.field_1724.method_6047().method_31574(class_1802.field_8801)) {
            if (this.keybind < this.switchDelay.getIntValue()) {
                ++this.keybind;
                return;
            }
            this.keybind = 0;
            InventoryUtil.swap(class_1802.field_8801);
        }
        if (this.mc.field_1724.method_6047().method_31574(class_1802.field_8801)) {
            if (this.glowstoneDelayCounter < this.glowstoneDelay.getIntValue()) {
                ++this.glowstoneDelayCounter;
                return;
            }
            this.glowstoneDelayCounter = 0;
            BlockUtil.interactWithBlock(var1, true);
        }
    }

    private void explodeAnchor(class_3965 var1) {
        int var2 = this.totemSlot.getIntValue() - 1;
        if (this.mc.field_1724.method_31548().method_67532() != var2) {
            if (this.keybind < this.switchDelay.getIntValue()) {
                ++this.keybind;
                return;
            }
            this.keybind = 0;
            this.mc.field_1724.method_31548().method_61496(var2);
        }
        if (this.mc.field_1724.method_31548().method_67532() == var2) {
            if (this.explodeDelayCounter < this.explodeDelay.getIntValue()) {
                ++this.explodeDelayCounter;
                return;
            }
            this.explodeDelayCounter = 0;
            BlockUtil.interactWithBlock(var1, true);
        }
    }

    private void resetCounters() {
        this.keybind = 0;
        this.glowstoneDelayCounter = 0;
        this.explodeDelayCounter = 0;
    }
}

