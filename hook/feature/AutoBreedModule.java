package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.SettingVisibility;
import hook.module.setting.StorageBlocksSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1429;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1826;
import net.minecraft.class_243;
import net.minecraft.class_2828;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Feeds the breeding item in your hand to nearby animals so they breed, like Meteor's Auto Breed: entity list,
 * range, hand and mob age filter. On top it refeeds animals once their love time ran out (so it keeps working
 * forever instead of feeding every animal only once), can pull the right food from the hotbar and looks at the
 * animal for anticheats.
 */
public final class AutoBreedModule extends Module {
    public enum BreedHand { MainHand, OffHand }
    public enum AgeFilter { Baby, Adult, Both }

    /** Meteor's default entity list. */
    private static final Set<String> DEFAULTS = Set.of("horse", "donkey", "cow", "mooshroom", "sheep", "pig", "chicken", "wolf",
            "cat", "ocelot", "rabbit", "llama", "turtle", "panda", "fox", "bee", "strider", "hoglin");

    private final StorageBlocksSetting entities = createEntityList();
    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 0.0, 6.0, 4.5, 0.1).getValue(EncryptedString.of("How far away from the player you can breed animals"));
    private final ModeSetting<BreedHand> hand = new ModeSetting<BreedHand>(EncryptedString.of("Hand For Breeding"), BreedHand.MainHand, BreedHand.class).setDescription(EncryptedString.of("The hand that holds the breeding item"));
    private final ModeSetting<AgeFilter> ageFilter = new ModeSetting<AgeFilter>(EncryptedString.of("Mob Age Filter"), AgeFilter.Adult, AgeFilter.class).setDescription(EncryptedString.of("Adult: breed. Baby: feed babies so they grow up faster. Both: everything"));
    private final BooleanSetting autoSwitch = new BooleanSetting(EncryptedString.of("Auto Switch"), true).setDescription(EncryptedString.of("Switches to the right food in your hotbar when the hand holds something the animal doesn't eat"));
    private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true).setDescription(EncryptedString.of("Switches back to your old hotbar slot when nothing is left to feed"));
    private final BooleanSetting rotate = new BooleanSetting(EncryptedString.of("Legit Rotate"), true).setDescription(EncryptedString.of("Smoothly turns your camera to the animal and feeds it with a normal right click on your crosshair, like a real player"));
    private final NumberSetting rotateSpeed = new NumberSetting(EncryptedString.of("Rotate Speed"), 3.0, 60.0, 18.0, 1.0).getValue(EncryptedString.of("Degrees per tick the camera turns"));
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 20.0, 1.0, 1.0).getValue(EncryptedString.of("Ticks between two feeds"));
    private final NumberSetting refeed = new NumberSetting(EncryptedString.of("Refeed After"), 5.0, 330.0, 31.0, 1.0).getValue(EncryptedString.of("Seconds before the same animal is fed again (love lasts 30s, breeding cooldown 5 min; on cooldown the server doesn't take the item)"));

    private final Map<UUID, Long> fed = new HashMap<>();
    private long tick;
    private int wait;
    private int previousSlot = -1;
    private class_1429 aiming;
    private int aimTicks;

    public AutoBreedModule() {
        super(EncryptedString.of("Auto Breed"), EncryptedString.of("Automatically breeds the selected animals around you"), -1, Category.MISC);
        this.entities.setDescription(EncryptedString.of("Animals that get bred"));
        SettingVisibility.set(this.switchBack, this.autoSwitch::getValue);
        this.addSettings(this.entities, this.range, this.hand, this.ageFilter, this.autoSwitch, this.switchBack, this.rotate, this.rotateSpeed, this.delay, this.refeed);
        SettingVisibility.set(this.rotateSpeed, this.rotate::getValue);
    }

    @Override
    public void onEnable() {
        this.fed.clear();
        this.wait = 0;
        this.previousSlot = -1;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.restoreSlot();
        this.fed.clear();
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1761 == null) return;
        this.tick++;
        if (this.tick % 200 == 0) {
            long keep = this.refeed.getIntValue() * 20L;
            this.fed.values().removeIf(t -> this.tick - t > keep);
        }
        if (this.wait > 0) {
            this.wait--;
            return;
        }
        class_1268 useHand = this.hand.getValue() == BreedHand.OffHand ? class_1268.field_5810 : class_1268.field_5808;
        double maxDist = this.range.getValue();
        long refeedTicks = this.refeed.getIntValue() * 20L;

        class_1429 target = null;
        int foodSlot = -1;
        double best = Double.MAX_VALUE;
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (!(entity instanceof class_1429 animal) || animal.method_31481() || !animal.method_5805()) continue;
            if (!this.typeSelected(animal.method_5864())) continue;
            boolean baby = animal.method_6109();
            AgeFilter filter = (AgeFilter) this.ageFilter.getValue();
            if (filter == AgeFilter.Adult && baby || filter == AgeFilter.Baby && !baby) continue;
            double dist = this.mc.field_1724.method_5739(animal);
            if (dist > maxDist || dist >= best) continue;
            Long last = this.fed.get(animal.method_5667());
            if (last != null && this.tick - last < refeedTicks) continue;
            int slot = this.foodFor(animal, useHand);
            if (slot == -2) continue; // nothing this animal eats is available
            target = animal;
            foodSlot = slot;
            best = dist;
        }
        if (target == null) {
            this.restoreSlot();
            return;
        }
        if (foodSlot >= 0) {
            if (this.previousSlot < 0) this.previousSlot = this.mc.field_1724.method_31548().method_67532();
            InventoryUtil.swap(foodSlot);
        }
        if (this.rotate.getValue()) {
            // legit: turn the real camera towards it, then right click it once it is under the crosshair
            if (this.aiming != target) { this.aiming = target; this.aimTicks = 0; }
            float[] aim = aimAt(this.mc.field_1724.method_33571(), target.method_5829().method_1005());
            float yaw = this.mc.field_1724.method_36454(), pitch = this.mc.field_1724.method_36455();
            float dy = net.minecraft.class_3532.method_15393(aim[0] - yaw), dp = aim[1] - pitch;
            float step = (float) this.rotateSpeed.getValue() * (0.85f + (float) Math.random() * 0.3f);
            this.mc.field_1724.method_36456(yaw + Math.max(-step, Math.min(step, dy)));
            this.mc.field_1724.method_36457(Math.max(-90f, Math.min(90f, pitch + Math.max(-step, Math.min(step, dp)))));
            boolean onTarget = this.mc.field_1765 instanceof net.minecraft.class_3966 eh && eh.method_17782() == target;
            if (!onTarget) {
                if (++this.aimTicks > 40) { this.fed.put(target.method_5667(), this.tick); this.aiming = null; } // can't reach it, try others
                return;
            }
            ((hook.mixin.MinecraftClientAccessor) this.mc).invokeDoItemUse();
            this.aiming = null;
        } else if (this.mc.field_1761.method_2905(this.mc.field_1724, target, useHand).method_23665()) {
            this.mc.field_1724.method_6104(useHand);
        }
        this.fed.put(target.method_5667(), this.tick);
        this.wait = this.delay.getIntValue();
    }

    /** -1: the hand already holds food for it, 0-8: hotbar slot to switch to, -2: no food. */
    private int foodFor(class_1429 animal, class_1268 useHand) {
        class_1799 held = this.mc.field_1724.method_5998(useHand);
        if (!held.method_7960() && animal.method_6481(held)) return -1;
        if (!this.autoSwitch.getValue() || useHand != class_1268.field_5808) return -2;
        for (int i = 0; i < 9; i++) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960() && animal.method_6481(stack)) return i;
        }
        return -2;
    }

    private void restoreSlot() {
        if (this.previousSlot >= 0 && this.switchBack.getValue() && this.mc.field_1724 != null) InventoryUtil.swap(this.previousSlot);
        this.previousSlot = -1;
    }

    private boolean typeSelected(class_1299<?> type) {
        class_2960 id = class_7923.field_41177.method_10221(type);
        StorageBlocksSetting.Entry entry = id == null ? null : this.entities.get(id.toString());
        return entry != null && entry.enabled();
    }

    private static float[] aimAt(class_243 from, class_243 to) {
        double dx = to.field_1352 - from.field_1352, dy = to.field_1351 - from.field_1351, dz = to.field_1350 - from.field_1350;
        double flat = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, flat));
        return new float[]{yaw, pitch};
    }

    private static StorageBlocksSetting createEntityList() {
        StorageBlocksSetting list = new StorageBlocksSetting(EncryptedString.of("Entities")).pickerOnly();
        for (class_1299<?> type : class_7923.field_41177) {
            class_1826 egg = class_1826.method_8019(type);
            if (egg == null) continue; // only mobs; Auto Breed skips everything that can't breed anyway
            class_2960 id = class_7923.field_41177.method_10221(type);
            if (id == null) continue;
            list.add(id.toString(), type.method_5897().getString(), (class_1792) egg, DEFAULTS.contains(id.method_12832()), Color.WHITE);
        }
        return list;
    }
}
