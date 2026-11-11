/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongCollection
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.minecraft.class_1923
 *  net.minecraft.class_243
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 */
package hook.utils.chunks;

import hook.event.events.Render3DEvent;
import hook.utils.RenderUtils;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.class_1923;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_4587;

public final class ChunkPlateRenderer {
    private final ArrayList<RenderUtils.ColoredBox> boxes = new ArrayList();
    private final ArrayList<RenderUtils.ColoredLine> lines = new ArrayList();
    private final LongOpenHashSet cachedChunks = new LongOpenHashSet();
    private Color cachedFill;
    private Color cachedEdge;
    private float cachedWidth;
    private double cachedY;
    private double cachedThickness;

    public void draw(Render3DEvent var1, LongSet var2, Color var3, Color var4, float var5) {
        this.draw(var1, var2, var3, var4, var5, 63.0, 0.025);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void draw(Render3DEvent var1, LongSet var2, Color var3, Color var4, float var5, double var6, double var8) {
        class_4184 var10;
        if (!var2.isEmpty() && var3.getAlpha() != 0 && (var10 = RenderUtils.getCamera()) != null && var10.method_19332()) {
            this.prepare(var2, var3, var4, var5, var6, var8);
            class_4587 var11 = var1.matrixStack;
            var11.method_22903();
            try {
                RenderUtils.applyCameraTransform(var11, var10);
                RenderUtils.renderFilledBoxes(var11, this.boxes);
                if (!this.lines.isEmpty()) {
                    RenderUtils.renderLines(var11, this.lines);
                }
            }
            finally {
                var11.method_22909();
            }
        }
    }

    private void prepare(LongSet var1, Color var2, Color var3, float var4, double var5, double var7) {
        if (!(var2.equals(this.cachedFill) && Objects.equals(var3, this.cachedEdge) && var4 == this.cachedWidth && var5 == this.cachedY && var7 == this.cachedThickness && this.cachedChunks.equals((Object)var1))) {
            this.cachedChunks.clear();
            this.cachedChunks.addAll((LongCollection)var1);
            this.cachedFill = var2;
            this.cachedEdge = var3;
            this.cachedWidth = var4;
            this.cachedY = var5;
            this.cachedThickness = var7;
            this.boxes.clear();
            this.lines.clear();
            LongOpenHashSet var9 = new LongOpenHashSet((LongCollection)var1);
            while (!var9.isEmpty()) {
                long var10 = var9.iterator().nextLong();
                int var12 = class_1923.method_8325((long)var10);
                int var13 = class_1923.method_8332((long)var10);
                int var14 = var12;
                int var15 = var12;
                int var16 = var13;
                int var17 = var13;
                while (var9.contains(class_1923.method_8331((int)(var14 - 1), (int)var13))) {
                    --var14;
                }
                while (var9.contains(class_1923.method_8331((int)(var15 + 1), (int)var13))) {
                    ++var15;
                }
                while (ChunkPlateRenderer.hasRow((LongSet)var9, var14, var15, var16 - 1)) {
                    --var16;
                }
                while (ChunkPlateRenderer.hasRow((LongSet)var9, var14, var15, var17 + 1)) {
                    ++var17;
                }
                for (int var18 = var14; var18 <= var15; ++var18) {
                    for (int var19 = var16; var19 <= var17; ++var19) {
                        var9.remove(class_1923.method_8331((int)var18, (int)var19));
                    }
                }
                this.boxes.add(new RenderUtils.ColoredBox((double)var14 * 16.0, var5, (double)var16 * 16.0, (double)(var15 + 1) * 16.0, var5 + var7, (double)(var17 + 1) * 16.0, var2));
            }
            if (var3 != null && var3.getAlpha() != 0) {
                LongOpenHashSet var20 = new LongOpenHashSet();
                LongOpenHashSet var11 = new LongOpenHashSet();
                LongIterator var21 = var1.iterator();
                while (var21.hasNext()) {
                    long var22 = var21.next();
                    int var23 = class_1923.method_8325((long)var22);
                    int var24 = class_1923.method_8332((long)var22);
                    var20.add(var22);
                    var11.add(class_1923.method_8331((int)var24, (int)var23));
                    if (!var1.contains(class_1923.method_8331((int)var23, (int)(var24 + 1)))) {
                        var20.add(class_1923.method_8331((int)var23, (int)(var24 + 1)));
                    }
                    if (var1.contains(class_1923.method_8331((int)(var23 + 1), (int)var24))) continue;
                    var11.add(class_1923.method_8331((int)var24, (int)(var23 + 1)));
                }
                this.mergeLines(var20, false, var5 + var7 + 0.003, var3, var4);
                this.mergeLines(var11, true, var5 + var7 + 0.003, var3, var4);
            }
        }
    }

    private static boolean hasRow(LongSet var0, int var1, int var2, int var3) {
        for (int var4 = var1; var4 <= var2; ++var4) {
            if (var0.contains(class_1923.method_8331((int)var4, (int)var3))) continue;
            return false;
        }
        return true;
    }

    private void mergeLines(LongOpenHashSet var1, boolean var2, double var3, Color var5, float var6) {
        while (!var1.isEmpty()) {
            long var7 = var1.iterator().nextLong();
            int var9 = class_1923.method_8325((long)var7);
            int var10 = class_1923.method_8332((long)var7);
            int var11 = var9;
            int var12 = var9;
            while (var1.contains(class_1923.method_8331((int)(var11 - 1), (int)var10))) {
                --var11;
            }
            while (var1.contains(class_1923.method_8331((int)(var12 + 1), (int)var10))) {
                ++var12;
            }
            for (int var13 = var11; var13 <= var12; ++var13) {
                var1.remove(class_1923.method_8331((int)var13, (int)var10));
            }
            class_243 var15 = var2 ? new class_243((double)var10 * 16.0, var3, (double)var11 * 16.0) : new class_243((double)var11 * 16.0, var3, (double)var10 * 16.0);
            class_243 var14 = var2 ? new class_243((double)var10 * 16.0, var3, (double)(var12 + 1) * 16.0) : new class_243((double)(var12 + 1) * 16.0, var3, (double)var10 * 16.0);
            this.lines.add(new RenderUtils.ColoredLine(var15, var14, var5, var6));
        }
    }
}

