/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import hook.feature.CharacterTextureAtlas;
import hook.feature.GlbDocument;
import hook.feature.GltfTransforms;
import hook.feature.HeightSliceLimbPartitioner;
import hook.feature.HumanoidBoneMap;
import hook.feature.PosedGltfMesh;
import hook.feature.QuadricMeshSimplifier;
import hook.feature.UiTextureAtlas;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class GltfCharacterBaker {
    static final int HEAD = 0;
    static final int BODY = 1;
    static final int RIGHT_ARM = 2;
    static final int LEFT_ARM = 3;
    static final int RIGHT_LEG = 4;
    static final int LEFT_LEG = 5;
    private static final int[] BUDGETS;
    private static final int DENSE = 150000;
    private static final float[] STICK;

    private GltfCharacterBaker() {
    }

    public static BakedCharacterModel bake(byte[] var0, CharacterProfile var1, UiTextureAtlas var2) throws IOException {
        PartProfile var7;
        GlbDocument var3 = GlbDocument.read(var0);
        PosedGltfMesh var4 = PosedGltfMesh.pose(var3, Map.of());
        HumanoidBoneMap var5 = HumanoidBoneMap.find(var4, var1.bones);
        Map<Integer, Float> var6 = var5.armTurns();
        if (!var6.isEmpty()) {
            var4 = PosedGltfMesh.pose(var3, var6);
            var5 = HumanoidBoneMap.find(var4, var1.bones);
        }
        var4.normalise(var1.height, var5.facesBack);
        var4.cluster(150000);
        PartProfile partProfile = var7 = var5.found() ? GltfCharacterBaker.skeletonCut(var4, var5, var1) : HeightSliceLimbPartitioner.cut(var4);
        if (var7 == null) {
            var7 = GltfCharacterBaker.still(var4);
        }
        List<BoneLayer> var8 = GltfCharacterBaker.split(var4, var7.part());
        GltfCharacterBaker.reduce(var4, var8);
        CharacterTextureAtlas var9 = CharacterTextureAtlas.pack(var3, GltfCharacterBaker.uvBounds(var4, var8), var2);
        ByteArrayOutputStream var10 = new ByteArrayOutputStream();
        GltfCharacterBaker.write(new DataOutputStream(var10), var4, var7, var8, var9, var1);
        ArrayList<String> var11 = new ArrayList<String>();
        if (GltfCharacterBaker.has(var8, 0)) {
            var11.add("head");
        }
        if (GltfCharacterBaker.has(var8, 2) || GltfCharacterBaker.has(var8, 3)) {
            var11.add("arms");
        }
        if (GltfCharacterBaker.has(var8, 4) || GltfCharacterBaker.has(var8, 5)) {
            var11.add("legs");
        }
        return new BakedCharacterModel(var10.toByteArray(), var11);
    }

    public static String describe(byte[] var0, CharacterProfile var1) throws IOException {
        GlbDocument var2 = GlbDocument.read(var0);
        PosedGltfMesh var3 = PosedGltfMesh.pose(var2, Map.of());
        HumanoidBoneMap var4 = HumanoidBoneMap.find(var3, var1.bones);
        Locale var10002 = Locale.ROOT;
        String var10003 = "%d vertices, %d triangles, %d bones%n";
        Object[] var10004 = new Object[]{var3.vertices, var3.triangles, var3.joints.size()};
        StringBuilder var5 = new StringBuilder(String.format(var10002, var10003, var10004));
        var5.append(var4.found() ? var4.describe() : "no usable skeleton, limbs are cut by shape\n");
        HashSet<Integer> var6 = new HashSet<Integer>();
        for (int var7 = 0; var7 < var3.vertices; ++var7) {
            var6.add(var3.material[var7]);
        }
        for (Object e : var6) {
            int var8 = (Integer)e;
            var5.append("material ").append(var2.materialName(var8)).append('\n');
        }
        return var5.toString();
    }

    private static PartProfile still(PosedGltfMesh var0) {
        float[][] var1 = new float[6][];
        int var10001 = 1;
        float[] var10002 = new float[]{0.0f, 24.0f, 0.0f};
        var1[var10001] = var10002;
        return new PartProfile(new int[var0.vertices], var1, new float[6][], new float[6][], new float[6][]);
    }

    private static PartProfile skeletonCut(PosedGltfMesh var0, HumanoidBoneMap var1, CharacterProfile var2) {
        int[] var3 = var1.groups();
        boolean var4 = var1.thigh[0] >= 0 && var1.thigh[1] >= 0;
        float var5 = var2.floor;
        if (var5 < 0.0f && var1.shin[0] >= 0 && var1.shin[1] >= 0) {
            var5 = Math.min(var0.joint(var1.shin[0])[1], var0.joint(var1.shin[1])[1]);
        }
        boolean var6 = !var2.headMaterials.isEmpty() && var1.head >= 0;
        int[] var7 = new int[var0.vertices];
        float[] var8 = new float[6];
        for (int var9 = 0; var9 < var0.vertices; ++var9) {
            boolean var23;
            float var10 = var0.pos[var9 * 3];
            float var11 = var0.pos[var9 * 3 + 1];
            if (var6 && var2.headMaterials.contains(var0.file.materialName(var0.material[var9]))) {
                var7[var9] = var11 < var2.neck ? 1 : 0;
                continue;
            }
            Arrays.fill(var8, 0.0f);
            for (int var12 = 0; var12 < 4; ++var12) {
                int var13 = var0.influence[var9 * 4 + var12];
                if (var13 < 0) continue;
                var8[var3[var13]] = var8[var3[var13]] + var0.weight[var9 * 4 + var12];
            }
            int var20 = 1;
            for (int var22 = 0; var22 < 6; ++var22) {
                if (var22 == 1 || !(var8[var22] >= STICK[var22]) || !(var8[var22] > var8[var20]) || var22 == 0 && var6) continue;
                var20 = var22;
            }
            boolean bl = var23 = var20 == 1 || var20 == 4 || var20 == 5;
            if (var4 && var23 && var2.hip > 0.0f) {
                var20 = var11 >= var2.hip ? 1 : (var10 < 0.0f ? 4 : 5);
            } else if (var4 && var20 == 1 && var11 < var5) {
                var20 = var10 < 0.0f ? 4 : 5;
            }
            var7[var9] = var20;
        }
        float[][] var17 = new float[6][];
        float[][] var18 = new float[6][];
        float[][] var19 = new float[6][];
        float[][] var21 = new float[6][];
        int var10001 = 1;
        float[] var10002 = new float[]{0.0f, 24.0f, 0.0f};
        var17[var10001] = var10002;
        if (var1.head >= 0) {
            var17[0] = var0.joint(var1.head);
        }
        for (int var24 = 0; var24 < 2; ++var24) {
            int var15;
            int var14 = var24 == 0 ? 2 : 3;
            int n = var15 = var24 == 0 ? 4 : 5;
            if (var1.upperArm[var24] >= 0) {
                var17[var14] = var0.joint(var1.upperArm[var24]);
                int var16 = var1.hand[var24] >= 0 ? var1.hand[var24] : var1.forearm[var24];
                float[] fArray = var18[var14] = var16 >= 0 ? var0.joint(var16) : var17[var14];
                if (var1.forearm[var24] >= 0) {
                    var19[var14] = var0.joint(var1.forearm[var24]);
                    var21[var14] = GltfCharacterBaker.bend(var0, var1.forearm[var24], var1.upperArm[var24]);
                }
            }
            if (var1.thigh[var24] < 0) continue;
            var17[var15] = var0.joint(var1.thigh[var24]);
            if (var1.shin[var24] < 0) continue;
            var19[var15] = var0.joint(var1.shin[var24]);
            var21[var15] = GltfCharacterBaker.bend(var0, var1.shin[var24], var1.thigh[var24]);
        }
        return new PartProfile(var7, var17, var18, var19, var21);
    }

    static int owner(int[] var0, int var1, int var2, int var3) {
        return var0[var2] == var0[var3] ? var0[var2] : var0[var1];
    }

    private static List<BoneLayer> split(PosedGltfMesh var0, int[] var1) {
        BoneLayer[][] var2 = new BoneLayer[6][2];
        for (int var3 = 0; var3 < var0.triangles; ++var3) {
            int var8;
            int var4 = var0.tris[var3 * 3];
            int var5 = var0.tris[var3 * 3 + 1];
            int var6 = var0.tris[var3 * 3 + 2];
            if (GltfTransforms.area(var0.pos, var4, var5, var6) < 1.0E-9f) continue;
            int var7 = GltfCharacterBaker.owner(var1, var4, var5, var6);
            BoneLayer var9 = var2[var7][var8 = var0.file.translucent(var0.material[var4]) ? 1 : 0];
            if (var9 == null) {
                BoneLayer boneLayer = new BoneLayer(var7, var8);
                var2[var7][var8] = boneLayer;
                var9 = boneLayer;
            }
            var9.source.add(var3);
            if (var8 != 1 || !var0.file.doubleSided(var0.material[var4])) continue;
            var9.source.add(~var3);
        }
        ArrayList<BoneLayer> var12 = new ArrayList<BoneLayer>();
        BoneLayer[][] var13 = var2;
        int var14 = var2.length;
        for (int var15 = 0; var15 < var14; ++var15) {
            BoneLayer[] var16;
            BoneLayer[] var17 = var16 = var13[var15];
            int var18 = var16.length;
            for (int var10 = 0; var10 < var18; ++var10) {
                BoneLayer var11 = var17[var10];
                if (var11 == null) continue;
                var12.add(var11);
            }
        }
        return var12;
    }

    private static void reduce(PosedGltfMesh var0, List<BoneLayer> var1) {
        double var2 = 0.0;
        for (BoneLayer var5 : var1) {
            var2 += (double)var5.source.size() * (var5.bone == 0 ? 2.0 : 1.0);
        }
        for (BoneLayer var11 : var1) {
            int[] var6 = new int[BUDGETS.length];
            for (int var7 = 0; var7 < BUDGETS.length; ++var7) {
                double var8 = (double)var11.source.size() * (var11.bone == 0 ? 2.0 : 1.0) / var2;
                var6[var7] = (int)Math.min((long)var11.source.size(), Math.max(16L, Math.round((double)BUDGETS[var7] * var8)));
            }
            boolean var12 = var6[BUDGETS.length - 1] < var11.source.size();
            var11.levels = new QuadricMeshSimplifier(var0, var11.source, var12).reduce(var6);
        }
    }

    private static Map<Integer, float[]> uvBounds(PosedGltfMesh var0, List<BoneLayer> var1) {
        LinkedHashMap<Integer, float[]> var2 = new LinkedHashMap<Integer, float[]>();
        for (BoneLayer var4 : var1) {
            for (int var6 : var4.source) {
                int var7 = var6 < 0 ? ~var6 : var6;
                for (int var8 = 0; var8 < 3; ++var8) {
                    int var9 = var0.tris[var7 * 3 + var8];
                    float[] var10 = var2.computeIfAbsent(var0.material[var9], var0x -> {
                        float[] var10000 = new float[]{1.0E9f, 1.0E9f, -1.0E9f, -1.0E9f};
                        return var10000;
                    });
                    var10[0] = Math.min(var10[0], var0.uv[var9 * 2]);
                    var10[1] = Math.min(var10[1], var0.uv[var9 * 2 + 1]);
                    var10[2] = Math.max(var10[2], var0.uv[var9 * 2]);
                    var10[3] = Math.max(var10[3], var0.uv[var9 * 2 + 1]);
                }
            }
        }
        return var2;
    }

    private static boolean has(List<BoneLayer> var0, int var1) {
        for (BoneLayer var3 : var0) {
            if (var3.bone != var1) continue;
            return true;
        }
        return false;
    }

    private static BoneLayer find(List<BoneLayer> var0, int var1, int var2) {
        for (BoneLayer var4 : var0) {
            if (var4.bone != var1 || var4.layer != var2) continue;
            return var4;
        }
        return null;
    }

    private static void write(DataOutputStream var0, PosedGltfMesh var1, PartProfile var2, List<BoneLayer> var3, CharacterTextureAtlas var4, CharacterProfile var5) throws IOException {
        float[][] var6 = var2.pivot();
        float[][] var7 = var2.grip();
        float[][] var8 = var2.bendAt();
        float[][] var9 = var2.bendWeights();
        float[] var10 = var1.bounds();
        float var11 = var6[4] != null && var6[5] != null ? (var6[4][1] + var6[5][1]) * 0.5f : 12.0f;
        var0.writeInt(1095648072);
        var0.writeInt(3);
        var0.writeFloat(var10[1]);
        var0.writeFloat(var10[4]);
        var0.writeFloat(var11);
        var0.writeFloat(var5.flat);
        var0.writeBoolean(true);
        var0.writeBoolean(var1.colored);
        var0.writeInt(BUDGETS.length);
        int var12 = 0;
        for (int var13 = 0; var13 < 6; ++var13) {
            if (!GltfCharacterBaker.has(var3, var13)) continue;
            ++var12;
        }
        var0.writeInt(var12);
        for (int var21 = 0; var21 < 6; ++var21) {
            if (!GltfCharacterBaker.has(var3, var21)) continue;
            var0.writeByte(var21);
            for (float var17 : var6[var21]) {
                var0.writeFloat(var17);
            }
            float[] var14 = var7[var21] != null ? var7[var21] : var6[var21];
            int var15 = var14.length;
            for (int var28 = 0; var28 < var15; ++var28) {
                float var31 = var14[var28];
                var0.writeFloat(var31);
            }
            float[] var26 = var14 = GltfCharacterBaker.bounds(var1, var3, var21);
            int var29 = var14.length;
            for (int var32 = 0; var32 < var29; ++var32) {
                float var18 = var26[var32];
                var0.writeFloat(var18);
            }
            var15 = var21 != 2 && var21 != 3 ? 0 : 1;
            int n = var29 = var21 != 4 && var21 != 5 ? 0 : 1;
            var0.writeFloat(var15 != 0 ? 0.6f : (var29 != 0 ? 0.55f : 1.0f));
            var0.writeFloat(var15 != 0 ? 40.0f : (var29 != 0 ? 30.0f : 0.0f));
            var0.writeBoolean(var8[var21] != null);
            if (var8[var21] != null) {
                for (float var20 : var8[var21]) {
                    var0.writeFloat(var20);
                }
            }
            for (int var34 = 0; var34 < BUDGETS.length; ++var34) {
                for (int var36 = 0; var36 < 2; ++var36) {
                    GltfCharacterBaker.writeLevel(var0, var1, var4, GltfCharacterBaker.find(var3, var21, var36), var34, var14, var9[var21], var8[var21] != null);
                }
            }
        }
        byte[] var22 = var4.packed();
        var0.writeInt(var4.width);
        var0.writeInt(var4.height);
        var0.writeInt(var22.length);
        var0.write(var22);
        var0.flush();
    }

    private static float[] bend(PosedGltfMesh var0, int var1, int var2) {
        float[] var3 = var0.joint(var1);
        float[] var4 = var0.below(var1);
        float[] var5 = var0.below(var2);
        for (int var6 = 0; var6 < var0.vertices; ++var6) {
            if (var5[var6] >= 0.5f) continue;
            float var7 = Math.max(0.0f, Math.min(1.0f, var3[1] - var0.pos[var6 * 3 + 1] + 0.5f));
            var4[var6] = var7 * var7 * (3.0f - 2.0f * var7);
        }
        return var4;
    }

    private static float[] bounds(PosedGltfMesh var0, List<BoneLayer> var1, int var2) {
        float[] var10000 = new float[]{1.0E9f, 1.0E9f, 1.0E9f, -1.0E9f, -1.0E9f, -1.0E9f};
        float[] var3 = var10000;
        for (BoneLayer var5 : var1) {
            if (var5.bone != var2) continue;
            for (int var7 : var5.source) {
                int var8 = var7 < 0 ? ~var7 : var7;
                for (int var9 = 0; var9 < 3; ++var9) {
                    int var10 = var0.tris[var8 * 3 + var9];
                    for (int var11 = 0; var11 < 3; ++var11) {
                        var3[var11] = Math.min(var3[var11], var0.pos[var10 * 3 + var11]);
                        var3[var11 + 3] = Math.max(var3[var11 + 3], var0.pos[var10 * 3 + var11]);
                    }
                }
            }
        }
        for (int var12 = 0; var12 < 3; ++var12) {
            if (!(var3[var12 + 3] - var3[var12] < 0.001f)) continue;
            var3[var12 + 3] = var3[var12] + 0.001f;
        }
        return var3;
    }

    /*
     * WARNING - void declaration
     */
    private static void writeLevel(DataOutputStream var0, PosedGltfMesh var1, CharacterTextureAtlas var2, BoneLayer var3, int var4, float[] var5, float[] var6, boolean var7) throws IOException {
        if (var3 == null) {
            var0.writeInt(0);
            var0.writeInt(0);
        } else {
            int[] var8 = var3.levels[var4];
            HashMap<Integer, Integer> var9 = new HashMap<Integer, Integer>();
            ArrayList<Integer> var10 = new ArrayList<Integer>();
            int[] var11 = new int[var8.length];
            for (int var12 = 0; var12 < var8.length; ++var12) {
                Integer n = (Integer)var9.get(var8[var12]);
                int resolved;
                if (n == null) {
                    Integer n2 = var10.size();
                    var9.put(var8[var12], n2);
                    var10.add(var8[var12]);
                    resolved = n2;
                } else {
                    resolved = n;
                }
                var11[var12] = resolved;
            }
            var0.writeInt(var10.size());
            for (Object e : var10) {
                int var23 = (Integer)e;
                int var14 = var23 < 0 ? ~var23 : var23;
                for (int var15 = 0; var15 < 3; ++var15) {
                    float var16 = (var1.pos[var14 * 3 + var15] - var5[var15]) / (var5[var15 + 3] - var5[var15]);
                    var0.writeChar(Math.round(Math.max(0.0f, Math.min(1.0f, var16)) * 65535.0f));
                }
            }
            for (Object e : var10) {
                int var24 = (Integer)e;
                int var29 = var24 < 0 ? ~var24 : var24;
                float var33 = var24 < 0 ? -1.0f : 1.0f;
                for (int var36 = 0; var36 < 3; ++var36) {
                    var0.writeByte(Math.round(var1.nrm[var29 * 3 + var36] * var33 * 127.0f));
                }
            }
            for (Object e : var10) {
                int var25 = (Integer)e;
                int var30 = var25 < 0 ? ~var25 : var25;
                float[] var34 = var2.map(var1.material[var30], var1.uv[var30 * 2], var1.uv[var30 * 2 + 1]);
                var0.writeChar(Math.round(Math.max(0.0f, Math.min(1.0f, var34[0])) * 65535.0f));
                var0.writeChar(Math.round(Math.max(0.0f, Math.min(1.0f, var34[1])) * 65535.0f));
            }
            if (var1.colored) {
                for (Object e : var10) {
                    int var26 = (Integer)e;
                    int var31 = var1.color[var26 < 0 ? ~var26 : var26];
                    var0.writeByte(var31 >> 16);
                    var0.writeByte(var31 >> 8);
                    var0.writeByte(var31);
                }
            }
            if (var7) {
                for (Object e : var10) {
                    int var27 = (Integer)e;
                    var0.writeByte(Math.round(Math.max(0.0f, Math.min(1.0f, var6[var27 < 0 ? ~var27 : var27])) * 255.0f));
                }
            }
            var0.writeInt(var11.length / 3);
            boolean var22 = var10.size() > 65535;
            int[] nArray = var11;
            int var32 = var11.length;
            for (int var35 = 0; var35 < var32; ++var35) {
                int var37 = nArray[var35];
                if (var22) {
                    var0.writeInt(var37);
                    continue;
                }
                var0.writeChar(var37);
            }
        }
    }

    static {
        int[] nArray = new int[3];
        int[] var3 = nArray;
        nArray[0] = 7500;
        var3[1] = 15000;
        var3[2] = 30000;
        BUDGETS = var3;
        float[] fArray = new float[6];
        float[] var5 = fArray;
        fArray[0] = 0.7f;
        var5[1] = 0.0f;
        var5[2] = 0.6f;
        var5[3] = 0.6f;
        var5[4] = 0.3f;
        var5[5] = 0.3f;
        STICK = var5;
    }

    @Environment(value=EnvType.CLIENT)
    public static final class CharacterProfile {
        public float height = 30.0f;
        public float flat;
        public final Map<String, String> bones = new HashMap<String, String>();
        public final Set<String> headMaterials = new HashSet<String>();
        public float neck = -1.0f;
        public float hip = -1.0f;
        public float floor = -1.0f;
    }

    @Environment(value=EnvType.CLIENT)
    record PartProfile(int[] part, float[][] pivot, float[][] grip, float[][] bendAt, float[][] bendWeights) {
    }

    @Environment(value=EnvType.CLIENT)
    public record BakedCharacterModel(byte[] data, List<String> limbs) {
    }

    @Environment(value=EnvType.CLIENT)
    private static final class BoneLayer {
        final int bone;
        final int layer;
        final List<Integer> source = new ArrayList<Integer>();
        int[][] levels;

        BoneLayer(int bone, int layer) {
            this.bone = bone;
            this.layer = layer;
        }
    }
}

