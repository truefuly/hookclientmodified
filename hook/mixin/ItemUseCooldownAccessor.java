package hook.mixin;

import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read/write access to MinecraftClient.itemUseCooldown (used by Fast Use). */
@Mixin(class_310.class)
public interface ItemUseCooldownAccessor {
    @Accessor(value = "field_1752", remap = false)
    int hook$getItemUseCooldown();

    @Accessor(value = "field_1752", remap = false)
    void hook$setItemUseCooldown(int cooldown);
}
