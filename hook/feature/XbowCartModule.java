package hook.feature;

import hook.event.EventListener;
import hook.event.events.PreItemUseEvent;
import hook.event.events.Render3DEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ButtonSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.utils.InventoryUtil;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import thetruetrident.cartcore.CartcoreCore;

/**
 * Crossbow cart: right-click a block with a loaded crossbow and it places a rail, a TNT minecart on it, fire in the
 * arrow's way and fires the crossbow through the fire into the cart.
 *
 * Humanize (default): done like a player would - one thing after the other, the hotbar slot is picked a moment before
 * each click, the head moves there smoothly with real mouse steps, the click happens only when your crosshair is
 * really on the block (hit point where the crosshair is, not the exact centre), random human pauses between steps,
 * and the crossbow is aimed right before the shot at the cart that is really there. No extra rotation packets, never
 * two actions in the same tick, nothing outside vanilla reach.
 */
public final class XbowCartModule extends Module {
    public static XbowCartModule instance;
    private final BooleanSetting humanize = new BooleanSetting(EncryptedString.of("Humanize"), true);
    private final NumberSetting turnSpeed = new NumberSetting(EncryptedString.of("Speed %"), 50.0, 250.0, 100.0, 5.0);
    private final NumberSetting stepDelay = new NumberSetting(EncryptedString.of("Step Delay (ticks)"), 0.0, 5.0, 0.0, 1.0);
    private final BooleanSetting messages = new BooleanSetting(EncryptedString.of("Messages"), true);
    private final NumberSetting cooldown = new NumberSetting(EncryptedString.of("Cooldown (ticks)"), 0.0, 40.0, 10.0, 1.0);
    private final BooleanSetting skipIfCart = new BooleanSetting(EncryptedString.of("Skip If Cart There"), true);
    private final BooleanSetting useFire = new BooleanSetting(EncryptedString.of("Place Fire"), true);
    private final BooleanSetting swapBack = new BooleanSetting(EncryptedString.of("Swap Back"), true);
    private final ButtonSetting usage;
    private int lastStart = Integer.MIN_VALUE / 2;
    private int lastAge = Integer.MIN_VALUE;

    // fast mode (Humanize off)
    private final ArrayDeque<Runnable> queue = new ArrayDeque<>();
    private int nextAt;

    // humanized sequence
    private static final int CLICK = 0, BUCKET = 1, SHOOT = 2;

    private static final class Step {
        final int kind;
        final IntSupplier slot;
        final class_243 point;      // where the crosshair goes (clicks)
        final class_2338 block;     // block the crosshair has to be on
        final class_2350 face;      // and its face (null = any)
        final class_3965 fallback;  // planned click if the crosshair never lands exactly
        boolean started, preAimed;
        int startAge, clickAfter, searchedAt;
        float[] rot;

        Step(int kind, IntSupplier slot, class_243 point, class_2338 block, class_2350 face, class_3965 fallback) {
            this.kind = kind;
            this.slot = slot;
            this.point = point;
            this.block = block;
            this.face = face;
            this.fallback = fallback;
        }
    }

    private final List<Step> steps = new ArrayList<>();
    private int stepIdx, readyAt;
    private CartPlanner.Plan seqPlan;
    private class_1268 seqHand;
    private final CartHuman.Turner turner = new CartHuman.Turner();
    private int restoreSlot = -1, swapBackAt = -1, cartStillSince;
    private class_238 lastCart;

    public XbowCartModule() {
        super(EncryptedString.of("Xbow Cart"), EncryptedString.of("Right-click a block with a loaded crossbow: rail, TNT minecart, fire and the shot - done like a real player. Needs rail, TNT minecart and flint and steel / fire charge / lava bucket in the hotbar."), -1, Category.COMBAT);
        this.humanize.setDescription(EncryptedString.of("Does it like a human: smooth mouse-like head movement, slot picked before each click, clicks only with the crosshair on the block, random pauses, one action per tick. Off = instant (easy to flag)."));
        this.turnSpeed.setDescription(EncryptedString.of("How fast it all goes: head movement and slot timing. 100 = a fast player, higher = even faster (still smooth, one action per tick)."));
        this.stepDelay.setDescription(EncryptedString.of("Extra pause between the steps (randomised). 0 = like a very quick player."));
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
        this.swapBack.setDescription(EncryptedString.of("Switches back to the slot you had afterwards (after a short random pause)."));
        hook.module.setting.SettingVisibility.set(this.turnSpeed, this.humanize::getValue);
        this.addSettings(this.humanize, this.turnSpeed, this.stepDelay, this.cooldown, this.skipIfCart, this.useFire, this.swapBack, this.messages, this.usage);
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
        this.reset();
    }

