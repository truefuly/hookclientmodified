/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_12247
 *  net.minecraft.class_1921
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package hook.mixin;

import net.minecraft.class_12247;
import net.minecraft.class_1921;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_1921.class})
public interface RenderLayerAccessor {
    @Invoker(value="of")
    public static class_1921 hook$create(String var0, class_12247 var1) {
        throw new AssertionError();
    }

    @Accessor(value="renderSetup", remap=true)
    public class_12247 hook$getRenderSetup();
}

