/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.task;

public enum ConflictPolicy {
    REJECT("Reject activation"),
    PREEMPT("Stop the other task");

    public final String title;

    private ConflictPolicy(String title) {
        this.title = title;
    }
}

