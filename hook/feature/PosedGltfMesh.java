/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hook.feature.GlbDocument;
import hook.feature.GltfCharacterBaker;
import hook.feature.GltfTransforms;
import java.io.IOException;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class PosedGltfMesh {
    private static final int MAX_TRIANGLES = 4000000;
    final GlbDocument file;
    final float[][] globals;
    final int[] parent;
    final Set<Integer> joints = new HashSet<Integer>();
    float[] pos = new float[0];
    float[] nrm = new float[0];
    float[] uv = new float[0];
    int[] material = new int[0];
    int[] color = new int[0];
    boolean colored;
    int[] influence = new int[0];
    float[] weight = new float[0];
    int[] tris = new int[0];
    int vertices;
    int triangles;
    private float scale = 1.0f;
    private float shiftX;
    private float shiftY;
    private float shiftZ;
    private boolean flipped;

    private PosedGltfMesh(GlbDocument var1) {
        this.file = var1;
        this.globals = new float[var1.nodes()][];
        this.parent = new int[var1.nodes()];
        Arrays.fill(this.parent, -1);
    }

    static PosedGltfMesh pose(GlbDocument var0, Map<Integer, Float> var1) throws IOException {
        PosedGltfMesh var2 = new PosedGltfMesh(var0);
        for (int var3 = 0; var3 < var0.nodes(); ++var3) {
            for (JsonElement var5 : GlbDocument.array(var0.node(var3), "children")) {
                var2.parent[var5.getAsInt()] = var3;
            }
        }
        JsonArray var9 = GlbDocument.array(var0.json, "scenes");
        JsonArray var10 = new JsonArray();
        if (var9.isEmpty()) {
            for (int var11 = 0; var11 < var0.nodes(); ++var11) {
                if (var2.parent[var11] >= 0) continue;
                var10.add((Number)var11);
            }
        } else {
            var10 = GlbDocument.array(var9.get(GlbDocument.integer(var0.json, "scene", 0)).getAsJsonObject(), "nodes");
        }
        for (JsonElement var6 : var10) {
            var2.walk(var6.getAsInt(), GltfTransforms.identity(), var1);
        }
        for (JsonElement var15 : GlbDocument.array(var0.json, "skins")) {
            for (JsonElement var8 : GlbDocument.array(var15.getAsJsonObject(), "joints")) {
                var2.joints.add(var8.getAsInt());
            }
        }
        for (JsonElement var16 : var10) {
            var2.collect(var16.getAsInt());
        }
        if (var2.triangles == 0) {
            throw new IOException("it has no triangles");
        }
        return var2;
    }

    private void walk(int var1, float[] var2, Map<Integer, Float> var3) {
        float[] var4 = GltfTransforms.mul(var2, GltfTransforms.local(this.file.node(var1)));
        Float var5 = var3.get(var1);
        if (var5 != null) {
            var4 = GltfTransforms.mul(GltfTransforms.rollAbout(var5.floatValue(), GltfTransforms.point(var4, 0.0f, 0.0f, 0.0f)), var4);
        }
        this.globals[var1] = var4;
        for (JsonElement var7 : GlbDocument.array(this.file.node(var1), "children")) {
            this.walk(var7.getAsInt(), var4, var3);
        }
    }

    private void collect(int var1) throws IOException {
        JsonObject var2 = this.file.node(var1);
        if (var2.has("mesh")) {
            JsonObject var3 = GlbDocument.array(this.file.json, "meshes").get(var2.get("mesh").getAsInt()).getAsJsonObject();
            int var4 = GlbDocument.integer(var2, "skin", -1);
            for (JsonElement var6 : GlbDocument.array(var3, "primitives")) {
                this.primitive(var6.getAsJsonObject(), var1, var4);
            }
        }
        for (JsonElement var8 : GlbDocument.array(var2, "children")) {
            this.collect(var8.getAsInt());
        }
    }

    private void primitive(JsonObject var1, int var2, int var3) throws IOException {
        if (GlbDocument.integer(var1, "mode", 4) == 4) {
            int[] var13;
            JsonObject var4 = GlbDocument.object(var1, "attributes");
            int var5 = GlbDocument.integer(var1, "material", -1);
            float[] var6 = this.file.floats(var4.get("POSITION").getAsInt());
            int var7 = var6.length / 3;
            float[] var8 = var4.has("NORMAL") ? this.file.floats(var4.get("NORMAL").getAsInt()) : null;
            String var9 = "TEXCOORD_" + this.file.texCoord(var5);
            float[] var10 = var4.has(var9) ? this.file.floats(var4.get(var9).getAsInt()) : new float[var7 * 2];
            float[] var11 = var4.has("COLOR_0") ? this.file.floats(var4.get("COLOR_0").getAsInt()) : null;
            int var12 = var11 == null ? 0 : var11.length / var7;
            this.colored |= var11 != null;
            int[] nArray = var13 = var1.has("indices") ? this.file.ints(var1.get("indices").getAsInt()) : null;
            if (var13 == null) {
                var13 = new int[var7];
                int var14 = 0;
                while (var14 < var7) {
                    var13[var14] = var14++;
                }
            }
            int[] var36 = var13;
            int var15 = var13.length;
            for (int var16 = 0; var16 < var15; ++var16) {
                int var17 = var36[var16];
                if (var17 >= 0 && var17 < var7) continue;
                throw new IOException("a mesh has broken indices");
            }
            if (this.triangles + var13.length / 3 > 4000000) {
                throw new IOException("it has more than four million triangles");
            }
            float[][] var37 = null;
            int[] var38 = null;
            int[] var39 = null;
            float[] var40 = null;
            if (var3 >= 0 && var4.has("JOINTS_0") && var4.has("WEIGHTS_0")) {
                JsonObject var18 = GlbDocument.array(this.file.json, "skins").get(var3).getAsJsonObject();
                JsonArray var19 = GlbDocument.array(var18, "joints");
                var39 = new int[var19.size()];
                float[] var20 = var18.has("inverseBindMatrices") ? this.file.floats(var18.get("inverseBindMatrices").getAsInt()) : null;
                var37 = new float[var19.size()][];
                for (int var21 = 0; var21 < var19.size(); ++var21) {
                    var39[var21] = var19.get(var21).getAsInt();
                    float[] var22 = var20 == null ? GltfTransforms.identity() : Arrays.copyOfRange(var20, var21 * 16, var21 * 16 + 16);
                    float[] var23 = this.globals[var39[var21]];
                    var37[var21] = GltfTransforms.mul(var23 == null ? GltfTransforms.identity() : var23, var22);
                }
                var38 = this.file.ints(var4.get("JOINTS_0").getAsInt());
                var40 = this.file.floats(var4.get("WEIGHTS_0").getAsInt());
            }
            float[] var41 = this.globals[var2];
            int var42 = this.vertices;
            this.grow(var7, var13.length / 3);
            for (int var43 = 0; var43 < var7; ++var43) {
                float[] var29;
                float[] var28;
                float var46 = var6[var43 * 3];
                float var48 = var6[var43 * 3 + 1];
                float var49 = var6[var43 * 3 + 2];
                float var24 = var8 == null ? 0.0f : var8[var43 * 3];
                float var25 = var8 == null ? 1.0f : var8[var43 * 3 + 1];
                float var26 = var8 == null ? 0.0f : var8[var43 * 3 + 2];
                int var27 = var42 + var43;
                Arrays.fill(this.influence, var27 * 4, var27 * 4 + 4, -1);
                if (var37 == null) {
                    var28 = GltfTransforms.point(var41, var46, var48, var49);
                    var29 = GltfTransforms.dir(var41, var24, var25, var26);
                    this.influence[var27 * 4] = var2;
                    this.weight[var27 * 4] = 1.0f;
                } else {
                    var28 = new float[3];
                    var29 = new float[3];
                    for (int var30 = 0; var30 < 4; ++var30) {
                        float var31 = var40[var43 * 4 + var30];
                        int var32 = var38[var43 * 4 + var30];
                        if (var31 <= 0.0f || var32 < 0 || var32 >= var37.length) continue;
                        float[] var33 = GltfTransforms.point(var37[var32], var46, var48, var49);
                        float[] var34 = GltfTransforms.dir(var37[var32], var24, var25, var26);
                        for (int var35 = 0; var35 < 3; ++var35) {
                            int n = var35;
                            var28[n] = var28[n] + var33[var35] * var31;
                            int n2 = var35;
                            var29[n2] = var29[n2] + var34[var35] * var31;
                        }
                        this.influence[var27 * 4 + var30] = var39[var32];
                        this.weight[var27 * 4 + var30] = var31;
                    }
                }
                System.arraycopy(var28, 0, this.pos, var27 * 3, 3);
                GltfTransforms.normalise(var29);
                System.arraycopy(var29, 0, this.nrm, var27 * 3, 3);
                this.uv[var27 * 2] = var10[var43 * 2];
                this.uv[var27 * 2 + 1] = var10[var43 * 2 + 1];
                this.material[var27] = var5;
                this.color[var27] = var11 == null ? -1 : 0xFF000000 | PosedGltfMesh.srgb(var11[var43 * var12]) << 16 | PosedGltfMesh.srgb(var11[var43 * var12 + 1]) << 8 | PosedGltfMesh.srgb(var11[var43 * var12 + 2]);
            }
            for (int var44 = 0; var44 < var13.length / 3; ++var44) {
                for (int var47 = 0; var47 < 3; ++var47) {
                    this.tris[(this.triangles + var44) * 3 + var47] = var42 + var13[var44 * 3 + var47];
                }
            }
            int var45 = this.triangles;
            this.vertices += var7;
            this.triangles += var13.length / 3;
            if (var8 == null) {
                this.smoothNormals(var42, var7, var45);
            }
        }
    }

    private void smoothNormals(int var1, int var2, int var3) {
        Arrays.fill(this.nrm, var1 * 3, (var1 + var2) * 3, 0.0f);
        for (int var4 = var3; var4 < this.triangles; ++var4) {
            float[] var5 = GltfTransforms.faceNormal(this.pos, this.tris[var4 * 3], this.tris[var4 * 3 + 1], this.tris[var4 * 3 + 2]);
            for (int var6 = 0; var6 < 3; ++var6) {
                int var7 = this.tris[var4 * 3 + var6];
                for (int var8 = 0; var8 < 3; ++var8) {
                    this.nrm[var7 * 3 + var8] = this.nrm[var7 * 3 + var8] + var5[var8];
                }
            }
        }
        for (int var9 = var1; var9 < var1 + var2; ++var9) {
            float[] var10000 = new float[]{this.nrm[var9 * 3], this.nrm[var9 * 3 + 1], this.nrm[var9 * 3 + 2]};
            float[] var10 = var10000;
            GltfTransforms.normalise(var10);
            System.arraycopy(var10, 0, this.nrm, var9 * 3, 3);
        }
    }

    private void grow(int var1, int var2) {
        int var3 = this.vertices + var1;
        int var4 = this.triangles + var2;
        this.pos = Arrays.copyOf(this.pos, var3 * 3);
        this.nrm = Arrays.copyOf(this.nrm, var3 * 3);
        this.uv = Arrays.copyOf(this.uv, var3 * 2);
        this.influence = Arrays.copyOf(this.influence, var3 * 4);
        this.weight = Arrays.copyOf(this.weight, var3 * 4);
        this.material = Arrays.copyOf(this.material, var3);
        this.color = Arrays.copyOf(this.color, var3);
        this.tris = Arrays.copyOf(this.tris, var4 * 3);
    }

    private static int srgb(float var0) {
        return Math.round((float)Math.pow(Math.max(0.0f, Math.min(1.0f, var0)), 0.45454545454545453) * 255.0f);
    }

    void cluster(int var1) {
        if (this.triangles > var1) {
            float[] var2 = this.bounds();
            float var3 = Math.max(var2[3] - var2[0], Math.max(var2[4] - var2[1], var2[5] - var2[2])) / 600.0f;
            int[] var4 = new int[this.vertices];
            while (this.weld(var3, var4) > var1) {
                var3 *= 1.25f;
            }
            int var5 = 0;
            int[] var6 = var4;
            int var7 = var4.length;
            for (int var8 = 0; var8 < var7; ++var8) {
                int var9 = var6[var8];
                var5 = Math.max(var5, var9 + 1);
            }
            float[] var22 = new float[var5 * 3];
            float[] var23 = new float[var5 * 3];
            float[] var24 = new float[var5 * 2];
            int[] var25 = new int[var5];
            int[] var10 = new int[var5];
            int[] var11 = new int[var5 * 4];
            float[] var12 = new float[var5 * 4];
            long[] var13 = new long[var5 * 3];
            for (int var14 = 0; var14 < this.vertices; ++var14) {
                int var15 = var4[var14];
                int var10000 = var25[var15];
                int n = var15;
                var25[n] = var25[n] + 1;
                if (var10000 == 0) {
                    var10[var15] = this.material[var14];
                    System.arraycopy(this.influence, var14 * 4, var11, var15 * 4, 4);
                    System.arraycopy(this.weight, var14 * 4, var12, var15 * 4, 4);
                }
                for (int var16 = 0; var16 < 3; ++var16) {
                    var22[var15 * 3 + var16] = var22[var15 * 3 + var16] + this.pos[var14 * 3 + var16];
                    var23[var15 * 3 + var16] = var23[var15 * 3 + var16] + this.nrm[var14 * 3 + var16];
                    var13[var15 * 3 + var16] = var13[var15 * 3 + var16] + (long)(this.color[var14] >> 16 - var16 * 8 & 0xFF);
                }
                var24[var15 * 2] = var24[var15 * 2] + this.uv[var14 * 2];
                var24[var15 * 2 + 1] = var24[var15 * 2 + 1] + this.uv[var14 * 2 + 1];
            }
            int[] var26 = new int[var5];
            for (int var27 = 0; var27 < var5; ++var27) {
                for (int var29 = 0; var29 < 3; ++var29) {
                    var22[var27 * 3 + var29] = var22[var27 * 3 + var29] / (float)var25[var27];
                }
                var24[var27 * 2] = var24[var27 * 2] / (float)var25[var27];
                var24[var27 * 2 + 1] = var24[var27 * 2 + 1] / (float)var25[var27];
                float[] var34 = new float[]{var23[var27 * 3], var23[var27 * 3 + 1], var23[var27 * 3 + 2]};
                float[] var30 = var34;
                GltfTransforms.normalise(var30);
                System.arraycopy(var30, 0, var23, var27 * 3, 3);
                var26[var27] = 0xFF000000 | (int)(var13[var27 * 3] / (long)var25[var27]) << 16 | (int)(var13[var27 * 3 + 1] / (long)var25[var27]) << 8 | (int)(var13[var27 * 3 + 2] / (long)var25[var27]);
            }
            HashSet<Long> var28 = new HashSet<Long>();
            int[] var31 = new int[this.triangles * 3];
            int var17 = 0;
            for (int var18 = 0; var18 < this.triangles; ++var18) {
                int var19 = var4[this.tris[var18 * 3]];
                int var20 = var4[this.tris[var18 * 3 + 1]];
                int var21 = var4[this.tris[var18 * 3 + 2]];
                if (var19 == var20 || var20 == var21 || var19 == var21 || !var28.add(PosedGltfMesh.key(var19, var20, var21))) continue;
                var31[var17++] = var19;
                var31[var17++] = var20;
                var31[var17++] = var21;
            }
            this.pos = var22;
            this.nrm = var23;
            this.uv = var24;
            this.material = var10;
            this.influence = var11;
            this.weight = var12;
            this.color = var26;
            this.tris = Arrays.copyOf(var31, var17);
            this.vertices = var5;
            this.triangles = var17 / 3;
        }
    }

    private int weld(float var1, int[] var2) {
        HashMap<CallSite, Integer> var3 = new HashMap<CallSite, Integer>();
        for (int var4 = 0; var4 < this.vertices; ++var4) {
            String var5 = (int)Math.floor(this.pos[var4 * 3] / var1) + "," + (int)Math.floor(this.pos[var4 * 3 + 1] / var1) + "," + (int)Math.floor(this.pos[var4 * 3 + 2] / var1) + "," + this.material[var4] + "," + Math.round(this.uv[var4 * 2] * 64.0f) + "," + Math.round(this.uv[var4 * 2 + 1] * 64.0f);
            Integer var6 = var3.putIfAbsent((CallSite)((Object)var5), var3.size());
            var2[var4] = var6 == null ? var3.size() - 1 : var6;
        }
        HashSet<Long> var9 = new HashSet<Long>();
        for (int var10 = 0; var10 < this.triangles; ++var10) {
            int var11 = var2[this.tris[var10 * 3]];
            int var7 = var2[this.tris[var10 * 3 + 1]];
            int var8 = var2[this.tris[var10 * 3 + 2]];
            if (var11 == var7 || var7 == var8 || var11 == var8) continue;
            var9.add(PosedGltfMesh.key(var11, var7, var8));
        }
        return var9.size();
    }

    private static long key(int var0, int var1, int var2) {
        int var3 = Math.min(var0, Math.min(var1, var2));
        int var4 = Math.max(var0, Math.max(var1, var2));
        int var5 = var0 + var1 + var2 - var3 - var4;
        return ((long)var3 * 2654435761L + (long)var5) * 2246822519L + (long)var4;
    }

    int[] separate(int[] var1) {
        HashMap<Long, Integer> var2 = new HashMap<Long, Integer>();
        ArrayList<Integer> var3 = new ArrayList<Integer>();
        ArrayList<Integer> var4 = new ArrayList<Integer>();
        for (int var5 = 0; var5 < this.triangles; ++var5) {
            int var6 = GltfCharacterBaker.owner(var1, this.tris[var5 * 3], this.tris[var5 * 3 + 1], this.tris[var5 * 3 + 2]);
            for (int var7 = 0; var7 < 3; ++var7) {
                int var8 = this.tris[var5 * 3 + var7];
                if (var1[var8] == var6) continue;
                Integer var9 = (Integer)var2.get((long)var8 * 8L + (long)var6);
                if (var9 == null) {
                    var9 = this.vertices + var3.size();
                    var2.put((long)var8 * 8L + (long)var6, var9);
                    var3.add(var8);
                    var4.add(var6);
                }
                this.tris[var5 * 3 + var7] = var9;
            }
        }
        int var10 = this.vertices;
        this.grow(var3.size(), 0);
        int[] var11 = Arrays.copyOf(var1, var10 + var3.size());
        for (int var12 = 0; var12 < var3.size(); ++var12) {
            int var13 = (Integer)var3.get(var12);
            int var14 = var10 + var12;
            System.arraycopy(this.pos, var13 * 3, this.pos, var14 * 3, 3);
            System.arraycopy(this.nrm, var13 * 3, this.nrm, var14 * 3, 3);
            System.arraycopy(this.uv, var13 * 2, this.uv, var14 * 2, 2);
            System.arraycopy(this.influence, var13 * 4, this.influence, var14 * 4, 4);
            System.arraycopy(this.weight, var13 * 4, this.weight, var14 * 4, 4);
            this.material[var14] = this.material[var13];
            this.color[var14] = this.color[var13];
            var11[var14] = (Integer)var4.get(var12);
        }
        this.vertices += var3.size();
        return var11;
    }

    void normalise(float var1, boolean var2) {
        this.flipped = var2;
        if (var2) {
            for (int var3 = 0; var3 < this.vertices; ++var3) {
                this.pos[var3 * 3] = -this.pos[var3 * 3];
                this.pos[var3 * 3 + 2] = -this.pos[var3 * 3 + 2];
                this.nrm[var3 * 3] = -this.nrm[var3 * 3];
                this.nrm[var3 * 3 + 2] = -this.nrm[var3 * 3 + 2];
            }
        }
        float[] var5 = this.bounds();
        this.scale = var1 / Math.max(1.0E-6f, var5[4] - var5[1]);
        this.shiftX = -(var5[0] + var5[3]) * 0.5f;
        this.shiftY = -var5[1];
        this.shiftZ = -(var5[2] + var5[5]) * 0.5f;
        for (int var4 = 0; var4 < this.vertices; ++var4) {
            this.pos[var4 * 3] = (this.pos[var4 * 3] + this.shiftX) * this.scale;
            this.pos[var4 * 3 + 1] = (this.pos[var4 * 3 + 1] + this.shiftY) * this.scale;
            this.pos[var4 * 3 + 2] = (this.pos[var4 * 3 + 2] + this.shiftZ) * this.scale;
        }
    }

    float[] bounds() {
        float[] var10000 = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -3.4028235E38f, -3.4028235E38f, -3.4028235E38f};
        float[] var1 = var10000;
        for (int var2 = 0; var2 < this.vertices; ++var2) {
            for (int var3 = 0; var3 < 3; ++var3) {
                var1[var3] = Math.min(var1[var3], this.pos[var2 * 3 + var3]);
                var1[var3 + 3] = Math.max(var1[var3 + 3], this.pos[var2 * 3 + var3]);
            }
        }
        return var1;
    }

    float[] world(int var1) {
        float[] var2 = this.globals[var1];
        return var2 == null ? new float[3] : GltfTransforms.point(var2, 0.0f, 0.0f, 0.0f);
    }

    float[] joint(int var1) {
        float[] var2 = this.world(var1);
        float var3 = this.flipped ? -var2[0] : var2[0];
        float var4 = this.flipped ? -var2[2] : var2[2];
        float[] var10000 = new float[]{(var3 + this.shiftX) * this.scale, (var2[1] + this.shiftY) * this.scale, (var4 + this.shiftZ) * this.scale};
        return var10000;
    }

    boolean under(int var1, int var2) {
        int var3 = var1;
        for (int var4 = 0; var3 >= 0 && var4 <= this.parent.length; ++var4) {
            if (var3 == var2) {
                return true;
            }
            var3 = this.parent[var3];
        }
        return false;
    }

    float[] below(int var1) {
        float[] var2 = new float[this.vertices];
        for (int var3 = 0; var3 < this.vertices; ++var3) {
            for (int var4 = 0; var4 < 4; ++var4) {
                int var5 = this.influence[var3 * 4 + var4];
                if (var5 < 0 || !this.under(var5, var1)) continue;
                int n = var3;
                var2[n] = var2[n] + this.weight[var3 * 4 + var4];
            }
        }
        return var2;
    }
}

