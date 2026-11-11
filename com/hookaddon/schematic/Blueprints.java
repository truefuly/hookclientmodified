/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2680
 */
package com.hookaddon.schematic;

import com.hookaddon.schematic.Schematic;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2680;

public final class Blueprints {
    public static Blueprint find(String name) {
        if (name == null || name.isBlank()) {
            return Blueprint.NONE;
        }
        String needle = name.trim().toLowerCase().replace(' ', '_').replace('-', '_');
        for (Blueprint blueprint : Blueprint.values()) {
            if (!blueprint.name().toLowerCase().equals(needle)) continue;
            return blueprint;
        }
        for (Blueprint blueprint : Blueprint.values()) {
            if (!blueprint.title.toLowerCase().replace(' ', '_').equals(needle)) continue;
            return blueprint;
        }
        return Blueprint.NONE;
    }

    public static Schematic build(Blueprint blueprint, class_2248 material) {
        if (blueprint == null || blueprint == Blueprint.NONE) {
            throw new IllegalArgumentException("No blueprint selected");
        }
        class_2680 state = (material == null ? class_2246.field_10445 : material).method_9564();
        class_2680 air = class_2246.field_10124.method_9564();
        return switch (blueprint.ordinal()) {
            case 1 -> Blueprints.of(blueprint.title, 5, 1, 5, (x, y, z) -> true, state, air);
            case 2 -> Blueprints.of(blueprint.title, 9, 3, 1, (x, y, z) -> true, state, air);
            case 3 -> Blueprints.of(blueprint.title, 3, 8, 3, (x, y, z) -> true, state, air);
            case 4 -> Blueprints.of(blueprint.title, 7, 7, 3, (x, y, z) -> y == x, state, air);
            default -> throw new IllegalArgumentException("No geometry for " + blueprint.title);
        };
    }

    private static Schematic of(String title, int sizeX, int sizeY, int sizeZ, CellFilter filter, class_2680 state, class_2680 air) {
        class_2680[] states = new class_2680[sizeX * sizeY * sizeZ];
        for (int y = 0; y < sizeY; ++y) {
            for (int z = 0; z < sizeZ; ++z) {
                for (int x = 0; x < sizeX; ++x) {
                    states[x + z * sizeX + y * sizeX * sizeZ] = filter.allows(x, y, z) ? state : air;
                }
            }
        }
        Schematic.Region region = new Schematic.Region(title, 0, 0, 0, sizeX, sizeY, sizeZ, states);
        return new Schematic(title, "blueprint", "", 0, Map.of(), List.of(region));
    }

    public static enum Blueprint {
        NONE("None"),
        FLAT_PLATFORM_5X5("Flat Platform 5x5"),
        WALL_9X3("Wall 9x3"),
        TOWER_3X3X8("Tower 3x3x8"),
        STAIRCASE_7X3("Staircase 7x3");

        public final String title;

        private Blueprint(String title) {
            this.title = title;
        }
    }

    @FunctionalInterface
    private static interface CellFilter {
        public boolean allows(int var1, int var2, int var3);
    }
}

