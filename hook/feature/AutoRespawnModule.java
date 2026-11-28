package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;

/** Respawns you the moment you die: the death screen is skipped and the respawn request goes out right away. */
public final class AutoRespawnModule extends Module {
    private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 100.0, 0.0, 1.0).getValue(EncryptedString.of("Ticks to wait before respawning (0 = instantly, 20 = one second)"));
    private int deadTicks;

    public AutoRespawnModule() {
        super(EncryptedString.of("Auto Respawn"), EncryptedString.of("Respawns you instantly every time you die"), -1, Category.MISC);
        this.addSettings(this.delay);
    }

    @EventListener
    public void onTick(TickEvent event) {
        net.minecraft.class_746 p = this.mc.field_1724;
        if (p == null) return;
        boolean dead = this.mc.field_1755 instanceof net.minecraft.class_418 || p.method_29504();
        if (!dead) {
            this.deadTicks = 0;
            return;
        }
        // TickEvent fires twice per tick (client + player); the delay counts client ticks well enough either way
        if (this.deadTicks++ < this.delay.getIntValue()) return;
        p.method_7331();
        if (this.mc.field_1755 instanceof net.minecraft.class_418) this.mc.method_1507(null);
        this.deadTicks = 0;
    }
}
