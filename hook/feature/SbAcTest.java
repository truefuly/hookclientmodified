package hook.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.class_238;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_746;

/**
 * Self-test only (-Dhook.carttest): watches every packet the builder sends and checks it the way server side
 * anticheats do (Grim, NoCheatPlus, Vulcan, Matrix, Intave style checks), using only what the server knows: the last
 * position and rotation it got. Prints "CARTTEST AC ..." lines and a summary. Does nothing outside the self-test.
 */
final class SbAcTest {
    private SbAcTest() {
    }

    static final boolean ON = System.getProperty("hook.carttest") != null;
    /** Mouse step of the current sensitivity (set by the builder). */
    static float mouseStep = 0.0f;

    // what the server knows
    private static double sx, sy, sz, lastDx, lastDz;
    private static float syaw, spitch, lastDYaw, lastDPitch;
    private static boolean hasPos, hasRot, sneak, sprint;
    private static int moveTick = Integer.MIN_VALUE, movesThisTick, lastRotChangeTick;

    private static final Map<String, Integer> flags = new TreeMap<>();
    private static final Map<String, Integer> printed = new HashMap<>();

    // placing
    private static int places, placeTick = Integer.MIN_VALUE, placesThisTick, stillPlaces;
    private static float lastPlaceDYaw = Float.NaN, grimDeltaX;
    private static boolean grimRotated, placedSinceMove;
    private static final List<Integer> placeTicks = new ArrayList<>();
    private static boolean needSwing;
    private static String needSwingWhat;
    private record Pending(class_2338 pos, class_2350 side, class_243 eye, double feetY, String what, boolean rot, boolean sight) {
    }
    private static final List<Pending> pending = new ArrayList<>();
    private static final Map<Long, Integer> unconfirmed = new java.util.concurrent.ConcurrentHashMap<>();
    private static volatile int lastAck = -1;
    private static double maxReach;

    // breaking
    private static class_2338 digPos;
    private static class_2680 digState;
    private static int digStart, breaks;
    private static double digNeed, digProgress, digMax, breakBalance;

    // inventory
    private static boolean invOpen;
    private static int invClicks;

    // camera smoothness: how much the turn speed changes from one drawn frame to the next
    private static long lastFrameNs;
    private static float lastFy = Float.NaN, lastFp;
    private static double lastV = Double.NaN, sumJerk, maxJerk;
    private static int frames, bigJerks;

    static void frame(float y, float p) {
        long now = System.nanoTime();
        if (!Float.isNaN(lastFy) && now > lastFrameNs) {
            double dt = (now - lastFrameNs) / 1.0e9;
            double v = Math.hypot(y - lastFy, p - lastFp) / dt; // degrees per second
            if (!Double.isNaN(lastV) && dt < 0.1) {
                double j = Math.abs(v - lastV);
                sumJerk += j;
                maxJerk = Math.max(maxJerk, j);
                if (j > 120.0) bigJerks++;
                frames++;
            }
            lastV = dt < 0.1 ? v : Double.NaN;
        }
        lastFrameNs = now;
        lastFy = y;
        lastFp = p;
    }

    // the same ticks drawn at a fixed 60 fps by several smoothing variants (yaw + pitch combined):
    // 0 straight line per tick (old), 1 curve from the past only, 2 Catmull-Rom one tick late,
    // 3 Catmull-Rom one tick late + soft follower, 4 soft follower only
    private static final int V = 5;
    private static final String[] VN = {"line", "hermite", "catmull", "catmull+follow", "follow"};
    private static final double[][] simLast = new double[V][], simLastV = new double[V][];
    private static final double[] simSum = new double[V], simMax = new double[V], simLag = new double[V];
    private static final int[] simBig = new int[V];
    private static final double[][] hist = new double[4][]; // yaw/pitch of the last 4 ticks, [3] newest
    private static final double[][] follow = new double[V][];
    private static int simFrames, simTicks;

