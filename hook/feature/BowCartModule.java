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
    private final hook.module.setting.NumberSetting speed = hook.module.setting.NumberSetting.speed(7.0);
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
    // after a shot: did the server really fire the arrow and blow the cart up? (explains failures on servers)
    private int watchUntil = -1;
    private class_2338 watchRail;
    private boolean watchArrow, watchCart;
    private volatile boolean watchBoom;
    // Lag fix: some servers (anticheats / plugins) drop the arrow when rail + cart come in the same tick as the bow
    // release. After such a dropped shot the placement is sent 1 (then 2) ticks later, aimed at where the arrow is by then.
    private static final int TEST_DELAY = Integer.getInteger("hook.bowdelay", 0);
    private int placeDelay = TEST_DELAY;
    private String delayServer = "";
    private CartPlanner.Plan delayed;
    private int delayedAt = -1;
    private class_243 launchStart, launchVel;
    private float launchPower;
    private int shotTick, watchArrowAt = -1, watchCartAt = -1;
    // the server's "arrow spawned" packet for our arrow (seen even if the arrow is gone again before the next tick)
    private volatile long shotNanos, arrowPacketNanos;
    // TNT carts can't explode here (gamerule / protected area): stop wasting carts until you join again or move away
    private int tntBlockedHits;
    private class_243 tntBlockedAt;


    public BowCartModule() {
        super(EncryptedString.of("Bow Cart"), EncryptedString.of("Flame bow: rail + TNT minecart are placed where the arrow lands (in reach), instantly. Normal bows are ignored."), -1, Category.COMBAT);
        this.highlight.setDescription(EncryptedString.of("Holding or drawing a Flame bow: a soft outline on the spot where the cart will be placed."));
        this.color.setDescription(EncryptedString.of("Colour of the outline"));
        this.messages.setDescription(EncryptedString.of("Tells you in the action bar why it could not cart."));
        this.crosshair.setDescription(EncryptedString.of("On: rail + cart go on the block you look at when you shoot (any angle). Off: where the arrow lands."));
        this.flameOnly.setDescription(EncryptedString.of("Normal bows (no Flame) can never light a cart, so they never place one. On: they just shoot. Off: you also get a hint."));
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
        this.cooldown.follow(this.speed, 2, 30);
        this.swapDelay.follow(this.speed, 0, 6);
        this.addSettings(this.crosshair, this.humanize, this.lookBack, this.flameOnly, this.speed, this.minPower, this.skipIfCart, this.swapBack, this.quickDraw, this.highlight, this.color, this.messages);
        hook.module.setting.SettingVisibility.set(this.color, this.highlight::getValue);
        this.moreSettings(this.lookBack, this.minPower, this.skipIfCart, this.quickDraw, this.color, this.messages);
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
        this.delayed = null;
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
        m.shotTick = p.field_6012;
        m.shotNanos = System.nanoTime();
        m.arrowPacketNanos = 0L;
        String srv = m.mc.method_1558() == null ? "" : String.valueOf(m.mc.method_1558().field_3761);
        if (!srv.equals(m.delayServer)) { m.delayServer = srv; m.placeDelay = TEST_DELAY; m.tntBlockedHits = 0; m.tntBlockedAt = null; }
        if (m.tntBlockedAt != null) {
            if (new class_243(p.method_23317(), p.method_23318(), p.method_23321()).method_1022(m.tntBlockedAt) < 64.0) {
                m.lastPlace = Integer.MIN_VALUE / 2;
                m.fail("paused - TNT carts don't explode here (move 64+ blocks away or rejoin to try again)");
                log("shot skipped: tnt blocked here");
                return;
            }
            m.tntBlockedAt = null;
            m.tntBlockedHits = 0;
        }
        log("shot server=" + (srv.isEmpty() ? "singleplayer" : srv) + " ping=" + m.pingTicks() * 50 + "ms delay=" + m.placeDelay + " draw=" + m.launchPower + " rail=" + plan.rail.method_23854());
        if (m.placeDelay > 0) {
            // the release goes out alone in this tick; rail + cart follow in a later tick
            m.delayed = plan;
            m.delayedAt = p.field_6012 + m.placeDelay;
            return;
        }
        m.placeNow(p, plan);
    }

    private static void log(String msg) {
        System.out.println("[BowCart] " + msg);
    }

    /** Delayed placement: follow the predicted arrow forward by the delay and use a spot it still has to fly through. */
    private void placeDelayed(class_746 p) {
        if (this.delayed == null || p.field_6012 < this.delayedAt) return;
        CartPlanner.Plan plan = this.delayed;
        this.delayed = null;
        if (this.launchStart == null || this.mc.field_1687 == null) return;
        class_243 pos = this.launchStart, vel = this.launchVel;
        // the arrow moves once in the tick it is fired, then once per tick until our packets are handled
        for (int i = 0; i < this.placeDelay; i++) {
            class_243 next = pos.method_1019(vel);
            net.minecraft.class_3965 bh = this.mc.field_1687.method_17742(new net.minecraft.class_3959(pos, next, net.minecraft.class_3959.class_3960.field_17558, net.minecraft.class_3959.class_242.field_1348, p));
            if (bh != null && bh.method_17783() != net.minecraft.class_239.class_240.field_1333) { pos = null; break; }
            pos = next;
            vel = vel.method_1021(0.99).method_1031(0, -0.05, 0);
        }
        CartPlanner.Plan use = null;
        if (pos != null) {
            if (CartPlanner.trace(pos, vel, CartPlanner.cartBox(plan.rail, plan.slope)).hitsCart) use = plan;
            else try { use = this.pathPlan(p, true, pos, vel, this.launchPower); } catch (Throwable t) { use = null; }
        }
        if (use == null) {
            log("delayed: arrow already past every spot in reach");
            this.fail("lag fix on (" + this.placeDelay + " tick): too close - aim a bit further away");
            return;
        }
        if (this.skipIfCart.getValue() && this.hasCart(use.rail)) return;
        if (CartTest.ENABLED) { CartTest.planRail = use.rail; CartTest.planInfo = (CartTest.planInfo == null ? "" : CartTest.planInfo) + " delayed=" + this.placeDelay + " rail=" + use.rail.method_23854(); }
        this.placeNow(p, use);
    }

    private void placeNow(class_746 p, CartPlanner.Plan plan) {
        BowCartModule m = this;
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
        m.watchUntil = p.field_6012 + 16;
        m.watchRail = plan.rail;
        m.watchArrow = false;
        m.watchCart = false;
        m.watchArrowAt = -1;
        m.watchCartAt = -1;
        log("placed +" + (p.field_6012 - m.shotTick) + "t rail=" + plan.rail.method_23854());
        m.watchBoom = false;
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
        this.placeDelayed(p);
        this.watchShot(p);
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

    private void watchShot(class_746 p) {
        if (this.watchUntil < 0 || this.mc.field_1687 == null) return;
        if (this.watchBoom) { this.watchUntil = -1; this.tntBlockedHits = 0; log("BOOM +" + (p.field_6012 - this.shotTick) + "t arrowSeen=" + this.watchArrowAt + " cartSeen=" + this.watchCartAt); return; }
        if (!this.watchArrow && !this.mc.field_1687.method_8390(net.minecraft.class_1665.class, p.method_5829().method_1014(16.0),
                a -> a.method_24921() == p && a.field_6012 < 40).isEmpty()) { this.watchArrow = true; this.watchArrowAt = p.field_6012 - this.shotTick; }
        boolean cart = this.hasCart(this.watchRail);
        if (cart && !this.watchCart) { this.watchCart = true; this.watchCartAt = p.field_6012 - this.shotTick; }
        if (p.field_6012 < this.watchUntil) return;
        this.watchUntil = -1;
        long ap = this.arrowPacketNanos;
        boolean fired = this.watchArrow || ap != 0L;
        log("result arrowSeen=" + this.watchArrowAt + " arrowPacket=" + (ap == 0L ? "none" : ((ap - this.shotNanos) / 1_000_000L) + "ms") + " cartSeen=" + this.watchCartAt + " cartNow=" + cart + " railNow=" + CartPlanner.isRailBlock(this.watchRail) + " boom=false");
        if (!this.watchCart) { this.fail("the cart didn't stay on the rail (the server removed it)"); return; }
        if (!cart) return; // cart is gone: it went off
        if (fired) {
            // the arrow came out and is gone, the cart is untouched: it hit the cart and the server stopped the blast
            if (++this.tntBlockedHits >= 2) {
                this.tntBlockedAt = new class_243(p.method_23317(), p.method_23318(), p.method_23321());
                this.fail("arrow hit the cart twice but TNT doesn't explode here - Bow Cart paused here so no more carts are wasted");
            } else this.fail("arrow fired and hit, but this server doesn't let TNT carts explode here (TNT off / protected area)");
            return;
        }
        if (!this.watchArrow) {
            if (this.placeDelay < 2) {
                this.placeDelay++;
                this.fail("the server dropped your arrow - lag fix: next shots place " + this.placeDelay + " tick later");
            } else this.fail("the server didn't fire your arrow");
        } else this.fail("arrow fired, but the cart didn't explode - TNT is probably blocked here");
    }

    @EventListener
    public void onPacket(hook.event.events.PacketReceiveEvent event) {
        if (event.packet instanceof net.minecraft.class_2664) this.watchBoom = true;
        else if (event.packet instanceof net.minecraft.class_8038<?> bundle) {
            for (Object sub : bundle.method_48324()) if (sub instanceof net.minecraft.class_2604 add) this.onAddEntity(add);
        } else if (event.packet instanceof net.minecraft.class_2604 add) this.onAddEntity(add);
        // server messages while a shot is watched (for example a protection plugin saying no) go to the log
        else if (this.watchUntil >= 0 && event.packet instanceof net.minecraft.class_7439 msg) {
            try { log("server msg: " + msg.comp_763().method_10851()); } catch (Throwable ignored) {}
        }
    }

    private void onAddEntity(net.minecraft.class_2604 add) {
        if (add.method_11169() != net.minecraft.class_1299.field_6122 && add.method_11169() != net.minecraft.class_1299.field_6135) return;
        var p = this.mc.field_1724;
        if (p == null || add.method_11166() != p.method_5628()) return; // data = owner id
        if (this.shotNanos == 0L || System.nanoTime() - this.shotNanos > 3_000_000_000L || this.arrowPacketNanos != 0L) return;
        this.arrowPacketNanos = System.nanoTime();
    }

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
        // a normal arrow can't light a TNT minecart: placing one would only catch the arrow
        if (!CartPlanner.hasFlame(bow)) return quiet || this.flameOnly.getValue() ? null : this.fail("this bow has no Flame - the arrow can't light the cart");
        if (power < Math.max(0.1f, this.minPower.getFloatValue() / 100.0f)) return null;
        // The server counts the draw itself, from when YOUR start-drawing packet arrived. With lag that can be a few
        // ticks less than you see; under 3 server ticks it fires no arrow at all - and then a cart placed with the
        // shot would just stand there. So only cart shots the server surely fires (2 ticks + your ping of margin).
        if (!quiet && p.method_6115()) {
            int need = 3 + 2 + this.pingTicks();
            if (p.method_6048() < need) return this.fail("drawn too short for the server (" + p.method_6048() + "/" + need + " ticks) - just shot, no cart placed");
        }
        if (CartPlanner.cartSlot() < 0) return quiet ? null : this.fail("no TNT minecart in hotbar");

        // same launch as BowItem: eye - 0.1, rotation * power * 3, plus your own movement
        class_243 start = new class_243(p.method_23317(), p.method_23320() - 0.1, p.method_23321());
        class_243 vel = CartPlanner.dir(p.method_36454(), p.method_36455()).method_1021(power * 3.0);
        class_243 mv = p.method_60478();
        vel = vel.method_1031(mv.field_1352, p.method_24828() ? 0.0 : mv.field_1351, mv.field_1350);
        if (!quiet) { this.launchStart = start; this.launchVel = vel; this.launchPower = power; }

        if (this.crosshair.getValue() && power >= this.quickDraw.getFloatValue() / 100.0f) {
            CartPlanner.Plan cp = this.crosshairPlan(p, quiet, start, vel);
            if (cp == SHOOT_ONLY) return null;
            if (cp != null) return cp;
            // nothing there, or the arrow (it leaves 0.1 below your eyes) would only graze under the cart into the
            // block: use a spot on the arrow's real path instead
        }
        return this.pathPlan(p, quiet, start, vel, power);
    }

    private static final CartPlanner.Plan SHOOT_ONLY = new CartPlanner.Plan();

    /** Your ping in game ticks (50 ms each), from the tab list. */
    private int pingTicks() {
        try {
            net.minecraft.class_640 info = this.mc.method_1562().method_2871(this.mc.field_1724.method_5667());
            return info == null ? 0 : (Math.max(0, info.method_2959()) + 49) / 50;
        } catch (Throwable t) {
            return 0;
        }
    }

    private CartPlanner.Plan crosshairPlan(class_746 p, boolean quiet, class_243 start, class_243 vel) {
        {
            // the block under your crosshair at the moment you shoot - works from any angle
            if (!(p.method_5745(p.method_55754(), 1.0f, false) instanceof net.minecraft.class_3965 bh) || bh.method_17783() != net.minecraft.class_239.class_240.field_1332) return null;
            class_2338 hitPos = bh.method_17777();
            class_2338 r;
            net.minecraft.class_3965 rc;
            if (CartPlanner.isRailBlock(hitPos)) { r = hitPos; rc = null; }
            else {
                if (bh.method_17780() == class_2350.field_11033) return null;
                r = hitPos.method_10093(bh.method_17780());
                rc = bh;
            }
            if (CartPlanner.railSlot() < 0 && !CartPlanner.isRailBlock(r)) return null;
            if (this.skipIfCart.getValue() && this.hasCart(r)) return SHOOT_ONLY; // a cart is already there: just shoot it
            CartPlanner.Plan cp = new CartPlanner.Plan();
            if (!CartPlanner.prepareRail(cp, r, rc)) return null;
            if (!CartPlanner.trace(start, vel, CartPlanner.cartBox(r)).hitsCart) return null;
            if (!quiet && CartTest.ENABLED) { CartTest.planFire = null; CartTest.planRail = cp.rail; CartTest.planInfo = "CROSSHAIR rail=" + cp.rail.method_23854(); }
            return cp;
        }
    }

    private CartPlanner.Plan pathPlan(class_746 p, boolean quiet, class_243 start, class_243 vel, float power) {
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
