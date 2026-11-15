package hook.feature;

import hook.event.EventListener;
import hook.event.events.PostItemUseEvent;
import hook.event.events.PreItemUseEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.ModeSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.SettingVisibility;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;

/**
 * Wind charge pearl catch: makes your thrown ender pearl land on a wind charge.
 *
 * Vanilla rules this replays tick by tick: a pearl can't be hit, but a flying pearl hits wind charges. Each tick the
 * pearl applies gravity 0.03 and drag 0.99, then tests its move against the charge box inflated by 0.3; a move that
 * starts inside the box never counts. Entities tick in spawn order, so a pearl thrown first tests against the charge
 * before the charge moves that tick. The charge spawns at eye height and flies straight at 1.5 blocks per tick, the
 * pearl spawns 0.1 lower at 1.5, both inherit your movement.
 *
 * Pearl First: the pearl flies, then after the set delay the charge is thrown to meet it.
 * Wind First: the charge goes out in the same tick right before the pearl, which then catches it on the first tick.
 * Auto: Pearl First with your delay when that throw can catch, otherwise Wind First.
 */
public final class PearlCatchModule extends Module {
    public enum Order { Auto, PearlFirst, WindFirst }

    private static final double THROW_SPEED = 1.5;
    private static final double PEARL_GRAVITY = 0.03;
    private static final double PEARL_DRAG = 0.99;
    private static final double WIND_HALF = 0.15625, WIND_SIZE = 0.3125, HIT_MARGIN = 0.3;

