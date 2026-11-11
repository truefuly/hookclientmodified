/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_4184
 *  net.minecraft.class_5636
 *  net.minecraft.class_638
 *  net.minecraft.class_758
 *  net.minecraft.class_9779
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package hook.mixin;

import hook.halloween.HalloweenAtmosphere;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_4184;
import net.minecraft.class_5636;
import net.minecraft.class_638;
import net.minecraft.class_758;
import net.minecraft.class_9779;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={class_758.class})
public abstract class HalloweenFogMixin {
    @ModifyArgs(method={"applyFog"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"))
    private void hook$halloweenFog(Args var1, class_4184 var2, int var3, class_9779 var4, float var5, class_638 var6) {
        if (!(HalloweenAtmosphere.fogAmount() <= 0.0f) && var2.method_19334() == class_5636.field_27888) {
            class_1309 var7;
            class_1297 class_12972 = var2.method_19331();
            if (class_12972 instanceof class_1309 && ((var7 = (class_1309)class_12972).method_6059(class_1294.field_5919) || var7.method_6059(class_1294.field_38092))) {
                return;
            }
            float[] var11 = new float[]{((Float)var1.get(3)).floatValue(), ((Float)var1.get(4)).floatValue(), ((Float)var1.get(5)).floatValue(), ((Float)var1.get(6)).floatValue(), ((Float)var1.get(7)).floatValue(), ((Float)var1.get(8)).floatValue()};
            Vector4f var12 = (Vector4f)var1.get(2);
            Vector4f var9 = new Vector4f((Vector4fc)var12);
            HalloweenAtmosphere.fog(var11, var9);
            var1.set(2, (Object)var9);
            for (int var10 = 0; var10 < 6; ++var10) {
                var1.set(3 + var10, (Object)Float.valueOf(var11[var10]));
            }
        }
    }
}

