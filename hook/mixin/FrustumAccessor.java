/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4604
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package hook.mixin;

import net.minecraft.class_4604;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_4604.class})
public interface FrustumAccessor {
    @Accessor(value="x")
    public double getX();

    @Accessor(value="x")
    public void setX(double var1);

    @Accessor(value="y")
    public double getY();

    @Accessor(value="y")
    public void setY(double var1);

    @Accessor(value="z")
    public double getZ();

    @Accessor(value="z")
    public void setZ(double var1);
}

