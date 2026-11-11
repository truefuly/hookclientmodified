/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10017
 *  net.minecraft.class_10042
 *  net.minecraft.class_10055
 *  net.minecraft.class_11659
 *  net.minecraft.class_1306
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4608
 *  net.minecraft.class_5603
 *  net.minecraft.class_572$class_573
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 *  net.minecraft.class_922
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package hook.feature;

import hook.feature.BakedCharacterData;
import hook.feature.BakedCharacterPart;
import hook.feature.Character3dModule;
import hook.feature.CharacterBone;
import hook.feature.CharacterJointAngles;
import hook.feature.CharacterLighting;
import hook.feature.CharacterMotionAnimator;
import hook.feature.DirectionalLighting;
import hook.feature.MeshBufferBuilder;
import hook.feature.WornCharacterModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_1306;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_5603;
import net.minecraft.class_572;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_922;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
final class CharacterMeshRenderer {
    private static final int GHOST_ALPHA = 38;
    private static final CharacterBone[] BONES = CharacterBone.values();
    private static final Quaternionf ROTATION = new Quaternionf();
    private static final float[] LEVELS = new float[153];
    private static final float[] PITCH = new float[BONES.length];
    private static final float[] JOINT = new float[BONES.length];
    private static final float[] JOINT_LEVELS = new float[153];
    private static float[] scratch = new float[24576];

    private CharacterMeshRenderer() {
    }

    static void submit(WornCharacterModel var0, class_591 var1, class_4587 var2, class_11659 var3, int var4, class_10055 var5) {
        BakedCharacterData var6 = var0.model;
        BakedCharacterPart[] var7 = var6.parts;
        boolean var8 = var5.field_53333;
        int var9 = class_922.method_23622((class_10042)var5, (float)0.0f);
        float[] var10 = CharacterMeshRenderer.pitches(var5, var6, var1);
        float[] var11 = CharacterMeshRenderer.joints(var5, var6);
        float var12 = var1.field_3398.field_3654;
        float var13 = -var1.field_3398.field_3675;
        float[] var14 = CharacterMotionAnimator.update(var5, var6, var1, var10);
        var2.method_22903();
        var1.method_63512().method_22703(var2);
        CharacterMeshRenderer.lift(var2, var5, var6);
        for (int var15 = 0; var15 < var7.length; ++var15) {
            BakedCharacterPart var16 = var7[var15];
            if (var16.base == null && var16.outline == null && var16.decals == null) continue;
            boolean var17 = var16.bends() && var14 != null;
            float var18 = var17 ? var14[var16.motionSlot * 3] : 0.0f;
            float var19 = var17 ? var14[var16.motionSlot * 3 + 1] : 0.0f;
            float var20 = var17 ? var14[var16.motionSlot * 3 + 2] : 0.0f;
            float var21 = 0.0f;
            float var22 = 0.0f;
            if (var16.neck) {
                var21 = CharacterMeshRenderer.look(var16.lookPitch * var12, var16.lookLimit);
                var22 = CharacterMeshRenderer.look(var16.lookYaw * var13, var16.lookLimit);
            } else if (var16.jointed) {
                var21 = var11[var16.bone.ordinal()];
            }
            var2.method_22903();
            CharacterMeshRenderer.chain(var2, var1, var6, var15, var14, !var17, null, var10);
            CharacterMeshRenderer.submitPart(var0, var16, var2, var3, var4, var9, var8, var17, var18, var19, var20, var21, var22);
            var2.method_22909();
        }
        var2.method_22909();
    }

