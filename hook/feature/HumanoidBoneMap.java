/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package hook.feature;

import com.google.gson.JsonElement;
import hook.feature.GlbDocument;
import hook.feature.PosedGltfMesh;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.IntPredicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class HumanoidBoneMap {
    static final int RIGHT = 0;
    static final int LEFT = 1;
    private static final Set<String> NOISE = Set.of("j", "bip", "bip01", "def", "deform", "mch", "org", "jnt", "jt", "bone", "bn", "b", "c", "x", "stretch", "valve", "biped", "mixamorig");
    private static final Set<String> LEFT_WORDS = Set.of("left", "l", "lf", "lft");
    private static final Set<String> RIGHT_WORDS = Set.of("right", "r", "rt", "rgt");
    private static final List<String> UPPER_ARM = List.of("upperarm", "uparm", "armupper");
    private static final List<String> FOREARM = List.of("forearm", "lowerarm", "elbow", "armlower", "loarm");
    private static final List<String> HAND = List.of("hand", "wrist");
    private static final List<String> THIGH = List.of("thigh", "upleg", "upperleg", "legupper");
    private static final List<String> SHIN = List.of("calf", "shin", "knee", "lowerleg", "leglower", "loleg");
    int head = -1;
    final int[] upperArm;
    final int[] forearm;
    final int[] hand;
    final int[] thigh;
    final int[] shin;
    boolean facesBack;
    private final PosedGltfMesh mesh;
    private final String[] core;
    private final int[] named;

    private HumanoidBoneMap(PosedGltfMesh var1) {
        int[] var10001 = new int[]{-1, -1};
        this.upperArm = var10001;
        var10001 = new int[]{-1, -1};
        this.forearm = var10001;
        var10001 = new int[]{-1, -1};
        this.hand = var10001;
        var10001 = new int[]{-1, -1};
        this.thigh = var10001;
        var10001 = new int[]{-1, -1};
        this.shin = var10001;
        this.mesh = var1;
        int var2 = var1.file.nodes();
        this.core = new String[var2];
        this.named = new int[var2];
        for (int var3 = 0; var3 < var2; ++var3) {
            List<String> var4 = HumanoidBoneMap.words(var1.file.nodeName(var3));
            this.named[var3] = -1;
            StringBuilder var5 = new StringBuilder();
            for (String var7Object : var4) {
                String var7 = var7Object;
                if (LEFT_WORDS.contains(var7)) {
                    this.named[var3] = 1;
                    continue;
                }
                if (RIGHT_WORDS.contains(var7)) {
                    this.named[var3] = 0;
                    continue;
                }
                if (NOISE.contains(var7)) continue;
                var5.append(var7);
            }
            this.core[var3] = var5.toString().replaceAll("\\d+$", "");
        }
    }

    static HumanoidBoneMap find(PosedGltfMesh var0, Map<String, String> var1) throws IOException {
        HumanoidBoneMap var2 = new HumanoidBoneMap(var0);
        if (var1.isEmpty()) {
            var2.facesBack = var2.facingBack();
            var2.head = var2.shallowest(var1x -> var2.core[var1x].startsWith("head"));
            for (int var12 = 0; var12 < 2; ++var12) {
                int var10;
                int var14;
                int var5;
                int var13 = var12;
                int var6 = var5 = var2.shallowest(var2x -> var2.side(var2x) == var13 && var2.is(var2x, FOREARM));
                int var7 = var2.shallowest(var4x -> var2.side(var4x) == var13 && (var2.is(var4x, UPPER_ARM) || var2.core[var4x].equals("arm")) && (var5 < 0 || var0.under(var5, var4x) && var4x != var5));
                if (var7 < 0 && var5 >= 0) {
                    var7 = var0.parent[var5];
                }
                if (var5 < 0 && var7 >= 0) {
                    var6 = var2.mainChild(var7);
                }
                var2.upperArm[var13] = var7;
                var2.forearm[var13] = var6;
                if (var6 >= 0) {
                    int var8 = var6;
                    int var9 = var2.shallowest(var3x -> var3x != var8 && var0.under(var3x, var8) && var2.is(var3x, HAND));
                    var2.hand[var13] = var9 >= 0 ? var9 : var2.mainChild(var8);
                }
                int var15 = var14 = var2.shallowest(var2x -> var2.side(var2x) == var13 && var2.is(var2x, SHIN));
                int var10BeforeFallback = var10 = var2.shallowest(var4x -> var2.side(var4x) == var13 && (var2.is(var4x, THIGH) || var2.core[var4x].equals("leg")) && (var14 < 0 || var0.under(var14, var4x) && var4x != var14));
                if (var14 < 0 && var10 >= 0 && (var15 = var2.shallowest(var3x -> var3x != var10BeforeFallback && var0.under(var3x, var10BeforeFallback) && var2.core[var3x].equals("leg"))) < 0) {
                    var15 = var2.mainChild(var10);
                }
                if (var10 < 0 && var15 >= 0) {
                    var10 = var0.parent[var15];
                }
                var2.thigh[var13] = var10;
                var2.shin[var13] = var15;
            }
            return var2;
        }
        var2.head = var2.named(var1, "head");
        for (int var3 = 0; var3 < 2; ++var3) {
            String var4 = var3 == 0 ? "right " : "left ";
            var2.upperArm[var3] = var2.named(var1, var4 + "arm");
            var2.forearm[var3] = var2.named(var1, var4 + "elbow");
            var2.hand[var3] = var2.named(var1, var4 + "hand");
            var2.thigh[var3] = var2.named(var1, var4 + "leg");
            var2.shin[var3] = var2.named(var1, var4 + "knee");
        }
        var2.facesBack = var2.upperArm[1] >= 0 && var2.upperArm[0] >= 0 && var0.world(var2.upperArm[1])[0] < var0.world(var2.upperArm[0])[0];
        return var2;
    }

    private boolean is(int var1, List<String> var2) {
        for (String var4 : var2) {
            if (!this.core[var1].contains(var4)) continue;
            return true;
        }
        return false;
    }

    private int named(Map<String, String> var1, String var2) throws IOException {
        String var3 = var1.get(var2);
        if (var3 == null) {
            return -1;
        }
        for (int var4 = 0; var4 < this.core.length; ++var4) {
            if (!this.mesh.file.nodeName(var4).equalsIgnoreCase(var3)) continue;
            return var4;
        }
        String var6 = var3.toLowerCase(Locale.ROOT);
        for (int var5 = 0; var5 < this.core.length; ++var5) {
            if (!this.mesh.file.nodeName(var5).toLowerCase(Locale.ROOT).startsWith(var6)) continue;
            return var5;
        }
        throw new IOException("no bone named " + var3);
    }

    private static List<String> words(String var0) {
        String var1 = var0.replaceAll("_\\d+$", "").replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase(Locale.ROOT).replace("\u624b\u9996", " wrist ").replace("\u8db3\u9996", " ankle ").replace("\u5de6", " left ").replace("\u53f3", " right ").replace("\u3072\u3058", " elbow ").replace("\u3072\u3056", " knee ").replace("\u8155", " arm ").replace("\u8db3", " leg ").replace("\u982d", " head ").replace("\u9996", " neck ");
        ArrayList<String> var2 = new ArrayList<String>();
        for (String var6 : var1.split("[^a-z0-9]+")) {
            if (var6.isEmpty()) continue;
            var2.add(var6);
        }
        return var2;
    }

    private boolean facingBack() {
        int var1 = this.shallowest(var1x -> this.named[var1x] == 1 && this.is(var1x, HAND));
        int var2 = this.shallowest(var1x -> this.named[var1x] == 0 && this.is(var1x, HAND));
        if (var1 < 0 || var2 < 0) {
            var1 = this.shallowest(var1x -> this.named[var1x] == 1 && this.is(var1x, SHIN));
            var2 = this.shallowest(var1x -> this.named[var1x] == 0 && this.is(var1x, SHIN));
        }
        if (var1 >= 0 && var2 >= 0) {
            return this.mesh.world(var1)[0] < this.mesh.world(var2)[0];
        }
        float[] var3 = this.mesh.bounds();
        float var4 = var3[4] - var3[1];
        float var5 = 0.0f;
        float var6 = 0.0f;
        int var7 = 0;
        int var8 = 0;
        for (int var9 = 0; var9 < this.mesh.vertices; ++var9) {
            float var10 = this.mesh.pos[var9 * 3 + 1] - var3[1];
            float var11 = this.mesh.pos[var9 * 3 + 2];
            if (var10 < var4 * 0.04f) {
                var5 += var11;
                ++var7;
                continue;
            }
            if (!(var10 > var4 * 0.1f) || !(var10 < var4 * 0.2f)) continue;
            var6 += var11;
            ++var8;
        }
        return var7 > 0 && var8 > 0 && var5 / (float)var7 < var6 / (float)var8 - var4 * 0.01f;
    }

    private int side(int var1) {
        if (this.named[var1] >= 0) {
            return this.named[var1];
        }
        return this.mesh.world(var1)[0] < 0.0f != this.facesBack ? 0 : 1;
    }

    private int shallowest(IntPredicate var1) {
        int var2 = -1;
        int var3 = Integer.MAX_VALUE;
        for (int var5 : this.mesh.joints) {
            int var6;
            if (!var1.test(var5)) continue;
            int var7 = this.mesh.parent[var5];
            for (var6 = 0; var7 >= 0 && var6 <= this.core.length; ++var6) {
                var7 = this.mesh.parent[var7];
            }
            if (var6 >= var3 && (var6 != var3 || var5 >= var2)) continue;
            var2 = var5;
            var3 = var6;
        }
        return var2;
    }

    private int mainChild(int var1) {
        int var2 = -1;
        int var3 = -1;
        for (JsonElement var5 : GlbDocument.array(this.mesh.file.node(var1), "children")) {
            int var6 = var5.getAsInt();
            int var7 = 0;
            for (int var8 = 0; var8 < this.core.length; ++var8) {
                if (!this.mesh.under(var8, var6)) continue;
                ++var7;
            }
            if (var7 <= var3) continue;
            var2 = var6;
            var3 = var7;
        }
        return var2;
    }

    Map<Integer, Float> armTurns() {
        HashMap<Integer, Float> var1 = new HashMap<Integer, Float>();
        for (int var2 = 0; var2 < 2; ++var2) {
            if (this.upperArm[var2] < 0 || this.forearm[var2] < 0) continue;
            float[] var3 = this.mesh.world(this.upperArm[var2]);
            float[] var4 = this.mesh.world(this.forearm[var2]);
            float var5 = var4[0] - var3[0];
            float var6 = var4[1] - var3[1];
            float var7 = (float)Math.toDegrees(Math.atan2(Math.abs(var5), -var6));
            if (!(var7 > 40.0f)) continue;
            var1.put(this.upperArm[var2], Float.valueOf((var5 < 0.0f ? 1.0f : -1.0f) * (var7 - 10.0f)));
        }
        return var1;
    }

    int[] groups() {
        int[] var1 = new int[this.core.length];
        for (int var2 = 0; var2 < var1.length; ++var2) {
            var1[var2] = 1;
            if (this.head >= 0 && this.mesh.under(var2, this.head)) {
                var1[var2] = 0;
                continue;
            }
            if (this.upperArm[0] >= 0 && this.mesh.under(var2, this.upperArm[0])) {
                var1[var2] = 2;
                continue;
            }
            if (this.upperArm[1] >= 0 && this.mesh.under(var2, this.upperArm[1])) {
                var1[var2] = 3;
                continue;
            }
            if (this.thigh[0] >= 0 && this.mesh.under(var2, this.thigh[0])) {
                var1[var2] = 4;
                continue;
            }
            if (this.thigh[1] < 0 || !this.mesh.under(var2, this.thigh[1])) continue;
            var1[var2] = 5;
        }
        return var1;
    }

    boolean found() {
        return this.head >= 0 || this.upperArm[0] >= 0 || this.upperArm[1] >= 0 || this.thigh[0] >= 0 || this.thigh[1] >= 0;
    }

    String describe() {
        StringBuilder var1 = new StringBuilder();
        var1.append("facing ").append(this.facesBack ? "-z (turned around)" : "+z").append('\n');
        this.line(var1, "head", this.head);
        for (int var2 = 0; var2 < 2; ++var2) {
            String var3 = var2 == 0 ? "right " : "left ";
            this.line(var1, var3 + "arm", this.upperArm[var2]);
            this.line(var1, var3 + "elbow", this.forearm[var2]);
            this.line(var1, var3 + "hand", this.hand[var2]);
            this.line(var1, var3 + "leg", this.thigh[var2]);
            this.line(var1, var3 + "knee", this.shin[var2]);
        }
        return var1.toString();
    }

    private void line(StringBuilder var1, String var2, int var3) {
        Locale var10001 = Locale.ROOT;
        String var10002 = "%-11s %s%n";
        Object[] var10003 = new Object[]{var2, var3 < 0 ? "-" : this.mesh.file.nodeName(var3)};
        var1.append(String.format(var10001, var10002, var10003));
    }
}

