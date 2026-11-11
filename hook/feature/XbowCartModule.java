package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.ButtonSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import hook.event.EventListener;
import hook.event.events.TickEvent;
import net.minecraft.class_2561;
import thetruetrident.cartcore.CartcoreCore;

/** CartCore crossbow-cart sequence, without chat reports or remote services. */
public final class XbowCartModule extends Module {
    private final BooleanSetting safeMode = new BooleanSetting(EncryptedString.of("Safe Xbow"), true);
    private final NumberSetting fireAngle = new NumberSetting(EncryptedString.of("Fire Angle"), 25.0, 85.0, 55.0, 5.0);
    private final NumberSetting stepDelay = new NumberSetting(EncryptedString.of("Step Delay (ticks)"), 0.0, 20.0, 0.0, 1.0);
    private final ButtonSetting usage;

    public XbowCartModule() {
        super(EncryptedString.of("Xbow Cart"), EncryptedString.of("Geladene Armbrust in der Hotbar; Schiene, TNT-Lore und Feuerzeug oder Lavaeimer dabeihaben. Rechtsklick auf einen Block startet."), -1, Category.COMBAT);
        this.safeMode.setDescription(EncryptedString.of("Verwendet während des Ablaufs optional eine Slab, wenn eine in der Hotbar liegt."));
        this.fireAngle.setDescription(EncryptedString.of("Kleinere Winkel zünden weiter vorne. Anpassen, wenn das Feuer zu nah gesetzt wird."));
        this.stepDelay.setDescription(EncryptedString.of("Wartezeit zwischen Schiene, TNT-Lore und Zündung in Spielticks."));
        this.usage = new ButtonSetting(EncryptedString.of("Anleitung"), () -> {
            if (this.mc.field_1724 != null) {
                this.mc.field_1724.method_7353(class_2561.method_43470("Xbow Cart: Modul aktivieren. Eine geladene Armbrust muss in der Hotbar liegen. Schiene, TNT-Lore und Feuerzeug oder Lavaeimer ebenfalls bereithalten. H wählt eine geladene Armbrust aus; danach auf den Zielblock rechtsklicken. Die Taste H kannst du unter Steuerung ändern."), false);
            }
        });
        this.usage.setDescription(EncryptedString.of("Zeigt Voraussetzungen und Bedienung im Chat."));
        this.addSettings(this.safeMode, this.fireAngle, this.stepDelay, this.usage);
    }

    @Override
    public void onEnable() {
        CartcoreCore.cartConfig.safeXbow = this.safeMode.getValue();
        CartcoreCore.cartConfig.fireAngle = this.fireAngle.getIntValue();
        CartcoreCore.cartConfig.crossbowStepDelay = this.stepDelay.getIntValue();
        CartcoreCore.cartConfig.crossbowCarting = true;
        CartcoreCore.cartConfig.save();
    }

    @Override
    public void onDisable() {
        CartcoreCore.cartConfig.crossbowCarting = false;
        CartcoreCore.releaseCartAim(this.mc);
        thetruetrident.cartcore.crossCartin.resetSequence(this.mc);
        CartcoreCore.cartConfig.save();
    }

    @EventListener
    public void onTick(TickEvent event) {
        CartcoreCore.cartConfig.safeXbow = this.safeMode.getValue();
        CartcoreCore.cartConfig.fireAngle = this.fireAngle.getIntValue();
        CartcoreCore.cartConfig.crossbowStepDelay = this.stepDelay.getIntValue();
    }
}
