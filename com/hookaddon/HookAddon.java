/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.hookaddon;

import com.hookaddon.command.HookCommand;
import com.hookaddon.compat.HookBridge;
import com.hookaddon.hud.HookStatusHud;
import com.hookaddon.modules.AutoAreaMiner;
import com.hookaddon.modules.AutoSchematicBuilder;
import com.hookaddon.modules.AutoSpawner;
import com.hookaddon.persist.HookStore;
import com.hookaddon.task.TaskManager;
import com.mojang.logging.LogUtils;
import hook.Hook;
import hook.compat.meteorclient.addons.MeteorAddon;
import hook.compat.meteorclient.commands.Commands;
import hook.compat.meteorclient.systems.hud.Hud;
import hook.compat.meteorclient.systems.hud.HudGroup;
import hook.compat.meteorclient.systems.modules.Category;
import hook.compat.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class HookAddon
extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("HookVisuals");
    public static final HudGroup HUD_GROUP = new HudGroup("HookVisuals");

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public void onInitialize() {
        LOG.info("Initializing Hook Addon");
        TaskManager.setLogSink(message -> LOG.debug("[task] {}", message));
        HookStore.ensureDirectories();
        Modules.get().add(new AutoSchematicBuilder());
        Modules.get().add(new AutoSpawner());
        Modules.get().add(new AutoAreaMiner());
        Commands.add(new HookCommand());
        Hud.get().register(HookStatusHud.INFO);
        HookBridge.register();
        Hook.INSTANCE.getSettingsManager().loadProfile();
        LOG.info("Hook Addon ready: 3 modules in the {} category", (Object)HookAddon.CATEGORY.name);
    }

    @Override
    public String getPackage() {
        return "com.hookaddon";
    }
}

