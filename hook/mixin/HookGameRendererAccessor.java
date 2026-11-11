/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4184
 *  net.minecraft.class_757
 *  net.minecraft.class_758
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package hook.mixin;

import net.minecraft.class_4184;
import net.minecraft.class_757;
import net.minecraft.class_758;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_757.class})
public interface HookGameRendererAccessor {
    @Invoker(value="getFov")
    public float hook$getFov(class_4184 var1, float var2, boolean var3);

    @Accessor(value="fogRenderer")
    public class_758 hook$getFogRenderer();

    @Invoker(value="shouldRenderBlockOutline")
    public boolean hook$shouldRenderBlockOutline();
}

