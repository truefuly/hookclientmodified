/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongCollection
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  net.minecraft.class_1923
 */
package hook.utils.chunks;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import net.minecraft.class_1923;

public final class ChunkRegionBuilder {
    private ChunkRegionBuilder() {
    }

    public static LongOpenHashSet build(long[] var0, int var1) {
        LongOpenHashSet var2 = new LongOpenHashSet();
        int var3 = var0.length;
        int var4 = Math.max(2, var1 * 2);
        int var5 = var4 + 1;
        boolean[] var6 = new boolean[var3];
        Long2ObjectOpenHashMap var7 = new Long2ObjectOpenHashMap();
        for (int var8 = 0; var8 < var3; ++var8) {
            long var9 = class_1923.method_8331((int)Math.floorDiv(class_1923.method_8325((long)var0[var8]), var5), (int)Math.floorDiv(class_1923.method_8332((long)var0[var8]), var5));
            ((IntArrayList)var7.computeIfAbsent(var9, var0x -> new IntArrayList())).add(var8);
        }
        IntArrayFIFOQueue var22 = new IntArrayFIFOQueue();
        IntArrayList var23 = new IntArrayList();
        IntArrayList var10 = new IntArrayList();
        for (int var11 = 0; var11 < var3; ++var11) {
            if (var6[var11]) continue;
            var23.clear();
            var22.enqueue(var11);
            var6[var11] = true;
            while (!var22.isEmpty()) {
                int var12 = var22.dequeueInt();
                var23.add(var12);
                int var13 = class_1923.method_8325((long)var0[var12]);
                int var14 = class_1923.method_8332((long)var0[var12]);
                int var15 = Math.floorDiv(var13, var5);
                int var16 = Math.floorDiv(var14, var5);
                var10.clear();
                for (int var17 = -1; var17 <= 1; ++var17) {
                    for (int var18 = -1; var18 <= 1; ++var18) {
                        IntArrayList var19 = (IntArrayList)var7.get(class_1923.method_8331((int)(var15 + var17), (int)(var16 + var18)));
                        if (var19 == null) continue;
                        for (int var20 = 0; var20 < var19.size(); ++var20) {
                            int var21 = var19.getInt(var20);
                            if (var6[var21] || Math.max(Math.abs(var13 - class_1923.method_8325((long)var0[var21])), Math.abs(var14 - class_1923.method_8332((long)var0[var21]))) > var4) continue;
                            var10.add(var21);
                        }
                    }
                }
                var10.sort(null);
                for (int var26 = 0; var26 < var10.size(); ++var26) {
                    int var27 = var10.getInt(var26);
                    var6[var27] = true;
                    var22.enqueue(var27);
                }
            }
            long[] var24 = new long[var23.size()];
            for (int var25 = 0; var25 < var24.length; ++var25) {
                var24[var25] = var0[var23.getInt(var25)];
            }
            ChunkRegionBuilder.connected(var2, var24);
        }
        return var2;
    }

    private static void connected(LongOpenHashSet var0, long[] var1) {
        LongOpenHashSet var2 = new LongOpenHashSet(var1);
        int var3 = var1.length;
        if (var3 < 2) {
            var0.addAll((LongCollection)var2);
        } else {
            int[] var4 = new int[var3];
            int[] var5 = new int[var3];
            boolean[] var6 = new boolean[var3];
            Arrays.fill(var4, Integer.MAX_VALUE);
            Arrays.fill(var5, -1);
            var6[0] = true;
            int var7 = 0;
            for (int var8 = 1; var8 < var3; ++var8) {
                int var9 = -1;
                for (int var10 = 0; var10 < var3; ++var10) {
                    int var12;
                    if (var6[var10]) continue;
                    int var11 = class_1923.method_8325((long)var1[var7]) - class_1923.method_8325((long)var1[var10]);
                    int var13 = var11 * var11 + (var12 = class_1923.method_8332((long)var1[var7]) - class_1923.method_8332((long)var1[var10])) * var12;
                    if (var13 < var4[var10] || var13 == var4[var10] && (var5[var10] < 0 || var7 < var5[var10])) {
                        var4[var10] = var13;
                        var5[var10] = var7;
                    }
                    if (var9 >= 0 && var4[var10] >= var4[var9] && (var4[var10] != var4[var9] || var5[var10] >= var5[var9] && (var5[var10] != var5[var9] || var10 >= var9))) continue;
                    var9 = var10;
                }
                if (var9 < 0) break;
                long var16 = var1[var5[var9]];
                long var18 = var1[var9];
                ChunkRegionBuilder.rectangle(var2, Math.min(class_1923.method_8325((long)var16), class_1923.method_8325((long)var18)), Math.min(class_1923.method_8332((long)var16), class_1923.method_8332((long)var18)), Math.max(class_1923.method_8325((long)var16), class_1923.method_8325((long)var18)), Math.max(class_1923.method_8332((long)var16), class_1923.method_8332((long)var18)));
                var6[var9] = true;
                var7 = var9;
            }
            var0.addAll((LongCollection)var2);
            LongIterator var14 = var2.iterator();
            while (var14.hasNext()) {
                long var15 = var14.next();
                int var17 = class_1923.method_8325((long)var15);
                int var19 = class_1923.method_8332((long)var15);
                ChunkRegionBuilder.rectangle(var0, var17 - 1, var19 - 1, var17 + 1, var19 + 1);
            }
        }
    }

    private static void rectangle(LongOpenHashSet var0, int var1, int var2, int var3, int var4) {
        for (int var5 = var1; var5 <= var3; ++var5) {
            for (int var6 = var2; var6 <= var4; ++var6) {
                var0.add(class_1923.method_8331((int)var5, (int)var6));
            }
        }
    }
}

