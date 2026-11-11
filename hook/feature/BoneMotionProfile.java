/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.CharacterBone;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class BoneMotionProfile {
    private static final float DEG = (float)Math.PI / 180;
    private static final float TAU = (float)Math.PI * 2;
    private static final float STRIDE = 0.6662f;
    float restPitch;
    float restYaw;
    float restRoll;
    float idlePitch;
    float idleYaw;
    float idleRoll;
    float idleRate = 0.4f;
    float idlePhase;
    float walkPitch;
    float walkYaw;
    float walkRoll;
    float walkRate = 1.0f;
    float walkPhase;
    float drag;
    float hang;
    float turnRoll;
    float turnYaw;
    CharacterBone follow;
    float followAmount;
    float followLimit;
    float stiffness;
    float damping;
    float bend;
    float limit = 80.0f;

    public static BoneMotionProfile none() {
        return new BoneMotionProfile();
    }

    public static BoneMotionProfile hair() {
        return new BoneMotionProfile().idle(2.5f, 0.0f, 2.0f, 0.35f).walk(4.0f, 0.0f, 3.0f, 2.0f).drag(16.0f).hang(0.75f).turn(12.0f, 0.0f).spring(55.0f, 7.0f).bend(1.3f);
    }

    public static BoneMotionProfile tail() {
        return new BoneMotionProfile().idle(2.0f, 10.0f, 0.0f, 0.45f).walk(3.0f, 9.0f, 0.0f, 1.0f).drag(8.0f).hang(0.25f).turn(0.0f, 18.0f).spring(35.0f, 5.0f).bend(1.2f);
    }

    public static BoneMotionProfile skirt(CharacterBone var0) {
        return new BoneMotionProfile().follow(var0, 0.45f).drag(4.0f).spring(90.0f, 12.0f).bend(1.0f);
    }

    public static BoneMotionProfile cloth() {
        return new BoneMotionProfile().idle(1.5f, 0.0f, 1.5f, 0.3f).walk(5.0f, 0.0f, 2.0f, 2.0f).drag(10.0f).hang(0.6f).turn(8.0f, 0.0f).spring(45.0f, 6.0f).bend(1.4f);
    }

    public BoneMotionProfile rest(float var1, float var2, float var3) {
        this.restPitch = var1;
        this.restYaw = var2;
        this.restRoll = var3;
        return this;
    }

    public BoneMotionProfile idle(float var1, float var2, float var3, float var4) {
        this.idlePitch = var1;
        this.idleYaw = var2;
        this.idleRoll = var3;
        this.idleRate = var4;
        return this;
    }

    public BoneMotionProfile idlePhase(float var1) {
        this.idlePhase = var1 * ((float)Math.PI / 180);
        return this;
    }

    public BoneMotionProfile walk(float var1, float var2, float var3, float var4) {
        this.walkPitch = var1;
        this.walkYaw = var2;
        this.walkRoll = var3;
        this.walkRate = var4;
        return this;
    }

    public BoneMotionProfile walkPhase(float var1) {
        this.walkPhase = var1 * ((float)Math.PI / 180);
        return this;
    }

    public BoneMotionProfile drag(float var1) {
        this.drag = var1;
        return this;
    }

    public BoneMotionProfile hang(float var1) {
        this.hang = var1;
        return this;
    }

    public BoneMotionProfile turn(float var1, float var2) {
        this.turnRoll = var1;
        this.turnYaw = var2;
        return this;
    }

    public BoneMotionProfile follow(CharacterBone var1, float var2) {
        return this.follow(var1, var2, 0.0f);
    }

    public BoneMotionProfile follow(CharacterBone var1, float var2, float var3) {
        this.follow = var1;
        this.followAmount = var2;
        this.followLimit = Math.max(0.0f, var3);
        return this;
    }

    public BoneMotionProfile spring(float var1, float var2) {
        this.stiffness = Math.max(0.0f, var1);
        this.damping = Math.max(0.0f, var2);
        return this;
    }

    public BoneMotionProfile bend(float var1) {
        this.bend = Math.max(0.0f, var1);
        return this;
    }

    public BoneMotionProfile limit(float var1) {
        this.limit = Math.abs(var1);
        return this;
    }

    public BoneMotionProfile mirrored() {
        BoneMotionProfile var1 = this.copy();
        var1.restYaw = -this.restYaw;
        var1.restRoll = -this.restRoll;
        var1.follow = this.follow == null ? null : this.follow.opposite();
        return var1;
    }

    public BoneMotionProfile copy() {
        BoneMotionProfile var1 = new BoneMotionProfile();
        var1.restPitch = this.restPitch;
        var1.restYaw = this.restYaw;
        var1.restRoll = this.restRoll;
        var1.idlePitch = this.idlePitch;
        var1.idleYaw = this.idleYaw;
        var1.idleRoll = this.idleRoll;
        var1.idleRate = this.idleRate;
        var1.idlePhase = this.idlePhase;
        var1.walkPitch = this.walkPitch;
        var1.walkYaw = this.walkYaw;
        var1.walkRoll = this.walkRoll;
        var1.walkRate = this.walkRate;
        var1.walkPhase = this.walkPhase;
        var1.drag = this.drag;
        var1.hang = this.hang;
        var1.turnRoll = this.turnRoll;
        var1.turnYaw = this.turnYaw;
        var1.follow = this.follow;
        var1.followAmount = this.followAmount;
        var1.followLimit = this.followLimit;
        var1.stiffness = this.stiffness;
        var1.damping = this.damping;
        var1.bend = this.bend;
        var1.limit = this.limit;
        return var1;
    }

    public float bendPower() {
        return this.bend;
    }

    public boolean springy() {
        return this.stiffness > 0.0f;
    }

    public void target(MotionFrame var1, CharacterBone var2, float[] var3) {
        float var4 = Math.min(1.0f, Math.max(0.0f, var1.walkSpeed));
        float var5 = (float)Math.sin(var1.time * this.idleRate * ((float)Math.PI * 2) + this.idlePhase);
        float var6 = (float)Math.sin(var1.walkPos * 0.6662f * this.walkRate + this.walkPhase) * var4;
        float var7 = Math.max(-1.5f, Math.min(1.5f, var1.turnRate / 180.0f));
        int var8 = var2.ordinal();
        float var9 = (this.restPitch + this.idlePitch * var5 + this.walkPitch * var6 + this.drag * var4) * ((float)Math.PI / 180) - this.hang * var1.pitch[var8];
        float var10 = (this.restYaw + this.idleYaw * var5 + this.walkYaw * var6 + this.turnYaw * var7) * ((float)Math.PI / 180);
        float var11 = (this.restRoll + this.idleRoll * var5 + this.walkRoll * var6 + this.turnRoll * var7) * ((float)Math.PI / 180);
        if (this.follow != null) {
            int var12 = this.follow.ordinal();
            var9 += BoneMotionProfile.soft(this.followAmount * (var1.pitch[var12] - var1.pitch[var8]), this.followLimit);
            var10 += BoneMotionProfile.soft(this.followAmount * (var1.yaw[var12] - var1.yaw[var8]), this.followLimit);
            var11 += BoneMotionProfile.soft(this.followAmount * (var1.roll[var12] - var1.roll[var8]), this.followLimit);
        }
        float var13 = this.limit * ((float)Math.PI / 180);
        var3[0] = Math.max(-var13, Math.min(var13, var9));
        var3[1] = Math.max(-var13, Math.min(var13, var10));
        var3[2] = Math.max(-var13, Math.min(var13, var11));
    }

    static float soft(float var0, float var1) {
        if (var1 <= 0.0f) {
            return var0;
        }
        float var2 = var1 * ((float)Math.PI / 180);
        return var2 * (float)Math.tanh(var0 / var2);
    }

    public void step(float[] var1, int var2, float[] var3, float var4) {
        if (!(this.stiffness <= 0.0f || var4 <= 0.0f || var4 > 0.25f || Float.isNaN(var1[var2]))) {
            int var11 = Math.max(1, (int)Math.ceil(var4 * 120.0f));
            float var6 = var4 / (float)var11;
            for (int var7 = 0; var7 < var11; ++var7) {
                for (int var8 = 0; var8 < 3; ++var8) {
                    float var9 = var1[var2 + var8];
                    float var10 = var1[var2 + 3 + var8];
                    var10 += (this.stiffness * (var3[var8] - var9) - this.damping * var10) * var6;
                    var1[var2 + 3 + var8] = var10;
                    var1[var2 + var8] = var9 + var10 * var6;
                }
            }
        } else {
            for (int var5 = 0; var5 < 3; ++var5) {
                var1[var2 + var5] = var3[var5];
                var1[var2 + 3 + var5] = 0.0f;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class MotionFrame {
        public float time;
        public float walkPos;
        public float walkSpeed;
        public float turnRate;
        public final float[] pitch = new float[6];
        public final float[] yaw = new float[6];
        public final float[] roll = new float[6];
    }
}

