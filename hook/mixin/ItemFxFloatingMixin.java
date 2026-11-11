/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11659
 *  net.minecraft.class_4587
 *  net.minecraft.class_4603
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.itemfx.ItemFx;
import net.minecraft.class_11659;
import net.minecraft.class_4587;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_4603.class})
public abstract class ItemFxFloatingMixin {
    @Shadow
    private int field_59973;

    @Inject(method={"renderFloatingItem"}, at={@At(value="HEAD")})
    private void hook$floatStart(class_4587 var1, float var2, class_11659 var3, CallbackInfo var4) {
        ItemFx.floating(Math.max(0.0f, Math.min(1.0f, ((float)(40 - this.field_59973) + var2) / 40.0f)));
    }

    @Inject(method={"renderFloatingItem"}, at={@At(value="RETURN")})
    private void hook$floatEnd(class_4587 var1, float var2, class_11659 var3, CallbackInfo var4) {
        ItemFx.floating(-1.0f);
    }
}

