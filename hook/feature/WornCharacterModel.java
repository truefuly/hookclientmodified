/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12249
 *  net.minecraft.class_1921
 *  net.minecraft.class_2960
 */
package hook.feature;

import hook.feature.BakedCharacterData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;

@Environment(value=EnvType.CLIENT)
public final class WornCharacterModel {
    final BakedCharacterData model;
    final class_1921 solid;
    final class_1921 decal;
    final class_1921 ghost;

    WornCharacterModel(BakedCharacterData model, class_2960 texture) {
        this.model = model;
        this.solid = class_12249.method_75982((class_2960)texture);
        this.decal = class_12249.method_76000((class_2960)texture);
        this.ghost = class_12249.method_75998((class_2960)texture);
    }
}

