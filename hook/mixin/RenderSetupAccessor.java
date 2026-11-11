/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_12247
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package hook.mixin;

import java.util.Map;
import net.minecraft.class_12247;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_12247.class}, remap=true)
public interface RenderSetupAccessor {
    @Accessor(value="textures", remap=true)
    public Map<?, ?> hook$getTextures();
}

