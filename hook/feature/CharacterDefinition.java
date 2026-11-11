/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.CharacterRig;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public abstract class CharacterDefinition {
    private final String id;
    private final String name;

    protected CharacterDefinition(String id, String name) {
        if (id == null || !id.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("character id must match [a-z0-9_]+: " + id);
        }
        this.id = id;
        this.name = name;
    }

    public final String id() {
        return this.id;
    }

    public final String name() {
        return this.name;
    }

    protected abstract void define(CharacterRig var1);

    public final CharacterRig rig() {
        CharacterRig rig = new CharacterRig();
        this.define(rig);
        return rig;
    }

    public static String decodeString(byte[] data, byte[] key, int seed) {
        byte[] seedBytes = Integer.toString(seed).getBytes();
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte)(data[i] ^ seedBytes[i % seedBytes.length]);
            data[i] = (byte)(data[i] ^ key[i % key.length]);
        }
        return new String(data, StandardCharsets.UTF_16);
    }
}

