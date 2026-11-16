package hook.feature;

import hook.utils.EnchantmentUtil;
import hook.utils.InventoryUtil;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1747;
import net.minecraft.class_1764;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1893;
import net.minecraft.class_2241;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2828;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4770;
import net.minecraft.class_638;
import net.minecraft.class_746;

/**
 * Shared planning for Xbow Cart and Bow Cart.
 *
 * Vanilla facts this relies on (1.21.11):
 * - a TNT minecart explodes instantly only when an arrow that is ON FIRE hits it (MinecartTNT.hurtServer);
 * - an arrow applies block effects (fire / lava) along its move BEFORE it checks entity hits in the same step
 *   (AbstractArrow.stepMoveAndHit), so it only has to fly through a fire block somewhere before the cart;
 * - a minecart sits on a flat rail at y + 0.0625 with a 0.98 x 0.7 box.
 */
final class CartPlanner {
    private CartPlanner() {}

    static final class Plan {
        class_2338 rail;          // where the cart goes
        class_3965 railClick;     // click that places the rail, null if a rail is already there
        class_3965 cartClick;     // click on the rail with the TNT minecart
        class_2338 fire;          // cell that gets fire, null if not needed
        int igniterSlot = -1;
        boolean igniterIsBucket;
        float[] aim;              // crossbow aim (Xbow Cart only)
        class_3965 supportClick;  // block placed under the fire when there is no ground in the arrow's way
        int supportSlot = -1;
        boolean slope;            // ramp: a rail on the block next to "rail" (one up) makes "rail" ascend, cart sits 0.5 higher
        class_2338 upper;         // the rail on top of the neighbour block
        class_3965 upperClick;
        class_2338 rampBlock;     // block placed from the hotbar to carry the upper rail (none there)
        class_3965 rampBlockClick;
        int rampBlockSlot = -1;
    }

    static class_310 mc() { return class_310.method_1551(); }

    // ---------------------------------------------------------------- items

    static boolean isRail(class_1799 s) {
        return !s.method_7960() && s.method_7909() instanceof class_1747 bi && bi.method_7711() instanceof class_2241;
    }

    static int hotbar(java.util.function.Predicate<class_1799> test) {
        var inv = mc().field_1724.method_31548();
        for (int i = 0; i < 9; i++) if (test.test(inv.method_5438(i))) return i;
        return -1;
    }

    static int hotbarItem(class_1792 item) { return hotbar(s -> s.method_7909() == item); }

    static int railSlot() {
        int plain = hotbarItem(class_1802.field_8129);
        return plain >= 0 ? plain : hotbar(CartPlanner::isRail);
    }

    static int cartSlot() { return hotbarItem(class_1802.field_8069); }

    /** Flint and steel, then fire charge, then lava bucket. */
    static int[] igniter() {
        int s = hotbarItem(class_1802.field_8884);
        if (s >= 0) return new int[]{s, 0};
        s = hotbarItem(class_1802.field_8814);
        if (s >= 0) return new int[]{s, 0};
        s = hotbarItem(class_1802.field_8187);
        if (s >= 0) return new int[]{s, 1};
        return null;
    }

    static boolean loadedCrossbow(class_1799 s) {
        return !s.method_7960() && s.method_7909() instanceof class_1764 && class_1764.method_7781(s);
    }

    static boolean hasFlame(class_1799 s) {
        try { return EnchantmentUtil.hasEnchantment(s, class_1893.field_9126); } catch (Throwable t) { return false; }
    }

    // ---------------------------------------------------------------- geometry

    static class_238 cartBox(class_2338 r) { return cartBox(r, false); }

    /** Cart box on a flat rail, or on an ascending rail (MinecartItem puts it 0.5 higher there). */
    static class_238 cartBox(class_2338 r, boolean slope) {
        double x = r.method_10263() + 0.5, y = r.method_10264() + 0.0625 + (slope ? 0.5 : 0.0), z = r.method_10260() + 0.5;
        return new class_238(x - 0.49, y, z - 0.49, x + 0.49, y + 0.7, z + 0.49);
    }

    static class_243 dir(float yaw, float pitch) {
        double y = Math.toRadians(yaw), p = Math.toRadians(pitch);
        return new class_243(-Math.sin(y) * Math.cos(p), -Math.sin(p), Math.cos(y) * Math.cos(p));
    }

    static float[] aimAt(class_243 from, class_243 to) {
        double dx = to.field_1352 - from.field_1352, dy = to.field_1351 - from.field_1351, dz = to.field_1350 - from.field_1350;
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        return new float[]{yaw, pitch};
    }

