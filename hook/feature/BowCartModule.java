package hook.feature;

import hook.event.EventListener;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.RgbSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import hook.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1753;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_746;

/**
 * Bow cart (Flame bows only): while you draw a Flame bow it shows where the rail and TNT minecart would go; when you
 * let go it places them there in the same tick, right after the shot, so the burning arrow blows the cart up the
 * moment it arrives. If the arrow comes down out of your reach (shooting up, far away) nothing is placed.
 *
 * Raised spots: an arrow aimed at the top of a block hits that block's side just below where a cart on top would be.
 * Then a rail goes on top of the block and one in front of it, so the front one becomes a ramp and the cart stands
 * half a block higher, exactly in the arrow's way (with no block there, one from your hotbar is put down first).
 */
public final class BowCartModule extends Module {
    private static BowCartModule instance;
    private final BooleanSetting highlight = new BooleanSetting(EncryptedString.of("Highlight"), true);
    private final RgbSetting color = new RgbSetting(EncryptedString.of("Highlight Color"), 0xFFFF8A3D);
    private final BooleanSetting messages = new BooleanSetting(EncryptedString.of("Messages"), true);
    private final BooleanSetting crosshair = new BooleanSetting(EncryptedString.of("Place At Crosshair"), true);
    private final BooleanSetting flameOnly = new BooleanSetting(EncryptedString.of("Flame Bows Only"), true);
    private final hook.module.setting.NumberSetting minPower = new hook.module.setting.NumberSetting(EncryptedString.of("Min Draw %"), 0.0, 100.0, 0.0, 5.0);
    private final hook.module.setting.NumberSetting cooldown = new hook.module.setting.NumberSetting(EncryptedString.of("Cooldown (ticks)"), 0.0, 40.0, 10.0, 1.0);
    private final BooleanSetting skipIfCart = new BooleanSetting(EncryptedString.of("Skip If Cart There"), true);
    private final BooleanSetting swapBack = new BooleanSetting(EncryptedString.of("Swap Back"), true);
    private final hook.module.setting.NumberSetting quickDraw = new hook.module.setting.NumberSetting(EncryptedString.of("Quick Shot Below %"), 0.0, 100.0, 60.0, 5.0);
    private int lastPlace = Integer.MIN_VALUE / 2;
    private final hook.module.setting.NumberSetting swapDelay = new hook.module.setting.NumberSetting(EncryptedString.of("Swap Back Delay (ticks)"), 0.0, 10.0, 2.0, 1.0);
    private int swapBackAt = -1, swapBackSlot = -1;
    private final BooleanSetting humanize = new BooleanSetting(EncryptedString.of("Humanize"), true);
    private final BooleanSetting lookBack = new BooleanSetting(EncryptedString.of("Look Back"), true);
    private final CartHuman.Turner turner = new CartHuman.Turner();
    private int lookBackAt = -1, lookBackEnd = -1;
    private float backYaw, backPitch;
    private CartPlanner.Plan pending;
    private volatile CartPlanner.Plan preview;
    private int lastAge = Integer.MIN_VALUE;


    public BowCartModule() {
        super(EncryptedString.of("Bow Cart"), EncryptedString.of("Flame bow: rail + TNT minecart are placed where the arrow lands (in reach), instantly. Normal bows are ignored."), -1, Category.COMBAT);
        this.highlight.setDescription(EncryptedString.of("Holding or drawing a Flame bow: a soft outline on the spot where the cart will be placed."));
        this.color.setDescription(EncryptedString.of("Colour of the outline"));
        this.messages.setDescription(EncryptedString.of("Tells you in the action bar why it could not cart."));
        this.crosshair.setDescription(EncryptedString.of("On: rail + cart go on the block you look at when you shoot (any angle). Off: where the arrow lands."));
        this.flameOnly.setDescription(EncryptedString.of("Only Flame bows trigger it; normal bows shoot normally."));
        this.minPower.setDescription(EncryptedString.of("Shots drawn less than this do nothing."));
        this.cooldown.setDescription(EncryptedString.of("Minimum ticks between two placements - stops double / spam placing."));
        this.skipIfCart.setDescription(EncryptedString.of("Does not place again where a minecart already stands."));
        this.swapBack.setDescription(EncryptedString.of("Switches back to the bow after placing."));
        this.quickDraw.setDescription(EncryptedString.of("Shots drawn less than this (quick taps) put rail + cart where the arrow will land instead of the crosshair."));
        this.swapDelay.setDescription(EncryptedString.of("Stays on the minecart slot this many ticks before going back to the bow, like a real player."));
        hook.module.setting.SettingVisibility.set(this.swapDelay, this.swapBack::getValue);
        this.humanize.setDescription(EncryptedString.of("Places like a player: one real mouse-like look at the spot (no extra rotation packets), click points on that view line instead of the exact centre, one arm swing per placement, random swap-back pause. Off = rotation packets only."));
        this.lookBack.setDescription(EncryptedString.of("After placing, the head moves smoothly back to where you were aiming, like you would."));
        hook.module.setting.SettingVisibility.set(this.lookBack, this.humanize::getValue);
        this.addSettings(this.crosshair, this.humanize, this.lookBack, this.flameOnly, this.minPower, this.cooldown, this.skipIfCart, this.swapBack, this.swapDelay, this.quickDraw, this.highlight, this.color, this.messages);
        hook.module.setting.SettingVisibility.set(this.color, this.highlight::getValue);
        instance = this;
    }

