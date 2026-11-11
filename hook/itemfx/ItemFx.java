/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10444
 *  net.minecraft.class_10444$class_10445
 *  net.minecraft.class_10444$class_10446
 *  net.minecraft.class_11659
 *  net.minecraft.class_1297
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_2394
 *  net.minecraft.class_2398
 *  net.minecraft.class_2960
 *  net.minecraft.class_4587
 *  net.minecraft.class_7833
 *  net.minecraft.class_7923
 *  net.minecraft.class_811
 *  net.minecraft.class_9334
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package hook.itemfx;

import hook.feature.CustomToolsModule;
import hook.feature.CustomTotemModule;
import hook.halloween.HalloweenParticles;
import hook.itemfx.Drips;
import hook.itemfx.ItemFxRenderer;
import hook.itemfx.ItemFxState;
import hook.mixin.ItemFxLayerAccessor;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_1297;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_7923;
import net.minecraft.class_811;
import net.minecraft.class_9334;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public final class ItemFx {
    static final int KIND_TOTEM = 1;
    static final int KIND_TOOL = 2;
    private static final Map<class_1792, Integer> CLASSES = new IdentityHashMap<class_1792, Integer>();
    private static final Map<Integer, Object> KEYS = new HashMap<Integer, Object>();
    private static final String[] TIERS = new String[]{"wooden_", "stone_", "copper_", "iron_", "golden_", "diamond_", "netherite_"};
    private static final Random RANDOM = new Random();
    private static final long START = System.nanoTime();
    private static long lastFrame;
    private static long frames;
    private static float time;
    private static float floatingAct;
    private static int dripModel;

    private ItemFx() {
    }

    static int kind(int var0) {
        return var0 & 0xF;
    }

    static int type(int var0) {
        return var0 >> 4 & 0xF;
    }

    static int tier(int var0) {
        return var0 >> 8 & 0xF;
    }

    static int style(int var0) {
        return var0 >> 12 & 0xF;
    }

    static boolean animations(int var0) {
        return (var0 >> 16 & 1) != 0;
    }

    static boolean particles(int var0) {
        return (var0 >> 17 & 1) != 0;
    }

    static boolean popFx(int var0) {
        return (var0 >> 18 & 1) != 0;
    }

    static int design(int var0) {
        int var1 = ItemFx.style(var0);
        if (ItemFx.kind(var0) == 1) {
            return switch (var1) {
                case 1 -> 5;
                case 2 -> 1;
                case 3 -> 2;
                default -> 4;
            };
        }
        return var1 == 0 ? 6 : 3;
    }

    static float shaderStyle(int var0) {
        return ItemFx.kind(var0) == 2 && ItemFx.style(var0) == 2 ? 1.0f : 0.0f;
    }

    static float intensity(int var0) {
        return (float)(var0 >> 20 & 0x7F) / 100.0f * 1.3f;
    }

    static boolean spookyLike(int var0) {
        return var0 == 1 || var0 == 4;
    }

    static boolean chromeLike(int var0) {
        return var0 == 2 || var0 == 5;
    }

    static float tint(int var0) {
        int var1 = ItemFx.tier(var0);
        return var1 != 3 && var1 != 5 && var1 != 6 ? 0.0f : 1.0f;
    }

    static int glowCount(int var0, float var1) {
        boolean var2 = var1 >= 0.0f && ItemFx.popFx(var0);
        boolean var3 = ItemFx.particles(var0);
        int var4 = ItemFx.design(var0);
        if (var4 == 6) {
            return var3 ? 10 : 0;
        }
        return switch (var4) {
            case 1, 4 -> {
                if (var2) {
                    yield 65;
                }
                if (var3) {
                    yield 32;
                }
                yield 0;
            }
            case 2, 5 -> {
                if (var2) {
                    yield 30;
                }
                if (var3) {
                    yield 8;
                }
                yield 0;
            }
            default -> {
                if (!var3) {
                    yield 0;
                }
                if (ItemFx.shaderStyle(var0) == 1.0f) {
                    yield 10;
                }
                switch (ItemFx.tier(var0)) {
                    case 2: 
                    case 3: {
                        yield 2;
                    }
                    case 4: {
                        yield 3;
                    }
                    case 5: {
                        yield 4;
                    }
                    case 6: {
                        yield 7;
                    }
                }
                yield 0;
            }
        };
    }

    static int smokeCount(int var0, float var1) {
        boolean var2;
        if (!ItemFx.spookyLike(ItemFx.design(var0))) {
            return 0;
        }
        boolean bl = var2 = var1 >= 0.0f && ItemFx.popFx(var0);
        return var2 ? 20 : (ItemFx.particles(var0) ? 8 : 0);
    }

    public static void frame() {
        long var0 = System.nanoTime();
        float var2 = lastFrame == 0L ? 0.016f : Math.min(0.1f, (float)(var0 - lastFrame) / 1.0E9f);
        lastFrame = var0;
        ++frames;
        time = (float)((double)(var0 - START) / 1.0E9 % 3600.0);
        if (CustomTotemModule.chromeActive()) {
            float[] var4;
            int var3;
            int n = var3 = CustomTotemModule.designIndex() == 1 ? 202 : 2;
            if (var3 != dripModel && (var4 = ItemFxRenderer.anchors(var3)) != null) {
                Drips.reset();
                Drips.setAnchors(var4);
                dripModel = var3;
            }
            Drips.update(var2);
        }
    }

    static float time() {
        return time;
    }

    static long frameIndex() {
        return frames;
    }

    public static void floating(float var0) {
        floatingAct = var0;
    }

    private static int classOf(class_1792 var0) {
        String var4;
        int var3;
        class_2960 var1 = class_7923.field_41178.method_10221((Object)var0);
        if (!"minecraft".equals(var1.method_12836())) {
            return 0;
        }
        String var2 = var1.method_12832();
        if (var2.equals("totem_of_undying")) {
            return 1;
        }
        if (var2.endsWith("_sword")) {
            var3 = 0;
            var4 = var2.substring(0, var2.length() - 5);
        } else if (var2.endsWith("_pickaxe")) {
            var3 = 1;
            var4 = var2.substring(0, var2.length() - 7);
        } else if (var2.endsWith("_axe")) {
            var3 = 2;
            var4 = var2.substring(0, var2.length() - 3);
        } else if (var2.endsWith("_shovel")) {
            var3 = 3;
            var4 = var2.substring(0, var2.length() - 6);
        } else {
            if (!var2.endsWith("_hoe")) {
                return 0;
            }
            var3 = 4;
            var4 = var2.substring(0, var2.length() - 3);
        }
        for (int var5 = 0; var5 < TIERS.length; ++var5) {
            if (!var4.equals(TIERS[var5])) continue;
            return 2 | var3 << 4 | var5 << 8;
        }
        return 0;
    }

    static int classify(class_1799 var0) {
        class_1792 var1 = var0.method_7909();
        Integer var2 = CLASSES.get(var1);
        if (var2 == null) {
            var2 = ItemFx.classOf(var1);
            CLASSES.put(var1, var2);
        }
        if (var2 == 0) {
            return 0;
        }
        class_2960 var3 = (class_2960)var0.method_58694(class_9334.field_54199);
        return var3 != null && var3.equals((Object)class_7923.field_41178.method_10221((Object)var1)) ? var2 : 0;
    }

    public static void tag(class_10444 var0, class_1799 var1, class_811 var2) {
        int var4;
        ItemFxState var3 = (ItemFxState)var0;
        var3.hook$setFx(0);
        if (!var0.method_65606() && !var1.method_7960() && (var4 = ItemFx.classify(var1)) != 0) {
            boolean var6;
            int var5;
            if (ItemFx.kind(var4) == 1) {
                if (!CustomTotemModule.active()) {
                    return;
                }
                int var7 = (int)Math.round(CustomTotemModule.glow.getValue());
                var5 = var4 | CustomTotemModule.designIndex() << 12 | (CustomTotemModule.animations.getValue() ? 65536 : 0) | (CustomTotemModule.particles.getValue() ? 131072 : 0) | (CustomTotemModule.popEffects.getValue() ? 262144 : 0) | Math.max(0, Math.min(100, var7)) << 20;
                var6 = true;
            } else {
                if (!CustomToolsModule.active()) {
                    return;
                }
                int var9 = CustomToolsModule.styleIndex();
                if (var9 == 0 && (ItemFx.type(var4) != 1 || ItemFx.tier(var4) != 5 && ItemFx.tier(var4) != 6)) {
                    return;
                }
                int var8 = (int)Math.round(CustomToolsModule.strength.getValue());
                var5 = var4 | var9 << 12 | (CustomToolsModule.animations.getValue() ? 65536 : 0) | (CustomToolsModule.effects.getValue() ? 131072 : 0) | Math.max(0, Math.min(100, var8)) << 20;
                var6 = var9 == 0 || CustomToolsModule.animations.getValue() || CustomToolsModule.effects.getValue() || var1.method_7958();
            }
            var3.hook$setFx(var5);
            var0.method_70946(KEYS.computeIfAbsent(var5, var0x -> new Object()));
            int var10 = ItemFxRenderer.meshKey(ItemFx.design(var5), ItemFx.type(var5), ItemFx.tier(var5));
            if (var6 || !ItemFxRenderer.ready(var10)) {
                var0.method_70947();
            }
            var3.hook$layer0().method_67996(true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean render(int var0, class_811 var1, class_10444.class_10446 var2, class_4587 var3, class_11659 var4, int var5) {
        int var6 = ItemFxRenderer.meshKey(ItemFx.design(var0), ItemFx.type(var0), ItemFx.tier(var0));
        if (!ItemFxRenderer.prepare(var6)) {
            return false;
        }
        ItemFxLayerAccessor var7 = (ItemFxLayerAccessor)var2;
        int var8 = floatingAct >= 0.0f ? 3 : (var1 == class_811.field_4317 ? 2 : (var1 != class_811.field_4321 && var1 != class_811.field_4322 ? 0 : 1));
        var3.method_22903();
        try {
            var7.hook$transform().method_23075(var1.method_67675(), var3.method_23760());
            var3.method_46416(0.5f, 0.5f, 0.5f);
            if (var8 == 2 && ItemFx.design(var0) <= 3) {
                if (ItemFx.kind(var0) == 1) {
                    var3.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(-20.0f));
                    var3.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(7.0f));
                } else {
                    var3.method_22907((Quaternionfc)new Quaternionf().rotationAxis((float)Math.toRadians(26.0), 0.70710677f, 0.70710677f, 0.0f));
                }
            }
            boolean var9 = var7.hook$glint() != class_10444.class_10445.field_55341;
            ItemFxRenderer.submit(var4, var3, var0, var8, var5, var9, var8 == 3 ? floatingAct : -1.0f);
        }
        finally {
            var3.method_22909();
        }
        return true;
    }

    static void anim(Matrix4f var0, int var1, int var2, float var3, float var4) {
        var0.identity();
        if (ItemFx.kind(var1) == 1) {
            if (ItemFx.animations(var1)) {
                float var5 = var2 == 2 ? 0.55f : 1.0f;
                var0.translate(0.0f, 0.018f * var5 * (float)Math.sin(var4 * 1.6f), 0.0f);
                var0.rotateY(0.13f * var5 * (float)Math.sin(var4 * 0.7f));
                var0.rotateZ(0.025f * var5 * (float)Math.sin(var4 * 1.1f));
            }
            if (var3 >= 0.0f && ItemFx.popFx(var1)) {
                float var6 = 1.0f + 0.12f * (float)Math.exp(-var3 * 6.0f) * (float)Math.sin(var3 * 28.0f);
                var0.scale(var6);
            }
        }
    }

    public static boolean totemPop(class_1297 var0, class_2394 var1) {
        if (var1 == class_2398.field_11220 && CustomTotemModule.active() && CustomTotemModule.popEffects.getValue()) {
            double var2 = var0.method_23317();
            double var4 = var0.method_23318() + (double)var0.method_17682() * 0.55;
            double var6 = var0.method_23321();
            Random var8 = RANDOM;
            int var9 = CustomTotemModule.designIndex();
            if (var9 != 0 && var9 != 2) {
                for (int var15 = 0; var15 < 34; ++var15) {
                    float[] var21 = ItemFx.dir(var8, 2.6f + var8.nextFloat() * 2.8f);
                    HalloweenParticles.spawn(2, var2, var4, var6, var21[0], var21[1] + 1.2f, var21[2], 0.028f, 0.5f, 0.6f + var8.nextFloat() * 0.4f, 0.92f, 0.95f, 1.0f, 1.0f);
                }
                for (int var16 = 0; var16 < 20; ++var16) {
                    float[] var22 = ItemFx.dir(var8, 0.8f + var8.nextFloat() * 1.5f);
                    HalloweenParticles.spawn(1, var2, var4, var6, var22[0], var22[1], var22[2], 0.08f + var8.nextFloat() * 0.06f, 0.4f, 1.0f + var8.nextFloat() * 0.6f, 0.8f, 0.9f, 1.0f, 0.8f);
                }
                for (int var17 = 0; var17 < 18; ++var17) {
                    float[] var23 = ItemFx.dir(var8, 0.6f + var8.nextFloat());
                    HalloweenParticles.spawn(4, var2 + (double)var23[0] * 0.4, var4 + (double)var23[1] * 0.4, var6 + (double)var23[2] * 0.4, var23[0] * 0.3f, var23[1] * 0.3f, var23[2] * 0.3f, 0.03f, 1.0f, 1.6f + var8.nextFloat(), 1.0f, 1.0f, 1.0f, 1.0f);
                }
            } else {
                for (int var10 = 0; var10 < 40; ++var10) {
                    float[] var11 = ItemFx.dir(var8, 1.4f + var8.nextFloat() * 2.2f);
                    HalloweenParticles.spawn(3, var2 + (double)var11[0] * 0.15, var4 + (double)var11[1] * 0.15, var6 + (double)var11[2] * 0.15, var11[0], var11[1] + 0.8f, var11[2], 0.035f + var8.nextFloat() * 0.03f, 0.6f, 1.1f + var8.nextFloat() * 1.0f, 1.0f, 0.42f + var8.nextFloat() * 0.25f, 0.08f, 1.0f);
                }
                for (int var12 = 0; var12 < 18; ++var12) {
                    float[] var18 = ItemFx.dir(var8, 0.8f + var8.nextFloat() * 1.4f);
                    HalloweenParticles.spawn(1, var2, var4, var6, var18[0], var18[1], var18[2], 0.09f + var8.nextFloat() * 0.06f, 0.4f, 1.2f + var8.nextFloat() * 0.6f, 0.62f, 0.25f, 1.0f, 0.9f);
                }
                for (int var13 = 0; var13 < 12; ++var13) {
                    float[] var19 = ItemFx.dir(var8, 0.5f + var8.nextFloat() * 0.8f);
                    HalloweenParticles.spawn(0, var2 + (double)var19[0] * 0.3, var4 - 0.3 + (double)var19[1] * 0.3, var6 + (double)var19[2] * 0.3, var19[0], var19[1] * 0.4f + 0.3f, var19[2], 0.3f + var8.nextFloat() * 0.2f, 2.6f, 2.2f + var8.nextFloat() * 1.0f, 0.16f, 0.09f, 0.18f, 0.4f);
                }
                for (int var14 = 0; var14 < 14; ++var14) {
                    float[] var20 = ItemFx.dir(var8, 3.0f + var8.nextFloat() * 2.5f);
                    HalloweenParticles.spawn(2, var2, var4, var6, var20[0], var20[1] + 1.0f, var20[2], 0.025f, 0.5f, 0.5f + var8.nextFloat() * 0.3f, 1.0f, 0.6f, 0.15f, 1.0f);
                }
            }
            return true;
        }
        return false;
    }

    private static float[] dir(Random var0, float var1) {
        double var2 = var0.nextDouble() * Math.PI * 2.0;
        double var4 = var0.nextDouble() * 2.0 - 1.0;
        double var6 = Math.sqrt(1.0 - var4 * var4);
        return new float[]{(float)(Math.cos(var2) * var6) * var1, (float)var4 * var1 * 0.7f, (float)(Math.sin(var2) * var6) * var1};
    }

    public static void moduleOff() {
        if (!CustomTotemModule.active() && !CustomToolsModule.active()) {
            ItemFxRenderer.releaseModels();
            Drips.reset();
        }
    }

    static {
        floatingAct = -1.0f;
    }
}

