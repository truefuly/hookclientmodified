/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelFutureListener
 *  io.netty.channel.ChannelHandlerContext
 *  net.minecraft.class_2535
 *  net.minecraft.class_2596
 *  net.minecraft.class_2598
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hook.mixin;

import hook.module.ext.ExtBridge;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import net.minecraft.class_2598;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_2535.class})
public abstract class ExtConnectionMixin {
    @Shadow
    public abstract class_2598 method_36121();

    @Inject(method={"channelRead0"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$extPacketReceive(ChannelHandlerContext var1, class_2596<?> var2, CallbackInfo var3) {
        if (this.method_36121() == class_2598.field_11942 && ExtBridge.packetReceive(var2)) {
            var3.cancel();
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void hook$extPacketSend(class_2596<?> var1, ChannelFutureListener var2, boolean var3, CallbackInfo var4) {
        if (this.method_36121() == class_2598.field_11942 && ExtBridge.packetSend(var1)) {
            var4.cancel();
        }
    }
}

