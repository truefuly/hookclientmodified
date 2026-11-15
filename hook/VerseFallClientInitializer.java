package hook;

import com.spotifyclient.SpotifyModClient;
import com.spotifyclient.module.Binds;
import net.fabricmc.api.ClientModInitializer;

/** Initializes VerseFall's visuals and HUD but disables its separate click GUI entry key. */
public final class VerseFallClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new SpotifyModClient().onInitializeClient();
        new thetruetrident.cartcore.CartcoreClient().onInitializeClient();
        new thetruetrident.cartcore.crossCartin().onInitializeClient();
        var modules = hook.Hook.INSTANCE.getModuleManager();
        var xbow = modules.getModuleByClass(hook.feature.XbowCartModule.class);
        var insta = modules.getModuleByClass(hook.feature.InstaCartModule.class);
        // the old CartCore crossbow logic stays off: Xbow Cart / Bow Cart replace it (its use-block hook blocked their clicks)
        thetruetrident.cartcore.CartcoreCore.cartConfig.crossbowCarting = false;
        thetruetrident.cartcore.CartcoreCore.cartConfig.instaCarting = insta != null && insta.isEnabled();
        thetruetrident.cartcore.CartcoreCore.cartConfig.save();
        Binds.openClickGui = () -> {};
        hook.feature.SwordcoreModuleSupport.initialize();
        if (hook.feature.AutoBreedTest.ENABLED)
            net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(c -> hook.feature.AutoBreedTest.tick());
    }
}
