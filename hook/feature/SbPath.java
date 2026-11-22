package hook.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_638;

/**
 * A* over standing positions. Moves: walk, step up (jump), drop (only as deep as allowed, deeper only into water),
 * bridge over gaps and pillar up with the scaffold block, and break soft obstacles out of the way. Lava, fire, magma,
 * cactus, berry bushes, powder snow and cobwebs are never walked into or next to.
 */
final class SbPath {
    private SbPath() {
    }

    enum Move { WALK, UP, DROP, BRIDGE, PILLAR, DOWN }

    record Step(class_2338 pos, Move move, List<class_2338> breaks) {
    }

    static final class Rules {
        int maxFall = 3;
        boolean scaffold = true;
        boolean breakObstacles = true;
        int maxNodes = 9000;
        Set<Long> avoid = Set.of();
        /** May this block be broken to get through (false for correct schematic blocks, storage...)? */
        Predicate<class_2338> mayBreak = p -> true;
        /** May a scaffold block go here (false inside the schematic where something else belongs)? */
        Predicate<class_2338> mayScaffold = p -> true;
    }

    private static final class Node implements Comparable<Node> {
        final class_2338 pos;
        final double g, f;
        final Node parent;
        final Move move;
        final List<class_2338> breaks;

        Node(class_2338 pos, double g, double h, Node parent, Move move, List<class_2338> breaks) {
            this.pos = pos;
            this.g = g;
            this.f = g + h;
            this.parent = parent;
            this.move = move;
            this.breaks = breaks;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.f, o.f);
        }
    }

    private static final class_2350[] HORIZONTAL = {class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039};

    /** Path from {@code start} to the cheapest goal, or null. An empty list means "already there". */
    static List<Step> find(class_638 w, class_2338 start, Predicate<class_2338> goal, class_2338 hint, Rules r) {
        if (goal.test(start)) return new ArrayList<>();
        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<Long, Double> best = new HashMap<>();
        open.add(new Node(start, 0, h(start, hint), null, Move.WALK, List.of()));
        best.put(start.method_10063(), 0.0);
        int expanded = 0;
        Node closest = null;
        double closestH = Double.MAX_VALUE;
        while (!open.isEmpty() && expanded < r.maxNodes) {
            Node n = open.poll();
            Double known = best.get(n.pos.method_10063());
            if (known != null && known < n.g - 1.0E-6) continue;
            if (goal.test(n.pos)) return build(n);
            expanded++;
            double hn = h(n.pos, hint);
            if (hn < closestH) { closestH = hn; closest = n; }
            expand(w, n, r, (pos, cost, move, breaks) -> {
                if (r.avoid.contains(pos.method_10063())) return;
                double g = n.g + cost;
                Double old = best.get(pos.method_10063());
                if (old != null && old <= g) return;
                best.put(pos.method_10063(), g);
                open.add(new Node(pos, g, h(pos, hint), n, move, breaks));
            });
        }
        return null;
    }

    private interface Sink {
        void add(class_2338 pos, double cost, Move move, List<class_2338> breaks);
    }

    private static double h(class_2338 a, class_2338 b) {
        double dx = a.method_10263() - b.method_10263(), dy = a.method_10264() - b.method_10264(), dz = a.method_10260() - b.method_10260();
        return Math.sqrt(dx * dx + dy * dy * 1.5 + dz * dz);
    }

    private static List<Step> build(Node n) {
        List<Step> out = new ArrayList<>();
        for (Node c = n; c.parent != null; c = c.parent) out.add(new Step(c.pos, c.move, c.breaks));
        Collections.reverse(out);
        return out;
    }

    /** Blocks that must be broken so the body fits at feet + head (null = impossible). */
    private static List<class_2338> clear(class_638 w, class_2338 feet, Rules r) {
        List<class_2338> b = null;
        for (int i = 0; i < 2; i++) {
            class_2338 p = feet.method_10086(i);
            if (SbWorld.passable(w, p)) continue;
            if (!r.breakObstacles || SbWorld.dangerous(w.method_8320(p)) || !SbWorld.breakable(w, p) || !r.mayBreak.test(p)) return null;
            if (b == null) b = new ArrayList<>(2);
            b.add(p);
        }
        return b == null ? List.of() : b;
    }

    private static boolean safeAt(class_638 w, class_2338 feet) {
        return !SbWorld.nearLava(w, feet) && !SbWorld.nearLava(w, feet.method_10084());
    }

    private static boolean inWater(class_638 w, class_2338 feet) {
        return SbWorld.water(w.method_8320(feet));
    }

    private static void expand(class_638 w, Node n, Rules r, Sink out) {
        class_2338 p = n.pos;
        // the block under us may only exist in the plan (placed by the previous bridge / pillar step)
        boolean underUs = SbWorld.floor(w, p.method_10074()) || (n.parent != null && (n.move == Move.PILLAR || n.move == Move.BRIDGE));
        for (class_2350 d : HORIZONTAL) {
            class_2338 q = p.method_10093(d);
            List<class_2338> br = clear(w, q, r);
            if (br != null && safeAt(w, q)) {
                double bc = br.size() * 4.0;
                if (SbWorld.floor(w, q.method_10074()) || inWater(w, q)) {
                    out.add(q, 1.0 + bc + (inWater(w, q) ? 2.0 : 0.0), Move.WALK, br);
                } else if (br.isEmpty() && SbWorld.passable(w, q.method_10074())) {
                    // drop down
                    boolean landed = false;
                    for (int k = 1; k <= 24; k++) {
                        class_2338 f = q.method_10087(k);
                        if (!SbWorld.passable(w, f)) break;
                        if (inWater(w, f)) { if (safeAt(w, f)) out.add(f, 1.0 + k * 0.4, Move.DROP, List.of()); landed = true; break; }
                        if (SbWorld.floor(w, f.method_10074())) {
                            if (k <= r.maxFall && safeAt(w, f)) out.add(f, 1.0 + k * 0.6, Move.DROP, List.of());
                            landed = true;
                            break;
                        }
                    }
                    // bridge over the gap with the scaffold block
                    class_2338 under = q.method_10074();
                    if (r.scaffold && underUs && w.method_8320(under).method_45474() && !SbWorld.dangerous(w.method_8320(under))
                            && r.mayScaffold.test(under)) {
                        out.add(q, landed ? 6.0 : 4.0, Move.BRIDGE, List.of());
                    }
                }
            }
            // step up one block (jump): head room above you, room at the target
            class_2338 u = q.method_10084();
            if (SbWorld.floor(w, q) && SbWorld.passable(w, p.method_10086(2))) {
                List<class_2338> bu = clear(w, u, r);
                if (bu != null && safeAt(w, u)) out.add(u, 2.0 + bu.size() * 4.0, Move.UP, bu);
            }
        }
        // dig straight down through the block we stand on (gets us off our own scaffold towers safely)
        class_2338 below = p.method_10074();
        if (r.breakObstacles && SbWorld.floor(w, below) && SbWorld.floor(w, below.method_10074()) && SbWorld.breakable(w, below)
                && !SbWorld.dangerous(w.method_8320(below)) && r.mayBreak.test(below) && safeAt(w, below)) {
            out.add(below, 3.0, Move.DOWN, List.of(below));
        }
        // pillar straight up with the scaffold block
        if (r.scaffold && SbWorld.passable(w, p.method_10086(2)) && underUs && r.mayScaffold.test(p) && safeAt(w, p.method_10084())) {
            out.add(p.method_10084(), 5.0, Move.PILLAR, List.of());
        }
    }
}
