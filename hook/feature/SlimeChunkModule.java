package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.RgbSetting;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;
import hook.utils.RenderUtils;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1589;
import net.minecraft.class_1621;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_4184;
import net.minecraft.class_4587;

/**
 * Slime chunks. With the world seed (automatic in singleplayer, or typed in) every slime chunk is known exactly. On a
 * server without the seed, chunks are found the way slimes give them away: a slime below Y 40 can only spawn in a
 * slime chunk, so its chunk gets marked (and remembered per server).
 */
public final class SlimeChunkModule extends Module {
    private final StringSetting seed = new StringSetting(EncryptedString.of("Seed"), "");
    private final BooleanSetting learn = new BooleanSetting(EncryptedString.of("Find From Slimes"), true);
    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range (chunks)"), 1.0, 16.0, 6.0, 1.0);
    private final RgbSetting color = new RgbSetting(EncryptedString.of("Color"), 0xFF6BE36B);
    private final NumberSetting fill = new NumberSetting(EncryptedString.of("Fill Opacity"), 0.0, 160.0, 45.0, 5.0);
    private final BooleanSetting throughWalls = new BooleanSetting(EncryptedString.of("Through Walls"), true);
    private final BooleanSetting notify = new BooleanSetting(EncryptedString.of("Notify"), true);

    private final Set<Long> found = new HashSet<>();
    private String foundKey = "";
    private long lastChunk = Long.MIN_VALUE;
    private boolean hinted;

    public SlimeChunkModule() {
        super(EncryptedString.of("Slime Chunks"), EncryptedString.of("Highlights slime chunks around you. Exact with the seed (automatic in singleplayer); on servers it finds them from slimes below Y 40."), -1, Category.RENDER);
        this.seed.setDescription(EncryptedString.of("World seed for exact slime chunks on a server (number, or the text seed). Empty = singleplayer seed / find from slimes."));
        this.learn.setDescription(EncryptedString.of("Marks every chunk where you see a slime below Y 40 - they can only spawn there in slime chunks. Remembered per server."));
        this.range.setDescription(EncryptedString.of("How many chunks around you are checked."));
        this.color.setDescription(EncryptedString.of("Colour of the slime chunk squares."));
        this.fill.setDescription(EncryptedString.of("How solid the square on the floor is. 0 = outline only."));
        this.throughWalls.setDescription(EncryptedString.of("On: outlines visible through blocks. Off: hidden behind blocks."));
        this.notify.setDescription(EncryptedString.of("Action bar note when you walk into a slime chunk, and when a new one is found."));
        this.addSettings(this.seed, this.learn, this.range, this.color, this.fill, this.throughWalls, this.notify);
        this.moreSettings(this.range, this.fill, this.throughWalls, this.notify);
    }

    /** Seed Cracker result: used when no seed is typed in yet (slime chunks only need the lower 48 bits). */
    public void offerSeed(long s) {
        String v = this.seed.getValue();
        if (v == null || v.isBlank()) this.seed.setValue(Long.toString(s));
    }

    /** Vanilla slime chunk rule (WorldgenRandom.seedSlimeChunk(x, z, seed, 987234911L).nextInt(10) == 0). */
    static boolean isSlimeChunk(long seed, int x, int z) {
        Random r = new Random(seed + (long) (x * x * 4987142) + (long) (x * 5947611) + (long) (z * z) * 4392871L + (long) (z * 389711) ^ 987234911L);
        return r.nextInt(10) == 0;
    }

    /** The seed to use, or null: typed seed first, then the singleplayer world's own seed. */
    private Long seed() {
        String s = this.seed.getValue() == null ? "" : this.seed.getValue().trim();
        if (!s.isEmpty()) {
            try { return Long.parseLong(s); } catch (NumberFormatException e) { return (long) s.hashCode(); } // text seeds like vanilla
        }
        try {
            if (this.mc.method_1576() != null) return this.mc.method_1576().method_30002().method_8412();
        } catch (Throwable ignored) {}
        return null;
    }

    private boolean overworld() {
        return this.mc.field_1687 != null && this.mc.field_1687.method_27983() == class_1937.field_25179;
    }

    private String serverKey() {
        if (this.mc.method_1576() != null) return "";
        return this.mc.method_1558() == null ? "" : String.valueOf(this.mc.method_1558().field_3761).toLowerCase(java.util.Locale.ROOT);
    }

    private static Path store() {
        return Path.of("config", "hook-slimechunks.txt");
    }