    static void simulateTick(float y0, float y1, float m0, float m1, float p0, float p1, float n0, float n1) {
        System.arraycopy(hist, 1, hist, 0, 3);
        hist[3] = new double[]{y1, p1};
        if (++simTicks < 5) return;
        for (int k = 1; k <= 3; k++) {
            double t = k / 3.0;
            double[][] v = new double[V][];
            v[0] = new double[]{y0 + (y1 - y0) * t, p0 + (p1 - p0) * t};
            v[1] = new double[]{herm(y0, y1, m0, m1, t), herm(p0, p1, n0, n1, t)};
            v[2] = new double[2];
            for (int c = 0; c < 2; c++) {
                double a = hist[1][c], b = hist[2][c], ma = (hist[2][c] - hist[0][c]) / 2.0, mb = (hist[3][c] - hist[1][c]) / 2.0;
                v[2][c] = herm(a, b, ma, mb, t);
            }
            double al = 1.0 - Math.exp(-(1.0 / 60.0) / 0.045);
            v[3] = foll(3, v[2], al);
            v[4] = foll(4, v[0], al);
            double[] truth = v[0];
            for (int i = 0; i < V; i++) {
                simLag[i] += Math.hypot(v[i][0] - truth[0], v[i][1] - truth[1]);
                if (simLast[i] != null) {
                    double[] vel = {(v[i][0] - simLast[i][0]) * 60.0, (v[i][1] - simLast[i][1]) * 60.0};
                    if (simLastV[i] != null) {
                        double j = Math.hypot(vel[0] - simLastV[i][0], vel[1] - simLastV[i][1]);
                        simSum[i] += j;
                        simMax[i] = Math.max(simMax[i], j);
                        if (j > 120.0) simBig[i]++;
                    }
                    simLastV[i] = vel;
                }
                simLast[i] = v[i];
            }
            simFrames++;
        }
    }

    private static double[] foll(int i, double[] target, double al) {
        if (follow[i] == null) follow[i] = target.clone();
        follow[i][0] += (target[0] - follow[i][0]) * al;
        follow[i][1] += (target[1] - follow[i][1]) * al;
        return follow[i].clone();
    }

    private static double herm(double a, double b, double m0, double m1, double t) {
        double t2 = t * t, t3 = t2 * t;
        return (2 * t3 - 3 * t2 + 1) * a + (t3 - 2 * t2 + t) * m0 + (-2 * t3 + 3 * t2) * b + (t3 - t2) * m1;
    }

    static String cameraReport() {
        StringBuilder sb = new StringBuilder(cameraReport0()).append(" | 60fps sim (" + simFrames + " frames):");
        int n = Math.max(1, simFrames);
        for (int i = 0; i < V; i++)
            sb.append(String.format(java.util.Locale.ROOT, " [%s mean=%.1f max=%.0f jumps=%.2f%% offFromTicks=%.2fdeg]", VN[i], simSum[i] / n, simMax[i], simBig[i] * 100.0 / n, simLag[i] / n));
        return sb.toString();
    }

    private static String cameraReport0() {
        return String.format(java.util.Locale.ROOT, "frames=%d meanSpeedChange=%.1f deg/s maxSpeedChange=%.0f deg/s jumps>120=%d (%.2f%%)",
                frames, frames == 0 ? 0.0 : sumJerk / frames, maxJerk, bigJerks, frames == 0 ? 0.0 : bigJerks * 100.0 / frames);
    }

    // rotations
    private static int rotPackets, gcdBad, slowAir;
    private static double maxTurn;

    static void reset() {
        flags.clear();
        teleports = 0;
        teleportPending = false;
        beforePlaceD = Float.NaN;
        rotSincePlace = false;
        printed.clear();
        placeTicks.clear();
        pending.clear();
        unconfirmed.clear();
        places = breaks = invClicks = rotPackets = gcdBad = stillPlaces = slowAir = 0;
        breakBalance = 0;
        maxTurn = maxReach = 0;
        hasPos = hasRot = invOpen = needSwing = grimRotated = placedSinceMove = false;
        lastPlaceDYaw = Float.NaN;
        digPos = null;
    }

