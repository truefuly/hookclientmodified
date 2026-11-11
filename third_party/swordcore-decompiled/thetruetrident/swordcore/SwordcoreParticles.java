/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
 *  net.minecraft.class_2378
 *  net.minecraft.class_2400
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 */
package thetruetrident.swordcore;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.class_2378;
import net.minecraft.class_2400;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public final class SwordcoreParticles {
    public static final class_2400 MOVEMENT_RESIST = FabricParticleTypes.simple();
    public static final class_2400 MOVEMENT_SYNC_INNER = FabricParticleTypes.simple();

    private SwordcoreParticles() {
    }

    public static void register() {
        class_2378.method_10230((class_2378)class_7923.field_41180, (class_2960)class_2960.method_60655((String)"swordcore", (String)"movement_resist"), (Object)MOVEMENT_RESIST);
        class_2378.method_10230((class_2378)class_7923.field_41180, (class_2960)class_2960.method_60655((String)"swordcore", (String)"movement_sync_inner"), (Object)MOVEMENT_SYNC_INNER);
    }
}

