package hook.feature;

import hook.event.EventListener;
import hook.event.events.PacketReceiveEvent;
import hook.event.events.PostItemUseEvent;
import hook.event.events.PreItemUseEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import java.util.ArrayList;
import java.util.List;
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
 * Wind charge pearl catch: after you throw an ender pearl, a wind charge goes out after the set delay, aimed so the
 * flying pearl lands on it.
 *
 * Measured on a real server, tick by tick: the pearl spawns 0.1 below your eyes, the charge at eye height, both at
 * 1.5 blocks per tick plus your movement and a small random spread (about 0.02 per tick per axis). Each tick the pearl
 * applies gravity 0.03 and drag 0.99, then moves and tests its move against the charge box (0.3125, hanging 0.15 below
 * the charge's position, inflated by the pearl's age margin 0..0.3) where the charge was before its own move that tick;
 * a move starting inside that box never counts. The charge flies
 * straight. This replays that, searches the throw angle where the pearl falls into the charge, and keeps the one that
 * still catches with the random spread pushed to its limits.
 */
public final class PearlCatchModule extends Module {
    private static final double THROW_SPEED = 1.5;
    private static final double PEARL_GRAVITY = 0.03;
    private static final double PEARL_DRAG = 0.99;
    private static final double WIND_HALF = 0.15625, WIND_SIZE = 0.3125, WIND_DROP = 0.15, HIT_MARGIN = 0.3;
    /** Random throw spread per tick and axis seen on the server. */
    private static final double SPREAD = 0.02;
    /** How long after the charge the pearl may still land on it. Big delays need a long flight (25-40 ticks). */
    private static final int SEARCH_TICKS = 80;

    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 20.0, 0.0, 1.0).getValue(EncryptedString.of("Ticks between the pearl and the wind charge"));
    private final NumberSetting maxPitch = new NumberSetting(EncryptedString.of("Max Pitch"), -90.0, 90.0, 90.0, 1.0).getValue(EncryptedString.of("Only catches pearls thrown at least this far up (90 = any direction, negative = only upwards)"));

    private class_243 pearlStart, pearlVel;
    private int pendingAge = -1;
    private int pendingDelay;
    private boolean pearlArmed;
    /** Exact start of our pearl as the server spawned it (random spread included), from its spawn packet. */
    private volatile class_243 spawnPos, spawnVel;
    private volatile long throwTime;

    public PearlCatchModule() {
        super(EncryptedString.of("Pearl Catch"), EncryptedString.of("Throws a wind charge after your pearl so the pearl lands on it"), -1, Category.COMBAT);
        this.addSettings(this.delay, this.maxPitch);
    }

    @Override
    public void onDisable() {
        this.pendingAge = -1;
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
        if (this.findWindChargeSlot() == -1) return;
        class_243 eye = p.method_33571();
        this.pearlStart = new class_243(eye.field_1352, eye.field_1351 - 0.1, eye.field_1350);
        this.pearlVel = directionOf(p.method_36454(), p.method_36455()).method_1021(THROW_SPEED).method_1019(this.inherit());
        this.spawnPos = null;
        this.spawnVel = null;
        this.throwTime = System.currentTimeMillis(); // before the use packet goes out: its spawn packet comes after
        this.pearlArmed = true;
        // same-tick mode: work the aim out BEFORE the pearl packet goes out, so both packets leave back to back and
        // the server handles them in the same tick (searching after the throw can take long enough to slip a tick)
        this.preAim = null;
        if (this.delay.getIntValue() == 0 && !PearlCatchTest.TRACE) {
            try { this.preAim = this.findAim(p.method_33571(), this.inherit(), 0); } catch (Throwable t) { this.preAim = null; }
        }
    }

    private float[] preAim;

    @EventListener
    public void onPostItemUse(PostItemUseEvent event) {
        if (!this.pearlArmed) return;
        this.pearlArmed = false;
        var p = this.mc.field_1724;
        if (p == null) return;
        if (!p.method_7357().method_7904(new net.minecraft.class_1799(class_1802.field_8634))) return; // pearl wasn't thrown
        this.pendingDelay = this.delay.getIntValue();
        this.pendingAge = p.field_6012 + this.pendingDelay;
        if (this.pendingDelay == 0) this.firePending();
    }

    @EventListener
    public void onPacket(PacketReceiveEvent event) {
        // entity spawns come wrapped in a bundle packet
        if (event.packet instanceof net.minecraft.class_8038<?> bundle) {
            for (Object sub : bundle.method_48324()) if (sub instanceof net.minecraft.class_2604 add) this.onAddEntity(add);
            return;
        }
        if (event.packet instanceof net.minecraft.class_2604 add) this.onAddEntity(add);
    }

    private void onAddEntity(net.minecraft.class_2604 add) {
        if (add.method_11169() != net.minecraft.class_1299.field_6082) return;
        var p = this.mc.field_1724;
        if (p == null || add.method_11166() != p.method_5628()) return; // data = owner id
        if (System.currentTimeMillis() - this.throwTime > 3000) return;
        this.spawnVel = add.method_73084();
        this.spawnPos = new class_243(add.method_11175(), add.method_11174(), add.method_11176());
        if (PearlCatchTest.ENABLED) System.out.println("PEARLSPAWN pos=" + this.spawnPos + " vel=" + this.spawnVel);
    }

    /**
     * Our pearl's path rebuilt from its spawn packet: exact start and velocity (with the server's random spread).
     * The packet may be sent after the pearl already moved a tick or two, so match its velocity to the throw.
     */
    private class_243[] packetPearlPath(int ticks) {
        class_243 pos = this.spawnPos, vel = this.spawnVel;
        if (pos == null || vel == null || this.pearlVel == null) return null;
        int bestK = 0;
        double best = Double.MAX_VALUE;
        class_243 v = this.pearlVel;
        for (int k = 0; k <= 3; k++) {
            double d = v.method_1020(vel).method_1033();
            if (d < best) { best = d; bestK = k; }
            v = v.method_1031(0.0, -PEARL_GRAVITY, 0.0).method_1021(PEARL_DRAG);
        }
        if (best > 0.15) return null; // not our throw
        class_243[] path = new class_243[ticks + 1];
        class_243[] analytic = this.pearlPath(this.pearlStart, this.pearlVel, Math.min(ticks, bestK), false);
        for (int t = 0; t < bestK && t <= ticks; t++) path[t] = analytic[Math.min(t, analytic.length - 1)];
        if (bestK > ticks) return null;
        path[bestK] = pos;
        class_243 cur = vel;
        for (int t = bestK + 1; t <= ticks; t++) {
            cur = cur.method_1031(0.0, -PEARL_GRAVITY, 0.0).method_1021(PEARL_DRAG);
            class_243 next = path[t - 1].method_1019(cur);
            if (this.blocked(path[t - 1], next)) {
                class_243[] cut = new class_243[t];
                System.arraycopy(path, 0, cut, 0, t);
                return cut;
            }
            path[t] = next;
        }
        return path;
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (PearlCatchTest.ENABLED) PearlCatchTest.tick(this);
        // TickEvent fires twice per game tick, so wait on the player's real tick counter
        if (this.pendingAge < 0 || this.mc.field_1724 == null) return;
        if (this.mc.field_1724.field_6012 >= this.pendingAge) this.firePending();
    }

    private void firePending() {
        this.pendingAge = -1;
        var p = this.mc.field_1724;
        if (p == null || this.mc.field_1761 == null || this.pearlStart == null) return;
        int windSlot = this.findWindChargeSlot();
        if (windSlot == -1 || p.method_7357().method_7904(p.method_31548().method_5438(windSlot))) {
            return;
        }
        float[] aim = this.pendingDelay == 0 && this.preAim != null ? this.preAim : PearlCatchTest.TRACE ? new float[]{p.method_36454(), p.method_36455()} : this.findAim(p.method_33571(), this.inherit(), this.pendingDelay);
        this.preAim = null;
        if (PearlCatchTest.ENABLED) System.out.println("PEARLAIM yaw=" + (aim == null ? "-" : aim[0] + " pitch=" + aim[1]) + " pearlVel=" + this.pearlVel + " start=" + this.pearlStart + " eye=" + p.method_33571() + " pkt=" + (this.packetPearlPath(3) != null));
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

    void testConfigure(int d) {
        this.delay.getValue((double) d);
    }

    private class_243 inherit() {
        var p = this.mc.field_1724;
        class_243 m = p.method_18798();
        return new class_243(m.field_1352, p.method_24828() ? 0.0 : m.field_1351, m.field_1350);
    }

    /** Pearl position per tick after its spawn (index 0 = spawn); cut where it would hit a block. */
    private class_243[] pearlPath(class_243 start, class_243 vel, int ticks, boolean checkBlocks) {
        class_243[] path = new class_243[ticks + 1];
        path[0] = start;
        class_243 v = vel;
        for (int t = 1; t <= ticks; t++) {
            v = v.method_1031(0.0, -PEARL_GRAVITY, 0.0).method_1021(PEARL_DRAG);
            class_243 next = path[t - 1].method_1019(v);
            if (checkBlocks && this.blocked(path[t - 1], next)) {
                class_243[] cut = new class_243[t];
                System.arraycopy(path, 0, cut, 0, t);
                return cut;
            }
            path[t] = next;
        }
        return path;
    }

    /**
     * Path of our pearl as the client sees it: the client runs the same physics from the exact spawn values the server
     * sent, so this has none of the random spread. Index = ticks since the throw (the entity's own age).
     */
    private class_243[] realPearlPath(int ticks) {
        var p = this.mc.field_1724;
        net.minecraft.class_1684 mine = null;
        for (class_1297 e : this.mc.field_1687.method_18112()) {
            if (e instanceof net.minecraft.class_1684 pearl && !pearl.method_31481() && pearl.method_24921() == p) {
                if (mine == null || pearl.field_6012 < mine.field_6012) mine = pearl;
            }
        }
        if (mine == null) return null;
        int age = mine.field_6012;
        if (age > ticks) return null;
        class_243[] path = new class_243[ticks + 1];
        class_243 pos = new class_243(mine.method_23317(), mine.method_23318(), mine.method_23321());
        class_243 v = mine.method_18798();
        // ticks before what we can see are never tested (the charge isn't out yet), fill them with the current point
        for (int t = 0; t <= age; t++) path[t] = pos;
        for (int t = age + 1; t <= ticks; t++) {
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

    private record Candidate(float yaw, float pitch, double score) {}

    /** Best {yaw, pitch} for a charge thrown from {@code windEye} {@code delay} ticks after the pearl, or null. */
    private float[] findAim(class_243 windEye, class_243 inherit, int delay) {
        int last = delay + SEARCH_TICKS;
        // exact path from the spawn packet; without it (charge goes out before the packet arrives) the throw itself
        class_243[] pearl = this.packetPearlPath(last);
        boolean exact = pearl != null;
        if (pearl == null) pearl = this.pearlPath(this.pearlStart, this.pearlVel, last, true);
        last = Math.min(last, pearl.length - 1);
        if (last <= delay) return null;

        // coarse search around the direction to every point of the pearl's path
        List<Candidate> found = new ArrayList<>();
        for (int t = delay + 1; t <= last; t++) {
            int flight = t - 1 - delay; // ticks the charge has flown when the pearl tests it
            class_243 rel = pearl[t].method_1020(windEye).method_1020(inherit.method_1021(flight));
            if (rel.method_1027() < 1.0E-6) continue;
            class_243 n = rel.method_1029();
            float gy = (float)Math.toDegrees(Math.atan2(-n.field_1352, n.field_1350));
            float gp = (float)Math.toDegrees(-Math.asin(n.field_1351));
            for (float dy = -5.0f; dy <= 5.0f; dy += 0.5f) {
                for (float dp = -5.0f; dp <= 5.0f; dp += 0.5f) {
                    float y = gy + dy, x = class_3532.method_15363(gp + dp, -90.0f, 90.0f);
                    double s = score(windEye, directionOf(y, x).method_1021(THROW_SPEED).method_1019(inherit), pearl, delay, last);
                    if (s > 0) found.add(new Candidate(y, x, s));
                }
            }
        }
        if (found.isEmpty()) return null;
        found.sort((a, b) -> Double.compare(b.score, a.score));

        // refine and pick the one that survives the random throw spread best
        class_243[][] pearlVariants = new class_243[5][];
        pearlVariants[0] = pearl;
        double[][] pd = {{SPREAD, 0, 0}, {-SPREAD, 0, 0}, {0, SPREAD, 0}, {0, -SPREAD, 0}};
        for (int i = 0; i < 4; i++) {
            pearlVariants[i + 1] = exact ? pearl : this.pearlPath(this.pearlStart, this.pearlVel.method_1031(pd[i][0], pd[i][1], pd[i][2]), last, false);
        }
        Candidate best = null;
        double bestRobust = -1;
        int checked = 0;
        for (Candidate c : found) {
            if (checked++ >= 40) break;
            for (float dy = -0.4f; dy <= 0.41f; dy += 0.2f) {
                for (float dp = -0.4f; dp <= 0.41f; dp += 0.2f) {
                    float y = c.yaw + dy, x = class_3532.method_15363(c.pitch + dp, -90.0f, 90.0f);
                    class_243 u = directionOf(y, x).method_1021(THROW_SPEED).method_1019(inherit);
                    double robust = this.robustness(windEye, u, pearlVariants, delay, last);
                    if (robust > bestRobust) {
                        if (this.windBlocked(windEye, u, delay, pearl, last)) continue;
                        bestRobust = robust;
                        best = new Candidate(y, x, robust);
                    }
                }
            }
        }
        if (best == null) return null;
        return new float[]{class_3532.method_15393(best.yaw), best.pitch};
    }

    /** How many of the spread variants (pearl and charge) still catch, plus a little for an early, centred catch. */
    private double robustness(class_243 windEye, class_243 u, class_243[][] pearlVariants, int delay, int last) {
        double total = 0;
        for (class_243[] pv : pearlVariants) if (score(windEye, u, pv, delay, last) > 0) total += 1;
        double[][] wd = {{SPREAD, 0, 0}, {-SPREAD, 0, 0}, {0, SPREAD, 0}, {0, -SPREAD, 0}, {0, 0, SPREAD}, {0, 0, -SPREAD}};
        for (double[] d : wd) if (score(windEye, u.method_1031(d[0], d[1], d[2]), pearlVariants[0], delay, last) > 0) total += 1;
        // network jitter can put the two throws one server tick closer or further apart than sent: catch both too
        if (score(windEye, u, pearlVariants[0], delay + 1, last) > 0) total += 1.5;
        if (delay > 0 && score(windEye, u, pearlVariants[0], delay - 1, last) > 0) total += 1.5;
        return total + score(windEye, u, pearlVariants[0], delay, last);
    }

    /** 0 = no catch; otherwise below 1, higher for an earlier, more centred catch. */
    private static double score(class_243 windEye, class_243 u, class_243[] pearl, int delay, int last) {
        int end = Math.min(last, pearl.length - 1);
        for (int t = delay + 1; t <= end; t++) {
            int flight = t - 1 - delay;
            double wx = windEye.field_1352 + u.field_1352 * flight, wy = windEye.field_1351 + u.field_1351 * flight, wz = windEye.field_1350 + u.field_1350 * flight;
            // wind charge box hangs 0.15 below its position (AbstractWindCharge.makeBoundingBox); the pearl's hit
            // margin grows with its age: tickCount is t in its t-th tick, margin = clamp((t - 2) / 20, 0, 0.3)
            double margin = Math.max(0.0, Math.min(HIT_MARGIN, (t - 2) / 20.0));
            class_238 box = new class_238(wx - WIND_HALF, wy - WIND_DROP, wz - WIND_HALF, wx + WIND_HALF, wy - WIND_DROP + WIND_SIZE, wz + WIND_HALF).method_1014(margin);
            class_243 from = pearl[t - 1], to = pearl[t];
            if (box.method_1006(from)) continue; // a move starting inside never registers
            if (box.method_992(from, to).isEmpty()) continue;
            class_243 c = box.method_1005();
            class_243 d = to.method_1020(from);
            double len2 = d.method_1027();
            double k = len2 < 1.0E-9 ? 0 : Math.max(0, Math.min(1, c.method_1020(from).method_1026(d) / len2));
            double dist = from.method_1019(d.method_1021(k)).method_1022(c);
            double room = Math.max(0.0, WIND_HALF + margin - dist) / (WIND_HALF + HIT_MARGIN);
            return 0.01 + 0.5 * room + 0.4 * (end + 1 - t) / (double)(end + 1);
        }
        return 0;
    }

    /** True if the charge would hit a block before the pearl reaches it. */
    private boolean windBlocked(class_243 windEye, class_243 u, int delay, class_243[] pearl, int last) {
        int end = Math.min(last, pearl.length - 1);
        int catchTick = end;
        for (int t = delay + 1; t <= end; t++) {
            if (score(windEye, u, java.util.Arrays.copyOf(pearl, t + 1), delay, t) > 0) { catchTick = t; break; }
        }
        int flight = Math.max(1, catchTick - 1 - delay);
        return this.blocked(windEye, windEye.method_1019(u.method_1021(flight)));
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