    private static void flag(String check, String detail) {
        flags.merge(check, 1, Integer::sum);
        if (printed.merge(check, 1, Integer::sum) <= 4) System.out.println("CARTTEST AC FLAG " + check + " " + detail);
    }

    private static int tick() {
        class_746 pl = class_310.method_1551().field_1724;
        return pl == null ? 0 : pl.field_6012;
    }

    static void onSend(class_2596<?> p) {
        if (!ON) return;
        class_310 mc = class_310.method_1551();
        class_746 pl = mc.field_1724;
        class_638 w = mc.field_1687;
        if (pl == null || w == null) return;
        int t = tick();
        try {
            if (p instanceof net.minecraft.class_2828 m) move(m, t, w);
            else if (p instanceof net.minecraft.class_2851 in) {
                sneak = in.comp_3139().comp_3164();
                sprint = in.comp_3139().comp_3165() || sprint && in.comp_3139().comp_3159();
            } else if (p instanceof net.minecraft.class_2848 c) {
                if (c.method_12365() == net.minecraft.class_2848.class_2849.field_12981) sprint = true;
                if (c.method_12365() == net.minecraft.class_2848.class_2849.field_12985) sprint = false;
            } else if (p instanceof net.minecraft.class_2885 ib) interact(ib, t, w, pl);
            else if (p instanceof net.minecraft.class_2846 a) action(a, t, w, pl);
            else if (p instanceof net.minecraft.class_2879) {
                needSwing = false;
                if (invOpen) flag("Grim.InventoryB", "swing with the inventory open");
            } else if (p instanceof net.minecraft.class_2813) {
                invClicks++;
                if (Math.hypot(lastDx, lastDz) > 0.03) flag("Grim.InventoryC", "inventory click while moving " + String.format(java.util.Locale.ROOT, "%.3f", Math.hypot(lastDx, lastDz)));
                if (sprint) flag("Grim.InventoryD", "inventory click while sprinting");
                if (pl.field_7512 == pl.field_7498) invOpen = true; // own inventory: open until the close packet
            } else if (p instanceof net.minecraft.class_2815) invOpen = false;
        } catch (Throwable e) {
            System.out.println("CARTTEST AC error " + e);
        }
    }

    static void onReceive(class_2596<?> p) {
        if (!ON) return;
        if (p instanceof net.minecraft.class_2708) {
            // a server teleport / setback: the client answers with an extra movement packet (not a timer cheat)
            teleportPending = true;
            teleports++;
            class_746 pl = class_310.method_1551().field_1724;
            if (teleports <= 5) System.out.println("CARTTEST AC dbg teleport from server at " + (pl == null ? "?" : pl.method_23317() + " " + pl.method_23318() + " " + pl.method_23321()));
        }
        if (p instanceof net.minecraft.class_4463 ack) {
            lastAck = Math.max(lastAck, ack.comp_633());
            unconfirmed.values().removeIf(s -> s <= lastAck);
        }
    }

    private static double[] eyes() {
        // the server can't be sure about the pose for a tick after sneaking changes: allow both
        return new double[]{1.27, 1.62};
    }

    private static boolean teleportPending;
    private static int teleports;
    private static float beforePlaceD = Float.NaN;
    private static boolean rotSincePlace;

