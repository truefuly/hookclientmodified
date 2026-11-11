package hook.feature;

import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.utils.EncryptedString;
import thetruetrident.cartcore.CartcoreClient;
import thetruetrident.cartcore.CartcoreCore;

/** CartCore bow insta-cart sequence, without chat reports or remote services. */
public final class InstaCartModule extends Module {
    private final BooleanSetting cartOverloading = new BooleanSetting(EncryptedString.of("Cart Overloading"), true);

    public InstaCartModule() {
        super(EncryptedString.of("Insta Cart"), EncryptedString.of("Runs CartCore's bow insta-cart flow while enabled."), -1, Category.COMBAT);
        this.addSetting(this.cartOverloading);
    }

    @Override
    public void onEnable() {
        CartcoreCore.cartConfig.cartOverloading = this.cartOverloading.getValue();
        CartcoreCore.cartConfig.instaCarting = true;
        CartcoreCore.cartConfig.save();
    }

    @Override
    public void onDisable() {
        CartcoreCore.cartConfig.instaCarting = false;
        CartcoreClient.rightClickRail = -1;
        CartcoreClient.shotBow = false;
        CartcoreClient.useStartBoolean = false;
        CartcoreClient.useStartTicker = -1;
        CartcoreCore.releaseCartAim(this.mc);
        CartcoreCore.cartConfig.save();
    }

    @EventListener
    public void onTick(TickEvent event) {
        CartcoreCore.cartConfig.cartOverloading = this.cartOverloading.getValue();
    }
}
