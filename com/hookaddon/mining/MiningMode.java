/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.mining;

public enum MiningMode {
    AREA("Fixed area"),
    FINDER("Auto target"),
    BORE("3x3 bore");

    public final String title;

    private MiningMode(String title) {
        this.title = title;
    }
}

