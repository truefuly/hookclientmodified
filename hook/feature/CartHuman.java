package hook.feature;

import java.util.Random;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;

/**
 * Human-like input for the cart modules.
 *
 * Everything goes the same way real mouse input goes: the head is turned through Entity.turn in whole mouse counts
 * (so every rotation change is a multiple of your sensitivity step, like a real mouse), never with extra rotation
 * packets - the normal movement packet of the tick carries the new rotation, exactly as in vanilla. Turns are smooth
 * per frame: they speed up, slow down near the target, follow a slight arc, carry a little hand tremor and sometimes
 * overshoot a long flick by a hair and correct back. Delays are random (gaussian), never two things at once.
 */
final class CartHuman {
    static final Random RNG = new Random();

    private CartHuman() {
    }

    static class_310 mc() { return class_310.method_1551(); }

    /** Rotation of one mouse count with your sensitivity (vanilla MouseHandler: (s * 0.6 + 0.2)^3 * 8 * 0.15). */
    static double gcd() {
        double s = 0.5;
        try {
            Object v = mc().field_1690.method_42495().method_41753();
            if (v instanceof Double d) s = d;
        } catch (Throwable ignored) {
        }
        double f = s * 0.6 + 0.2;
        return Math.max(1.0e-4, f * f * f * 8.0 * 0.15);
    }

    static double gauss(double mean, double sd, double min, double max) {
        return Math.max(min, Math.min(max, mean + RNG.nextGaussian() * sd));
    }

    /** Random delay in ticks, at least {@code min}. */
    static int ticks(double mean, double sd, int min) {
        return (int) Math.max(min, Math.round(mean + RNG.nextGaussian() * sd));
    }

    /** Offset for an aim point on a face: mostly near the middle, never at the edge. */
    static double jitter(double amount) {
        return Math.max(-amount, Math.min(amount, RNG.nextGaussian() * amount * 0.45));
    }

    /** Turns the head by whole mouse counts (like the mouse does); returns what was really applied {yaw, pitch}. */
    static double[] turnBy(class_746 p, double dYaw, double dPitch) {
        double g = gcd();
        long cy = Math.round(dYaw / g), cp = Math.round(dPitch / g);
        if (cy == 0 && cp == 0) return new double[]{0, 0};
        float before = p.method_36455();
        p.method_5872(cy * g / 0.15, cp * g / 0.15);
        return new double[]{cy * g, p.method_36455() - before};
    }

    /** One instant flick to a rotation (whole mouse counts, lands within half a count). */
    static void turnTo(class_746 p, float yaw, float pitch) {
        turnBy(p, class_3532.method_15393(yaw - p.method_36454()), pitch - p.method_36455());
    }

    static void turnTo(class_746 p, float[] rot) { turnTo(p, rot[0], rot[1]); }

    static double angle(class_746 p, float yaw, float pitch) {
        double dy = class_3532.method_15393(yaw - p.method_36454()), dp = pitch - p.method_36455();
        return Math.sqrt(dy * dy + dp * dp);
    }

    /** Smooth, frame-based head movement toward a rotation. */
    static final class Turner {
        private boolean active;
        private float tYaw, tPitch;
        private boolean waypoint;
        private float wYaw, wPitch;
        private double vel, curve, startDist, maxSpeed, gain;
        private double resYaw, resPitch;
        private long lastNs;
        long lastFrameNs;

        boolean active() { return this.active; }

        void stop() {
            this.active = false;
            this.vel = 0;
        }

        /** @param speed rough top speed in degrees per tick */
        void aim(class_746 p, float yaw, float pitch, double speed, boolean mayOvershoot) {
            boolean fresh = !this.active;
            this.active = true;
            this.tYaw = yaw;
            this.tPitch = class_3532.method_15363(pitch, -90.0f, 90.0f);
            double dy = class_3532.method_15393(yaw - p.method_36454()), dp = this.tPitch - p.method_36455();
            this.startDist = Math.sqrt(dy * dy + dp * dp);
            this.waypoint = false;
            if (mayOvershoot && this.startDist > 20 && RNG.nextFloat() < 0.4f) {
                double over = Math.min(3.2, this.startDist * gauss(0.045, 0.015, 0.015, 0.08));
                this.wYaw = (float) (yaw + dy / this.startDist * over);
                this.wPitch = class_3532.method_15363((float) (this.tPitch + dp / this.startDist * over), -90.0f, 90.0f);
                this.waypoint = true;
            }
            this.curve = gauss(0, 0.11, -0.24, 0.24);
            this.maxSpeed = speed * 20.0 * gauss(1.0, 0.1, 0.8, 1.2);
            this.gain = gauss(26.0, 3.0, 19.0, 34.0) * Math.sqrt(Math.max(0.5, speed / 110.0));
            if (fresh) {
                this.vel = 0;
                this.lastNs = 0;
            }
        }

