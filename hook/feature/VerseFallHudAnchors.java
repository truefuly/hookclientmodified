package hook.feature;

import com.spotifyclient.ModSettings;
import com.spotifyclient.hud.DragController;
import com.spotifyclient.hud.DragSystem;
import com.spotifyclient.hud.Draggable;
import com.spotifyclient.hud.HudSpace;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;

/**
 * VerseFall's HUD elements (music / lyrics bar, now playing, armor, keystrokes...) store a plain top-left position,
 * so they slide away from where they were put when the window size changes, and the lyrics bar (its width follows
 * the lyric) drifts sideways with every line. This keeps an anchor per element - left / center / right and
 * top / middle / bottom, whichever is nearest when it was placed - and re-applies it every frame, so a centered
 * element stays exactly centered and edge elements keep their distance to that edge.
 */
public final class VerseFallHudAnchors {
    private static final Map<String, float[]> ANCHORS = new HashMap<>(); // ax, ay (0 start, 1 center, 2 end), ox, oy
    private static final Map<String, Float> SCALES = new HashMap<>();
    private static final Map<DragController, String> IDS = new java.util.IdentityHashMap<>();
    private static final Set<String> DRAGGING = new HashSet<>();
    private static boolean loaded;
    private static String lastRaw;
    private static boolean broken;
    /** Size of the music bar, owned by the HookVisuals HUD layout ("Music Bar" element). */
    public static volatile float musicBarScale = 1.0f;
    public static final String MUSIC_BAR = "lyricsbar";

    /** VerseFall's lyrics bar, or null. */
    public static Draggable lyricsBar() {
        if (broken) return null;
        try {
            return DragSystem.get().byId(MUSIC_BAR);
        } catch (Throwable t) {
            return null;
        }
    }

    /** Elements placed by the HookVisuals layout instead of VerseFall's own dragging. */
    public static boolean managed(DragController g) {
        return MUSIC_BAR.equals(IDS.get(g));
    }