    /** Loads the chunks found earlier on this server (file lines: server x z). */
    private void syncStore() {
        String key = this.serverKey();
        if (key.equals(this.foundKey)) return;
        this.foundKey = key;
        this.found.clear();
        this.hinted = false;
        if (key.isEmpty()) return;
        try {
            if (!Files.exists(store())) return;
            for (String line : Files.readAllLines(store(), StandardCharsets.UTF_8)) {
                String[] p = line.trim().split(" ");
                if (p.length == 3 && p[0].equals(key)) this.found.add(pack(Integer.parseInt(p[1]), Integer.parseInt(p[2])));
            }
        } catch (Throwable ignored) {}
    }

    private void remember(int x, int z) {
        if (this.foundKey.isEmpty()) return;
        try {
            Files.createDirectories(store().getParent());
            Files.writeString(store(), this.foundKey + " " + x + " " + z + "\n", StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Throwable ignored) {}
    }

    private static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private boolean slime(Long seed, int x, int z) {
        if (seed != null) return isSlimeChunk(seed, x, z);
        return this.found.contains(pack(x, z));
    }

    private void bar(String msg) {
        if (this.notify.getValue() && this.mc.field_1724 != null) this.mc.field_1724.method_7353(class_2561.method_43470("§aSlime Chunks: §f" + msg), true);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) return;
        this.syncStore();
        if (!this.overworld()) return;
        Long seed = this.seed();
        if (seed == null && !this.hinted) {
            this.hinted = true;
            this.bar("no seed - marking chunks where slimes show up below Y 40");
        }
        if (seed == null && this.learn.getValue()) {
            for (class_1297 e : this.mc.field_1687.method_18112()) {
                if (!(e instanceof class_1621) || e instanceof class_1589) continue; // magma cubes are slimes too
                if (e.method_23318() >= 40.0) continue; // swamp slimes spawn higher up
                int cx = (int) Math.floor(e.method_23317()) >> 4, cz = (int) Math.floor(e.method_23321()) >> 4;
                if (this.found.add(pack(cx, cz))) {
                    this.remember(cx, cz);
                    this.bar("found a slime chunk at " + (cx << 4) + ", " + (cz << 4));
                }
            }
        }
        int pcx = (int) Math.floor(this.mc.field_1724.method_23317()) >> 4, pcz = (int) Math.floor(this.mc.field_1724.method_23321()) >> 4;
        long here = pack(pcx, pcz);
        if (here != this.lastChunk) {
            this.lastChunk = here;
            if (this.slime(seed, pcx, pcz)) this.bar("you are in a slime chunk");
        }
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null || !this.overworld()) return;
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null || !camera.method_19332()) return;
        Long seed = this.seed();
        if (seed == null && this.found.isEmpty()) return;
        double feet = this.mc.field_1724.method_30950(camera.method_55437()).field_1351;
        double y = Math.floor(feet) + 0.02;
        int pcx = (int) Math.floor(this.mc.field_1724.method_23317()) >> 4, pcz = (int) Math.floor(this.mc.field_1724.method_23321()) >> 4;
        int r = this.range.getIntValue();
        int argb = this.color.getArgb();
        int cr = (argb >> 16) & 255, cg = (argb >> 8) & 255, cb = argb & 255;
        List<RenderUtils.ColoredBox> boxes = new ArrayList<>();
        List<RenderUtils.ColoredLine> lines = new ArrayList<>();
        int fillA = this.fill.getIntValue();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int cx = pcx + dx, cz = pcz + dz;
                if (!this.slime(seed, cx, cz)) continue;
                boolean here = dx == 0 && dz == 0;
                double x1 = (cx << 4) + 0.05, z1 = (cz << 4) + 0.05, x2 = (cx << 4) + 15.95, z2 = (cz << 4) + 15.95;
                if (fillA > 0) boxes.add(new RenderUtils.ColoredBox(x1, y, z1, x2, y + 0.02, z2, new Color(cr, cg, cb, Math.min(255, here ? fillA + 30 : fillA))));
                Color lc = new Color(cr, cg, cb, here ? 255 : 200);
                float w = here ? 2.5f : 1.6f;
                class_243 a = new class_243(x1, y, z1), b = new class_243(x2, y, z1), c = new class_243(x2, y, z2), d = new class_243(x1, y, z2);
                lines.add(new RenderUtils.ColoredLine(a, b, lc, w));
                lines.add(new RenderUtils.ColoredLine(b, c, lc, w));
                lines.add(new RenderUtils.ColoredLine(c, d, lc, w));
                lines.add(new RenderUtils.ColoredLine(d, a, lc, w));
            }
        }
        if (boxes.isEmpty() && lines.isEmpty()) return;
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        RenderUtils.applyCameraTransform(matrices, camera);
        RenderUtils.renderFilledBoxes(matrices, boxes);
        RenderUtils.renderLines(matrices, lines, this.throughWalls.getValue());
        matrices.method_22909();
    }
}
