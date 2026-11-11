/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1299
 *  net.minecraft.class_2338
 */
package com.hookaddon.spawner;

import net.minecraft.class_1299;
import net.minecraft.class_2338;

public record SpawnerTarget(class_2338 pos, class_1299<?> type, boolean confirmed) {
    public String describe() {
        String where = "(" + this.pos.method_10263() + ", " + this.pos.method_10264() + ", " + this.pos.method_10260() + ")";
        if (this.type == null) {
            return "spawner at " + where;
        }
        return "spawner at " + where;
    }
}

