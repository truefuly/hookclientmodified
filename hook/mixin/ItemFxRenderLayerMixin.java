/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1921
 *  net.minecraft.class_9801
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.itemfx.ItemFxRenderer;
import net.minecraft.class_1921;
import net.minecraft.class_9801;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_1921.class})
public abstract class ItemFxRenderLayerMixin {
    @Inject(method={"draw"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$drawFx(class_9801 var1, CallbackInfo var2) {
        if (ItemFxRenderer.isMarker(this)) {
            ItemFxRenderer.flush(var1);
            var2.cancel();
        }
    }
}

