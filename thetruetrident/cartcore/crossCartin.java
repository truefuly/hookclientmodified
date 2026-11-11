/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.event.player.UseBlockCallback
 *  net.minecraft.class_1269
 *  net.minecraft.class_1657
 *  net.minecraft.class_1764
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_243
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_3417
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_3675$class_307
 */
package thetruetrident.cartcore;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3417;
import net.minecraft.class_3675;
import thetruetrident.cartcore.C;
import thetruetrident.cartcore.CartcoreClient;
import thetruetrident.cartcore.CartcoreCore;

@Environment(value=EnvType.CLIENT)
public class crossCartin
implements ClientModInitializer {
    public static int crossbowTimer = -1;
    public static int pitchMemory = 1;
    public static int safeXbowOriginalPitch = 1;
    public static class_243 blockPosMem = null;
    public static boolean slabBoolean = false;
    public static int acquireSlabDelay = -1;
    public static boolean ejacBoolean = false;
    public static boolean secondSlabBoolean = false;
    public static int crossbowRecoverDelay = -1;
    private static int C = -1;
    private static int crossbowStepDelay = 0;
    private static boolean B = false;
    private static boolean A = false;

    public static boolean isSequenceActive() {
        return crossbowTimer >= 0 || blockPosMem != null || ejacBoolean || C >= 0 || crossbowRecoverDelay >= 0 || slabBoolean || secondSlabBoolean;
    }

    private static void A(class_3675.class_306 class_3062) {
        class_304.method_1420((class_3675.class_306)class_3062);
        class_304.method_1416((class_3675.class_306)class_3062, (boolean)false);
    }

    private static void B(class_310 class_3102, class_3675.class_306 class_3062) {
        if (CartcoreCore.swapChargedCrossbowSlot((class_1657)class_3102.field_1724)) {
            float f = CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724);
            class_3102.field_1724.method_36457(Math.max(-90.0f, f - 3.0f));
            crossCartin.A(class_3062);
            CartcoreCore.releaseCartAim(class_3102);
        } else {
            CartcoreCore.releaseCartAim(class_3102);
        }
        ejacBoolean = false;
        C = -1;
    }

    private static void B() {
        slabBoolean = false;
        secondSlabBoolean = false;
        safeXbowOriginalPitch = 1;
        crossbowRecoverDelay = -1;
    }

    public static void resetSequence(class_310 client) {
        crossCartin.A();
        B = false;
        A = false;
        CartcoreCore.releaseCartAim(client);
    }

    private static void A() {
        crossbowTimer = -1;
        crossbowStepDelay = 0;
        blockPosMem = null;
        ejacBoolean = false;
        C = -1;
        crossbowRecoverDelay = -1;
        crossCartin.B();
    }

    private static void C() {
        crossbowRecoverDelay = 2;
    }

    private static void A(class_310 class_3102, class_3675.class_306 class_3062) {
        if (!ejacBoolean) {
            return;
        }
        if (C < 0) {
            C = 1;
        }
        if (C > 0) {
            --C;
            return;
        }
        if (CartcoreCore.targetPitch != null) {
            CartcoreCore.releaseCartAim(class_3102);
        }
        if (ejacBoolean) {
            crossCartin.B(class_3102, class_3062);
            crossCartin.B();
        }
    }

    private static boolean A(class_1657 class_16572) {
        if (class_16572 == null) {
            return false;
        }
        return crossCartin.A(class_16572.method_6047()) || crossCartin.A(class_16572.method_6079());
    }

    private static boolean A(class_1799 class_17992) {
        return class_17992 != null && class_17992.method_7909() instanceof class_1764 && class_1764.method_7781((class_1799)class_17992);
    }

    private static void A(class_310 class_3102) {
        if (class_3102 == null || class_3102.field_1690 == null) {
            return;
        }
        if (!thetruetrident.cartcore.C.A.method_1434() && !class_3102.field_1690.field_1904.method_1434()) {
            B = false;
        }
    }

    public void onInitializeClient() {
        UseBlockCallback.EVENT.register((class_16572, class_19372, class_12682, class_39652) -> {
            class_310 class_3102 = class_310.method_1551();
            crossCartin.A(class_3102);
            if (class_3102.field_1724 == null || class_3102.field_1687 == null || !CartcoreCore.cartConfig.crossbowCarting) {
                return class_1269.field_5811;
            }
            class_2338 class_23382 = class_39652.method_17777();
            if (CartcoreCore.checkIfCrossbowLoadedInHotbar((class_1657)class_3102.field_1724)) {
                boolean bl;
                boolean bl2 = thetruetrident.cartcore.C.A.method_1434() && crossbowTimer < 0;
                boolean bl3 = bl = crossCartin.A((class_1657)class_3102.field_1724) && crossbowTimer < 0;
                // A prior keybind tap only selects the charged crossbow; it must not block the subsequent block click.
                if (crossbowTimer < 0 && (bl2 || bl)) {
                    if (!CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "rail")) {
                        return class_1269.field_5811;
                    }
                    if (!CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "tnt_minecart")) {
                        return class_1269.field_5811;
                    }
                    if (!CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "flint_and_steel") && !CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "lava_bucket")) {
                        return class_1269.field_5811;
                    }
                    CartcoreCore.playSoundSlow(class_3417.field_14700, class_3102, 0.5f);
                    CartcoreCore.playSoundSlow(class_3417.field_47724, class_3102, 2.0f);
                    CartcoreClient.checkIfReplenishNeeded(class_3102);
                    crossbowTimer = 5;
                    crossbowStepDelay = 0;
                    B = true;
                    CartcoreCore.holdCartAim(class_3102, 10);
                    if (class_3102.field_1724.method_6047().method_7909() instanceof class_1764) {
                        return class_1269.field_5814;
                    }
                    if (class_3102.field_1724.method_6079().method_7909() instanceof class_1764) {
                        return class_1269.field_5814;
                    }
                }
            }
            return class_1269.field_5811;
        });
        ClientTickEvents.START_CLIENT_TICK.register(class_3102 -> {
            if (!CartcoreCore.cartConfig.crossbowCarting || class_3102.field_1724 == null || class_3102.field_1687 == null) {
                return;
            }
            crossCartin.A(class_3102);
            class_3675.class_306 class_3062 = class_3675.class_307.field_1672.method_1447(1);
            boolean bl = thetruetrident.cartcore.C.A.method_1434();
            if (bl && !A && !B && CartcoreCore.checkIfCrossbowLoadedInHotbar((class_1657)class_3102.field_1724)) {
                CartcoreCore.holdCartAim(class_3102, 4);
                CartcoreCore.swapChargedCrossbowSlot((class_1657)class_3102.field_1724);
                crossCartin.A(class_3062);
                B = true;
            }
            A = bl;
            --acquireSlabDelay;
            if (crossbowRecoverDelay == 0) {
                CartcoreCore.swapChargedCrossbowSlot((class_1657)class_3102.field_1724);
                crossbowRecoverDelay = -1;
            } else if (crossbowRecoverDelay > 0) {
                --crossbowRecoverDelay;
            }
            if (acquireSlabDelay == 0) {
                CartcoreCore.swapSlot((class_1657)class_3102.field_1724, "_slab");
                CartcoreCore.playSoundSlow(class_3417.field_26966, class_3102, 2.0f);
            }
            if (class_3102.field_1687 != null) {
                crossCartin.A(class_3102, class_3062);
                if (crossbowTimer >= 3 && crossbowStepDelay > 0) {
                    --crossbowStepDelay;
                    return;
                }
                if (blockPosMem != null) {
                    CartcoreCore.holdCartAim(class_3102, 6);
                    class_2338 targetedBlock = CartcoreCore.getTargetedBlockPos();
                    if (targetedBlock != null && !targetedBlock.method_46558().equals((Object)blockPosMem)) {
                        if (class_3102.field_1687.method_8320(targetedBlock).method_26204() == class_2246.field_10036) {
                            CartcoreCore.targetPitch = pitchMemory - 5;
                            CartcoreCore.pitchSpeed = 10.0f;
                            blockPosMem = null;
                            crossCartin.B();
                            ejacBoolean = true;
                            C = 1;
                            crossbowTimer = Math.min(crossbowTimer, 0);
                        }
                        if (blockPosMem != null) {
                            crossCartin.A(class_3062);
                            if (class_3102.field_1724.method_6047().method_7909() == class_1802.field_8884 || class_3102.field_1724.method_6047().method_7909() == class_1802.field_8187) {
                                crossCartin.C();
                            }
                            if (slabBoolean) {
                                CartcoreCore.targetPitch = (int)(CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724) + 30.0f);
                                CartcoreCore.pitchSpeed = 30.0f;
                                acquireSlabDelay = 1;
                                blockPosMem = CartcoreCore.getTargetedBlockPos().method_46558();
                                pitchMemory = safeXbowOriginalPitch;
                                slabBoolean = false;
                                secondSlabBoolean = true;
                                return;
                            }
                            if (secondSlabBoolean) {
                                CartcoreCore.targetPitch = safeXbowOriginalPitch - 10;
                                secondSlabBoolean = false;
                            } else {
                                CartcoreCore.targetPitch = pitchMemory - 5;
                            }
                            CartcoreCore.pitchSpeed = 10.0f;
                            blockPosMem = null;
                            ejacBoolean = true;
                            C = 1;
                        }
                    }
                }
                crossCartin.A(class_3102, class_3062);
                if (crossbowTimer == 5) {
                    CartcoreCore.holdCartAim(class_3102, 8);
                    CartcoreCore.swapSlot((class_1657)class_3102.field_1724, "rail");
                    crossCartin.A(class_3062);
                }
                if (crossbowTimer == 4) {
                    CartcoreCore.holdCartAim(class_3102, 8);
                    if (!CartcoreCore.swapSlot((class_1657)class_3102.field_1724, "tnt_minecart") && class_3102.field_1724.method_6079().method_7909() == class_1802.field_8069) {
                        CartcoreClient.checkIfReplenishNeeded(class_3102);
                        CartcoreCore.swapSlot((class_1657)class_3102.field_1724, "_sword");
                    }
                    crossCartin.A(class_3062);
                }
                if (crossbowTimer == 3) {
                    CartcoreCore.holdCartAim(class_3102, 8);
                    if (!CartcoreCore.swapSlot((class_1657)class_3102.field_1724, "flint_and_steel")) {
                        CartcoreCore.swapSlot((class_1657)class_3102.field_1724, "lava_bucket");
                    }
                    CartcoreCore.targetPitch = CartcoreCore.cartConfig.fireAngle;
                    CartcoreCore.pitchSpeed = 30.0f;
                    if (CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "_slab") && CartcoreCore.cartConfig.safeXbow && CartcoreClient.flopperFish) {
                        slabBoolean = true;
                        secondSlabBoolean = false;
                        safeXbowOriginalPitch = (int)CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724);
                    }
                    class_2338 targetPos = CartcoreCore.getTargetedBlockPos();
                    if (targetPos == null) {
                        crossCartin.resetSequence(class_3102);
                        return;
                    }
                    blockPosMem = targetPos.method_46558();
                    pitchMemory = (int)CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724);
                }
                if (crossbowTimer >= 3) {
                    crossbowStepDelay = CartcoreCore.cartConfig.crossbowStepDelay;
                }
                --crossbowTimer;
            }
        });
    }
}

