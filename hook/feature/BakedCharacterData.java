/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.BakedCharacterPart;
import hook.feature.CharacterBone;
import hook.feature.CharacterDetailLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class BakedCharacterData {
    public final String id;
    public final String name;
    public final CharacterDetailLevel detail;
    public final float scale;
    public final BakedCharacterPart[] parts;
    public final int motionSlots;
    public final int atlasWidth;
    public final int atlasHeight;
    public final int[][] atlasMips;
    public final float whiteU;
    public final float whiteV;
    public final float minY;
    public final float maxY;
    public final float hipY;
    public final boolean firstPersonArms;
    public final long buildMillis;

    BakedCharacterData(String id, String name, CharacterDetailLevel detail, float scale, BakedCharacterPart[] parts, int motionSlots, int atlasWidth, int atlasHeight, int[][] atlasMips, float whiteU, float whiteV, float minY, float maxY, float hipY, boolean firstPersonArms, long buildMillis) {
        this.id = id;
        this.name = name;
        this.detail = detail;
        this.scale = scale;
        this.parts = parts;
        this.motionSlots = motionSlots;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
        this.atlasMips = atlasMips;
        this.whiteU = whiteU;
        this.whiteV = whiteV;
        this.minY = minY;
        this.maxY = maxY;
        this.hipY = hipY;
        this.firstPersonArms = firstPersonArms;
        this.buildMillis = buildMillis;
    }

    public int triangles() {
        int total = 0;
        for (BakedCharacterPart part : this.parts) {
            total += part.triangles();
        }
        return total;
    }

    public BakedCharacterPart part(CharacterBone bone) {
        for (BakedCharacterPart part : this.parts) {
            if (part.extra || part.bone != bone) continue;
            return part;
        }
        return null;
    }
}

