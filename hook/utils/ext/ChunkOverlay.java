/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12179
 *  net.minecraft.class_12180
 *  net.minecraft.class_1923
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_9848
 */
package hook.utils.ext;

import hook.utils.RenderVisibility;
import hook.utils.ext.FoglessLines;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_1923;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_9848;

@Environment(value=EnvType.CLIENT)
public final class ChunkOverlay {
    public static final double MARKER_Y = 63.0;
    private static final float EDGE_SHADE = 0.77f;
    private static final float LINE_WIDTH = 1.5f;

    private ChunkOverlay() {
    }

    public static void draw(LongSet var0, int var1, float var2) {
        if (!var0.isEmpty()) {
            class_12179 var3 = class_12179.method_75539((int)var1);
            int var4 = class_9848.method_61330((int)Math.min(255, class_9848.method_61320((int)var1) * 3 / 2), (int)class_9848.method_61321((int)var1, (float)0.77f));
            FoglessLines var5 = FoglessLines.forFrame(var2);
            LongIterator var6 = var0.iterator();
            while (var6.hasNext()) {
                double var17;
                double var15;
                int var10;
                double var13;
                long var7 = var6.next();
                int var9 = class_1923.method_8325((long)var7);
                double var11 = var9 << 4;
                if (!RenderVisibility.box(var11, 63.0, var13 = (double)((var10 = class_1923.method_8332((long)var7)) << 4), var15 = var11 + 16.0, 63.0, var17 = var13 + 16.0)) continue;
                class_12180.method_75541((class_238)new class_238(var11, 63.0, var13, var15, 63.0, var17), (class_12179)var3).method_75533();
                if (!var0.contains(class_1923.method_8331((int)var9, (int)(var10 - 1)))) {
                    ChunkOverlay.edge(var5, var11, var13, var15, var13, var4);
                }
                if (!var0.contains(class_1923.method_8331((int)var9, (int)(var10 + 1)))) {
                    ChunkOverlay.edge(var5, var11, var17, var15, var17, var4);
                }
                if (!var0.contains(class_1923.method_8331((int)(var9 - 1), (int)var10))) {
                    ChunkOverlay.edge(var5, var11, var13, var11, var17, var4);
                }
                if (var0.contains(class_1923.method_8331((int)(var9 + 1), (int)var10))) continue;
                ChunkOverlay.edge(var5, var15, var13, var15, var17, var4);
            }
        }
    }

    private static void edge(FoglessLines var0, double var1, double var3, double var5, double var7, int var9) {
        var0.line(new class_243(var1, 63.0, var3), new class_243(var5, 63.0, var7), var9, 1.5f);
    }
}