    @Override
    public void onEnable() {
        thetruetrident.cartcore.CartcoreCore.cartConfig.instaCarting = false; // old insta-cart auto-releases the bow
        super.onEnable();
    }

    public static boolean active() {
        return instance != null && instance.isEnabled();
    }

    private boolean hasCart(class_2338 r) {
        return this.mc.field_1687 != null && !this.mc.field_1687.method_8390(net.minecraft.class_1688.class, new class_238(r).method_1014(0.3), e -> e.method_5805()).isEmpty();
    }

    @Override
    public void onDisable() {
        this.preview = null;
        this.pending = null;
        this.turner.stop();
        this.lookBackAt = -1;
        super.onDisable();
    }

    /** Called right before the bow is released (the arrow is about to be shot). */
    public static void beforeRelease() {
        BowCartModule m = instance;
        if (m == null) return;
        m.pending = null;
        if (!m.isEnabled()) return;
        try { m.pending = m.plan(false); } catch (Throwable t) { m.pending = null; }
    }

    /** Called right after the release packet went out: place everything before the arrow's first server tick. */
    public static void afterRelease() {
        BowCartModule m = instance;
        if (m == null) return;
        m.preview = null;
        if (m.pending == null) return;
        CartPlanner.Plan plan = m.pending;
        m.pending = null;
        class_746 p = m.mc.field_1724;
        if (p == null) return;
        // exactly one rail + one cart per shot
        if (p.field_6012 - m.lastPlace < m.cooldown.getIntValue()) return;
        if (m.skipIfCart.getValue() && m.hasCart(plan.rail)) return;
        m.lastPlace = p.field_6012;
        int slot = p.method_31548().method_67532();
        float yaw0 = p.method_36454(), pitch0 = p.method_36455();
        boolean human = m.humanize.getValue();
        boolean turned = true;
        CartPlanner.REAL_ROTATE = human; // the head really looks at what it clicks (mouse-like, no extra packets)
        try {
            if (human && plan.rampBlockClick == null && plan.upperClick == null) turned = m.placeFlat(p, plan);
            else {
                if (!(plan.railClick == null && CartPlanner.isRailBlock(plan.rail))) CartPlanner.placeRail(plan);
                CartPlanner.placeCart(plan);
            }
        } finally {
            CartPlanner.REAL_ROTATE = false;
        }
        if (human && m.lookBack.getValue() && turned && CartHuman.angle(p, yaw0, pitch0) > 1.0) {
            // look back up to where you aimed, a moment later and not perfectly exact
            m.backYaw = yaw0 + (float) CartHuman.gauss(0, 1.2, -3, 3);
            m.backPitch = pitch0 + (float) CartHuman.gauss(0, 1.0, -3, 3);
            m.lookBackAt = p.field_6012 + CartHuman.ticks(0.9, 0.4, 1);
            m.lookBackEnd = m.lookBackAt + 20;
        }
        if (m.swapBack.getValue()) {
            int delay = human ? CartHuman.ticks(m.swapDelay.getValue() + 0.5, 0.7, Math.max(1, m.swapDelay.getIntValue())) : m.swapDelay.getIntValue();
            if (delay <= 0) InventoryUtil.swap(slot);
            else { m.swapBackSlot = slot; m.swapBackAt = p.field_6012 + delay; }
        }
    }

