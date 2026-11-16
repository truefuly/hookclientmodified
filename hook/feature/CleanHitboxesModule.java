package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.RgbSetting;
import hook.utils.EncryptedString;
import hook.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1531;
import net.minecraft.class_1542;
import net.minecraft.class_1569;
import net.minecraft.class_1657;
import net.minecraft.class_1676;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_4587;

/**
 * Real hitboxes (the exact boxes the game uses to hit things), drawn as clean thin outlines - like F3+B but you
 * choose which kinds of entities, colours, look line and eye height.
 */
public final class CleanHitboxesModule extends Module {
    private final BooleanSetting players = new BooleanSetting(EncryptedString.of("Players"), true);
    private final RgbSetting playerColor = new RgbSetting(EncryptedString.of("Player Color"), 0xFFFFFFFF);
    private final BooleanSetting hostile = new BooleanSetting(EncryptedString.of("Hostile Mobs"), true);
    private final RgbSetting hostileColor = new RgbSetting(EncryptedString.of("Hostile Color"), 0xFFFF6B5E);
    private final BooleanSetting passive = new BooleanSetting(EncryptedString.of("Passive Mobs"), true);
    private final RgbSetting passiveColor = new RgbSetting(EncryptedString.of("Passive Color"), 0xFF7BE38F);
    private final BooleanSetting items = new BooleanSetting(EncryptedString.of("Items"), false);
    private final RgbSetting itemColor = new RgbSetting(EncryptedString.of("Item Color"), 0xFFFFD24A);
    private final BooleanSetting projectiles = new BooleanSetting(EncryptedString.of("Projectiles"), false);
    private final RgbSetting projectileColor = new RgbSetting(EncryptedString.of("Projectile Color"), 0xFF6EC8FF);
    private final BooleanSetting others = new BooleanSetting(EncryptedString.of("Other Entities"), false);
    private final RgbSetting otherColor = new RgbSetting(EncryptedString.of("Other Color"), 0xFFB8B8C8);
    private final BooleanSetting self = new BooleanSetting(EncryptedString.of("Yourself (F5)"), false);
    private final BooleanSetting invisible = new BooleanSetting(EncryptedString.of("Show Invisible"), false);
    private final BooleanSetting lookLine = new BooleanSetting(EncryptedString.of("Look Direction"), false);
    private final BooleanSetting eyeHeight = new BooleanSetting(EncryptedString.of("Eye Height"), false);
    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 8.0, 128.0, 64.0, 4.0);
    private final NumberSetting width = new NumberSetting(EncryptedString.of("Line Width"), 1.0, 4.0, 1.5, 0.5);
    private final NumberSetting opacity = new NumberSetting(EncryptedString.of("Opacity %"), 20.0, 100.0, 85.0, 5.0);
    private final BooleanSetting throughWalls = new BooleanSetting(EncryptedString.of("Through Walls"), false);

    public CleanHitboxesModule() {
        super(EncryptedString.of("Clean Hitboxes"), EncryptedString.of("Shows the real hitboxes of players, mobs and other things as clean outlines. Pick what and which colours in the settings."), -1, Category.RENDER);
        this.players.setDescription(EncryptedString.of("Hitboxes of other players."));
        this.hostile.setDescription(EncryptedString.of("Zombies, skeletons, creepers and every other hostile mob."));
        this.passive.setDescription(EncryptedString.of("Animals, villagers and other non-hostile mobs."));
        this.items.setDescription(EncryptedString.of("Dropped items."));
        this.projectiles.setDescription(EncryptedString.of("Arrows, tridents, pearls, snowballs..."));
        this.others.setDescription(EncryptedString.of("Everything else: minecarts, boats, armor stands, item frames, XP orbs..."));
        this.self.setDescription(EncryptedString.of("Your own hitbox when you are in third person."));
        this.invisible.setDescription(EncryptedString.of("Also shows invisible entities."));
        this.lookLine.setDescription(EncryptedString.of("A short line where players and mobs are looking."));
        this.eyeHeight.setDescription(EncryptedString.of("A thin line around the box at eye height."));
        this.range.setDescription(EncryptedString.of("Only entities closer than this (blocks)."));
        this.width.setDescription(EncryptedString.of("Thickness of the lines."));
        this.opacity.setDescription(EncryptedString.of("How strong the lines are."));
        this.throughWalls.setDescription(EncryptedString.of("On: visible through blocks. Off: hidden behind blocks like vanilla hitboxes."));
        hook.module.setting.SettingVisibility.set(this.playerColor, this.players::getValue);
        hook.module.setting.SettingVisibility.set(this.hostileColor, this.hostile::getValue);
        hook.module.setting.SettingVisibility.set(this.passiveColor, this.passive::getValue);
        hook.module.setting.SettingVisibility.set(this.itemColor, this.items::getValue);
        hook.module.setting.SettingVisibility.set(this.projectileColor, this.projectiles::getValue);
        hook.module.setting.SettingVisibility.set(this.otherColor, this.others::getValue);
        this.addSettings(this.players, this.playerColor, this.hostile, this.hostileColor, this.passive, this.passiveColor,
            this.items, this.itemColor, this.projectiles, this.projectileColor, this.others, this.otherColor,
            this.self, this.invisible, this.lookLine, this.eyeHeight, this.range, this.width, this.opacity, this.throughWalls);
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) return;
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null) return;
        float tick = camera.method_55437();
        double maxSq = this.range.getValue() * this.range.getValue();
        int alpha = (int) Math.round(255 * this.opacity.getValue() / 100.0);
        float w = this.width.getFloatValue();
        class_243 cam = camera.method_71156();
        List<RenderUtils.ColoredLine> lines = new ArrayList<>();
        for (class_1297 e : this.mc.field_1687.method_18112()) {
            if (e.method_31481()) continue;
            if (e == this.mc.field_1724 || e == this.mc.method_1560()) {
                if (!this.self.getValue() || !camera.method_19333()) continue; // own box only in third person
            }
            if (e instanceof class_1309 l && !l.method_5805()) continue;
            if (!this.invisible.getValue() && e.method_5767()) continue;
            if (NoRenderModule.hides(e)) continue;
            Integer argb = this.colorOf(e);
            if (argb == null) continue;
            class_243 pos = e.method_30950(tick);
            if (pos.method_1025(cam) > maxSq) continue;
            Color c = new Color((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, alpha);
            // the real box, moved to where the entity is drawn this frame
            class_243 off = pos.method_1020(new class_243(e.method_23317(), e.method_23318(), e.method_23321()));
            class_238 b = e.method_5829().method_997(off);
            RenderUtils.addBoxOutline(lines, b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324, c, w);
            if (this.eyeHeight.getValue() && e instanceof class_1309) {
                double y = pos.field_1351 + e.method_5751();
                Color ec = new Color(255, 80, 80, alpha);
                lines.add(new RenderUtils.ColoredLine(new class_243(b.field_1323, y, b.field_1321), new class_243(b.field_1320, y, b.field_1321), ec, w));
                lines.add(new RenderUtils.ColoredLine(new class_243(b.field_1320, y, b.field_1321), new class_243(b.field_1320, y, b.field_1324), ec, w));
                lines.add(new RenderUtils.ColoredLine(new class_243(b.field_1320, y, b.field_1324), new class_243(b.field_1323, y, b.field_1324), ec, w));
                lines.add(new RenderUtils.ColoredLine(new class_243(b.field_1323, y, b.field_1324), new class_243(b.field_1323, y, b.field_1321), ec, w));
            }
            if (this.lookLine.getValue() && e instanceof class_1309) {
                class_243 eye = new class_243(pos.field_1352, pos.field_1351 + e.method_5751(), pos.field_1350);
                class_243 dir = e.method_5828(tick);
                lines.add(new RenderUtils.ColoredLine(eye, eye.method_1019(dir.method_1021(1.6)), new Color(90, 150, 255, alpha), w));
            }
        }
        if (lines.isEmpty()) return;
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        RenderUtils.applyCameraTransform(matrices, camera);
        RenderUtils.renderLines(matrices, lines, this.throughWalls.getValue());
        matrices.method_22909();
    }

    /** Colour for this kind of entity, or null when that kind is turned off. */
    private Integer colorOf(class_1297 e) {
        if (e instanceof class_1657) return this.players.getValue() ? this.playerColor.getArgb() : null;
        if (e instanceof class_1542) return this.items.getValue() ? this.itemColor.getArgb() : null;
        if (e instanceof class_1676) return this.projectiles.getValue() ? this.projectileColor.getArgb() : null;
        if (e instanceof class_1569) return this.hostile.getValue() ? this.hostileColor.getArgb() : null;
        if (e instanceof class_1309 && !(e instanceof class_1531)) return this.passive.getValue() ? this.passiveColor.getArgb() : null;
        return this.others.getValue() ? this.otherColor.getArgb() : null;
    }
}
