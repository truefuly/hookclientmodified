package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_1656;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_746;

/**
 * Creative helpers: your own fly speed, stopping dead when you let go of the keys, and "Fly Through" - flying through
 * blocks while staying in creative (build, place, break as normal).
 *
 * Fly Through is singleplayer only: a server moves every non-spectator player back out of any block they fly into
 * (vanilla checks each move for new block collisions; creative is not excepted), so on a server it can't work.
 */
public final class CreativeModule extends Module {
    private static CreativeModule instance;
    private static final float VANILLA_FLY = 0.05f;

    private final NumberSetting flySpeed = new NumberSetting(EncryptedString.of("Fly Speed"), 0.25, 5.0, 1.0, 0.25);
    private final BooleanSetting flyThrough = new BooleanSetting(EncryptedString.of("Fly Through Blocks"), true);
    private final BooleanSetting instantStop = new BooleanSetting(EncryptedString.of("Stop Instantly"), false);
    private final BooleanSetting noOverlay = new BooleanSetting(EncryptedString.of("See Through While Inside"), true);

    private boolean speedSet, cullOff, warned;

    public CreativeModule() {
        super(EncryptedString.of("Creative"), EncryptedString.of("Creative mode settings: fly speed, stop instantly, and fly through blocks while building (singleplayer)."), -1, Category.MISC);
        this.flySpeed.setDescription(EncryptedString.of("Creative flying speed. 1 = normal, 2 = twice as fast. Sprinting still doubles it."));
        this.flyThrough.setDescription(EncryptedString.of("While flying in creative you go straight through blocks - no spectator, you can still place and break. Singleplayer only: servers push every non-spectator back out of blocks."));
        this.instantStop.setDescription(EncryptedString.of("Flying: you stop right away when you let go of the movement keys instead of gliding on."));
        this.noOverlay.setDescription(EncryptedString.of("Inside blocks: keeps the world around you visible (turns off cave culling while your head is in a block)."));
        this.addSettings(this.flySpeed, this.flyThrough, this.instantStop, this.noOverlay);
        instance = this;
    }

    private static boolean singleplayer() {
        class_310 mc = class_310.method_1551();
        return mc.method_1576() != null && mc.method_1576().method_3724();
    }

    /**
     * Called from Player.tick for every player (client and the integrated server): true = this player flies through
     * blocks this tick. Only our own player, only creative + flying, only in singleplayer.
     */
    public static boolean noclip(class_1657 p) {
        CreativeModule m = instance;
        if (m == null || !m.isEnabled() || !m.flyThrough.getValue()) return false;
        class_1656 a = p.method_31549();
        if (!a.field_7477 || !a.field_7479) return false; // creative (instabuild) and flying
        if (!singleplayer()) return false;
        class_746 me = class_310.method_1551().field_1724;
        return me != null && (p == me || p.method_5667().equals(me.method_5667()));
    }

    @Override
    public void onDisable() {
        class_746 p = this.mc.field_1724;
        if (p != null && this.speedSet) p.method_31549().method_7248(VANILLA_FLY);
        this.speedSet = false;
        if (this.cullOff) { this.mc.field_1730 = true; this.cullOff = false; }
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        class_746 p = this.mc.field_1724;
        if (p == null) return;
        class_1656 a = p.method_31549();
        boolean creative = a.field_7477;
        // fly speed (the server re-sends abilities now and then, so keep setting it)
        if (creative) {
            float want = VANILLA_FLY * this.flySpeed.getFloatValue();
            if (Math.abs(a.method_7252() - want) > 1.0e-4f) a.method_7248(want);
            this.speedSet = true;
        } else if (this.speedSet) {
            a.method_7248(VANILLA_FLY);
            this.speedSet = false;
        }
        // stop instantly: no glide when no movement key is held
        if (creative && a.field_7479 && this.instantStop.getValue()) {
            boolean moving = this.mc.field_1690.field_1894.method_1434() || this.mc.field_1690.field_1881.method_1434()
                || this.mc.field_1690.field_1913.method_1434() || this.mc.field_1690.field_1849.method_1434();
            boolean vertical = this.mc.field_1690.field_1903.method_1434() || this.mc.field_1690.field_1832.method_1434();
            class_243 v = p.method_18798();
            p.method_18800(moving ? v.field_1352 : 0.0, vertical ? v.field_1351 : 0.0, moving ? v.field_1350 : 0.0);
        }
        // fly through on a server: say once why it does nothing there
        if (this.flyThrough.getValue() && creative && a.field_7479 && !singleplayer()) {
            if (!this.warned) {
                this.warned = true;
                p.method_7353(class_2561.method_43470("§eCreative: §fFly Through only works in singleplayer - servers push you back out of blocks."), true);
            }
        } else if (!a.field_7479) this.warned = false;
        // head inside a solid block: turn cave culling off so the world stays visible (like spectator)
        boolean inside = noclip(p) && this.noOverlay.getValue() && this.mc.field_1687 != null
            && this.mc.field_1687.method_8320(net.minecraft.class_2338.method_49638(p.method_33571())).method_26216();
        if (inside && !this.cullOff) { this.cullOff = this.mc.field_1730; this.mc.field_1730 = false; }
        else if (!inside && this.cullOff) { this.mc.field_1730 = true; this.cullOff = false; }
    }
}
