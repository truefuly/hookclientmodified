/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.schematic;

import com.hookaddon.schematic.Schematic;
import com.hookaddon.schematic.SchematicException;
import java.io.IOException;
import java.nio.file.Path;

public interface SchematicFormat {
    public String id();

    public String title();

    public boolean supports(String var1);

    public Schematic load(Path var1) throws SchematicException, IOException;
}

