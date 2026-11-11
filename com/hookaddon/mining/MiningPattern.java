/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.mining;

public enum MiningPattern {
    LAYER_BY_LAYER("Layer by layer"),
    STRIP("Strip mining"),
    TUNNEL("Tunnel mining"),
    GRID("Grid traversal");

    public final String title;

    private MiningPattern(String title) {
        this.title = title;
    }
}

