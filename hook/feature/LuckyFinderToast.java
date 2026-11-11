/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_368
 *  net.minecraft.class_368$class_369
 *  net.minecraft.class_374
 */
package hook.feature;

import net.minecraft.class_10799;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_368;
import net.minecraft.class_374;

public final class LuckyFinderToast
implements class_368 {
    private static final class_2960 TEXTURE = class_2960.method_60656((String)"toast/advancement");
    private static final class_1799 ICON = new class_1799((class_1935)class_1802.field_8687);
    private static final class_2561 TITLE = class_2561.method_43470((String)"Lucky Finder");
    private final class_2561 coordinates;
    private class_368.class_369 visibility = class_368.class_369.field_2210;

    public LuckyFinderToast(int var1, int var2) {
        this.coordinates = class_2561.method_43470((String)("X: " + var1 + " Z: " + var2));
    }

    public class_368.class_369 method_61988() {
        return this.visibility;
    }

    public void method_61989(class_374 var1, long var2) {
        this.visibility = (double)var2 >= 5000.0 * var1.method_48221() ? class_368.class_369.field_2209 : class_368.class_369.field_2210;
    }

    public void method_1986(class_332 var1, class_327 var2, long var3) {
        var1.method_52706(class_10799.field_56883, TEXTURE, 0, 0, this.method_29049(), this.method_29050());
        var1.method_51439(var2, TITLE, 30, 7, -43521, false);
        var1.method_51439(var2, this.coordinates, 30, 18, -1, false);
        var1.method_51445(ICON, 8, 8);
    }
}

