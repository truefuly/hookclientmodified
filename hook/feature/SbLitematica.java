package hook.feature;

import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2802;

/**
 * Reads Litematica (when installed) through reflection, so HookVisuals still runs without it: which placement is
 * loaded, its box, and the block the hologram wants at a position. Litematica's "schematic world" already has the
 * placement's rotation, mirror, offset and disabled sub-regions applied, so it is exactly what you see in game.
 */
final class SbLitematica {
    private static Boolean present;
    private static Method getManager, getSelected, getAll, isEnabled, getName, getBox, getPos1, getPos2, getOrigin,
            getRotation, getMirror, getSchematic, getMetadata, getTotalBlocks, getSchematicWorld, getChunkProvider, getRegionBoxes;
    private static Object anyRegion;

    private SbLitematica() {
    }

    static boolean available() {
        if (present == null) {
            try {
                Class<?> dm = Class.forName("fi.dy.masa.litematica.data.DataManager");
                Class<?> mgr = Class.forName("fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager");
                Class<?> pl = Class.forName("fi.dy.masa.litematica.schematic.placement.SchematicPlacement");
                Class<?> box = Class.forName("fi.dy.masa.litematica.selection.Box");
                Class<?> sch = Class.forName("fi.dy.masa.litematica.schematic.LitematicaSchematic");
                Class<?> meta = Class.forName("fi.dy.masa.litematica.schematic.SchematicMetadata");
                Class<?> wh = Class.forName("fi.dy.masa.litematica.world.SchematicWorldHandler");
                Class<?> ws = Class.forName("fi.dy.masa.litematica.world.WorldSchematic");
                getManager = dm.getMethod("getSchematicPlacementManager");
                getSelected = mgr.getMethod("getSelectedSchematicPlacement");
                getAll = mgr.getMethod("getAllSchematicsPlacements");
                isEnabled = pl.getMethod("isEnabled");
                getName = pl.getMethod("getName");
                getBox = pl.getMethod("getEclosingBox");
                getOrigin = pl.getMethod("getOrigin");
                getRotation = pl.getMethod("getRotation");
                getMirror = pl.getMethod("getMirror");
                getSchematic = pl.getMethod("getSchematic");
                getPos1 = box.getMethod("getPos1");
                getPos2 = box.getMethod("getPos2");
                getMetadata = sch.getMethod("getMetadata");
                getTotalBlocks = meta.getMethod("getTotalBlocks");
                getSchematicWorld = wh.getMethod("getSchematicWorld");
                getChunkProvider = ws.getMethod("getChunkProvider");
                Class<?> req = Class.forName("fi.dy.masa.litematica.schematic.placement.SubRegionPlacement$RequiredEnabled");
                getRegionBoxes = pl.getMethod("getSubRegionBoxes", req);
                anyRegion = req.getField("PLACEMENT_ENABLED").get(null);
                present = true;
            } catch (Throwable t) {
                present = false;
            }
        }
        return present;
    }

    /** The placement to build: the one selected in Litematica, else the enabled one closest to you. */
    static Object placement(double px, double py, double pz) {
        if (!available()) return null;
        try {
            Object mgr = getManager.invoke(null);
            if (mgr == null) return null;
            Object sel = getSelected.invoke(mgr);
            if (sel != null && (Boolean) isEnabled.invoke(sel)) return sel;
            Object best = null;
            double bestD = Double.MAX_VALUE;
            for (Object p : (List<?>) getAll.invoke(mgr)) {
                if (!(Boolean) isEnabled.invoke(p)) continue;
                int[] b = box(p);
                if (b == null) continue;
                double dx = Math.max(0, Math.max(b[0] - px, px - b[3])), dy = Math.max(0, Math.max(b[1] - py, py - b[4])), dz = Math.max(0, Math.max(b[2] - pz, pz - b[5]));
                double d = dx * dx + dy * dy + dz * dz;
                if (d < bestD) { bestD = d; best = p; }
            }
            return best;
        } catch (Throwable t) {
            return null;
        }
    }

    static String name(Object p) {
        try {
            return String.valueOf(getName.invoke(p));
        } catch (Throwable t) {
            return "Schematic";
        }
    }

    /** minX, minY, minZ, maxX, maxY, maxZ (inclusive) of the whole placement. */
    static int[] box(Object p) {
        if (!available()) return null;
        try {
            // union of the enabled sub-regions (the enclosing box is not always filled in yet)
            int[] r = null;
            for (Object b : ((java.util.Map<?, ?>) getRegionBoxes.invoke(p, anyRegion)).values()) r = union(r, b);
            if (r == null) r = union(null, getBox.invoke(p));
            return r;
        } catch (Throwable t) {
            return null;
        }
    }

    private static int[] union(int[] r, Object b) throws Exception {
        if (b == null) return r;
        class_2338 a = (class_2338) getPos1.invoke(b), c = (class_2338) getPos2.invoke(b);
        if (a == null || c == null) return r;
        int[] n = {Math.min(a.method_10263(), c.method_10263()), Math.min(a.method_10264(), c.method_10264()), Math.min(a.method_10260(), c.method_10260()),
                Math.max(a.method_10263(), c.method_10263()), Math.max(a.method_10264(), c.method_10264()), Math.max(a.method_10260(), c.method_10260())};
        if (r == null) return n;
        return new int[]{Math.min(r[0], n[0]), Math.min(r[1], n[1]), Math.min(r[2], n[2]), Math.max(r[3], n[3]), Math.max(r[4], n[4]), Math.max(r[5], n[5])};
    }

    /** Changes whenever the placement is moved, rotated, mirrored or swapped for another one. */
    static String signature(Object p) {
        try {
            int[] b = box(p);
            return System.identityHashCode(p) + "|" + getOrigin.invoke(p) + "|" + getRotation.invoke(p) + "|" + getMirror.invoke(p)
                    + "|" + (b == null ? "" : java.util.Arrays.toString(b));
        } catch (Throwable t) {
            return "?";
        }
    }

    /** Non-air blocks in the schematic (all regions). */
    static int totalBlocks(Object p) {
        if (!available()) return -1;
        try {
            return (Integer) getTotalBlocks.invoke(getMetadata.invoke(getSchematic.invoke(p)));
        } catch (Throwable t) {
            return -1;
        }
    }

    static class_1937 world() {
        try {
            return (class_1937) getSchematicWorld.invoke(null);
        } catch (Throwable t) {
            return null;
        }
    }

    /** The hologram has this chunk loaded (otherwise its blocks read as air). */
    static boolean loaded(class_1937 w, int cx, int cz) {
        try {
            return ((class_2802) getChunkProvider.invoke(w)).method_12123(cx, cz);
        } catch (Throwable t) {
            return false;
        }
    }

    static class_2680 target(class_1937 w, class_2338 pos) {
        return w.method_8320(pos);
    }
}
