/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10185
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_11910
 *  net.minecraft.class_241
 *  net.minecraft.class_243
 *  net.minecraft.class_304
 *  net.minecraft.class_3532
 *  net.minecraft.class_5498
 *  net.minecraft.class_638
 *  net.minecraft.class_744
 *  net.minecraft.class_746
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package hook.feature;

import hook.Hook;
import hook.event.EventListener;
import hook.event.events.ChunkMarkClosedEvent;
import hook.event.events.KeyEvent;
import hook.event.events.MouseButtonEvent;
import hook.event.events.MouseScrolledEvent;
import hook.event.events.SetScreenEvent;
import hook.event.events.TickEvent;
import hook.feature.FreelookModule;
import hook.halloween.CinematicCamera;
import hook.mixin.InputAccessor;
import hook.module.Category;
import hook.module.Module;
import hook.module.setting.BooleanSetting;
import hook.module.setting.NumberSetting;
import hook.module.setting.Setting;
import hook.utils.EncryptedString;
import hook.utils.KeyUtils;
import hook.utils.Utils;
import net.minecraft.class_10185;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_304;
import net.minecraft.class_3532;
import net.minecraft.class_5498;
import net.minecraft.class_638;
import net.minecraft.class_744;
import net.minecraft.class_746;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class FreecamModule
extends Module {
    private static final double MIN_SPEED = 0.01;
    private static final double MAX_SCROLL_SPEED = 12.0;
    private static final double ACCELERATION = 12.0;
    private static final double SCROLL_STEP = 0.25;
    private final NumberSetting speed;
    private final NumberSetting scrollSensitivity;
    private final BooleanSetting keepWalking;
    private final BooleanSetting keepJumping;
    private final BooleanSetting keepSneaking;
    public final Vector3d currentPosition;
    public final Vector3d previousPosition;
    private final Vector3d smoothedVelocity;
    private class_5498 savedPerspective;
    private class_746 attachedPlayer;
    private class_638 attachedWorld;
    private boolean cameraActive;
    private boolean savedChunkCulling;
    private float savedPlayerYaw;
    private float savedPlayerPitch;
    private class_10185 lockedInput;
    private class_241 lockedMovementVector;
    private boolean lockedSprinting;
    private double movementSpeed;
    private long lastMovementNanos;
    public float yaw;
    public float pitch;
    public float previousYaw;
    public float previousPitch;
    private boolean isMovingForward;
    private boolean isMovingBackward;
    private boolean isMovingRight;
    private boolean isMovingLeft;
    private boolean isMovingUp;
    private boolean isMovingDown;

    public FreecamModule() {
        super(EncryptedString.of("Freecam"), EncryptedString.of("Moves the camera independently from the real player"), -1, Category.RENDER);
        class_241 var33;
        class_10185 var32;
        Vector3d var31;
        Vector3d var30;
        Vector3d var29;
        BooleanSetting var28;
        BooleanSetting var27;
        BooleanSetting var26;
        NumberSetting var25;
        NumberSetting var24;
        String var37 = "Speed";
        EncryptedString var38 = EncryptedString.of(var37);
        double var52 = 0.01;
        double var6 = 50.0;
        double var8 = 1.0;
        double var10 = 0.1;
        this.speed = var24 = new NumberSetting(var38, var52, var6, var8, var10);
        String var39 = "Scroll Sensitivity";
        EncryptedString var40 = EncryptedString.of(var39);
        double var54 = 0.0;
        double var69 = 5.0;
        double var71 = 1.0;
        double var73 = 0.1;
        this.scrollSensitivity = var25 = new NumberSetting(var40, var54, var69, var71, var73);
        String var41 = "Keep Walking";
        EncryptedString var42 = EncryptedString.of(var41);
        boolean var56 = false;
        this.keepWalking = var26 = new BooleanSetting(var42, var56);
        String var43 = "Keep Jumping";
        EncryptedString var44 = EncryptedString.of(var43);
        boolean var57 = false;
        this.keepJumping = var27 = new BooleanSetting(var44, var57);
        String var45 = "Keep Sneaking";
        EncryptedString var46 = EncryptedString.of(var45);
        boolean var58 = false;
        this.keepSneaking = var28 = new BooleanSetting(var46, var58);
        this.currentPosition = var29 = new Vector3d();
        this.previousPosition = var30 = new Vector3d();
        this.smoothedVelocity = var31 = new Vector3d();
        this.lockedInput = var32 = class_10185.field_54098;
        this.lockedMovementVector = var33 = class_241.field_1340;
        int var34 = 5;
        Setting[] var35 = new Setting[var34];
        int var47 = 0;
        NumberSetting var60 = this.speed;
        var35[var47] = var60;
        int var48 = 1;
        NumberSetting var62 = this.scrollSensitivity;
        var35[var48] = var62;
        int var49 = 2;
        BooleanSetting var64 = this.keepWalking;
        var35[var49] = var64;
        int var50 = 3;
        BooleanSetting var66 = this.keepJumping;
        var35[var50] = var66;
        int var51 = 4;
        BooleanSetting var68 = this.keepSneaking;
        var35[var51] = var68;
        this.addSettings(var35);
    }

    @Override
    public void onEnable() {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            FreelookModule var1 = (FreelookModule)Hook.INSTANCE.getModuleManager().getModuleByClass(FreelookModule.class);
            if (var1 != null && var1.isEnabled()) {
                var1.toggle(false);
            }
            this.attachedPlayer = this.mc.field_1724;
            this.attachedWorld = this.mc.field_1687;
            this.cameraActive = true;
            this.savedPerspective = this.mc.field_1690.method_31044();
            this.savedChunkCulling = this.mc.field_1730;
            this.mc.field_1730 = false;
            this.savedPlayerYaw = this.mc.field_1724.method_36454();
            this.savedPlayerPitch = this.mc.field_1724.method_36455();
            this.yaw = this.savedPlayerYaw;
            this.pitch = this.savedPlayerPitch;
            if (this.savedPerspective == class_5498.field_26666) {
                this.yaw += 180.0f;
                this.pitch = -this.pitch;
            }
            class_243 var2 = this.mc.field_1773.method_19418().method_71156();
            Utils.copyVector(this.currentPosition, var2);
            Utils.copyVector(this.previousPosition, var2);
            this.previousYaw = this.yaw;
            this.previousPitch = this.pitch;
            this.smoothedVelocity.zero();
            this.movementSpeed = this.speed.getValue();
            this.lastMovementNanos = System.nanoTime();
            this.lockedInput = this.mc.field_1724.field_3913.field_54155;
            this.lockedMovementVector = this.mc.field_1724.field_3913.method_3128();
            this.lockedSprinting = this.mc.field_1724.method_5624();
            this.isMovingForward = this.mc.field_1690.field_1894.method_1434();
            this.isMovingBackward = this.mc.field_1690.field_1881.method_1434();
            this.isMovingRight = this.mc.field_1690.field_1849.method_1434();
            this.isMovingLeft = this.mc.field_1690.field_1913.method_1434();
            this.isMovingUp = this.mc.field_1690.field_1903.method_1434();
            this.isMovingDown = this.mc.field_1690.field_1832.method_1434();
            this.resetMovementKeys();
            super.onEnable();
        } else {
            this.toggle(false);
        }
    }

    @Override
    public void onDisable() {
        if (this.cameraActive) {
            this.cameraActive = false;
            this.clearCameraInput();
            if (this.savedPerspective != null) {
                this.mc.field_1690.method_31043(this.savedPerspective);
            }
            this.mc.field_1730 = this.savedChunkCulling;
            if (this.mc.field_1724 != null && this.mc.field_1724 == this.attachedPlayer) {
                this.mc.field_1724.field_3913.field_54155 = class_10185.field_54098;
                ((InputAccessor)this.mc.field_1724.field_3913).hook$setMovementVector(class_241.field_1340);
                this.mc.field_1724.method_5660(false);
            }
            this.lockedInput = class_10185.field_54098;
            this.lockedMovementVector = class_241.field_1340;
            this.lockedSprinting = false;
            this.attachedPlayer = null;
            this.attachedWorld = null;
            this.savedPerspective = null;
            this.previousPosition.set((Vector3dc)this.currentPosition);
            this.previousYaw = this.yaw;
            this.previousPitch = this.pitch;
            class_304.method_1424();
            super.onDisable();
        }
    }

    private void resetMovementKeys() {
        this.mc.field_1690.field_1894.method_23481(false);
        this.mc.field_1690.field_1881.method_23481(false);
        this.mc.field_1690.field_1849.method_23481(false);
        this.mc.field_1690.field_1913.method_23481(false);
        this.mc.field_1690.field_1903.method_23481(false);
        this.mc.field_1690.field_1832.method_23481(false);
    }

    private void clearCameraInput() {
        this.isMovingLeft = false;
        this.isMovingRight = false;
        this.isMovingBackward = false;
        this.isMovingForward = false;
        this.isMovingDown = false;
        this.isMovingUp = false;
        this.smoothedVelocity.zero();
    }

    @EventListener
    private void handleSetScreenEvent(SetScreenEvent var1) {
        this.clearCameraInput();
        this.resetMovementKeys();
    }

    @EventListener
    private void handleTickEvent(TickEvent var1) {
        if (this.mc.field_1724 == this.attachedPlayer && this.mc.field_1687 == this.attachedWorld && this.mc.field_1724 != null && this.mc.field_1687 != null) {
            if (!this.mc.field_1690.method_31044().method_31034()) {
                this.mc.field_1690.method_31043(class_5498.field_26664);
            }
        } else {
            this.toggle(false);
        }
    }

    public void applyContinuedPlayerInput(class_744 var1) {
        if (this.isEnabled() && this.cameraActive && this.mc.field_1724 == this.attachedPlayer && this.mc.field_1687 == this.attachedWorld && var1 == this.mc.field_1724.field_3913 && !CinematicCamera.inputLocked()) {
            class_10185 var2;
            var1.field_54155 = var2 = FreecamModule.continuedInput(this.lockedInput, this.keepWalking.getValue(), this.keepJumping.getValue(), this.keepSneaking.getValue(), this.lockedSprinting);
            ((InputAccessor)var1).hook$setMovementVector(this.keepWalking.getValue() ? this.lockedMovementVector : class_241.field_1340);
            this.mc.field_1724.method_5728(var2.comp_3165());
            this.mc.field_1724.method_5660(var2.comp_3164());
        }
    }

    static class_10185 continuedInput(class_10185 var0, boolean var1, boolean var2, boolean var3, boolean var4) {
        return new class_10185(var1 && var0.comp_3159(), var1 && var0.comp_3160(), var1 && var0.comp_3161(), var1 && var0.comp_3162(), var2 && var0.comp_3163(), var3 & var0.comp_3164(), var1 && var4 && !(var3 & var0.comp_3164()));
    }

    public void updateCameraMovement() {
        if (this.isEnabled() && this.cameraActive && this.mc.field_1724 == this.attachedPlayer && this.mc.field_1687 == this.attachedWorld) {
            if (this.mc.field_1755 != null || CinematicCamera.inputLocked()) {
                this.clearCameraInput();
                this.lastMovementNanos = System.nanoTime();
                this.previousPosition.set((Vector3dc)this.currentPosition);
                return;
            }
            this.previousPosition.set((Vector3dc)this.currentPosition);
            this.previousYaw = this.yaw;
            this.previousPitch = this.pitch;
            long var1 = System.nanoTime();
            double var3 = class_3532.method_15350((double)((double)(var1 - this.lastMovementNanos) / 1.0E9), (double)0.001, (double)0.1);
            this.lastMovementNanos = var1;
            double var5 = Math.toRadians(this.yaw);
            double var7 = -Math.sin(var5);
            double var9 = Math.cos(var5);
            double var11 = -Math.cos(var5);
            double var13 = -Math.sin(var5);
            double var15 = class_3532.method_15350((double)this.movementSpeed, (double)0.01, (double)12.0) * 4.0;
            if (this.mc.field_1690.field_1867.method_1434()) {
                var15 *= 2.0;
            }
            double var17 = 0.0;
            double var19 = 0.0;
            double var21 = 0.0;
            if (this.isMovingForward) {
                var17 += var7 * var15;
                var21 += var9 * var15;
            }
            if (this.isMovingBackward) {
                var17 -= var7 * var15;
                var21 -= var9 * var15;
            }
            if (this.isMovingRight) {
                var17 += var11 * var15;
                var21 += var13 * var15;
            }
            if (this.isMovingLeft) {
                var17 -= var11 * var15;
                var21 -= var13 * var15;
            }
            if (this.isMovingUp) {
                var19 += var15;
            }
            if (this.isMovingDown) {
                var19 -= var15;
            }
            double var23 = 1.0 - Math.exp(-12.0 * var3);
            this.smoothedVelocity.x = class_3532.method_16436((double)var23, (double)this.smoothedVelocity.x, (double)var17);
            this.smoothedVelocity.y = class_3532.method_16436((double)var23, (double)this.smoothedVelocity.y, (double)var19);
            this.smoothedVelocity.z = class_3532.method_16436((double)var23, (double)this.smoothedVelocity.z, (double)var21);
            this.currentPosition.add(this.smoothedVelocity.x * var3, this.smoothedVelocity.y * var3, this.smoothedVelocity.z * var3);
        }
    }

    @EventListener
    private void onKey(KeyEvent var1) {
        if (this.mc.field_1755 == null && !CinematicCamera.inputLocked() && !KeyUtils.isKeyPressed(292)) {
            boolean var2 = true;
            class_11908 var3 = new class_11908(var1.key, 0, 0);
            if (this.mc.field_1690.field_1894.method_1417(var3)) {
                this.isMovingForward = var1.mode != 0;
            } else if (this.mc.field_1690.field_1881.method_1417(var3)) {
                this.isMovingBackward = var1.mode != 0;
            } else if (this.mc.field_1690.field_1849.method_1417(var3)) {
                this.isMovingRight = var1.mode != 0;
            } else if (this.mc.field_1690.field_1913.method_1417(var3)) {
                this.isMovingLeft = var1.mode != 0;
            } else if (this.mc.field_1690.field_1903.method_1417(var3)) {
                this.isMovingUp = var1.mode != 0;
            } else if (this.mc.field_1690.field_1832.method_1417(var3)) {
                this.isMovingDown = var1.mode != 0;
            } else {
                var2 = false;
            }
            if (var2) {
                this.resetMovementKeys();
                var1.cancel();
            }
        }
    }

    @EventListener
    private void handleMouseButtonEvent(MouseButtonEvent var1) {
        if (this.mc.field_1755 == null && !CinematicCamera.inputLocked()) {
            boolean var2 = true;
            class_11909 var3 = new class_11909(0.0, 0.0, new class_11910(var1.button, 0));
            if (this.mc.field_1690.field_1894.method_1433(var3)) {
                this.isMovingForward = var1.actions != 0;
            } else if (this.mc.field_1690.field_1881.method_1433(var3)) {
                this.isMovingBackward = var1.actions != 0;
            } else if (this.mc.field_1690.field_1849.method_1433(var3)) {
                this.isMovingRight = var1.actions != 0;
            } else if (this.mc.field_1690.field_1913.method_1433(var3)) {
                this.isMovingLeft = var1.actions != 0;
            } else if (this.mc.field_1690.field_1903.method_1433(var3)) {
                this.isMovingUp = var1.actions != 0;
            } else if (this.mc.field_1690.field_1832.method_1433(var3)) {
                this.isMovingDown = var1.actions != 0;
            } else {
                var2 = false;
            }
            if (var2) {
                this.resetMovementKeys();
                var1.cancel();
            }
        }
    }

    @EventListener
    private void handleMouseScrolledEvent(MouseScrolledEvent var1) {
        if (this.mc.field_1755 == null && var1.amount != 0.0) {
            double var2 = this.scrollSensitivity.getValue();
            if (var2 > 0.0) {
                double var4 = class_3532.method_15350((double)Math.abs(var1.amount), (double)0.25, (double)8.0);
                double var6 = Math.pow(1.0 + 0.25 * var2, var4);
                this.movementSpeed = var1.amount > 0.0 ? class_3532.method_15350((double)(this.movementSpeed * var6), (double)0.01, (double)12.0) : class_3532.method_15350((double)(this.movementSpeed / var6), (double)0.01, (double)12.0);
            }
            var1.cancel();
        }
    }

    @EventListener
    private void handleChunkMarkClosedEvent(ChunkMarkClosedEvent var1) {
        var1.cancel();
    }

    public void updateRotation(double var1, double var3) {
        this.previousYaw = this.yaw;
        this.previousPitch = this.pitch;
        this.yaw = class_3532.method_15393((float)(this.yaw + (float)var1));
        this.pitch = class_3532.method_15363((float)(this.pitch + (float)var3), (float)-90.0f, (float)90.0f);
    }

    public double getInterpolatedX(float var1) {
        return class_3532.method_16436((double)var1, (double)this.previousPosition.x, (double)this.currentPosition.x);
    }

    public double getInterpolatedY(float var1) {
        return class_3532.method_16436((double)var1, (double)this.previousPosition.y, (double)this.currentPosition.y);
    }

    public double getInterpolatedZ(float var1) {
        return class_3532.method_16436((double)var1, (double)this.previousPosition.z, (double)this.currentPosition.z);
    }

    public double getInterpolatedYaw(float var1) {
        return class_3532.method_16439((float)var1, (float)this.previousYaw, (float)this.yaw);
    }

    public double getInterpolatedPitch(float var1) {
        return class_3532.method_16439((float)var1, (float)this.previousPitch, (float)this.pitch);
    }
}