    @Override
    public void onDisable() {
        this.reset();
    }

    private void reset() {
        this.queue.clear();
        this.steps.clear();
        this.turner.stop();
        this.restoreSlot = -1;
        this.swapBackAt = -1;
    }

    private boolean busy() {
        return !this.queue.isEmpty() || !this.steps.isEmpty();
    }

    @EventListener
    public void onTick(TickEvent event) {
        class_746 p = this.mc.field_1724;
        if (p == null) return;
        if (CartTest.ENABLED && this.mc.field_1755 instanceof net.minecraft.class_433) this.mc.method_1507(null); // self-test: never sit paused
        if (p.field_6012 == this.lastAge) return; // the tick event fires twice per game tick
        this.lastAge = p.field_6012;
        while (thetruetrident.cartcore.C.A.method_1436()) {
            int s = CartPlanner.hotbar(CartPlanner::loadedCrossbow);
            if (s >= 0) InventoryUtil.swap(s);
        }
        if (CartTest.ENABLED) CartTest.tick();
        if (this.swapBackAt >= 0 && p.field_6012 >= this.swapBackAt && this.steps.isEmpty()) {
            if (!p.method_6115() && this.restoreSlot >= 0) InventoryUtil.swap(this.restoreSlot);
            this.swapBackAt = -1;
            this.restoreSlot = -1;
        }
        if (!this.steps.isEmpty()) {
            this.turner.tickFallback(p);
            try {
                this.runStep(p);
            } catch (Throwable t) {
                this.abort("error: " + t);
            }
            return;
        }
        if (!this.queue.isEmpty() && p.field_6012 >= this.nextAt) {
            this.queue.poll().run();
            this.nextAt = p.field_6012 + this.stepDelay.getIntValue();
            if (this.queue.isEmpty()) this.finish(0);
        }
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (!this.steps.isEmpty()) this.turner.frame(this.mc.field_1724);
    }

    @EventListener
    public void onPacket(hook.event.events.PacketReceiveEvent event) {
        if (CartTest.ENABLED && event.packet instanceof net.minecraft.class_2664) CartTest.explosions++;
    }

    @EventListener
    public void onPreItemUse(PreItemUseEvent event) {
        if (this.busy()) { event.cancel(); return; }
        // a held right-click fires this every tick: one cart per click, never two
        if (this.mc.field_1724 != null && this.mc.field_1724.field_6012 - this.lastStart < this.cooldown.getIntValue()
                && (CartPlanner.loadedCrossbow(this.mc.field_1724.method_6047()) || CartPlanner.loadedCrossbow(this.mc.field_1724.method_6079()))) { event.cancel(); return; }
        if (this.tryStart()) event.cancel();
    }

