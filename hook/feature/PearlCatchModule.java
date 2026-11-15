package hook.feature;

import hook.event.EventListener;
import hook.event.events.PreItemUseEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;

/**
 * Wind charge pearl catch: when you throw an ender pearl, a wind charge is thrown first in the same tick, aimed so
 * the pearl runs right into it and you land on the charge.
 *
 * How vanilla decides it: a pearl can't be hit, but a flying pearl hits wind charges. Every tick the pearl applies
 * gravity (0.03) and drag (0.99), then tests its move against entity boxes inflated by 0.3; a move that starts inside
 * a box never counts. Entities tick in spawn order, so the charge (thrown first) has already moved when the pearl
 * tests its move. The charge spawns at eye height and flies straight at 1.5 blocks per tick; the pearl spawns 0.1
 * lower at 1.5; both inherit your movement. This module replays exactly that and picks the throw angle where the
 * pearl's move passes closest to the centre of the charge, so the server's small random throw spread can't make it miss.
 */
public final class PearlCatchModule extends Module {
    private static final double THROW_SPEED = 1.5;
    private static final double PEARL_GRAVITY = 0.03;
    private static final double PEARL_DRAG = 0.99;
    /** Wind charge hitbox is 0.3125 wide; projectile hit tests inflate the target box by 0.3. */
    private static final double WIND_HALF = 0.15625, WIND_SIZE = 0.3125, HIT_MARGIN = 0.3;

    private final NumberSetting maxTicks = new NumberSetting(EncryptedString.of("Catch Ticks"), 1.0, 10.0, 3.0, 1.0).getValue(EncryptedString.of("Latest tick after the throw in which the pearl may land on the wind charge"));
    private final NumberSetting maxPitch = new NumberSetting(EncryptedString.of("Max Pitch"), -90.0, 90.0, 90.0, 1.0).getValue(EncryptedString.of("Only catches pearls thrown at least this far up (90 = any direction, negative = only upwards)"));
    private final NumberSetting maxTurn = new NumberSetting(EncryptedString.of("Max Aim Offset"), 0.0, 10.0, 6.0, 0.5).getValue(EncryptedString.of("How many degrees the wind charge may be aimed away from where you look"));

    public PearlCatchModule() {
        super(EncryptedString.of("Pearl Catch"), EncryptedString.of("Throws a wind charge right before your pearl so the pearl lands on it"), -1, Category.COMBAT);
        this.addSettings(this.maxTicks, this.maxPitch, this.maxTurn);
    }

    @EventListener
    public void onPreItemUse(PreItemUseEvent event) {
        var p = this.mc.field_1724;
        if (p == null || this.mc.field_1687 == null || this.mc.field_1761 == null || this.mc.field_1755 != null) return;
        if (p.method_6047().method_7909() != class_1802.field_8634) return;
        if (p.method_7357().method_7904(p.method_6047())) return; // pearl on cooldown, nothing gets thrown
        if (p.method_36455() > this.maxPitch.getFloatValue()) return;
        int windSlot = this.findWindChargeSlot();
        if (windSlot == -1) return;
        if (p.method_7357().method_7904(p.method_31548().method_5438(windSlot))) return;

        float yaw = p.method_36454(), pitch = p.method_36455();
        // the server uses the position from your last movement packet, which is your current position
        class_243 eye = p.method_33571();
        class_243 motion = p.method_18798();
        class_243 inherit = new class_243(motion.field_1352, p.method_24828() ? 0.0 : motion.field_1351, motion.field_1350);
        class_243 pearlStart = new class_243(eye.field_1352, eye.field_1351 - 0.1, eye.field_1350);
        class_243 pearlVel = directionOf(yaw, pitch).method_1021(THROW_SPEED).method_1019(inherit);

        float[] aim = this.findBestAim(eye, inherit, pearlStart, pearlVel, yaw, pitch);
        if (aim == null) return;

        int pearlSlot = p.method_31548().method_67532();
        InventoryUtil.swap(windSlot);
        p.method_36456(aim[0]);
        p.method_36457(aim[1]);
        // the use packet carries this rotation, so the server throws the charge exactly where we aimed
        this.mc.field_1761.method_2919((class_1657)p, class_1268.field_5808);
        // camera never moves: the rotation is only used for that one packet
        p.method_36456(yaw);
        p.method_36457(pitch);
        // back on the pearl: vanilla now throws it with your real rotation, right after the charge
        InventoryUtil.swap(pearlSlot);
    }

    private float[] findBestAim(class_243 eye, class_243 inherit, class_243 pearlStart, class_243 pearlVel0, float yaw, float pitch) {
        int ticks = this.maxTicks.getIntValue();
        class_243[] pearl = new class_243[ticks + 1];
        pearl[0] = pearlStart;
        class_243 v = pearlVel0;
        int pearlTicks = ticks;
        for (int t = 1; t <= ticks; t++) {
            v = v.method_1031(0.0, -PEARL_GRAVITY, 0.0).method_1021(PEARL_DRAG);
            class_243 next = pearl[t - 1].method_1019(v);
            if (this.blocked(pearl[t - 1], next)) { pearlTicks = t - 1; break; }
            pearl[t] = next;
        }
        if (pearlTicks < 1) return null;

        float range = this.maxTurn.getFloatValue();
        float[] best = null;
        double bestScore = 0;
        for (float dy = -range; dy <= range + 1.0E-3f; dy += 0.25f) {
            for (float dp = -range; dp <= range + 1.0E-3f; dp += 0.25f) {
                float y = yaw + dy, x = class_3532.method_15363(pitch + dp, -90.0f, 90.0f);
                class_243 windVel = directionOf(y, x).method_1021(THROW_SPEED).method_1019(inherit);
                double score = this.simulate(eye, windVel, pearl, pearlTicks);
                // prefer the least turn on equal score
                score -= (Math.abs(dy) + Math.abs(dp)) * 1.0E-4;
                if (score > bestScore) { bestScore = score; best = new float[]{class_3532.method_15393(y), x}; }
            }
        }
        return best;
    }

    /** 0 = the pearl never lands on the charge; otherwise higher for an earlier, more centred catch. */
    private double simulate(class_243 eye, class_243 windVel, class_243[] pearl, int pearlTicks) {
        for (int t = 1; t <= pearlTicks; t++) {
            class_243 wind = eye.method_1019(windVel.method_1021(t));
            if (this.blocked(eye.method_1019(windVel.method_1021(t - 1)), wind)) return 0; // charge burst on a block first
            class_238 box = new class_238(wind.field_1352 - WIND_HALF, wind.field_1351, wind.field_1350 - WIND_HALF,
                    wind.field_1352 + WIND_HALF, wind.field_1351 + WIND_SIZE, wind.field_1350 + WIND_HALF).method_1014(HIT_MARGIN);
            if (box.method_1006(pearl[t - 1])) continue; // a move starting inside never registers
            if (box.method_992(pearl[t - 1], pearl[t]).isEmpty()) continue;
            // room for the random spread: how close the move passes to the box centre
            class_243 c = box.method_1005();
            class_243 d = pearl[t].method_1020(pearl[t - 1]);
            double len2 = d.method_1027();
            double k = len2 < 1.0E-9 ? 0 : Math.max(0, Math.min(1, c.method_1020(pearl[t - 1]).method_1026(d) / len2));
            double dist = pearl[t - 1].method_1019(d.method_1021(k)).method_1022(c);
            double room = Math.max(0.0, WIND_HALF + HIT_MARGIN - dist);
            return (pearlTicks + 1 - t) * 0.1 + room + 0.001;
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
