/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2596
 *  net.minecraft.class_2797
 *  net.minecraft.class_310
 *  net.minecraft.class_419
 *  net.minecraft.class_7439
 */
package com.hookaddon.compat;

import hook.Hook;
import hook.compat.meteorclient.commands.Commands;
import hook.compat.meteorclient.events.game.GameLeftEvent;
import hook.compat.meteorclient.events.game.ReceiveMessageEvent;
import hook.compat.meteorclient.events.render.Render3DEvent;
import hook.compat.meteorclient.events.world.TickEvent;
import hook.compat.meteorclient.settings.KeybindSetting;
import hook.compat.meteorclient.systems.hud.Hud;
import hook.compat.orbit.EventBus;
import hook.event.EventListener;
import hook.event.events.KeyEvent;
import hook.event.events.PacketReceiveEvent;
import hook.event.events.PacketSendEvent;
import hook.event.events.Render2DEvent;
import hook.event.events.SetScreenEvent;
import hook.event.events.TickEvent;
import net.minecraft.class_2596;
import net.minecraft.class_2797;
import net.minecraft.class_310;
import net.minecraft.class_419;
import net.minecraft.class_7439;

public final class HookBridge {
    private static final class_310 mc = class_310.method_1551();
    private static HookBridge instance;
    private int lastTickAge = Integer.MIN_VALUE;

    private HookBridge() {
    }

    public static void register() {
        if (instance != null || Hook.INSTANCE == null) {
            return;
        }
        instance = new HookBridge();
        Hook.INSTANCE.getEventBus().register(instance);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (HookBridge.mc.field_1724 == null) {
            return;
        }
        if (HookBridge.mc.field_1724.field_6012 == this.lastTickAge) {
            return;
        }
        this.lastTickAge = HookBridge.mc.field_1724.field_6012;
        EventBus.get().post(new TickEvent.Pre());
    }

    @EventListener
    public void onRender3d(hook.event.events.Render3DEvent event) {
        EventBus.get().post(new Render3DEvent());
    }

    @EventListener
    public void onRender2d(Render2DEvent event) {
        Hud.get().render(event.context);
    }

    @EventListener
    public void onPacketReceive(PacketReceiveEvent event) {
        class_2596<?> class_25962 = event.packet;
        if (class_25962 instanceof class_7439) {
            class_7439 message = (class_7439)class_25962;
            EventBus.get().post(new ReceiveMessageEvent(message.comp_763()));
        }
    }

    @EventListener
    public void onPacketSend(PacketSendEvent event) {
        class_2797 chat;
        class_2596<?> class_25962 = event.packet;
        if (class_25962 instanceof class_2797 && Commands.dispatch((chat = (class_2797)class_25962).comp_945())) {
            event.cancel();
        }
    }

    @EventListener
    public void onSetScreen(SetScreenEvent event) {
        if (event.screen instanceof class_419) {
            EventBus.get().post(new GameLeftEvent());
        }
    }

    @EventListener
    public void onKey(KeyEvent event) {
        if (event.mode != 1 || HookBridge.mc.field_1755 != null) {
            return;
        }
        KeybindSetting.handleKeyPress(event.key);
    }
}

