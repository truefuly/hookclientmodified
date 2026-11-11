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
import hook.feature.Sdf3D;
import hook.feature.SurfaceDecal;
import hook.feature.Vec3;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class CharacterPartBuilder {
    final String name;
    final CharacterBone bone;
    final CharacterPartBuilder parent;
    final boolean extra;
    Sdf3D shape;
    float pivotX;
    float pivotY;
    float pivotZ;
    boolean pivotSet;
    float gripX;
    float gripY;
    float gripZ;
    boolean gripSet;
    int outlineColor;
    float outline = -1.0f;
    boolean outlineColorSet;
    final List<SurfaceDecal> decals = new ArrayList<SurfaceDecal>();
    BoneMotionProfile motion;
    float detail = 1.0f;
    float resolution = 1.0f;
    float flat = -1.0f;
    float occlusion = -1.0f;
    float swing = 1.0f;
    float swingLimit;
    float jointX;
    float jointY;
    float jointZ;
    float jointDirX;
    float jointDirY = -1.0f;
    float jointDirZ;
    float jointBlend;
    boolean jointSet;
    boolean neck;
    float lookPitch;
    float lookYaw;
    float lookLimit;

    CharacterPartBuilder(String var1, CharacterBone var2, CharacterPartBuilder var3, boolean var4) {
        this.name = var1;
        this.bone = var2;
        this.parent = var3;
        this.extra = var4;
        if (var3 == null) {
            this.pivotX = var2.pivotX;
            this.pivotY = var2.pivotY;
            this.pivotZ = var2.pivotZ;
        } else {
            this.pivotX = var3.pivotX;
            this.pivotY = var3.pivotY;
            this.pivotZ = var3.pivotZ;
        }
    }

    public String name() {
        return this.name;
    }

    public CharacterBone bone() {
        return this.bone;
    }

    public boolean extra() {
        return this.extra;
    }

    public Sdf3D shape() {
        return this.shape;
    }

    public CharacterPartBuilder shape(Sdf3D var1) {
        this.shape = var1;
        return this;
    }

    public CharacterPartBuilder pivot(float var1, float var2, float var3) {
        this.pivotX = var1;
        this.pivotY = var2;
        this.pivotZ = var3;
        this.pivotSet = true;
        return this;
    }

    public Vec3 pivot() {
        return new Vec3(this.pivotX, this.pivotY, this.pivotZ);
    }

    public CharacterPartBuilder grip(float var1, float var2, float var3) {
        this.gripX = var1;
        this.gripY = var2;
        this.gripZ = var3;
        this.gripSet = true;
        return this;
    }

    public CharacterPartBuilder outline(float var1) {
        this.outline = Math.max(0.0f, var1);
        return this;
    }

    public CharacterPartBuilder outline(int var1, float var2) {
        this.outlineColor = var1;
        this.outlineColorSet = true;
        this.outline = Math.max(0.0f, var2);
        return this;
    }

    public CharacterPartBuilder noOutline() {
        this.outline = 0.0f;
        return this;
    }

    public CharacterPartBuilder decal(SurfaceDecal var1) {
        this.decals.add(var1);
        return this;
    }

    public CharacterPartBuilder motion(BoneMotionProfile var1) {
        this.motion = var1;
        return this;
    }

    public CharacterPartBuilder detail(float var1) {
        this.detail = Math.max(0.05f, var1);
        return this;
    }

    public CharacterPartBuilder resolution(float var1) {
        this.resolution = Math.max(0.35f, Math.min(3.0f, var1));
        return this;
    }

    public CharacterPartBuilder flatLight(float var1) {
        this.flat = Math.max(0.0f, Math.min(1.0f, var1));
        return this;
    }

    public CharacterPartBuilder occlusion(float var1) {
        this.occlusion = Math.max(0.0f, Math.min(1.0f, var1));
        return this;
    }

    public CharacterPartBuilder swing(float var1) {
        return this.swing(var1, 0.0f);
    }

    public CharacterPartBuilder swing(float var1, float var2) {
        if (this.extra || !this.bone.arm() && !this.bone.leg()) {
            throw new IllegalStateException(this.name + ": swing() only applies to the arm and leg bone parts");
        }
        this.swing = Math.max(0.0f, Math.min(2.0f, var1));
        this.swingLimit = Math.max(0.0f, Math.min(90.0f, var2));
        return this;
    }

    public CharacterPartBuilder joint(float var1, float var2, float var3) {
        return this.joint(var1, var2, var3, 0.7f);
    }

    public CharacterPartBuilder joint(float var1, float var2, float var3, float var4) {
        if (this.extra || !this.bone.arm() && !this.bone.leg()) {
            throw new IllegalStateException(this.name + ": joint() only applies to the arm and leg bone parts");
        }
        this.jointX = var1;
        this.jointY = var2;
        this.jointZ = var3;
        this.jointDirX = 0.0f;
        this.jointDirY = -1.0f;
        this.jointDirZ = 0.0f;
        this.jointBlend = Math.max(0.1f, var4);
        this.jointSet = true;
        this.neck = false;
        return this;
    }

    public CharacterPartBuilder neck(float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
        if (!this.bone.arm() && !this.bone.leg()) {
            float var8 = (float)Math.sqrt(var4 * var4 + var5 * var5 + var6 * var6);
            if (!(var8 > 1.0E-4f)) {
                throw new IllegalArgumentException(this.name + ": neck() needs a direction");
            }
            this.jointX = var1;
            this.jointY = var2;
            this.jointZ = var3;
            this.jointDirX = var4 / var8;
            this.jointDirY = var5 / var8;
            this.jointDirZ = var6 / var8;
            this.jointBlend = Math.max(0.1f, var7);
            this.jointSet = true;
            this.neck = true;
            return this;
        }
        throw new IllegalStateException(this.name + ": limbs bend with joint(), not neck()");
    }

    public CharacterPartBuilder look(float var1, float var2, float var3) {
        if (!this.neck) {
            throw new IllegalStateException(this.name + ": look() needs a neck()");
        }
        this.lookPitch = var1;
        this.lookYaw = var2;
        this.lookLimit = Math.max(0.0f, var3);
        return this;
    }

    public CharacterPartBuilder mirrorOf(CharacterPartBuilder var1) {
        this.shape = var1.shape == null ? null : var1.shape.mirrored();
        this.pivotX = -var1.pivotX;
        this.pivotY = var1.pivotY;
        this.pivotZ = var1.pivotZ;
        this.pivotSet = true;
        if (var1.gripSet) {
            this.gripX = -var1.gripX;
            this.gripY = var1.gripY;
            this.gripZ = var1.gripZ;
            this.gripSet = true;
        }
        this.outline = var1.outline;
        this.outlineColor = var1.outlineColor;
        this.outlineColorSet = var1.outlineColorSet;
        this.decals.clear();
        for (SurfaceDecal var3 : var1.decals) {
            this.decals.add(var3.mirrored());
        }
        this.motion = var1.motion == null ? null : var1.motion.mirrored();
        this.detail = var1.detail;
        this.resolution = var1.resolution;
        this.flat = var1.flat;
        this.occlusion = var1.occlusion;
        this.swing = var1.swing;
        this.swingLimit = var1.swingLimit;
        this.jointX = -var1.jointX;
        this.jointY = var1.jointY;
        this.jointZ = var1.jointZ;
        this.jointDirX = -var1.jointDirX;
        this.jointDirY = var1.jointDirY;
        this.jointDirZ = var1.jointDirZ;
        this.jointBlend = var1.jointBlend;
        this.jointSet = var1.jointSet;
        this.neck = var1.neck;
        this.lookPitch = var1.lookPitch;
        this.lookYaw = var1.lookYaw;
        this.lookLimit = var1.lookLimit;
        return this;
    }

    float gripX() {
        return this.gripSet ? this.gripX : this.pivotX + this.bone.gripX;
    }

    float gripY() {
        return this.gripSet ? this.gripY : this.pivotY + this.bone.gripY;
    }

    float gripZ() {
        return this.gripSet ? this.gripZ : this.pivotZ + this.bone.gripZ;
    }
}

