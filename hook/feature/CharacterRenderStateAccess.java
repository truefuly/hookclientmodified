/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.WornCharacterModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface CharacterRenderStateAccess {
    public WornCharacterModel hook$character();

    public void hook$setCharacter(WornCharacterModel var1);

    public float hook$seat();

    public void hook$setSeat(float var1);
}

