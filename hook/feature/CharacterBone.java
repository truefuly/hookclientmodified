/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public enum CharacterBone {
    HEAD(0.0f, 24.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    BODY(0.0f, 24.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    RIGHT_ARM(-5.0f, 22.0f, 0.0f, -1.0f, -10.0f, 2.0f),
    LEFT_ARM(5.0f, 22.0f, 0.0f, 1.0f, -10.0f, 2.0f),
    RIGHT_LEG(-1.9f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f),
    LEFT_LEG(1.9f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f);

    public final float pivotX;
    public final float pivotY;
    public final float pivotZ;
    final float gripX;
    final float gripY;
    final float gripZ;

    private CharacterBone(float nullxx, float nullxxx, float nullxxxx, float nullxxxxx, float nullxxxxxx, float nullxxxxxxx) {
        this.pivotX = nullxx;
        this.pivotY = nullxxx;
        this.pivotZ = nullxxxx;
        this.gripX = nullxxxxx;
        this.gripY = nullxxxxxx;
        this.gripZ = nullxxxxxxx;
    }

    public boolean arm() {
        return this == RIGHT_ARM || this == LEFT_ARM;
    }

    public boolean leg() {
        return this == RIGHT_LEG || this == LEFT_LEG;
    }

    public CharacterBone opposite() {
        return switch (this.ordinal()) {
            case 2 -> LEFT_ARM;
            case 3 -> RIGHT_ARM;
            case 4 -> LEFT_LEG;
            case 5 -> RIGHT_LEG;
            default -> this;
        };
    }
}

