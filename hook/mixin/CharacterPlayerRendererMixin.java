/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10055
 *  net.minecraft.class_1007
 *  net.minecraft.class_11659
 *  net.minecraft.class_11890
 *  net.minecraft.class_1297
 *  net.minecraft.class_2960
 *  net.minecraft.class_3883
 *  net.minecraft.class_4587
 *  net.minecraft.class_5617$class_5618
 *  net.minecraft.class_583
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 *  net.minecraft.class_922
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.feature.Character3dModule;
import hook.feature.CharacterFeatureRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11659;
import net.minecraft.class_11890;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_4587;
import net.minecraft.class_5617;
import net.minecraft.class_583;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_1007.class})
public abstract class CharacterPlayerRendererMixin<T extends class_11890>
extends class_922<T, class_10055, class_591> {
    protected CharacterPlayerRendererMixin(class_5617.class_5618 var1, class_591 var2, float var3) {
        super(var1, (class_583)var2, var3);
    }

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void hook$addCharacterLayer(class_5617.class_5618 var1, boolean var2, CallbackInfo var3) {
        this.method_4046(new CharacterFeatureRenderer((class_3883<class_10055, class_591>)this));
    }

    @Inject(method={"updateRenderState"}, at={@At(value="TAIL")})
    private void hook$extractCharacter(class_11890 var1, class_10055 var2, float var3, CallbackInfo var4) {
        Character3dModule.extract((class_1297)var1, var2);
    }

    @Inject(method={"renderArm"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$characterFirstPersonHand(class_4587 var1, class_11659 var2, int var3, class_2960 var4, class_630 var5, boolean var6, CallbackInfo var7) {
        if (CharacterFeatureRenderer.firstPersonHand((class_591)this.method_4038(), var1, var2, var3, var5)) {
            var7.cancel();
        }
    }
}

