package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_1657;

/** Enables vanilla flight where the current world/server grants flight permission. */
public final class FlyModule extends Module {
    private final NumberSetting flySpeed = new NumberSetting(EncryptedString.of("Fly Speed"), 0.01, 0.20, 0.05, 0.01);
    private class_1657 trackedPlayer;
    private boolean originalFlying;
    private float originalFlySpeed;
    private boolean captured;
    private boolean warned;

    public FlyModule() {
        super(EncryptedString.of("Fly"), EncryptedString.of("Uses normal Minecraft flight when the world or server allows it."), -1, Category.MISC);
        this.addSettings(this.flySpeed);
    }

    @Override
    public void onEnable() {
        this.trackedPlayer = null;
        this.captured = false;
        this.warned = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.restore();
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        class_1657 player = this.mc.field_1724;
        if (player == null) return;
        var abilities = player.method_31549();
        if (this.trackedPlayer != player) {
            this.restore();
            this.trackedPlayer = player;
            this.originalFlying = abilities.field_7479;
            this.originalFlySpeed = abilities.method_7252();
            this.captured = true;
        }
        if (!abilities.field_7478) {
            if (!this.warned) {
                player.method_7353(net.minecraft.class_2561.method_43470("Fly benötigt Kreativmodus oder einen Server, der Fliegen erlaubt."), false);
                this.warned = true;
            }
            return;
        }
        this.warned = false;
        abilities.method_7248(this.flySpeed.getFloatValue());
        if (!abilities.field_7479) {
            abilities.field_7479 = true;
            player.method_7355();
        }
    }

    private void restore() {
        if (!this.captured || this.trackedPlayer == null) return;
        var abilities = this.trackedPlayer.method_31549();
        abilities.field_7479 = this.originalFlying;
        abilities.method_7248(this.originalFlySpeed);
        this.trackedPlayer.method_7355();
        this.captured = false;
        this.trackedPlayer = null;
    }
}
