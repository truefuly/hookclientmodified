/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package hook.halloween.witch;

import hook.halloween.witch.WitchAsset;
import hook.halloween.witch.WitchRig;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class WitchPerformance {
    public static final float APPEAR_END = 2.2f;
    public static final float NOTICE = 3.7f;
    public static final float TURN_START = 4.1f;
    public static final float WAVE_START = 5.3f;
    public static final float WAVE_END = 8.0f;
    public static final float HAT_START = 8.0f;
    public static final float HAT_END = 9.8f;
    public static final float SPELL_START = 9.7f;
    public static final float SPELL_END = 11.9f;
    public static final float MOUNT_START = 11.9f;
    public static final float MOUNT_END = 13.9f;
    public static final float FLY_START = 14.7f;
    public static final float WAVE_BACK = 19.7f;
    public static final float VANISH_LEN = 2.8f;
    private final WitchRig rig;
    private final int hips;
    private final int spine1;
    private final int spine2;
    private final int chest;
    private final int neck;
    private final int head;
    private final int jaw;
    private final int eyeR;
    private final int eyeL;
    private final int[] hat;
    private final int[] hair;
    private final int[] cloak;
    private final int shoulderR;
    private final int elbowR;
    private final int wristR;
    private final int shoulderL;
    private final int elbowL;
    private final int wristL;
    private final int[][] fingersR;
    private final int[][] fingersL;
    private final int hipR;
    private final int kneeR;
    private final int ankleR;
    private final int hipL;
    private final int kneeL;
    private final int ankleL;
    private final Quaternionf[] stand;
    private final Quaternionf[] sit;
    private final Quaternionf q = new Quaternionf();
    private final Vector3f v = new Vector3f();
    public final Vector3d origin = new Vector3d();
    public final Matrix4f model = new Matrix4f();
    public final Matrix4f broom = new Matrix4f();
    public float alpha;
    public float glow;
    public float dissolveAt;
    public float dissolveMode;
    public boolean finished;
    public float vanishStart = Float.MAX_VALUE;
    public float casting;
    private Ground ground;
    private float bodyYaw;
    private float yawVel;
    private float startYaw;
    private float headYaw;
    private float headYawVel;
    private final Vector3f flyPos = new Vector3f();
    private final Vector3f flyVel = new Vector3f();
    private float flyYaw;
    private float flyYawVel;
    private float flyPitch;
    private float flyPitchVel;
    private float flyRoll;
    private float flyRollVel;
    private float pathS;
    private float lastT = -1.0f;
    private final Matrix4f heldGrip = new Matrix4f();
    private boolean gripReady;
    private Vector3f[] path;
    private float exitFade = 1.0f;
    private boolean exiting;

    public WitchPerformance(WitchRig var1) {
        this.rig = var1;
        WitchAsset var2 = var1.asset;
        this.hips = var2.joint("Root_M");
        this.spine1 = var2.joint("Spine1_M");
        this.spine2 = var2.joint("Spine2_M");
        this.chest = var2.joint("Chest_M");
        this.neck = var2.joint("Neck_M");
        this.head = var2.joint("Head_M");
        this.jaw = var2.joint("JawJoint_M");
        this.eyeR = var2.joint("EyeJoint_R");
        this.eyeL = var2.joint("EyeJoint_L");
        this.hat = WitchPerformance.joints(var2, "hat1_M", "hat2_M", "hat3_L", "hat4_L", "hat5_L", "hat6_L");
        this.hair = WitchPerformance.joints(var2, "hairstrand20_L", "hairstrand21_L", "hairstrand22_L", "hairstrand23_L", "hairstrand24_L", "hairstrand25_L", "hairstrand26_L", "hairstrand27_L");
        this.cloak = WitchPerformance.joints(var2, "cloak1_M", "cloak2_M", "cloak3_M", "cloak4_M", "cloak5_M", "cloak6_M", "cloak7_M");
        this.shoulderR = var2.joint("Shoulder_R");
        this.elbowR = var2.joint("Elbow_R");
        this.wristR = var2.joint("Wrist_R");
        this.shoulderL = var2.joint("Shoulder_L");
        this.elbowL = var2.joint("Elbow_L");
        this.wristL = var2.joint("Wrist_L");
        this.fingersR = WitchPerformance.fingers(var2, "_R");
        this.fingersL = WitchPerformance.fingers(var2, "_L");
        this.hipR = var2.joint("Hip_R");
        this.kneeR = var2.joint("Knee_R");
        this.ankleR = var2.joint("Ankle_R");
        this.hipL = var2.joint("Hip_L");
        this.kneeL = var2.joint("Knee_L");
        this.ankleL = var2.joint("Ankle_L");
        this.stand = new Quaternionf[var1.count];
        this.sit = new Quaternionf[var1.count];
        for (int var3 = 0; var3 < var1.count; ++var3) {
            this.stand[var3] = new Quaternionf();
            this.sit[var3] = new Quaternionf();
        }
    }

    private static int[] joints(WitchAsset var0, String ... var1) {
        int[] var2 = new int[var1.length];
        for (int var3 = 0; var3 < var1.length; ++var3) {
            var2[var3] = var0.joint(var1[var3]);
        }
        return var2;
    }

    private static int[][] fingers(WitchAsset var0, String var1) {
        String[] var2 = new String[]{"ThumbFinger", "IndexFinger", "MiddleFinger", "RingFinger", "PinkyFinger"};
        int[][] var3 = new int[5][3];
        for (int var4 = 0; var4 < 5; ++var4) {
            for (int var5 = 0; var5 < 3; ++var5) {
                var3[var4][var5] = var0.joint(var2[var4] + (var5 + 1) + var1);
            }
        }
        return var3;
    }

    public void start(Vector3d var1, Vector3d var2, Ground var3) {
        this.origin.set((Vector3dc)var1);
        this.ground = var3;
        float var4 = (float)Math.atan2(var2.x - var1.x, var2.z - var1.z);
        this.bodyYaw = this.startYaw = var4 + 0.95f;
        this.yawVel = 0.0f;
        this.headYaw = 0.0f;
        this.headYawVel = 0.0f;
        this.finished = false;
        this.alpha = 0.0f;
        this.lastT = -1.0f;
        this.gripReady = false;
        this.pathS = 0.0f;
        this.path = null;
        this.vanishStart = Float.MAX_VALUE;
        this.exitFade = 1.0f;
        this.exiting = false;
    }

    public void exit() {
        this.exiting = true;
    }

    static float clamp01(float var0) {
        return var0 < 0.0f ? 0.0f : Math.min(var0, 1.0f);
    }

    static float span(float var0, float var1, float var2) {
        return WitchPerformance.clamp01((var0 - var1) / (var2 - var1));
    }

    static float smooth(float var0) {
        var0 = WitchPerformance.clamp01(var0);
        return var0 * var0 * var0 * (var0 * (var0 * 6.0f - 15.0f) + 10.0f);
    }

    static float window(float var0, float var1, float var2, float var3, float var4) {
        return WitchPerformance.smooth(WitchPerformance.span(var0, var1, var1 + var2)) * (1.0f - WitchPerformance.smooth(WitchPerformance.span(var0, var3 - var4, var3)));
    }

    private static float wrap(float var0) {
        while ((double)var0 > Math.PI) {
            var0 -= (float)Math.PI * 2;
        }
        while ((double)var0 < -Math.PI) {
            var0 += (float)Math.PI * 2;
        }
        return var0;
    }

    private static Quaternionf axis(Quaternionf var0, float var1, float var2, float var3, float var4) {
        float var5 = (float)Math.sqrt(var1 * var1 + var2 * var2 + var3 * var3);
        return var0.rotationAxis((float)Math.toRadians(var4), var1 / var5, var2 / var5, var3 / var5);
    }

    private void turn(Quaternionf[] var1, int var2, float var3, float var4, float var5, float var6) {
        if (var2 >= 0 && var6 != 0.0f) {
            var1[var2].premul((Quaternionfc)WitchPerformance.axis(this.q, var3, var4, var5, var6));
        }
    }

    private static void clear(Quaternionf[] var0) {
        for (Quaternionf var4 : var0) {
            var4.identity();
        }
    }

    private void curl(Quaternionf[] var1, int[][] var2, float var3, float var4, float var5) {
        for (int var6 = 1; var6 < 5; ++var6) {
            float var7 = ((float)var6 - 2.5f) * 3.0f * (1.0f - var4);
            for (int var8 = 0; var8 < 3; ++var8) {
                float var9 = var4 * (var8 == 0 ? 55.0f : (var8 == 1 ? 80.0f : 55.0f));
                this.turn(var1, var2[var6][var8], 0.0f, 0.0f, 1.0f, var3 * var9);
                if (var8 != 0) continue;
                this.turn(var1, var2[var6][var8], 1.0f, 0.0f, 0.0f, var7);
            }
        }
        for (int var10 = 0; var10 < 3; ++var10) {
            this.turn(var1, var2[0][var10], 0.0f, 1.0f, 0.0f, -var3 * var5 * (var10 == 0 ? 25.0f : 35.0f));
        }
    }

    public void update(float var1, Vector3d var2) {
        float var56;
        float var3 = this.lastT < 0.0f ? 0.0f : Math.max(0.0f, Math.min(0.1f, var1 - this.lastT));
        this.lastT = var1;
        WitchRig var4 = this.rig;
        WitchPerformance.clear(this.stand);
        WitchPerformance.clear(this.sit);
        float var5 = (float)(var2.x - this.origin.x);
        float var6 = (float)(var2.z - this.origin.z);
        float var7 = (float)Math.atan2(var5, var6);
        float var8 = (float)Math.sqrt(var5 * var5 + var6 * var6);
        float var9 = (float)(var2.y - (this.origin.y + 1.5));
        float var10 = (float)Math.toDegrees(Math.atan2(var9, Math.max(0.5f, var8)));
        if (var1 < 14.7f) {
            float var11 = var1 < 4.1f ? this.startYaw : this.startYaw + WitchPerformance.wrap(var7 - this.startYaw);
            float var12 = 15.0f;
            float var13 = WitchPerformance.wrap(var11 - this.bodyYaw);
            this.yawVel += (var13 * var12 - this.yawVel * 2.0f * (float)Math.sqrt(var12)) * var3;
            this.bodyYaw = WitchPerformance.wrap(this.bodyYaw + this.yawVel * var3);
            if (var3 == 0.0f) {
                this.bodyYaw = var11;
            }
        }
        float var47 = (float)Math.toDegrees(Math.max(-1.15f, Math.min(1.15f, WitchPerformance.wrap(var7 - this.bodyYaw))));
        float var48 = 40.0f * WitchPerformance.window(var1, 2.15f, 0.5f, 3.05f, 0.45f) - 36.0f * WitchPerformance.window(var1, 3.0f, 0.5f, 3.9f, 0.3f);
        float var49 = WitchPerformance.smooth(WitchPerformance.span(var1, 3.7f, 4.15f));
        float var14 = var48 * (1.0f - var49) + var47 * var49;
        float var15 = var1 > 3.7f && var1 < 4.2f ? 90.0f : 45.0f;
        this.headYawVel += ((var14 - this.headYaw) * var15 - this.headYawVel * 2.0f * (float)Math.sqrt(var15)) * var3;
        this.headYaw += this.headYawVel * var3;
        if (var3 == 0.0f) {
            this.headYaw = var14;
        }
        float var16 = 1.0f - WitchPerformance.smooth(WitchPerformance.span(var1, 11.9f, 12.7f));
        float var17 = (float)Math.sin(var1 * 1.7f);
        float var18 = (float)Math.sin(var1 * 0.83f + 0.4f);
        Quaternionf[] var19 = this.stand;
        this.turn(var19, this.spine1, 1.0f, 0.0f, 0.0f, 1.2f * var17);
        this.turn(var19, this.chest, 1.0f, 0.0f, 0.0f, -1.6f * var17);
        this.turn(var19, this.spine2, 0.0f, 0.0f, 1.0f, 1.1f * var18);
        this.turn(var19, this.hips, 0.0f, 0.0f, 1.0f, -1.2f * var18);
        this.turn(var19, this.chest, 0.0f, 1.0f, 0.0f, this.headYaw * 0.18f * var16);
        this.turn(var19, this.neck, 0.0f, 1.0f, 0.0f, this.headYaw * 0.32f * var16);
        this.turn(var19, this.head, 0.0f, 1.0f, 0.0f, this.headYaw * 0.5f * var16);
        float var20 = var49 * var16;
        this.turn(var19, this.neck, 1.0f, 0.0f, 0.0f, -var10 * 0.35f * var20);
        this.turn(var19, this.head, 1.0f, 0.0f, 0.0f, -var10 * 0.45f * var20);
        float var21 = Math.max(-26.0f, Math.min(26.0f, (var14 - this.headYaw) * 1.4f + var47 * 0.15f * var49));
        this.turn(var19, this.eyeR, 0.0f, 1.0f, 0.0f, var21 * var16);
        this.turn(var19, this.eyeL, 0.0f, 1.0f, 0.0f, var21 * var16);
        this.turn(var19, this.shoulderR, 0.0f, 0.0f, 1.0f, 26.0f);
        this.turn(var19, this.shoulderR, 1.0f, 0.0f, 0.0f, -6.0f);
        this.turn(var19, this.elbowR, 1.0f, 0.0f, 0.0f, -14.0f);
        this.curl(var19, this.fingersR, -1.0f, 0.25f, 0.2f);
        this.turn(var19, this.shoulderL, 0.0f, 0.0f, 1.0f, -14.0f);
        this.turn(var19, this.shoulderL, 1.0f, 0.0f, 0.0f, -16.0f);
        this.turn(var19, this.elbowL, 1.0f, 0.0f, 0.0f, -58.0f);
        this.turn(var19, this.wristL, 0.0f, 1.0f, 0.0f, 12.0f);
        this.curl(var19, this.fingersL, 1.0f, 1.0f, 1.0f);
        float var22 = 1.0f - WitchPerformance.smooth(WitchPerformance.span(var1, 1.2f, 2.6f));
        if (var22 > 0.0f) {
            this.turn(var19, this.neck, 1.0f, 0.0f, 0.0f, 14.0f * var22);
            this.turn(var19, this.head, 1.0f, 0.0f, 0.0f, 16.0f * var22);
            this.turn(var19, this.spine2, 1.0f, 0.0f, 0.0f, 4.0f * var22);
            this.turn(var19, this.shoulderR, 0.0f, 0.0f, 1.0f, -18.0f * var22);
        }
        float var23 = (float)Math.sin(Math.PI * (double)WitchPerformance.span(var1, 3.7f, 4.25f));
        this.turn(var19, this.chest, 1.0f, 0.0f, 0.0f, -5.0f * var23);
        this.turn(var19, this.head, 1.0f, 0.0f, 0.0f, -4.0f * var23);
        float var24 = WitchPerformance.window(var1, 5.3f, 0.7f, 8.0f, 0.6f);
        float var25 = WitchPerformance.window(var1, 5.25f, 0.47f, 8.05f, 0.55f);
        if (var24 > 0.0f || var25 > 0.0f) {
            float var26 = (float)Math.sin((double)(var1 - 5.3f - 0.5f) * Math.PI * 2.0 * 1.45);
            float var27 = WitchPerformance.window(var1, 5.75f, 0.4f, 7.55f, 0.45f);
            this.turn(var19, this.shoulderR, 0.0f, 0.0f, 1.0f, -84.0f * var24);
            this.turn(var19, this.shoulderR, 1.0f, 0.0f, 0.0f, -26.0f * var24);
            this.turn(var19, this.elbowR, 0.0f, 0.0f, 1.0f, -96.0f * var25);
            this.turn(var19, this.elbowR, 0.0f, 0.0f, 1.0f, 20.0f * var26 * var27);
            this.turn(var19, this.wristR, 0.0f, 0.0f, 1.0f, 16.0f * (float)Math.sin((double)(var1 - 5.3f - 0.62f) * Math.PI * 2.0 * 1.45) * var27);
            this.curl(var19, this.fingersR, -1.0f, -0.25f * var25, -0.4f * var25);
            this.turn(var19, this.chest, 0.0f, 0.0f, 1.0f, 3.5f * var24);
            this.turn(var19, this.spine2, 0.0f, 1.0f, 0.0f, -4.0f * var24);
            this.turn(var19, this.head, 0.0f, 0.0f, 1.0f, -4.0f * var24 + 2.0f * var26 * var27);
            this.turn(var19, this.jaw, 1.0f, 0.0f, 0.0f, 4.0f * var24);
        }
        float var50 = WitchPerformance.window(var1, 8.0f, 0.5f, 9.8f, 0.55f);
        float var51 = WitchPerformance.window(var1, 8.5f, 0.4f, 9.5f, 0.5f);
        if (var50 > 0.0f) {
            this.turn(var19, this.shoulderR, 1.0f, 0.0f, 0.0f, -95.0f * var50);
            this.turn(var19, this.shoulderR, 0.0f, 0.0f, 1.0f, -22.0f * var50);
            this.turn(var19, this.elbowR, 1.0f, 0.0f, 0.0f, -70.0f * var50);
            this.turn(var19, this.elbowR, 0.0f, 1.0f, 0.0f, 30.0f * var50);
            this.curl(var19, this.fingersR, -1.0f, 0.45f * var50, 0.6f * var50);
            this.turn(var19, this.spine1, 1.0f, 0.0f, 0.0f, 9.0f * var51);
            this.turn(var19, this.chest, 1.0f, 0.0f, 0.0f, 6.0f * var51);
            this.turn(var19, this.neck, 1.0f, 0.0f, 0.0f, 8.0f * var51);
            this.turn(var19, this.head, 1.0f, 0.0f, 0.0f, 9.0f * var51);
            this.turn(var19, this.hipR, 1.0f, 0.0f, 0.0f, -12.0f * var51);
            this.turn(var19, this.kneeR, 1.0f, 0.0f, 0.0f, 22.0f * var51);
            this.turn(var19, this.ankleR, 1.0f, 0.0f, 0.0f, -10.0f * var51);
            this.turn(var19, this.hipL, 1.0f, 0.0f, 0.0f, -16.0f * var51);
            this.turn(var19, this.kneeL, 1.0f, 0.0f, 0.0f, 30.0f * var51);
            this.turn(var19, this.ankleL, 1.0f, 0.0f, 0.0f, -14.0f * var51);
        }
        float var28 = WitchPerformance.window(var1, 9.7f, 0.7f, 11.9f, 0.55f);
        this.casting = WitchPerformance.window(var1, 10.15f, 0.3f, 11.549999f, 0.35f);
        if (var28 > 0.0f) {
            float var29 = (float)Math.sin((var1 - 9.7f) * 3.3f) * this.casting;
            this.turn(var19, this.shoulderR, 1.0f, 0.0f, 0.0f, -128.0f * var28);
            this.turn(var19, this.shoulderR, 0.0f, 0.0f, 1.0f, -18.0f * var28 + 8.0f * var29);
            this.turn(var19, this.elbowR, 1.0f, 0.0f, 0.0f, -22.0f * var28);
            this.turn(var19, this.wristR, 1.0f, 0.0f, 0.0f, -20.0f * var28);
            this.curl(var19, this.fingersR, -1.0f, -0.35f * var28, -0.5f * var28);
            this.turn(var19, this.chest, 1.0f, 0.0f, 0.0f, -7.0f * var28);
            this.turn(var19, this.neck, 1.0f, 0.0f, 0.0f, -12.0f * var28);
            this.turn(var19, this.head, 1.0f, 0.0f, 0.0f, -16.0f * var28);
            this.turn(var19, this.jaw, 1.0f, 0.0f, 0.0f, 7.0f * this.casting);
        }
        Quaternionf[] var52 = this.sit;
        this.turn(var52, this.spine1, 1.0f, 0.0f, 0.0f, 9.0f + 1.0f * var17);
        this.turn(var52, this.spine2, 1.0f, 0.0f, 0.0f, 6.0f);
        this.turn(var52, this.chest, 1.0f, 0.0f, 0.0f, 3.0f);
        this.turn(var52, this.neck, 1.0f, 0.0f, 0.0f, -9.0f);
        this.turn(var52, this.head, 1.0f, 0.0f, 0.0f, -6.0f);
        this.turn(var52, this.hipR, 1.0f, 0.0f, 0.0f, -78.0f);
        this.turn(var52, this.hipR, 0.0f, 0.0f, 1.0f, -10.0f);
        this.turn(var52, this.kneeR, 1.0f, 0.0f, 0.0f, 92.0f);
        this.turn(var52, this.ankleR, 1.0f, 0.0f, 0.0f, -18.0f);
        this.turn(var52, this.hipL, 1.0f, 0.0f, 0.0f, -74.0f);
        this.turn(var52, this.hipL, 0.0f, 0.0f, 1.0f, 10.0f);
        this.turn(var52, this.kneeL, 1.0f, 0.0f, 0.0f, 98.0f);
        this.turn(var52, this.ankleL, 1.0f, 0.0f, 0.0f, -18.0f);
        this.turn(var52, this.shoulderR, 0.0f, 0.0f, 1.0f, 30.0f);
        this.turn(var52, this.shoulderR, 1.0f, 0.0f, 0.0f, -44.0f);
        this.turn(var52, this.elbowR, 1.0f, 0.0f, 0.0f, -22.0f);
        this.turn(var52, this.shoulderL, 0.0f, 0.0f, 1.0f, -30.0f);
        this.turn(var52, this.shoulderL, 1.0f, 0.0f, 0.0f, -44.0f);
        this.turn(var52, this.elbowL, 1.0f, 0.0f, 0.0f, -22.0f);
        this.curl(var52, this.fingersR, -1.0f, 1.0f, 1.0f);
        this.curl(var52, this.fingersL, 1.0f, 1.0f, 1.0f);
        if (var1 > 14.2f) {
            float var30 = WitchPerformance.smooth(WitchPerformance.span(var1, 16.2f, 17.3f)) * (1.0f - WitchPerformance.smooth(WitchPerformance.span(var1, this.vanishStart - 1.0f, this.vanishStart)));
            float[] var31 = this.lookAngles(var2);
            float var32 = Math.max(-75.0f, Math.min(75.0f, var31[0])) * var30;
            float var33 = Math.max(-30.0f, Math.min(30.0f, var31[1])) * var30;
            this.turn(var52, this.chest, 0.0f, 1.0f, 0.0f, var32 * 0.2f);
            this.turn(var52, this.neck, 0.0f, 1.0f, 0.0f, var32 * 0.35f);
            this.turn(var52, this.head, 0.0f, 1.0f, 0.0f, var32 * 0.45f);
            this.turn(var52, this.head, 1.0f, 0.0f, 0.0f, -var33 * 0.6f);
            float var34 = WitchPerformance.window(var1, 19.7f, 0.55f, 21.6f, 0.55f);
            if (var34 > 0.0f) {
                float var35 = (float)Math.sin((double)(var1 - 19.7f) * Math.PI * 2.0 * 1.6) * WitchPerformance.window(var1, 20.1f, 0.3f, 21.2f, 0.3f);
                this.turn(var52, this.shoulderR, 1.0f, 0.0f, 0.0f, 44.0f * var34);
                this.turn(var52, this.shoulderR, 0.0f, 0.0f, 1.0f, -105.0f * var34);
                this.turn(var52, this.elbowR, 0.0f, 0.0f, 1.0f, -80.0f * var34 + 18.0f * var35);
                this.curl(var52, this.fingersR, -1.0f, -1.2f * var34, -1.3f * var34);
            }
        }
        float var53 = WitchPerformance.smooth(WitchPerformance.span(var1, 12.2f, 13.75f));
        float var54 = var1 < 12.7f ? (float)Math.sin(Math.PI * (double)WitchPerformance.span(var1, 11.9f, 12.7f)) : 0.0f;
        for (int var55 = 0; var55 < var4.count; ++var55) {
            var4.rot[var55].set((Quaternionfc)this.stand[var55]).slerp((Quaternionfc)this.sit[var55], var53);
        }
        if (var54 > 0.0f) {
            this.turn(var4.rot, this.hipR, 1.0f, 0.0f, 0.0f, -22.0f * var54);
            this.turn(var4.rot, this.kneeR, 1.0f, 0.0f, 0.0f, 38.0f * var54);
            this.turn(var4.rot, this.ankleR, 1.0f, 0.0f, 0.0f, -16.0f * var54);
            this.turn(var4.rot, this.hipL, 1.0f, 0.0f, 0.0f, -22.0f * var54);
            this.turn(var4.rot, this.kneeL, 1.0f, 0.0f, 0.0f, 38.0f * var54);
            this.turn(var4.rot, this.ankleL, 1.0f, 0.0f, 0.0f, -16.0f * var54);
            this.turn(var4.rot, this.spine1, 1.0f, 0.0f, 0.0f, 8.0f * var54);
        }
        if ((var56 = WitchPerformance.window(var1, 14.55f, 0.35f, 17.4f, 0.6f)) > 0.0f) {
            float var57 = 0.5f + 0.5f * (float)Math.sin((double)(var1 - 14.7f) * Math.PI * 2.0 * 3.2);
            this.turn(var4.rot, this.jaw, 1.0f, 0.0f, 0.0f, (8.0f + 10.0f * var57) * var56);
            this.turn(var4.rot, this.head, 1.0f, 0.0f, 0.0f, -14.0f * var56);
            this.turn(var4.rot, this.chest, 1.0f, 0.0f, 0.0f, -5.0f * var56 * var57);
        }
        float var58 = this.flyVel.length();
        float var59 = Math.min(1.0f, var58 / 7.0f);
        float var60 = WitchPerformance.smooth(WitchPerformance.span(var1, 12.5f, 14.4f));
        float var36 = 0.25f + 0.75f * var59;
        for (int var37 = 0; var37 < this.cloak.length; ++var37) {
            float var38 = var1 * (3.0f + 6.0f * var59) - (float)var37 * 0.85f;
            float var39 = (6.0f + 6.5f * (float)var37) * (0.35f * var60 + var59) * 0.5f;
            this.turn(var4.rot, this.cloak[var37], 1.0f, 0.0f, 0.0f, var39 + (float)Math.sin(var38) * (1.5f + 4.0f * var36) * (0.4f + 0.2f * (float)var37));
            this.turn(var4.rot, this.cloak[var37], 0.0f, 0.0f, 1.0f, (float)Math.sin(var38 * 0.7f + 1.3f) * 2.0f * var36);
        }
        for (int var61 = 0; var61 < this.hair.length; ++var61) {
            float var64 = var1 * (2.2f + 5.0f * var59) - (float)var61 * 0.7f;
            this.turn(var4.rot, this.hair[var61], 1.0f, 0.0f, 0.0f, 2.0f + 4.5f * (float)var61 * var59 + (float)Math.sin(var64) * (1.2f + 3.0f * var59));
            this.turn(var4.rot, this.hair[var61], 0.0f, 0.0f, 1.0f, (float)Math.sin(var64 * 0.8f + 0.5f) * (1.0f + 2.5f * var59));
        }
        for (int var62 = 1; var62 < this.hat.length; ++var62) {
            float var65 = var1 * (2.0f + 4.0f * var59) - (float)var62 * 0.6f;
            this.turn(var4.rot, this.hat[var62], 1.0f, 0.0f, 0.0f, (float)var62 * 4.0f * var59 + (float)Math.sin(var65) * (1.0f + 2.0f * var59) * (float)var62 * 0.5f);
        }
        float var63 = (float)Math.sin(Math.PI * (double)WitchPerformance.span(var1, 12.5f, 13.299999f));
        var4.rootOffset.set(0.0f, -0.05f * var54 + 0.1f * Math.max(0.0f, var63) * (1.0f - var53 * 0.3f) - 0.045f * var51, 0.0f);
        var4.solve();
        if (!this.gripReady) {
            this.computeGrip();
        }
        Matrix4f var66 = new Matrix4f((Matrix4fc)var4.world[this.wristL]).mul((Matrix4fc)this.heldGrip);
        Matrix4f var67 = this.seatedBroom(new Matrix4f());
        Matrix4f var40 = new Matrix4f((Matrix4fc)var67).translateLocal(0.36f, 0.06f, 0.02f);
        float var41 = WitchPerformance.smooth(WitchPerformance.span(var1, 11.95f, 12.849999f));
        float var42 = WitchPerformance.smooth(WitchPerformance.span(var1, 12.75f, 13.599999f));
        Matrix4f var43 = new Matrix4f();
        WitchPerformance.blend(var66, var40, var41, var43);
        WitchPerformance.blend(var43, var67, var42, this.broom);
        float var44 = 0.0f;
        if (var1 >= 11.9f) {
            var44 = 0.62f * WitchPerformance.smooth(WitchPerformance.span(var1, 12.5f, 14.299999f));
            var44 += 0.06f * (float)Math.sin((var1 - 11.9f) * 2.4f) * WitchPerformance.smooth(WitchPerformance.span(var1, 13.599999f, 14.299999f));
        }
        this.fly(var1, var3, var44, var2);
        if (this.exiting) {
            this.exitFade = Math.max(0.0f, this.exitFade - var3 / 0.9f);
        }
        if (var1 < 2.3f) {
            float var45 = WitchPerformance.span(var1, 0.0f, 2.0f);
            this.dissolveMode = 1.0f;
            this.dissolveAt = -0.25f + 2.35f * (1.0f - (1.0f - var45) * (1.0f - var45) * (1.0f - var45));
        } else if (var1 >= this.vanishStart) {
            float var69 = WitchPerformance.span(var1, this.vanishStart, this.vanishStart + 2.8f);
            this.dissolveMode = -1.0f;
            this.dissolveAt = -0.3f + 2.45f * WitchPerformance.smooth(var69);
        } else {
            this.dissolveMode = 0.0f;
        }
        float var70 = var1 >= this.vanishStart ? 1.0f - WitchPerformance.smooth(WitchPerformance.span(var1, this.vanishStart + 1.54f, this.vanishStart + 2.8f)) : 1.0f;
        this.alpha = WitchPerformance.smooth(WitchPerformance.span(var1, 0.0f, 0.35f)) * var70 * this.exitFade;
        float var46 = Math.max(1.0f - WitchPerformance.smooth(WitchPerformance.span(var1, 2.2f, 3.7f)), this.casting * 0.7f);
        if (var1 >= this.vanishStart) {
            var46 = 1.0f;
        }
        this.glow = (0.45f + 0.55f * var46) * this.exitFade;
        this.finished = var1 >= this.vanishStart + 2.8f + 0.4f || this.exiting && this.exitFade <= 0.0f;
    }

    private float[] lookAngles(Vector3d var1) {
        Vector3f var2 = this.model.transformPosition(this.rig.position(this.chest, new Vector3f()));
        Vector3f var3 = new Vector3f((float)(var1.x - this.origin.x) - var2.x, (float)(var1.y - this.origin.y) - var2.y, (float)(var1.z - this.origin.z) - var2.z);
        new Matrix4f((Matrix4fc)this.model).invert().transformDirection(var3);
        float var4 = (float)Math.toDegrees(Math.atan2(var3.x, var3.z));
        float var5 = (float)Math.toDegrees(Math.atan2(var3.y, Math.sqrt(var3.x * var3.x + var3.z * var3.z)));
        return new float[]{var4, var5};
    }

    private void computeGrip() {
        WitchRig var1 = this.rig;
        Vector3f var2 = new Vector3f();
        for (int var3 = 1; var3 < 5; ++var3) {
            var2.add((Vector3fc)var1.position(this.fingersL[var3][0], this.v));
        }
        var2.div(4.0f);
        Vector3f var5 = var1.position(this.wristL, new Vector3f());
        var2.lerp((Vector3fc)var5, 0.35f);
        Matrix4f var4 = new Matrix4f().translation(var2.x + 0.012f, 0.0f, var2.z + 0.01f).rotateY((float)Math.toRadians(-20.0));
        this.heldGrip.set((Matrix4fc)var1.world[this.wristL]).invert().mul((Matrix4fc)var4);
        this.gripReady = true;
    }

    private Matrix4f seatedBroom(Matrix4f var1) {
        WitchRig var2 = this.rig;
        Vector3f var3 = var2.position(this.wristL, new Vector3f());
        Vector3f var4 = new Vector3f((Vector3fc)var3).add(-0.17f, 0.0f, 0.0f);
        Vector3f var5 = var2.position(this.hipR, new Vector3f()).add((Vector3fc)var2.position(this.hipL, this.v)).mul(0.5f);
        var5.y -= 0.115f;
        var5.z -= 0.02f;
        Vector3f var6 = new Vector3f((Vector3fc)var4).sub((Vector3fc)var5);
        var6.y -= 0.02f;
        var6.normalize();
        Vector3f var7 = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f var8 = new Vector3f((Vector3fc)var7).cross((Vector3fc)var6).normalize();
        var7.set((Vector3fc)var6).cross((Vector3fc)var8).normalize();
        Matrix4f var9 = new Matrix4f(var7.x, var7.y, var7.z, 0.0f, var6.x, var6.y, var6.z, 0.0f, var8.x, var8.y, var8.z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
        Vector3f var10 = new Vector3f((Vector3fc)var5).sub((Vector3fc)new Vector3f((Vector3fc)var6).mul(0.74f));
        return var1.translation((Vector3fc)var10).mul((Matrix4fc)var9);
    }

    private static void blend(Matrix4f var0, Matrix4f var1, float var2, Matrix4f var3) {
        Vector3f var4 = var0.getTranslation(new Vector3f());
        Vector3f var5 = var1.getTranslation(new Vector3f());
        Quaternionf var6 = var0.getNormalizedRotation(new Quaternionf());
        Quaternionf var7 = var1.getNormalizedRotation(new Quaternionf());
        var3.translation((Vector3fc)var4.lerp((Vector3fc)var5, var2)).rotate((Quaternionfc)var6.slerp((Quaternionfc)var7, var2));
    }

    private void buildPath(Vector3f var1, Vector3d var2) {
        float var3 = (float)(var2.x - this.origin.x);
        float var4 = (float)(var2.z - this.origin.z);
        float var5 = (float)Math.atan2(var1.z - var4, var1.x - var3);
        float var6 = Math.max(4.0f, (float)Math.hypot(var1.x - var3, var1.z - var4));
        float[][] var7 = new float[][]{{0.0f, 0.0f, 0.0f}, {0.32f, 1.0f, 1.8f}, {0.95f, 2.5f, 4.4f}, {1.65f, 4.0f, 7.2f}, {2.4f, 5.5f, 10.0f}, {3.15f, 7.5f, 13.0f}, {3.85f, 11.0f, 17.0f}, {4.45f, 19.0f, 23.0f}, {4.85f, 32.0f, 31.0f}, {5.1f, 48.0f, 40.0f}};
        this.path = new Vector3f[var7.length];
        float var8 = var1.y;
        for (int var9 = 0; var9 < var7.length; ++var9) {
            Vector3f var12;
            float var10 = var5 + var7[var9][0];
            float var11 = var6 + var7[var9][1];
            Vector3f vector3f = var12 = var9 == 0 ? new Vector3f((Vector3fc)var1) : new Vector3f(var3 + (float)Math.cos(var10) * var11, var1.y + var7[var9][2], var4 + (float)Math.sin(var10) * var11);
            if (var9 > 0) {
                double var13;
                if (this.ground != null && !Double.isNaN(var13 = this.ground.top(this.origin.x + (double)var12.x, this.origin.z + (double)var12.z))) {
                    var12.y = Math.max(var12.y, (float)(var13 - this.origin.y) + 3.6f);
                }
                var12.y = Math.max(var12.y, var8 + 0.4f);
            }
            var8 = var12.y;
            this.path[var9] = var12;
        }
    }

    private Vector3f pathAt(float var1, Vector3f var2) {
        int var3 = this.path.length;
        var1 = Math.max(0.0f, Math.min((float)var3 - 1.001f, var1));
        int var4 = (int)var1;
        float var5 = var1 - (float)var4;
        Vector3f var6 = this.path[Math.max(0, var4 - 1)];
        Vector3f var7 = this.path[var4];
        Vector3f var8 = this.path[Math.min(var3 - 1, var4 + 1)];
        Vector3f var9 = this.path[Math.min(var3 - 1, var4 + 2)];
        float var10 = var5 * var5;
        float var11 = var10 * var5;
        for (int var12 = 0; var12 < 3; ++var12) {
            float var13 = var6.get(var12);
            float var14 = var7.get(var12);
            float var15 = var8.get(var12);
            float var16 = var9.get(var12);
            var2.setComponent(var12, 0.5f * (2.0f * var14 + (-var13 + var15) * var5 + (2.0f * var13 - 5.0f * var14 + 4.0f * var15 - var16) * var10 + (-var13 + 3.0f * var14 - 3.0f * var15 + var16) * var11));
        }
        return var2;
    }

    private void fly(float var1, float var2, float var3, Vector3d var4) {
        if (var1 < 14.7f) {
            this.flyPos.set(0.0f, var3, 0.0f);
            this.flyVel.zero();
            this.flyYaw = this.bodyYaw;
            this.flyYawVel = 0.0f;
            this.flyRoll = 0.0f;
            this.flyRollVel = 0.0f;
            this.pathS = 0.0f;
            this.flyPitch = -0.16f * WitchPerformance.smooth(WitchPerformance.span(var1, 13.7f, 14.7f));
            this.flyPitchVel = 0.0f;
            this.model.translation((Vector3fc)this.flyPos).rotateY(this.flyYaw).rotateX(this.flyPitch);
        } else {
            Vector3f var10;
            if (this.path == null) {
                this.buildPath(new Vector3f(0.0f, var3, 0.0f), var4);
            }
            float var5 = var1 - 14.7f;
            float var6 = 7.0f * WitchPerformance.smooth(WitchPerformance.span(var5, 0.0f, 2.8f)) + 0.35f;
            Vector3f var7 = this.pathAt(this.pathS, new Vector3f());
            Vector3f var8 = this.pathAt(this.pathS + 0.02f, new Vector3f());
            float var9 = Math.max(0.05f, var8.distance((Vector3fc)var7) / 0.02f);
            this.pathS += var6 * var2 / var9;
            if (this.vanishStart == Float.MAX_VALUE && (this.pathS > (float)this.path.length - 2.4f || var5 > 12.5f)) {
                this.vanishStart = var1;
            }
            Vector3f vector3f = var10 = this.pathS >= (float)this.path.length - 1.01f ? new Vector3f((Vector3fc)this.flyPos).fma(var2, (Vector3fc)this.flyVel) : this.pathAt(this.pathS, new Vector3f());
            if (var2 > 0.0f) {
                Vector3f var11 = new Vector3f((Vector3fc)var10).sub((Vector3fc)this.flyPos).div(var2);
                this.flyVel.lerp((Vector3fc)var11, 1.0f - (float)Math.exp(-var2 * 12.0f));
            }
            this.flyPos.set((Vector3fc)var10);
            Vector3f var19 = this.pathAt(this.pathS + 0.22f, new Vector3f()).sub((Vector3fc)var10);
            if (var19.lengthSquared() < 1.0E-6f) {
                var19.set((Vector3fc)this.flyVel);
            }
            float var12 = var19.lengthSquared() > 1.0E-6f ? (float)Math.atan2(var19.x, var19.z) : this.flyYaw;
            float var13 = (float)Math.sqrt(var19.x * var19.x + var19.z * var19.z);
            float var14 = -((float)Math.atan2(var19.y, Math.max(0.05f, var13))) * 0.5f;
            float var15 = 7.0f;
            float var16 = 2.0f * (float)Math.sqrt(var15);
            this.flyYawVel += (WitchPerformance.wrap(var12 - this.flyYaw) * var15 - this.flyYawVel * var16) * var2;
            this.flyYaw = WitchPerformance.wrap(this.flyYaw + this.flyYawVel * var2);
            this.flyPitchVel += ((var14 - this.flyPitch) * var15 - this.flyPitchVel * var16) * var2;
            this.flyPitch += this.flyPitchVel * var2;
            float var17 = Math.max(-0.55f, Math.min(0.55f, -this.flyYawVel * 0.5f));
            this.flyRollVel += ((var17 - this.flyRoll) * 5.0f - this.flyRollVel * 2.0f * (float)Math.sqrt(5.0)) * var2;
            this.flyRoll += this.flyRollVel * var2;
            float var18 = 0.06f * (float)Math.sin(var1 * 2.6f);
            this.model.translation(this.flyPos.x, this.flyPos.y + var18, this.flyPos.z).rotateY(this.flyYaw).rotateX(this.flyPitch).rotateZ(this.flyRoll);
        }
    }

    public Vector3f broomTail(Vector3f var1) {
        return new Matrix4f((Matrix4fc)this.model).mul((Matrix4fc)this.broom).transformPosition(var1.set(0.0f, 0.22f, 0.0f));
    }

    public Vector3f center(Vector3f var1) {
        return this.model.transformPosition(this.rig.position(this.chest, var1));
    }

    public Vector3f rightHand(Vector3f var1) {
        Vector3f var2 = new Vector3f();
        for (int var3 = 1; var3 < 5; ++var3) {
            var2.add((Vector3fc)this.rig.position(this.fingersR[var3][0], this.v));
        }
        var2.div(4.0f);
        return this.model.transformPosition(var1.set((Vector3fc)var2));
    }

    public Vector3f bodyAt(float var1, float var2, Vector3f var3) {
        float var4 = var1 > 1.45f ? 0.22f : (var1 > 0.9f ? 0.2f : 0.16f);
        var3.set((float)Math.cos(var2) * var4, var1, (float)Math.sin(var2) * var4);
        return this.model.transformPosition(var3);
    }

    public Vector3f velocity(Vector3f var1) {
        return var1.set((Vector3fc)this.flyVel);
    }

    public float speed() {
        return this.flyVel.length();
    }

    public static interface Ground {
        public double top(double var1, double var3);
    }
}

