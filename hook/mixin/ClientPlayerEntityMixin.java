/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.class_310
 *  net.minecraft.class_638
 *  net.minecraft.class_742
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import com.mojang.authlib.GameProfile;
import hook.event.events.SendMovementPacketsEvent;
import hook.event.events.TickEvent;
import hook.manager.EventManager;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_746.class})
public class ClientPlayerEntityMixin
extends class_742 {
    @Shadow
    @Final
    protected class_310 field_3937;

    public ClientPlayerEntityMixin(class_638 world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method={"sendMovementPackets"}, at={@At(value="HEAD")})
    private void onSendMovementPackets(CallbackInfo ci) {
        EventManager.b(new SendMovementPacketsEvent());
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void onPlayerTick(CallbackInfo ci) {
        EventManager.b(new TickEvent());
    }
}

