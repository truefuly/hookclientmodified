/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2487
 *  net.minecraft.class_2495
 *  net.minecraft.class_2499
 *  net.minecraft.class_2501
 *  net.minecraft.class_2505
 *  net.minecraft.class_2507
 *  net.minecraft.class_2520
 *  net.minecraft.class_2680
 */
package com.hookaddon.schematic;

import com.hookaddon.schematic.BitPacking;
import com.hookaddon.schematic.BlockStateResolver;
import com.hookaddon.schematic.Schematic;
import com.hookaddon.schematic.SchematicException;
import com.hookaddon.schematic.SchematicFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_2246;
import net.minecraft.class_2487;
import net.minecraft.class_2495;
import net.minecraft.class_2499;
import net.minecraft.class_2501;
import net.minecraft.class_2505;
import net.minecraft.class_2507;
import net.minecraft.class_2520;
import net.minecraft.class_2680;

public final class LitematicaFormat
implements SchematicFormat {
    private static final long MAX_FILE_BYTES = 0x10000000L;
    private static final class_2680 AIR = class_2246.field_10124.method_9564();

    @Override
    public String id() {
        return "litematic";
    }

    @Override
    public String title() {
        return "Litematica (.litematic)";
    }

    @Override
    public boolean supports(String fileName) {
        return fileName.toLowerCase(Locale.ROOT).endsWith(".litematic");
    }

    @Override
    public Schematic load(Path file) throws SchematicException, IOException {
        if (Files.size(file) > 0x10000000L) {
            throw new SchematicException("file is larger than 256 MiB, refusing to load it");
        }
        class_2487 root = NbtFiles.read(file, "Litematica");
        if (root == null) {
            throw new SchematicException("'" + String.valueOf(file.getFileName()) + "' contains no NBT data");
        }
        class_2520 class_25202 = root.method_10580("Regions");
        if (!(class_25202 instanceof class_2487)) {
            throw new SchematicException("not a Litematica schematic: missing the Regions tag");
        }
        class_2487 regions = (class_2487)class_25202;
        int dataVersion = root.method_68083("MinecraftDataVersion", -1);
        Map<String, String> metadata = this.readMetadata(root);
        ArrayList<Schematic.Region> parsedRegions = new ArrayList<Schematic.Region>();
        ArrayList<String> problems = new ArrayList<String>();
        for (String regionName : regions.method_10541()) {
            class_2520 class_25203 = regions.method_10580(regionName);
            if (!(class_25203 instanceof class_2487)) continue;
            class_2487 regionTag = (class_2487)class_25203;
            try {
                parsedRegions.add(this.readRegion(regionName, regionTag, problems));
            }
            catch (SchematicException e) {
                throw new SchematicException("region '" + regionName + "': " + e.getMessage(), e);
            }
        }
        if (parsedRegions.isEmpty()) {
            throw new SchematicException("the schematic contains no regions");
        }
        if (!problems.isEmpty()) {
            throw new SchematicException(LitematicaFormat.join(problems));
        }
        return new Schematic(file.getFileName().toString(), this.id(), file.toAbsolutePath().toString(), dataVersion, metadata, parsedRegions);
    }

    private Schematic.Region readRegion(String name, class_2487 tag, List<String> problems) throws SchematicException {
        class_2499 paletteTag;
        int[] size = LitematicaFormat.readBlockPos(tag, "Size");
        if (size == null) {
            throw new SchematicException("the region has no Size tag");
        }
        int[] position = LitematicaFormat.readBlockPos(tag, "Position");
        if (position == null) {
            position = new int[]{0, 0, 0};
        }
        int minX = size[0] >= 0 ? position[0] : position[0] + size[0] + 1;
        int minY = size[1] >= 0 ? position[1] : position[1] + size[1] + 1;
        int minZ = size[2] >= 0 ? position[2] : position[2] + size[2] + 1;
        int sizeX = Math.abs(size[0]);
        int sizeY = Math.abs(size[1]);
        int sizeZ = Math.abs(size[2]);
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            throw new SchematicException("the region has an empty size");
        }
        long volumeLong = (long)sizeX * (long)sizeY * (long)sizeZ;
        if (volumeLong > Integer.MAX_VALUE) {
            throw new SchematicException("the region is too large (" + volumeLong + " blocks)");
        }
        int volume = (int)volumeLong;
        class_2520 class_25202 = tag.method_10580("BlockStatePalette");
        if (!(class_25202 instanceof class_2499) || (paletteTag = (class_2499)class_25202).isEmpty()) {
            throw new SchematicException("the region has no block palette");
        }
        class_2680[] palette = new class_2680[paletteTag.size()];
        for (int i = 0; i < palette.length; ++i) {
            Object object = paletteTag.get(i);
            if (object instanceof class_2487) {
                class_2487 entry = (class_2487)object;
                try {
                    palette[i] = BlockStateResolver.fromLitematic(entry);
                    continue;
                }
                catch (SchematicException e) {
                    problems.add(name + ": " + e.getMessage());
                }
            } else {
                problems.add(name + ": palette entry " + i + " is not a compound");
            }
            palette[i] = AIR;
        }
        int[] paletteIndices = this.readBlockStates(tag, volume, palette.length, problems);
        class_2680[] states = new class_2680[volume];
        for (int i = 0; i < volume; ++i) {
            states[i] = palette[paletteIndices[i]];
        }
        return new Schematic.Region(name, minX, minY, minZ, sizeX, sizeY, sizeZ, states);
    }

    private static int[] readBlockPos(class_2487 tag, String key) {
        class_2520 class_25202 = tag.method_10580(key);
        if (class_25202 instanceof class_2487) {
            class_2487 posTag = (class_2487)class_25202;
            return new int[]{posTag.method_68083("x", 0), posTag.method_68083("y", 0), posTag.method_68083("z", 0)};
        }
        class_25202 = tag.method_10580(key);
        if (class_25202 instanceof class_2495) {
            class_2495 arrayTag = (class_2495)class_25202;
            int[] raw = arrayTag.method_10588();
            return (int[])(raw.length >= 3 ? raw : null);
        }
        return null;
    }

    private int[] readBlockStates(class_2487 tag, int volume, int paletteSize, List<String> problems) throws SchematicException {
        int bits = BitPacking.bitsForPalette(paletteSize);
        long[] longs = null;
        class_2520 class_25202 = tag.method_10580("BlockStates");
        if (class_25202 instanceof class_2501) {
            class_2501 array = (class_2501)class_25202;
            longs = array.method_10615();
        } else {
            class_25202 = tag.method_10580("BlockStates");
            if (class_25202 instanceof class_2495) {
                class_2495 array = (class_2495)class_25202;
                int[] raw = array.method_10588();
                longs = new long[raw.length];
                for (int i = 0; i < raw.length; ++i) {
                    longs[i] = (long)raw[i] & 0xFFFFFFFFL;
                }
            }
        }
        if (longs == null) {
            throw new SchematicException("the region has no BlockStates data");
        }
        try {
            return BitPacking.unpack(longs, bits, volume);
        }
        catch (IllegalArgumentException e) {
            throw new SchematicException("cannot decode the block data: " + e.getMessage(), e);
        }
    }

    private static String join(List<String> problems) {
        int shown = Math.min(3, problems.size());
        return problems.size() + " block palette entries could not be resolved (" + String.join((CharSequence)"; ", problems.subList(0, shown)) + (problems.size() > shown ? "; ..." : "") + ")";
    }

    private Map<String, String> readMetadata(class_2487 root) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<String, String>();
        root.method_10562("Metadata").ifPresent(meta -> {
            for (String key : new String[]{"Name", "Author", "Description", "RegionCount", "TotalBlocks", "TotalVolume", "TimeCreated", "TimeModified"}) {
                String value = meta.method_68564(key, "");
                if (value.isEmpty()) continue;
                metadata.put(key, value);
            }
        });
        return metadata;
    }

    static final class NbtFiles {
        private NbtFiles() {
        }

        static class_2487 read(Path file, String formatName) throws SchematicException, IOException {
            boolean gzip;
            byte[] header = new byte[2];
            try (InputStream in = Files.newInputStream(file, new OpenOption[0]);){
                gzip = in.readNBytes(header, 0, 2) == 2 && (header[0] & 0xFF) == 31 && (header[1] & 0xFF) == 139;
            }
            try {
                if (gzip) {
                    return class_2507.method_30613((Path)file, (class_2505)class_2505.method_53898());
                }
                return class_2507.method_10633((Path)file);
            }
            catch (IOException | RuntimeException e) {
                throw new SchematicException("cannot read the " + formatName + " file '" + String.valueOf(file.getFileName()) + "': " + e.getMessage(), e);
            }
        }
    }
}

