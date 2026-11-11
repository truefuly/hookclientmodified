/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.BoneMotionProfile;
import hook.feature.CharacterBone;
import hook.feature.MeshBufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class BakedCharacterPart {
    public final String name;
    public final CharacterBone bone;
    public final int parent;
    public final boolean extra;
    public final float pivotX;
    public final float pivotY;
    public final float pivotZ;
    public final float gripX;
    public final float gripY;
    public final float gripZ;
    public final BoneMotionProfile motion;
    public final MeshBufferBuilder base;
    public final MeshBufferBuilder outline;
    public final MeshBufferBuilder decals;
    public final int motionSlot;
    public final float swingScale;
    public final float swingLimit;
    public final boolean jointed;
    public final float jointX;
    public final float jointY;
    public final float jointZ;
    public final boolean neck;
    public final float lookPitch;
    public final float lookYaw;
    public final float lookLimit;

    BakedCharacterPart(String name, CharacterBone bone, int parent, boolean extra, float pivotX, float pivotY, float pivotZ, float gripX, float gripY, float gripZ, BoneMotionProfile motion, MeshBufferBuilder base, MeshBufferBuilder outline, MeshBufferBuilder decals, int motionSlot, float swingScale, float swingLimit, boolean jointed, float jointX, float jointY, float jointZ, boolean neck, float lookPitch, float lookYaw, float lookLimit) {
        this.name = name;
        this.bone = bone;
        this.parent = parent;
        this.extra = extra;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;
        this.gripX = gripX;
        this.gripY = gripY;
        this.gripZ = gripZ;
        this.motion = motion;
        this.base = base;
        this.outline = outline;
        this.decals = decals;
        this.motionSlot = motionSlot;
        this.swingScale = swingScale;
        this.swingLimit = swingLimit;
        this.jointed = jointed;
        this.jointX = jointX;
        this.jointY = jointY;
        this.jointZ = jointZ;
        this.neck = neck;
        this.lookPitch = lookPitch;
        this.lookYaw = lookYaw;
        this.lookLimit = lookLimit;
    }

    public boolean bends() {
        return this.motion != null && this.motion.bendPower() > 0.0f;
    }

    public boolean swings() {
        return this.swingScale != 1.0f || this.swingLimit > 0.0f;
    }

    public float swingPitch(float basePitch, float inputSwing, float amount) {
        if (amount > 0.0f && this.swings()) {
            float pitch = BoneMotionProfile.soft(inputSwing * this.swingScale, this.swingLimit);
            return basePitch + Math.min(1.0f, amount) * (pitch - inputSwing);
        }
        return basePitch;
    }

    public int triangles() {
        int baseTriangles = this.base == null ? 0 : this.base.triangles;
        int outlineTriangles = this.outline == null ? 0 : this.outline.triangles;
        int decalTriangles = this.decals == null ? 0 : this.decals.triangles;
        return baseTriangles + outlineTriangles + decalTriangles;
    }
}

