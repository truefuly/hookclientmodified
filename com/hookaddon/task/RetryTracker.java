/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.task;

public final class RetryTracker {
    private int attempts;
    private long lastAttemptAt;

    public synchronized int attempts() {
        return this.attempts;
    }

    public synchronized long lastAttemptAt() {
        return this.lastAttemptAt;
    }

    public synchronized boolean allow(long nowMs, long cooldownMs, int maxAttempts) {
        if (maxAttempts > 0 && this.attempts >= maxAttempts) {
            return false;
        }
        return this.attempts == 0 || nowMs - this.lastAttemptAt >= cooldownMs;
    }

    public synchronized void failure(long nowMs) {
        ++this.attempts;
        this.lastAttemptAt = nowMs;
    }

    public synchronized void success() {
        this.attempts = 0;
        this.lastAttemptAt = 0L;
    }

    public synchronized void reset() {
        this.attempts = 0;
        this.lastAttemptAt = 0L;
    }

    public synchronized String toString() {
        return "RetryTracker{attempts=" + this.attempts + "}";
    }
}

