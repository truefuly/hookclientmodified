package hook.feature;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_1750;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_746;

/**
 * Finds how to place a block like a player would: click a visible face of a neighbouring block while really looking
 * at that spot. Every candidate is simulated with Minecraft's own placement logic (with the rotation the player will
 * have), so stairs, logs, slabs, doors, observers... end up facing exactly like in the schematic.
 */
final class SbPlacer {
    private SbPlacer() {
    }

    record Solution(class_3965 hit, float yaw, float pitch, double score) {
    }

    static float[] lookAt(class_243 eye, class_243 p) {
        double dx = p.field_1352 - eye.field_1352, dy = p.field_1351 - eye.field_1351, dz = p.field_1350 - eye.field_1350;
        double h = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, h));
        return new float[]{net.minecraft.class_3532.method_15393(yaw), Math.max(-90.0f, Math.min(90.0f, pitch))};
    }

    static double angle(float y1, float p1, float y2, float p2) {
        float dy = Math.abs(net.minecraft.class_3532.method_15393(y1 - y2));
        return dy + Math.abs(p1 - p2);
    }

    /** Ray from the eye to a point: what block and face it really hits first. */
    static class_3965 ray(class_638 w, class_746 pl, class_243 eye, class_243 to) {
        class_243 d = to.method_1020(eye);
        double len = Math.sqrt(d.field_1352 * d.field_1352 + d.field_1351 * d.field_1351 + d.field_1350 * d.field_1350);
        if (len < 1.0E-4) return null;
        class_243 end = to.method_1031(d.field_1352 / len * 0.05, d.field_1351 / len * 0.05, d.field_1350 / len * 0.05);
        class_3965 r = w.method_17742(new class_3959(eye, end, class_3959.class_3960.field_17559, class_3959.class_242.field_1348, pl));
        return r != null && r.method_17783() == class_239.class_240.field_1332 ? r : null;
    }

    /**
     * Best way to place {@code item} at {@code target} from {@code eye} so the result matches {@code want} (null = any
     * state, e.g. scaffolding), or null if it can't be done from there.
     */
    /**
     * Turning the head somewhere else than where you click is what anticheats look for, so it is off: placements that
     * need it are done from another spot where the natural look gives the right state.
     */
    static boolean forced = false;

    /** Where the crosshair lands when looking with that rotation (like Entity#raycast). */
    static class_3965 rayRot(class_638 w, class_746 pl, class_243 eye, float yaw, float pitch, double reach) {
        double f = Math.toRadians(pitch), g = Math.toRadians(-yaw);
        class_243 d = new class_243(Math.sin(g) * Math.cos(f), -Math.sin(f), Math.cos(g) * Math.cos(f));
        class_243 end = eye.method_1031(d.field_1352 * reach, d.field_1351 * reach, d.field_1350 * reach);
        class_3965 r = w.method_17742(new class_3959(eye, end, class_3959.class_3960.field_17559, class_3959.class_242.field_1348, pl));
        return r != null && r.method_17783() == class_239.class_240.field_1332 ? r : null;
    }

    /**
     * The head never lands exactly on the spot (mouse steps, smoothing): the crosshair must stay on the same face and
     * the click must still give the same result when the look is a bit off.
     */
    private static boolean robust(class_746 pl, class_638 w, class_1747 item, class_3965 r, float[] rot, class_2338 target, class_2680 cur, class_2680 want,
            class_243 eye, double reach) {
        float e = 1.5f;
        float[][] d = {{e, 0}, {-e, 0}, {0, e}, {0, -e}, {e, e}, {-e, -e}, {e, -e}, {-e, e}};
        for (float[] o : d) {
            float[] rr = {rot[0] + o[0], Math.max(-90f, Math.min(90f, rot[1] + o[1]))};
            class_3965 h = rayRot(w, pl, eye, rr[0], rr[1], reach + 0.5);
            if (h == null || !h.method_17777().equals(r.method_17777()) || h.method_17780() != r.method_17780()) return false;
            if (eye.method_1025(h.method_17784()) > reach * reach) return false;
            class_2680 st = simulate(pl, w, item, h, rr, target, cur);
            if (st == null || want != null && !SbWorld.matchesShape(want, st)) return false;
        }
        return true;
    }

    static Solution find(class_310 mc, class_2338 target, class_2680 want, class_1747 item, class_243 eye, double reach) {
        class_638 w = mc.field_1687;
        class_746 pl = mc.field_1724;
        if (w == null || pl == null) return null;
        class_2680 cur = w.method_8320(target);
        Solution best = null;
        List<class_3965> hits = new ArrayList<>();
        float curYaw = pl.method_36454(), curPitch = pl.method_36455();
        double cx = target.method_10263() + 0.5, cy = target.method_10264() + 0.5, cz = target.method_10260() + 0.5;
        for (class_2350 dir : class_2350.values()) {
            class_2338 nb = target.method_10093(dir);
            class_2680 ns = w.method_8320(nb);
            if (ns.method_26215() || ns.method_26218(w, nb).method_1110()) continue; // nothing to click on
            if (ns.method_45474()) continue; // grass, snow layers, liquids: get replaced instead of clicked
            class_2350 side = dir.method_10153();
            double fx = cx + dir.method_10148() * 0.5, fy = cy + dir.method_10164() * 0.5, fz = cz + dir.method_10165() * 0.5;
            // a grid of spots over the face: each one is a different, real way of looking at it
            double[] g = {-0.36, -0.18, 0.0, 0.18, 0.36};
            for (double u : g) for (double v : g) {
                double ox, oy, oz;
                if (dir.method_10164() != 0) { ox = u; oy = 0; oz = v; }
                else if (dir.method_10148() != 0) { ox = 0; oy = v; oz = u; }
                else { ox = u; oy = v; oz = 0; }
                class_243 point = new class_243(fx + ox, fy + oy, fz + oz);
                // the face must point towards the eye
                double nx = side.method_10148(), ny = side.method_10164(), nz = side.method_10165();
                if ((eye.field_1352 - point.field_1352) * nx + (eye.field_1351 - point.field_1351) * ny + (eye.field_1350 - point.field_1350) * nz <= 0.02) continue;
                if (eye.method_1025(point) > reach * reach) continue;
                float[] rot = lookAt(eye, point);
                double score = angle(rot[0], rot[1], curYaw, curPitch) + eye.method_1025(point) * 2.0 + (Math.abs(u) + Math.abs(v)) * 4.0;
                if (best != null && score >= best.score) continue;
                class_3965 r = ray(w, pl, eye, point);
                if (r == null || !r.method_17777().equals(nb) || r.method_17780() != side) continue;
                hits.add(r);
                class_2680 st = simulate(pl, w, item, r, rot, target, cur);
                if (st == null) continue;
                if (want != null && !SbWorld.matchesShape(want, st)) continue;
                if (!robust(pl, w, item, r, rot, target, cur, want, eye, reach)) continue;
                best = new Solution(r, rot[0], rot[1], score);
            }
        }
        if (best != null || want == null || !forced) return best;
        // some states can't come from looking at the clicked spot (an observer facing up on the ground, stairs
        // facing away from every free side...): turn the head into the needed direction first, then click
        float[][] rots = {{curYaw, -90f}, {curYaw, 90f}, {0f, 0f}, {90f, 0f}, {180f, 0f}, {-90f, 0f},
                {0f, 60f}, {90f, 60f}, {180f, 60f}, {-90f, 60f}, {0f, -60f}, {90f, -60f}, {180f, -60f}, {-90f, -60f}};
        for (class_3965 r : hits) {
            for (float[] rot : rots) {
                class_2680 st = simulate(pl, w, item, r, rot, target, cur);
                if (st == null || !SbWorld.matchesShape(want, st)) continue;
                double score = 400.0 + angle(rot[0], rot[1], curYaw, curPitch) + eye.method_1025(r.method_17784()) * 2.0;
                if (best == null || score < best.score) best = new Solution(r, rot[0], rot[1], score);
            }
        }
        return best;
    }

    /**
     * Clicking the block that is already there with the same item: a single slab becomes a double one, 1 candle
     * becomes 2... Looks at spots on the block's real outline (a bottom slab's top is half way up).
     */
    static Solution findGrow(class_310 mc, class_2338 target, class_2680 want, class_1747 item, class_243 eye, double reach) {
        class_638 w = mc.field_1687;
        class_746 pl = mc.field_1724;
        if (w == null || pl == null) return null;
        class_2680 cur = w.method_8320(target);
        var shape = cur.method_26218(w, target);
        if (shape.method_1110()) return null;
        class_238 bb = shape.method_1107().method_996(target);
        float curYaw = pl.method_36454(), curPitch = pl.method_36455();
        // candles, sea pickles and turtle eggs only take another one while NOT sneaking (it is clicked standing up)
        boolean sneaking = pl.method_5715();
        if (sneaking) pl.method_5660(false);
        try {
        Solution best = null;
        double[] g = {-0.3, -0.15, 0.0, 0.15, 0.3};
        for (class_2350 side : class_2350.values()) {
            double fx = side.method_10148() > 0 ? bb.field_1320 : side.method_10148() < 0 ? bb.field_1323 : (bb.field_1323 + bb.field_1320) / 2;
            double fy = side.method_10164() > 0 ? bb.field_1325 : side.method_10164() < 0 ? bb.field_1322 : (bb.field_1322 + bb.field_1325) / 2;
            double fz = side.method_10165() > 0 ? bb.field_1324 : side.method_10165() < 0 ? bb.field_1321 : (bb.field_1321 + bb.field_1324) / 2;
            double sx = (bb.field_1320 - bb.field_1323), sy = (bb.field_1325 - bb.field_1322), sz = (bb.field_1324 - bb.field_1321);
            for (double u : g) for (double v : g) {
                double ox, oy, oz;
                if (side.method_10164() != 0) { ox = u * sx; oy = 0; oz = v * sz; }
                else if (side.method_10148() != 0) { ox = 0; oy = v * sy; oz = u * sz; }
                else { ox = u * sx; oy = v * sy; oz = 0; }
                class_243 point = new class_243(fx + ox, fy + oy, fz + oz);
                if ((eye.field_1352 - point.field_1352) * side.method_10148() + (eye.field_1351 - point.field_1351) * side.method_10164() + (eye.field_1350 - point.field_1350) * side.method_10165() <= 0.02) continue;
                if (eye.method_1025(point) > reach * reach) continue;
                float[] rot = lookAt(eye, point);
                double score = angle(rot[0], rot[1], curYaw, curPitch) + eye.method_1025(point) * 2.0 + (Math.abs(u) + Math.abs(v)) * 4.0;
                if (best != null && score >= best.score) continue;
                class_3965 r = ray(w, pl, eye, point);
                if (r == null || !r.method_17777().equals(target) || r.method_17780() != side) continue;
                class_2680 st = simulate(pl, w, item, r, rot, target, cur);
                if (st == null || st.method_26204() != want.method_26204() || st == cur) continue;
                if (!SbWorld.matches(want, st) && !SbFix.grows(want, st)) continue;
                if (!robust(pl, w, item, r, rot, target, cur, null, eye, reach)) continue;
                best = new Solution(r, rot[0], rot[1], score);
            }
        }
        return best;
        } finally {
            if (sneaking) pl.method_5660(true);
        }
    }

    private static java.lang.reflect.Method getPlacementState;

    /**
     * The state the item itself would place: torches, signs, banners, heads... become their wall version when a wall
     * is clicked, which the block alone doesn't know.
     */
    private static class_2680 placementState(class_1747 item, class_1750 ctx) throws Exception {
        if (getPlacementState == null) {
            java.lang.reflect.Method m;
            try { m = class_1747.class.getDeclaredMethod("method_7707", class_1750.class); }
            catch (NoSuchMethodException e) { m = class_1747.class.getDeclaredMethod("getPlacementState", class_1750.class); }
            m.setAccessible(true);
            getPlacementState = m;
        }
        return (class_2680) getPlacementState.invoke(item, ctx);
    }

    /** What block state placing would produce with that click and rotation (null = not placeable). */
    static class_2680 simulate(class_746 pl, class_638 w, class_1747 item, class_3965 hit, float[] rot, class_2338 target, class_2680 cur) {
        return simulate(pl, w, item, hit, rot, target, cur, false);
    }

    /** {@code planning}: the player will stand somewhere else by then, so overlapping the target is fine. */
    static class_2680 simulate(class_746 pl, class_638 w, class_1747 item, class_3965 hit, float[] rot, class_2338 target, class_2680 cur, boolean planning) {
        float oy = pl.method_36454(), op = pl.method_36455();
        try {
            pl.method_36456(rot[0]);
            pl.method_36457(rot[1]);
            class_1750 ctx = new class_1750(pl, class_1268.field_5808, new class_1799(item), hit);
            if (!ctx.method_8037().equals(target) || !ctx.method_7716()) return null;
            class_2680 st = placementState(item, ctx);
            if (st == null || !st.method_26184(w, target)) return null;
            // never place a block into yourself
            if (!st.method_26220(w, target).method_1110() && !planning && pl.method_5829().method_994(new class_238(target))) return null;
            return st;
        } catch (Throwable t) {
            return null;
        } finally {
            pl.method_36456(oy);
            pl.method_36457(op);
        }
    }
}
