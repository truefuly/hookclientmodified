/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.BakedCharacterAsset;
import hook.feature.BakedCharacterData;
import hook.feature.BakedCharacterLoader;
import hook.feature.BakedCharacterPart;
import hook.feature.BoneMotionProfile;
import hook.feature.CharacterBone;
import hook.feature.CharacterDefinition;
import hook.feature.CharacterDetailLevel;
import hook.feature.CharacterPartBuilder;
import hook.feature.CharacterRig;
import hook.feature.CharacterTextureAtlasPacker;
import hook.feature.MeshBufferBuilder;
import hook.feature.MeshData;
import hook.feature.MeshSimplifier;
import hook.feature.PixelCanvas;
import hook.feature.Sdf3D;
import hook.feature.SdfMeshBaker;
import hook.feature.SdfShapes;
import hook.feature.SurfaceDecal;
import hook.feature.SurfaceMaterialSample;
import hook.feature.TaperedTubeSdf3D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class CharacterMeshBaker {
    private static final float DECAL_RESERVE = 0.1f;
    private static final float AO_STEP = 0.38f;
    private static final int MIN_TRIANGLES = 24;
    private static final float MAX_EXTENT = 160.0f;
    private static final float OUTLINE_SHARE = 0.5f;
    private static final float BURIED = 0.02f;
    private static final float SINK = 0.12f;
    private static final int CORNER = 10;

    private CharacterMeshBaker() {
    }

    public static BakedCharacterData build(CharacterDefinition var0, CharacterDetailLevel var1) {
        if (var0 instanceof BakedCharacterAsset) {
            BakedCharacterAsset var2 = (BakedCharacterAsset)var0;
            return BakedCharacterLoader.load(var2, var1);
        }
        long var3 = System.nanoTime();
        CharacterRig var5 = var0.rig();
        List<CharacterPartBuilder> var6 = CharacterMeshBaker.order(var5);
        int var7 = var6.size();
        if (var7 == 0) {
            throw new IllegalStateException(var0.id() + ": the design defines no parts");
        }
        IdentityHashMap<CharacterPartBuilder, Integer> var8 = new IdentityHashMap<CharacterPartBuilder, Integer>();
        for (int var9 = 0; var9 < var7; ++var9) {
            var8.put(var6.get(var9), var9);
        }
        ArrayList<Object> var34 = new ArrayList<Object>();
        ArrayList<PixelCanvas> var10 = new ArrayList<PixelCanvas>();
        Sdf3D[] var11 = new Sdf3D[var7];
        float[] var12 = new float[var7];
        for (int var13 = 0; var13 < var7; ++var13) {
            CharacterPartBuilder var14 = var6.get(var13);
            if (var14.shape != null) {
                Sdf3D var15 = (Sdf3D)var14.shape;
                if (!((Sdf3D)var15).bounded()) {
                    throw new IllegalStateException(var14.name + ": shape is unbounded; intersect planes with a box");
                }
                if (((Sdf3D)var15).maxX - ((Sdf3D)var15).minX > 160.0f || ((Sdf3D)var15).maxY - ((Sdf3D)var15).minY > 160.0f || ((Sdf3D)var15).maxZ - ((Sdf3D)var15).minZ > 160.0f) {
                    throw new IllegalStateException(var14.name + ": shape is larger than 160.0 units");
                }
                var12[var13] = SdfMeshBaker.fitStep(((Sdf3D)var15).maxX - ((Sdf3D)var15).minX, ((Sdf3D)var15).maxY - ((Sdf3D)var15).minY, ((Sdf3D)var15).maxZ - ((Sdf3D)var15).minZ, var1.step * var14.resolution);
                float var16 = var12[var13] * 0.75f;
                var11[var13] = var15 = ((Sdf3D)var15).map(var1x -> {
                    Sdf3D sdf3D;
                    if (var1x instanceof TaperedTubeSdf3D) {
                        TaperedTubeSdf3D var2x = (TaperedTubeSdf3D)var1x;
                        sdf3D = var2x.thickened(var16);
                    } else {
                        sdf3D = var1x;
                    }
                    return sdf3D;
                });
                var34.add(var15);
            }
            for (SurfaceDecal var40 : var14.decals) {
                var10.add(var40.canvas);
            }
        }
        Sdf3D var35 = var34.isEmpty() ? null : SdfShapes.union(var34.toArray(new Sdf3D[0]));
        Sdf3D[] var36 = CharacterMeshBaker.covers(var6, var11);
        CharacterTextureAtlasPacker var39 = CharacterTextureAtlasPacker.pack(var10);
        float[] var41 = new float[var7];
        for (int var17 = 0; var17 < var7; ++var17) {
            CharacterPartBuilder var18 = var6.get(var17);
            var41[var17] = var18.outline >= 0.0f ? var18.outline : var5.outline;
        }
        MeshData[] var42 = new MeshData[var7];
        float[] var43 = new float[var7];
        IntStream.range(0, var7).parallel().forEach(var5x -> {
            Sdf3D var6x = var11[var5x];
            if (var6x != null) {
                var42[var5x] = SdfMeshBaker.mesh(var6x, var6x.minX, var6x.minY, var6x.minZ, var6x.maxX, var6x.maxY, var6x.maxZ, var12[var5x]);
                var43[var5x] = CharacterMeshBaker.visible(var42[var5x], var36[var5x]);
            }
        });
        int[] var19 = new int[var7];
        CharacterMeshBaker.budget(var6, var42, var43, var1, var19, var41);
        MeshData[] var20 = new MeshData[var7];
        IntStream.range(0, var7).parallel().forEach(var3x -> {
            if (var42[var3x] != null && var42[var3x].triCount != 0) {
                MeshSimplifier var4 = new MeshSimplifier(var42[var3x]);
                var4.reduce(var19[var3x]);
                var20[var3x] = var4.snapshot();
            }
        });
        BakedCharacterPart[] var21 = new BakedCharacterPart[var7];
        float[] var10000 = new float[]{Float.MAX_VALUE, -3.4028235E38f};
        float[] var22 = var10000;
        int[] var23 = new int[var7];
        int var24 = 0;
        for (int var25 = 0; var25 < var7; ++var25) {
            CharacterPartBuilder var26 = var6.get(var25);
            var23[var25] = var26.extra && var26.motion != null ? var24++ : -1;
        }
        IntStream.range(0, var7).parallel().forEach(var13x -> {
            boolean var31x;
            BoneMotionProfile var27x;
            CharacterPartBuilder var14x = (CharacterPartBuilder)var6.get(var13x);
            MeshBufferBuilder var15x = null;
            MeshBufferBuilder var16x = null;
            MeshBufferBuilder var17x = null;
            if (var20[var13x] != null && var20[var13x].triCount > 0) {
                MeshData var21x;
                float var19x;
                float var18x = var14x.occlusion >= 0.0f ? var14x.occlusion : var5.occlusion;
                MeshArrays var20x = CharacterMeshBaker.shade(var11[var13x], var35, var20[var13x], var12[var13x], var18x, var19x = var14x.flat >= 0.0f ? var14x.flat : var5.flat);
                var15x = CharacterMeshBaker.baseSurface(var20x, var21x = CharacterMeshBaker.bury(var20x, var20[var13x], var36[var13x], var12[var13x]), var14x, var39);
                if (var15x != null && var41[var13x] > 0.0f) {
                    int var22x = var14x.outlineColorSet ? var14x.outlineColor : var5.outlineColor;
                    var16x = CharacterMeshBaker.hullSurface(var11[var13x], var20x, var15x, var14x, var41[var13x], var12[var13x], var22x, var39);
                }
                if (!var14x.decals.isEmpty()) {
                    var17x = CharacterMeshBaker.decalSurface(var14x, var20x, var21x, var39);
                }
                float[] var22x = var22;
                // MONITORENTER : var22
                for (int var23x = 0; var23x < var20x.count; ++var23x) {
                    var22[0] = Math.min(var22[0], var20x.pos[var23x * 3 + 1]);
                    var22[1] = Math.max(var22[1], var20x.pos[var23x * 3 + 1]);
                }
                // MONITOREXIT : var22x
            }
            int var26x = var14x.parent == null ? -1 : (Integer)var8.get(var14x.parent);
            BoneMotionProfile boneMotionProfile = var27x = var14x.extra ? var14x.motion : null;
            if (var27x != null && var27x.bendPower() > 0.0f) {
                float var28x = CharacterMeshBaker.reach(var15x);
                CharacterMeshBaker.weigh(var15x, var28x, var27x.bendPower());
                if (var16x != null && var15x != null) {
                    var16x.weight = var15x.weight;
                }
                CharacterMeshBaker.weigh(var17x, var28x, var27x.bendPower());
            }
            boolean var29x = !var14x.extra && (var14x.bone.arm() || var14x.bone.leg());
            CharacterPartBuilder var30x = var14x;
            while (var30x != null && !var30x.jointSet) {
                var30x = var30x.parent;
            }
            boolean bl = var31x = var30x != null;
            if (var31x) {
                CharacterMeshBaker.jointWeights(var15x, var14x, var30x);
                CharacterMeshBaker.jointWeights(var17x, var14x, var30x);
                if (var16x != null && var15x != null) {
                    var16x.joint = var15x.joint;
                }
            }
            var21[var13x] = new BakedCharacterPart(var14x.name, var14x.bone, var26x, var14x.extra, var14x.pivotX, var14x.pivotY, var14x.pivotZ, var14x.gripX(), var14x.gripY(), var14x.gripZ(), var27x, var15x, var16x, var17x, var23[var13x], var29x ? var14x.swing : 1.0f, var29x ? var14x.swingLimit : 0.0f, var31x, var31x ? var30x.jointX - var14x.pivotX : 0.0f, var31x ? var30x.jointY - var14x.pivotY : 0.0f, var31x ? var30x.jointZ - var14x.pivotZ : 0.0f, var31x && var30x.neck, var31x ? var30x.lookPitch : 0.0f, var31x ? var30x.lookYaw : 0.0f, var31x ? var30x.lookLimit : 0.0f);
        });
        long var44 = System.nanoTime();
        float var27 = 12.0f;
        CharacterPartBuilder var28 = var5.bones[CharacterBone.RIGHT_LEG.ordinal()];
        CharacterPartBuilder var29 = var5.bones[CharacterBone.LEFT_LEG.ordinal()];
        if (var28 != null && var29 != null) {
            var27 = (var28.pivotY + var29.pivotY) * 0.5f;
        } else if (var28 != null) {
            var27 = var28.pivotY;
        } else if (var29 != null) {
            var27 = var29.pivotY;
        }
        float var30 = var22[0] == Float.MAX_VALUE ? 0.0f : var22[0];
        float var31 = var22[1] == -3.4028235E38f ? 32.0f : var22[1];
        long var32 = (var44 - var3) / 1000000L;
        return new BakedCharacterData(var0.id(), var0.name(), var1, var5.scale, var21, var24, var39.width, var39.height, var39.mips, var39.whiteU(), var39.whiteV(), var30, var31, var27, var5.firstPersonArms, var32);
    }

    private static List<CharacterPartBuilder> order(CharacterRig var0) {
        ArrayList<CharacterPartBuilder> var1 = new ArrayList<CharacterPartBuilder>();
        for (CharacterPartBuilder var5 : var0.bones) {
            if (var5 == null) continue;
            var1.add(var5);
        }
        var1.addAll(var0.extras);
        return var1;
    }

    private static Sdf3D[] covers(List<CharacterPartBuilder> var0, Sdf3D[] var1) {
        int var2 = var0.size();
        CharacterPartBuilder[] var3 = new CharacterPartBuilder[var2];
        for (int var4 = 0; var4 < var2; ++var4) {
            CharacterPartBuilder var5 = var0.get(var4);
            while (var5.extra && var5.motion == null && var5.parent != null) {
                var5 = var5.parent;
            }
            var3[var4] = var5;
        }
        Sdf3D[] var8 = new Sdf3D[var2];
        for (int var9 = 0; var9 < var2; ++var9) {
            if (var1[var9] == null) continue;
            ArrayList<Sdf3D> var6 = new ArrayList<Sdf3D>();
            for (int var7 = 0; var7 < var2; ++var7) {
                if (var7 == var9 || var1[var7] == null || var3[var7] != var3[var9] || !CharacterMeshBaker.overlaps(var1[var9], var1[var7])) continue;
                var6.add(var1[var7]);
            }
            if (var6.isEmpty()) continue;
            var8[var9] = SdfShapes.union(var6.toArray(new Sdf3D[0]));
        }
        return var8;
    }

    private static boolean overlaps(Sdf3D var0, Sdf3D var1) {
        return var0.minX <= var1.maxX && var1.minX <= var0.maxX && var0.minY <= var1.maxY && var1.minY <= var0.maxY && var0.minZ <= var1.maxZ && var1.minZ <= var0.maxZ;
    }

    private static float visible(MeshData var0, Sdf3D var1) {
        if (var1 != null && var0.triCount != 0) {
            boolean[] var2 = new boolean[var0.vertexCount];
            for (int var3 = 0; var3 < var0.vertexCount; ++var3) {
                var2[var3] = var1.distance(var0.pos[var3 * 3], var0.pos[var3 * 3 + 1], var0.pos[var3 * 3 + 2]) < -0.02f;
            }
            int var5 = 0;
            for (int var4 = 0; var4 < var0.triCount; ++var4) {
                if (!var2[var0.tris[var4 * 3]] || !var2[var0.tris[var4 * 3 + 1]] || !var2[var0.tris[var4 * 3 + 2]]) continue;
                ++var5;
            }
            return Math.max(0.05f, 1.0f - (float)var5 / (float)var0.triCount);
        }
        return 1.0f;
    }

    private static void budget(List<CharacterPartBuilder> var0, MeshData[] var1, float[] var2, CharacterDetailLevel var3, int[] var4, float[] var5) {
        int var6 = var0.size();
        float var7 = 0.0f;
        float var8 = 0.0f;
        float[] var9 = new float[var6];
        for (int var10 = 0; var10 < var6; ++var10) {
            CharacterPartBuilder var11 = var0.get(var10);
            if (var1[var10] == null) continue;
            var9[var10] = (float)var1[var10].triCount * var2[var10] * var11.detail;
            var7 += var9[var10];
            if (!(var5[var10] > 0.0f)) continue;
            var8 += var9[var10];
        }
        if (!(var7 <= 0.0f)) {
            float var14 = (float)var3.budget * 0.9f;
            float var15 = var14 / (1.0f + var8 / var7);
            for (int var12 = 0; var12 < var6; ++var12) {
                if (var1[var12] == null) continue;
                int var13 = Math.max(24, Math.round(var15 * var9[var12] / var7 / var2[var12]));
                var4[var12] = Math.min(var13, var1[var12].triCount);
            }
        }
    }

    private static MeshArrays shade(Sdf3D var0, Sdf3D var1, MeshData var2, float var3, float var4, float var5) {
        MeshArrays var6 = new MeshArrays(var2.vertexCount);
        int var7 = (var2.vertexCount + 255) / 256;
        IntStream.range(0, var7).parallel().forEach(var7x -> {
            float[] var8 = new float[3];
            float[] var9 = new float[3];
            SurfaceMaterialSample var10 = new SurfaceMaterialSample();
            int var11 = Math.min(var2.vertexCount, (var7x + 1) * 256);
            for (int var12 = var7x * 256; var12 < var11; ++var12) {
                var8[0] = var2.pos[var12 * 3];
                var8[1] = var2.pos[var12 * 3 + 1];
                var8[2] = var2.pos[var12 * 3 + 2];
                CharacterMeshBaker.project(var0, var8, var9, var3);
                System.arraycopy(var8, 0, var6.pos, var12 * 3, 3);
                System.arraycopy(var9, 0, var6.nrm, var12 * 3, 3);
                var0.sample(var8[0], var8[1], var8[2], var10);
                float var13 = CharacterMeshBaker.occlusion(var1, var8[0], var8[1], var8[2], var9[0], var9[1], var9[2], var10.occlusion(var4));
                var13 += (1.0f - var13) * Math.min(1.0f, var10.glow);
                var6.ao[var12] = var13;
                var6.flat[var12] = var10.flat(var5);
                var6.glow[var12] = (byte)(var10.glow >= 0.5f ? 1 : 0);
                var6.color[var12] = 0xFF000000 | SurfaceMaterialSample.channel(var10.r * var13) << 16 | SurfaceMaterialSample.channel(var10.g * var13) << 8 | SurfaceMaterialSample.channel(var10.b * var13);
            }
        });
        return var6;
    }

    private static MeshData bury(MeshArrays var0, MeshData var1, Sdf3D var2, float var3) {
        if (var2 == null) {
            return var1;
        }
        float[] var4 = new float[var0.count];
        int var5 = (var0.count + 255) / 256;
        IntStream.range(0, var5).parallel().forEach(var4x -> {
            float[] var5x = new float[3];
            float[] var6x = new float[3];
            int var7x = Math.min(var0.count, (var4x + 1) * 256);
            for (int var8x = var4x * 256; var8x < var7x; ++var8x) {
                float var9x;
                var5x[0] = var0.pos[var8x * 3];
                var5x[1] = var0.pos[var8x * 3 + 1];
                var5x[2] = var0.pos[var8x * 3 + 2];
                var4[var8x] = var9x = var2.distance(var5x[0], var5x[1], var5x[2]);
                if (!(var9x < 0.0f)) continue;
                CharacterMeshBaker.gradient(var2, var5x, Math.max(0.01f, var3 * 0.3f), var6x);
                float var10x = Math.min(0.12f, -2.0f * var9x);
                var0.pos[var8x * 3] = var5x[0] - var6x[0] * var10x;
                var0.pos[var8x * 3 + 1] = var5x[1] - var6x[1] * var10x;
                var0.pos[var8x * 3 + 2] = var5x[2] - var6x[2] * var10x;
            }
        });
        var0.cover = var4;
        int[] var6 = new int[var1.triCount * 3];
        int var7 = 0;
        for (int var8 = 0; var8 < var1.triCount; ++var8) {
            float var14;
            float var13;
            float var12;
            int var9 = var1.tris[var8 * 3];
            int var10 = var1.tris[var8 * 3 + 1];
            int var11 = var1.tris[var8 * 3 + 2];
            if (var4[var9] < -0.02f && var4[var10] < -0.02f && var4[var11] < -0.02f && var2.distance(var12 = (var1.pos[var9 * 3] + var1.pos[var10 * 3] + var1.pos[var11 * 3]) / 3.0f, var13 = (var1.pos[var9 * 3 + 1] + var1.pos[var10 * 3 + 1] + var1.pos[var11 * 3 + 1]) / 3.0f, var14 = (var1.pos[var9 * 3 + 2] + var1.pos[var10 * 3 + 2] + var1.pos[var11 * 3 + 2]) / 3.0f) < -0.02f) continue;
            var6[var7 * 3] = var9;
            var6[var7 * 3 + 1] = var10;
            var6[var7 * 3 + 2] = var11;
            ++var7;
        }
        return var7 == var1.triCount ? var1 : new MeshData(var1.pos, var1.vertexCount, Arrays.copyOf(var6, var7 * 3), var7);
    }

    static void project(Sdf3D var0, float[] var1, float[] var2, float var3) {
        CharacterMeshBaker.project(var0, var1, var2, var3, 0.0f);
    }

    static void project(Sdf3D var0, float[] var1, float[] var2, float var3, float var4) {
        float var110;
        float var15;
        float var36 = var3 * 0.3f;
        float var10 = var15 = Math.max(0.01f, var36);
        float var11 = var3 * 0.01f;
        float var39 = var1[0];
        float var55 = var1[1];
        float var69 = var1[2];
        float var12 = var0.distance(var39, var55, var69) - var4;
        CharacterMeshBaker.gradient(var0, var1, var15, var2);
        for (int var13 = 0; var13 < 2 && ((var110 = Math.abs(var12) - var11) == 0.0f ? 0 : (var110 < 0.0f ? -1 : 1)) > 0; ++var13) {
            float var63;
            float var61;
            float var59;
            float var45 = Math.min(var3, var12);
            float var28 = Math.max(var3, var45);
            float var58 = var1[0];
            float var73 = var2[0] * var28;
            var1[0] = var59 = var58 - var73;
            float var60 = var1[1];
            float var76 = var2[1] * var28;
            var1[1] = var61 = var60 - var76;
            float var62 = var1[2];
            float var79 = var2[2] * var28;
            var1[2] = var63 = var62 - var79;
            float var50 = var1[0];
            float var66 = var1[1];
            float var82 = var1[2];
            var12 = var0.distance(var50, var66, var82) - var4;
            CharacterMeshBaker.gradient(var0, var1, var10, var2);
        }
    }

    static void gradient(Sdf3D var0, float[] var1, float var2, float[] var3) {
        float var107;
        float var59;
        float var98;
        float var29;
        float var30;
        float var64;
        float var96;
        float var56;
        float var76;
        float var26;
        float var27;
        float var62;
        float var74;
        float var53;
        float var17 = var1[0];
        float var51 = var17 + var2;
        float var19 = var1[1];
        float var21 = var1[2];
        float var23 = var0.distance(var51, var19, var21);
        float var24 = var23 - (var53 = var0.distance(var74 = var17 - var2, var19, var21));
        float var32 = var24 * var24;
        float var33 = var32 + (var62 = (var27 = (var26 = var0.distance(var17, var76 = var19 + var2, var21)) - (var56 = var0.distance(var17, var96 = var19 - var2, var21))) * var27);
        float var26x = (float)Math.sqrt(var33 + (var64 = (var30 = (var29 = var0.distance(var17, var19, var98 = var21 + var2)) - (var59 = var0.distance(var17, var19, var107 = var21 - var2))) * var30));
        float var127 = var26x - 1.0E-12f;
        if ((var127 == 0.0f ? 0 : (var127 < 0.0f ? -1 : 1)) < 0) {
            var3[0] = 0.0f;
            var3[1] = 1.0f;
            var3[2] = 0.0f;
        } else {
            float var87;
            float var85;
            float var83;
            var3[0] = var83 = var24 / var26x;
            var3[1] = var85 = var27 / var26x;
            var3[2] = var87 = var30 / var26x;
        }
    }

    private static float occlusion(Sdf3D var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
        if (var0 != null && !(var7 <= 0.0f)) {
            float var8 = 0.0f;
            float var9 = 0.0f;
            float var10 = 1.0f;
            for (int var11 = 1; var11 <= 5; ++var11) {
                float var12 = 0.38f * (float)var11;
                float var13 = var0.distance(var1 + var4 * var12, var2 + var5 * var12, var3 + var6 * var12);
                var8 += Math.max(0.0f, var12 - var13) * var10;
                var9 += var12 * var10;
                var10 *= 0.72f;
            }
            float var14 = Math.min(1.0f, var8 / var9 * 1.6f);
            return 1.0f - var7 * var14 * (1.4f - 0.4f * var14);
        }
        return 1.0f;
    }

    private static MeshBufferBuilder baseSurface(MeshArrays var0, MeshData var1, CharacterPartBuilder var2, CharacterTextureAtlasPacker var3) {
        if (var1.triCount == 0) {
            return null;
        }
        MeshBufferBuilder.MeshArrays var4 = new MeshBufferBuilder.MeshArrays();
        float var5 = var3.whiteU();
        float var6 = var3.whiteV();
        for (int var7 = 0; var7 < var0.count; ++var7) {
            var4.vertex(var0.pos[var7 * 3] - var2.pivotX, var0.pos[var7 * 3 + 1] - var2.pivotY, var0.pos[var7 * 3 + 2] - var2.pivotZ, var0.nrm[var7 * 3], var0.nrm[var7 * 3 + 1], var0.nrm[var7 * 3 + 2], var5, var6, var0.color[var7], var0.glow[var7] != 0, var0.flat[var7]);
        }
        CharacterMeshBaker.pair(var1.tris, var1.triCount, var1.vertexCount, var4);
        return var4.build();
    }

    private static MeshBufferBuilder hullSurface(Sdf3D var0, MeshArrays var1, MeshBufferBuilder var2, CharacterPartBuilder var3, float var4, float var5, int var6, CharacterTextureAtlasPacker var7) {
        float[] var8 = new float[var1.count * 3];
        float[] var9 = new float[var1.count];
        float var10 = var4 / 0.5f * 1.5f;
        int var11 = (var1.count + 255) / 256;
        IntStream.range(0, var11).parallel().forEach(var7x -> {
            float[] var8x = new float[3];
            float[] var9x = new float[3];
            int var10x = Math.min(var1.count, (var7x + 1) * 256);
            for (int var11x = var7x * 256; var11x < var10x; ++var11x) {
                float var18x;
                float var12x = var1.pos[var11x * 3];
                float var13x = var1.pos[var11x * 3 + 1];
                float var14x = var1.pos[var11x * 3 + 2];
                float var15x = var1.nrm[var11x * 3];
                float var16x = var1.nrm[var11x * 3 + 1];
                float var17x = var1.nrm[var11x * 3 + 2];
                float f = var18x = var1.cover != null && var1.cover[var11x] < 0.0f ? 0.0f : var4;
                if (var18x > 0.0f) {
                    float var19x = 0.0f;
                    for (int var20x = 1; var20x <= 5; ++var20x) {
                        float var21x = var10 * (float)var20x / 5.0f;
                        var19x = Math.max(var19x, -var0.distance(var12x - var15x * var21x, var13x - var16x * var21x, var14x - var17x * var21x));
                    }
                    var18x = Math.min(var18x, 0.5f * var19x);
                }
                var8x[0] = var12x + var15x * var18x;
                var8x[1] = var13x + var16x * var18x;
                var8x[2] = var14x + var17x * var18x;
                if (var18x > 0.0f) {
                    CharacterMeshBaker.project(var0, var8x, var9x, var5, var18x);
                }
                var8[var11x * 3] = var8x[0];
                var8[var11x * 3 + 1] = var8x[1];
                var8[var11x * 3 + 2] = var8x[2];
                var9[var11x] = var18x;
            }
        });
        MeshBufferBuilder.MeshArrays var12 = new MeshBufferBuilder.MeshArrays();
        float var13 = var7.whiteU();
        float var14 = var7.whiteV();
        for (int var15 = 0; var15 < var1.count; ++var15) {
            var12.vertex(var8[var15 * 3] - var3.pivotX, var8[var15 * 3 + 1] - var3.pivotY, var8[var15 * 3 + 2] - var3.pivotZ, 0.0f, 1.0f, 0.0f, var13, var14, var6 | 0xFF000000, false);
        }
        int[] var21 = var2.quads;
        for (int var16 = 0; var16 < var21.length; var16 += 4) {
            int var17 = var21[var16];
            int var18 = var21[var16 + 1];
            int var19 = var21[var16 + 2];
            int var20 = var21[var16 + 3];
            if (var9[var17] <= 0.0f && var9[var18] <= 0.0f && var9[var19] <= 0.0f && var9[var20] <= 0.0f) continue;
            var12.quad(var17, var20, var19, var18, var19 == var20 ? 1 : 2);
        }
        return var12.build();
    }

    static void pair(int[] var0, int var1, int var2, MeshBufferBuilder.MeshArrays var3) {
        long[] var4 = new long[var1 * 3];
        for (int var5 = 0; var5 < var1; ++var5) {
            for (int var6 = 0; var6 < 3; ++var6) {
                int var7 = var0[var5 * 3 + var6];
                int var8 = var0[var5 * 3 + (var6 + 1) % 3];
                long var9 = (long)Math.min(var7, var8) * (long)var2 + (long)Math.max(var7, var8);
                var4[var5 * 3 + var6] = var9 << 21 | (long)var5 * 3L + (long)var6;
            }
        }
        Arrays.sort(var4);
        int[] var20 = new int[var1 * 3];
        Arrays.fill(var20, -1);
        long var21 = 0x1FFFFFL;
        int var22 = 0;
        while (var22 + 1 < var4.length) {
            if (var4[var22] >>> 21 == var4[var22 + 1] >>> 21) {
                if (var22 + 2 < var4.length && var4[var22 + 2] >>> 21 == var4[var22] >>> 21) {
                    while (var22 + 1 < var4.length && var4[var22 + 1] >>> 21 == var4[var22] >>> 21) {
                        ++var22;
                    }
                } else {
                    int var10;
                    int var24 = (int)(var4[var22] & var21);
                    var20[var24] = var10 = (int)(var4[var22 + 1] & var21);
                    var20[var10] = var24;
                    ++var22;
                }
            }
            ++var22;
        }
        boolean[] var23 = new boolean[var1];
        for (int var25 = 0; var25 < var1; ++var25) {
            if (var23[var25]) continue;
            int var26 = -1;
            for (int var11 = 0; var11 < 3 && var26 < 0; ++var11) {
                int var12 = var20[var25 * 3 + var11];
                if (var12 < 0 || var23[var12 / 3] || var12 / 3 == var25) continue;
                var26 = var11;
            }
            int var27 = var0[var25 * 3];
            int var28 = var0[var25 * 3 + 1];
            int var13 = var0[var25 * 3 + 2];
            if (var26 < 0) {
                var23[var25] = true;
                var3.quad(var27, var28, var13, var13, 1);
                continue;
            }
            int var14 = var20[var25 * 3 + var26];
            int var15 = var14 / 3;
            var23[var25] = true;
            var23[var15] = true;
            int var16 = var0[var25 * 3 + var26];
            int var17 = var0[var25 * 3 + (var26 + 1) % 3];
            int var18 = var0[var25 * 3 + (var26 + 2) % 3];
            int var19 = var0[var15 * 3 + (var14 % 3 + 2) % 3];
            var3.quad(var16, var19, var17, var18, 2);
        }
    }

    private static MeshBufferBuilder decalSurface(CharacterPartBuilder var0, MeshArrays var1, MeshData var2, CharacterTextureAtlasPacker var3) {
        MeshBufferBuilder.MeshArrays var4 = new MeshBufferBuilder.MeshArrays();
        float[] var5 = new float[200];
        float[] var6 = new float[200];
        for (SurfaceDecal var8 : var0.decals) {
            for (int var9 = 0; var9 < var2.triCount; ++var9) {
                int var10 = var2.tris[var9 * 3];
                int var11 = var2.tris[var9 * 3 + 1];
                int var12 = var2.tris[var9 * 3 + 2];
                if (!CharacterMeshBaker.faces(var8, var1, var10, var11, var12)) continue;
                int[] var10000 = new int[]{var10, var11, var12};
                int[] var13 = var10000;
                float var14 = 0.0f;
                for (int var15 = 0; var15 < 3; ++var15) {
                    int var16 = var13[var15];
                    int var17 = var15 * 10;
                    var5[var17] = var1.pos[var16 * 3];
                    var5[var17 + 1] = var1.pos[var16 * 3 + 1];
                    var5[var17 + 2] = var1.pos[var16 * 3 + 2];
                    var5[var17 + 3] = var1.nrm[var16 * 3];
                    var5[var17 + 4] = var1.nrm[var16 * 3 + 1];
                    var5[var17 + 5] = var1.nrm[var16 * 3 + 2];
                    var5[var17 + 8] = var1.ao[var16];
                    var5[var17 + 9] = var1.flat[var16];
                    float var18 = var5[var17] - var8.cx;
                    float var19 = var5[var17 + 1] - var8.cy;
                    float var20 = var5[var17 + 2] - var8.cz;
                    if (!var8.wrap) {
                        var5[var17 + 6] = 0.5f + (var18 * var8.rx + var19 * var8.ry + var20 * var8.rz) / var8.width;
                        var5[var17 + 7] = 0.5f - (var18 * var8.ux + var19 * var8.uy + var20 * var8.uz) / var8.height;
                        continue;
                    }
                    float var21 = var18 * var8.dx + var19 * var8.dy + var20 * var8.dz;
                    float var22 = var18 - var21 * var8.dx;
                    float var23 = var19 - var21 * var8.dy;
                    float var24 = var20 - var21 * var8.dz;
                    float var25 = (float)Math.toDegrees(Math.atan2(var22 * var8.rx + var23 * var8.ry + var24 * var8.rz, var22 * var8.ux + var23 * var8.uy + var24 * var8.uz));
                    if (var15 == 0) {
                        var14 = var25;
                    } else {
                        float var26;
                        for (var26 = var25 - var14; var26 > 180.0f; var26 -= 360.0f) {
                        }
                        while (var26 < -180.0f) {
                            var26 += 360.0f;
                        }
                        var25 = var14 + var26;
                    }
                    var5[var17 + 6] = 0.5f + var25 / var8.width;
                    var5[var17 + 7] = 0.5f - var21 / var8.height;
                }
                CharacterMeshBaker.emitClipped(var8, var5, var6, 0.0f, var0, var3, var4);
                if (!var8.wrap || !(var8.width >= 359.9f)) continue;
                CharacterMeshBaker.emitClipped(var8, var5, var6, 1.0f, var0, var3, var4);
                CharacterMeshBaker.emitClipped(var8, var5, var6, -1.0f, var0, var3, var4);
            }
        }
        return var4.build();
    }

    private static boolean faces(SurfaceDecal var0, MeshArrays var1, int var2, int var3, int var4) {
        float var6 = var1.pos[var2 * 3 + 1];
        float var9 = var1.pos[var3 * 3 + 1] - var6;
        float var7 = var1.pos[var2 * 3 + 2];
        float var13 = var1.pos[var4 * 3 + 2] - var7;
        float var10 = var1.pos[var3 * 3 + 2] - var7;
        float var12 = var1.pos[var4 * 3 + 1] - var6;
        float var14 = var9 * var13 - var10 * var12;
        float var5 = var1.pos[var2 * 3];
        float var11 = var1.pos[var4 * 3] - var5;
        float var8 = var1.pos[var3 * 3] - var5;
        float var15 = var10 * var11 - var8 * var13;
        float var16 = var8 * var12 - var9 * var11;
        float var17 = (float)Math.sqrt(var14 * var14 + var15 * var15 + var16 * var16);
        if (var17 < 1.0E-10f) {
            return false;
        }
        var14 /= var17;
        var15 /= var17;
        var16 /= var17;
        int[] var10000 = new int[]{var2, var3, var4};
        int[] var18 = var10000;
        if (!var0.wrap) {
            if (var14 * var0.dx + var15 * var0.dy + var16 * var0.dz < var0.cosLimit) {
                return false;
            }
            int[] var31 = var18;
            int var32 = var18.length;
            for (int var33 = 0; var33 < var32; ++var33) {
                int var34 = var31[var33];
                float var35 = (var1.pos[var34 * 3] - var0.cx) * var0.dx + (var1.pos[var34 * 3 + 1] - var0.cy) * var0.dy + (var1.pos[var34 * 3 + 2] - var0.cz) * var0.dz;
                if (!(var35 < var0.near) && !(var35 > var0.far)) continue;
                return false;
            }
            return true;
        }
        float var19 = (var1.pos[var2 * 3] + var1.pos[var3 * 3] + var1.pos[var4 * 3]) / 3.0f - var0.cx;
        float var20 = (var1.pos[var2 * 3 + 1] + var1.pos[var3 * 3 + 1] + var1.pos[var4 * 3 + 1]) / 3.0f - var0.cy;
        float var21 = (var1.pos[var2 * 3 + 2] + var1.pos[var3 * 3 + 2] + var1.pos[var4 * 3 + 2]) / 3.0f - var0.cz;
        float var22 = var19 * var0.dx + var20 * var0.dy + var21 * var0.dz;
        float var23 = var19 - var22 * var0.dx;
        float var24 = var20 - var22 * var0.dy;
        float var25 = var21 - var22 * var0.dz;
        float var26 = (float)Math.sqrt(var23 * var23 + var24 * var24 + var25 * var25);
        if (var26 < 1.0E-5f) {
            return false;
        }
        if ((var14 * var23 + var15 * var24 + var16 * var25) / var26 < var0.cosLimit) {
            return false;
        }
        float var27 = var0.near == -3.4028235E38f ? 0.0f : var0.near;
        return var26 >= var27 && var26 <= var0.far;
    }

    private static void emitClipped(SurfaceDecal var0, float[] var1, float[] var2, float var3, CharacterPartBuilder var4, CharacterTextureAtlasPacker var5, MeshBufferBuilder.MeshArrays var6) {
        float[] var7 = new float[200];
        System.arraycopy(var1, 0, var7, 0, 30);
        if (var3 != 0.0f) {
            for (int var8 = 0; var8 < 3; ++var8) {
                var7[var8 * 10 + 6] = var7[var8 * 10 + 6] + var3;
            }
        }
        int var20 = 3;
        var20 = CharacterMeshBaker.clip(var7, var20, var2, 6, 0.0f, true);
        var20 = CharacterMeshBaker.clip(var2, var20, var7, 6, 1.0f, false);
        var20 = CharacterMeshBaker.clip(var7, var20, var2, 7, 0.0f, true);
        if ((var20 = CharacterMeshBaker.clip(var2, var20, var7, 7, 1.0f, false)) >= 3) {
            int var9 = -1;
            for (int var10 = 0; var10 < var20; ++var10) {
                int var11 = var10 * 10;
                float var12 = var7[var11 + 3];
                float var13 = var7[var11 + 4];
                float var14 = var7[var11 + 5];
                float var15 = (float)Math.sqrt(var12 * var12 + var13 * var13 + var14 * var14);
                if (var15 > 1.0E-8f) {
                    var12 /= var15;
                    var13 /= var15;
                    var14 /= var15;
                }
                int var16 = SurfaceMaterialSample.channel(1.0f - var0.occlusion * (1.0f - var7[var11 + 8]));
                int var17 = 0xFF000000 | var16 << 16 | var16 << 8 | var16;
                float var18 = var0.flat == var0.flat ? var0.flat : var7[var11 + 9];
                int var19 = var6.vertex(var7[var11] + var12 * var0.offset - var4.pivotX, var7[var11 + 1] + var13 * var0.offset - var4.pivotY, var7[var11 + 2] + var14 * var0.offset - var4.pivotZ, var12, var13, var14, var5.u(var0.canvas, var7[var11 + 6]), var5.v(var0.canvas, var7[var11 + 7]), var17, var0.glow, var18);
                if (var9 >= 0) continue;
                var9 = var19;
            }
            int var25 = 1;
            while (var25 + 2 < var20) {
                var6.quad(var9, var9 + var25, var9 + var25 + 1, var9 + var25 + 2, 2);
                var25 += 2;
            }
            if (var25 + 1 < var20) {
                var6.quad(var9, var9 + var25, var9 + var25 + 1, var9 + var25 + 1, 1);
            }
        }
    }

    private static int clip(float[] var0, int var1, float[] var2, int var3, float var4, boolean var5) {
        int var6 = 0;
        for (int var7 = 0; var7 < var1; ++var7) {
            float var11;
            int var8 = var7 * 10;
            int var9 = (var7 + var1 - 1) % var1 * 10;
            float var10 = var5 ? var0[var8 + var3] - var4 : var4 - var0[var8 + var3];
            float f = var11 = var5 ? var0[var9 + var3] - var4 : var4 - var0[var9 + var3];
            if (var10 >= 0.0f) {
                if (var11 < 0.0f) {
                    var6 = CharacterMeshBaker.cut(var0, var9, var8, var11, var10, var2, var6);
                }
                System.arraycopy(var0, var8, var2, var6 * 10, 10);
                ++var6;
            } else if (var11 >= 0.0f) {
                var6 = CharacterMeshBaker.cut(var0, var9, var8, var11, var10, var2, var6);
            }
            if (var6 >= 19) break;
        }
        return var6;
    }

    private static int cut(float[] var0, int var1, int var2, float var3, float var4, float[] var5, int var6) {
        float var7 = var3 / (var3 - var4);
        for (int var8 = 0; var8 < 10; ++var8) {
            var5[var6 * 10 + var8] = var0[var1 + var8] + (var0[var2 + var8] - var0[var1 + var8]) * var7;
        }
        return var6 + 1;
    }

    private static float reach(MeshBufferBuilder var0) {
        if (var0 == null) {
            return 1.0f;
        }
        float var1 = 0.0f;
        for (int var2 = 0; var2 < var0.vertexCount(); ++var2) {
            float var3 = var0.pos[var2 * 3];
            float var4 = var0.pos[var2 * 3 + 1];
            float var5 = var0.pos[var2 * 3 + 2];
            var1 = Math.max(var1, var3 * var3 + var4 * var4 + var5 * var5);
        }
        return Math.max(0.5f, (float)Math.sqrt(var1));
    }

    private static void jointWeights(MeshBufferBuilder var0, CharacterPartBuilder var1, CharacterPartBuilder var2) {
        if (var0 != null) {
            float[] var3 = new float[var0.vertexCount()];
            float var4 = var2.jointX - var1.pivotX;
            float var5 = var2.jointY - var1.pivotY;
            float var6 = var2.jointZ - var1.pivotZ;
            float var7 = var2.jointBlend;
            boolean var8 = false;
            for (int var9 = 0; var9 < var3.length; ++var9) {
                float var10 = (var0.pos[var9 * 3] - var4) * var2.jointDirX + (var0.pos[var9 * 3 + 1] - var5) * var2.jointDirY + (var0.pos[var9 * 3 + 2] - var6) * var2.jointDirZ;
                float var11 = Math.max(0.0f, Math.min(1.0f, (var10 + var7) / (2.0f * var7)));
                var3[var9] = var11 * var11 * (3.0f - 2.0f * var11);
                var8 |= var3[var9] > 0.0f;
            }
            if (var8) {
                var0.joint = var3;
            }
        }
    }

    private static void weigh(MeshBufferBuilder var0, float var1, float var2) {
        if (var0 != null) {
            float[] var3 = new float[var0.vertexCount()];
            for (int var4 = 0; var4 < var3.length; ++var4) {
                float var5 = var0.pos[var4 * 3];
                float var6 = var0.pos[var4 * 3 + 1];
                float var7 = var0.pos[var4 * 3 + 2];
                float var8 = Math.min(1.0f, (float)Math.sqrt(var5 * var5 + var6 * var6 + var7 * var7) / var1);
                var3[var4] = (float)Math.pow(var8, var2);
            }
            var0.weight = var3;
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class MeshArrays {
        final int count;
        final float[] pos;
        final float[] nrm;
        final int[] color;
        final byte[] glow;
        final float[] ao;
        final float[] flat;
        float[] cover;

        MeshArrays(int var1) {
            this.count = var1;
            this.pos = new float[var1 * 3];
            this.nrm = new float[var1 * 3];
            this.color = new int[var1];
            this.glow = new byte[var1];
            this.ao = new float[var1];
            this.flat = new float[var1];
        }
    }
}

