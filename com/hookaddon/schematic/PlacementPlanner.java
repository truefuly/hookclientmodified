/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 */
package com.hookaddon.schematic;

import com.hookaddon.schematic.Schematic;
import com.hookaddon.schematic.TransformMath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_310;

public final class PlacementPlanner {
    private static final class_310 mc = class_310.method_1551();

    private PlacementPlanner() {
    }

    public static Plan build(Schematic schematic, TransformMath.Mirror mirror, TransformMath.Rotation rotation, Anchor anchor, class_2338 originSetting) {
        List<Schematic.Entry> entries = schematic.entries();
        if (entries.isEmpty()) {
            return new Plan(originSetting, mirror, rotation, 0, 0, 0, List.of(), Map.of());
        }
        int[] transformedX = new int[entries.size()];
        int[] transformedY = new int[entries.size()];
        int[] transformedZ = new int[entries.size()];
        int minTieX = Integer.MAX_VALUE;
        int minTieY = Integer.MAX_VALUE;
        int minTieZ = Integer.MAX_VALUE;
        int maxTieX = Integer.MIN_VALUE;
        int maxTieY = Integer.MIN_VALUE;
        int maxTieZ = Integer.MIN_VALUE;
        for (int i = 0; i < entries.size(); ++i) {
            Schematic.Entry entry = entries.get(i);
            int[] t = TransformMath.transform(entry.x(), entry.y(), entry.z(), mirror, rotation);
            transformedX[i] = t[0];
            transformedY[i] = t[1];
            transformedZ[i] = t[2];
            minTieX = Math.min(minTieX, t[0]);
            minTieY = Math.min(minTieY, t[1]);
            minTieZ = Math.min(minTieZ, t[2]);
            maxTieX = Math.max(maxTieX, t[0]);
            maxTieY = Math.max(maxTieY, t[1]);
            maxTieZ = Math.max(maxTieZ, t[2]);
        }
        int sizeX = maxTieX - minTieX + 1;
        int sizeY = maxTieY - minTieY + 1;
        int sizeZ = maxTieZ - minTieZ + 1;
        class_2338 anchorPoint = switch (anchor.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> originSetting;
            case 1, 2 -> PlacementPlanner.mc.field_1724 == null ? originSetting : PlacementPlanner.mc.field_1724.method_24515();
        };
        int offsetX = anchorPoint.method_10263() - minTieX;
        int offsetY = anchorPoint.method_10264() - minTieY;
        int offsetZ = anchorPoint.method_10260() - minTieZ;
        if (anchor == Anchor.CENTER_ON_PLAYER) {
            offsetX -= sizeX / 2;
            offsetZ -= sizeZ / 2;
        }
        class_2338 origin = new class_2338(offsetX, offsetY, offsetZ);
        HashMap<class_2248, Integer> required = new HashMap<class_2248, Integer>();
        ArrayList<Target> targets = new ArrayList<Target>(entries.size());
        for (int i = 0; i < entries.size(); ++i) {
            Schematic.Entry entry = entries.get(i);
            class_2338 world = new class_2338(transformedX[i] + offsetX, transformedY[i] + offsetY, transformedZ[i] + offsetZ);
            targets.add(new Target(world, entry.state()));
            required.merge(entry.state().method_26204(), 1, Integer::sum);
        }
        int anchorX = anchorPoint.method_10263();
        int anchorZ = anchorPoint.method_10260();
        targets.sort(Comparator.comparingInt(target -> target.pos().method_10264()).thenComparingInt(target -> PlacementPlanner.horizontalDistanceSq(target.pos(), anchorX, anchorZ)));
        return new Plan(origin, mirror, rotation, sizeX, sizeY, sizeZ, targets, required);
    }

    private static int horizontalDistanceSq(class_2338 pos, int anchorX, int anchorZ) {
        int dx = pos.method_10263() - anchorX;
        int dz = pos.method_10260() - anchorZ;
        return dx * dx + dz * dz;
    }

    public static Map<class_2248, Integer> missingMaterials(Plan plan, Map<class_2248, Integer> available) {
        HashMap<class_2248, Integer> missing = new HashMap<class_2248, Integer>();
        for (Map.Entry<class_2248, Integer> entry : plan.required().entrySet()) {
            int have = available.getOrDefault(entry.getKey(), 0);
            if (have >= entry.getValue()) continue;
            missing.put(entry.getKey(), entry.getValue() - have);
        }
        return missing;
    }

    public record Plan(class_2338 origin, TransformMath.Mirror mirror, TransformMath.Rotation rotation, int sizeX, int sizeY, int sizeZ, List<Target> targets, Map<class_2248, Integer> required) {
        public int volume() {
            return this.sizeX * this.sizeY * this.sizeZ;
        }
    }

    public static enum Anchor {
        POSITION("At origin position"),
        PLAYER("At my position"),
        CENTER_ON_PLAYER("Centred on me");

        public final String title;

        private Anchor(String title) {
            this.title = title;
        }
    }

    public record Target(class_2338 pos, class_2680 expected) {
    }
}

