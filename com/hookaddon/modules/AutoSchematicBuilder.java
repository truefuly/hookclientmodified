/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 */
package com.hookaddon.modules;

import com.hookaddon.HookAddon;
import com.hookaddon.persist.HookStore;
import com.hookaddon.schematic.Blueprints;
import com.hookaddon.schematic.PlacementEngine;
import com.hookaddon.schematic.PlacementPlanner;
import com.hookaddon.schematic.Schematic;
import com.hookaddon.schematic.SchematicException;
import com.hookaddon.schematic.SchematicFormats;
import com.hookaddon.schematic.TransformMath;
import com.hookaddon.task.Capability;
import com.hookaddon.task.ConflictPolicy;
import com.hookaddon.task.TaskManager;
import com.hookaddon.util.HookStatus;
import hook.compat.meteorclient.events.game.GameLeftEvent;
import hook.compat.meteorclient.events.render.Render3DEvent;
import hook.compat.meteorclient.events.world.TickEvent;
import hook.compat.meteorclient.renderer.ShapeMode;
import hook.compat.meteorclient.settings.BlockPosSetting;
import hook.compat.meteorclient.settings.BlockSetting;
import hook.compat.meteorclient.settings.BoolSetting;
import hook.compat.meteorclient.settings.ColorSetting;
import hook.compat.meteorclient.settings.DoubleSetting;
import hook.compat.meteorclient.settings.EnumSetting;
import hook.compat.meteorclient.settings.IntSetting;
import hook.compat.meteorclient.settings.KeybindSetting;
import hook.compat.meteorclient.settings.Setting;
import hook.compat.meteorclient.settings.SettingGroup;
import hook.compat.meteorclient.settings.StringSetting;
import hook.compat.meteorclient.systems.modules.Module;
import hook.compat.meteorclient.utils.misc.Keybind;
import hook.compat.meteorclient.utils.render.color.Color;
import hook.compat.meteorclient.utils.render.color.SettingColor;
import hook.compat.orbit.EventHandler;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class AutoSchematicBuilder
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlacement;
    private final SettingGroup sgSafety;
    private final SettingGroup sgRender;
    private final SettingGroup sgKeys;
    private final Setting<Blueprints.Blueprint> blueprint;
    private final Setting<class_2248> blueprintMaterial;
    private final Setting<String> schematic;
    private final Setting<PlacementPlanner.Anchor> anchor;
    private final Setting<class_2338> origin;
    private final Setting<TransformMath.Mirror> mirror;
    private final Setting<TransformMath.Rotation> rotation;
    private final Setting<Boolean> autoResume;
    private final Setting<Integer> maxOperationSize;
    private final Setting<ConflictPolicy> conflictPolicy;
    private final Setting<Integer> placeDelay;
    private final Setting<Double> reach;
    private final Setting<Boolean> autoNavigate;
    private final Setting<Boolean> rotateSetting;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> skipMissing;
    private final Setting<Boolean> placeSupport;
    private final Setting<class_2248> supportBlock;
    private final Setting<Boolean> autoMove;
    private final Setting<Integer> verifyBudget;
    private final Setting<Integer> verifyTimeout;
    private final Setting<Integer> maxAttempts;
    private final Setting<Boolean> autoTool;
    private final Setting<Integer> minToolDurability;
    private final Setting<Boolean> avoidLava;
    private final Setting<Boolean> avoidFluids;
    private final Setting<Boolean> avoidFalling;
    private final Setting<Boolean> preview;
    private final Setting<SettingColor> boundsColor;
    private final Setting<SettingColor> pendingColor;
    private final Setting<Integer> maxPreviewBlocks;
    private final Setting<Keybind> pauseKey;
    private final Setting<Keybind> stopKey;
    private final PlacementEngine engine;
    private Schematic schematicFile;
    private PlacementPlanner.Plan plan;
    private boolean claimed;
    private boolean interrupted;

    public AutoSchematicBuilder() {
        super(HookAddon.CATEGORY, "auto-schematic-builder", "Builds a schematic from config/hook-addon/schematics block by block, verifying every placement.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlacement = this.settings.createGroup("Placement");
        this.sgSafety = this.settings.createGroup("Safety");
        this.sgRender = this.settings.createGroup("Render");
        this.sgKeys = this.settings.createGroup("Keys");
        this.blueprint = this.sgGeneral.add(new EnumSetting.Builder().name("blueprint").description("Place a built-in shape instead of a schematic file; takes effect on the next start").defaultValue(Blueprints.Blueprint.NONE).onChanged(value -> {
            if (this.isActive() && value != Blueprints.Blueprint.NONE && this.mc.field_1724 != null) {
                this.startBuild(null);
            }
        }).build());
        this.blueprintMaterial = this.sgGeneral.add(new BlockSetting.Builder().name("blueprint-material").description("Block the selected blueprint is built from").defaultValue(class_2246.field_10445).visible(() -> this.blueprint.get() != Blueprints.Blueprint.NONE).onChanged(value -> {
            if (this.isActive() && this.blueprint.get() != Blueprints.Blueprint.NONE && this.mc.field_1724 != null) {
                this.startBuild(null);
            }
        }).build());
        this.schematic = this.sgGeneral.add(new StringSetting.Builder().name("schematic").description("Schematic file name inside config/hook-addon/schematics").defaultValue("").placeholder("house.litematic").visible(() -> this.blueprint.get() == Blueprints.Blueprint.NONE).build());
        this.anchor = this.sgGeneral.add(new EnumSetting.Builder().name("anchor").description("Where the schematic is placed in the world").defaultValue(PlacementPlanner.Anchor.POSITION).build());
        this.origin = this.sgGeneral.add(new BlockPosSetting.Builder().name("origin").description("World position the schematic corner is placed at").defaultValue(class_2338.field_10980).visible(() -> this.anchor.get() == PlacementPlanner.Anchor.POSITION).build());
        this.mirror = this.sgGeneral.add(new EnumSetting.Builder().name("mirror").description("Mirrors the schematic before rotating it").defaultValue(TransformMath.Mirror.NONE).build());
        this.rotation = this.sgGeneral.add(new EnumSetting.Builder().name("rotation").description("Rotates the schematic around its origin").defaultValue(TransformMath.Rotation.NONE).build());
        this.autoResume = this.sgGeneral.add(new BoolSetting.Builder().name("auto-resume").description("Reopens the last build when the module is enabled again").defaultValue(true).build());
        this.maxOperationSize = this.sgGeneral.add(new IntSetting.Builder().name("max-operation-size").description("Refuse to plan a build larger than this many blocks").defaultValue(200000).sliderRange(256.0, 1000000.0).build());
        this.conflictPolicy = this.sgGeneral.add(new EnumSetting.Builder().name("conflict-policy").description("What to do when another Hook module already owns movement or mining").defaultValue(ConflictPolicy.REJECT).build());
        this.placeDelay = this.sgPlacement.add(new IntSetting.Builder().name("place-delay").description("Ticks between two placements").defaultValue(2).range(0.0, 40.0).build());
        this.reach = this.sgPlacement.add(new DoubleSetting.Builder().name("reach").description("Interaction distance in blocks").defaultValue(4.5).range(3.0, 6.0).build());
        this.autoNavigate = this.sgPlacement.add(new BoolSetting.Builder().name("navigate").description("Walk to blocks that are out of reach").defaultValue(true).build());
        this.rotateSetting = this.sgPlacement.add(new BoolSetting.Builder().name("rotate").description("Look at a block before placing or breaking it").defaultValue(true).build());
        this.swing = this.sgPlacement.add(new BoolSetting.Builder().name("swing").description("Swing the hand while placing and breaking").defaultValue(true).build());
        this.skipMissing = this.sgPlacement.add(new BoolSetting.Builder().name("skip-missing-materials").description("Skip blocks whose material is not in the inventory instead of waiting").defaultValue(true).build());
        this.placeSupport = this.sgPlacement.add(new BoolSetting.Builder().name("place-support").description("Place a support block when a target has nothing to be placed against").defaultValue(true).build());
        this.supportBlock = this.sgPlacement.add(new BlockSetting.Builder().name("support-block").description("Block used for support placements").defaultValue(class_2246.field_10445).visible(this.placeSupport::get).build());
        this.autoMove = this.sgPlacement.add(new BoolSetting.Builder().name("auto-move-to-hotbar").description("Move building materials into an empty hotbar slot automatically").defaultValue(true).build());
        this.verifyBudget = this.sgPlacement.add(new IntSetting.Builder().name("verify-per-tick").description("Positions checked per tick while verifying a build").defaultValue(1000).sliderRange(50.0, 5000.0).build());
        this.verifyTimeout = this.sgPlacement.add(new IntSetting.Builder().name("confirm-timeout").description("Ticks to wait for the server to confirm a placement").defaultValue(60).range(5.0, 400.0).build());
        this.maxAttempts = this.sgPlacement.add(new IntSetting.Builder().name("max-attempts").description("Attempts per block before it is reported as failed").defaultValue(3).range(1.0, 10.0).build());
        this.autoTool = this.sgSafety.add(new BoolSetting.Builder().name("auto-tool").description("Select the fastest tool when breaking a wrong block").defaultValue(true).build());
        this.minToolDurability = this.sgSafety.add(new IntSetting.Builder().name("min-tool-durability").description("Do not use a tool that has fewer durability points left").defaultValue(10).range(0.0, 500.0).visible(this.autoTool::get).build());
        this.avoidLava = this.sgSafety.add(new BoolSetting.Builder().name("avoid-lava").description("Never break blocks touching lava").defaultValue(true).build());
        this.avoidFluids = this.sgSafety.add(new BoolSetting.Builder().name("avoid-fluids").description("Never break blocks touching any fluid").defaultValue(false).build());
        this.avoidFalling = this.sgSafety.add(new BoolSetting.Builder().name("avoid-falling-blocks").description("Never break a block that has sand or gravel above it").defaultValue(true).build());
        this.preview = this.sgRender.add(new BoolSetting.Builder().name("preview").description("Render the schematic bounds and the blocks that are still pending").defaultValue(true).build());
        this.boundsColor = this.sgRender.add(new ColorSetting.Builder().name("bounds-color").description("Colour of the schematic outline").defaultValue(new SettingColor(157, 89, 248, 200)).build());
        this.pendingColor = this.sgRender.add(new ColorSetting.Builder().name("pending-color").description("Colour of pending blocks and the current target").defaultValue(new SettingColor(64, 220, 255, 160)).visible(this.preview::get).build());
        this.maxPreviewBlocks = this.sgRender.add(new IntSetting.Builder().name("max-preview-blocks").description("Upper bound on how many pending blocks are drawn").defaultValue(512).sliderRange(16.0, 4096.0).visible(this.preview::get).build());
        this.pauseKey = this.sgKeys.add(new KeybindSetting.Builder().name("pause-key").description("Pause and resume the current build").defaultValue(Keybind.none()).action(this::togglePause).build());
        this.stopKey = this.sgKeys.add(new KeybindSetting.Builder().name("stop-key").description("Cancel the current build without disabling the module").defaultValue(Keybind.none()).action(this::stopBuild).build());
        this.engine = new PlacementEngine(this.name, this);
    }

    public PlacementEngine engine() {
        return this.engine;
    }

    public Schematic loadedSchematic() {
        return this.schematicFile;
    }

    @Override
    public void onActivate() {
        HookStore.ensureDirectories();
        TaskManager.setLogSink(message -> HookAddon.LOG.debug("[task] {}", message));
        if (!this.ensureClaim()) {
            return;
        }
        String requested = this.schematic.get();
        HookStore.BuildState saved = HookStore.loadBuildState();
        if (this.autoResume.get().booleanValue() && saved != null && this.blueprint.get() == Blueprints.Blueprint.NONE && (requested == null || requested.isBlank())) {
            Blueprints.Blueprint savedBlueprint;
            this.anchor.set(AutoSchematicBuilder.parseAnchor(saved.anchor));
            this.origin.set(new class_2338(saved.originX, saved.originY, saved.originZ));
            this.mirror.set(AutoSchematicBuilder.parseMirror(saved.mirror));
            this.rotation.set(AutoSchematicBuilder.parseRotation(saved.rotation));
            Blueprints.Blueprint blueprint = savedBlueprint = saved.blueprint == null ? Blueprints.Blueprint.NONE : Blueprints.find(saved.blueprint);
            if (savedBlueprint != Blueprints.Blueprint.NONE) {
                class_2248 material = AutoSchematicBuilder.parseMaterial(saved.blueprintMaterial);
                this.blueprint.set(savedBlueprint);
                if (material != null) {
                    this.blueprintMaterial.set(material);
                }
                this.info("Resuming blueprint %s", savedBlueprint.title);
            } else if (saved.schematicFile != null && !saved.schematicFile.isBlank()) {
                this.schematic.set(saved.schematicFile);
                this.info("Resuming build of %s", saved.schematicFile);
            }
        }
        if (!this.startBuildInternal(this.schematic.get())) {
            this.disable();
        }
    }

    @Override
    public void onDeactivate() {
        this.engine.shutdown();
        if (this.claimed) {
            TaskManager.get().release(this.name);
            this.claimed = false;
        }
        HookStatus.reset("schematic");
    }

    private void forceStop() {
        this.engine.cancel();
        if (this.isActive()) {
            this.disable();
        }
    }

    private boolean ensureClaim() {
        if (this.claimed) {
            return true;
        }
        if (!this.isActive()) {
            return false;
        }
        TaskManager.ClaimResult claim = TaskManager.get().claim(this.name, this.title, EnumSet.of(Capability.MOVE, Capability.MINE, Capability.INTERACT), this.conflictPolicy.get(), this::forceStop);
        if (!claim.ok()) {
            this.error(claim.message(), new Object[0]);
            this.disable();
            return false;
        }
        this.claimed = true;
        return true;
    }

    public boolean startBuild(String fileName) {
        if (!this.ensureClaim()) {
            return false;
        }
        return this.startBuildInternal(fileName);
    }

    private boolean startBuildInternal(String fileName) {
        Schematic parsed;
        Path file;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            this.error("Join a world before starting a build.", new Object[0]);
            return false;
        }
        if (this.blueprint.get() != Blueprints.Blueprint.NONE) {
            Schematic shape;
            try {
                shape = Blueprints.build(this.blueprint.get(), this.blueprintMaterial.get());
            }
            catch (RuntimeException e) {
                this.error("Could not build the '%s' blueprint: %s", this.blueprint.get().title, e.getMessage());
                return false;
            }
            return this.startPlan(shape, this.blueprint.get().title);
        }
        if (fileName == null || fileName.isBlank()) {
            this.error("No schematic selected. Put a %s file in %s and set the 'schematic' setting.", String.join((CharSequence)"/", AutoSchematicBuilder.schematicExtensions()), HookStore.schematicsDir());
            return false;
        }
        try {
            file = this.resolveSchematicFile(fileName);
        }
        catch (SchematicException e) {
            this.error(e.getMessage(), new Object[0]);
            return false;
        }
        if (!Files.isRegularFile(file, new LinkOption[0])) {
            this.error("Schematic '%s' was not found in %s", fileName, HookStore.schematicsDir());
            return false;
        }
        try {
            parsed = SchematicFormats.load(file);
        }
        catch (SchematicException e) {
            this.error("Could not load '%s': %s", fileName, e.getMessage());
            return false;
        }
        catch (Exception e) {
            this.error("Could not load '%s': %s", fileName, e);
            HookAddon.LOG.error("Schematic parsing failed for {}", (Object)file, (Object)e);
            return false;
        }
        return this.startPlan(parsed, fileName);
    }

    private boolean startPlan(Schematic parsed, String sourceLabel) {
        PlacementPlanner.Plan newPlan = PlacementPlanner.build(parsed, this.mirror.get(), this.rotation.get(), this.anchor.get(), this.origin.get());
        if (newPlan.targets().isEmpty()) {
            this.error("'%s' contains no blocks to place.", sourceLabel);
            return false;
        }
        if (newPlan.targets().size() > this.maxOperationSize.get()) {
            this.error("'%s' has %d blocks, which is above the max-operation-size of %d.", sourceLabel, newPlan.targets().size(), this.maxOperationSize.get());
            return false;
        }
        this.schematicFile = parsed;
        this.plan = newPlan;
        this.engine.setPlan(newPlan);
        HookStatus.reset("schematic");
        this.saveState(sourceLabel);
        this.info("Loaded %s (%s) - %d x %d x %d, %d blocks to place", sourceLabel, parsed.format, newPlan.sizeX(), newPlan.sizeY(), newPlan.sizeZ(), newPlan.targets().size());
        return true;
    }

    public void togglePause() {
        if (!this.isActive()) {
            return;
        }
        if (this.engine.phase() == PlacementEngine.Phase.PAUSED) {
            this.engine.resume();
            this.info("Build resumed.", new Object[0]);
        } else if (this.engine.phase() == PlacementEngine.Phase.BUILDING || this.engine.phase() == PlacementEngine.Phase.VERIFYING) {
            this.engine.pause();
            this.info("Build paused at %s.", this.describeProgress());
        } else {
            this.info("Nothing to pause.", new Object[0]);
        }
    }

    public void stopBuild() {
        if (!this.isActive()) {
            return;
        }
        this.engine.cancel();
        HookStore.clearBuildState();
        this.info("Build cancelled.", new Object[0]);
    }

    public void restartBuild() {
        if (this.plan == null) {
            this.error("No schematic loaded.", new Object[0]);
            return;
        }
        this.engine.restart();
        HookStatus.reset("schematic");
        this.info("Re-verifying the build from scratch.", new Object[0]);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.plan == null) {
            return;
        }
        if (!this.ensureClaim()) {
            return;
        }
        if (this.interrupted) {
            this.interrupted = false;
            PlacementEngine.Phase phaseBefore = this.engine.phase();
            if (phaseBefore == PlacementEngine.Phase.VERIFYING || phaseBefore == PlacementEngine.Phase.BUILDING || phaseBefore == PlacementEngine.Phase.PAUSED) {
                this.engine.setPlan(this.plan);
                HookStatus.info(this, "Reconnected: re-verifying the build (%d blocks).", this.plan.targets().size());
            }
        }
        PlacementEngine.Phase before = this.engine.phase();
        this.engine.tick(this.engineOptions());
        if (before == PlacementEngine.Phase.VERIFYING && this.engine.phase() == PlacementEngine.Phase.BUILDING) {
            this.saveState(this.schematic.get());
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (this.plan != null) {
            this.saveState(this.schematic.get());
        }
        this.engine.shutdown();
        if (this.claimed) {
            TaskManager.get().release(this.name);
            this.claimed = false;
        }
        this.interrupted = true;
    }

    private PlacementEngine.Options engineOptions() {
        return new PlacementEngine.Options(this.reach.get(), this.autoNavigate.get(), this.rotateSetting.get(), this.swing.get(), this.placeDelay.get(), this.verifyTimeout.get(), this.maxAttempts.get(), this.verifyBudget.get(), this.skipMissing.get(), this.placeSupport.get(), this.supportBlock.get(), this.autoMove.get(), this.autoTool.get(), this.minToolDurability.get(), this.avoidLava.get(), this.avoidFluids.get(), this.avoidFalling.get());
    }

    @EventHandler
    private void onRender3d(Render3DEvent event) {
        if (!this.preview.get().booleanValue() || this.plan == null) {
            return;
        }
        class_238 bounds = new class_238((double)this.plan.origin().method_10263(), (double)this.plan.origin().method_10264(), (double)this.plan.origin().method_10260(), (double)(this.plan.origin().method_10263() + this.plan.sizeX()), (double)(this.plan.origin().method_10264() + this.plan.sizeY()), (double)(this.plan.origin().method_10260() + this.plan.sizeZ()));
        event.renderer.box(bounds, (Color)this.boundsColor.get(), (Color)this.boundsColor.get(), ShapeMode.Lines, 0.0);
        List<class_2338> pending = this.engine.pendingPositions();
        int limit = Math.min(pending.size(), this.maxPreviewBlocks.get());
        for (int i = 0; i < limit; ++i) {
            event.renderer.box(pending.get(i), (Color)this.pendingColor.get(), (Color)this.pendingColor.get(), ShapeMode.Lines, 0.0);
        }
        class_2338 current = this.engine.currentTarget();
        if (current != null) {
            event.renderer.box(current, Color.WHITE, (Color)this.pendingColor.get(), ShapeMode.Both, 0.0);
        }
    }

    @Override
    public String getInfoString() {
        return this.plan == null ? null : this.describeProgress();
    }

    public String describeProgress() {
        if (this.plan == null) {
            return "no schematic";
        }
        PlacementEngine.Status status = this.engine.status();
        int total = status.total();
        int done = status.verified();
        int percent = total == 0 ? 0 : (int)Math.round((double)done * 100.0 / (double)total);
        StringBuilder builder = new StringBuilder().append(done).append('/').append(total).append(" (").append(percent).append("%)");
        if (status.skipped() > 0) {
            builder.append(" skipped ").append(status.skipped());
        }
        if (status.failed() > 0) {
            builder.append(" failed ").append(status.failed());
        }
        if (status.etaSeconds() >= 0.0) {
            builder.append(", ETA ").append(AutoSchematicBuilder.formatSeconds(status.etaSeconds()));
        }
        return builder.toString();
    }

    public String describeFull() {
        if (this.plan == null) {
            return "no schematic loaded";
        }
        PlacementEngine.Status status = this.engine.status();
        return "phase=" + String.valueOf((Object)status.phase()) + ", " + this.describeProgress() + ", missing=" + status.missingBlocks() + ", incorrect=" + status.wrongBlocks() + ", " + status.message();
    }

    private static String formatSeconds(double seconds) {
        int total = (int)Math.round(seconds);
        if (total < 60) {
            return total + "s";
        }
        return total / 60 + "m " + total % 60 + "s";
    }

    private void saveState(String fileName) {
        boolean useBlueprint;
        if (this.plan == null) {
            return;
        }
        boolean bl = useBlueprint = this.blueprint.get() != Blueprints.Blueprint.NONE;
        if (!useBlueprint && (fileName == null || fileName.isBlank())) {
            return;
        }
        HookStore.BuildState state = new HookStore.BuildState();
        state.schematicFile = useBlueprint ? null : fileName;
        state.blueprint = useBlueprint ? this.blueprint.get().name() : null;
        state.blueprintMaterial = useBlueprint ? class_7923.field_41175.method_10221(this.blueprintMaterial.get()).toString() : null;
        state.anchor = this.anchor.get().name();
        state.originX = this.plan.origin().method_10263();
        state.originY = this.plan.origin().method_10264();
        state.originZ = this.plan.origin().method_10260();
        state.mirror = this.mirror.get().name();
        state.rotation = this.rotation.get().name();
        state.dimension = this.mc.field_1687 == null ? null : this.mc.field_1687.method_27983().method_29177().toString();
        state.savedAt = System.currentTimeMillis();
        HookStore.saveBuildState(state);
    }

    private Path resolveSchematicFile(String fileName) throws SchematicException {
        Path directory = HookStore.schematicsDir();
        Path resolved = directory.resolve(fileName.trim()).normalize();
        if (!resolved.startsWith(directory.normalize())) {
            throw new SchematicException("File name must point inside " + String.valueOf(directory));
        }
        if (SchematicFormats.forFile(resolved.getFileName().toString()) == null) {
            throw new SchematicException("'" + fileName + "' has an unsupported extension (" + String.join((CharSequence)", ", AutoSchematicBuilder.schematicExtensions()) + ")");
        }
        return resolved;
    }

    private static List<String> schematicExtensions() {
        return SchematicFormats.extensions();
    }

    private static PlacementPlanner.Anchor parseAnchor(String value) {
        if (value == null) {
            return PlacementPlanner.Anchor.POSITION;
        }
        try {
            return PlacementPlanner.Anchor.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            return PlacementPlanner.Anchor.POSITION;
        }
    }

    private static TransformMath.Mirror parseMirror(String value) {
        if (value == null) {
            return TransformMath.Mirror.NONE;
        }
        try {
            return TransformMath.Mirror.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            return TransformMath.Mirror.NONE;
        }
    }

    private static TransformMath.Rotation parseRotation(String value) {
        if (value == null) {
            return TransformMath.Rotation.NONE;
        }
        try {
            return TransformMath.Rotation.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            return TransformMath.Rotation.NONE;
        }
    }

    private static class_2248 parseMaterial(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            class_2960 identifier = class_2960.method_12829((String)id);
            if (identifier == null || !class_7923.field_41175.method_10250(identifier)) {
                return null;
            }
            return (class_2248)class_7923.field_41175.method_63535(identifier);
        }
        catch (RuntimeException e) {
            return null;
        }
    }
}

