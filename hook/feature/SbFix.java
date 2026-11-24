package hook.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_7923;

/**
 * Blocks that are not simply "placed": states you get by right-clicking a placed block (repeater delay, comparator
 * mode, note block pitch, open trapdoors / gates...), by clicking the same block again (double slabs, more candles,
 * sea pickles, snow layers, turtle eggs), by using a tool on another block (farmland, dirt paths, stripped logs) or by
 * pouring water into it (waterlogged blocks). Like a player builds them.
 */
final class SbFix {
    private SbFix() {
    }

    static final int PLACE = 0, ADJUST = 1, GROW = 2, CONVERT = 3, WATERLOG = 4;

    /** Counted properties that grow by clicking the same item onto the block again. */
    private static final Set<String> GROW_PROPS = Set.of("candles", "pickles", "layers", "eggs");

    static String id(class_2248 b) {
        try {
            return class_7923.field_41175.method_10221(b).method_12832();
        } catch (Throwable t) {
            return "?";
        }
    }

    private static Map<String, class_2248> byId;

    static synchronized class_2248 block(String id) {
        if (byId == null) {
            Map<String, class_2248> m = new HashMap<>();
            for (class_2248 b : class_7923.field_41175) m.put(id(b), b);
            byId = m;
        }
        return byId.get(id);
    }

    /** Opened / closed by hand (wooden doors, trapdoors, fence gates; not the iron ones). */
    static boolean handOpenable(class_2680 s) {
        String id = id(s.method_26204());
        return !id.startsWith("iron_") && (id.endsWith("_door") || id.endsWith("_trapdoor") || id.endsWith("_fence_gate"));
    }

    /** Extra properties a finished block must have besides its shape (see {@link SbWorld#matchesShape}). */
    static boolean extraMatch(class_2680 want, class_2680 have) {
        for (class_2769<?> p : want.method_28501()) {
            String k = p.method_11899();
            if (!adjustable(want, k) && !GROW_PROPS.contains(k)) continue;
            if (!have.method_28498(p)) return false;
            if (!want.method_11654(p).equals(have.method_11654(p))) return false;
        }
        return true;
    }

    private static boolean adjustable(class_2680 want, String k) {
        return switch (k) {
            case "delay", "note", "inverted" -> true;
            case "mode" -> id(want.method_26204()).equals("comparator");
            // an open door that is open because a redstone signal holds it open: comes by itself
            case "open" -> handOpenable(want) && !Boolean.TRUE.equals(get(want, "powered"));
            default -> false;
        };
    }

    static Object get(class_2680 s, String name) {
        for (class_2769<?> p : s.method_28501()) if (p.method_11899().equals(name)) return s.method_11654(p);
        return null;
    }

    /** Right-clicks on the placed block that turn {@code have} into {@code want}, or -1 when clicking can't do it. */
    static int clicks(class_2680 want, class_2680 have) {
        if (want.method_26204() != have.method_26204() || !SbWorld.matchesShape(want, have)) return -1;
        int n = 0;
        for (class_2769<?> p : want.method_28501()) {
            String k = p.method_11899();
            if (!have.method_28498(p)) continue;
            Object a = want.method_11654(p), b = have.method_11654(p);
            if (a.equals(b)) continue;
            if (GROW_PROPS.contains(k)) return -1;
            if (!adjustable(want, k)) continue;
            switch (k) {
                case "delay" -> n += (((Integer) a - (Integer) b) % 4 + 4) % 4;
                case "note" -> n += (((Integer) a - (Integer) b) % 25 + 25) % 25;
                default -> n += 1;
            }
        }
        return n;
    }

    /** {@code have} becomes {@code want} by clicking the same item onto it again (slab -> double slab, 1 -> 3 candles...). */
    static boolean grows(class_2680 want, class_2680 have) {
        if (want.method_26204() != have.method_26204()) return false;
        boolean more = false;
        for (class_2769<?> p : want.method_28501()) {
            String k = p.method_11899();
            if (!have.method_28498(p)) continue;
            Object a = want.method_11654(p), b = have.method_11654(p);
            if (a.equals(b)) continue;
            if (k.equals("type") && "double".equals(String.valueOf(a)) && !"double".equals(String.valueOf(b))) { more = true; continue; }
            if (GROW_PROPS.contains(k) && a instanceof Integer ia && b instanceof Integer ib) {
                if (ia > ib) { more = true; continue; }
                return false;
            }
            if (k.equals("type") || SbWorld.important(k)) return false;
        }
        return more;
    }

