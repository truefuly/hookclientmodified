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

public final class ChunkFinderToast
implements class_368 {
    private static final class_2960 TEXTURE = class_2960.method_60656((String)"toast/advancement");
    private static final long DISPLAY_TIME_MS = 5000L;
    private static final int PURPLE = -43521;
    private static final int WHITE = -1;
    private static final class_1799 ICON = new class_1799((class_1935)class_1802.field_8106);
    private final class_2561 coordinates;
    private class_368.class_369 visibility = class_368.class_369.field_2210;

    public ChunkFinderToast(int x, int z) {
        this.coordinates = class_2561.method_43470((String)("X: " + x + " Z: " + z));
    }

    public class_368.class_369 method_61988() {
        return this.visibility;
    }

    public void method_61989(class_374 manager, long time) {
        this.visibility = (double)time >= 5000.0 * manager.method_48221() ? class_368.class_369.field_2209 : class_368.class_369.field_2210;
    }

    public void method_1986(class_332 context, class_327 textRenderer, long time) {
        context.method_52706(class_10799.field_56883, TEXTURE, 0, 0, this.method_29049(), this.method_29050());
        context.method_51439(textRenderer, (class_2561)class_2561.method_43470((String)"Chunk Finder"), 30, 7, -43521, false);
        context.method_51439(textRenderer, this.coordinates, 30, 18, -1, false);
        context.method_51445(ICON, 8, 8);
    }
}

