/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_1297
 *  net.minecraft.class_1922
 *  net.minecraft.class_1944
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350$class_2351
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2902$class_2903
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_3419
 *  net.minecraft.class_3532
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_5819
 *  net.minecraft.class_638
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3d
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package hook.halloween;

import hook.halloween.CinematicCamera;
import hook.halloween.HalloweenAtmosphere;
import hook.halloween.HalloweenParticles;
import hook.halloween.witch.WitchAsset;
import hook.halloween.witch.WitchPerformance;
import hook.halloween.witch.WitchRenderer;
import hook.halloween.witch.WitchRig;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1297;
import net.minecraft.class_1922;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2902;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_5819;
import net.minecraft.class_638;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class WitchScene {
    private static CompletableFuture<WitchAsset> loading;
    private static WitchAsset asset;
    private static WitchRig rig;
    private static WitchPerformance perf;
    private static boolean running;
    private static float t;
    private static boolean laughed;
    private static boolean puffed;
    private static float acc;
    private static float trailAcc;
    private static final List<Delayed> SOUNDS;
    private static final class_5819 SOUND_RANDOM;
    private static final WitchRenderer.Look LOOK;

    private WitchScene() {
    }

    public static void preload() {
        if (asset == null && loading == null) {
            loading = CompletableFuture.supplyAsync(() -> {
                try {
                    WitchAsset var1;
                    try (InputStream var0 = WitchScene.class.getResourceAsStream("/assets/hook/witch/witch.kwm");){
                        if (var0 == null) {
                            throw new IllegalStateException("missing witch model");
                        }
                        var1 = WitchAsset.load(var0);
                    }
                    return var1;
                }
                catch (Exception var5) {
                    throw new RuntimeException(var5);
                }
            });
        }
    }

    private static boolean ready() {
        if (asset != null) {
            return true;
        }
        WitchScene.preload();
        try {
            asset = loading.join();
        }
        catch (Throwable var1) {
            System.err.println("Hook: witch model failed to load: " + String.valueOf(var1));
            asset = null;
        }
        loading = null;
        return asset != null;
    }

    public static boolean start() {
        class_310 var0 = class_310.method_1551();
        if (var0.field_1724 != null && var0.field_1687 != null && WitchScene.ready()) {
            rig = new WitchRig(asset);
            perf = new WitchPerformance(rig);
            class_243 var1 = var0.field_1724.method_5836(1.0f);
            class_243 var2 = WitchScene.place(var0, var1);
            class_638 var3 = var0.field_1687;
            perf.start(new Vector3d(var2.field_1352, var2.field_1351, var2.field_1350), new Vector3d(var1.field_1352, var1.field_1351, var1.field_1350), (var1x, var3x) -> {
                int var6;
                int var5 = class_3532.method_15357((double)var1x);
                return var3.method_8393(var5 >> 4, (var6 = class_3532.method_15357((double)var3x)) >> 4) ? (double)var3.method_8624(class_2902.class_2903.field_13197, var5, var6) : Double.NaN;
            });
            t = 0.0f;
            laughed = false;
            puffed = false;
            acc = 0.0f;
            trailAcc = 0.0f;
            running = true;
            SOUNDS.clear();
            return true;
        }
        return false;
    }

    private static class_243 place(class_310 var0, class_243 var1) {
        class_638 var2 = var0.field_1687;
        float var3 = var0.field_1724.method_36454();
        double var4 = -Math.sin(Math.toRadians(var3));
        double var6 = Math.cos(Math.toRadians(var3));
        block0: for (double var8 = 10.0; var8 >= 5.0; var8 -= 0.5) {
            double var10 = var1.field_1352 + var4 * var8;
            double var12 = var1.field_1350 + var6 * var8;
            int var14 = class_3532.method_15357((double)var10);
            int var15 = class_3532.method_15357((double)var12);
            for (int var16 = class_3532.method_15357((double)var1.field_1351) + 4; var16 >= class_3532.method_15357((double)var1.field_1351) - 10; --var16) {
                class_2338 var17 = new class_2338(var14, var16, var15);
                if (var2.method_8320(var17).method_26220((class_1922)var2, var17).method_1110() || !var2.method_8320(var17.method_10084()).method_26220((class_1922)var2, var17.method_10084()).method_1110() || !var2.method_8320(var17.method_10086(2)).method_26220((class_1922)var2, var17.method_10086(2)).method_1110()) continue;
                double var18 = (double)var16 + var2.method_8320(var17).method_26220((class_1922)var2, var17).method_1105(class_2350.class_2351.field_11052);
                class_243 var20 = new class_243(var10, var18, var12);
                class_3965 var21 = var2.method_17742(new class_3959(var1, var20.method_1031(0.0, 1.2, 0.0), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)var0.field_1724));
                if (var21.method_17783() != class_239.class_240.field_1333) continue block0;
                return var20;
            }
        }
        return new class_243(var1.field_1352 + var4 * 7.0, var1.field_1351 - 1.62, var1.field_1350 + var6 * 7.0);
    }

    public static boolean running() {
        return running;
    }

    public static void hurry() {
        if (running && perf != null) {
            perf.exit();
        }
    }

    private static float smooth(float var0) {
        var0 = Math.max(0.0f, Math.min(1.0f, var0));
        return var0 * var0 * (3.0f - 2.0f * var0);
    }

    public static void update(float var0) {
        if (!running) {
            WitchScene.flushSounds(var0);
        } else {
            class_310 var1 = class_310.method_1551();
            if (var1.field_1724 != null && var1.field_1687 != null) {
                float var33;
                float var31;
                float var27;
                class_243 var2 = var1.field_1724.method_5836(1.0f);
                perf.update(t += var0, new Vector3d(var2.field_1352, var2.field_1351, var2.field_1350));
                Vector3d var3 = WitchScene.perf.origin;
                Vector3f var4 = perf.center(new Vector3f());
                class_243 var5 = new class_243(var3.x + (double)var4.x, var3.y + (double)var4.y, var3.z + (double)var4.z);
                Random var6 = HalloweenParticles.random();
                if (t < 2.8000002f) {
                    acc += var0 * 60.0f;
                    while (acc >= 1.0f) {
                        acc -= 1.0f;
                        double var7 = var6.nextDouble() * Math.PI * 2.0;
                        double var9 = 0.55 + var6.nextDouble() * 0.35;
                        float var11 = 0.9f;
                        if (var6.nextFloat() < 0.35f) {
                            HalloweenParticles.spawn(0, var3.x + Math.cos(var7) * var9, var3.y + 0.1 + var6.nextDouble() * 0.5, var3.z + Math.sin(var7) * var9, (float)(-Math.sin(var7) * (double)var11), 0.25f + var6.nextFloat() * 0.3f, (float)(Math.cos(var7) * (double)var11), 0.45f + var6.nextFloat() * 0.3f, 2.4f, 2.4f + var6.nextFloat(), 0.2f, 0.15f, 0.27f, 0.55f);
                            continue;
                        }
                        Vector3f var12 = perf.bodyAt(Math.max(0.05f, WitchScene.perf.dissolveAt), (float)var7, new Vector3f());
                        HalloweenParticles.spawn(var6.nextFloat() < 0.5f ? 1 : 3, var3.x + (double)var12.x, var3.y + (double)var12.y, var3.z + (double)var12.z, (float)Math.cos(var7) * 0.35f, 0.25f + var6.nextFloat() * 0.4f, (float)Math.sin(var7) * 0.35f, 0.05f + var6.nextFloat() * 0.04f, 0.4f, 0.9f + var6.nextFloat() * 0.8f, 1.0f, 0.55f + var6.nextFloat() * 0.25f, 0.2f, 0.9f);
                    }
                }
                if (t > 2.2f && t < 11.9f) {
                    trailAcc += var0 * 7.0f;
                    while (trailAcc >= 1.0f) {
                        trailAcc -= 1.0f;
                        double var18 = var6.nextDouble() * Math.PI * 2.0;
                        HalloweenParticles.spawn(4, var3.x + Math.cos(var18) * 0.8, var3.y + 0.3 + var6.nextDouble() * 1.5, var3.z + Math.sin(var18) * 0.8, (float)(-Math.sin(var18)) * 0.12f, 0.05f, (float)Math.cos(var18) * 0.12f, 0.025f, 0.6f, 2.2f + var6.nextFloat(), 0.8f, 0.7f, 1.0f, 0.75f);
                    }
                }
                if (WitchScene.perf.casting > 0.01f) {
                    Vector3f var19 = perf.rightHand(new Vector3f());
                    acc += var0 * 70.0f * WitchScene.perf.casting;
                    while (acc >= 1.0f) {
                        acc -= 1.0f;
                        float var8 = t * 7.0f + var6.nextFloat() * 0.6f;
                        float var28 = 0.25f + var6.nextFloat() * 0.25f;
                        float var10 = 1.4f + var6.nextFloat() * 0.8f;
                        boolean var34 = var6.nextFloat() < 0.55f;
                        HalloweenParticles.spawn(var6.nextFloat() < 0.4f ? 2 : 1, var3.x + (double)var19.x, var3.y + (double)var19.y, var3.z + (double)var19.z, (float)Math.cos(var8) * var28 * 3.0f, var10, (float)Math.sin(var8) * var28 * 3.0f, 0.04f + var6.nextFloat() * 0.04f, 0.5f, 1.1f + var6.nextFloat() * 0.9f, var34 ? 1.0f : 0.72f, var34 ? 0.6f : 0.45f, var34 ? 0.25f : 1.0f, 0.95f);
                    }
                }
                if (!puffed && t >= 12.799999f) {
                    puffed = true;
                    for (int var20 = 0; var20 < 26; ++var20) {
                        double var24 = var6.nextDouble() * Math.PI * 2.0;
                        HalloweenParticles.spawn(0, var3.x + Math.cos(var24) * 0.4, var3.y + 0.15, var3.z + Math.sin(var24) * 0.4, (float)Math.cos(var24) * 1.6f, 0.2f + var6.nextFloat() * 0.3f, (float)Math.sin(var24) * 1.6f, 0.4f + var6.nextFloat() * 0.3f, 2.6f, 2.0f + var6.nextFloat(), 0.22f, 0.17f, 0.28f, 0.5f);
                    }
                }
                if (!laughed && t >= 14.7f) {
                    laughed = true;
                    WitchScene.sound(class_3417.field_19153, var5.method_1031(0.0, 0.6, 0.0), 1.9f, 0.86f, 0.0f);
                    WitchScene.sound(class_3417.field_19153, var5.method_1031(0.0, 2.5, 0.0), 1.3f, 0.84f, 0.22f);
                    WitchScene.sound(class_3417.field_19153, var5.method_1031(0.0, 5.0, 0.0), 0.95f, 0.81f, 0.5f);
                    WitchScene.sound(class_3417.field_14869, var5, 0.7f, 0.6f, 0.05f);
                }
                if (t > 13.9f && WitchScene.perf.alpha > 0.02f) {
                    Vector3f var21 = perf.broomTail(new Vector3f());
                    Vector3f var25 = perf.velocity(new Vector3f());
                    float var29 = var25.length();
                    trailAcc += var0 * (8.0f + 40.0f * Math.min(1.0f, var29 / 6.0f)) * WitchScene.perf.alpha;
                    while (trailAcc >= 1.0f) {
                        trailAcc -= 1.0f;
                        float var32 = var6.nextFloat();
                        double var35 = var3.x + (double)var21.x + (var6.nextDouble() - 0.5) * 0.18;
                        double var13 = var3.y + (double)var21.y + (var6.nextDouble() - 0.5) * 0.18;
                        double var15 = var3.z + (double)var21.z + (var6.nextDouble() - 0.5) * 0.18;
                        if (var32 < 0.22f) {
                            HalloweenParticles.spawn(0, var35, var13, var15, -var25.x * 0.15f, -var25.y * 0.15f + 0.1f, -var25.z * 0.15f, 0.18f + var6.nextFloat() * 0.12f, 3.0f, 1.4f + var6.nextFloat() * 0.8f, 0.24f, 0.19f, 0.3f, 0.42f * WitchScene.perf.alpha);
                            continue;
                        }
                        if (var32 < 0.62f) {
                            HalloweenParticles.spawn(3, var35, var13, var15, -var25.x * 0.25f + (var6.nextFloat() - 0.5f) * 0.6f, -var25.y * 0.2f, -var25.z * 0.25f + (var6.nextFloat() - 0.5f) * 0.6f, 0.03f + var6.nextFloat() * 0.025f, 0.5f, 1.0f + var6.nextFloat() * 1.2f, 1.0f, 0.5f + var6.nextFloat() * 0.25f, 0.15f, WitchScene.perf.alpha);
                            continue;
                        }
                        boolean var17 = var6.nextFloat() < 0.45f;
                        HalloweenParticles.spawn(2, var35, var13, var15, -var25.x * 0.4f + (var6.nextFloat() - 0.5f), -var25.y * 0.4f + (var6.nextFloat() - 0.5f), -var25.z * 0.4f + (var6.nextFloat() - 0.5f), 0.03f + var6.nextFloat() * 0.02f, 0.5f, 0.6f + var6.nextFloat() * 0.6f, var17 ? 0.75f : 1.0f, var17 ? 0.5f : 0.7f, var17 ? 1.0f : 0.35f, WitchScene.perf.alpha);
                    }
                }
                if (WitchScene.perf.dissolveMode < 0.0f && WitchScene.perf.alpha > 0.02f) {
                    acc += var0 * 90.0f;
                    while (acc >= 1.0f) {
                        acc -= 1.0f;
                        float var22 = var6.nextFloat() * 6.2832f;
                        Vector3f var26 = perf.bodyAt(WitchScene.perf.dissolveAt, var22, new Vector3f());
                        boolean var30 = var6.nextFloat() < 0.4f;
                        HalloweenParticles.spawn(var6.nextFloat() < 0.5f ? 1 : 4, var3.x + (double)var26.x, var3.y + (double)var26.y, var3.z + (double)var26.z, (float)Math.cos(var22) * 0.5f, 0.4f + var6.nextFloat() * 0.5f, (float)Math.sin(var22) * 0.5f, 0.06f + var6.nextFloat() * 0.05f, 0.3f, 1.2f + var6.nextFloat(), var30 ? 0.75f : 1.0f, var30 ? 0.5f : 0.62f, var30 ? 1.0f : 0.28f, 1.0f);
                    }
                }
                WitchScene.flushSounds(var0);
                float var23 = (float)var5.method_1022(var2);
                class_243 var36 = var5;
                if (t < 11.9f) {
                    var27 = 1.0f - 0.16f * WitchScene.smooth((t - 1.0f) / 5.0f) - 0.06f * WitchScene.perf.casting;
                    var31 = 4.5f;
                    var33 = 1.2f;
                    if (WitchScene.perf.casting > 0.0f) {
                        var36 = var5.method_1031(0.0, 0.5 * (double)WitchScene.perf.casting, 0.0);
                    }
                } else if (t < 14.7f) {
                    var27 = 0.86f;
                    var31 = 5.0f;
                    var33 = 1.6f;
                } else {
                    var27 = Math.max(0.34f, Math.min(0.86f, 9.5f / Math.max(1.0f, var23)));
                    var31 = 6.5f;
                    var33 = 2.4f;
                    Vector3f var37 = perf.velocity(new Vector3f());
                    var36 = var5.method_1031((double)var37.x * 0.2, (double)var37.y * 0.2, (double)var37.z * 0.2);
                }
                CinematicCamera.follow(var36, var31, var27, var33);
                if (WitchScene.perf.finished) {
                    running = false;
                }
            } else {
                WitchScene.reset();
            }
        }
    }

    public static boolean finished() {
        return !running;
    }

    private static void sound(class_3414 var0, class_243 var1, float var2, float var3, float var4) {
        SOUNDS.add(new Delayed(var0, var1, var2, var3, var4));
    }

    private static void flushSounds(float var0) {
        class_310 var1 = class_310.method_1551();
        for (int var2 = SOUNDS.size() - 1; var2 >= 0; --var2) {
            Delayed var3 = SOUNDS.get(var2);
            var3.delay -= var0;
            if (!(var3.delay <= 0.0f)) continue;
            SOUNDS.remove(var2);
            try {
                var1.method_1483().method_4873((class_1113)new class_1109(var3.event, class_3419.field_15250, var3.volume, var3.pitch, SOUND_RANDOM, var3.at.field_1352, var3.at.field_1351, var3.at.field_1350));
                continue;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public static void render(class_243 var0) {
        if (running && perf != null && !(WitchScene.perf.alpha <= 0.003f)) {
            class_310 var1 = class_310.method_1551();
            if (var1.field_1687 != null && WitchRenderer.ensure(asset)) {
                Vector3d var2 = WitchScene.perf.origin;
                Matrix4f var3 = new Matrix4f().translation((float)(var2.x - var0.field_1352), (float)(var2.y - var0.field_1351), (float)(var2.z - var0.field_1350)).mul((Matrix4fc)WitchScene.perf.model);
                Vector3f var4 = perf.center(new Vector3f());
                class_2338 var5 = class_2338.method_49637((double)(var2.x + (double)var4.x), (double)(var2.y + (double)var4.y), (double)(var2.z + (double)var4.z));
                WitchRenderer.Look var6 = LOOK;
                var6.alpha = WitchScene.perf.alpha;
                var6.block = var1.field_1687.method_8314(class_1944.field_9282, var5);
                var6.sky = var1.field_1687.method_8314(class_1944.field_9284, var5);
                var6.worldLight = 0.85f;
                var6.emissive = 1.4f * WitchScene.perf.glow;
                var6.rim = 0.35f + 0.45f * WitchScene.perf.glow;
                var6.dissolveMode = WitchScene.perf.dissolveMode;
                var6.dissolveAt = WitchScene.perf.dissolveAt;
                Vector4f var7 = HalloweenAtmosphere.moonDir();
                if (var7.w > 0.05f) {
                    Vector3f var8 = new Vector3f(var7.x, Math.max(0.35f, var7.y), var7.z).normalize();
                    var6.keyDir[0] = var8.x;
                    var6.keyDir[1] = var8.y;
                    var6.keyDir[2] = var8.z;
                } else {
                    var6.keyDir[0] = 0.3f;
                    var6.keyDir[1] = 0.8f;
                    var6.keyDir[2] = 0.4f;
                }
                var6.keyDir[3] = 1.0f;
                var6.fillColor[3] = 0.22f + 0.35f * WitchScene.perf.glow;
                WitchRenderer.draw(rig, var3, WitchScene.perf.broom, var6);
            }
        }
    }

    public static void reset() {
        running = false;
        perf = null;
        rig = null;
        SOUNDS.clear();
        WitchRenderer.release();
        asset = null;
        if (loading != null) {
            loading.cancel(true);
            loading = null;
        }
    }

    static {
        SOUNDS = new ArrayList<Delayed>();
        SOUND_RANDOM = class_5819.method_43047();
        LOOK = new WitchRenderer.Look();
    }

    private static final class Delayed {
        final class_3414 event;
        final class_243 at;
        final float volume;
        final float pitch;
        float delay;

        Delayed(class_3414 var1, class_243 var2, float var3, float var4, float var5) {
            this.event = var1;
            this.at = var2;
            this.volume = var3;
            this.pitch = var4;
            this.delay = var5;
        }
    }
}

