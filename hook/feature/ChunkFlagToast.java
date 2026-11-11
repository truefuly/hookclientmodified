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
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_3414
 *  net.minecraft.class_368
 *  net.minecraft.class_368$class_369
 *  net.minecraft.class_374
 *  net.minecraft.class_9334
 */
package hook.feature;

import net.minecraft.class_10799;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_368;
import net.minecraft.class_374;
import net.minecraft.class_9334;

public final class ChunkFlagToast
implements class_368 {
    private static final class_2960 TEXTURE = class_2960.method_60656((String)"toast/advancement");
    private static final class_2960 GLOW = class_2960.method_60655((String)"hook", (String)"textures/gui/chest-glow.png");
    private static final Object TYPE_KEY = new Object();
    private static final class_3414 SOUND = class_3414.method_47908((class_2960)class_2960.method_60655((String)"hook", (String)"chunk_flag"));
    private static final class_1799 ICON = ChunkFlagToast.createIcon();
    private class_2561 coordinates;
    private class_368.class_369 visibility = class_368.class_369.field_2210;
    private boolean playSound;
    private boolean resetPending = true;
    private long resetAt;

    public ChunkFlagToast(int var1, int var2, boolean var3) {
        this.refresh(var1, var2, var3);
    }

    private static class_1799 createIcon() {
        class_1799 var0 = new class_1799((class_1935)class_1802.field_8106);
        var0.method_57379(class_9334.field_49641, true);
        return var0;
    }

    public static void show(int var0, int var1, boolean var2) {
        class_374 var3 = class_310.method_1551().method_1566();
        ChunkFlagToast var4 = (ChunkFlagToast)var3.method_1997(ChunkFlagToast.class, TYPE_KEY);
        if (var4 == null) {
            var3.method_1999((class_368)new ChunkFlagToast(var0, var1, var2));
        } else {
            var4.refresh(var0, var1, var2);
        }
    }

    private void refresh(int var1, int var2, boolean var3) {
        this.coordinates = class_2561.method_43470((String)("X: " + var1 + " Z: " + var2));
        this.playSound = var3;
        this.resetPending = true;
        this.visibility = class_368.class_369.field_2210;
    }

    public Object method_1987() {
        return TYPE_KEY;
    }

    public class_368.class_369 method_61988() {
        return this.visibility;
    }

    public class_3414 method_66530() {
        return this.playSound ? SOUND : null;
    }

    public void method_61989(class_374 var1, long var2) {
        if (this.resetPending) {
            this.resetAt = var2;
            this.resetPending = false;
        }
        this.visibility = (double)(var2 - this.resetAt) >= 5000.0 * var1.method_48221() ? class_368.class_369.field_2209 : class_368.class_369.field_2210;
    }

    public void method_1986(class_332 var1, class_327 var2, long var3) {
        var1.method_52706(class_10799.field_56883, TEXTURE, 0, 0, this.method_29049(), this.method_29050());
        float var5 = 0.76f + 0.08f * (float)Math.sin((double)var3 / 850.0);
        int var6 = Math.round(var5 * 255.0f) << 24 | 0xFFFFFF;
        var1.method_25293(class_10799.field_56883, GLOW, 0, 0, 0.0f, 0.0f, 32, 32, 64, 64, 64, 64, var6);
        var1.method_51439(var2, (class_2561)class_2561.method_43470((String)"Chunk Finder Better"), 30, 7, -43521, false);
        var1.method_51439(var2, this.coordinates, 30, 18, -1, false);
        var1.method_51445(ICON, 8, 8);
    }
}

