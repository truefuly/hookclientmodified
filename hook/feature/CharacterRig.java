/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.CharacterBone;
import hook.feature.CharacterPartBuilder;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class CharacterRig {
    float scale = 1.0f;
    int outlineColor = -14017512;
    float outline = 0.2f;
    float occlusion = 0.55f;
    float flat;
    boolean firstPersonArms = true;
    final CharacterPartBuilder[] bones = new CharacterPartBuilder[CharacterBone.values().length];
    final List<CharacterPartBuilder> extras = new ArrayList<CharacterPartBuilder>();

    public CharacterRig scale(float value) {
        if (value <= 0.05f || value >= 8.0f) {
            throw new IllegalArgumentException("scale must be between 0.05 and 8");
        }
        this.scale = value;
        return this;
    }

    public CharacterRig outline(int color, float thickness) {
        this.outlineColor = color;
        this.outline = Math.max(0.0f, thickness);
        return this;
    }

    public CharacterRig occlusion(float value) {
        this.occlusion = Math.max(0.0f, Math.min(1.0f, value));
        return this;
    }

    public CharacterRig flatLight(float value) {
        this.flat = Math.max(0.0f, Math.min(1.0f, value));
        return this;
    }

    public CharacterRig firstPersonArms(boolean enabled) {
        this.firstPersonArms = enabled;
        return this;
    }

    public CharacterPartBuilder part(CharacterBone bone) {
        CharacterPartBuilder builder = this.bones[bone.ordinal()];
        if (builder == null) {
            this.bones[bone.ordinal()] = builder = new CharacterPartBuilder(bone.name(), bone, null, false);
        }
        return builder;
    }

    public CharacterPartBuilder extra(String name, CharacterBone bone) {
        return this.extra(name, this.part(bone));
    }

    public CharacterPartBuilder extra(String name, CharacterPartBuilder parent) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("extra needs a name");
        }
        for (CharacterPartBuilder existing : this.extras) {
            if (!existing.name.equals(name)) continue;
            throw new IllegalArgumentException("duplicate extra name: " + name);
        }
        for (CharacterBone bone : CharacterBone.values()) {
            if (!bone.name().equals(name)) continue;
            throw new IllegalArgumentException("extra name clashes with a bone: " + name);
        }
        CharacterPartBuilder extra = new CharacterPartBuilder(name, parent.bone, parent, true);
        this.extras.add(extra);
        return extra;
    }

    public float scale() {
        return this.scale;
    }
}