    /**
     * Flat rail spot: one look at a random point on the rail spot, then both clicks lie on that one view line (rail
     * on the floor where the line meets it, cart where it meets the rail's top) - exactly what a player looking there
     * and clicking twice would send.
     */
    private boolean placeFlat(class_746 p, CartPlanner.Plan plan) {
        class_2338 r = plan.rail;
        class_243 eye = p.method_33571();
        class_243 dir = CartPlanner.dir(p.method_36454(), p.method_36455());
        // already looking at the spot (crosshair mode): no head movement at all, the clicks lie on your own view line
        boolean turned = !looksInto(eye, dir, r);
        if (turned) {
            class_243 aim = new class_243(r.method_10263() + 0.5 + CartHuman.jitter(0.26), r.method_10264() + 0.125, r.method_10260() + 0.5 + CartHuman.jitter(0.26));
            CartHuman.turnTo(p, CartPlanner.aimAt(eye, aim));
            dir = CartPlanner.dir(p.method_36454(), p.method_36455());
        }
        boolean floorClick = plan.railClick != null && plan.railClick.method_17780() == class_2350.field_11036 && plan.railClick.method_17777().equals(r.method_10074());
        if (plan.railClick != null) {
            InventoryUtil.swap(CartPlanner.railSlot());
            CartPlanner.useOn(floorClick ? new net.minecraft.class_3965(onLine(eye, dir, r, r.method_10264()), class_2350.field_11036, r.method_10074(), false) : plan.railClick);
        }
        InventoryUtil.swap(CartPlanner.cartSlot());
        CartPlanner.useOn(new net.minecraft.class_3965(onLine(eye, dir, r, r.method_10264() + 0.125), class_2350.field_11036, r, false));
        return turned;
    }

    /** True if the view line goes down into the rail cell's floor (both at the floor and at the rail's top). */
    private static boolean looksInto(class_243 eye, class_243 dir, class_2338 r) {
        if (dir.field_1351 > -1.0e-3) return false;
        for (double y : new double[]{r.method_10264(), r.method_10264() + 0.125}) {
            double t = (y - eye.field_1351) / dir.field_1351;
            double x = eye.field_1352 + dir.field_1352 * t - r.method_10263(), z = eye.field_1350 + dir.field_1350 * t - r.method_10260();
            if (x < 0.04 || x > 0.96 || z < 0.04 || z > 0.96) return false;
        }
        return true;
    }

    /** Where the view line crosses height y, kept inside the block column of r. */
    private static class_243 onLine(class_243 eye, class_243 dir, class_2338 r, double y) {
        double x = r.method_10263() + 0.5, z = r.method_10260() + 0.5;
        if (dir.field_1351 < -1.0e-3) {
            double t = (y - eye.field_1351) / dir.field_1351;
            x = eye.field_1352 + dir.field_1352 * t;
            z = eye.field_1350 + dir.field_1350 * t;
        }
        x = Math.max(r.method_10263() + 0.02, Math.min(r.method_10263() + 0.98, x));
        z = Math.max(r.method_10260() + 0.02, Math.min(r.method_10260() + 0.98, z));
        return new class_243(x, y, z);
    }

