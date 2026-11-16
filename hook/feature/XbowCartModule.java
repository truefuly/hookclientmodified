package hook.feature;

import hook.event.EventListener;
import hook.event.events.PreItemUseEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ButtonSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import java.util.ArrayDeque;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import thetruetrident.cartcore.CartcoreCore;

/**
 * Crossbow cart: right-click a block with a loaded crossbow and it places a rail, a TNT minecart on it, fire in the
 * arrow's way and fires the crossbow through the fire into the cart - all planned up front, by default in one tick.
 */
public final class XbowCartModule extends Module {
    public static XbowCartModule instance;
    private final NumberSetting stepDelay = new NumberSetting(EncryptedString.of("Step Delay (ticks)"), 0.0, 5.0, 1.0, 1.0);
    private final BooleanSetting messages = new BooleanSetting(EncryptedString.of("Messages"), true);
    private final NumberSetting cooldown = new NumberSetting(EncryptedString.of("Cooldown (ticks)"), 0.0, 40.0, 10.0, 1.0);
    private final BooleanSetting skipIfCart = new BooleanSetting(EncryptedString.of("Skip If Cart There"), true);
    private final BooleanSetting useFire = new BooleanSetting(EncryptedString.of("Place Fire"), true);
    private final BooleanSetting swapBack = new BooleanSetting(EncryptedString.of("Swap Back"), true);
    private int lastStart = Integer.MIN_VALUE / 2;
    private final ButtonSetting usage;

    private final ArrayDeque<Runnable> queue = new ArrayDeque<>();
    /** Where the head turns before each queued step (empty array = no turn). */
    private final ArrayDeque<float[]> turns = new ArrayDeque<>();
    private final BooleanSetting rotate = new BooleanSetting(EncryptedString.of("Rotate Head"), true);
    private final NumberSetting turnSpeed = new NumberSetting(EncryptedString.of("Turn Speed"), 10.0, 180.0, 45.0, 5.0);
    private final java.util.Random rng = new java.util.Random();
    private int nextAt;
    private int restoreSlot = -1;

    public XbowCartModule() {
        super(EncryptedString.of("Xbow Cart"), EncryptedString.of("Right-click a block with a loaded crossbow: rail, TNT minecart, fire and the shot, instantly. Needs rail, TNT minecart and flint and steel / fire charge / lava bucket in the hotbar."), -1, Category.COMBAT);
        this.stepDelay.setDescription(EncryptedString.of("0 = everything in the same tick (fastest). Higher = one step every N ticks."));
        this.messages.setDescription(EncryptedString.of("Tells you in the action bar why it could not cart."));
        this.usage = new ButtonSetting(EncryptedString.of("Anleitung"), () -> {
            if (this.mc.field_1724 != null) {
                this.mc.field_1724.method_7353(class_2561.method_43470("Xbow Cart: keep a loaded crossbow, a rail, a TNT minecart and flint and steel (or fire charge / lava bucket) in your hotbar. Press H to pick the loaded crossbow, look at a block within reach and right-click. The module places rail + cart, puts fire into the arrow's path and shoots through it."), false);
            }
        });
        this.usage.setDescription(EncryptedString.of("Shows requirements and usage in chat."));
        this.cooldown.setDescription(EncryptedString.of("Minimum ticks between two carts - stops double placing."));
        this.skipIfCart.setDescription(EncryptedString.of("Never places a second cart where one already stands."));
        this.useFire.setDescription(EncryptedString.of("Puts fire in the arrow's path (needed unless the crossbow has Flame-like ignition)."));
        this.swapBack.setDescription(EncryptedString.of("Switches back to the crossbow slot afterwards."));
        this.rotate.setDescription(EncryptedString.of("Really turns your head to each spot (smoothly) instead of only sending rotation packets - looks human, fewer anticheat flags."));
        this.turnSpeed.setDescription(EncryptedString.of("Degrees per tick the head turns (slightly randomised). Higher = faster."));
        hook.module.setting.SettingVisibility.set(this.turnSpeed, this.rotate::getValue);
        this.addSettings(this.rotate, this.turnSpeed, this.stepDelay, this.cooldown, this.skipIfCart, this.useFire, this.swapBack, this.messages, this.usage);
        instance = this;
        disableLegacy();
    }

