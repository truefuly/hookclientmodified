/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2248
 *  net.minecraft.class_2487
 *  net.minecraft.class_2680
 *  net.minecraft.class_2769
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 */
package com.hookaddon.schematic;

import com.hookaddon.schematic.SchematicException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_2248;
import net.minecraft.class_2487;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public final class BlockStateResolver {
    private BlockStateResolver() {
    }

    public static class_2680 fromLitematic(class_2487 entry) throws SchematicException {
        String name = entry.method_68564("Name", "");
        if (name.isEmpty()) {
            throw new SchematicException("palette entry without a block name");
        }
        LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
        entry.method_10562("Properties").ifPresent(props -> {
            for (String key : props.method_10541()) {
                properties.put(key, props.method_68564(key, ""));
            }
        });
        return BlockStateResolver.resolve(name, properties);
    }

    public static class_2680 fromPaletteKey(String key) throws SchematicException {
        int bracket = key.indexOf(91);
        String id = bracket >= 0 ? key.substring(0, bracket) : key;
        LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
        if (bracket >= 0 && key.endsWith("]")) {
            String body = key.substring(bracket + 1, key.length() - 1);
            for (String pair : body.split(",")) {
                if (pair.isEmpty()) continue;
                int equals = pair.indexOf(61);
                if (equals <= 0) {
                    throw new SchematicException("malformed property '" + pair + "' in '" + key + "'");
                }
                properties.put(pair.substring(0, equals).trim(), pair.substring(equals + 1).trim());
            }
        }
        return BlockStateResolver.resolve(id, properties);
    }

    public static class_2680 resolve(String id, Map<String, String> properties) throws SchematicException {
        class_2960 identifier = class_2960.method_12829((String)id);
        if (identifier == null) {
            throw new SchematicException("'" + id + "' is not a valid block id");
        }
        class_2248 block = (class_2248)class_7923.field_41175.method_63535(identifier);
        if (block == null) {
            throw new SchematicException("block '" + id + "' is not registered (is the mod that adds it installed?)");
        }
        class_2680 state = block.method_9564();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            class_2680 updated = BlockStateResolver.apply(state, property.getKey(), property.getValue());
            if (updated == null) {
                throw new SchematicException("'" + id + "' does not accept " + property.getKey() + "=" + property.getValue());
            }
            state = updated;
        }
        return state;
    }

    private static class_2680 apply(class_2680 state, String propertyName, String propertyValue) {
        for (class_2769 property : state.method_28501()) {
            if (!property.method_11899().equals(propertyName)) continue;
            Optional parsed = property.method_11900(propertyValue);
            if (parsed.isEmpty()) {
                return null;
            }
            return (class_2680)state.method_11657(property, (Comparable)parsed.get());
        }
        return null;
    }
}

