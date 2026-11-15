package hook.feature;

import hook.event.EventListener;
import hook.event.events.PostItemUseEvent;
import hook.event.events.PreItemUseEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2350;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;

/**
 * Catches a thrown ender pearl with a wind charge for a teleport plus wind boost.
 *
 * A wind charge can't hit a pearl; it only bursts when it hits a block, and the burst pushes every entity within
 * 2.4 blocks away from its centre, harder the closer it is. So right after the pearl leaves your hand (same tick,
 * before it flies out of reach) this searches the directions around you for the block hit whose burst pushes the
 * pearl hardest along its flight path, throws the wind charge there with a silent rotation and switches back.
 */
public final class PearlCatchModule extends Module {
    /** Player-thrown pearls and wind charges both start at 1.5 blocks per tick. */
    private static final double THROW_SPEED = 1.5;
    private static final double PEARL_GRAVITY = 0.03;
    private static final double PEARL_DRAG = 0.99;

    private final NumberSetting maxTicks = new NumberSetting(EncryptedString.of("Catch Ticks"), 2.0, 10.0, 4.0, 1.0).getValue(EncryptedString.of("How many ticks after the throw the wind charge may meet the pearl at most"));
    private final NumberSetting maxPitch = new NumberSetting(EncryptedString.of("Max Pitch"), -90.0, 90.0, 90.0, 1.0).getValue(EncryptedString.of("Only boosts pearls thrown at least this far up (90 = any direction, negative = only upwards)"));
    private final BooleanSetting silent = new BooleanSetting(EncryptedString.of("Silent Rotation"), true).setDescription(EncryptedString.of("Aims the wind charge without moving your camera"));
    private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true).setDescription(EncryptedString.of("Returns to the pearl slot right away"));

    private boolean pearlArmed;

    public PearlCatchModule() {
        super(EncryptedString.of("Pearl Catch"), EncryptedString.of("Boosts your thrown ender pearl further with a wind charge burst"), -1, Category.COMBAT);
        this.addSettings(this.maxTicks, this.maxPitch, this.silent, this.switchBack);
    }

    @Override
    public void onDisable() {
        this.pearlArmed = false;
        super.onDisable();
    }

    @EventListener
    public void onPreItemUse(PreItemUseEvent event) {
        this.pearlArmed = false;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1755 != null) return;
        class_1799 hand = this.mc.field_1724.method_5998(class_1268.field_5808);
        if (hand.method_7909() != class_1802.field_8634) return;
        if (this.mc.field_1724.method_7357().method_7904(hand)) return; // still on cooldown, nothing gets thrown
        if (this.mc.field_1724.method_36455() > this.maxPitch.getFloatValue()) return;
        this.pearlArmed = true;
    }

    @EventListener
    public void onPostItemUse(PostItemUseEvent event) {
        if (!this.pearlArmed) return;
        this.pearlArmed = false;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1761 == null) return;
        // the pearl was really thrown only if it went on cooldown
        if (!this.mc.field_1724.method_7357().method_7904(new class_1799(class_1802.field_8634))) return;
        int windSlot = this.findWindChargeSlot();
        if (windSlot == -1) return;
        if (this.mc.field_1724.method_7357().method_7904(this.mc.field_1724.method_31548().method_5438(windSlot))) return;

        class_243 eye = this.mc.field_1724.method_33571();
        class_243 pearlPos = new class_243(eye.field_1352, eye.field_1351 - 0.1, eye.field_1350);
        class_243 motion = this.mc.field_1724.method_18798();
        class_243 pearlVel = this.mc.field_1724.method_5828(1.0f).method_1021(THROW_SPEED)
                .method_1031(motion.field_1352, this.mc.field_1724.method_24828() ? 0.0 : motion.field_1351, motion.field_1350);

        float[] aim = this.findBestAim(eye, pearlPos, pearlVel);
        if (aim == null) return;

        int previousSlot = this.mc.field_1724.method_31548().method_67532();
        float yaw = this.mc.field_1724.method_36454();
        float pitch = this.mc.field_1724.method_36455();
        InventoryUtil.swap(windSlot);
        this.mc.field_1724.method_36456(aim[0]);
        this.mc.field_1724.method_36457(aim[1]);
        // the use packet carries this rotation, so the server throws the charge exactly where we aimed
        this.mc.field_1761.method_2919((class_1657)this.mc.field_1724, class_1268.field_5808);
        if (this.silent.getValue()) {
            this.mc.field_1724.method_36456(yaw);
            this.mc.field_1724.method_36457(pitch);
        }
        if (this.switchBack.getValue()) InventoryUtil.swap(previousSlot);
    }

    /**
     * The pearl itself can't be hit, but a flying pearl hits wind charges (they are redirectable projectiles). So the
     * wind charge is thrown right away into the point where it meets the pearl a few ticks later: the pearl runs into
     * it, the charge bursts and you get teleported with the boost. Picks the tick where both arrive at the same time.
     */
    private float[] findBestAim(class_243 eye, class_243 pearlStart, class_243 pearlVel0) {
        int ticks = this.maxTicks.getIntValue();
        class_243 motion = this.mc.field_1724.method_18798();
        class_243 inherit = new class_243(motion.field_1352, this.mc.field_1724.method_24828() ? 0.0 : motion.field_1351, motion.field_1350);
        class_243 p = pearlStart;
        class_243 v = pearlVel0;
        class_243 bestTarget = null;
        double bestMiss = Double.MAX_VALUE;
        for (int t = 1; t <= ticks; t++) {
            p = p.method_1019(v);
            v = v.method_1031(0.0, -PEARL_GRAVITY, 0.0).method_1021(PEARL_DRAG);
            if (t < 2) continue; // never burst right in your face
            // where the charge must head (its own motion on top of what it inherits from you)
            class_243 rel = p.method_1020(inherit.method_1021(t)).method_1020(eye);
            double miss = Math.abs(rel.method_1033() - THROW_SPEED * t);
            // don't aim through walls
            class_3965 hit = this.mc.field_1687.method_17742(new class_3959(eye, p, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)this.mc.field_1724));
            if (hit != null && hit.method_17783() == class_239.class_240.field_1332) break;
            if (miss < bestMiss) { bestMiss = miss; bestTarget = rel; }
        }
        if (bestTarget == null || bestMiss > 1.0) return null;
        class_243 d = bestTarget.method_1029();
        float yaw = (float)Math.toDegrees(Math.atan2(-d.field_1352, d.field_1350));
        float pitch = (float)Math.toDegrees(-Math.asin(d.field_1351));
        return new float[]{class_3532.method_15393(yaw), class_3532.method_15363(pitch, -90.0f, 90.0f)};
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
