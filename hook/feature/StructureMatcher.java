package hook.feature;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Finds the exact start chunk of template structures (shipwrecks, igloos, ruined portals) by laying the game's own
 * structure files (data/minecraft/structure/*.nbt from the Minecraft jar) over the blocks in the world.
 *
 * <p>These structures are placed at the corner of their start chunk ({@code chunkPos.getWorldPosition()}), turned
 * (and for portals mirrored) around a fixed pivot. One block of the world (a chest, a furnace, obsidian) together with
 * every block of the same kind in a template gives the shift; only shifts that land on a chunk corner (x, z multiples
 * of 16) can be right, and the one whose template blocks are really there wins. A structure that fits two different
 * ways is skipped, so only sure positions are used.
 */
final class StructureMatcher {
    private StructureMatcher() {
    }

    /** Block name at a position without "minecraft:", or null when that chunk is not loaded. */
    interface World {
        String block(int x, int y, int z);
    }

    static final class Template {
        final String id;
        final int sx, sy, sz;
        final int[] x, y, z;
        final String[] name;

        Template(String id, int sx, int sy, int sz, List<int[]> pos, List<String> names) {
            this.id = id;
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
            int n = pos.size();
            this.x = new int[n];
            this.y = new int[n];
            this.z = new int[n];
            this.name = new String[n];
            for (int i = 0; i < n; i++) {
                this.x[i] = pos.get(i)[0];
                this.y[i] = pos.get(i)[1];
                this.z[i] = pos.get(i)[2];
                this.name[i] = names.get(i);
            }
        }
    }

    /** A kind of template structure. pivot < 0 = template size / 2 (ruined portals). */
    record Kind(String type, String[] templates, int pivotX, int pivotZ, boolean mirror, String anchor, double minRatio) {
    }

    static final Kind SHIPWRECK, IGLOO, RUINED_PORTAL;
    static final Kind[] KINDS;

    static {
        String[] ships = {"with_mast", "with_mast_degraded", "sideways_full", "sideways_fronthalf", "sideways_backhalf", "rightsideup_full",
            "rightsideup_fronthalf", "rightsideup_backhalf", "rightsideup_full_degraded", "rightsideup_fronthalf_degraded",
            "rightsideup_backhalf_degraded", "upsidedown_full", "upsidedown_fronthalf", "upsidedown_backhalf", "upsidedown_full_degraded",
            "upsidedown_fronthalf_degraded", "upsidedown_backhalf_degraded", "sideways_full_degraded", "sideways_fronthalf_degraded",
            "sideways_backhalf_degraded"};
        for (int i = 0; i < ships.length; i++) ships[i] = "shipwreck/" + ships[i];
        SHIPWRECK = new Kind("shipwreck", ships, 4, 15, false, "chest", 0.8);
        IGLOO = new Kind("igloo", new String[]{"igloo/top"}, 3, 5, false, "furnace", 0.8);
        String[] portals = new String[13];
        for (int i = 0; i < 10; i++) portals[i] = "ruined_portal/portal_" + (i + 1);
        for (int i = 0; i < 3; i++) portals[10 + i] = "ruined_portal/giant_portal_" + (i + 1);
        // portals age (mossy / cracked bricks, crying obsidian, missing gold and blocks), so fewer of their blocks must match
        RUINED_PORTAL = new Kind("ruined_portal", portals, -1, -1, true, "obsidian", 0.6);
        KINDS = new Kind[]{SHIPWRECK, IGLOO, RUINED_PORTAL};
    }

    static volatile boolean debug;

    private static final Map<String, List<Template>> CACHE = new HashMap<>();

    /** One template per palette (shipwrecks come in every wood type: one palette each). */
    static synchronized List<Template> templates(String id) {
        List<Template> t = CACHE.get(id);
        if (t != null) return t;
        t = new ArrayList<>();
        try (InputStream in = net.minecraft.class_310.class.getClassLoader().getResourceAsStream("data/minecraft/structure/" + id + ".nbt")) {
            if (in != null) t = parse(id, in);
        } catch (Throwable e) {
            System.out.println("Hook SeedCracker: template " + id + " " + e);
        }
        CACHE.put(id, t);
        return t;
    }

    /** Block names that are the same thing for matching (aging, waterlogging is not in the name anyway). */
    static String canon(String s) {
        if (s == null) return null;
        if (s.startsWith("minecraft:")) s = s.substring(10);
        if (s.equals("crying_obsidian")) return "obsidian";
        if (s.startsWith("mossy_")) s = s.substring(6);
        if (s.startsWith("cracked_")) s = s.substring(8);
        if (s.startsWith("infested_")) s = s.substring(9);
        if (s.equals("trapped_chest")) return "chest";
        return s;
    }

    private static boolean ignored(String n) {
        return n.equals("air") || n.equals("cave_air") || n.equals("structure_void") || n.equals("structure_block") || n.equals("jigsaw");
    }

    // ------------------------------------------------------------------ matching

    /** World position of a template block (mirror FRONT_BACK, then rotation around the pivot), like StructureTemplate.transform. */
    static int tx(int x, int z, boolean mirror, int rot, int px, int pz) {
        if (mirror) x = -x;
        return switch (rot) {
            case 1 -> px + pz - z;          // clockwise 90
            case 2 -> px + px - x;          // 180
            case 3 -> px - pz + z;          // counterclockwise 90
            default -> x;
        };
    }

    static int tz(int x, int z, boolean mirror, int rot, int px, int pz) {
        if (mirror) x = -x;
        return switch (rot) {
            case 1 -> pz - px + x;
            case 2 -> pz + pz - z;
            case 3 -> px + pz - x;
            default -> z;
        };
    }

    /** Result: chunk x, chunk z, or null (no sure fit / not enough loaded yet: retry = true). */
    static final class Hit {
        int cx, cz, matched, total;
        String template;
        boolean retry;
    }

    /**
     * Tries every template of the kind against an anchor block seen at (ax, ay, az). Returns the start chunk when exactly
     * one placement fits, or a Hit with retry = true when it may fit but too much of it is not loaded yet.
     */
    static Hit match(Kind k, World w, int ax, int ay, int az) {
        Hit best = null;
        int dbgBest = -1;
        String dbg = null;
        int bestCount = 0, secondCount = 0;
        long bestKey = Long.MIN_VALUE;
        boolean retry = false;
        for (String id : k.templates) for (Template t : templates(id)) {
            int px = k.pivotX < 0 ? t.sx / 2 : k.pivotX, pz = k.pivotZ < 0 ? t.sz / 2 : k.pivotZ;
            for (int m = 0; m < (k.mirror ? 2 : 1); m++) {
                boolean mir = m == 1;
                for (int rot = 0; rot < 4; rot++) {
                    for (int i = 0; i < t.name.length; i++) {
                        if (!t.name[i].equals(k.anchor)) continue;
                        int ox = ax - tx(t.x[i], t.z[i], mir, rot, px, pz), oz = az - tz(t.x[i], t.z[i], mir, rot, px, pz), oy = ay - t.y[i];
                        if ((ox & 15) != 0 || (oz & 15) != 0) continue;
                        int matched = 0, miss = 0, unknown = 0, n = t.name.length;
                        int maxMiss = (int) Math.floor(n * (1 - k.minRatio));
                        for (int b = 0; b < n && miss <= maxMiss; b++) {
                            String got = canon(w.block(ox + tx(t.x[b], t.z[b], mir, rot, px, pz), oy + t.y[b], oz + tz(t.x[b], t.z[b], mir, rot, px, pz)));
                            if (got == null) unknown++;
                            else if (got.equals(t.name[b])) matched++;
                            else miss++;
                        }
                        if (debug && matched > dbgBest) { dbgBest = matched; dbg = id + " rot" + rot + (mir ? "m" : "") + " " + matched + "/" + n + " miss" + miss + " unk" + unknown + " chunk " + (ox >> 4) + " " + (oz >> 4); }
                        if (miss > maxMiss) continue;
                        if (unknown > n * 0.15) { retry = true; continue; }
                        if (matched < k.minRatio * (n - unknown) || matched < 12) continue;
                        long key = ((long) (ox >> 4) << 32) ^ ((oz >> 4) & 0xFFFFFFFFL);
                        if (matched > bestCount) {
                            if (best != null && key != bestKey) secondCount = Math.max(secondCount, bestCount);
                            best = new Hit();
                            best.cx = ox >> 4;
                            best.cz = oz >> 4;
                            best.matched = matched;
                            best.total = n;
                            best.template = id;
                            bestCount = matched;
                            bestKey = key;
                        } else if (key != bestKey) {
                            secondCount = Math.max(secondCount, matched);
                        }
                    }
                }
            }
        }
        if (debug && dbg != null) System.out.println("Hook SeedCracker: match " + k.type() + " at " + ax + " " + ay + " " + az + " best " + dbg);
        // a second placement almost as good (e.g. a half ship inside a full one at another shift) = not sure: skip
        if (best != null && secondCount >= bestCount * 0.9) best = null;
        if (best == null && retry) {
            Hit h = new Hit();
            h.retry = true;
            return h;
        }
        return best;
    }

    // ------------------------------------------------------------------ tiny NBT reader (gzip, only what templates use)

    static List<Template> parse(String id, InputStream raw) throws IOException {
        DataInputStream in = new DataInputStream(new GZIPInputStream(raw));
        int type = in.readByte();
        if (type != 10) throw new IOException("not a compound");
        in.readUTF();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) read(in, 10);
        List<?> size = (List<?>) root.get("size");
        List<List<?>> palettes = new ArrayList<>();
        if (root.get("palette") instanceof List<?> one) palettes.add(one);
        else if (root.get("palettes") instanceof List<?> ps) for (Object o : ps) palettes.add((List<?>) o);
        List<Template> out = new ArrayList<>();
        for (List<?> palette : palettes) {
            List<String> pal = new ArrayList<>();
            for (Object o : palette) pal.add(canon((String) ((Map<?, ?>) o).get("Name")));
            List<int[]> pos = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (Object o : (List<?>) root.get("blocks")) {
                Map<?, ?> b = (Map<?, ?>) o;
                String n = pal.get((Integer) b.get("state"));
                if (ignored(n)) continue;
                List<?> p = (List<?>) b.get("pos");
                pos.add(new int[]{(Integer) p.get(0), (Integer) p.get(1), (Integer) p.get(2)});
                names.add(n);
            }
            out.add(new Template(id, (Integer) size.get(0), (Integer) size.get(1), (Integer) size.get(2), pos, names));
        }
        return out;
    }

    private static Object read(DataInputStream in, int type) throws IOException {
        switch (type) {
            case 1: return in.readByte();
            case 2: return in.readShort();
            case 3: return in.readInt();
            case 4: return in.readLong();
            case 5: return in.readFloat();
            case 6: return in.readDouble();
            case 7: { int n = in.readInt(); in.skipNBytes(n); return null; }
            case 8: return in.readUTF();
            case 9: {
                int et = in.readByte(), n = in.readInt();
                List<Object> l = new ArrayList<>(Math.max(0, n));
                for (int i = 0; i < n; i++) l.add(read(in, et));
                return l;
            }
            case 10: {
                Map<String, Object> m = new HashMap<>();
                while (true) {
                    int t = in.readByte();
                    if (t == 0) return m;
                    String k = in.readUTF();
                    m.put(k, read(in, t));
                }
            }
            case 11: { int n = in.readInt(); in.skipNBytes(4L * n); return null; }
            case 12: { int n = in.readInt(); in.skipNBytes(8L * n); return null; }
            default: throw new IOException("bad tag " + type);
        }
    }
}