        /** Keeps the same target but moves it (e.g. the player walked a bit), without restarting the motion. */
        void retarget(float yaw, float pitch) {
            this.tYaw = yaw;
            this.tPitch = class_3532.method_15363(pitch, -90.0f, 90.0f);
            this.waypoint = false;
        }

        /** Angle left to the final target. */
        double dist(class_746 p) {
            return this.active ? angle(p, this.tYaw, this.tPitch) : 0.0;
        }

        /** Called every rendered frame (and from the tick when no frame was drawn). */
        void frame(class_746 p) {
            long now = System.nanoTime();
            this.lastFrameNs = now;
            if (!this.active || p == null) return;
            double dt = this.lastNs == 0 ? 1.0 / 60.0 : (now - this.lastNs) / 1.0e9;
            this.lastNs = now;
            dt = Math.max(0.001, Math.min(0.1, dt)); // low FPS: bigger steps, same speed
            float gy = this.waypoint ? this.wYaw : this.tYaw, gp = this.waypoint ? this.wPitch : this.tPitch;
            double dy = class_3532.method_15393(gy - p.method_36454()), dp = gp - p.method_36455();
            double d = Math.sqrt(dy * dy + dp * dp);
            double g = gcd();
            if (this.waypoint && d < Math.max(0.5, g)) {
                this.waypoint = false; // overshot a little: now settle back on the real spot
                this.vel *= 0.35;
                return;
            }
            if (d < g * 0.6) {
                this.vel = 0;
                return;
            }
            // ease in (velocity builds up), ease out (proportional to what is left), capped at the top speed
            double want = Math.min(this.maxSpeed, Math.max(d * this.gain, 4.0));
            this.vel += (want - this.vel) * Math.min(1.0, dt * 24.0);
            double step = Math.min(d, this.vel * dt);
            double ux = dy / d, uy = dp / d;
            // slight arc that fades out near the target, plus a tiny tremor while moving
            double arc = this.curve * step * Math.min(1.0, d / Math.max(1.0, this.startDist));
            double mx = ux * step - uy * arc, my = uy * step + ux * arc;
            if (d > 1.5) {
                double tremor = 0.02 * Math.sqrt(dt * 60.0);
                mx += RNG.nextGaussian() * tremor;
                my += RNG.nextGaussian() * tremor;
            }
            this.resYaw += mx;
            this.resPitch += my;
            double[] done = turnBy(p, this.resYaw, this.resPitch);
            this.resYaw -= done[0];
            this.resPitch -= done[1];
            if (Math.abs(this.resPitch) > 5) this.resPitch = 0; // clamped at straight up / down
        }

        /** Tick fallback: if no frame was drawn for a while (minimised...), move one tick's worth. */
        void tickFallback(class_746 p) {
            if (System.nanoTime() - this.lastFrameNs > 120_000_000L) {
                this.lastNs = System.nanoTime() - 50_000_000L;
                this.frame(p);
            }
        }
    }

    /** Point on a block face, nudged randomly away from the middle (never near the edge). */
    static class_243 facePoint(net.minecraft.class_2338 pos, net.minecraft.class_2350 face, double amount) {
        double x = pos.method_10263() + 0.5 + face.method_10148() * 0.5;
        double y = pos.method_10264() + 0.5 + face.method_10164() * 0.5;
        double z = pos.method_10260() + 0.5 + face.method_10165() * 0.5;
        if (face.method_10148() == 0) x += jitter(amount);
        if (face.method_10164() == 0) y += jitter(amount);
        if (face.method_10165() == 0) z += jitter(amount);
        return new class_243(x, y, z);
    }
}