    private static void move(net.minecraft.class_2828 m, int t, class_638 w) {
        if (teleportPending) {
            // the teleport confirmation: take its position / rotation as the new truth, nothing else
            teleportPending = false;
            if (m.method_36172()) { syaw = m.method_12271(syaw); spitch = m.method_12270(spitch); hasRot = true; }
            if (m.method_36171()) { sx = m.method_12269(sx); sy = m.method_12268(sy); sz = m.method_12274(sz); hasPos = true; }
            return;
        }
        if (t == moveTick) {
            if (++movesThisTick > 1) flag("Grim.Timer", "two movement packets in one tick");
        } else {
            moveTick = t;
            movesThisTick = 1;
            if (placeTick != t) placesThisTick = 0;
        }
        if (needSwing) { flag("Grim.NoSwing", needSwingWhat + " without a swing"); needSwing = false; }
        // mining progress like the server counts it: every tick with that tick's tool, ground and water state
        if (digPos != null) {
            class_746 pl = class_310.method_1551().field_1724;
            float d = digState.method_26165(pl, w, digPos);
            digProgress += d;
            digMax = Math.max(digMax, d);
        }
        float ny = m.method_12271(syaw), np = m.method_12270(spitch);
        double nx = m.method_12269(sx), nyy = m.method_12268(sy), nz = m.method_12274(sz);
        if (m.method_36172() && hasRot) {
            float dy = ny - syaw, dp = np - spitch;
            lastDYaw = dy;
            lastDPitch = dp;
            if (dy != 0 || dp != 0) {
                grimDeltaX = Math.abs(dy);
                grimRotated = true;
                rotSincePlace = true;
                rotPackets++;
                lastRotChangeTick = t;
                maxTurn = Math.max(maxTurn, Math.hypot(dy, dp));
                if (Math.hypot(dy, dp) > 35 && printed.merge("turn", 1, Integer::sum) <= 3) System.out.println("CARTTEST AC dbg big turn " + dy + "/" + dp + " to " + ny + "/" + np);
                if (Math.abs(dy) > 320) flag("Grim.AimModulo360", "yaw jumped " + dy);
                if (mouseStep > 0) {
                    boolean bad = off(dy) || Math.abs(np) < 89.99f && Math.abs(spitch) < 89.99f && off(dp);
                    if (bad) { gcdBad++; flag("Grim.AimSensitivity", "rotation not a mouse step: " + dy + "/" + dp + " step " + mouseStep); }
                }
            }
        } else if (m.method_36172()) hasRot = true;
        if (!m.method_36172()) { lastDYaw = 0; lastDPitch = 0; }
        if (m.method_36171()) {
            if (hasPos) { lastDx = nx - sx; lastDz = nz - sz; } else lastDx = lastDz = 0;
            if (invOpen && Math.hypot(lastDx, lastDz) > 0.03) flag("Grim.InventoryC", "moving with the inventory open");
            hasPos = true;
        } else { lastDx = lastDz = 0; }
        if (m.method_36172()) { syaw = ny; spitch = np; }
        if (m.method_36171()) { sx = nx; sy = nyy; sz = nz; }
        // Grim DuplicateRotPlace: the turn after a place must not be exactly the turn after the last place
        if (placedSinceMove && grimRotated) {
            placedSinceMove = false;
            {
                if (grimDeltaX > 2 && Math.abs(grimDeltaX - lastPlaceDYaw) < 1.0E-4) flag("Grim.DuplicateRotPlace", "same turn after two places " + grimDeltaX);
                lastPlaceDYaw = grimDeltaX;
                grimRotated = false;
            }
        }
        // interactions that didn't line up with the old rotation may line up with this one (Grim checks both)
        for (Pending pd : pending) {
            boolean rot = pd.rot, sight = pd.sight;
            for (double h : eyes()) {
                class_243 eye = new class_243(pd.eye.field_1352, pd.feetY + h, pd.eye.field_1350);
                if (rayHits(w, eye, syaw, spitch, pd.pos)) { rot = true; if (los(w, eye, syaw, spitch, pd.pos, pd.side)) sight = true; }
            }
            if (!rot) flag(pd.what.equals("place") ? "Grim.RotationPlace" : "Grim.RotationBreak", pd.pos.method_23854() + " not looked at");
            else if (!sight) flag("NCP.Direction/Vulcan.Interact", pd.what + " " + pd.pos.method_23854() + " " + pd.side + " not what the crosshair sees");
        }
        pending.clear();
    }

