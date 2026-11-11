package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.RgbSetting;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1657;

/** Players you hit glow through walls for a few seconds, blinking shortly before it fades. (Ported from SilentClient.) */
public final class HitGlowModule extends Module {
    private static HitGlowModule instance;
    private final NumberSetting duration = new NumberSetting(EncryptedString.of("Duration"), 1.0, 30.0, 7.0, 1.0).getValue(EncryptedString.of("Seconds a hit player keeps glowing"));
    private final RgbSetting color = new RgbSetting(EncryptedString.of("Glow Color"), 0xFFFF5A5A);
    private final BooleanSetting blink = new BooleanSetting(EncryptedString.of("Blink Before Fading"), true).setDescription(EncryptedString.of("Flashes during the last 1.5 seconds"));
    private final StringSetting ignored = new StringSetting(EncryptedString.of("Ignored Players"), "").setDescription(EncryptedString.of("Comma separated names that never glow"));
    private final Map<UUID, Long> glowing = new HashMap<>();

    public HitGlowModule() {
        super(EncryptedString.of("Hit Glow"), EncryptedString.of("Players you hit glow through walls for a few seconds"), -1, Category.RENDER);
        this.addSettings(this.duration, this.color, this.blink, this.ignored);
        instance = this;
    }

    @Override
    public void onDisable() {
        this.glowing.clear();
        super.onDisable();
    }

    /** Called when the local player attacks an entity. */
    public static void onAttack(class_1297 target) {
        HitGlowModule module = instance;
        if (module != null && module.isEnabled() && target instanceof class_1657 player && player != module.mc.field_1724) {
            module.glowing.put(player.method_5667(), System.currentTimeMillis() + Math.round(module.duration.getValue() * 1000.0));
        }
    }

    /** Outline color for this entity, or 0 for none. */
    public static int outlineColor(class_1297 entity) {
        HitGlowModule module = instance;
        if (module == null || !module.isEnabled() || module.glowing.isEmpty() || !(entity instanceof class_1657 player)) {
            return 0;
        }
        if (module.mc.field_1687 == null) {
            module.glowing.clear();
            return 0;
        }
        Long expiry = module.glowing.get(player.method_5667());
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        if (remaining <= 0) {
            module.glowing.remove(player.method_5667());
            return 0;
        }
        String name = player.method_5477().getString().toLowerCase(Locale.ROOT);
        for (String ignoredName : module.ignored.getValue().toLowerCase(Locale.ROOT).split(",")) {
            if (!ignoredName.isBlank() && ignoredName.trim().equals(name)) return 0;
        }
        if (module.blink.getValue() && remaining <= 1500) {
            long interval = remaining > 1000 ? 200 : remaining > 500 ? 120 : 70;
            if ((remaining / interval) % 2 != 0) return 0;
        }
        return 0xFF000000 | module.color.getArgb();
    }
}
