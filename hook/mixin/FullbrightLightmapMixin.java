/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_765
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package hook.mixin;

import hook.feature.FullbrightModule;
import net.minecraft.class_765;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={class_765.class})
public abstract class FullbrightLightmapMixin {
    private static final Vector3fc HOOK_WHITE = new Vector3f(1.0f, 1.0f, 1.0f);

    @ModifyArg(method={"update"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal=0))
    private float hook$fullbrightAmbientFactor(float ambient) {
        float strength = FullbrightModule.strength();
        if (strength <= 0.0f) {
            return ambient;
        }
        return ambient + (1.0f - ambient) * strength;
    }

    @ModifyArg(method={"update"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/buffers/Std140Builder;putVec3(Lorg/joml/Vector3fc;)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal=1))
    private Vector3fc hook$fullbrightAmbientColor(Vector3fc ambientColor) {
        return FullbrightModule.strength() > 0.0f ? HOOK_WHITE : ambientColor;
    }
}

