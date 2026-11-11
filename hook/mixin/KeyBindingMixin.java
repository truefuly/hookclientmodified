/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_304
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_306
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package hook.mixin;

import hook.Hook;
import hook.imixin.IKeybinding;
import net.minecraft.class_1041;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_304.class})
public abstract class KeyBindingMixin
implements IKeybinding {
    @Shadow
    private class_3675.class_306 field_1655;

    @Shadow
    public abstract void method_23481(boolean var1);

    @Override
    public boolean hook$isActuallyPressed() {
        return class_3675.method_15987((class_1041)Hook.mc.method_22683(), (int)this.field_1655.method_1444());
    }

    @Override
    public void hook$resetPressed() {
        this.method_23481(this.hook$isActuallyPressed());
    }
}

