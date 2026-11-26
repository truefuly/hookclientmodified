package hook.feature;

import hook.event.EventListener;
import hook.event.events.PacketReceiveEvent;
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
import net.minecraft.class_1826;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2828;
import net.minecraft.class_2960;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_7923;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Like Meteor's Auto Breed, but never through walls: feeds every selected animal in reach that you can actually see,
 * pulls the right food from the hotbar, refeeds once the breeding cooldown is over and only uses vanilla-legal reach,
 * rotation and hit positions so it works on servers with anticheats.
 */
public final class AutoBreedModule extends Module {
    public enum BreedHand { MainHand, OffHand }
    public enum AgeFilter { Baby, Adult, Both }

    /** Meteor's default list plus the newer breedable animals. */
    private static final Set<String> DEFAULTS = Set.of("horse", "donkey", "cow", "mooshroom", "sheep", "pig", "chicken", "wolf",
            "cat", "ocelot", "rabbit", "llama", "trader_llama", "turtle", "panda", "fox", "bee", "strider", "hoglin", "goat",
            "frog", "axolotl", "camel", "sniffer", "armadillo");

    private final StorageBlocksSetting entities = createEntityList();
    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 1.0, 6.0, 3.0, 0.1).getValue(EncryptedString.of("Reach to the animal's hitbox. 3 = vanilla reach, safe on every server; vanilla servers accept up to 6"));
    private final ModeSetting<BreedHand> hand = new ModeSetting<BreedHand>(EncryptedString.of("Hand For Breeding"), BreedHand.MainHand, BreedHand.class).setDescription(EncryptedString.of("The hand that holds the breeding item"));
    private final BooleanSetting switchFood = new BooleanSetting(EncryptedString.of("Auto Switch Food"), true).setDescription(EncryptedString.of("Takes the right food for each animal from your hotbar and switches back afterwards"));
    private final ModeSetting<AgeFilter> ageFilter = new ModeSetting<AgeFilter>(EncryptedString.of("Mob Age Filter"), AgeFilter.Adult, AgeFilter.class).setDescription(EncryptedString.of("Adult: breed. Baby: feed babies so they grow up faster. Both: everything"));
    private final NumberSetting perTick = new NumberSetting(EncryptedString.of("Animals Per Tick"), 1.0, 5.0, 1.0, 1.0).getValue(EncryptedString.of("How many animals are fed per tick. 1 is safest on anticheat servers (20 per second)"));
    private final BooleanSetting continuous = new BooleanSetting(EncryptedString.of("Continuous Breeding"), true).setDescription(EncryptedString.of("Feed the same animal again once its breeding cooldown is over"));
    private final NumberSetting interval = new NumberSetting(EncryptedString.of("Breeding Interval"), 20.0, 24000.0, 6020.0, 20.0).getValue(EncryptedString.of("Ticks before the same animal is fed again (6000 = vanilla breeding cooldown)"));

    private final Map<UUID, Integer> fed = new HashMap<>();
    /** Fed adults waiting for the server's "in love" hearts (entity event 18): uuid -> {tick fed, tries}. */
    private final Map<UUID, int[]> unconfirmed = new HashMap<>();
    private final java.util.concurrent.ConcurrentLinkedQueue<net.minecraft.class_2663> events = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private static final int CONFIRM_TICKS = 12, MAX_TRIES = 3;
    private int lastAge = Integer.MIN_VALUE;
    private int previousSlot = -1;
    static AutoBreedModule instance;

    public AutoBreedModule() {
        super(EncryptedString.of("Auto Breed"), EncryptedString.of("Breeds every selected animal around you that you can see"), -1, Category.MISC);
        this.entities.setDescription(EncryptedString.of("Animals that get bred"));
        this.addSettings(this.entities, this.range, this.hand, this.switchFood, this.ageFilter, this.perTick, this.continuous, this.interval);
        SettingVisibility.set(this.interval, this.continuous::getValue);
        this.moreSettings(this.hand, this.ageFilter, this.perTick, this.interval);
        instance = this;
    }

    @Override
    public void onEnable() {
        this.fed.clear();
        this.unconfirmed.clear();
        this.events.clear();
        this.previousSlot = -1;
        this.lastAge = Integer.MIN_VALUE;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.restoreSlot();
        this.fed.clear();
        super.onDisable();
    }

    private record Candidate(class_1429 animal, class_243 point, double dist) {}

    /** The server shows hearts (event 18) when an animal really went into love mode - ours or anyone's. */
    @EventListener
    public void onPacket(PacketReceiveEvent event) {
        if (event.packet instanceof net.minecraft.class_2663 e && e.method_11470() == 18) this.events.add(e);
    }

    private void readHearts(int now) {
        net.minecraft.class_2663 e;
        while ((e = this.events.poll()) != null) {
            if (this.mc.field_1687 == null) continue;
            class_1297 entity = e.method_11469(this.mc.field_1687);
            if (entity == null) continue;
            this.unconfirmed.remove(entity.method_5667());
            this.retries.remove(entity.method_5667());
            this.fed.put(entity.method_5667(), now); // in love now: no point feeding it again before the cooldown
        }
        // no hearts in time: the server did not take it (lag, anticheat, wrong food) -> try that animal again
        var it = this.unconfirmed.entrySet().iterator();
        while (it.hasNext()) {
            var en = it.next();
            if (now - en.getValue()[0] < CONFIRM_TICKS) continue;
            it.remove();
            if (en.getValue()[1] < MAX_TRIES) {
                this.fed.remove(en.getKey());
                this.retries.put(en.getKey(), en.getValue()[1]);
            }
        }
    }

    private final Map<UUID, Integer> retries = new HashMap<>();

    @EventListener
    public void onTick(TickEvent event) {
        var p = this.mc.field_1724;
        if (p == null || this.mc.field_1687 == null || this.mc.field_1761 == null) return;
        int now = p.field_6012;
        if (now == this.lastAge) return; // the tick event fires twice per game tick
        this.lastAge = now;
        this.readHearts(now);
        if (now % 200 == 0) {
            int keep = this.continuous.getValue() ? this.interval.getIntValue() : Integer.MAX_VALUE;
            this.fed.values().removeIf(t -> now - t > keep || now < t);
            this.retries.keySet().removeIf(id -> !this.fed.containsKey(id) && !this.unconfirmed.containsKey(id));
        }
        if (this.mc.field_1755 != null || p.method_6115()) return; // not while in a menu or eating / using an item

        class_1268 useHand = this.hand.getValue() == BreedHand.OffHand ? class_1268.field_5810 : class_1268.field_5808;
        double maxDist = this.range.getValue();
        int refeed = this.continuous.getValue() ? this.interval.getIntValue() : Integer.MAX_VALUE;
        AgeFilter filter = (AgeFilter) this.ageFilter.getValue();
        class_243 eye = p.method_33571();

        List<Candidate> list = new ArrayList<>();
        for (class_1297 entity : this.mc.field_1687.method_18112()) {
            if (!(entity instanceof class_1429 animal) || animal.method_31481() || !animal.method_5805()) continue;
            if (!this.typeSelected(animal.method_5864())) continue;
            boolean baby = animal.method_6109();
            if (filter == AgeFilter.Adult && baby || filter == AgeFilter.Baby && !baby) continue;
            class_238 box = animal.method_5829();
            double dist = Math.sqrt(box.method_49271(eye)); // vanilla measures reach to the hitbox, not the centre
            if (dist > maxDist) continue;
            Integer last = this.fed.get(animal.method_5667());
            if (last != null && now - last < refeed) continue;
            class_243 point = this.visiblePoint(eye, box, maxDist);
            if (point == null) continue; // never through walls
            list.add(new Candidate(animal, point, dist));
        }
        if (list.isEmpty()) {
            this.restoreSlot();
            return;
        }
        list.sort((a, b) -> Double.compare(a.dist, b.dist));

        int budget = this.perTick.getIntValue();
        for (Candidate c : list) {
            if (budget <= 0) break;
            int slot = this.foodFor(c.animal, useHand);
            if (slot == -2) continue;
            if (slot >= 0) {
                if (this.previousSlot < 0) this.previousSlot = p.method_31548().method_67532();
                InventoryUtil.swap(slot);
            }
            this.feed(c, useHand);
            budget--;
        }
    }

    private void feed(Candidate c, class_1268 useHand) {
        var p = this.mc.field_1724;
        class_1429 target = c.animal;
        // look at the visible spot server side, then click exactly there like a real right click
        float[] aim = aimAt(p.method_33571(), c.point);
        this.mc.method_1562().method_52787(new class_2828.class_2831(aim[0], aim[1], p.method_24828(), p.field_5976));
        class_3966 hit = new class_3966(target, c.point);
        net.minecraft.class_1269 result = this.mc.field_1761.method_2917(p, target, hit, useHand);
        if (!result.method_23665()) this.mc.field_1761.method_2905(p, target, useHand);
        p.method_6104(useHand);
        this.fed.put(target.method_5667(), p.field_6012);
        if (!target.method_6109()) {
            int tries = this.retries.getOrDefault(target.method_5667(), 0) + 1;
            this.unconfirmed.put(target.method_5667(), new int[]{p.field_6012, tries});
        }
    }

    /** -1: the hand already holds food for it, 0-8: hotbar slot to switch to, -2: no food. */
    private int foodFor(class_1429 animal, class_1268 useHand) {
        class_1799 held = this.mc.field_1724.method_5998(useHand);
        if (!held.method_7960() && animal.method_6481(held)) return -1;
        if (!this.switchFood.getValue() || useHand != class_1268.field_5808) return -2;
        for (int i = 0; i < 9; i++) {
            class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960() && animal.method_6481(stack)) return i;
        }
        return -2;
    }

    private void restoreSlot() {
        if (this.previousSlot >= 0 && this.mc.field_1724 != null) InventoryUtil.swap(this.previousSlot);
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

    /**
     * A point on the animal that your eyes can see with nothing solid in between (fence gaps count, walls don't),
     * within reach. Centre first, then a grid over the hitbox. null = not visible.
     */
    private class_243 visiblePoint(class_243 eye, class_238 box, double reach) {
        double[] fx = {0.5, 0.2, 0.8}, fy = {0.5, 0.85, 0.2}, fz = {0.5, 0.2, 0.8};
        for (double y : fy) for (double x : fx) for (double z : fz) {
            class_243 to = new class_243(box.field_1323 + (box.field_1320 - box.field_1323) * x,
                    box.field_1322 + (box.field_1325 - box.field_1322) * y,
                    box.field_1321 + (box.field_1324 - box.field_1321) * z);
            if (eye.method_1022(to) > reach + 0.6) continue;
            class_3965 hit = this.mc.field_1687.method_17742(new class_3959(eye, to, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, this.mc.field_1724));
            if (hit == null || hit.method_17783() == class_239.class_240.field_1333) return to;
        }
        return null;
    }
}
