/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_3675$class_307
 */
package thetruetrident.cartcore;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_304;
import net.minecraft.class_3675;

@Environment(value=EnvType.CLIENT)
public class C {
    public static final class_304 A = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.cartcore.crossbowcart", class_3675.class_307.field_1668, 72, class_304.class_11900.field_62556));

    public static class_304 A() {
        return A;
    }
}

