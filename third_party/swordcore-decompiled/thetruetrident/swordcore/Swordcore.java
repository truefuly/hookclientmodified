/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ModInitializer
 */
package thetruetrident.swordcore;

import net.fabricmc.api.ModInitializer;
import thetruetrident.swordcore.SwordcoreParticles;

public class Swordcore
implements ModInitializer {
    public static final String MOD_ID = "swordcore";

    public void onInitialize() {
        SwordcoreParticles.register();
    }
}

