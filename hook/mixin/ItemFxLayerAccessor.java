/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10444$class_10445
 *  net.minecraft.class_10444$class_10446
 *  net.minecraft.class_804
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package hook.mixin;

import net.minecraft.class_10444;
import net.minecraft.class_804;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_10444.class_10446.class})
public interface ItemFxLayerAccessor {
    @Accessor(value="transform", remap=true)
    public class_804 hook$transform();

    @Accessor(value="glint", remap=true)
    public class_10444.class_10445 hook$glint();
}

