/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_4184
 *  net.minecraft.class_5636
 *  net.minecraft.class_638
 *  net.minecraft.class_758
 *  org.joml.Vector4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package hook.mixin;

import hook.feature.CustomSkysModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_4184;
import net.minecraft.class_5636;
import net.minecraft.class_638;
import net.minecraft.class_758;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_758.class})
public abstract class CustomSkysFogMixin {
    @Inject(method={"getFogColor"}, at={@At(value="RETURN")})
    private void hook$fogColour(class_4184 var1, float var2, class_638 var3, int var4, float var5, CallbackInfoReturnable<Vector4f> var6) {
        if (var1.method_19334() == class_5636.field_27888 && !(var5 > 0.0f)) {
            class_1309 var7;
            class_1297 class_12972 = var1.method_19331();
            if (class_12972 instanceof class_1309 && ((var7 = (class_1309)class_12972).method_6059(class_1294.field_5919) || var7.method_6059(class_1294.field_38092))) {
                return;
            }
            CustomSkysModule var9 = CustomSkysModule.get();
            if (var9 != null) {
                var9.fog(var1, var3, (Vector4f)var6.getReturnValue());
            }
        }
    }
}

