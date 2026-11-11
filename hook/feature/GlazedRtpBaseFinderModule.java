package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_2338;

/** Hook port of Glazed's RTPBaseFinder, adapted to Hook's event and setting APIs. */
public final class GlazedRtpBaseFinderModule extends Module {
    private final BooleanSetting notifications = new BooleanSetting(EncryptedString.of("Notifications"), true);
    private final NumberSetting targetY = new NumberSetting(EncryptedString.of("Dig To Y"), -64.0, 0.0, -58.0, 1.0);
    private class_2338 target;
    private boolean waitingForTeleport;
    private long teleportStarted;

    public GlazedRtpBaseFinderModule() {
        super(EncryptedString.of("Glazed RTP Base Finder"), EncryptedString.of("Mines straight down to the target Y, then runs /rtp east."), -1, Category.GLAZED);
        this.addSettings(this.notifications, this.targetY);
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.waitingForTeleport = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.releaseAttack();
        this.target = null;
        this.waitingForTeleport = false;
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.method_1562() == null) {
            this.releaseAttack();
            return;
        }
        if (this.mc.field_1755 != null) {
            this.releaseAttack();
            return;
        }
        if (this.waitingForTeleport) {
            this.releaseAttack();
            if (System.currentTimeMillis() - this.teleportStarted > 3000L) {
                this.target = this.mc.field_1724.method_24515().method_10087(1);
                this.waitingForTeleport = false;
            }
            return;
        }
        if (this.target == null) this.target = this.mc.field_1724.method_24515().method_10087(1);
        if (this.target.method_10264() <= this.targetY.getIntValue()) {
            this.releaseAttack();
            this.mc.method_1562().method_45730("rtp east");
            this.teleportStarted = System.currentTimeMillis();
            this.waitingForTeleport = true;
            this.notify("Reached target Y; teleporting east.");
            return;
        }
        this.mc.field_1724.method_36457(90.0f);
        if (this.mc.field_1687.method_8320(this.target).method_26215()) {
            this.releaseAttack();
            this.target = this.target.method_10087(1);
        } else {
            this.mc.field_1690.field_1886.method_23481(true);
        }
    }

    private void releaseAttack() {
        if (this.mc.field_1690 != null) this.mc.field_1690.field_1886.method_23481(false);
    }

    private void notify(String message) {
        if (this.notifications.getValue() && this.mc.field_1724 != null) {
            this.mc.field_1724.method_7353(net.minecraft.class_2561.method_43470("[Glazed] " + message), false);
        }
    }
}
