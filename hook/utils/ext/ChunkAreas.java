/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleArrayList
 *  it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1923
 *  net.minecraft.class_243
 */
package hook.utils.ext;

import hook.utils.ext.TracerEngine;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1923;
import net.minecraft.class_243;

@Environment(value=EnvType.CLIENT)
public final class ChunkAreas {
    private final DoubleArrayList areaX = new DoubleArrayList();
    private final DoubleArrayList areaZ = new DoubleArrayList();
    private int areasSize = -1;
    private int areasHash;
    private final LongOpenHashSet seen = new LongOpenHashSet();
    private final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
    private final LongArrayList area = new LongArrayList();

    public void drawTracers(LongSet var1, double var2, int var4) {
        if (!var1.isEmpty()) {
            this.find(var1);
            for (int var5 = 0; var5 < this.areaX.size(); ++var5) {
                TracerEngine.draw(new class_243(this.areaX.getDouble(var5), var2, this.areaZ.getDouble(var5)), var4);
            }
        }
    }

    private void find(LongSet var1) {
        int var2 = var1.hashCode();
        if (var1.size() != this.areasSize || var2 != this.areasHash) {
            this.areasSize = var1.size();
            this.areasHash = var2;
            this.areaX.clear();
            this.areaZ.clear();
            this.seen.clear();
            LongIterator var3 = var1.iterator();
            while (var3.hasNext()) {
                long var4 = var3.next();
                if (!this.seen.add(var4)) continue;
                this.area.clear();
                this.queue.enqueue(var4);
                double var6 = 0.0;
                double var8 = 0.0;
                while (!this.queue.isEmpty()) {
                    long var10 = this.queue.dequeueLong();
                    this.area.add(var10);
                    int var12 = class_1923.method_8325((long)var10);
                    int var13 = class_1923.method_8332((long)var10);
                    var6 += (double)(var12 << 4) + 8.0;
                    var8 += (double)(var13 << 4) + 8.0;
                    for (int var14 = -1; var14 <= 1; ++var14) {
                        for (int var15 = -1; var15 <= 1; ++var15) {
                            long var16 = class_1923.method_8331((int)(var12 + var14), (int)(var13 + var15));
                            if (!var1.contains(var16) || !this.seen.add(var16)) continue;
                            this.queue.enqueue(var16);
                        }
                    }
                }
                double var29 = var6 / (double)this.area.size();
                double var30 = var8 / (double)this.area.size();
                if (!var1.contains(class_1923.method_8331((int)((int)Math.floor(var29) >> 4), (int)((int)Math.floor(var30) >> 4)))) {
                    double var31 = Double.MAX_VALUE;
                    double var32 = var29;
                    double var18 = var30;
                    for (int var20 = 0; var20 < this.area.size(); ++var20) {
                        double var25;
                        long var21 = this.area.getLong(var20);
                        double var23 = (double)(class_1923.method_8325((long)var21) << 4) + 8.0;
                        double var27 = (var23 - var29) * (var23 - var29) + ((var25 = (double)(class_1923.method_8332((long)var21) << 4) + 8.0) - var30) * (var25 - var30);
                        if (!(var27 < var31)) continue;
                        var31 = var27;
                        var32 = var23;
                        var18 = var25;
                    }
                    var29 = var32;
                    var30 = var18;
                }
                this.areaX.add(var29);
                this.areaZ.add(var30);
            }
        }
    }
}

