/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10017
 *  net.minecraft.class_10055
 *  net.minecraft.class_11659
 *  net.minecraft.class_1306
 *  net.minecraft.class_3883
 *  net.minecraft.class_3887
 *  net.minecraft.class_4587
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 */
package hook.feature;

import hook.feature.Character3dModule;
import hook.feature.CharacterMeshRenderer;
import hook.feature.WornCharacterModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_1306;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_591;
import net.minecraft.class_630;

@Environment(value=EnvType.CLIENT)
public final class CharacterFeatureRenderer
extends class_3887<class_10055, class_591> {
    public CharacterFeatureRenderer(class_3883<class_10055, class_591> context) {
        super(context);
    }

    public void render(class_4587 matrices, class_11659 commands, int light, class_10055 state, float animationProgress, float headYaw) {
        this.renderCharacter(matrices, commands, light, state, animationProgress, headYaw);
    }

    public void renderCharacter(class_4587 matrices, class_11659 commands, int light, class_10055 state, float animationProgress, float headYaw) {
        WornCharacterModel worn = Character3dModule.worn((class_10017)state);
        if (worn == null || state.field_53333 && state.field_53461) {
            return;
        }
        CharacterMeshRenderer.submit(worn, (class_591)this.method_17165(), matrices, commands, light, state);
    }

    public static boolean firstPersonHand(class_591 model, class_4587 matrices, class_11659 commands, int light, class_630 hand) {
        return CharacterMeshRenderer.firstPersonHand(model, matrices, commands, light, hand);
    }

    public static boolean translateToHand(class_591 model, class_10055 state, class_1306 arm, class_4587 matrices) {
        return CharacterMeshRenderer.translateToHand(model, state, arm, matrices);
    }
}

