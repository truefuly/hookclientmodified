/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_5250
 */
package hook.utils.ext;

import hook.feature.UiPalette;
import hook.module.ext.ClientModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5250;

@Environment(value=EnvType.CLIENT)
public final class ModuleChat {
    private static final String HEADER = "HookVisuals";
    private static final int DARK_GREY = 0x555555;
    private static final int GREY = 0xAAAAAA;
    private static final int WHITE = 0xFFFFFF;
    private static final int DANGER = 0xFF5555;
    private static final int GOOD = 0x55FF55;
    private static final int WARN = 0xFFAA00;
    private static final int VALUE = 0x55FFFF;

    private ModuleChat() {
    }

    public static Body body() {
        return new Body();
    }

    public static void send(ClientModule var0, class_2561 var1) {
        ModuleChat.onClientThread(() -> {
            if (var0.notificationsOn()) {
                ModuleChat.addLine(var0, var1);
            }
        });
    }

    public static void send(ClientModule var0, Body var1) {
        ModuleChat.send(var0, var1.build());
    }

    public static void send(ClientModule var0, String var1) {
        ModuleChat.send(var0, (class_2561)class_2561.method_43470((String)var1));
    }

    public static void actionBar(ClientModule var0, Body var1) {
        class_2561 var2 = var1.build();
        ModuleChat.onClientThread(() -> {
            class_310 var2x = class_310.method_1551();
            if (var0.notificationsOn() && var2x.field_1705 != null) {
                var2x.field_1705.method_1758((class_2561)ModuleChat.line(var0, var2), false);
            }
        });
    }

    private static void addLine(ClientModule var0, class_2561 var1) {
        class_310 var2 = class_310.method_1551();
        if (var2.field_1705 != null) {
            var2.field_1705.method_1743().method_1812((class_2561)ModuleChat.line(var0, var1));
        }
    }

    private static class_5250 line(ClientModule var0, class_2561 var1) {
        return class_2561.method_43473().method_10852((class_2561)class_2561.method_43470((String)"[").method_54663(0x555555)).method_10852((class_2561)class_2561.method_43470((String)HEADER).method_54663(UiPalette.accent(1.0f) & 0xFFFFFF)).method_10852((class_2561)class_2561.method_43470((String)"] ").method_54663(0x555555)).method_10852((class_2561)class_2561.method_43470((String)var0.name()).method_54663(0xFFFFFF)).method_10852((class_2561)class_2561.method_43470((String)" : ").method_54663(0x555555)).method_10852((class_2561)class_2561.method_43473().method_54663(0xAAAAAA).method_10852(var1));
    }

    private static void onClientThread(Runnable var0) {
        class_310 var1 = class_310.method_1551();
        if (var1.method_18854()) {
            var0.run();
        } else {
            var1.execute(var0);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Body {
        private final class_5250 root = class_2561.method_43473().method_54663(0xAAAAAA);

        private Body() {
        }

        public Body text(String var1) {
            this.root.method_10852((class_2561)class_2561.method_43470((String)var1));
            return this;
        }

        public Body name(String var1) {
            return this.part(var1, 0xFFFFFF);
        }

        public Body danger(String var1) {
            return this.part(var1, 0xFF5555);
        }

        public Body good(String var1) {
            return this.part(var1, 0x55FF55);
        }

        public Body warn(String var1) {
            return this.part(var1, 0xFFAA00);
        }

        public Body value(Object var1) {
            return this.part(String.valueOf(var1), 0x55FFFF);
        }

        public Body coords(int var1, int var2, int var3) {
            return this.part(var1 + ", " + var2 + ", " + var3, 0x55FFFF);
        }

        public Body append(class_2561 var1) {
            this.root.method_10852(var1);
            return this;
        }

        public class_2561 build() {
            return this.root.method_27661();
        }

        private Body part(String var1, int var2) {
            this.root.method_10852((class_2561)class_2561.method_43470((String)var1).method_54663(var2));
            return this;
        }
    }
}

