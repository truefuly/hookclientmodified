/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_4174
 *  net.minecraft.class_9334
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.mixin.MinecraftClientAccessor;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import net.minecraft.class_1799;
import net.minecraft.class_4174;
import net.minecraft.class_9334;

public final class AutoEatModule
extends Module {
    private final NumberSetting healthThreshold = new NumberSetting(EncryptedString.of("Health Threshold"), 0.0, 19.0, 17.0, 1.0).getValue(EncryptedString.of("Eats when your health is at or below this"));
    private final NumberSetting hungerThreshold = new NumberSetting(EncryptedString.of("Hunger Threshold"), 0.0, 19.0, 19.0, 1.0).getValue(EncryptedString.of("Eats when your hunger bar is at or below this"));
    public boolean isEa;
    private int selectedFoodSlot;
    private int previousSelectedSlot;

    public AutoEatModule() {
        super(EncryptedString.of("Auto Eat"), EncryptedString.of("Eats food from your hotbar when hunger or health gets low"), -1, Category.MISC);
        this.addSettings(this.healthThreshold, this.hungerThreshold);
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
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) return;
        if (!this.isEa) {
            if (this.shouldEat()) {
                this.selectedFoodSlot = this.findBestFoodSlot();
                if (this.selectedFoodSlot != -1) {
                    this.saveCurrentSlot();
                }
            }
            return;
        }
        if (!this.shouldEat()) {
            this.stopEating();
            return;
        }
        class_1799 selected = this.mc.field_1724.method_31548().method_5438(this.selectedFoodSlot);
        if (selected.method_58694(class_9334.field_50075) == null) { // food in that slot is used up
            int bestSlot = this.findBestFoodSlot();
            if (bestSlot == -1) {
                this.stopEating();
                return;
            }
            this.selectSlot(bestSlot);
        }
        this.startEating();
    }

    public boolean shouldEat() {
        float healthDifference = this.mc.field_1724.method_6032() - (float)this.healthThreshold.getIntValue();
        boolean lowHealth = healthDifference <= 0.0f;
        int food = this.mc.field_1724.method_7344().method_7586();
        boolean lowHunger = food <= this.hungerThreshold.getIntValue();
        // Minecraft only lets you eat with a hunger bar below 20, so low health alone can't start eating
        return food < 20 && this.findBestFoodSlot() != -1 && (lowHealth || lowHunger);
    }

    private int findBestFoodSlot() {
        int bestSlot = -1;
        int bestNutrition = -1;
        for (int slot = 0; slot < 9; ++slot) {
            int nutrition;
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(slot);
            class_4174 food = (class_4174)stack.method_58694(class_9334.field_50075);
            if (food == null || (nutrition = food.comp_2491()) <= bestNutrition) continue;
            bestSlot = slot;
            bestNutrition = nutrition;
        }
        return bestSlot;
    }

    private void saveCurrentSlot() {
        this.previousSelectedSlot = this.mc.field_1724.method_31548().method_67532();
        this.startEating();
    }

    private void startEating() {
        this.selectSlot(this.selectedFoodSlot);
        this.setUseKeyPressed(true);
        if (!this.mc.field_1724.method_6115()) {
            ((MinecraftClientAccessor)this.mc).invokeDoItemUse();
        }
        this.isEa = true;
    }

    private void stopEating() {
        this.selectSlot(this.previousSelectedSlot);
        this.setUseKeyPressed(false);
        this.isEa = false;
    }

    private void setUseKeyPressed(boolean pressed) {
        this.mc.field_1690.field_1904.method_23481(pressed);
    }

    private void selectSlot(int slot) {
        InventoryUtil.swap(slot);
        this.selectedFoodSlot = slot;
    }
}

