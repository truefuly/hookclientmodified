/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_2394
 *  net.minecraft.class_634
 *  net.minecraft.class_702
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package hook.mixin;

import hook.itemfx.ItemFx;
import net.minecraft.class_1297;
import net.minecraft.class_2394;
import net.minecraft.class_634;
import net.minecraft.class_702;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={class_634.class})
public abstract class ItemFxTotemPopMixin {
    @Redirect(method={"onEntityStatus"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/particle/ParticleManager;addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V"))
    private void hook$totemBurst(class_702 var1, class_1297 var2, class_2394 var3, int var4) {
        boolean var5 = false;
        try {
            var5 = ItemFx.totemPop(var2, var3);
        }
        catch (Throwable var7) {
            System.err.println("Hook: totem burst failed: " + String.valueOf(var7));
        }
        if (!var5) {
            var1.method_3051(var2, var3, var4);
        }
    }
}

