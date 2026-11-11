/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 *  net.minecraft.class_2818
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import hook.event.events.SetBlockStateEvent;
import hook.manager.EventManager;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_2818.class})
public class WorldChunkMixin {
    @Shadow
    @Final
    class_1937 field_12858;

    @Inject(method={"setBlockState"}, at={@At(value="TAIL")})
    private void onSetBlockState(class_2338 var1, class_2680 var2, int var3, CallbackInfoReturnable<class_2680> var4) {
        if (this.field_12858.method_8608() && EventManager.hasActiveListener(SetBlockStateEvent.class)) {
            EventManager.b(new SetBlockStateEvent(var1, (class_2680)var4.getReturnValue(), var2));
        }
    }
}