    static boolean sturdyTop(class_2338 pos) {
        class_638 w = mc().field_1687;
        return w.method_8320(pos).method_26206(w, pos, class_2350.field_11036);
    }

    static boolean free(class_2338 pos) {
        class_2680 s = mc().field_1687.method_8320(pos);
        return (s.method_26215() || s.method_45474()) && s.method_26227().method_15769();
    }

    static boolean isFire(class_2338 pos) {
        return mc().field_1687.method_8320(pos).method_26204() instanceof class_4770;
    }

    static boolean isRailBlock(class_2338 pos) {
        return mc().field_1687.method_8320(pos).method_26204() instanceof class_2241;
    }

    /** Result of an arrow simulation: did it reach the cart, which block cells did its box cross before that, where did it stop. */
    static final class Trace {
        boolean hitsCart;
        final List<class_2338> cells = new ArrayList<>();
        class_3965 block; // block it hits when no cart is given
        final List<class_243[]> segs = new ArrayList<>();
        final java.util.Map<class_2338, Double> depth = new java.util.HashMap<>();
    }

    /** How deep the arrow box gets into the fire cell; below this it may just graze past it. */
    static final double FIRE_DEPTH = 0.1;

    /** Simulates an arrow (pos = entity position, i.e. bottom centre of its 0.5 box). cart may be null. */
    static double INSET = 0.06;

    static Trace trace(class_243 pos, class_243 vel, class_238 cart) {
        class_638 world = mc().field_1687;
        class_1297 shooter = mc().field_1724;
        Trace t = new Trace();
        // arrows have no entity-hit margin in their first ticks (ProjectileUtil margin), so the box is exact.
        // "safe" (shrunk) decides whether it surely hits; the real box decides where the arrow actually stops.
        class_238 safe = cart == null ? null : cart.method_1014(-INSET);
        java.util.LinkedHashMap<class_2338, Double> cells = new java.util.LinkedHashMap<>();
        for (int i = 0; i < 120; i++) {
            class_243 next = pos.method_1019(vel);
            class_3965 bh = world.method_17742(new class_3959(pos, next, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, shooter));
            boolean hitBlock = bh != null && bh.method_17783() != class_239.class_240.field_1333;
            class_243 end = hitBlock ? bh.method_17784() : next;
            Optional<class_243> real = cart == null ? Optional.empty() : cart.method_992(pos, end);
            class_243 stop = real.orElse(end);
            touch(cells, pos, stop);
            t.segs.add(new class_243[]{pos, stop});
            if (real.isPresent()) {
                t.hitsCart = safe.method_992(pos, end).isPresent();
                t.cells.addAll(cells.keySet());
                t.depth.putAll(cells);
                return t;
            }
            if (hitBlock) {
                t.block = bh;
                t.cells.addAll(cells.keySet());
                t.depth.putAll(cells);
                return t;
            }
            pos = next;
            vel = vel.method_1021(0.99).method_1031(0, -0.05, 0);
            if (pos.field_1351 < world.method_31607() - 8) break;
        }
        return t;
    }

    /**
     * Every block cell the arrow's 0.5 x 0.5 box passes through between a and b, with how deep it gets into the cell
     * (smallest overlap over the three axes, best sample). The server checks blocks with exactly this box.
     */
    private static void touch(java.util.Map<class_2338, Double> cells, class_243 a, class_243 b) {
        double len = a.method_1022(b);
        int n = Math.max(1, (int) Math.ceil(len / 0.05));
        for (int i = 0; i <= n; i++) {
            double f = i / (double) n;
            double x = a.field_1352 + (b.field_1352 - a.field_1352) * f;
            double y = a.field_1351 + (b.field_1351 - a.field_1351) * f;
            double z = a.field_1350 + (b.field_1350 - a.field_1350) * f;
            double x0 = x - 0.25, x1 = x + 0.25, y0 = y, y1 = y + 0.5, z0 = z - 0.25, z1 = z + 0.25;
            for (int cx = (int) Math.floor(x0); cx <= (int) Math.floor(x1); cx++)
                for (int cy = (int) Math.floor(y0); cy <= (int) Math.floor(y1); cy++)
                    for (int cz = (int) Math.floor(z0); cz <= (int) Math.floor(z1); cz++) {
                        double d = Math.min(Math.min(Math.min(x1, cx + 1) - Math.max(x0, cx), Math.min(y1, cy + 1) - Math.max(y0, cy)), Math.min(z1, cz + 1) - Math.max(z0, cz));
                        if (d <= 0) continue;
                        cells.merge(new class_2338(cx, cy, cz), d, Math::max);
                    }
        }
    }

