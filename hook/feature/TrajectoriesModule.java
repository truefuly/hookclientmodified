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
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1665;
import net.minecraft.class_1676;
import net.minecraft.class_1684;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1779;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1823;
import net.minecraft.class_1835;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4184;
import net.minecraft.class_4537;
import net.minecraft.class_4587;

/** Shows where a held bow, crossbow, trident, ender pearl or other throwable will fly and land, plus the path of flying projectiles. */
public final class TrajectoriesModule extends Module {
    private final RgbSetting color = new RgbSetting(EncryptedString.of("Color"), 0xFF7FD4FF);
    private final RgbSetting hitColor = new RgbSetting(EncryptedString.of("Hit Color"), 0xFFFF5A6E);
    private final NumberSetting width = new NumberSetting(EncryptedString.of("Line Width"), 1.0, 6.0, 1.5, 0.5).getValue(EncryptedString.of("Thickness of the flight path"));
    private final BooleanSetting hitBox = new BooleanSetting(EncryptedString.of("Show Hit Spot"), true).setDescription(EncryptedString.of("Marks the block or entity it is going to hit"));
    private final BooleanSetting always = new BooleanSetting(EncryptedString.of("Bow Always"), true).setDescription(EncryptedString.of("Shows the bow path even before you pull it back (as full power)"));
    private final BooleanSetting flying = new BooleanSetting(EncryptedString.of("Flying Projectiles"), true).setDescription(EncryptedString.of("Also shows where arrows and pearls that are already in the air will land"));

    private final List<RenderUtils.ColoredLine> lines = new ArrayList<>();

    public TrajectoriesModule() {
        super(EncryptedString.of("Trajectories"), EncryptedString.of("Shows how projectiles will fly and where they will hit"), -1, Category.RENDER);
        this.addSettings(this.color, this.hitColor, this.width, this.hitBox, this.always, this.flying);
    }

    /** speed, gravity, pitch offset, air drag, water drag */
    private double[] params(class_1799 stack) {
        if (stack == null || stack.method_7960()) return null;
        class_1792 item = stack.method_7909();
        var p = this.mc.field_1724;
        if (item instanceof class_1753) {
            float power = 1.0f;
            if (p.method_6115() && p.method_6030() == stack) power = class_1753.method_7722(p.method_6048());
            else if (!this.always.getValue()) return null;
            if (power < 0.1f) power = 0.1f;
            return new double[]{power * 3.0, 0.05, 0, 0.99, 0.6};
        }
        if (item instanceof class_1764) return class_1764.method_7781(stack) ? new double[]{3.15, 0.05, 0, 0.99, 0.6} : null;
        if (item instanceof class_1835) return new double[]{2.5, 0.05, 0, 0.99, 0.99};
        if (item instanceof class_1776 || item instanceof class_1823 || item instanceof class_1771) return new double[]{1.5, 0.03, 0, 0.99, 0.8};
        if (item instanceof class_4537) return new double[]{0.5, 0.05, -20, 0.99, 0.8};
        if (item instanceof class_1779) return new double[]{0.7, 0.07, -20, 0.99, 0.8};
        return null;
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        var p = this.mc.field_1724;
        if (p == null || this.mc.field_1687 == null) return;
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null) return;
        this.lines.clear();
        float tick = camera.method_55437();

        double[] pr = this.params(p.method_6047());
        if (pr == null) pr = this.params(p.method_6079());
        if (pr != null) {
            float yaw = p.method_61415(tick), pitch = p.method_61414(tick);
            double ry = Math.toRadians(yaw), rp = Math.toRadians(pitch), rpo = Math.toRadians(pitch + pr[2]);
            class_243 dir = new class_243(-Math.sin(ry) * Math.cos(rp), -Math.sin(rpo), Math.cos(ry) * Math.cos(rp)).method_1029();
            class_243 vel = dir.method_1021(pr[0]);
            class_243 mv = p.method_60478();
            vel = vel.method_1031(mv.field_1352, p.method_24828() ? 0.0 : mv.field_1351, mv.field_1350);
            class_243 eye = p.method_5836(tick);
            // start slightly below and beside the eyes so the line is visible in first person
            class_243 side = new class_243(-Math.cos(ry), 0, -Math.sin(ry)).method_1021(0.15);
            class_243 start = eye.method_1031(side.field_1352, -0.1, side.field_1350);
            this.simulate(start, vel, pr[1], pr[3], pr[4], p, true);
        }

