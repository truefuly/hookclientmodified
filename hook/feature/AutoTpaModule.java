/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_634
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.MinMaxSetting;
import hook.module.setting.ModeSetting;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_634;

public final class AutoTpaModule
extends Module {
    private final MinMaxSetting delayRange = new MinMaxSetting(EncryptedString.of("Delay"), 1.0, 80.0, 1.0, 10.0, 30.0);
    private final ModeSetting<Mode> mode = new ModeSetting<Mode>(EncryptedString.of("Mode"), Mode.TPAHERE, Mode.class);
    private final StringSetting playerName = new StringSetting(EncryptedString.of("Player"), "DrDonutt");
    private int delayCounter;

    public AutoTpaModule() {
        super(EncryptedString.of("Auto Tpa"), EncryptedString.of("Module that helps you teleport streamers to you"), -1, Category.MISC);
        this.addSettings(this.mode, this.delayRange, this.playerName);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (this.delayCounter > 0) {
            --this.delayCounter;
            return;
        }
        class_634 network = this.mc.method_1562();
        String command = this.mode.getValue().equals((Object)Mode.TPA) ? "tpa " : "tpahere ";
        network.method_45730(command + this.playerName.getValue());
        this.delayCounter = this.delayRange.getRandomIntInRange();
    }

    static enum Mode {
        TPA,
        TPAHERE;

    }
}

