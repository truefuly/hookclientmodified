/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.minecraft.class_1657
 *  net.minecraft.class_1802
 *  net.minecraft.class_2374
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 *  net.minecraft.class_490
 *  org.lwjgl.glfw.GLFW
 */
package thetruetrident.cartcore;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2374;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3675;
import thetruetrident.cartcore.C;
import thetruetrident.cartcore.CartcoreCore;
import thetruetrident.cartcore.crossCartin;

@Environment(value=EnvType.CLIENT)
public class CartcoreClient
implements ClientModInitializer {
    public static int rightClickRail = -1;
    public static boolean shotBow = false;
    public static boolean flopperFish = true;
    public static boolean findCart = false;
    public static int replenishWithDelay = -1;
    public static int useStartTicker = -1;
    public static boolean msgSent = false;
    public static boolean useStartBoolean = false;
    public static boolean underBoolean = false;
    public static boolean replenishStart = false;
    private static boolean A;

    public void onInitializeClient() {
        new C();
        ClientTickEvents.START_CLIENT_TICK.register(class_3102 -> {
            if (class_3102.field_1687 == null || class_3102.field_1724 == null || class_3102.field_1755 != null) return;
            if (!CartcoreCore.cartConfig.instaCarting) {
                return;
            }
            if (--rightClickRail > 0 && class_3102.field_1692 == null) {
                CartcoreCore.holdCartAim(class_3102, rightClickRail + 2);
                class_3675.class_306 class_3062 = class_3675.class_307.field_1672.method_1447(1);
                class_304.method_1420((class_3675.class_306)class_3062);
                class_304.method_1416((class_3675.class_306)class_3062, (boolean)false);
                if (rightClickRail < 3) {
                    if (!CartcoreCore.swapSlot((class_1657)class_3102.field_1724, "tnt_minecart")) {
                        if (class_3102.field_1724.method_6079().method_7909() == class_1802.field_8069) {
                            CartcoreCore.swapSlot((class_1657)class_3102.field_1724, "_sword");
                            if (!CartcoreCore.cartConfig.cartOverloading) {
                                rightClickRail = 1;
                            }
                        } else {
                            CartcoreClient.checkIfReplenishNeeded(class_3102);
                        }
                    } else if (!CartcoreCore.cartConfig.cartOverloading) {
                        rightClickRail = 1;
                    }
                }
                if (rightClickRail <= 2 && class_3102.field_1724.method_6079().method_7909() != class_1802.field_8069 && !CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "tnt_minecart")) {
                    CartcoreClient.checkIfReplenishNeeded(class_3102);
                }
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(class_3102 -> {
            if (CartcoreCore.targetPitch != null && CartcoreCore.targetPitch > 90) {
                CartcoreCore.targetPitch = 90;
            }
            if (!CartcoreCore.cartConfig.instaCarting) {
                return;
            }
            if (class_3102.field_1687 == null) {
                return;
            }
            if (CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724) < 12.0f) {
                shotBow = false;
                return;
            }
            net.minecraft.class_2338 targeted = CartcoreCore.getTargetedBlockPos();
            if (targeted == null) return;
            double d = targeted.method_19770((class_2374)class_3102.field_1724.method_33571());
            if (!CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "rail")) {
                shotBow = false;
                return;
            }
            if (!CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "tnt_minecart") && class_3102.field_1724.method_6079().method_7909() != class_1802.field_8069) {
                shotBow = false;
                return;
            }
            if (useStartBoolean) {
                ++useStartTicker;
                CartcoreCore.holdCartAim(class_3102, 4);
            }
            int n = 6;
            if (!CartcoreCore.checkIfPlaceAbove(class_3102)) {
                n = 4;
                if (underBoolean) {
                    n = 2;
                }
            }
            if (CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724) > 27.0f) {
                if (useStartTicker >= 3) {
                    class_3675.class_306 class_3062 = class_3675.class_307.field_1672.method_1447(1);
                    class_304.method_1416((class_3675.class_306)class_3062, (boolean)false);
                    useStartBoolean = false;
                    useStartTicker = 0;
                    CartcoreCore.holdCartAim(class_3102, 8);
                    CartcoreCore.playSoundSlow((class_3414)class_3417.field_15001.comp_349(), class_3102, 2.0f);
                }
            } else if (useStartTicker == n) {
                class_3675.class_306 class_3063 = class_3675.class_307.field_1672.method_1447(1);
                class_304.method_1416((class_3675.class_306)class_3063, (boolean)false);
                useStartBoolean = false;
                useStartTicker = 0;
                CartcoreCore.holdCartAim(class_3102, 8);
                CartcoreCore.playSoundSlow((class_3414)class_3417.field_15001.comp_349(), class_3102, 2.0f);
            }
            if (class_3102.field_1724.method_6047().method_7909() == class_1802.field_8102) {
                if (class_3102.field_1690.field_1904.method_1434()) {
                    if (!useStartBoolean) {
                        useStartBoolean = true;
                        useStartTicker = 0;
                        CartcoreCore.holdCartAim(class_3102, 8);
                        CartcoreCore.playSoundSlow(class_3417.field_19196, class_3102, 0.5f);
                        A = false;
                        if (CartcoreCore.isBlockInFrontOfFeet((class_1657)class_3102.field_1724)) {
                            if (CartcoreCore.checkIfPlaceAbove(class_3102)) {
                                CartcoreCore.targetPitch = 17;
                            } else {
                                if (d > 9.0) {
                                    CartcoreCore.targetPitch = (int)(CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724) - 15.0f);
                                } else if (d < 5.0) {
                                    CartcoreCore.targetPitch = (int)(CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724) - 10.0f);
                                    CartcoreCore.pitchSpeed = 15.0f;
                                    underBoolean = true;
                                }
                                A = true;
                            }
                        }
                    }
                } else {
                    useStartBoolean = false;
                }
            }
            if (shotBow) {
                CartcoreCore.holdCartAim(class_3102, 10);
                if (CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "rail")) {
                    if (!CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "tnt_minecart") && class_3102.field_1724.method_6079().method_7909() != class_1802.field_8069) {
                        shotBow = false;
                        return;
                    }
                    rightClickRail = 5;
                    CartcoreCore.holdCartAim(class_3102, 10);
                    if (A) {
                        CartcoreCore.targetPitch = (int)(CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724) + 15.0f);
                        CartcoreCore.pitchSpeed = 20.0f;
                        A = false;
                        if (d > 9.0) {
                            rightClickRail = 6;
                        }
                        if (underBoolean) {
                            CartcoreCore.targetPitch = (int)(CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724) + 10.0f);
                            CartcoreCore.pitchSpeed = 10.0f;
                            underBoolean = false;
                        }
                    } else if (!CartcoreCore.isBlockInFrontOfFeet((class_1657)class_3102.field_1724)) {
                        CartcoreCore.targetPitch = (int)(CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724) + 10.0f);
                        CartcoreCore.pitchSpeed = 10.0f;
                    } else if (CartcoreCore.checkIfPlaceAbove(class_3102)) {
                        CartcoreCore.targetPitch = (int)(CartcoreCore.getPhysicalPitch((class_1657)class_3102.field_1724) + 5.0f);
                    }
                    CartcoreCore.swapSlot((class_1657)class_3102.field_1724, "rail");
                    shotBow = false;
                } else {
                    shotBow = false;
                }
            }
        });
    }

    public static void checkIfReplenishNeeded(class_310 class_3102) {
        if (class_3102 == null || class_3102.field_1724 == null) {
            return;
        }
        if (!flopperFish || !CartcoreCore.cartConfig.cartReplenishing) {
            return;
        }
        if (CartcoreCore.checkIfInHotbar((class_1657)class_3102.field_1724, "tnt_minecart")) {
            return;
        }
        if (!CartcoreCore.checkIfInInventory((class_1657)class_3102.field_1724, "tnt_minecart")) {
            return;
        }
        if (!replenishStart && replenishWithDelay < 0) {
            replenishWithDelay = 12;
        }
    }

}