    static boolean firstPersonHand(class_591 var0, class_4587 var1, class_11659 var2, int var3, class_630 var4) {
        WornCharacterModel var5 = Character3dModule.firstPerson();
        if (var5 == null) {
            return false;
        }
        BakedCharacterData var7 = var5.model;
        CharacterBone var6 = var4 == var0.field_3401 ? CharacterBone.RIGHT_ARM : CharacterBone.LEFT_ARM;
        BakedCharacterPart var8 = var7.part(var6);
        if (var8 != null && var8.base != null) {
            var4.method_41923();
            var4.field_3674 = var6 == CharacterBone.RIGHT_ARM ? 0.1f : -0.1f;
            BakedCharacterPart[] var9 = var7.parts;
            for (int var10 = 0; var10 < var9.length; ++var10) {
                BakedCharacterPart var11 = var9[var10];
                if (var11.bone != var6 || var11.base == null && var11.outline == null && var11.decals == null) continue;
                var1.method_22903();
                CharacterMeshRenderer.chain(var1, var0, var7, var10, null, true, var4, null);
                CharacterMeshRenderer.submitPart(var5, var11, var1, var2, var3, class_4608.field_21444, false, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
                var1.method_22909();
            }
            return true;
        }
        return false;
    }

    private static void submitPart(WornCharacterModel var0, BakedCharacterPart var1, class_4587 var2, class_11659 var3, int var4, int var5, boolean var6, boolean var7, float var8, float var9, float var10, float var11, float var12) {
        if (var6) {
            if (var1.base != null) {
                var3.method_73529(1).method_73483(var2, var0.ghost, (var9x, var10x) -> CharacterMeshRenderer.emit(var9x, var10x, var1.base, var4, var5, 38, var7, var8, var9, var10, var1, var11, var12));
            }
        } else {
            if (var1.base != null || var1.outline != null) {
                var3.method_73483(var2, var0.solid, (var9x, var10x) -> {
                    if (var1.base != null) {
                        CharacterMeshRenderer.emit(var9x, var10x, var1.base, var4, var5, 255, var7, var8, var9, var10, var1, var11, var12);
                    }
                    if (var1.outline != null) {
                        CharacterMeshRenderer.emit(var9x, var10x, var1.outline, var4, var5, 255, var7, var8, var9, var10, var1, var11, var12);
                    }
                });
            }
            if (var1.decals != null) {
                var3.method_73529(1).method_73483(var2, var0.decal, (var9x, var10x) -> CharacterMeshRenderer.emit(var9x, var10x, var1.decals, var4, var5, 255, var7, var8, var9, var10, var1, var11, var12));
            }
        }
    }

    static boolean translateToHand(class_591 var0, class_10055 var1, class_1306 var2, class_4587 var3) {
        float var10;
        WornCharacterModel var4 = Character3dModule.worn((class_10017)var1);
        if (var4 == null) {
            return false;
        }
        BakedCharacterData var5 = var4.model;
        CharacterBone var6 = var2 == class_1306.field_6183 ? CharacterBone.RIGHT_ARM : CharacterBone.LEFT_ARM;
        BakedCharacterPart var7 = var5.part(var6);
        if (var7 == null) {
            return false;
        }
        float var8 = var5.scale;
        class_630 var9 = CharacterMeshRenderer.vanilla(var0, var6);
        var0.method_63512().method_22703(var3);
        CharacterMeshRenderer.lift(var3, var1, var5);
        CharacterMeshRenderer.pivot(var3, var9, var7, var8, CharacterMeshRenderer.pitch(var1, var7, var9.field_3654));
        float f = var10 = var7.jointed ? CharacterJointAngles.angle(var1, var6, CharacterMeshRenderer.walk(var1, var6) * CharacterMeshRenderer.weight(var1, var6)) : 0.0f;
        if (var10 != 0.0f) {
            float var11 = var7.jointY * var8 / 16.0f;
            float var12 = var7.jointZ * var8 / 16.0f;
            var3.method_46416(0.0f, -var11, -var12);
            var3.method_22907((Quaternionfc)ROTATION.rotationX(var10));
            var3.method_46416(0.0f, var11, var12);
        }
        var3.method_46416((var7.gripX - var7.pivotX) * var8 / 16.0f, -(var7.gripY - var7.pivotY) * var8 / 16.0f, -(var7.gripZ - var7.pivotZ) * var8 / 16.0f);
        var3.method_22905(var8, var8, var8);
        var3.method_46416((var2 == class_1306.field_6183 ? 1.0f : -1.0f) / 16.0f, -0.625f, 0.125f);
        return true;
    }

    private static float[] pitches(class_10055 var0, BakedCharacterData var1, class_591 var2) {
        float[] var3 = PITCH;
        for (CharacterBone var7 : BONES) {
            float var8 = CharacterMeshRenderer.vanilla((class_591)var2, (CharacterBone)var7).field_3654;
            BakedCharacterPart var9 = !var7.arm() && !var7.leg() ? null : var1.part(var7);
            var3[var7.ordinal()] = var9 == null ? var8 : CharacterMeshRenderer.pitch(var0, var9, var8);
        }
        return var3;
    }

    private static float pitch(class_10055 var0, BakedCharacterPart var1, float var2) {
        float var3 = var1.swingPitch(var2, CharacterMeshRenderer.walk(var0, var1.bone), CharacterMeshRenderer.weight(var0, var1.bone));
        if (var1.jointed && var1.bone.leg() && var0.field_53410 && !var0.field_53413) {
            var3 -= 0.5f * (1.0f - var0.field_53403);
        }
        return var3;
    }

    private static float[] joints(class_10055 var0, BakedCharacterData var1) {
        float[] var2 = JOINT;
        for (CharacterBone var6 : BONES) {
            BakedCharacterPart var7 = !var6.arm() && !var6.leg() ? null : var1.part(var6);
            var2[var6.ordinal()] = var7 != null && var7.jointed ? CharacterJointAngles.angle(var0, var6, CharacterMeshRenderer.walk(var0, var6) * CharacterMeshRenderer.weight(var0, var6)) : 0.0f;
        }
        return var2;
    }

    private static float look(float var0, float var1) {
        float var2 = var1 * ((float)Math.PI / 180);
        return class_3532.method_15363((float)var0, (float)(-var2), (float)var2);
    }

    private static float walk(class_10055 var0, CharacterBone var1) {
        float var2 = var0.field_53450 * 0.6662f;
        float var3 = var0.field_53451 / var0.field_53405;
        return switch (var1) {
            case CharacterBone.RIGHT_LEG -> class_3532.method_15362((double)var2) * 1.4f * var3;
            case CharacterBone.LEFT_LEG -> class_3532.method_15362((double)(var2 + (float)Math.PI)) * 1.4f * var3;
            case CharacterBone.RIGHT_ARM -> class_3532.method_15362((double)(var2 + (float)Math.PI)) * var3;
            case CharacterBone.LEFT_ARM -> class_3532.method_15362((double)var2) * var3;
            default -> 0.0f;
        };
    }

    private static float weight(class_10055 var0, CharacterBone var1) {
        class_572.class_573 var3;
        if (var1.leg()) {
            return var0.field_53413 ? 0.0f : 1.0f - var0.field_53403;
        }
        if (!var1.arm()) {
            return 0.0f;
        }
        class_572.class_573 var2 = var1 == CharacterBone.RIGHT_ARM ? var0.field_55304 : var0.field_55306;
        class_572.class_573 class_5732 = var3 = var1 == CharacterBone.RIGHT_ARM ? var0.field_55306 : var0.field_55304;
        if (var3.method_30156()) {
            return 0.0f;
        }
        float var4 = switch (var2) {
            case class_572.class_573.field_3409 -> 1.0f;
            case class_572.class_573.field_3410, class_572.class_573.field_3406, class_572.class_573.field_42877, class_572.class_573.field_63542 -> 0.5f;
            default -> 0.0f;
        };
        return var0.field_53414 ? var4 : var4 * (1.0f - var0.field_53403);
    }

    private static void lift(class_4587 var0, class_10055 var1, BakedCharacterData var2) {
        float var3;
        if (var1.field_53413 && (var3 = 12.0f - var2.scale * var2.hipY) != 0.0f) {
            var0.method_46416(0.0f, -var3 / 16.0f, 0.0f);
        }
    }

    private static void chain(class_4587 var0, class_591 var1, BakedCharacterData var2, int var3, float[] var4, boolean var5, class_630 var6, float[] var7) {
        BakedCharacterPart var8 = var2.parts[var3];
        float var9 = var2.scale;
        if (!var8.extra) {
            if (var6 == null) {
                class_630 var10 = CharacterMeshRenderer.vanilla(var1, var8.bone);
                CharacterMeshRenderer.pivot(var0, var10, var8, var9, var7 == null ? var10.field_3654 : var7[var8.bone.ordinal()]);
            } else {
                var0.method_46416(var6.field_3657 / 16.0f, var6.field_3656 / 16.0f, var6.field_3655 / 16.0f);
                var0.method_22907((Quaternionfc)ROTATION.rotationZYX(var6.field_3674, var6.field_3675, var6.field_3654));
                float var12 = var8.bone == CharacterBone.RIGHT_ARM ? -1.0f : 1.0f;
                var0.method_46416((var12 - (var8.gripX - var8.pivotX) * var9) / 16.0f, (10.0f + (var8.gripY - var8.pivotY) * var9) / 16.0f, (-2.0f + (var8.gripZ - var8.pivotZ) * var9) / 16.0f);
            }
            float var13 = var9 / 16.0f;
            var0.method_22905(var13, -var13, -var13);
        } else {
            int var11;
            CharacterMeshRenderer.chain(var0, var1, var2, var8.parent, var4, true, var6, var7);
            BakedCharacterPart var14 = var2.parts[var8.parent];
            var0.method_46416(var8.pivotX - var14.pivotX, var8.pivotY - var14.pivotY, var8.pivotZ - var14.pivotZ);
            if (var5 && var4 != null && var8.motionSlot >= 0 && (var4[var11 = var8.motionSlot * 3] != 0.0f || var4[var11 + 1] != 0.0f || var4[var11 + 2] != 0.0f)) {
                var0.method_22907((Quaternionfc)ROTATION.rotationZYX(var4[var11 + 2], var4[var11 + 1], var4[var11]));
            }
        }
    }

    private static void pivot(class_4587 var0, class_630 var1, BakedCharacterPart var2, float var3, float var4) {
        class_5603 var5 = var1.method_41921();
        var0.method_46416((var3 * var2.pivotX + (var1.field_3657 - var5.comp_2997()) * var3) / 16.0f, (24.0f - var3 * var2.pivotY + (var1.field_3656 - var5.comp_2998()) * var3) / 16.0f, (-var3 * var2.pivotZ + (var1.field_3655 - var5.comp_2999()) * var3) / 16.0f);
        if (var4 != 0.0f || var1.field_3675 != 0.0f || var1.field_3674 != 0.0f) {
            var0.method_22907((Quaternionfc)ROTATION.rotationZYX(var1.field_3674, var1.field_3675, var4));
        }
        if (var1.field_37938 != 1.0f || var1.field_37939 != 1.0f || var1.field_37940 != 1.0f) {
            var0.method_22905(var1.field_37938, var1.field_37939, var1.field_37940);
        }
    }

    private static class_630 vanilla(class_591 var0, CharacterBone var1) {
        return switch (var1) {
            case CharacterBone.RIGHT_LEG -> var0.field_3392;
            case CharacterBone.LEFT_LEG -> var0.field_3397;
            case CharacterBone.RIGHT_ARM -> var0.field_3401;
            case CharacterBone.LEFT_ARM -> var0.field_27433;
            case CharacterBone.HEAD -> var0.field_3398;
            case CharacterBone.BODY -> var0.field_3391;
            default -> throw new MatchException(null, null);
        };
    }

    private static void emit(class_4587.class_4665 var0, class_4588 var1, MeshBufferBuilder var2, int var3, int var4, int var5, boolean var6, float var7, float var8, float var9, BakedCharacterPart var10, float var11, float var12) {
        DirectionalLighting var42;
        int var13 = var2.vertexCount();
        if (scratch.length < var13 * 6) {
            scratch = new float[Math.max(var13 * 6, scratch.length * 2)];
        }
        float[] var14 = scratch;
        Matrix4f var15 = var0.method_23761();
        Matrix3f var16 = var0.method_23762();
        float var17 = var15.m00();
        float var18 = var15.m10();
        float var19 = var15.m20();
        float var20 = var15.m30();
        float var21 = var15.m01();
        float var22 = var15.m11();
        float var23 = var15.m21();
        float var24 = var15.m31();
        float var25 = var15.m02();
        float var26 = var15.m12();
        float var27 = var15.m22();
        float var28 = var15.m32();
        float var29 = var16.m00();
        float var30 = var16.m10();
        float var31 = var16.m20();
        float var32 = var16.m01();
        float var33 = var16.m11();
        float var34 = var16.m21();
        float var35 = var16.m02();
        float var36 = var16.m12();
        float var37 = var16.m22();
        float[] var38 = var2.pos;
        float[] var39 = var2.nrm;
        float[] var40 = var6 ? var2.weight : null;
        float[] var41 = var2.flat;
        DirectionalLighting directionalLighting = var42 = var41 != null ? CharacterLighting.current() : null;
        if (var40 != null) {
            CharacterMeshRenderer.levels(LEVELS, var7, var8, var9);
        }
        float[] var43 = (float[])(var11 == 0.0f && var12 == 0.0f ? null : var2.joint);
        float var44 = var10.jointX;
        float var45 = var10.jointY;
        float var46 = var10.jointZ;
        if (var43 != null) {
            CharacterMeshRenderer.levels(JOINT_LEVELS, var11, var12, 0.0f);
        }
        for (int var47 = 0; var47 < var13; ++var47) {
            int var54;
            float var48 = var38[var47 * 3];
            float var49 = var38[var47 * 3 + 1];
            float var50 = var38[var47 * 3 + 2];
            float var51 = var39[var47 * 3];
            float var52 = var39[var47 * 3 + 1];
            float var53 = var39[var47 * 3 + 2];
            if (var43 != null && (var54 = Math.round(var43[var47] * 16.0f)) > 0) {
                float[] var55 = JOINT_LEVELS;
                int var56 = var54 * 9;
                float var57 = var48 - var44;
                float var58 = var49 - var45;
                float var59 = var50 - var46;
                var48 = var44 + var55[var56] * var57 + var55[var56 + 1] * var58 + var55[var56 + 2] * var59;
                var49 = var45 + var55[var56 + 3] * var57 + var55[var56 + 4] * var58 + var55[var56 + 5] * var59;
                var50 = var46 + var55[var56 + 6] * var57 + var55[var56 + 7] * var58 + var55[var56 + 8] * var59;
                float var60 = var55[var56] * var51 + var55[var56 + 1] * var52 + var55[var56 + 2] * var53;
                float var61 = var55[var56 + 3] * var51 + var55[var56 + 4] * var52 + var55[var56 + 5] * var53;
                var53 = var55[var56 + 6] * var51 + var55[var56 + 7] * var52 + var55[var56 + 8] * var53;
                var51 = var60;
                var52 = var61;
            }
            if (var40 != null) {
                int var69 = Math.round(var40[var47] * 16.0f) * 9;
                float[] var72 = LEVELS;
                float var75 = var72[var69] * var48 + var72[var69 + 1] * var49 + var72[var69 + 2] * var50;
                float var77 = var72[var69 + 3] * var48 + var72[var69 + 4] * var49 + var72[var69 + 5] * var50;
                float var79 = var72[var69 + 6] * var48 + var72[var69 + 7] * var49 + var72[var69 + 8] * var50;
                float var82 = var72[var69] * var51 + var72[var69 + 1] * var52 + var72[var69 + 2] * var53;
                float var83 = var72[var69 + 3] * var51 + var72[var69 + 4] * var52 + var72[var69 + 5] * var53;
                float var84 = var72[var69 + 6] * var51 + var72[var69 + 7] * var52 + var72[var69 + 8] * var53;
                var48 = var75;
                var49 = var77;
                var50 = var79;
                var51 = var82;
                var52 = var83;
                var53 = var84;
            }
            int var70 = var47 * 6;
            var14[var70] = var17 * var48 + var18 * var49 + var19 * var50 + var20;
            var14[var70 + 1] = var21 * var48 + var22 * var49 + var23 * var50 + var24;
            var14[var70 + 2] = var25 * var48 + var26 * var49 + var27 * var50 + var28;
            float var73 = var29 * var51 + var30 * var52 + var31 * var53;
            float var76 = var32 * var51 + var33 * var52 + var34 * var53;
            float var78 = var35 * var51 + var36 * var52 + var37 * var53;
            float var80 = var73 * var73 + var76 * var76 + var78 * var78;
            if (var80 > 1.0E-12f && Math.abs(var80 - 1.0f) > 0.001f) {
                var80 = 1.0f / (float)Math.sqrt(var80);
                var73 *= var80;
                var76 *= var80;
                var78 *= var80;
            }
            var14[var70 + 3] = var73;
            var14[var70 + 4] = var76;
            var14[var70 + 5] = var78;
            if (var42 == null || !(var41[var47] > 0.0f)) continue;
            var42.flatten(var14, var70 + 3, var41[var47]);
        }
        int[] var62 = var2.quads;
        int[] var63 = var2.color;
        float[] var64 = var2.uv;
        byte[] var65 = var2.glow;
        for (int var66 = 0; var66 < var62.length; ++var66) {
            int var67 = var62[var66];
            int var68 = var67 * 6;
            int var71 = var5 == 255 ? var63[var67] : var63[var67] & 0xFFFFFF | var5 << 24;
            int var74 = var65 != null && var65[var67] != 0 ? 0xF000F0 : var3;
            var1.method_23919(var14[var68], var14[var68 + 1], var14[var68 + 2], var71, var64[var67 * 2], var64[var67 * 2 + 1], var4, var74, var14[var68 + 3], var14[var68 + 4], var14[var68 + 5]);
        }
    }

    private static void levels(float[] var0, float var1, float var2, float var3) {
        for (int var4 = 0; var4 <= 16; ++var4) {
            float var5 = (float)var4 / 16.0f;
            float var6 = (float)Math.cos(var1 * var5);
            float var7 = (float)Math.sin(var1 * var5);
            float var8 = (float)Math.cos(var2 * var5);
            float var9 = (float)Math.sin(var2 * var5);
            float var10 = (float)Math.cos(var3 * var5);
            float var11 = (float)Math.sin(var3 * var5);
            int var12 = var4 * 9;
            var0[var12] = var10 * var8;
            var0[var12 + 1] = var10 * var9 * var7 - var11 * var6;
            var0[var12 + 2] = var10 * var9 * var6 + var11 * var7;
            var0[var12 + 3] = var11 * var8;
            var0[var12 + 4] = var11 * var9 * var7 + var10 * var6;
            var0[var12 + 5] = var11 * var9 * var6 - var10 * var7;
            var0[var12 + 6] = -var9;
            var0[var12 + 7] = var8 * var7;
            var0[var12 + 8] = var8 * var6;
        }
    }
}

