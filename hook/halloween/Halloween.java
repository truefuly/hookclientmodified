/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_3419
 *  net.minecraft.class_437
 *  net.minecraft.class_638
 */
package hook.halloween;

import hook.Hook;
import hook.feature.HalloweenModule;
import hook.feature.HookClickGuiScreen;
import hook.feature.UiAudio;
import hook.feature.UiPalette;
import hook.halloween.CinematicCamera;
import hook.halloween.CinematicOverlay;
import hook.halloween.HalloweenAtmosphere;
import hook.halloween.HalloweenMist;
import hook.halloween.HalloweenMusic;
import hook.halloween.HalloweenParticles;
import hook.halloween.HalloweenSky;
import hook.halloween.WelcomeScreen;
import hook.halloween.WitchScene;
import hook.itemfx.ItemFx;
import net.minecraft.class_310;
import net.minecraft.class_3419;
import net.minecraft.class_437;
import net.minecraft.class_638;

public final class Halloween {
    public static final float LOCK_SECONDS = 20.0f;
    private static Phase phase = Phase.IDLE;
    private static float phaseT;
    private static float lockLeft;
    private static long lastNanos;
    private static class_638 lastWorld;
    private static boolean moduleOn;
    private static WelcomeScreen welcome;
    public static float worstFrame;

    private Halloween() {
    }

    public static Phase phase() {
        return phase;
    }

    public static void onModuleEnabled() {
        moduleOn = true;
        HalloweenAtmosphere.setWanted(true);
        class_310 var0 = class_310.method_1551();
        if (var0.field_1687 != null && var0.field_1724 != null) {
            Halloween.startEvent();
        }
    }

    public static void onModuleDisabled() {
        moduleOn = false;
        HalloweenAtmosphere.setWanted(false);
        HalloweenMusic.stopEvent();
        if (phase != Phase.IDLE) {
            Halloween.abort();
        }
    }

    public static void replayIntro() {
        class_310 var0 = class_310.method_1551();
        if (moduleOn && phase == Phase.IDLE && var0.field_1687 != null && var0.field_1724 != null) {
            Halloween.startEvent();
        }
    }

    public static boolean guiLocked() {
        return phase != Phase.IDLE || lockLeft > 0.0f;
    }

    public static void lockedNotice() {
        int var0 = Math.max(1, (int)Math.ceil(Math.max(lockLeft, phase != Phase.IDLE ? 1.0f : 0.0f)));
        CinematicOverlay.notice((String)(phase == Phase.IDLE ? "HookVisuals unlocks in " + var0 + "s" : "HookVisuals unlocks after the Halloween intro"));
    }

    private static void startEvent() {
        HookClickGuiScreen var1;
        class_310 var0 = class_310.method_1551();
        phase = Phase.CLOSING;
        phaseT = 0.0f;
        lockLeft = 20.0f;
        WitchScene.preload();
        HalloweenMusic.playEvent(Halloween.eventVolume());
        HookClickGuiScreen hookClickGuiScreen = var1 = Hook.INSTANCE != null ? Hook.INSTANCE.GUI : null;
        if (var1 != null && var0.field_1755 == var1) {
            var1.requestClose();
        }
        CinematicOverlay.cinematic(true);
    }

    private static float eventVolume() {
        try {
            return (float)(HalloweenModule.eventVolume.getValue() / 100.0);
        }
        catch (Throwable var1) {
            return 0.6f;
        }
    }

    static void titleFinished(WelcomeScreen var0) {
        class_310 var1 = class_310.method_1551();
        if (phase == Phase.TITLE && var0 == welcome) {
            if (var1.field_1755 == var0) {
                var1.method_1507(null);
            }
            welcome = null;
            Halloween.beginWitch();
        }
    }

    private static void beginWitch() {
        worstFrame = 0.0f;
        phase = Phase.WITCH;
        phaseT = 0.0f;
        CinematicCamera.begin();
        if (!WitchScene.start()) {
            Halloween.beginOutro();
        }
    }

    private static void beginOutro() {
        phase = Phase.OUTRO;
        phaseT = 0.0f;
        CinematicCamera.release();
        CinematicOverlay.cinematic(false);
    }

