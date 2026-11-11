/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.task;

import com.hookaddon.task.Capability;
import com.hookaddon.task.ConflictPolicy;
import com.hookaddon.task.RetryTracker;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class TaskManager {
    private static final int DEBUG_LOG_LIMIT = 250;
    private static final TaskManager INSTANCE = new TaskManager();
    private final Map<Capability, Claim> holders = new EnumMap<Capability, Claim>(Capability.class);
    private final Map<String, Runnable> stopHandlers = new LinkedHashMap<String, Runnable>();
    private final Map<String, RetryTracker> retries = new LinkedHashMap<String, RetryTracker>();
    private final Deque<String> debugLog = new ArrayDeque<String>();
    private static Consumer<String> logSink = message -> {};

    private TaskManager() {
    }

    public static TaskManager get() {
        return INSTANCE;
    }

    public static void setLogSink(Consumer<String> sink) {
        logSink = sink == null ? message -> {} : sink;
    }

    public synchronized ClaimResult claim(String owner, String title, Collection<Capability> capabilities, ConflictPolicy policy, Runnable stopHandler) {
        if (capabilities.isEmpty()) {
            return ClaimResult.success();
        }
        EnumSet<Capability> wanted = EnumSet.noneOf(Capability.class);
        wanted.addAll(capabilities);
        Set<Capability> conflicting = this.conflicts(wanted, owner);
        if (!conflicting.isEmpty()) {
            if (policy == ConflictPolicy.PREEMPT) {
                Map<String, String> preempted = this.preempt(conflicting, owner);
                conflicting = this.conflicts(wanted, owner);
                if (conflicting.isEmpty()) {
                    this.log(owner + " took over from " + String.join((CharSequence)", ", preempted.values()));
                }
            }
            if (!conflicting.isEmpty()) {
                Claim holder = this.holders.get((Object)conflicting.iterator().next());
                String holderTitle = holder == null ? "another task" : holder.title();
                return new ClaimResult(false, "Cannot start: " + TaskManager.describe(conflicting) + " is already used by " + holderTitle + ". Stop it first or set the module's conflict policy to '" + ConflictPolicy.PREEMPT.title + "'.");
            }
        }
        Claim claim = new Claim(owner, title, Set.copyOf(wanted));
        for (Capability capability : wanted) {
            this.holders.put(capability, claim);
        }
        if (stopHandler == null) {
            this.stopHandlers.remove(owner);
        } else {
            this.stopHandlers.put(owner, stopHandler);
        }
        this.log(owner + " claimed " + TaskManager.describe(wanted));
        return ClaimResult.success();
    }

    public synchronized void release(String owner) {
        EnumSet<Capability> released = EnumSet.noneOf(Capability.class);
        this.holders.entrySet().removeIf(entry -> {
            if (((Claim)entry.getValue()).owner().equals(owner)) {
                released.add((Capability)((Object)((Object)entry.getKey())));
                return true;
            }
            return false;
        });
        this.stopHandlers.remove(owner);
        RetryTracker tracker = this.retries.get(owner);
        if (tracker != null) {
            tracker.reset();
        }
        if (!released.isEmpty()) {
            this.log(owner + " released " + TaskManager.describe(released));
        }
    }

    public synchronized boolean owns(String owner, Capability capability) {
        Claim holder = this.holders.get((Object)capability);
        return holder != null && holder.owner().equals(owner);
    }

    public synchronized Optional<String> holderOf(Capability capability) {
        Claim holder = this.holders.get((Object)capability);
        return holder == null ? Optional.empty() : Optional.of(holder.owner());
    }

    public synchronized Optional<Claim> claimOf(String owner) {
        for (Claim claim : this.holders.values()) {
            if (!claim.owner().equals(owner)) continue;
            return Optional.of(claim);
        }
        return Optional.empty();
    }

    public synchronized boolean anyHeld() {
        return !this.holders.isEmpty();
    }

    public synchronized void releaseAll(String reason) {
        if (this.holders.isEmpty() && this.retries.isEmpty()) {
            return;
        }
        LinkedHashSet owners = new LinkedHashSet();
        this.holders.values().forEach(claim -> owners.add(claim.owner()));
        this.holders.clear();
        this.stopHandlers.clear();
        this.retries.clear();
        this.log("released all claims (" + reason + "): " + String.join((CharSequence)", ", owners));
    }

    public synchronized RetryTracker retries(String owner) {
        return this.retries.computeIfAbsent(owner, key -> new RetryTracker());
    }

    public static void debug(String message) {
        logSink.accept(message);
    }

    public synchronized List<String> debugLog() {
        return new ArrayList<String>(this.debugLog);
    }

    public synchronized void clearDebugLog() {
        this.debugLog.clear();
    }

    private Set<Capability> conflicts(Set<Capability> wanted, String owner) {
        EnumSet<Capability> conflicting = EnumSet.noneOf(Capability.class);
        for (Capability capability : wanted) {
            Claim holder = this.holders.get((Object)capability);
            if (holder == null || holder.owner().equals(owner)) continue;
            conflicting.add(capability);
        }
        return conflicting;
    }

    private Map<String, String> preempt(Set<Capability> conflicting, String newOwner) {
        LinkedHashMap<String, String> owners = new LinkedHashMap<String, String>();
        for (Capability capability : conflicting) {
            Claim holder = this.holders.get((Object)capability);
            if (holder == null) continue;
            owners.put(holder.owner(), holder.title());
        }
        for (Map.Entry entry : owners.entrySet()) {
            if (((String)entry.getKey()).equals(newOwner)) continue;
            Runnable stopHandler = this.stopHandlers.get(entry.getKey());
            if (stopHandler != null) {
                try {
                    stopHandler.run();
                }
                catch (RuntimeException e) {
                    logSink.accept("stop handler of " + (String)entry.getKey() + " failed: " + e.getMessage());
                }
            }
            this.release((String)entry.getKey());
        }
        return owners;
    }

    private static String describe(Collection<Capability> capabilities) {
        ArrayList<String> titles = new ArrayList<String>();
        for (Capability capability : capabilities) {
            titles.add(capability.title);
        }
        return String.join((CharSequence)", ", titles);
    }

    private void log(String message) {
        this.debugLog.addLast(message);
        while (this.debugLog.size() > 250) {
            this.debugLog.removeFirst();
        }
        logSink.accept(message);
    }

    public record ClaimResult(boolean ok, String message) {
        public static ClaimResult success() {
            return new ClaimResult(true, "ok");
        }
    }

    public record Claim(String owner, String title, Set<Capability> capabilities) {
    }
}

