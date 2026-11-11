/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.AoiDesign;
import hook.feature.BakedCharacterAsset;
import hook.feature.CharacterDefinition;
import hook.feature.LocalCharacterAssetLibrary;
import hook.feature.LunaDesign;
import hook.feature.MikaDesign;
import hook.feature.MikuDesign;
import hook.feature.SakuraDesign;
import hook.feature.TralaleroDesign;
import hook.feature.TungTungSahurDesign;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class Character3dHelper {
    private static final List<CharacterDefinition> BUILT_IN = List.of(new SakuraDesign(), new LunaDesign(), new MikaDesign(), new AoiDesign(), new MikuDesign(), BakedCharacterAsset.builtIn("cj", "CJ", "/assets/hook/characters/cj.anc"), BakedCharacterAsset.builtIn("neighbor", "Neighbor", "/assets/hook/characters/neighbor.anc"), new TungTungSahurDesign(), new TralaleroDesign());
    private static volatile List<CharacterDefinition> designs = BUILT_IN;

    private Character3dHelper() {
    }

    public static List<CharacterDefinition> all() {
        return designs;
    }

    static boolean refresh() {
        ArrayList<CharacterDefinition> updated = new ArrayList<CharacterDefinition>(BUILT_IN);
        ArrayList<String> knownNames = new ArrayList<String>(Character3dHelper.names(BUILT_IN));
        for (BakedCharacterAsset asset : LocalCharacterAssetLibrary.scan()) {
            if (knownNames.contains(asset.name())) continue;
            knownNames.add(asset.name());
            updated.add(asset);
        }
        List<CharacterDefinition> previous = designs;
        designs = List.copyOf(updated);
        if (previous.size() != updated.size()) {
            return true;
        }
        for (int index = 0; index < updated.size(); ++index) {
            if (previous.get(index).id().equals(((CharacterDefinition)updated.get(index)).id())) continue;
            return true;
        }
        return false;
    }

    static List<String> names() {
        return Character3dHelper.names(designs);
    }

    private static List<String> names(List<CharacterDefinition> definitions) {
        ArrayList<String> result = new ArrayList<String>(definitions.size());
        for (CharacterDefinition definition : definitions) {
            result.add(definition.name());
        }
        return result;
    }

    static CharacterDefinition byName(String name) {
        for (CharacterDefinition definition : designs) {
            if (!definition.name().equals(name)) continue;
            return definition;
        }
        return designs.get(0);
    }
}

