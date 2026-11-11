/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_241
 *  net.minecraft.class_744
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package hook.mixin;

import net.minecraft.class_241;
import net.minecraft.class_744;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_744.class})
public interface InputAccessor {
    @Accessor(value="movementVector")
    public void hook$setMovementVector(class_241 var1);
}

