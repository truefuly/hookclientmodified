/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_6017
 *  net.minecraft.class_6793
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package hook.mixin;

import net.minecraft.class_6017;
import net.minecraft.class_6793;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_6793.class})
public interface CountPlacementModifierAccessor {
    @Accessor(value="count")
    public class_6017 getCount();
}

