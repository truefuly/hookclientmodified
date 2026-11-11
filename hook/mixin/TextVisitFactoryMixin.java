/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_5223
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package hook.mixin;

import hook.Hook;
import hook.feature.NameProtectModule;
import net.minecraft.class_5223;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={class_5223.class})
public class TextVisitFactoryMixin {
    @ModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", ordinal=0), method={"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"}, index=0)
    private static String adjustText(String s) {
        if (Hook.INSTANCE != null && Hook.INSTANCE.MODULE_MANAGER != null) {
            NameProtectModule nameprotect = (NameProtectModule)Hook.INSTANCE.MODULE_MANAGER.getModuleByClass(NameProtectModule.class);
            return nameprotect != null && nameprotect.isEnabled() && s.contains(Hook.mc.method_1548().method_1676()) ? s.replace(Hook.mc.method_1548().method_1676(), nameprotect.getFakeName()) : s;
        }
        return s;
    }
}

