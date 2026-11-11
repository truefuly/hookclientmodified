/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2487
 *  net.minecraft.class_2680
 */
package com.hookaddon.schematic;

import com.hookaddon.schematic.BitPacking;
import com.hookaddon.schematic.BlockStateResolver;
import com.hookaddon.schematic.LitematicaFormat;
import com.hookaddon.schematic.Schematic;
import com.hookaddon.schematic.SchematicException;
import com.hookaddon.schematic.SchematicFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_2246;
import net.minecraft.class_2487;
import net.minecraft.class_2680;

public final class SpongeSchematicFormat
implements SchematicFormat {
    private static final long MAX_FILE_BYTES = 0x10000000L;
    private static final class_2680 AIR = class_2246.field_10124.method_9564();

    @Override
    public String id() {
        return "sponge";
    }

    @Override
    public String title() {
        return "Sponge Schematic v2 (.schem)";
    }

    @Override
    public boolean supports(String fileName) {
        return fileName.toLowerCase(Locale.ROOT).endsWith(".schem");
    }

    @Override
    public Schematic load(Path file) throws SchematicException, IOException {
        int[] indices;
        if (Files.size(file) > 0x10000000L) {
            throw new SchematicException("file is larger than 256 MiB, refusing to load it");
        }
        class_2487 root = LitematicaFormat.NbtFiles.read(file, "Sponge");
        if (root == null) {
            throw new SchematicException("'" + String.valueOf(file.getFileName()) + "' contains no NBT data");
        }
        int version = root.method_68083("Version", -1);
        if (version == -1) {
            throw new SchematicException("missing Version tag; this looks like a WorldEdit .schematic (MCEdit) file, which is not supported");
        }
        if (version != 2) {
            throw new SchematicException("schematic version " + version + " is not supported, only version 2");
        }
        int width = root.method_68565("Width", (short)0) & 0xFFFF;
        int height = root.method_68565("Height", (short)0) & 0xFFFF;
        int length = root.method_68565("Length", (short)0) & 0xFFFF;
        if (width <= 0 || height <= 0 || length <= 0) {
            throw new SchematicException("the schematic has an empty size (" + width + "x" + height + "x" + length + ")");
        }
        long volumeLong = (long)width * (long)height * (long)length;
        if (volumeLong > Integer.MAX_VALUE) {
            throw new SchematicException("the schematic is too large (" + volumeLong + " blocks)");
        }
        int volume = (int)volumeLong;
        class_2487 paletteTag = root.method_10562("Palette").orElse(null);
        if (paletteTag == null || paletteTag.method_33133()) {
            throw new SchematicException("the schematic has no palette");
        }
        int paletteMax = root.method_68083("PaletteMax", 0);
        class_2680[] palette = this.readPalette(paletteTag, paletteMax);
        byte[] blockData = root.method_10547("BlockData").orElse(null);
        if (blockData == null) {
            throw new SchematicException("the schematic has no BlockData array");
        }
        try {
            indices = BitPacking.readVarInts(blockData, volume);
        }
        catch (IllegalArgumentException e) {
            throw new SchematicException("cannot decode the block data: " + e.getMessage(), e);
        }
        class_2680[] states = new class_2680[volume];
        for (int i = 0; i < volume; ++i) {
            int paletteIndex = indices[i];
            states[i] = paletteIndex >= 0 && paletteIndex < palette.length ? palette[paletteIndex] : AIR;
        }
        int[] offset = root.method_10561("Offset").orElse(new int[]{0, 0, 0});
        int offsetX = offset.length > 0 ? offset[0] : 0;
        int offsetY = offset.length > 1 ? offset[1] : 0;
        int offsetZ = offset.length > 2 ? offset[2] : 0;
        LinkedHashMap<String, String> metadata = new LinkedHashMap<String, String>();
        root.method_10562("Metadata").ifPresent(meta -> {
            for (String key : meta.method_10541()) {
                String value = meta.method_68564(key, "");
                if (value.isEmpty()) continue;
                metadata.put(key, value);
            }
        });
        metadata.put("DataVersion", Integer.toString(root.method_68083("DataVersion", -1)));
        List<Schematic.Region> regions = List.of(new Schematic.Region("root", offsetX, offsetY, offsetZ, width, height, length, states));
        return new Schematic(file.getFileName().toString(), this.id(), file.toAbsolutePath().toString(), root.method_68083("DataVersion", -1), metadata, regions);
    }

    private class_2680[] readPalette(class_2487 paletteTag, int paletteMax) throws SchematicException {
        int highest = Math.max(paletteMax, 0);
        for (String key : paletteTag.method_10541()) {
            highest = Math.max(highest, paletteTag.method_68083(key, -1) + 1);
        }
        if (highest <= 0) {
            throw new SchematicException("the palette is empty");
        }
        class_2680[] palette = new class_2680[highest];
        ArrayList<Object> problems = new ArrayList<Object>();
        for (String key : paletteTag.method_10541()) {
            int index = paletteTag.method_68083(key, -1);
            if (index < 0 || index >= palette.length) {
                problems.add("'" + key + "' maps to the out of range index " + index);
                continue;
            }
            try {
                palette[index] = BlockStateResolver.fromPaletteKey(key);
            }
            catch (SchematicException e) {
                problems.add(e.getMessage());
                palette[index] = AIR;
            }
        }
        if (!problems.isEmpty()) {
            int shown = Math.min(3, problems.size());
            throw new SchematicException(problems.size() + " palette entries could not be resolved (" + problems.subList(0, shown).stream().map(String::valueOf).collect(java.util.stream.Collectors.joining("; ")) + (problems.size() > shown ? "; ..." : "") + ")");
        }
        return palette;
    }
}

