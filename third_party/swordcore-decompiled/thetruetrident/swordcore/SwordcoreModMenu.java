/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.terraformersmc.modmenu.api.ConfigScreenFactory
 *  com.terraformersmc.modmenu.api.ModMenuApi
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package thetruetrident.swordcore;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import thetruetrident.swordcore.SwordcoreConfigScreen;

@Environment(value=EnvType.CLIENT)
public class SwordcoreModMenu
implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SwordcoreConfigScreen::new;
    }
}

