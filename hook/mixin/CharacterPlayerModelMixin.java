/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10055
 *  net.minecraft.class_1306
 *  net.minecraft.class_4587
 *  net.minecraft.class_591
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.feature.CharacterFeatureRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_1306;
import net.minecraft.class_4587;
import net.minecraft.class_591;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_591.class})
public abstract class CharacterPlayerModelMixin {
    @Inject(method={"setArmAngle"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$characterHand(class_10055 var1, class_1306 var2, class_4587 var3, CallbackInfo var4) {
        if (CharacterFeatureRenderer.translateToHand(((class_591)(Object)this), var1, var2, var3)) {
            var4.cancel();
        }
    }
}

