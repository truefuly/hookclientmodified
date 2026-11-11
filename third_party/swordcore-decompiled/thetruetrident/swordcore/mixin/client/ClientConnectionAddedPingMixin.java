/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelFutureListener
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2535
 *  net.minecraft.class_2596
 *  net.minecraft.class_310
 *  net.minecraft.class_5321
 *  net.minecraft.class_638
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.gen.Invoker
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package thetruetrident.swordcore.mixin.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thetruetrident.swordcore.SwordcoreClient;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_2535.class})
public abstract class ClientConnectionAddedPingMixin {
    private static final ThreadLocal<Boolean> SWORDCORE_SENDING_DELAYED_PACKET = ThreadLocal.withInitial(() -> false);
    @Shadow
    private Channel field_11651;

    @Invoker(value="method_10764")
    protected abstract void swordcore$sendImmediately(class_2596<?> var1, ChannelFutureListener var2, boolean var3);

    @Inject(method={"method_10764"}, at={@At(value="HEAD")}, cancellable=true)
    private void swordcore$delayOutgoingPacket(class_2596<?> packet, ChannelFutureListener callbacks, boolean flush, CallbackInfo ci) {
        int addedPing = SwordcoreClient.getAddedPingMillis();
        if (addedPing <= 0 || Boolean.TRUE.equals(SWORDCORE_SENDING_DELAYED_PACKET.get()) || this.field_11651 == null || !this.field_11651.isOpen() || class_310.method_1551().field_1687 == null) {
            return;
        }
        class_638 worldAtSend = class_310.method_1551().field_1687;
        class_5321 dimensionAtSend = worldAtSend.method_27983();
        ci.cancel();
        this.field_11651.eventLoop().schedule(() -> {
            class_638 currentWorld = class_310.method_1551().field_1687;
            if (currentWorld != worldAtSend || !currentWorld.method_27983().equals(dimensionAtSend)) {
                return;
            }
            SWORDCORE_SENDING_DELAYED_PACKET.set(true);
            try {
                this.swordcore$sendImmediately(packet, callbacks, flush);
            }
            finally {
                SWORDCORE_SENDING_DELAYED_PACKET.set(false);
            }
        }, (long)addedPing, TimeUnit.MILLISECONDS);
    }
}

