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

import hook.halloween.HalloweenAtmosphere;
import net.minecraft.class_765;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={class_765.class})
public abstract class HalloweenLightmapMixin {
    private static final String PUT = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;";
    private static final String VEC = "Lcom/mojang/blaze3d/buffers/Std140Builder;putVec3(Lorg/joml/Vector3fc;)Lcom/mojang/blaze3d/buffers/Std140Builder;";

    @ModifyArg(method={"update"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal=1))
    private float hook$sky(float var1) {
        return HalloweenAtmosphere.lightmap(1, var1);
    }

    @ModifyArg(method={"update"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal=2))
    private float hook$block(float var1) {
        return HalloweenAtmosphere.lightmap(2, var1);
    }

    @ModifyArg(method={"update"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal=3))
    private float hook$nightVision(float var1) {
        return HalloweenAtmosphere.lightmap(3, var1);
    }

    @ModifyArg(method={"update"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal=5))
    private float hook$darken(float var1) {
        return HalloweenAtmosphere.lightmap(5, var1);
    }

    @ModifyArg(method={"update"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal=6))
    private float hook$gamma(float var1) {
        return HalloweenAtmosphere.lightmap(6, var1);
    }

    @ModifyArg(method={"update"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/buffers/Std140Builder;putVec3(Lorg/joml/Vector3fc;)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal=0))
    private Vector3fc hook$moonlight(Vector3fc var1) {
        return HalloweenAtmosphere.strength() > 0.0f ? HalloweenAtmosphere.skyLight(new Vector3f(var1)) : var1;
    }
}

