/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.loader.api.FabricLoader
 */
package thetruetrident.cartcore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(value=EnvType.CLIENT)
public class cartConfig {
    private static final Gson B = new GsonBuilder().setPrettyPrinting().create();
    private static final Path A = FabricLoader.getInstance().getConfigDir().resolve("cartcore.json");
    public boolean tickVariance = true;
    public boolean instaCarting = false;
    public boolean cartOverloading = true;
    public boolean crossbowCarting = false;
    public boolean safeXbow = true;
    public int fireAngle = 55;
    public int crossbowStepDelay = 0;
    public boolean autoWebbing = true;
    public boolean cartReplenishing = false;
    public double cartReplenishingSpeed = 20.0;
    public int replenishSlotsAbove = 7;
    public boolean soundEffects = true;
    public boolean chatLogs = true;

    public static cartConfig load() {
        if (Files.notExists(A, new LinkOption[0])) {
            cartConfig fresh = new cartConfig();
            fresh.save();
            return fresh;
        }
        try (BufferedReader reader = Files.newBufferedReader(A)) {
            cartConfig loaded = B.fromJson((Reader) reader, cartConfig.class);
            if (loaded == null) loaded = new cartConfig();
            loaded.A();
            return loaded;
        } catch (IOException | RuntimeException exception) {
            return new cartConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(A.getParent(), new FileAttribute[0]);
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(A, new OpenOption[0]);){
                B.toJson((Object)this, (Appendable)bufferedWriter);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private void A() {
        this.cartReplenishingSpeed = Math.max(0.5, this.cartReplenishingSpeed);
        this.replenishSlotsAbove = Math.max(0, Math.min(8, this.replenishSlotsAbove));
        this.fireAngle = Math.max(25, Math.min(85, this.fireAngle));
        this.crossbowStepDelay = Math.max(0, Math.min(20, this.crossbowStepDelay));
    }
}