    private static boolean off(float d) {
        if (Math.abs(d) < 1.0E-4) return false;
        double q = d / mouseStep;
        return Math.abs(q - Math.round(q)) > 0.03;
    }

    /**
     * A click the builder means to do on a block that changes by it (repeater delay, trapdoor open...): the client
     * already changed the block before the packet is checked here, so check against the block as it was.
     */
    static long intendedPos = Long.MIN_VALUE;
    static class_238 intendedBox;

    private static class_238 box(class_638 w, class_2338 pos) {
        if (pos.method_10063() == intendedPos && intendedBox != null) return intendedBox;
        class_2680 st = w.method_8320(pos);
        var sh = st.method_26218(w, pos);
        if (sh.method_1110()) return new class_238(pos);
        return sh.method_1107().method_996(pos);
    }

    private static boolean rayHits(class_638 w, class_243 eye, float yaw, float pitch, class_2338 pos) {
        class_243 end = eye.method_1019(dir(yaw, pitch).method_1021(7.0));
        return box(w, pos).method_1014(0.0005).method_992(eye, end).isPresent();
    }

    /** What the vanilla crosshair would hit with that rotation: the same block and face. */
    private static boolean los(class_638 w, class_243 eye, float yaw, float pitch, class_2338 pos, class_2350 side) {
        class_3965 h = SbPlacer.rayRot(w, class_310.method_1551().field_1724, eye, yaw, pitch, 6.0);
        if (h == null) return false;
        // the client places its predicted block before the packet goes out: hitting that new block is the same look
        if (h.method_17777().equals(pos.method_10093(side))) return true;
        return h.method_17777().equals(pos) && h.method_17780() == side;
    }

    private static class_243 dir(float yaw, float pitch) {
        double f = Math.toRadians(pitch), g = Math.toRadians(-yaw);
        return new class_243(Math.sin(g) * Math.cos(f), -Math.sin(f), Math.cos(g) * Math.cos(f));
    }