    /** @return true when the right-click was handled here (sequence started, or refused with a message). */
    boolean tryStart() {
        class_746 p = this.mc.field_1724;
        if (p == null || this.mc.field_1687 == null || this.mc.field_1761 == null) return false;
        if (this.busy()) return true;
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
        if (this.skipIfCart.getValue() && !this.mc.field_1687.method_8390(net.minecraft.class_1688.class, new class_238(rail).method_1014(0.3), e -> e.method_5805()).isEmpty())
            return this.fail("there is already a cart");

        CartPlanner.Plan plan0 = new CartPlanner.Plan();
        if (!CartPlanner.prepareRail(plan0, rail, railClick)) return this.fail(CartPlanner.why);

        // dense search over the whole cart box: every aim point whose arrow reaches the cart through a usable fire
        // cell; flat rail first, then ramp rails (cart half a block higher) so steep / odd angles work too
        class_243 start = new class_243(p.method_23317(), p.method_23320() - 0.1, p.method_23321());
        List<Object[]> layouts = new ArrayList<>();
        layouts.add(new Object[]{plan0, CartPlanner.cartBox(rail)});
        if (railClick != null) for (boolean pb : new boolean[]{false, true}) for (class_2350 d : CartPlanner.HORIZONTAL) {
            CartPlanner.Plan sp = new CartPlanner.Plan();
            if (CartPlanner.prepareSlope(sp, rail, d, pb)) layouts.add(new Object[]{sp, CartPlanner.cartBox(rail, true)});
        }
        CartPlanner.Plan plan = null;
        for (Object[] lay : layouts) {
            CartPlanner.Plan cand = (CartPlanner.Plan) lay[0];
            float[] a = CartPlanner.searchAim(start, (class_238) lay[1], tr ->
                (cand.rampBlock == null || !CartPlanner.pathEnters(tr, cand.rampBlock)) && (!this.useFire.getValue() || CartPlanner.chooseFire(cand, tr, false)));
            if (a != null) {
                cand.aim = a;
                plan = cand;
                break;
            }
        }
        if (plan == null) return this.fail(CartPlanner.igniter() == null ? "no flint and steel / fire charge / lava bucket" : "no spot for fire between you and the cart, step back a bit");

        final CartPlanner.Plan fp = plan;
        if (CartTest.ENABLED) {
            CartTest.planFire = plan.fire;
            CartTest.planRail = plan.rail;
            CartTest.planInfo = "rail=" + plan.rail.method_23854() + " fire=" + (plan.fire == null ? "-" : plan.fire.method_23854()) + " aim=" + plan.aim[0] + "/" + plan.aim[1] + " railClick=" + (plan.railClick != null);
        }
        this.lastStart = p.field_6012;
        this.restoreSlot = this.swapBack.getValue() ? p.method_31548().method_67532() : -1;
        this.swapBackAt = -1;
        final class_1268 shootHand = hand;
        final int crossbowSlot = hand == class_1268.field_5808 ? p.method_31548().method_67532() : -1;

        if (this.humanize.getValue()) {
            this.buildSteps(fp, shootHand, crossbowSlot);
            return true;
        }
        this.queue.clear();
        this.queue.add(() -> CartPlanner.placeRail(fp));
        this.queue.add(() -> CartPlanner.placeCart(fp));
        if (plan.fire != null) this.queue.add(() -> CartPlanner.placeFire(fp));
        this.queue.add(() -> this.shoot(fp, shootHand, crossbowSlot));
        this.nextAt = p.field_6012;
        if (this.stepDelay.getIntValue() == 0) {
            while (!this.queue.isEmpty()) this.queue.poll().run();
            this.finish(0);
        }
        return true;
    }

    // ---------------------------------------------------------------- humanized sequence

    private void buildSteps(CartPlanner.Plan plan, class_1268 hand, int crossbowSlot) {
        this.steps.clear();
        this.stepIdx = 0;
        this.seqPlan = plan;
        this.seqHand = hand;
        this.lastCart = null;
        if (plan.rampBlockClick != null) this.steps.add(click(() -> plan.rampBlockSlot, plan.rampBlockClick));
        if (plan.upperClick != null) this.steps.add(click(CartPlanner::railSlot, plan.upperClick));
        if (plan.railClick != null) this.steps.add(click(CartPlanner::railSlot, plan.railClick));
        // the cart goes on the rail: aim at the rail's top (flat rail 2 px high, ascending rail half a block)
        class_2338 r = plan.rail;
        double top = plan.slope ? 0.5 : 0.125;
        class_243 cartPoint = new class_243(r.method_10263() + 0.5 + CartHuman.jitter(0.28), r.method_10264() + top, r.method_10260() + 0.5 + CartHuman.jitter(0.28));
        // near the edge of your reach the middle of the rail may be too far: aim at the near part of the rail instead
        class_243 eyeNow = this.mc.field_1724.method_33571();
        if (eyeNow.method_1022(cartPoint) > this.mc.field_1724.method_55754() - 0.1) cartPoint = CartPlanner.nearTop(r, r.method_10264() + top, eyeNow, 0.2);
        this.steps.add(new Step(CLICK, CartPlanner::cartSlot, cartPoint, r, null, plan.cartClick));
        if (plan.fire != null) {
            if (plan.supportClick != null) this.steps.add(click(() -> plan.supportSlot, plan.supportClick));
            class_2338 below = plan.fire.method_10074();
            class_3965 fireClick = new class_3965(new class_243(plan.fire.method_10263() + 0.5, plan.fire.method_10264(), plan.fire.method_10260() + 0.5), class_2350.field_11036, below, false);
            this.steps.add(new Step(plan.igniterIsBucket ? BUCKET : CLICK, () -> plan.igniterSlot, CartHuman.facePoint(below, class_2350.field_11036, 0.26), below, class_2350.field_11036, fireClick));
        }
        this.steps.add(new Step(SHOOT, () -> crossbowSlot, null, null, null, null));
        this.readyAt = this.mc.field_1724.field_6012; // first step right away (you just clicked)
    }

