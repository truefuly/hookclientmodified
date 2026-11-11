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
        super(EncryptedString.of("Xbow Cart"), EncryptedString.of("Needs a loaded crossbow, rail, TNT minecart and flint and steel or lava bucket in the hotbar. Right-click a block to start."), -1, Category.COMBAT);
        this.safeMode.setDescription(EncryptedString.of("Uses a slab during the sequence if one is in the hotbar."));
        this.fireAngle.setDescription(EncryptedString.of("Lower angles ignite further away. Adjust if the fire is placed too close."));
        this.stepDelay.setDescription(EncryptedString.of("Delay between rail, TNT minecart and ignition in ticks."));
        this.usage = new ButtonSetting(EncryptedString.of("Anleitung"), () -> {
            if (this.mc.field_1724 != null) {
                this.mc.field_1724.method_7353(class_2561.method_43470("Xbow Cart: enable the module and keep a loaded crossbow, a rail, a TNT minecart and flint and steel or a lava bucket in your hotbar. Press H to select a loaded crossbow, then right-click the target block. You can rebind H in Controls."), false);
            }
        });
        this.usage.setDescription(EncryptedString.of("Shows requirements and usage in chat."));
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
