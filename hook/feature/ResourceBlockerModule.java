package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_124;
import net.minecraft.class_2561;

/**
 * Declines server resource packs without the server noticing: the client answers
 * accepted -> downloaded -> loaded, but never downloads or applies the pack.
 * (Ported from SilentClient; the packet handling lives in ResourcePackBlockMixin.)
 */
public final class ResourceBlockerModule extends Module {
    private static ResourceBlockerModule instance;
    private final BooleanSetting notify = new BooleanSetting(EncryptedString.of("Chat Notice"), true).setDescription(EncryptedString.of("Tells you in chat when a pack was blocked"));

    public ResourceBlockerModule() {
        super(EncryptedString.of("Resource Blocker"), EncryptedString.of("Skips server resource packs while the server thinks you loaded them"), -1, Category.MISC);
        this.addSettings(this.notify);
        instance = this;
    }

    public static boolean active() {
        return instance != null && instance.isEnabled();
    }

    public static void blocked() {
        ResourceBlockerModule module = instance;
        if (module == null || !module.notify.getValue()) return;
        module.mc.execute(() -> {
            if (module.mc.field_1724 != null) {
                module.mc.field_1724.method_7353(class_2561.method_43470("[Resource Blocker] ").method_27692(class_124.field_1080)
                        .method_10852(class_2561.method_43470("Blocked the server resource pack").method_27692(class_124.field_1068)), false);
            }
        });
    }
}
