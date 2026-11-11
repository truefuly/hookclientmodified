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
public enum CharacterDetailLevel {
    LOW("Low", 7500, 0.42f),
    MEDIUM("Medium", 15000, 0.3f),
    HIGH("High", 30000, 0.21f);

    public final String label;
    public final int budget;
    public final float step;

    private CharacterDetailLevel(String nullxx, int nullxxx, float nullxxxx) {
        this.label = nullxx;
        this.budget = nullxxx;
        this.step = nullxxxx;
    }

    public static CharacterDetailLevel byLabel(String var0) {
        for (CharacterDetailLevel var4 : CharacterDetailLevel.values()) {
            if (!var4.label.equalsIgnoreCase(var0) && !var4.name().equalsIgnoreCase(var0)) continue;
            return var4;
        }
        return MEDIUM;
    }
}

