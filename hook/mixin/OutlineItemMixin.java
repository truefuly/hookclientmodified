/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10444$class_10445
 *  net.minecraft.class_1921
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_811
 *  net.minecraft.class_918
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.feature.ItemOutlineModule;
import java.util.List;
import net.minecraft.class_10444;
import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_811;
import net.minecraft.class_918;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_918.class})
public abstract class OutlineItemMixin {
    @Inject(method={"renderItem"}, at={@At(value="HEAD")})
    private static void hook$outlineHeldItem(class_811 var0, class_4587 var1, class_4597 var2, int var3, int var4, int[] var5, List<?> var6, class_1921 var7, class_10444.class_10445 var8, CallbackInfo var9) {
        ItemOutlineModule.renderHeldItem(var0, var1, var2, var3, var4, var5, (List<net.minecraft.class_777>)(List)var6, var7);
    }
}

