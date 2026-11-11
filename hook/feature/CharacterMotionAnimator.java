/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10055
 *  net.minecraft.class_3532
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 */
package hook.feature;

import hook.feature.BakedCharacterData;
import hook.feature.BakedCharacterPart;
import hook.feature.BoneMotionProfile;
import hook.feature.CharacterBone;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_3532;
import net.minecraft.class_591;
import net.minecraft.class_630;

@Environment(value=EnvType.CLIENT)
final class CharacterMotionAnimator {
    private static final long CONSTANT_0055 = 20000000000L;
    private static final Int2ObjectOpenHashMap<MotionState> STATES = new Int2ObjectOpenHashMap();
    private static final BoneMotionProfile.MotionFrame INPUT = new BoneMotionProfile.MotionFrame();
    private static final float[] CONSTANT_0101 = new float[3];
    private static long numericState0002;

    private CharacterMotionAnimator() {
    }

    static float[] update(class_10055 var0, BakedCharacterData var1, class_591 var2, float[] var3) {
        float var7;
        if (var1.motionSlots == 0) {
            return null;
        }
        long var4 = System.nanoTime();
        CharacterMotionAnimator.prune(var4);
        MotionState var6 = (MotionState)STATES.get(var0.field_53528);
        if (var6 == null || var6.model != var1) {
            var6 = new MotionState(var1);
            var6.bodyRot = var0.field_53446;
            STATES.put(var0.field_53528, (Object)var6);
        }
        float f = var7 = var6.nanos == 0L ? 0.0f : (float)(var4 - var6.nanos) / 1.0E9f;
        if (var6.nanos != 0L && var7 < 5.0E-4f) {
            return var6.angles;
        }
        var6.nanos = var4;
        float var8 = class_3532.method_15393((float)(var0.field_53446 - var6.bodyRot));
        var6.bodyRot = var0.field_53446;
        var6.turnRate = var7 > 0.0f && var7 < 0.25f ? (var6.turnRate += (var8 / var7 - var6.turnRate) * Math.min(1.0f, var7 * 12.0f)) : 0.0f;
        BoneMotionProfile.MotionFrame var9 = INPUT;
        var9.time = var0.field_53328 / 20.0f;
        var9.walkPos = var0.field_53450;
        var9.walkSpeed = var0.field_53451;
        var9.turnRate = var6.turnRate;
        CharacterMotionAnimator.bone(var9, CharacterBone.HEAD, var2.field_3398, var3);
        CharacterMotionAnimator.bone(var9, CharacterBone.BODY, var2.field_3391, var3);
        CharacterMotionAnimator.bone(var9, CharacterBone.RIGHT_ARM, var2.field_3401, var3);
        CharacterMotionAnimator.bone(var9, CharacterBone.LEFT_ARM, var2.field_27433, var3);
        CharacterMotionAnimator.bone(var9, CharacterBone.RIGHT_LEG, var2.field_3392, var3);
        CharacterMotionAnimator.bone(var9, CharacterBone.LEFT_LEG, var2.field_3397, var3);
        for (BakedCharacterPart var13 : var1.parts) {
            if (var13.motionSlot < 0 || var13.motion == null) continue;
            var13.motion.target(var9, var13.bone, CONSTANT_0101);
            var13.motion.step(var6.spring, var13.motionSlot * 6, CONSTANT_0101, var7);
            System.arraycopy(var6.spring, var13.motionSlot * 6, var6.angles, var13.motionSlot * 3, 3);
        }
        return var6.angles;
    }

    private static void bone(BoneMotionProfile.MotionFrame var0, CharacterBone var1, class_630 var2, float[] var3) {
        int var4 = var1.ordinal();
        var0.pitch[var4] = var3[var4];
        var0.yaw[var4] = -var2.field_3675;
        var0.roll[var4] = -var2.field_3674;
    }

    private static void prune(long var0) {
        if (var0 - numericState0002 >= 5000000000L) {
            numericState0002 = var0;
            STATES.values().removeIf(var2 -> var0 - var2.nanos > 20000000000L);
        }
    }

    static void clear() {
        STATES.clear();
    }

    private static final class MotionState {
        final BakedCharacterData model;
        final float[] spring;
        final float[] angles;
        float bodyRot;
        float turnRate;
        long nanos;

        MotionState(BakedCharacterData var1) {
            this.model = var1;
            this.spring = new float[var1.motionSlots * 6];
            this.angles = new float[var1.motionSlots * 3];
            for (int var2 = 0; var2 < var1.motionSlots; ++var2) {
                this.spring[var2 * 6] = Float.intBitsToFloat(2143289344);
            }
        }
    }
}

