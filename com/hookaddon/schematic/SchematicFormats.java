/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.schematic;

import com.hookaddon.schematic.LitematicaFormat;
import com.hookaddon.schematic.Schematic;
import com.hookaddon.schematic.SchematicException;
import com.hookaddon.schematic.SchematicFormat;
import com.hookaddon.schematic.SpongeSchematicFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class SchematicFormats {
    private static final List<SchematicFormat> FORMATS = List.of(new LitematicaFormat(), new SpongeSchematicFormat());

    private SchematicFormats() {
    }

    public static List<SchematicFormat> all() {
        return FORMATS;
    }

    public static SchematicFormat byId(String id) {
        for (SchematicFormat format : FORMATS) {
            if (!format.id().equalsIgnoreCase(id)) continue;
            return format;
        }
        return null;
    }

    public static SchematicFormat forFile(String fileName) {
        for (SchematicFormat format : FORMATS) {
            if (!format.supports(fileName)) continue;
            return format;
        }
        return null;
    }

    public static boolean isSupported(String fileName) {
        return SchematicFormats.forFile(fileName) != null;
    }

    public static Schematic load(Path file) throws SchematicException, IOException {
        SchematicFormat format = SchematicFormats.forFile(file.getFileName().toString());
        if (format == null) {
            throw new SchematicException("'" + String.valueOf(file.getFileName()) + "' is not a supported schematic file (" + String.join((CharSequence)", ", SchematicFormats.extensions()) + ")");
        }
        return format.load(file);
    }

    public static List<String> extensions() {
        ArrayList<String> extensions = new ArrayList<String>();
        block8: for (SchematicFormat format : FORMATS) {
            switch (format.id()) {
                case "litematic": {
                    extensions.add("*.litematic");
                    continue block8;
                }
                case "sponge": {
                    extensions.add("*.schem");
                    continue block8;
                }
            }
            extensions.add("*." + format.id());
        }
        return extensions;
    }

    public static List<Path> listFiles(Path directory) {
        if (directory == null || !Files.isDirectory(directory, new LinkOption[0])) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0]))
                .filter(path -> SchematicFormats.isSupported(path.getFileName().toString()))
                .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                .toList();
        } catch (IOException e) {
            return List.of();
        }
    }
}

