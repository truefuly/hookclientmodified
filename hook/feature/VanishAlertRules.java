/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1934
 */
package hook.feature;

import hook.utils.ext.ModuleChat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1934;

public final class VanishAlertRules {
    public static final List<String> DONUT_STAFF;
    private static final Set<String> STAFF_KEYS;

    private VanishAlertRules() {
    }

    public static boolean isStaffName(String var0) {
        return var0 != null && STAFF_KEYS.contains(var0.toLowerCase(Locale.ROOT));
    }

    public static boolean isInYourRegion(UUID var0) {
        return false;
    }

    public static boolean alarmedRecently(UUID var0) {
        return false;
    }

    public static ModuleChat.Body switchedTo(ModuleChat.Body var0, String var1) {
        if (var1 == null) {
            return var0.text("switched game mode");
        }
        var0.text("switched to ");
        if (class_1934.field_9219.method_8381().equals(var1)) {
            return var0.danger(var1);
        }
        return class_1934.field_9220.method_8381().equals(var1) ? var0.warn(var1) : var0.name(var1);
    }

    private static Set<String> staffKeys() {
        HashSet<String> var0 = new HashSet<String>();
        for (String var2 : DONUT_STAFF) {
            var0.add(var2.toLowerCase(Locale.ROOT));
        }
        return Set.copyOf(var0);
    }

    static {
        STAFF_KEYS = VanishAlertRules.staffKeys();
        String[] var10000 = new String[]{"FluffyMaster07", "archivePedro", "Munkerlich", "Frenk_Btw", "Napooo_", "auzzitech", "CryptoDaveYT", "W1zox_", "ZEEF69", "showered", "CaptainMoose35", "BobIsFound", "NoahvdAa", "0GSummer", "LzouzMP5", "Pastagamer08", "u_vv", "Owen1212055", "Splaterd", "Fallerfly"};
        DONUT_STAFF = List.of(var10000);
    }
}