    // ---------------------------------------------------------------- fire

    /**
     * Picks the fire cell on the arrow's way (latest one = closest to the cart). If no cell on the way has ground under
     * it, a block from your hotbar is placed as ground first (arrow path checked so it never hits that block).
     */
    static boolean chooseFire(Plan plan, Trace trace, boolean arrowBurning) {
        plan.fire = null;
        plan.supportClick = null;
        plan.supportSlot = -1;
        if (arrowBurning) return true;
        for (class_2338 c : trace.cells) if (isFire(c)) return true; // already burning on the way
        int[] ig = igniter();
        if (ig == null) return false;
        class_746 p = mc().field_1724;
        class_243 eye = p.method_33571();
        double reach = p.method_55754();
        class_238 me = p.method_5829().method_1014(-0.001); // only your own hitbox may not be in the fire
        class_2338 best = null;
        double bestDepth = 0;
        for (double minDepth : new double[]{FIRE_DEPTH, 0.03})
        for (class_2338 c : trace.cells) {
            if (best != null && minDepth < FIRE_DEPTH) break; // the deep tier already found one
            double depth = trace.depth.getOrDefault(c, 0.0);
            if (depth < minDepth || depth + 1e-6 < bestDepth) continue;
            if (!fireCellOk(plan, c, me, eye, reach)) continue;
            if (!sturdyTop(c.method_10074())) continue;
            if (ig[1] == 1 && !bucketReaches(c)) continue;
            best = c; // deepest contact wins, on a tie the later one (closest to the cart)
            bestDepth = depth;
        }
        if (best == null && ig[1] == 0) {
            int support = supportSlot();
            if (support >= 0) {
                for (class_2338 c : trace.cells) {
                    double depth = trace.depth.getOrDefault(c, 0.0);
                    if (depth < FIRE_DEPTH || depth + 1e-6 < bestDepth) continue;
                    if (!fireCellOk(plan, c, me, eye, reach)) continue;
                    class_2338 under = c.method_10074();
                    if (under.equals(plan.rail) || under.equals(plan.rail.method_10074()) || under.equals(plan.upper) || under.equals(plan.rampBlock) || !free(under)) continue;
                    if (me.method_994(new class_238(under))) continue;
                    if (pathEnters(trace, under)) continue; // the arrow would hit the support block
                    class_3965 click = supportClick(under, eye, reach);
                    if (click == null) continue;
                    best = c;
                    bestDepth = depth;
                    plan.supportClick = click;
                    plan.supportSlot = support;
                }
            }
        }
        if (best == null) return false;
        plan.fire = best;
        plan.igniterSlot = ig[0];
        plan.igniterIsBucket = ig[1] == 1;
        return true;
    }

    private static boolean fireCellOk(Plan plan, class_2338 c, class_238 me, class_243 eye, double reach) {
        if (c.equals(plan.rail) || c.equals(plan.upper) || c.equals(plan.rampBlock)) return false;
        if (!mc().field_1687.method_8320(c).method_26215()) return false;
        if (me.method_994(new class_238(c))) return false; // never set yourself on fire
        class_243 click = new class_243(c.method_10263() + 0.5, c.method_10264(), c.method_10260() + 0.5);
        return eye.method_1022(click) <= reach - 0.05;
    }

    /** True if the arrow's position line goes through this block cell (it would collide with a block there). */
    static boolean pathEnters(Trace t, class_2338 cell) {
        class_238 box = new class_238(cell).method_1014(0.02);
        for (class_243[] s : t.segs) if (box.method_992(s[0], s[1]).isPresent() || box.method_1006(s[0])) return true;
        return false;
    }

    /** A full solid block from the hotbar that can serve as ground (no TNT, no falling or container blocks). */
    static int supportSlot() {
        return hotbar(s -> {
            if (s.method_7960() || !(s.method_7909() instanceof class_1747 bi)) return false;
            var block = bi.method_7711();
            if (block instanceof net.minecraft.class_2530 || block instanceof net.minecraft.class_2346 || block instanceof net.minecraft.class_2237 || block instanceof class_2241) return false;
            try {
                return block.method_9564().method_26234(mc().field_1687, class_2338.field_10980);
            } catch (Throwable t) {
                return false;
            }
        });
    }