    /** The old CartCore crossbow logic must stay off, this module replaces it. */
    private static void disableLegacy() {
        try {
            if (CartcoreCore.cartConfig.crossbowCarting) {
                CartcoreCore.cartConfig.crossbowCarting = false;
                CartcoreCore.cartConfig.save();
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onEnable() {
        disableLegacy();
        this.queue.clear();
        this.turns.clear();
    }

    @Override
    public void onDisable() {
        this.queue.clear();
        this.turns.clear();
        this.restoreSlot = -1;
    }

    @EventListener
    public void onTick(TickEvent event) {
        class_746 p = this.mc.field_1724;
        if (p == null) return;
        while (thetruetrident.cartcore.C.A.method_1436()) {
            int s = CartPlanner.hotbar(CartPlanner::loadedCrossbow);
            if (s >= 0) InventoryUtil.swap(s);
        }
        if (CartTest.ENABLED) CartTest.tick();
        if (!this.queue.isEmpty() && p.field_6012 >= this.nextAt) {
            float[] t = this.turns.isEmpty() ? new float[0] : this.turns.peek();
            if (this.rotate.getValue() && t.length == 2 && !this.turnToward(p, t)) return;
            if (!this.turns.isEmpty()) this.turns.poll();
            CartPlanner.REAL_ROTATE = this.rotate.getValue();
            try { this.queue.poll().run(); } finally { CartPlanner.REAL_ROTATE = false; }
            this.nextAt = p.field_6012 + this.stepDelay.getIntValue();
            if (this.queue.isEmpty()) this.finish();
        }
    }

    @EventListener
    public void onPacket(hook.event.events.PacketReceiveEvent event) {
        if (CartTest.ENABLED && event.packet instanceof net.minecraft.class_2664) CartTest.explosions++;
    }

    @EventListener
    public void onPreItemUse(PreItemUseEvent event) {
        if (!this.queue.isEmpty()) { event.cancel(); return; }
        // a held right-click fires this every tick: one cart per click, never two
        if (this.mc.field_1724 != null && this.mc.field_1724.field_6012 - this.lastStart < this.cooldown.getIntValue()
                && (CartPlanner.loadedCrossbow(this.mc.field_1724.method_6047()) || CartPlanner.loadedCrossbow(this.mc.field_1724.method_6079()))) { event.cancel(); return; }
        if (this.tryStart()) event.cancel();
    }

    /** @return true when the right-click was handled here (sequence started, or refused with a message). */
    boolean tryStart() {
        class_746 p = this.mc.field_1724;
        if (p == null || this.mc.field_1687 == null || this.mc.field_1761 == null) return false;
        class_1268 hand;
        if (CartPlanner.loadedCrossbow(p.method_6047())) hand = class_1268.field_5808;
        else if (CartPlanner.loadedCrossbow(p.method_6079())) hand = class_1268.field_5810;
        else return false;
        // only when you look at a block in reach; otherwise the crossbow shoots normally
        if (this.mc.field_1765 instanceof net.minecraft.class_3966) return false; // aiming at an entity: normal shot
        // fresh block raycast along your view (does not depend on the last rendered crosshair)
        if (!(p.method_5745(p.method_55754(), 1.0f, false) instanceof class_3965 bh) || bh.method_17783() != class_239.class_240.field_1332) return false;

        class_2338 clicked = bh.method_17777();
        class_2338 rail;
        class_3965 railClick;
        if (CartPlanner.isRailBlock(clicked)) {
            rail = clicked;
            railClick = null;
        } else {
            if (bh.method_17780() == class_2350.field_11033) return this.fail("can't put a rail under a block");
            rail = clicked.method_10093(bh.method_17780());
            railClick = bh;
        }
        if (CartPlanner.railSlot() < 0 && !CartPlanner.isRailBlock(rail)) return this.fail("no rail in hotbar");
        if (CartPlanner.cartSlot() < 0) return this.fail("no TNT minecart in hotbar");
        if (this.skipIfCart.getValue() && !this.mc.field_1687.method_8390(net.minecraft.class_1688.class, new net.minecraft.class_238(rail).method_1014(0.3), e -> e.method_5805()).isEmpty())
            return this.fail("there is already a cart");

        CartPlanner.Plan plan0 = new CartPlanner.Plan();
        if (!CartPlanner.prepareRail(plan0, rail, railClick)) return this.fail("can't place a rail there");

        // dense search over the whole cart box: every aim point whose arrow reaches the cart through a usable fire
        // cell; flat rail first, then ramp rails (cart half a block higher) so steep / odd angles work too
        class_243 start = new class_243(p.method_23317(), p.method_23320() - 0.1, p.method_23321());
        boolean found = false;
        java.util.List<Object[]> layouts = new java.util.ArrayList<>();
        layouts.add(new Object[]{plan0, CartPlanner.cartBox(rail)});
        CartPlanner.Plan plan = plan0;
        if (railClick != null) for (boolean pb : new boolean[]{false, true}) for (class_2350 d : CartPlanner.HORIZONTAL) {
            CartPlanner.Plan sp = new CartPlanner.Plan();
            if (CartPlanner.prepareSlope(sp, rail, d, pb)) layouts.add(new Object[]{sp, CartPlanner.cartBox(rail, true)});
        }
        outer:
        for (Object[] lay : layouts) {
            CartPlanner.Plan cand = (CartPlanner.Plan) lay[0];
            var box = (net.minecraft.class_238) lay[1];
            double cx = (box.field_1323 + box.field_1320) / 2, cz = (box.field_1321 + box.field_1324) / 2;
            double h = box.field_1325 - box.field_1322;
            for (double inset : new double[]{0.15, 0.06}) {
                CartPlanner.INSET = inset;
                for (int iy = 0; iy <= 12; iy++) {
                    double fy = h * (0.5 + ((iy % 2 == 0) ? 1 : -1) * ((iy + 1) / 2) * 0.075);
                    if (fy < 0.03 || fy > h - 0.03) continue;
                    for (int ix = 0; ix <= 12; ix++) for (int iz = 0; iz <= 12; iz++) {
                        double ox = ((ix % 2 == 0) ? 1 : -1) * ((ix + 1) / 2) * 0.07;
                        double oz = ((iz % 2 == 0) ? 1 : -1) * ((iz + 1) / 2) * 0.07;
                        class_243 point = new class_243(cx + ox, box.field_1322 + fy, cz + oz);
                        float[] a = CartPlanner.aimAt(start, point);
                        class_243 vel = CartPlanner.dir(a[0], a[1]).method_1021(3.15);
                        CartPlanner.Trace tr = CartPlanner.trace(start, vel, box);
                        if (!tr.hitsCart) continue;
                        if (cand.rampBlock != null && CartPlanner.pathEnters(tr, cand.rampBlock)) continue;
                        if (this.useFire.getValue() && !CartPlanner.chooseFire(cand, tr, false)) continue;
                        cand.aim = a;
                        plan = cand;
                        found = true;
                        break outer;
                    }
                }
            }
        }
        CartPlanner.INSET = 0.06;
        if (!found) return this.fail(CartPlanner.igniter() == null ? "no flint and steel / fire charge / lava bucket" : "no spot for fire between you and the cart, step back a bit");

        final CartPlanner.Plan fp = plan;
        if (CartTest.ENABLED) {
            CartTest.planFire = plan.fire;
            CartTest.planRail = plan.rail;
            CartTest.planInfo = "rail=" + plan.rail.method_23854() + " fire=" + (plan.fire == null ? "-" : plan.fire.method_23854()) + " aim=" + plan.aim[0] + "/" + plan.aim[1] + " railClick=" + (plan.railClick != null);
        }
        this.lastStart = p.field_6012;
        this.restoreSlot = this.swapBack.getValue() ? p.method_31548().method_67532() : -1;
        final class_1268 shootHand = hand;
        final int crossbowSlot = hand == class_1268.field_5808 ? p.method_31548().method_67532() : -1;
        this.queue.clear();
        this.turns.clear();
        net.minecraft.class_3965 firstClick = fp.rampBlockClick != null ? fp.rampBlockClick : fp.upperClick != null ? fp.upperClick : fp.railClick;
        this.queue.add(() -> CartPlanner.placeRail(fp));
        this.turns.add(firstClick != null ? angleTo(p, firstClick.method_17784()) : new float[0]);
        this.queue.add(() -> CartPlanner.placeCart(fp));
        this.turns.add(fp.cartClick != null ? angleTo(p, fp.cartClick.method_17784()) : new float[0]);
        if (plan.fire != null) {
            this.queue.add(() -> CartPlanner.placeFire(fp));
            this.turns.add(angleTo(p, new class_243(fp.fire.method_10263() + 0.5, fp.fire.method_10264(), fp.fire.method_10260() + 0.5)));
        }
        this.queue.add(() -> this.shoot(fp, shootHand, crossbowSlot));
        this.turns.add(new float[]{fp.aim[0], fp.aim[1]});
        this.nextAt = p.field_6012;
        if (this.stepDelay.getIntValue() == 0 && !this.rotate.getValue()) {
            while (!this.queue.isEmpty()) this.queue.poll().run();
            this.finish();
        }
        return true;
    }

    private void shoot(CartPlanner.Plan plan, class_1268 hand, int crossbowSlot) {
        class_746 p = this.mc.field_1724;
        if (crossbowSlot >= 0) InventoryUtil.swap(crossbowSlot);
        float yaw = p.method_36454(), pitch = p.method_36455();
        p.method_36456(plan.aim[0]);
        p.method_36457(plan.aim[1]);
        // the use packet carries this rotation; the camera is put straight back
        this.mc.field_1761.method_2919((class_1657) p, hand);
        p.method_36456(yaw);
        p.method_36457(pitch);
    }

    /** Turns the camera a step toward the target like a mouse flick that slows down at the end; true once there. */
    private boolean turnToward(class_746 p, float[] t) {
        float dy = net.minecraft.class_3532.method_15393(t[0] - p.method_36454());
        float dp = t[1] - p.method_36455();
        float far = Math.max(Math.abs(dy), Math.abs(dp));
        if (far < 1.0f) { p.method_36456(p.method_36454() + dy); p.method_36457(t[1]); return true; }
        float step = this.turnSpeed.getFloatValue() * (0.85f + 0.3f * this.rng.nextFloat());
        step = Math.max(Math.min(step, far * 0.75f + 1.5f), 1.0f); // ease out near the target
        float k = Math.min(1.0f, step / far);
        p.method_36456(p.method_36454() + dy * k);
        p.method_36457(net.minecraft.class_3532.method_15363(p.method_36455() + dp * k, -90.0f, 90.0f));
        if (k >= 1.0f) return true;
        return false;
    }

    private static float[] angleTo(class_746 p, class_243 at) {
        return CartPlanner.aimAt(p.method_33571(), at);
    }

    private void finish() {
        if (this.restoreSlot >= 0 && this.mc.field_1724 != null) InventoryUtil.swap(this.restoreSlot);
        this.restoreSlot = -1;
    }

    /** Shows why and swallows the click, so the loaded crossbow is not wasted. */
    private boolean fail(String why) {
        if (CartTest.ENABLED) CartTest.lastFail = why;
        if (this.messages.getValue() && this.mc.field_1724 != null)
            this.mc.field_1724.method_7353(class_2561.method_43470("§cXbow Cart: §f" + why), true);
        return true;
    }
}
