/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10442
 *  net.minecraft.class_10444
 *  net.minecraft.class_11566
 *  net.minecraft.class_1799
 *  net.minecraft.class_1937
 *  net.minecraft.class_811
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.itemfx.ItemFx;
import net.minecraft.class_10442;
import net.minecraft.class_10444;
import net.minecraft.class_11566;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_10442.class})
public abstract class ItemFxModelManagerMixin {
    @Inject(method={"clearAndUpdate"}, at={@At(value="TAIL")})
    private void hook$tagFx(class_10444 var1, class_1799 var2, class_811 var3, class_1937 var4, class_11566 var5, int var6, CallbackInfo var7) {
        try {
            ItemFx.tag(var1, var2, var3);
        }
        catch (Throwable var9) {
            System.err.println("Hook: custom item tagging failed: " + String.valueOf(var9));
        }
    }
}

