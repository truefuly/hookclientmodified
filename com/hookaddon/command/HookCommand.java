/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.class_2172
 */
package com.hookaddon.command;

import com.hookaddon.modules.AutoAreaMiner;
import com.hookaddon.modules.AutoSchematicBuilder;
import com.hookaddon.modules.AutoSpawner;
import com.hookaddon.persist.HookStore;
import com.hookaddon.schematic.Blueprints;
import com.hookaddon.schematic.SchematicFormats;
import com.hookaddon.task.TaskManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import hook.compat.meteorclient.commands.Command;
import hook.compat.meteorclient.settings.Setting;
import hook.compat.meteorclient.systems.modules.Module;
import hook.compat.meteorclient.systems.modules.Modules;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.class_2172;

public class HookCommand
extends Command {
    public HookCommand() {
        super("hook", "Hook Addon: status, build and area control, diagnostics.", "ha");
    }

    @Override
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.executes(context -> {
            this.printStatus();
            return 1;
        });
        builder.then(this.literal("schematics").executes(context -> {
            this.listSchematics();
            return 1;
        }));
        builder.then(this.literal("path").executes(context -> {
            this.info("Config folder: %s", HookStore.configDir());
            this.info("Schematic folder: %s", HookStore.schematicsDir());
            return 1;
        }));
        builder.then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)this.literal("build").then(this.literal("load").then(this.argument("file", StringArgumentType.string()).executes(context -> {
            this.loadSchematic(StringArgumentType.getString((CommandContext)context, (String)"file"));
            return 1;
        })))).then(this.literal("blueprint").then(this.argument("name", StringArgumentType.string()).executes(context -> {
            this.setBlueprint(StringArgumentType.getString((CommandContext)context, (String)"name"));
            return 1;
        })))).then(this.literal("pause").executes(context -> {
            this.withSchematic(module -> {
                module.togglePause();
                return null;
            });
            return 1;
        }))).then(this.literal("resume").executes(context -> {
            this.withSchematic(module -> {
                module.togglePause();
                return null;
            });
            return 1;
        }))).then(this.literal("restart").executes(context -> {
            this.withSchematic(module -> {
                module.restartBuild();
                return null;
            });
            return 1;
        }))).then(this.literal("stop").executes(context -> {
            this.withSchematic(module -> {
                module.stopBuild();
                return null;
            });
            return 1;
        })));
        builder.then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)this.literal("area").then(this.literal("prepare").executes(context -> {
            this.withArea(module -> module.prepareArea(), "Area prepared.");
            return 1;
        }))).then(this.literal("start").executes(context -> {
            this.withArea(module -> module.startRun(), "Mining started.");
            return 1;
        }))).then(this.literal("pause").executes(context -> {
            this.withArea(module -> {
                module.togglePause();
                return true;
            }, "Toggled pause.");
            return 1;
        }))).then(this.literal("reset").executes(context -> {
            this.withArea(module -> {
                module.resetRun();
                return true;
            }, "Area plan rebuilt.");
            return 1;
        })));
        builder.then(((LiteralArgumentBuilder)this.literal("spawner").then(this.literal("clear-visited").executes(context -> {
            AutoSpawner module = Modules.get().get(AutoSpawner.class);
            module.searcher().clearVisited();
            this.info("Cleared the list of already scanned positions.", new Object[0]);
            return 1;
        }))).then(this.literal("rtp").executes(context -> {
            AutoSpawner module = Modules.get().get(AutoSpawner.class);
            this.info("RTP state: %s, consecutive failures: %d, attempts: %d", new Object[]{module.rtpFallback().state(), module.rtpFallback().consecutiveFailures(), module.rtpFallback().totalAttempts()});
            return 1;
        })));
        builder.then(this.literal("log").executes(context -> {
            this.printLog();
            return 1;
        }));
    }

    private void printStatus() {
        AutoSchematicBuilder schematic = Modules.get().get(AutoSchematicBuilder.class);
        AutoSpawner spawner = Modules.get().get(AutoSpawner.class);
        AutoAreaMiner area = Modules.get().get(AutoAreaMiner.class);
        this.info("Hook Addon - %d of 3 modules active", HookCommand.activeCount(schematic, spawner, area));
        this.info("Auto Schematic Builder: %s", schematic.isActive() ? schematic.describeFull() : "inactive");
        this.info("Auto Spawner: %s", spawner.isActive() ? spawner.describeFull() : "inactive");
        this.info("Auto Area Miner: %s", area.isActive() ? area.describeFull() : "inactive");
    }

    private static int activeCount(Module ... modules) {
        int active = 0;
        for (Module module : modules) {
            if (!module.isActive()) continue;
            ++active;
        }
        return active;
    }

    private void listSchematics() {
        Path directory = HookStore.schematicsDir();
        List<Path> files = SchematicFormats.listFiles(directory);
        if (files.isEmpty()) {
            this.warning("No schematics in %s (supported: %s).", directory, String.join((CharSequence)", ", SchematicFormats.extensions()));
            return;
        }
        this.info("%d schematic file(s) in %s:", files.size(), directory);
        int shown = 0;
        for (Path file : files) {
            this.info("  %s", file.getFileName());
            if (++shown < 20) continue;
            this.info("  ... and %d more", files.size() - shown);
            break;
        }
    }

    private void setBlueprint(String name) {
        AutoSchematicBuilder module = Modules.get().get(AutoSchematicBuilder.class);
        Blueprints.Blueprint blueprint = Blueprints.find(name);
        if (blueprint == Blueprints.Blueprint.NONE) {
            this.warning("Unknown blueprint '%s'. Available: %s", name, HookCommand.availableBlueprints());
            return;
        }
        Setting<Blueprints.Blueprint> setting = module.settings.get("blueprint", Blueprints.Blueprint.class);
        if (setting == null) {
            this.warning("The blueprint setting is unavailable.", new Object[0]);
            return;
        }
        setting.set(blueprint);
        this.info("Blueprint set to %s.", blueprint.title);
        if (!module.isActive()) {
            module.enable();
        } else {
            module.startBuild(null);
        }
    }

    private static String availableBlueprints() {
        ArrayList<String> names = new ArrayList<String>();
        for (Blueprints.Blueprint value : Blueprints.Blueprint.values()) {
            if (value == Blueprints.Blueprint.NONE) continue;
            names.add(value.title);
        }
        return String.join((CharSequence)", ", names);
    }

    private void loadSchematic(String fileName) {
        Setting<String> setting;
        AutoSchematicBuilder module = Modules.get().get(AutoSchematicBuilder.class);
        Setting<Blueprints.Blueprint> blueprint = module.settings.get("blueprint", Blueprints.Blueprint.class);
        if (blueprint != null) {
            blueprint.set(Blueprints.Blueprint.NONE);
        }
        if ((setting = module.settings.get("schematic", String.class)) != null) {
            setting.set(fileName);
        }
        if (!module.isActive()) {
            module.enable();
            return;
        }
        module.startBuild(fileName);
    }

    private void withSchematic(Function<AutoSchematicBuilder, Void> action) {
        AutoSchematicBuilder module = Modules.get().get(AutoSchematicBuilder.class);
        if (!module.isActive()) {
            this.warning("Enable Auto Schematic Builder first.", new Object[0]);
            return;
        }
        action.apply(module);
    }

    private void withArea(Predicate<AutoAreaMiner> action, String okMessage) {
        AutoAreaMiner module = Modules.get().get(AutoAreaMiner.class);
        if (!module.isActive()) {
            this.warning("Enable Auto Area Miner first.", new Object[0]);
            return;
        }
        if (action.test(module)) {
            this.info(okMessage, new Object[0]);
        }
    }

    private void printLog() {
        List<String> lines = TaskManager.get().debugLog();
        if (lines.isEmpty()) {
            this.info("The task log is empty.", new Object[0]);
            return;
        }
        int from = Math.max(0, lines.size() - 10);
        this.info("Task log (last %d entries):", lines.size() - from);
        for (int i = from; i < lines.size(); ++i) {
            this.info("  %s", lines.get(i));
        }
    }
}

