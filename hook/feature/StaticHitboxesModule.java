/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_4050
 */
package hook.feature;

import hook.event.EventListener;
import hook.event.events.TargetPoseEvent;
import hook.module.Category;
import hook.module.Module;
import hook.utils.EncryptedString;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_4050;

public final class StaticHitboxesModule
extends Module {
    public StaticHitboxesModule() {
        super(EncryptedString.of("Static HitBoxes"), EncryptedString.of("Expands a Player's Hitbox"), -1, Category.COMBAT);
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
    public void onTargetPose(TargetPoseEvent event) {
        class_1657 player;
        class_1297 class_12972;
        if (this.isEnabled() && (class_12972 = event.entity) instanceof class_1657 && !(player = (class_1657)class_12972).method_7340()) {
            event.cir.setReturnValue(class_4050.field_18076);
        }
    }
}

