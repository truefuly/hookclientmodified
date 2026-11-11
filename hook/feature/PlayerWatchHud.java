/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1068
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_5348
 *  net.minecraft.class_640
 *  net.minecraft.class_7532
 *  net.minecraft.class_8685
 *  net.minecraft.class_9848
 */
package hook.feature;

import hook.utils.ext.NameFilter;
import java.util.UUID;
import net.minecraft.class_1068;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5348;
import net.minecraft.class_640;
import net.minecraft.class_7532;
import net.minecraft.class_8685;
import net.minecraft.class_9848;

public final class PlayerWatchHud {
    private static final float LIFE_SECONDS = 1.5f;
    private static final float TITLE_SECONDS = 1.25f;
    private static final int TITLE_IN = 10;
    private static final int TITLE_STAY = 40;
    private static final int TITLE_OUT = 15;
    private static final float NOTE_SECONDS = 4.5f;
    private static final class_2561 NOTE = class_2561.method_43470((String)"Good luck, HookVisuals. Be careful.");
    private static final int NOTE_COLOR = 0xFFFF55;
    private static UUID watcher;
    private static boolean hidden;
    private static String title;
    private static int stage;
    private static long startNanos;
    private static long noteNanos;
    private static boolean active;

    private PlayerWatchHud() {
    }

    public static void watching(UUID var0, String var1, int var2) {
        class_310 var3 = class_310.method_1551();
        if (var3.field_1687 != null && var1 != null) {
            String var4 = NameFilter.apply(var1);
            watcher = var0;
            hidden = !var4.equals(var1);
            title = var2 > 0 ? var4 + " +" + var2 : var4;
            stage = 0;
            startNanos = System.nanoTime();
            noteNanos = 0L;
            active = true;
        }
    }

    public static boolean hasWork() {
        if (active && class_310.method_1551().field_1687 == null) {
            active = false;
        }
        return active || noteNanos != 0L;
    }

    public static void render(class_332 var0) {
        if (PlayerWatchHud.hasWork()) {
            if (active) {
                class_310 var1 = class_310.method_1551();
                float var2 = (float)(System.nanoTime() - startNanos) / 1.0E9f;
                if (stage == 0 && var2 >= 1.25f) {
                    stage = 1;
                    var1.field_1705.method_34001(10, 40, 15);
                    var1.field_1705.method_34002((class_2561)class_2561.method_43470((String)"WATCHING YOU").method_27695(new class_124[]{class_124.field_1061, class_124.field_1067}));
                    var1.field_1705.method_34004((class_2561)class_2561.method_43470((String)title).method_27695(new class_124[]{class_124.field_1061, class_124.field_1067}));
                } else if (stage == 1 && var2 >= 4.5f) {
                    stage = 2;
                    var1.field_1705.method_1742();
                    noteNanos = System.nanoTime();
                    active = false;
                }
                if (active && var2 < 1.5f && watcher != null) {
                    PlayerWatchHud.drawFace(var0, var1, var2 / 1.5f);
                }
            }
            PlayerWatchHud.renderNote(var0);
        }
    }

    private static void drawFace(class_332 var0, class_310 var1, float var2) {
        class_640 var4;
        class_8685 var3 = null;
        if (!hidden && var1.method_1562() != null && (var4 = var1.method_1562().method_2871(watcher)) != null) {
            var3 = var4.method_52810();
        }
        if (var3 == null) {
            var3 = class_1068.method_4648((UUID)watcher);
        }
        float var11 = 0.05f + 0.6f * (float)Math.sin((double)var2 * Math.PI);
        double var5 = Math.toRadians(60.0 - 150.0 * (double)var2);
        float var7 = (float)Math.cos(var5);
        float var8 = (float)var0.method_51443() * 0.42f;
        var0.method_51448().pushMatrix();
        var0.method_51448().translate((float)var0.method_51421() / 2.0f, (float)var0.method_51443() / 2.0f - var8 * 0.08f);
        var0.method_51448().scale(Math.max(0.04f, Math.abs(var7)), 1.0f);
        int var9 = Math.round(var8);
        int var10 = class_9848.method_61330((int)Math.round(Math.max(0.0f, Math.min(1.0f, var11)) * 255.0f), (int)0xFFFFFF);
        class_7532.method_44443((class_332)var0, (class_8685)var3, (int)(-var9 / 2), (int)(-var9 / 2), (int)var9, (int)var10);
        var0.method_51448().popMatrix();
    }

    private static void renderNote(class_332 var0) {
        if (noteNanos != 0L) {
            float var1 = (float)(System.nanoTime() - noteNanos) / 1.0E9f;
            if (var1 >= 4.0f) {
                noteNanos = 0L;
            } else {
                float var2 = var1 < 0.5f ? var1 / 0.5f : Math.min(1.0f, (4.0f - var1) / 1.0f);
                int var3 = Math.round(var2 * 255.0f);
                if (var3 > 8) {
                    class_327 var4 = class_310.method_1551().field_1772;
                    var0.method_51448().pushMatrix();
                    var0.method_51448().translate((float)var0.method_51421() / 2.0f, (float)var0.method_51443() / 2.0f);
                    var0.method_51448().scale(2.0f, 2.0f);
                    var0.method_51439(var4, NOTE, -var4.method_27525((class_5348)NOTE) / 2, -4, class_9848.method_61330((int)var3, (int)0xFFFF55), true);
                    var0.method_51448().popMatrix();
                }
            }
        }
    }
}

