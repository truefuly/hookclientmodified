/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10799
 *  net.minecraft.class_11909
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_364
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 *  net.minecraft.class_7919
 *  org.lwjgl.glfw.GLFW
 */
package thetruetrident.swordcore;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_7919;
import org.lwjgl.glfw.GLFW;
import thetruetrident.swordcore.SwordcoreClient;
import thetruetrident.swordcore.SwordcoreConfig;
import thetruetrident.swordcore.SwordcorePresetApplier;
import thetruetrident.swordcore.mixin.client.DrawContextAccessor;

@Environment(value=EnvType.CLIENT)
public class SwordcoreConfigScreen
extends class_437 {
    private static final class_2960 MOVEMENT_RESIST_IMAGE = class_2960.method_60655((String)"swordcore", (String)"textures/gui/config/movement_resist.png");
    private static final class_2960 MOVEMENT_SYNC_IMAGE = class_2960.method_60655((String)"swordcore", (String)"textures/gui/config/movement_sync.png");
    private static final class_2960 MOVEMENT_RESIST_PARTICLE = class_2960.method_60655((String)"swordcore", (String)"textures/particle/movement_resist.png");
    private static final class_2960 MOVEMENT_SYNC_INNER_PARTICLE = class_2960.method_60655((String)"swordcore", (String)"textures/particle/movement_sync_inner.png");
    private static final String TOOLTIP_PREFIX = "\ue81b ";
    private static long handCursor;
    private final class_437 parent;
    private Page page = Page.MAIN;
    private class_4185 presetButton;

    public SwordcoreConfigScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"Swordcore"));
        this.parent = parent;
    }

    protected void method_25426() {
        this.method_37067();
        if (this.page == Page.MAIN) {
            this.initMain();
        } else if (this.page == Page.COMBAT) {
            this.initCombat();
        } else if (this.page == Page.MOVEMENT_SYNC) {
            this.initMovementSync();
        } else if (this.page == Page.MOVEMENT_RESIST) {
            this.initMovementResist();
        } else if (this.page == Page.S_TAPPING) {
            this.initSTapping();
        } else {
            this.initOther();
        }
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        context.method_25294(0, 0, this.field_22789, this.field_22790, -804253680);
        context.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 16, 0xFFFFFF);
        if (this.page == Page.MAIN) {
            this.renderMainImages(context, mouseX, mouseY);
            this.updateCardCursor(mouseX, mouseY);
        } else if (this.page == Page.MOVEMENT_SYNC) {
            this.restoreCursor();
            this.drawParticleIcon(context, MOVEMENT_SYNC_INNER_PARTICLE, this.field_22789 / 2 + 116, this.row(8));
        } else if (this.page == Page.MOVEMENT_RESIST) {
            this.restoreCursor();
            this.drawParticleIcon(context, MOVEMENT_RESIST_PARTICLE, this.field_22789 / 2 + 116, this.row(4));
        } else {
            this.restoreCursor();
        }
        super.method_25394(context, mouseX, mouseY, delta);
    }

    public void method_25419() {
        this.restoreCursor();
        SwordcoreClient.swordConfig.save();
        if (this.field_22787 != null) {
            this.field_22787.method_1507(this.parent);
        }
    }

    public void method_25432() {
        this.restoreCursor();
        super.method_25432();
    }

    public boolean method_25402(class_11909 click, boolean doubleClick) {
        if (this.page == Page.MAIN && click.method_74245() == 0) {
            MainCards cards = this.mainCards();
            if (this.inside(click.comp_4798(), click.comp_4799(), cards.syncX, cards.y, cards.cardWidth, cards.cardHeight)) {
                this.open(Page.MOVEMENT_SYNC);
                return true;
            }
            if (this.inside(click.comp_4798(), click.comp_4799(), cards.resistX, cards.y, cards.cardWidth, cards.cardHeight)) {
                this.open(Page.MOVEMENT_RESIST);
                return true;
            }
        }
        return super.method_25402(click, doubleClick);
    }

    private void initMain() {
        int center = this.field_22789 / 2;
        int buttonWidth = Math.min(150, Math.max(100, (this.field_22789 - 64) / 4));
        MainCards cards = this.mainCards();
        int topY = 58;
        int bottomY = Math.min(this.field_22790 - 54, cards.y + cards.cardHeight + 18);
        this.presetButton = this.button("\ue81b Preset: " + this.label(SwordcoreClient.swordConfig.preset), center - 90, 32, 180, () -> {
            SwordcoreConfig.Preset next = this.nextPreset();
            SwordcorePresetApplier.applyPreset(next);
            this.rebuild();
        }, "Cycles Swordcore presets, different presets for different situations/playstyles.");
        this.method_37063((class_364)this.presetButton);
        this.method_37063((class_364)this.toggle("Swordcore", SwordcoreClient.swordConfig.enabled, center - buttonWidth * 2 - 9, topY, buttonWidth, "Master enable for Swordcore.", value -> {
            SwordcoreClient.swordConfig.enabled = value;
        }));
        this.method_37063((class_364)this.premiumToggle("Visual Effects \ue81c", SwordcoreClient.swordConfig.visualEffects, center - buttonWidth - 3, topY, buttonWidth, "Shows client-side Swordcore particles and the blue target crosshair. Can only be disabled in Swordcore premium.", value -> {
            SwordcoreClient.swordConfig.visualEffects = value;
        }));
        this.method_37063((class_364)this.premiumToggle("Sound Effects \ue81c", SwordcoreClient.swordConfig.soundEffects, center + 3, topY, buttonWidth, "Plays Swordcore feedback sounds. Can only be disabled in Swordcore premium.", value -> {
            SwordcoreClient.swordConfig.soundEffects = value;
        }));
        this.method_37063((class_364)this.button("Combat...", center + buttonWidth + 9, topY, buttonWidth, () -> this.open(Page.COMBAT), "Shield disable, SmartSword, and free hitting."));
        this.method_37063((class_364)this.button("Movement Sync...", cards.syncX + 10, cards.y + cards.cardHeight - 30, cards.cardWidth - 20, () -> this.open(Page.MOVEMENT_SYNC), "Open Movement Sync settings."));
        this.method_37063((class_364)this.button("Movement Resist...", cards.resistX + 10, cards.y + cards.cardHeight - 30, cards.cardWidth - 20, () -> this.open(Page.MOVEMENT_RESIST), "Open Movement Resist settings."));
        int bottomGap = 6;
        int bottomX = center - (buttonWidth * 3 + bottomGap * 2) / 2;
        this.method_37063((class_364)this.toggle("\ue81c SmartSword", SwordcoreClient.swordConfig.smartSword, bottomX, bottomY, buttonWidth, "When the bind of smart-sword is held (default R, REBINDABLE), any player target in range will be automatically hit, critical hit, or sprint hit at the most optimal time to maximize gameplay value.", value -> {
            SwordcoreClient.swordConfig.smartSword = value;
        }));
        this.method_37063((class_364)this.button("S Tapping: " + this.onOff(SwordcoreClient.swordConfig.sTapping), bottomX + buttonWidth + bottomGap, bottomY, buttonWidth, () -> this.open(Page.S_TAPPING), "Open S Tapping settings."));
        this.method_37063((class_364)this.button("Utility / Other...", bottomX + (buttonWidth + bottomGap) * 2, bottomY, buttonWidth, () -> this.open(Page.OTHER), "Autosprint, jump reset, targeting, and logs."));
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Done"), button -> this.method_25419()).method_46434(center - buttonWidth / 2, this.field_22790 - 30, buttonWidth, 20).method_46431());
    }

    private void initCombat() {
        int center = this.field_22789 / 2;
        int x = center - 110;
        this.method_37063((class_364)this.toggle("Shield Disable", SwordcoreClient.swordConfig.shieldDisable, x, this.row(0), "Swaps from sword to axe and hits when the target blocks with a shield.", value -> {
            SwordcoreClient.swordConfig.shieldDisable = value;
        }));
        this.method_37063((class_364)this.button("Mode: " + this.label(SwordcoreClient.swordConfig.shieldDisableMode), x, this.row(1), 220, () -> {
            SwordcoreClient.swordConfig.shieldDisableMode = this.nextShieldMode();
            this.rebuild();
        }, "Normal swaps and hits. Stun adds a next-tick hit. Stun Sneak also sneaks for the swap and follow-up ticks."));
        this.method_37063((class_364)this.toggle("Free Hitting", SwordcoreClient.swordConfig.freeHitting, x, this.row(2), "Stops you from missing attacks (stops you from hitting the air).", value -> {
            SwordcoreClient.swordConfig.freeHitting = value;
        }));
        this.addNumberBox("Added Ping", Integer.toString(SwordcoreClient.swordConfig.addedPing), x, this.row(4), "Adds this many milliseconds of outgoing packet delay while in-game. 0 disables it.", value -> {
            SwordcoreClient.swordConfig.addedPing = Math.max(0, this.parseInt((String)value, SwordcoreClient.swordConfig.addedPing));
        });
        this.addBackButton();
    }

    private void initMovementSync() {
        int center = this.field_22789 / 2;
        int x = center - 110;
        this.method_37063((class_364)this.toggle("Movement Syncing", SwordcoreClient.swordConfig.movementSyncing, x, this.row(0), "Taps forward/back to mirror spacing against the target's movement.", value -> {
            SwordcoreClient.swordConfig.movementSyncing = value;
        }));
        this.method_37063((class_364)this.button("Jump Following: " + this.label(SwordcoreClient.swordConfig.jumpFollowingMode), x, this.row(1), 220, () -> {
            SwordcoreClient.swordConfig.jumpFollowingMode = this.nextJumpFollowingMode();
            this.rebuild();
        }, "Off disables Jump Following. On always allows it. Neutral Based only allows it when NEUTRAL MODE is Aggressive or Adaptable is currently aggressive."));
        this.method_37063((class_364)this.toggle("Jump Respects Resist", SwordcoreClient.swordConfig.jumpFollowingRespectsResist, x, this.row(2), "When on, Jump Following will not trigger inside the Movement Resist bubble.", value -> {
            SwordcoreClient.swordConfig.jumpFollowingRespectsResist = value;
        }));
        this.method_37063((class_364)this.button("NEUTRAL MODE: " + this.label(SwordcoreClient.swordConfig.neutralMode), x, this.row(3), 220, () -> {
            SwordcoreClient.swordConfig.neutralMode = this.nextNeutralMode();
            this.rebuild();
        }, "Passive only presses W toward the target during cooldown. Aggressive may press W whenever the target moves away."));
        this.method_37063((class_364)this.button("Movement Equalizer: " + this.label(SwordcoreClient.swordConfig.movementEqualizer), x, this.row(4), 220, () -> {
            SwordcoreClient.swordConfig.movementEqualizer = this.nextMovementEqualizerMode();
            this.rebuild();
        }, "When a target sprints toward you, Equalizer stops Movement Sync from holding S. Jump modes tap jump too."));
        this.method_37063((class_364)this.toggle("Air Gate", SwordcoreClient.swordConfig.airGate, x, this.row(5), "When on, Movement Sync will not hold W while you are already in the air.", value -> {
            SwordcoreClient.swordConfig.airGate = value;
        }));
        this.method_37063((class_364)this.toggle("No S During Aggro Jump", SwordcoreClient.swordConfig.preventBackDuringAggressiveJump, x, this.row(6), "When on, Movement Sync will not hold S while airborne from an aggressive Movement Sync jump.", value -> {
            SwordcoreClient.swordConfig.preventBackDuringAggressiveJump = value;
        }));
        this.addNumberBox("Equalizer Range", Float.toString(SwordcoreClient.swordConfig.movementEqualizerRange), x, this.row(7), "How far outside Movement Resist range the Equalizer can activate. Default is 1.0.", value -> {
            SwordcoreClient.swordConfig.movementEqualizerRange = Math.max(0.0f, this.parseFloat((String)value, SwordcoreClient.swordConfig.movementEqualizerRange));
        });
        this.addNumberBox("Sync Inner Range", Float.toString(SwordcoreClient.swordConfig.movementSyncInnerRange), x, this.row(8), "When the target is on cooldown, Movement Sync moves forward until this inner bubble. Default is 2.0.", value -> {
            SwordcoreClient.swordConfig.movementSyncInnerRange = Math.max(0.0f, this.parseFloat((String)value, SwordcoreClient.swordConfig.movementSyncInnerRange));
        });
        this.addBackButton();
    }

    private void initSTapping() {
        int center = this.field_22789 / 2;
        int x = center - 110;
        this.method_37063((class_364)this.toggle("S Tapping", SwordcoreClient.swordConfig.sTapping, x, this.row(0), "After a successful Swordcore hit, taps S and overrides sync/resist.", value -> {
            SwordcoreClient.swordConfig.sTapping = value;
        }));
        this.addNumberBox("S Tap Length", Integer.toString(SwordcoreClient.swordConfig.sTapLength), x, this.row(1), "Ticks to hold S after a successful Swordcore hit. Default is 2.", value -> {
            SwordcoreClient.swordConfig.sTapLength = Math.max(0, this.parseInt((String)value, SwordcoreClient.swordConfig.sTapLength));
        });
        this.addBackButton();
    }

    private void initMovementResist() {
        int center = this.field_22789 / 2;
        int x = center - 110;
        this.method_37063((class_364)this.toggle("Movement Resisting", SwordcoreClient.swordConfig.movementResisting, x, this.row(0), "Prevents walking into a target that is not on attack cooldown.", value -> {
            SwordcoreClient.swordConfig.movementResisting = value;
        }));
        this.method_37063((class_364)this.toggle("Bubble Escape", SwordcoreClient.swordConfig.movementResistEscape, x, this.row(1), "When inside Movement Resist range, presses the fastest W/S/A/D direction out of the bubble.", value -> {
            SwordcoreClient.swordConfig.movementResistEscape = value;
        }));
        this.method_37063((class_364)this.toggle("Sprint Hit Follow Through", SwordcoreClient.swordConfig.sprintHitFollowThrough, x, this.row(2), "After a sprint sword hit, forces a short W sprint jump toward the target and overrides Movement Resist.", value -> {
            SwordcoreClient.swordConfig.sprintHitFollowThrough = value;
        }));
        this.addNumberBox("Sprint Jump Delay", Integer.toString(SwordcoreClient.swordConfig.sprintHitJumpDelay), x, this.row(3), "Ticks to wait before the sprint-hit follow-through taps jump. Default is 2.", value -> {
            SwordcoreClient.swordConfig.sprintHitJumpDelay = Math.max(0, this.parseInt((String)value, SwordcoreClient.swordConfig.sprintHitJumpDelay));
        });
        this.addNumberBox("Resist Range", Float.toString(SwordcoreClient.swordConfig.movementResistingRange), x, this.row(4), "Eye-centered target reach bubble to stay outside of. Default is 3.6.", value -> {
            SwordcoreClient.swordConfig.movementResistingRange = Math.max(0.0f, this.parseFloat((String)value, SwordcoreClient.swordConfig.movementResistingRange));
        });
        this.addBackButton();
    }

    private void initOther() {
        int center = this.field_22789 / 2;
        int x = center - 110;
        this.method_37063((class_364)this.toggle("Chat Logs", SwordcoreClient.swordConfig.chatLogs, x, this.row(0), "Shows Swordcore action logs in chat.", value -> {
            SwordcoreClient.swordConfig.chatLogs = value;
        }));
        this.method_37063((class_364)this.toggle("Auto WTap / Autosprint", SwordcoreClient.swordConfig.autoWTapAutosprint, x, this.row(1), "When you are already walking forward, activates sprinting. It does not press W for you.", value -> {
            SwordcoreClient.swordConfig.autoWTapAutosprint = value;
        }));
        this.method_37063((class_364)this.toggle("Item Gate", SwordcoreClient.swordConfig.itemGate, x, this.row(2), "When on, Movement Sync and Movement Resist only run while holding a sword, axe, or spear.", value -> {
            SwordcoreClient.swordConfig.itemGate = value;
        }));
        this.method_37063((class_364)this.toggle("Jump Resetting", SwordcoreClient.swordConfig.jumpResetting, x, this.row(3), "Taps jump when your health drops from detectable player-opponent damage.", value -> {
            SwordcoreClient.swordConfig.jumpResetting = value;
        }));
        this.method_37063((class_364)this.toggle("Combo Counter", SwordcoreClient.swordConfig.comboCounter, x, this.row(4), "Counts consecutive hits on your Swordcore target until you take damage.", value -> {
            SwordcoreClient.swordConfig.comboCounter = value;
        }));
        this.method_37063((class_364)this.toggle("Visual Tinting", SwordcoreClient.swordConfig.visualTinting, x, this.row(5), "Flashes a fading blue tint when neutral mode becomes passive and red when it becomes aggressive.", value -> {
            SwordcoreClient.swordConfig.visualTinting = value;
        }));
        this.addNumberBox("Target Range", Float.toString(SwordcoreClient.swordConfig.targetRange), x, this.row(6), "How far Swordcore searches for a player target.", value -> {
            SwordcoreClient.swordConfig.targetRange = Math.max(1.0f, this.parseFloat((String)value, SwordcoreClient.swordConfig.targetRange));
        });
        this.addBackButton();
    }

    private void renderMainImages(class_332 context, int mouseX, int mouseY) {
        MainCards cards = this.mainCards();
        boolean syncHovered = this.inside(mouseX, mouseY, cards.syncX, cards.y, cards.cardWidth, cards.cardHeight);
        boolean resistHovered = this.inside(mouseX, mouseY, cards.resistX, cards.y, cards.cardWidth, cards.cardHeight);
        this.drawImagePanel(context, MOVEMENT_SYNC_IMAGE, cards.syncX, cards.y, cards.cardWidth, cards.cardHeight, syncHovered ? -1 : -14979585);
        this.drawImagePanel(context, MOVEMENT_RESIST_IMAGE, cards.resistX, cards.y, cards.cardWidth, cards.cardHeight, resistHovered ? -1 : -49608);
        context.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"Movement Sync"), cards.syncX + cards.cardWidth / 2, cards.y + 8, 0xFFFFFF);
        context.method_27534(this.field_22793, (class_2561)class_2561.method_43470((String)"Movement Resist"), cards.resistX + cards.cardWidth / 2, cards.y + 8, 0xFFFFFF);
    }

    private void updateCardCursor(int mouseX, int mouseY) {
        if (this.field_22787 == null) {
            return;
        }
        MainCards cards = this.mainCards();
        boolean hovered = this.inside(mouseX, mouseY, cards.syncX, cards.y, cards.cardWidth, cards.cardHeight) || this.inside(mouseX, mouseY, cards.resistX, cards.y, cards.cardWidth, cards.cardHeight);
        long handle = this.field_22787.method_22683().method_4490();
        if (hovered) {
            if (handCursor == 0L) {
                handCursor = GLFW.glfwCreateStandardCursor((int)221188);
            }
            GLFW.glfwSetCursor((long)handle, (long)handCursor);
        } else {
            GLFW.glfwSetCursor((long)handle, (long)0L);
        }
    }

    private void restoreCursor() {
        if (this.field_22787 != null) {
            GLFW.glfwSetCursor((long)this.field_22787.method_22683().method_4490(), (long)0L);
        }
    }

    private void drawImagePanel(class_332 context, class_2960 image, int x, int y, int panelWidth, int panelHeight, int borderColor) {
        context.method_25294(x - 2, y - 2, x + panelWidth + 2, y + panelHeight + 2, borderColor);
        ((DrawContextAccessor)context).swordcore$drawTexturedQuad(class_10799.field_56883, image, x, x + panelWidth, y, y + panelHeight, 0.0f, 1.0f, 0.0f, 1.0f, -1);
        context.method_25294(x, y + panelHeight - 42, x + panelWidth, y + panelHeight, -1610612736);
    }

    private void drawParticleIcon(class_332 context, class_2960 image, int x, int y) {
        context.method_25294(x - 2, y - 2, x + 22, y + 22, -15724520);
        context.method_25294(x - 1, y - 1, x + 21, y + 21, -1);
        context.method_25294(x, y, x + 20, y + 20, -16448248);
        ((DrawContextAccessor)context).swordcore$drawTexturedQuad(class_10799.field_56883, image, x + 2, x + 18, y + 2, y + 18, 0.0f, 1.0f, 0.0f, 1.0f, -1);
    }

    private int mainCardWidth() {
        return Math.min(270, Math.max(130, (this.field_22789 - 48) / 2));
    }

    private int mainCardHeight() {
        return Math.min(152, Math.max(74, this.field_22790 - 154));
    }

    private MainCards mainCards() {
        int cardWidth = this.mainCardWidth();
        int cardHeight = this.mainCardHeight();
        int gap = 12;
        int syncX = this.field_22789 / 2 - cardWidth - gap / 2;
        int y = Math.max(94, this.field_22790 / 2 - cardHeight / 2 - 8);
        return new MainCards(syncX, syncX + cardWidth + gap, y, cardWidth, cardHeight);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int areaWidth, int areaHeight) {
        return mouseX >= (double)x && mouseX < (double)(x + areaWidth) && mouseY >= (double)y && mouseY < (double)(y + areaHeight);
    }

    private class_4185 toggle(String label, boolean current, int x, int y, String tooltip, Consumer<Boolean> setter) {
        return this.toggle(label, current, x, y, 220, tooltip, setter);
    }

    private class_4185 toggle(String label, boolean current, int x, int y, int width, String tooltip, Consumer<Boolean> setter) {
        return this.withTooltip(class_4185.method_46430((class_2561)class_2561.method_43470((String)(label + ": " + (current ? "ON" : "OFF"))), button -> {
            boolean next = button.method_25369().getString().endsWith("OFF");
            setter.accept(next);
            this.refreshPreset();
            SwordcoreClient.swordConfig.save();
            button.method_25355((class_2561)class_2561.method_43470((String)(label + ": " + (next ? "ON" : "OFF"))));
        }).method_46434(x, y, width, 20).method_46431(), tooltip);
    }

    private class_4185 premiumToggle(String label, boolean current, int x, int y, int width, String tooltip, Consumer<Boolean> setter) {
        return this.withTooltip(class_4185.method_46430((class_2561)class_2561.method_43470((String)(label + ": " + (current ? "ON" : "OFF"))), button -> {
            boolean next = button.method_25369().getString().endsWith("OFF");
            if (!SwordcoreClient.premiumFeaturesEnabled && !next) {
                next = true;
            }
            setter.accept(next);
            this.refreshPreset();
            SwordcoreClient.swordConfig.save();
            button.method_25355((class_2561)class_2561.method_43470((String)(label + ": " + (next ? "ON" : "OFF"))));
        }).method_46434(x, y, width, 20).method_46431(), tooltip);
    }

    private class_4185 button(String label, int x, int y, int width, Runnable action, String tooltip) {
        return this.withTooltip(class_4185.method_46430((class_2561)class_2561.method_43470((String)label), button -> {
            action.run();
            this.refreshPreset();
            SwordcoreClient.swordConfig.save();
        }).method_46434(x, y, width, 20).method_46431(), tooltip);
    }

    private void addNumberBox(String label, String value, int x, int y, String tooltip, Consumer<String> setter) {
        class_342 field = new class_342(this.field_22793, x, y, 220, 20, (class_2561)class_2561.method_43470((String)label));
        field.method_1852(value);
        field.method_47400(class_7919.method_47407((class_2561)class_2561.method_43470((String)this.tooltipText(label + ": " + tooltip)).method_27692(class_124.field_1080)));
        field.method_1863(text -> {
            setter.accept((String)text);
            this.refreshPreset();
            SwordcoreClient.swordConfig.save();
        });
        this.method_37063((class_364)field);
    }

    private void addBackButton() {
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Back"), button -> this.open(Page.MAIN)).method_46434(this.field_22789 / 2 - 110, this.field_22790 - 30, 220, 20).method_46431());
    }

    private void open(Page next) {
        this.page = next;
        this.rebuild();
    }

    private void rebuild() {
        if (this.field_22787 != null) {
            this.method_25423(this.field_22789, this.field_22790);
        }
    }

    private void refreshPreset() {
        SwordcorePresetApplier.refreshPresetFromCurrent();
        if (this.presetButton != null) {
            this.presetButton.method_25355((class_2561)class_2561.method_43470((String)("Preset: " + this.label(SwordcoreClient.swordConfig.preset))));
        }
    }

    private int row(int index) {
        return 44 + index * 28;
    }

    private float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        }
        catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private SwordcoreConfig.ShieldDisableMode nextShieldMode() {
        SwordcoreConfig.ShieldDisableMode[] values = SwordcoreConfig.ShieldDisableMode.values();
        return values[(SwordcoreClient.swordConfig.shieldDisableMode.ordinal() + 1) % values.length];
    }

    private SwordcoreConfig.NeutralMode nextNeutralMode() {
        SwordcoreConfig.NeutralMode[] values = SwordcoreConfig.NeutralMode.values();
        return values[(SwordcoreClient.swordConfig.neutralMode.ordinal() + 1) % values.length];
    }

    private SwordcoreConfig.JumpFollowingMode nextJumpFollowingMode() {
        SwordcoreConfig.JumpFollowingMode[] values = SwordcoreConfig.JumpFollowingMode.values();
        return values[(SwordcoreClient.swordConfig.jumpFollowingMode.ordinal() + 1) % values.length];
    }

    private SwordcoreConfig.MovementEqualizerMode nextMovementEqualizerMode() {
        SwordcoreConfig.MovementEqualizerMode[] values = SwordcoreConfig.MovementEqualizerMode.values();
        return values[(SwordcoreClient.swordConfig.movementEqualizer.ordinal() + 1) % values.length];
    }

    private SwordcoreConfig.Preset nextPreset() {
        SwordcoreConfig.Preset[] values = SwordcoreConfig.Preset.values();
        int index = SwordcoreClient.swordConfig.preset == SwordcoreConfig.Preset.CUSTOM ? -1 : SwordcoreClient.swordConfig.preset.ordinal();
        SwordcoreConfig.Preset next = values[(index + 1) % values.length];
        return next == SwordcoreConfig.Preset.CUSTOM ? values[0] : next;
    }

    private String label(Enum<?> value) {
        if (value instanceof SwordcoreConfig.Preset) {
            SwordcoreConfig.Preset preset = (SwordcoreConfig.Preset)value;
            return switch (preset) {
                default -> throw new MatchException(null, null);
                case SwordcoreConfig.Preset.ADAPTABLE -> "Adaptable";
                case SwordcoreConfig.Preset.PASSIVE -> "Passive";
                case SwordcoreConfig.Preset.AGGRESSIVE -> "Aggressive";
                case SwordcoreConfig.Preset.PASSIVE_PLUS_PLUS -> "Passive++";
                case SwordcoreConfig.Preset.LIGHT -> "Light";
                case SwordcoreConfig.Preset.CUSTOM -> "Custom";
            };
        }
        if (value == SwordcoreConfig.NeutralMode.ADAPTABLE) {
            return "Adaptable (!!)";
        }
        if (value == SwordcoreConfig.NeutralMode.ADAPTABLE_INVERSE) {
            return "Adaptable (Inverse)";
        }
        if (value == SwordcoreConfig.MovementEqualizerMode.ADAPTABLE_EXPERIMENTAL) {
            return "Adaptable (Experimental)";
        }
        return value.name().toLowerCase().replace('_', ' ');
    }

    private String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    private class_4185 withTooltip(class_4185 button, String tooltip) {
        button.method_47400(class_7919.method_47407((class_2561)class_2561.method_43470((String)this.tooltipText(tooltip)).method_27692(class_124.field_1080)));
        return button;
    }

    private String tooltipText(String tooltip) {
        return tooltip.startsWith(TOOLTIP_PREFIX) ? tooltip : TOOLTIP_PREFIX + tooltip;
    }

    @Environment(value=EnvType.CLIENT)
    private static enum Page {
        MAIN,
        COMBAT,
        MOVEMENT_SYNC,
        MOVEMENT_RESIST,
        S_TAPPING,
        OTHER;

    }

    @Environment(value=EnvType.CLIENT)
    private record MainCards(int syncX, int resistX, int y, int cardWidth, int cardHeight) {
    }
}

