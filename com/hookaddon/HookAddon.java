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
        // The automation modules (Auto Schematic Builder, Auto Spawner, Auto Area Miner), their
        // command, HUD element and bridge were removed from the client.
        Hook.INSTANCE.getSettingsManager().loadProfile();
        LOG.info("Hook Addon ready");
    }

    @Override
    public String getPackage() {
        return "com.hookaddon";
    }
}