    /** Click that places a block into the given cell: on top of the block below, else against a solid side. */
    private static class_3965 supportClick(class_2338 cell, class_243 eye, double reach) {
        class_2350[] dirs = {class_2350.field_11033, class_2350.field_11043, class_2350.field_11035, class_2350.field_11039, class_2350.field_11034};
        for (class_2350 d : dirs) {
            class_2338 against = cell.method_10093(d);
            class_2350 face = d.method_10153();
            var w = mc().field_1687;
            if (!w.method_8320(against).method_26206(w, against, face)) continue;
            class_243 hit = new class_243(cell.method_10263() + 0.5 + d.method_10148() * 0.5, cell.method_10264() + 0.5 + d.method_10164() * 0.5, cell.method_10260() + 0.5 + d.method_10165() * 0.5);
            if (eye.method_1022(hit) > reach - 0.05) continue;
            return new class_3965(hit, face, against, false);
        }
        return null;
    }

    private static boolean bucketReaches(class_2338 cell) {
        class_746 p = mc().field_1724;
        class_243 eye = p.method_33571();
        class_243 top = new class_243(cell.method_10263() + 0.5, cell.method_10264(), cell.method_10260() + 0.5);
        class_243 far = eye.method_1019(top.method_1020(eye).method_1029().method_1021(p.method_55754()));
        class_3965 hit = mc().field_1687.method_17742(new class_3959(eye, far, class_3959.class_3960.field_17559, class_3959.class_242.field_1348, p));
        return hit != null && hit.method_17783() == class_239.class_240.field_1332 && hit.method_17777().equals(cell.method_10074()) && hit.method_17780() == class_2350.field_11036;
    }

    // ---------------------------------------------------------------- rail spot

    /** Rail + cart clicks for a cart at r. railClick may be supplied (crosshair hit) or is derived (top of the block below). */
    static boolean prepareRail(Plan plan, class_2338 r, class_3965 railClick) {
        class_746 p = mc().field_1724;
        class_243 eye = p.method_33571();
        double reach = p.method_55754();
        plan.rail = r;
        if (isRailBlock(r)) {
            plan.railClick = null;
        } else {
            if (!free(r) || !sturdyTop(r.method_10074())) return false;
            if (p.method_5829().method_994(new class_238(r))) return false;
            if (railSlot() < 0) return false;
            if (railClick == null) {
                class_243 top = new class_243(r.method_10263() + 0.5, r.method_10264(), r.method_10260() + 0.5);
                railClick = new class_3965(top, class_2350.field_11036, r.method_10074(), false);
            }
            if (eye.method_1022(railClick.method_17784()) > reach - 0.05) return false;
            plan.railClick = railClick;
        }
        if (cartSlot() < 0) return false;
        class_243 cartPoint = new class_243(r.method_10263() + 0.5, r.method_10264() + 0.0625, r.method_10260() + 0.5);
        if (eye.method_1022(cartPoint) > reach - 0.05) return false;
        plan.cartClick = new class_3965(cartPoint, class_2350.field_11036, r, false);
        return true;
    }

    static final class_2350[] HORIZONTAL = {class_2350.field_11043, class_2350.field_11035, class_2350.field_11039, class_2350.field_11034};

    /**
     * Ramp: "low" gets a rail, the block next to it in {@code dir} gets a rail on top first, so the low rail turns into
     * an ascending rail and the cart stands half a block higher - right where an arrow aimed at a raised block flies
     * (it would hit the block's side 0.1 below a cart standing on top of it).
     */
    static boolean prepareSlope(Plan plan, class_2338 low, class_2350 dir) { return prepareSlope(plan, low, dir, false); }

