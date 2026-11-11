/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2794
 *  net.minecraft.class_5868
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package hook.mixin;

import net.minecraft.class_2794;
import net.minecraft.class_5868;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={class_5868.class}, priority=900)
public abstract class HeightContextMixin {
    @Redirect(method={"<init>"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/gen/chunk/ChunkGenerator;getMinimumY()I"), require=0)
    private int onMinY(class_2794 cg) {
        return cg == null ? -9999999 : cg.method_33730();
    }

    @Redirect(method={"<init>"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/gen/chunk/ChunkGenerator;getWorldHeight()I"), require=0)
    private int onHeight(class_2794 cg) {
        return cg == null ? 100000000 : cg.method_12104();
    }
}

