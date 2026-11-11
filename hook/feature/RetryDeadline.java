/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import java.util.random.RandomGenerator;

public final class RetryDeadline {
    public static final long MIN_DELAY_NANOS = 30000000000L;
    public static final long MAX_DELAY_NANOS = 120000000000L;
    private long deadline;
    private boolean armed;

    public void arm(long now, RandomGenerator random) {
        this.deadline = now + random.nextLong(30000000000L, 120000000001L);
        this.armed = true;
    }

    public void retry(long now) {
        this.deadline = now + 1000000000L;
        this.armed = true;
    }

    public boolean isArmed() {
        return this.armed;
    }

    public boolean due(long now) {
        return this.armed && now - this.deadline >= 0L;
    }

    public void clear() {
        this.armed = false;
    }
}