    private final ModeSetting<Order> order = new ModeSetting<Order>(EncryptedString.of("Order"), Order.Auto, Order.class).setDescription(EncryptedString.of("Pearl First: wind charge after the delay. Wind First: wind charge right before the pearl. Auto: Pearl First when it can catch, else Wind First"));
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 20.0, 2.0, 1.0).getValue(EncryptedString.of("Ticks between the pearl and the wind charge (Pearl First)"));
    private final NumberSetting window = new NumberSetting(EncryptedString.of("Catch Window"), 1.0, 30.0, 12.0, 1.0).getValue(EncryptedString.of("Latest tick after the wind charge is thrown in which the pearl may land on it"));
    private final NumberSetting maxPitch = new NumberSetting(EncryptedString.of("Max Pitch"), -90.0, 90.0, 90.0, 1.0).getValue(EncryptedString.of("Only catches pearls thrown at least this far up (90 = any direction, negative = only upwards)"));

    /** Pending Pearl First throw. */
    private class_243[] pendingPearl;
    private int pendingIn = -1;
    private int pendingDelay;
    private boolean pearlArmed;
    private class_243[] armedPearl;

    public PearlCatchModule() {
        super(EncryptedString.of("Pearl Catch"), EncryptedString.of("Throws a wind charge so your ender pearl lands on it"), -1, Category.COMBAT);
        this.addSettings(this.order, this.delay, this.window, this.maxPitch);
        SettingVisibility.set(this.delay, () -> this.order.getValue() != Order.WindFirst);
    }

    @Override
    public void onDisable() {
        this.pendingIn = -1;
        this.pendingPearl = null;
        this.pearlArmed = false;
        super.onDisable();
    }

    @EventListener
    public void onPreItemUse(PreItemUseEvent event) {
        this.pearlArmed = false;
        var p = this.mc.field_1724;
        if (p == null || this.mc.field_1687 == null || this.mc.field_1761 == null || this.mc.field_1755 != null) return;
        if (p.method_6047().method_7909() != class_1802.field_8634) return;
        if (p.method_7357().method_7904(p.method_6047())) return;
        if (p.method_36455() > this.maxPitch.getFloatValue()) return;
        int windSlot = this.findWindChargeSlot();
        if (windSlot == -1 || p.method_7357().method_7904(p.method_31548().method_5438(windSlot))) return;

        class_243 eye = p.method_33571();
        class_243 inherit = this.inherit();
        int d = this.delay.getIntValue();
        // pearl path from its spawn, long enough for delay + window
        class_243[] pearl = this.pearlPath(new class_243(eye.field_1352, eye.field_1351 - 0.1, eye.field_1350),
                directionOf(p.method_36454(), p.method_36455()).method_1021(THROW_SPEED).method_1019(inherit), d + this.window.getIntValue() + 1);

        Order o = (Order) this.order.getValue();
        if (o != Order.WindFirst) {
            // will the delayed charge be able to catch it? (you keep roughly your current motion meanwhile)
            class_243 laterEye = eye.method_1019(inherit.method_1021(d));
            float[] aim = this.findAim(laterEye, inherit, pearl, d, false);
            if (aim != null || o == Order.PearlFirst) {
                if (aim == null) return; // Pearl First can't catch this throw: don't waste a charge
                this.pearlArmed = true;
                this.armedPearl = pearl;
                return; // vanilla throws the pearl now, the charge follows after the delay
            }
        }
        // Wind First: charge now, vanilla throws the pearl right after in this same tick
        float[] aim = this.findAim(eye, inherit, pearl, 0, true);
        if (aim == null) return;
        this.throwWind(windSlot, aim);
    }

    @EventListener
    public void onPostItemUse(PostItemUseEvent event) {
        if (!this.pearlArmed) return;
        this.pearlArmed = false;
        var p = this.mc.field_1724;
        if (p == null) return;
        if (!p.method_7357().method_7904(p.method_6047()) && p.method_6047().method_7909() == class_1802.field_8634) return; // pearl wasn't thrown
        this.pendingPearl = this.armedPearl;
        this.pendingDelay = this.delay.getIntValue();
        this.pendingIn = this.pendingDelay;
        if (this.pendingIn == 0) this.firePending();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.pendingIn <= 0) return;
        if (--this.pendingIn == 0) this.firePending();
    }

    private void firePending() {
        this.pendingIn = -1;
        class_243[] pearl = this.pendingPearl;
        this.pendingPearl = null;
        var p = this.mc.field_1724;
        if (pearl == null || p == null || this.mc.field_1761 == null) return;
        int windSlot = this.findWindChargeSlot();
        if (windSlot == -1 || p.method_7357().method_7904(p.method_31548().method_5438(windSlot))) return;
        // aim again from where you really are now
        float[] aim = this.findAim(p.method_33571(), this.inherit(), pearl, this.pendingDelay, false);
        if (aim != null) this.throwWind(windSlot, aim);
    }

    private void throwWind(int windSlot, float[] aim) {
        var p = this.mc.field_1724;
        int previous = p.method_31548().method_67532();
        float yaw = p.method_36454(), pitch = p.method_36455();
        InventoryUtil.swap(windSlot);
        p.method_36456(aim[0]);
        p.method_36457(aim[1]);
        // the use packet carries this rotation; the camera is put straight back
        this.mc.field_1761.method_2919((class_1657)p, class_1268.field_5808);
        p.method_36456(yaw);
        p.method_36457(pitch);
        InventoryUtil.swap(previous);
    }

    private class_243 inherit() {
        var p = this.mc.field_1724;
        class_243 m = p.method_18798();
        return new class_243(m.field_1352, p.method_24828() ? 0.0 : m.field_1351, m.field_1350);
    }

    /** Pearl position per tick after its spawn (index 0 = spawn); cut where it would hit a block. */
    private class_243[] pearlPath(class_243 start, class_243 vel, int ticks) {
        class_243[] path = new class_243[ticks + 1];
        path[0] = start;
        class_243 v = vel;
        for (int t = 1; t <= ticks; t++) {
            v = v.method_1031(0.0, -PEARL_GRAVITY, 0.0).method_1021(PEARL_DRAG);
            class_243 next = path[t - 1].method_1019(v);
            if (this.blocked(path[t - 1], next)) {
                class_243[] cut = new class_243[t];
                System.arraycopy(path, 0, cut, 0, t);
                return cut;
            }
            path[t] = next;
        }
        return path;
    }

    /**
     * Best {yaw, pitch} for a charge thrown from {@code windEye} {@code delay} ticks after the pearl spawned
     * (windFirst: in the same tick, before it), or null when no throw makes the pearl land on it.
     */
    private float[] findAim(class_243 windEye, class_243 inherit, class_243[] pearl, int delay, boolean windFirst) {
        int last = Math.min(pearl.length - 1, delay + this.window.getIntValue());
        float[] best = null;
        double bestScore = 0;
        // coarse: around the direction to the pearl at every possible meeting tick
        for (int t = delay + 1; t <= last; t++) {
            int flight = windFirst ? t : t - 1 - delay; // ticks the charge has flown when the pearl tests it
            class_243 rel = pearl[t].method_1020(windEye).method_1020(inherit.method_1021(flight));
            if (rel.method_1027() < 1.0E-6) continue;
            class_243 n = rel.method_1029();
            float gy = (float)Math.toDegrees(Math.atan2(-n.field_1352, n.field_1350));
            float gp = (float)Math.toDegrees(-Math.asin(n.field_1351));
            for (float dy = -6.0f; dy <= 6.0f; dy += 0.5f) {
                for (float dp = -6.0f; dp <= 6.0f; dp += 0.5f) {
                    float y = gy + dy, x = class_3532.method_15363(gp + dp, -90.0f, 90.0f);
                    double s = this.score(windEye, inherit, y, x, pearl, delay, windFirst, last);
                    if (s > bestScore) { bestScore = s; best = new float[]{y, x}; }
                }
            }
        }
        if (best == null) return null;
        // fine: centre it as well as possible
        float by = best[0], bp = best[1];
        for (float dy = -0.5f; dy <= 0.5f; dy += 0.05f) {
            for (float dp = -0.5f; dp <= 0.5f; dp += 0.05f) {
                float y = by + dy, x = class_3532.method_15363(bp + dp, -90.0f, 90.0f);
                double s = this.score(windEye, inherit, y, x, pearl, delay, windFirst, last);
                if (s > bestScore) { bestScore = s; best = new float[]{y, x}; }
            }
        }
        return new float[]{class_3532.method_15393(best[0]), best[1]};
    }

    /** 0 = no catch; otherwise higher for an earlier, more centred catch. */
    private double score(class_243 windEye, class_243 inherit, float yaw, float pitch, class_243[] pearl, int delay, boolean windFirst, int last) {
        class_243 u = directionOf(yaw, pitch).method_1021(THROW_SPEED).method_1019(inherit);
        for (int t = delay + 1; t <= last; t++) {
            int flight = windFirst ? t : t - 1 - delay;
            class_243 wind = windEye.method_1019(u.method_1021(flight));
            if (flight > 0 && this.blocked(windEye.method_1019(u.method_1021(flight - 1)), wind)) return 0; // charge burst on a block first
            class_238 box = new class_238(wind.field_1352 - WIND_HALF, wind.field_1351, wind.field_1350 - WIND_HALF,
                    wind.field_1352 + WIND_HALF, wind.field_1351 + WIND_SIZE, wind.field_1350 + WIND_HALF).method_1014(HIT_MARGIN);
            if (box.method_1006(pearl[t - 1])) continue; // a move starting inside never registers
            if (box.method_992(pearl[t - 1], pearl[t]).isEmpty()) continue;
            class_243 c = box.method_1005();
            class_243 d = pearl[t].method_1020(pearl[t - 1]);
            double len2 = d.method_1027();
            double k = len2 < 1.0E-9 ? 0 : Math.max(0, Math.min(1, c.method_1020(pearl[t - 1]).method_1026(d) / len2));
            double dist = pearl[t - 1].method_1019(d.method_1021(k)).method_1022(c);
            double room = Math.max(0.0, WIND_HALF + HIT_MARGIN - dist);
            return (last + 1 - t) * 0.05 + room + 0.001;
        }
        return 0;
    }

    private boolean blocked(class_243 from, class_243 to) {
        class_3965 hit = this.mc.field_1687.method_17742(new class_3959(from, to, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)this.mc.field_1724));
        return hit != null && hit.method_17783() == class_239.class_240.field_1332;
    }

    private static class_243 directionOf(float yaw, float pitch) {
        double y = Math.toRadians(yaw);
        double x = Math.toRadians(pitch);
        double cos = Math.cos(x);
        return new class_243(-Math.sin(y) * cos, -Math.sin(x), Math.cos(y) * cos);
    }

    private int findWindChargeSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            if (this.mc.field_1724.method_31548().method_5438(slot).method_7909() == class_1802.field_49098) return slot;
        }
        return -1;
    }
}
