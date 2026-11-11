/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package com.hookaddon.spawner;

import hook.compat.meteorclient.utils.player.ChatUtils;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.class_310;

public final class RtpFallback {
    private static final class_310 mc = class_310.method_1551();
    private State state = State.IDLE;
    private long sentAtMs;
    private long lastAttemptAtMs;
    private long cooldownUntilMs;
    private int postTeleportTicks;
    private double beforeX;
    private double beforeY;
    private double beforeZ;
    private String beforeDimension;
    private int consecutiveFailures;
    private int totalAttempts;
    private int totalSuccesses;

    public State state() {
        return this.state;
    }

    public int consecutiveFailures() {
        return this.consecutiveFailures;
    }

    public int totalAttempts() {
        return this.totalAttempts;
    }

    public int totalSuccesses() {
        return this.totalSuccesses;
    }

    public long lastAttemptAtMs() {
        return this.lastAttemptAtMs;
    }

    public long nextAttemptAtMs() {
        return Math.max(this.cooldownUntilMs, this.lastAttemptAtMs);
    }

    public boolean isPaused() {
        return this.state == State.PAUSED;
    }

    public boolean request(String reason, Options options, Consumer<String> notify) {
        Object command;
        if (!options.enabled()) {
            return false;
        }
        if (this.state == State.PAUSED) {
            notify.accept("RTP fallback is paused after " + this.consecutiveFailures + " failures. Toggle the module off and on to retry.");
            return false;
        }
        if (this.state != State.IDLE) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now < this.cooldownUntilMs) {
            notify.accept("RTP is on cooldown for " + this.remainingSeconds(now) + "s (" + reason + ").");
            return false;
        }
        if (options.pauseAfterFailures() && this.consecutiveFailures >= Math.max(1, options.maxConsecutive())) {
            this.state = State.PAUSED;
            notify.accept("RTP paused after " + this.consecutiveFailures + " consecutive failures.");
            return false;
        }
        Object object = command = options.command() == null || options.command().isBlank() ? "/rtp" : options.command().trim();
        if (!((String)command).startsWith("/")) {
            command = "/" + (String)command;
        }
        ChatUtils.sendPlayerMsg((String)command);
        if (RtpFallback.mc.field_1724 != null) {
            this.beforeX = RtpFallback.mc.field_1724.method_23317();
            this.beforeY = RtpFallback.mc.field_1724.method_23318();
            this.beforeZ = RtpFallback.mc.field_1724.method_23321();
            this.beforeDimension = this.currentDimension();
        }
        this.sentAtMs = now;
        this.lastAttemptAtMs = now;
        ++this.totalAttempts;
        this.state = State.WAITING_CONFIRM;
        notify.accept("Sent '" + (String)command + "' (" + reason + "), waiting up to " + options.teleportTimeoutSeconds() + "s for the teleport...");
        return true;
    }

    public Event tick(Options options, Consumer<String> notify) {
        if (this.state == State.IDLE || this.state == State.PAUSED) {
            return Event.NONE;
        }
        if (RtpFallback.mc.field_1724 == null || RtpFallback.mc.field_1687 == null) {
            this.state = State.IDLE;
            return Event.NONE;
        }
        long now = System.currentTimeMillis();
        switch (this.state.ordinal()) {
            case 1: {
                if (this.teleported(options)) {
                    this.consecutiveFailures = 0;
                    ++this.totalSuccesses;
                    this.cooldownUntilMs = now + (long)options.retryDelaySeconds() * 1000L;
                    this.postTeleportTicks = Math.max(0, options.postDelayTicks());
                    this.state = State.POST_TELEPORT;
                    notify.accept("Teleport confirmed, waiting " + this.postTeleportTicks / 20 + "s before searching again.");
                    return Event.CONFIRMED;
                }
                if (now - this.sentAtMs <= (long)options.teleportTimeoutSeconds() * 1000L) break;
                this.fail(options, "no position change after " + options.teleportTimeoutSeconds() + "s - the command may be denied, on cooldown or unavailable here", notify);
                return Event.FAILED;
            }
            case 2: {
                if (this.postTeleportTicks-- > 0) break;
                this.state = State.IDLE;
                break;
            }
            case 3: {
                if (now < this.cooldownUntilMs) break;
                this.state = State.IDLE;
                break;
            }
            default: {
                return Event.NONE;
            }
        }
        return Event.NONE;
    }

    public void onChatMessage(String message, Options options, List<String> keywords, Consumer<String> notify) {
        if (this.state != State.WAITING_CONFIRM || message == null || message.isBlank()) {
            return;
        }
        if (keywords == null || keywords.isEmpty()) {
            return;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank() || !lower.contains(keyword.toLowerCase(Locale.ROOT))) continue;
            String snippet = message.length() > 120 ? message.substring(0, 120) + "..." : message;
            this.fail(options, "the server replied: \"" + snippet + "\"", notify);
            return;
        }
    }

    public void stop(Options options, Consumer<String> notify) {
        if (this.state == State.WAITING_CONFIRM) {
            this.lastAttemptAtMs = System.currentTimeMillis();
            this.cooldownUntilMs = this.lastAttemptAtMs + (long)options.retryDelaySeconds() * 1000L;
            notify.accept("RTP attempt cancelled, waiting " + options.retryDelaySeconds() + "s before retrying.");
        }
        this.state = State.IDLE;
    }

    public void reset() {
        this.state = State.IDLE;
        this.consecutiveFailures = 0;
    }

    public void clearCooldown() {
        this.cooldownUntilMs = 0L;
        this.state = State.IDLE;
        this.consecutiveFailures = 0;
    }

    public void restoreCounters(int consecutiveFailures, long lastAttemptAtMs) {
        this.consecutiveFailures = Math.max(0, consecutiveFailures);
        this.cooldownUntilMs = this.lastAttemptAtMs = Math.max(0L, lastAttemptAtMs);
    }

    private void fail(Options options, String reason, Consumer<String> notify) {
        long now;
        ++this.consecutiveFailures;
        this.lastAttemptAtMs = now = System.currentTimeMillis();
        this.cooldownUntilMs = now + (long)options.retryDelaySeconds() * 1000L;
        this.state = State.RETRY_WAIT;
        notify.accept("RTP attempt " + this.totalAttempts + " failed: " + reason + " (" + this.consecutiveFailures + "/" + options.maxConsecutive() + ")");
        if (options.pauseAfterFailures() && this.consecutiveFailures >= Math.max(1, options.maxConsecutive())) {
            this.state = State.PAUSED;
            notify.accept("Too many failed RTP attempts, pausing. Enable it again to continue.");
        }
    }

    private boolean teleported(Options options) {
        double dz;
        double dy;
        String dimension = this.currentDimension();
        if (this.beforeDimension != null && !this.beforeDimension.equals(dimension)) {
            return true;
        }
        double dx = RtpFallback.mc.field_1724.method_23317() - this.beforeX;
        return dx * dx + (dy = RtpFallback.mc.field_1724.method_23318() - this.beforeY) * dy + (dz = RtpFallback.mc.field_1724.method_23321() - this.beforeZ) * dz >= options.minDistance() * options.minDistance();
    }

    private String currentDimension() {
        if (RtpFallback.mc.field_1687 == null) {
            return "";
        }
        return RtpFallback.mc.field_1687.method_27983().method_29177().toString();
    }

    private int remainingSeconds(long now) {
        long remaining = this.cooldownUntilMs - now;
        return remaining <= 0L ? 0 : (int)Math.ceil((double)remaining / 1000.0);
    }

    public static enum State {
        IDLE,
        WAITING_CONFIRM,
        POST_TELEPORT,
        RETRY_WAIT,
        PAUSED;

    }

    public record Options(boolean enabled, String command, int retryDelaySeconds, int maxConsecutive, int teleportTimeoutSeconds, int postDelayTicks, boolean pauseAfterFailures, double minDistance) {
    }

    public static enum Event {
        NONE,
        SENT,
        CONFIRMED,
        FAILED,
        PAUSED;

    }
}

