/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TargetMarginEvent;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.utils.EncryptedString;
import net.minecraft.class_1657;

public final class HitboxModule
extends Module {
    private final NumberSetting expand = new NumberSetting(EncryptedString.of("Expand"), 0.0, 2.0, 0.5, 0.05).getValue(EncryptedString.of("How many blocks other players' hitboxes grow"));

    public HitboxModule() {
        super(EncryptedString.of("HitBox"), EncryptedString.of("Makes other players easier to hit by growing their hitbox"), -1, Category.COMBAT);
        this.addSettings(this.expand);
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
    public void onTargetMargin(TargetMarginEvent event) {
        if (event.entity instanceof class_1657) {
            event.cir.setReturnValue(Float.valueOf((float)this.expand.getValue()));
        }
    }

}

