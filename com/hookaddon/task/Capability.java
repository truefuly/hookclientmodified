/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.task;

public enum Capability {
    MOVE("movement"),
    MINE("mining"),
    INTERACT("block interaction");

    public final String title;

    private Capability(String title) {
        this.title = title;
    }
}