    static boolean prepareSlope(Plan plan, class_2338 low, class_2350 dir, boolean placeBlock) {
        class_746 p = mc().field_1724;
        class_243 eye = p.method_33571();
        double reach = p.method_55754() - 0.05;
        class_2338 block = low.method_10093(dir);
        class_2338 upper = block.method_10084();
        if (railSlot() < 0 || cartSlot() < 0) return false;
        if (isRailBlock(low) || isRailBlock(upper)) return false;
        if (!free(low) || !sturdyTop(low.method_10074()) || !free(upper)) return false;
        class_3965 blockClick = null;
        int blockSlot = -1;
        if (placeBlock) {
            // no block there: put one from the hotbar (on the ground) and the upper rail on it
            if (!free(block) || !sturdyTop(block.method_10074())) return false;
            blockSlot = supportSlot();
            if (blockSlot < 0) return false;
            if (p.method_5829().method_994(new class_238(block))) return false;
            blockClick = new class_3965(new class_243(block.method_10263() + 0.5, block.method_10264(), block.method_10260() + 0.5), class_2350.field_11036, block.method_10074(), false);
            if (eye.method_1022(blockClick.method_17784()) > reach) return false;
        } else {
            if (!sturdyTop(block)) return false;
            if (!mc().field_1687.method_8320(block).method_26234(mc().field_1687, block)) return false; // a real full block
        }
        // no other rails around that could steal the connection
        for (class_2338 c : new class_2338[]{low, upper})
            for (class_2350 d : HORIZONTAL)
                for (int dy = -1; dy <= 1; dy++) {
                    class_2338 n = c.method_10093(d).method_10086(dy);
                    if (!n.equals(low) && !n.equals(upper) && isRailBlock(n)) return false;
                }
        class_238 me = p.method_5829();
        if (me.method_994(new class_238(low)) || me.method_994(new class_238(upper))) return false;
        class_3965 upClick = new class_3965(new class_243(upper.method_10263() + 0.5, upper.method_10264(), upper.method_10260() + 0.5), class_2350.field_11036, block, false);
        class_3965 lowClick = new class_3965(new class_243(low.method_10263() + 0.5, low.method_10264(), low.method_10260() + 0.5), class_2350.field_11036, low.method_10074(), false);
        if (eye.method_1022(upClick.method_17784()) > reach || eye.method_1022(lowClick.method_17784()) > reach) return false;
        class_243 cartPoint = new class_243(low.method_10263() + 0.5, low.method_10264() + 0.5, low.method_10260() + 0.5);
        if (eye.method_1022(cartPoint) > reach) return false;
        plan.rail = low;
        plan.railClick = lowClick;
        plan.slope = true;
        plan.upper = upper;
        plan.upperClick = upClick;
        plan.cartClick = new class_3965(cartPoint, class_2350.field_11036, low, false);
        plan.rampBlock = placeBlock ? block : null;
        plan.rampBlockClick = blockClick;
        plan.rampBlockSlot = blockSlot;
        return true;
    }

    // ---------------------------------------------------------------- doing it

    /**
     * Right-click on a block with the main hand, exactly like vanilla (client prediction + use packet with the right
     * prediction sequence), but without going through MultiPlayerGameMode.useItemOn: other mods hook that method and
     * can swallow the click (e.g. AntiBreak blocks every flint and steel, its 64 durability is below its limit).
     */
    static net.minecraft.class_1269 useOn(class_3965 hit) {
        class_746 p = mc().field_1724;
        ((hook.mixin.ClientPlayerInteractionManagerAccessor) mc().field_1761).syncSlot();
        net.minecraft.class_1269 result = net.minecraft.class_1269.field_5811;
        try (net.minecraft.class_7202 h = ((hook.mixin.ClientLevelPredictionAccessor) mc().field_1687).hook$predictionHandler().method_41937()) {
            int seq = h.method_41942();
            try {
                class_1799 stack = p.method_6047();
                if (!stack.method_7960()) {
                    int count = stack.method_7947();
                    result = stack.method_7981(new net.minecraft.class_1838(p, class_1268.field_5808, hit));
                    if (p.method_56992()) stack.method_7939(count);
                }
            } catch (Throwable ignored) {
                // prediction only; the server decides
            }
            mc().method_1562().method_52787(new net.minecraft.class_2885(class_1268.field_5808, hit, seq));
            if (result.method_23665()) p.method_6104(class_1268.field_5808); // arm swing like a real placement
        }
        return result;
    }

    /**
     * When set, look() turns the real head like a mouse does (whole mouse counts, no extra packet: the normal movement
     * packet of this tick carries it, after the click - exactly the vanilla order). Otherwise a rotation packet only.
     */
    static boolean REAL_ROTATE = false;

    static void look(class_243 at) {
        class_746 p = mc().field_1724;
        float[] a = aimAt(p.method_33571(), at);
        if (REAL_ROTATE) { CartHuman.turnTo(p, a); return; }
        mc().method_1562().method_52787(new class_2828.class_2831(a[0], a[1], p.method_24828(), p.field_5976));
    }

    /** A burning cell on the arrow's way that it really goes into. */
    static boolean passesFire(Trace t) {
        for (class_2338 c : t.cells) if (t.depth.getOrDefault(c, 0.0) >= 0.05 && isFire(c)) return true;
        return false;
    }