    private static Step click(IntSupplier slot, class_3965 hit) {
        class_243 aim = CartHuman.facePoint(hit.method_17777(), hit.method_17780(), 0.28);
        // the middle of the face can be out of reach while its near part is not: then aim at the planned (near) point
        net.minecraft.class_746 p = net.minecraft.class_310.method_1551().field_1724;
        if (p != null && p.method_33571().method_1022(aim) > p.method_55754() - 0.1) aim = hit.method_17784();
        return new Step(CLICK, slot, aim, hit.method_17777(), hit.method_17780(), hit);
    }

    private void runStep(class_746 p) {
        if (this.mc.field_1755 != null || !p.method_5805()) { this.abort(null); return; }
        int age = p.field_6012;
        if (age < this.readyAt) return;
        Step s = this.steps.get(this.stepIdx);
        if (!s.started) {
            s.started = true;
            s.startAge = age;
            int slot = s.slot.getAsInt();
            if (s.kind != SHOOT && slot < 0) { this.abort("item missing from the hotbar"); return; }
            boolean swapped = false;
            if (slot >= 0 && slot != p.method_31548().method_67532()) {
                InventoryUtil.swap(slot);
                swapped = true;
            }
            // a real player needs a moment between the hotbar key and the click
            // fast players press the slot key and right-click almost together (vanilla handles both in the same tick)
            s.clickAfter = age + (swapped ? CartHuman.ticks(0.45 / this.mul(), 0.4, 0) : 0);
            if (s.kind == SHOOT) {
                s.rot = this.shotAim(p);
                s.searchedAt = age;
                if (s.rot == null) s.rot = new float[]{this.seqPlan.aim[0], this.seqPlan.aim[1]};
            } else {
                s.rot = CartPlanner.aimAt(p.method_33571(), s.point);
            }
            if (!s.preAimed) this.turner.aim(p, s.rot[0], s.rot[1], 110.0 * this.mul(), true);
            else this.turner.retarget(s.rot[0], s.rot[1]);
        }
        if (age < s.clickAfter) return;
        int waited = age - s.startAge;
        double dist = this.turner.dist(p);

        if (s.kind == SHOOT) {
            if (!CartPlanner.loadedCrossbow(p.method_5998(this.seqHand))) { this.abort("crossbow not loaded any more"); return; }
            class_238 cart = this.cartBox();
            if (cart == null) {
                if (waited > 60) this.abort("the cart did not appear"); // high ping: the cart shows up late
                return;
            }
            // the cart must stand still (on a ramp it may still slide / its position may still be interpolating)
            if (this.lastCart == null || this.lastCart.method_1005().method_1022(cart.method_1005()) > 0.01) {
                this.lastCart = cart;
                this.cartStillSince = age;
            }
            if (age - this.cartStillSince < 1 && waited < 60) return;
            // what the server will use is your real rotation right now: it has to hit the cart through the fire
            class_243 start = new class_243(p.method_23317(), p.method_23320() - 0.1, p.method_23321());
            CartPlanner.Trace tr = CartPlanner.trace(start, CartPlanner.dir(p.method_36454(), p.method_36455()).method_1021(3.15), cart);
            boolean good = tr.hitsCart && (!this.useFire.getValue() || CartPlanner.passesFire(tr));
            if (good && dist < 1.0) {
                this.mc.field_1761.method_2919((class_1657) p, this.seqHand);
                if (CartTest.ENABLED) CartTest.shot();
                this.next(age);
                return;
            }
            // very high sensitivity: one mouse step is bigger than the window that hits - land the last bit exactly
            if (!good && dist <= CartHuman.gcd() * 0.75 + 0.05 && waited > 2) {
                p.method_36456(p.method_36454() + net.minecraft.class_3532.method_15393(s.rot[0] - p.method_36454()));
                p.method_36457(s.rot[1]);
                return;
            }
            // arrived but it would miss (you moved, the cart sits a bit different): aim again
            if (dist < 0.2 && age - s.searchedAt >= 4) {
                s.searchedAt = age;
                float[] a = this.shotAim(p);
                if (a != null) {
                    s.rot = a;
                    this.turner.retarget(a[0], a[1]);
                }
            }
            if (waited > 75) this.abort("no clear shot at the cart");
            return;
        }

        // the click: only when the crosshair is really on the block (hit point = where the crosshair is)
        class_239 ch = p.method_5745(p.method_55754(), 1.0f, false);
        boolean on = ch instanceof class_3965 bh && bh.method_17783() == class_239.class_240.field_1332
            && bh.method_17777().equals(s.block) && (s.face == null || bh.method_17780() == s.face);
        class_3965 hit = null;
        if (on && dist < 3.0) hit = (class_3965) ch; // fast players click as soon as the crosshair is on it
        else if (waited > 14 && dist < 3.0) hit = s.fallback; // crosshair never landed exactly (odd shapes): planned click
        else if (waited > 45) { this.abort("could not reach the spot"); return; }
        if (hit == null) return;
        if (s.kind == BUCKET) {
            if (this.mc.field_1761.method_2919((class_1657) p, class_1268.field_5808).method_23665()) p.method_6104(class_1268.field_5808);
        } else {
            CartPlanner.useOn(hit);
        }
        this.next(age);
    }

