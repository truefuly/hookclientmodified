/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_2561
 *  net.minecraft.class_268
 *  net.minecraft.class_332
 *  net.minecraft.class_640
 *  net.minecraft.class_7532
 *  net.minecraft.class_8685
 *  org.joml.Matrix3x2fStack
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render2DEvent;
import hook.event.events.TickEvent;
import hook.feature.HookClickGuiScreen;
import hook.feature.MinecraftUiRenderer;
import hook.feature.StaffClassifier;
import hook.feature.StaffListPanel;
import hook.feature.UiAudio;
import hook.feature.UiIcon;
import hook.feature.UiRenderer;
import hook.module.Category;
import hook.module.Module;
import hook.module.NewFeature;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.class_1041;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_332;
import net.minecraft.class_640;
import net.minecraft.class_7532;
import net.minecraft.class_8685;
import org.joml.Matrix3x2fStack;

public final class StaffListModule
extends Module
implements NewFeature {
    private final BooleanSetting showEmpty = new BooleanSetting("Show When Empty", true);
    private final BooleanSetting avatars = new BooleanSetting("Head Avatars", true);
    private final BooleanSetting notify = new BooleanSetting("Chat Notifications", false);
    private final BooleanSetting sound = new BooleanSetting("Alert Sound", false);
    private final NumberSetting posX = new NumberSetting("Position X", 0.0, 2560.0, 25.0, 1.0);
    private final NumberSetting posY = new NumberSetting("Position Y", 0.0, 1440.0, 140.0, 1.0);
    private final NumberSetting scale = new NumberSetting("Scale", 0.5, 2.5, 1.0, 0.05);
    private final NumberSetting width = new NumberSetting("Width", 140.0, 450.0, 210.0, 5.0);
    private final NumberSetting opacity = new NumberSetting("Opacity", 0.0, 1.0, 0.62, 0.05);
    private final StaffListPanel panel = new StaffListPanel();
    private Map<String, StaffListPanel.StaffMember> staff = Map.of();
    private Object world;
    private int ticks;
    private boolean initialized;
    private long lastRender;

    public StaffListModule() {
        super("Staff List", "Shows staff from player names and scoreboard ranks.", -1, Category.DONUT);
        this.addSettings(this.showEmpty, this.avatars, this.notify, this.sound, this.posX, this.posY, this.scale, this.width, this.opacity);
    }

    @Override
    public void onEnable() {
        this.clear();
    }

    @Override
    public void onDisable() {
        this.clear();
    }

    private void clear() {
        this.staff = Map.of();
        this.panel.reset();
        this.world = null;
        this.ticks = 0;
        this.initialized = false;
        this.lastRender = 0L;
    }

    private static String text(class_2561 value) {
        return value == null ? "" : value.getString();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null || this.mc.method_1562() == null) {
            if (this.world != null) {
                this.clear();
            }
            return;
        }
        if (this.world != this.mc.field_1687) {
            this.clear();
            this.world = this.mc.field_1687;
        }
        if (this.ticks++ % 5 != 0) {
            return;
        }
        TreeMap<String, StaffListPanel.StaffMember> found = new TreeMap<String, StaffListPanel.StaffMember>();
        for (class_640 player : this.mc.method_1562().method_2880()) {
            String suffix;
            String prefix;
            String teamName;
            String string;
            if (player.method_2966() == null || (string = player.method_2966().name()) == null || string.isEmpty()) continue;
            class_268 team = player.method_2955();
            String display = StaffListModule.text(player.method_2971());
            if (!StaffClassifier.isStaff(string, display, teamName = team == null ? "" : team.method_1197(), prefix = team == null ? "" : StaffListModule.text(team.method_1144()), suffix = team == null ? "" : StaffListModule.text(team.method_1136()))) continue;
            found.put(string.toLowerCase(Locale.ROOT), new StaffListPanel.StaffMember(string, player.method_2966().id()));
        }
        for (class_640 player : this.mc.field_1687.method_18456()) {
            String string = player.method_7334().name();
            if (string == null || !StaffClassifier.knownName(string)) continue;
            found.putIfAbsent(string.toLowerCase(Locale.ROOT), new StaffListPanel.StaffMember(string, player.method_5667()));
        }
        if (this.initialized) {
            boolean joined = false;
            for (Map.Entry entry : found.entrySet()) {
                if (this.staff.containsKey(entry.getKey())) continue;
                joined = true;
                if (!this.notify.getValue()) continue;
                this.message(((StaffListPanel.StaffMember)entry.getValue()).name() + " is now online.");
            }
            if (joined && this.sound.getValue()) {
                UiAudio.play(UiAudio.Cue.CONFIRM);
            }
            if (this.notify.getValue()) {
                for (Map.Entry<Object, Object> entry : this.staff.entrySet()) {
                    if (found.containsKey(entry.getKey())) continue;
                    this.message(((StaffListPanel.StaffMember)entry.getValue()).name() + " left or vanished.");
                }
            }
        }
        this.initialized = true;
        if (!found.equals(this.staff)) {
            this.staff = found;
            this.panel.setMembers(List.copyOf(found.values()));
        }
    }

    private void message(String value) {
        if (this.mc.field_1705 != null) {
            this.mc.field_1705.method_1743().method_1812((class_2561)class_2561.method_43470((String)"HookVisuals \u00b7 Staff List  ").method_10852((class_2561)class_2561.method_43470((String)value)));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventListener
    public void onRender(Render2DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null || this.mc.field_1690.field_1842 || event.context == null) {
            return;
        }
        class_1041 window = this.mc.method_22683();
        long now = System.nanoTime();
        float delta = this.lastRender == 0L ? 0.016f : Math.min(0.1f, (float)(now - this.lastRender) / 1.0E9f);
        this.lastRender = now;
        float panelScale = Math.max(0.45f, (float)window.method_4506() / 1080.0f) * this.scale.getFloatValue();
        float panelWidth = Math.min(this.width.getFloatValue() * panelScale, (float)Math.max(1, window.method_4489() - 16));
        float x = Math.max(8.0f, Math.min(this.posX.getFloatValue() * panelScale, (float)window.method_4489() - panelWidth - 8.0f));
        float y = Math.max(8.0f, Math.min(this.posY.getFloatValue() * panelScale, (float)window.method_4506() - 108.0f * panelScale));
        float displayScale = window.method_4495();
        Matrix3x2fStack matrices = event.context.method_51448();
        matrices.pushMatrix();
        matrices.scale(1.0f / displayScale, 1.0f / displayScale);
        try {
            this.panel.render(new MinecraftUiRenderer(event.context, displayScale), x, y, panelWidth, panelScale, this.opacity.getFloatValue(), delta, this.mc.field_1755 instanceof HookClickGuiScreen, this.showEmpty.getValue(), this.avatars.getValue(), window.method_4506(), (member, headX, headY, size, alpha) -> this.drawHead(event.context, member, headX, headY, size, alpha));
        }
        finally {
            matrices.popMatrix();
        }
    }

    private void drawHead(class_332 context, StaffListPanel.StaffMember member, float x, float y, float size, float alpha) {
        class_640 player;
        class_640 class_6402 = player = this.mc.method_1562() == null ? null : this.mc.method_1562().method_2871(member.uuid());
        if (player == null) {
            new MinecraftUiRenderer(context, this.mc.method_22683().method_4495()).icon(UiIcon.USER, x, y, size, UiRenderer.white(alpha * 0.5f));
            return;
        }
        class_7532.method_44443((class_332)context, (class_8685)player.method_52810(), (int)Math.round(x), (int)Math.round(y), (int)Math.max(1, Math.round(size)), (int)UiRenderer.white(alpha));
    }
}

