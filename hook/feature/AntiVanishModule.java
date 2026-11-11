/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.suggestion.Suggestion
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_1297
 *  net.minecraft.class_1299
 *  net.minecraft.class_1657
 *  net.minecraft.class_1676
 *  net.minecraft.class_1934
 *  net.minecraft.class_2338
 *  net.minecraft.class_2374
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2588
 *  net.minecraft.class_2596
 *  net.minecraft.class_2639
 *  net.minecraft.class_2664
 *  net.minecraft.class_2675
 *  net.minecraft.class_2680
 *  net.minecraft.class_2703
 *  net.minecraft.class_2703$class_2705
 *  net.minecraft.class_2703$class_5893
 *  net.minecraft.class_2739
 *  net.minecraft.class_2741
 *  net.minecraft.class_2765
 *  net.minecraft.class_2767
 *  net.minecraft.class_2769
 *  net.minecraft.class_2805
 *  net.minecraft.class_2824
 *  net.minecraft.class_2846
 *  net.minecraft.class_2879
 *  net.minecraft.class_2885
 *  net.minecraft.class_2886
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_640
 *  net.minecraft.class_7417
 *  net.minecraft.class_7439
 *  net.minecraft.class_7828
 *  net.minecraft.class_7923
 */
package hook.feature;

import com.mojang.brigadier.suggestion.Suggestion;
import hook.feature.PlayerWatchHud;
import hook.feature.VanishAlertRules;
import hook.feature.VanishChatParser;
import hook.feature.VanishSignalClassifier;
import hook.module.ext.ClientModule;
import hook.module.ext.EventHandler;
import hook.module.ext.ModuleCategory;
import hook.module.ext.ModuleRegistry;
import hook.module.ext.event.PacketReceiveNettyEvent;
import hook.module.ext.event.PacketSendEvent;
import hook.module.ext.event.TickEvent;
import hook.utils.ext.DonutRegionTracker;
import hook.utils.ext.ModuleChat;
import hook.utils.ext.NameFilter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_1676;
import net.minecraft.class_1934;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_2596;
import net.minecraft.class_2639;
import net.minecraft.class_2664;
import net.minecraft.class_2675;
import net.minecraft.class_2680;
import net.minecraft.class_2703;
import net.minecraft.class_2739;
import net.minecraft.class_2741;
import net.minecraft.class_2765;
import net.minecraft.class_2767;
import net.minecraft.class_2769;
import net.minecraft.class_2805;
import net.minecraft.class_2824;
import net.minecraft.class_2846;
import net.minecraft.class_2879;
import net.minecraft.class_2885;
import net.minecraft.class_2886;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_640;
import net.minecraft.class_7417;
import net.minecraft.class_7439;
import net.minecraft.class_7828;
import net.minecraft.class_7923;

