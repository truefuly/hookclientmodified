package hook.feature;

import java.util.Map;
import java.util.Set;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_3486;
import net.minecraft.class_3610;
import net.minecraft.class_638;

/** Block rules for the Schematic Builder: where it can stand and walk, what is dangerous, what may be broken. */
final class SbWorld {
    private SbWorld() {
    }

    /** Properties that decide how a block looks once placed; everything else (connections, power...) updates itself. */
    private static final Set<String> IMPORTANT = Set.of("facing", "axis", "half", "type", "rotation", "hinge", "face", "orientation", "attachment", "hanging", "vertical_direction", "attach_face");

    static class_2680 state(class_638 w, class_2338 p) {
        return w.method_8320(p);
    }

    static boolean lava(class_2680 s) {
        class_3610 f = s.method_26227();
        return !f.method_15769() && f.method_15767(class_3486.field_15518);
    }

    static boolean water(class_2680 s) {
        class_3610 f = s.method_26227();
        return !f.method_15769() && f.method_15767(class_3486.field_15517);
    }

    /** Blocks that hurt, trap or burn: never walk into, stand on or next to. */
    static boolean dangerous(class_2680 s) {
        class_2248 b = s.method_26204();
        return lava(s) || b == class_2246.field_10036 || b == class_2246.field_22089 || b == class_2246.field_10092 || b == class_2246.field_10029
                || b == class_2246.field_16999 || b == class_2246.field_27879 || b == class_2246.field_17350 || b == class_2246.field_23860
                || b == class_2246.field_10606 || b == class_2246.field_28048 || b == class_2246.field_10343;
    }

    /** The body fits through this block (no collision) and it is not dangerous. */
    static boolean passable(class_638 w, class_2338 p) {
        class_2680 s = w.method_8320(p);
        if (dangerous(s)) return false;
        return s.method_26220(w, p).method_1110();
    }

    /** Something solid to stand on (collision top reaches the block top). */
    static boolean floor(class_638 w, class_2338 p) {
        class_2680 s = w.method_8320(p);
        if (dangerous(s) || s.method_26204() == class_2246.field_16492) return false; // scaffolding: you sink through
        var shape = s.method_26220(w, p);
        if (shape.method_1110()) return false;
        double top = shape.method_1105(net.minecraft.class_2350.class_2351.field_11052);
        if (top < 0.99) return false;
        // something at the top under a player standing in the middle (not an open trapdoor / door at the edge)
        for (net.minecraft.class_238 b : shape.method_1090())
            if (b.field_1325 >= top - 0.05 && b.field_1320 > 0.2 && b.field_1323 < 0.8 && b.field_1324 > 0.2 && b.field_1321 < 0.8) return true;
        return false;
    }

    /** Lava (or fire) directly beside or below a spot: keep away from it. */
    static boolean nearLava(class_638 w, class_2338 p) {
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) for (int dy = -1; dy <= 1; dy++) {
            if (dx == 0 && dz == 0 && dy == 0) continue;
            class_2680 s = w.method_8320(p.method_10069(dx, dy, dz));
            if (lava(s) || s.method_26204() == class_2246.field_10036 || s.method_26204() == class_2246.field_22089) return true;
        }
        return false;
    }

    /** A spot where the player can stand: room for the body, a safe floor, no lava around. */
    static boolean standable(class_638 w, class_2338 feet) {
        return passable(w, feet) && passable(w, feet.method_10084()) && floor(w, feet.method_10074()) && !nearLava(w, feet) && !nearLava(w, feet.method_10084());
    }

    /** Liquid next to a block (except below): breaking it would let the liquid flow in. */
    static boolean liquidBeside(class_638 w, class_2338 p) {
        for (net.minecraft.class_2350 d : net.minecraft.class_2350.values()) {
            if (d == net.minecraft.class_2350.field_11033) continue;
            if (!w.method_8320(p.method_10093(d)).method_26227().method_15769()) return true;
        }
        return false;
    }

    /** Can be broken out of the way: not unbreakable, no container, not too hard, no liquid behind it. */
    static boolean breakable(class_638 w, class_2338 p) {
        class_2680 s = w.method_8320(p);
        if (s.method_26215() || !s.method_26227().method_15769()) return false;
        float h = s.method_26214(w, p);
        if (h < 0 || h > 6.0f) return false;
        if (s.method_31709()) return false; // chests, furnaces, signs...
        if (s.method_26204() == class_2246.field_9987) return false;
        return !liquidBeside(w, p);
    }

    static boolean important(String prop) {
        return IMPORTANT.contains(prop);
    }

    /**
     * The placed block is finished: right shape and also the states you set by clicking it (repeater delay, comparator
     * mode, note, open trapdoors...) and the counted ones (double slab, candles, layers...).
     */
    static boolean matches(class_2680 want, class_2680 have) {
        return matchesShape(want, have) && SbFix.extraMatch(want, have);
    }

    /** Same block, same facing / half / axis... (what placing decides). */
    @SuppressWarnings({"rawtypes"})
    static boolean matchesShape(class_2680 want, class_2680 have) {
        if (want.method_26204() != have.method_26204()) {
            // kelp / kelp_plant, vines / vines_plant: the same thing, it turns into the other when something grows on it
            class_2248 a = SbFix.stem(want.method_26204());
            return a != want.method_26204() || a != have.method_26204() ? a == SbFix.stem(have.method_26204()) : false;
        }
        for (Map.Entry<class_2769<?>, Comparable<?>> e : want.method_11656().entrySet()) {
            class_2769 prop = e.getKey();
            if (!IMPORTANT.contains(prop.method_11899())) continue;
            if (!have.method_28498(prop)) return false;
            if (!e.getValue().equals(have.method_11654(prop))) return false;
        }
        return true;
    }

    /** Parts placed together with another part (door top, bed head, piston head): never placed on their own. */
    static boolean secondaryPart(class_2680 s) {
        class_2248 b = s.method_26204();
        if (b == class_2246.field_10379 || b == class_2246.field_10008) return true;
        if (s.method_28498(net.minecraft.class_2741.field_12533) && s.method_11654(net.minecraft.class_2741.field_12533) == net.minecraft.class_2756.field_12609) return true;
        return s.method_28498(net.minecraft.class_2741.field_12483) && s.method_11654(net.minecraft.class_2741.field_12483) == net.minecraft.class_2742.field_12560;
    }

    /** Right-clicking it does something (opens a menu, flips it...): only place against it while sneaking. */
    static boolean usable(class_638 w, class_2338 p) {
        class_2680 s = w.method_8320(p);
        if (w.method_8321(p) != null || s.method_26196(w, p) != null) return true;
        class_2248 b = s.method_26204();
        return b instanceof net.minecraft.class_2323 || b instanceof net.minecraft.class_2533 || b instanceof net.minecraft.class_2349
                || b instanceof net.minecraft.class_2401 || b instanceof net.minecraft.class_2269 || b instanceof net.minecraft.class_2462
                || b instanceof net.minecraft.class_2286 || b instanceof net.minecraft.class_2428 || b instanceof net.minecraft.class_2244
                || b instanceof net.minecraft.class_3709;
    }

    static boolean container(class_2680 s) {
        class_2248 b = s.method_26204();
        return b == class_2246.field_10034 || b == class_2246.field_10380 || b == class_2246.field_16328 || b instanceof net.minecraft.class_2480;
    }
}