    private VerseFallHudAnchors() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("hook-hud-anchors.txt");
    }

    /** Called every frame. */
    public static void update() {
        if (broken) return;
        try {
            update0();
        } catch (Throwable t) {
            broken = true;
            System.err.println("Hook: HUD anchors disabled: " + t);
        }
    }

    private static void update0() {
        DragSystem ds = DragSystem.get();
        if (ds == null || !ds.drewRecently()) return;
        load();
        float sw = HudSpace.width(), sh = HudSpace.height();
        if (sw <= 0.0f || sh <= 0.0f) return;
        String raw = ModSettings.hudPositions == null ? "" : ModSettings.hudPositions;
        boolean first = lastRaw == null;
        boolean rawChanged = !first && !raw.equals(lastRaw); // moved, double-clicked home or reset in VerseFall
        lastRaw = raw;
        boolean dirty = false;
        boolean resave = false;
        for (Draggable d : ds.all()) {
            if (!d.interactive()) continue;
            DragController g = d.drag();
            String id = d.id();
            IDS.put(g, id);
            if (MUSIC_BAR.equals(id)) continue; // placed by the HookVisuals layout
            float sc = scale(id);
            float w = d.width() * sc, h = d.height() * sc;
            if (w <= 0.0f || h <= 0.0f) continue;
            if (g.dragging()) {
                DRAGGING.add(id);
                continue;
            }
            boolean released = DRAGGING.remove(id);
            float[] a = ANCHORS.get(id);
            if (released) {
                // drop near the screen's center lines: snap onto them
                float x = g.targetX(), y = g.targetY();
                if (Math.abs(x + w / 2.0f - sw / 2.0f) < 6.0f) x = sw / 2.0f - w / 2.0f;
                if (Math.abs(y + h / 2.0f - sh / 2.0f) < 6.0f) y = sh / 2.0f - h / 2.0f;
                if (x != g.targetX() || y != g.targetY()) {
                    g.setTarget(x, y);
                    resave = true;
                }
            }
            if (released || a == null || rawChanged) {
                ANCHORS.put(id, anchorOf(g.targetX(), g.targetY(), w, h, sw, sh));
                dirty = true;
                continue;
            }
            float x = place(a[0], a[2], w, sw);
            float y = place(a[1], a[3], h, sh);
            x = Math.max(0.0f, Math.min(x, sw - w));
            y = Math.max(0.0f, Math.min(y, sh - h));
            if (Math.abs(x - g.targetX()) > 0.25f || Math.abs(y - g.targetY()) > 0.25f) {
                g.setTarget(x, y);
                g.syncToTarget();
            }
        }
        if (resave) {
            ds.savePositions();
            lastRaw = ModSettings.hudPositions;
        }
        if (dirty) save();
    }

    private static float[] anchorOf(float x, float y, float w, float h, float sw, float sh) {
        float cx = x + w / 2.0f - sw / 2.0f, cy = y + h / 2.0f - sh / 2.0f;
        float ax = Math.abs(cx) < sw / 6.0f ? 1 : cx > 0.0f ? 2 : 0;
        float ay = Math.abs(cy) < sh / 6.0f ? 1 : cy > 0.0f ? 2 : 0;
        float ox = ax == 1 ? cx : ax == 2 ? sw - x - w : x;
        float oy = ay == 1 ? cy : ay == 2 ? sh - y - h : y;
        return new float[]{ax, ay, ox, oy};
    }

    private static float place(float anchor, float offset, float size, float screen) {
        if (anchor == 1) return screen / 2.0f + offset - size / 2.0f;
        if (anchor == 2) return screen - offset - size;
        return offset;
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        try {
            Path f = file();
            if (!Files.isRegularFile(f)) return;
            for (String line : Files.readAllLines(f, StandardCharsets.UTF_8)) {
                String[] p = line.trim().split(",");
                if (p.length == 3 && p[0].equals("SCALE")) {
                    try { SCALES.put(p[1], Float.parseFloat(p[2])); } catch (NumberFormatException ignored) { }
                    continue;
                }
                if (p.length != 5) continue;
                try {
                    ANCHORS.put(p[0], new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3]), Float.parseFloat(p[4])});
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void save() {
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, float[]> e : ANCHORS.entrySet()) {
                float[] a = e.getValue();
                sb.append(e.getKey()).append(',').append((int) a[0]).append(',').append((int) a[1]).append(',')
                        .append(String.format(Locale.ROOT, "%.1f,%.1f", a[2], a[3])).append('\n');
            }
            for (Map.Entry<String, Float> e : SCALES.entrySet()) {
                sb.append("SCALE,").append(e.getKey()).append(',').append(String.format(Locale.ROOT, "%.2f", e.getValue())).append("\n");
            }
            Path f = file();
            Files.createDirectories(f.getParent());
            Files.writeString(f, sb.toString(), StandardCharsets.UTF_8);
        } catch (Throwable ignored) {
        }
    }

    /** HUD editor "Reset HUD": forget the anchors (VerseFall positions are reset too). */
    public static void reset() {
        ANCHORS.clear();
        SCALES.clear();
        try {
            DragSystem.get().resetPositions();
            lastRaw = ModSettings.hudPositions;
        } catch (Throwable ignored) {
        }
        save();
    }

    static float scale(String id) {
        if (MUSIC_BAR.equals(id)) return musicBarScale;
        Float s = SCALES.get(id);
        return s == null ? 1.0f : s;
    }

    /** Size of a VerseFall element (by its drag controller) - used by the mixins for drawing and hit tests. */
    public static float scaleOf(DragController g) {
        String id = IDS.get(g);
        return id == null ? 1.0f : scale(id);
    }

    public static float scaleOf(String id) {
        return scale(id);
    }

    /** Mouse wheel in the HUD editor over a VerseFall element: resize it (scaled around its top-left corner). */
    public static boolean scrollAtMouse(float step) {
        if (broken) return false;
        try {
            DragSystem ds = DragSystem.get();
            float mx = HudSpace.mouseX(), my = HudSpace.mouseY();
            java.util.List<Draggable> all = ds.all();
            for (int i = all.size() - 1; i >= 0; i--) {
                Draggable d = all.get(i);
                if (!d.interactive() || MUSIC_BAR.equals(d.id())) continue;
                float sc = scale(d.id());
                float x = d.drag().targetX(), y = d.drag().targetY();
                if (mx < x || my < y || mx >= x + d.width() * sc || my >= y + d.height() * sc) continue;
                float ns = Math.max(0.5f, Math.min(2.5f, Math.round((sc + step) * 20.0f) / 20.0f));
                SCALES.put(d.id(), ns);
                // keep its anchor point: re-anchor from the current position with the new size
                ANCHORS.put(d.id(), anchorOf(x, y, d.width() * ns, d.height() * ns, HudSpace.width(), HudSpace.height()));
                save();
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }
}