@Environment(value=EnvType.CLIENT)
public final class AntiVanishModule
extends ClientModule {
    private static final class_310 MC = class_310.method_1551();
    private static final String PROBE_COMMAND = "minecraft:msg";
    private static final int SENSOR_RANGE = 64;
    private static final long SIGNAL_WINDOW_MS = 15000L;
    private static final long DETECTION_TTL_MS = 20000L;
    private static final long CRITICAL_COOLDOWN_MS = 12000L;
    private static final long ANNOUNCE_COOLDOWN_MS = 1500L;
    private static final long ANNOUNCE_WINDOW_MS = 4000L;
    private static final int MAX_ANNOUNCE_PER_WINDOW = 6;
    private static final int MAX_OBSERVATIONS_PER_TICK = 512;
    private static final int CRITICAL_SCORE = 35;
    private static final long SELF_PLACE_TTL_MS = 6000L;
    private static final long CONTAINER_SELF_GRACE_MS = 2500L;
    private static final long SPELL_SETTLE_MS = 1000L;
    private final ConcurrentLinkedQueue<VanishSignal> observations = new ConcurrentLinkedQueue();
    private final Map<UUID, StaffProfile> knownPlayers = new HashMap<UUID, StaffProfile>();
    private final Map<String, AlertCandidate> detections = new LinkedHashMap<String, AlertCandidate>();
    private final Map<String, Long> signalCooldowns = new HashMap<String, Long>();
    private final Map<String, Long> announceCooldowns = new HashMap<String, Long>();
    private final Deque<Long> announceTimes = new ArrayDeque<Long>();
    private final Map<Long, Long> selfPlacedBlocks = new ConcurrentHashMap<Long, Long>();
    private final Map<UUID, Long> confirmedDepartures = new HashMap<UUID, Long>();
    private final Map<Long, Long> automatedMechanisms = new HashMap<Long, Long>();
    private final Map<String, Deque<Long>> weakParticleBursts = new HashMap<String, Deque<Long>>();
    private final Deque<VanishFinding> signals = new ArrayDeque<VanishFinding>();
    private final Deque<SpatialSignal> recentExplosions = new ArrayDeque<SpatialSignal>();
    private final Deque<PendingTabRemoval> pendingVanishes = new ArrayDeque<PendingTabRemoval>();
    private final Map<String, VanishObservation> hiddenSpells = new LinkedHashMap<String, VanishObservation>();
    private final Deque<AntiVanishHelper> recentMessages = new ArrayDeque<AntiVanishHelper>();
    private boolean serverSendsLeaveMessages;
    private final Set<Integer> completionRequestIds = ConcurrentHashMap.newKeySet();
    private volatile List<String> pendingCompletionNames;
    private int nextCompletionId = 30000;
    private static final long RECENT_MESSAGE_TTL_MS = 8000L;
    private static final long TRUSTED_LISTED_MS = 1500L;
    private final Map<UUID, Long> listedSinceMs = new HashMap<UUID, Long>();
    private final DonutRegionTracker shard = new DonutRegionTracker();
    private int tickCounter;
    private volatile long lastLocalActionMs;
    private long lastContainerActivityMs;
    private long lastCriticalMs;
    private long criticalUntilMs;
    private String criticalWatcher = "";
    private final Set<UUID> pendingChimes = new HashSet<UUID>();
    private UUID ghostId;
    private String ghostName;

    public AntiVanishModule() {
        super("anti-vanish", "Anti Vanish", "Detects vanished players.", ModuleCategory.DONUT);
        this.usesNotifications();
    }

    @Override
    public void onEnable() {
        this.resetRuntime();
        if (AntiVanishModule.MC.field_1724 != null && AntiVanishModule.MC.field_1687 != null) {
            this.trackListedPlayers();
        }
    }

    @Override
    public void onDisable() {
        this.resetRuntime();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (AntiVanishModule.MC.field_1724 == null || AntiVanishModule.MC.field_1687 == null || MC.method_1562() == null) {
            this.resetRuntime();
            return;
        }
        if (this.shard.moved(MC)) {
            this.resetRuntime();
        }
        this.playPendingChime();
        ++this.tickCounter;
        if (AntiVanishModule.MC.field_1724.field_7512 != AntiVanishModule.MC.field_1724.field_7498) {
            this.lastContainerActivityMs = System.currentTimeMillis();
        }
        this.drainObservations();
        this.processPendingVanishes();
        this.processCompletionProbe();
        if (this.tickCounter % 100 == 0) {
            this.sendCompletionProbe();
        }
        if (this.tickCounter % 10 == 0) {
            this.trackListedPlayers();
            this.reviewSpells();
        }
        if (this.tickCounter % 5 == 0) {
            this.scanInvisiblePlayers();
        }
        this.pruneState();
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent event) {
        this.handleOutbound(event.packet);
    }

    private void handleOutbound(class_2596<?> packet) {
        if (packet instanceof class_2885) {
            class_2885 place = (class_2885)packet;
            this.lastLocalActionMs = System.currentTimeMillis();
            if (place.method_12543() == null || place.method_12543().method_17777() == null) {
                return;
            }
            long expiresAt = System.currentTimeMillis() + 6000L;
            class_2338 target = place.method_12543().method_17777();
            class_2338 adjacent = target.method_10093(place.method_12543().method_17780());
            this.selfPlacedBlocks.put(target.method_10063(), expiresAt);
            this.selfPlacedBlocks.put(adjacent.method_10063(), expiresAt);
            String itemPath = "";
            if (AntiVanishModule.MC.field_1724 != null && place.method_12546() != null) {
                class_2960 id = class_7923.field_41178.method_10221((Object)AntiVanishModule.MC.field_1724.method_5998(place.method_12546()).method_7909());
                itemPath = VanishSignalClassifier.path(id == null ? "" : id.toString());
            }
            AntiVanishModule.markSelfMultiBlockFootprint(target, itemPath, this.selfPlacedBlocks, expiresAt);
            AntiVanishModule.markSelfMultiBlockFootprint(adjacent, itemPath, this.selfPlacedBlocks, expiresAt);
        } else if (packet instanceof class_2846 || packet instanceof class_2886 || packet instanceof class_2824 || packet instanceof class_2879) {
            this.lastLocalActionMs = System.currentTimeMillis();
        }
    }

    static void markSelfMultiBlockFootprint(class_2338 position, String itemPath, Map<Long, Long> placements, long expiresAt) {
        boolean isTall;
        if (position == null || placements == null) {
            return;
        }
        placements.put(position.method_10063(), expiresAt);
        String path = itemPath == null ? "" : itemPath;
        boolean bl = isTall = path.endsWith("_door") && !path.endsWith("trapdoor") || path.contains("sunflower") || path.contains("lilac") || path.contains("rose_bush") || path.contains("peony") || path.contains("tall_grass") || path.contains("large_fern") || path.contains("pitcher_plant");
        if (isTall) {
            placements.put(position.method_10084().method_10063(), expiresAt);
            placements.put(position.method_10074().method_10063(), expiresAt);
        }
        if (path.endsWith("_bed")) {
            placements.put(position.method_10069(1, 0, 0).method_10063(), expiresAt);
            placements.put(position.method_10069(-1, 0, 0).method_10063(), expiresAt);
            placements.put(position.method_10069(0, 0, 1).method_10063(), expiresAt);
            placements.put(position.method_10069(0, 0, -1).method_10063(), expiresAt);
        }
    }

    @EventHandler
    public void onPacketReceiveNetty(PacketReceiveNettyEvent var1) {
        class_2596<?> var6 = var1.packet;
        if ((byte)(this.handleInbound(var6) ? 1 : 0) != 0) {
            var1.setCancelled(true);
        }
    }

    private boolean handleInbound(class_2596<?> packet) {
        class_2639 suggestions;
        class_2703 gameMode;
        class_2703 listed;
        if (packet instanceof class_7828) {
            class_7828 removed = (class_7828)packet;
            if (removed.comp_1105().size() < 4) {
                for (UUID id : removed.comp_1105()) {
                    this.observations.offer(VanishSignal.tabRemove(id));
                }
            }
            return false;
        }
        if (packet instanceof class_2703 && (listed = (class_2703)packet).method_46327().contains(class_2703.class_5893.field_40700)) {
            int hiddenCount = 0;
            for (class_2703.class_2705 entry : listed.method_46329()) {
                if (entry == null || entry.comp_1108()) continue;
                ++hiddenCount;
            }
            if (hiddenCount > 0 && hiddenCount < 4) {
                for (class_2703.class_2705 entry : listed.method_46329()) {
                    if (entry == null || entry.comp_1108()) continue;
                    this.observations.offer(VanishSignal.tabHide(entry.comp_1106()));
                }
            }
            return false;
        }
        if (packet instanceof class_2703 && (gameMode = (class_2703)packet).method_46327().contains(class_2703.class_5893.field_29137)) {
            this.queueGamemodeUpdates(gameMode);
            return false;
        }
        if (packet instanceof class_7439) {
            String departed;
            String text;
            class_7439 message = (class_7439)packet;
            String string = text = message.comp_763() == null ? "" : message.comp_763().getString();
            if (!text.isBlank()) {
                this.observations.offer(VanishSignal.systemChat(text));
            }
            if (!(departed = AntiVanishModule.departedPlayerName(message.comp_763())).isBlank()) {
                this.observations.offer(VanishSignal.playerLeft(departed));
            }
            return false;
        }
        if (packet instanceof class_2639 && this.completionRequestIds.remove((suggestions = (class_2639)packet).comp_2262())) {
            ArrayList<String> names = new ArrayList<String>();
            for (Suggestion suggestion : suggestions.method_11397().getList()) {
                String name = suggestion.getText();
                if (name == null || name.isBlank()) continue;
                names.add(name.trim());
            }
            this.pendingCompletionNames = names;
            return true;
        }
        if (packet instanceof class_2739) {
            class_2739 metadata = (class_2739)packet;
            this.observations.offer(VanishSignal.entityMetadata(metadata.comp_1127()));
        } else if (packet instanceof class_2664) {
            class_2664 explosion = (class_2664)packet;
            this.observations.offer(VanishSignal.explosion(explosion.comp_2883(), explosion.comp_4594()));
        } else if (packet instanceof class_2765) {
            class_2765 sound = (class_2765)packet;
            this.observations.offer(VanishSignal.entitySound(sound.method_11883(), AntiVanishModule.soundId(((class_3414)sound.method_11882().comp_349()).comp_3319())));
        } else if (packet instanceof class_2675) {
            class_2675 particle = (class_2675)packet;
            class_2960 id = class_7923.field_41180.method_10221((Object)particle.method_11551().method_10295());
            this.observations.offer(VanishSignal.position(VanishSignalType.PARTICLE, particle.method_11544(), particle.method_11547(), particle.method_11546(), id == null ? "" : id.toString()));
        } else if (packet instanceof class_2767) {
            class_2767 sound = (class_2767)packet;
            this.onSoundPacket(sound);
        }
        return false;
    }

    private void onSoundPacket(class_2767 var1) {
        if (var1 != null && var1.method_11894() != null && var1.method_11894().comp_349() != null) {
            String var2 = AntiVanishModule.soundId(((class_3414)var1.method_11894().comp_349()).comp_3319());
            this.observations.offer(VanishSignal.position(VanishSignalType.POSITIONAL_SOUND, var1.method_11890(), var1.method_11889(), var1.method_11893(), var2));
        }
    }

    public static boolean shouldShowHud() {
        AntiVanishModule var0 = AntiVanishModule.instance();
        return var0 != null && var0.isEnabled() && var0.hasHudContent();
    }

    private boolean hasHudContent() {
        long var1 = System.currentTimeMillis();
        if (var1 < this.criticalUntilMs) {
            return true;
        }
        for (AlertCandidate var4 : this.detections.values()) {
            if (var4.expiresAt <= var1 || !AntiVanishModule.detectionWorthShowing(var4)) continue;
            return true;
        }
        return false;
    }

    private static boolean detectionWorthShowing(AlertCandidate var0) {
        if (var0 == null) {
            return false;
        }
        String var1 = var0.name == null ? "" : var0.name.trim();
        return !var1.isBlank() && !"Unknown".equalsIgnoreCase(var1) && !"You".equalsIgnoreCase(var1) && !"CRITICAL".equalsIgnoreCase(var1);
    }

    public static String hudTag(VanishHudEntry var0) {
        String var1;
        if (var0 == null) {
            return "WATCH";
        }
        if ("CRITICAL".equalsIgnoreCase(var0.name())) {
            return "ALERT";
        }
        String string = var1 = var0.reason() == null ? "" : var0.reason().toLowerCase(Locale.ROOT);
        if (var1.startsWith("vanish event")) {
            return "VANISH";
        }
        if (var1.startsWith("invisible entity")) {
            return "INVIS";
        }
        if (var1.startsWith("suspicious sound")) {
            return "SOUND";
        }
        return var1.startsWith("ghost particle") ? "PARTICLE" : "WATCH";
    }

    public static String hudValue(VanishHudEntry var0) {
        if (var0 == null) {
            return "Staff";
        }
        if ("CRITICAL".equalsIgnoreCase(var0.name())) {
            return AntiVanishModule.shortName(var0.reason());
        }
        String var1 = var0.name() == null ? "" : var0.name().trim();
        return !var1.isBlank() && !"Unknown".equalsIgnoreCase(var1) && !"You".equalsIgnoreCase(var1) ? AntiVanishModule.shortName(var1) : "Staff";
    }

    private static String shortName(String var0) {
        String var1 = var0 == null ? "" : NameFilter.apply(var0.trim());
        return var1.length() <= 16 ? var1 : var1.substring(0, 16);
    }

    public static List<VanishHudEntry> hudEntries() {
        AntiVanishModule var0 = AntiVanishModule.instance();
        return var0 != null && AntiVanishModule.shouldShowHud() ? var0.hudSnapshot() : List.of();
    }

    private static AntiVanishModule instance() {
        return ModuleRegistry.get(AntiVanishModule.class);
    }

    private void drainObservations() {
        VanishSignal var2;
        for (int var1 = 0; var1 < 512 && (var2 = this.observations.poll()) != null; ++var1) {
            this.processObservation(var2);
        }
        while (this.observations.size() > 4096) {
            this.observations.poll();
        }
    }

    private void processObservation(VanishSignal var1) {
        switch (var1.type().ordinal()) {
            case 0: {
                this.handleTabRemoval(var1.profileId());
                break;
            }
            case 1: {
                this.handleTabHidden(var1.profileId());
                break;
            }
            case 2: {
                this.serverSendsLeaveMessages = true;
                this.confirmDeparture(var1.detail());
                break;
            }
            case 3: {
                this.cacheRecentMessage(var1.detail());
                break;
            }
            case 4: {
                this.inspectInvisibleEntity(var1.entityId());
                break;
            }
            case 5: {
                this.inspectPositionalSound(var1);
                break;
            }
            case 6: {
                this.inspectEntitySound(var1);
                break;
            }
            case 7: {
                this.inspectParticle(var1);
                break;
            }
            case 8: {
                this.rememberExplosion(var1);
                break;
            }
            case 9: {
                this.announceGamemode(var1.profileId(), var1.detail());
            }
        }
    }

    private void trackListedPlayers() {
        if (MC.method_1562() != null) {
            long var1 = System.currentTimeMillis();
            for (class_640 var4 : MC.method_1562().method_45732()) {
                if (var4 == null || var4.method_2966() == null || var4.method_2966().id() == null) continue;
                UUID var5 = var4.method_2966().id();
                if (AntiVanishModule.MC.field_1724 != null && AntiVanishModule.MC.field_1724.method_5667().equals(var5)) continue;
                this.listedSinceMs.putIfAbsent(var5, var1);
                String var6 = var4.method_2966().name();
                if (var6 != null && !this.knownPlayers.containsKey(var5)) {
                    this.knownPlayers.put(var5, new StaffProfile(var5, var6));
                }
                if (var6 == null || this.hiddenSpells.isEmpty()) continue;
                this.endSpell(var6, false, 1000L);
            }
            if (this.listedSinceMs.size() > 1024 || this.knownPlayers.size() > 1024) {
                HashSet<UUID> var7 = new HashSet<UUID>();
                for (class_640 var9 : MC.method_1562().method_2880()) {
                    if (var9 == null || var9.method_2966() == null) continue;
                    var7.add(var9.method_2966().id());
                }
                this.listedSinceMs.keySet().retainAll(var7);
                this.knownPlayers.keySet().retainAll(var7);
            }
        }
    }

    private boolean trustedListed(UUID var1) {
        Long var2 = var1 == null ? null : this.listedSinceMs.get(var1);
        return var2 != null && System.currentTimeMillis() - var2 >= 1500L;
    }

    private boolean credibleSubject(UUID var1, String var2) {
        return var1 != null && VanishChatParser.isUsername(var2) && VanishAlertRules.isStaffName(var2) && this.trustedListed(var1);
    }

    private void handleTabRemoval(UUID var1) {
        String var2;
        if (var1 != null && !AntiVanishModule.MC.field_1724.method_5667().equals(var1) && this.credibleSubject(var1, var2 = this.knownName(var1)) && VanishChatParser.isPlausiblePlayerName(var2)) {
            this.pendingVanishes.removeIf(var1x -> var1x.uuid().equals(var1));
            this.pendingVanishes.addLast(new PendingTabRemoval(var1, var2, this.tickCounter + 20));
        }
    }

    private void handleTabHidden(UUID var1) {
        if (var1 != null && !AntiVanishModule.MC.field_1724.method_5667().equals(var1)) {
            String var3;
            StaffProfile var2 = this.knownPlayers.get(var1);
            String string = var3 = var2 != null && var2.name() != null ? var2.name() : this.knownName(var1);
            if (this.credibleSubject(var1, var3) && VanishChatParser.isPlausiblePlayerName(var3) && !this.recentMessageNames(var3)) {
                this.reportHidden(var1, var3, false, "Vanish Event: hidden from TAB", "hidden from TAB", 100);
            }
        }
    }

    private String knownName(UUID var1) {
        class_640 var3;
        StaffProfile var2 = this.knownPlayers.get(var1);
        if (var2 != null && var2.name() != null && !var2.name().isBlank()) {
            return var2.name();
        }
        if (MC.method_1562() != null && (var3 = MC.method_1562().method_2871(var1)) != null && var3.method_2966() != null) {
            return var3.method_2966().name();
        }
        return null;
    }

    private UUID knownUuid(String var1) {
        for (StaffProfile var3 : this.knownPlayers.values()) {
            if (!var3.name().equalsIgnoreCase(var1)) continue;
            return var3.uuid();
        }
        return null;
    }

    private void confirmDeparture(String var1) {
        long var2 = System.currentTimeMillis();
        for (StaffProfile var5 : this.knownPlayers.values()) {
            if (!VanishChatParser.containsPlayerName(var1, var5.name())) continue;
            this.confirmedDepartures.put(var5.uuid(), var2 + 5000L);
            this.pendingVanishes.removeIf(var1x -> var1x.uuid().equals(var5.uuid()));
            this.hiddenSpells.remove(AntiVanishModule.spellKey(var5.name()));
            AlertCandidate var6 = this.detections.get(var5.uuid().toString());
            if (var6 != null && AntiVanishModule.tabDepartureReason(var6.reason)) {
                this.detections.remove(var5.uuid().toString());
            }
            this.signals.removeIf(var1x -> var1x.type() == VanishEvidenceType.VANISH && var1x.subject().equalsIgnoreCase(var5.name()) && AntiVanishModule.tabDepartureReason(var1x.reason()));
        }
    }

    private void processPendingVanishes() {
        while (!this.pendingVanishes.isEmpty() && this.pendingVanishes.peekFirst().dueTick() <= this.tickCounter) {
            PendingTabRemoval var1 = this.pendingVanishes.removeFirst();
            if (MC.method_1562().method_2871(var1.uuid()) != null) continue;
            long var2 = System.currentTimeMillis();
            if (this.confirmedDepartures.getOrDefault(var1.uuid(), 0L) > var2) continue;
            if (this.recentMessageNames(var1.name())) {
                this.serverSendsLeaveMessages = true;
                continue;
            }
            class_1657 var4 = AntiVanishModule.MC.field_1687.method_18470(var1.uuid());
            if (var4 != null && !var4.method_31481()) {
                this.reportHidden(var1.uuid(), var1.name(), false, "Vanish Event: entity remained", "off TAB, still nearby", 100);
                continue;
            }
            String var5 = "Vanish Event: silent TAB disappearance";
            int var6 = AntiVanishModule.silentTabRemovalScore(this.serverSendsLeaveMessages);
            if (this.serverSendsLeaveMessages) {
                this.reportHidden(var1.uuid(), var1.name(), false, var5, "left TAB silently", var6);
                continue;
            }
            NearbyStaffProof var7 = this.regionProof(var1.uuid());
            if (var7 == null) continue;
            this.signals.addLast(new VanishFinding(VanishEvidenceType.VANISH, var1.name(), var7, var5, var6, var2));
            this.evaluateCritical();
        }
    }

    static int silentTabRemovalScore(boolean serverSendsLeaveMessages) {
        return serverSendsLeaveMessages ? 100 : 30;
    }

    static boolean tabDepartureReason(String reason) {
        if (reason == null) {
            return false;
        }
        String lower = reason.toLowerCase(Locale.ROOT);
        return lower.startsWith("vanish event:") && (lower.contains("tab") || lower.contains("entity remained") || lower.contains("no leave packet"));
    }

    private void cacheRecentMessage(String var1) {
        if (var1 != null && !var1.isBlank()) {
            long var2 = System.currentTimeMillis();
            this.recentMessages.addLast(new AntiVanishHelper(var1, var2));
            while (!(this.recentMessages.isEmpty() || var2 - this.recentMessages.peekFirst().atMs() <= 8000L && this.recentMessages.size() <= 64)) {
                this.recentMessages.removeFirst();
            }
        }
    }

    private boolean recentMessageNames(String var1) {
        if (var1 != null && !var1.isBlank()) {
            long var2 = System.currentTimeMillis();
            for (AntiVanishHelper var5 : this.recentMessages) {
                if (var2 - var5.atMs() > 8000L || !VanishChatParser.looksLikeLeaveMessage(var5.text(), var1)) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    private void sendCompletionProbe() {
        if (MC.method_1562() != null) {
            int var10000;
            String var1 = PROBE_COMMAND;
            int var2 = var10000 = this.nextCompletionId++;
            if (this.nextCompletionId > 40000) {
                this.nextCompletionId = 30000;
            }
            this.completionRequestIds.add(var2);
            while (this.completionRequestIds.size() > 8) {
                this.completionRequestIds.remove(this.completionRequestIds.iterator().next());
            }
            try {
                MC.method_1562().method_52787((class_2596)new class_2805(var2, var1.trim() + " "));
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    private void processCompletionProbe() {
        List<String> var1 = this.pendingCompletionNames;
        if (var1 != null) {
            this.pendingCompletionNames = null;
            if (MC.method_1562() != null && AntiVanishModule.MC.field_1724 != null) {
                HashSet<String> var2 = new HashSet<String>();
                for (class_640 var4 : MC.method_1562().method_2880()) {
                    if (var4 == null || var4.method_2966() == null || var4.method_2966().name() == null) continue;
                    var2.add(var4.method_2966().name().toLowerCase(Locale.ROOT));
                }
                String var7 = AntiVanishModule.MC.field_1724.method_5477().getString();
                ArrayList<String> var8 = new ArrayList<String>();
                for (String var6Object : var1) {
                    String var6 = var6Object;
                    if (!VanishAlertRules.isStaffName(var6) || !VanishChatParser.isPlausiblePlayerName(var6) || var6.equalsIgnoreCase(var7) || var2.contains(var6.toLowerCase(Locale.ROOT)) || this.recentMessageNames(var6)) continue;
                    var8.add(var6);
                }
                if (!var8.isEmpty() && var8.size() <= 3) {
                    Iterator<String> iterator = var8.iterator();
                    while (iterator.hasNext()) {
                        String var10Object;
                        String var10 = var10Object = iterator.next();
                        this.reportHidden(this.knownUuid(var10), var10, false, "Vanish Event: hidden but targetable", "off TAB, still targetable", 90);
                    }
                }
            }
        }
    }

    private static String departedPlayerName(class_2561 var0) {
        class_2588 var1;
        class_7417 class_74172;
        if (var0 != null && (class_74172 = var0.method_10851()) instanceof class_2588 && "multiplayer.player.left".equals((var1 = (class_2588)class_74172).method_11022())) {
            Object[] var5 = var1.method_11023();
            if (var5.length != 0 && var5[0] != null) {
                String string;
                Object object = var5[0];
                if (object instanceof class_2561) {
                    class_2561 var3 = (class_2561)object;
                    string = var3.getString();
                } else {
                    string = String.valueOf(var5[0]);
                }
                return string;
            }
            return "";
        }
        return "";
    }

    private void reportHidden(UUID var1, String var2, boolean var3, String var4, String var5, int var6) {
        this.upsertDetection(AntiVanishModule.detectionKey(var1, var2), var2, var4, var6, System.currentTimeMillis() + 20000L);
        VanishObservation var7 = this.hiddenSpells.get(AntiVanishModule.spellKey(var2));
        if (var7 == null || var7.spectator != var3) {
            this.hiddenSpells.put(AntiVanishModule.spellKey(var2), new VanishObservation(var1, var2, var3, var5, this.regionProof(var1), this.tickCounter));
        }
    }

    private void reviewSpells() {
        Iterator<VanishObservation> var1 = this.hiddenSpells.values().iterator();
        while (var1.hasNext()) {
            VanishObservation var2 = var1.next();
            if (var2.spectator && MC.method_1562().method_2871(var2.uuid) == null) {
                var1.remove();
                continue;
            }
            if (var2.watched || var2.sinceTick >= this.tickCounter) continue;
            NearbyStaffProof var3 = this.regionProof(var2.uuid);
            if (var3 == null && !var2.told) {
                var3 = var2.proof;
            }
            if (var3 != null) {
                var2.told = true;
                var2.watched = true;
                this.watchingYou(var2.name, var3);
                continue;
            }
            if (var2.told) continue;
            var2.told = true;
            this.news(var2.name, var2.how(), var2.detail);
        }
    }

    private boolean endSpell(String var1, boolean var2, long var3) {
        VanishObservation var5 = this.hiddenSpells.get(AntiVanishModule.spellKey(var1));
        if (var5 != null && var5.spectator == var2 && System.currentTimeMillis() - var5.sinceMs >= var3) {
            this.hiddenSpells.remove(AntiVanishModule.spellKey(var1));
            this.detections.remove(AntiVanishModule.detectionKey(var5.uuid, var5.name));
            if (var5.told) {
                this.news(var5.name, ModuleChat.body().text("is ").good("visible again").build(), "");
            }
            return true;
        }
        return false;
    }

    private NearbyStaffProof regionProof(UUID var1) {
        if (var1 == null) {
            return null;
        }
        class_1657 var2 = AntiVanishModule.MC.field_1687.method_18470(var1);
        if (var2 != null && var2 != AntiVanishModule.MC.field_1724) {
            return new NearbyStaffProof(var1, Math.round(var2.method_5739((class_1297)AntiVanishModule.MC.field_1724)));
        }
        return VanishAlertRules.isInYourRegion(var1) ? new NearbyStaffProof(var1, -1) : null;
    }

    private static String spellKey(String var0) {
        return var0.toLowerCase(Locale.ROOT);
    }

    private static String detectionKey(UUID var0, String var1) {
        return var0 != null ? var0.toString() : var1;
    }

    private void scanInvisiblePlayers() {
        double var1 = this.sensorRangeSq();
        for (class_1657 var4 : AntiVanishModule.MC.field_1687.method_18456()) {
            if (var4 == null || var4 == AntiVanishModule.MC.field_1724 || !var4.method_5767() || var4.method_5858((class_1297)AntiVanishModule.MC.field_1724) > var1 || !this.realPlayer(var4)) continue;
            this.triggerSensor(VanishEvidenceType.INVISIBLE, var4, "Invisible Entity: metadata flag", 35, 5000L);
        }
    }

    private void inspectInvisibleEntity(int var1) {
        class_1657 var2;
        class_1297 class_12972 = AntiVanishModule.MC.field_1687.method_8469(var1);
        if (class_12972 instanceof class_1657 && (var2 = (class_1657)class_12972) != AntiVanishModule.MC.field_1724 && var2.method_5767() && !(var2.method_5858((class_1297)AntiVanishModule.MC.field_1724) > this.sensorRangeSq()) && this.realPlayer(var2)) {
            this.triggerSensor(VanishEvidenceType.INVISIBLE, var2, "Invisible Entity: metadata flag", 35, 5000L);
        }
    }

    private boolean realPlayer(class_1657 var1) {
        return this.credibleSubject(var1.method_5667(), AntiVanishModule.profileName(var1));
    }

    private static String profileName(class_1657 var0) {
        return var0.method_7334() == null ? var0.method_5477().getString() : var0.method_7334().name();
    }

    private void inspectPositionalSound(VanishSignal var1) {
        if (VanishSignalClassifier.suspiciousSound(var1.detail())) {
            class_243 var2 = var1.position();
            long var3 = System.currentTimeMillis();
            if (!(!this.nearPlayer(var2) || this.hasVisibleCause(var2) || this.isExplosionRelated(var2) || this.isPoweredMechanism(var2, var1.detail()) || var3 - this.lastLocalActionMs < 1000L || this.recentlySelfPlaced(var2) || this.villagerToggledDoor(var1.detail(), var2) || this.selfContainerActive() && AntiVanishModule.isContainerSignal(var1.detail()))) {
                this.triggerSensor(VanishEvidenceType.SOUND, this.locatedSubject(var2), "Suspicious Sound: " + AntiVanishModule.shortId(var1.detail()), 14, 3000L);
            }
        }
    }

    private void inspectEntitySound(VanishSignal var1) {
        class_1657 var2;
        class_1297 class_12972;
        if (VanishSignalClassifier.suspiciousSound(var1.detail()) && (class_12972 = AntiVanishModule.MC.field_1687.method_8469(var1.entityId())) instanceof class_1657 && (var2 = (class_1657)class_12972) != AntiVanishModule.MC.field_1724 && var2.method_5767() && var2.method_5858((class_1297)AntiVanishModule.MC.field_1724) <= this.sensorRangeSq() && this.realPlayer(var2)) {
            this.triggerSensor(VanishEvidenceType.SOUND, var2, "Suspicious Sound: invisible source", 16, 3000L);
        }
    }

    private void inspectParticle(VanishSignal var1) {
        if (VanishSignalClassifier.suspiciousParticle(var1.detail())) {
            String var5;
            class_243 var2 = var1.position();
            long var3 = System.currentTimeMillis();
            if (!(!this.nearPlayer(var2) || this.hasVisibleCause(var2) || this.isExplosionRelated(var2) || var3 - this.lastLocalActionMs < 900L || this.hasAmbientParticleSource(var2, var1.detail()) || ((var5 = AntiVanishModule.shortId(var1.detail())).equals("block") || var5.contains("smoke")) && this.nearSelf(var2, 6.25) || (var5.equals("block") || var5.contains("smoke")) && !this.particleBurstReady(var5, var3))) {
                this.triggerSensor(VanishEvidenceType.PARTICLE, this.locatedSubject(var2), "Ghost Particle: " + AntiVanishModule.shortId(var1.detail()), 16, 3000L);
            }
        }
    }

    private boolean villagerToggledDoor(String var1, class_243 var2) {
        boolean var4;
        String var3 = VanishSignalClassifier.path(var1);
        boolean bl = var4 = var3.contains("door") && !var3.contains("trapdoor") || var3.contains("fence_gate");
        if (var4 && var2 != null && AntiVanishModule.MC.field_1687 != null) {
            for (class_1297 var6 : AntiVanishModule.MC.field_1687.method_18112()) {
                if (var6.method_5864() != class_1299.field_6077 || !(var6.method_73189().method_1025(var2) <= 9.0)) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean recentlySelfPlaced(class_243 var1) {
        if (var1 == null) {
            return false;
        }
        Long var2 = this.selfPlacedBlocks.get(class_2338.method_49638((class_2374)var1).method_10063());
        return var2 != null && var2 > System.currentTimeMillis();
    }

    private void rememberExplosion(VanishSignal var1) {
        long var2 = System.currentTimeMillis();
        this.recentExplosions.addLast(new SpatialSignal(var1.position(), Math.max(2.0, AntiVanishModule.parseDouble(var1.detail(), 4.0) + 4.0), var2));
        while (this.recentExplosions.size() > 8) {
            this.recentExplosions.removeFirst();
        }
    }

    private boolean particleBurstReady(String var1, long var2) {
        Deque var4 = this.weakParticleBursts.computeIfAbsent(var1, var0 -> new ArrayDeque());
        var4.addLast(var2);
        while (!var4.isEmpty() && var2 - (Long)var4.peekFirst() > 2000L) {
            var4.removeFirst();
        }
        if (var4.size() < 2) {
            return false;
        }
        var4.clear();
        return true;
    }

    private void triggerSensor(VanishEvidenceType var1, String var2, String var3, int var4, long var5) {
        this.addSignal(var1, var2, null, var3, var4, var5);
    }

    private void triggerSensor(VanishEvidenceType var1, class_1657 var2, String var3, int var4, long var5) {
        String var15 = AntiVanishModule.profileName(var2);
        UUID var17 = var2.method_5667();
        NearbyStaffProof var16 = this.regionProof(var17);
        this.addSignal(var1, var15, var16, var3, var4, var5);
    }

    private void addSignal(VanishEvidenceType var1, String var2, NearbyStaffProof var3, String var4, int var5, long var6) {
        String var10;
        long var11;
        long var8 = System.currentTimeMillis();
        if (var8 - (var11 = this.signalCooldowns.getOrDefault(var10 = var1.name() + "|" + var2.toLowerCase(Locale.ROOT), 0L).longValue()) >= var6) {
            this.signalCooldowns.put(var10, var8);
            this.signals.addLast(new VanishFinding(var1, var2, var3, var4, var5, var8));
            this.upsertDetection("signal:" + var10, var2, var4, var5, var8 + 20000L);
            this.announceTrigger(var10, var2, var3 != null, var4);
            this.evaluateCritical();
        }
    }

    private void evaluateCritical() {
        long var1 = System.currentTimeMillis();
        this.pruneSignals(var1);
        EnumMap<VanishEvidenceType, Integer> var3 = new EnumMap<VanishEvidenceType, Integer>(VanishEvidenceType.class);
        VanishFinding var4 = null;
        for (VanishFinding var6 : this.signals) {
            var3.merge(var6.type(), var6.weight(), Math::max);
            if (var6.proof() == null) continue;
            var4 = var6;
        }
        int var7 = var3.values().stream().mapToInt(Integer::intValue).sum();
        if (var4 != null && var3.size() >= 2 && var7 >= 35 && var1 - this.lastCriticalMs >= 12000L) {
            this.watchingYou(var4.subject(), var4.proof());
        }
    }

    private void watchingYou(String var1, NearbyStaffProof var2) {
        long var3;
        this.lastCriticalMs = var3 = System.currentTimeMillis();
        this.criticalUntilMs = var3 + 7000L;
        this.criticalWatcher = var1;
        this.pendingChimes.add(var2.id());
        this.ghostId = var2.id();
        this.ghostName = var1;
    }

    private void playPendingChime() {
        if (!this.pendingChimes.isEmpty()) {
            boolean var1 = this.pendingChimes.stream().allMatch(VanishAlertRules::alarmedRecently);
            this.pendingChimes.clear();
            if (!var1) {
                if (MC.method_1483() != null) {
                    MC.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)class_3417.field_15203, (float)1.0f, (float)1.0f));
                }
                PlayerWatchHud.watching(this.ghostId, this.ghostName, 0);
            }
        }
    }

    private void news(String var1, class_2561 var2, String var3) {
        ModuleChat.Body var4 = ModuleChat.body().name(NameFilter.apply(var1)).text(" ").append(var2);
        if (!var3.isEmpty()) {
            var4.text(" (" + var3 + ")");
        }
        ModuleChat.send((ClientModule)this, var4);
    }

    private void queueGamemodeUpdates(class_2703 var1) {
        if (!var1.method_46327().contains(class_2703.class_5893.field_29136)) {
            for (class_2703.class_2705 var3 : var1.method_46329()) {
                if (var3 == null || var3.comp_1110() == null || var3.comp_1106() == null) continue;
                this.observations.offer(VanishSignal.gamemode(var3.comp_1106(), var3.comp_1110().method_8381()));
            }
        }
    }

    private void announceGamemode(UUID var1, String var2) {
        StaffProfile var3;
        if (var1 != null && (AntiVanishModule.MC.field_1724 == null || !AntiVanishModule.MC.field_1724.method_5667().equals(var1)) && (var3 = this.knownPlayers.get(var1)) != null && VanishAlertRules.isStaffName(var3.name())) {
            if (class_1934.field_9219.method_8381().equals(var2)) {
                if (this.credibleSubject(var1, var3.name())) {
                    this.reportHidden(var1, var3.name(), true, "Vanish Event: switched to spectator", "", 100);
                    return;
                }
            } else if (this.endSpell(var3.name(), true, 0L)) {
                return;
            }
            ModuleChat.send((ClientModule)this, VanishAlertRules.switchedTo(ModuleChat.body().name(NameFilter.apply(var3.name())).text(" "), var2));
        }
    }

    private void announceTrigger(String var1, String var2, boolean var3, String var4) {
        long var5 = System.currentTimeMillis();
        Long var7 = this.announceCooldowns.get(var1);
        if (var7 == null || var5 - var7 >= 1500L) {
            while (!this.announceTimes.isEmpty() && var5 - this.announceTimes.peekFirst() > 4000L) {
                this.announceTimes.removeFirst();
            }
            if (this.announceTimes.size() < 6) {
                boolean var10;
                this.announceCooldowns.put(var1, var5);
                this.announceTimes.addLast(var5);
                int var8 = var4.indexOf(": ");
                ModuleChat.Body var9 = var8 < 0 ? ModuleChat.body().warn(var4) : ModuleChat.body().warn(var4.substring(0, var8)).text(var4.substring(var8));
                boolean bl = var10 = var2 != null && !var2.isBlank() && !"Unknown".equalsIgnoreCase(var2) && !"CRITICAL".equalsIgnoreCase(var2);
                if (var10) {
                    var9.text(" (");
                    if (var3) {
                        var9.name(NameFilter.apply(var2));
                    } else {
                        var9.value(var2);
                    }
                    var9.text(")");
                }
                ModuleChat.send((ClientModule)this, var9);
            }
        }
    }

    private void upsertDetection(String var1, String var2, String var3, int var4, long var5) {
        AlertCandidate var7 = this.detections.get(var1);
        if (var7 == null) {
            this.detections.put(var1, new AlertCandidate(var2, var3, var4, var5));
        } else {
            var7.name = var2;
            var7.expiresAt = Math.max(var7.expiresAt, var5);
            if (var4 >= var7.score) {
                var7.reason = var3;
                var7.score = var4;
            }
        }
    }

    private List<VanishHudEntry> hudSnapshot() {
        long var1 = System.currentTimeMillis();
        ArrayList<VanishHudEntry> var3 = new ArrayList<VanishHudEntry>();
        if (var1 < this.criticalUntilMs) {
            var3.add(new VanishHudEntry("CRITICAL", this.criticalWatcher));
        }
        HashSet var4 = new HashSet();
        this.detections.values().stream().filter(var2 -> var2.expiresAt > var1).filter(AntiVanishModule::detectionWorthShowing).sorted(Comparator.comparingInt(var0 -> var0.score).reversed().thenComparing(var0 -> var0.name, String.CASE_INSENSITIVE_ORDER)).map(var0 -> new VanishHudEntry(var0.name, var0.reason)).filter(var1x -> var4.add(AntiVanishModule.hudTag(var1x) + "\u0000" + AntiVanishModule.hudValue(var1x))).limit(4L).forEach(var3::add);
        return List.copyOf(var3);
    }

    private void pruneState() {
        long var1 = System.currentTimeMillis();
        this.detections.entrySet().removeIf(var2 -> ((AlertCandidate)var2.getValue()).expiresAt <= var1);
        this.signalCooldowns.entrySet().removeIf(var2 -> var1 - (Long)var2.getValue() > 60000L);
        this.announceCooldowns.entrySet().removeIf(var2 -> var1 - (Long)var2.getValue() > 60000L);
        this.selfPlacedBlocks.entrySet().removeIf(var2 -> (Long)var2.getValue() <= var1);
        this.confirmedDepartures.entrySet().removeIf(var2 -> (Long)var2.getValue() <= var1);
        this.automatedMechanisms.entrySet().removeIf(var2 -> (Long)var2.getValue() <= var1);
        this.weakParticleBursts.values().removeIf(var2 -> {
            while (!var2.isEmpty() && var1 - (Long)var2.peekFirst() > 2000L) {
                var2.removeFirst();
            }
            return var2.isEmpty();
        });
        while (!this.recentExplosions.isEmpty() && var1 - this.recentExplosions.peekFirst().timeMs() > 3000L) {
            this.recentExplosions.removeFirst();
        }
        this.pruneSignals(var1);
    }

    private void pruneSignals(long var1) {
        while (!this.signals.isEmpty() && var1 - this.signals.peekFirst().timeMs() > 15000L) {
            this.signals.removeFirst();
        }
    }

    private boolean nearPlayer(class_243 var1) {
        return var1 != null && var1.method_1025(AntiVanishModule.MC.field_1724.method_73189()) <= this.sensorRangeSq();
    }

    private boolean nearSelf(class_243 var1, double var2) {
        return var1 != null && AntiVanishModule.MC.field_1724 != null && var1.method_1025(AntiVanishModule.MC.field_1724.method_73189()) < var2;
    }

    private boolean selfContainerActive() {
        return System.currentTimeMillis() - this.lastContainerActivityMs < 2500L;
    }

    private static boolean isContainerSignal(String var0) {
        String var1 = VanishSignalClassifier.path(var0);
        return var1.contains("chest") || var1.contains("barrel") || var1.contains("shulker");
    }

    private String locatedSubject(class_243 var1) {
        if (var1 != null && AntiVanishModule.MC.field_1724 != null) {
            class_243 var2 = AntiVanishModule.MC.field_1724.method_73189();
            double var3 = var1.field_1352 - var2.field_1352;
            double var5 = var1.field_1351 - var2.field_1351;
            double var7 = var1.field_1350 - var2.field_1350;
            long var9 = Math.round(Math.sqrt(var3 * var3 + var5 * var5 + var7 * var7));
            String var11 = AntiVanishModule.compass(var3, var7);
            return var11.isEmpty() ? var9 + "m" : var9 + "m " + var11;
        }
        return "nearby";
    }

    private static String compass(double var0, double var2) {
        String var4;
        String string = var2 < -1.0 ? "N" : (var4 = var2 > 1.0 ? "S" : "");
        String var5 = var0 > 1.0 ? "E" : (var0 < -1.0 ? "W" : "");
        return var4 + var5;
    }

    private boolean hasVisibleCause(class_243 var1) {
        if (var1 == null) {
            return true;
        }
        for (class_1297 var3 : AntiVanishModule.MC.field_1687.method_18112()) {
            class_1657 var4;
            if (var3 == AntiVanishModule.MC.field_1724 || !(var3 instanceof class_1657 ? !(var4 = (class_1657)var3).method_5767() && var3.method_73189().method_1025(var1) <= 16.0 : var3 instanceof class_1676 && var3.method_73189().method_1025(var1) <= 16.0)) continue;
            return true;
        }
        return false;
    }

    private boolean isExplosionRelated(class_243 var1) {
        if (var1 == null) {
            return false;
        }
        long var2 = System.currentTimeMillis();
        while (!this.recentExplosions.isEmpty() && var2 - this.recentExplosions.peekFirst().timeMs() > 3000L) {
            this.recentExplosions.removeFirst();
        }
        for (SpatialSignal var5 : this.recentExplosions) {
            if (!(var5.center().method_1025(var1) <= var5.radius() * var5.radius())) continue;
            return true;
        }
        return false;
    }

    private boolean hasAmbientParticleSource(class_243 var1, String var2) {
        if (var1 != null && AntiVanishModule.shortId(var2).contains("smoke")) {
            class_2338 var3 = class_2338.method_49638((class_2374)var1);
            for (int var4 = -2; var4 <= 2; ++var4) {
                for (int var5 = -2; var5 <= 2; ++var5) {
                    for (int var6 = -2; var6 <= 2; ++var6) {
                        class_2338 var7 = var3.method_10069(var4, var5, var6);
                        class_2960 var8 = class_7923.field_41175.method_10221((Object)AntiVanishModule.MC.field_1687.method_8320(var7).method_26204());
                        String var9 = AntiVanishModule.shortId(var8 == null ? "" : var8.toString());
                        if (!var9.contains("campfire") && !var9.contains("furnace") && !var9.contains("smoker") && !var9.contains("torch") && !var9.contains("fire") && !var9.contains("candle") && !var9.contains("respawn_anchor")) continue;
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private boolean isPoweredMechanism(class_243 var1, String var2) {
        String var3 = AntiVanishModule.shortId(var2);
        if (!var3.contains("door") && !var3.contains("trapdoor")) {
            return false;
        }
        class_2338 var4 = class_2338.method_49638((class_2374)var1);
        long var5 = System.currentTimeMillis();
        for (int var7 = -1; var7 <= 1; ++var7) {
            boolean var12;
            class_2338 var8 = var4.method_10069(0, var7, 0);
            class_2680 var9 = AntiVanishModule.MC.field_1687.method_8320(var8);
            long var10 = var8.method_10063();
            boolean bl = var12 = var9.method_28498((class_2769)class_2741.field_12484) && (Boolean)var9.method_11654((class_2769)class_2741.field_12484) != false;
            if (var12 || AntiVanishModule.MC.field_1687.method_49803(var8)) {
                this.automatedMechanisms.put(var10, var5 + 5000L);
                return true;
            }
            if (this.automatedMechanisms.getOrDefault(var10, 0L) <= var5) continue;
            return true;
        }
        return false;
    }

    private double sensorRangeSq() {
        double var1 = 64.0;
        return var1 * var1;
    }

    private static double parseDouble(String var0, double var1) {
        try {
            return Double.parseDouble(var0);
        }
        catch (Exception var4) {
            return var1;
        }
    }

    private static String soundId(class_2960 var0) {
        return var0 == null ? "" : var0.toString();
    }

    private static String shortId(String var0) {
        return VanishSignalClassifier.path(var0);
    }

    private void resetRuntime() {
        this.observations.clear();
        this.knownPlayers.clear();
        this.detections.clear();
        this.signalCooldowns.clear();
        this.announceCooldowns.clear();
        this.announceTimes.clear();
        this.selfPlacedBlocks.clear();
        this.confirmedDepartures.clear();
        this.automatedMechanisms.clear();
        this.weakParticleBursts.clear();
        this.signals.clear();
        this.recentExplosions.clear();
        this.pendingVanishes.clear();
        this.hiddenSpells.clear();
        this.shard.clear();
        this.tickCounter = 0;
        this.lastLocalActionMs = 0L;
        this.lastContainerActivityMs = 0L;
        this.lastCriticalMs = 0L;
        this.criticalUntilMs = 0L;
        this.criticalWatcher = "";
        this.pendingChimes.clear();
        this.ghostId = null;
        this.ghostName = null;
        this.recentMessages.clear();
        this.listedSinceMs.clear();
        this.serverSendsLeaveMessages = false;
        this.completionRequestIds.clear();
        this.pendingCompletionNames = null;
        this.nextCompletionId = 30000;
    }

    @Environment(value=EnvType.CLIENT)
    private record VanishSignal(VanishSignalType type, UUID profileId, int entityId, double x, double y, double z, String detail) {
        static VanishSignal tabRemove(UUID var0) {
            return new VanishSignal(VanishSignalType.TAB_REMOVE, var0, -1, 0.0, 0.0, 0.0, "");
        }

        static VanishSignal tabHide(UUID var0) {
            return new VanishSignal(VanishSignalType.TAB_HIDE, var0, -1, 0.0, 0.0, 0.0, "");
        }

        static VanishSignal gamemode(UUID var0, String var1) {
            return new VanishSignal(VanishSignalType.GAMEMODE, var0, -1, 0.0, 0.0, 0.0, var1);
        }

        static VanishSignal playerLeft(String var0) {
            return new VanishSignal(VanishSignalType.PLAYER_LEFT, null, -1, 0.0, 0.0, 0.0, var0);
        }

        static VanishSignal systemChat(String var0) {
            return new VanishSignal(VanishSignalType.SYSTEM_CHAT, null, -1, 0.0, 0.0, 0.0, var0);
        }

        static VanishSignal entityMetadata(int var0) {
            return new VanishSignal(VanishSignalType.ENTITY_METADATA, null, var0, 0.0, 0.0, 0.0, "");
        }

        static VanishSignal explosion(class_243 var0, float var1) {
            return new VanishSignal(VanishSignalType.EXPLOSION, null, -1, var0.field_1352, var0.field_1351, var0.field_1350, Float.toString(var1));
        }

        static VanishSignal entitySound(int var0, String var1) {
            return new VanishSignal(VanishSignalType.ENTITY_SOUND, null, var0, 0.0, 0.0, 0.0, var1);
        }

        static VanishSignal position(VanishSignalType var0, double var1, double var3, double var5, String var7) {
            return new VanishSignal(var0, null, -1, var1, var3, var5, var7);
        }

        class_243 position() {
            return new class_243(this.x, this.y, this.z);
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static enum VanishSignalType {
        TAB_REMOVE,
        TAB_HIDE,
        PLAYER_LEFT,
        SYSTEM_CHAT,
        ENTITY_METADATA,
        POSITIONAL_SOUND,
        ENTITY_SOUND,
        PARTICLE,
        EXPLOSION,
        GAMEMODE;

    }

    @Environment(value=EnvType.CLIENT)
    private static final class AlertCandidate {
        String name;
        String reason;
        int score;
        long expiresAt;

        AlertCandidate(String name, String reason, int score, long expiresAt) {
            this.name = name;
            this.reason = reason;
            this.score = score;
            this.expiresAt = expiresAt;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record VanishHudEntry(String name, String reason) {
    }

    @Environment(value=EnvType.CLIENT)
    private record StaffProfile(UUID uuid, String name) {
    }

    @Environment(value=EnvType.CLIENT)
    private record PendingTabRemoval(UUID uuid, String name, int dueTick) {
    }

    @Environment(value=EnvType.CLIENT)
    private record NearbyStaffProof(UUID id, int distance) {
    }

    @Environment(value=EnvType.CLIENT)
    private record VanishFinding(VanishEvidenceType type, String subject, NearbyStaffProof proof, String reason, int weight, long timeMs) {
    }

    @Environment(value=EnvType.CLIENT)
    private static enum VanishEvidenceType {
        VANISH,
        INVISIBLE,
        PARTICLE,
        SOUND;

    }

    @Environment(value=EnvType.CLIENT)
    private record AntiVanishHelper(String text, long atMs) {
    }

    @Environment(value=EnvType.CLIENT)
    private static final class VanishObservation {
        final UUID uuid;
        final String name;
        final boolean spectator;
        final String detail;
        final NearbyStaffProof proof;
        final int sinceTick;
        final long sinceMs = System.currentTimeMillis();
        boolean told;
        boolean watched;

        VanishObservation(UUID uuid, String name, boolean spectator, String detail, NearbyStaffProof proof, int sinceTick) {
            this.uuid = uuid;
            this.name = name;
            this.spectator = spectator;
            this.detail = detail;
            this.proof = proof;
            this.sinceTick = sinceTick;
        }

        class_2561 how() {
            return this.spectator ? VanishAlertRules.switchedTo(ModuleChat.body(), class_1934.field_9219.method_8381()).build() : ModuleChat.body().danger("vanished").build();
        }
    }

    @Environment(value=EnvType.CLIENT)
    private record SpatialSignal(class_243 center, double radius, long timeMs) {
    }
}