    /** Checks shared by placing and breaking; returns the eye that passed (or the standing eye). */
    private static class_243 common(class_638 w, class_2338 pos, class_2350 side, String what) {
        class_238 b = box(w, pos);
        class_243 best = null;
        double bestDist = Double.MAX_VALUE;
        for (double h : eyes()) {
            class_243 eye = new class_243(sx, sy + h, sz);
            double cx = Math.max(b.field_1323, Math.min(eye.field_1352, b.field_1320));
            double cy = Math.max(b.field_1322, Math.min(eye.field_1351, b.field_1325));
            double cz = Math.max(b.field_1321, Math.min(eye.field_1350, b.field_1324));
            double d = eye.method_1022(new class_243(cx, cy, cz));
            if (d < bestDist) { bestDist = d; best = eye; }
        }
        maxReach = Math.max(maxReach, bestDist);
        if (bestDist > 4.5 + 0.0425) flag(what.equals("place") ? "Grim.FarPlace" : "Grim.FarBreak", pos.method_23854() + String.format(java.util.Locale.ROOT, " %.2f blocks", bestDist));
        // the clicked face must be one the eye can see (Grim PositionPlace / PositionBreak)
        boolean ok = false;
        for (double h : eyes()) {
            double ex = sx, ey = sy + h, ez = sz;
            boolean v = switch (side) {
                case field_11036 -> ey > b.field_1325 - 1.0E-4;
                case field_11033 -> ey < b.field_1322 + 1.0E-4;
                case field_11034 -> ex > b.field_1320 - 1.0E-4;
                case field_11039 -> ex < b.field_1323 + 1.0E-4;
                case field_11035 -> ez > b.field_1324 - 1.0E-4;
                default -> ez < b.field_1321 + 1.0E-4;
            };
            if (v) ok = true;
        }
        if (!ok && what.equals("place")) flag("Grim.PositionPlace", pos.method_23854() + " " + side + " face can't be seen from " + String.format(java.util.Locale.ROOT, "%.2f %.2f %.2f", sx, sy, sz));
        if (!ok && what.equals("break")) flag("Grim.PositionBreak", pos.method_23854() + " " + side);
        boolean rot = false, sight = false;
        if (pos.method_10063() == intendedPos) return best; // the crosshair was checked on the block before it changed
        for (double h : eyes()) {
            class_243 eye = new class_243(sx, sy + h, sz);
            if (rayHits(w, eye, syaw, spitch, pos)) { rot = true; if (los(w, eye, syaw, spitch, pos, side)) sight = true; }
        }
        if (!rot || !sight) {
            pending.add(new Pending(pos, side, best, sy, what, rot, sight));
            class_746 pl = class_310.method_1551().field_1724;
            if (printed.merge("dbg", 1, Integer::sum) <= 8)
                System.out.println("CARTTEST AC dbg " + what + " " + pos.method_23854() + " " + side + " rot=" + rot + " sight=" + sight
                        + String.format(java.util.Locale.ROOT, " server eye %.3f %.3f %.3f rot %.2f/%.2f | client feet %.3f %.3f %.3f eye %.3f rot %.2f/%.2f sneak=%s",
                        sx, sy, sz, syaw, spitch, pl.method_23317(), pl.method_23318(), pl.method_23321(), pl.method_23320(), pl.method_36454(), pl.method_36455(), sneak)
                        + " clientRay=" + rayHits(w, pl.method_33571(), pl.method_36454(), pl.method_36455(), pos));
        }
        return best;
    }

    private static void interact(net.minecraft.class_2885 ib, int t, class_638 w, class_746 pl) {
        class_3965 hit = ib.method_12543();
        class_2338 pos = hit.method_17777();
        class_2680 st = w.method_8320(pos);
        if (st.method_26215() || !st.method_26227().method_15769() && st.method_26218(w, pos).method_1110()) flag("Grim.AirLiquidPlace", "clicked " + st + " at " + pos.method_23854());
        if (unconfirmed.containsKey(pos.method_10063())) flag("Grim.GhostBlock", "clicked " + pos.method_23854() + " before the server confirmed it");
        class_243 rel = hit.method_17784().method_1020(class_243.method_24954(pos));
        if (rel.field_1352 < -1.0E-3 || rel.field_1352 > 1.001 || rel.field_1351 < -1.0E-3 || rel.field_1351 > 1.001 || rel.field_1350 < -1.0E-3 || rel.field_1350 > 1.001)
            flag("Grim.FabricatedPlace", "hit vector outside the block " + rel);
        common(w, pos, hit.method_17780(), "place");
        boolean intended = pos.method_10063() == intendedPos;
        intendedPos = Long.MIN_VALUE; // one click only
        boolean placing = pl.method_6047().method_7909() instanceof net.minecraft.class_1747;
        if (placing) {
            places++;
            placeTicks.add(t);
            if (t == placeTick && ++placesThisTick > 1) flag("Grim.MultiPlace", "two places in one tick");
            if (t != placeTick) placesThisTick = 1;
            placeTick = t;
            if (t - lastRotChangeTick >= 1) stillPlaces++;
            // the other way Grim can read it: the last turn before this place vs. the one before the last place
            if (rotSincePlace && grimDeltaX > 2 && Math.abs(grimDeltaX - beforePlaceD) < 1.0E-4) flag("Grim.DuplicateRotPlace", "same last turn before two places " + grimDeltaX);
            beforePlaceD = grimDeltaX;
            rotSincePlace = false;
            grimRotated = false;
            placedSinceMove = true;
            unconfirmed.put(pos.method_10093(hit.method_17780()).method_10063(), ib.method_42080());
        }
        if (invOpen) flag("Grim.InventoryB", "click on a block with the inventory open");
        if (placing && !sneak && SbWorld.usable(w, pos) && !SbWorld.container(st) && !intended) flag("Bot.UsedBlock", "right-clicked " + st.method_26204() + " without sneaking (opens / flips it)");
        needSwing = true;
        needSwingWhat = "right click";
    }

