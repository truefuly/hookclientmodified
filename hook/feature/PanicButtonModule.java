package hook.feature;

import hook.Hook;
import hook.module.Category;
import hook.module.Module;
import hook.utils.EncryptedString;
import java.util.IdentityHashMap;
import java.util.Map;

/** Temporarily disables and hides every non-visual module, then restores its prior state. */
public final class PanicButtonModule extends Module {
    private static volatile boolean panicActive;
    private final Map<Module, Boolean> previousStates = new IdentityHashMap<>();

    public PanicButtonModule() {
        super(EncryptedString.of("Panic Button"), EncryptedString.of("Temporarily disables and hides non-visual modules. Press again to restore them."), -1, Category.CLIENT);
    }

    public static boolean shouldHide(Module module) {
        if (!panicActive || module == null || module instanceof PanicButtonModule) return false;
        return module.getCategory() != Category.VISUALS;
    }

    public static boolean isPanicActive() {
        return panicActive;
    }

    @Override
    public void onEnable() {
        previousStates.clear();
        panicActive = true;
        ModuleManagerAccess.disableNonVisual(this, previousStates);
        ModuleCatalog.invalidate();
    }

    @Override
    public void onDisable() {
        panicActive = false;
        for (Map.Entry<Module, Boolean> entry : previousStates.entrySet()) {
            entry.getKey().toggle(entry.getValue());
        }
        previousStates.clear();
        ModuleCatalog.invalidate();
    }

    private static final class ModuleManagerAccess {
        private static void disableNonVisual(PanicButtonModule panic, Map<Module, Boolean> states) {
            if (Hook.INSTANCE == null || Hook.INSTANCE.getModuleManager() == null) return;
            for (Module module : Hook.INSTANCE.getModuleManager().c()) {
                if (module == panic || module.getCategory() == Category.VISUALS) continue;
                states.put(module, module.isEnabled());
                if (module.isEnabled()) module.toggle(false);
            }
        }
    }
}