    private void next(int age) {
        if (CartTest.ENABLED) {
            Step s = this.steps.get(this.stepIdx);
            CartTest.stepLog = (CartTest.stepLog == null ? "" : CartTest.stepLog + " ") + "k" + s.kind + ":" + (age - s.startAge) + "t/fps" + this.mc.method_47599();
        }
        this.stepIdx++;
        if (this.stepIdx >= this.steps.size()) {
            this.steps.clear();
            this.turner.stop();
            this.finish(CartHuman.ticks(2.0 + this.stepDelay.getValue(), 0.8, 1));
            return;
        }
        // next action starts the tick after (slot + flick overlap like a fast player), never two actions in one tick
        this.readyAt = age + 1 + (this.stepDelay.getValue() > 0 ? CartHuman.ticks(this.stepDelay.getValue(), 0.5, 0) : 0);
        // the eyes go to the next spot right away (the hand follows next tick), like a fast player
        Step n = this.steps.get(this.stepIdx);
        class_746 p = this.mc.field_1724;
        if (p != null) {
            float[] r = n.kind == SHOOT ? new float[]{this.seqPlan.aim[0], this.seqPlan.aim[1]} : CartPlanner.aimAt(p.method_33571(), n.point);
            this.turner.aim(p, r[0], r[1], 110.0 * this.mul(), true);
            n.preAimed = true;
        }
    }

    private double mul() {
        return this.turnSpeed.getValue() / 100.0;
    }

    /** The TNT minecart that is really standing on the rail now, or null. */
    private class_238 cartBox() {
        var list = this.mc.field_1687.method_8390(net.minecraft.class_1688.class, new class_238(this.seqPlan.rail).method_1014(0.6), e -> e.method_5805());
        return list.isEmpty() ? null : list.get(0).method_5829();
    }

    /** Aim at the cart that is there now, from where you stand now (you may have moved during the sequence). */
    private float[] shotAim(class_746 p) {
        class_238 box = this.cartBox();
        if (box == null) box = CartPlanner.cartBox(this.seqPlan.rail, this.seqPlan.slope);
        class_243 start = new class_243(p.method_23317(), p.method_23320() - 0.1, p.method_23321());
        boolean needFire = this.useFire.getValue() && (this.seqPlan.fire == null || CartPlanner.isFire(this.seqPlan.fire));
        return CartPlanner.searchAim(start, box, tr -> !needFire || CartPlanner.passesFire(tr));
    }

    private void abort(String why) {
        this.steps.clear();
        this.turner.stop();
        if (why != null) this.fail(why);
        this.finish(CartHuman.ticks(3.0, 1.0, 2));
    }

    // ---------------------------------------------------------------- fast mode

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

    private void finish(int delay) {
        if (this.restoreSlot < 0 || this.mc.field_1724 == null) return;
        if (delay <= 0) {
            InventoryUtil.swap(this.restoreSlot);
            this.restoreSlot = -1;
        } else {
            this.swapBackAt = this.mc.field_1724.field_6012 + delay;
        }
    }

    /** Shows why and swallows the click, so the loaded crossbow is not wasted. */
    private boolean fail(String why) {
        if (CartTest.ENABLED) CartTest.lastFail = why;
        if (this.messages.getValue() && this.mc.field_1724 != null)
            this.mc.field_1724.method_7353(class_2561.method_43470("§cXbow Cart: §f" + why), true);
        return true;
    }
}