    @EventListener
    public void onTick(TickEvent event) {
        class_746 p = this.mc.field_1724;
        if (p == null) { this.preview = null; return; }
        if (p.field_6012 == this.lastAge) return; // the tick event fires twice per game tick
        this.lastAge = p.field_6012;
        if (this.lookBackAt >= 0 && p.field_6012 >= this.lookBackAt) {
            this.turner.aim(p, this.backYaw, this.backPitch, 90.0, false);
            this.lookBackAt = -1;
        }
        if (this.turner.active()) {
            this.turner.tickFallback(p);
            if (p.field_6012 > this.lookBackEnd || this.turner.dist(p) < 0.3 || p.method_6115()) this.turner.stop();
        }
        if (this.swapBackAt >= 0 && p.field_6012 >= this.swapBackAt) {
            if (!p.method_6115()) InventoryUtil.swap(this.swapBackSlot);
            this.swapBackAt = -1;
        }
        if (!this.highlight.getValue()) { this.preview = null; return; }
        try { this.preview = this.plan(true); } catch (Throwable t) { this.preview = null; }
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.turner.active()) this.turner.frame(this.mc.field_1724);
        CartPlanner.Plan plan = this.preview;
        if (plan == null || !this.highlight.getValue()) return;
        class_4184 camera = RenderUtils.getCamera();
        if (camera == null) return;
        int argb = this.color.getArgb();
        Color base = new Color((argb >> 16) & 255, (argb >> 8) & 255, argb & 255);
        // gentle flicker, like a small flame breathing
        double t = System.currentTimeMillis() / 1000.0;
        double flicker = 0.78 + 0.14 * Math.sin(t * 5.1) + 0.08 * Math.sin(t * 11.7 + 1.3);
        List<RenderUtils.ColoredLine> lines = new ArrayList<>();
        floor(lines, plan.rail, plan.slope ? 0.0 : 0.0, base, flicker);
        if (plan.upper != null) floor(lines, plan.upper, 0.0, base, flicker);
        class_4587 matrices = event.matrixStack;
        matrices.method_22903();
        RenderUtils.applyCameraTransform(matrices, camera);
        RenderUtils.renderLines(matrices, lines);
        matrices.method_22909();
    }

    /** Thin outline lying on the top of the block below this cell, with two faint wider layers as a soft glow. */
    private static void floor(List<RenderUtils.ColoredLine> lines, class_2338 c, double lift, Color col, double flicker) {
        double y = c.method_10264() + 0.02 + lift;
        double[][] layers = {{0.0, 200, 1.6}, {0.035, 70, 3.5}, {0.07, 28, 6.0}};
        for (double[] l : layers) {
            double g = l[0];
            double x1 = c.method_10263() + 0.04 - g, z1 = c.method_10260() + 0.04 - g;
            double x2 = c.method_10263() + 0.96 + g, z2 = c.method_10260() + 0.96 + g;
            Color k = alpha(col, (int) Math.max(0, Math.min(255, l[1] * flicker)));
            float w = (float) l[2];
            lines.add(new RenderUtils.ColoredLine(new class_243(x1, y, z1), new class_243(x2, y, z1), k, w));
            lines.add(new RenderUtils.ColoredLine(new class_243(x2, y, z1), new class_243(x2, y, z2), k, w));
            lines.add(new RenderUtils.ColoredLine(new class_243(x2, y, z2), new class_243(x1, y, z2), k, w));
            lines.add(new RenderUtils.ColoredLine(new class_243(x1, y, z2), new class_243(x1, y, z1), k, w));
        }
    }

    private static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), a); }

    /** quiet = live preview: no messages, no test bookkeeping. */
    private CartPlanner.Plan plan(boolean quiet) {
        class_746 p = this.mc.field_1724;
        if (p == null || this.mc.field_1687 == null) return null;
        class_1799 bow;
        float power;
        if (p.method_6115()) {
            bow = p.method_6030();
            power = class_1753.method_7722(p.method_6048());
        } else {
            if (!quiet) return null;
            // just holding it: show where a full draw would land
            bow = p.method_6047().method_7909() instanceof class_1753 ? p.method_6047() : p.method_6079();
            power = 1.0f;
        }
        if (!(bow.method_7909() instanceof class_1753)) return null;
        if (this.flameOnly.getValue() && !CartPlanner.hasFlame(bow)) return null; // normal bows: ignored, plain shot
        if (power < Math.max(0.1f, this.minPower.getFloatValue() / 100.0f)) return null;
        if (CartPlanner.cartSlot() < 0) return quiet ? null : this.fail("no TNT minecart in hotbar");

        if (this.crosshair.getValue() && power >= this.quickDraw.getFloatValue() / 100.0f) {
            // the block under your crosshair at the moment you shoot - works from any angle
            if (!(p.method_5745(p.method_55754(), 1.0f, false) instanceof net.minecraft.class_3965 bh) || bh.method_17783() != net.minecraft.class_239.class_240.field_1332) return null;
            class_2338 hitPos = bh.method_17777();
            class_2338 r;
            net.minecraft.class_3965 rc;
            if (CartPlanner.isRailBlock(hitPos)) { r = hitPos; rc = null; }
            else {
                if (bh.method_17780() == class_2350.field_11033) return quiet ? null : this.fail("can't put a rail under a block");
                r = hitPos.method_10093(bh.method_17780());
                rc = bh;
            }
            if (CartPlanner.railSlot() < 0 && !CartPlanner.isRailBlock(r)) return quiet ? null : this.fail("no rail in hotbar");
            if (this.skipIfCart.getValue() && this.hasCart(r)) return null;
            CartPlanner.Plan cp = new CartPlanner.Plan();
            if (!CartPlanner.prepareRail(cp, r, rc)) return quiet ? null : this.fail("can't place a rail there");
            if (!quiet && CartTest.ENABLED) { CartTest.planFire = null; CartTest.planRail = cp.rail; CartTest.planInfo = "CROSSHAIR rail=" + cp.rail.method_23854(); }
            return cp;
        }

        // same launch as BowItem: eye - 0.1, rotation * power * 3, plus your own movement
        class_243 start = new class_243(p.method_23317(), p.method_23320() - 0.1, p.method_23321());
        class_243 vel = CartPlanner.dir(p.method_36454(), p.method_36455()).method_1021(power * 3.0);
        class_243 mv = p.method_60478();
        vel = vel.method_1031(mv.field_1352, p.method_24828() ? 0.0 : mv.field_1351, mv.field_1350);

        CartPlanner.Trace land = CartPlanner.trace(start, vel, null);
        class_243 eye = p.method_33571();
        double reach = p.method_55754(); // vanilla block reach
        class_2338 rail = null;
        if (land.block != null) {
            class_2338 hit = land.block.method_17777();
            class_2350 face = land.block.method_17780();
            if (CartPlanner.isRailBlock(hit)) rail = hit;
            else if (face != class_2350.field_11033) rail = hit.method_10093(face);
        }
        boolean landInReach = rail != null && eye.method_1022(new class_243(rail.method_10263() + 0.5, rail.method_10264(), rail.method_10260() + 0.5)) <= reach;

        // the landing cell first, then the cells the arrow crosses just before it (it often only grazes the
        // bottom of a cart standing exactly where it lands, but goes straight through one a block earlier).
        // When it lands out of reach (e.g. aimed over a raised spot), every cell on its way that is in reach counts.
        List<class_2338> candidates = new ArrayList<>();
        if (landInReach) candidates.add(rail);
        for (int i = land.cells.size() - 1; i >= 0 && candidates.size() < 20; i--) {
            class_2338 c = land.cells.get(i);
            if (candidates.contains(c)) continue;
            if (landInReach && Math.abs(c.method_10264() - rail.method_10264()) > 1) continue;
            if (eye.method_1022(new class_243(c.method_10263() + 0.5, c.method_10264(), c.method_10260() + 0.5)) > reach) continue;
            candidates.add(c);
        }
        if (candidates.isEmpty()) return null; // nothing in reach (shooting up / far): just a normal shot

        String why = "can't place a rail where it lands";
        CartPlanner.Plan plan = null;
        for (double inset : new double[]{0.15, 0.06}) {
            CartPlanner.INSET = inset;
            for (class_2338 c : candidates) {
                CartPlanner.Plan cand = new CartPlanner.Plan();
                if (!CartPlanner.prepareRail(cand, c, null)) continue;
                CartPlanner.Trace tr = CartPlanner.trace(start, vel, CartPlanner.cartBox(c));
                if (!tr.hitsCart) { why = "arrow would miss the cart there"; continue; }
                plan = cand;
                break;
            }
            if (plan != null) break;
        }
        // raised spot: the arrow hits the block's side just below a cart on top -> ramp rail, cart sits 0.5 higher
        if (plan == null) {
            List<class_2338> lows = new ArrayList<>();
            for (class_2338 c : candidates) {
                if (!lows.contains(c)) lows.add(c);
                if (!lows.contains(c.method_10074())) lows.add(c.method_10074());
            }
            outer:
            for (boolean placeBlock : new boolean[]{false, true})
                for (double inset : new double[]{0.15, 0.06}) {
                    CartPlanner.INSET = inset;
                    for (class_2338 c : lows) {
                        for (class_2350 d : CartPlanner.HORIZONTAL) {
                            CartPlanner.Plan cand = new CartPlanner.Plan();
                            if (!CartPlanner.prepareSlope(cand, c, d, placeBlock)) continue;
                            CartPlanner.Trace tr = CartPlanner.trace(start, vel, CartPlanner.cartBox(c, true));
                            if (!tr.hitsCart) continue;
                            if (cand.rampBlock != null && CartPlanner.pathEnters(tr, cand.rampBlock)) continue; // arrow would hit our block
                            plan = cand;
                            break outer;
                        }
                    }
                }
        }
        CartPlanner.INSET = 0.06;
        if (plan == null) {
            if (quiet || !landInReach) return null; // long shot past things: stay quiet
            return this.fail(CartPlanner.railSlot() < 0 ? "no rail in hotbar" : why);
        }
        if (!quiet && CartTest.ENABLED) {
            CartTest.planFire = null;
            CartTest.planRail = plan.rail;
            CartTest.planInfo = (plan.slope ? (plan.rampBlock != null ? "RAMP+BLOCK " : "RAMP ") : "") + "rail=" + plan.rail.method_23854() + " power=" + power;
        }
        return plan;
    }

    private CartPlanner.Plan fail(String why) {
        if (CartTest.ENABLED) CartTest.lastFail = why;
        if (this.messages.getValue() && this.mc.field_1724 != null)
            this.mc.field_1724.method_7353(class_2561.method_43470("§cBow Cart: §f" + why), true);
        return null;
    }
}
