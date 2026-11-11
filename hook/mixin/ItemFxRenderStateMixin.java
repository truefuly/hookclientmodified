/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10444
 *  net.minecraft.class_10444$class_10446
 *  net.minecraft.class_11659
 *  net.minecraft.class_4587
 *  net.minecraft.class_811
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.itemfx.ItemFx;
import hook.itemfx.ItemFxState;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_4587;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_10444.class})
public abstract class ItemFxRenderStateMixin
implements ItemFxState {
    @Shadow
    class_811 field_55337;
    @Shadow
    private int field_55339;
    @Shadow
    private class_10444.class_10446[] field_55340;
    @Unique
    private int hook$fx;

    @Override
    public int hook$fx() {
        return this.hook$fx;
    }

    @Override
    public void hook$setFx(int var1) {
        this.hook$fx = var1;
    }

    @Override
    public class_10444.class_10446 hook$layer0() {
        return this.field_55340[0];
    }

    @Inject(method={"clear"}, at={@At(value="HEAD")})
    private void hook$clearFx(CallbackInfo var1) {
        this.hook$fx = 0;
    }

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$renderFx(class_4587 var1, class_11659 var2, int var3, int var4, int var5, CallbackInfo var6) {
        if (this.hook$fx != 0 && this.field_55339 > 0) {
            try {
                if (ItemFx.render(this.hook$fx, this.field_55337, this.field_55340[0], var1, var2, var3)) {
                    var6.cancel();
                }
            }
            catch (Throwable var8) {
                this.hook$fx = 0;
                System.err.println("Hook: custom item render failed: " + String.valueOf(var8));
            }
        }
    }
}

