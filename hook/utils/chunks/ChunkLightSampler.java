/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1944
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_2680
 *  net.minecraft.class_2804
 *  net.minecraft.class_2818
 *  net.minecraft.class_2826
 *  net.minecraft.class_3562
 *  net.minecraft.class_4076
 *  net.minecraft.class_638
 */
package hook.utils.chunks;

import java.util.function.LongConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1944;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2804;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_3562;
import net.minecraft.class_4076;
import net.minecraft.class_638;

@Environment(value=EnvType.CLIENT)
public final class ChunkLightSampler {
    public static final int MIN_Y = -58;
    public static final int MAX_Y = 30;
    private static final int EDGE_LIGHT = 4;
    private static final int LOW_SECTION = -4;
    private static final int HIGH_SECTION = 1;
    private static final int FIRST_CACHED = -5;
    private static final int CACHED = 8;
    private static final class_2350[] SIDES = class_2350.values();
    private final class_2804[] layers = new class_2804[72];
    private final class_2338.class_2339 cursor = new class_2338.class_2339();
    private int chunkX;
    private int chunkZ;

    public int scan(class_638 var1, class_2818 var2, LongConsumer var3) {
        this.chunkX = var2.method_12004().field_9181;
        this.chunkZ = var2.method_12004().field_9180;
        if (!this.cacheLight(var1)) {
            return 0;
        }
        class_2826[] var4 = var2.method_12006();
        int var5 = this.chunkX << 4;
        int var6 = this.chunkZ << 4;
        int var7 = 0;
        for (int var8 = -4; var8 <= 1; ++var8) {
            class_2826 var10;
            int var9 = var2.method_31603(var8);
            if (var9 < 0 || var9 >= var4.length || var4[var9] == null || !this.nearLight(var8) || !(var10 = var4[var9]).method_19523(ChunkLightSampler::open)) continue;
            int var11 = Math.max(-58, var8 << 4);
            int var12 = Math.min(30, (var8 << 4) + 15);
            for (int var13 = var11; var13 <= var12; ++var13) {
                for (int var14 = 0; var14 < 16; ++var14) {
                    for (int var15 = 0; var15 < 16; ++var15) {
                        int var17;
                        int var16;
                        if (!ChunkLightSampler.open(var10.method_12254(var15, var13 & 0xF, var14)) || this.lightAt(var16 = var5 + var15, var13, var17 = var6 + var14) != 0 || !this.edge(var1, var16, var13, var17)) continue;
                        ++var7;
                        if (var3 == null) continue;
                        var3.accept(class_2338.method_10064((int)var16, (int)var13, (int)var17));
                    }
                }
            }
        }
        return var7;
    }

    private boolean cacheLight(class_638 var1) {
        class_3562 var2 = var1.method_22336().method_15562(class_1944.field_9282);
        boolean var3 = false;
        for (int var4 = -1; var4 <= 1; ++var4) {
            for (int var5 = -1; var5 <= 1; ++var5) {
                for (int var6 = 0; var6 < 8; ++var6) {
                    class_2804 var7 = var2.method_15544(class_4076.method_18676((int)(this.chunkX + var4), (int)(-5 + var6), (int)(this.chunkZ + var5)));
                    if (var7 != null && var7.method_12146()) {
                        var7 = null;
                    }
                    this.layers[ChunkLightSampler.slot((int)var4, (int)var5, (int)var6)] = var7;
                    var3 |= var7 != null;
                }
            }
        }
        return var3;
    }

    private boolean nearLight(int var1) {
        for (int var2 = -1; var2 <= 1; ++var2) {
            for (int var3 = -1; var3 <= 1; ++var3) {
                for (int var4 = var1 - 1; var4 <= var1 + 1; ++var4) {
                    if (this.layers[ChunkLightSampler.slot(var2, var3, var4 - -5)] == null) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean edge(class_638 var1, int var2, int var3, int var4) {
        boolean var5 = false;
        int var6 = 0;
        for (class_2350 var10 : SIDES) {
            int var13;
            int var12;
            int var11 = var2 + var10.method_10148();
            int var14 = this.lightAt(var11, var12 = var3 + var10.method_10164(), var13 = var4 + var10.method_10165());
            if (var14 > 4) {
                return false;
            }
            var6 = Math.max(var6, var14);
            if (var14 != 4 || var5 || !ChunkLightSampler.open(var1.method_8320((class_2338)this.cursor.method_10103(var11, var12, var13)))) continue;
            var5 = true;
        }
        return var6 == 4 && var5;
    }

    private int lightAt(int var1, int var2, int var3) {
        int var4 = (var2 >> 4) - -5;
        if (var4 >= 0 && var4 < 8) {
            class_2804 var5 = this.layers[ChunkLightSampler.slot((var1 >> 4) - this.chunkX, (var3 >> 4) - this.chunkZ, var4)];
            return var5 == null ? 0 : var5.method_12139(var1 & 0xF, var2 & 0xF, var3 & 0xF);
        }
        return 0;
    }

    private static int slot(int var0, int var1, int var2) {
        return ((var0 + 1) * 3 + var1 + 1) * 8 + var2;
    }

    private static boolean open(class_2680 var0) {
        return var0.method_26215() || var0.method_27852(class_2246.field_27161);
    }
}