    /**
     * Dense search over a cart box for a crossbow aim (arrow from {@code start}, speed 3.15) that the given test
     * accepts; middle of the box first. Returns {yaw, pitch} or null.
     */
    static float[] searchAim(class_243 start, class_238 box, java.util.function.Predicate<Trace> accept) {
        double cx = (box.field_1323 + box.field_1320) / 2, cz = (box.field_1321 + box.field_1324) / 2;
        double h = box.field_1325 - box.field_1322;
        try {
            for (double inset : new double[]{0.15, 0.06}) {
                INSET = inset;
                for (int iy = 0; iy <= 12; iy++) {
                    double fy = h * (0.5 + ((iy % 2 == 0) ? 1 : -1) * ((iy + 1) / 2) * 0.075);
                    if (fy < 0.03 || fy > h - 0.03) continue;
                    for (int ix = 0; ix <= 12; ix++) for (int iz = 0; iz <= 12; iz++) {
                        double ox = ((ix % 2 == 0) ? 1 : -1) * ((ix + 1) / 2) * 0.07;
                        double oz = ((iz % 2 == 0) ? 1 : -1) * ((iz + 1) / 2) * 0.07;
                        float[] a = aimAt(start, new class_243(cx + ox, box.field_1322 + fy, cz + oz));
                        Trace tr = trace(start, dir(a[0], a[1]).method_1021(3.15), box);
                        if (tr.hitsCart && accept.test(tr)) return a;
                    }
                }
            }
        } finally {
            INSET = 0.06;
        }
        return null;
    }

    static void placeRail(Plan plan) {
        if (plan.rampBlockClick != null) {
            InventoryUtil.swap(plan.rampBlockSlot);
            look(plan.rampBlockClick.method_17784());
            useOn(plan.rampBlockClick);
        }
        if (plan.upperClick != null) {
            InventoryUtil.swap(railSlot());
            look(plan.upperClick.method_17784());
            useOn(plan.upperClick);
        }
        if (plan.railClick == null) return;
        InventoryUtil.swap(railSlot());
        look(plan.railClick.method_17784());
        useOn(plan.railClick);
    }

    static void placeCart(Plan plan) {
        InventoryUtil.swap(cartSlot());
        look(plan.cartClick.method_17784());
        useOn(plan.cartClick);
    }

    static void placeFire(Plan plan) {
        if (plan.fire == null) return;
        class_746 p = mc().field_1724;
        if (plan.supportClick != null) {
            InventoryUtil.swap(plan.supportSlot);
            look(plan.supportClick.method_17784());
            useOn(plan.supportClick);
        }
        InventoryUtil.swap(plan.igniterSlot);
        class_2338 below = plan.fire.method_10074();
        class_243 top = new class_243(plan.fire.method_10263() + 0.5, plan.fire.method_10264(), plan.fire.method_10260() + 0.5);
        if (plan.igniterIsBucket) {
            float[] a = aimAt(p.method_33571(), top);
            if (REAL_ROTATE) {
                CartHuman.turnTo(p, a);
                if (mc().field_1761.method_2919((class_1657) p, class_1268.field_5808).method_23665()) p.method_6104(class_1268.field_5808);
                return;
            }
            float yaw = p.method_36454(), pitch = p.method_36455();
            p.method_36456(a[0]);
            p.method_36457(a[1]);
            if (mc().field_1761.method_2919((class_1657) p, class_1268.field_5808).method_23665()) p.method_6104(class_1268.field_5808);
            p.method_36456(yaw);
            p.method_36457(pitch);
        } else {
            look(top);
            String pre = CartTest.ENABLED ? (" cell=" + mc().field_1687.method_8320(plan.fire) + " below=" + mc().field_1687.method_8320(below) + " clicked=" + below.method_23854() + " canPlace=" + class_4770.method_30032(mc().field_1687, plan.fire, p.method_5735()) + " survive=" + class_4770.method_24416(mc().field_1687, plan.fire).method_26184(mc().field_1687, plan.fire) + " sturdy=" + sturdyTop(below) + " state=" + class_4770.method_24416(mc().field_1687, plan.fire)) : "";
            var r = useOn(new class_3965(top, class_2350.field_11036, below, false));
            if (CartTest.ENABLED) CartTest.fireResult = r + pre + " clientFire=" + isFire(plan.fire) + " held=" + p.method_6047().method_7909();
        }
    }
}
