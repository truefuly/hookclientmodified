package hook.mixin;

import net.minecraft.class_638;
import net.minecraft.class_7202;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Access to the client world's block prediction handler (for vanilla-exact use packets). */
@Mixin(value = class_638.class, remap = false)
public interface ClientLevelPredictionAccessor {
    @Invoker(value = "method_41925", remap = false)
    class_7202 hook$predictionHandler();
}
