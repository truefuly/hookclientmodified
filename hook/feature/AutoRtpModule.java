/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_419
 */
package hook.feature;

import hook.Hook;
import hook.event.EventListener;
import hook.event.events.SetScreenEvent;
import hook.event.events.TickEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.StringSetting;
import hook.utils.EncryptedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_419;

public final class AutoRtpModule
extends Module {
    private static final List<String> REGIONS = List.of("rtp asia", "rtp eu central", "rtp eu west", "rtp eu oceania", "rtp east", "rtp west");
    private final StringSetting command;
    private final NumberSetting delay;
    private final BooleanSetting randomRegion;
    private final BooleanSetting disableOnBypassFlag;
    private final List<String> queue = new ArrayList<String>();
    private long lastSent;

    public AutoRtpModule() {
        super(EncryptedString.of("Auto RTP"), EncryptedString.of("Runs /rtp on a timer and cooperates with Player Bypass"), -1, Category.DONUT);
        this.command = new StringSetting(EncryptedString.of("Command"), "/rtp").setDescription(EncryptedString.of("Command sent on each interval when Random Region is off"));
        this.delay = new NumberSetting(EncryptedString.of("Delay"), 0.05, 10.0, 1.0, 0.05).getValue(EncryptedString.of("Seconds between RTP commands"));
        this.randomRegion = new BooleanSetting(EncryptedString.of("Random Region"), false).setDescription(EncryptedString.of("Cycles through every Donut RTP region in a shuffled order"));
        this.disableOnBypassFlag = new BooleanSetting(EncryptedString.of("Disable On Bypass Flag"), true).setDescription(EncryptedString.of("Turns Auto RTP off on Player Bypass flags; any kick also disables Auto RTP"));
        this.addSettings(this.command, this.delay, this.randomRegion, this.disableOnBypassFlag);
    }

    @Override
    public void onEnable() {
        this.lastSent = 0L;
        this.queue.clear();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.queue.clear();
        super.onDisable();
    }

    @EventListener
    public void onTick(TickEvent event) {
        String nextCommand;
        long delayMillis;
        if (this.mc.field_1724 == null || this.mc.method_1562() == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastSent - (delayMillis = (long)(this.delay.getValue() * 1000.0)) < 0L) {
            return;
        }
        if (this.randomRegion.getValue()) {
            if (this.queue.isEmpty()) {
                this.queue.addAll(REGIONS);
                Collections.shuffle(this.queue);
            }
            nextCommand = this.queue.removeFirst();
        } else {
            nextCommand = AutoRtpModule.normalize(this.command.getValue());
        }
        if (!nextCommand.isBlank()) {
            this.lastSent = now;
            this.mc.method_1562().method_45730(nextCommand);
        }
    }

    public void onBypassFlag() {
        if (this.isEnabled() && this.disableOnBypassFlag.getValue()) {
            this.toggle(false);
            if (this.mc.field_1724 != null) {
                this.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)"\u00a7e[Auto RTP] \u00a7fDisabled because Player Bypass detected a flag."), false);
            }
            Hook.INSTANCE.getSettingsManager().saveNow();
        }
    }

    @EventListener
    public void onDisconnectedScreen(SetScreenEvent event) {
        if (event.screen instanceof class_419) {
            this.toggle(false);
            Hook.INSTANCE.getSettingsManager().saveNow();
        }
    }

    private static String normalize(String command) {
        if (command == null) {
            return "";
        }
        command = command.trim();
        while (command.startsWith("/")) {
            command = command.substring(1).trim();
        }
        return command;
    }
}

