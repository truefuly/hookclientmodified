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
    private final BooleanSetting continuous = new BooleanSetting(EncryptedString.of("Continuous Breeding"), false).setDescription(EncryptedString.of("Feed the same animal again after the breeding interval"));
    private final NumberSetting interval = new NumberSetting(EncryptedString.of("Breeding Interval"), 1.0, 24000.0, 6600.0, 20.0).getValue(EncryptedString.of("Ticks before the same animal is fed again (6600 = 30s love + 5 min cooldown)"));

    private final Map<UUID, Long> fed = new HashMap<>();
    private long tick;
    private int wait;
    private int previousSlot = -1;

    public AutoBreedModule() {
        super(EncryptedString.of("Auto Breed"), EncryptedString.of("Automatically breeds the selected animals around you"), -1, Category.MISC);
        this.entities.setDescription(EncryptedString.of("Animals that get bred"));
        this.addSettings(this.entities, this.range, this.hand, this.ageFilter, this.continuous, this.interval);
        SettingVisibility.set(this.interval, this.continuous::getValue);
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
            long keep = this.continuous.getValue() ? this.interval.getIntValue() : Long.MAX_VALUE;
            this.fed.values().removeIf(t -> this.tick - t > keep);
        }
        if (this.wait > 0) {
            this.wait--;
            return;
        }
        class_1268 useHand = this.hand.getValue() == BreedHand.OffHand ? class_1268.field_5810 : class_1268.field_5808;
        double maxDist = this.range.getValue();
        long refeedTicks = this.continuous.getValue() ? this.interval.getIntValue() : Long.MAX_VALUE;

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
            if (!this.canSee(animal)) continue; // only with a clear line of sight, never through blocks
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
        // like Meteor: look at it server side and feed it right away, one animal per tick
        float[] aim = aimAt(this.mc.field_1724.method_33571(), target.method_5829().method_1005());
        this.mc.method_1562().method_52787(new class_2828.class_2831(aim[0], aim[1], this.mc.field_1724.method_24828(), this.mc.field_1724.field_5976));
        // like a real right click (and Meteor): interact at the exact spot on the animal, then plain interact
        net.minecraft.class_3966 hit = new net.minecraft.class_3966(target, target.method_5829().method_1005());
        net.minecraft.class_1269 result = this.mc.field_1761.method_2917(this.mc.field_1724, target, hit, useHand);
        if (!result.method_23665()) result = this.mc.field_1761.method_2905(this.mc.field_1724, target, useHand);
        this.mc.field_1724.method_6104(useHand);
        this.fed.put(target.method_5667(), this.tick);
    }

    /** -1: the hand already holds food for it, 0-8: hotbar slot to switch to, -2: no food. */
    private int foodFor(class_1429 animal, class_1268 useHand) {
        class_1799 held = this.mc.field_1724.method_5998(useHand);
        if (!held.method_7960() && animal.method_6481(held)) return -1;
        if (true) return -2; // like Meteor: only the item already in the breeding hand
        for (int i = 0; i < 9; i++) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960() && animal.method_6481(stack)) return i;
        }
        return -2;
    }

    private void restoreSlot() {

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
            list.addEntity(id.toString(), type.method_5897().getString(), (class_1792) egg, type, DEFAULTS.contains(id.method_12832()), Color.WHITE);
        }
        return list;
    }

    /** True when some part of the animal can be seen from your eyes (fence gaps count, solid walls don't). */
    private boolean canSee(class_1429 animal) {
        class_243 eye = this.mc.field_1724.method_33571();
        net.minecraft.class_238 box = animal.method_5829();
        double[][] pts = {
            {(box.field_1323 + box.field_1320) / 2, (box.field_1322 + box.field_1325) / 2, (box.field_1321 + box.field_1324) / 2},
            {(box.field_1323 + box.field_1320) / 2, box.field_1325 - 0.05, (box.field_1321 + box.field_1324) / 2},
            {box.field_1323 + 0.05, (box.field_1322 + box.field_1325) / 2, box.field_1321 + 0.05},
            {box.field_1320 - 0.05, (box.field_1322 + box.field_1325) / 2, box.field_1324 - 0.05},
            {box.field_1323 + 0.05, (box.field_1322 + box.field_1325) / 2, box.field_1324 - 0.05},
            {box.field_1320 - 0.05, (box.field_1322 + box.field_1325) / 2, box.field_1321 + 0.05},
        };
        for (double[] p : pts) {
            class_243 to = new class_243(p[0], p[1], p[2]);
            net.minecraft.class_3965 hit = this.mc.field_1687.method_17742(new net.minecraft.class_3959(eye, to, net.minecraft.class_3959.class_3960.field_17558, net.minecraft.class_3959.class_242.field_1348, this.mc.field_1724));
            if (hit == null || hit.method_17783() == net.minecraft.class_239.class_240.field_1333) return true;
        }
        return false;
    }
}
