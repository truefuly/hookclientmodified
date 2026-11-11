/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 */
package com.hookaddon.schematic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;

public final class Schematic {
    public final String name;
    public final String format;
    public final String filePath;
    public final int dataVersion;
    public final Map<String, String> metadata;
    private final List<Region> regions;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    private int solidBlocks = -1;

    public Schematic(String name, String format, String filePath, int dataVersion, Map<String, String> metadata, List<Region> regions) {
        this.name = name;
        this.format = format;
        this.filePath = filePath;
        this.dataVersion = dataVersion;
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<String, String>(metadata));
        this.regions = List.copyOf(regions);
        this.recomputeBounds();
    }

    private void recomputeBounds() {
        if (this.regions.isEmpty()) {
            this.maxZ = 0;
            this.maxY = 0;
            this.maxX = 0;
            this.minZ = 0;
            this.minY = 0;
            this.minX = 0;
            return;
        }
        this.minZ = Integer.MAX_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.minX = Integer.MAX_VALUE;
        this.maxZ = Integer.MIN_VALUE;
        this.maxY = Integer.MIN_VALUE;
        this.maxX = Integer.MIN_VALUE;
        for (Region region : this.regions) {
            this.minX = Math.min(this.minX, region.offsetX());
            this.minY = Math.min(this.minY, region.offsetY());
            this.minZ = Math.min(this.minZ, region.offsetZ());
            this.maxX = Math.max(this.maxX, region.offsetX() + region.sizeX());
            this.maxY = Math.max(this.maxY, region.offsetY() + region.sizeY());
            this.maxZ = Math.max(this.maxZ, region.offsetZ() + region.sizeZ());
        }
    }

    public List<Region> regions() {
        return this.regions;
    }

    public class_2338 minCorner() {
        return new class_2338(this.minX, this.minY, this.minZ);
    }

    public int sizeX() {
        return Math.max(0, this.maxX - this.minX);
    }

    public int sizeY() {
        return Math.max(0, this.maxY - this.minY);
    }

    public int sizeZ() {
        return Math.max(0, this.maxZ - this.minZ);
    }

    public int volume() {
        return this.sizeX() * this.sizeY() * this.sizeZ();
    }

    public int solidBlockCount() {
        if (this.solidBlocks >= 0) {
            return this.solidBlocks;
        }
        int total = 0;
        for (Region region : this.regions) {
            for (class_2680 state : region.states()) {
                if (state.method_26215()) continue;
                ++total;
            }
        }
        this.solidBlocks = total;
        return total;
    }

    public List<Entry> entries() {
        ArrayList<Entry> entries = new ArrayList<Entry>(this.solidBlockCount());
        for (Region region : this.regions) {
            for (int y = 0; y < region.sizeY(); ++y) {
                for (int z = 0; z < region.sizeZ(); ++z) {
                    for (int x = 0; x < region.sizeX(); ++x) {
                        class_2680 state = region.stateAt(x, y, z);
                        if (state.method_26215()) continue;
                        entries.add(new Entry(region.offsetX() + x, region.offsetY() + y, region.offsetZ() + z, state));
                    }
                }
            }
        }
        return entries;
    }

    public Map<class_2248, Integer> requiredBlocks() {
        LinkedHashMap<class_2248, Integer> required = new LinkedHashMap<class_2248, Integer>();
        for (Region region : this.regions) {
            for (class_2680 state : region.states()) {
                if (state.method_26215()) continue;
                required.merge(state.method_26204(), 1, Integer::sum);
            }
        }
        return required;
    }

    public record Region(String name, int offsetX, int offsetY, int offsetZ, int sizeX, int sizeY, int sizeZ, class_2680[] states) {
        public int volume() {
            return this.sizeX * this.sizeY * this.sizeZ;
        }

        public class_2680 stateAt(int localX, int localY, int localZ) {
            return this.states[localX + localZ * this.sizeX + localY * this.sizeX * this.sizeZ];
        }
    }

    public record Entry(int x, int y, int z, class_2680 state) {
        public class_2338 relativePos() {
            return new class_2338(this.x, this.y, this.z);
        }
    }
}