        if (this.flying.getValue()) {
            for (class_1297 e : this.mc.field_1687.method_18112()) {
                if (!(e instanceof class_1676) || e.method_31481()) continue;
                if (!(e instanceof class_1665 || e instanceof class_1684)) continue;
                if (e.method_5739(p) > 128) continue;
                class_243 v = e.method_18798();
                if (v.method_1027() < 0.001) continue;
                boolean pearl = e instanceof class_1684;
                this.simulate(e.method_30950(tick), v, pearl ? 0.03 : 0.05, 0.99, pearl ? 0.8 : 0.6, e, false);
            }
        }

        if (this.lines.isEmpty()) return;
        class_4587 m = event.matrixStack;
        m.method_22903();
        RenderUtils.applyCameraTransform(m, camera);
        RenderUtils.renderLines(m, this.lines);
        m.method_22909();
    }

    private void simulate(class_243 pos, class_243 vel, double gravity, double drag, double waterDrag, class_1297 shooter, boolean own) {
        var world = this.mc.field_1687;
        List<class_243> pts = new ArrayList<>();
        pts.add(pos);
        class_243 hitPoint = null, hitNormal = null;
        class_238 hitEntityBox = null;
        for (int i = 0; i < 300; i++) {
            class_243 next = pos.method_1019(vel);
            class_3965 block = world.method_17742(new class_3959(pos, next, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, shooter));
            boolean hitBlock = block != null && block.method_17783() != class_239.class_240.field_1333;
            class_243 end = hitBlock ? block.method_17784() : next;
            class_1297 hitEntity = null;
            class_243 entityPoint = null;
            double bestD = Double.MAX_VALUE;
            class_238 sweep = new class_238(pos, end).method_1014(1.0);
            for (class_1297 e : world.method_8335(shooter, sweep)) {
                if (!(e instanceof class_1309) || !e.method_5863() || e.method_7325()) continue;
                if (own && e == this.mc.field_1724) continue;
                Optional<class_243> hit = e.method_5829().method_1014(0.3).method_992(pos, end);
                if (hit.isPresent()) {
                    double d = pos.method_1025(hit.get());
                    if (d < bestD) { bestD = d; hitEntity = e; entityPoint = hit.get(); }
                }
            }
            if (hitEntity != null) { pts.add(entityPoint); hitEntityBox = hitEntity.method_5829(); break; }
            pts.add(end);
            if (hitBlock) {
                hitPoint = end;
                var side = block.method_17780();
                hitNormal = new class_243(side.method_10148(), side.method_10164(), side.method_10165());
                break;
            }
            pos = next;
            boolean inWater = !world.method_8316(net.minecraft.class_2338.method_49638(pos)).method_15769();
            vel = vel.method_1021(inWater ? waterDrag : drag).method_1031(0, -gravity, 0);
            if (pos.field_1351 < world.method_31607() - 16) break;
        }
        this.drawPath(pts);
        float w = this.width.getFloatValue();
        int hc = this.hitColor.getArgb();
        if (!this.hitBox.getValue()) return;
        if (hitEntityBox != null) {
            RenderUtils.addBoxOutline(this.lines, hitEntityBox.field_1323, hitEntityBox.field_1322, hitEntityBox.field_1321, hitEntityBox.field_1320, hitEntityBox.field_1325, hitEntityBox.field_1324, withAlpha(hc, 0.9), w);
        } else if (hitPoint != null) {
            this.ring(hitPoint.method_1019(hitNormal.method_1021(0.02)), hitNormal, 0.35, withAlpha(hc, 0.95), w);
            this.ring(hitPoint.method_1019(hitNormal.method_1021(0.02)), hitNormal, 0.12, withAlpha(hc, 0.95), w);
            this.ring(hitPoint.method_1019(hitNormal.method_1021(0.02)), hitNormal, 0.35, withAlpha(hc, 0.25), w * 3.0f);
        }
    }

    /** Smooth Catmull-Rom curve through the tick points, with a soft glow underneath and a fade-in near the hand. */
    private void drawPath(List<class_243> pts) {
        if (pts.size() < 2) return;
        int c = this.color.getArgb();
        float w = this.width.getFloatValue();
        List<class_243> smooth = new ArrayList<>();
        int sub = 8;
        for (int i = 0; i < pts.size() - 1; i++) {
            class_243 p0 = pts.get(Math.max(0, i - 1)), p1 = pts.get(i), p2 = pts.get(i + 1), p3 = pts.get(Math.min(pts.size() - 1, i + 2));
            for (int s = 0; s < sub; s++) smooth.add(catmull(p0, p1, p2, p3, s / (double) sub));
        }
        smooth.add(pts.get(pts.size() - 1));
        double total = 0;
        for (int i = 1; i < smooth.size(); i++) total += smooth.get(i).method_1022(smooth.get(i - 1));
        double run = 0;
        for (int i = 1; i < smooth.size(); i++) {
            class_243 a = smooth.get(i - 1), b = smooth.get(i);
            run += b.method_1022(a);
            double fade = Math.min(1.0, run / 1.5); // fades in over the first 1.5 blocks
            this.lines.add(new RenderUtils.ColoredLine(a, b, withAlpha(c, 0.22 * fade), w * 3.5f));
            this.lines.add(new RenderUtils.ColoredLine(a, b, withAlpha(c, fade), w));
        }
    }

    private void ring(class_243 center, class_243 normal, double radius, Color c, float w) {
        class_243 up = Math.abs(normal.field_1351) > 0.9 ? new class_243(1, 0, 0) : new class_243(0, 1, 0);
        class_243 u = normal.method_1036(up).method_1029();
        class_243 v = normal.method_1036(u).method_1029();
        int seg = 40;
        class_243 prev = null;
        for (int i = 0; i <= seg; i++) {
            double t = Math.PI * 2 * i / seg;
            class_243 pt = center.method_1019(u.method_1021(Math.cos(t) * radius)).method_1019(v.method_1021(Math.sin(t) * radius));
            if (prev != null) this.lines.add(new RenderUtils.ColoredLine(prev, pt, c, w));
            prev = pt;
        }
    }

    private static class_243 catmull(class_243 p0, class_243 p1, class_243 p2, class_243 p3, double t) {
        double t2 = t * t, t3 = t2 * t;
        return new class_243(
                0.5 * (2 * p1.field_1352 + (-p0.field_1352 + p2.field_1352) * t + (2 * p0.field_1352 - 5 * p1.field_1352 + 4 * p2.field_1352 - p3.field_1352) * t2 + (-p0.field_1352 + 3 * p1.field_1352 - 3 * p2.field_1352 + p3.field_1352) * t3),
                0.5 * (2 * p1.field_1351 + (-p0.field_1351 + p2.field_1351) * t + (2 * p0.field_1351 - 5 * p1.field_1351 + 4 * p2.field_1351 - p3.field_1351) * t2 + (-p0.field_1351 + 3 * p1.field_1351 - 3 * p2.field_1351 + p3.field_1351) * t3),
                0.5 * (2 * p1.field_1350 + (-p0.field_1350 + p2.field_1350) * t + (2 * p0.field_1350 - 5 * p1.field_1350 + 4 * p2.field_1350 - p3.field_1350) * t2 + (-p0.field_1350 + 3 * p1.field_1350 - 3 * p2.field_1350 + p3.field_1350) * t3));
    }

    private static Color withAlpha(int argb, double mul) {
        int a = (int) Math.round(((argb >>> 24) & 0xFF) * Math.max(0, Math.min(1, mul)));
        return new Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, a);
    }
}
