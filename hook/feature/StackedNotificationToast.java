/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10799
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_3532
 *  net.minecraft.class_368
 *  net.minecraft.class_368$class_369
 *  net.minecraft.class_374
 *  net.minecraft.class_5348
 *  net.minecraft.class_9334
 */
package hook.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3532;
import net.minecraft.class_368;
import net.minecraft.class_374;
import net.minecraft.class_5348;
import net.minecraft.class_9334;

@Environment(value=EnvType.CLIENT)
public class StackedNotificationToast
implements class_368 {
    private static final class_2960 BACKGROUND = class_2960.method_60656((String)"toast/advancement");
    private static final int TITLE_COLOR = -30465;
    private static final int MESSAGE_COLOR = -1;
    private static final int TEXT_X = 30;
    private static final int TEXT_PAD = 4;
    private static final int MAX_WIDTH = 240;
    private static final long DISPLAY_MILLIS = 5000L;
    private final Object token;
    private final class_1799 icon;
    private final boolean sound;
    private class_2561 title;
    private class_2561 message;
    private int width;
    private int count = 1;
    private boolean changed;
    private boolean hiding;
    private long changedAt;
    private class_368.class_369 wanted = class_368.class_369.field_2209;

    public StackedNotificationToast(class_1799 var1, class_2561 var2, class_2561 var3, boolean var4) {
        this(field_2208, var1, var2, var3, var4);
    }

    private StackedNotificationToast(Object var1, class_1799 var2, class_2561 var3, class_2561 var4, boolean var5) {
        this.token = var1;
        this.icon = var2.method_7972();
        this.icon.method_57379(class_9334.field_49641, (Object)true);
        this.title = var3;
        this.message = var4;
        this.sound = var5;
        this.fit();
    }

    public static void addOrUpdate(class_1799 var0, String var1, class_2561 var2, boolean var3) {
        StackedNotificationToast.addOrUpdate(var0, var1, null, var2, var3);
    }

    public static void addOrUpdate(class_1799 var0, String var1, String var2, class_2561 var3, boolean var4) {
        class_310 var5 = class_310.method_1551();
        if (!var5.method_18854()) {
            var5.execute(() -> StackedNotificationToast.addOrUpdate(var0, var1, var2, var3, var4));
        } else {
            class_374 var6 = var5.method_1566();
            StackedNotificationToast var7 = (StackedNotificationToast)var6.method_1997(StackedNotificationToast.class, (Object)var1);
            if (var7 == null) {
                var6.method_1999((class_368)new StackedNotificationToast(var1, var0, (class_2561)class_2561.method_43470((String)var1), var3, var4));
            } else {
                ++var7.count;
                if (var2 != null) {
                    var7.title = class_2561.method_43470((String)(var7.count + " " + var2));
                }
                var7.message = var3;
                var7.changed = true;
                var7.fit();
            }
        }
    }

    private void fit() {
        class_327 var1 = class_310.method_1551().field_1772;
        int var2 = Math.max(var1.method_27525((class_5348)this.title), var1.method_27525((class_5348)this.message));
        this.width = class_3532.method_15340((int)(30 + var2 + 4), (int)160, (int)240);
    }

    public Object method_1987() {
        return this.token;
    }

    public class_3414 method_66530() {
        return this.sound ? (class_3414)class_3417.field_14793.comp_349() : null;
    }

    public class_368.class_369 method_61988() {
        return this.wanted;
    }

    public int method_29049() {
        return this.width;
    }

    public void method_61989(class_374 var1, long var2) {
        if (!this.hiding) {
            if (this.changed) {
                this.changedAt = var2;
                this.changed = false;
            }
            this.hiding = (double)(var2 - this.changedAt) >= 5000.0 * var1.method_48221();
            this.wanted = this.hiding ? class_368.class_369.field_2209 : class_368.class_369.field_2210;
        }
    }

    public void method_71995() {
        if (this.changed) {
            this.hiding = false;
            class_310.method_1551().method_1566().method_1999((class_368)this);
        }
    }

    public void method_1986(class_332 var1, class_327 var2, long var3) {
        int var5 = this.width / 2;
        StackedNotificationToast.drawBackground(var1, 0, 0, var5);
        StackedNotificationToast.drawBackground(var1, 160 - (this.width - var5), var5, this.width - var5);
        boolean var6 = this.message.getString().isEmpty();
        this.drawText(var1, var2, this.title, var6 ? 12 : 7, -30465);
        if (!var6) {
            this.drawText(var1, var2, this.message, 18, -1);
        }
        var1.method_51445(this.icon, 8, 8);
    }

    private static void drawBackground(class_332 var0, int var1, int var2, int var3) {
        var0.method_70846(class_10799.field_56883, BACKGROUND, 160, 32, var1, 0, var2, 0, var3, 32);
    }

    private void drawText(class_332 var1, class_327 var2, class_2561 var3, int var4, int var5) {
        int var6 = this.width - 30 - 4;
        int var7 = var2.method_27525((class_5348)var3);
        if (var7 <= var6) {
            var1.method_51439(var2, var3, 30, var4, var5, false);
        } else {
            var1.method_51448().pushMatrix();
            var1.method_51448().translate(30.0f, (float)var4);
            var1.method_51448().scale((float)var6 / (float)var7, 1.0f);
            var1.method_51439(var2, var3, 0, 0, var5, false);
            var1.method_51448().popMatrix();
        }
    }
}

