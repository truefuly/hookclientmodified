/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10055
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package hook.mixin;

import hook.feature.CharacterRenderStateAccess;
import hook.feature.WornCharacterModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_10055.class})
public abstract class CharacterRenderStateMixin
implements CharacterRenderStateAccess {
    @Unique
    private WornCharacterModel hook$character;
    @Unique
    private float hook$seat;

    @Override
    public WornCharacterModel hook$character() {
        return this.hook$character;
    }

    @Override
    public void hook$setCharacter(WornCharacterModel var1) {
        this.hook$character = var1;
    }

    @Override
    public float hook$seat() {
        return this.hook$seat;
    }

    @Override
    public void hook$setSeat(float var1) {
        this.hook$seat = var1;
    }
}