    private static void action(net.minecraft.class_2846 a, int t, class_638 w, class_746 pl) {
        var act = a.method_12363();
        class_2338 pos = a.method_12362();
        if (act == net.minecraft.class_2846.class_2847.field_12968) {
            class_2680 st = w.method_8320(pos);
            if (st.method_26215()) flag("Grim.InvalidBreak", "start breaking air at " + pos.method_23854());
            common(w, pos, a.method_12360(), "break");
            if (invOpen) flag("Grim.InventoryB", "breaking with the inventory open");
            float delta = st.method_26165(pl, w, pos);
            digNeed = delta <= 0 ? 1e9 : 1.0 / delta;
            digProgress = 0;
            digMax = delta;
            digState = st;
            digPos = delta >= 1.0f ? null : pos; // instant breaks finish with the start packet
            digStart = t;
            breaks++;
            needSwing = true;
            needSwingWhat = "start breaking";
        } else if (act == net.minecraft.class_2846.class_2847.field_12973) {
            if (digPos == null || !digPos.equals(pos)) flag("Grim.WrongBreak", "finished " + pos.method_23854() + " without starting it");
            else {
                int ticks = t - digStart;
                // vanilla server: speed right now x ticks since the start must reach 0.7, or the block is not broken
                float now = digState.method_26165(pl, w, pos); // the client already removed the block
                if (now * (ticks + 1) < 0.7f) flag("Vanilla.DestroyMismatch", pos.method_23854() + " after " + ticks + " ticks, speed " + now);
                // Grim FastBreak: best speed seen during the break, time balance with 1 s of tolerance
                double predicted = Math.ceil(1.0 / Math.max(1.0E-6, digMax)) * 50.0, real = ticks * 50.0, diff = predicted - real;
                if (diff < 25) breakBalance *= 0.9; else breakBalance += diff;
                if (breakBalance > 1000) flag("Grim.FastBreak", pos.method_23854() + " broken in " + ticks + " ticks, predicted " + (int) (predicted / 50));
                if (digProgress < 0.9) slowAir++;
            }
            digPos = null;
        }
    }

    static void report() {
        if (!ON) return;
        int maxPerSec = 0, minGap = Integer.MAX_VALUE;
        for (int i = 0; i < placeTicks.size(); i++) {
            int n = 0;
            for (int j = i; j < placeTicks.size() && placeTicks.get(j) - placeTicks.get(i) < 20; j++) n++;
            maxPerSec = Math.max(maxPerSec, n);
            if (i > 0) minGap = Math.min(minGap, placeTicks.get(i) - placeTicks.get(i - 1));
        }
        int total = 0;
        for (int v : flags.values()) total += v;
        System.out.println("CARTTEST AC SUMMARY flags=" + total + " " + flags + " | teleports=" + teleports + " places=" + places + " breaks=" + breaks + " invClicks=" + invClicks
                + " maxPlaces/s=" + maxPerSec + " minPlaceGap=" + (minGap == Integer.MAX_VALUE ? "-" : minGap + "t")
                + " breaksFasterThanAirSpeed=" + slowAir + String.format(java.util.Locale.ROOT, " maxReach=%.2f maxTurn/tick=%.1f rotations=%d offStep=%d stillClicks=%d%%",
                maxReach, maxTurn, rotPackets, gcdBad, places == 0 ? 0 : stillPlaces * 100 / places));
        System.out.println("CARTTEST CAMERA " + cameraReport());
    }
}