    /** What to place first for a state that is finished by clicking it again (a single slab for a double one). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static class_2680[] firstSteps(class_2680 want) {
        for (class_2769 p : want.method_28501()) {
            if (p.method_11899().equals("type") && "double".equals(String.valueOf(want.method_11654(p)))) {
                class_2680 bottom = null, top = null;
                for (Object v : p.method_11898()) {
                    if ("bottom".equals(String.valueOf(v))) bottom = want.method_11657(p, (Comparable) v);
                    if ("top".equals(String.valueOf(v))) top = want.method_11657(p, (Comparable) v);
                }
                if (bottom != null && top != null) return new class_2680[]{bottom, top};
            }
        }
        return new class_2680[]{want};
    }

    /** The block a tool turns into {@code want} (dirt for farmland and paths, the log for a stripped log), or null. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static class_2680 convertBase(class_2680 want) {
        String id = id(want.method_26204());
        if (id.equals("farmland") || id.equals("dirt_path")) return class_2246.field_10566.method_9564();
        if (!id.startsWith("stripped_")) return null;
        class_2248 b = block(id.substring(9));
        if (b == null) return null;
        class_2680 s = b.method_9564();
        for (class_2769 p : s.method_28501()) {
            Object v = get(want, p.method_11899());
            if (v != null) try { s = s.method_11657(p, (Comparable) v); } catch (Throwable ignored) { }
        }
        return s;
    }

    /** The tool that converts (item id suffix). */
    static String convertTool(class_2680 want) {
        String id = id(want.method_26204());
        if (id.equals("farmland")) return "_hoe";
        if (id.equals("dirt_path")) return "_shovel";
        return "_axe";
    }

    /** {@code have} turns into {@code want} with the tool. */
    static boolean convertsFrom(class_2680 want, class_2680 have) {
        String w = id(want.method_26204()), h = id(have.method_26204());
        if (w.equals("farmland")) return h.equals("dirt") || h.equals("grass_block") || h.equals("dirt_path");
        if (w.equals("dirt_path")) return h.equals("dirt") || h.equals("grass_block") || h.equals("podzol") || h.equals("coarse_dirt") || h.equals("mycelium") || h.equals("rooted_dirt");
        class_2680 base = convertBase(want);
        return base != null && base.method_26204() == have.method_26204() && SbWorld.matchesShape(base, have);
    }

    /** Hoe / shovel need air above the block. */
    static boolean needsAirAbove(class_2680 want) {
        String id = id(want.method_26204());
        return id.equals("farmland") || id.equals("dirt_path");
    }

    static boolean waterlogged(class_2680 s) {
        return Boolean.TRUE.equals(get(s, "waterlogged"));
    }

    /** Falls when nothing holds it (sand, gravel, concrete powder, anvils...). */
    static boolean falls(class_2680 s) {
        return s.method_26204() instanceof net.minecraft.class_2346;
    }

    /**
     * Redstone parts come after the other blocks of their layer so half-built circuits don't fire (observers last).
     * Less than one layer (2.5): putting them off longer walls them in.
     */
    static double redstoneLate(class_2680 s) {
        String id = id(s.method_26204());
        if (id.equals("observer")) return 2.2;
        if (id.equals("redstone_block") || id.equals("redstone_torch") || id.equals("redstone_wall_torch") || id.equals("lever") || id.equals("daylight_detector")
                || id.equals("tripwire_hook") || id.endsWith("_button") || id.endsWith("_pressure_plate")) return 2.0;
        if (id.equals("redstone_wire") || id.equals("repeater") || id.equals("comparator")) return 1.0;
        if (id.contains("piston") || id.equals("dispenser") || id.equals("dropper") || id.equals("crafter")) return 0.5;
        return 0.0;
    }
}
