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

/**
 * The Base Finder modules (own category). All of them read the shared {@link BaseFinder} chunk scan, which already
 * separates blocks worldgen never makes from ones that structures also generate - so villages, trial chambers, ancient
 * cities, mansions, temples, shipwrecks, bastions, end cities and so on are not flagged.
 */
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
            if (this.mc.field_1687 != null) this.check();
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

        boolean chunkInRange(BaseFinder.ChunkData cd) {
            return this.inRange(new class_2338(cd.centerX(), (int) BaseFinder.feetY(), cd.centerZ()));
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

    /** Marks the given blocks in range (at most 2000). Returns how many were drawn. */
    static int drawList(Esp m, BaseFinder.Draw d, Color c, List<class_2338> list, int n) {
        for (class_2338 p : list) {
            if (n >= 2000) return n;
            if (!m.inRange(p)) continue;
            d.block(p, c, m.fill.getIntValue());
            if (m.tracers.getValue()) d.tracer(class_243.method_24953(p), c);
            n++;
        }
        return n;
    }

    static String xz(BaseFinder.ChunkData cd) {
        return cd.centerX() + ", " + cd.centerZ();
    }

    static String pos(class_2338 p) {
        return p.method_10263() + ", " + p.method_10264() + ", " + p.method_10260();
    }

    // =================================================================== 1. Base Blocks
    public static final class BaseBlocks extends Esp {
        private final NumberSetting minScore = new NumberSetting(EncryptedString.of("Base Score"), 4.0, 60.0, 10.0, 1.0);
        private final BooleanSetting chunks = new BooleanSetting(EncryptedString.of("Highlight Chunks"), true);
        private final BooleanSetting blocks = new BooleanSetting(EncryptedString.of("Show Blocks"), true);

        public BaseBlocks() {
            super("Base ESP", "Finds player-made blocks and scores every chunk. Blocks worldgen never makes (enchanting tables, anvils, beacons, shulkers, observers, concrete...) always count; blocks villages, trial chambers, ancient cities, mansions etc. also have only count outside those structures.", 0xFFFF5A5A);
            this.minScore.setDescription(EncryptedString.of("Score a chunk needs to count as a base. Never-natural blocks give 2, others 1, every different block type +1."));
            this.chunks.setDescription(EncryptedString.of("Colours whole chunks that reach the base score."));
            this.blocks.setDescription(EncryptedString.of("Boxes around every single player block."));
            this.addSettings(this.color, this.minScore, this.chunks, this.blocks, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        boolean isBase(BaseFinder.ChunkData cd) {
            return BaseFinder.settled(cd) && BaseFinder.baseScore(cd) >= this.minScore.getIntValue();
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (!this.isBase(cd)) continue;
                String zone = BaseFinder.naturalZone(cd.cx, cd.cz);
                this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "base at " + xz(cd) + " (score " + cd.score + ", " + cd.distinct + " block types"
                    + (zone != null ? ", built inside a structure (" + zone + ")" : "") + ")");
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 230);
            double y = BaseFinder.feetY();
            int n = 0;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (this.blocks.getValue()) n = drawList(this, d, c, BaseFinder.finds(cd, BaseFinder.BASE, BaseFinder.BASE_W), n);
                if (this.chunks.getValue() && this.isBase(cd)) d.chunk(cd.cx, cd.cz, y, c, Math.min(120, 30 + cd.score * 2));
            }
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 2. Storage
    public static final class Storage extends Esp {
        private final BooleanSetting entities = new BooleanSetting(EncryptedString.of("Chest Minecarts & Donkeys"), true);
        private final BooleanSetting structureLoot = new BooleanSetting(EncryptedString.of("Show Structure Loot"), false);
        private final NumberSetting minChunk = new NumberSetting(EncryptedString.of("Stash Alert"), 2.0, 60.0, 6.0, 1.0);
        private final hook.module.setting.StorageBlocksSetting types = new hook.module.setting.StorageBlocksSetting(EncryptedString.of("Storages"))
                .add("chest", "Chest", net.minecraft.class_1802.field_8106, true, new Color(255, 179, 71))
                .add("ender_chest", "Ender Chest", net.minecraft.class_1802.field_8466, true, new Color(60, 210, 170))
                .add("shulker", "Shulker Box", net.minecraft.class_1802.field_8545, true, new Color(200, 110, 255))
                .add("barrel", "Barrel", net.minecraft.class_1802.field_16307, true, new Color(232, 151, 70))
                .add("hopper", "Hopper", net.minecraft.class_1802.field_8239, true, new Color(120, 130, 255))
                .add("furnace", "Furnace / Smoker", net.minecraft.class_1802.field_8732, true, new Color(102, 168, 255))
                .add("dispenser", "Dispenser", net.minecraft.class_1802.field_8357, false, new Color(95, 188, 255))
                .add("dropper", "Dropper", net.minecraft.class_1802.field_8878, false, new Color(118, 134, 162))
                .add("brewing_stand", "Brewing Stand", net.minecraft.class_1802.field_8740, false, new Color(249, 115, 140))
                .add("crafter", "Crafter", net.minecraft.class_1802.field_46791, false, new Color(255, 195, 72))
                .add("chiseled_bookshelf", "Chiseled Bookshelf", net.minecraft.class_1802.field_40215, false, new Color(189, 126, 255))
                .add("decorated_pot", "Decorated Pot", net.minecraft.class_1802.field_42699, false, new Color(223, 119, 92));

        public Storage() {
            super("Storage ESP", "Chests, barrels, shulkers, ender chests, hoppers, furnaces... (pick which, each with its own color) plus chest minecarts and donkeys/llamas with chests. Loot chests of structures, shipwrecks and buried treasure are left out. Alerts on stashes.", 0xFFFFB347);
            this.minChunk.setDescription(EncryptedString.of("This many player containers in one chunk = stash alert."));
            this.structureLoot.setDescription(EncryptedString.of("Also show containers inside villages, mineshafts, strongholds and other structures."));
            this.addSettings(this.types, this.entities, this.structureLoot, this.minChunk, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        private static String typeKey(String id) {
            if (id.equals("chest") || id.equals("trapped_chest")) return "chest";
            if (id.endsWith("shulker_box")) return "shulker";
            if (id.equals("blast_furnace") || id.equals("smoker")) return "furnace";
            return id;
        }

        private List<class_2338> shown(BaseFinder.ChunkData cd) {
            if (!this.structureLoot.getValue()) return BaseFinder.finds(cd, BaseFinder.STORAGE, BaseFinder.STORAGE_W);
            List<class_2338> all = new java.util.ArrayList<>(cd.hits[BaseFinder.STORAGE]);
            all.addAll(cd.hits[BaseFinder.STORAGE_W]);
            return all;
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (!BaseFinder.settled(cd)) continue;
                List<class_2338> f = BaseFinder.finds(cd, BaseFinder.STORAGE, BaseFinder.STORAGE_W);
                int shulkers = 0;
                for (class_2338 p : cd.hits[BaseFinder.STORAGE]) if (BaseFinder.id(this.mc.field_1687.method_8320(p).method_26204()).contains("shulker")) shulkers++;
                if (f.size() >= this.minChunk.getIntValue() || shulkers >= 2)
                    this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "stash: " + f.size() + " containers" + (shulkers > 0 ? " (" + shulkers + " shulkers)" : "") + " at " + xz(cd));
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            hook.module.setting.StorageBlocksSetting.Entry chestEntry = this.types.get("chest");
            Color c = chestEntry != null ? new Color(chestEntry.color().getRed(), chestEntry.color().getGreen(), chestEntry.color().getBlue(), 230) : BaseFinder.col(this.color.getArgb(), 230);
            int n = 0;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                for (class_2338 p : this.shown(cd)) {
                    if (!this.inRange(p)) continue;
                    String id = BaseFinder.id(this.mc.field_1687.method_8320(p).method_26204());
                    hook.module.setting.StorageBlocksSetting.Entry entry = this.types.get(typeKey(id));
                    if (entry == null || !entry.enabled()) continue;
                    Color k = new Color(entry.color().getRed(), entry.color().getGreen(), entry.color().getBlue(), 235);
                    d.block(p, k, this.fill.getIntValue());
                    if (this.tracers.getValue()) d.tracer(class_243.method_24953(p), k);
                    if (++n > 2000) break;
                }
            }
            if (this.entities.getValue() && this.mc.field_1687 != null) {
                for (class_1297 e : this.mc.field_1687.method_18112()) {
                    String t = BaseFinder.entityId(e);
                    boolean cart = t.equals("chest_minecart") || t.equals("hopper_minecart") || t.endsWith("chest_boat") || t.endsWith("chest_raft");
                    boolean animal = e instanceof net.minecraft.class_1492 h && h.method_6703() && !t.equals("trader_llama");
                    if (!cart && !animal) continue;
                    // mineshaft loot carts are natural
                    if (t.equals("chest_minecart") && BaseFinder.naturalAt(e.method_23317(), e.method_23321())) continue;
                    if (e.method_5707(this.mc.field_1724.method_33571()) > this.range.getValue() * this.range.getValue()) continue;
                    net.minecraft.class_238 b = e.method_5829();
                    d.box(b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324, c, this.fill.getIntValue());
                    if (this.tracers.getValue()) d.tracer(new class_243(e.method_23317(), e.method_23318(), e.method_23321()), c);
                }
            }
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 3. New Chunks
    public static final class NewChunks extends Module {
        public enum HeightMode { BelowYou, Fixed }

        private final RgbSetting newColor = new RgbSetting(EncryptedString.of("New Color"), 0xFFFF4D5E);
        private final RgbSetting oldColor = new RgbSetting(EncryptedString.of("Old Color"), 0xFF3DDC84);
        private final BooleanSetting showOld = new BooleanSetting(EncryptedString.of("Show Old"), true);
        private final NumberSetting fill = new NumberSetting(EncryptedString.of("Fill Opacity"), 0.0, 160.0, 28.0, 4.0);
        private final hook.module.setting.ModeSetting<HeightMode> heightMode = new hook.module.setting.ModeSetting<HeightMode>(EncryptedString.of("Height Mode"), HeightMode.BelowYou, HeightMode.class);
        private final NumberSetting below = new NumberSetting(EncryptedString.of("Below You"), 0.0, 64.0, 10.0, 1.0);
        private final NumberSetting height = new NumberSetting(EncryptedString.of("Height"), -64.0, 320.0, 0.0, 1.0);
        private final NumberSetting maxChunks = new NumberSetting(EncryptedString.of("Remember"), 500.0, 20000.0, 5000.0, 500.0);

        private final java.util.Map<Long, Boolean> state = new java.util.concurrent.ConcurrentHashMap<>(); // true = new, false = old
        private final java.util.Map<Long, Long> loadedAt = new java.util.concurrent.ConcurrentHashMap<>();
        private final java.util.Map<Long, Set<Long>> earlyFlow = new java.util.concurrent.ConcurrentHashMap<>();
        private final Set<Long> checked = java.util.concurrent.ConcurrentHashMap.newKeySet();
        private final java.util.ArrayDeque<Long> order = new java.util.ArrayDeque<>();
        private Object world;
        private double smoothY = Double.NaN;

        public NewChunks() {
            super(EncryptedString.of("New Chunks"), EncryptedString.of("Shows which chunks were just generated (red: nobody was here) and which already existed (green: someone was here). Old = the chunk arrived with flowing water/lava or player-built blocks in it; new = liquid only starts flowing after it loaded. Connected chunks are drawn as one clean area."), -1, Category.DONUT);
            this.showOld.setDescription(EncryptedString.of("Also draw chunks that existed before (travel trails, visited areas)."));
            this.heightMode.setDescription(EncryptedString.of("Below You: a flat layer that glides along a few blocks under you (readable from above while flying). Fixed: always at the chosen height."));
            this.below.setDescription(EncryptedString.of("How many blocks under your feet the layer sits."));
            hook.module.setting.SettingVisibility.set(this.below, () -> this.heightMode.getValue() == HeightMode.BelowYou);
            this.height.setDescription(EncryptedString.of("Height of the layer in Fixed mode."));
            this.maxChunks.setDescription(EncryptedString.of("How many detected chunks are kept (oldest are forgotten)."));
            hook.module.setting.SettingVisibility.set(this.height, () -> this.heightMode.getValue() == HeightMode.Fixed);
            this.addSettings(this.newColor, this.oldColor, this.showOld, this.fill, this.heightMode, this.below, this.height, this.maxChunks);
        }

        @Override
        public void onDisable() {
            this.reset();
            super.onDisable();
        }

        private void reset() {
            this.state.clear();
            this.loadedAt.clear();
            this.earlyFlow.clear();
            this.checked.clear();
            synchronized (this.order) { this.order.clear(); }
            this.smoothY = Double.NaN;
        }

        /** Self-test: what was decided for a chunk (null = unknown). */
        Boolean stateOf(int cx, int cz) {
            return this.state.get(BaseFinder.key(cx, cz));
        }

        int count(boolean fresh) {
            int n = 0;
            for (Boolean b : this.state.values()) if (b == fresh) n++;
            return n;
        }

        @EventListener
        public void onTick(TickEvent event) {
            if (this.mc.field_1687 != this.world) { this.world = this.mc.field_1687; this.reset(); }
            long now = System.currentTimeMillis();
            this.loadedAt.values().removeIf(t -> now - t > 60_000L);
            synchronized (this.order) {
                while (this.order.size() > this.maxChunks.getIntValue()) {
                    Long k = this.order.pollFirst();
                    if (k == null) break;
                    this.state.remove(k);
                    this.checked.remove(k);
                }
            }
        }

        private void mark(long k, boolean fresh) {
            Boolean prev = this.state.put(k, fresh);
            if (prev == null) synchronized (this.order) { this.order.addLast(k); }
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
                int cx = cd.method_11523(), cz = cd.method_11524();
                long k = BaseFinder.key(cx, cz);
                this.loadedAt.put(k, System.currentTimeMillis());
                this.checked.remove(k);
                this.earlyFlow.remove(k);
                // judged on the client thread once the chunk is in the world
                this.mc.execute(() -> this.checkLoaded(cx, cz, k));
            }
        }

        private static boolean flowing(class_2680 st) {
            net.minecraft.class_3610 f = st.method_26227();
            return !f.method_15769() && !f.method_15771();
        }

        /** Liquid that starts flowing shortly after a chunk arrived = the chunk was just generated. */
        private void fluid(class_2338 pos, class_2680 st) {
            if (!flowing(st)) return;
            long k = BaseFinder.key(pos.method_10263() >> 4, pos.method_10260() >> 4);
            Long t = this.loadedAt.get(k);
            if (t == null || System.currentTimeMillis() - t > 20_000L) return; // too long after loading to tell
            if (Boolean.FALSE.equals(this.state.get(k))) return; // already known old
            net.minecraft.class_746 me = this.mc.field_1724;
            if (me != null) {
                double dx = pos.method_10263() + 0.5 - me.method_23317(), dz = pos.method_10260() + 0.5 - me.method_23321();
                if (dx * dx + dz * dz < 10 * 10) return; // you (buckets, mining) cause flows near yourself
            }
            if (!this.checked.contains(k)) this.earlyFlow.computeIfAbsent(k, x -> java.util.concurrent.ConcurrentHashMap.newKeySet()).add(pos.method_10063());
            this.mark(k, true);
        }

        /** Flowing liquid or player-built blocks already in the chunk when it arrived = it existed before. */
        private void checkLoaded(int cx, int cz, long k) {
            try {
                if (this.mc.field_1687 == null) return;
                Object c = this.mc.field_1687.method_2935().method_2857(cx, cz, net.minecraft.class_2806.field_12803, false);
                if (!(c instanceof net.minecraft.class_2818 chunk)) return;
                Set<Long> early = this.earlyFlow.getOrDefault(k, Set.of());
                net.minecraft.class_2826[] secs = chunk.method_12006();
                int bottom = chunk.method_32891();
                boolean old = false;
                for (int i = 0; i < secs.length && !old; i++) {
                    net.minecraft.class_2826 s = secs[i];
                    if (s == null || s.method_38292()) continue;
                    if (s.method_19523(o -> BaseFinder.playerBuilt(((class_2680) o).method_26204()))) { old = true; break; }
                    if (!s.method_19523(o -> flowing((class_2680) o))) continue;
                    int by = (bottom + i) << 4;
                    for (int y = 0; y < 16 && !old; y++) for (int z = 0; z < 16 && !old; z++) for (int x = 0; x < 16; x++) {
                        if (!flowing(s.method_12254(x, y, z))) continue;
                        if (early.contains(class_2338.method_10064((cx << 4) + x, by + y, (cz << 4) + z))) continue; // flowed after loading
                        old = true;
                        break;
                    }
                }
                if (old) this.mark(k, false);
            } finally {
                this.checked.add(k);
                this.earlyFlow.remove(k);
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            if (this.state.isEmpty() || this.mc.field_1724 == null) return;
            double y;
            if (this.heightMode.getValue() == HeightMode.Fixed) {
                y = this.height.getValue();
            } else {
                double target = Math.floor(this.mc.field_1724.method_23318()) - this.below.getIntValue() + 0.02;
                // glide to the new height instead of jumping with every block you climb
                this.smoothY = Double.isNaN(this.smoothY) || Math.abs(this.smoothY - target) > 24 ? target : this.smoothY + (target - this.smoothY) * 0.15;
                y = this.smoothY;
            }
            int pcx = (int) Math.floor(this.mc.field_1724.method_23317()) >> 4, pcz = (int) Math.floor(this.mc.field_1724.method_23321()) >> 4;
            int range = this.mc.field_1690.method_38521() + 3;
            BaseFinder.Draw d = new BaseFinder.Draw();
            int f = this.fill.getIntValue();
            boolean showOld = this.showOld.getValue();
            Color nc = BaseFinder.col(this.newColor.getArgb(), 235), oc = BaseFinder.col(this.oldColor.getArgb(), 215);
            for (java.util.Map.Entry<Long, Boolean> e : this.state.entrySet()) {
                boolean fresh = e.getValue();
                if (!fresh && !showOld) continue;
                long k = e.getKey();
                int cx = net.minecraft.class_1923.method_8325(k), cz = net.minecraft.class_1923.method_8332(k);
                int dist = Math.max(Math.abs(cx - pcx), Math.abs(cz - pcz));
                if (dist > range) continue;
                // fade out towards the edge of the render distance
                float fade = dist <= range - 4 ? 1.0f : Math.max(0.15f, (range - dist) / 4.0f);
                Color c = fresh ? nc : oc;
                Color cf = new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.round(c.getAlpha() * fade));
                d.region(cx, cz, y, cf, Math.round(f * fade),
                        this.same(cx, cz - 1, fresh), this.same(cx + 1, cz, fresh), this.same(cx, cz + 1, fresh), this.same(cx - 1, cz, fresh));
            }
            d.render(event, true);
        }

        private boolean same(int cx, int cz, boolean fresh) {
            Boolean s = this.state.get(BaseFinder.key(cx, cz));
            return s != null && s == fresh;
        }
    }

    // =================================================================== 4. Kelp ESP
    /**
     * Worldgen kelp tops are always age 20-23. Kelp only grows (age 24-25 = grew at least once / twice) while a player is
     * within simulation distance, about 8 minutes per step. Grown kelp = someone spent a long time here.
     */
    public static final class KelpEsp extends Esp {
        private final NumberSetting minGrown = new NumberSetting(EncryptedString.of("Min Grown Kelp"), 1.0, 40.0, 3.0, 1.0);
        private final NumberSetting minPct = new NumberSetting(EncryptedString.of("Grown %"), 5.0, 100.0, 25.0, 5.0);
        private final BooleanSetting marks = new BooleanSetting(EncryptedString.of("Mark Kelp Tops"), true);

        public KelpEsp() {
            super("Kelp ESP", "Finds chunks with grown kelp and marks them at the water surface. Freshly generated kelp never passes age 23 - older kelp only exists where a player stayed loaded for a long time (bases, AFK spots, farms).", 0xFF3DDC84);
            this.minGrown.setDescription(EncryptedString.of("Grown kelp plants a chunk needs."));
            this.minPct.setDescription(EncryptedString.of("Share of the kelp that could still grow that actually grew."));
            this.marks.setDescription(EncryptedString.of("Boxes on every grown kelp top."));
            this.addSettings(this.color, this.minGrown, this.minPct, this.marks, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        boolean grown(BaseFinder.ChunkData cd) {
            int grown = cd.count(BaseFinder.KELP_GROWN);
            int growable = cd.count(BaseFinder.KELP) - cd.kelpStuck;
            return grown >= this.minGrown.getIntValue() && growable > 0 && grown * 100 >= growable * this.minPct.getIntValue();
        }

        /** The water surface above this chunk's kelp. */
        double surface(BaseFinder.ChunkData cd) {
            int top = Integer.MIN_VALUE;
            for (class_2338 p : cd.hits[BaseFinder.KELP]) top = Math.max(top, p.method_10264());
            if (top == Integer.MIN_VALUE) return 63;
            class_2338 p = new class_2338(cd.centerX(), top, cd.centerZ());
            for (class_2338 k : cd.hits[BaseFinder.KELP]) if (k.method_10264() == top) { p = k; break; }
            int y = p.method_10264();
            while (y < 320 && !this.mc.field_1687.method_8320(new class_2338(p.method_10263(), y + 1, p.method_10260())).method_26227().method_15769()) y++;
            return y + 1.0;
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (!this.grown(cd)) continue;
                this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "grown kelp at " + xz(cd) + " (" + cd.count(BaseFinder.KELP_GROWN) + "/" + (cd.count(BaseFinder.KELP) - cd.kelpStuck) + " grew - long loaded)");
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 230);
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (!this.grown(cd) || !this.chunkInRange(cd)) continue;
                d.chunk(cd.cx, cd.cz, this.surface(cd), c, Math.max(this.fill.getIntValue(), 25));
                if (this.marks.getValue()) for (class_2338 p : cd.hits[BaseFinder.KELP_GROWN]) d.block(p, c, 0);
                if (this.tracers.getValue()) d.tracer(new class_243(cd.centerX(), this.surface(cd), cd.centerZ()), c);
            }
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 5. Amethyst ESP
    /**
     * Geodes generate with all four bud stages mixed (about a quarter fully grown). Buds only grow while a player is
     * nearby, so a geode that is nearly all clusters was kept loaded for a long time.
     */
    public static final class AmethystEsp extends Esp {
        private final NumberSetting minPct = new NumberSetting(EncryptedString.of("Grown %"), 30.0, 100.0, 60.0, 5.0);
        private final NumberSetting minClusters = new NumberSetting(EncryptedString.of("Min Clusters"), 1.0, 40.0, 6.0, 1.0);
        private final BooleanSetting all = new BooleanSetting(EncryptedString.of("Show All Clusters"), false);

        public AmethystEsp() {
            super("Amethyst ESP", "Highlights fully grown amethyst clusters. A geode that is mostly clusters grew while someone was near for a long time; fresh geodes are only about a quarter grown.", 0xFFC77DFF);
            this.minPct.setDescription(EncryptedString.of("Share of fully grown clusters a geode needs to count as grown (fresh ones are ~25%)."));
            this.minClusters.setDescription(EncryptedString.of("Clusters needed before a chunk is judged."));
            this.all.setDescription(EncryptedString.of("Also box clusters in geodes that are not grown."));
            this.addSettings(this.color, this.minPct, this.minClusters, this.all, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        boolean grown(BaseFinder.ChunkData cd) {
            int full = cd.count(BaseFinder.AMETHYST_FULL), bud = cd.count(BaseFinder.AMETHYST_BUD);
            return full >= this.minClusters.getIntValue() && full * 100 >= (full + bud) * this.minPct.getIntValue();
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (!this.grown(cd)) continue;
                class_2338 p = cd.hits[BaseFinder.AMETHYST_FULL].get(0);
                int full = cd.count(BaseFinder.AMETHYST_FULL), bud = cd.count(BaseFinder.AMETHYST_BUD);
                this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "grown geode at " + pos(p) + " (" + (full * 100 / (full + bud)) + "% clusters - long loaded)");
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 235);
            Color dim = new Color(c.getRed(), c.getGreen(), c.getBlue(), 110);
            int n = 0;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                boolean g = this.grown(cd);
                if (!g && !this.all.getValue()) continue;
                n = drawList(this, d, g ? c : dim, cd.hits[BaseFinder.AMETHYST_FULL], n);
                if (g && this.chunkInRange(cd)) {
                    int minY = Integer.MAX_VALUE;
                    for (class_2338 p : cd.hits[BaseFinder.AMETHYST_FULL]) minY = Math.min(minY, p.method_10264());
                    d.chunk(cd.cx, cd.cz, minY - 1.0, c, 20);
                }
            }
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 6. Growth (all signs together)
    public static final class Growth extends Esp {
        private final BooleanSetting chunks = new BooleanSetting(EncryptedString.of("Highlight Chunks"), true);
        private final BooleanSetting marks = new BooleanSetting(EncryptedString.of("Mark Plants"), false);

        public Growth() {
            super("Growth Detector", "All plant clues in one: grown kelp (worldgen kelp never passes age 23), mostly-grown amethyst geodes, and picked or planted berry bushes (worldgen bushes are always ripe).", 0xFFB06BFF);
            this.chunks.setDescription(EncryptedString.of("Colours chunks with a clue."));
            this.marks.setDescription(EncryptedString.of("Boxes the plants that gave the clue."));
            this.addSettings(this.color, this.chunks, this.marks, this.range, this.fill, this.walls, this.alerts);
        }

        /** null = no sign, else a short reason. */
        String reason(BaseFinder.ChunkData cd) {
            int grown = cd.count(BaseFinder.KELP_GROWN), growable = cd.count(BaseFinder.KELP) - cd.kelpStuck;
            if (grown >= 3 && grown * 4 >= growable) return "kelp grew " + grown + "/" + growable;
            int full = cd.count(BaseFinder.AMETHYST_FULL), bud = cd.count(BaseFinder.AMETHYST_BUD);
            if (full >= 6 && full * 100 >= (full + bud) * 60) return "amethyst " + (full * 100 / (full + bud)) + "% grown";
            int picked = cd.count(BaseFinder.BERRY_PICKED);
            if (picked >= 3 && picked * 2 >= cd.count(BaseFinder.BERRY) && !this.foxNear(cd)) return picked + " berry bushes picked/planted";
            return null;
        }

        /** Foxes eat berries too. */
        private boolean foxNear(BaseFinder.ChunkData cd) {
            for (class_1297 e : this.mc.field_1687.method_18112()) {
                if (!BaseFinder.entityId(e).equals("fox")) continue;
                if (Math.abs(e.method_23317() - cd.centerX()) < 40 && Math.abs(e.method_23321() - cd.centerZ()) < 40) return true;
            }
            return false;
        }

        @Override
        void check() {
            if (this.mc.field_1724.field_6012 % 20 != 0) return;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                String r = this.reason(cd);
                if (r != null) this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "lived-in chunk at " + xz(cd) + " (" + r + ")");
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 220);
            double y = BaseFinder.feetY();
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (!this.alerted.contains(BaseFinder.key(cd.cx, cd.cz)) && !(this.alerts.getValue() ? false : this.reason(cd) != null)) continue;
                if (this.chunks.getValue()) d.chunk(cd.cx, cd.cz, y, c, this.fill.getIntValue());
                if (this.marks.getValue()) {
                    for (int k : new int[]{BaseFinder.KELP_GROWN, BaseFinder.AMETHYST_FULL, BaseFinder.BERRY_PICKED})
                        for (class_2338 p : cd.hits[k]) if (this.inRange(p)) d.block(p, c, 0);
                }
            }
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 7. Light
    public static final class Light extends Esp {
        private final BooleanSetting sources = new BooleanSetting(EncryptedString.of("Placed Light"), true);
        private final BooleanSetting hidden = new BooleanSetting(EncryptedString.of("Lit Caves"), true);
        private final NumberSetting minLit = new NumberSetting(EncryptedString.of("Lit Cave Spots"), 2.0, 60.0, 6.0, 1.0);

        public Light() {
            super("Light ESP", "Torches, lanterns, glowstone, sea lanterns, froglights, candles, bulbs... that players place (not the ones villages, strongholds, ancient cities etc. come with), plus cave spots lit by light no natural source explains.", 0xFFFFE066);
            this.sources.setDescription(EncryptedString.of("Boxes every placed light source."));
            this.hidden.setDescription(EncryptedString.of("Marks dark-cave air that is lit (9+) where no lava, glow lichen, magma, glow berries, sea pickles... in reach could do it: someone put light there."));
            this.minLit.setDescription(EncryptedString.of("Unexplained lit spots a chunk needs before it is marked."));
            this.addSettings(this.color, this.sources, this.hidden, this.minLit, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        @Override
        boolean needAir() {
            return this.hidden.getValue();
        }

        @Override
        void check() {
            if (this.mc.field_1724.field_6012 % 10 != 0) return;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (!BaseFinder.settled(cd)) continue;
                int below = 0;
                for (class_2338 p : BaseFinder.finds(cd, BaseFinder.LIGHT, BaseFinder.LIGHT_W)) if (p.method_10264() < 40 && BaseFinder.overworld()) below++;
                if (below >= 3) this.alertOnce(BaseFinder.key(cd.cx, cd.cz), below + " placed lights underground at " + xz(cd));
                else if (this.hidden.getValue() && BaseFinder.litByPlayer(cd, this.minLit.getIntValue())) this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "cave lit by a player at " + xz(cd));
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 230);
            int n = 0;
            Color h = new Color(255, 150, 40, 200);
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (this.sources.getValue()) n = drawList(this, d, c, BaseFinder.finds(cd, BaseFinder.LIGHT, BaseFinder.LIGHT_W), n);
                if (this.hidden.getValue() && cd.litVerdict == Boolean.TRUE) {
                    int m = 0;
                    for (class_2338 p : cd.hits[BaseFinder.LIT_CAVE]) {
                        if (!this.inRange(p)) continue;
                        d.box(p.method_10263() + 0.35, p.method_10264() + 0.35, p.method_10260() + 0.35, p.method_10263() + 0.65, p.method_10264() + 0.65, p.method_10260() + 0.65, h, 60);
                        if (++m > 80) break;
                    }
                }
            }
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 8. Portals
    public static final class Portals extends Esp {
        public Portals() {
            super("Portal ESP", "Lit nether portals (ruined portals are never lit - a lit one was made by a player) and activated end portals (not the End's exit portal).", 0xFFB050FF);
            this.addSettings(this.color, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (cd.count(BaseFinder.PORTAL) == 0) continue;
                class_2338 p = cd.hits[BaseFinder.PORTAL].get(0);
                this.alertOnce(BaseFinder.key(cd.cx, cd.cz), BaseFinder.id(this.mc.field_1687.method_8320(p).method_26204()).replace('_', ' ') + " at " + pos(p));
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 230);
            int n = 0;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) n = drawList(this, d, c, cd.hits[BaseFinder.PORTAL], n);
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 9. Signs
    public static final class Signs extends Esp {
        private final BooleanSetting log = new BooleanSetting(EncryptedString.of("Log Text"), true);

        public Signs() {
            super("Sign ESP", "Boxes every player sign through walls and writes its text with coordinates in chat (bases are full of signs: names, warnings, shop labels). Village and igloo signs are left out.", 0xFF8BD3FF);
            this.log.setDescription(EncryptedString.of("Each sign's text goes into chat once."));
            this.addSettings(this.color, this.log, this.range, this.fill, this.tracers, this.walls);
        }

        private boolean ok(BaseFinder.ChunkData cd) {
            return BaseFinder.settled(cd) && !BaseFinder.natural(cd);
        }

        @Override
        void check() {
            if (!this.log.getValue()) return;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (cd.count(BaseFinder.SIGN) == 0 || !this.ok(cd)) continue;
                for (class_2338 p : cd.hits[BaseFinder.SIGN]) {
                    if (this.alerted.contains(p.method_10063())) continue;
                    if (!(this.mc.field_1687.method_8321(p) instanceof net.minecraft.class_2625 sign)) continue;
                    String text = signText(sign.method_49853()) + " " + signText(sign.method_49854());
                    this.alerted.add(p.method_10063());
                    if (text.isBlank()) continue;
                    BaseFinder.chat("Sign", pos(p) + ": §e" + text.trim());
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
            Color c = BaseFinder.col(this.color.getArgb(), 230);
            int n = 0;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) if (cd.count(BaseFinder.SIGN) > 0 && this.ok(cd)) n = drawList(this, d, c, cd.hits[BaseFinder.SIGN], n);
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 10. Entity clues
    public static final class EntityClues extends Esp {
        private final BooleanSetting frames = new BooleanSetting(EncryptedString.of("Item Frames & Armor Stands"), true);
        private final BooleanSetting pets = new BooleanSetting(EncryptedString.of("Tamed / Named / Leashed"), true);
        private final BooleanSetting farms = new BooleanSetting(EncryptedString.of("Animal Farms"), true);
        private final NumberSetting farmSize = new NumberSetting(EncryptedString.of("Farm Size"), 4.0, 40.0, 10.0, 1.0);
        private final BooleanSetting villagers = new BooleanSetting(EncryptedString.of("Trading Halls"), true);
        private final BooleanSetting misc = new BooleanSetting(EncryptedString.of("Crystals, Boats, Carts"), true);

        public EntityClues() {
            super("Entity Clues", "Entities only players leave behind: item frames, armor stands, tamed pets, name-tagged or leashed mobs, end crystals, parked boats/minecarts, animal farms and villager trading halls. Wandering trader llamas, skeleton-trap horses, mineshaft carts, end city frames and village animals are left out.", 0xFF5AC8FF);
            this.farmSize.setDescription(EncryptedString.of("This many animals of one kind within 6 blocks = farm."));
            this.villagers.setDescription(EncryptedString.of("6+ villagers locked in cells within 8 blocks."));
            this.addSettings(this.color, this.frames, this.pets, this.farms, this.farmSize, this.villagers, this.misc, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        private String clue(class_1297 e, String id) {
            boolean nat = BaseFinder.naturalAt(e.method_23317(), e.method_23321());
            if (this.frames.getValue() && (id.equals("item_frame") || id.equals("glow_item_frame") || id.equals("armor_stand") || id.equals("painting")))
                return nat && !id.equals("glow_item_frame") ? null : id.replace('_', ' ');
            if (this.pets.getValue() && !id.equals("trader_llama") && !id.equals("skeleton_horse") && !id.equals("zombie_horse")) {
                if (e instanceof net.minecraft.class_1321 t && t.method_6181()) return "tamed " + id;
                if (e instanceof net.minecraft.class_1496 h && h.method_6727()) return "tamed " + id;
                if (!(e instanceof net.minecraft.class_1657) && e.method_5797() != null) return "named " + id;
                if (e instanceof net.minecraft.class_9817 l && l.method_60953()) {
                    class_1297 holder = l.method_60952();
                    if (holder == null || !BaseFinder.entityId(holder).equals("wandering_trader")) return "leashed " + id;
                }
            }
            if (this.misc.getValue()) {
                if (id.equals("end_crystal") && !nat) return "end crystal";
                if (id.endsWith("_boat") || id.endsWith("_raft")) return id.replace('_', ' ');
                if ((id.equals("minecart") || id.endsWith("_minecart")) && !nat) return id.replace('_', ' ');
            }
            return null;
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            if (this.mc.field_1687 == null || this.mc.field_1724 == null) return;
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 230);
            double r2 = this.range.getValue() * this.range.getValue();
            Map<String, java.util.List<class_1297>> groups = new HashMap<>();
            for (class_1297 e : this.mc.field_1687.method_18112()) {
                if (e == this.mc.field_1724 || e.method_5707(this.mc.field_1724.method_33571()) > r2) continue;
                String id = BaseFinder.entityId(e);
                String why = this.clue(e, id);
                if (why != null) {
                    net.minecraft.class_238 b = e.method_5829();
                    d.box(b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324, c, this.fill.getIntValue());
                    if (this.tracers.getValue()) d.tracer(new class_243(e.method_23317(), e.method_23318(), e.method_23321()), c);
                    this.alertOnce(BaseFinder.key((int) Math.floor(e.method_23317()) >> 4, (int) Math.floor(e.method_23321()) >> 4) * 31 + id.hashCode(),
                        why + " at " + (int) e.method_23317() + ", " + (int) e.method_23318() + ", " + (int) e.method_23321());
                }
                boolean farmAnimal = this.farms.getValue() && e instanceof net.minecraft.class_1429 && !BaseFinder.naturalAt(e.method_23317(), e.method_23321());
                // trading hall: villagers locked in cells (free villagers gather at the bell at night)
                boolean villager = this.villagers.getValue() && id.equals("villager") && BaseFinder.enclosed(e);
                if (farmAnimal || villager) groups.computeIfAbsent(id, k -> new java.util.ArrayList<>()).add(e);
            }
            Color fc = new Color(255, 230, 90, 220);
            for (Map.Entry<String, java.util.List<class_1297>> en : groups.entrySet()) {
                boolean vil = en.getKey().equals("villager");
                int need = vil ? 6 : this.farmSize.getIntValue();
                double reach = vil ? 64.0 : 36.0;
                List<class_1297> list = en.getValue();
                if (list.size() < need) continue;
                for (class_1297 a : list) {
                    int near = 0;
                    for (class_1297 b : list) if (a.method_5858(b) < reach) near++;
                    if (near < need) continue;
                    net.minecraft.class_238 bb = a.method_5829().method_1014(vil ? 4.0 : 2.5);
                    d.box(bb.field_1323, bb.field_1322, bb.field_1321, bb.field_1320, bb.field_1325, bb.field_1324, fc, 0);
                    this.alertOnce(BaseFinder.key((int) a.method_23317() >> 4, (int) a.method_23321() >> 4) * 7 + en.getKey().hashCode(),
                        (vil ? "villager trading hall" : en.getKey() + " farm") + " (" + near + ") at " + (int) a.method_23317() + ", " + (int) a.method_23318() + ", " + (int) a.method_23321());
                    break;
                }
            }
            d.render(event, this.walls.getValue());
        }
    }

    // =================================================================== 11. Tunnels
    public static final class Tunnels extends Esp {
        private final NumberSetting minSpots = new NumberSetting(EncryptedString.of("Min Length"), 2.0, 40.0, 4.0, 1.0);

        public Tunnels() {
            super("Tunnel Finder", "Dead-straight tunnels (1x2 from 8 blocks, up to 3x3 from 12) with solid floor, roof and walls - caves never look like that; strip mines, travel tunnels and nether highways do. Mineshafts, strongholds and other structures are left out.", 0xFF4FA3FF);
            this.minSpots.setDescription(EncryptedString.of("Tunnel spots a chunk needs (higher = only long tunnels)."));
            this.addSettings(this.color, this.minSpots, this.range, this.fill, this.tracers, this.walls, this.alerts);
        }

        @Override
        boolean needAir() {
            return true;
        }

        boolean ok(BaseFinder.ChunkData cd) {
            return cd.count(BaseFinder.TUNNEL) >= this.minSpots.getIntValue() && BaseFinder.settled(cd) && !BaseFinder.natural(cd);
        }

        @Override
        void check() {
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (!this.ok(cd)) continue;
                this.alertOnce(BaseFinder.key(cd.cx, cd.cz), "player tunnel at " + pos(cd.hits[BaseFinder.TUNNEL].get(0)));
            }
        }

        @EventListener
        public void onRender3D(Render3DEvent event) {
            BaseFinder.Draw d = new BaseFinder.Draw();
            Color c = BaseFinder.col(this.color.getArgb(), 220);
            int n = 0;
            for (BaseFinder.ChunkData cd : BaseFinder.all()) {
                if (!this.ok(cd)) continue;
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
