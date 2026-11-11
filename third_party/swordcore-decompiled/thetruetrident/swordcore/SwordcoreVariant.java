/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package thetruetrident.swordcore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class SwordcoreVariant {
    private static final String VARIANT_RESOURCE = "/assets/swordcore/swordcore_variant.properties";

    private SwordcoreVariant() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean isPremium() {
        Properties properties = new Properties();
        try (InputStream stream = SwordcoreVariant.class.getResourceAsStream(VARIANT_RESOURCE);){
            if (stream == null) {
                boolean bl2 = true;
                return bl2;
            }
            properties.load(stream);
            boolean bl = Boolean.parseBoolean(properties.getProperty("premium", "true"));
            return bl;
        }
        catch (IOException ignored) {
            return true;
        }
    }
}

