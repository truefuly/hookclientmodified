package hook.feature;

import hook.event.EventListener;
import hook.event.events.PacketReceiveEvent;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.RgbSetting;
import hook.utils.EncryptedString;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;

/** The Base Finder modules (own category). All of them read the shared {@link BaseFinder} chunk scan. */
public final class BaseFinderModules {
    private BaseFinderModules() {
    }

    /** Common settings + drawing for a "mark these blocks" module. */
    abstract static class Esp extends Module {
        final RgbSetting color;
        final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 16.0, 512.0, 160.0, 16.0);
        final NumberSetting fill = new NumberSetting(EncryptedString.of("Fill Opacity"), 0.0, 160.0, 40.0, 5.0);
        final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), false);
        final BooleanSetting walls = new BooleanSetting(EncryptedString.of("Through Walls"), true);
        final BooleanSetting alerts = new BooleanSetting(EncryptedString.of("Chat Alerts"), true);
        final Set<Long> alerted = new HashSet<>();

        Esp(String name, String desc, int argb) {
            super(EncryptedString.of(name), EncryptedString.of(desc), -1, Category.DONUT);
            this.color = new RgbSetting(EncryptedString.of("Color"), argb);
            this.range.setDescription(EncryptedString.of("Only marks things within this many blocks."));
            this.fill.setDescription(EncryptedString.of("How solid the boxes are. 0 = outline only."));
            this.tracers.setDescription(EncryptedString.of("Lines from your crosshair to every find."));
            this.walls.setDescription(EncryptedString.of("Visible through blocks."));
            this.alerts.setDescription(EncryptedString.of("Chat message with coordinates for every new find."));
        }

        boolean needAir() {
            return false;
        }

        @EventListener
        public void onTick(TickEvent event) {
            BaseFinder.tick(this.needAir());
            this.check();
        }

        void check() {
        }

        @EventListener
        public void onPacket(PacketReceiveEvent event) {
            BaseFinder.onPacket(event.packet);
        }

        boolean inRange(class_2338 p) {
            double r = this.range.getValue();
            return BaseFinder.distSq(p) <= r * r;
        }

        void alertOnce(long key, String msg) {
            if (this.alerts.getValue() && this.alerted.add(key)) BaseFinder.chat(String.valueOf(this.getName()), msg);
        }

        @Override
        public void onDisable() {
            this.alerted.clear();
            super.onDisable();
        }
    }

    /** Marks every block of the given kinds in range (nearest 2000). */
    static void drawKinds(Esp m, BaseFinder.Draw d, Color c, int... kinds) {
        int n = 0;
        for (BaseFinder.ChunkData cd : BaseFinder.all()) {
            for (int k : kinds) {
                for (class_2338 p : cd.hits[k]) {
                    if (!m.inRange(p)) continue;
                    d.block(p, c, m.fill.getIntValue());
                    if (m.tracers.getValue()) d.tracer(class_243.method_24953(p), c);
                    if (++n > 2000) return;
                }
            }
        }
    }

    // =================================================================== 1. Base Blocks
    public static final class BaseBlocks extends Esp {
        private final NumberSetting minScore = new NumberSetting(EncryptedString.of("Base Score"), 2.0, 40.0, 6.0, 1.0);
        private final BooleanSetting chunks = new BooleanSetting(EncryptedString.of("Highlight Chunks"), true);
        private final BooleanSetting blocks = new BooleanSetting(EncryptedString.of("Show Blocks"), true);

        public BaseBlocks() {
            super("Base ESP", "Finds player-made blocks (beds, crafting/furnaces, redstone, concrete, glass, beacons, anvils...) and scores every chunk - many of them together = a base.", 0xFFFF5A5A);
            this.minScore.setDescription(EncryptedString.of("How many player blocks a chunk needs to count as a base (chunk highlight + alert)."));
            this.chunks.setDescription(EncryptedString.of("Colours whole chunks that reach the base score."));
            this.blocks.setDescription(EncryptedString.of("Boxes around every single player block."));
            this.addSettings(this.color, this.minScore, this.chunks, this.blocks, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        int score(BaseFinder.ChunkData cd) {
            return cd.count(BaseFinder.BASE) + cd.count(BaseFinder.STORAGE) / 2 + cd.count(BaseFinder.LIGHT) / 3 + cd.count(BaseFinder.SIGN);
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                int s = this.score(cd);
                if (s >= this.minScore.getIntValue())
                    this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "possible base at " + ((cd.cx << 4) + 8) + ", " + ((cd.cz << 4) + 8) + " (score " + s + ", " + cd.count(BaseFinder.BASE) + " player blocks)");
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 230);
            if (this.blocks.getValue()) drawKinds(this, d, c, BaseFinder.BASE);
            if (this.chunks.getValue()) {
                double y = BaseFinder.feetY();
                for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                    int s = this.score(cd);
                    if (s >= this.minScore.getIntValue()) d.chunk(cd.cx, cd.cz, y, c, Math.min(120, 30 + s * 4));
                }
            }
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 2. Storage
    public static final class Storage extends Esp {
        private final BooleanSetting entities = new BooleanSetting(EncryptedString.of("Chest Minecarts & Donkeys"), true);
        private final NumberSetting minChunk = new NumberSetting(EncryptedString.of("Stash Alert"), 2.0, 60.0, 8.0, 1.0);

        public Storage() {
            super("Storage ESP", "Chests, barrels, shulkers, ender chests, hoppers, furnaces... plus chest minecarts and donkeys/llamas with chests. Alerts on stashes.", 0xFFFFB347);
            this.minChunk.setDescription(EncryptedString.of("This many containers in one chunk = stash alert."));
            this.addSettings(this.color, this.entities, this.minChunk, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                int n = cd.count(BaseFinder.STORAGE);
                if (n >= this.minChunk.getIntValue()) this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "stash: " + n + " containers at " + ((cd.cx << 4) + 8) + ", " + ((cd.cz << 4) + 8));
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 230);
            int n = 0;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                for (class_2338 p : cd.hits[BaseFinder.STORAGE]) {
                    if (!this.inRange(p)) continue;
                    String id = BaseFinder.id(this.mc.field_1687.method_8320(p).method_26204());
                    Color k = id.contains("shulker") ? new Color(200, 110, 255, 235) : id.equals("ender_chest") ? new Color(60, 210, 170, 235) : id.contains("chest") || id.equals("barrel") ? c : new Color(170, 170, 170, 200);
                    d.block(p, k, this.fill.getIntValue());
                    if (this.tracers.getValue()) d.tracer(class_243.method_24953(p), k);
                    if (++n > 2000) break;
                }
            }
            if (this.entities.getValue() && this.mc.field_1687 != null) {
                for (class_1297 e : this.mc.field_1687.method_18112()) {
                    String t = entityId(e);
                    boolean chested = t.equals("chest_minecart") || t.equals("hopper_minecart") || t.equals("furnace_minecart") || t.endsWith("chest_boat")
                        || (e instanceof net.minecraft.class_1492 h && h.method_6703());
                    if (!chested || e.method_5707(this.mc.field_1724.method_33571()) > this.range.getValue() * this.range.getValue()) continue;
                    net.minecraft.class_238 b = e.method_5829();
                    d.box(b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324, c, this.fill.getIntValue());
                }
            }
            d.render(event, this.walls.getValue());
        }
    }

    static String entityId(class_1297 e) {
        try { return net.minecraft.class_7923.field_41177.method_10221(e.method_5864()).method_12832(); } catch (Throwable t) { return "?"; }
    }

    // =================================================================== 3. New Chunks
    public static final class NewChunks extends Module {
        private final RgbSetting newColor = new RgbSetting(EncryptedString.of("New Color"), 0xFFFF4040);
        private final RgbSetting oldColor = new RgbSetting(EncryptedString.of("Old Color"), 0xFF40FF70);
        private final BooleanSetting showOld = new BooleanSetting(EncryptedString.of("Show Old"), true);
        private final NumberSetting fill = new NumberSetting(EncryptedString.of("Fill Opacity"), 0.0, 160.0, 35.0, 5.0);
        private final NumberSetting height = new NumberSetting(EncryptedString.of("Height"), -64.0, 320.0, 0.0, 8.0);
        private final BooleanSetting atFeet = new BooleanSetting(EncryptedString.of("At Your Feet"), true);
        private final Set<Long> fresh = new HashSet<>();
        private final Set<Long> old = new HashSet<>();
        private Object world;

        public NewChunks() {
            super(EncryptedString.of("New Chunks"), EncryptedString.of("New vs. old chunks by liquid flow: water/lava that starts flowing when a chunk loads = it was just generated (nobody was here). Old chunks along a line = a travel trail."), -1, Category.DONUT);
            this.showOld.setDescription(EncryptedString.of("Also draw chunks that were already generated before (someone was here)."));
            this.height.setDescription(EncryptedString.of("Height of the squares (when not at your feet)."));
            this.addSettings(this.newColor, this.oldColor, this.showOld, this.fill, this.atFeet, this.height);
        }

        @Override
        public void onDisable() {
            this.fresh.clear();
            this.old.clear();
            super.onDisable();
        }

        @EventListener
        public void onTick(TickEvent event) {
            if (this.mc.field_1687 != this.world) { this.world = this.mc.field_1687; this.fresh.clear(); this.old.clear(); }
        }

        @EventListener
        public void onPacket(PacketReceiveEvent event) {
            Object pk = event.packet;
            if (pk instanceof net.minecraft.class_8038<?> bundle) { for (Object s : bundle.method_48324()) this.handle(s); return; }
            this.handle(pk);
        }

        private void handle(Object pk) {
            if (pk instanceof net.minecraft.class_2626 u) this.fluid(u.method_11309(), u.method_11308());
            else if (pk instanceof net.minecraft.class_2637 s) s.method_30621((pos, st) -> this.fluid(pos, st));
            else if (pk instanceof net.minecraft.class_2672 cd) {
                long k = BaseFinder.key(cd.method_11523(), cd.method_11524());
                // checked a moment later, once the chunk is in the world: flowing liquid already there = old chunk
                this.mc.execute(() -> this.checkLoaded(cd.method_11523(), cd.method_11524(), k));
            }
        }

        private void fluid(class_2338 pos, class_2680 st) {
            if (st.method_26227().method_15769() || st.method_26227().method_15771()) return;
            long k = BaseFinder.key(pos.method_10263() >> 4, pos.method_10260() >> 4);
            synchronized (this.fresh) { if (!this.old.contains(k)) this.fresh.add(k); }
        }

        private void checkLoaded(int cx, int cz, long k) {
            if (this.mc.field_1687 == null) return;
            Object c = this.mc.field_1687.method_2935().method_2857(cx, cz, net.minecraft.class_2806.field_12803, false);
            if (!(c instanceof net.minecraft.class_2818 chunk)) return;
            net.minecraft.class_2826[] secs = chunk.method_12006();
            for (net.minecraft.class_2826 s : secs) {
                if (s == null || s.method_38292()) continue;
                if (s.method_19523(o -> { class_2680 b = (class_2680) o; return !b.method_26227().method_15769() && !b.method_26227().method_15771(); })) {
                    synchronized (this.fresh) { if (!this.fresh.contains(k)) this.old.add(k); }
                    return;
                }
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            double y = this.atFeet.getValue() ? BaseFinder.feetY() : this.height.getValue();
            Color nc = BaseFinder.col(this.newColor.getArgb(), 220), oc = BaseFinder.col(this.oldColor.getArgb(), 200);
            int f = this.fill.getIntValue();
            synchronized (this.fresh) {
                for (long k : this.fresh) d.chunk(net.minecraft.class_1923.method_8325(k), net.minecraft.class_1923.method_8332(k), y, nc, f);
                if (this.showOld.getValue()) for (long k : this.old) d.chunk(net.minecraft.class_1923.method_8325(k), net.minecraft.class_1923.method_8332(k), y, oc, f);
            }
            d.render(event, true);
        }
    }

    // =================================================================== 4. Growth (chunk age)
    public static final class Growth extends Esp {
        private final NumberSetting kelpPct = new NumberSetting(EncryptedString.of("Kelp Grown %"), 50.0, 100.0, 85.0, 5.0);
        private final BooleanSetting chunks = new BooleanSetting(EncryptedString.of("Highlight Chunks"), true);
        private final BooleanSetting marks = new BooleanSetting(EncryptedString.of("Mark Grown Plants"), false);

        public Growth() {
            super("Growth Detector", "Plants only grow while a player keeps the chunk loaded: kelp reaching the surface, fully grown amethyst, ripe berries everywhere = this area was lived in for a long time.", 0xFFB06BFF);
            this.kelpPct.setDescription(EncryptedString.of("Share of kelp columns that reached the water surface for a chunk to count as 'lived in'. Fresh chunks have short kelp."));
            this.chunks.setDescription(EncryptedString.of("Colours lived-in chunks."));
            this.marks.setDescription(EncryptedString.of("Boxes the grown kelp tops / amethyst clusters / ripe berries."));
            this.addSettings(this.color, this.kelpPct, this.chunks, this.marks, this.range, this.fill, this.walls, this.alerts);
        }

        /** 0 = no sign, else a short reason. */
        String reason(BaseFinder.ChunkData cd) {
            int kelp = cd.count(BaseFinder.KELP), top = cd.count(BaseFinder.KELP_TOP);
            if (kelp >= 4 && top * 100 >= kelp * this.kelpPct.getIntValue()) return "kelp grown " + top + "/" + kelp;
            int full = cd.count(BaseFinder.AMETHYST_FULL), bud = cd.count(BaseFinder.AMETHYST_BUD);
            if (full >= 6 && full >= bud * 3) return "amethyst grown " + full + " clusters / " + bud + " buds";
            int ber = cd.count(BaseFinder.BERRY), ripe = cd.count(BaseFinder.BERRY_RIPE);
            if (ber >= 6 && ripe * 100 >= ber * 90) return "berries ripe " + ripe + "/" + ber;
            return null;
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                String r = this.reason(cd);
                if (r != null) this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "lived-in chunk at " + ((cd.cx << 4) + 8) + ", " + ((cd.cz << 4) + 8) + " (" + r + ")");
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 220);
            double y = BaseFinder.feetY();
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (this.reason(cd) == null) continue;
                if (this.chunks.getValue()) d.chunk(cd.cx, cd.cz, y, c, this.fill.getIntValue());
                if (this.marks.getValue()) {
                    for (int k : new int[]{BaseFinder.KELP_TOP, BaseFinder.AMETHYST_FULL, BaseFinder.BERRY_RIPE})
                        for (class_2338 p : cd.hits[k]) if (this.inRange(p)) d.block(p, c, 0);
                }
            }
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 5. Light
    public static final class Light extends Esp {
        private final BooleanSetting sources = new BooleanSetting(EncryptedString.of("Placed Light"), true);
        private final BooleanSetting hidden = new BooleanSetting(EncryptedString.of("Lit Caves"), true);
        private final NumberSetting minLit = new NumberSetting(EncryptedString.of("Lit Cave Blocks"), 2.0, 60.0, 6.0, 1.0);

        public Light() {
            super("Light ESP", "Torches, lanterns, glowstone, sea lanterns, froglights, candles... that players place, and dark caves with block light but no natural source (torches hidden behind walls / in tunnels).", 0xFFFFE066);
            this.sources.setDescription(EncryptedString.of("Boxes every placed light source."));
            this.hidden.setDescription(EncryptedString.of("Marks cave air that is lit (light 9+) in chunks without lava/glow lichen/magma: someone put light there, even if you can't see it."));
            this.minLit.setDescription(EncryptedString.of("Lit cave spots a chunk needs before it is marked."));
            this.addSettings(this.color, this.sources, this.hidden, this.minLit, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        @Override
        boolean needAir() {
            return this.hidden.getValue();
        }

        boolean litCave(BaseFinder.ChunkData cd) {
            if (cd.count(BaseFinder.LIT_CAVE) < this.minLit.getIntValue()) return false;
            // natural light in this chunk or right next to it explains it
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                BaseFinder.ChunkData n = BaseFinder.get(cd.cx + dx, cd.cz + dz);
                if (n != null && n.count(BaseFinder.NATURAL_LIGHT) > 0) return false;
            }
            return true;
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                int below = 0;
                for (class_2338 p : cd.hits[BaseFinder.LIGHT]) if (p.method_10264() < 40) below++;
                if (below >= 3) this.alertOnce(BaseFinder.key(cd.cx, cd.cz), below + " light sources underground at " + ((cd.cx << 4) + 8) + ", " + ((cd.cz << 4) + 8));
                else if (this.hidden.getValue() && this.litCave(cd)) this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "lit cave (hidden light) at " + ((cd.cx << 4) + 8) + ", " + ((cd.cz << 4) + 8));
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 230);
            if (this.sources.getValue()) drawKinds(this, d, c, BaseFinder.LIGHT);
            if (this.hidden.getValue()) {
                Color h = new Color(255, 150, 40, 200);
                for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                    if (!this.litCave(cd)) continue;
                    int n = 0;
                    for (class_2338 p : cd.hits[BaseFinder.LIT_CAVE]) {
                        if (!this.inRange(p)) continue;
                        d.box(p.method_10263() + 0.35, p.method_10264() + 0.35, p.method_10260() + 0.35, p.method_10263() + 0.65, p.method_10264() + 0.65, p.method_10260() + 0.65, h, 60);
                        if (++n > 80) break;
                    }
                }
            }
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 6. Portals
    public static final class Portals extends Esp {
        public Portals() {
            super("Portal ESP", "Lit nether portals (ruined portals are never lit - a lit one was made by a player), end portals, frames with eyes and end gateways.", 0xFFB050FF);
            this.addSettings(this.color, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (cd.count(BaseFinder.PORTAL) == 0) continue;
                class_2338 p = cd.hits[BaseFinder.PORTAL].get(0);
                this.alertOnce(BaseFinder.key(cd.cx, cd.cz), BaseFinder.id(this.mc.field_1687.method_8320(p).method_26204()).replace('_', ' ') + " at " + p.method_10263() + ", " + p.method_10264() + ", " + p.method_10260());
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            drawKinds(this, d, BaseFinder.col(this.color.getArgb(), 230), BaseFinder.PORTAL);
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 7. Signs
    public static final class Signs extends Esp {
        private final BooleanSetting log = new BooleanSetting(EncryptedString.of("Log Text"), true);

        public Signs() {
            super("Sign ESP", "Boxes every sign through walls and writes its text with coordinates in chat (bases are full of signs: names, warnings, shop labels).", 0xFF8BD3FF);
            this.log.setDescription(EncryptedString.of("Each sign's text goes into chat once."));
            this.addSettings(this.color, this.log, this.range, this.fill, this.tracers, this.walls);
        }

        @Override
        void check() {
            if (!this.log.getValue() || this.mc.field_1687 == null) return;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                for (class_2338 p : cd.hits[BaseFinder.SIGN]) {
                    if (this.alerted.contains(p.method_10063())) continue;
                    if (!(this.mc.field_1687.method_8321(p) instanceof net.minecraft.class_2625 sign)) continue;
                    String text = signText(sign.method_49853()) + " " + signText(sign.method_49854());
                    this.alerted.add(p.method_10063());
                    if (text.isBlank()) continue;
                    BaseFinder.chat("Sign", p.method_10263() + ", " + p.method_10264() + ", " + p.method_10260() + ": §e" + text.trim());
                }
            }
        }

        private static String signText(net.minecraft.class_8242 t) {
            StringBuilder sb = new StringBuilder();
            for (net.minecraft.class_2561 line : t.method_49877(false)) {
                String s = line.getString().trim();
                if (!s.isEmpty()) sb.append(sb.length() > 0 ? " | " : "").append(s);
            }
            return sb.toString();
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            drawKinds(this, d, BaseFinder.col(this.color.getArgb(), 230), BaseFinder.SIGN);
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 8. Entity clues
    public static final class EntityClues extends Esp {
        private final BooleanSetting frames = new BooleanSetting(EncryptedString.of("Item Frames & Armor Stands"), true);
        private final BooleanSetting pets = new BooleanSetting(EncryptedString.of("Tamed / Named / Leashed"), true);
        private final BooleanSetting farms = new BooleanSetting(EncryptedString.of("Animal Farms"), true);
        private final NumberSetting farmSize = new NumberSetting(EncryptedString.of("Farm Size"), 4.0, 40.0, 10.0, 1.0);
        private final BooleanSetting misc = new BooleanSetting(EncryptedString.of("Crystals, Boats, Carts"), true);

        public EntityClues() {
            super("Entity Clues", "Entities only players leave behind: item frames, armor stands, tamed pets, name-tagged or leashed mobs, end crystals, parked boats/minecarts and animal farms (many of one kind packed together).", 0xFF5AC8FF);
            this.farmSize.setDescription(EncryptedString.of("This many animals of one kind within 6 blocks = farm."));
            this.addSettings(this.color, this.frames, this.pets, this.farms, this.farmSize, this.misc, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        private boolean clue(class_1297 e, String id) {
            if (this.frames.getValue() && (id.equals("item_frame") || id.equals("glow_item_frame") || id.equals("armor_stand") || id.equals("painting"))) return true;
            if (this.pets.getValue()) {
                if (e instanceof net.minecraft.class_1321 t && t.method_6181()) return true;
                if (!(e instanceof net.minecraft.class_1657) && e.method_5797() != null) return true;
                if (e instanceof net.minecraft.class_9817 l && l.method_60953()) return true;
            }
            if (this.misc.getValue() && (id.equals("end_crystal") || id.endsWith("_boat") || id.endsWith("_raft") || id.equals("minecart") || id.endsWith("_minecart"))) return true;
            return false;
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            if (this.mc.field_1687 == null || this.mc.field_1724 == null) return;
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 230);
            double r2 = this.range.getValue() * this.range.getValue();
            Map<String, java.util.List<class_1297>> animals = new HashMap<>();
            for (class_1297 e : this.mc.field_1687.method_18112()) {
                if (e == this.mc.field_1724 || e.method_5707(this.mc.field_1724.method_33571()) > r2) continue;
                String id = entityId(e);
                if (this.clue(e, id)) {
                    net.minecraft.class_238 b = e.method_5829();
                    d.box(b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324, c, this.fill.getIntValue());
                    if (this.tracers.getValue()) d.tracer(new class_243(e.method_23317(), e.method_23318(), e.method_23321()), c);
                }
                if (this.farms.getValue() && e instanceof net.minecraft.class_1429) animals.computeIfAbsent(id, k -> new java.util.ArrayList<>()).add(e);
            }
            if (this.farms.getValue()) {
                Color fc = new Color(255, 230, 90, 220);
                for (Map.Entry<String, java.util.List<class_1297>> en : animals.entrySet()) {
                    List<class_1297> list = en.getValue();
                    if (list.size() < this.farmSize.getIntValue()) continue;
                    for (class_1297 a : list) {
                        int near = 0;
                        for (class_1297 b : list) if (a.method_5858(b) < 36.0) near++;
                        if (near >= this.farmSize.getIntValue()) {
                            net.minecraft.class_238 bb = a.method_5829().method_1014(2.5);
                            d.box(bb.field_1323, bb.field_1322, bb.field_1321, bb.field_1320, bb.field_1325, bb.field_1324, fc, 0);
                            this.alertOnce(BaseFinder.key((int) a.method_23317() >> 4, (int) a.method_23321() >> 4), en.getKey() + " farm (" + near + ") at " + (int) a.method_23317() + ", " + (int) a.method_23318() + ", " + (int) a.method_23321());
                            break;
                        }
                    }
                }
            }
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 9. Tunnels
    public static final class Tunnels extends Esp {
        public Tunnels() {
            super("Tunnel Finder", "Dead-straight 1x2 tunnels underground (8+ blocks, solid floor, roof and walls) - caves never look like that; strip mines and travel tunnels do.", 0xFF4FA3FF);
            this.addSettings(this.color, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        @Override
        boolean needAir() {
            return true;
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (cd.count(BaseFinder.TUNNEL) < 4) continue;
                class_2338 p = cd.hits[BaseFinder.TUNNEL].get(0);
                this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "player tunnel at " + p.method_10263() + ", " + p.method_10264() + ", " + p.method_10260());
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 220);
            int n = 0;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (cd.count(BaseFinder.TUNNEL) < 4) continue;
                for (class_2338 p : cd.hits[BaseFinder.TUNNEL]) {
                    if (!this.inRange(p)) continue;
                    d.box(p.method_10263() + 0.1, p.method_10264(), p.method_10260() + 0.1, p.method_10263() + 0.9, p.method_10264() + 2.0, p.method_10260() + 0.9, c, this.fill.getIntValue());
                    if (++n > 1500) break;
                }
            }
            d.render(event, this.walls.getValue());
        }
    }
}