    private static void abort() {
        class_310 var0 = class_310.method_1551();
        if (welcome != null) {
            if (var0.field_1755 == welcome) {
                var0.method_1507(null);
            }
            welcome = null;
        }
        if (WitchScene.running()) {
            WitchScene.hurry();
            phase = Phase.WITCH;
        } else {
            Halloween.beginOutro();
        }
        lockLeft = Math.min(lockLeft, 1.5f);
    }

    public static void hardReset() {
        class_310 var0 = class_310.method_1551();
        if (welcome != null && var0.field_1755 == welcome) {
            var0.method_1507(null);
        }
        welcome = null;
        phase = Phase.IDLE;
        phaseT = 0.0f;
        lockLeft = 0.0f;
        CinematicCamera.reset();
        CinematicOverlay.reset();
        WitchScene.reset();
        HalloweenParticles.clear();
        HalloweenMusic.stopEvent();
    }

    public static void frame() {
        float var4;
        class_310 var0 = class_310.method_1551();
        long var1 = System.nanoTime();
        float var3 = lastNanos == 0L ? 0.016f : Math.min(0.1f, (float)(var1 - lastNanos) / 1.0E9f);
        lastNanos = var1;
        float f = var4 = var0.method_1493() ? 0.0f : var3;
        if (var0.field_1687 != lastWorld) {
            if (lastWorld != null) {
                Halloween.hardReset();
                HalloweenSky.release();
                HalloweenMist.release();
                HalloweenParticles.release();
            }
            lastWorld = var0.field_1687;
        }
        HalloweenAtmosphere.frame(var4, var0);
        Halloween.syncMenuMusic(var0);
        if (lockLeft > 0.0f) {
            lockLeft = Math.max(0.0f, lockLeft - var4);
        }
        phaseT += var4;
        if (phase == Phase.WITCH) {
            worstFrame = Math.max(worstFrame, var3);
        }
        switch (phase.ordinal()) {
            case 1: {
                boolean var5;
                boolean bl = var5 = Hook.INSTANCE == null || var0.field_1755 != Hook.INSTANCE.GUI;
                if (var0.field_1687 == null) {
                    Halloween.hardReset();
                    break;
                }
                if (!(var5 && phaseT > 0.35f) && !(phaseT > 1.2f)) break;
                welcome = new WelcomeScreen();
                var0.method_1507((class_437)welcome);
                phase = Phase.TITLE;
                phaseT = 0.0f;
                break;
            }
            case 2: {
                if (welcome == null || var0.field_1755 == welcome || !(phaseT > 0.5f)) break;
                welcome = null;
                Halloween.beginWitch();
                break;
            }
            case 3: {
                WitchScene.update(var4);
                if (!WitchScene.finished()) break;
                Halloween.beginOutro();
                break;
            }
            case 4: {
                if (CinematicCamera.active() || !(phaseT > 1.2f)) break;
                phase = Phase.IDLE;
                WitchScene.reset();
            }
        }
        HalloweenParticles.update(var4);
        ItemFx.frame();
        CinematicCamera.update(var4);
        CinematicOverlay.frame(var3);
    }

    private static void syncMenuMusic(class_310 var0) {
        boolean var1 = false;
        float var2 = 0.35f;
        boolean var3 = true;
        try {
            HookClickGuiScreen var4 = Hook.INSTANCE != null ? Hook.INSTANCE.GUI : null;
            var1 = var4 != null && var4.showing() && UiPalette.selected() == UiPalette.Halloween && hook.module.modules.client.Hook.halloweenMusic.getValue();
            var2 = (float)(hook.module.modules.client.Hook.halloweenVolume.getValue() / 100.0);
            var3 = hook.module.modules.client.Hook.beatShake.getValue();
        }
        catch (Throwable var4) {
            // empty catch block
        }
        float var8 = 1.0f;
        try {
            var8 = var0.field_1690.method_71978(class_3419.field_15250);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        HalloweenMusic.setMenuWanted(var1, var2);
        HalloweenMusic.setBeatEnabled(var3);
        HalloweenMusic.setEventVolume(Halloween.eventVolume());
        UiAudio.setMaster(var8);
    }

    public static enum Phase {
        IDLE,
        CLOSING,
        TITLE,
        WITCH,
        OUTRO;

    }
}

