/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ModInitializer
 */
package hook;

import com.hookaddon.HookAddon;
import hook.Hook;
import net.fabricmc.api.ModInitializer;

public final class Main
implements ModInitializer {
    public void onInitialize() {
        new Hook();
        new HookAddon().onInitialize();
        new thetruetrident.swordcore.Swordcore().onInitialize();
    }
}

